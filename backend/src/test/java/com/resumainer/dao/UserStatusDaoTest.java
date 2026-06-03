package com.resumainer.dao;

import com.resumainer.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserStatusDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private UserStatusDao userStatusDao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        userStatusDao = new UserStatusDao(dataSource);
    }

    @Test
    void findByCode_existingStatus_returnsUserStatus() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("code")).thenReturn("ACTIVE");
        when(resultSet.getString("name")).thenReturn("Active");

        UserStatus result = userStatusDao.findByCode("ACTIVE");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ACTIVE", result.getCode());
        assertEquals("Active", result.getName());
        verify(preparedStatement).setString(1, "ACTIVE");
    }

    @Test
    void findByCode_missing_returnsNull() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertNull(userStatusDao.findByCode("UNKNOWN"));
    }

    @Test
    void findByCode_nullInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> userStatusDao.findByCode(null));
    }

    @Test
    void findByCode_emptyInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> userStatusDao.findByCode(""));
    }
}
