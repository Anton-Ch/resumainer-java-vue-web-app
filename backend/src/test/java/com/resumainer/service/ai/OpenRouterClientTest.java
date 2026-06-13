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
}
