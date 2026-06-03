package com.resumainer.dao;

import com.resumainer.model.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for the 'language' lookup table.
 */
public class LanguageDao {

    private static final Logger log = LoggerFactory.getLogger(LanguageDao.class);
    private static final String SELECT_BY_CODE = "SELECT id, code, name FROM language WHERE code = ?";
    private final DataSource dataSource;

    public LanguageDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Language findByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Language code must not be null or empty");
        }

        String trimmed = code.trim();
        log.debug("Finding language by code: {}", trimmed);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODE)) {
            stmt.setString(1, trimmed);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            log.debug("Language not found for code: {}", trimmed);
            return null;
        } catch (SQLException e) {
            log.error("Error finding language by code: {}", trimmed, e);
            throw new RuntimeException("Database error finding language by code", e);
        }
    }

    private Language mapRow(ResultSet rs) throws SQLException {
        Language language = new Language();
        language.setId(rs.getLong("id"));
        language.setCode(rs.getString("code"));
        language.setName(rs.getString("name"));
        return language;
    }
}
