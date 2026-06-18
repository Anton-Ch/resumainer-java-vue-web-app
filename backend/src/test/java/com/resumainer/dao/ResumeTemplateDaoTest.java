package com.resumainer.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for ResumeTemplateDao.
 * Covers findDefaultTemplatePath() — active template lookup.
 */
class ResumeTemplateDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private ResumeTemplateDao dao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        dao = new ResumeTemplateDao(dataSource);
    }

    @Test
    void findDefaultTemplatePath_returnsPath_whenTemplateExists() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("html_file_path")).thenReturn("templates/one-page.html");

        String path = dao.findDefaultTemplatePath();

        assertEquals("templates/one-page.html", path);
        verify(connection).prepareStatement(contains("FROM resume_template"));
        verify(statement).executeQuery();
    }

    @Test
    void findDefaultTemplatePath_returnsNull_whenNoActiveTemplate() throws Exception {
        when(resultSet.next()).thenReturn(false);

        String path = dao.findDefaultTemplatePath();

        assertNull(path);
    }

    @Test
    void findDefaultTemplatePath_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new java.sql.SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.findDefaultTemplatePath());

        assertTrue(ex.getMessage().contains("Database error finding default template"));
    }
}
