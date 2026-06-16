package com.resumainer.service;

import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dao.ProfilePromptDao;
import com.resumainer.dao.PromptConfigDao;
import com.resumainer.dao.ResumeBudgetConfigDao;
import com.resumainer.model.ResumeGenerationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Verifies that ResumePromptBuilder injects the DB-resolved Work Experience budget
 * into the prompt, including current-job-first and Page 1/Page 2 limits.
 */
@ExtendWith(MockitoExtension.class)
class ResumePromptBuilderWorkExperienceBudgetPromptTest {

    @Mock private PromptConfigDao promptConfigDao;
    @Mock private ProfilePromptDao profilePromptDao;
    @Mock private GenerationRequestDao generationRequestDao;
    @Mock private ResumeBudgetConfigService budgetConfigService;

    private ResumePromptBuilder builder;

    private final UUID requestId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID promptConfigId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        builder = new ResumePromptBuilder(
                promptConfigDao,
                profilePromptDao,
                generationRequestDao,
                budgetConfigService
        );

        when(promptConfigDao.getSystemPrompt(promptConfigId)).thenReturn("SYSTEM PROMPT");
        when(promptConfigDao.getLanguagePrompt(promptConfigId, "ENGLISH_ONLY")).thenReturn("LANGUAGE FRAGMENT");
        when(promptConfigDao.getAdaptationPrompt(promptConfigId, "BALANCED")).thenReturn("ADAPTATION FRAGMENT");
        when(promptConfigDao.getCoverLetterPrompt(promptConfigId, false)).thenReturn("COVER LETTER DISABLED");

        when(profilePromptDao.loadContact(userId)).thenReturn(map(
                "fullName", "Anton Example",
                "resumeEmail", "anton@example.com",
                "location", "Astana"
        ));
        when(profilePromptDao.loadWorkExperience(userId)).thenReturn(sixteenWorkExperienceRecords());
        when(profilePromptDao.loadEducation(userId)).thenReturn(List.of());
        when(profilePromptDao.loadCourses(userId)).thenReturn(sevenCourses());
        when(profilePromptDao.loadProjects(userId)).thenReturn(fourProjects());
        when(profilePromptDao.loadAdditionalInfo(userId)).thenReturn(map(
                "skills", "Java, SQL, BPMN"
        ));
        when(profilePromptDao.loadWorkFormats(userId)).thenReturn(List.of());

        mockBudgetConfig();
        when(budgetConfigService.getWorkExperienceDistributionRules()).thenReturn(distributionRules());
    }

    @Test
    void build_includesResolvedWorkExperienceBudgetFromDbRules() {
        ResumeGenerationRequest request = baseRequest();
        when(generationRequestDao.findById(requestId, userId)).thenReturn(request);

        ResumePromptBuilder.PromptResult result = builder.build(requestId, userId);

        assertTrue(result.requestPrompt.contains("Work Experience:"),
                "budget section must include Work Experience rules");
        assertTrue(result.requestPrompt.contains("Resolved DB case: EC-016"),
                "prompt must expose the matched DB edge case");
        assertTrue(result.requestPrompt.contains("Profile contains 16 work experience records"),
                "prompt must show total profile job count used by resolver");
        assertTrue(result.requestPrompt.contains("Return no more than 10 workExperience records total"),
                "prompt must enforce resolved maxTotalJobs");
        assertTrue(result.requestPrompt.contains("Page 1: return up to 3 primary workExperience records"),
                "prompt must use resolved targetPage1Jobs");
        assertTrue(result.requestPrompt.contains("Page 2: return up to 7 additional workExperience records"),
                "prompt must use resolved targetPage2Jobs");
        assertTrue(result.requestPrompt.contains("The Page 1 count comes from the DB distribution rule"),
                "prompt must avoid priority/count ambiguity");
        assertTrue(result.requestPrompt.contains("If the profile contains a current job, it must be the first workExperience record"),
                "prompt must force current job as first Page 1 record");
        assertTrue(result.requestPrompt.contains("Fill remaining Page 1 slots by suitability for the vacancy and company, then by recency"),
                "prompt must define selection priority inside the resolved limit");
        assertTrue(result.requestPrompt.contains("A source job can appear on only one page"),
                "prompt must forbid duplication across Page 1/Page 2");
    }

    private ResumeGenerationRequest baseRequest() {
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setPromptConfigId(promptConfigId);
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        request.setIncludeCoverLetter(false);
        request.setVacancyTitle("Senior Java Developer");
        request.setVacancyDescription("Legacy Spring MVC and custom JDBC connection pool");
        request.setCompanyName("ABC LTD.");
        return request;
    }

    private void mockBudgetConfig() {
        lenient().when(budgetConfigService.getSkillsGroups()).thenReturn(4);
        lenient().when(budgetConfigService.getSkillsGroupsMax()).thenReturn(5);
        lenient().when(budgetConfigService.getSkillsPerGroup()).thenReturn(5);
        lenient().when(budgetConfigService.getSkillsPerGroupMax()).thenReturn(7);
        lenient().when(budgetConfigService.getWordsPerSkillMax()).thenReturn(3);
        lenient().when(budgetConfigService.getMaxCourses()).thenReturn(7);
        lenient().when(budgetConfigService.getCourseFocusWordsMin()).thenReturn(1);
        lenient().when(budgetConfigService.getCourseFocusWordsMax()).thenReturn(3);
        lenient().when(budgetConfigService.getMaxProjects()).thenReturn(4);
        lenient().when(budgetConfigService.getProjectSentencesMin()).thenReturn(2);
        lenient().when(budgetConfigService.getProjectSentencesMax()).thenReturn(3);
    }

    private List<ResumeBudgetConfigDao.WorkExperienceDistributionRule> distributionRules() {
        return List.of(
                rule("EC-017", 4, 5, 0, 0, true, "one_page", 5, 0, null, 5),
                rule("EC-001", 1, 1, 0, 0, false, "one_page", 1, 0, null, 10),
                rule("EC-002", 2, 2, 0, 0, false, "one_page", 2, 0, null, 10),
                rule("EC-003", 3, 3, 0, 0, false, "one_page", 3, 0, null, 10),
                rule("EC-010", 4, 4, 0, 0, false, "two_page", 2, 2, 2, 20),
                rule("EC-012", 5, 5, 0, 0, false, "two_page", 3, 2, 2, 20),
                rule("EC-014", 6, 6, 0, 0, false, "two_page", 3, 3, 3, 25),
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

    private List<Map<String, Object>> sixteenWorkExperienceRecords() {
        List<Map<String, Object>> records = new ArrayList<>();
        records.add(map(
                "id", "work-current",
                "jobTitle", "Business Analyst",
                "companyName", "Current Company",
                "description", "Current role",
                "isCurrent", true
        ));
        for (int i = 2; i <= 16; i++) {
            records.add(map(
                    "id", "work-" + i,
                    "jobTitle", "Role " + i,
                    "companyName", "Company " + i,
                    "description", "Description " + i,
                    "isCurrent", false
            ));
        }
        return records;
    }

    private List<Map<String, Object>> sevenCourses() {
        List<Map<String, Object>> courses = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            courses.add(map("id", "course-" + i, "name", "Course " + i));
        }
        return courses;
    }

    private List<Map<String, Object>> fourProjects() {
        List<Map<String, Object>> projects = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            projects.add(map("id", "project-" + i, "projectName", "Project " + i));
        }
        return projects;
    }

    private static Map<String, Object> map(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put((String) keyValues[i], keyValues[i + 1]);
        }
        return result;
    }
}
