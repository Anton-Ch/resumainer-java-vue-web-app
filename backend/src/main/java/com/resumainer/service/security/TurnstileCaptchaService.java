package com.resumainer.service.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Cloudflare Turnstile captcha verification service.
 *
 * <p>Verifies tokens server-side against the Cloudflare Siteverify API.
 * Supports config-driven dev bypass mode for local testing.
 *
 * <p>Uses {@link HttpClient} (Java 21 standard library) — no extra dependencies.
 * Never logs the secret key or raw tokens.
 */
@Service
public class TurnstileCaptchaService implements CaptchaService {

    private static final Logger log = LoggerFactory.getLogger(TurnstileCaptchaService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final String secretKey;
    private final boolean devBypassEnabled;
    private final String devToken;
    private final String verifyUrl;
    private final Duration timeout;

    /**
     * Primary constructor used by Spring wiring.
     * Config values are injected via {@code @Value} from {@code application.properties}.
     *
     * <p>{@code @Autowired} is explicit because the class also has a package-private
     * constructor for testing with mock {@link HttpClient}.
     */
    @Autowired
    public TurnstileCaptchaService(
            @Value("${app.captcha.turnstile.secret-key}") String secretKey,
            @Value("${app.captcha.dev-bypass-enabled}") boolean devBypassEnabled,
            @Value("${app.captcha.dev-token}") String devToken,
            @Value("${app.captcha.turnstile.verify-url}") String verifyUrl,
            @Value("${app.captcha.turnstile.timeout-ms}") int timeoutMs) {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build(),
                secretKey, devBypassEnabled, devToken, verifyUrl, timeoutMs);
    }

    /**
     * Package-private constructor for testing with mock {@link HttpClient}.
     */
    TurnstileCaptchaService(HttpClient httpClient,
                            String secretKey,
                            boolean devBypassEnabled,
                            String devToken,
                            String verifyUrl,
                            int timeoutMs) {
        this.httpClient = httpClient;
        this.secretKey = secretKey != null ? secretKey : "";
        this.devBypassEnabled = devBypassEnabled;
        this.devToken = devToken != null ? devToken : "";
        this.verifyUrl = verifyUrl;
        this.timeout = Duration.ofMillis(timeoutMs > 0 ? timeoutMs : 10000);
    }

    @Override
    public CaptchaResult verify(String token) {
        // 1. Dev bypass check
        if (devBypassEnabled && devToken.equals(token)) {
            log.debug("Dev captcha bypass: token accepted (dev mode)");
            return CaptchaResult.success();
        }

        // 2. Blank token (not dev bypass) — reject early
        if (token == null || token.isBlank()) {
            log.debug("Captcha verification: blank token rejected");
            return CaptchaResult.failure("CAPTCHA_INVALID");
        }

        // 3. Missing secret in prod — fail safely
        if (secretKey.isEmpty()) {
            log.warn("Captcha verification: secret key is not configured in prod mode");
            return CaptchaResult.failure("CAPTCHA_INVALID");
        }

        // 4. Verify with Cloudflare Siteverify API
        try {
            String formBody = "secret=" + urlEncode(secretKey)
                    + "&response=" + urlEncode(token);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(verifyUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(timeout)
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Captcha verification: Siteverify returned HTTP {}", response.statusCode());
                return CaptchaResult.failure("CAPTCHA_INVALID");
            }

            JsonNode json = MAPPER.readTree(response.body());
            boolean success = json.path("success").asBoolean(false);

            if (success) {
                log.debug("Captcha verification: token valid");
                return CaptchaResult.success();
            } else {
                log.debug("Captcha verification: token rejected by Cloudflare");
                return CaptchaResult.failure("CAPTCHA_INVALID");
            }

        } catch (Exception e) {
            log.warn("Captcha verification: Siteverify API call failed: {}", e.getMessage());
            return CaptchaResult.failure("CAPTCHA_INVALID");
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "TurnstileCaptchaService{provider=turnstile, devBypassEnabled="
                + devBypassEnabled + "}";
    }
}
