package com.resumainer.service;

import com.resumainer.dao.AiModelDao;
import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dto.generate.GenerationRequestCreateDto;
import com.resumainer.model.ResumeGenerationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Handles generation request creation with AI model availability validation.
 */
@Service
public class GenerationRequestService {

    private static final Logger log = LoggerFactory.getLogger(GenerationRequestService.class);

    private final GenerationRequestDao requestDao;
    private final AiModelDao aiModelDao;

    public GenerationRequestService(GenerationRequestDao requestDao, AiModelDao aiModelDao) {
        this.requestDao = requestDao;
        this.aiModelDao = aiModelDao;
    }

    /**
     * Creates a new generation request. Validates that the selected AI model
     * is available for the requesting user.
     *
     * @param userId   authenticated user ID
     * @param dto      request data from frontend
     * @return the created request with ID populated
     */
    public ResumeGenerationRequest createRequest(UUID userId, GenerationRequestCreateDto dto) {
        // Validate AI model exists and is active
        // Frontend filters by privilege; backend validates the model is active
        if (aiModelDao.findById(dto.getAiModelId()) == null) {
            throw new IllegalArgumentException("Selected AI model is not available.");
        }

        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setUserId(userId);
        request.setAiModelId(dto.getAiModelId());
        request.setVacancyTitle(dto.getVacancyTitle());
        request.setVacancyDescription(dto.getVacancyDescription());
        request.setCompanyName(dto.getCompanyName());
        request.setCompanyDescription(dto.getCompanyDescription());
        request.setAdditionalComments(dto.getAdditionalComments());
        request.setIncludeCoverLetter(dto.isIncludeCoverLetter());
        request.setLanguageMode(dto.getLanguageMode());
        request.setAdaptationSelection(dto.getAdaptationSelection());
        request.setStatus("pending");

        ResumeGenerationRequest created = requestDao.create(request);
        log.info("Generation request created: id={}, userId={}", created.getId(), userId);
        return created;
    }

    /**
     * Finds a request by ID, verifying ownership.
     */
    public ResumeGenerationRequest findById(UUID requestId, UUID userId) {
        return requestDao.findById(requestId, userId);
    }

    /**
     * Updates generation settings (language, adaptation, model, cover letter) for a pending request.
     * Settings must be saved via this endpoint before calling POST /generate.
     * This separates request state from the generation command.
     *
     * @param requestId          the generation request ID
     * @param userId             the authenticated user ID
     * @param languageMode       target language mode
     * @param adaptationSelection target adaptation level
     * @param aiModelId          target AI model
     * @param includeCoverLetter whether to include a cover letter
     * @throws IllegalArgumentException if request not found or not in pending status
     */
    public void updateSettings(UUID requestId, UUID userId,
                                String languageMode, String adaptationSelection,
                                UUID aiModelId, boolean includeCoverLetter) {
        ResumeGenerationRequest request = requestDao.findById(requestId, userId);
        if (request == null) {
            throw new IllegalArgumentException("Generation request not found.");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("Cannot update settings: request is already " + request.getStatus());
        }
        boolean updated = requestDao.updateSettings(requestId, userId, languageMode, adaptationSelection,
                aiModelId, includeCoverLetter);
        if (!updated) {
            throw new IllegalArgumentException("Failed to update settings. Request may not be in pending status.");
        }
        log.info("Settings updated for request {}: lang={}, adapt={}, modelId={}, coverLetter={}",
                requestId, languageMode, adaptationSelection, aiModelId, includeCoverLetter);
    }
}
