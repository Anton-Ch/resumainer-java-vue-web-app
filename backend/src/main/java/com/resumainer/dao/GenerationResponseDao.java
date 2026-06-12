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
            + "(response_id, job_title, company_name, description, location, "
            + "is_first_page, start_date, end_date, order_in_resume) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_EXPERIENCE_BY_RESPONSE =
            "SELECT * FROM generation_response_experience WHERE response_id = ? ORDER BY order_in_resume";

    // --- Course ---
    private static final String INSERT_COURSE =
            "INSERT INTO generation_response_course "
            + "(response_id, name, provider, is_first_page, course_focus, order_in_resume) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_COURSES_BY_RESPONSE =
            "SELECT * FROM generation_response_course WHERE response_id = ? ORDER BY order_in_resume";

    // --- Project ---
    private static final String INSERT_PROJECT =
            "INSERT INTO generation_response_project "
            + "(response_id, project_name, role, description, location, start_date, end_date, order_in_resume) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_PROJECTS_BY_RESPONSE =
            "SELECT * FROM generation_response_project WHERE response_id = ? ORDER BY order_in_resume";

    // --- Skill ---
    private static final String INSERT_SKILL =
            "INSERT INTO generation_response_skill "
            + "(response_id, skill_group, skill_name, order_in_resume) "
            + "VALUES (?, ?, ?, ?)";

    private static final String SELECT_SKILLS_BY_RESPONSE =
            "SELECT * FROM generation_response_skill WHERE response_id = ? ORDER BY order_in_resume";

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

    public void insertExperience(GenerationResponseExperience exp, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_EXPERIENCE)) {
            stmt.setObject(1, exp.getResponseId());
            stmt.setString(2, exp.getJobTitle());
            stmt.setString(3, exp.getCompanyName());
            stmt.setString(4, exp.getDescription());
            stmt.setString(5, exp.getLocation());
            stmt.setBoolean(6, exp.isFirstPage());
            stmt.setDate(7, Date.valueOf(exp.getStartDate()));
            setDateOrNull(stmt, 8, exp.getEndDate());
            stmt.setInt(9, exp.getOrderInResume());
            stmt.executeUpdate();
        }
    }

    public void insertCourse(GenerationResponseCourse course, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_COURSE)) {
            stmt.setObject(1, course.getResponseId());
            stmt.setString(2, course.getName());
            stmt.setString(3, course.getProvider());
            stmt.setBoolean(4, course.isFirstPage());
            stmt.setString(5, course.getCourseFocus());
            stmt.setInt(6, course.getOrderInResume());
            stmt.executeUpdate();
        }
    }

    public void insertProject(GenerationResponseProject project, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_PROJECT)) {
            stmt.setObject(1, project.getResponseId());
            stmt.setString(2, project.getProjectName());
            stmt.setString(3, project.getRole());
            stmt.setString(4, project.getDescription());
            stmt.setString(5, project.getLocation());
            stmt.setDate(6, Date.valueOf(project.getStartDate()));
            setDateOrNull(stmt, 7, project.getEndDate());
            stmt.setInt(8, project.getOrderInResume());
            stmt.executeUpdate();
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
