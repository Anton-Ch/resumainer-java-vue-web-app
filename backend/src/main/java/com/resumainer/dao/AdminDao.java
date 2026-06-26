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
            + "sr.pdf_file_path IS NOT NULL AND sr.pdf_file_path <> '' AS pdf_file_present, "
            + "sr.html_file_path IS NOT NULL AND sr.html_file_path <> '' AS html_file_present";

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
        row.pdfFilePresent = rs.getBoolean("pdf_file_present");
        row.htmlFilePresent = rs.getBoolean("html_file_present");
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

    // --- Phase 4: Admin users listing ---

    private static final Set<String> ALLOWED_USER_SORT_FIELDS = Set.of(
            "fullname", "username", "email",
            "role", "status", "generationpermission",
            "rights", "resumescount", "createdat"
    );

    private static final Map<String, String> USER_SORT_MAP = Map.of(
            "fullname", "cd.full_name",
            "username", "u.username",
            "email", "u.email",
            "role", "r.code",
            "status", "us.code",
            "generationpermission", "up.code",
            "rights", "u.is_privileged",
            "resumescount", "resumes_count",
            "createdat", "u.created_at"
    );

    private static final String USER_SELECT_COLUMNS =
            "u.id, cd.full_name, u.username, u.email, "
            + "r.code AS role_code, r.name AS role_name, "
            + "us.code AS status_code, us.name AS status_name, "
            + "up.code AS permission_code, up.name AS permission_name, "
            + "u.is_privileged, "
            + "COUNT(sr.id) AS resumes_count, "
            + "u.created_at";

    private static final String USER_FROM_JOIN =
            "FROM users u "
            + "LEFT JOIN contact_detail cd ON cd.user_id = u.id "
            + "JOIN role r ON r.id = u.role_id "
            + "JOIN user_status us ON us.id = u.status_id "
            + "JOIN user_permission up ON up.id = u.permission_id "
            + "LEFT JOIN saved_resumes sr ON sr.user_id = u.id AND sr.is_deleted = FALSE";

    /**
     * Paginated admin users listing with search, filters, date range, and sort.
     */
    public List<AdminUserRow> findUsers(String search,
                                         String role, String status,
                                         String permission, String rights,
                                         String createdFrom, String createdTo,
                                         String sortField, String sortDir,
                                         int page, int size) {
        String sortColumn = validateUserSortField(sortField);
        String direction = validateSortDir(sortDir);

        SqlBuilder sql = new SqlBuilder("WHERE u.is_deleted = FALSE");

        if (isNotBlank(search)) {
            sql.append(" AND (LOWER(u.username) LIKE ? OR LOWER(u.email) LIKE ? "
                      + "OR LOWER(cd.full_name) LIKE ?)");
            String p = "%" + search.toLowerCase().trim() + "%";
            sql.addParam(p).addParam(p).addParam(p);
        }

        if (isNotBlank(role) && !"ALL".equalsIgnoreCase(role.trim())) {
            sql.appendInClause("AND r.code IN", new String[]{role});
        }

        if (isNotBlank(status) && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.appendInClause("AND us.code IN", new String[]{status});
        }

        if (isNotBlank(permission) && !"ALL".equalsIgnoreCase(permission.trim())) {
            sql.appendInClause("AND up.code IN", new String[]{permission});
        }

        if ("PRIVILEGED".equalsIgnoreCase(rights)) {
            sql.append(" AND u.is_privileged = TRUE");
        } else if ("NON_PRIVILEGED".equalsIgnoreCase(rights)) {
            sql.append(" AND u.is_privileged = FALSE");
        }

        if (isNotBlank(createdFrom)) {
            sql.append(" AND u.created_at >= ?::date");
            sql.addParam(createdFrom.trim());
        }

        if (isNotBlank(createdTo)) {
            sql.append(" AND u.created_at < (?::date + INTERVAL '1 day')");
            sql.addParam(createdTo.trim());
        }

        int offset = page * size;
        String groupBy = " GROUP BY u.id, cd.full_name, u.username, u.email, "
                + "r.code, r.name, us.code, us.name, up.code, up.name, "
                + "u.is_privileged, u.created_at";

        String query = "SELECT " + USER_SELECT_COLUMNS + " "
                + USER_FROM_JOIN + " "
                + sql.getWhere()
                + groupBy
                + " ORDER BY " + sortColumn + " " + direction
                + " LIMIT ? OFFSET ?";

        log.debug("findUsers: page={}, size={}, search={}, role={}, status={}, "
                + "permission={}, rights={}, from={}, to={}, sort={} {}",
                page, size, search, role, status, permission, rights,
                createdFrom, createdTo, sortField, direction);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);
            stmt.setInt(sql.getParamCount() + 1, size);
            stmt.setInt(sql.getParamCount() + 2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<AdminUserRow> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapUserRow(rs));
                }
                return results;
            }

        } catch (SQLException e) {
            log.error("Error finding admin users", e);
            throw new RuntimeException("Database error finding admin users", e);
        }
    }

    /**
     * COUNT query with the same filters as findUsers (for pagination).
     */
    public long countUsers(String search, String role, String status,
                           String permission, String rights,
                           String createdFrom, String createdTo) {
        SqlBuilder sql = new SqlBuilder("WHERE u.is_deleted = FALSE");

        if (isNotBlank(search)) {
            sql.append(" AND (LOWER(u.username) LIKE ? OR LOWER(u.email) LIKE ? "
                      + "OR LOWER(cd.full_name) LIKE ?)");
            String p = "%" + search.toLowerCase().trim() + "%";
            sql.addParam(p).addParam(p).addParam(p);
        }

        if (isNotBlank(role) && !"ALL".equalsIgnoreCase(role.trim())) {
            sql.appendInClause("AND r.code IN", new String[]{role});
        }

        if (isNotBlank(status) && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.appendInClause("AND us.code IN", new String[]{status});
        }

        if (isNotBlank(permission) && !"ALL".equalsIgnoreCase(permission.trim())) {
            sql.appendInClause("AND up.code IN", new String[]{permission});
        }

        if ("PRIVILEGED".equalsIgnoreCase(rights)) {
            sql.append(" AND u.is_privileged = TRUE");
        } else if ("NON_PRIVILEGED".equalsIgnoreCase(rights)) {
            sql.append(" AND u.is_privileged = FALSE");
        }

        if (isNotBlank(createdFrom)) {
            sql.append(" AND u.created_at >= ?::date");
            sql.addParam(createdFrom.trim());
        }

        if (isNotBlank(createdTo)) {
            sql.append(" AND u.created_at < (?::date + INTERVAL '1 day')");
            sql.addParam(createdTo.trim());
        }

        String query = "SELECT COUNT(DISTINCT u.id) " + USER_FROM_JOIN + " " + sql.getWhere();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }

        } catch (SQLException e) {
            log.error("Error counting admin users", e);
            throw new RuntimeException("Database error counting admin users", e);
        }
    }

    private AdminUserRow mapUserRow(ResultSet rs) throws SQLException {
        AdminUserRow row = new AdminUserRow();
        row.id = (UUID) rs.getObject("id");
        row.fullName = rs.getString("full_name");
        row.username = rs.getString("username");
        row.email = rs.getString("email");
        row.roleCode = rs.getString("role_code");
        row.roleName = rs.getString("role_name");
        row.statusCode = rs.getString("status_code");
        row.statusName = rs.getString("status_name");
        row.permissionCode = rs.getString("permission_code");
        row.permissionName = rs.getString("permission_name");
        row.isPrivileged = rs.getBoolean("is_privileged");
        row.resumesCount = rs.getLong("resumes_count");
        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        row.createdAt = ts != null ? ts.toLocalDateTime() : null;
        return row;
    }

    private String validateUserSortField(String field) {
        if (field == null || field.isBlank()) return "u.created_at";
        String f = field.trim().toLowerCase();
        if (!ALLOWED_USER_SORT_FIELDS.contains(f)) {
            throw new IllegalArgumentException("Invalid sort field: " + field
                    + ". Allowed: " + ALLOWED_USER_SORT_FIELDS);
        }
        return USER_SORT_MAP.getOrDefault(f, "u.created_at");
    }

    // --- Phase 5: Admin user details ---

    /**
     * Finds a single user's details for the admin user details page.
     * Returns null if user is not found or soft-deleted.
     */
    public AdminUserDetailsRow findUserDetails(UUID userId) {
        String sql = "SELECT u.id, u.username, u.email AS account_email, "
                + "r.code AS role_code, r.name AS role_name, "
                + "us.code AS status_code, us.name AS status_name, "
                + "up.code AS permission_code, up.name AS permission_name, "
                + "u.is_privileged, "
                + "dl.code AS default_lang_code, dl.name AS default_lang_name, "
                + "sl.code AS secondary_lang_code, sl.name AS secondary_lang_name, "
                + "u.created_at, u.updated_at, "
                + "cd.full_name, cd.professional_title, cd.phone, "
                + "cd.resume_email, cd.location, "
                + "cd.linkedin_url, cd.portfolio_url, cd.telegram, cd.whatsapp, "
                + "api.skills, api.languages AS api_languages, "
                + "api.professional_aspirations, api.achievements, "
                + "api.general_information, "
                + "api.ready_for_relocation, api.ready_for_business_trips, "
                + "api.date_of_birth, api.citizenship "
                + "FROM users u "
                + "JOIN role r ON r.id = u.role_id "
                + "JOIN user_status us ON us.id = u.status_id "
                + "JOIN user_permission up ON up.id = u.permission_id "
                + "LEFT JOIN language dl ON dl.id = u.default_language_id "
                + "LEFT JOIN language sl ON sl.id = u.secondary_language_id "
                + "LEFT JOIN contact_detail cd ON cd.user_id = u.id "
                + "LEFT JOIN additional_profile_info api ON api.user_id = u.id "
                + "WHERE u.id = ? AND u.is_deleted = FALSE";

        log.debug("findUserDetails: userId={}", userId);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUserDetailsRow(rs) : null;
            }

        } catch (SQLException e) {
            log.error("Error finding user details: {}", userId, e);
            throw new RuntimeException("Database error finding user details", e);
        }
    }

    private AdminUserDetailsRow mapUserDetailsRow(ResultSet rs) throws SQLException {
        AdminUserDetailsRow row = new AdminUserDetailsRow();
        row.id = (UUID) rs.getObject("id");
        row.username = rs.getString("username");
        row.accountEmail = rs.getString("account_email");
        row.roleCode = rs.getString("role_code");
        row.roleName = rs.getString("role_name");
        row.statusCode = rs.getString("status_code");
        row.statusName = rs.getString("status_name");
        row.permissionCode = rs.getString("permission_code");
        row.permissionName = rs.getString("permission_name");
        row.isPrivileged = rs.getBoolean("is_privileged");
        row.defaultLanguageCode = rs.getString("default_lang_code");
        row.defaultLanguageName = rs.getString("default_lang_name");
        row.secondaryLanguageCode = rs.getString("secondary_lang_code");
        row.secondaryLanguageName = rs.getString("secondary_lang_name");
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        row.createdAt = createdAt != null ? createdAt.toLocalDateTime() : null;
        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        row.updatedAt = updatedAt != null ? updatedAt.toLocalDateTime() : null;

        // Contact section
        row.fullName = rs.getString("full_name");
        row.professionalTitle = rs.getString("professional_title");
        row.phone = rs.getString("phone");
        row.resumeEmail = rs.getString("resume_email");
        row.location = rs.getString("location");
        row.linkedinUrl = rs.getString("linkedin_url");
        row.portfolioUrl = rs.getString("portfolio_url");
        row.telegram = rs.getString("telegram");
        row.whatsapp = rs.getString("whatsapp");

        // Additional info section (no photo_file_path per security rules)
        row.skills = rs.getString("skills");
        row.apiLanguages = rs.getString("api_languages");
        row.professionalAspirations = rs.getString("professional_aspirations");
        row.achievements = rs.getString("achievements");
        row.generalInformation = rs.getString("general_information");
        row.readyForRelocation = rs.getString("ready_for_relocation");
        row.readyForBusinessTrips = rs.getString("ready_for_business_trips");
        java.sql.Date dob = rs.getDate("date_of_birth");
        row.dateOfBirth = dob != null ? dob.toLocalDate() : null;
        row.citizenship = rs.getString("citizenship");
        return row;
    }

    // --- Phase 6: Access update and user soft-delete ---

    /**
     * Checks if a user exists and is not deleted (for access update / soft-delete validation).
     */
    public boolean existsAndNotDeleted(UUID userId) {
        String sql = "SELECT 1 FROM users WHERE id = ? AND is_deleted = FALSE";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Error checking user existence: {}", userId, e);
            throw new RuntimeException("Database error checking user", e);
        }
    }

    /**
     * Updates user access account fields: role, status, permission, is_privileged, updated_at.
     * Uses resolved lookup IDs, not hardcoded IDs.
     */
    public boolean updateUserAccess(UUID userId, Long roleId, Long statusId,
                                     Long permissionId, boolean isPrivileged) {
        String sql = "UPDATE users SET role_id = ?, status_id = ?, permission_id = ?, "
                + "is_privileged = ?, updated_at = NOW() WHERE id = ? AND is_deleted = FALSE";

        log.debug("updateUserAccess: userId={}, roleId={}, statusId={}, permissionId={}, isPrivileged={}",
                userId, roleId, statusId, permissionId, isPrivileged);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, roleId);
            stmt.setLong(2, statusId);
            stmt.setLong(3, permissionId);
            stmt.setBoolean(4, isPrivileged);
            stmt.setObject(5, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error updating user access: {}", userId, e);
            throw new RuntimeException("Database error updating user access", e);
        }
    }

    /**
     * Finds a role ID by its code.
     */
    public Long findRoleIdByCode(String code) {
        String sql = "SELECT id FROM role WHERE code = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        } catch (SQLException e) {
            log.error("Error finding role by code: {}", code, e);
            throw new RuntimeException("Database error finding role", e);
        }
    }

    /**
     * Finds a user status ID by its code.
     */
    public Long findStatusIdByCode(String code) {
        String sql = "SELECT id FROM user_status WHERE code = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        } catch (SQLException e) {
            log.error("Error finding status by code: {}", code, e);
            throw new RuntimeException("Database error finding status", e);
        }
    }

    /**
     * Finds a user permission ID by its code.
     */
    public Long findPermissionIdByCode(String code) {
        String sql = "SELECT id FROM user_permission WHERE code = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        } catch (SQLException e) {
            log.error("Error finding permission by code: {}", code, e);
            throw new RuntimeException("Database error finding permission", e);
        }
    }

    /**
     * Returns current role code and status code for a user (for self-protection checks).
     */
    public UserAccessState findUserAccessState(UUID userId) {
        String sql = "SELECT r.code AS role_code, us.code AS status_code "
                + "FROM users u JOIN role r ON r.id = u.role_id "
                + "JOIN user_status us ON us.id = u.status_id "
                + "WHERE u.id = ? AND u.is_deleted = FALSE";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                UserAccessState state = new UserAccessState();
                state.roleCode = rs.getString("role_code");
                state.statusCode = rs.getString("status_code");
                return state;
            }
        } catch (SQLException e) {
            log.error("Error finding user access state: {}", userId, e);
            throw new RuntimeException("Database error finding user access state", e);
        }
    }

    public static class UserAccessState {
        public String roleCode;
        public String statusCode;
    }

    /**
     * Transactional user soft-delete cascade.
     * Handles its own connection and transaction lifecycle.
     */
    public void adminSoftDeleteUser(UUID targetUserId) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // 1. Mark user deleted, set status to BLOCKED, update updated_at
            String updateUser = "UPDATE users SET is_deleted = TRUE, deleted_at = NOW(), "
                    + "status_id = (SELECT id FROM user_status WHERE code = 'BLOCKED'), "
                    + "updated_at = NOW() WHERE id = ? AND is_deleted = FALSE";
            try (PreparedStatement stmt = conn.prepareStatement(updateUser)) {
                stmt.setObject(1, targetUserId);
                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return; // user not found or already deleted
                }
            }

            // 2. Soft-delete saved resumes
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE saved_resumes SET is_deleted = TRUE, deleted_at = NOW() "
                    + "WHERE user_id = ? AND is_deleted = FALSE")) {
                stmt.setObject(1, targetUserId);
                stmt.executeUpdate();
            }

            // 3. Soft-delete work_experience
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE work_experience SET is_deleted = TRUE, deleted_at = NOW() "
                    + "WHERE user_id = ? AND is_deleted = FALSE")) {
                stmt.setObject(1, targetUserId);
                stmt.executeUpdate();
            }

            // 4. Soft-delete education
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE education SET is_deleted = TRUE, deleted_at = NOW() "
                    + "WHERE user_id = ? AND is_deleted = FALSE")) {
                stmt.setObject(1, targetUserId);
                stmt.executeUpdate();
            }

            // 5. Soft-delete project
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE project SET is_deleted = TRUE, deleted_at = NOW() "
                    + "WHERE user_id = ? AND is_deleted = FALSE")) {
                stmt.setObject(1, targetUserId);
                stmt.executeUpdate();
            }

            // 6. Soft-delete course_certificate
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE course_certificate SET is_deleted = TRUE, deleted_at = NOW() "
                    + "WHERE user_id = ? AND is_deleted = FALSE")) {
                stmt.setObject(1, targetUserId);
                stmt.executeUpdate();
            }

            // contact_detail and additional_profile_info intentionally skipped:
            // they do not have is_deleted/deleted_at columns

            conn.commit();
            log.debug("Admin soft-deleted user with cascade: userId={}", targetUserId);

        } catch (SQLException e) {
            log.error("Error in user soft-delete transaction for user: {}", targetUserId, e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException re) {
                    log.error("Rollback failed for user soft-delete: {}", targetUserId, re);
                }
            }
            throw new RuntimeException("Database error deleting user", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {
                    log.error("Error closing connection after user soft-delete", e);
                }
            }
        }
    }

    /**
     * Lightweight row representation for admin user details.
     * Contains all fields for account, contacts, and additional info sections.
     */
    public static class AdminUserDetailsRow {
        public UUID id;
        public String username;
        public String accountEmail;
        public String roleCode;
        public String roleName;
        public String statusCode;
        public String statusName;
        public String permissionCode;
        public String permissionName;
        public boolean isPrivileged;
        public String defaultLanguageCode;
        public String defaultLanguageName;
        public String secondaryLanguageCode;
        public String secondaryLanguageName;
        public java.time.LocalDateTime createdAt;
        public java.time.LocalDateTime updatedAt;

        // Contact section
        public String fullName;
        public String professionalTitle;
        public String phone;
        public String resumeEmail;
        public String location;
        public String linkedinUrl;
        public String portfolioUrl;
        public String telegram;
        public String whatsapp;

        // Additional info section (safe fields only, no photo_file_path)
        public String skills;
        public String apiLanguages;
        public String professionalAspirations;
        public String achievements;
        public String generalInformation;
        public String readyForRelocation;
        public String readyForBusinessTrips;
        public java.time.LocalDate dateOfBirth;
        public String citizenship;
    }

    /**
     * Lightweight row representation for admin user listing.
     */
    public static class AdminUserRow {
        public UUID id;
        public String fullName;
        public String username;
        public String email;
        public String roleCode;
        public String roleName;
        public String statusCode;
        public String statusName;
        public String permissionCode;
        public String permissionName;
        public boolean isPrivileged;
        public long resumesCount;
        public java.time.LocalDateTime createdAt;
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
