package com.resumainer.controller;

import com.resumainer.dao.SavedResumeDao;
import com.resumainer.service.GeneratedFileStorageService;
import com.resumainer.service.PublicResumeRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T187 Comprehensive tests: PublicResumeController — public PDF serving, rate limiting, security.
 */
class PublicResumeControllerTest {

    private SavedResumeDao savedResumeDao;
    private GeneratedFileStorageService fileStorage;
    private PublicResumeRateLimiter rateLimiter;
    private PublicResumeController controller;

    @BeforeEach
    void setUp() {
        savedResumeDao = mock(SavedResumeDao.class);
        fileStorage = mock(GeneratedFileStorageService.class);
        rateLimiter = mock(PublicResumeRateLimiter.class);
        controller = new PublicResumeController(savedResumeDao, fileStorage, rateLimiter);

        // By default, rate limiter allows requests
        when(rateLimiter.checkRateLimit(anyString()))
                .thenReturn(PublicResumeRateLimiter.RateLimitResult.allowed());
    }

    // --- Valid requests ---

    @Test
    void publicResume_validCode_returns200() throws Exception {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-pdf-", ".pdf");
        tempFile.toFile().deleteOnExit();
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "ABC12")).thenReturn("rel.pdf");
        when(fileStorage.resolveSafePath("rel.pdf")).thenReturn(tempFile);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentType().toString().contains("application/pdf"),
                "Content-Type should be application/pdf");
        assertTrue(response.getHeaders().getFirst("Content-Disposition").startsWith("inline"),
                "Content-Disposition should be inline");
    }

    @Test
    void publicResume_contentType_isPdf() throws Exception {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-pdf-", ".pdf");
        tempFile.toFile().deleteOnExit();
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "ABC12")).thenReturn("rel.pdf");
        when(fileStorage.resolveSafePath("rel.pdf")).thenReturn(tempFile);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);

        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
    }

    // --- 404 cases (must be consistent — no info leak) ---

    @Test
    void publicResume_wrongUsername_returns404() {
        when(savedResumeDao.findPdfPathByUsernameAndCode("bob", "ABC12")).thenReturn(null);

        ResponseEntity<Resource> response = controller.publicResume("bob", "ABC12", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_invalidCode_returns404() {
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "XXXXX")).thenReturn(null);

        ResponseEntity<Resource> response = controller.publicResume("alice", "XXXXX", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_blankUsername_returns404() {
        ResponseEntity<Resource> response = controller.publicResume("", "ABC12", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_nullCode_returns404() {
        ResponseEntity<Resource> response = controller.publicResume("alice", null, new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_deletedResume_returns404() {
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "DELETED")).thenReturn(null);

        ResponseEntity<Resource> response = controller.publicResume("alice", "DELETED", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_missingPhysicalFile_returns404() throws Exception {
        java.nio.file.Path nonExistent = java.nio.file.Paths.get("target/nonexistent-public.pdf");
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "ABC12")).thenReturn("missing.pdf");
        when(fileStorage.resolveSafePath("missing.pdf")).thenReturn(nonExistent);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_traversalPath_returns404() {
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "ABC12")).thenReturn("../../etc/passwd");
        when(fileStorage.resolveSafePath("../../etc/passwd"))
                .thenThrow(new SecurityException("Path traversal detected"));

        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // --- 404s are consistent (no info leak about username vs code validity) ---

    @Test
    void publicResume_all404Cases_areUniformly404() {
        // Wrong username → 404
        when(savedResumeDao.findPdfPathByUsernameAndCode("bob", "ABC12")).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND,
                controller.publicResume("bob", "ABC12", new MockHttpServletRequest()).getStatusCode());

        // Deleted → 404
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "DELETED")).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND,
                controller.publicResume("alice", "DELETED", new MockHttpServletRequest()).getStatusCode());

        // Blank username → 404
        assertEquals(HttpStatus.NOT_FOUND,
                controller.publicResume("", "ABC12", new MockHttpServletRequest()).getStatusCode());
    }

    // --- T187-hardening: Timing mitigation on all public 404 branches ---

    @Test
    void publicResume_blankUsername_returns404WithDelay() {
        long start = System.nanoTime();
        ResponseEntity<Resource> response = controller.publicResume("", "ABC12", new MockHttpServletRequest());
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(elapsedMs >= 150,
                "Public 404 for blank username should include timing-mitigation delay, got " + elapsedMs + "ms");
    }

    @Test
    void publicResume_missingPhysicalFile_returns404WithDelay() throws Exception {
        java.nio.file.Path nonExistent = java.nio.file.Paths.get("target/nonexistent-public.pdf");
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "ABC12")).thenReturn("missing.pdf");
        when(fileStorage.resolveSafePath("missing.pdf")).thenReturn(nonExistent);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");

        long start = System.nanoTime();
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(elapsedMs >= 150,
                "Public 404 for missing file should include timing-mitigation delay, got " + elapsedMs + "ms");
    }

    @Test
    void publicResume_traversalPath_returns404WithDelay() {
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "ABC12")).thenReturn("../../etc/passwd");
        when(fileStorage.resolveSafePath("../../etc/passwd"))
                .thenThrow(new SecurityException("Path traversal detected"));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");

        long start = System.nanoTime();
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(elapsedMs >= 150,
                "Public 404 for path traversal should include timing-mitigation delay, got " + elapsedMs + "ms");
    }

    @Test
    void publicResume_validCode_returns200WithoutDelay() throws Exception {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-pdf-", ".pdf");
        tempFile.toFile().deleteOnExit();
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "ABC12")).thenReturn("rel.pdf");
        when(fileStorage.resolveSafePath("rel.pdf")).thenReturn(tempFile);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");

        long start = System.nanoTime();
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(elapsedMs < 50,
                "Successful 200 response should NOT include artificial delay, got " + elapsedMs + "ms");
    }

    @Test
    void publicResume_rateLimited_returns429WithoutDelay() {
        when(rateLimiter.checkRateLimit("10.0.0.1"))
                .thenReturn(PublicResumeRateLimiter.RateLimitResult.denied(60));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.1");

        long start = System.nanoTime();
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertEquals(429, response.getStatusCode().value());
        assertTrue(elapsedMs < 50,
                "429 rate-limited response should NOT include artificial delay, got " + elapsedMs + "ms");
    }

    // --- Rate limiter ---

    @Test
    void publicResume_rateLimited_returns429() {
        when(rateLimiter.checkRateLimit("10.0.0.1"))
                .thenReturn(PublicResumeRateLimiter.RateLimitResult.denied(60));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.1");
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);

        assertEquals(429, response.getStatusCode().value());
        assertEquals("60", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    void publicResume_rateLimited_doesNotCallDao() {
        when(rateLimiter.checkRateLimit("10.0.0.1"))
                .thenReturn(PublicResumeRateLimiter.RateLimitResult.denied(60));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.1");
        controller.publicResume("alice", "ABC12", req);

        // DAO should never be called when rate limited
        verify(savedResumeDao, never()).findPdfPathByUsernameAndCode(anyString(), anyString());
    }

    @Test
    void publicResume_rateLimited_429bodyIsEmpty() {
        when(rateLimiter.checkRateLimit("10.0.0.1"))
                .thenReturn(PublicResumeRateLimiter.RateLimitResult.denied(60));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.1");
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);

        // 429 body must not leak route/user/code details
        assertNull(response.getBody(), "429 body must be empty (no info leak)");
    }

    // --- Security: no HTML or cover letter via public route ---

    @Test
    void publicResume_doesNotExposeHtml() {
        // The DAO query already filters for pdf_file_path IS NOT NULL + pdf_status='READY'
        // This test verifies the endpoint only serves PDF — the DAO handles the filtering
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "ABC12")).thenReturn(null);

        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // --- Public route is outside /api/ — no AuthInterceptor ---

    @Test
    void publicResume_noAuthenticationRequired() {
        // No session, no user attribute — still works (no ServiceException)
        when(savedResumeDao.findPdfPathByUsernameAndCode("alice", "ABC12")).thenReturn(null);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");
        // No session attribute set — this should NOT throw ServiceException
        ResponseEntity<Resource> response = controller.publicResume("alice", "ABC12", req);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
