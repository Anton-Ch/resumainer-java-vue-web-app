package com.resumainer.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Spring Security {@link AuthenticationFailureHandler} that returns a JSON
 * error response with a stable error code.
 *
 * <p>Maps authentication exceptions to API error codes:
 * <ul>
 *   <li>{@link BadCredentialsException} → {@code INVALID_CREDENTIALS}</li>
 *   <li>{@link LockedException} → {@code ACCOUNT_LOCKED}</li>
 *   <li>{@link DisabledException} with message containing "verif" → {@code EMAIL_NOT_VERIFIED}</li>
 *   <li>Other → generic safe error</li>
 * </ul>
 */
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonAuthenticationFailureHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("email");
        if (email == null || email.isBlank()) {
            // Try to get from request body (set by custom filter)
            email = (String) request.getAttribute("auth_email");
        }

        String errorCode;
        String message;

        // Unwrap InternalAuthenticationServiceException to find the real cause
        Throwable cause = exception;
        if (exception instanceof InternalAuthenticationServiceException && exception.getCause() != null) {
            cause = exception.getCause();
        }

        if (exception instanceof BadCredentialsException) {
            errorCode = "INVALID_CREDENTIALS";
            message = "Invalid email or password";
        } else if (cause instanceof LockedException) {
            errorCode = "ACCOUNT_LOCKED";
            message = "Account is temporarily locked. Try again later.";
        } else if (cause instanceof DisabledException) {
            // DisabledException is thrown only for email-not-verified case
            errorCode = "EMAIL_NOT_VERIFIED";
            message = "Please verify your email before logging in.";
        } else {
            // Safe generic fallback for any unexpected exception type
            errorCode = "INVALID_CREDENTIALS";
            message = "Invalid email or password";
        }

        // Safe logging: email, IP, error code only
        String ip = request.getRemoteAddr();
        log.warn("Login failed: email={}, ip={}, errorCode={}", email, ip, errorCode);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), Map.of(
                "success", false,
                "message", message,
                "errorCode", errorCode
        ));
    }
}
