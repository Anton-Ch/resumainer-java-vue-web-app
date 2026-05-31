package com.resumainer.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for the Landing Page and future features.
 * <p>
 * Catches all unhandled exceptions and returns user-friendly HTML pages
 * without exposing stack traces. This satisfies Constitution V.2
 * (Error Safety — never expose stack traces to the client).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles 404 errors (page not found).
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public String handleNotFound(Exception ex, HttpServletResponse response) {
        log.warn("Page not found: {}", ex.getMessage());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "error/404";
    }

    /**
     * Handles all unhandled server errors (500).
     */
    @ExceptionHandler(Exception.class)
    public String handleServerError(Exception ex, HttpServletResponse response) {
        log.error("Unhandled server error", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return "error/500";
    }
}
