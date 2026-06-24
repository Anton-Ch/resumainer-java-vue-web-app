package com.resumainer.controller;

import com.resumainer.dao.SavedResumeDao;
import com.resumainer.model.PublicResumeLookupResult;
import com.resumainer.service.GeneratedFileStorageService;
import com.resumainer.service.PublicResumeRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.Path;

/**
 * Public resume PDF route — accessible without authentication.
 * Serves only the finalized PDF artifact; no HTML, no cover letter, no metadata.
 * <p>
 * Route: GET /{username}/{publicCode}
 * <p>
 * Status codes:
 * <ul>
 *   <li>200 — PDF served inline for active, non-deleted resume with ready PDF.
 *   <li>410 — soft-deleted known public link, Thymeleaf error page.
 *   <li>404 — invalid username/code, missing file, unsafe path, or path traversal.
 * </ul>
 * Both 404 and 410 use the same uniform artificial delay to prevent timing enumeration.
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

    @GetMapping("/{username:(?!(?:api|app|static|assets|error)$)[a-zA-Z0-9._-]+}/{publicCode:[a-zA-Z0-9]+}")
    public Object publicResume(@PathVariable String username,
                                @PathVariable String publicCode,
                                HttpServletRequest request) {
        if (username == null || username.isBlank() || publicCode == null || publicCode.isBlank()) {
            return publicNotFound();
        }

        // Rate limit check
        String clientIp = request.getRemoteAddr();
        PublicResumeRateLimiter.RateLimitResult rateLimit = rateLimiter.checkRateLimit(clientIp);
        if (!rateLimit.allowed) {
            log.warn("PUBLIC_PDF_RATE_LIMITED ip={}", clientIp);
            return ResponseEntity.status(429)
                    .header("Retry-After", String.valueOf(rateLimit.retryAfterSeconds))
                    .build();
        }

        String maskedIp = maskIp(clientIp);
        log.info("PUBLIC_PDF_ACCESS user={} code={} ip={}", username, publicCode, maskedIp);

        // Look up resume status
        PublicResumeLookupResult lookup = savedResumeDao.findPublicResumeStatus(username, publicCode);

        if (lookup.isDeleted()) {
            log.info("PUBLIC_PDF_DELETED user={} code={}", username, publicCode);
            return publicGone();
        }

        if (!lookup.isActive()) {
            return publicNotFound();
        }

        // Serve PDF
        try {
            Path filePath = fileStorage.resolveSafePath(lookup.getPdfFilePath());
            log.debug("PUBLIC_PDF resolved path: {}", filePath);
            Resource resource = new FileSystemResource(filePath);
            if (!resource.exists()) {
                log.warn("PUBLIC_PDF missing file for user={} code={}", username, publicCode);
                return publicNotFound();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
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
     * All public error branches use this helper to prevent timing-based
     * enumeration of valid usernames and public codes.
     */
    private ResponseEntity<Resource> publicNotFound() {
        applyUniformDelay();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    /**
     * Returns a 410 Gone Thymeleaf view with the same uniform delay as 404.
     */
    private ModelAndView publicGone() {
        applyUniformDelay();
        ModelAndView mav = new ModelAndView("error/410");
        mav.setStatus(HttpStatus.GONE);
        return mav;
    }

    /**
     * Applies the uniform artificial delay used by all public error responses.
     */
    private void applyUniformDelay() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String maskIp(String ip) {
        if (ip != null && ip.contains(".")) {
            return ip.substring(0, ip.lastIndexOf('.') + 1) + "x";
        }
        return ip;
    }
}
