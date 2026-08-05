package com.resumainer.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Bounded in-memory limiter for resend-verification requests.
 *
 * <p>Email and IP keys are evaluated and reserved under one synchronization
 * boundary, so a denied dual-key decision never consumes only one quota.
 * Obsolete keys are removed lazily after the daily window. At capacity, active
 * keys are never evicted because eviction would permit a limit bypass.
 */
@Service
public class ResendVerificationRateLimiter {

    private final Clock clock;
    private final Duration cooldown;
    private final Duration hourlyWindow;
    private final int hourlyLimit;
    private final Duration dailyWindow;
    private final int dailyLimit;
    private final int maxRetainedKeys;
    private final Map<String, Bucket> buckets = new HashMap<>();

    @Autowired
    public ResendVerificationRateLimiter(
            Clock clock,
            @Value("${app.auth.resend.cooldown-seconds:60}") long cooldownSeconds,
            @Value("${app.auth.resend.hourly-limit:5}") int hourlyLimit,
            @Value("${app.auth.resend.daily-limit:20}") int dailyLimit,
            @Value("${app.auth.resend.max-retained-keys:10000}") int maxRetainedKeys) {
        this(clock, Duration.ofSeconds(cooldownSeconds), Duration.ofHours(1), hourlyLimit,
                Duration.ofDays(1), dailyLimit, maxRetainedKeys);
    }

    ResendVerificationRateLimiter(Clock clock, Duration cooldown, Duration hourlyWindow,
                                  int hourlyLimit, Duration dailyWindow, int dailyLimit,
                                  int maxRetainedKeys) {
        this.clock = clock;
        this.cooldown = cooldown;
        this.hourlyWindow = hourlyWindow;
        this.hourlyLimit = hourlyLimit;
        this.dailyWindow = dailyWindow;
        this.dailyLimit = dailyLimit;
        this.maxRetainedKeys = maxRetainedKeys;
    }

    /** Atomically checks and reserves independent normalized-email and IP keys. */
    public synchronized Decision reserve(String normalizedEmail, String clientIp) {
        Instant now = clock.instant();
        cleanup(now);
        String emailKey = "email:" + normalizedEmail;
        String ipKey = "ip:" + clientIp;
        Bucket email = buckets.get(emailKey);
        Bucket ip = buckets.get(ipKey);

        long retry = Math.max(retryAfter(email, now), retryAfter(ip, now));
        if (retry > 0) {
            return Decision.denied(retry);
        }

        int missing = (email == null ? 1 : 0) + (ip == null ? 1 : 0);
        if (buckets.size() + missing > maxRetainedKeys) {
            return Decision.denied(capacityRetryAfter(now));
        }

        buckets.computeIfAbsent(emailKey, ignored -> new Bucket()).accepted.addLast(now);
        buckets.computeIfAbsent(ipKey, ignored -> new Bucket()).accepted.addLast(now);
        return Decision.permit();
    }

    int retainedKeyCount() {
        synchronized (this) {
            return buckets.size();
        }
    }

    private long retryAfter(Bucket bucket, Instant now) {
        if (bucket == null || bucket.accepted.isEmpty()) {
            return 0;
        }
        prune(bucket, now);
        if (bucket.accepted.isEmpty()) {
            return 0;
        }
        long retry = secondsUntil(bucket.accepted.peekLast().plus(cooldown), now);
        retry = Math.max(retry, windowRetry(bucket, hourlyWindow, hourlyLimit, now));
        retry = Math.max(retry, windowRetry(bucket, dailyWindow, dailyLimit, now));
        return retry;
    }

    private long windowRetry(Bucket bucket, Duration window, int limit, Instant now) {
        Instant boundary = now.minus(window);
        int count = 0;
        Instant oldestInside = null;
        for (Instant accepted : bucket.accepted) {
            if (accepted.isAfter(boundary)) {
                if (oldestInside == null) {
                    oldestInside = accepted;
                }
                count++;
            }
        }
        return count >= limit && oldestInside != null
                ? secondsUntil(oldestInside.plus(window), now) : 0;
    }

    private long capacityRetryAfter(Instant now) {
        long retry = Long.MAX_VALUE;
        for (Bucket bucket : buckets.values()) {
            if (!bucket.accepted.isEmpty()) {
                retry = Math.min(retry,
                        secondsUntil(bucket.accepted.peekFirst().plus(dailyWindow), now));
            }
        }
        return retry == Long.MAX_VALUE ? 1 : Math.max(1, retry);
    }

    private long secondsUntil(Instant permittedAt, Instant now) {
        long millis = Duration.between(now, permittedAt).toMillis();
        return millis <= 0 ? 0 : Math.max(1, (millis + 999) / 1000);
    }

    private void cleanup(Instant now) {
        buckets.entrySet().removeIf(entry -> {
            prune(entry.getValue(), now);
            return entry.getValue().accepted.isEmpty();
        });
    }

    private void prune(Bucket bucket, Instant now) {
        Instant expiry = now.minus(dailyWindow);
        while (!bucket.accepted.isEmpty() && !bucket.accepted.peekFirst().isAfter(expiry)) {
            bucket.accepted.removeFirst();
        }
    }

    private static final class Bucket {
        private final Deque<Instant> accepted = new ArrayDeque<>();
    }

    public record Decision(boolean allowed, long retryAfterSeconds) {
        public static Decision permit() {
            return new Decision(true, 0);
        }

        public static Decision denied(long retryAfterSeconds) {
            return new Decision(false, Math.max(1, retryAfterSeconds));
        }
    }
}
