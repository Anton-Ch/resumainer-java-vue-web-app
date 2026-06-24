---
description: "Task breakdown for Home Page Saved Resume Details Modal Fix"
---

# Tasks: Home Page Saved Resume Details Modal Fix

**Input**: Design documents from `specs/009-home-modal-fix/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/api-contracts.md, quickstart.md, security-review-plan.md. `component-diagram.md` is optional: if it is missing, do not invent it and do not block execution; proceed using the required planning documents.

**Constitution Compliance**: Every task phase MUST reference the ResumAIner Constitution principles:

- **I** — Code Quality & Maintainability (layered architecture, SOLID, no Spring Boot/JPA, dedicated DTO instead of raw entity)
- **II** — Testing Excellence (JUnit 5, Mockito, TDD for business logic, Vitest for frontend)
- **III** — User Experience (i18n for ALL new strings, PrimeVue design tokens, row hover/cursor feedback)
- **IV** — Performance & Reliability (PreparedStatement, JDBC transaction for delete, no modal-details API call)
- **V** — Security by Design (no raw filesystem paths, authenticated owner-only HTML download, 404/410 uniform delay, generic delete error messages)

**Organization**: Tasks are grouped by implementation phase. Each phase has clear checkpoint gates. DeepSeek/OpenCode MUST NOT continue past any `[STOP]` task until the user explicitly confirms the checkpoint. Evidence must be concrete: changed files, test command outputs, sanitized API samples, screenshots/traces where applicable, and explicit notes about unresolved issues.

## Execution Markers

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
- [ ] T017 Inspect current export endpoints: verify `GenerateResumeController` serves `GET /api/generate/resumes/{id}/pdf`, `/pdf?disposition=inline`, `/html`. Confirm these are the canonical endpoints before using them in `HomeSavedResumeDto`. If the actual routes differ, STOP and report before changing the DTO contract.
- [ ] T018 Run grep baseline:

```bash
grep -R "pdfUrl" frontend/src || true
grep -R "publicUrl" frontend/src || true
grep -R "pdf_file_path\|html_file_path" frontend/src || true
```

- [ ] T019 [STOP] [EVIDENCE] Report baseline findings to the user:
  - modal visibility bug location;
  - current DTO fields;
  - current old field usage;
  - current public route deleted behavior;
  - whether 410 page exists;
  - whether public base URL config exists;
  - whether evidence folder is ignored by git;
  - current export endpoint routes (confirm canonicals match plan).
  Wait for user confirmation before coding.

---

## Phase 1 — Backend Contract, Config, and Public Route

**Goal**: Backend returns safe canonical Home DTOs and correct public route statuses.

### 1.1 Public base URL config (SEC-002)

- [ ] T020 [TDD] Add tests for `PublicUrlService`:
  - uses configured `APP_PUBLIC_BASE_URL` when non-blank;
  - supports existing property path if project already has one;
  - uses `X-Forwarded-Proto` and `X-Forwarded-Host` headers when config absent;
  - falls back to request scheme + host + port when forwarded headers absent;
  - trims trailing slash;
  - never hardcodes localhost or production domain;
  - logs a WARN when fallback origin is used.
- [ ] T021 Add `.env.example` entry:

```env
APP_PUBLIC_BASE_URL=http://localhost:8080
```

- [ ] T022 Ensure `.env` is ignored by git if present.
- [ ] T023 Implement `PublicUrlService` (NEW) in `backend/src/main/java/com/resumainer/service/PublicUrlService.java`:
  - read `APP_PUBLIC_BASE_URL` via `System.getenv()` or existing app config;
  - support forwarded headers (`X-Forwarded-Proto`, `X-Forwarded-Host`);
  - fallback to request origin (scheme + host + port);
  - normalize trailing slash;
  - format `{baseUrl}/{username}/{publicCode}` with exactly one slash;
  - log warning when fallback is used.
- [ ] T024 [SEC] Do not add a new dotenv library unless the project already uses one.

### 1.2 Home safe DTO

- [ ] T025 [TDD] Add backend tests for canonical `HomeSavedResumeDto` shape and mapping.
- [ ] T026 Create `HomeSavedResumeDto` (NEW) in `backend/src/main/java/com/resumainer/dto/home/HomeSavedResumeDto.java` with fields:
  - `id` (long), `resumeTitle`, `vacancyTitle`, `companyName` (nullable), `languageCode` (nullable), `languageName` (nullable), `adaptationLevel` (nullable), `createdAt` (String), `publicUrlLink` (nullable), `pdfOpenUrl` (nullable), `pdfDownloadUrl` (nullable), `htmlDownloadUrl` (nullable), `pdfAvailable` (boolean), `pdfStatus` (nullable), `pdfMessage` (nullable), `coverLetter` (nullable).
- [ ] T027 Implement shared mapper method used by both paginated list and `summary.lastResume`. Map canonical export endpoints from `GenerateResumeController` (not `ResumeDownloadController` legacy routes).
- [ ] T028 Update `ResumeController.listResumes()` response to use `HomeSavedResumeDto`.
- [ ] T029 Update `UserHomeService.getHomeSummary()` so `summary.lastResume` uses the same `HomeSavedResumeDto` mapper. Ensure it lives under `summary.lastResume`, not at root level.
- [ ] T030 [SEC] Ensure response never includes: `pdf_file_path`, `html_file_path`, local filesystem path, or raw storage directory.

- [ ] T030A [STOP] [EVIDENCE] Backend DTO/config checkpoint. Stop and report before continuing to delete/public-route work:
  - `PublicUrlService` tests pass or current failure diagnostics are shown;
  - `APP_PUBLIC_BASE_URL` resolution order is proven: env/config → `X-Forwarded-*` → request origin fallback;
  - `.env.example` and `.gitignore` status is documented;
  - `/api/resumes` sample uses `HomeSavedResumeDto` canonical fields;
  - `/api/user/home` sample contains `summary.lastResume`, not root-level `lastResume`;
  - `summary.lastResume` and paginated list use the same mapper;
  - canonical `GenerateResumeController` URLs are confirmed for PDF open, PDF download, and HTML download;
  - no raw path fields are present in API samples;
  - no code changes outside the backend contract/config scope were made unless explicitly listed.
  Wait for user confirmation.

### 1.3 Soft delete consistency

- [ ] T031 [TDD] Add/update delete test: successful delete sets both `is_deleted=true` and `deleted_at`.
- [ ] T032 Update `ResumeDao` soft-delete to set both `is_deleted = true` and `deleted_at = CURRENT_TIMESTAMP` in one consistent update.
- [ ] T033 Ensure Home paginated list excludes deleted resumes (`is_deleted = false` in WHERE clause).
- [ ] T034 Ensure `summary.lastResume` excludes deleted resumes.
- [ ] T035 [SEC] Implement delete endpoint response policy:
  - non-owned resume and non-existent resume MUST return the same HTTP status and same generic response body, preferably `404` with `{ "message": "Failed to delete resume." }`;
  - unexpected server/DB errors MAY return `500`, but the response body must still be generic and must not expose internal details;
  - frontend always shows the same generic delete failure toast;
  - backend may log detailed reasons server-side only.

- [ ] T035A [STOP] [EVIDENCE] Delete/list consistency checkpoint. Stop and report before continuing to public-route/410 work:
  - successful delete sets both `is_deleted=true` and `deleted_at`;
  - `/api/resumes` excludes deleted resumes;
  - `summary.lastResume` excludes deleted resumes;
  - if deleted resume was latest, next active or empty latest state is proven by test/sample;
  - non-owned and non-existent delete responses have the same status/body;
  - unexpected server/DB error path is generic and does not leak internal details;
  - no public route changes were made yet unless explicitly listed and justified.
  Wait for user confirmation.

### 1.4 Public route status model

- [ ] T036 [TDD] Add/update public route tests:
  - active public resume -> HTTP `200 OK` with PDF inline;
  - invalid username/code -> HTTP `404`;
  - missing physical PDF file -> HTTP `404`;
  - unsafe/path traversal -> HTTP `404`;
  - soft-deleted known public resume -> HTTP `410 Gone` with Thymeleaf page;
  - `404` and `410` responses use the same uniform artificial delay mechanism; prefer asserting shared helper/mechanism invocation over fragile wall-clock timing unless the project already has a stable timing-test pattern;
  - `410` response body contains no dynamic resume data, username, public code, deletion date, ID, file path, company, or vacancy data;
  - public route does NOT intercept `/api/**`, `/app/**`, `/static/**`, `/assets/**`, `/error/**`, landing page, or Vue SPA assets.
- [ ] T037 Create `PublicResumeLookupResult` (NEW) with status values: `ACTIVE`, `DELETED`, `NOT_FOUND`, `MISSING_FILE`, `UNSAFE_PATH`.
- [ ] T038 Update `PublicResumeController`:
  - `ACTIVE` -> serve PDF inline with `200 OK`;
  - `DELETED` -> render Thymeleaf `410.html` with HTTP `410 Gone`;
  - `NOT_FOUND` / `MISSING_FILE` / `UNSAFE_PATH` -> HTTP `404`.
- [ ] T039 [SEC] Apply the same existing uniform artificial delay mechanism to both `404` and `410` error responses. Prefer reusing an existing helper/service/filter. If no existing delay mechanism is found, STOP and ask before inventing a new timing value, `Thread.sleep`, filter, or helper.

### 1.5 Thymeleaf 410 page

- [ ] T040 Inspect existing `templates/error/404.html` and `templates/error/500.html` for style reference.
- [ ] T041 Create `backend/src/main/resources/templates/error/410.html` if missing, following existing error page style. Content must be static branded text and i18n strings only. No dynamic resume data, username, public code, deletion date, IDs, paths, filenames, company/vacancy data.
- [ ] T042 Add i18n messages to `messages.properties` and `messages_ru.properties`:

```properties
error.410.title=Resume no longer available
error.410.message=The user has removed this resume. It is no longer available.
error.410.hint=This public resume link is no longer available.
error.410.cta=Go to homepage
```

```properties
error.410.title=Резюме больше недоступно
error.410.message=Пользователь решил удалить данное резюме. Больше оно не доступно.
error.410.hint=Больше оно не доступно.
error.410.cta=На главную
```

- [ ] T043 Ensure deleted public route renders/forwards to Thymeleaf `410.html` page with HTTP status `410`.

- [ ] T043A [STOP] [EVIDENCE] Public route + 410 checkpoint. Stop and report before backend aggregation tests:
  - active public link returns `200 OK` and serves PDF inline;
  - invalid username/code returns `404`;
  - missing file and unsafe/path traversal return `404` without metadata leakage;
  - known soft-deleted public link returns `410 Gone`;
  - route non-interception tests pass for `/api/**`, `/app/**`, `/static/**`, `/assets/**`, `/error/**`, landing page, and Vue SPA assets;
  - `410.html` exists or existing 410 page is verified;
  - 410 body/page contains no username, public code, ID, deletion date, file path, vacancy, company, or resume-specific data;
  - 404 and 410 use the same existing delay mechanism, or execution stopped because no existing mechanism was found.
  Wait for user confirmation.

### 1.6 Backend evidence

- [ ] T044 Run targeted backend tests:

```bash
cd backend
./mvnw test -Dtest="UserHomeControllerTest,ResumeControllerTest,PublicResumeControllerTest,GenerateResumeControllerTest,ResumeServiceTest,UserHomeServiceTest,PublicUrlServiceTest"
```

- [ ] T045 Run broader backend tests if targeted pass:

```bash
cd backend
./mvnw test
```

- [ ] T046 [EVIDENCE] Save sanitized API sample responses under `tempfiles/Playwright_evidence/backend_api_samples/`. Do not include secrets or real PII.
- [ ] T047 [STOP] [EVIDENCE] Report backend checkpoint to user:
  - changed files list;
  - DTO fields confirmation;
  - public URL config implementation (env var / forwarded headers / fallback);
  - sample `/api/resumes` and `/api/user/home` with `summary.lastResume`, with sensitive content redacted;
  - `410 Gone` test proof (status + body content check);
  - invalid `404` test proof;
  - delete consistency proof (both `is_deleted` and `deleted_at`);
  - no raw path proof;
  - route non-interception test proof.
  Wait for user confirmation.

---

## Phase 2 — Frontend DTO, Modal, Table, Summary Card

**Goal**: Home page UI uses canonical fields and opens modal from row/card.

### 2.1 Frontend service/types

- [ ] T048 [TDD] Update/add frontend tests expecting canonical saved resume fields.
- [ ] T049 Update `frontend/src/services/userHomeService.ts`:
  - remove old `publicUrl`;
  - remove old `pdfUrl`;
  - add `publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`, `pdfAvailable`, `pdfStatus`, `pdfMessage`, `coverLetter`.
- [ ] T050 Update all consuming TypeScript code to compile with new fields. Ensure `publicUrlLink` is not treated as old-field.

### 2.2 ResumeDetailsDialog — v-model fix

- [ ] T051 [TDD] Add test: parent sets `visible=true` -> Dialog opens.
- [ ] T052 [TDD] Add test: close emits `update:visible=false`.
- [ ] T053 [CTX7] Confirm Vue 3 custom `v-model` computed bridge pattern through Context7 if uncertain.
- [ ] T054 Fix modal visibility binding. Replace `ref(props.visible)` with computed get/set bridge:

```ts
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
})
```

- [ ] T054A [STOP] [EVIDENCE] Frontend modal opening checkpoint. Stop and report before implementing modal actions/delete:
  - canonical frontend types compile with `publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`, `pdfAvailable`, `pdfMessage`, `coverLetter`;
  - modal `v-model:visible` computed bridge is implemented and tested;
  - row click opens the selected resume modal in tests;
  - latest card opens `summary.lastResume` in tests;
  - latest card is non-clickable when `lastResume` is absent;
  - no delete UI/actions were implemented yet unless explicitly listed and approved.
  Wait for user confirmation.

### 2.3 ResumeDetailsDialog — modal content

- [ ] T055 Update modal content to display:
  - metadata block: saved resume creation date shown as generation date, vacancy title, company name only if present, language, adaptation level;
  - public URL text (from `publicUrlLink`);
  - Copy public link button (copies full URL, shows success toast);
  - Open PDF button (uses `pdfOpenUrl`, opens in new tab);
  - Download PDF button (uses `pdfDownloadUrl`, triggers download);
  - Download HTML button (uses `htmlDownloadUrl`, authenticated owner-only);
  - cover letter block: if short (≤150 chars) show full text; if long show preview (~150 chars) with "Show full cover letter" toggle;
  - Copy cover letter button (copies FULL text, not preview);
  - empty-state text (i18n) when cover letter absent;
  - PDF unavailable message and disabled PDF buttons when `pdfAvailable=false`;
  - HTML download disabled/hidden consistently when URL unavailable.
- [ ] T056 [SEC] Modal MUST NOT expose raw filesystem paths.
- [ ] T057 Handle clipboard API failure gracefully — show user-readable error feedback via toast.
- [ ] T058 Ensure long modal metadata text (title, vacancy, company) wraps within grid cells without truncation (word-wrap/break).

### 2.4 ResumeDetailsDialog — delete flow

- [ ] T059 [TDD] Add test: delete button exists and opens confirmation.
- [ ] T060 [TDD] Add test: cancel delete keeps resume unchanged.
- [ ] T061 [TDD] Add test: confirm delete triggers delete API call.
- [ ] T062 [TDD] Add test: after successful delete, modal closes and reload functions are called.
- [ ] T063 [TDD] Add test: delete API failure keeps modal open and shows generic error toast.
- [ ] T064 Add delete action in modal:
  - Delete resume button triggers confirmation dialog (PrimeVue ConfirmDialog or similar);
  - On confirm: button disables with loading spinner (prevents double-click);
  - On API success: close modal, emit reload for table + summary;
  - On API failure: re-enable button, show generic i18n error toast ("Failed to delete resume." / "Не удалось удалить резюме.");
  - On cancel: no action, modal remains usable.

- [ ] T064A [STOP] [EVIDENCE] Frontend modal actions/delete checkpoint. Stop and report before row styling/summary/i18n cleanup:
  - PDF open/download actions use canonical fields and are disabled when `pdfAvailable=false`;
  - public link text and copy action use `publicUrlLink`;
  - HTML download handles unavailable `htmlDownloadUrl` without calling an undefined URL;
  - cover letter preview/expand/copy behavior is tested, and copy always uses full text;
  - delete confirmation, cancel, success, failure, and double-click guard tests pass;
  - raw path fields are not rendered in modal;
  - i18n keys needed by these actions are listed.
  Wait for user confirmation.

### 2.5 SavedResumesTable row click and styling

- [ ] T065 [TDD] Add test: row click emits selected resume for modal.
- [ ] T066 [TDD] Add test: no Details button/column is rendered.
- [ ] T067 [CTX7] If DataTable row styling is unclear, check PrimeVue DataTable docs through Context7.
- [ ] T068 Add pointer cursor and hover highlight for clickable rows via CSS class.
- [ ] T069 Ensure no extra Details column/button is added.

### 2.6 SummaryCards latest resume behavior

- [ ] T070 [TDD] Add test: latest card emits `openLastResume` only when `lastResume` exists.
- [ ] T071 [TDD] Add test: latest card is not visually clickable when `lastResume` is null.
- [ ] T072 Update `SummaryCards.vue` so clickable styling applies only when `lastResume` exists. No `openLastResume` emit when absent.
- [ ] T073 Ensure `UserHomePage.vue` correctly passes `summary.lastResume` into `openResumeModal`.

### 2.7 i18n

- [ ] T074 Add RU and EN i18n keys for all new visible strings in `frontend/src/i18n/en.json` and `ru.json`:
  - Copy public link / Скопировать ссылку
  - Copy cover letter / Скопировать сопроводительное письмо
  - Show full cover letter / Показать полное письмо
  - Open PDF / Открыть PDF
  - Download PDF / Скачать PDF
  - Download HTML / Скачать HTML
  - PDF unavailable / PDF недоступен (with status message)
  - Cover letter was not selected / Письмо не было выбрано (empty-state)
  - Failed to delete resume / Не удалось удалить резюме (generic error)
  - Delete resume / Удалить резюме
  - Confirm deletion? / Подтвердить удаление?
  - Cancel / Отмена
  - Generation date / Дата создания
  - Language / Язык
  - Adaptation level / Уровень адаптации
  - Company / Компания
  - Vacancy / Вакансия
  - Copied / Скопировано (success feedback)
  - Failed to copy / Не удалось скопировать (error feedback)
- [ ] T075 Ensure no hardcoded visible user-facing strings remain in modified Vue components.

### 2.8 Frontend evidence

- [ ] T076 Run frontend tests:

```bash
cd frontend
npm test -- --run
```

- [ ] T077 Run frontend build:

```bash
cd frontend
npm run build
```

- [ ] T078 Run grep audit:

```bash
grep -R "pdfUrl" frontend/src || true
grep -R "publicUrl[^L]" frontend/src || true
grep -R "pdf_file_path\|html_file_path" frontend/src || true
```

Manual classification required:
- Allowed: `publicUrlLink` only.
- Forbidden standalone old fields: `.publicUrl`, `publicUrl:`, `{ publicUrl }`, `pdfUrl`, `.pdfUrl`, `pdfUrl:`.
- Forbidden raw path fields: `pdf_file_path`, `html_file_path`, storage directory names.
- Do not treat any grep output as automatically pass/fail without inspection; classify every match.

- [ ] T079 [STOP] [EVIDENCE] Report frontend checkpoint to user:
  - changed files list;
  - modal v-model fix;
  - table row click behavior;
  - summary card behavior;
  - delete flow (confirm, cancel, success, error, double-click guard);
  - i18n keys added;
  - tests/build output;
  - grep audit output.
  Wait for user confirmation.

---

## Phase 3 — Playwright MCP End-to-End Verification

**Goal**: Prove actual UI behavior with real local app and generated resume.

### 3.1 Prepare evidence folder

- [ ] T080 Create local evidence folder:

```bash
mkdir -p tempfiles/Playwright_evidence
```

- [ ] T081 Confirm `tempfiles/` is ignored by git. If not, update `.gitignore`.

### 3.2 Manual prerequisite stop

- [ ] T082 [STOP] Ask the user to manually generate a new disposable resume for a throwaway/local test user. Warn clearly that the Playwright delete scenario will intentionally delete this generated resume.
  - Optional but recommended: ask the user to generate two disposable resumes if next-latest-card behavior must be proven. If only one resume exists, verify latest-card empty state after deletion instead.
- [ ] T083 [STOP] Ask the user to provide:
  - local app base URL;
  - throwaway/local test login;
  - throwaway/local test password;
  - whether the generated test resume includes cover letter.
- [ ] T084 Do not write the credentials into files. Do not commit credentials or evidence.

### 3.3 Playwright scenario — modal and actions

- [ ] T085 [PW] Log in as the throwaway/local test user.
- [ ] T086 [PW] Open Home page.
- [ ] T087 [PW] Verify saved resumes table includes the newly generated resume.
- [ ] T088 [PW] Hover over table row; capture screenshot showing hover/pointer visual if possible.
- [ ] T089 [PW] Click table row; verify modal opens for the selected row (not latest by mistake).
- [ ] T090 [PW] Capture screenshot:

```text
tempfiles/Playwright_evidence/01_table_row_modal_open.png
```

- [ ] T091 [PW] Verify modal metadata: vacancy title, company if present, saved resume creation date shown as generation date, language, adaptation level.
- [ ] T092 [PW] Verify visible public URL text (full absolute URL).
- [ ] T093 [PW] Click Copy public link; verify success feedback or clipboard content if available.
- [ ] T094 [PW] Click Open PDF; verify new tab/popup or inline PDF route response.
- [ ] T095 [PW] Capture screenshot or trace:

```text
tempfiles/Playwright_evidence/02_open_pdf.png
```

- [ ] T096 [PW] Click Download PDF; verify download event/request.
- [ ] T097 [PW] Click Download HTML; verify download event/request.
- [ ] T098 [PW] If cover letter exists:
  - verify cover letter text visible;
  - if long text, verify preview and expand toggle;
  - verify Copy cover letter button visible;
  - click Copy cover letter; verify full text copied (not preview).
- [ ] T099 [PW] If cover letter does not exist:
  - verify exact empty-state text: `Сопроводительное письмо не было выбрано в настройках генерации.`
- [ ] T100 [PW] Close modal via close icon/Escape/backdrop.
- [ ] T101 [PW] Click latest-resume summary card.
- [ ] T102 [PW] Verify same modal opens for latest resume.
- [ ] T103 [PW] Capture screenshot:

```text
tempfiles/Playwright_evidence/03_latest_card_modal_open.png
```

### 3.4 Delete and 410 verification

- [ ] T104 [PW] Copy/store the public URL locally for the test session only.
- [ ] T105 [PW] Open modal delete action.
- [ ] T106 [PW] Verify confirmation dialog appears.
- [ ] T107 [PW] Cancel delete first; verify resume remains and modal stays usable.
- [ ] T108 [PW] Open delete action again and confirm delete.
- [ ] T109 [PW] Verify confirm button disables with loading state.
- [ ] T110 [PW] Verify modal closes after successful soft-delete.
- [ ] T111 [PW] Verify table refreshes and deleted resume is gone.
- [ ] T112 [PW] Verify summary latest card refreshes to next active resume or empty state.
- [ ] T113 [PW] Open previously copied public URL.
- [ ] T114 [PW] Verify HTTP status is `410 Gone`.
- [ ] T115 [PW] Verify backend Thymeleaf 410 page is shown, not Vue SPA.
- [ ] T116 [PW] Verify text: `Пользователь решил удалить данное резюме. Больше оно не доступно.`
- [ ] T117 [PW] Verify 410 page contains no resume-specific data (no username, public code, ID, dates, paths, vacancy, company).
- [ ] T118 [PW] Capture screenshot:

```text
tempfiles/Playwright_evidence/04_deleted_public_link_410.png
```

### 3.5 Playwright failure handling

- [ ] T119 If any Playwright step fails:
  - save screenshot;
  - save trace if available;
  - capture console errors;
  - capture network failures;
  - consult Context7 docs for the failing framework/library;
  - apply smallest targeted fix;
  - rerun exact failing scenario;
  - repeat until solved or same failure persists after 3 focused cycles, then STOP.

### 3.6 Playwright evidence stop

- [ ] T120 [STOP] [EVIDENCE] Report Playwright checkpoint to user:
  - evidence folder contents;
  - screenshots/traces created;
  - all scenarios completed;
  - console/network errors if any;
  - deleted public link 410 proof;
  - any remaining issues.
  Wait for user confirmation.

---

## Phase 4 — Final Regression and Handoff

**Goal**: Ensure no hidden regressions remain.

- [ ] T121 [NO-PDF-ENGINE] Run backend tests:

```bash
cd backend
./mvnw test
```

- [ ] T122 Run frontend tests:

```bash
cd frontend
npm test -- --run
```

- [ ] T123 Run frontend build:

```bash
cd frontend
npm run build
```

- [ ] T124 Run final grep audit:

```bash
grep -R "pdfUrl" frontend/src || true
grep -R "publicUrl[^L]" frontend/src || true
grep -R "pdf_file_path\|html_file_path" frontend/src || true
grep -R "APP_PUBLIC_BASE_URL" . || true
```

Manual classification required for final grep:
- Allowed: `publicUrlLink`, `.env.example` documentation, valid `APP_PUBLIC_BASE_URL` config/test usage.
- Forbidden: old standalone `.publicUrl`, `publicUrl:`, `{ publicUrl }`, `pdfUrl`, `.pdfUrl`, `pdfUrl:`, raw file path fields in frontend/API DTOs.
- Every grep match must be classified in the final handoff report.

- [ ] T125 Verify git status does not include:
  - `tempfiles/Playwright_evidence/`;
  - screenshots;
  - traces;
  - test credentials;
  - generated resume content.
- [ ] T126 [NO-PDF-ENGINE] Verify no unrelated PDF engine/fitting/template files were modified. If any were modified, justify explicitly or revert.
- [ ] T127 Update final task report with:
  - summary of implementation;
  - changed files list;
  - tests run and results;
  - build status;
  - Playwright evidence location;
  - manual stop-point confirmations;
  - known limitations, if any.
- [ ] T128 [STOP] [EVIDENCE] Final handoff to user. Do not claim completion without evidence.

---

## Definition of Done

Feature is done only if all are true:

- [ ] Table row click opens modal for selected resume.
- [ ] No Details column/button added to table.
- [ ] Rows show pointer cursor and hover highlight.
- [ ] Latest-resume card opens modal for `summary.lastResume`.
- [ ] Latest card is not clickable when `lastResume` absent.
- [ ] Modal uses canonical fields (`publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`, `pdfAvailable`, `coverLetter`), not old `pdfUrl`/`publicUrl`.
- [ ] Modal shows saved resume creation date as generation date, vacancy title, company if present, language, adaptation level.
- [ ] Modal shows public URL text and Copy public link action.
- [ ] Modal opens PDF in new tab when available (uses `pdfOpenUrl`).
- [ ] Modal downloads PDF when available (uses `pdfDownloadUrl`).
- [ ] Modal downloads HTML for authenticated owner when available (uses `htmlDownloadUrl` from canonical `GenerateResumeController` endpoints).
- [ ] Modal disables PDF buttons and shows message when `pdfAvailable=false`.
- [ ] Modal shows cover letter and Copy cover letter button when present (copy = full text).
- [ ] Modal shows approved i18n empty-state text when cover letter absent.
- [ ] Modal has delete action with confirmation dialog.
- [ ] Delete confirm button disables with loading state (double-click prevention).
- [ ] Cancel delete keeps resume unchanged.
- [ ] Successful delete closes modal, reloads table, reloads summary.
- [ ] Delete API failure keeps modal open with generic error toast.
- [ ] Public URL is computed from `APP_PUBLIC_BASE_URL` or forwarded-header/request-origin fallback.
- [ ] `.env.example` includes `APP_PUBLIC_BASE_URL`.
- [ ] `PublicUrlService` logs warning when fallback origin is used.
- [ ] Soft delete sets both `is_deleted` and `deleted_at`.
- [ ] Deleted public link returns HTTP `410 Gone` with backend Thymeleaf page.
- [ ] `410.html` contains no dynamic resume data, username, public code, IDs, dates, paths, or company/vacancy.
- [ ] Invalid/unsafe/missing public link returns HTTP `404`.
- [ ] `404` and `410` responses have same uniform artificial delay (reuse existing mechanism).
- [ ] Public route does not intercept `/api/**`, `/app/**`, `/static/**`, `/assets/**`, `/error/**`, landing page, or Vue SPA assets.
- [ ] No raw `pdf_file_path`/`html_file_path`/storage directory exposed in API responses.
- [ ] No old standalone `publicUrl` / `pdfUrl` frontend usage remains.
- [ ] RU/EN i18n strings present for all new UI text.
- [ ] Backend tests pass (`mvn test`).
- [ ] Frontend tests pass (`npm test -- --run`).
- [ ] Frontend build passes (`npm run build`).
- [ ] Playwright MCP evidence exists in `tempfiles/Playwright_evidence/`.
- [ ] Evidence files and credentials are not committed.
- [ ] No modified PDF renderer/fitting/finalization/template files.
- [ ] User confirmed all stop points.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 0** (Context): No dependencies — can start immediately.
- **Phase 1** (Backend): Depends on Phase 0 completion.
- **Phase 2** (Frontend): Can start after Phase 1 backend DTO endpoints are ready (frontend needs canonical fields to compile). Backend Phase 1.4 (public route) and 1.5 (410 page) can overlap with Phase 2.
- **Phase 3** (Playwright): Depends on Phase 1 + Phase 2 completion + Docker environment.
- **Phase 4** (Regression): Depends on all previous phases complete.

### Parallel Opportunities

- Frontend Phase 2.1-2.6 can proceed after backend Phase 1.2 (DTO contract) is stable.
- Backend Phase 1.4 (public route) and 1.5 (410 page) are independent of Phase 2.
- i18n (Phase 2.7) is independent of other frontend implementation.
