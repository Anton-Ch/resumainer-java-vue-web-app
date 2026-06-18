package com.resumainer.service.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AiClientException.
 * Covers all 3 constructors, getErrorCode, and message propagation.
 */
class AiClientExceptionTest {

    @Test
    void constructor_withMessage_setsDefaultErrorCode() {
        AiClientException ex = new AiClientException("Something went wrong");

        assertEquals("Something went wrong", ex.getMessage());
        assertEquals("GENERATION_FAILED", ex.getErrorCode());
    }

    @Test
    void constructor_withMessageAndErrorCode_setsCustomCode() {
        AiClientException ex = new AiClientException("Rate limited", "RATE_LIMITED");

        assertEquals("Rate limited", ex.getMessage());
        assertEquals("RATE_LIMITED", ex.getErrorCode());
    }

    @Test
    void constructor_withMessageAndCause() {
        Throwable cause = new RuntimeException("Network error");
        AiClientException ex = new AiClientException("AI call failed", cause);

        assertEquals("AI call failed", ex.getMessage());
        assertEquals("GENERATION_FAILED", ex.getErrorCode());
        assertSame(cause, ex.getCause());
    }
}
