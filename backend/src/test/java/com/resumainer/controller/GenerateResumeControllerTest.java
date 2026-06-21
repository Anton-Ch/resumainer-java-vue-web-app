package com.resumainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dao.AiModelDao;
import com.resumainer.dao.SavedResumeDao;
import com.resumainer.dto.UserSession;
import com.resumainer.dto.generate.ExportResultDto;
import com.resumainer.dto.generate.GenerationReviewDto;
import com.resumainer.dto.generate.SavedResumeExportDto;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.model.ResumeGenerationRequest;
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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
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
    void getAiModels_privileged_usesPrivilegedDao() throws Exception {
        UUID userId = UUID.randomUUID();
        // isPrivileged() == true — uses findAvailableModelsPrivileged
        UserSession privilegedUser = new UserSession(userId, "privileged@test.com", "USER", true);
        when(aiModelDao.findAvailableModelsPrivileged()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/generate/ai-models")
                        .sessionAttr("user", privilegedUser))
                .andExpect(status().isOk());

        verify(aiModelDao).findAvailableModelsPrivileged();
        verify(aiModelDao, never()).findAvailableModels();
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

    @Test
    void generate_whenAlreadyInProgress_returns409ConflictNot500() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        doThrow(new com.resumainer.service.ai.AiClientException(
                "Generation already in progress. Please wait for it to complete.",
                "GENERATION_ALREADY_IN_PROGRESS"))
                .when(resumeGenerationService).generate(requestId, userId);

        mockMvc.perform(post("/api/generate/requests/{requestId}/generate", requestId)
                        .sessionAttr("user", user))
                .andExpect(status().isConflict())  // 409, not 500
                .andExpect(jsonPath("$.errorCode").value("GENERATION_ALREADY_IN_PROGRESS"))
                .andExpect(jsonPath("$.message").value("Generation already in progress. Please wait for it to complete."))
                .andExpect(jsonPath("$.retryAllowed").value(false))
                .andExpect(jsonPath("$.changeSettingsAllowed").value(false))
                .andExpect(jsonPath("$.requestStatus").value("failed"));
    }

    @Test
    void generate_whenAiProviderFails_stillReturns500GenericError() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        // Real AI provider failure — not GENERATION_ALREADY_IN_PROGRESS
        doThrow(new com.resumainer.service.ai.AiClientException(
                "AI model returned an error",
                "AI_PROVIDER_ERROR"))
                .when(resumeGenerationService).generate(requestId, userId);

        mockMvc.perform(post("/api/generate/requests/{requestId}/generate", requestId)
                        .sessionAttr("user", user))
                .andExpect(status().isInternalServerError())  // 500 for real AI failures
                .andExpect(jsonPath("$.errorCode").value("AI_PROVIDER_ERROR"))
                .andExpect(jsonPath("$.retryAllowed").value(true))
                .andExpect(jsonPath("$.changeSettingsAllowed").value(true));
    }

    @Test
    void generate_nonAiIllegalArgument_returns404() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        // IllegalArgumentException that is NOT an AI response validation failure
        doThrow(new IllegalArgumentException("Request not found: " + requestId))
                .when(resumeGenerationService).generate(requestId, userId);

        mockMvc.perform(post("/api/generate/requests/{requestId}/generate", requestId)
                        .sessionAttr("user", user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("REQUEST_NOT_FOUND"));
    }

    @Test
    void generate_genericException_returns500() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        // Generic Exception fallback
        doThrow(new RuntimeException("Unexpected error"))
                .when(resumeGenerationService).generate(requestId, userId);

        mockMvc.perform(post("/api/generate/requests/{requestId}/generate", requestId)
                        .sessionAttr("user", user))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("GENERATION_FAILED"))
                .andExpect(jsonPath("$.retryAllowed").value(true))
                .andExpect(jsonPath("$.changeSettingsAllowed").value(true));
    }

    // ============================================================
    // Security: endpoints require authentication
    // ============================================================

    @Test
    void anyEndpoint_withoutSession_returns401() throws Exception {
        UUID requestId = UUID.randomUUID();
        mockMvc.perform(get("/api/generate/requests/{requestId}/review", requestId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("auth.unauthorized"));
    }

    // ============================================================
    // createRequest (POST /api/generate/requests)
    // ============================================================

    @Test
    void createRequest_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");
        UUID requestId = UUID.randomUUID();

        ResumeGenerationRequest created = new ResumeGenerationRequest();
        created.setId(requestId);
        when(generationRequestService.createRequest(eq(userId), any())).thenReturn(created);

        String json = objectMapper.writeValueAsString(Map.of(
                "vacancyTitle", "Software Engineer",
                "vacancyDescription", "Build amazing software",
                "languageMode", "ENGLISH_ONLY",
                "adaptationSelection", "BALANCED",
                "includeCoverLetter", false
        ));

        mockMvc.perform(post("/api/generate/requests")
                        .sessionAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    // ============================================================
    // updateSettings (PUT /api/generate/requests/{id}/settings)
    // ============================================================

    @Test
    void updateSettings_success() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        doNothing().when(generationRequestService).updateSettings(eq(requestId), eq(userId),
                any(), any(), any(), anyBoolean());

        String json = objectMapper.writeValueAsString(Map.of(
                "languageMode", "ENGLISH_ONLY",
                "adaptationSelection", "BALANCED"
        ));

        mockMvc.perform(put("/api/generate/requests/{requestId}/settings", requestId)
                        .sessionAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateSettings_badRequest() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        doThrow(new IllegalArgumentException("Invalid settings"))
                .when(generationRequestService).updateSettings(eq(requestId), eq(userId),
                any(), any(), any(), anyBoolean());

        String json = objectMapper.writeValueAsString(Map.of(
                "languageMode", "INVALID"
        ));

        mockMvc.perform(put("/api/generate/requests/{requestId}/settings", requestId)
                        .sessionAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid settings"));
    }

    // ============================================================
    // getReview (GET /api/generate/requests/{id}/review)
    // ============================================================

    @Test
    void getReview_returns200() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        GenerationReviewDto review = new GenerationReviewDto();
        review.setRequestId(requestId);
        when(resumeReviewService.getReview(requestId, userId)).thenReturn(review);

        mockMvc.perform(get("/api/generate/requests/{requestId}/review", requestId)
                        .sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId.toString()));
    }

    // ============================================================
    // saveReview (PUT /api/generate/requests/{id}/review)
    // ============================================================

    @Test
    void saveReview_returns200() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        doNothing().when(resumeReviewService).saveReview(eq(requestId), eq(userId), any(), any());

        String json = objectMapper.writeValueAsString(Map.of(
                "fieldUpdates", Map.of(
                        "work_experience:abc123:jobTitle:BALANCED", "Updated title"
                )
        ));

        mockMvc.perform(put("/api/generate/requests/{requestId}/review", requestId)
                        .sessionAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ============================================================
    // finalizeRequest (POST /api/generate/requests/{id}/finalize)
    // ============================================================

    @Test
    void finalizeRequest_success() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        ExportResultDto export = new ExportResultDto();
        when(resumeFinalizeService.finalizeRequest(requestId, userId, "BALANCED")).thenReturn(export);

        String json = objectMapper.writeValueAsString(Map.of(
                "selectedAdaptationLevel", "BALANCED"
        ));

        mockMvc.perform(post("/api/generate/requests/{requestId}/finalize", requestId)
                        .sessionAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void finalizeRequest_badRequest() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        when(resumeFinalizeService.finalizeRequest(requestId, userId, "INVALID"))
                .thenThrow(new IllegalArgumentException("Invalid adaptation level"));

        String json = objectMapper.writeValueAsString(Map.of(
                "selectedAdaptationLevel", "INVALID"
        ));

        mockMvc.perform(post("/api/generate/requests/{requestId}/finalize", requestId)
                        .sessionAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid adaptation level"));
    }

    @Test
    void finalizeRequest_internalError() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        when(resumeFinalizeService.finalizeRequest(requestId, userId, "BALANCED"))
                .thenThrow(new RuntimeException("DB connection lost"));

        String json = objectMapper.writeValueAsString(Map.of(
                "selectedAdaptationLevel", "BALANCED"
        ));

        mockMvc.perform(post("/api/generate/requests/{requestId}/finalize", requestId)
                        .sessionAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to finalize resume. Please try again."));
    }

    @Test
    void finalizeRequest_finalizationAlreadyInProgress_returns409() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        when(resumeFinalizeService.finalizeRequest(requestId, userId, "BALANCED"))
                .thenThrow(new RuntimeException("Finalization already in progress. Please wait for it to complete."));

        String json = objectMapper.writeValueAsString(Map.of(
                "selectedAdaptationLevel", "BALANCED"
        ));

        mockMvc.perform(post("/api/generate/requests/{requestId}/finalize", requestId)
                        .sessionAttr("user", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Finalization already in progress. Please wait for it to complete."));
    }

    // ============================================================
    // getExport (GET /api/generate/requests/{id}/export)
    // ============================================================

    @Test
    void getExport_returns200() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        SavedResumeDao.SavedResumeRow row = new SavedResumeDao.SavedResumeRow();
        row.id = 1L;
        row.language = "EN";
        row.adaptationLevel = "BALANCED";
        row.publicCode = "abc123";
        row.coverLetter = "Dear hiring manager...";
        when(savedResumeDao.findByGenerationRequestId(requestId, userId))
                .thenReturn(List.of(row));

        mockMvc.perform(get("/api/generate/requests/{requestId}/export", requestId)
                        .sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumes[0].languageCode").value("EN"))
                .andExpect(jsonPath("$.resumes[0].htmlDownloadUrl").value("/api/generate/resumes/1/html"))
                .andExpect(jsonPath("$.resumes[0].coverLetter").value("Dear hiring manager..."));
    }

    @Test
    void getExport_notFound_returns404() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        when(savedResumeDao.findByGenerationRequestId(requestId, userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/generate/requests/{requestId}/export", requestId)
                        .sessionAttr("user", user))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // downloadHtml (GET /api/generate/resumes/{id}/html)
    // ============================================================

    @Test
    void downloadHtml_notFound_returns404() throws Exception {
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        when(savedResumeDao.findById(999L, userId)).thenReturn(null);

        mockMvc.perform(get("/api/generate/resumes/999/html")
                        .sessionAttr("user", user))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadHtml_success_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        // Create a real temp file so FileSystemResource.exists() returns true
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-resume", ".html");
        tempFile.toFile().deleteOnExit();

        SavedResumeDao.SavedResumeRow row = new SavedResumeDao.SavedResumeRow();
        row.id = 1L;
        row.htmlFilePath = "/path/to/file.html";
        when(savedResumeDao.findById(1L, userId)).thenReturn(row);
        when(fileStorage.resolveSafePath("/path/to/file.html")).thenReturn(tempFile);

        mockMvc.perform(get("/api/generate/resumes/1/html")
                        .sessionAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"resume-1.html\""));
    }

    @Test
    void downloadHtml_internalError_returns500() throws Exception {
        UUID userId = UUID.randomUUID();
        UserSession user = new UserSession(userId, "test@test.com", "USER");

        SavedResumeDao.SavedResumeRow row = new SavedResumeDao.SavedResumeRow();
        row.id = 1L;
        row.htmlFilePath = "/path/to/file.html";
        when(savedResumeDao.findById(1L, userId)).thenReturn(row);
        when(fileStorage.resolveSafePath(anyString())).thenThrow(new RuntimeException("Storage error"));

        mockMvc.perform(get("/api/generate/resumes/1/html")
                        .sessionAttr("user", user))
                .andExpect(status().isInternalServerError());
    }

    // ============================================================
    // downloadPdf (GET /api/generate/resumes/{id}/pdf) — placeholder
    // ============================================================

    // Feature 008: PDF download and public route — tested via integration/smoke tests
}
