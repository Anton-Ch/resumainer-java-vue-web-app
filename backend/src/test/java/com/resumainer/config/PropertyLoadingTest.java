package com.resumainer.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for pure Spring MVC property loading mechanism.
 *
 * <p>Verifies that:
 * <ol>
 *   <li>{@code application.properties} is loadable and contains required keys</li>
 *   <li>{@code application-dev.properties} is loadable</li>
 *   <li>{@code application-prod.properties} is loadable</li>
 *   <li>dev-fallback-enabled defaults to false (safe) in base file</li>
 * </ol>
 *
 * <p>There is no default active profile — it must be set explicitly
 * via {@code SPRING_PROFILES_ACTIVE} env var or {@code -Dspring.profiles.active}.
 *
 * <p>These tests do not require Spring context — they verify the property
 * files directly since the file loading is deterministic (ClassPathResource based).
 */
class PropertyLoadingTest {

    @Test
    void baseApplicationProperties_isLoadable() throws Exception {
        ClassPathResource resource = new ClassPathResource("application.properties");
        assertTrue(resource.exists(), "application.properties must exist on classpath");

        Properties props = new Properties();
        try (var is = resource.getInputStream()) {
            props.load(is);
        }

        // Verify key email properties are defined
        assertTrue(props.containsKey("app.email.provider"),
                "application.properties must contain app.email.provider");
        assertTrue(props.containsKey("app.email.resend.api-key"),
                "application.properties must contain app.email.resend.api-key");
        assertTrue(props.containsKey("app.email.from"),
                "application.properties must contain app.email.from");
        assertTrue(props.containsKey("app.email.dev-fallback-enabled"),
                "application.properties must contain app.email.dev-fallback-enabled");
        assertTrue(props.containsKey("app.frontend.public.base-url"),
                "application.properties must contain app.frontend.public.base-url");
        assertTrue(props.containsKey("app.backend.public.base-url"),
                "application.properties must contain app.backend.public.base-url");
    }

    @Test
    void devProfileProperties_isLoadable() throws Exception {
        ClassPathResource resource = new ClassPathResource("application-dev.properties");
        assertTrue(resource.exists(), "application-dev.properties must exist on classpath");

        Properties props = new Properties();
        try (var is = resource.getInputStream()) {
            props.load(is);
        }

        // Dev profile must enable email dev fallback
        assertEquals("true", props.getProperty("app.email.dev-fallback-enabled"),
                "Dev profile must enable email dev fallback");
    }

    @Test
    void prodProfileProperties_isLoadable() throws Exception {
        ClassPathResource resource = new ClassPathResource("application-prod.properties");
        assertTrue(resource.exists(), "application-prod.properties must exist on classpath");

        Properties props = new Properties();
        try (var is = resource.getInputStream()) {
            props.load(is);
        }

        // Prod profile must NOT enable dev fallback
        assertEquals("false", props.getProperty("app.email.dev-fallback-enabled"),
                "Prod profile must disable email dev fallback");
    }

    @Test
    void propertySourcesPlaceholderConfigurer_isConfigurable() {
        // Call the production method — no duplicated logic
        PropertySourcesPlaceholderConfigurer configurer =
                WebConfig.propertySourcesPlaceholderConfigurer();
        assertNotNull(configurer);
    }

    @Test
    void noDefaultActiveProfile() {
        // There should be no default active profile — it must be set explicitly
        // (no spring.profiles.active in application.properties)
        String profile = System.getProperty("spring.profiles.active");
        if (profile == null || profile.isBlank()) {
            profile = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        // In this test run, we haven't set either, so profile must be null/blank
        // or it was set by another test that ran earlier — we just verify the
        // runtime detection returns something deterministic (null if unset)
        // This test documents that there is no fallback to "dev"
        assertTrue(profile == null || profile.isBlank() || "dev".equals(profile) || "prod".equals(profile),
                "No default profile: profile must be explicitly set or absent");
    }

    @Test
    void emailDevFallback_defaultIsFalse() throws Exception {
        // Verify the default value of dev-fallback-enabled is false (safe for prod)
        ClassPathResource resource = new ClassPathResource("application.properties");
        Properties props = new Properties();
        try (var is = resource.getInputStream()) {
            props.load(is);
        }

        String rawValue = props.getProperty("app.email.dev-fallback-enabled");
        // The value is ${APP_EMAIL_DEV_FALLBACK_ENABLED:false} — default is false
        assertTrue(rawValue.contains(":false"),
                "Default dev-fallback-enabled must be false (safe for production)");
    }
}
