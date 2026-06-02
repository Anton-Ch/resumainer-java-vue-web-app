# Memory Synthesis

## Current Scope
- Feature: 003-vue-auth-page
- Spec: Feature Specification: Vue Auth Page
- Feature folder: specs\003-vue-auth-page
- Spec context: # Feature Specification : Vue Auth Page **Feature Branch **: `feat/003-vue-auth-page` **Created**: 2026-06-02 **Status**: Draft **Input**: User description : "создай спецификацию для Vue Auth Page . Тут уже будет и frontend и...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] The following technology decisions are mandatory and MUST NOT be changed without a governance amendment. Layer Technology Constraint Language Java 21 LTS Required Web Framework Spring MVC (no Spring Boot) Required Data Access Plain JDBC with custom thread-safe Connection Pool Required. No ORM, JPA, Hibernate, or Spring Data Database PostgreSQL (3NF normalized) Required Migrations Flyway (versioned SQL scripts) Required Frontend SPA Vue 3 (Composition API) + Vite + PrimeVue Required Landing Page Thymeleaf Required AI Integration OpenRouter API behind AiClientFactory interface Required. (Source: `.specify/memory/constitution.md`)
- [D2] Status Active Why this is durable Every feature that adds i18n or custom error pages needs this configuration. Without it, 404 errors fall through to the servlet container default page. Decision Custom 404 Thymeleaf templates require two changes: AppInitializer : override createDispatcherServlet() and call dispatcherServlet.setThrowExceptionIfNoHandlerFound(true) so Spring MVC throws NoHandlerFoundException for unhandled URLs. (Source: `docs/memory/DECISIONS.md`)
- [D3] The user experience MUST be consistent across all screens, languages, and interaction patterns. Every user-facing interaction follows the same rules. Internationalization : All user-facing strings MUST be externalized into resource files ( messages_en.properties , messages_ru.properties ) for both Thymeleaf (Landing Page) and Vue SPA. (Source: `.specify/memory/constitution.md`)
- [D4] B1 | Controller Without Registration Is Invisible to Spring MVC | spring-mvc , controller , configuration , component-scan , bean-registration | BUGS .md | active B2 | Spring MVC (non-Boot): Use @Value for Profile , Not Environment .getActiveProfiles() | spring-mvc , profile , environment , configuration , properties | BUGS .md | active B3 | SpringResourceTemplateResolver : ServletContext prefix fails in MockMvc tests | thymeleaf , template , resolver , classpath , mockmvc , spring-mvc , testing | BUGS .md... (Source: `docs/memory/INDEX.md`)
- [D5] D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web .xml) | servlet , spring-mvc , jakarta-ee , tomcat , initialization , web .xml | DECISIONS .md | active D2 | Maven Wrapper Must Be at Same Directory Level as pom .xml | maven , wrapper , build , project-structure , best-practice | DECISIONS .md | active D3 | Docker Tomcat : Use bash /dev/tcp Instead of nc for TCP Health Checks | docker , tomcat , wait-for-it , networking ,... (Source: `docs/memory/INDEX.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [none]

## Relevant Security Constraints
- [none]

## Related Historical Lessons
- [B1] Status Active Symptoms DispatcherServlet initializes successfully (no errors), but all HTTP requests return 404 with &quot;No endpoint GET /...&quot; even though the mapping exists in the controller. HandlerMapping shows zero mappings. Root Cause In Spring MVC (non-Boot) with @Configuration + @EnableWebMvc, the @Controller annotation alone does NOT register the controller as a Spring bean. (Source: `docs/memory/BUGS.md`)
- [B2] Status Active Symptoms Controller displays &quot;Active Profile: default&quot; even though spring.profiles.active=dev is set in application.properties. The @Value(&quot;${spring.profiles.active:default}&quot;) annotation resolves correctly, but Environment.getActiveProfiles() returns an empty array. Root Cause In pure Spring MVC (without Spring Boot), Environment.getActiveProfiles() is populated only through programmatic profile activation in a WebApplicationInitializer or ApplicationContextInitializer. (Source: `docs/memory/BUGS.md`)
- [B3] Status Active Symptoms @ControllerAdvice with @ExceptionHandler methods never gets invoked. Log shows: ControllerAdvice beans: none during DispatcherServlet init. 404 errors show Tomcat default page instead of custom Thymeleaf template. (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
