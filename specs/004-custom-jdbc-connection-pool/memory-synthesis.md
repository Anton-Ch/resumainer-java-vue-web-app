# Memory Synthesis

## Current Scope
- Feature: 004-custom-jdbc-connection-pool
- Spec: Feature Specification: Custom JDBC Connection Pool
- Feature folder: specs\004-custom-jdbc-connection-pool
- Spec context: # Feature Specification : Custom JDBC Connection Pool **Feature Branch **: `004-custom-jdbc-connection-pool` **Created**: 2026-06-04 **Status**: Approved **Input**: User description : "rework database connections to use a thread-safe custom JDBC connection pool "...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] Performance and reliability are cross-cutting concerns that affect every layer from database to frontend rendering. Database Access : All database queries MUST use PreparedStatement to prevent SQL injection and enable query plan reuse. Raw string concatenation for SQL is forbidden. (Source: `.specify/memory/constitution.md`)
- [D2] The following technology decisions are mandatory and MUST NOT be changed without a governance amendment. Layer Technology Constraint Language Java 21 LTS Required Web Framework Spring MVC (no Spring Boot) Required Data Access Plain JDBC with custom thread-safe Connection Pool Required. No ORM, JPA, Hibernate, or Spring Data Database PostgreSQL (3NF normalized) Required Migrations Flyway (versioned SQL scripts) Required Frontend SPA Vue 3 (Composition API) + Vite + PrimeVue Required Landing Page Thymeleaf Required AI Integration OpenRouter API behind AiClientFactory interface Required. (Source: `.specify/memory/constitution.md`)
- [D3] Status Active Why this is durable Unit tests caught 0 of the 6 bugs found during manual testing of Feature 003. Bugs like missing Flyway bean, unresolved DataSource URL, unresponsive i18n validation messages, and duplicate toggle text were invisible to unit tests. They only appeared in the full Docker environment with actual PostgreSQL, Nginx, and browser interaction. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [V1] Status Active Why this is durable Registration requires creating User + ContactDetail atomically. The standard DAO pattern (each method opens/closes its own Connection via DataSource) cannot support multi-table transactions. This pattern will repeat for every future feature that needs atomic multi-table operations. (Source: `docs/memory/DECISIONS.md`)
- [V2] Milestone : Phase 3 (User Story 1 — Registration) of Feature 003 (Vue Auth Page) completed with TDD. (Source: `docs/memory/WORKLOG.md`)
- [V3] Every code change MUST preserve or improve the long-term maintainability of the codebase. Code quality is not negotiable and is verified through automated and manual review. Package Structure : Code MUST follow the standard Java layered architecture: controller/ , service/ , dao/ , model/ , config/ , util/ . (Source: `.specify/memory/constitution.md`)

## Relevant Security Constraints
- [S1] D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web .xml) | servlet , spring-mvc , jakarta-ee , tomcat , initialization , web .xml | DECISIONS .md | active D2 | Maven Wrapper Must Be at Same Directory Level as pom .xml | maven , wrapper , build , project-structure , best-practice | DECISIONS .md | active D3 | Docker Tomcat : Use bash /dev/tcp Instead of nc for TCP Health Checks | docker , tomcat , wait-for-it , networking ,... (Source: `docs/memory/INDEX.md`)

## Related Historical Lessons
- [B1] Status Active Symptoms Database connection fails with: Unable to parse URL jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:resumainer} . The URL contains the literal ${DB_HOST:localhost} string instead of the resolved value. (Source: `docs/memory/BUGS.md`)
- [B2] Status Active Symptoms Application starts successfully, API endpoints return HTTP 500 with error: ERROR: relation &quot;users&quot; does not exist (or any other table). The SQL migration files exist in db/migration/ but Flyway never created the tables. Root Cause In pure Spring MVC (without Spring Boot), Flyway is NOT auto-configured. (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
