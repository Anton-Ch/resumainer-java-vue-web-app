package com.resumainer.dao;

import com.resumainer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DAO for 'resume_generation_response' and all child section tables.
 * Provides insert, load-bundle, and review-update operations.
 */
@Repository
public class GenerationResponseDao {

    private static final Logger log = LoggerFactory.getLogger(GenerationResponseDao.class);

    // --- Response ---
    private static final String INSERT_RESPONSE =
            "INSERT INTO resume_generation_response "
            + "(generation_request_id, language_id, adaptation_level_id, status_id, "
            + "professional_title, value_line, professional_summary, professional_aspirations, cover_letter) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String SELECT_RESPONSES_BY_REQUEST =
            "SELECT rgr.*, l.code AS language_code "
            + "FROM resume_generation_response rgr "
            + "JOIN language l ON rgr.language_id = l.id "
            + "WHERE rgr.generation_request_id = ? ORDER BY rgr.language_id, rgr.adaptation_level_id";

    private static final String UPDATE_RESPONSE_FIELD =
            "UPDATE resume_generation_response SET %s = ?, updated_at = NOW() WHERE id = ?";

    // --- Experience ---
    private static final String INSERT_EXPERIENCE =
            "INSERT INTO generation_response_experience "
                    + "(response_id, source_id, job_title, company_name, description, location, "
                    + "is_first_page, start_date, end_date, order_in_resume) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String SELECT_EXPERIENCE_BY_RESPONSE =
            "SELECT * FROM generation_response_experience WHERE response_id = ? ORDER BY order_in_resume";

    // --- Course ---
    private static final String INSERT_COURSE =
            "INSERT INTO generation_response_course "
                    + "(response_id, source_id, name, provider, is_first_page, course_focus, order_in_resume) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_COURSES_BY_RESPONSE =
            "SELECT * FROM generation_response_course WHERE response_id = ? ORDER BY order_in_resume";

    // --- Project ---
    private static final String INSERT_PROJECT =
            "INSERT INTO generation_response_project "
                    + "(response_id, source_id, project_name, role, description, location, start_date, end_date, order_in_resume) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String SELECT_PROJECTS_BY_RESPONSE =
            "SELECT * FROM generation_response_project WHERE response_id = ? ORDER BY order_in_resume";

    // --- Skill ---
    private static final String INSERT_SKILL =
            "INSERT INTO generation_response_skill "
            + "(response_id, skill_group, skill_name, order_in_resume) "
            + "VALUES (?, ?, ?, ?)";

    private static final String SELECT_SKILLS_BY_RESPONSE =
            "SELECT * FROM generation_response_skill WHERE response_id = ? ORDER BY order_in_resume";

    // --- Experience Bullet (Feature 008) ---
    private static final String INSERT_EXPERIENCE_BULLET =
            "INSERT INTO generation_response_experience_bullet "
            + "(experience_id, bullet_order, bullet_text, is_edited) "
            + "VALUES (?, ?, ?, ?)";

    private static final String SELECT_EXPERIENCE_BULLETS =
            "SELECT * FROM generation_response_experience_bullet WHERE experience_id = ? ORDER BY bullet_order";

    // --- Project Bullet (Feature 008) ---
    private static final String INSERT_PROJECT_BULLET =
            "INSERT INTO generation_response_project_bullet "
            + "(project_id, bullet_order, bullet_text, is_edited) "
            + "VALUES (?, ?, ?, ?)";

    private static final String SELECT_PROJECT_BULLETS =
            "SELECT * FROM generation_response_project_bullet WHERE project_id = ? ORDER BY bullet_order";

    private final DataSource dataSource;

    public GenerationResponseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --- Connection-accepting overloads (for transactions) ---

    public ResumeGenerationResponse insertResponse(ResumeGenerationResponse response, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_RESPONSE)) {
            stmt.setObject(1, response.getGenerationRequestId());
            stmt.setLong(2, response.getLanguageId());
            stmt.setLong(3, response.getAdaptationLevelId());
            stmt.setLong(4, response.getStatusId());
            stmt.setString(5, response.getProfessionalTitle());
            stmt.setString(6, response.getValueLine());
            stmt.setString(7, response.getProfessionalSummary());
            stmt.setString(8, response.getProfessionalAspirations());
            stmt.setString(9, response.getCoverLetter());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) response.setId((UUID) rs.getObject("id"));
            }
            return response;
        }
    }

    public UUID insertExperience(GenerationResponseExperience exp, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_EXPERIENCE)) {
            stmt.setObject(1, exp.getResponseId());
            stmt.setString(2, exp.getSourceId());
            stmt.setString(3, exp.getJobTitle());
            stmt.setString(4, exp.getCompanyName());
            stmt.setString(5, exp.getDescription());
            stmt.setString(6, exp.getLocation());
            stmt.setBoolean(7, exp.isFirstPage());
            stmt.setDate(8, Date.valueOf(exp.getStartDate()));
            setDateOrNull(stmt, 9, exp.getEndDate());
            stmt.setInt(10, exp.getOrderInResume());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID id = (UUID) rs.getObject("id");
                    exp.setId(id);
                    return id;
                }
            }
            throw new SQLException("Insert experience did not return an id");
        }
    }

    public void insertCourse(GenerationResponseCourse course, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_COURSE)) {
            stmt.setObject(1, course.getResponseId());
            stmt.setString(2, course.getSourceId());
            stmt.setString(3, course.getName());
            stmt.setString(4, course.getProvider());
            stmt.setBoolean(5, course.isFirstPage());
            stmt.setString(6, course.getCourseFocus());
            stmt.setInt(7, course.getOrderInResume());
            stmt.executeUpdate();
        }
    }

    public UUID insertProject(GenerationResponseProject project, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_PROJECT)) {
            stmt.setObject(1, project.getResponseId());
            stmt.setString(2, project.getSourceId());
            stmt.setString(3, project.getProjectName());
            stmt.setString(4, project.getRole());
            stmt.setString(5, project.getDescription());
            stmt.setString(6, project.getLocation());
            stmt.setDate(7, Date.valueOf(project.getStartDate()));
            setDateOrNull(stmt, 8, project.getEndDate());
            stmt.setInt(9, project.getOrderInResume());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID id = (UUID) rs.getObject("id");
                    project.setId(id);
                    return id;
                }
            }
            throw new SQLException("Insert project did not return an id");
        }
    }

    public void insertSkill(GenerationResponseSkill skill, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SKILL)) {
            stmt.setObject(1, skill.getResponseId());
            stmt.setString(2, skill.getSkillGroup());
            stmt.setString(3, skill.getSkillName());
            stmt.setInt(4, skill.getOrderInResume());
            stmt.executeUpdate();
        }
    }

    // --- Bullet insert (Feature 008) ---

    /** Insert one experience bullet point within a transaction. */
    public void insertExperienceBullet(GenerationResponseExperienceBullet bullet, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_EXPERIENCE_BULLET)) {
            stmt.setObject(1, bullet.getExperienceId());
            stmt.setInt(2, bullet.getBulletOrder());
            stmt.setString(3, bullet.getBulletText());
            stmt.setBoolean(4, bullet.isEdited());
            stmt.executeUpdate();
        }
    }

    /** Insert one project bullet point within a transaction. */
    public void insertProjectBullet(GenerationResponseProjectBullet bullet, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_PROJECT_BULLET)) {
            stmt.setObject(1, bullet.getProjectId());
            stmt.setInt(2, bullet.getBulletOrder());
            stmt.setString(3, bullet.getBulletText());
            stmt.setBoolean(4, bullet.isEdited());
            stmt.executeUpdate();
        }
    }

    // --- Load methods ---

    public List<ResumeGenerationResponse> findResponsesByRequestId(UUID requestId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_RESPONSES_BY_REQUEST)) {
            stmt.setObject(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<ResumeGenerationResponse> results = new ArrayList<>();
                while (rs.next()) results.add(mapResponseRow(rs));
                return results;
            }
        } catch (SQLException e) {
            log.error("Error loading responses for request: {}", requestId, e);
            throw new RuntimeException("Database error loading responses", e);
        }
    }

    public List<GenerationResponseExperience> findExperienceByResponseId(UUID responseId) {
        return findChildList(responseId, SELECT_EXPERIENCE_BY_RESPONSE, this::mapExperienceRow);
    }

    public List<GenerationResponseCourse> findCoursesByResponseId(UUID responseId) {
        return findChildList(responseId, SELECT_COURSES_BY_RESPONSE, this::mapCourseRow);
    }

    public List<GenerationResponseProject> findProjectsByResponseId(UUID responseId) {
        return findChildList(responseId, SELECT_PROJECTS_BY_RESPONSE, this::mapProjectRow);
    }

    public List<GenerationResponseSkill> findSkillsByResponseId(UUID responseId) {
        return findChildList(responseId, SELECT_SKILLS_BY_RESPONSE, this::mapSkillRow);
    }

    /** Load experience bullet points for one generated experience entry (Feature 008). */
    public List<GenerationResponseExperienceBullet> findExperienceBullets(UUID experienceId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_EXPERIENCE_BULLETS)) {
            stmt.setObject(1, experienceId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<GenerationResponseExperienceBullet> results = new ArrayList<>();
                while (rs.next()) results.add(mapExperienceBulletRow(rs));
                return results;
            }
        } catch (SQLException e) {
            log.error("Error loading experience bullets for experience: {}", experienceId, e);
            throw new RuntimeException("Database error loading experience bullets", e);
        }
    }

    /** Load project bullet points for one generated project entry (Feature 008). */
    public List<GenerationResponseProjectBullet> findProjectBullets(UUID projectId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PROJECT_BULLETS)) {
            stmt.setObject(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<GenerationResponseProjectBullet> results = new ArrayList<>();
                while (rs.next()) results.add(mapProjectBulletRow(rs));
                return results;
            }
        } catch (SQLException e) {
            log.error("Error loading project bullets for project: {}", projectId, e);
            throw new RuntimeException("Database error loading project bullets", e);
        }
    }

    // --- Safe update methods (used by ResumeReviewService) ---

    /** Column allowlist for resume_generation_response */
    private static final java.util.Map<String, String> RESPONSE_COLUMN_MAP = java.util.Map.of(
            "professionalTitle", "professional_title",
            "valueLine", "value_line",
            "professionalSummary", "professional_summary",
            "professionalAspirations", "professional_aspirations",
            "coverLetter", "cover_letter"
    );

    /** Column allowlist for generation_response_experience */
    private static final java.util.Map<String, String> EXPERIENCE_COLUMN_MAP = java.util.Map.of(
            "jobTitle", "job_title",
            "companyName", "company_name",
            "description", "description"
    );

    /** Column allowlist for generation_response_course */
    private static final java.util.Map<String, String> COURSE_COLUMN_MAP = java.util.Map.of(
            "courseName", "name",
            "provider", "provider",
            "courseFocus", "course_focus"
    );

    /** Column allowlist for generation_response_project */
    private static final java.util.Map<String, String> PROJECT_COLUMN_MAP = java.util.Map.of(
            "projectName", "project_name",
            "role", "role",
            "description", "description"
    );

    /** Update a top-level response field. fieldName must be in RESPONSE_COLUMN_MAP. */
    public void updateResponseField(UUID id, String fieldName, String value) {
        String dbColumn = RESPONSE_COLUMN_MAP.get(fieldName);
        if (dbColumn == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is not allowed for update.");
        }
        String sql = "UPDATE resume_generation_response SET " + dbColumn + " = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setObject(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating response field {} for id: {}", fieldName, id, e);
            throw new RuntimeException("Failed to update " + fieldName, e);
        }
    }

    /** Update a work experience field. fieldName must be in EXPERIENCE_COLUMN_MAP. */
    public void updateExperienceField(UUID id, String fieldName, String value) {
        String dbColumn = EXPERIENCE_COLUMN_MAP.get(fieldName);
        if (dbColumn == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is not allowed for update.");
        }
        String sql = "UPDATE generation_response_experience SET " + dbColumn + " = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setObject(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating experience field {} for id: {}", fieldName, id, e);
            throw new RuntimeException("Failed to update " + fieldName, e);
        }
    }

    /** Update a course field. fieldName must be in COURSE_COLUMN_MAP. */
    public void updateCourseField(UUID id, String fieldName, String value) {
        String dbColumn = COURSE_COLUMN_MAP.get(fieldName);
        if (dbColumn == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is not allowed for update.");
        }
        String sql = "UPDATE generation_response_course SET " + dbColumn + " = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setObject(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating course field {} for id: {}", fieldName, id, e);
            throw new RuntimeException("Failed to update " + fieldName, e);
        }
    }

    /** Update a project field. fieldName must be in PROJECT_COLUMN_MAP. */
    public void updateProjectField(UUID id, String fieldName, String value) {
        String dbColumn = PROJECT_COLUMN_MAP.get(fieldName);
        if (dbColumn == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is not allowed for update.");
        }
        String sql = "UPDATE generation_response_project SET " + dbColumn + " = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setObject(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating project field {} for id: {}", fieldName, id, e);
            throw new RuntimeException("Failed to update " + fieldName, e);
        }
    }

    /** Update skill group name. Replaces the skill_group value for all skills with this group+responseId. */
    public void updateSkillGroupName(UUID responseId, String oldGroupName, String newGroupName) {
        String sql = "UPDATE generation_response_skill SET skill_group = ?, updated_at = NOW() WHERE response_id = ? AND skill_group = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newGroupName);
            stmt.setObject(2, responseId);
            stmt.setString(3, oldGroupName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating skill group name for response: {}", responseId, e);
            throw new RuntimeException("Failed to update skill group name", e);
        }
    }

    /** Delete all skills for a response */
    public void deleteSkillsByResponseId(UUID responseId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM generation_response_skill WHERE response_id = ?")) {
            stmt.setObject(1, responseId);
            stmt.executeUpdate();
        }
    }

    // --- Response bundle loading (T040) ---

    /**
     * Loads a full generation response bundle: response + all child sections.
     * Used by template renderer and review service.
     */
    public static class ResponseBundle {
        public ResumeGenerationResponse response;
        public List<GenerationResponseExperience> experience;
        public List<GenerationResponseCourse> courses;
        public List<GenerationResponseProject> projects;
        public List<GenerationResponseSkill> skills;
    }

    public ResponseBundle loadResponseBundle(UUID responseId) {
        ResponseBundle bundle = new ResponseBundle();
        // Load response by ID (need a single-response query)
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT rgr.*, l.code AS language_code FROM resume_generation_response rgr "
                     + "JOIN language l ON rgr.language_id = l.id WHERE rgr.id = ?")) {
            stmt.setObject(1, responseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new RuntimeException("Response not found: " + responseId);
                bundle.response = mapResponseRow(rs);
            }
        } catch (SQLException e) {
            log.error("Error loading response bundle: {}", responseId, e);
            throw new RuntimeException("Database error loading response bundle", e);
        }
        bundle.experience = findExperienceByResponseId(responseId);
        bundle.courses = findCoursesByResponseId(responseId);
        bundle.projects = findProjectsByResponseId(responseId);
        bundle.skills = findSkillsByResponseId(responseId);
        return bundle;
    }

    // --- Row mapping ---

    private ResumeGenerationResponse mapResponseRow(ResultSet rs) throws SQLException {
        ResumeGenerationResponse r = new ResumeGenerationResponse();
        r.setId((UUID) rs.getObject("id"));
        r.setGenerationRequestId((UUID) rs.getObject("generation_request_id"));
        r.setLanguageId(rs.getLong("language_id"));
        r.setAdaptationLevelId(rs.getLong("adaptation_level_id"));
        r.setStatusId(rs.getLong("status_id"));
        r.setProfessionalTitle(rs.getString("professional_title"));
        r.setValueLine(rs.getString("value_line"));
        r.setProfessionalSummary(rs.getString("professional_summary"));
        r.setProfessionalAspirations(rs.getString("professional_aspirations"));
        r.setCoverLetter(rs.getString("cover_letter"));
        r.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        r.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return r;
    }

    private GenerationResponseExperience mapExperienceRow(ResultSet rs) throws SQLException {
        GenerationResponseExperience e = new GenerationResponseExperience();
        e.setId((UUID) rs.getObject("id"));
        e.setResponseId((UUID) rs.getObject("response_id"));
        e.setSourceId(rs.getString("source_id"));
        e.setJobTitle(rs.getString("job_title"));
        e.setCompanyName(rs.getString("company_name"));
        e.setDescription(rs.getString("description"));
        e.setLocation(rs.getString("location"));
        e.setFirstPage(rs.getBoolean("is_first_page"));
        e.setStartDate(rs.getDate("start_date").toLocalDate());
        if (rs.getDate("end_date") != null) e.setEndDate(rs.getDate("end_date").toLocalDate());
        e.setOrderInResume(rs.getInt("order_in_resume"));
        return e;
    }

    private GenerationResponseCourse mapCourseRow(ResultSet rs) throws SQLException {
        GenerationResponseCourse c = new GenerationResponseCourse();
        c.setId((UUID) rs.getObject("id"));
        c.setResponseId((UUID) rs.getObject("response_id"));
        c.setSourceId(rs.getString("source_id"));
        c.setName(rs.getString("name"));
        c.setProvider(rs.getString("provider"));
        c.setFirstPage(rs.getBoolean("is_first_page"));
        c.setCourseFocus(rs.getString("course_focus"));
        c.setOrderInResume(rs.getInt("order_in_resume"));
        return c;
    }

    private GenerationResponseProject mapProjectRow(ResultSet rs) throws SQLException {
        GenerationResponseProject p = new GenerationResponseProject();
        p.setId((UUID) rs.getObject("id"));
        p.setResponseId((UUID) rs.getObject("response_id"));
        p.setSourceId(rs.getString("source_id"));
        p.setProjectName(rs.getString("project_name"));
        p.setRole(rs.getString("role"));
        p.setDescription(rs.getString("description"));
        p.setLocation(rs.getString("location"));
        p.setStartDate(rs.getDate("start_date").toLocalDate());
        if (rs.getDate("end_date") != null) p.setEndDate(rs.getDate("end_date").toLocalDate());
        p.setOrderInResume(rs.getInt("order_in_resume"));
        return p;
    }

    private GenerationResponseSkill mapSkillRow(ResultSet rs) throws SQLException {
        GenerationResponseSkill s = new GenerationResponseSkill();
        s.setId((UUID) rs.getObject("id"));
        s.setResponseId((UUID) rs.getObject("response_id"));
        s.setSkillGroup(rs.getString("skill_group"));
        s.setSkillName(rs.getString("skill_name"));
        s.setOrderInResume(rs.getInt("order_in_resume"));
        return s;
    }

    private GenerationResponseExperienceBullet mapExperienceBulletRow(ResultSet rs) throws SQLException {
        GenerationResponseExperienceBullet b = new GenerationResponseExperienceBullet();
        b.setId(rs.getLong("id"));
        b.setExperienceId((UUID) rs.getObject("experience_id"));
        b.setBulletOrder(rs.getInt("bullet_order"));
        b.setBulletText(rs.getString("bullet_text"));
        b.setEdited(rs.getBoolean("is_edited"));
        b.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        b.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return b;
    }

    private GenerationResponseProjectBullet mapProjectBulletRow(ResultSet rs) throws SQLException {
        GenerationResponseProjectBullet b = new GenerationResponseProjectBullet();
        b.setId(rs.getLong("id"));
        b.setProjectId((UUID) rs.getObject("project_id"));
        b.setBulletOrder(rs.getInt("bullet_order"));
        b.setBulletText(rs.getString("bullet_text"));
        b.setEdited(rs.getBoolean("is_edited"));
        b.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        b.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return b;
    }

    // --- Helpers ---

    private <T> List<T> findChildList(UUID responseId, String sql, RowMapper<T> mapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, responseId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) results.add(mapper.map(rs));
                return results;
            }
        } catch (SQLException e) {
            log.error("Error loading child rows for response: {}", responseId, e);
            throw new RuntimeException("Database error loading child rows", e);
        }
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private void setDateOrNull(PreparedStatement stmt, int index, LocalDate date) throws SQLException {
        if (date != null) stmt.setDate(index, Date.valueOf(date));
        else stmt.setNull(index, Types.DATE);
    }
}
