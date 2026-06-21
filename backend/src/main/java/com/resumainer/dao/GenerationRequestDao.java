package com.resumainer.dao;

import com.resumainer.model.ResumeGenerationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO for 'resume_generation_request' table.
 * All queries owner-scoped via user_id.
 */
@Repository
public class GenerationRequestDao {

    private static final Logger log = LoggerFactory.getLogger(GenerationRequestDao.class);

    private static final String INSERT =
            "INSERT INTO resume_generation_request "
            + "(user_id, ai_model_id, vacancy_title, vacancy_description, company_name, "
            + "company_description, additional_comments, include_cover_letter, "
            + "language_mode, adaptation_selection, prompt_config_id, budget_config_id, "
            + "budget_config_version_used, status) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String SELECT_BY_ID =
            "SELECT * FROM resume_generation_request WHERE id = ? AND user_id = ?";

    private static final String UPDATE_STATUS =
            "UPDATE resume_generation_request SET status = ?, error_message = ?, "
            + "completed_at = CASE WHEN ? THEN NOW() ELSE completed_at END "
            + "WHERE id = ? AND user_id = ?";

    private static final String UPDATE_SETTINGS =
            "UPDATE resume_generation_request SET language_mode = ?, adaptation_selection = ?, "
            + "ai_model_id = ?, include_cover_letter = ? "
            + "WHERE id = ? AND user_id = ? AND status = 'pending'";

    private static final String UPDATE_BUDGET_SNAPSHOT =
            "UPDATE resume_generation_request SET budget_config_id = ?, "
            + "budget_config_version_used = ? WHERE id = ? AND user_id = ?";

    private static final String COUNT_PROCESSING_BY_USER =
            "SELECT COUNT(*) FROM resume_generation_request "
            + "WHERE user_id = ? AND status = 'processing'";

    private static final String TRY_MARK_FINALIZING =
            "UPDATE resume_generation_request "
            + "SET status = 'finalizing', error_message = NULL "
            + "WHERE id = ? AND user_id = ? AND status = 'completed'";

    private final DataSource dataSource;

    public GenerationRequestDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ResumeGenerationRequest create(ResumeGenerationRequest request) {
        try (Connection conn = dataSource.getConnection()) {
            return create(request, conn);
        } catch (SQLException e) {
            log.error("Error creating generation request", e);
            throw new RuntimeException("Database error creating generation request", e);
        }
    }

    public ResumeGenerationRequest findById(UUID requestId, UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setObject(1, requestId);
            stmt.setObject(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            log.error("Error finding request: {}", requestId, e);
            throw new RuntimeException("Database error finding request", e);
        }
    }

    public void updateStatus(UUID requestId, UUID userId, String status, String errorMessage, boolean completed) {
        try (Connection conn = dataSource.getConnection()) {
            updateStatus(conn, requestId, userId, status, errorMessage, completed);
        } catch (SQLException e) {
            log.error("Error updating request status: {}", requestId, e);
            throw new RuntimeException("Database error updating request status", e);
        }
    }

    /** Connection-aware overload for transaction composition (Phase 22C). */
    public void updateStatus(Connection conn, UUID requestId, UUID userId,
                              String status, String errorMessage, boolean completed) {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)) {
            stmt.setString(1, status);
            stmt.setString(2, errorMessage);
            stmt.setBoolean(3, completed);
            stmt.setObject(4, requestId);
            stmt.setObject(5, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating request status", e);
        }
    }

    /**
     * Updates generation settings on a pending request.
     * Only allowed when status = 'pending' (before generation starts).
     * If the request has already been generated, settings cannot be changed.
     */
    public boolean updateSettings(UUID requestId, UUID userId,
                                   String languageMode, String adaptationSelection,
                                   UUID aiModelId, boolean includeCoverLetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SETTINGS)) {
            stmt.setString(1, languageMode);
            stmt.setString(2, adaptationSelection);
            stmt.setObject(3, aiModelId);
            stmt.setBoolean(4, includeCoverLetter);
            stmt.setObject(5, requestId);
            stmt.setObject(6, userId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                log.warn("No pending request found for settings update: requestId={}, userId={}", requestId, userId);
            }
            return rows > 0;
        } catch (SQLException e) {
            log.error("Error updating request settings: {}", requestId, e);
            throw new RuntimeException("Database error updating request settings", e);
        }
    }

    /**
     * Stores the active budget config snapshot on a request before generation.
     * This ensures the generated output is traceable to the exact budget config version used.
     */
    public void updateBudgetSnapshot(UUID requestId, UUID userId, long budgetConfigId, int budgetVersionNo) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_BUDGET_SNAPSHOT)) {
            stmt.setLong(1, budgetConfigId);
            stmt.setInt(2, budgetVersionNo);
            stmt.setObject(3, requestId);
            stmt.setObject(4, userId);
            stmt.executeUpdate();
            log.debug("Budget snapshot saved for request {}: configId={}, version={}",
                    requestId, budgetConfigId, budgetVersionNo);
        } catch (SQLException e) {
            log.error("Error updating budget snapshot for request: {}", requestId, e);
            throw new RuntimeException("Database error updating budget snapshot", e);
        }
    }

    public boolean hasProcessingRequest(UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_PROCESSING_BY_USER)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Error checking processing requests for user: {}", userId, e);
            throw new RuntimeException("Database error checking processing requests", e);
        }
    }

    /**
     * Atomically marks a generation request as finalizing.
     * Only succeeds when the current status is 'completed' (post-generation).
     *
     * @param requestId the generation request ID
     * @param userId    the owning user ID (owner-scoped)
     * @return true if the status was updated (lock acquired), false otherwise
     */
    public boolean tryMarkFinalizing(UUID requestId, UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(TRY_MARK_FINALIZING)) {
            stmt.setObject(1, requestId);
            stmt.setObject(2, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            log.error("Error marking request as finalizing: {}", requestId, e);
            throw new RuntimeException("Database error marking request as finalizing", e);
        }
    }

    // --- Connection-accepting overloads (D10) ---

    public ResumeGenerationRequest create(ResumeGenerationRequest request, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setObject(1, request.getUserId());
            stmt.setObject(2, request.getAiModelId());
            stmt.setString(3, request.getVacancyTitle());
            stmt.setString(4, request.getVacancyDescription());
            stmt.setString(5, request.getCompanyName());
            stmt.setString(6, request.getCompanyDescription());
            stmt.setString(7, request.getAdditionalComments());
            stmt.setBoolean(8, request.isIncludeCoverLetter());
            stmt.setString(9, request.getLanguageMode());
            stmt.setString(10, request.getAdaptationSelection());
            stmt.setObject(11, request.getPromptConfigId());
            stmt.setObject(12, request.getBudgetConfigId(), Types.BIGINT);
            stmt.setObject(13, request.getBudgetConfigVersionUsed(), Types.INTEGER);
            stmt.setString(14, request.getStatus());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    request.setId((UUID) rs.getObject("id"));
                }
            }
            log.debug("Generation request created: id={}, userId={}", request.getId(), request.getUserId());
            return request;
        }
    }

    private ResumeGenerationRequest mapRow(ResultSet rs) throws SQLException {
        ResumeGenerationRequest r = new ResumeGenerationRequest();
        r.setId((UUID) rs.getObject("id"));
        r.setUserId((UUID) rs.getObject("user_id"));
        r.setAiModelId((UUID) rs.getObject("ai_model_id"));
        r.setVacancyTitle(rs.getString("vacancy_title"));
        r.setVacancyDescription(rs.getString("vacancy_description"));
        r.setCompanyName(rs.getString("company_name"));
        r.setCompanyDescription(rs.getString("company_description"));
        r.setAdditionalComments(rs.getString("additional_comments"));
        r.setIncludeCoverLetter(rs.getBoolean("include_cover_letter"));
        r.setLanguageMode(rs.getString("language_mode"));
        r.setAdaptationSelection(rs.getString("adaptation_selection"));
        r.setPromptConfigId((UUID) rs.getObject("prompt_config_id"));
        r.setBudgetConfigId((Long) rs.getObject("budget_config_id"));
        r.setBudgetConfigVersionUsed((Integer) rs.getObject("budget_config_version_used"));
        r.setStatus(rs.getString("status"));
        r.setErrorMessage(rs.getString("error_message"));
        r.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        r.setCompletedAt(rs.getTimestamp("completed_at") != null ? rs.getTimestamp("completed_at").toLocalDateTime() : null);
        return r;
    }
}
