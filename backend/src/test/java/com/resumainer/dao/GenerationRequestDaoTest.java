package com.resumainer.dao;

import com.resumainer.model.ResumeGenerationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for GenerationRequestDao.
 * Covers all 6 public methods: create, findById, updateStatus,
 * updateSettings, updateBudgetSnapshot, hasProcessingRequest.
 */
class GenerationRequestDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private GenerationRequestDao dao;

    private final UUID requestId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID aiModelId = UUID.randomUUID();
    private final UUID promptConfigId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        dao = new GenerationRequestDao(dataSource);
    }

    // ─── findById ───────────────────────────────────────────────

    @Test
    void findById_returnsRequest_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        stubFullResultSet();

        ResumeGenerationRequest result = dao.findById(requestId, userId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals("BILINGUAL", result.getLanguageMode());
        assertEquals("BALANCED", result.getAdaptationSelection());

        verify(statement).setObject(1, requestId);
        verify(statement).setObject(2, userId);
    }

    @Test
    void findById_returnsNull_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        ResumeGenerationRequest result = dao.findById(requestId, userId);

        assertNull(result);
    }

    @Test
    void findById_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.findById(requestId, userId));

        assertTrue(ex.getMessage().contains("Database error finding request"));
    }

    // ─── create (auto-connection) ────────────────────────────────

    @Test
    void create_insertsAndReturnsRequestWithId() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(requestId);

        ResumeGenerationRequest req = makeFullRequest();
        ResumeGenerationRequest result = dao.create(req);

        assertNotNull(result.getId());
        assertEquals(requestId, result.getId());
        verify(statement).executeQuery();
    }

    @Test
    void create_throwsException_onSqlError() throws Exception {
        doThrow(new SQLException("DB error")).when(connection).prepareStatement(anyString());

        ResumeGenerationRequest req = makeFullRequest();
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.create(req));

        assertTrue(ex.getMessage().contains("Database error creating generation request"));
    }

    // ─── create (connection-accepting overload) ──────────────────

    @Test
    void create_withConnection_insertsAndSetsId() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(requestId);

        ResumeGenerationRequest req = makeFullRequest();
        ResumeGenerationRequest result = dao.create(req, connection);

        assertEquals(requestId, result.getId());
        verify(connection, never()).close(); // caller manages connection
        verify(statement).setObject(1, req.getUserId());
        verify(statement).setString(3, "Senior Java Developer");
    }

    @Test
    void create_withConnection_doesNotSetId_whenNoGeneratedId() throws Exception {
        when(resultSet.next()).thenReturn(false);

        ResumeGenerationRequest req = makeFullRequest();
        req.setId(null);
        ResumeGenerationRequest result = dao.create(req, connection);

        assertNull(result.getId());
    }

    // ─── updateStatus ────────────────────────────────────────────

    @Test
    void updateStatus_setsComplete_withCompletedTrue() throws Exception {
        dao.updateStatus(requestId, userId, "completed", null, true);

        verify(statement).setString(1, "completed");
        verify(statement).setString(2, null);
        verify(statement).setBoolean(3, true);
        verify(statement).setObject(4, requestId);
        verify(statement).setObject(5, userId);
        verify(statement).executeUpdate();
    }

    @Test
    void updateStatus_setsFailed_withErrorMessage() throws Exception {
        dao.updateStatus(requestId, userId, "failed", "AI error", false);

        verify(statement).setString(1, "failed");
        verify(statement).setString(2, "AI error");
        verify(statement).setBoolean(3, false);
        verify(statement).executeUpdate();
    }

    @Test
    void updateStatus_throwsException_onSqlError() throws Exception {
        doThrow(new SQLException("DB error")).when(connection).prepareStatement(anyString());

        assertThrows(RuntimeException.class,
                () -> dao.updateStatus(requestId, userId, "failed", null, false));
    }

    // ─── updateSettings ──────────────────────────────────────────

    @Test
    void updateSettings_returnsTrue_whenUpdated() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = dao.updateSettings(requestId, userId, "ENGLISH_ONLY",
                "MINIMAL", aiModelId, false);

        assertTrue(result);
        verify(statement).setString(1, "ENGLISH_ONLY");
        verify(statement).setString(2, "MINIMAL");
        verify(statement).setObject(3, aiModelId);
        verify(statement).setBoolean(4, false);
        verify(statement).setObject(5, requestId);
        verify(statement).setObject(6, userId);
    }

    @Test
    void updateSettings_returnsFalse_whenNoPendingRequest() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        boolean result = dao.updateSettings(requestId, userId, "RU", "MAXIMUM", aiModelId, true);

        assertFalse(result);
    }

    @Test
    void updateSettings_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.updateSettings(requestId, userId, "EN", "BALANCED", aiModelId, false));
    }

    // ─── updateBudgetSnapshot ────────────────────────────────────

    @Test
    void updateBudgetSnapshot_savesConfigIdAndVersion() throws Exception {
        dao.updateBudgetSnapshot(requestId, userId, 42L, 3);

        verify(statement).setLong(1, 42L);
        verify(statement).setInt(2, 3);
        verify(statement).setObject(3, requestId);
        verify(statement).setObject(4, userId);
        verify(statement).executeUpdate();
    }

    @Test
    void updateBudgetSnapshot_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.updateBudgetSnapshot(requestId, userId, 1L, 1));
    }

    // ─── hasProcessingRequest ────────────────────────────────────

    @Test
    void hasProcessingRequest_returnsTrue_whenCountGreaterThanZero() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        assertTrue(dao.hasProcessingRequest(userId));
        verify(statement).setObject(1, userId);
    }

    @Test
    void hasProcessingRequest_returnsFalse_whenCountIsZero() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        assertFalse(dao.hasProcessingRequest(userId));
    }

    @Test
    void hasProcessingRequest_returnsFalse_whenNoResult() throws Exception {
        when(resultSet.next()).thenReturn(false);

        assertFalse(dao.hasProcessingRequest(userId));
    }

    @Test
    void hasProcessingRequest_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class,
                () -> dao.hasProcessingRequest(userId));
    }

    // ─── helper ──────────────────────────────────────────────────

    private ResumeGenerationRequest makeFullRequest() {
        ResumeGenerationRequest r = new ResumeGenerationRequest();
        r.setUserId(userId);
        r.setAiModelId(aiModelId);
        r.setVacancyTitle("Senior Java Developer");
        r.setVacancyDescription("Looking for experienced Java developer");
        r.setCompanyName("TechCorp");
        r.setCompanyDescription("Fintech startup");
        r.setAdditionalComments("Remote preferred");
        r.setIncludeCoverLetter(true);
        r.setLanguageMode("BILINGUAL");
        r.setAdaptationSelection("BALANCED");
        r.setPromptConfigId(promptConfigId);
        r.setBudgetConfigId(42L);
        r.setBudgetConfigVersionUsed(3);
        r.setStatus("pending");
        return r;
    }

    private void stubFullResultSet() throws Exception {
        when(resultSet.getObject("id")).thenReturn(requestId);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getObject("ai_model_id")).thenReturn(aiModelId);
        when(resultSet.getString("vacancy_title")).thenReturn("Senior Java Developer");
        when(resultSet.getString("vacancy_description")).thenReturn("Looking for experienced");
        when(resultSet.getString("company_name")).thenReturn("TechCorp");
        when(resultSet.getString("company_description")).thenReturn("Fintech");
        when(resultSet.getString("additional_comments")).thenReturn("Remote");
        when(resultSet.getBoolean("include_cover_letter")).thenReturn(true);
        when(resultSet.getString("language_mode")).thenReturn("BILINGUAL");
        when(resultSet.getString("adaptation_selection")).thenReturn("BALANCED");
        when(resultSet.getObject("prompt_config_id")).thenReturn(promptConfigId);
        when(resultSet.getObject("budget_config_id")).thenReturn(42L);
        when(resultSet.getObject("budget_config_version_used")).thenReturn(3);
        when(resultSet.getString("status")).thenReturn("pending");
        when(resultSet.getString("error_message")).thenReturn(null);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("completed_at")).thenReturn(null);
    }
}
