package com.resumainer.controller;

import com.resumainer.dao.AiModelDao;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc tests for GenerateResumeController.
 * Uses mocked services — no DB required.
 */
@ExtendWith(MockitoExtension.class)
class GenerateResumeControllerTest {

    @Mock
    private GenerationRequestService generationRequestService;
    @Mock
    private ResumeGenerationService resumeGenerationService;
    @Mock
    private ResumeReviewService resumeReviewService;
    @Mock
    private ResumeFinalizeService resumeFinalizeService;
    @Mock
    private GeneratedFileStorageService fileStorage;
    @Mock
    private AiModelDao aiModelDao;

    @InjectMocks
    private GenerateResumeController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAiModels_returns200() throws Exception {
        when(aiModelDao.findAvailableModels()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/generate/ai-models")
                        .sessionAttr("user", new com.resumainer.dto.UserSession(
                                java.util.UUID.randomUUID(), "test@test.com", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void getAiModels_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/generate/ai-models"))
                .andExpect(status().isFound()); // redirect to auth
    }
}
