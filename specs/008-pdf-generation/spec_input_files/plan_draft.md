# Implementation Plan: PDF/HTML Resume Export and Bullet-Point Review Hardening

**Branch**: `feat/008-pdf-generation` | **Date**: 2026-06-20 | **Spec**: [spec_draft.md](spec_draft.md)

**Input**: Feature specification from `specs/008-pdf-generation/spec.md`, current `feat/007-resume-generation` codebase, and approved PDF spike from `specs/008-pdf-generation/spec_input_files/pdf-spike-openhtmltopdf-v12-final`.

---

## Summary

Implement production-ready PDF and matching HTML resume export for ResumAIner, plus prerequisite bullet-point persistence/review hardening.

The implementation is organized into two Phase Groups:

1. **Phase Group 1 — Bullet Points + Review + Prompt/Parser Hardening**
   - Verify or add structured bullet-point persistence for generated work experience and projects.
   - Update parser, validator, prompt builder, DAO/service layer, Review API, and frontend Review page so bullets are first-class editable fields.
   - Ensure finalization consumes edited bullets.

2. **Phase Group 2 — PDF/HTML Generation from Approved Spike**
   - Port the approved OpenHTMLToPDF spike into the main backend.
   - Generate strict 1-page/2-page PDFs with validation and fitting.
   - Generate matching PDF-parity HTML and make Download HTML use it.
   - Preserve legacy HTML renderer as deprecated/unused fallback/reference.
   - Add authenticated PDF download and public PDF route if in MVP scope.

**Critical rule**: the PDF/HTML engine MUST be ported from the spike. Do not invent a new fitting engine or layout. DeepSeek/OpenCode must inspect and copy/adapt the spike classes wherever possible.

---

## Technical Context

**Language/Version**: Java 21 LTS

**Backend Framework**: Spring MVC 6.x, Jakarta EE 10, no Spring Boot

**Frontend**: Vue 3, TypeScript, Vite, PrimeVue 4, vue-router, vue-i18n

**Database**: PostgreSQL 17, Flyway migrations, plain JDBC with existing custom connection pool

**Persistence Style**: DAO layer with `PreparedStatement` only; no ORM, no JPA, no Hibernate

**PDF Library**: OpenHTMLToPDF + PDFBox, ported from approved spike

**Existing Production Budget Source**:

- `ResumeBudgetConfigDao`
- `ResumeBudgetConfigService`
- `WorkExperienceBudgetResolver`
- `resume_budget_configs`
- `resume_template_selection_rules`
- `resume_work_experience_distribution_rules`
- `resume_section_budget_rules`

**Approved Spike Path**:

```text
specs/008-pdf-generation/spec_input_files/pdf-spike-openhtmltopdf-v12-final
```

**Prototype Output Path for Reference**:

```text
specs/008-pdf-generation/spec_input_files/pdf-spike-openhtmltopdf-v12-final/output-edge
```

**Testing**:

- Backend: JUnit 5 + Mockito + useful integration-style service tests where practical
- Frontend: Vue Test Utils/Vitest where existing project uses them, plus manual smoke tests
- Target: 80% coverage for new/modified feature code, but no fake tests purely for coverage

**Quality Constraints**:

- KISS: keep code simple, explicit, professional
- detailed diagnostics logging, but no secrets/PII leaks
- no user-supplied raw paths
- UTF-8 everywhere
- no browser-only CSS in PDF XHTML
- no real OpenRouter calls in automated tests

---

## Constitution Check

*GATE: Must pass before task breakdown. Re-check after major design changes and before final handoff.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | ✅ Pass if implemented as planned | Layered architecture: controller/service/dao/model/dto/util/config. PDF engine classes are ported from spike into dedicated package. Legacy renderer remains but is marked deprecated and excluded from new flow. KISS required. |
| **II. Testing Excellence** | ✅ Pass if TDD followed | TDD required for parser, bullet persistence, review saving, PDF fitting, validation, finalization rollback, download endpoints. Target 80% useful coverage for new/modified feature code. |
| **III. User Experience Consistency** | ✅ Pass if UI remains aligned | Review supports bullet editing. Export actions work. HTML/PDF parity. EN/RU i18n for user-facing text. No hardcoded user-facing UI strings. |
| **IV. Performance & Reliability** | ✅ Pass if spike safeguards ported | PreparedStatement SQL. Transaction/compensation for file + DB finalization. PDF fit attempts bounded by config. Detailed logs for fitting/debug. |
| **V. Security by Design** | ✅ Pass if enforced | Owner-scoped downloads. Public route serves only PDF for active public resume. No raw paths, no secrets, no PII-heavy logs. Invalid/deleted public links return 404. |

### Complexity Justification

The feature is complex because PDF fitting is inherently layout-sensitive. Complexity is controlled by porting the proven spike instead of inventing new logic. No unapproved architectural experiments are allowed.

---

## Non-Negotiable Porting Rules

1. **Inspect the spike before coding PDF/HTML logic.**
2. **Copy/adapt production-worthy spike classes instead of rewriting them.**
3. **Do not port spike-only tables or mock data.**
4. **Do not build a new renderer from scratch.**
5. **Do not use browser-only CSS in PDF templates.**
6. **Do not delete the legacy renderer; mark it deprecated and route around it.**
7. **Do not change budget rules blindly. Use existing production budget config/resolver.**
8. **If confidence is below 70%, stop and ask.**
9. **If spike behavior and current app behavior conflict, stop and report the conflict.**

These rules exist because prior weak implementation drifted away from the prototype and caused extensive rework.

---

## Project Structure

### Documentation

```text
specs/008-pdf-generation/
├── spec.md
├── plan.md
├── tasks.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
├── checklists/
└── spec_input_files/
    └── pdf-spike-openhtmltopdf-v12-final/
        ├── src/
        ├── output-edge/
        ├── work/pdf-spike-edge.sqlite
        ├── README.md
        ├── RUN_NOTES.md
        └── TRANSFER_TO_MAIN_PROJECT.md
```

### Backend — Expected New/Updated Structure

```text
backend/src/main/java/com/resumainer/
├── controller/
│   ├── GenerateResumeController.java        # Update PDF/HTML export endpoints
│   └── ResumeDownloadController.java        # Update/add public PDF route if existing pattern fits
├── dao/
│   ├── ResumeBudgetConfigDao.java           # Existing; reuse
│   ├── SavedResumeDao.java                  # Update for PDF metadata
│   ├── GenerationResponseDao.java           # Update/read bullets if missing
│   ├── GenerationResponsePersonalDao.java   # Reuse/update as needed
│   └── PdfRenderConfigDao.java              # New, for PDF fit/fill config
├── dto/generate/
│   ├── GenerationReviewDto.java             # Update with bullet fields
│   ├── GenerationReviewUpdateDto.java       # Update for bullet update keys
│   ├── ExportResultDto.java                 # Update PDF available/URLs
│   └── SavedResumeExportDto.java            # Update PDF/HTML/public URLs
├── model/
│   ├── GenerationResponseExperience.java    # Update or link bullets
│   ├── GenerationResponseProject.java       # Update or link bullets
│   ├── GenerationResponseExperienceBullet.java # New if missing
│   ├── GenerationResponseProjectBullet.java    # New if missing
│   ├── SavedResume.java                     # Update PDF/public metadata
│   └── pdf/ or render/ models               # Ported spike records, production names
├── service/
│   ├── AiResponseParser.java                # Update bullet parsing
│   ├── AiResponseValidator.java             # Update bullet validation
│   ├── GenerationResponsePersistenceService.java # Persist bullets transactionally
│   ├── ResumeReviewService.java             # Review bullet edits
│   ├── ResumeFinalizeService.java           # New PDF/HTML finalization flow
│   ├── ResumeTemplateRenderer.java          # Mark deprecated/legacy, no new finalization usage
│   ├── PdfGenerationService.java            # Existing boundary, now real impl
│   ├── OpenHtmlPdfGenerationService.java    # New real implementation
│   ├── PdfRenderConfigService.java          # New
│   └── pdf/ or render/                      # Ported spike engine classes
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
    └── PublicCodeGenerator.java             # Reuse/update for public route rules
```

### Backend — Migrations

Inspect current highest Flyway migration first. Do not assume version numbers.

Likely new migrations:

```text
V{NEXT}__add_generated_bullet_tables_if_missing.sql
V{NEXT+1}__create_pdf_render_config_tables.sql
V{NEXT+2}__update_saved_resume_pdf_metadata.sql
V{NEXT+3}__seed_pdf_render_config.sql
```

Do not modify already-applied migrations unless the project workflow explicitly allows it. Prefer additive migrations.

### Frontend — Expected Updates

```text
frontend/src/
├── components/generate/
│   ├── GeneratedRecordGroup.vue      # Update bullet editing
│   ├── ReviewStepForm.vue            # Update bullet display/save
│   └── ExportResult.vue              # Enable real PDF/HTML links
├── services/
│   └── generateResumeService.ts      # Real download/open PDF + HTML
├── types/
│   └── generate.ts                   # Add bullet DTO fields and PDF metadata
└── views/generate/
    ├── GenerateReviewPage.vue
    └── GenerateExportPage.vue
```

---

## Phase Group 1: Bullet Points + Review + Prompt/Parser Hardening

### Design Goals

- Make bullets first-class structured data.
- Keep MVP editing simple: edit existing bullets only.
- Avoid breaking existing generated description/paragraph fields.
- Ensure prompt, parser, persistence, review API, frontend, and final renderer all agree on `bulletPoints`.
- Keep changes minimal and traceable.

### Backend Design

1. Inspect existing generation response tables.
2. If bullet tables already exist, reuse them.
3. If missing, add normalized bullet tables:

```sql
-- Example names only; inspect existing naming conventions first.
generation_response_experience_bullet (
  id BIGSERIAL PRIMARY KEY,
  experience_id BIGINT NOT NULL REFERENCES generation_response_experience(id) ON DELETE CASCADE,
  bullet_order INT NOT NULL,
  bullet_text VARCHAR(250) NOT NULL,
  is_edited BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (experience_id, bullet_order)
)

generation_response_project_bullet (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES generation_response_project(id) ON DELETE CASCADE,
  bullet_order INT NOT NULL,
  bullet_text VARCHAR(250) NOT NULL,
  is_edited BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (project_id, bullet_order)
)
```

4. Parser expects camelCase `bulletPoints`.
5. Validator rejects null/empty bullet list where bullets are required.
6. Review update keys must support bullets without frontend manually constructing database IDs incorrectly.
7. Save review edits transactionally and mark edited bullets.

### Frontend Design

- Under each work/project record, render bullet fields as separate textareas or inputs.
- Keep record-first grouping.
- No add/delete/reorder for MVP.
- Prevent empty bullet save.
- Preserve unsaved changes warning.

### Prompt Design

Update DB prompt config / prompt builder only as needed. The prompt must explicitly require:

```json
"bulletPoints": ["short action/result bullet", "..."]
```

Rules:

- max 15 words per bullet target;
- max 250 characters per bullet hard validation;
- no fabricated facts;
- preserve source IDs if already used in current system;
- use profile-owned data where required;
- do not rewrite factual education.

Do not hardcode these rules only in Java if the project already has DB-backed prompt config. Prefer prompt config migration/seed update plus tests proving prompt output includes the rule.

---

## Phase Group 2: PDF/HTML Generation from Approved Spike

### Design Goals

- PDF is the canonical final artifact.
- HTML download must match PDF layout.
- Use OpenHTMLToPDF and PDFBox as in spike.
- Preserve page split and notes.
- Reuse existing production budget resolver.
- Keep legacy HTML renderer but remove it from the new finalization path.

### Production-Worthy Spike Classes to Port

Port/adapt these from the spike. Do not rewrite from scratch:

```text
render/XhtmlTemplateRenderer.java
pdf/OpenHtmlPdfRenderer.java
pdf/PdfAnalyzer.java
pdf/PdfValidationService.java
pdf/ContentExpectationBuilder.java
pdf/CssSafetyInspector.java
pdf/FeedbackFitEngine.java
pdf/PdfBlankPageCleaner.java
pdf/PdfPageMerger.java
plan/PagePlanBuilder.java
model/ResumeData.java          -> rename to ResumeRenderData if desired
model/PagePlan.java
model/FitLimits.java
model/FitState.java
model/FillTarget.java
model/FitAttempt.java
model/FitResult.java
model/PdfMetrics.java
```

### Spike-Only Code Not to Port

Do not port these as production components:

```text
dao/ScenarioDao.java
model/ResumeDataFactory.java
model/MockCandidate.java
model/Scenario.java
budget/EdgeCaseRuleProvider.java as production source
SQLite schema mock tables
standalone batch runner behavior from SpikeRunner.java
output-edge mock artifacts as runtime assets
```

They may be used only as reference/test fixture ideas.

### Budget / Breakpoint Integration

The spike uses `edge_case_rule` for deterministic edge cases. Production must use existing project config:

```text
ResumeBudgetConfigDao
ResumeBudgetConfigService
WorkExperienceBudgetResolver
resume_budget_configs
resume_template_selection_rules
resume_work_experience_distribution_rules
resume_section_budget_rules
```

Implementation shape:

```text
finalized response + profile data
→ ResumeRenderData
→ WorkExperienceBudgetResolver / production budget config
→ PagePlan
→ XhtmlTemplateRenderer
→ FeedbackFitEngine
→ PDF + parity HTML
→ validation
→ SavedResume metadata commit
```

### PDF Fit Config

Add production DB-backed PDF fit config because current budget config is content-distribution oriented, not rendering-fit oriented.

Suggested tables:

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

Adjust names to project conventions. Keep config simple.

### PDF/HTML Parity

The new renderer must generate:

- final PDF;
- matching downloadable HTML.

Both must include consistent notes:

```text
Page 1 footer: SEE THE NEXT PAGE / СМ. СЛЕДУЮЩУЮ СТРАНИЦУ
Page 2+ header: SEE THE PREVIOUS PAGE / СМ. ПРЕДЫДУЩУЮ СТРАНИЦУ
```

Use the V12.1 notes fix from spike: explicit A4 page height and consistent note styling.

### Legacy Renderer Rule

Existing `ResumeTemplateRenderer` or equivalent must not be deleted.

Add clear Javadoc/comment:

```java
/**
 * @deprecated Replaced by the PDF/HTML parity renderer introduced in feat/008.
 * Kept as legacy reference/fallback only. Do not use in new finalization flow.
 */
@Deprecated
```

Then update finalization to use the new renderer. The Download HTML endpoint must serve the new parity HTML file.

---

## API Contracts

### Finalize

```http
POST /api/generate/requests/{requestId}/finalize
```

Behavior:

- owner-scoped;
- selected adaptation level required;
- saves one resume for single-language mode;
- saves two resumes for bilingual mode;
- generates parity HTML and PDF before/within safe finalization flow;
- if any artifact generation fails, rollback and cleanup;
- returns export DTO.

### Export

```http
GET /api/generate/requests/{requestId}/export
```

Export DTO now must expose real PDF/HTML availability:

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

Do not expose server filesystem paths.

### Authenticated HTML Download

```http
GET /api/generate/resumes/{savedResumeId}/html
```

- owner-scoped;
- returns new parity HTML;
- content type `text/html; charset=UTF-8`;
- no raw path exposure.

### Authenticated PDF Download/Open

```http
GET /api/generate/resumes/{savedResumeId}/pdf
GET /api/generate/resumes/{savedResumeId}/pdf?disposition=inline
```

- owner-scoped;
- `attachment` for download by default;
- `inline` for open if requested/approved;
- content type `application/pdf`.

### Public PDF Route

Preferred accepted route:

```http
GET /{username}/{publicCode}
```

Rules:

- no login;
- PDF only;
- no cover letter;
- inline disposition;
- invalid/deleted/disabled returns 404;
- no private HTML;
- no raw path;
- reserved usernames forbidden;
- username changes update public paths for saved resumes if public URL paths are stored.

If current router conflicts make this risky, stop and ask before implementing a different public path.

---

## Finalization Transaction / Compensation Design

Because file generation and DB commits cannot be one single database transaction, use explicit compensation:

1. Validate request owner and selected level.
2. Load finalized response/profile/budget data.
3. Generate HTML/PDF into staging/temp directory.
4. Validate PDF and HTML expectations.
5. Start DB transaction.
6. Insert/update saved resume rows with artifact metadata.
7. Commit DB transaction.
8. Move/promote staged artifacts to final storage if needed.
9. On any failure before DB commit: delete staged files.
10. On DB failure after file writes: delete staged files.
11. On bilingual failure: cleanup both languages and save neither.

Keep this simple. Do not add a background job queue unless explicitly approved.

---

## Logging Plan

Detailed logs are required for debugging, but logs must be safe.

Log:

- requestId, userId, savedResumeId;
- selected language/adaptation;
- budget config ID/version;
- PDF fit config ID/key;
- page plan summary: target pages, page1 work count, page2 work count, project count;
- each fitting attempt at debug level: attempt number, font, line-height, gaps, page count, fill, validation reason;
- final selected attempt;
- artifact paths as internal debug logs only, never frontend response;
- cleanup/rollback actions;
- validation failures with reason codes.

Do not log:

- API keys;
- raw complete AI prompts;
- full PII-heavy profile dumps;
- full resume text at info/warn/error level;
- filesystem paths in API responses.

---

## Testing Plan

### Backend Tests — Phase Group 1

- Parser accepts `bulletPoints` arrays.
- Parser rejects missing/empty bullet points where required.
- Persistence writes bullet rows in order.
- Review DTO returns bullets grouped under correct record.
- Review save updates bullet text and edited marker.
- Finalization uses edited bullet text.
- Empty bullet save rejected.
- Existing description fields remain compatible.

### Backend Tests — Phase Group 2

- PDF config DAO loads active fit limits and fill targets.
- Page plan uses existing production `WorkExperienceBudgetResolver`.
- Renderer outputs page notes in both HTML and PDF source XHTML.
- CSS safety inspector rejects forbidden CSS tokens.
- Fit engine handles underfill/overflow/missing text according to spike behavior.
- Blank page cleaner removes trailing blank page.
- PDF validator checks page count, required text, generic anchors, and fill targets.
- Finalization success creates saved resume with PDF and HTML paths.
- Finalization failure rolls back DB and deletes files.
- Bilingual partial failure rolls back both languages.
- Authenticated PDF/HTML downloads are owner-scoped.
- Public PDF route returns PDF/404 correctly.
- Legacy renderer is not called in new finalization flow.

### Frontend Tests

- Review renders bullet fields under each generated record.
- Editing bullet marks dirty state and saves through API.
- Empty bullet validation shown.
- Export card enables Download PDF / Open PDF / Download HTML when available.
- Download HTML points to real parity HTML endpoint.
- i18n strings for new labels/errors exist in EN/RU.

### Manual Smoke Tests

1. EN-only Minimal → Review bullets → edit → finalize → PDF/HTML download.
2. RU-only Balanced → check RU page notes and final personal info line.
3. Bilingual All → finalize Balanced → two PDFs + two HTML files.
4. Dense case with many work/project records → verify exactly 2 pages.
5. Sparse page2 case → verify accepted adaptive fill and no false failure.
6. Public link → open PDF inline without login.
7. Non-owner access → forbidden/404 for private download.

---

## Definition of Done

- [ ] Spec Kit constitution checked and respected.
- [ ] `mvn clean package` passes.
- [ ] `npm run build` passes.
- [ ] New/modified feature code targets 80% useful test coverage.
- [ ] Bullet points are first-class persisted/editable fields.
- [ ] Prompt builder requests `bulletPoints` arrays.
- [ ] Parser/validator rejects invalid bullets.
- [ ] Review UI edits bullets and persists them.
- [ ] Final PDF/HTML uses edited bullets.
- [ ] Approved spike engine is ported/adapted, not rewritten.
- [ ] Spike-only mock DB tables are not ported.
- [ ] Existing production budget resolver/config is reused.
- [ ] PDF fit config is DB-backed.
- [ ] PDF and HTML artifacts match in layout/page split/notes.
- [ ] Legacy renderer remains but is marked deprecated and unused in new finalization.
- [ ] Authenticated PDF/HTML download works and is owner-scoped.
- [ ] Public PDF route works or is explicitly deferred with user approval.
- [ ] Logs are detailed and safe.
- [ ] No fake PDF placeholders remain for finalized resumes.
- [ ] No raw filesystem paths exposed to frontend.

---

## Human Review Gates

1. **After Phase Group 1 schema inspection**: confirm whether bullet tables already exist or new migration is needed.
2. **After Phase Group 1 parser/review DTO contract**: confirm frontend contract before UI changes.
3. **Before Phase Group 2 migrations**: review PDF fit config table names and saved resume metadata fields.
4. **After porting spike renderer but before finalization integration**: run spike-equivalent tests.
5. **Before changing public route path**: confirm route does not conflict with Vue/Nginx/backend routing.
6. **Before deleting anything**: do not delete legacy renderer or existing budget rules without explicit approval.

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
