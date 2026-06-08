package com.resumainer.service;

import com.resumainer.dao.*;
import com.resumainer.dto.CoursePage;
import com.resumainer.dto.ProfileSectionStatus;
import com.resumainer.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private DataSource dataSource;
    @Mock private ContactDetailDao contactDetailDao;
    @Mock private WorkExperienceDao workExperienceDao;
    @Mock private EducationDao educationDao;
    @Mock private ProjectDao projectDao;
    @Mock private CourseCertificateDao courseCertificateDao;
    @Mock private AdditionalProfileInfoDao additionalProfileInfoDao;
    @Mock private WorkFormatDao workFormatDao;
    @Mock private UserDao userDao;

    private ProfileService profileService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(dataSource, contactDetailDao, workExperienceDao,
                educationDao, projectDao, courseCertificateDao,
                additionalProfileInfoDao, workFormatDao, userDao);
        userId = UUID.randomUUID();
    }

    // ========================================================================
    // Section Status
    // ========================================================================

    @Test
    void getSectionStatus_allComplete_returnsCompleted() {
        ContactDetail contact = new ContactDetail();
        contact.setFullName("John Doe");
        contact.setProfessionalTitle("Dev");
        contact.setResumeEmail("john@test.com");
        contact.setPhone("+123");
        contact.setLocation("NYC");
        when(contactDetailDao.findByUserId(userId)).thenReturn(contact);

        when(workExperienceDao.findByUserId(userId)).thenReturn(List.of(new WorkExperience()));
        when(educationDao.findByUserId(userId)).thenReturn(List.of(new Education(), new Education()));
        when(projectDao.findByUserId(userId)).thenReturn(List.of());
        when(courseCertificateDao.countByUserId(eq(userId), any(), any(), any())).thenReturn(5L);

        AdditionalProfileInfo additional = new AdditionalProfileInfo();
        additional.setCitizenship("US");
        additional.setDateOfBirth(LocalDate.of(1990, 1, 1));
        when(additionalProfileInfoDao.findByUserId(userId)).thenReturn(additional);

        ProfileSectionStatus status = profileService.getSectionStatus(userId);

        assertEquals("completed", status.getContact());
        assertEquals("completed", status.getAdditional());
        assertEquals(1, ((Map<?, ?>) status.getExperience()).get("count"));
        assertEquals(2, ((Map<?, ?>) status.getEducation()).get("count"));
        assertEquals(0, ((Map<?, ?>) status.getProjects()).get("count"));
        assertEquals(5, ((Map<?, ?>) status.getCourses()).get("count"));
    }

    @Test
    void getSectionStatus_contactIncomplete_returnsIncomplete() {
        when(contactDetailDao.findByUserId(userId)).thenReturn(new ContactDetail());
        when(workExperienceDao.findByUserId(userId)).thenReturn(List.of());
        when(educationDao.findByUserId(userId)).thenReturn(List.of());
        when(projectDao.findByUserId(userId)).thenReturn(List.of());
        when(courseCertificateDao.countByUserId(eq(userId), any(), any(), any())).thenReturn(0L);
        when(additionalProfileInfoDao.findByUserId(userId)).thenReturn(null);

        ProfileSectionStatus status = profileService.getSectionStatus(userId);

        assertEquals("incomplete", status.getContact());
        assertEquals("incomplete", status.getAdditional());
    }

    @Test
    void getSectionStatus_noContact_returnsIncomplete() {
        when(contactDetailDao.findByUserId(userId)).thenReturn(null);
        when(workExperienceDao.findByUserId(userId)).thenReturn(List.of());
        when(educationDao.findByUserId(userId)).thenReturn(List.of());
        when(projectDao.findByUserId(userId)).thenReturn(List.of());
        when(courseCertificateDao.countByUserId(eq(userId), any(), any(), any())).thenReturn(0L);
        when(additionalProfileInfoDao.findByUserId(userId)).thenReturn(null);

        ProfileSectionStatus status = profileService.getSectionStatus(userId);

        assertEquals("incomplete", status.getContact());
    }

    // ========================================================================
    // Contact Details
    // ========================================================================

    @Test
    void getContactDetails_returnsContact() {
        ContactDetail contact = new ContactDetail();
        contact.setFullName("John Doe");
        when(contactDetailDao.findByUserId(userId)).thenReturn(contact);

        ContactDetail result = profileService.getContactDetails(userId);

        assertEquals("John Doe", result.getFullName());
    }

    @Test
    void getContactDetails_notFound_returnsNull() {
        when(contactDetailDao.findByUserId(userId)).thenReturn(null);

        assertNull(profileService.getContactDetails(userId));
    }

    @Test
    void updateContactDetails_savesAndReturnsUpdated() {
        ContactDetail existing = new ContactDetail();
        existing.setUserId(userId);
        when(contactDetailDao.findByUserId(userId)).thenReturn(existing);

        ContactDetail updated = new ContactDetail();
        updated.setFullName("Jane");
        when(contactDetailDao.findByUserId(userId)).thenReturn(updated);

        ContactDetail result = profileService.updateContactDetails(userId, new ContactDetail());

        assertEquals("Jane", result.getFullName());
        verify(contactDetailDao).update(any(ContactDetail.class));
    }

    @Test
    void updateContactDetails_notFound_throwsException() {
        when(contactDetailDao.findByUserId(userId)).thenReturn(null);

        assertThrows(com.resumainer.exception.ServiceException.class,
                () -> profileService.updateContactDetails(userId, new ContactDetail()));
    }

    // ========================================================================
    // Work Experience
    // ========================================================================

    @Test
    void createWorkExperience_setsUserIdAndCreates() {
        WorkExperience exp = new WorkExperience();
        exp.setJobTitle("Dev");
        when(workExperienceDao.create(any())).thenAnswer(i -> i.getArgument(0));

        WorkExperience result = profileService.createWorkExperience(userId, exp);

        assertEquals(userId, result.getUserId());
        assertEquals("Dev", result.getJobTitle());
    }

    @Test
    void getWorkExperiences_returnsList() {
        when(workExperienceDao.findByUserId(userId)).thenReturn(List.of(new WorkExperience()));

        assertEquals(1, profileService.getWorkExperiences(userId).size());
    }

    @Test
    void deleteWorkExperience_owned_returnsTrue() {
        when(workExperienceDao.softDelete(1L, userId)).thenReturn(true);

        assertTrue(profileService.deleteWorkExperience(userId, 1L));
    }

    @Test
    void deleteWorkExperience_notOwned_returnsFalse() {
        when(workExperienceDao.softDelete(999L, userId)).thenReturn(false);

        assertFalse(profileService.deleteWorkExperience(userId, 999L));
    }

    // ========================================================================
    // Education
    // ========================================================================

    @Test
    void createEducation_setsUserIdAndCreates() {
        Education edu = new Education();
        edu.setInstitutionName("MIT");
        when(educationDao.create(any())).thenAnswer(i -> i.getArgument(0));

        Education result = profileService.createEducation(userId, edu);

        assertEquals(userId, result.getUserId());
        assertEquals("MIT", result.getInstitutionName());
    }

    // ========================================================================
    // Projects
    // ========================================================================

    @Test
    void createProject_setsUserIdAndCreates() {
        Project p = new Project();
        p.setProjectName("My App");
        when(projectDao.create(any())).thenAnswer(i -> i.getArgument(0));

        Project result = profileService.createProject(userId, p);

        assertEquals(userId, result.getUserId());
        assertEquals("My App", result.getProjectName());
    }

    // ========================================================================
    // Courses
    // ========================================================================

    @Test
    void getCourses_withPagination_returnsCoursePage() {
        CourseCertificate course = new CourseCertificate();
        course.setName("AWS");
        when(courseCertificateDao.findByUserId(eq(userId), any(), any(), any(),
                eq("start_date"), eq("desc"), eq(0), eq(10)))
                .thenReturn(List.of(course));
        when(courseCertificateDao.countByUserId(eq(userId), any(), any(), any()))
                .thenReturn(1L);

        CoursePage page = profileService.getCourses(userId, 0, 10,
                "start_date", "desc", null, null, null);

        assertEquals(1, page.getContent().size());
        assertEquals("AWS", page.getContent().get(0).getName());
        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getTotalPages());
    }

    @Test
    void getCourses_invalidSize_defaultsTo10() {
        when(courseCertificateDao.findByUserId(any(), any(), any(), any(),
                any(), any(), anyInt(), anyInt())).thenReturn(List.of());
        when(courseCertificateDao.countByUserId(any(), any(), any(), any()))
                .thenReturn(0L);

        CoursePage page = profileService.getCourses(userId, 0, 999,
                "start_date", "desc", null, null, null);

        assertEquals(10, page.getSize());
    }

    @Test
    void createCourse_setsUserIdAndCreates() {
        CourseCertificate c = new CourseCertificate();
        c.setName("AWS");
        when(courseCertificateDao.create(any())).thenAnswer(i -> i.getArgument(0));

        CourseCertificate result = profileService.createCourse(userId, c);

        assertEquals(userId, result.getUserId());
        assertEquals("AWS", result.getName());
    }

    // ========================================================================
    // Additional Info
    // ========================================================================

    @Test
    void getAdditionalInfo_noData_returnsEmptyFields() {
        when(additionalProfileInfoDao.findByUserId(userId)).thenReturn(null);
        when(userDao.findById(userId)).thenReturn(null);
        when(workFormatDao.findByUserId(userId)).thenReturn(List.of());

        Map<String, Object> result = profileService.getAdditionalInfo(userId);

        assertNull(result.get("username"));
        assertNotNull(result.get("acceptableWorkFormats"));
        assertTrue(((List<?>) result.get("acceptableWorkFormats")).isEmpty());
    }

    @Test
    void getAdditionalInfo_withData_returnsAggregated() {
        AdditionalProfileInfo info = new AdditionalProfileInfo();
        info.setSkills("Java");
        info.setCitizenship("US");
        info.setDateOfBirth(LocalDate.of(1990, 5, 15));
        when(additionalProfileInfoDao.findByUserId(userId)).thenReturn(info);

        User user = new User();
        user.setUsername("johndoe");
        when(userDao.findById(userId)).thenReturn(user);

        WorkFormat wf = new WorkFormat(1L, "remote", "Remote");
        when(workFormatDao.findByUserId(userId)).thenReturn(List.of(wf));

        Map<String, Object> result = profileService.getAdditionalInfo(userId);

        assertEquals("johndoe", result.get("username"));
        assertEquals("Java", result.get("skills"));
        assertEquals("US", result.get("citizenship"));
        assertEquals(List.of("remote"), result.get("acceptableWorkFormats"));
    }

    @Test
    void updateAdditionalInfo_withTransaction_savesAll() throws Exception {
        Connection conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);

        WorkFormat wf = new WorkFormat(1L, "remote", "Remote");
        when(workFormatDao.findAll()).thenReturn(List.of(wf));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", "newuser");
        data.put("skills", "Java");
        data.put("spokenLanguages", "English");
        data.put("citizenship", "US");
        data.put("dateOfBirth", "1990-05-15");
        data.put("acceptableWorkFormats", List.of("remote"));

        profileService.updateAdditionalInfo(userId, data);

        verify(conn).setAutoCommit(false);
        verify(userDao).updateUsername(eq(userId), eq("newuser"), eq(conn));
        verify(additionalProfileInfoDao).upsert(any(AdditionalProfileInfo.class), eq(conn));
        verify(workFormatDao).saveUserFormats(eq(userId), eq(List.of(1L)), eq(conn));
        verify(conn).commit();
    }

    @Test
    void updateAdditionalInfo_transactionFails_rollsBack() throws Exception {
        Connection conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);
        doThrow(new RuntimeException("DB error")).when(userDao)
                .updateUsername(any(), any(), any());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", "fail");
        data.put("citizenship", "US");
        data.put("dateOfBirth", "1990-01-01");

        assertThrows(com.resumainer.exception.ServiceException.class,
                () -> profileService.updateAdditionalInfo(userId, data));
        verify(conn).rollback();
    }

    // ========================================================================
    // Work Formats
    // ========================================================================

    @Test
    void getAllWorkFormats_returnsList() {
        when(workFormatDao.findAll()).thenReturn(List.of(
                new WorkFormat(1L, "remote", "Remote")));

        List<WorkFormat> result = profileService.getAllWorkFormats();

        assertEquals(1, result.size());
        assertEquals("remote", result.get(0).getCode());
    }
}
