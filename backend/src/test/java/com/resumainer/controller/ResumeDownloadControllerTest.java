package com.resumainer.controller;

import com.resumainer.dao.SavedResumeDao;
import com.resumainer.dto.UserSession;
import com.resumainer.service.GeneratedFileStorageService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ResumeDownloadControllerTest {

    private SavedResumeDao savedResumeDao;
    private GeneratedFileStorageService fileStorage;
    private HttpSession session;
    private ResumeDownloadController controller;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        savedResumeDao = mock(SavedResumeDao.class);
        fileStorage = mock(GeneratedFileStorageService.class);
        session = mock(HttpSession.class);
        controller = new ResumeDownloadController(savedResumeDao, fileStorage);

        UserSession userSession = new UserSession();
        userSession.setUserId(userId);
        when(session.getAttribute("user")).thenReturn(userSession);
    }

    @Test
    void downloadHtml_returns404_whenRowNotFound() {
        when(savedResumeDao.findById(5L, userId)).thenReturn(null);

        ResponseEntity<Resource> response = controller.downloadHtml(session, 5L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void downloadHtml_returns404_whenHtmlFilePathNull() {
        SavedResumeDao.SavedResumeRow row = new SavedResumeDao.SavedResumeRow();
        row.id = 5L;
        row.htmlFilePath = null;
        when(savedResumeDao.findById(5L, userId)).thenReturn(row);

        ResponseEntity<Resource> response = controller.downloadHtml(session, 5L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void downloadHtml_returns404_whenFileNotExists() {
        SavedResumeDao.SavedResumeRow row = new SavedResumeDao.SavedResumeRow();
        row.id = 5L;
        row.htmlFilePath = "/nonexistent/file.html";
        when(savedResumeDao.findById(5L, userId)).thenReturn(row);
        when(fileStorage.resolvePath(anyString())).thenReturn(Path.of("/nonexistent/file.html"));

        ResponseEntity<Resource> response = controller.downloadHtml(session, 5L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void downloadHtml_requiresAuthentication() {
        when(session.getAttribute("user")).thenReturn(null);

        assertThrows(com.resumainer.exception.ServiceException.class, () ->
                controller.downloadHtml(session, 5L));
    }

    @Test
    void downloadHtml_internalError_returns500() {
        SavedResumeDao.SavedResumeRow row = new SavedResumeDao.SavedResumeRow();
        row.id = 5L;
        row.htmlFilePath = "/path/to/file.html";
        when(savedResumeDao.findById(5L, userId)).thenReturn(row);
        when(fileStorage.resolvePath(anyString())).thenThrow(new RuntimeException("I/O error"));

        org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> response =
                controller.downloadHtml(session, 5L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
