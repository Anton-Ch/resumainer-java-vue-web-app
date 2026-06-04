package com.resumainer.service;

import com.resumainer.dao.ContactDetailDao;
import com.resumainer.dao.RoleDao;
import com.resumainer.dao.UserDao;
import com.resumainer.dto.LoginRequest;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.ContactDetail;
import com.resumainer.model.Role;
import com.resumainer.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Service for user authentication: registration, login, logout.
 * <p>
 * Registration uses JDBC transaction management (Constitution IV):
 * User creation + ContactDetail creation in a single transaction.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final ContactDetailDao contactDetailDao;
    private final PasswordService passwordService;
    private final DataSource dataSource;

    public AuthService(UserDao userDao, RoleDao roleDao,
                       ContactDetailDao contactDetailDao,
                       PasswordService passwordService,
                       DataSource dataSource) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.contactDetailDao = contactDetailDao;
        this.passwordService = passwordService;
        this.dataSource = dataSource;
    }

    /**
     * Register a new user.
     * <p>
     * Validates input, checks email uniqueness, hashes password,
     * then creates User + ContactDetail in a single JDBC transaction.
     *
     * @param request the registration request (validated DTO)
     * @return the newly created User
     * @throws ServiceException if email is taken, passwords don't match,
     *                          password is weak, or database error occurs
     */
    public User register(RegisterRequest request) {
        // Validate password match
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            log.warn("Registration failed: password mismatch for email: {}", request.getEmail());
            throw new ServiceException("auth.password.mismatch", "Passwords do not match");
        }

        // Validate password strength
        if (!passwordService.isStrongPassword(request.getPassword())) {
            log.warn("Registration failed: weak password for email: {}", request.getEmail());
            throw new ServiceException("auth.password.weak", "Password does not meet strength requirements");
        }

        // Check email uniqueness
        String email = request.getEmail().toLowerCase().trim();
        User existing = userDao.findByEmail(email);
        if (existing != null) {
            log.warn("Registration failed: email already registered: {}", email);
            throw new ServiceException("auth.email.alreadyRegistered", "Email already registered");
        }

        // Hash password
        String passwordHash = passwordService.hashPassword(request.getPassword());

        // Create user entity
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);

        // Look up default role (USER)
        Role userRole = roleDao.findByCode("USER");
        if (userRole == null) {
            throw new ServiceException("auth.role.notFound", "Default role not found");
        }
        user.setRoleId(userRole.getId());
        // Default status = ACTIVE (id 1), default permission = ALLOWED (id 1)
        user.setStatusId(1L);
        user.setPermissionId(1L);
        user.setPrivileged(false);

        // Execute in transaction
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // Create user
            userDao.create(user, conn);

            // Create empty contact detail shell
            ContactDetail contactDetail = ContactDetail.createEmpty(user.getId());
            contactDetailDao.create(contactDetail, conn);

            conn.commit();
            log.info("User registered successfully: {}", email);
            return user;

        } catch (SQLException e) {
            rollbackQuietly(conn);
            log.error("Registration failed (rollback) for email: {}", email, e);
            throw new ServiceException("auth.registration.failed", "Registration failed due to database error", e);

        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Authenticate a user by email and password.
     * <p>
     * Checks: account exists → account is ACTIVE → account is not locked →
     * BCrypt password match. On failure: increments failed attempt counter,
     * locks account after 5 consecutive failures for 15 minutes.
     * On success: resets counter.
     *
     * @param request the login request (email + password)
     * @return the authenticated User
     * @throws ServiceException with generic error (no email enumeration)
     */
    public User authenticate(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.debug("Login attempt for email: {}", email);

        // Find user
        User user = userDao.findByEmail(email);
        if (user == null) {
            log.warn("Login failed: user not found for email: {}", email);
            throw new ServiceException("auth.invalidCredentials", "Invalid email or password");
        }

        // Check account status (statusId=1 = ACTIVE)
        if (user.getStatusId() != 1L || user.isDeleted()) {
            log.warn("Login failed: account blocked for email: {}", email);
            throw new ServiceException("auth.account.blocked",
                    "Your account is inactive. Contact support for assistance.");
        }

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("Login failed: account locked for email: {}", email);
            throw new ServiceException("auth.account.locked",
                    "Too many failed attempts. Try again later.");
        }

        // Verify password
        boolean passwordMatch = passwordService.verifyPassword(
                request.getPassword(), user.getPasswordHash());

        if (!passwordMatch) {
            // Increment failed attempts
            int newAttempts = user.getFailedLoginAttempts() + 1;
            LocalDateTime lockTime = null;

            if (newAttempts >= 5) {
                lockTime = LocalDateTime.now().plusMinutes(15);
                log.warn("Login failed: account locked after {} failed attempts for email: {}",
                        newAttempts, email);
            } else {
                log.warn("Login failed: wrong password (attempt {}/{}) for email: {}",
                        newAttempts, 5, email);
            }

            userDao.updateLoginAttempts(user.getId(), newAttempts, lockTime);

            if (newAttempts >= 5) {
                throw new ServiceException("auth.account.locked",
                        "Too many failed attempts. Try again later.");
            }
            throw new ServiceException("auth.invalidCredentials", "Invalid email or password");
        }

        // Success — reset counter and lock
        userDao.resetLoginAttempts(user.getId());
        log.info("Login successful for email: {}", email);
        return user;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                log.warn("Rollback failed", e);
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Failed to close connection", e);
            }
        }
    }
}
