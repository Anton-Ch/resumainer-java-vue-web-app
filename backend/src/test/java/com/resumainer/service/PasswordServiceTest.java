package com.resumainer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void hashPassword_returnsValidBcryptHash() {
        String hash = passwordService.hashPassword("MyStr0ngPass!");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"), "BCrypt hash should start with $2a$");
        assertTrue(hash.length() >= 50, "BCrypt hash should be at least 50 chars");
    }

    @Test
    void verifyPassword_correctHash_returnsTrue() {
        String password = "MyStr0ngPass!";
        String hash = passwordService.hashPassword(password);
        assertTrue(passwordService.verifyPassword(password, hash));
    }

    @Test
    void verifyPassword_wrongHash_returnsFalse() {
        String password = "MyStr0ngPass!";
        String hash = passwordService.hashPassword(password);
        assertFalse(passwordService.verifyPassword("WrongPass1!", hash));
    }

    @Test
    void verifyPassword_nullInput_throwsException() {
        String hash = passwordService.hashPassword("MyStr0ngPass!");
        assertThrows(IllegalArgumentException.class,
                () -> passwordService.verifyPassword(null, hash));
        assertThrows(IllegalArgumentException.class,
                () -> passwordService.verifyPassword("pass", null));
    }

    @Test
    void isStrongPassword_valid_returnsTrue() {
        assertTrue(passwordService.isStrongPassword("MyStr0ng!"));
        assertTrue(passwordService.isStrongPassword("Abcdef1g"));
        assertTrue(passwordService.isStrongPassword("LongEn0ughPassword"));
    }

    @Test
    void isStrongPassword_tooShort_returnsFalse() {
        assertFalse(passwordService.isStrongPassword("Ab1!"));
        assertFalse(passwordService.isStrongPassword("Sh0rt!"));
    }

    @Test
    void isStrongPassword_noUppercase_returnsFalse() {
        assertFalse(passwordService.isStrongPassword("abcdef1gh"));
    }

    @Test
    void isStrongPassword_noLowercase_returnsFalse() {
        assertFalse(passwordService.isStrongPassword("ABCDEF1GH"));
    }

    @Test
    void isStrongPassword_noDigit_returnsFalse() {
        assertFalse(passwordService.isStrongPassword("Abcdefgh!"));
    }
}
