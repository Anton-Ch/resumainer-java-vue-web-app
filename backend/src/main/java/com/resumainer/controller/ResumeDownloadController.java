package com.resumainer.controller;

import com.resumainer.dao.SavedResumeDao;
import com.resumainer.dto.UserSession;
import com.resumainer.exception.ServiceException;
import com.resumainer.service.GeneratedFileStorageService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Standalone controller for resume file downloads.
 * Mapped at /api/resumes/ (without /generate/ prefix) as a fallback
 * for browsers that cache old JS with the legacy URL pattern.
 */
@RestController
@RequestMapping("/api")
public class ResumeDownloadController {

    private static final Logger log = LoggerFactory.getLogger(ResumeDownloadController.class);

    private final SavedResumeDao savedResumeDao;
    private final GeneratedFileStorageService fileStorage;

    public ResumeDownloadController(SavedResumeDao savedResumeDao,
                                     GeneratedFileStorageService fileStorage) {
        this.savedResumeDao = savedResumeDao;
        this.fileStorage = fileStorage;
    }

    @GetMapping("/resumes/{savedResumeId}/html")
    public ResponseEntity<Resource> downloadHtml(HttpSession session,
                                                  @PathVariable long savedResumeId) {
        UUID userId = getUserId(session);
        log.debug("GET /api/resumes/{}/html (legacy path) — userId={}", savedResumeId, userId);

        SavedResumeDao.SavedResumeRow row = savedResumeDao.findById(savedResumeId, userId);
        if (row == null || row.htmlFilePath == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            Path filePath = fileStorage.resolvePath(row.htmlFilePath);
            Resource resource = new FileSystemResource(filePath);
            if (!resource.exists()) {
                log.warn("HTML file not found: {}", row.htmlFilePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"resume-" + savedResumeId + ".html\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error serving HTML: {}", savedResumeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private UUID getUserId(HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute("user");
        if (userSession == null) {
            throw new ServiceException("auth.unauthorized", "Not authenticated");
        }
        return userSession.getUserId();
    }
}
