package com.resumainer.infrastructure.db;

import java.util.Objects;

/**
 * Immutable configuration object for the custom JDBC connection pool.
 * <p>
 * Validates all parameters at construction time (fail-fast, FR-016).
 * Connects to a single PostgreSQL database using a single technical user.
 */
public class ConnectionPoolConfig {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int initialSize;
    private final int maxSize;
    private final long borrowTimeoutMillis;
    private final int validationTimeoutSeconds;

    public ConnectionPoolConfig(
            String jdbcUrl, String username, String password,
            int initialSize, int maxSize,
            long borrowTimeoutMillis, int validationTimeoutSeconds
    ) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("jdbcUrl must not be null or blank");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be null or blank");
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        if (initialSize <= 0 || initialSize > maxSize) {
            throw new IllegalArgumentException(
                    "initialSize (" + initialSize + ") must be >= 1 and <= maxSize (" + maxSize + ")"
            );
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException(
                    "maxSize (" + maxSize + ") must be >= 1"
            );
        }
        if (borrowTimeoutMillis <= 0) {
            throw new IllegalArgumentException(
                    "borrowTimeoutMillis (" + borrowTimeoutMillis + ") must be > 0"
            );
        }
        if (validationTimeoutSeconds <= 0) {
            throw new IllegalArgumentException(
                    "validationTimeoutSeconds (" + validationTimeoutSeconds + ") must be > 0"
            );
        }
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.initialSize = initialSize;
        this.maxSize = maxSize;
        this.borrowTimeoutMillis = borrowTimeoutMillis;
        this.validationTimeoutSeconds = validationTimeoutSeconds;
    }

    public String getJdbcUrl() { return jdbcUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getInitialSize() { return initialSize; }
    public int getMaxSize() { return maxSize; }
    public long getBorrowTimeoutMillis() { return borrowTimeoutMillis; }
    public int getValidationTimeoutSeconds() { return validationTimeoutSeconds; }
}
