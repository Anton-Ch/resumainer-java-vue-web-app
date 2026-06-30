package com.resumainer.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for CSRF token bootstrap and health check.
 *
 * <p>{@code GET /api/csrf} — returns the CSRF token and forces the
 * {@code CookieCsrfTokenRepository} to write the {@code XSRF-TOKEN} cookie.
 * Called by the frontend on app load to bootstrap the CSRF token.
 *
 * <p>{@code GET /api/csrf/ping} — simple health check to verify controller deployment.
 */
@RestController
public class CsrfTokenController {

    @GetMapping("/api/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/api/csrf/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }
}
