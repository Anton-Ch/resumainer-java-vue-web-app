package com.resumainer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordStrengthValidatorTest {

    @Test
    void validPassword_returnsTrue() {
        assertTrue(PasswordStrengthValidator.isValid("MyStr0ng!"));
        assertTrue(PasswordStrengthValidator.isValid("Abcdef1g"));
        assertTrue(PasswordStrengthValidator.isValid("LongEn0ughPassword"));
    }

    @Test
    void tooShort_returnsFalse() {
        assertFalse(PasswordStrengthValidator.isValid("Ab1!"));
        assertFalse(PasswordStrengthValidator.isValid("Sh0rt!"));
    }

    @Test
    void noUppercase_returnsFalse() {
        assertFalse(PasswordStrengthValidator.isValid("abcdef1gh"));
    }

    @Test
    void noLowercase_returnsFalse() {
        assertFalse(PasswordStrengthValidator.isValid("ABCDEF1GH"));
    }

    @Test
    void noDigit_returnsFalse() {
        assertFalse(PasswordStrengthValidator.isValid("Abcdefgh!"));
    }

    @Test
    void nullInput_returnsFalse() {
        assertFalse(PasswordStrengthValidator.isValid(null));
    }

    @Test
    void getRequirements_returnsNonEmpty() {
        assertNotNull(PasswordStrengthValidator.getRequirements());
        assertFalse(PasswordStrengthValidator.getRequirements().isBlank());
    }
}
