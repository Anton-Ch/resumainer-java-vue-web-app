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

**Files changed**:
- `backend/src/main/java/com/resumainer/controller/PublicResumeController.java` — `publicNotFound()` helper
- `backend/src/test/java/com/resumainer/controller/PublicResumeControllerTest.java` — 5 timing tests
