package com.resumainer.dao;

import com.resumainer.model.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LanguageDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private LanguageDao languageDao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        languageDao = new LanguageDao(dataSource);
    }

    @Test
    void findByCode_existing_returnsLanguage() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("code")).thenReturn("EN");
        when(resultSet.getString("name")).thenReturn("English");

        Language result = languageDao.findByCode("EN");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EN", result.getCode());
        assertEquals("English", result.getName());
        verify(preparedStatement).setString(1, "EN");
    }

    @Test
    void findByCode_missing_returnsNull() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertNull(languageDao.findByCode("UNKNOWN"));
    }

    @Test
    void findByCode_nullInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> languageDao.findByCode(null));
    }

    @Test
    void findByCode_emptyInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> languageDao.findByCode(""));
    }
}
