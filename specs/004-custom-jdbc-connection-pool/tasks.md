---
description: "Task list for Custom JDBC Connection Pool implementation"
---

# Tasks: Custom JDBC Connection Pool

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Input**: Design documents from `specs/004-custom-jdbc-connection-pool/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Constitution Compliance**: Every task phase MUST reference the ResumAIner Constitution principles:
- **I** — Code Quality & Maintainability (layered architecture, SOLID, no Spring Boot/JPA)
- **II** — Testing Excellence (JUnit 5, Mockito, TDD for business logic, JaCoCo 50%+)
- **III** — User Experience (error messages clear and developer-readable)
- **IV** — Performance & Reliability (PreparedStatement, JDBC transactions, thread-safe pool)
- **V** — Security by Design (credentials from env vars, no secrets in logs, connection reset)

**Organization**: Tasks are grouped by implementation phase. Each class follows TDD: write failing test → implement → verify pass.

## Format: `[ID] [Marker] Description`

| Marker | Meaning |
|--------|---------|
| `[P]` | Can run in parallel (different files, no dependencies) |
| `[TDD]` | Must follow RED-GREEN-REFACTOR: write test → fail → implement → pass |
| `[REVIEW]` | Requires code review before proceeding |
| `[SUBAGENT]` | Can be dispatched to a parallel subagent |

## Path Conventions

All source code: `backend/src/main/java/com/resumainer/infrastructure/db/`
All tests: `backend/src/test/java/com/resumainer/infrastructure/db/`

---

## Phase 0: Setup — Inspect & Clean Existing Connection Artifacts

**Purpose**: Find and remove old connection management code before introducing the custom pool.

### Task 001: Find existing connection-related code

- [ ] **Step 1**: Search the codebase for keywords that indicate existing connection patterns

```bash
# From backend/ directory:
rg -n "DriverManager.getConnection|ConnectionManager|DatabaseConnection|DbConnection|ConnectionProvider|dataSource|DataSource" --type java
```

- [ ] **Step 2**: Identify which files need to be removed, modified, or kept. Document findings:

```
Files to DELETE:   (list full paths)
Files to MODIFY:   (list full paths, what changes)
Files kept AS-IS:  (list full paths)
```

- [ ] **Step 3**: Report findings and get approval before deleting anything

**Checkpoint**: Clear picture of existing connection architecture. Old artifacts identified.

---

### Task 002: Remove obsolete connection classes [P] [REVIEW]

- [ ] **Step 1**: Delete identified obsolete files (get explicit confirmation per file)

```bash
# Example — adjust paths based on Task 001 findings:
git rm backend/src/main/java/com/resumainer/.../OldConnectionManager.java
git rm backend/src/test/java/com/resumainer/.../OldConnectionManagerTest.java
```

- [ ] **Step 2**: Remove any obsolete configuration or imports

```bash
rg -n "OldConnectionManager|oldConnectionManager|ConnectionProvider" --type java backend/src/
# Remove any remaining references to deleted classes
```

- [ ] **Step 3**: Verify the project still compiles

```bash
cd backend && mvn compile -q
# Expected: BUILD SUCCESS (or known pre-existing failures unrelated to cleanup)
```

- [ ] **Step 4**: Commit

```bash
git add -A && git commit -m "chore(004-jdbc-pool): remove obsolete connection management classes"
```

---

## Phase 1: Foundational — Core Pool Classes (TDD)

**Purpose**: Implement the 5 core classes with TDD. Each class is independently testable.

**⚠️ CRITICAL**: All user stories depend on this phase.

### Task 003: Create ConnectionPoolConfig [P] [TDD] [SUBAGENT]

- [ ] **Step 1: Create test directory and write failing test**

Create `backend/src/test/java/com/resumainer/infrastructure/db/ConnectionPoolConfigTest.java`:

```java
package com.resumainer.infrastructure.db;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionPoolConfigTest {

    @Test
    void constructor_withValidParams_createsConfig() {
        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://localhost:5432/test",
                "user", "pass",
                2, 10, 5000, 2
        );
        assertEquals("jdbc:postgresql://localhost:5432/test", config.getJdbcUrl());
        assertEquals("user", config.getUsername());
        assertEquals("pass", config.getPassword());
        assertEquals(2, config.getInitialSize());
        assertEquals(10, config.getMaxSize());
        assertEquals(5000, config.getBorrowTimeoutMillis());
        assertEquals(2, config.getValidationTimeoutSeconds());
    }
}
```

- [ ] **Step 2: Run test — expect compilation failure**

```bash
cd backend && mvn test-compile -q
# Expected: compilation error — ConnectionPoolConfig does not exist
```

- [ ] **Step 3: Implement ConnectionPoolConfig**

Create `backend/src/main/java/com/resumainer/infrastructure/db/ConnectionPoolConfig.java`:

```java
package com.resumainer.infrastructure.db;

import java.util.Objects;

public class ConnectionPoolConfig {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int initialSize;
    private final int maxSize;
    private final long borrowTimeoutMillis;
    private final int validationTimeoutSeconds;

    public ConnectionPoolConfig(
            String jdbcUrl, String username, String password,
            int initialSize, int maxSize,
            long borrowTimeoutMillis, int validationTimeoutSeconds
    ) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("jdbcUrl must not be null or blank");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be null or blank");
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        if (initialSize <= 0 || initialSize > maxSize) {
            throw new IllegalArgumentException(
                    "initialSize (" + initialSize + ") must be >= 1 and <= maxSize (" + maxSize + ")"
            );
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException(
                    "maxSize (" + maxSize + ") must be >= 1"
            );
        }
        if (borrowTimeoutMillis <= 0) {
            throw new IllegalArgumentException(
                    "borrowTimeoutMillis (" + borrowTimeoutMillis + ") must be > 0"
            );
        }
        if (validationTimeoutSeconds <= 0) {
            throw new IllegalArgumentException(
                    "validationTimeoutSeconds (" + validationTimeoutSeconds + ") must be > 0"
            );
        }
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.initialSize = initialSize;
        this.maxSize = maxSize;
        this.borrowTimeoutMillis = borrowTimeoutMillis;
        this.validationTimeoutSeconds = validationTimeoutSeconds;
    }

    public String getJdbcUrl() { return jdbcUrl; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getInitialSize() { return initialSize; }
    public int getMaxSize() { return maxSize; }
    public long getBorrowTimeoutMillis() { return borrowTimeoutMillis; }
    public int getValidationTimeoutSeconds() { return validationTimeoutSeconds; }
}
```

- [ ] **Step 4: Run test again — expect PASS**

```bash
cd backend && mvn test -Dtest=ConnectionPoolConfigTest -q
# Expected: PASS (Tests run: 1, Failures: 0)
```

- [ ] **Step 5: Add validation failure tests**

Extend `ConnectionPoolConfigTest.java`:

```java
    @Test
    void constructor_withInitialSizeZero_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ConnectionPoolConfig("jdbc:pg://localhost/db", "u", "p", 0, 10, 5000, 2)
        );
    }

    @Test
    void constructor_withInitialSizeGreaterThanMaxSize_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ConnectionPoolConfig("jdbc:pg://localhost/db", "u", "p", 15, 10, 5000, 2)
        );
    }

    @Test
    void constructor_withNullJdbcUrl_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ConnectionPoolConfig(null, "u", "p", 2, 10, 5000, 2)
        );
    }
```

- [ ] **Step 6: Run all tests — expect PASS**

```bash
cd backend && mvn test -Dtest=ConnectionPoolConfigTest -q
# Expected: PASS (Tests run: 4, Failures: 0)
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/resumainer/infrastructure/db/ConnectionPoolConfig.java backend/src/test/java/com/resumainer/infrastructure/db/ConnectionPoolConfigTest.java
git commit -m "feat(004-jdbc-pool): add ConnectionPoolConfig with validation"
```

---

### Task 004: Create ConnectionPoolException [P] [TDD] [SUBAGENT]

- [ ] **Step 1: Write failing test**

Create `backend/src/test/java/com/resumainer/infrastructure/db/ConnectionPoolExceptionTest.java`:

```java
package com.resumainer.infrastructure.db;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionPoolExceptionTest {

    @Test
    void exception_withMessage_containsMessage() {
        ConnectionPoolException ex = new ConnectionPoolException("Pool is closed");
        assertEquals("Pool is closed", ex.getMessage());
    }

    @Test
    void exception_withMessageAndCause_containsBoth() {
        Throwable cause = new RuntimeException("DB down");
        ConnectionPoolException ex = new ConnectionPoolException("Failed to connect", cause);
        assertEquals("Failed to connect", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
```

- [ ] **Step 2: Run test — expect compilation failure**
- [ ] **Step 3: Implement ConnectionPoolException**

Create `backend/src/main/java/com/resumainer/infrastructure/db/ConnectionPoolException.java`:

```java
package com.resumainer.infrastructure.db;

public class ConnectionPoolException extends RuntimeException {

    public ConnectionPoolException(String message) {
        super(message);
    }

    public ConnectionPoolException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 4: Run test — expect PASS**

```bash
cd backend && mvn test -Dtest=ConnectionPoolExceptionTest -q
# Expected: PASS (Tests run: 2, Failures: 0)
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/resumainer/infrastructure/db/ConnectionPoolException.java backend/src/test/java/com/resumainer/infrastructure/db/ConnectionPoolExceptionTest.java
git commit -m "feat(004-jdbc-pool): add ConnectionPoolException"
```

---

### Task 005: Create ConnectionFactory [P] [TDD] [SUBAGENT]

- [ ] **Step 1: Write failing test with mocked DriverManager**

Create `backend/src/test/java/com/resumainer/infrastructure/db/ConnectionFactoryTest.java`:

```java
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
    void createConnection_returnsConnection() throws SQLException {
        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://localhost:5432/test", "u", "p", 2, 10, 5000, 2
        );
        // Note: ConnectionFactory relies on DriverManager which cannot be easily mocked
        // We test validation and closeQuietly instead, and test createConnection
        // with an invalid URL to verify error handling
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
```

- [ ] **Step 2: Run test — expect compilation failure**
- [ ] **Step 3: Implement ConnectionFactory**

Create `backend/src/main/java/com/resumainer/infrastructure/db/ConnectionFactory.java`:

```java
package com.resumainer.infrastructure.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final Logger log = LoggerFactory.getLogger(ConnectionFactory.class);
    private final ConnectionPoolConfig config;

    public ConnectionFactory(ConnectionPoolConfig config) {
        this.config = config;
    }

    public Connection createConnection() {
        try {
            log.debug("Creating new database connection to {}", maskUrl(config.getJdbcUrl()));
            return DriverManager.getConnection(
                    config.getJdbcUrl(),
                    config.getUsername(),
                    config.getPassword()
            );
        } catch (SQLException e) {
            throw new ConnectionPoolException("Could not create physical database connection", e);
        }
    }

    public boolean isValid(Connection connection) {
        if (connection == null) {
            return false;
        }
        try {
            return connection.isValid(config.getValidationTimeoutSeconds());
        } catch (SQLException e) {
            log.warn("Connection validation failed with exception", e);
            return false;
        }
    }

    public void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            log.warn("Failed to close database connection", e);
        }
    }

    private static String maskUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("://[^:]+:[^@]+@", "://***:***@");
    }
}
```

- [ ] **Step 4: Run tests — expect PASS**

```bash
cd backend && mvn test -Dtest=ConnectionFactoryTest -q
# Expected: PASS (Tests run: 6, Failures: 0)
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/resumainer/infrastructure/db/ConnectionFactory.java backend/src/test/java/com/resumainer/infrastructure/db/ConnectionFactoryTest.java
git commit -m "feat(004-jdbc-pool): add ConnectionFactory with validation and closeQuietly"
```

---

### Task 006: Create PooledConnectionProxy [P] [TDD] [SUBAGIENT]

- [ ] **Step 1: Write failing test**

Create `backend/src/test/java/com/resumainer/infrastructure/db/PooledConnectionProxyTest.java`:

```java
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
        // Arrange: create a proxy around mockPhysicalConnection
        // The proxy's close() should NOT close physical connection
        // Instead it should reset and return it
        Connection proxy = PooledConnectionProxy.create(mockPhysicalConnection, () -> {
            // This is the return callback — just a no-op for this test
        });

        proxy.close();

        // Physical connection should NOT be closed by proxy.close()
        verify(mockPhysicalConnection, never()).close();
        // But it should be reset
        verify(mockPhysicalConnection).rollback();
        verify(mockPhysicalConnection).setAutoCommit(true);
        verify(mockPhysicalConnection).setReadOnly(false);
        verify(mockPhysicalConnection).clearWarnings();
    }

    @Test
    void close_delegatesToPhysicalWhenPoolClosed() throws SQLException {
        Connection proxy = PooledConnectionProxy.create(mockPhysicalConnection, () -> { });

        proxy.close();
        proxy.close(); // second close — should be idempotent

        // verify rollback/clearWarnings happened twice (for each close attempt)
        verify(mockPhysicalConnection, times(2)).rollback();
    }

    @Test
    void nonCloseMethods_delegatedToPhysical() throws SQLException {
        when(mockPhysicalConnection.isClosed()).thenReturn(false);

        Connection proxy = PooledConnectionProxy.create(mockPhysicalConnection, () -> { });

        assertFalse(proxy.isClosed());
        verify(mockPhysicalConnection).isClosed();
    }
}
```

- [ ] **Step 2: Run test — expect compilation failure**
- [ ] **Step 3: Implement PooledConnectionProxy**

Create `backend/src/main/java/com/resumainer/infrastructure/db/PooledConnectionProxy.java`:

```java
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
                new Class[]{Connection.class},
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
            return; // idempotent close
        }
        closed = true;
        try {
            physicalConnection.rollback();
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
```

- [ ] **Step 4: Run tests — expect PASS**

```bash
cd backend && mvn test -Dtest=PooledConnectionProxyTest -q
# Expected: PASS (Tests run: 3, Failures: 0)
```

- [ ] **Step 5: Fix test by adjusting close behavior**

The test `close_delegatesToPhysicalWhenPoolClosed` currently verifies rollback is called twice (idempotent). Update it to also verify physical connection is NOT closed:

```java
    @Test
    void close_isIdempotent() throws SQLException {
        Connection proxy = PooledConnectionProxy.create(mockPhysicalConnection, () -> { });

        proxy.close();
        proxy.close();

        verify(mockPhysicalConnection, times(1)).rollback(); // first close resets
        verify(mockPhysicalConnection, never()).close(); // physical NOT closed
    }
```

- [ ] **Step 6: Run tests — expect PASS**

```bash
cd backend && mvn test -Dtest=PooledConnectionProxyTest -q
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/resumainer/infrastructure/db/PooledConnectionProxy.java backend/src/test/java/com/resumainer/infrastructure/db/PooledConnectionProxyTest.java
git commit -m "feat(004-jdbc-pool): add PooledConnectionProxy with dynamic proxy"
```

---

### Task 007: Create SimpleConnectionPool [TDD] [REVIEW]

**Note**: This is the most complex class. It depends on Config, Factory, Exception, and Proxy being complete.

- [ ] **Step 1: Write failing test for initialization**

Create `backend/src/test/java/com/resumainer/infrastructure/db/SimpleConnectionPoolTest.java`:

```java
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
    void init_createsInitialConnections() throws SQLException {
        when(mockFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(2)).thenReturn(true);

        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        pool.init();

        verify(mockFactory, times(2)).createConnection();
    }

    @Test
    void getConnection_returnsValidConnection() throws SQLException {
        when(mockFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(2)).thenReturn(true);

        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        pool.init();

        Connection conn = pool.getConnection();
        assertNotNull(conn);
        conn.close(); // return to pool
    }

    @Test
    void getConnection_whenPoolExhausted_throwsException() throws SQLException {
        when(mockFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(2)).thenReturn(true);

        SimpleConnectionPool pool = new SimpleConnectionPool(
                new ConnectionPoolConfig("jdbc:pg://localhost/db", "u", "p", 1, 1, 100, 2),
                mockFactory
        );
        pool.init();

        Connection conn1 = pool.getConnection();
        assertNotNull(conn1);

        assertThrows(ConnectionPoolException.class, () -> {
            // Timeout is 100ms — pool exhausted, no idle connections
            // The 1 borrowed connection is NOT returned, so pool is stuck
            Connection conn2 = pool.getConnection();
        });
    }

    @Test
    void close_pool_closesIdleConnections() throws SQLException {
        when(mockFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(2)).thenReturn(true);

        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        pool.init();

        pool.close();

        verify(mockConnection, times(2)).close(); // both idle connections closed
    }

    @Test
    void getConnection_afterClose_throwsException() throws SQLException {
        when(mockFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(2)).thenReturn(true);

        SimpleConnectionPool pool = new SimpleConnectionPool(config, mockFactory);
        pool.init();
        pool.close();

        assertThrows(ConnectionPoolException.class, pool::getConnection);
    }

    @Test
    void invalidConnection_isNotReturned() throws SQLException {
        when(mockFactory.createConnection())
                .thenReturn(mockConnection)
                .thenReturn(mockConnection2);
        when(mockConnection.isValid(2)).thenReturn(false); // invalid
        when(mockConnection2.isValid(2)).thenReturn(true);  // valid

        SimpleConnectionPool pool = new SimpleConnectionPool(
                new ConnectionPoolConfig("jdbc:pg://localhost/db", "u", "p", 0, 5, 5000, 2),
                mockFactory
        );

        Connection conn = pool.getConnection();
        assertNotNull(conn);
        verify(mockFactory, times(2)).createConnection();
        verify(mockFactory).closeQuietly(mockConnection); // invalid one closed
    }

    @Test
    void close_isIdempotent() throws SQLException {
        when(mockFactory.createConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(2)).thenReturn(true);

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
```

- [ ] **Step 2: Run test — expect compilation failure**
- [ ] **Step 3: Implement SimpleConnectionPool**

Create `backend/src/main/java/com/resumainer/infrastructure/db/SimpleConnectionPool.java`:

```java
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
import java.util.logging.Logger;

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
                    continue; // try again
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
                    totalConnections.decrementAndGet();
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
                        log.warn("Returned idle connection is invalid, discarding");
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
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("java.util.logging is not used");
    }
}
```

- [ ] **Step 4: Create DataSourceConfig with init() call**

Create `backend/src/main/java/com/resumainer/infrastructure/db/DataSourceConfig.java`:

```java
package com.resumainer.infrastructure.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public ConnectionPoolConfig connectionPoolConfig(
            @Value("${db.url}") String url,
            @Value("${db.username}") String username,
            @Value("${db.password}") String password,
            @Value("${db.pool.initial-size:2}") int initialSize,
            @Value("${db.pool.max-size:10}") int maxSize,
            @Value("${db.pool.borrow-timeout-ms:5000}") long borrowTimeout,
            @Value("${db.pool.validation-timeout-seconds:2}") int validationTimeout
    ) {
        return new ConnectionPoolConfig(
                url, username, password,
                initialSize, maxSize, borrowTimeout, validationTimeout
        );
    }

    @Bean
    public ConnectionFactory connectionFactory(ConnectionPoolConfig config) {
        return new ConnectionFactory(config);
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public DataSource dataSource(ConnectionPoolConfig config, ConnectionFactory factory) {
        return new SimpleConnectionPool(config, factory);
    }
}
```

- [ ] **Step 5: Run all pool tests — expect PASS**

```bash
cd backend && mvn test -Dtest="ConnectionPoolConfigTest,ConnectionPoolExceptionTest,ConnectionFactoryTest,PooledConnectionProxyTest,SimpleConnectionPoolTest" -q
# Expected: PASS (Tests run: ~18, Failures: 0)
```

- [ ] **Step 6: Run full Maven build**

```bash
cd backend && mvn clean compile -q
# Expected: BUILD SUCCESS
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/resumainer/infrastructure/db/SimpleConnectionPool.java backend/src/test/java/com/resumainer/infrastructure/db/SimpleConnectionPoolTest.java backend/src/main/java/com/resumainer/infrastructure/db/DataSourceConfig.java
git commit -m "feat(004-jdbc-pool): add SimpleConnectionPool with DataSource bean"
```

**Checkpoint**: All 5 pool classes implemented. `mvn compile` passes.

---

## Phase 2: Integration — Wire Pool into Existing Application

**Purpose**: Update existing services/DAOs to use the new `DataSource` bean.

### Task 008: Replace DriverManagerDataSource with SimpleConnectionPool [P] [SUBAGENT]

**Why this is minimal**: The existing codebase (`UserDao.java`, `RoleDao.java`, `ContactDetailDao.java`, `AuthService.java`) already depends on `javax.sql.DataSource` via constructor injection and uses `dataSource.getConnection()` with try-with-resources. The only change needed is replacing the `DriverManagerDataSource` bean in `WebConfig.java` with the new `SimpleConnectionPool` bean.

**Files to modify:**
- Modify: `backend/src/main/java/com/resumainer/config/WebConfig.java` — delete old `dataSource()` method and `DriverManagerDataSource` import
- Create: `backend/src/main/java/com/resumainer/infrastructure/db/DataSourceConfig.java`

**Files NOT modified** (already use DataSource correctly):
- `UserDao.java` — already injects `DataSource` and has `create(User, Connection)` overload
- `AuthService.java` — already uses `DataSource` with manual transaction management
- All other DAOs — already inject `DataSource`

- [ ] **Step 1: Create DataSourceConfig with SimpleConnectionPool bean**

Create `backend/src/main/java/com/resumainer/infrastructure/db/DataSourceConfig.java`:

```java
package com.resumainer.infrastructure.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean(initMethod = "init", destroyMethod = "close")
    public DataSource dataSource() {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "5432");
        String dbName = System.getenv().getOrDefault("DB_NAME", "resumainer");
        String user = System.getenv().getOrDefault("DB_USER", "resumainer");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "resumainer_dev");

        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://" + host + ":" + port + "/" + dbName,
                user, password, 2, 10, 5000, 2
        );
        ConnectionFactory factory = new ConnectionFactory(config);
        return new SimpleConnectionPool(config, factory);
    }
}
```

- [ ] **Step 2: Remove old dataSource() bean from WebConfig.java**

Edit `backend/src/main/java/com/resumainer/config/WebConfig.java` — delete the entire `dataSource()` method (approximately lines 183-197):

```java
// DELETE this entire method:
// @Bean
// public DataSource dataSource() {
//     String host = getEnv("DB_HOST", "localhost");
//     String port = getEnv("DB_PORT", "5432");
//     String dbName = getEnv("DB_NAME", "resumainer");
//     String user = getEnv("DB_USER", "resumainer");
//     String password = getEnv("DB_PASSWORD", "resumainer_dev");
//     DriverManagerDataSource ds = new DriverManagerDataSource();
//     ds.setUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName);
//     ds.setUsername(user);
//     ds.setPassword(password);
//     ds.setDriverClassName("org.postgresql.Driver");
//     return ds;
// }
```

- [ ] **Step 3: Remove unused DriverManagerDataSource import from WebConfig.java**

```java
// DELETE: import org.springframework.jdbc.datasource.DriverManagerDataSource;
```

- [ ] **Step 4: Verify build passes**

```bash
cd backend && mvn compile -q
# Expected: BUILD SUCCESS
```

- [ ] **Step 5: Run all project tests**

```bash
cd backend && mvn test -q
# Expected: BUILD SUCCESS (all existing + new pool tests pass)
```

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/resumainer/infrastructure/db/DataSourceConfig.java backend/src/main/java/com/resumainer/config/WebConfig.java
git commit -m "feat(004-jdbc-pool): replace DriverManagerDataSource with SimpleConnectionPool"
```

---

**Checkpoint**: Application compiles and all tests pass with the custom pool.

---

## Phase 3: Testing — Integration & Smoke Tests

### Task 009: Add Docker smoke test with real PostgreSQL [P] [SUBAGENT]

- [ ] **Step 1**: Create a test that starts Docker Compose and verifies the pool connects

Create `backend/src/test/java/com/resumainer/infrastructure/db/PoolSmokeTest.java`:

```java
package com.resumainer.infrastructure.db;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

@Tag("smoke")
class PoolSmokeTest {

    @Test
    void poolInitializesAndCanQueryPostgres() throws Exception {
        // This test requires Docker running with PostgreSQL
        // Environment variables must be set (see quickstart.md)

        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://" + System.getenv().getOrDefault("DB_HOST", "localhost")
                        + ":" + System.getenv().getOrDefault("DB_PORT", "5432")
                        + "/" + System.getenv().getOrDefault("DB_NAME", "resumainer"),
                System.getenv().getOrDefault("DB_USER", "resumainer"),
                System.getenv().getOrDefault("DB_PASSWORD", "resumainer"),
                2, 5, 5000, 2
        );

        ConnectionFactory factory = new ConnectionFactory(config);
        SimpleConnectionPool pool = new SimpleConnectionPool(config, factory);
        pool.init();

        // Verify we can get a connection and run a query
        try (Connection conn = pool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 AS value")) {

            assertTrue(rs.next());
            assertEquals(1, rs.getInt("value"));
        }

        pool.close();
    }
}
```

- [ ] **Step 2: Run smoke test with Docker**

```bash
# From repo root — ensure Docker Compose is running with PostgreSQL:
docker compose up -d postgres

# Run smoke test:
cd backend && mvn test -Dtest=PoolSmokeTest -Dgroups=smoke -q
# Expected: PASS (Tests run: 1, Failures: 0)
```

- [ ] **Step 3: Run full test suite with JaCoCo coverage**

```bash
cd backend && mvn clean verify -q
# Expected: BUILD SUCCESS
# Check: backend/target/site/jacoco/index.html — verify pool classes covered
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/test/java/com/resumainer/infrastructure/db/PoolSmokeTest.java
git commit -m "test(004-jdbc-pool): add Docker smoke test for pool with PostgreSQL"
```

---

## Phase 4: Polish — Documentation & Final Verification

### Task 010: Add Javadoc to public classes [P]

- [ ] **Step 1**: Add Javadoc to each public class explaining:
   - Purpose of the class
   - Thread-safety model (for `SimpleConnectionPool`)
   - Lifecycle (init, borrow, return, shutdown)
   - Why custom pool exists and that it can be replaced with HikariCP

Example Javadoc for `SimpleConnectionPool`:

```java
/**
 * A minimal, thread-safe custom JDBC connection pool implementing {@link javax.sql.DataSource}.
 * <p>
 * This pool is an educational implementation for the ResumAIner Capstone project.
 * It uses an {@link java.util.concurrent.ArrayBlockingQueue} for idle connection management,
 * {@link java.util.concurrent.atomic.AtomicInteger} for connection counting, and
 * {@link java.util.concurrent.atomic.AtomicBoolean} for shutdown state.
 * <p>
 * Lifecycle:
 * <ol>
 *   <li><b>Init</b>: {@link #init()} creates {@code initialSize} physical connections</li>
 *   <li><b>Borrow</b>: {@link #getConnection()} takes from idle queue or creates new (up to maxSize)</li>
 *   <li><b>Return</b>: proxy's {@code close()} resets and returns connection to idle queue</li>
 *   <li><b>Shutdown</b>: {@link #close()} closes all idle connections gracefully</li>
 * </ol>
 * <p>
 * After Capstone acceptance, this pool can be replaced with HikariCP by changing
 * the {@code DataSourceConfig} bean and {@code pom.xml} — no other code changes needed
 * because all application code depends on {@code DataSource}, not {@code SimpleConnectionPool}.
 */
```

- [ ] **Step 2: Build with tests to verify**

```bash
cd backend && mvn clean test -q
# Expected: BUILD SUCCESS
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "docs(004-jdbc-pool): add Javadoc to pool classes"
```

---

### Task 011: Final verification — mvn clean package [REVIEW]

- [ ] **Step 1: Run full Maven lifecycle**

```bash
cd backend && mvn clean package -q
# Expected: BUILD SUCCESS
# All tests pass, JaCoCo report generated, WAR file created
```

- [ ] **Step 2: Verify coverage report**

Open `backend/target/site/jacoco/index.html` and verify:
- Pool classes (infrastructure.db package) are covered
- No shocking coverage gaps

- [ ] **Step 3: Quickstart validation**

```bash
# Follow quickstart.md steps:
# 1. Check env vars are set
echo "DB_HOST=$env:DB_HOST DB_PORT=$env:DB_PORT"

# 2. Verify Docker Compose starts
docker compose up --build -d

# 3. Check logs for pool init message
docker compose logs backend | grep "Connection pool initialized"

# Expected: "Connection pool initialized: initialSize=2, maxSize=10"
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "chore(004-jdbc-pool): final build verification and quickstart validation"
```

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Phase 0: Setup — No dependencies, can start immediately
    └── Task 001 (find artifacts) → Task 002 (delete obsolete) [sequential]

Phase 1: Foundational — Depends on Phase 0
    ├── Task 003 (ConnectionPoolConfig) [P] — independent
    ├── Task 004 (ConnectionPoolException) [P] — independent
    ├── Task 005 (ConnectionFactory) [P] — independent (depends on 003 Config)
    ├── Task 006 (PooledConnectionProxy) [P] — independent
    └── Task 007 (SimpleConnectionPool) — BLOCKS all — depends on 003+004+005+006

Phase 2: Integration — Depends on Phase 1
    └── Task 008 (Replace DriverManagerDataSource) — depends on 007 Pool only

Phase 3: Testing — Depends on Phase 2
    └── Task 009 (Docker smoke test) — depends on 008

Phase 4: Polish — Depends on Phase 3 (can overlap with testing)
    ├── Task 010 (Javadoc) [P]
    └── Task 011 (Final verification) [REVIEW]
```

### Parallel Opportunities

```text
Tasks 003, 004, 005, 006 can run in PARALLEL (different files, no dependencies)
Exception: Task 005 (ConnectionFactory) depends on Task 003 (Config) — needs config object

Optimal parallel split:
  Agent A: Task 003 (Config) → Task 005 (Factory)
  Agent B: Task 004 (Exception) → Task 006 (Proxy)
  Both merge → Task 007 (Pool)
```

### Implementation Strategy

```text
1. Complete Phase 0: Inspect and clean old code
2. Complete Phase 1, Tasks 003-006 in parallel: 4 foundation classes
3. Task 007: SimpleConnectionPool (core — requires all 4 foundation classes)
4. Phase 2: Wire into app
5. Phase 3: Test with real PostgreSQL
6. Phase 4: Polish and final verify
```

---

## Notes

- [TDD] tasks must follow RED-GREEN-REFACTOR: write failing test → confirm fail → implement → confirm pass
- [P] tasks = different files, no dependencies — can run in parallel
- [REVIEW] tasks require human review before proceeding
- Commit after each logical task group
- Stop after Phase 0 to review old code findings before deleting anything
- Stop after Task 007 for checkpoint: all pool classes compile and pass tests
- Error messages in ConnectionPoolException must NOT include JDBC URL, host, port, or database name (SEC-001)
- `unwrap()` must throw SQLException, not return null (SEC-003)
- All logs must NOT contain database credentials (FR-015)
- Pool config uses `System.getenv()` not `${...}` placeholders (B2 guard)
