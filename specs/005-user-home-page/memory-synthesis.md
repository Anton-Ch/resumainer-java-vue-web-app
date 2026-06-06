# Memory Synthesis

## Current Scope
- Feature: 005-user-home-page
- Spec: Feature Specification: User Home Page & Resume Workspace
- Feature folder: specs\005-user-home-page
- Spec context: # Feature Specification : User Home Page & Resume Workspace **Feature Branch **: `feat/005-user-home-page` **Created**: 2026-06-06 **Status**: Clarified **Input**: User description : "Let's create the User Home Page the resume workspace ....

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] The user experience MUST be consistent across all screens, languages, and interaction patterns. Every user-facing interaction follows the same rules. Internationalization : All user-facing strings MUST be externalized into resource files ( messages_en.properties , messages_ru.properties ) for both Thymeleaf (Landing Page) and Vue SPA. (Source: `.specify/memory/constitution.md`)
- [D2] Status Active Why this is durable Adding infrastructure beans (DataSource, Flyway) to the Spring context breaks any test that loads @ContextConfiguration(classes = WebConfig.class) because the DataSource initialization requires a real PostgreSQL connection. Controller tests that don't need database access should use standalone MockMvc setup to avoid this dependency. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [V1] Status : Active Why this is durable : When backend paginates with SQL LIMIT/OFFSET, PrimeVue DataTable MUST use :lazy=&quot;true&quot; . Client-side mode loads ALL records into browser memory and defeats server-side search/filter/sort. This mistake is easy to make (lazy is not the default) and hard to catch early. (Source: `docs/memory/DECISIONS.md`)

## Relevant Security Constraints
- [S1] D1 | Java Servlet Initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web .xml) | servlet , spring-mvc , jakarta-ee , tomcat , initialization , web .xml | DECISIONS .md | active D2 | Maven Wrapper Must Be at Same Directory Level as pom .xml | maven , wrapper , build , project-structure , best-practice | DECISIONS .md | active D3 | Docker Tomcat : Use bash /dev/tcp Instead of nc for TCP Health Checks | docker , tomcat , wait-for-it , networking ,... (Source: `docs/memory/INDEX.md`)
- [S2] W1 | First Feature MVP Achieved : Hello World Tomcat | milestone , mvp , hello-world , docker , spring-mvc , tomcat | WORKLOG .md | active W2 | Second Feature MVP Achieved : Thymeleaf Landing Page | milestone , mvp , landing-page , thymeleaf , i18n , feature-002 , bilingual | WORKLOG .md | active W3 | Feature 003 Planning and Security Review Completed | milestone , feature-003 , vue-auth , planning , security-review , specification | WORKLOG .md... (Source: `docs/memory/INDEX.md`)
- [S3] Status : Active Why this is durable : Feature 005 (User Home Page &amp; Resume Workspace) completed full planning cycle: spec → clarification → brainstorming → plan → research → data model → contracts → security review → component diagram → tasks. All 46 FRs, 11 SCs, 41 tasks, and 5 security findings documented. Tasks include [TDD], [SUBAGENT], and [REVIEW] execution markers. (Source: `docs/memory/WORKLOG.md`)

## Related Historical Lessons
- [B1] Status Superseded-by-ComponentScan Symptoms @ControllerAdvice with @ExceptionHandler methods never gets invoked. Log shows: ControllerAdvice beans: none during DispatcherServlet init. 404 errors show Tomcat default page instead of custom Thymeleaf template. (Source: `docs/memory/BUGS.md`)
- [B2] Status Active Symptoms Controller displays &quot;Active Profile: default&quot; even though spring.profiles.active=dev is set in application.properties. The @Value(&quot;${spring.profiles.active:default}&quot;) annotation resolves correctly, but Environment.getActiveProfiles() returns an empty array. Root Cause In pure Spring MVC (without Spring Boot), Environment.getActiveProfiles() is populated only through programmatic profile activation in a WebApplicationInitializer or ApplicationContextInitializer. (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [c] potentially stale memory surfaced from bugs / recurring bug patterns (`docs/memory/`) / template / 2026-05-31 - all spring stereotype annotations require explicit @bean in pure spring mvc (source: `docs/memory/bugs.md`)

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
