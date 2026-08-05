package com.resumainer.service;

import com.resumainer.dao.AuthTokenDao;
import com.resumainer.dao.ContactDetailDao;
import com.resumainer.dao.RoleDao;
import com.resumainer.dao.UserDao;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.AuthToken;
import com.resumainer.model.Role;
import com.resumainer.model.User;
import com.resumainer.service.email.EmailService;
import com.resumainer.service.email.EmailTemplateService;
import com.resumainer.service.security.CaptchaResult;
import com.resumainer.service.security.CaptchaService;
import com.resumainer.util.TokenHashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserDao userDao;
    private RoleDao roleDao;
    private ContactDetailDao contactDetailDao;
    private PasswordService passwordService;
    private CaptchaService captchaService;
    private AuthTokenDao authTokenDao;
    private EmailTemplateService emailTemplateService;
    private EmailService emailService;
    private DataSource dataSource;
    private Connection connection;
    private AuthService authService;
    private Clock clock;

    @BeforeEach
    void setUp() throws Exception {
        userDao = mock(UserDao.class);
        roleDao = mock(RoleDao.class);
        contactDetailDao = mock(ContactDetailDao.class);
        passwordService = mock(PasswordService.class);
        captchaService = mock(CaptchaService.class);
        authTokenDao = mock(AuthTokenDao.class);
        emailTemplateService = mock(EmailTemplateService.class);
        emailService = mock(EmailService.class);
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(captchaService.verify(any())).thenReturn(CaptchaResult.success());

        clock = Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC);
        authService = new AuthService(userDao, roleDao, contactDetailDao,
                passwordService, captchaService, authTokenDao,
                emailTemplateService, emailService, dataSource, clock, 1440);

        // Default: userDao.create sets ID
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getId() == null) u.setId(UUID.randomUUID());
            return null;
        }).when(userDao).create(any(User.class), any(Connection.class));
    }

    // ============================================================
    // T110 — Captcha required
    // ============================================================

    @Test
    void register_invalidCaptcha_throwsBeforeAnySideEffect() throws Exception {
        when(captchaService.verify(any())).thenReturn(CaptchaResult.failure("CAPTCHA_INVALID"));

        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "StrongPass1", "bad");

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.register(request));
        assertEquals("auth.captcha.invalid", ex.getErrorCode());

        // BLOCKER 4: verify the real overloads — never any DB or email
        verify(userDao, never()).create(any(User.class), any(Connection.class));
        verify(contactDetailDao, never()).create(any(), any(Connection.class));
        verify(authTokenDao, never()).insert(any(), any(Connection.class));
        verify(dataSource, never()).getConnection();
        verify(emailService, never()).send(any());
        verify(passwordService, never()).hashPassword(any());
    }

    @Test
    void register_missingCaptchaToken_throwsException() {
        when(captchaService.verify(any())).thenReturn(CaptchaResult.failure("CAPTCHA_INVALID"));

        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "StrongPass1", "");

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.register(request));
        assertEquals("auth.captcha.invalid", ex.getErrorCode());
    }

    // ============================================================
    // T111 — Unverified user creation
    // ============================================================

    @Test
    void register_validInput_createsUnverifiedUser() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "StrongPass1", "captcha");
        when(passwordService.isStrongPassword("StrongPass1")).thenReturn(true);
        when(passwordService.hashPassword("StrongPass1")).thenReturn("$2a$12$hash");
        when(userDao.findByEmail("test@example.com")).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));

        User result = authService.register(request);

        assertNotNull(result);
        assertFalse(result.isEmailVerified(), "New user must be unverified");
        assertTrue(result.isPasswordLoginEnabled(), "Password login must be enabled");
        verify(passwordService).hashPassword("StrongPass1");
        verify(userDao).create(any(User.class), any(Connection.class));
        verify(contactDetailDao).create(any(), any(Connection.class));
        verify(authTokenDao).insert(any(), any(Connection.class));
        verify(emailService).send(any()); // email after commit
    }

    // ============================================================
    // T113 — Raw token is not stored (BLOCKER 3 real hash verification)
    // ============================================================

    @Test
    void register_tokenHash_isSha256OfRawToken() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));

        // Capture the raw token passed to email template
        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);
        when(emailTemplateService.createVerificationEmail(anyString(), rawTokenCaptor.capture()))
                .thenReturn(new com.resumainer.service.email.EmailMessage("t@t.com", "s", "<p>h</p>", "t"));

        // Capture the AuthToken inserted into DB
        ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        when(authTokenDao.insert(tokenCaptor.capture(), any(Connection.class))).thenReturn(null);

        authService.register(new RegisterRequest("test@example.com", "StrongPass1", "StrongPass1", "captcha"));

        String rawToken = rawTokenCaptor.getValue();
        AuthToken stored = tokenCaptor.getValue();

        assertNotNull(rawToken, "Raw token must be captured");
        assertNotNull(stored, "Stored token must be captured");

        // Independently calculate SHA-256 of the raw token (NOT using TokenHashUtil)
        String expectedHash;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            expectedHash = java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedHash, stored.getTokenHash(),
                "Stored token_hash must equal SHA-256(rawToken) calculated independently");
        assertNotEquals(rawToken, stored.getTokenHash(),
                "Stored token_hash must NOT equal rawToken");
        assertTrue(stored.getTokenHash().matches("[0-9a-f]{64}"),
                "token_hash must be 64 lowercase hex characters");
        assertEquals("EMAIL_VERIFICATION", stored.getTokenType());
        assertNull(stored.getConsumedAt(), "New token must not be consumed");
        assertEquals(LocalDateTime.of(2026, 8, 6, 12, 0), stored.getExpiresAt(),
                "Registration expiry must use the injected clock plus 1440 minutes");
    }

    @Test
    void register_twoRegistrations_differentRawTokens() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));
        // Capture ALL raw token values across multiple registrations
        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);
        when(emailTemplateService.createVerificationEmail(anyString(), rawTokenCaptor.capture()))
                .thenReturn(new com.resumainer.service.email.EmailMessage("t@t.com", "s", "<p>h</p>", "t"));

        authService.register(new RegisterRequest("a@t.com", "StrongPass1", "StrongPass1", "captcha"));
        authService.register(new RegisterRequest("b@t.com", "StrongPass1", "StrongPass1", "captcha"));

        var allTokens = rawTokenCaptor.getAllValues();
        assertEquals(2, allTokens.size(), "Must capture tokens from two registrations");
        assertNotNull(allTokens.get(0));
        assertNotNull(allTokens.get(1));
        assertNotEquals(allTokens.get(0), allTokens.get(1),
                "Two different registrations must produce different raw tokens");
    }

    @Test
    void register_generatedToken_isImmediatelyAcceptedByVerificationUsingSameClock() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));
        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AuthToken> storedTokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        when(emailTemplateService.createVerificationEmail(anyString(), rawTokenCaptor.capture()))
                .thenReturn(new com.resumainer.service.email.EmailMessage("t@t.com", "s", "<p>h</p>", "t"));
        when(authTokenDao.insert(storedTokenCaptor.capture(), any(Connection.class))).thenReturn(null);

        authService.register(new RegisterRequest("test@example.com", "StrongPass1", "StrongPass1", "captcha"));
        AuthToken stored = storedTokenCaptor.getValue();
        stored.setId(1L);
        when(authTokenDao.findByHash(stored.getTokenHash(), "EMAIL_VERIFICATION", connection))
                .thenReturn(stored);
        when(userDao.markEmailVerified(any(UUID.class), same(connection))).thenReturn(1);
        when(authTokenDao.markConsumed(1L, connection)).thenReturn(1);

        VerificationService verificationService = new VerificationService(
                authTokenDao, userDao, dataSource, clock);

        assertEquals(VerificationService.VerifyResult.SUCCESS,
                verificationService.verify(rawTokenCaptor.getValue()));
    }

    // ============================================================
    // T117 — Order: captcha before hashing, same connection for DB ops
    // ============================================================

    @Test
    void register_captchaCheckedBeforePasswordHash() {
        when(captchaService.verify(any())).thenReturn(CaptchaResult.failure("CAPTCHA_INVALID"));

        assertThrows(ServiceException.class,
                () -> authService.register(new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "bad")));

        InOrder order = inOrder(captchaService);
        order.verify(captchaService).verify(any());
        verify(passwordService, never()).hashPassword(any());
        verify(userDao, never()).create(any());
    }

    @Test
    void register_usesSameConnectionForDbOps() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));

        authService.register(new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "captcha"));

        // Verify all three DB ops use the same Connection
        verify(userDao).create(any(User.class), eq(connection));
        verify(contactDetailDao).create(any(), eq(connection));
        verify(authTokenDao).insert(any(), eq(connection));
    }

    @Test
    void register_commitBeforeEmail() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));

        authService.register(new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "captcha"));

        InOrder order = inOrder(connection, emailService);
        order.verify(connection).commit();
        order.verify(emailService).send(any());
    }

    // ============================================================
    // Existing validations
    // ============================================================

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "StrongPass1", "StrongPass1", "captcha");
        when(passwordService.isStrongPassword("StrongPass1")).thenReturn(true);
        when(userDao.findByEmail("existing@example.com")).thenReturn(new User());

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.register(request));
        assertEquals("auth.email.alreadyRegistered", ex.getErrorCode());
        verify(userDao, never()).create(any());
        verify(emailService, never()).send(any());
    }

    @Test
    void register_passwordMismatch_throwsException() {
        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "Different", "captcha");

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.register(request));
        assertEquals("auth.password.mismatch", ex.getErrorCode());
    }

    @Test
    void register_weakPassword_throwsException() {
        RegisterRequest request = new RegisterRequest("test@example.com", "weak", "weak", "captcha");

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.register(request));
        assertEquals("auth.password.weak", ex.getErrorCode());
    }

    // ============================================================
    // BLOCKER 4 — Transaction failure tests
    // ============================================================

    @Test
    void register_userInsertFailure_rollsBackNoEmail() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));
        doThrow(new RuntimeException("DB error")).when(userDao).create(any(User.class), any(Connection.class));

        assertThrows(ServiceException.class,
                () -> authService.register(new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "captcha")));

        verify(connection).rollback();
        verify(contactDetailDao, never()).create(any(), any(Connection.class));
        verify(authTokenDao, never()).insert(any(), any(Connection.class));
        verify(emailService, never()).send(any());
    }

    @Test
    void register_contactInsertFailure_rollsBackNoTokenNoEmail() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));
        doThrow(new RuntimeException("DB error")).when(contactDetailDao).create(any(), any(Connection.class));

        assertThrows(ServiceException.class,
                () -> authService.register(new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "captcha")));

        verify(connection).rollback();
        verify(authTokenDao, never()).insert(any(), any(Connection.class));
        verify(emailService, never()).send(any());
    }

    @Test
    void register_tokenInsertFailure_rollsBackNoEmail() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));
        doThrow(new RuntimeException("DB error")).when(authTokenDao).insert(any(), any(Connection.class));

        assertThrows(ServiceException.class,
                () -> authService.register(new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "captcha")));

        verify(connection).rollback();
        verify(emailService, never()).send(any());
    }

    @Test
    void register_commitFailure_rollsBackNoEmail() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));
        doThrow(new SQLException("Commit failed")).when(connection).commit();

        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.register(new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "captcha")));

        verify(connection).rollback();
        verify(emailService, never()).send(any());
        assertEquals("auth.registration.failed", ex.getErrorCode());
    }

    @Test
    void register_emailFailureAfterCommit_dbStateCommitted() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(userDao.findByEmail(any())).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));
        doThrow(new RuntimeException("Email send failed")).when(emailService).send(any());

        // Should NOT throw — email failure is logged, registration is committed
        User result = authService.register(new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "captcha"));

        assertNotNull(result);
        assertNotNull(result.getId());
        // Commit was already done
        verify(connection).commit();
        // No rollback after email failure
        verify(connection, never()).rollback();
    }

    // ============================================================
    // BLOCKER 6 — Unique violation race
    // ============================================================

    @Test
    void register_uniqueConstraintViolation_returnsDuplicateError() throws Exception {
        when(passwordService.isStrongPassword(any())).thenReturn(true);
        when(passwordService.hashPassword(any())).thenReturn("$2a$12$hash");
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));
        when(userDao.findByEmail(any())).thenReturn(null); // pre-check passes
        // Simulate PostgreSQL unique violation on insert (SQLState 23505)
        SQLException uniqueViolation = new SQLException("duplicate key", "23505", 23505);
        doThrow(new RuntimeException("Unique violation", uniqueViolation))
                .when(userDao).create(any(User.class), any(Connection.class));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.register(new RegisterRequest("t@t.com", "StrongPass1", "StrongPass1", "captcha")));

        assertEquals("auth.email.duplicate", ex.getErrorCode());
        verify(connection).rollback();
        verify(emailService, never()).send(any());
    }
}
