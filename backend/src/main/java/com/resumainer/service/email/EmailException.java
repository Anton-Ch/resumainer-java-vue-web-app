package com.resumainer.service.email;

/**
 * Exception thrown when email sending fails.
 *
 * <p>Runtime exception so callers can choose to handle or propagate.
 * Message must NEVER contain the Resend API key or other secrets.
 */
public class EmailException extends RuntimeException {

    public EmailException(String message) {
        super(message);
    }

    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
