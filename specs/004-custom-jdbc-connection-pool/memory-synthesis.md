# Memory Synthesis

## Current Scope
- Feature: 004-custom-jdbc-connection-pool
- Spec: Feature Specification: Custom JDBC Connection Pool
- Feature folder: specs\004-custom-jdbc-connection-pool
- Spec context: # Feature Specification : Custom JDBC Connection Pool **Feature Branch **: `004-custom-jdbc-connection-pool` **Created**: 2026-06-04 **Status**: Approved **Input**: User description : "rework database connections to use a thread-safe custom JDBC connection pool "...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] Status Active Why this is durable Unit tests caught 0 of the 6 bugs found during manual testing of Feature 003. Bugs like missing Flyway bean, unresolved DataSource URL, unresponsive i18n validation messages, and duplicate toggle text were invisible to unit tests. They only appeared in the full Docker environment with actual PostgreSQL, Nginx, and browser interaction. (Source: `docs/memory/DECISIONS.md`)
- [D2] Performance and reliability are cross-cutting concerns that affect every layer from database to frontend rendering. Database Access : All database queries MUST use PreparedStatement to prevent SQL injection and enable query plan reuse. Raw string concatenation for SQL is forbidden. (Source: `.specify/memory/constitution.md`)
- [D3] Status Active Why this is durable Adding infrastructure beans (DataSource, Flyway) to the Spring context breaks any test that loads @ContextConfiguration(classes = WebConfig.class) because the DataSource initialization requires a real PostgreSQL connection. Controller tests that don't need database access should use standalone MockMvc setup to avoid this dependency. (Source: `docs/memory/DECISIONS.md`)
- [D4] Context : Custom ConnectionFactory uses DriverManager.getConnection() to create physical database connections. In Tomcat with Java 9+, the DriverManager's ServiceLoader-based driver discovery fails to find the PostgreSQL driver in WEB-INF/lib because DriverManager's static initializer runs before the webapp classloader is active (classloader isolation in Java module system). Decision : Add Class.forName(&quot;org.postgresql.Driver&quot;) in ConnectionFactory's static initializer to force-load the driver class at webapp startup time. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [V1] Status Active Why this is durable Registration requires creating User + ContactDetail atomically. The standard DAO pattern (each method opens/closes its own Connection via DataSource) cannot support multi-table transactions. This pattern will repeat for every future feature that needs atomic multi-table operations. (Source: `docs/memory/DECISIONS.md`)

## Relevant Security Constraints
- [none]

## Related Historical Lessons
- [B1] Status Active Symptoms Application starts successfully, API endpoints return HTTP 500 with error: ERROR: relation &quot;users&quot; does not exist (or any other table). The SQL migration files exist in db/migration/ but Flyway never created the tables. Root Cause In pure Spring MVC (without Spring Boot), Flyway is NOT auto-configured. (Source: `docs/memory/BUGS.md`)
- [B2] Status : Active Why this is durable : Any code using java.lang.reflect.Proxy with InvocationHandler will encounter this. Method.invoke() wraps the target method's exception in InvocationTargetException . Catching Exception generically (as catch (Exception e) ) wraps the InvocationTargetException in yet another exception, producing a 3-layer chain that breaks instanceof checks and getCause() traversal. (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [c] potentially stale memory surfaced from worklog / worklog / template / 2026-06-04 - feature 004 custom jdbc connection pool implementation completed (source: `docs/memory/worklog.md`)

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
