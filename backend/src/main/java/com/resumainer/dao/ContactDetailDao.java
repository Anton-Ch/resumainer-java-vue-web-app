package com.resumainer.dao;

import com.resumainer.model.ContactDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO for the 'contact_detail' table (UUID PK, 1:1 with users).
 * Supports read, create (empty shell), and update operations.
 * All queries use PreparedStatement (Constitution IV).
 */
@Repository
public class ContactDetailDao {

    private static final Logger log = LoggerFactory.getLogger(ContactDetailDao.class);

    private static final String SELECT_BY_USER_ID =
            "SELECT id, user_id, full_name, professional_title, phone, resume_email, location, "
            + "linkedin_url, portfolio_url, telegram, whatsapp, "
            + "created_at, updated_at FROM contact_detail WHERE user_id = ?";

    private static final String INSERT =
            "INSERT INTO contact_detail (user_id) VALUES (?)";

    private static final String UPDATE =
            "UPDATE contact_detail SET full_name = ?, professional_title = ?, phone = ?, "
            + "resume_email = ?, location = ?, linkedin_url = ?, portfolio_url = ?, "
            + "telegram = ?, whatsapp = ?, updated_at = NOW() "
            + "WHERE user_id = ?";

    private final DataSource dataSource;

    public ContactDetailDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --- Auto-managed connection methods ---

    public ContactDetail findByUserId(UUID userId) {
        try (Connection conn = dataSource.getConnection()) {
            return findByUserId(userId, conn);
        } catch (SQLException e) {
            log.error("Error finding contact detail for user: {}", userId, e);
            throw new RuntimeException("Database error finding contact detail", e);
        }
    }

    public void create(ContactDetail contactDetail) {
        try (Connection conn = dataSource.getConnection()) {
            create(contactDetail, conn);
        } catch (SQLException e) {
            log.error("Error creating contact detail for user: {}", contactDetail.getUserId(), e);
            throw new RuntimeException("Database error creating contact detail", e);
        }
    }

    public void update(ContactDetail contactDetail) {
        try (Connection conn = dataSource.getConnection()) {
            update(contactDetail, conn);
        } catch (SQLException e) {
            log.error("Error updating contact detail for user: {}", contactDetail.getUserId(), e);
            throw new RuntimeException("Database error updating contact detail", e);
        }
    }

    // --- Connection-accepting overloads (D10) ---

    public ContactDetail findByUserId(UUID userId, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER_ID)) {
            stmt.setObject(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public void create(ContactDetail contactDetail, Connection conn) throws SQLException {
        if (contactDetail == null) {
            throw new IllegalArgumentException("ContactDetail must not be null");
        }
        if (contactDetail.getUserId() == null) {
            throw new IllegalArgumentException("ContactDetail userId must not be null");
        }
        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {
            stmt.setObject(1, contactDetail.getUserId());
            int rows = stmt.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("Unexpected insert result: " + rows);
            }
            log.debug("Contact detail created for user: {}", contactDetail.getUserId());
        }
    }

    public void update(ContactDetail contactDetail, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            stmt.setString(1, contactDetail.getFullName());
            stmt.setString(2, contactDetail.getProfessionalTitle());
            stmt.setString(3, contactDetail.getPhone());
            stmt.setString(4, contactDetail.getResumeEmail());
            stmt.setString(5, contactDetail.getLocation());
            stmt.setString(6, contactDetail.getLinkedinUrl());
            stmt.setString(7, contactDetail.getPortfolioUrl());
            stmt.setString(8, contactDetail.getTelegram());
            stmt.setString(9, contactDetail.getWhatsapp());
            stmt.setObject(10, contactDetail.getUserId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new RuntimeException("Contact detail not found for user: "
                        + contactDetail.getUserId());
            }
            log.debug("Contact detail updated for user: {}", contactDetail.getUserId());
        }
    }

    // --- Row mapping ---

    private ContactDetail mapRow(ResultSet rs) throws SQLException {
        ContactDetail cd = new ContactDetail();
        cd.setId(rs.getObject("id", UUID.class));
        cd.setUserId(rs.getObject("user_id", UUID.class));
        cd.setFullName(rs.getString("full_name"));
        cd.setProfessionalTitle(rs.getString("professional_title"));
        cd.setPhone(rs.getString("phone"));
        cd.setResumeEmail(rs.getString("resume_email"));
        cd.setLocation(rs.getString("location"));
        cd.setLinkedinUrl(rs.getString("linkedin_url"));
        cd.setPortfolioUrl(rs.getString("portfolio_url"));
        cd.setTelegram(rs.getString("telegram"));
        cd.setWhatsapp(rs.getString("whatsapp"));
        cd.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        cd.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return cd;
    }
}
