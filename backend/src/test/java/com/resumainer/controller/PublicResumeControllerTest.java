package com.resumainer.controller;

import com.resumainer.dao.SavedResumeDao;
import com.resumainer.service.GeneratedFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T157-T158 RED tests: PublicResumeController must serve PDF without authentication
 * at GET /{username}/{publicCode} — NOT inside /api/generate/**.
 */
class PublicResumeControllerTest {

    private SavedResumeDao savedResumeDao;
    private GeneratedFileStorageService fileStorage;
    private PublicResumeController controller;

    @BeforeEach
    void setUp() {
        savedResumeDao = mock(SavedResumeDao.class);
        fileStorage = mock(GeneratedFileStorageService.class);
        controller = new PublicResumeController(savedResumeDao, fileStorage);
    }

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
    }

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
}
