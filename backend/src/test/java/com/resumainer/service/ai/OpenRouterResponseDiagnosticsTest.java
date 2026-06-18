package com.resumainer.service.ai;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OpenRouter raw response diagnostic logging helpers.
 * These test the shape extraction and dev-only gating logic
 * WITHOUT calling real OpenRouter.
 */
class OpenRouterResponseDiagnosticsTest {

    // ── Environment save/restore ──────────────────────────────────────

    private String originalProfiles;
    private String originalDebugFlag;

    @BeforeEach
    void saveEnv() {
        originalProfiles = System.getenv("SPRING_PROFILES_ACTIVE");
        originalDebugFlag = System.getenv("AI_DEBUG_OPENROUTER_RAW_RESPONSE");
    }

    @AfterEach
    void restoreEnv() {
        restoreEnvVar("SPRING_PROFILES_ACTIVE", originalProfiles);
        restoreEnvVar("AI_DEBUG_OPENROUTER_RAW_RESPONSE", originalDebugFlag);
    }

    private void restoreEnvVar(String name, String value) {
        // Cannot truly set env vars in Java — tests use inline-reflective helpers
        // from the diagnostic methods that read System.getenv().
        // For testability, the diagnostic methods accept overridable values
        // via a package-private testing interface.
    }

    // ── Shape extraction tests ────────────────────────────────────────

    @Test
    void extractShape_normalResponse_hasChoicesAndContent() {
        String json = """
            {
              "choices": [
                {
                  "finish_reason": "stop",
                  "message": {
                    "role": "assistant",
                    "content": "{\\"english\\":{\\"minimal\\":{}}}"
                  }
                }
              ]
            }
            """;

        ResponseShape shape = ResponseShape.fromJson(json);

        assertNotNull(shape, "Shape should not be null");
        assertTrue(shape.jsonParseable, "Should be parseable JSON");
        assertTrue(shape.hasChoices, "Should have choices");
        assertEquals(1, shape.choicesCount, "Should have 1 choice");
        assertTrue(shape.hasFirstChoice, "Should have first choice");
        assertEquals("stop", shape.finishReason, "Finish reason should be stop");
        assertTrue(shape.hasMessage, "First choice should have message");
        assertNotNull(shape.messageKeys, "messageKeys should not be null");
        assertTrue(shape.messageKeys.contains("role"), "Should have role key");
        assertTrue(shape.messageKeys.contains("content"), "Should have content key");
        assertTrue(shape.hasContent, "Should have content");
        assertTrue(shape.contentLength > 0, "Content length should be > 0");
        assertFalse(shape.hasError, "Should not have error");
        assertNull(shape.errorKeys, "Error keys should be null when no error");
    }

    @Test
    void extractShape_missingContent_hasMessageButNoContent() {
        String json = """
            {
              "choices": [
                {
                  "finish_reason": "stop",
                  "message": {
                    "role": "assistant"
                  }
                }
              ]
            }
            """;

        ResponseShape shape = ResponseShape.fromJson(json);

        assertTrue(shape.jsonParseable);
        assertTrue(shape.hasChoices);
        assertEquals(1, shape.choicesCount);
        assertTrue(shape.hasMessage);
        assertTrue(shape.messageKeys.contains("role"));
        assertFalse(shape.messageKeys.contains("content"), "message should not have content key");
        assertFalse(shape.hasContent);
        assertEquals(0, shape.contentLength);
    }

    @Test
    void extractShape_openRouterErrorBody_hasError() {
        String json = """
            {
              "error": {
                "message": "Provider returned error",
                "code": 500
              }
            }
            """;

        ResponseShape shape = ResponseShape.fromJson(json);

        assertTrue(shape.jsonParseable);
        assertTrue(shape.hasError);
        assertNotNull(shape.errorKeys);
        assertTrue(shape.errorKeys.contains("message"));
        assertTrue(shape.errorKeys.contains("code"));
        assertFalse(shape.hasChoices);
        assertEquals(0, shape.choicesCount);
    }

    @Test
    void extractShape_invalidJson_notParseable() {
        String body = "not valid json at all {{{";

        ResponseShape shape = ResponseShape.fromJson(body);

        assertNotNull(shape);
        assertFalse(shape.jsonParseable);
        assertTrue(shape.bodyLength > 0, "bodyLength should reflect input length");
        assertEquals(body.length(), shape.bodyLength);
        // No exception should have been thrown
    }

    @Test
    void extractShape_emptyChoicesArray_noFirstChoice() {
        String json = """
            {
              "choices": []
            }
            """;

        ResponseShape shape = ResponseShape.fromJson(json);

        assertTrue(shape.jsonParseable);
        assertTrue(shape.hasChoices);
        assertEquals(0, shape.choicesCount);
        assertFalse(shape.hasFirstChoice, "no first choice when array is empty");
    }

    @Test
    void extractShape_nullContent_hasContentFalse() {
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

        ResponseShape shape = ResponseShape.fromJson(json);

        assertTrue(shape.hasChoices);
        assertEquals(1, shape.choicesCount);
        assertTrue(shape.hasMessage);
        assertFalse(shape.hasContent, "content is null — hasContent should be false");
        assertEquals(0, shape.contentLength);
    }

    @Test
    void extractShape_blankContent_hasContentTrueButZeroLength() {
        String json = """
            {
              "choices": [
                {
                  "message": {
                    "content": ""
                  }
                }
              ]
            }
            """;

        ResponseShape shape = ResponseShape.fromJson(json);

        assertTrue(shape.hasContent);
        assertEquals(0, shape.contentLength);
    }

    @Test
    void extractShape_noChoicesField_noErrorField() {
        String json = """
            {
              "id": "chatcmpl-123",
              "object": "chat.completion",
              "created": 1234567890
            }
            """;

        ResponseShape shape = ResponseShape.fromJson(json);

        assertTrue(shape.jsonParseable);
        assertFalse(shape.hasChoices);
        assertEquals(0, shape.choicesCount);
        assertFalse(shape.hasError);
    }

    @Test
    void shape_neverThrows() {
        // Even with null input, shape extraction should not throw
        ResponseShape shape = ResponseShape.fromJson(null);
        assertNotNull(shape);
        assertFalse(shape.jsonParseable);

        // Even empty string should not throw
        shape = ResponseShape.fromJson("");
        assertNotNull(shape);
        assertFalse(shape.jsonParseable);
        assertEquals(0, shape.bodyLength);
    }

    // ── Dev-profile gate tests ────────────────────────────────────────

    @Test
    void devProfileDetection_acceptsExactDev() {
        assertTrue(OpenRouterDevDiagnostics.isDevProfile("dev"));
        assertTrue(OpenRouterDevDiagnostics.isDevProfile("dev,docker"));
        assertTrue(OpenRouterDevDiagnostics.isDevProfile("docker,dev"));
        assertTrue(OpenRouterDevDiagnostics.isDevProfile("docker,dev,other"));
    }

    @Test
    void devProfileDetection_rejectsNonDev() {
        assertFalse(OpenRouterDevDiagnostics.isDevProfile("prod"));
        assertFalse(OpenRouterDevDiagnostics.isDevProfile("staging"));
        assertFalse(OpenRouterDevDiagnostics.isDevProfile("development"));
        assertFalse(OpenRouterDevDiagnostics.isDevProfile(""));
        assertFalse(OpenRouterDevDiagnostics.isDevProfile(null));
    }

    @Test
    void devProfileDetection_caseSensitive() {
        // "DEV" or "Dev" should NOT match — exact "dev" only
        assertFalse(OpenRouterDevDiagnostics.isDevProfile("DEV"));
        assertFalse(OpenRouterDevDiagnostics.isDevProfile("Dev"));
        assertFalse(OpenRouterDevDiagnostics.isDevProfile("prod,DEV"));
    }

    @Test
    void debugFlagDetection_caseInsensitive() {
        assertTrue(OpenRouterDevDiagnostics.isDebugFlagEnabled("true"));
        assertTrue(OpenRouterDevDiagnostics.isDebugFlagEnabled("TRUE"));
        assertTrue(OpenRouterDevDiagnostics.isDebugFlagEnabled("True"));
        assertFalse(OpenRouterDevDiagnostics.isDebugFlagEnabled("false"));
        assertFalse(OpenRouterDevDiagnostics.isDebugFlagEnabled("FALSE"));
        assertFalse(OpenRouterDevDiagnostics.isDebugFlagEnabled(null));
        assertFalse(OpenRouterDevDiagnostics.isDebugFlagEnabled(""));
        assertFalse(OpenRouterDevDiagnostics.isDebugFlagEnabled("1"));
    }

    // ── Logging method smoke tests ────────────────────────────────
    // These verify log methods don't throw. In test env SPRING_PROFILES_ACTIVE
    // is null, so the guard (isRawResponseLoggingEnabled) returns false early.
    // The env-dependent wrapper methods (isDevProfileActive, isDebugFlagEnabled,
    // isRawResponseLoggingEnabled) cannot be fully covered without env var
    // manipulation, which is intentionally avoided for stability.

    @Test
    void logRawResponse_doesNotThrow_whenGuardReturnsFalse() {
        // SPRING_PROFILES_ACTIVE is null in test → guard returns early
        OpenRouterDevDiagnostics.logRawResponse("deepseek/deepseek-v4", 200, "{}");
        OpenRouterDevDiagnostics.logRawResponse("model", 500, null);
    }

    @Test
    void logResponseShape_doesNotThrow_withVariousInputs() {
        // Always safe — shape extraction never throws per ResponseShape contract
        OpenRouterDevDiagnostics.logResponseShape("model", 200, "{}");
        OpenRouterDevDiagnostics.logResponseShape("model", 500, null);
        OpenRouterDevDiagnostics.logResponseShape("model", 200, "invalid json{{{");
        OpenRouterDevDiagnostics.logResponseShape(null, 0, "");
    }
}
