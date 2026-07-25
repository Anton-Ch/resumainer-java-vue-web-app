package com.resumainer.service.email;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Phase 9 — Tests for {@link EmailServiceImpl}.
 *
 * <p>Uses mock {@link HttpClient} via the package-private constructor to
 * control Resend API responses without real HTTP calls.
 */
class EmailServiceImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String FROM = "ResumAIner <noreply@send.resumainer.com>";
    private static final String API_KEY = "re_123456789abcdef";
    private static final String RESEND_URL = "https://api.resend.com/emails";

    private EmailMessage sampleMessage;

    @BeforeEach
    void setUp() {
        sampleMessage = new EmailMessage(
                "user@example.com",
                "Verify your email / Подтвердите email — ResumAIner",
                "<p>Please verify: http://localhost:8080/api/auth/verify-email?token=super-secret-token-123</p>",
                "Please verify: http://localhost:8080/api/auth/verify-email?token=super-secret-token-123"
        );
    }

    // ============================================================
    // Helper: create a mock HTTP response
    // ============================================================

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> resp = mock(HttpResponse.class);
        when(resp.statusCode()).thenReturn(statusCode);
        when(resp.body()).thenReturn(body);
        return resp;
    }

    // ============================================================
    // Helper: capture log events from EmailServiceImpl logger
    // ============================================================

    private static ListAppender<ILoggingEvent> captureLogs() {
        Logger emailLogger = (Logger) LoggerFactory.getLogger(EmailServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        emailLogger.addAppender(listAppender);
        return listAppender;
    }

    // ============================================================
    // T101 — Dev logging fallback
    // ============================================================

    @Test
    @DisplayName("T101: dev logging fallback works when dev fallback enabled and key is empty")
    void devFallback_logsEmail_whenEnabled() {
        EmailServiceImpl service = new EmailServiceImpl(
                HttpClient.newHttpClient(),
                "",       // no API key
                FROM,
                RESEND_URL,
                true,     // dev fallback enabled
                5000
        );

        // Should not throw — dev fallback logs instead of sending
        assertDoesNotThrow(() -> service.send(sampleMessage));
    }

    @Test
    @DisplayName("T101: dev fallback still sends via Resend when API key is present")
    void devFallback_usesResendWhenKeyPresent() throws Exception {
        String successJson = MAPPER.writeValueAsString(java.util.Map.of("id", "abc123"));
        HttpResponse<String> mockResp = mockResponse(200, successJson);

        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResp);

        EmailServiceImpl service = new EmailServiceImpl(
                mockHttp,
                API_KEY,
                FROM,
                RESEND_URL,
                true,   // dev fallback enabled, but key is present
                5000
        );

        assertDoesNotThrow(() -> service.send(sampleMessage));
    }

    @Test
    @DisplayName("T101: dev fallback only logs metadata, not raw body content or tokens")
    void devFallback_doesNotLogRawToken() {
        // Arrange: capture log output
        ListAppender<ILoggingEvent> logAppender = captureLogs();

        EmailServiceImpl service = new EmailServiceImpl(
                HttpClient.newHttpClient(),
                "",       // no API key — triggers dev fallback
                FROM,
                RESEND_URL,
                true,     // dev fallback enabled
                5000
        );

        // Act
        service.send(sampleMessage);

        // Assert: log events do not contain the secret token
        String markerToken = "super-secret-token-123";
        for (ILoggingEvent event : logAppender.list) {
            String formatted = event.getFormattedMessage();
            assertFalse(formatted.contains(markerToken),
                    "Log must not contain the raw token: " + formatted);
        }

        // Verify metadata IS logged
        boolean hasMetadata = logAppender.list.stream()
                .anyMatch(e -> e.getFormattedMessage().contains("To=")
                        && e.getFormattedMessage().contains("Subject="));
        assertTrue(hasMetadata, "Dev fallback must log recipient and subject");

        // Verify body lengths are logged (not bodies themselves)
        boolean hasLengths = logAppender.list.stream()
                .anyMatch(e -> e.getFormattedMessage().contains("body length"));
        assertTrue(hasLengths, "Dev fallback must log body lengths");

        // Clean up appender
        Logger emailLogger = (Logger) LoggerFactory.getLogger(EmailServiceImpl.class);
        emailLogger.detachAppender(logAppender);
    }

    // ============================================================
    // T102 — Prod missing API key fails safely
    // ============================================================

    @Test
    @DisplayName("T102: prod mode with empty API key throws EmailException")
    void prodMissingKey_throwsException() {
        EmailServiceImpl service = new EmailServiceImpl(
                HttpClient.newHttpClient(),
                "",       // no API key
                FROM,
                RESEND_URL,
                false,    // dev fallback disabled (prod)
                5000
        );

        EmailException ex = assertThrows(EmailException.class,
                () -> service.send(sampleMessage));
        assertNotNull(ex.getMessage());
        // Must not leak the empty key
        assertFalse(ex.getMessage().contains("re_"),
                "Exception message must not contain API key prefix");
    }

    @Test
    @DisplayName("T102: prod missing key message is descriptive but safe")
    void prodMissingKey_messageIsSafe() {
        EmailServiceImpl service = new EmailServiceImpl(
                HttpClient.newHttpClient(),
                "",       // no API key
                FROM,
                RESEND_URL,
                false,    // prod
                5000
        );

        EmailException ex = assertThrows(EmailException.class,
                () -> service.send(sampleMessage));
        String msg = ex.getMessage();
        assertTrue(msg.contains("RESEND_API_KEY") || msg.contains("not configured"),
                "Exception message must indicate missing key configuration");
    }

    // ============================================================
    // T105 — Builds correct HTTP request to Resend
    // ============================================================

    @Test
    @DisplayName("T105: sends POST to Resend API with correct headers")
    void send_buildsCorrectHttpRequest() throws Exception {
        String successJson = MAPPER.writeValueAsString(java.util.Map.of("id", "abc123"));
        HttpResponse<String> mockResp = mockResponse(200, successJson);

        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenAnswer(invocation -> {
                    HttpRequest req = invocation.getArgument(0);
                    assertEquals("POST", req.method(),
                            "Must be POST request");
                    assertTrue(req.uri().toString().contains("api.resend.com/emails"),
                            "URI must target Resend API");
                    assertTrue(req.headers().firstValue("Authorization").isPresent(),
                            "Authorization header must be present");
                    assertTrue(req.headers().firstValue("Authorization").get().startsWith("Bearer "),
                            "Authorization header must start with 'Bearer '");
                    assertTrue(req.headers().firstValue("Content-Type").isPresent(),
                            "Content-Type header must be present");
                    assertTrue(req.headers().firstValue("Content-Type").get().contains("application/json"),
                            "Content-Type must be application/json");
                    return mockResp;
                });

        EmailServiceImpl service = new EmailServiceImpl(
                mockHttp, API_KEY, FROM, RESEND_URL, false, 5000);

        service.send(sampleMessage);

        verify(mockHttp, times(1)).send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class));
    }

    @Test
    @DisplayName("T105: JSON body includes from, to, subject, html, text")
    void send_jsonBodyIncludesRequiredFields() throws Exception {
        String successJson = MAPPER.writeValueAsString(java.util.Map.of("id", "abc123"));
        HttpResponse<String> mockResp = mockResponse(200, successJson);

        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenAnswer(invocation -> {
                    HttpRequest req = invocation.getArgument(0);
                    String bodyStr = readRequestBody(req);
                    assertNotNull(bodyStr, "Request body must be present");

                    JsonNode json = MAPPER.readTree(bodyStr);
                    assertEquals(FROM, json.get("from").asText(),
                            "JSON must contain 'from' field");
                    assertTrue(json.get("to").isArray(),
                            "'to' must be an array");
                    assertEquals("user@example.com",
                            json.get("to").get(0).asText(),
                            "'to' must contain recipient");
                    assertTrue(json.has("subject"),
                            "JSON must contain 'subject'");
                    assertTrue(json.has("html"),
                            "JSON must contain 'html' field");
                    assertTrue(json.has("text"),
                            "JSON must contain 'text' field");
                    return mockResp;
                });

        EmailServiceImpl service = new EmailServiceImpl(
                mockHttp, API_KEY, FROM, RESEND_URL, false, 5000);

        service.send(sampleMessage);

        verify(mockHttp, times(1)).send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class));
    }

    /**
     * Helper: reads the body from an HttpRequest as a String.
     */
    private static String readRequestBody(HttpRequest req) throws Exception {
        java.net.http.HttpRequest.BodyPublisher pub = req.bodyPublisher().orElse(null);
        if (pub == null) return null;

        java.util.concurrent.CompletableFuture<String> future = new java.util.concurrent.CompletableFuture<>();
        StringBuilder sb = new StringBuilder();
        pub.subscribe(new java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer>() {
            private java.util.concurrent.Flow.Subscription subscription;
            @Override
            public void onSubscribe(java.util.concurrent.Flow.Subscription sub) {
                this.subscription = sub;
                sub.request(Long.MAX_VALUE);
            }
            @Override
            public void onNext(java.nio.ByteBuffer item) {
                byte[] bytes = new byte[item.remaining()];
                item.get(bytes);
                sb.append(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
            }
            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }
            @Override
            public void onComplete() {
                future.complete(sb.toString());
            }
        });
        return future.get(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("T105: successful Resend response (200) does not throw")
    void send_successfulResponse_returnsSuccess() throws Exception {
        String successJson = MAPPER.writeValueAsString(java.util.Map.of("id", "abc123"));
        HttpResponse<String> mockResp = mockResponse(200, successJson);

        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResp);

        EmailServiceImpl service = new EmailServiceImpl(
                mockHttp, API_KEY, FROM, RESEND_URL, false, 5000);

        assertDoesNotThrow(() -> service.send(sampleMessage));
    }

    // ============================================================
    // T105 — Error handling — no API key leak
    // ============================================================

    @Test
    @DisplayName("T105: Resend non-2xx fails safely and does not leak API key")
    void send_non2xx_failsSafely() throws Exception {
        String errorBody = "{\"statusCode\":401,\"message\":\"invalid API key\"}";
        HttpResponse<String> mockResp = mockResponse(401, errorBody);

        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResp);

        EmailServiceImpl service = new EmailServiceImpl(
                mockHttp, API_KEY, FROM, RESEND_URL, false, 5000);

        EmailException ex = assertThrows(EmailException.class,
                () -> service.send(sampleMessage));
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().contains("re_123456789abcdef"),
                "Exception must not leak the full API key");
    }

    @Test
    @DisplayName("T105: HTTP exception fails safely and does not leak API key")
    void send_httpException_failsSafely() throws Exception {
        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection refused"));

        EmailServiceImpl service = new EmailServiceImpl(
                mockHttp, API_KEY, FROM, RESEND_URL, false, 5000);

        EmailException ex = assertThrows(EmailException.class,
                () -> service.send(sampleMessage));
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().contains("re_123456789abcdef"),
                "Exception must not leak the API key");
        assertFalse(ex.getMessage().contains("Connection refused"),
                "Exception message must not leak raw IO error details");
    }

    // ============================================================
    // T106 — No API key exposure in toString / logs
    // ============================================================

    @Test
    @DisplayName("T106: toString does not expose API key")
    void toString_doesNotExposeApiKey() {
        EmailServiceImpl service = new EmailServiceImpl(
                HttpClient.newHttpClient(),
                "re_my-secret-key-12345",
                FROM,
                RESEND_URL,
                false,
                5000
        );

        String str = service.toString();
        assertFalse(str.contains("re_my-secret-key-12345"),
                "toString must not contain the API key");
        assertFalse(str.contains("12345"),
                "toString must not contain parts of the API key");
        assertTrue(str.contains("provider=resend"),
                "toString must indicate provider without exposing key");
    }

    @Test
    @DisplayName("T106: exception message does not expose API key")
    void exceptionMessage_doesNotExposeApiKey() {
        String fakeKey = "re_test_do_not_leak";
        String errorBody = "{\"statusCode\":401,\"message\":\"invalid\"}";
        HttpResponse<String> mockResp = mockResponse(401, errorBody);

        HttpClient mockHttp = mock(HttpClient.class);
        try {
            when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockResp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        EmailServiceImpl service = new EmailServiceImpl(
                mockHttp, fakeKey, FROM, RESEND_URL, false, 5000);

        EmailException ex = assertThrows(EmailException.class,
                () -> service.send(sampleMessage));
        String msg = ex.getMessage();
        assertFalse(msg.contains(fakeKey),
                "Exception message must not contain the API key value");
        assertFalse(msg.contains("re_test"),
                "Exception message must not leak even partial API key");
    }

    // ============================================================
    // Edge cases
    // ============================================================

    @Test
    @DisplayName("null message throws NullPointerException")
    void nullMessage_throwsNullPointer() {
        EmailServiceImpl service = new EmailServiceImpl(
                HttpClient.newHttpClient(),
                API_KEY, FROM, RESEND_URL, false, 5000);

        assertThrows(NullPointerException.class,
                () -> service.send(null));
    }
}
