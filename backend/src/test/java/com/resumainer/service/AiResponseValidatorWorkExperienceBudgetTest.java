package com.resumainer.service;

import com.resumainer.dao.ResumeBudgetConfigDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Verifies hard validation of Work Experience budgets resolved from DB rules.
 */
@ExtendWith(MockitoExtension.class)
class AiResponseValidatorWorkExperienceBudgetTest {

    @Mock
    private ResumeBudgetConfigService budgetConfigService;

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
        when(budgetConfigService.getWorkExperienceDistributionRules()).thenReturn(distributionRules());
    }

    @Test
    void validate_workExperienceExceedsResolvedMax_rejects() {
        AiResponseParser.ParsedVariant variant = validVariant();
        variant.experience = selectedExperience(3, 8, true); // 11 total > EC-016 max 10

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(List.of(variant), false, denseProfilePayload()));

        assertTrue(ex.getMessage().contains("workExperience count exceeds resolved budget maximum"));
        assertTrue(ex.getMessage().contains("11 > 10"));
    }

    @Test
    void validate_page1CountExceedsResolvedTarget_rejects() {
        AiResponseParser.ParsedVariant variant = validVariant();
        variant.experience = selectedExperience(4, 6, true); // total 10 ok, Page 1 4 > 3

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(List.of(variant), false, denseProfilePayload()));

        assertTrue(ex.getMessage().contains("Page 1 workExperience count exceeds resolved budget"));
        assertTrue(ex.getMessage().contains("4 > 3"));
    }

    @Test
    void validate_page2CountExceedsResolvedTarget_rejects() {
        AiResponseParser.ParsedVariant variant = validVariant();
        variant.experience = selectedExperience(2, 8, true); // total 10 ok, Page 2 8 > 7

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(List.of(variant), false, denseProfilePayload()));

        assertTrue(ex.getMessage().contains("Page 2 workExperience count exceeds resolved budget"));
        assertTrue(ex.getMessage().contains("8 > 7"));
    }

    @Test
    void validate_currentJobMissingFromSelectedExperience_rejects() {
        AiResponseParser.ParsedVariant variant = validVariant();
        variant.experience = selectedExperienceWithoutCurrentJob();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(List.of(variant), false, denseProfilePayload()));

        assertTrue(ex.getMessage().contains("current workExperience sourceId must be included"));
        assertTrue(ex.getMessage().contains("work-1"));
    }

    @Test
    void validate_currentJobNotFirstSelectedRecord_rejects() {
        AiResponseParser.ParsedVariant variant = validVariant();
        variant.experience = selectedExperienceWithCurrentJobSecond();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(List.of(variant), false, denseProfilePayload()));

        assertTrue(ex.getMessage().contains("current workExperience sourceId must be the first selected record"));
        assertTrue(ex.getMessage().contains("work-1"));
    }

    @Test
    void validate_currentJobNotMarkedAsFirstPage_rejects() {
        AiResponseParser.ParsedVariant variant = validVariant();
        variant.experience = selectedExperience(3, 7, true);
        variant.experience.get(0).isFirstPage = false;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(List.of(variant), false, denseProfilePayload()));

        assertTrue(ex.getMessage().contains("current workExperience sourceId must have isFirstPage=true"));
    }

    @Test
    void validate_validResolvedWorkExperienceBudget_passes() {
        AiResponseParser.ParsedVariant variant = validVariant();
        variant.experience = selectedExperience(3, 7, true); // EC-016 exact budget: 3 + 7

        assertDoesNotThrow(() -> validator.validate(List.of(variant), false, denseProfilePayload()));
    }

    private AiResponseParser.ParsedVariant validVariant() {
        AiResponseParser.ParsedVariant variant = new AiResponseParser.ParsedVariant();
        variant.languageCode = "EN";
        variant.adaptationLevel = "BALANCED";
        variant.coverLetter = null;
        variant.skills = validSkills();
        variant.courses = List.of(course("course-1"));
        variant.projects = List.of(project("project-1"));
        variant.experience = selectedExperience(3, 7, true);
        return variant;
    }

    private List<AiResponseParser.ExperienceItem> selectedExperience(int page1Count, int page2Count, boolean includeCurrentFirst) {
        List<AiResponseParser.ExperienceItem> items = new ArrayList<>();
        int nextId = 1;

        if (includeCurrentFirst) {
            items.add(experience("work-1", true));
            nextId = 2;
        }

        while (items.stream().filter(item -> item.isFirstPage).count() < page1Count) {
            items.add(experience("work-" + nextId++, true));
        }
        for (int i = 0; i < page2Count; i++) {
            items.add(experience("work-" + nextId++, false));
        }
        return items;
    }

    private List<AiResponseParser.ExperienceItem> selectedExperienceWithoutCurrentJob() {
        List<AiResponseParser.ExperienceItem> items = new ArrayList<>();
        items.add(experience("work-2", true));
        items.add(experience("work-3", true));
        items.add(experience("work-4", true));
        for (int i = 5; i <= 11; i++) {
            items.add(experience("work-" + i, false));
        }
        return items;
    }

    private List<AiResponseParser.ExperienceItem> selectedExperienceWithCurrentJobSecond() {
        List<AiResponseParser.ExperienceItem> items = new ArrayList<>();
        items.add(experience("work-2", true));
        items.add(experience("work-1", true));
        items.add(experience("work-3", true));
        for (int i = 4; i <= 10; i++) {
            items.add(experience("work-" + i, false));
        }
        return items;
    }

    private AiResponseParser.ExperienceItem experience(String sourceId, boolean isFirstPage) {
        AiResponseParser.ExperienceItem item = new AiResponseParser.ExperienceItem();
        item.sourceId = sourceId;
        item.jobTitle = "Role " + sourceId;
        item.companyName = "Company " + sourceId;
        item.description = "Description " + sourceId;
        item.isFirstPage = isFirstPage;
        return item;
    }

    private AiResponseParser.CourseItem course(String sourceId) {
        AiResponseParser.CourseItem item = new AiResponseParser.CourseItem();
        item.sourceId = sourceId;
        item.name = "Course";
        item.provider = "Provider";
        item.courseFocus = "Java";
        return item;
    }

    private AiResponseParser.ProjectItem project(String sourceId) {
        AiResponseParser.ProjectItem item = new AiResponseParser.ProjectItem();
        item.sourceId = sourceId;
        item.projectName = "Project";
        item.role = "Developer";
        item.description = "Project description";
        return item;
    }

    private List<AiResponseParser.SkillItem> validSkills() {
        List<AiResponseParser.SkillItem> skills = new ArrayList<>();
        for (int group = 1; group <= 4; group++) {
            for (int i = 1; i <= 5; i++) {
                AiResponseParser.SkillItem item = new AiResponseParser.SkillItem();
                item.skillGroup = "Group " + group;
                item.skillName = "Skill " + i;
                skills.add(item);
            }
        }
        return skills;
    }

    private Map<String, Object> denseProfilePayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("workExperience", profileWorkExperience());
        payload.put("courses", profileCourses());
        payload.put("projects", profileProjects());
        return payload;
    }

    private List<Map<String, Object>> profileWorkExperience() {
        List<Map<String, Object>> records = new ArrayList<>();
        records.add(map("id", "work-1", "isCurrent", true));
        for (int i = 2; i <= 16; i++) {
            records.add(map("id", "work-" + i, "isCurrent", false));
        }
        return records;
    }

    private List<Map<String, Object>> profileCourses() {
        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            records.add(map("id", "course-" + i));
        }
        return records;
    }

    private List<Map<String, Object>> profileProjects() {
        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            records.add(map("id", "project-" + i));
        }
        return records;
    }

    private List<ResumeBudgetConfigDao.WorkExperienceDistributionRule> distributionRules() {
        return List.of(
                rule("EC-017", 4, 5, 0, 0, true,  "one_page", 5, 0, null, 5),
                rule("EC-001", 1, 1, 0, 0, false, "one_page", 1, 0, null, 10),
                rule("EC-002", 2, 2, 0, 0, false, "one_page", 2, 0, null, 10),
                rule("EC-003", 3, 3, 0, 0, false, "one_page", 3, 0, null, 10),
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

    private static Map<String, Object> map(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put((String) keyValues[i], keyValues[i + 1]);
        }
        return result;
    }
}
