package com.resumainer.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF protection filter implementing OWASP cookie-to-header pattern.
 * <p>
 * 1. On any request: if session has no CSRF token, generates one via SecureRandom,
 *    stores in session, sets as non-HTTP-only cookie {@code XSRF-TOKEN}.
 * 2. For POST/PUT/DELETE: validates that {@code X-CSRF-Token} header
 *    matches the token stored in session.
 * 3. Skips validation for {@code /api/auth/*}, {@code /api/public/**}.
 * <p>
 * Constitution V: CSRF protection per SEC-003. Spring Boot's FilterRegistrationBean
 * is not used — filter is registered via AppInitializer.getServletFilters() (see B6).
 */
public class CsrfFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CsrfFilter.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String CSRF_SESSION_ATTR = "CSRF_TOKEN";
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-CSRF-Token";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);

        // Ensure CSRF token exists in session
        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (sessionToken == null) {
            sessionToken = generateToken();
            session.setAttribute(CSRF_SESSION_ATTR, sessionToken);
        }

        // Always set CSRF cookie (non-HTTP-only so JS can read it)
        jakarta.servlet.http.Cookie csrfCookie = new jakarta.servlet.http.Cookie(CSRF_COOKIE_NAME, sessionToken);
        csrfCookie.setPath("/");
        csrfCookie.setHttpOnly(false);
        csrfCookie.setSecure(false); // Set to true in production with HTTPS
        csrfCookie.setAttribute("SameSite", "Lax");
        response.addCookie(csrfCookie);

        // Validate for state-changing requests (skip GET, HEAD, OPTIONS, TRACE)
        // Skip CSRF validation for auth endpoints (no session needed yet)
        // and public endpoints
        String path = request.getRequestURI();
        boolean isExcludedPath = path.startsWith("/api/auth/") || path.startsWith("/api/public/");

        String method = request.getMethod();
        boolean isStateChanging = "POST".equals(method) || "PUT".equals(method)
                || "DELETE".equals(method) || "PATCH".equals(method);

        if (isStateChanging && !isExcludedPath) {
            String headerToken = request.getHeader(CSRF_HEADER_NAME);

            if (headerToken == null || !sessionToken.equals(headerToken)) {
                log.warn("CSRF validation failed for {} {}", method, path);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or missing CSRF token\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
