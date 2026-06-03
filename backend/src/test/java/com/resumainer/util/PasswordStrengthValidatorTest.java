package com.resumainer.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void nullEmptyAndBlank_returnsFalse(String input) {
        assertFalse(PasswordStrengthValidator.isValid(input));
    }

    @Test
    void getRequirements_returnsNonEmpty() {
        assertNotNull(PasswordStrengthValidator.getRequirements());
        assertFalse(PasswordStrengthValidator.getRequirements().isBlank());
    }
}
