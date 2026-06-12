---
description: "Task breakdown for Generate Resume feature"
---

# Tasks: Generate Resume Feature

**Input**: Design documents from `specs/007-generate-resume/` plus prototype indexes and dumps.

**Prerequisites**: `spec.md` ✅, `plan.md` ✅, `frontend_prototype_index.md` ✅, `backend_prototype_index.md` ✅, BA artifacts available ✅

**Constitution Compliance**: Every phase MUST comply with ResumAIner Constitution:

- **I** — Code Quality & Maintainability: layered architecture, SOLID/DRY, Javadoc, no Spring Boot/JPA.
- **II** — Testing Excellence: JUnit 5, Mockito, TDD for business logic, JaCoCo 50%+ Service/DAO.
- **III** — User Experience Consistency: i18n EN/RU, dual validation, consistent wizard/review/export UX.
- **IV** — Performance & Reliability: PreparedStatement, JDBC transactions, UTF-8, server-side PDF, budget config.
- **V** — Security by Design: owner-scoped access, PII-safe logging, API key safety, sanitized AI HTML.

**Organization**: Tasks are grouped by dependency phase. Complete phases in order unless tasks are marked `[P]`.

## Format

`[ID] [P?] [TDD?] [SUBAGENT?] [REVIEW?] [Story] Description`

- **[P]**: Can be done in parallel with other [P] tasks in the same phase.
- **[TDD]**: Write failing test first, then implementation, then refactor.
- **[SUBAGENT]**: Safe to dispatch to subagent.
- **[REVIEW]**: Requires code review checkpoint before continuing.

---

## Phase 0: Memory and Prototype Context

**Purpose**: Prevent DeepSeek from ignoring project constraints or rereading giant dumps blindly.

- [ ] T001 [REVIEW] Run `/speckit.memory-md.prepare-context` if available and read `docs/memory/INDEX.md` + `memory-synthesis.md`. Stop on hard conflicts. (Constitution I)
- [ ] T002 [REVIEW] Read `.specify/memory/constitution.md` and current `constitution.md`; summarize relevant constraints in feature `memory.md`. (Constitution I-V)
- [ ] T003 [P] Read `frontend_prototype_index.md`; identify source files required for frontend implementation. (Constitution I, III)
- [ ] T004 [P] Read `backend_prototype_index.md`; identify Python reference files required for backend implementation. (Constitution I, IV)
- [ ] T005 [REVIEW] Confirm no contradiction between BA dump, frontend prototype, backend prototype, and generated spec/plan. Ask user if conflict exists. (Constitution I)

**Checkpoint**: Active feature memory and context are clear before code changes.

---

## Phase 1: Database Migrations — Generate Resume Core

**Purpose**: Establish PostgreSQL schema for generation request/response/prompt/final artifacts.

- [ ] T006 [TDD] [SUBAGENT] Create Flyway migration for `resume_language_mode` handling or controlled check values if not already present. Values: ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL. (Constitution I, IV)
- [ ] T007 [TDD] [SUBAGENT] Create/verify `adaptation_level` seed values: MINIMAL, BALANCED, MAXIMUM. Do not seed ALL as actual response level. (Constitution I, IV)
- [ ] T008 [TDD] Create migration to update `resume_generation_request` with language mode, adaptation selection, include cover letter, AI model, prompt config, budget config, status/error fields. (Constitution I, IV)
- [ ] T009 [TDD] Create migration to update `resume_generation_response` with `language_id`, `adaptation_level_id`, `value_line`, `cover_letter`, and unique `(generation_request_id, language_id, adaptation_level_id)`. (Constitution I, IV)
- [ ] T010 [TDD] [P] Create `generation_response_experience` table. Include response FK, fields, bullets support via child table or normalized text strategy per BA model. (Constitution I, IV)
- [ ] T011 [TDD] [P] Create `generation_response_course` table. (Constitution I, IV)
- [ ] T012 [TDD] [P] Create `generation_response_project` table. (Constitution I, IV)
- [ ] T013 [TDD] [P] Create `generation_response_skill_group` and `generation_response_skill` tables. (Constitution I, IV)
- [ ] T014 [TDD] [P] Create `generation_response_personal` table with location, spoken languages, relocation, business trips, work formats, citizenship, DOB, GPA, order. (Constitution I, IV)
- [ ] T015 [TDD] Create/verify `saved_resume` fields for `html_file_path`, `pdf_file_path`, `public_code`, `public_url_link`, language/template/status/deleted metadata. (Constitution I, IV, V)
- [ ] T016 [TDD] [REVIEW] Verify Flyway migrations apply on fresh DB and do not break existing profile feature. (Constitution IV)

**Checkpoint**: DB can represent Bilingual + All as 6 draft response rows and finalized EN/RU saved resumes.

---

## Phase 2: Database Migrations — Profile Dependencies

**Purpose**: Update profile data required by generation.

- [ ] T017 [TDD] [SUBAGENT] Update `education` table to bilingual factual fields: `institution_name_ru/en`, `degree_ru/en`, `field_of_study_ru/en`. (Constitution I, IV)
- [ ] T018 [TDD] Update Education model/DTO/DAO to use bilingual fields. (Constitution I, II, IV)
- [ ] T019 [TDD] Update Profile API validation so bilingual Education fields are required. (Constitution II, III)
- [ ] T020 [TDD] [P] Verify `work_format` + `user_work_format` remain normalized 3NF and seeded with BA-approved values. (Constitution I, IV)
- [ ] T021 [TDD] [P] Update WorkFormat DAO/service if needed to expose selected codes and localized display names for prompt payload. (Constitution I, II, IV)
- [ ] T022 [REVIEW] Ensure Additional Profile UI hides Default resume language and Additional resume language fields but leaves DB columns for future use. (Constitution III)

**Checkpoint**: Profile can provide bilingual Education and normalized work formats to generation.

---

## Phase 3: Backend Models and DTOs

**Purpose**: Create Java data structures for request, response, review, finalization, and export.

- [ ] T023 [P] Create/update model classes: `ResumeGenerationRequest`, `ResumeGenerationResponse`, `GenerationResponsePersonal`, `SavedResume`, `AiModel`, prompt config models. (Constitution I)
- [ ] T024 [P] Create response section models for experience, course, project, skill group, skill. (Constitution I)
- [ ] T025 [P] Create `GenerationRequestCreateDto` matching Vacancy + Settings frontend payload. (Constitution I, III)
- [ ] T026 [P] Create `GenerationReviewDto` grouped for frontend by language/section/record/field/adaptation level. (Constitution I, III)
- [ ] T027 [P] Create `GenerationReviewUpdateDto` for edited review values. (Constitution I, III)
- [ ] T028 [P] Create `FinalizeResumeRequestDto` with selected adaptation level. (Constitution I)
- [ ] T029 [P] Create `ExportResultDto` and `SavedResumeExportDto` exposing public URL plus PDF/HTML download references. (Constitution I, III)
- [ ] T030 [REVIEW] Compile DTO/model layer and verify no duplicate/conflicting class names. (Constitution I)

**Checkpoint**: Java model/DTO layer compiles.

---

## Phase 4: Backend DAO Layer

**Purpose**: Implement PreparedStatement-only data access.

- [ ] T031 [TDD] [SUBAGENT] Create `AiModelDao` for active model lookup with masked/no-log API key handling. (Constitution II, IV, V)
- [ ] T032 [TDD] [SUBAGENT] Create `PromptConfigDao` for active prompt config and fragment lookup. (Constitution II, IV)
- [ ] T033 [TDD] Create `GenerationRequestDao` for create/read/update status owner-scoped operations. (Constitution II, IV, V)
- [ ] T034 [TDD] Create `GenerationResponseDao` for response rows and child response section rows. (Constitution II, IV)
- [ ] T035 [TDD] Create `GenerationResponsePersonalDao` with one row per response. (Constitution II, IV)
- [ ] T036 [TDD] Create `SavedResumeDao` for saved resume insert/read/download/public lookup. (Constitution II, IV, V)
- [ ] T037 [TDD] Create `ResumeTemplateDao` if templates are DB-backed, or template path lookup service if file-backed. (Constitution II, IV)
- [ ] T038 [TDD] Add DAO method to load full generation response bundle for rendering. (Constitution II, IV)
- [ ] T039 [TDD] Add DAO method to load full profile prompt payload including bilingual Education and normalized work formats. (Constitution II, IV)
- [ ] T040 [REVIEW] Code review: all DAO SQL uses PreparedStatement and owner-scoped filters where applicable. (Constitution IV, V)

**Checkpoint**: DAO tests pass.

---

## Phase 5: Backend Prompt and AI Client

**Purpose**: Implement DB-backed prompt assembly and AI provider abstraction.

- [ ] T041 [TDD] [SUBAGENT] Create `ResumePromptBuilder` using Builder pattern. Load system prompt + language fragment + adaptation fragment + cover letter fragment. (Constitution I, II)
- [ ] T042 [TDD] Ensure prompt payload includes profile work formats as codes plus EN/RU display names. (Constitution II, IV)
- [ ] T043 [TDD] Ensure prompt payload includes bilingual Education but instructs AI not to rewrite factual Education. (Constitution II)
- [ ] T044 [TDD] Store rendered prompt log in `ai_prompt_render_log`. (Constitution II, IV, V)
- [ ] T045 [TDD] Create `AiClient` interface. (Constitution I, II)
- [ ] T046 [TDD] Create `MockAiClient` returning deterministic test JSON. (Constitution II)
- [ ] T047 [TDD] Create `OpenRouterClient` with configured URL/model/API key and safe error handling. (Constitution I, V)
- [ ] T048 [TDD] Create `AiClientFactory` to choose mock vs real provider. (Constitution I, II)
- [ ] T049 [REVIEW] Verify automated tests cannot accidentally call real OpenRouter. (Constitution II, V)

**Checkpoint**: Prompt builder and AI client tests pass.

---

## Phase 6: Backend AI Response Parser and Persistence

**Purpose**: Convert AI JSON into normalized DB rows.

- [ ] T050 [TDD] [SUBAGENT] Create `AiResponseParser` for English-only selected-level response. (Constitution II)
- [ ] T051 [TDD] Extend parser for Russian-only selected-level response. (Constitution II)
- [ ] T052 [TDD] Extend parser for Bilingual selected-level response. (Constitution II)
- [ ] T053 [TDD] Extend parser for Bilingual + All response. Expect EN/RU × Minimal/Balanced/Maximum. (Constitution II)
- [ ] T054 [TDD] Parser must reject missing required top-level fields. (Constitution II, V)
- [ ] T055 [TDD] Parser must parse and validate `personalInfo.workFormats`. (Constitution II)
- [ ] T056 [TDD] Create `GenerationResponsePersistenceService` with transaction boundaries. (Constitution II, IV)
- [ ] T057 [TDD] Persistence must insert `generation_response_personal` for every response row. (Constitution II, IV)
- [ ] T058 [TDD] Persistence must create 6 response rows for Bilingual + All sample. (Constitution II)
- [ ] T059 [REVIEW] Verify no partial committed response rows after parser/persistence failure. (Constitution IV)

**Checkpoint**: Parser and persistence tests pass.

---

## Phase 7: Backend Generation and Review APIs

**Purpose**: Expose generation flow to frontend.

- [ ] T060 [TDD] Create `GenerateResumeController.createRequest` endpoint: `POST /api/generate/requests`. (Constitution I, II, III, V)
- [ ] T061 [TDD] Create `GenerateResumeController.generate` endpoint: `POST /api/generate/requests/{id}/generate`. (Constitution I, II, V)
- [ ] T062 [TDD] Create `ResumeReviewService.getReview` returning grouped review DTO. (Constitution I, II, III)
- [ ] T063 [TDD] Create `GET /api/generate/requests/{id}/review`. (Constitution I, II, III, V)
- [ ] T064 [TDD] Create `ResumeReviewService.saveReviewEdits`. (Constitution I, II, III)
- [ ] T065 [TDD] Create `PUT /api/generate/requests/{id}/review`. (Constitution I, II, III, V)
- [ ] T066 [TDD] Ensure all endpoints extract authenticated user from session and enforce owner scope. (Constitution V)
- [ ] T067 [REVIEW] Manual API smoke test with mock AI client. (Constitution II, III)

**Checkpoint**: Frontend can create request, generate, load Review, and save edits via backend.

---

## Phase 8: Backend HTML Rendering, File Storage, and PDF

**Purpose**: Finalize selected level into saved artifacts.

- [ ] T068 [TDD] Create `GeneratedFileStorageService` for safe path creation under `generated_results/{username}/{public_code}/`. (Constitution I, II, IV, V)
- [ ] T069 [TDD] Create `ResumeTemplateRenderer` that fills one-page/two-page HTML templates from response bundle. (Constitution I, II, IV)
- [ ] T070 [TDD] Renderer uses bilingual Education fields based on response language. (Constitution II, III)
- [ ] T071 [TDD] Renderer uses `generation_response_personal` and includes work formats fallback from profile if response value is missing. (Constitution II)
- [ ] T072 [TDD] Renderer sanitizes AI limited HTML allowlist before final HTML output. (Constitution V)
- [ ] T073 [TDD] Create `PdfGenerationService` interface. (Constitution I, II, IV)
- [ ] T074 [TDD] Create initial HTML-to-PDF converter implementation or safe stub if converter integration is staged, but keep service boundary final. (Constitution I, IV)
- [ ] T075 [TDD] Validate PDF file exists and page count where converter/library supports it. (Constitution IV)
- [ ] T076 [REVIEW] Verify filled HTML is written before PDF conversion. (Constitution IV)

**Checkpoint**: Backend can render HTML and call PDF service boundary.

---

## Phase 9: Backend Finalize, Export, and Public Routes

**Purpose**: Complete saved resume lifecycle.

- [ ] T077 [TDD] Create `ResumeFinalizeService.finalizeRequest(requestId, selectedLevel, userId)`. (Constitution I, II, IV, V)
- [ ] T078 [TDD] Finalization for EN-only selected level creates 1 saved resume. (Constitution II)
- [ ] T079 [TDD] Finalization for RU-only selected level creates 1 saved resume. (Constitution II)
- [ ] T080 [TDD] Finalization for Bilingual + All + selected BALANCED creates 2 saved resumes. (Constitution II)
- [ ] T081 [TDD] Create `POST /api/generate/requests/{id}/finalize`. (Constitution I, II, III, V)
- [ ] T082 [TDD] Create `GET /api/generate/requests/{id}/export`. (Constitution I, II, III, V)
- [ ] T083 [TDD] Create authenticated PDF download endpoint `GET /api/resumes/{id}/pdf`. (Constitution V)
- [ ] T084 [TDD] Create authenticated HTML download endpoint `GET /api/resumes/{id}/html`. (Constitution V)
- [ ] T085 [TDD] Create public PDF route `GET /candidate/{publicCode}` or approved project route. (Constitution III, V)
- [ ] T086 [TDD] Public route returns Gone/Not Found for deleted/inactive resume. (Constitution V)
- [ ] T087 [REVIEW] Verify public route does not expose HTML/private profile data. (Constitution V)

**Checkpoint**: Export flow is complete from backend perspective.

---

## Phase 10: Frontend API Integration

**Purpose**: Replace prototype mocks with production API calls.

- [ ] T088 [P] Create/update `frontend/src/services/generateResumeService.ts` with all Generate Resume endpoints. (Constitution I, III)
- [ ] T089 [P] Update `frontend/src/types/generate.ts` to match backend DTOs. (Constitution I)
- [ ] T090 Update `useGenerateResumeFlow.ts` to store request ID, generated response IDs, selected adaptation level, and export DTO. (Constitution I, III)
- [ ] T091 Update Vacancy page to call create/update request logic or store draft until Settings. (Constitution III)
- [ ] T092 Update Settings page to send language mode, adaptation selection, cover letter flag. (Constitution III)
- [ ] T093 Update Review page to call real generate/review/save endpoints. (Constitution III)
- [ ] T094 Update Export page to consume real export DTO. (Constitution III)
- [ ] T095 Remove production dependency on `generateMockService.ts`; keep only if clearly dev-only. (Constitution I)
- [ ] T096 [REVIEW] Verify no route still depends on hardcoded mock public links. (Constitution III)

**Checkpoint**: Frontend talks to backend for all Generate Resume steps.

---

## Phase 11: Frontend Review and Export UX

**Purpose**: Preserve final prototype behavior while using real data.

- [ ] T097 [P] Ensure Review tabs are exactly: Professional Positioning, Work Experience, Courses and Certifications, Projects and Volunteering, Skills, Personal Information. (Constitution III)
- [ ] T098 [P] Ensure Education is not added back to Review. (Constitution III)
- [ ] T099 [P] Ensure Personal Information tab edits `generation_response_personal` fields. (Constitution III)
- [ ] T100 [P] Ensure bilingual desktop layout displays EN left / RU right. (Constitution III)
- [ ] T101 [P] Ensure all-level field-first layout for single-value sections. (Constitution III)
- [ ] T102 [P] Ensure record-first layout for repeatable sections. (Constitution III)
- [ ] T103 [P] Ensure level badges use one-word labels. (Constitution III)
- [ ] T104 Add/verify Export button: Download HTML with muted helper text. (Constitution III)
- [ ] T105 Wire Download PDF to backend endpoint. (Constitution III, V)
- [ ] T106 Wire Open PDF to public URL. (Constitution III)
- [ ] T107 Wire Download HTML to backend endpoint. (Constitution III, V)
- [ ] T108 Add/verify all EN/RU i18n strings. (Constitution III)
- [ ] T109 [REVIEW] `npm run build` must pass. (Constitution I)

**Checkpoint**: Frontend behavior matches approved prototype.

---

## Phase 12: Profile Frontend/Backend Adjustments

**Purpose**: Implement dependencies discovered during prototype testing.

- [ ] T110 [TDD] Update Education API/DTO/DAO to support bilingual fields end-to-end. (Constitution I, II, III, IV)
- [ ] T111 Update Education frontend form with required bilingual fields. (Constitution III)
- [ ] T112 Update Education frontend validation for bilingual required fields. (Constitution III)
- [ ] T113 Update Education cards to display current UI language values. (Constitution III)
- [ ] T114 Hide Default resume language and Additional resume language fields from Additional Profile Info frontend. (Constitution III)
- [ ] T115 [TDD] Verify WorkFormat profile API returns selected codes and localized labels for generation payload. (Constitution II, IV)
- [ ] T116 [REVIEW] Regression test existing My Profile save/load flows. (Constitution II, III)

**Checkpoint**: Profile dependencies are production-ready for generation.

---

## Phase 13: End-to-End Tests and Manual Smoke

**Purpose**: Verify full feature before completion.

- [ ] T117 [TDD] Backend test: EN-only + Balanced creates expected rows and saved files with mock AI. (Constitution II)
- [ ] T118 [TDD] Backend test: RU-only + Minimal creates expected rows and saved files with mock AI. (Constitution II)
- [ ] T119 [TDD] Backend test: Bilingual + All creates 6 draft rows. (Constitution II)
- [ ] T120 [TDD] Backend test: Bilingual + All finalize Balanced creates 2 saved resumes. (Constitution II)
- [ ] T121 [TDD] Backend test: invalid AI JSON returns user-readable error without partial finalization. (Constitution II, V)
- [ ] T122 [TDD] Backend test: owner cannot access another user's generation request or file downloads. (Constitution II, V)
- [ ] T123 [TDD] Backend test: public PDF route works for active saved resume and rejects deleted/inactive. (Constitution II, V)
- [ ] T124 Manual smoke: full UI EN-only generation with mock AI. (Constitution III)
- [ ] T125 Manual smoke: full UI Bilingual + All + cover letter with mock AI. (Constitution III)
- [ ] T126 Manual smoke: real OpenRouter generation only after mock flow passes. Do not put real API call in automated tests. (Constitution II, V)
- [ ] T127 Verify `mvn clean package` passes and JaCoCo reports coverage. (Constitution II)
- [ ] T128 Verify frontend `npm run build` passes. (Constitution I)

**Checkpoint**: Feature is verifiably working.

---

## Phase 14: Documentation and Memory Capture

**Purpose**: Update BA/dev documentation and durable memory after implementation.

- [ ] T129 [P] Update README/quickstart for Generate Resume API and local testing. (Constitution I)
- [ ] T130 [P] Update API documentation/Swagger annotations for new endpoints. (Constitution I, III)
- [ ] T131 [P] Update Decision Log if implementation creates new technical decisions or supersedes old decisions. (Constitution I)
- [ ] T132 [P] Update developer docs with generated file storage rules and PDF converter boundary. (Constitution I, IV)
- [ ] T133 Run `/speckit.memory-md.capture` to propose durable memory updates. Do not commit memory changes without user approval. (Constitution I)
- [ ] T134 [REVIEW] Final code review against constitution, plan, and this task list. (Constitution I-V)

**Final Checkpoint**: Feature implementation complete and ready for user acceptance testing.

---

## Dependency Notes

- Phase 1 blocks most backend work.
- Phase 2 blocks correct generation payload and rendering.
- Phase 5 blocks real generation.
- Phase 6 blocks Review data.
- Phase 8 blocks Finalize.
- Phase 9 blocks Export.
- Phase 10+ can begin after backend contracts stabilize.

---

## OpenCode Safety Reminders

- Do not use Spring Boot.
- Do not use JPA/Hibernate/ORM.
- Do not concatenate SQL.
- Do not call OpenRouter from tests.
- Do not log API keys or PII.
- Do not simplify work formats into text.
- Do not render final resume in Vue.
- Do not skip saving HTML before PDF conversion.
- Do not expose filesystem paths directly to frontend users.
- Do not change public link format without asking user.
