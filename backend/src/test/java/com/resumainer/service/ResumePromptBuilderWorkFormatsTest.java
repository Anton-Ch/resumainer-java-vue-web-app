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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Verifies that normalized work formats are rendered into the prompt payload
 * with localized English/Russian display values.
 */
@ExtendWith(MockitoExtension.class)
class ResumePromptBuilderWorkFormatsTest {

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
        when(promptConfigDao.getLanguagePrompt(promptConfigId, "BILINGUAL"))
                .thenReturn("Language mode: BILINGUAL. For personalInfo.workFormats, use workFormats.english for English output and workFormats.russian for Russian output.");
        when(promptConfigDao.getAdaptationPrompt(promptConfigId, "MINIMAL")).thenReturn("ADAPTATION FRAGMENT");
        when(promptConfigDao.getCoverLetterPrompt(promptConfigId, false)).thenReturn("COVER LETTER DISABLED");

        when(profilePromptDao.loadContact(userId)).thenReturn(map(
                "fullName", "Anton Example",
                "resumeEmail", "anton@example.com",
                "location", "Astana"
        ));
        when(profilePromptDao.loadWorkExperience(userId)).thenReturn(List.of(map(
                "id", "work-1",
                "jobTitle", "Business Analyst",
                "companyName", "Example Company"
        )));
        when(profilePromptDao.loadEducation(userId)).thenReturn(List.of());
        when(profilePromptDao.loadCourses(userId)).thenReturn(List.of());
        when(profilePromptDao.loadProjects(userId)).thenReturn(List.of());
        when(profilePromptDao.loadAdditionalInfo(userId)).thenReturn(map(
                "skills", "Java, SQL, BPMN"
        ));
        when(profilePromptDao.loadWorkFormats(userId)).thenReturn(List.of(
                map("code", "full-time", "name", "Full-time"),
                map("code", "remote", "name", "Remote"),
                map("code", "hybrid", "name", "Hybrid"),
                map("code", "on_project_site", "name", "On-site project based")
        ));

        mockBudgetConfig();
    }

    @Test
    void build_rendersLocalizedWorkFormatsForEnglishAndRussianPromptOutput() {
        ResumeGenerationRequest request = baseRequest();
        when(generationRequestDao.findById(requestId, userId)).thenReturn(request);

        ResumePromptBuilder.PromptResult result = builder.build(requestId, userId);

        assertTrue(result.profilePayloadJson.contains("\"workFormats\""),
                "profilePayloadJson must contain workFormats section");
        assertTrue(result.profilePayloadJson.contains("\"english\""),
                "workFormats must expose English display names");
        assertTrue(result.profilePayloadJson.contains("\"russian\""),
                "workFormats must expose Russian display names");

        assertTrue(result.profilePayloadJson.contains("\"Full-time\""));
        assertTrue(result.profilePayloadJson.contains("\"Remote\""));
        assertTrue(result.profilePayloadJson.contains("\"Hybrid\""));
        assertTrue(result.profilePayloadJson.contains("\"On-site project based\""));

        assertTrue(result.profilePayloadJson.contains("\"Полная занятость\""));
        assertTrue(result.profilePayloadJson.contains("\"Удалённо\""));
        assertTrue(result.profilePayloadJson.contains("\"Гибрид\""));
        assertTrue(result.profilePayloadJson.contains("\"На проектной площадке\""));

        assertTrue(result.requestPrompt.contains("workFormats.english"),
                "requestPrompt must explicitly point the model to English work format values");
        assertTrue(result.requestPrompt.contains("workFormats.russian"),
                "requestPrompt must explicitly point the model to Russian work format values");
    }

    private ResumeGenerationRequest baseRequest() {
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setPromptConfigId(promptConfigId);
        request.setLanguageMode("BILINGUAL");
        request.setAdaptationSelection("MINIMAL");
        request.setIncludeCoverLetter(false);
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

    private static Map<String, Object> map(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put((String) keyValues[i], keyValues[i + 1]);
        }
        return result;
    }
}
