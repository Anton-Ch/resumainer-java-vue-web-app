# Memory Synthesis

## Current Scope
- Feature: 009-home-modal-fix
- Spec: Feature Specification: Home Page Saved Resume Details Modal Fix
- Feature folder: specs\009-home-modal-fix
- Spec context: # Feature Specification : Home Page Saved Resume Details Modal Fix **Feature Branch **: `feat009/home-page-modal-fix` **Created**: 2026-06-24 **Status**: Draft — corrected after specification review **Input**: Fix the Home page saved resume details...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] Status Active Why this is durable Unit tests caught 0 of the 6 bugs found during manual testing of Feature 003. Bugs like missing Flyway bean, unresolved DataSource URL, unresponsive i18n validation messages, and duplicate toggle text were invisible to unit tests. They only appeared in the full Docker environment with actual PostgreSQL, Nginx, and browser interaction. (Source: `docs/memory/DECISIONS.md`)
- [D2] Status Active Why this is durable Mocking the unit under test makes the test pass trivially without actually verifying real behavior. (Source: `docs/memory/DECISIONS.md`)
- [D3] Status Active Why this is durable During manual testing, hardcoded English strings were found in AuthPage.vue (info panel text, subtitles) and in LoginForm/RegisterForm (Zod validation messages). These were not caught during implementation because they were &quot;invisible&quot; — the page looked correct in English, but switching to Russian revealed untranslated text. Every future feature with UI will have the same risk. (Source: `docs/memory/DECISIONS.md`)
- [D4] Status Active Why this is durable When a user starts generation while another request is already processing, the backend must reject the new request. Returning HTTP 500 (Internal Server Error) is semantically wrong — it suggests a server fault when the real issue is a client-side conflict. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [V1] Status Active Why this is durable The frontend service generateResumeService.ts had methods downloadPdf(savedResumeId) , openPdf(savedResumeId) , downloadHtml(savedResumeId) that constructed URLs from a RESUME_BASE constant and the saved resume ID. Meanwhile the backend DTO SavedResumeExportDto already carried pdfDownloadUrl , pdfOpenUrl , htmlDownloadUrl , and publicUrlLink — fully resolved canonical URLs. The ID-based construction bypassed backend route changes, ignored the ?disposition=inline parameter, and created fragile coupling where any backend route rename would break the frontend. (Source: `docs/memory/DECISIONS.md`)

## Relevant Security Constraints
- [S1] Status Active Symptoms A public unauthenticated route (like GET /{username}/{publicCode} ) has multiple 404 conditions: invalid username, invalid code, deleted resume, disabled resume. Without a uniform delay, an attacker can measure response times to distinguish &quot;valid username but wrong code&quot; (~10ms DB query) from &quot;invalid username&quot; (~1ms immediate return). This leaks information about which usernames exist in the system. (Source: `docs/memory/BUGS.md`)
- [S2] Status Active Why this is durable Fixing a bug without a regression test means the same bug can be reintroduced by a future change and go undetected. A regression test that would have passed before the fix (because it tested the wrong thing or was absent) provides no safety. The test must specifically fail on the old code and pass on the new code. (Source: `docs/memory/DECISIONS.md`)
- [S3] Security MUST be integrated into every feature from design through implementation. Security is not an afterthought. Password Storage : All passwords MUST be hashed using BCrypt. (Source: `.specify/memory/constitution.md`)

## Related Historical Lessons
- [none]

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
