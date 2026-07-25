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

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [none]

## Relevant Security Constraints
- [S1] D37 | Checkpoint evidence standard : changed files + assertions + sample + audit | evidence ,checkpoint,quality,verification,testing,process,standard,best-practice | DECISIONS .md | active D38 | Do not mock the unit whose behavior is under test | testing ,mock,unit-test,tdd,best-practice,anti-pattern | DECISIONS .md | active D39 | Every bug fix must include a regression test that would fail on the previous implementation | bug-fix ,regression,testing,tdd,quality,process,best-practice | DECISIONS .md | active D40 | Separate implementation complete from contract proven | quality ,verification,contract,testing,evidence,process,best-practice | DECISIONS... (Source: `docs/memory/INDEX.md`)
- [S2] Status Active Why this is durable Every feature with form submissions needs CSRF protection. Without Spring Security, there is no built-in CSRF filter. This pattern must be reused for all future POST/PUT/DELETE endpoints. (Source: `docs/memory/DECISIONS.md`)
- [S3] DECISIONS / Technical Decisions (`docs/memory/`) / Template / 2026-06-30 - SPA CSRF with Spring Security 6.5 requires explicit bootstrap (Source: `docs/memory/DECISIONS.md`)

## Related Historical Lessons
- [B1] Status Active Symptoms After submitting a form (login/register), the API returns a non-2xx response (409, 401, etc.) with a descriptive error message. The request fails on the network level but the user sees NO error message on the page. The form just resets or stays unchanged with no feedback. (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
