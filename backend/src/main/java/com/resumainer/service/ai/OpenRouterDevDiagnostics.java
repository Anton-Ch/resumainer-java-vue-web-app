package com.resumainer.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Dev-only diagnostic helpers for OpenRouter raw response logging.
 * Package-private — not part of public API.
 *
 * <p><b>SECURITY WARNING:</b> Raw response logging exposes AI-generated
 * resume content and profile-derived data. It MUST only be enabled in
 * dev profile with explicit opt-in.
 */
final class OpenRouterDevDiagnostics {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterDevDiagnostics.class);

    private static final String ENV_SPRING_PROFILES = "SPRING_PROFILES_ACTIVE";
    private static final String ENV_DEBUG_FLAG = "AI_DEBUG_OPENROUTER_RAW_RESPONSE";

    private OpenRouterDevDiagnostics() {
        // Utility class — no instances
    }

    /**
     * Returns true if the current Spring profile contains "dev".
     * Accepts comma-separated profiles like "dev,docker" or "docker,dev".
     */
    static boolean isDevProfileActive() {
        String profiles = System.getenv(ENV_SPRING_PROFILES);
        return isDevProfile(profiles);
    }

    /**
     * Returns true if the AI_DEBUG_OPENROUTER_RAW_RESPONSE flag is explicitly "true".
     * Case-insensitive.
     */
    static boolean isDebugFlagEnabled() {
        String flag = System.getenv(ENV_DEBUG_FLAG);
        return isDebugFlagEnabled(flag);
    }

    /**
     * Returns true if raw response diagnostic logging should be enabled:
     * dev profile + debug flag = true.
     */
    static boolean isRawResponseLoggingEnabled() {
        return isDevProfileActive() && isDebugFlagEnabled();
    }

    // ── Testable helpers (package-private for unit tests) ─────────────

    static boolean isDevProfile(String profiles) {
        if (profiles == null || profiles.isBlank()) {
            return false;
        }
        List<String> parts = Arrays.asList(profiles.split(","));
        return parts.stream().anyMatch(p -> "dev".equals(p.trim()));
    }

    static boolean isDebugFlagEnabled(String flag) {
        return flag != null && "true".equalsIgnoreCase(flag.trim());
    }

    // ── Logging methods ───────────────────────────────────────────────

    /**
     * Logs the full raw OpenRouter response body at WARN level.
     * Only logs if isRawResponseLoggingEnabled() returns true.
     *
     * @param modelCode    the model code used
     * @param httpStatus   the HTTP status code from OpenRouter
     * @param responseBody the full raw response body
     */
    static void logRawResponse(String modelCode, int httpStatus, String responseBody) {
        if (!isRawResponseLoggingEnabled()) {
            return;
        }
        log.warn("OPENROUTER_RAW_RESPONSE modelCode={} httpStatus={} body={}",
                modelCode, httpStatus, responseBody);
    }

    /**
     * Logs a compact response shape summary at WARN level.
     * Always safe — works even when raw response logging is disabled.
     *
     * @param modelCode    the model code used
     * @param httpStatus   the HTTP status code from OpenRouter
     * @param responseBody the raw response body
     */
    static void logResponseShape(String modelCode, int httpStatus, String responseBody) {
        ResponseShape shape = ResponseShape.fromJson(responseBody);
        log.warn("OPENROUTER_RESPONSE_SHAPE modelCode={} httpStatus={} {}",
                modelCode, httpStatus, shape);
    }
}
