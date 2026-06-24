# Draft Implementation Plan: Home Page Saved Resume Details Modal Fix

**Draft file**: `draft_plan.md`  
**Feature Branch**: `feat009/home-page-modal-fix`  
**Spec**: `draft_spec.md`  
**Target implementer**: OpenCode + DeepSeek V4 Flash  
**Status**: Draft for conversion into final Spec Kit `plan.md`

---

## 0. Instruction to DeepSeek / OpenCode

This plan is a draft. If you agree with it and it matches the current codebase, copy it directly into final `plan.md`.  
If you find any mismatch, uncertainty, or disagreement, STOP and ask the user before coding.

Do not use this plan as loose inspiration. Treat it as a strict implementation plan with explicit stop points and evidence requirements.

---

## 1. Summary

Fix the Home page saved resume details modal end to end.

The feature repairs three connected flows:

1. Saved resumes table row click opens `ResumeDetailsDialog`.
2. Latest-resume summary card opens `ResumeDetailsDialog`.
3. Modal uses the current canonical saved-resume export/link contract and exposes public link, PDF actions, HTML download, cover letter, metadata, and delete behavior safely.

The feature also updates public-route deleted behavior:

- invalid link -> `404 Not Found`;
- soft-deleted known public link -> `410 Gone` Thymeleaf page.

This is primarily a frontend/backend contract and UI wiring feature. It must not change PDF rendering/fitting/finalization internals.

---

## 2. Current Known Architecture and Files to Inspect

Before coding, inspect the current repository on branch `main`.

### Frontend files likely involved

```text
frontend/src/views/UserHomePage.vue
frontend/src/components/home/SummaryCards.vue
frontend/src/components/home/SavedResumesTable.vue
frontend/src/components/home/ResumeDetailsDialog.vue
frontend/src/composables/useUserHome.ts
frontend/src/services/userHomeService.ts
frontend/src/services/resumeService.ts
frontend/src/services/generateResumeService.ts
frontend/src/components/generate/ExportResult.vue
frontend/src/i18n/ru.json
frontend/src/i18n/en.json
frontend/src/__tests__ or component test folders
```

### Backend files likely involved

```text
backend/src/main/java/com/resumainer/controller/ResumeController.java
backend/src/main/java/com/resumainer/controller/UserHomeController.java
backend/src/main/java/com/resumainer/controller/PublicResumeController.java
backend/src/main/java/com/resumainer/controller/ResumeDownloadController.java
backend/src/main/java/com/resumainer/dao/ResumeDao.java
backend/src/main/java/com/resumainer/dao/SavedResumeDao.java
backend/src/main/java/com/resumainer/service/ResumeService.java
backend/src/main/java/com/resumainer/service/UserHomeService.java
backend/src/main/java/com/resumainer/model/SavedResume.java
backend/src/main/java/com/resumainer/model/UserHomeSummary.java
backend/src/main/java/com/resumainer/dto/generate/SavedResumeExportDto.java
backend/src/main/resources/application.properties
backend/src/main/resources/application-dev.properties
backend/src/main/resources/application-prod.properties
backend/src/main/resources/messages.properties
backend/src/main/resources/messages_ru.properties
backend/src/main/resources/templates/error/404.html
backend/src/main/resources/templates/error/500.html
backend/src/main/resources/templates/error/410.html (create if missing)
```

### Root/config files likely involved

```text
.env.example
.gitignore
docker-compose.yml (if present)
README.md or deployment docs (only if config documentation already exists)
```

---

## 3. Confirmed Technical Decisions

### 3.1 Modal trigger

- Use row click only.
- No Details column or button.
- Add pointer cursor and hover highlight to rows.

### 3.2 Latest-resume card

- Use `summary.lastResume`.
- Only clickable when `lastResume` exists.
- Opens the same modal.

### 3.3 DTO strategy

Create or use a dedicated safe Home DTO instead of raw entity:

```java
HomeSavedResumeDto
```

or equivalent project naming.

Both:

- paginated `/api/resumes` response;
- `summary.lastResume`

must use the same DTO mapper.

### 3.4 Public URL

- Backend returns a full absolute `publicUrlLink`.
- It is computed from `APP_PUBLIC_BASE_URL` or fallback request origin.
- Normalize trailing slash.
- Result format: `{baseUrl}/{username}/{publicCode}`.

### 3.5 Old fields

- Remove frontend usage of old `publicUrl` and `pdfUrl`.
- Do not preserve old test DB records.
- Do not build new UI logic on legacy fields.

### 3.6 Public delete status

- Deleted known public link -> `410 Gone`.
- Invalid/unknown/unsafe/missing-file -> `404 Not Found`.
- Create Thymeleaf 410 page if missing.
- Vue app is not responsible for public error pages.

### 3.7 HTML download

- Authenticated owner only.
- No public HTML route.

### 3.8 Diagnostics

Errors must be investigated with full diagnostics, Context7 docs, targeted fix, rerun. Do not abandon failing checks.

---

## 4. Implementation Design

### 4.1 Backend Home DTO and mapper

Create a safe DTO. Exact package may follow existing style, but recommended:

```text
backend/src/main/java/com/resumainer/dto/home/HomeSavedResumeDto.java
```

Potential fields:

```java
private UUID id;
private String resumeTitle;
private String vacancyTitle;
private String companyName;
private String languageCode;
private String languageName;
private String adaptationLevel;
private LocalDateTime createdAt;
private String publicUrlLink;
private String pdfOpenUrl;
private String pdfDownloadUrl;
private String htmlDownloadUrl;
private boolean pdfAvailable;
private String pdfStatus;
private String pdfMessage;
private String coverLetter;
```

If existing JSON serialization style prefers strings over `LocalDateTime`, follow project convention.

Create/use a mapper method, for example:

```java
HomeSavedResumeDto toHomeSavedResumeDto(SavedResume resume, String publicBaseUrl)
```

or a dedicated service:

```java
SavedResumeHomeMapper
```

Keep it simple. Do not over-abstract.

### 4.2 URL builder

Create small utility/service to build public/action URLs safely, for example:

```text
PublicUrlService
SavedResumeActionUrlBuilder
```

Responsibilities:

- read configured public base URL;
- fallback to request origin if config absent;
- trim trailing slash;
- append `/{username}/{publicCode}`;
- produce authenticated action URLs:
  - PDF open;
  - PDF download;
  - HTML download.

Do not expose raw `pdf_file_path` or `html_file_path`.

### 4.3 Config source

Preferred env var:

```text
APP_PUBLIC_BASE_URL=http://localhost:8080
```

Add to `.env.example`.

Because this is plain Spring MVC, do not assume `.env` is automatically loaded. Inspect existing config loading first. Then implement one of:

1. read environment variable directly via `System.getenv("APP_PUBLIC_BASE_URL")`;
2. bind to existing property system if project already supports it;
3. map env var into Java properties in deployment config.

Document the actual implementation in final report.

### 4.4 Public route result type

Current public route likely returns PDF path or `null`. That is not enough to distinguish deleted from invalid.

Introduce an internal result model, for example:

```java
enum PublicResumeLookupStatus {
    ACTIVE,
    DELETED,
    NOT_FOUND,
    UNSAFE_PATH,
    MISSING_FILE
}
```

or a record/class:

```java
PublicResumeLookupResult(status, pdfPath, fileName)
```

Then controller behavior:

```text
ACTIVE -> 200 inline PDF
DELETED -> 410 Thymeleaf error page
NOT_FOUND / UNSAFE_PATH / MISSING_FILE -> 404
```

Do not leak which invalid username/code failed.

### 4.5 Thymeleaf 410 page

Inspect:

```text
backend/src/main/resources/templates/error/404.html
backend/src/main/resources/templates/error/500.html
```

If missing, create:

```text
backend/src/main/resources/templates/error/410.html
```

Follow existing style and `landing.css`.

Messages:

```properties
error.410.title=Resume no longer available
error.410.message=The user decided to delete this resume.
error.410.hint=This public resume link is no longer available.
error.410.cta=Go to homepage
```

Russian:

```properties
error.410.title=Резюме больше недоступно
error.410.message=Пользователь решил удалить данное резюме.
error.410.hint=Больше оно не доступно.
error.410.cta=На главную
```

The exact text may be adjusted for existing style, but must preserve the meaning.

### 4.6 Delete consistency

Find current delete method. Update so soft delete sets both:

```sql
is_deleted = true,
deleted_at = CURRENT_TIMESTAMP
```

or equivalent existing schema behavior. If one column does not exist, STOP and report before creating migrations.

After delete, frontend must:

- close modal;
- reload table;
- reload summary;
- show next latest resume or empty state.

### 4.7 Frontend DTO contract

Update `frontend/src/services/userHomeService.ts`.

Old fields to remove:

```ts
publicUrl
pdfUrl
```

New canonical fields:

```ts
publicUrlLink
pdfOpenUrl
pdfDownloadUrl
htmlDownloadUrl
pdfAvailable
pdfStatus
pdfMessage
coverLetter
```

Run grep/audit to ensure no stale old usage remains.

### 4.8 Modal binding fix

In `ResumeDetailsDialog.vue`, replace one-time local visible copy with computed `v-model` bridge:

```ts
const visible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
})
```

Use Context7 Vue 3 documentation if uncertain.

### 4.9 Modal content

Modal must include:

- title;
- vacancy title;
- company if present;
- generation date from `createdAt`;
- language;
- adaptation level;
- public URL text;
- Copy public link button;
- Open PDF in new tab;
- Download PDF;
- Download HTML;
- cover letter text when present;
- Copy cover letter button when present;
- approved empty state when cover letter absent;
- PDF unavailable state and disabled PDF actions.

Design must be consistent with project design tokens and existing PrimeVue components.

### 4.10 SavedResumesTable row styling

No Details button. Add row hover/cursor styling. If DataTable requires pass-through styling, use PrimeVue documented approach. Consult Context7 PrimeVue DataTable docs if uncertain.

### 4.11 SummaryCards behavior

Make latest resume card clickable styling conditional:

```vue
:class="{ clickable: lastResume }"
```

or equivalent.

Do not emit `openLastResume` when no last resume exists.

---

## 5. Testing Strategy

### 5.1 Backend tests

Add/update tests for:

- DTO mapping includes canonical fields.
- No raw file path in `/api/resumes`.
- `summary.lastResume` uses same DTO mapping as list items.
- Public URL uses `APP_PUBLIC_BASE_URL`.
- Public URL fallback uses request origin.
- Trailing slash normalized.
- Delete sets `is_deleted=true` and `deleted_at`.
- Deleted public route returns `410 Gone`.
- Invalid public route returns `404`.
- Missing PDF returns `404`.
- Unsafe/path traversal returns `404`.
- Old deleted public route test expecting `404` is updated to `410`.

### 5.2 Frontend tests

Add/update Vitest/Vue Test Utils tests for:

- `ResumeDetailsDialog` opens when parent sets visible true.
- Modal close emits `update:visible=false`.
- Row click emits selected resume.
- Row has clickable hover class/style.
- Summary card emits only when `lastResume` exists.
- Summary card not clickable when `lastResume` absent.
- Modal displays metadata.
- Modal uses `publicUrlLink`.
- Modal uses `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`.
- Modal does not use `pdfUrl` or `publicUrl`.
- Cover letter present -> copy button visible.
- Cover letter absent -> empty-state text visible.
- PDF unavailable -> PDF buttons disabled.

### 5.3 Playwright MCP evidence

Evidence folder:

```text
tempfiles/Playwright_evidence/
```

Manual prerequisite:

- STOP and ask the user to manually generate a new resume for a throwaway/local test user.
- STOP and ask the user for local test login/password.
- Do not store credentials in docs or commit them.

Playwright scenarios:

1. Login as test user.
2. Open Home.
3. Verify saved resumes table has the newly generated resume.
4. Hover table row: pointer/hover highlight visible.
5. Click table row: modal opens.
6. Verify modal metadata.
7. Verify public URL text.
8. Copy public link.
9. Open PDF in new tab.
10. Trigger Download PDF.
11. Trigger Download HTML.
12. If cover letter exists: verify text and copy button.
13. If cover letter absent: verify exact empty-state text.
14. Close modal.
15. Click latest-resume card.
16. Verify same modal opens for latest resume.
17. Delete from modal.
18. Verify table refresh.
19. Verify summary refresh.
20. Open old public URL.
21. Verify `410 Gone` Thymeleaf page and text.

If Playwright cannot read clipboard, verify success toast/message and no console errors.

---

## 6. Evidence-First Checkpoint Model

Each checkpoint must produce:

1. changed files list;
2. tests added/updated;
3. commands run;
4. command output summary;
5. if UI-related: Playwright screenshots/traces saved under `tempfiles/Playwright_evidence/`;
6. what was verified manually;
7. unresolved risks or explicit statement that none remain.

Do not continue past a STOP point until the user confirms.

---

## 7. Stop Points

### STOP POINT 1 — After Context and Baseline Inspection

Before coding, report:

- current modal bug confirmation;
- current DTO fields;
- current public route behavior;
- whether `410.html` exists;
- whether `.env.example` exists;
- current old frontend usage of `publicUrl` / `pdfUrl`;
- current public delete tests expecting 404/410.

Wait for user confirmation.

### STOP POINT 2 — After Backend Contract and Public Route Tests

After backend implementation and tests, report:

- DTO shape;
- public URL config behavior;
- `/api/resumes` sample response with sensitive data redacted;
- `summary.lastResume` sample response;
- `410 Gone` test result;
- invalid link `404` test result;
- no raw paths evidence.

Wait for user confirmation.

### STOP POINT 3 — After Frontend Unit Tests

After frontend modal/table/card implementation and Vitest, report:

- modal v-model fix;
- table row click behavior;
- latest card behavior;
- i18n keys added;
- old fields grep result;
- test command output.

Wait for user confirmation.

### STOP POINT 4 — Before Playwright

STOP and ask user to:

1. manually generate a new resume under a throwaway/local test user;
2. provide local test login/password;
3. confirm app base URL;
4. confirm whether the test resume includes cover letter or not.

Do not proceed to Playwright until user confirms.

### STOP POINT 5 — After Playwright Evidence

Report:

- evidence folder path;
- scenario list completed;
- screenshots/traces generated;
- any console/network errors;
- final visible behavior;
- deleted public link `410` evidence.

Wait for user confirmation.

### STOP POINT 6 — Final Handoff

Report:

- all changed files;
- all commands run;
- all tests/build status;
- final manual verification status;
- any limitations.

---

## 8. Build and Test Commands

Adjust commands to actual repo structure if needed.

Backend:

```bash
cd backend
./mvnw test
```

or project-root equivalent if Maven is root-scoped.

Targeted backend examples:

```bash
./mvnw test -Dtest="UserHomeControllerTest,ResumeControllerTest,PublicResumeControllerTest,ResumeDownloadControllerTest"
```

Frontend:

```bash
cd frontend
npm test -- --run
npm run build
```

Grep audit:

```bash
grep -R "pdfUrl" frontend/src || true
grep -R "publicUrl[^L]" frontend/src || true
grep -R "pdf_file_path\|html_file_path" frontend/src || true
```

Evidence folder:

```bash
mkdir -p tempfiles/Playwright_evidence
```

---

## 9. Do Not Do

- Do not add Details column/button.
- Do not preserve old frontend `publicUrl` / `pdfUrl`.
- Do not expose raw file paths.
- Do not hardcode public domain.
- Do not modify PDF renderer/fitting/template pipeline.
- Do not add unnecessary DB migrations.
- Do not create a new Vue page for public deleted link; it is Thymeleaf/backend.
- Do not commit test credentials or Playwright evidence.
- Do not mark task complete without evidence.
