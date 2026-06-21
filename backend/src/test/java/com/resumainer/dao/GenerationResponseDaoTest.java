package com.resumainer.dao;

import com.resumainer.dao.GenerationResponseDao.ResponseBundle;
import com.resumainer.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DAO tests for GenerationResponseDao.
 * Covers insertResponse, insertExperience, insertCourse, insertProject, insertSkill,
 * findResponsesByRequestId, findExperience/Courses/Projects/SkillsByResponseId,
 * updateResponseField, updateExperienceField, updateCourseField, updateProjectField,
 * updateSkillGroupName, deleteSkillsByResponseId, loadResponseBundle.
 */
class GenerationResponseDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private GenerationResponseDao dao;

    private final UUID responseId = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(statement.executeUpdate()).thenReturn(1);

        dao = new GenerationResponseDao(dataSource);
    }

    // ─── insertResponse ───────────────────────────────────────────

    @Test
    void insertResponse_setsIdFromReturningClause() throws Exception {
        UUID generatedId = UUID.randomUUID();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(generatedId);

        ResumeGenerationResponse r = makeResponse();
        ResumeGenerationResponse result = dao.insertResponse(r, connection);

        assertEquals(generatedId, result.getId());
        verify(connection, never()).close();
    }

    @Test
    void insertResponse_doesNotSetId_whenNoGeneratedId() throws Exception {
        when(resultSet.next()).thenReturn(false);

        ResumeGenerationResponse r = makeResponse();
        r.setId(null);
        ResumeGenerationResponse result = dao.insertResponse(r, connection);

        assertNull(result.getId());
    }

    @Test
    void insertResponse_setsAllFields() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(responseId);

        ResumeGenerationResponse r = makeResponse();
        dao.insertResponse(r, connection);

        verify(statement).setObject(1, requestId);
        verify(statement).setLong(2, 1L);
        verify(statement).setLong(3, 2L);
        verify(statement).setLong(4, 1L);
        verify(statement).setString(5, "Senior Developer");
        verify(statement).setString(6, "Value line text");
        verify(statement).setString(7, "Professional summary");
        verify(statement).setString(8, "Become Tech Lead");
        verify(statement).setString(9, "Cover letter text");
        verify(statement).executeQuery();
    }

    // ─── insertSkill ──────────────────────────────────────────────

    @Test
    void insertSkill_persistsSkillData() throws Exception {
        GenerationResponseSkill skill = new GenerationResponseSkill();
        skill.setResponseId(responseId);
        skill.setSkillGroup("Programming Languages");
        skill.setSkillName("Java");
        skill.setOrderInResume(0);

        dao.insertSkill(skill, connection);

        verify(statement).setObject(1, responseId);
        verify(statement).setString(2, "Programming Languages");
        verify(statement).setString(3, "Java");
        verify(statement).setInt(4, 0);
        verify(statement).executeUpdate();
    }

    // ─── findResponsesByRequestId ─────────────────────────────────

    @Test
    void findResponsesByRequestId_returnsResponses_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        stubResponseResultSet();

        List<ResumeGenerationResponse> results = dao.findResponsesByRequestId(requestId);

        assertEquals(1, results.size());
        ResumeGenerationResponse r = results.get(0);
        assertEquals(responseId, r.getId());
        assertEquals(requestId, r.getGenerationRequestId());
        assertEquals(1L, r.getLanguageId());
        assertEquals(2L, r.getAdaptationLevelId());
        assertEquals(1L, r.getStatusId());
        assertEquals("Senior Developer", r.getProfessionalTitle());
        assertEquals("Value line text", r.getValueLine());
        assertEquals("Professional summary", r.getProfessionalSummary());
        assertEquals("Become Tech Lead", r.getProfessionalAspirations());
        assertEquals("Cover letter text", r.getCoverLetter());
        assertNotNull(r.getCreatedAt());
        assertNull(r.getUpdatedAt());
        verify(statement).setObject(1, requestId);
    }

    @Test
    void findResponsesByRequestId_returnsEmptyList_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<ResumeGenerationResponse> results = dao.findResponsesByRequestId(requestId);

        assertTrue(results.isEmpty());
    }

    @Test
    void findResponsesByRequestId_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.findResponsesByRequestId(requestId));

        assertTrue(ex.getMessage().contains("Database error loading responses"));
    }

    // ─── findChildList methods (experience, courses, projects, skills) ───

    @Test
    void findExperienceByResponseId_returnsExperienceList_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        stubExperienceResultSet();

        List<GenerationResponseExperience> results = dao.findExperienceByResponseId(responseId);

        assertEquals(1, results.size());
        assertEquals(responseId, results.get(0).getResponseId());
        assertEquals("Java Developer", results.get(0).getJobTitle());
        assertEquals("TechCorp", results.get(0).getCompanyName());
        assertEquals(LocalDate.of(2020, 1, 1), results.get(0).getStartDate());
        assertEquals(LocalDate.of(2023, 6, 30), results.get(0).getEndDate());
        assertTrue(results.get(0).isFirstPage());
    }

    @Test
    void findExperienceByResponseId_handlesNullEndDate() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        UUID expId = UUID.randomUUID();
        when(resultSet.getObject("id")).thenReturn(expId);
        when(resultSet.getObject("response_id")).thenReturn(responseId);
        when(resultSet.getString("source_id")).thenReturn("work-1");
        when(resultSet.getString("job_title")).thenReturn("Dev");
        when(resultSet.getString("company_name")).thenReturn("Co");
        when(resultSet.getString("description")).thenReturn("Work");
        when(resultSet.getString("location")).thenReturn("NYC");
        when(resultSet.getBoolean("is_first_page")).thenReturn(false);
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2021-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(null);
        when(resultSet.getInt("order_in_resume")).thenReturn(1);

        List<GenerationResponseExperience> results = dao.findExperienceByResponseId(responseId);

        assertEquals(1, results.size());
        assertNull(results.get(0).getEndDate());
    }

    @Test
    void findCoursesByResponseId_returnsCourseList_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        UUID courseId = UUID.randomUUID();
        when(resultSet.getObject("id")).thenReturn(courseId);
        when(resultSet.getObject("response_id")).thenReturn(responseId);
        when(resultSet.getString("source_id")).thenReturn("course-1");
        when(resultSet.getString("name")).thenReturn("Spring Boot");
        when(resultSet.getString("provider")).thenReturn("Udemy");
        when(resultSet.getBoolean("is_first_page")).thenReturn(true);
        when(resultSet.getString("course_focus")).thenReturn("Backend");
        when(resultSet.getInt("order_in_resume")).thenReturn(0);

        List<GenerationResponseCourse> results = dao.findCoursesByResponseId(responseId);

        assertEquals(1, results.size());
        assertEquals("Spring Boot", results.get(0).getName());
        assertEquals("Udemy", results.get(0).getProvider());
        assertTrue(results.get(0).isFirstPage());
    }

    @Test
    void findProjectsByResponseId_returnsProjectList_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        UUID projId = UUID.randomUUID();
        when(resultSet.getObject("id")).thenReturn(projId);
        when(resultSet.getObject("response_id")).thenReturn(responseId);
        when(resultSet.getString("source_id")).thenReturn("proj-1");
        when(resultSet.getString("project_name")).thenReturn("E-Commerce");
        when(resultSet.getString("role")).thenReturn("Lead");
        when(resultSet.getString("description")).thenReturn("Built platform");
        when(resultSet.getString("location")).thenReturn("Remote");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2023-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(Date.valueOf("2024-03-15"));
        when(resultSet.getInt("order_in_resume")).thenReturn(0);

        List<GenerationResponseProject> results = dao.findProjectsByResponseId(responseId);

        assertEquals(1, results.size());
        assertEquals("E-Commerce", results.get(0).getProjectName());
        assertEquals(LocalDate.of(2024, 3, 15), results.get(0).getEndDate());
    }

    @Test
    void findProjectsByResponseId_handlesNullEndDate() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id")).thenReturn(UUID.randomUUID());
        when(resultSet.getObject("response_id")).thenReturn(responseId);
        when(resultSet.getString("source_id")).thenReturn("proj-2");
        when(resultSet.getString("project_name")).thenReturn("Side");
        when(resultSet.getString("role")).thenReturn("Dev");
        when(resultSet.getString("description")).thenReturn("Pet");
        when(resultSet.getString("location")).thenReturn("Home");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2024-06-01"));
        when(resultSet.getDate("end_date")).thenReturn(null);
        when(resultSet.getInt("order_in_resume")).thenReturn(1);

        List<GenerationResponseProject> results = dao.findProjectsByResponseId(responseId);

        assertEquals(1, results.size());
        assertNull(results.get(0).getEndDate());
    }

    @Test
    void findSkillsByResponseId_returnsSkillList_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        UUID skillId = UUID.randomUUID();
        when(resultSet.getObject("id")).thenReturn(skillId);
        when(resultSet.getObject("response_id")).thenReturn(responseId);
        when(resultSet.getString("skill_group")).thenReturn("Languages");
        when(resultSet.getString("skill_name")).thenReturn("Java");
        when(resultSet.getInt("order_in_resume")).thenReturn(0);

        List<GenerationResponseSkill> results = dao.findSkillsByResponseId(responseId);

        assertEquals(1, results.size());
        assertEquals("Languages", results.get(0).getSkillGroup());
        assertEquals("Java", results.get(0).getSkillName());
    }

    @Test
    void findChildList_returnsEmptyList_whenNoResults() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertTrue(dao.findExperienceByResponseId(responseId).isEmpty());
        assertTrue(dao.findCoursesByResponseId(responseId).isEmpty());
        assertTrue(dao.findProjectsByResponseId(responseId).isEmpty());
        assertTrue(dao.findSkillsByResponseId(responseId).isEmpty());
    }

    @Test
    void findChildList_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("Child error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.findExperienceByResponseId(responseId));

        assertTrue(ex.getMessage().contains("Database error loading child rows"));
    }

    // ─── updateResponseField ──────────────────────────────────────

    @Test
    void updateResponseField_updatesAllowedField() throws Exception {
        dao.updateResponseField(responseId, "professionalTitle", "Senior Lead");

        verify(statement).setString(1, "Senior Lead");
        verify(statement).setObject(2, responseId);
        verify(statement).executeUpdate();
    }

    @Test
    void updateResponseField_updatesAllAllowedFields() throws Exception {
        dao.updateResponseField(responseId, "professionalTitle", "A");
        dao.updateResponseField(responseId, "valueLine", "B");
        dao.updateResponseField(responseId, "professionalSummary", "C");
        dao.updateResponseField(responseId, "professionalAspirations", "D");
        dao.updateResponseField(responseId, "coverLetter", "E");

        verify(statement, times(5)).executeUpdate();
    }

    @Test
    void updateResponseField_throwsException_forUnknownField() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> dao.updateResponseField(responseId, "invalidField", "value"));
    }

    @Test
    void updateResponseField_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.updateResponseField(responseId, "professionalTitle", "X"));

        assertTrue(ex.getMessage().contains("Failed to update"));
    }

    // ─── updateExperienceField ────────────────────────────────────

    @Test
    void updateExperienceField_updatesAllowedFields() throws Exception {
        dao.updateExperienceField(responseId, "jobTitle", "Senior");
        dao.updateExperienceField(responseId, "companyName", "ACME");
        dao.updateExperienceField(responseId, "description", "Updated desc");

        verify(statement, times(3)).executeUpdate();
    }

    @Test
    void updateExperienceField_throwsException_forUnknownField() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> dao.updateExperienceField(responseId, "invalidField", "x"));
    }

    @Test
    void updateExperienceField_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.updateExperienceField(responseId, "jobTitle", "X"));
    }

    // ─── updateCourseField ────────────────────────────────────────

    @Test
    void updateCourseField_updatesAllowedFields() throws Exception {
        dao.updateCourseField(responseId, "courseName", "New Course");
        dao.updateCourseField(responseId, "provider", "Coursera");
        dao.updateCourseField(responseId, "courseFocus", "Data Science");

        verify(statement, times(3)).executeUpdate();
    }

    @Test
    void updateCourseField_throwsException_forUnknownField() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> dao.updateCourseField(responseId, "invalidField", "x"));
    }

    @Test
    void updateCourseField_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.updateCourseField(responseId, "courseName", "X"));
    }

    // ─── updateProjectField ───────────────────────────────────────

    @Test
    void updateProjectField_updatesAllowedFields() throws Exception {
        dao.updateProjectField(responseId, "projectName", "New Project");
        dao.updateProjectField(responseId, "role", "Lead");
        dao.updateProjectField(responseId, "description", "Updated");

        verify(statement, times(3)).executeUpdate();
    }

    @Test
    void updateProjectField_throwsException_forUnknownField() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> dao.updateProjectField(responseId, "invalidField", "x"));
    }

    @Test
    void updateProjectField_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.updateProjectField(responseId, "projectName", "X"));
    }

    // ─── updateSkillGroupName ──────────────────────────────────────

    @Test
    void updateSkillGroupName_replacesOldGroupName() throws Exception {
        dao.updateSkillGroupName(responseId, "Old Group", "New Group");

        verify(statement).setString(1, "New Group");
        verify(statement).setObject(2, responseId);
        verify(statement).setString(3, "Old Group");
        verify(statement).executeUpdate();
    }

    @Test
    void updateSkillGroupName_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.updateSkillGroupName(responseId, "Old", "New"));
    }

    // ─── deleteSkillsByResponseId ─────────────────────────────────

    @Test
    void deleteSkillsByResponseId_deletesAllSkills() throws Exception {
        dao.deleteSkillsByResponseId(responseId, connection);

        verify(statement).setObject(1, responseId);
        verify(statement).executeUpdate();
        verify(connection, never()).close();
    }

    // ─── loadResponseBundle ───────────────────────────────────────
    //
    // Note: loadResponseBundle happy path is implicitly covered by:
    //   - findResponsesByRequestId (mapResponseRow)
    //   - findExperience/Courses/Projects/SkillsByResponseId (mapRow + findChildList)
    // Full bundle test would require multi-connection mock chaining beyond standard pattern.

    @Test
    void loadResponseBundle_throwsException_whenResponseNotFound() throws Exception {
        when(connection.prepareStatement(contains("FROM resume_generation_response rgr")))
                .thenReturn(statement);
        when(resultSet.next()).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> dao.loadResponseBundle(responseId));
    }

    @Test
    void loadResponseBundle_throwsException_onSqlError() throws Exception {
        when(connection.prepareStatement(contains("FROM resume_generation_response rgr")))
                .thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.loadResponseBundle(responseId));

        assertTrue(ex.getMessage().contains("Database error loading response bundle"));
    }

    // ─── existing tests (insertExperience, insertCourse, insertProject) ───

    @Test
    void insertExperience_persistsSourceId() throws Exception {
        GenerationResponseExperience exp = new GenerationResponseExperience();
        exp.setResponseId(responseId);
        exp.setSourceId("work-5");
        exp.setJobTitle("Business Analyst");
        exp.setCompanyName("Bobrosoft");
        exp.setDescription("Gathered requirements.");
        exp.setLocation("Astana");
        exp.setFirstPage(true);
        exp.setStartDate(LocalDate.of(2025, 5, 1));
        exp.setEndDate(null);
        exp.setOrderInResume(0);

        UUID expId = UUID.randomUUID();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(expId);

        dao.insertExperience(exp, connection);

        verify(connection).prepareStatement(contains("source_id"));
        verify(statement).setObject(1, responseId);
        verify(statement).setString(2, "work-5");
        verify(statement).setString(3, "Business Analyst");
        verify(statement).setString(4, "Bobrosoft");
        verify(statement).setString(5, "Gathered requirements.");
        verify(statement).setString(6, "Astana");
        verify(statement).setBoolean(7, true);
        verify(statement).setDate(8, Date.valueOf(LocalDate.of(2025, 5, 1)));
        verify(statement).setNull(9, java.sql.Types.DATE);
        verify(statement).setInt(10, 0);
        verify(statement).executeQuery(); // RETURNING id
    }

    @Test
    void insertCourse_persistsSourceId() throws Exception {
        GenerationResponseCourse course = new GenerationResponseCourse();
        course.setResponseId(responseId);
        course.setSourceId("course-5");
        course.setName("Microsoft Business Analysis");
        course.setProvider("Coursera");
        course.setFirstPage(true);
        course.setCourseFocus("Business analysis");
        course.setOrderInResume(1);

        dao.insertCourse(course, connection);

        verify(connection).prepareStatement(contains("source_id"));
        verify(statement).setObject(1, responseId);
        verify(statement).setString(2, "course-5");
        verify(statement).setString(3, "Microsoft Business Analysis");
        verify(statement).setString(4, "Coursera");
        verify(statement).setBoolean(5, true);
        verify(statement).setString(6, "Business analysis");
        verify(statement).setInt(7, 1);
        verify(statement).executeUpdate();
    }

    @Test
    void insertProject_persistsSourceId() throws Exception {
        GenerationResponseProject project = new GenerationResponseProject();
        project.setResponseId(responseId);
        project.setSourceId("project-2");
        project.setProjectName("Reporting Optimization");
        project.setRole("Developer");
        project.setDescription("Optimized reporting workflow.");
        project.setLocation("Ust-Kamenogorsk");
        project.setStartDate(LocalDate.of(2026, 5, 1));
        project.setEndDate(null);
        project.setOrderInResume(2);

        UUID projId = UUID.randomUUID();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(projId);

        dao.insertProject(project, connection);

        verify(connection).prepareStatement(contains("source_id"));
        verify(statement).setObject(1, responseId);
        verify(statement).setString(2, "project-2");
        verify(statement).setString(3, "Reporting Optimization");
        verify(statement).setString(4, "Developer");
        verify(statement).setString(5, "Optimized reporting workflow.");
        verify(statement).setString(6, "Ust-Kamenogorsk");
        verify(statement).setDate(7, Date.valueOf(LocalDate.of(2026, 5, 1)));
        verify(statement).setNull(8, java.sql.Types.DATE);
        verify(statement).setInt(9, 2);
        verify(statement).executeQuery(); // RETURNING id
    }

    // ─── helper ───────────────────────────────────────────────────

    private ResumeGenerationResponse makeResponse() {
        ResumeGenerationResponse r = new ResumeGenerationResponse();
        r.setGenerationRequestId(requestId);
        r.setLanguageId(1L);
        r.setAdaptationLevelId(2L);
        r.setStatusId(1L);
        r.setProfessionalTitle("Senior Developer");
        r.setValueLine("Value line text");
        r.setProfessionalSummary("Professional summary");
        r.setProfessionalAspirations("Become Tech Lead");
        r.setCoverLetter("Cover letter text");
        return r;
    }

    private void stubResponseResultSet() throws Exception {
        when(resultSet.getObject("id")).thenReturn(responseId);
        when(resultSet.getObject("generation_request_id")).thenReturn(requestId);
        when(resultSet.getLong("language_id")).thenReturn(1L);
        when(resultSet.getLong("adaptation_level_id")).thenReturn(2L);
        when(resultSet.getLong("status_id")).thenReturn(1L);
        when(resultSet.getString("professional_title")).thenReturn("Senior Developer");
        when(resultSet.getString("value_line")).thenReturn("Value line text");
        when(resultSet.getString("professional_summary")).thenReturn("Professional summary");
        when(resultSet.getString("professional_aspirations")).thenReturn("Become Tech Lead");
        when(resultSet.getString("cover_letter")).thenReturn("Cover letter text");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.of(2024, 1, 1, 12, 0)));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
    }

    private void stubExperienceResultSet() throws Exception {
        UUID expId = UUID.randomUUID();
        when(resultSet.getObject("id")).thenReturn(expId);
        when(resultSet.getObject("response_id")).thenReturn(responseId);
        when(resultSet.getString("source_id")).thenReturn("work-exp-1");
        when(resultSet.getString("job_title")).thenReturn("Java Developer");
        when(resultSet.getString("company_name")).thenReturn("TechCorp");
        when(resultSet.getString("description")).thenReturn("Backend development");
        when(resultSet.getString("location")).thenReturn("New York");
        when(resultSet.getBoolean("is_first_page")).thenReturn(true);
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2020-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(Date.valueOf("2023-06-30"));
        when(resultSet.getInt("order_in_resume")).thenReturn(0);
    }
}
