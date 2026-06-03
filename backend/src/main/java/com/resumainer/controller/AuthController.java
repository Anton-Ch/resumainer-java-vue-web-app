package com.resumainer.controller;

import com.resumainer.dto.AuthResponse;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.dto.UserSession;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.User;
import com.resumainer.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * <p>
 * Handles user registration, login, logout, and session status checks.
 * Session-based authentication: user data stored in HttpSession after login/register.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     * <p>
     * Validates input, creates user + profile, starts session.
     *
     * @param request the registration request (validated via @Valid)
     * @param session the HTTP session (for auto-login after registration)
     * @return AuthResponse with role and redirect URL
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpSession session) {

        log.info("Registration attempt for email: {}", request.getEmail());

        try {
            User user = authService.register(request);

            if (user == null || user.getId() == null) {
                log.error("Registration returned null user for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(AuthResponse.failure("Registration failed"));
            }

            // Auto-login: create session
            UserSession userSession = new UserSession(
                    user.getId(), user.getEmail(), "USER");
            session.setAttribute("user", userSession);

            log.info("User registered and logged in: {}", user.getEmail());
            return ResponseEntity.ok(AuthResponse.success("USER", "/home"));

        } catch (ServiceException e) {
            log.warn("Registration failed for {}: {}", request.getEmail(), e.getMessage());

            HttpStatus status = switch (e.getErrorCode()) {
                case "auth.email.alreadyRegistered" -> HttpStatus.CONFLICT;
                case "auth.password.weak", "auth.password.mismatch" -> HttpStatus.BAD_REQUEST;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };

            return ResponseEntity.status(status)
                    .body(AuthResponse.failure(e.getMessage()));
        }
    }
}
