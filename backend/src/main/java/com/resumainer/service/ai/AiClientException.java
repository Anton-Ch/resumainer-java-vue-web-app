package com.resumainer.service.ai;

/**
 * Exception thrown by AiClient implementations when the AI provider
 * returns an error, times out, or the call otherwise fails.
 * The message should be user-readable (no stack traces or API keys).
 */
public class AiClientException extends RuntimeException {

    public AiClientException(String message) {
        super(message);
    }

    public AiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
