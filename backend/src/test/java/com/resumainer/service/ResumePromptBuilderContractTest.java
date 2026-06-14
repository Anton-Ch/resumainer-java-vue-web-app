package com.resumainer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for dynamic JSON contract generation.
 * These test the contract shape directly without mocking the full builder.
 */
@ExtendWith(MockitoExtension.class)
class ResumePromptBuilderContractTest {

    private final ResumePromptBuilder builder = new ResumePromptBuilder(null, null, null, null);

    @Test
    void buildJsonContract_russianOnlyMinimal_containsOnlyRuMinimal() {
        String contract = builder.buildJsonContract("RUSSIAN_ONLY", "MINIMAL");
        assertTrue(contract.contains("\"ru\""), "must contain ru root");
        assertTrue(contract.contains("\"minimal\""), "must contain minimal level");
        assertFalse(contract.contains("\"en\""), "must not contain en root");
        assertFalse(contract.contains("\"balanced\""), "must not contain balanced");
        assertFalse(contract.contains("\"maximum\""), "must not contain maximum");
    }

    @Test
    void buildJsonContract_russianOnlyBalanced_containsOnlyRuBalanced() {
        String contract = builder.buildJsonContract("RUSSIAN_ONLY", "BALANCED");
        assertTrue(contract.contains("\"ru\""));
        assertTrue(contract.contains("\"balanced\""));
        assertFalse(contract.contains("\"en\""));
        assertFalse(contract.contains("\"minimal\""));
        assertFalse(contract.contains("\"maximum\""));
    }

    @Test
    void buildJsonContract_russianOnlyMaximum_containsOnlyRuMaximum() {
        String contract = builder.buildJsonContract("RUSSIAN_ONLY", "MAXIMUM");
        assertTrue(contract.contains("\"ru\""));
        assertTrue(contract.contains("\"maximum\""));
        assertFalse(contract.contains("\"en\""));
        assertFalse(contract.contains("\"minimal\""));
        assertFalse(contract.contains("\"balanced\""));
    }

    @Test
    void buildJsonContract_englishOnlyMinimal_containsOnlyEnMinimal() {
        String contract = builder.buildJsonContract("ENGLISH_ONLY", "MINIMAL");
        assertTrue(contract.contains("\"en\""));
        assertTrue(contract.contains("\"minimal\""));
        assertFalse(contract.contains("\"ru\""));
        assertFalse(contract.contains("\"balanced\""));
        assertFalse(contract.contains("\"maximum\""));
    }

    @Test
    void buildJsonContract_englishOnlyBalanced_containsOnlyEnBalanced() {
        String contract = builder.buildJsonContract("ENGLISH_ONLY", "BALANCED");
        assertTrue(contract.contains("\"en\""));
        assertTrue(contract.contains("\"balanced\""));
        assertFalse(contract.contains("\"ru\""));
        assertFalse(contract.contains("\"minimal\""));
        assertFalse(contract.contains("\"maximum\""));
    }

    @Test
    void buildJsonContract_englishOnlyMaximum_containsOnlyEnMaximum() {
        String contract = builder.buildJsonContract("ENGLISH_ONLY", "MAXIMUM");
        assertTrue(contract.contains("\"en\""));
        assertTrue(contract.contains("\"maximum\""));
        assertFalse(contract.contains("\"ru\""));
        assertFalse(contract.contains("\"minimal\""));
        assertFalse(contract.contains("\"balanced\""));
    }

    @Test
    void buildJsonContract_bilingualAll_containsBothLanguagesAllLevels() {
        String contract = builder.buildJsonContract("BILINGUAL", "ALL");
        assertTrue(contract.contains("\"en\""), "must contain en");
        assertTrue(contract.contains("\"ru\""), "must contain ru");
        assertTrue(contract.contains("\"minimal\""), "must contain minimal");
        assertTrue(contract.contains("\"balanced\""), "must contain balanced");
        assertTrue(contract.contains("\"maximum\""), "must contain maximum");
    }
}
