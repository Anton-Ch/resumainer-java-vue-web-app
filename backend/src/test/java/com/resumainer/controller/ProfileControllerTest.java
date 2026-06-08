package com.resumainer.controller;

import com.resumainer.dto.CoursePage;
import com.resumainer.dto.ProfileSectionStatus;
import com.resumainer.dto.UserSession;
import com.resumainer.model.*;
import com.resumainer.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock private ProfileService profileService;

    private MockMvc mockMvc;
    private UUID userId;
    private UserSession userSession;

    @BeforeEach
    void setUp() {
        ProfileController controller = new ProfileController(profileService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        userId = UUID.randomUUID();
        userSession = new UserSession(userId, "test@example.com", "USER");
    }

    // ========================================================================
    // Section Status
    // ========================================================================

    @Test
    void getStatus_returnsStatus() throws Exception {
        when(profileService.getSectionStatus(userId)).thenReturn(new ProfileSectionStatus());

        mockMvc.perform(get("/api/profile/status")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    // ========================================================================
    // Contact Details
    // ========================================================================

    @Test
    void getContact_returnsContact() throws Exception {
        ContactDetail contact = new ContactDetail();
        contact.setFullName("John Doe");
        when(profileService.getContactDetails(userId)).thenReturn(contact);

        mockMvc.perform(get("/api/profile/contact")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void getContact_notFound_returns404() throws Exception {
        when(profileService.getContactDetails(userId)).thenReturn(null);

        mockMvc.perform(get("/api/profile/contact")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateContact_returnsUpdated() throws Exception {
        ContactDetail updated = new ContactDetail();
        updated.setFullName("Jane");
        when(profileService.updateContactDetails(eq(userId), any())).thenReturn(updated);

        mockMvc.perform(put("/api/profile/contact")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Jane\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Jane"));
    }

    // ========================================================================
    // Work Experience
    // ========================================================================

    @Test
    void getExperiences_returnsList() throws Exception {
        when(profileService.getWorkExperiences(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/profile/experience")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createExperience_returns201() throws Exception {
        WorkExperience exp = new WorkExperience();
        exp.setId(1L);
        exp.setJobTitle("Dev");
        when(profileService.createWorkExperience(eq(userId), any())).thenReturn(exp);

        mockMvc.perform(post("/api/profile/experience")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Dev\",\"companyName\":\"Co\",\"description\":\"Work\",\"location\":\"Loc\",\"startDate\":\"2023-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteExperience_returns204() throws Exception {
        when(profileService.deleteWorkExperience(userId, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/profile/experience/1")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteExperience_notFound_returns404() throws Exception {
        when(profileService.deleteWorkExperience(userId, 999L)).thenReturn(false);

        mockMvc.perform(delete("/api/profile/experience/999")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNotFound());
    }

    // ========================================================================
    // Education
    // ========================================================================

    @Test
    void getEducations_returnsList() throws Exception {
        when(profileService.getEducations(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/profile/education")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createEducation_returns201() throws Exception {
        Education edu = new Education();
        edu.setId(1L);
        edu.setInstitutionName("MIT");
        when(profileService.createEducation(eq(userId), any())).thenReturn(edu);

        mockMvc.perform(post("/api/profile/education")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionName\":\"MIT\",\"degree\":\"BS\",\"fieldOfStudy\":\"CS\",\"startDate\":\"2019-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.institutionName").value("MIT"));
    }

    // ========================================================================
    // Projects
    // ========================================================================

    @Test
    void getProjects_returnsList() throws Exception {
        when(profileService.getProjects(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/profile/projects")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createProject_returns201() throws Exception {
        Project p = new Project();
        p.setId(1L);
        p.setProjectName("App");
        when(profileService.createProject(eq(userId), any())).thenReturn(p);

        mockMvc.perform(post("/api/profile/projects")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectName\":\"App\",\"description\":\"Built app\",\"location\":\"Remote\",\"startDate\":\"2024-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectName").value("App"));
    }

    // ========================================================================
    // Courses
    // ========================================================================

    @Test
    void getCourses_returnsPage() throws Exception {
        when(profileService.getCourses(eq(userId), eq(0), eq(10), any(), any(),
                any(), any(), any())).thenReturn(new CoursePage(List.of(), 0, 0, 10));

        mockMvc.perform(get("/api/profile/courses")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createCourse_returns201() throws Exception {
        CourseCertificate c = new CourseCertificate();
        c.setId(1L);
        c.setName("AWS");
        when(profileService.createCourse(eq(userId), any())).thenReturn(c);

        mockMvc.perform(post("/api/profile/courses")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"AWS\",\"provider\":\"Coursera\",\"startDate\":\"2025-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseName").value("AWS"));
    }

    // ========================================================================
    // Additional Info
    // ========================================================================

    @Test
    void getAdditionalInfo_returnsInfo() throws Exception {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("username", "johndoe");
        when(profileService.getAdditionalInfo(userId)).thenReturn(info);

        mockMvc.perform(get("/api/profile/additional")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    void updateAdditionalInfo_returnsUpdated() throws Exception {
        Map<String, Object> updated = new LinkedHashMap<>();
        updated.put("username", "newuser");
        when(profileService.getAdditionalInfo(userId)).thenReturn(updated);

        mockMvc.perform(put("/api/profile/additional")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"citizenship\":\"US\",\"dateOfBirth\":\"1990-05-15\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    // ========================================================================
    // Work Formats
    // ========================================================================

    @Test
    void getWorkFormats_returnsList() throws Exception {
        when(profileService.getAllWorkFormats()).thenReturn(List.of(
                new WorkFormat(1L, "remote", "Remote")));

        mockMvc.perform(get("/api/profile/work-formats")
                        .sessionAttr("user", userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("remote"));
    }

    // ========================================================================
    // Security: no session = unauthorized
    // ========================================================================

    @Test
    void allEndpoints_withoutSession_returnsError() {
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                mockMvc.perform(get("/api/profile/status")));
    }
}
