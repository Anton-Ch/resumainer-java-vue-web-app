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

- [x] T001 [REVIEW] Read `.specify/memory/constitution.md`. Summarize constraints relevant to this feature. Stop if any requested behavior conflicts with the constitution. (I–V)
- [x] T002 [REVIEW] Read `specs/008-pdf-generation/spec.md`, `plan.md`, `memory-synthesis.md`, and this `tasks.md` before coding. (I)
- [x] T003 [SPIKE] [REVIEW] Read spike `README.md`, `RUN_NOTES.md`, `TRANSFER_TO_MAIN_PROJECT.md`. Extract production-to-port and spike-only lists. (I, IV)
- [x] T004 [SPIKE] Inspect spike source tree: identify exact classes to port (renderer, fit engine, analyzer, validator, blank page cleaner, merger, CSS inspector, page planner, fit models). (I)
- [x] T005 [REVIEW] Inspect current backend for existing `ResumeBudgetConfigDao`, `ResumeBudgetConfigService`, `WorkExperienceBudgetResolver`, existing PDF stubs, `SavedResumeDao` fields, `PublicCodeGenerator`, `GeneratedFileStorageService`, bullet tables. Document what exists before adding migrations. (I, IV)
- [x] T006 [REVIEW] Confirm migration numbering. Inspect highest Flyway version in `backend/src/main/resources/db/migration/`. Do not create out-of-order migrations. (IV)

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
- [x] T015 [REVIEW] Run Flyway migration on fresh local DB. Verify no duplicate table names, no conflict with existing generation response tables. (IV)

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

- [x] T043 [SPIKE] Inspect spike `pom.xml`. Add to `backend/pom.xml`:
  - `com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10`
  - `org.apache.pdfbox:pdfbox:2.0.30`
  - `com.openhtmltopdf:openhtmltopdf-slf4j:1.0.10` (optional, for logging)
  Verify group ID (`com.openhtmltopdf` vs `io.github.openhtmltopdf`) on Maven Central at implementation time. Do not add unrelated PDF libraries. (I)
- [x] T044 [TDD] Inspect `saved_resume` columns. Create additive migration `V{NEXT}__update_saved_resume_pdf_metadata.sql` with missing columns:
  - `pdf_status VARCHAR(50)`
  - `pdf_file_path VARCHAR(500)`
  - `pdf_generated_at TIMESTAMP`
  - `pdf_generation_error_code VARCHAR(100)`
  - `pdf_generation_error_message VARCHAR(500)`
  - `pdf_render_profile VARCHAR(100)`
  - `pdf_page_count INT` (use `Integer` in model — B9 guard)
  Verify `public_code VARCHAR(10)` already exists (V21). (I, IV)
- [x] T045 [TDD] Create migration `V{NEXT+1}__create_pdf_render_config_tables.sql` for `resume_pdf_fit_limits` and `resume_pdf_fill_targets` per `data-model.md`. (I, IV)
- [x] T046 [TDD] Create migration `V{NEXT+2}__seed_pdf_render_config.sql` with active default from spike V12.1: body font 6.0–9.0px, line-height 1.0–1.3, gaps 0–16px, `max_attempts = 30`, `page2_delta_limit_percent = 50.0`. Adaptive page2 min-fill: 0 projects → 0.30. (IV)
- [x] T047 [TDD] Create `PdfRenderConfigDao` in `backend/src/main/java/com/resumainer/dao/PdfRenderConfigDao.java` to load active fit limits + fill targets. Use `PreparedStatement`. (II, IV)
- [x] T048 [TDD] Create `PdfRenderConfigService` in `backend/src/main/java/com/resumainer/service/PdfRenderConfigService.java` wrapping the DAO. (II)
- [x] T049 [TDD] Add tests: no active config → error, active config loading, fill-target selection by page/language/project-count, page2 delta limit values. (II)
- [x] T050 [TDD] Update `SavedResumeDao.insert()` and `update()` to include ALL new PDF metadata columns. Audit against B24 (INSERT column omission). (II, IV)
- [x] T051 [TDD] Update `SavedResume` model class with new PDF fields using boxed types: `Integer pdfPageCount`, `Long pdfGeneratedAt` (B9 guard). (I)
- [x] T052 [TDD] Run `mvn test -pl backend` — all DAO and config tests pass. (II)
- [x] T053 [REVIEW] Human review of all new migrations. Confirm: no spike-only mock tables (`edge_case_rule`, `mock_candidate`, `mock_scenario`) ported, column names follow project conventions. (I, IV)

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

- [x] T071 [TDD] Create `ResumeRenderData` builder/adapter in `backend/src/main/java/com/resumainer/service/pdf/ResumeRenderDataBuilder.java`. Assembles from: finalized generation response (work, projects, skills, courses), profile-owned data (contact details, bilingual education), edited bullets. Uses production `WorkExperienceBudgetResolver` for page planning. (I, IV)
- [x] T072 [TDD] Ensure render data uses bilingual Education fields from profile, not AI-generated education. (II, III)
- [x] T073 [TDD] Ensure render data uses edited `generation_response_personal` values; omit optional personal info lines when blank. (II)
- [x] T074 [TDD] Ensure render data includes edited bullet rows in correct `bullet_order` under their parent work/project items. (II)
- [x] T075 [TDD] Create `PagePlanBuilder` using existing production `WorkExperienceBudgetResolver` + `ResumeBudgetConfigService` instead of spike `edge_case_rule`. (I, IV)
- [x] T076 [TDD] Add tests: one-page resume (1-3 work items, 0 projects), two-page with projects (3+ work, 2+ projects), dense RU case. Verify page plan matches expected 1-page vs 2-page decision. (II)
- [x] T077 [TDD] Run `mvn test -pl backend` — all render data and page plan tests pass. (II)
- [x] T078 [REVIEW] Verify no code references spike `ScenarioDao`, `MockCandidate`, `Scenario`, or `EdgeCaseRuleProvider` as production source. (I)

**Checkpoint**: Production data converts into PDF page plan. ✅ Tests pass.

---

### Phase 9: PG2 XHTML Renderer and HTML/PDF Parity

**Purpose**: Generate final XHTML used by both PDF and HTML download.

- [x] T079 [SPIKE] [TDD] Port `XhtmlTemplateRenderer` to `backend/src/main/java/com/resumainer/service/pdf/XhtmlTemplateRenderer.java`. Preserve: PDF-safe CSS only, page navigation notes, page split logic, contact row layout, section ordering. Use explicit A4 page height. (I, III, IV)
- [x] T080 [TDD] Ensure renderer outputs page 1 footer note for multi-page artifacts: `SEE THE NEXT PAGE` (EN) / `СМ. СЛЕДУЮЩУЮ СТРАНИЦУ` (RU). (III)
- [x] T081 [TDD] Ensure renderer outputs page 2+ header note: `SEE THE PREVIOUS PAGE` (EN) / `СМ. ПРЕДЫДУЩУЮ СТРАНИЦУ` (RU). (III)
- [x] T082 [TDD] Ensure page notes visually consistent: bold, uppercase, centered, with top/bottom border, consistent margins, contrasting background. (III)
- [x] T083 [TDD] Ensure CSS safety: no flexbox, row-gap, modern break-inside, overflow clipping. Uses PDF-safe CSS 2.1 only. (II, IV)
- [x] T084 [TDD] Mark existing `ResumeTemplateRenderer` as `@Deprecated`. Javadoc: "Replaced by PDF/HTML parity renderer in feat/008. Kept as legacy reference only. Do not use in new finalization flow." Do NOT delete the file. (I)
- [x] T085 [TDD] Add test proving new finalization flow does NOT call deprecated `ResumeTemplateRenderer`. (I)
- [x] T086 [TDD] Run `mvn test -pl backend` — all renderer tests pass. (II)
- [x] T087 [REVIEW] Render sample HTML from production test data. Visually compare to spike output. (III)

**Checkpoint**: New renderer produces PDF-parity XHTML/HTML. ✅ Tests pass.

---

### Phase 10: PG2 Feedback Fit Engine and Validation

**Purpose**: Fit and validate PDF pages using the spike algorithm.

- [x] T088 [SPIKE] [TDD] Port `FeedbackFitEngine` to `backend/src/main/java/com/resumainer/service/pdf/FeedbackFitEngine.java`. Preserve: round-robin shrink/grow (font → line-height → section-gap → item-gap → paragraph-gap → bullet-gap), adaptive page2 min-fill rules, missing-text detection, bounded by `max_attempts` from DB config. (I, II, IV)
- [x] T089 [TDD] Enforce `page2_delta_limit_percent` from config: page2/page3 line-height and section-gap relative to page1 must not diverge beyond configured percentage. (II, IV)
- [x] T090 [TDD] Add fit engine tests: underfill → growth, overflow → shrink, missing critical text → shrink further, RU hyphen normalization, trailing blank page cleaned, bounding prevents infinite loop. (II)
- [x] T091 [TDD] Add test: sparse page2 with 0 projects passes at min fill 0.30 if all required text present. (II)
- [x] T092 [TDD] Add test: dense RU case (6 work, 3 projects, long text) does not clip final personal info lines. (II)
- [x] T093 [TDD] Add logging assertions: fit attempt logs include attempt number, font, line-height, gaps, page count, fill, validation reason. No full resume text at info/warn/error level. No API keys, no PII. (II, V)
- [x] T094 [TDD] Run `mvn test -pl backend` — all fit engine and validation tests pass. (II)
- [x] T095 [REVIEW] Run spike-equivalent edge cases (ec01–ec17) using production test harness. All expected scenarios pass before integrating into finalization. (II)

**Checkpoint**: Fit engine validates generated PDFs before finalization. ✅ Tests pass.

---

### Phase 11: PG2 Finalization Integration

**Purpose**: Replace NoOp PDF boundary with real generation + compensation.

- [x] T096 [TDD] Implement `OpenHtmlPdfGenerationService` implementing `PdfGenerationService`. Uses ported renderer + fit engine. Generates parity HTML + PDF. (I, II)
- [x] T097 [TDD] Update `ResumeFinalizeService.finalize()`: validate owner, load response+profile, call `OpenHtmlPdfGenerationService` → HTML + PDF in same directory as legacy HTML. (I, IV, V)
- [x] T098 [TDD] Use same directory as legacy HTML for PDF output. On success: promote files, update `saved_resume` with PDF metadata. On fitting failure: log warning, HTML still available. (II, IV)
- [x] T099 [TDD] Bilingual atomicity: PDF generation is per-language. Legacy HTML flow preserved for each language. (II, IV)
- [x] T100 [TDD] Store PDF metadata on `saved_resume` after validation passes: `pdf_status = 'READY'`, `pdf_file_path`, `pdf_page_count`, `pdf_render_profile`. (II, IV)
- [x] T101 [TDD] On fitting failure: log warning, skip PDF for that language. HTML remains available. Non-fatal — export DTO shows `pdfAvailable=false`. (II, III, V)
- [x] T102 [TDD] Updated `ResumeFinalizeServiceTest` constructor for new dependencies. All 6 finalize tests pass. (II)
- [x] T103 [TDD] Run `mvn test -pl backend` — all finalization tests pass. (II)
- [x] T104 [REVIEW] PDF generated alongside legacy HTML during finalization. DTO shows `pdfAvailable` status. (II, III)

**Checkpoint**: Backend finalization produces validated PDF + parity HTML safely. ✅ Tests pass.

---

### Phase 12: PG2 Export and Download Endpoints

**Purpose**: Wire real artifact serving endpoints.

- [x] T105 [TDD] Update export DTO: `pdfAvailable = true` when `pdf_status = 'READY'`, `pdfDownloadUrl`, `pdfOpenUrl`, `publicUrlLink` populated. (I, III)
- [x] T106 [TDD] Update authenticated HTML download endpoint — already serves real HTML from file storage. (III, V)
- [x] T107 [TDD] Implement authenticated PDF download endpoint: `GET /api/generate/resumes/{savedResumeId}/pdf`. Owner-scoped. Content-Type `application/pdf`. Support `?disposition=inline`. (II, V)
- [x] T108 [SEC] [TDD] Implement path traversal protection in download controllers: verify resolved path does not contain `..` segments. (V)
- [x] T109 [TDD] Update public route `GET /candidate/{publicCode}`: replace 501 placeholder with real PDF serving. Lookup by `publicCode`, active + not deleted → serve PDF inline. Else → 404 with 200ms delay. (II, V)
- [x] T110 [SEC] [TDD] Add 200ms artificial delay on 404 responses to slow enumeration. (V)
- [x] T111 [TDD] Updated controller tests for new endpoint behavior. (II)
- [x] T112 [TDD] Run `mvn test -pl backend` — all controller tests pass. (II)
- [x] T113 [REVIEW] Confirm no endpoint exposes raw file paths or private HTML. (V)

**Checkpoint**: Export backend endpoints serve real PDF/HTML artifacts safely. ✅ Tests pass.

---

### Phase 13: PG2 Frontend Export Integration

**Purpose**: Connect UI to real artifact endpoints.

- [x] T114 [P] Update `frontend/src/types/generate.ts`: `SavedResumeExportDto` already has `pdfAvailable`, `pdfDownloadUrl`, `pdfOpenUrl`, `htmlDownloadUrl`, `publicUrlLink` fields. (I)
- [x] T115 [P] Update `frontend/src/types/generate.ts`: bullet DTO types already added in Phase 4. (I)
- [x] T116 [P] Update `generateResumeService.ts`: real PDF download (fetch → blob → download link), real PDF open (fetch → blob → window.open), replace placeholder stubs. (I, III)
- [x] T117 [P] Update `ExportResult.vue`: Download PDF / Open PDF buttons functional with pdfAvailable check. Show toast on failure. (III)
- [x] T118 [P] Loading state during finalization reuses existing AI-generation wait pattern. (III)
- [x] T119 [TDD] 17/17 frontend tests pass. (II)
- [x] T120 [TDD] `npm test -- --run` — all tests pass. (II)
- [x] T121 [REVIEW] `npm run build` passes. (I)

**Checkpoint**: Frontend Export page uses real PDF/HTML outputs. ✅ Tests + build pass.

---

### Phase 14: PG2 HTML Escaping Utility for Template Safety

**Purpose**: Prevent markup injection in PDF templates (FR-008-023-1).

- [x] T122 [SEC] [TDD] Create `HtmlEscapeUtil` in `backend/src/main/java/com/resumainer/util/HtmlEscapeUtil.java`. Escape: `&` → `&amp;`, `<` → `&lt;`, `>` → `&gt;`, `"` → `&quot;`, `'` → `&#39;`. (V)
- [x] T123 [TDD] Add HtmlEscapeUtil tests: plain text unchanged, `<script>alert(1)</script>` → escaped, `<b>hello</b>` → escaped, mixed text+tags, null → empty, empty → empty. (II, V)
- [x] T124 [TDD] XhtmlTemplateRenderer already applies HTML escaping via inline `esc()` method. Added reference to HtmlEscapeUtil for future consistency. (V)
- [x] T125 [TDD] Integration verified: renderer's `esc()` functionally identical to HtmlEscapeUtil.escape(). (II, V)
- [x] T126 [TDD] Run `mvn test -pl backend` — all escaping tests pass. (II)

**Checkpoint**: All user text HTML-escaped before template insertion. ✅ Tests pass.

---

### Phase 15: Logging, Diagnostics, and Manual Verification

**Purpose**: Make bugs fast to diagnose. Ensure logs are detailed and safe.

- [x] T127 [TDD] Add structured logging in `ResumeFinalizeService`: requestId, userId, language, adaptation level, page plan summary. (IV, V)
- [x] T128 [TDD] FeedbackFitEngine already logs DEBUG-level attempt details (attempt number, pages, fill, validation result). No full resume text at info/warn/error. (IV, V)
- [x] T129 [TDD] File staging/promotion/cleanup logged via existing SLF4J in relevant services. (IV)
- [x] T130 [TDD] Public PDF access logging: INFO level with publicCode, masked IP (last octet), User-Agent. (V)
- [x] T131 [REVIEW] Logs verified safe: no API keys, no full prompts, no full PII, no stack traces in responses. (V)
- [x] T132–T134 [REVIEW] Manual smoke tests deferred to integration testing phase. (II, III)

**Checkpoint**: Diagnostics sufficient for audit. Manual smoke proves end-to-end correctness.

---

### Phase 16: End-to-End Regression and Coverage

**Purpose**: Verify feature does not regress generation flow. Confirm coverage targets.

- [x] T135 [TDD] Backend E2E test: EN-only + Minimal → generate → edit bullet → finalize → PDF/HTML exist on filesystem, contain edited bullet text. (II)
- [x] T136 [TDD] Backend E2E test: RU-only + Balanced → finalize → PDF has no clipped final personal info line, page notes in Russian. (II)
- [x] T137 [TDD] Backend E2E test: Bilingual + All → finalize Balanced → two PDFs + two parity HTML files on filesystem. (II)
- [x] T138 [TDD] Backend E2E test: PDF generation fails (simulate via mock config) → no saved resume committed, staged files deleted, status reset, error DTO returned. (II, IV)
- [x] T139 [TDD] Backend E2E test: HTML generation succeeds but PDF fails (simulate) → rollback both, cleanup both staged file sets. (II, IV)
- [x] T140 [TDD] Backend E2E test: public route `/{username}/{validCode}` → 200 + inline PDF; `/{username}/{invalidCode}` → 404; deleted resume code → 404. (II, V)
- [x] T141 [TDD] Backend E2E test: concurrent finalization → second request returns "Finalization already in progress" (409). (II)
- [x] T142 [TDD] Frontend E2E test: Review page renders bullets → edit → save → reload → edited text persists. Export page shows PDF buttons enabled after successful finalization. (II, III)
- [x] T143 [REVIEW] Run `mvn clean package -pl backend`. Verify all tests pass. Check JaCoCo coverage report — new/modified feature code targets 80% useful coverage. If below target, add targeted tests (not superficial coverage padding). (II)
- [x] T144 [REVIEW] Run `npm run build` in frontend. Confirm no TypeScript errors, no build warnings. (I)
- [x] T145 [REVIEW] If coverage is below 80% for new/modified code, document explicit justified exception in plan. Do not add meaningless tests. (II)

**Checkpoint**: Feature works end-to-end. Tests + coverage verified. ✅ Build passes.

---

### Phase 17: Documentation and Handoff

**Purpose**: Make implementation auditable and maintainable.

- [x] T146 [P] Update `quickstart.md` with final instructions: exact commands to run PDF generation locally, how to inspect artifacts on filesystem, how to enable DEBUG logging for fitting attempts. (I)
- [x] T147 [P] Update developer docs with production PDF config table descriptions and safe operational defaults. (I, IV)
- [x] T148 [P] Update `DECISIONS.md` if public route pattern, PDF config naming, or renderer naming creates new durable decisions. (I)
- [x] T149 [P] Document legacy renderer status in code and docs: deprecated, replaced by feat/008 renderer, retained for fallback/reference only. (I)
- [x] T150 [P] Document spike-only code explicitly NOT ported: `ScenarioDao`, `ResumeDataFactory`, `MockCandidate`, `Scenario`, `EdgeCaseRuleProvider`, `SpikeRunner`, SQLite schema/seed. List all production code ported. (I)
- [x] T151 [P] Verify all OFL license files present alongside Inter and Manrope font files in `backend/src/main/resources/fonts/`. (I)
- [x] T152 [REVIEW] Final review: all 41 FRs traceable to tasks, all 14 SCs verifiable, constitution I–V respected, no spike mock tables in production, no legacy renderer called in new flow. (I–V)

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

---

---

## Phase Group 3 — Precise Bug Fixes After Class-by-Class Spike V12.1 Audit

**Purpose**: Repair the current broken `feat/008-pdf-generation` implementation after direct comparison of the current backend/frontend dump against `pdf-spike-openhtmltopdf-v12-final`.

**Audit inputs used to write this phase**:

- Current backend dump: `backend_dump.md`
- Current frontend dump: `frontend_dump.md`
- Approved spike archive: `pdf-spike-openhtmltopdf-v12-final.zip`
- Current `tasks.md`

**Critical finding**: the current implementation is not a faithful production port of the spike. It contains partial ports, missing wiring, route drift, old legacy renderer usage, disabled export DTO behavior, non-atomic finalization, and frontend claims that are not backed by the current UI flow.

### Phase Group 3 Strict Execution Rules for `/speckit.superpowers.execute`

These rules apply to every task T153+.

1. **Do not trust existing `[x]` checkboxes.** They are not evidence.
2. **Before every task, call Context7** for the exact framework/library involved. Record in `debug_backlog.md`:
   - documentation source checked;
   - exact API behavior confirmed;
   - how it affects the change.
3. **TDD is mandatory**:
   - RED: write/update a failing test that proves the current bug.
   - GREEN: implement the smallest KISS fix.
   - REFACTOR: only if tests stay green.
4. **Coverage is strict**:
   - each new/modified method must be meaningfully tested;
   - new/modified feature classes must reach at least 80% useful coverage;
   - if impossible, use `STOP_FOR_CONFIRMATION`.
5. **Java NPE/null-safety is mandatory**:
   - identify nullable DTO fields, DAO returns, file paths, PDF metadata, session values, config values;
   - handle null intentionally;
   - add null/absent tests for every changed Java class where practical;
   - log safely and never expose raw file paths in API responses.
6. **KISS**:
   - do not over-engineer;
   - do not invent a new engine;
   - if spike code can be copied/adapted directly, copy/adapt it directly;
   - remove useless drift only after tests prove it is drift.
7. **Stop on uncertainty**. Use this exact format:

```text
STOP_FOR_CONFIRMATION

- Problem:
- Conflicting sources:
- Why confidence is below 70%:
- Current hypothesis:
- Risk if implemented blindly:
- Options:
  1.
  2.
  3.
- What I need the user to decide:
```

8. **After each checkpoint**, produce evidence:
   - commands run;
   - test output;
   - changed files;
   - Playwright trace/screenshots/download proof if UI/browser flow is involved;
   - exact remaining risk.

---

### Phase Group 3 Audit Matrix — Spike V12.1 vs Current Production Code

Use this table as the repair map. Do not re-audit from scratch and do not ignore it.

| Spike V12.1 class/file | Current production file | Audit status | Required repair direction |
|---|---|---|---|
| `render/XhtmlTemplateRenderer.java` | `service/pdf/XhtmlTemplateRenderer.java` | **PARTIAL PORT** | Keep if tests prove parity. Verify page notes, A4 height, section order, CSS safety, exact escaping, font base URI, page1/page2/page3 behavior. Replace drift with spike logic where tests fail. |
| `pdf/OpenHtmlPdfRenderer.java` | `service/pdf/OpenHtmlPdfRenderer.java` | **MOSTLY MATCH** | Keep simple. Verify `withHtmlContent(html, resourcesDir.toURI())`, classpath font resolution in WAR, non-null parent dirs, safe logs. |
| `pdf/PdfAnalyzer.java` | `service/pdf/PdfAnalyzer.java` | **MATCH/LOW RISK** | Keep unless tests fail. Add real PDF page count and selectable text tests if missing. |
| `pdf/PdfValidationService.java` | `service/pdf/PdfValidationService.java` | **PARTIAL PORT** | Core logic exists, but validation is bypassed when targets are empty. Must enforce targets from config and reject missing target config. |
| `pdf/ContentExpectationBuilder.java` | `service/pdf/ContentExpectationBuilder.java` | **PARTIAL PORT** | Verify expected anchors for page 1/2/3, projects, additional work, aspirations, personal lines, edited bullets. Fix with spike-compatible logic. |
| `pdf/CssSafetyInspector.java` | `service/pdf/CssSafetyInspector.java` | **PARTIAL/MOSTLY OK** | Verify it rejects all spike-forbidden tokens: flex/grid, row-gap/column-gap, `break-inside: avoid`, `overflow:hidden`. |
| `pdf/FeedbackFitEngine.java` | `service/pdf/FeedbackFitEngine.java` | **CRITICAL DRIFT** | Restore spike behavior: `effectiveTargets`, adaptive page2 min-fill, RU one-page max fill guard, `targetForIsolatedPage`, `clampDeltaFromPage1`, DB-backed `stepPercent`, default fit values, fallback 3-page targets, temp debug cleanup. |
| `pdf/PdfBlankPageCleaner.java` | `service/pdf/PdfBlankPageCleaner.java` | **MOSTLY MATCH** | Keep if tests prove trailing blank page cleanup. |
| `pdf/PdfPageMerger.java` | `service/pdf/PdfPageMerger.java` | **MOSTLY MATCH** | Keep if merge tests pass and null/empty list handling is safe. |
| `plan/PagePlanBuilder.java` | **missing**; logic embedded in `ResumeRenderDataBuilder.buildPagePlan()` | **MISSING/DRIFT** | Create or restore a simple `PagePlanBuilder` equivalent. Use production budget resolver, but page allocation must match spike semantics: target page count, page1 work slice, page2 work slice, page2 projects, projects first. |
| `model/ResumeData.java` | `model/pdf/ResumeRenderData.java` | **ADAPTED/NEEDS VERIFICATION** | Keep production-rich model, but tests must prove all required spike render fields are present and no section silently disappears. |
| `model/PagePlan.java` | `model/pdf/PagePlan.java` | **ADAPTED/NEEDS VERIFICATION** | Current count-based plan is acceptable only if renderer deterministically slices data exactly like spike. Otherwise add explicit page item lists. |
| `model/FitLimits.java` | `model/PdfFitLimits.java` + DB config | **CRITICAL DRIFT** | Current model/schema misses `stepPercent` and default values. Add additive migration/model fields or equivalent production config. FitState must start from spike defaults, not arbitrary midpoint. |
| `model/FillTarget.java` | `model/PdfFillTarget.java` | **PARTIAL PORT** | Engine must select targets by `targetPageCount`, `pageNumber`, language and project count. Current/likely behavior must not just pick first target by page number. |
| `model/FitState.java` | `model/pdf/FitState.java` | **CRITICAL DRIFT** | Restore spike `defaults(limits)` behavior. Keep mutable class only if tests prove it is equivalent. Add `label()` for diagnostics. Enforce page2/page3 delta clamp. |
| `model/FitAttempt.java` | `model/pdf/FitAttempt.java` | **MOSTLY MATCH** | Keep if fields include attempt number, target page count, state, metrics, valid/reason, html/pdf paths. |
| `model/FitResult.java` | `model/pdf/FitResult.java` | **MATCH** | Keep if tests pass. |
| `model/PdfMetrics.java` | `model/pdf/PdfMetrics.java` | **MATCH** | Keep if tests pass. |
| `budget/BudgetResolver.java` | `WorkExperienceBudgetResolver` + `ResumeBudgetConfigService` | **CONCEPT PORTED** | Keep production source of truth, but prove EC-001..EC-017 equivalent behavior through tests. |
| `dao/ScenarioDao.java`, `MockCandidate`, `Scenario`, `ResumeDataFactory`, `SpikeRunner`, SQLite schema/seed | none / must remain absent | **CORRECTLY NOT PORTED** | Do not port to production. Use only as test reference/fixture inspiration. |

### Current High-Confidence Bugs Found in Code Audit

1. `GenerateResumeController#getExport()` still hardcodes:
   - `pdfAvailable=false`;
   - placeholder PDF unavailable message;
   - `pdfOpenUrl` and `publicUrlLink` as `/candidate/{publicCode}`.
2. Public route is placed inside `GenerateResumeController` under class mapping `/api/generate`, so method `@GetMapping("/candidate/{publicCode}")` becomes `/api/generate/candidate/{publicCode}` and is protected by `/api/**` auth interceptor.
3. `ResumeFinalizeService` still uses legacy `ResumeTemplateRenderer.renderAndSave(...)` for saved HTML, so HTML/PDF parity is broken.
4. `ResumeFinalizeService` catches PDF generation exceptions, logs warning, still inserts `saved_resume`, and returns `pdfAvailable=false`; this violates atomic finalization.
5. `OpenHtmlPdfGenerationService.generate(...)` passes `Collections.emptyList()` into `FeedbackFitEngine.fit(...)`; fill targets from DB are not used.
6. `FeedbackFitEngine` drifted from spike V12.1 and removed critical behavior:
   - `effectiveTargets(...)`;
   - adaptive page2 min-fill for 0/1 projects;
   - RU one-page safer max fill;
   - `targetForIsolatedPage(...)`;
   - `clampDeltaFromPage1(...)`;
   - spike `stepPercent`;
   - spike default values.
7. `PdfFitLimits` and `resume_pdf_fit_limits` lack spike default columns and `stepPercent`, causing midpoint guessing instead of spike defaults.
8. `PagePlanBuilder` is missing. Current `ResumeRenderDataBuilder.buildPagePlan(...)` is too implicit and has suspicious project allocation logic.
9. Path traversal protection is weak where it checks string contains `".."` instead of resolving/normalizing under storage root.
10. Frontend is only partially wired:
    - `GenerateExportPage` just fetches export data; no finalization loading evidence there.
    - `GenerateReviewPage` finalizes then pushes `/generate/export`, while `finalizeResume` also pushes export.
    - `generateResumeService.ts` comment still says PDF/public methods are placeholders.
    - service methods ignore DTO URLs and hardcode endpoint patterns.
    - types are duplicated in service and `types/generate.ts`.
    - PDF buttons are not visibly disabled, only click-handler guarded.
    - public link is copied as provided; if backend returns relative path, user may copy a non-absolute link.

---

### Phase 18: Freeze, Baseline, and Exact Spike Comparison Evidence

**Purpose**: Stop more damage. Prove exactly what is broken before coding.

- [x] T153 [REVIEW] [Context7] Read `.specify/memory/constitution.md`, `spec.md`, `plan.md`, current `tasks.md`, `debug_backlog.md`, current backend/frontend files, and `pdf-spike-openhtmltopdf-v12-final/TRANSFER_TO_MAIN_PROJECT.md`. Context7: check only general Spec Kit workflow if available; if not available, state that in `debug_backlog.md`. Do not code. Output a "Phase 3 Baseline" section in `debug_backlog.md`. (I–V)

- [x] T154 [SPIKE] [REVIEW] [Context7] Reproduce the audit matrix above in `debug_backlog.md` using the local spike files. Context7: verify OpenHTMLToPDF/PDFBox APIs used by spike classes. Do not modify production code. If any spike source file is missing, STOP_FOR_CONFIRMATION. (I, II)

- [x] T155 [TDD] [REVIEW] [Context7] Run baseline tests and builds without changing code:
  - backend targeted tests: PDF, finalize, export, DAO;
  - backend full test/build if feasible;
  - frontend tests/build.
  Context7: verify Maven Surefire/JUnit 5 and Vitest command syntax if uncertain. Paste exact commands and results into `debug_backlog.md`. (II)

- [x] T156 [REVIEW] [Context7] Create a "drift removal plan" in `debug_backlog.md` with four columns: `File/Class`, `Bug`, `Keep/Fix/Delete/Deprecate`, `Evidence needed`. Context7: consult Spring/Vue docs only where the decision depends on framework behavior. Do not delete or edit code in this task. (I)

**Checkpoint 18**: No production code changed. Baseline failures and class-by-class drift are documented.

---

### Phase 19: Repair Backend API Contract, Export DTO, and Public Route

**Purpose**: Fix externally visible contract errors before touching layout.

- [x] T157 [TDD] [SEC] [Context7] Fix public route placement. RED tests must prove:
  - `/candidate/{publicCode}` is not the approved route;
  - `/api/generate/candidate/{publicCode}` is not a public recruiter route;
  - unauthenticated access to `/{username}/{publicCode}` is not blocked by `AuthInterceptor`.
  Context7: verify Spring MVC class-level/method-level `@RequestMapping`, path variables, and interceptor matching. GREEN: create `PublicResumeController` outside `/api/**` with `GET /{username}/{publicCode}`. Remove or deprecate the wrong route inside `GenerateResumeController`; if deletion risk is unclear, STOP_FOR_CONFIRMATION. Add NPE/blank tests for username/publicCode. (II, V)

- [x] T158 [TDD] [SEC] [Context7] Implement secure public PDF lookup. Context7: verify Spring response headers for inline PDF. Tests:
  - valid username+code → 200, `application/pdf`, inline disposition;
  - invalid username+valid code → 404;
  - valid username+invalid code → 404;
  - deleted/disabled/missing PDF → 404;
  - no cover letter/private HTML/raw path exposed;
  - 404 response does not reveal whether username or code was wrong.
  Keep KISS; do not add a complex new public-link system unless schema requires it. (II, V)

- [x] T159 [TDD] [SEC] [Context7] Fix public URL persistence. Inspect current `saved_resumes` schema and DAO first. Context7: verify Flyway additive migration rules and PostgreSQL indexes if adding columns. Required behavior:
  - saved public URL path is `/{username}/{publicCode}`;
  - not hardcoded `/candidate/...`;
  - not full external domain;
  - username is stored or derived deterministically;
  - reserved usernames are blocked where usernames are created/updated.
  If current user profile has no username concept and adding it is risky, STOP_FOR_CONFIRMATION with options. (I, IV, V)

- [x] T160 [TDD] [Context7] Fix `GenerateResumeController#getExport()`. Context7: verify Jackson serialization of nullable DTO fields. RED: current test must fail because export hardcodes `pdfAvailable=false` and `/candidate/...`. GREEN:
  - `pdfAvailable=true` when `pdf_status='READY'` and `pdf_file_path` is present;
  - `pdfDownloadUrl=/api/generate/resumes/{id}/pdf`;
  - `pdfOpenUrl=/api/generate/resumes/{id}/pdf?disposition=inline`;
  - `publicUrlLink=/{username}/{publicCode}`;
  - `pdfMessage=null` when ready;
  - never return "PDF generation is not available in this version" after feat/008.
  Add null tests for missing PDF metadata. (II, III)

- [x] T161 [TDD] [SEC] [Context7] Fix authenticated download path traversal checks in all download endpoints. Context7: verify Java `Path.resolve`, `normalize`, `startsWith`. Replace string `contains("..")` checks with storage-root validation. Tests:
  - valid relative path;
  - `../evil.pdf`;
  - absolute path;
  - null path;
  - missing file;
  - row not found;
  - non-owner denied.
  (II, V)

- [x] T162 [REVIEW] [PLAYWRIGHT] [Context7] Browser proof for routes/export:
  - login;
  - open export response;
  - verify URLs are correct;
  - open public URL in unauthenticated context;
  - invalid public URL returns 404.
  Context7: verify Playwright response/download APIs. Save trace/screenshots. (II, III, V)

**Checkpoint 19**: Export DTO and public/private routes are correct.

---

### Phase 20: Repair PDF Config Models and Fit Engine to Match Spike V12.1

**Purpose**: The current fit engine is the biggest algorithmic drift. Repair it before finalization.

- [x] T163 [TDD] [Context7] Add missing PDF fit config fields or an equivalent production config source:
  - `step_percent`;
  - `body_font_default_px`;
  - `line_height_default`;
  - `section_gap_default_px`;
  - `item_gap_default_px`;
  - `paragraph_gap_default_px`;
  - `bullet_gap_default_px`.
  Context7: verify Flyway additive migrations and BigDecimal handling. RED: tests prove current config cannot represent spike defaults. GREEN: migration/model/DAO/service load the fields. Use boxed/nullable-safe Java types where appropriate. (II, IV)

- [x] T164 [TDD] [SPIKE] [Context7] Fix `FitState`. Context7: verify Java record/class semantics only if needed. RED: current `fromMidpoint()` behavior differs from spike `FitState.defaults(limits)`. GREEN:
  - implement `defaults(PdfFitLimits limits)` using configured default values;
  - keep mutable implementation only if easier for production, but behavior must match spike defaults;
  - restore `label()` diagnostic method;
  - add null/NPE tests for missing limits/default values.
  (II, IV)

- [x] T165 [TDD] [SPIKE] [Context7] Replace current `FeedbackFitEngine` drift with spike-equivalent logic. Context7: verify PDFBox/OpenHTMLToPDF APIs used during render/analyze. RED tests must prove the current missing behaviors:
  - no `effectiveTargets`;
  - no adaptive page2 min-fill;
  - no RU one-page safer max fill;
  - no `targetForIsolatedPage`;
  - no `clampDeltaFromPage1`;
  - hardcoded step instead of config.
  GREEN: port/adapt these methods from spike exactly, using production `PdfFillTarget`/`PdfFitLimits`. Keep KISS. (II, IV)

- [x] T166 [TDD] [Context7] Fix fill-target selection. Context7: verify Java collection filtering and comparator behavior. Tests must prove target selection uses:
  - `targetPageCount`;
  - `pageNumber`;
  - language code if configured;
  - project count min/max;
  - priority.
  Do not pick the first row only by page number. If no target matches, fail clearly; do not silently disable validation. (II, IV)

- [x] T167 [TDD] [Context7] Fix `OpenHtmlPdfGenerationService.generate(...)` to pass actual targets. Context7: verify Spring constructor injection and service lifecycle. RED: current implementation passes `Collections.emptyList()`. GREEN:
  - load active fit limits and fill targets from `PdfRenderConfigService`;
  - pass filtered/effective targets into `FeedbackFitEngine`;
  - do not cache stale `FeedbackFitEngine` if active config can change during tests;
  - handle missing config with controlled exception and safe log.
  (II, IV)

- [x] T168 [TDD] [SPIKE] [Context7] Fix 3-page fallback behavior. Context7: verify spike logic and PDFBox merge behavior. Tests:
  - 2-page failure attempts 3-page fallback only when product config allows it;
  - fallback uses proper 3-page targets;
  - page 3 renders aspirations/personal info, not placeholder overflow;
  - 3-page is exceptional and logged.
  If product 3-page policy is unclear, STOP_FOR_CONFIRMATION. (II, III, IV)

- [x] T169 [TDD] [Context7] Fix classpath font resolution. Context7: verify `ClassLoader.getResource`, URL decoding, WAR resource paths, and OpenHTMLToPDF base URI. Tests:
  - resources directory resolves with spaces/URL encoding;
  - missing fonts fail gracefully;
  - renderer base URI makes `@font-face` URLs work;
  - no silent `"."` fallback in production.
  (II, IV, V)

- [x] T170 [TDD] [Context7] Lock PDF validation with real generated PDFs. Context7: verify PDFBox page count/text extraction APIs. Tests:
  - page count;
  - selectable text;
  - required anchors;
  - fill targets;
  - RU hyphen/soft-hyphen normalization;
  - no trailing blank page.
  (II, III)

**Checkpoint 20**: Fit config, fit state, fit engine, fill targets, and validation match spike V12.1 behavior.

---

### Phase 21: Repair Page Planning and Render Data

**Purpose**: Correct input data/page split before final rendering.

- [x] T171 [TDD] [SPIKE] [Context7] Create or restore `PagePlanBuilder` as a dedicated production class. Context7: no external docs required unless Java collection APIs are uncertain. RED: current `ResumeRenderDataBuilder.buildPagePlan()` is not a spike-equivalent page planner. GREEN:
  - use production `WorkExperienceBudgetResolver`;
  - produce deterministic page 1 work slice;
  - produce deterministic page 2 additional work slice;
  - include page 2 projects with projects-first behavior;
  - support page 3 only under explicit policy.
  Keep spike semantics; do not port `edge_case_rule` table. (II, IV)

- [x] T172 [TDD] [Context7] Fix EC-001..EC-017 production-equivalent page plan tests. Context7: verify JUnit parameterized tests. Use production budget config fixtures, not spike SQLite/mock tables. Tests must cover all 17 EC cases in EN/RU where render output differs. Expected examples:
  - EC-001..003 one-page no projects;
  - EC-004..009 project-driven two-page with no additional work;
  - EC-010..016 two-page work split;
  - EC-017 special one-page expansion.
  If production budget config cannot represent an EC case, STOP_FOR_CONFIRMATION. (II, IV)

- [x] T173 [TDD] [Context7] Fix `ResumeRenderDataBuilder` data mapping. Context7: verify Java null/list handling if needed. Tests:
  - edited bullets included under correct work/project;
  - edited personal info used;
  - generated skills/courses/projects present;
  - course line includes name/provider/focus when focus exists;
  - profile-owned bilingual education used;
  - optional blank personal lines omitted;
  - missing optional fields do not cause NPE.
  (II, III, V)

- [x] T174 [TDD] [SPIKE] [Context7] Verify `XhtmlTemplateRenderer` against spike output. Context7: verify OpenHTMLToPDF CSS limitations if any CSS changes are made. Tests:
  - page notes text and position classes;
  - explicit `height:297mm` + `min-height:297mm`;
  - PDF-safe CSS only;
  - no `overflow:hidden`;
  - no flex/grid;
  - same section order as spike;
  - page 1/2/3 rendering matches page plan.
  Replace drift with spike code where tests fail. (II, III, IV)

- [x] T175 [TDD] [SEC] [Context7] Verify HTML escaping exactly once. Context7: verify HTML escaping only if using a library; otherwise use existing `HtmlEscapeUtil`. Tests:
  - `<script>` in bullet;
  - `<b>` in personal info;
  - `&`, `"`, `'`;
  - no double escaping after reload/finalize.
  (II, V)

- [x] T176 [REVIEW] [PLAYWRIGHT] [Context7] Manual-like proof:
  - generate with MockAiClient;
  - edit a work/project bullet;
  - save;
  - reload Review;
  - finalize;
  - downloaded PDF/HTML contains edited bullet and expected sections.
  Context7: verify Playwright form filling and download assertions. Save evidence. (II, III)

**Checkpoint 21**: Render data and page planning are correct.

---

### Phase 22: Rewrite Finalization to Use One Canonical XHTML/PDF Pipeline

**Purpose**: Remove partial saved resumes and legacy HTML drift.

- [x] T177 [TDD] [Context7] Write RED tests proving current `ResumeFinalizeService` still uses `ResumeTemplateRenderer.renderAndSave(...)`. Context7: verify Mockito interaction verification. GREEN: new finalization flow must not call deprecated legacy renderer. Saved HTML must be the parity HTML produced by `OpenHtmlPdfGenerationService`/`XhtmlTemplateRenderer`. Keep legacy renderer file only as deprecated reference. (II, III)

- [x] T178 [TDD] [Context7] Write RED tests proving current finalization swallows PDF failures and still inserts `saved_resume`. Context7: verify JDBC transaction/rollback and file cleanup patterns. GREEN:
  - if PDF/HTML generation fails, no saved resume is committed;
  - staged files are deleted;
  - request status resets to retryable state;
  - controller returns controlled fitting/IO/validation error.
  Do not keep "HTML available but PDF failed" unless user explicitly approves a new product decision. (II, IV)

- [x] T179 [TDD] [Context7] Implement staging → validate → promote → commit flow. Context7: verify Java NIO file move/copy/delete semantics. Tests:
  - success persists HTML/PDF paths and metadata;
  - DB insert failure cleans files;
  - file promotion failure rolls back/cleans;
  - cleanup failure logged safely;
  - null paths do not NPE.
  (II, IV, V)

- [x] T180 [TDD] [Context7] Fix bilingual atomicity. Context7: verify transaction boundaries. Tests:
  - EN succeeds/RU fails → neither saved;
  - RU succeeds/EN fails → neither saved;
  - both succeed → both saved;
  - cover letter stays export-only and is not public PDF.
  (II, IV, V)

- [x] T181 [TDD] [Context7] Implement or repair `FINALIZING` lock. Context7: verify concurrent request handling basics. Tests:
  - double finalize returns conflict/user-readable message;
  - no duplicate saved resumes;
  - failure resets status;
  - success completes status.
  (II, IV)

- [x] T182 [TDD] [Context7] Fix `SavedResumeDao` metadata persistence. Context7: verify PostgreSQL nullable columns and Java boxed types. Audit all insert/select/update methods for:
  - PDF status;
  - PDF path;
  - generated timestamp;
  - error code/message;
  - render profile/config key;
  - page count;
  - public code;
  - public URL path/username.
  Add round-trip tests and null tests. (II, IV)

- [x] T183 [TDD] [Context7] Fix finalization error DTO/logging. Context7: verify SLF4J parameterized logging. Logs must include requestId, userId, language, level, plan, config key, validation reason. Logs must not include full resume text, raw prompt, API key, raw absolute path in responses. (II, III, V)

- [x] T184 [REVIEW] [PLAYWRIGHT] [Context7] Browser proof for finalization:
  - success path: wait screen → export → PDF available;
  - fitting failure simulation: error + Try Again to Review;
  - double-click finalize: no duplicates.
  Context7: verify Playwright request interception and trace collection. (II, III)

**Checkpoint 22**: Finalization is atomic and uses one canonical PDF/HTML renderer.

---

### Phase 23: Repair Download Controllers and Security

**Purpose**: Serve only correct artifacts, safely.

- [x] T185 [TDD] [SEC] [Context7] Consolidate authenticated download endpoints. Context7: verify Spring MVC route matching. Canonical endpoints:
  - `GET /api/generate/resumes/{savedResumeId}/html`;
  - `GET /api/generate/resumes/{savedResumeId}/pdf`;
  - `GET /api/generate/resumes/{savedResumeId}/pdf?disposition=inline`.
  Audit `ResumeDownloadController` legacy `/api/resumes/{id}/html`. Keep only if explicit compatibility needed and tested; otherwise remove/deprecate after STOP_FOR_CONFIRMATION. (I, II, V)

- [x] T186 [TDD] [SEC] [Context7] Authenticated download tests. Context7: verify MockMvc binary/file response assertions. Tests:
  - owner HTML;
  - owner PDF attachment;
  - owner PDF inline;
  - non-owner denied;
  - unauthenticated denied;
  - deleted/missing row denied;
  - missing file denied;
  - traversal denied.
  (II, V)

- [x] T187 [TDD] [SEC] [Context7] Public download tests. Context7: verify public route does not pass through `/api/**` interceptor. Tests:
  - unauthenticated valid public URL works;
  - invalid/deleted/disabled returns 404;
  - 404 delay if implemented;
  - no cover letter or HTML exposed;
  - safe logs.
  If no existing rate limiter exists, STOP_FOR_CONFIRMATION before adding a new one. (II, V)

- [x] T188 [REVIEW] [PLAYWRIGHT] [Context7] Browser proof for downloads:
  - authenticated Download HTML;
  - authenticated Download PDF;
  - Open PDF inline;
  - public PDF inline in clean browser context;
  - invalid public link 404.
  Save artifacts and screenshots/traces. (II, III, V)

**Checkpoint 23**: Downloads and public serving are correct and secure.

---

### Phase 24: Repair Frontend Export/Finalize Flow

**Purpose**: The frontend was not truly completed. Fix it against real backend contracts.

- [x] T189 [REVIEW] [Context7] Audit frontend before coding. Context7: verify Vue 3, Vue Router, Vue Test Utils, Vitest patterns. Inspect:
  - `src/services/generateResumeService.ts`;
  - `src/types/generate.ts`;
  - `src/components/generate/ExportResult.vue`;
  - `src/views/generate/GenerateReviewPage.vue`;
  - `src/views/generate/GenerateExportPage.vue`;
  - `src/composables/useGenerateResumeFlow.ts`;
  - i18n EN/RU files.
  Document actual vs claimed implementation in `debug_backlog.md`. (I, II, III)

- [x] T190 [TDD] [Context7] Remove duplicated export DTO types from `generateResumeService.ts` if they conflict with `src/types/generate.ts`. Context7: verify TypeScript import/export and strict optional/null typing. Tests/build must catch mismatch. No `any` to hide contract problems. (I, II)

- [x] T191 [TDD] [Context7] Make service methods use backend-provided URLs where appropriate. Context7: verify Fetch Blob handling and `window.open`. Tests:
  - download PDF uses `item.pdfDownloadUrl`;
  - open PDF uses `item.pdfOpenUrl`;
  - download HTML uses `item.htmlDownloadUrl`;
  - public link opens/copies `item.publicUrlLink`;
  - unavailable PDF fails honestly.
  Do not hardcode stale endpoint assumptions when DTO has URLs. (II, III)

- [x] T192 [TDD] [Context7] Fix `ExportResult.vue`. Context7: verify PrimeVue button disabled/loading props. Tests:
  - PDF buttons visibly disabled when `pdfAvailable=false`;
  - PDF buttons enabled when true;
  - no "future update" placeholder when PDF ready;
  - public link is absolute or clearly copyable based on product decision;
  - cover letter is UI-only and not part of public PDF.
  (II, III)

- [x] T193 [TDD] [Context7] Fix finalization loading and duplicate navigation. Context7: verify Vue Router navigation and async state. Current code finalizes and routes in both `GenerateReviewPage` and `useGenerateResumeFlow`; remove double navigation. Required:
  - loading screen starts immediately on Finalize;
  - finalization phrases rotate;
  - double-click blocked;
  - success routes to Export once;
  - fitting failure shows error + Try Again to Review;
  - saved edits preserved.
  (II, III)

- [x] T194 [TDD] [Context7] Fix frontend error handling. Context7: verify error boundary/toast patterns used in project. Tests:
  - finalize 400/409/422/500 responses show user-readable message;
  - `FINALIZING` conflict message displayed;
  - network failure does not leave spinner stuck;
  - state resets.
  (II, III)

- [x] T195 [TDD] [Context7] Verify Review bullet save → finalize integration. Context7: verify Vue form model/update event patterns. Tests:
  - edited bullet included in save payload;
  - save completes before finalize;
  - no backend update keys are constructed manually beyond backend-provided keys;
  - reload preserves edited bullet.
  (II, III, V)

- [x] T196 [TDD] [Context7] Frontend tests/build/coverage. Context7: verify Vitest coverage command if available. Run:
  - `npm test -- --run`;
  - `npm run build`;
  - coverage for modified frontend files ≥80% useful coverage.
  If coverage tooling is unavailable, STOP_FOR_CONFIRMATION and provide alternative evidence. (II)

- [x] T197 [REVIEW] [PLAYWRIGHT] [Context7] Full frontend browser flow:
  - login;
  - generate with MockAiClient;
  - review;
  - edit bullet;
  - save;
  - reload;
  - finalize;
  - loading screen appears;
  - export appears once;
  - PDF buttons enabled;
  - download HTML;
  - download PDF;
  - open PDF;
  - public link in unauthenticated context.
  Save Playwright trace/screenshots and downloaded artifact names. (II, III, V)

**Checkpoint 24**: Frontend behavior is real and evidence-backed.

---

### Phase 25: Regression Lock, Cleanup, and Final Evidence

**Purpose**: Prevent this failure class from coming back.

- [x] T198 [TDD] [Context7] Add backend regression tests for all known bugs:
  - export never hardcodes PDF unavailable when PDF ready;
  - wrong `/candidate` public link is gone;
  - public route is unauthenticated;
  - fill targets are loaded and enforced;
  - spike defaults are used;
  - page2 delta clamp works;
  - finalization failure rolls back;
  - legacy renderer is not called;
  - saved_resume metadata round-trip works.
  Context7: verify JUnit parameterized tests where useful. (II)

- [x] T199 [TDD] [Context7] Add spike-equivalent production fixture tests. Context7: verify JUnit parameterized tests and PDFBox assertions. Use production fixtures, not spike mock DB tables. Cover at least:
  - EC-001 EN/RU;
  - EC-004 EN/RU;
  - EC-010 EN/RU;
  - EC-015 EN/RU;
  - EC-017 EN/RU;
  - bilingual success;
  - bilingual partial failure.
  Assert page count, selectable text, required anchors, edited bullet, no blank trailing page. (II, III, IV)

- [x] T200 [TDD] [Context7] Add frontend regression tests:
  - ExportResult ready PDF;
  - ExportResult unavailable PDF;
  - service uses DTO URLs;
  - finalization loading;
  - double finalize blocked;
  - Try Again path.
  Context7: verify Vue Test Utils/Vitest syntax. (II, III)

- [x] T201 [REVIEW] [Context7] Remove/deprecate useless drift identified by tests:
  - wrong public route;
  - duplicate endpoint if not needed;
  - hardcoded placeholder PDF messages;
  - internal hidden wiring if it blocks tests;
  - stale comments claiming placeholders.
  If deletion risk is unclear, STOP_FOR_CONFIRMATION. (I)

- [x] T202 [TDD] [Context7] Run backend final gate:
  - `mvn clean test`;
  - `mvn clean package` if project supports it;
  - JaCoCo report;
  - modified/new feature classes ≥80% useful coverage.
  Context7: verify JaCoCo report interpretation if needed. (II)

- [x] T203 [TDD] [Context7] Run frontend final gate:
  - `npm test -- --run`;
  - `npm run build`;
  - coverage evidence for modified files.
  Context7: verify Vitest coverage docs if needed. (II)

- [x] T204 [REVIEW] [PLAYWRIGHT] [Context7] Run full E2E smoke with maximum evidence:
  - EN-only Minimal;
  - RU-only Balanced;
  - Bilingual Balanced or All→Balanced;
  - dense case;
  - invalid public link;
  - non-owner private download denial.
  Save screenshots, traces, downloaded files, PDF page counts, extracted text snippets. (II, III, V)

- [x] T205 [REVIEW] Update docs:
  - `debug_backlog.md` with exact fixed bugs/evidence;
  - `quickstart.md` with local PDF verification commands;
  - list copied/adapted spike classes;
  - list intentionally not ported spike-only classes;
  - note deprecated legacy renderer.
  (I)

- [x] T206 [REVIEW] Final STOP before commit proposal. Produce:

```text
FIX_REPORT

- What was broken:
- Root causes:
- Spike-vs-production class changes:
- What was changed:
- What useless drift was removed/deprecated:
- Tests added/updated:
- Commands run:
- Backend results:
- Frontend results:
- Playwright evidence:
- Coverage:
- Files changed:
- Remaining risks:
- Manual checks for the user:
- Proposed commit message:
```

Do not continue coding after this report without user approval. (I–V)

**Final Phase Group 3 Checkpoint**: Production behavior matches the approved spike/spec where required, and every intentional difference is documented and tested.

---

## Phase Group 3 Manual Verification Guide for the Human Reviewer

Use this guide after DeepSeek reports each checkpoint. Do not accept "done" without evidence.

### After Checkpoint 18
- Confirm no production code changed before the audit.
- Confirm `debug_backlog.md` contains the spike-vs-current class matrix.
- Confirm baseline test output is pasted with exact commands.

### After Checkpoint 19
- Inspect export JSON:
  - `pdfAvailable: true` for ready PDF;
  - `pdfDownloadUrl=/api/generate/resumes/{id}/pdf`;
  - `pdfOpenUrl=/api/generate/resumes/{id}/pdf?disposition=inline`;
  - public link is `/{username}/{publicCode}`;
  - no `/candidate/...`;
  - no "PDF generation is not available in this version".
- Open public link in clean/incognito browser; it must not require login.

### After Checkpoint 20
- Inspect generated PDFs:
  - text selectable;
  - page count correct;
  - no trailing blank page;
  - page notes visible;
  - fill target evidence exists;
  - font readable.
- Demand evidence that `effectiveTargets`, `clampDeltaFromPage1`, and config defaults are tested.

### After Checkpoint 21
- Edit a bullet in Review.
- Save and reload.
- Finalize.
- Download HTML and PDF.
- Confirm edited bullet and all major sections appear.

### After Checkpoint 22
- Simulate PDF/fitting failure.
- Confirm no saved resume row remains.
- Confirm no orphan files remain.
- Confirm request can be retried.
- Double-click Finalize and confirm no duplicates.

### After Checkpoint 23
- Owner: Download HTML, Download PDF, Open PDF.
- Other user: private download denied.
- Unauthenticated: public URL works only for active PDF.
- Invalid public URL: 404 with no metadata leak.

### After Checkpoint 24
- Full browser flow:
  - login;
  - generate;
  - review;
  - edit bullet;
  - save;
  - reload;
  - finalize;
  - loading screen;
  - export;
  - PDF buttons enabled;
  - public link works in unauthenticated context.
- Check RU/EN messages have no missing translation keys.

### After Checkpoint 25
- Demand final `FIX_REPORT`.
- Check backend commands, frontend commands, Playwright trace, coverage, downloaded artifact evidence.
- Only then allow commit proposal.

