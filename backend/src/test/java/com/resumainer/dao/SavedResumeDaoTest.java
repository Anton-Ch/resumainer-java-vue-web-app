package com.resumainer.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SavedResumeDao.
 * Covers insert, findById, findByGenerationRequestId, findPublicCodeByCode,
 * and SQL error handling.
 */
class SavedResumeDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private SavedResumeDao dao;

    private final UUID userId = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();
    private final UUID responseId = UUID.randomUUID();

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

        dao = new SavedResumeDao(dataSource);
    }

    // ─── insert (auto-connection) ─────────────────────────────────

    @Test
    void insert_returnsGeneratedId() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(42L);

        long id = insertFull();

        assertEquals(42L, id);
    }

    @Test
    void insert_returnsMinusOne_whenNoGeneratedId() throws Exception {
        when(resultSet.next()).thenReturn(false);

        long id = insertFull();

        assertEquals(-1L, id);
    }

    @Test
    void insert_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> insertFull());

        assertTrue(ex.getMessage().contains("Failed to save resume"));
    }

    // ─── insert (connection-accepting) ────────────────────────────

    @Test
    void insert_withConnection_setsAllParameters() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(42L);

        long id = insertWithConnection();

        assertEquals(42L, id);
        verify(statement).setObject(1, userId);
        verify(statement).setString(2, "Resume Title");
        verify(statement).setString(3, "Java Dev");
        verify(statement).setString(4, "MockCo");
        verify(statement).setString(5, "EN");
        verify(statement).setString(6, "BALANCED");
        verify(statement).setString(7, "PUBLIC123");
        verify(statement).setString(8, "/c/PUBLIC123");
        verify(statement).setString(9, "/path/file.html");
        verify(statement).setString(10, "/path/file.pdf");
        verify(statement).setString(11, "Cover text");
        verify(statement).setObject(12, requestId);
        verify(statement).setObject(13, responseId);
        verify(statement).setLong(14, 1L);
        verify(statement).setLong(15, 2L);
        verify(statement).setObject(16, userId);
        verify(statement).executeQuery();
    }

    @Test
    void insert_withConnection_returnsMinusOne_whenNoGeneratedId() throws Exception {
        when(resultSet.next()).thenReturn(false);

        long id = insertWithConnection();

        assertEquals(-1L, id);
    }

    @Test
    void insert_withConnection_doesNotCloseConnection() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(42L);

        insertWithConnection();

        verify(connection, never()).close();
    }

    // ─── findById ─────────────────────────────────────────────────

    @Test
    void findById_returnsRow_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        stubRowResultSet(5L);

        SavedResumeDao.SavedResumeRow row = dao.findById(5L, userId);

        assertNotNull(row);
        assertEquals(5L, row.id);
        assertEquals(userId, row.userId);
        assertEquals("My Resume", row.title);
        assertEquals("Java Dev", row.vacancy);
        assertEquals("MockCo", row.company);
        assertEquals("EN", row.language);
        assertEquals("BALANCED", row.adaptationLevel);
        assertEquals("PUBLIC123", row.publicCode);
        assertEquals("/c/PUBLIC123", row.publicUrlLink);
        assertEquals("/path/file.html", row.htmlFilePath);
        assertEquals("/path/file.pdf", row.pdfFilePath);
        assertEquals("Cover text", row.coverLetter);

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

        UUID otherUser = UUID.randomUUID();
        SavedResumeDao.SavedResumeRow row = dao.findById(5L, otherUser);

        assertNull(row);
        verify(statement).setObject(2, otherUser);
    }

    @Test
    void findById_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.findById(5L, userId));
    }

    // ─── findByGenerationRequestId ────────────────────────────────

    @Test
    void findByGenerationRequestId_returnsRows_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        stubRowResultSet(1L);
        // Second row: override id
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getObject("user_id")).thenReturn(userId);

        List<SavedResumeDao.SavedResumeRow> rows = dao.findByGenerationRequestId(requestId, userId);

        assertEquals(2, rows.size());
        assertEquals(1L, rows.get(0).id);
        assertEquals(2L, rows.get(1).id);
        verify(statement).setObject(1, requestId);
        verify(statement).setObject(2, userId);
    }

    @Test
    void findByGenerationRequestId_returnsEmptyList_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<SavedResumeDao.SavedResumeRow> rows = dao.findByGenerationRequestId(requestId, userId);

        assertTrue(rows.isEmpty());
    }

    @Test
    void findByGenerationRequestId_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.findByGenerationRequestId(requestId, userId));
    }

    // ─── findPublicCodeByCode ─────────────────────────────────────

    @Test
    void findPublicCodeByCode_returnsCode_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("public_code")).thenReturn("PUBLIC123");

        String code = dao.findPublicCodeByCode("PUBLIC123");

        assertEquals("PUBLIC123", code);
        verify(statement).setString(1, "PUBLIC123");
    }

    @Test
    void findPublicCodeByCode_returnsNull_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        String code = dao.findPublicCodeByCode("MISSING");

        assertNull(code);
    }

    @Test
    void findPublicCodeByCode_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.findPublicCodeByCode("ERR"));

        assertTrue(ex.getMessage().contains("Database error looking up public code"));
    }

    // ─── helpers ──────────────────────────────────────────────────

    private long insertFull() {
        return dao.insert(userId, "Resume Title", "Java Dev", "MockCo",
                "EN", "BALANCED", "PUBLIC123", "/c/PUBLIC123",
                "/path/file.html", "/path/file.pdf", "Cover text",
                requestId, responseId, 1L, 2L);
    }

    private long insertWithConnection() throws Exception {
        return dao.insert(connection, userId, "Resume Title", "Java Dev", "MockCo",
                "EN", "BALANCED", "PUBLIC123", "/c/PUBLIC123",
                "/path/file.html", "/path/file.pdf", "Cover text",
                requestId, responseId, 1L, 2L);
    }

    private void stubRowResultSet(long id) throws Exception {
        when(resultSet.getLong("id")).thenReturn(id);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("resume_title")).thenReturn("My Resume");
        when(resultSet.getString("vacancy")).thenReturn("Java Dev");
        when(resultSet.getString("company")).thenReturn("MockCo");
        when(resultSet.getString("language")).thenReturn("EN");
        when(resultSet.getString("adaptation_level")).thenReturn("BALANCED");
        when(resultSet.getString("public_code")).thenReturn("PUBLIC123");
        when(resultSet.getString("public_url_link")).thenReturn("/c/PUBLIC123");
        when(resultSet.getString("html_file_path")).thenReturn("/path/file.html");
        when(resultSet.getString("pdf_file_path")).thenReturn("/path/file.pdf");
        when(resultSet.getString("cover_letter")).thenReturn("Cover text");
    }
}
