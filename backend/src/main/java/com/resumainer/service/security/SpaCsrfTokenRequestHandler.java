package com.resumainer.service.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * Custom {@link CsrfTokenRequestHandler} for SPA CSRF integration.
 *
 * <p>Spring Security 6.5+ recommended handler for single-page applications using
 * {@code CookieCsrfTokenRepository} with the {@code XSRF-TOKEN} cookie and
 * {@code X-XSRF-TOKEN} header pattern.
 *
 * <p>Logic:
 * <ul>
 *   <li><b>handle()</b> — uses XOR handler for BREACH protection when rendering
 *       the token in the response body, then forces deferred token loading by
 *       calling {@code csrfToken.get()} so the cookie is set on every response.</li>
 *   <li><b>resolveCsrfTokenValue()</b> — if the request has an {@code X-XSRF-TOKEN}
 *       header, resolves using the plain (non-XOR) handler because the cookie
 *       contains the raw token. Otherwise falls back to XOR handler for
 *       server-side form parameters.</li>
 * </ul>
 */
public final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       Supplier<CsrfToken> csrfToken) {
        // Use XOR for BREACH protection when rendering in response body
        this.xor.handle(request, response, csrfToken);
        // Force deferred token to load — triggers cookie creation via CookieCsrfTokenRepository
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        // If the request has a header (SPA sends X-XSRF-TOKEN), resolve plain (no XOR)
        // because the cookie contains the raw CsrfToken value.
        // Otherwise, use XOR for server-side form _csrf parameter.
        return (StringUtils.hasText(headerValue) ? this.plain : this.xor)
                .resolveCsrfTokenValue(request, csrfToken);
    }
}
