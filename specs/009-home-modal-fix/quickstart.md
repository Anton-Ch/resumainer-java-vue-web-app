# Quickstart: Home Page Saved Resume Details Modal Fix

**Feature**: `009-home-modal-fix` | **Branch**: `feat009/home-page-modal-fix`

---

## Prerequisites

- Docker Desktop running
- JDK 21
- Node.js 20+
- Playwright browsers installed (`npx playwright install`)
- Docker Compose environment (PostgreSQL + backend + frontend)

---

## Backend targeted tests

Run targeted tests first. Adjust exact test class names only after inspecting the codebase.

```bash
cd backend
./mvnw test -Dtest="UserHomeControllerTest,ResumeControllerTest,PublicResumeControllerTest,GenerateResumeControllerTest,ResumeServiceTest,UserHomeServiceTest,PublicUrlServiceTest"
```

Notes:

- New Home modal DTO URLs MUST use canonical authenticated export endpoints from `GenerateResumeController`.
- `ResumeDownloadControllerTest` may still exist for legacy fallback, but it must not be the source of truth for new modal URLs.
- Public route tests must include active `200`, deleted `410`, invalid `404`, missing file `404`, unsafe path `404`, and route non-interception (`/api/**`, `/app/**`, `/static/**`, `/assets/**`, `/error/**`).

Then run the full backend test suite:

```bash
./mvnw test
```

---

## Frontend tests

```bash
cd frontend
npm test -- --run
npm run build
```

Required frontend coverage:

- row click opens modal for selected row;
- latest-resume card opens modal only when `summary.lastResume` exists;
- modal visibility uses reactive `v-model` bridge;
- canonical fields drive all modal actions;
- no old standalone `publicUrl` / `pdfUrl` usage remains;
- cover-letter preview/toggle/copy-full-text behavior;
- PDF unavailable state;
- HTML unavailable state;
- delete cancel keeps resume unchanged;
- delete confirm closes modal and reloads table+summary;
- delete failure keeps modal open and shows generic error toast;
- confirm delete button disables with loading state after first click.

---

## Grep/manual audit after implementation

```bash
grep -R "pdfUrl" frontend/src || true
grep -R "publicUrl" frontend/src || true
grep -R "pdf_file_path\|html_file_path" frontend/src || true
```

Manual classification rule:

Allowed:

- `publicUrlLink`

Forbidden old standalone fields:

- `publicUrl:`
- `.publicUrl`
- `{ publicUrl }`
- `pdfUrl:`
- `.pdfUrl`
- `{ pdfUrl }`

Raw path leaks forbidden anywhere in frontend/API DTOs:

- `pdf_file_path`
- `html_file_path`
- server-local storage directory paths

Do not treat every `grep` hit as automatic failure; classify output manually and document the result.

---

## Docker

```bash
docker compose up --build
```

Verify `.env.example` documents `APP_PUBLIC_BASE_URL`. In runtime, the backend must actually receive the environment variable through Docker Compose/container environment or existing config mechanism.

---

## Playwright MCP evidence

Before Playwright smoke testing, STOP and ask the user to:

1. provide throwaway local test credentials;
2. confirm a fresh generated saved resume exists for that test user;
3. confirm at least one resume has a cover letter and/or provide a second resume without cover letter if needed for empty-state testing.

Create evidence folder:

```bash
mkdir -p tempfiles/Playwright_evidence
```

Evidence saved under `tempfiles/Playwright_evidence/` — not committed.

Required Playwright evidence checklist:

1. Login as throwaway test user.
2. Home page loads.
3. Saved-resume table row hover shows pointer/hover highlight.
4. Row click opens modal for the selected row, not latest by mistake.
5. Close modal via close icon/Escape/backdrop and reopen another row.
6. Latest-resume card click opens the same modal for `summary.lastResume`.
7. Latest-resume card is not clickable when `summary.lastResume` is absent, if test data allows.
8. Modal shows saved resume creation date as generation date, vacancy title, company only if present, language, adaptation level, public URL text.
9. Copy public link shows success feedback; if clipboard read permission is unavailable, verify toast/no console error.
10. Open PDF opens new tab/inline PDF endpoint.
11. Download PDF triggers download request.
12. Download HTML triggers authenticated owner-only download request or is disabled/hidden consistently when unavailable.
13. Cover letter long text shows preview and expands; Copy cover letter copies full text, not preview.
14. No cover letter shows exact empty-state text.
15. Delete action opens confirmation.
16. Cancel delete keeps resume and public link working.
17. Confirm delete disables button with loading state, soft-deletes, closes modal, reloads table and summary.
18. Previously valid public link for deleted resume returns `410 Gone` backend Thymeleaf page with approved text.
19. Invalid public link still returns `404`.
20. Save screenshots/traces/log notes under `tempfiles/Playwright_evidence/`.

---

## Context7 documentation references

Use Context7 before implementing or debugging the related area:

- Vue 3 computed + `v-model` bridge
- PrimeVue Dialog `v-model:visible`
- PrimeVue DataTable row-click event payload and row styling
- Vue Test Utils + Vitest component testing
- Playwright locators, screenshots, downloads, multiple tabs, clipboard permissions
- Spring MVC controller route mapping / Thymeleaf error page behavior if public route or 410 page behavior is unclear

If a checkpoint fails:

1. capture full diagnostics;
2. identify failing layer;
3. consult Context7 docs for the relevant framework/library;
4. apply the smallest targeted fix;
5. rerun the exact failing command;
6. after 3 focused unsuccessful cycles on the same error, STOP and ask with full diagnostics.
