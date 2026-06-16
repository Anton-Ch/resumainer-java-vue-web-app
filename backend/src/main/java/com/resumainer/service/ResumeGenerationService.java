package com.resumainer.service;

import com.resumainer.dao.AiModelDao;
import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dao.PromptConfigDao;
import com.resumainer.dao.ResumeBudgetConfigDao.BudgetConfig;
import com.resumainer.model.AiModel;
import com.resumainer.model.ResumeGenerationRequest;
import com.resumainer.service.ai.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the full generation flow:
 * build prompt → call AI → parse response → persist.
 */
@Service
public class ResumeGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ResumeGenerationService.class);

    /**
     * Build marker — proves running WAR was built after this timestamp during Phase 4.8.2
     */
    private static final String BUILD_MARKER = "BUILD_MARKER feat007-phase-4.8.2 2026-06-13T22:30:00Z";

    private final GenerationRequestDao requestDao;
    private final ResumePromptBuilder promptBuilder;
    private final PromptConfigDao promptConfigDao;
    private final AiClientFactory aiClientFactory;
    private final AiResponseParser responseParser;
    private final AiResponseValidator responseValidator;
    private final GenerationResponsePersistenceService persistenceService;
    private final AiModelDao aiModelDao;
    private final ResumeBudgetConfigService budgetConfigService;

    public ResumeGenerationService(GenerationRequestDao requestDao,
                                   ResumePromptBuilder promptBuilder,
                                   PromptConfigDao promptConfigDao,
                                   AiClientFactory aiClientFactory,
                                   AiResponseParser responseParser,
                                   AiResponseValidator responseValidator,
                                   GenerationResponsePersistenceService persistenceService,
                                   AiModelDao aiModelDao,
                                   ResumeBudgetConfigService budgetConfigService) {
        this.requestDao = requestDao;
        this.promptBuilder = promptBuilder;
        this.promptConfigDao = promptConfigDao;
        this.aiClientFactory = aiClientFactory;
        this.responseParser = responseParser;
        this.responseValidator = responseValidator;
        this.persistenceService = persistenceService;
        this.aiModelDao = aiModelDao;
        this.budgetConfigService = budgetConfigService;
    }

    /**
     * Executes generation for a request. Synchronous for MVP.
     * Settings must be persisted via PUT /settings before calling this method.
     *
     * @param requestId the generation request ID
     * @param userId    the authenticated user ID
     * @throws IllegalArgumentException if request not found
     * @throws AiClientException        if generation fails
     */
    public void generate(UUID requestId, UUID userId) {
        log.info(BUILD_MARKER);
        ResumeGenerationRequest request = requestDao.findById(requestId, userId);
        if (request == null) {
            throw new IllegalArgumentException("Generation request not found.");
        }

        // Phase 4.8 — Reject generation unless request is pending or failed (retry).
        // Completed or processing requests must not be silently regenerated.
        String status = request.getStatus();
        if ("completed".equals(status)) {
            throw new IllegalStateException(
                    "Generation request " + requestId + " is already completed. "
                            + "Create a new request to generate again.");
        }
        if ("processing".equals(status)) {
            throw new IllegalStateException(
                    "Generation request " + requestId + " is already processing.");
        }

        // Check one active generation BEFORE spending AI tokens
        if (requestDao.hasProcessingRequest(userId)) {
            throw new com.resumainer.service.ai.AiClientException(
                    "Generation already in progress. Please wait for it to complete.",
                    "GENERATION_ALREADY_IN_PROGRESS");
        }

        // Set status to processing BEFORE AI call to prevent concurrent duplicates
        requestDao.updateStatus(requestId, userId, "processing", null, false);

        try {
            log.info("GENERATION_START requestId={}, lang={}, adapt={}",
                    requestId, request.getLanguageMode(), request.getAdaptationSelection());

            // Snapshot active budget config on request before generation
            BudgetConfig budgetConfig = budgetConfigService.getActiveBudgetConfig();
            requestDao.updateBudgetSnapshot(requestId, userId, budgetConfig.id, budgetConfig.versionNo);
            log.info("Budget snapshot for request {}: configId={}, version={}",
                    requestId, budgetConfig.id, budgetConfig.versionNo);

            // 1. Build prompt
            ResumePromptBuilder.PromptResult promptResult = promptBuilder.build(requestId, userId);

            // 2. Save rendered prompt log before AI call
            UUID promptRenderLogId = promptConfigDao.insertPromptRenderLog(
                    requestId,
                    promptResult.promptConfigId,
                    promptResult.systemPrompt,
                    promptResult.requestPrompt,
                    promptResult.profilePayloadJson,
                    promptResult.promptHash
            );

            log.info("PROMPT_RENDER_LOG_SAVED requestId={}, promptRenderLogId={}, promptConfigId={}, promptHash={}",
                    requestId, promptRenderLogId, promptResult.promptConfigId, promptResult.promptHash);

            // 3. Look up model info for audit logging
            AiModel model = aiModelDao.findById(request.getAiModelId());
            String modelCode = model != null ? model.getModelCode() : "unknown";
            String provider = model != null ? model.getProvider() : "unknown";
            log.info("GENERATION_AI_CALL_START requestId={}, modelCode={}, provider={}",
                    requestId, modelCode, provider);

            // 3. Call AI
            AiClient aiClient = aiClientFactory.createOpenRouter(request.getAiModelId());
            long t0 = System.currentTimeMillis();
            String rawResponse = aiClient.generate(promptResult.systemPrompt, promptResult.requestPrompt);
            long durationMs = System.currentTimeMillis() - t0;
            log.info("GENERATION_AI_CALL_DONE requestId={}, durationMs={}", requestId, durationMs);

            // 4. Parse response
            List<AiResponseParser.ParsedVariant> variants = responseParser.parse(
                    rawResponse, request.getLanguageMode(), request.getAdaptationSelection());

            // 5. Validate parsed response against request/profile-dependent rules
            responseValidator.validate(
                    variants,
                    request.isIncludeCoverLetter(),
                    promptResult.profilePayload
            );

            // 6. Persist
            persistenceService.persistResponses(requestId, userId, variants);

            log.info("GENERATION_DONE requestId={}, variants={}, durationMs={}",
                    requestId, variants.size(), durationMs);
        } catch (Exception e) {
            // If generation fails mid-way, reset status back to pending so retry is possible
            log.warn("Generation failed for request {}: {}. Resetting status to failed.", requestId, e.getMessage());
            requestDao.updateStatus(requestId, userId, "failed", e.getMessage(), false);
            throw e;
        }
    }
}
