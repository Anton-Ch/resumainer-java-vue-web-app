package com.resumainer.util;

import java.security.SecureRandom;

/**
 * Generates unique public codes for saved resumes.
 * Default length: 5 characters. Character set excludes ambiguous chars.
 */
public class PublicCodeGenerator {

    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // No 0, O, I, L, 1
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 5;
    private static final int MAX_ATTEMPTS = 5;

    private PublicCodeGenerator() {}

    /**
     * Generates a random public code of default length (5 chars).
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Generates a random public code of the specified length.
     */
    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Attempts to generate a unique code with collision retry.
     * Returns a code after up to MAX_ATTEMPTS tries; falls back to longer code.
     */
    public static String generateWithRetry(java.util.function.Predicate<String> isUnique) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generate();
            if (isUnique.test(code)) {
                return code;
            }
        }
        // Fallback: generate a longer code (8 chars)
        return generate(8);
    }
}
