---
document_type: security-review
review_type: audit
assessment_date: 2026-06-07
codebase_analyzed: ResumAIner — Full project audit (backend + frontend + planning artifacts)
total_files_analyzed: 15
total_findings: 6
overall_risk: MODERATE
critical_count: 0
high_count: 1
medium_count: 2
low_count: 3
informational_count: 0
owasp_categories: [A01, A02, A04, A05, A06, A09]
cwe_ids: [CWE-639, CWE-200, CWE-524, CWE-276, CWE-942, CWE-532]
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

# SECURITY REVIEW REPORT — Full Project Audit

## Executive Summary

**Overall Security Posture:** MODERATE RISK
**Assessment Date:** 2026-06-07
**Codebase Analyzed:** ResumAIner (Java + Vue + PostgreSQL)
**Total Files Analyzed:** 15 (planning artifacts, backend source, frontend source, config)
**Total Findings:** 6 (0 Critical, 1 High, 2 Medium, 3 Low)

### Findings by Severity

| Severity | Count | Percentage |
|---|---|---|
| Critical | 0 | 0% |
| High | 1 | 17% |
| Medium | 2 | 33% |
| Low | 3 | 50% |

### Risk Summary

The project has a solid security foundation: PreparedStatement-only SQL access, session-based authentication with CSRF protection, global exception handler without stack trace exposure, and a security-aware planning process (SEC-001 through SEC-005). The plan review findings from 2026-06-07 have been integrated into the tasks.

The main risk is the **permissive CORS configuration** (`allowedOriginPatterns("*")` with credentials) which effectively disables cross-origin protection. While acceptable for local development with Vite, this must be tightened before VPS deployment. Additionally, the `AuthExceptionHandler` returns 500 for unexpected errors with a generic message — better than exposing internals, but some error codes leak implementation details. The Feature 006 plan properly addresses all known risks through SEC-001 (owner-scoped access), SEC-002 (PII-safe logging), and SEC-005 (cache-control headers).

No SQL injection, command injection, or authentication bypass vectors were identified in the existing codebase.

---

## Vulnerability Findings

### [HIGH] CORS Wildcard with Credentials

**Finding ID:** SEC-006
**Location:** `backend/src/main/java/com/resumainer/config/WebConfig.java:68-73`
**OWASP Category:** A01:2025-Broken Access Control
**CWE:** CWE-942 — Permissive Cross-domain Policy with Untrusted Domains
**CVSS Score:** 7.5

#### Description

CORS is configured with `allowedOriginPatterns("*")` together with `allowCredentials(true)`. This combination allows any external origin to make authenticated requests with credentials (cookies). While `.allowedOriginPatterns("*")` does not set the literal `Access-Control-Allow-Origin: *` header (it echoes the request origin), in combination with credentials it permits any website to make cross-origin authenticated requests if the user has an active session.

#### Affected Code

```java
registry.addMapping("/api/**")
        .allowedOriginPatterns("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
```

#### Exploit Scenario

1. Attacker crafts a malicious webpage that makes PUT requests to `https://resumainer-vps.com/api/profile/contact` with a victim's credentials
2. If the victim visits the malicious page while logged into ResumAIner, the browser sends the session cookie
3. The server's CORS config allows the request because `allowedOriginPatterns("*")` accepts any origin with credentials
4. Attacker exfiltrates the victim's profile data via the response

#### Impact

Personal data (full name, email, phone, DOB, citizenship) could be exfiltrated from any authenticated user's profile via a cross-origin attack.

#### Remediation

Replace the wildcard with explicit allowed origins based on deployment environment:

```java
// Development: allow Vite dev server
// Production: restrict to actual frontend origin
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:5173",      // Vite dev
                "http://localhost:8080",       // Docker backend
                "https://resumainer.com"       // Production
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("Content-Type", "X-CSRF-Token", "Accept")
            .allowCredentials(true)
            .maxAge(3600);
}
```

**Spec-Kit Task:** TASK-SEC-006

---

### [MEDIUM] Error Response Information Disclosure

**Finding ID:** SEC-007
**Location:** `backend/src/main/java/com/resumainer/exception/AuthExceptionHandler.java`
**OWASP Category:** A05:2025-Security Misconfiguration
**CWE:** CWE-200 — Exposure of Sensitive Information

#### Description

The `AuthExceptionHandler` catches `ServiceException` and returns HTTP status codes based on error codes (409, 400, 401, 423, 403, 500). While the global handler (NFR-003) properly strips stack traces, the error codes themselves may reveal internal state: for example, returning 423 (Locked) vs 401 (Unauthorized) tells an attacker whether the account exists vs is locked.

#### Impact

Low — the error codes are standard HTTP semantics and not custom codes. However, the differentiation between 401/403/423 could theoretically be used for account enumeration in a prolonged attack.

#### Remediation

Accept as-is for MVP. This is standard Spring MVC behavior and follows REST conventions. The existing rate limiting on login (5 attempts, 15 min lockout) mitigates enumeration risk.

**Spec-Kit Task:** None — accepted risk.

---

### [MEDIUM] Profile Backup and Recovery Not Addressed

**Finding ID:** SEC-008
**Location:** `specs/006-user-profile/`, `data-model.md`
**OWASP Category:** A06:2025-Insecure Design
**CWE:** CWE-276 — Incorrect Default Permissions

#### Description

The soft-delete mechanism (SEC-003) protects against accidental deletion but there is no planned mechanism for restoring soft-deleted records. A user who accidentally deletes a work experience entry has no way to undo this within the application — only a database administrator could set `is_deleted = FALSE`. The plan does not include an "undo" or "recycle bin" feature.

#### Impact

Low for MVP — users can re-enter data. The data is not permanently lost (soft-delete keeps it). This is a UX gap rather than a security vulnerability.

#### Remediation

Document in the plan that soft-deleted records can be recovered via direct database update during MVP. Consider adding a "Recently Deleted" section post-MVP.

**Spec-Kit Task:** None — deferred to post-MVP.

---

### [LOW] Profile Data in Error Responses

**Finding ID:** SEC-009
**Location:** `specs/006-user-profile/contracts/api.md`, general
**OWASP Category:** A09:2025-Security Logging & Alerting Failures
**CWE:** CWE-532 — Insertion of Sensitive Information into Log File

#### Description

The plan (SEC-002) correctly prohibits logging PII in ProfileService. However, the `ContactDetailDao` (existing) logs `userId` in error messages. This is acceptable (user ID is not PII), but developers must remain vigilant not to log field values when implementing new Profile DAOs.

#### Impact

Low — the plan already addresses this with SEC-002 and T022. This is a reminder to enforce the practice during implementation review.

#### Remediation

Already covered by T022 (`Methods MUST NOT log PII`) and T048 (security verification gate). Ensure code review checks for accidental PII logging.

**Spec-Kit Task:** T022 (already exists), T048 (already exists)

---

### [LOW] No Content Security Policy Headers

**Finding ID:** SEC-010
**Location:** `WebConfig.java` (response headers section)
**OWASP Category:** A02:2025-Security Misconfiguration
**CWE:** CWE-524 — Information Exposure Through Caching

#### Description

No Content-Security-Policy header is configured on the backend responses. While the Vue SPA renders user-provided content (resume data, AI-generated output), there is no CSP to mitigate XSS in AI-generated content.

#### Impact

Low for MVP — AI-generated content is reviewed by the user before saving (in Resume Review phase, not in Profile). Profile data is user-entered, not AI-generated, so XSS risk is minimal. CSP would add defense-in-depth.

#### Remediation

Add CSP header in Nginx (Docker) or via a filter in the backend:

```
Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;
```

Defer to post-MVP hardening sprint.

**Spec-Kit Task:** None — deferred.

---

### [LOW] Session Timeout Not Explicit for Profile Endpoints

**Finding ID:** SEC-011
**Location:** `AppInitializer.java`
**OWASP Category:** A07:2025-Authentication Failures
**CWE:** N/A

#### Description

The `AppInitializer` sets session timeout to 30 minutes. Profile data operations (filling forms, adding records) may take longer than 30 minutes, potentially causing session expiration and data loss on save.

#### Impact

Low — the user would see a 401 response and need to re-login. Form data might be lost if not auto-saved (the spec explicitly prohibits autosave). This is a UX concern more than a security concern.

#### Remediation

Consider increasing session timeout for profile operations to 60 minutes, or implementing a "session about to expire" warning in the frontend before form submission.

**Spec-Kit Task:** None — optional UX improvement.

---

## Architecture Risks

### Risk Category: Trust Boundaries

#### Risk Description

The Profile feature introduces new data flow: Vue SPA → ProfileController → ProfileService → 6 DAOs → PostgreSQL. The trust boundary between frontend and backend is properly protected by session auth + CSRF. The trust boundary between service and DAO layer is protected by owner-scoped queries (WHERE user_id = ?).

**Assessment**: Well-structured. No architecture risks identified beyond SEC-001 (already addressed in tasks).

---

## Missing Security Controls

| Control | Status | Priority | Recommendation |
|---|---|---|---|
| Content Security Policy | ❌ Missing | Low | Add CSP to Nginx config post-MVP |
| Rate Limiting (Profile API) | ❌ Missing | Low | Not needed for MVP (single user editing own profile) |
| CORS Restriction | ⚠️ Partial | HIGH | Must restrict to explicit origins before production |
| Security Logging (Profile) | ✅ Planned | Medium | SEC-002 encoded in T022 and T048 |
| Cache Control | ✅ Planned | Low | SEC-005 encoded in T023 and T048 |

---

## Dependency Risks

| Package | Version | Risk | Notes |
|---|---|---|---|
| Spring MVC | 6.2.11 | 🟢 Low | Maintained, no known CVEs for this version |
| PostgreSQL JDBC | 42.x | 🟢 Low | Maintained |
| Flyway | 10.x | 🟢 Low | Maintained |
| Vue 3 | 3.x | 🟢 Low | Maintained |
| PrimeVue | 4.x | 🟢 Low | Maintained |

No significant supply chain risks identified. All core dependencies are actively maintained.

---

## Secrets Detection

| Type | Location | Risk | Status |
|---|---|---|---|
| API Keys | `.env` (local) | 🟡 Medium | ✅ Not committed to Git |
| Database Password | `.env` (local) | 🟡 Medium | ✅ Not committed to Git |
| OpenRouter API Key | Not yet configured | 🟡 Medium | Will be handled in AI integration feature |

No hardcoded secrets found in source code.

---

## STRIDE Threat Model Summary

| Component | Spoofing | Tampering | Repudiation | Info Disclosure | DoS | Elevation of Privilege |
|---|---|---|---|---|---|---|
| Auth API | 🟢 | 🟢 | 🟡 | 🟢 | 🟡 | 🟢 |
| Profile API | 🟢 | 🟢 | 🟡 | 🟡 | 🟢 | 🟡 |
| Profile DAOs | 🟢 | 🟢 | 🟡 | 🟢 | 🟢 | 🟢 |
| Database | 🟢 | 🟢 | 🟢 | 🟡 | 🟡 | 🟢 |
| Vue Frontend | 🟢 | 🟢 | 🟢 | 🟡 | 🟢 | 🟢 |

**Legend:** 🟢 Low Risk | 🟡 Medium Risk | 🔴 High Risk

---

## Spec-Kit Alignment Updates

### Security Coverage in Tasks

| Task ID | Security Reference | Coverage |
|---|---|---|
| T014-T019 | SEC-001 (owner-scoped DAO) | ✅ All DAOs include WHERE user_id = ? |
| T022 | SEC-002 (PII logging) | ✅ ProfileService MUST NOT log PII |
| T023 | SEC-005 (Cache-Control) | ✅ Controller returns no-store, private |
| T001-T004, T014-T019 | SEC-003 (soft-delete) | ✅ is_deleted + deleted_at in migrations and DAOs |
| T007 | SEC-004 (username unique) | ✅ DB UNIQUE constraint + DAO exception handling |
| T048 | All SEC findings | ✅ REVIEW gate verifies all 5 SEC items |

### Remediation Tasks (New)

| Task ID | Severity | Category | Description | Recommended Phase |
|---|---|---|---|---|
| TASK-SEC-006 | HIGH | CORS | Restrict CORS allowed origins to explicit list before production deployment | Pre-deployment |

---

## Action Plan

1. **Immediate (pre-deployment):** Restrict CORS configuration (SEC-006) to explicit allowed origins
2. **During implementation:** Enforce SEC-001 through SEC-005 per existing tasks T014-T048
3. **During code review:** Verify PII logging constraint (SEC-002) in all new Profile DAOs and Services
4. **Post-MVP:** Add CSP headers, consider session timeout extension for profile operations

## INDEX.md Row

```
| docs/security-reviews/2026-06-07-full-audit.md | audit | 2026-06-07 | MODERATE | C:0 H:1 M:2 L:3 | A01,A02,A05,A06,A09 |
```
