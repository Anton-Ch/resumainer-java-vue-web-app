# Implementation Plan: Resume Generation

**Branch**: `feat/007-resume-generation` | **Date**: 2026-06-12 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/007-resume-generation/spec.md`

---

## Summary

Implement the full Generate Resume feature — the core business flow of ResumAIner. Users enter vacancy data, select generation settings (language mode, AI model, adaptation level, cover letter), trigger AI generation via OpenRouter, review and edit structured output, finalize one adaptation level, and obtain saved HTML artifacts.

The feature spans: backend generation pipeline (request → AI → response → finalize → save), 10+ new DB tables with Flyway migrations, frontend wizard (vacancy → settings → review → export), OpenRouter AI integration, mock AI client for tests, modular prompt configuration, and HTML template rendering. PDF conversion is deferred to `feat/008-pdf-conversion`; feat/007 defines only the service boundary.

---

## Technical Context

**Language/Version**: Java 21 LTS
**Backend Framework**: Spring MVC 6.x (no Spring Boot), Jakarta EE 10
**Frontend**: Vue 3 (Composition API) + Vite + PrimeVue 4 + vue-router + vue-i18n
**Database**: PostgreSQL 17, plain JDBC (no ORM/JPA/Hibernate), custom Connection Pool
**Migrations**: Flyway 10.x with `@Bean(initMethod="migrate")`
**AI Integration**: OpenRouter API behind `AiClient` interface + `AiClientFactory` (Factory Method pattern). `MockAiClient` for tests
**PDF Generation**: Deferred to `feat/008-pdf-conversion`. In feat/007, only the `PdfGenerationService` interface/stub boundary is defined. No PDF library is chosen or integrated in this feature.
**Testing**: JUnit 5 + Mockito + JaCoCo (50%+ coverage on Service/DAO)
**Build**: Maven (`mvn clean package` must pass) + npm (`npm run build` must pass)
**Deployment**: Docker Compose (Tomcat + Nginx + PostgreSQL)
**Performance Goals**: Generation requests complete within 30-60s (AI provider dependent). Review/export endpoints respond under 500ms p95
**Constraints**: Backend owns all HTML rendering. Vue renders only structured forms. PDF conversion deferred to `feat/008-pdf-conversion`. Manual JDBC transactions for generation persistence and finalization. PreparedStatement-only SQL. UTF-8 for all text and file I/O. API keys never logged or exposed.

---

## Constitution Check

*GATE: Must pass before proceeding to task breakdown. Re-check if design changes.*

| Principle | Status | Notes |
|-----------|--------|-------|
| **I. Code Quality & Maintainability** | ✅ Pass | Layered architecture: `controller/`, `service/`, `dao/`, `model/`, `dto/`, `util/`, `config/`. Builder pattern for prompt assembly, Factory Method for AI client creation, Strategy for adaptation level selection. No Spring Boot/JPA/Hibernate. New classes follow existing project conventions. |
| **II. Testing Excellence** | ✅ Pass | JUnit 5 + Mockito for all backend tests. TDD for prompt builder, parser, finalize, and persistence services. Mock AI provider for all automated tests — no real OpenRouter calls. JaCoCo 50%+ coverage target on Service/DAO layers. Frontend smoke tests for wizard flow. |
| **III. User Experience Consistency** | ✅ Pass | All new UI strings externalized in EN/RU i18n files (Vue). Dual validation: frontend via PrimeVue Zod resolver, backend via Jakarta Validation. Error messages user-readable with no stack traces. Wizard preserves prototype UX (vacancy → settings → review → export). |
| **IV. Performance & Reliability** | ✅ Pass | PreparedStatement-only SQL. Manual JDBC transactions (commit/rollback) around generation persistence and finalization. `catch(Exception)` for all transaction blocks (per D23). SQL-level pagination for resume lists. UTF-8 encoding for all DB connections and file writes. PDF service boundary defined; real PDF conversion deferred to feat/008. |
| **V. Security by Design** | ✅ Pass | Owner-scoped queries for all private endpoints (request/review/export/download). API keys masked in UI, never logged. AI HTML sanitized with allowlist before storage. Public PDF route deferred to `feat/008-pdf-conversion`. File paths never accepted from user input. Backend validation authoritative. |

### Complexity Justification

No violations identified. All design choices align with established project patterns and constitution principles.

---

## Project Structure

### Documentation (this feature)

```text
specs/007-resume-generation/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Technology research (Phase 0)
├── data-model.md        # Data model design (Phase 1)
├── quickstart.md        # Implementation quickstart (Phase 1)
├── contracts/           # API contracts (Phase 1)
├── memory-synthesis.md  # Project memory synthesis
├── doc-synthesis.md     # Doc cache synthesis
├── checklists/          # Quality checklists
└── spec_input_files/    # Reference prototypes and drafts
```

### Source Code

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
│   ├── PdfGenerationService.java      (interface/stub, real impl deferred to feat/008)
│   └── GeneratedFileStorageService.java
├── service/ai/
│   ├── AiClient.java                  (interface)
│   ├── AiClientFactory.java
│   ├── OpenRouterClient.java
│   └── MockAiClient.java
├── dao/
│   ├── AiModelDao.java
│   ├── PromptConfigDao.java
│   ├── GenerationRequestDao.java
│   ├── GenerationResponseDao.java
│   ├── SavedResumeDao.java
│   └── ResumeTemplateDao.java
├── dto/generate/
│   ├── GenerationRequestCreateDto.java
│   ├── GenerationRequestResponseDto.java
│   ├── ReviewDataDto.java
│   ├── ReviewUpdateDto.java
│   ├── FinalizeRequestDto.java
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
    └── WebConfig.java                 (existing, add bean registrations)

backend/src/main/resources/db/migration/
├── V{NEXT}__education_bilingual_fields.sql
├── V{NEXT+1}__generation_pipeline.sql
├── V{NEXT+2}__prompt_config.sql
└── V{NEXT+3}__saved_resume_files.sql

> **Note:** Before creating migration files, inspect the highest existing version in `backend/src/main/resources/db/migration/`. Current highest is V16. Use `V17`–`V20` or the next available numbers. Do not create out-of-order Flyway versions.

backend/src/test/java/com/resumainer/
├── service/
├── dao/
├── controller/
└── util/

frontend/src/
├── views/generate/
│   ├── GenerateVacancyPage.vue
│   ├── GenerateSettingsPage.vue
│   ├── GenerateReviewPage.vue
│   ├── GenerateErrorPage.vue          (temporary, only after AI failure)
│   └── GenerateExportPage.vue
├── components/generate/
│   ├── VacancyStepForm.vue
│   ├── SettingsStepForm.vue
│   ├── ReviewStepForm.vue
│   ├── GeneratedRecordGroup.vue
│   ├── GeneratedVariantTextarea.vue
│   ├── AdaptationLevelRadioGroup.vue
│   ├── ExportResult.vue
│   ├── WhimsicalLoader.vue
│   ├── GenerateStepper.vue
│   └── BilingualDivider.vue
├── services/
│   └── generateResumeService.ts
├── composables/
│   └── useGenerateResumeFlow.ts
├── types/
│   └── generate.ts
├── router/
│   └── index.ts                      (add /generate/* routes)
└── i18n/
    ├── en.json                       (add generation strings)
    └── ru.json                       (add generation strings)
```

> **Generation error screen:** `GenerateErrorPage.vue` is a temporary screen shown only after AI generation fails. It is not a permanent wizard step. It provides two actions: "Try again" (retries with the same vacancy data and settings) and "Change settings" (returns to Generate Settings preserving vacancy data). No raw provider errors, stack traces, API keys, or sensitive technical details are exposed. If the user changes the AI model or settings and re-runs generation, the error screen disappears.

**Structure Decision**: Follows existing project monorepo layout (`backend/` + `frontend/`). New files placed in domain-specific sub-packages matching established patterns.

---

## Phase 0: Outline & Research

### Research Tasks

All technology choices are already known from project stack. No NEEDS CLARIFICATION items remain. The following confirmations will be documented in `research.md`:

1. **OpenRouter API**: Confirm current API endpoint format, auth header pattern, JSON response structure, error codes
2. **PrimeVue 4 Form patterns**: Confirm PrimeVue 4 Zod resolver pattern for new wizard forms
3. **Unique public code algorithm**: Determine character set and collision strategy for 5-character public codes
4. **PDF converter research**: Deferred to `feat/008-pdf-conversion`. Not evaluated in feat/007.

### Research Output

See `research.md` for detailed findings.

---

## Phase 1: Design & Contracts

### Data Model

See `data-model.md` for complete entity design including:
- All generation pipeline tables (request, response, child sections)
- Prompt config tables (config, system prompt, language prompt, adaptation prompt, cover letter prompt, render log)
- Saved resume with file paths and public URL
- Bilingual education fields
- All relationships, PK/FK strategies, indexes, constraints

### API Contracts

See `contracts/` directory for endpoint specifications:
- `GET /api/generate/ai-models` — List AI models available for current user (filtered by privileged flag)
- `POST /api/generate/requests` — Create generation request (validates `ai_model_id` availability for current user)
- `GET /api/generate/requests/{id}` — Get request details
- `POST /api/generate/requests/{id}/generate` — Execute generation
- `GET /api/generate/requests/{id}/review` — Get review data
- `PUT /api/generate/requests/{id}/review` — Save review edits
- `POST /api/generate/requests/{id}/finalize` — Finalize selected level
- `GET /api/generate/requests/{id}/export` — Get export data
- `GET /api/resumes/{id}/html` — Download HTML (authenticated, owner-scoped, fully functional in feat/007)
- `GET /api/resumes/{id}/pdf` — Placeholder stub in feat/007. Returns safe "not available yet" response. Real PDF download deferred to `feat/008-pdf-conversion`.
- `GET /candidate/{publicCode}` — Placeholder in feat/007 (real public PDF route deferred to `feat/008-pdf-conversion`). Export DTO may provide a placeholder `publicUrlLink` that does not expose resume data.

> **Export DTO contract:** `GET /api/generate/requests/{id}/export` returns: `htmlDownloadUrl` (real), `pdfDownloadUrl` (placeholder), `pdfOpenUrl` (placeholder), `publicUrlLink` (placeholder), `pdfAvailable` (false in feat/007), `pdfMessage` (user-facing text: "PDF generation coming in a future update"), `coverLetter` (if generated).

> **AI model endpoint rules:**
> - `GET /api/generate/ai-models` returns safe metadata only (id, provider, displayName, modelCode). API keys must never appear in the response.
> - Regular users see only active, non-hidden models. Users with `privileged = true` additionally see hidden active models.
> - `POST /api/generate/requests` must validate the selected `ai_model_id` against the current user's privilege level. Non-privileged users must not be able to select hidden/internal models even by manipulating the request payload. Return a user-readable validation error if the model is unavailable or forbidden.

### Quickstart

See `quickstart.md` for implementation notes, key patterns, and watchpoints.

---

## Implementation Order

| Phase | Step | Tasks | Dependencies |
|-------|------|-------|-------------|
| **1** | Migrations + seed data | Flyway scripts (V{NEXT}–V{NEXT+3}), seed prompt config, seed adaptation levels, seed templates | Existing schema |
| **2** | Models + DTOs + DAOs | All model classes, DTOs, DAO interfaces with PreparedStatement. `AiModelDao` with privileged filtering. | Step 1 |
| **3** | Prompt config + builder | PromptConfigDao, ResumePromptBuilder (Builder pattern), render log | Step 2 |
| **4** | AI client abstraction | AiClient interface, AiClientFactory (Factory Method), MockAiClient, OpenRouterClient | Step 2 |
| **5** | Parser + persistence | AiResponseParser, GenerationResponsePersistenceService, transaction management | Steps 2-4 |
| **6** | Request + generation API + AI model endpoint | `GenerationRequestService` (with `ai_model_id` privilege validation), `GenerateResumeController` (create/generate + `GET /api/generate/ai-models` with privileged filtering) | Steps 3-5 |
| **7** | Review API | ResumeReviewService, GET/PUT review endpoints with grouped DTOs | Steps 2, 5 |
| **8** | Template rendering + file storage | ResumeTemplateRenderer, GeneratedFileStorageService, HTML template files | Steps 2, 7 |
| **9** | PDF boundary | Define `PdfGenerationService` interface only. Create `NoOpPdfGenerationService` stub that clearly reports PDF not available in this feature. No PDF library chosen or integrated in feat/007. | Step 8 |
| **10** | Finalize API | ResumeFinalizeService, finalize endpoint with transaction + file coordination | Steps 7-9 |
| **11** | Export + download (real HTML + placeholder PDF/public) | Export DTO with real `htmlDownloadUrl` plus placeholder `pdfDownloadUrl`, `pdfOpenUrl`, `publicUrlLink`, `pdfAvailable=false`, `pdfMessage`. Authenticated HTML download endpoint (owner-scoped). PDF/download stubs returning safe placeholder responses. | Steps 9-10 |
| **12** | Frontend service layer | generateResumeService.ts, types/generate.ts, CSRF-aware API calls | Step 11 |
| **13** | Frontend pages | All wizard pages (vacancy, settings, review, error, export), components, i18n strings, routing | Step 12 |
| **14** | Tests + verification | Backend unit tests (DAO/Service), frontend smoke verification, Docker integration test | All steps |

---

## Security Boundaries

| Endpoint | Auth | Owner Scope | Notes |
|----------|------|-------------|-------|
| `GET /api/generate/ai-models` | Session required | No owner scope (same list for all users) | Filtered by `is_privileged` flag — hidden models excluded for non-privileged users |
| `POST /api/generate/requests` | Session required | User ID from session | Validates `ai_model_id` availability for current user. Non-privileged users cannot select hidden models |
| `GET /api/generate/requests/{id}` | Session required | WHERE user_id = ? | Read own request |
| `POST /.../generate` | Session required | Owner check | Execute own request |
| `GET /.../review` | Session required | Owner check | Read own data |
| `PUT /.../review` | Session required | Owner check | Edit own data |
| `POST /.../finalize` | Session required | Owner check | Finalize own resume |
| `GET /.../export` | Session required | Owner check | Export own results |
| `GET /api/resumes/{id}/html` | Session required | Owner check | Download own HTML file — fully functional in feat/007 |
| `GET /api/resumes/{id}/pdf` | Session required | Owner check | Placeholder stub — returns "not available yet" in feat/007. No fake PDF file created. Real PDF download in feat/008 |
| `GET /candidate/{code}` | None | Public (placeholder) | Placeholder only in feat/007 — returns safe placeholder response, no resume data exposed. Real public PDF route deferred to feat/008 |

---

## Security Implementation Notes

The following items from the security review (`security-review-plan.md`) must be addressed during implementation:

### SEC-001: Prompt Render Log Access Control

The `ai_prompt_render_log` table stores rendered prompts containing user profile data, vacancy text, and potentially PII. Access MUST be restricted:

- **No frontend endpoint** shall expose render logs to any user, including admins, in MVP
- Backend writes to render log are **append-only** — no read endpoint for regular users
- Admin access is via **direct database query only** (no API endpoint in MVP)
- Document this restriction in `PromptConfigDao` and `ResumePromptBuilder` design

### SEC-002: Generated File Path Sanitization

The `GeneratedFileStorageService` constructs paths under `generated_results/{username}/{public_code}/`. Path traversal prevention MUST be implemented:

- Strip `../`, `./`, null bytes, and path separator characters from the username segment
- Use `Path.normalize()` and verify resolved path starts with the expected base directory
- Never use raw username strings in file path construction
- The `public_code` segment is server-generated alphanumeric — naturally safe, but validate anyway
- Unit tests MUST include path traversal attempts (e.g., username=`../../etc/passwd`)

### SEC-003: Public Code Collision Handling

The `PublicCodeGenerator` produces 5-character public codes (matching backend prototype convention, e.g. `02OP7`, `53MZ4`). Collision handling MUST be implemented:

- Default length: 5 characters. Character set should exclude ambiguous characters (0, O, I, L, 1) if possible.
- Generate a candidate code, attempt insert, catch unique constraint violation
- Retry with a new code on collision (max 5 attempts)
- Fall back to a longer code (6-8 characters) if collisions persist
- Unit test: verify retry logic produces a valid unique code
- Document the character set

### SEC-004: Rate Limiting on Request Creation

Rate limiting on `POST /api/generate/requests` is acknowledged as an acceptable risk for MVP:

- No active rate limiting implementation required for MVP
- Document in `GenerationRequestService` that this is a known gap for future hardening
- Consider adding a limit (e.g., max 50 active non-completed requests per user) if time permits

### SEC-005: One Active Generation Per User

Only one generation request per user may be in `processing` status at a time:

- Before starting generation, the system MUST check that the user has no other request in `processing` status
- If a processing request exists, return a user-readable message: "Generation already in progress. Please wait for it to complete."
- After generation completes (success or failure), the user may start a new one
- This rule limits AI costs and prevents concurrent provider calls from the same user
- Enforce at the service layer (`ResumeGenerationService`), not just the frontend
- The spec edge case (concurrent requests) and clarification Q3 both document this rule

---

## Testing Plan

### Backend Unit Tests (JUnit 5 + Mockito)

| Area | Test Focus |
|------|-----------|
| `ResumePromptBuilder` | Correct fragment loading, bilingual education inclusion, work format inclusion, render log creation |
| `AiResponseParser` | EN-only/RU-only/Bilingual parsing, All levels → 3 response sets, invalid JSON rejection, missing field rejection |
| `GenerationResponsePersistenceService` | Correct row counts, transaction rollback on error, prior draft handling |
| `ResumeFinalizeService` | Correct saved resume count, HTML file creation, rollback on HTML write failure, no PDF conversion in feat/007 |
| `GeneratedFileStorageService` | Path construction, username sanitization (strip `../`, `Path.normalize()`), UTF-8 writes, path traversal attempts rejected |
| `GenerationRequestDao` | PreparedStatement verification, owner-scoped selects, unique constraint behavior |
| `GenerationResponseDao` | Response + child table round-trip, unique `(request_id, lang_id, adapt_level_id)` |
| `PromptConfigDao` | Active config lookup, fragment retrieval |
| `AiModelDao` | Privileged filtering (non-privileged user sees no hidden models), inactive model exclusion, API key not present in DTO |
| `SavedResumeDao` | Insert + owner-scoped select, public code uniqueness |
| `PublicCodeGenerator` | Collision retry logic (max 5 attempts), fallback to longer code, ambiguous char exclusion |

### Backend Integration Tests

- MockMvc controller tests for all generation endpoints (standalone setup, no DB needed for contract tests)
- Smoke test: full pipeline with MockAiClient

### Frontend Verification

- Wizard navigation: vacancy → settings → review → export
- Validation: required fields, language mode, adaptation selection
- Bilingual layout: EN/RU side-by-side on desktop
- All-levels layout: three variants per field
- Personal Information tab: editing and display
- Export actions: Copy public link (placeholder), Download PDF (placeholder), Open PDF (placeholder), Download HTML (real), Copy cover letter
- i18n: all new strings in EN/RU
- Generation error screen: mock AI failure shows temporary error screen with "Try again" and "Change settings"
- Try again action: re-calls generation endpoint with same request/settings
- Change settings action: returns to settings page, vacancy data preserved
- AI model change after failure: new model is used on retry

### End-to-End Smoke Tests

1. English only + Balanced + no cover letter → generate → review → finalize → HTML downloaded
2. Russian only + Minimal + cover letter → generate → review → finalize → HTML downloaded
3. Bilingual + All + cover letter → review all 6 variants → finalize Balanced → 2 HTML saved resumes
4. Invalid AI response (via MockAiClient) → readable error screen → Try again / Change settings
5. HTML download → owner receives file, non-owner gets 403
6. Export page shows PDF actions as disabled/unavailable (PDF deferred to feat/008)
7. AI model filtering → non-privileged user cannot see hidden model; privileged user can
8. AI model validation → non-privileged user cannot create request with hidden model id

---

## Definition of Done

- [ ] All Flyway migrations apply cleanly on fresh PostgreSQL
- [ ] `mvn clean package` succeeds with tests passing
- [ ] JaCoCo coverage ≥ 50% on Service and DAO layers
- [ ] No automated test calls real OpenRouter
- [ ] Frontend `npm run build` succeeds
- [ ] EN-only/RU-only/Bilingual flows work end-to-end
- [ ] Bilingual + All creates 6 draft response rows
- [ ] Finalize selected level creates correct saved resume rows
- [ ] HTML saved to disk during finalization
- [ ] HTML download works from Export page and is owner-scoped
- [ ] Export page shows all prototype actions: Copy public link, Download PDF, Open PDF, Download HTML, Copy cover letter
- [ ] PDF/public link actions are placeholders using safe placeholder URLs/responses — no fake PDF artifacts created
- [ ] Public placeholder link does not expose resume data
- [ ] PDF service boundary (interface/stub) exists; real PDF conversion deferred to `feat/008-pdf-conversion`
- [ ] Frontend export contract is stable: feat/008 can replace placeholder URLs with real PDF URLs without redesigning Export UI
- [ ] No hardcoded UI strings — all in i18n EN/RU
- [ ] No secrets/API keys in logs or UI
- [ ] API keys masked in AI Model dropdown
- [ ] AI-generated HTML sanitized with allowlist
- [ ] Owner-scoped access enforced on all private endpoints
- [ ] Prompt render log has no frontend endpoint — admin DB access only (SEC-001)
- [ ] Generated file paths sanitized against path traversal — `Path.normalize()` + base dir check (SEC-002)
- [ ] Public code generation handles unique constraint collisions with retry logic (SEC-003)
- [ ] Rate limiting on request creation documented as known MVP gap (SEC-004)
- [ ] One active generation per user enforced — concurrent processing requests blocked (SEC-005)
- [ ] Docker Compose build + smoke test passes

---

## Execution Strategy

### TDD Requirements

- `AiResponseParser` — Complex JSON parsing with strict validation and multiple language/adaptation combinations
- `ResumePromptBuilder` — Prompt assembly logic with modular fragments
- `GenerationResponsePersistenceService` — Transaction boundary correctness and rollback behavior
- `ResumeFinalizeService` — Multi-step finalization with file + DB coordination

### Parallel Execution Opportunities

- **Backend migrations + models** (Steps 1-2): Can proceed in parallel with frontend route/i18n setup
- **Frontend pages** (Step 13): Can proceed in parallel with backend endpoint implementation once contracts are defined
- **PDF boundary stubs** (Step 9): Trivial — interface + NoOp stub only. Real PDF conversion is feat/008.

### Human Checkpoints

1. After migrations and seed data — verify schema and seed records
2. After backend API endpoints (Steps 6-11) — verify with mock AI client
3. After frontend pages (Step 13) — verify full wizard flow end-to-end
4. Before merge — final review against spec and constitution

### Review Gates

- API contracts (DTOs and endpoint signatures) — review before frontend integration
- Security-sensitive code (AI model selection, file access, public routes) — review before finalization
- Data model changes (Flyway migrations) — review before applying
