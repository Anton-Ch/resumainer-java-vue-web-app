# Memory Synthesis

## Current Scope
- Feature: 005-user-home-page
- Spec: Feature Specification: User Home Page & Resume Workspace
- Feature folder: specs\005-user-home-page
- Spec context: # Feature Specification : User Home Page & Resume Workspace **Feature Branch **: `feat/005-user-home-page` **Created**: 2026-06-06 **Status**: Clarified **Input**: User description : "Let's create the User Home Page the resume workspace ....

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] The following technology decisions are mandatory and MUST NOT be changed without a governance amendment. Layer Technology Constraint Language Java 21 LTS Required Web Framework Spring MVC (no Spring Boot) Required Data Access Plain JDBC with custom thread-safe Connection Pool Required. No ORM, JPA, Hibernate, or Spring Data Database PostgreSQL (3NF normalized) Required Migrations Flyway (versioned SQL scripts) Required Frontend SPA Vue 3 (Composition API) + Vite + PrimeVue Required Landing Page Thymeleaf Required AI Integration OpenRouter API behind AiClientFactory interface Required. (Source: `.specify/memory/constitution.md`)
- [D2] Performance and reliability are cross-cutting concerns that affect every layer from database to frontend rendering. Database Access : All database queries MUST use PreparedStatement to prevent SQL injection and enable query plan reuse. Raw string concatenation for SQL is forbidden. (Source: `.specify/memory/constitution.md`)
- [D3] Status Active Why this is durable Adding infrastructure beans (DataSource, Flyway) to the Spring context breaks any test that loads @ContextConfiguration(classes = WebConfig.class) because the DataSource initialization requires a real PostgreSQL connection. Controller tests that don't need database access should use standalone MockMvc setup to avoid this dependency. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [none]

## Relevant Security Constraints
- [S1] Security MUST be integrated into every feature from design through implementation. Security is not an afterthought. Password Storage : All passwords MUST be hashed using BCrypt. (Source: `.specify/memory/constitution.md`)
- [S2] Milestone : Feature 003 (Vue Auth Page) reaches Spec + Plan + Tasks + Security Review complete. (Source: `docs/memory/WORKLOG.md`)
- [S3] D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web .xml) | servlet , spring-mvc , jakarta-ee , tomcat , initialization , web .xml | DECISIONS .md | active D17 | Explicit Class .forName for JDBC driver in Tomcat webapps (Java 9 +) | tomcat ,jdbc,classloader,postgresql,driver,java-9,module-system,architecture | DECISIONS .md | active D2 | Maven Wrapper Must Be at Same Directory Level as pom .xml | maven , wrapper , build , project-structure , best-practice | DECISIONS .md | active D3 |... (Source: `docs/memory/INDEX.md`)

## Related Historical Lessons
- [none]

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
