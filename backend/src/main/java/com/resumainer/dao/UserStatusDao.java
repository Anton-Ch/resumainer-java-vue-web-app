package com.resumainer.dao;

import com.resumainer.model.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class UserStatusDao {

    private static final Logger log = LoggerFactory.getLogger(UserStatusDao.class);
    private static final String SELECT_BY_CODE = "SELECT id, code, name FROM user_status WHERE code = ?";
    private final DataSource dataSource;

    public UserStatusDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserStatus findByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("UserStatus code must not be null or empty");
        }

        String trimmed = code.trim();
        log.debug("Finding user status by code: {}", trimmed);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODE)) {
            stmt.setString(1, trimmed);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            log.debug("User status not found for code: {}", trimmed);
            return null;
        } catch (SQLException e) {
            log.error("Error finding user status by code: {}", trimmed, e);
            throw new RuntimeException("Database error finding user status by code", e);
        }
    }

    private UserStatus mapRow(ResultSet rs) throws SQLException {
        UserStatus status = new UserStatus();
        status.setId(rs.getLong("id"));
        status.setCode(rs.getString("code"));
        status.setName(rs.getString("name"));
        return status;
    }
}
