package com.resumainer.service.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class ResendVerificationRateLimiterTest {

    @Test
    void reserve_secondRequestInsideCooldown_isDeniedWithoutPartialReservation() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 100);

        assertTrue(limiter.reserve("first@example.com", "198.51.100.10").allowed());

        ResendVerificationRateLimiter.Decision blocked =
                limiter.reserve("second@example.com", "198.51.100.10");
        assertFalse(blocked.allowed());
        assertEquals(60, blocked.retryAfterSeconds());

        // The rejected request must not consume the second email key.
        assertTrue(limiter.reserve("second@example.com", "198.51.100.11").allowed());
    }

    @Test
    void reserve_atCooldownBoundary_isAllowed() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 100);
        assertTrue(limiter.reserve("user@example.com", "198.51.100.1").allowed());
        clock.advance(Duration.ofSeconds(60));
        assertTrue(limiter.reserve("user@example.com", "198.51.100.1").allowed());
    }

    @Test
    void reserve_multipleViolatedLimits_returnsMaximumWholeSecondWait() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        ResendVerificationRateLimiter limiter = new ResendVerificationRateLimiter(
                clock, Duration.ofSeconds(60), Duration.ofHours(1), 5,
                Duration.ofDays(1), 20, 100);

        assertTrue(limiter.reserve("user@example.com", "198.51.100.20").allowed());
        clock.advance(Duration.ofSeconds(1));

        ResendVerificationRateLimiter.Decision decision =
                limiter.reserve("user@example.com", "198.51.100.20");

        assertFalse(decision.allowed());
        assertEquals(59, decision.retryAfterSeconds());
    }

    @Test
    void reserve_simultaneousRequests_onlyOnePassesCooldown() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 100);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<ResendVerificationRateLimiter.Decision>> futures = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return limiter.reserve("same@example.com", "198.51.100.30");
                }));
            }
            ready.await();
            start.countDown();

            long allowed = 0;
            for (Future<ResendVerificationRateLimiter.Decision> future : futures) {
                if (future.get().allowed()) {
                    allowed++;
                }
            }
            assertEquals(1, allowed);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void reserve_atCapacity_doesNotEvictActiveKeysAndPermitBypass() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 2);

        assertTrue(limiter.reserve("one@example.com", "198.51.100.40").allowed());
        ResendVerificationRateLimiter.Decision capacityBlocked =
                limiter.reserve("two@example.com", "198.51.100.41");

        assertFalse(capacityBlocked.allowed());
        assertEquals(86400, capacityBlocked.retryAfterSeconds(),
                "capacity retry must wait until the earliest active key expires");
        assertFalse(limiter.reserve("one@example.com", "198.51.100.40").allowed());
        assertTrue(limiter.retainedKeyCount() <= 2);
    }

    @Test
    void reserve_afterDailyExpiry_lazilyRemovesObsoleteKeys() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 2);
        assertTrue(limiter.reserve("old@example.com", "198.51.100.50").allowed());

        clock.advance(Duration.ofDays(1).plusSeconds(1));

        assertTrue(limiter.reserve("new@example.com", "198.51.100.51").allowed());
        assertTrue(limiter.retainedKeyCount() <= 2);
    }

    @Test
    void reserve_sixthRequestInsideHour_isDeniedUntilOldestHourlyEntryExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 100);
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.reserve("hour@example.com", "198.51.100." + i).allowed());
            clock.advance(Duration.ofSeconds(60));
        }

        ResendVerificationRateLimiter.Decision decision =
                limiter.reserve("hour@example.com", "198.51.100.99");

        assertFalse(decision.allowed());
        assertEquals(3300, decision.retryAfterSeconds());
    }

    @Test
    void reserve_atHourlyBoundary_isAllowed() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 100);
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.reserve("hour@example.com", "198.51.100." + i).allowed());
            if (i < 4) clock.advance(Duration.ofSeconds(60));
        }
        clock.advance(Duration.ofMinutes(56));
        assertTrue(limiter.reserve("hour@example.com", "198.51.100.99").allowed());
    }

    @Test
    void reserve_sixthRequestFromSameIpInsideHour_isDeniedAndBoundaryAllowed() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T12:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 100);
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.reserve("user" + i + "@example.com", "198.51.100.80").allowed());
            if (i < 4) clock.advance(Duration.ofSeconds(60));
        }
        assertFalse(limiter.reserve("blocked@example.com", "198.51.100.80").allowed());
        clock.advance(Duration.ofMinutes(56));
        assertTrue(limiter.reserve("allowed@example.com", "198.51.100.80").allowed());
    }

    @Test
    void reserve_twentyFirstRequestInsideDay_isDeniedUntilOldestDailyEntryExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 100);
        for (int i = 0; i < 20; i++) {
            assertTrue(limiter.reserve("day@example.com", "203.0.113." + i).allowed());
            clock.advance(Duration.ofMinutes(61));
        }

        ResendVerificationRateLimiter.Decision decision =
                limiter.reserve("day@example.com", "203.0.113.99");

        assertFalse(decision.allowed());
        assertEquals(13200, decision.retryAfterSeconds());
    }

    @Test
    void reserve_atDailyBoundary_isAllowed() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 100);
        for (int i = 0; i < 20; i++) {
            assertTrue(limiter.reserve("day@example.com", "203.0.113." + i).allowed());
            if (i < 19) clock.advance(Duration.ofMinutes(61));
        }
        clock.advance(Duration.ofMinutes(24 * 60 - 19 * 61));
        assertTrue(limiter.reserve("day@example.com", "203.0.113.99").allowed());
    }

    @Test
    void reserve_twentyFirstRequestFromSameIpInsideDay_isDeniedAndBoundaryAllowed() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        ResendVerificationRateLimiter limiter = limiter(clock, 100);
        for (int i = 0; i < 20; i++) {
            assertTrue(limiter.reserve("user" + i + "@example.com", "203.0.113.80").allowed());
            if (i < 19) clock.advance(Duration.ofMinutes(61));
        }
        assertFalse(limiter.reserve("blocked@example.com", "203.0.113.80").allowed());
        clock.advance(Duration.ofMinutes(24 * 60 - 19 * 61));
        assertTrue(limiter.reserve("allowed@example.com", "203.0.113.80").allowed());
    }

    private static ResendVerificationRateLimiter limiter(Clock clock, int maxKeys) {
        return new ResendVerificationRateLimiter(
                clock, Duration.ofSeconds(60), Duration.ofHours(1), 5,
                Duration.ofDays(1), 20, maxKeys);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
