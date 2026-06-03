package com.resumainer.exception;

/**
 * Exception thrown by service layer for business logic failures.
 * Carries a user-facing error code for i18n message resolution.
 */
public class ServiceException extends RuntimeException {

    private final String errorCode;

    public ServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
