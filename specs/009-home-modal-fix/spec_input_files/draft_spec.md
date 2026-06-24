# Draft Feature Specification: Home Page Saved Resume Details Modal Fix

**Draft file**: `draft_spec.md`  
**Feature Branch**: `feat009/home-page-modal-fix`  
**Target implementer**: OpenCode + DeepSeek V4 Flash  
**Product**: ResumAIner  
**Status**: Draft for conversion into final Spec Kit docs

---

## 0. Instruction to DeepSeek / OpenCode

This file is a draft prepared after codebase and business-analysis review.  

If you agree with a section and it matches the current repository, **copy it directly into the final Spec Kit `spec.md`**.  
If anything is unclear, inconsistent with the current codebase, unsupported by the current architecture, or you disagree with any requirement, **STOP immediately and ask the user for clarification**.  
Do **not** silently reinterpret product decisions.  
Do **not** invent a different UX, a different route contract, or a different data model without approval.

When implementing this feature, use these rules:

1. Evidence first: every logical group of work must end with proof, not just a claim.
2. Use Context7 MCP for relevant framework/library documentation before fixing uncertain framework behavior.
3. Use Playwright MCP for end-to-end UI evidence.
4. Store Playwright evidence under `tempfiles/Playwright_evidence/`.
5. Do not commit screenshots, videos, traces, credentials, or generated resume content.
6. If a test/build/Playwright error occurs:
   - capture the full diagnostic output;
   - identify the failing layer;
   - consult Context7 documentation for the relevant framework/library;
   - apply the smallest targeted fix;
   - rerun the exact failing command;
   - repeat the cycle until the error is solved;
   - if the same error persists after 3 focused fix cycles, STOP and ask the user with full diagnostics.

---

## 1. Problem Statement

The Home page already contains a table of saved/generated resumes and a clickable summary card for the latest resume. Business analysis requires that opening a saved resume from Home shows a modal dialog with:

- public PDF page link;
- copy public link button;
- Open PDF in a new tab;
- Download PDF;
- Download HTML;
- cover letter text when generated;
- copy cover letter button when cover letter exists;
- clear empty-state text when cover letter was not selected;
- metadata: generation date, vacancy title, company name if present, language, adaptation level.

Current behavior is broken:

1. The table row click and latest-resume card are intended to open `ResumeDetailsDialog`, but the modal does not open.
2. The likely immediate Vue bug is a local `ref(props.visible)` copy inside the dialog instead of a proper `v-model` computed bridge.
3. Home page saved-resume DTOs still appear to use older fields such as `publicUrl` and `pdfUrl`, while the PDF/export pipeline now uses newer canonical fields such as `publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`, and PDF availability/status metadata.
4. Public route behavior for soft-deleted resumes must return `410 Gone` with a human-readable Thymeleaf page, not generic `404`.

This feature must repair the Home modal flow end to end without touching PDF rendering/fitting/finalization internals.

---

## 2. Scope

### In Scope

- Home table row click opens saved resume details modal.
- Home table row visually communicates clickability with pointer cursor and hover highlight.
- No extra "Details" action column or button is added because the table is already wide.
- Latest-resume summary card opens the same modal for `summary.lastResume`.
- Latest-resume summary card is visually clickable only when `lastResume` exists.
- Modal uses the current, canonical Home saved-resume DTO contract.
- Modal includes PDF/public/HTML/cover-letter actions and metadata.
- Backend Home list and summary latest resume use the same safe DTO mapper.
- Backend returns public link as a full absolute URL based on configurable public base URL.
- `.env.example` includes public base URL configuration.
- Backend reads public base URL from environment/config and falls back to request origin if absent.
- Public route returns:
  - `200 OK` for active public resume;
  - `404 Not Found` for invalid username/code, missing physical PDF, unsafe path, or path traversal;
  - `410 Gone` for valid known public code of a soft-deleted resume.
- Create Thymeleaf `410.html` page if missing.
- Existing public error page responsibility remains backend/Thymeleaf, not Vue SPA.
- Update tests and Playwright MCP evidence.

### Out of Scope

- No changes to OpenHTMLToPDF renderer.
- No changes to PDF fitting engine.
- No changes to PDF templates.
- No changes to AI generation, prompt building, parser, finalization pipeline, or budget rules.
- No new "resume details page"; modal remains on Home page.
- No new public HTML route for recruiters.
- No new username slug system unless current route is impossible with existing username format; if so, STOP and ask.
- No support required for legacy test saved resumes that only have old `public_url` / `pdf_url` fields.
- Do not preserve old frontend fields `publicUrl` and `pdfUrl`; migrate to canonical fields.

---

## 3. Product Decisions Already Confirmed

1. **Open trigger**: only table row click; no "Details" column/button.
2. **Row visual feedback**: pointer cursor + hover highlight.
3. **Latest resume card**: opens `summary.lastResume`.
4. **Latest resume definition**: newest by saved resume `createdAt DESC`, regardless of PDF availability.
5. **Generation date in modal**: use saved resume `createdAt`.
6. **Company display**: show company name only if present.
7. **Cover letter absent text**:
   - Russian: `Сопроводительное письмо не было выбрано в настройках генерации.`
   - English equivalent must be added via i18n.
8. **PDF unavailable**: modal opens; PDF actions are disabled and status/message is visible.
9. **Public link display**: full URL like:
   - local: `http://localhost:8080/johndoe/GTFQ`
   - production: `https://resumainer.com/johndoe/GTFQ`
10. **Public URL source**: backend returns full `publicUrlLink`, computed from public base URL config or request origin.
11. **DTO strategy**: create/use dedicated safe Home DTO, not raw entity and not export-only DTO.
12. **Home modal data loading**: list and summary endpoints return enough data; no separate details fetch is required.
13. **Legacy old records**: ignore them; it is acceptable if older test rows without new canonical metadata do not work.
14. **Soft-delete public route**: deleted public resume returns `410 Gone`.
15. **HTML download**: authenticated owner only, from Home modal; not available on public route.
16. **Metadata block**: show date, vacancy title, company if present, language, adaptation level.
17. **New visible UI text**: RU/EN through i18n only.
18. **Evidence**: Playwright evidence under `tempfiles/Playwright_evidence/`.

---

## 4. User Scenarios and Acceptance Criteria

### User Story 1 — Open Saved Resume from Home Table

As a logged-in user, I want to click a saved resume row on the Home page and see details in a modal so that I can quickly access the generated PDF, public link, HTML artifact, and cover letter.

**Acceptance Criteria**

1. Given the Home page has saved resumes, when the user hovers over a row, the row shows pointer cursor and hover highlight.
2. Given the user clicks any saved resume row, when data is loaded, the details modal opens.
3. Given the modal opens, then it displays the selected resume, not the latest resume by mistake.
4. Given the modal is closed via close icon / Escape / backdrop, then parent `modalVisible` state is synchronized to `false`.
5. Given the user reopens another row, then the modal displays the newly selected resume data.

---

### User Story 2 — Open Latest Resume from Summary Card

As a logged-in user, I want the "Latest resume" summary card to open details for the newest generated resume.

**Acceptance Criteria**

1. Given `summary.lastResume` exists, the latest-resume card is visually clickable.
2. Given `summary.lastResume` exists and the user clicks the card, the same `ResumeDetailsDialog` opens.
3. Given `summary.lastResume` is absent, the card is not visually clickable and does not emit open action.
4. Given the latest resume is soft-deleted after modal delete action, the summary is reloaded and the card either shows the next newest active resume or empty state.

---

### User Story 3 — Use Modal Actions

As a logged-in user, I want the modal to expose the same practical export actions as the export page, without leaving Home.

**Acceptance Criteria**

1. The modal displays:
   - resume title;
   - vacancy title;
   - company name only if present;
   - created/generated date;
   - language;
   - adaptation level;
   - public URL text;
   - PDF status/message if relevant.
2. The modal primary actions are:
   - Open PDF in new tab;
   - Download PDF;
   - Copy public link.
3. The modal secondary action includes:
   - Download HTML.
4. If PDF is unavailable, Open PDF and Download PDF are disabled and a user-readable message is shown.
5. If HTML download URL is unavailable, Download HTML is disabled or hidden consistently.
6. The modal never exposes raw filesystem paths.

---

### User Story 4 — Cover Letter Display and Copy

As a logged-in user, I want to see and copy the cover letter when it was generated.

**Acceptance Criteria**

1. Given cover letter exists, the modal displays the cover letter text and a Copy cover letter button.
2. Given cover letter exists, clicking Copy cover letter copies exact text and shows success feedback.
3. Given cover letter does not exist, the modal displays:
   - RU: `Сопроводительное письмо не было выбрано в настройках генерации.`
   - EN: approved equivalent via i18n.
4. Given cover letter does not exist, no active Copy cover letter button is shown.

---

### User Story 5 — Public Link Behavior After Delete

As a recruiter opening a public link, I want to understand when a resume was intentionally removed instead of thinking the site is broken.

**Acceptance Criteria**

1. Given a public link points to an active saved resume, public route returns `200 OK` and opens PDF inline.
2. Given a public link has invalid username or code, route returns `404 Not Found`.
3. Given the saved resume is soft-deleted and the public username/code used to be valid, route returns `410 Gone`.
4. Given `410 Gone` is returned, user sees a backend Thymeleaf error page, not Vue SPA, with a clear message:
   - RU: `Пользователь решил удалить данное резюме. Больше оно не доступно.`
5. Given path traversal or unsafe file path is attempted, route returns `404 Not Found` and leaks no metadata.

---

### User Story 6 — Public Base URL Configuration

As a developer/deployer, I want public links to adapt to local and production environments without code changes.

**Acceptance Criteria**

1. `.env.example` includes `APP_PUBLIC_BASE_URL`.
2. Backend reads public base URL from environment/config.
3. If config is absent, backend falls back to current request origin.
4. Backend normalizes trailing slash.
5. Generated/displayed public URL is:
   - `{baseUrl}/{username}/{publicCode}`
6. No hardcoded `localhost`, VPS IP, or production domain exists in Java/Vue code.

---

## 5. Functional Requirements

### FR-009-001 — Modal Visibility Binding

`ResumeDetailsDialog.vue` MUST use a proper Vue 3 `v-model:visible` computed bridge or equivalent reactive binding. It MUST NOT use one-time `ref(props.visible)` copying.

### FR-009-002 — Table Row Trigger

`SavedResumesTable.vue` MUST open modal on row click only. It MUST NOT add a Details column/button.

### FR-009-003 — Row Clickability Styling

Table rows MUST show pointer cursor and hover highlight to communicate clickability.

### FR-009-004 — Latest Card Trigger

`SummaryCards.vue` MUST emit `openLastResume` only when `lastResume` exists. The clickable visual style MUST apply only when `lastResume` exists.

### FR-009-005 — Canonical Home Saved Resume DTO

Backend MUST provide a safe Home saved resume DTO with canonical fields. Recommended fields:

```ts
type HomeSavedResumeDto = {
    id: string
    resumeTitle: string
    vacancyTitle: string
    companyName?: string | null
    languageCode?: string | null
    languageName?: string | null
    adaptationLevel?: string | null
    createdAt: string
    publicUrlLink: string | null
    pdfOpenUrl: string | null
    pdfDownloadUrl: string | null
    htmlDownloadUrl: string | null
    pdfAvailable: boolean
    pdfStatus?: string | null
    pdfMessage?: string | null
    coverLetter?: string | null
}
```

Exact Java naming may follow existing conventions, but the frontend must not use old `publicUrl` or `pdfUrl`.

### FR-009-006 — Shared Mapper

The paginated saved-resume table response and `summary.lastResume` MUST use the same safe mapping logic or mapper method.

### FR-009-007 — Remove Old Frontend Fields

Frontend code MUST stop using old `publicUrl` and `pdfUrl`. New canonical fields MUST drive all modal actions.

### FR-009-008 — Public Link URL

Backend MUST return full absolute `publicUrlLink` based on `APP_PUBLIC_BASE_URL` or fallback request origin.

### FR-009-009 — Public Base URL Env

`.env.example` MUST document `APP_PUBLIC_BASE_URL`. Backend MUST actually read the env/config value; simply adding `.env.example` is not sufficient.

### FR-009-010 — Authenticated Action URLs

PDF and HTML modal action URLs MUST use authenticated owner endpoints. They MUST NOT expose raw file paths.

### FR-009-011 — Cover Letter Empty State

If `coverLetter` is null/blank, modal MUST show the approved i18n empty-state text.

### FR-009-012 — PDF Unavailable State

If `pdfAvailable=false`, modal MUST show a user-readable status/message and disable PDF actions.

### FR-009-013 — Soft Delete Consistency

Delete action MUST mark both:

- `is_deleted = true`
- `deleted_at = CURRENT_TIMESTAMP`

or equivalent existing schema-consistent values.

### FR-009-014 — Public Deleted Route

Public route MUST distinguish active, deleted, not found, and unsafe/missing-file states. Deleted known public code MUST return `410 Gone`.

### FR-009-015 — Thymeleaf 410 Page

If `src/main/resources/templates/error/410.html` does not exist, create it following existing error page style. This is backend/Thymeleaf responsibility, not Vue SPA.

### FR-009-016 — Tests

Backend and frontend tests MUST be updated. Old tests expecting deleted public resume to return `404` MUST be updated to `410`.

### FR-009-017 — Playwright Evidence

Playwright MCP evidence MUST be stored under `tempfiles/Playwright_evidence/`.

---

## 6. Non-Functional Requirements

### Reliability

- No raw filesystem paths in API responses.
- No hardcoded public domain.
- No silent swallowing of errors.
- Deleted public links produce clear `410 Gone` page.

### Security

- Public route must not leak metadata for invalid username/code or unsafe path.
- Public route only serves PDF for active public resume.
- HTML download is authenticated owner-only.
- Test credentials and generated evidence must not be committed.

### UX

- Modal design must be consistent with existing project frontend design tokens, colors, spacing, and PrimeVue components.
- Modal should be similar in structure to export page, but not pixel-perfect.
- Row click should feel intentional through hover/cursor.
- No extra table action column.

### Maintainability

- Keep code layered.
- Use a dedicated DTO/mapper instead of exposing raw model internals.
- Do not modify unrelated PDF generation code.
- Use i18n for all visible text.

---

## 7. Edge Cases

- No saved resumes.
- `summary.lastResume` is null.
- Table has rows but summary has no lastResume due API inconsistency.
- Latest resume PDF is unavailable.
- Latest resume has no cover letter.
- Company name is null/blank.
- Public base URL has trailing slash.
- `APP_PUBLIC_BASE_URL` is missing.
- Soft-deleted resume public link is opened.
- Invalid public username/code is opened.
- Existing physical PDF file is missing.
- Unsafe/path traversal attempt.
- User deletes latest resume from modal.
- Clipboard API unavailable in Playwright/browser.
- Legacy records contain old `public_url`/`pdf_url` only.
- Old frontend references to `pdfUrl`/`publicUrl` remain after migration.

---

## 8. Success Definition

The feature is complete only when:

1. Clicking a saved resume table row opens the modal.
2. Clicking the latest-resume summary card opens the same modal for newest resume.
3. Modal shows correct metadata and actions.
4. Public link is full absolute URL using config/fallback.
5. Copy public link works.
6. Open PDF and Download PDF work when available.
7. Download HTML works for authenticated owner.
8. Cover letter display/copy works when present.
9. Approved empty-state text appears when cover letter was not selected.
10. PDF unavailable state is visible and buttons are disabled.
11. Delete closes modal, refreshes table, refreshes summary.
12. Deleted public link returns `410 Gone` Thymeleaf page.
13. Invalid public link still returns `404`.
14. No old `pdfUrl` / `publicUrl` frontend usage remains.
15. Tests/build pass.
16. Playwright MCP evidence exists under `tempfiles/Playwright_evidence/`.
17. Human stop-point confirmations are completed after each logical task group.
