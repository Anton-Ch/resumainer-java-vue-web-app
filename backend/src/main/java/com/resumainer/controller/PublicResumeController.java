package com.resumainer.controller;

import com.resumainer.dao.SavedResumeDao;
import com.resumainer.service.GeneratedFileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;

/**
 * Public resume PDF route — accessible without authentication.
 * Serves only the finalized PDF artifact; no HTML, no cover letter, no metadata.
 * Route: GET /{username}/{publicCode}
 */
@Controller
public class PublicResumeController {

    private static final Logger log = LoggerFactory.getLogger(PublicResumeController.class);

    private final SavedResumeDao savedResumeDao;
    private final GeneratedFileStorageService fileStorage;

    public PublicResumeController(SavedResumeDao savedResumeDao,
                                   GeneratedFileStorageService fileStorage) {
        this.savedResumeDao = savedResumeDao;
        this.fileStorage = fileStorage;
    }

    @GetMapping("/{username}/{publicCode}")
    public ResponseEntity<Resource> publicResume(@PathVariable String username,
                                                  @PathVariable String publicCode,
                                                  jakarta.servlet.http.HttpServletRequest request) {
        if (username == null || username.isBlank() || publicCode == null || publicCode.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        String maskedIp = request.getRemoteAddr();
        if (maskedIp != null && maskedIp.contains(".")) {
            maskedIp = maskedIp.substring(0, maskedIp.lastIndexOf('.') + 1) + "x";
        }
        log.info("PUBLIC_PDF_ACCESS user={} code={} ip={}", username, publicCode, maskedIp);

        String pdfPath = savedResumeDao.findPdfPathByUsernameAndCode(username, publicCode);
        if (pdfPath == null) {
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            Path filePath = fileStorage.resolveSafePath(pdfPath);
            Resource resource = new FileSystemResource(filePath);
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "inline; filename=\"resume.pdf\"")
                    .body(resource);
        } catch (SecurityException e) {
            log.warn("Path safety rejected for user={} code={}", username, publicCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Error serving public PDF for code: {}", publicCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
