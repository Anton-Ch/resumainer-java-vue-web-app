package com.resumainer.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.resumainer.model.AiModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Real AI client that calls OpenRouter API.
 * Uses java.net.http.HttpClient (Java 21 standard library, no extra deps).
 * Never logs the API key.
 */
public class OpenRouterClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterClient.class);

    // Increased from 120s to 240s because DeepSeek V4 Flash
    // can take 2-4 minutes for complex bilingual+all requests.
    private static final Duration TIMEOUT = Duration.ofSeconds(240);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AiModel model;
    private final HttpClient httpClient;

    public OpenRouterClient(AiModel model) {
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String generate(String systemPrompt, String requestPrompt) throws AiClientException {
        String requestBody = buildRequestBody(systemPrompt, requestPrompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(model.getProviderApiUrl()))
                .header("Authorization", "Bearer " + model.getApiKeyEncrypted())
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "https://resumainer.local")
                .header("X-Title", "ResumAIner")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            log.debug("Calling OpenRouter model: {}", model.getModelCode());
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();

            // Try to detect provider error JSON even on non-200
            if (response.statusCode() != 200) {
                String errorMessage = extractErrorMessage(responseBody);
                log.warn("OpenRouter returned HTTP {}: error={}", response.statusCode(), errorMessage);
                throw new AiClientException(
                        "AI provider returned an error. Please try again or choose a different model.",
                        "AI_PROVIDER_ERROR");
            }

            return extractContent(responseBody);

        } catch (AiClientException e) {
            throw e;
        } catch (java.net.http.HttpTimeoutException e) {
            log.warn("OpenRouter timeout for model: {}", model.getModelCode());
            throw new AiClientException("AI provider did not respond in time. Please try again.");
        } catch (java.io.InterruptedIOException e) {
            Thread.currentThread().interrupt();
            throw new AiClientException("Request was interrupted. Please try again.");
        } catch (Exception e) {
            log.warn("OpenRouter call failed for model: {} — {}", model.getModelCode(), e.getMessage());
            throw new AiClientException("Failed to contact AI provider. Please try again.");
        }
    }

    /**
     * Builds the OpenRouter request body using Jackson ObjectNode/ArrayNode.
     */
    private String buildRequestBody(String systemPrompt, String requestPrompt) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", model.getModelCode());
        root.put("temperature", 0.2);

        ArrayNode messages = root.putArray("messages");

        ObjectNode system = messages.addObject();
        system.put("role", "system");
        system.put("content", systemPrompt);

        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", requestPrompt);

        return root.toString();
    }

    /**
     * Extracts error message from OpenRouter error response JSON.
     */
    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = MAPPER.readTree(responseBody);
            JsonNode error = root.path("error");
            if (!error.isMissingNode() && !error.isNull()) {
                return error.path("message").asText("Unknown provider error");
            }
        } catch (Exception ignored) {
            // Unable to parse error JSON — use default message
        }
        return "Unknown provider error";
    }

    /**
     * Extracts choices[0].message.content from a successful OpenRouter response.
     * Uses Jackson only — no manual string parsing.
     */
    private String extractContent(String responseBody) {
        try {
            JsonNode root = MAPPER.readTree(responseBody);

            // Check for provider error in success response
            JsonNode error = root.path("error");
            if (!error.isMissingNode() && !error.isNull()) {
                String errorMessage = error.path("message").asText("AI provider returned an error.");
                log.warn("OpenRouter returned error in 200 response: {}", errorMessage);
                throw new AiClientException(
                        "AI provider returned an error. Please try again or choose another model.",
                        "AI_PROVIDER_ERROR");
            }

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                log.warn("OpenRouter response missing non-empty choices array");
                throw new AiClientException("Unexpected AI response format.");
            }

            JsonNode firstChoice = choices.get(0);
            JsonNode content = firstChoice.path("message").path("content");

            if (content.isMissingNode() || content.isNull()) {
                log.warn("OpenRouter response missing choices[0].message.content");
                throw new AiClientException("Unexpected AI response format.");
            }

            if (!content.isTextual()) {
                log.warn("OpenRouter response content is not textual: nodeType={}", content.getNodeType());
                throw new AiClientException("Unexpected AI response format.");
            }

            String text = content.asText();
            if (text == null || text.isBlank()) {
                log.warn("OpenRouter response content is blank");
                throw new AiClientException("AI response was empty. Please try again.");
            }

            return text;

        } catch (AiClientException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to parse OpenRouter response JSON: {}", e.getMessage());
            throw new AiClientException("Failed to process AI response. Please try again.");
        }
    }

    /**
     * Package-private for testing — allows tests to call extractContent without
     * making it public.
     */
    String testExtractContent(String responseBody) {
        return extractContent(responseBody);
    }
}
