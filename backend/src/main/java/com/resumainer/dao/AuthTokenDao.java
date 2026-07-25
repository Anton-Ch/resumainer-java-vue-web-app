package com.resumainer.dao;

import com.resumainer.model.AuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * DAO for the 'auth_tokens' table (BIGSERIAL PK).
 *
 * <p>All queries use PreparedStatement. Raw tokens are never stored —
 * only cryptographic hashes are persisted.
 *
 * <p>Provides {@code Connection}-accepting overloads for transaction support.
 */
@Repository
public class AuthTokenDao {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenDao.class);

    private static final String INSERT =
            "INSERT INTO auth_tokens (user_id, token_type, token_hash, expires_at, consumed_at) " +
            "VALUES (?::uuid, ?, ?, ?, ?) RETURNING id, created_at";

    private static final String SELECT_BY_HASH =
            "SELECT id, user_id, token_type, token_hash, expires_at, consumed_at, created_at " +
            "FROM auth_tokens WHERE token_hash = ? AND token_type = ?";

    private static final String MARK_CONSUMED =
            "UPDATE auth_tokens SET consumed_at = ? WHERE id = ? AND consumed_at IS NULL";

    private static final String SELECT_BY_HASH_FOR_UPDATE =
            "SELECT id, user_id, token_type, token_hash, expires_at, consumed_at, created_at " +
            "FROM auth_tokens WHERE token_hash = ? AND token_type = ? FOR UPDATE";

    private static final String INVALIDATE_OLD =
            "UPDATE auth_tokens SET consumed_at = ? " +
            "WHERE user_id = ?::uuid AND token_type = ? AND consumed_at IS NULL";

    private final DataSource dataSource;

    public AuthTokenDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ============================================================
    // Auto-managed connection methods
    // ============================================================

    /**
     * Insert a new auth token (auto-managed connection).
     *
     * @param token the auth token to insert (must have user_id, token_type, token_hash, expires_at)
     * @return the inserted token with populated id and created_at
     */
    public AuthToken insert(AuthToken token) {
        try (Connection conn = dataSource.getConnection()) {
            return insert(token, conn);
        } catch (SQLException e) {
            log.error("Error inserting auth token for user: {}", token.getUserId(), e);
            throw new RuntimeException("Database error inserting auth token", e);
        }
    }

    /**
     * Insert a new auth token within an existing connection (for transaction support).
     *
     * @param token the auth token to insert
     * @param conn  the existing database connection (transaction-managed)
     * @return the inserted token with populated id and created_at
     */
    public AuthToken insert(AuthToken token, Connection conn) {
        log.debug("Inserting auth token: type={}, userId={}", token.getTokenType(), token.getUserId());

        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setObject(1, token.getUserId());
            stmt.setString(2, token.getTokenType());
            stmt.setString(3, token.getTokenHash());
            stmt.setObject(4, token.getExpiresAt());
            stmt.setObject(5, null); // consumed_at is initially NULL

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    token.setId(rs.getLong("id"));
                    token.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                }
            }
            return token;

        } catch (SQLException e) {
            log.error("Error inserting auth token for user: {}", token.getUserId(), e);
            throw new RuntimeException("Database error inserting auth token", e);
        }
    }

    // ============================================================
    // Query methods
    // ============================================================

    /**
     * Find an auth token by its hash and type.
     *
     * @param tokenHash the hashed token value
     * @param tokenType the token type (EMAIL_VERIFICATION, PASSWORD_RESET)
     * @return the auth token if found, or null
     */
    public AuthToken findByHash(String tokenHash, String tokenType) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_HASH)) {

            stmt.setString(1, tokenHash);
            stmt.setString(2, tokenType);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            log.error("Error finding auth token by hash", e);
            throw new RuntimeException("Database error finding auth token", e);
        }
    }

    /**
     * Find an auth token by hash within an existing connection (for transaction support).
     * Uses SELECT ... FOR UPDATE to lock the row for concurrency safety.
     *
     * @param tokenHash the hashed token value
     * @param tokenType the token type
     * @param conn      existing database connection (transaction-managed)
     * @return the auth token if found, or null
     */
    public AuthToken findByHash(String tokenHash, String tokenType, Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_HASH_FOR_UPDATE)) {

            stmt.setString(1, tokenHash);
            stmt.setString(2, tokenType);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            log.error("Error finding auth token by hash (tx)", e);
            throw new RuntimeException("Database error finding auth token", e);
        }
    }

    // ============================================================
    // Update methods
    // ============================================================

    /**
     * Mark a token as consumed (one-time use).
     *
     * @param tokenId the token ID to mark as consumed
     */
    public void markConsumed(Long tokenId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(MARK_CONSUMED)) {

            stmt.setObject(1, LocalDateTime.now());
            stmt.setLong(2, tokenId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            log.error("Error marking auth token consumed: id={}", tokenId, e);
            throw new RuntimeException("Database error updating auth token", e);
        }
    }

    /**
     * Mark a token as consumed within an existing connection (for transaction support).
     *
     * @param tokenId the token ID to mark as consumed
     * @param conn    existing database connection (transaction-managed)
     * @return the number of rows affected
     * @throws RuntimeException if the token is not found (affected == 0)
     */
    public int markConsumed(Long tokenId, Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement(MARK_CONSUMED)) {
            stmt.setObject(1, LocalDateTime.now());
            stmt.setLong(2, tokenId);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                log.warn("markConsumed: token not found: id={}", tokenId);
                throw new RuntimeException("Token not found for consumption: " + tokenId);
            }
            return affected;
        } catch (SQLException e) {
            log.error("Error marking auth token consumed (tx): id={}", tokenId, e);
            throw new RuntimeException("Database error updating auth token", e);
        }
    }

    /**
     * Invalidate all old active (non-consumed) tokens of the given type for a user.
     * Sets consumed_at to NOW() for matching tokens.
     *
     * @param userId    the user's UUID as string
     * @param tokenType the token type to invalidate
     */
    public void invalidateOldTokens(String userId, String tokenType) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INVALIDATE_OLD)) {

            stmt.setObject(1, LocalDateTime.now());
            stmt.setObject(2, userId);
            stmt.setString(3, tokenType);
            stmt.executeUpdate();

        } catch (SQLException e) {
            log.error("Error invalidating old tokens for user: {}", userId, e);
            throw new RuntimeException("Database error invalidating auth tokens", e);
        }
    }

    // ============================================================
    // Row mapper
    // ============================================================

    private AuthToken mapRow(ResultSet rs) throws SQLException {
        AuthToken token = new AuthToken();
        token.setId(rs.getLong("id"));
        token.setUserId(rs.getString("user_id"));
        token.setTokenType(rs.getString("token_type"));
        token.setTokenHash(rs.getString("token_hash"));
        token.setExpiresAt(rs.getObject("expires_at", LocalDateTime.class));
        token.setConsumedAt(rs.getObject("consumed_at", LocalDateTime.class));
        token.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return token;
    }
}
