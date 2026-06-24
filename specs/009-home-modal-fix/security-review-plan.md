---
document_type: security-review
review_type: plan
assessment_date: 2026-06-24
codebase_analyzed: ResumAIner Feature 009 — Home Page Saved Resume Details Modal Fix
total_files_analyzed: 7
total_findings: 3
overall_risk: MODERATE
critical_count: 0
high_count: 0
medium_count: 1
low_count: 1
informational_count: 1
owasp_categories: [A01, A05]
cwe_ids: [CWE-203, CWE-208, CWE-22]
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

# Security Review Report — Plan Level

## Feature 009: Home Page Saved Resume Details Modal Fix

**Review date**: 2026-06-24
**Overall risk**: MODERATE (implementation risk becomes LOW after required mitigations)
**Files reviewed**: 7 (plan.md, spec.md, research.md, data-model.md, contracts/api-contracts.md, memory-synthesis.md, research.md)

---

## Executive Summary

This is a low-risk repair feature. The plan correctly addresses the three primary security concerns identified in project memory (D31 — DTO URL contract, B28 — uniform timing delay, path traversal protection).

Three findings were identified:
- **SEC-001 (Medium)**: 410 response may leak that a username was historically valid
- **SEC-002 (Low)**: `APP_PUBLIC_BASE_URL` fallback to request origin may produce incorrect URLs behind reverse proxy
- **SEC-003 (Info)**: Delete error message could leak resume existence for non-owned IDs

All findings have clear mitigations that MUST be applied during implementation and reflected in `plan.md` / `tasks.md`.

---

## Plan Artifacts Reviewed

| Artifact | Path | Status |
|---|---|---|
| Feature spec | `specs/009-home-modal-fix/spec.md` | ✅ Reviewed |
| Implementation plan | `specs/009-home-modal-fix/plan.md` | ✅ Reviewed |
| Research | `specs/009-home-modal-fix/research.md` | ✅ Reviewed |
| Data model | `specs/009-home-modal-fix/data-model.md` | ✅ Reviewed |
| API contracts | `specs/009-home-modal-fix/contracts/api-contracts.md` | ✅ Reviewed |
| Memory synthesis | `specs/009-home-modal-fix/memory-synthesis.md` | ✅ Reviewed |

---

## Vulnerability Findings

### SEC-001 — 410 response may leak valid username information [MEDIUM]

| Field | Value |
|---|---|
| Location | `specs/009-home-modal-fix/plan.md` — public route section, `data-model.md` — PublicResumeLookupResult |
| OWASP Category | A01:2025-Broken Access Control |
| CWE | CWE-203: Observable Discrepancy |
| CVSS Score | 5.3 (AV:N/AC:L/PR:N/UI:N/S:U/C:L/I:N/A:N) |

**Description**: Returning `410 Gone` for soft-deleted resumes indicates that the username+code combination was *historically valid*. An attacker can distinguish between "never existed" (404) and "existed but was deleted" (410). While the uniform artificial delay (FR-019) prevents timing attacks, the different HTTP status code itself leaks information: a 410 response tells the attacker the username existed and had a resume at some point.

The spec explicitly requires `410 Gone` (User Story 6, FR-019). This is a deliberate product decision for improved recruiter UX over strict information hiding. The risk is partially mitigated by:
- Uniform artificial delay on both 404 and 410 (prevents timing enumeration)
- Same delay value used for all error responses
- No additional metadata returned in 410 body (only branded Thymeleaf page)

**Mitigation** (apply during implementation):
- Ensure 410 response body contains no specific information about *when* the resume was deleted or *what* the resume contained
- Ensure 410 page is a static Thymeleaf template with no dynamic data beyond i18n strings
- Verify 410 and 404 use identical `Content-Type`, `Content-Length` (or transfer-encoding), `Cache-Control` headers to prevent header-based fingerprinting

**Verification**: Add assertion in backend test: `GET /deleted/resume` → `410` with predefined Thymeleaf body. Verify no dynamic content (dates, IDs, filenames) appears in 410 response body.

---

### SEC-002 — APP_PUBLIC_BASE_URL fallback may produce incorrect URLs behind reverse proxy [LOW]

| Field | Value |
|---|---|
| Location | `specs/009-home-modal-fix/data-model.md` — Public Base URL Configuration |
| OWASP Category | A05:2021-Security Misconfiguration |
| CWE | CWE-208: Information Exposure Through Environmental Variables |
| CVSS Score | 2.2 (AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:N/A:N) |

**Description**: The plan specifies that `APP_PUBLIC_BASE_URL` falls back to the current request origin if not configured. In a reverse proxy setup (Nginx → Tomcat), the request origin may be `http://backend:8080` (internal Docker network) instead of `https://resumainer.com` (public). This would generate incorrect public URLs.

The plan (FR-007, data-model.md) already accounts for this by reading `APP_PUBLIC_BASE_URL` from environment. The fallback to request origin is only a safety net for local development.

**Mitigation** (apply during implementation):
- Read `X-Forwarded-Host` or `X-Forwarded-Proto` headers for origin resolution behind proxy
- Log a warning when `APP_PUBLIC_BASE_URL` is not configured and request origin is used as fallback
- Add a dev-mode assertion: if `APP_PUBLIC_BASE_URL` contains `localhost` in production profile, log a warning

**Verification**: Backend test: `APP_PUBLIC_BASE_URL` not set + `X-Forwarded-Host` header present → URL uses forwarded host. Docker integration test: verify public URLs from backend match frontend-facing origin.

---

### SEC-003 — Delete error message may leak resume existence for non-owned IDs [INFORMATIONAL]

| Field | Value |
|---|---|
| Location | `specs/009-home-modal-fix/spec.md` — FR-017, User Story 5 |
| OWASP Category | A01:2025-Broken Access Control |
| CWE | CWE-208: Observable Timing Discrepancy |
| CVSS Score | 3.1 (AV:N/AC:H/PR:L/UI:N/S:U/C:L/I:N/A:N) |

**Description**: The delete endpoint (`DELETE /api/resumes/{id}`) currently returns `404` when the resume is not found or not owned by the authenticated user. If an error toast is shown with "Resume not found" vs some other message, a malicious authenticated user could distinguish between "this ID does not exist" and "this ID belongs to another user."

**Mitigation** (apply during implementation):
- Use a single generic error message for all delete failures: "Failed to delete resume."
- Log the detailed reason server-side (owner mismatch vs not found vs DB error) but return the same generic response to the client
- Add uniform artificial delay on delete failures (async, similar to B28 pattern) to prevent timing-based enumeration of valid IDs

**Verification**: Backend test: delete non-owned resume and non-existent resume both return same HTTP status and body. Confirm no timing difference in tests.

---

## Confirmed Secure Patterns

The plan correctly incorporates the following security measures from project memory and spec:

| # | Pattern | Source | Status in plan |
|---|---|---|---|
| 1 | **No raw filesystem paths in API responses** | FR-010, Constitution V | ✅ Covered — explicitly required in data-model.md and contracts |
| 2 | **HTML download is authenticated owner-only** | FR-011, Constitution V | ✅ Covered — explicitly stated in spec and contracts |
| 3 | **Uniform artificial delay for 404 and 410** | FR-019, B28 | ✅ Covered — guardrail: reuse existing mechanism or STOP |
| 4 | **Path traversal returns 404 with no metadata** | FR-019, spec Edge Cases | ✅ Covered — explicitly in data-model PublicResumeLookupResult |
| 5 | **Soft-delete consistency (is_deleted + deleted_at)** | FR-018 | ✅ Covered — delete sets both fields |
| 6 | **Delete confirmation before execution** | FR-015 | ✅ Covered — modal confirmation dialog required |
| 7 | **Double-click delete protection** | FR-017 (brainstorming) | ✅ Covered — button disabled+loading after first click |
| 8 | **i18n for all user-facing text** | FR-023, Constitution III | ✅ Covered — explicit list of what must be i18n'd |
| 9 | **No database migration without confirmation** | FR-025 | ✅ Covered — schema inspection first, STOP if migration needed |
| 10 | **DTO URL contract (D31)** | D31 | ✅ Covered — HomeSavedResumeDto uses canonical URLs |
| 11 | **Public route does not intercept /api/**, /app/**, etc.** | FR-022 | ✅ Covered — explicitly required in spec |

---

## Summary

| Severity | Count | Key Concern |
|---|---|---|
| Critical | 0 | — |
| High | 0 | — |
| Medium | 1 | SEC-001: 410 response may leak historical username validity |
| Low | 1 | SEC-002: URL fallback behind reverse proxy |
| Informational | 1 | SEC-003: Delete error message consistency |

**Overall Risk**: **MODERATE** by highest active finding severity (SEC-001). Implementation risk is expected to become **LOW** after the required mitigations are applied. No systemic vulnerabilities. No changes to auth/session/CSP boundaries.

---

## Action Plan

### Durable Memory Capture

No new systemic vulnerabilities or reusable security patterns were identified that warrant a durable memory entry. The existing B28 (timing delay) and D31 (DTO URL contract) entries already cover the primary security patterns in this feature.

### Remediation Planning

No critical or high findings found — formal followup review is not required. Apply mitigations during implementation:
1. **SEC-001**: Add tests for 404/410 delay reuse and 410 page body. The 410 body must contain only approved static/i18n text and no username, public code, IDs, dates, paths, filenames, vacancy/company data, or deletion metadata.
2. **SEC-002**: Implement `X-Forwarded-Host`/`X-Forwarded-Proto` detection in `PublicUrlService`; log warning when `APP_PUBLIC_BASE_URL` is missing and fallback origin is used. Do not add a dotenv library unless already present.
3. **SEC-003**: Use generic delete failure message in API/frontend. Verify non-owned and non-existent delete attempts return the same public response status/body/message shape.

---

## INDEX.md Routing Row

```
| specs/009-home-modal-fix/security-review-plan.md | plan | 2026-06-24 | LOW | C:0 H:0 M:1 L:1 I:1 | A01,A05 |
```


## Correction Note

This corrected security review uses `MODERATE` as `overall_risk` because SEC-001 is a Medium finding. The product accepts the 410-vs-404 distinction for recruiter UX, but the listed mitigations are mandatory before implementation can be considered low risk.
