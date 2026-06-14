package com.resumainer.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ResumeBudgetConfigDao.
 * Uses mocked JDBC — no real DB required.
 */
class ResumeBudgetConfigDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private ResumeBudgetConfigDao dao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        dao = new ResumeBudgetConfigDao(dataSource);
    }

    @AfterEach
    void tearDown() {
        // Reset mocks between tests to avoid stubbing cross-contamination
        reset(preparedStatement, resultSet);
    }

    @Test
    void loadsActiveBudgetConfigId() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("Default MVP Resume Budget");
        when(resultSet.getInt("version_no")).thenReturn(1);

        ResumeBudgetConfigDao.BudgetConfig config = dao.loadActiveConfig();

        assertNotNull(config);
        assertEquals(1L, config.id);
        assertEquals("Default MVP Resume Budget", config.name);
        assertEquals(1, config.versionNo);
    }

    @Test
    void returnsNullWhenNoActiveConfig() throws Exception {
        when(resultSet.next()).thenReturn(false);

        ResumeBudgetConfigDao.BudgetConfig config = dao.loadActiveConfig();

        assertNull(config);
    }

    @Test
    void loadsSkillsSectionBudget() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("min_value", Integer.class)).thenReturn(4);
        when(resultSet.getObject("max_value", Integer.class)).thenReturn(5);

        ResumeBudgetConfigDao.SectionBudget budget = dao.loadSectionBudget(1L, "skills", "light", "groups");

        assertNotNull(budget);
        assertEquals(4, budget.minValue);
        assertEquals(5, budget.maxValue);
    }

    @Test
    void returnsNullWhenNoSectionBudget() throws Exception {
        when(resultSet.next()).thenReturn(false);

        ResumeBudgetConfigDao.SectionBudget budget = dao.loadSectionBudget(1L, "skills", "nonexistent", "groups");

        assertNull(budget);
    }
}
