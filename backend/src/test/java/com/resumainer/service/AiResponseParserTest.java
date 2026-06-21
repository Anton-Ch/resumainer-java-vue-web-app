package com.resumainer.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AiResponseParser covering language/adaptation combinations and required field validation.
 */
class AiResponseParserTest {

    private final AiResponseParser parser = new AiResponseParser();

    @Test
    void parseEnglishOnlyBalanced_returnsOneVariant() {
        String json = variantJson("Java Developer", "5 years of Java experience.");
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        assertEquals(1, variants.size());
        assertEquals("EN", variants.get(0).languageCode);
        assertEquals("BALANCED", variants.get(0).adaptationLevel);
        assertEquals("Java Developer", variants.get(0).professionalTitle);
    }

    @Test
    void parseRussianOnlyMinimal_returnsOneVariant() {
        String json = variantJson("Java разработчик", "5 лет опыта.");
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
                "minimal": %s,
                "balanced": %s,
                "maximum": %s
              },
              "ru": {
                "minimal": %s,
                "balanced": %s,
                "maximum": %s
              }
            }
            """.formatted(
                variantJson("Dev EN", "Summary EN"),
                variantJson("Dev EN B", "Summary EN B"),
                variantJson("Dev EN M", "Summary EN M"),
                variantJson("Dev RU", "Summary RU"),
                variantJson("Dev RU B", "Summary RU B"),
                variantJson("Dev RU M", "Summary RU M")
        );

        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "BILINGUAL", "ALL");

        assertEquals(6, variants.size());
        assertEquals("EN", variants.get(0).languageCode);
        assertEquals("MINIMAL", variants.get(0).adaptationLevel);
        assertEquals("RU", variants.get(3).languageCode);
    }

    @Test
    void parseBilingualBalanced_returnsTwoVariants() {
        String json = """
            {
              "en": {
                "balanced": %s
              },
              "ru": {
                "balanced": %s
              }
            }
            """.formatted(
                variantJson("Dev EN", "Summary EN"),
                variantJson("Dev RU", "Summary RU")
        );

        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "BILINGUAL", "BALANCED");
        assertEquals(2, variants.size());
    }

    @Test
    void parseInvalidJson_rejectsWithException() {
        assertThrows(IllegalArgumentException.class, () ->
                parser.parse("{invalid}", "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseMissingProfessionalTitle_rejectsWithException() {
        String json = variantJsonWithout("""
              "professionalTitle": "Business Analyst",
            """);

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseMissingValueLine_rejectsWithException() {
        String json = variantJsonWithout("""
              "valueLine": "Analyst focused on reporting workflows.",
            """);

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseMissingProfessionalSummary_rejectsWithException() {
        String json = variantJsonWithout("""
              "professionalSummary": "Summary",
            """);

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseMissingProfessionalAspirations_rejectsWithException() {
        String json = variantJsonWithout("""
              "professionalAspirations": "Grow into a stronger backend-oriented analyst.",
            """);

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseMissingWorkExperience_rejectsWithException() {
        String json = variantJsonWithout("""
              "workExperience": [
                {
                  "sourceId": "work-5",
                  "jobTitle": "Business Analyst",
                  "companyName": "Bobrosoft",
                  "description": "Gathered requirements.",
                  "location": "Astana",
                  "startDate": "2025-05",
                  "endDate": null
                }
              ],
            """);

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseWorkExperienceItemMissingSourceId_rejectsWithException() {
        String json = variantJson().replace("""
                  "sourceId": "work-5",
                """, "");

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseWorkExperienceItemMissingJobTitle_rejectsWithException() {
        String json = variantJson().replace("""
                  "jobTitle": "Business Analyst",
                """, "");

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseWorkExperienceItemMissingCompanyName_rejectsWithException() {
        String json = variantJson().replace("""
                  "companyName": "Bobrosoft",
                """, "");

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseWorkExperienceItemMissingDescription_rejectsWithException() {
        String json = variantJson().replace("""
                  "description": "Gathered requirements.",
                """, "");

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseMissingSkills_rejectsWithException() {
        String json = variantJsonWithout("""
              "skills": [
                {"skillGroup": "Analysis", "skillName": "BPMN"}
              ],
            """);

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseSkillMissingSkillGroup_rejectsWithException() {
        String json = variantJsonWithSections(
                defaultWorkExperienceSection(),
                defaultCoursesSection(),
                defaultProjectsSection(),
                """
              "skills": [
                {"skillName": "BPMN"}
              ],
                """,
                defaultPersonalInfoSection()
        );

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseSkillMissingSkillName_rejectsWithException() {
        String json = variantJsonWithSections(
                defaultWorkExperienceSection(),
                defaultCoursesSection(),
                defaultProjectsSection(),
                """
              "skills": [
                {"skillGroup": "Analysis"}
              ],
                """,
                defaultPersonalInfoSection()
        );

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "ENGLISH_ONLY", "BALANCED"));
    }

    @Test
    void parseMissingPersonalInfo_allowsProfileOwnedSectionToBeOmitted() {
        String json = variantJsonWithSections(
                defaultWorkExperienceSection(),
                defaultCoursesSection(),
                defaultProjectsSection(),
                defaultSkillsSectionWithoutTrailingComma(),
                ""
        );

        List<AiResponseParser.ParsedVariant> variants =
                parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        assertEquals(1, variants.size());
        assertNull(variants.get(0).personalInfo);
    }

    @Test
    void parsePersonalInfoMissingWorkFormats_allowsProfileOwnedFieldToBeOmitted() {
        String json = variantJsonWithSections(
                defaultWorkExperienceSection(),
                defaultCoursesSection(),
                defaultProjectsSection(),
                defaultSkillsSection(),
                """
              "personalInfo": {
                "location": "Astana",
                "willingnessToRelocate": "Yes",
                "willingnessForBusinessTrips": "Negotiable",
                "citizenship": "RK",
                "dateOfBirth": "1992-02-29",
                "spokenLanguages": "English"
              }
                """
        );

        List<AiResponseParser.ParsedVariant> variants =
                parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        assertEquals(1, variants.size());
        assertNotNull(variants.get(0).personalInfo);
        assertNull(variants.get(0).personalInfo.workFormats);
    }

    @Test
    void parseWithSnakeCaseFields_succeeds() {
        String json = """
            {
              "professional_title": "Dev",
              "value_line": "Backend expert",
              "professional_summary": "Summary",
              "professional_aspirations": "Aspirations",
              "work_experience": [
                {
                  "source_id": "work-5",
                  "job_title": "Business Analyst",
                  "company_name": "Bobrosoft",
                  "description": "Gathered requirements."
                }
              ],
              "skills": [
                {"skill_group": "Languages", "skill_name": "Java"}
              ],
              "personal_info": {
                "location": "Astana",
                "willingness_to_relocate": "Yes",
                "willingness_for_business_trips": "Negotiable",
                "citizenship": "RK",
                "date_of_birth": "1992-02-29",
                "work_formats": ["Remote"]
              }
            }
            """;

        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "MINIMAL");

        assertEquals(1, variants.size());
        assertEquals("Dev", variants.get(0).professionalTitle);
        assertEquals("work-5", variants.get(0).experience.get(0).sourceId);
    }

    @Test
    void parseSkills_standardFormat_parsesSkillGroupSkillName() {
        String json = variantJsonWithSections(
                defaultWorkExperienceSection(),
                defaultCoursesSection(),
                defaultProjectsSection(),
                """
              "skills": [
                {"skillGroup": "Languages", "skillName": "Java"},
                {"skillGroup": "Languages", "skillName": "SQL"},
                {"skillGroup": "Tools", "skillName": "Docker"}
              ],
                """,
                defaultPersonalInfoSection()
        );

        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        assertEquals(3, variants.get(0).skills.size());
        assertEquals("Languages", variants.get(0).skills.get(0).skillGroup);
        assertEquals("Java", variants.get(0).skills.get(0).skillName);
        assertEquals("Tools", variants.get(0).skills.get(2).skillGroup);
    }

    @Test
    void parseSkills_prototypeGroupFormat_parsesGroupNameSkillsArray() {
        String json = variantJsonWithSections(
                defaultWorkExperienceSection(),
                defaultCoursesSection(),
                defaultProjectsSection(),
                """
              "skills": [
                {"groupName": "Languages", "skills": ["Java", "SQL", "Python"]},
                {"groupName": "Tools", "skills": ["Docker", "Kubernetes"]}
              ],
                """,
                defaultPersonalInfoSection()
        );

        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        assertEquals(5, variants.get(0).skills.size());
        assertEquals("Languages", variants.get(0).skills.get(0).skillGroup);
        assertEquals("Java", variants.get(0).skills.get(0).skillName);
        assertEquals("Tools", variants.get(0).skills.get(3).skillGroup);
    }

    @Test
    void parseBilingualAll_incompleteResponse_throwsException() {
        String json = """
            {
              "en": {
                "minimal": %s
              }
            }
            """.formatted(variantJson("Dev EN", "Summary EN"));

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "BILINGUAL", "ALL"));
    }

    @Test
    void parseBilingualAll_missingLanguage_throwsException() {
        String json = """
            {
              "en": {
                "minimal": %s,
                "balanced": %s,
                "maximum": %s
              }
            }
            """.formatted(
                variantJson("Dev EN", "Sum EN"),
                variantJson("Dev EN B", "Sum EN B"),
                variantJson("Dev EN M", "Sum EN M")
        );

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "BILINGUAL", "ALL"));
    }

    @Test
    void parseBilingualAll_missingLevels_throwsException() {
        String json = """
            {
              "en": {
                "minimal": %s
              },
              "ru": {
                "minimal": %s
              }
            }
            """.formatted(
                variantJson("Dev EN", "Sum EN"),
                variantJson("Dev RU", "Sum RU")
        );

        assertThrows(IllegalArgumentException.class, () ->
                parser.parse(json, "BILINGUAL", "ALL"));
    }

    @Test
    void parseWorkFormats_extractsList() {
        String json = variantJson();

        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        assertNotNull(variants.get(0).personalInfo);
        assertTrue(variants.get(0).personalInfo.workFormats.contains("Remote"));
    }

    @Test
    void parseRepeatableSections_preservesSourceId() {
        String json = variantJson();

        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        assertEquals(1, variants.size());
        assertEquals("work-5", variants.get(0).experience.get(0).sourceId,
                "workExperience sourceId must be preserved");
        assertEquals("course-5", variants.get(0).courses.get(0).sourceId,
                "course sourceId must be preserved");
        assertEquals("project-2", variants.get(0).projects.get(0).sourceId,
                "project sourceId must be preserved");
    }

    @Test
    void parse_parsesWorkExperienceBulletPoints() {
        String json = variantJson().replace(
                "\"description\": \"Gathered requirements.\"",
                "\"description\": \"Gathered requirements.\",\n                  \"bulletPoints\": [\"Reduced reporting time by 30%\", \"Automated 5 manual workflows\"]"
        );
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        AiResponseParser.ExperienceItem exp = variants.get(0).experience.get(0);
        assertNotNull(exp.bulletPoints, "bulletPoints must not be null");
        assertEquals(2, exp.bulletPoints.size());
        assertEquals("Reduced reporting time by 30%", exp.bulletPoints.get(0));
        assertEquals("Automated 5 manual workflows", exp.bulletPoints.get(1));
    }

    @Test
    void parse_parsesProjectBulletPoints() {
        String json = variantJson().replace(
                "\"description\": \"Optimized reporting workflow.\"",
                "\"description\": \"Optimized reporting workflow.\",\n                  \"bulletPoints\": [\"Improved query performance by 40%\"]"
        );
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        AiResponseParser.ProjectItem proj = variants.get(0).projects.get(0);
        assertNotNull(proj.bulletPoints, "bulletPoints must not be null");
        assertEquals(1, proj.bulletPoints.size());
        assertEquals("Improved query performance by 40%", proj.bulletPoints.get(0));
    }

    @Test
    void parse_bulletPointsMissing_returnsEmptyList() {
        String json = variantJson(); // no bulletPoints in default JSON
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        AiResponseParser.ExperienceItem exp = variants.get(0).experience.get(0);
        assertNotNull(exp.bulletPoints, "bulletPoints must not be null when missing");
        assertTrue(exp.bulletPoints.isEmpty(), "bulletPoints must be empty when not in JSON");
    }

    @Test
    void parse_bulletPointsEmptyArray_returnsEmptyList() {
        String json = variantJson().replace(
                "\"description\": \"Gathered requirements.\"",
                "\"description\": \"Gathered requirements.\",\n                  \"bulletPoints\": []"
        );
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        AiResponseParser.ExperienceItem exp = variants.get(0).experience.get(0);
        assertNotNull(exp.bulletPoints);
        assertTrue(exp.bulletPoints.isEmpty());
    }

    @Test
    void parse_bulletPointsWhitespaceOnly_keptForLaterValidation() {
        String json = variantJson().replace(
                "\"description\": \"Gathered requirements.\"",
                "\"description\": \"Gathered requirements.\",\n                  \"bulletPoints\": [\"   \"]"
        );
        List<AiResponseParser.ParsedVariant> variants = parser.parse(json, "ENGLISH_ONLY", "BALANCED");

        AiResponseParser.ExperienceItem exp = variants.get(0).experience.get(0);
        assertEquals(1, exp.bulletPoints.size());
        assertEquals("   ", exp.bulletPoints.get(0)); // parser keeps, validator rejects
    }

    private String variantJson() {
        return variantJson("Business Analyst", "Summary");
    }

    private String variantJson(String professionalTitle, String professionalSummary) {
        return """
            {
              "professionalTitle": "%s",
              "valueLine": "Analyst focused on reporting workflows.",
              "professionalSummary": "%s",
              "professionalAspirations": "Grow into a stronger backend-oriented analyst.",
              "workExperience": [
                {
                  "sourceId": "work-5",
                  "jobTitle": "Business Analyst",
                  "companyName": "Bobrosoft",
                  "description": "Gathered requirements.",
                  "location": "Astana",
                  "startDate": "2025-05",
                  "endDate": null
                }
              ],
              "courses": [
                {
                  "sourceId": "course-5",
                  "name": "Microsoft Business Analysis",
                  "provider": "Coursera",
                  "courseFocus": "Business analysis"
                }
              ],
              "projects": [
                {
                  "sourceId": "project-2",
                  "projectName": "Reporting Optimization",
                  "role": "Developer",
                  "description": "Optimized reporting workflow.",
                  "startDate": "2026-05"
                }
              ],
              "skills": [
                {"skillGroup": "Analysis", "skillName": "BPMN"}
              ],
              "personalInfo": {
                "location": "Astana",
                "willingnessToRelocate": "Yes",
                "willingnessForBusinessTrips": "Negotiable",
                "citizenship": "RK",
                "dateOfBirth": "1992-02-29",
                "workFormats": ["Remote"]
              }
            }
            """.formatted(professionalTitle, professionalSummary);
    }

    private String variantJsonWithSections(String workExperienceSection,
                                           String coursesSection,
                                           String projectsSection,
                                           String skillsSection,
                                           String personalInfoSection) {
        return """
            {
              "professionalTitle": "Business Analyst",
              "valueLine": "Analyst focused on reporting workflows.",
              "professionalSummary": "Summary",
              "professionalAspirations": "Grow into a stronger backend-oriented analyst.",
            %s
            %s
            %s
            %s
            %s
            }
            """.formatted(
                workExperienceSection,
                coursesSection,
                projectsSection,
                skillsSection,
                personalInfoSection
        );
    }

    private String defaultWorkExperienceSection() {
        return """
              "workExperience": [
                {
                  "sourceId": "work-5",
                  "jobTitle": "Business Analyst",
                  "companyName": "Bobrosoft",
                  "description": "Gathered requirements.",
                  "location": "Astana",
                  "startDate": "2025-05",
                  "endDate": null
                }
              ],
                """;
    }

    private String defaultCoursesSection() {
        return """
              "courses": [
                {
                  "sourceId": "course-5",
                  "name": "Microsoft Business Analysis",
                  "provider": "Coursera",
                  "courseFocus": "Business analysis"
                }
              ],
                """;
    }

    private String defaultProjectsSection() {
        return """
              "projects": [
                {
                  "sourceId": "project-2",
                  "projectName": "Reporting Optimization",
                  "role": "Developer",
                  "description": "Optimized reporting workflow.",
                  "startDate": "2026-05"
                }
              ],
                """;
    }

    private String defaultSkillsSection() {
        return """
              "skills": [
                {"skillGroup": "Analysis", "skillName": "BPMN"}
              ],
                """;
    }

    private String defaultSkillsSectionWithoutTrailingComma() {
        return """
              "skills": [
                {"skillGroup": "Analysis", "skillName": "BPMN"}
              ]
                """;
    }

    private String defaultPersonalInfoSection() {
        return """
              "personalInfo": {
                "location": "Astana",
                "willingnessToRelocate": "Yes",
                "willingnessForBusinessTrips": "Negotiable",
                "citizenship": "RK",
                "dateOfBirth": "1992-02-29",
                "workFormats": ["Remote"]
              }
                """;
    }

    private String variantJsonWithout(String exactBlockToRemove) {
        return variantJson().replace(exactBlockToRemove, "");
    }
}
