package com.resumainer.controller;

import com.resumainer.dao.SavedResumeDao;
import com.resumainer.service.GeneratedFileStorageService;
import com.resumainer.service.PublicResumeRateLimiter;
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
 * <p>
 * Rate limited: in-memory, 10 requests per 60 seconds per IP address.
 */
@Controller
public class PublicResumeController {

    private static final Logger log = LoggerFactory.getLogger(PublicResumeController.class);

    private final SavedResumeDao savedResumeDao;
    private final GeneratedFileStorageService fileStorage;
    private final PublicResumeRateLimiter rateLimiter;

    public PublicResumeController(SavedResumeDao savedResumeDao,
                                   GeneratedFileStorageService fileStorage,
                                   PublicResumeRateLimiter rateLimiter) {
        this.savedResumeDao = savedResumeDao;
        this.fileStorage = fileStorage;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/{username}/{publicCode}")
    public ResponseEntity<Resource> publicResume(@PathVariable String username,
                                                  @PathVariable String publicCode,
                                                  jakarta.servlet.http.HttpServletRequest request) {
        if (username == null || username.isBlank() || publicCode == null || publicCode.isBlank()) {
            return publicNotFound();
        }

        // Rate limit check — only for public PDF route
        String clientIp = request.getRemoteAddr();
        PublicResumeRateLimiter.RateLimitResult rateLimit = rateLimiter.checkRateLimit(clientIp);
        if (!rateLimit.allowed) {
            log.warn("PUBLIC_PDF_RATE_LIMITED ip={}", clientIp);
            return ResponseEntity.status(429)
                    .header("Retry-After", String.valueOf(rateLimit.retryAfterSeconds))
                    .build();
        }

        String maskedIp = clientIp;
        if (maskedIp != null && maskedIp.contains(".")) {
            maskedIp = maskedIp.substring(0, maskedIp.lastIndexOf('.') + 1) + "x";
        }
        log.info("PUBLIC_PDF_ACCESS user={} code={} ip={}", username, publicCode, maskedIp);

        String pdfPath = savedResumeDao.findPdfPathByUsernameAndCode(username, publicCode);
        if (pdfPath == null) {
            return publicNotFound();
        }

        try {
            Path filePath = fileStorage.resolveSafePath(pdfPath);
            log.debug("PUBLIC_PDF resolved path: {}", filePath);
            Resource resource = new FileSystemResource(filePath);
            boolean exists = resource.exists();
            log.debug("PUBLIC_PDF resource exists: {} path={}", exists, filePath);
            if (!exists) {
                return publicNotFound();
            }
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "inline; filename=\"resume.pdf\"")
                    .body(resource);
        } catch (SecurityException e) {
            log.warn("Path safety rejected for user={} code={}", username, publicCode);
            return publicNotFound();
        } catch (Exception e) {
            log.error("Error serving public PDF for code: {}", publicCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Returns a 404 NOT FOUND response with a uniform timing-mitigation delay.
     * All public 404 branches use this helper to prevent timing-based
     * enumeration of valid usernames and public codes.
     */
    private ResponseEntity<Resource> publicNotFound() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
