# TRANSFER_TO_MAIN_PROJECT.md — DeepSeek/OpenCode Handoff

This document explains what to port from this standalone PDF spike into the real ResumAIner capstone project and what must stay spike-only.

## Goal

Port the proven V12 PDF generation approach into the main Java backend without dragging mock/test harness code into production.

The production flow should be:

1. User finalizes approved generated resume data.
2. Backend builds a production `ResumePdfData` / `ResumeRenderData` object from real saved resume data.
3. Backend resolves the render/budget rule.
4. Backend renders page-isolated XHTML pages.
5. OpenHTMLToPDF renders each planned page.
6. PDFBox validates page count, selectable text, fill, and expected critical content.
7. PDFBox merges page PDFs into final saved resume PDF.
8. Backend persists PDF metadata/status/path.

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
- model records related to rendering/fitting:
  - `ResumeData` or a renamed production equivalent such as `ResumePdfData`
  - `PagePlan`
  - `FitLimits`
  - `FitState`
  - `FillTarget`
  - `FitAttempt`
  - `FitResult`
  - `PdfMetrics`

Port the idea of `BudgetResolver`, but replace its spike data source.

## Spike-only code NOT to port

Do not port these as production components:

- `dao/ScenarioDao.java`
- `model/ResumeDataFactory.java`
- `model/MockCandidate.java`
- `model/Scenario.java`
- standalone `App.java` runner entrypoint
- edge scenario batch runner behavior from `SpikeRunner.java`
- synthetic generated strings from `ResumeDataFactory`
- SQLite `mock_*` tables
- SQLite `edge_case_rule` table as-is

These exist only to prove the PDF algorithm against deterministic edge cases.

## DB tables to port conceptually

Port these concepts into the main app DB if equivalent configuration does not already exist:

### PDF fit limits

Spike table:

- `pdf_fit_limits`

Production equivalent may be named:

- `pdf_render_fit_config`

It controls:

- max attempts;
- step percent;
- page2/page3 delta limit against page1;
- body font min/default/max;
- line-height min/default/max;
- section/item/paragraph/bullet gap min/default/max.

### Fill targets

Spike table:

- `pdf_fill_targets`

Production equivalent may be named:

- `pdf_page_fill_target`

It controls per-page min/max fill targets.

Important: V12 also applies adaptive page 2 min-fill in code:

- page 2 with 0 projects -> min-fill can go down to `0.30`;
- page 2 with 1 project -> min-fill can go down to `0.44`;
- page 2 with 2+ projects -> default configured min-fill.

You may keep this in code or move these adaptive thresholds into DB. If moved into DB, document the rule clearly.

## DB tables NOT to port

Do not port these tables to the production capstone schema:

- `edge_case_rule`
- `mock_candidate`
- `mock_scenario`

They are spike-only. They are not user data. They are not saved resume data. They are not production render metadata.

## Breakpoints / edge-case rules in production

The spike uses `edge_case_rule` to decide page budget and split behavior for deterministic test fixtures.

Production still needs equivalent breakpoint logic, but it must come from real production configuration, not the spike mock tables.

The production resolver must decide at least:

- target page count: 1 or 2 by default;
- exceptional 3-page fallback only if product rules allow it;
- how many work experience items go to page 1;
- how many work experience items go to page 2;
- whether projects exist and should appear before additional work on page 2;
- how many projects/courses are allowed by the selected resume generation/adaptation level.

If the main project already has `resume_budget_config` / budget resolver tables from feature `007-resume-generation`, use those as the source of truth. Do not create duplicate spike tables.

If no production budget config exists, create a production config table with explicit business semantics. Do not call it `edge_case_rule`, because the production table is not a test matrix.

## PDF / HTML parity rule

V12 intentionally renders the same page notes in PDF and HTML:

- page 1 footer: `See the next page` / `См. следующую страницу`;
- page 2+ header: `See the previous page` / `См. предыдущую страницу`.

The two notes must stay visually consistent: same font size, weight, uppercase treatment, letter spacing, background, horizontal margins, and border style; only the border side/top-vs-bottom placement differs.

Use an explicit A4 page height (`height:297mm` with `min-height:297mm`) for PDF pages. This is intentional: OpenHTMLToPDF can fail to place an absolute `bottom` footer reliably when the containing page only has `min-height`.

The PDF is produced by page-isolated rendering and PDFBox merge. The HTML artifact should match the final PDF layout rules. Do not reintroduce separate browser-only HTML fitting.

## Debug attempts rule

`debugAttempts=true`:

- keep every attempt HTML/PDF under `output-edge/debug-attempts/...`.

`debugAttempts=false`:

- use a temp directory for intermediate attempts;
- delete temp attempts after final PDF assembly;
- do not leave attempt files in production output.

In the production backend, debug attempts should normally be disabled and should never leak to public download URLs.

## Safety rules to preserve

Keep these safeguards:

- PDF-safe CSS only;
- no `overflow:hidden` clipping;
- no browser-only flex/row-gap based fitting;
- selectable PDF text validation;
- required critical text validation;
- generic generated anchors for long generated paragraphs;
- soft-hyphen/dash normalization for PDF text extraction quirks;
- blank page cleanup/check;
- page-isolated fitting;
- consistent global font size across pages;
- page2/page3 line-height and section-gap delta limit against page1.

## Do not change without confirmation

Before changing these in the main project, stop and ask the user:

- PDF page count policy;
- 3-page fallback policy;
- render budget/breakpoint rules;
- public PDF URL behavior;
- PDF/HTML parity behavior;
- font minimums;
- content truncation or redistribution rules.
