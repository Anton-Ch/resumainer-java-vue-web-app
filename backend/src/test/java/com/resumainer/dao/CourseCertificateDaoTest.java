package com.resumainer.dao;

import com.resumainer.model.CourseCertificate;
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

class CourseCertificateDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private CourseCertificateDao dao;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        dao = new CourseCertificateDao(dataSource);
        userId = UUID.randomUUID();
    }

    @Test
    void findByUserId_withPagination_returnsPage() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("name")).thenReturn("AWS Course");
        when(resultSet.getString("provider")).thenReturn("Coursera");
        when(resultSet.getString("description")).thenReturn("Cloud course");
        when(resultSet.getString("course_focus")).thenReturn("AWS, Cloud");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2025-01-15"));
        when(resultSet.getDate("end_date")).thenReturn(Date.valueOf("2025-06-20"));
        when(resultSet.getString("credential_url")).thenReturn("https://coursera.org/verify/abc");
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-01-15 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getBoolean("is_deleted")).thenReturn(false);
        when(resultSet.getTimestamp("deleted_at")).thenReturn(null);

        List<CourseCertificate> results = dao.findByUserId(userId, null, null, null,
                "start_date", "desc", 0, 10);

        assertEquals(1, results.size());
        assertEquals("AWS Course", results.get(0).getName());
        assertEquals("Coursera", results.get(0).getProvider());
        assertEquals(LocalDate.of(2025, 1, 15), results.get(0).getStartDate());
        assertEquals(LocalDate.of(2025, 6, 20), results.get(0).getEndDate());
        verify(statement).setObject(1, userId);
    }

    @Test
    void findByUserId_withSearch_appliesFilter() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        dao.findByUserId(userId, "Java", null, null, "name", "asc", 0, 10);

        verify(statement).setString(2, "%java%");
        verify(statement).setString(3, "%java%");
        verify(statement).setString(4, "%java%");
    }

    @Test
    void findByUserId_withShortSearch_ignoresFilter() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        dao.findByUserId(userId, "Ja", null, null, "name", "asc", 0, 10);

        // Only userId param — no search params
        verify(statement, times(1)).setObject(anyInt(), any());
    }

    @Test
    void findByUserId_withDateFilters_appliesFilter() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        dao.findByUserId(userId, null, "2024-01-01", "2024-12-31",
                "start_date", "desc", 0, 10);

        verify(statement).setDate(2, Date.valueOf("2024-01-01"));
        verify(statement).setDate(3, Date.valueOf("2024-12-31"));
    }

    @Test
    void findByUserId_withInvalidSortField_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                dao.findByUserId(userId, null, null, null, "invalid", "desc", 0, 10));
    }

    @Test
    void countByUserId_returnsCount() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(42L);

        long count = dao.countByUserId(userId, null, null, null);

        assertEquals(42L, count);
    }

    @Test
    void countByUserId_withSearch_appliesFilter() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(5L);

        long count = dao.countByUserId(userId, "Spring", null, null);

        assertEquals(5L, count);
        verify(statement).setString(2, "%spring%");
    }

    @Test
    void findById_found_returnsCourse() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("name")).thenReturn("AWS");
        when(resultSet.getString("provider")).thenReturn("Coursera");
        when(resultSet.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-01-01 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);
        when(resultSet.getBoolean("is_deleted")).thenReturn(false);
        when(resultSet.getTimestamp("deleted_at")).thenReturn(null);

        CourseCertificate result = dao.findById(1L, userId);

        assertNotNull(result);
        assertEquals("AWS", result.getName());
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

        CourseCertificate c = new CourseCertificate();
        c.setUserId(userId);
        c.setName("AWS");
        c.setProvider("Coursera");
        c.setStartDate(LocalDate.of(2025, 1, 1));

        CourseCertificate result = dao.create(c);

        assertEquals(42L, result.getId());
        verify(statement).setObject(1, userId);
        verify(statement).setString(2, "AWS");
    }

    @Test
    void update_existing_succeeds() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        CourseCertificate c = new CourseCertificate();
        c.setId(1L);
        c.setUserId(userId);
        c.setName("Updated");
        c.setProvider("Udemy");
        c.setStartDate(LocalDate.of(2025, 1, 1));

        dao.update(c);

        verify(statement).setString(1, "Updated");
        verify(statement).setLong(8, 1L);
    }

    @Test
    void update_notFound_throwsException() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        CourseCertificate c = new CourseCertificate();
        c.setId(999L);
        c.setUserId(userId);
        c.setName("X");
        c.setProvider("X");
        c.setStartDate(LocalDate.now());

        assertThrows(RuntimeException.class, () -> dao.update(c));
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
