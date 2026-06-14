# debug_backlog.md — feat/007 Resume Generation Debug & Completion Backlog

> Working communication file.  
> Goal: finish `feat/007-resume-generation` until the full working flow is stable: vacancy → settings → AI generation → Review page → save edits → finalize → saved HTML → HTML download/export.  
> Real PDF conversion is still out of scope for feat/007 and belongs to feat/008.

---

## 0. Current priority

**Primary focus now:** finish and debug the Review page fully, especially empty `Work Experience`, `Courses & Certifications`, `Projects & Volunteering`, and `Skills` tabs.

Review must work like the frontend prototype:

- 6 tabs:
  - Professional Positioning
  - Work Experience
  - Courses & Certifications
  - Projects & Volunteering
  - Skills
  - Personal Information
- No Education tab.
- Editable generated fields.
- If settings were `BILINGUAL + ALL`, the UI must show:
  - EN and RU columns/cards side by side on desktop;
  - inside each language: variants for Minimal / Balanced / Maximum;
  - one selected final adaptation level applies to all languages during finalize.
- Save edits before finalize.
- Do not use mock data to hide real pipeline failures.

---

---

## 0.6 Review update — 2026-06-13 after screenshot of Projects tab

### Assessment

Screenshot reveals a new important Review UI/data-shaping bug. Phase 4.4 cannot be accepted yet.

The Projects tab currently renders one project block, then EN/RU cards, then Minimal/Balanced/Maximum fields. But the visible block has values only for one language/level and empty fields for the other levels/language. This strongly suggests that records are being grouped by the generated child row UUID instead of a stable source record identity.

### Root-cause hypothesis

The frontend Review UI groups projects by `sourceId`, but the adapter currently sets `sourceId` from backend `record.recordId`. Backend `recordId` for child rows appears to be the generated child row id, which is different for every response row: EN MIN, EN BAL, EN MAX, RU MIN, RU BAL, RU MAX.

Therefore the UI treats the same original project as 6 different project records. For each project block, only one language/level has data; other cells are empty because they belong to other generated child ids.

This is **not primarily a prompt-count problem**.

The prompt uses arrays for `workExperience`, `courses`, `projects`, and `skills`; that means the number of items should be dynamic. The stricter issue is that the frontend/backend contract lacks a stable child-record grouping key across language/adaptation variants.

### Current decision

Do **not** proceed to Phase 5 yet.

Add and complete:

- **Phase 4.5 — Stable child record grouping across languages and adaptation levels**

Preferred fix options:

1. **Best long-term fix:** persist and expose stable `sourceId` / `sourceKey` for generated child records.
   - For work/courses/projects: use original profile record id if available.
   - For skills: use stable group key, e.g. normalized skill group name + order.
   - For personal information: one stable group key.
2. **Acceptable MVP fix:** if DB schema change is too large now, compute frontend grouping key from `sectionKey + orderInResume` instead of generated child row UUID, while keeping backend `updateKey` with the real child record id for saving.

The fix must be covered by tests and Playwright evidence.

---

## 0.14 Review update — 2026-06-13 after Phase 4.8.1 report and manual retest

### Assessment

Phase 4.8.1 is **not accepted**. It was manually confirmed that the application still does not perform a fresh AI generation: Generate returns in about 2 seconds and Review shows old records. This is the exact bug Phase 4.8 was supposed to close.

The latest evidence is not sufficient. A backend unit-test report and a single requestId with a quick OpenRouter/provider error do not prove that the real browser flow is correct. If OpenRouter fails quickly, the UI must show an Error state for the new requestId, not an old Review.

There is also still a source/build/runtime trust problem. If the report says lifecycle guards exist, the uploaded code dump, Docker image, running container logs, and Playwright network trace must all prove the same thing. No more “ready for Phase 5” claims without two complete UI runs.

### Hard stop

Do **not** start Phase 5.

Phase 4.8 remains a hard blocker. Must complete:

- **Phase 4.8.2 — Runtime truth and two-pass Playwright proof**

### New issues

| ID | Date/time | Phase | Severity | Symptom | Root cause hypothesis | Evidence | Current decision | Next action |
|---|---:|---|---|---|---|---|---|---|
| BUG-007-GEN-005 | 2026-06-13 | 4.8.2 | Critical | Generate still returns in ~2 seconds and Review shows old records | Fresh generation lifecycle is still broken; likely stale requestId/state, stale build, source changes not applied to running backend/frontend, or provider error routes to old Review | Manual retest after Phase 4.8.1 report | Hard stop before Phase 5 | Two full Playwright UI runs with unique vacancy markers and requestId chain; if OpenRouter fails, UI must show error, not old Review. |
| BUG-007-SRC-003 | 2026-06-13 | 4.8.2 | Critical | Phase 4.8.1 report claims lifecycle guard in source/build, but uploaded evidence is not sufficient and appears inconsistent | Report/build/source/runtime mismatch | User still reproduces stale Review; source/build truth not proven through running app | Hard stop before Phase 5 | Verify exact working-tree file, regenerate code dump, rebuild Docker no-cache, prove running class contains guard, then prove through Playwright. |

### New decisions

| ID | Date | Decision | Reason | Status |
|---|---|---|---|---|
| DEC-007-TWO-PASS-PLAYWRIGHT-001 | 2026-06-13 | Phase 4.8 cannot close until Playwright performs two full UI generation runs with different requestIds and unique vacancy markers | Single reused requestId or DB-only evidence has repeatedly hidden the stale Review bug | Active |
| DEC-007-ERROR-NO-OLD-REVIEW-001 | 2026-06-13 | If OpenRouter fails quickly, the UI must show Error, not old Review | Provider errors must not be masked by stale generated content | Active |

# Phase 4.8.2 — Runtime truth and two-pass Playwright proof

## Goal

Prove in the running Docker app, through the UI, that Generate does not reuse stale Review data and that provider errors do not route to old Review.

## Source/build truth tasks

- [ ] T4.8.2.S1 Inspect exact source file `src/main/java/com/resumainer/service/ResumeGenerationService.java`.
- [ ] T4.8.2.S2 Confirm source contains pending/completed/processing lifecycle guard before prompt/AI call.
- [ ] T4.8.2.S3 Regenerate code dump and verify the dump contains the same guard.
- [ ] T4.8.2.S4 Rebuild backend Docker with `--no-cache`.
- [ ] T4.8.2.S5 Rebuild frontend Docker with `--no-cache` if any frontend flow state changes are made.
- [ ] T4.8.2.S6 Confirm running container logs include the rebuilt timestamp/version or a temporary safe build marker.

## RED tests

- [ ] T4.8.2.R1 Backend RED: completed request cannot reach prompt builder or AI client.
- [ ] T4.8.2.R2 Backend RED: OpenRouter/AiClient exception marks request failed and does not persist generated responses.
- [ ] T4.8.2.R3 Controller RED: provider error returns error DTO, not `{status: completed}`.
- [ ] T4.8.2.R4 Frontend/Playwright RED: provider error after Generate does not route to Review.
- [ ] T4.8.2.R5 Frontend/Playwright RED: second full UI generation run creates a different requestId.

## Two mandatory Playwright runs

Run Playwright from a clean browser context. Do not reuse old requestId. Do not start from Review.

### Run A

- [ ] T4.8.2.A1 Start at `/generate/vacancy`.
- [ ] T4.8.2.A2 Fill vacancy title/description with unique marker `PLAYWRIGHT_RUN_A_<timestamp>`.
- [ ] T4.8.2.A3 Click Continue.
- [ ] T4.8.2.A4 Capture requestId A from `POST /api/generate/requests`.
- [ ] T4.8.2.A5 Select BILINGUAL + ALL.
- [ ] T4.8.2.A6 Click Generate.
- [ ] T4.8.2.A7 Capture `PUT /api/generate/requests/A/settings`.
- [ ] T4.8.2.A8 Capture `POST /api/generate/requests/A/generate`.
- [ ] T4.8.2.A9 If OpenRouter succeeds, capture `GET /api/generate/requests/A/review` and verify Review content is for A.
- [ ] T4.8.2.A10 If OpenRouter fails quickly, verify UI goes to Error page and does **not** show old Review.

### Run B

- [ ] T4.8.2.B1 Start again from `/generate/vacancy` in a clean/new context or after explicit state reset.
- [ ] T4.8.2.B2 Fill unique marker `PLAYWRIGHT_RUN_B_<timestamp>`.
- [ ] T4.8.2.B3 Capture requestId B from `POST /api/generate/requests`.
- [ ] T4.8.2.B4 Assert B != A.
- [ ] T4.8.2.B5 Capture settings/generate/review or error chain for B.
- [ ] T4.8.2.B6 Assert no request in Run B uses requestId A or old request `7176ff83-59d6-4e6d-96a1-011c70d45f67`.

## Acceptance criteria

- [ ] Uploaded source dump matches reported implementation.
- [ ] Running Docker backend/frontend match source.
- [ ] Two full Playwright UI runs completed.
- [ ] Run A and Run B use different requestIds.
- [ ] All network calls in each run use the correct requestId.
- [ ] Old request `7176ff83-59d6-4e6d-96a1-011c70d45f67` is not used by new generation flows.
- [ ] Fast OpenRouter error routes to Error page, not old Review.
- [ ] If generation succeeds, Review content is visibly tied to the unique marker of the current run.
- [ ] `debug_backlog.md` contains screenshots/network/log evidence.


---

## 0.15 Review update — dynamic prompt contract and six-scenario matrix

### Assessment

The latest 4-run evidence reduces the probability that the fast 500–700 ms generations are caused by stale requestId reuse: requestIds are different and the backend logs show OpenRouter calls. However, the generation quality is still not stable enough to enter Phase 5.

Confirmed remaining risks:

- One BILINGUAL + ALL run produced placeholder titles: `Profession` / `Профессия` instead of an actual role.
- The prompt/output contract is too ambiguous for non-BILINGUAL + ALL scenarios.
- The current JSON contract must become dynamic and exact: it must match the selected `languageMode` and `adaptationSelection`.
- Single-language + single-level scenarios must be tested as a matrix, not assumed to work because BILINGUAL + ALL works.
- The Review UI/finalize flow must not default to `BALANCED` when the only generated variant is `MINIMAL` or `MAXIMUM`.

### Current decision

Do **not** start Phase 5 until Phase 4.9 is completed.

Add and complete:

- **Phase 4.9 — Dynamic prompt contract and single-language/single-level matrix validation**

### New issues

| ID | Date/time | Phase | Severity | Symptom | Root cause hypothesis | Evidence | Current decision | Next action |
|---|---:|---|---|---|---|---|---|---|
| BUG-007-PROMPT-001 | 2026-06-13 | 4.9 | Critical | AI can return placeholder or loosely shaped output such as `Profession` / `Профессия` | Prompt contract is not exact enough for selected language/adaptation; model may follow placeholder labels or infer wrong shape | 4-run log showed placeholder professional titles in one run | Block Phase 5 | Add versioned prompt config / prompt builder dynamic JSON contract and validation tests |
| BUG-007-TITLE-001 | 2026-06-13 | 4.9 | High | `professionalTitle` may be `Profession`, `Профессия`, `string`, or another label instead of real role | No backend validation/fallback for placeholder generated values | 4-run log | Block Phase 5 | Add generated-content validation and source-preserving fallback |
| BUG-007-MATRIX-001 | 2026-06-13 | 4.9 | High | Six single-language/single-level scenarios are not proven | Previous E2E focused mostly on BILINGUAL + ALL and one EN + BALANCED run | Missing evidence for RU MIN/BAL/MAX and EN MIN/MAX | Block Phase 5 | Run strict Playwright matrix with complete logs and DB dumps |
| BUG-007-FINALIZE-002 | 2026-06-13 | 4.9 | High | Single-level Review may finalize as `BALANCED` even when generated level is `MINIMAL` or `MAXIMUM` | Frontend selectedLevel may default to Balanced and not sync with loaded single variant | Source-level risk | Must be tested/fixed before Phase 5/6 | Add tests and set selectedLevel from actual Review model when showLevels=false |

### New decisions

| ID | Date | Decision | Reason | Status |
|---|---|---|---|---|
| DEC-007-DYNAMIC-CONTRACT-001 | 2026-06-13 | The AI output JSON contract must be generated dynamically from `languageMode` + `adaptationSelection` | A flat/generic contract is too ambiguous and lets the model return extra/missing roots or placeholder values | Active |
| DEC-007-NO-PLACEHOLDERS-001 | 2026-06-13 | Generated critical fields must reject placeholder labels such as `Profession`, `Профессия`, `string`, `Title`, `Должность`, `N/A` | Placeholder output looks valid syntactically but breaks resume quality | Active |
| DEC-007-PROMPT-V4-001 | 2026-06-13 | Add a new versioned prompt config / prompt fragments for safe dynamic generation; do not alter budget config tables | Prompt quality must improve without breaking DB-backed budget config behavior | Active |
| DEC-007-MATRIX-E2E-001 | 2026-06-13 | Six single-language/single-level scenarios require Playwright proof before Phase 5 | BILINGUAL + ALL passing does not prove single combinations | Active |
| DEC-007-LOG-REDACTION-002 | 2026-06-13 | Full diagnostic dumps must exclude `ai_model(s)` and secret columns such as API keys, password hashes, tokens, cookies, and auth headers | Debug evidence is needed, but secrets must never be written to logs | Active |

# Phase 4.9 — Dynamic prompt contract and single-language/single-level matrix validation

## Goal

Make the AI prompt contract exact and safe for every supported generation shape:

- RUSSIAN_ONLY + MINIMAL
- RUSSIAN_ONLY + BALANCED
- RUSSIAN_ONLY + MAXIMUM
- ENGLISH_ONLY + MINIMAL
- ENGLISH_ONLY + BALANCED
- ENGLISH_ONLY + MAXIMUM
- BILINGUAL + ALL

Then prove the six single-language/single-level scenarios through Playwright with complete, redacted evidence.

## Hard scope boundary

Do not modify budget config tables or budget rules for this phase.

Allowed:

- new prompt config version;
- prompt fragment migration;
- prompt builder dynamic contract;
- parser tests;
- generated content validation/fallback;
- Review/finalize selectedLevel fix;
- Playwright matrix tests and logs.

Not allowed:

- changing budget config schema/data;
- logging API keys;
- dumping `ai_model` / `ai_models`;
- dumping password hashes, tokens, cookies, auth headers;
- hiding broken AI output with frontend-only cosmetic fixes.

## New prompts — safe target content

> These prompt texts are target content for a new versioned prompt config, e.g. `Default Resume Generation Prompt Config v4`.  
> Store them in the prompt config system if possible. If current DB prompt tables cannot express one piece, keep it in `ResumePromptBuilder` as code-generated dynamic instructions and document why.

### New system prompt

```text
You are ResumAIner, a strict resume generation engine.

Return valid JSON only. Do not include markdown fences, comments, explanations, or extra text.

Use only facts from the provided source profile, vacancy data, company data, and user additional instructions. Do not invent employers, dates, degrees, certificates, locations, languages, citizenship, or work formats.

Generate only the requested language roots and only the requested adaptation levels. Do not add extra languages. Do not add extra adaptation levels.

Never output placeholder labels or schema examples as real content. Forbidden placeholder values include: "Profession", "Профессия", "Title", "Должность", "string", "N/A", "Not specified", "Не указано", "TBD".

If a required resume field cannot be confidently generated, use the closest factual source value from the profile or vacancy. For professionalTitle, prefer: source professional title, current/most relevant work experience job title, or vacancy title. Do not use generic labels.

Preserve real company names, product names, course names, provider names, technologies, and proper nouns. Translate natural descriptive text into the requested language, but do not translate brand names unless the source already provides a localized version.

Education is profile-owned and must not be invented. Work formats are profile-owned normalized data: use only profile.workFormats values. If no work formats are selected, return null or an empty array according to the JSON contract.

Keep bilingual versions semantically consistent but natural in each language. Do not produce word-for-word machine translation if it sounds unnatural.
```

### New language fragments

```text
Language mode: ENGLISH_ONLY.
Return exactly one root object: "en".
All generated free-text resume fields and cover letter text must be in natural English.
Do not include the "ru" root.
```

```text
Language mode: RUSSIAN_ONLY.
Return exactly one root object: "ru".
All generated free-text resume fields and cover letter text must be in natural Russian.
Do not include the "en" root.
Keep non-translatable proper nouns, company names, product names, course names, and providers as written in the source unless a localized source value exists.
```

```text
Language mode: BILINGUAL.
Return exactly two root objects: "en" and "ru".
The two versions must describe the same real professional facts and situations.
Use natural English for "en" and natural Russian for "ru".
Preserve sourceId/order parity across languages for repeatable sections.
Do not add any language root other than "en" and "ru".
```

### New adaptation fragments

```text
Adaptation selection: MINIMAL.
Return exactly one adaptation level object: "minimal".
Make light cleanup and concise tailoring while preserving the original emphasis.
Do not include "balanced" or "maximum".
```

```text
Adaptation selection: BALANCED.
Return exactly one adaptation level object: "balanced".
Make practical tailoring to the vacancy, emphasizing relevant experience without overfitting.
Do not include "minimal" or "maximum".
```

```text
Adaptation selection: MAXIMUM.
Return exactly one adaptation level object: "maximum".
Strongly tailor positioning, emphasis, and wording to the vacancy while preserving facts.
Do not include "minimal" or "balanced".
```

```text
Adaptation selection: ALL.
Return exactly three adaptation level objects for each requested language: "minimal", "balanced", and "maximum".
The final saved resume will later use only one selected adaptation level.
Do not add any adaptation level other than "minimal", "balanced", and "maximum".
```

### New cover letter fragments

```text
Cover letter: enabled.
Return a non-empty coverLetter string for every generated language/adaptation variant.
The coverLetter must mention the target company name exactly as provided in vacancy/company data or user additional instructions.
The coverLetter language must match the variant language.
```

```text
Cover letter: disabled.
Return coverLetter as null for every generated language/adaptation variant.
Do not generate cover letter text.
```

### New global content quality rules

```text
Critical quality rules:
- professionalTitle must be an actual role/title, never "Profession", "Профессия", "Title", "Должность", "string", "N/A", or another placeholder.
- valueLine must be a concise positioning line, not a repeated field label.
- professionalSummary must be non-empty and factual.
- workExperience entries must preserve real companyName and dates where available.
- courses entries must include non-empty courseFocus if the course is included.
- projects entries must include non-empty projectName, role, and description if source project data exists.
- skills must be grouped when source skills allow it; do not invent skills.
- personalInfo must not invent optional blank profile fields.
- coverLetter must include the target company name when cover letter is enabled.
```

## Dynamic JSON contract requirement

`ResumePromptBuilder` must generate an exact JSON contract for the selected settings.

Do not use one flat generic contract for all settings.

Always instruct the model to return nested language/adaptation structure, even for single-language/single-level requests.

### Variant object definition

Use this object shape for every requested variant:

```json
{
  "professionalTitle": "actual role/title, not a placeholder",
  "valueLine": "concise positioning line",
  "professionalSummary": "resume-ready summary",
  "professionalAspirations": "optional but factual aspiration text",
  "workExperience": [
    {
      "jobTitle": "actual job title",
      "companyName": "source company name",
      "description": "adapted factual description",
      "location": "source location or null",
      "startDate": "YYYY-MM or source value",
      "endDate": "YYYY-MM, source value, or null",
      "isFirstPage": true
    }
  ],
  "courses": [
    {
      "name": "source course/certificate name",
      "provider": "source provider",
      "courseFocus": "1-3 word focus, non-empty if course exists"
    }
  ],
  "projects": [
    {
      "projectName": "source project name",
      "role": "source role or factual role",
      "description": "adapted factual project description",
      "startDate": "YYYY-MM or source value"
    }
  ],
  "skills": [
    {
      "skillGroup": "meaningful group name",
      "skillName": "source skill"
    }
  ],
  "personalInfo": {
    "location": "source location or null",
    "spokenLanguages": "source languages or null",
    "willingnessToRelocate": "source value or null",
    "willingnessForBusinessTrips": "source value or null",
    "citizenship": "source citizenship or null",
    "dateOfBirth": "YYYY-MM-DD source value or null",
    "workFormats": ["source work format values only"]
  },
  "coverLetter": "string when enabled, null when disabled"
}
```

### Exact contract examples by settings

For `RUSSIAN_ONLY + MINIMAL`:

```json
{
  "ru": {
    "minimal": { "VARIANT_OBJECT": "as defined above" }
  }
}
```

For `RUSSIAN_ONLY + BALANCED`:

```json
{
  "ru": {
    "balanced": { "VARIANT_OBJECT": "as defined above" }
  }
}
```

For `RUSSIAN_ONLY + MAXIMUM`:

```json
{
  "ru": {
    "maximum": { "VARIANT_OBJECT": "as defined above" }
  }
}
```

For `ENGLISH_ONLY + MINIMAL`:

```json
{
  "en": {
    "minimal": { "VARIANT_OBJECT": "as defined above" }
  }
}
```

For `ENGLISH_ONLY + BALANCED`:

```json
{
  "en": {
    "balanced": { "VARIANT_OBJECT": "as defined above" }
  }
}
```

For `ENGLISH_ONLY + MAXIMUM`:

```json
{
  "en": {
    "maximum": { "VARIANT_OBJECT": "as defined above" }
  }
}
```

For `BILINGUAL + ALL`:

```json
{
  "en": {
    "minimal": { "VARIANT_OBJECT": "as defined above" },
    "balanced": { "VARIANT_OBJECT": "as defined above" },
    "maximum": { "VARIANT_OBJECT": "as defined above" }
  },
  "ru": {
    "minimal": { "VARIANT_OBJECT": "as defined above" },
    "balanced": { "VARIANT_OBJECT": "as defined above" },
    "maximum": { "VARIANT_OBJECT": "as defined above" }
  }
}
```

Implementation note: the real prompt should expand `VARIANT_OBJECT` into the actual object fields. Do not send `"VARIANT_OBJECT"` literally to the model.

## Required backend TDD tests

- [ ] T4.9.B1 RED: `buildJsonContract_russianMinimal_containsOnlyRuMinimal()`
- [ ] T4.9.B2 RED: `buildJsonContract_russianBalanced_containsOnlyRuBalanced()`
- [ ] T4.9.B3 RED: `buildJsonContract_russianMaximum_containsOnlyRuMaximum()`
- [ ] T4.9.B4 RED: `buildJsonContract_englishMinimal_containsOnlyEnMinimal()`
- [ ] T4.9.B5 RED: `buildJsonContract_englishBalanced_containsOnlyEnBalanced()`
- [ ] T4.9.B6 RED: `buildJsonContract_englishMaximum_containsOnlyEnMaximum()`
- [ ] T4.9.B7 RED: `buildJsonContract_bilingualAll_containsEnAndRuAllThreeLevels()`
- [ ] T4.9.B8 RED: `promptRejectsOrWarnsAboutPlaceholderProfessionalTitle()`
- [ ] T4.9.B9 RED: `professionalTitleFallbackUsesSourceRoleWhenAiReturnsPlaceholder()`
- [ ] T4.9.B10 RED: `coverLetterInstructionRequiresCompanyNameWhenEnabled()`
- [ ] T4.9.B11 RED: parser accepts exact nested single-language/single-level JSON.
- [ ] T4.9.B12 RED: parser does not persist extra unrequested language roots/levels if model returns them.

## Required frontend TDD tests

- [ ] T4.9.F1 RED: single EN MINIMAL Review sets selectedLevel to Minimal.
- [ ] T4.9.F2 RED: single EN MAXIMUM Review sets selectedLevel to Maximum.
- [ ] T4.9.F3 RED: single RU MINIMAL Review sets selectedLevel to Minimal.
- [ ] T4.9.F4 RED: single RU MAXIMUM Review sets selectedLevel to Maximum.
- [ ] T4.9.F5 RED: finalize uses the actual single generated level, not default Balanced.
- [ ] T4.9.F6 RED: single-language Review shows only selected language and does not render empty other-language card.

## Six-scenario Playwright matrix

All scenarios must use:

- cover letter enabled;
- unique vacancy title;
- unique company name;
- unique company description;
- unique vacancy description;
- unique additional AI instruction telling the model to include the company name in the cover letter;
- clean browser context or explicit generation state reset;
- captured browser console logs;
- captured network request/response metadata;
- backend/java Docker logs;
- frontend/nginx Docker logs;
- postgres Docker logs;
- redacted AI request/response files;
- model response timer;
- full redacted DB dump after generation.

### Scenario 1 — Russian + Minimal

- Language: RUSSIAN_ONLY
- Adaptation: MINIMAL
- Cover letter: enabled
- Vacancy title: `S1_RU_MIN_Business_Analyst_<timestamp>`
- Company name: `S1_RU_MIN_Company_<timestamp>`
- Company description: unique Russian company description with the marker.
- Vacancy description: unique Russian BA vacancy description with the marker.
- Additional AI instructions: `Mention S1_RU_MIN_Company_<timestamp> in the cover letter.`

Expected:

- exactly 1 response row;
- lang = RU;
- adaptation = MINIMAL;
- Review shows Russian content only;
- no EN card;
- professionalTitle is Russian and not `Профессия`;
- coverLetter is Russian and contains exact company name.

### Scenario 2 — Russian + Balanced

- Language: RUSSIAN_ONLY
- Adaptation: BALANCED
- Cover letter: enabled
- Unique marker prefix: `S2_RU_BAL_<timestamp>`

Expected:

- exactly 1 response row;
- lang = RU;
- adaptation = BALANCED;
- Review shows Russian content only;
- professionalTitle is Russian and not placeholder;
- coverLetter contains exact company name.

### Scenario 3 — Russian + Maximum

- Language: RUSSIAN_ONLY
- Adaptation: MAXIMUM
- Cover letter: enabled
- Unique marker prefix: `S3_RU_MAX_<timestamp>`

Expected:

- exactly 1 response row;
- lang = RU;
- adaptation = MAXIMUM;
- selectedLevel/finalize state is Maximum, not Balanced;
- Review shows Russian content only;
- coverLetter contains exact company name.

### Scenario 4 — English + Minimal

- Language: ENGLISH_ONLY
- Adaptation: MINIMAL
- Cover letter: enabled
- Unique marker prefix: `S4_EN_MIN_<timestamp>`

Expected:

- exactly 1 response row;
- lang = EN;
- adaptation = MINIMAL;
- selectedLevel/finalize state is Minimal, not Balanced;
- Review shows English content only;
- coverLetter contains exact company name.

### Scenario 5 — English + Balanced

- Language: ENGLISH_ONLY
- Adaptation: BALANCED
- Cover letter: enabled
- Unique marker prefix: `S5_EN_BAL_<timestamp>`

Expected:

- exactly 1 response row;
- lang = EN;
- adaptation = BALANCED;
- Review shows English content only;
- coverLetter contains exact company name.

### Scenario 6 — English + Maximum

- Language: ENGLISH_ONLY
- Adaptation: MAXIMUM
- Cover letter: enabled
- Unique marker prefix: `S6_EN_MAX_<timestamp>`

Expected:

- exactly 1 response row;
- lang = EN;
- adaptation = MAXIMUM;
- selectedLevel/finalize state is Maximum, not Balanced;
- Review shows English content only;
- coverLetter contains exact company name.

## Required evidence after each scenario

For each scenario create a separate folder:

```text
tempfiles/phase_4_9_matrix/<scenario_code>/
```

Required files:

```text
01_playwright_console.log
02_playwright_network.md
03_browser_screenshot_vacancy.png
04_browser_screenshot_settings.png
05_browser_screenshot_review_professional_positioning.png
06_browser_screenshot_review_work_experience.png
07_browser_screenshot_review_courses.png
08_browser_screenshot_review_projects.png
09_browser_screenshot_review_skills.png
10_browser_screenshot_review_personal_info.png
11_docker_logs_backend_java.log
12_docker_logs_frontend_nginx.log
13_docker_logs_postgres.log
14_ai_request_without_secrets.json
15_ai_raw_response.json
16_ai_extracted_content.txt
17_ai_parsed_json.json
18_review_dto.json
19_db_dump_redacted.sql
20_db_review_field_matrix.md
21_scenario_assertions.md
```

## DB dump requirements

After each scenario, dump all application tables and all non-secret columns.

Strict exclusions:

- exclude table `ai_model`;
- exclude table `ai_models` if present;
- exclude API key columns anywhere;
- exclude password hashes;
- exclude auth/session tokens;
- exclude cookies;
- exclude CSRF tokens;
- exclude Authorization headers;
- exclude any `.env` values.

The dump must still include generation-related tables:

- `resume_generation_request`;
- `resume_generation_response`;
- all `generation_response_*` child tables;
- prompt config tables;
- budget config tables;
- saved resume tables if touched;
- user/profile tables with secret columns removed.

## Review field matrix requirements

For each scenario, create `20_db_review_field_matrix.md`.

It must list what will appear on Review for every section and field.

Minimum structure:

```markdown
# Review Field Matrix — <scenario>

requestId:
languageMode:
adaptationSelection:
companyName:
model:
durationMs:

## Professional Positioning
| field | DB value | UI value | language correct? | placeholder? |
|---|---|---|---|---|

## Work Experience
| record | field | DB value | UI value | language correct? | placeholder? |
|---|---|---|---|---|---|

## Courses & Certifications
| record | field | DB value | UI value | language correct? | placeholder? |
|---|---|---|---|---|---|

## Projects & Volunteering
| record | field | DB value | UI value | language correct? | placeholder? |
|---|---|---|---|---|---|

## Skills
| group | skill | DB value | UI value | language correct? | placeholder? |
|---|---|---|---|---|---|

## Personal Information
| field | DB value | UI value | visible? | reason |
|---|---|---|---|---|

## Cover Letter
| expected company name | DB value contains company? | UI value contains company? | language correct? |
|---|---|---|---|
```

## Pass/fail criteria per scenario

A scenario fails if any of these happen:

- wrong language root is persisted;
- wrong adaptation level is persisted;
- more variants than requested are persisted;
- no response row is persisted after successful model call;
- Review shows the wrong language;
- Review shows an empty other-language card;
- professionalTitle is placeholder;
- coverLetter is missing;
- coverLetter does not contain exact company name;
- visible required field is blank;
- selectedLevel/finalize state is wrong for MINIMAL or MAXIMUM;
- old requestId/content appears;
- AI request/response logs are missing;
- DB dump is missing;
- secrets are included in logs/dumps.

## Acceptance criteria for Phase 4.9

- [ ] New safe prompt config / prompt fragments are documented.
- [ ] Dynamic JSON contract is implemented and tested for all 6 single scenarios plus BILINGUAL + ALL.
- [ ] Placeholder professionalTitle validation/fallback is implemented and tested.
- [ ] Single-level selectedLevel/finalize default is fixed and tested.
- [ ] Six Playwright scenarios pass.
- [ ] Every scenario has full redacted evidence folder.
- [ ] `debug_backlog.md` is updated with per-scenario results.
- [ ] No secrets are written to logs/dumps.
- [ ] Phase 5 may start only after review of the matrix logs.


---

## 1. Phase checklist

Must update this checklist while working.

- [ ] **Phase 0 — Working protocol and safety rules**
- [ ] **Phase 1 — Baseline reproduction and debug observability**
- [ ] **Phase 2 — AI response pipeline diagnosis for empty Review tabs**
- [ ] **Phase 3 — Fix Work/Courses/Projects/Skills Review data population**
- [ ] **Phase 4 — Validate Review UI layout for EN/RU and ALL adaptation levels**
- [ ] **Phase 4.5 — Stable child record grouping across languages and adaptation levels**
- [ ] **Phase 5 — Review save/update contract and tests**
- [ ] **Phase 6 — Finalize HTML storage and Docker writable path**
- [ ] **Phase 7 — Export page and HTML download verification**
- [ ] **Phase 8 — Full E2E test pass for feat/007**
- [ ] **Phase 9 — Cleanup, docs, and final handoff**

---

## 2. Operating rules

### 2.1 Hard boundaries

- [ ] Do not touch real PDF conversion.
- [ ] Do not remove PDF/public-link placeholder buttons from Export.
- [ ] Do not use fake PDF files as real PDFs.
- [ ] Do not hide backend bugs with frontend mock data.
- [ ] Do not flatten backend DTOs only to match the prototype if the current hierarchical DTO can be fixed.
- [ ] Do not save edits on every keystroke.
- [ ] Do not log API keys, Authorization headers, session cookies, password hashes, or secrets.
- [ ] Do not commit local debug files with real prompts/responses.
- [ ] Do not continue random fixes after a new blocker appears. Stop, document the blocker in this file, then ask for review.

### 2.2 Required workflow per task

For every task:

1. Reproduce or inspect first.
2. State the observed root cause.
3. Make the smallest coherent fix.
4. Add or update tests.
5. Run targeted tests.
6. Run build/compile.
7. Update this backlog with evidence.
8. If blocked, stop and write a detailed issue entry.

---

## 3. Working log

Must append entries here.

| Date/time | Phase | Task | Status | Evidence / command output | Files changed | Notes / blockers |
|---|---:|---|---|---|---|---|
| YYYY-MM-DD HH:mm |  |  | Not started / In progress / Done / Blocked |  |  |  |
| 2026-06-13 | 4.4/4.5 | Review after Projects screenshot | Blocked / new bug found | Screenshot shows project fields filled only for one language/level while other levels/language are empty inside the same project block. Likely grouping by generated child record UUID instead of stable source identity. | debug_backlog.md | Add Phase 4.5 before Phase 5. |

---

## 4. Issue log

Must add blockers here instead of guessing.

| ID | Date/time | Phase | Severity | Symptom | Root cause hypothesis | Evidence | Current decision | Next action |
|---|---|---:|---|---|---|---|---|---|
| BUG-007-RVW-001 |  | 2 | High | Work/Courses/Projects/Skills tabs empty | Unknown: prompt, AI response, parser, persistence, review DTO, or adapter |  | Diagnose full pipeline |  |
| BUG-007-RVW-003 | 2026-06-13 | 4.5 | Critical | In Projects tab, one project block shows values for only one language/level and empty fields for other levels/language | Same original project is likely represented by different generated child row UUIDs for EN/RU and MIN/BAL/MAX; frontend groups by generated row id instead of stable source key | Screenshot; frontend uses `projRecordIds`; backend child DTO recordId is generated child row UUID | Do not proceed to Phase 5 | Introduce stable grouping key: sourceId/sourceKey from profile or orderInResume-based MVP grouping; keep updateKey per real child row for saving |
| BUG-007-FIN-001 |  | 6 | High | Finalize returns 500 | GeneratedFileStorageService cannot write `generated_results/` in Docker |  | Fix writable storage path |  |

---

## 5. Decision log

| ID | Date | Decision | Reason | Status |
|---|---|---|---|---|
| DEC-007-CHILD-GROUP-001 | 2026-06-13 | Review UI must group child records by stable source identity, not generated child row UUID | Generated child ids differ per language/adaptation response and cause sparse/empty cells | Active |
| DEC-007-CHILD-GROUP-002 | 2026-06-13 | For MVP, grouping by `sectionKey + orderInResume` is acceptable only if original sourceId is not available yet | It fixes UI alignment without breaking updateKey save contract, but is weaker than persisted sourceId | Proposed |
| DEC-007-REVIEW-001 |  | Keep backend hierarchical Review DTO and use frontend adapter | Avoid backend DTO flattening and preserve domain structure | Active |
| DEC-007-REVIEW-002 |  | Backend returns `updateKey` for each editable field; frontend sends updateKey back | Frontend must not guess save keys | Active |
| DEC-007-PDF-001 |  | Real PDF conversion remains deferred to feat/008 | feat/007 scope is HTML generation/export and PDF placeholders | Active |
| DEC-007-LOG-001 |  | Add dev-only AI/debug logging with secret redaction | Avoid blind debugging of AI pipeline | Proposed |

---

# Phase 0 — Working protocol and safety rules

## Goal

Prepare this file as the working loop document and make sure to use it as a source of truth.

## Tasks

- [ ] T0.1 Read this whole `debug_backlog.md` before coding.
- [ ] T0.2 Confirm current branch is `feat/007-resume-generation`.
- [ ] T0.3 Add this file to the repo under a clear docs path, for example:
  - `docs/debug_backlog.md`, or
  - `specs/007-resume-generation/debug_backlog.md`.
- [ ] T0.4 Do not mark tasks as done without evidence.
- [ ] T0.5 When a phase is complete, add a short phase report under that phase.

## Tests / validation

- [ ] Confirm file exists in repo.
- [ ] Confirm updates Working log and Issue log after each significant step.

---

# Phase 1 — Baseline reproduction and debug observability

## Goal

Before fixing empty Review tabs, establish clear debug visibility from user profile → prompt payload → OpenRouter request → raw AI response → parsed JSON → persisted rows → Review DTO → frontend adapter → rendered UI.

## Tasks

### Backend debug mode

- [ ] T1.1 Add a dev-only config flag:
  - property example: `resumainer.debug.ai.enabled=false`
  - env override example: `RESUMAINER_DEBUG_AI_ENABLED=true`
- [ ] T1.2 Add a dev-only debug output directory:
  - property example: `resumainer.debug.ai.dir=debug/feat007-ai`
  - must be gitignored.
- [ ] T1.3 Add `.gitignore` entries if missing:
  - `debug/`
  - `generated_results/`
  - `*.local.json`
  - `*_raw_ai_response.json`
  - `*_prompt_payload.json`
- [ ] T1.4 Implement safe debug snapshots per requestId:
  - `01_profile_payload.json`
  - `02_prompt_payload.json`
  - `03_openrouter_request_without_secrets.json`
  - `04_raw_openrouter_response.json`
  - `05_extracted_ai_content.txt`
  - `06_parsed_ai_json.json`
  - `07_persistence_counts.json`
  - `08_review_dto.json`
- [ ] T1.5 Never log or write:
  - API key
  - Authorization header
  - session cookie
  - password/password hash
  - CSRF token
- [ ] T1.6 Add structured logs with requestId:
  - generation started
  - prompt built
  - OpenRouter call started/finished
  - response parsed
  - rows persisted by section
  - review DTO built with section counts
  - finalize started/failed/succeeded

### Frontend debug mode

- [ ] T1.7 Add frontend dev-only debug console group for Review page:
  - raw Review DTO section counts
  - adapted `enVariants` / `ruVariants` counts
  - missing sections warning
  - update payload preview before save
- [ ] T1.8 Ensure debug output is only active in dev mode or behind a flag.

## Backend tests

- [ ] T1.B1 Test debug writer redacts Authorization/API key.
- [ ] T1.B2 Test debug writer creates files only when debug flag is enabled.
- [ ] T1.B3 Test debug writer does not break generation if debug file write fails; it should log warning and continue.

## Frontend tests

- [ ] T1.F1 If frontend test runner exists, add test for Review debug summary helper.
- [ ] T1.F2 If no frontend test runner exists, add this as a later setup task and use Playwright evidence for now.

## Acceptance criteria

- [ ] We can inspect what went to the model.
- [ ] We can inspect the raw AI response.
- [ ] We can inspect parsed JSON.
- [ ] We can see exactly how many work/course/project/skill/personal rows were persisted.
- [ ] No secrets are written to logs or debug files.

---

# Phase 2 — AI response pipeline diagnosis for empty Review tabs

## Goal

Find the exact reason why Work/Courses/Projects/Skills tabs are empty.

Possible failure points:

1. Profile payload lacks work/courses/projects/skills.
2. Prompt does not ask model to generate these sections clearly.
3. OpenRouter response does not contain these sections.
4. AI response contains sections, but `AiResponseParser` fails to parse them.
5. Parser parses them, but `GenerationResponsePersistenceService` does not persist them.
6. Rows are persisted, but `ResumeReviewService` does not load/build them into Review DTO.
7. Review DTO has data, but `generateReviewAdapter.ts` drops them.
8. Adapter has data, but `ReviewStepForm.vue` does not render them.

## Tasks

### Reproduce with controlled test

- [ ] T2.1 Login with `test@test.com / Aa123456`.
- [ ] T2.2 Use scenario A:
  - Language: English only
  - Adaptation: Balanced
- [ ] T2.3 Use scenario B:
  - Language: Bilingual
  - Adaptation: All
- [ ] T2.4 Generate with a vacancy that explicitly asks for BA experience, courses, projects, skills.
- [ ] T2.5 Save debug files from Phase 1.

### Inspect profile payload

- [ ] T2.6 Check debug `01_profile_payload.json`.
- [ ] T2.7 Confirm profile payload contains:
  - work experience records
  - courses/certifications
  - projects
  - skills/additional info
  - personal info
- [ ] T2.8 If profile payload lacks sections, inspect profile DAO / prompt payload builder.

### Inspect prompt

- [ ] T2.9 Check `02_prompt_payload.json` and rendered prompt log.
- [ ] T2.10 Confirm prompt tells model to output:
  - workExperience
  - courses
  - projects
  - skills
  - personalInfo
- [ ] T2.11 Confirm expected output JSON schema includes these sections for each generated variant.
- [ ] T2.12 If prompt/schema does not require sections, fix prompt config, not parser.

### Inspect raw AI response

- [ ] T2.13 Check `04_raw_openrouter_response.json`.
- [ ] T2.14 Check `05_extracted_ai_content.txt`.
- [ ] T2.15 Check `06_parsed_ai_json.json`.
- [ ] T2.16 Record whether AI returned:
  - workExperience count
  - courses count
  - projects count
  - skills count
  - personalInfo present

### Inspect parser

- [ ] T2.17 Add/verify `AiResponseParserTest` with sample AI JSON containing all sections.
- [ ] T2.18 Test camelCase and snake_case variants:
  - `workExperience` and `work_experience`
  - `professionalTitle` and `professional_title`
  - `personalInfo` and `personal_info`
- [ ] T2.19 Confirm parser outputs `ParsedVariant` with non-empty child collections.

### Inspect persistence

- [ ] T2.20 Add/verify service/DAO tests proving generated child rows are inserted:
  - generation_response_experience
  - generation_response_course
  - generation_response_project
  - generation_response_skill
  - generation_response_personal
- [ ] T2.21 Log persistence counts by section.

### Inspect Review DTO

- [ ] T2.22 Test `ResumeReviewService.getReview()` after seeded generated rows.
- [ ] T2.23 Assert Review DTO includes non-empty records for work/courses/projects/skills when generated rows exist.
- [ ] T2.24 Assert every editable field has `updateKey`.

### Inspect frontend adapter

- [ ] T2.25 Add unit tests for `adaptGenerationReviewDto()`.
- [ ] T2.26 Input DTO with work/courses/projects/skills records.
- [ ] T2.27 Assert adapted `GeneratedVariant` contains corresponding records.
- [ ] T2.28 Assert `__meta` preserves updateKey for all editable fields.

### Inspect UI rendering

- [ ] T2.29 Use Playwright to verify non-empty sections appear in Review UI when DTO has data.
- [ ] T2.30 If API response has data but UI empty, fix frontend adapter/UI.

## Backend tests

- [ ] T2.B1 `AiResponseParserTest.parsesFullVariantWithAllSections()`
- [ ] T2.B2 `GenerationResponsePersistenceServiceTest.persistsAllGeneratedSections()`
- [ ] T2.B3 `ResumeReviewServiceTest.reviewDtoContainsAllSectionsWhenRowsExist()`
- [ ] T2.B4 `ResumeReviewServiceTest.reviewDtoContainsUpdateKeysForEditableFields()`

## Frontend tests

- [ ] T2.F1 `generateReviewAdapter.adaptsWorkExperience()`
- [ ] T2.F2 `generateReviewAdapter.adaptsCourses()`
- [ ] T2.F3 `generateReviewAdapter.adaptsProjects()`
- [ ] T2.F4 `generateReviewAdapter.adaptsSkills()`
- [ ] T2.F5 `generateReviewAdapter.preservesUpdateKeys()`
- [ ] T2.F6 Playwright: Review page shows non-empty tabs when backend returns non-empty DTO.

## Acceptance criteria

- [ ] We know exactly where the empty tabs originate.
- [ ] The root cause is documented in Issue log.
- [ ] No guessing remains about AI response, parser, persistence, DTO, adapter, or UI.

---

# Phase 3 — Fix Work/Courses/Projects/Skills Review data population

## Goal

Make empty tabs non-empty when generated data exists.

## Tasks by root cause

### If profile payload is missing data

- [ ] T3.1 Inspect profile prompt builder.
- [ ] T3.2 Ensure work/courses/projects/skills are included in AI prompt payload if present in user profile.
- [ ] T3.3 Add backend test for profile payload completeness.

### If prompt/schema does not require sections

- [ ] T3.4 Update DB prompt config / system prompt / request prompt schema.
- [ ] T3.5 Make output schema explicit:
  - every variant should contain `workExperience`, `courses`, `projects`, `skills`, `personalInfo`.
- [ ] T3.6 Add prompt render test or snapshot-like assertion.

### If AI response omits sections despite good prompt

- [ ] T3.7 Add validation after parsing: if critical sections are missing, log warning with requestId and model.
- [ ] T3.8 Do not invent fake data.
- [ ] T3.9 Consider prompt tightening only if repeated evidence shows model omits sections.

### If parser fails

- [ ] T3.10 Fix `AiResponseParser` for all expected section names.
- [ ] T3.11 Support camelCase and snake_case where reasonable.
- [ ] T3.12 Add parser tests with realistic sample response.

### If persistence fails

- [ ] T3.13 Fix `GenerationResponsePersistenceService` or DAO insertion.
- [ ] T3.14 Add tests that verify DB row counts after persistence.

### If Review DTO fails

- [ ] T3.15 Fix `ResumeReviewService.buildSections()` and child record loaders.
- [ ] T3.16 Ensure sections:
  - `professional_positioning`
  - `work_experience`
  - `courses`
  - `projects`
  - `skills`
  - `personal_information`
- [ ] T3.17 Ensure no Education section.
- [ ] T3.18 Ensure each editable field includes `updateKey`.

### If frontend adapter fails

- [ ] T3.19 Fix `generateReviewAdapter.ts` mapping.
- [ ] T3.20 Add adapter tests.

### If UI fails

- [ ] T3.21 Fix `ReviewStepForm.vue` rendering.
- [ ] T3.22 Verify tabs render records and empty state only when a section truly has no records.

## Backend tests

- [ ] T3.B1 Tests for prompt payload containing profile work/courses/projects/skills.
- [ ] T3.B2 Parser tests for full sample.
- [ ] T3.B3 Persistence row count tests.
- [ ] T3.B4 Review DTO non-empty section tests.

## Frontend tests

- [ ] T3.F1 Adapter mapping tests.
- [ ] T3.F2 Playwright non-empty Review tabs test.

## Acceptance criteria

- [ ] If AI produces work/courses/projects/skills, Review UI shows them.
- [ ] Empty tabs are explained by actual missing AI output, not by parser/persistence/UI loss.
- [ ] Debug logs show the exact counts at each pipeline step.

---

# Phase 4 — Validate Review UI layout for EN/RU and ALL adaptation levels

## Goal

Ensure Review page matches prototype behavior for `BILINGUAL + ALL`.

## Tasks

- [ ] T4.1 Generate with settings:
  - Language: Bilingual
  - Adaptation: All
- [ ] T4.2 Confirm backend creates 6 response rows:
  - EN Minimal
  - EN Balanced
  - EN Maximum
  - RU Minimal
  - RU Balanced
  - RU Maximum
- [ ] T4.3 Confirm Review DTO contains both languages.
- [ ] T4.4 Confirm adapter creates:
  - `enVariants.length === 3`
  - `ruVariants.length === 3`
- [ ] T4.5 Confirm UI shows EN/RU side by side on desktop.
- [ ] T4.6 Confirm inside each language card there are level variants.
- [ ] T4.7 Confirm selected final adaptation level applies to both languages.
- [ ] T4.8 Confirm no Education tab.
- [ ] T4.9 Confirm mobile layout stacks language cards vertically.

## Backend tests

- [ ] T4.B1 Test Bilingual + All creates 6 responses.
- [ ] T4.B2 Test Review DTO groups by language and adaptation level.

## Frontend tests

- [ ] T4.F1 Adapter test for Bilingual + All.
- [ ] T4.F2 Playwright desktop layout test.
- [ ] T4.F3 Playwright mobile layout smoke test.

## Acceptance criteria

- [ ] Bilingual + All Review behaves like prototype.
- [ ] One final selected level creates one saved resume per language.
- [ ] No duplicated or mixed EN/RU fields.

---

# Phase 4.5 — Stable child record grouping across languages and adaptation levels

## Goal

Fix sparse Review records where values appear only in one language/level and other cells are empty because the UI groups child rows by generated child UUID.

This applies to repeatable sections:

- Work Experience
- Courses
- Projects
- Skills groups

## Suspected current broken behavior

For BILINGUAL + ALL, the same original project can generate 6 child rows:

- EN MIN project row id A
- EN BAL project row id B
- EN MAX project row id C
- RU MIN project row id D
- RU BAL project row id E
- RU MAX project row id F

If the frontend groups by A/B/C/D/E/F, it renders 6 project blocks. Each block has data only for one variant, and other levels/languages look empty.

## Required behavior

The same source project should render as one project block:

- EN card: Minimal/Balanced/Maximum values
- RU card: Minimal/Balanced/Maximum values

The same rule applies to work records, course records, and skill groups.

## TDD tasks

- [ ] T4.5.B1 Add backend/DTO test proving child records expose a stable grouping key across EN/RU and MIN/BAL/MAX.
- [ ] T4.5.F1 Add adapter test with 6 backend child records for the same logical project but different generated child `recordId`s. Expected: one UI project group with all 6 variants populated.
- [ ] T4.5.F2 Add adapter test with two source projects. Expected: two UI project groups, not six or twelve sparse groups.
- [ ] T4.5.F3 Add equivalent tests for work experience and courses.
- [ ] T4.5.F4 Add Playwright assertion: Projects tab has one visible project block for one profile project and all EN/RU + levels are filled in that block.
- [ ] T4.5.F5 Add Playwright assertion: no project block has all empty RU or all empty non-minimal levels when corresponding DB values exist.

## Implementation options

### Preferred backend-contract fix

- [ ] T4.5.P1 Add stable `sourceId` or `sourceKey` to Review DTO `RecordReviewGroup`.
- [ ] T4.5.P2 Persist original profile source id for work/courses/projects where available.
- [ ] T4.5.P3 Use source id in frontend grouping, but keep backend `updateKey` pointing to the actual generated child row id for save.
- [ ] T4.5.P4 Add migration if child generated tables need `source_id` / `source_key` columns.

### MVP frontend/adapter fix if schema change is too large

- [ ] T4.5.M1 Compute UI grouping key as `sectionKey + orderInResume` for repeatable sections.
- [ ] T4.5.M2 Keep actual generated child `recordId` only inside per-field `updateKey` metadata.
- [ ] T4.5.M3 Do not use generated child UUID as visible UI grouping key.
- [ ] T4.5.M4 Document this as an MVP compromise and create follow-up task for persisted source ids.

## Acceptance criteria

- [ ] Projects tab no longer shows sparse blocks with only one level/language filled.
- [ ] Work/Courses/Projects use stable grouping across languages and adaptation levels.
- [ ] Save/update still uses backend-provided `updateKey` and targets the correct generated child row.
- [ ] Tests prove grouping works when generated child row ids differ per response.
- [ ] BILINGUAL + ALL screenshot/Playwright evidence shows a clean matrix-like layout.

---

# Phase 5 — Review save/update contract and tests

## Goal

Make editing and saving robust for all supported Review fields.

## Tasks

- [ ] T5.1 Verify backend returns `updateKey` for every editable field.
- [ ] T5.2 Verify frontend stores original value and current value.
- [ ] T5.3 Verify frontend sends only changed fields.
- [ ] T5.4 Verify backend rejects forbidden update keys.
- [ ] T5.5 Verify backend updates top-level fields.
- [ ] T5.6 Verify backend updates child fields that are editable.
- [ ] T5.7 Verify backend updates personal info fields.
- [ ] T5.8 Verify skills update behavior is clear:
  - CSV group update if implemented;
  - otherwise read-only with clear limitation.
- [ ] T5.9 Verify save happens before finalize.

## Backend tests

- [ ] T5.B1 Save professional title.
- [ ] T5.B2 Save work experience description.
- [ ] T5.B3 Save course focus/provider if supported.
- [ ] T5.B4 Save project description if supported.
- [ ] T5.B5 Save personal location/work formats if supported.
- [ ] T5.B6 Reject forbidden field.
- [ ] T5.B7 Reject malformed updateKey.
- [ ] T5.B8 Reject update target not belonging to request/user.

## Frontend tests

- [ ] T5.F1 `buildReviewUpdatePayload()` sends changed field only.
- [ ] T5.F2 `buildReviewUpdatePayload()` ignores unchanged fields.
- [ ] T5.F3 `buildReviewUpdatePayload()` uses backend updateKey.
- [ ] T5.F4 Playwright edit → save → reload review → value persists.

## Acceptance criteria

- [ ] User edits are not lost.
- [ ] Save payload is minimal and safe.
- [ ] Backend does not trust raw frontend field names.

---

# Phase 6 — Finalize HTML storage and Docker writable path

## Goal

Fix current finalize 500 caused by generated HTML storage path not writable in Docker.

## Known symptom

`POST /api/generate/.../finalize` returns 500. Current hypothesis: `GeneratedFileStorageService.saveFile()` cannot create/write under `generated_results/` inside Docker.

## Tasks

- [ ] T6.1 Inspect `GeneratedFileStorageService`.
- [ ] T6.2 Replace hardcoded/relative storage path with configurable property:
  - `resumainer.generated-results-dir=/app/generated_results` or similar.
- [ ] T6.3 Add environment variable override for Docker.
- [ ] T6.4 Update Docker Compose / Dockerfile / runtime config to mount writable volume.
- [ ] T6.5 Ensure directory is created on startup or before write.
- [ ] T6.6 Ensure app user has write permissions.
- [ ] T6.7 If write fails, return structured user-readable error, not raw stacktrace.
- [ ] T6.8 Do not create saved resume marked downloadable if HTML write failed.
- [ ] T6.9 Clean up orphan files if DB save fails after file write.

## Backend tests

- [ ] T6.B1 `GeneratedFileStorageServiceTest.savesFileToConfiguredDirectory()`
- [ ] T6.B2 `GeneratedFileStorageServiceTest.createsNestedDirectories()`
- [ ] T6.B3 `GeneratedFileStorageServiceTest.failsGracefullyWhenDirectoryNotWritable()`
- [ ] T6.B4 `ResumeFinalizeServiceTest.doesNotCreateDownloadableResumeWhenHtmlWriteFails()`
- [ ] T6.B5 Controller test returns structured JSON error for storage failure.

## E2E tests

- [ ] T6.E1 Docker E2E finalize succeeds.
- [ ] T6.E2 Generated HTML file exists inside mounted volume.
- [ ] T6.E3 Export page shows saved resume card after finalize.

## Acceptance criteria

- [ ] Finalize no longer fails with storage path error.
- [ ] HTML is saved to disk.
- [ ] Saved resume DB row points to stored HTML.
- [ ] No real PDF is generated in feat/007.

---

# Phase 7 — Export page and HTML download verification

## Goal

Complete feat/007 export behavior: saved HTML download works; PDF/public actions remain safe placeholders.

## Tasks

- [ ] T7.1 Verify export DTO after successful finalize.
- [ ] T7.2 Verify Export page shows saved resume card(s).
- [ ] T7.3 Verify HTML download endpoint:
  - authenticated
  - owner-scoped
  - no raw file path exposed
- [ ] T7.4 Verify PDF buttons remain visible and placeholder-only.
- [ ] T7.5 Verify public link placeholder does not expose private data.
- [ ] T7.6 Verify copy cover letter works if cover letter generated.

## Backend tests

- [ ] T7.B1 HTML download owner can download.
- [ ] T7.B2 Other user cannot download.
- [ ] T7.B3 Missing file returns safe JSON error or correct HTTP status.
- [ ] T7.B4 Export DTO includes `htmlDownloadUrl`, placeholder `pdfDownloadUrl`, `pdfOpenUrl`, `publicUrlLink`, `pdfAvailable=false`.

## Frontend / Playwright tests

- [ ] T7.F1 Export page shows all five actions.
- [ ] T7.F2 Download HTML returns `.html` file.
- [ ] T7.F3 PDF buttons show placeholder behavior.
- [ ] T7.F4 Public link copy works and is safe placeholder.

## Acceptance criteria

- [ ] Full flow reaches Export after finalize.
- [ ] User can download generated HTML.
- [ ] PDF remains out of scope but UI contract is preserved.

---

# Phase 8 — Full E2E test pass for feat/007

## Goal

Run stable E2E verification for all critical paths.

## Scenarios

### Scenario A — English only + Balanced

- [ ] T8.A1 Login.
- [ ] T8.A2 Fill vacancy.
- [ ] T8.A3 Choose English only + Balanced.
- [ ] T8.A4 Generate.
- [ ] T8.A5 Review shows 6 tabs.
- [ ] T8.A6 Edit professional title.
- [ ] T8.A7 Save/finalize.
- [ ] T8.A8 Export shows one saved resume.
- [ ] T8.A9 Download HTML.

### Scenario B — Bilingual + All

- [ ] T8.B1 Login.
- [ ] T8.B2 Fill vacancy.
- [ ] T8.B3 Choose Bilingual + All.
- [ ] T8.B4 Generate.
- [ ] T8.B5 Review shows EN/RU + Minimal/Balanced/Maximum.
- [ ] T8.B6 Select Balanced.
- [ ] T8.B7 Save/finalize.
- [ ] T8.B8 Export shows two saved resumes, EN and RU.
- [ ] T8.B9 Download both HTML files.

### Scenario C — AI error handling

- [ ] T8.C1 Use invalid model/API key or forced error.
- [ ] T8.C2 Generate fails safely.
- [ ] T8.C3 Error page shows Try again / Change settings.
- [ ] T8.C4 No raw provider error/secrets shown.

### Scenario D — Storage failure handling

- [ ] T8.D1 Simulate unwritable storage dir.
- [ ] T8.D2 Finalize fails safely.
- [ ] T8.D3 No saved resume marked downloadable.
- [ ] T8.D4 Existing valid saved resumes unaffected.

## Acceptance criteria

- [ ] All critical E2E scenarios pass or have documented known limitations.
- [ ] Failures produce structured reports, not blind debugging.

---

# Phase 9 — Cleanup, docs, and final handoff

## Goal

Clean up temporary debug artifacts and prepare final branch handoff.

## Tasks

- [ ] T9.1 Remove accidental debug files from git.
- [ ] T9.2 Ensure `.gitignore` protects debug and generated output.
- [ ] T9.3 Keep dev debug mode available but disabled by default.
- [ ] T9.4 Update README/feature notes with:
  - how to run generation locally;
  - how to enable debug logging;
  - where generated HTML files are stored;
  - PDF is deferred to feat/008.
- [ ] T9.5 Update this backlog with final status.
- [ ] T9.6 Prepare concise PR summary.
- [ ] T9.7 Run final commands:
  - backend tests
  - backend package
  - frontend build
  - Playwright E2E

## Final acceptance criteria for feat/007

- [ ] Vacancy step works.
- [ ] Settings step works.
- [ ] AI model selection works.
- [ ] Generation works with real OpenRouter.
- [ ] Review page works like prototype.
- [ ] Work/Courses/Projects/Skills are shown when generated data exists.
- [ ] Bilingual + All Review layout works.
- [ ] Save edits works.
- [ ] Finalize creates saved HTML.
- [ ] Export displays saved resumes.
- [ ] HTML download works.
- [ ] PDF/public actions remain placeholders.
- [ ] API errors return JSON, not Thymeleaf/Tomcat HTML.
- [ ] Debug logging exists for future investigation and is safe by default.

---

# Appendix A — Handoff template for review

When it is required to review, paste this section with updates.

```md
## Handoff to review

### Current phase

Phase X — ...

### What I changed

- ...

### Commands run

```bash
...
```

### Results

- ...

### Current blocker / question

- ...

### Evidence

- Logs:
- Request payload:
- Response body:
- Files changed:

### My hypothesis

- ...

### Proposed next action

- ...
```

---

# Appendix B — Minimum debug evidence for empty Review tabs

For every investigation of empty tabs, capture:

- [ ] `requestId`
- [ ] `languageMode`
- [ ] `adaptationSelection`
- [ ] model code
- [ ] profile payload section counts
- [ ] prompt output schema excerpt
- [ ] raw AI response section counts
- [ ] parsed variant section counts
- [ ] DB persisted section counts
- [ ] Review DTO section counts
- [ ] frontend adapter section counts
- [ ] UI rendered section counts

Table format:

| Stage | Work | Courses | Projects | Skills | Personal | Evidence |
|---|---:|---:|---:|---:|---:|---|
| Profile payload |  |  |  |  |  |  |
| Prompt schema |  |  |  |  |  |  |
| Raw AI response |  |  |  |  |  |  |
| Parsed JSON |  |  |  |  |  |  |
| Persisted DB rows |  |  |  |  |  |  |
| Review DTO |  |  |  |  |  |  |
| Frontend adapter |  |  |  |  |  |  |
| UI |  |  |  |  |  |  |

---

# Appendix C — Suggested mock vacancy for E2E

```text
Vacancy title: Business Analyst

Vacancy description:
We are looking for a business analyst to gather requirements, model business processes, prepare documentation, analyze stakeholders, support developers, and improve internal digital products. Experience with BPMN, SQL, user stories, acceptance criteria, dashboards, and AI-assisted workflows is valuable.

Company name: MockTech

Company description:
A mock IT company building internal platforms and digital services for business and government workflows.

Additional comments:
Emphasize business analysis, systems thinking, documentation, stakeholder communication, process modeling, dashboard thinking, and AI-assisted workflows. Keep content concise and resume-ready.
```

---

# Appendix D — Out of scope for feat/007

- Real PDF conversion.
- Real public recruiter PDF link.
- Full resume template designer.
- Advanced version history for generated resumes.
- Autosave every keystroke.
- Rich bullet editor if backend does not store bullets separately.
- New profile editing features unrelated to generation.

