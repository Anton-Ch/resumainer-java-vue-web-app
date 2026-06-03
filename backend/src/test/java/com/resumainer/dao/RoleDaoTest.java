package com.resumainer.dao;

import com.resumainer.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private RoleDao roleDao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        roleDao = new RoleDao(dataSource);
    }

    @Test
    void findByCode_existingRole_returnsRole() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("code")).thenReturn("USER");
        when(resultSet.getString("name")).thenReturn("Regular User");

        Role result = roleDao.findByCode("USER");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("USER", result.getCode());
        assertEquals("Regular User", result.getName());
        verify(connection).prepareStatement("SELECT id, code, name FROM role WHERE code = ?");
        verify(preparedStatement).setString(1, "USER");
    }

    @Test
    void findByCode_missingRole_returnsNull() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Role result = roleDao.findByCode("UNKNOWN");

        assertNull(result);
    }

    @Test
    void findByCode_nullInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> roleDao.findByCode(null));
    }

    @Test
    void findByCode_emptyInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> roleDao.findByCode(""));
    }
}
