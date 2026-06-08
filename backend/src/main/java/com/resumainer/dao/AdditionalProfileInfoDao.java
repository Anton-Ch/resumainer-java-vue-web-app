package com.resumainer.dao;

import com.resumainer.model.AdditionalProfileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

/**
 * DAO for the 'additional_profile_info' table (BIGSERIAL PK, 1:1 with users).
 * Uses upsert pattern (INSERT ... ON CONFLICT ... DO UPDATE) for atomic
 * create-or-update behavior. No soft-delete — updated in-place.
 * All queries use PreparedStatement (Constitution IV) and filter by user_id (SEC-001).
 */
@Repository
public class AdditionalProfileInfoDao {

    private static final Logger log = LoggerFactory.getLogger(AdditionalProfileInfoDao.class);

    private static final String SELECT_BY_USER =
            "SELECT id, user_id, skills, languages, professional_aspirations, achievements, "
            + "general_information, default_resume_language_id, additional_resume_language_id, "
            + "ready_for_relocation, ready_for_business_trips, "
            + "date_of_birth, citizenship, photo_file_path, "
            + "created_at, updated_at "
            + "FROM additional_profile_info WHERE user_id = ?";

    private static final String UPSERT =
            "INSERT INTO additional_profile_info ("
            + "user_id, skills, languages, professional_aspirations, achievements, "
            + "general_information, default_resume_language_id, additional_resume_language_id, "
            + "ready_for_relocation, ready_for_business_trips, "
            + "date_of_birth, citizenship, photo_file_path) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT (user_id) DO UPDATE SET "
            + "skills = EXCLUDED.skills, "
            + "languages = EXCLUDED.languages, "
            + "professional_aspirations = EXCLUDED.professional_aspirations, "
            + "achievements = EXCLUDED.achievements, "
            + "general_information = EXCLUDED.general_information, "
            + "default_resume_language_id = EXCLUDED.default_resume_language_id, "
            + "additional_resume_language_id = EXCLUDED.additional_resume_language_id, "
            + "ready_for_relocation = EXCLUDED.ready_for_relocation, "
            + "ready_for_business_trips = EXCLUDED.ready_for_business_trips, "
            + "date_of_birth = EXCLUDED.date_of_birth, "
            + "citizenship = EXCLUDED.citizenship, "
            + "photo_file_path = EXCLUDED.photo_file_path, "
            + "updated_at = NOW() "
            + "RETURNING id";

    private final DataSource dataSource;

    public AdditionalProfileInfoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --- Auto-managed connection methods ---

    public AdditionalProfileInfo findByUserId(UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return findByUserId(userId, conn);
        } catch (SQLException e) {
            log.error("Error finding additional info for user: {}", userId, e);
            throw new RuntimeException("Database error finding additional info", e);
        }
    }

    /**
     * Upsert: INSERT if not exists, UPDATE if exists (1:1 with users).
     */
    public AdditionalProfileInfo upsert(AdditionalProfileInfo info) {
        try (Connection conn = dataSource.getConnection()) {
            return upsert(info, conn);
        } catch (SQLException e) {
            log.error("Error upserting additional info for user: {}", info.getUserId(), e);
            throw new RuntimeException("Database error saving additional info", e);
        }
    }

    // --- Connection-accepting overloads (D10) ---

    public AdditionalProfileInfo findByUserId(UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public AdditionalProfileInfo upsert(AdditionalProfileInfo info, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPSERT)) {
            stmt.setObject(1, info.getUserId());
            stmt.setString(2, info.getSkills());
            stmt.setString(3, info.getLanguages());
            stmt.setString(4, info.getProfessionalAspirations());
            stmt.setString(5, info.getAchievements());
            stmt.setString(6, info.getGeneralInformation());
            setLongOrNull(stmt, 7, info.getDefaultResumeLanguageId());
            setLongOrNull(stmt, 8, info.getAdditionalResumeLanguageId());
            stmt.setString(9, info.getReadyForRelocation());
            stmt.setString(10, info.getReadyForBusinessTrips());
            stmt.setDate(11, Date.valueOf(info.getDateOfBirth()));
            stmt.setString(12, info.getCitizenship());
            stmt.setString(13, info.getPhotoFilePath());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    info.setId(rs.getLong("id"));
                }
            }
            log.debug("Additional info upserted: userId={}", info.getUserId());
            return info;
        }
    }

    // --- Row mapping ---

    private AdditionalProfileInfo mapRow(ResultSet rs) throws SQLException {
        AdditionalProfileInfo a = new AdditionalProfileInfo();
        a.setId(rs.getLong("id"));
        a.setUserId((UUID) rs.getObject("user_id"));
        a.setSkills(rs.getString("skills"));
        a.setLanguages(rs.getString("languages"));
        a.setProfessionalAspirations(rs.getString("professional_aspirations"));
        a.setAchievements(rs.getString("achievements"));
        a.setGeneralInformation(rs.getString("general_information"));
        a.setDefaultResumeLanguageId(getLongOrNull(rs, "default_resume_language_id"));
        a.setAdditionalResumeLanguageId(getLongOrNull(rs, "additional_resume_language_id"));
        a.setReadyForRelocation(rs.getString("ready_for_relocation"));
        a.setReadyForBusinessTrips(rs.getString("ready_for_business_trips"));
        a.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        a.setCitizenship(rs.getString("citizenship"));
        a.setPhotoFilePath(rs.getString("photo_file_path"));
        a.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            a.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return a;
    }

    // --- Helpers ---

    private void setLongOrNull(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) {
            stmt.setLong(index, value);
        } else {
            stmt.setNull(index, Types.BIGINT);
        }
    }

    private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long val = rs.getLong(column);
        return rs.wasNull() ? null : val;
    }
}
