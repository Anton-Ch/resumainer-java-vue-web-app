package com.resumainer.dto;

/**
 * Authentication response DTO — returned to frontend after login/register/logout.
 */
public class AuthResponse {

    private boolean success;
    private String role;
    private String message;
    private String redirectUrl;

    public AuthResponse() {
    }

    public AuthResponse(boolean success, String role, String message, String redirectUrl) {
        this.success = success;
        this.role = role;
        this.message = message;
        this.redirectUrl = redirectUrl;
    }

    /**
     * Factory for successful authentication response.
     */
    public static AuthResponse success(String role, String redirectUrl) {
        return new AuthResponse(true, role, null, redirectUrl);
    }

    /**
     * Factory for failed authentication response.
     */
    public static AuthResponse failure(String message) {
        return new AuthResponse(false, null, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
