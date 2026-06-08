package com.resumainer.dao;

import com.resumainer.model.Education;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for the 'education' table (BIGSERIAL PK).
 * All queries use PreparedStatement (Constitution IV).
 * All SELECT queries filter by user_id (SEC-001) and is_deleted = FALSE (SEC-003).
 */
@Repository
public class EducationDao {

    private static final Logger log = LoggerFactory.getLogger(EducationDao.class);

    private static final String SELECT_BY_USER =
            "SELECT id, user_id, institution_name, degree, field_of_study, description, "
            + "start_date, end_date, is_current, location, gpa_grade, "
            + "created_at, updated_at, is_deleted, deleted_at "
            + "FROM education WHERE user_id = ? AND is_deleted = FALSE "
            + "ORDER BY start_date DESC, end_date DESC NULLS FIRST";

    private static final String SELECT_BY_ID =
            "SELECT id, user_id, institution_name, degree, field_of_study, description, "
            + "start_date, end_date, is_current, location, gpa_grade, "
            + "created_at, updated_at, is_deleted, deleted_at "
            + "FROM education WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private static final String INSERT =
            "INSERT INTO education (user_id, institution_name, degree, field_of_study, description, "
            + "start_date, end_date, is_current, location, gpa_grade) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String UPDATE =
            "UPDATE education SET institution_name = ?, degree = ?, field_of_study = ?, description = ?, "
            + "start_date = ?, end_date = ?, is_current = ?, location = ?, gpa_grade = ?, "
            + "updated_at = NOW() WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private static final String SOFT_DELETE =
            "UPDATE education SET is_deleted = TRUE, deleted_at = NOW() "
            + "WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private final DataSource dataSource;

    public EducationDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --- Auto-managed connection methods ---

    public List<Education> findByUserId(UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return findByUserId(userId, conn);
        } catch (SQLException e) {
            log.error("Error finding education for user: {}", userId, e);
            throw new RuntimeException("Database error finding education", e);
        }
    }

    public Education findById(long id, UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return findById(id, userId, conn);
        } catch (SQLException e) {
            log.error("Error finding education by id: {}", id, e);
            throw new RuntimeException("Database error finding education", e);
        }
    }

    public Education create(Education education) {
        try (Connection conn = dataSource.getConnection()) {
            return create(education, conn);
        } catch (SQLException e) {
            log.error("Error creating education for user: {}", education.getUserId(), e);
            throw new RuntimeException("Database error creating education", e);
        }
    }

    public void update(Education education) {
        try (Connection conn = dataSource.getConnection()) {
            update(education, conn);
        } catch (SQLException e) {
            log.error("Error updating education: {}", education.getId(), e);
            throw new RuntimeException("Database error updating education", e);
        }
    }

    public boolean softDelete(long id, UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return softDelete(id, userId, conn);
        } catch (SQLException e) {
            log.error("Error deleting education: {}", id, e);
            throw new RuntimeException("Database error deleting education", e);
        }
    }

    // --- Connection-accepting overloads (D10) ---

    public List<Education> findByUserId(UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Education> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        }
    }

    public Education findById(long id, UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setLong(1, id);
            stmt.setObject(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Education create(Education education, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setObject(1, education.getUserId());
            stmt.setString(2, education.getInstitutionName());
            stmt.setString(3, education.getDegree());
            stmt.setString(4, education.getFieldOfStudy());
            stmt.setString(5, education.getDescription());
            stmt.setDate(6, Date.valueOf(education.getStartDate()));
            setDateOrNull(stmt, 7, education.getEndDate());
            stmt.setBoolean(8, education.isCurrent());
            stmt.setString(9, education.getLocation());
            stmt.setString(10, education.getGpaGrade());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    education.setId(rs.getLong("id"));
                }
            }
            log.debug("Education created: id={}, userId={}", education.getId(), education.getUserId());
            return education;
        }
    }

    public void update(Education education, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            stmt.setString(1, education.getInstitutionName());
            stmt.setString(2, education.getDegree());
            stmt.setString(3, education.getFieldOfStudy());
            stmt.setString(4, education.getDescription());
            stmt.setDate(5, Date.valueOf(education.getStartDate()));
            setDateOrNull(stmt, 6, education.getEndDate());
            stmt.setBoolean(7, education.isCurrent());
            stmt.setString(8, education.getLocation());
            stmt.setString(9, education.getGpaGrade());
            stmt.setLong(10, education.getId());
            stmt.setObject(11, education.getUserId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Education not found or access denied: id="
                        + education.getId());
            }
            log.debug("Education updated: id={}", education.getId());
        }
    }

    public boolean softDelete(long id, UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SOFT_DELETE)) {
            stmt.setLong(1, id);
            stmt.setObject(2, userId);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) {
                log.debug("Education deleted: id={}", id);
            }
            return deleted;
        }
    }

    // --- Row mapping ---

    private Education mapRow(ResultSet rs) throws SQLException {
        Education e = new Education();
        e.setId(rs.getLong("id"));
        e.setUserId((UUID) rs.getObject("user_id"));
        e.setInstitutionName(rs.getString("institution_name"));
        e.setDegree(rs.getString("degree"));
        e.setFieldOfStudy(rs.getString("field_of_study"));
        e.setDescription(rs.getString("description"));
        e.setStartDate(rs.getDate("start_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            e.setEndDate(endDate.toLocalDate());
        }
        e.setCurrent(rs.getBoolean("is_current"));
        e.setLocation(rs.getString("location"));
        e.setGpaGrade(rs.getString("gpa_grade"));
        e.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            e.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        e.setDeleted(rs.getBoolean("is_deleted"));
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            e.setDeletedAt(deletedAt.toLocalDateTime());
        }
        return e;
    }

    private void setDateOrNull(PreparedStatement stmt, int index, java.time.LocalDate date) throws SQLException {
        if (date != null) {
            stmt.setDate(index, Date.valueOf(date));
        } else {
            stmt.setNull(index, Types.DATE);
        }
    }
}
