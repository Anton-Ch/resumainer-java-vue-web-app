---
document_type: security-review
review_type: plan
assessment_date: 2026-06-07
codebase_analyzed: spec.md, plan.md, contracts/api.md, data-model.md, research.md, memory-synthesis.md (Feature 006 — User Profile Page)
total_files_analyzed: 6
total_findings: 5
overall_risk: MODERATE
critical_count: 0
high_count: 0
medium_count: 3
low_count: 2
informational_count: 0
owasp_categories: [A01, A04, A05, A06]
cwe_ids: [CWE-639, CWE-276, CWE-200, CWE-359, CWE-524]
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
  owasp_categories: "OWASP Top 10 2025 categories (A01-A09) that have at least one finding."
  cwe_ids: "CWE identifiers referenced in this document."
  finding_id: "Unique finding identifier (SEC-NNN) for cross-referencing and task linkage."
  location: "File path and line number of the vulnerable code (path/to/file.ext:line)."
  owasp_category: "OWASP Top 10 2025 category for this finding (AXX:2025-Name)."
  cwe: "Common Weakness Enumeration identifier with short name (CWE-NNN: Name)."
  cvss_score: "CVSS v3.1 base score (0.0-10.0). 9.0+=Critical, 7.0-8.9=High, 4.0-6.9=Medium, 0.1-3.9=Low."
  spec_kit_task: "Spec-Kit task ID for backlog tracking and remediation follow-up (TASK-SEC-NNN)."
---

# Security Review: Feature 006 — User Profile Page (Plan Review)

**Assessment date**: 2026-06-07
**Review type**: Plan (pre-implementation)
**Overall risk**: MODERATE
**Total findings**: 5 (0 Critical, 0 High, 3 Medium, 2 Low)

## Executive Summary

The Feature 006 plan for the User Profile Page is well-structured and follows established security patterns (PreparedStatement, backend validation, session auth). The plan correctly identifies authentication requirements and the existing global exception handler.

However, five gaps were identified. The most significant is the **absence of owner-scoped access control (IDOR)** — the plan must explicitly require that each user can only read/write their own profile data. Additionally, **PII data handling** (date of birth, citizenship, phone, email) lacks explicit guidance on data minimization, access logging, or retention. The **delete operations** plan does not distinguish between soft-delete and hard-delete, which could cause irreversible data loss.

All findings are actionable before task breakdown and can be addressed with clear implementation requirements.

## Plan Artifacts Reviewed

| Artifact | Path |
|---|---|
| spec.md | `specs/006-user-profile/spec.md` |
| plan.md | `specs/006-user-profile/plan.md` |
| contracts/api.md | `specs/006-user-profile/contracts/api.md` |
| data-model.md | `specs/006-user-profile/data-model.md` |
| research.md | `specs/006-user-profile/research.md` |
| memory-synthesis.md | `specs/006-user-profile/memory-synthesis.md` |

## Vulnerability Findings

### SEC-001: Owner-Scoped Access Control Not Explicit (MEDIUM)

**Location**: plan.md (API Contracts section), contracts/api.md
**OWASP**: A01:2025-Broken Access Control
**CWE**: CWE-639 — Authorization Bypass Through User-Controlled Key

**Description**: The plan states "All endpoints require authentication" but does not explicitly require that each request is scoped to the authenticated user's own data. Without explicit owner-scoped WHERE clauses in DAO queries or user_id verification in service methods, an authenticated user could access or modify another user's profile data by guessing/changing record IDs.

**Risk**: If user A sends `PUT /api/profile/experience/42` where record 42 belongs to user B, the system must reject it. The plan's current API contracts do not specify this check.

**Recommendation**: Add explicit requirement to plan/contracts:
- Every DAO query MUST include `WHERE user_id = ?` parameter derived from the authenticated session
- ProfileController MUST extract authenticated user from session and pass to service layer
- Service methods MUST verify ownership before returning or mutating any record

**Spec Kit resolution**: Add a non-functional security requirement to the spec and a check in the Implementation Gate checklist.

---

### SEC-002: PII Data Handling — No Data Minimization or Access Logging (MEDIUM)

**Location**: data-model.md, research.md
**OWASP**: A04:2025-Insecure Design, A05:2025-Security Misconfiguration
**CWE**: CWE-200 — Exposure of Sensitive Information, CWE-359 — Exposure of Private Information

**Description**: The profile feature stores PII (full name, email, phone, DOB, citizenship, location). The plan does not address:
- Whether profile data is included in server logs (risk: PII in log files)
- Whether profile access should be logged for audit (risk: no trace of who viewed/modified personal data)
- Whether profile data in API responses can be minimized for non-essential endpoints (e.g., status endpoint returns label strings but does not expose raw personal data)
- Whether DOB and citizenship, now confirmed as NOT NULL and required, need special handling

**Risk**: PII could leak through server logs if `@Slf4j` is used without care. Profile data is returned in full on every GET request. No audit trail for PII modifications.

**Recommendation**:
- Add a logging constraint: "Profile data field values MUST NOT appear in log statements. Log only operation type, user ID, and success/failure."
- Consider whether the status endpoint should return counts/labels without exposing raw personal data
- Ensure global exception handler (NFR-003) strips PII from error messages

---

### SEC-003: Delete Operations — No Soft-Delete or Recovery Path (MEDIUM)

**Location**: contracts/api.md, plan.md (Phase B/C)
**OWASP**: A04:2025-Insecure Design
**CWE**: CWE-276 — Incorrect Default Permissions

**Description**: The plan specifies DELETE endpoints that return 204 No Content for work experience, education, projects, and courses records. However, there is no mention of whether these are hard deletes (DELETE FROM) or soft deletes (is_deleted flag). The existing `users` and `saved_resume` tables use soft-delete pattern. Profile records — especially work experience and education — are core resume content with significant user effort invested. Accidental deletion has no recovery path.

**Risk**: A user accidentally deletes a Work Experience record. It is immediately and permanently removed from the database with no undo or recovery option.

**Recommendation**: For profile record deletions, consider one of:
- Soft-delete with `deleted_at` timestamp and `is_deleted` flag (consistent with `users.is_deleted` and `saved_resumes` pattern)
- Or, if hard delete is chosen, add a confirmation step on frontend (already described: "use PrimeVue ConfirmDialog") AND document the hard-delete decision with rationale in plan

**Preferred**: Soft-delete for data integrity, since profile data feeds resume generation. Soft-deleted records can be excluded from queries by default but recovered if needed.

---

### SEC-004: Username in Additional Info — Race Condition on Uniqueness (LOW)

**Location**: spec.md FR-028, contracts/api.md
**OWASP**: A01:2025-Broken Access Control
**CWE**: CWE-367 — Time-of-Check Time-of-Use (TOCTOU) Race Condition

**Description**: Username uniqueness validation uses a check-then-save pattern. Between the uniqueness check (SELECT) and the save (UPDATE users SET username = ?), another concurrent request could claim the same username. Without a unique constraint at the database level, a race condition could allow duplicate usernames.

**Risk**: Two users could save the same username if requests arrive simultaneously.

**Mitigation**: The BA data dictionary confirms `users.username` has a UNIQUE constraint (see data dictionary line 150). This is already handled. The frontend validation is cosmetic — the DB constraint is the authoritative guard. This finding is LOW because the DB already prevents the issue.

**Recommendation**: Ensure the DAO layer catches `DataIntegrityViolationException` or SQL constraint violation when an INSERT/UPDATE violates the unique constraint, and translates it to a user-friendly "username taken" error rather than a 500 Internal Server Error.

---

### SEC-005: Profile Section Status Endpoint — Information Disclosure (LOW)

**Location**: contracts/api.md — `GET /api/profile/status`
**OWASP**: A01:2025-Broken Access Control
**CWE**: CWE-524 — Information Exposure Through Caching

**Description**: The status endpoint returns structured data about each section including labels like "3 records" / "No records". This endpoint reveals information about profile completeness to any authenticated user. While this is intended for the profile owner, it should be explicitly scoped to the authenticated user only (see SEC-001).

**Risk**: Low — the status endpoint only reveals "completed/incomplete" and record counts, not actual personal data. However, if the response is cached by a shared browser or proxy, another user of the same machine could infer profile completeness.

**Recommendation**: 
- Ensure the status endpoint is owner-scoped (same as SEC-001)
- Set appropriate cache-control headers: `Cache-Control: no-store, private` on all profile endpoints

## Confirmed Secure Patterns

| Pattern | Status | Details |
|---|---|---|
| **PreparedStatement** | ✅ Confirmed | Explicitly required in plan constraints. All DAO queries use parameterized SQL. |
| **Backend validation authoritative** | ✅ Confirmed | Spec and plan state backend validation is final. Dual validation with frontend. |
| **Authentication required** | ✅ Confirmed | All `/api/profile/*` endpoints require `requiresAuth` guard. |
| **No stack traces exposed** | ✅ Confirmed | NFR-003/004 global exception handler strips internals from responses. |
| **Username validation** | ✅ Confirmed | Restricted character set (English letters, digits, underscores, hyphens). DB UNIQUE constraint. |
| **Session-based auth** | ✅ Confirmed | Existing session management from Feature 003 reused. |

## Action Plan & Next Steps

### Required Before Task Breakdown

| Priority | Finding | Action |
|---|---|---|
| Medium | SEC-001 — Owner-scoped access | Add `WHERE user_id = ?` requirement to plan + DAO contracts |
| Medium | SEC-002 — PII logging | Add logging constraint to plan's Non-Functional requirements |
| Medium | SEC-003 — Delete safety | Decide soft-delete vs hard-delete, document in plan |
| Low | SEC-004 — Username race condition | Already mitigated by DB constraint. Add exception handling note. |
| Low | SEC-005 — Cache headers | Add Cache-Control header requirement to API contracts |

### Durable Memory Check

No systemic vulnerabilities found — all findings are specific to this feature's implementation planning. No new reusable security patterns warrant durable memory capture at this stage.

### Next Step

After resolving SEC-001, SEC-002, and SEC-003 in the plan, proceed to `/speckit.tasks` for task breakdown with security markers included.

## INDEX.md Row

```
| docs/security-reviews/2026-06-07-feature-006-profile-plan.md | plan | 2026-06-07 | MODERATE | C:0 H:0 M:3 L:2 | A01,A04,A05,A06 |
```
