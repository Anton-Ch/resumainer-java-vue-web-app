package com.resumainer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration request DTO.
 * Validated via Jakarta Validation on controller entry.
 */
public class RegisterRequest {

    @NotBlank(message = "{auth.email.required}")
    @Email(message = "{auth.email.invalid}")
    @Size(max = 255, message = "{auth.email.tooLong}")
    private String email;

    @NotBlank(message = "{auth.password.required}")
    @Size(min = 8, max = 128, message = "{auth.password.length}")
    private String password;

    @NotBlank(message = "{auth.passwordConfirmation.required}")
    private String passwordConfirmation;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String passwordConfirmation) {
        this.email = email;
        this.password = password;
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }
}
