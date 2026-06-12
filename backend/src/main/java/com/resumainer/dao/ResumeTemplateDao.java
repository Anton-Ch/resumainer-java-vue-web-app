package com.resumainer.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

/**
 * DAO for 'resume_template' table.
 * Loads HTML template content for rendering.
 */
@Repository
public class ResumeTemplateDao {

    private static final Logger log = LoggerFactory.getLogger(ResumeTemplateDao.class);

    private static final String SELECT_DEFAULT =
            "SELECT * FROM resume_template WHERE is_active = TRUE ORDER BY id LIMIT 1";

    private static final String SELECT_BY_ID =
            "SELECT * FROM resume_template WHERE id = ?";

    private final DataSource dataSource;

    public ResumeTemplateDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Returns the html_file_path of the default active template. */
    public String findDefaultTemplatePath() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_DEFAULT);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getString("html_file_path") : null;
        } catch (SQLException e) {
            log.error("Error finding default template", e);
            throw new RuntimeException("Database error finding default template", e);
        }
    }
}
