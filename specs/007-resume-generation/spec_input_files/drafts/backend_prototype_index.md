# Backend Prototype Index — Generate Resume Feature

**Project:** ResumAIner  
**Prototype dump:** `backend_prototype_dump.md`  
**Purpose:** help OpenCode / DeepSeek V4 Flash navigate the Python backend prototype v3.2 without rereading the whole dump every time.  
**Use this index first** when implementing backend Java equivalents.

---

## 1. Reading Rules for OpenCode

1. Start with this file before opening `backend_prototype_dump.md`.
2. Use the Python prototype as executable reference for behavior, not as final Java architecture.
3. Map Python `services → dao → sqlite` to Java `controller → service → dao → PostgreSQL`.
4. Preserve the data model and flow decisions exactly unless BA artifacts explicitly supersede them.
5. Do not copy SQLite shortcuts into Java when PostgreSQL/Flyway/JDBC patterns are required.
6. Do not simplify 3NF relationships for convenience. Work formats must stay normalized.
7. Keep real OpenRouter calls behind an interface/factory and use mock provider in automated tests.

---

## 2. Backend Prototype Flow

```text
add_user.py
  → creates schema/seed data/demo user profile

add_request.py
  → creates resume_generation_request from frontend-like generation_request.json

generate.py
  → builds prompt from DB config
  → calls OpenRouter or sample JSON
  → parses AI response
  → persists resume_generation_response + child tables

finalize_resume.py
  → selects adaptation level
  → renders filled HTML template
  → creates placeholder PDF marker in prototype
  → inserts saved_resume rows
  → creates public links

fill_htmls.py
  → low-level render of one response row to HTML
```

Production Java must implement the same pipeline behind HTTP endpoints and service classes.

---

## 3. Highest-Value Files to Read First

| Priority | File | Open When | Why It Matters |
|---|---|---|---|
| 1 | `README_quick_start.md` | Understanding prototype behavior and test commands | Explains full v3/v3.2 purpose, API key/model seed, sample test, real API test. |
| 2 | `docs/FLOW_OVERVIEW.md` | Designing Java sequence and services | Compact end-to-end flow from request to saved resume. |
| 3 | `docs/JAVA_MAPPING.md` | Mapping Python prototype to Java classes | Explicit handoff idea for Java/OpenCode implementation. |
| 4 | `schema.sql` | Creating Flyway migrations and Java models | Authoritative prototype DB structure including prompt config, bilingual Education, personal info, saved files. |
| 5 | `seed_prompt_config.sql` | Implementing prompt config seeds | Contains modular prompt fragments and JSON contract expectations. |
| 6 | `user_info.json` | Understanding demo profile payload | Shows realistic user profile input, bilingual Education, work formats. |
| 7 | `generation_request.json` | Understanding frontend request shape | Shows language/adaptation/cover letter request payload. |
| 8 | `add_user.py` | Implementing profile seed/test setup and profile insert logic | Shows how profile data, education, work formats, and lookups are inserted. |
| 9 | `add_request.py` | Implementing request creation endpoint/service | Shows how request JSON maps to `resume_generation_request`. |
| 10 | `generate.py` | Implementing generation entrypoint | Orchestrates prompt builder, AI client, parser, persistence. |
| 11 | `finalize_resume.py` | Implementing finalize endpoint | CLI wrapper for selected-level finalization. |
| 12 | `app/services/generation_service.py` | Implementing `ResumeGenerationService` | Central generation orchestration. |
| 13 | `app/services/prompt_builder_service.py` | Implementing `ResumePromptBuilder` | Assembles DB-backed system/request prompt and profile payload. |
| 14 | `app/clients/openrouter_client.py` | Implementing `OpenRouterClient` | Real external API behavior and error handling reference. |
| 15 | `app/services/response_parser_service.py` | Implementing parser for AI JSON | Normalizes single/bilingual and one/all adaptation responses. |
| 16 | `app/services/response_persistence_service.py` | Implementing persistence of generated response | Saves top-level response and child tables including Personal Information. |
| 17 | `app/services/finalize_resume_service.py` | Implementing `ResumeFinalizeService` | Selects level, creates saved_resume rows, file paths, public codes. |
| 18 | `app/services/html_render_service.py` | Implementing `ResumeTemplateRenderer` | Fills restored one/two-page HTML templates and saves filled HTML. |
| 19 | `app/dao/profile_dao.py` | Building profile payload | Shows how profile records and normalized work formats are read for prompt payload/rendering. |
| 20 | `app/dao/generation_response_dao.py` | Loading response bundle | Shows how response + child tables are rehydrated for rendering/review. |
| 21 | `app/dao/generation_request_dao.py` | Request DAO | Maps request table. |
| 22 | `app/dao/prompt_config_dao.py` | Prompt config DAO | Reads active prompt config and fragments. |
| 23 | `app/dao/saved_resume_dao.py` | Saved resume DAO | Writes final saved resume records. |
| 24 | `app/enums.py` | Controlled values | Language mode, adaptation selection, language codes, status normalization. |
| 25 | `samples/sample_ai_response_bilingual_all.json` | Testing without OpenRouter | Expected AI response shape for Bilingual + All. |
| 26 | `one_page_template_en.html`, `one_page_template_ru.html` | Implementing final renderer | One-page production-like HTML templates with markers. |
| 27 | `two_page_template_en.html`, `two_page_template_ru.html` | Implementing final renderer | Two-page production-like HTML templates with markers/page notes. |

---

## 4. Files Usually Not Needed

| File/Folder | Why Usually Skip |
|---|---|
| `__pycache__/` | Python bytecode. Ignore. |
| `*.db` | Local SQLite run outputs. Use only for manual debugging if needed. |
| `generated_results/` | Example generated artifacts. Useful only to inspect output structure. |
| `output/` | Legacy/placeholder output folder. New final structure is `generated_results/{username}/{public_code}`. |
| `code2prompt.exe` | Dump utility, not implementation reference. |

---

## 5. Core Data Model Decisions to Preserve

### 5.1 Language and Adaptation

- `resume_generation_request.language_mode` stores request mode:
  - `ENGLISH_ONLY`
  - `RUSSIAN_ONLY`
  - `BILINGUAL`
- `resume_generation_request.adaptation_selection` / adaptation request choice may be:
  - `MINIMAL`
  - `BALANCED`
  - `MAXIMUM`
  - `ALL`
- `resume_generation_response.language_id` stores actual response language.
- `resume_generation_response.adaptation_level_id` stores actual generated level.
- Unique response identity must be:

```text
(generation_request_id, language_id, adaptation_level_id)
```

Do not use the old unique pair `(generation_request_id, language_id)`.

### 5.2 Education

Education is profile-owned factual data and must be bilingual in profile:

- `institution_name_ru`
- `institution_name_en`
- `degree_ru`
- `degree_en`
- `field_of_study_ru`
- `field_of_study_en`

AI must not invent/translate Education during Review.

### 5.3 Personal Information

Personal Information is generated/reviewed per response:

```text
generation_response_personal
```

It must include at least:

- location;
- spoken languages;
- willingness to relocate;
- willingness for business trips;
- work formats;
- citizenship;
- date of birth;
- GPA / grade when available.

### 5.4 Work Formats

Work formats must use 3NF:

```text
work_format
user_work_format
```

Do not store work formats as comma-separated text in `additional_profile_info`.

Prompt payload should expose both codes and localized display names so the model does not invent values.

### 5.5 Prompt Config

Prompt config is DB-backed and modular:

- `ai_prompt_config`
- `ai_system_prompt`
- `ai_request_prompt_language`
- `ai_request_prompt_adaptation`
- `ai_request_prompt_cover_letter`
- `ai_prompt_render_log`

The Java implementation must support active prompt config lookup and rendered prompt logging for debugging/reproducibility.

### 5.6 Saved Artifacts

Finalized resumes must save filled HTML before PDF conversion.

Recommended structure:

```text
generated_results/{username}/{public_code}/
  yyyy-MM-dd-HH-mm_en_balanced.html
  yyyy-MM-dd-HH-mm_en_balanced.pdf
```

For bilingual generation, there are two saved resumes and two public codes/links.

`public_url_link` is for recruiter/public PDF access. HTML download is authenticated user-only unless a later requirement says otherwise.

---

## 6. Java Service Mapping

| Python Prototype | Java Target |
|---|---|
| `generate.py` | `GenerateResumeController.generate(requestId)` + `ResumeGenerationService.generate(requestId)` |
| `add_request.py` | `GenerateResumeController.createRequest(dto)` + `GenerationRequestService` |
| `finalize_resume.py` | `GenerateResumeController.finalize(requestId, selectedLevel)` + `ResumeFinalizeService` |
| `prompt_builder_service.py` | `ResumePromptBuilder` using Builder pattern |
| `openrouter_client.py` | `AiClient` interface + `OpenRouterClient` implementation |
| sample response | `MockAiClient` for tests/dev |
| `response_parser_service.py` | `AiResponseParser` |
| `response_persistence_service.py` | `GenerationResponsePersistenceService` |
| `html_render_service.py` | `ResumeTemplateRenderer` |
| PDF placeholder | `PdfGenerationService` + `HtmlToPdfConverter` |
| DAOs | Plain JDBC DAO classes with PreparedStatement |

---

## 7. Recommended Java Packages

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
│   ├── GenerationRequestDao.java
│   ├── GenerationResponseDao.java
│   ├── PromptConfigDao.java
│   ├── AiModelDao.java
│   ├── SavedResumeDao.java
│   └── ResumeTemplateDao.java
├── dto/generate/
├── model/
└── util/
```

---

## 8. Critical Implementation Watchpoints

1. Use manual JDBC transactions around generation persistence and finalization.
2. Never log API keys, full prompts with secrets, or PII in normal logs.
3. Prompt render log may contain PII and must be admin/debug scoped.
4. Use `PreparedStatement` everywhere.
5. Use UTF-8 for all text and file writes.
6. Sanitize AI-generated limited HTML before storage/rendering.
7. Do not call real OpenRouter in automated tests.
8. Validate generated response shape before persisting.
9. If one language/adaptation fails validation, handle the whole generation consistently.
10. Preserve exact public link behavior: one saved resume = one public code/link.

---

## 9. Quick Backend Verification Checklist

- [ ] Bilingual + All creates 6 response rows.
- [ ] Bilingual + Balanced creates 2 response rows.
- [ ] English only + All creates 3 response rows.
- [ ] `generation_response_personal` has one row per response.
- [ ] `work_formats` reaches `generation_response_personal` from normalized profile data.
- [ ] Finalizing `BALANCED` after Bilingual + All creates 2 `saved_resume` rows.
- [ ] Filled HTML files exist on disk before PDF conversion.
- [ ] PDF conversion is isolated in a separate service.
- [ ] Export DTO exposes HTML and PDF download references.
- [ ] Public URL serves PDF directly.
- [ ] User-owned download endpoints are owner-scoped.
