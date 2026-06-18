package com.resumainer.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PromptConfigDao.
 * Covers findActiveConfigId, getSystemPrompt, getLanguagePrompt,
 * getAdaptationPrompt, getCoverLetterPrompt, insertPromptRenderLog.
 */
class PromptConfigDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private PromptConfigDao dao;

    private final UUID configId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        dao = new PromptConfigDao(dataSource);
    }

    // ─── findActiveConfigId ───────────────────────────────────────

    @Test
    void findActiveConfigId_returnsId_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(configId);

        UUID result = dao.findActiveConfigId();

        assertEquals(configId, result);
    }

    @Test
    void findActiveConfigId_returnsNull_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        UUID result = dao.findActiveConfigId();

        assertNull(result);
    }

    @Test
    void findActiveConfigId_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.findActiveConfigId());

        assertTrue(ex.getMessage().contains("Database error finding active prompt config"));
    }

    // ─── getSystemPrompt ──────────────────────────────────────────

    @Test
    void getSystemPrompt_returnsPrompt_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("prompt")).thenReturn("You are a resume assistant");

        String prompt = dao.getSystemPrompt(configId);

        assertEquals("You are a resume assistant", prompt);
        verify(statement).setObject(1, configId);
    }

    @Test
    void getSystemPrompt_throwsException_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.getSystemPrompt(configId));

        assertTrue(ex.getMessage().contains("No system prompt"));
    }

    @Test
    void getSystemPrompt_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.getSystemPrompt(configId));
    }

    // ─── getLanguagePrompt ────────────────────────────────────────

    @Test
    void getLanguagePrompt_returnsPrompt_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("prompt")).thenReturn("Bilingual resume");

        String prompt = dao.getLanguagePrompt(configId, "BILINGUAL");

        assertEquals("Bilingual resume", prompt);
        verify(statement).setObject(1, configId);
        verify(statement).setString(2, "BILINGUAL");
    }

    @Test
    void getLanguagePrompt_throwsException_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> dao.getLanguagePrompt(configId, "UNKNOWN"));
    }

    @Test
    void getLanguagePrompt_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.getLanguagePrompt(configId, "BILINGUAL"));
    }

    // ─── getAdaptationPrompt ──────────────────────────────────────

    @Test
    void getAdaptationPrompt_returnsPrompt_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("prompt")).thenReturn("Minimal changes");

        String prompt = dao.getAdaptationPrompt(configId, "MINIMAL");

        assertEquals("Minimal changes", prompt);
        verify(statement).setObject(1, configId);
        verify(statement).setString(2, "MINIMAL");
    }

    @Test
    void getAdaptationPrompt_throwsException_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> dao.getAdaptationPrompt(configId, "UNKNOWN"));
    }

    @Test
    void getAdaptationPrompt_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.getAdaptationPrompt(configId, "MINIMAL"));
    }

    // ─── getCoverLetterPrompt ─────────────────────────────────────

    @Test
    void getCoverLetterPrompt_returnsPrompt_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("prompt")).thenReturn("Write a cover letter");

        String prompt = dao.getCoverLetterPrompt(configId, true);

        assertEquals("Write a cover letter", prompt);
        verify(statement).setObject(1, configId);
        verify(statement).setBoolean(2, true);
    }

    @Test
    void getCoverLetterPrompt_throwsException_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> dao.getCoverLetterPrompt(configId, false));
    }

    @Test
    void getCoverLetterPrompt_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.getCoverLetterPrompt(configId, true));
    }

    // ─── insertPromptRenderLog ────────────────────────────────────

    @Test
    void insertPromptRenderLog_persistsRenderedPromptAndReturnsId() throws Exception {
        UUID expectedLogId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID promptConfigId = UUID.randomUUID();

        String systemPrompt = "SYSTEM PROMPT";
        String requestPrompt = "REQUEST PROMPT";
        String profilePayloadJson = "{\"contact\":{\"fullName\":\"Anton\"}}";
        String promptHash = "hash-123";

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id", UUID.class)).thenReturn(expectedLogId);

        UUID actualLogId = dao.insertPromptRenderLog(
                requestId, promptConfigId, systemPrompt, requestPrompt,
                profilePayloadJson, promptHash);

        assertEquals(expectedLogId, actualLogId);
        verify(connection).prepareStatement(contains("INSERT INTO ai_prompt_render_log"));
        verify(statement).setObject(1, requestId);
        verify(statement).setObject(2, promptConfigId);
        verify(statement).setString(3, systemPrompt);
        verify(statement).setString(4, requestPrompt);
        verify(statement).setString(5, profilePayloadJson);
        verify(statement).setString(6, promptHash);
        verify(statement).executeQuery();
    }

    @Test
    void insertPromptRenderLog_whenInsertReturnsNoId_throwsRuntimeException() throws Exception {
        UUID requestId = UUID.randomUUID();
        UUID promptConfigId = UUID.randomUUID();

        when(resultSet.next()).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                dao.insertPromptRenderLog(
                        requestId, promptConfigId,
                        "SYSTEM PROMPT", "REQUEST PROMPT",
                        "{\"profile\":true}", "hash-456"));

        assertTrue(exception.getMessage().contains("Prompt render log insert returned no id"));
    }

    @Test
    void insertPromptRenderLog_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.insertPromptRenderLog(
                        UUID.randomUUID(), configId,
                        "S", "R", "{}", "h"));
    }
}
