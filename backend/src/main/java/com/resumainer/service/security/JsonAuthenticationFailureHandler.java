package com.resumainer.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dao.UserDao;
import com.resumainer.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Spring Security {@link AuthenticationFailureHandler} that returns a JSON
 * error response with a stable error code and tracks failed login attempts.
 *
 * <p>On {@link BadCredentialsException} (wrong password), increments the
 * failed login counter for the user. After 3 failed attempts signals
 * captcha required; after 5 locks the account for 15 minutes.
 */
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonAuthenticationFailureHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int CAPTCHA_THRESHOLD = 3;
    private static final int LOCK_THRESHOLD = 5;
    private static final int LOCK_MINUTES = 15;

    private final UserDao userDao;

    public JsonAuthenticationFailureHandler(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("email");
        if (email == null || email.isBlank()) {
            email = (String) request.getAttribute("auth_email");
        }

        String errorCode;
        String message;

        // Unwrap InternalAuthenticationServiceException to find the real cause
        Throwable cause = exception;
        if (exception instanceof InternalAuthenticationServiceException && exception.getCause() != null) {
            cause = exception.getCause();
        }

        if (exception instanceof BadCredentialsException) {
            // Increment failed login counter
            int attempts = incrementFailedCounter(email);

            if (attempts >= LOCK_THRESHOLD) {
                errorCode = "ACCOUNT_LOCKED";
                message = "Account is temporarily locked. Try again later.";
            } else if (attempts >= CAPTCHA_THRESHOLD) {
                errorCode = "CAPTCHA_REQUIRED";
                message = "Invalid email or password. CAPTCHA required.";
            } else {
                errorCode = "INVALID_CREDENTIALS";
                message = "Invalid email or password";
            }
        } else if (cause instanceof LockedException) {
            errorCode = "ACCOUNT_LOCKED";
            message = "Account is temporarily locked. Try again later.";
        } else if (cause instanceof DisabledException) {
            errorCode = "EMAIL_NOT_VERIFIED";
            message = "Please verify your email before logging in.";
        } else {
            errorCode = "INVALID_CREDENTIALS";
            message = "Invalid email or password";
        }

        // Safe application logging: email, IP, error code only
        // Never log: passwords, password hashes, raw tokens, token hashes, API keys, secrets, stack traces
        String ip = request.getRemoteAddr();
        if (email != null && !email.isBlank()) {
            log.warn("Login failed: email={}, ip={}, errorCode={}", email, ip, errorCode);
        } else {
            log.warn("Login failed: ip={}, errorCode={}", ip, errorCode);
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), Map.of(
                "success", false,
                "message", message,
                "errorCode", errorCode
        ));
    }

    /**
     * Increments the failed login counter only for users eligible for
     * password-login failure tracking.
     *
     * <p>Does NOT increment for:
     * <ul>
     *   <li>unknown email (no user found)</li>
     *   <li>deleted user</li>
     *   <li>blocked user (statusId != ACTIVE)</li>
     *   <li>unverified email</li>
     *   <li>password login disabled</li>
     *   <li>already locked account</li>
     * </ul>
     *
     * <p>This prevents enumeration via counter side-effects: blocked, deleted,
     * unverified, and disabled accounts all behave the same as unknown email
     * (no counter increment).
     *
     * @return the new failed attempt count, or -1 if not eligible
     */
    private int incrementFailedCounter(String email) {
        if (email == null || email.isBlank()) {
            return -1;
        }

        try {
            User user = userDao.findByEmail(email);
            // Not found or deleted — safe, no increment
            if (user == null || user.isDeleted()) {
                return -1;
            }
            // Blocked — treated same as not found for enumeration prevention
            if (user.getStatusId() != 1L) {
                return -1;
            }
            // Unverified — not eligible for password login
            if (!user.isEmailVerified()) {
                return -1;
            }
            // Password login disabled — not eligible
            if (!user.isPasswordLoginEnabled()) {
                return -1;
            }
            // Already locked — no further increment needed
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
                return -1;
            }

            int newAttempts = user.getFailedLoginAttempts() + 1;
            LocalDateTime lockedUntil = null;

            if (newAttempts >= LOCK_THRESHOLD) {
                lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
                log.warn("Account locked after {} failed attempts for email: {}", newAttempts, email);
            }

            userDao.updateLoginAttempts(user.getId(), newAttempts, lockedUntil);
            return newAttempts;

        } catch (Exception e) {
            log.warn("Failed to update login counter for email: {}", email);
            return -1;
        }
    }
}
