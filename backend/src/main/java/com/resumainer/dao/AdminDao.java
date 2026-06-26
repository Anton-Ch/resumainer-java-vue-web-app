package com.resumainer.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Data access for admin-specific cross-user queries.
 * <p>
 * Provides dashboard aggregate counts, paginated admin resume listing,
 * and admin user queries. All queries use PreparedStatement (Constitution IV).
 */
@Repository
public class AdminDao {

    private static final Logger log = LoggerFactory.getLogger(AdminDao.class);

    private static final Set<String> ALLOWED_RESUME_SORT_FIELDS = Set.of(
            "resumetitle", "vacancytitle", "companyname",
            "language", "adaptationlevel", "createdat",
            "ownerusername", "owneremail", "ownerfullname"
    );

    private static final Map<String, String> RESUME_SORT_MAP = Map.of(
            "resumetitle", "sr.resume_title",
            "vacancytitle", "sr.vacancy",
            "companyname", "sr.company",
            "language", "sr.language",
            "adaptationlevel", "sr.adaptation_level",
            "createdat", "sr.created_at",
            "ownerusername", "u.username",
            "owneremail", "u.email",
            "ownerfullname", "cd.full_name"
    );

    private static final String RESUME_SELECT_COLUMNS =
            "sr.id, sr.user_id, u.username, u.email, cd.full_name, "
            + "sr.resume_title, sr.vacancy, sr.company, "
            + "sr.language, l.name AS language_name, sr.adaptation_level, sr.created_at, "
            + "sr.public_code, sr.public_url_link, "
            + "sr.cover_letter, sr.pdf_status, "
            + "sr.pdf_file_path, sr.html_file_path";

    private static final String RESUME_FROM_JOIN =
            "FROM saved_resumes sr "
            + "JOIN users u ON sr.user_id = u.id "
            + "LEFT JOIN contact_detail cd ON cd.user_id = u.id "
            + "LEFT JOIN language l ON l.code = sr.language";

    private final DataSource dataSource;

    public AdminDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns total number of non-deleted users.
     */
    public long countNonDeletedUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_deleted = FALSE";
        log.debug("countNonDeletedUsers");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getLong(1) : 0;

        } catch (SQLException e) {
            log.error("Error counting non-deleted users", e);
            throw new RuntimeException("Database error counting users", e);
        }
    }

    /**
     * Returns total number of non-deleted saved resumes.
     */
    public long countNonDeletedResumes() {
        String sql = "SELECT COUNT(*) FROM saved_resumes WHERE is_deleted = FALSE";
        log.debug("countNonDeletedResumes");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getLong(1) : 0;

        } catch (SQLException e) {
            log.error("Error counting non-deleted resumes", e);
            throw new RuntimeException("Database error counting resumes", e);
        }
    }

    /**
     * Paginated admin resume listing with search, filters, date range, sort.
     */
    public List<AdminSavedResumeRow> findResumes(String search,
                                                  String language, String adaptationLevel,
                                                  String createdFrom, String createdTo,
                                                  String sortField, String sortDir,
                                                  int page, int size) {
        String sortColumn = validateResumeSortField(sortField);
        String direction = validateSortDir(sortDir);

        SqlBuilder sql = new SqlBuilder("WHERE sr.is_deleted = FALSE");

        if (isNotBlank(search)) {
            sql.append(" AND (LOWER(sr.resume_title) LIKE ? OR LOWER(sr.vacancy) LIKE ? "
                      + "OR LOWER(sr.company) LIKE ? OR LOWER(u.username) LIKE ? "
                      + "OR LOWER(u.email) LIKE ? OR LOWER(cd.full_name) LIKE ?)");
            String p = "%" + search.toLowerCase().trim() + "%";
            for (int i = 0; i < 6; i++) {
                sql.addParam(p);
            }
        }

        if (isNotBlank(language)) {
            sql.appendInClause("AND sr.language IN", language.split(","));
        }

        if (isNotBlank(adaptationLevel)) {
            sql.appendInClause("AND sr.adaptation_level IN", adaptationLevel.split(","));
        }

        if (isNotBlank(createdFrom)) {
            sql.append(" AND sr.created_at >= ?::date");
            sql.addParam(createdFrom.trim());
        }

        if (isNotBlank(createdTo)) {
            sql.append(" AND sr.created_at < (?::date + INTERVAL '1 day')");
            sql.addParam(createdTo.trim());
        }

        int offset = page * size;
        String query = "SELECT " + RESUME_SELECT_COLUMNS + " "
                + RESUME_FROM_JOIN + " "
                + sql.getWhere()
                + " ORDER BY " + sortColumn + " " + direction
                + " LIMIT ? OFFSET ?";

        log.debug("findResumes: page={}, size={}, search={}, language={}, "
                + "adaptationLevel={}, from={}, to={}, sort={} {}",
                page, size, search, language, adaptationLevel, createdFrom, createdTo,
                sortField, direction);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);
            stmt.setInt(sql.getParamCount() + 1, size);
            stmt.setInt(sql.getParamCount() + 2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<AdminSavedResumeRow> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapResumeRow(rs));
                }
                return results;
            }

        } catch (SQLException e) {
            log.error("Error finding admin resumes", e);
            throw new RuntimeException("Database error finding admin resumes", e);
        }
    }

    /**
     * COUNT query with the same filters as findResumes (for pagination).
     */
    public long countResumes(String search, String language, String adaptationLevel,
                             String createdFrom, String createdTo) {
        SqlBuilder sql = new SqlBuilder("WHERE sr.is_deleted = FALSE");

        if (isNotBlank(search)) {
            sql.append(" AND (LOWER(sr.resume_title) LIKE ? OR LOWER(sr.vacancy) LIKE ? "
                      + "OR LOWER(sr.company) LIKE ? OR LOWER(u.username) LIKE ? "
                      + "OR LOWER(u.email) LIKE ? OR LOWER(cd.full_name) LIKE ?)");
            String p = "%" + search.toLowerCase().trim() + "%";
            for (int i = 0; i < 6; i++) {
                sql.addParam(p);
            }
        }

        if (isNotBlank(language)) {
            sql.appendInClause("AND sr.language IN", language.split(","));
        }

        if (isNotBlank(adaptationLevel)) {
            sql.appendInClause("AND sr.adaptation_level IN", adaptationLevel.split(","));
        }

        if (isNotBlank(createdFrom)) {
            sql.append(" AND sr.created_at >= ?::date");
            sql.addParam(createdFrom.trim());
        }

        if (isNotBlank(createdTo)) {
            sql.append(" AND sr.created_at < (?::date + INTERVAL '1 day')");
            sql.addParam(createdTo.trim());
        }

        String query = "SELECT COUNT(*) " + RESUME_FROM_JOIN + " " + sql.getWhere();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }

        } catch (SQLException e) {
            log.error("Error counting admin resumes", e);
            throw new RuntimeException("Database error counting admin resumes", e);
        }
    }

    /**
     * Finds all saved resumes for a specific user (admin view).
     * Reuses the same query structure as findResumes but scoped to userId.
     */
    public List<AdminSavedResumeRow> findResumesByUserId(UUID userId,
                                                          String search,
                                                          String language,
                                                          String adaptationLevel,
                                                          String createdFrom,
                                                          String createdTo,
                                                          String sortField,
                                                          String sortDir,
                                                          int page, int size) {
        String sortColumn = validateResumeSortField(sortField);
        String direction = validateSortDir(sortDir);

        SqlBuilder sql = new SqlBuilder("WHERE sr.is_deleted = FALSE AND sr.user_id = ?");
        sql.addParam(userId);

        if (isNotBlank(search)) {
            sql.append(" AND (LOWER(sr.resume_title) LIKE ? OR LOWER(sr.vacancy) LIKE ? "
                      + "OR LOWER(sr.company) LIKE ?)");
            String p = "%" + search.toLowerCase().trim() + "%";
            sql.addParam(p).addParam(p).addParam(p);
        }

        if (isNotBlank(language)) {
            sql.appendInClause("AND sr.language IN", language.split(","));
        }

        if (isNotBlank(adaptationLevel)) {
            sql.appendInClause("AND sr.adaptation_level IN", adaptationLevel.split(","));
        }

        if (isNotBlank(createdFrom)) {
            sql.append(" AND sr.created_at >= ?::date");
            sql.addParam(createdFrom.trim());
        }

        if (isNotBlank(createdTo)) {
            sql.append(" AND sr.created_at < (?::date + INTERVAL '1 day')");
            sql.addParam(createdTo.trim());
        }

        int offset = page * size;
        String query = "SELECT " + RESUME_SELECT_COLUMNS + " "
                + RESUME_FROM_JOIN + " "
                + sql.getWhere()
                + " ORDER BY " + sortColumn + " " + direction
                + " LIMIT ? OFFSET ?";

        log.debug("findResumesByUserId: userId={}, page={}, size={}", userId, page, size);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);
            stmt.setInt(sql.getParamCount() + 1, size);
            stmt.setInt(sql.getParamCount() + 2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<AdminSavedResumeRow> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapResumeRow(rs));
                }
                return results;
            }

        } catch (SQLException e) {
            log.error("Error finding admin resumes for user: {}", userId, e);
            throw new RuntimeException("Database error finding admin resumes for user", e);
        }
    }

    public long countResumesByUserId(UUID userId, String search, String language,
                                      String adaptationLevel, String createdFrom,
                                      String createdTo) {
        SqlBuilder sql = new SqlBuilder("WHERE sr.is_deleted = FALSE AND sr.user_id = ?");
        sql.addParam(userId);

        if (isNotBlank(search)) {
            sql.append(" AND (LOWER(sr.resume_title) LIKE ? OR LOWER(sr.vacancy) LIKE ? "
                      + "OR LOWER(sr.company) LIKE ?)");
            String p = "%" + search.toLowerCase().trim() + "%";
            sql.addParam(p).addParam(p).addParam(p);
        }

        if (isNotBlank(language)) {
            sql.appendInClause("AND sr.language IN", language.split(","));
        }

        if (isNotBlank(adaptationLevel)) {
            sql.appendInClause("AND sr.adaptation_level IN", adaptationLevel.split(","));
        }

        if (isNotBlank(createdFrom)) {
            sql.append(" AND sr.created_at >= ?::date");
            sql.addParam(createdFrom.trim());
        }

        if (isNotBlank(createdTo)) {
            sql.append(" AND sr.created_at < (?::date + INTERVAL '1 day')");
            sql.addParam(createdTo.trim());
        }

        String query = "SELECT COUNT(*) " + RESUME_FROM_JOIN + " " + sql.getWhere();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }

        } catch (SQLException e) {
            log.error("Error counting admin resumes for user: {}", userId, e);
            throw new RuntimeException("Database error counting admin resumes for user", e);
        }
    }

    // --- Phase 3: Admin resume soft-delete ---

    /**
     * Admin-scoped soft-delete for any saved resume (not owner-scoped).
     *
     * @param resumeId the resume ID to delete
     * @return true if a row was updated, false if not found or already deleted
     */
    public boolean adminSoftDeleteResume(long resumeId) {
        String sql = "UPDATE saved_resumes SET is_deleted = TRUE, deleted_at = NOW() "
                + "WHERE id = ? AND is_deleted = FALSE";

        log.debug("adminSoftDeleteResume: resumeId={}", resumeId);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, resumeId);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) {
                log.debug("Admin soft-deleted resume: id={}", resumeId);
            } else {
                log.debug("Admin soft-delete skipped: resumeId={} (not found or already deleted)", resumeId);
            }
            return updated;

        } catch (SQLException e) {
            log.error("Error admin soft-deleting resume: {}", resumeId, e);
            throw new RuntimeException("Database error deleting resume", e);
        }
    }

    // --- Row mapping ---

    private AdminSavedResumeRow mapResumeRow(ResultSet rs) throws SQLException {
        AdminSavedResumeRow row = new AdminSavedResumeRow();
        row.id = rs.getLong("id");
        row.userId = (UUID) rs.getObject("user_id");
        row.username = rs.getString("username");
        row.email = rs.getString("email");
        row.fullName = rs.getString("full_name");
        row.resumeTitle = rs.getString("resume_title");
        row.vacancyTitle = rs.getString("vacancy");
        row.companyName = rs.getString("company");
        row.languageCode = rs.getString("language");
        row.languageName = rs.getString("language_name");
        row.adaptationLevel = rs.getString("adaptation_level");
        // created_at is DATE type, convert to string
        java.sql.Date cd = rs.getDate("created_at");
        row.createdAt = cd != null ? cd.toString() : null;
        row.publicCode = rs.getString("public_code");
        row.publicUrlLink = rs.getString("public_url_link");
        row.coverLetter = rs.getString("cover_letter");
        row.pdfStatus = rs.getString("pdf_status");
        // Determine if PDF and HTML files exist (not null paths)
        row.pdfFilePresent = rs.getString("pdf_file_path") != null;
        row.htmlFilePresent = rs.getString("html_file_path") != null;
        return row;
    }

    // --- Sort whitelist ---

    private String validateResumeSortField(String field) {
        if (field == null || field.isBlank()) return "sr.created_at";
        String f = field.trim().toLowerCase();
        if (!ALLOWED_RESUME_SORT_FIELDS.contains(f)) {
            throw new IllegalArgumentException("Invalid sort field: " + field
                    + ". Allowed: " + ALLOWED_RESUME_SORT_FIELDS);
        }
        return RESUME_SORT_MAP.getOrDefault(f, "sr.created_at");
    }

    private String validateSortDir(String dir) {
        if (dir == null || dir.isBlank()) return "desc";
        String d = dir.trim().toLowerCase();
        if (!"asc".equals(d) && !"desc".equals(d)) {
            throw new IllegalArgumentException("Invalid sort direction: " + dir);
        }
        return d;
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    // --- Result row DTO ---

    /**
     * Lightweight row representation for admin resume query results.
     */
    public static class AdminSavedResumeRow {
        public long id;
        public UUID userId;
        public String username;
        public String email;
        public String fullName;
        public String resumeTitle;
        public String vacancyTitle;
        public String companyName;
        public String languageCode;
        public String languageName;
        public String adaptationLevel;
        public String createdAt;
        public String publicCode;
        public String publicUrlLink;
        public String coverLetter;
        public String pdfStatus;
        public boolean pdfFilePresent;
        public boolean htmlFilePresent;
    }

    // --- SQL Builder ---

    private static class SqlBuilder {
        private final StringBuilder where = new StringBuilder();
        private final List<Object> params = new ArrayList<>();

        SqlBuilder(String base) {
            where.append(base);
        }

        SqlBuilder append(String clause) {
            where.append(" ").append(clause);
            return this;
        }

        SqlBuilder addParam(Object param) {
            params.add(param);
            return this;
        }

        void appendInClause(String prefix, String[] values) {
            where.append(" ").append(prefix).append(" (");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) where.append(", ");
                where.append("?");
                params.add(values[i].trim().toUpperCase());
            }
            where.append(")");
        }

        String getWhere() {
            return where.toString();
        }

        int getParamCount() {
            return params.size();
        }

        void setParameters(PreparedStatement stmt) throws SQLException {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) stmt.setString(i + 1, (String) p);
                else if (p instanceof UUID) stmt.setObject(i + 1, p);
                else if (p instanceof java.sql.Date) stmt.setDate(i + 1, (java.sql.Date) p);
                else stmt.setObject(i + 1, p);
            }
        }
    }
}
