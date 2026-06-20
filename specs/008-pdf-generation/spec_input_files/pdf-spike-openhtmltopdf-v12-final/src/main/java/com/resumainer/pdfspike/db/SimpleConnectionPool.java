package com.resumainer.pdfspike.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

public final class SimpleConnectionPool implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(SimpleConnectionPool.class);
    private final ConnectionFactory factory;
    private final int maxSize;
    private final Deque<Connection> idle = new ArrayDeque<>();
    private int total;
    private int borrowed;
    private boolean closed;

    public SimpleConnectionPool(ConnectionPoolConfig config) {
        this.factory = new ConnectionFactory(config);
        this.maxSize = config.maxSize();
    }

    public synchronized Connection getConnection() {
        if (closed) throw new ConnectionPoolException("Pool is closed");
        Connection physical = idle.pollFirst();
        if (physical == null) {
            if (total >= maxSize) throw new ConnectionPoolException("No available connections in spike pool");
            physical = factory.createPhysicalConnection();
            total++;
            log.debug("Borrowed new connection borrowed={} total={}", borrowed + 1, total);
        } else {
            log.debug("Borrowed idle connection borrowed={} idle={}", borrowed + 1, idle.size());
        }
        borrowed++;
        Connection target = physical;
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        returnConnection(target);
                        return null;
                    }
                    return method.invoke(target, args);
                });
    }

    private synchronized void returnConnection(Connection c) {
        borrowed--;
        idle.addLast(c);
        log.debug("Returned connection borrowed={} idle={}", borrowed, idle.size());
    }

    @Override
    public synchronized void close() {
        closed = true;
        while (!idle.isEmpty()) {
            try { idle.pollFirst().close(); } catch (SQLException ignored) { }
        }
    }
}
