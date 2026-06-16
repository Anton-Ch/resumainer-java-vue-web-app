package com.resumainer.service;

import com.resumainer.dao.ResumeBudgetConfigDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests for request/profile/budget-dependent AI response validation.
 * AiResponseParser validates JSON shape; AiResponseValidator validates business rules that need request/profile/budget context.
 */
@ExtendWith(MockitoExtension.class)
class AiResponseValidatorTest {

    @Mock private ResumeBudgetConfigService budgetConfigService;

    private AiResponseValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AiResponseValidator(budgetConfigService);

        lenient().when(budgetConfigService.getSkillsGroups()).thenReturn(4);
        lenient().when(budgetConfigService.getSkillsGroupsMax()).thenReturn(5);
        lenient().when(budgetConfigService.getSkillsPerGroup()).thenReturn(5);
        lenient().when(budgetConfigService.getSkillsPerGroupMax()).thenReturn(7);
        lenient().when(budgetConfigService.getMaxCourses()).thenReturn(7);
        lenient().when(budgetConfigService.getMaxProjects()).thenReturn(4);
        lenient().when(budgetConfigService.getWorkExperienceDistributionRules()).thenReturn(distributionRules());
    }

    @Test
    void validate_includeCoverLetterTrue_allVariantsHaveCoverLetter_passes() {
        List<AiResponseParser.ParsedVariant> variants = List.of(
                variant("EN", "MINIMAL", "Dear team, ..."),
                variant("RU", "MINIMAL", "Здравствуйте, ...")
        );

        assertDoesNotThrow(() ->
                validator.validate(variants, true, Map.of()));
    }

    @Test
    void validate_includeCoverLetterTrue_missingCoverLetter_rejects() {
        List<AiResponseParser.ParsedVariant> variants = List.of(
                variant("EN", "BALANCED", null)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(variants, true, Map.of()));

        assertTrue(ex.getMessage().contains("coverLetter is required"));
        assertTrue(ex.getMessage().contains("EN/BALANCED"));
    }

    @Test
    void validate_includeCoverLetterTrue_blankCoverLetter_rejects() {
        List<AiResponseParser.ParsedVariant> variants = List.of(
                variant("RU", "MAXIMUM", "   ")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(variants, true, Map.of()));

        assertTrue(ex.getMessage().contains("coverLetter is required"));
        assertTrue(ex.getMessage().contains("RU/MAXIMUM"));
    }

    @Test
    void validate_includeCoverLetterFalse_nullCoverLetter_passes() {
        List<AiResponseParser.ParsedVariant> variants = List.of(
                variant("EN", "MINIMAL", null),
                variant("RU", "MINIMAL", null)
        );

        assertDoesNotThrow(() ->
                validator.validate(variants, false, Map.of()));
    }

    @Test
    void validate_includeCoverLetterFalse_blankCoverLetter_passes() {
        List<AiResponseParser.ParsedVariant> variants = List.of(
                variant("EN", "BALANCED", "")
        );

        assertDoesNotThrow(() ->
                validator.validate(variants, false, Map.of()));
    }

    @Test
    void validate_includeCoverLetterFalse_generatedCoverLetter_rejects() {
        List<AiResponseParser.ParsedVariant> variants = List.of(
                variant("EN", "BALANCED", "Dear hiring team, ...")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(variants, false, Map.of()));

        assertTrue(ex.getMessage().contains("coverLetter must be null or blank when disabled"));
        assertTrue(ex.getMessage().contains("EN/BALANCED"));
    }

    @Test
    void validate_emptyVariants_rejects() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(), true, Map.of()));

        assertTrue(ex.getMessage().contains("no parsed variants"));
    }

    @Test
    void validate_returnedSourceIdsExistInProfilePayload_passes() {
        AiResponseParser.ParsedVariant variant = fullVariant();

        assertDoesNotThrow(() ->
                validator.validate(List.of(variant), true, fullProfilePayload()));
    }

    @Test
    void validate_profileHasManyCourses_responseMayIncludeRelevantSubset_passes() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.courses = List.of(course("course-2", "Advanced SQL", "LinkedIn Learning", "SQL reporting"));

        Map<String, Object> profilePayload = Map.of(
                "workExperience", List.of(Map.of("id", "work-5")),
                "courses", List.of(
                        Map.of("id", "course-1"),
                        Map.of("id", "course-2"),
                        Map.of("id", "course-3")
                ),
                "projects", List.of(Map.of("id", "project-2"))
        );

        assertDoesNotThrow(() ->
                validator.validate(List.of(variant), true, profilePayload));
    }

    @Test
    void validate_coursesExceedBudgetMaximum_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.courses = courses(8);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, profilePayloadWithCourses(8)));

        assertTrue(ex.getMessage().contains("course count exceeds budget maximum"));
    }

    @Test
    void validate_projectsExceedBudgetMaximum_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.projects = projects(5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, profilePayloadWithProjects(5)));

        assertTrue(ex.getMessage().contains("project count exceeds budget maximum"));
    }

    @Test
    void validate_skillGroupCountBelowBudgetMinimum_passesAsSoftWarning() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.skills = skills(3, 5);

        assertDoesNotThrow(() ->
                validator.validate(List.of(variant), true, fullProfilePayload()));
    }

    @Test
    void validate_skillGroupCountAboveBudgetMaximum_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.skills = skills(6, 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, fullProfilePayload()));

        assertTrue(ex.getMessage().contains("skill group count exceeds budget maximum"));
    }

    @Test
    void validate_skillsPerGroupBelowBudgetMinimum_passesAsSoftWarning() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.skills = skills(4, 4);

        assertDoesNotThrow(() ->
                validator.validate(List.of(variant), true, fullProfilePayload()));
    }

    @Test
    void validate_skillsPerGroupAboveBudgetMaximum_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.skills = skills(4, 8);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, fullProfilePayload()));

        assertTrue(ex.getMessage().contains("has too many skills"));
    }

    @Test
    void validate_skillsAtBudgetMaximum_passes() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.skills = skills(5, 7);

        assertDoesNotThrow(() ->
                validator.validate(List.of(variant), true, fullProfilePayload()));
    }

    @Test
    void validate_unknownWorkExperienceSourceId_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.experience.get(0).sourceId = "unknown-work";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, fullProfilePayload()));

        assertTrue(ex.getMessage().contains("unknown workExperience sourceId"));
    }

    @Test
    void validate_unknownCourseSourceId_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.courses.get(0).sourceId = "unknown-course";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, fullProfilePayload()));

        assertTrue(ex.getMessage().contains("unknown course sourceId"));
    }

    @Test
    void validate_unknownProjectSourceId_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.projects.get(0).sourceId = "unknown-project";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, fullProfilePayload()));

        assertTrue(ex.getMessage().contains("unknown project sourceId"));
    }

    @Test
    void validate_profileHasCourses_responseHasNoCourses_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.courses = List.of();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, fullProfilePayload()));

        assertTrue(ex.getMessage().contains("courses are required because profile has courses"));
    }

    @Test
    void validate_profileHasNoCourses_responseHasNoCourses_passes() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.courses = List.of();

        Map<String, Object> profilePayload = Map.of(
                "workExperience", List.of(Map.of("id", "work-5")),
                "courses", List.of(),
                "projects", List.of(Map.of("id", "project-2"))
        );

        assertDoesNotThrow(() ->
                validator.validate(List.of(variant), true, profilePayload));
    }

    @Test
    void validate_profileHasNoCourses_responseInventsCourse_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();

        Map<String, Object> profilePayload = Map.of(
                "workExperience", List.of(Map.of("id", "work-5")),
                "courses", List.of(),
                "projects", List.of(Map.of("id", "project-2"))
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, profilePayload));

        assertTrue(ex.getMessage().contains("courses returned but profile has no source course records"));
    }

    @Test
    void validate_courseMissingRequiredField_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.courses.get(0).courseFocus = null;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, fullProfilePayload()));

        assertTrue(ex.getMessage().contains("courses[].courseFocus is required"));
    }

    @Test
    void validate_profileHasProjects_responseHasNoProjects_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.projects = List.of();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, fullProfilePayload()));

        assertTrue(ex.getMessage().contains("projects are required because profile has projects"));
    }

    @Test
    void validate_profileHasNoProjects_responseHasNoProjects_passes() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.projects = List.of();

        Map<String, Object> profilePayload = Map.of(
                "workExperience", List.of(Map.of("id", "work-5")),
                "courses", List.of(Map.of("id", "course-5")),
                "projects", List.of()
        );

        assertDoesNotThrow(() ->
                validator.validate(List.of(variant), true, profilePayload));
    }

    @Test
    void validate_profileHasNoProjects_responseInventsProject_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();

        Map<String, Object> profilePayload = Map.of(
                "workExperience", List.of(Map.of("id", "work-5")),
                "courses", List.of(Map.of("id", "course-5")),
                "projects", List.of()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, profilePayload));

        assertTrue(ex.getMessage().contains("projects returned but profile has no source project records"));
    }

    @Test
    void validate_projectMissingRequiredField_rejects() {
        AiResponseParser.ParsedVariant variant = fullVariant();
        variant.projects.get(0).description = null;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validate(List.of(variant), true, fullProfilePayload()));

        assertTrue(ex.getMessage().contains("projects[].description is required"));
    }

    private AiResponseParser.ParsedVariant variant(String languageCode,
                                                   String adaptationLevel,
                                                   String coverLetter) {
        AiResponseParser.ParsedVariant variant = new AiResponseParser.ParsedVariant();
        variant.languageCode = languageCode;
        variant.adaptationLevel = adaptationLevel;
        variant.coverLetter = coverLetter;
        variant.experience = new ArrayList<>();
        variant.courses = new ArrayList<>();
        variant.projects = new ArrayList<>();
        variant.skills = skills(4, 5);
        return variant;
    }

    private AiResponseParser.ParsedVariant fullVariant() {
        AiResponseParser.ParsedVariant variant = variant("EN", "BALANCED", "Dear hiring team, ...");
        variant.experience = List.of(experience("work-5"));
        variant.courses = new ArrayList<>(List.of(course("course-5", "Microsoft Business Analysis", "Coursera", "Business analysis")));
        variant.projects = new ArrayList<>(List.of(project("project-2", "Reporting Optimization", "Developer", "Optimized reporting workflow.")));
        return variant;
    }

    private AiResponseParser.ExperienceItem experience(String sourceId) {
        AiResponseParser.ExperienceItem item = new AiResponseParser.ExperienceItem();
        item.sourceId = sourceId;
        item.jobTitle = "Business Analyst";
        item.companyName = "Bobrosoft";
        item.description = "Gathered requirements.";
        return item;
    }

    private AiResponseParser.CourseItem course(String sourceId, String name, String provider, String courseFocus) {
        AiResponseParser.CourseItem item = new AiResponseParser.CourseItem();
        item.sourceId = sourceId;
        item.name = name;
        item.provider = provider;
        item.courseFocus = courseFocus;
        return item;
    }

    private List<AiResponseParser.CourseItem> courses(int count) {
        List<AiResponseParser.CourseItem> items = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            items.add(course("course-" + i, "Course " + i, "Provider " + i, "Focus " + i));
        }
        return items;
    }

    private AiResponseParser.ProjectItem project(String sourceId, String projectName, String role, String description) {
        AiResponseParser.ProjectItem item = new AiResponseParser.ProjectItem();
        item.sourceId = sourceId;
        item.projectName = projectName;
        item.role = role;
        item.description = description;
        return item;
    }

    private List<AiResponseParser.ProjectItem> projects(int count) {
        List<AiResponseParser.ProjectItem> items = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            items.add(project("project-" + i, "Project " + i, "Role " + i, "Description " + i));
        }
        return items;
    }

    private List<AiResponseParser.SkillItem> skills(int groups, int skillsPerGroup) {
        List<AiResponseParser.SkillItem> items = new ArrayList<>();
        for (int g = 1; g <= groups; g++) {
            for (int s = 1; s <= skillsPerGroup; s++) {
                AiResponseParser.SkillItem item = new AiResponseParser.SkillItem();
                item.skillGroup = "Group " + g;
                item.skillName = "Skill " + g + "." + s;
                items.add(item);
            }
        }
        return items;
    }

    private Map<String, Object> fullProfilePayload() {
        return Map.of(
                "workExperience", List.of(Map.of("id", "work-5")),
                "courses", List.of(Map.of("id", "course-5")),
                "projects", List.of(Map.of("id", "project-2"))
        );
    }

    private Map<String, Object> profilePayloadWithCourses(int count) {
        List<Map<String, String>> courses = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            courses.add(Map.of("id", "course-" + i));
        }
        return Map.of(
                "workExperience", List.of(Map.of("id", "work-5")),
                "courses", courses,
                "projects", List.of(Map.of("id", "project-2"))
        );
    }

    private Map<String, Object> profilePayloadWithProjects(int count) {
        List<Map<String, String>> projects = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            projects.add(Map.of("id", "project-" + i));
        }
        return Map.of(
                "workExperience", List.of(Map.of("id", "work-5")),
                "courses", List.of(Map.of("id", "course-5")),
                "projects", projects
        );
    }

    private List<ResumeBudgetConfigDao.WorkExperienceDistributionRule> distributionRules() {
        return List.of(
                rule("EC-017", 4, 5, 0, 0, true,  "one_page", 5, 0, null, 5),
                rule("EC-001", 1, 1, 0, 0, false, "one_page", 1, 0, null, 10),
                rule("EC-002", 2, 2, 0, 0, false, "one_page", 2, 0, null, 10),
                rule("EC-003", 3, 3, 0, 0, false, "one_page", 3, 0, null, 10),
                rule("EC-004", 1, 1, 1, 1, false, "two_page", 1, 0, 0, 15),
                rule("EC-005", 2, 2, 1, 1, false, "two_page", 2, 0, 0, 15),
                rule("EC-006", 3, 3, 1, 1, false, "two_page", 3, 0, 0, 15),
                rule("EC-007", 1, 1, 2, null, false, "two_page", 1, 0, 0, 15),
                rule("EC-008", 2, 2, 2, null, false, "two_page", 2, 0, 0, 15),
                rule("EC-009", 3, 3, 2, null, false, "two_page", 3, 0, 0, 15),
                rule("EC-010", 4, 4, 0, 0, false, "two_page", 2, 2, 2, 20),
                rule("EC-011", 4, 4, 1, null, false, "two_page", 2, 2, 2, 20),
                rule("EC-012", 5, 5, 0, 0, false, "two_page", 3, 2, 2, 20),
                rule("EC-013", 5, 5, 1, null, false, "two_page", 3, 2, 2, 20),
                rule("EC-014", 6, 6, 0, 0, false, "two_page", 3, 3, 3, 25),
                rule("EC-015", 6, 6, 1, null, false, "two_page", 3, 3, 3, 25),
                rule("EC-016", 7, 99, 0, null, false, "two_page", 3, 7, 7, 30)
        );
    }

    private ResumeBudgetConfigDao.WorkExperienceDistributionRule rule(
            String caseKey,
            int minTotalJobs,
            int maxTotalJobs,
            int minProjects,
            Integer maxProjects,
            boolean requireNoCourses,
            String templateMode,
            int page1Jobs,
            int page2Jobs,
            Integer page2MaxAdditionalJobs,
            int priority
    ) {
        return new ResumeBudgetConfigDao.WorkExperienceDistributionRule(
                caseKey,
                minTotalJobs,
                maxTotalJobs,
                minProjects,
                maxProjects,
                requireNoCourses,
                templateMode,
                page1Jobs,
                page2Jobs,
                page2MaxAdditionalJobs,
                priority
        );
    }

}
