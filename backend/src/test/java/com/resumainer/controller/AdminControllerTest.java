package com.resumainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumainer.dto.admin.AdminDashboardDto;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AdminControllerTest {

    private MockMvc mockMvc;
    private AdminService adminService;
    private ObjectMapper objectMapper;

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
}
