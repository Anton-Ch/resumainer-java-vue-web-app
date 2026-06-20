# Implementation Plan: PDF/HTML Resume Export and Bullet-Point Review Hardening

**Branch**: `feat/008-pdf-generation` | **Date**: 2026-06-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/008-pdf-generation/spec.md`, production codebase (feat/007), approved PDF spike at `spec_input_files/pdf-spike-openhtmltopdf-v12-final/`, and memory synthesis from [memory-synthesis.md](memory-synthesis.md).

---

## Summary

Implement production-ready PDF and matching HTML resume export for ResumAIner, plus prerequisite bullet-point persistence and review hardening.

Two Phase Groups:

1. **Phase Group 1 — Bullet Points + Review + Prompt/Parser Hardening**
   - Structured bullet-point persistence for generated work experience and projects.
   - Update parser, validator, prompt builder, DAO/service, Review API, and frontend so bullets are first-class editable fields.
   - Finalization consumes edited bullets.

2. **Phase Group 2 — PDF/HTML Generation from Approved Spike**
   - Port approved OpenHTMLToPDF spike into production backend.
   - Generate strict 1-page/2-page PDFs with validation and fitting.
   - Generate matching PDF-parity HTML; Download HTML uses new renderer.
   - Legacy HTML renderer preserved as deprecated fallback.
   - Authenticated PDF download and public PDF route.

**Critical rule**: Port/copy/adapt from the spike. Do not invent a new fitting engine or layout.

---

## Technical Context

**Language/Version**: Java 21 LTS
**Backend Framework**: Spring MVC 6.x, Jakarta EE 10, no Spring Boot
**Frontend**: Vue 3, TypeScript, Vite, PrimeVue 4, vue-router, vue-i18n
**Database**: PostgreSQL 17, Flyway migrations, plain JDBC, custom connection pool
**Persistence Style**: DAO layer, `PreparedStatement` only; no ORM/JPA/Hibernate

**PDF Library** (researched via Context7 + spike pom.xml):

| Dependency | Version | Source | Notes |
|---|---|---|---|
| `openhtmltopdf-pdfbox` | 1.0.10 | Spike: `com.openhtmltopdf` | Context7 shows newer group `io.github.openhtmltopdf` — verify at implementation |
| `pdfbox` | 2.0.30 | Spike: `org.apache.pdfbox` | Spike uses PDFBox 2.x. Context7 refers to PDFBox 3 as backend; use spike version for compatibility |
| `openhtmltopdf-core` | 1.0.10 | Transitive via pdfbox artifact | Not explicitly in spike pom but required |
| `openhtmltopdf-slf4j` | 1.0.10 | Optional | For SLF4J logging integration |

**OpenHTMLToPDF CSS limitations** (per Context7 docs):
- No `overflow:hidden` (incompatible with tagged content)
- No `position: relative/absolute` for out-of-flow content (disrupts reading order)
- No flexbox/grid (CSS3 not fully supported)
- Use `div` with `display: table` instead of `<table>` elements
- Custom fonts via `@font-face` or programmatic `AutoFont` loading
- Form controls not supported — irrelevant for resume rendering

**Spike-produced PDF validations**: 17 edge case scenarios (ec01–ec17), EN+RU bilingual, 1-page and 2-page, validated for page count, text extraction, page notes, fill targets. All pass in spike.

**Existing Production Budget Source**: `ResumeBudgetConfigDao`, `ResumeBudgetConfigService`, `WorkExperienceBudgetResolver`, and their backing tables.

**Storage**: Files on configurable filesystem directory; metadata in `saved_resume` record. Compensation logic for file+DB rollback. (Per brainstorming Q1)

**Fonts**: Inter (body) and Manrope (headings) ported from spike, isolated to PDF pipeline. Both OFL-licensed. (Per brainstorming Q2)

**Testing**:
- Backend: JUnit 5 + Mockito; integration-style service tests where practical
- Frontend: Vue Test Utils/Vitest (as existing project uses) + manual smoke tests
- Target: useful test coverage for new/modified feature code; no fake tests

**Quality Constraints**: KISS, detailed safe logging, no raw paths, UTF-8, no browser-only CSS in PDF XHTML, no real OpenRouter in automated tests.

---

## Constitution Check

*GATE: Must pass before task breakdown. Re-check after major design changes and before final handoff.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | ✅ PASS | Layered architecture: controller/service/dao/model/dto/config. PDF classes ported into dedicated `service/pdf/` and `model/pdf/` packages. Legacy renderer kept but deprecated. KISS enforced. |
| **II. Testing Excellence** | ✅ PASS | TDD for parser, bullet persistence, review saving, PDF fitting, validation, finalization rollback, download endpoints. Target useful coverage. Reference: D23 (catch Exception for rollback), D16 (standalone MockMvc). |
| **III. User Experience Consistency** | ✅ PASS | Review supports bullet editing. Export actions work. HTML/PDF parity. EN/RU i18n (including page notes). No hardcoded strings. Fitting failure: "Try again" button returns to Review with edits preserved. Loading screen reuses existing AI-generation wait pattern. |
| **IV. Performance & Reliability** | ✅ PASS | PreparedStatement SQL. Transaction/compensation for file+DB finalization (D10, D23). PDF fit attempts bounded by DB config. Detailed safe logs. UTF-8 throughout. |
| **V. Security by Design** | ✅ PASS | Owner-scoped downloads. Public route serves PDF only for active public resume. All user-editable text HTML-escaped before template insertion (FR-008-023-1). No raw paths, no secrets, no PII in logs. Invalid/deleted public links return 404. |

### Complexity Justification

PDF fitting is layout-sensitive by nature. Complexity is controlled by porting the proven spike instead of inventing new logic. No architectural experiments. Ported classes follow spike behavior closely; deviations documented explicitly.

### Memory-Driven Guardrails

| Memory | Impact |
|---|---|
| **B24** (INSERT omission) | Audit all DAO write methods against their migrations. When adding `pdf_status`, `pdf_file_path`, `pdf_page_count` to `saved_resumes`, ensure `insert()` and `update()` include ALL columns. |
| **B15** (FK type match) | Bullet tables reference `generation_response_experience(id)` and `generation_response_project(id)` — verify these are BIGINT/BIGSERIAL. |
| **B9** (Long unboxing NPE) | Nullable PDF metadata fields (`pdf_page_count`, `pdf_generated_at`) use `Long` (boxed), never `long` (primitive). |
| **D23** (catch Exception) | File+DB compensation: catch `Exception`, not just `SQLException`, to ensure rollback on all error types. |
| **D25** (HTML-first pipeline) | This feature completes the deferred PDF conversion from Feature 007. |

---

## Non-Negotiable Porting Rules

These rules exist because prior weak implementation drifted away from the prototype and caused extensive rework.

1. **Inspect the spike before coding PDF/HTML logic.**
2. **Copy/adapt production-worthy spike classes instead of rewriting them.**
3. **Do not port spike-only tables or mock data** (ScenarioDao, MockCandidate, edge_case_rule, mock_scenario).
4. **Do not build a new renderer from scratch.**
5. **Do not use browser-only CSS in PDF templates** (see CSS limitations in Technical Context).
6. **Do not delete the legacy renderer**; mark it `@Deprecated` and route around it.
7. **Do not change budget rules blindly.** Use existing production `WorkExperienceBudgetResolver`.
8. **If confidence is below 70%, stop and ask.**
9. **If spike behavior and current app behavior conflict, stop and report the conflict.**

---

## Project Structure

### Documentation

```text
specs/008-pdf-generation/
├── spec.md
├── plan.md              ← this file
├── tasks.md
├── research.md
├── data-model.md
├── quickstart.md
├── memory-synthesis.md
├── contracts/
├── checklists/
└── spec_input_files/
    └── pdf-spike-openhtmltopdf-v12-final/
```

### Backend — New/Updated Structure

```text
backend/src/main/java/com/resumainer/
├── controller/
│   ├── GenerateResumeController.java        # Update: finalize/export endpoints
│   ├── ResumeDownloadController.java        # New: PDF/HTML download + public route
│   └── PublicResumeController.java          # New: GET /{username}/{publicCode}
├── dao/
│   ├── ResumeBudgetConfigDao.java           # Existing; reuse
│   ├── SavedResumeDao.java                  # Update: PDF metadata columns
│   ├── GenerationResponseDao.java           # Update: bullet read/write
│   ├── GenerationResponseExperienceDao.java # Update: bullet linkage
│   ├── GenerationResponseProjectDao.java    # Update: bullet linkage
│   └── PdfRenderConfigDao.java              # New: fit limits + fill targets
├── dto/generate/
│   ├── GenerationReviewDto.java             # Update: bullet fields
│   ├── GenerationReviewUpdateDto.java       # Update: bullet update keys
│   ├── ExportResultDto.java                 # Update: pdfAvailable, pdf/HTML URLs
│   └── SavedResumeExportDto.java            # Update: PDF/public metadata
├── model/
│   ├── GenerationResponseExperience.java    # Update: bullet list
│   ├── GenerationResponseProject.java       # Update: bullet list
│   ├── GenerationResponseExperienceBullet.java  # New
│   ├── GenerationResponseProjectBullet.java     # New
│   ├── SavedResume.java                     # Update: pdf_* fields
│   └── pdf/                                 # Ported spike models (renamed)
│       ├── ResumeRenderData.java
│       ├── PagePlan.java
│       ├── FitLimits.java
│       ├── FitState.java
│       ├── FillTarget.java
│       ├── FitAttempt.java
│       ├── FitResult.java
│       └── PdfMetrics.java
├── service/
│   ├── AiResponseParser.java                # Update: bulletPoints parsing
│   ├── AiResponseValidator.java             # Update: bullet validation
│   ├── GenerationResponsePersistenceService.java  # Update: bullet persistence
│   ├── ResumeReviewService.java             # Update: bullet edit/save
│   ├── ResumeFinalizeService.java           # Update: real finalization flow
│   ├── ResumeTemplateRenderer.java          # @Deprecated — not used in new flow
│   ├── PdfGenerationService.java            # Existing interface → real impl
│   ├── OpenHtmlPdfGenerationService.java    # New: real implementation
│   ├── PdfRenderConfigService.java          # New
│   └── pdf/                                 # Ported spike engine
│       ├── XhtmlTemplateRenderer.java
│       ├── OpenHtmlPdfRenderer.java
│       ├── PdfAnalyzer.java
│       ├── PdfValidationService.java
│       ├── ContentExpectationBuilder.java
│       ├── CssSafetyInspector.java
│       ├── FeedbackFitEngine.java
│       ├── PdfBlankPageCleaner.java
│       └── PdfPageMerger.java
└── util/
    └── HtmlEscapeUtil.java                  # New: HTML-escape user text for templates
```

### Backend — Migrations

Inspect current highest Flyway migration first. Do not assume version numbers.

Expected migrations:

```text
V{NEXT}__add_generation_response_bullet_tables.sql       # If not already present
V{NEXT+1}__create_pdf_render_config_tables.sql            # resume_pdf_fit_limits + resume_pdf_fill_targets
V{NEXT+2}__update_saved_resume_pdf_metadata.sql           # pdf_status, pdf_file_path, pdf_page_count, etc.
V{NEXT+3}__seed_pdf_render_config.sql                     # Active default from spike V12.1 values
```

Additive only. Do not modify already-applied migrations.

### Frontend — Expected Updates

```text
frontend/src/
├── components/generate/
│   ├── GeneratedRecordGroup.vue      # Update: bullet editing fields
│   ├── ReviewStepForm.vue            # Update: bullet display/save
│   └── ExportResult.vue              # Update: real PDF/HTML links + pdfAvailable
├── services/
│   └── generateResumeService.ts      # Update: download/open PDF + HTML
├── types/
│   └── generate.ts                   # Update: bullet DTO + PDF metadata types
└── views/generate/
    ├── GenerateReviewPage.vue        # Update: bullet editing integration
    └── GenerateExportPage.vue        # Update: loading screen during finalization
```

---

## Phase Group 1: Bullet Points + Review + Prompt/Parser Hardening

### Design Goals

- Make bullets first-class structured data.
- MVP editing: edit existing bullets only.
- Keep generated description/paragraph fields compatible.
- Prompt, parser, persistence, Review API, frontend, final renderer agree on `bulletPoints`.
- Changes minimal and traceable.

### Schema Design

Inspect existing generation response tables first. If missing, add:

```sql
generation_response_experience_bullet (
  id BIGSERIAL PRIMARY KEY,                                          -- D7: BIGSERIAL for lookup tables
  experience_id BIGINT NOT NULL REFERENCES generation_response_experience(id) ON DELETE CASCADE,
  bullet_order INT NOT NULL,
  bullet_text VARCHAR(250) NOT NULL CHECK (TRIM(bullet_text) <> ''),  -- FR-008-004: non-empty
  is_edited BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (experience_id, bullet_order)
)

generation_response_project_bullet (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES generation_response_project(id) ON DELETE CASCADE,
  bullet_order INT NOT NULL,
  bullet_text VARCHAR(250) NOT NULL CHECK (TRIM(bullet_text) <> ''),
  is_edited BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (project_id, bullet_order)
)
```

Verify FK types match referenced PKs (B15 guard).

### Parser and Validator

- Parser expects camelCase `bulletPoints` array in AI response JSON.
- Validator rejects null/empty/missing bullet lists where bullets are required.
- Validator enforces max 250 chars per bullet.
- Reject non-array or malformed `bulletPoints`.

### Review API

- `GenerationReviewDto` includes bullet list under each work/project record.
- Backend-generated opaque update keys for bullets (D27 pattern).
- Save marks `is_edited = true` on changed bullets.
- Reject empty/whitespace-only bullets.

### Frontend

- Under each work/project record, render bullets as separate editable text inputs.
- Record-first grouping preserved.
- No add/delete/reorder for MVP (FR-008-011).
- Empty bullet validation (frontend + backend, dual validation per Constitution III).
- Unsaved changes warning preserved.

### Prompt Design

Update prompt config (DB-backed) so AI is instructed to return `bulletPoints` arrays:

```json
"bulletPoints": ["short action/result bullet", "..."]
```

Rules: max 15 words/bullet target, max 250 chars/bullet hard limit, no fabricated facts, preserve source IDs, use profile-owned data, do not rewrite factual education.

---

## Phase Group 2: PDF/HTML Generation from Approved Spike

### Design Goals

- PDF is the canonical final artifact.
- HTML download matches PDF layout (page split, notes, section order).
- OpenHTMLToPDF + PDFBox as in spike.
- Reuse existing production budget resolver (not spike `edge_case_rule`).
- Legacy HTML renderer kept but removed from new finalization path.

### Production-Worthy Spike Classes to Port

Port/adapt from spike — do NOT rewrite:

```text
service/pdf/XhtmlTemplateRenderer.java      # HTML/XHTML template rendering
service/pdf/OpenHtmlPdfRenderer.java        # PDF generation via OpenHTMLToPDF
service/pdf/PdfAnalyzer.java                # Page count + text extraction
service/pdf/PdfValidationService.java       # Content + page count validation
service/pdf/ContentExpectationBuilder.java  # Required content expectations
service/pdf/CssSafetyInspector.java         # Reject browser-only CSS
service/pdf/FeedbackFitEngine.java          # Iterative font/gap fitting
service/pdf/PdfBlankPageCleaner.java        # Remove trailing blank pages
service/pdf/PdfPageMerger.java              # Multi-page PDF assembly
service/pdf/PagePlanBuilder.java            # Page allocation from budget

model/pdf/ResumeRenderData.java             # Immutable render input (rename from spike ResumeData)
model/pdf/PagePlan.java
model/pdf/FitLimits.java
model/pdf/FitState.java
model/pdf/FillTarget.java
model/pdf/FitAttempt.java
model/pdf/FitResult.java
model/pdf/PdfMetrics.java
```

### Spike-Only Code NOT to Port

```text
dao/ScenarioDao.java
model/ResumeDataFactory.java
model/MockCandidate.java
model/Scenario.java
budget/EdgeCaseRuleProvider.java (as production source)
SQLite schema + seed.sql
SpikeRunner.java (standalone batch runner)
output-edge/ mock artifacts
```

### Budget / Breakpoint Integration

Production must use existing config, NOT spike edge_case_rule:

```
finalized response + profile data
  → ResumeRenderData
  → WorkExperienceBudgetResolver / production budget config
  → PagePlan
  → XhtmlTemplateRenderer → HTML
  → OpenHtmlPdfRenderer → PDF
  → PdfAnalyzer + PdfValidationService → validation
  → FeedbackFitEngine → iterative fitting
  → PdfBlankPageCleaner → cleanup
  → SavedResume metadata commit + file promotion
```

### PDF Fit Config

Add production DB-backed PDF fit config:

```sql
resume_pdf_fit_limits (
  id BIGSERIAL PRIMARY KEY,
  config_key VARCHAR(100) NOT NULL UNIQUE,
  active BOOLEAN NOT NULL DEFAULT FALSE,
  body_font_min_px NUMERIC(5,2) NOT NULL,
  body_font_max_px NUMERIC(5,2) NOT NULL,
  line_height_min NUMERIC(5,2) NOT NULL,
  line_height_max NUMERIC(5,2) NOT NULL,
  section_gap_min_px NUMERIC(5,2) NOT NULL,
  section_gap_max_px NUMERIC(5,2) NOT NULL,
  item_gap_min_px NUMERIC(5,2) NOT NULL,
  item_gap_max_px NUMERIC(5,2) NOT NULL,
  paragraph_gap_min_px NUMERIC(5,2) NOT NULL,
  paragraph_gap_max_px NUMERIC(5,2) NOT NULL,
  bullet_gap_min_px NUMERIC(5,2) NOT NULL,
  bullet_gap_max_px NUMERIC(5,2) NOT NULL,
  max_attempts INT NOT NULL,
  page2_delta_limit_percent NUMERIC(5,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)

resume_pdf_fill_targets (
  id BIGSERIAL PRIMARY KEY,
  fit_limits_id BIGINT NOT NULL REFERENCES resume_pdf_fit_limits(id),
  target_page_count INT NOT NULL,
  page_number INT NOT NULL,
  language_code VARCHAR(10),
  project_count_min INT,
  project_count_max INT,
  min_fill NUMERIC(5,4) NOT NULL,
  max_fill NUMERIC(5,4) NOT NULL,
  priority INT NOT NULL DEFAULT 100,
  UNIQUE (fit_limits_id, target_page_count, page_number, language_code, project_count_min, project_count_max, priority)
)
```

Seed with active default from spike V12.1 values including adaptive page2 min-fill: 0 projects can go as low as 0.30.

### PDF/HTML Parity

Both PDF and HTML generated from same XHTML template. Notes:

```text
Page 1 footer: SEE THE NEXT PAGE / СМ. СЛЕДУЮЩУЮ СТРАНИЦУ
Page 2+ header: SEE THE PREVIOUS PAGE / СМ. ПРЕДЫДУЩУЮ СТРАНИЦУ
```

Use explicit A4 page height for absolute footer note positioning.

### Saved Resume PDF Metadata

Add columns to `saved_resumes` (only if missing):

```sql
pdf_status VARCHAR(50),                           -- PENDING, GENERATING, READY, FAILED
pdf_file_path VARCHAR(500),                       -- Relative path, never exposed raw
pdf_generated_at TIMESTAMP,
pdf_generation_error_code VARCHAR(100),
pdf_generation_error_message VARCHAR(500),
pdf_render_profile VARCHAR(100),                  -- config_key reference
pdf_page_count INT,                               -- Use Integer (boxed) — B9 guard
```

### Legacy Renderer Rule

Do not delete existing `ResumeTemplateRenderer`. Add:

```java
/**
 * @deprecated Replaced by PDF/HTML parity renderer in feat/008.
 * Kept as legacy reference/fallback only. Do not use in new finalization flow.
 */
@Deprecated
```

---

## API Contracts

### Finalize

```http
POST /api/generate/requests/{requestId}/finalize
```

- Owner-scoped.
- Selected adaptation level required.
- Sets request status to FINALIZING (blocks concurrent — FR-008-028-2).
- Generates parity HTML + PDF in staging directory.
- Validates PDF (page count, required content, text extraction, fill, blank pages).
- On success: promotes files to final storage, commits `saved_resume` with metadata, returns export DTO.
- On failure: deletes staged files, rolls back DB, resets request status, returns error DTO with fitting failure message (FR-008-028-1).
- Bilingual: atomic — both languages succeed or neither saved (FR-008-029).

### Export

```http
GET /api/generate/requests/{requestId}/export
```

Export DTO exposes real PDF/HTML availability:

```json
{
  "resumes": [
    {
      "savedResumeId": 123,
      "languageCode": "en",
      "adaptationLevel": "BALANCED",
      "htmlDownloadUrl": "/api/generate/resumes/123/html",
      "pdfDownloadUrl": "/api/generate/resumes/123/pdf",
      "pdfOpenUrl": "/api/generate/resumes/123/pdf?disposition=inline",
      "publicUrlLink": "/{username}/{publicCode}",
      "pdfAvailable": true,
      "pdfMessage": null,
      "coverLetter": "..."
    }
  ]
}
```

No raw filesystem paths.

### Authenticated Downloads

```http
GET /api/generate/resumes/{savedResumeId}/html   → text/html; charset=UTF-8
GET /api/generate/resumes/{savedResumeId}/pdf    → application/pdf (attachment)
GET /api/generate/resumes/{savedResumeId}/pdf?disposition=inline  → application/pdf (inline)
```

Owner-scoped. New parity HTML served, not legacy renderer output.

### Public PDF Route

```http
GET /{username}/{publicCode}
```

**Route pattern**: `/{username}/{publicCode}` per BA documentation. Public codes are generated by existing `PublicCodeGenerator` (SecureRandom, 5-char with collision retry → 8-char fallback, ambiguous chars excluded). Reserved usernames must be prevented from registration to avoid URL squatting. Existing placeholder at `GET /candidate/{publicCode}` in Feature 007 is replaced with this new route.

- Public code generated by existing `PublicCodeGenerator` (SecureRandom, 5-char with collision retry → 8-char fallback, ambiguous chars excluded).
- `SavedResumeDao.findPublicCodeByCode()` provides uniqueness check — already implemented.
- `public_code` column already exists via V21 migration.
- No authentication.
- PDF only, inline disposition.
- No cover letter, no private HTML.
- Invalid/deleted/disabled → 404 (no metadata leakage).
- Username changes → regenerate public codes for affected saved resumes to invalidate old URLs.

### Security Mitigations for Download & Public Route (per security review SEC-001–SEC-003)

**SEC-001 — Path Traversal Prevention**:
- All authenticated PDF/HTML download controllers MUST validate that the resolved file path is within the configured storage directory root before reading.
- Use `Path.resolve(storageRoot, relativePath).normalize()` then verify `resolved.startsWith(storageRoot)`.
- If validation fails → return 404 (not 500, to avoid path disclosure).
- `GeneratedFileStorageService` already sanitizes path segments; extend this pattern to the download path.

**SEC-002 — Public Code Generation** (already implemented):
- `PublicCodeGenerator` uses `java.security.SecureRandom` with 28-character alphabet (ambiguous chars excluded).
- Default 5 chars → ~17M combinations; collision retry → 8-char fallback → ~378B combinations.
- Combined with rate limiting (SEC-003), brute-force risk is mitigated.
- No change needed — reuse existing infrastructure.

**SEC-003 — Rate Limiting on Public Route**:
- Apply IP-based rate limiting on `GET /{username}/{publicCode}`.
- Limit: 30 requests per minute per IP (generous for legitimate recruiter access, restrictive for brute-force).
- Return HTTP 429 (Too Many Requests) with `Retry-After` header when exceeded.
- Reuse existing rate-limiting pattern from Feature 003 login rate limiter if available.
- Add small artificial delay (~200ms) on 404 responses to slow enumeration.
- Log repeated 429/404 patterns at WARN level for monitoring.

---

## Finalization Transaction / Compensation Design

File generation + DB commits cannot be one transaction. Use explicit compensation:

1. Validate owner + selected level.
2. Set request status → FINALIZING.
3. Load finalized response + profile + budget data.
4. Generate HTML + PDF into staging directory.
5. Validate PDF expectations.
6. Start DB transaction.
7. Insert/update `saved_resume` rows with artifact metadata (INCLUDE all columns — B24 guard).
8. Commit DB transaction.
9. Move staged artifacts to final storage.
10. On failure before DB commit: delete staged files, reset request status.
11. On bilingual failure: cleanup both languages, save neither.
12. Catch `Exception`, not just `SQLException` (D23 guard).

No background job queue for MVP.

---

## Logging Plan

**Log**: requestId, userId, savedResumeId, language/adaptation, budget config ID, PDF fit config key, page plan summary, fitting attempts (debug level), final selected attempt, cleanup/rollback actions, validation failure reason codes.

**Do NOT log**: API keys, raw AI prompts, full PII dumps, full resume text at info/warn/error, filesystem paths in API responses.

---

## Testing Plan

### Backend — Phase Group 1
- Parser accepts/rejects `bulletPoints` correctly.
- Persistence writes/reads bullets in order.
- Review DTO returns bullets under correct record.
- Review save updates bullet text + edited marker.
- Finalization uses edited bullets.
- Empty bullet rejected.
- Existing description fields unaffected.

### Backend — Phase Group 2
- PDF config DAO loads active fit limits + fill targets.
- Page plan uses production `WorkExperienceBudgetResolver`.
- Renderer outputs page notes (both HTML + PDF XHTML).
- CSS inspector rejects forbidden CSS.
- Fit engine: underfill/overflow/missing text per spike behavior.
- Blank page cleaner removes trailing blank pages.
- PDF validator: page count, required text, generic anchors, fill targets.
- Finalization success: saved resume with PDF/HTML paths.
- Finalization failure: rollback + file cleanup.
- Bilingual partial failure: both rolled back.
- Authenticated downloads: owner-scoped.
- Public route: PDF/404 correct.
- Legacy renderer NOT called in new flow.

### Frontend
- Review renders bullet fields under each record.
- Edit marks dirty, saves via API.
- Empty bullet validation shown.
- Export: Download PDF/Open PDF/Download HTML enabled when `pdfAvailable=true`.
- Download HTML uses real parity HTML endpoint.
- Loading screen with rotating phrases during finalization (FR-008-028-3).
- i18n strings for new labels/errors in EN/RU.

### Manual Smoke Tests
1. EN-only Minimal → review bullets → edit → finalize → PDF/HTML download.
2. RU-only Balanced — check RU page notes, final personal info line.
3. Bilingual All → finalize Balanced → two PDFs + two HTML files.
4. Dense case → exactly 2 pages.
5. Sparse page2 → accepted adaptive fill, no false failure.
6. Public link → open PDF inline without login.
7. Non-owner → forbidden/404 for private download.
8. Fitting failure → "Try again" button → Review with edits preserved.

---

## Definition of Done

- [ ] Spec Kit constitution checked and respected.
- [ ] All 41 FRs (FR-008-001 to FR-008-028-3) implemented and testable.
- [ ] `mvn clean package` passes.
- [ ] `npm run build` passes.
- [ ] Bullet points are first-class persisted/editable fields.
- [ ] Prompt builder requests `bulletPoints` arrays.
- [ ] Parser/validator rejects invalid bullets.
- [ ] Review UI edits bullets and persists them.
- [ ] Final PDF/HTML uses edited bullets.
- [ ] Approved spike engine ported/adapted, not rewritten.
- [ ] Spike-only mock DB tables NOT ported.
- [ ] Existing production budget resolver/config reused.
- [ ] PDF fit config DB-backed.
- [ ] PDF and HTML artifacts match layout/page split/notes.
- [ ] Legacy renderer deprecated, unused in new finalization.
- [ ] Authenticated PDF/HTML download works, owner-scoped.
- [ ] Public PDF route works.
- [ ] FINALIZING status prevents concurrent finalization.
- [ ] Loading screen with rotating phrases during finalization.
- [ ] Fitting failure: "Try again" → Review, edits preserved.
- [ ] All user-editable text HTML-escaped before template insertion.
- [ ] Logs detailed and safe.
- [ ] No fake PDF placeholders for finalized resumes.
- [ ] No raw filesystem paths exposed.
- [ ] Path traversal protection in download controllers (SEC-001): resolved path validated against storage root.
- [ ] Rate limiting on public PDF route (SEC-003): 30 req/min/IP, HTTP 429, 200ms 404 delay.

---

## Human Review Gates

1. **After Phase Group 1 schema inspection**: confirm bullet tables exist or new migration needed.
2. **After Phase Group 1 parser/review DTO contract**: confirm frontend contract before UI changes.
3. **Before Phase Group 2 migrations**: review PDF fit config table names + saved resume metadata fields.
4. **After porting spike renderer, before finalization integration**: run spike-equivalent tests.
5. **Before changing public route path**: confirm no conflict with Vue/Nginx/backend routing (`/{username}/{publicCode}`).
6. **Before deleting anything**: do not delete legacy renderer or budget rules without explicit approval.

---

## OpenCode Safety Reminders

- Do not use Spring Boot.
- Do not use JPA/Hibernate/ORM.
- Do not concatenate SQL.
- Do not call real OpenRouter in automated tests.
- Do not create fake PDF files.
- Do not silently ignore PDF validation failures.
- Do not skip cleanup on failure.
- Do not port spike mock tables.
- Do not invent a new PDF engine.
- Do not remove the legacy HTML renderer.
- Do not bypass existing budget config/resolver.
- Do not hide backend bugs with frontend mocks.
- HTML-escape all user text entering templates (FR-008-023-1).
- Catch Exception, not just SQLException, in compensation blocks (D23).
- Use Long (boxed) for nullable PDF metadata (B9).
- Audit all DAO INSERT for new column inclusion (B24).
