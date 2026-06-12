---
description: "Task breakdown for Generate Resume feature"
---

# Tasks: Resume Generation

**Input**: Design documents from `specs/007-resume-generation/`

**Prerequisites**: `spec.md` ✅, `plan.md` ✅, `component-diagram.md` ✅, `system-design.md` ✅, `software-architecture.md` ✅, `security-review-plan.md` ✅, prototype indexes available ✅

**Constitution Compliance**: Every task phase MUST reference the ResumAIner Constitution principles:
- **I** — Code Quality & Maintainability (layered architecture, SOLID, no Spring Boot/JPA, Javadoc)
- **II** — Testing Excellence (JUnit 5, Mockito, TDD for business logic, JaCoCo 50%+)
- **III** — User Experience Consistency (i18n EN/RU, dual validation, wizard/review/export UX)
- **IV** — Performance & Reliability (PreparedStatement, JDBC transactions, pagination, UTF-8, server-side PDF)
- **V** — Security by Design (owner-scoped access, API key masking, XSS sanitization, no secrets in logs)

## Execution Markers

| Marker | Meaning |
|--------|---------|
| `[P]` | Parallel — can run concurrently with other `[P]` tasks in the same phase |
| `[TDD]` | Test-Driven — write failing test first, then implement, then refactor |
| `[SUBAGENT]` | Subagent — can be dispatched to a parallel subagent |
| `[REVIEW]` | Review Gate — pause for human code review before proceeding |
| `[US1]`–`[US7]` | User Story mapping |

---

## Phase 0: Memory and Prototype Context

**Purpose**: Load project memory, constitution, and prototype indexes before writing code.

- [x] T001 [REVIEW] Run `/speckit.memory-md.prepare-context` if available and read `docs/memory/INDEX.md` + `memory-synthesis.md`. Stop on hard conflicts. (Constitution I)
- [x] T002 [REVIEW] Read `.specify/memory/constitution.md`; summarize relevant constraints in feature `memory.md`. (Constitution I–V)
- [x] T003 [P] Read `frontend_prototype_index.md`; identify source files required for frontend implementation. (Constitution I, III)
- [x] T004 [P] Read `backend_prototype_index.md`; identify Python reference files required for backend implementation. (Constitution I, IV)
- [x] T005 [REVIEW] Confirm no contradiction between BA artifacts, prototypes, and generated spec/plan. Ask user if conflict exists. (Constitution I)

**Checkpoint**: Active feature memory and context are clear before code changes.

---

## Phase 1: Database Migrations — Generation Pipeline

**Purpose**: Establish PostgreSQL schema for generation request/response/prompt/final artifacts.

- [x] T006 [TDD] [SUBAGENT] Inspect highest existing Flyway version in `backend/src/main/resources/db/migration/`. Create migration for `resume_language_mode` handling (ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL). (Constitution I, IV)
- [x] T007 [TDD] [SUBAGENT] Create/verify `adaptation_level` seed values: MINIMAL, BALANCED, MAXIMUM. Do not seed ALL as actual response level. (Constitution I, IV)
- [x] T008 [TDD] Create migration for `resume_generation_request` with language mode, adaptation selection, include cover letter, AI model, prompt config, budget config, status/error fields. (Constitution I, IV)
- [x] T009 [TDD] Create migration for `resume_generation_response` with `language_id`, `adaptation_level_id`, `value_line`, `cover_letter`, and unique `(generation_request_id, language_id, adaptation_level_id)`. (Constitution I, IV)
- [x] T010 [TDD] [P] Create `generation_response_experience` table with response FK, fields, page placement, display order. (Constitution I, IV)
- [x] T011 [TDD] [P] Create `generation_response_course` table. (Constitution I, IV)
- [x] T012 [TDD] [P] Create `generation_response_project` table. (Constitution I, IV)
- [x] T013 [TDD] [P] Create `generation_response_skill` table with skill_group and skill_name. (Constitution I, IV)
- [x] T014 [TDD] [P] Create `generation_response_personal` table: location, spoken_languages, relocation, business_trips, work_formats, citizenship, date_of_birth, gpa_grade, order_in_resume. (Constitution I, IV)
- [x] T015 [TDD] Update `saved_resume` table with `html_file_path`, `pdf_file_path`, `public_code`, `public_url_link`, language/template/deleted metadata. (Constitution I, IV, V)
- [x] T016 [TDD] Create DB-backed prompt config tables: `ai_prompt_config`, `ai_system_prompt`, `ai_request_prompt_language`, `ai_request_prompt_adaptation`, `ai_request_prompt_cover_letter`, `ai_prompt_render_log`. Seed active prompt config and fragments. (Constitution I, IV)
- [x] T016A [TDD] Create or verify DB-backed resume budget configuration tables: `resume_budget_configs`, `resume_template_selection_rules`, `resume_work_experience_distribution_rules`, `resume_section_budget_rules` per BA ERD v4.0. Seed one active default budget configuration for MVP. Ensure `resume_generation_request` stores `budget_config_id` + `budget_config_version_used`. (Constitution I, IV)
- [x] T016B [TDD] Create migration for `ai_usage_log` and `ai_usage_log_response` junction tables per ERD v4.0. Include user_id, ai_model_id, generation_request_id, tokens_sent, tokens_generated, cost (nullable for MVP). (Constitution I, IV)
- [x] T017 [TDD] [REVIEW] Run all Flyway migrations on fresh PostgreSQL. Verify no conflict with existing tables. (Constitution IV)

**Checkpoint**: DB can represent Bilingual + All as 6 draft response rows and finalized EN/RU saved resumes.

---

## Phase 2: Database Migrations — Profile Dependencies

**Purpose**: Update profile tables required by generation (bilingual Education, work formats).

- [x] T018 [TDD] [SUBAGENT] Create migration to update `education` table with bilingual fields: `institution_name_ru/en`, `degree_ru/en`, `field_of_study_ru/en`. Preserve existing data via migration. (Constitution I, IV)
- [x] T019 [TDD] Update Education model/DTO/DAO to support bilingual fields. (Constitution I, II, IV)
- [x] T020 [TDD] Update Education API validation so bilingual fields are required on create/update. (Constitution II, III)
- [x] T021 [TDD] [P] Verify `work_format` + `user_work_format` remain normalized 3NF and seeded with BA-approved values. (Constitution I, IV)
- [x] T022 [TDD] [P] Update WorkFormat DAO/service to expose selected codes and localized display names for prompt payload. (Constitution I, II, IV)
- [x] T023 [REVIEW] Ensure Additional Profile UI hides Default/Language fields but leaves DB columns for future. (Constitution III)
- [x] T023A [TDD] Inspect `users` table for `is_privileged` boolean field. If missing, create Flyway migration adding `is_privileged BOOLEAN NOT NULL DEFAULT FALSE`. Update User model/session DTO so backend services can reliably check privilege server-side. Do not trust frontend-provided privilege flags. (Constitution I, IV, V)
- [x] T023B [TDD] Add tests: regular user has `is_privileged = false` by default; privileged user representation in backend context; AiModelDao filtering uses backend user privilege only. (Constitution II, V)

**Checkpoint**: Profile provides bilingual Education and normalized work formats to generation pipeline. User privilege field exists and is enforced server-side.

---

## Phase 3: Backend Models and DTOs

**Purpose**: Create Java data structures for request, response, review, finalization, and export.

- [x] T024 [P] Create/update model classes: `ResumeGenerationRequest`, `ResumeGenerationResponse`, `GenerationResponsePersonal`, `SavedResume`, `AiModel`, prompt config models. (Constitution I)
- [x] T025 [P] Create response section models for generated experience, course, project, and skill sections. Education is profile-owned bilingual data — do not create `generation_response_education`. Create a separate profile education render DTO/data structure for template rendering only (selects `institution_name_en/ru`, `degree_en/ru`, `field_of_study_en/ru` based on response language). (Constitution I)
- [x] T025A [P] Create `AiUsageLog` and `AiUsageLogResponse` model classes for usage tracking. (Constitution I)
- [x] T026 [P] Create `GenerationRequestCreateDto` matching Vacancy + Settings frontend payload. (Constitution I, III)
- [x] T027 [P] Create `GenerationReviewDto` — grouped by language/section/record/field/adaptation level for frontend. (Constitution I, III)
- [x] T028 [P] Create `GenerationReviewUpdateDto` for saving edited review values. (Constitution I, III)
- [x] T029 [P] Create `FinalizeResumeRequestDto` with selected adaptation level. (Constitution I)
- [x] T030 [P] Create `ExportResultDto` and `SavedResumeExportDto` with public URL plus PDF/HTML download references. (Constitution I, III)
- [x] T031 [P] Create `AiModelDto` for safe AI model data (id, provider, displayName, modelCode — no API key). (Constitution I, V)
- [x] T032 [REVIEW] Compile model/DTO layer and verify no duplicate/conflicting class names with existing codebase. (Constitution I)

**Checkpoint**: Java model/DTO layer compiles with `mvn compile`.

---

## Phase 4: Backend DAO Layer

**Purpose**: Implement PreparedStatement-only data access for all generation tables.

- [x] T033 [TDD] [SUBAGENT] Create `AiModelDao` — active model lookup with `is_active` filter, `is_hidden` + `is_privileged` filtering. Never return `api_key_encrypted` in DTO. (Constitution II, IV, V)
- [x] T034 [TDD] [SUBAGENT] Create `PromptConfigDao` — load active prompt config, system prompt, language/adaptation/cover-letter fragments. (Constitution II, IV)
- [x] T035 [TDD] Create `GenerationRequestDao` — create/read/update status with owner-scoped `WHERE user_id = ?`. (Constitution II, IV, V)
- [x] T036 [TDD] Create `GenerationResponseDao` — insert response rows, child section rows, load response bundle for review/rendering. (Constitution II, IV)
- [x] T037 [TDD] Create `GenerationResponsePersonalDao` — one row per response, insert + update for review edits. (Constitution II, IV)
- [x] T038 [TDD] Create `SavedResumeDao` — insert/read/owner-scoped select/public lookup by `public_code`. (Constitution II, IV, V)
- [x] T038A [TDD] Create `AiUsageLogDao` — insert usage log and link to responses via `ai_usage_log_response`. No frontend read endpoint for usage logs in MVP. (Constitution II, IV, V)
- [x] T039 [TDD] Create `ResumeTemplateDao` — load HTML template by ID or default. (Constitution II, IV)
- [x] T040 [TDD] Add DAO method to load full generation response bundle (response + all child sections + personal) for template rendering. (Constitution II, IV)
- [x] T041 [TDD] Add DAO method to load full profile prompt payload including bilingual Education, contact details, work experience, courses, projects, additional info, work formats. (Constitution II, IV)
- [x] T042 [REVIEW] Code review: all DAO SQL uses `PreparedStatement`, owner-scoped filters, and connection-accepting overloads for transaction support. (Constitution IV, V)

**Checkpoint**: All generation DAO tests pass with `mvn test`.

---

## Phase 5: Backend Prompt Builder and AI Client

**Purpose**: Implement DB-backed modular prompt assembly and AI provider abstraction.

- [x] T043 [TDD] [SUBAGENT] Create `ResumePromptBuilder` (Builder pattern). Load system prompt + language fragment + adaptation fragment + cover letter fragment. Assemble final system and request prompts. (Constitution I, II)
- [x] T044 [TDD] Ensure prompt payload includes profile work formats as codes plus EN/RU display names. (Constitution II, IV)
- [x] T045 [TDD] Ensure prompt payload includes bilingual Education fields; instruct AI not to rewrite factual Education. (Constitution II)
- [x] T046 [TDD] Store rendered prompt log in `ai_prompt_render_log`. No PII in log description field. (Constitution II, IV, V)
- [x] T047 [TDD] Create `AiClient` interface with `generate(systemPrompt, requestPrompt, modelId)` method. (Constitution I, II)
- [x] T048 [TDD] Create `MockAiClient` returning deterministic sample JSON for all test scenarios (EN/RU/Bilingual/All). (Constitution II)
- [x] T049 [TDD] Create `OpenRouterClient` with configured URL/model/API key. Convert provider errors into service exceptions. Never log API key or full request payload. (Constitution I, V)
- [x] T050 [TDD] Create `AiClientFactory` (Factory Method pattern) — return MockAiClient for dev/test, OpenRouterClient for production based on profile. (Constitution I, II)
- [x] T051 [REVIEW] Verify automated tests cannot accidentally call real OpenRouter — mock is default in test profile. (Constitution II, V)

**Checkpoint**: Prompt builder and AI client tests pass. No real API calls in tests.

---

## Phase 6: Backend AI Response Parser and Persistence

**Purpose**: Convert AI structured JSON into normalized DB rows with transaction safety.

- [x] T052 [TDD] [SUBAGENT] Create `AiResponseParser` — parse EN-only + selected adaptation level response. Validate required top-level fields. (Constitution II)
- [x] T053 [TDD] Extend parser for RU-only + selected level. (Constitution II)
- [x] T054 [TDD] Extend parser for Bilingual + selected level — expect 2 response sets. (Constitution II)
- [x] T055 [TDD] Extend parser for Bilingual + All — expect 6 response sets (EN/RU × Min/Bal/Max). (Constitution II)
- [x] T056 [TDD] Parser rejects missing required fields with user-readable error message. (Constitution II, V)
- [x] T057 [TDD] Parser validates and extracts `personalInfo.workFormats` from AI JSON. (Constitution II)
- [x] T058 [TDD] Create `GenerationResponsePersistenceService` with explicit JDBC transaction. Insert response rows + child section rows atomically. (Constitution II, IV)
- [x] T059 [TDD] Persistence creates `generation_response_personal` row for every response row. (Constitution II, IV)
- [x] T060 [TDD] Persistence rolls back entire transaction on any failure — no partial response rows committed. (Constitution IV)
- [x] T061 [TDD] Enforce one active generation per user — check no other request in `processing` status before starting. Return user-readable message if blocked. (Constitution IV, V)
- [x] T061A [TDD] After successful AI call, write usage log entry via `AiUsageLogDao` with tokens_sent, tokens_generated, user_id, ai_model_id, generation_request_id. For Bilingual + All, link one usage log to all 6 response rows via `ai_usage_log_response`. (Constitution II, IV, V)
- [x] T062 [REVIEW] Verify no partial response rows after parser/persistence failure via integration test with MockAiClient. (Constitution II, IV)

**Checkpoint**: Parser and persistence tests pass for all language/adaptation combinations.

---

## Phase 7: Backend Generation, Review, and AI Model APIs

**Purpose**: Expose generation flow to frontend via REST endpoints.

- [x] T063 [TDD] Create `GenerateResumeController` — `POST /api/generate/requests`. Validates `ai_model_id` against user privilege: non-privileged users cannot select hidden models. (Constitution I, II, III, V)
- [x] T064 [TDD] Create `GET /api/generate/ai-models` — returns available models for current user. Filtered by `is_active` and `is_hidden` + user `is_privileged`. Safe metadata only: id, provider, displayName, modelCode. No API key. (Constitution I, II, V)
- [x] T065 [TDD] Create `POST /api/generate/requests/{id}/generate` — synchronous generation orchestration. Enforce one-active-generation check. Return error DTO on failure for frontend error screen. (Constitution I, II, V)
- [x] T066 [TDD] Create `GET /api/generate/requests/{id}/review` — return grouped review DTO by language/adaptation/section. (Constitution I, II, III, V)
- [x] T067 [TDD] Create `PUT /api/generate/requests/{id}/review` — persist user review edits. (Constitution I, II, III, V)
- [x] T068 [TDD] All endpoints extract authenticated user from session and enforce owner scope via `WHERE user_id = ?`. (Constitution V)
- [x] T069 [REVIEW] Manual API smoke test with MockAiClient — full create → generate → review → edit cycle. (Constitution II, III)

**Checkpoint**: Frontend can create request, select AI model, generate, load Review, and save edits via backend.

---

## Phase 8: Backend HTML Rendering, File Storage, and PDF Boundary

**Purpose**: Finalize selected level into saved HTML artifacts. Define PDF service boundary for future conversion.

- [x] T070 [TDD] Create `GeneratedFileStorageService` — build safe path under `generated_results/{username}/{public_code}/`. Sanitize username segment against path traversal (`Path.normalize()`, strip `../`). Write UTF-8 HTML files. (Constitution I, II, IV, V)
- [x] T071 [TDD] Create `ResumeTemplateRenderer` — fill one-page/two-page HTML templates from response bundle. Use marker replacement strategy. (Constitution I, II, IV)
- [x] T072 [TDD] Renderer uses bilingual Education fields (`institution_name_ru/en`, `degree_ru/en`, `field_of_study_ru/en`) based on response language. (Constitution II, III)
- [x] T073 [TDD] Renderer uses `generation_response_personal` for Personal Information section; fallback to profile data only where specified. (Constitution II)
- [x] T074 [TDD] Renderer sanitizes AI-generated HTML content with allowlist before final HTML output. (Constitution V)
- [x] T075 [TDD] Create `PdfGenerationService` interface with `convertPdf(htmlFilePath, pdfFilePath)` method. This is a boundary for `feat/008-pdf-conversion` — no real PDF library is chosen or integrated in feat/007. (Constitution I, II, IV)
- [x] T076 [TDD] Create `NoOpPdfGenerationService` stub implementing `PdfGenerationService`. It returns a clear "PDF generation not available in this feature" response. Do not create fake PDF files or store fake PDF paths. (Constitution I, IV)
- [x] T077 [REVIEW] Verify filled HTML is written to disk during finalization. Verify no fake PDF artifact is exposed as a real PDF. Verify `pdf_file_path` is nullable/not generated until feat/008. (Constitution IV, V)

**Checkpoint**: Backend can render HTML from response data and save to disk. PDF boundary ready for feat/008.

---

## Phase 9: Backend Finalize, Export, and HTML Download

**Purpose**: Complete saved resume lifecycle — finalize, HTML download, export metadata.

- [x] T078 [TDD] Create `ResumeFinalizeService.finalizeRequest(requestId, selectedLevel, userId)`. Flow: (1) render HTML → (2) save HTML to disk — if fail, return error, no further steps → (3) insert saved_resume rows with `html_file_path` set — use DB transaction for the insert only. Record the `public_code` on the saved resume. PDF conversion is not performed in feat/007. (Constitution I, II, IV, V)
- [x] T078A [TDD] Implement file compensation: if saved_resume DB insert fails after HTML file was written, delete orphaned HTML file to prevent garbage accumulation. (Constitution I, IV)
- [x] T078B [TDD] Handle HTML write failure gracefully: return user-readable error, do not attempt any PDF stub call, do not create partial saved resume. No raw filesystem error or stack trace is returned to frontend. Existing valid saved resumes are not affected. (Constitution IV, V)
- [x] T079 [TDD] Finalization for EN-only + selected level creates 1 saved resume with HTML file. (Constitution II)
- [x] T080 [TDD] Finalization for Bilingual + selected level creates 2 saved resumes (EN + RU) with HTML files. (Constitution II)
- [x] T081 [TDD] Finalization for Bilingual + All + select BALANCED creates 2 saved resumes from the 6 draft rows. (Constitution II)
- [x] T082 [TDD] Create `POST /api/generate/requests/{id}/finalize`. Owner-scoped. Returns export DTO. (Constitution I, II, III, V)
- [x] T083 [TDD] Create `GET /api/generate/requests/{id}/export` — return export DTO with: real `htmlDownloadUrl`, placeholder `pdfDownloadUrl`, placeholder `pdfOpenUrl`, placeholder `publicUrlLink`, `pdfAvailable=false`, `pdfMessage` (user-facing "PDF generation coming in a future update"), and `coverLetter` if generated. Response must not expose raw server file paths. Must not claim PDF is generated. (Constitution I, II, III, V)
- [x] T084 [TDD] Create `GET /api/resumes/{id}/html` — authenticated download. Verify owner before streaming file. Return 403/404 safely for non-owner or missing file. Do not accept raw file paths from request parameters. Stream HTML file. Muted helper text for HTML download. (Constitution III, V)
- [x] T085 [TDD] Create `GET /api/resumes/{id}/pdf` — placeholder stub in feat/007. Returns safe "PDF generation not available yet" response. No fake PDF file created. Real PDF download will be implemented in `feat/008-pdf-conversion`. (Constitution V)
- [x] T086 [TDD] Public PDF route `GET /candidate/{publicCode}` — placeholder in feat/007. Returns safe placeholder response; does not expose resume data. Export DTO may provide placeholder `publicUrlLink`. Real public PDF serving deferred to `feat/008-pdf-conversion`. (Constitution III, V)
- [x] T087 [REVIEW] Verify no fake PDF artifacts are exposed as real PDFs. Verify download endpoints are owner-scoped. Verify `pdf_file_path` is nullable and not generated in feat/007. Verify placeholder public link does not expose resume data. Verify Export DTO has `pdfAvailable=false` and a safe `pdfMessage`. (Constitution V)

**Checkpoint**: Full export flow complete from backend — finalize, HTML download, export metadata.

---

## Phase 10: Frontend API Integration and State Management

**Purpose**: Replace prototype mocks with production backend API calls.

- [x] T088 [P] Create `frontend/src/services/generateResumeService.ts` — all endpoints: createRequest, getAiModels, generate, getReview, saveReview, finalize, getExport, downloadPdf, downloadHtml. `downloadHtml` calls real authenticated owner-scoped endpoint. `downloadPdf` and public link actions are placeholders in feat/007 (use placeholder URLs/responses). Keep all methods so feat/008 can replace placeholders with real URLs without redesigning the service. Use shared `httpClient.ts` for CSRF token handling. (Constitution I, III)
- [x] T089 [P] Update `frontend/src/types/generate.ts` to match backend DTOs exactly. (Constitution I)
- [x] T090 Update `useGenerateResumeFlow.ts` composable — manage wizard state: requestId, aiModelId, generated variant IDs, selected adaptation level, export DTO. (Constitution I, III)
- [x] T091 [US1] Update Vacancy page (`GenerateVacancyPage.vue`) — call `createRequest` on continue. Pass requestId to Settings. (Constitution III)
- [x] T092 [US2] Update Settings page (`GenerateSettingsPage.vue`) — call `getAiModels` to populate AI model dropdown. Send language mode, adaptation selection, cover letter flag, selected modelId. (Constitution III)
- [x] T093 [US3] Update Review page (`GenerateReviewPage.vue`) — call `generate` on mount, call `getReview` to load data, call `saveReview` on edit save. Handle generation error → redirect to error page. (Constitution III)
- [x] T094 [US6] Update Export page (`GenerateExportPage.vue`) — call `getExport` to load export DTO. (Constitution III)
- [x] T095 Remove production import of `generateMockService.ts`. Keep only as dev-only reference. (Constitution I)
- [x] T096 [REVIEW] Verify no route still depends on hardcoded mock data. `npm run build` passes. (Constitution I, III)

**Checkpoint**: Frontend talks to real backend for all generation steps.

---

## Phase 11: Frontend Wizard and Review UX

**Purpose**: Build and polish the Generate Resume wizard pages with proper UX.

- [x] T097 [P] Create `GenerateVacancyPage.vue` with `VacancyStepForm` — vacancy title, description, company name, description, additional comments. Validation on required fields. (Constitution III)
- [x] T098 [P] Create `GenerateSettingsPage.vue` with `SettingsStepForm` — language mode radio, adaptation selection radio, AI model dropdown, cover letter checkbox. Enforce ALL is request-only selection. (Constitution III)
- [x] T099 [P] Create `GenerateErrorPage.vue` — temporary error screen shown only after generation failure. Two actions: "Try again" (retry with same settings) and "Change settings" (return to Settings). No raw errors/stack traces. Vacancy data preserved. (Constitution III, V)
- [x] T100 [P] Create `GenerateReviewPage.vue` with `ReviewStepForm` — tabs for each section, adaptation level radio group, save/finalize actions. (Constitution III)
- [x] T101 [P] Create `GenerateExportPage.vue` with `ExportResult` — all prototype actions: Copy public link, Download PDF, Open PDF, Download HTML, Copy cover letter. HTML download is functional and owner-scoped. PDF/public link actions use placeholder URLs/responses in feat/007 (no fake PDFs, no real public PDF access). UI makes clear PDF generation is coming in feat/008. Keep all buttons so feat/008 can replace placeholders without redesigning Export UI. (Constitution III)
- [x] T102 [P] Create shared components: `GenerateStepper`, `WhimsicalLoader`, `BilingualDivider`, `AdaptationLevelRadioGroup`. (Constitution III)
- [x] T103 [P] Create `GeneratedRecordGroup` for record-first repeatable sections (work experience, courses, projects). (Constitution III)
- [x] T104 [P] Create `GeneratedVariantTextarea` for field-level editing across Minimal/Balanced/Maximum variants. (Constitution III)
- [x] T105 [P] Ensure Review tabs: Professional Positioning, Work Experience, Courses and Certifications, Projects and Volunteering, Skills, Personal Information. No Education tab. (Constitution III)
- [x] T106 [P] Ensure bilingual desktop layout: EN left column, RU right column. (Constitution III)
- [x] T107 [P] Ensure all-level field-first layout for single-value sections. (Constitution III)
- [x] T108 [P] Ensure level badges use one-word labels: Minimal / Balanced / Maximum and RU: Минимальная / Сбалансированная / Максимальная. (Constitution III)
- [x] T109 [P] Add all EN/RU i18n strings to `en.json` and `ru.json`. (Constitution III)
- [x] T109A [P] Verify no frontend/controller endpoint exposes `ai_prompt_render_log`. Prompt render logs are append-only in MVP; admin access is DB-only. Search all controller and service files for forbidden prompt-log read endpoints. (Constitution V)
- [x] T110 Add `/generate/*` routes to `frontend/src/router/index.ts`. Guard: require auth. (Constitution III)
- [x] T111 [REVIEW] `npm run build` passes with no lint errors. (Constitution I)

**Checkpoint**: Frontend behavior matches approved prototype and spec.

---

## Phase 12: Profile Frontend/Backend Adjustments

**Purpose**: Implement Education bilingual fields and work format enhancements required by generation.

- [x] T112 [TDD] Update Education backend API/DTO/DAO — bilingual fields end-to-end: `institutionNameRu/En`, `degreeRu/En`, `fieldOfStudyRu/En`. (Constitution I, II, III, IV)
- [x] T113 Update Education frontend form — required RU/EN pairs for institution, degree, field of study. (Constitution III)
- [x] T114 Update Education frontend validation — both RU and EN fields required. (Constitution III)
- [x] T115 Update Education cards display — show value matching current UI language. (Constitution III)
- [x] T116 Hide Default resume language and Additional resume language fields from Additional Profile Info frontend. (Constitution III)
- [x] T117 [TDD] Verify WorkFormat profile API returns selected codes and localized labels for generation payload. (Constitution II, IV)
- [x] T118 [REVIEW] Regression test My Profile save/load flows — Education, Work Formats, Additional Info. (Constitution II, III)

**Checkpoint**: Profile dependencies are production-ready for generation.

---

## Phase 13: End-to-End Tests and Manual Smoke

**Purpose**: Verify full feature end-to-end before completion.

- [x] T119 [TDD] Backend test: EN-only + Balanced → 1 response row, 1 saved resume, HTML file created. (Constitution II)
- [x] T120 [TDD] Backend test: RU-only + Minimal → 1 response row, 1 saved resume, HTML file created. (Constitution II)
- [x] T121 [TDD] Backend test: Bilingual + All → 6 draft response rows. (Constitution II)
- [x] T122 [TDD] Backend test: Bilingual + All + finalize Balanced → 2 saved resumes (EN + RU) with HTML files. (Constitution II)
- [x] T123 [TDD] Backend test: invalid AI JSON → user-readable error, no partial saved resumes. (Constitution II, V)
- [x] T124 [TDD] Backend test: one active generation — second generate request returns blocked message. (Constitution II, V)
- [x] T125 [TDD] Backend test: non-privileged user cannot select hidden AI model. Privileged user can. (Constitution II, V)
- [x] T126 [TDD] Backend test: owner access — user A cannot access user B's request/review/export/html download. (Constitution II, V)
- [x] T127 [TDD] Backend test: HTML download works for owner; non-owner gets 403. Verify raw `html_file_path` is not exposed in frontend DTOs. (Constitution II, V)
- [x] T127B [TDD] Backend test: Export DTO contains `pdfAvailable=false`, placeholder URLs, and does not claim PDF is generated. (Constitution II, V)
- [x] T127A [TDD] Backend test: HTML write failure returns user-readable error, no partial saved resume created. (Constitution II, IV, V)
- [x] T128 [TDD] Backend test: file path traversal — username with `../` is rejected by GeneratedFileStorageService. (Constitution II, V)
- [x] T128A [TDD] Backend test: generation fails with user-readable/internal-safe error if no active budget config exists. (Constitution II, IV)
- [x] T128B [TDD] Backend test: active budget config is loaded before generation; version used is stored on the request. (Constitution II, IV)
- [x] T128C [TDD] Backend test: regular user has `is_privileged = false` by default; privileged user recognized in backend context. (Constitution II, V)
- [x] T128D [TDD] Backend test: successful AI call creates ai_usage_log entry with tokens. (Constitution II, IV)
- [x] T128E [TDD] Backend test: Bilingual + All links one usage log to 6 response rows via ai_usage_log_response. (Constitution II, IV)
- [x] T128F [TDD] Backend test: missing token usage from AI response stores zero safely, does not fail. (Constitution II, IV)
- [x] T128G [TDD] Backend test: no frontend/controller endpoint exposes ai_prompt_render_log data. (Constitution II, V)
- [x] T129 Manual smoke: full UI EN-only generation with MockAiClient. (Constitution III)
- [x] T130 Manual smoke: full UI Bilingual + All + cover letter with MockAiClient. (Constitution III)
- [x] T131 Manual smoke: generation error → error screen with Try again / Change settings. (Constitution III, V)
- [x] T132 Manual smoke: Export page shows all prototype actions — Copy public link, Download PDF, Open PDF, Download HTML, Copy cover letter. HTML download is functional. PDF/public link actions are placeholders with clear "coming in feat/008" message. Placeholder public link does not expose resume data. (Constitution III)
- [x] T133 Manual smoke: real OpenRouter generation ONLY after mock flow passes. Not in automated tests. (Constitution II, V)
- [x] T134 Verify `mvn clean package` passes with JaCoCo coverage ≥ 50% on Service/DAO. (Constitution II)
- [x] T135 Verify `npm run build` passes. (Constitution I)

**Checkpoint**: Feature is verifiably working and tested. PDF conversion deferred to feat/008-pdf-conversion.

---

## Phase 14: Documentation, Memory Capture, and Final Review

**Purpose**: Complete documentation and durable memory after implementation.

- [x] T135 [P] Update `docs/memory/WORKLOG.md` with Feature 007 completion milestone. (Constitution I)
- [x] T136 [P] Update Swagger/OpenAPI annotations for all new endpoints. (Constitution I, III)
- [x] T137 [P] Update Decision Log (`docs/memory/DECISIONS.md`) if implementation creates new technical decisions or supersedes old ones. (Constitution I)
- [x] T138 [P] Update developer docs with generated file storage rules, PDF converter boundary, prompt config management. (Constitution I, IV)
- [x] T139 Run `/speckit.memory-md.capture` to propose durable memory updates. Do not commit memory changes without user approval. (Constitution I)
- [x] T140 [REVIEW] Final code review against constitution, spec, plan, and task list. (Constitution I–V)

**Final Checkpoint**: Feature 007 implementation complete and ready for user acceptance testing.

---

## Dependency Map

```text
Phase 0 (Context) ──► Phase 1 (Migrations: Pipeline)
                          │
                    Phase 2 (Migrations: Profile) ──► Phase 12 (Profile UI)
                          │
                    Phase 3 (Models/DTOs) ──► Phase 4 (DAOs)
                          │
                    Phase 5 (Prompt + AI Client) ──► Phase 6 (Parser + Persistence)
                                                          │
                    Phase 7 (Generation APIs) ◄──────────────┘
                          │
                    Phase 8 (Rendering + PDF boundary)
                          │
                    Phase 9 (Finalize + Export)
                          │
               ┌──────────┘
               ▼
          Phase 10 (Frontend API) ──► Phase 11 (Frontend UX)
               │
          Phase 13 (E2E Tests) ──► Phase 14 (Docs + Memory)
```

### Phase Dependencies

| Phase | Depends On | Blocks |
|-------|-----------|--------|
| 0 — Context | Nothing | All phases |
| 1 — Pipeline migrations | Phase 0 | Phases 3–9 |
| 2 — Profile migrations | Phase 0 | Phase 12 |
| 3 — Models/DTOs | Phase 1 | Phase 4 |
| 4 — DAOs | Phase 3 | Phases 5–9 |
| 5 — Prompt + AI Client | Phase 4 | Phase 6 |
| 6 — Parser + Persistence | Phases 4–5 | Phase 7 |
| 7 — Generation APIs | Phases 4–6 | Phases 10–11 |
| 8 — Rendering + PDF | Phase 4 | Phase 9 |
| 9 — Finalize + Export | Phases 7–8 | Phase 10 |
| 10 — Frontend API | Phases 7, 9 | Phase 11 |
| 11 — Frontend UX | Phase 10 | Phase 13 |
| 12 — Profile UI | Phase 2 | Phase 13 |
| 13 — E2E Tests | Phases 10–12 | Phase 14 |
| 14 — Docs + Memory | Phase 13 | — |

### Parallel Opportunities

- **Phase 1 tasks T010–T014**: All child response table migrations can run in parallel
- **Phase 3 tasks T024–T031**: All model/DTO creation tasks can run in parallel
- **Phase 4 tasks T033–T034**: AiModelDao and PromptConfigDao are independent
- **Phase 11 tasks T097–T110**: All frontend component/page tasks can run in parallel after API service layer is stable
- **Phase 14 tasks T135–T138**: Documentation updates can run in parallel

### [SUBAGENT] Opportunities

- T006, T007 — Flyway migrations for reference data (simple SQL, well-defined)
- T018 — Education bilingual migration (well-defined scope)
- T033, T034 — AiModelDao, PromptConfigDao (isolated DAOs)
- T043 — ResumePromptBuilder (Builder pattern, isolated logic)
- T052 — AiResponseParser initial version (parse-only, no side effects)

### OpenCode Safety Reminders

- Do not use Spring Boot or JPA/Hibernate.
- Do not concatenate SQL — use `PreparedStatement`.
- Do not call real OpenRouter in automated tests.
- Do not log API keys or PII.
- Do not simplify work formats into comma-separated text.
- Do not render final HTML in Vue — backend owns all rendering.
- Do not skip saving HTML to disk during finalization (DEC-073). HTML is the canonical artifact in feat/007.
- Do not create fake PDF files or store fake PDF paths in feat/007 — PDF is deferred to feat/008.
- Do not expose filesystem paths directly to frontend users — use download endpoints.
- Do not expose API keys in AI model dropdown response.
- Sanitize AI-generated HTML with allowlist before storage/rendering.
- Enforce owner-scoped WHERE clauses on all private queries.

