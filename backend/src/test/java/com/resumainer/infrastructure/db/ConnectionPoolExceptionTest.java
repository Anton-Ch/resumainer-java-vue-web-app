package com.resumainer.infrastructure.db;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionPoolExceptionTest {

    @Test
    void exception_withMessage_containsMessage() {
        ConnectionPoolException ex = new ConnectionPoolException("Pool is closed");
        assertEquals("Pool is closed", ex.getMessage());
    }

    @Test
    void exception_withMessageAndCause_containsBoth() {
        Throwable cause = new RuntimeException("DB down");
        ConnectionPoolException ex = new ConnectionPoolException("Failed to connect", cause);
        assertEquals("Failed to connect", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
