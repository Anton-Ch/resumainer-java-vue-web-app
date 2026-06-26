package com.resumainer.service;

import com.resumainer.dao.AdminDao;
import com.resumainer.dao.AdminDao.AdminSavedResumeRow;
import com.resumainer.dao.AdminDao.AdminUserRow;
import com.resumainer.dto.admin.AdminSavedResumeDto;
import com.resumainer.dto.admin.AdminUserListItemDto;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AdminService resume listing and dashboard methods.
 */
class AdminServiceResumeTest {

    private AdminDao adminDao;
    private AdminService adminService;

    private AdminSavedResumeRow createRow(long id, String username, String email,
                                          String fullName, String resumeTitle,
                                          String languageCode, String languageName,
                                          String adaptationLevel, String pdfStatus,
                                          boolean pdfPresent, boolean htmlPresent) {
        AdminSavedResumeRow row = new AdminSavedResumeRow();
        row.id = id;
        row.userId = UUID.randomUUID();
        row.username = username;
        row.email = email;
        row.fullName = fullName;
        row.resumeTitle = resumeTitle;
        row.vacancyTitle = "Vacancy";
        row.companyName = "Company";
        row.languageCode = languageCode;
        row.languageName = languageName;
        row.adaptationLevel = adaptationLevel;
        row.createdAt = "2026-06-25";
        row.publicCode = "CODE" + id;
        row.publicUrlLink = "https://example.com/" + username + "/CODE" + id;
        row.coverLetter = "Cover letter";
        row.pdfStatus = pdfStatus;
        row.pdfFilePresent = pdfPresent;
        row.htmlFilePresent = htmlPresent;
        return row;
    }

    @BeforeEach
    void setUp() {
        adminDao = mock(AdminDao.class);
        adminService = new AdminService(adminDao);
    }

    @Test
    void getResumes_returnsPagedResponseWithItems() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "READY", true, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void getResumes_dtoHasCorrectOwnerFields() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "READY", true, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminSavedResumeDto dto = result.getItems().get(0);
        assertEquals("anton@example.com", dto.getOwnerEmail());
        assertEquals("Anton Ch.", dto.getOwnerFullName());
        assertEquals("anton", dto.getOwnerUsername());
        assertNotNull(dto.getOwnerUserId());
    }

    @Test
    void getResumes_dtoHasCorrectResumeFields() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "READY", true, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminSavedResumeDto dto = result.getItems().get(0);
        assertEquals("Java Dev", dto.getResumeTitle());
        assertEquals("RU", dto.getLanguageCode());
        assertEquals("Russian", dto.getLanguageName());
        assertEquals("BALANCED", dto.getAdaptationLevel());
        assertNotNull(dto.getCreatedAt());
    }

    @Test
    void getResumes_dtoHasSafeUrlsAndNoRawPaths() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "READY", true, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminSavedResumeDto dto = result.getItems().get(0);

        // Must have safe canonical URLs, not raw file paths
        assertNotNull(dto.getPdfOpenUrl());
        assertTrue(dto.getPdfOpenUrl().contains("/api/generate/resumes/"));
        assertTrue(dto.getPdfOpenUrl().contains("disposition=inline"));
        assertTrue(dto.getPdfDownloadUrl().contains("/api/generate/resumes/"));
        assertTrue(dto.getHtmlDownloadUrl().contains("/api/generate/resumes/"));

        // Must not expose raw paths
        assertNull(getFieldIfExists(dto, "pdfFilePath"));
        assertNull(getFieldIfExists(dto, "htmlFilePath"));
    }

    @Test
    void getResumes_pdfNotAvailableWhenStatusNotReady() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "PENDING", true, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertFalse(result.getItems().get(0).isPdfAvailable());
    }

    @Test
    void getResumes_pdfNotAvailableWhenFileMissing() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "READY", false, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertFalse(result.getItems().get(0).isPdfAvailable());
    }

    @Test
    void getResumes_htmlDownloadUrlNullWhenFileMissing() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "READY", true, false);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertNull(result.getItems().get(0).getHtmlDownloadUrl());
    }

    @Test
    void getResumes_passesSearchFilterToDao() {
        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(0L);

        adminService.getResumes("developer", "EN", "BALANCED",
                "2026-06-01", "2026-06-25",
                "createdAt", "desc", 0, 10);

        verify(adminDao).findResumes(eq("developer"), eq("EN"), eq("BALANCED"),
                eq("2026-06-01"), eq("2026-06-25"),
                eq("createdAt"), eq("desc"), eq(0), eq(10));
        verify(adminDao).countResumes(eq("developer"), eq("EN"), eq("BALANCED"),
                eq("2026-06-01"), eq("2026-06-25"));
    }

    @Test
    void getResumes_emptyResult() {
        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(0L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getResumes_coverLetterIsPreserved() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "READY", true, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertEquals("Cover letter", result.getItems().get(0).getCoverLetter());
    }

    // --- pdfMessage tests (Fix 2) ---

    @Test
    void getResumes_pdfMessageIsNull_whenPdfAvailable() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "READY", true, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminSavedResumeDto dto = result.getItems().get(0);
        assertTrue(dto.isPdfAvailable());
        assertNull(dto.getPdfMessage());
    }

    @Test
    void getResumes_pdfMessageIsNotNull_whenPdfNotAvailable() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "PENDING", true, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminSavedResumeDto dto = result.getItems().get(0);
        assertFalse(dto.isPdfAvailable());
        assertNotNull(dto.getPdfMessage());
    }

    @Test
    void getResumes_pdfMessageIsNotNull_whenPdfFileMissing() {
        AdminSavedResumeRow row = createRow(1L, "anton", "anton@example.com",
                "Anton Ch.", "Java Dev", "RU", "Russian",
                "BALANCED", "READY", false, true);

        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminSavedResumeDto> result = adminService.getResumes(
                null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminSavedResumeDto dto = result.getItems().get(0);
        assertFalse(dto.isPdfAvailable());
        assertNotNull(dto.getPdfMessage());
    }

    // --- Date validation tests (Fix 1) ---

    @Test
    void getResumes_invalidCreatedFrom_throwsServiceException() {
        assertThrows(ServiceException.class, () ->
                adminService.getResumes(null, null, null, "not-a-date", null,
                        "createdAt", "desc", 0, 10)
        );
    }

    @Test
    void getResumes_invalidCreatedTo_throwsServiceException() {
        assertThrows(ServiceException.class, () ->
                adminService.getResumes(null, null, null, null, "bad-date",
                        "createdAt", "desc", 0, 10)
        );
    }

    @Test
    void getResumes_validDates_doNotThrow() {
        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(adminDao.countResumes(any(), any(), any(), any(), any()))
                .thenReturn(0L);

        assertDoesNotThrow(() ->
                adminService.getResumes(null, null, null,
                        "2026-06-01", "2026-06-25",
                        "createdAt", "desc", 0, 10)
        );
    }

    // --- Sort validation: propagated to DAO, caught by GlobalExceptionHandler ---

    @Test
    void getResumes_invalidSortField_propagatesFromDao() {
        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid sort field: badField"));

        assertThrows(IllegalArgumentException.class, () ->
                adminService.getResumes(null, null, null, null, null,
                        "badField", "desc", 0, 10)
        );
    }

    @Test
    void getResumes_invalidSortDir_propagatesFromDao() {
        when(adminDao.findResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid sort direction: badDir"));

        assertThrows(IllegalArgumentException.class, () ->
                adminService.getResumes(null, null, null, null, null,
                        "createdAt", "badDir", 0, 10)
        );
    }

    // --- Phase 3: Admin resume delete tests ---

    @Test
    void deleteResume_returnsTrue_whenDaoSucceeds() {
        when(adminDao.adminSoftDeleteResume(101L)).thenReturn(true);

        boolean result = adminService.deleteResume(101L);

        assertTrue(result);
        verify(adminDao).adminSoftDeleteResume(101L);
    }

    @Test
    void deleteResume_returnsFalse_whenDaoReturnsFalse() {
        when(adminDao.adminSoftDeleteResume(999L)).thenReturn(false);

        boolean result = adminService.deleteResume(999L);

        assertFalse(result);
        verify(adminDao).adminSoftDeleteResume(999L);
    }

    @Test
    void deleteResume_doesNotReturnInternalDetails() {
        when(adminDao.adminSoftDeleteResume(101L)).thenReturn(true);

        boolean result = adminService.deleteResume(101L);

        // Service returns only boolean, no path/owner/internal details
        assertEquals(true, result);
    }

    // --- Phase 4: Admin users listing tests ---

    private AdminUserRow createUserRow(UUID id, String username, String email,
                                        String fullName, String roleCode, String roleName,
                                        String statusCode, String statusName,
                                        String permissionCode, String permissionName,
                                        boolean isPrivileged, long resumesCount) {
        AdminUserRow row = new AdminUserRow();
        row.id = id;
        row.username = username;
        row.email = email;
        row.fullName = fullName;
        row.roleCode = roleCode;
        row.roleName = roleName;
        row.statusCode = statusCode;
        row.statusName = statusName;
        row.permissionCode = permissionCode;
        row.permissionName = permissionName;
        row.isPrivileged = isPrivileged;
        row.resumesCount = resumesCount;
        row.createdAt = LocalDateTime.of(2026, 6, 1, 12, 0);
        return row;
    }

    @Test
    void getUsers_returnsPagedResponseWithItems() {
        AdminUserRow row = createUserRow(UUID.randomUUID(), "johndoe", "john@example.com",
                "John Doe", "USER", "Regular User", "ACTIVE", "Active",
                "ALLOWED", "Allowed", false, 3);

        when(adminDao.findUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countUsers(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminUserListItemDto> result = adminService.getUsers(
                null, null, null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
    }

    @Test
    void getUsers_dtoHasCorrectIdentityFields() {
        UUID userId = UUID.randomUUID();
        AdminUserRow row = createUserRow(userId, "johndoe", "john@example.com",
                "John Doe", "USER", "Regular User", "ACTIVE", "Active",
                "ALLOWED", "Allowed", false, 3);

        when(adminDao.findUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countUsers(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminUserListItemDto> result = adminService.getUsers(
                null, null, null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminUserListItemDto dto = result.getItems().get(0);
        assertEquals(userId.toString(), dto.getId());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("johndoe", dto.getUsername());
    }

    @Test
    void getUsers_dtoHasReadableStatusAndRole() {
        AdminUserRow row = createUserRow(UUID.randomUUID(), "johndoe", "john@example.com",
                "John Doe", "USER", "Regular User", "ACTIVE", "Active",
                "ALLOWED", "Allowed", false, 3);

        when(adminDao.findUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countUsers(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminUserListItemDto> result = adminService.getUsers(
                null, null, null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminUserListItemDto dto = result.getItems().get(0);
        assertEquals("USER", dto.getRoleCode());
        assertEquals("Regular User", dto.getRoleName());
        assertEquals("ACTIVE", dto.getStatusCode());
        assertEquals("Active", dto.getStatusName());
        assertEquals("ALLOWED", dto.getPermissionCode());
        assertEquals("Allowed", dto.getPermissionName());
        assertFalse(dto.isPrivileged());
    }

    @Test
    void getUsers_dtoHasResumesCountAndCreatedAt() {
        AdminUserRow row = createUserRow(UUID.randomUUID(), "johndoe", "john@example.com",
                "John Doe", "USER", "Regular User", "ACTIVE", "Active",
                "ALLOWED", "Allowed", false, 5);

        when(adminDao.findUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countUsers(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminUserListItemDto> result = adminService.getUsers(
                null, null, null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminUserListItemDto dto = result.getItems().get(0);
        assertEquals(5L, dto.getResumesCount());
        assertNotNull(dto.getCreatedAt());
    }

    @Test
    void getUsers_emptyResult() {
        when(adminDao.findUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(adminDao.countUsers(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0L);

        PagedResponse<AdminUserListItemDto> result = adminService.getUsers(
                null, null, null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getUsers_passesSearchAndFiltersToDao() {
        when(adminDao.findUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(adminDao.countUsers(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0L);

        adminService.getUsers("john", "ADMIN", "ACTIVE", "ALLOWED", "PRIVILEGED",
                "2026-06-01", "2026-06-25",
                "createdAt", "desc", 0, 10);

        verify(adminDao).findUsers(eq("john"), eq("ADMIN"), eq("ACTIVE"),
                eq("ALLOWED"), eq("PRIVILEGED"),
                eq("2026-06-01"), eq("2026-06-25"),
                eq("createdAt"), eq("desc"), eq(0), eq(10));
        verify(adminDao).countUsers(eq("john"), eq("ADMIN"), eq("ACTIVE"),
                eq("ALLOWED"), eq("PRIVILEGED"),
                eq("2026-06-01"), eq("2026-06-25"));
    }

    @Test
    void getUsers_invalidDate_throwsServiceException() {
        assertThrows(ServiceException.class, () ->
                adminService.getUsers(null, null, null, null, null, "bad-date", null,
                        "createdAt", "desc", 0, 10)
        );
    }

    @Test
    void getUsers_dtoHasNoSensitiveFields() {
        AdminUserRow row = createUserRow(UUID.randomUUID(), "johndoe", "john@example.com",
                "John Doe", "USER", "Regular User", "ACTIVE", "Active",
                "ALLOWED", "Allowed", false, 3);

        when(adminDao.findUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(adminDao.countUsers(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PagedResponse<AdminUserListItemDto> result = adminService.getUsers(
                null, null, null, null, null, null, null,
                "createdAt", "desc", 0, 10);

        AdminUserListItemDto dto = result.getItems().get(0);

        // No password hash or sensitive fields via reflection
        assertNull(getFieldIfExists(dto, "passwordHash"));
        assertNull(getFieldIfExists(dto, "password_hash"));
    }

    // Helper: reflection check that DTO doesn't have a field
    private Object getFieldIfExists(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            return null; // Field doesn't exist — good!
        } catch (Exception e) {
            return null;
        }
    }
}
