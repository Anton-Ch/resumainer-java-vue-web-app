# Memory Synthesis

## Current Scope
- Feature: 002-thymeleaf-landing-page
- Spec: Feature Specification: Thymeleaf Landing Page
- Feature folder: specs\002-thymeleaf-landing-page
- Spec context: # Feature Specification : Thymeleaf Landing Page **Feature Branch **: `feat/002-thymeleaf-landing-page` **Created**: 2026-05-31 **Status**: Approved **Input**: User description : "Create a Thymeleaf Landing Page that introduces ResumAIner to first-time visitors , explains...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] The user experience MUST be consistent across all screens, languages, and interaction patterns. Every user-facing interaction follows the same rules. Internationalization : All user-facing strings MUST be externalized into resource files ( messages_en.properties , messages_ru.properties ) for both Thymeleaf (Landing Page) and Vue SPA. (Source: `.specify/memory/constitution.md`)
- [D2] The following technology decisions are mandatory and MUST NOT be changed without a governance amendment. Layer Technology Constraint Language Java 21 LTS Required Web Framework Spring MVC (no Spring Boot) Required Data Access Plain JDBC with custom thread-safe Connection Pool Required. No ORM, JPA, Hibernate, or Spring Data Database PostgreSQL (3NF normalized) Required Migrations Flyway (versioned SQL scripts) Required Frontend SPA Vue 3 (Composition API) + Vite + PrimeVue Required Landing Page Thymeleaf Required AI Integration OpenRouter API behind AiClientFactory interface Required. (Source: `.specify/memory/constitution.md`)
- [D3] Status Active Why this is durable Maven Wrapper scripts (mvnw, mvnw.cmd) determine the project root directory. They look for pom.xml in the same directory. If pom.xml is elsewhere (e.g., backend/), running mvnw from the project root fails with &quot;The goal you specified requires a project to execute but there is no POM in this directory.&quot; Decision Place mvnw, mvnw.cmd, and .mvn/wrapper/ in the same directory as pom.xml. (Source: `docs/memory/DECISIONS.md`)
- [D4] Performance and reliability are cross-cutting concerns that affect every layer from database to frontend rendering. Database Access : All database queries MUST use PreparedStatement to prevent SQL injection and enable query plan reuse. Raw string concatenation for SQL is forbidden. (Source: `.specify/memory/constitution.md`)
- [D5] Testing is the primary mechanism for verifying correctness and preventing regression. Tests are written first, are meaningful, and produce measurable coverage. Test Framework : All tests MUST use JUnit 5 with Mockito for isolation. (Source: `.specify/memory/constitution.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [none]

## Relevant Security Constraints
- [S1] Security MUST be integrated into every feature from design through implementation. Security is not an afterthought. Password Storage : All passwords MUST be hashed using BCrypt. (Source: `.specify/memory/constitution.md`)

## Related Historical Lessons
- [B1] Status Active Symptoms DispatcherServlet initializes successfully (no errors), but all HTTP requests return 404 with &quot;No endpoint GET /...&quot; even though the mapping exists in the controller. HandlerMapping shows zero mappings. Root Cause In Spring MVC (non-Boot) with @Configuration + @EnableWebMvc, the @Controller annotation alone does NOT register the controller as a Spring bean. (Source: `docs/memory/BUGS.md`)
- [B2] Status Active Symptoms Controller displays &quot;Active Profile: default&quot; even though spring.profiles.active=dev is set in application.properties. The @Value(&quot;${spring.profiles.active:default}&quot;) annotation resolves correctly, but Environment.getActiveProfiles() returns an empty array. Root Cause In pure Spring MVC (without Spring Boot), Environment.getActiveProfiles() is populated only through programmatic profile activation in a WebApplicationInitializer or ApplicationContextInitializer. (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
