package com.resumainer.dao;

import com.resumainer.model.WorkFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WorkFormatDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private WorkFormatDao dao;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        dao = new WorkFormatDao(dataSource);
        userId = UUID.randomUUID();
    }

    @Test
    void findAll_returnsAllFormats() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("code")).thenReturn("full-time", "remote");
        when(resultSet.getString("name")).thenReturn("Full-time", "Remote");

        List<WorkFormat> results = dao.findAll();

        assertEquals(2, results.size());
        assertEquals("full-time", results.get(0).getCode());
        assertEquals("Full-time", results.get(0).getName());
        assertEquals("remote", results.get(1).getCode());
    }

    @Test
    void findAll_empty_returnsEmptyList() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    void findByUserId_returnsFormats() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("code")).thenReturn("remote");
        when(resultSet.getString("name")).thenReturn("Remote");

        List<WorkFormat> results = dao.findByUserId(userId);

        assertEquals(1, results.size());
        assertEquals("remote", results.get(0).getCode());
        verify(statement).setObject(1, userId);
    }

    @Test
    void findByUserId_none_returnsEmptyList() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertTrue(dao.findByUserId(userId).isEmpty());
    }

    @Test
    void saveUserFormats_deletesOldInsertsNew() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(2);
        when(statement.executeBatch()).thenReturn(new int[]{1, 1});

        List<Long> formatIds = Arrays.asList(1L, 3L);
        dao.saveUserFormats(userId, formatIds);

        verify(connection).setAutoCommit(false);
        verify(statement, times(1)).executeUpdate(); // DELETE
        verify(statement, times(1)).executeBatch();   // INSERT batch
        verify(connection).commit();
    }

    @Test
    void saveUserFormats_emptyList_deletesOnly() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        dao.saveUserFormats(userId, List.of());

        verify(statement, times(1)).executeUpdate();
        verify(statement, never()).executeBatch();
        verify(connection).commit();
    }

    @Test
    void saveUserFormats_rollbackOnError() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class, () -> dao.saveUserFormats(userId, List.of(1L)));

        verify(connection).rollback();
    }
}
