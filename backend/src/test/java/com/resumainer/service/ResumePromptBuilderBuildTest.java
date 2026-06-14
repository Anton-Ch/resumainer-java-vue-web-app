package com.resumainer.service;

import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dao.ProfilePromptDao;
import com.resumainer.dao.PromptConfigDao;
import com.resumainer.model.ResumeGenerationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests the full ResumePromptBuilder.build() result object.
 *
 * ResumePromptBuilderContractTest covers only the JSON contract shape.
 * This test verifies that the rendered profile payload JSON is also exposed
 * through PromptResult so it can later be saved into ai_prompt_render_log.
 */
@ExtendWith(MockitoExtension.class)
class ResumePromptBuilderBuildTest {

    @Mock
    private PromptConfigDao promptConfigDao;

    @Mock
    private ProfilePromptDao profilePromptDao;

    @Mock
    private GenerationRequestDao generationRequestDao;

    @Mock
    private ResumeBudgetConfigService budgetConfigService;

    private ResumePromptBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ResumePromptBuilder(
                promptConfigDao,
                profilePromptDao,
                generationRequestDao,
                budgetConfigService
        );
    }

    @Test
    void build_returnsProfilePayloadJsonForRenderLogging() {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID promptConfigId = UUID.randomUUID();

        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setPromptConfigId(promptConfigId);
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        request.setIncludeCoverLetter(true);

        when(generationRequestDao.findById(requestId, userId)).thenReturn(request);

        when(promptConfigDao.getSystemPrompt(promptConfigId)).thenReturn("SYSTEM PROMPT");
        when(promptConfigDao.getLanguagePrompt(promptConfigId, "ENGLISH_ONLY")).thenReturn("LANGUAGE FRAGMENT");
        when(promptConfigDao.getAdaptationPrompt(promptConfigId, "BALANCED")).thenReturn("ADAPTATION FRAGMENT");
        when(promptConfigDao.getCoverLetterPrompt(promptConfigId, true)).thenReturn("COVER LETTER FRAGMENT");

        when(profilePromptDao.loadContact(userId)).thenReturn(map(
                "fullName", "Anton Example",
                "resumeEmail", "anton@example.com",
                "location", "Ust-Kamenogorsk"
        ));
        when(profilePromptDao.loadWorkExperience(userId)).thenReturn(List.of(map(
                "sourceId", "work-1",
                "jobTitle", "Business Analyst",
                "companyName", "Example Company"
        )));
        when(profilePromptDao.loadEducation(userId)).thenReturn(List.of(map(
                "sourceId", "education-1",
                "institutionNameEn", "Example University"
        )));
        when(profilePromptDao.loadCourses(userId)).thenReturn(List.of(map(
                "sourceId", "course-1",
                "name", "Business Analysis Foundations"
        )));
        when(profilePromptDao.loadProjects(userId)).thenReturn(List.of(map(
                "sourceId", "project-1",
                "projectName", "Resume Generator"
        )));
        when(profilePromptDao.loadAdditionalInfo(userId)).thenReturn(map(
                "skills", "Java, SQL, BPMN",
                "professionalAspirations", "Grow into a strong Java backend developer"
        ));
        when(profilePromptDao.loadWorkFormats(userId)).thenReturn(List.of(map(
                "code", "REMOTE",
                "name", "Remote"
        )));

        mockBudgetConfig();

        ResumePromptBuilder.PromptResult result = builder.build(requestId, userId);

        assertNotNull(result.profilePayload, "profilePayload must be returned for diagnostics");
        assertNotNull(result.profilePayloadJson, "profilePayloadJson must be returned for prompt render logging");
        assertFalse(result.profilePayloadJson.isBlank(), "profilePayloadJson must not be blank");

        assertTrue(result.profilePayloadJson.contains("\"contact\""), "must include contact section");
        assertTrue(result.profilePayloadJson.contains("\"additionalInfo\""), "must include additionalInfo section");
        assertTrue(result.profilePayloadJson.contains("\"professionalAspirations\""),
                "must include professionalAspirations because it is required for resume generation diagnostics");

        assertTrue(result.requestPrompt.contains(result.profilePayloadJson),
                "requestPrompt must contain exactly the same rendered profilePayloadJson");
    }

    private void mockBudgetConfig() {
        when(budgetConfigService.getSkillsGroups()).thenReturn(2);
        when(budgetConfigService.getSkillsGroupsMax()).thenReturn(4);
        when(budgetConfigService.getSkillsPerGroup()).thenReturn(3);
        when(budgetConfigService.getSkillsPerGroupMax()).thenReturn(6);
        when(budgetConfigService.getWordsPerSkillMax()).thenReturn(3);
        when(budgetConfigService.getMaxCourses()).thenReturn(5);
        when(budgetConfigService.getCourseFocusWordsMin()).thenReturn(3);
        when(budgetConfigService.getCourseFocusWordsMax()).thenReturn(8);
        when(budgetConfigService.getMaxProjects()).thenReturn(3);
        when(budgetConfigService.getProjectSentencesMin()).thenReturn(1);
        when(budgetConfigService.getProjectSentencesMax()).thenReturn(2);
    }

    private static Map<String, Object> map(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put((String) keyValues[i], keyValues[i + 1]);
        }
        return result;
    }
}
