package com.resumainer.controller;

import com.resumainer.dto.UserSession;
import com.resumainer.dto.admin.AdminDashboardDto;
import com.resumainer.dto.admin.AdminSavedResumeDto;
import com.resumainer.dto.admin.AdminUserAccessUpdateRequest;
import com.resumainer.dto.admin.AdminUserDetailsDto;
import com.resumainer.dto.admin.AdminUserListItemDto;
import com.resumainer.model.PagedResponse;
import com.resumainer.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Map;
import java.util.UUID;

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

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserDetails(
            @PathVariable UUID userId,
            @SessionAttribute(value = "user", required = false) UserSession currentAdmin) {

        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("getUserDetails: targetUserId={}, currentAdminId={}",
                userId, currentAdmin.getUserId());

        try {
            AdminUserDetailsDto details = adminService.getUserDetails(userId, currentAdmin.getUserId());
            if (details == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found."));
            }
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (com.resumainer.exception.ServiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching user details for {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to load user details."));
        }
    }

    @GetMapping("/users/{userId}/resumes")
    public ResponseEntity<?> getUserResumes(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String adaptationLevel,
            @RequestParam(required = false) String createdFrom,
            @RequestParam(required = false) String createdTo,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @SessionAttribute(value = "user", required = false) UserSession currentAdmin) {

        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("getUserResumes: targetUserId={}, currentAdminId={}",
                userId, currentAdmin.getUserId());

        try {
            AdminUserDetailsDto details = adminService.getUserDetails(userId, currentAdmin.getUserId());
            if (details == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found."));
            }

            String sortField = "createdAt";
            String sortDir = "desc";
            if (sort != null && !sort.isBlank()) {
                String[] parts = sort.split(",");
                sortField = parts[0].trim();
                if (parts.length > 1) {
                    sortDir = parts[1].trim();
                }
            }

            PagedResponse<AdminSavedResumeDto> result = adminService.getResumesByUserId(
                    userId, search, language, adaptationLevel, createdFrom, createdTo,
                    sortField, sortDir, page, size);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching resumes for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to load user resumes."));
        }
    }

    @PatchMapping("/users/{userId}/access")
    public ResponseEntity<?> updateUserAccess(
            @PathVariable UUID userId,
            @RequestBody AdminUserAccessUpdateRequest request,
            @SessionAttribute(value = "user", required = false) UserSession currentAdmin) {

        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("updateUserAccess: targetUserId={}, currentAdminId={}",
                userId, currentAdmin.getUserId());

        try {
            AdminUserDetailsDto details = adminService.updateUserAccess(
                    userId, currentAdmin.getUserId(), request);
            if (details == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found."));
            }
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            // Self-protection violation
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (com.resumainer.exception.ServiceException e) {
            // Invalid lookup code
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating user access for {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to update user access."));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable UUID userId,
            @SessionAttribute(value = "user", required = false) UserSession currentAdmin) {

        if (currentAdmin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("deleteUser: targetUserId={}, currentAdminId={}",
                userId, currentAdmin.getUserId());

        try {
            boolean deleted = adminService.deleteUser(userId, currentAdmin.getUserId());
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "User deleted"));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Failed to delete user."));
        } catch (IllegalArgumentException e) {
            // Self-delete attempt
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete user."));
        }
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
