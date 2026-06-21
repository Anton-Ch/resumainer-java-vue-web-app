package com.resumainer.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
            + "public_code, public_url_link, html_file_path, pdf_file_path, cover_letter, "
            + "generation_request_id, response_id, "
            + "adaptation_level_id, language_id, created_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

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
                       String coverLetter,
                       UUID generationRequestId, UUID responseId,
                       long adaptationLevelId, long languageId) {
        try (Connection conn = dataSource.getConnection()) {
            return insert(conn, userId, resumeTitle, vacancy, company, language,
                    adaptationLevel, publicCode, publicUrlLink, htmlFilePath, pdfFilePath,
                    coverLetter,
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
                       String coverLetter,
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
            stmt.setString(11, coverLetter);
            stmt.setObject(12, generationRequestId);
            stmt.setObject(13, responseId);
            stmt.setLong(14, adaptationLevelId);
            stmt.setLong(15, languageId);
            stmt.setObject(16, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("id") : -1;
            }
        }
    }

    private static final String SELECT_BY_GENERATION_REQUEST =
            "SELECT id, user_id, resume_title, vacancy, company, language, "
            + "adaptation_level, public_code, public_url_link, html_file_path, "
            + "pdf_file_path, generation_request_id, response_id, "
            + "adaptation_level_id, language_id, cover_letter, is_deleted "
            + "FROM saved_resumes WHERE generation_request_id = ? AND user_id = ? AND is_deleted = FALSE";

    private static final String SELECT_BY_ID =
            "SELECT id, user_id, resume_title, vacancy, company, language, "
            + "adaptation_level, public_code, public_url_link, html_file_path, "
            + "pdf_file_path, generation_request_id, response_id, "
            + "adaptation_level_id, language_id, cover_letter, is_deleted "
            + "FROM saved_resumes WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    /**
     * Finds a saved resume by ID, owner-scoped.
     */
    public SavedResumeRow findById(long id, UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setLong(1, id);
            stmt.setObject(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                SavedResumeRow row = new SavedResumeRow();
                row.id = rs.getLong("id");
                row.userId = (UUID) rs.getObject("user_id");
                row.title = rs.getString("resume_title");
                row.vacancy = rs.getString("vacancy");
                row.company = rs.getString("company");
                row.language = rs.getString("language");
                row.adaptationLevel = rs.getString("adaptation_level");
                row.publicCode = rs.getString("public_code");
                row.publicUrlLink = rs.getString("public_url_link");
                row.htmlFilePath = rs.getString("html_file_path");
                row.pdfFilePath = rs.getString("pdf_file_path");
                row.coverLetter = rs.getString("cover_letter");
                row.pdfStatus = rs.getString("pdf_status");
                row.pdfPageCount = rs.getObject("pdf_page_count") != null ? rs.getInt("pdf_page_count") : null;
                return row;
            }
        } catch (SQLException e) {
            log.error("Error finding saved resume by id: {}", id, e);
            throw new RuntimeException("Database error finding saved resume", e);
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

    /**
     * Finds all saved resumes for a generation request, owner-scoped.
     */
    public List<SavedResumeRow> findByGenerationRequestId(UUID generationRequestId, UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_GENERATION_REQUEST)) {
            stmt.setObject(1, generationRequestId);
            stmt.setObject(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<SavedResumeRow> results = new ArrayList<>();
                while (rs.next()) {
                    SavedResumeRow row = new SavedResumeRow();
                    row.id = rs.getLong("id");
                    row.userId = (UUID) rs.getObject("user_id");
                    row.title = rs.getString("resume_title");
                    row.vacancy = rs.getString("vacancy");
                    row.company = rs.getString("company");
                    row.language = rs.getString("language");
                    row.adaptationLevel = rs.getString("adaptation_level");
                    row.publicCode = rs.getString("public_code");
                    row.publicUrlLink = rs.getString("public_url_link");
                    row.htmlFilePath = rs.getString("html_file_path");
                    row.pdfFilePath = rs.getString("pdf_file_path");
                    row.coverLetter = rs.getString("cover_letter");
                    results.add(row);
                }
                return results;
            }
        } catch (SQLException e) {
            log.error("Error finding saved resumes for request: {}", generationRequestId, e);
            throw new RuntimeException("Database error finding saved resumes", e);
        }
    }

    /** Lightweight row representation for export. */
    public static class SavedResumeRow {
        public long id;
        public UUID userId;
        public String title;
        public String vacancy;
        public String company;
        public String language;
        public String adaptationLevel;
        public String publicCode;
        public String publicUrlLink;
        public String htmlFilePath;
        public String pdfFilePath;
        public String coverLetter;
        public String pdfStatus;
        public Integer pdfPageCount;
    }

    /** Update PDF generation result on a saved resume (Feature 008). */
    public void updatePdfMetadata(long savedResumeId, String pdfStatus, String pdfFilePath,
                                   Integer pdfPageCount, String renderProfile,
                                   String errorCode, String errorMessage) {
        String sql = "UPDATE saved_resumes SET pdf_status = ?, pdf_file_path = ?, pdf_page_count = ?, "
                   + "pdf_render_profile = ?, pdf_generation_error_code = ?, "
                   + "pdf_generation_error_message = ?, pdf_generated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pdfStatus);
            stmt.setString(2, pdfFilePath);
            if (pdfPageCount != null) stmt.setInt(3, pdfPageCount);
            else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, renderProfile);
            stmt.setString(5, errorCode);
            stmt.setString(6, errorMessage);
            stmt.setLong(7, savedResumeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating PDF metadata for resume: {}", savedResumeId, e);
            throw new RuntimeException("Failed to update PDF metadata", e);
        }
    }
}
