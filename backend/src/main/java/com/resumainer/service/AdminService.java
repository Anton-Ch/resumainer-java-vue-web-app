package com.resumainer.service;

import com.resumainer.dao.AdminDao;
import com.resumainer.dao.AdminDao.AdminSavedResumeRow;
import com.resumainer.dto.admin.AdminDashboardDto;
import com.resumainer.dto.admin.AdminSavedResumeDto;
import com.resumainer.dto.admin.AdminUserListItemDto;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic for admin operations.
 * <p>
 * Provides dashboard stats, cross-user resume listing, user management,
 * and mutation operations with self-protection rules.
 */
@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private static final String PDF_ENDPOINT = "/api/generate/resumes/";
    private static final String PDF_OPEN_SUFFIX = "/pdf?disposition=inline";
    private static final String PDF_DOWNLOAD_SUFFIX = "/pdf";
    private static final String HTML_DOWNLOAD_SUFFIX = "/html";

    private final AdminDao adminDao;

    public AdminService(AdminDao adminDao) {
        this.adminDao = adminDao;
    }

    /**
     * Returns dashboard summary with real user/resume counts and WIP token stats.
     */
    public AdminDashboardDto getDashboard() {
        long totalUsers = adminDao.countNonDeletedUsers();
        long totalResumes = adminDao.countNonDeletedResumes();
        return new AdminDashboardDto(totalUsers, totalResumes);
    }

    /**
     * Paginated admin resume listing with search, filters, date range, and sort.
     *
     * @throws ServiceException if date format is invalid
     * @throws IllegalArgumentException if sort field or direction is invalid (propagated from DAO)
     */
    public PagedResponse<AdminSavedResumeDto> getResumes(String search,
                                                           String language,
                                                           String adaptationLevel,
                                                           String createdFrom,
                                                           String createdTo,
                                                           String sortField,
                                                           String sortDir,
                                                           int page, int size) {
        // Validate date formats before DAO SQL execution
        validateDateParam("createdFrom", createdFrom);
        validateDateParam("createdTo", createdTo);

        List<AdminSavedResumeRow> rows = adminDao.findResumes(
                search, language, adaptationLevel, createdFrom, createdTo,
                sortField, sortDir, page, size);

        long totalElements = adminDao.countResumes(
                search, language, adaptationLevel, createdFrom, createdTo);

        List<AdminSavedResumeDto> items = rows.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(items, page, size, totalElements);
    }

    /**
     * Paginated resume listing for a specific user (admin view).
     */
    public PagedResponse<AdminSavedResumeDto> getResumesByUserId(UUID userId,
                                                                  String search,
                                                                  String language,
                                                                  String adaptationLevel,
                                                                  String createdFrom,
                                                                  String createdTo,
                                                                  String sortField,
                                                                  String sortDir,
                                                                  int page, int size) {
        List<AdminSavedResumeRow> rows = adminDao.findResumesByUserId(
                userId, search, language, adaptationLevel, createdFrom, createdTo,
                sortField, sortDir, page, size);

        long totalElements = adminDao.countResumesByUserId(
                userId, search, language, adaptationLevel, createdFrom, createdTo);

        List<AdminSavedResumeDto> items = rows.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(items, page, size, totalElements);
    }

    // --- Phase 4: Admin users listing ---

    /**
     * Paginated admin users listing with search, filters, date range, and sort.
     */
    public PagedResponse<AdminUserListItemDto> getUsers(String search,
                                                         String role, String status,
                                                         String permission, String rights,
                                                         String createdFrom, String createdTo,
                                                         String sortField, String sortDir,
                                                         int page, int size) {
        // Validate date formats before DAO SQL execution
        validateDateParam("createdFrom", createdFrom);
        validateDateParam("createdTo", createdTo);

        List<AdminDao.AdminUserRow> rows = adminDao.findUsers(
                search, role, status, permission, rights, createdFrom, createdTo,
                sortField, sortDir, page, size);

        long totalElements = adminDao.countUsers(
                search, role, status, permission, rights, createdFrom, createdTo);

        List<AdminUserListItemDto> items = rows.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(items, page, size, totalElements);
    }

    // --- Phase 3: Admin resume delete ---

    /**
     * Admin-scoped soft-delete for any saved resume.
     *
     * @param resumeId the resume ID to delete
     * @return true if deleted, false if not found or already deleted
     */
    public boolean deleteResume(long resumeId) {
        log.debug("deleteResume: resumeId={}", resumeId);
        return adminDao.adminSoftDeleteResume(resumeId);
    }

    // --- Mapping ---

    private AdminSavedResumeDto toDto(AdminSavedResumeRow row) {
        AdminSavedResumeDto dto = new AdminSavedResumeDto();

        dto.setId(row.id);
        dto.setOwnerUserId(row.userId != null ? row.userId.toString() : null);
        dto.setOwnerUsername(row.username);
        dto.setOwnerEmail(row.email);
        dto.setOwnerFullName(row.fullName);
        dto.setResumeTitle(row.resumeTitle);
        dto.setVacancyTitle(row.vacancyTitle);
        dto.setCompanyName(row.companyName);
        dto.setLanguageCode(row.languageCode);
        dto.setLanguageName(row.languageName);
        dto.setAdaptationLevel(row.adaptationLevel);
        dto.setCreatedAt(row.createdAt);

        // Public URL (already stored as full URL in saved_resumes.public_url_link)
        dto.setPublicUrlLink(row.publicUrlLink);

        // Canonical authenticated export endpoints (relative paths)
        long id = row.id;
        dto.setPdfOpenUrl(PDF_ENDPOINT + id + PDF_OPEN_SUFFIX);
        dto.setPdfDownloadUrl(PDF_ENDPOINT + id + PDF_DOWNLOAD_SUFFIX);
        dto.setHtmlDownloadUrl(PDF_ENDPOINT + id + HTML_DOWNLOAD_SUFFIX);

        // PDF availability: status READY AND PDF file physically present
        boolean pdfReady = "READY".equals(row.pdfStatus) && row.pdfFilePresent;
        dto.setPdfAvailable(pdfReady);
        dto.setPdfStatus(row.pdfStatus);
        // pdfMessage: null when available, descriptive when not available
        if (pdfReady) {
            dto.setPdfMessage(null);
        } else {
            dto.setPdfMessage("PDF is being generated. Please try again later.");
        }

        // HTML download URL: null when HTML file is not present
        if (!row.htmlFilePresent) {
            dto.setHtmlDownloadUrl(null);
        }

        dto.setCoverLetter(row.coverLetter);

        return dto;
    }

    private AdminUserListItemDto toUserDto(AdminDao.AdminUserRow row) {
        AdminUserListItemDto dto = new AdminUserListItemDto();
        dto.setId(row.id != null ? row.id.toString() : null);
        dto.setFullName(row.fullName);
        dto.setUsername(row.username);
        dto.setEmail(row.email);
        dto.setRoleCode(row.roleCode);
        dto.setRoleName(row.roleName);
        dto.setStatusCode(row.statusCode);
        dto.setStatusName(row.statusName);
        dto.setPermissionCode(row.permissionCode);
        dto.setPermissionName(row.permissionName);
        dto.setPrivileged(row.isPrivileged);
        dto.setResumesCount(row.resumesCount);
        dto.setCreatedAt(row.createdAt != null ? row.createdAt.toString() : null);
        return dto;
    }

    /**
     * Validates a date parameter is parseable as ISO date (yyyy-MM-dd) if non-blank.
     *
     * @throws ServiceException if the value is non-blank but not a valid date
     */
    private void validateDateParam(String paramName, String value) {
        if (value == null || value.isBlank()) return;
        try {
            LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            log.warn("Invalid {} parameter: {}", paramName, value);
            throw new ServiceException("INVALID_DATE",
                    "Invalid date format for " + paramName + ". Expected yyyy-MM-dd.");
        }
    }
}
