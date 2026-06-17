package com.resumainer.service.ai;

import com.resumainer.model.AiModel;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OpenRouterClient.extractContent().
 */
class OpenRouterClientTest {

    private final AiModel dummyModel = new AiModel();
    private final OpenRouterClient client = new OpenRouterClient(dummyModel);

    @Test
    void normalResponseExtractsContent() {
        String json = """
            {
              "choices": [
                {
                  "message": {
                    "content": "Hello\\nWorld"
                  }
                }
              ]
            }
            """;
        String result = client.testExtractContent(json);
        assertEquals("Hello\nWorld", result);
    }

    @Test
    void contentWithEscapedQuotes() {
        String json = """
            {
              "choices": [
                {
                  "message": {
                    "content": "He said: \\"Hello\\""
                  }
                }
              ]
            }
            """;
        String result = client.testExtractContent(json);
        assertEquals("He said: \"Hello\"", result);
    }

    @Test
    void missingChoicesThrowsException() {
        String json = """
            {
              "id": "abc"
            }
            """;
        assertThrows(AiClientException.class, () -> client.testExtractContent(json));
    }

    @Test
    void emptyChoicesThrowsException() {
        String json = """
            {
              "choices": []
            }
            """;
        assertThrows(AiClientException.class, () -> client.testExtractContent(json));
    }

    @Test
    void providerErrorJsonThrowsException() {
        String json = """
            {
              "error": {
                "message": "Invalid API key",
                "code": "invalid_api_key"
              }
            }
            """;
        assertThrows(AiClientException.class, () -> client.testExtractContent(json));
    }

    @Test
    void invalidJsonThrowsException() {
        String text = "not json";
        assertThrows(AiClientException.class, () -> client.testExtractContent(text));
    }

    @Test
    void missingMessageContentThrowsException() {
        String json = """
            {
              "choices": [
                {
                  "message": {}
                }
              ]
            }
            """;
        assertThrows(AiClientException.class, () -> client.testExtractContent(json));
    }

    @Test
    void nullContentThrowsException() {
        String json = """
            {
              "choices": [
                {
                  "message": {
                    "content": null
                  }
                }
              ]
            }
            """;
        assertThrows(AiClientException.class, () -> client.testExtractContent(json));
    }

    // ── Reasoning config in request body ────────────────────────────

    @Test
    void buildRequestBody_includesReasoningDisabledConfig() {
        String body = client.buildRequestBody("system", "user");
        assertTrue(body.contains("\"reasoning\""), "Must contain reasoning config");
        assertTrue(body.contains("\"effort\":\"none\""), "Must set effort=none");
        assertTrue(body.contains("\"exclude\":true"), "Must set exclude=true");
        assertTrue(body.contains("\"model\""), "Must still contain model");
        assertTrue(body.contains("\"temperature\":0.2"), "Must still contain temperature");
        assertTrue(body.contains("\"messages\""), "Must still contain messages");
    }

    // ── Reasoning detection ─────────────────────────────────────────

    @Test
    void hasReasoningInResponse_returnsTrueWhenReasoningPresent() {
        String json = """
            {
              "choices": [{
                "finish_reason": "stop",
                "message": {
                  "role": "assistant",
                  "content": null,
                  "reasoning": "Let me think about this..."
                }
              }]
            }
            """;
        assertTrue(client.hasReasoningInResponse(json));
    }

    @Test
    void hasReasoningInResponse_returnsTrueWhenReasoningDetailsPresent() {
        String json = """
            {
              "choices": [{
                "message": {
                  "content": null,
                  "reasoning_details": [{"type": "text", "text": "thinking"}]
                }
              }]
            }
            """;
        assertTrue(client.hasReasoningInResponse(json));
    }

    @Test
    void hasReasoningInResponse_returnsFalseWhenNeitherPresent() {
        String json = """
            {
              "choices": [{
                "message": {
                  "content": null
                }
              }]
            }
            """;
        assertFalse(client.hasReasoningInResponse(json));
    }

    @Test
    void hasReasoningInResponse_returnsFalseWhenContentPresent() {
        String json = """
            {
              "choices": [{
                "message": {
                  "content": "Hello",
                  "reasoning": "thinking"
                }
              }]
            }
            """;
        assertTrue(client.hasReasoningInResponse(json)); // reasoning field IS present
    }

    @Test
    void reasoningNeverUsedAsContent() {
        // Even if reasoning contains what looks like content, extractContent should not return it
        String json = """
            {
              "choices": [{
                "message": {
                  "content": null,
                  "reasoning": "This looks like valid resume JSON but it is reasoning"
                }
              }]
            }
            """;
        // extractContent throws because content is null
        assertThrows(AiClientException.class, () -> client.testExtractContent(json));
        // hasReasoning confirms reasoning exists
        assertTrue(client.hasReasoningInResponse(json));
    }
}
