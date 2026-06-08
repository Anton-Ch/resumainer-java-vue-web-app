package com.resumainer.dao;

import com.resumainer.model.WorkExperience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for the 'work_experience' table (BIGSERIAL PK).
 * All queries use PreparedStatement (Constitution IV).
 * All SELECT queries filter by user_id (SEC-001) and is_deleted = FALSE (SEC-003).
 */
@Repository
public class WorkExperienceDao {

    private static final Logger log = LoggerFactory.getLogger(WorkExperienceDao.class);

    private static final String SELECT_BY_USER =
            "SELECT id, user_id, job_title, company_name, description, location, "
            + "start_date, end_date, is_current, company_url, "
            + "created_at, updated_at, is_deleted, deleted_at "
            + "FROM work_experience WHERE user_id = ? AND is_deleted = FALSE "
            + "ORDER BY start_date DESC, end_date DESC NULLS FIRST";

    private static final String SELECT_BY_ID =
            "SELECT id, user_id, job_title, company_name, description, location, "
            + "start_date, end_date, is_current, company_url, "
            + "created_at, updated_at, is_deleted, deleted_at "
            + "FROM work_experience WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private static final String INSERT =
            "INSERT INTO work_experience (user_id, job_title, company_name, description, location, "
            + "start_date, end_date, is_current, company_url) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String UPDATE =
            "UPDATE work_experience SET job_title = ?, company_name = ?, description = ?, location = ?, "
            + "start_date = ?, end_date = ?, is_current = ?, company_url = ?, "
            + "updated_at = NOW() WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private static final String SOFT_DELETE =
            "UPDATE work_experience SET is_deleted = TRUE, deleted_at = NOW() "
            + "WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private final DataSource dataSource;

    public WorkExperienceDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --- Auto-managed connection methods ---

    public List<WorkExperience> findByUserId(UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return findByUserId(userId, conn);
        } catch (SQLException e) {
            log.error("Error finding work experience for user: {}", userId, e);
            throw new RuntimeException("Database error finding work experience", e);
        }
    }

    public WorkExperience findById(long id, UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return findById(id, userId, conn);
        } catch (SQLException e) {
            log.error("Error finding work experience by id: {}", id, e);
            throw new RuntimeException("Database error finding work experience", e);
        }
    }

    public WorkExperience create(WorkExperience experience) {
        try (Connection conn = dataSource.getConnection()) {
            return create(experience, conn);
        } catch (SQLException e) {
            log.error("Error creating work experience for user: {}", experience.getUserId(), e);
            throw new RuntimeException("Database error creating work experience", e);
        }
    }

    public void update(WorkExperience experience) {
        try (Connection conn = dataSource.getConnection()) {
            update(experience, conn);
        } catch (SQLException e) {
            log.error("Error updating work experience: {}", experience.getId(), e);
            throw new RuntimeException("Database error updating work experience", e);
        }
    }

    public boolean softDelete(long id, UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return softDelete(id, userId, conn);
        } catch (SQLException e) {
            log.error("Error deleting work experience: {}", id, e);
            throw new RuntimeException("Database error deleting work experience", e);
        }
    }

    // --- Connection-accepting overloads (D10) ---

    public List<WorkExperience> findByUserId(UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<WorkExperience> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        }
    }

    public WorkExperience findById(long id, UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setLong(1, id);
            stmt.setObject(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public WorkExperience create(WorkExperience experience, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setObject(1, experience.getUserId());
            stmt.setString(2, experience.getJobTitle());
            stmt.setString(3, experience.getCompanyName());
            stmt.setString(4, experience.getDescription());
            stmt.setString(5, experience.getLocation());
            stmt.setDate(6, Date.valueOf(experience.getStartDate()));
            setDateOrNull(stmt, 7, experience.getEndDate());
            stmt.setBoolean(8, experience.isCurrent());
            stmt.setString(9, experience.getCompanyUrl());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    experience.setId(rs.getLong("id"));
                }
            }
            log.debug("Work experience created: id={}, userId={}", experience.getId(), experience.getUserId());
            return experience;
        }
    }

    public void update(WorkExperience experience, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            stmt.setString(1, experience.getJobTitle());
            stmt.setString(2, experience.getCompanyName());
            stmt.setString(3, experience.getDescription());
            stmt.setString(4, experience.getLocation());
            stmt.setDate(5, Date.valueOf(experience.getStartDate()));
            setDateOrNull(stmt, 6, experience.getEndDate());
            stmt.setBoolean(7, experience.isCurrent());
            stmt.setString(8, experience.getCompanyUrl());
            stmt.setLong(9, experience.getId());
            stmt.setObject(10, experience.getUserId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Work experience not found or access denied: id="
                        + experience.getId());
            }
            log.debug("Work experience updated: id={}", experience.getId());
        }
    }

    public boolean softDelete(long id, UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SOFT_DELETE)) {
            stmt.setLong(1, id);
            stmt.setObject(2, userId);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) {
                log.debug("Work experience deleted: id={}", id);
            }
            return deleted;
        }
    }

    // --- Row mapping ---

    private WorkExperience mapRow(ResultSet rs) throws SQLException {
        WorkExperience e = new WorkExperience();
        e.setId(rs.getLong("id"));
        e.setUserId((UUID) rs.getObject("user_id"));
        e.setJobTitle(rs.getString("job_title"));
        e.setCompanyName(rs.getString("company_name"));
        e.setDescription(rs.getString("description"));
        e.setLocation(rs.getString("location"));
        e.setStartDate(rs.getDate("start_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            e.setEndDate(endDate.toLocalDate());
        }
        e.setCurrent(rs.getBoolean("is_current"));
        e.setCompanyUrl(rs.getString("company_url"));
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
