# Implementation Plan: Custom JDBC Connection Pool

**Branch**: `feat/004-custom-jdbc-connection-pool` | **Date**: 2026-06-04 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/004-custom-jdbc-connection-pool/spec.md`

## Summary

Replace direct/adhoc database connection management with a custom thread-safe JDBC connection pool implementing `javax.sql.DataSource`. The pool consists of 5 classes (Config, Factory, Proxy, Pool, Exception) in package `com.resumainer.infrastructure.db`. Thread-safety uses `BlockingQueue` + `AtomicInteger` + `AtomicBoolean`. Connection validation is lazy at borrow time via `Connection.isValid()`. Transactions remain managed at the Service layer.

## Technical Context

**Language/Version**: Java 21 LTS

**Primary Dependencies**: PostgreSQL JDBC Driver (org.postgresql:postgresql), SLF4J + Logback

**Storage**: PostgreSQL 16 (same database, no schema changes needed)

**Testing**: JUnit 5 + Mockito for unit tests (no real PostgreSQL required). JaCoCo for coverage. Docker smoke-test with real PostgreSQL for integration.

**Target Platform**: Linux x86_64 (Docker container — Apache Tomcat 10.1+)

**Project Type**: Web application backend — infrastructure layer (connection pool)

**Performance Goals**: Connection acquisition under 100ms when idle connections available. Pool max size enforced strictly.

**Constraints**: Must implement `javax.sql.DataSource`. Must NOT use HikariCP/DBCP/C3P0/Tomcat Pool. Must NOT use Spring Boot auto-configuration. Database credentials from environment variables via `System.getenv()` (not `${...}` placeholders).

**Scale/Scope**: Single PostgreSQL database, single technical user, Capstone project scale (concurrent users < 100)

## Constitution Check

*GATE: Must pass before proceeding to task breakdown. Re-check if design changes.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | ✅ Pass | 5 classes, SRP, layered package (`config/util`), GoF Singleton pattern documented. No JPA/Hibernate/Spring Boot. `mvn clean package` will succeed. |
| **II. Testing Excellence** | ✅ Pass | JUnit 5 + Mockito. TDD for pool logic. No real DB required in unit tests. JaCoCo coverage tracked. |
| **III. User Experience Consistency** | ✅ Pass | Infrastructure feature, no UI. Error messages clear and developer-readable. |
| **IV. Performance & Reliability** | ✅ Pass | PreparedStatement unchanged in DAOs. Thread-safe via BlockingQueue. Connection validation at borrow time. Logging for observability (FR-015). |
| **V. Security by Design** | ✅ Pass | Credentials from external config/env vars (B2 guard). Connections reset on return (rollback, autoCommit). No secrets in logs (FR-015). |

## Project Structure

### Documentation (this feature)

```text
specs/004-custom-jdbc-connection-pool/
├── spec.md                 # Approved feature specification
├── plan.md                 # This file
├── research.md             # Phase 0 — Context7 research findings
├── data-model.md           # Phase 1 — configuration entities
├── quickstart.md           # Phase 1 — developer setup guide
├── contracts/              # Phase 1 — DataSource contract notes
├── memory-synthesis.md     # Memory Hub context
├── doc-synthesis.md        # Doc cache synthesis
├── checklists/
│   └── requirements.md     # Spec quality checklist
└── spec_input_files/
    └── connection_pool_idea.md  # Input idea document
```

### Source Code (repository root)

```text
backend/
└── src/
    └── main/
        └── java/
            └── com/
                └── resumainer/
                    └── infrastructure/
                        └── db/
                            ├── ConnectionPoolConfig.java
                            ├── ConnectionFactory.java
                            ├── PooledConnectionProxy.java
                            ├── SimpleConnectionPool.java
                            └── ConnectionPoolException.java
    └── test/
        └── java/
            └── com/
                └── resumainer/
                    └── infrastructure/
                        └── db/
                            ├── ConnectionPoolConfigTest.java
                            ├── ConnectionFactoryTest.java
                            ├── PooledConnectionProxyTest.java
                            ├── SimpleConnectionPoolTest.java
                            └── ConnectionPoolExceptionTest.java
```

**Structure Decision**: Option 2 — Web application (backend/frontend monorepo). The pool is a pure backend infrastructure addition under `com.resumainer.infrastructure.db`. No frontend changes.

## Complexity Tracking

No constitution violations. The feature is within scope and follows all principles.

## Phase 0: Research Topics

The following topics need Context7 documentation consultation before implementation:

1. **Java dynamic proxy pattern**: `InvocationHandler` for `PooledConnectionProxy` — intercept `close()` on `java.sql.Connection`
2. **`Connection.isValid(int timeout)`**: PostgreSQL JDBC driver support and behavior
3. **Spring `@Bean(destroyMethod = "close")`**: Lifecycle behavior in pure Spring MVC (no Boot)
4. **`ArrayBlockingQueue` usage for resource pools**: Best practices for `poll(timeout, unit)` vs `take()`
5. **JDBC `Connection` reset contract**: `clearWarnings()`, `setReadOnly(false)` behavior

## Phase 1: Design Artifacts

- **data-model.md**: Configuration entity (ConnectionPoolConfig fields, validation rules)
- **contracts/**: DataSource interface contract — methods to implement, unsupported methods
- **quickstart.md**: Developer setup with pool configuration properties

## Phase 2: Task Breakdown (future — `/speckit.tasks`)

Tasks will be generated after plan approval. Expected structure:

1. Inspect and clean existing connection artifacts
2. Create `ConnectionPoolConfig` with validation
3. Create `ConnectionFactory` (physical connection creation + validation)
4. Create `ConnectionPoolException` — messages must describe the problem WITHOUT including JDBC URL, host, port, or database name (SEC-001 guard)
5. Create `PooledConnectionProxy` (InvocationHandler)
6. Create `SimpleConnectionPool` (core pool logic)
7. Create Spring `DataSourceConfig` bean
8. Update existing services/DAOs to use DataSource
9. Add unit tests for all pool classes
10. Docker smoke test with real PostgreSQL
11. Javadoc and design documentation
12. `mvn clean package` verification

## Known Bug Guards (from Memory Synthesis)

- **B1 — Flyway bean**: Ensure `Flyway @Bean(initMethod="migrate")` is configured before pool init. Pool depends on existing schema.
- **B2 — DataSource URL**: Use `System.getenv("DB_HOST")` not `${DB_HOST}`. Pool config resolves from env vars explicitly.
- **D3 — Docker testing**: Unit tests cover logic, Docker smoke-test covers real DB integration.
- **SEC-003 — unwrap() contract**: `DataSource.unwrap()` must throw `SQLException` if the class is not wrappable — do NOT return `null` (per JDBC 4.0 Wrapper spec).
