# ResumAIner PDF Spike V12.1 — Housekeeping + PDF/HTML Parity Notes Fix

Standalone Java 21 Maven spike for `feat/008-pdf-generation`.

V12.1 is a small notes-fix housekeeping version based on V12. It keeps the V11 fitting algorithm working, adds clearer comments for DeepSeek/OpenCode, restores page navigation notes in both PDF and HTML, implements the page2/page3 delta limit, and fixes the debug-attempts flag behavior.

## What V12.1 includes

- PDF/HTML parity for page notes:
  - page 1 footer: `See the next page` / `См. следующую страницу`;
  - page 2+ header: `See the previous page` / `См. предыдущую страницу`.
- Notes are absolutely positioned and reserved through page classes:
  - `has-next` reserves bottom space;
  - `has-prev` reserves top space.
- A4 pages use explicit `height:297mm` plus `min-height:297mm` so the absolute bottom footer note is reliably positioned in OpenHTMLToPDF.
- Isolated page rendering keeps notes visible and only disables `page-break-after`.
- `page2_delta_limit_percent` is now enforced for page2/page3 line-height and section-gap changes relative to page1.
- `debugAttempts=false` now writes intermediate attempts to a temp directory and deletes them after final PDF assembly.
- SQL comments mark spike-only tables clearly.
- Java comments mark spike-only DAOs/data factories clearly.
- Added `TRANSFER_TO_MAIN_PROJECT.md` for DeepSeek/OpenCode handoff.
- 3-page fallback page 3 now renders aspirations and personal info instead of placeholder overflow text.
- Optional personal/aspiration rendering is safer: blank optional sections are skipped.

## Production code to port

Port the concept and most logic from:

- `render/XhtmlTemplateRenderer.java`
- `pdf/OpenHtmlPdfRenderer.java`
- `pdf/PdfAnalyzer.java`
- `pdf/PdfValidationService.java`
- `pdf/ContentExpectationBuilder.java`
- `pdf/CssSafetyInspector.java`
- `pdf/FeedbackFitEngine.java`
- `pdf/PdfBlankPageCleaner.java`
- `pdf/PdfPageMerger.java`
- `plan/PagePlanBuilder.java`
- render/fitting model records:
  - `ResumeData` or a renamed production equivalent;
  - `PagePlan`;
  - `FitLimits`;
  - `FitState`;
  - `FillTarget`;
  - `FitAttempt`;
  - `FitResult`;
  - `PdfMetrics`.

Port the idea of `BudgetResolver`, but wire it to production budget/config data, not spike mock tables.

## Spike-only code not to port

Do not port these as production components:

- `dao/ScenarioDao.java`
- `model/ResumeDataFactory.java`
- `model/MockCandidate.java`
- `model/Scenario.java`
- standalone batch runner behavior from `SpikeRunner.java`
- synthetic text fixtures
- mock SQLite seed data

These are only for proving the algorithm.

## DB tables to port

Port conceptually, preferably with production names:

- `pdf_fit_limits` -> production render fit config;
- `pdf_fill_targets` -> production page fill targets.

## DB tables not to port

Do not port:

- `edge_case_rule`
- `mock_candidate`
- `mock_scenario`

They are marked as `SPIKE_ONLY_DO_NOT_PORT` in `schema.sql`.

## Breakpoint / budget source

The spike uses `edge_case_rule` as a deterministic edge-case matrix. The real capstone app must use production budget/config rules instead.

If the main project already has `resume_budget_config` or equivalent from `feat/007-resume-generation`, use that as source of truth for:

- target pages;
- page1/page2 work item split;
- project/course count handling;
- exceptional 3-page fallback policy.

Do not duplicate spike mock tables in production.

## Run

```powershell
Remove-Item -Recurse -Force output-edge, work -ErrorAction SilentlyContinue
mvn test
mvn -q exec:java -Dexec.args="--mode=edge --out=output-edge --db=work/pdf-spike-edge.sqlite --debug-attempts=true"
```

Without debug attempts:

```powershell
mvn -q exec:java -Dexec.args="--mode=edge --out=output-edge --db=work/pdf-spike-edge.sqlite --debug-attempts=false"
```

## Output

```text
output-edge/html/
output-edge/pdf/
output-edge/report/pdf-spike-report.md
output-edge/report/pdf-spike-report.json
output-edge/logs/pdf-spike.log
output-edge/debug-attempts/   # only when --debug-attempts=true
work/pdf-spike-edge.sqlite
```

## Important design notes

This is still a spike, not production code. Keep it working for verification, but follow `TRANSFER_TO_MAIN_PROJECT.md` when porting into the real backend.

V12 intentionally avoids full section redistribution. If a future case cannot fit, implement redistribution only after explicit product confirmation.
