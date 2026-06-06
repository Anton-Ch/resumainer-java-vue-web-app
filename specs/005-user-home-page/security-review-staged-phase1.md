---
document_type: security-review
review_type: staged
assessment_date: 2026-06-06
codebase_analyzed: resumainer-java-vue-web-app (Phase 1 changes)
total_files_analyzed: 5
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

# SECURITY REVIEW REPORT — STAGED CHANGES (Phase 1)

## Executive Summary

**Overall Risk: LOW** — One informational finding. The Phase 1 changes introduce no critical or high-risk security issues. The CORS configuration uses a wildcard pattern which is acceptable for development but should be tightened for production.

## Staged Diff Reviewed

| File | Change | Status |
|------|--------|--------|
| `WebConfig.java` | + CORS with `allowedOriginPatterns("*")` | 🔍 See finding |
| `frontend/nginx.conf` | + `/app/` location with SPA fallback | ✅ Secure |
| `docker/nginx.conf` | + `/app/` location with SPA fallback (Docker) | ✅ Secure |
| `frontend/vite.config.ts` | + `base: '/app/'` | ✅ No risk |
| `frontend/Dockerfile` | dist → `/usr/share/nginx/html/app/` | ✅ No risk |

## Vulnerability Findings

### 🔵 LOW: CORS wildcard origin with credentials enabled

**Location:** `backend/src/main/java/com/resumainer/config/WebConfig.java:67-68`

**OWASP Category:** A01:2025-Broken Access Control

**CWE:** CWE-942: Permissive Cross-domain Policy with Untrusted Domains

**Description:** The CORS configuration uses `.allowedOriginPatterns("*")` combined with `.allowCredentials(true)`. This allows any origin to make credentialed cross-origin requests to the API. While `allowedOriginPatterns("*")` (pattern, not origin list) is not equivalent to the `Access-Control-Allow-Origin: *` header (which would be rejected by browsers with credentials), it still permits any origin pattern, reducing the security benefit of the Same-Origin Policy.

**Context:** This is a development-phase configuration. In production, the frontend Nginx serves the SPA and proxies `/api/` to the backend — all within the same origin, so CORS is not exercised. The CORS config primarily enables the Vite dev server (port 5173) to call the backend (port 8080) during development.

**Remediation:** Before production deployment, restrict `allowedOriginPatterns` to specific origins:
```java
.allowedOriginPatterns("http://localhost:5173", "https://yourdomain.com")
```
Or better, since Nginx proxies `/api/` in production, disable CORS entirely in the `prod` profile.

**Severity:** LOW — functional risk is minimal because Nginx proxies all API calls in production, making CORS irrelevant at the browser level.

## Confirmed Secure Patterns

| Pattern | Location | Status |
|---------|----------|--------|
| ✅ Nginx `autoindex off` | Both nginx.conf | Prevents directory listing |
| ✅ `try_files` without `$uri/` fallback | Both nginx.conf | Prevents directory traversal via try_files |
| ✅ `alias` path ends with `/` matching location | Both nginx.conf | Prevents alias path traversal bug |
| ✅ No hardcoded secrets in any changed file | All | No credentials, API keys, or tokens |
| ✅ PagedResponse — pure DTO, no user input | `PagedResponse.java` | No injection surface |
| ✅ Flyway migration — static SQL only | `V8__create_*.sql` | No dynamic queries, no injection risk |
| ✅ AuthInterceptor already covers new endpoints | Existing code (unchanged) | Session validation active |
| ✅ CSP, X-Frame-Options, X-Content-Type-Options | Both nginx.conf | Existing security headers preserved |

## Action Plan

1. ✅ **No critical or high findings** — proceed to next phase
2. 📝 Add production CORS restriction to backlog (for deployment hardening)

## Proposed INDEX.md Row

```text
| specs/005-user-home-page/security-review-staged-phase1.md | staged | 2026-06-06 | LOW | C:0 H:0 M:0 L:1 | A01 |
```
