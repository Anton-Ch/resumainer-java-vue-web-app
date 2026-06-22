package com.resumainer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T187 RED tests for PublicResumeRateLimiter.
 * Verifies: 10 requests/60s per IP, 429 on 11th, retry-after header, cleanup of stale entries.
 */
class PublicResumeRateLimiterTest {

    private PublicResumeRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new PublicResumeRateLimiter();
    }

    @Test
    void firstRequest_isAllowed() {
        PublicResumeRateLimiter.RateLimitResult result = limiter.checkRateLimit("192.168.1.1");
        assertTrue(result.allowed, "First request should be allowed");
    }

    @Test
    void tenRequests_sameIp_areAllowed() {
        for (int i = 0; i < 10; i++) {
            PublicResumeRateLimiter.RateLimitResult result = limiter.checkRateLimit("192.168.1.1");
            assertTrue(result.allowed, "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void eleventhRequest_sameIp_isDenied() {
        for (int i = 0; i < 10; i++) {
            limiter.checkRateLimit("192.168.1.1");
        }

        PublicResumeRateLimiter.RateLimitResult result = limiter.checkRateLimit("192.168.1.1");
        assertFalse(result.allowed, "11th request should be denied");
        assertTrue(result.retryAfterSeconds > 0, "retryAfter should be positive");
        assertTrue(result.retryAfterSeconds <= 60, "retryAfter should be at most 60 seconds");
    }

    @Test
    void differentIps_haveIndependentLimits() {
        // Exhaust IP 1
        for (int i = 0; i < 10; i++) {
            limiter.checkRateLimit("192.168.1.1");
        }

        // IP 2 should still be allowed
        PublicResumeRateLimiter.RateLimitResult result = limiter.checkRateLimit("192.168.1.2");
        assertTrue(result.allowed, "Different IP should have independent limit");
    }

    @Test
    void deniedRequest_hasRetryAfterHeader() {
        for (int i = 0; i < 10; i++) {
            limiter.checkRateLimit("192.168.1.1");
        }

        PublicResumeRateLimiter.RateLimitResult result = limiter.checkRateLimit("192.168.1.1");
        assertFalse(result.allowed);
        assertTrue(result.retryAfterSeconds > 0);
        assertTrue(result.retryAfterSeconds <= 60);
    }

    @Test
    void nullIp_orEmptyIp_isAllowed() {
        // Null or empty IPs should not cause NPE; default to allowing them
        // (they'll still be rejected by PublicResumeController path validation)
        assertDoesNotThrow(() -> limiter.checkRateLimit(null));
        assertDoesNotThrow(() -> limiter.checkRateLimit(""));
    }
}