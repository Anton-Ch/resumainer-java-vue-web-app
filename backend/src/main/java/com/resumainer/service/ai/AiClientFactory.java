package com.resumainer.service.ai;

import com.resumainer.dao.AiModelDao;
import com.resumainer.model.AiModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Factory Method pattern — returns the correct AiClient implementation.
 * MockAiClient for dev/test profiles, OpenRouterClient for production.
 */
@Service
public class AiClientFactory {

    private static final Logger log = LoggerFactory.getLogger(AiClientFactory.class);

    private final AiModelDao aiModelDao;

    public AiClientFactory(AiModelDao aiModelDao) {
        this.aiModelDao = aiModelDao;
    }

    /**
     * Returns a MockAiClient (for dev/test, no real API calls).
     */
    public AiClient createMock() {
        log.debug("Creating MockAiClient");
        return new MockAiClient();
    }

    /**
     * Returns an OpenRouterClient for the given model ID.
     * Loads the full AiModel (including encrypted API key) from the DAO.
     *
     * @param aiModelId the UUID of the ai_model to use
     * @throws AiClientException if the model is not found
     */
    public AiClient createOpenRouter(UUID aiModelId) {
        AiModel model = aiModelDao.findById(aiModelId);
        if (model == null) {
            throw new AiClientException("Selected AI model not found.");
        }
        log.debug("Creating OpenRouterClient for model: {}", model.getModelCode());
        return new OpenRouterClient(model);
    }
}
