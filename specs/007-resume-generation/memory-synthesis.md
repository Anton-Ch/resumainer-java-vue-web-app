# Memory Synthesis

## Current Scope
- Feature: 007-resume-generation
- Spec: Feature Specification: Resume Generation
- Feature folder: specs\007-resume-generation
- Spec context: # Feature Specification : Resume Generation **Feature Branch **: `feat/007-resume-generation` **Created**: 2026-06-12 **Status**: Draft **Input**: Build the full Generate Resume feature for ResumAIner : vacancy-specific resume generation using structured profile data ,...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] The user experience MUST be consistent across all screens, languages, and interaction patterns. Every user-facing interaction follows the same rules. Internationalization : All user-facing strings MUST be externalized into resource files ( messages_en.properties , messages_ru.properties ) for both Thymeleaf (Landing Page) and Vue SPA. (Source: `.specify/memory/constitution.md`)
- [D2] The following technology decisions are mandatory and MUST NOT be changed without a governance amendment. Layer Technology Constraint Language Java 21 LTS Required Web Framework Spring MVC (no Spring Boot) Required Data Access Plain JDBC with custom thread-safe Connection Pool Required. No ORM, JPA, Hibernate, or Spring Data Database PostgreSQL (3NF normalized) Required Migrations Flyway (versioned SQL scripts) Required Frontend SPA Vue 3 (Composition API) + Vite + PrimeVue Required Landing Page Thymeleaf Required AI Integration OpenRouter API behind AiClientFactory interface Required. (Source: `.specify/memory/constitution.md`)
- [D3] Status Active Why this is durable Every feature that adds i18n will need error pages in all supported languages. This decision defines the pattern: Thymeleaf templates with full branding for most errors, static HTML only for failures before the template engine is available. Decision Error pages (404, 500) are implemented as Thymeleaf templates in templates/error/ with full header, nav bar, language switcher, and footer matching the landing page design. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [V1] Status Active Milestone Phases 3-4 of Feature 006 (User Profile Page) completed with TDD. Phase 3 (DAO layer): 6 new DAOs: WorkExperienceDao, EducationDao, ProjectDao, CourseCertificateDao (pagination), AdditionalProfileInfoDao (upsert), WorkFormatDao All with connection-accepting overloads (D10), owner-scoped WHERE user_id=? (Source: `docs/memory/WORKLOG.md`)
- [V2] Status Active Why this is durable Multi-page wizards that share state across routes must use module-level state in composables. Otherwise each route page creates a fresh state instance on mount, losing all data from previous steps. Decision When a Vue composable is used across multiple pages in a wizard flow (e.g., Vacancy -&gt; Settings -&gt; Review -&gt; Export), the reactive state MUST be defined at the module level (outside the exported function), not inside the function body. (Source: `docs/memory/DECISIONS.md`)

## Relevant Security Constraints
- [S1] Status : Active Milestone : Feature 007 (Resume Generation) reached Tasks + Implementation phase complete. (Source: `docs/memory/WORKLOG.md`)
- [S2] Milestone : Feature 003 (Vue Auth Page) reaches Spec + Plan + Tasks + Security Review complete. (Source: `docs/memory/WORKLOG.md`)
- [S3] Status : Active Why this is durable : Feature 005 (User Home Page &amp; Resume Workspace) completed full planning cycle: spec → clarification → brainstorming → plan → research → data model → contracts → security review → component diagram → tasks. All 46 FRs, 11 SCs, 41 tasks, and 5 security findings documented. Tasks include [TDD], [SUBAGENT], and [REVIEW] execution markers. (Source: `docs/memory/WORKLOG.md`)

## Related Historical Lessons
- [B1] Status Active Symptoms A validateSortField() method converts the input to lowercase (field.trim().toLowerCase()) and then checks Set.contains(f). Even though the input value is in the ALLOWED list, the check fails and throws IllegalArgumentException. Error message shows the allowed values including the expected value, but contains() still returns false. (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
