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
 * Runtime override wins over profile file.
 *
 * Uses the production {@link WebConfig#propertySourcesPlaceholderConfigurer()}.
 * Sets profile=dev (normally → dev-fallback-enabled=true from file) AND
 * overrides with system property → system property must win.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PropertyOverrideTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class PropertyOverrideTest {

    private static final String PROFILE_KEY = "spring.profiles.active";
    private static final String DEV_FALLBACK_KEY = "app.email.dev-fallback-enabled";
    private static String prevProfile, prevFallback;

    @BeforeAll
    static void setup() {
        prevProfile = setOrClear(PROFILE_KEY, "dev");
        prevFallback = setOrClear(DEV_FALLBACK_KEY, "false");
    }

    @AfterAll
    static void restore() {
        setOrClear(PROFILE_KEY, prevProfile);
        setOrClear(DEV_FALLBACK_KEY, prevFallback);
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
    @DisplayName("OVERRIDE: system property app.email.dev-fallback-enabled=false wins over dev profile")
    void runtimeOverrideWins() {
        assertTrue(emailServiceImpl.toString().contains("devFallbackEnabled=false"),
                "System property must override dev profile file via WebConfig's PropertySourcesPlaceholderConfigurer");
    }
}
