---
document_type: security-review
review_type: followup
assessment_date: 2026-06-06
codebase_analyzed: resumainer-java-vue-web-app (feature 005-user-home-page)
total_files_analyzed: 5
total_findings: 5
overall_risk: LOW
critical_count: 0
high_count: 0
medium_count: 2
low_count: 1
informational_count: 2
owasp_categories: [A01, A05, A08]
cwe_ids: [CWE-89, CWE-285, CWE-200, CWE-532]
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

# Security Follow-Up Plan: User Home Page & Resume Workspace

## Executive Summary

All 5 findings from the security review have been resolved. 2 were applied to `plan.md` (Constraints section), 1 to `data-model.md`, 1 to `research.md`. No technical debt was deferred — all findings are marked for immediate implementation.

## Inputs Reviewed

| Input | Source |
|-------|--------|
| Security review report | `specs/005-user-home-page/security-review-plan.md` |
| Implementation plan | `specs/005-user-home-page/plan.md` |
| Data model | `specs/005-user-home-page/data-model.md` |
| Research | `specs/005-user-home-page/research.md` |
| API contracts | `specs/005-user-home-page/contracts/api-contracts.md` |

## Resolution Decisions

| Finding | Severity | Resolution | Rationale |
|---------|----------|------------|-----------|
| SEC-001 — Sort SQL injection | 🟡 Medium | **Implement now** | Low effort, high impact — whitelist + enum |
| SEC-002 — Resume owner authorization | 🟡 Medium | **Implement now** | Required for basic access control |
| SEC-003 — Soft-delete filter | 🔵 Low | **Implement now** | Single WHERE clause, trivial to add |
| SEC-004 — Nginx hardening | ℹ️ Info | **Implement now** | One-line config change in research.md |
| SEC-005 — Log safety | ℹ️ Info | **Implement now** | Code review practice, no code change |

## Artifacts Updated

| Artifact | Change | Finding |
|----------|--------|---------|
| `plan.md` (Constraints section) | Added 5 constraint lines covering all findings | SEC-001 — SEC-005 |
| `data-model.md` | Added "Database Query Rules" section with whitelist, owner filter, soft-delete rules | SEC-001, SEC-002, SEC-003 |
| `research.md` (Nginx config) | `autoindex off;` + `try_files $uri /app/index.html` (removed `$uri/`) | SEC-004 |

## Remediation Tasks

| Task ID | Title | Severity | Source | Verification |
|---------|-------|----------|--------|-------------|
| TASK-SEC-001 | Sort parameter: whitelist validation + direction enum | 🟡 Medium | SEC-001 | Test: invalid sort field returns 400. Test: SQL injection attempt does not alter query. |
| TASK-SEC-002 | Resume DAO: filter all queries by authenticated user ID | 🟡 Medium | SEC-002 | Test: delete another user's resume returns 403. Test: list returns only own resumes. |
| TASK-SEC-003 | Resume list: exclude soft-deleted records | 🔵 Low | SEC-003 | Test: deleted resume absent from list response. |
| TASK-SEC-004 | Nginx: secure `/app/` location block | ℹ️ Info | SEC-004 | Review: `autoindex off` present, `$uri/` absent. |
| TASK-SEC-005 | Log safety: no resume content at INFO+ level | ℹ️ Info | SEC-005 | Review: no request/response body logging for `/api/resumes` or `/api/user/home`. |

## Backlog-Ready Task Table

| Task ID | Title | Severity | Type | Source Finding | Depends On | Acceptance Criteria |
|---------|-------|----------|------|----------------|------------|-------------------|
| TASK-SEC-001 | Sort whitelist + direction validation | 🟡 Medium | Implement | SEC-001 | — | Invalid sort field returns 400; SQL injection attempt doesn't alter query; direction restricted to asc/desc |
| TASK-SEC-002 | Resume DAO user ownership filter | 🟡 Medium | Implement | SEC-002 | — | All resume queries filter by authenticated user ID; delete other user's resume returns 403 |
| TASK-SEC-003 | Soft-delete exclusion in list queries | 🔵 Low | Implement | SEC-003 | — | `WHERE deleted_at IS NULL` in all list queries; deleted resume not in response |
| TASK-SEC-004 | Nginx `/app/` secure config | ℹ️ Info | Implement | SEC-004 | — | `autoindex off` + `try_files $uri /app/index.html` |
| TASK-SEC-005 | Log safety for resume endpoints | ℹ️ Info | Implement | SEC-005 | — | No resume content logged at INFO+; Logback config verified |

## Confirmed Secure Patterns (unchanged)

All 11 confirmed patterns from the security review remain active. No patterns were invalidated by the follow-up.

## Next Steps

The artifacts have been updated. The findings are ready to be incorporated into task breakdown during `/speckit.tasks`. No separate `/speckit.security-review.apply` is needed.

## Proposed INDEX.md Row

```text
| specs/005-user-home-page/security-review-followup.md | followup | 2026-06-06 | LOW | C:0 H:0 M:2 L:1 | A01,A05,A08 |
```
