package com.resumainer.controller;

import com.resumainer.dao.AiModelDao;
import com.resumainer.dao.SavedResumeDao;
import com.resumainer.dto.UserSession;
import com.resumainer.dto.generate.*;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.ResumeGenerationRequest;
import com.resumainer.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for the generation pipeline.
 * All endpoints require authentication (guarded by AuthInterceptor).
 * User ID is extracted from HttpSession, never from request body.
 */
@RestController
@RequestMapping("/api/generate")
public class GenerateResumeController {

    private static final Logger log = LoggerFactory.getLogger(GenerateResumeController.class);

    private final GenerationRequestService generationRequestService;
    private final ResumeGenerationService resumeGenerationService;
    private final ResumeReviewService resumeReviewService;
    private final ResumeFinalizeService resumeFinalizeService;
    private final GeneratedFileStorageService fileStorage;
    private final AiModelDao aiModelDao;
    private final SavedResumeDao savedResumeDao;

    public GenerateResumeController(GenerationRequestService generationRequestService,
                                     ResumeGenerationService resumeGenerationService,
                                     ResumeReviewService resumeReviewService,
                                     ResumeFinalizeService resumeFinalizeService,
                                     GeneratedFileStorageService fileStorage,
                                     AiModelDao aiModelDao,
                                     SavedResumeDao savedResumeDao) {
        this.generationRequestService = generationRequestService;
        this.resumeGenerationService = resumeGenerationService;
        this.resumeReviewService = resumeReviewService;
        this.resumeFinalizeService = resumeFinalizeService;
        this.fileStorage = fileStorage;
        this.aiModelDao = aiModelDao;
        this.savedResumeDao = savedResumeDao;
    }

    /**
     * T064: Returns AI models available for the current user.
     * Non-privileged users see only active, non-hidden models.
     * Never exposes API keys.
     */
    @GetMapping("/ai-models")
    public ResponseEntity<List<AiModelDto>> getAiModels(HttpSession session) {
        UUID userId = getUserId(session);
        log.debug("GET /api/generate/ai-models — userId={}", userId);

        boolean isPrivileged = isUserPrivileged(session);
        List<AiModelDto> models = isPrivileged
                ? aiModelDao.findAvailableModelsPrivileged()
                : aiModelDao.findAvailableModels();

        return noCache(ResponseEntity.ok(models));
    }

    /**
     * T063: Creates a new generation request.
     */
    @PostMapping("/requests")
    public ResponseEntity<UUID> createRequest(HttpSession session,
                                               @RequestBody GenerationRequestCreateDto dto) {
        UUID userId = getUserId(session);
        log.debug("POST /api/generate/requests — userId={}", userId);

        ResumeGenerationRequest created = generationRequestService.createRequest(userId, dto);
        return noCache(ResponseEntity.status(HttpStatus.CREATED).body(created.getId()));
    }

    /**
     * T065: Executes generation for a request. Synchronous for MVP.
     * Settings must be persisted via PUT /settings before calling this endpoint.
     * This endpoint does not accept settings overrides — it uses persisted request settings.
     */
    @PostMapping("/requests/{requestId}/generate")
    public ResponseEntity<?> generate(HttpSession session,
                                       @PathVariable UUID requestId) {
        UUID userId = getUserId(session);
        log.debug("POST /api/generate/requests/{}/generate — userId={}", requestId, userId);

        try {
            resumeGenerationService.generate(requestId, userId);
            return noCache(ResponseEntity.ok().body(java.util.Map.of("status", "completed")));
        } catch (IllegalArgumentException e) {
            if (isAiResponseValidationFailure(e)) {
                return noCache(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(new GenerationErrorDto(
                                "AI_RESPONSE_VALIDATION_FAILED",
                                e.getMessage(),
                                true, true, "failed")));
            }

            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GenerationErrorDto(
                            "REQUEST_NOT_FOUND", e.getMessage(),
                            false, false, "failed")));
        } catch (com.resumainer.service.ai.AiClientException e) {
            log.warn("Generation failed for request: {} — {} [{}]", requestId, e.getMessage(), e.getErrorCode());
            String errorCode = e.getErrorCode();
            boolean retryAllowed = !"GENERATION_ALREADY_IN_PROGRESS".equals(errorCode);
            boolean changeAllowed = !"GENERATION_ALREADY_IN_PROGRESS".equals(errorCode);
            String message = "GENERATION_ALREADY_IN_PROGRESS".equals(errorCode)
                    ? "Generation already in progress. Please wait for it to complete."
                    : "Generation failed while contacting the AI model. "
                      + "Please try again or change settings.";
            HttpStatus status = "GENERATION_ALREADY_IN_PROGRESS".equals(errorCode)
                    ? HttpStatus.CONFLICT
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            return noCache(ResponseEntity.status(status)
                    .body(new GenerationErrorDto(
                            errorCode, message,
                            retryAllowed, changeAllowed, "failed")));
        } catch (Exception e) {
            log.warn("Generation failed for request: {} — {}", requestId, e.getMessage());
            return noCache(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenerationErrorDto(
                            "GENERATION_FAILED",
                            "Generation failed. Please try again or change settings.",
                            true, true, "failed")));
        }
    }

    /**
     * T065b: Updates generation settings for a pending request.
     * Settings must be persisted via this endpoint before calling POST /generate.
     * This separates request state from the generation command (DEC-007-SET-001).
     */
    @PutMapping("/requests/{requestId}/settings")
    public ResponseEntity<?> updateSettings(HttpSession session,
                                             @PathVariable UUID requestId,
                                             @RequestBody GenerationSettingsDto settings) {
        UUID userId = getUserId(session);
        log.debug("PUT /api/generate/requests/{}/settings — userId={}", requestId, userId);

        try {
            generationRequestService.updateSettings(requestId, userId,
                    settings.getLanguageMode(),
                    settings.getAdaptationSelection(),
                    settings.getAiModelId(),
                    settings.getIncludeCoverLetter() != null ? settings.getIncludeCoverLetter() : false);
            return noCache(ResponseEntity.ok().body(java.util.Map.of("success", true)));
        } catch (IllegalArgumentException e) {
            return noCache(ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage())));
        }
    }

    /**
     * T066: Returns grouped review data.
     */
    @GetMapping("/requests/{requestId}/review")
    public ResponseEntity<GenerationReviewDto> getReview(HttpSession session,
                                                          @PathVariable UUID requestId) {
        UUID userId = getUserId(session);
        log.debug("GET /api/generate/requests/{}/review — userId={}", requestId, userId);

        GenerationReviewDto review = resumeReviewService.getReview(requestId, userId);
        return noCache(ResponseEntity.ok(review));
    }

    /**
     * T067: Saves review edits.
     * fieldUpdates maps backend-generated updateKey to new value.
     * updateKey format: "sectionKey:recordId:fieldName:adaptationCode"
     * Frontend must not construct update keys manually.
     */
    @PutMapping("/requests/{requestId}/review")
    public ResponseEntity<?> saveReview(HttpSession session,
                                         @PathVariable UUID requestId,
                                         @RequestBody GenerationReviewUpdateDto dto) {
        UUID userId = getUserId(session);
        log.debug("PUT /api/generate/requests/{}/review — userId={}", requestId, userId);

        if (dto.getFieldUpdates() != null) {
            dto.getFieldUpdates().forEach((updateKey, value) -> {
                resumeReviewService.saveReview(requestId, userId, updateKey, value);
            });
        }
        return noCache(ResponseEntity.ok().body(java.util.Map.of("success", true)));
    }

    /**
     * T082: Finalizes a generation request.
     */
    @PostMapping("/requests/{requestId}/finalize")
    public ResponseEntity<?> finalizeRequest(HttpSession session,
                                              @PathVariable UUID requestId,
                                              @RequestBody FinalizeResumeRequestDto dto) {
        UUID userId = getUserId(session);
        log.debug("POST /api/generate/requests/{}/finalize — userId={}", requestId, userId);

        try {
            ExportResultDto export = resumeFinalizeService.finalizeRequest(
                    requestId, userId, dto.getSelectedAdaptationLevel());
            return noCache(ResponseEntity.ok(export));
        } catch (IllegalArgumentException e) {
            return noCache(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage())));
        } catch (Exception e) {
            log.warn("Finalization failed for request: {}", requestId, e);
            return noCache(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Failed to finalize resume. Please try again.")));
        }
    }

    /**
     * T083: Returns export data with download URLs.
     */
    @GetMapping("/requests/{requestId}/export")
    public ResponseEntity<ExportResultDto> getExport(HttpSession session,
                                                      @PathVariable UUID requestId) {
        UUID userId = getUserId(session);
        log.debug("GET /api/generate/requests/{}/export — userId={}", requestId, userId);

        // Verify ownership and load saved resumes
        List<SavedResumeDao.SavedResumeRow> savedRows = savedResumeDao.findByGenerationRequestId(requestId, userId);
        if (savedRows.isEmpty()) {
            return noCache(ResponseEntity.notFound().build());
        }

        ExportResultDto export = new ExportResultDto();
        List<SavedResumeExportDto> exportItems = new ArrayList<>();

        for (SavedResumeDao.SavedResumeRow row : savedRows) {
            SavedResumeExportDto item = new SavedResumeExportDto();
            item.setSavedResumeId(row.id);
            item.setLanguageCode(row.language);
            item.setAdaptationLevel(row.adaptationLevel);
            item.setHtmlDownloadUrl("/api/generate/resumes/" + row.id + "/html");
            item.setPdfDownloadUrl("/api/generate/resumes/" + row.id + "/pdf");
            item.setPdfOpenUrl("/api/generate/resumes/" + row.id + "/pdf?disposition=inline");
            item.setPublicUrlLink("/" + row.username + "/" + row.publicCode);
            boolean pdfReady = "READY".equals(row.pdfStatus) && row.pdfFilePath != null;
            item.setPdfAvailable(pdfReady);
            item.setPdfMessage(pdfReady ? null : "PDF is being generated. Please try again later.");
            item.setCoverLetter(row.coverLetter);
            exportItems.add(item);
        }

        export.setResumes(exportItems);
        return noCache(ResponseEntity.ok(export));
    }

    /**
     * T084: Authenticated HTML download.
     * Owner-scoped: only the generating user can download their resume HTML.
     */
    @GetMapping("/resumes/{savedResumeId}/html")
    public ResponseEntity<Resource> downloadHtml(HttpSession session,
                                                  @PathVariable long savedResumeId) {
        UUID userId = getUserId(session);
        log.debug("GET /api/resumes/{}/html — userId={}", savedResumeId, userId);

        SavedResumeDao.SavedResumeRow row = savedResumeDao.findById(savedResumeId, userId);
        if (row == null || row.htmlFilePath == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        try {
            Path filePath = fileStorage.resolveSafePath(row.htmlFilePath);
            Resource resource = new FileSystemResource(filePath);
            if (!resource.exists()) {
                log.warn("HTML file not found for saved resume {}: {}", savedResumeId, row.htmlFilePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"resume-" + savedResumeId + ".html\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error serving HTML for saved resume: {}", savedResumeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Feature 008: Real PDF download with owner scoping and path traversal protection.
     * Supports ?disposition=inline for in-browser viewing.
     */
    @GetMapping("/resumes/{savedResumeId}/pdf")
    public ResponseEntity<Resource> downloadPdf(HttpSession session,
                                                 @PathVariable long savedResumeId,
                                                 @RequestParam(required = false, defaultValue = "attachment") String disposition) {
        UUID userId = getUserId(session);
        log.debug("GET /api/resumes/{}/pdf — userId={}", savedResumeId, userId);

        SavedResumeDao.SavedResumeRow row = savedResumeDao.findById(savedResumeId, userId);
        if (row == null || row.pdfFilePath == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            Path filePath = fileStorage.resolveSafePath(row.pdfFilePath);
            Resource resource = new FileSystemResource(filePath);
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            disposition + "; filename=\"resume-" + savedResumeId + ".pdf\"")
                    .body(resource);
        } catch (SecurityException e) {
            log.warn("Path safety rejected for PDF saved resume {}", savedResumeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Error serving PDF for saved resume: {}", savedResumeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Public resume route moved to PublicResumeController (GET /{username}/{publicCode}) — Feature 008

    private boolean isAiResponseValidationFailure(IllegalArgumentException e) {
        String message = e.getMessage();
        return message != null && message.startsWith("AI response validation failed");
    }

    // --- Session helpers ---

    private UUID getUserId(HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute("user");
        if (userSession == null) {
            throw new ServiceException("auth.unauthorized", "Not authenticated");
        }
        return userSession.getUserId();
    }

    private boolean isUserPrivileged(HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute("user");
        return userSession != null && userSession.isPrivileged();
    }

    private <T> ResponseEntity<T> noCache(ResponseEntity<T> response) {
        return ResponseEntity.status(response.getStatusCode())
                .cacheControl(CacheControl.noStore().cachePrivate())
                .headers(response.getHeaders())
                .body(response.getBody());
    }

    private <T> ResponseEntity<T> noCache(ResponseEntity.BodyBuilder builder, T body) {
        return builder.cacheControl(CacheControl.noStore().cachePrivate()).body(body);
    }
}
