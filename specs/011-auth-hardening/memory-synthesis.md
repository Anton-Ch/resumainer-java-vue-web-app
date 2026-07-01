# Memory Synthesis

## Current Scope
- Feature: 011-auth-hardening
- Spec: Feature Specification: Auth Hardening and Spring Security Migration
- Feature folder: specs\011-auth-hardening
- Spec context: # Feature Specification : Auth Hardening and Spring Security Migration **Feature Branch **: `feat/011-auth-hardening` **Created**: 2026-06-30 **Status**: Draft v0 .2 — review-corrected **Input**: Full authentication hardening before production deploy : migrate existing...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] Status Active Why this is durable Unit tests caught 0 of the 6 bugs found during manual testing of Feature 003. Bugs like missing Flyway bean, unresolved DataSource URL, unresponsive i18n validation messages, and duplicate toggle text were invisible to unit tests. They only appeared in the full Docker environment with actual PostgreSQL, Nginx, and browser interaction. (Source: `docs/memory/DECISIONS.md`)
- [D2] Status Active Why this is durable During manual testing, hardcoded English strings were found in AuthPage.vue (info panel text, subtitles) and in LoginForm/RegisterForm (Zod validation messages). These were not caught during implementation because they were &quot;invisible&quot; — the page looked correct in English, but switching to Russian revealed untranslated text. Every future feature with UI will have the same risk. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [V1] Status Active Why this is durable Every Vue SPA feature that accepts user input needs form validation. PrimeVue 4 introduced a new Form component with resolver-based validation. The pattern is non-obvious and differs from PrimeVue 3. (Source: `docs/memory/DECISIONS.md`)

## Relevant Security Constraints
- [S1] Status Active Why this is durable During the Feature 010 security review, the agent correctly avoided exposing raw file paths in DTOs but SQL SELECT columns loaded pdf_file_path and html_file_path into ResultSet rows (only used for null-check booleans). Security logs also exposed user.getEmail() instead of userId. These are subtle exposure surfaces that routine DTO reviews miss. (Source: `docs/memory/DECISIONS.md`)
- [S2] D37 | Checkpoint evidence standard : changed files + assertions + sample + audit | evidence ,checkpoint,quality,verification,testing,process,standard,best-practice | DECISIONS .md | active D38 | Do not mock the unit whose behavior is under test | testing ,mock,unit-test,tdd,best-practice,anti-pattern | DECISIONS .md | active D39 | Every bug fix must include a regression test that would fail on the previous implementation | bug-fix ,regression,testing,tdd,quality,process,best-practice | DECISIONS .md | active D40 | Separate implementation complete from contract proven | quality ,verification,contract,testing,evidence,process,best-practice | DECISIONS... (Source: `docs/memory/INDEX.md`)
- [S3] Status Active Why this is durable Every feature with form submissions needs CSRF protection. Without Spring Security, there is no built-in CSRF filter. This pattern must be reused for all future POST/PUT/DELETE endpoints. (Source: `docs/memory/DECISIONS.md`)

## Related Historical Lessons
- [B1] Status Active Symptoms POST, PUT, PATCH, or DELETE requests to any /api/* endpoint return 403 with: {&quot;error&quot;:&quot;Invalid or missing CSRF token&quot;}. The XSRF-TOKEN cookie is present in document.cookie, but the X-CSRF-Token header is missing from the request. Root Cause The backend CsrfFilter implements OWASP cookie-to-header pattern: it sets a non-HTTP-only cookie XSRF-TOKEN and validates the X-CSRF-Token header for unsafe methods (POST, PUT, PATCH, DELETE). (Source: `docs/memory/BUGS.md`)
- [B2] Status Active Symptoms After submitting a form (login/register), the API returns a non-2xx response (409, 401, etc.) with a descriptive error message. The request fails on the network level but the user sees NO error message on the page. The form just resets or stays unchanged with no feedback. (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
