# Feature Specification: Home Page Saved Resume Details Modal Fix

**Feature Branch**: `feat009/home-page-modal-fix`

**Created**: 2026-06-24

**Status**: Draft — corrected after specification review

**Input**: Fix the Home page saved resume details modal end-to-end — repair table row click opening modal, latest-resume summary card opening modal, canonical DTO fields (`publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`), modal delete flow, public route returning `410 Gone` for soft-deleted resumes, and backend Thymeleaf `410` error page.

> **Instruction for DeepSeek / OpenCode**: This corrected spec supersedes the previous generated spec. If you agree with its decisions and they match the current codebase, copy them directly into the final Spec Kit documents. If anything is unclear, conflicts with the codebase, or you disagree with any requirement, STOP and ask for clarification before planning or coding. Do not silently reinterpret requirements.

---

## Clarifications

### Session 2026-06-24

- **Q: Delete error handling** — What should happen if the DELETE API call fails? → **A**: If confirm delete fails (network error, server error), modal stays open with the resume data unchanged, and a user-readable error toast is shown. The user may retry or close the modal manually.
- **Q: Double-click delete protection** — Should the confirm button be protected against rapid duplicate clicks? → **A**: Yes. The confirm button MUST be disabled and show a loading state (spinner) after the first click, preventing further clicks until the API responds. On success, modal closes. On error, button re-enables so user can retry.
- **Q: Cover letter display for long text** — How should the modal handle very long cover letter text? → **A**: Show a preview of ~150 characters with a "Show full cover letter" toggle button. When expanded, display full text with scroll. This keeps modal compact by default while making full text accessible on demand.
- **Q: Public route timing for 410 vs 404** — Should 410 have an artificial delay like 404 to prevent timing enumeration? → **A**: Yes. The same uniform artificial delay applied to 404 responses must also be applied to 410 responses so an attacker cannot distinguish "never existed" (404) from "was deleted" (410) by measuring response time.
- **Q: Long text truncation in modal metadata** — How should the modal handle very long resume title, vacancy, or company name text? → **A**: Allow word-wrap/break within the grid cell. Text stays single-line by default but wraps to multiple lines when needed. No hard truncation or ellipsis — full text is always readable without hover/tooltip dependency.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Open Saved Resume from Home Table (Priority: P1)

As a logged-in user, I want to click a saved resume row on the Home page and see details in a modal so that I can quickly access the generated PDF, public link, HTML download, and cover letter.

**Why this priority**: The modal is the primary mechanism for users to access their generated artifacts. Without it, users cannot view or share their resumes from Home.

**Independent Test**: Log in as a user with saved resumes, click a row, and verify the modal opens with correct metadata and actions.

**Acceptance Scenarios**:

1. **Given** the Home page has saved resumes, **When** the user hovers over a row, **Then** the row shows pointer cursor and hover highlight.
2. **Given** the user clicks any saved resume row, **When** data is loaded, **Then** the details modal opens for the selected resume.
3. **Given** the modal opens, **Then** it displays the selected resume, not the latest resume by mistake.
4. **Given** the modal is closed via close icon / Escape / backdrop click, **Then** the modal closes and parent state is synchronized.
5. **Given** the user reopens another row, **Then** the modal displays the newly selected resume data.
6. **Given** the table is empty, **Then** row-click behavior is not available and no modal opens.

---

### User Story 2 — Open Latest Resume from Summary Card (Priority: P1)

As a logged-in user, I want the "Latest resume" summary card to open details for the newest generated resume so that I can act on it immediately.

**Why this priority**: The summary card is the most prominent element on the Home page; if it does not open the modal, users cannot reach their latest generated output quickly.

**Independent Test**: Click the latest-resume card when `summary.lastResume` exists and verify the same modal opens for that resume.

**Acceptance Scenarios**:

1. **Given** `summary.lastResume` exists, **When** the user clicks the latest-resume card, **Then** the same `ResumeDetailsDialog` opens for that resume.
2. **Given** `summary.lastResume` is absent, **When** the user hovers/clicks the card, **Then** the card is not visually clickable and no action occurs.
3. **Given** the latest resume is soft-deleted via modal, **When** the table and summary reload, **Then** the card shows the next newest active resume or an empty state.
4. **Given** `summary.lastResume` is null while table rows exist, **Then** this is treated as API inconsistency; the card stays non-clickable and the table rows still open their own modals.

---

### User Story 3 — Use Modal Actions: PDF, Public Link, HTML Download (Priority: P1)

As a logged-in user, I want the modal to expose PDF actions (open in new tab, download), public link copy, and HTML download so that I can share or export my resume without leaving Home.

**Why this priority**: These actions are the core value of the modal — they replace the need to navigate to other pages for basic export tasks.

**Independent Test**: Open the modal and verify each action button works: Open PDF, Download PDF, Copy public link, Download HTML.

**Acceptance Scenarios**:

1. **Given** the modal is open, **Then** it displays: resume title, vacancy title, company name only if present, the saved resume creation date shown to the user as generation date, language, adaptation level, and public URL text. Long text values wrap within their grid cells and remain fully readable without truncation.
2. **Given** PDF is available, **When** the user clicks Open PDF, **Then** PDF opens in a new browser tab.
3. **Given** PDF is available, **When** the user clicks Download PDF, **Then** a PDF file download starts.
4. **Given** PDF is unavailable, **Then** PDF action buttons are disabled and a user-readable message explains why.
5. **Given** HTML download URL is available, **When** the user clicks Download HTML, **Then** an HTML file download starts through an authenticated owner-only endpoint.
6. **Given** HTML download URL is unavailable, **Then** the Download HTML action is disabled or hidden consistently with the existing Export page pattern and MUST NOT call an undefined URL.
7. **Given** the user clicks Copy public link, **Then** the full public URL is copied to clipboard and success feedback is shown.
8. **Given** the public link is displayed, **Then** it is a full absolute URL based on configured public base URL or request origin fallback.
9. **Given** action URLs are rendered in the modal, **Then** no raw filesystem paths or storage directories are exposed.

---

### User Story 4 — Cover Letter Display and Copy (Priority: P2)

As a logged-in user, I want to see and copy the cover letter from the modal when it was generated during resume creation.

**Why this priority**: Cover letter access adds value but is secondary to the core PDF/public-link actions.

**Independent Test**: Open modal for a resume with a cover letter and verify text and copy button exist; open modal for a resume without a cover letter and verify the exact empty-state text.

**Acceptance Scenarios**:

1. **Given** cover letter exists and is short (≤150 chars), **Then** the modal displays the full text with a Copy cover letter button.
2. **Given** cover letter exists and is long (>150 chars), **Then** the modal shows a preview (~150 chars) with a "Show full cover letter" toggle button and Copy cover letter button.
3. **Given** cover letter preview is shown, **When** the user clicks "Show full cover letter", **Then** the full text is displayed with scroll.
4. **Given** cover letter exists, **When** the user clicks Copy cover letter, **Then** the full cover letter text is copied to clipboard with success feedback, not the shortened preview.
5. **Given** cover letter does not exist, **Then** the modal displays the approved i18n empty-state text and no active Copy cover letter button.
6. **Given** clipboard API is unavailable, **Then** the UI shows a user-readable failure message and does not crash.

**Approved empty-state text**:

- RU: `Сопроводительное письмо не было выбрано в настройках генерации.`
- EN: `Cover letter was not selected in generation settings.`

---

### User Story 5 — Delete Saved Resume from Modal (Priority: P1)

As a logged-in user, I want to delete a saved resume from its details modal so that outdated or unwanted public links and artifacts are no longer visible or accessible.

**Why this priority**: The modal is the place where the user manages a saved resume. Delete must be end-to-end, because public link behavior after deletion depends on this flow.

**Independent Test**: Open a saved resume modal, click Delete, cancel once, then confirm delete and verify table, summary card, modal state, and old public link behavior.

**Acceptance Scenarios**:

1. **Given** the modal is open for a saved resume, **Then** a Delete resume action is available.
2. **Given** the user clicks Delete resume, **Then** a confirmation prompt/dialog appears before any backend deletion occurs.
3. **Given** the user cancels deletion, **Then** the resume remains unchanged, the modal remains usable, and the public link still works.
4. **Given** the user confirms deletion, **Then** the resume is soft-deleted, the modal closes, the table reloads, and the summary card reloads.
5. **Given** the deleted resume was the latest resume, **When** summary reloads, **Then** the latest-resume card shows the next newest active resume or an empty state.
6. **Given** the resume is soft-deleted, **When** its previously valid public link is opened, **Then** the public route returns HTTP `410 Gone` with the approved backend Thymeleaf error page.
7. **Given** confirm delete is triggered but the API call fails (network error, server error), **Then** the modal stays open with the resume data unchanged and a user-readable error toast is shown. The user may retry or close the modal manually.
8. **Given** the user clicks the confirm delete button, **Then** the button is immediately disabled and shows a loading state, preventing duplicate clicks until the API responds.

---

### User Story 6 — Public Link Behavior After Deletion (Priority: P2)

As a recruiter opening a shared resume link, I want to see a clear "resume removed" page instead of a generic error when the resume was deleted.

**Why this priority**: A proper `410 Gone` page improves recruiter experience and prevents confusion about broken links.

**Independent Test**: Soft-delete a resume, open its public link, and verify a `410 Gone` backend Thymeleaf page with clear explanatory text.

**Acceptance Scenarios**:

1. **Given** a public link points to an active saved resume, **When** opened, **Then** the route returns HTTP `200 OK` and opens PDF inline.
2. **Given** a public link has an invalid username or code, **When** opened, **Then** the route returns HTTP `404 Not Found`.
3. **Given** a saved resume is soft-deleted and its public code was previously valid, **When** opened, **Then** the route returns HTTP `410 Gone`.
4. **Given** HTTP `410` is returned, **Then** the user sees a backend Thymeleaf error page, not Vue SPA, with a clear message indicating the resume was removed.
5. **Given** path traversal or unsafe file path is attempted, **Then** the route returns HTTP `404` and leaks no metadata about the attempted path.
6. **Given** unrelated application routes such as `/api/**`, `/app/**`, `/static/**`, `/assets/**`, `/error/**`, or the landing page are requested, **Then** they are not intercepted by the public resume route.
7. **Given** an invalid public link returns `404` or a soft-deleted public link returns `410`, **Then** both responses have the same uniform artificial delay to prevent timing-based enumeration of valid vs deleted vs non-existent codes.

**Approved 410 page text**:

- RU: `Пользователь решил удалить данное резюме. Больше оно не доступно.`
- EN: `The user has removed this resume. It is no longer available.`

---

### User Story 7 — Public Base URL Configuration (Priority: P3)

As a developer or deployer, I want public links to adapt to local and production environments without code changes.

**Why this priority**: Configuration is important for deployment flexibility but does not affect core modal behavior.

**Independent Test**: Set `APP_PUBLIC_BASE_URL` to different values and verify generated public URLs match.

**Acceptance Scenarios**:

1. **Given** `.env.example` documents `APP_PUBLIC_BASE_URL`, **Then** deployers know where to set the value.
2. **Given** `APP_PUBLIC_BASE_URL` is configured, **When** a public URL is generated, **Then** it uses the configured base.
3. **Given** `APP_PUBLIC_BASE_URL` is absent, **When** a public URL is generated, **Then** the system falls back to the current request origin.
4. **Given** a trailing slash is present in `APP_PUBLIC_BASE_URL`, **Then** it is normalized to produce a clean URL with exactly one slash before `{username}`.
5. **Given** generated public URLs are displayed, **Then** no hardcoded `localhost`, VPS IP, or production domain exists in application code.

---

### Edge Cases

- No saved resumes exist — table is empty, latest card shows no last resume and is not clickable.
- `summary.lastResume` is null while table has rows — latest card is not clickable, table rows still work.
- Latest resume PDF is unavailable — modal opens, PDF buttons disabled with message.
- HTML download URL is unavailable — Download HTML action is disabled or hidden consistently with the existing Export page pattern and no undefined URL is called.
- Latest resume has no cover letter — exact approved empty-state text is shown.
- Cover letter is very long (>150 chars) — preview shown with toggle to expand full text.
- Company name is null or blank — company field is hidden.
- Public base URL has trailing slash — system normalizes it.
- `APP_PUBLIC_BASE_URL` is missing entirely — fallback to request origin.
- Clipboard API is unavailable — user-readable failure feedback is shown.
- Legacy test records with only old `publicUrl` / `pdfUrl` fields — acceptable that they do not work after this feature.
- Old standalone frontend references to `pdfUrl` / `publicUrl` remain after migration — targeted audit must catch them. `publicUrlLink` is allowed and must not be treated as old-field usage.
- Public route receives invalid username/code — return `404`, not `410`.
- Public route receives known soft-deleted public code — return `410`, not `404`.
- Public route returns `404` and `410` — both have same uniform artificial delay.
- Resume title or vacancy is very long (>100 chars) — text wraps within grid cell, stays fully readable.
- Public route receives unsafe path traversal attempt — return `404` and leak no metadata.
- Delete is cancelled — no DB update, no summary/table reload side effects required.
- Delete is confirmed and API succeeds — modal closes and both table and summary are refreshed.
- Delete is confirmed but API fails — modal stays open, error toast shown, resume data unchanged, button re-enables for retry.
- Delete confirm button is double-clicked — second click prevented by disabled+loading state after first click.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Modal visibility MUST use proper Vue 3 `v-model` reactive binding (computed get/set bridge). One-time `ref(props.visible)` copying is forbidden.
- **FR-002**: Saved Resumes table MUST open modal on row click. No extra "Details" column or button shall be added.
- **FR-003**: Table rows MUST visually communicate clickability via pointer cursor and hover highlight.
- **FR-004**: Latest-resume summary card MUST emit open action only when `lastResume` exists. Clickable visual style MUST apply only when `lastResume` is present.
- **FR-005**: Frontend MUST stop using old standalone `publicUrl` and `pdfUrl` fields. New canonical fields (`publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`, `pdfAvailable`, `pdfMessage`, `coverLetter`) MUST drive all modal actions. `publicUrlLink` is allowed and is not old-field usage.
- **FR-006**: Backend paginated saved-resume list and `summary.lastResume` MUST use the same DTO mapper returning canonical fields.
- **FR-007**: Backend MUST return full absolute `publicUrlLink` using configured `APP_PUBLIC_BASE_URL`, falling back to current request origin when absent.
- **FR-008**: `.env.example` MUST document `APP_PUBLIC_BASE_URL`. Backend MUST actually read this value from environment/config. Creating `.env.example` alone is not sufficient.
- **FR-009**: Backend MUST normalize trailing slash in `APP_PUBLIC_BASE_URL` to avoid duplicate slashes in generated public links.
- **FR-010**: Backend MUST NOT expose raw filesystem paths (`pdf_file_path`, `html_file_path`, storage directory) in API responses.
- **FR-011**: Modal PDF and HTML action URLs MUST use authenticated owner endpoints, not raw file paths. HTML download MUST be authenticated owner-only and MUST NOT be exposed through the public route.
- **FR-012**: If `coverLetter` is null or blank, modal MUST show exact approved i18n empty-state text and no active copy button. If cover letter exists and is short (≤150 chars), display full text. If longer, show ~150 char preview with a "Show full cover letter" toggle to expand. Copy cover letter MUST always copy the full cover letter text, even when the modal is showing only the preview.
- **FR-013**: If PDF is unavailable (`pdfAvailable=false`), modal MUST show user-readable status message and disable PDF actions.
- **FR-013-1**: If `htmlDownloadUrl` is unavailable, the Download HTML action MUST be disabled or hidden consistently with the existing Export page pattern and MUST NOT call an undefined URL.
- **FR-014**: Modal MUST include a Delete resume action.
- **FR-015**: Delete resume action MUST require confirmation before soft delete.
- **FR-016**: Cancel delete MUST keep the resume unchanged and keep the modal usable.
- **FR-017**: Confirm delete MUST soft-delete the resume, close modal, reload saved-resume table, and reload summary/latest card. If the API call fails, modal MUST stay open with resume data unchanged and a user-readable error toast MUST be shown. The confirm button MUST be disabled with loading state after the first click to prevent duplicate delete requests.
- **FR-018**: Soft delete MUST set both `is_deleted = true` and `deleted_at = CURRENT_TIMESTAMP` (or equivalent existing schema) so list filtering and public-route status are consistent.
- **FR-019**: Public route MUST distinguish between: active resume (→ HTTP `200`), soft-deleted known resume (→ HTTP `410 Gone`), invalid username/code (→ HTTP `404`), missing file (→ HTTP `404`), unsafe/path traversal (→ HTTP `404`). Both `404` and `410` responses MUST have the same uniform artificial delay to prevent timing-based enumeration of valid vs deleted codes. If an existing public-route artificial delay/rate-limiting mechanism exists, reuse it for both `404` and `410`. If no such mechanism exists, STOP and ask for clarification before inventing a new delay value or timing mechanism.
- **FR-020**: Soft-deleted public resume route MUST render a backend Thymeleaf error page (not Vue SPA) with HTTP `410` status and clear explanatory text in both EN and RU.
- **FR-021**: First inspect existing backend error pages. If `templates/error/410.html` does not exist, create it following the existing `404.html` / `500.html` style. This is a backend Thymeleaf responsibility, not a Vue SPA responsibility.
- **FR-022**: Public resume route MUST NOT intercept `/api/**`, `/app/**`, `/static/**`, `/assets/**`, `/error/**`, landing page routes, or Vue SPA assets.
- **FR-023**: All new visible text MUST use i18n (`messages.properties` / `messages_ru.properties` for backend, `en.json` / `ru.json` for Vue frontend). No hardcoded user-facing strings. This includes modal action labels, confirmation dialog text, delete loading/error states, copy success/failure feedback, cover-letter toggle labels, and all empty-state messages.
- **FR-024**: Tests MUST be updated. Old tests expecting HTTP `404` for deleted public resumes MUST be updated to HTTP `410`.
- **FR-025**: No database migration may be added unless schema inspection proves a required field is missing. If a migration appears necessary, STOP and ask for confirmation before creating it.
- **FR-026**: Modal metadata text fields (resume title, vacancy title, company name) MUST allow word-wrap within their grid cells. Long text MUST remain fully readable without truncation, ellipsis, or tooltip dependency.
- **FR-027**: Do not modify PDF renderer, PDF fitting engine, AI generation, prompt builder, parser, finalization pipeline, budget rules, or PDF resume templates. Creating/updating backend Thymeleaf error page `templates/error/410.html` is allowed and required if missing.

### Key Entities *(feature involves data)*

- **HomeSavedResumeDto** — A safe canonical DTO representing a saved resume for Home page display. Contains fields: resume title, vacancy title, company name (optional), language, adaptation level, saved resume creation date shown as generation date, public URL link, PDF open URL, PDF download URL, HTML download URL, PDF availability flag, PDF status/message, and cover letter. This DTO replaces prior frontend use of raw `SavedResume` entity fields and legacy `publicUrl`/`pdfUrl`.
- **PublicResumeLookupResult** — An internal backend model representing the result of a public resume lookup, with status values: ACTIVE (serve PDF), DELETED (return `410`), NOT_FOUND, MISSING_FILE, UNSAFE_PATH (return `404`). Enables proper HTTP status differentiation without leaking metadata.
- **Public base URL configuration** — `APP_PUBLIC_BASE_URL` controls the domain/base portion of generated public resume links. It may come from environment/config and falls back to request origin if not set.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can open the saved resume details modal by clicking any table row on the Home page. Measured by: row click → modal visible.
- **SC-002**: Users can open the same modal by clicking the latest-resume summary card when `lastResume` exists. Measured by: card click → modal visible.
- **SC-003**: Modal displays correct metadata (saved resume creation date shown as generation date, vacancy, company if present, language, adaptation level) for the selected resume. Measured by: visual inspection or Playwright snapshot.
- **SC-004**: Users can execute all modal actions (Open PDF, Download PDF, Copy public link, Copy full cover letter when available, Download HTML) when data is available. Measured by: each action produces expected result (new tab, download, clipboard feedback, full cover-letter clipboard feedback, download).
- **SC-005**: Modal correctly handles unavailable states: PDF unavailable (buttons disabled + message), no cover letter (exact empty-state text). Measured by: visual inspection and tests.
- **SC-006**: Users can delete a saved resume from the modal only after confirmation. Measured by: cancel keeps resume; confirm soft-deletes and refreshes Home state.
- **SC-007**: After confirmed deletion, the deleted resume disappears from Home table and latest card updates to next newest active resume or empty state. Measured by: UI test / Playwright evidence.
- **SC-008**: Public route returns correct HTTP statuses: `200` for active, `410` for soft-deleted, `404` for invalid/unsafe/missing-file. Measured by: backend test assertions.
- **SC-009**: Soft-deleted public links show a branded backend Thymeleaf `410` error page with bilingual explanatory text. Measured by: browser rendering the `410` page with correct text.
- **SC-010**: Public URLs are full absolute URLs using `APP_PUBLIC_BASE_URL` or request origin fallback, with no hardcoded domains in source code. Measured by: config tests and source audit.
- **SC-011**: No old standalone `publicUrl` or `pdfUrl` frontend field usage remains. `publicUrlLink` remains valid canonical usage. Measured by targeted grep/manual audit, not naive substring grep.
- **SC-012**: Public resume route does not intercept `/api/**`, `/app/**`, `/static/**`, `/assets/**`, `/error/**`, landing page routes, or Vue SPA assets. Measured by backend route tests.
- **SC-013**: Backend and frontend tests pass. Backend build succeeds. Frontend build succeeds. Measured by: targeted tests, `mvn test`, `npm test -- --run`, and `npm run build` exit code `0`.

---

## Constitution Alignment

This feature MUST comply with the ResumAIner Constitution principles:

| Principle | Impact on this feature |
|---|---|
| **I. Code Quality & Maintainability** | Use dedicated DTO instead of raw entity. Keep changes layered (controller/service/DAO). Do not modify PDF renderer, fitting engine, AI generation pipeline, finalization pipeline, budget rules, or PDF resume templates. Creating/updating backend Thymeleaf `410` error page is allowed because it is an error page, not a PDF resume template. |
| **II. Testing Excellence** | JUnit 5 + Mockito for backend tests. Frontend Vitest tests for modal, table row, summary card, delete behavior, and canonical fields. Update existing tests that expect `404` for deleted resumes to expect `410`. |
| **III. User Experience Consistency** | All new visible text through i18n (EN/RU). Modal design consistent with existing PrimeVue component patterns and project design tokens. Row hover/cursor feedback follows platform conventions. |
| **IV. Performance & Reliability** | No additional modal details API call is required. Modal loads data already present in parent. Public route uses existing PDF serving mechanism. Delete refreshes table and summary to avoid stale latest-card state. |
| **V. Security by Design** | No raw filesystem paths in API responses. HTML download is authenticated owner-only. Public route does not leak metadata for invalid username/code or unsafe paths. Path traversal returns `404` without information disclosure. Soft-deleted known public links return `410` intentionally. |

**Technology Constraint Check** (per Constitution Technology Stack):
- [x] Java 21, Spring MVC (no Spring Boot), Plain JDBC (no ORM)
- [x] PostgreSQL with Flyway migrations
- [x] Docker Compose for deployment
- [x] Dev + Prod Spring profiles

---

## Assumptions

- The Home page table (`SavedResumesTable`) and summary cards (`SummaryCards`) already exist and display resume data.
- The modal component (`ResumeDetailsDialog`) already exists but has a visibility binding bug (`ref(props.visible)` instead of computed bridge).
- The backend endpoints (`/api/resumes`, `/api/user/home`) already return saved-resume data that can be mapped to canonical Home DTO fields.
- The public route controller (`PublicResumeController`) already exists and serves PDFs for active public links.
- Soft-delete via `is_deleted` flag is already implemented; `deleted_at` timestamp update may be missing and must be verified.
- Legacy test database records that only have old `publicUrl` / `pdfUrl` fields may stop working correctly — this is acceptable per product decision.
- Do not assume a new database migration is unnecessary. Inspect schema first. If required fields are missing, STOP and ask before adding a migration.
- No modifications to PDF rendering, fitting engine, AI generation, prompt building, parser, finalization pipeline, budget rules, or PDF resume templates are needed.
- Creating/updating backend Thymeleaf error page `templates/error/410.html` is in scope if missing.
- Standard Vue 3 Composition API patterns with computed get/set bridge are the correct solution for the modal visibility bug.
- Playwright evidence is required later in tasks and must be saved under `tempfiles/Playwright_evidence/`, not committed.

---

## Brainstorm Log

### Session 2026-06-24

**Focus**: Edge case deep-dive for Home Page Saved Resume Details Modal Fix

**Questions asked and resolved**:

1. **Delete error handling** — If DELETE API call fails, modal stays open with resume data unchanged and error toast shown. User may retry or close.
2. **Double-click delete protection** — Confirm button disabled with loading state after first click, preventing duplicate requests.
3. **Cover letter long text** — Preview (~150 chars) with "Show full cover letter" toggle; full text on demand.
4. **Public route 410 timing** — Same uniform artificial delay applied to both 404 and 410 to prevent timing-based enumeration of valid vs deleted codes.
5. **Modal metadata text overflow** — Long text wraps within grid cells without truncation or ellipsis; full text always readable without tooltip dependency.

**Sections updated**:
- User Story 3 (HTML unavailable handling and saved-resume creation date wording)
- User Story 4 (cover letter expandable, copy-full-text rule, corrected numbering)
- User Story 5 (delete error handling, double-click guard)
- User Story 6 (410 timing delay)
- User Story 3 (long text wrapping)
- Functional Requirements (FR-012, FR-013-1, FR-017, FR-019, FR-023, FR-026, FR-027)
- Edge Cases (including HTML unavailable, copy/clipboard, delete failure, and long-text behavior)
- Clarifications section (5 entries)

**Result**: All identified edge cases resolved. Spec ready for planning.
