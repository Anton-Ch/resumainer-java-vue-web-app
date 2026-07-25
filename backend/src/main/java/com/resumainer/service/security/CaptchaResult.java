package com.resumainer.service.security;

import java.util.Objects;

/**
 * Result of a captcha verification.
 * Immutable value object.
 */
public final class CaptchaResult {

    private static final CaptchaResult SUCCESS = new CaptchaResult(true, null);

    private final boolean success;
    private final String errorCode;

    private CaptchaResult(boolean success, String errorCode) {
        this.success = success;
        this.errorCode = errorCode;
    }

    /** Returns a success result. */
    public static CaptchaResult success() {
        return SUCCESS;
    }

    /** Returns a failure result with the given error code. */
    public static CaptchaResult failure(String errorCode) {
        Objects.requireNonNull(errorCode, "errorCode must not be null for failure");
        return new CaptchaResult(false, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CaptchaResult that)) return false;
        return success == that.success && Objects.equals(errorCode, that.errorCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, errorCode);
    }

    @Override
    public String toString() {
        return "CaptchaResult{success=" + success
                + (errorCode != null ? ", errorCode=" + errorCode : "")
                + '}';
    }
}
