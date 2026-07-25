package com.resumainer.dto;

/**
 * Authentication response DTO — returned to frontend after login/register/logout.
 */
public class AuthResponse {

    private boolean success;
    private String code;
    private String role;
    private String message;
    private String redirectUrl;

    public AuthResponse() {
    }

    public AuthResponse(boolean success, String code, String role, String message, String redirectUrl) {
        this.success = success;
        this.code = code;
        this.role = role;
        this.message = message;
        this.redirectUrl = redirectUrl;
    }

    /**
     * Factory for successful authentication response.
     */
    public static AuthResponse success(String role, String redirectUrl) {
        return new AuthResponse(true, null, role, null, redirectUrl);
    }

    /**
     * Factory for registration-pending-verification response.
     */
    public static AuthResponse pendingVerification() {
        return new AuthResponse(true, "REGISTRATION_PENDING_EMAIL_VERIFICATION", null,
                "Please check your email to verify your account.", "/auth/check-email");
    }

    /**
     * Factory for failed authentication response with public error code.
     *
     * @param code    the public error code from the API contract (e.g. CAPTCHA_INVALID)
     * @param message user-facing error message
     * @return AuthResponse with success=false, the given code and message
     */
    public static AuthResponse failure(String code, String message) {
        return new AuthResponse(false, code, null, message, null);
    }

    // Note: legacy failure(String) factory was removed in Phase 10.
    // All auth failures must use failure(String code, String message).

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
