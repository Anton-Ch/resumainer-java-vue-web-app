package com.resumainer.dao;

import com.resumainer.model.WorkExperience;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WorkExperienceDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private WorkExperienceDao dao;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        dao = new WorkExperienceDao(dataSource);
        userId = UUID.randomUUID();
    }

    @Test
    void findByUserId_returnsExperiences() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("job_title")).thenReturn("Senior Dev", "Junior Dev");
        when(resultSet.getString("company_name")).thenReturn("Company A", "Company B");
        when(resultSet.getString("description")).thenReturn("Led team", "Learned");
        when(resultSet.getString("location")).thenReturn("NYC", "Remote");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2023-01-01"), Date.valueOf("2020-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(null);
        when(resultSet.getBoolean("is_current")).thenReturn(true, false);
        when(resultSet.getString("company_url")).thenReturn("https://a.com", null);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getBoolean("is_deleted")).thenReturn(false);
        when(resultSet.getTimestamp("deleted_at")).thenReturn(null);

        List<WorkExperience> results = dao.findByUserId(userId);

        assertEquals(2, results.size());
        assertEquals("Senior Dev", results.get(0).getJobTitle());
        assertEquals("Company A", results.get(0).getCompanyName());
        assertTrue(results.get(0).isCurrent());
        assertEquals(LocalDate.of(2023, 1, 1), results.get(0).getStartDate());
        assertEquals("Junior Dev", results.get(1).getJobTitle());
        verify(statement).setObject(1, userId);
    }

    @Test
    void findByUserId_emptyList_returnsEmptyList() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<WorkExperience> results = dao.findByUserId(userId);

        assertTrue(results.isEmpty());
    }

    @Test
    void findById_found_returnsExperience() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("job_title")).thenReturn("Dev");
        when(resultSet.getString("company_name")).thenReturn("Co");
        when(resultSet.getString("description")).thenReturn("Work");
        when(resultSet.getString("location")).thenReturn("Loc");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2023-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(Date.valueOf("2024-01-01"));
        when(resultSet.getBoolean("is_current")).thenReturn(false);
        when(resultSet.getString("company_url")).thenReturn(null);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getBoolean("is_deleted")).thenReturn(false);
        when(resultSet.getTimestamp("deleted_at")).thenReturn(null);

        WorkExperience result = dao.findById(1L, userId);

        assertNotNull(result);
        assertEquals("Dev", result.getJobTitle());
        assertEquals(LocalDate.of(2024, 1, 1), result.getEndDate());
        verify(statement).setLong(1, 1L);
        verify(statement).setObject(2, userId);
    }

    @Test
    void findById_notFound_returnsNull() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        WorkExperience result = dao.findById(999L, userId);

        assertNull(result);
    }

    @Test
    void create_returnsExperienceWithId() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(42L);

        WorkExperience exp = new WorkExperience();
        exp.setUserId(userId);
        exp.setJobTitle("Dev");
        exp.setCompanyName("Co");
        exp.setDescription("Work");
        exp.setLocation("Loc");
        exp.setStartDate(LocalDate.of(2023, 1, 1));
        exp.setCurrent(true);

        WorkExperience result = dao.create(exp);

        assertEquals(42L, result.getId());
        verify(statement).setObject(1, userId);
        verify(statement).setString(2, "Dev");
    }

    @Test
    void update_existingRecord_succeeds() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        WorkExperience exp = new WorkExperience();
        exp.setId(1L);
        exp.setUserId(userId);
        exp.setJobTitle("Updated");
        exp.setCompanyName("Co");
        exp.setDescription("Desc");
        exp.setLocation("Loc");
        exp.setStartDate(LocalDate.of(2023, 1, 1));
        exp.setCurrent(false);

        dao.update(exp);

        verify(statement).setString(1, "Updated");
        verify(statement).setLong(9, 1L);
        verify(statement).setObject(10, userId);
    }

    @Test
    void update_notFound_throwsException() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        WorkExperience exp = new WorkExperience();
        exp.setId(999L);
        exp.setUserId(userId);
        exp.setJobTitle("Dev");
        exp.setCompanyName("Co");
        exp.setDescription("Desc");
        exp.setLocation("Loc");
        exp.setStartDate(LocalDate.of(2023, 1, 1));

        assertThrows(RuntimeException.class, () -> dao.update(exp));
    }

    @Test
    void softDelete_ownedByUser_returnsTrue() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        boolean deleted = dao.softDelete(1L, userId);

        assertTrue(deleted);
        verify(statement).setLong(1, 1L);
        verify(statement).setObject(2, userId);
    }

    @Test
    void softDelete_notOwned_returnsFalse() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        boolean deleted = dao.softDelete(999L, userId);

        assertFalse(deleted);
    }
}
