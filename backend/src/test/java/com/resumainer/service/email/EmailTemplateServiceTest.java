package com.resumainer.service.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 9 — Tests for {@link EmailTemplateService}.
 *
 * <p>Verifies bilingual EN+RU template generation in a single email,
 * correct URL building with encoded tokens, and proper use of configured
 * base URLs.
 */
class EmailTemplateServiceTest {

    private static final String FRONTEND_URL = "http://localhost:5173";
    private static final String BACKEND_URL = "http://localhost:8080";

    private EmailTemplateService service;

    private EmailTemplateService createService(String frontend, String backend) {
        return new EmailTemplateService(frontend, backend);
    }

    // ============================================================
    // Blocker 1 — Bilingual templates (EN first, RU second)
    // ============================================================

    @Test
    @DisplayName("Verification HTML contains both EN and RU content")
    void verificationHtml_containsBothEnAndRu() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createVerificationEmail("user@test.com", "token123");

        String html = msg.getHtmlBody();
        // EN content
        assertTrue(html.contains("Please verify your email address"),
                "HTML must contain English verification text");
        // RU content
        assertTrue(html.contains("подтвердите ваш email"),
                "HTML must contain Russian verification text");
        // Brand
        assertTrue(html.contains("ResumAIner"), "HTML must contain brand name");
    }

    @Test
    @DisplayName("Verification plain text contains both EN and RU content")
    void verificationText_containsBothEnAndRu() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createVerificationEmail("user@test.com", "token123");

        String text = msg.getTextBody();
        assertTrue(text.contains("verify your email address"),
                "Text must contain English verification text");
        assertTrue(text.contains("подтвердите ваш email"),
                "Text must contain Russian verification text");
    }

    @Test
    @DisplayName("Verification EN content appears before RU content in HTML")
    void verificationHtml_enBeforeRu() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createVerificationEmail("user@test.com", "token123");

        String html = msg.getHtmlBody();
        int enIndex = html.indexOf("verify your email");
        int ruIndex = html.indexOf("подтвердите ваш email");
        assertTrue(enIndex >= 0, "EN content must be present");
        assertTrue(ruIndex >= 0, "RU content must be present");
        assertTrue(enIndex < ruIndex,
                "EN content must appear before RU content in HTML body");
    }

    @Test
    @DisplayName("Verification EN content appears before RU content in plain text")
    void verificationText_enBeforeRu() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createVerificationEmail("user@test.com", "token123");

        String text = msg.getTextBody();
        int enIndex = text.indexOf("verify your email");
        int ruIndex = text.indexOf("подтвердите ваш email");
        assertTrue(enIndex >= 0, "EN content must be present");
        assertTrue(ruIndex >= 0, "RU content must be present");
        assertTrue(enIndex < ruIndex,
                "EN content must appear before RU content in text body");
    }

    @Test
    @DisplayName("Password-reset HTML contains both EN and RU content")
    void passwordResetHtml_containsBothEnAndRu() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createPasswordResetEmail("user@test.com", "token456");

        String html = msg.getHtmlBody();
        assertTrue(html.contains("reset your password"),
                "HTML must contain English reset text");
        assertTrue(html.contains("восстановление пароля"),
                "HTML must contain Russian reset text");
    }

    @Test
    @DisplayName("Password-reset plain text contains both EN and RU content")
    void passwordResetText_containsBothEnAndRu() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createPasswordResetEmail("user@test.com", "token456");

        String text = msg.getTextBody();
        assertTrue(text.contains("reset your password"),
                "Text must contain English reset text");
        assertTrue(text.contains("восстановление пароля"),
                "Text must contain Russian reset text");
    }

    @Test
    @DisplayName("Password-reset EN content appears before RU content in HTML")
    void passwordResetHtml_enBeforeRu() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createPasswordResetEmail("user@test.com", "token456");

        String html = msg.getHtmlBody();
        int enIndex = html.indexOf("reset your password");
        int ruIndex = html.indexOf("восстановление пароля");
        assertTrue(enIndex >= 0, "EN content must be present");
        assertTrue(ruIndex >= 0, "RU content must be present");
        assertTrue(enIndex < ruIndex,
                "EN content must appear before RU content in HTML body");
    }

    @Test
    @DisplayName("Password-reset EN content appears before RU content in plain text")
    void passwordResetText_enBeforeRu() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createPasswordResetEmail("user@test.com", "token456");

        String text = msg.getTextBody();
        int enIndex = text.indexOf("reset your password");
        int ruIndex = text.indexOf("восстановление пароля");
        assertTrue(enIndex >= 0, "EN content must be present");
        assertTrue(ruIndex >= 0, "RU content must be present");
        assertTrue(enIndex < ruIndex,
                "EN content must appear before RU content in text body");
    }

    // ============================================================
    // Blocker 2 — URL building with configured base URLs
    // ============================================================

    @Test
    @DisplayName("Verification link uses configured backend base URL")
    void verificationLink_usesBackendBaseUrl() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createVerificationEmail("user@test.com", "tok");

        assertTrue(msg.getHtmlBody().contains(BACKEND_URL),
                "HTML link must reference backend URL");
        assertTrue(msg.getTextBody().contains(BACKEND_URL),
                "Text link must reference backend URL");
    }

    @Test
    @DisplayName("Verification link uses /api/auth/verify-email path")
    void verificationLink_usesCorrectPath() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createVerificationEmail("user@test.com", "tok");

        assertTrue(msg.getHtmlBody().contains("/api/auth/verify-email"),
                "HTML link must contain /api/auth/verify-email path");
        assertTrue(msg.getTextBody().contains("/api/auth/verify-email"),
                "Text link must contain /api/auth/verify-email path");
    }

    @Test
    @DisplayName("Password-reset link uses configured frontend base URL")
    void passwordResetLink_usesFrontendBaseUrl() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createPasswordResetEmail("user@test.com", "tok");

        assertTrue(msg.getHtmlBody().contains(FRONTEND_URL),
                "HTML link must reference frontend URL");
        assertTrue(msg.getTextBody().contains(FRONTEND_URL),
                "Text link must reference frontend URL");
    }

    @Test
    @DisplayName("Password-reset link uses /app/auth/reset-password path")
    void passwordResetLink_usesCorrectPath() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createPasswordResetEmail("user@test.com", "tok");

        assertTrue(msg.getHtmlBody().contains("/app/auth/reset-password"),
                "HTML link must contain /app/auth/reset-password path");
        assertTrue(msg.getTextBody().contains("/app/auth/reset-password"),
                "Text link must contain /app/auth/reset-password path");
    }

    @Test
    @DisplayName("Token with special characters is correctly URL-encoded in the link")
    void token_isUrlEncoded() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        String tokenWithSpecialChars = "abc def&token?=123+";
        EmailMessage msg = service.createVerificationEmail("user@test.com", tokenWithSpecialChars);

        String html = msg.getHtmlBody();
        String text = msg.getTextBody();

        // Space → + (application/x-www-form-urlencoded), & → %26, ? → %3F, + → %2B
        String expectedQuery = "token=abc+def%26token%3F%3D123%2B";
        assertTrue(html.contains(expectedQuery),
                "Token with special chars must be URL-encoded in HTML");
        assertTrue(text.contains(expectedQuery),
                "Token must be URL-encoded in text body");
    }

    @Test
    @DisplayName("Custom base URLs change generated links")
    void customBaseUrls_changeGeneratedLinks() {
        String customFrontend = "https://app.mysite.com";
        String customBackend = "https://api.mysite.com";
        service = createService(customFrontend, customBackend);

        EmailMessage verifyMsg = service.createVerificationEmail("user@test.com", "tok");
        assertTrue(verifyMsg.getHtmlBody().contains(customBackend),
                "Verification link must use custom backend URL");

        EmailMessage resetMsg = service.createPasswordResetEmail("user@test.com", "tok");
        assertTrue(resetMsg.getHtmlBody().contains(customFrontend),
                "Reset link must use custom frontend URL");
    }

    @Test
    @DisplayName("Trailing slashes on base URLs do not cause double-slash in links")
    void trailingSlashes_doNotCauseDoubleSlash() {
        service = createService("http://localhost:5173/", "http://localhost:8080/");
        EmailMessage msg = service.createVerificationEmail("user@test.com", "tok");

        String html = msg.getHtmlBody();
        // Should NOT contain double-slash after base URL
        int baseUrlIndex = html.indexOf("http://localhost:8080");
        if (baseUrlIndex >= 0) {
            String afterBase = html.substring(baseUrlIndex + "http://localhost:8080".length());
            assertFalse(afterBase.startsWith("//"),
                    "Must not produce double-slash after base URL");
        }
    }

    // ============================================================
    // Subject line format
    // ============================================================

    @Test
    @DisplayName("Verification subject is combined EN/RU")
    void verificationSubject_isCombined() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createVerificationEmail("user@test.com", "tok");

        assertEquals("Verify your email / Подтвердите email — ResumAIner",
                msg.getSubject());
    }

    @Test
    @DisplayName("Password-reset subject is combined EN/RU")
    void passwordResetSubject_isCombined() {
        service = createService(FRONTEND_URL, BACKEND_URL);
        EmailMessage msg = service.createPasswordResetEmail("user@test.com", "tok");

        assertEquals("Reset your password / Восстановление пароля — ResumAIner",
                msg.getSubject());
    }

    // ============================================================
    // Edge cases
    // ============================================================

    // Note: null base URLs cannot be tested without Spring context.
    // In production, @Value will either inject the configured value or the
    // default from application.properties. Null injection is not possible
    // through normal Spring wiring with defaults defined.
}
