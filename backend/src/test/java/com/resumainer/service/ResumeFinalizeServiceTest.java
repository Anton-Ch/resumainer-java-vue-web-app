package com.resumainer.service;

import com.resumainer.dao.*;
import com.resumainer.model.ResumeGenerationRequest;
import com.resumainer.model.ResumeGenerationResponse;
import com.resumainer.model.User;
import com.resumainer.model.pdf.PagePlan;
import com.resumainer.model.pdf.ResumeRenderData;
import com.resumainer.service.pdf.PagePlanBuilder;
import com.resumainer.service.pdf.ResumeRenderDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResumeFinalizeService.
 * Covers adaptation level selection and Phase 22A finalization behavior.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ResumeFinalizeServiceTest {

    @Mock private DataSource dataSource;
    @Mock private GenerationRequestDao requestDao;
    @Mock private GenerationResponseDao responseDao;
    @Mock private SavedResumeDao savedResumeDao;
    @Mock private GeneratedFileStorageService fileStorage;
    @Mock private ProfilePromptDao profilePromptDao;
    @Mock private UserDao userDao;
    @Mock private OpenHtmlPdfGenerationService pdfGenerationService;
    @Mock private ResumeRenderDataBuilder renderDataBuilder;
    @Mock private PagePlanBuilder pagePlanBuilder;
    @Mock private Connection mockConnection;

    private ResumeFinalizeService service;
    private final UUID requestId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() throws Exception {
        service = new ResumeFinalizeService(
                dataSource, requestDao, responseDao,
                savedResumeDao, fileStorage, profilePromptDao, userDao,
                pdfGenerationService, renderDataBuilder, pagePlanBuilder);

        // Default: transaction connection mock
        when(dataSource.getConnection()).thenReturn(mockConnection);

        // Default: connection-aware insert succeeds (Phase 22C)
        when(savedResumeDao.insert(any(Connection.class), any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        // Default: request exists with completed status
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setVacancyTitle("Java Developer");
        request.setCompanyName("MockTech");
        request.setStatus("completed");
        when(requestDao.findById(requestId, userId)).thenReturn(request);

        // Default: finalization lock acquired
        when(requestDao.tryMarkFinalizing(requestId, userId)).thenReturn(true);

        // Default: username lookup
        User user = new User();
        user.setUsername(TEST_USERNAME);
        when(userDao.findById(userId)).thenReturn(user);

        // Default: render data builder returns empty data
        when(renderDataBuilder.buildRenderData(any())).thenReturn(new ResumeRenderData());

        // Default: page plan builder returns 1-page plan
        PagePlan pagePlan = new PagePlan();
        pagePlan.setTargetPageCount(1);
        pagePlan.setPage1WorkCount(0);
        when(pagePlanBuilder.build(anyInt(), anyInt(), anyInt())).thenReturn(pagePlan);

        // Default: profile data
        when(profilePromptDao.loadEducation(any())).thenReturn(Collections.emptyList());
        when(profilePromptDao.loadContact(any())).thenReturn(Collections.emptyMap());

        // Default: response bundle with populated response for buildRenderData
        GenerationResponseDao.ResponseBundle defaultBundle = new GenerationResponseDao.ResponseBundle();
        ResumeGenerationResponse defaultResp = new ResumeGenerationResponse();
        defaultResp.setId(UUID.randomUUID());
        defaultResp.setLanguageId(1L);
        defaultResp.setAdaptationLevelId(1L);
        defaultBundle.response = defaultResp;
        when(responseDao.loadResponseBundle(any()))
                .thenReturn(defaultBundle);

        // Default: public code uniqueness
        when(savedResumeDao.findPublicCodeByCode(any())).thenReturn(null);
    }

    // ── Phase 22A Test 1: PDF parity HTML is saved (not legacy HTML) ──

    @Test
    void finalizeRequest_success_usesPdfParityHtmlPath() throws Exception {
        // Given: one EN MINIMAL response
        ResumeGenerationResponse resp = createResponse(1L, 1L); // EN, MINIMAL
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(resp));

        // Given: PDF generation succeeds with PDF-parity HTML path
        String parityHtmlPath = "generated_results/testuser/ABC123/resume_en.html";
        String pdfPath = "generated_results/testuser/ABC123/resume_en.pdf";
        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        parityHtmlPath, pdfPath, 1, null);
        when(pdfGenerationService.generate(any(ResumeRenderData.class), any(PagePlan.class),
                any(), anyString())).thenReturn(pdfResult);

        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        // When
        var result = service.finalizeRequest(requestId, userId, "MINIMAL");

        // Then: saved_resume uses PDF-parity HTML path, not legacy renderer path
        assertNotNull(result);
        assertEquals(1, result.getResumes().size());

        // Verify insert received the PDF-parity HTML path (position 10 = htmlFilePath with Connection arg)
        verify(savedResumeDao).insert((Connection) any(), eq(userId), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                eq(parityHtmlPath), eq(pdfPath), nullable(String.class), any(), any(), anyLong(), anyLong());

        // Verify PDF metadata was updated
        verify(savedResumeDao).updatePdfMetadata((Connection) any(), eq(1L), eq("READY"), eq(pdfPath),
                eq(1), eq("default-v1"), (String) any(), (String) any());
    }

    // ── Phase 22A Test 2: Legacy renderer is NOT called ──

    @Test
    void finalizeRequest_success_doesNotCallLegacyRenderer() {
        // Given: one EN MINIMAL response
        ResumeGenerationResponse resp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(resp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenReturn(pdfResult);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        // When
        service.finalizeRequest(requestId, userId, "MINIMAL");

        // Then: pdfGenerationService.generate() was called (not legacy renderer)
        verify(pdfGenerationService, atLeastOnce())
                .generate(any(), any(), any(), anyString());
    }

    // ── Phase 22A Test 3: PDF failure does NOT save HTML-only resume ──

    @Test
    void finalizeRequest_pdfFailure_doesNotInsertSavedResume() {
        // Given: one EN MINIMAL response
        ResumeGenerationResponse resp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(resp));

        // Given: PDF generation fails
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenThrow(new RuntimeException("PDF generation failed"));

        // When/Then: finalization throws
        assertThrows(RuntimeException.class, () ->
                service.finalizeRequest(requestId, userId, "MINIMAL"));

        // Then: no saved_resume was inserted
        verify(savedResumeDao, never()).insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong());

        // Then: no PDF metadata was updated
        verify(savedResumeDao, never()).updatePdfMetadata(anyLong(), anyString(), anyString(),
                any(), anyString(), anyString(), anyString());
    }

    @Test
    void finalizeRequest_pdfFittingFailure_doesNotInsertSavedResume() {
        // Given: one EN MINIMAL response
        ResumeGenerationResponse resp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(resp));

        // Given: PDF generation returns failure (fitting exhausted)
        OpenHtmlPdfGenerationService.PdfGenerationResult failResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.failure("Fitting exhausted");
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenReturn(failResult);

        // When/Then: finalization throws
        assertThrows(RuntimeException.class, () ->
                service.finalizeRequest(requestId, userId, "MINIMAL"));

        // Then: no saved_resume was inserted
        verify(savedResumeDao, never()).insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong());

        // Then: no PDF metadata was updated
        verify(savedResumeDao, never()).updatePdfMetadata(anyLong(), anyString(), anyString(),
                any(), anyString(), anyString(), anyString());
    }

    // ── Phase 22A Test 4: Response URLs no longer use /candidate/ ──

    @Test
    void finalizeRequest_success_responseUrlsDoNotContainCandidate() {
        // Given: one EN MINIMAL response
        ResumeGenerationResponse resp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(resp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenReturn(pdfResult);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        // When
        var result = service.finalizeRequest(requestId, userId, "MINIMAL");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getResumes().size());
        var item = result.getResumes().get(0);

        // publicUrlLink must use /{username}/{publicCode}, not /candidate/
        assertFalse(item.getPublicUrlLink().contains("/candidate/"),
                "publicUrlLink must not contain /candidate/");
        assertTrue(item.getPublicUrlLink().startsWith("/" + TEST_USERNAME + "/"),
                "publicUrlLink must start with /username/");

        // pdfOpenUrl must use authenticated route, not /candidate/
        assertFalse(item.getPdfOpenUrl().contains("/candidate/"),
                "pdfOpenUrl must not contain /candidate/");
        assertTrue(item.getPdfOpenUrl().contains("/api/generate/resumes/"),
                "pdfOpenUrl must use /api/generate/resumes/ path");
        assertTrue(item.getPdfOpenUrl().contains("disposition=inline"),
                "pdfOpenUrl must include disposition=inline");

        // htmlDownloadUrl must use /api/generate/resumes/ path
        assertTrue(item.getHtmlDownloadUrl().contains("/api/generate/resumes/"),
                "htmlDownloadUrl must use /api/generate/resumes/ path");

        // pdfDownloadUrl must use /api/generate/resumes/ path
        assertTrue(item.getPdfDownloadUrl().contains("/api/generate/resumes/"),
                "pdfDownloadUrl must use /api/generate/resumes/ path");

        // pdfAvailable must be true when PDF succeeded
        assertTrue(item.isPdfAvailable(), "pdfAvailable must be true");
    }

    // ── Existing tests: adaptation level selection (updated for new constructor) ──

    @Test
    void finalizeMinimalOnly_withCorrectSelectedLevel_succeeds() {
        ResumeGenerationResponse minimalResp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(minimalResp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenReturn(pdfResult);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        var result = service.finalizeRequest(requestId, userId, "MINIMAL");

        assertNotNull(result);
        assertEquals(1, result.getResumes().size());
        assertEquals("MINIMAL", result.getResumes().get(0).getAdaptationLevel());
    }

    @Test
    void finalizeMinimalOnly_withWrongSelectedLevel_fallsBackToAvailable() {
        ResumeGenerationResponse minimalResp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(minimalResp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenReturn(pdfResult);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        var result = service.finalizeRequest(requestId, userId, "BALANCED");

        assertNotNull(result);
        assertEquals(1, result.getResumes().size());
        assertEquals("MINIMAL", result.getResumes().get(0).getAdaptationLevel());
    }

    @Test
    void finalizeMultipleLevels_withInvalidLevel_rejects() {
        ResumeGenerationResponse minimalResp = createResponse(1L);
        ResumeGenerationResponse balancedResp = createResponse(2L);
        ResumeGenerationResponse maximumResp = createResponse(3L);

        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(minimalResp, balancedResp, maximumResp));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.finalizeRequest(requestId, userId, "INVALID"));

        assertTrue(ex.getMessage().contains("Unknown"));
    }

    @Test
    void finalizeMultipleLevels_withWrongSingleLevel_rejects() {
        ResumeGenerationResponse minimalResp = createResponse(1L);
        ResumeGenerationResponse balancedResp = createResponse(2L);

        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(minimalResp, balancedResp));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.finalizeRequest(requestId, userId, "MAXIMUM"));

        assertTrue(ex.getMessage().contains("MAXIMUM"));
        assertTrue(ex.getMessage().contains("Available levels"));
    }

    @Test
    void finalizeSingleLevel_withNullSelectedLevel_shouldFallback() {
        ResumeGenerationResponse balancedResp = createResponse(2L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(balancedResp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenReturn(pdfResult);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        var result = service.finalizeRequest(requestId, userId, null);

        assertNotNull(result);
        assertEquals(1, result.getResumes().size());
        assertEquals("BALANCED", result.getResumes().get(0).getAdaptationLevel());
    }

    @Test
    void finalizeRequest_bilingualBalanced_createsTwoSavedResumes() {
        ResumeGenerationResponse enResp = createResponse(1L, 2L); // EN, BALANCED
        ResumeGenerationResponse ruResp = createResponse(2L, 2L); // RU, BALANCED

        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(enResp, ruResp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenReturn(pdfResult);
        when(savedResumeDao.insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        var result = service.finalizeRequest(requestId, userId, "BALANCED");

        assertNotNull(result);
        assertEquals(2, result.getResumes().size());
        assertTrue(result.getResumes().stream().anyMatch(r -> "EN".equals(r.getLanguageCode())));
        assertTrue(result.getResumes().stream().anyMatch(r -> "RU".equals(r.getLanguageCode())));
    }

    // ── Phase 22B Test A: Success status lifecycle ──────────────────────

    @Test
    void finalizeRequest_success_acquiresLockBeforeGenerateAndRestoresStatus() throws Exception {
        ResumeGenerationResponse resp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(resp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenReturn(pdfResult);
        when(savedResumeDao.insert(any(Connection.class), any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        service.finalizeRequest(requestId, userId, "MINIMAL");

        // Verify ordering: lock before generate
        InOrder order = inOrder(requestDao, pdfGenerationService);
        order.verify(requestDao).tryMarkFinalizing(requestId, userId);
        order.verify(pdfGenerationService).generate(any(), any(), any(), anyString());

        // Verify status restored to completed inside transaction
        verify(requestDao).updateStatus((Connection) any(), eq(requestId), eq(userId),
                eq("completed"), isNull(), eq(false));
    }

    // ── Phase 22B Test B: Already finalizing prevents all work ───────────

    @Test
    void finalizeRequest_alreadyFinalizing_preventsAllWork() {
        // Override default: request already finalizing
        ResumeGenerationRequest finalizingRequest = new ResumeGenerationRequest();
        finalizingRequest.setId(requestId);
        finalizingRequest.setUserId(userId);
        finalizingRequest.setStatus("finalizing");
        when(requestDao.findById(requestId, userId)).thenReturn(finalizingRequest);

        Exception ex = assertThrows(RuntimeException.class, () ->
                service.finalizeRequest(requestId, userId, "MINIMAL"));

        assertTrue(ex.getMessage().contains("Finalization already in progress"));

        // Verify no PDF generation, no insert, no tryMarkFinalizing
        verify(pdfGenerationService, never()).generate(any(), any(), any(), anyString());
        verify(savedResumeDao, never()).insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong());
        verify(savedResumeDao, never()).updatePdfMetadata(anyLong(), anyString(), anyString(),
                any(), anyString(), anyString(), anyString());
    }

    // ── Phase 22B Test C: Lock not acquired prevents all work ────────────

    @Test
    void finalizeRequest_lockNotAcquired_concurrentFinalizing_preventsWork() {
        // Override: first findById returns completed, tryMarkFinalizing fails
        when(requestDao.tryMarkFinalizing(requestId, userId)).thenReturn(false);
        // Reloaded request shows finalizing (concurrent finalization won)
        ResumeGenerationRequest reloaded = new ResumeGenerationRequest();
        reloaded.setId(requestId);
        reloaded.setUserId(userId);
        reloaded.setStatus("finalizing");
        when(requestDao.findById(requestId, userId))
                .thenReturn(newResumeGenerationRequest("completed")) // first call
                .thenReturn(reloaded); // reload after lock fails

        Exception ex = assertThrows(RuntimeException.class, () ->
                service.finalizeRequest(requestId, userId, "MINIMAL"));

        assertTrue(ex.getMessage().contains("Finalization already in progress"));

        verify(pdfGenerationService, never()).generate(any(), any(), any(), anyString());
        verify(savedResumeDao, never()).insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong());
    }

    // ── Phase 22B Test D: PDF failure restores status ────────────────────

    @Test
    void finalizeRequest_pdfFailure_restoresStatus() {
        ResumeGenerationResponse resp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(resp));

        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenThrow(new RuntimeException("PDF engine crashed"));

        assertThrows(RuntimeException.class, () ->
                service.finalizeRequest(requestId, userId, "MINIMAL"));

        // Verify status was restored
        verify(requestDao).updateStatus(requestId, userId, "completed", null, false);
        // Verify no insert
        verify(savedResumeDao, never()).insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong());
    }

    // ── Phase 22B Test E: Fitting failure restores status ────────────────

    @Test
    void finalizeRequest_fittingFailure_restoresStatus() {
        ResumeGenerationResponse resp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId))
                .thenReturn(List.of(resp));

        OpenHtmlPdfGenerationService.PdfGenerationResult failResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.failure("Fitting exhausted");
        when(pdfGenerationService.generate(any(), any(), any(), anyString()))
                .thenReturn(failResult);

        assertThrows(RuntimeException.class, () ->
                service.finalizeRequest(requestId, userId, "MINIMAL"));

        // Verify status was restored
        verify(requestDao).updateStatus(requestId, userId, "completed", null, false);
        // Verify no insert
        verify(savedResumeDao, never()).insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong());
    }

    // ── Phase 22C Test 1: Single transaction success ──────────────────────

    @Test
    void finalizeRequest_success_persistsAllSavedResumesInSingleTransaction() throws Exception {
        // Override dataSource to return a mock connection for the DB transaction
        when(dataSource.getConnection()).thenReturn(mockConnection);

        ResumeGenerationResponse resp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(resp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString())).thenReturn(pdfResult);
        when(savedResumeDao.insert(eq(mockConnection), any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);

        service.finalizeRequest(requestId, userId, "MINIMAL");

        // Verify transaction lifecycle
        verify(dataSource).getConnection(); // only once for DB writes
        verify(mockConnection).setAutoCommit(false);
        verify(savedResumeDao).insert((Connection) any(), any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong());
        verify(savedResumeDao).updatePdfMetadata((Connection) any(), anyLong(), anyString(), anyString(),
                (Integer) any(), (String) any(), (String) any(), (String) any());
        verify(requestDao).updateStatus((Connection) eq(mockConnection), eq(requestId), eq(userId),
                eq("completed"), isNull(), eq(false));
        verify(mockConnection).commit();
        verify(mockConnection, never()).rollback();
    }

    // ── Phase 22C Test 2: Second insert fails → rollback ──────────────────

    @Test
    void finalizeRequest_whenSecondSavedResumeInsertFails_rollsBack() throws Exception {
        when(dataSource.getConnection()).thenReturn(mockConnection);

        // Bilingual: EN and RU
        ResumeGenerationResponse enResp = createResponse(1L, 2L); // EN, BALANCED
        ResumeGenerationResponse ruResp = createResponse(2L, 2L); // RU, BALANCED
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(enResp, ruResp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString())).thenReturn(pdfResult);

        // First insert succeeds, second fails
        when(savedResumeDao.insert(eq(mockConnection), any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L)
                .thenThrow(new RuntimeException("DB insert failed"));

        assertThrows(RuntimeException.class, () ->
                service.finalizeRequest(requestId, userId, "BALANCED"));

        // Verify rollback
        verify(mockConnection).rollback();
        verify(mockConnection, never()).commit();
        // Best-effort status reset
        verify(requestDao).updateStatus(eq(requestId), eq(userId), eq("completed"), isNull(), eq(false));
    }

    // ── Phase 22C Test 3: Metadata update fails → rollback ────────────────

    @Test
    void finalizeRequest_whenPdfMetadataUpdateFails_rollsBack() throws Exception {
        when(dataSource.getConnection()).thenReturn(mockConnection);

        ResumeGenerationResponse resp = createResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(resp));

        OpenHtmlPdfGenerationService.PdfGenerationResult pdfResult =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/parity.html", "/tmp/parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), anyString())).thenReturn(pdfResult);
        when(savedResumeDao.insert(eq(mockConnection), any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(1L);
        doThrow(new RuntimeException("Metadata update failed"))
                .when(savedResumeDao).updatePdfMetadata((Connection) any(), anyLong(), anyString(),
                        anyString(), (Integer) any(), (String) any(), (String) any(), (String) any());

        assertThrows(RuntimeException.class, () ->
                service.finalizeRequest(requestId, userId, "MINIMAL"));

        verify(mockConnection).rollback();
        verify(mockConnection, never()).commit();
        verify(requestDao).updateStatus(eq(requestId), eq(userId), eq("completed"), isNull(), eq(false));
    }

    // ── Phase 22C Test 4: Bilingual second language artifact fails before DB ─

    @Test
    void finalizeRequest_bilingualWhenSecondLanguagePdfGenerationFails_doesNotOpenDbTransaction() throws Exception {
        // Bilingual: EN succeeds, RU fails
        ResumeGenerationResponse enResp = createResponse(1L, 2L);
        ResumeGenerationResponse ruResp = createResponse(2L, 2L);
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(enResp, ruResp));

        // First generate succeeds, second fails
        OpenHtmlPdfGenerationService.PdfGenerationResult success =
                OpenHtmlPdfGenerationService.PdfGenerationResult.success(
                        "/tmp/en_parity.html", "/tmp/en_parity.pdf", 1, null);
        when(pdfGenerationService.generate(any(), any(), any(), eq("resume_en")))
                .thenReturn(success);
        when(pdfGenerationService.generate(any(), any(), any(), eq("resume_ru")))
                .thenThrow(new RuntimeException("RU PDF generation failed"));

        assertThrows(RuntimeException.class, () ->
                service.finalizeRequest(requestId, userId, "BALANCED"));

        // No DB transaction was opened
        verify(dataSource, never()).getConnection();
        // No insert was called
        verify(savedResumeDao, never()).insert(any(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong());
        verify(savedResumeDao, never()).insert(eq(mockConnection), any(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), any(), anyLong(), anyLong());
        // Status reset attempted (best-effort)
        verify(requestDao).updateStatus(eq(requestId), eq(userId), eq("completed"), isNull(), eq(false));
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

    /** Helper to create a request with a specific status for test overrides. */
    private ResumeGenerationRequest newResumeGenerationRequest(String status) {
        ResumeGenerationRequest r = new ResumeGenerationRequest();
        r.setId(requestId);
        r.setUserId(userId);
        r.setVacancyTitle("Java Developer");
        r.setCompanyName("MockTech");
        r.setStatus(status);
        return r;
    }
}
