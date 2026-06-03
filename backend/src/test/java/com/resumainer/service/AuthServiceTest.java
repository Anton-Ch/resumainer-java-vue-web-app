package com.resumainer.service;

import com.resumainer.dao.ContactDetailDao;
import com.resumainer.dao.RoleDao;
import com.resumainer.dao.UserDao;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.Role;
import com.resumainer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.UUID;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserDao userDao;
    private RoleDao roleDao;
    private ContactDetailDao contactDetailDao;
    private PasswordService passwordService;
    private DataSource dataSource;
    private Connection connection;
    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        userDao = mock(UserDao.class);
        roleDao = mock(RoleDao.class);
        contactDetailDao = mock(ContactDetailDao.class);
        passwordService = mock(PasswordService.class);
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);

        authService = new AuthService(userDao, roleDao, contactDetailDao,
                passwordService, dataSource);
    }

    @Test
    void register_validInput_createsUserAndContactDetail() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "StrongPass1");
        when(passwordService.isStrongPassword("StrongPass1")).thenReturn(true);
        when(passwordService.hashPassword("StrongPass1")).thenReturn("$2a$12$hashvalue");
        when(userDao.findByEmail("test@example.com")).thenReturn(null);
        when(roleDao.findByCode("USER")).thenReturn(new Role(1L, "USER", "Regular User"));

        // Act
        User result = authService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("$2a$12$hashvalue", result.getPasswordHash());
        assertEquals(1L, result.getRoleId());
        verify(passwordService).hashPassword("StrongPass1");
        verify(userDao).create(any(User.class), any(Connection.class));
        verify(contactDetailDao).create(any(), any(Connection.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existing@example.com", "StrongPass1", "StrongPass1");
        when(passwordService.isStrongPassword("StrongPass1")).thenReturn(true);
        when(userDao.findByEmail("existing@example.com")).thenReturn(new User());

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.register(request));
        assertEquals("auth.email.alreadyRegistered", ex.getErrorCode());
        verify(userDao, never()).create(any());
    }

    @Test
    void register_passwordMismatch_throwsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "StrongPass1", "DifferentPass1");

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.register(request));
        assertEquals("auth.password.mismatch", ex.getErrorCode());
        verify(userDao, never()).create(any());
    }

    @Test
    void register_weakPassword_throwsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "weak", "weak");

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.register(request));
        assertEquals("auth.password.weak", ex.getErrorCode());
        verify(userDao, never()).create(any());
    }
}
