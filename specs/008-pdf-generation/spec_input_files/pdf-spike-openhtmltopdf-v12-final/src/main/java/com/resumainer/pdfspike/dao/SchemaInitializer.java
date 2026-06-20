package com.resumainer.pdfspike.dao;

import com.resumainer.pdfspike.db.SimpleConnectionPool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class SchemaInitializer {
    private final SimpleConnectionPool pool;

    public SchemaInitializer(SimpleConnectionPool pool) { this.pool = pool; }

    public void initialize() {
        executeResource("/db/schema.sql");
        executeResource("/db/seed.sql");
    }

    void executeResource(String resource) {
        try (var in = SchemaInitializer.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + resource);
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection c = pool.getConnection(); var st = c.createStatement()) {
                for (String statement : splitSql(sql)) {
                    if (!statement.isBlank()) st.execute(statement);
                }
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to execute " + resource, e);
        }
    }

    static List<String> splitSql(String sql) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inLineComment = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            char next = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';
            if (!inLineComment && ch == '-' && next == '-') {
                inLineComment = true;
                i++;
                continue;
            }
            if (inLineComment && (ch == '\n' || ch == '\r')) {
                inLineComment = false;
                current.append('\n');
                continue;
            }
            if (inLineComment) continue;
            if (ch == ';') {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (!current.toString().isBlank()) result.add(current.toString().trim());
        return result;
    }
}
