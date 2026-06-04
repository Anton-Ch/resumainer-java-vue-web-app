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
class PooledConnectionProxyTest {

    @Mock
    private Connection mockPhysicalConnection;

    @Test
    void close_intercepted_returnsConnectionToPool() throws SQLException {
        Connection proxy = PooledConnectionProxy.create(mockPhysicalConnection, () -> { });

        proxy.close();

        verify(mockPhysicalConnection, never()).close();
        verify(mockPhysicalConnection).rollback();
        verify(mockPhysicalConnection).setAutoCommit(true);
        verify(mockPhysicalConnection).setReadOnly(false);
        verify(mockPhysicalConnection).clearWarnings();
    }

    @Test
    void close_isIdempotent() throws SQLException {
        Connection proxy = PooledConnectionProxy.create(mockPhysicalConnection, () -> { });

        proxy.close();
        proxy.close();

        verify(mockPhysicalConnection, times(1)).rollback();
        verify(mockPhysicalConnection, never()).close();
    }

    @Test
    void nonCloseMethods_delegatedToPhysical() throws SQLException {
        when(mockPhysicalConnection.isClosed()).thenReturn(false);

        Connection proxy = PooledConnectionProxy.create(mockPhysicalConnection, () -> { });

        assertFalse(proxy.isClosed());
        verify(mockPhysicalConnection).isClosed();
    }
}
