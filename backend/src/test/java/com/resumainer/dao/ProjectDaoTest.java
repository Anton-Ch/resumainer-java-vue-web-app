package com.resumainer.dao;

import com.resumainer.model.Project;
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

class ProjectDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private ProjectDao dao;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        dao = new ProjectDao(dataSource);
        userId = UUID.randomUUID();
    }

    @Test
    void findByUserId_returnsProjects() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("project_name")).thenReturn("E-Commerce App");
        when(resultSet.getString("role")).thenReturn("Lead Dev");
        when(resultSet.getString("description")).thenReturn("Built platform");
        when(resultSet.getString("location")).thenReturn("Remote");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2024-01-01"));
        when(resultSet.getDate("end_date")).thenReturn(null);
        when(resultSet.getBoolean("is_ongoing")).thenReturn(true);
        when(resultSet.getString("project_url")).thenReturn("https://github.com/proj");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getBoolean("is_deleted")).thenReturn(false);
        when(resultSet.getTimestamp("deleted_at")).thenReturn(null);

        List<Project> results = dao.findByUserId(userId);

        assertEquals(1, results.size());
        assertEquals("E-Commerce App", results.get(0).getProjectName());
        assertEquals("Lead Dev", results.get(0).getRole());
        assertTrue(results.get(0).isOngoing());
    }

    @Test
    void findByUserId_empty_returnsEmptyList() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(dao.findByUserId(userId).isEmpty());
    }

    @Test
    void findById_found_returnsProject() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("project_name")).thenReturn("App");
        when(resultSet.getString("description")).thenReturn("Built");
        when(resultSet.getString("location")).thenReturn("Remote");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2024-01-01"));
        when(resultSet.getBoolean("is_ongoing")).thenReturn(false);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getBoolean("is_deleted")).thenReturn(false);
        when(resultSet.getTimestamp("deleted_at")).thenReturn(null);

        Project result = dao.findById(1L, userId);

        assertNotNull(result);
        assertEquals("App", result.getProjectName());
    }

    @Test
    void findById_notFound_returnsNull() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertNull(dao.findById(999L, userId));
    }

    @Test
    void create_returnsWithId() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(42L);

        Project p = new Project();
        p.setUserId(userId);
        p.setProjectName("App");
        p.setDescription("Built");
        p.setLocation("Remote");
        p.setStartDate(LocalDate.of(2024, 1, 1));

        Project result = dao.create(p);

        assertEquals(42L, result.getId());
        verify(statement).setObject(1, userId);
        verify(statement).setString(2, "App");
    }

    @Test
    void update_existing_succeeds() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        Project p = new Project();
        p.setId(1L);
        p.setUserId(userId);
        p.setProjectName("Updated");
        p.setDescription("Desc");
        p.setLocation("Loc");
        p.setStartDate(LocalDate.of(2024, 1, 1));

        dao.update(p);

        verify(statement).setString(1, "Updated");
        verify(statement).setLong(9, 1L);
    }

    @Test
    void update_notFound_throwsException() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        Project p = new Project();
        p.setId(999L);
        p.setUserId(userId);
        p.setProjectName("X");
        p.setDescription("X");
        p.setLocation("X");
        p.setStartDate(LocalDate.now());

        assertThrows(RuntimeException.class, () -> dao.update(p));
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
