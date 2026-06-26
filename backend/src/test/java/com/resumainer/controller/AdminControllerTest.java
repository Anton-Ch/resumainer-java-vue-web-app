package com.resumainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dto.admin.AdminDashboardDto;
import com.resumainer.dto.admin.AdminSavedResumeDto;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.model.PagedResponse;
import com.resumainer.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
