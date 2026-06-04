package com.resumainer.dao;

import com.resumainer.model.UserPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class UserPermissionDao {

    private static final Logger log = LoggerFactory.getLogger(UserPermissionDao.class);
    private static final String SELECT_BY_CODE = "SELECT id, code, name FROM user_permission WHERE code = ?";
    private final DataSource dataSource;

    public UserPermissionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserPermission findByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("UserPermission code must not be null or empty");
        }

        String trimmed = code.trim();
        log.debug("Finding user permission by code: {}", trimmed);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODE)) {
            stmt.setString(1, trimmed);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            log.debug("User permission not found for code: {}", trimmed);
            return null;
        } catch (SQLException e) {
            log.error("Error finding user permission by code: {}", trimmed, e);
            throw new RuntimeException("Database error finding user permission by code", e);
        }
    }

    private UserPermission mapRow(ResultSet rs) throws SQLException {
        UserPermission permission = new UserPermission();
        permission.setId(rs.getLong("id"));
        permission.setCode(rs.getString("code"));
        permission.setName(rs.getString("name"));
        return permission;
    }
}
