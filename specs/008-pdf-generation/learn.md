# What I Learned: PDF/HTML Resume Export and Bullet-Point Review Hardening

**Feature**: Production PDF generation from approved spike + structured bullet persistence and review editing
**Generated**: 2026-06-23
**Scope**: Full feature (Phase Group 1 + 2 + 3)
**Implementation status**: 206/206 tasks completed

---

## Key Decisions

### 1. Port From a Spike Instead of Building an Engine

**What we did**: We took an existing standalone spike (V12.1) that already produced correct PDF outputs for 17 edge case scenarios, and adapted its classes into the production backend instead of writing a PDF engine from scratch.

**Why**: PDF layout fitting is layout-sensitive, edge-case-heavy work. The spike had already solved all the hard problems: hyphenation handling, Cyrillic font support, page break detection, adaptive fill targets. Rewriting from scratch would reintroduce every bug the spike had already fixed. The spike's V12.1 had 17 passing edge case scenarios (ec01–ec17) in both English and Russian — that's 34 validated PDF outputs. Building a new engine and reaching that same confidence would take weeks of iteration.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Build a new renderer from scratch | Guaranteed to reintroduce every bug the spike already fixed; would need weeks of tuning to match 34 validated edge cases |
| Use wkhtmltopdf as external process | Requires Qt/WebKit in Docker image, complex process management, non-Java dependency; Cyrillic rendering issues in older versions |
| Use iText library | AGPL license forces open-sourcing the entire project; overkill for resume PDFs |

**When you'd choose differently**: If no proven spike existed, or if the spike was poorly structured. The key insight is: **validate the spike first, then port**. We learned this the hard way — early port attempts drifted from spike behavior, causing weeks of rework in Phase Group 3. Next time: audit class-by-class before merging any ported code.

---

### 2. Feedback-Driven PDF Fitting Instead of One-Shot Rendering

**What we did**: The PDF engine doesn't try to get the layout right on the first attempt. It renders, analyzes, validates, detects problems, then adjusts parameters (font size, line height, spacing gaps) and re-renders — up to 30 times.

**Why**: A one-shot renderer would need to predict exactly how much space each section would take, which is impossible without knowing exact text metrics (Cyrillic is wider than Latin, bullet lists vary in length). The feedback loop removes the prediction problem: render → measure → adjust → repeat. Each attempt adjusts one parameter at a time in round-robin order (font → line-height → section-gap → item-gap → paragraph-gap → bullet-gap), shrinking or growing until the PDF fits within configured fill targets.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| CSS `page-break` with overflow prediction | Unreliable across different PDF engines; doesn't handle text clipping detection |
| Fixed template per page count (1-page vs 2-page) | Can't handle 30+ edge cases; every resume has different text length |
| Binary search on font size | Easier but less flexible; only adjusts font, ignores line-height and gaps which have bigger visual impact |

**When you'd choose differently**: For a document format with completely predictable content (e.g., a fixed-form certificate), one-shot rendering would be simpler and faster. The feedback loop shines when input length varies unpredictably.

---

### 3. JDBC-Level Transactions With Compensation (No @Transactional)

**What we did**: Finalization manages its own JDBC connection: `getConnection()` → `setAutoCommit(false)` → render → validate → `commit()` → promote files. On failure: `rollback()`, delete staged files, reset request status. All catches `Exception`, not just `SQLException`.

**Why**: Pure Spring MVC doesn't have `@Transactional` (that's Spring Boot's AOP-based transaction management). Even if it did, a database rollback can't undo files already written to disk. The finalization flow has two resources (database + filesystem) that can't be in the same transaction. The compensation pattern explicitly handles "DB rolled back but files exist" and "files deleted but DB not yet rolled back" — both are cleanup paths that must run independently.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Spring `@Transactional` with AOP | Not available in pure Spring MVC; requires Spring Boot auto-configuration |
| Write files first, then DB | Creates orphan files on DB failure, requiring cleanup sweep |
| DB first, then files | Leaves saved_resume rows pointing to non-existent files on I/O failure |

**When you'd choose differently**: If your storage was also transactional (e.g., files in the database as BLOBs, or an object store with atomic put), the two-phase approach becomes unnecessary. Also, if using Spring Boot, `@Transactional` simplifies the DB half significantly — you'd only need manual compensation for the filesystem half.

---

### 4. Opaque Update Keys for Review Editing

**What we did**: When the frontend sends edited bullet text back, it uses an opaque string like `work_experience:<UUID>:bulletPoints:BALANCED:3`. The frontend never constructs these — it receives them from the backend's review DTO and sends them back unchanged. The backend parses the key, validates the section against an allowlist, maps the field name to a DB column via a per-section column map, and updates only that field.

**Why**: The naive approach — frontend sends `{ section: "work_experience", recordId: "...", field: "bullet_text", value: "..." }` — looks cleaner but introduces tight coupling. If the backend renames a column, every frontend build breaks. If the frontend sends a field name that doesn't exist, the backend has no way to reject it safely. Opaque keys make the backend the sole authority over data structure.

**When you'd choose differently**: For a GraphQL API where the frontend explicitly requests fields, or for a simple CRUD app where fields rarely change. Opaque keys add complexity that's worth it when multiple sections need different validation rules (as in resume review: 6 sections with different field types).

---

### 5. Footer Safe Zone Detection via PDF Text Extraction

**What we did**: Instead of guessing CSS safe zones, the system reads the generated PDF's actual text positions. `PdfAnalyzer.PositionStripper` identifies text rows within the bottom 18mm of each page and flags when more than 1 row occupies that zone while a "See the next page" footer note is present. This `FOOTER_OVERLAP` signal feeds back into the fitting engine.

**Why**: The footer note ("SEE THE NEXT PAGE" / "СМ. СЛЕДУЮЩУЮ СТРАНИЦУ") was visually overlapping the body content on Page 1, especially in Russian where the text is ~40% longer. CSS padding adjustments alone (14mm → 20mm) weren't enough because different resumes need different safe zones depending on content density. By measuring actual text positions in the rendered PDF, the system can detect overlap precisely and respond with targeted shrinking — only when needed, not preemptively.

**When you'd choose differently**: If your PDF content is fully predictable (same sections, same approximate length every time), a static CSS padding approach is simpler and sufficient. The text-extraction approach is necessary when content varies widely — which is exactly the case with AI-generated resumes.

---

## Concepts to Know

### Feedback Loop Fitting Algorithm

**What it is**: An iterative optimization algorithm that renders a document, measures it against quality criteria (page count, text presence, fill ratio), and adjusts layout parameters to improve the result. Each adjustment is a small delta to one parameter at a time, cycling through parameters in round-robin order: font size → line height → section gap → item gap → paragraph gap → bullet gap. If shrinking doesn't help, it tries growing. If the result gets worse, it reverts the last change.

**Where we used it**: `FeedbackFitEngine.java` — the core fitting loop calls `nextStateForPage()` to determine the next parameter adjustment based on the validation result. Shrink `PAGE1:MISSING_TEXTS` → reduce spacing. Grow `PAGE1:LOW_FILL` → increase font size.

**Why it matters**: Without this algorithm, fitting is a guessing game. You'd either ship oversized PDFs or clip content. The feedback loop converges on the "sweet spot" where all validation criteria pass, and it does so deterministically (same input → same output every time).

---

### HTML-to-PDF Rendering Pipeline

**What it is**: A sequence of transformations: structured data → XHTML template → HTML string → PDF bytes → analyzed metrics → validation result. Each stage has a dedicated class with a single responsibility. The pipeline is: `ResumeRenderDataBuilder` → `PagePlanBuilder` → `XhtmlTemplateRenderer` → `OpenHtmlPdfRenderer` → `PdfAnalyzer` → `PdfValidationService` → `FeedbackFitEngine` (loop) → `PdfBlankPageCleaner` → final PDF.

**Where we used it**: All 11 classes in `backend/src/main/java/com/resumainer/service/pdf/`. Each class does exactly one transformation: the `XhtmlTemplateRenderer` builds HTML strings, `OpenHtmlPdfRenderer` calls the rendering library, `PdfAnalyzer` extracts text positions.

**Why it matters**: The pipeline pattern makes each stage independently testable. The `XhtmlTemplateRenderer` tests verify HTML output without needing a PDF renderer. The `PdfValidatorService` tests work with mock metrics. When a PDF fails, the logs tell you exactly which stage rejected it: "PAGE1:MISSING_TEXTS" means validation, "PAGE2:LOW_FILL" means fill target not met. You fix the stage, not the whole pipeline.

---

### File + Database Compensation Pattern

**What it is**: A transaction pattern for operations that touch two non-transactional resources (filesystem + database). The pattern is: ① start DB transaction, ② write files to staging directory, ③ validate files, ④ commit DB, ⑤ promote files from staging to final. On any failure before commit: delete staged files + rollback DB. On failure after commit: log the inconsistency (rare, but possible with filesystem errors).

**Where we used it**: `ResumeFinalizeService.finalize()` — the entire bilingual finalization flow follows this pattern. Both language renderings happen in staging. Both must succeed. If one fails, both staged directories are deleted and DB is rolled back.

**Why it matters**: Without this pattern, a crash halfway through finalization leaves orphan files and/or partial DB rows. The user sees a "Finalization failed" error but their next attempt might fail because a stale `FINALIZING` status blocks it. The compensation pattern guarantees that failure leaves no trace — the system is in exactly the same state as before the attempt.

---

### CSS Limitations in PDF Rendering

**What it is**: OpenHTMLToPDF (the library that converts HTML to PDF) does NOT support modern CSS. No flexbox, no grid, no `overflow: hidden`, no reliable `break-inside: avoid`. It supports CSS 2.1 plus basic CSS 3 features. Layout must use `display: table`, `float`, and explicit positioning. Font loading uses `@font-face` with TTF files, not WOFF2.

**Where we used it**: `CssSafetyInspector.java` explicitly rejects flexbox/grid tokens. `XhtmlTemplateRenderer.java` uses `display: table-cell` for the contact info row layout, `div`-based sections, explicit `height: 297mm` for page sizing.

**Why it matters**: If you add `display: flex` to the PDF template, OpenHTMLToPDF silently ignores it — the layout breaks with no error message. The `CssSafetyInspector` prevents this by rejecting forbidden CSS at render time, surfacing the problem as a test failure rather than a broken PDF in production.

---

## Architecture Overview

Feature 008 adds two independent pipelines that converge at finalization:

```
Bullet Pipeline (Phase Group 1):
  AI Response → AiResponseParser → bulletPoints[]
    → AiResponseValidator → GenerationResponsePersistenceService → bullet DB tables
    → ResumeReviewService (updateKey-based edit) → frontend Review UI
    → (edited bullets flow into render pipeline below)

PDF Pipeline (Phase Group 2 + 3):
  finalized response + profile data
    → ResumeRenderDataBuilder → ResumeRenderData (immutable input)
    → PagePlanBuilder → PagePlan (page 1/2 allocation)
    → XhtmlTemplateRenderer → XHTML string
    → OpenHtmlPdfRenderer → PDF bytes
    → PdfAnalyzer → PdfMetrics (page count, text positions, fill ratios)
    → PdfValidationService → validation result
    → FeedbackFitEngine (loop: adjust → render → analyze → validate)
    → PdfBlankPageCleaner → final PDF
    → SavedResumeDao (commit with PDF metadata)
```

Key design properties:
- **Each class does one thing**: 11 pipeline classes, each with a single responsibility
- **Immutable data between stages**: `ResumeRenderData`, `PagePlan`, `PdfMetrics` are immutable records
- **Fitting is a loop, not a one-shot**: externalized from the pipeline into `FeedbackFitEngine`
- **Validation is multi-criteria**: page count AND text presence AND fill ratios AND footer safe zone
- **Compensation wraps it all**: file staging + DB transaction + rollback both on failure

---

## Glossary

| Term | Meaning |
|------|---------|
| **Spike** | A standalone prototype used to validate a technical approach before production implementation. The V12.1 spike proved OpenHTMLToPDF could produce correct Russian/English PDFs across 17 edge cases |
| **Fit engine / fitting** | The iterative algorithm that adjusts layout parameters (font size, gaps) until the PDF passes validation. Not about "fitting text into a box" — it's an optimization loop |
| **Fill target** | Required minimum/maximum percentage of a PDF page that should be covered by text. Page 1 might need 60-95% fill, page 2 might accept 30-95% (lower minimum because it's shorter) |
| **Update key** | An opaque backend-generated string that encodes `section:recordId:field:adaptationCode[:groupIdx]`. The frontend receives it from the review DTO and sends it back unchanged on save — it never constructs update keys |
| **Compensation** | Manual cleanup code that undoes partial work when a multi-step operation fails. Runs when a database rollback can't undo filesystem changes, or vice versa |
| **Atomic bilingual finalization** | When generating both English and Russian versions, either both succeed or neither is saved. A failure in one language triggers rollback of both |
| **STS / autoCommit** | JDBC's default mode where each statement is an independent transaction. For multi-step operations, you disable autoCommit (setAutoCommit(false)), do all work, then commit or rollback. Pure Spring MVC requires manual management of this lifecycle |
