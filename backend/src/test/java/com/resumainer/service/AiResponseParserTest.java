package com.resumainer.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AiResponseParser covering all language/adaptation combinations.
 */
class AiResponseParserTest {

    private final AiResponseParser parser = new AiResponseParser();

    @Test
    void parseEnglishOnlyBalanced_returnsOneVariant() {
        String json = """
            {
              "professionalTitle": "Java Developer",
              "valueLine": "Backend expert",
              "professionalSummary": "5 years of Java experience.",
              "professionalAspirations": "Senior role.",
              "skills": [{"skillGroup": "Languages", "skillName": "Java"}]
            }
            """;
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");
        assertEquals(1, variants.size());
        assertEquals("EN", variants.get(0).languageCode);
        assertEquals("BALANCED", variants.get(0).adaptationLevel);
        assertEquals("Java Developer", variants.get(0).professionalTitle);
    }

    @Test
    void parseRussianOnlyMinimal_returnsOneVariant() {
        String json = """
            {
              "professionalTitle": "Java разработчик",
              "professionalSummary": "5 лет опыта."
            }
            """;
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "RUSSIAN_ONLY", "MINIMAL");
        assertEquals(1, variants.size());
        assertEquals("RU", variants.get(0).languageCode);
        assertEquals("MINIMAL", variants.get(0).adaptationLevel);
    }

    @Test
    void parseBilingualAll_returnsSixVariants() {
        String json = """
            {
              "en": {
                "minimal": {"professionalTitle": "Dev EN", "professionalSummary": "Summary EN"},
                "balanced": {"professionalTitle": "Dev EN B", "professionalSummary": "Summary EN B"},
                "maximum": {"professionalTitle": "Dev EN M", "professionalSummary": "Summary EN M"}
              },
              "ru": {
                "minimal": {"professionalTitle": "Dev RU", "professionalSummary": "Summary RU"},
                "balanced": {"professionalTitle": "Dev RU B", "professionalSummary": "Summary RU B"},
                "maximum": {"professionalTitle": "Dev RU M", "professionalSummary": "Summary RU M"}
              }
            }
            """;
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "BILINGUAL", "ALL");
        assertEquals(6, variants.size());
        // EN variants come first
        assertEquals("EN", variants.get(0).languageCode);
        assertEquals("MINIMAL", variants.get(0).adaptationLevel);
        assertEquals("RU", variants.get(3).languageCode);
    }

    @Test
    void parseBilingualBalanced_returnsTwoVariants() {
        String json = """
            {
              "en": {
                "balanced": {"professionalTitle": "Dev EN", "professionalSummary": "Summary EN"}
              },
              "ru": {
                "balanced": {"professionalTitle": "Dev RU", "professionalSummary": "Summary RU"}
              }
            }
            """;
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "BILINGUAL", "BALANCED");
        assertEquals(2, variants.size());
    }

    @Test
    void parseInvalidJson_rejectsWithException() {
        assertThrows(IllegalArgumentException.class, () ->
            parser.parse("{invalid}", "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseMissingRequiredField_rejectsWithException() {
        String json = """
            { "valueLine": "only this" }
            """;
        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseWithSnakeCaseFields_succeeds() {
        String json = """
            {
              "professional_title": "Dev",
              "professional_summary": "Summary"
            }
            """;
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "MINIMAL");
        assertEquals(1, variants.size());
        assertEquals("Dev", variants.get(0).professionalTitle);
    }

    @Test
    void parseWorkFormats_extractsList() {
        String json = """
            {
              "professionalTitle": "Dev",
              "professionalSummary": "Summary",
              "personalInfo": {
                "workFormats": ["remote", "hybrid"]
              }
            }
            """;
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");
        assertNotNull(variants.get(0).personalInfo);
        assertTrue(variants.get(0).personalInfo.workFormats.contains("remote"));
        assertTrue(variants.get(0).personalInfo.workFormats.contains("hybrid"));
    }
}
