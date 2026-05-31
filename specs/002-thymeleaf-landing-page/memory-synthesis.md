# Memory Synthesis

## Current Scope
- Feature: 002-thymeleaf-landing-page
- Spec: Feature Specification: Thymeleaf Landing Page
- Feature folder: specs\002-thymeleaf-landing-page
- Spec context: # Feature Specification : Thymeleaf Landing Page **Feature Branch **: `feat/002-thymeleaf-landing-page` **Created**: 2026-05-31 **Status**: Approved **Input**: User description : "Create a Thymeleaf Landing Page that introduces ResumAIner to first-time visitors , explains...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] Status Active Why this is durable Tomcat 10.1+ uses Jakarta EE 10 (jakarta.servlet.* namespace). web.xml with javax.* namespace causes class loading conflicts. This decision applies to every servlet/controller feature in the project. (Source: `docs/memory/DECISIONS.md`)
- [D2] B1 | Controller Without Registration Is Invisible to Spring MVC | spring-mvc, controller, configuration, component-scan, bean-registration | BUGS.md | active B2 | Spring MVC (non-Boot): Use @Value for Profile, Not Environment.getActiveProfiles() | spring-mvc, profile, environment, configuration, properties | BUGS.md | active (Source: `docs/memory/INDEX.md`)
- [D3] The following technology decisions are mandatory and MUST NOT be changed without a governance amendment. Layer Technology Constraint Language Java 21 LTS Required Web Framework Spring MVC (no Spring Boot) Required Data Access Plain JDBC with custom thread-safe Connection Pool Required. No ORM, JPA, Hibernate, or Spring Data Database PostgreSQL (3NF normalized) Required Migrations Flyway (versioned SQL scripts) Required Frontend SPA Vue 3 (Composition API) + Vite + PrimeVue Required Landing Page Thymeleaf Required AI Integration OpenRouter API behind AiClientFactory interface Required. (Source: `.specify/memory/constitution.md`)
- [D4] Status Active Why this is durable Maven Wrapper scripts (mvnw, mvnw.cmd) determine the project root directory. They look for pom.xml in the same directory. If pom.xml is elsewhere (e.g., backend/), running mvnw from the project root fails with &quot;The goal you specified requires a project to execute but there is no POM in this directory.&quot; Decision Place mvnw, mvnw.cmd, and .mvn/wrapper/ in the same directory as pom.xml. (Source: `docs/memory/DECISIONS.md`)
- [D5] W1 | First Feature MVP Achieved: Hello World Tomcat | milestone, mvp, hello-world, docker, spring-mvc, tomcat | WORKLOG.md | active (Source: `docs/memory/INDEX.md`)

## Active Architecture Constraints
- [A1] Status Active Why this is durable Defines the project's approach to servlet container setup. All future controllers depend on this initialization mechanism. Boundary The project does not use web.xml for servlet registration. (Source: `docs/memory/ARCHITECTURE.md`)

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
