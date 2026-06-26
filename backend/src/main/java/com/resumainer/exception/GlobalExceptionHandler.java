package com.resumainer.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingPathVariableException;
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Resource not found"));
        }
        return "error/404";
    }

    /**
     * Handles ServiceException (auth errors, not found, business rule violations).
     * Returns 401 for auth errors, 400 for other service errors.
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handleServiceException(ServiceException ex, HttpServletRequest request) {
        log.warn("Service error at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        boolean isAuth = "auth.unauthorized".equals(ex.getErrorCode());
        HttpStatus status = isAuth ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of(
                "errorCode", ex.getErrorCode(),
                "message", ex.getMessage()
        ));
    }

    /**
     * Handles bad request errors (invalid UUID path variable, missing path variable).
     * Always returns JSON for API requests.
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingPathVariableException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("Bad request at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "INVALID_REQUEST",
                "message", "Invalid request parameter."
        ));
    }

    /**
     * Handles IllegalArgumentException (invalid sort field, direction, etc.).
     * Returns 400 with a safe message — no stack trace, no raw DB error.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex,
                                                                      HttpServletRequest request) {
        log.warn("Invalid argument at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "INVALID_REQUEST",
                "message", ex.getMessage() != null ? ex.getMessage() : "Invalid request parameter."
        ));
    }

    /**
     * Handles all unhandled server errors (500). Returns JSON for API, HTML for pages.
     */
    @ExceptionHandler(Exception.class)
    public Object handleServerError(Exception ex, HttpServletResponse response, HttpServletRequest request) {
        log.error("Unhandled server error at {} {}", request.getMethod(), request.getRequestURI(), ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "errorCode", "INTERNAL_ERROR",
                    "message", "Unexpected server error."
            ));
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
