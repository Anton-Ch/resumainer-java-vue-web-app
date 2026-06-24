package com.resumainer.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Builds full absolute public URLs for saved resume public links.
 * <p>
 * Resolution order (when {@link HttpServletRequest} is available):
 * <ol>
 *   <li>Use configured {@code APP_PUBLIC_BASE_URL} when non-blank.
 *   <li>Use {@code X-Forwarded-Proto} and {@code X-Forwarded-Host} headers when present.
 *   <li>Fall back to request scheme + host + port for local development.
 * </ol>
 * When no {@link HttpServletRequest} is provided, the configured base URL MUST be
 * non-blank; otherwise an {@link IllegalStateException} is thrown.
 * Trailing slashes are normalised. A warning is logged when fallback origin is used.
 */
@Service
public class PublicUrlService {

    private static final Logger log = LoggerFactory.getLogger(PublicUrlService.class);

    private static final String ENV_PUBLIC_BASE_URL = "APP_PUBLIC_BASE_URL";

    private final String configuredBaseUrl;

    /**
     * Default constructor used by Spring (no-arg).
     * Reads {@code APP_PUBLIC_BASE_URL} from environment variables at construction time.
     */
    public PublicUrlService() {
        this(System.getenv(ENV_PUBLIC_BASE_URL));
        log.debug("PublicUrlService created with APP_PUBLIC_BASE_URL={}",
                configuredBaseUrl != null ? "configured" : "null");
    }

    /**
     * Package-private constructor for unit tests.
     *
     * @param configuredBaseUrl the value of APP_PUBLIC_BASE_URL, or null/blank if not configured
     */
    PublicUrlService(String configuredBaseUrl) {
        String raw = configuredBaseUrl != null ? configuredBaseUrl.trim() : null;
        this.configuredBaseUrl = (raw != null && !raw.isEmpty()) ? raw : null;
    }

    /**
     * Build a public URL using only the configured base URL.
     * <p>
     * This overload MUST NOT be called when {@code APP_PUBLIC_BASE_URL} is not configured.
     *
     * @param username   the resume owner's username
     * @param publicCode the resume's public code
     * @return full absolute public URL
     * @throws IllegalArgumentException if username or publicCode is null or blank
     * @throws IllegalStateException    if configured base URL is absent (not configured)
     */
    public String buildPublicUrl(String username, String publicCode) {
        validateParams(username, publicCode);
        if (configuredBaseUrl == null) {
            throw new IllegalStateException(
                    "APP_PUBLIC_BASE_URL is not configured and request origin is unavailable. "
                    + "Set APP_PUBLIC_BASE_URL in environment or use the overload accepting HttpServletRequest.");
        }
        return normaliseBase(configuredBaseUrl) + "/" + username + "/" + publicCode;
    }

    /**
     * Build a public URL using configured base, forwarded headers, or request fallback.
     *
     * @param username   the resume owner's username
     * @param publicCode the resume's public code
     * @param request    the current HTTP request (for forwarded headers / fallback)
     * @return full absolute public URL
     * @throws IllegalArgumentException if username or publicCode is null or blank
     */
    public String buildPublicUrl(String username, String publicCode, HttpServletRequest request) {
        validateParams(username, publicCode);
        String base = resolveBaseUrl(request);
        return base + "/" + username + "/" + publicCode;
    }

    /**
     * Resolves the base URL from: configured value → forwarded headers → request origin.
     */
    String resolveBaseUrl(HttpServletRequest request) {
        // 1. Use configured APP_PUBLIC_BASE_URL if present
        if (configuredBaseUrl != null) {
            return normaliseBase(configuredBaseUrl);
        }

        // 2. Try forwarded headers
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");

        if (forwardedProto != null && !forwardedProto.isBlank()
                && forwardedHost != null && !forwardedHost.isBlank()) {
            return normaliseBase(forwardedProto + "://" + forwardedHost);
        }

        // 3. Fallback to request origin
        String fallback = buildFallbackOrigin(request);
        log.warn("APP_PUBLIC_BASE_URL is not configured. Using request origin fallback: {}", fallback);
        return normaliseBase(fallback);
    }

    /**
     * Builds the fallback origin from request scheme + host + port.
     * Omits default ports (80 for http, 443 for https).
     */
    private String buildFallbackOrigin(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) {
            return scheme + "://" + host;
        }
        return scheme + "://" + host + ":" + port;
    }

    /**
     * Normalises a base URL by stripping trailing slashes.
     */
    private String normaliseBase(String base) {
        if (base == null || base.isBlank()) {
            return "";
        }
        int end = base.length();
        while (end > 0 && base.charAt(end - 1) == '/') {
            end--;
        }
        return base.substring(0, end);
    }

    private void validateParams(String username, String publicCode) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be null or blank");
        }
        if (publicCode == null || publicCode.isBlank()) {
            throw new IllegalArgumentException("publicCode must not be null or blank");
        }
    }
}
