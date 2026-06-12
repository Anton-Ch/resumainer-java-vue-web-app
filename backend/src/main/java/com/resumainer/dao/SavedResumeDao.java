package com.resumainer.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

/**
 * DAO for 'saved_resumes' table.
 * Insert after finalization, owner-scoped read, public lookup by code.
 */
@Repository
public class SavedResumeDao {

    private static final Logger log = LoggerFactory.getLogger(SavedResumeDao.class);

    private static final String INSERT =
            "INSERT INTO saved_resumes "
            + "(user_id, resume_title, vacancy, company, language, adaptation_level, "
            + "public_code, public_url_link, html_file_path, pdf_file_path, "
            + "generation_request_id, response_id, "
            + "adaptation_level_id, language_id, created_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String SELECT_BY_PUBLIC_CODE =
            "SELECT * FROM saved_resumes WHERE public_code = ? AND is_deleted = FALSE";

    private final DataSource dataSource;

    public SavedResumeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Auto-managed insert (gets its own connection).
     */
    public long insert(UUID userId, String resumeTitle, String vacancy,
                       String company, String language, String adaptationLevel,
                       String publicCode, String publicUrlLink,
                       String htmlFilePath, String pdfFilePath,
                       UUID generationRequestId, UUID responseId,
                       long adaptationLevelId, long languageId) {
        try (Connection conn = dataSource.getConnection()) {
            return insert(conn, userId, resumeTitle, vacancy, company, language,
                    adaptationLevel, publicCode, publicUrlLink, htmlFilePath, pdfFilePath,
                    generationRequestId, responseId, adaptationLevelId, languageId);
        } catch (SQLException e) {
            log.error("Error inserting saved resume", e);
            throw new RuntimeException("Failed to save resume.", e);
        }
    }

    public long insert(Connection conn, UUID userId, String resumeTitle, String vacancy,
                       String company, String language, String adaptationLevel,
                       String publicCode, String publicUrlLink,
                       String htmlFilePath, String pdfFilePath,
                       UUID generationRequestId, UUID responseId,
                       long adaptationLevelId, long languageId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setObject(1, userId);
            stmt.setString(2, resumeTitle);
            stmt.setString(3, vacancy);
            stmt.setString(4, company);
            stmt.setString(5, language);
            stmt.setString(6, adaptationLevel);
            stmt.setString(7, publicCode);
            stmt.setString(8, publicUrlLink);
            stmt.setString(9, htmlFilePath);
            stmt.setString(10, pdfFilePath);
            stmt.setObject(11, generationRequestId);
            stmt.setObject(12, responseId);
            stmt.setLong(13, adaptationLevelId);
            stmt.setLong(14, languageId);
            stmt.setObject(15, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("id") : -1;
            }
        }
    }

    /** Returns the public code if the resume is active and not deleted. Null otherwise. */
    public String findPublicCodeByCode(String publicCode) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_PUBLIC_CODE)) {
            stmt.setString(1, publicCode);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("public_code") : null;
            }
        } catch (SQLException e) {
            log.error("Error looking up public code: {}", publicCode, e);
            throw new RuntimeException("Database error looking up public code", e);
        }
    }
}
