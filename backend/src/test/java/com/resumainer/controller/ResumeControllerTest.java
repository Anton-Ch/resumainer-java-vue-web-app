package com.resumainer.controller;

import com.resumainer.dto.UserSession;
import com.resumainer.model.PagedResponse;
import com.resumainer.model.SavedResume;
import com.resumainer.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ResumeControllerTest {

    private MockMvc mockMvc;
    private ResumeService resumeService;
    private UUID userId;
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        resumeService = mock(ResumeService.class);
        ResumeController controller = new ResumeController(resumeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        userId = UUID.randomUUID();
        userSession = new UserSession(userId, "test@test.com", "USER");
    }

    @Test
    void listResumes_returnsPagedResponse() throws Exception {
        List<SavedResume> items = List.of(new SavedResume(), new SavedResume());
        PagedResponse<SavedResume> response = new PagedResponse<>(items, 0, 10, 2);
        when(resumeService.listResumes(eq(userId), isNull(), isNull(), isNull(), isNull(), eq("createdAt,desc"), eq(0), eq(10)))
                .thenReturn(response);

        mockMvc.perform(get("/api/resumes")
                        .sessionAttr("user", userSession)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void listResumes_withSearch_returnsFiltered() throws Exception {
        when(resumeService.listResumes(eq(userId), eq("analyst"), isNull(), isNull(), isNull(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        mockMvc.perform(get("/api/resumes")
                        .sessionAttr("user", userSession)
                        .param("search", "analyst"))
                .andExpect(status().isOk());
    }

    @Test
    void listResumes_withLanguageFilter() throws Exception {
        when(resumeService.listResumes(eq(userId), isNull(), eq("EN,RU"), isNull(), isNull(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        mockMvc.perform(get("/api/resumes")
                        .sessionAttr("user", userSession)
                        .param("language", "EN,RU"))
                .andExpect(status().isOk());
    }

    @Test
    void listResumes_withInvalidSize_returns400() throws Exception {
        when(resumeService.listResumes(eq(userId), isNull(), isNull(), isNull(), isNull(), anyString(), eq(0), eq(7)))
                .thenThrow(new IllegalArgumentException("Invalid size"));

        mockMvc.perform(get("/api/resumes")
                        .sessionAttr("user", userSession)
                        .param("size", "7"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listResumes_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/resumes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteResume_ownedByUser_returns200() throws Exception {
        when(resumeService.deleteResume(userId, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/resumes/1")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resume deleted"));
    }

    @Test
    void deleteResume_notFound_returns404() throws Exception {
        when(resumeService.deleteResume(userId, 999L)).thenReturn(false);

        mockMvc.perform(delete("/api/resumes/999")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNotFound());
    }
}
