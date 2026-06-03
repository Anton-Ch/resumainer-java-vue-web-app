package com.resumainer.dao;

import com.resumainer.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO for the 'users' table (UUID PK).
 * All queries use PreparedStatement (Constitution IV).
 */
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final String INSERT =
            "INSERT INTO users (email, password_hash, role_id, status_id, permission_id, " +
            "default_language_id, secondary_language_id, is_privileged) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
            "RETURNING id";

    private static final String SELECT_BY_EMAIL =
            "SELECT id, email, password_hash, username, role_id, status_id, permission_id, " +
            "default_language_id, secondary_language_id, is_privileged, failed_login_attempts, " +
            "locked_until, created_at, updated_at, deleted_at, is_deleted " +
            "FROM users WHERE LOWER(email) = LOWER(?) AND is_deleted = FALSE";

    private static final String SELECT_BY_ID =
            "SELECT id, email, password_hash, username, role_id, status_id, permission_id, " +
            "default_language_id, secondary_language_id, is_privileged, failed_login_attempts, " +
            "locked_until, created_at, updated_at, deleted_at, is_deleted " +
            "FROM users WHERE id = ?";

    private static final String UPDATE_LOGIN_ATTEMPTS =
            "UPDATE users SET failed_login_attempts = ?, locked_until = ? WHERE id = ?";

    private static final String RESET_LOGIN_ATTEMPTS =
            "UPDATE users SET failed_login_attempts = ?, locked_until = ? WHERE id = ?";

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Create a new user (auto-managed connection).
     *
     * @param user the user to create
     */
    public void create(User user) {
        try (Connection conn = dataSource.getConnection()) {
            create(user, conn);
        } catch (SQLException e) {
            log.error("Error creating user: {}", user.getEmail(), e);
            throw new RuntimeException("Database error creating user", e);
        }
    }

    /**
     * Create a new user within an existing connection (for transaction support).
     *
     * @param user the user to create
     * @param conn the existing database connection (transaction-managed)
     */
    public void create(User user, Connection conn) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        log.debug("Creating user: {}", user.getEmail());

        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {

            stmt.setString(1, user.getEmail().toLowerCase().trim());
            stmt.setString(2, user.getPasswordHash());
            setLongOrNull(stmt, 3, user.getRoleId());
            setLongOrNull(stmt, 4, user.getStatusId());
            setLongOrNull(stmt, 5, user.getPermissionId());
            setLongOrNull(stmt, 6, user.getDefaultLanguageId());
            setLongOrNull(stmt, 7, user.getSecondaryLanguageId());
            stmt.setBoolean(8, user.isPrivileged());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Insert did not return a generated key");
                }
                user.setId(rs.getObject("id", UUID.class));
            }
            log.debug("User created: {}, id={}", user.getEmail(), user.getId());

        } catch (SQLException e) {
            log.error("Error creating user: {}", user.getEmail(), e);
            throw new RuntimeException("Database error creating user", e);
        }
    }

    /**
     * Find a user by email (case-insensitive).
     *
     * @param email the email to search for
     * @return the User if found, null otherwise
     */
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }

        String trimmed = email.trim().toLowerCase();
        log.debug("Finding user by email: {}", trimmed);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMAIL)) {

            stmt.setString(1, trimmed);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            log.debug("User not found for email: {}", trimmed);
            return null;

        } catch (SQLException e) {
            log.error("Error finding user by email: {}", trimmed, e);
            throw new RuntimeException("Database error finding user by email", e);
        }
    }

    /**
     * Find a user by their UUID.
     *
     * @param id the user UUID
     * @return the User if found, null otherwise
     */
    public User findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        log.debug("Finding user by id: {}", id);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            log.debug("User not found for id: {}", id);
            return null;

        } catch (SQLException e) {
            log.error("Error finding user by id: {}", id, e);
            throw new RuntimeException("Database error finding user by id", e);
        }
    }

    /**
     * Update failed login attempts and optional lock time.
     */
    public void updateLoginAttempts(UUID id, int attempts, LocalDateTime lockedUntil) {
        log.debug("Updating login attempts for user {}: attempts={}, lockedUntil={}", id, attempts, lockedUntil);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LOGIN_ATTEMPTS)) {

            stmt.setInt(1, attempts);
            stmt.setObject(2, lockedUntil != null ? Timestamp.valueOf(lockedUntil) : null);
            stmt.setObject(3, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            log.error("Error updating login attempts for user: {}", id, e);
            throw new RuntimeException("Database error updating login attempts", e);
        }
    }

    /**
     * Reset failed login attempts counter and clear lock.
     */
    public void resetLoginAttempts(UUID id) {
        log.debug("Resetting login attempts for user: {}", id);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(RESET_LOGIN_ATTEMPTS)) {

            stmt.setInt(1, 0);
            stmt.setObject(2, null);
            stmt.setObject(3, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            log.error("Error resetting login attempts for user: {}", id, e);
            throw new RuntimeException("Database error resetting login attempts", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getObject("id", UUID.class));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setUsername(rs.getString("username"));
        user.setRoleId(rs.getLong("role_id"));
        user.setStatusId(rs.getLong("status_id"));
        user.setPermissionId(rs.getLong("permission_id"));
        user.setDefaultLanguageId(rs.getObject("default_language_id", Long.class));
        user.setSecondaryLanguageId(rs.getObject("secondary_language_id", Long.class));
        user.setPrivileged(rs.getBoolean("is_privileged"));
        user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        user.setLockedUntil(rs.getObject("locked_until", LocalDateTime.class));
        user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        user.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        user.setDeletedAt(rs.getObject("deleted_at", LocalDateTime.class));
        user.setDeleted(rs.getBoolean("is_deleted"));
        return user;
    }

    private void setLongOrNull(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) {
            stmt.setLong(index, value);
        } else {
            stmt.setNull(index, Types.BIGINT);
        }
    }
}
