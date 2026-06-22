package com.resumainer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal in-memory rate limiter for the public PDF route only.
 * <p>
 * Limits: 10 requests per 60 seconds per IP address.
 * Uses {@link ConcurrentHashMap} for thread safety in MVP single-instance deployment.
 * <p>
 * Stale IP entries are automatically cleaned up when the window expires.
 * This limiter does NOT use Redis, DB tables, external libraries, or CAPTCHA.
 * It does NOT apply to /api/**, authenticated downloads, static files, or frontend routes.
 * <p>
 * Thread-safety note: cleanupStaleEntries uses removeIf which iterates over
 * ConcurrentHashMap's weakly-consistent view — acceptable for rate limiting where
 * occasional stale entries are harmless.
 */
@Service
public class PublicResumeRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(PublicResumeRateLimiter.class);

    static final int MAX_REQUESTS = 10;
    static final long WINDOW_MS = 60_000; // 60 seconds

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    /**
     * Checks whether a request from the given IP is allowed under the rate limit.
     *
     * @param ip the client IP address (from request.getRemoteAddr())
     * @return RateLimitResult with allowed=true if permitted, or denied with retry-after seconds
     */
    public RateLimitResult checkRateLimit(String ip) {
        // Allow null/empty IPs — they'll be rejected by path validation upstream
        if (ip == null || ip.isBlank()) {
            return RateLimitResult.allowed();
        }

        cleanupStaleEntries();

        long now = System.currentTimeMillis();
        WindowCounter counter = counters.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.windowStart >= WINDOW_MS) {
                return new WindowCounter(now, 1);
            }
            existing.count++;
            return existing;
        });

        if (counter.count > MAX_REQUESTS) {
            long remainingMs = counter.windowStart + WINDOW_MS - now;
            long retryAfter = Math.max(1, (remainingMs + 999) / 1000); // ceiling division
            if (retryAfter > 60) retryAfter = 60;
            log.debug("Rate limit exceeded for IP: {} (count={}, retryAfter={}s)", ip, counter.count, retryAfter);
            return RateLimitResult.denied(retryAfter);
        }
        return RateLimitResult.allowed();
    }

    /**
     * Removes IP entries whose window has expired to prevent unbounded memory growth.
     */
    private void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        counters.entrySet().removeIf(entry -> now - entry.getValue().windowStart >= WINDOW_MS);
    }

    // --- Result type ---

    public static class RateLimitResult {
        public final boolean allowed;
        public final long retryAfterSeconds;

        private RateLimitResult(boolean allowed, long retryAfterSeconds) {
            this.allowed = allowed;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public static RateLimitResult allowed() {
            return new RateLimitResult(true, 0);
        }

        public static RateLimitResult denied(long retryAfterSeconds) {
            return new RateLimitResult(false, retryAfterSeconds);
        }
    }

    // --- Internal counter ---

    private static class WindowCounter {
        final long windowStart;
        int count;

        WindowCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}