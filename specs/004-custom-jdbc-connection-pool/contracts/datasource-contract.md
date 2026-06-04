# DataSource Interface Contract

**Date**: 2026-06-04 | **Feature**: Custom JDBC Connection Pool

## Implemented Methods

| Method | Behavior |
|--------|----------|
| `Connection getConnection()` | Borrows a connection from the pool. Waits up to `borrowTimeoutMillis` if pool is exhausted. Returns a `PooledConnectionProxy` wrapping the physical connection. Throws `ConnectionPoolException` on timeout or if pool is closed. |
| `Connection getConnection(String username, String password)` | **NOT SUPPORTED**. Throws `SQLFeatureNotSupportedException` with message: "Custom per-user database credentials are not supported by this connection pool." |
| `PrintWriter getLogWriter()` | Returns the log writer (initially null). |
| `void setLogWriter(PrintWriter)` | Sets the log writer. |
| `int getLoginTimeout()` | Returns the login timeout (seconds). |
| `void setLoginTimeout(int seconds)` | Sets the login timeout (seconds). |
| `Logger getParentLogger()` | Returns the parent logger. |

## Not Implemented / Delegated

| Method | Behavior |
|--------|----------|
| `unwrap(Class<T> iface)` | Throws `SQLException` with message `"Not a wrapper for {iface.getName()}"` if the class is not wrappable (per JDBC 4.0 `Wrapper` specification). |
| `isWrapperFor(Class<?> iface)` | Returns `true` only for `DataSource`, `SimpleConnectionPool`, or implemented interfaces. |

## PooledConnectionProxy Contract

The proxy wrapper around a physical `Connection`.

| Method | Behavior |
|--------|----------|
| `close()` | **INTERCEPTED**. Resets physical connection (rollback, autoCommit, readOnly, clearWarnings). If pool is not closed → returns physical connection to idle queue. If pool is closed → closes physical connection. |
| All other Connection methods | Delegated directly to the physical connection via `method.invoke(physicalConnection, args)`. |
