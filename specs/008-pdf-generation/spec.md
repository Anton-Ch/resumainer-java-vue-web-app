# Feature Specification: PDF/HTML Resume Export and Bullet-Point Review Hardening

**Feature Branch**: `feat/008-pdf-generation`

**Created**: 2026-06-20

**Status**: Draft *(brainstormed 2026-06-20)*

**Input**: Implement production PDF/HTML resume generation for ResumAIner using the approved standalone HTML-to-PDF spike. Also complete the prerequisite bullet-point review and persistence work needed for reliable resume rendering. The rendering engine MUST be ported from the approved spike instead of being invented from scratch.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Review Generated Bullet Points Before Finalization (Priority: P1)

As a logged-in user, I want generated work experience and project bullet points to appear as separate editable items during review so that I can correct each bullet before final PDF/HTML rendering.

**Why this priority**: The final PDF layout depends on bullet points being structured, countable, editable, and validated. A single free-text blob is not reliable enough for strict one/two-page rendering.

**Independent Test**: Generate a resume with work experience and projects, open Review, edit one bullet point, save, reload Review, and verify the edited bullet persists and is used in final HTML/PDF.

**Acceptance Scenarios**:

1. **Given** the AI response contains structured bullet points for work experience, **When** the response is processed, **Then** each bullet point is stored as an individual ordered item.
2. **Given** the AI response contains structured bullet points for projects, **When** the response is processed, **Then** each project bullet point is stored separately and linked to the generated project item.
3. **Given** I open the Review page, **When** I inspect work experience or projects, **Then** bullet points are displayed as separate editable fields under the correct record.
4. **Given** I edit a bullet point and save, **When** I reload Review, **Then** the edited bullet text is preserved.
5. **Given** I finalize the resume, **When** PDF/HTML is generated, **Then** the final artifact uses the edited bullet text, not stale AI-generated text.
6. **Given** a bullet point is empty or whitespace-only, **When** I save review or finalize, **Then** the system rejects it with a user-readable error.
7. **Given** this is MVP, **Then** the Review UI supports editing existing bullets only. It does not need add/delete/reorder bullet actions unless explicitly approved.

---

### User Story 2 — Generate a Strict 1-Page or 2-Page PDF (Priority: P1)

As a logged-in user, I want the system to generate a professional PDF resume that fits the selected one-page or two-page structure so that I can send it to employers without manual formatting.

**Why this priority**: PDF is the practical resume artifact. The whole AI generation flow is incomplete unless the final output is stable, printable, selectable, and not clipped.

**Independent Test**: Finalize multiple edge-case resumes and verify each generated PDF has the expected page count, selectable text, no missing required content, no clipping, no blank extra page, and correct page notes.

**Acceptance Scenarios**:

1. **Given** a scenario selected for a one-page resume, **When** finalization succeeds, **Then** the generated PDF has exactly one page.
2. **Given** a scenario selected for a two-page resume, **When** finalization succeeds, **Then** the generated PDF has exactly two pages.
3. **Given** a two-page PDF, **Then** page 1 includes the footer note "SEE THE NEXT PAGE" (English) / "СМ. СЛЕДУЮЩУЮ СТРАНИЦУ" (Russian), and page 2 includes the header note "SEE THE PREVIOUS PAGE" / "СМ. ПРЕДЫДУЩУЮ СТРАНИЦУ".
4. **Given** the PDF is generated, **Then** all visible resume text is selectable/extractable text, not an image-only rendering.
5. **Given** a generated PDF would overflow, **When** fitting runs, **Then** the system uses a deterministic feedback loop to adjust font size, line height, and gaps within configured limits.
6. **Given** content cannot fit within the configured limits, **When** fitting fails, **Then** the system shows the message "Resume could not be generated. Please try again and reduce the longest texts in your resume fields." and provides a "Try again" button that returns to the Review page with all edits preserved and the generation request status reset to allow re-finalization.
7. **Given** the PDF renderer produces a trailing blank page, **Then** the automated cleanup logic removes it before validation.

---

### User Story 3 — Download Matching HTML Artifact (Priority: P1)

As a logged-in user, I want Download HTML to return the same final resume layout as the PDF so that the HTML artifact is useful and matches the PDF output.

**Why this priority**: The application already has HTML download. After this feature, HTML must not be a stale legacy renderer while PDF uses a new renderer.

**Independent Test**: Finalize a two-page resume, download both PDF and HTML, and verify that section order, page split, and page notes match.

**Acceptance Scenarios**:

1. **Given** finalization succeeds, **When** I click Download HTML, **Then** I receive the new PDF-parity HTML artifact generated by the same rendering pipeline that produced the PDF.
2. **Given** the PDF is two pages, **When** I open the downloaded HTML, **Then** it shows the same page split and the same page navigation notes as the PDF.
3. **Given** the old HTML renderer still exists, **Then** it is marked as legacy/deprecated and is not used by the finalization flow.
4. **Given** future rollback or comparison is needed, **Then** the old renderer code remains available but unused.

---

### User Story 4 — Preserve Existing Budget and Page-Break Rules (Priority: P1)

As a developer, I want the PDF engine to use the existing production budget configuration and work-experience resolver so that the page split follows the same rules already created for resume generation.

**Why this priority**: The rendering engine uses configuration tables for edge-case testing, but the application already has production budget configuration. Duplicating or conflicting sources of truth must be avoided.

**Independent Test**: Verify finalization uses existing production budget configuration and resolver, not test-only or mock configuration tables.

**Acceptance Scenarios**:

1. **Given** the application has active budget configuration, **When** a resume is finalized, **Then** the system loads that production configuration and uses it to decide target page count and page split.
2. **Given** the rendering engine reference implementation contains mock/test tables, **Then** these are never introduced into production database migrations.
3. **Given** no active budget configuration exists, **When** finalization is attempted, **Then** the system fails with a user-readable error and logs enough diagnostics for debugging.
4. **Given** a work/project/course count edge case, **When** the resolver runs, **Then** the result is deterministic and traceable through logs.

---

### User Story 5 — Export PDF from Authenticated and Public Routes (Priority: P2)

As a logged-in user, I want Download PDF and Open PDF to work after finalization. As a recruiter with a public link, I want to open the public PDF without logging in.

**Why this priority**: Authenticated PDF download is required for MVP. Public recruiter access is valuable but can be treated as P2 if time is tight.

**Independent Test**: Finalize a resume, download the PDF as owner, then open the public route without authentication and verify it serves only the PDF for an active saved resume.

**Acceptance Scenarios**:

1. **Given** I own a saved resume, **When** I click Download PDF, **Then** I receive the generated PDF file.
2. **Given** I own a saved resume, **When** I click Open PDF, **Then** the PDF opens inline in the browser.
3. **Given** another authenticated user tries to download my private resume PDF by ID, **Then** access is denied.
4. **Given** an unauthenticated recruiter opens the public URL, **Then** the route serves the PDF inline if the saved resume is active and public.
5. **Given** the saved resume is deleted, disabled, or the public code is invalid, **Then** the public route returns a 404 error and does not leak metadata.
6. **Given** a public PDF is served, **Then** it never includes the cover letter and never serves private HTML.

---

### Edge Cases

- AI response contains a work or project item with missing or empty bullet points.
- AI response uses old description-only structure instead of structured bullets.
- User edits a bullet to an empty string.
- User finalizes without saving current Review edits.
- Single-language (EN-only/RU-only) selected level is Minimal or Maximum but old code defaults to Balanced.
- Bilingual finalization succeeds for one language but PDF generation fails for the other language.
- PDF renderer creates a blank trailing page that must be detected and removed.
- Two-page sparse resume has low page 2 fill but correct content — must not falsely fail validation.
- Dense Russian resume has long text and risks clipping the last personal info line.
- Generated HTML writes successfully but PDF fails — system must clean up both.
- PDF writes successfully but database insert fails — system must clean up files.
- User downloads HTML/PDF after soft-delete — must be denied.
- Public route is guessed or brute-forced — must not leak information.
- Font files are missing or not resolvable at runtime — must fail gracefully.
- User clicks Finalize while finalization is already in progress — must show status message, not start duplicate work.
- User-edited bullet text contains HTML tags, script, or CSS markup — must be HTML-escaped before template insertion.
- PDF storage directory is not writable or runs out of disk space — must fail gracefully with cleanup.
- Generation request status is stuck in FINALIZING due to crash — must be recoverable on next valid action.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Phase Group 1 — Bullet Points, Review, Prompt, and Parser Hardening

- **FR-008-001**: The system MUST store work experience bullet points as structured ordered bullet items, not only as a single description blob.
- **FR-008-002**: The system MUST store project bullet points as structured ordered bullet items, not only as a single description blob.
- **FR-008-003**: The system MUST inspect the existing schema before adding bullet-point tables. If suitable bullet tables already exist, reuse or update them; do not duplicate.
- **FR-008-004**: Work and project bullet point text MUST be non-empty after trimming.
- **FR-008-005**: Bullet point order MUST be deterministic and preserved from AI response through Review save and final rendering.
- **FR-008-006**: The AI prompt MUST request structured bullet points for work experience and projects.
- **FR-008-007**: The AI prompt MUST limit bullet points to practical resume-sized content: target maximum 15 words per bullet and maximum 250 characters per bullet.
- **FR-008-008**: The system MUST reject invalid or missing required bullet points where generated work or project entries require bullets.
- **FR-008-009**: The Review interface MUST display bullet points as separate editable fields grouped under their parent record.
- **FR-008-010**: The frontend Review page MUST allow editing existing bullet points and saving changes.
- **FR-008-011**: MVP frontend Review MUST NOT add, delete, or reorder bullet points unless explicitly approved.
- **FR-008-012**: Finalization MUST use the edited bullet values, not stale originally-generated values.
- **FR-008-013**: Existing generated description fields may remain for paragraphs, but structured bullets MUST be first-class render inputs.

#### Phase Group 2 — PDF/HTML Generation

- **FR-008-014**: The system MUST implement real PDF generation using an HTML-to-PDF rendering engine based on the approved rendering pipeline reference implementation.
- **FR-008-015**: The system MUST port or adapt the approved rendering engine instead of inventing a new renderer or fitting algorithm.
- **FR-008-016**: The generated PDF MUST be exactly 1 page or 2 pages for MVP-selected scenarios. A 3-page fallback is exceptional and allowed only by explicit product configuration.
- **FR-008-017**: The generated PDF MUST contain selectable text and must not be image-only.
- **FR-008-018**: The generated PDF MUST validate page count, required content presence, missing text detection, trailing blank page removal, and basic fill constraints before the saved resume is committed.
- **FR-008-019**: The generated HTML artifact MUST match the final PDF layout, including page split and page navigation notes.
- **FR-008-020**: The Download HTML action MUST deliver the new PDF-parity HTML file, not the legacy renderer output.
- **FR-008-021**: The legacy HTML renderer MUST remain in the codebase but be clearly marked deprecated and not used in the new finalization path.
- **FR-008-022**: The PDF/HTML renderer MUST include page navigation notes consistently:
  - Page 1 footer: "SEE THE NEXT PAGE" (English) / "СМ. СЛЕДУЮЩУЮ СТРАНИЦУ" (Russian)
  - Page 2+ header: "SEE THE PREVIOUS PAGE" (English) / "СМ. ПРЕДЫДУЩУЮ СТРАНИЦУ" (Russian)
- **FR-008-023**: The renderer MUST use PDF-safe styling. Browser-only styling primitives MUST NOT be used in PDF templates.
- **FR-008-023-1**: All user-editable text that enters the HTML-to-PDF rendering pipeline (bullet points, personal info lines, any Review-editable fields) MUST be HTML-escaped before template insertion to prevent markup injection. No user-controlled HTML formatting is supported in MVP.
- **FR-008-024**: The system MUST use existing production budget configuration and resolver for content distribution and page splitting.
- **FR-008-025**: The system MUST NOT introduce rendering-engine-only mock or test tables into production database migrations.
- **FR-008-026**: The system MUST add production fitting configuration tables or an equivalent production configuration source for fit limits and fill targets.
- **FR-008-027**: The system MUST store PDF status and metadata on saved resumes or equivalent persisted records.
- **FR-008-028**: If PDF/HTML finalization fails, the system MUST roll back database changes and clean up generated files to avoid partial saved resumes and orphan artifacts.
- **FR-008-028-1**: On fitting failure, the system MUST display the message: "Resume could not be generated. Please try again and reduce the longest texts in your resume fields." and provide a "Try again" action that returns the user to the Review page with all previously saved edits preserved and the generation request status reset to a state that permits re-finalization.
- **FR-008-028-2**: The system MUST prevent concurrent finalization by setting the generation request status to FINALIZING before starting PDF/HTML generation. While in this status, any additional finalization attempts MUST return a user-readable message that finalization is already in progress without starting duplicate work. On completion (success or failure), the status MUST be reset to unblock further actions.
- **FR-008-028-3**: The frontend MUST display a loading state during finalization that reuses the existing AI-generation wait screen pattern, with randomly rotating status phrases adapted for the finalization context (e.g., "Generating your resume PDF...", "Optimizing page layout...", "Preparing final files..."). The loading state MUST be shown from the moment Finalize is clicked until the result (success or failure) is returned.
- **FR-008-029**: Bilingual finalization MUST be atomic. If one language fails PDF/HTML generation, neither language is saved as finalized.
- **FR-008-030**: Authenticated PDF download MUST be owner-scoped.
- **FR-008-031**: The public PDF route MUST not expose private HTML, cover letter text, raw file paths, or deleted/disabled resumes.

#### Logging, Testing, and Quality

- **FR-008-032**: Logging MUST be detailed enough to diagnose fitting attempts, selected configurations, page plans, PDF validation failures, file writes, and rollback decisions.
- **FR-008-033**: Logs MUST NOT include secrets, API keys, raw full prompts, full personal data dumps, or raw filesystem paths in any user-facing response.
- **FR-008-034**: Backend and frontend implementation MUST follow test-driven development for all business-critical logic.
- **FR-008-035**: New and modified feature code MUST target useful automated test coverage, not superficial coverage-only tests.
- **FR-008-036**: Code MUST follow KISS principles: simple, readable, layered, professional, and aligned with existing project conventions. Do not over-engineer.
- **FR-008-037**: All work MUST comply with the active project constitution before coding, after design, and before final handoff.

### Key Entities *(include if feature involves data)*

- **Work Experience Bullet Point**: Ordered bullet point linked to one generated work experience item. Stores bullet text, order, optional edit marker, and parent experience reference.
- **Project Bullet Point**: Ordered bullet point linked to one generated project item.
- **Resume Render Data**: Immutable data structure assembled from finalized generated response, profile-owned data, and existing budget resolver results. Used as input to the rendering pipeline.
- **PDF Page Plan**: Page allocation result for page 1, page 2, and page 3 fallback. Backed by production budget resolver.
- **PDF Fit Limits**: Production configuration for minimum/maximum font size, line height, spacing gaps, maximum fitting attempts, and page-to-page delta limits.
- **PDF Fill Target**: Production configuration for minimum/maximum fill percentage per target page, language, and content context.
- **PDF Storage**: Generated PDF and parity HTML files stored on the filesystem in a configurable directory. Metadata (status, file path, page count, timestamps) tracked in the database via saved resume record. Files cleaned up on rollback via explicit compensation logic.
- **Saved Resume PDF Metadata**: PDF status, file path (relative, never exposed raw), generated timestamp, page count, render configuration used, and failure information stored on the saved resume record.
- **Legacy HTML Renderer**: Existing HTML renderer retained for future fallback or reference but not used for this feature's finalization path.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-008-001**: All Phase Group 1 bullet-point persistence and review tests pass.
- **SC-008-002**: Work and project bullets can be edited in Review and appear in final PDF/HTML output.
- **SC-008-003**: Existing generation flows continue to work: single-language (EN-only, RU-only), bilingual, and all adaptation levels.
- **SC-008-004**: All approved rendering pipeline edge scenarios pass after porting or equivalent production tests are created.
- **SC-008-005**: Generated PDFs have correct 1-page or 2-page counts, no silent content clipping, no blank trailing pages, and fully selectable text.
- **SC-008-006**: Generated HTML matches generated PDF for page split, content order, and page navigation notes.
- **SC-008-007**: Authenticated Download PDF works for the owner and is denied for non-owners.
- **SC-008-008**: Download HTML uses the new PDF-parity HTML output.
- **SC-008-009**: Public PDF route returns inline PDF for active public resumes and returns a 404 error for invalid, deleted, or disabled ones.
- **SC-008-010**: No production database migration includes rendering-engine-only mock or test tables.
- **SC-008-011**: Automated backend build and test suite passes without errors.
- **SC-008-012**: Automated frontend build passes without errors.
- **SC-008-013**: New and modified feature code targets useful automated test coverage.
- **SC-008-014**: Implementation follows KISS principles and the active project constitution.

---

## Constitution Alignment

This feature MUST comply with the ResumAIner Constitution principles:

| Principle | Impact on this feature |
|---|---|
| **I. Code Quality & Maintainability** | Ported rendering classes must follow layered architecture. KISS required — no unnecessary abstractions. Legacy renderer remains but is deprecated and excluded from new flow. |
| **II. Testing Excellence** | TDD required for bullet persistence, parser, validator, review saving, PDF fitting, PDF validation, finalization rollback, and download endpoints. Useful test coverage, not superficial. |
| **III. User Experience Consistency** | Review UI supports bullet editing. Export actions work. HTML/PDF layout parity. EN/RU internationalization for all user-facing text including page notes. No hardcoded UI strings. |
| **IV. Performance & Reliability** | PreparedStatement for all SQL. JDBC transactions with compensation for file + database finalization. PDF fitting attempts bounded by configuration. Detailed diagnostic logs. UTF-8 throughout. |
| **V. Security by Design** | Owner-scoped downloads. Public route serves only PDF for active public resumes. No raw file paths, no secrets, no personal data dumps in logs. Invalid or deleted public links return 404. |

**Technology Constraint Check** (per Constitution Technology Stack):
- [x] Server-side HTML-to-PDF generation (no frontend PDF rendering)
- [x] PostgreSQL with database migrations for new tables
- [x] Plain JDBC — no ORM, JPA, or Hibernate
- [x] Spring MVC — no Spring Boot
- [x] Internationalization via resource files for both English and Russian
- [x] Dual validation: frontend for UX + backend as authoritative security boundary
- [x] Logging safe: no secrets, API keys, or personal data beyond diagnostic minimum

---

## Assumptions

- The approved rendering pipeline reference implementation is available for inspection and porting.
- The reference implementation includes source code, example output, logs, and reference data for verification.
- The existing project already contains production budget configuration tables and a work experience budget resolver. Implementation must inspect and reuse these before adding anything new.
- Font files (Inter for body text, Manrope for headings) are ported from the spike into backend static resources for PDF rendering use only. Both are OFL-licensed. Existing web fonts are not affected.
- The current saved resume model may already include some PDF or public-link fields. Inspect before adding database migrations. Add only missing fields.
- The legacy HTML renderer exists and must remain available but unused by the new finalization path.
- Resume generation via AI is already implemented. This feature focuses on bullet hardening plus final render and export.
- Users have stable internet connectivity for PDF download.

---

## Deferred / Non-Goals

- Multiple visual resume templates beyond the current approved PDF/HTML layout.
- User-selectable templates.
- User-controlled add, delete, or reorder of bullet points in Review MVP.
- Browser-based PDF generation.
- DOCX export.
- Advanced visual regression infrastructure beyond initial render, text extraction, and page-count validation, unless time allows.
- Full section redistribution beyond approved rendering pipeline behavior, unless fitting genuinely fails and user or product approval is given.

---

## Clarifications

### Session 2026-06-20

- **Q: Should the PDF engine be built from scratch or ported from the reference implementation?** → **A:** Port or copy from the approved rendering pipeline wherever possible. Do not invent a new engine.
- **Q: Should reference implementation mock tables be ported?** → **A:** No. Mock and test tables are reference-only. Use production budget configuration and resolver from the main project.
- **Q: Should HTML match PDF?** → **A:** Yes. Download HTML must use the new PDF-parity renderer and match PDF page split and navigation notes.
- **Q: Should the old HTML renderer be deleted?** → **A:** No. Keep it as deprecated reference or fallback, but do not use it in the new finalization path.
- **Q: What testing discipline is required?** → **A:** Test-driven development, useful tests, targeted coverage for new and modified feature code. Tests must verify real behavior, not just coverage numbers.
- **Q: What coding style should be used?** → **A:** KISS: simple, professional, readable, minimal cleverness, no unnecessary abstractions.
- **Q: Where should generated PDF/HTML files be stored?** → **A:** Files on filesystem (configurable storage directory), metadata (status, path, page count, timestamps) in database via saved resume record. Compensation logic cleans up files on DB rollback.
- **Q: Which fonts should the PDF renderer use?** → **A:** Port Inter (body) and Manrope (headings) from the spike for PDF rendering only. These fonts are isolated to the PDF pipeline and do not affect existing web fonts. Both fonts are OFL-licensed and free to use.
- **Q: What happens when fitting engine exhausts all attempts?** → **A:** Show error message: "Resume could not be generated. Please try again and reduce the longest texts in your resume fields." Provide a "Try again" button that returns the user to the Review page, preserves all previously entered edits, and resets the generation request status to a state that allows re-finalization without deadlocking the request.
- **Q: Should user-edited bullet text be HTML-escaped before PDF rendering?** → **A:** Yes. All user-edited bullet text and any user-modifiable text that goes into the HTML-to-PDF template MUST be HTML-escaped. No formatting markup is supported in bullets for MVP.
- **Q: How to handle concurrent finalization (double-click Finalize)?** → **A:** Use a FINALIZING status on the generation request. While in this status, any further finalization attempts return a message "Finalization is already in progress." Once complete (success or failure), the status is reset/unblocked. The UI MUST show a loading state similar to the existing AI generation wait screen, with randomly rotating status phrases adapted for finalization context (e.g., "Generating your resume PDF...", "Optimizing page layout...", "Preparing final files...").

---

## Brainstorm Log

### Session 2026-06-20

**5 open questions explored and resolved:**

1. **PDF/HTML storage** → Files on configurable filesystem directory; metadata (status, path, page count, timestamps) in database via saved resume record. Compensation logic cleans up files on DB rollback. *(Added FR-008-028, updated Key Entities)*

2. **PDF fonts** → Inter (body) and Manrope (headings) ported from spike, isolated to PDF pipeline. Both OFL-licensed. Existing web fonts unaffected. *(Updated Assumptions)*

3. **Fitting failure UX** → Error message: "Resume could not be generated. Please try again and reduce the longest texts in your resume fields." "Try again" button returns to Review page with edits preserved, request status reset to allow re-finalization. *(Added FR-008-028-1, updated User Story 2 Scenario 6)*

4. **Bullet text HTML safety** → All user-editable text entering HTML-to-PDF pipeline MUST be HTML-escaped before template insertion. No user-controlled formatting in MVP. *(Added FR-008-023-1)*

5. **Concurrent finalization** → FINALIZING status on generation request blocks duplicate finalization. UI shows loading screen reusing existing AI-generation wait pattern with rotating phrases. On completion (success/failure), status resets. *(Added FR-008-028-2, FR-008-028-3, edge cases)*

**New edge cases identified**: Concurrent finalization, HTML/script injection in bullets, unwritable storage directory, stuck FINALIZING status after crash.

**Total impact**: 3 new FRs added, 4 new edge cases, 1 assumption updated, 1 key entity refined.
