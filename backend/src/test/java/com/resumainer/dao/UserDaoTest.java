package com.resumainer.dao;

import com.resumainer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        userDao = new UserDao(dataSource);
    }

    @Test
    void create_user_persistsSuccessfully() throws Exception {
        UUID generatedId = UUID.randomUUID();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id", UUID.class)).thenReturn(generatedId);

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$hash");
        user.setRoleId(1L);
        user.setStatusId(1L);
        user.setPermissionId(1L);

        userDao.create(user);

        assertEquals(generatedId, user.getId());
        verify(preparedStatement).setString(1, "test@example.com");
        verify(preparedStatement).setString(2, "$2a$10$hash");
    }

    @Test
    void findByEmail_existingUser_returnsUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id", UUID.class)).thenReturn(userId);
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("password_hash")).thenReturn("$2a$10$hash");
        when(resultSet.getLong("role_id")).thenReturn(1L);
        when(resultSet.getLong("status_id")).thenReturn(1L);
        when(resultSet.getLong("permission_id")).thenReturn(1L);
        when(resultSet.getInt("failed_login_attempts")).thenReturn(0);
        when(resultSet.getBoolean("is_deleted")).thenReturn(false);

        User result = userDao.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(1L, result.getRoleId());
        verify(preparedStatement).setString(1, "test@example.com");
    }

    @Test
    void findByEmail_missingUser_returnsNull() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertNull(userDao.findByEmail("unknown@example.com"));
    }

    @Test
    void findVerificationCandidateForUpdate_selectsOnlyRequiredColumnsAndReturnsDeletedRow() throws Exception {
        UUID userId = UUID.randomUUID();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id", UUID.class)).thenReturn(userId);
        when(resultSet.getObject("status_id", Long.class)).thenReturn(1L);
        when(resultSet.getBoolean("email_verified")).thenReturn(false);
        when(resultSet.getBoolean("is_deleted")).thenReturn(true);

        UserDao.VerificationCandidate result = userDao.findVerificationCandidateForUpdate(
                "deleted@example.com", connection);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals(1L, result.statusId());
        assertFalse(result.emailVerified());
        assertTrue(result.deleted(), "locked lookup must return deleted rows for generic handling");

        var sqlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sqlCaptor.capture());
        String sql = sqlCaptor.getValue().toLowerCase(java.util.Locale.ROOT);
        assertTrue(sql.contains("select id, status_id, email_verified, is_deleted"));
        assertTrue(sql.contains("for update"));
        assertFalse(sql.contains("password_hash"));
        assertFalse(sql.contains("locked_until"));
        assertFalse(sql.contains("failed_login_attempts"));
        assertFalse(sql.contains("password_login_enabled"));
        verify(dataSource, never()).getConnection();
    }

    @Test
    void findById_existingUser_returnsUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject("id", UUID.class)).thenReturn(userId);
        when(resultSet.getString("email")).thenReturn("test@example.com");

        User result = userDao.findById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(preparedStatement).setObject(1, userId);
    }

    @Test
    void updateLoginAttempts_setsAttemptsAndLock() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDateTime lockTime = LocalDateTime.now().plusMinutes(15);
        Timestamp expectedTimestamp = Timestamp.valueOf(lockTime);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        userDao.updateLoginAttempts(userId, 5, lockTime);

        verify(preparedStatement).setInt(1, 5);
        verify(preparedStatement).setObject(2, expectedTimestamp);
        verify(preparedStatement).setObject(3, userId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void resetLoginAttempts_resetsCounterAndLock() throws Exception {
        UUID userId = UUID.randomUUID();
        when(preparedStatement.executeUpdate()).thenReturn(1);

        userDao.resetLoginAttempts(userId);

        verify(preparedStatement).setInt(1, 0);
        verify(preparedStatement).setObject(2, (Object) null);
        verify(preparedStatement).setObject(3, userId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void findByEmail_nullInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> userDao.findByEmail(null));
    }

    @Test
    void findById_nullInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> userDao.findById(null));
    }
}
