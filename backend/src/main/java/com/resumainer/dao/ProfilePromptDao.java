package com.resumainer.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * DAO for loading full profile prompt payload needed by ResumePromptBuilder.
 * Composes data from multiple tables: contact, work experience, education,
 * courses, projects, additional info, and work formats.
 */
@Repository
public class ProfilePromptDao {

    private static final Logger log = LoggerFactory.getLogger(ProfilePromptDao.class);

    private static final String SELECT_CONTACT =
            "SELECT full_name, phone, resume_email, location, professional_title, "
            + "linkedin_url, portfolio_url, telegram, whatsapp "
            + "FROM contact_detail WHERE user_id = ?";

    private static final String SELECT_ADDITIONAL_INFO =
            "SELECT skills, languages, professional_aspirations, achievements, "
            + "general_information, ready_for_relocation, ready_for_business_trips, "
            + "citizenship, date_of_birth "
            + "FROM additional_profile_info WHERE user_id = ?";

    private static final String SELECT_WORK_FORMATS =
            "SELECT wf.code, wf.name "
            + "FROM user_work_format uwf "
            + "JOIN work_format wf ON wf.id = uwf.work_format_id "
            + "WHERE uwf.user_id = ? ORDER BY wf.id";

    private static final String SELECT_EDUCATION =
            "SELECT institution_name_ru, institution_name_en, degree_ru, degree_en, "
            + "field_of_study_ru, field_of_study_en "
            + "FROM education WHERE user_id = ? AND is_deleted = FALSE ORDER BY start_date DESC";

    private final DataSource dataSource;

    public ProfilePromptDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Returns contact detail as a map (keys match AI prompt contract). */
    public Map<String, Object> loadContact(UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_CONTACT)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Collections.emptyMap();
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("fullName", rs.getString("full_name"));
                map.put("phone", rs.getString("phone"));
                map.put("resumeEmail", rs.getString("resume_email"));
                map.put("location", rs.getString("location"));
                map.put("professionalTitle", rs.getString("professional_title"));
                return map;
            }
        } catch (SQLException e) {
            log.error("Error loading contact for user: {}", userId, e);
            throw new RuntimeException("Database error loading contact", e);
        }
    }

    /** Returns additional profile info as a map. */
    public Map<String, Object> loadAdditionalInfo(UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ADDITIONAL_INFO)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Collections.emptyMap();
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("skills", rs.getString("skills"));
                map.put("languages", rs.getString("languages"));
                map.put("professionalAspirations", rs.getString("professional_aspirations"));
                map.put("achievements", rs.getString("achievements"));
                map.put("generalInformation", rs.getString("general_information"));
                map.put("readyForRelocation", rs.getString("ready_for_relocation"));
                map.put("readyForBusinessTrips", rs.getString("ready_for_business_trips"));
                map.put("citizenship", rs.getString("citizenship"));
                map.put("dateOfBirth", rs.getDate("date_of_birth") != null ?
                        rs.getDate("date_of_birth").toString() : null);
                return map;
            }
        } catch (SQLException e) {
            log.error("Error loading additional info for user: {}", userId, e);
            throw new RuntimeException("Database error loading additional info", e);
        }
    }

    /** Returns list of work format codes + names. */
    public List<Map<String, Object>> loadWorkFormats(UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_WORK_FORMATS)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> wf = new LinkedHashMap<>();
                    wf.put("code", rs.getString("code"));
                    wf.put("name", rs.getString("name"));
                    results.add(wf);
                }
                return results;
            }
        } catch (SQLException e) {
            log.error("Error loading work formats for user: {}", userId, e);
            throw new RuntimeException("Database error loading work formats", e);
        }
    }

    /** Returns list of education records with bilingual fields. */
    public List<Map<String, Object>> loadEducation(UUID userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_EDUCATION)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> edu = new LinkedHashMap<>();
                    edu.put("institutionNameRu", rs.getString("institution_name_ru"));
                    edu.put("institutionNameEn", rs.getString("institution_name_en"));
                    edu.put("degreeRu", rs.getString("degree_ru"));
                    edu.put("degreeEn", rs.getString("degree_en"));
                    edu.put("fieldOfStudyRu", rs.getString("field_of_study_ru"));
                    edu.put("fieldOfStudyEn", rs.getString("field_of_study_en"));
                    results.add(edu);
                }
                return results;
            }
        } catch (SQLException e) {
            log.error("Error loading education for user: {}", userId, e);
            throw new RuntimeException("Database error loading education", e);
        }
    }
}
