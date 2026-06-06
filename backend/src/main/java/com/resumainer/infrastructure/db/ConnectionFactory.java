package com.resumainer.infrastructure.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final Logger log = LoggerFactory.getLogger(ConnectionFactory.class);

    static {
        // Force-load PostgreSQL driver before DriverManager can find it.
        // In Tomcat with Java 9+, DriverManager's ServiceLoader-based driver discovery
        // may fail because the driver jar is in WEB-INF/lib, not the system classpath.
        // This is the standard approach — Spring's DriverManagerDataSource does the same
        // via setDriverClassName() which internally calls Class.forName().
        try {
            Class.forName("org.postgresql.Driver");
            log.debug("PostgreSQL JDBC driver registered successfully");
        } catch (ClassNotFoundException e) {
            log.error("PostgreSQL JDBC driver not found on classpath", e);
        }
    }

    private final ConnectionPoolConfig config;

    public ConnectionFactory(ConnectionPoolConfig config) {
        this.config = config;
    }

    public Connection createConnection() {
        try {
            log.debug("Creating new database connection to {}", maskUrl(config.getJdbcUrl()));
            return DriverManager.getConnection(
                    config.getJdbcUrl(),
                    config.getUsername(),
                    config.getPassword()
            );
        } catch (SQLException e) {
            throw new ConnectionPoolException("Could not create physical database connection", e);
        }
    }

    public boolean isValid(Connection connection) {
        if (connection == null) {
            return false;
        }
        try {
            return connection.isValid(config.getValidationTimeoutSeconds());
        } catch (SQLException e) {
            log.warn("Connection validation failed with exception", e);
            return false;
        }
    }

    public void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            log.warn("Failed to close database connection", e);
        }
    }

    private static String maskUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("://[^:]+:[^@]+@", "://***:***@");
    }
}
