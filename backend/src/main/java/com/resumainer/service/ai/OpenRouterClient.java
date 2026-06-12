package com.resumainer.service.ai;

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

    private static final Duration TIMEOUT = Duration.ofSeconds(120);

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

            if (response.statusCode() != 200) {
                log.warn("OpenRouter returned HTTP {}: {}", response.statusCode(), response.body());
                throw new AiClientException("AI provider returned an error. Please try again or choose a different model.");
            }

            String responseBody = response.body();
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

    private String buildRequestBody(String systemPrompt, String requestPrompt) {
        return "{\"model\":\"" + escapeJson(model.getModelCode()) + "\","
                + "\"temperature\":0.2,"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(requestPrompt) + "\"}"
                + "]}";
    }

    private String extractContent(String responseBody) {
        // Simple JSON parse: extract choices[0].message.content
        // Uses basic string operations to avoid Jackson dependency for this one call
        try {
            // Find "content" field in the first choice
            int choicesIdx = responseBody.indexOf("\"choices\"");
            if (choicesIdx < 0) throw new AiClientException("Unexpected AI response format.");

            int messageIdx = responseBody.indexOf("\"message\"", choicesIdx);
            int contentIdx = responseBody.indexOf("\"content\"", messageIdx);
            int colonIdx = responseBody.indexOf(':', contentIdx);
            int startIdx = responseBody.indexOf('"', colonIdx + 1) + 1;
            int endIdx = responseBody.indexOf('"', startIdx);

            String content = responseBody.substring(startIdx, endIdx);
            // Unescape JSON string
            content = content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
            return content;

        } catch (Exception e) {
            log.warn("Failed to parse AI response JSON");
            throw new AiClientException("Failed to process AI response. Please try again.");
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
