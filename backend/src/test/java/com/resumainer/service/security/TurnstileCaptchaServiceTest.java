package com.resumainer.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Phase 8 — Tests for {@link TurnstileCaptchaService}.
 *
 * <p>Uses mock {@link HttpClient} via the package-private constructor to
 * control Cloudflare Siteverify API responses without real HTTP calls.
 */
class TurnstileCaptchaServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DEV_TOKEN = "dev-captcha-pass";
    private static final String VERIFY_URL =
            "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    private static final String TEST_SECRET = "1x0000000000000000000000000000000AA";

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
    // T090 — Dev bypass: exact dev-token passes in dev mode
    // ============================================================

    @Test
    @DisplayName("T090: exact dev-captcha-pass token passes when dev bypass enabled")
    void devCaptchaPass_passesWhenDevBypassEnabled() {
        TurnstileCaptchaService service = new TurnstileCaptchaService(
                HttpClient.newHttpClient(),
                TEST_SECRET,
                true,   // devBypassEnabled
                DEV_TOKEN,
                VERIFY_URL,
                5000
        );

        CaptchaResult result = service.verify(DEV_TOKEN);

        assertTrue(result.isSuccess(), "Dev token must pass when dev bypass enabled");
        assertNull(result.getErrorCode());
    }

    @Test
    @DisplayName("T090: non-dev token still verified against Turnstile in dev mode")
    void nonDevToken_stillVerifiedInDevMode() throws Exception {
        // Prepare mock response
        String failJson = MAPPER.writeValueAsString(
                Map.of("success", false, "error-codes", new String[]{"invalid-input-response"}));
        HttpResponse<String> mockResp = mockResponse(200, failJson);

        // Prepare mock HTTP client
        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResp);

        TurnstileCaptchaService service = new TurnstileCaptchaService(
                mockHttp, TEST_SECRET, true, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify("some-other-token");

        assertFalse(result.isSuccess(), "Non-dev token must be verified against Turnstile");
        assertEquals("CAPTCHA_INVALID", result.getErrorCode());
    }

    // ============================================================
    // T091 — Dev bypass rejected in prod mode
    // ============================================================

    @Test
    @DisplayName("T091: dev-captcha-pass rejected when dev bypass disabled (prod)")
    void devCaptchaPass_rejectedWhenDevBypassDisabled() throws Exception {
        String failJson = MAPPER.writeValueAsString(
                Map.of("success", false, "error-codes", new String[]{"invalid-input-response"}));
        HttpResponse<String> mockResp = mockResponse(200, failJson);

        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResp);

        TurnstileCaptchaService service = new TurnstileCaptchaService(
                mockHttp, TEST_SECRET, false, DEV_TOKEN, VERIFY_URL, 5000);

        // In prod mode, dev-captcha-pass is verified against Turnstile
        CaptchaResult result = service.verify(DEV_TOKEN);

        assertFalse(result.isSuccess(), "Dev token must be rejected in prod mode");
        assertEquals("CAPTCHA_INVALID", result.getErrorCode());
    }

    @Test
    @DisplayName("T091: empty token rejected in prod mode")
    void emptyToken_rejectedInProdMode() {
        TurnstileCaptchaService service = new TurnstileCaptchaService(
                HttpClient.newHttpClient(), TEST_SECRET, false, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify("");

        assertFalse(result.isSuccess());
        assertEquals("CAPTCHA_INVALID", result.getErrorCode());
    }

    @Test
    @DisplayName("T091: null token rejected in prod mode")
    void nullToken_rejectedInProdMode() {
        TurnstileCaptchaService service = new TurnstileCaptchaService(
                HttpClient.newHttpClient(), TEST_SECRET, false, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify(null);

        assertFalse(result.isSuccess());
        assertEquals("CAPTCHA_INVALID", result.getErrorCode());
    }

    @Test
    @DisplayName("T091: empty token rejected even in dev mode (no token provided)")
    void emptyToken_rejectedEvenInDevMode() {
        TurnstileCaptchaService service = new TurnstileCaptchaService(
                HttpClient.newHttpClient(), TEST_SECRET, true, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify("");

        assertFalse(result.isSuccess(), "Empty token must fail even in dev mode");
    }

    @Test
    @DisplayName("T091: null token rejected even in dev mode")
    void nullToken_rejectedEvenInDevMode() {
        TurnstileCaptchaService service = new TurnstileCaptchaService(
                HttpClient.newHttpClient(), TEST_SECRET, true, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify(null);

        assertFalse(result.isSuccess(), "Null token must fail even in dev mode");
    }

    // ============================================================
    // T092 — Invalid captcha handling
    // ============================================================

    @Test
    @DisplayName("T092: blank whitespace token rejected")
    void blankToken_rejected() {
        TurnstileCaptchaService service = new TurnstileCaptchaService(
                HttpClient.newHttpClient(), TEST_SECRET, false, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify("   ");

        assertFalse(result.isSuccess());
        assertEquals("CAPTCHA_INVALID", result.getErrorCode());
    }

    @Test
    @DisplayName("T092: Turnstile API failure returns CAPTCHA_INVALID")
    void turnstileApiFails_returnsInvalid() throws Exception {
        String failJson = MAPPER.writeValueAsString(
                Map.of("success", false, "error-codes", new String[]{"invalid-input-response"}));
        HttpResponse<String> mockResp = mockResponse(200, failJson);

        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResp);

        TurnstileCaptchaService service = new TurnstileCaptchaService(
                mockHttp, TEST_SECRET, false, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify("some-valid-looking-token");

        assertFalse(result.isSuccess());
        assertEquals("CAPTCHA_INVALID", result.getErrorCode());
    }

    @Test
    @DisplayName("T092: Turnstile non-200 HTTP response returns CAPTCHA_INVALID")
    void turnstileNon200_returnsInvalid() throws Exception {
        HttpResponse<String> mockResp = mockResponse(500, "Internal Server Error");

        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResp);

        TurnstileCaptchaService service = new TurnstileCaptchaService(
                mockHttp, TEST_SECRET, false, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify("some-token");

        assertFalse(result.isSuccess());
        assertEquals("CAPTCHA_INVALID", result.getErrorCode());
    }

    @Test
    @DisplayName("T092: HTTP exception returns CAPTCHA_INVALID")
    void httpException_returnsInvalid() throws Exception {
        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection refused"));

        TurnstileCaptchaService service = new TurnstileCaptchaService(
                mockHttp, TEST_SECRET, false, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify("some-token");

        assertFalse(result.isSuccess());
        assertEquals("CAPTCHA_INVALID", result.getErrorCode());
    }

    // ============================================================
    // T096 — Missing secret key fails safely
    // ============================================================

    @Test
    @DisplayName("T096: empty secret key in prod fails with CAPTCHA_INVALID")
    void emptySecretKey_failsInProdMode() {
        TurnstileCaptchaService service = new TurnstileCaptchaService(
                HttpClient.newHttpClient(), "", false, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify("some-token");

        assertFalse(result.isSuccess(), "Missing secret must fail in prod mode");
        assertEquals("CAPTCHA_INVALID", result.getErrorCode());
    }

    @Test
    @DisplayName("T096: dev token passes with empty secret in dev mode")
    void devToken_passesWithEmptySecretInDevMode() {
        TurnstileCaptchaService service = new TurnstileCaptchaService(
                HttpClient.newHttpClient(), "", true, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify(DEV_TOKEN);

        assertTrue(result.isSuccess(), "Dev token must pass in dev mode even without secret");
    }

    // ============================================================
    // Successful Turnstile response
    // ============================================================

    @Test
    @DisplayName("Successful Turnstile API response returns success")
    void turnstileApiSuccess_returnsSuccess() throws Exception {
        String successJson = MAPPER.writeValueAsString(
                Map.of("success", true, "challenge_ts", "2026-07-01T12:00:00Z"));
        HttpResponse<String> mockResp = mockResponse(200, successJson);

        HttpClient mockHttp = mock(HttpClient.class);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResp);

        TurnstileCaptchaService service = new TurnstileCaptchaService(
                mockHttp, TEST_SECRET, false, DEV_TOKEN, VERIFY_URL, 5000);

        CaptchaResult result = service.verify("valid-token-from-client");

        assertTrue(result.isSuccess());
        assertNull(result.getErrorCode());
    }

    // ============================================================
    // Secret leakage checks
    // ============================================================

    @Test
    @DisplayName("Service toString does not expose secret key")
    void toString_doesNotExposeSecret() {
        TurnstileCaptchaService service = new TurnstileCaptchaService(
                HttpClient.newHttpClient(), "my-super-secret-key-12345",
                false, DEV_TOKEN, VERIFY_URL, 5000);

        String str = service.toString();
        assertFalse(str.contains("my-super-secret-key-12345"));
        assertFalse(str.contains("12345"));
    }

    @Test
    @DisplayName("CaptchaResult toString does not expose sensitive data")
    void captchaResultToString_isClean() {
        CaptchaResult success = CaptchaResult.success();
        CaptchaResult failure = CaptchaResult.failure("CAPTCHA_INVALID");

        assertTrue(success.toString().contains("success=true"));
        assertTrue(failure.toString().contains("errorCode=CAPTCHA_INVALID"));
    }

    @Test
    @DisplayName("Null errorCode rejected by CaptchaResult.failure")
    void failureWithNullErrorCode_throws() {
        assertThrows(NullPointerException.class,
                () -> CaptchaResult.failure(null));
    }
}
