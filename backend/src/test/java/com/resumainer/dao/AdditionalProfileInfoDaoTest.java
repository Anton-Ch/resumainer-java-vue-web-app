package com.resumainer.dao;

import com.resumainer.model.AdditionalProfileInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdditionalProfileInfoDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private AdditionalProfileInfoDao dao;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        dao = new AdditionalProfileInfoDao(dataSource);
        userId = UUID.randomUUID();
    }

    @Test
    void findByUserId_found_returnsInfo() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("skills")).thenReturn("Java, Spring");
        when(resultSet.getString("languages")).thenReturn("English C1");
        when(resultSet.getString("professional_aspirations")).thenReturn("Architect");
        when(resultSet.getString("achievements")).thenReturn("Built platform");
        when(resultSet.getString("general_information")).thenReturn("AI notes");
        when(resultSet.getLong("default_resume_language_id")).thenReturn(1L);
        when(resultSet.getLong("additional_resume_language_id")).thenReturn(2L);
        when(resultSet.getString("ready_for_relocation")).thenReturn("Negotiable");
        when(resultSet.getString("ready_for_business_trips")).thenReturn("Yes");
        when(resultSet.getDate("date_of_birth")).thenReturn(Date.valueOf("1990-05-15"));
        when(resultSet.getString("citizenship")).thenReturn("Kazakhstan");
        when(resultSet.getString("photo_file_path")).thenReturn(null);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);

        AdditionalProfileInfo result = dao.findByUserId(userId);

        assertNotNull(result);
        assertEquals("Java, Spring", result.getSkills());
        assertEquals("Architect", result.getProfessionalAspirations());
        assertEquals(1L, result.getDefaultResumeLanguageId());
        assertEquals("Negotiable", result.getReadyForRelocation());
        assertEquals(LocalDate.of(1990, 5, 15), result.getDateOfBirth());
        assertEquals("Kazakhstan", result.getCitizenship());
    }

    @Test
    void findByUserId_notFound_returnsNull() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        AdditionalProfileInfo result = dao.findByUserId(userId);

        assertNull(result);
    }

    @Test
    void findByUserId_nullFields_handlesNull() throws Exception {
        // Simulate the scenario where language IDs are actually null
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("user_id")).thenReturn(userId);
        when(resultSet.getString("skills")).thenReturn(null);
        when(resultSet.getString("languages")).thenReturn(null);
        when(resultSet.getString("professional_aspirations")).thenReturn(null);
        when(resultSet.getString("achievements")).thenReturn(null);
        when(resultSet.getString("general_information")).thenReturn(null);
        when(resultSet.getLong("default_resume_language_id")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);
        when(resultSet.getLong("additional_resume_language_id")).thenReturn(0L);
        when(resultSet.getString("ready_for_relocation")).thenReturn(null);
        when(resultSet.getString("ready_for_business_trips")).thenReturn(null);
        when(resultSet.getDate("date_of_birth")).thenReturn(Date.valueOf("1990-01-01"));
        when(resultSet.getString("citizenship")).thenReturn("Test");
        when(resultSet.getString("photo_file_path")).thenReturn(null);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
        when(resultSet.getTimestamp("updated_at")).thenReturn(null);

        AdditionalProfileInfo result = dao.findByUserId(userId);

        assertNotNull(result);
        assertNull(result.getDefaultResumeLanguageId());
        assertNull(result.getAdditionalResumeLanguageId());
        assertNull(result.getSkills());
        assertNull(result.getReadyForRelocation());
    }

    @Test
    void upsert_firstTime_creates() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(42L);

        AdditionalProfileInfo info = new AdditionalProfileInfo();
        info.setUserId(userId);
        info.setDateOfBirth(LocalDate.of(1990, 5, 15));
        info.setCitizenship("Kazakhstan");

        AdditionalProfileInfo result = dao.upsert(info);

        assertEquals(42L, result.getId());
        verify(statement).setObject(1, userId);
        verify(statement).setDate(11, Date.valueOf("1990-05-15"));
        verify(statement).setString(12, "Kazakhstan");
    }

    @Test
    void upsert_secondTime_updatesExisting() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);

        AdditionalProfileInfo info = new AdditionalProfileInfo();
        info.setUserId(userId);
        info.setSkills("Kubernetes");
        info.setDateOfBirth(LocalDate.of(1990, 5, 15));
        info.setCitizenship("Kazakhstan");

        AdditionalProfileInfo result = dao.upsert(info);

        assertNotNull(result);
        verify(statement, times(1)).executeQuery();
    }
}
