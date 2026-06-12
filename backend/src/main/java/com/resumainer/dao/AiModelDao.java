package com.resumainer.dao;

import com.resumainer.dto.generate.AiModelDto;
import com.resumainer.model.AiModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for the 'ai_model' table.
 * Supports active model lookup with privileged filtering.
 * Never returns api_key_encrypted in DTOs sent to frontend.
 */
@Repository
public class AiModelDao {

    private static final Logger log = LoggerFactory.getLogger(AiModelDao.class);

    private static final String SELECT_ACTIVE =
            "SELECT id, provider, model_code, display_name, provider_api_url, "
            + "api_key_encrypted, is_active, is_paid, is_hidden "
            + "FROM ai_model WHERE is_active = TRUE";

    private static final String SELECT_AVAILABLE =
            "SELECT id, provider, model_code, display_name "
            + "FROM ai_model WHERE is_active = TRUE";

    private static final String SELECT_AVAILABLE_NON_HIDDEN =
            "SELECT id, provider, model_code, display_name "
            + "FROM ai_model WHERE is_active = TRUE AND is_hidden = FALSE";

    private static final String SELECT_BY_ID =
            "SELECT id, provider, model_code, display_name, provider_api_url, "
            + "api_key_encrypted, is_active, is_paid, is_hidden "
            + "FROM ai_model WHERE id = ?";

    private final DataSource dataSource;

    public AiModelDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns models visible to a non-privileged user (active, non-hidden).
     * Safe DTO — no API key.
     */
    public List<AiModelDto> findAvailableModels() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_AVAILABLE_NON_HIDDEN);
             ResultSet rs = stmt.executeQuery()) {
            List<AiModelDto> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new AiModelDto(
                        (UUID) rs.getObject("id"),
                        rs.getString("provider"),
                        rs.getString("display_name"),
                        rs.getString("model_code")
                ));
            }
            return results;
        } catch (SQLException e) {
            log.error("Error finding available models", e);
            throw new RuntimeException("Database error finding available models", e);
        }
    }

    /**
     * Returns models visible to a privileged user (active, including hidden).
     * Safe DTO — no API key.
     */
    public List<AiModelDto> findAvailableModelsPrivileged() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_AVAILABLE);
             ResultSet rs = stmt.executeQuery()) {
            List<AiModelDto> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new AiModelDto(
                        (UUID) rs.getObject("id"),
                        rs.getString("provider"),
                        rs.getString("display_name"),
                        rs.getString("model_code")
                ));
            }
            return results;
        } catch (SQLException e) {
            log.error("Error finding privileged models", e);
            throw new RuntimeException("Database error finding privileged models", e);
        }
    }

    /**
     * Loads full AiModel by ID (includes encrypted API key — for service layer only).
     */
    public AiModel findById(UUID id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            log.error("Error finding model by id: {}", id, e);
            throw new RuntimeException("Database error finding model", e);
        }
    }

    private AiModel mapRow(ResultSet rs) throws SQLException {
        AiModel m = new AiModel();
        m.setId((UUID) rs.getObject("id"));
        m.setProvider(rs.getString("provider"));
        m.setModelCode(rs.getString("model_code"));
        m.setDisplayName(rs.getString("display_name"));
        m.setProviderApiUrl(rs.getString("provider_api_url"));
        m.setApiKeyEncrypted(rs.getString("api_key_encrypted"));
        m.setActive(rs.getBoolean("is_active"));
        m.setPaid(rs.getBoolean("is_paid"));
        m.setHidden(rs.getBoolean("is_hidden"));
        return m;
    }
}
