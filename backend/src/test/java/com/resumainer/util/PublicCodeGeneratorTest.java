package com.resumainer.util;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PublicCodeGenerator.
 */
class PublicCodeGeneratorTest {

    @Test
    void generate_returnsFiveCharacters() {
        String code = PublicCodeGenerator.generate();
        assertNotNull(code);
        assertEquals(5, code.length());
    }

    @Test
    void generate_excludesAmbiguousChars() {
        String invalidChars = "0OIL1";
        for (int i = 0; i < 100; i++) {
            String code = PublicCodeGenerator.generate();
            for (char c : code.toCharArray()) {
                assertFalse(invalidChars.indexOf(c) >= 0,
                        "Generated code contains ambiguous char: " + c);
            }
        }
    }

    @Test
    void generate_usesValidCharsOnly() {
        String validChars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        for (int i = 0; i < 100; i++) {
            String code = PublicCodeGenerator.generate();
            for (char c : code.toCharArray()) {
                assertTrue(validChars.indexOf(c) >= 0,
                        "Generated code contains invalid char: " + c);
            }
        }
    }

    @Test
    void generate_generatesUniqueCodes() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(PublicCodeGenerator.generate());
        }
        // 1000 codes should all be unique
        assertEquals(1000, codes.size());
    }

    @Test
    void generateWithRetry_returnsUniqueCodeOnFirstAttempt() {
        // Predicate always returns true (no collision)
        String code = PublicCodeGenerator.generateWithRetry(s -> true);
        assertNotNull(code);
        assertEquals(5, code.length());
    }

    @Test
    void generateWithRetry_fallsBackToLongerCodeOnPersistentCollision() {
        // Predicate always returns false (always collides)
        String code = PublicCodeGenerator.generateWithRetry(s -> false);
        assertNotNull(code);
        assertEquals(8, code.length());
    }

    @Test
    void generate_customLength() {
        String code = PublicCodeGenerator.generate(8);
        assertEquals(8, code.length());
    }
}
