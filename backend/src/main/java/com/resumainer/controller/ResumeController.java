package com.resumainer.controller;

import com.resumainer.dto.UserSession;
import com.resumainer.dto.home.HomeSavedResumeDto;
import com.resumainer.model.PagedResponse;
import com.resumainer.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * REST controller for saved resume operations.
 * <p>
 * Handles paginated listing (with search/filter/sort) and soft-delete.
 * All endpoints require authentication (handled by AuthInterceptor).
 */
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * Paginated list of saved resumes for the authenticated user.
     *
     * @param userSession     the authenticated user session
     * @param search          optional search text
     * @param language        optional language filter (comma-separated)
     * @param adaptationLevel optional adaptation level filter
     * @param createdDate     optional created date filter (YYYY-MM-DD)
     * @param sort            sort field and direction (e.g., "createdAt,desc")
     * @param page            page number (default 0)
     * @param size            page size (default 10, must be 10/20/50)
     * @return paginated response with resume items
     */
    @GetMapping
    public ResponseEntity<PagedResponse<HomeSavedResumeDto>> listResumes(
            @SessionAttribute(value = "user", required = false) UserSession userSession,
            HttpServletRequest request,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String adaptationLevel,
            @RequestParam(required = false) String createdDate,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("listResumes: userId={}, search={}, page={}, size={}", userSession.getUserId(), search, page, size);

        try {
            PagedResponse<HomeSavedResumeDto> result = resumeService.listResumes(
                    userSession.getUserId(), request, search, language, adaptationLevel,
                    createdDate, dateFrom, dateTo, sort, page, size);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid params for listResumes: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error listing resumes for user {}: {}", userSession.getUserId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Soft-delete a saved resume (owner-protected).
     *
     * @param userSession the authenticated user session
     * @param id          the resume ID to delete
     * @return 200 with message on success, 404 if not found/not owned
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResume(
            @SessionAttribute(value = "user", required = false) UserSession userSession,
            @PathVariable long id) {

        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("deleteResume: userId={}, resumeId={}", userSession.getUserId(), id);

        try {
            boolean deleted = resumeService.deleteResume(userSession.getUserId(), id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Resume deleted"));
            }
            // Non-owned and non-existent: same generic response (SEC-003)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Failed to delete resume."));
        } catch (Exception e) {
            log.error("Error deleting resume {} for user {}: {}", id, userSession.getUserId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete resume."));
        }
    }
}
