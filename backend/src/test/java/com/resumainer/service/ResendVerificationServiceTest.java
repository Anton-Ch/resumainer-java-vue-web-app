package com.resumainer.service;

import com.resumainer.dao.AuthTokenDao;
import com.resumainer.dao.UserDao;
import com.resumainer.model.AuthToken;
import com.resumainer.service.email.EmailException;
import com.resumainer.service.email.EmailService;
import com.resumainer.service.email.EmailTemplateService;
import com.resumainer.service.security.CaptchaResult;
import com.resumainer.service.security.CaptchaService;
import com.resumainer.service.security.ResendVerificationRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ResendVerificationServiceTest {

    private CaptchaService captchaService;
    private ResendVerificationRateLimiter rateLimiter;
    private UserDao userDao;
    private AuthTokenDao authTokenDao;
    private EmailTemplateService templateService;
    private EmailService emailService;
    private DataSource dataSource;
    private Connection connection;
    private ResendVerificationService service;

    @BeforeEach
    void setUp() throws Exception {
        captchaService = mock(CaptchaService.class);
        rateLimiter = mock(ResendVerificationRateLimiter.class);
        userDao = mock(UserDao.class);
        authTokenDao = mock(AuthTokenDao.class);
        templateService = mock(EmailTemplateService.class);
        emailService = mock(EmailService.class);
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(captchaService.verify(anyString())).thenReturn(CaptchaResult.success());
        when(rateLimiter.reserve(anyString(), anyString()))
                .thenReturn(ResendVerificationRateLimiter.Decision.permit());
        service = new ResendVerificationService(captchaService, rateLimiter, userDao, authTokenDao,
                templateService, emailService, dataSource,
                Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC), 1440);
    }

    @Test
    void resend_invalidCaptcha_doesNotReserveQuotaOrOpenConnection() {
        when(captchaService.verify("bad"))
                .thenReturn(CaptchaResult.failure("CAPTCHA_INVALID"));

        ResendVerificationService.Result result =
                service.resend(" User@Example.com ", "bad", "198.51.100.1");

        assertEquals(ResendVerificationService.Status.CAPTCHA_INVALID, result.status());
        verifyNoInteractions(rateLimiter, dataSource, emailService);
    }

    @Test
    void resend_eligibleUser_invalidatesAndInsertsOnSameConnection_thenCommitsBeforeEmail() throws Exception {
        UserDao.VerificationCandidate user = unverifiedUser("user@example.com");
        when(userDao.findVerificationCandidateForUpdate("user@example.com", connection)).thenReturn(user);
        when(templateService.createVerificationEmail(eq("user@example.com"), anyString()))
                .thenReturn(mock(com.resumainer.service.email.EmailMessage.class));

        ResendVerificationService.Result result =
                service.resend(" User@Example.com ", "captcha", "198.51.100.2");

        assertEquals(ResendVerificationService.Status.SUCCESS, result.status());
        InOrder order = inOrder(userDao, authTokenDao, connection, emailService);
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).findVerificationCandidateForUpdate("user@example.com", connection);
        order.verify(authTokenDao).invalidateOldTokens(user.id().toString(), "EMAIL_VERIFICATION", connection);
        order.verify(authTokenDao).insert(any(AuthToken.class), same(connection));
        order.verify(connection).commit();
        order.verify(emailService).send(any());
        verify(dataSource, times(1)).getConnection();
        verify(connection).close();
    }

    @Test
    void resend_eligibleUser_persistsOnlyIndependentSha256HashAndFixedExpiry() throws Exception {
        UserDao.VerificationCandidate user = unverifiedUser("user@example.com");
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection))).thenReturn(user);
        var rawCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        var tokenCaptor = org.mockito.ArgumentCaptor.forClass(AuthToken.class);
        when(templateService.createVerificationEmail(eq("user@example.com"), rawCaptor.capture()))
                .thenReturn(mock(com.resumainer.service.email.EmailMessage.class));

        service.resend("user@example.com", "captcha", "198.51.100.2");

        verify(authTokenDao).insert(tokenCaptor.capture(), same(connection));
        String rawToken = rawCaptor.getValue();
        AuthToken stored = tokenCaptor.getValue();
        String expected = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        assertEquals(expected, stored.getTokenHash());
        assertNotEquals(rawToken, stored.getTokenHash());
        assertTrue(stored.getTokenHash().matches("[0-9a-f]{64}"));
        assertEquals("EMAIL_VERIFICATION", stored.getTokenType());
        assertNull(stored.getConsumedAt());
        assertEquals(LocalDateTime.of(2026, 8, 6, 12, 0), stored.getExpiresAt());
    }

    @Test
    void resend_twoSuccesses_generateDifferentRawTokens() throws Exception {
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection)))
                .thenReturn(unverifiedUser("a@example.com"), unverifiedUser("b@example.com"));
        var rawCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        when(templateService.createVerificationEmail(anyString(), rawCaptor.capture()))
                .thenReturn(mock(com.resumainer.service.email.EmailMessage.class));

        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            service.resend("a@example.com", "captcha", "ip1");
            service.resend("b@example.com", "captcha", "ip2");

            assertEquals(2, rawCaptor.getAllValues().size());
            String first = rawCaptor.getAllValues().get(0);
            String second = rawCaptor.getAllValues().get(1);
            assertNotEquals(first, second);
            assertTrue(appender.list.stream().noneMatch(event -> {
                String message = event.getFormattedMessage();
                return message.contains(first) || message.contains(second);
            }), "Neither raw token may appear in resend logs");
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void resend_unknownVerifiedDeletedAndEligible_returnIdenticalPublicResult() throws Exception {
        UserDao.VerificationCandidate verified = verificationCandidate(true, false);
        UserDao.VerificationCandidate deleted = verificationCandidate(false, true);
        UserDao.VerificationCandidate eligible = unverifiedUser("eligible@example.com");
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection)))
                .thenReturn(null, verified, deleted, eligible);
        when(templateService.createVerificationEmail(anyString(), anyString()))
                .thenReturn(mock(com.resumainer.service.email.EmailMessage.class));

        ResendVerificationService.Result unknown = service.resend("unknown@example.com", "c", "ip1");
        ResendVerificationService.Result alreadyVerified = service.resend("verified@example.com", "c", "ip2");
        ResendVerificationService.Result deletedResult = service.resend("deleted@example.com", "c", "ip3");
        ResendVerificationService.Result eligibleResult = service.resend("eligible@example.com", "c", "ip4");

        assertEquals(unknown, alreadyVerified);
        assertEquals(unknown, deletedResult);
        assertEquals(unknown, eligibleResult);
    }

    @Test
    void resend_unknownAndEligibleOutcomes_haveSameMinimumDuration() throws Exception {
        UserDao.VerificationCandidate eligible = unverifiedUser("eligible@example.com");
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection))).thenReturn(null, eligible);
        when(templateService.createVerificationEmail(anyString(), anyString()))
                .thenReturn(mock(com.resumainer.service.email.EmailMessage.class));

        long unknownStart = System.nanoTime();
        service.resend("unknown@example.com", "c", "ip1");
        long unknownMs = (System.nanoTime() - unknownStart) / 1_000_000;
        long eligibleStart = System.nanoTime();
        service.resend("eligible@example.com", "c", "ip2");
        long eligibleMs = (System.nanoTime() - eligibleStart) / 1_000_000;

        assertTrue(unknownMs >= 190, "unknown outcome must have the uniform minimum delay");
        assertTrue(eligibleMs >= 190, "eligible outcome must have the uniform minimum delay");
    }

    @Test
    void resend_emailDeliveryFailsAfterCommit_returnsGenericSuccess() throws Exception {
        UserDao.VerificationCandidate user = unverifiedUser("user@example.com");
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection))).thenReturn(user);
        when(templateService.createVerificationEmail(anyString(), anyString()))
                .thenReturn(mock(com.resumainer.service.email.EmailMessage.class));
        doThrow(new EmailException("provider failure with internal detail"))
                .when(emailService).send(any());

        ResendVerificationService.Result result =
                service.resend("user@example.com", "captcha", "198.51.100.3");

        assertEquals(ResendVerificationService.Result.success(), result);
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void resend_emailDeliveryFailureLog_excludesProviderSecretDetails() throws Exception {
        UserDao.VerificationCandidate user = unverifiedUser("user@example.com");
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection))).thenReturn(user);
        when(templateService.createVerificationEmail(anyString(), anyString()))
                .thenReturn(mock(com.resumainer.service.email.EmailMessage.class));
        String secretMarker = "Bearer phase11-secret-authorization-value";
        doThrow(new EmailException(secretMarker)).when(emailService).send(any());
        Logger logger = (Logger) LoggerFactory.getLogger(ResendVerificationService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            service.resend("user@example.com", "captcha", "198.51.100.5");
            assertTrue(appender.list.stream()
                    .noneMatch(event -> event.getFormattedMessage().contains(secretMarker)));
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void resend_eligibleTokenWriteFailureAndUnknown_returnIdenticalGenericSuccess() throws Exception {
        UserDao.VerificationCandidate eligible = unverifiedUser("eligible@example.com");
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection)))
                .thenReturn(null, eligible);
        doThrow(new RuntimeException("database write failure"))
                .when(authTokenDao).invalidateOldTokens(anyString(), anyString(), same(connection));

        ResendVerificationService.Result unknown =
                service.resend("unknown@example.com", "captcha", "198.51.100.4");
        ResendVerificationService.Result eligibleFailure =
                service.resend("eligible@example.com", "captcha", "198.51.100.5");

        assertEquals(unknown, eligibleFailure,
                "account-specific token write failure must not reveal eligibility");
        assertEquals(ResendVerificationService.Result.success(), eligibleFailure);
        verify(connection).rollback();
        verifyNoInteractions(emailService);
        verify(connection, times(2)).close();
    }

    @Test
    void resend_invalidateFailure_rollsBackWithoutInsertEmailAndCloses() throws Exception {
        UserDao.VerificationCandidate eligible = unverifiedUser("eligible@example.com");
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection))).thenReturn(eligible);
        doThrow(new RuntimeException("invalidate failed"))
                .when(authTokenDao).invalidateOldTokens(anyString(), anyString(), same(connection));

        assertEquals(ResendVerificationService.Result.success(),
                service.resend("eligible@example.com", "captcha", "ip"));

        verify(connection).rollback();
        verify(authTokenDao, never()).insert(any(), any(Connection.class));
        verify(connection, never()).commit();
        verifyNoInteractions(emailService);
        verify(connection).close();
    }

    @Test
    void resend_insertFailure_rollsBackWithoutCommitEmailAndCloses() throws Exception {
        UserDao.VerificationCandidate eligible = unverifiedUser("eligible@example.com");
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection))).thenReturn(eligible);
        doThrow(new RuntimeException("insert failed"))
                .when(authTokenDao).insert(any(), same(connection));

        service.resend("eligible@example.com", "captcha", "ip");

        verify(connection).rollback();
        verify(connection, never()).commit();
        verifyNoInteractions(emailService);
        verify(connection).close();
    }

    @Test
    void resend_commitFailure_attemptsRollbackWithoutEmailAndCloses() throws Exception {
        UserDao.VerificationCandidate eligible = unverifiedUser("eligible@example.com");
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection))).thenReturn(eligible);
        doThrow(new SQLException("commit failed")).when(connection).commit();

        service.resend("eligible@example.com", "captcha", "ip");

        verify(connection).rollback();
        verifyNoInteractions(emailService);
        verify(connection).close();
    }

    @Test
    void resend_unknownAccount_commitsWithoutTokenOrEmailAndCloses() throws Exception {
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection))).thenReturn(null);

        service.resend("unknown@example.com", "captcha", "ip");

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verifyNoInteractions(authTokenDao, emailService);
        verify(connection).close();
    }

    @Test
    void resend_verifiedAndDeletedAccounts_doNotChangeTokensOrSendEmail() throws Exception {
        UserDao.VerificationCandidate verified = verificationCandidate(true, false);
        UserDao.VerificationCandidate deleted = verificationCandidate(false, true);
        when(userDao.findVerificationCandidateForUpdate(anyString(), same(connection)))
                .thenReturn(verified, deleted);

        service.resend("verified@example.com", "captcha", "ip1");
        service.resend("deleted@example.com", "captcha", "ip2");

        verifyNoInteractions(authTokenDao, emailService);
        verify(connection, times(2)).commit();
        verify(connection, times(2)).close();
    }

    private static UserDao.VerificationCandidate unverifiedUser(String email) {
        return verificationCandidate(false, false);
    }

    private static UserDao.VerificationCandidate verificationCandidate(boolean verified, boolean deleted) {
        return new UserDao.VerificationCandidate(UUID.randomUUID(), 1L, verified, deleted);
    }
}
