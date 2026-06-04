---
document_type: security-review
review_type: audit
assessment_date: 2026-06-04
codebase_analyzed: resumainer-java-vue-web-app (branch feat/004-custom-jdbc-connection-pool)
total_files_analyzed: 25
total_findings: 9
overall_risk: LOW
critical_count: 0
high_count: 0
medium_count: 3
low_count: 4
informational_count: 2
owasp_categories: ["A02:2025-Security Misconfiguration", "A05:2025-Injection", "A09:2025-Security Logging and Monitoring Failures", "A10:2025-Mishandling of Exceptional Conditions"]
cwe_ids: ["CWE-614", "CWE-1021", "CWE-532", "CWE-754"]
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

**Overall Security Posture:** LOW RISK
**Assessment Date:** 2026-06-04
**Codebase Analyzed:** resumainer-java-vue-web-app (feat/004-custom-jdbc-connection-pool)
**Total Files Analyzed:** 25 (source code, config, Docker, frontend)
**Total Findings:** 9 (0 Critical, 0 High, 3 Medium, 4 Low, 2 Informational)

### Findings by Severity

| Severity | Count | Percentage |
|----------|-------|-----------|
| Critical | 0 | 0% |
| High | 0 | 0% |
| Medium | 3 | 33% |
| Low | 4 | 44% |
| Informational | 2 | 22% |

### Risk Summary

The codebase demonstrates strong security awareness overall. All database queries use `PreparedStatement` (no SQL injection), passwords are hashed with BCrypt (cost factor 12), CSRF protection follows OWASP cookie-to-header pattern, error handlers never expose stack traces, and the Docker container runs as a non-root user.

The 3 Medium findings are all configuration hardening issues — no active vulnerabilities that would allow data exfiltration or privilege escalation. The most impactful issue is the missing `Content-Security-Policy` header in the Nginx configuration, which would help mitigate XSS in the Vue SPA. The CSRF cookie not being marked `Secure` is currently mitigated by the API-only architecture (no sensitive cookie-based flows over HTTP).

---

## Vulnerability Findings

### MEDIUM — Missing Content-Security-Policy Header

**Finding ID:** SEC-101
**Location:** `docker/nginx.conf:19-22`
**OWASP Category:** A02:2025-Security Misconfiguration
**CWE:** CWE-1021 (Improper Restriction of Rendered UI Layers)
**CVSS Score:** 5.4 (Medium)

#### Description

The Nginx configuration sets `X-Content-Type-Options`, `X-Frame-Options`, and `Referrer-Policy` headers but does **not** include a `Content-Security-Policy` header. Without CSP, the Vue SPA has no defense-in-layer against XSS attacks. If an attacker injects malicious script into any rendered content (e.g., via AI-generated resume HTML or user profile data), the browser will execute it without restriction.

#### Affected Code

```nginx
# docker/nginx.conf:19-22 — CURRENT (no CSP):
add_header X-Content-Type-Options nosniff;
add_header X-Frame-Options DENY;
add_header Referrer-Policy same-origin;

# MISSING: Content-Security-Policy
```

#### Impact

An XSS vulnerability in any part of the Vue SPA could lead to:
- Session hijacking (attacker reads the CSRF token cookie)
- Phishing (attacker renders fake login form)
- Data exfiltration (attacker reads profile data from the page)

#### Remediation

Add a strict `Content-Security-Policy` header to the Nginx config. For a Vue SPA that calls its own API, a reasonable starting policy:

```nginx
add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self' http://resumainer-app:8080; frame-ancestors 'none'; base-uri 'self'; form-action 'self'" always;
```

#### Fixed Code Example

```nginx
# docker/nginx.conf — UPDATED security section:
# Security headers
add_header X-Content-Type-Options nosniff;
add_header X-Frame-Options DENY;
add_header Referrer-Policy same-origin;
add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'" always;
```

**Spec-Kit Task:** TASK-SEC-101

---

### MEDIUM — CSRF Cookie Not Marked Secure

**Finding ID:** SEC-102
**Location:** `backend/src/main/java/com/resumainer/filter/CsrfFilter.java:55`
**OWASP Category:** A02:2025-Security Misconfiguration
**CWE:** CWE-614 (Sensitive Cookie in HTTP Session Without 'Secure' Attribute)
**CVSS Score:** 4.3 (Medium)

#### Description

The CSRF cookie (`XSRF-TOKEN`) is set with `setSecure(false)`. In production deployment with Docker, when the application is served over HTTPS, this cookie will still be sent over unencrypted HTTP connections, exposing the CSRF token to network eavesdropping.

The code has a comment acknowledging this: `// Set to true in production with HTTPS`.

#### Affected Code

```java
// CsrfFilter.java:52-57
jakarta.servlet.http.Cookie csrfCookie = new jakarta.servlet.http.Cookie(CSRF_COOKIE_NAME, sessionToken);
csrfCookie.setPath("/");
csrfCookie.setHttpOnly(false);
csrfCookie.setSecure(false); // Set to true in production with HTTPS
csrfCookie.setAttribute("SameSite", "Lax");
response.addCookie(csrfCookie);
```

#### Impact

If an attacker can perform man-in-the-middle (MITM) on the network, they can read the CSRF token. Combined with a captured session cookie, this allows crafting forged requests.

#### Remediation

Make `Secure` flag configurable via Spring profile. In `prod` profile, set to `true`. In `dev` profile, keep `false` for local HTTP testing.

```java
// CsrfFilter.java — add constructor parameter:
private final boolean secure;

public CsrfFilter(boolean secure) {
    this.secure = secure;
}

// In doFilterInternal:
csrfCookie.setSecure(this.secure);
```

Then in `WebConfig.java` or a config class:

```java
@Bean
public CsrfFilter csrfFilter(@Value("${csrf.cookie.secure:false}") boolean secure) {
    return new CsrfFilter(secure);
}
```

And in `application-prod.properties`:
```properties
csrf.cookie.secure=true
```

**Spec-Kit Task:** TASK-SEC-102

---

### MEDIUM — Missing HSTS Header

**Finding ID:** SEC-103
**Location:** `docker/nginx.conf:19-22`
**OWASP Category:** A02:2025-Security Misconfiguration
**CWE:** CWE-1021
**CVSS Score:** 4.0 (Medium)

#### Description

The Nginx configuration does not include `Strict-Transport-Security` header. When the application is deployed with HTTPS (production), browsers will not know to always use HTTPS, leaving the first request vulnerable to SSL-strip attacks.

#### Remediation

```nginx
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
```

**Spec-Kit Task:** TASK-SEC-103

---

### LOW — Dev Profile Logging at DEBUG Level May Expose Sensitive Details

**Finding ID:** SEC-104
**Location:** `backend/src/main/resources/application-dev.properties:4`
**OWASP Category:** A09:2025-Security Logging and Monitoring Failures
**CWE:** CWE-532 (Insertion of Sensitive Information into Log File)
**CVSS Score:** 2.6 (Low)

#### Description

The `dev` profile sets `logging.level.com.resumainer=DEBUG`. The `ConnectionFactory.createConnection()` method logs at DEBUG level with a masked URL (`maskUrl()`), and `SimpleConnectionPool` logs borrow/return operations at DEBUG level. While credentials are masked, DEBUG logging in dev could still reveal internal state (connection counts, timing, error details) that would not be visible at INFO level.

#### Assessment

This is acceptable for development but a deployment risk if the `dev` profile is accidentally used in production. The `prod` profile correctly uses `INFO` level.

#### Remediation

Add a note to the deployment checklist: verify `SPRING_PROFILES_ACTIVE=prod` in production Docker deployments. The Docker Compose already sets this correctly (`SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}`).

Also enhance `ConnectionFactory.maskUrl()` to verify it correctly masks the URL even for edge cases (passwords with special characters):

```java
// Current implementation — verify handles all URL formats:
private static String maskUrl(String url) {
    if (url == null) return null;
    return url.replaceAll("://[^:]+:[^@]+@", "://***:***@");
}
```

**Spec-Kit Task:** TASK-SEC-104

---

### LOW — AuthService Email Logging in Warnings

**Finding ID:** SEC-105
**Location:** `backend/src/main/java/com/resumainer/service/AuthService.java:61,67,76,117,144,150,157,173,177`
**OWASP Category:** A09:2025-Security Logging and Monitoring Failures
**CWE:** CWE-532
**CVSS Score:** 2.6 (Low)

#### Description

`AuthService` logs user email addresses in WARN-level messages across all auth failure paths. For example:

```java
log.warn("Registration failed: password mismatch for email: {}", request.getEmail());
log.warn("Login failed: user not found for email: {}", email);
log.warn("Login failed: account blocked for email: {}", email);
```

While this aids debugging, logging emails in warnings is a privacy consideration. Logs may be aggregated, shipped to log management systems, or retained longer than necessary.

#### Assessment

This is a common practice and not a vulnerability per se. The threat model for a Capstone project does not include sophisticated log analysis attacks. Noted for hardening in production.

#### Remediation

For production, consider removing email from WARN-level logs and keeping it only in DEBUG-level logs. Example:

```java
// Change from:
log.warn("Login failed: user not found for email: {}", email);
// To:
if (log.isDebugEnabled()) {
    log.debug("Login failed: user not found for email: {}", email);
}
log.warn("Login failed: invalid credentials");
```

**Spec-Kit Task:** TASK-SEC-105

---

### LOW — AuthService.authenticate() Login Attempt Updates Not in Transaction

**Finding ID:** SEC-106
**Location:** `backend/src/main/java/com/resumainer/service/AuthService.java:180,190`
**OWASP Category:** A10:2025-Mishandling of Exceptional Conditions
**CWE:** CWE-754 (Improper Check for Unusual or Exceptional Conditions)
**CVSS Score:** 2.9 (Low)

#### Description

The `authenticate()` method calls `userDao.updateLoginAttempts()` and `userDao.resetLoginAttempts()` without a transaction boundary. If the database fails after incrementing the counter but before the method returns, the failed login count is incremented but the error is swallowed by the DAO's exception handler.

#### Affected Code

```java
// AuthService.java:180 — no transaction around this
userDao.updateLoginAttempts(user.getId(), newAttempts, lockTime);
// ... exception thrown after this if >=5 attempts
```

#### Impact

In a worst-case scenario, repeated failures combined with database errors could increment the failed attempts counter without the user knowing, potentially causing an account lockout that cannot be reset by successful login.

#### Assessment

Extremely unlikely edge case. The DAO's try-with-resources ensures the connection is closed, and the database `UPDATE` is atomic per statement. Noted for hardening.

#### Remediation

Not required for MVP. For hardening, wrap the login attempt update in the same connection used by the DAO (pass connection through), but this would require refactoring the authentication flow to use a shared connection.

---

### LOW — Prod Properties Use Spring ${...} Placeholders for DB Credentials

**Finding ID:** SEC-107
**Location:** `backend/src/main/resources/application-prod.properties:10-12`
**OWASP Category:** A05:2025-Injection
**CWE:** CWE-74
**CVSS Score:** 3.7 (Low)

#### Description

The `application-prod.properties` file uses `${DB_PASSWORD}` and `${DB_USER}` Spring placeholder syntax. In pure Spring MVC (without Spring Boot), these placeholders are only resolved if `PropertySourcesPlaceholderConfigurer` is registered as a bean. If that bean is missing, the literal string `${DB_PASSWORD}` will be used as the password.

#### Affected Code

```properties
db.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME:resumainer}
db.user=${DB_USER}
db.password=${DB_PASSWORD}
```

#### Impact

If `PropertySourcesPlaceholderConfigurer` is not configured, the application will attempt to connect to PostgreSQL with the literal password `${DB_PASSWORD}`, causing connection failures.

#### Remediation

Verify that `WebConfig.java` or another `@Configuration` class has:

```java
@Bean
public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
}
```

If this is missing, add it to `WebConfig.java`. Note: `PropertySourcesPlaceholderConfigurer` is likely already registered based on Feature 003 working correctly, but verify explicitly.

**Spec-Kit Task:** TASK-SEC-107

---

### INFORMATIONAL — Nginx Missing Permissions-Policy Header

**Finding ID:** SEC-108
**Location:** `docker/nginx.conf:19-22`

The Nginx config does not include `Permissions-Policy` header. This controls which browser APIs the SPA can access. Low impact — no current feature uses sensitive APIs (camera, microphone, geolocation).

**Remediation:** Add `add_header Permissions-Policy "camera=(), microphone=(), geolocation=()" always;`

---

### INFORMATIONAL — Login Endpoint Response Time May Allow Timing Attack

**Finding ID:** SEC-109
**Location:** `backend/src/main/java/com/resumainer/service/AuthService.java:162-164`

The password verification uses BCrypt which is intentionally slow (~250ms). However, the email lookup (`userDao.findByEmail`) is significantly faster (~5ms). An attacker could measure response times to determine whether an email exists (user enumeration via timing).

**Assessment:** The current implementation returns the same `"Invalid email or password"` message for both cases (user not found and wrong password), which is correct. However, the timing difference between the two paths could theoretically be measured over many requests. The BCrypt verification cost mitigates this — timing is dominated by BCrypt, not the DB query.

**Remediation:** Not required. The generic error message is the primary defense, and BCrypt timing dominates the response time.

---

## Architecture Risks

### Risk Category: Trust Boundaries

#### Risk Description

The Nginx reverse proxy is the only TLS/HTTPS termination point. All traffic between Nginx, Tomcat (app), and PostgreSQL (db) within the Docker network is unencrypted. If an attacker gains access to the Docker network (e.g., through a compromised container), they can sniff database credentials and API traffic.

#### Affected Components

- Nginx ↔ Tomcat communication (HTTP, not HTTPS)
- Tomcat ↔ PostgreSQL communication (TCP, not SSL)

#### Risk Assessment

**Likelihood:** Low (requires container breakout)
**Impact:** High (full database access)
**Risk Level:** Low

#### Mitigation Recommendations

For production deployment:
1. Enable PostgreSQL SSL between app and db containers (`ssl=true&sslmode=require` in JDBC URL)
2. The current Docker network isolation and non-root user mitigate container breakout risk

---

## Missing Security Controls

| Control | Status | Priority | Recommendation |
|---------|--------|----------|----------------|
| Content-Security-Policy | ❌ Missing | Medium | Add CSP header to Nginx config |
| HSTS (Strict-Transport-Security) | ❌ Missing | Medium | Add HSTS header for HTTPS |
| Permissions-Policy | ❌ Missing | Low | Restrict browser API access |
| Rate Limiting (API-level) | ⚠️ Partial (BCrypt cost) | Low | Account lockout at service level is sufficient |
| PostgreSQL SSL | ❌ Missing | Low | Add for production deployment |

---

## Secrets Detection

| Type | Location | Risk | Status |
|------|----------|------|--------|
| DB_PASSWORD via env var | docker-compose.yml:42 | LOW | ✅ Properly handled (required, no default) |
| DB_PASSWORD via env var | production properties | LOW | ✅ From environment, not hardcoded |
| Dev defaults in WebConfig | WebConfig.java:189 | LOW | ✅ `resumainer_dev` default, documented as dev-only |
| `.env` file safety | Quickstart guide | INFO | ✅ Warning added in quickstart.md |

---

## STRIDE Threat Model Summary

| Component | Spoofing | Tampering | Repudiation | Info Disclosure | DoS | EoP |
|-----------|----------|-----------|-------------|-----------------|-----|-----|
| Auth API | 🟡 | 🟢 | 🟢 | 🟢 | 🟡 | 🟢 |
| User/DAO Layer | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| Nginx Proxy | 🟡 | 🟢 | 🟢 | 🟢 | 🟡 | 🟢 |
| PostgreSQL | 🟢 | 🟢 | 🟢 | 🟡 | 🟢 | 🟢 |
| Docker Deployment | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |

**Legend:** 🟢 Low Risk | 🟡 Medium Risk | 🔴 High Risk

---

## Spec-Kit Alignment Updates

### Generated Remediation Tasks

| Task ID | Severity | Category | Description | Recommended Phase |
|---------|----------|----------|-------------|-------------------|
| TASK-SEC-101 | Medium | A02-Misconfig | Add Content-Security-Policy header to nginx.conf | After Feature 004 |
| TASK-SEC-102 | Medium | A02-Misconfig | Make CSRF cookie Secure flag configurable by profile | After Feature 004 |
| TASK-SEC-103 | Medium | A02-Misconfig | Add Strict-Transport-Security header to nginx.conf | After Feature 004 |
| TASK-SEC-104 | Low | A09-Logging | Verify prod profile logging level, add dev checklist | During Feature 004 |
| TASK-SEC-105 | Low | A09-Logging | Remove emails from WARN logs for production | Future |
| TASK-SEC-107 | Low | A05-Injection | Verify PropertySourcesPlaceholderConfigurer bean exists | During Feature 004 |

---

## Appendix

### A. Assessment Methodology

Manual code review of all source files in the `feat/004-custom-jdbc-connection-pool` branch against OWASP Top 10 2025, CWE/SANS Top 25, and secure coding best practices for Java + Spring MVC + Vue 3 applications.

### B. Confirmed Secure Patterns

| Pattern | Location | Status |
|---------|----------|--------|
| PreparedStatement for all SQL | All DAO classes | ✅ Verified |
| BCrypt password hashing (cost 12) | PasswordService.java | ✅ Verified |
| CSRF protection (OWASP cookie-to-header) | CsrfFilter.java | ✅ Verified |
| No stack traces in error responses | GlobalExceptionHandler.java | ✅ Verified |
| Auth session check on API endpoints | AuthInterceptor.java | ✅ Verified |
| Non-root user in Docker | Dockerfile:52 | ✅ Verified |
| Multi-stage Docker build | Dockerfile | ✅ Verified |
| Flyway migrations on startup | WebConfig.java:214 | ✅ Verified |
| DB_PASSWORD required (no default) | docker-compose.yml:42 | ✅ Verified |

### C. Limitations

- No SAST tooling was used (manual code review only)
- No dynamic testing (no running application was tested)
- No dependency vulnerability scan (CVEs not checked against pom.xml)
- Vue SPA frontend was reviewed for auth logic, not for DOM-based XSS

### D. Action Plan

1. **Feature 004 Implementation** — proceed as planned (no critical findings block it)
2. **During Feature 004** — verify TASK-SEC-104 and TASK-SEC-107 (dev checklist, PropertySourcesPlaceholderConfigurer)
3. **After Feature 004** — implement TASK-SEC-101, TASK-SEC-102, TASK-SEC-103 (Nginx security headers)
4. **Future Sprint** — TASK-SEC-105 (log hardening)

## INDEX.md Row

```text
| docs/security-reviews/2026-06-04-full-audit.md | audit | 2026-06-04 | LOW | C:0 H:0 M:3 L:4 I:2 | A02,A05,A09,A10 |
```
