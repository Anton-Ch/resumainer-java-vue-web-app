package com.resumainer.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/**
 * Global exception handler that returns JSON for API errors (/api/*)
 * and HTML for page errors (Thymeleaf views).
 * No stack traces are exposed to the client (Constitution V.2).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles 404 errors (page not found). Returns JSON for API, HTML for pages.
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Object handleNotFound(Exception ex, HttpServletResponse response, HttpServletRequest request) {
        log.warn("Not found: {} {}", request.getMethod(), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        if (isApiRequest(request)) {
            return Map.of("error", "Resource not found");
        }
        return "error/404";
    }

    /**
     * Handles all unhandled server errors (500). Returns JSON for API, HTML for pages.
     */
    @ExceptionHandler(Exception.class)
    public Object handleServerError(Exception ex, HttpServletResponse response, HttpServletRequest request) {
        log.error("Unhandled server error at {} {}", request.getMethod(), request.getRequestURI(), ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        if (isApiRequest(request)) {
            return Map.of("error", "Internal server error");
        }
        return "error/500";
    }

    /**
     * Handles validation errors (@Valid on @RequestBody) — returns JSON for API.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error at {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getBindingResult().getFieldError() != null
                        ? ex.getBindingResult().getFieldError().getDefaultMessage()
                        : "Invalid request");
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Validation failed";
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    /**
     * Returns true if the request path starts with /api/, indicating an API client.
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/api/");
    }
}
