# Plan Input Draft — Generate Resume Feature

**Recommended Spec Kit command:** `/speckit.plan`  
**Feature branch suggestion:** `feat/007-generate-resume`  
**Spec source:** `spec_input_draft.md` / generated `spec.md`  
**Reference first:** `frontend_prototype_index.md`, `backend_prototype_index.md`

---

## 0. Mandatory Pre-Plan Instructions for OpenCode

Before creating or updating `plan.md`:

1. Run `/speckit.memory-md.prepare-context` if available.
2. Read `docs/memory/INDEX.md` and `memory-synthesis.md`.
3. Read `.specify/memory/constitution.md` and attached/current `constitution.md`.
4. Read `frontend_prototype_index.md` and `backend_prototype_index.md`.
5. Only then open targeted files from `frontend_prototype_dump.md` and `backend_prototype_dump.md`.
6. Do not proceed if there is a hard conflict with architecture memory or constitution.

---

## 1. Summary

Implement the full Generate Resume feature as a brownfield extension to the existing ResumAIner Java/Spring MVC + Vue application.

The feature includes:

- generation request creation from vacancy/settings;
- DB-backed AI model and prompt configuration;
- OpenRouter integration behind an AI client abstraction;
- mock AI client for tests;
- structured AI JSON parsing;
- response persistence by language and adaptation level;
- editable Review UI;
- Personal Information stored as `generation_response_personal`;
- bilingual Education profile model;
- backend HTML template rendering;
- server-side PDF generation through a separate service;
- saved HTML/PDF artifacts and export endpoints;
- public recruiter PDF links.

---

## 2. Technical Context

**Backend language/version:** Java 21  
**Backend framework:** Spring MVC 6.x, not Spring Boot  
**Frontend:** Vue 3 + Vite + PrimeVue + vue-router + vue-i18n  
**Database:** PostgreSQL 17  
**Database access:** plain JDBC only, no ORM/JPA/Hibernate  
**Migrations:** Flyway  
**Testing:** JUnit 5 + Mockito + JaCoCo  
**AI provider:** OpenRouter, accessed through backend service only  
**PDF:** server-side HTML-to-PDF conversion from saved filled HTML templates  
**Deployment target:** existing Docker Compose/Tomcat/Vue/PostgreSQL setup

---

## 3. Constitution Check

| Principle | Status | Plan Response |
|---|---|---|
| I. Code Quality & Maintainability | Pass required | Use layered Java packages: controller/service/dao/model/dto/config/util. Prompt Builder, AI Client Factory, Strategy for adaptation selection. No Spring Boot/JPA. |
| II. Testing Excellence | Pass required | Unit tests for DAO/service/parser/prompt builder/finalize. Mock AI provider for all automated tests. JaCoCo 50%+ for Service/DAO. |
| III. User Experience Consistency | Pass required | Vue i18n EN/RU for all strings. Dual validation. Preserve wizard UX and Review/Export prototype behavior. |
| IV. Performance & Reliability | Pass required | PreparedStatement-only SQL. Manual transactions around generation and finalization. UTF-8 file writes. Server-side PDF. Budget config read before generation. |
| V. Security by Design | Pass required | Owner-scoped private endpoints. API keys masked/no logs. PII-safe logs. Sanitized AI HTML. Public PDF route only exposes saved PDF. |

---

## 4. Architecture Overview

### 4.1 Backend Flow

```text
Vue Vacancy/Settings
  → POST /api/generate/requests
  → resume_generation_request

Vue Generate action
  → POST /api/generate/requests/{id}/generate
  → ResumeGenerationService
  → ResumePromptBuilder
  → AiClientFactory → OpenRouterClient or MockAiClient
  → AiResponseParser
  → GenerationResponsePersistenceService
  → resume_generation_response + child tables

Vue Review
  → GET /api/generate/requests/{id}/review
  → Review DTO grouped by language/section/record/field/level

Vue Save Review Edits
  → PUT /api/generate/requests/{id}/review
  → persist edited response rows

Vue Finalize
  → POST /api/generate/requests/{id}/finalize
  → ResumeFinalizeService
  → ResumeTemplateRenderer saves filled HTML
  → PdfGenerationService converts HTML to PDF
  → SavedResumeDao inserts saved_resume rows

Vue Export
  → GET /api/generate/requests/{id}/export
  → export DTO with public links and download endpoints
```

### 4.2 Frontend Flow

```text
/generate/vacancy
  → create or update draft request input

/generate/settings
  → select language mode, adaptation, cover letter

/generate/review
  → load generated variants, edit fields, select final level

/generate/export
  → show public links, PDF download/open, HTML download, cover letter copy
```

---

## 5. Project Structure

### Backend Source Structure

```text
backend/src/main/java/com/resumainer/
├── controller/
│   └── GenerateResumeController.java
├── service/
│   ├── GenerationRequestService.java
│   ├── ResumeGenerationService.java
│   ├── ResumePromptBuilder.java
│   ├── AiResponseParser.java
│   ├── GenerationResponsePersistenceService.java
│   ├── ResumeReviewService.java
│   ├── ResumeFinalizeService.java
│   ├── ResumeTemplateRenderer.java
│   ├── PdfGenerationService.java
│   └── GeneratedFileStorageService.java
├── service/ai/
│   ├── AiClient.java
│   ├── AiClientFactory.java
│   ├── OpenRouterClient.java
│   └── MockAiClient.java
├── dao/
│   ├── AiModelDao.java
│   ├── PromptConfigDao.java
│   ├── GenerationRequestDao.java
│   ├── GenerationResponseDao.java
│   ├── GenerationResponsePersonalDao.java
│   ├── SavedResumeDao.java
│   └── ResumeTemplateDao.java
├── dto/generate/
│   ├── GenerationRequestCreateDto.java
│   ├── GenerationSettingsDto.java
│   ├── GenerationReviewDto.java
│   ├── GenerationReviewUpdateDto.java
│   ├── FinalizeResumeRequestDto.java
│   ├── ExportResultDto.java
│   └── SavedResumeExportDto.java
├── model/
│   ├── ResumeGenerationRequest.java
│   ├── ResumeGenerationResponse.java
│   ├── GenerationResponsePersonal.java
│   ├── SavedResume.java
│   ├── AiModel.java
│   └── PromptConfig related models
├── util/
│   ├── PublicCodeGenerator.java
│   ├── HtmlSanitizer.java
│   └── JsonUtils.java
└── config/
    └── WebConfig.java
```

### Frontend Source Structure

```text
frontend/src/
├── views/generate/
│   ├── GenerateVacancyPage.vue
│   ├── GenerateSettingsPage.vue
│   ├── GenerateReviewPage.vue
│   └── GenerateExportPage.vue
├── components/generate/
│   ├── VacancyStepForm.vue
│   ├── SettingsStepForm.vue
│   ├── ReviewStepForm.vue
│   ├── GeneratedRecordGroup.vue
│   ├── GeneratedVariantTextarea.vue
│   ├── AdaptationLevelRadioGroup.vue
│   ├── ExportResult.vue
│   └── WhimsicalLoader.vue
├── services/
│   └── generateResumeService.ts
├── composables/
│   └── useGenerateResumeFlow.ts
├── types/
│   └── generate.ts
└── i18n/
    ├── en.json
    └── ru.json
```

---

## 6. Database / Flyway Plan

### 6.1 New or Updated Tables

Implement Flyway migrations for:

1. Update `education` to bilingual fields:
   - `institution_name_ru`
   - `institution_name_en`
   - `degree_ru`
   - `degree_en`
   - `field_of_study_ru`
   - `field_of_study_en`
2. Add/confirm `resume_language_mode` representation.
3. Add/confirm `adaptation_level` lookup includes MINIMAL/BALANCED/MAXIMUM.
4. Update `resume_generation_request`:
   - `language_mode`
   - `adaptation_selection` or adaptation request selection field;
   - `include_cover_letter`
   - `prompt_config_id`
   - `budget_config_id`
   - request status fields.
5. Update `resume_generation_response`:
   - `language_id`
   - `adaptation_level_id`
   - unique `(generation_request_id, language_id, adaptation_level_id)`.
6. Add/confirm child response tables:
   - `generation_response_experience`
   - `generation_response_course`
   - `generation_response_project`
   - `generation_response_skill_group`
   - `generation_response_skill`
   - `generation_response_personal`
7. Add DB-backed prompt config tables:
   - `ai_prompt_config`
   - `ai_system_prompt`
   - `ai_request_prompt_language`
   - `ai_request_prompt_adaptation`
   - `ai_request_prompt_cover_letter`
   - `ai_prompt_render_log`
8. Update `saved_resume`:
   - `html_file_path`
   - `pdf_file_path`
   - `public_code`
   - `public_url_link`
   - language/template metadata if not already present.
9. Confirm normalized work formats:
   - `work_format`
   - `user_work_format`.

### 6.2 PostgreSQL Integrity Rules

- Partial unique index: only one active prompt config.
- Partial unique index: only one active budget config.
- Unique prompt fragments per config and controlled value.
- Unique response row per request/language/adaptation.
- Unique public code on saved resumes.
- Foreign keys for all child response rows.

---

## 7. Backend API Plan

### 7.1 Generation Request API

```http
POST /api/generate/requests
GET  /api/generate/requests/{requestId}
```

Create/read request input. Owner-scoped.

### 7.2 Generation Execution API

```http
POST /api/generate/requests/{requestId}/generate
```

Starts synchronous generation for MVP unless existing app architecture already uses async jobs. If synchronous, return completed review DTO or generation status. If async, add status endpoint.

### 7.3 Review API

```http
GET /api/generate/requests/{requestId}/review
PUT /api/generate/requests/{requestId}/review
```

Returns and persists editable generated content grouped for frontend.

### 7.4 Finalize API

```http
POST /api/generate/requests/{requestId}/finalize
```

Body:

```json
{
  "selectedAdaptationLevel": "BALANCED"
}
```

Creates saved resume rows, renders HTML, generates PDF, returns export DTO.

### 7.5 Export API

```http
GET /api/generate/requests/{requestId}/export
GET /api/resumes/{savedResumeId}/pdf
GET /api/resumes/{savedResumeId}/html
GET /candidate/{publicCode}
```

Public route serves PDF directly. Authenticated routes stream user-owned files.

---

## 8. Service Design

### 8.1 `GenerationRequestService`

- Validate vacancy/settings DTO.
- Resolve active AI model, prompt config, budget config.
- Persist request.
- Owner-scope all operations.

### 8.2 `ResumePromptBuilder`

- Builder pattern.
- Load active prompt config and fragments.
- Load user profile payload including:
  - contact details;
  - work experience;
  - courses;
  - projects;
  - bilingual education;
  - normalized work formats;
  - additional info.
- Render final system prompt and request prompt.
- Store `ai_prompt_render_log`.

### 8.3 `AiClientFactory`

- Factory Method pattern.
- Return `MockAiClient` for tests/dev mode.
- Return `OpenRouterClient` for real configured model.

### 8.4 `OpenRouterClient`

- Read active model config.
- Use configured provider URL/model/API key.
- Never log API key.
- Return raw JSON/text content.
- Convert provider errors into service exceptions.

### 8.5 `AiResponseParser`

- Parse JSON strictly.
- Support single-language and bilingual responses.
- Support one selected adaptation level and All levels.
- Normalize keys from model where safe.
- Validate required fields.
- Reject unknown/incomplete structure with clear error.

### 8.6 `GenerationResponsePersistenceService`

- Use transaction.
- Delete/replace prior draft responses for same request only when safe.
- Insert top-level response rows.
- Insert child section tables.
- Insert `generation_response_personal`.
- Mark request completed/failed.

### 8.7 `ResumeReviewService`

- Return review DTO grouped for frontend.
- Save user edits.
- Prevent editing another user's request.
- Keep statuses consistent.

### 8.8 `ResumeFinalizeService`

- Validate selected level exists.
- For selected language rows, render HTML and generate PDF.
- Create public code per saved resume.
- Insert `saved_resume` rows.
- Use transaction where DB changes must be atomic.
- Coordinate file rollback/cleanup best-effort if DB transaction fails.

### 8.9 `GeneratedFileStorageService`

- Build safe server path:

```text
generated_results/{username}/{public_code}/
```

- Sanitize username/path segments.
- Write UTF-8 HTML.
- Store relative path in DB where possible.
- Stream files through controller endpoints.

### 8.10 `ResumeTemplateRenderer`

- Backend-owned final rendering.
- Fill one-page/two-page HTML templates using markers.
- Use bilingual Education fields based on response language.
- Use `generation_response_personal` for Personal Information.
- Use fallback only when response personal field is missing and profile data is safe.

### 8.11 `PdfGenerationService`

- Separate service.
- Converts saved HTML to PDF.
- Validates output file exists.
- Validates page count if library supports it.
- Does not contain prompt or AI logic.

---

## 9. Frontend Plan

### 9.1 Replace Mock Service

Create `generateResumeService.ts` with methods:

- `createRequest(payload)`
- `generate(requestId)`
- `getReview(requestId)`
- `saveReview(requestId, payload)`
- `finalize(requestId, selectedLevel)`
- `getExport(requestId)`
- `downloadPdf(savedResumeId)`
- `downloadHtml(savedResumeId)`

Keep mock service only for prototype/dev if clearly separated and not used in production route.

### 9.2 Update Types

Update `src/types/generate.ts` to match backend DTOs:

- language mode;
- adaptation selection;
- generated variant IDs;
- response IDs;
- saved resume IDs;
- Personal Information fields;
- html/pdf/public link export fields.

### 9.3 Review UI

Preserve prototype layout:

- Professional Positioning
- Work Experience
- Courses and Certifications
- Projects and Volunteering
- Skills
- Personal Information

Do not add Education to Review.

### 9.4 Export UI

Export page receives real backend data.

Buttons:

- Download PDF → backend PDF endpoint.
- Open PDF → public URL.
- Download HTML → backend HTML endpoint.
- Copy public link.
- Copy cover letter.

---

## 10. Security Plan

- All authenticated generation APIs require session user.
- All private request/review/export/file download queries include owner filter.
- Public PDF route checks saved resume active state and deleted state.
- API key is masked and never logged.
- Prompt render logs are admin/debug sensitive because they may contain profile/vacancy data.
- AI output is sanitized before storage/rendering.
- File paths are not accepted from user input.
- Download endpoints stream server-owned files only after DB authorization checks.

---

## 11. Testing Plan

### Backend Unit Tests

- Prompt builder loads correct fragments.
- Prompt builder includes bilingual education and work formats.
- AI parser handles EN-only, RU-only, Bilingual + All.
- AI parser rejects invalid JSON/missing required fields.
- Persistence creates expected row counts.
- Finalize creates correct saved resume count.
- File storage creates safe paths.
- Template renderer fills Personal Information and Education correctly.
- PDF service is mocked in finalize tests.

### Backend DAO Tests

- PreparedStatement usage through DAO methods.
- Owner-scoped select/update/delete.
- Unique constraints behavior.
- Work format round-trip through `user_work_format`.

### Frontend Tests / Manual Verification

- Wizard navigation.
- Validation.
- Bilingual review layout.
- All-level review layout.
- Personal Information tab editing.
- Export actions call correct endpoints.
- i18n EN/RU strings.

### End-to-End Manual Smoke Tests

1. English only + Balanced + no cover letter.
2. Russian only + Minimal + cover letter.
3. Bilingual + All + cover letter → finalize Balanced.
4. Invalid AI JSON via mock → readable error.
5. Public PDF route works after finalization.
6. HTML download works for owner.

---

## 12. Implementation Order

1. Migrations and seed data.
2. Backend models/DTOs/DAOs.
3. Prompt config and prompt builder.
4. AI client abstraction and mock client.
5. Parser and persistence.
6. Review DTO/read/update APIs.
7. Template rendering and file storage.
8. PDF service abstraction.
9. Finalize API.
10. Export/download/public routes.
11. Frontend service/types integration.
12. Frontend page wiring.
13. Tests and smoke verification.

---

## 13. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| DeepSeek copies Python shortcuts into Java | Keep backend index and plan explicit about JDBC/Flyway/3NF. |
| AI returns malformed JSON | Strict parser + clear error + mock tests. |
| Bilingual output is inconsistent | Prefer one request with bilingual prompt config; persist both language rows under same request. |
| HTML/PDF layout breaks | Backend renders from restored templates; PDF page count validation. |
| API key leak | Mask UI, no logs, dedicated model DAO/service. |
| File path traversal | Generate server paths only; never trust user path input. |
| Review DTO becomes too complex | Mirror prototype grouping and write DTO tests. |

---

## 14. Definition of Done

- [ ] Migrations apply cleanly on fresh PostgreSQL.
- [ ] Backend compiles with `mvn clean package`.
- [ ] Backend DAO/Service tests pass.
- [ ] No automated test calls OpenRouter.
- [ ] Frontend builds with `npm run build`.
- [ ] EN-only/RU-only/Bilingual flows work manually.
- [ ] Bilingual + All creates 6 draft response rows.
- [ ] Finalize selected level creates correct saved resume rows.
- [ ] HTML files are saved before PDF conversion.
- [ ] PDF and HTML downloads work from Export page.
- [ ] Public PDF link opens PDF directly.
- [ ] No hardcoded frontend strings introduced.
- [ ] No PII/API key logging.
