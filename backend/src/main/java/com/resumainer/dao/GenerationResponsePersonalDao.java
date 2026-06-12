package com.resumainer.dao;

import com.resumainer.model.GenerationResponsePersonal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

/**
 * DAO for 'generation_response_personal' table.
 * One row per response. Insert + update for review edits.
 */
@Repository
public class GenerationResponsePersonalDao {

    private static final Logger log = LoggerFactory.getLogger(GenerationResponsePersonalDao.class);

    private static final String INSERT =
            "INSERT INTO generation_response_personal "
            + "(response_id, location, spoken_languages, willingness_to_relocate, "
            + "willingness_for_business_trips, citizenship, date_of_birth, work_formats, "
            + "gpa_grade, order_in_resume) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_RESPONSE =
            "SELECT * FROM generation_response_personal WHERE response_id = ?";

    private static final String UPDATE =
            "UPDATE generation_response_personal SET "
            + "location = ?, spoken_languages = ?, willingness_to_relocate = ?, "
            + "willingness_for_business_trips = ?, citizenship = ?, date_of_birth = ?, "
            + "work_formats = ?, gpa_grade = ?, order_in_resume = ?, "
            + "updated_at = NOW() WHERE response_id = ?";

    private final DataSource dataSource;

    public GenerationResponsePersonalDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(GenerationResponsePersonal personal, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setObject(1, personal.getResponseId());
            stmt.setString(2, personal.getLocation());
            stmt.setString(3, personal.getSpokenLanguages());
            stmt.setString(4, personal.getWillingnessToRelocate());
            stmt.setString(5, personal.getWillingnessForBusinessTrips());
            stmt.setString(6, personal.getCitizenship());
            stmt.setDate(7, Date.valueOf(personal.getDateOfBirth()));
            stmt.setString(8, personal.getWorkFormats());
            stmt.setString(9, personal.getGpaGrade());
            stmt.setInt(10, personal.getOrderInResume());
            stmt.executeUpdate();
        }
    }

    public GenerationResponsePersonal findByResponseId(UUID responseId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_RESPONSE)) {
            stmt.setObject(1, responseId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            log.error("Error finding personal info for response: {}", responseId, e);
            throw new RuntimeException("Database error finding personal info", e);
        }
    }

    public void update(GenerationResponsePersonal personal) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            stmt.setString(1, personal.getLocation());
            stmt.setString(2, personal.getSpokenLanguages());
            stmt.setString(3, personal.getWillingnessToRelocate());
            stmt.setString(4, personal.getWillingnessForBusinessTrips());
            stmt.setString(5, personal.getCitizenship());
            stmt.setDate(6, Date.valueOf(personal.getDateOfBirth()));
            stmt.setString(7, personal.getWorkFormats());
            stmt.setString(8, personal.getGpaGrade());
            stmt.setInt(9, personal.getOrderInResume());
            stmt.setObject(10, personal.getResponseId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating personal info for response: {}", personal.getResponseId(), e);
            throw new RuntimeException("Database error updating personal info", e);
        }
    }

    public void update(GenerationResponsePersonal personal, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            stmt.setString(1, personal.getLocation());
            stmt.setString(2, personal.getSpokenLanguages());
            stmt.setString(3, personal.getWillingnessToRelocate());
            stmt.setString(4, personal.getWillingnessForBusinessTrips());
            stmt.setString(5, personal.getCitizenship());
            stmt.setDate(6, Date.valueOf(personal.getDateOfBirth()));
            stmt.setString(7, personal.getWorkFormats());
            stmt.setString(8, personal.getGpaGrade());
            stmt.setInt(9, personal.getOrderInResume());
            stmt.setObject(10, personal.getResponseId());
            stmt.executeUpdate();
        }
    }

    private GenerationResponsePersonal mapRow(ResultSet rs) throws SQLException {
        GenerationResponsePersonal p = new GenerationResponsePersonal();
        p.setId((UUID) rs.getObject("id"));
        p.setResponseId((UUID) rs.getObject("response_id"));
        p.setLocation(rs.getString("location"));
        p.setSpokenLanguages(rs.getString("spoken_languages"));
        p.setWillingnessToRelocate(rs.getString("willingness_to_relocate"));
        p.setWillingnessForBusinessTrips(rs.getString("willingness_for_business_trips"));
        p.setCitizenship(rs.getString("citizenship"));
        p.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
        p.setWorkFormats(rs.getString("work_formats"));
        p.setGpaGrade(rs.getString("gpa_grade"));
        p.setOrderInResume(rs.getInt("order_in_resume"));
        return p;
    }
}
