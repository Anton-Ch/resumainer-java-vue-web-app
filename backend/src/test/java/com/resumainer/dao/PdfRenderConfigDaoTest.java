package com.resumainer.dao;

import com.resumainer.model.PdfFillTarget;
import com.resumainer.model.PdfFitLimits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PdfRenderConfigDao.
 */
class PdfRenderConfigDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private PdfRenderConfigDao dao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        dao = new PdfRenderConfigDao(dataSource);
    }

    @Test
    void findActive_returnsLimitsWhenPresent() throws Exception {
        when(resultSet.next()).thenReturn(true);
        mockFitLimitsRow();

        PdfFitLimits limits = dao.findActive();

        assertNotNull(limits);
        assertEquals("default-v1", limits.getConfigKey());
        assertEquals(30, limits.getMaxAttempts());
        assertEquals(new BigDecimal("9.0"), limits.getBodyFontMinPx());
    }

    @Test
    void findActive_returnsNullWhenNoConfig() throws Exception {
        when(resultSet.next()).thenReturn(false);

        PdfFitLimits limits = dao.findActive();

        assertNull(limits);
    }

    @Test
    void findFillTargets_returnsEmptyListWhenNoTargets() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<PdfFillTarget> targets = dao.findFillTargets(1L);

        assertNotNull(targets);
        assertTrue(targets.isEmpty());
    }

    private void mockFitLimitsRow() throws Exception {
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("config_key")).thenReturn("default-v1");
        when(resultSet.getBoolean("active")).thenReturn(true);
        when(resultSet.getBigDecimal("body_font_min_px")).thenReturn(new BigDecimal("9.0"));
        when(resultSet.getBigDecimal("body_font_max_px")).thenReturn(new BigDecimal("16.0"));
        when(resultSet.getBigDecimal("line_height_min")).thenReturn(new BigDecimal("1.05"));
        when(resultSet.getBigDecimal("line_height_max")).thenReturn(new BigDecimal("1.75"));
        when(resultSet.getBigDecimal("section_gap_min_px")).thenReturn(new BigDecimal("2.4"));
        when(resultSet.getBigDecimal("section_gap_max_px")).thenReturn(new BigDecimal("50.0"));
        when(resultSet.getBigDecimal("item_gap_min_px")).thenReturn(new BigDecimal("2.4"));
        when(resultSet.getBigDecimal("item_gap_max_px")).thenReturn(new BigDecimal("30.0"));
        when(resultSet.getBigDecimal("paragraph_gap_min_px")).thenReturn(new BigDecimal("1.6"));
        when(resultSet.getBigDecimal("paragraph_gap_max_px")).thenReturn(new BigDecimal("24.0"));
        when(resultSet.getBigDecimal("bullet_gap_min_px")).thenReturn(new BigDecimal("0.8"));
        when(resultSet.getBigDecimal("bullet_gap_max_px")).thenReturn(new BigDecimal("18.0"));
        when(resultSet.getInt("max_attempts")).thenReturn(30);
        when(resultSet.getBigDecimal("page2_delta_limit_percent")).thenReturn(new BigDecimal("50.0"));
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
    }

    private void mockFillTargetRow(long id, int targetPages, int pageNumber, String minFillStr, int priority) throws Exception {
        when(resultSet.getLong("id")).thenReturn(id);
        when(resultSet.getLong("fit_limits_id")).thenReturn(1L);
        when(resultSet.getInt("target_page_count")).thenReturn(targetPages);
        when(resultSet.getInt("page_number")).thenReturn(pageNumber);
        when(resultSet.getString("language_code")).thenReturn(null);
        when(resultSet.getObject("project_count_min")).thenReturn(id == 2 ? 0 : 1);
        when(resultSet.getObject("project_count_max")).thenReturn(id == 2 ? 0 : null);
        when(resultSet.getBigDecimal("min_fill")).thenReturn(new BigDecimal(minFillStr));
        when(resultSet.getBigDecimal("max_fill")).thenReturn(new BigDecimal("0.96"));
        when(resultSet.getInt("priority")).thenReturn(priority);
    }
}
