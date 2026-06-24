---
description: "Task breakdown for Home Page Saved Resume Details Modal Fix"
---

# Draft Tasks: Home Page Saved Resume Details Modal Fix

**Draft file**: `draft_tasks.md`  
**Feature Branch**: `feat009/home-page-modal-fix`  
**Input**: `draft_spec.md`, `draft_plan.md`, current `main` branch  
**Target implementer**: OpenCode + DeepSeek V4 Flash  
**Status**: Draft for conversion into final Spec Kit `tasks.md`

---

## 0. Instruction to DeepSeek / OpenCode

This task list is a draft prepared for strict execution.

If you agree with a task and it matches the current codebase, copy it directly into final `tasks.md`.  
If anything is unclear, unsupported by current code, inconsistent with existing architecture, or you disagree with it, **STOP immediately and ask the user**.

Do not proceed through stop points without user confirmation.

Evidence-first rule:

- Every logical group must produce proof.
- Proof must include commands, outputs, screenshots/traces where relevant, and changed file list.
- Playwright MCP evidence must be stored under `tempfiles/Playwright_evidence/`.
- Do not commit evidence or credentials.

Diagnostic rule for failures:

1. Capture full error output.
2. Identify failing layer: backend / frontend / Playwright / environment.
3. Consult Context7 docs for the relevant library/framework.
4. Apply the smallest targeted fix.
5. Re-run the exact failing command.
6. Repeat until solved.
7. If the same error persists after 3 focused cycles, STOP and ask the user with full diagnostics.

---

## Task Markers

| Marker | Meaning |
|---|---|
| `[TDD]` | Write/update failing test first, then implement |
| `[CTX7]` | Use Context7 MCP documentation before/while implementing |
| `[PW]` | Use Playwright MCP |
| `[SEC]` | Security-sensitive |
| `[STOP]` | Stop and ask user to review/confirm before continuing |
| `[EVIDENCE]` | Must collect and report concrete evidence |
| `[NO-PDF-ENGINE]` | Must not modify PDF renderer/fitting/finalization internals |

---

## Phase 0 — Context Loading and Baseline Inspection

**Goal**: Confirm current behavior before changing code.

- [ ] T001 [CTX7] Read/refresh Vue 3 Composition API docs for `computed`, props, emits, and custom `v-model` patterns using Context7.
- [ ] T002 [CTX7] Read/refresh PrimeVue Dialog docs for `v-model:visible` using Context7.
- [ ] T003 [CTX7] Read/refresh PrimeVue DataTable row click/styling docs using Context7.
- [ ] T004 [CTX7] Read/refresh Vue Test Utils / Vitest component testing docs using Context7.
- [ ] T005 [CTX7] Read/refresh Playwright locator, screenshot, trace, download, popup/new-tab, and clipboard docs using Context7.
- [ ] T006 Inspect `frontend/src/components/home/ResumeDetailsDialog.vue`; confirm whether it uses `ref(props.visible)` or another broken one-time prop copy.
- [ ] T007 Inspect `frontend/src/views/UserHomePage.vue`; confirm table and summary card both route into `openResumeModal`.
- [ ] T008 Inspect `frontend/src/components/home/SavedResumesTable.vue`; confirm row-click behavior and current row styling.
- [ ] T009 Inspect `frontend/src/components/home/SummaryCards.vue`; confirm latest card click behavior and whether clickable styling is conditional.
- [ ] T010 Inspect `frontend/src/services/userHomeService.ts`; document current `SavedResumeData` fields.
- [ ] T011 Inspect backend list/summary APIs: `ResumeController`, `UserHomeController`, `ResumeService`, `UserHomeService`, DAO methods.
- [ ] T012 Inspect current public route: `PublicResumeController` and related DAO/service lookup methods.
- [ ] T013 Inspect current delete behavior; determine whether both `is_deleted` and `deleted_at` are updated.
- [ ] T014 Inspect existing error pages. Confirm that `404.html` and `500.html` exist. Check whether `templates/error/410.html` exists.
- [ ] T015 Inspect current config mechanism and decide how backend can read `APP_PUBLIC_BASE_URL`. Do not assume `.env` is automatically read in plain Spring MVC.
- [ ] T016 Inspect `.env.example` and `.gitignore`; document whether `APP_PUBLIC_BASE_URL` and `tempfiles/` are present.
- [ ] T017 Run grep baseline:

```bash
grep -R "pdfUrl" frontend/src || true
grep -R "publicUrl" frontend/src || true
grep -R "pdf_file_path\|html_file_path" frontend/src || true
```

- [ ] T018 [STOP] [EVIDENCE] Report baseline findings to the user:
  - modal visibility bug location;
  - current DTO fields;
  - current old field usage;
  - current public route deleted behavior;
  - whether 410 page exists;
  - whether public base URL config exists;
  - whether evidence folder is ignored by git.
  Wait for user confirmation before coding.

---

## Phase 1 — Backend Contract, Config, and Public Route

**Goal**: Backend returns safe canonical Home DTOs and correct public route statuses.

### 1.1 Public base URL config

- [ ] T019 [TDD] Add tests for public URL builder/config behavior:
  - uses configured `APP_PUBLIC_BASE_URL`;
  - trims trailing slash;
  - falls back to request origin when config missing;
  - never hardcodes localhost or production domain.
- [ ] T020 Add `.env.example` entry:

```env
APP_PUBLIC_BASE_URL=http://localhost:8080
```

- [ ] T021 Ensure `.env` is ignored if present.
- [ ] T022 Implement actual backend reading of `APP_PUBLIC_BASE_URL` via existing config system or `System.getenv`.
- [ ] T023 Implement public URL normalization and builder:
  - `{baseUrl}/{username}/{publicCode}`;
  - no double slash;
  - no raw paths.

### 1.2 Home safe DTO

- [ ] T024 [TDD] Add backend tests for canonical Home saved resume DTO shape.
- [ ] T025 Create or update dedicated safe DTO, recommended name:

```text
backend/src/main/java/com/resumainer/dto/home/HomeSavedResumeDto.java
```

- [ ] T026 Add fields:
  - `id`
  - `resumeTitle`
  - `vacancyTitle`
  - `companyName`
  - `languageCode` or existing language representation
  - `languageName` if available
  - `adaptationLevel`
  - `createdAt`
  - `publicUrlLink`
  - `pdfOpenUrl`
  - `pdfDownloadUrl`
  - `htmlDownloadUrl`
  - `pdfAvailable`
  - `pdfStatus`
  - `pdfMessage`
  - `coverLetter`
- [ ] T027 Implement shared mapper used by both paginated list and summary latest resume.
- [ ] T028 Update `/api/resumes` response to use the safe DTO or equivalent project pattern.
- [ ] T029 Update `summary.lastResume` to use the exact same DTO/mapper.
- [ ] T030 [SEC] Ensure response never includes:
  - `pdf_file_path`;
  - `html_file_path`;
  - local filesystem path;
  - raw storage directory.

### 1.3 Soft delete consistency

- [ ] T031 [TDD] Add/update delete test: successful delete sets both `is_deleted=true` and `deleted_at`.
- [ ] T032 Update DAO/service delete implementation to set both values.
- [ ] T033 Ensure Home list excludes deleted resumes.
- [ ] T034 Ensure `summary.lastResume` excludes deleted resumes.

### 1.4 Public route status model

- [ ] T035 [TDD] Add/update public route tests:
  - active public resume -> `200 OK`;
  - invalid username/code -> `404`;
  - missing physical PDF -> `404`;
  - unsafe/path traversal -> `404`;
  - soft-deleted known public resume -> `410 Gone`.
- [ ] T036 Replace null-only public lookup with status-aware result, for example:
  - `ACTIVE`;
  - `DELETED`;
  - `NOT_FOUND`;
  - `MISSING_FILE`;
  - `UNSAFE_PATH`.
- [ ] T037 Update `PublicResumeController`:
  - active -> serve PDF inline;
  - deleted -> return/render `410 Gone`;
  - invalid/missing/unsafe -> `404`.
- [ ] T038 [SEC] Do not leak metadata for invalid username/code.

### 1.5 Thymeleaf 410 page

- [ ] T039 Inspect existing `templates/error/404.html` and `templates/error/500.html`.
- [ ] T040 If `templates/error/410.html` is missing, create it by following existing style. This is backend/Thymeleaf responsibility, not Vue.
- [ ] T041 Add i18n messages to `messages.properties` and `messages_ru.properties`:
  - `error.410.title`
  - `error.410.message`
  - `error.410.hint`
  - `error.410.cta`
- [ ] T042 Ensure deleted public route renders or forwards to Thymeleaf 410 page with HTTP status 410.

### 1.6 Backend evidence

- [ ] T043 Run targeted backend tests, adjusted to actual project layout:

```bash
cd backend
./mvnw test -Dtest="UserHomeControllerTest,ResumeControllerTest,PublicResumeControllerTest,ResumeDownloadControllerTest"
```

- [ ] T044 Run broader backend tests if targeted pass:

```bash
cd backend
./mvnw test
```

- [ ] T045 [EVIDENCE] Save sanitized API sample responses locally under:

```text
tempfiles/Playwright_evidence/backend_api_samples/
```

Do not include secrets or real PII.

- [ ] T046 [STOP] [EVIDENCE] Report backend checkpoint:
  - changed files;
  - DTO fields;
  - public URL config implementation;
  - sample `/api/resumes` response with sensitive content redacted;
  - sample `summary.lastResume` response with sensitive content redacted;
  - `410 Gone` test proof;
  - invalid `404` test proof;
  - delete consistency proof;
  - no raw path proof.
  Wait for user confirmation.

---

## Phase 2 — Frontend DTO, Modal, Table, Summary Card

**Goal**: Home page UI uses canonical fields and opens modal from row/card.

### 2.1 Frontend service/types

- [ ] T047 [TDD] Update/add frontend tests expecting canonical saved resume fields.
- [ ] T048 Update `frontend/src/services/userHomeService.ts`:
  - remove old `publicUrl`;
  - remove old `pdfUrl`;
  - add `publicUrlLink`;
  - add `pdfOpenUrl`;
  - add `pdfDownloadUrl`;
  - add `htmlDownloadUrl`;
  - add `pdfAvailable`;
  - add `pdfStatus`;
  - add `pdfMessage`;
  - add `coverLetter`.
- [ ] T049 Update all consuming TypeScript code to compile with new fields.

### 2.2 ResumeDetailsDialog

- [ ] T050 [TDD] Add/update `ResumeDetailsDialog` test: parent sets visible true -> Dialog opens.
- [ ] T051 [TDD] Add/update test: close emits `update:visible=false`.
- [ ] T052 [CTX7] Confirm Vue 3 custom `v-model` computed bridge pattern through Context7 if uncertain.
- [ ] T053 Fix modal visibility binding. Use computed get/set bridge. Do not use `ref(props.visible)`.
- [ ] T054 Update modal content:
  - metadata block: date, vacancy title, company if present, language, adaptation level;
  - public link text;
  - Copy public link button;
  - Open PDF button;
  - Download PDF button;
  - Download HTML button;
  - cover letter block;
  - Copy cover letter button when cover letter exists;
  - empty-state text when cover letter absent;
  - PDF unavailable message/status and disabled PDF buttons.
- [ ] T055 Ensure modal uses project design tokens, existing colors, spacing, and PrimeVue components consistently.
- [ ] T056 Ensure modal does not expose raw paths.
- [ ] T057 If clipboard API is used, handle failure gracefully and show user-readable feedback.

### 2.3 SavedResumesTable row click and styling

- [ ] T058 [TDD] Add/update test: row click emits/open selected resume.
- [ ] T059 [TDD] Add/update test: no Details button/column is required.
- [ ] T060 [CTX7] If DataTable row styling is unclear, check PrimeVue DataTable docs through Context7.
- [ ] T061 Add pointer cursor and hover highlight for clickable rows.
- [ ] T062 Ensure no extra Details column/button is added.

### 2.4 SummaryCards latest resume behavior

- [ ] T063 [TDD] Add/update test: latest card emits `openLastResume` only when `lastResume` exists.
- [ ] T064 [TDD] Add/update test: latest card is not visually clickable when `lastResume` is null.
- [ ] T065 Update `SummaryCards.vue` so clickable styling applies only when `lastResume` exists.
- [ ] T066 Ensure `UserHomePage.vue` still passes `summary.lastResume` into `openResumeModal`.

### 2.5 Delete UI refresh

- [ ] T067 [TDD] Add/update test: after delete success, modal closes and Home reload functions are called.
- [ ] T068 Update frontend delete flow so after successful delete:
  - modal closes;
  - table reloads;
  - summary reloads;
  - latest card updates to next active resume or empty state.

### 2.6 i18n

- [ ] T069 Add RU and EN i18n keys for all new visible strings:
  - Copy public link;
  - Copy cover letter;
  - Open PDF;
  - Download PDF;
  - Download HTML;
  - PDF unavailable;
  - cover letter empty state;
  - generation date;
  - language;
  - adaptation level;
  - company;
  - vacancy;
  - copied success/failure feedback if used.
- [ ] T070 Ensure no hardcoded visible user-facing strings remain in modified Vue components.

### 2.7 Frontend evidence

- [ ] T071 Run frontend tests:

```bash
cd frontend
npm test -- --run
```

- [ ] T072 Run frontend build:

```bash
cd frontend
npm run build
```

- [ ] T073 Run grep audit:

```bash
grep -R "pdfUrl" frontend/src || true
grep -R "publicUrl[^L]" frontend/src || true
grep -R "pdf_file_path\|html_file_path" frontend/src || true
```

Expected:
- no `pdfUrl`;
- no old `publicUrl`;
- no raw file path fields in frontend.

- [ ] T074 [STOP] [EVIDENCE] Report frontend checkpoint:
  - changed files;
  - modal v-model fix;
  - table row behavior;
  - summary card behavior;
  - i18n keys;
  - tests/build output;
  - grep audit output.
  Wait for user confirmation.

---

## Phase 3 — Playwright MCP End-to-End Verification

**Goal**: Prove actual UI behavior with real local app and generated resume.

### 3.1 Prepare evidence folder

- [ ] T075 Create local evidence folder:

```bash
mkdir -p tempfiles/Playwright_evidence
```

- [ ] T076 Confirm `tempfiles/` is ignored by git. If not, update `.gitignore`.

### 3.2 Manual prerequisite stop

- [ ] T077 [STOP] Ask the user to manually generate a new resume for a throwaway/local test user.
- [ ] T078 [STOP] Ask the user to provide:
  - local app base URL;
  - throwaway/local test login;
  - throwaway/local test password;
  - whether the generated test resume includes cover letter.
- [ ] T079 Do not write the credentials into files. Do not commit credentials.

### 3.3 Playwright scenario

- [ ] T080 [PW] Log in as the throwaway/local test user.
- [ ] T081 [PW] Open Home page.
- [ ] T082 [PW] Verify saved resumes table includes the newly generated resume.
- [ ] T083 [PW] Hover over table row; capture screenshot showing hover/pointer visual if possible.
- [ ] T084 [PW] Click table row; verify modal opens.
- [ ] T085 [PW] Capture screenshot:

```text
tempfiles/Playwright_evidence/01_table_row_modal_open.png
```

- [ ] T086 [PW] Verify modal metadata:
  - vacancy title;
  - company if present;
  - generation date;
  - language;
  - adaptation level.
- [ ] T087 [PW] Verify visible public URL text.
- [ ] T088 [PW] Click Copy public link.
- [ ] T089 [PW] Verify success feedback or clipboard content if available.
- [ ] T090 [PW] Click Open PDF.
- [ ] T091 [PW] Verify new tab/popup or inline PDF route response.
- [ ] T092 [PW] Capture screenshot or trace:

```text
tempfiles/Playwright_evidence/02_open_pdf.png
```

- [ ] T093 [PW] Click Download PDF and verify download event/request.
- [ ] T094 [PW] Click Download HTML and verify download event/request.
- [ ] T095 [PW] If cover letter exists:
  - verify cover letter text visible;
  - verify Copy cover letter button visible;
  - click Copy cover letter;
  - verify success feedback or clipboard content.
- [ ] T096 [PW] If cover letter does not exist:
  - verify exact empty-state text:
    `Сопроводительное письмо не было выбрано в настройках генерации.`
- [ ] T097 [PW] Close modal.
- [ ] T098 [PW] Click latest-resume summary card.
- [ ] T099 [PW] Verify same modal opens for latest resume.
- [ ] T100 [PW] Capture screenshot:

```text
tempfiles/Playwright_evidence/03_latest_card_modal_open.png
```

### 3.4 Delete and 410 verification

- [ ] T101 [PW] Copy/store the public URL locally for the test session only.
- [ ] T102 [PW] Open modal delete action.
- [ ] T103 [PW] Verify confirmation dialog appears.
- [ ] T104 [PW] Cancel delete first; verify resume remains.
- [ ] T105 [PW] Open delete action again and confirm delete.
- [ ] T106 [PW] Verify modal closes.
- [ ] T107 [PW] Verify table refreshes and deleted resume is gone.
- [ ] T108 [PW] Verify summary latest card refreshes to next active resume or empty state.
- [ ] T109 [PW] Open previously copied public URL.
- [ ] T110 [PW] Verify HTTP status is `410 Gone`.
- [ ] T111 [PW] Verify Thymeleaf 410 page is shown, not Vue SPA.
- [ ] T112 [PW] Verify text:
  `Пользователь решил удалить данное резюме. Больше оно не доступно.`
- [ ] T113 [PW] Capture screenshot:

```text
tempfiles/Playwright_evidence/04_deleted_public_link_410.png
```

### 3.5 Playwright failure handling

- [ ] T114 If any Playwright step fails:
  - save screenshot;
  - save trace if available;
  - capture console errors;
  - capture network failures;
  - consult Context7 docs for the failing framework/library;
  - apply smallest targeted fix;
  - rerun exact failing scenario;
  - repeat until solved or same failure persists after 3 focused cycles, then STOP.

### 3.6 Playwright evidence stop

- [ ] T115 [STOP] [EVIDENCE] Report Playwright checkpoint:
  - evidence folder contents;
  - screenshots/traces created;
  - all scenarios completed;
  - console/network errors if any;
  - deleted public link 410 proof.
  Wait for user confirmation.

---

## Phase 4 — Final Regression and Handoff

**Goal**: Ensure no hidden regressions remain.

- [ ] T116 Run backend tests:

```bash
cd backend
./mvnw test
```

- [ ] T117 Run frontend tests:

```bash
cd frontend
npm test -- --run
```

- [ ] T118 Run frontend build:

```bash
cd frontend
npm run build
```

- [ ] T119 Run final grep audit:

```bash
grep -R "pdfUrl" frontend/src || true
grep -R "publicUrl[^L]" frontend/src || true
grep -R "pdf_file_path\|html_file_path" frontend/src || true
grep -R "APP_PUBLIC_BASE_URL" . || true
```

- [ ] T120 Verify git status does not include:
  - `tempfiles/Playwright_evidence/`;
  - screenshots;
  - traces;
  - test credentials;
  - generated resume content.
- [ ] T121 [NO-PDF-ENGINE] Verify no unrelated PDF engine/fitting/template files were modified. If any were modified, justify explicitly or revert.
- [ ] T122 Update final task report with:
  - summary of implementation;
  - changed files;
  - tests run;
  - build status;
  - Playwright evidence location;
  - manual stop-point confirmations;
  - known limitations, if any.

- [ ] T123 [STOP] [EVIDENCE] Final handoff to user. Do not claim completion without evidence.

---

## Definition of Done

Feature is done only if all are true:

- [ ] Table row click opens modal.
- [ ] No Details column/button added.
- [ ] Rows show pointer cursor and hover highlight.
- [ ] Latest-resume card opens modal for `summary.lastResume`.
- [ ] Latest card is not clickable when no last resume exists.
- [ ] Modal uses canonical fields, not `pdfUrl`/`publicUrl`.
- [ ] Modal shows generated date, vacancy title, company if present, language, adaptation level.
- [ ] Modal shows public URL text and Copy public link action.
- [ ] Modal opens PDF in new tab when available.
- [ ] Modal downloads PDF when available.
- [ ] Modal downloads HTML for authenticated owner when available.
- [ ] Modal disables PDF buttons and shows message when PDF unavailable.
- [ ] Modal shows cover letter and copy button when present.
- [ ] Modal shows approved empty-state text when cover letter absent.
- [ ] Public URL is computed from `APP_PUBLIC_BASE_URL` or request-origin fallback.
- [ ] `.env.example` includes `APP_PUBLIC_BASE_URL`.
- [ ] Soft delete sets both `is_deleted` and `deleted_at`.
- [ ] Deleted public link returns `410 Gone` Thymeleaf page.
- [ ] Invalid public link returns `404`.
- [ ] No raw file path is exposed.
- [ ] RU/EN i18n strings are present.
- [ ] Backend tests pass.
- [ ] Frontend tests pass.
- [ ] Frontend build passes.
- [ ] Playwright MCP evidence exists in `tempfiles/Playwright_evidence/`.
- [ ] Evidence files and credentials are not committed.
- [ ] User confirmed all stop points.
