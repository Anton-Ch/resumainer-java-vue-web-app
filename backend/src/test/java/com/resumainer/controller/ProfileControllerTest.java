package com.resumainer.controller;

import com.resumainer.dto.CoursePage;
import com.resumainer.dto.ProfileSectionStatus;
import com.resumainer.dto.UserSession;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.exception.ServiceException;
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
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
                        .content("{\"fullName\":\"Jane\",\"professionalTitle\":\"Dev\",\"phone\":\"+123\",\"resumeEmail\":\"jane@test.com\",\"location\":\"NYC\"}"))
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
    void updateExperience_returnsOk() throws Exception {
        WorkExperience exp = new WorkExperience();
        exp.setId(1L);
        exp.setJobTitle("Updated Dev");
        doNothing().when(profileService).updateWorkExperience(eq(userId), eq(1L), any());

        mockMvc.perform(put("/api/profile/experience/1")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Updated Dev\",\"companyName\":\"Co\",\"description\":\"Work\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Updated Dev"));
    }

    @Test
    void updateExperience_notFound_returns404() throws Exception {
        doThrow(new RuntimeException("Experience not found"))
                .when(profileService).updateWorkExperience(eq(userId), eq(999L), any());

        mockMvc.perform(put("/api/profile/experience/999")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Dev\",\"companyName\":\"Co\",\"description\":\"Work\",\"location\":\"Loc\",\"startDate\":\"2023-01-01\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Experience not found"));
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
    void updateEducation_returnsOk() throws Exception {
        Education edu = new Education();
        edu.setId(1L);
        edu.setInstitutionNameEn("Updated MIT");
        doNothing().when(profileService).updateEducation(eq(userId), eq(1L), any());

        mockMvc.perform(put("/api/profile/education/1")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionNameRu\":\"MIT RU\",\"institutionNameEn\":\"Updated MIT\",\"degreeRu\":\"BS RU\",\"degreeEn\":\"BS\",\"fieldOfStudyRu\":\"CS RU\",\"fieldOfStudyEn\":\"CS\",\"startDate\":\"2019-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionNameEn").value("Updated MIT"));
    }

    @Test
    void updateEducation_notFound_returns404() throws Exception {
        doThrow(new RuntimeException("Education not found"))
                .when(profileService).updateEducation(eq(userId), eq(999L), any());

        mockMvc.perform(put("/api/profile/education/999")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionNameRu\":\"MIT RU\",\"institutionNameEn\":\"MIT\",\"degreeRu\":\"BS RU\",\"degreeEn\":\"BS\",\"fieldOfStudyRu\":\"CS RU\",\"fieldOfStudyEn\":\"CS\",\"startDate\":\"2019-01-01\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Education not found"));
    }

    @Test
    void deleteEducation_returns204() throws Exception {
        when(profileService.deleteEducation(userId, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/profile/education/1")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEducation_notFound_returns404() throws Exception {
        when(profileService.deleteEducation(userId, 999L)).thenReturn(false);

        mockMvc.perform(delete("/api/profile/education/999")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEducation_returns201() throws Exception {
        Education edu = new Education();
        edu.setId(1L);
        edu.setInstitutionNameEn("MIT");
        edu.setInstitutionNameRu("MIT RU");
        edu.setDegreeEn("BS");
        edu.setDegreeRu("BS RU");
        edu.setFieldOfStudyEn("CS");
        edu.setFieldOfStudyRu("CS RU");
        when(profileService.createEducation(eq(userId), any())).thenReturn(edu);

        mockMvc.perform(post("/api/profile/education")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionNameRu\":\"MIT RU\",\"institutionNameEn\":\"MIT\",\"degreeRu\":\"BS RU\",\"degreeEn\":\"BS\",\"fieldOfStudyRu\":\"CS RU\",\"fieldOfStudyEn\":\"CS\",\"startDate\":\"2019-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.institutionNameEn").value("MIT"));
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
    void updateProject_returnsOk() throws Exception {
        Project p = new Project();
        p.setId(1L);
        p.setProjectName("Updated App");
        doNothing().when(profileService).updateProject(eq(userId), eq(1L), any());

        mockMvc.perform(put("/api/profile/projects/1")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectName\":\"Updated App\",\"description\":\"Better app\",\"location\":\"Remote\",\"startDate\":\"2024-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Updated App"));
    }

    @Test
    void updateProject_notFound_returns404() throws Exception {
        doThrow(new RuntimeException("Project not found"))
                .when(profileService).updateProject(eq(userId), eq(999L), any());

        mockMvc.perform(put("/api/profile/projects/999")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectName\":\"App\",\"description\":\"Built app\",\"location\":\"Remote\",\"startDate\":\"2024-01-01\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found"));
    }

    @Test
    void deleteProject_returns204() throws Exception {
        when(profileService.deleteProject(userId, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/profile/projects/1")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProject_notFound_returns404() throws Exception {
        when(profileService.deleteProject(userId, 999L)).thenReturn(false);

        mockMvc.perform(delete("/api/profile/projects/999")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNotFound());
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
    void getCourses_withCombinedSort_parsesCorrectly() throws Exception {
        when(profileService.getCourses(eq(userId), eq(0), eq(10), eq("provider"), eq("asc"),
                any(), any(), any())).thenReturn(new CoursePage(List.of(), 0, 0, 10));

        mockMvc.perform(get("/api/profile/courses")
                        .sessionAttr("user", userSession)
                        .param("sort", "provider,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updateCourse_returnsOk() throws Exception {
        CourseCertificate c = new CourseCertificate();
        c.setId(1L);
        c.setName("Updated AWS");
        doNothing().when(profileService).updateCourse(eq(userId), eq(1L), any());

        mockMvc.perform(put("/api/profile/courses/1")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"Updated AWS\",\"provider\":\"Coursera\",\"startDate\":\"2025-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("Updated AWS"));
    }

    @Test
    void updateCourse_notFound_returns404() throws Exception {
        doThrow(new RuntimeException("Course not found"))
                .when(profileService).updateCourse(eq(userId), eq(999L), any());

        mockMvc.perform(put("/api/profile/courses/999")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseName\":\"AWS\",\"provider\":\"Coursera\",\"startDate\":\"2025-01-01\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found"));
    }

    @Test
    void deleteCourse_returns204() throws Exception {
        when(profileService.deleteCourse(userId, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/profile/courses/1")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCourse_notFound_returns404() throws Exception {
        when(profileService.deleteCourse(userId, 999L)).thenReturn(false);

        mockMvc.perform(delete("/api/profile/courses/999")
                        .sessionAttr("user", userSession))
                .andExpect(status().isNotFound());
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

    @Test
    void updateAdditionalInfo_serviceException_returns500() throws Exception {
        doThrow(new ServiceException("test.error", "Service failed"))
                .when(profileService).updateAdditionalInfo(eq(userId), any());

        mockMvc.perform(put("/api/profile/additional")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Service failed"));
    }

    @Test
    void updateAdditionalInfo_genericException_returns400() throws Exception {
        doThrow(new RuntimeException("Bad data"))
                .when(profileService).updateAdditionalInfo(eq(userId), any());

        mockMvc.perform(put("/api/profile/additional")
                        .sessionAttr("user", userSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid data: Bad data"));
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
    void anyEndpoint_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/profile/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("auth.unauthorized"))
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }
}
