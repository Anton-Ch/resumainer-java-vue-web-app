package com.resumainer.dao;

import com.resumainer.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for the 'project' table (BIGSERIAL PK).
 * All queries use PreparedStatement (Constitution IV).
 * All SELECT queries filter by user_id (SEC-001) and is_deleted = FALSE (SEC-003).
 */
@Repository
public class ProjectDao {

    private static final Logger log = LoggerFactory.getLogger(ProjectDao.class);

    private static final String SELECT_BY_USER =
            "SELECT id, user_id, project_name, role, description, location, "
            + "start_date, end_date, is_ongoing, project_url, "
            + "created_at, updated_at, is_deleted, deleted_at "
            + "FROM project WHERE user_id = ? AND is_deleted = FALSE "
            + "ORDER BY start_date DESC, end_date DESC NULLS FIRST";

    private static final String SELECT_BY_ID =
            "SELECT id, user_id, project_name, role, description, location, "
            + "start_date, end_date, is_ongoing, project_url, "
            + "created_at, updated_at, is_deleted, deleted_at "
            + "FROM project WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private static final String INSERT =
            "INSERT INTO project (user_id, project_name, role, description, location, "
            + "start_date, end_date, is_ongoing, project_url) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String UPDATE =
            "UPDATE project SET project_name = ?, role = ?, description = ?, location = ?, "
            + "start_date = ?, end_date = ?, is_ongoing = ?, project_url = ?, "
            + "updated_at = NOW() WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private static final String SOFT_DELETE =
            "UPDATE project SET is_deleted = TRUE, deleted_at = NOW() "
            + "WHERE id = ? AND user_id = ? AND is_deleted = FALSE";

    private final DataSource dataSource;

    public ProjectDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --- Auto-managed connection methods ---

    public List<Project> findByUserId(UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return findByUserId(userId, conn);
        } catch (SQLException e) {
            log.error("Error finding projects for user: {}", userId, e);
            throw new RuntimeException("Database error finding projects", e);
        }
    }

    public Project findById(long id, UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return findById(id, userId, conn);
        } catch (SQLException e) {
            log.error("Error finding project by id: {}", id, e);
            throw new RuntimeException("Database error finding project", e);
        }
    }

    public Project create(Project project) {
        try (Connection conn = dataSource.getConnection()) {
            return create(project, conn);
        } catch (SQLException e) {
            log.error("Error creating project for user: {}", project.getUserId(), e);
            throw new RuntimeException("Database error creating project", e);
        }
    }

    public void update(Project project) {
        try (Connection conn = dataSource.getConnection()) {
            update(project, conn);
        } catch (SQLException e) {
            log.error("Error updating project: {}", project.getId(), e);
            throw new RuntimeException("Database error updating project", e);
        }
    }

    public boolean softDelete(long id, UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return softDelete(id, userId, conn);
        } catch (SQLException e) {
            log.error("Error deleting project: {}", id, e);
            throw new RuntimeException("Database error deleting project", e);
        }
    }

    // --- Connection-accepting overloads (D10) ---

    public List<Project> findByUserId(UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Project> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        }
    }

    public Project findById(long id, UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setLong(1, id);
            stmt.setObject(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Project create(Project project, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setObject(1, project.getUserId());
            stmt.setString(2, project.getProjectName());
            stmt.setString(3, project.getRole());
            stmt.setString(4, project.getDescription());
            stmt.setString(5, project.getLocation());
            stmt.setDate(6, Date.valueOf(project.getStartDate()));
            setDateOrNull(stmt, 7, project.getEndDate());
            stmt.setBoolean(8, project.isOngoing());
            stmt.setString(9, project.getProjectUrl());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    project.setId(rs.getLong("id"));
                }
            }
            log.debug("Project created: id={}, userId={}", project.getId(), project.getUserId());
            return project;
        }
    }

    public void update(Project project, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            stmt.setString(1, project.getProjectName());
            stmt.setString(2, project.getRole());
            stmt.setString(3, project.getDescription());
            stmt.setString(4, project.getLocation());
            stmt.setDate(5, Date.valueOf(project.getStartDate()));
            setDateOrNull(stmt, 6, project.getEndDate());
            stmt.setBoolean(7, project.isOngoing());
            stmt.setString(8, project.getProjectUrl());
            stmt.setLong(9, project.getId());
            stmt.setObject(10, project.getUserId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Project not found or access denied: id="
                        + project.getId());
            }
            log.debug("Project updated: id={}", project.getId());
        }
    }

    public boolean softDelete(long id, UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SOFT_DELETE)) {
            stmt.setLong(1, id);
            stmt.setObject(2, userId);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) {
                log.debug("Project deleted: id={}", id);
            }
            return deleted;
        }
    }

    // --- Row mapping ---

    private Project mapRow(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getLong("id"));
        p.setUserId((UUID) rs.getObject("user_id"));
        p.setProjectName(rs.getString("project_name"));
        p.setRole(rs.getString("role"));
        p.setDescription(rs.getString("description"));
        p.setLocation(rs.getString("location"));
        p.setStartDate(rs.getDate("start_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            p.setEndDate(endDate.toLocalDate());
        }
        p.setOngoing(rs.getBoolean("is_ongoing"));
        p.setProjectUrl(rs.getString("project_url"));
        p.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            p.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        p.setDeleted(rs.getBoolean("is_deleted"));
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            p.setDeletedAt(deletedAt.toLocalDateTime());
        }
        return p;
    }

    private void setDateOrNull(PreparedStatement stmt, int index, java.time.LocalDate date) throws SQLException {
        if (date != null) {
            stmt.setDate(index, Date.valueOf(date));
        } else {
            stmt.setNull(index, Types.DATE);
        }
    }
}
