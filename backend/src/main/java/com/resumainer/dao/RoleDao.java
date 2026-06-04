package com.resumainer.dao;

import com.resumainer.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class RoleDao {

    private static final Logger log = LoggerFactory.getLogger(RoleDao.class);

    private static final String SELECT_BY_CODE = "SELECT id, code, name FROM role WHERE code = ?";

    private final DataSource dataSource;

    public RoleDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Find a role by its unique code (e.g. "USER", "ADMIN").
     *
     * @param code the role code (not null, not empty)
     * @return the Role if found, null otherwise
     * @throws IllegalArgumentException if code is null or empty
     */
    public Role findByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Role code must not be null or empty");
        }

        String trimmedCode = code.trim();
        log.debug("Finding role by code: {}", trimmedCode);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODE)) {

            stmt.setString(1, trimmedCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

            log.debug("Role not found for code: {}", trimmedCode);
            return null;

        } catch (SQLException e) {
            log.error("Error finding role by code: {}", trimmedCode, e);
            throw new RuntimeException("Database error finding role by code", e);
        }
    }

    private Role mapRow(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getLong("id"));
        role.setCode(rs.getString("code"));
        role.setName(rs.getString("name"));
        return role;
    }
}
