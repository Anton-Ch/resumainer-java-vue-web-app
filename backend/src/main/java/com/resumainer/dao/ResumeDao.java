package com.resumainer.dao;

import com.resumainer.model.SavedResume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DAO for the 'saved_resumes' table.
 * <p>
 * Uses the custom SimpleConnectionPool via injected {@link DataSource}.
 * All queries use PreparedStatement (Constitution IV).
 * All list queries filter by user_id (SEC-002) and exclude soft-deleted
 * records via deleted_at IS NULL (SEC-003). Sort fields are validated
 * against a whitelist to prevent SQL injection (SEC-001).
 */
@Repository
public class ResumeDao {

    private static final Logger log = LoggerFactory.getLogger(ResumeDao.class);

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "resume_title", "vacancy", "company", "language", "adaptation_level", "created_at"
    );

    private final DataSource dataSource;

    public ResumeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Paginated SELECT with optional search/filter/sort.
     */
    public List<SavedResume> findByUserId(UUID userId, String search,
                                          String language, String adaptationLevel,
                                          String createdDate, String dateFrom,
                                          String dateTo, String sortField,
                                          String sortDir, int page, int size) {
        String sortColumn = validateSortField(sortField);
        String direction = validateSortDir(sortDir);

        SqlBuilder sql = new SqlBuilder("WHERE user_id = ? AND deleted_at IS NULL");
        sql.addParam(userId);

        if (isNotBlank(search)) {
            sql.append(" AND (LOWER(resume_title) LIKE ? OR LOWER(vacancy) LIKE ? OR LOWER(company) LIKE ?)");
            String p = "%" + search.toLowerCase().trim() + "%";
            sql.addParam(p).addParam(p).addParam(p);
        }

        if (isNotBlank(language)) {
            sql.appendInClause("AND language IN", language.split(","));
        }

        if (isNotBlank(adaptationLevel)) {
            sql.appendInClause("AND adaptation_level IN", adaptationLevel.split(","));
        }

        if (isNotBlank(createdDate)) {
            sql.append(" AND created_at = ?");
            sql.addParam(Date.valueOf(createdDate.trim()));
        } else {
            if (isNotBlank(dateFrom)) {
                sql.append(" AND created_at >= ?");
                sql.addParam(Date.valueOf(dateFrom.trim()));
            }
            if (isNotBlank(dateTo)) {
                sql.append(" AND created_at <= ?");
                sql.addParam(Date.valueOf(dateTo.trim()));
            }
        }

        int offset = page * size;
        String query = "SELECT id, resume_title, vacancy, company, language, adaptation_level, "
                + "created_at, public_url, pdf_url, cover_letter "
                + "FROM saved_resumes " + sql.getWhere()
                + " ORDER BY " + sortColumn + " " + direction
                + " LIMIT ? OFFSET ?";

        log.debug("findByUserId: userId={}, page={}, size={}", userId, page, size);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);
            stmt.setInt(sql.getParamCount() + 1, size);
            stmt.setInt(sql.getParamCount() + 2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<SavedResume> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }

        } catch (SQLException e) {
            log.error("Error finding resumes for user: {}", userId, e);
            throw new RuntimeException("Database error finding resumes", e);
        }
    }

    /**
     * COUNT query with the same filters.
     */
    public long countByUserId(UUID userId, String search, String language,
                              String adaptationLevel, String createdDate,
                              String dateFrom, String dateTo) {
        SqlBuilder sql = new SqlBuilder("WHERE user_id = ? AND deleted_at IS NULL");
        sql.addParam(userId);

        if (isNotBlank(search)) {
            sql.append(" AND (LOWER(resume_title) LIKE ? OR LOWER(vacancy) LIKE ? OR LOWER(company) LIKE ?)");
            String p = "%" + search.toLowerCase().trim() + "%";
            sql.addParam(p).addParam(p).addParam(p);
        }

        if (isNotBlank(language)) {
            sql.appendInClause("AND language IN", language.split(","));
        }

        if (isNotBlank(adaptationLevel)) {
            sql.appendInClause("AND adaptation_level IN", adaptationLevel.split(","));
        }

        if (isNotBlank(createdDate)) {
            sql.append(" AND created_at = ?");
            sql.addParam(Date.valueOf(createdDate.trim()));
        } else {
            if (isNotBlank(dateFrom)) {
                sql.append(" AND created_at >= ?");
                sql.addParam(Date.valueOf(dateFrom.trim()));
            }
            if (isNotBlank(dateTo)) {
                sql.append(" AND created_at <= ?");
                sql.addParam(Date.valueOf(dateTo.trim()));
            }
        }

        String query = "SELECT COUNT(*) FROM saved_resumes " + sql.getWhere();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }

        } catch (SQLException e) {
            log.error("Error counting resumes for user: {}", userId, e);
            throw new RuntimeException("Database error counting resumes", e);
        }
    }

    /**
     * Find single resume by ID with owner check.
     */
    public SavedResume findById(long id, UUID userId) {
        String sql = "SELECT id, resume_title, vacancy, company, language, adaptation_level, "
                + "created_at, public_url, pdf_url, cover_letter "
                + "FROM saved_resumes WHERE id = ? AND user_id = ? AND deleted_at IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.setObject(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            log.error("Error finding resume by id: {}", id, e);
            throw new RuntimeException("Database error finding resume", e);
        }
    }

    /**
     * Soft-delete a resume by setting deleted_at (owner-protected).
     */
    public boolean softDelete(long id, UUID userId) {
        String sql = "UPDATE saved_resumes SET deleted_at = NOW() WHERE id = ? AND user_id = ? AND deleted_at IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.setObject(2, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("Error soft-deleting resume: {}", id, e);
            throw new RuntimeException("Database error deleting resume", e);
        }
    }

    private SavedResume mapRow(ResultSet rs) throws SQLException {
        SavedResume r = new SavedResume();
        r.setId(rs.getLong("id"));
        r.setResumeTitle(rs.getString("resume_title"));
        r.setVacancy(rs.getString("vacancy"));
        r.setCompany(rs.getString("company"));
        r.setLanguage(rs.getString("language"));
        r.setAdaptationLevel(rs.getString("adaptation_level"));
        Date cd = rs.getDate("created_at");
        r.setCreatedAt(cd != null ? cd.toString() : null);
        r.setPublicUrl(rs.getString("public_url"));
        r.setPdfUrl(rs.getString("pdf_url"));
        r.setCoverLetter(rs.getString("cover_letter"));
        return r;
    }

    private String validateSortField(String field) {
        if (field == null || field.isBlank()) return "created_at";
        String f = field.trim().toLowerCase();
        if (!ALLOWED_SORT_FIELDS.contains(f)) {
            throw new IllegalArgumentException("Invalid sort field: " + field
                    + ". Allowed: " + ALLOWED_SORT_FIELDS);
        }
        return f;
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
                else if (p instanceof Date) stmt.setDate(i + 1, (Date) p);
                else stmt.setObject(i + 1, p);
            }
        }
    }
}
