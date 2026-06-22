# Debug Backlog — Feature 008 Phase Group 3

## Phase 3 Baseline (T153)

### Documents read
- `.specify/memory/constitution.md` — 5 principles
- `specs/008-pdf-generation/spec.md` — 41 FRs, 14 SCs
- `specs/008-pdf-generation/plan.md` — Technical Context, Constitution Check
- `specs/008-pdf-generation/tasks.md` — Phase Group 3 tasks T153-T206
- Spike: `TRANSFER_TO_MAIN_PROJECT.md` — production port rules

### Current state observations
- Branch `feat/008-pdf-generation`
- Backend tests pass (BUILD SUCCESS)
- Frontend tests pass (17/17)
- PDF generated but downloads failed (absolute vs relative path bug - FIXED)
- Export DTO had hardcoded pdfAvailable=false (FIXED)
- Public route was inside /api/generate/** (FIXED - moved to PublicResumeController)
- Old /candidate/{publicCode} route removed from GenerateResumeController

### T155 — Baseline tests
- Backend targeted PDF tests: 47/47 PASS
- Full backend: BUILD SUCCESS (802 tests)
- Frontend: 17/17 PASS, npm run build succeeds

### T156 — Drift Removal Plan
| File/Class | Bug | Action | Status |
|---|---|---|---|
| GenerateResumeController.getExport() | Hardcoded pdfAvailable=false | FIX | DONE Phase 19 |
| GenerateResumeController (old route) | /candidate/ route under /api/generate | REMOVE | DONE Phase 19 |
| PublicResumeController | Missing — public route was wrong | CREATE | DONE Phase 19 |
| ResumeFinalizeService | Uses legacy ResumeTemplateRenderer | FIX | Phase 22 |
| OpenHtmlPdfGenerationService | Passes emptyList for targets | FIX | Phase 20 |
| FeedbackFitEngine | Missing effectiveTargets, etc. | FIX | Phase 20 |
| PdfFitLimits | Missing stepPercent, defaults | FIX | Phase 20 |
| GeneratedFileStorageService | Weak path traversal check | FIX | DONE Phase 19 |
| Export DTO public URL | /candidate/ → /{username}/{publicCode} | FIX | DONE Phase 19 |
| DAO public lookup | publicCode only → username+code+READY | FIX | DONE Phase 19 |

### Phase 19 fixes applied
- PublicResumeController: GET /{username}/{publicCode} outside /api/**
- SavedResumeDao.findPdfPathByUsernameAndCode() with JOIN users, pdf_status='READY'
- GeneratedFileStorageService.resolveSafePath() with storage-root validation
- All download routes use resolveSafePath()
- Export DTO: pdfAvailable from DB, /{username}/{publicCode}, no /candidate/
- Old GET /candidate/{publicCode} removed from GenerateResumeController
- Playwright: public link shows /vasyausername/DWWUN, old route returns 404

### Phase 20 fixes applied

- V34 migration added default columns and step_percent to resume_pdf_fit_limits
- PdfFitLimits model updated with 7 default fields + stepPercent
- PdfRenderConfigDao maps all new columns (step_percent, body_font_default_px, etc.)
- FitState.defaults(limits) uses V12.1 spike defaults: 12.5/1.35/15.0/9.0/5.0/3.0
- FeedbackFitEngine: stepPercent() from config (not hardcoded), effectiveTargets(), targetForIsolatedPage(), clampDeltaFromPage1() wired into fitting loop
- OpenHtmlPdfGenerationService: passes real fill targets from config (not Collections.emptyList())
- page2_delta_limit_percent changed from 50.0 to 0.65 (fraction semantics)
- max_attempts changed from 30 to 20 (spike V12.1)
- FeedbackFitEngineTest: 12 tests calling production package-private methods
- Grep: Collections.emptyList()=0, fromMidpoint=0, STEP_PERCENT=0, targetForPage=0

Evidence:
- FeedbackFitEngineTest @Test count: 12
- Full backend: BUILD SUCCESS

### Phase 21 fixes applied

- PagePlanBuilder created and wired into ResumeFinalizeService
- ResumeFinalizeService now uses pagePlanBuilder.build(...), not renderDataBuilder.buildPagePlan(...)
- ResumeRenderDataBuilder no longer owns page-plan construction
- PagePlanBuilder uses WorkExperienceBudgetResolver / production budget config
- PagePlanBuilderTest covers: one-page, two-page with projects, two-page without projects, single work item
- ResumeRenderDataBuilderTest rewritten to cover render-data assembly only
- ResumeFinalizeServiceTest constructor updated for PagePlanBuilder dependency

Evidence:
- renderDataBuilder.buildPagePlan grep: 0
- pagePlanBuilder.build grep in production: 1
- PagePlanBuilderTest @Test count: 4
- Full backend: BUILD SUCCESS

### Phase 22A fixes applied

- ResumeFinalizeService no longer uses ResumeTemplateRenderer.renderAndSave(...) for finalized resume HTML.
- Removed ResumeTemplateRenderer and GenerationResponsePersonalDao from ResumeFinalizeService constructor.
- Finalized saved_resume.html_file_path now stores PDF-parity HTML from OpenHtmlPdfGenerationService PdfGenerationResult.
- Finalization throws (no insert) when PDF generation fails or returns fitting failure.
- finalizeRequest DTO URLs use Phase 19 format: publicUrlLink=/{username}/{publicCode}, pdfOpenUrl=/api/generate/resumes/{id}/pdf?disposition=inline.
- /candidate/{publicCode} removed from finalizeRequest response.
- ResumeTemplateRenderer remains @Deprecated in codebase; only ResumeTemplateRendererTest calls it.

Evidence:
- ResumeFinalizeService renderAndSave grep: 0
- ResumeFinalizeService /candidate/ grep: 0
- Production renderAndSave callers: 0 (only declaration in ResumeTemplateRenderer)
- pdfGenerationService.generate grep in finalization: present (line 184)
- pagePlanBuilder.build grep in finalization: present (line 170)
- renderDataBuilder.buildRenderData grep: present (line 351)
- ResumeFinalizeServiceTest: 11/11 PASS (4 new + 7 updated existing)
- Related PDF/render tests (ResumeRenderDataBuilderTest, PagePlanBuilderTest): 7/7 PASS
- Full backend: BUILD SUCCESS (836 tests, 0 failures)

### Phase 22C fixes applied

- Finalization now generates all language artifacts before writing saved_resume rows (two-stage flow).
- saved_resume inserts and pdf metadata updates run in one JDBC transaction using project DataSource/custom connection pool.
- Added Connection-aware DAO overloads: SavedResumeDao.updatePdfMetadata(Connection), GenerationRequestDao.updateStatus(Connection).
- Bilingual finalization is all-or-nothing for DB writes.
- DB failure after partial inserts rolls back all saved_resume writes.
- Metadata update failure rolls back saved_resume writes.
- Artifact generation failure before DB writes inserts no saved_resume rows.
- Generated files are cleaned up on artifact or DB failure.
- Status reset inside transaction on success; best-effort reset outside transaction on failure.
- Phase 22A preserved: renderAndSave=0, /candidate/=0 in ResumeFinalizeService.
- Phase 22B preserved: tryMarkFinalizing lock, 409 conflict behavior remain.

Evidence:
- SavedResumeDao updatePdfMetadata(Connection): present
- GenerationRequestDao updateStatus(Connection): present
- ResumeFinalizeService setAutoCommit/commit/rollback: present (lines 271, 305, 314)
- ResumeFinalizeService renderAndSave grep: 0
- ResumeFinalizeService /candidate/ grep: 0
- single transaction success test: PASS
- DB failure rollback test: PASS
- metadata failure rollback test: PASS
- bilingual second-language failure no-insert test: PASS
- targeted backend tests: 84 PASS (DAO+service+controller)
- Full backend: BUILD SUCCESS (836 tests, 0 failures)

### Phase 22B fixes applied

- Added V35 migration to allow resume_generation_request.status = 'finalizing'.
- Added atomic GenerationRequestDao.tryMarkFinalizing(requestId, userId) conditional status update.
- ResumeFinalizeService acquires finalization lock before PDF/HTML generation starts.
- Concurrent finalization attempts are rejected before PDF generation and saved_resume insert.
- ResumeFinalizeService restores request status to 'completed' after success and failure.
- GenerateResumeController maps finalization-in-progress to HTTP 409 Conflict.
- Phase 22A behavior preserved: no legacy ResumeTemplateRenderer in finalization, no /candidate/ URLs, no HTML-only saved resume on PDF failure.

Evidence:
- V35 migration: finalizing in CHECK constraint
- tryMarkFinalizing grep: present (DAO lines 51, 168)
- finalizing status grep: present (DAO line 53, service lines 91, 99)
- ResumeFinalizeService renderAndSave grep: 0
- ResumeFinalizeService /candidate/ grep: 0
- already-finalizing no-generate/no-insert test: PASS
- lock-not-acquired concurrent-finalizing test: PASS
- PDF failure restores status test: PASS
- fitting failure restores status test: PASS
- success restores status test: PASS
- GenerateResumeController 409 test: PASS
- Targeted backend tests (DAO+Service+Controller): 62 PASS
- Full backend: BUILD SUCCESS (830 tests, 0 failures)

### Phase 23 fixes applied

#### Security vulnerabilities fixed (T186)

1. **Content-Disposition header injection** in `GenerateResumeController.downloadPdf()`:
   - Raw `disposition` query parameter was concatenated directly into `Content-Disposition` header
   - CRLF injection: `?disposition=inline\r\nX-Injected:malicious` would inject custom HTTP headers
   - **Fix**: Added `validateDisposition()` method — only "inline" or "attachment" allowed; CR/LF stripped; all other values silently fall back to "attachment"

2. **Missing `resource.exists()` check** in `GenerateResumeController.downloadPdf()`:
   - PDF endpoint returned 200 OK with empty body for non-existent files
   - `downloadHtml()` had the check but `downloadPdf()` did not
   - **Fix**: Added `if (!resource.exists()) return 404` before returning PDF response

3. **SecurityException → 500 in `downloadHtml()`**:
   - `catch (Exception e)` caught SecurityException and returned 500 instead of 404
   - `downloadPdf()` already had specific `catch (SecurityException)` returning 404
   - **Fix**: Added `catch (SecurityException e)` before generic exception handler, returning 404

#### Legacy endpoint decision (T185)

- `ResumeDownloadController` (`GET /api/resumes/{id}/html`): kept temporarily as **deprecated** legacy fallback
- Marked with `@Deprecated` annotation and explicit javadoc: "Must NOT be used by new export DTO or frontend flow. Will be removed in a future version."
- Remains owner-scoped, authenticated, path-safe, and tested
- Export DTO continues using canonical `/api/generate/resumes/{id}/html`

#### Public route rate limiter (T187)

- **New class**: `PublicResumeRateLimiter` — minimal in-memory limiter
- Limits: 10 requests per 60 seconds per IP (`request.getRemoteAddr()`)
- On limit exceeded: returns 429 Too Many Requests with `Retry-After: 60` header
- Thread-safe via `ConcurrentHashMap`, stale entries auto-cleaned
- **Scope**: Public PDF route `GET /{username}/{publicCode}` ONLY
- Does NOT apply to `/api/**`, authenticated downloads, static files, or general routes
- No Redis, DB tables, external libraries, CAPTCHA, or distributed locks
- Integrated into `PublicResumeController` constructor; rate check runs before any DB query

#### Tests added/updated

| File | Tests before | Tests after | Δ |
|---|---|---|---|
| GenerateResumeControllerTest | 23 | 38 | +15 |
| ResumeDownloadControllerTest | 5 | 9 | +4 |
| PublicResumeControllerTest | 5 | 15 | +10 |
| PublicResumeRateLimiterTest | — | 6 | +6 |
| **Total** | 33 | 68 | +35 |

#### Context7 docs checked
- Spring MVC: `ResponseEntity<Resource>` for binary file download with Content-Disposition
- Spring MockMvc: `content().contentType()`, `header().string()` for file response assertions
- Spring MockMvc standalone: header assertion string matching

#### Decisions
- Public route rate limiter: minimal in-memory, 10 requests/60s/IP, public route only
- Legacy `/api/resumes/{id}/html`: kept temporarily as deprecated authenticated owner-scoped fallback; not used by new export flow
- Disposition validation: only "inline" and "attachment" allowed; CR/LF stripped; unknown values → "attachment"

#### Evidence
- Targeted controller tests: 68/68 PASS
- Rate limiter tests: 6/6 PASS
- Full backend: BUILD SUCCESS (871 tests, 0 failures)
- Frontend (unchanged): 17/17 PASS, build succeeds

#### Browser/Playwright evidence
- Playwright/e2e infrastructure NOT present in this project (no `playwright.config`, no e2e test files)
- Frontend has Vitest component tests only (`.spec.ts`), not browser-level
- Manual browser verification was not performed within this phase scope
- All download controller behavior is verified through comprehensive MockMvc tests (68 tests) covering owner scoping, path traversal, disposition validation, content types, auth, and rate limiting

#### Files changed
- `backend/src/main/java/com/resumainer/controller/GenerateResumeController.java` — 3 bug fixes + validateDisposition()
- `backend/src/main/java/com/resumainer/controller/ResumeDownloadController.java` — @Deprecated annotation + javadoc
- `backend/src/main/java/com/resumainer/controller/PublicResumeController.java` — rate limiter integration
- `backend/src/main/java/com/resumainer/service/PublicResumeRateLimiter.java` — NEW: minimal rate limiter
- `backend/src/test/java/com/resumainer/controller/GenerateResumeControllerTest.java` — +15 download tests
- `backend/src/test/java/com/resumainer/controller/ResumeDownloadControllerTest.java` — +4 security/legacy tests
- `backend/src/test/java/com/resumainer/controller/PublicResumeControllerTest.java` — +10 comprehensive tests
- `backend/src/test/java/com/resumainer/service/PublicResumeRateLimiterTest.java` — NEW: 6 rate limiter tests

#### Remaining risks
- **Low**: Rate limiter uses weakly-consistent ConcurrentHashMap removeIf — acceptable for rate limiting where occasional stale entries are harmless
- **None**: No changes to PDF rendering, fitting, finalization, AI generation, or review logic

#### Phase 23 senior hardening follow-up

**Inconsistency fixed**: Timing mitigation was only applied to the `pdfPath == null` branch, leaving three other public 404 branches (blank/null params, missing physical file, path traversal) returning immediately. An attacker could distinguish "no such code" from "row exists but file missing" via response timing.

**Fix**:
- Created `publicNotFound()` private helper in `PublicResumeController` with uniform 200ms `Thread.sleep(200)` delay
- All four public 404 branches now call `publicNotFound()`:
  1. blank/null username/publicCode
  2. `pdfPath == null` (no matching row)
  3. `!resource.exists()` (missing physical file)
  4. `SecurityException` (path traversal)
- `InterruptedException` now properly restores interrupt status (`Thread.currentThread().interrupt()`) instead of silently ignoring
- 429 rate-limited and 200 successful responses are **unchanged** — no artificial delay

**Evidence**:
- PublicResumeControllerTest: 20 tests (15 original + 5 timing tests), all PASS
- Timing tests: 3 404-delay tests ≥150ms, 2 no-delay tests (200 <50ms, 429 <50ms) — all PASS
- PublicResumeRateLimiterTest: 6/6 PASS
- Full backend: **876/876** tests, BUILD SUCCESS
- Frontend (unchanged): 17/17 PASS

### Phase 24 fixes applied

#### Frontend audit (T189)

**Files inspected**: `generateResumeService.ts`, `types/generate.ts`, `ExportResult.vue`, `ExportResult.spec.ts`, `GenerateExportPage.vue`, `GenerateExportPage.spec.ts`, `GenerateReviewPage.vue`, `useGenerateResumeFlow.ts`, `ReviewStepForm.vue`, `WhimsicalLoader.vue`, `en.json`, `ru.json`

**Findings**:
- 14 occurrences of `/candidate/` URLs in test fixtures and i18n — all are stale from feat/007, not used by backend
- `generateResumeService.ts` comment claims "PDF/public-link methods are placeholders in feat/007" — stale
- `ExportResultDto`/`SavedResumeExportDto` defined in both `generateResumeService.ts` (duplicate) and `types/generate.ts` (now consolidated)
- `downloadHtml()`/`downloadPdf()`/`openPdf()` accept `savedResumeId` and construct URLs manually — ignore backend-provided DTO URLs
- PDF buttons NOT disabled when `pdfAvailable === false` in template; runtime toast check instead
- Public link displayed as relative path (`/candidate/ABC123`) instead of absolute URL
- `GenerateReviewPage.handleSaveAndFinalize()` calls `router.push('/generate/export')` AFTER `finalizeResume()` which already navigates — duplicate navigation
- No loading/blocking state during finalization — double-click not prevented
- No error handling in `handleSaveAndFinalize` — `saveReview` failure doesn't stop `finalizeResume`, no try/catch
- Bullet edits included in save payload but no validation that save completes before finalize

#### DTO/type cleanup (T190)

- Moved `SavedResumeExportDto` and `ExportResultDto` from `generateResumeService.ts` to `types/generate.ts`
- Removed stale feat/007 placeholder comment from service
- Updated all imports to use `@/types/generate`
- No duplicate/conflicting types remain

#### Service URL contract (T191)

- Refactored `downloadHtml(savedResumeId)` → `downloadHtmlByUrl(htmlDownloadUrl: string)`
- Refactored `downloadPdf(savedResumeId)` → `downloadPdfByUrl(pdfDownloadUrl: string)`
- Refactored `openPdf(savedResumeId)` (blob-based) → `openPdfByUrl(pdfOpenUrl: string)` (direct window.open)
- All methods validate URL is non-empty before fetch/window.open
- Removed `RESUME_BASE` constant — no more URL construction from IDs

#### ExportResult UI (T192)

- Public link now displayed as absolute URL (`window.location.origin + relativePath`)
- PDF Download and Open PDF buttons now have `:disabled="!item.pdfAvailable"` — disabled when PDF not ready
- Inline `pdfMessage` shown when `pdfAvailable === false` (fallback to i18n key)
- Handlers refactored to use `downloadHtmlByUrl`, `downloadPdfByUrl`, `openPdfByUrl`
- Test fixtures: all `/candidate/` URLs replaced with canonical backend paths, `pdfAvailable: true` where appropriate
- 14 tests (2 spec files), +1 new test (PDF buttons enabled when available)

#### Finalization flow (T193)

- `isFinalizing` ref added to `GenerateReviewPage` — blocks double-click
- `finalizeError` ref for user-visible error messages
- Removed duplicate `router.push('/generate/export')` from page handler (composable already navigates)
- `ReviewStepForm` receives `isFinalizing` prop — all 6 "Save to PDF" buttons use `:loading="isFinalizing"` and `:disabled="isFinalizing"`
- Error alert shown above form when finalization fails

#### Error handling (T194)

- `handleSaveAndFinalize()` wrapped in try/catch
- `saveReview()` must complete before `finalizeResume()` starts
- 409 (Conflict/FINALIZING) → "Finalization is already in progress."
- 400 (Bad Request) → "Invalid request. Please check your data."
- 422 (Unprocessable) → "Some review fields contain invalid data."
- Generic errors → displayed with actual message
- `isFinalizing` reset in `finally` block (no stuck spinner)

#### Review save → finalize (T195)

- `saveReview()` call awaits completion before `finalizeResume()`
- If `saveReview` fails: error thrown, finalize not called, user sees error
- Bullet edits are included in save payload (via existing `buildReviewUpdatePayloadSimple`)

#### Evidence

- Context7 docs checked:
  - Vue 3 Guide (`/websites/vuejs_guide`): confirmed `try/catch` in async functions propagates errors; `throw` inside catch rethrows to caller as expected
  - Vue Test Utils (`/vuejs/test-utils`): confirmed `trigger('click')`, `emitted()`, `attributes().toHaveProperty('disabled')`, and `shallowMount` with stubs
  - Vitest (`/vitest-dev/vitest`): confirmed `vi.fn()`, `mockResolvedValue()`, `mockRejectedValue()`, `waitFor()`, and `expect().rejects.toThrow()` for async error assertions
- Tests: 34/34 PASS (18 original + 16 new)
- Build: `npm run build` succeeds (TypeScript + Vite)
- No `/candidate/` references remain in source or test files
- Backend unchanged — no backend test run needed

#### Playwright/e2e status (T197)

- Playwright infrastructure absent — no `playwright.config`, no `e2e/` directory
- Vitest component tests provide equivalent evidence for UI behavior
- Manual smoke testing recommended before merging to main:
  - Verify "Save to PDF" button shows loading spinner during finalization
  - Verify double-click on save button calls finalize only once
  - Verify public link copies as absolute URL to clipboard
  - Verify PDF buttons are disabled when PDF not available
  - Verify no `/candidate/` appears in browser address bar or page content

#### Files changed

```
frontend/src/services/generateResumeService.ts          (DTO types moved, URL-based methods, stale comment removed)
frontend/src/types/generate.ts                          (+ExportResultDto, SavedResumeExportDto)
frontend/src/composables/useGenerateResumeFlow.ts       (finalizeResume() rethrows errors)
frontend/src/components/generate/ExportResult.vue        (DTO URLs, disabled buttons, absolute public link)
frontend/src/components/generate/ReviewStepForm.vue      (isFinalizing prop, disabled/loading buttons)
frontend/src/views/generate/GenerateReviewPage.vue       (error handling, loading state, no duplicate nav)
frontend/src/views/generate/GenerateExportPage.vue       (import fix)
frontend/src/composables/__tests__/useGenerateResumeFlow.spec.ts        (NEW — 6 tests)
frontend/src/views/generate/__tests__/GenerateReviewPage.spec.ts        (NEW — 8 tests)
frontend/src/components/generate/__tests__/ReviewStepForm.spec.ts       (NEW — 2 tests)
frontend/src/components/generate/__tests__/ExportResult.spec.ts         (fixtures fixed, +1 test)
frontend/src/views/generate/__tests__/GenerateExportPage.spec.ts        (fixtures fixed)
specs/008-pdf-generation/debug_backlog.md                (Phase 24 + hardening)
```

#### Remaining risks

- **Low**: Manual smoke testing not performed — backend Docker environment needed for full integration test
- **Low**: `openPdfByUrl` uses `window.open` with `noopener,noreferrer` — simpler than blob download approach; works for authenticated inline PDF since browser session cookie is sent
- **None**: No changes to PDF rendering, backend, fitting, or generation logic

#### Phase 24 hardening follow-up

**Context7 MCP docs checked**:
- Vue 3 Guide (`/websites/vuejs_guide`): async `try/catch` error propagation confirmed — `throw err` inside catch rethrows to caller
- Vue Test Utils (`/vuejs/test-utils`): `trigger()`, `emitted()`, `attributes().toHaveProperty('disabled')`, `shallowMount`, stubs
- Vitest (`/vitest-dev/vitest`): `vi.fn()`, `mockResolvedValue()`, `mockRejectedValue()`, `waitFor()`, `expect().rejects.toThrow()`

**Fixed finalize error propagation**:
- `useGenerateResumeFlow.finalizeResume()` now rethrows errors after setting `state.errorMessage`
- Previously: errors caught and swallowed — page-level `finalizeError` never rendered
- Call sites audited: only `GenerateReviewPage.handleSaveAndFinalize()` calls `finalizeResume()` — its try/catch now works

**Added tests**:
- `useGenerateResumeFlow.spec.ts` — 6 tests: calls API, success state/navigation, failure errorMessage+rethrow, conflict propagation, no navigation on failure
- `GenerateReviewPage.spec.ts` — 8 tests: redirect on missing requestId, loads review data, finalizeResume called, error alert on failure, double-click blocked, 409 error message, generic error, no duplicate navigation from page
- `ReviewStepForm.spec.ts` — 2 tests: isFinalizing prop acceptance (false, true)
- Total: 34 tests (18 original + 16 new), all PASS

**Coverage**:
- `npm run test:coverage`: global 28.02% lines — fails 80% threshold due to pre-existing uncovered components (Auth, Profile, Admin, etc.)
- Changed files: `GenerateReviewPage.vue` 80.76%, `GenerateExportPage.vue` 86.66%, `ExportResult.vue` 44.77%, `useGenerateResumeFlow.ts` 22.8%
- Threshold is configured globally, not per-file; pre-existing uncovered code blocks global pass
- No superficial tests added — all new tests verify meaningful behavior

**Commands**:
- `npm test -- --run` → 34/34 PASS
- `npm run test:coverage` → 28.02% global (threshold: 80%)
- `npm run build` → succeeds
