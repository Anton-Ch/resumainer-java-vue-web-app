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
    void parseSkills_standardFormat_parsesSkillGroupSkillName() {
        String json = """
            {
              "professionalTitle": "Dev",
              "professionalSummary": "Summary",
              "skills": [
                {"skillGroup": "Languages", "skillName": "Java"},
                {"skillGroup": "Languages", "skillName": "SQL"},
                {"skillGroup": "Tools", "skillName": "Docker"}
              ]
            }
            """;
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");
        assertEquals(3, variants.get(0).skills.size());
        assertEquals("Languages", variants.get(0).skills.get(0).skillGroup);
        assertEquals("Java", variants.get(0).skills.get(0).skillName);
        assertEquals("Tools", variants.get(0).skills.get(2).skillGroup);
    }

    @Test
    void parseSkills_prototypeGroupFormat_parsesGroupNameSkillsArray() {
        // Prototype sends {groupName, skills[]} instead of {skillGroup, skillName}
        String json = """
            {
              "professionalTitle": "Dev",
              "professionalSummary": "Summary",
              "skills": [
                {"groupName": "Languages", "skills": ["Java", "SQL", "Python"]},
                {"groupName": "Tools", "skills": ["Docker", "Kubernetes"]}
              ]
            }
            """;
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");
        assertEquals(5, variants.get(0).skills.size());
        assertEquals("Languages", variants.get(0).skills.get(0).skillGroup);
        assertEquals("Java", variants.get(0).skills.get(0).skillName);
        assertEquals("Languages", variants.get(0).skills.get(2).skillGroup);
        assertEquals("Python", variants.get(0).skills.get(2).skillName);
        assertEquals("Tools", variants.get(0).skills.get(3).skillGroup);
        assertEquals("Docker", variants.get(0).skills.get(3).skillName);
    }

    @Test
    void parseBilingualAll_incompleteResponse_throwsException() {
        // Only EN+MINIMAL returned, missing EN+BALANCED, EN+MAXIMUM, RU+MINIMAL, RU+BALANCED, RU+MAXIMUM
        String json = """
            {
              "en": {
                "minimal": {"professionalTitle": "Dev EN", "professionalSummary": "Summary EN"}
              }
            }
            """;
        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(json, "BILINGUAL", "ALL"));
    }

    @Test
    void parseBilingualAll_missingLanguage_throwsException() {
        // Only EN variants, no RU
        String json = """
            {
              "en": {
                "minimal": {"professionalTitle": "Dev EN", "professionalSummary": "Sum EN"},
                "balanced": {"professionalTitle": "Dev EN B", "professionalSummary": "Sum EN B"},
                "maximum": {"professionalTitle": "Dev EN M", "professionalSummary": "Sum EN M"}
              }
            }
            """;
        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(json, "BILINGUAL", "ALL"));
    }

    @Test
    void parseBilingualAll_missingLevels_throwsException() {
        // Both languages but only MINIMAL, missing BALANCED and MAXIMUM
        String json = """
            {
              "en": {
                "minimal": {"professionalTitle": "Dev EN", "professionalSummary": "Sum EN"}
              },
              "ru": {
                "minimal": {"professionalTitle": "Dev RU", "professionalSummary": "Sum RU"}
              }
            }
            """;
        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(json, "BILINGUAL", "ALL"));
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
