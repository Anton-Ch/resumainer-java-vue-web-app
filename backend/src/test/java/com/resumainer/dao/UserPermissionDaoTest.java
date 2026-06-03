package com.resumainer.dao;

import com.resumainer.model.UserPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserPermissionDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private UserPermissionDao userPermissionDao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        userPermissionDao = new UserPermissionDao(dataSource);
    }

    @Test
    void findByCode_existing_returnsUserPermission() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("code")).thenReturn("ALLOWED");
        when(resultSet.getString("name")).thenReturn("Allowed");

        UserPermission result = userPermissionDao.findByCode("ALLOWED");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ALLOWED", result.getCode());
        assertEquals("Allowed", result.getName());
        verify(preparedStatement).setString(1, "ALLOWED");
    }

    @Test
    void findByCode_missing_returnsNull() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertNull(userPermissionDao.findByCode("UNKNOWN"));
    }

    @Test
    void findByCode_nullInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> userPermissionDao.findByCode(null));
    }

    @Test
    void findByCode_emptyInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> userPermissionDao.findByCode(""));
    }
}
