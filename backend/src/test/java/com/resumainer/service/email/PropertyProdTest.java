package com.resumainer.service.email;

import com.resumainer.config.WebConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PROD profile: uses the SAME production {@link WebConfig#propertySourcesPlaceholderConfigurer()}.
 *
 * Simulates production configuration with real URLs and API key.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PropertyProdTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class PropertyProdTest {

    private static final String PROFILE_KEY = "spring.profiles.active";
    private static final String RESEND_KEY = "RESEND_API_KEY";
    private static final String FRONTEND_URL_KEY = "APP_FRONTEND_PUBLIC_BASE_URL";
    private static final String BACKEND_URL_KEY = "APP_BACKEND_PUBLIC_BASE_URL";
    private static final String PROD_FRONTEND = "https://resumainer.example";
    private static final String PROD_BACKEND = "https://api.resumainer.example";

    private static String prevProfile, prevResend, prevFrontend, prevBackend;

    @BeforeAll
    static void setProd() {
        prevProfile = setOrClear(PROFILE_KEY, "prod");
        prevResend = setOrClear(RESEND_KEY, "re_test_prod_key");
        prevFrontend = setOrClear(FRONTEND_URL_KEY, PROD_FRONTEND);
        prevBackend = setOrClear(BACKEND_URL_KEY, PROD_BACKEND);
    }

    @AfterAll
    static void restore() {
        setOrClear(PROFILE_KEY, prevProfile);
        setOrClear(RESEND_KEY, prevResend);
        setOrClear(FRONTEND_URL_KEY, prevFrontend);
        setOrClear(BACKEND_URL_KEY, prevBackend);
    }

    private static String setOrClear(String key, String value) {
        String prev = System.getProperty(key);
        if (value != null) System.setProperty(key, value);
        else System.clearProperty(key);
        return prev;
    }

    @Configuration
    static class TestConfig {
        @Bean
        public static PropertySourcesPlaceholderConfigurer pspc() {
            return WebConfig.propertySourcesPlaceholderConfigurer();
        }

        @Bean
        public EmailTemplateService emailTemplateService(
                @org.springframework.beans.factory.annotation.Value("${app.frontend.public.base-url}") String f,
                @org.springframework.beans.factory.annotation.Value("${app.backend.public.base-url}") String b) {
            return new EmailTemplateService(f, b);
        }

        @Bean
        public EmailServiceImpl emailServiceImpl(
                @org.springframework.beans.factory.annotation.Value("${app.email.resend.api-key}") String k,
                @org.springframework.beans.factory.annotation.Value("${app.email.from}") String f,
                @org.springframework.beans.factory.annotation.Value("${app.email.resend.api-url:https://api.resend.com/emails}") String u,
                @org.springframework.beans.factory.annotation.Value("${app.email.dev-fallback-enabled}") boolean d,
                @org.springframework.beans.factory.annotation.Value("${app.email.resend.timeout-ms:10000}") int t) {
            return new EmailServiceImpl(java.net.http.HttpClient.newHttpClient(), k, f, u, d, t);
        }
    }

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private EmailServiceImpl emailServiceImpl;

    @Test
    @DisplayName("PROD: beans start without unresolved placeholders")
    void beansStart() {
        assertNotNull(emailTemplateService);
        assertNotNull(emailServiceImpl);
    }

    @Test
    @DisplayName("PROD: application-prod.properties → dev-fallback-enabled=false")
    void prodFallbackFalse() {
        assertTrue(emailServiceImpl.toString().contains("devFallbackEnabled=false"),
                "PROD profile must load application-prod.properties and set dev-fallback-enabled=false");
    }

    @Test
    @DisplayName("PROD: verification email uses configured production backend URL")
    void prodVerificationUrl() {
        String token = "abc123";
        EmailMessage msg = emailTemplateService.createVerificationEmail("user@example.com", token);

        String html = msg.getHtmlBody();
        // Must use the configured prod backend URL
        assertTrue(html.contains(PROD_BACKEND + "/api/auth/verify-email?token=" + token),
                "Verification email must use prod backend URL: " + PROD_BACKEND);
        // Must NOT contain localhost
        assertFalse(html.contains("localhost"),
                "Prod verification email must not contain localhost");
        assertFalse(html.contains("127.0.0.1"),
                "Prod verification email must not contain 127.0.0.1");

        String text = msg.getTextBody();
        assertTrue(text.contains(PROD_BACKEND + "/api/auth/verify-email?token=" + token),
                "Verification text must use prod backend URL");
    }

    @Test
    @DisplayName("PROD: reset email uses configured production frontend URL")
    void prodResetUrl() {
        String token = "xyz789";
        EmailMessage msg = emailTemplateService.createPasswordResetEmail("user@example.com", token);

        String html = msg.getHtmlBody();
        assertTrue(html.contains(PROD_FRONTEND + "/app/auth/reset-password?token=" + token),
                "Reset email must use prod frontend URL: " + PROD_FRONTEND);
        assertFalse(html.contains("localhost"),
                "Prod reset email must not contain localhost");

        String text = msg.getTextBody();
        assertTrue(text.contains(PROD_FRONTEND + "/app/auth/reset-password?token=" + token),
                "Reset text must use prod frontend URL");
    }

    @Test
    @DisplayName("PROD: no API key or token exposed in exception")
    void prodNoApiKeyLeak() {
        // Create a fresh service without key to test failure
        EmailServiceImpl noKeyService = new EmailServiceImpl(
                java.net.http.HttpClient.newHttpClient(),
                "", "ResumAIner <noreply@send.resumainer.com>",
                "https://api.resend.com/emails", false, 5000);

        EmailException ex = assertThrows(EmailException.class,
                () -> noKeyService.send(new EmailMessage("p@t.com", "T", "<p>h</p>", "t")));
        assertFalse(ex.getMessage().contains("re_"),
                "Exception must not leak API key");
    }
}
