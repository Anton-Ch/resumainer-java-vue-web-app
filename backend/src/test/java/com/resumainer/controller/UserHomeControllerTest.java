package com.resumainer.controller;

import com.resumainer.dto.UserSession;
import com.resumainer.model.SavedResume;
import com.resumainer.model.UserHomeSummary;
import com.resumainer.model.UserHomeSummary.ProfileChecklist;
import com.resumainer.model.UserHomeSummary.Summary;
import com.resumainer.service.UserHomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserHomeControllerTest {

    private MockMvc mockMvc;
    private UserHomeService userHomeService;
    private UUID userId;
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        userHomeService = mock(UserHomeService.class);
        UserHomeController controller = new UserHomeController(userHomeService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        userId = UUID.randomUUID();
        userSession = new UserSession(userId, "test@test.com", "USER");
    }

    @Test
    void getHomeSummary_withValidSession_returns200() throws Exception {
        SavedResume lastResume = new SavedResume();
        lastResume.setId(1L);
        lastResume.setResumeTitle("Test Resume");

        UserHomeSummary summary = new UserHomeSummary();
        summary.setProfileReady(false);
        summary.setProfileChecklist(new ProfileChecklist(false, false, false, false));
        summary.setSummary(new Summary(0, "INCOMPLETE", null));
        when(userHomeService.getHomeSummary(userId)).thenReturn(summary);

        mockMvc.perform(get("/api/user/home")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileReady").value(false))
                .andExpect(jsonPath("$.summary.savedResumesCount").value(0))
                .andExpect(jsonPath("$.summary.profileStatus").value("INCOMPLETE"));

        verify(userHomeService).getHomeSummary(userId);
    }

    @Test
    void getHomeSummary_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/user/home"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getHomeSummary_readyProfile_returnsCorrectData() throws Exception {
        SavedResume lastResume = new SavedResume();
        lastResume.setId(5L);
        lastResume.setResumeTitle("Senior Dev Resume");

        UserHomeSummary summary = new UserHomeSummary();
        summary.setProfileReady(true);
        summary.setProfileChecklist(new ProfileChecklist(true, true, true, true));
        summary.setSummary(new Summary(3, "READY", 5L));
        summary.setLastResume(lastResume);
        when(userHomeService.getHomeSummary(userId)).thenReturn(summary);

        mockMvc.perform(get("/api/user/home")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileReady").value(true))
                .andExpect(jsonPath("$.summary.savedResumesCount").value(3))
                .andExpect(jsonPath("$.summary.profileStatus").value("READY"))
                .andExpect(jsonPath("$.lastResume.resumeTitle").value("Senior Dev Resume"));
    }
}
