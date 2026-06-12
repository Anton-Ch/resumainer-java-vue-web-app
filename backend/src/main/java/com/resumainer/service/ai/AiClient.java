package com.resumainer.service.ai;

/**
 * Abstraction over AI providers (OpenRouter, mock).
 * Keeps generation logic independent of the specific AI provider.
 */
public interface AiClient {

    /**
     * Sends prompts to the AI provider and returns the response text.
     *
     * @param systemPrompt  the system-level instructions
     * @param requestPrompt the user/vacancy-specific prompt
     * @return the raw response text from the AI provider
     * @throws AiClientException if the provider returns an error or the call fails
     */
    String generate(String systemPrompt, String requestPrompt) throws AiClientException;
}
