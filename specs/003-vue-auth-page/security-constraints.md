# Security Constraints: Feature 003 — Vue Auth Page

**Generated**: 2026-06-02 (from `/speckit.security-review.plan`)
**Source**: `.specify/memory/constitution.md §V`, OWASP Top 10 2025

## Trust Boundaries

| Boundary | From | To | Restrictions |
|----------|------|----|-------------|
| Browser ↔ Nginx | Visitor/User | Frontend | TLS (terminated at Nginx), CSP headers, XSS protection |
| Nginx ↔ Tomcat | Nginx | Backend | Internal network only, port 8080 not exposed publicly |
| Tomcat ↔ PostgreSQL | Backend | Database | Internal network only, single DB user with limited schema access |

## Authentication & Session Rules

1. **BCrypt mandatory**: All passwords MUST be hashed with BCrypt before storage. No plaintext or reversible encryption. Cost factor >= 10.
2. **No email enumeration**: Login errors MUST use generic "Invalid email or password" regardless of whether the email exists.
3. **Session security**: JSESSIONID cookie MUST use HttpOnly + SameSite=Lax + Secure (in production). Session invalidation on logout.
4. **Rate limiting**: Lock account for 15 min after 5 consecutive failed attempts. Counter resets on success or lockout expiry.
5. **Session timeout**: 30 min default inactivity. 7 days with "Remember me". Configure via `session.getMaxInactiveInterval()`.
6. **Password strength**: Minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 digit. OWASP recommends 12+ for future hardening.

## Authorization Rules

1. **Admin endpoints**: Backend MUST enforce ADMIN role check — frontend-only hiding is insufficient.
2. **Interceptor paths**: AuthInterceptor protects `/api/**` except `/api/auth/*`. Role-based checks for `/admin/**`.
3. **Public resume links**: Must NOT require authentication. Only expose finalized PDF — no profile data or drafts.

## Data Protection Rules

1. **PreparedStatement**: ALL SQL queries MUST use PreparedStatement. String concatenation forbidden.
2. **No secrets in logs**: Auth event logs at INFO/WARN level. Never log passwords, session tokens, or full API keys.
3. **Error safety**: All error responses MUST return `{message, errorCode, timestamp}` without stack traces.
4. **XSS sanitization**: User-generated content MUST be sanitized on input. AI-generated HTML MUST use allowlist.

## Future Considerations (Not Yet Implemented)

1. **Password reset**: Not in MVP scope. When added: use time-limited tokens (15-60 min), hash stored in DB, email delivery.
2. **Session persistence**: In-memory HttpSession is fine for single-server MVP. Multi-server would need Redis or sticky sessions.
3. **Dependency scanning**: No automated CVE scanning configured. Manual review required before adding new dependencies.
