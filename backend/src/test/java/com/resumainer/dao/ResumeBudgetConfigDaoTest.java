package com.resumainer.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for ResumeBudgetConfigDao.
 * Covers loadActiveConfig, loadSectionBudget, loadAllSectionBudgets,
 * loadWorkExperienceDistributionRules, and SQL error handling.
 */
class ResumeBudgetConfigDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private ResumeBudgetConfigDao dao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        dao = new ResumeBudgetConfigDao(dataSource);
    }

    // ─── loadActiveConfig ─────────────────────────────────────────

    @Test
    void loadsActiveBudgetConfigId() throws Exception {
        when(resultSet.next()).thenReturn(true);
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
    void loadActiveConfig_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.loadActiveConfig());

        assertTrue(ex.getMessage().contains("Database error loading budget config"));
    }

    // ─── loadSectionBudget ────────────────────────────────────────

    @Test
    void loadsSkillsSectionBudget() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("min_value", Integer.class)).thenReturn(4);
        when(resultSet.getObject("max_value", Integer.class)).thenReturn(5);

        ResumeBudgetConfigDao.SectionBudget budget = dao.loadSectionBudget(1L, "skills", "light", "groups");

        assertNotNull(budget);
        assertEquals(4, budget.minValue.intValue());
        assertEquals(5, budget.maxValue.intValue());
    }

    @Test
    void returnsNullWhenNoSectionBudget() throws Exception {
        when(resultSet.next()).thenReturn(false);

        ResumeBudgetConfigDao.SectionBudget budget = dao.loadSectionBudget(1L, "skills", "nonexistent", "groups");

        assertNull(budget);
    }

    @Test
    void loadSectionBudget_handlesNullMinMax() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("min_value", Integer.class)).thenReturn(null);
        when(resultSet.getObject("max_value", Integer.class)).thenReturn(null);

        ResumeBudgetConfigDao.SectionBudget budget = dao.loadSectionBudget(1L, "skills", "light", "groups");

        assertNotNull(budget);
        assertNull(budget.minValue);
        assertNull(budget.maxValue);
    }

    @Test
    void loadSectionBudget_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.loadSectionBudget(1L, "skills", "light", "groups"));
    }

    // ─── loadAllSectionBudgets ────────────────────────────────────

    @Test
    void loadAllSectionBudgets_returnsAllBudgets() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("metric_key")).thenReturn("groups", "items_per_group");
        when(resultSet.getObject("min_value", Integer.class)).thenReturn(3, 2);
        when(resultSet.getObject("max_value", Integer.class)).thenReturn(5, 4);

        Map<String, ResumeBudgetConfigDao.SectionBudget> result = dao.loadAllSectionBudgets(1L, "skills", "light");

        assertEquals(2, result.size());
        assertEquals(3, result.get("groups").minValue.intValue());
        assertEquals(5, result.get("groups").maxValue.intValue());
        assertEquals(2, result.get("items_per_group").minValue.intValue());
        verify(statement).setLong(1, 1L);
        verify(statement).setString(2, "skills");
        verify(statement).setString(3, "light");
    }

    @Test
    void loadAllSectionBudgets_returnsEmptyMap_whenNoResults() throws Exception {
        when(resultSet.next()).thenReturn(false);

        Map<String, ResumeBudgetConfigDao.SectionBudget> result = dao.loadAllSectionBudgets(1L, "skills", "light");

        assertTrue(result.isEmpty());
    }

    @Test
    void loadAllSectionBudgets_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.loadAllSectionBudgets(1L, "skills", "light"));
    }

    // ─── loadWorkExperienceDistributionRules ──────────────────────

    @Test
    void loadWorkExperienceDistributionRules_returnsRules() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("case_key")).thenReturn("default");
        when(resultSet.getInt("min_total_jobs")).thenReturn(0);
        when(resultSet.getInt("max_total_jobs")).thenReturn(10);
        when(resultSet.getInt("min_projects")).thenReturn(0);
        when(resultSet.getObject("max_projects", Integer.class)).thenReturn(3);
        when(resultSet.getBoolean("require_no_courses")).thenReturn(false);
        when(resultSet.getString("template_mode")).thenReturn("standard");
        when(resultSet.getInt("page1_jobs")).thenReturn(2);
        when(resultSet.getInt("page2_jobs")).thenReturn(3);
        when(resultSet.getObject("page2_max_additional_jobs", Integer.class)).thenReturn(5);
        when(resultSet.getInt("priority")).thenReturn(100);

        List<ResumeBudgetConfigDao.WorkExperienceDistributionRule> rules =
                dao.loadWorkExperienceDistributionRules(1L);

        assertEquals(1, rules.size());
        ResumeBudgetConfigDao.WorkExperienceDistributionRule rule = rules.get(0);
        assertEquals("default", rule.caseKey);
        assertEquals(0, rule.minTotalJobs);
        assertEquals(10, rule.maxTotalJobs);
        assertEquals(0, rule.minProjects);
        assertEquals(Integer.valueOf(3), rule.maxProjects);
        assertFalse(rule.requireNoCourses);
        assertEquals("standard", rule.templateMode);
        assertEquals(2, rule.page1Jobs);
        assertEquals(3, rule.page2Jobs);
        assertEquals(Integer.valueOf(5), rule.page2MaxAdditionalJobs);
        assertEquals(100, rule.priority);
        verify(statement).setLong(1, 1L);
    }

    @Test
    void loadWorkExperienceDistributionRules_handlesNullOptionals() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("case_key")).thenReturn("special");
        when(resultSet.getInt("min_total_jobs")).thenReturn(5);
        when(resultSet.getInt("max_total_jobs")).thenReturn(8);
        when(resultSet.getInt("min_projects")).thenReturn(2);
        when(resultSet.getObject("max_projects", Integer.class)).thenReturn(null);
        when(resultSet.getBoolean("require_no_courses")).thenReturn(true);
        when(resultSet.getString("template_mode")).thenReturn("compact");
        when(resultSet.getInt("page1_jobs")).thenReturn(1);
        when(resultSet.getInt("page2_jobs")).thenReturn(2);
        when(resultSet.getObject("page2_max_additional_jobs", Integer.class)).thenReturn(null);
        when(resultSet.getInt("priority")).thenReturn(50);

        List<ResumeBudgetConfigDao.WorkExperienceDistributionRule> rules =
                dao.loadWorkExperienceDistributionRules(1L);

        assertEquals(1, rules.size());
        assertNull(rules.get(0).maxProjects);
        assertNull(rules.get(0).page2MaxAdditionalJobs);
        assertTrue(rules.get(0).requireNoCourses);
    }

    @Test
    void loadWorkExperienceDistributionRules_returnsEmptyList_whenNoRules() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<ResumeBudgetConfigDao.WorkExperienceDistributionRule> rules =
                dao.loadWorkExperienceDistributionRules(1L);

        assertTrue(rules.isEmpty());
    }

    @Test
    void loadWorkExperienceDistributionRules_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.loadWorkExperienceDistributionRules(1L));
    }
}
