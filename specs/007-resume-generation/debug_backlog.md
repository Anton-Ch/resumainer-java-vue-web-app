# debug_backlog.md — feat/007 Resume Generation Debug & Completion Backlog

> Working communication file for OpenCode agent.  
> Current purpose: continue `feat/007-resume-generation` **after AI generation / Review data pipeline stabilization** and finish the remaining MVP flow:
>
> `vacancy → settings → AI generation → Review page → save edits → finalize → saved HTML → HTML download/export`
>
> Real PDF conversion is still out of scope for feat/007 and belongs to feat/008.

---

## 0. Current status — updated 2026-06-16

### MVP debug state

The old blockers around empty Review tabs, stale generation reuse, dynamic prompt contract, sourceId grouping, prompt quality, and Work Experience over-generation are considered **closed for MVP** based on the latest green tests and manual UI smoke tests.

Do **not** reopen old Phase 2–4.9 issues unless there is fresh runtime evidence.

### Current primary focus

**Primary focus now:** finish the post-generation flow:

1. Review edit/save contract.
2. Finalize selected variant(s) into saved HTML.
3. Export page and HTML download.
4. Full E2E pass for feat/007.
5. Cleanup and handoff.

### Hard stop against perfectionism

Prompt iteration for MVP is frozen at **Prompt Config v4** unless a new critical bug appears.

Do not continue prompt polishing just because wording could be nicer. Generated text is editable by the user. For MVP, the accepted standard is:

- valid JSON;
- correct language/adaptation shape;
- no placeholder critical fields;
- source-backed repeatable sections;
- budget rules respected;
- Review UI populated;
- user can edit before finalize.

---

## 0.1 Accepted as working / done

### AI generation and prompt pipeline

- [x] Active DB prompt config v4 created through a versioned Flyway migration.
- [x] Prompt config v4 applies stronger rules for:
  - valid and pure JSON;
  - source-backed facts;
  - strict budget compliance;
  - full-context tailoring;
  - role-relevant skill grouping;
  - bilingual semantic parity;
  - concise cover letters.
- [x] Prompt render log confirms v4 prompt config is used by new generation requests.
- [x] Prompt quality accepted for MVP. No more prompt-tuning unless a real bug appears.

### Dynamic generation contract

- [x] Dynamic JSON contract implemented for selected `languageMode` and `adaptationSelection`.
- [x] Single-language/single-level scenarios tested.
- [x] BILINGUAL + ALL scenario tested.
- [x] Model is instructed to return only requested language roots and adaptation levels.
- [x] Parser/validator flow accepts expected nested contract.
- [x] Placeholder critical values such as `Profession`, `Профессия`, `string`, `N/A` are treated as invalid / guarded.

### SourceId and child grouping

- [x] Source ID rule added to prompts.
- [x] AI is instructed to preserve `sourceId` for repeatable sections:
  - `workExperience`;
  - `courses`;
  - `projects`.
- [x] Parser / persistence / validator work was updated to support `sourceId`.
- [x] Review sparse-card bug is no longer the current blocker.
- [x] Latest UI smoke test shows repeatable sections populated instead of empty/sparse.

### Work Experience budget

- [x] DB-backed Work Experience distribution rules implemented.
- [x] `WorkExperienceBudgetResolver` added and tested.
- [x] Dense profile edge case accepted:
  - EC-016;
  - 16 work records;
  - 26 courses;
  - 11 projects;
  - two-page mode;
  - max 10 workExperience records;
  - Page 1 up to 3;
  - Page 2 up to 7.
- [x] Current job must be included first and marked Page 1.
- [x] Prompt builder injects resolved budget rules.
- [x] `AiResponseValidator` rejects budget violations.
- [x] Controller maps validation failure to a user-actionable error instead of `404 REQUEST_NOT_FOUND`.

### Courses / Projects / Skills budget

- [x] Courses budget respected in latest smoke test: up to 7.
- [x] Projects budget respected in latest smoke test: up to 4.
- [x] Skills are grouped into role-relevant keyword groups.
- [x] Skills quality accepted for MVP.

### Error handling

- [x] AI validation failure marks request as `failed`.
- [x] Invalid AI response does not persist generated responses.
- [x] Validation failure is mapped to:
  - HTTP 422;
  - `AI_RESPONSE_VALIDATION_FAILED`;
  - retry/change-settings style recovery.

---

## 0.2 Latest accepted evidence

Use this as the baseline when continuing.

### Backend tests reported green

The user confirmed all relevant tests passed after the latest backend changes.

Known covered areas:

- Work Experience budget resolver.
- Prompt builder contract.
- Work Experience budget prompt section.
- AI response parser required fields / sourceId / personal info.
- AI response validator.
- AI response validator Work Experience budget.
- Generation service lifecycle.
- Controller validation error mapping.

### Manual UI smoke tests accepted

Latest manual smoke tests confirmed:

- Docker app rebuilt and migrations applied.
- New prompt config v4 used in rendered prompt log.
- BILINGUAL + ALL generation returns populated Review DTO.
- Work Experience count respects budget.
- Courses and Projects respect budget.
- Current job appears first.
- Skills are populated and grouped.
- Prompt output is good enough for MVP.

---

## 0.3 Current next action

Start here.

> **Next phase:** Phase 5 — Review save/update contract and tests.

Do not jump to Finalize until Review save is verified.

Reason: Finalize must use the user-approved edited content, not only the original generated content.

### Latest manual finding — 2026-06-16

A manual Review edit/save test found three concrete Phase 5/6 blockers:

1. `PUT /review` returns `200 OK`, and markers persist for:
   - `generation_response_course`;
   - `generation_response_experience`;
   - `generation_response_project`.
2. Markers do **not** persist for:
   - `generation_response_personal`;
   - `generation_response_skill`.
3. Finalize after an `ENGLISH_ONLY + MINIMAL` request fails with:
   - `400 Bad Request`;
   - `Selected adaptation level not found: BALANCED`.

Current interpretation:

- Review save handler likely does not support or correctly route `personal_information` updates.
- Review save handler likely does not support aggregated `skills` update keys.
- Frontend or backend finalize still defaults to `BALANCED` even when the only generated adaptation is `MINIMAL`.

These are now the first concrete tasks inside Phase 5/6.

---

## 1. Phase checklist

Must update this checklist while working.

- [x] **Phase 0 — Working protocol and safety rules**
- [x] **Phase 1 — Baseline reproduction and debug observability**
- [x] **Phase 2 — AI response pipeline diagnosis for empty Review tabs**
- [x] **Phase 3 — Fix Work/Courses/Projects/Skills Review data population**
- [x] **Phase 4 — Validate Review UI layout for EN/RU and ALL adaptation levels**
- [x] **Phase 4.5 — Stable child record grouping across languages and adaptation levels**
- [x] **Phase 4.8 — Runtime truth / stale generation guard**
- [x] **Phase 4.9 — Dynamic prompt contract and matrix validation**
- [x] **Phase 4.10 — Work Experience budget and AI response validation**
- [x] **Phase 4.11 — Prompt Config v4 quality pass**
- [x] **Phase 5 — Review save/update contract and tests**
- [ ] **Phase 6 — Finalize HTML storage and Docker writable path**
- [ ] **Phase 7 — Export page and HTML download verification**
- [ ] **Phase 8 — Full E2E test pass for feat/007**
- [ ] **Phase 9 — Cleanup, docs, and final handoff**

---

## 2. Operating rules for OpenCode agent

### 2.1 Hard boundaries

- [ ] Do not touch real PDF conversion.
- [ ] Do not remove PDF/public-link placeholder buttons from Export.
- [ ] Do not use fake PDF files as real PDFs.
- [ ] Do not hide backend bugs with frontend mock data.
- [ ] Do not flatten backend DTOs only to match prototype if the current hierarchical DTO works.
- [ ] Do not save edits on every keystroke.
- [ ] Do not log API keys, Authorization headers, session cookies, password hashes, or secrets.
- [ ] Do not commit local debug files with real prompts/responses.
- [ ] Do not reopen prompt work unless there is fresh failing evidence.
- [ ] Do not change budget config schema/data unless the user explicitly approves.
- [ ] Do not continue random fixes after a blocker appears. Stop, document the blocker, then ask for review.
- [ ] Do not change high-risk behavior that may deviate from BA requirements, backend prototype, frontend prototype, or already accepted working behavior without explicit user confirmation.

### 2.2 Required workflow per task

For every task:

1. Inspect current code first.
2. State the observed root cause.
3. Add or update RED test where practical.
4. Make the smallest coherent fix.
5. Run targeted tests.
6. Run build/compile.
7. Update this backlog with evidence.
8. If blocked, stop and write a detailed issue entry.

### 2.3 Evidence standard

Do not mark a phase done without:

- test command;
- test result;
- changed files;
- short explanation of what was proven;
- if UI-related: Playwright/network evidence.

### 2.4 High-risk change confirmation rule

OpenCode must **stop and ask for explicit user confirmation** before implementing any change that can alter accepted BA/product behavior or diverge from the backend/frontend prototypes.

High-risk examples:

- changing the Review page structure, tabs, layout, language columns, or adaptation-level UX;
- changing finalize/export flow behavior beyond the specific bug being fixed;
- changing generated resume data model, table schema, or persisted semantics;
- changing prompt behavior, budget rules, or sourceId rules after they were accepted for MVP;
- removing editable fields instead of fixing save behavior;
- making Skills read-only if the current frontend presents them as editable;
- changing PDF/public-link placeholder behavior;
- changing routing/navigation between Vacancy, Settings, Review, Finalize, and Export;
- silently replacing backend hierarchical DTOs with a new flat contract;
- adding autosave or keystroke-save behavior;
- changing BA requirements from `complete_business_analysis.md`;
- changing behavior copied from the backend or frontend prototype.

Allowed without asking:

- adding tests that document current accepted behavior;
- fixing a narrow bug while preserving the existing UI/contract;
- adding validation/error messages that do not change successful flow behavior;
- improving logs/debug output with secret redaction.

If unsure whether a change is high-risk, stop and ask.

---

## 3. Working log

Append new entries here.

| Date/time | Phase | Task | Status | Evidence / command output | Files changed | Notes / blockers |
|---|---:|---|---|---|---|---|
| 2026-06-13 | 4.4/4.5 | Review after Projects screenshot | Closed | Sparse project cards were traced to unstable child grouping / missing stable source identity. Later sourceId work and smoke tests closed the blocker for MVP. | backend/parser/persistence/review/prompt files | Do not reopen without fresh UI evidence. |
| 2026-06-13 | 4.9 | Dynamic prompt contract and six-scenario matrix | Closed | Six scenarios reported green; prompt contract and parser/validator work accepted. | ResumePromptBuilder, parser/validator tests | Phase closed for MVP. |
| 2026-06-15/16 | 4.10 | Work Experience budget and validator | Closed | Backend tests green; UI smoke test showed EC-016 max 10 workExperience records and current job first. | WorkExperienceBudgetResolver, prompt builder, AiResponseValidator, lifecycle/controller tests | Accepted for MVP. |
| 2026-06-16 | 4.11 | Prompt Config v4 quality pass | Closed | New versioned prompt config v4 applied; smoke test response quality accepted. | V29/V30 prompt config migration depending on actual version number | Freeze prompt polishing for MVP. |
| 2026-06-16 22:30 | 5/6 | Bug 1: Header location from generated personal + Bug 2: Work Experience isFirstPage split | Fixed | **RED→GREEN TDD.** 19 renderer tests + 6 finalize tests + 4 download controller tests pass. Rendered HTML now uses `generation_response_personal.location` in header (fallback to contact). Work Experience split uses `isFirstPage` flag from DB (legacy fallback: first 2/rest when no flag set). Russian/Bilingual labels verified. JaCoCo: Renderer 85%, FinalizeService 84%, DownloadController 73%. | ResumeTemplateRenderer.java, ResumeFinalizeServiceTest.java, ResumeTemplateRendererTest.java, ResumeDownloadControllerTest.java | No changes to bullets, prompt, budget rules, or frontend. |
| YYYY-MM-DD HH:mm | 5 | Review save/update | Not started |  |  | Start here. |

---

## 4. Issue log

Use this only for active or newly discovered blockers.

| ID | Date/time | Phase | Severity | Symptom | Root cause hypothesis | Evidence | Current decision | Next action |
|---|---|---:|---|---|---|---|---|---|
| BUG-007-SAVE-001 |  | 5 | High | Review edits may not persist or may save wrong generated child field | Save/update contract still needs verification after sourceId/updateKey work | Manual test confirms partial save only | Active | Start Phase 5 with tests. |
| BUG-007-SAVE-002 | 2026-06-16 | 5 | High | `generation_response_personal` does not persist edited markers even though `PUT /review` returns 200 | Frontend `buildReviewUpdatePayload` had no handler for `pi:` meta keys — personal info updateKeys were never included in the save payload | Manual DB check after editing all visible Review fields | Fixed 2026-06-16 | Switched to `buildReviewUpdatePayloadSimple` which handles `pi:` keys. |
| BUG-007-SAVE-003 | 2026-06-16 | 5 | High | `generation_response_skill` does not persist edited markers even though `PUT /review` returns 200 | Frontend `buildReviewUpdatePayload` had incomplete skills handler that never added to `fieldUpdates` | Manual DB check after editing skills on Review | Fixed 2026-06-16 | Switched to `buildReviewUpdatePayloadSimple` which handles `sk:` keys. |
| BUG-007-FIN-001 |  | 6 | Unknown/High | Finalize may fail or may not save HTML correctly in Docker | Old backlog suspected generated HTML storage path may be non-writable in Docker | Needs retest after current rebuild | Active, verify after selectedLevel fix | Inspect storage service and run finalize test. |
| BUG-007-FIN-002 | 2026-06-16 | 6 | Critical | `ENGLISH_ONLY + MINIMAL` finalize fails with `Selected adaptation level not found: BALANCED` | Frontend hardcoded `selectedLevel = ref<PrototypeLevel>('Balanced')` — never initialized from actual generated levels. Backend lacked auto-level fallback for single-level scenarios. | Manual request `6997b5b0-788c-4a45-a109-f8e2b26f1b3c` | Fixed 2026-06-16 | Frontend: init selectedLevel from actual generated levels (prefer Balanced, fallback to first). Backend: auto-select single available level when requested level is wrong or missing. |
| BUG-007-EXP-001 |  | 7 | Unknown | Export/HTML download not yet verified after latest generation changes | Export path not yet part of latest smoke test; current export 404 is expected after failed finalize | Missing successful finalize evidence | Active | Complete Phase 7 after finalize succeeds. |

### Archived / resolved issues

| ID | Status | Resolution |
|---|---|---|
| BUG-007-RVW-001 | Closed for MVP | Review tabs now populate when generated data exists. |
| BUG-007-RVW-003 | Closed for MVP | Stable source identity/sourceId work and Review smoke tests removed sparse-card blocker. |
| BUG-007-GEN-005 | Closed for MVP | Fresh generation lifecycle and request status handling tested/accepted. |
| BUG-007-SRC-003 | Closed for MVP | Runtime/source mismatch no longer active after rebuilds/tests. |
| BUG-007-PROMPT-001 | Closed for MVP | Dynamic prompt contract and prompt config v4 accepted. |
| BUG-007-TITLE-001 | Closed for MVP | Placeholder critical-field validation/fallback added/accepted. |
| BUG-007-MATRIX-001 | Closed for MVP | Six scenario matrix reported green. |
| BUG-007-FINALIZE-002 | Closed / superseded | Single-level selectedLevel/finalize default risk belongs to Phase 5/6 verification now. |

---

## 5. Decision log

| ID | Date | Decision | Reason | Status |
|---|---|---|---|---|
| DEC-007-REVIEW-001 | earlier | Keep backend hierarchical Review DTO and use frontend adapter | Avoid risky DTO flattening and preserve domain structure | Active |
| DEC-007-REVIEW-002 | earlier | Backend returns `updateKey` for each editable field; frontend sends updateKey back | Frontend must not guess save keys | Active |
| DEC-007-CHILD-GROUP-001 | 2026-06-13 | Review UI must group child records by stable source identity, not generated child row UUID | Generated child ids differ per language/adaptation | Accepted |
| DEC-007-DYNAMIC-CONTRACT-001 | 2026-06-13 | AI output JSON contract is generated dynamically from settings | Prevent extra/missing language roots or adaptation levels | Accepted |
| DEC-007-NO-PLACEHOLDERS-001 | 2026-06-13 | Generated critical fields must reject placeholder labels | Placeholder output is syntactically valid but semantically broken | Accepted |
| DEC-007-PROMPT-V4-001 | 2026-06-16 | Prompt Config v4 is accepted for MVP | Good enough response quality; avoid endless prompt tuning | Accepted |
| DEC-007-WORK-BUDGET-001 | 2026-06-16 | Work Experience output must follow DB-resolved distribution rules | Prevent AI from returning all jobs for dense profiles | Accepted |
| DEC-007-PDF-001 | earlier | Real PDF conversion remains deferred to feat/008 | feat/007 scope is HTML generation/export and PDF placeholders | Active |
| DEC-007-LOG-001 | earlier | Debug logging must redact secrets | Debug evidence is useful but secrets must never leak | Active |
| DEC-007-HIGH-RISK-CONFIRM-001 | 2026-06-16 | OpenCode must stop and ask for user confirmation before changing high-risk behavior that may diverge from BA requirements or accepted backend/frontend prototypes | Prevent the agent from independently redesigning working flows and drifting away from product requirements | Active |
| DEC-007-SINGLE-LEVEL-FINALIZE-001 | 2026-06-16 | Single-language/single-adaptation requests must finalize the actual generated level, not default BALANCED | EN MINIMAL request has no BALANCED response and currently fails finalize | Active |

---

# Phase 5 — Review save/update contract and tests

## Goal

Make editing and saving robust for all supported Review fields before finalize.

The Review UI must allow user edits, send only changed values, persist them correctly, and reload the edited values.

## Current status

Partially tested manually.

Known current bugs:

- `generation_response_personal` does not persist edited markers after `PUT /review`.
- `generation_response_skill` does not persist edited markers after `PUT /review`.
- `generation_response_course`, `generation_response_experience`, and `generation_response_project` did persist markers in the same manual test.
- `PUT /review` returning success is not enough; tests must verify DB values actually changed.

Start here.

## Backend tasks

- [ ] T5.B0 Inspect current save/update endpoint and DTOs.
- [ ] T5.B1 Verify backend returns `updateKey` for every editable field.
- [ ] T5.B2 Add/verify test: save professional positioning fields.
- [ ] T5.B3 Add/verify test: save work experience description.
- [ ] T5.B4 Add/verify test: save course focus/provider if supported.
- [ ] T5.B5 Add/verify test: save project description if supported.
- [ ] T5.B6 Add/verify test: save skills group/skills if editable; otherwise document skills as read-only for MVP.
- [ ] T5.B7 Add/verify test: save personal info fields if editable.
- [ ] T5.B8 Reject malformed `updateKey`.
- [ ] T5.B9 Reject forbidden field.
- [ ] T5.B10 Reject update target not belonging to request/user.
- [ ] T5.B11 Ensure save does not change `requestId`, `language`, `adaptation`, `sourceId`, or generated child ownership.
- [ ] T5.B12 Ensure update transaction is atomic enough for one save request.
- [ ] T5.B13 Add support/test for `personal_information` update keys:
  - `location`;
  - `spokenLanguages`;
  - `willingnessToRelocate`;
  - `willingnessForBusinessTrips`;
  - `citizenship`;
  - `dateOfBirth`;
  - `workFormats`.
- [ ] T5.B14 Add support/test for `skills:{responseId}:groupName:{level}:{groupIndex}`.
- [ ] T5.B15 Add support/test for `skills:{responseId}:skills:{level}:{groupIndex}`.
- [ ] T5.B16 If Skills cannot be safely updated as grouped CSV, stop and ask the user before making Skills read-only.
- [ ] T5.B17 Add regression test with marker `review_edited` proving personal and skills persist in DB.

## Frontend tasks

- [ ] T5.F0 Inspect current Review save code.
- [ ] T5.F1 Verify frontend stores original value and current value.
- [ ] T5.F2 Verify frontend sends only changed fields.
- [ ] T5.F3 Verify frontend uses backend-provided `updateKey`.
- [ ] T5.F4 Verify frontend does not construct raw DB ids manually.
- [ ] T5.F5 Verify save button state:
  - disabled when no changes;
  - loading while saving;
  - success state after saving;
  - safe error message on failure.
- [ ] T5.F6 Verify dirty state remains if save fails.
- [ ] T5.F7 Verify navigation/finalize prompts user to save pending edits or auto-saves explicitly if current design requires it.
- [ ] T5.F8 Verify frontend includes changed personal information fields in the Review save payload.
- [ ] T5.F9 Verify frontend includes changed skills group/skills fields in the Review save payload.
- [ ] T5.F10 Verify frontend does not report save success if backend rejects some update keys.

## Playwright tasks

- [ ] T5.E1 Generate a fresh Review.
- [ ] T5.E2 Edit professional title.
- [ ] T5.E3 Edit work experience description.
- [ ] T5.E4 Edit course focus or project description.
- [ ] T5.E5 Save.
- [ ] T5.E6 Reload Review.
- [ ] T5.E7 Assert edited values persist.
- [ ] T5.E8 Assert unchanged fields were not sent in save payload if current API supports diff payload.
- [ ] T5.E9 Assert save error does not lose typed edits.
- [ ] T5.E10 Edit personal information fields with marker `review_edited`, save, reload Review, and assert markers persist.
- [ ] T5.E11 Edit skills group/skills fields with marker `review_edited`, save, reload Review, and assert markers persist.
- [ ] T5.E12 Verify DB rows in `generation_response_personal` and `generation_response_skill` contain edited markers.

## Acceptance criteria

- [ ] User edits are not lost.
- [ ] Save payload is minimal and safe.
- [ ] Backend does not trust raw frontend field names.
- [ ] Backend validates `updateKey` ownership.
- [ ] Reloading Review shows saved edits.
- [ ] Finalize will use saved edited values.

---

# Phase 6 — Finalize HTML storage and Docker writable path

## Goal

Finalize selected Review variant(s) into saved HTML files in Docker.

## Current status

Blocked by selected adaptation level bug.

Known current bug:

- `ENGLISH_ONLY + MINIMAL` finalize currently attempts or receives `BALANCED` and fails with `Selected adaptation level not found: BALANCED`.

Old backlog also suspected a possible Docker storage issue around `generated_results/`. Treat storage as **needs verification after selectedLevel is fixed**, not confirmed current bug.

## Tasks

### Inspect current finalize flow

- [ ] T6.1 Inspect finalize controller/service.
- [ ] T6.2 Inspect selected language/adaptation behavior:
  - English only -> one saved resume;
  - Russian only -> one saved resume;
  - Bilingual -> one saved resume per language for selected adaptation.
- [ ] T6.3 Verify finalize uses saved edited Review values, not stale generated originals.
- [ ] T6.4 Verify selectedLevel for single-level requests:
  - MINIMAL finalizes Minimal;
  - BALANCED finalizes Balanced;
  - MAXIMUM finalizes Maximum.
- [ ] T6.4.1 Fix frontend selectedLevel initialization from actual Review DTO/generated levels.
- [ ] T6.4.2 Fix backend finalize to avoid blind BALANCED default.
- [ ] T6.4.3 If selectedLevel is missing and only one generated level exists, backend may use that single level.
- [ ] T6.4.4 If selectedLevel is invalid and multiple levels exist, backend must return clear structured 400 with available levels.
- [ ] T6.4.5 Add regression test for request with only MINIMAL generated response.

### Storage

- [ ] T6.5 Inspect `GeneratedFileStorageService`.
- [ ] T6.6 Verify generated results directory is configurable.
- [ ] T6.7 Verify Docker compose mounts a writable volume/path.
- [ ] T6.8 Verify directory is created before write.
- [ ] T6.9 Verify app user has write permission.
- [ ] T6.10 If write fails, return structured JSON error, not raw stacktrace.
- [ ] T6.11 Do not create saved resume marked downloadable if HTML write failed.
- [ ] T6.12 Clean up orphan files if DB save fails after file write.

## Backend tests

- [ ] T6.B1 `GeneratedFileStorageServiceTest.savesFileToConfiguredDirectory()`
- [ ] T6.B2 `GeneratedFileStorageServiceTest.createsNestedDirectories()`
- [ ] T6.B3 `GeneratedFileStorageServiceTest.failsGracefullyWhenDirectoryNotWritable()`
- [ ] T6.B4 `ResumeFinalizeServiceTest.finalizesEnglishOnlyAsOneSavedResume()`
- [ ] T6.B5 `ResumeFinalizeServiceTest.finalizesBilingualAsTwoSavedResumes()`
- [ ] T6.B6 `ResumeFinalizeServiceTest.usesSavedEditedValues()`
- [ ] T6.B7 `ResumeFinalizeServiceTest.doesNotCreateDownloadableResumeWhenHtmlWriteFails()`
- [ ] T6.B8 Controller test returns structured JSON error for storage failure.
- [ ] T6.B9 Finalize single MINIMAL request does not look for BALANCED.
- [ ] T6.B10 Finalize single MAXIMUM request does not look for BALANCED.
- [ ] T6.B11 Finalize invalid selected level returns available levels in error.

## E2E tests

- [ ] T6.E1 Docker E2E finalize succeeds.
- [ ] T6.E2 Generated HTML file exists inside mounted volume.
- [ ] T6.E3 Export page shows saved resume card after finalize.
- [ ] T6.E4 Finalized HTML contains edited value from Phase 5.
- [ ] T6.E5 ENGLISH_ONLY + MINIMAL finalize reaches Export instead of failing with BALANCED.

## Acceptance criteria

- [ ] Finalize succeeds in Docker.
- [ ] HTML is saved to disk.
- [ ] Saved resume DB row points to stored HTML.
- [ ] Bilingual finalize creates the expected language outputs.
- [ ] Single-level finalize uses the actual generated adaptation level.
- [ ] No real PDF is generated in feat/007.
- [ ] Storage failures are safe and structured.

---

# Phase 7 — Export page and HTML download verification

## Goal

Complete feat/007 export behavior: saved HTML download works; PDF/public actions remain safe placeholders.

## Tasks

- [ ] T7.1 Verify export DTO after successful finalize.
- [ ] T7.2 Verify Export page shows saved resume card(s).
- [ ] T7.3 Verify HTML download endpoint:
  - authenticated;
  - owner-scoped;
  - no raw file path exposed.
- [ ] T7.4 Verify generated HTML content is correct and contains edited values.
- [ ] T7.5 Verify PDF buttons remain visible and placeholder-only.
- [ ] T7.6 Verify public link placeholder does not expose private data.
- [ ] T7.7 Verify copy cover letter works if cover letter generated.
- [ ] T7.8 Verify missing file returns safe error.
- [ ] T7.9 Verify another user cannot download the file.

## Backend tests

- [ ] T7.B1 HTML download owner can download.
- [ ] T7.B2 Other user cannot download.
- [ ] T7.B3 Missing file returns safe JSON error or correct HTTP status.
- [ ] T7.B4 Export DTO includes:
  - `htmlDownloadUrl`;
  - placeholder `pdfDownloadUrl`;
  - placeholder `pdfOpenUrl`;
  - placeholder `publicUrlLink`;
  - `pdfAvailable=false`.

## Frontend / Playwright tests

- [ ] T7.F1 Export page shows saved resume card.
- [ ] T7.F2 Export page shows all placeholder actions.
- [ ] T7.F3 Download HTML returns `.html` file.
- [ ] T7.F4 PDF buttons show placeholder behavior.
- [ ] T7.F5 Public link copy works and remains placeholder/safe.
- [ ] T7.F6 Cover letter copy works if cover letter exists.

## Acceptance criteria

- [ ] Full flow reaches Export after finalize.
- [ ] User can download generated HTML.
- [ ] PDF remains out of scope but UI contract is preserved.
- [ ] User cannot access another user's generated file.

---

# Phase 8 — Full E2E test pass for feat/007

## Goal

Run stable E2E verification for all critical paths.

## Scenario A — English only + Balanced

- [ ] T8.A1 Login.
- [ ] T8.A2 Fill vacancy.
- [ ] T8.A3 Choose English only + Balanced.
- [ ] T8.A4 Generate.
- [ ] T8.A5 Review shows 6 tabs.
- [ ] T8.A6 Edit professional title.
- [ ] T8.A7 Save.
- [ ] T8.A8 Finalize.
- [ ] T8.A9 Export shows one saved resume.
- [ ] T8.A10 Download HTML.

## Scenario B — Bilingual + All

- [ ] T8.B1 Login.
- [ ] T8.B2 Fill vacancy.
- [ ] T8.B3 Choose Bilingual + All.
- [ ] T8.B4 Generate.
- [ ] T8.B5 Review shows EN/RU + Minimal/Balanced/Maximum.
- [ ] T8.B6 Select Balanced.
- [ ] T8.B7 Edit one EN field and one RU field.
- [ ] T8.B8 Save.
- [ ] T8.B9 Finalize.
- [ ] T8.B10 Export shows two saved resumes, EN and RU.
- [ ] T8.B11 Download both HTML files.

## Scenario C — AI validation/provider error handling

- [ ] T8.C1 Force invalid AI response or provider error.
- [ ] T8.C2 Generate fails safely.
- [ ] T8.C3 Request status becomes `failed`.
- [ ] T8.C4 UI shows Error / Try again / Change settings.
- [ ] T8.C5 No old Review content appears.
- [ ] T8.C6 No raw provider error/secrets are shown.

## Scenario D — Storage failure handling

- [ ] T8.D1 Simulate unwritable generated results directory.
- [ ] T8.D2 Finalize fails safely.
- [ ] T8.D3 No saved resume is marked downloadable.
- [ ] T8.D4 Existing valid saved resumes remain unaffected.

## Acceptance criteria

- [ ] All critical E2E scenarios pass or have documented known limitations.
- [ ] Failures produce structured reports, not blind debugging.
- [ ] No secrets appear in logs/dumps.

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
- [ ] T9.7 Run final backend tests.
- [ ] T9.8 Run frontend build.
- [ ] T9.9 Run Playwright E2E.
- [ ] T9.10 Confirm no secrets/debug dumps are committed.

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

# Appendix A — Suggested commands

## Backend targeted tests

```powershell
.\mvnw.cmd test -Dtest=WorkExperienceBudgetResolverTest,ResumePromptBuilderContractTest,ResumePromptBuilderWorkExperienceBudgetPromptTest,AiResponseParserTest,AiResponseValidatorTest,AiResponseValidatorWorkExperienceBudgetTest,ResumeGenerationServiceLifecycleTest,GenerateResumeControllerTest
```

## Full backend tests

```powershell
.\mvnw.cmd test
```

## Docker rebuild

```powershell
docker compose up -d --build --force-recreate app
```

If source/runtime mismatch is suspected:

```powershell
docker compose build --no-cache app
docker compose up -d --force-recreate app
```

## Logs

```powershell
docker compose logs -f app
```

---

# Appendix B — Handoff template for OpenCode agent

When pausing for review, update and paste this section.

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

### Files changed

- ...

### Current blocker / question

- ...

### Evidence

- Logs:
- Request payload:
- Response body:
- Screenshots:
- Test output:

### My hypothesis

- ...

### Proposed next action

- ...
```

---

# Appendix C — Minimum debug evidence for Review / Finalize / Export

For every investigation, capture:

- [ ] `requestId`
- [ ] `languageMode`
- [ ] `adaptationSelection`
- [ ] model code
- [ ] response status
- [ ] generated response row counts
- [ ] DB marker check for `generation_response_personal`
- [ ] DB marker check for `generation_response_skill`
- [ ] Review DTO section counts
- [ ] frontend adapter section counts
- [ ] UI rendered section counts
- [ ] save payload
- [ ] save response
- [ ] finalize payload
- [ ] finalize response
- [ ] saved resume DB rows
- [ ] generated HTML file path without exposing host secrets
- [ ] export DTO
- [ ] HTML download response status

Table format:

| Stage | Professional | Work | Courses | Projects | Skills | Personal | Saved HTML | Evidence |
|---|---:|---:|---:|---:|---:|---:|---:|---|
| Generated response |  |  |  |  |  |  |  |  |
| Review DTO |  |  |  |  |  |  |  |  |
| Frontend adapter |  |  |  |  |  |  |  |  |
| UI |  |  |  |  |  |  |  |  |
| Save payload |  |  |  |  |  |  |  |  |
| Finalize output |  |  |  |  |  |  |  |  |
| Export DTO |  |  |  |  |  |  |  |  |

---

# Appendix D — Suggested smoke vacancy for remaining E2E

```text
Vacancy title: Java Developer

Vacancy description:
We are looking for a Java developer to support a legacy Spring MVC system, improve JDBC-based data access, maintain backend services, write tests, document changes, and collaborate with analysts and stakeholders. Experience with Java, Spring MVC, JDBC, PostgreSQL, REST APIs, Docker, Git, and clear technical documentation is valuable.

Company name: MockTech Resume Systems

Company description:
A small software company maintaining internal business platforms and legacy Java systems.

Additional comments:
Keep the resume honest, concise, and focused on Java backend skills supported by analytical and system design experience. Mention the company name in the cover letter.
```

---

# Appendix E — Out of scope for feat/007

- Real PDF conversion.
- Real public recruiter PDF link.
- Full resume template designer.
- Advanced version history for generated resumes.
- Autosave every keystroke.
- Rich bullet editor if backend does not store bullets separately.
- New profile editing features unrelated to generation.
