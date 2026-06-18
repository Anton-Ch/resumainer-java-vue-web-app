package com.resumainer.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ProfilePromptDao.
 * Covers all 7 public load* methods: loadContact, loadAdditionalInfo,
 * loadWorkFormats, loadWorkExperience, loadCourses, loadProjects, loadEducation.
 */
class ProfilePromptDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private ProfilePromptDao dao;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        dao = new ProfilePromptDao(dataSource);
    }

    // ─── loadContact ──────────────────────────────────────────────

    @Test
    void loadContact_returnsContactMap_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("full_name")).thenReturn("John Doe");
        when(resultSet.getString("phone")).thenReturn("+1234567890");
        when(resultSet.getString("resume_email")).thenReturn("john@example.com");
        when(resultSet.getString("location")).thenReturn("New York");
        when(resultSet.getString("professional_title")).thenReturn("Senior Developer");
        when(resultSet.getString("linkedin_url")).thenReturn("https://linkedin.com/in/johndoe");
        when(resultSet.getString("portfolio_url")).thenReturn("https://portfolio.dev");
        when(resultSet.getString("telegram")).thenReturn("@johndoe");
        when(resultSet.getString("whatsapp")).thenReturn("+1234567890");

        Map<String, Object> result = dao.loadContact(userId);

        assertEquals(9, result.size());
        assertEquals("John Doe", result.get("fullName"));
        assertEquals("+1234567890", result.get("phone"));
        assertEquals("john@example.com", result.get("resumeEmail"));
        assertEquals("Senior Developer", result.get("professionalTitle"));
        verify(statement).setObject(1, userId);
    }

    @Test
    void loadContact_returnsEmptyMap_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        Map<String, Object> result = dao.loadContact(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void loadContact_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.loadContact(userId));

        assertTrue(ex.getMessage().contains("Database error loading contact"));
    }

    // ─── loadAdditionalInfo ───────────────────────────────────────

    @Test
    void loadAdditionalInfo_returnsInfoMap_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("skills")).thenReturn("Java, Spring");
        when(resultSet.getString("languages")).thenReturn("English, Russian");
        when(resultSet.getString("professional_aspirations")).thenReturn("Become Tech Lead");
        when(resultSet.getString("achievements")).thenReturn("Team of the Year");
        when(resultSet.getString("general_information")).thenReturn("Open to offers");
        when(resultSet.getString("ready_for_relocation")).thenReturn("Yes");
        when(resultSet.getString("ready_for_business_trips")).thenReturn("Yes");
        when(resultSet.getString("citizenship")).thenReturn("US");
        when(resultSet.getDate("date_of_birth")).thenReturn(Date.valueOf("1990-01-15"));

        Map<String, Object> result = dao.loadAdditionalInfo(userId);

        assertEquals(9, result.size());
        assertEquals("Java, Spring", result.get("skills"));
        assertEquals("1990-01-15", result.get("dateOfBirth"));
        verify(statement).setObject(1, userId);
    }

    @Test
    void loadAdditionalInfo_returnsEmptyMap_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        Map<String, Object> result = dao.loadAdditionalInfo(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void loadAdditionalInfo_returnsNullDateOfBirth_whenDateIsNull() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("skills")).thenReturn("Java");
        when(resultSet.getString("languages")).thenReturn("English");
        when(resultSet.getString("professional_aspirations")).thenReturn("Lead");
        when(resultSet.getString("achievements")).thenReturn("Award");
        when(resultSet.getString("general_information")).thenReturn("Info");
        when(resultSet.getString("ready_for_relocation")).thenReturn("No");
        when(resultSet.getString("ready_for_business_trips")).thenReturn("No");
        when(resultSet.getString("citizenship")).thenReturn("US");
        when(resultSet.getDate("date_of_birth")).thenReturn(null);

        Map<String, Object> result = dao.loadAdditionalInfo(userId);

        assertNull(result.get("dateOfBirth"));
    }

    @Test
    void loadAdditionalInfo_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.loadAdditionalInfo(userId));

        assertTrue(ex.getMessage().contains("Database error loading additional info"));
    }

    // ─── loadWorkFormats ──────────────────────────────────────────

    @Test
    void loadWorkFormats_returnsList_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("code")).thenReturn("REMOTE", "OFFICE");
        when(resultSet.getString("name")).thenReturn("Remote", "Office");

        List<Map<String, Object>> result = dao.loadWorkFormats(userId);

        assertEquals(2, result.size());
        assertEquals("REMOTE", result.get(0).get("code"));
        assertEquals("Office", result.get(1).get("name"));
        verify(statement).setObject(1, userId);
    }

    @Test
    void loadWorkFormats_returnsEmptyList_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<Map<String, Object>> result = dao.loadWorkFormats(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void loadWorkFormats_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.loadWorkFormats(userId));

        assertTrue(ex.getMessage().contains("Database error loading work formats"));
    }

    // ─── loadWorkExperience ───────────────────────────────────────

    @Test
    void loadWorkExperience_returnsExperienceList_whenFound() throws Exception {
        UUID expId = UUID.randomUUID();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id")).thenReturn(expId);
        when(resultSet.getString("job_title")).thenReturn("Java Developer");
        when(resultSet.getString("company_name")).thenReturn("TechCorp");
        when(resultSet.getString("description")).thenReturn("Backend development");
        when(resultSet.getString("location")).thenReturn("New York");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2020-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(Date.valueOf("2023-06-30"));
        when(resultSet.getBoolean("is_current")).thenReturn(false);

        List<Map<String, Object>> result = dao.loadWorkExperience(userId);

        assertEquals(1, result.size());
        Map<String, Object> exp = result.get(0);
        assertEquals(expId.toString(), exp.get("id"));
        assertEquals("Java Developer", exp.get("jobTitle"));
        assertEquals("TechCorp", exp.get("companyName"));
        assertEquals("2020-01-01", exp.get("startDate"));
        assertEquals("2023-06-30", exp.get("endDate"));
        assertFalse((Boolean) exp.get("isCurrent"));
        verify(statement).setObject(1, userId);
    }

    @Test
    void loadWorkExperience_handlesNullEndDateAndCurrent() throws Exception {
        UUID expId = UUID.randomUUID();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id")).thenReturn(expId);
        when(resultSet.getString("job_title")).thenReturn("Developer");
        when(resultSet.getString("company_name")).thenReturn("Startup");
        when(resultSet.getString("description")).thenReturn("Full stack");
        when(resultSet.getString("location")).thenReturn("Remote");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2024-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(null);
        when(resultSet.getBoolean("is_current")).thenReturn(true);

        List<Map<String, Object>> result = dao.loadWorkExperience(userId);

        assertEquals(1, result.size());
        assertNull(result.get(0).get("endDate"));
        assertTrue((Boolean) result.get(0).get("isCurrent"));
    }

    @Test
    void loadWorkExperience_returnsEmptyList_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<Map<String, Object>> result = dao.loadWorkExperience(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void loadWorkExperience_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.loadWorkExperience(userId));

        assertTrue(ex.getMessage().contains("Database error loading work experience"));
    }

    // ─── loadCourses ──────────────────────────────────────────────

    @Test
    void loadCourses_returnsCourseList_whenFound() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id")).thenReturn(courseId);
        when(resultSet.getString("name")).thenReturn("Spring Boot Advanced");
        when(resultSet.getString("provider")).thenReturn("Udemy");
        when(resultSet.getString("description")).thenReturn("Deep dive into Spring");
        when(resultSet.getString("course_focus")).thenReturn("Backend");

        List<Map<String, Object>> result = dao.loadCourses(userId);

        assertEquals(1, result.size());
        Map<String, Object> course = result.get(0);
        assertEquals(courseId.toString(), course.get("id"));
        assertEquals("Spring Boot Advanced", course.get("name"));
        assertEquals("Udemy", course.get("provider"));
        assertEquals("Backend", course.get("courseFocus"));
        verify(statement).setObject(1, userId);
    }

    @Test
    void loadCourses_returnsEmptyList_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<Map<String, Object>> result = dao.loadCourses(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void loadCourses_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.loadCourses(userId));

        assertTrue(ex.getMessage().contains("Database error loading courses"));
    }

    // ─── loadProjects ─────────────────────────────────────────────

    @Test
    void loadProjects_returnsProjectList_whenFound() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id")).thenReturn(projectId);
        when(resultSet.getString("project_name")).thenReturn("E-Commerce Platform");
        when(resultSet.getString("role")).thenReturn("Lead Developer");
        when(resultSet.getString("description")).thenReturn("Built a scalable platform");
        when(resultSet.getString("location")).thenReturn("Remote");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2023-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(Date.valueOf("2024-03-15"));
        when(resultSet.getBoolean("is_ongoing")).thenReturn(false);

        List<Map<String, Object>> result = dao.loadProjects(userId);

        assertEquals(1, result.size());
        Map<String, Object> prj = result.get(0);
        assertEquals(projectId.toString(), prj.get("id"));
        assertEquals("E-Commerce Platform", prj.get("projectName"));
        assertEquals("Lead Developer", prj.get("role"));
        assertEquals("2023-01-01", prj.get("startDate"));
        assertEquals("2024-03-15", prj.get("endDate"));
        assertFalse((Boolean) prj.get("isOngoing"));
        verify(statement).setObject(1, userId);
    }

    @Test
    void loadProjects_handlesNullEndDateAndOngoing() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id")).thenReturn(projectId);
        when(resultSet.getString("project_name")).thenReturn("Side Project");
        when(resultSet.getString("role")).thenReturn("Developer");
        when(resultSet.getString("description")).thenReturn("Pet project");
        when(resultSet.getString("location")).thenReturn("Home");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2024-06-01"));
        when(resultSet.getDate("end_date")).thenReturn(null);
        when(resultSet.getBoolean("is_ongoing")).thenReturn(true);

        List<Map<String, Object>> result = dao.loadProjects(userId);

        assertEquals(1, result.size());
        assertNull(result.get(0).get("endDate"));
        assertTrue((Boolean) result.get(0).get("isOngoing"));
    }

    @Test
    void loadProjects_returnsEmptyList_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<Map<String, Object>> result = dao.loadProjects(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void loadProjects_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.loadProjects(userId));

        assertTrue(ex.getMessage().contains("Database error loading projects"));
    }

    // ─── loadEducation ────────────────────────────────────────────

    @Test
    void loadEducation_returnsEducationList_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("institution_name_ru")).thenReturn("МГУ");
        when(resultSet.getString("institution_name_en")).thenReturn("MSU");
        when(resultSet.getString("degree_ru")).thenReturn("Бакалавр");
        when(resultSet.getString("degree_en")).thenReturn("Bachelor");
        when(resultSet.getString("field_of_study_ru")).thenReturn("Информатика");
        when(resultSet.getString("field_of_study_en")).thenReturn("Computer Science");

        List<Map<String, Object>> result = dao.loadEducation(userId);

        assertEquals(1, result.size());
        Map<String, Object> edu = result.get(0);
        assertEquals("МГУ", edu.get("institutionNameRu"));
        assertEquals("MSU", edu.get("institutionNameEn"));
        assertEquals("Бакалавр", edu.get("degreeRu"));
        assertEquals("Bachelor", edu.get("degreeEn"));
        assertEquals("Информатика", edu.get("fieldOfStudyRu"));
        assertEquals("Computer Science", edu.get("fieldOfStudyEn"));
        verify(statement).setObject(1, userId);
    }

    @Test
    void loadEducation_returnsMultipleRecords() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("institution_name_ru")).thenReturn("МГУ", "СПбГУ");
        when(resultSet.getString("institution_name_en")).thenReturn("MSU", "SPbSU");
        when(resultSet.getString("degree_ru")).thenReturn("Бакалавр", "Магистр");
        when(resultSet.getString("degree_en")).thenReturn("Bachelor", "Master");
        when(resultSet.getString("field_of_study_ru")).thenReturn("Информатика", "Математика");
        when(resultSet.getString("field_of_study_en")).thenReturn("CS", "Math");

        List<Map<String, Object>> result = dao.loadEducation(userId);

        assertEquals(2, result.size());
        assertEquals("МГУ", result.get(0).get("institutionNameRu"));
        assertEquals("СПбГУ", result.get(1).get("institutionNameRu"));
    }

    @Test
    void loadEducation_handlesNullBilingualFields() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("institution_name_ru")).thenReturn(null);
        when(resultSet.getString("institution_name_en")).thenReturn(null);
        when(resultSet.getString("degree_ru")).thenReturn(null);
        when(resultSet.getString("degree_en")).thenReturn(null);
        when(resultSet.getString("field_of_study_ru")).thenReturn(null);
        when(resultSet.getString("field_of_study_en")).thenReturn(null);

        List<Map<String, Object>> result = dao.loadEducation(userId);

        assertEquals(1, result.size());
        assertNull(result.get(0).get("institutionNameRu"));
        assertNull(result.get(0).get("institutionNameEn"));
    }

    @Test
    void loadEducation_returnsEmptyList_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<Map<String, Object>> result = dao.loadEducation(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void loadEducation_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.loadEducation(userId));

        assertTrue(ex.getMessage().contains("Database error loading education"));
    }
}
