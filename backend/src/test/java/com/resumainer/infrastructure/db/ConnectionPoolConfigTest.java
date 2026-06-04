package com.resumainer.infrastructure.db;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionPoolConfigTest {

    @Test
    void constructor_withValidParams_createsConfig() {
        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://localhost:5432/test",
                "user", "pass",
                2, 10, 5000, 2
        );
        assertEquals("jdbc:postgresql://localhost:5432/test", config.getJdbcUrl());
        assertEquals("user", config.getUsername());
        assertEquals("pass", config.getPassword());
        assertEquals(2, config.getInitialSize());
        assertEquals(10, config.getMaxSize());
        assertEquals(5000, config.getBorrowTimeoutMillis());
        assertEquals(2, config.getValidationTimeoutSeconds());
    }

    @Test
    void constructor_withInitialSizeZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ConnectionPoolConfig("jdbc:pg://localhost/db", "u", "p", 0, 10, 5000, 2)
        );
    }

    @Test
    void constructor_withInitialSizeGreaterThanMaxSize_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ConnectionPoolConfig("jdbc:pg://localhost/db", "u", "p", 15, 10, 5000, 2)
        );
    }

    @Test
    void constructor_withNullJdbcUrl_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ConnectionPoolConfig(null, "u", "p", 2, 10, 5000, 2)
        );
    }

    @Test
    void constructor_withMaxSizeZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ConnectionPoolConfig("jdbc:pg://localhost/db", "u", "p", 2, 0, 5000, 2)
        );
    }

    @Test
    void constructor_withNegativeTimeout_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ConnectionPoolConfig("jdbc:pg://localhost/db", "u", "p", 2, 10, -1, 2)
        );
    }
}
