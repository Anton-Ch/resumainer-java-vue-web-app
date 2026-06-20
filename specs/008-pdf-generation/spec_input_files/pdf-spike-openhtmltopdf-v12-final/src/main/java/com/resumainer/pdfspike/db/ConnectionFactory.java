package com.resumainer.pdfspike.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {
    private static final Logger log = LoggerFactory.getLogger(ConnectionFactory.class);
    private final ConnectionPoolConfig config;

    public ConnectionFactory(ConnectionPoolConfig config) {
        this.config = config;
    }

    Connection createPhysicalConnection() {
        try {
            Connection c = DriverManager.getConnection(config.jdbcUrl());
            c.createStatement().execute("PRAGMA foreign_keys = ON");
            log.debug("Created SQLite physical connection url={}", config.jdbcUrl());
            return c;
        } catch (SQLException e) {
            throw new ConnectionPoolException("Failed to create connection", e);
        }
    }
}
