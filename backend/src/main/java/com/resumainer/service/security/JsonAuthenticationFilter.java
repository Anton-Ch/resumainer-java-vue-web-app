package com.resumainer.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

/**
 * Spring Security filter that reads JSON credentials from {@code POST /api/auth/login}.
 *
 * <p>Replaces the default form-login parameter parsing for the SPA JSON contract.
 * Expects: {@code {"email": "...", "password": "...", "rememberMe": bool}}.
 *
 * <p>All parsing errors are converted to {@link BadCredentialsException} so that
 * the {@link JsonAuthenticationFailureHandler} always returns a JSON error response.
 */
public class JsonAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final AntPathRequestMatcher DEFAULT_MATCHER =
            new AntPathRequestMatcher("/api/auth/login", "POST");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonAuthenticationFilter() {
        super(DEFAULT_MATCHER);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws AuthenticationException, IOException {

        Map<String, Object> body;
        try {
            body = objectMapper.readValue(request.getReader(), Map.class);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid JSON request body");
        }

        String email = body.get("email") != null ? body.get("email").toString().trim().toLowerCase() : "";
        String password = body.get("password") != null ? body.get("password").toString() : "";

        if (email.isBlank() || password.isBlank()) {
            throw new BadCredentialsException("Email and password are required");
        }

        // Store email for logging in failure handler
        request.setAttribute("auth_email", email);

        UsernamePasswordAuthenticationToken authRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(email, password);

        // Allow subclasses to set the details
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private void setDetails(HttpServletRequest request,
                            UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}
