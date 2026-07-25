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
 * No active profile — safe defaults from base application.properties.
 *
 * Uses the production {@link WebConfig#propertySourcesPlaceholderConfigurer()}.
 * Sets spring.profiles.active=default explicitly to prevent an external
 * DEV/PROD env var from making this test flaky.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PropertyDefaultTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class PropertyDefaultTest {

    private static final String PROFILE_KEY = "spring.profiles.active";
    private static String previous;

    @BeforeAll
    static void setNoProfile() {
        previous = System.getProperty(PROFILE_KEY);
        // Explicitly set to "default" so no profile-specific file is loaded.
        // WebConfig.propertySourcesPlaceholderConfigurer() ignores "default".
        System.setProperty(PROFILE_KEY, "default");
    }

    @AfterAll
    static void restore() {
        if (previous != null) System.setProperty(PROFILE_KEY, previous);
        else System.clearProperty(PROFILE_KEY);
    }

    @Configuration
    static class TestConfig {
        @Bean
        public static PropertySourcesPlaceholderConfigurer pspc() {
            return WebConfig.propertySourcesPlaceholderConfigurer();
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
    private EmailServiceImpl emailServiceImpl;

    @Test
    @DisplayName("NO PROFILE: dev-fallback-enabled=false (safe default from base file)")
    void noProfileSafeDefault() {
        assertTrue(emailServiceImpl.toString().contains("devFallbackEnabled=false"),
                "Without any profile, dev-fallback-enabled must resolve to false (safe production default)");
    }

    @Test
    @DisplayName("NO PROFILE: missing API key throws (no dev fallback)")
    void noProfileMissingKeyFails() {
        assertThrows(EmailException.class,
                () -> emailServiceImpl.send(new EmailMessage("n@t.com", "T", "<p>h</p>", "t")));
    }
}
