package com.resumainer.service;

import com.resumainer.dao.AuthTokenDao;
import com.resumainer.dao.ContactDetailDao;
import com.resumainer.dao.RoleDao;
import com.resumainer.dao.UserDao;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.AuthToken;
import com.resumainer.model.ContactDetail;
import com.resumainer.model.Role;
import com.resumainer.model.User;
import com.resumainer.service.email.EmailMessage;
import com.resumainer.service.email.EmailService;
import com.resumainer.service.email.EmailTemplateService;
import com.resumainer.service.security.CaptchaResult;
import com.resumainer.service.security.CaptchaService;
import com.resumainer.util.TokenHashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Service for user registration with email verification.
 *
 * <p>Phase 10: Registration requires CAPTCHA verification, creates an
 * unverified user, generates a hashed email verification token, and sends
 * a verification email. No auto-login after registration.
 *
 * <p>Token hashing uses the shared {@link TokenHashUtil}.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final ContactDetailDao contactDetailDao;
    private final PasswordService passwordService;
    private final CaptchaService captchaService;
    private final AuthTokenDao authTokenDao;
    private final EmailTemplateService emailTemplateService;
    private final EmailService emailService;
    private final DataSource dataSource;
    private final int verificationTtlMinutes;

    public AuthService(UserDao userDao, RoleDao roleDao,
                       ContactDetailDao contactDetailDao,
                       PasswordService passwordService,
                       CaptchaService captchaService,
                       AuthTokenDao authTokenDao,
                       EmailTemplateService emailTemplateService,
                       EmailService emailService,
                       DataSource dataSource,
                       @Value("${app.auth.email-verification.ttl-minutes:1440}")
                       int verificationTtlMinutes) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.contactDetailDao = contactDetailDao;
        this.passwordService = passwordService;
        this.captchaService = captchaService;
        this.authTokenDao = authTokenDao;
        this.emailTemplateService = emailTemplateService;
        this.emailService = emailService;
        this.dataSource = dataSource;
        this.verificationTtlMinutes = verificationTtlMinutes;
    }

    /**
     * Register a new user with email verification.
     *
     * <p>Order:
     * <ol>
     *   <li>Validate captcha (before any side effect)</li>
     *   <li>Validate password match and strength</li>
     *   <li>Check email uniqueness</li>
     *   <li>JDBC transaction: create user + contact + auth_token</li>
     *   <li>Commit</li>
     *   <li>Send verification email (after commit)</li>
     * </ol>
     */
    public User register(RegisterRequest request) {
        // 1. Validate captcha FIRST
        CaptchaResult captchaResult = captchaService.verify(request.getCaptchaToken());
        if (!captchaResult.isSuccess()) {
            log.warn("Registration failed: invalid captcha for email: {}", request.getEmail());
            throw new ServiceException("auth.captcha.invalid", "CAPTCHA verification failed. Please try again.");
        }

        // 2. Validate password match
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            log.warn("Registration failed: password mismatch for email: {}", request.getEmail());
            throw new ServiceException("auth.password.mismatch", "Passwords do not match");
        }

        // 3. Validate password strength
        if (!passwordService.isStrongPassword(request.getPassword())) {
            log.warn("Registration failed: weak password for email: {}", request.getEmail());
            throw new ServiceException("auth.password.weak", "Password does not meet strength requirements");
        }

        // 4. Check email uniqueness
        String email = request.getEmail().toLowerCase().trim();
        User existing = userDao.findByEmail(email);
        if (existing != null) {
            log.warn("Registration failed: email already registered: {}", email);
            throw new ServiceException("auth.email.alreadyRegistered", "Email already registered");
        }

        // 5. Generate raw token + hash (before transaction)
        String rawToken = TokenHashUtil.generateRawToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(verificationTtlMinutes);

        // 6. Hash password
        String passwordHash = passwordService.hashPassword(request.getPassword());

        // 7. Build user entity (unverified)
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setEmailVerified(false);
        user.setPasswordLoginEnabled(true);

        Role userRole = roleDao.findByCode("USER");
        if (userRole == null) {
            throw new ServiceException("auth.role.notFound", "Default role not found");
        }
        user.setRoleId(userRole.getId());
        user.setStatusId(1L);
        user.setPermissionId(1L);
        user.setPrivileged(false);

        // 8. Execute JDBC transaction: user + contact + auth_token
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            userDao.create(user, conn);

            ContactDetail contactDetail = ContactDetail.createEmpty(user.getId());
            contactDetailDao.create(contactDetail, conn);

            AuthToken token = new AuthToken(user.getId().toString(), "EMAIL_VERIFICATION", tokenHash, expiresAt);
            authTokenDao.insert(token, conn);

            conn.commit();
            log.info("User registered successfully (unverified): {}", email);

        } catch (Exception e) {
            rollbackQuietly(conn);
            // Check for PostgreSQL unique violation on email (SQLState 23505)
            if (isUniqueViolation(e)) {
                log.warn("Registration failed: duplicate email (race) for: {}", email);
                throw new ServiceException("auth.email.duplicate",
                        "An account with this email already exists. Please sign in instead.");
            }
            log.error("Registration failed (rollback) for email: {}", email, e);
            throw new ServiceException("auth.registration.failed", "Registration failed due to database error", e);

        } finally {
            closeQuietly(conn);
        }

        // 9. Send verification email AFTER commit
        try {
            EmailMessage emailMsg = emailTemplateService.createVerificationEmail(email, rawToken);
            emailService.send(emailMsg);
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.warn("Verification email sending failed for user: {} (user created, resend can recover)", email);
        }

        return user;
    }

    /**
     * Check if an exception is caused by a PostgreSQL unique constraint violation.
     * Walks the cause chain looking for SQLState 23505.
     */
    private boolean isUniqueViolation(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof SQLException se && "23505".equals(se.getSQLState())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException e) { log.warn("Rollback failed", e); }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { log.warn("Failed to close connection", e); }
        }
    }
}
