package com.resumainer.dao;

import com.resumainer.model.SavedResume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResumeDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private ResumeDao resumeDao;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        resumeDao = new ResumeDao(dataSource);
        userId = UUID.randomUUID();
    }

    @Test
    void findByUserId_withValidParams_returnsResumes() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("resume_title")).thenReturn("Title 1", "Title 2");
        when(resultSet.getString("vacancy")).thenReturn("Vacancy 1", "Vacancy 2");
        when(resultSet.getString("company")).thenReturn("Company 1", "Company 2");
        when(resultSet.getString("language")).thenReturn("EN", "RU");
        when(resultSet.getString("adaptation_level")).thenReturn("BALANCED", "MAXIMUM");
        when(resultSet.getDate("created_at")).thenReturn(Date.valueOf("2025-01-09"), Date.valueOf("2025-01-10"));
        when(resultSet.getString("public_url")).thenReturn("/url/1", "/url/2");
        when(resultSet.getString("pdf_url")).thenReturn("/pdf/1", "/pdf/2");
        when(resultSet.getString("cover_letter")).thenReturn("Letter 1", "Letter 2");

        List<SavedResume> results = resumeDao.findByUserId(userId, null, null, null, null, null, null,
                "created_at", "desc", 0, 10);

        assertEquals(2, results.size());
        assertEquals("Title 1", results.get(0).getResumeTitle());
        assertEquals("EN", results.get(0).getLanguage());
        assertEquals("Title 2", results.get(1).getResumeTitle());
        assertEquals("RU", results.get(1).getLanguage());
        verify(statement).setObject(1, userId);
    }

    @Test
    void findByUserId_withSearch_filtersBySearch() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        resumeDao.findByUserId(userId, "analyst", null, null, null, null, null, "created_at", "desc", 0, 10);

        verify(statement).setString(2, "%analyst%");
        verify(statement).setString(3, "%analyst%");
        verify(statement).setString(4, "%analyst%");
    }

    @Test
    void countByUserId_returnsCount() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(42L);

        long count = resumeDao.countByUserId(userId, null, null, null, null, null, null);

        assertEquals(42L, count);
        verify(statement).setObject(1, userId);
    }

    @Test
    void findById_ownedByUser_returnsResume() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("resume_title")).thenReturn("Test Resume");

        SavedResume result = resumeDao.findById(1L, userId);

        assertNotNull(result);
        assertEquals("Test Resume", result.getResumeTitle());
        verify(statement).setLong(1, 1L);
        verify(statement).setObject(2, userId);
    }

    @Test
    void findById_notOwned_returnsNull() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        SavedResume result = resumeDao.findById(999L, userId);

        assertNull(result);
    }

    @Test
    void softDelete_ownedByUser_returnsTrue() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        boolean deleted = resumeDao.softDelete(1L, userId);

        assertTrue(deleted);
        verify(statement).setLong(1, 1L);
        verify(statement).setObject(2, userId);
    }

    @Test
    void softDelete_notOwned_returnsFalse() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        boolean deleted = resumeDao.softDelete(999L, userId);

        assertFalse(deleted);
    }

    @Test
    void findByUserId_withInvalidSortField_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                resumeDao.findByUserId(userId, null, null, null, null, null, null, "invalid", "desc", 0, 10));
    }
}
