package com.resumainer.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PublicUrlService.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PublicUrlServiceTest {

    @Mock
    private HttpServletRequest request;

    private ListAppender<ILoggingEvent> logWatcher;
    private Logger publicUrlServiceLogger;

    @BeforeEach
    void setUp() {
        // Attach ListAppender to capture WARN logs from PublicUrlService
        publicUrlServiceLogger = (Logger) LoggerFactory.getLogger(PublicUrlService.class);
        logWatcher = new ListAppender<>();
        logWatcher.start();
        publicUrlServiceLogger.addAppender(logWatcher);
    }

    @AfterEach
    void tearDown() {
        publicUrlServiceLogger.detachAppender(logWatcher);
    }

    // --- configured base URL ---

    @Test
    void buildPublicUrl_usesConfiguredBaseUrl_whenNonBlank() {
        PublicUrlService service = new PublicUrlService("http://localhost:8080");
        String result = service.buildPublicUrl("johndoe", "GTFQ");
        assertEquals("http://localhost:8080/johndoe/GTFQ", result);
    }

    @Test
    void buildPublicUrl_usesConfiguredBaseUrl_withHttps() {
        PublicUrlService service = new PublicUrlService("https://resumainer.com");
        String result = service.buildPublicUrl("johndoe", "GTFQ");
        assertEquals("https://resumainer.com/johndoe/GTFQ", result);
    }

    // --- trailing slash ---

    @Test
    void buildPublicUrl_trimsTrailingSlash_fromConfiguredBaseUrl() {
        PublicUrlService service = new PublicUrlService("http://localhost:8080/");
        String result = service.buildPublicUrl("johndoe", "GTFQ");
        assertEquals("http://localhost:8080/johndoe/GTFQ", result);
    }

    @Test
    void buildPublicUrl_trimsTrailingSlashes_fromConfiguredBaseUrl() {
        PublicUrlService service = new PublicUrlService("http://localhost:8080///");
        String result = service.buildPublicUrl("johndoe", "GTFQ");
        assertEquals("http://localhost:8080/johndoe/GTFQ", result);
    }

    // --- forwarded headers ---

    @Test
    void buildPublicUrl_usesForwardedHeaders_whenConfigAbsent() {
        PublicUrlService service = new PublicUrlService(null);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("resumainer.com");

        String result = service.buildPublicUrl("johndoe", "GTFQ", request);
        assertEquals("https://resumainer.com/johndoe/GTFQ", result);

        // Should NOT have logged a WARN because forwarded headers were used
        assertTrue(logWatcher.list.isEmpty(), "No WARN expected when forwarded headers are present");
    }

    @Test
    void buildPublicUrl_usesXForwardedProtoAndHost_withPort() {
        PublicUrlService service = new PublicUrlService(null);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("resumainer.com:443");

        String result = service.buildPublicUrl("johndoe", "GTFQ", request);
        assertEquals("https://resumainer.com:443/johndoe/GTFQ", result);
    }

    // --- request origin fallback ---

    @Test
    void buildPublicUrl_fallsBackToRequestOrigin_whenNoConfigAndNoForwardedHeaders() {
        PublicUrlService service = new PublicUrlService(null);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);

        String result = service.buildPublicUrl("johndoe", "GTFQ", request);
        assertEquals("http://localhost:8080/johndoe/GTFQ", result);
    }

    @Test
    void buildPublicUrl_fallbackOmitsDefaultPort80() {
        PublicUrlService service = new PublicUrlService(null);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("example.com");
        when(request.getServerPort()).thenReturn(80);

        String result = service.buildPublicUrl("johndoe", "GTFQ", request);
        assertEquals("http://example.com/johndoe/GTFQ", result);
    }

    @Test
    void buildPublicUrl_fallbackOmitsDefaultPort443() {
        PublicUrlService service = new PublicUrlService(null);
        when(request.getScheme()).thenReturn("https");
        when(request.getServerName()).thenReturn("example.com");
        when(request.getServerPort()).thenReturn(443);

        String result = service.buildPublicUrl("johndoe", "GTFQ", request);
        assertEquals("https://example.com/johndoe/GTFQ", result);
    }

    // --- WARN log assertion ---

    @Test
    void buildPublicUrl_logsWarn_whenFallbackOriginIsUsed() {
        PublicUrlService service = new PublicUrlService(null);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);

        service.buildPublicUrl("johndoe", "GTFQ", request);

        boolean foundWarn = logWatcher.list.stream()
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("APP_PUBLIC_BASE_URL is not configured"));
        assertTrue(foundWarn, "Expected WARN log when APP_PUBLIC_BASE_URL is not configured and fallback is used");
    }

    // --- no double slash ---

    @Test
    void buildPublicUrl_noDoubleSlash_betweenBaseAndUsername() {
        PublicUrlService service = new PublicUrlService("http://localhost:8080");
        String result = service.buildPublicUrl("johndoe", "GTFQ");
        assertFalse(result.contains("//johndoe"), "Should not contain double slash before username");
        assertFalse(result.contains("//GTFQ"), "Should not contain double slash before code");
    }

    // --- no-request overload ---

    @Test
    void buildPublicUrl_noRequestOverload_withConfiguredBase_returnsAbsoluteUrl() {
        PublicUrlService service = new PublicUrlService("https://resumainer.com");
        String result = service.buildPublicUrl("johndoe", "GTFQ");
        assertTrue(result.startsWith("https://"), "Should return absolute URL");
        assertEquals("https://resumainer.com/johndoe/GTFQ", result);
    }

    @Test
    void buildPublicUrl_noRequestOverload_withoutConfiguredBase_throwsIllegalStateException() {
        PublicUrlService service = new PublicUrlService(null);
        assertThrows(IllegalStateException.class,
                () -> service.buildPublicUrl("johndoe", "GTFQ"));
    }

    // --- validation ---

    @Test
    void buildPublicUrl_throwsOnNullUsername() {
        PublicUrlService service = new PublicUrlService("http://localhost:8080");
        assertThrows(IllegalArgumentException.class,
                () -> service.buildPublicUrl(null, "GTFQ"));
    }

    @Test
    void buildPublicUrl_throwsOnBlankUsername() {
        PublicUrlService service = new PublicUrlService("http://localhost:8080");
        assertThrows(IllegalArgumentException.class,
                () -> service.buildPublicUrl("  ", "GTFQ"));
    }

    @Test
    void buildPublicUrl_throwsOnNullPublicCode() {
        PublicUrlService service = new PublicUrlService("http://localhost:8080");
        assertThrows(IllegalArgumentException.class,
                () -> service.buildPublicUrl("johndoe", null));
    }

    @Test
    void buildPublicUrl_throwsOnBlankPublicCode() {
        PublicUrlService service = new PublicUrlService("http://localhost:8080");
        assertThrows(IllegalArgumentException.class,
                () -> service.buildPublicUrl("johndoe", ""));
    }

    // --- no hardcoded domain in production code ---

    @Test
    void buildPublicUrl_allowsUrlSafeUsernameAndCode() {
        PublicUrlService service = new PublicUrlService("http://localhost:8080");
        String result = service.buildPublicUrl("john.doe", "ABC-123");
        assertEquals("http://localhost:8080/john.doe/ABC-123", result);
    }

    @Test
    void buildPublicUrl_noHardcodedLocalhost_whenConfiguredWithDomain() {
        PublicUrlService service = new PublicUrlService("https://resumainer.com");
        String result = service.buildPublicUrl("johndoe", "GTFQ");
        assertFalse(result.contains("localhost"), "Should not contain localhost when domain is configured");
        assertFalse(result.contains("127.0.0.1"));
    }

    // --- configured base takes priority over forwarded headers ---

    @Test
    void buildPublicUrl_configuredBaseTakesPriority_overForwardedHeaders() {
        PublicUrlService service = new PublicUrlService("https://configured.com");
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("http");
        when(request.getHeader("X-Forwarded-Host")).thenReturn("forwarded.com");

        String result = service.buildPublicUrl("johndoe", "GTFQ", request);
        assertEquals("https://configured.com/johndoe/GTFQ", result,
                "Configured base must take priority over forwarded headers");
    }

    @Test
    void buildPublicUrl_configuredBaseTakesPriority_overRequestOrigin() {
        PublicUrlService service = new PublicUrlService("https://configured.com");
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("origin.com");
        when(request.getServerPort()).thenReturn(8080);

        String result = service.buildPublicUrl("johndoe", "GTFQ", request);
        assertEquals("https://configured.com/johndoe/GTFQ", result,
                "Configured base must take priority over request origin");
    }
}
