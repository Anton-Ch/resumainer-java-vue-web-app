package com.resumainer.dao;

import com.resumainer.model.CourseCertificate;
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
 * DAO for the 'course_certificate' table (BIGSERIAL PK).
 * Supports server-side pagination with LIMIT/OFFSET, search, date range
 * filtering, and column sorting.
 * All queries use PreparedStatement (Constitution IV).
 * All SELECT queries filter by user_id (SEC-001) and is_deleted = FALSE (SEC-003).
 */
@Repository
public class CourseCertificateDao {

    private static final Logger log = LoggerFactory.getLogger(CourseCertificateDao.class);

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "provider", "start_date", "end_date", "course_focus",
            "coursename", "startdate", "enddate", "coursefocus"
    );

    private static final Set<String> SORT_FIELDS_NEEDING_MAP = Set.of(
            "coursename", "startdate", "enddate", "coursefocus"
    );

    private static final String SELECT_BASE =
            "SELECT id, user_id, name, provider, description, course_focus, "
            + "start_date, end_date, credential_url, "
            + "created_at, updated_at, is_deleted, deleted_at "
            + "FROM course_certificate";

    private static final String SELECT_BY_ID =
            SELECT_BASE + " WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private static final String INSERT =
            "INSERT INTO course_certificate (user_id, name, provider, description, course_focus, "
            + "start_date, end_date, credential_url) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String UPDATE =
            "UPDATE course_certificate SET name = ?, provider = ?, description = ?, course_focus = ?, "
            + "start_date = ?, end_date = ?, credential_url = ?, "
            + "updated_at = NOW() WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private static final String SOFT_DELETE =
            "UPDATE course_certificate SET is_deleted = TRUE, deleted_at = NOW() "
            + "WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private final DataSource dataSource;

    public CourseCertificateDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --- Auto-managed connection methods ---

    /**
     * Paginated SELECT with optional search, date filter, and sorting.
     */
    public List<CourseCertificate> findByUserId(UUID userId, String search,
                                                  String dateFrom, String dateTo,
                                                  String sortField, String sortDir,
                                                  int page, int size) {
        String sortColumn = validateSortField(sortField);
        String direction = validateSortDir(sortDir);

        SqlBuilder sql = new SqlBuilder("WHERE user_id = ? AND is_deleted = FALSE");
        sql.addParam(userId);

        if (isNotBlank(search) && search.trim().length() >= 3) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(provider) LIKE ? OR LOWER(course_focus) LIKE ?)");
            String p = "%" + search.trim().toLowerCase() + "%";
            sql.addParam(p).addParam(p).addParam(p);
        }

        if (isNotBlank(dateFrom)) {
            sql.append(" AND start_date >= ?");
            sql.addParam(Date.valueOf(dateFrom.trim()));
        }
        if (isNotBlank(dateTo)) {
            sql.append(" AND start_date <= ?");
            sql.addParam(Date.valueOf(dateTo.trim()));
        }

        int offset = page * size;
        String query = SELECT_BASE + " " + sql.getWhere()
                + " ORDER BY " + sortColumn + " " + direction
                + " LIMIT ? OFFSET ?";

        log.debug("findByUserId: userId={}, page={}, size={}", userId, page, size);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);
            stmt.setInt(sql.getParamCount() + 1, size);
            stmt.setInt(sql.getParamCount() + 2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                List<CourseCertificate> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }

        } catch (SQLException e) {
            log.error("Error finding courses for user: {}", userId, e);
            throw new RuntimeException("Database error finding courses", e);
        }
    }

    /**
     * COUNT query with the same filters (for pagination).
     */
    public long countByUserId(UUID userId, String search,
                              String dateFrom, String dateTo) {
        SqlBuilder sql = new SqlBuilder("WHERE user_id = ? AND is_deleted = FALSE");
        sql.addParam(userId);

        if (isNotBlank(search) && search.trim().length() >= 3) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(provider) LIKE ? OR LOWER(course_focus) LIKE ?)");
            String p = "%" + search.trim().toLowerCase() + "%";
            sql.addParam(p).addParam(p).addParam(p);
        }

        if (isNotBlank(dateFrom)) {
            sql.append(" AND start_date >= ?");
            sql.addParam(Date.valueOf(dateFrom.trim()));
        }
        if (isNotBlank(dateTo)) {
            sql.append(" AND start_date <= ?");
            sql.addParam(Date.valueOf(dateTo.trim()));
        }

        String query = "SELECT COUNT(*) FROM course_certificate " + sql.getWhere();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            sql.setParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }

        } catch (SQLException e) {
            log.error("Error counting courses for user: {}", userId, e);
            throw new RuntimeException("Database error counting courses", e);
        }
    }

    public CourseCertificate findById(long id, UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setLong(1, id);
            stmt.setObject(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            log.error("Error finding course by id: {}", id, e);
            throw new RuntimeException("Database error finding course", e);
        }
    }

    public CourseCertificate create(CourseCertificate course) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT)) {

            stmt.setObject(1, course.getUserId());
            stmt.setString(2, course.getName());
            stmt.setString(3, course.getProvider());
            stmt.setString(4, course.getDescription());
            stmt.setString(5, course.getCourseFocus());
            stmt.setDate(6, Date.valueOf(course.getStartDate()));
            setDateOrNull(stmt, 7, course.getEndDate());
            stmt.setString(8, course.getCredentialUrl());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    course.setId(rs.getLong("id"));
                }
            }
            log.debug("Course created: id={}, userId={}", course.getId(), course.getUserId());
            return course;

        } catch (SQLException e) {
            log.error("Error creating course for user: {}", course.getUserId(), e);
            throw new RuntimeException("Database error creating course", e);
        }
    }

    public void update(CourseCertificate course) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {

            stmt.setString(1, course.getName());
            stmt.setString(2, course.getProvider());
            stmt.setString(3, course.getDescription());
            stmt.setString(4, course.getCourseFocus());
            stmt.setDate(5, Date.valueOf(course.getStartDate()));
            setDateOrNull(stmt, 6, course.getEndDate());
            stmt.setString(7, course.getCredentialUrl());
            stmt.setLong(8, course.getId());
            stmt.setObject(9, course.getUserId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Course not found or access denied: id="
                        + course.getId());
            }
            log.debug("Course updated: id={}", course.getId());

        } catch (SQLException e) {
            log.error("Error updating course: {}", course.getId(), e);
            throw new RuntimeException("Database error updating course", e);
        }
    }

    public boolean softDelete(long id, UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SOFT_DELETE)) {

            stmt.setLong(1, id);
            stmt.setObject(2, userId);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) {
                log.debug("Course deleted: id={}", id);
            }
            return deleted;

        } catch (SQLException e) {
            log.error("Error deleting course: {}", id, e);
            throw new RuntimeException("Database error deleting course", e);
        }
    }

    // --- Row mapping ---

    private CourseCertificate mapRow(ResultSet rs) throws SQLException {
        CourseCertificate c = new CourseCertificate();
        c.setId(rs.getLong("id"));
        c.setUserId((UUID) rs.getObject("user_id"));
        c.setName(rs.getString("name"));
        c.setProvider(rs.getString("provider"));
        c.setDescription(rs.getString("description"));
        c.setCourseFocus(rs.getString("course_focus"));
        c.setStartDate(rs.getDate("start_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            c.setEndDate(endDate.toLocalDate());
        }
        c.setCredentialUrl(rs.getString("credential_url"));
        c.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            c.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        c.setDeleted(rs.getBoolean("is_deleted"));
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            c.setDeletedAt(deletedAt.toLocalDateTime());
        }
        return c;
    }

    // --- Helpers ---

    private String validateSortField(String field) {
        if (field == null || field.isBlank()) return "start_date";
        String f = field.trim().toLowerCase();
        if (!ALLOWED_SORT_FIELDS.contains(f)) {
            throw new IllegalArgumentException("Invalid sort field: " + field
                    + ". Allowed: " + ALLOWED_SORT_FIELDS);
        }
        // Map frontend camelCase field names to DB column names
        if (SORT_FIELDS_NEEDING_MAP.contains(f)) {
            switch (f) {
                case "coursename": return "name";
                case "startdate": return "start_date";
                case "enddate": return "end_date";
                case "coursefocus": return "course_focus";
                default: return f;
            }
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

    private void setDateOrNull(PreparedStatement stmt, int index, java.time.LocalDate date) throws SQLException {
        if (date != null) {
            stmt.setDate(index, Date.valueOf(date));
        } else {
            stmt.setNull(index, Types.DATE);
        }
    }

    /**
     * Internal SQL WHERE clause builder for dynamic filtering.
     */
    static class SqlBuilder {
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
