package com.resumainer.dao;

import com.resumainer.model.WorkFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for the 'work_format' (lookup) and 'user_work_format' (junction) tables.
 * Provides read access to work format lookup data and write access to
 * user-work-format assignments. All queries use PreparedStatement (Constitution IV).
 */
@Repository
public class WorkFormatDao {

    private static final Logger log = LoggerFactory.getLogger(WorkFormatDao.class);

    private static final String SELECT_ALL = "SELECT id, code, name FROM work_format ORDER BY id";

    private static final String SELECT_BY_USER =
            "SELECT wf.id, wf.code, wf.name "
            + "FROM work_format wf "
            + "INNER JOIN user_work_format uwf ON wf.id = uwf.work_format_id "
            + "WHERE uwf.user_id = ? ORDER BY wf.id";

    private static final String DELETE_BY_USER =
            "DELETE FROM user_work_format WHERE user_id = ?";

    private static final String INSERT_USER_FORMAT =
            "INSERT INTO user_work_format (user_id, work_format_id) VALUES (?, ?)";

    private final DataSource dataSource;

    public WorkFormatDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --- Auto-managed connection methods ---

    public List<WorkFormat> findAll() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            List<WorkFormat> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;

        } catch (SQLException e) {
            log.error("Error finding all work formats", e);
            throw new RuntimeException("Database error finding work formats", e);
        }
    }

    public List<WorkFormat> findByUserId(UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return findByUserId(userId, conn);
        } catch (SQLException e) {
            log.error("Error finding work formats for user: {}", userId, e);
            throw new RuntimeException("Database error finding work formats", e);
        }
    }

    /**
     * Replaces all work format assignments for a user (delete old + insert new).
     * Intended to be called within a transaction (connection overload).
     */
    public void saveUserFormats(UUID userId, List<Long> workFormatIds) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                saveUserFormats(userId, workFormatIds, conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log.error("Error saving work formats for user: {}", userId, e);
            throw new RuntimeException("Database error saving work formats", e);
        }
    }

    // --- Connection-accepting overloads (D10) ---

    public List<WorkFormat> findByUserId(UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<WorkFormat> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        }
    }

    public void saveUserFormats(UUID userId, List<Long> workFormatIds, Connection conn) throws SQLException {
        // Delete existing assignments
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_BY_USER)) {
            stmt.setObject(1, userId);
            int deleted = stmt.executeUpdate();
            log.debug("Deleted {} work format assignments for user: {}", deleted, userId);
        }

        // Insert new assignments
        if (workFormatIds != null && !workFormatIds.isEmpty()) {
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_USER_FORMAT)) {
                for (Long formatId : workFormatIds) {
                    stmt.setObject(1, userId);
                    stmt.setLong(2, formatId);
                    stmt.addBatch();
                }
                int[] inserted = stmt.executeBatch();
                log.debug("Inserted {} work format assignments for user: {}", inserted.length, userId);
            }
        }
    }

    // --- Row mapping ---

    private WorkFormat mapRow(ResultSet rs) throws SQLException {
        return new WorkFormat(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name")
        );
    }
}
