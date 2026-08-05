package com.resumainer.service;

import com.resumainer.dao.AuthTokenDao;
import com.resumainer.dao.UserDao;
import com.resumainer.model.AuthToken;
import com.resumainer.util.TokenHashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VerificationServiceTest {

    private AuthTokenDao authTokenDao;
    private UserDao userDao;
    private DataSource dataSource;
    private Connection connection;
    private VerificationService verificationService;
    private Clock clock;

    @BeforeEach
    void setUp() throws Exception {
        authTokenDao = mock(AuthTokenDao.class);
        userDao = mock(UserDao.class);
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        // Default: both updates return 1 (success)
        when(userDao.markEmailVerified(any(UUID.class), any(Connection.class))).thenReturn(1);
        when(authTokenDao.markConsumed(anyLong(), any(Connection.class))).thenReturn(1);

        clock = Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC);
        verificationService = new VerificationService(authTokenDao, userDao, dataSource, clock);
    }

    private AuthToken createValidToken(String rawToken) {
        String hash = TokenHashUtil.hashToken(rawToken);
        AuthToken token = new AuthToken();
        token.setId(1L);
        token.setUserId(UUID.randomUUID().toString());
        token.setTokenType("EMAIL_VERIFICATION");
        token.setTokenHash(hash);
        token.setExpiresAt(LocalDateTime.of(2026, 8, 6, 12, 0));
        token.setConsumedAt(null);
        return token;
    }

    @Test
    void verify_validToken_marksVerifiedAndConsumed() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        AuthToken stored = createValidToken(rawToken);

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(stored);

        assertEquals(VerificationService.VerifyResult.SUCCESS, verificationService.verify(rawToken));

        InOrder order = inOrder(authTokenDao, userDao, connection);
        order.verify(authTokenDao).findByHash(tokenHash, "EMAIL_VERIFICATION", connection);
        order.verify(userDao).markEmailVerified(any(UUID.class), eq(connection));
        order.verify(authTokenDao).markConsumed(eq(1L), eq(connection));
        order.verify(connection).commit();
    }

    @Test
    void verify_validToken_bothReturnOne_commitOnce() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        AuthToken stored = createValidToken(rawToken);

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(stored);

        assertEquals(VerificationService.VerifyResult.SUCCESS, verificationService.verify(rawToken));
        verify(connection, times(1)).commit();
    }

    @Test
    void verify_expiredToken_returnsExpired() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        AuthToken expired = createValidToken(rawToken);
        expired.setExpiresAt(LocalDateTime.of(2026, 8, 5, 11, 59));

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(expired);

        assertEquals(VerificationService.VerifyResult.TOKEN_EXPIRED, verificationService.verify(rawToken));
        verify(userDao, never()).markEmailVerified(any(UUID.class), any(Connection.class));
        verify(authTokenDao, never()).markConsumed(anyLong(), any(Connection.class));
    }

    @Test
    void verify_atExactExpiryBoundary_returnsExpired() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        AuthToken token = createValidToken(rawToken);
        token.setExpiresAt(LocalDateTime.of(2026, 8, 5, 12, 0));
        when(authTokenDao.findByHash(TokenHashUtil.hashToken(rawToken), "EMAIL_VERIFICATION", connection))
                .thenReturn(token);

        assertEquals(VerificationService.VerifyResult.TOKEN_EXPIRED, verificationService.verify(rawToken));
        verify(userDao, never()).markEmailVerified(any(UUID.class), any(Connection.class));
    }

    @Test
    void verify_beforeExpiry_acceptsToken() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        AuthToken token = createValidToken(rawToken);
        token.setExpiresAt(LocalDateTime.of(2026, 8, 5, 12, 0, 0, 1));
        when(authTokenDao.findByHash(TokenHashUtil.hashToken(rawToken), "EMAIL_VERIFICATION", connection))
                .thenReturn(token);

        assertEquals(VerificationService.VerifyResult.SUCCESS, verificationService.verify(rawToken));
    }

    @Test
    void verify_nonUtcJvmDefault_doesNotChangeInjectedClockDecision() throws Exception {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Almaty"));
            String rawToken = TokenHashUtil.generateRawToken();
            AuthToken token = createValidToken(rawToken);
            token.setExpiresAt(LocalDateTime.of(2026, 8, 5, 12, 1));
            when(authTokenDao.findByHash(TokenHashUtil.hashToken(rawToken), "EMAIL_VERIFICATION", connection))
                    .thenReturn(token);

            assertEquals(VerificationService.VerifyResult.SUCCESS, verificationService.verify(rawToken));
        } finally {
            TimeZone.setDefault(original);
        }
    }

    @Test
    void verify_consumedToken_returnsInvalid() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        AuthToken consumed = createValidToken(rawToken);
        consumed.setConsumedAt(LocalDateTime.now().minusHours(2));

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(consumed);

        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify(rawToken));
        verify(userDao, never()).markEmailVerified(any(UUID.class), any(Connection.class));
        verify(authTokenDao, never()).markConsumed(anyLong(), any(Connection.class));
    }

    @Test
    void verify_invalidToken_returnsInvalid() throws Exception {
        String rawToken = "unknown-token";
        String tokenHash = TokenHashUtil.hashToken(rawToken);

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(null);

        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify(rawToken));
        verify(userDao, never()).markEmailVerified(any(UUID.class), any(Connection.class));
        verify(authTokenDao, never()).markConsumed(anyLong(), any(Connection.class));
    }

    @Test
    void verify_blankToken_skipsDb() throws Exception {
        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify(""));
        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify("   "));
        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify(null));
        verify(dataSource, never()).getConnection();
    }

    // ============================================================
    // BLOCKER 3 — Zero-row updates must rollback
    // ============================================================

    @Test
    void verify_userUpdateReturnsZero_rollbackAndInvalid() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        AuthToken stored = createValidToken(rawToken);

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(stored);
        when(userDao.markEmailVerified(any(UUID.class), any(Connection.class))).thenReturn(0);

        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify(rawToken));
        verify(connection).rollback();
        verify(authTokenDao, never()).markConsumed(anyLong(), any(Connection.class));
        verify(connection, never()).commit();
    }

    @Test
    void verify_tokenUpdateReturnsZero_rollbackAndInvalid() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        AuthToken stored = createValidToken(rawToken);

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(stored);
        when(userDao.markEmailVerified(any(UUID.class), any(Connection.class))).thenReturn(1);
        when(authTokenDao.markConsumed(anyLong(), any(Connection.class))).thenReturn(0);

        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify(rawToken));
        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    // ============================================================
    // Exception/commit failures
    // ============================================================

    @Test
    void verify_userUpdateThrows_rollbackTokenNotConsumed() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        AuthToken stored = createValidToken(rawToken);

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(stored);
        doThrow(new RuntimeException("DB error")).when(userDao).markEmailVerified(any(UUID.class), any(Connection.class));

        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify(rawToken));
        verify(connection).rollback();
        verify(authTokenDao, never()).markConsumed(anyLong(), any(Connection.class));
    }

    @Test
    void verify_tokenConsumeThrows_rollback() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        AuthToken stored = createValidToken(rawToken);

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(stored);
        doThrow(new RuntimeException("DB error")).when(authTokenDao).markConsumed(anyLong(), any(Connection.class));

        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify(rawToken));
        verify(connection).rollback();
    }

    @Test
    void verify_commitFailure_rollback() throws Exception {
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        AuthToken stored = createValidToken(rawToken);

        when(authTokenDao.findByHash(tokenHash, "EMAIL_VERIFICATION", connection)).thenReturn(stored);
        doThrow(new SQLException("Commit failed")).when(connection).commit();

        assertEquals(VerificationService.VerifyResult.TOKEN_INVALID, verificationService.verify(rawToken));
        verify(connection).rollback();
    }
}
