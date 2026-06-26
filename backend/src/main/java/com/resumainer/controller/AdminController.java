package com.resumainer.controller;

import com.resumainer.dto.admin.AdminDashboardDto;
import com.resumainer.dto.admin.AdminSavedResumeDto;
import com.resumainer.dto.admin.AdminUserListItemDto;
import com.resumainer.model.PagedResponse;
import com.resumainer.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin controller for all /api/admin/** endpoints.
 * <p>
 * Every endpoint is protected by AuthInterceptor ADMIN role check.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> getDashboard() {
        AdminDashboardDto dashboard = adminService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/resumes")
    public ResponseEntity<PagedResponse<AdminSavedResumeDto>> getResumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String adaptationLevel,
            @RequestParam(required = false) String createdFrom,
            @RequestParam(required = false) String createdTo,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        // Parse sort parameter: "field,direction" or just "field"
        String sortField = "createdAt";
        String sortDir = "desc";
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            sortField = parts[0].trim();
            if (parts.length > 1) {
                sortDir = parts[1].trim();
            }
        }

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                search, language, adaptationLevel, createdFrom, createdTo,
                sortField, sortDir, page, size);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/users")
    public ResponseEntity<PagedResponse<AdminUserListItemDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String permission,
            @RequestParam(required = false) String rights,
            @RequestParam(required = false) String createdFrom,
            @RequestParam(required = false) String createdTo,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        // Parse sort parameter: "field,direction" or just "field"
        String sortField = "createdAt";
        String sortDir = "desc";
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            sortField = parts[0].trim();
            if (parts.length > 1) {
                sortDir = parts[1].trim();
            }
        }

        PagedResponse<AdminUserListItemDto> result = adminService.getUsers(
                search, role, status, permission, rights, createdFrom, createdTo,
                sortField, sortDir, page, size);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<Map<String, String>> deleteResume(@PathVariable long id) {
        log.debug("deleteResume: resumeId={}", id);

        try {
            boolean deleted = adminService.deleteResume(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Resume deleted"));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Failed to delete resume."));
        } catch (Exception e) {
            log.error("Error deleting resume {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete resume."));
        }
    }
}
