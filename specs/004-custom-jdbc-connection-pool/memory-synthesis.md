# Memory Synthesis

## Current Scope
- Feature: 004-custom-jdbc-connection-pool
- Spec: Feature Specification: Custom JDBC Connection Pool
- Feature folder: specs\004-custom-jdbc-connection-pool
- Spec context: # Feature Specification : Custom JDBC Connection Pool **Feature Branch **: `004-custom-jdbc-connection-pool` **Created**: 2026-06-04 **Status**: Approved **Input**: User description : "rework database connections to use a thread-safe custom JDBC connection pool "...

## Relevant Project Context
- [C1] constraint that affects many features constraint that an AI should respect before planning (Source: `docs/memory/PROJECT_CONTEXT.md`)

## Relevant Decisions
- [D1] The following technology decisions are mandatory and MUST NOT be changed without a governance amendment. Layer Technology Constraint Language Java 21 LTS Required Web Framework Spring MVC (no Spring Boot) Required Data Access Plain JDBC with custom thread-safe Connection Pool Required. No ORM, JPA, Hibernate, or Spring Data Database PostgreSQL (3NF normalized) Required Migrations Flyway (versioned SQL scripts) Required Frontend SPA Vue 3 (Composition API) + Vite + PrimeVue Required Landing Page Thymeleaf Required AI Integration OpenRouter API behind AiClientFactory interface Required. (Source: `.specify/memory/constitution.md`)
- [D2] Performance and reliability are cross-cutting concerns that affect every layer from database to frontend rendering. Database Access : All database queries MUST use PreparedStatement to prevent SQL injection and enable query plan reuse. Raw string concatenation for SQL is forbidden. (Source: `.specify/memory/constitution.md`)

## Active Architecture Constraints
- [A1] stable system boundaries ownership lines between modules or services integration constraints that affect many features (Source: `docs/memory/ARCHITECTURE.md`)

## Accepted Deviations
- [V1] Every code change MUST preserve or improve the long-term maintainability of the codebase. Code quality is not negotiable and is verified through automated and manual review. Package Structure : Code MUST follow the standard Java layered architecture: controller/ , service/ , dao/ , model/ , config/ , util/ . (Source: `.specify/memory/constitution.md`)
- [V2] Status Active Why this is durable Registration requires creating User + ContactDetail atomically. The standard DAO pattern (each method opens/closes its own Connection via DataSource) cannot support multi-table transactions. This pattern will repeat for every future feature that needs atomic multi-table operations. (Source: `docs/memory/DECISIONS.md`)

## Relevant Security Constraints
- [S1] Security MUST be integrated into every feature from design through implementation. Security is not an afterthought. Password Storage : All passwords MUST be hashed using BCrypt. (Source: `.specify/memory/constitution.md`)
- [S2] &lt;!-- Sync Impact Report (v1.0.0) Version change: (template) → 1.0.0 Modified principles: - [PRINCIPLE_1_NAME] → I. Code Quality &amp; Maintainability - [PRINCIPLE_2_NAME] → II. Testing Excellence - [PRINCIPLE_3_NAME] → III. (Source: `.specify/memory/constitution.md`)
- [S3] D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web .xml) | servlet , spring-mvc , jakarta-ee , tomcat , initialization , web .xml | DECISIONS .md | active D2 | Maven Wrapper Must Be at Same Directory Level as pom .xml | maven , wrapper , build , project-structure , best-practice | DECISIONS .md | active D3 | Docker Tomcat : Use bash /dev/tcp Instead of nc for TCP Health Checks | docker , tomcat , wait-for-it , networking ,... (Source: `docs/memory/INDEX.md`)

## Related Historical Lessons
- [none]

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
