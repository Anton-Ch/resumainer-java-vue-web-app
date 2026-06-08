package com.resumainer.dao;

import com.resumainer.model.Education;
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

class EducationDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private EducationDao dao;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        dao = new EducationDao(dataSource);
        userId = UUID.randomUUID();
    }

    @Test
    void findByUserId_returnsEducations() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("institution_name")).thenReturn("MIT");
        when(resultSet.getString("degree")).thenReturn("BS");
        when(resultSet.getString("field_of_study")).thenReturn("CS");
        when(resultSet.getString("description")).thenReturn("Great");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2019-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(Date.valueOf("2023-01-01"));
        when(resultSet.getBoolean("is_current")).thenReturn(false);
        when(resultSet.getString("location")).thenReturn("Cambridge");
        when(resultSet.getString("gpa_grade")).thenReturn("3.9");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getBoolean("is_deleted")).thenReturn(false);
        when(resultSet.getTimestamp("deleted_at")).thenReturn(null);

        List<Education> results = dao.findByUserId(userId);

        assertEquals(1, results.size());
        assertEquals("MIT", results.get(0).getInstitutionName());
        assertEquals("BS", results.get(0).getDegree());
        assertEquals("3.9", results.get(0).getGpaGrade());
    }

    @Test
    void findByUserId_empty_returnsEmptyList() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(dao.findByUserId(userId).isEmpty());
    }

    @Test
    void findById_found_returnsEducation() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("institution_name")).thenReturn("MIT");
        when(resultSet.getString("degree")).thenReturn("BS");
        when(resultSet.getString("field_of_study")).thenReturn("CS");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2019-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(null);
        when(resultSet.getBoolean("is_current")).thenReturn(true);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2019-01-01 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getBoolean("is_deleted")).thenReturn(false);
        when(resultSet.getTimestamp("deleted_at")).thenReturn(null);

        Education result = dao.findById(1L, userId);

        assertNotNull(result);
        assertEquals("MIT", result.getInstitutionName());
        assertTrue(result.isCurrent());
        assertNull(result.getEndDate());
    }

    @Test
    void create_returnsWithId() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(42L);

        Education edu = new Education();
        edu.setUserId(userId);
        edu.setInstitutionName("MIT");
        edu.setDegree("BS");
        edu.setFieldOfStudy("CS");
        edu.setStartDate(LocalDate.of(2019, 1, 1));

        Education result = dao.create(edu);

        assertEquals(42L, result.getId());
        verify(statement).setObject(1, userId);
        verify(statement).setString(2, "MIT");
    }

    @Test
    void update_existing_succeeds() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        Education edu = new Education();
        edu.setId(1L);
        edu.setUserId(userId);
        edu.setInstitutionName("Updated");
        edu.setDegree("PhD");
        edu.setFieldOfStudy("Physics");
        edu.setStartDate(LocalDate.of(2020, 1, 1));

        dao.update(edu);

        verify(statement).setString(1, "Updated");
        verify(statement).setLong(10, 1L);
    }

    @Test
    void update_notFound_throwsException() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        Education edu = new Education();
        edu.setId(999L);
        edu.setUserId(userId);
        edu.setInstitutionName("X");
        edu.setDegree("X");
        edu.setFieldOfStudy("X");
        edu.setStartDate(LocalDate.now());

        assertThrows(RuntimeException.class, () -> dao.update(edu));
    }

    @Test
    void softDelete_owned_returnsTrue() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        assertTrue(dao.softDelete(1L, userId));
    }

    @Test
    void softDelete_notOwned_returnsFalse() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        assertFalse(dao.softDelete(999L, userId));
    }
}
