package com.resumainer.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

/**
 * DAO for DB-backed modular prompt configuration.
 * Loads active prompt config and fragments for prompt assembly.
 */
@Repository
public class PromptConfigDao {

    private static final Logger log = LoggerFactory.getLogger(PromptConfigDao.class);

    private static final String SELECT_ACTIVE_CONFIG =
            "SELECT id, name, description FROM ai_prompt_config WHERE is_active = TRUE LIMIT 1";

    private static final String SELECT_SYSTEM_PROMPT =
            "SELECT prompt FROM ai_system_prompt WHERE prompt_config_id = ?";

    private static final String SELECT_LANGUAGE_PROMPT =
            "SELECT prompt FROM ai_request_prompt_language WHERE prompt_config_id = ? AND language_mode = ?";

    private static final String SELECT_ADAPTATION_PROMPT =
            "SELECT prompt FROM ai_request_prompt_adaptation WHERE prompt_config_id = ? AND adaptation_selection = ?";

    private static final String SELECT_COVER_LETTER_PROMPT =
            "SELECT prompt FROM ai_request_prompt_cover_letter WHERE prompt_config_id = ? AND include_cover_letter = ?";

    private static final String INSERT_PROMPT_RENDER_LOG = """
        INSERT INTO ai_prompt_render_log
        (generation_request_id, prompt_config_id, system_prompt_rendered,
         request_prompt_rendered, profile_payload_json, prompt_hash)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id
        """;

    private final DataSource dataSource;

    public PromptConfigDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Returns the active prompt config ID, or null if none active. */
    public UUID findActiveConfigId() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVE_CONFIG);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? (UUID) rs.getObject("id") : null;
        } catch (SQLException e) {
            log.error("Error finding active prompt config", e);
            throw new RuntimeException("Database error finding active prompt config", e);
        }
    }

    public String getSystemPrompt(UUID configId) {
        return getPromptString(configId, SELECT_SYSTEM_PROMPT, "system");
    }

    public String getLanguagePrompt(UUID configId, String languageMode) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_LANGUAGE_PROMPT)) {
            stmt.setObject(1, configId);
            stmt.setString(2, languageMode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("No language prompt for config=" + configId + " mode=" + languageMode);
                }
                return rs.getString("prompt");
            }
        } catch (SQLException e) {
            log.error("Error loading language prompt", e);
            throw new RuntimeException("Database error loading language prompt", e);
        }
    }

    public String getAdaptationPrompt(UUID configId, String adaptationSelection) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ADAPTATION_PROMPT)) {
            stmt.setObject(1, configId);
            stmt.setString(2, adaptationSelection);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("No adaptation prompt for config=" + configId + " selection=" + adaptationSelection);
                }
                return rs.getString("prompt");
            }
        } catch (SQLException e) {
            log.error("Error loading adaptation prompt", e);
            throw new RuntimeException("Database error loading adaptation prompt", e);
        }
    }

    public String getCoverLetterPrompt(UUID configId, boolean includeCoverLetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_COVER_LETTER_PROMPT)) {
            stmt.setObject(1, configId);
            stmt.setBoolean(2, includeCoverLetter);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("No cover letter prompt for config=" + configId + " include=" + includeCoverLetter);
                }
                return rs.getString("prompt");
            }
        } catch (SQLException e) {
            log.error("Error loading cover letter prompt", e);
            throw new RuntimeException("Database error loading cover letter prompt", e);
        }
    }

    public UUID insertPromptRenderLog(UUID generationRequestId,
                                      UUID promptConfigId,
                                      String systemPrompt,
                                      String requestPrompt,
                                      String profilePayloadJson,
                                      String promptHash) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PROMPT_RENDER_LOG)) {

            stmt.setObject(1, generationRequestId);
            stmt.setObject(2, promptConfigId);
            stmt.setString(3, systemPrompt);
            stmt.setString(4, requestPrompt);
            stmt.setString(5, profilePayloadJson);
            stmt.setString(6, promptHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject("id", UUID.class);
                }
                throw new RuntimeException("Prompt render log insert returned no id");
            }
        } catch (SQLException e) {
            log.error("Error inserting prompt render log for request {}", generationRequestId, e);
            throw new RuntimeException("Database error inserting prompt render log", e);
        }
    }

    private String getPromptString(UUID configId, String sql, String type) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, configId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("No " + type + " prompt for config=" + configId);
                }
                return rs.getString("prompt");
            }
        } catch (SQLException e) {
            log.error("Error loading " + type + " prompt", e);
            throw new RuntimeException("Database error loading " + type + " prompt", e);
        }
    }
}
