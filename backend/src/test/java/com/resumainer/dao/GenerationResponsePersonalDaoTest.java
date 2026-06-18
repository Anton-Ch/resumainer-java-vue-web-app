package com.resumainer.dao;

import com.resumainer.model.GenerationResponsePersonal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for GenerationResponsePersonalDao.
 * Covers insert, findByResponseId, update (both overloads).
 */
class GenerationResponsePersonalDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private GenerationResponsePersonalDao dao;

    private final UUID responseId = UUID.randomUUID();
    private final UUID personalId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        dao = new GenerationResponsePersonalDao(dataSource);
    }

    // ─── insert (connection-accepting) ───────────────────────────

    @Test
    void insert_persistsPersonalInfo() throws Exception {
        GenerationResponsePersonal p = makePersonal();
        dao.insert(p, connection);

        verify(statement).setObject(1, responseId);
        verify(statement).setString(2, "Moscow");
        verify(statement).setString(3, "English, Russian");
        verify(statement).setString(4, "Yes");
        verify(statement).setString(5, "No");
        verify(statement).setString(6, "Russian");
        verify(statement).setDate(eq(7), any(Date.class));
        verify(statement).setString(8, "OFFICE,REMOTE");
        verify(statement).setString(9, "4.0");
        verify(statement).setInt(10, 1);
        verify(statement).executeUpdate();
    }

    @Test
    void insert_withNullDateOfBirth_handlesGracefully() throws Exception {
        GenerationResponsePersonal p = makePersonal();
        p.setDateOfBirth(null);

        assertThrows(NullPointerException.class, () -> dao.insert(p, connection));
    }

    // ─── findByResponseId ────────────────────────────────────────

    @Test
    void findByResponseId_returnsPersonal_whenFound() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        stubResultSet();

        GenerationResponsePersonal result = dao.findByResponseId(responseId);

        assertNotNull(result);
        assertEquals(personalId, result.getId());
        assertEquals(responseId, result.getResponseId());
        assertEquals("Moscow", result.getLocation());
        assertEquals("EN", result.getWillingnessToRelocate());

        verify(statement).setObject(1, responseId);
    }

    @Test
    void findByResponseId_returnsNull_whenNotFound() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        GenerationResponsePersonal result = dao.findByResponseId(responseId);

        assertNull(result);
    }

    @Test
    void findByResponseId_throwsException_onSqlError() throws Exception {
        when(statement.executeQuery()).thenThrow(new SQLException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> dao.findByResponseId(responseId));

        assertTrue(ex.getMessage().contains("Database error finding personal info"));
    }

    // ─── update (auto-connection) ────────────────────────────────

    @Test
    void update_modifiesPersonalInfo() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);
        GenerationResponsePersonal p = makePersonal();

        dao.update(p);

        verify(statement).setString(1, "Moscow");
        verify(statement).setObject(10, responseId);
        verify(statement).executeUpdate();
    }

    @Test
    void update_throwsException_onSqlError() throws Exception {
        when(statement.executeUpdate()).thenThrow(new SQLException("DB error"));
        GenerationResponsePersonal p = makePersonal();

        assertThrows(RuntimeException.class, () -> dao.update(p));
    }

    // ─── update (connection-accepting overload) ──────────────────

    @Test
    void update_withConnection_updatesAndDoesNotCloseConnection() throws Exception {
        GenerationResponsePersonal p = makePersonal();
        dao.update(p, connection);

        verify(connection, never()).close();
        verify(statement).executeUpdate();
    }

    @Test
    void update_withConnection_nullDateOfBirth_throwsNpe() throws Exception {
        GenerationResponsePersonal p = makePersonal();
        p.setDateOfBirth(null);

        // DAO calls Date.valueOf(personal.getDateOfBirth()) which throws NPE on null
        assertThrows(NullPointerException.class,
                () -> dao.update(p, connection));
    }

    // ─── helper ──────────────────────────────────────────────────

    private GenerationResponsePersonal makePersonal() {
        GenerationResponsePersonal p = new GenerationResponsePersonal();
        p.setResponseId(responseId);
        p.setLocation("Moscow");
        p.setSpokenLanguages("English, Russian");
        p.setWillingnessToRelocate("Yes");
        p.setWillingnessForBusinessTrips("No");
        p.setCitizenship("Russian");
        p.setDateOfBirth(LocalDate.of(1990, 1, 15));
        p.setWorkFormats("OFFICE,REMOTE");
        p.setGpaGrade("4.0");
        p.setOrderInResume(1);
        return p;
    }

    private void stubResultSet() throws Exception {
        when(resultSet.getObject("id")).thenReturn(personalId);
        when(resultSet.getObject("response_id")).thenReturn(responseId);
        when(resultSet.getString("location")).thenReturn("Moscow");
        when(resultSet.getString("spoken_languages")).thenReturn("English, Russian");
        when(resultSet.getString("willingness_to_relocate")).thenReturn("EN");
        when(resultSet.getString("willingness_for_business_trips")).thenReturn("NO");
        when(resultSet.getString("citizenship")).thenReturn("Russian");
        when(resultSet.getDate("date_of_birth")).thenReturn(Date.valueOf(LocalDate.of(1990, 1, 15)));
        when(resultSet.getString("work_formats")).thenReturn("OFFICE,REMOTE");
        when(resultSet.getString("gpa_grade")).thenReturn("4.0");
        when(resultSet.getInt("order_in_resume")).thenReturn(1);
    }
}
