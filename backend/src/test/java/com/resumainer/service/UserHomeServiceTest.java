package com.resumainer.service;

import com.resumainer.dao.AdditionalProfileInfoDao;
import com.resumainer.dao.ContactDetailDao;
import com.resumainer.dao.EducationDao;
import com.resumainer.dao.ResumeDao;
import com.resumainer.dao.WorkExperienceDao;
import com.resumainer.model.AdditionalProfileInfo;
import com.resumainer.model.ContactDetail;
import com.resumainer.model.SavedResume;
import com.resumainer.model.UserHomeSummary;
import com.resumainer.dto.home.HomeSavedResumeDto;
import com.resumainer.model.WorkExperience;
import com.resumainer.model.Education;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UserHomeService.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class UserHomeServiceTest {

    @Mock
    private ContactDetailDao contactDetailDao;

    @Mock
    private ResumeDao resumeDao;

    @Mock
    private WorkExperienceDao workExperienceDao;

    @Mock
    private EducationDao educationDao;

    @Mock
    private AdditionalProfileInfoDao additionalProfileInfoDao;

    @Mock
    private HomeSavedResumeMapper homeMapper;

    @Mock
    private HttpServletRequest request;

    private UserHomeService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new UserHomeService(contactDetailDao, resumeDao,
                workExperienceDao, educationDao, additionalProfileInfoDao, homeMapper);

        // Default: complete profile
        ContactDetail contact = new ContactDetail();
        contact.setFullName("John Doe");
        contact.setResumeEmail("john@example.com");
        contact.setPhone("+1-555-1234");
        contact.setLocation("New York");
        when(contactDetailDao.findByUserId(userId)).thenReturn(contact);

        when(workExperienceDao.findByUserId(userId)).thenReturn(List.of(new WorkExperience()));
        when(educationDao.findByUserId(userId)).thenReturn(List.of(new Education()));

        AdditionalProfileInfo info = new AdditionalProfileInfo();
        info.setDateOfBirth(LocalDate.of(1990, 1, 15));
        info.setCitizenship("US");
        when(additionalProfileInfoDao.findByUserId(userId)).thenReturn(info);

        SavedResume resume = new SavedResume();
        resume.setId(1L);
        when(resumeDao.findByUserId(eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq("created_at"), eq("desc"), eq(0), eq(1)))
                .thenReturn(List.of(resume));
        when(resumeDao.countByUserId(eq(userId), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(3L);

        // Stub homeMapper.toDto for the resume used in getHomeSummary
        HomeSavedResumeDto dto = new HomeSavedResumeDto();
        dto.setId(1L);
        when(homeMapper.toDto(resume, request)).thenReturn(dto);
    }

    @Test
    void getHomeSummary_withCompleteProfile_returnsReady() {
        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertTrue(result.isProfileReady());
        assertNotNull(result.getProfileChecklist());
        assertTrue(result.getProfileChecklist().isContactDetails());
        assertTrue(result.getProfileChecklist().isWorkExperience());
        assertTrue(result.getProfileChecklist().isEducation());
        assertTrue(result.getProfileChecklist().isAdditionalInfo());

        assertNotNull(result.getSummary());
        assertEquals(3L, result.getSummary().getSavedResumesCount());
        assertEquals("READY", result.getSummary().getProfileStatus());
        assertEquals(1L, result.getSummary().getLastResumeId());

        assertNotNull(result.getLastResume());
        assertEquals(1L, result.getLastResume().getId());
    }

    @Test
    void getHomeSummary_withMissingContactDetails_returnsIncomplete() {
        ContactDetail incomplete = new ContactDetail();
        incomplete.setFullName("John Doe");
        incomplete.setResumeEmail("john@example.com");
        // Phone is null — incomplete
        incomplete.setLocation("New York");
        when(contactDetailDao.findByUserId(userId)).thenReturn(incomplete);

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isContactDetails());
        assertEquals("INCOMPLETE", result.getSummary().getProfileStatus());
    }

    @Test
    void getHomeSummary_withNullContact_returnsIncomplete() {
        when(contactDetailDao.findByUserId(userId)).thenReturn(null);

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isContactDetails());
    }

    @Test
    void getHomeSummary_withoutWorkExperience_returnsIncomplete() {
        when(workExperienceDao.findByUserId(userId)).thenReturn(List.of());

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isWorkExperience());
    }

    @Test
    void getHomeSummary_withoutEducation_returnsIncomplete() {
        when(educationDao.findByUserId(userId)).thenReturn(List.of());

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isEducation());
    }

    @Test
    void getHomeSummary_withoutAdditionalInfo_returnsIncomplete() {
        AdditionalProfileInfo empty = new AdditionalProfileInfo();
        empty.setDateOfBirth(null);
        empty.setCitizenship(null);
        when(additionalProfileInfoDao.findByUserId(userId)).thenReturn(empty);

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isAdditionalInfo());
    }

    @Test
    void getHomeSummary_withNullAdditionalInfo_returnsIncomplete() {
        when(additionalProfileInfoDao.findByUserId(userId)).thenReturn(null);

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isAdditionalInfo());
    }

    @Test
    void getHomeSummary_withNoResumes_returnsNullLastResume() {
        when(resumeDao.findByUserId(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), anyInt(), anyInt())).thenReturn(List.of());
        when(resumeDao.countByUserId(any(), any(), any(), any(), any(), any(), any())).thenReturn(0L);

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertEquals(0L, result.getSummary().getSavedResumesCount());
        assertNull(result.getSummary().getLastResumeId());
        assertNull(result.getLastResume());
    }

    @Test
    void getHomeSummary_whenContactDaoThrows_returnsIncomplete() {
        when(contactDetailDao.findByUserId(userId)).thenThrow(new RuntimeException("DB error"));

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isContactDetails());
    }

    @Test
    void getHomeSummary_whenWorkExpDaoThrows_returnsIncomplete() {
        when(workExperienceDao.findByUserId(userId)).thenThrow(new RuntimeException("DB error"));

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isWorkExperience());
    }

    @Test
    void getHomeSummary_whenEducationDaoThrows_returnsIncomplete() {
        when(educationDao.findByUserId(userId)).thenThrow(new RuntimeException("DB error"));

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isEducation());
    }

    @Test
    void getHomeSummary_whenAdditionalInfoDaoThrows_returnsIncomplete() {
        when(additionalProfileInfoDao.findByUserId(userId)).thenThrow(new RuntimeException("DB error"));

        UserHomeSummary result = service.getHomeSummary(userId, request);

        assertFalse(result.isProfileReady());
        assertFalse(result.getProfileChecklist().isAdditionalInfo());
    }
}
