package com.resumainer.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AiUsageLogDao.
 * Covers createUsageLog() — append-only usage logging with response linking.
 */
class AiUsageLogDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement logStmt;
    private PreparedStatement linkStmt;
    private ResultSet resultSet;
    private AiUsageLogDao dao;

    private final UUID userId = UUID.randomUUID();
    private final UUID modelId = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();
    private final UUID logId = UUID.randomUUID();
    private final UUID respId1 = UUID.randomUUID();
    private final UUID respId2 = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        logStmt = mock(PreparedStatement.class);
        linkStmt = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(logStmt, linkStmt);
        when(logStmt.executeQuery()).thenReturn(resultSet);

        dao = new AiUsageLogDao(dataSource);
    }

    @Test
    void createUsageLog_insertsLogAndLinksResponses() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(logId);

        UUID[] responseIds = {respId1, respId2};
        UUID actualLogId = dao.createUsageLog(userId, modelId, requestId, 100, 50, responseIds, connection);

        assertEquals(logId, actualLogId);

        verify(logStmt).setObject(1, userId);
        verify(logStmt).setObject(2, modelId);
        verify(logStmt).setObject(3, requestId);
        verify(logStmt).setInt(4, 100);
        verify(logStmt).setInt(5, 50);
        verify(logStmt).executeQuery();

        verify(linkStmt, times(2)).setObject(eq(1), eq(logId));
        verify(linkStmt).setObject(2, respId1);
        verify(linkStmt).setObject(2, respId2);
        verify(linkStmt, times(2)).addBatch();
        verify(linkStmt).executeBatch();
    }

    @Test
    void createUsageLog_returnsNull_whenInsertFails() throws Exception {
        when(resultSet.next()).thenReturn(false);

        UUID[] responseIds = {respId1};
        UUID actualLogId = dao.createUsageLog(userId, modelId, requestId, 0, 0, responseIds, connection);

        assertNull(actualLogId);
    }

    @Test
    void createUsageLog_skipsLinking_whenLogIdIsNull() throws Exception {
        when(resultSet.next()).thenReturn(false);

        UUID actualLogId = dao.createUsageLog(userId, modelId, requestId, 0, 0, null, connection);

        assertNull(actualLogId);
        verify(linkStmt, never()).executeBatch();
    }

    @Test
    void createUsageLog_skipsLinking_whenResponseIdsNull() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(logId);

        UUID actualLogId = dao.createUsageLog(userId, modelId, requestId, 10, 5, null, connection);

        assertEquals(logId, actualLogId);
        verify(linkStmt, never()).executeBatch();
    }
}
