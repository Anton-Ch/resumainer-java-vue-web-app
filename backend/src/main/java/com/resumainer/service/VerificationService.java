package com.resumainer.service;

import com.resumainer.dao.AuthTokenDao;
import com.resumainer.dao.UserDao;
import com.resumainer.model.AuthToken;
import com.resumainer.util.TokenHashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Service for email verification with atomic transaction guarantees.
 *
 * <p>Verifies a raw token, marks the user as email verified, and marks
 * the token as consumed — all within a single JDBC transaction with
 * row-level locking ({@code SELECT ... FOR UPDATE}).
 *
 * <p>On any failure: rollback, token remains reusable, user remains unverified.
 */
@Service
public class VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationService.class);

    private final AuthTokenDao authTokenDao;
    private final UserDao userDao;
    private final DataSource dataSource;

    public VerificationService(AuthTokenDao authTokenDao, UserDao userDao, DataSource dataSource) {
        this.authTokenDao = authTokenDao;
        this.userDao = userDao;
        this.dataSource = dataSource;
    }

    /**
     * Result of a verification attempt.
     */
    public enum VerifyResult {
        SUCCESS,
        TOKEN_EXPIRED,
        TOKEN_INVALID
    }

    /**
     * Verify an email verification token atomically.
     *
     * <p>Within one JDBC transaction:
     * <ol>
     *   <li>Look up the token hash with {@code FOR UPDATE} row lock</li>
     *   <li>Validate consumed/expired state</li>
     *   <li>Mark user as email verified</li>
     *   <li>Mark token as consumed</li>
     *   <li>Commit</li>
     * </ol>
     *
     * @param rawToken the raw verification token from the email link
     * @return the verification result status
     */
    public VerifyResult verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return VerifyResult.TOKEN_INVALID;
        }

        String tokenHash = TokenHashUtil.hashToken(rawToken);

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // Look up token with row lock (FOR UPDATE)
            AuthToken stored = authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", conn);

            if (stored == null) {
                conn.rollback();
                return VerifyResult.TOKEN_INVALID;
            }

            if (stored.isConsumed()) {
                conn.rollback();
                return VerifyResult.TOKEN_INVALID;
            }

            if (stored.isExpired()) {
                conn.rollback();
                return VerifyResult.TOKEN_EXPIRED;
            }

            // Mark user verified — must affect exactly 1 row
            int userUpdated = userDao.markEmailVerified(UUID.fromString(stored.getUserId()), conn);
            if (userUpdated != 1) {
                log.warn("Email verification failed: markEmailVerified affected {} rows", userUpdated);
                conn.rollback();
                return VerifyResult.TOKEN_INVALID;
            }

            // Mark token consumed — must affect exactly 1 row
            int tokenUpdated = authTokenDao.markConsumed(stored.getId(), conn);
            if (tokenUpdated != 1) {
                log.warn("Email verification failed: markConsumed affected {} rows", tokenUpdated);
                conn.rollback();
                return VerifyResult.TOKEN_INVALID;
            }

            conn.commit();
            log.info("Email verified successfully for user: {}", stored.getUserId());
            return VerifyResult.SUCCESS;

        } catch (Exception e) {
            rollbackQuietly(conn);
            log.warn("Email verification failed (rollback): {}", e.getMessage());
            return VerifyResult.TOKEN_INVALID;

        } finally {
            closeQuietly(conn);
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException e) { log.warn("Rollback failed", e); }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { log.warn("Close failed", e); }
        }
    }
}
