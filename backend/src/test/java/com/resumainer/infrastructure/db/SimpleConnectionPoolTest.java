package com.resumainer.infrastructure.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleConnectionPoolTest {

    private ConnectionPoolConfig config;
    @Mock
    private ConnectionFactory mockFactory;
    @Mock
    private Connection mockConnection;
    @Mock
    private Connection mockConnection2;

    @BeforeEach
    void setUp() {
        config = new ConnectionPoolConfig(
                "jdbc:pg://localhost/db", "u", "p", 2, 5, 5000, 2
        );
    }

    @Test
    void init_createsInitialConnections() {
        when(mockFactory.createConnection()).thenReturn(mockConnection);

        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        pool.init();

        verify(mockFactory, times(2)).createConnection();
    }

    @Test
    void getConnection_returnsValidConnection() throws Exception {
        when(mockFactory.createConnection()).thenReturn(mockConnection);
        when(mockFactory.isValid(mockConnection)).thenReturn(true);

        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        pool.init();

        Connection conn = pool.getConnection();
        assertNotNull(conn);
        conn.close(); // return to pool — proxy intercepts
    }

    @Test
    void close_pool_closesIdleConnections() {
        when(mockFactory.createConnection()).thenReturn(mockConnection);

        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        pool.init();
        pool.close();

        verify(mockFactory, times(2)).closeQuietly(mockConnection);
    }

    @Test
    void getConnection_afterClose_throwsException() {
        when(mockFactory.createConnection()).thenReturn(mockConnection);

        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        pool.init();
        pool.close();

        assertThrows(ConnectionPoolException.class, pool::getConnection);
    }

    @Test
    void close_isIdempotent() {
        when(mockFactory.createConnection()).thenReturn(mockConnection);

        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        pool.init();
        pool.close();
        assertDoesNotThrow(pool::close); // second close — no-op
    }

    @Test
    void getConnection_withCredentials_throwsNotSupported() {
        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        assertThrows(SQLException.class, () ->
            pool.getConnection("another_user", "another_pass")
        );
    }
}
