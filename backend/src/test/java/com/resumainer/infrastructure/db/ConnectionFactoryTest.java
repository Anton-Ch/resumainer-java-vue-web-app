package com.resumainer.infrastructure.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionFactoryTest {

    @Mock
    private Connection mockConnection;

    @Test
    void createConnection_withInvalidUrl_throwsException() {
        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://localhost:5432/test", "u", "p", 2, 10, 5000, 2
        );
        ConnectionFactory factory = new ConnectionFactory(config);
        assertThrows(ConnectionPoolException.class, factory::createConnection);
    }

    @Test
    void isValid_validConnection_returnsTrue() throws SQLException {
        when(mockConnection.isValid(2)).thenReturn(true);
        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://localhost:5432/test", "u", "p", 2, 10, 5000, 2
        );
        ConnectionFactory factory = new ConnectionFactory(config);
        assertTrue(factory.isValid(mockConnection));
        verify(mockConnection).isValid(2);
    }

    @Test
    void isValid_closedConnection_returnsFalse() throws SQLException {
        when(mockConnection.isValid(2)).thenReturn(false);
        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://localhost:5432/test", "u", "p", 2, 10, 5000, 2
        );
        ConnectionFactory factory = new ConnectionFactory(config);
        assertFalse(factory.isValid(mockConnection));
    }

    @Test
    void isValid_nullConnection_returnsFalse() throws SQLException {
        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://localhost:5432/test", "u", "p", 2, 10, 5000, 2
        );
        ConnectionFactory factory = new ConnectionFactory(config);
        assertFalse(factory.isValid(null));
    }

    @Test
    void closeQuietly_validConnection_closesWithoutError() throws SQLException {
        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://localhost:5432/test", "u", "p", 2, 10, 5000, 2
        );
        ConnectionFactory factory = new ConnectionFactory(config);
        factory.closeQuietly(mockConnection);
        verify(mockConnection).close();
    }

    @Test
    void closeQuietly_nullConnection_doesNotThrow() {
        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://localhost:5432/test", "u", "p", 2, 10, 5000, 2
        );
        ConnectionFactory factory = new ConnectionFactory(config);
        assertDoesNotThrow(() -> factory.closeQuietly(null));
    }
}
