package com.resumainer.service;

import com.resumainer.dao.ContactDetailDao;
import com.resumainer.dao.RoleDao;
import com.resumainer.dao.UserDao;
import com.resumainer.dto.LoginRequest;
import com.resumainer.dto.RegisterRequest;
import com.resumainer.exception.ServiceException;
import com.resumainer.model.Role;
import com.resumainer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.UUID;

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
    void register_validInput_createsUserAndContactDetail() throws Exception {
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

    // ============================================================
    // Authenticate Tests
    // ============================================================

    private User createTestUser(String email, long statusId, int failedAttempts, LocalDateTime lockedUntil) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("$2a$12$existingHashValue");
        user.setRoleId(1L);
        user.setStatusId(statusId);
        user.setFailedLoginAttempts(failedAttempts);
        user.setLockedUntil(lockedUntil);
        return user;
    }

    @Test
    void authenticate_validCredentials_returnsUser() {
        // Arrange
        User user = createTestUser("test@example.com", 1L, 0, null);
        when(userDao.findByEmail("test@example.com")).thenReturn(user);
        when(passwordService.verifyPassword("CorrectPass1", "$2a$12$existingHashValue")).thenReturn(true);

        LoginRequest request = new LoginRequest("test@example.com", "CorrectPass1", false);

        // Act
        User result = authService.authenticate(request);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userDao).resetLoginAttempts(user.getId());
    }

    @Test
    void authenticate_wrongPassword_throwsException() {
        // Arrange
        User user = createTestUser("test@example.com", 1L, 0, null);
        when(userDao.findByEmail("test@example.com")).thenReturn(user);
        when(passwordService.verifyPassword("WrongPass1", "$2a$12$existingHashValue")).thenReturn(false);

        LoginRequest request = new LoginRequest("test@example.com", "WrongPass1", false);

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.authenticate(request));
        assertEquals("auth.invalidCredentials", ex.getErrorCode());
        verify(userDao).updateLoginAttempts(eq(user.getId()), eq(1), any());
        verify(userDao, never()).resetLoginAttempts(any());
    }

    @Test
    void authenticate_blockedAccount_throwsException() {
        // Arrange
        User user = createTestUser("blocked@example.com", 2L, 0, null); // statusId=2 = BLOCKED
        when(userDao.findByEmail("blocked@example.com")).thenReturn(user);

        LoginRequest request = new LoginRequest("blocked@example.com", "AnyPass1", false);

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.authenticate(request));
        assertEquals("auth.account.blocked", ex.getErrorCode());
        verify(userDao, never()).updateLoginAttempts(any(), anyInt(), any());
        verify(userDao, never()).resetLoginAttempts(any());
    }

    @Test
    void authenticate_lockedAccount_throwsException() {
        // Arrange
        User user = createTestUser("locked@example.com", 1L, 5, LocalDateTime.now().plusHours(1));
        when(userDao.findByEmail("locked@example.com")).thenReturn(user);

        LoginRequest request = new LoginRequest("locked@example.com", "AnyPass1", false);

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.authenticate(request));
        assertEquals("auth.account.locked", ex.getErrorCode());
        verify(userDao, never()).updateLoginAttempts(any(), anyInt(), any());
        verify(userDao, never()).resetLoginAttempts(any());
    }

    @Test
    void authenticate_after5FailedAttempts_locksAccount() {
        // Arrange
        User user = createTestUser("test@example.com", 1L, 4, null); // 4 failed attempts already
        when(userDao.findByEmail("test@example.com")).thenReturn(user);
        when(passwordService.verifyPassword("WrongPass1", "$2a$12$existingHashValue")).thenReturn(false);

        LoginRequest request = new LoginRequest("test@example.com", "WrongPass1", false);

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.authenticate(request));
        assertEquals("auth.account.locked", ex.getErrorCode());
        // Should set lockedUntil (not null) because failed attempts now = 5
        verify(userDao).updateLoginAttempts(eq(user.getId()), eq(5), argThat(locked -> locked != null));
    }

    @Test
    void authenticate_nonExistentEmail_throwsException() {
        // Arrange
        when(userDao.findByEmail("unknown@example.com")).thenReturn(null);

        LoginRequest request = new LoginRequest("unknown@example.com", "SomePass1", false);

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class,
                () -> authService.authenticate(request));
        assertEquals("auth.invalidCredentials", ex.getErrorCode());
        // Generic error — no email enumeration
    }
}
