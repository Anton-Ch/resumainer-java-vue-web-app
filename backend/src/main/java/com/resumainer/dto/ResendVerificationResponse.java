package com.resumainer.dto;

/** Exact anti-enumeration-safe success response for verification resend. */
public record ResendVerificationResponse(boolean success, String message) {

    public static ResendVerificationResponse genericSuccess() {
        return new ResendVerificationResponse(true,
                "If this account needs verification, a new verification email has been sent.");
    }
}
