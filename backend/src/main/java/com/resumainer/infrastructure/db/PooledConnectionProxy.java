package com.resumainer.infrastructure.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

public class PooledConnectionProxy implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(PooledConnectionProxy.class);
    private final Connection physicalConnection;
    private final Runnable returnCallback;
    private volatile boolean closed;

    private PooledConnectionProxy(Connection physicalConnection, Runnable returnCallback) {
        this.physicalConnection = physicalConnection;
        this.returnCallback = returnCallback;
        this.closed = false;
    }

    public static Connection create(Connection physicalConnection, Runnable returnCallback) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{ Connection.class },
                new PooledConnectionProxy(physicalConnection, returnCallback)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("close")) {
            handleClose();
            return null;
        }
        try {
            return method.invoke(physicalConnection, args);
        } catch (Exception e) {
            throw new SQLException("Failed to invoke " + method.getName() + " on connection", e);
        }
    }

    private void handleClose() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            // Rollback only if a transaction is active (autoCommit was disabled).
            // PostgreSQL throws: Cannot rollback when autoCommit is enabled.
            if (!physicalConnection.getAutoCommit()) {
                physicalConnection.rollback();
            }
        } catch (SQLException e) {
            log.warn("Failed to rollback connection on return", e);
        }
        try {
            physicalConnection.setAutoCommit(true);
        } catch (SQLException e) {
            log.warn("Failed to reset autoCommit on return", e);
        }
        try {
            physicalConnection.setReadOnly(false);
        } catch (SQLException e) {
            log.warn("Failed to reset readOnly on return", e);
        }
        try {
            physicalConnection.clearWarnings();
        } catch (SQLException e) {
            log.warn("Failed to clear warnings on return", e);
        }
        returnCallback.run();
    }
}
