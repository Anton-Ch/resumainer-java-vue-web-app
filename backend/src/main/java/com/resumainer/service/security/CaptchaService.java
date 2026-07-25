package com.resumainer.service.security;

/**
 * Service interface for captcha verification.
 * Implementations verify tokens from various providers (Turnstile, etc.).
 *
 * <p>Following KISS: single verify method returning a simple result.
 */
public interface CaptchaService {

    /**
     * Verify a captcha token.
     *
     * @param token the captcha token from the client (may be {@code null} or blank)
     * @return verification result — never {@code null}
     */
    CaptchaResult verify(String token);
}
