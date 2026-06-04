package com.resumainer.infrastructure.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A minimal, thread-safe custom JDBC connection pool implementing {@link DataSource}.
 * <p>
 * This is an educational implementation for the ResumAIner Capstone project.
 * It uses {@link ArrayBlockingQueue} for idle connection management,
 * {@link AtomicInteger} for connection counting, and
 * {@link AtomicBoolean} for shutdown state.
 * <p>
 * Lifecycle:
 * <ol>
 *   <li><b>Init</b>: {@link #init()} creates {@code initialSize} physical connections</li>
 *   <li><b>Borrow</b>: {@link #getConnection()} takes from idle queue or creates new (up to maxSize)</li>
 *   <li><b>Return</b>: proxy's {@code close()} resets and returns connection to idle queue via {@link #returnConnection(Connection)}</li>
 *   <li><b>Shutdown</b>: {@link #close()} closes all idle connections gracefully</li>
 * </ol>
 * <p>
 * After Capstone acceptance, this pool can be replaced with HikariCP by changing
 * the DataSourceConfig bean and pom.xml — no other code changes needed
 * because all application code depends on {@code DataSource}, not {@code SimpleConnectionPool}.
 */
public class SimpleConnectionPool implements DataSource {

    private static final Logger log = LoggerFactory.getLogger(SimpleConnectionPool.class);
    private final ConnectionPoolConfig config;
    private final ConnectionFactory factory;
    private final BlockingQueue<Connection> idleConnections;
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private volatile PrintWriter logWriter;

    public SimpleConnectionPool(ConnectionPoolConfig config, ConnectionFactory factory) {
        this.config = config;
        this.factory = factory;
        this.idleConnections = new ArrayBlockingQueue<>(config.getMaxSize());
    }

    /**
     * Initialize the pool — creates initialSize physical connections.
     */
    public void init() {
        log.info("Initializing connection pool: initialSize={}, maxSize={}",
                config.getInitialSize(), config.getMaxSize());
        for (int i = 0; i < config.getInitialSize(); i++) {
            Connection connection = factory.createConnection();
            idleConnections.offer(connection);
            totalConnections.incrementAndGet();
        }
        log.info("Connection pool initialized: {} connections created", config.getInitialSize());
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (closed.get()) {
            throw new ConnectionPoolException("Connection pool is already closed");
        }

        Connection physical = borrowConnection();
        return PooledConnectionProxy.create(physical, () -> returnConnection(physical));
    }

    private Connection borrowConnection() {
        long deadline = System.currentTimeMillis() + config.getBorrowTimeoutMillis();

        while (System.currentTimeMillis() < deadline) {
            // Try idle queue first
            Connection connection = idleConnections.poll();
            if (connection != null) {
                if (factory.isValid(connection)) {
                    log.debug("Borrowed connection from idle queue");
                    return connection;
                } else {
                    log.warn("Idle connection is invalid, discarding");
                    factory.closeQuietly(connection);
                    totalConnections.decrementAndGet();
                    continue;
                }
            }

            // Try creating new connection if under max
            if (totalConnections.get() < config.getMaxSize()) {
                try {
                    Connection newConnection = factory.createConnection();
                    totalConnections.incrementAndGet();
                    log.debug("Created new connection (total={})", totalConnections.get());
                    return newConnection;
                } catch (ConnectionPoolException e) {
                    log.warn("Failed to create new connection, will retry", e);
                    continue;
                }
            }

            // Pool exhausted — wait for a connection to be returned
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) break;
            try {
                connection = idleConnections.poll(remaining, TimeUnit.MILLISECONDS);
                if (connection != null) {
                    if (factory.isValid(connection)) {
                        log.debug("Borrowed connection after waiting");
                        return connection;
                    } else {
                        log.warn("Returned connection is invalid, discarding");
                        factory.closeQuietly(connection);
                        totalConnections.decrementAndGet();
                        continue;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new ConnectionPoolException(
                "Could not acquire database connection within " + config.getBorrowTimeoutMillis() + " ms"
        );
    }

    /**
     * Return a physical connection to the pool.
     * Called by PooledConnectionProxy when close() is invoked.
     */
    public void returnConnection(Connection connection) {
        if (closed.get()) {
            log.debug("Pool is closed, physically closing returned connection");
            factory.closeQuietly(connection);
            totalConnections.decrementAndGet();
            return;
        }
        boolean offered = idleConnections.offer(connection);
        if (!offered) {
            log.warn("Idle queue is full, closing returned connection");
            factory.closeQuietly(connection);
            totalConnections.decrementAndGet();
        }
    }

    /**
     * Graceful shutdown — closes all idle connections and marks pool as closed.
     * Borrowed connections are physically closed when returned.
     * Idempotent and thread-safe.
     */
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return; // idempotent
        }
        log.info("Shutting down connection pool");
        Connection connection;
        while ((connection = idleConnections.poll()) != null) {
            factory.closeQuietly(connection);
            totalConnections.decrementAndGet();
        }
        log.info("Connection pool closed");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException(
                "Custom per-user database credentials are not supported by this connection pool"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        throw new SQLException("Not a wrapper for " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    @Override
    public PrintWriter getLogWriter() { return logWriter; }

    @Override
    public void setLogWriter(PrintWriter logWriter) { this.logWriter = logWriter; }

    @Override
    public void setLoginTimeout(int seconds) { /* no-op for MVP */ }

    @Override
    public int getLoginTimeout() { return 0; }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("java.util.logging is not used");
    }
}
