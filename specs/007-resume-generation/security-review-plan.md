---
document_type: security-review
review_type: plan
assessment_date: 2026-06-12
codebase_analyzed: ResumAIner (feat/007-resume-generation)
total_files_analyzed: 5
total_findings: 5
overall_risk: LOW
critical_count: 0
high_count: 0
medium_count: 2
low_count: 2
informational_count: 1
owasp_categories: [A01:2025-Broken Access Control, A05:2025-Security Misconfiguration, A08:2025-Software and Data Integrity Failures]
cwe_ids: [CWE-22: Path Traversal, CWE-200: Information Exposure, CWE-770: Allocation of Resources Without Limits]
field_summaries:
  document_type: "Always 'security-review'. Allows indexers to skip non-review documents."
  review_type: "Which command generated this document: audit, branch, staged, plan, tasks, or followup."
  assessment_date: "ISO 8601 date the review was performed (YYYY-MM-DD)."
  overall_risk: "Highest severity tier with active findings (CRITICAL, HIGH, MODERATE, LOW, INFORMATIONAL)."
  critical_count: "Number of Critical findings (CVSS 9.0-10.0)."
  high_count: "Number of High findings (CVSS 7.0-8.9)."
  medium_count: "Number of Medium findings (CVSS 4.0-6.9)."
  low_count: "Number of Low findings (CVSS 0.1-3.9)."
  informational_count: "Number of Informational findings."
  owasp_categories: "OWASP Top 10 2025 categories (A01-A10) that have at least one finding."
  cwe_ids: "CWE identifiers referenced in this document."
  finding_id: "Unique finding identifier (SEC-NNN) for cross-referencing and task linkage."
  location: "File path and line number of the vulnerable code (path/to/file.ext:line)."
  owasp_category: "OWASP Top 10 2025 category for this finding (AXX:2025-Name)."
  cwe: "Common Weakness Enumeration identifier with short name (CWE-NNN: Name)."
  cvss_score: "CVSS v3.1 base score (0.0-10.0). 9.0+=Critical, 7.0-8.9=High, 4.0-6.9=Medium, 0.1-3.9=Low."
  spec_kit_task: "Spec-Kit task ID for backlog tracking and remediation follow-up (TASK-SEC-NNN)."
---

# Security Review: Resume Generation (Plan Review)

## Executive Summary

**Overall Risk: LOW**

The implementation plan for Feature 007 (Resume Generation) demonstrates strong security awareness. Key controls are addressed from the design phase: owner-scoped endpoints, PreparedStatement-only SQL, API key masking, XSS sanitization for AI-generated content, CSRF token handling, and public route safety (410 Gone for deleted resumes). No critical or high-risk gaps were identified.

Two medium-severity findings require explicit implementation guidance: prompt render log access control and file path sanitization. Three lower-severity items are noted for awareness. All are easily addressed during implementation with documented patterns.

## Plan Artifacts Reviewed

| Artifact | File |
|----------|------|
| Spec | `specs/007-resume-generation/spec.md` |
| Plan | `specs/007-resume-generation/plan.md` |
| Memory Synthesis | `specs/007-resume-generation/memory-synthesis.md` |
| Constitution | `.specify/memory/constitution.md` |
| Project Memory | `docs/memory/DECISIONS.md`, `docs/memory/BUGS.md`, `docs/memory/WORKLOG.md` |

## Vulnerability Findings

### SEC-001: Prompt Render Log Access Control (Medium)

| Field | Value |
|-------|-------|
| **OWASP Category** | A01:2025-Broken Access Control |
| **CWE** | CWE-200: Information Exposure |
| **CVSS Score** | 5.3 (Medium) |
| **Location** | Plan — Phase 3: Prompt config + builder; spec FR-GEN-007 |
| **Spec-Kit Task** | TASK-SEC-001 |

**Description:** The plan identifies prompt render logs (`ai_prompt_render_log`) as "admin/debug sensitive" and notes they "may contain PII." However, no explicit access control mechanism is specified. The render log will contain the user's full profile payload (name, contact info, work history, education, skills) and vacancy text. If exposed through an API endpoint without admin-only restrictions, this constitutes a PII data leak.

**Risk:** An authenticated user could access another user's prompt render log and view their full profile data and vacancy information.

**Recommendation:** Add explicit access control to the prompt render log:
- No frontend endpoint should expose render logs
- Backend render log writes should be append-only (no read endpoint for regular users)
- Admin-only database access for debugging; no API endpoint for render logs in MVP
- Include this restriction in the DAO and service design for `ai_prompt_render_log`

---

### SEC-002: Generated File Path Sanitization (Medium)

| Field | Value |
|-------|-------|
| **OWASP Category** | A01:2025-Broken Access Control |
| **CWE** | CWE-22: Path Traversal |
| **CVSS Score** | 5.3 (Medium) |
| **Location** | Plan — GeneratedFileStorageService, `generated_results/{username}/{public_code}/` |
| **Spec-Kit Task** | TASK-SEC-002 |

**Description:** The plan specifies file storage under `generated_results/{username}/{public_code}/` and states "sanitize username/path segments." However, path traversal prevention must be explicitly implemented. If the username contains `../` sequences, an attacker could write files outside the intended directory structure. While the username comes from the database (not user input), a compromised account or future username update feature could introduce traversal risk.

**Risk:** Path traversal allowing file writes outside the intended `generated_results/` directory, potentially overwriting system files or application resources.

**Recommendation:** In `GeneratedFileStorageService`:
- Strip all `../`, `./`, null bytes, and path separator characters from the username segment
- Use `Path.normalize()` and verify the resolved path starts with the expected base directory
- Never use raw username strings in file path construction
- For the `public_code` segment, enforce alphanumeric-only validation (it's generated server-side, so this is naturally safe)

---

### SEC-003: Public Code Collision Handling (Low)

| Field | Value |
|-------|-------|
| **OWASP Category** | A05:2025-Security Misconfiguration |
| **CWE** | — |
| **CVSS Score** | 2.0 (Low) |
| **Location** | Plan — PublicCodeGenerator, public code in `saved_resume` |
| **Spec-Kit Task** | TASK-SEC-003 |

**Description:** The plan uses 4-character public codes for resume URLs (e.g., `A8IWU`). At 4 chars with mixed-case alphanumeric (excluding ambiguous chars), the namespace is ~810,000 combinations. A unique constraint is specified, but no collision retry logic is described. If a collision occurs, the insert would fail with a constraint violation.

**Risk:** Unlikely for MVP scale (<1k resumes). At larger scale, insert failures due to unique constraint violations would need retry logic.

**Recommendation:** 
- For MVP: Add collision retry in `PublicCodeGenerator.generate()` — loop until unique insert succeeds (max 5 attempts), then fall back to longer code
- Document the collision handling approach in `GeneratedFileStorageService` and `SavedResumeDao`

---

### SEC-004: Rate Limiting on Request Creation (Low)

| Field | Value |
|-------|-------|
| **OWASP Category** | A01:2025-Broken Access Control |
| **CWE** | CWE-770: Allocation of Resources Without Limits |
| **CVSS Score** | 2.6 (Low) |
| **Location** | Plan — GenerationRequestService, `POST /api/generate/requests/` |
| **Spec-Kit Task** | TASK-SEC-004 |

**Description:** The plan limits active generation to one per user, but does not limit the number of draft generation requests a user can create. A user could call `POST /api/generate/requests` repeatedly without triggering generation, creating unlimited database rows. This is a low-severity resource exhaustion vector.

**Risk:** Database bloat from abandoned draft requests. Minor for MVP with limited users.

**Recommendation:** 
- For MVP: Accept as acceptable risk — document in assumptions
- Optional enhancement: Limit active (non-completed/non-deleted) requests per user to a reasonable number (e.g., 50)

---

### SEC-005: PDF Library Dependency Risk (Informational)

| Field | Value |
|-------|-------|
| **OWASP Category** | A08:2025-Software and Data Integrity Failures |
| **CWE** | — |
| **CVSS Score** | N/A |
| **Location** | Plan — PdfGenerationService |
| **Spec-Kit Task** | TASK-SEC-005 |

**Description:** The plan specifies a "separate PdfGenerationService" for PDF conversion but does not specify the HTML-to-PDF library. Different libraries have different security profiles:
- **Flying Saucer** (xhtmlrenderer): Mature, CSS 2.1 only, no JS execution — safe by design
- **OpenPDF**: Active development, iText fork, well-maintained
- **Apache PDFBox**: Low-level, no HTML-to-PDF conversion — requires separate HTML renderer
- **wkhtmltopdf**: Uses WebKit — larger attack surface, JS execution possible

**Risk:** LOW. For a server-side HTML-to-PDF tool processing controlled template input (not user-provided HTML), the risk is minimal. Library choice should prioritize A4 layout support, Cyrillic text rendering, and page count validation.

**Recommendation:** 
- Document the library choice with security rationale before implementation
- Prefer a library that does NOT execute JavaScript (Flying Saucer or OpenPDF-based)
- Validate page count after PDF generation (one or two pages) as specified in the plan

---

## Confirmed Secure Patterns

The following security controls are correctly addressed in the plan:

| # | Control | Evidence in Plan |
|---|---------|-----------------|
| ✅ | **Owner-scoped access** | Security boundaries table maps all 10 endpoints with user_id filtering |
| ✅ | **PreparedStatement SQL** | Mandated for all DAO queries (Constitution §IV) |
| ✅ | **API key masking** | "Never expose API keys," "masked in UI," "never in logs" |
| ✅ | **XSS sanitization** | "AI-generated HTML sanitized with allowlist" |
| ✅ | **CSRF protection** | "CSRF-aware API calls" using existing httpClient.ts pattern (B17) |
| ✅ | **No secrets in logs** | Constitution §V — API keys, PII not logged |
| ✅ | **Public route safety** | 410 Gone for deleted resumes (spec SC-009) |
| ✅ | **Error safety** | FR-GEN-045: no raw provider errors, stack traces, or API keys exposed |
| ✅ | **One active generation** | Prevents concurrent AI calls abuse |
| ✅ | **File download auth** | Owner-scoped download endpoints with DB authorization |
| ✅ | **Bean registration** | Explicit @Bean / @ComponentScan for all new classes (B5) |
| ✅ | **Transaction safety** | catch(Exception) for rollback, not just SQLException (D23) |

## Action Plan & Next Steps

### Required Before Implementation

1. **SEC-001**: Add explicit note that prompt render logs have no API read endpoint for regular users — admin DB access only
2. **SEC-002**: Add path traversal prevention implementation detail to `GeneratedFileStorageService` design
3. **SEC-003**: Add collision retry logic to `PublicCodeGenerator`

### Memory Capture

No new systemic security patterns or architecture boundaries were discovered that need durable memory capture. All findings are implementation-level guidance that fit within existing security decisions (D8 CSRF, D23 transactions, Constitution §V).

### Recommended Approach

These findings are best addressed as implementation task notes rather than blocking the plan. Security review can proceed to `/speckit.superpowers.tasks` with the above items included as acceptance criteria for the relevant tasks.

## INDEX.md Row

```text
| specs/007-resume-generation/security-review-plan.md | plan | 2026-06-12 | LOW | C:0 H:0 M:2 L:2 I:1 | A01,A05,A08 |
```
