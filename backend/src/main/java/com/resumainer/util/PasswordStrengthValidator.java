package com.resumainer.util;

/**
 * Reusable password strength validator.
 * <p>
 * Rules: minimum 8 characters, at least one uppercase letter,
 * one lowercase letter, and one digit.
 * <p>
 * Used by both frontend (Zod schema) and backend (Jakarta Validation).
 */
public final class PasswordStrengthValidator {

    private static final int MIN_LENGTH = 8;
    private static final String UPPERCASE_PATTERN = ".*[A-Z].*";
    private static final String LOWERCASE_PATTERN = ".*[a-z].*";
    private static final String DIGIT_PATTERN = ".*\\d.*";

    private PasswordStrengthValidator() {
        // Utility class — prevent instantiation
    }

    /**
     * Check if a password meets minimum strength requirements.
     *
     * @param password the password to check (null returns false)
     * @return true if the password meets all strength requirements
     */
    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= MIN_LENGTH
                && password.matches(UPPERCASE_PATTERN)
                && password.matches(LOWERCASE_PATTERN)
                && password.matches(DIGIT_PATTERN);
    }

    /**
     * Get a description of the password strength requirements.
     *
     * @return human-readable requirement description
     */
    public static String getRequirements() {
        return "Password must be at least " + MIN_LENGTH
                + " characters with at least one uppercase letter, "
                + "one lowercase letter, and one digit.";
    }
}
