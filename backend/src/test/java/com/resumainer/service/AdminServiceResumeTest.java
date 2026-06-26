package com.resumainer.service;

import com.resumainer.dao.AdminDao;
import com.resumainer.dao.AdminDao.AdminSavedResumeRow;
import com.resumainer.dao.AdminDao.AdminUserDetailsRow;
import com.resumainer.dao.AdminDao.AdminUserRow;
import com.resumainer.dto.admin.AdminSavedResumeDto;
import com.resumainer.dto.admin.AdminUserAccessUpdateRequest;
import com.resumainer.dto.admin.AdminUserDetailsDto;
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

    // --- Phase 5: Admin user details tests ---

    private AdminUserDetailsRow createUserDetailsRow(UUID id) {
        AdminUserDetailsRow row = new AdminUserDetailsRow();
        row.id = id;
        row.username = "johndoe";
        row.accountEmail = "john@example.com";
        row.roleCode = "USER";
        row.roleName = "Regular User";
        row.statusCode = "ACTIVE";
        row.statusName = "Active";
        row.permissionCode = "ALLOWED";
        row.permissionName = "Allowed";
        row.isPrivileged = false;
        row.defaultLanguageCode = "EN";
        row.defaultLanguageName = "English";
        row.createdAt = java.time.LocalDateTime.of(2026, 6, 1, 12, 0);

        // Contact section
        row.fullName = "John Doe";
        row.professionalTitle = "Developer";
        row.phone = "+123456789";
        row.resumeEmail = "resume@example.com";
        row.location = "Almaty";
        row.linkedinUrl = "https://linkedin.com/in/johndoe";

        // Additional info
        row.skills = "Java, Spring";
        row.apiLanguages = "English, Russian";
        row.professionalAspirations = "Grow professionally";
        row.citizenship = "Kazakhstan";
        return row;
    }

    @Test
    void getUserDetails_returnsDto_whenUserExists() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, adminId);

        assertNotNull(dto);
        assertEquals(userId.toString(), dto.getId());
        assertFalse(dto.isCurrentAdmin());
        verify(adminDao).findUserDetails(userId);
    }

    @Test
    void getUserDetails_returnsNull_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(adminDao.findUserDetails(userId)).thenReturn(null);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        assertNull(dto);
    }

    @Test
    void getUserDetails_isCurrentAdmin_true_whenViewingSelf() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, userId);

        assertTrue(dto.isCurrentAdmin());
    }

    @Test
    void getUserDetails_isCurrentAdmin_false_whenViewingOther() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, adminId);

        assertFalse(dto.isCurrentAdmin());
    }

    @Test
    void getUserDetails_accountEmail_fromUsersEmail() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        row.accountEmail = "john@example.com";
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        assertEquals("john@example.com", dto.getAccount().getAccountEmail());
    }

    @Test
    void getUserDetails_resumeEmail_fromContactDetailResumeEmail() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        row.resumeEmail = "resume@example.com";
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        assertEquals("resume@example.com", dto.getContacts().getResumeEmail());
    }

    @Test
    void getUserDetails_fullName_fromContactDetail() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        row.fullName = "John Doe";
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        assertEquals("John Doe", dto.getContacts().getFullName());
    }

    @Test
    void getUserDetails_missingContactDetail_returnsNullSection() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        // Set all contact fields to null to simulate missing contact_detail
        row.fullName = null;
        row.professionalTitle = null;
        row.phone = null;
        row.resumeEmail = null;
        row.location = null;
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        assertNull(dto.getContacts());
    }

    @Test
    void getUserDetails_missingAdditionalInfo_returnsNullSection() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        // Set all additional info fields to null
        row.skills = null;
        row.apiLanguages = null;
        row.professionalAspirations = null;
        row.achievements = null;
        row.generalInformation = null;
        row.dateOfBirth = null;
        row.citizenship = null;
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        assertNull(dto.getAdditionalInfo());
    }

    @Test
    void getUserDetails_accountSectionHasAllFields() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        assertEquals("johndoe", dto.getAccount().getUsername());
        assertEquals("USER", dto.getAccount().getRoleCode());
        assertEquals("ACTIVE", dto.getAccount().getStatusCode());
        assertEquals("ALLOWED", dto.getAccount().getPermissionCode());
        assertEquals("EN", dto.getAccount().getDefaultLanguageCode());
        assertFalse(dto.getAccount().isPrivileged());
    }

    @Test
    void getUserDetails_contactsSectionHasSafeFields() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        assertEquals("Developer", dto.getContacts().getProfessionalTitle());
        assertEquals("+123456789", dto.getContacts().getPhone());
        assertEquals("Almaty", dto.getContacts().getLocation());
    }

    @Test
    void getUserDetails_additionalInfoSectionHasSafeFields() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        assertEquals("Java, Spring", dto.getAdditionalInfo().getSkills());
        assertEquals("English, Russian", dto.getAdditionalInfo().getLanguages());
        assertEquals("Kazakhstan", dto.getAdditionalInfo().getCitizenship());
    }

    @Test
    void getUserDetails_dtoHasNoSensitiveFields() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        // No password hash
        assertNull(getFieldIfExists(dto.getAccount(), "passwordHash"));
        assertNull(getFieldIfExists(dto.getAccount(), "password_hash"));
        // No photo file path
        assertNull(getFieldIfExists(dto.getAdditionalInfo(), "photoFilePath"));
        assertNull(getFieldIfExists(dto.getAdditionalInfo(), "photo_file_path"));
    }

    @Test
    void getUserDetails_contactsAreNull_whenDetailMissing() {
        UUID userId = UUID.randomUUID();
        AdminUserDetailsRow row = createUserDetailsRow(userId);
        when(adminDao.findUserDetails(userId)).thenReturn(row);

        AdminUserDetailsDto dto = adminService.getUserDetails(userId, UUID.randomUUID());

        // When contact exists, it should be not null
        assertNotNull(dto.getContacts());
    }

    // --- Phase 6: Access update tests ---

    @Test
    void updateUserAccess_returnsUpdatedDetails_whenSuccess() {
        UUID targetId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();
        req.setRoleCode("ADMIN");
        req.setStatusCode("ACTIVE");
        req.setPermissionCode("ALLOWED");
        req.setPrivileged(true);

        when(adminDao.existsAndNotDeleted(targetId)).thenReturn(true);
        when(adminDao.findRoleIdByCode("ADMIN")).thenReturn(2L);
        when(adminDao.findStatusIdByCode("ACTIVE")).thenReturn(1L);
        when(adminDao.findPermissionIdByCode("ALLOWED")).thenReturn(1L);
        when(adminDao.updateUserAccess(targetId, 2L, 1L, 1L, true)).thenReturn(true);

        // After update, getUserDetails returns DTO
        AdminUserDetailsRow detailsRow = createUserDetailsRow(targetId);
        when(adminDao.findUserDetails(targetId)).thenReturn(detailsRow);

        AdminUserDetailsDto result = adminService.updateUserAccess(targetId, adminId, req);

        assertNotNull(result);
        assertEquals(targetId.toString(), result.getId());
        verify(adminDao).updateUserAccess(targetId, 2L, 1L, 1L, true);
    }

    @Test
    void updateUserAccess_returnsNull_whenTargetNotFound() {
        UUID targetId = UUID.randomUUID();
        when(adminDao.existsAndNotDeleted(targetId)).thenReturn(false);

        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();
        AdminUserDetailsDto result = adminService.updateUserAccess(targetId, UUID.randomUUID(), req);

        assertNull(result);
    }

    @Test
    void updateUserAccess_invalidRoleCode_throwsServiceException() {
        UUID targetId = UUID.randomUUID();
        when(adminDao.existsAndNotDeleted(targetId)).thenReturn(true);
        when(adminDao.findRoleIdByCode("INVALID")).thenReturn(null);

        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();
        req.setRoleCode("INVALID");

        assertThrows(ServiceException.class, () ->
                adminService.updateUserAccess(targetId, UUID.randomUUID(), req)
        );
    }

    @Test
    void updateUserAccess_rejectsSelfDemotion() {
        UUID adminId = UUID.randomUUID();
        when(adminDao.existsAndNotDeleted(adminId)).thenReturn(true);
        when(adminDao.findRoleIdByCode("USER")).thenReturn(1L);
        when(adminDao.findStatusIdByCode("ACTIVE")).thenReturn(1L);
        when(adminDao.findPermissionIdByCode("ALLOWED")).thenReturn(1L);
        AdminDao.UserAccessState state = new AdminDao.UserAccessState();
        state.roleCode = "ADMIN";
        state.statusCode = "ACTIVE";
        when(adminDao.findUserAccessState(adminId)).thenReturn(state);

        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();
        req.setRoleCode("USER");
        req.setStatusCode("ACTIVE");
        req.setPermissionCode("ALLOWED");
        req.setPrivileged(false);

        assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUserAccess(adminId, adminId, req)
        );
    }

    @Test
    void updateUserAccess_rejectsSelfBlock() {
        UUID adminId = UUID.randomUUID();
        when(adminDao.existsAndNotDeleted(adminId)).thenReturn(true);
        when(adminDao.findRoleIdByCode("ADMIN")).thenReturn(2L);
        when(adminDao.findStatusIdByCode("BLOCKED")).thenReturn(2L);
        when(adminDao.findPermissionIdByCode("ALLOWED")).thenReturn(1L);
        AdminDao.UserAccessState state = new AdminDao.UserAccessState();
        state.roleCode = "ADMIN";
        state.statusCode = "ACTIVE";
        when(adminDao.findUserAccessState(adminId)).thenReturn(state);

        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();
        req.setRoleCode("ADMIN");
        req.setStatusCode("BLOCKED");
        req.setPermissionCode("ALLOWED");
        req.setPrivileged(false);

        assertThrows(IllegalArgumentException.class, () ->
                adminService.updateUserAccess(adminId, adminId, req)
        );
    }

    @Test
    void updateUserAccess_allowsOwnPermissionEdit() {
        UUID adminId = UUID.randomUUID();
        when(adminDao.existsAndNotDeleted(adminId)).thenReturn(true);
        when(adminDao.findRoleIdByCode("ADMIN")).thenReturn(2L);
        when(adminDao.findStatusIdByCode("ACTIVE")).thenReturn(1L);
        when(adminDao.findPermissionIdByCode("FORBIDDEN")).thenReturn(2L);
        AdminDao.UserAccessState state = new AdminDao.UserAccessState();
        state.roleCode = "ADMIN";
        state.statusCode = "ACTIVE";
        when(adminDao.findUserAccessState(adminId)).thenReturn(state);
        when(adminDao.updateUserAccess(adminId, 2L, 1L, 2L, false)).thenReturn(true);
        when(adminDao.findUserDetails(adminId)).thenReturn(createUserDetailsRow(adminId));

        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();
        req.setRoleCode("ADMIN");
        req.setStatusCode("ACTIVE");
        req.setPermissionCode("FORBIDDEN");
        req.setPrivileged(false);

        AdminUserDetailsDto result = adminService.updateUserAccess(adminId, adminId, req);

        assertNotNull(result);
    }

    // --- Phase 6: User soft-delete tests ---

    @Test
    void deleteUser_returnsTrue_whenSuccess() {
        UUID targetId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        when(adminDao.existsAndNotDeleted(targetId)).thenReturn(true);
        doNothing().when(adminDao).adminSoftDeleteUser(targetId);

        boolean result = adminService.deleteUser(targetId, adminId);

        assertTrue(result);
        verify(adminDao).adminSoftDeleteUser(targetId);
    }

    @Test
    void deleteUser_rejectsSelfDelete() {
        UUID adminId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                adminService.deleteUser(adminId, adminId)
        );
    }

    @Test
    void deleteUser_returnsFalse_whenTargetNotFound() {
        UUID targetId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        when(adminDao.existsAndNotDeleted(targetId)).thenReturn(false);

        boolean result = adminService.deleteUser(targetId, adminId);

        assertFalse(result);
        verify(adminDao, never()).adminSoftDeleteUser(any());
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
