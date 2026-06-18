package com.resumainer.service.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MockAiClient.
 * Verifies generate() returns a valid non-empty JSON string.
 */
class MockAiClientTest {

    private final MockAiClient client = new MockAiClient();

    @Test
    void generate_returnsNonEmptyResponse() throws AiClientException {
        String response = client.generate("system prompt", "request prompt");

        assertNotNull(response);
        assertFalse(response.isBlank());
    }

    @Test
    void generate_returnsValidJsonWithExpectedFields() throws AiClientException {
        String response = client.generate("system", "request");

        assertTrue(response.contains("professionalTitle"));
        assertTrue(response.contains("workExperience"));
        assertTrue(response.contains("skills"));
        assertTrue(response.contains("personalInfo"));
        assertTrue(response.contains("Senior Java Developer"));
    }

    @Test
    void generate_ignoresPrompts_returnsSameResponse() throws AiClientException {
        String r1 = client.generate("any", "prompt");
        String r2 = client.generate("different", "inputs");

        assertEquals(r1, r2);
    }
}
