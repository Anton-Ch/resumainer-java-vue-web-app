package com.resumainer.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Shared utility for cryptographically secure token generation and hashing.
 *
 * <p>Single source of truth for token operations used by registration
 * and verification. Eliminates duplicated SecureRandom/SHA-256 logic.
 */
public final class TokenHashUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenHashUtil() {
        // Utility class — no instantiation
    }

    /**
     * Generate a cryptographically secure random token string.
     * 32 bytes → 43 URL-safe Base64 characters (no padding).
     */
    public static String generateRawToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Hash a raw token using SHA-256.
     *
     * @param rawToken the raw token string
     * @return lowercase hex SHA-256 hash (64 characters)
     */
    public static String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isEmpty()) {
            throw new IllegalArgumentException("rawToken must not be null or empty");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
