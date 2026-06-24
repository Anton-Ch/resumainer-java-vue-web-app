# Implementation Plan: Home Page Saved Resume Details Modal Fix

**Branch**: `feat009/home-page-modal-fix` | **Date**: 2026-06-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/009-home-modal-fix/spec.md`

> **Instruction for DeepSeek / OpenCode**: This corrected plan supersedes the previously generated plan. Copy these decisions directly into the final Spec Kit plan if they match the current codebase. If any item conflicts with actual code, STOP and ask before changing the architecture or inventing a workaround.

---

## Summary

Fix the Home page saved resume details modal end-to-end:

- repair row-click and latest-resume card modal triggers;
- adopt canonical Home DTO fields (`publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`, `pdfAvailable`, `pdfStatus`, `pdfMessage`, `coverLetter`);
- implement delete-from-modal flow with confirmation, loading guard, generic failure toast, table reload, and summary/latest-card reload;
- update public route behavior to return `410 Gone` for known soft-deleted public resumes;
- add backend Thymeleaf `410` error page if missing;
- support configurable public base URL through `APP_PUBLIC_BASE_URL` with safe fallback behavior.

This is a **repair feature**. Do not modify PDF renderer, PDF fitting engine, AI generation, prompt builder, parser, finalization pipeline, budget rules, or PDF resume templates.

---

## Technical Context

**Language/Version**: Java 21 (backend), TypeScript 5.x (frontend Vue 3)

**Primary Dependencies**:

- Backend: Spring MVC 6, Plain JDBC, Flyway, SLF4J+Logback, JUnit 5+Mockito
- Frontend: Vue 3 (Composition API), Vite, PrimeVue 4, vue-i18n, Vitest
- Infrastructure: Docker Compose with 3 containers (backend Tomcat, frontend Nginx, PostgreSQL)

**Storage**: PostgreSQL — `saved_resumes` table and related entities. Feature is read-heavy with one write path (soft-delete). No new tables or migrations unless schema inspection proves a required field is missing and the user confirms migration creation.

**Testing**:

- Backend: JUnit 5 + Mockito (standalone MockMvc for controllers, mocked DAOs/services where appropriate). Targeted tests for Home DTO mapping, public route status codes, delete consistency, URL builder config/fallback, canonical export URLs, and route non-interception.
- Frontend: Vitest + Vue Test Utils for modal visibility, table row click, summary card click, delete flow, canonical fields, unavailable states, cover-letter preview/copy behavior.
- E2E: Playwright MCP evidence under `tempfiles/Playwright_evidence/`. Evidence files are local-only and must not be committed.

**Target Platform**: Docker Compose deployment (local dev), VPS production behind possible reverse proxy.

**Project Type**: Web application (Java backend + Vue frontend SPA + Thymeleaf landing/error pages)

**Performance Goals**:

- Modal details use already-loaded parent data; do not add a new modal-details API endpoint for this feature.
- Delete flow triggers table reload + summary reload after successful delete.
- Public route uses existing PDF serving mechanism with minimal overhead.
- Existing public-route artificial delay must be reused for both `404` and `410`. If no such mechanism exists, STOP and ask before inventing a delay value or adding `Thread.sleep`.

**Constraints**:

- No modifications to PDF renderer, fitting engine, AI generation, prompt builder, parser, finalization pipeline, budget rules, or PDF resume templates (FR-027).
- Creating/updating `backend/src/main/resources/templates/error/410.html` is allowed and required if missing. This is a backend Thymeleaf error page, not a PDF resume template and not Vue SPA work.
- No database migration unless schema inspection proves a required field is missing (FR-025). If a migration appears necessary, STOP and ask for confirmation before creating it.
- Soft-deleted public routes return `410 Gone` (not `404`) for known previously valid public links; existing tests expecting `404` for deleted public resumes must be updated.
- Same uniform artificial delay for both `404` and `410` public-route error responses (per FR-019 + B28 guardrail).
- All new visible text through i18n (FR-023).
- Use canonical authenticated export endpoints from `GenerateResumeController`; do not drive new UI/DTO URLs through deprecated `ResumeDownloadController` legacy routes.

**Scale/Scope**: Single feature — affects roughly 10 production files plus tests, i18n resources, backend error templates, `.env.example`, and documentation. 27 FRs, 7 user stories. No new Vue pages and no new modal-details API endpoint.

---

## Canonical Endpoint Decision

New Home modal actions MUST use the same canonical authenticated export endpoints as the current Export page flow:

```text
GET /api/generate/resumes/{id}/pdf?disposition=inline   # Open PDF in new tab
GET /api/generate/resumes/{id}/pdf                       # Download PDF
GET /api/generate/resumes/{id}/html                      # Download HTML, owner-only
```

`ResumeDownloadController` is treated as deprecated legacy fallback and MUST NOT drive new `HomeSavedResumeDto` URL generation or frontend modal actions. If the current codebase differs, STOP and report the exact current routes before changing contracts.

---

## Public URL Resolution Decision

Create or update a small backend URL builder/service (for example `PublicUrlService`) that returns full absolute public URLs for `publicUrlLink`.

Resolution order:

1. Use `APP_PUBLIC_BASE_URL` when non-blank.
2. If the project already has an existing application property mechanism for environment-backed settings, support that existing property path too. Do not add a new dotenv library.
3. If config is absent, use reverse-proxy forwarded headers when present: `X-Forwarded-Proto`, `X-Forwarded-Host`, and optionally `X-Forwarded-Port`.
4. If forwarded headers are absent, use request scheme + host + port as local-development fallback.
5. Log a warning when fallback origin is used because `APP_PUBLIC_BASE_URL` is not configured.

Normalize trailing slash from the base URL before appending `/{username}/{publicCode}` so the result never contains double slashes.

`.env.example` must document `APP_PUBLIC_BASE_URL`, but creating `.env.example` is not enough: the backend must actually read the value from environment/config at runtime.

---

## Security Review Findings to Implement

The plan-level security review identified three findings. These are mandatory implementation guardrails and must be reflected in tasks.

### SEC-001 — 410 intentional information disclosure risk

Product decision accepts `410 Gone` for known deleted public links to improve recruiter UX. Mitigations still required:

- `410.html` body must be static/branded and must not include dynamic resume data, username, public code, deletion date, saved resume ID, file path, company/vacancy, or filename.
- `410` and `404` error responses must use the same uniform artificial delay.
- Align error-response headers where practical: `Content-Type`, `Cache-Control`, and no metadata-revealing headers.
- Add backend tests verifying deleted public response contains only approved page text and no dynamic identifiers.

### SEC-002 — Public URL behind reverse proxy

- `PublicUrlService` must support forwarded headers in fallback mode.
- Log a warning when fallback request origin is used.
- Add tests for configured `APP_PUBLIC_BASE_URL`, trailing slash normalization, forwarded header fallback, and request-origin fallback.
- Do not hardcode `localhost`, VPS IP, or production domain in source code.

### SEC-003 — Delete error enumeration

- Delete failures shown to the frontend must use a generic user-facing message, for example: `Failed to delete resume.` / `Не удалось удалить резюме.`
- Backend may log the detailed server-side reason, but the response and frontend toast must not distinguish non-owned ID vs non-existent ID vs DB failure.
- Add tests proving non-owned and non-existent delete attempts return the same public response shape/status/message.

---

## Constitution Check

*GATE: Must pass before proceeding to task breakdown. Re-check if design changes.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | ✅ Pass | Dedicated DTO instead of raw entity. Layered changes (controller/service/DAO). No modifications to PDF/AI/generation pipeline. Thymeleaf 410 error page follows existing backend error-page pattern. No ORM, no Spring Boot. |
| **II. Testing Excellence** | ✅ Pass | JUnit 5 + Mockito for backend. Vitest for frontend. Existing tests expecting 404 for deleted public resumes updated to 410. Playwright MCP evidence required. No real AI calls. |
| **III. User Experience Consistency** | ✅ Pass | i18n for all new text (EN/RU). Modal uses existing PrimeVue components and project design tokens. Row cursor/hover feedback follows platform conventions. PRG pattern not applicable because modal actions are API calls, not form submissions. |
| **IV. Performance & Reliability** | ✅ Pass | No modal-details API call. PreparedStatement in existing DAOs. JDBC transaction for delete if multi-step. Delete refreshes table+summary for consistency. Existing delay mechanism reused rather than invented. |
| **V. Security by Design** | ✅ Pass | No raw filesystem paths in API responses. HTML download is authenticated owner-only. Public route 404/410 share uniform delay. No metadata leak on invalid paths. `is_deleted` + `deleted_at` consistency. Generic delete failure messaging. |

**Gate result**: ✅ ALL PRINCIPLES PASS — no violations to justify.

---

## Project Structure

### Documentation (this feature)

```text
specs/009-home-modal-fix/
├── plan.md              # This file
├── research.md          # Technology research and decision records
├── data-model.md        # Data model for Home DTO and public route status
├── quickstart.md        # Developer quickstart for this feature
├── contracts/
│   └── api-contracts.md # API contracts and DTO shape
├── memory-synthesis.md  # Memory context for planning
├── checklists/
│   └── requirements.md  # Quality checklist
└── spec.md              # Feature specification
```

### Source Code (repository root)

```text
# Backend files involved (expected; inspect actual code before editing):
backend/src/main/java/com/resumainer/
├── controller/
│   ├── ResumeController.java          # Update list response to use HomeSavedResumeDto if this is the actual /api/resumes owner
│   ├── UserHomeController.java        # Update summary.lastResume to use HomeSavedResumeDto
│   ├── PublicResumeController.java    # Add 410 support, route guard, uniform delay reuse
│   └── GenerateResumeController.java  # Verify canonical authenticated PDF/HTML endpoints used by DTO URLs
├── dto/
│   └── home/
│       └── HomeSavedResumeDto.java    # NEW or equivalent: canonical Home DTO
├── service/
│   ├── ResumeService.java             # Map canonical DTO for paginated list
│   ├── UserHomeService.java           # Map canonical DTO for summary.lastResume
│   └── PublicUrlService.java          # NEW or equivalent: public URL builder
├── model/
│   └── PublicResumeLookupResult.java  # NEW or equivalent: public route lookup result statuses
└── dao/
    └── ResumeDao.java                 # Verify soft-delete sets is_deleted + deleted_at and list excludes deleted rows

# Frontend files involved:
frontend/src/
├── components/home/
│   ├── ResumeDetailsDialog.vue        # FIX: v-model bridge, canonical fields, actions, cover-letter preview, delete flow
│   ├── SavedResumesTable.vue          # ADD/FIX: row click, pointer/hover styling
│   └── SummaryCards.vue               # FIX: conditional clickable styling
├── composables/
│   └── useUserHome.ts                 # UPDATE: modal state, delete flow, table+summary reload
├── services/
│   ├── userHomeService.ts             # UPDATE: canonical SavedResumeData fields
│   └── resumeService.ts               # UPDATE: delete error handling if delete API wrapper lives here
├── i18n/
│   ├── en.json                        # ADD: new modal/delete/copy/toggle/error keys
│   └── ru.json                        # ADD: new modal/delete/copy/toggle/error keys
└── views/
    └── UserHomePage.vue               # VERIFY: passes selected row/latest card data correctly to modal

# Backend templates/resources:
backend/src/main/resources/
├── templates/error/
│   └── 410.html                       # NEW if missing: backend Thymeleaf 410 error page
└── messages*.properties               # ADD: 410 page i18n keys if template uses backend messages

# Root/config:
.env.example                           # ADD: APP_PUBLIC_BASE_URL documentation
```

**Structure Decision**: Existing monorepo with `backend/` and `frontend/` — no structural changes needed.

---

## Complexity Tracking

> No Constitution violations to justify — this section is intentionally empty.
