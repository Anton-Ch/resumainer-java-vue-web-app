---
document_type: security-review
review_type: plan
assessment_date: 2026-06-06
codebase_analyzed: resumainer-java-vue-web-app (feature 005-user-home-page)
total_files_analyzed: 6
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

# Security Review: User Home Page & Resume Workspace (Plan Level)

## Executive Summary

**Overall Risk: LOW** — No critical or high findings. The plan demonstrates strong security awareness: PreparedStatement requirement, sort whitelist, admin role guard on both layers, and no technical metadata exposure are correctly specified. Two medium-severity items require attention during implementation (SQL injection vector in sort parameter construction, authorization enforcement for resume ownership). No systemic architecture risks found.

## Plan Artifacts Reviewed

| Artifact | Path | Status |
|----------|------|--------|
| `spec.md` | `specs/005-user-home-page/spec.md` | ✅ Reviewed |
| `plan.md` | `specs/005-user-home-page/plan.md` | ✅ Reviewed |
| `research.md` | `specs/005-user-home-page/research.md` | ✅ Reviewed |
| `data-model.md` | `specs/005-user-home-page/data-model.md` | ✅ Reviewed |
| `contracts/api-contracts.md` | `specs/005-user-home-page/contracts/api-contracts.md` | ✅ Reviewed |
| `WebConfig.java` | `backend/src/main/java/com/resumainer/config/WebConfig.java` | ✅ Reviewed (existing code for interceptor patterns) |

## Vulnerability Findings

### SEC-001 (Medium) — SQL Injection via Sort Parameter if Whitelist Bypassed

**Location**: `research.md:71` (sort whitelist mentioned), `data-model.md` (sort field list defined)

**OWASP**: A01:2025-Broken Access Control / A08:2025-Software and Data Integrity Failures

**CWE**: CWE-89: Improper Neutralization of Special Elements used in an SQL Command ('SQL Injection')

**CVSS**: 6.5 (AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:N/A:N)

**Description**: The plan correctly specifies a sort whitelist (`resumeTitle`, `vacancy`, `company`, `language`, `adaptationLevel`, `createdAt`). However, the implementation risk is in HOW the sort direction (`asc`/`desc`) is validated and how the whitelisted field is interpolated into the SQL query. If the direction is concatenated directly or the whitelist check is case-sensitive without normalization, an attacker could inject arbitrary SQL via the `sort` parameter.

**Recommendation**: 
- Validate sort field against a hardcoded `Set<String>` ALLOWED_SORT_FIELDS (case-insensitive)
- Validate direction as either `"asc"` or `"desc"` only (enum, not raw string)
- Use `PreparedStatement` for pagination (LIMIT/OFFSET as params)
- Construct ORDER BY clause using only validated tokens, never raw user input
- The sort field should be mapped to actual column name via switch/map, not concatenated

**Status**: Design choice is sound — implementation must enforce strictly.

---

### SEC-002 (Medium) — Resume Owner Authorization on DELETE

**Location**: `contracts/api-contracts.md:133-159` (DELETE endpoint definition)

**OWASP**: A01:2025-Broken Access Control

**CWE**: CWE-285: Improper Authorization

**CVSS**: 5.3 (AV:N/AC:L/PR:L/UI:N/S:U/C:N/I:L/A:L)

**Description**: The contract correctly defines `403 Not the owner of this resume` for the DELETE endpoint. However, the plan and spec do not specify HOW the owner check is performed. The AuthInterceptor only checks authentication (valid session), not authorization (does this user own this resume). If the ResumeDao or ResumeService does not filter by user_id, a user could delete another user's resume by guessing the ID.

**Recommendation**:
- All resume queries (list, delete, detail) MUST filter by the authenticated user's ID
- The user ID must come from the session/security context, NOT from a request parameter
- Add explicit test: `givenResumeOwnedByOtherUser_whenDelete_thenReturns403`
- Consider adding a `DELETE /api/resumes/{id}` authorization check in the Service layer, not just DAO

**Status**: Good design intent — implementation must enforce owner check in every resume query.

---

### SEC-003 (Low) — Soft-Deleted Resume Data Exposure in List Endpoint

**Location**: `data-model.md:30-39` (SavedResume includes publicUrl), `contracts/api-contracts.md:90-112` (list response)

**OWASP**: A05:2025-Security Misconfiguration

**CWE**: CWE-200: Exposure of Sensitive Information to an Unauthorized Actor

**CVSS**: 3.5 (AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:N/A:N)

**Description**: The plan specifies soft-delete for resumes. The `GET /api/resumes` endpoint must exclude soft-deleted resumes from its results. If the DAO query does not include a `WHERE deleted_at IS NULL` clause, soft-deleted resumes will still appear in the list and their `publicUrl` will be returned, even though the public link should return 410 Gone.

**Recommendation**: 
- Add explicit requirement to research.md or data-model.md: all resume list queries MUST filter `WHERE deleted_at IS NULL`
- The delete endpoint should set `deleted_at` timestamp, not physically remove the row
- Add a test: `givenDeletedResume_whenListResumes_thenNotIncluded`

**Status**: Minor — easily mitigated with proper SQL clause.

---

### SEC-004 (Informational) — Nginx SPA Fallback May Serve Unexpected Files

**Location**: `research.md:97-99` (Nginx config for `/app/` location)

**OWASP**: A05:2025-Security Misconfiguration

**CWE**: CWE-200: Exposure of Sensitive Information

**CVSS**: 2.1 (AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:N/A:N)

**Description**: The proposed Nginx config uses `try_files $uri $uri/ /app/index.html` for the `/app/` location. The `$uri/` component could potentially expose directory listings if autoindex is enabled, or serve unexpected files if the SPA build output contains artifacts beyond index.html. The risk is minimal because the SPA build typically contains only hashed assets in `/app/assets/` and `index.html`.

**Recommendation**:
- Add `autoindex off;` explicitly in the `/app/` location block
- Consider using `try_files $uri /app/index.html;` (without `$uri/` directory check)
- Ensure the frontend Docker build copies only the `dist/` output into Nginx's webroot

**Status**: Low risk — good practice to document explicitly.

---

### SEC-005 (Informational) — Cover Letter Content May Leak Personal Data in Logs

**Location**: `plan.md:43` (log safety mentioned), existing `AuthInterceptor.java` uses SLF4J

**OWASP**: A05:2025-Security Misconfiguration

**CWE**: CWE-532: Insertion of Sensitive Information into Log File

**CVSS**: 2.6 (AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:N/A:N)

**Description**: Cover letters are free-text fields that may contain personal information, phone numbers, or email addresses. If the backend logs request/response bodies at DEBUG/TRACE level (common during development), cover letter content could leak into log files. The plan mentions log safety but does not specify logging behavior for the new endpoints.

**Recommendation**:
- Add a note in the implementation tasks: "Ensure no resume content (title, cover letter, company) is logged at INFO level or above"
- Consider truncating or masking personal fields in access logs
- Verify Logback config does not log request/response bodies for `/api/resumes` or `/api/user/home`

**Status**: Good practice — implement with minimal effort.

## Confirmed Secure Patterns

| Pattern | Source | Status |
|---------|--------|--------|
| ✅ PreparedStatement for all SQL queries | Constitution IV, plan.md:43 | Explicitly required |
| ✅ Sort field whitelist (prevent SQL injection in ORDER BY) | data-model.md:71 | Explicitly defined |
| ✅ Admin role guard on both frontend AND backend | plan.md:45, FR-008/FR-009 | Correct |
| ✅ No token/technical metadata in User Home (FR-036) | spec.md:155 | Good data minimization |
| ✅ User-readable error messages (no stack traces) | spec.md (Edge Cases + Constitution V) | Consistent |
| ✅ CSRF protection via OWASP cookie-to-header | Existing CsrfFilter.java | Confirmed active |
| ✅ Security headers (CSP, X-Frame-Options, etc.) | WebConfig.java:209-214 | Confirmed active |
| ✅ AuthInterceptor protects `/api/**` paths | WebConfig.java:147-149 | Confirmed active |
| ✅ Delete requires confirmation dialog (FR-039) | spec.md:158 | Prevents accidental deletion |
| ✅ Soft-delete pattern (not physical removal) | contract api-contracts.md:135 | Correct approach |
| ✅ Independent block loading (FR-046) — prevents cascading failures | spec.md:165 | Good resilience |

## Action Plan

### 1. Durable Memory Capture

No systemic vulnerabilities or reusable security patterns discovered that warrant a new durable memory entry. The findings are implementation-level, not architectural.

### 2. Remediation Planning

**No critical or high findings** — follow-up review (`/speckit.security-review.followup`) is not required. However, the two medium findings (SEC-001, SEC-002) should be addressed during implementation:

- **SEC-001**: Add explicit sort validation method with whitelist + direction enum to `ResumeService` TDD tasks
- **SEC-002**: Add explicit owner-check tests to `ResumeService` and `ResumeDao` TDD tasks

### 3. Proposed INDEX.md Row

```text
| specs/005-user-home-page/security-review-plan.md | plan | 2026-06-06 | LOW | C:0 H:0 M:2 L:1 | A01,A05,A08 |
```
