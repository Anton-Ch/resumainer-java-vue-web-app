package com.resumainer.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SavedResumeDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private SavedResumeDao dao;

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

        dao = new SavedResumeDao(dataSource);
    }

    @Test
    void findById_returnsRow_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(5L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("resume_title")).thenReturn("My Resume");
        when(resultSet.getString("vacancy")).thenReturn("Java Dev");
        when(resultSet.getString("company")).thenReturn("MockCo");
        when(resultSet.getString("language")).thenReturn("EN");
        when(resultSet.getString("adaptation_level")).thenReturn("BALANCED");
        when(resultSet.getString("public_code")).thenReturn("ABC123");
        when(resultSet.getString("public_url_link")).thenReturn("/c/ABC123");
        when(resultSet.getString("html_file_path")).thenReturn("/path/to/file.html");
        when(resultSet.getString("pdf_file_path")).thenReturn(null);
        when(resultSet.getString("cover_letter")).thenReturn("Cover text");

        SavedResumeDao.SavedResumeRow row = dao.findById(5L, userId);

        assertNotNull(row);
        assertEquals(5L, row.id);
        assertEquals(userId, row.userId);
        assertEquals("My Resume", row.title);
        assertEquals("/path/to/file.html", row.htmlFilePath);
        assertEquals("ABC123", row.publicCode);

        verify(statement).setLong(1, 5L);
        verify(statement).setObject(2, userId);
    }

    @Test
    void findById_returnsNull_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        SavedResumeDao.SavedResumeRow row = dao.findById(999L, userId);

        assertNull(row);
    }

    @Test
    void findById_verifiesOwnerScoping() throws Exception {
        when(resultSet.next()).thenReturn(false);

        // Different user should get null
        UUID otherUser = UUID.randomUUID();
        SavedResumeDao.SavedResumeRow row = dao.findById(5L, otherUser);

        assertNull(row);
        verify(statement).setObject(2, otherUser);
    }
}
