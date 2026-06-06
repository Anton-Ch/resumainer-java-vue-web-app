package com.resumainer.dao;

import com.resumainer.model.ContactDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class ContactDetailDao {

    private static final Logger log = LoggerFactory.getLogger(ContactDetailDao.class);

    private static final String INSERT = "INSERT INTO contact_detail (user_id) VALUES (?)";

    private final DataSource dataSource;

    public ContactDetailDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String SELECT_BY_USER_ID =
            "SELECT id, user_id, full_name, phone, resume_email, location, " +
            "created_at, updated_at FROM contact_detail WHERE user_id = ?";

    /**
     * Find contact detail by user ID.
     *
     * @param userId the user's UUID
     * @return ContactDetail if found, null otherwise
     */
    public ContactDetail findByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        log.debug("Finding contact detail by userId: {}", userId);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USER_ID)) {

            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            log.error("Error finding contact detail for user: {}", userId, e);
            throw new RuntimeException("Database error finding contact detail", e);
        }
    }

    /**
     * Create an empty contact detail shell (auto-managed connection).
     *
     * @param contactDetail the contact detail to create (must have userId set)
     */
    public void create(ContactDetail contactDetail) {
        try (Connection conn = dataSource.getConnection()) {
            create(contactDetail, conn);
        } catch (SQLException e) {
            log.error("Error creating contact detail for user: {}", contactDetail.getUserId(), e);
            throw new RuntimeException("Database error creating contact detail", e);
        }
    }

    /**
     * Create contact detail within an existing connection (for transaction support).
     *
     * @param contactDetail the contact detail to create
     * @param conn the existing database connection
     */
    public void create(ContactDetail contactDetail, Connection conn) {
        if (contactDetail == null) {
            throw new IllegalArgumentException("ContactDetail must not be null");
        }
        if (contactDetail.getUserId() == null) {
            throw new IllegalArgumentException("ContactDetail userId must not be null");
        }

        log.debug("Creating contact detail for user: {}", contactDetail.getUserId());

        try (PreparedStatement stmt = conn.prepareStatement(INSERT)) {

            stmt.setObject(1, contactDetail.getUserId());

            int rows = stmt.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("Unexpected insert result: " + rows);
            }
            log.debug("Contact detail created for user: {}", contactDetail.getUserId());

        } catch (SQLException e) {
            log.error("Error creating contact detail for user: {}", contactDetail.getUserId(), e);
            throw new RuntimeException("Database error creating contact detail", e);
        }
    }

    private ContactDetail mapRow(ResultSet rs) throws SQLException {
        ContactDetail cd = new ContactDetail();
        cd.setId(rs.getObject("id", UUID.class));
        cd.setUserId(rs.getObject("user_id", UUID.class));
        cd.setFullName(rs.getString("full_name"));
        cd.setPhone(rs.getString("phone"));
        cd.setResumeEmail(rs.getString("resume_email"));
        cd.setLocation(rs.getString("location"));
        cd.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        cd.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return cd;
    }
}
