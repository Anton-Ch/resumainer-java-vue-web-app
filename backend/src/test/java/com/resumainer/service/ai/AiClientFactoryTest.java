package com.resumainer.service.ai;

import com.resumainer.dao.AiModelDao;
import com.resumainer.model.AiModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AiClientFactory.
 * Covers createMock, createOpenRouter, and model-not-found error.
 */
@ExtendWith(MockitoExtension.class)
class AiClientFactoryTest {

    @Mock
    private AiModelDao aiModelDao;

    private AiClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AiClientFactory(aiModelDao);
    }

    @Test
    void createMock_returnsMockAiClient() {
        AiClient client = factory.createMock();
        assertInstanceOf(MockAiClient.class, client);
    }

    @Test
    void createOpenRouter_returnsOpenRouterClient_whenModelFound() {
        UUID modelId = UUID.randomUUID();
        AiModel model = new AiModel();
        model.setId(modelId);
        model.setModelCode("deepseek/deepseek-v4");
        model.setApiKeyEncrypted("enc:sk-test");
        when(aiModelDao.findById(modelId)).thenReturn(model);

        AiClient client = factory.createOpenRouter(modelId);

        assertInstanceOf(OpenRouterClient.class, client);
        verify(aiModelDao).findById(modelId);
    }

    @Test
    void createOpenRouter_throwsException_whenModelNotFound() {
        UUID modelId = UUID.randomUUID();
        when(aiModelDao.findById(modelId)).thenReturn(null);

        AiClientException ex = assertThrows(AiClientException.class,
                () -> factory.createOpenRouter(modelId));

        assertTrue(ex.getMessage().contains("not found"));
    }
}
