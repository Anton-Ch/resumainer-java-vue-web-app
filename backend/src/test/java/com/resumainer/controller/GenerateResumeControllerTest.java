package com.resumainer.controller;

import com.resumainer.dao.AiModelDao;
import com.resumainer.dao.SavedResumeDao;
import com.resumainer.dto.UserSession;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.service.GenerationRequestService;
import com.resumainer.service.ResumeFinalizeService;
import com.resumainer.service.ResumeGenerationService;
import com.resumainer.service.ResumeReviewService;
import com.resumainer.service.GeneratedFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc tests for GenerateResumeController.
 * Uses mocked services — no DB required.
 */
@ExtendWith(MockitoExtension.class)
class GenerateResumeControllerTest {

    @Mock private GenerationRequestService generationRequestService;
    @Mock private ResumeGenerationService resumeGenerationService;
    @Mock private ResumeReviewService resumeReviewService;
    @Mock private ResumeFinalizeService resumeFinalizeService;
    @Mock private GeneratedFileStorageService fileStorage;
    @Mock private AiModelDao aiModelDao;
    @Mock private SavedResumeDao savedResumeDao;

    @InjectMocks
    private GenerateResumeController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAiModels_returns200() throws Exception {
        when(aiModelDao.findAvailableModels()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/generate/ai-models")
                        .sessionAttr("user", new UserSession(
                                UUID.randomUUID(), "test@test.com", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void getAiModels_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/generate/ai-models"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("auth.unauthorized"));
    }

    @Test
    void generate_validationFailure_returns422RetryableError() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        doThrow(new IllegalArgumentException(
                "AI response validation failed in EN/BALANCED: workExperience count exceeds resolved budget maximum: 16 > 10"))
                .when(resumeGenerationService).generate(requestId, userId);

        mockMvc.perform(post("/api/generate/requests/{requestId}/generate", requestId)
                        .sessionAttr("user", user))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("AI_RESPONSE_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("AI response validation failed in EN/BALANCED: workExperience count exceeds resolved budget maximum: 16 > 10"))
                .andExpect(jsonPath("$.retryAllowed").value(true))
                .andExpect(jsonPath("$.changeSettingsAllowed").value(true))
                .andExpect(jsonPath("$.requestStatus").value("failed"));
    }
}
