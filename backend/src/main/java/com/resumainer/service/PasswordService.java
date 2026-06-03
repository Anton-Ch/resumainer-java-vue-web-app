package com.resumainer.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for password hashing and verification using BCrypt.
 * <p>
 * Uses {@code at.favre.lib:bcrypt} — a modern, actively maintained BCrypt fork.
 * Constitution V: BCrypt only, never log plaintext passwords.
 */
public class PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordService.class);

    /**
     * BCrypt cost factor — higher = slower but more secure.
     * 12 is a good balance for 2026 (takes ~250ms on modern hardware).
     */
    private static final int COST_FACTOR = 12;

    // Password strength requirements (per spec assumptions)
    private static final int MIN_LENGTH = 8;

    private static final String UPPERCASE_PATTERN = ".*[A-Z].*";
    private static final String LOWERCASE_PATTERN = ".*[a-z].*";
    private static final String DIGIT_PATTERN = ".*\\d.*";

    /**
     * Hash a password using BCrypt with default cost factor.
     *
     * @param plainPassword the plain-text password (not null)
     * @return BCrypt hash string starting with $2a$
     * @throws IllegalArgumentException if password is null
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        log.debug("Hashing password");
        return BCrypt.withDefaults()
                .hashToString(COST_FACTOR, plainPassword.toCharArray());
    }

    /**
     * Verify a password against a BCrypt hash.
     *
     * @param plainPassword the plain-text password to verify
     * @param bcryptHash    the BCrypt hash to verify against
     * @return true if the password matches the hash, false otherwise
     * @throws IllegalArgumentException if either argument is null
     */
    public boolean verifyPassword(String plainPassword, String bcryptHash) {
        if (plainPassword == null || bcryptHash == null) {
            throw new IllegalArgumentException("Password and hash must not be null");
        }
        log.debug("Verifying password");
        BCrypt.Result result = BCrypt.verifyer()
                .verify(plainPassword.toCharArray(), bcryptHash);
        return result.verified;
    }

    /**
     * Check if a password meets minimum strength requirements.
     * <p>
     * Requirements: minimum 8 characters, at least one uppercase letter,
     * one lowercase letter, and one digit.
     *
     * @param password the password to check
     * @return true if the password meets strength requirements
     */
    public boolean isStrongPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= MIN_LENGTH
                && password.matches(UPPERCASE_PATTERN)
                && password.matches(LOWERCASE_PATTERN)
                && password.matches(DIGIT_PATTERN);
    }
}
