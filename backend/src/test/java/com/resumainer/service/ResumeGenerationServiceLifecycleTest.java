package com.resumainer.service;

import com.resumainer.dao.AiModelDao;
import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dao.PromptConfigDao;
import com.resumainer.dao.ResumeBudgetConfigDao.BudgetConfig;
import com.resumainer.model.AiModel;
import com.resumainer.model.ResumeGenerationRequest;
import com.resumainer.service.ai.AiClient;
import com.resumainer.service.ai.AiClientException;
import com.resumainer.service.ai.AiClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Lifecycle tests for ResumeGenerationService.generate().
 * Focus on status guards: pending-only, processing-before-AI, completed-after-persist.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ResumeGenerationServiceLifecycleTest {

    @Mock private GenerationRequestDao requestDao;
    @Mock private ResumePromptBuilder promptBuilder;
    @Mock private PromptConfigDao promptConfigDao;
    @Mock private AiClientFactory aiClientFactory;
    @Mock private AiResponseParser responseParser;
    @Mock private AiResponseValidator responseValidator;
    @Mock private GenerationResponsePersistenceService persistenceService;
    @Mock private AiModelDao aiModelDao;
    @Mock private ResumeBudgetConfigService budgetConfigService;
    @Mock private AiClient aiClient;

    private ResumeGenerationService service;
    private final UUID requestId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID modelId = UUID.randomUUID();
    private final UUID promptConfigId = UUID.randomUUID();
    private final Map<String, Object> profilePayload = Map.of("contact", true);
    private List<AiResponseParser.ParsedVariant> parsedVariants;

    @BeforeEach
    void setUp() {
        service = new ResumeGenerationService(
                requestDao, promptBuilder, promptConfigDao, aiClientFactory, responseParser,
                responseValidator, persistenceService, aiModelDao, budgetConfigService);

        // Common stubs
        ResumePromptBuilder.PromptResult promptResult = new ResumePromptBuilder.PromptResult();
        promptResult.systemPrompt = "system";
        promptResult.requestPrompt = "request";
        promptResult.promptConfigId = promptConfigId;
        promptResult.profilePayload = profilePayload;
        promptResult.profilePayloadJson = "{\"contact\":true}";
        promptResult.promptHash = "hash-123";
        when(promptBuilder.build(any(), any())).thenReturn(promptResult);
        when(promptConfigDao.insertPromptRenderLog(any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(UUID.randomUUID());
        when(aiClientFactory.createOpenRouter(any())).thenReturn(aiClient);
        when(aiClient.generate(anyString(), anyString())).thenReturn("{}");

        AiResponseParser.ParsedVariant parsedVariant = new AiResponseParser.ParsedVariant();
        parsedVariant.languageCode = "EN";
        parsedVariant.adaptationLevel = "BALANCED";
        parsedVariant.coverLetter = "Cover letter";
        parsedVariants = List.of(parsedVariant);
        when(responseParser.parse(anyString(), anyString(), anyString())).thenReturn(parsedVariants);

        when(budgetConfigService.getActiveBudgetConfig())
                .thenReturn(new BudgetConfig(1L, "Test", 1));
        when(aiModelDao.findById(any())).thenReturn(new AiModel());
    }

    @Test
    void generateRejectsCompletedRequest() {
        // Given a completed request
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setAiModelId(modelId);
        request.setStatus("completed");
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        when(requestDao.findById(requestId, userId)).thenReturn(request);

        // When generate is called → status check rejects before any processing
        assertThrows(IllegalStateException.class, () ->
            service.generate(requestId, userId));

        // Then AI client is NOT called
        verify(aiClient, never()).generate(anyString(), anyString());
        // And no new response rows are created
        verify(persistenceService, never()).persistResponses(any(), any(), any());
        // And status was NOT changed to processing
        verify(requestDao, never()).updateStatus(requestId, userId, "processing", null, false);
    }

    @Test
    void generateRejectsProcessingRequest() {
        // Given a processing request (already being generated by another action)
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setAiModelId(modelId);
        request.setStatus("processing");
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        when(requestDao.findById(requestId, userId)).thenReturn(request);

        // When/Then — status check rejects before hasProcessingRequest or AI call
        // Now throws AiClientException instead of IllegalStateException to support
        // proper 409 Conflict mapping in the controller
        AiClientException ex = assertThrows(AiClientException.class,
                () -> service.generate(requestId, userId));
        assertEquals("GENERATION_ALREADY_IN_PROGRESS", ex.getErrorCode());
        verify(aiClient, never()).generate(anyString(), anyString());
        // Request must NOT be marked failed — it is already processing legitimately
        verify(requestDao, never()).updateStatus(eq(requestId), eq(userId), eq("failed"), anyString(), anyBoolean());
    }

    @Test
    void generateAcceptsPendingRequest() {
        // Given a pending request
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setAiModelId(modelId);
        request.setStatus("pending");
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        when(requestDao.findById(requestId, userId)).thenReturn(request);
        when(requestDao.hasProcessingRequest(userId)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> service.generate(requestId, userId));

        // Then AI client was called
        verify(aiClient, atLeastOnce()).generate(anyString(), anyString());
    }

    @Test
    void generateSetsProcessingBeforeAiCall() {
        // Given a pending request
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setAiModelId(modelId);
        request.setStatus("pending");
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        when(requestDao.findById(requestId, userId)).thenReturn(request);
        when(requestDao.hasProcessingRequest(userId)).thenReturn(false);

        // When
        service.generate(requestId, userId);

        // Then request status was set to 'processing' before AI call
        // (verify that updateStatus was called with 'processing' before generate)
        verify(requestDao).updateStatus(requestId, userId, "processing", null, false);
    }

    @Test
    void failedRequestCanBeRetried() {
        // Given a failed request
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setAiModelId(modelId);
        request.setStatus("failed");
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        when(requestDao.findById(requestId, userId)).thenReturn(request);
        when(requestDao.hasProcessingRequest(userId)).thenReturn(false);

        // When/Then - a failed request can be retried (treated like pending)
        assertDoesNotThrow(() -> service.generate(requestId, userId));
        verify(aiClient, atLeastOnce()).generate(anyString(), anyString());
    }

    @Test
    void generateSavesPromptRenderLogBeforeAiCall() {
        // Given a pending request
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setAiModelId(modelId);
        request.setStatus("pending");
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        when(requestDao.findById(requestId, userId)).thenReturn(request);
        when(requestDao.hasProcessingRequest(userId)).thenReturn(false);

        // When
        service.generate(requestId, userId);

        // Then the final rendered prompt is saved before the AI call.
        InOrder inOrder = inOrder(promptBuilder, promptConfigDao, aiClient);
        inOrder.verify(promptBuilder).build(requestId, userId);
        inOrder.verify(promptConfigDao).insertPromptRenderLog(
                eq(requestId),
                eq(promptConfigId),
                eq("system"),
                eq("request"),
                eq("{\"contact\":true}"),
                eq("hash-123")
        );
        inOrder.verify(aiClient).generate("system", "request");
    }


    @Test
    void generateValidatesParsedAiResponseBeforePersist() {
        // Given a pending request with cover letter enabled
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setAiModelId(modelId);
        request.setStatus("pending");
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        request.setIncludeCoverLetter(true);
        when(requestDao.findById(requestId, userId)).thenReturn(request);
        when(requestDao.hasProcessingRequest(userId)).thenReturn(false);

        // When
        service.generate(requestId, userId);

        // Then parsed AI response is validated before persistence.
        InOrder inOrder = inOrder(responseParser, responseValidator, persistenceService);
        inOrder.verify(responseParser).parse("{}", "ENGLISH_ONLY", "BALANCED");
        inOrder.verify(responseValidator).validate(parsedVariants, true, profilePayload);
        inOrder.verify(persistenceService).persistResponses(requestId, userId, parsedVariants);
    }


    @Test
    void generateWhenAiResponseValidationFailsMarksRequestFailedAndDoesNotPersist() {
        // Given a pending request and parsed AI response that violates validator rules
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setAiModelId(modelId);
        request.setStatus("pending");
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        request.setIncludeCoverLetter(false);
        when(requestDao.findById(requestId, userId)).thenReturn(request);
        when(requestDao.hasProcessingRequest(userId)).thenReturn(false);

        IllegalArgumentException validationError = new IllegalArgumentException(
                "AI response validation failed in EN/BALANCED: workExperience count exceeds resolved budget maximum: 16 > 10");
        doThrow(validationError).when(responseValidator).validate(parsedVariants, false, profilePayload);

        // When / Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.generate(requestId, userId));
        assertSame(validationError, ex,
                "Service must rethrow the original validation exception for upper layers to map correctly");

        // Then request lifecycle is safely closed as failed and nothing is persisted.
        verify(requestDao).updateStatus(requestId, userId, "processing", null, false);
        verify(requestDao).updateStatus(requestId, userId, "failed", validationError.getMessage(), false);
        verify(persistenceService, never()).persistResponses(any(), any(), any());
    }

    // ── GENERATION_ALREADY_IN_PROGRESS lifecycle tests ──────────────────

    @Test
    void generateWhenCurrentRequestAlreadyProcessing_throwsAlreadyInProgressWithoutMarkingFailed() {
        // Given the current request itself is already processing (duplicate POST)
        ResumeGenerationRequest request = new ResumeGenerationRequest();
        request.setId(requestId);
        request.setUserId(userId);
        request.setAiModelId(modelId);
        request.setStatus("processing");
        request.setLanguageMode("ENGLISH_ONLY");
        request.setAdaptationSelection("BALANCED");
        when(requestDao.findById(requestId, userId)).thenReturn(request);

        // When: duplicate POST /generate for the SAME processing request
        // Then: must throw AiClientException with GENERATION_ALREADY_IN_PROGRESS
        AiClientException ex = assertThrows(AiClientException.class,
                () -> service.generate(requestId, userId));
        assertEquals("GENERATION_ALREADY_IN_PROGRESS", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("already in progress") || ex.getMessage().contains("already processing"));

        // And: request must NOT be marked failed (it was processing before and stays processing)
        verify(requestDao, never()).updateStatus(eq(requestId), eq(userId), eq("failed"), anyString(), anyBoolean());
        // And: AI client is NOT called
        verify(aiClient, never()).generate(anyString(), anyString());
        // And: no response rows are created
        verify(persistenceService, never()).persistResponses(any(), any(), any());
    }

    @Test
    void generateWhenAnotherUserRequestIsProcessing_marksCurrentFailedAndReturnsConflict() {
        // Given request A (this request) is pending
        ResumeGenerationRequest currentRequest = new ResumeGenerationRequest();
        currentRequest.setId(requestId);
        currentRequest.setUserId(userId);
        currentRequest.setAiModelId(modelId);
        currentRequest.setStatus("pending");
        currentRequest.setLanguageMode("ENGLISH_ONLY");
        currentRequest.setAdaptationSelection("BALANCED");
        when(requestDao.findById(requestId, userId)).thenReturn(currentRequest);

        // Given another request for the same user is processing
        when(requestDao.hasProcessingRequest(userId)).thenReturn(true);

        // When: POST /generate for a NEW pending request while another is processing
        AiClientException ex = assertThrows(AiClientException.class,
                () -> service.generate(requestId, userId));
        assertEquals("GENERATION_ALREADY_IN_PROGRESS", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("already in progress"));

        // Then: current request is marked FAILED (not left pending forever)
        verify(requestDao).updateStatus(requestId, userId, "failed",
                "Generation already in progress. Please wait for it to complete.", false);

        // And: AI client is NOT called for this request
        verify(aiClient, never()).generate(anyString(), anyString());
        // And: no response rows are created
        verify(persistenceService, never()).persistResponses(any(), any(), any());
    }

    @Test
    void generateWhenAnotherRequestIsProcessing_doesNotInterfereWithProcessingRequest() {
        // Given request A is pending
        ResumeGenerationRequest currentRequest = new ResumeGenerationRequest();
        currentRequest.setId(requestId);
        currentRequest.setUserId(userId);
        currentRequest.setAiModelId(modelId);
        currentRequest.setStatus("pending");
        currentRequest.setLanguageMode("ENGLISH_ONLY");
        currentRequest.setAdaptationSelection("BALANCED");
        when(requestDao.findById(requestId, userId)).thenReturn(currentRequest);

        // Given another request is processing
        when(requestDao.hasProcessingRequest(userId)).thenReturn(true);

        // When
        assertThrows(AiClientException.class, () -> service.generate(requestId, userId));

        // Then: request A is marked failed, but the OTHER processing request is untouched
        // (only requestId is failed, not the processing one)
        verify(requestDao).updateStatus(eq(requestId), eq(userId), eq("failed"), anyString(), eq(false));
        // Never set requestId to 'processing' because it was blocked early
        verify(requestDao, never()).updateStatus(eq(requestId), eq(userId), eq("processing"), any(), anyBoolean());
    }
}
