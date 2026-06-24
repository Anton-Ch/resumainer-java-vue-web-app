package com.resumainer.controller;

import com.resumainer.dao.SavedResumeDao;
import com.resumainer.model.PublicResumeLookupResult;
import com.resumainer.service.GeneratedFileStorageService;
import com.resumainer.service.PublicResumeRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static com.resumainer.model.PublicResumeLookupResult.Status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests: PublicResumeController — public PDF serving, 410/404 handling, rate limiting, security.
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

        when(rateLimiter.checkRateLimit(anyString()))
                .thenReturn(PublicResumeRateLimiter.RateLimitResult.allowed());
    }

    // --- Active resume → 200 ---

    @Test
    void publicResume_validCode_returns200() throws Exception {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-pdf-", ".pdf");
        tempFile.toFile().deleteOnExit();
        when(savedResumeDao.findPublicResumeStatus("alice", "ABC12"))
                .thenReturn(new PublicResumeLookupResult(Status.ACTIVE, "rel.pdf"));
        when(fileStorage.resolveSafePath("rel.pdf")).thenReturn(tempFile);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");
        Object result = controller.publicResume("alice", "ABC12", req);

        assertInstanceOf(ResponseEntity.class, result);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentType().toString().contains("application/pdf"));
        assertTrue(response.getHeaders().getFirst("Content-Disposition").startsWith("inline"));
    }

    // --- Deleted resume → 410 ---

    @Test
    void publicResume_deletedResume_returns410() {
        when(savedResumeDao.findPublicResumeStatus("alice", "DELETED"))
                .thenReturn(new PublicResumeLookupResult(Status.DELETED, null));

        Object result = controller.publicResume("alice", "DELETED", new MockHttpServletRequest());

        assertInstanceOf(ModelAndView.class, result);
        ModelAndView mav = (ModelAndView) result;
        assertEquals("error/410", mav.getViewName());
        assertEquals(HttpStatus.GONE.value(), mav.getStatus().value());
    }

    @Test
    void publicResume_deletedResume_returns410WithDelay() {
        when(savedResumeDao.findPublicResumeStatus("alice", "DELETED"))
                .thenReturn(new PublicResumeLookupResult(Status.DELETED, null));

        long start = System.nanoTime();
        controller.publicResume("alice", "DELETED", new MockHttpServletRequest());
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertTrue(elapsedMs >= 150,
                "410 response should include timing-mitigation delay, got " + elapsedMs + "ms");
    }

    @Test
    void publicResume_deletedResume_410bodyHasNoDynamicData() {
        when(savedResumeDao.findPublicResumeStatus("alice", "DELETED"))
                .thenReturn(new PublicResumeLookupResult(Status.DELETED, null));

        Object result = controller.publicResume("alice", "DELETED", new MockHttpServletRequest());

        assertInstanceOf(ModelAndView.class, result);
        ModelAndView mav = (ModelAndView) result;
        // 410 uses Thymeleaf template with i18n — no dynamic resume data in model
        assertTrue(mav.getModel().isEmpty(),
                "410 ModelAndView should not contain any dynamic resume data");
    }

    // --- Not found → 404 ---

    @Test
    void publicResume_wrongUsername_returns404() {
        when(savedResumeDao.findPublicResumeStatus("bob", "ABC12"))
                .thenReturn(new PublicResumeLookupResult(Status.NOT_FOUND, null));

        ResponseEntity<?> response = (ResponseEntity<?>) controller.publicResume("bob", "ABC12", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_invalidCode_returns404() {
        when(savedResumeDao.findPublicResumeStatus("alice", "XXXXX"))
                .thenReturn(new PublicResumeLookupResult(Status.NOT_FOUND, null));

        ResponseEntity<?> response = (ResponseEntity<?>) controller.publicResume("alice", "XXXXX", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_blankUsername_returns404() {
        ResponseEntity<?> response = (ResponseEntity<?>) controller.publicResume("", "ABC12", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_nullCode_returns404() {
        ResponseEntity<?> response = (ResponseEntity<?>) controller.publicResume("alice", null, new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_missingPhysicalFile_returns404() throws Exception {
        java.nio.file.Path nonExistent = Paths.get("target/nonexistent-public.pdf");
        when(savedResumeDao.findPublicResumeStatus("alice", "ABC12"))
                .thenReturn(new PublicResumeLookupResult(Status.MISSING_FILE, "missing.pdf"));
        when(fileStorage.resolveSafePath("missing.pdf")).thenReturn(nonExistent);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");

        // MISSING_FILE from lookup goes to publicNotFound since isActive() is false
        ResponseEntity<?> response = (ResponseEntity<?>) controller.publicResume("alice", "ABC12", req);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void publicResume_notActive_withoutPdf_returns404() {
        // Status is not ACTIVE — resolved before file check
        when(savedResumeDao.findPublicResumeStatus("alice", "NOPDF"))
                .thenReturn(new PublicResumeLookupResult(Status.NOT_FOUND, null));

        ResponseEntity<?> response = (ResponseEntity<?>) controller.publicResume("alice", "NOPDF", new MockHttpServletRequest());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // --- All 404/410 have uniform delay ---

    @Test
    void publicResume_all404Cases_haveUniformDelay() {
        when(savedResumeDao.findPublicResumeStatus("bob", "ABC12"))
                .thenReturn(new PublicResumeLookupResult(Status.NOT_FOUND, null));

        long start = System.nanoTime();
        controller.publicResume("bob", "ABC12", new MockHttpServletRequest());
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertTrue(elapsedMs >= 150,
                "404 should include timing-mitigation delay, got " + elapsedMs + "ms");
    }

    @Test
    void publicResume_validCode_returns200WithoutDelay() throws Exception {
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-pdf-", ".pdf");
        tempFile.toFile().deleteOnExit();
        when(savedResumeDao.findPublicResumeStatus("alice", "ABC12"))
                .thenReturn(new PublicResumeLookupResult(Status.ACTIVE, "rel.pdf"));
        when(fileStorage.resolveSafePath("rel.pdf")).thenReturn(tempFile);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");

        long start = System.nanoTime();
        controller.publicResume("alice", "ABC12", req);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertTrue(elapsedMs < 100,
                "Successful 200 should NOT include artificial delay, got " + elapsedMs + "ms");
    }

    // --- Rate limiter ---

    @Test
    void publicResume_rateLimited_returns429() {
        when(rateLimiter.checkRateLimit("10.0.0.1"))
                .thenReturn(PublicResumeRateLimiter.RateLimitResult.denied(60));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.1");
        ResponseEntity<?> response = (ResponseEntity<?>) controller.publicResume("alice", "ABC12", req);

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

        verify(savedResumeDao, never()).findPublicResumeStatus(anyString(), anyString());
    }

    @Test
    void publicResume_rateLimited_429BodyIsEmpty() {
        when(rateLimiter.checkRateLimit("10.0.0.1"))
                .thenReturn(PublicResumeRateLimiter.RateLimitResult.denied(60));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.1");
        ResponseEntity<?> response = (ResponseEntity<?>) controller.publicResume("alice", "ABC12", req);

        assertNull(response.getBody(), "429 body must be empty (no info leak)");
    }

    // --- No auth required ---

    @Test
    void publicResume_noAuthenticationRequired() {
        when(savedResumeDao.findPublicResumeStatus("alice", "ABC12"))
                .thenReturn(new PublicResumeLookupResult(Status.NOT_FOUND, null));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");
        ResponseEntity<?> response = (ResponseEntity<?>) controller.publicResume("alice", "ABC12", req);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // --- 410 has same delay as 404 ---

    @Test
    void publicResume_404and410_haveSameDelayOrder() {
        // 404 delay
        when(savedResumeDao.findPublicResumeStatus("bob", "ABC12"))
                .thenReturn(new PublicResumeLookupResult(Status.NOT_FOUND, null));
        long start404 = System.nanoTime();
        controller.publicResume("bob", "ABC12", new MockHttpServletRequest());
        long elapsed404 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start404);

        // 410 delay
        when(savedResumeDao.findPublicResumeStatus("alice", "DELETED"))
                .thenReturn(new PublicResumeLookupResult(Status.DELETED, null));
        long start410 = System.nanoTime();
        controller.publicResume("alice", "DELETED", new MockHttpServletRequest());
        long elapsed410 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start410);

        // Both should have at least ~200ms delay and the same order of magnitude
        assertTrue(elapsed404 >= 150, "404 delay too short: " + elapsed404 + "ms");
        assertTrue(elapsed410 >= 150, "410 delay too short: " + elapsed410 + "ms");
        // 410 should not be dramatically faster than 404
        assertTrue(elapsed410 > elapsed404 / 2,
                "410 delay (" + elapsed410 + "ms) should not be dramatically less than 404 (" + elapsed404 + "ms)");
    }

    // --- Route non-interception (MockMvc) ---

    @Test
    void route_interception_getApiResumes_notHandledByPublicResumeController() throws Exception {
        MockMvc mockMvc = standaloneSetup(controller).build();
        when(savedResumeDao.findPublicResumeStatus(anyString(), anyString()))
                .thenReturn(new PublicResumeLookupResult(Status.NOT_FOUND, null));

        // /api/resumes should not be matched by the public resume controller.
        // If it were matched, our controller would be called and return 404 after delay.
        mockMvc.perform(get("/api/resumes"))
                .andExpect(status().isNotFound());

        // Our controller's DAO should NOT have been called for /api/resumes
        verify(savedResumeDao, never()).findPublicResumeStatus(anyString(), anyString());
    }

    @Test
    void route_interception_getApiAnything_notHandledByPublicResumeController() throws Exception {
        MockMvc mockMvc = standaloneSetup(controller).build();

        mockMvc.perform(get("/api/anything"))
                .andExpect(status().isNotFound());

        verify(savedResumeDao, never()).findPublicResumeStatus(anyString(), anyString());
    }

    @Test
    void route_interception_getAppAuth_notHandledByPublicResumeController() throws Exception {
        MockMvc mockMvc = standaloneSetup(controller).build();

        mockMvc.perform(get("/app/auth"))
                .andExpect(status().isNotFound());

        verify(savedResumeDao, never()).findPublicResumeStatus(anyString(), anyString());
    }

    @Test
    void route_interception_getStaticCss_notHandledByPublicResumeController() throws Exception {
        MockMvc mockMvc = standaloneSetup(controller).build();

        mockMvc.perform(get("/static/css"))
                .andExpect(status().isNotFound());

        verify(savedResumeDao, never()).findPublicResumeStatus(anyString(), anyString());
    }

    @Test
    void route_interception_getAssetsApp_notHandledByPublicResumeController() throws Exception {
        MockMvc mockMvc = standaloneSetup(controller).build();

        mockMvc.perform(get("/assets/app"))
                .andExpect(status().isNotFound());

        verify(savedResumeDao, never()).findPublicResumeStatus(anyString(), anyString());
    }

    @Test
    void route_interception_getError410_notHandledByPublicResumeController() throws Exception {
        MockMvc mockMvc = standaloneSetup(controller).build();

        mockMvc.perform(get("/error/410"))
                .andExpect(status().isNotFound());

        verify(savedResumeDao, never()).findPublicResumeStatus(anyString(), anyString());
    }

    @Test
    void route_interception_validPublicRoute_isHandledByPublicResumeController() throws Exception {
        // Arrange: DAO returns ACTIVE so our controller handles the request
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-pdf-", ".pdf");
        tempFile.toFile().deleteOnExit();
        when(savedResumeDao.findPublicResumeStatus("alice", "ABC12"))
                .thenReturn(new PublicResumeLookupResult(Status.ACTIVE, "rel.pdf"));
        when(fileStorage.resolveSafePath("rel.pdf")).thenReturn(tempFile);

        MockMvc mockMvc = standaloneSetup(controller).build();

        // Assert: our controller handles it (returns 200 with PDF)
        mockMvc.perform(get("/alice/ABC12"))
                .andExpect(status().isOk());

        verify(savedResumeDao).findPublicResumeStatus("alice", "ABC12");
    }
}
