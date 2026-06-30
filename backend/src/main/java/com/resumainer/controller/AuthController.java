package com.resumainer.controller;

import com.resumainer.dto.AuthResponse;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.model.User;
import com.resumainer.service.AuthService;
import com.resumainer.service.security.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication endpoints.
 * <p>
 * Registration still uses custom AuthService. Login/logout are handled by
 * Spring Security (Phase 4). Status reads from Spring Security Authentication.
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
     * Phase 4: still uses old custom registration flow. Auto-login still active
     * via session attribute for backward compatibility. Will be updated in Phase 10.
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

            // Auto-login via old custom session (temporary — Phase 10 will change this)
            com.resumainer.dto.UserSession userSession = new com.resumainer.dto.UserSession(
                    user.getId(), user.getEmail(), "USER", user.isPrivileged());
            session.setAttribute("user", userSession);

            log.info("User registered and logged in: {}", user.getEmail());
            return ResponseEntity.ok(AuthResponse.success("USER", "/home"));

        } catch (com.resumainer.exception.ServiceException e) {
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
     * Check the current authentication status from Spring Security.
     * <p>
     * Source of truth is Spring Security Authentication, not old session attribute.
     *
     * @param authentication Spring Security Authentication (null if unauthenticated)
     * @return map with authenticated flag, email, and role
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "email", "",
                    "role", ""
            ));
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            String role = userDetails.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())) ? "ADMIN" : "USER";
            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "email", userDetails.getUsername(),
                    "role", role
            ));
        }

        // Fallback: basic info from authentication
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "email", authentication.getName(),
                "role", ""
        ));
    }
}
