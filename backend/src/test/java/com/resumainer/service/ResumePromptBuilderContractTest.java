package com.resumainer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Test
    void buildJsonContract_allSupportedCombinations_returnValidJson() {
        String[] languageModes = {"ENGLISH_ONLY", "RUSSIAN_ONLY", "BILINGUAL"};
        String[] adaptationSelections = {"MINIMAL", "BALANCED", "MAXIMUM", "ALL"};

        for (String languageMode : languageModes) {
            for (String adaptationSelection : adaptationSelections) {
                String contract = builder.buildJsonContract(languageMode, adaptationSelection);

                assertDoesNotThrow(
                        () -> objectMapper.readTree(contract),
                        "Contract must be valid JSON for "
                                + languageMode + " / " + adaptationSelection
                                + "\nActual contract:\n" + contract
                );
            }
        }
    }

    @Test
    void buildJsonContract_russianOnlyBalanced_hasExpectedNestedShape() throws Exception {
        String contract = builder.buildJsonContract("RUSSIAN_ONLY", "BALANCED");

        JsonNode root = objectMapper.readTree(contract);
        JsonNode balanced = root.path("ru").path("balanced");

        assertTrue(root.has("ru"), "must contain ru root");
        assertFalse(root.has("en"), "must not contain en root");
        assertTrue(balanced.isObject(), "must contain ru.balanced object");
        assertTrue(balanced.has("professionalTitle"), "variant must contain professionalTitle");
        assertTrue(balanced.has("professionalSummary"), "variant must contain professionalSummary");
        assertTrue(balanced.has("professionalAspirations"), "variant must contain professionalAspirations");
        assertTrue(balanced.has("workExperience"), "variant must contain workExperience");
        assertTrue(balanced.has("courses"), "variant must contain courses");
        assertTrue(balanced.has("projects"), "variant must contain projects");
        assertTrue(balanced.has("skills"), "variant must contain skills");
        assertTrue(balanced.has("personalInfo"), "variant must contain personalInfo");
        assertTrue(balanced.has("coverLetter"), "variant must contain coverLetter");

        // Feature 008: bulletPoints as first-class array in workExperience and projects
        JsonNode workExpItem = balanced.path("workExperience").get(0);
        assertTrue(workExpItem.has("bulletPoints"), "workExperience must contain bulletPoints array");
        assertTrue(workExpItem.path("bulletPoints").isArray(), "bulletPoints must be an array");
        JsonNode projItem = balanced.path("projects").get(0);
        assertTrue(projItem.has("bulletPoints"), "projects must contain bulletPoints array");
        assertTrue(projItem.path("bulletPoints").isArray(), "bulletPoints must be an array");
    }

    @Test
    void buildJsonContract_bilingualAll_hasBothLanguagesAndAllLevels() throws Exception {
        String contract = builder.buildJsonContract("BILINGUAL", "ALL");

        JsonNode root = objectMapper.readTree(contract);

        assertTrue(root.path("en").path("minimal").isObject(), "must contain en.minimal");
        assertTrue(root.path("en").path("balanced").isObject(), "must contain en.balanced");
        assertTrue(root.path("en").path("maximum").isObject(), "must contain en.maximum");

        assertTrue(root.path("ru").path("minimal").isObject(), "must contain ru.minimal");
        assertTrue(root.path("ru").path("balanced").isObject(), "must contain ru.balanced");
        assertTrue(root.path("ru").path("maximum").isObject(), "must contain ru.maximum");
    }

    @Test
    void buildJsonContract_russianOnlyBalanced_repeatableSectionsContainSourceId() throws Exception {
        String contract = builder.buildJsonContract("RUSSIAN_ONLY", "BALANCED");

        JsonNode root = objectMapper.readTree(contract);
        JsonNode balanced = root.path("ru").path("balanced");

        assertTrue(balanced.path("workExperience").isArray(), "workExperience must be an array");
        assertTrue(balanced.path("workExperience").get(0).has("sourceId"),
                "workExperience items must contain sourceId");

        assertTrue(balanced.path("courses").isArray(), "courses must be an array");
        assertTrue(balanced.path("courses").get(0).has("sourceId"),
                "course items must contain sourceId");

        assertTrue(balanced.path("projects").isArray(), "projects must be an array");
        assertTrue(balanced.path("projects").get(0).has("sourceId"),
                "project items must contain sourceId");
    }

    @Test
    void buildJsonContract_workExperienceIsFirstPageIsBooleanInstructionNotHardcodedTrue() throws Exception {
        String contract = builder.buildJsonContract("RUSSIAN_ONLY", "BALANCED");

        JsonNode root = objectMapper.readTree(contract);
        JsonNode workExperienceItem = root.path("ru").path("balanced").path("workExperience").get(0);

        assertTrue(workExperienceItem.has("isFirstPage"),
                "workExperience items must contain isFirstPage");
        assertTrue(workExperienceItem.path("isFirstPage").isTextual(),
                "isFirstPage contract value must be an instruction string, not hardcoded boolean true");
        assertEquals("boolean - true for Page 1 records, false for Page 2 records",
                workExperienceItem.path("isFirstPage").asText());
    }

}
