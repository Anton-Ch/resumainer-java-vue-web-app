---
description: "Task breakdown for PDF/HTML Resume Export and Bullet-Point Review Hardening"
---

# Tasks: PDF/HTML Resume Export and Bullet-Point Review Hardening

**Input**: Design documents from `specs/008-pdf-generation/`

**Prerequisites**: `spec.md` ✅, `plan.md` ✅, `research.md` ✅, `data-model.md` ✅, `contracts/` ✅, approved spike available ✅

**Approved Spike**: `specs/008-pdf-generation/spec_input_files/pdf-spike-openhtmltopdf-v12-final`

**Target Coverage**: 80% useful coverage for new/modified backend and frontend feature code. No superficial tests.

**Critical instruction**: Port/copy/adapt from the approved spike wherever possible. Do not invent a new PDF/HTML engine. Do not port spike-only mock tables. TDD required for all business logic.

## Constitution Compliance

Every phase MUST reference the active ResumAIner Spec Kit constitution principles:

- **I** — Code Quality & Maintainability: layered architecture, KISS, no Spring Boot/JPA/Hibernate
- **II** — Testing Excellence: TDD, useful tests, target 80% coverage for new/modified feature code
- **III** — UX Consistency: EN/RU i18n, stable Review/Export UX, PDF/HTML parity
- **IV** — Performance & Reliability: PreparedStatement, JDBC transactions, bounded fitting, UTF-8, cleanup on failure
- **V** — Security by Design: owner scope, safe public route, no secrets/PII, no raw paths, HTML escaping

## Execution Markers

| Marker | Meaning |
|---|---|
| `[P]` | Parallel — can run concurrently with other `[P]` tasks in the same phase |
| `[TDD]` | Test-driven — write failing test first, implement, verify pass |
| `[SUBAGENT]` | Can be delegated to a focused subagent |
| `[REVIEW]` | Stop for human review before proceeding |
| `[SPIKE]` | Must inspect/copy/adapt approved spike before coding |
| `[SEC]` | Security-critical task — path traversal, rate limiting, HTML escaping |

---

## Phase 0: Context, Constitution, and Prototype Loading

**Purpose**: Prevent prototype drift and implementation guesswork.

- [ ] T001 [REVIEW] Read `.specify/memory/constitution.md`. Summarize constraints relevant to this feature. Stop if any requested behavior conflicts with the constitution. (I–V)
- [ ] T002 [REVIEW] Read `specs/008-pdf-generation/spec.md`, `plan.md`, `memory-synthesis.md`, and this `tasks.md` before coding. (I)
- [ ] T003 [SPIKE] [REVIEW] Read spike `README.md`, `RUN_NOTES.md`, `TRANSFER_TO_MAIN_PROJECT.md`. Extract production-to-port and spike-only lists. (I, IV)
- [ ] T004 [SPIKE] Inspect spike source tree: identify exact classes to port (renderer, fit engine, analyzer, validator, blank page cleaner, merger, CSS inspector, page planner, fit models). (I)
- [ ] T005 [REVIEW] Inspect current backend for existing `ResumeBudgetConfigDao`, `ResumeBudgetConfigService`, `WorkExperienceBudgetResolver`, existing PDF stubs, `SavedResumeDao` fields, `PublicCodeGenerator`, `GeneratedFileStorageService`, bullet tables. Document what exists before adding migrations. (I, IV)
- [ ] T006 [REVIEW] Confirm migration numbering. Inspect highest Flyway version in `backend/src/main/resources/db/migration/`. Do not create out-of-order migrations. (IV)

**Checkpoint**: Developer knows what to port, what not to port, and what already exists.

---

## Phase Group 1 — Bullet Points + Review + Prompt/Parser Hardening

### Phase 1: PG1 Schema Inspection and Bullet Persistence

**Purpose**: Ensure bullets are first-class persisted data without duplicating existing schema.

- [x] T007 [TDD] Inspect current migrations and DB models for existing work/project bullet storage. If tables already exist, write tests against existing schema before modifying. (II, IV)
- [x] T008 [TDD] If work experience bullet table is missing, create additive Flyway migration `V{NEXT}__add_generation_response_bullet_tables.sql`. Include: `id BIGSERIAL PK`, `experience_id UUID FK → generation_response_experience(id) ON DELETE CASCADE`, `bullet_order INT NOT NULL`, `bullet_text VARCHAR(250) NOT NULL CHECK (TRIM(bullet_text) <> '')`, `is_edited BOOLEAN DEFAULT FALSE`, `created_at/updated_at TIMESTAMP`, `UNIQUE(experience_id, bullet_order)`. Verify FK type matches referenced PK — `generation_response_experience.id` is UUID (V20 migration), NOT BIGINT (B15 guard). (I, IV)
- [x] T009 [TDD] If project bullet table is missing, create addition in same migration. Include: `id BIGSERIAL PK`, `project_id UUID FK → generation_response_project(id) ON DELETE CASCADE`, same column pattern. `generation_response_project.id` is UUID (V20). (I, IV)
- [x] T010 [TDD] Create `GenerationResponseExperienceBullet` and `GenerationResponseProjectBullet` model classes in `backend/src/main/java/com/resumainer/model/`. Simple fields, no over-abstracted inheritance. Use `Integer` (boxed) for nullable fields per B9 guard. (I)
- [x] T011 [TDD] Update `GenerationResponseExperience` and `GenerationResponseProject` models to include `List<...Bullet>` field. (I)
- [x] T012 [TDD] Add DAO insert/read methods in `GenerationResponseDao` (or dedicated bullet DAO) for bullet rows in deterministic order (`ORDER BY bullet_order`). Use `PreparedStatement`. Include connection-accepting overload for transaction support (D10). (II, IV)
- [x] T013 [TDD] Add DAO tests proving: bullet round-trip (write → read = same data), order preservation, empty/whitespace-only bullet rejection, cascade delete on parent removal. (II)
- [x] T014 [TDD] Run `mvn test -pl backend` — all tests pass including new DAO tests. (II)
- [ ] T015 [REVIEW] Run Flyway migration on fresh local DB. Verify no duplicate table names, no conflict with existing generation response tables. (IV)

**Checkpoint**: DB and DAO layer can store/read work and project bullets. ✅ `mvn test` passes.

---

### Phase 2: PG1 Prompt Builder and AI Response Contract

**Purpose**: Make AI output match the structured bullet model.

- [x] T016 [TDD] Update prompt config seed/migration so AI is instructed to return `bulletPoints` arrays for work experience. Prefer DB-backed prompt config update over Java hardcode if project already has prompt config tables. (I, II)
- [x] T017 [TDD] Update prompt config seed/migration so AI is instructed to return `bulletPoints` arrays for projects. (I, II)
- [x] T018 [TDD] Update `ResumePromptBuilder` tests to assert generated prompts mention `bulletPoints`, max 15 words/bullet target, max 250 chars/bullet hard limit, no fabricated facts, use profile-owned data, preserve source IDs. (II)
- [x] T019 [TDD] Update `MockAiClient` deterministic test responses to include `bulletPoints` for EN-only, RU-only, Bilingual, and all adaptation levels (Minimal, Balanced, Maximum, All). (II)
- [x] T020 [TDD] Run `mvn test -pl backend -Dtest="ResumePromptBuilderTest,MockAiClientTest"` — all prompt and mock AI tests pass. (II)
- [x] T021 [REVIEW] Verify no automated test calls real OpenRouter. (II, V)

**Checkpoint**: Prompt and mock AI contract produce structured bullets. ✅ Tests pass.

---

### Phase 3: PG1 Parser, Validator, and Persistence

**Purpose**: Parse, validate, and persist bullets transactionally.

- [x] T022 [TDD] Update `AiResponseParser` to parse work experience `bulletPoints` arrays from AI JSON response. Reject null, non-array, empty array, or whitespace-only bullets where bullets are required per adaptation level. (II)
- [x] T023 [TDD] Update `AiResponseParser` to parse project `bulletPoints` arrays. Reject invalid shape (non-array, missing field). (II)
- [x] T024 [TDD] Update `AiResponseValidator` to enforce max 250 chars per bullet. Produce user-readable validation error messages. (II, III)
- [x] T025 [TDD] Update `GenerationResponsePersistenceService` to insert bullets within the same JDBC transaction as parent response/section rows. On any bullet failure → rollback entire generation response. Use connection-accepting DAO overloads (D10). Catch `Exception` (not just `SQLException`) for rollback (D23). (II, IV)
- [x] T026 [TDD] Add parser tests for: EN-only, RU-only, Bilingual, All levels, valid bullets, missing `bulletPoints` field, null/empty array, whitespace-only bullets, non-array type. (II)
- [x] T027 [TDD] Add persistence tests: all response rows + bullet rows inserted atomically, rollback on bullet failure, orphan cleanup on parent deletion. (II, IV)
- [x] T028 [TDD] Run `mvn test -pl backend` — all tests pass including parser, validator, and persistence tests. (II)
- [x] T029 [REVIEW] Run targeted backend tests: `AiResponseParserTest`, `AiResponseValidatorTest`, `GenerationResponsePersistenceServiceTest`. All GREEN. (II)

**Checkpoint**: AI response with bullets is parsed, validated, and persisted safely. ✅ All backend tests pass.

---

### Phase 4: PG1 Review API and Frontend Bullet Editing

**Purpose**: Expose and edit bullets on Review page.

- [x] T030 [TDD] Update `GenerationReviewDto` in `backend/src/main/java/com/resumainer/dto/generate/` to include bullet list under each generated work/project record. Preserve record-first grouping. (I, III)
- [x] T031 [TDD] Update review update-key format to support bullet edits. Use backend-owned opaque update keys (D27 pattern). Frontend must not construct raw DB paths. (I, V)
- [x] T032 [TDD] Update `ResumeReviewService.save()` in `backend/src/main/java/com/resumainer/service/ResumeReviewService.java` to update bullet text and set `is_edited = true` on changed bullets. Reject empty/whitespace-only bullets with descriptive error. (II, IV)
- [x] T033 [TDD] Add service/controller tests: bullet edit save (text updated in DB), bullet reload (edited text returned), empty bullet rejection, owner-scoped access. (II, V)
- [x] T034 [TDD] Run `mvn test -pl backend` — review service and controller tests pass. (II)
- [x] T035 [P] Update `frontend/src/types/generate.ts` with bullet DTO types matching backend `GenerationReviewDto` shape. (I)
- [x] T036 [P] Update `GeneratedRecordGroup.vue` and `ReviewStepForm.vue` in `frontend/src/components/generate/` to render each bullet as separate editable text input under its parent work/project record. Record-first grouping preserved. (III)
- [x] T037 [P] Add frontend validation: bullet cannot be empty/whitespace-only. Show inline error message. (III)
- [x] T038 [P] Ensure MVP Review UI does NOT add, delete, or reorder bullets. Add code comment for future extensibility. (I, III)
- [x] T039 [TDD] Add frontend tests (Vitest + Vue Test Utils): rendering bullet fields under correct records, editing marks dirty state, save payload includes bullets, empty bullet validation shown. (II, III)
- [x] T040 [TDD] Run `npm test -- --run` in frontend — all tests pass. (II)
- [x] T041 [REVIEW] Manual smoke: generate with MockAiClient → Review → edit bullet → save → reload → edited bullet persists. (II, III)
- [x] T042 [REVIEW] `npm run build` passes. (I)

**Checkpoint**: User can edit generated bullets; backend persists them. ✅ All tests + build pass.

---

## Phase Group 2 — PDF/HTML Generation from Approved Spike

### Phase 5: PG2 Dependencies, DB Config, and Saved Resume Metadata

**Purpose**: Prepare production config and dependencies for PDF generation.

- [ ] T043 [SPIKE] Inspect spike `pom.xml`. Add to `backend/pom.xml`:
  - `com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10`
  - `org.apache.pdfbox:pdfbox:2.0.30`
  - `com.openhtmltopdf:openhtmltopdf-slf4j:1.0.10` (optional, for logging)
  Verify group ID (`com.openhtmltopdf` vs `io.github.openhtmltopdf`) on Maven Central at implementation time. Do not add unrelated PDF libraries. (I)
- [ ] T044 [TDD] Inspect `saved_resume` columns. Create additive migration `V{NEXT}__update_saved_resume_pdf_metadata.sql` with missing columns:
  - `pdf_status VARCHAR(50)`
  - `pdf_file_path VARCHAR(500)`
  - `pdf_generated_at TIMESTAMP`
  - `pdf_generation_error_code VARCHAR(100)`
  - `pdf_generation_error_message VARCHAR(500)`
  - `pdf_render_profile VARCHAR(100)`
  - `pdf_page_count INT` (use `Integer` in model — B9 guard)
  Verify `public_code VARCHAR(10)` already exists (V21). (I, IV)
- [ ] T045 [TDD] Create migration `V{NEXT+1}__create_pdf_render_config_tables.sql` for `resume_pdf_fit_limits` and `resume_pdf_fill_targets` per `data-model.md`. (I, IV)
- [ ] T046 [TDD] Create migration `V{NEXT+2}__seed_pdf_render_config.sql` with active default from spike V12.1: body font 6.0–9.0px, line-height 1.0–1.3, gaps 0–16px, `max_attempts = 30`, `page2_delta_limit_percent = 50.0`. Adaptive page2 min-fill: 0 projects → 0.30. (IV)
- [ ] T047 [TDD] Create `PdfRenderConfigDao` in `backend/src/main/java/com/resumainer/dao/PdfRenderConfigDao.java` to load active fit limits + fill targets. Use `PreparedStatement`. (II, IV)
- [ ] T048 [TDD] Create `PdfRenderConfigService` in `backend/src/main/java/com/resumainer/service/PdfRenderConfigService.java` wrapping the DAO. (II)
- [ ] T049 [TDD] Add tests: no active config → error, active config loading, fill-target selection by page/language/project-count, page2 delta limit values. (II)
- [ ] T050 [TDD] Update `SavedResumeDao.insert()` and `update()` to include ALL new PDF metadata columns. Audit against B24 (INSERT column omission). (II, IV)
- [ ] T051 [TDD] Update `SavedResume` model class with new PDF fields using boxed types: `Integer pdfPageCount`, `Long pdfGeneratedAt` (B9 guard). (I)
- [ ] T052 [TDD] Run `mvn test -pl backend` — all DAO and config tests pass. (II)
- [ ] T053 [REVIEW] Human review of all new migrations. Confirm: no spike-only mock tables (`edge_case_rule`, `mock_candidate`, `mock_scenario`) ported, column names follow project conventions. (I, IV)

**Checkpoint**: PDF dependencies and production config exist. ✅ Tests pass.

---

### Phase 6: PG2 Port Spike Model Classes

**Purpose**: Bring spike data models into production backend.

- [x] T054 [SPIKE] [TDD] Port/adapt spike models into `backend/src/main/java/com/resumainer/model/pdf/`:
  - `FitState` — mutable fit parameters (class, adapted from spike record to support fitting loop mutations)
  - `PdfMetrics` — page count, text extraction, fill ratios (record)
  - `FitAttempt` — recorded attempt parameters + result (record, paths as String)
  - `FitResult` — final outcome with selected attempt and all attempts (record)
  - `PagePlan` — page allocation result (class, adapted from spike to use integer counts instead of spike-specific types)
  - `ResumeRenderData` — immutable render input with inner types for work/project/course/skill items (class, renamed from spike ResumeData, adapted for production data sources)
  Keep them simple immutable records/classes. Use production naming conventions (no spike-specific prefixes). (I)
- [x] T055 [TDD] Port spike models: `PagePlan` (page allocation result), `ResumeRenderData` (immutable render input, renamed from spike `ResumeData`). (I)
- [x] T056 [TDD] Run `mvn test -pl backend` — model compilation succeeds, no test regressions. (II)

**Checkpoint**: Spike models ported and compilable. ✅ Tests pass.

---

### Phase 7: PG2 Port Spike Core PDF Classes

**Purpose**: Bring the proven PDF engine into backend with minimal behavioral drift.

- [x] T057 [SPIKE] [TDD] Port `CssSafetyInspector` to `backend/src/main/java/com/resumainer/service/pdf/CssSafetyInspector.java`. Must reject: flexbox tokens, `row-gap`, `column-gap`, CSS `break-inside: avoid` (unreliable in OpenHTMLToPDF), `overflow: hidden`. Allow: standard CSS 2.1 properties, `@page`, custom fonts. (II, IV)
- [x] T058 [TDD] Add CssSafetyInspector tests: reject flexbox/grid/unsupported tokens, allow A4 page size, allow font-family declarations. All tests PASS. (II)
- [x] T059 [SPIKE] [TDD] Port `PdfAnalyzer` to `backend/src/main/java/com/resumainer/service/pdf/PdfAnalyzer.java`. Must extract: page count from PDF, text content (stripped of PDF artifacts), detect empty/missing pages. (II)
- [x] T060 [TDD] Add PdfAnalyzer tests: 1-page PDF → count=1, 2-page PDF → count=2, text extraction contains expected strings, empty page detection. (II)
- [x] T061 [SPIKE] [TDD] Port `PdfValidationService` to `backend/src/main/java/com/resumainer/service/pdf/PdfValidationService.java`. Must validate: page count matches target, required text anchors present, fill targets met, RU/EN normalization. (II, III)
- [x] T062 [SPIKE] [TDD] Port `ContentExpectationBuilder` to `backend/src/main/java/com/resumainer/service/pdf/ContentExpectationBuilder.java`. Builds required text expectations from actual `ResumeRenderData`. (II)
- [x] T063 [TDD] Add PdfValidationService + ContentExpectationBuilder tests. (II)
- [x] T064 [SPIKE] [TDD] Port `PdfBlankPageCleaner` to `backend/src/main/java/com/resumainer/service/pdf/PdfBlankPageCleaner.java`. (II, IV)
- [x] T065 [SPIKE] [TDD] Port `PdfPageMerger` to `backend/src/main/java/com/resumainer/service/pdf/PdfPageMerger.java`. (II)
- [x] T066 [TDD] Add PdfBlankPageCleaner + PdfPageMerger tests — covered via integration test. (II)
- [x] T067 [SPIKE] [TDD] Port `OpenHtmlPdfRenderer` to `backend/src/main/java/com/resumainer/service/pdf/OpenHtmlPdfRenderer.java`. Configure: `builder.useFastMode()`, A4 page size, UTF-8 encoding. (I, IV)
- [x] T068 [TDD] Add OpenHtmlPdfRenderer integration test: render simple HTML → PDF bytes produced, verify PDF header signature `%PDF`, verify non-empty output. (II)
- [x] T069 [TDD] Run `mvn test -pl backend` — all PDF utility tests pass. (II)
- [x] T070 [REVIEW] Compare ported classes against spike originals. Document any intentional difference. No creative rewrite allowed. (I)

**Checkpoint**: Core PDF classes ported and tested. ✅ All tests pass.

---

### Phase 8: PG2 Render Data Adapter and Page Planning

**Purpose**: Connect current generated response/profile data to the ported renderer.

- [ ] T071 [TDD] Create `ResumeRenderData` builder/adapter in `backend/src/main/java/com/resumainer/service/pdf/ResumeRenderDataBuilder.java`. Assembles from: finalized generation response (work, projects, skills, education, courses), profile-owned data (contact details, bilingual education), edited bullets, `generation_response_personal` values. Equivalent to spike `ResumeData` but populated from production DAOs. (I, IV)
- [ ] T072 [TDD] Ensure render data uses bilingual Education fields from profile, not AI-generated education. (II, III)
- [ ] T073 [TDD] Ensure render data uses edited `generation_response_personal` values; omit optional personal info lines when blank (not null or empty string). (II)
- [ ] T074 [TDD] Ensure render data includes edited bullet rows in correct `bullet_order` under their parent work/project items. (II)
- [ ] T075 [TDD] Port/adapt `PagePlanBuilder` from spike to `backend/src/main/java/com/resumainer/service/pdf/PagePlanBuilder.java`. Must use existing production `WorkExperienceBudgetResolver` + `ResumeBudgetConfigService` instead of spike `edge_case_rule`. (I, IV)
- [ ] T076 [TDD] Add tests: one-page resume (1–3 work items, 0 projects), two-page with projects (3+ work, 2+ projects), two-page without projects (4+ work, 0 projects), dense RU case (6 work, 3 projects). Verify page plan matches expected 1-page vs 2-page decision. (II)
- [ ] T077 [TDD] Run `mvn test -pl backend` — all render data and page plan tests pass. (II)
- [ ] T078 [REVIEW] Verify no code references spike `ScenarioDao`, `MockCandidate`, `Scenario`, or `EdgeCaseRuleProvider` as production source. (I)

**Checkpoint**: Production data converts into PDF page plan. ✅ Tests pass.

---

### Phase 9: PG2 XHTML Renderer and HTML/PDF Parity

**Purpose**: Generate final XHTML used by both PDF and HTML download.

- [ ] T079 [SPIKE] [TDD] Port `XhtmlTemplateRenderer` to `backend/src/main/java/com/resumainer/service/pdf/XhtmlTemplateRenderer.java`. Preserve: PDF-safe CSS only, page navigation notes, page split logic, contact row layout, section ordering (Personal → Summary → Experience → Projects → Skills → Education → Courses), optional-line handling (omit empty sections). Use explicit A4 page height from spike V12.1. (I, III, IV)
- [ ] T080 [TDD] Ensure renderer outputs page 1 footer note for multi-page artifacts: `SEE THE NEXT PAGE` (EN) / `СМ. СЛЕДУЮЩУЮ СТРАНИЦУ` (RU), selected by language. (III)
- [ ] T081 [TDD] Ensure renderer outputs page 2+ header note: `SEE THE PREVIOUS PAGE` (EN) / `СМ. ПРЕДЫДУЩУЮ СТРАНИЦУ` (RU). (III)
- [ ] T082 [TDD] Ensure page notes are visually consistent: bold, uppercase, centered, with top/bottom border, consistent margins, contrasting background. (III)
- [ ] T083 [TDD] Ensure CSS safety test runs on generated XHTML: fails on flexbox, row-gap, modern break-inside, overflow clipping, and unsupported PDF CSS. (II, IV)
- [ ] T084 [TDD] Mark existing `ResumeTemplateRenderer` as `@Deprecated` in `backend/src/main/java/com/resumainer/service/ResumeTemplateRenderer.java`. Add Javadoc: "Replaced by PDF/HTML parity renderer in feat/008. Kept as legacy reference only. Do not use in new finalization flow." Do NOT delete the file. (I)
- [ ] T085 [TDD] Add test proving new finalization flow does NOT call deprecated `ResumeTemplateRenderer`. (I)
- [ ] T086 [TDD] Run `mvn test -pl backend` — all renderer tests pass. (II)
- [ ] T087 [REVIEW] Render sample HTML from production test data. Visually compare to spike output. (III)

**Checkpoint**: New renderer produces PDF-parity XHTML/HTML. ✅ Tests pass.

---

### Phase 10: PG2 Feedback Fit Engine and Validation

**Purpose**: Fit and validate PDF pages using the spike algorithm.

- [ ] T088 [SPIKE] [TDD] Port `FeedbackFitEngine` to `backend/src/main/java/com/resumainer/service/pdf/FeedbackFitEngine.java`. Preserve: round-robin shrink/grow (font → line-height → section-gap → item-gap → paragraph-gap → bullet-gap), adaptive page2 min-fill rules, missing-text detection with generic anchors, bounded by `max_attempts` from DB config. (I, II, IV)
- [ ] T089 [TDD] Enforce `page2_delta_limit_percent` from config: page2/page3 line-height and section-gap relative to page1 must not diverge beyond configured percentage. (II, IV)
- [ ] T090 [TDD] Add fit engine tests: underfill → growth (font enlarged until fill target met), overflow → shrink (font reduced), missing critical text → shrink further, RU hyphen normalization (`бизнес-правила` matches `бизнесправила`), trailing blank page cleaned, bounding prevents infinite loop. (II)
- [ ] T091 [TDD] Add test: sparse page2 with 0 projects passes at min fill 0.30 if all required text present. (II)
- [ ] T092 [TDD] Add test: dense RU case (6 work, 3 projects, long text) does not clip final personal info lines. (II)
- [ ] T093 [TDD] Add logging assertions: fit attempt logs include attempt number, font, line-height, gaps, page count, fill, validation reason. No full resume text at info/warn/error level. No API keys, no PII. (II, V)
- [ ] T094 [TDD] Run `mvn test -pl backend` — all fit engine and validation tests pass. (II)
- [ ] T095 [REVIEW] Run spike-equivalent edge cases (ec01–ec17) using production test harness. All expected scenarios pass before integrating into finalization. (II)

**Checkpoint**: Fit engine validates generated PDFs before finalization. ✅ Tests pass.

---

### Phase 11: PG2 Finalization Integration

**Purpose**: Replace NoOp PDF boundary with real generation + compensation.

- [ ] T096 [TDD] Implement `OpenHtmlPdfGenerationService` in `backend/src/main/java/com/resumainer/service/OpenHtmlPdfGenerationService.java` implementing existing `PdfGenerationService` interface. Uses ported renderer + fit engine internally. Generates parity HTML + PDF. (I, II)
- [ ] T097 [TDD] Update `ResumeFinalizeService.finalize()` in `backend/src/main/java/com/resumainer/service/ResumeFinalizeService.java`:
  - Validate owner + selected adaptation level.
  - Set request status → `FINALIZING` (blocks concurrent — FR-008-028-2).
  - Load response + profile + budget data.
  - Call `OpenHtmlPdfGenerationService` → HTML + PDF in staging directory.
  - Validate PDF (page count, content, fill, blank pages).
  - On success: promote files to final storage, commit `saved_resume` with metadata, return export DTO.
  - On fitting failure: delete staged files, reset request status (allow re-finalization), return error with "Try again" guidance.
  - Bilingual: atomic — both succeed or neither saved.
  - Catch `Exception` (not just `SQLException` — D23).
  - Include ALL columns in INSERT (B24 guard).
  Do NOT call deprecated `ResumeTemplateRenderer`. (I, IV, V)
- [ ] T098 [TDD] Implement staging directory: create temp subdirectory under configured storage root, write HTML + PDF there, validate, promote to final path on success, delete on failure. (II, IV)
- [ ] T099 [TDD] Implement bilingual atomicity: generate EN + RU, if either fails → delete both staged sets, commit neither, reset status for both. (II, IV)
- [ ] T100 [TDD] Store PDF metadata on `saved_resume` only after validation passes: `pdf_status = 'READY'`, `pdf_file_path` (relative), `pdf_generated_at`, `pdf_render_profile`, `pdf_page_count`. (II, IV)
- [ ] T101 [TDD] On fitting failure: store `pdf_status = 'FAILED'`, `pdf_generation_error_code`, user-readable `pdf_generation_error_message` ("Resume could not be generated..."). Reset request status to allow retry. (II, III, V)
- [ ] T102 [TDD] Add tests: successful finalization → saved resume with PDF metadata, fitting failure → no saved resume committed + staged files deleted + status reset, HTML succeeds but PDF fails → both cleaned, DB failure after file write → staged files deleted (compensation), bilingual partial failure → both rolled back. (II, IV)
- [ ] T103 [TDD] Run `mvn test -pl backend` — all finalization tests pass. (II)
- [ ] T104 [REVIEW] Manually inspect generated files from local finalization smoke: open PDF, verify page count, selectable text, page notes, no clipping. (II, III)

**Checkpoint**: Backend finalization produces validated PDF + parity HTML safely. ✅ Tests pass.

---

### Phase 12: PG2 Export and Download Endpoints

**Purpose**: Wire real artifact serving endpoints.

- [ ] T105 [TDD] Update export DTO in `ExportResultDto.java` / `SavedResumeExportDto.java`: `pdfAvailable = true` when `pdf_status = 'READY'`, `htmlDownloadUrl` → `/api/generate/resumes/{id}/html`, `pdfDownloadUrl` → `/api/generate/resumes/{id}/pdf`, `pdfOpenUrl` → `/api/generate/resumes/{id}/pdf?disposition=inline`, `publicUrlLink` → `/{username}/{publicCode}`. (I, III)
- [ ] T106 [TDD] Update existing authenticated HTML download endpoint in `GenerateResumeController`: serve new parity HTML from file storage. Content-Type `text/html; charset=UTF-8`. Owner-scoped. Do NOT serve legacy HTML. (III, V)
- [ ] T107 [TDD] Implement authenticated PDF download endpoint: `GET /api/generate/resumes/{savedResumeId}/pdf`. Owner-scoped. Content-Type `application/pdf`. Default disposition `attachment`. Support `?disposition=inline` for in-browser viewing. (II, V)
- [ ] T108 [SEC] [TDD] Implement path traversal protection in both download controllers: load `pdf_file_path` from DB (relative), resolve with `Path.resolve(storageRoot, relativePath).normalize()`, verify `resolved.startsWith(storageRoot)`. If not → return 404 (not 500, avoid path disclosure). (V)
- [ ] T109 [TDD] Update public route `GET /{username}/{publicCode}` in `GenerateResumeController`: replace 501 placeholder with real implementation. Lookup by `publicCode` via `SavedResumeDao.findPublicCodeByCode()`. If found + active + not deleted → serve PDF inline. Else → 404. No cover letter, no HTML. (II, V)
- [ ] T110 [SEC] [TDD] Add rate limiting on public route: 30 requests/minute/IP. Return HTTP 429 `Retry-After: 60` when exceeded. Add 200ms artificial delay on 404 responses to slow enumeration. Log repeated patterns at WARN. (V)
- [ ] T111 [TDD] Add tests: owner download → 200 + PDF bytes, non-owner download → 403/404, missing file → 404, deleted resume → 404, public route valid code → 200 + PDF inline, invalid code → 404, deleted → 404, rate limit → 429 after 30 requests. (II, V)
- [ ] T112 [TDD] Run `mvn test -pl backend` — all controller/download tests pass. (II)
- [ ] T113 [REVIEW] Confirm no endpoint exposes raw `html_file_path`, `pdf_file_path`, base storage directory, or private HTML. (V)

**Checkpoint**: Export backend endpoints serve real PDF/HTML artifacts safely. ✅ Tests pass.

---

### Phase 13: PG2 Frontend Export Integration

**Purpose**: Connect UI to real artifact endpoints.

- [ ] T114 [P] Update `frontend/src/types/generate.ts`: add `pdfAvailable: boolean`, `pdfDownloadUrl: string`, `pdfOpenUrl: string`, `htmlDownloadUrl: string`, `publicUrlLink: string` fields to export DTO types. (I)
- [ ] T115 [P] Update `frontend/src/types/generate.ts`: add bullet DTO fields (`bulletPoints: string[]`, `bulletUpdateKeys: string[]`) to review DTO types. (I)
- [ ] T116 [P] Update `generateResumeService.ts` in `frontend/src/services/`: real PDF download (fetch → blob → download link), real PDF open (fetch → blob → `window.open(blobURL)`), real HTML download, public link copy from backend-provided URL. Remove placeholder assumptions. (I, III)
- [ ] T117 [P] Update `ExportResult.vue` in `frontend/src/components/generate/`: Download PDF / Open PDF buttons enabled only when `pdfAvailable === true`. Show informational message when not available. Download HTML button uses `htmlDownloadUrl` from backend. Copy public link uses returned URL (no client-side fabrication). (III)
- [ ] T118 [P] Add loading screen component during finalization: reuse existing AI-generation wait pattern. Randomly rotating phrases: "Generating your resume PDF...", "Optimizing page layout...", "Preparing final files...", "Almost ready...". Show from Finalize click until result returns. (III)
- [ ] T119 [TDD] Add/update frontend tests: export buttons disabled when `pdfAvailable=false`, enabled when `true`, correct URLs in button hrefs, loading screen appears during finalization, cover letter copy works. (II, III)
- [ ] T120 [TDD] Run `npm test -- --run` in frontend — all tests pass. (II)
- [ ] T121 [REVIEW] `npm run build` passes without errors. (I)

**Checkpoint**: Frontend Export page uses real PDF/HTML outputs. ✅ Tests + build pass.

---

### Phase 14: PG2 HTML Escaping Utility for Template Safety

**Purpose**: Prevent markup injection in PDF templates (FR-008-023-1).

- [ ] T122 [SEC] [TDD] Create `HtmlEscapeUtil` in `backend/src/main/java/com/resumainer/util/HtmlEscapeUtil.java`. Escape: `&` → `&amp;`, `<` → `&lt;`, `>` → `&gt;`, `"` → `&quot;`, `'` → `&#39;`. (V)
- [ ] T123 [TDD] Add HtmlEscapeUtil tests: plain text unchanged, `<script>alert(1)</script>` → escaped, `<b>hello</b>` → escaped (no formatting in MVP), mixed text+tags escaped correctly, null → empty string, empty string → empty string. (II, V)
- [ ] T124 [TDD] Apply `HtmlEscapeUtil.escape()` to ALL user-editable text before insertion into XHTML template: bullet text, personal info lines, any Review-editable fields. Integrate into `XhtmlTemplateRenderer` or `ResumeRenderDataBuilder`. (V)
- [ ] T125 [TDD] Add integration test: render resume with XSS-laden bullet text → verify no raw HTML in generated XHTML/PDF output. (II, V)
- [ ] T126 [TDD] Run `mvn test -pl backend` — all escaping tests pass. (II)

**Checkpoint**: All user text HTML-escaped before template insertion. ✅ Tests pass.

---

### Phase 15: Logging, Diagnostics, and Manual Verification

**Purpose**: Make bugs fast to diagnose. Ensure logs are detailed and safe.

- [ ] T127 [TDD] Add structured logging in `ResumeFinalizeService`: requestId, userId, savedResumeId, language, adaptation level, budget config ID, PDF fit config key, page plan summary (target pages, work count, project count). (IV, V)
- [ ] T128 [TDD] Add DEBUG-level fit attempt logging in `FeedbackFitEngine`: attempt number, font size, line-height, gaps, page count, fill percentage, validation result. Do NOT log full resume text at info/warn/error. (IV, V)
- [ ] T129 [TDD] Add logs for file staging, promotion, cleanup, rollback actions, and failure reason codes. (IV)
- [ ] T130 [TDD] Add public PDF access logging: INFO level, publicCode (not full path), anonymized IP (last octet masked), User-Agent. (V)
- [ ] T131 [REVIEW] Verify logs are useful AND safe: no API keys, no raw full prompts, no full PII dump, no stack traces in frontend response, artifact paths only at DEBUG level, never in API responses. (V)
- [ ] T132 [REVIEW] Manual smoke tests:
  1. EN-only Minimal → Review bullets → edit → finalize → PDF download (verify 1 page, selectable text, page notes)
  2. RU-only Balanced → check RU page notes (`СМ. СЛЕДУЮЩУЮ СТРАНИЦУ`), final personal info line not clipped
  3. Bilingual All → finalize Balanced → two PDFs + two HTML files
  4. Dense case (6+ work, 3+ projects) → verify exactly 2 pages
  5. Sparse page2 (0 projects) → verify accepted at low fill, no false failure
  6. Public link → open `/{username}/{publicCode}` in incognito → PDF loads inline
  7. Non-owner → 403/404 for private download
  8. Fitting failure → "Try again" button → Review page with edits preserved
  (II, III)
- [ ] T133 [REVIEW] Render representative PDFs as images. Verify: no text clipping, no overlap, no missing page notes, no extra blank pages, no broken glyphs (Cyrillic). (II, III)
- [ ] T134 [REVIEW] Verify generated PDF text extraction: includes required fields, bullets, personal info, page notes. Select all text in PDF viewer → copy → paste → verify content. (II)

**Checkpoint**: Diagnostics sufficient for audit. Manual smoke proves end-to-end correctness.

---

### Phase 16: End-to-End Regression and Coverage

**Purpose**: Verify feature does not regress generation flow. Confirm coverage targets.

- [ ] T135 [TDD] Backend E2E test: EN-only + Minimal → generate → edit bullet → finalize → PDF/HTML exist on filesystem, contain edited bullet text. (II)
- [ ] T136 [TDD] Backend E2E test: RU-only + Balanced → finalize → PDF has no clipped final personal info line, page notes in Russian. (II)
- [ ] T137 [TDD] Backend E2E test: Bilingual + All → finalize Balanced → two PDFs + two parity HTML files on filesystem. (II)
- [ ] T138 [TDD] Backend E2E test: PDF generation fails (simulate via mock config) → no saved resume committed, staged files deleted, status reset, error DTO returned. (II, IV)
- [ ] T139 [TDD] Backend E2E test: HTML generation succeeds but PDF fails (simulate) → rollback both, cleanup both staged file sets. (II, IV)
- [ ] T140 [TDD] Backend E2E test: public route `/{username}/{validCode}` → 200 + inline PDF; `/{username}/{invalidCode}` → 404; deleted resume code → 404. (II, V)
- [ ] T141 [TDD] Backend E2E test: concurrent finalization → second request returns "Finalization already in progress" (409). (II)
- [ ] T142 [TDD] Frontend E2E test: Review page renders bullets → edit → save → reload → edited text persists. Export page shows PDF buttons enabled after successful finalization. (II, III)
- [ ] T143 [REVIEW] Run `mvn clean package -pl backend`. Verify all tests pass. Check JaCoCo coverage report — new/modified feature code targets 80% useful coverage. If below target, add targeted tests (not superficial coverage padding). (II)
- [ ] T144 [REVIEW] Run `npm run build` in frontend. Confirm no TypeScript errors, no build warnings. (I)
- [ ] T145 [REVIEW] If coverage is below 80% for new/modified code, document explicit justified exception in plan. Do not add meaningless tests. (II)

**Checkpoint**: Feature works end-to-end. Tests + coverage verified. ✅ Build passes.

---

### Phase 17: Documentation and Handoff

**Purpose**: Make implementation auditable and maintainable.

- [ ] T146 [P] Update `quickstart.md` with final instructions: exact commands to run PDF generation locally, how to inspect artifacts on filesystem, how to enable DEBUG logging for fitting attempts. (I)
- [ ] T147 [P] Update developer docs with production PDF config table descriptions and safe operational defaults. (I, IV)
- [ ] T148 [P] Update `DECISIONS.md` if public route pattern, PDF config naming, or renderer naming creates new durable decisions. (I)
- [ ] T149 [P] Document legacy renderer status in code and docs: deprecated, replaced by feat/008 renderer, retained for fallback/reference only. (I)
- [ ] T150 [P] Document spike-only code explicitly NOT ported: `ScenarioDao`, `ResumeDataFactory`, `MockCandidate`, `Scenario`, `EdgeCaseRuleProvider`, `SpikeRunner`, SQLite schema/seed. List all production code ported. (I)
- [ ] T151 [P] Verify all OFL license files present alongside Inter and Manrope font files in `backend/src/main/resources/fonts/`. (I)
- [ ] T152 [REVIEW] Final review: all 41 FRs traceable to tasks, all 14 SCs verifiable, constitution I–V respected, no spike mock tables in production, no legacy renderer called in new flow. (I–V)

**Final Checkpoint**: Ready for user acceptance testing and merge.

---

## Dependency Map

```text
Phase 0 Context
  ├── Phase Group 1 (MUST complete before PG2 finalization wiring)
  │   ├── Phase 1 Schema (blocking for all PG1)
  │   ├── Phase 2 Prompt (can parallel with Phase 1 DTO contract decided)
  │   ├── Phase 3 Parser/Persistence (depends on Phase 1)
  │   └── Phase 4 Review UI/API (depends on Phase 3)
  │
  └── Phase Group 2
      ├── Phase 5 Config/Deps (blocking for all PG2)
      ├── Phase 6 Port Models (depends on Phase 5)
      ├── Phase 7 Port PDF Core (depends on Phase 6, can parallel by class)
      ├── Phase 8 Render Data/Page Plan (depends on Phase 7)
      ├── Phase 9 XHTML Renderer (depends on Phase 8)
      ├── Phase 10 Fit/Validation (depends on Phase 9)
      ├── Phase 11 Finalization (depends on Phase 10)
      ├── Phase 12 Download Endpoints (depends on Phase 11)
      ├── Phase 13 Frontend Export (can start after Phase 12 contracts stable)
      ├── Phase 14 HTML Escaping (can start after Phase 8 — XHTML template ready)
      ├── Phase 15 Diagnostics (spans PG2, final review at end)
      ├── Phase 16 E2E/Coverage (depends on all PG2 phases)
      └── Phase 17 Docs (can run in parallel after implementation details stabilize)
```

## Parallel Opportunities

- Phase 2 prompt updates parallel with Phase 1 DAO work (once DTO contract decided)
- Phase 6 model porting + Phase 7 PDF core porting can run in parallel by class
- Phase 13 frontend integration can begin after Phase 12 endpoint contracts are stable
- Phase 14 HTML escaping is independent of most PG2 phases (after Phase 9 XHTML template)
- Phase 17 documentation tasks all `[P]` — run in parallel after implementation stabilizes

## Safety Rules

- Do not port `edge_case_rule`, `mock_candidate`, or `mock_scenario` to production.
- Do not use `ScenarioDao` or `ResumeDataFactory` as production code.
- Do not invent a new PDF layout.
- Do not remove old `ResumeTemplateRenderer`; mark it `@Deprecated` and avoid using it.
- Do not bypass existing `WorkExperienceBudgetResolver`.
- Do not make frontend generate final HTML.
- Do not hide backend fitting bugs by changing frontend mocks.
- Do not log API keys or full prompts.
- Do not expose raw file paths.
- Do not return fake PDF placeholders after implementing real PDF generation.
- Do not change budget rules silently.
- HTML-escape ALL user text before template insertion (FR-008-023-1).
- Catch `Exception`, not just `SQLException`, in compensation blocks (D23).
- Use `Long`/`Integer` (boxed) for nullable PDF metadata (B9).
- Audit ALL DAO INSERT statements for new column inclusion (B24).
- Stop and ask if confidence is below 70%.
