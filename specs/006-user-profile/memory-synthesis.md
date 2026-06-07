# Memory Synthesis

## Current Scope
Feature 006 — User Profile Page (6 sections: Contact Details, Work Experience, Projects, Education, Courses, Additional Info). Backend REST APIs + PostgreSQL persistence alongside frontend Vue 3 + PrimeVue implementation.

## Relevant Decisions
- **D10: DAO connection-accepting overloads for JDBC transaction support** (Status: Active) — Profile sections require atomic multi-table saves (e.g., ContactDetails + user, or AdditionalInfo + user_work_format). DAOs must expose connection-accepting overloads for service-level transaction management.
- **D12: PrimeVue 4 Form with Zod resolver** (Status: Active) — Standard validation pattern for all Profile forms (Contact, AdditionalInfo, inline record forms). Uses zod for schema + vue-i18n for error messages.
- **D13: All user-facing strings must use i18n $t()** (Status: Active) — Critical for Profile. No hardcoded labels, placeholders, toasts, validation messages, status text.
- **D14: Mandatory manual integration testing phase** (Status: Active) — Applies after Profile implementation.
- **D17: PrimeVue DataTable lazy mode for server-paginated APIs** (Status: Active) — Required for Courses DataTable with up to 300 records. Lazy mode with server-side LIMIT/OFFSET.
- **D18: Independent block loading for resilient page architecture** (Status: Active) — Each profile section loads independently; failure in one section does not block others.
- **D20: PrimeVue 4 ToastService + ConfirmationService require app.use()** (Status: Active) — Needed for save success/error toasts and delete confirmations.
- **D21: PrimeVue 4 Tooltip is a global directive requiring explicit registration** (Status: Active) — For Courses table sort-header tooltips.
- **D15: Separate @Configuration for infrastructure beans via @ComponentScan** (Status: Active) — Profile DAOs, Services, Controllers registered via @Repository/@Service/@Controller + @ComponentScan.

## Active Architecture Constraints
- **A2: SPA under /app/ routing** (Source: ARCHITECTURE.md) — All SPA routes should be under `/app/*`. Profile routes are currently at `/profile/*` in prototype — must align with project routing convention (verify if `/app/profile/*` or standalone `/profile/*` is correct based on existing setup).

## Accepted Deviations
- No accepted deviations apply to this feature.

## Relevant Security Constraints
- Backend validation is authoritative — all Profile form submissions must be validated server-side even if frontend validates first.
- No secrets in logs or builds — applies to any profile data handling.
- Username validation: English letters/digits/underscores/hyphens only, no Cyrillic, no spaces. Unique enforced at DB level (UNIQUE constraint on `users.username`).

## Related Historical Lessons
- **Feature 005 User Home**: Established pattern for PrimeVue DataTable lazy mode + server pagination + independent block loading. Same pattern applies to Profile sections.
- **Feature 003 Auth**: Established i18n patterns, DAO/service/controller layers, and test patterns. Profile follows same architecture.
- **Feature 004 Connection Pool**: @ComponentScan pattern for bean discovery — Profile DAOs/services/controllers follow same registration.

## Conflict Warnings
- (none) — Routing verified: existing router uses `/profile/*` directly (aligned with prototype). Memory entry A2 about `/app/` prefix does not match current implementation — resolved as soft conflict, existing route pattern takes precedence.
- **Date of Birth NOT NULL**: Data dictionary requires DOB as NOT NULL, confirmed by user as required field. Must ensure frontend validation and DB constraint are aligned.

## Retrieval Notes
- Index entries considered: 20 (all in INDEX.md)
- Source sections read: BUGS.md, DECISIONS.md, ARCHITECTURE.md, WORKLOG.md (selected entries only)
- Max synthesis budget: 900 words (current: ~580)
- Optimizer: enabled (SQLite cache)
