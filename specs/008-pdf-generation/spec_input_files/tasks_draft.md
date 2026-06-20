---
description: "Task breakdown for PDF/HTML Resume Export and Bullet-Point Review Hardening"
---

# Tasks: PDF/HTML Resume Export and Bullet-Point Review Hardening

**Input**: Design documents from `specs/008-pdf-generation/`

**Prerequisites**: `spec.md` ✅, `plan.md` ✅, approved spike available ✅, current `feat/007-resume-generation` codebase available ✅

**Approved Spike**: `specs/008-pdf-generation/spec_input_files/pdf-spike-openhtmltopdf-v12-final`

**Critical instruction**: DeepSeek/OpenCode MUST port/copy/adapt from the approved spike wherever possible. Do not invent a new PDF/HTML engine. Do not port spike-only mock tables. Do not skip tests.

**Constitution Compliance**: Every phase MUST reference the active ResumAIner Spec Kit constitution principles:

- **I** — Code Quality & Maintainability: layered architecture, KISS, no Spring Boot/JPA/Hibernate
- **II** — Testing Excellence: TDD, useful tests, target 80% coverage for new/modified feature code
- **III** — UX Consistency: EN/RU i18n, stable Review/Export UX, PDF/HTML parity
- **IV** — Performance & Reliability: PreparedStatement, JDBC transactions, bounded fitting attempts, UTF-8, cleanup on failure
- **V** — Security by Design: owner scope, safe public route, no secrets/PII leaks, no raw paths

## Execution Markers

| Marker | Meaning |
|---|---|
| `[P]` | Parallel — can run concurrently with other `[P]` tasks in the same phase |
| `[TDD]` | Test-driven — write/adjust failing test first, implement smallest fix, verify pass |
| `[SUBAGENT]` | Can be delegated to a focused subagent |
| `[REVIEW]` | Stop for human review before proceeding |
| `[PG1]` | Phase Group 1 — Bullet points + review + prompt/parser hardening |
| `[PG2]` | Phase Group 2 — PDF/HTML generation from approved spike |
| `[SPIKE]` | Must inspect/copy/adapt approved spike before coding |

---

## Phase 0: Context, Constitution, and Prototype Loading

**Purpose**: Prevent prototype drift and implementation guesswork.

- [ ] T001 [REVIEW] Read `.specify/memory/constitution.md`. Summarize constraints relevant to this feature in implementation notes. Stop if any requested behavior conflicts with the constitution. (Constitution I–V)
- [ ] T002 [REVIEW] Read `specs/008-pdf-generation/spec.md`, `plan.md`, and this `tasks.md` before coding. (Constitution I)
- [ ] T003 [SPIKE] [REVIEW] Read `specs/008-pdf-generation/spec_input_files/pdf-spike-openhtmltopdf-v12-final/README.md`, `RUN_NOTES.md`, and `TRANSFER_TO_MAIN_PROJECT.md`. Extract production-to-port and spike-only lists. (Constitution I, IV)
- [ ] T004 [SPIKE] Inspect approved spike source tree and identify exact classes to port: renderer, fit engine, analyzer, validator, blank page cleaner, merger, CSS inspector, page planner, fit models. (Constitution I)
- [ ] T005 [REVIEW] Inspect current backend codebase for existing `ResumeBudgetConfigDao`, `ResumeBudgetConfigService`, `WorkExperienceBudgetResolver`, existing PDF stubs, saved resume fields, and bullet tables. Document what exists before adding migrations. (Constitution I, IV)
- [ ] T006 [REVIEW] Confirm implementation branch name and migration numbering. Do not create out-of-order Flyway migrations. (Constitution IV)

**Checkpoint**: Developer knows what to port, what not to port, and what already exists.

---

# Phase Group 1 — Bullet Points + Review + Prompt/Parser Hardening

## Phase 1: PG1 Schema Inspection and Bullet Persistence

**Purpose**: Ensure bullets are first-class persisted data without duplicating existing schema.

- [ ] T007 [PG1] [TDD] Inspect current migrations and DB models for existing work/project bullet storage. If tables already exist, write tests against existing schema before modifying. (Constitution II, IV)
- [ ] T008 [PG1] [TDD] If work experience bullet table is missing, create additive Flyway migration for `generation_response_experience_bullet` or project-convention equivalent. Include parent FK, order, non-empty text constraint where practical, edited marker, timestamps. (Constitution I, IV)
- [ ] T009 [PG1] [TDD] If project bullet table is missing, create additive Flyway migration for `generation_response_project_bullet` or project-convention equivalent. Include parent FK, order, non-empty text constraint where practical, edited marker, timestamps. (Constitution I, IV)
- [ ] T010 [PG1] [TDD] Update model classes for generated experience/project bullets. Keep KISS: simple fields, no over-abstracted inheritance. (Constitution I)
- [ ] T011 [PG1] [TDD] Update DAO insert/read methods to persist and load bullets in deterministic order. Use PreparedStatement only. (Constitution II, IV)
- [ ] T012 [PG1] [TDD] Add DAO tests proving bullet round-trip, order preservation, empty bullet rejection, and cascade/cleanup behavior. (Constitution II)
- [ ] T013 [PG1] [REVIEW] Run Flyway migration on fresh local DB/test DB. Verify no duplicate table names and no conflict with existing generation response tables. (Constitution IV)

**Checkpoint**: DB and DAO layer can store/read work and project bullets.

---

## Phase 2: PG1 Prompt Builder and AI Response Contract

**Purpose**: Make AI output match the structured bullet model.

- [ ] T014 [PG1] [TDD] Update prompt config seed/migration, not only Java hardcode, so the AI is instructed to return `bulletPoints` arrays for work experience. (Constitution I, II)
- [ ] T015 [PG1] [TDD] Update prompt config seed/migration so the AI is instructed to return `bulletPoints` arrays for projects/volunteering. (Constitution I, II)
- [ ] T016 [PG1] [TDD] Update `ResumePromptBuilder` tests to assert prompts mention `bulletPoints`, max 15 words target, max 250 chars hard limit, no fabricated facts, and existing sourceId/profile-owned data rules if present. (Constitution II)
- [ ] T017 [PG1] [TDD] Update `MockAiClient` deterministic responses to include `bulletPoints` for EN/RU and all adaptation levels. (Constitution II)
- [ ] T018 [PG1] [TDD] Update API/contract docs or comments for generated JSON shape. Use camelCase `bulletPoints`. (Constitution I)
- [ ] T019 [PG1] [REVIEW] Verify no automated test calls real OpenRouter. (Constitution II, V)

**Checkpoint**: Prompt and mock AI contract produce structured bullets.

---

## Phase 3: PG1 Parser, Validator, and Persistence

**Purpose**: Parse, validate, and persist bullets transactionally.

- [ ] T020 [PG1] [TDD] Update `AiResponseParser` to parse work experience `bulletPoints` arrays. Reject null, non-array, empty, or whitespace-only bullets where required. (Constitution II)
- [ ] T021 [PG1] [TDD] Update `AiResponseParser` to parse project `bulletPoints` arrays. Reject invalid shape. (Constitution II)
- [ ] T022 [PG1] [TDD] Update `AiResponseValidator` to enforce max bullet length and user-readable validation errors. (Constitution II, III)
- [ ] T023 [PG1] [TDD] Update `GenerationResponsePersistenceService` to insert bullets within the same transaction as parent response/section rows. Roll back all on any bullet failure. (Constitution II, IV)
- [ ] T024 [PG1] [TDD] Add parser tests for EN-only, RU-only, Bilingual, All levels, and invalid/missing bullet cases. (Constitution II)
- [ ] T025 [PG1] [TDD] Add persistence tests proving all response rows and bullet rows roll back on failure. (Constitution II, IV)
- [ ] T026 [PG1] [REVIEW] Run targeted backend tests: parser, validator, persistence, prompt builder. (Constitution II)

**Checkpoint**: AI response with bullets is parsed, validated, and persisted safely.

---

## Phase 4: PG1 Review API and Frontend Bullet Editing

**Purpose**: Expose and edit bullets on Review page.

- [ ] T027 [PG1] [TDD] Update `GenerationReviewDto` to include bullets under generated experience/project records. Preserve record-first grouping. (Constitution I, III)
- [ ] T028 [PG1] [TDD] Update review update-key format or equivalent backend-owned update identity to support bullet edits. Frontend must not invent raw DB paths. (Constitution I, V)
- [ ] T029 [PG1] [TDD] Update `ResumeReviewService` save logic to update bullet text and edited marker. Reject empty bullets. (Constitution II, IV)
- [ ] T030 [PG1] [TDD] Add service/controller tests for bullet edit save/reload and owner-scoped access. (Constitution II, V)
- [ ] T031 [PG1] [P] Update `frontend/src/types/generate.ts` with bullet fields matching backend DTOs. (Constitution I)
- [ ] T032 [PG1] [P] Update `GeneratedRecordGroup.vue` / `ReviewStepForm.vue` to render each bullet as editable field under its parent record. (Constitution III)
- [ ] T033 [PG1] [P] Add frontend validation: bullet cannot be empty/whitespace-only. (Constitution III)
- [ ] T034 [PG1] [P] Ensure MVP Review UI does not add/delete/reorder bullets. Put a code comment if needed. (Constitution I, III)
- [ ] T035 [PG1] [TDD] Add frontend tests for rendering bullets, editing, save payload, and empty bullet validation. (Constitution II, III)
- [ ] T036 [PG1] [REVIEW] Manual smoke: generate with MockAiClient → Review → edit bullet → save → reload → edited bullet persists. (Constitution II, III)

**Checkpoint**: User can edit generated bullets and backend persists them.

---

# Phase Group 2 — PDF/HTML Generation from Approved Spike

## Phase 5: PG2 Dependencies, DB Config, and Saved Resume Metadata

**Purpose**: Prepare production config and dependencies for PDF generation.

- [ ] T037 [PG2] [SPIKE] Inspect spike `pom.xml` for OpenHTMLToPDF/PDFBox dependencies. Add equivalent backend Maven dependencies with versions compatible with Java 21 and existing app. Do not add unrelated PDF libraries. (Constitution I)
- [ ] T038 [PG2] [TDD] Inspect `saved_resume` fields. Add additive migration only for missing PDF metadata fields: `pdf_status`, `pdf_file_path`, `pdf_generated_at`, `pdf_generation_error_code`, `pdf_generation_error_message`, `pdf_render_profile` or config key, `pdf_page_count`, public username/path fields if missing and in scope. (Constitution I, IV)
- [ ] T039 [PG2] [TDD] Add production PDF fit config migration: fit limits and fill targets. Do not reuse spike SQLite table names blindly if project naming convention differs. (Constitution I, IV)
- [ ] T040 [PG2] [TDD] Seed active default PDF fit config from approved spike V12.1 values. Include adaptive page2 min-fill rules: 0 projects can go as low as 0.30. (Constitution IV)
- [ ] T041 [PG2] [TDD] Create `PdfRenderConfigDao` and `PdfRenderConfigService` to load active fit limits/fill targets. Use PreparedStatement. (Constitution II, IV)
- [ ] T042 [PG2] [TDD] Add tests for no active PDF config, active config loading, fill-target selection, and page2 delta limit values. (Constitution II)
- [ ] T043 [PG2] [REVIEW] Human review of migrations before continuing. Confirm no spike-only mock tables are added. (Constitution I, IV)

**Checkpoint**: PDF dependencies and production config exist.

---

## Phase 6: PG2 Port Spike Core Classes

**Purpose**: Bring the proven PDF engine into backend with minimal behavioral drift.

- [ ] T044 [PG2] [SPIKE] [TDD] Port/adapt spike `FitLimits`, `FitState`, `FillTarget`, `FitAttempt`, `FitResult`, and `PdfMetrics` into production package. Keep them simple immutable records/classes. (Constitution I)
- [ ] T045 [PG2] [SPIKE] [TDD] Port/adapt spike `CssSafetyInspector`. Tests must reject browser-only CSS and allow PDF-safe CSS. (Constitution II, IV)
- [ ] T046 [PG2] [SPIKE] [TDD] Port/adapt spike `PdfAnalyzer`. Verify page count and text extraction. (Constitution II)
- [ ] T047 [PG2] [SPIKE] [TDD] Port/adapt spike `PdfValidationService`, including generic text anchors and RU/EN normalization behavior. (Constitution II, III)
- [ ] T048 [PG2] [SPIKE] [TDD] Port/adapt spike `ContentExpectationBuilder`. It must build required text expectations from actual render data, not hardcoded text. (Constitution II)
- [ ] T049 [PG2] [SPIKE] [TDD] Port/adapt spike `PdfBlankPageCleaner`. Verify trailing blank pages are removed. (Constitution II, IV)
- [ ] T050 [PG2] [SPIKE] [TDD] Port/adapt spike `PdfPageMerger`. Verify merged page count. (Constitution II)
- [ ] T051 [PG2] [SPIKE] [TDD] Port/adapt spike `OpenHtmlPdfRenderer`. Configure resource/font path safely for backend runtime. (Constitution I, IV)
- [ ] T052 [PG2] [REVIEW] Compare ported classes against spike. Document any intentional difference. No creative rewrite allowed. (Constitution I)

**Checkpoint**: Core PDF utility classes are ported and unit-tested.

---

## Phase 7: PG2 Render Data Adapter and Page Planning

**Purpose**: Connect current generated response/profile data to the ported renderer.

- [ ] T053 [PG2] [TDD] Create `ResumeRenderData` adapter from finalized generation response + profile-owned data + edited bullets. Equivalent to spike `ResumeData`, but populated from production DAOs. (Constitution I, IV)
- [ ] T054 [PG2] [TDD] Ensure render data uses bilingual Education fields from profile, not AI-generated education. (Constitution II, III)
- [ ] T055 [PG2] [TDD] Ensure render data uses edited `generation_response_personal` values and optional personal lines are omitted when blank. (Constitution II)
- [ ] T056 [PG2] [TDD] Ensure render data uses edited bullet rows in correct order. (Constitution II)
- [ ] T057 [PG2] [TDD] Adapt `PagePlanBuilder` so it uses existing production `WorkExperienceBudgetResolver` / budget rules instead of spike `edge_case_rule`. (Constitution I, IV)
- [ ] T058 [PG2] [TDD] Add tests for one-page, two-page with projects, two-page without projects, and dense RU cases. (Constitution II)
- [ ] T059 [PG2] [REVIEW] Verify no code references spike `ScenarioDao`, `mock_candidate`, `mock_scenario`, or `edge_case_rule` as production source. (Constitution I)

**Checkpoint**: Production data can be converted into a PDF page plan.

---

## Phase 8: PG2 XHTML Renderer and HTML/PDF Parity

**Purpose**: Generate the final XHTML used by both PDF and HTML download.

- [ ] T060 [PG2] [SPIKE] [TDD] Port/adapt spike `XhtmlTemplateRenderer`. Preserve PDF-safe CSS, page notes, page split, contact rows, section order, and optional-line handling. (Constitution I, III, IV)
- [ ] T061 [PG2] [TDD] Ensure renderer outputs page 1 footer note for two-page and three-page artifacts: `SEE THE NEXT PAGE` / `СМ. СЛЕДУЮЩУЮ СТРАНИЦУ`. (Constitution III)
- [ ] T062 [PG2] [TDD] Ensure renderer outputs page 2/page 3 header note: `SEE THE PREVIOUS PAGE` / `СМ. ПРЕДЫДУЩУЮ СТРАНИЦУ`. (Constitution III)
- [ ] T063 [PG2] [TDD] Ensure page notes are visually consistent: font, weight, uppercase, margins, background, border style. (Constitution III)
- [ ] T064 [PG2] [TDD] Ensure renderer uses explicit A4 page height from spike V12.1 so absolute footer notes render reliably. (Constitution IV)
- [ ] T065 [PG2] [TDD] Ensure CSS safety test fails on flexbox, row-gap, modern break-inside, overflow clipping, and other unsupported PDF CSS. (Constitution II, IV)
- [ ] T066 [PG2] [TDD] Mark existing `ResumeTemplateRenderer` as deprecated/legacy. Do not delete. Add tests or wiring checks proving new finalization does not call it. (Constitution I)
- [ ] T067 [PG2] [REVIEW] Render sample HTML from production test data and compare visually to spike output. (Constitution III)

**Checkpoint**: New renderer produces PDF-parity HTML.

---

## Phase 9: PG2 Feedback Fit Engine and Validation

**Purpose**: Fit and validate PDF pages using the spike algorithm.

- [ ] T068 [PG2] [SPIKE] [TDD] Port/adapt spike `FeedbackFitEngine`. Preserve round-robin shrink/grow, adaptive page2 min-fill, missing-text handling, and bounded attempts. (Constitution I, II, IV)
- [ ] T069 [PG2] [TDD] Enforce `page2_delta_limit_percent` for page2/page3 line-height and section-gap relative to page1. (Constitution II, IV)
- [ ] T070 [PG2] [TDD] Add tests for underfill growth, overflow shrink, missing critical text shrink, generic long-text anchors, RU hyphen normalization, and trailing blank page cleanup. (Constitution II)
- [ ] T071 [PG2] [TDD] Add tests that sparse page2 with 0 projects can pass at min fill 0.30 if all required text is present. (Constitution II)
- [ ] T072 [PG2] [TDD] Add tests that dense RU cases do not clip final personal info lines. (Constitution II)
- [ ] T073 [PG2] [TDD] Add logging tests or assertions where practical for reason codes, not full PII text. (Constitution II, V)
- [ ] T074 [PG2] [REVIEW] Run spike-equivalent edge cases using production test harness. All expected scenarios must pass before integrating into finalization. (Constitution II)

**Checkpoint**: Fit engine validates generated PDFs before finalization.

---

## Phase 10: PG2 Real PdfGenerationService and Finalization Integration

**Purpose**: Replace NoOp PDF boundary with real generation.

- [ ] T075 [PG2] [TDD] Implement `OpenHtmlPdfGenerationService implements PdfGenerationService` or update existing service boundary idiomatically. Use ported renderer/fit engine internally. (Constitution I, II)
- [ ] T076 [PG2] [TDD] Update `ResumeFinalizeService` flow to generate parity HTML and PDF. Do not call legacy renderer. (Constitution I, IV)
- [ ] T077 [PG2] [TDD] Implement staging directory and cleanup/compensation on any failure. (Constitution II, IV)
- [ ] T078 [PG2] [TDD] Make bilingual finalization atomic: if EN or RU generation fails, neither saved resume is committed and both staged artifact sets are deleted. (Constitution II, IV)
- [ ] T079 [PG2] [TDD] Store PDF status/path/page count/render config on saved resume only after validation passes. (Constitution II, IV)
- [ ] T080 [PG2] [TDD] On PDF validation failure, store/log safe error reason and return user-readable failure. Do not expose stack traces. (Constitution III, V)
- [ ] T081 [PG2] [TDD] Add tests for success, HTML failure, PDF failure, DB failure after file write, and bilingual partial failure. (Constitution II, IV)
- [ ] T082 [PG2] [REVIEW] Manually inspect generated files from local finalization smoke before wiring frontend. (Constitution II, III)

**Checkpoint**: Backend finalization produces validated PDF + parity HTML safely.

---

## Phase 11: PG2 Export and Download Endpoints

**Purpose**: Make Export page actions real.

- [ ] T083 [PG2] [TDD] Update export DTO so `pdfAvailable=true` when PDF is ready and download/open URLs point to real endpoints. (Constitution I, III)
- [ ] T084 [PG2] [TDD] Update authenticated HTML download endpoint to serve new parity HTML. Do not serve legacy HTML unless explicitly requested by internal/debug mechanism. (Constitution III, V)
- [ ] T085 [PG2] [TDD] Implement authenticated PDF download/open endpoint. Owner-scoped. Supports attachment/inline disposition safely. (Constitution II, V)
- [ ] T086 [PG2] [TDD] Implement or update public PDF route if in scope: `/{username}/{publicCode}`. Return inline PDF only. Return 404 for invalid/deleted/disabled. (Constitution II, V)
- [ ] T087 [PG2] [TDD] Add tests for owner download, non-owner denial, missing file, deleted resume, and public 404 behavior. (Constitution II, V)
- [ ] T088 [PG2] [REVIEW] Confirm no endpoint exposes raw `html_file_path`, `pdf_file_path`, base storage directory, or private HTML publicly. (Constitution V)

**Checkpoint**: Export backend endpoints serve real PDF/HTML artifacts safely.

---

## Phase 12: PG2 Frontend Export Integration

**Purpose**: Connect UI to real artifact endpoints.

- [ ] T089 [PG2] [P] Update `generateResumeService.ts`: real PDF download/open, real HTML download, public link copy. Remove placeholder assumptions for finalized resumes. (Constitution I, III)
- [ ] T090 [PG2] [P] Update `frontend/src/types/generate.ts` for PDF metadata and availability fields. (Constitution I)
- [ ] T091 [PG2] [P] Update `ExportResult.vue` so Download PDF/Open PDF are enabled only when `pdfAvailable=true`; show safe message otherwise. (Constitution III)
- [ ] T092 [PG2] [P] Ensure Download HTML button uses `htmlDownloadUrl` returned by backend and downloads the new parity HTML. (Constitution III)
- [ ] T093 [PG2] [P] Ensure copy public link uses returned public URL and does not fabricate route client-side. (Constitution III, V)
- [ ] T094 [PG2] [TDD] Add/update frontend tests for export buttons, disabled states, real URL handling, and cover letter copy. (Constitution II, III)
- [ ] T095 [PG2] [REVIEW] `npm run build` must pass. (Constitution I)

**Checkpoint**: Frontend Export page uses real PDF/HTML outputs.

---

## Phase 13: Logging, Diagnostics, and Manual Verification

**Purpose**: Make bugs fast to diagnose.

- [ ] T096 [PG2] [TDD] Add structured logging around page plan selection: requestId, savedResumeId, language, adaptation, budget config ID/version, target page count, page split. (Constitution IV, V)
- [ ] T097 [PG2] [TDD] Add debug-level fit attempt logging: attempt number, font, line-height, gaps, page count, fill, validation reason. Do not log full resume text at info/warn/error. (Constitution IV, V)
- [ ] T098 [PG2] [TDD] Add logs for file staging, promotion, cleanup, rollback, and failure reason codes. (Constitution IV)
- [ ] T099 [PG2] [REVIEW] Verify logs are useful and safe: no API keys, no full prompt, no raw PII dump, no stack traces in frontend response. (Constitution V)
- [ ] T100 [PG2] [REVIEW] Manual smoke: EN one-page, RU one-page, EN two-page, RU two-page, bilingual, sparse page2, dense page2. (Constitution II, III)
- [ ] T101 [PG2] [REVIEW] Render/inspect representative PDFs as images. Verify no clipping, overlap, missing notes, extra pages, or broken glyphs. (Constitution II, III)
- [ ] T102 [PG2] [REVIEW] Verify generated PDF text extraction includes required fields, bullets, personal info, and notes. (Constitution II)

**Checkpoint**: Diagnostics and manual proof are sufficient for audit.

---

## Phase 14: End-to-End Regression and Coverage

**Purpose**: Verify feature does not regress generation flow.

- [ ] T103 [TDD] Backend E2E: EN-only + Minimal → edit bullet → finalize → PDF/HTML exist and contain edited bullet. (Constitution II)
- [ ] T104 [TDD] Backend E2E: RU-only + Balanced → final PDF has no clipped final personal info line. (Constitution II)
- [ ] T105 [TDD] Backend E2E: Bilingual + All → finalize Balanced → two PDFs + two parity HTML files. (Constitution II)
- [ ] T106 [TDD] Backend E2E: PDF generation fails → no saved resume committed and staged files deleted. (Constitution II, IV)
- [ ] T107 [TDD] Backend E2E: HTML generation succeeds but PDF fails → rollback and cleanup. (Constitution II, IV)
- [ ] T108 [TDD] Backend E2E: public PDF route works for active resume and 404s for deleted/invalid. (Constitution II, V)
- [ ] T109 [TDD] Frontend E2E/manual: Review bullet edit and Export buttons. (Constitution II, III)
- [ ] T110 [REVIEW] Run `mvn clean package`. Confirm tests pass and coverage target is met or justified. (Constitution II)
- [ ] T111 [REVIEW] Run `npm run build`. Confirm no TypeScript/build errors. (Constitution I)
- [ ] T112 [REVIEW] If coverage is below 80% for new/modified feature code, add useful tests or document explicit justified exception. Do not add meaningless tests. (Constitution II)

**Checkpoint**: Feature works end-to-end and is test-covered.

---

## Phase 15: Documentation and Handoff

**Purpose**: Make implementation auditable and maintainable.

- [ ] T113 [P] Update feature `quickstart.md` with how to run PDF generation locally, how to inspect artifacts, and how to enable debug attempts. (Constitution I)
- [ ] T114 [P] Update developer docs with production PDF config tables and safe operational defaults. (Constitution I, IV)
- [ ] T115 [P] Update decision log if public route, PDF config names, or renderer naming creates new decisions. (Constitution I)
- [ ] T116 [P] Document legacy renderer status: deprecated/replaced, retained for fallback/reference only. (Constitution I)
- [ ] T117 [P] Document spike-only code not ported and production code ported. (Constitution I)
- [ ] T118 [REVIEW] Final review against spec, plan, tasks, constitution, and approved spike. (Constitution I–V)

**Final Checkpoint**: Ready for user acceptance testing.

---

## Dependency Map

```text
Phase 0 Context
  ├── Phase Group 1
  │   ├── Phase 1 Schema
  │   ├── Phase 2 Prompt
  │   ├── Phase 3 Parser/Persistence
  │   └── Phase 4 Review UI/API
  │
  └── Phase Group 2
      ├── Phase 5 Config/Deps
      ├── Phase 6 Port Core Spike Classes
      ├── Phase 7 Render Data/Page Plan
      ├── Phase 8 XHTML Renderer/Parity
      ├── Phase 9 Fit/Validation
      ├── Phase 10 Finalization
      ├── Phase 11 Download Endpoints
      ├── Phase 12 Frontend Export
      ├── Phase 13 Diagnostics
      ├── Phase 14 E2E/Coverage
      └── Phase 15 Docs
```

Phase Group 1 should complete before Phase Group 2 finalization wiring, because PDF rendering must consume finalized structured bullets.

---

## Parallel Opportunities

- Phase 1 schema tests and migration drafting can run in parallel after schema inspection.
- Phase 2 prompt updates can run in parallel with bullet DAO work once DTO contract is decided.
- Phase 6 spike core class porting can run in parallel by package after dependency versions are added.
- Phase 12 frontend export integration can begin after endpoint contracts are stable.
- Documentation tasks can run in parallel after implementation details stabilize.

---

## OpenCode / DeepSeek Safety Rules

- Do not port `edge_case_rule`, `mock_candidate`, or `mock_scenario` to production.
- Do not use `ScenarioDao` or `ResumeDataFactory` as production code.
- Do not invent a new PDF layout.
- Do not remove old `ResumeTemplateRenderer`; mark it deprecated and avoid using it.
- Do not bypass existing `WorkExperienceBudgetResolver`.
- Do not make frontend generate final HTML.
- Do not hide backend fitting bugs by changing frontend mocks.
- Do not log API keys or full prompts.
- Do not expose raw file paths.
- Do not return fake PDF placeholders after implementing real PDF generation.
- Do not change budget rules silently.
- Stop and ask if confidence is below 70%.

