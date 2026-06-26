package com.resumainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dto.UserSession;
import com.resumainer.dto.admin.AdminDashboardDto;
import com.resumainer.dto.admin.AdminSavedResumeDto;
import com.resumainer.dto.admin.AdminUserAccessUpdateRequest;
import com.resumainer.dto.admin.AdminUserAccountDto;
import com.resumainer.dto.admin.AdminUserAdditionalInfoDto;
import com.resumainer.dto.admin.AdminUserContactDto;
import com.resumainer.dto.admin.AdminUserDetailsDto;
import com.resumainer.dto.admin.AdminUserListItemDto;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.model.PagedResponse;
import com.resumainer.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AdminControllerTest {

    private MockMvc mockMvc;
    private AdminService adminService;
    private ObjectMapper objectMapper;

    private AdminSavedResumeDto createTestDto() {
        AdminSavedResumeDto dto = new AdminSavedResumeDto();
        dto.setId(1L);
        dto.setOwnerUserId("uuid-1");
        dto.setOwnerUsername("anton");
        dto.setOwnerEmail("anton@example.com");
        dto.setOwnerFullName("Anton Ch.");
        dto.setResumeTitle("Java Dev");
        dto.setVacancyTitle("Senior Java Developer");
        dto.setCompanyName("ABC LTD");
        dto.setLanguageCode("RU");
        dto.setLanguageName("Russian");
        dto.setAdaptationLevel("BALANCED");
        dto.setCreatedAt("2026-06-25");
        dto.setPublicUrlLink("https://example.com/anton/CODE1");
        dto.setPdfOpenUrl("/api/generate/resumes/1/pdf?disposition=inline");
        dto.setPdfDownloadUrl("/api/generate/resumes/1/pdf");
        dto.setHtmlDownloadUrl("/api/generate/resumes/1/html");
        dto.setPdfAvailable(true);
        dto.setPdfStatus("READY");
        dto.setCoverLetter("Cover letter text");
        return dto;
    }

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        objectMapper = new ObjectMapper();

        AdminController controller = new AdminController(adminService);

        mockMvc = standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // --- Dashboard tests ---

    @Test
    void getDashboard_returnsOkWithCorrectFields() throws Exception {
        AdminDashboardDto dto = new AdminDashboardDto(10, 25);
        when(adminService.getDashboard()).thenReturn(dto);

        mockMvc.perform(get("/api/admin/dashboard")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalResumes").value(25))
                .andExpect(jsonPath("$.totalTokensSent").value(0))
                .andExpect(jsonPath("$.totalTokensSentWip").value(true))
                .andExpect(jsonPath("$.totalTokensGenerated").value(0))
                .andExpect(jsonPath("$.totalTokensGeneratedWip").value(true));

        verify(adminService).getDashboard();
    }

    @Test
    void getDashboard_returnsSafeError_whenServiceFails() throws Exception {
        when(adminService.getDashboard()).thenThrow(new RuntimeException("Internal DB error"));

        mockMvc.perform(get("/api/admin/dashboard")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- Admin resumes tests (T039) ---

    @Test
    void getResumes_returnsOkWithItemsField() throws Exception {
        PagedResponse<AdminSavedResumeDto> paged = new PagedResponse<>(
                List.of(createTestDto()), 0, 10, 1);
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/resumes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Must use "items" not "content"
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].ownerUsername").value("anton"))
                .andExpect(jsonPath("$.items[0].ownerEmail").value("anton@example.com"))
                .andExpect(jsonPath("$.items[0].ownerFullName").value("Anton Ch."))
                .andExpect(jsonPath("$.items[0].resumeTitle").value("Java Dev"))
                .andExpect(jsonPath("$.items[0].pdfOpenUrl").exists())
                .andExpect(jsonPath("$.items[0].pdfDownloadUrl").exists())
                .andExpect(jsonPath("$.items[0].pdfAvailable").value(true))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                // Ensure no raw paths exposed
                .andExpect(jsonPath("$.items[0].pdfFilePath").doesNotExist())
                .andExpect(jsonPath("$.items[0].htmlFilePath").doesNotExist());

        verify(adminService).getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void getResumes_usesItemsNotContent() throws Exception {
        PagedResponse<AdminSavedResumeDto> paged = new PagedResponse<>(
                List.of(createTestDto()), 0, 10, 1);
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/resumes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").doesNotExist())
                .andExpect(jsonPath("$.items").exists());
    }

    @Test
    void getResumes_passesQueryParameters() throws Exception {
        PagedResponse<AdminSavedResumeDto> paged = new PagedResponse<>(List.of(), 0, 10, 0);
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/resumes")
                        .param("page", "1")
                        .param("size", "20")
                        .param("search", "java")
                        .param("language", "EN")
                        .param("adaptationLevel", "BALANCED")
                        .param("createdFrom", "2026-06-01")
                        .param("createdTo", "2026-06-25")
                        .param("sort", "resumeTitle,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminService).getResumes(
                eq("java"), eq("EN"), eq("BALANCED"),
                eq("2026-06-01"), eq("2026-06-25"),
                eq("resumeTitle"), eq("asc"), eq(1), eq(20));
    }

    @Test
    void getResumes_defaultParameters() throws Exception {
        PagedResponse<AdminSavedResumeDto> paged = new PagedResponse<>(List.of(), 0, 10, 0);
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/resumes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminService).getResumes(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("createdAt"), eq("desc"), eq(0), eq(10));
    }

    @Test
    void getResumes_parsesSortParameter() throws Exception {
        PagedResponse<AdminSavedResumeDto> paged = new PagedResponse<>(List.of(), 0, 10, 0);
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        // Sort without direction should default to desc
        mockMvc.perform(get("/api/admin/resumes")
                        .param("sort", "ownerUsername")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminService).getResumes(any(), any(), any(), any(), any(),
                eq("ownerUsername"), eq("desc"), anyInt(), anyInt());
    }

    @Test
    void getResumes_emptyResult() throws Exception {
        PagedResponse<AdminSavedResumeDto> paged = new PagedResponse<>(List.of(), 0, 10, 0);
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/resumes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getResumes_returnsSafeError_whenServiceFails() throws Exception {
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/admin/resumes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- Fix 1: Invalid params return 400, not 500 ---

    @Test
    void getResumes_invalidSortField_returnsBadRequest() throws Exception {
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid sort field: password"));

        mockMvc.perform(get("/api/admin/resumes")
                        .param("sort", "password,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getResumes_invalidDate_returnsBadRequest() throws Exception {
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new com.resumainer.exception.ServiceException("INVALID_DATE",
                        "Invalid date format for createdFrom"));

        mockMvc.perform(get("/api/admin/resumes")
                        .param("createdFrom", "not-a-date")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_DATE"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getResumes_negativePage_doesNotThrow() throws Exception {
        // Spring MVC default param binding accepts negative int — let the DAO handle it
        // This should not crash
        PagedResponse<AdminSavedResumeDto> paged = new PagedResponse<>(List.of(), 0, 10, 0);
        when(adminService.getResumes(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/resumes")
                        .param("page", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- Phase 4: Admin users listing tests ---

    @Test
    void getUsers_returnsOkWithItemsField() throws Exception {
        AdminUserListItemDto dto = new AdminUserListItemDto();
        dto.setId("uuid-1");
        dto.setUsername("johndoe");
        dto.setEmail("john@example.com");
        dto.setFullName("John Doe");
        dto.setRoleCode("USER");
        dto.setRoleName("Regular User");
        dto.setStatusCode("ACTIVE");
        dto.setStatusName("Active");
        dto.setPermissionCode("ALLOWED");
        dto.setPrivileged(false);
        dto.setResumesCount(3L);
        dto.setCreatedAt("2026-06-01T12:00:00");

        PagedResponse<AdminUserListItemDto> paged = new PagedResponse<>(
                List.of(dto), 0, 10, 1);
        when(adminService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].username").value("johndoe"))
                .andExpect(jsonPath("$.items[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.items[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$.items[0].roleCode").value("USER"))
                .andExpect(jsonPath("$.items[0].statusCode").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].resumesCount").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                // Ensure no content field (must be items)
                .andExpect(jsonPath("$.content").doesNotExist())
                // Ensure no sensitive fields
                .andExpect(jsonPath("$.items[0].passwordHash").doesNotExist());
    }

    @Test
    void getUsers_passesQueryParameters() throws Exception {
        PagedResponse<AdminUserListItemDto> paged = new PagedResponse<>(List.of(), 0, 10, 0);
        when(adminService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/users")
                        .param("search", "john")
                        .param("role", "USER")
                        .param("status", "ACTIVE")
                        .param("permission", "ALLOWED")
                        .param("rights", "PRIVILEGED")
                        .param("createdFrom", "2026-06-01")
                        .param("createdTo", "2026-06-25")
                        .param("sort", "username,asc")
                        .param("page", "1")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminService).getUsers(
                eq("john"), eq("USER"), eq("ACTIVE"), eq("ALLOWED"), eq("PRIVILEGED"),
                eq("2026-06-01"), eq("2026-06-25"),
                eq("username"), eq("asc"), eq(1), eq(20));
    }

    @Test
    void getUsers_defaultParameters() throws Exception {
        PagedResponse<AdminUserListItemDto> paged = new PagedResponse<>(List.of(), 0, 10, 0);
        when(adminService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminService).getUsers(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(),
                eq("createdAt"), eq("desc"), eq(0), eq(10));
    }

    @Test
    void getUsers_emptyResult() throws Exception {
        PagedResponse<AdminUserListItemDto> paged = new PagedResponse<>(List.of(), 0, 10, 0);
        when(adminService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getUsers_invalidDate_returnsBadRequest() throws Exception {
        when(adminService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new com.resumainer.exception.ServiceException("INVALID_DATE",
                        "Invalid date format for createdFrom"));

        mockMvc.perform(get("/api/admin/users")
                        .param("createdFrom", "not-a-date")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_DATE"));
    }

    @Test
    void getUsers_returnsSafeError_whenServiceFails() throws Exception {
        when(adminService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/admin/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- Phase 5: Admin user details tests ---

    private UUID adminUserId = UUID.randomUUID();
    private UUID otherUserId = UUID.randomUUID();

    private AdminUserDetailsDto createTestUserDetails(boolean isCurrentAdmin) {
        AdminUserDetailsDto dto = new AdminUserDetailsDto();
        dto.setId(isCurrentAdmin ? adminUserId.toString() : otherUserId.toString());
        dto.setCurrentAdmin(isCurrentAdmin);

        var account = new AdminUserAccountDto();
        account.setId(dto.getId());
        account.setUsername("johndoe");
        account.setAccountEmail("john@example.com");
        account.setRoleCode("USER");
        account.setStatusCode("ACTIVE");
        dto.setAccount(account);

        var contacts = new AdminUserContactDto();
        contacts.setFullName("John Doe");
        contacts.setResumeEmail("resume@example.com");
        dto.setContacts(contacts);

        var additional = new AdminUserAdditionalInfoDto();
        additional.setSkills("Java");
        dto.setAdditionalInfo(additional);

        return dto;
    }

    @Test
    void getUserDetails_returnsOk_whenUserExists() throws Exception {
        AdminUserDetailsDto dto = createTestUserDetails(false);
        when(adminService.getUserDetails(otherUserId, adminUserId)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/users/{userId}", otherUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(otherUserId.toString()))
                .andExpect(jsonPath("$.isCurrentAdmin").value(false))
                .andExpect(jsonPath("$.account.accountEmail").value("john@example.com"))
                .andExpect(jsonPath("$.contacts.resumeEmail").value("resume@example.com"))
                .andExpect(jsonPath("$.account.roleCode").value("USER"))
                .andExpect(jsonPath("$.account.isPrivileged").value(false))
                .andExpect(jsonPath("$.additionalInfo.skills").value("Java"))
                // Negative assertions: old names must not exist
                .andExpect(jsonPath("$.currentAdmin").doesNotExist())
                .andExpect(jsonPath("$.account.privileged").doesNotExist());
    }

    @Test
    void getUserDetails_isCurrentAdmin_true_whenViewingSelf() throws Exception {
        AdminUserDetailsDto dto = createTestUserDetails(true);
        dto.setId(adminUserId.toString());
        dto.setCurrentAdmin(true);
        when(adminService.getUserDetails(adminUserId, adminUserId)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/users/{userId}", adminUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCurrentAdmin").value(true))
                .andExpect(jsonPath("$.currentAdmin").doesNotExist());
    }

    @Test
    void getUserDetails_returnsNotFound_whenUserDoesNotExist() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(adminService.getUserDetails(missingId, adminUserId)).thenReturn(null);

        mockMvc.perform(get("/api/admin/users/{userId}", missingId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getUserDetails_returnsSafeError_whenServiceFails() throws Exception {
        when(adminService.getUserDetails(any(), any())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/admin/users/{userId}", otherUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getUserDetails_noSession_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users/{userId}", otherUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserDetails_dtoHasNoSensitiveFields() throws Exception {
        AdminUserDetailsDto dto = createTestUserDetails(false);
        when(adminService.getUserDetails(otherUserId, adminUserId)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/users/{userId}", otherUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.account.password_hash").doesNotExist())
                .andExpect(jsonPath("$.additionalInfo.photoFilePath").doesNotExist());
    }

    // --- Phase 6: Access update tests ---

    @Test
    void updateUserAccess_returnsUpdatedDetails_whenSuccess() throws Exception {
        AdminUserDetailsDto details = createTestUserDetails(false);
        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();
        req.setRoleCode("ADMIN");
        req.setStatusCode("ACTIVE");
        req.setPermissionCode("ALLOWED");
        req.setPrivileged(true);

        when(adminService.updateUserAccess(eq(otherUserId), eq(adminUserId), any()))
                .thenReturn(details);

        mockMvc.perform(patch("/api/admin/users/{userId}/access", otherUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(otherUserId.toString()))
                .andExpect(jsonPath("$.account.accountEmail").value("john@example.com"));
    }

    @Test
    void updateUserAccess_deserializesIsPrivilegedFromJson() throws Exception {
        when(adminService.updateUserAccess(eq(otherUserId), eq(adminUserId), any()))
                .thenReturn(createTestUserDetails(false));

        // Send raw JSON with isPrivileged: true and verify it is deserialized correctly
        String rawJson = "{\"roleCode\":\"ADMIN\",\"statusCode\":\"ACTIVE\",\"permissionCode\":\"ALLOWED\",\"isPrivileged\":true}";

        mockMvc.perform(patch("/api/admin/users/{userId}/access", otherUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<AdminUserAccessUpdateRequest> captor =
                org.mockito.ArgumentCaptor.forClass(AdminUserAccessUpdateRequest.class);
        verify(adminService).updateUserAccess(eq(otherUserId), eq(adminUserId), captor.capture());

        AdminUserAccessUpdateRequest captured = captor.getValue();
        assertNotNull(captured);
        assertEquals("ADMIN", captured.getRoleCode());
        assertTrue(captured.isPrivileged(), "isPrivileged must be true from JSON isPrivileged: true");
    }

    @Test
    void updateUserAccess_deserializesIsPrivilegedFalse() throws Exception {
        when(adminService.updateUserAccess(eq(otherUserId), eq(adminUserId), any()))
                .thenReturn(createTestUserDetails(false));

        String rawJson = "{\"roleCode\":\"USER\",\"statusCode\":\"ACTIVE\",\"permissionCode\":\"ALLOWED\",\"isPrivileged\":false}";

        mockMvc.perform(patch("/api/admin/users/{userId}/access", otherUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<AdminUserAccessUpdateRequest> captor =
                org.mockito.ArgumentCaptor.forClass(AdminUserAccessUpdateRequest.class);
        verify(adminService).updateUserAccess(eq(otherUserId), eq(adminUserId), captor.capture());

        AdminUserAccessUpdateRequest captured = captor.getValue();
        assertNotNull(captured);
        assertFalse(captured.isPrivileged(), "isPrivileged must be false from JSON isPrivileged: false");
    }

    @Test
    void updateUserAccess_invalidCode_returnsBadRequest() throws Exception {
        when(adminService.updateUserAccess(any(), any(), any()))
                .thenThrow(new com.resumainer.exception.ServiceException("INVALID_ROLE", "Invalid role code"));

        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();
        req.setRoleCode("INVALID");

        mockMvc.perform(patch("/api/admin/users/{userId}/access", otherUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateUserAccess_selfDemotion_returnsBadRequest() throws Exception {
        when(adminService.updateUserAccess(eq(adminUserId), eq(adminUserId), any()))
                .thenThrow(new IllegalArgumentException("You cannot demote your own admin account."));

        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();
        req.setRoleCode("USER");

        mockMvc.perform(patch("/api/admin/users/{userId}/access", adminUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateUserAccess_notFound_returnsNotFound() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(adminService.updateUserAccess(eq(missingId), eq(adminUserId), any())).thenReturn(null);

        AdminUserAccessUpdateRequest req = new AdminUserAccessUpdateRequest();

        mockMvc.perform(patch("/api/admin/users/{userId}/access", missingId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- Phase 6: User soft-delete tests ---

    @Test
    void deleteUser_returnsOk_whenSuccess() throws Exception {
        when(adminService.deleteUser(otherUserId, adminUserId)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/users/{userId}", otherUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted"));

        verify(adminService).deleteUser(otherUserId, adminUserId);
    }

    @Test
    void deleteUser_selfDelete_returnsBadRequest() throws Exception {
        when(adminService.deleteUser(adminUserId, adminUserId))
                .thenThrow(new IllegalArgumentException("You cannot delete your own admin account."));

        mockMvc.perform(delete("/api/admin/users/{userId}", adminUserId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteUser_notFound_returnsNotFound() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(adminService.deleteUser(missingId, adminUserId)).thenReturn(false);

        mockMvc.perform(delete("/api/admin/users/{userId}", missingId)
                        .sessionAttr("user", new UserSession(adminUserId, "admin@test.com", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteUser_noSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{userId}", otherUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- Phase 3: Admin resume delete tests ---

    @Test
    void deleteResume_returnsOkWithMessage_whenSuccess() throws Exception {
        when(adminService.deleteResume(101L)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/resumes/101")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(adminService).deleteResume(101L);
    }

    @Test
    void deleteResume_returnsNotFound_whenNoSuchResume() throws Exception {
        when(adminService.deleteResume(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/admin/resumes/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteResume_returnsSafeError_whenServiceFails() throws Exception {
        when(adminService.deleteResume(anyLong())).thenThrow(new RuntimeException("Internal DB error"));

        mockMvc.perform(delete("/api/admin/resumes/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").exists());
    }
}
