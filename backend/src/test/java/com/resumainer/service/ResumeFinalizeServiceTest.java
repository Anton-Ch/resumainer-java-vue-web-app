package com.resumainer.service;

import com.resumainer.dao.*;
import com.resumainer.model.ResumeGenerationRequest;
import com.resumainer.model.ResumeGenerationResponse;
import com.resumainer.service.ai.AiClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResumeFinalizeService.
 * Focus: selected adaptation level handling for single-level requests.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ResumeFinalizeServiceTest {

    @Mock private DataSource dataSource;
    @Mock private GenerationRequestDao requestDao;
    @Mock private GenerationResponseDao responseDao;
    @Mock private GenerationResponsePersonalDao personalDao;
    @Mock private SavedResumeDao savedResumeDao;
    @Mock private ResumeTemplateRenderer templateRenderer;
    @Mock private GeneratedFileStorageService fileStorage;
    @Mock private ProfilePromptDao profilePromptDao;

    private ResumeFinalizeService service;
    private final UUID requestId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ResumeFinalizeService(
                dataSource, requestDao, responseDao, personalDao,
                savedResumeDao, templateRenderer, fileStorage, profilePromptDao);

        // Default: request exists
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setVacancyTitle("Java Developer");
        request.setCompanyName("MockTech");
        when(requestDao.findById(requestId, userId)).thenReturn(request);
    }

    // ── T6.B9: Single-level MINIMAL finalize ────────────────────────────

    @Test
    void finalizeMinimalOnly_withCorrectSelectedLevel_succeeds() {
        // Given: only MINIMAL responses exist
        ResumeGenerationResponse minimalResp = new ResumeGenerationResponse();
        minimalResp.setId(UUID.randomUUID());
        minimalResp.setLanguageId(1L); // EN
        minimalResp.setAdaptationLevelId(1L); // MINIMAL

        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(minimalResp));
        when(responseDao.loadResponseBundle(any()))
                .thenReturn(new GenerationResponseDao.ResponseBundle());

        // Mock template renderer and storage
        when(profilePromptDao.loadEducation(any())).thenReturn(Collections.emptyList());
        when(profilePromptDao.loadContact(any())).thenReturn(Collections.emptyMap());
        when(templateRenderer.renderAndSave(any(), any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("/tmp/test.html");
        when(savedResumeDao.findPublicCodeByCode(any())).thenReturn(null);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        // When: finalize with correct level MINIMAL
        var result = service.finalizeRequest(requestId, userId, "MINIMAL");

        // Then: one resume finalized
        assertNotNull(result);
        assertEquals(1, result.getResumes().size());
        assertEquals("MINIMAL", result.getResumes().get(0).getAdaptationLevel());
    }

    // ── T6.B10: Wrong selected level with single available → auto-fallback ──

    @Test
    void finalizeMinimalOnly_withWrongSelectedLevel_fallsBackToAvailable() {
        // Given: only MINIMAL responses exist
        ResumeGenerationResponse minimalResp = new ResumeGenerationResponse();
        minimalResp.setId(UUID.randomUUID());
        minimalResp.setLanguageId(1L);
        minimalResp.setAdaptationLevelId(1L); // MINIMAL

        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(minimalResp));
        when(responseDao.loadResponseBundle(any()))
                .thenReturn(new GenerationResponseDao.ResponseBundle());
        when(profilePromptDao.loadEducation(any())).thenReturn(Collections.emptyList());
        when(profilePromptDao.loadContact(any())).thenReturn(Collections.emptyMap());
        when(templateRenderer.renderAndSave(any(), any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("/tmp/test.html");
        when(savedResumeDao.findPublicCodeByCode(any())).thenReturn(null);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        // When: finalize with wrong level BALANCED, but only MINIMAL exists
        // Then: should fall back to MINIMAL (instead of failing)
        var result = service.finalizeRequest(requestId, userId, "BALANCED");

        // Should have fallen back to the only available level
        assertNotNull(result);
        assertEquals(1, result.getResumes().size());
        assertEquals("MINIMAL", result.getResumes().get(0).getAdaptationLevel());
    }

    // ── T6.B11: Invalid selected level with multiple levels → rejects ───

    @Test
    void finalizeMultipleLevels_withInvalidLevel_rejects() {
        // Given: MINIMAL, BALANCED, MAXIMUM all exist
        ResumeGenerationResponse minimalResp = createResponse(1L);
        ResumeGenerationResponse balancedResp = createResponse(2L);
        ResumeGenerationResponse maximumResp = createResponse(3L);

        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(minimalResp, balancedResp, maximumResp));

        // When: finalize with invalid level when multiple exist
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.finalizeRequest(requestId, userId, "INVALID"));

        // Then: error mentions unknown level
        assertTrue(ex.getMessage().contains("Unknown"));
    }

    @Test
    void finalizeMultipleLevels_withWrongSingleLevel_rejects() {
        // Given: MINIMAL and BALANCED exist (not MAXIMUM)
        ResumeGenerationResponse minimalResp = createResponse(1L);
        ResumeGenerationResponse balancedResp = createResponse(2L);

        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(minimalResp, balancedResp));

        // When: finalize with MAXIMUM which doesn't exist among multiple levels
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.finalizeRequest(requestId, userId, "MAXIMUM"));

        // Then: error mentions available levels
        assertTrue(ex.getMessage().contains("MAXIMUM"));
        assertTrue(ex.getMessage().contains("Available levels"));
    }

    // ── Auto-select safety net: single-level fallback ───────────────────

    @Test
    void finalizeSingleLevel_withNullSelectedLevel_shouldFallback() {
        // Given: only BALANCED exists
        ResumeGenerationResponse balancedResp = createResponse(2L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(balancedResp));
        when(responseDao.loadResponseBundle(any()))
                .thenReturn(new GenerationResponseDao.ResponseBundle());
        when(profilePromptDao.loadEducation(any())).thenReturn(Collections.emptyList());
        when(profilePromptDao.loadContact(any())).thenReturn(Collections.emptyMap());
        when(templateRenderer.renderAndSave(any(), any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("/tmp/test.html");
        when(savedResumeDao.findPublicCodeByCode(any())).thenReturn(null);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        // When: finalize with null level → should auto-select the only level
        var result = service.finalizeRequest(requestId, userId, null);

        // Then: auto-selected the single available level
        assertNotNull(result);
        assertEquals(1, result.getResumes().size());
        assertEquals("BALANCED", result.getResumes().get(0).getAdaptationLevel());
    }

    // ── Bilingual finalize ──────────────────────────────────────────

    @Test
    void finalizeRequest_bilingualBalanced_createsTwoSavedResumes() {
        ResumeGenerationResponse enResp = createResponse(1L, 2L); // EN, BALANCED
        ResumeGenerationResponse ruResp = createResponse(2L, 2L); // RU, BALANCED

        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(enResp, ruResp));
        when(responseDao.loadResponseBundle(any()))
                .thenReturn(new GenerationResponseDao.ResponseBundle());
        when(profilePromptDao.loadEducation(any())).thenReturn(Collections.emptyList());
        when(profilePromptDao.loadContact(any())).thenReturn(Collections.emptyMap());
        when(templateRenderer.renderAndSave(any(), any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("/tmp/test.html");
        when(savedResumeDao.findPublicCodeByCode(any())).thenReturn(null);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        var result = service.finalizeRequest(requestId, userId, "BALANCED");

        assertNotNull(result);
        assertEquals(2, result.getResumes().size());
        // One EN, one RU
        assertTrue(result.getResumes().stream().anyMatch(r -> "EN".equals(r.getLanguageCode())));
        assertTrue(result.getResumes().stream().anyMatch(r -> "RU".equals(r.getLanguageCode())));
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private ResumeGenerationResponse createResponse(long adaptationLevelId) {
        return createResponse(1L, adaptationLevelId);
    }

    private ResumeGenerationResponse createResponse(long languageId, long adaptationLevelId) {
        ResumeGenerationResponse resp = new ResumeGenerationResponse();
        resp.setId(UUID.randomUUID());
        resp.setLanguageId(languageId);
        resp.setAdaptationLevelId(adaptationLevelId);
        return resp;
    }
}
