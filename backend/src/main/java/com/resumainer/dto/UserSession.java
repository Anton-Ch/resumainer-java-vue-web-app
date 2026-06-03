package com.resumainer.dto;

import java.util.UUID;

/**
 * User session data stored in HttpSession after authentication.
 * Not a DTO in the REST sense — it's the server-side session object.
 */
public class UserSession {

    private UUID userId;
    private String email;
    private String role;

    public UserSession() {
    }

    public UserSession(UUID userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserSession{userId=" + userId + ", email='" + email + "', role='" + role + "'}";
    }
}
