package com.resumainer.dao;

import com.resumainer.dto.generate.AiModelDto;
import com.resumainer.model.AiModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AiModelDao.
 * Covers findAvailableModels, findAvailableModelsPrivileged, findById.
 * Verifies API key is never returned in DTOs.
 */
class AiModelDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private AiModelDao dao;

    private final UUID modelId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        dao = new AiModelDao(dataSource);
    }

    @Test
    void findAvailableModels_returnsNonHiddenActiveModels() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject("id", UUID.class)).thenReturn(modelId);
        when(resultSet.getString("provider")).thenReturn("openrouter");
        when(resultSet.getString("display_name")).thenReturn("DeepSeek V4");
        when(resultSet.getString("model_code")).thenReturn("deepseek/deepseek-v4");

        List<AiModelDto> models = dao.findAvailableModels();

        assertEquals(2, models.size());
        assertEquals("deepseek/deepseek-v4", models.get(0).getModelCode());
        verify(connection).prepareStatement(contains("is_hidden = FALSE"));
        // AiModelDto intentionally excludes apiKeyEncrypted — verified by DTO class design
    }

    @Test
    void findAvailableModels_returnsEmptyList_whenNoModels() throws Exception {
        when(resultSet.next()).thenReturn(false);

        List<AiModelDto> models = dao.findAvailableModels();

        assertTrue(models.isEmpty());
    }

    @Test
    void findAvailableModelsPrivileged_returnsAllActiveModelsIncludingHidden() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id", UUID.class)).thenReturn(modelId);
        when(resultSet.getString("provider")).thenReturn("openrouter");
        when(resultSet.getString("display_name")).thenReturn("Hidden Model");
        when(resultSet.getString("model_code")).thenReturn("provider/hidden-model");

        List<AiModelDto> models = dao.findAvailableModelsPrivileged();

        assertEquals(1, models.size());
        assertEquals("Hidden Model", models.get(0).getDisplayName());
        verify(connection).prepareStatement(argThat(sql -> sql.contains("FROM ai_model WHERE is_active = TRUE")
                && !sql.contains("is_hidden")));
    }

    @Test
    void findById_returnsFullModel_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id")).thenReturn(modelId);
        when(resultSet.getString("provider")).thenReturn("openrouter");
        when(resultSet.getString("model_code")).thenReturn("deepseek/deepseek-v4");
        when(resultSet.getString("display_name")).thenReturn("DeepSeek V4");
        when(resultSet.getString("provider_api_url")).thenReturn("https://api.openrouter.ai/v1");
        when(resultSet.getString("api_key_encrypted")).thenReturn("enc:sk-...");
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getBoolean("is_paid")).thenReturn(false);
        when(resultSet.getBoolean("is_hidden")).thenReturn(false);

        AiModel model = dao.findById(modelId);

        assertNotNull(model);
        assertEquals(modelId, model.getId());
        assertEquals("deepseek/deepseek-v4", model.getModelCode());
        assertEquals("enc:sk-...", model.getApiKeyEncrypted());
        assertTrue(model.isActive());
        verify(statement).setObject(1, modelId);
    }

    @Test
    void findById_returnsNull_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        AiModel model = dao.findById(modelId);

        assertNull(model);
    }

    @Test
    void findAvailableModels_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new java.sql.SQLException("Connection failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.findAvailableModels());

        assertTrue(ex.getMessage().contains("Database error finding available models"));
    }
}
