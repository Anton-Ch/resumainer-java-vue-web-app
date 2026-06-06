---
document_type: security-review
review_type: branch
assessment_date: 2026-06-06
codebase_analyzed: resumainer-java-vue-web-app (branch feat/005-user-home-page)
total_files_analyzed: 60
total_findings: 1
overall_risk: LOW
critical_count: 0
high_count: 0
medium_count: 0
low_count: 1
informational_count: 0
owasp_categories: [A01]
cwe_ids: [CWE-942]
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

# SECURITY REVIEW REPORT — BRANCH: feat/005-user-home-page vs main

## Executive Summary

**Overall Risk: LOW** — One LOW-severity finding (CORS wildcard, carried over from Phase 1). No new security vulnerabilities were introduced in Feature 005. The implementation follows secure-by-design principles: PreparedStatement for all queries, sort whitelist for injection prevention, owner authorization checks, soft-delete data isolation, and proper session validation.

## Branch Diff Reviewed

**Target**: `feat/005-user-home-page`
**Base**: `main`
**Files changed**: ~120 files (60 source + 60 spec/prototype)

## Vulnerability Findings

### 🔵 LOW: CORS wildcard origin with credentials (carried forward)

**Location**: `backend/src/main/java/com/resumainer/config/WebConfig.java:67-68`

**OWASP Category**: A01:2025-Broken Access Control

**CWE**: CWE-942: Permissive Cross-domain Policy with Untrusted Domains

**Description**: CORS config uses `.allowedOriginPatterns("*")` with `.allowCredentials(true)`. In production (Docker), Nginx proxies `/api/` requests to the backend within the same origin, so CORS is not exercised. This config primarily enables Vite dev server (port 5173) to call the backend. Not exploitable in production, but technically permissive.

**Remediation**: Before production deployment, restrict to specific origins or disable CORS in `prod` profile. Unchanged since Phase 1 — already documented and accepted.

## Confirmed Secure Patterns

| Pattern | Location | Evidence |
|---------|----------|----------|
| ✅ **No SQL injection** | `ResumeDao.java` | All queries via PreparedStatement. Sort field validated against `ALLOWED_SORT_FIELDS` whitelist |
| ✅ **Owner authorization** | `ResumeDao.java` | Every query has `WHERE user_id = ?`. `softDelete()` checks `id = ? AND user_id = ?` |
| ✅ **Soft-delete data isolation** | `ResumeDao.java` | All list queries: `WHERE deleted_at IS NULL`. Deleted resumes invisible to list/GET |
| ✅ **Authentication** | `AuthInterceptor.java` | Session check on all `/api/**` paths (except `/api/auth/**`) |
| ✅ **CSRF protection** | `CsrfFilter.java` | OWASP cookie-to-header pattern (existing, unchanged) |
| ✅ **Security headers** | `WebConfig.java:209-214` + `nginx.conf` | CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy |
| ✅ **No hardcoded secrets** | All files | No passwords, API keys, or tokens in any changed file |
| ✅ **Log safety** | `ResumeController.java`, `UserHomeController.java` | Only userId, HTTP status logged. No resume content at INFO+ |
| ✅ **Input validation** | `ResumeService.java` | Page/size bounds checked. Sort field whitelist. Direction restricted to asc/desc |
| ✅ **No stack traces exposed** | Controllers | All errors return generic messages. `IllegalArgumentException` → 400, server error → 500 |
| ✅ **Admin route guard** | `Vue Router guard` | `/admin` requires `requiresAdmin` meta. Backend also enforces via session role |
| ✅ **Nginx hardening** | `frontend/nginx.conf`, `docker/nginx.conf` | `autoindex off`. `try_files` without `$uri/`. SPA under `/app/` isolated |
| ✅ **Dependency supply chain** | `package.json` | Only `primeicons` added (npm package for icon font). No risky dependencies |
| ✅ **HTTP-only cookies** | `WebConfig.java:126` | Language cookie uses `setCookieHttpOnly(true)` (existing) |

## Findings Summary

| Severity | Count | Status |
|----------|-------|--------|
| Critical | 0 | — |
| High | 0 | — |
| Medium | 0 | — |
| Low | 1 | CORS wildcard (accepted risk) |
| Informational | 0 | — |

## Action Plan

1. ✅ **No critical/high findings** — feature is safe for merge
2. 📝 CORS restriction: add to deployment checklist for production
3. No durable memory capture needed — no new systemic vulnerabilities discovered

## Proposed INDEX.md Row

```text
| specs/005-user-home-page/security-review-branch.md | branch | 2026-06-06 | LOW | C:0 H:0 M:0 L:1 | A01 |
```
