package com.resumainer.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * Tests for PromptConfigDao prompt render log persistence.
 *
 * TDD note:
 * This test is expected to fail to compile until PromptConfigDao gets
 * insertPromptRenderLog(...). After the DAO method is implemented, it should pass.
 */
class PromptConfigDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private PromptConfigDao dao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        dao = new PromptConfigDao(dataSource);
    }

    @Test
    void insertPromptRenderLog_persistsRenderedPromptAndReturnsId() throws Exception {
        UUID expectedLogId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID promptConfigId = UUID.randomUUID();

        String systemPrompt = "SYSTEM PROMPT";
        String requestPrompt = "REQUEST PROMPT";
        String profilePayloadJson = "{\"contact\":{\"fullName\":\"Anton\"}}";
        String promptHash = "hash-123";

        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id", UUID.class)).thenReturn(expectedLogId);

        UUID actualLogId = dao.insertPromptRenderLog(
                requestId,
                promptConfigId,
                systemPrompt,
                requestPrompt,
                profilePayloadJson,
                promptHash
        );

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

        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                dao.insertPromptRenderLog(
                        requestId,
                        promptConfigId,
                        "SYSTEM PROMPT",
                        "REQUEST PROMPT",
                        "{\"profile\":true}",
                        "hash-456"
                )
        );

        assertTrue(exception.getMessage().contains("Prompt render log insert returned no id"));
    }
}
