package com.resumainer.dao;

import com.resumainer.model.ContactDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO for the 'contact_detail' table (UUID PK).
 * Created as empty shell on user registration.
 */
public class ContactDetailDao {

    private static final Logger log = LoggerFactory.getLogger(ContactDetailDao.class);

    private static final String INSERT = "INSERT INTO contact_detail (user_id) VALUES (?)";

    private final DataSource dataSource;

    public ContactDetailDao(DataSource dataSource) {
        this.dataSource = dataSource;
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
}
