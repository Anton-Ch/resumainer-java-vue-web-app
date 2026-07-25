package com.resumainer.util;

import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class TokenHashUtilTest {

    @Test
    void generateRawToken_returnsNonNull() {
        String token = TokenHashUtil.generateRawToken();
        assertNotNull(token);
    }

    @Test
    void generateRawToken_returnsUrlSafeBase64() {
        String token = TokenHashUtil.generateRawToken();
        // URL-safe Base64: alphanumeric, -, _, no padding
        assertTrue(token.matches("[A-Za-z0-9\\-_]+"),
                "Token must be URL-safe Base64: " + token);
    }

    @Test
    void generateRawToken_twoCalls_differentValues() {
        String t1 = TokenHashUtil.generateRawToken();
        String t2 = TokenHashUtil.generateRawToken();
        assertNotEquals(t1, t2);
    }

    @Test
    void hashToken_returns64HexChars() {
        String hash = TokenHashUtil.hashToken("test-token");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void hashToken_deterministic() {
        String input = "some-raw-token-value";
        String hash1 = TokenHashUtil.hashToken(input);
        String hash2 = TokenHashUtil.hashToken(input);
        assertEquals(hash1, hash2);
    }

    @Test
    void hashToken_differentInputs_differentHashes() {
        String h1 = TokenHashUtil.hashToken("token-a");
        String h2 = TokenHashUtil.hashToken("token-b");
        assertNotEquals(h1, h2);
    }

    @Test
    void hashToken_matchesIndependentCalculation() {
        String input = "test-raw-token-for-independent-verification";
        String utilHash = TokenHashUtil.hashToken(input);

        // Independent SHA-256 calculation (not using TokenHashUtil)
        String independentHash;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            independentHash = HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertEquals(independentHash, utilHash,
                "TokenHashUtil.hashToken must produce the same result as direct SHA-256");
    }

    @Test
    void hashToken_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> TokenHashUtil.hashToken(null));
    }

    @Test
    void hashToken_empty_throws() {
        assertThrows(IllegalArgumentException.class, () -> TokenHashUtil.hashToken(""));
    }
}
