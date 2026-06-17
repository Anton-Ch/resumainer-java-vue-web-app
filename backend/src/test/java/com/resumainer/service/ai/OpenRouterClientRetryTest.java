package com.resumainer.service.ai;

import com.resumainer.model.AiModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for OpenRouterClient retry loop behavior.
 * Uses Mockito to simulate HTTP responses for reasoning-only then valid-content sequences.
 */
@ExtendWith(MockitoExtension.class)
class OpenRouterClientRetryTest {

    private static final String CONTENT_PRESENT_JSON = """
            {
              "choices": [{
                "finish_reason": "stop",
                "message": {
                  "role": "assistant",
                  "content": "{\\"english\\":{\\"minimal\\":{}}}"
                }
              }]
            }
            """;

    private static final String MISSING_CONTENT_WITH_REASONING_JSON = """
            {
              "choices": [{
                "finish_reason": "stop",
                "message": {
                  "role": "assistant",
                  "content": null,
                  "reasoning": "Let me compose a resume for this job..."
                }
              }]
            }
            """;

    private static final String MISSING_CONTENT_BLANK_WITH_REASONING_JSON = """
            {
              "choices": [{
                "finish_reason": "stop",
                "message": {
                  "role": "assistant",
                  "content": "",
                  "reasoning": "Let me think..."
                }
              }]
            }
            """;

    @Mock
    private HttpClient mockHttpClient;

    private AiModel model;

    @BeforeEach
    void setUp() {
        model = new AiModel();
        model.setModelCode("@preset/deepseekflashonly");
        model.setProviderApiUrl("https://openrouter.ai/api/v1/chat/completions");
    }

    @SuppressWarnings("unchecked")
    @Test
    void generate_contentPresentOnAttempt1_singleHttpCall_success() throws Exception {
        // Given content is present on first attempt
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(CONTENT_PRESENT_JSON);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        OpenRouterClient client = new OpenRouterClient(model, mockHttpClient);

        // When
        String result = client.generate("system", "user");

        // Then: exactly 1 HTTP call
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any());
        assertNotNull(result);
        assertTrue(result.contains("english"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void generate_attempt1NullContentAttempt2ValidContent_sends2HttpCalls_success() throws Exception {
        // Given attempt 1 returns null content + reasoning, attempt 2 returns valid content
        HttpResponse<String> response1 = mock(HttpResponse.class);
        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn(MISSING_CONTENT_WITH_REASONING_JSON);

        HttpResponse<String> response2 = mock(HttpResponse.class);
        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn(CONTENT_PRESENT_JSON);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response1)
                .thenReturn(response2);

        OpenRouterClient client = new OpenRouterClient(model, mockHttpClient);

        // When
        String result = client.generate("system", "user");

        // Then: exactly 2 HTTP calls
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any());
        assertNotNull(result);
        assertTrue(result.contains("english"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void generate_attempt1And2NullContentAttempt3ValidContent_sends3HttpCalls_success() throws Exception {
        // Given attempts 1 & 2 return null content + reasoning, attempt 3 valid
        HttpResponse<String> response1 = mock(HttpResponse.class);
        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn(MISSING_CONTENT_WITH_REASONING_JSON);

        HttpResponse<String> response2 = mock(HttpResponse.class);
        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn(MISSING_CONTENT_WITH_REASONING_JSON);

        HttpResponse<String> response3 = mock(HttpResponse.class);
        when(response3.statusCode()).thenReturn(200);
        when(response3.body()).thenReturn(CONTENT_PRESENT_JSON);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response1)
                .thenReturn(response2)
                .thenReturn(response3);

        OpenRouterClient client = new OpenRouterClient(model, mockHttpClient);

        // When
        String result = client.generate("system", "user");

        // Then: exactly 3 HTTP calls
        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any());
        assertNotNull(result);
        assertTrue(result.contains("english"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void generate_all3AttemptsNullContentWithReasoning_throwsAfter3Calls_noReasoningReturned() throws Exception {
        // Given all 3 attempts return null content + reasoning
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(MISSING_CONTENT_WITH_REASONING_JSON);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response, response, response);

        OpenRouterClient client = new OpenRouterClient(model, mockHttpClient);

        // When / Then
        AiClientException ex = assertThrows(AiClientException.class,
                () -> client.generate("system", "user"));

        assertEquals("MISSING_CONTENT_WITH_REASONING", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("3 attempts"), "Should mention 3 attempts");
        assertTrue(ex.getMessage().contains("reasoning"), "Should mention reasoning");

        // Exactly 3 HTTP calls
        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void generate_reasoningTextNeverReturnedAsContent() throws Exception {
        // Given response has reasoning that looks like JSON, but content is null
        String reasoningOnlyJson = """
            {
              "choices": [{
                "finish_reason": "stop",
                "message": {
                  "role": "assistant",
                  "content": null,
                  "reasoning": "{\\"english\\":{\\"minimal\\":{\\"professionalTitle\\":\\"Engineer\\"}}}"
                }
              }]
            }
            """;

        // First attempt: reasoning-only (triggers MISSING_CONTENT_WITH_REASONING)
        HttpResponse<String> badResponse = mock(HttpResponse.class);
        when(badResponse.statusCode()).thenReturn(200);
        when(badResponse.body()).thenReturn(reasoningOnlyJson);

        // All 3 attempts return reasoning-only
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(badResponse, badResponse, badResponse);

        OpenRouterClient client = new OpenRouterClient(model, mockHttpClient);

        // When / Then: must throw, never return reasoning content
        AiClientException ex = assertThrows(AiClientException.class,
                () -> client.generate("system", "user"));

        assertEquals("MISSING_CONTENT_WITH_REASONING", ex.getErrorCode());
        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any());

        // The reasoning text must NOT appear in the exception message as content
        // (the message may mention "reasoning" generically but never the reasoning payload)
        assertFalse(ex.getMessage().contains("Engineer"),
                "Reasoning text must never leak into AiClientException message");
    }

    @SuppressWarnings("unchecked")
    @Test
    void generate_blankContentWithReasoning_retriesAsIfNull() throws Exception {
        // Given attempt 1 has blank content + reasoning, attempt 2 valid
        HttpResponse<String> response1 = mock(HttpResponse.class);
        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn(MISSING_CONTENT_BLANK_WITH_REASONING_JSON);

        HttpResponse<String> response2 = mock(HttpResponse.class);
        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn(CONTENT_PRESENT_JSON);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response1)
                .thenReturn(response2);

        OpenRouterClient client = new OpenRouterClient(model, mockHttpClient);

        // When
        String result = client.generate("system", "user");

        // Then: blank content triggers retry (treated as null), succeeds on attempt 2
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any());
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void generate_nonRetryableError_doesNotRetry() throws Exception {
        // Given OpenRouter returns HTTP 500 error immediately
        HttpResponse<String> errorResponse = mock(HttpResponse.class);
        when(errorResponse.statusCode()).thenReturn(500);
        when(errorResponse.body()).thenReturn("");

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(errorResponse);

        OpenRouterClient client = new OpenRouterClient(model, mockHttpClient);

        // When / Then: non-200 errors are NOT retried
        assertThrows(AiClientException.class, () -> client.generate("system", "user"));

        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void generate_missingContentNoReasoning_doesNotRetry() throws Exception {
        // Given content is null and NO reasoning exists (plain malformed response)
        String json = """
            {
              "choices": [{
                "message": {
                  "content": null
                }
              }]
            }
            """;

        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(json);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        OpenRouterClient client = new OpenRouterClient(model, mockHttpClient);

        // When / Then: no reasoning → don't retry, fail immediately
        assertThrows(AiClientException.class, () -> client.generate("system", "user"));
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any());
    }
}
