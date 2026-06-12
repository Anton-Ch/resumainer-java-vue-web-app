package com.resumainer.controller;

import com.resumainer.dao.AiModelDao;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public GenerateResumeController(GenerationRequestService generationRequestService,
                                     ResumeGenerationService resumeGenerationService,
                                     ResumeReviewService resumeReviewService,
                                     ResumeFinalizeService resumeFinalizeService,
                                     GeneratedFileStorageService fileStorage,
                                     AiModelDao aiModelDao) {
        this.generationRequestService = generationRequestService;
        this.resumeGenerationService = resumeGenerationService;
        this.resumeReviewService = resumeReviewService;
        this.resumeFinalizeService = resumeFinalizeService;
        this.fileStorage = fileStorage;
        this.aiModelDao = aiModelDao;
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
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", e.getMessage())));
        } catch (Exception e) {
            log.warn("Generation failed for request: {} — {}", requestId, e.getMessage());
            return noCache(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "status", "failed",
                            "error", "Generation failed. Please try again or change settings.")));
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
     * T067: Saves a review edit for a specific field.
     */
    @PutMapping("/requests/{requestId}/review")
    public ResponseEntity<?> saveReview(HttpSession session,
                                         @PathVariable UUID requestId,
                                         @RequestBody GenerationReviewUpdateDto dto) {
        UUID userId = getUserId(session);
        log.debug("PUT /api/generate/requests/{}/review — userId={}", requestId, userId);

        if (dto.getFieldUpdates() != null) {
            dto.getFieldUpdates().forEach((fieldPath, value) -> {
                // fieldPath format: "responseId.fieldName"
                String[] parts = fieldPath.split("\\.", 2);
                if (parts.length == 2) {
                    UUID responseId = UUID.fromString(parts[0]);
                    String fieldName = parts[1];
                    resumeReviewService.saveReview(requestId, userId, responseId, fieldName, value);
                }
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

        // Verify ownership
        ResumeGenerationRequest request = generationRequestService.findById(requestId, userId);
        if (request == null) {
            return noCache(ResponseEntity.notFound().build());
        }

        // Export data loaded from saved_resumes — simplified for MVP
        return noCache(ResponseEntity.ok(new ExportResultDto()));
    }

    /**
     * T084: Authenticated HTML download.
     */
    @GetMapping("/resumes/{savedResumeId}/html")
    public ResponseEntity<Resource> downloadHtml(HttpSession session,
                                                  @PathVariable long savedResumeId) {
        UUID userId = getUserId(session);
        log.debug("GET /api/resumes/{}/html — userId={}", savedResumeId, userId);

        // Look up saved resume and verify ownership
        // For MVP, return placeholder if not found
        // TODO: Implement full owner-scoped lookup in Phase 13
        return noCache(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * T085: PDF download — placeholder stub in feat/007.
     */
    @GetMapping("/resumes/{savedResumeId}/pdf")
    public ResponseEntity<?> downloadPdf(@PathVariable long savedResumeId) {
        log.debug("GET /api/resumes/{}/pdf — placeholder stub (feat/008)", savedResumeId);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(java.util.Map.of(
                        "available", false,
                        "message", "PDF generation is not available in this version. "
                                + "It will be available in a future update."));
    }

    /**
     * T086: Public route — placeholder in feat/007.
     */
    @GetMapping("/candidate/{publicCode}")
    public ResponseEntity<?> publicResume(@PathVariable String publicCode) {
        log.debug("GET /candidate/{} — placeholder stub (feat/008)", publicCode);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(java.util.Map.of(
                        "available", false,
                        "message", "Public resume links are not available in this version. "
                                + "They will be available in a future update."));
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
