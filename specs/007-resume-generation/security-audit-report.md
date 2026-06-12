---
document_type: security-review
review_type: audit
assessment_date: 2026-06-12
codebase_analyzed: ResumAIner (feat/007-resume-generation)
total_files_analyzed: 62
total_findings: 8
overall_risk: LOW
critical_count: 0
high_count: 0
medium_count: 3
low_count: 4
informational_count: 1
owasp_categories: [A01:2025-Broken Access Control, A05:2025-Security Misconfiguration, A06:2025-Insecure Design, A09:2025-Security Logging and Alerting Failures, A10:2025-Mishandling of Exceptional Conditions]
cwe_ids: [CWE-22: Path Traversal, CWE-200: Information Exposure, CWE-352: Cross-Site Request Forgery, CWE-770: Allocation of Resources Without Limits, CWE-862: Missing Authorization]
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

# SECURITY AUDIT REPORT

## Executive Summary

**Overall Security Posture:** LOW RISK
**Assessment Date:** 2026-06-12
**Codebase Analyzed:** ResumAIner — feat/007-resume-generation (spec.md, plan.md, tasks.md + existing backend)
**Total Files Analyzed:** 62 (58 existing Java + Vue files, 4 feature artifacts)
**Total Findings:** 8

### Findings by Severity

| Severity      | Count |
| ------------- | ----- |
| Critical      | 0     |
| High          | 0     |
| Medium        | 3     |
| Low           | 4     |
| Informational | 1     |

### Risk Summary

The comprehensive audit confirms that Feature 007 (Resume Generation) is planned with strong security awareness. The existing backend already implements essential controls: CSRF protection via OWASP cookie-to-header pattern (CsrfFilter), authentication interceptor (AuthInterceptor), BCrypt password hashing (PasswordService), PreparedStatement-based DAO pattern, and centralized error handling (GlobalExceptionHandler, AuthExceptionHandler).

The feature plan explicitly addresses all high-impact security concerns: owner-scoped access for all 10+ endpoints, API key masking and no-log rules, XSS sanitization for AI-generated HTML, and public route safety (410 Gone for deleted resumes). No critical or high-severity gaps were identified in the plan.

Three medium-severity items require attention during implementation: prompt render log access control enforcement, file path traversal prevention in GeneratedFileStorageService, and rate limiting on request creation. All are already noted in the plan as SEC-001 through SEC-005 and have corresponding tasks in tasks.md.

---

## Vulnerability Findings

### MEDIUM: A01-001 — Prompt Render Log PII Exposure

**Finding ID:** SEC-AUDIT-001
**Location:** `specs/007-resume-generation/plan.md` (SEC-001), `specs/007-resume-generation/tasks.md` (T044, T109A)
**OWASP Category:** A01:2025-Broken Access Control
**CWE:** CWE-200: Information Exposure
**CVSS Score:** 5.3 (Medium)

#### Description

The `ai_prompt_render_log` table stores rendered prompts containing user profile data (name, contact info, work history, education) and the full vacancy text. If exposed through an API endpoint without explicit access control, this constitutes a PII data leak. The plan correctly identifies this risk (SEC-001) and tasks T044 and T109A address it, but the enforcement depends on correct implementation rather than architectural prevention.

#### Affected Artifacts

- Plan.md SEC-001: Correctly identifies risk but relies on "no frontend endpoint" as a manual implementation constraint
- Tasks T044: Stores rendered prompt log
- Tasks T109A: Searches for forbidden prompt-log endpoints
- No architectural constraint prevents an accidental controller from exposing this data

#### Impact

An attacker or confused user who discovers a render-log endpoint could access the full profile data and vacancy text of any generation request, violating user privacy and potentially exposing PII.

#### Remediation

1. Ensure `AiUsageLogDao` and any render-log DAO are NOT injected into any controller
2. Add a package-level or architectural note: "ai_prompt_render_log DAO must only be used by service layer, never by controller"
3. Extend T109A to include a code review checklist item: "Verify no @RestController has a @Autowired PromptRenderLogDao or similar"
4. Consider adding an Aspect that logs and blocks any attempt to expose render log data through web endpoints

**Spec-Kit Task:** Already covered by T044, T078, T109A, T128G. Consider adding a REVIEW gate before controller implementation.

---

### MEDIUM: A01-002 — File Path Traversal in GeneratedFileStorageService

**Finding ID:** SEC-AUDIT-002
**Location:** `specs/007-resume-generation/tasks.md` (T070)
**OWASP Category:** A01:2025-Broken Access Control
**CWE:** CWE-22: Path Traversal
**CVSS Score:** 5.3 (Medium)

#### Description

`GeneratedFileStorageService` constructs file paths under `generated_results/{username}/{public_code}/`. While the plan specifies path sanitization, the implementation must explicitly prevent path traversal through the username segment. Since username comes from the database (not user input), the risk is lower, but a compromised admin account or future username-update feature could introduce traversal vectors.

#### Affected Artifacts

- Plan.md SEC-002: Correctly identifies the risk
- Tasks T070: "Sanitize username segment against path traversal" — correct but brief
- Tasks T128: Test for path traversal

#### Impact

An attacker with database write access (or exploiting a username-update vulnerability) could write HTML/PDF files outside the intended `generated_results/` directory, potentially overwriting system files or web application resources.

#### Remediation

1. In T070, add explicit implementation guidance:
   - Strip `../`, `./`, null bytes, and path separator characters from username
   - Use `Path.normalize()` and verify resolved path starts with expected base directory
   - Use a allowlist of allowed characters for username segments (alphanumeric + hyphen + underscore)
2. The `public_code` is server-generated alphanumeric — validate format with regex `^[A-Za-z0-9]{5,8}$`
3. Add integration test that verifies even with a crafted username, the resolved path stays within the base directory

**Spec-Kit Task:** Already covered by T070, T128. Strengthen T070 implementation guidance.

---

### MEDIUM: A06-001 — Rate Limiting on Request Creation

**Finding ID:** SEC-AUDIT-003
**Location:** `specs/007-resume-generation/plan.md` (SEC-004), `specs/007-resume-generation/tasks.md`
**OWASP Category:** A06:2025-Insecure Design
**CWE:** CWE-770: Allocation of Resources Without Limits
**CVSS Score:** 4.3 (Medium)

#### Description

The plan limits active generation to one per user, but does not limit the number of draft generation requests a user can create. A malicious user could call `POST /api/generate/requests` repeatedly, creating unlimited database rows and potentially exhausting storage or causing a slow degradation of database performance. The plan acknowledges this as an acceptable MVP risk (SEC-004).

#### Affected Artifacts

- Plan.md SEC-004: Acceptable risk for MVP, documented gap
- No task explicitly implements rate limiting on request creation

#### Impact

Database bloat from abandoned draft requests. At MVP scale with limited users, the impact is minimal. At production scale (1000+ users), this could lead to degraded query performance and increased storage costs.

#### Remediation

1. For MVP: Accept as documented risk. The SEC-005 one-active-generation rule provides partial protection for the expensive operation (AI calls).
2. Optional enhancement: Add a soft limit of 50 active (non-completed, non-deleted) requests per user in `GenerationRequestService.createRequest()`. Return a user-readable message when exceeded.
3. Add a database cleanup job (cron or scheduled) for abandoned requests older than 30 days.

**Spec-Kit Task:** Document as part of SEC-004. Add optional task for soft limit if time permits.

---

### LOW: A05-001 — PreparedStatement Enforcement Lacks Automated Verification

**Finding ID:** SEC-AUDIT-004
**Location:** All DAO tasks (Phase 4)
**OWASP Category:** A05:2025-Injection
**CWE:** CWE-89: SQL Injection
**CVSS Score:** 3.7 (Low)

#### Description

The plan and tasks consistently require PreparedStatement-only SQL (Constitution IV). However, there is no automated verification mechanism to detect accidentally introduced string-concatenated SQL. The existing codebase already follows this pattern correctly, but new DAO implementations in Phase 4 could introduce injection vulnerabilities without automated detection.

#### Affected Artifacts

- Plan.md Constitution Check: "PreparedStatement-only SQL"
- Tasks T042: "Code review: all DAO SQL uses PreparedStatement"
- No static analysis or linter rule enforces this

#### Impact

If a developer accidentally uses string concatenation in a DAO method, it would create a SQL injection vulnerability. Manual code review (T042) is the only defense.

#### Remediation

1. Add a PMD or Checkstyle rule that flags string concatenation in `dao/` package files
2. Alternatively, add a code review checklist item in T042 specifically: "Search for `+` operators inside SQL strings in all new DAO files"
3. Add a simple unit test utility that scans DAO source files for "+ \"" patterns

**Spec-Kit Task:** Enhance T042 with explicit SQL injection prevention checklist items.

---

### LOW: A01-003 — Public Placeholder Link Could Be Confused with Real Link

**Finding ID:** SEC-AUDIT-005
**Location:** `specs/007-resume-generation/spec.md` (US6, US7)
**OWASP Category:** A01:2025-Broken Access Control
**CWE:** CWE-862: Missing Authorization
**CVSS Score:** 2.6 (Low)

#### Description

In feat/007, the Export UI provides a "Copy public link" action that copies a placeholder URL (not a real public PDF link). If the placeholder URL is confusingly similar to the future real URL pattern, users may bookmark or share it, leading to broken links when feat/008 deploys. More critically, if the placeholder URL accidentally follows the same pattern as the future public route, it could create naming conflicts.

#### Affected Artifacts

- spec.md US6 acc. 3: "a safe placeholder link is copied"
- tasks.md T101: Placeholder URLs in Export DTO
- No explicit requirement for placeholder URL format

#### Impact

Low. Users may experience broken bookmarks if the placeholder URL format changes in feat/008.

#### Remediation

1. Make the placeholder URL visually distinct from the real future URL (e.g., `/placeholder/public-link` vs `/candidate/{publicCode}`)
2. The Export DTO `pdfMessage` field should clearly state: "Public resume links will be available in a future update"
3. Ensure the placeholder link route returns a clear "not available" page, not a 404 or redirect that could be mistaken

**Spec-Kit Task:** Add note to T101 about placeholder URL design.

---

### LOW: A09-001 — AI Usage Log Lacks Read-Back Protection

**Finding ID:** SEC-AUDIT-006
**Location:** `specs/007-resume-generation/tasks.md` (T038A, T061A)
**OWASP Category:** A09:2025-Security Logging and Alerting Failures
**CWE:** CWE-200: Information Exposure
**CVSS Score:** 2.5 (Low)

#### Description

The AI Usage Log (T038A) stores `tokens_sent` and `tokens_generated` per user and model. While this does not contain PII, it reveals usage patterns. The task correctly specifies "no frontend read endpoint for usage logs in MVP," but this is an implementation constraint rather than an architectural enforcement.

#### Affected Artifacts

- T038A: "No frontend read endpoint for usage logs in MVP"
- T061A: Writes usage log after AI call

#### Impact

Low. If exposed, usage logs reveal which AI models a user has access to and how many tokens they consume. This is not PII but could inform a competitor about system usage patterns.

#### Remediation

1. Ensure `AiUsageLogDao` is not injected into any controller class
2. Add a code review item: "Verify AiUsageLogDao usage is limited to service layer"
3. Admin dashboard for usage statistics is a separate post-MVP feature

**Spec-Kit Task:** Add to T109A scope (already covers render log access, extend to usage log).

---

### LOW: A10-001 — PDF Stub Error Handling Consistency

**Finding ID:** SEC-AUDIT-007
**Location:** `specs/007-resume-generation/tasks.md` (T076, T085)
**OWASP Category:** A10:2025-Mishandling of Exceptional Conditions
**CWE:** — (Design issue)
**CVSS Score:** 2.1 (Low)

#### Description

The `NoOpPdfGenerationService` stub (T076) and the PDF download placeholder stub (T085) must return consistent, safe error responses. If the stub throws an exception instead of returning a controlled error response, it could trigger the global exception handler and potentially leak internal details.

#### Affected Artifacts

- T076: "returns a clear 'PDF generation not available in this feature' response"
- T085: "Returns safe 'PDF generation not available yet' response"
- No explicit requirement about the HTTP status code or response format

#### Impact

Low. An improperly implemented stub could return a 500 error with a stack trace instead of a controlled 501 Not Implemented or 200 with error message.

#### Remediation

1. Specify the placeholder stub behavior explicitly: `PdfGenerationService` should return a controlled response object (not throw an exception) with `success=false` and `message="PDF generation is not available in this version. It will be available in a future update."`
2. The PDF download endpoint should return HTTP 501 Not Implemented with a JSON body containing the message, not a 500 Internal Server Error
3. No stack trace should be included in any PDF placeholder response

**Spec-Kit Task:** Add to T076, T085 implementation guidance.

---

### INFORMATIONAL: Existing Security Controls Confirmed

**Finding ID:** SEC-AUDIT-008
**Location:** Existing `backend/src/main/java/com/resumainer/`
**OWASP Category:** — (Positive finding)

#### Description

The existing codebase already implements the following security controls, which Feature 007 builds upon:

| Control | File | Status |
|---------|------|--------|
| CSRF Protection (OWASP cookie-to-header) | `filter/CsrfFilter.java` | ✅ Implemented |
| Authentication Interceptor | `interceptor/AuthInterceptor.java` | ✅ Implemented |
| BCrypt Password Hashing | `service/PasswordService.java` | ✅ Implemented |
| PreparedStatement DAO Pattern | All `dao/*.java` files | ✅ Consistent |
| Global Exception Handler (no stack traces) | `exception/GlobalExceptionHandler.java` | ✅ Implemented |
| Auth-specific Error Handler | `exception/AuthExceptionHandler.java` | ✅ Implemented |
| Service Exceptions Layer | `exception/ServiceException.java` | ✅ Implemented |
| Connection Pool with Thread Safety | `infrastructure/db/SimpleConnectionPool.java` | ✅ Implemented |

These controls provide a secure foundation for the generation feature. Feature 007 must extend the same patterns for all new endpoints, DAOs, and services. Key patterns to follow:
- DAOs must use the existing `PreparedStatement` pattern from existing DAOs like `WorkExperienceDao`
- Controllers must respect `AuthInterceptor` for session validation
- Error handling should use `ServiceException` for business logic errors
- CSRF tokens must be handled via the shared `httpClient.ts` (established in Feature 006)

---

## Architecture Risks

### Risk Category: Trust Boundaries

#### Risk Description — Generated File Storage

The generated HTML files are stored on the server filesystem under `generated_results/{username}/{public_code}/`. This creates a trust boundary between the filesystem and the web application. The files are served through owner-scoped download endpoints, but the files themselves are on disk and could be accessed through misconfigured web server rules.

#### Affected Components

- `GeneratedFileStorageService` (writes files)
- `GET /api/resumes/{id}/html` (serves files)
- Nginx configuration (must not expose `generated_results/` directory)

#### Risk Assessment

**Likelihood:** Low
**Impact:** Medium (if Nginx exposes the directory, all HTML resumes could be world-readable)
**Risk Level:** Low

#### Mitigation Recommendations

1. Ensure Nginx does NOT serve the `generated_results/` directory directly — all access must go through the authenticated backend endpoint
2. Add a note to Docker/Nginx configuration: "Block direct access to /generated_results/ path"
3. Consider storing generated files outside the web root entirely

---

## Missing Security Controls

| Control | Status | Priority | Recommendation |
|---------|--------|----------|----------------|
| Content Security Policy (CSP) | ⚠️ Not verified | Medium | Should be reviewed for the Vue SPA when PDF/HTML download actions are present |
| Rate Limiting on Request Creation | ❌ Missing (MVP gap) | Low | Optional soft limit of 50 active requests per user |
| Static Analysis for SQL Injection | ❌ Missing | Low | Add PMD or manual review step in T042 |
| Automated Secret Scanning in CI | ❌ Missing | Informational | Future improvement for CI/CD pipeline |
| Security Headers Audit | ⚠️ Not verified | Informational | Should include in Docker deployment review |

---

## Dependency Risks

No production dependencies have been added for this feature beyond the existing stack (Spring MVC, Flyway, PostgreSQL, Vue 3, PrimeVue). The PDF library selection is explicitly deferred to feat/008 and requires a separate dependency audit at that time.

---

## Secrets Detection

| Type | Location | Risk | Status |
|------|----------|------|--------|
| OpenRouter API Key | `ai_model.api_key_encrypted` (DB) | HIGH | 🔒 Mitigated: encrypted at rest, masked in UI, never logged |
| Database Credentials | Environment variables | MEDIUM | 🔒 Mitigated: `System.getenv()` pattern, not in source code |
| Session Secret | Configuration | MEDIUM | ✅ Managed via environment, not committed |

No hardcoded secrets were found in source code. All sensitive credentials are handled through environment variables or encrypted storage.

---

## STRIDE Threat Model Summary

| Component | Spoofing | Tampering | Repudiation | Info Disclosure | DoS | Elevation of Privilege |
|-----------|----------|-----------|-------------|-----------------|-----|------------------------|
| Generation Request API | 🟢 | 🟢 | 🟢 | 🟡 (owner check) | 🟡 (one active gen) | 🟢 |
| AI Model API | 🟢 | 🟢 | 🟢 | 🟡 (model metadata) | 🟢 | 🟡 (privilege filter) |
| Review/Edit API | 🟢 | 🟡 (edit validation) | 🟢 | 🟡 (owner check) | 🟢 | 🟢 |
| Finalize API | 🟢 | 🟡 (HTML integrity) | 🟢 | 🟡 (owner check) | 🟢 | 🟢 |
| HTML Download | 🟢 | 🟢 | 🟢 | 🟡 (owner check) | 🟢 | 🟢 |
| PDF Stub | 🟢 | 🟢 | 🟢 | 🟢 (placeholder) | 🟢 | 🟢 |
| Public Link (feat/007) | 🟢 | 🟢 | 🟢 | 🟢 (placeholder only) | 🟢 | 🟢 |
| Database | 🟡 (connection pool) | 🟡 (transactions) | 🟢 | 🔴 (encrypted at rest?) | 🟡 | 🟢 |
| AI Provider (OpenRouter) | 🟡 (API key) | 🟡 (response validation) | 🟡 (render log) | 🟡 (prompt data) | 🔴 (provider timeout) | 🟢 |

**Legend:** 🔴 High Risk | 🟡 Medium Risk | 🟢 Low Risk

---

## Spec-Kit Alignment Updates

### Generated Remediation Tasks

| Task ID | Severity | Category | Description | Recommended Phase |
|---------|----------|----------|-------------|-------------------|
| TASK-SEC-AUDIT-001 | Medium | A01-Access Control | Add architectural note preventing render-log DAO injection into controllers | Phase 7 (Code Review) |
| TASK-SEC-AUDIT-002 | Medium | A01-Access Control | Strengthen path traversal prevention with allowlist + integration test | Phase 8 (File Storage) |
| TASK-SEC-AUDIT-003 | Medium | A06-Insecure Design | Consider soft limit of 50 draft requests per user (optional) | Phase 6 (Request API) |
| TASK-SEC-AUDIT-004 | Low | A05-Injection | Add SQL injection prevention checklist to T042 code review | Phase 4 (DAO Review) |
| TASK-SEC-AUDIT-005 | Low | A01-Access Control | Make placeholder public link visually distinct from real URL | Phase 11 (Export UI) |
| TASK-SEC-AUDIT-006 | Low | A09-Logging | Extend T109A to verify AiUsageLogDao is not controller-injected | Phase 7 (Code Review) |
| TASK-SEC-AUDIT-007 | Low | A10-Error Handling | PDF stub must return HTTP 501, not 500; no stack traces | Phase 8 (PDF Boundary) |

---

## Conclusion

Feature 007 (Resume Generation) passes the comprehensive security audit with **LOW overall risk**.

**Strengths:**
- Owner-scoped access enforced on all endpoints (plan Security Boundaries table)
- CSRF protection via existing CsrfFilter + httpClient.ts pattern
- PreparedStatement-only SQL mandated by constitution and plan
- API key masking and no-log rules explicitly specified
- XSS sanitization for AI-generated content required
- Error screen prohibits raw provider errors (FR-GEN-045)
- Security review performed pre-implementation (security-review-plan.md)
- All 5 security findings from previous review have corresponding tasks

**Areas to watch:**
- Prompt render log access control depends on implementation discipline, not architecture
- File path traversal prevention must be verified during implementation
- Rate limiting on request creation is an accepted MVP gap

**Next steps:**
1. Proceed with implementation following the established security patterns
2. Ensure all TDD tasks include security-focused test cases
3. Run `/speckit.security-review.followup` if any critical issues emerge during implementation
4. Before deploying feat/008 (PDF conversion), perform a dedicated security review of the PDF library and public route

---

## Memory Hub INDEX.md Row

```text
| specs/007-resume-generation/security-audit-report.md | audit | 2026-06-12 | LOW | C:0 H:0 M:3 L:4 I:1 | A01,A05,A06,A09,A10 |
```
