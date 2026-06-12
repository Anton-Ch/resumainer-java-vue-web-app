package com.resumainer.service;

import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dao.PromptConfigDao;
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

    private final GenerationRequestDao requestDao;
    private final ResumePromptBuilder promptBuilder;
    private final AiClientFactory aiClientFactory;
    private final AiResponseParser responseParser;
    private final GenerationResponsePersistenceService persistenceService;

    public ResumeGenerationService(GenerationRequestDao requestDao,
                                    ResumePromptBuilder promptBuilder,
                                    AiClientFactory aiClientFactory,
                                    AiResponseParser responseParser,
                                    GenerationResponsePersistenceService persistenceService) {
        this.requestDao = requestDao;
        this.promptBuilder = promptBuilder;
        this.aiClientFactory = aiClientFactory;
        this.responseParser = responseParser;
        this.persistenceService = persistenceService;
    }

    /**
     * Executes generation for a request. Synchronous for MVP.
     *
     * @param requestId the generation request ID
     * @param userId    the authenticated user ID
     * @throws IllegalArgumentException if request not found
     * @throws AiClientException       if generation fails
     */
    public void generate(UUID requestId, UUID userId) {
        ResumeGenerationRequest request = requestDao.findById(requestId, userId);
        if (request == null) {
            throw new IllegalArgumentException("Generation request not found.");
        }

        // Check one active generation BEFORE spending AI tokens
        if (requestDao.hasProcessingRequest(userId)) {
            throw new com.resumainer.service.ai.AiClientException(
                    "Generation already in progress. Please wait for it to complete.",
                    "GENERATION_ALREADY_IN_PROGRESS");
        }

        log.info("Starting generation for request: {}", requestId);

        // 1. Build prompt
        ResumePromptBuilder.PromptResult promptResult = promptBuilder.build(requestId, userId);

        // 2. Call AI
        AiClient aiClient = aiClientFactory.createOpenRouter(request.getAiModelId());
        String rawResponse = aiClient.generate(promptResult.systemPrompt, promptResult.requestPrompt);

        // 3. Parse response
        List<AiResponseParser.ParsedVariant> variants = responseParser.parse(
                rawResponse, request.getLanguageMode(), request.getAdaptationSelection());

        // 4. Persist
        persistenceService.persistResponses(requestId, userId, variants);

        log.info("Generation completed for request: {} — {} variants", requestId, variants.size());
    }
}
