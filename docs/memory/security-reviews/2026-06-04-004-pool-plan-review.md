---
document_type: security-review
review_type: plan
assessment_date: 2026-06-04
codebase_analyzed: resumainer-java-vue-web-app (specs/004-custom-jdbc-connection-pool)
total_files_analyzed: 8
total_findings: 4
overall_risk: LOW
critical_count: 0
high_count: 0
medium_count: 1
low_count: 2
informational_count: 1
owasp_categories: ["A04:2025-Insecure Design", "A05:2025-Security Misconfiguration", "A09:2025-Security Logging and Monitoring Failures"]
cwe_ids: ["CWE-209", "CWE-200", "CWE-543"]
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

# Security Review: Feature 004 — Custom JDBC Connection Pool (Plan)

**Assessment Date**: 2026-06-04
**Overall Risk**: LOW
**Total Findings**: 4 (0 Critical, 0 High, 1 Medium, 2 Low, 1 Informational)

## Executive Summary

The plan for Feature 004 (Custom JDBC Connection Pool) demonstrates strong security awareness. Key security requirements are already embedded in the spec (FR-012, FR-014, FR-015, FR-016) and properly referenced in the plan's Constitution Check. The decision to use `System.getenv()` instead of Spring placeholders (B2 guard) and to log without credentials (FR-015) show proactive security consideration.

All 4 findings are pre-implementation notes — none block the plan. The single Medium finding (SEC-003) is about DataSource.unwrap() contract deviation and should be addressed during implementation.

## Plan Artifacts Reviewed

| Artifact | Path |
|----------|------|
| Implementation Plan | `specs/004-custom-jdbc-connection-pool/plan.md` |
| Feature Spec | `specs/004-custom-jdbc-connection-pool/spec.md` |
| Data Model | `specs/004-custom-jdbc-connection-pool/data-model.md` |
| DataSource Contract | `specs/004-custom-jdbc-connection-pool/contracts/datasource-contract.md` |
| Quickstart Guide | `specs/004-custom-jdbc-connection-pool/quickstart.md` |
| Research | `specs/004-custom-jdbc-connection-pool/research.md` |
| Memory Synthesis | `specs/004-custom-jdbc-connection-pool/memory-synthesis.md` |
| Constitution | `.specify/memory/constitution.md` |

## Vulnerability Findings

### SEC-001 — Error messages may leak connection details (LOW)

- **Location**: `specs/004-custom-jdbc-connection-pool/spec.md` — FR-008, FR-010, FR-016
- **OWASP**: A04:2025-Insecure Design
- **CWE**: CWE-209 (Information Exposure Through Error Messages)
- **CVSS**: 3.1 (Low)

**Description**: The spec requires "clear timeout error" (FR-008), "clear pool closed error" (FR-010), and "clear exception" (FR-016) messages. Without explicit guidance, these error messages could accidentally include the JDBC URL, database name, or other connection details that should not be exposed to application callers.

**Recommendation**: Add a note to the implementation tasks that ConnectionPoolException messages must describe the problem without including the JDBC URL, host, port, or database name. Example good message: `"Could not acquire database connection within 5000 ms."` Example bad message: `"Could not connect to jdbc:postgresql://prod-db:5432/resumainer: timeout"`

---

### SEC-002 — No SSL/TLS guidance for production JDBC connection (LOW)

- **Location**: `specs/004-custom-jdbc-connection-pool/quickstart.md`
- **OWASP**: A05:2025-Security Misconfiguration
- **CWE**: CWE-200 (Exposure of Sensitive Information)
- **CVSS**: 2.6 (Low)

**Description**: The quickstart shows the JDBC URL without SSL parameters (`jdbc:postgresql://host:port/db`). For production deployments, the database connection should use SSL to encrypt credentials and data in transit. The plan does not document this as a deployment hardening concern.

**Recommendation**: Add a note in the quickstart or an Assumption in the plan that production deployments should use SSL: `jdbc:postgresql://host:port/db?ssl=true&sslmode=require`. This is a deployment concern, not a code change.

---

### SEC-003 — DataSource.unwrap() contract deviation (MEDIUM)

- **Location**: `specs/004-custom-jdbc-connection-pool/contracts/datasource-contract.md` — line 21
- **OWASP**: A04:2025-Insecure Design
- **CWE**: CWE-543 (Use of Singleton Pattern Without Synchronization)
- **CVSS**: 5.3 (Medium)

**Description**: The contract states that `unwrap(Class<T> iface)` returns `null` if the class is not wrappable. According to the JDBC 4.0 specification (`javax.sql.Wrapper`), `unwrap()` must throw `SQLException` if the class is not supported. Returning `null` violates the contract and can cause `NullPointerException` in consumer code that calls `unwrap()` and uses the result without null-check (which is a reasonable assumption given the JDBC spec).

**Recommendation**: Update the contract (and implementation) to throw `SQLException` with message "Not a wrapper for {iface.getName()}" instead of returning `null`. This matches the JDBC specification and prevents NPEs in service/DAO code.

---

### SEC-004 — Missing .env file warning (INFORMATIONAL)

- **Location**: `specs/004-custom-jdbc-connection-pool/quickstart.md` — line 7
- **OWASP**: A05:2025-Security Misconfiguration
- **CWE**: CWE-200 (Exposure of Sensitive Information)
- **CVSS**: 1.0 (Informational)

**Description**: The quickstart mentions adding DB credentials to `.env` for Docker Compose but does not explicitly warn against committing `.env` to Git. While `.env` is typically in `.gitignore`, an explicit reminder reduces risk.

**Recommendation**: Add a note: "Ensure `.env` is in `.gitignore` and never committed to the repository."

## Confirmed Secure Patterns

| Pattern | Source | Status |
|---------|--------|--------|
| Database credentials from environment variables, not hardcoded | plan.md (B2 guard), spec.md (FR-013) | ✅ Verified |
| No secrets in logs (credentials, passwords) | spec.md (FR-015) | ✅ Verified |
| Connection reset on return (rollback, autoCommit, readOnly, clearWarnings) | spec.md (FR-005), plan.md (Q2) | ✅ Verified |
| getConnection(username, password) explicitly unsupported | spec.md (FR-012), contracts/datasource-contract.md | ✅ Verified |
| Thread-safe pool shutdown via AtomicBoolean | plan.md (Q5) | ✅ Verified |
| Fail-fast on invalid pool configuration | spec.md (FR-016), data-model.md (validation) | ✅ Verified |
| PreparedStatement unchanged in DAOs (SQL injection protection) | constitution.md (D2) | ✅ Verified |
| Single technical DB user (no per-user credentials) | spec.md (Assumptions) | ✅ Verified |

## Action Plan & Next Steps

### Durable Memory Capture

No systemic security vulnerabilities or reusable security patterns were identified. The existing memory (B2 — DataSource URL, FR-015 — log safety) already covers the security concerns for this feature. No capture needed.

### Remediation Planning

**SEC-003** (Medium) should be addressed during implementation in the `unwrap()` method. Affects one method signature in `SimpleConnectionPool`.

**SEC-001** (Low) — add to implementation checklist for ConnectionPoolException messages.

**SEC-002** (Low) — add to quickstart as deployment note.

**SEC-004** (Informational) — add to quickstart as reminder.

No critical or high findings — remediation tasks can be part of regular implementation tasks.

## INDEX.md Row

```text
| docs/security-reviews/2026-06-04-004-pool-plan-review.md | plan | 2026-06-04 | LOW | C:0 H:0 M:1 L:2 I:1 | A04,A05,A09 |
```
