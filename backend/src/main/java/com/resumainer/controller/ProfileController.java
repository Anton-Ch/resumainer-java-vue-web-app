package com.resumainer.controller;

import com.resumainer.dto.CoursePage;
import com.resumainer.dto.ProfileSectionStatus;
import com.resumainer.dto.UserSession;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.*;
import com.resumainer.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for profile management endpoints.
 * <p>
 * All endpoints require authentication (guarded by AuthInterceptor).
 * All responses include Cache-Control: no-store, private (SEC-005).
 * User ID is extracted from HttpSession, never from request body (SEC-001).
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // ========================================================================
    // Section Status
    // ========================================================================

    @GetMapping("/status")
    public ResponseEntity<ProfileSectionStatus> getSectionStatus(HttpSession session) {
        UUID userId = getUserId(session);
        log.debug("GET /api/profile/status — userId={}", userId);
        return noCache(ResponseEntity.ok(profileService.getSectionStatus(userId)));
    }

    // ========================================================================
    // Contact Details
    // ========================================================================

    @GetMapping("/contact")
    public ResponseEntity<?> getContactDetails(HttpSession session) {
        UUID userId = getUserId(session);
        log.debug("GET /api/profile/contact — userId={}", userId);
        ContactDetail contact = profileService.getContactDetails(userId);
        if (contact == null) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Contact details not found")));
        }
        return noCache(ResponseEntity.ok(contact));
    }

    @PutMapping("/contact")
    public ResponseEntity<?> updateContactDetails(HttpSession session,
                                                   @Valid @RequestBody ContactDetail contact) {
        UUID userId = getUserId(session);
        log.debug("PUT /api/profile/contact — userId={}", userId);
        try {
            ContactDetail updated = profileService.updateContactDetails(userId, contact);
            return noCache(ResponseEntity.ok(updated));
        } catch (ServiceException e) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage())));
        }
    }

    // ========================================================================
    // Work Experience
    // ========================================================================

    @GetMapping("/experience")
    public ResponseEntity<List<WorkExperience>> getExperiences(HttpSession session) {
        UUID userId = getUserId(session);
        log.debug("GET /api/profile/experience — userId={}", userId);
        return noCache(ResponseEntity.ok(profileService.getWorkExperiences(userId)));
    }

    @PostMapping("/experience")
    public ResponseEntity<WorkExperience> createExperience(HttpSession session,
                                                            @RequestBody WorkExperience experience) {
        UUID userId = getUserId(session);
        log.debug("POST /api/profile/experience — userId={}", userId);
        WorkExperience created = profileService.createWorkExperience(userId, experience);
        return noCache(ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @PutMapping("/experience/{id}")
    public ResponseEntity<?> updateExperience(HttpSession session,
                                               @PathVariable long id,
                                               @RequestBody WorkExperience experience) {
        UUID userId = getUserId(session);
        log.debug("PUT /api/profile/experience/{} — userId={}", id, userId);
        try {
            profileService.updateWorkExperience(userId, id, experience);
            return noCache(ResponseEntity.ok(experience));
        } catch (RuntimeException e) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage())));
        }
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<Void> deleteExperience(HttpSession session,
                                                  @PathVariable long id) {
        UUID userId = getUserId(session);
        log.debug("DELETE /api/profile/experience/{} — userId={}", id, userId);
        boolean deleted = profileService.deleteWorkExperience(userId, id);
        if (!deleted) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return noCache(ResponseEntity.noContent().build());
    }

    // ========================================================================
    // Education
    // ========================================================================

    @GetMapping("/education")
    public ResponseEntity<List<Education>> getEducations(HttpSession session) {
        UUID userId = getUserId(session);
        log.debug("GET /api/profile/education — userId={}", userId);
        return noCache(ResponseEntity.ok(profileService.getEducations(userId)));
    }

    @PostMapping("/education")
    public ResponseEntity<Education> createEducation(HttpSession session,
                                                      @RequestBody Education education) {
        UUID userId = getUserId(session);
        log.debug("POST /api/profile/education — userId={}", userId);
        Education created = profileService.createEducation(userId, education);
        return noCache(ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @PutMapping("/education/{id}")
    public ResponseEntity<?> updateEducation(HttpSession session,
                                              @PathVariable long id,
                                              @RequestBody Education education) {
        UUID userId = getUserId(session);
        log.debug("PUT /api/profile/education/{} — userId={}", id, userId);
        try {
            profileService.updateEducation(userId, id, education);
            return noCache(ResponseEntity.ok(education));
        } catch (RuntimeException e) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage())));
        }
    }

    @DeleteMapping("/education/{id}")
    public ResponseEntity<Void> deleteEducation(HttpSession session,
                                                 @PathVariable long id) {
        UUID userId = getUserId(session);
        log.debug("DELETE /api/profile/education/{} — userId={}", id, userId);
        boolean deleted = profileService.deleteEducation(userId, id);
        if (!deleted) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return noCache(ResponseEntity.noContent().build());
    }

    // ========================================================================
    // Projects
    // ========================================================================

    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getProjects(HttpSession session) {
        UUID userId = getUserId(session);
        log.debug("GET /api/profile/projects — userId={}", userId);
        return noCache(ResponseEntity.ok(profileService.getProjects(userId)));
    }

    @PostMapping("/projects")
    public ResponseEntity<Project> createProject(HttpSession session,
                                                  @RequestBody Project project) {
        UUID userId = getUserId(session);
        log.debug("POST /api/profile/projects — userId={}", userId);
        Project created = profileService.createProject(userId, project);
        return noCache(ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<?> updateProject(HttpSession session,
                                            @PathVariable long id,
                                            @RequestBody Project project) {
        UUID userId = getUserId(session);
        log.debug("PUT /api/profile/projects/{} — userId={}", id, userId);
        try {
            profileService.updateProject(userId, id, project);
            return noCache(ResponseEntity.ok(project));
        } catch (RuntimeException e) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage())));
        }
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(HttpSession session,
                                               @PathVariable long id) {
        UUID userId = getUserId(session);
        log.debug("DELETE /api/profile/projects/{} — userId={}", id, userId);
        boolean deleted = profileService.deleteProject(userId, id);
        if (!deleted) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return noCache(ResponseEntity.noContent().build());
    }

    // ========================================================================
    // Courses & Certificates
    // ========================================================================

    @GetMapping("/courses")
    public ResponseEntity<CoursePage> getCourses(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "start_date") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        UUID userId = getUserId(session);
        log.debug("GET /api/profile/courses — userId={}, page={}, size={}", userId, page, size);

        // Parse sort field and direction from combined sort parameter
        String sortField = sort;
        String sortDir = order;
        if (sort.contains(",")) {
            String[] parts = sort.split(",");
            sortField = parts[0];
            sortDir = parts.length > 1 ? parts[1] : order;
        }

        CoursePage coursePage = profileService.getCourses(userId, page, size,
                sortField, sortDir, search, dateFrom, dateTo);
        return noCache(ResponseEntity.ok(coursePage));
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseCertificate> createCourse(HttpSession session,
                                                           @RequestBody CourseCertificate course) {
        UUID userId = getUserId(session);
        log.debug("POST /api/profile/courses — userId={}", userId);
        CourseCertificate created = profileService.createCourse(userId, course);
        return noCache(ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(HttpSession session,
                                           @PathVariable long id,
                                           @RequestBody CourseCertificate course) {
        UUID userId = getUserId(session);
        log.debug("PUT /api/profile/courses/{} — userId={}", id, userId);
        try {
            profileService.updateCourse(userId, id, course);
            return noCache(ResponseEntity.ok(course));
        } catch (RuntimeException e) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage())));
        }
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(HttpSession session,
                                              @PathVariable long id) {
        UUID userId = getUserId(session);
        log.debug("DELETE /api/profile/courses/{} — userId={}", id, userId);
        boolean deleted = profileService.deleteCourse(userId, id);
        if (!deleted) {
            return noCache(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return noCache(ResponseEntity.noContent().build());
    }

    // ========================================================================
    // Additional Info
    // ========================================================================

    @GetMapping("/additional")
    public ResponseEntity<?> getAdditionalInfo(HttpSession session) {
        UUID userId = getUserId(session);
        log.debug("GET /api/profile/additional — userId={}", userId);
        Map<String, Object> info = profileService.getAdditionalInfo(userId);
        return noCache(ResponseEntity.ok(info));
    }

    @PutMapping("/additional")
    public ResponseEntity<?> updateAdditionalInfo(HttpSession session,
                                                    @RequestBody Map<String, Object> data) {
        UUID userId = getUserId(session);
        log.debug("PUT /api/profile/additional — userId={}", userId);
        try {
            profileService.updateAdditionalInfo(userId, data);
            Map<String, Object> updated = profileService.getAdditionalInfo(userId);
            return noCache(ResponseEntity.ok(updated));
        } catch (ServiceException e) {
            return noCache(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            log.error("Failed to update additional info: userId={}", userId, e);
            return noCache(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid data: " + e.getMessage())));
        }
    }

    // ========================================================================
    // Work Formats (lookup)
    // ========================================================================

    @GetMapping("/work-formats")
    public ResponseEntity<List<WorkFormat>> getWorkFormats(HttpSession session) {
        UUID userId = getUserId(session);
        log.debug("GET /api/profile/work-formats — userId={}", userId);
        return noCache(ResponseEntity.ok(profileService.getAllWorkFormats()));
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private UUID getUserId(HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute("user");
        if (userSession == null) {
            throw new ServiceException("auth.unauthorized", "Not authenticated");
        }
        return userSession.getUserId();
    }

    /**
     * Applies Cache-Control: no-store, private to the response (SEC-005).
     */
    private <T> ResponseEntity<T> noCache(ResponseEntity.BodyBuilder builder, T body) {
        return builder.cacheControl(CacheControl.noStore().cachePrivate()).body(body);
    }

    private <T> ResponseEntity<T> noCache(ResponseEntity<T> response) {
        return ResponseEntity.status(response.getStatusCode())
                .cacheControl(CacheControl.noStore().cachePrivate())
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}
