# Memory Synthesis

## Current Scope
Feature 011 — Auth Hardening and Spring Security Migration. Full migration from custom session auth to Spring Security in a non-Boot Spring MVC app with Vue SPA frontend.

**Affected modules**: auth controller/service/DAO, security filters/interceptors, frontend auth pages/service/router, admin user details, landing page, Docker/configuration, Flyway migrations.

## Relevant Decisions
- **CSRF cookie-to-header pattern (D-CSRF-001)** — Current project uses custom `CsrfFilter` (extends OncePerRequestFilter) with XSRF-TOKEN cookie and X-CSRF-Token header. Decision explicitly notes: "If the project adds Spring Security in the future, replace CsrfFilter with Spring Security's built-in CSRF protection." (Reason: migration target known, Source: `docs/memory/DECISIONS.md` L256-281)
- **D47 — Data exposure audit must include SQL SELECT columns and log statements** — When auditing security, check all of: DTOs, SQL SELECT columns, log statements, error responses. Do NOT rely on DTO-only review. (Reason: our new auth logging FRs must follow this, Source: `docs/memory/DECISIONS.md` L1177-1200)
- **D40 — Separate implementation complete from contract proven** — Checkpoint evidence standard: changed files + assertions + sample + audit. (Reason: applies to every STOP checkpoint in this feature, Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- **Non-Boot filter registration** — In pure Spring MVC, filters register via `AppInitializer.getServletFilters()` returning `Filter[]`. `FilterRegistrationBean` is Spring Boot only and will cause compilation errors. (Reason: critical for SecurityFilterChain bootstrap, Source: `docs/memory/BUGS.md` L174-187)
- **Flyway requires explicit bean** — Pure Spring MVC does NOT auto-configure Flyway. Must define `@Bean(initMethod="migrate")` in config. (Reason: applies to all new migrations in this feature, Source: `docs/memory/BUGS.md` L330-361)
- **MockMvc standalone session isolation** — Each `perform()` creates a fresh MockHttpSession. Filter tests requiring session state must pre-configure MockHttpSession. (Reason: applies to SecurityFilterChain/CSRF testing, Source: `docs/memory/BUGS.md` L291-326)

## Accepted Deviations
- None identified.

## Relevant Security Constraints
- **S1 — CSRF is mandatory for all form submissions** — Without Spring Security, custom CSRF filter is required. This feature replaces it with Spring Security CSRF. (Source: `docs/memory/DECISIONS.md`)
- **S2 — BCrypt for all password storage** — Constitution mandates BCrypt hashing. (Source: `.specify/memory/constitution.md`)
- **S3 — No secrets in logs, DTOs, or frontend assets** — D47 extends this to SQL columns and log statements. (Source: `docs/memory/DECISIONS.md` L1177-1200)

## Related Historical Lessons
- **B1 — CSRF header migration risk** — Frontend API services must include CSRF headers for unsafe methods. When migrating from X-CSRF-Token to X-XSRF-TOKEN, ALL unsafe-request paths must be updated, not just auth service. (Source: `docs/memory/BUGS.md` L554-580)
- **B2 — Auth error handling in frontend** — Login/Register forms must catch errors from authService. Our new auth error codes must be handled the same way. (Source: `docs/memory/BUGS.md` L647-670)
- **B3 — Uniform 404 delay for public endpoints** — Public unauthenticated endpoints must use uniform delay to prevent enumeration. Relevant for verify-email, password-reset-validate, and forgot-password endpoints. (Source: `docs/memory/BUGS.md` L901-918)
- **B29 — Dual-flag soft delete** — `is_deleted` and `deleted_at` must both be updated. Relevant for user account status checks in authentication. (Source: `docs/memory/BUGS.md` L937-947)

## Conflict Warnings
- **Hard conflict: Custom CsrfFilter vs Spring Security CSRF** — The existing decision (D-CSRF-001) explicitly recommends replacing custom CSRF with Spring Security CSRF. This feature does exactly that. No conflict — the decision anticipates this migration.
- **Soft conflict: Session-based testing patterns** — Existing MockMvc filter tests use custom session patterns. After migration to Spring Security, tests must use Spring Security test annotations (`@WithMockUser`, `SecurityMockMvcRequestPostProcessors`). Plan must account for test migration.

## Retrieval Notes
- Index entries considered: 20 max (10 used)
- Source sections read: DECISIONS.md (targeted), BUGS.md (targeted), INDEX.md
- Budget status: within 900-word limit
