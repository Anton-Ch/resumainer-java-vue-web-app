package com.resumainer.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Lightweight shape summary of an OpenRouter response.
 * Extracted for diagnostic logging — never throws.
 *
 * <p>Security: contains only structural metadata, never the full content.
 */
class ResponseShape {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    final boolean jsonParseable;
    final int bodyLength;

    final boolean hasChoices;
    final int choicesCount;
    final boolean hasFirstChoice;
    final String finishReason;
    final boolean hasMessage;
    final List<String> messageKeys;
    final boolean hasContent;
    final int contentLength;
    final boolean hasError;
    final List<String> errorKeys;

    private ResponseShape(boolean jsonParseable, int bodyLength,
                          boolean hasChoices, int choicesCount, boolean hasFirstChoice,
                          String finishReason, boolean hasMessage, List<String> messageKeys,
                          boolean hasContent, int contentLength,
                          boolean hasError, List<String> errorKeys) {
        this.jsonParseable = jsonParseable;
        this.bodyLength = bodyLength;
        this.hasChoices = hasChoices;
        this.choicesCount = choicesCount;
        this.hasFirstChoice = hasFirstChoice;
        this.finishReason = finishReason;
        this.hasMessage = hasMessage;
        this.messageKeys = messageKeys;
        this.hasContent = hasContent;
        this.contentLength = contentLength;
        this.hasError = hasError;
        this.errorKeys = errorKeys;
    }

    /**
     * Extracts a response shape summary from a raw response body.
     * Never throws — returns a best-effort shape even for invalid JSON.
     */
    static ResponseShape fromJson(String responseBody) {
        int bodyLength = responseBody != null ? responseBody.length() : 0;

        if (responseBody == null || responseBody.isBlank()) {
            return new ResponseShape(false, bodyLength,
                    false, 0, false, null, false, null,
                    false, 0, false, null);
        }

        try {
            JsonNode root = MAPPER.readTree(responseBody);

            // Choices
            JsonNode choices = root.path("choices");
            boolean hasChoices = choices.isArray();
            int choicesCount = hasChoices ? choices.size() : 0;
            boolean hasFirstChoice = choicesCount > 0;
            String finishReason = null;
            boolean hasMessage = false;
            List<String> messageKeys = null;
            boolean hasContent = false;
            int contentLength = 0;

            if (hasFirstChoice) {
                JsonNode firstChoice = choices.get(0);
                JsonNode fr = firstChoice.path("finish_reason");
                finishReason = fr.isMissingNode() || fr.isNull() ? null : fr.asText();

                JsonNode message = firstChoice.path("message");
                hasMessage = !message.isMissingNode() && !message.isNull();

                if (hasMessage) {
                    messageKeys = new ArrayList<>();
                    Iterator<Map.Entry<String, JsonNode>> fields = message.fields();
                    while (fields.hasNext()) {
                        messageKeys.add(fields.next().getKey());
                    }
                    JsonNode content = message.path("content");
                    hasContent = !content.isMissingNode() && !content.isNull();
                    contentLength = hasContent ? content.asText().length() : 0;
                }
            }

            // Error
            JsonNode error = root.path("error");
            boolean hasError = !error.isMissingNode() && !error.isNull();
            List<String> errorKeys = null;
            if (hasError) {
                errorKeys = new ArrayList<>();
                Iterator<Map.Entry<String, JsonNode>> fields = error.fields();
                while (fields.hasNext()) {
                    errorKeys.add(fields.next().getKey());
                }
            }

            return new ResponseShape(true, bodyLength,
                    hasChoices, choicesCount, hasFirstChoice,
                    finishReason, hasMessage, messageKeys,
                    hasContent, contentLength,
                    hasError, errorKeys);

        } catch (Exception e) {
            return new ResponseShape(false, bodyLength,
                    false, 0, false, null, false, null,
                    false, 0, false, null);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "jsonParseable=%s bodyLength=%d hasChoices=%s choicesCount=%d hasFirstChoice=%s "
                + "finishReason=%s hasMessage=%s messageKeys=%s hasContent=%s contentLength=%d "
                + "hasError=%s errorKeys=%s",
                jsonParseable, bodyLength,
                hasChoices, choicesCount, hasFirstChoice,
                finishReason != null ? finishReason : "null",
                hasMessage, messageKeys,
                hasContent, contentLength,
                hasError, errorKeys);
    }
}
