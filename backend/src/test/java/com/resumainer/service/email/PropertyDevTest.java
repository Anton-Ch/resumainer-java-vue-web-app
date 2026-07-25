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
 * DEV profile: uses the SAME production {@link WebConfig#propertySourcesPlaceholderConfigurer()}
 * to load {@code application.properties} → {@code application-dev.properties}.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PropertyDevTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class PropertyDevTest {

    private static final String PROFILE_KEY = "spring.profiles.active";
    private static String previous;

    @BeforeAll
    static void setDev() { previous = setOrClear(PROFILE_KEY, "dev"); }

    @AfterAll
    static void restore() { setOrClear(PROFILE_KEY, previous); }

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
    @DisplayName("DEV: application-dev.properties loaded via WebConfig → dev-fallback-enabled=true")
    void devFallbackTrue() {
        assertTrue(emailServiceImpl.toString().contains("devFallbackEnabled=true"),
                "WebConfig.propertySourcesPlaceholderConfigurer() must load dev profile → dev-fallback-enabled=true");
    }
}
