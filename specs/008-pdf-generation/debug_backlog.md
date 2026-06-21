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
