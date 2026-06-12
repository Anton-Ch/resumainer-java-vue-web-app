package com.resumainer.controller;

import com.resumainer.dao.AiModelDao;
import com.resumainer.dto.UserSession;
import com.resumainer.dto.generate.*;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.ResumeGenerationRequest;
import com.resumainer.service.*;
import jakarta.servlet.http.HttpSession;
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
    private final AiModelDao aiModelDao;

    public GenerateResumeController(GenerationRequestService generationRequestService,
                                     ResumeGenerationService resumeGenerationService,
                                     ResumeReviewService resumeReviewService,
                                     AiModelDao aiModelDao) {
        this.generationRequestService = generationRequestService;
        this.resumeGenerationService = resumeGenerationService;
        this.resumeReviewService = resumeReviewService;
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
