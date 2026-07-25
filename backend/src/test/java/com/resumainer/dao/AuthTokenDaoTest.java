package com.resumainer.dao;

import com.resumainer.model.AuthToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthTokenDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private AuthTokenDao authTokenDao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        authTokenDao = new AuthTokenDao(dataSource);
    }

    @Test
    void insert_token_persistsSuccessfully() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        AuthToken token = new AuthToken(
                "550e8400-e29b-41d4-a716-446655440000",
                "EMAIL_VERIFICATION",
                "abcdef123456hash",
                LocalDateTime.now().plusHours(24)
        );

        AuthToken result = authTokenDao.insert(token);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNotNull(result.getCreatedAt());
        verify(preparedStatement).setObject(1, "550e8400-e29b-41d4-a716-446655440000");
        verify(preparedStatement).setString(2, "EMAIL_VERIFICATION");
        verify(preparedStatement).setString(3, "abcdef123456hash");
    }

    @Test
    void findByHash_existingToken_returnsToken() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("user_id")).thenReturn("550e8400-e29b-41d4-a716-446655440000");
        when(resultSet.getString("token_type")).thenReturn("EMAIL_VERIFICATION");
        when(resultSet.getString("token_hash")).thenReturn("hash123");
        when(resultSet.getObject("expires_at", LocalDateTime.class)).thenReturn(LocalDateTime.now().plusHours(24));
        when(resultSet.getObject("consumed_at", LocalDateTime.class)).thenReturn(null);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        AuthToken result = authTokenDao.findByHash("hash123", "EMAIL_VERIFICATION");

        assertNotNull(result);
        assertEquals("EMAIL_VERIFICATION", result.getTokenType());
        assertEquals("hash123", result.getTokenHash());
        assertFalse(result.isConsumed());
        assertFalse(result.isExpired());
        verify(preparedStatement).setString(1, "hash123");
        verify(preparedStatement).setString(2, "EMAIL_VERIFICATION");
    }

    @Test
    void findByHash_notFound_returnsNull() throws Exception {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        AuthToken result = authTokenDao.findByHash("nonexistent", "EMAIL_VERIFICATION");

        assertNull(result);
    }

    @Test
    void markConsumed_updatesTimestamp() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        authTokenDao.markConsumed(1L);

        verify(preparedStatement).setObject(eq(1), any(LocalDateTime.class));
        verify(preparedStatement).setLong(2, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void findByHash_withConnection_usesForUpdate() throws Exception {
        // The Connection-accepting overload uses SELECT ... FOR UPDATE
        // We verify it goes through the correct SQL path
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("user_id")).thenReturn("550e8400-e29b-41d4-a716-446655440000");
        when(resultSet.getString("token_type")).thenReturn("EMAIL_VERIFICATION");
        when(resultSet.getString("token_hash")).thenReturn("hash123");
        when(resultSet.getObject("expires_at", LocalDateTime.class)).thenReturn(LocalDateTime.now().plusHours(24));
        when(resultSet.getObject("consumed_at", LocalDateTime.class)).thenReturn(null);
        when(resultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        AuthToken result = authTokenDao.findByHash("hash123", "EMAIL_VERIFICATION", connection);

        assertNotNull(result);
        // Verify the SQL contains FOR UPDATE by checking the prepared statement creation
        verify(connection).prepareStatement(contains("FOR UPDATE"));
    }

    @Test
    void invalidateOldTokens_updatesActiveTokens() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(2);

        authTokenDao.invalidateOldTokens("550e8400-e29b-41d4-a716-446655440000", "EMAIL_VERIFICATION");

        verify(preparedStatement).setObject(eq(1), any(LocalDateTime.class));
        verify(preparedStatement).setObject(2, "550e8400-e29b-41d4-a716-446655440000");
        verify(preparedStatement).setString(3, "EMAIL_VERIFICATION");
        verify(preparedStatement).executeUpdate();
    }
}
