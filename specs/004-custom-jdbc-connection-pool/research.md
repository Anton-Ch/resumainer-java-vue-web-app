# Research: Custom JDBC Connection Pool

**Date**: 2026-06-04 | **Source**: Context7 documentation + project memory

## 1. Java Dynamic Proxy for PooledConnectionProxy

**Decision**: Use `java.lang.reflect.Proxy.newProxyInstance()` with `InvocationHandler` to create a logical Connection wrapper.

**Rationale**:
- `java.lang.reflect.Proxy` creates runtime implementations of `java.sql.Connection` interface without static wrappers
- `InvocationHandler.invoke(Object proxy, Method method, Object[] args)` intercepts `close()` to return connection to pool
- All other methods delegate to the underlying physical connection via `method.invoke(physicalConnection, args)`

**Implementation pattern**:
```java
Connection proxy = (Connection) Proxy.newProxyInstance(
    Connection.class.getClassLoader(),
    new Class[]{ Connection.class },
    (proxyConn, method, args) -> {
        if (method.getName().equals("close")) {
            // return physical connection to pool instead of closing
            pool.returnConnection(physicalConnection);
            return null;
        }
        return method.invoke(physicalConnection, args);
    }
);
```

**Alternatives considered**:
- Static wrapper class: Rejected — requires implementing all ~20 Connection methods manually
- CGLib/Javassist: Rejected — unnecessary external dependency when JDK Proxy suffices

**Source**: Standard Java Reflection API (`java.lang.reflect.Proxy`, `java.lang.reflect.InvocationHandler`)

---

## 2. Spring @Bean destroyMethod for DataSource

**Decision**: Use `@Bean(destroyMethod = "close")` on the DataSource configuration method.

**Rationale** (verified via Spring Framework 6.2 docs):
- Spring auto-detects `close()` and `shutdown()` methods on `@Bean` methods
- With `destroyMethod = "close"`, Spring calls `SimpleConnectionPool.close()` during context shutdown
- The `SimpleConnectionPool.close()` method is idempotent and thread-safe (guarded by `AtomicBoolean`)
- This ensures all idle connections are closed when Tomcat shuts down

**If auto-detection needs disabling**:
```java
@Bean(destroyMethod = "")  // disables auto-detection
```

**Source**: [Spring Framework Reference — `@Bean` destroyMethod](https://docs.spring.io/spring-framework/reference/6.2/core/beans/java/bean-annotation.html)

---

## 3. Connection.isValid() with PostgreSQL JDBC Driver

**Decision**: Use `Connection.isValid(int timeout)` for lazy validation at borrow time.

**Rationale**:
- PostgreSQL JDBC driver (`org.postgresql:postgresql`) supports `Connection.isValid(timeout)` since version 9.4
- Returns `true` if the connection is not closed and responds within the timeout
- No custom `SELECT 1` or validation query needed
- Validation timeout of 2 seconds (as per default config) is sufficient

**Behavior**:
- If connection is closed → returns `false` immediately
- If database is unreachable → waits up to `timeout` seconds, returns `false`
- If database responds → returns `true`
- Throws `SQLException` if driver does not support validation (pgJDBC does support it)

**Source**: [PostgreSQL JDBC Driver Documentation](https://jdbc.postgresql.org/documentation/use/)

---

## 4. ArrayBlockingQueue for Connection Pool

**Decision**: Use `ArrayBlockingQueue<Connection>` for idle connection management.

**Rationale**:
- `ArrayBlockingQueue` is a bounded, thread-safe FIFO queue
- `poll(long timeout, TimeUnit unit)` — waits up to timeout for a connection
- `offer(Connection, long timeout, TimeUnit unit)` — returns connection with timeout
- Both methods handle thread coordination without explicit `wait()`/`notify()`
- Bounded by `maxSize` — prevents unbounded memory growth

**Key patterns**:
- **Borrow**: `idleConnections.poll(borrowTimeoutMillis, TimeUnit.MILLISECONDS)` → returns null on timeout
- **Return**: `idleConnections.offer(connection, timeout, unit)` → returns false if queue full
- **Initialization**: Pre-fill queue with `initialSize` connections during pool construction

**Alternatives considered**:
- `LinkedBlockingQueue`: Rejected — unbounded by default, requires explicit capacity
- `SynchronousQueue`: Rejected — no capacity, each put waits for a take
- `ReentrantLock` + `Condition`: Rejected — more error-prone than `BlockingQueue`

**Source**: Java Concurrent API (`java.util.concurrent.ArrayBlockingQueue`)

---

## 5. Database URL Configuration — Environment Variables

**Decision**: Use `System.getenv()` for database configuration — not `${...}` Spring placeholder syntax.

**Rationale** (bug guard B2 from project memory):
- In pure Spring MVC (no Spring Boot), `${DB_HOST}` placeholders in `@Value` or direct string usage are NOT resolved automatically
- Using `System.getenv("DB_HOST")` works reliably in all environments
- Default values handled via `System.getenv().getOrDefault("DB_HOST", "localhost")`

**Pattern**:
```java
String host = System.getenv().getOrDefault("DB_HOST", "localhost");
String port = System.getenv().getOrDefault("DB_PORT", "5432");
String dbName = System.getenv().getOrDefault("DB_NAME", "resumainer");
String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
```

**Source**: Project memory — `docs/memory/BUGS.md` (B2 — DataSource URL placeholders)

---

## 6. Connection Reset Contract

**Decision**: Reset connection with rollback, setAutoCommit(true), setReadOnly(false), clearWarnings().

**Rationale**:
- `rollback()` — clears any uncommitted transaction left by the previous user
- `setAutoCommit(true)` — restores JDBC default (false is used during service transactions)
- `setReadOnly(false)` — restores default (may have been set to true for read-only operations)
- `clearWarnings()` — clears accumulated SQLWarning chain from previous operations
- These four operations cover all state that can leak between requests
- No need to reset schema/catalog (single database, single user)
- No need to reset networkTimeout (remains at driver default)

**Source**: Standard JDBC Connection contract + project consensus (Q2 from brainstorm session)
