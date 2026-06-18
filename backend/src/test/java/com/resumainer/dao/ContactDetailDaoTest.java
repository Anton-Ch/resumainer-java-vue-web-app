package com.resumainer.dao;

import com.resumainer.model.ContactDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for ContactDetailDao.
 * Covers findByUserId, create, update (both auto-connection and connection-accepting overloads),
 * null/validation guards, mapRow, and SQL error handling.
 */
class ContactDetailDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private ContactDetailDao dao;

    private final UUID userId = UUID.randomUUID();
    private final UUID contactId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(statement.executeUpdate()).thenReturn(1);

        dao = new ContactDetailDao(dataSource);
    }

    // ─── findByUserId (auto-connection) ───────────────────────────

    @Test
    void findByUserId_returnsContact_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        stubResultSet();

        ContactDetail result = dao.findByUserId(userId);

        assertNotNull(result);
        assertEquals(contactId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals("John Doe", result.getFullName());
        assertEquals("Developer", result.getProfessionalTitle());
        assertEquals("john@example.com", result.getResumeEmail());
        verify(statement).setObject(1, userId);
    }

    @Test
    void findByUserId_returnsNull_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        ContactDetail result = dao.findByUserId(userId);

        assertNull(result);
    }

    @Test
    void findByUserId_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.findByUserId(userId));

        assertTrue(ex.getMessage().contains("Database error finding contact detail"));
    }

    // ─── findByUserId (connection-accepting) ──────────────────────

    @Test
    void findByUserId_withConnection_returnsContact_whenFound() throws Exception {
        when(resultSet.next()).thenReturn(true);
        stubResultSet();

        ContactDetail result = dao.findByUserId(userId, connection);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        verify(connection, never()).close();
    }

    @Test
    void findByUserId_withConnection_returnsNull_whenNotFound() throws Exception {
        when(resultSet.next()).thenReturn(false);

        ContactDetail result = dao.findByUserId(userId, connection);

        assertNull(result);
    }

    // ─── create (auto-connection) ──────────────────────────────────

    @Test
    void create_insertsContactDetail() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        ContactDetail cd = ContactDetail.createEmpty(userId);
        dao.create(cd);

        verify(connection).prepareStatement(
            "INSERT INTO contact_detail (user_id) VALUES (?)");
        verify(statement).setObject(1, userId);
        verify(statement).executeUpdate();
    }

    @Test
    void create_throwsException_onNullInput() {
        assertThrows(IllegalArgumentException.class, () -> dao.create((ContactDetail) null));
    }

    @Test
    void create_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        ContactDetail cd = ContactDetail.createEmpty(userId);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.create(cd));

        assertTrue(ex.getMessage().contains("Database error creating contact detail"));
    }

    @Test
    void create_throwsException_onUnexpectedRowCount() throws Exception {
        when(statement.executeUpdate()).thenReturn(2);

        ContactDetail cd = ContactDetail.createEmpty(userId);
        assertThrows(RuntimeException.class, () -> dao.create(cd));
    }

    // ─── create (connection-accepting) ─────────────────────────────

    @Test
    void create_withConnection_insertsContact() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        ContactDetail cd = ContactDetail.createEmpty(userId);
        dao.create(cd, connection);

        verify(connection, never()).close();
    }

    @Test
    void create_withConnection_throwsException_onNullInput() {
        assertThrows(IllegalArgumentException.class,
                () -> dao.create(null, connection));
    }

    @Test
    void create_withConnection_throwsException_onNullUserId() throws Exception {
        ContactDetail cd = ContactDetail.createEmpty(null);
        assertThrows(IllegalArgumentException.class,
                () -> dao.create(cd, connection));
    }

    @Test
    void create_withConnection_throwsException_onUnexpectedRowCount() throws Exception {
        when(statement.executeUpdate()).thenReturn(2);

        ContactDetail cd = ContactDetail.createEmpty(userId);
        assertThrows(RuntimeException.class,
                () -> dao.create(cd, connection));
    }

    // ─── update (auto-connection) ──────────────────────────────────

    @Test
    void update_updatesContactDetail() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        ContactDetail cd = makeContactDetail();
        dao.update(cd);

        verify(statement).setString(1, "John Doe");
        verify(statement).setString(2, "Developer");
        verify(statement).setString(3, "+1234567890");
        verify(statement).setString(4, "john@example.com");
        verify(statement).setString(5, "New York");
        verify(statement).setString(6, "https://linkedin.com");
        verify(statement).setString(7, "https://portfolio.com");
        verify(statement).setString(8, "@johndoe");
        verify(statement).setString(9, "+1234567890");
        verify(statement).setObject(10, userId);
        verify(statement).executeUpdate();
    }

    @Test
    void update_throwsException_whenNotFound() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        ContactDetail cd = makeContactDetail();
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.update(cd));

        assertTrue(ex.getMessage().contains("Contact detail not found"));
    }

    @Test
    void update_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));

        ContactDetail cd = makeContactDetail();
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.update(cd));

        assertTrue(ex.getMessage().contains("Database error updating contact detail"));
    }

    // ─── update (connection-accepting) ────────────────────────────

    @Test
    void update_withConnection_updatesContact() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        ContactDetail cd = makeContactDetail();
        dao.update(cd, connection);

        verify(connection, never()).close();
    }

    @Test
    void update_withConnection_throwsException_whenNotFound() throws Exception {
        when(statement.executeUpdate()).thenReturn(0);

        ContactDetail cd = makeContactDetail();
        assertThrows(RuntimeException.class,
                () -> dao.update(cd, connection));
    }

    // ─── helper ───────────────────────────────────────────────────

    private ContactDetail makeContactDetail() {
        ContactDetail cd = new ContactDetail();
        cd.setId(contactId);
        cd.setUserId(userId);
        cd.setFullName("John Doe");
        cd.setProfessionalTitle("Developer");
        cd.setPhone("+1234567890");
        cd.setResumeEmail("john@example.com");
        cd.setLocation("New York");
        cd.setLinkedinUrl("https://linkedin.com");
        cd.setPortfolioUrl("https://portfolio.com");
        cd.setTelegram("@johndoe");
        cd.setWhatsapp("+1234567890");
        return cd;
    }

    private void stubResultSet() throws Exception {
        when(resultSet.getObject("id", UUID.class)).thenReturn(contactId);
        when(resultSet.getObject("user_id", UUID.class)).thenReturn(userId);
        when(resultSet.getString("full_name")).thenReturn("John Doe");
        when(resultSet.getString("professional_title")).thenReturn("Developer");
        when(resultSet.getString("phone")).thenReturn("+1234567890");
        when(resultSet.getString("resume_email")).thenReturn("john@example.com");
        when(resultSet.getString("location")).thenReturn("New York");
        when(resultSet.getString("linkedin_url")).thenReturn("https://linkedin.com");
        when(resultSet.getString("portfolio_url")).thenReturn("https://portfolio.com");
        when(resultSet.getString("telegram")).thenReturn("@johndoe");
        when(resultSet.getString("whatsapp")).thenReturn("+1234567890");
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.of(2024, 1, 1, 12, 0));
        when(resultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(null);
    }
}
