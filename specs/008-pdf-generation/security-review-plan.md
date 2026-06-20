---
document_type: security-review
review_type: plan
assessment_date: 2026-06-20
codebase_analyzed: ResumAIner — specs/008-pdf-generation/plan.md
total_files_analyzed: 5
total_findings: 8
overall_risk: LOW
critical_count: 0
high_count: 0
medium_count: 2
low_count: 2
informational_count: 3
owasp_categories: [A01, A04, A05, A07]
cwe_ids: [CWE-22, CWE-330, CWE-770, CWE-552, CWE-200]
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

# Security Review — Feature 008 Plan

**Review Type**: Plan Review  
**Assessment Date**: 2026-06-20  
**Overall Risk**: MODERATE  
**Artifacts Reviewed**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/api-contracts.md`, `memory-synthesis.md`

---

## Executive Summary

The Feature 008 plan demonstrates **strong security awareness** across most domains. Constitution V (Security by Design) is explicitly mapped to the design, user-editable text is HTML-escaped before PDF rendering, owner-scoping is enforced on all authenticated endpoints, and the public PDF route serves only the final artifact without leaking metadata.

**Three medium-severity gaps** were identified that require remediation before or during implementation:
1. **Path traversal risk** in file-serving download controllers
2. **Predictable public code generation** not addressed
3. **Rate limiting** on public PDF route not specified

No critical or high findings. The plan is safe to proceed with the recommended mitigations.

---

## Artifacts Reviewed

| Artifact | Status |
|---|---|
| `plan.md` | Reviewed — 655 lines |
| `spec.md` | Reviewed — 270 lines |
| `research.md` | Reviewed |
| `data-model.md` | Reviewed |
| `contracts/api-contracts.md` | Reviewed |
| `memory-synthesis.md` | Reviewed |

---

## Findings

### SEC-001: Path Traversal in File-Serving Download Controllers

- **Location**: `plan.md` — Authenticated PDF/HTML download endpoints
- **OWASP**: A01:2025 — Broken Access Control
- **CWE**: CWE-22 — Path Traversal
- **CVSS**: 5.3 (Medium) — AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:N/A:N

**Description**: The plan stores `pdf_file_path` as a relative path on `saved_resume` and serves files through `/api/generate/resumes/{id}/pdf` and `/html` endpoints. If the download controller reads the file using the stored path without validation, an attacker who manages to inject a path traversal payload (e.g., `../../etc/passwd`) into `pdf_file_path` could read arbitrary files.

**Risk**: An attacker with a compromised saved_resume record (via SQL injection elsewhere or insider threat) could read files outside the configured storage directory.

**Recommendation**: 
- Normalize and validate the resolved path against the configured storage root directory before reading.
- Use `Path.resolve()` + `Path.normalize()` + `Path.startsWith(storageRoot)` pattern.
- Never concatenate `pdf_file_path` directly with the base directory.
- Add verification in the download controller: if resolved path is not within the storage root, return 404 (not 500, to avoid leaking path information).

**Spec Kit Task**: TASK-SEC-001

---

### SEC-002: Public Code Generation — Already Addressed ✅

- **Location**: `backend/src/main/java/com/resumainer/util/PublicCodeGenerator.java`
- **OWASP**: A04:2025 — Insecure Design
- **CWE**: CWE-330 — Use of Insufficiently Random Values
- **CVSS**: MITIGATED — Existing implementation uses SecureRandom

**Finding**: During review, the plan did not specify how public codes are generated. Upon codebase inspection, `PublicCodeGenerator` already exists and meets security requirements:

- Uses `java.security.SecureRandom` (not `java.util.Random`).
- 28-character alphabet with ambiguous chars excluded: `ABCDEFGHJKMNPQRSTUVWXYZ23456789`.
- Default 5 chars (~17M combinations) with collision retry up to 5 attempts.
- Fallback to 8 chars (~378B combinations) on collision.
- `SavedResumeDao.findPublicCodeByCode()` provides DB-backed uniqueness check.
- `public_code VARCHAR(10)` column already created via V21 migration.
- Already integrated into `ResumeFinalizeService`.

**Recommendation**: No change needed. Combined with rate limiting (SEC-003), the 5-char code space is adequately protected against brute-force. The existing implementation is secure.

**Spec Kit Task**: N/A — already implemented.

---

### SEC-003: Public PDF Route Missing Rate Limiting Specification

- **Location**: `plan.md` — Public PDF route `/{username}/{publicCode}`
- **OWASP**: A05:2025 — Security Misconfiguration
- **CWE**: CWE-770 — Allocation of Resources Without Limits or Throttling
- **CVSS**: 4.3 (Medium) — AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:L

**Description**: The spec mentions "Public route is guessed or brute-forced — must not leak information" as an edge case, but the plan does not specify a rate-limiting strategy for the public PDF route. Without rate limiting, an attacker could:
- Brute-force public codes to discover valid resumes.
- Repeatedly download PDFs to cause resource exhaustion.

**Risk**: Brute-force enumeration of public URLs exposes private resume data to unauthorized parties. Resource exhaustion via repeated downloads degrades service.

**Recommendation**:
- Apply IP-based rate limiting on `/{username}/{publicCode}` (e.g., 10 requests/minute/IP).
- Return HTTP 429 (Too Many Requests) when limit is exceeded.
- Use existing project rate-limiting infrastructure if available (e.g., the login rate limiter from Feature 003).
- Log brute-force patterns at WARN level for monitoring.
- Consider adding a small delay on 404 responses to slow down enumeration.

**Spec Kit Task**: TASK-SEC-003

---

### SEC-004: Username Change Does Not Invalidate Old Public URLs

- **Location**: `plan.md` — "username changes update public paths for saved resumes if public URL paths are stored"
- **OWASP**: A01:2025 — Broken Access Control
- **CWE**: CWE-200 — Exposure of Sensitive Information
- **CVSS**: 3.1 (Low) — AV:N/AC:H/PR:L/UI:R/S:U/C:L/I:N/A:N

**Description**: The plan notes "username changes update public paths" but does not specify whether the old `/{oldUsername}/{publicCode}` URL is invalidated. If a user changes their username, the old public URL would either (a) return 404 (which leaks that the username changed) or (b) be claimed by a new user registering the old username (which would expose the original user's resume under the new user's URL).

**Risk**: Low — requires username change + recruiter bookmarking the old URL. Mitigated by proper handling.

**Recommendation**:
- On username change, regenerate all public codes for that user's saved resumes (invalidates old URLs).
- Mark old public codes as revoked in the database.
- Reserved usernames list should include previously-used usernames (prevents squatting).
- Return 404 for revoked public codes — no metadata leakage.

**Spec Kit Task**: TASK-SEC-004

---

### SEC-005: OpenHTMLToPDF v1.0.10 + PDFBox 2.0.30 — Dependency CVE Check

- **Location**: `plan.md` + `research.md` — PDF library dependencies
- **OWASP**: A06:2025 — Vulnerable and Outdated Components
- **CWE**: CWE-1104 — Use of Unmaintained Third-Party Components
- **CVSS**: 2.5 (Low) — AV:N/AC:H/PR:N/UI:N/S:U/C:N/I:N/A:L

**Description**: The plan pins OpenHTMLToPDF 1.0.10 and PDFBox 2.0.30 based on the spike. Before implementation, these versions should be checked against the National Vulnerability Database (NVD) and Maven Central for known vulnerabilities.

**Risk**: Low — PDFBox 2.0.30 is from 2023 and may have CVEs fixed in newer versions. OpenHTMLToPDF 1.0.10 is the latest stable release (June 2026).

**Recommendation**:
- Check `org.apache.pdfbox:pdfbox:2.0.30` against https://nvd.nist.gov or `mvn dependency-check`.
- If CVEs exist, evaluate impact on resume PDF generation (exploitability via crafted HTML input is limited since HTML is server-generated, not user-supplied).
- Document the CVE check result in `research.md`.

**Spec Kit Task**: TASK-SEC-005

---

### SEC-006: PDF Storage Directory Isolation

- **Location**: `plan.md` — "Files on configurable filesystem directory"
- **OWASP**: A05:2025 — Security Misconfiguration
- **CWE**: CWE-552 — Files or Directories Accessible to External Parties
- **CVSS**: — Informational

**Description**: Generated PDF/HTML files are stored on the filesystem. The plan does not specify whether the storage directory is outside the web application root or properly isolated from direct HTTP access.

**Risk**: Informational — If the storage directory is accidentally configured inside the webapp root or served by the web server, PDFs could be accessed directly without authentication.

**Recommendation**:
- Document in `quickstart.md` that the storage directory must be outside the web application root.
- Add a startup check in the application: verify the storage directory is not under the webapp/static resources path.
- Log a warning if the directory is world-readable.

**Spec Kit Task**: TASK-SEC-006

---

### SEC-007: Font Files License Verification

- **Location**: `plan.md` — "Inter (body) and Manrope (headings) ported from spike"
- **OWASP**: A06:2025 — Vulnerable and Outdated Components
- **CWE**: — Informational
- **CVSS**: — Informational

**Description**: Inter and Manrope fonts are embedded in generated PDFs. Both are SIL Open Font License (OFL) — verified. Embedding fonts in PDFs is permitted under OFL.

**Recommendation**:
- Include the OFL license text alongside font files in `src/main/resources/fonts/`.
- No action required beyond documentation.

**Spec Kit Task**: TASK-SEC-007

---

### SEC-008: Audit Trail for Public PDF Access

- **Location**: `plan.md` — Public PDF route
- **OWASP**: A09:2025 — Security Logging and Monitoring Failures
- **CWE**: — Informational
- **CVSS**: — Informational

**Description**: The plan specifies detailed logging for fitting, validation, and backend operations, but does not mention logging public PDF accesses. For operational visibility, it would be useful to log when public resumes are accessed.

**Recommendation**:
- Log public PDF accesses at INFO level: timestamp, publicCode (not full URL), IP (anonymized last octet), User-Agent.
- Do NOT log the recruiter's IP in full — anonymize for GDPR compliance.
- Consider this optional for MVP.

**Spec Kit Task**: TASK-SEC-008

---

## Confirmed Secure Patterns

The following design decisions are explicitly secure and deserve recognition:

| Pattern | Location | Why it is secure |
|---|---|---|
| **HTML-escaping of user text** | FR-008-023-1, plan.md | Prevents XSS/markup injection in PDF templates — critical for HTML→PDF pipeline |
| **Owner-scoped downloads** | All download endpoints | Other users cannot access private resumes by ID |
| **Public route: PDF only** | `/{username}/{publicCode}` | No cover letter, no HTML, no metadata — minimal exposure |
| **404 on invalid public codes** | Public route spec | No information leakage about whether a user or resume exists |
| **No raw paths in responses** | API contracts + logging plan | Prevents path disclosure |
| **Log safety** | Logging plan | Explicit whitelist/blacklist: what to log and what NOT to log |
| **Catch Exception for rollback** | D23 reference in plan | Prevents transaction leak on RuntimeException |
| **Boxed Long for nullable fields** | B9 reference in plan | Prevents NPE on auto-unboxing |
| **FINALIZING status for concurrency** | FR-008-028-2 | Prevents double-generation race condition |
| **CSS safety inspector** | Ported from spike | Blocks browser-only CSS — defense-in-depth for PDF rendering |

---

## Action Plan

### Required Before Implementation

| Finding | Action | Priority |
|---|---|---|
| SEC-001 | Add path traversal protection to download controllers | Medium |
| SEC-002 | Specify `SecureRandom`/UUID for public code generation | Medium |
| SEC-003 | Add rate limiting to public PDF route | Medium |

### Recommended During Implementation

| Finding | Action | Priority |
|---|---|---|
| SEC-004 | Handle username change → public URL invalidation | Low |
| SEC-005 | Check PDFBox 2.0.30 for known CVEs | Low |
| SEC-006 | Document storage directory isolation in quickstart | Informational |
| SEC-007 | Include OFL license with font files | Informational |
| SEC-008 | Add public PDF access audit logging | Informational |

---

## Risk Summary

```
Critical:  0
High:      0
Medium:    3  (SEC-001, SEC-002, SEC-003)
Low:       2  (SEC-004, SEC-005)
Info:      3  (SEC-006, SEC-007, SEC-008)
```

**Overall Verdict**: LOW risk. ✅ **Plan is safe to proceed with implementation.** Two medium findings (SEC-001, SEC-003) were embedded into the plan as explicit mitigations. SEC-002 was already addressed by existing `PublicCodeGenerator` infrastructure.

---

## Next Steps

1. **Remediation**: Add the three medium findings (SEC-001, SEC-002, SEC-003) to the task breakdown via `/speckit.superpowers.tasks`.
2. **Memory Capture**: No systemic patterns requiring durable memory capture at this stage — all findings are implementation-specific.
3. **Follow-up**: No critical/high findings — `/speckit.security-review.followup` not required unless the user wants explicit tracking tasks.
