package com.resumainer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for auth-related errors.
 * <p>
 * Maps {@link ServiceException} to appropriate HTTP status codes and JSON bodies.
 * No stack traces are exposed to the client (Constitution III, V).
 */
@ControllerAdvice
public class AuthExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handleServiceException(ServiceException ex) {
        log.warn("Service exception: code={}, message={}", ex.getErrorCode(), ex.getMessage());

        HttpStatus status = switch (ex.getErrorCode()) {
            case "auth.email.alreadyRegistered" -> HttpStatus.CONFLICT;
            case "auth.password.weak", "auth.password.mismatch" -> HttpStatus.BAD_REQUEST;
            case "auth.invalidCredentials" -> HttpStatus.UNAUTHORIZED;
            case "auth.account.locked" -> HttpStatus.LOCKED;
            case "auth.account.blocked" -> HttpStatus.FORBIDDEN;
            case "auth.registration.failed", "auth.role.notFound" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("errorCode", ex.getErrorCode());

        return ResponseEntity.status(status).body(body);
    }
}
