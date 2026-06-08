# Memory Synthesis

## Current Scope
- Feature: 006-user-profile
- Spec: Feature Specification: User Profile Page
- Feature folder: specs\006-user-profile
- Spec context: # Feature Specification : User Profile Page **Feature Branch **: `feat/006-profile-page` **Created**: 2026-06-07 **Status**: Approved **Input**: User description from `tempfiles/prototype/profile_brief_opencode.md` ## Clarifications ### Session 2026-06-07 - **Q1: Data volume per profile section...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] Status Active Why this is durable During manual testing, hardcoded English strings were found in AuthPage.vue (info panel text, subtitles) and in LoginForm/RegisterForm (Zod validation messages). These were not caught during implementation because they were &quot;invisible&quot; — the page looked correct in English, but switching to Russian revealed untranslated text. Every future feature with UI will have the same risk. (Source: `docs/memory/DECISIONS.md`)
- [D2] The user experience MUST be consistent across all screens, languages, and interaction patterns. Every user-facing interaction follows the same rules. Internationalization : All user-facing strings MUST be externalized into resource files ( messages_en.properties , messages_ru.properties ) for both Thymeleaf (Landing Page) and Vue SPA. (Source: `.specify/memory/constitution.md`)
- [D3] Status Active Why this is durable Unit tests caught 0 of the 6 bugs found during manual testing of Feature 003. Bugs like missing Flyway bean, unresolved DataSource URL, unresponsive i18n validation messages, and duplicate toggle text were invisible to unit tests. They only appeared in the full Docker environment with actual PostgreSQL, Nginx, and browser interaction. (Source: `docs/memory/DECISIONS.md`)
- [D4] Status Active Why this is durable pom.xml targets Java 21 ( &lt;release&gt;21&lt;/release&gt; , &lt;maven.compiler.source&gt;21&lt;/maven.compiler.source&gt; ). Installing a newer JDK (23+) causes subtle failures: Mockito 5.x inline mock maker cannot self-attach because JDK 23+ disables the Attach API. Every developer setting up this project will face this if their system JDK doesn't match. (Source: `docs/memory/DECISIONS.md`)
- [D5] Status Active Why this is durable Adding infrastructure beans (DataSource, Flyway) to the Spring context breaks any test that loads @ContextConfiguration(classes = WebConfig.class) because the DataSource initialization requires a real PostgreSQL connection. Controller tests that don't need database access should use standalone MockMvc setup to avoid this dependency. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [V1] Status Active Why this is durable Every Vue SPA feature that accepts user input needs form validation. PrimeVue 4 introduced a new Form component with resolver-based validation. The pattern is non-obvious and differs from PrimeVue 3. (Source: `docs/memory/DECISIONS.md`)
- [V2] Status Active Why this is durable Registration requires creating User + ContactDetail atomically. The standard DAO pattern (each method opens/closes its own Connection via DataSource) cannot support multi-table transactions. This pattern will repeat for every future feature that needs atomic multi-table operations. (Source: `docs/memory/DECISIONS.md`)

## Relevant Security Constraints
- [S1] Status Active Why this is durable Every feature with form submissions needs CSRF protection. Without Spring Security, there is no built-in CSRF filter. This pattern must be reused for all future POST/PUT/DELETE endpoints. (Source: `docs/memory/DECISIONS.md`)
- [S2] Security MUST be integrated into every feature from design through implementation. Security is not an afterthought. Password Storage : All passwords MUST be hashed using BCrypt. (Source: `.specify/memory/constitution.md`)

## Related Historical Lessons
- [none]

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
