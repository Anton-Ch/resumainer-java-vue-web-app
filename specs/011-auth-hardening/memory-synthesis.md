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
- [S1] Status : Active — Phase 10 backend registration and email verification was implemented, verified, accepted, and committed as c1d7466 . Phase 11 Resend Verification is next. Milestones Phases 1-6: Spring Security bootstrap, auth schema, user details, JSON login/logout/status, failed-login protection, and SPA CSRF migration. (Source: `docs/memory/WORKLOG.md`)
- [S2] D37 | Checkpoint evidence standard : changed files + assertions + sample + audit | evidence ,checkpoint,quality,verification,testing,process,standard,best-practice | DECISIONS .md | active D38 | Do not mock the unit whose behavior is under test | testing ,mock,unit-test,tdd,best-practice,anti-pattern | DECISIONS .md | active D39 | Every bug fix must include a regression test that would fail on the previous implementation | bug-fix ,regression,testing,tdd,quality,process,best-practice | DECISIONS .md | active D40 | Separate implementation complete from contract proven | quality ,verification,contract,testing,evidence,process,best-practice | DECISIONS... (Source: `docs/memory/INDEX.md`)
- [S3] Status Active Why this is durable Phases 23-27 delivered the complete production PDF pipeline : Phase 23 : Download controller security fixes (Content-Disposition header injection fix , missing exists () check , SecurityException →500 bug ), public route rate limiter , timing hardening Phase 24 : Frontend export /finalize flow repair — DTO URL contract , disabled PDF buttons , absolute public link , duplicate navigation fix , error handling , double-click prevention Phase 25 : V12 .1 budget parity... (Source: `docs/memory/WORKLOG.md`)

## Related Historical Lessons
- [none]

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
