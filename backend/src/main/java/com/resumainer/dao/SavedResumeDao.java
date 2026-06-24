package com.resumainer.dao;

import com.resumainer.model.PublicResumeLookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import static com.resumainer.model.PublicResumeLookupResult.Status;
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
            "SELECT sr.id, sr.user_id, sr.resume_title, sr.vacancy, sr.company, sr.language, "
            + "sr.adaptation_level, sr.public_code, sr.public_url_link, sr.html_file_path, "
            + "sr.pdf_file_path, sr.generation_request_id, sr.response_id, "
            + "sr.adaptation_level_id, sr.language_id, sr.cover_letter, sr.is_deleted, "
            + "sr.pdf_status, sr.pdf_page_count, u.username "
            + "FROM saved_resumes sr JOIN users u ON sr.user_id = u.id "
            + "WHERE sr.generation_request_id = ? AND sr.user_id = ? AND sr.is_deleted = FALSE";

    private static final String SELECT_BY_ID =
            "SELECT sr.id, sr.user_id, sr.resume_title, sr.vacancy, sr.company, sr.language, "
            + "sr.adaptation_level, sr.public_code, sr.public_url_link, sr.html_file_path, "
            + "sr.pdf_file_path, sr.generation_request_id, sr.response_id, "
            + "sr.adaptation_level_id, sr.language_id, sr.cover_letter, sr.is_deleted, "
            + "sr.pdf_status, sr.pdf_page_count, u.username "
            + "FROM saved_resumes sr JOIN users u ON sr.user_id = u.id "
            + "WHERE sr.id = ? AND sr.user_id = ? AND sr.is_deleted = FALSE";

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
                row.username = rs.getString("username");
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
     * Feature 009: Look up public resume status by username and public code.
     * Returns a {@link PublicResumeLookupResult} with status and PDF path.
     * The caller can distinguish deleted, not-found, and active states.
     */
    public PublicResumeLookupResult findPublicResumeStatus(String username, String publicCode) {
        if (username == null || username.isBlank() || publicCode == null || publicCode.isBlank()) {
            return new PublicResumeLookupResult(Status.NOT_FOUND, null);
        }
        String sql = "SELECT sr.is_deleted, sr.deleted_at, sr.pdf_file_path, sr.pdf_status "
                   + "FROM saved_resumes sr JOIN users u ON sr.user_id = u.id "
                   + "WHERE u.username = ? AND sr.public_code = ?";
        log.debug("findPublicResumeStatus: user={}, code={}", username, publicCode);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, publicCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    log.debug("findPublicResumeStatus: no row found");
                    return new PublicResumeLookupResult(Status.NOT_FOUND, null);
                }
                boolean isDeleted = rs.getBoolean("is_deleted");
                java.sql.Timestamp deletedAt = rs.getTimestamp("deleted_at");

                if (isDeleted || deletedAt != null) {
                    log.debug("findPublicResumeStatus: resume is deleted (isDeleted={}, deletedAt={})",
                            isDeleted, deletedAt);
                    return new PublicResumeLookupResult(Status.DELETED, null);
                }
                String pdfPath = rs.getString("pdf_file_path");
                String pdfStatus = rs.getString("pdf_status");
                if (pdfPath == null || !"READY".equals(pdfStatus)) {
                    log.debug("findPublicResumeStatus: PDF not ready (path={}, status={})", pdfPath, pdfStatus);
                    return new PublicResumeLookupResult(Status.MISSING_FILE, null);
                }
                log.debug("findPublicResumeStatus: ACTIVE — path={}", pdfPath);
                return new PublicResumeLookupResult(Status.ACTIVE, pdfPath);
            }
        } catch (SQLException e) {
            log.error("Error looking up public resume for user={} code={}", username, publicCode, e);
            throw new RuntimeException("Database error looking up public resume", e);
        }
    }

    /** Feature 008: Find PDF file path by username + public code for public route. */
    public String findPdfPathByUsernameAndCode(String username, String publicCode) {
        if (username == null || username.isBlank() || publicCode == null || publicCode.isBlank()) return null;
        String sql = "SELECT sr.pdf_file_path FROM saved_resumes sr JOIN users u ON sr.user_id = u.id "
                   + "WHERE u.username = ? AND sr.public_code = ? AND sr.is_deleted = FALSE "
                   + "AND sr.pdf_file_path IS NOT NULL AND sr.pdf_status = 'READY'";
        log.debug("findPdfPathByUsernameAndCode: user={}, code={}", username, publicCode);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, publicCode);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasRow = rs.next();
                log.debug("findPdfPathByUsernameAndCode result: hasRow={}", hasRow);
                if (hasRow) {
                    String path = rs.getString("pdf_file_path");
                    log.debug("findPdfPathByUsernameAndCode path: {}", path);
                    return path;
                }
                return null;
            }
        } catch (SQLException e) {
            log.error("Error finding PDF for user={} code={}", username, publicCode, e);
            throw new RuntimeException("Database error", e);
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
                    row.pdfStatus = rs.getString("pdf_status");
                    row.pdfPageCount = rs.getObject("pdf_page_count") != null ? rs.getInt("pdf_page_count") : null;
                    row.username = rs.getString("username");
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
        public String username;
    }

    /** Update PDF generation result on a saved resume (Feature 008). */
    public void updatePdfMetadata(long savedResumeId, String pdfStatus, String pdfFilePath,
                                   Integer pdfPageCount, String renderProfile,
                                   String errorCode, String errorMessage) {
        try (Connection conn = dataSource.getConnection()) {
            updatePdfMetadata(conn, savedResumeId, pdfStatus, pdfFilePath, pdfPageCount,
                    renderProfile, errorCode, errorMessage);
        } catch (SQLException e) {
            log.error("Error updating PDF metadata for resume: {}", savedResumeId, e);
            throw new RuntimeException("Failed to update PDF metadata", e);
        }
    }

    /** Connection-aware overload for transaction composition (Phase 22C). */
    public void updatePdfMetadata(Connection conn, long savedResumeId, String pdfStatus,
                                   String pdfFilePath, Integer pdfPageCount,
                                   String renderProfile, String errorCode,
                                   String errorMessage) {
        String sql = "UPDATE saved_resumes SET pdf_status = ?, pdf_file_path = ?, pdf_page_count = ?, "
                   + "pdf_render_profile = ?, pdf_generation_error_code = ?, "
                   + "pdf_generation_error_message = ?, pdf_generated_at = NOW() WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            throw new RuntimeException("Failed to update PDF metadata", e);
        }
    }
}
