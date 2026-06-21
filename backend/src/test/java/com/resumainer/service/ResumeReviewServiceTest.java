package com.resumainer.service;

import com.resumainer.dao.GenerationRequestDao;
import com.resumainer.dao.GenerationResponseDao;
import com.resumainer.dao.GenerationResponsePersonalDao;
import com.resumainer.dto.generate.GenerationReviewDto;
import com.resumainer.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ResumeReviewService — GET review and SAVE review flows.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class ResumeReviewServiceTest {

    @Mock
    private GenerationRequestDao requestDao;

    @Mock
    private GenerationResponseDao responseDao;

    @Mock
    private GenerationResponsePersonalDao personalDao;

    @Mock
    private DataSource dataSource;

    private ResumeReviewService service;

    private final UUID requestId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID responseId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        service = new ResumeReviewService(requestDao, responseDao, personalDao, dataSource);

        // Default: request exists
        when(requestDao.findById(requestId, userId)).thenReturn(new ResumeGenerationRequest());
    }

    // ─── GET review tests ──────────────────────────────────────────────

    @Test
    void getReview_requestNotFound_throws() {
        when(requestDao.findById(requestId, userId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.getReview(requestId, userId));
    }

    @Test
    void getReview_noResponses_returnsEmptyLanguages() {
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of());

        GenerationReviewDto dto = service.getReview(requestId, userId);

        assertNotNull(dto);
        assertEquals(requestId, dto.getRequestId());
        assertTrue(dto.getLanguages().isEmpty());
    }

    @Test
    void getReview_singleResponse_buildsProfessionalPositioning() {
        ResumeGenerationResponse resp = createMinimalResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(resp));
        // No child records
        when(responseDao.findExperienceByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findCoursesByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findProjectsByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findSkillsByResponseId(responseId)).thenReturn(List.of());
        when(personalDao.findByResponseId(responseId)).thenReturn(null);

        GenerationReviewDto dto = service.getReview(requestId, userId);

        assertEquals(1, dto.getLanguages().size());
        assertEquals("EN", dto.getLanguages().get(0).getLanguageCode());

        // Should have professional_positioning, skills, personal_information (all non-child sections)
        List<GenerationReviewDto.SectionReviewGroup> sections = dto.getLanguages().get(0).getSections();
        assertTrue(sections.stream().anyMatch(s -> "professional_positioning".equals(s.getSectionKey())));
        assertTrue(sections.stream().anyMatch(s -> "skills".equals(s.getSectionKey())));
        assertTrue(sections.stream().anyMatch(s -> "personal_information".equals(s.getSectionKey())));
    }

    @Test
    void getReview_multipleLanguages_createsSeparateGroups() {
        ResumeGenerationResponse respEn = createMinimalResponse(1L, 1L);
        ResumeGenerationResponse respRu = createMinimalResponse(2L, 1L);
        respRu.setId(UUID.randomUUID());
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(respEn, respRu));

        GenerationReviewDto dto = service.getReview(requestId, userId);

        assertEquals(2, dto.getLanguages().size());
        assertEquals("EN", dto.getLanguages().get(0).getLanguageCode());
        assertEquals("RU", dto.getLanguages().get(1).getLanguageCode());
    }

    @Test
    void getReview_withCoverLetter_includesCoverLetterField() {
        ResumeGenerationResponse resp = createMinimalResponse(1L, 1L);
        resp.setCoverLetter("Dear hiring manager...");
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(resp));
        when(responseDao.findExperienceByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findCoursesByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findProjectsByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findSkillsByResponseId(responseId)).thenReturn(List.of());
        when(personalDao.findByResponseId(responseId)).thenReturn(null);

        GenerationReviewDto dto = service.getReview(requestId, userId);

        GenerationReviewDto.SectionReviewGroup pp = dto.getLanguages().get(0).getSections().stream()
                .filter(s -> "professional_positioning".equals(s.getSectionKey()))
                .findFirst().orElseThrow();
        assertTrue(pp.getRecords().get(0).getFieldVariants().containsKey("coverLetter"));
    }

    @Test
    void getReview_withoutCoverLetter_omitsCoverLetterField() {
        ResumeGenerationResponse resp = createMinimalResponse(1L, 1L);
        resp.setCoverLetter(null);
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(resp));
        when(responseDao.findExperienceByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findCoursesByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findProjectsByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findSkillsByResponseId(responseId)).thenReturn(List.of());
        when(personalDao.findByResponseId(responseId)).thenReturn(null);

        GenerationReviewDto dto = service.getReview(requestId, userId);

        GenerationReviewDto.SectionReviewGroup pp = dto.getLanguages().get(0).getSections().stream()
                .filter(s -> "professional_positioning".equals(s.getSectionKey()))
                .findFirst().orElseThrow();
        assertFalse(pp.getRecords().get(0).getFieldVariants().containsKey("coverLetter"));
    }

    @Test
    void getReview_withWorkExperience_includesRecords() {
        ResumeGenerationResponse resp = createMinimalResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(resp));
        GenerationResponseExperience exp = new GenerationResponseExperience();
        exp.setId(UUID.randomUUID());
        exp.setResponseId(responseId);
        exp.setJobTitle("Software Engineer");
        exp.setCompanyName("Tech Corp");
        exp.setDescription("Built stuff");
        exp.setOrderInResume(0);
        when(responseDao.findExperienceByResponseId(responseId)).thenReturn(List.of(exp));
        when(responseDao.findCoursesByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findProjectsByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findSkillsByResponseId(responseId)).thenReturn(List.of());
        when(personalDao.findByResponseId(responseId)).thenReturn(null);

        GenerationReviewDto dto = service.getReview(requestId, userId);

        List<GenerationReviewDto.SectionReviewGroup> sections = dto.getLanguages().get(0).getSections();
        assertTrue(sections.stream().anyMatch(s -> "work_experience".equals(s.getSectionKey())));
    }

    @Test
    void getReview_withSkills_includesGroupedSkills() {
        ResumeGenerationResponse resp = createMinimalResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(resp));
        when(responseDao.findExperienceByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findCoursesByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findProjectsByResponseId(responseId)).thenReturn(List.of());
        GenerationResponseSkill skill = new GenerationResponseSkill();
        skill.setSkillGroup("Technical");
        skill.setSkillName("Java");
        when(responseDao.findSkillsByResponseId(responseId)).thenReturn(List.of(skill));
        when(personalDao.findByResponseId(responseId)).thenReturn(null);

        GenerationReviewDto dto = service.getReview(requestId, userId);

        GenerationReviewDto.SectionReviewGroup skillsSection = dto.getLanguages().get(0).getSections().stream()
                .filter(s -> "skills".equals(s.getSectionKey()))
                .findFirst().orElseThrow();
        assertFalse(skillsSection.getRecords().isEmpty());
        assertTrue(skillsSection.getRecords().get(0).getFieldVariants().containsKey("groupName"));
        assertTrue(skillsSection.getRecords().get(0).getFieldVariants().containsKey("skills"));
    }

    @Test
    void getReview_withPersonalInfo_includesSection() {
        ResumeGenerationResponse resp = createMinimalResponse(1L, 1L);
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(resp));
        when(responseDao.findExperienceByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findCoursesByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findProjectsByResponseId(responseId)).thenReturn(List.of());
        when(responseDao.findSkillsByResponseId(responseId)).thenReturn(List.of());
        GenerationResponsePersonal personal = new GenerationResponsePersonal();
        personal.setId(UUID.randomUUID());
        personal.setLocation("New York");
        personal.setSpokenLanguages("English");
        personal.setWillingnessToRelocate("Yes");
        personal.setWillingnessForBusinessTrips("No");
        personal.setCitizenship("US");
        personal.setDateOfBirth(LocalDate.of(1990, 1, 15));
        personal.setWorkFormats("Remote");
        when(personalDao.findByResponseId(responseId)).thenReturn(personal);

        GenerationReviewDto dto = service.getReview(requestId, userId);

        GenerationReviewDto.SectionReviewGroup pi = dto.getLanguages().get(0).getSections().stream()
                .filter(s -> "personal_information".equals(s.getSectionKey()))
                .findFirst().orElseThrow();
        assertFalse(pi.getRecords().isEmpty());
        assertTrue(pi.getRecords().get(0).getFieldVariants().containsKey("location"));
        assertTrue(pi.getRecords().get(0).getFieldVariants().containsKey("workFormats"));
    }

    @Test
    void getReview_adaptationCode_mapsCorrectly() {
        ResumeGenerationResponse min = createMinimalResponse(1L, 1L);  // level 1 → MINIMAL
        ResumeGenerationResponse bal = createMinimalResponse(1L, 2L);  // level 2 → BALANCED
        bal.setId(UUID.randomUUID());
        ResumeGenerationResponse max = createMinimalResponse(1L, 3L);  // level 3 → MAXIMUM
        max.setId(UUID.randomUUID());
        when(responseDao.findResponsesByRequestId(requestId)).thenReturn(List.of(min, bal, max));

        GenerationReviewDto dto = service.getReview(requestId, userId);

        GenerationReviewDto.SectionReviewGroup pp = dto.getLanguages().get(0).getSections().stream()
                .filter(s -> "professional_positioning".equals(s.getSectionKey()))
                .findFirst().orElseThrow();
        // Each response creates a record
        assertEquals(3, pp.getRecords().size());
    }

    // ─── SAVE review tests ─────────────────────────────────────────────

    @Test
    void saveReview_requestNotFound_throws() {
        when(requestDao.findById(requestId, userId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.saveReview(requestId, userId, "professional_positioning:abc:professionalTitle:MINIMAL", "New Title"));
    }

    @Test
    void saveReview_invalidUpdateKeyFormat_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveReview(requestId, userId, "invalid", "value"));
    }

    @Test
    void saveReview_unknownSectionKey_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveReview(requestId, userId, "unknown_section:abc:field:MINIMAL", "value"));
    }

    @Test
    void saveReview_disallowedField_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveReview(requestId, userId,
                        "professional_positioning:" + responseId + ":invalidField:MINIMAL", "value"));
    }

    @Test
    void saveReview_forbiddenField_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveReview(requestId, userId,
                        "professional_positioning:" + responseId + ":status:MINIMAL", "value"));
    }

    @Test
    void saveReview_professionalPositioning_savesResponseField() {
        service.saveReview(requestId, userId,
                "professional_positioning:" + responseId + ":professionalTitle:MINIMAL", "New Title");

        verify(responseDao).updateResponseField(responseId, "professionalTitle", "New Title");
    }

    @Test
    void saveReview_workExperience_savesExperienceField() {
        UUID expId = UUID.randomUUID();
        service.saveReview(requestId, userId,
                "work_experience:" + expId + ":jobTitle:MINIMAL", "New Job Title");

        verify(responseDao).updateExperienceField(expId, "jobTitle", "New Job Title");
    }

    @Test
    void saveReview_courses_savesCourseField() {
        UUID courseId = UUID.randomUUID();
        service.saveReview(requestId, userId,
                "courses:" + courseId + ":courseName:MINIMAL", "New Course");

        verify(responseDao).updateCourseField(courseId, "courseName", "New Course");
    }

    @Test
    void saveReview_projects_savesProjectField() {
        UUID projectId = UUID.randomUUID();
        service.saveReview(requestId, userId,
                "projects:" + projectId + ":projectName:MINIMAL", "New Project");

        verify(responseDao).updateProjectField(projectId, "projectName", "New Project");
    }

    @Test
    void saveReview_skills_groupName_updatesGroupName() {
        UUID respId = UUID.randomUUID();
        GenerationResponseSkill skill = new GenerationResponseSkill();
        skill.setSkillGroup("Technical");
        skill.setSkillName("Java");
        when(responseDao.findSkillsByResponseId(respId)).thenReturn(List.of(skill));

        service.saveReview(requestId, userId,
                "skills:" + respId + ":groupName:MINIMAL:0", "Core Skills");

        verify(responseDao).updateSkillGroupName(respId, "Technical", "Core Skills");
    }

    @Test
    void saveReview_skills_groupIdxOutOfRange_throws() {
        UUID respId = UUID.randomUUID();
        when(responseDao.findSkillsByResponseId(respId)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.saveReview(requestId, userId,
                        "skills:" + respId + ":groupName:MINIMAL:0", "Core"));
    }

    @Test
    void saveReview_skills_skillsField_withJdbcTransaction() throws Exception {
        UUID respId = UUID.randomUUID();
        GenerationResponseSkill skill = new GenerationResponseSkill();
        skill.setSkillGroup("Technical");
        skill.setSkillName("Java");
        when(responseDao.findSkillsByResponseId(respId)).thenReturn(List.of(skill));

        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);

        service.saveReview(requestId, userId,
                "skills:" + respId + ":skills:MINIMAL:0", "Python, JavaScript");

        verify(conn).setAutoCommit(false);
        // Two skills → two insertSkill calls (plus one DELETE via stmt)
        verify(responseDao, times(2)).insertSkill(any(), eq(conn));
        verify(conn).commit();
    }

    @Test
    void saveReview_skills_skillsField_transactionRollsBackOnException() throws Exception {
        UUID respId = UUID.randomUUID();
        GenerationResponseSkill skill = new GenerationResponseSkill();
        skill.setSkillGroup("Technical");
        skill.setSkillName("Java");
        when(responseDao.findSkillsByResponseId(respId)).thenReturn(List.of(skill));

        Connection conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> service.saveReview(requestId, userId,
                        "skills:" + respId + ":skills:MINIMAL:0", "Python"));
    }

    @Test
    void saveReview_personalInfo_location_saves() throws Exception {
        UUID personalId = UUID.randomUUID();
        // Mock JDBC for personal info lookup
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getObject("id")).thenReturn(personalId);
        when(rs.getObject("response_id")).thenReturn(responseId);
        when(rs.getString("location")).thenReturn("Old City");
        when(rs.getString("spoken_languages")).thenReturn("English");
        when(rs.getString("willingness_to_relocate")).thenReturn("No");
        when(rs.getString("willingness_for_business_trips")).thenReturn("No");
        when(rs.getString("citizenship")).thenReturn("US");
        when(rs.getDate("date_of_birth")).thenReturn(java.sql.Date.valueOf(LocalDate.of(1990, 1, 15)));
        when(rs.getString("work_formats")).thenReturn("Office");
        when(rs.getString("gpa_grade")).thenReturn(null);
        when(rs.getInt("order_in_resume")).thenReturn(0);

        service.saveReview(requestId, userId,
                "personal_information:" + personalId + ":location:MINIMAL", "New York");

        verify(personalDao).update(any());
    }

    @Test
    void saveReview_personalInfo_invalidDateFormat_skipsUpdate() throws Exception {
        UUID personalId = UUID.randomUUID();
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getObject("id")).thenReturn(personalId);
        when(rs.getObject("response_id")).thenReturn(responseId);
        when(rs.getString("location")).thenReturn("Old City");
        when(rs.getString("spoken_languages")).thenReturn("English");
        when(rs.getString("willingness_to_relocate")).thenReturn("No");
        when(rs.getString("willingness_for_business_trips")).thenReturn("No");
        when(rs.getString("citizenship")).thenReturn("US");
        when(rs.getDate("date_of_birth")).thenReturn(java.sql.Date.valueOf(LocalDate.of(1990, 1, 15)));
        when(rs.getString("work_formats")).thenReturn("Office");
        when(rs.getString("gpa_grade")).thenReturn(null);
        when(rs.getInt("order_in_resume")).thenReturn(0);

        // Invalid date format — should not throw, should warn and skip
        service.saveReview(requestId, userId,
                "personal_information:" + personalId + ":dateOfBirth:MINIMAL", "not-a-date");

        // update should NOT be called because the date parsing failed before setting the value
        verify(personalDao, never()).update(any());
    }

    @Test
    void saveReview_personalInfo_unknownField_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveReview(requestId, userId,
                        "personal_information:" + UUID.randomUUID() + ":unknownField:MINIMAL", "value"));
    }

    // ─── Helper ────────────────────────────────────────────────────────

    private ResumeGenerationResponse createMinimalResponse(long languageId, long adaptationLevelId) {
        ResumeGenerationResponse resp = new ResumeGenerationResponse();
        resp.setId(responseId);
        resp.setGenerationRequestId(requestId);
        resp.setLanguageId(languageId);
        resp.setAdaptationLevelId(adaptationLevelId);
        resp.setStatusId(1L);
        resp.setProfessionalTitle("Software Engineer");
        resp.setValueLine("Building great software");
        resp.setProfessionalSummary("Experienced developer");
        resp.setProfessionalAspirations("Lead teams");
        resp.setCoverLetter(null);
        return resp;
    }

    // ─── Bullet point save tests (Feature 008) ──────────────────────────

    @Test
    void saveReview_experienceBullet_updatesBulletText() {
        UUID expId = UUID.randomUUID();
        String updateKey = "work_experience:" + expId + ":bulletPoints:0";

        service.saveReview(requestId, userId, updateKey, "Updated bullet text");

        verify(responseDao).updateExperienceBullet(expId, 0, "Updated bullet text");
    }

    @Test
    void saveReview_projectBullet_updatesBulletText() {
        UUID projId = UUID.randomUUID();
        String updateKey = "projects:" + projId + ":bulletPoints:2";

        service.saveReview(requestId, userId, updateKey, "New project bullet");

        verify(responseDao).updateProjectBullet(projId, 2, "New project bullet");
    }

    @Test
    void saveReview_bulletEmptyText_rejects() {
        UUID expId = UUID.randomUUID();
        String updateKey = "work_experience:" + expId + ":bulletPoints:0";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.saveReview(requestId, userId, updateKey, "   "));

        assertTrue(ex.getMessage().contains("Bullet point text cannot be empty"));
    }

    @Test
    void saveReview_bulletInvalidOrder_rejects() {
        UUID expId = UUID.randomUUID();
        String updateKey = "work_experience:" + expId + ":bulletPoints:abc";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.saveReview(requestId, userId, updateKey, "Some text"));

        assertTrue(ex.getMessage().contains("Invalid bullet order"));
    }
}
