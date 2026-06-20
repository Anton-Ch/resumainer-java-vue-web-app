# Memory Synthesis — Feature 008

## Current Scope

PDF/HTML resume export + bullet-point review hardening. Two phase groups: (1) structured bullet persistence, prompt hardening, review editing; (2) HTML-to-PDF rendering engine ported from approved spike, PDF/HTML parity, authenticated download, public PDF route.

Affected modules: DAO layer (bullet tables, PDF config, saved resume metadata), service layer (parser, validator, review, finalization, PDF generation), controller (export, download, public route), frontend (review bullets, export buttons, loading states).

---

## Relevant Decisions

- **D25**: HTML-first generation pipeline with deferred PDF conversion. This IS Feature 008 — the deferred PDF step from Feature 007. The spike already validated OpenHTMLToPDF approach. *(Source: DECISIONS.md, active)*
- **D10**: DAO connection-accepting overloads for JDBC transaction support. Finalization spans file writes + DB inserts — requires connection-passing pattern for atomicity. *(Source: DECISIONS.md, active)*
- **D23**: Manual JDBC transaction must catch Exception, not just SQLException. Critical for file+DB compensation: if file write succeeds but DB insert throws RuntimeException, rollback must still fire. *(Source: DECISIONS.md, active)*
- **D27**: Backend-generated opaque updateKey for review/save pattern. Bullet editing must use the same pattern — frontend receives opaque keys, not raw DB IDs. *(Source: DECISIONS.md, active)*
- **D7**: Hybrid PK strategy: gen_random_uuid() for entities, BIGSERIAL for lookups. New bullet tables (generation_response_experience_bullet, generation_response_project_bullet) should use BIGSERIAL as they are lookup/child tables. New PDF config tables (resume_pdf_fit_limits, resume_pdf_fill_targets) should also use BIGSERIAL. *(Source: DECISIONS.md, active)*

---

## Active Architecture Constraints

- **Constitution IV (Performance & Reliability)**: PreparedStatement for all SQL. Manual JDBC transactions with commit/rollback. PDF server-side only. Post-generation validation must verify page count. Budget config DB-backed. UTF-8 throughout. *(Source: .specify/memory/constitution.md)*
- **Constitution V (Security by Design)**: Owner-scoped downloads. Public route exposes only finalized PDF — no profile data, drafts, cover letter. Soft-deleted resumes return 404 for public links. Backend validation authoritative. No raw paths in responses. Logs safe — no secrets, no PII dumps. *(Source: .specify/memory/constitution.md)*
- **Constitution I (Code Quality)**: Layered architecture. KISS — no over-engineering. Legacy renderer kept but deprecated. Ported spike classes follow project conventions. *(Source: .specify/memory/constitution.md)*

---

## Accepted Deviations

- *None identified for this scope.*

---

## Relevant Security Constraints

- Public link must serve PDF inline only, never HTML, never cover letter. *(Source: constitution V)*
- Invalid/deleted/disabled public links → 404, no metadata leakage. *(Source: constitution V + spec.md)*
- User-editable text in HTML-to-PDF pipeline MUST be HTML-escaped (FR-008-023-1). *(Source: spec.md, clarified in brainstorming)*
- No raw filesystem paths in API responses or logs. *(Source: constitution V)*

---

## Related Historical Lessons

- **B24 (Cover letter INSERT omission)**: When adding new columns to saved_resumes (PDF metadata fields: pdf_status, pdf_file_path, pdf_page_count, etc.), ensure INSERT and UPDATE statements include ALL new columns. The V8 migration added cover_letter but INSERT skipped it — same mistake likely with new PDF metadata columns. Audit every DAO write method against its migration. *(Source: BUGS.md, active)*
- **B15 (FK type mismatch)**: FK column type must match referenced PK. New bullet tables reference generation_response_experience(id) and generation_response_project(id) — verify these are BIGINT/BIGSERIAL before creating FK. *(Source: BUGS.md, active)*
- **B9 (Long auto-unboxing NPE)**: Nullable PDF metadata fields (pdf_page_count, pdf_generated_at) must use Long (boxed), not long (primitive), to avoid NullPointerException on auto-unboxing when PDF generation fails and fields remain null. *(Source: BUGS.md, active)*
- **W16 (Feature 007 export fixes)**: ExportResult.vue pattern with bilingual two-column layout, language chips, toast notifications, and real DTO integration should be reused/extended for PDF/HTML download buttons. *(Source: WORKLOG.md, active)*

---

## Conflict Warnings

- *None detected. The spec aligns with all active constitution principles, decisions, and architectural constraints.*

---

## Retrieval Notes

- Index entries considered: 10
- Decisions selected: 5 (D25, D10, D23, D27, D7)
- Architecture constraints: 3 (Constitution I, IV, V)
- Security constraints: 4
- Bug patterns: 3 (B24, B15, B9)
- Worklog items: 1 (W16)
- Budget status: ~490 words (within 900 limit)
