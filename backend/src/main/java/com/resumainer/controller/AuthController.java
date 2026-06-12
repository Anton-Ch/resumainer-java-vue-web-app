package com.resumainer.controller;

import com.resumainer.dto.AuthResponse;
import com.resumainer.dto.LoginRequest;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.dto.UserSession;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.User;
import com.resumainer.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
                    user.getId(), user.getEmail(), "USER", user.isPrivileged());
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

    /**
     * Log in an existing user.
     * <p>
     * Validates credentials, invalidates old session (session fixation prevention),
     * creates new authenticated session, returns role-based redirect.
     *
     * @param request the login request (email, password, rememberMe)
     * @param session the HTTP session
     * @return AuthResponse with role and redirect URL
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {

        log.info("Login attempt for email: {}", request.getEmail());

        try {
            User user = authService.authenticate(request);

            // Session regeneration (SEC-002): prevent session fixation
            HttpSession oldSession = servletRequest.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession newSession = servletRequest.getSession(true);

            // Determine role-based redirect
            String role = user.getRoleId() == 2L ? "ADMIN" : "USER";
            String redirectUrl = role.equals("ADMIN") ? "/admin" : "/home";

            UserSession userSession = new UserSession(
                    user.getId(), user.getEmail(), role, user.isPrivileged());
            newSession.setAttribute("user", userSession);

            // Remember me: extend session TTL to 7 days
            if (request.isRememberMe()) {
                newSession.setMaxInactiveInterval(604800); // 7 days in seconds
            }

            log.info("Login successful for email: {}", user.getEmail());
            return ResponseEntity.ok(AuthResponse.success(role, redirectUrl));

        } catch (ServiceException e) {
            log.warn("Login failed for {}: {}", request.getEmail(), e.getMessage());

            HttpStatus status = switch (e.getErrorCode()) {
                case "auth.invalidCredentials" -> HttpStatus.UNAUTHORIZED;
                case "auth.account.locked" -> HttpStatus.LOCKED;
                case "auth.account.blocked" -> HttpStatus.FORBIDDEN;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };

            return ResponseEntity.status(status)
                    .body(AuthResponse.failure(e.getMessage()));
        }
    }

    /**
     * Log out the current user.
     * <p>
     * Invalidates the session and returns success response.
     *
     * @param session the HTTP session
     * @return AuthResponse indicating success
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpSession session) {
        log.info("Logout request");

        if (session != null) {
            session.invalidate();
            log.info("Session invalidated");
        }

        return ResponseEntity.ok(AuthResponse.success(null, "/login"));
    }

    /**
     * Check the current authentication status.
     * <p>
     * Returns whether the user is authenticated, their email, and role.
     *
     * @param session the HTTP session
     * @return map with authenticated flag, email, and role
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute("user");

        if (userSession == null) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "email", "",
                    "role", ""
            ));
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "email", userSession.getEmail(),
                "role", userSession.getRole()
        ));
    }
}
