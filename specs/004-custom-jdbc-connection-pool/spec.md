# Feature Specification: Custom JDBC Connection Pool

**Feature Branch**: `004-custom-jdbc-connection-pool`

**Created**: 2026-06-04

**Status**: Approved

**Input**: User description: "rework database connections to use a thread-safe custom JDBC connection pool"

## Clarifications

### Session 2026-06-04

- Q1: What should happen when pool configuration is invalid (initialSize > maxSize or maxSize = 0)? → A: Fail-fast — pool throws an exception during initialization; the application does not start. This scenario must be covered by tests.
- Q2: Which connection properties must be reset when returning to the pool? → A: Minimal reset — rollback(), setAutoCommit(true), setReadOnly(false), clearWarnings().
- Q3: What should happen to borrowed connections when pool.close() is called? → A: Pool sets closed flag and closes idle connections immediately. Borrowed connections are physically closed on return (proxy close() detects closed flag and closes physically instead of returning to the idle queue). No waiting for borrowed connections.
- Q4: What should happen when ConnectionFactory.createConnection() throws SQLException (database unavailable)? → A: Pool catches the exception, decreases the connection count, attempts to take another connection from the idle queue or create a new one. If none succeeds — waits up to borrowTimeoutMillis and throws ConnectionPoolException. Full retry loop.
- Q5: Should pool.close() be thread-safe and idempotent? → A: Yes. First call closes idle connections and sets closed flag. Subsequent calls are no-ops. Implemented via AtomicBoolean.

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Replace direct/manual connection creation with a manage connection pool (Priority: P1)

As a backend developer, I want the application to manage database connections through a central thread-safe pool instead of creating connections manually, so that the application doesn't exhaust database resources and connections are reliably reused.

**Why this priority**: This is the core functionality — without a working pool that handles connection lifecycle, all database-dependent features are at risk of resource leaks, performance degradation, and connection failures under concurrent load.

**Independent Test**: Can be tested by verifying that the pool initializes the configured number of connections on startup, and that repeated `getConnection()` calls return valid connections without creating physical connections beyond the configured limit.

**Acceptance Scenarios**:

1. **Given** the application starts, **When** the connection pool is initialized, **Then** the configured initial number of physical database connections are established immediately.
2. **Given** a developer calls `dataSource.getConnection()`, **When** idle connections are available, **Then** a valid logical connection is returned immediately.
3. **Given** the pool has reached its maximum size, **When** a developer requests a connection, **Then** the request waits up to the configured timeout before failing with a clear timeout error.

---

### User Story 2 — Safely return connections to the pool (Priority: P1)

As a developer, I want to call `close()` on a connection without physically closing the underlying database connection, so that the physical connection is reset and returned to the pool for reuse by other parts of the application.

**Why this priority**: Without safe connection return, every `close()` call would destroy a physical connection, defeating the purpose of the pool and degrading performance.

**Independent Test**: Can be tested by borrowing a connection, closing it, and verifying that the same physical connection can be borrowed again.

**Acceptance Scenarios**:

1. **Given** a developer has obtained a connection from the pool, **When** they call `close()` on it, **Then** the physical connection is not closed but reset and returned to the idle pool.
2. **Given** a connection is returned to the pool, **When** it was in a transaction, **Then** the transaction is rolled back before returning.
3. **Given** a connection is closed twice, **When** the second `close()` is called, **Then** no error occurs (idempotent close).

---

### User Story 3 — Graceful pool shutdown (Priority: P2)

As an operations developer, I want the pool to shut down gracefully when the application stops, so that active connections are properly closed and no database connections are leaked.

**Why this priority**: Resource leaks during shutdown can cause database-side connection exhaustion over repeated deployments.

**Independent Test**: Can be tested by creating a pool, borrowing a connection, returning it, calling `close()` on the pool, and verifying all idle physical connections are closed.

**Acceptance Scenarios**:

1. **Given** the pool is shutting down, **When** a developer calls the pool `close()` method, **Then** all idle physical connections are properly closed.
2. **Given** the pool is already closed, **When** a developer requests a connection, **Then** a clear error is thrown indicating the pool is closed.
3. **Given** the pool is shutting down, **When** a developer returns a connection, **Then** the physical connection is closed rather than returned to idle.

---

### User Story 4 — Connection validation before use (Priority: P2)

As a developer, I want to be sure that connections returned from the pool are usable, so that my application doesn't fail with stale/broken connections.

**Why this priority**: Without validation, the application could receive a dead connection, leading to hard-to-debug intermittent failures.

**Independent Test**: Can be tested by simulating an invalid connection in the pool and verifying that the pool does not return it to the application but instead creates a replacement or throws a clear error.

**Acceptance Scenarios**:

1. **Given** an idle connection becomes invalid (e.g., database restarted), **When** a developer requests a connection, **Then** the pool detects the invalid connection, discards it, and either returns a valid connection or throws a clear timeout error.
2. **Given** all available connections are invalid, **When** a developer requests a connection, **Then** the pool throws a clear timeout error after exhausting all retries within the configured timeout.

---

### User Story 5 — Future migration path (Priority: P3)

As a future maintainer, I want the application to depend only on the standard `DataSource` interface, so that the educational custom pool can be replaced with a production-grade pool (e.g., HikariCP) after Capstone acceptance with minimal code changes.

**Why this priority**: This is a long-term maintainability concern. It does not affect current functionality but is critical for the post-Capstone future of the project.

**Independent Test**: Can be verified by confirming that no application code outside the pool implementation imports or references the custom pool classes directly — only `javax.sql.DataSource` is used.

**Acceptance Scenarios**:

1. **Given** the custom pool is implemented, **When** a developer writes a service or DAO class, **Then** they depend on `javax.sql.DataSource`, not on the custom pool type.
2. **Given** a developer wants to replace the pool, **When** they create a new `DataSource` bean in Spring configuration, **Then** no other code changes are required.

---

### Edge Cases

- What happens when the pool is exhausted and all connections are busy? — The request should wait up to the configured timeout, then fail with a clear timeout error.
- What happens when a borrowed connection is never returned? — The physical connection remains unavailable for other threads until explicitly returned (leak detection is intentionally excluded).
- What happens when a developer passes a username and password to `getConnection(username, password)`? — The method should throw an `SQLFeatureNotSupportedException` since per-user database credentials are not supported.
- What happens if the pool fails to create the initial set of connections on startup? — The pool initialization should fail with a clear error, preventing the application from starting with an unusable pool.
- What happens when the database becomes unavailable after pool initialization? — Borrow requests should detect invalid connections during validation and fail with a clear timeout error.
- What happens when the pool configuration is invalid (e.g., `initialSize > maxSize` or `maxSize <= 0`)? — The pool throws a clear exception during initialization, preventing the application from starting with a broken pool (fail-fast). This scenario must be covered by tests.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST manage database connections through a central thread-safe DataSource.
- **FR-002**: The pool MUST create the configured number of physical connections on initialization ("initial size").
- **FR-003**: The pool MUST never create more physical connections than the configured maximum ("max size").
- **FR-004**: The pool MUST reuse idle connections instead of creating new ones whenever idle connections are available.
- **FR-005**: When a developer calls `close()` on a borrowed connection, the physical connection MUST NOT be closed — it MUST be reset and returned to the idle pool.
- **FR-006**: Before returning a connection to the developer, the pool MUST validate that the connection is usable (not closed, responds to a lightweight validation query).
- **FR-007**: If a connection is invalid OR connection creation fails (SQLException), the pool MUST close/discard it, decrease the connection count, and attempt to provide another valid connection — either from the idle queue, by creating a new one, or by waiting for one to be returned. This retry loop continues until the borrow timeout is exhausted.
- **FR-008**: When all connections are in use and the pool is at maximum size, the borrow request MUST wait up to the configured timeout before throwing a clear timeout error.
- **FR-009**: The pool MUST support graceful shutdown that closes all idle physical connections. The close() method MUST be idempotent and thread-safe — subsequent calls after the first MUST be no-ops.
- **FR-010**: After shutdown, any borrow request MUST fail immediately with a clear "pool closed" error.
- **FR-011**: The custom pool MUST comply with the standard Java DataSource contract so that the pool is interchangeable with other DataSource implementations.
- **FR-012**: Per-user database credentials (`getConnection(username, password)`) MUST be explicitly unsupported and MUST fail with a clear error message indicating the feature is not available.
- **FR-013**: The pool configuration MUST be configurable through external properties (JDBC URL, credentials, pool sizes, timeout settings).
- **FR-014**: Transactions MUST be managed by the Service layer, not by the pool. The pool only provides and returns connections.
- **FR-015**: The pool MUST log key lifecycle events (initialization, shutdown, connection borrow, connection return, validation failures, timeout errors) without leaking database credentials or sensitive configuration.
- **FR-016**: The pool MUST validate its configuration on initialization and MUST fail immediately with a clear exception if the configuration is invalid (e.g., `initialSize > maxSize`, `maxSize <= 0`, negative timeout values).

### Key Entities *(include if feature involves data)*

- **Connection Pool Config**: An immutable configuration object that holds pool settings — database connection parameters, pool size limits, and timeout values.
- **Physical Connection**: An actual TCP/database connection to PostgreSQL, created and managed by the pool.
- **Logical Connection (Proxy)**: A wrapper around a physical connection that intercepts `close()` calls and returns the physical connection to the pool instead of destroying it.
- **Idle Connection Queue**: A thread-safe queue of available physical connections ready for borrowing.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Pool initialization completes successfully, creating the configured number of initial physical connections, within reasonable startup time (under 3 seconds in local development).
- **SC-002**: Multiple concurrent threads can borrow and return connections without deadlocks or data races.
- **SC-003**: The pool never creates more physical connections than the configured maximum, verified by monitoring total connection count during concurrent load.
- **SC-004**: A borrow request that exceeds the configured timeout throws a clear timeout error that a developer can catch and handle.
- **SC-005**: After an invalid connection is detected, the pool does not return it to application code (returns a valid connection or fails with a timeout).
- **SC-006**: Graceful shutdown closes all idle connections and prevents new borrows.
- **SC-007**: The custom pool can be replaced with HikariCP by changing only the Spring `@Bean` configuration method and the `pom.xml` dependency — no service or DAO code changes required.
- **SC-008**: All acceptance scenarios from User Stories 1–5 are covered by automated unit tests.

## Constitution Alignment

This feature MUST comply with the ResumAIner Constitution principles:

| Principle | Impact on this feature |
|---|---|
| **I. Code Quality & Maintainability** | Pool classes follow SRP with clear separation: config, factory, proxy, pool manager, exception. All code uses `DataSource` abstraction. No Spring Boot, JPA, or Hibernate. Maven CLI build must succeed. |
| **II. Testing Excellence** | JUnit 5 + Mockito tests required for pool behavior (borrow, return, timeout, validation, shutdown). Connection pool tests must not require a real PostgreSQL database. JaCoCo coverage tracked. |
| **III. User Experience Consistency** | This is an infrastructure feature with no direct end-user UI impact. Error messages must be clear and descriptive for developers. |
| **IV. Performance & Reliability** | Thread-safe connection coordination prevents data races under concurrent load. Validation at borrow time prevents stale connection usage. Pool logs lifecycle events and errors for operational observability. PreparedStatement usage in existing DAOs remains unchanged. |
| **V. Security by Design** | Database credentials are stored in external configuration, never hardcoded. Connections are properly reset before returning to pool (rollback if needed, autoCommit reset). |

**Technology Constraint Check** (per Constitution Technology Stack):
- [x] Java 21, Spring MVC (no Spring Boot), Plain JDBC (no ORM)
- [x] PostgreSQL with Flyway migrations (unchanged)
- [x] Docker Compose for deployment (unchanged)
- [x] Dev + Prod Spring profiles (unchanged)

## Brainstorm Log

### Session 2026-06-04

Explored edge cases and boundary conditions for the custom JDBC connection pool. Key findings:

- **Invalid configuration (Q1)**: Fail-fast behavior added — pool validates config at init and throws if `initialSize > maxSize` or `maxSize <= 0`. New FR-016 and edge case.
- **Connection reset (Q2)**: Minimal reset strategy confirmed — rollback, setAutoCommit(true), setReadOnly(false), clearWarnings(). Documented in Assumptions.
- **Shutdown with borrowed connections (Q3)**: Pool sets closed flag, closes idle connections immediately. Borrowed connections are closed physically on return. No waiting, no tracking. Already consistent with US3.
- **Connection creation failure (Q4)**: Full retry loop — pool tries all available options (idle queue, new creation, wait for return) before timing out. FR-007 updated.
- **Idempotent close (Q5)**: pool.close() is idempotent and thread-safe via AtomicBoolean. FR-009 updated.

All 5 questions were resolved and applied to the spec.

## Assumptions

- The application connects to a single PostgreSQL database using a single technical database user — per-user database credentials are not required.
- The pool is intended for educational/demonstration purposes for a Capstone project — production-grade features (leak detection, JMX, async creation, idle eviction threads) are out of scope.
- The existing application already has some form of database connection mechanism (direct `DriverManager.getConnection()` or similar) that will be replaced by this pool.
- Transactions are managed manually in the Service layer using `setAutoCommit(false)`, `commit()`, `rollback()` — the pool itself does not manage transactions.
- The pool can be replaced with HikariCP after Capstone acceptance by changing only Spring configuration and `pom.xml`.
- Connection validation uses Java's `Connection.isValid(timeout)` method — no custom SQL validation queries are needed for the initial implementation.
- Pool logging uses standard logging levels: INFO for lifecycle events (init, shutdown), WARN for pool exhaustion or validation failures, ERROR for connection creation failures. Debug-level logging (borrow/return for individual connections) is disabled by default.
- Connection reset on return includes: rollback() (to clear any uncommitted transaction), setAutoCommit(true), setReadOnly(false), clearWarnings(). This minimal set is sufficient for single-user-database PostgreSQL usage.
