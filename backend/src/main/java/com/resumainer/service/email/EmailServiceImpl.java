package com.resumainer.service.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Email sending service that delivers via the Resend API.
 *
 * <p>Supports config-driven dev fallback mode for local testing:
 * <ul>
 *   <li>If {@code RESEND_API_KEY} is set (non-empty) → always sends via Resend.</li>
 *   <li>If key is empty AND dev fallback is enabled → logs the email content safely.</li>
 *   <li>If key is empty AND dev fallback is disabled (production) → throws
 *       {@link EmailException} with a clear message.</li>
 * </ul>
 *
 * <p>Uses {@link HttpClient} (Java 21 standard library) — no extra dependencies.
 * Never logs the API key or Authorization header.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final String apiKey;
    private final String from;
    private final String resendUrl;
    private final boolean devFallbackEnabled;
    private final Duration timeout;

    /**
     * Primary constructor used by Spring wiring.
     */
    @Autowired
    public EmailServiceImpl(
            @Value("${app.email.resend.api-key}") String apiKey,
            @Value("${app.email.from}") String from,
            @Value("${app.email.resend.api-url:https://api.resend.com/emails}") String resendUrl,
            @Value("${app.email.dev-fallback-enabled}") boolean devFallbackEnabled,
            @Value("${app.email.resend.timeout-ms:10000}") int timeoutMs) {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build(),
                apiKey, from, resendUrl, devFallbackEnabled, timeoutMs);
    }

    /**
     * Package-private constructor for testing with mock {@link HttpClient}.
     */
    EmailServiceImpl(HttpClient httpClient,
                     String apiKey,
                     String from,
                     String resendUrl,
                     boolean devFallbackEnabled,
                     int timeoutMs) {
        this.httpClient = httpClient;
        this.apiKey = apiKey != null ? apiKey : "";
        this.from = from != null ? from : "";
        this.resendUrl = resendUrl;
        this.devFallbackEnabled = devFallbackEnabled;
        this.timeout = Duration.ofMillis(timeoutMs > 0 ? timeoutMs : 10000);
    }

    @Override
    public void send(EmailMessage message) {
        if (message == null) {
            throw new NullPointerException("message must not be null");
        }

        // If API key is available, always send via Resend
        if (!apiKey.isEmpty()) {
            sendViaResend(message);
            return;
        }

        // No API key — check if dev fallback is enabled
        if (devFallbackEnabled) {
            log.warn("DEV FALLBACK: Resend API key is not configured. Logging email metadata instead of sending.");
            log.warn("DEV FALLBACK: To={}, Subject={}", message.getTo(), message.getSubject());
            log.warn("DEV FALLBACK: HTML body length={}", message.getHtmlBody().length());
            log.warn("DEV FALLBACK: Text body length={}", message.getTextBody().length());
            return;
        }

        // Production mode with no API key — fail loudly
        log.error("Email sending failed: RESEND_API_KEY is not configured");
        throw new EmailException(
                "Email sending is not configured: RESEND_API_KEY is missing. "
                + "Set RESEND_API_KEY environment variable or enable dev fallback.");
    }

    /**
     * Sends email via the Resend REST API.
     */
    private void sendViaResend(EmailMessage message) {
        try {
            Map<String, Object> body = Map.of(
                    "from", from,
                    "to", List.of(message.getTo()),
                    "subject", message.getSubject(),
                    "html", message.getHtmlBody(),
                    "text", message.getTextBody()
            );

            String jsonBody = MAPPER.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resendUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(timeout)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("Email sent successfully via Resend: to={}, subject={}",
                        message.getTo(), message.getSubject());
            } else {
                // Log status code and safe error info — never log the API key
                log.warn("Resend API returned HTTP {} for to={}",
                        response.statusCode(), message.getTo());
                throw new EmailException(
                        "Email sending failed: Resend API returned HTTP "
                        + response.statusCode());
            }

        } catch (EmailException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Resend API call failed for to={}: {}",
                    message.getTo(), e.getMessage());
            throw new EmailException(
                    "Email sending failed due to an unexpected error", e);
        }
    }

    @Override
    public String toString() {
        return "EmailServiceImpl{provider=resend, from='" + from
                + "', devFallbackEnabled=" + devFallbackEnabled
                + ", keyConfigured=" + !apiKey.isEmpty() + '}';
    }
}
