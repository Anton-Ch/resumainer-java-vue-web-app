package com.resumainer.service.security;

import com.resumainer.dao.UserDao;
import com.resumainer.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Spring Security {@link UserDetailsService} that loads users by email.
 *
 * <p>Email is the only login identifier. Username is not accepted.
 *
 * <p>Account state checks throw specific exceptions for the failure handler:
 * <ul>
 *   <li>{@link UsernameNotFoundException} — user not found or deleted (safe generic)</li>
 *   <li>{@link DisabledException} — blocked, unverified, or password login disabled</li>
 *   <li>{@link LockedException} — account temporarily locked</li>
 * </ul>
 */
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Load user by email.
     *
     * @param email the email address (login identifier)
     * @return CustomUserDetails for the found user
     * @throws UsernameNotFoundException if user not found or deleted
     * @throws DisabledException         if blocked, unverified, or password login disabled
     * @throws LockedException           if account is temporarily locked
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email must not be empty");
        }

        String normalizedEmail = email.trim().toLowerCase();
        log.debug("Loading user by email: {}", normalizedEmail);

        User user = userDao.findByEmail(normalizedEmail);
        if (user == null) {
            log.warn("Login failed: user not found for email: {}", normalizedEmail);
            throw new UsernameNotFoundException("Invalid email or password");
        }

        // Check deleted (safe — same as not found)
        if (user.isDeleted()) {
            log.warn("Login failed: user deleted for email: {}", normalizedEmail);
            throw new UsernameNotFoundException("Invalid email or password");
        }

        // Check blocked (statusId=1 = ACTIVE)
        // Uses UsernameNotFoundException so failure handler maps to INVALID_CREDENTIALS
        if (user.getStatusId() != 1L) {
            log.warn("Login failed: account inactive for email: {}", normalizedEmail);
            throw new UsernameNotFoundException("Invalid email or password");
        }

        // Check locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            log.warn("Login failed: account locked for email: {}", normalizedEmail);
            throw new LockedException("Account is temporarily locked");
        }

        // Check email verified
        if (!user.isEmailVerified()) {
            log.warn("Login failed: email not verified for email: {}", normalizedEmail);
            throw new DisabledException("Email not verified. Please verify your email before logging in.");
        }

        // Check password login enabled
        // Uses UsernameNotFoundException so failure handler maps to INVALID_CREDENTIALS
        if (!user.isPasswordLoginEnabled()) {
            log.warn("Login failed: password login disabled for email: {}", normalizedEmail);
            throw new UsernameNotFoundException("Invalid email or password");
        }

        log.debug("User loaded successfully: {}", normalizedEmail);
        return new CustomUserDetails(user, user.getRoleId());
    }
}
