# Data Model: Custom JDBC Connection Pool

**Date**: 2026-06-04 | **Feature**: Custom JDBC Connection Pool

## ConnectionPoolConfig

An immutable configuration object that holds all pool settings.

| Field | Type | Default | Description | Validation |
|-------|------|---------|-------------|------------|
| `jdbcUrl` | String | — | PostgreSQL JDBC URL | Must be a valid JDBC URL format `jdbc:postgresql://host:port/db` |
| `username` | String | — | Database user name | Must not be null or empty |
| `password` | String | — | Database user password | Must not be null |
| `initialSize` | int | `2` | Number of connections created on pool init | Must be >= 1 AND <= maxSize |
| `maxSize` | int | `10` | Maximum number of physical connections | Must be >= 1 |
| `borrowTimeoutMillis` | long | `5000` | Max wait time for a connection (ms) | Must be > 0 |
| `validationTimeoutSeconds` | int | `2` | Timeout for Connection.isValid() | Must be > 0 |

**Validation Rule**: `initialSize <= maxSize` AND `maxSize >= 1` AND `borrowTimeoutMillis > 0` AND `validationTimeoutSeconds > 0`. Violation throws `ConnectionPoolException` at init (fail-fast, FR-016).

## Connection States

```
IDLE ──borrow()──> BORROWED ──close() (proxy)──> IDLE
                      │
                      │ pool.close()
                      ▼
                    CLOSED (physical close on return)

IDLE ──pool.close()──> CLOSED (physical)
```

- **IDLE**: Physical connection is in the `ArrayBlockingQueue`, ready for borrowing
- **BORROWED**: Logical connection (Proxy) handed to application code. Physical connection is not in the queue
- **CLOSED**: Physical connection is closed. Either by pool.close() (idle) or by proxy on return after shutdown

## Pool Thread-Safety State

| Field | Type | Purpose |
|-------|------|---------|
| `idleConnections` | `ArrayBlockingQueue<Connection>` | Thread-safe queue of idle physical connections |
| `totalConnections` | `AtomicInteger` | Current total number of physical connections |
| `closed` | `AtomicBoolean` | Pool shutdown flag (guards all operations) |

## ConnectionFactory Contract

Creates and validates physical `java.sql.Connection` objects.

| Method | Returns | Description |
|--------|---------|-------------|
| `createConnection()` | `Connection` | Creates a new physical PostgreSQL connection via DriverManager |
| `isValid(Connection)` | `boolean` | Validates connection via `Connection.isValid(validationTimeoutSeconds)` |
| `closeQuietly(Connection)` | `void` | Closes a physical connection silently (no exception thrown) |
