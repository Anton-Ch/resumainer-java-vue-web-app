package com.resumainer.service.ai;

/**
 * Exception thrown by AiClient implementations when the AI provider
 * returns an error, times out, or the call otherwise fails.
 * The message should be user-readable (no stack traces or API keys).
 */
public class AiClientException extends RuntimeException {

    private final String errorCode;

    public AiClientException(String message) {
        super(message);
        this.errorCode = "GENERATION_FAILED";
    }

    public AiClientException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiClientException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERATION_FAILED";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
