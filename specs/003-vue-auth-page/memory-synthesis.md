# Memory Synthesis

## Current Scope
- Feature: 003-vue-auth-page
- Spec: Feature Specification: Vue Auth Page
- Feature folder: specs\003-vue-auth-page
- Spec context: # Feature Specification : Vue Auth Page **Feature Branch **: `feat/003-vue-auth-page` **Created**: 2026-06-02 **Status**: Draft **Input**: User description : "create a specification for a Vue Auth Page . This will include both...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] The following technology decisions are mandatory and MUST NOT be changed without a governance amendment. Layer Technology Constraint Language Java 21 LTS Required Web Framework Spring MVC (no Spring Boot) Required Data Access Plain JDBC with custom thread-safe Connection Pool Required. No ORM, JPA, Hibernate, or Spring Data Database PostgreSQL (3NF normalized) Required Migrations Flyway (versioned SQL scripts) Required Frontend SPA Vue 3 (Composition API) + Vite + PrimeVue Required Landing Page Thymeleaf Required AI Integration OpenRouter API behind AiClientFactory interface Required. (Source: `.specify/memory/constitution.md`)
- [D2] B1 | Controller Without Registration Is Invisible to Spring MVC | spring-mvc , controller , configuration , component-scan , bean-registration | BUGS .md | active B2 | Spring MVC (non-Boot): Use @Value for Profile , Not Environment .getActiveProfiles() | spring-mvc , profile , environment , configuration , properties | BUGS .md | active B3 | SpringResourceTemplateResolver : ServletContext prefix fails in MockMvc tests | thymeleaf , template , resolver , classpath , mockmvc , spring-mvc , testing | BUGS .md... (Source: `docs/memory/INDEX.md`)
- [D3] D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web .xml) | servlet , spring-mvc , jakarta-ee , tomcat , initialization , web .xml | DECISIONS .md | active D2 | Maven Wrapper Must Be at Same Directory Level as pom .xml | maven , wrapper , build , project-structure , best-practice | DECISIONS .md | active D3 | Docker Tomcat : Use bash /dev/tcp Instead of nc for TCP Health Checks | docker , tomcat , wait-for-it , networking ,... (Source: `docs/memory/INDEX.md`)
- [D4] Performance and reliability are cross-cutting concerns that affect every layer from database to frontend rendering. Database Access : All database queries MUST use PreparedStatement to prevent SQL injection and enable query plan reuse. Raw string concatenation for SQL is forbidden. (Source: `.specify/memory/constitution.md`)
- [D5] Status Active Why this is durable When running a Java application in Docker, the WAR/JAR is baked into the image during docker build. docker compose up -d without rebuilding uses the cached image with the old code. Developers waste time debugging why changes don't take effect. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [none]

## Relevant Security Constraints
- [S1] Security MUST be integrated into every feature from design through implementation. Security is not an afterthought. Password Storage : All passwords MUST be hashed using BCrypt. (Source: `.specify/memory/constitution.md`)

## Related Historical Lessons
- [B1] Status Active Symptoms DispatcherServlet initializes successfully (no errors), but all HTTP requests return 404 with &quot;No endpoint GET /...&quot; even though the mapping exists in the controller. HandlerMapping shows zero mappings. Root Cause In Spring MVC (non-Boot) with @Configuration + @EnableWebMvc, the @Controller annotation alone does NOT register the controller as a Spring bean. (Source: `docs/memory/BUGS.md`)
- [B2] Status Active Symptoms Docker container repeatedly restarts with exit code 127. Logs show: /usr/bin/env: 'bash\r': No such file or directory Root Cause On Windows, Git checks out or creates shell scripts with CRLF ( \r\n ) line endings. The Linux kernel's shebang ( #! (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
