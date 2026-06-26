# Memory Synthesis

## Current Scope
- Feature: 010-admin-page-users
- Spec: Feature Specification: Admin Console Users and Resumes
- Feature folder: specs\010-admin-page-users
- Spec context: # Feature Specification : Admin Console Users and Resumes **Feature Branch **: `feat/010-admin-page-users` **Created**: 2026-06-26 **Status**: Draft **Input**: Admin console : Admin Home dashboard with all-resumes moderation table , Admin Users list...

## Relevant Project Context
- [none]

## Relevant Decisions
- [D1] The user experience MUST be consistent across all screens, languages, and interaction patterns. Every user-facing interaction follows the same rules. Internationalization : All user-facing strings MUST be externalized into resource files ( messages_en.properties , messages_ru.properties ) for both Thymeleaf (Landing Page) and Vue SPA. (Source: `.specify/memory/constitution.md`)
- [D2] Status Active Why this is durable Every feature that involves multi-section review/editing (generated resume, profile, admin forms) needs a safe way for frontend to send back edited field values. The naive approach (frontend constructs field paths) leads to format mismatches, SQL injection risks, and tight coupling to backend column names. Decision When the backend exposes editable fields in a review DTO, every field variant MUST include an opaque updateKey string. (Source: `docs/memory/DECISIONS.md`)
- [D3] Status Active Why this is durable Unit tests caught 0 of the 6 bugs found during manual testing of Feature 003. Bugs like missing Flyway bean, unresolved DataSource URL, unresponsive i18n validation messages, and duplicate toggle text were invisible to unit tests. They only appeared in the full Docker environment with actual PostgreSQL, Nginx, and browser interaction. (Source: `docs/memory/DECISIONS.md`)
- [D4] Status Active Why this is durable PrimeVue DataTable lazy mode sends sortField from Column field prop directly to server. The frontend uses camelCase field names (courseName, startDate) but the DB uses snake_case column names (name, start_date). Without explicit mapping, sort requests fail with IllegalArgumentException and data disappears when user clicks a column header to sort. (Source: `docs/memory/DECISIONS.md`)

## Active Architecture Constraints
- [none]

## Accepted Deviations
- [none]

## Relevant Security Constraints
- [S1] Status Active Why this is durable Fixing a bug without a regression test means the same bug can be reintroduced by a future change and go undetected. A regression test that would have passed before the fix (because it tested the wrong thing or was absent) provides no safety. The test must specifically fail on the old code and pass on the new code. (Source: `docs/memory/DECISIONS.md`)
- [S2] Security MUST be integrated into every feature from design through implementation. Security is not an afterthought. Password Storage : All passwords MUST be hashed using BCrypt. (Source: `.specify/memory/constitution.md`)

## Related Historical Lessons
- [B1] Status Active Symptoms A saved resume can be soft-deleted via the Home page (DELETE /api/resumes/{id}) but still be accessible via the public route GET /{username}/{publicCode}. The ResumeDao.softDelete() sets deleted_at = NOW() but does not set is_deleted = TRUE. ResumeDao list queries filter by deleted_at IS NULL (so the list correctly excludes deleted records), but SavedResumeDao public route queries filter by is_deleted = FALSE (so deleted records remain accessible via public URL). (Source: `docs/memory/BUGS.md`)

## Conflict Warnings
- [none]

## Retrieval Notes
- Index entries considered: 10
- Source sections read: 10
- Budget status: within limit
