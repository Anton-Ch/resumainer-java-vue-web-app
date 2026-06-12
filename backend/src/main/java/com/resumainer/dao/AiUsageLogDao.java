package com.resumainer.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

/**
 * DAO for 'ai_usage_log' and 'ai_usage_log_response' tables.
 * Append-only in MVP — no frontend read endpoint.
 */
@Repository
public class AiUsageLogDao {

    private static final Logger log = LoggerFactory.getLogger(AiUsageLogDao.class);

    private static final String INSERT_LOG =
            "INSERT INTO ai_usage_log (user_id, ai_model_id, generation_request_id, "
            + "tokens_sent, tokens_generated) VALUES (?, ?, ?, ?, ?) RETURNING id";

    private static final String INSERT_LOG_RESPONSE =
            "INSERT INTO ai_usage_log_response (ai_usage_log_id, generation_response_id) VALUES (?, ?)";

    private final DataSource dataSource;

    public AiUsageLogDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates a usage log entry and links it to all generated response IDs.
     * @return the usage log ID
     */
    public UUID createUsageLog(UUID userId, UUID aiModelId, UUID generationRequestId,
                                int tokensSent, int tokensGenerated,
                                UUID[] responseIds, Connection conn) throws SQLException {
        UUID logId;
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_LOG)) {
            stmt.setObject(1, userId);
            stmt.setObject(2, aiModelId);
            stmt.setObject(3, generationRequestId);
            stmt.setInt(4, tokensSent);
            stmt.setInt(5, tokensGenerated);
            try (ResultSet rs = stmt.executeQuery()) {
                logId = rs.next() ? (UUID) rs.getObject("id") : null;
            }
        }
        if (logId != null && responseIds != null) {
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_LOG_RESPONSE)) {
                for (UUID respId : responseIds) {
                    stmt.setObject(1, logId);
                    stmt.setObject(2, respId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
        return logId;
    }
}
