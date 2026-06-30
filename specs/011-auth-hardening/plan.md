# Implementation Plan: Auth Hardening and Spring Security Migration

**Branch**: `feat/011-auth-hardening`  
**Date**: 2026-06-30  
**Spec**: `specs/011-auth-hardening/spec.md`  
**Status**: Draft v0.3 — review-corrected  
**Input**: Feature specification for full authentication hardening before production deployment.

> **Instruction for implementer**: This plan is a controlled implementation guide for a high-risk authentication migration. If any item conflicts with the actual current codebase, STOP and ask before changing architecture, adding migrations, or inventing workarounds. Do not expand scope beyond this plan. Do not silently reinterpret requirements.

**Memory Synthesis**: `specs/011-auth-hardening/memory-synthesis.md` — READ BEFORE IMPLEMENTING. Contains critical architecture constraints (non-Boot filter registration, Flyway setup, CSRF migration), relevant decisions (D47 data exposure audit, D40 checkpoint evidence), and historical bug patterns (CSRF header migration, auth error handling, uniform 404 delay).

**Spec Clarifications** (from `/speckit.superpowers.brainstorm` 2026-06-30):
1. Auth event logging: log failed/success/verification/reset — never secrets, passwords, raw tokens, or hashes.
2. Google OAuth not configured: hide the Continue with Google button when client ID/secret are absent.

**Review Corrections v0.3**:
1. Auth event logging is safe application logging only. It is NOT an audit-log feature and MUST NOT create a new `audit_log` table.
2. Non-Boot Spring Security filter registration is a hard rule: do NOT use Spring Boot `FilterRegistrationBean`.
3. Flyway migration pickup must be verified because this non-Boot app requires explicit Flyway configuration.
4. Spring Security/CSRF tests must account for MockMvc session behavior.
5. Public unauthenticated endpoints must use simple uniform delay where appropriate to reduce enumeration timing signals.
6. Final security evidence must include D47-style exposure audit: DTOs + SQL SELECT columns + logs + error responses.

---

## Summary

Implement production-oriented authentication hardening for ResumAIner by migrating the current custom session authentication to Spring Security while preserving the existing Vue SPA user experience.

This feature includes:

- Full migration from custom session auth to Spring Security.
- JSON-based SPA login/logout/status/register flows.
- Email-only login.
- Strict email confirmation before login.
- Password reset by email.
- Google OAuth2 login.
- Persistent remember-me using Spring Security.
- Cloudflare Turnstile captcha for public auth forms.
- Resend email delivery.
- Auth i18n and copy polish.
- Safe auth event application logging without secrets or audit-log table.
- Landing/public-entry anti-abuse hardening.
- Controlled removal of legacy auth/session/CSRF/interceptor code after proof checkpoints.

This feature MUST NOT migrate the project to Spring Boot, JWT, JPA/Hibernate, Spring Data, Lombok, or an external identity platform.

---

## Technical Context

### Current Backend

- Java 21.
- Non-Boot Spring MVC 6.
- Plain JDBC.
- Custom connection pool.
- Flyway migrations.
- Current auth is custom session-based auth:
  - `AuthController`
  - `AuthService`
  - `UserDao`
  - `UserSession`
  - `AuthInterceptor`
  - `CsrfFilter`
  - `PasswordService`
  - `PasswordStrengthValidator`

### Current Frontend

- Vue 3.
- TypeScript.
- Vite.
- PrimeVue.
- vue-i18n.
- Current auth frontend:
  - `AuthPage.vue`
  - `LoginForm.vue`
  - `RegisterForm.vue`
  - `authService.ts`
  - `useAuth.ts`
  - router guard in `router/index.ts`

### Current Auth UX Contract to Preserve

- Vue SPA remains the UI.
- Auth endpoints return JSON.
- Frontend does not use Spring Security default HTML login pages.
- `/api/auth/status` remains the central frontend auth-state check.
- Role-based redirect behavior remains:
  - USER → `/app/home`
  - ADMIN → `/app/admin`

---

## Mandatory MCP Usage Rules

> These rules are mandatory for OpenCode and DeepSeek. They are not optional style preferences.

### Context7 MCP

Before each logical task group, use Context7 MCP to refresh official/current documentation relevant to that group.

Required examples:

- Before Spring Security dependency/config work:
  - Spring Security servlet architecture.
  - SecurityFilterChain in non-Boot applications.
  - Spring Security CSRF.
  - Spring Security form login/custom JSON login.
  - Spring Security remember-me.
  - Spring Security OAuth2 Login.
- Before backend controller/service/DAO work:
  - Spring MVC controller handling.
  - JDBC PreparedStatement.
  - transaction handling.
  - JUnit 5 and Mockito.
- Before frontend work:
  - Vue 3 Composition API.
  - Vue Router.
  - PrimeVue forms/dialogs/toasts if used.
  - vue-i18n.
  - Vitest and Vue Test Utils.
- Before testing/evidence phases:
  - JUnit 5.
  - Mockito.
  - Vitest.
  - Playwright MCP usage patterns.

The implementer MUST report briefly:

1. which Context7 docs were checked;
2. which implementation choices they affected;
3. any conflict found between docs and the planned approach.

### Serena MCP

Serena MCP is mandatory for code navigation and edits.

Rules:

- Use Serena MCP for symbol inspection, references, call graph, and targeted edits.
- Do not use broad blind `grep`/manual file scanning as the primary navigation method.
- Manual search is allowed only if Serena cannot answer the query.
- If manual search is used, report why Serena was insufficient.
- Before editing each logical code group:
  1. inspect relevant symbols;
  2. inspect references;
  3. understand call graph;
  4. edit only targeted files.
- Do not perform broad automated rewrites.

### Postgres MCP

Postgres MCP is mandatory for database inspection and verification when schema/data is relevant.

Use Postgres MCP for:

- checking current `users` table structure;
- checking role/status/permission lookup data;
- verifying current password hash format;
- verifying current soft-delete fields;
- verifying migrations after applying them;
- inspecting `auth_tokens`;
- inspecting `oauth_accounts`;
- inspecting `persistent_logins`;
- verifying test data state after migration.

Rules:

- Do not guess database schema.
- Do not create migrations based only on assumptions.
- If schema conflicts with this plan, STOP and report.
- Never hard-delete data during verification.

### Playwright MCP

Playwright MCP is mandatory for browser/e2e/manual evidence.

Use Playwright MCP for:

- registration flow;
- check-email page;
- email verification result page;
- login/logout;
- CSRF browser behavior;
- remember-me browser behavior where practical;
- forgot password;
- reset password;
- Google OAuth2 start/failure states;
- captcha UI state;
- admin Account tab email verification status;
- i18n review;
- screenshots/evidence.

Rules:

- Do not install local Playwright.
- Use available Playwright MCP only.
- Capture screenshots or clear browser evidence for major checkpoints.

### spec-kit-memory MCP

If a valuable reusable lesson is discovered, propose it to the user first.

Rules:

- Do not write memory silently.
- Ask for explicit user confirmation.
- Only then use spec-kit-memory MCP.

---

## Strict Dependency Policy

### Allowed Dependencies

Only these Spring Security-related dependency categories are allowed without extra approval:

- `spring-security-web`
- `spring-security-config`
- `spring-security-core`
- `spring-security-crypto`
- `spring-security-oauth2-client`
- `spring-security-oauth2-jose`

### Forbidden Dependencies Without Explicit User Approval

- Spring Boot starters.
- Spring Boot autoconfiguration.
- Spring Data JPA.
- Hibernate.
- Lombok.
- JWT libraries.
- Keycloak SDK.
- Auth0 SDK.
- Firebase Auth SDK.
- Clerk SDK.
- New ORM frameworks.
- New web frameworks.
- Redis or distributed cache libraries.
- Email queue frameworks.

### Dependency STOP Rule

If implementation seems to require any dependency outside the allowed list:

1. STOP.
2. Explain why it seems necessary.
3. Show the exact alternative without the dependency.
4. Wait for user approval.

---

## Scope Decisions

### In Scope

1. Full migration to Spring Security in the existing non-Boot Spring MVC project.
2. JSON login/logout/status APIs for Vue SPA.
3. Email-only login.
4. Strict email verification.
5. Password reset by email.
6. Google OAuth2 login.
7. Persistent remember-me.
8. Cloudflare Turnstile captcha.
9. Resend email service.
10. Auth token storage.
11. OAuth account storage.
12. Existing test user migration.
13. Spring Security CSRF.
14. Backend admin authorization through Spring Security.
15. Auth error contract.
16. i18n and copy polish for auth and landing.
17. Admin Account tab email verification status.
18. Landing/public anti-abuse documentation and config.

### Out of Scope

1. Spring Boot migration.
2. JWT/stateless auth.
3. JPA/Hibernate/Spring Data.
4. Multi-provider OAuth beyond Google.
5. GitHub/LinkedIn/Microsoft login.
6. Account settings page for linking/unlinking OAuth accounts.
7. User password setup flow for OAuth-only accounts.
8. Forced password change for migrated test users.
9. Dev bootstrap admin.
10. Admin Users table email verification column.
11. Audit log.
12. Email queue.
13. Redis distributed rate limiting.
14. Full DDoS protection.
15. Captcha on GET landing page.
16. PDF generation/fitting/validation/templates.
17. AI generation/prompt/parser/OpenRouter/finalization.
18. Admin moderation redesign.
19. Hard-delete of users/resumes/profile data.
20. Unrelated profile editing.

---

## Key Product Decisions

### Full Spring Security Migration

The final source of truth for authentication and authorization MUST be Spring Security.

Legacy custom auth/session/interceptor code may exist only temporarily during migration checkpoints.

Final state must NOT contain two permanent independent security systems.

### Non-Boot Integration

This is not a Spring Boot application.

DeepSeek MUST NOT use examples that assume:

- `@SpringBootApplication`;
- Spring Boot starters;
- auto-configured `SecurityFilterChain`;
- Boot-specific `application.yml` behavior;
- Boot-specific OAuth client auto-configuration.

Spring Security must be registered explicitly for the existing servlet/Spring MVC setup.

Hard non-Boot registration rules:

- Do NOT use Spring Boot `FilterRegistrationBean`.
- Do NOT use Boot-only `@SpringBootApplication` or auto-configuration examples.
- Register Spring Security through the existing servlet initializer style, such as `AppInitializer.getServletFilters()` / `DelegatingFilterProxy`, or another documented non-Boot servlet registration approach.
- If official Spring Security docs suggest a different non-Boot registration path, STOP and report before changing the plan.

### JSON Login

Frontend remains SPA-first.

No Spring Security default generated login page.

Login endpoint remains API-driven.

Expected login request:

```json
{
  "email": "user@example.com",
  "password": "StrongPass1",
  "rememberMe": true,
  "captchaToken": "optional-or-required-after-failures"
}
```

### Email-Only Login

Only `users.email` is accepted as login identifier.

`username` remains profile/display identity only.

### Existing Users

Because current users are capstone/test data:

- mark all existing users as email verified;
- set all existing user passwords to encoded `Aa123456`;
- preserve roles/statuses/permissions/privileged flags/deletion flags.

This is explicitly not production-safe for real user data.

### Registration

Registration creates an unverified account and does not log the user in.

After successful registration:

- redirect/show `/app/auth/check-email`;
- send verification email;
- verification TTL is 24 hours.

### Password Reset

Password reset:

- generic request response;
- captcha required;
- token TTL 15 minutes;
- token stored hashed only;
- one-time use;
- reset link points to frontend.

### Google OAuth2

Google OAuth2 starts from backend:

```http
GET /oauth2/authorization/google
```

Rules:

- allow any verified Google account;
- reject unverified Google provider emails;
- auto-link existing user by verified email;
- create new app user for verified new email;
- do not populate `contact_detail` from Google profile;
- OAuth-only users get technical random password hash;
- password login disabled for OAuth-only users;
- frontend hides the "Continue with Google" button when Google OAuth2 client configuration is absent.

### Remember-Me

Use Spring Security persistent remember-me.

Use standard persistent token table:

```sql
persistent_logins(username, series, token, last_used)
```

Do not create a custom remember-me framework unless a verified non-Boot integration issue requires a STOP decision.

### CSRF

Move to Spring Security CSRF.

Use:

- cookie: `XSRF-TOKEN`;
- header: `X-XSRF-TOKEN`.

Remove legacy `CsrfFilter` after Spring Security CSRF is verified.

### Captcha

Use Cloudflare Turnstile.

Required for:

- registration;
- forgot password;
- resend verification;
- login only after suspicious/failed attempts.

Dev mode:

- config/profile based;
- may accept `dev-captcha-pass`.

Prod mode:

- real Turnstile verification required;
- dev bypass forbidden.

### Email

Use Resend.

Email templates:

- plain text + basic HTML;
- bilingual:
  1. English first;
  2. Russian duplicate below.

Prod:

- missing `RESEND_API_KEY` must not silently no-op.

Dev:

- may log email links/content if key is absent.

### Public Base URLs

Use two explicit config values:

- `APP_BACKEND_PUBLIC_BASE_URL`
- `APP_FRONTEND_PUBLIC_BASE_URL`

Verification link uses backend base URL.

Password reset link uses frontend base URL.

---

## Database Design

### Migration Requirements

Before writing migrations, verify the current Flyway setup.

Important non-Boot warning:

- This project is pure Spring MVC, not Spring Boot.
- Flyway is not auto-configured by Spring Boot.
- The current project must have explicit Flyway configuration, such as a bean with `initMethod="migrate"` or equivalent.
- If migrations are not picked up automatically by the current config, STOP and report before inventing a new migration mechanism.

Create migrations for:

1. new auth-related columns on `users`;
2. `auth_tokens`;
3. `oauth_accounts`;
4. `persistent_logins`;
5. existing test user migration.

### Users Table Additions

Add fields as needed:

```sql
email_verified BOOLEAN NOT NULL DEFAULT FALSE;
email_verified_at TIMESTAMP NULL;
password_login_enabled BOOLEAN NOT NULL DEFAULT TRUE;
updated_at TIMESTAMP NULL;
```

Notes:

- Exact SQL must match current PostgreSQL style and current schema.
- Verify existing columns with Postgres MCP before writing migration.
- If any column already exists, STOP and report before editing.

### Existing Test User Migration

Set all current existing users to:

```sql
email_verified = TRUE;
email_verified_at = COALESCE(email_verified_at, created_at, CURRENT_TIMESTAMP);
password_hash = '<encoded hash for Aa123456>';
password_login_enabled = TRUE;
```

Rules:

- preserve roles;
- preserve status;
- preserve permissions;
- preserve privileged flag;
- preserve deletion flags;
- preserve ownership relationships.

Important warning to include in migration comment:

```sql
-- CAPSTONE TEST DATA ONLY:
-- This resets existing test user passwords to Aa123456.
-- This is not safe for real production user data.
```

### Auth Tokens Table

Create simple table:

```sql
auth_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_type VARCHAR(40) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)
```

Required constraints/indexes:

- index on `token_hash`;
- index on `(user_id, token_type)`;
- check token_type in `EMAIL_VERIFICATION`, `PASSWORD_RESET` if consistent with project migration style.

Rules:

- no raw token storage;
- no abstract framework;
- simple DAO/service only.

### OAuth Accounts Table

Create table:

```sql
oauth_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    provider VARCHAR(40) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255) NOT NULL,
    provider_email_verified BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    UNIQUE (provider, provider_subject)
)
```

Recommended index:

- `oauth_accounts(user_id)`;
- `oauth_accounts(provider_email)`.

Rules:

- Google is provider value `GOOGLE`.
- Do not store provider access tokens or refresh tokens in this feature.
- Do not store Google client secret in database.

### Persistent Logins Table

Use Spring Security persistent remember-me standard shape:

```sql
persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
)
```

Important:

- Because login identifier is email, `username` stores email.
- If standard implementation expects specific column names/types, follow Spring Security docs and STOP if conflict appears.

---

## Backend Architecture

### New/Changed Packages and Classes

Exact names may be adjusted to match project style, but do not invent unnecessary layers.

Expected additions:

```txt
com.resumainer.config
  SecurityConfig.java

com.resumainer.service.security
  CustomUserDetailsService.java
  CustomUserDetails.java
  JsonAuthenticationSuccessHandler.java
  JsonAuthenticationFailureHandler.java
  JsonLogoutSuccessHandler.java
  OAuth2LoginSuccessHandler.java
  OAuth2LoginFailureHandler.java
  CaptchaService.java
  TurnstileCaptchaService.java
  DevCaptchaService.java
  AuthTokenService.java
  EmailVerificationService.java
  PasswordResetService.java
  ResendEmailService.java
  EmailTemplateService.java
  RememberMeConfigSupport.java (only if needed)

com.resumainer.dao
  AuthTokenDao.java
  OAuthAccountDao.java
```

Expected DTOs:

```txt
com.resumainer.dto.auth
  AuthErrorResponse.java
  LoginRequest.java or updated existing LoginRequest
  RegisterRequest.java or updated existing RegisterRequest
  RegisterPendingResponse.java
  PasswordResetRequestDto.java
  PasswordResetConfirmDto.java
  PasswordResetValidateResponse.java
  ResendVerificationRequestDto.java
```

Do not create a huge `SecurityEverythingService`.

Keep classes small and explicit.

### SecurityConfig

Must configure:

- Spring Security for existing non-Boot app;
- protected routes;
- public routes;
- JSON login;
- JSON logout;
- CSRF;
- remember-me;
- OAuth2 login;
- exception handling;
- admin authorization.

Public endpoints must include only necessary unauthenticated endpoints:

- landing/static/public resources;
- `/api/auth/register`;
- `/api/auth/login`;
- `/api/auth/status`;
- `/api/auth/verify-email`;
- `/api/auth/email-verification/resend`;
- `/api/auth/password-reset/request`;
- `/api/auth/password-reset/validate`;
- `/api/auth/password-reset/confirm`;
- OAuth2 authorization/callback endpoints;
- public resume endpoints as already intended by existing app.

Protected endpoints:

- `/api/admin/**` requires ADMIN.
- normal app APIs require authentication as currently intended.

### CustomUserDetailsService

Responsibilities:

- load user by email;
- map role to Spring Security authority;
- enforce soft-delete/status/lock checks through Spring Security-compatible mechanisms;
- expose user id, email, username, role, status, permissions needed by auth status.

Must not:

- return password hashes in API DTOs;
- load by username;
- bypass existing account status logic.

### Password Encoding

Use Spring Security password encoding.

Rules:

- New passwords are encoded with the configured encoder.
- Existing test users receive encoded `Aa123456`.
- Do not store plain passwords.
- Do not log passwords.
- Do not expose encoded hashes.

### Login Failure Handling

Failure handler must return JSON errors.

Expected codes:

- `INVALID_CREDENTIALS`
- `EMAIL_NOT_VERIFIED`
- `ACCOUNT_LOCKED`
- `CAPTCHA_REQUIRED`
- `CAPTCHA_INVALID`
- `RATE_LIMITED`

Do not expose:

- SQL errors;
- stack traces;
- internal class names.

### Email Verification Service

Responsibilities:

- create token;
- hash token;
- store token;
- send verification email;
- verify token;
- consume token;
- redirect to frontend result.

Rules:

- 24h TTL.
- One-time use.
- Raw token only in email link.
- Previous active verification tokens for user should become unusable when new token is created.

### Password Reset Service

Responsibilities:

- accept generic reset request;
- require captcha;
- rate limit;
- create token for existing eligible user;
- send reset email;
- validate token;
- confirm reset;
- consume token;
- invalidate other active reset tokens for user.

Rules:

- 15m TTL.
- Generic request response.
- Token hash only.
- No account existence leak.

### OAuth2 Login

Responsibilities:

- start through Spring Security endpoint;
- handle Google user info/OIDC principal;
- require provider email verified;
- find existing app user by email;
- link existing user if email verified by Google;
- create new user if no existing app user;
- create `oauth_accounts` row;
- reject unverified provider email;
- redirect by role on success;
- redirect to safe auth error state on failure.

Do not:

- store Google access token;
- store Google refresh token;
- create contact details from Google name;
- create duplicate users with same email.

### Captcha Service

Responsibilities:

- verify Turnstile token in prod;
- support dev token/config in dev;
- fail safely on missing prod config.

Rules:

- no frontend secret exposure;
- Turnstile secret is backend-only;
- captcha failure returns localized clear message.

### Email Service

Use Resend.

Responsibilities:

- send verification email;
- send password reset email;
- support dev logging if configured;
- fail safely in production if missing API key.

Do not:

- silently no-op in prod;
- expose Resend API key;
- hardcode secrets.

### Auth Event Logging

Use safe application logging only.

This feature MUST NOT add an audit-log table or audit-log subsystem.

Log only these auth events:

- failed login attempt: email, IP, timestamp, reason code;
- successful login: email, IP, timestamp;
- email verification completion: email, timestamp;
- password reset completion: email, timestamp.

Never log:

- passwords;
- raw verification/reset tokens;
- token hashes;
- password hashes;
- API keys;
- OAuth client secrets;
- Turnstile secret;
- Resend secret;
- stack traces in user-facing auth responses;
- excessive PII beyond email and IP needed for operational debugging.

D47 exposure audit applies to this logging:

- check DTOs;
- check SQL SELECT columns;
- check log statements;
- check error responses.

### Legacy Cleanup

Temporary compatibility:

- `AuthInterceptor` may be marked deprecated during migration.
- `CsrfFilter` may be marked deprecated during migration.
- Old session-user source of truth may be temporarily bridged.

Final target:

- Spring Security controls auth.
- Spring Security controls authorization.
- Spring Security controls CSRF.
- No permanent double-auth.

---

## Frontend Architecture

### Routes

Add:

```txt
/app/auth/check-email
/app/auth/verified
/app/auth/forgot-password
/app/auth/reset-password
```

Keep:

```txt
/app/auth
/app/home
/app/admin
```

### Auth Service

Update `authService.ts` to support:

- register with captcha token;
- login with email, password, rememberMe, optional captcha;
- logout;
- status;
- resend verification;
- request password reset;
- validate password reset token;
- confirm password reset.

### CSRF Client Change

Change frontend CSRF header from old custom header to:

```txt
X-XSRF-TOKEN
```

Cookie remains:

```txt
XSRF-TOKEN
```

### Login Form

Changes:

- identifier label becomes email only;
- username login removed;
- remember-me remains;
- captcha appears only if required after failed attempts;
- specific messages for:
  - unverified email;
  - account locked;
  - captcha required;
  - invalid credentials.

### Register Form

Changes:

- captcha required;
- registration success does not log in;
- route to check-email state;
- copy explains email confirmation.

### Check Email Page

Simple page:

- tells user to check email;
- explains link expiry;
- offers resend verification;
- resend requires captcha.

### Verified Result Page

States:

- success;
- expired;
- invalid.

### Forgot Password Page

- email field;
- captcha;
- generic success message.

### Reset Password Page

- validates token;
- shows new password form only if token is valid;
- shows expired/invalid state otherwise.

### Google OAuth Button

- button links to backend `/oauth2/authorization/google`;
- do not build Google OAuth URL in frontend;
- hide the button when Google OAuth2 client configuration is absent;
- do not show a dead button or confusing configuration error to normal users;
- handle OAuth error query state on return.

### Admin Account Tab

Show email verification status only in Admin User Details → Account tab.

Do not add Admin Users table column.

### i18n

Update:

- `src/i18n/en.json`;
- `src/i18n/ru.json`.

Rules:

- no raw keys visible;
- natural English;
- natural Russian;
- no mixed ты/вы inconsistency inside the same surface;
- proofread landing and auth copy.

---

## Configuration

### Backend Properties

Expected properties:

```properties
app.frontend.public-base-url=
app.backend.public-base-url=

app.email.provider=resend
app.email.resend.api-key=
app.email.from=

app.captcha.enabled=
app.captcha.provider=turnstile
app.captcha.turnstile.secret-key=
app.captcha.dev-token=dev-captcha-pass

app.auth.email-verification.ttl-minutes=1440
app.auth.password-reset.ttl-minutes=15
app.auth.resend.cooldown-seconds=60
app.auth.resend.max-per-hour=5
app.auth.resend.max-per-day=20
app.auth.password-reset.max-per-ip-15m=5
app.auth.password-reset.max-per-email-15m=3
app.auth.login.captcha-after-failed-attempts=3
app.auth.login.lock-after-failed-attempts=5
app.auth.login.lock-minutes=15

spring.security.oauth2.client.registration.google.client-id=
spring.security.oauth2.client.registration.google.client-secret=
```

Because this is non-Boot, property names are illustrative. Implementer must adapt to the current project config-loading style and STOP if Spring Security OAuth2 client expects a different non-Boot configuration mechanism.

### Production Safety

Production must not silently run without:

- Resend API key;
- Turnstile secret;
- Google client id/secret if Google login is enabled;
- public backend/frontend base URLs.

Dev may allow:

- logged email links;
- dev captcha token;
- local OAuth disabled if secrets missing, as long as UI handles this safely.

---

## Security Review Findings to Implement

### SEC-001 — No Spring Boot Creativity

DeepSeek must not migrate to Spring Boot.

Any code using Boot-only assumptions must be rejected.

### SEC-002 — Spring Security Is Authoritative

Final auth/authorization source of truth must be Spring Security.

Legacy session checks must not remain as an equal second security system.

### SEC-003 — No Raw Token Storage

Email verification and password reset tokens must be stored only as hashes.

### SEC-004 — No Secret Leakage

Do not expose:

- password hashes;
- token hashes;
- raw tokens except email link;
- Resend API key;
- Turnstile secret;
- Google client secret;
- internal exceptions;
- filesystem paths;
- stack traces.

### SEC-005 — OAuth Email Verification Required

Google OAuth login must reject unverified provider emails.

### SEC-006 — No Account Enumeration in Password Reset

Forgot-password request must return generic success regardless of email existence.

### SEC-007 — Captcha Is Not DDoS Protection

Captcha protects form submissions, not GET landing from DDoS.

Landing hardening must be done through proxy/app limits and static serving configuration.

### SEC-008 — Persistent Remember-Me Must Respect Account State

Remember-me must not authenticate blocked, deleted, locked, or invalid users.

### SEC-009 — CSRF Migration Must Be Verified

Do not leave both custom CSRF and Spring Security CSRF active permanently.

### SEC-010 — Existing Test Password Reset Warning

Migration that sets all existing passwords to `Aa123456` must be documented as test-data-only and not production-safe.

### SEC-011 — Safe Auth Logging Only

Auth event logging is allowed only as safe application logging. Do not create an audit-log table or audit subsystem in this feature. Logs must not include passwords, raw tokens, token hashes, password hashes, API keys, OAuth secrets, Turnstile secrets, Resend secrets, or stack traces.

### SEC-012 — D47 Exposure Audit

Security review must check all of:

- DTO fields;
- SQL SELECT columns;
- log statements;
- error responses.

Do not rely on DTO-only review.

### SEC-013 — Public Endpoint Timing and Enumeration

Public unauthenticated auth endpoints must avoid obvious enumeration leaks. Use generic responses and simple uniform delay where appropriate for forgot-password, reset-token validation, email verification, and resend-verification flows. Do not overbuild a new timing framework.

---

## Implementation Phases

### Phase 0 — Baseline Security Map

**Goal**: Understand current auth/security code before editing.

Required MCP:

- Context7 MCP for Spring Security non-Boot servlet basics and current APIs.
- Serena MCP for code navigation.
- Postgres MCP for current DB schema.
- Playwright MCP only if baseline UI evidence is needed.

Tasks:

1. Inspect current auth backend flow:
   - `AuthController`;
   - `AuthService`;
   - `UserDao`;
   - `UserSession`;
   - `AuthInterceptor`;
   - `CsrfFilter`;
   - `PasswordService`;
   - `PasswordStrengthValidator`.
2. Inspect current frontend auth flow:
   - `AuthPage.vue`;
   - `LoginForm.vue`;
   - `RegisterForm.vue`;
   - `authService.ts`;
   - `useAuth.ts`;
   - router guard.
3. Inspect current admin auth behavior.
4. Inspect current database:
   - users table;
   - role/status/permission lookup tables;
   - failed login fields;
   - password hash style;
   - soft-delete fields.
5. Inspect current tests.
6. Confirm which files must not be touched.

STOP checkpoint:

- Report baseline findings.
- List exact files to be affected.
- List exact migrations needed.
- Confirm no unrelated domains need edits.
- Wait for user approval.

---

### Phase 1 — Dependencies and Non-Boot Spring Security Bootstrap

**Goal**: Add Spring Security dependencies and explicit non-Boot integration without changing business behavior more than necessary.

Required MCP:

- Context7 MCP for Spring Security servlet/non-Boot setup.
- Serena MCP for project initializer/config edits.

Tasks:

1. Add allowed Spring Security dependencies.
2. Register Spring Security filter chain in the existing non-Boot application.
3. Do NOT use Spring Boot `FilterRegistrationBean`; use the existing non-Boot servlet initializer style such as `AppInitializer.getServletFilters()` / `DelegatingFilterProxy`, or STOP if a different documented non-Boot registration method is needed.
4. Create minimal `SecurityConfig`.
5. Ensure app starts.
6. Keep endpoints temporarily permissive if needed for bootstrap only.
7. Add tests proving Spring Security filter chain is active.
8. Add MockMvc/Spring Security test setup notes so tests do not accidentally rely on fresh-session-per-perform behavior.

STOP checkpoint:

- Show dependency diff.
- Show non-Boot registration mechanism.
- Show startup/test evidence.
- Confirm no Spring Boot classes/starters were added.
- Wait for user approval.

---

### Phase 2 — Database Migrations

**Goal**: Add required auth schema safely.

Required MCP:

- Context7 MCP for JDBC/Flyway references if needed.
- Serena MCP for migration files.
- Postgres MCP before and after migrations.

Tasks:

1. Verify current Flyway explicit configuration and confirm new migrations are picked up by the existing non-Boot setup.
2. Add users auth columns.
3. Add `auth_tokens`.
4. Add `oauth_accounts`.
5. Add `persistent_logins`.
6. Add existing test-user migration to:
   - mark email verified;
   - set encoded `Aa123456`;
   - preserve roles/status/permissions/deletion flags.
7. Add DAO tests/migration evidence where project style supports it.

STOP checkpoint:

- Show migration files.
- Show Postgres MCP schema verification.
- Show sample existing user after migration.
- Confirm no data deletion.
- Wait for user approval.

---

### Phase 3 — Password Encoding and UserDetails

**Goal**: Build Spring Security user loading and password verification.

Required MCP:

- Context7 MCP for PasswordEncoder, UserDetailsService.
- Serena MCP for service/DAO edits.
- Postgres MCP if verifying users.

Tasks:

1. Implement password encoder config.
2. Implement `CustomUserDetails`.
3. Implement `CustomUserDetailsService`.
4. Load by email only.
5. Map role to authorities.
6. Enforce deleted/blocked/locked states.
7. Preserve failed login fields.
8. Add tests.

STOP checkpoint:

- Show login identifier is email only.
- Show role mapping.
- Show account state checks.
- Show tests.
- Wait for user approval.

---

### Phase 4 — JSON Login/Logout/Status Migration

**Goal**: Move password login/logout/status to Spring Security while preserving SPA contract.

Required MCP:

- Context7 MCP for Spring Security custom login handlers.
- Serena MCP for controller/service/frontend contract.
- Playwright MCP for browser verification after implementation.

Tasks:

1. Configure JSON login endpoint.
2. Add success handler.
3. Add failure handler.
4. Preserve rememberMe request field.
5. Implement JSON logout.
6. Keep `/api/auth/status` compatible.
7. Add safe application logging for successful logins.
8. Add tests proving successful login logging excludes passwords, token values, hashes, secrets, and stack traces.
9. Update tests.
10. Verify frontend login/logout/status.

STOP checkpoint:

- Show API examples.
- Show tests.
- Show Playwright evidence.
- Confirm legacy session is not source of truth.
- Wait for user approval.

---

### Phase 5 — Failed Login, Captcha Trigger, and Account Lock

**Goal**: Preserve and integrate failed login protection.

Required MCP:

- Context7 MCP for authentication failure handling.
- Serena MCP for auth service/DAO edits.
- Postgres MCP for failed counters if needed.

Tasks:

1. Increment failed attempts on bad password.
2. Require captcha after 3 failed attempts.
3. Lock account after 5 failed attempts.
4. Lock duration 15 minutes.
5. Reset counters on successful login.
6. Add safe application logging for failed login attempts with email, IP, timestamp, and reason code only.
7. Add tests proving failed-login logs exclude passwords, hashes, raw tokens, secrets, and stack traces.
8. Add auth error codes.
9. Add tests.

STOP checkpoint:

- Show failed attempt behavior.
- Show lock behavior.
- Show captcha-required behavior.
- Show tests.
- Wait for user approval.

---

### Phase 6 — Spring Security CSRF Migration

**Goal**: Replace custom CSRF filter with Spring Security CSRF.

Required MCP:

- Context7 MCP for Spring Security CSRF and CookieCsrfTokenRepository.
- Serena MCP for backend/frontend edits.
- Playwright MCP for browser verification.

Tasks:

1. Configure CSRF cookie `XSRF-TOKEN`.
2. Configure frontend header `X-XSRF-TOKEN`.
3. Update HTTP client.
4. Mark legacy `CsrfFilter` deprecated if still temporarily present.
5. Remove legacy `CsrfFilter` after proof.
6. Add/update tests.

STOP checkpoint:

- Show unsafe request with valid CSRF succeeds.
- Show unsafe request without CSRF fails.
- Show frontend sends `X-XSRF-TOKEN`.
- Confirm legacy CSRF is removed or scheduled for immediate cleanup.
- Wait for user approval.

---

### Phase 7 — Authorization and Legacy AuthInterceptor Cleanup

**Goal**: Move backend route protection to Spring Security.

Required MCP:

- Context7 MCP for authorization matchers/authorities.
- Serena MCP for interceptor/config references.
- Playwright MCP for admin/non-admin checks.

Tasks:

1. Protect `/api/admin/**` with ADMIN authority.
2. Protect user APIs according to existing behavior.
3. Ensure public endpoints remain public only where intended.
4. Mark legacy `AuthInterceptor` deprecated if temporarily present.
5. Remove legacy `AuthInterceptor` after proof.
6. Add tests.

STOP checkpoint:

- Show admin endpoint rejects unauthenticated.
- Show admin endpoint rejects USER.
- Show admin endpoint allows ADMIN.
- Confirm frontend guard is UX only.
- Wait for user approval.

---

### Phase 8 — Email Service with Resend

**Goal**: Add email delivery abstraction and Resend implementation.

Required MCP:

- Context7 MCP for Java HTTP client or current project HTTP style.
- Serena MCP for service/config edits.

Tasks:

1. Implement email template service.
2. Implement bilingual plain text + HTML templates.
3. Implement Resend email service.
4. Implement dev logging mode.
5. Implement prod missing-config fail-safe.
6. Add tests with mocked email sending.

STOP checkpoint:

- Show example verification email.
- Show example reset email.
- Show dev missing-key behavior.
- Show prod missing-key behavior.
- Wait for user approval.

---

### Phase 9 — Email Verification

**Goal**: Implement strict registration confirmation.

Required MCP:

- Context7 MCP for Spring Security registration/auth considerations.
- Serena MCP for auth controller/service/DAO.
- Postgres MCP for token verification.
- Playwright MCP for browser flow.

Tasks:

1. Update registration request with captcha token.
2. Registration creates unverified user.
3. Registration does not login.
4. Create hashed verification token.
5. Send verification email.
6. Add backend verify endpoint.
7. Redirect to frontend verified status page.
8. Add safe application logging for email verification completion only.
9. Add simple uniform delay where appropriate for public verification/resend outcomes to reduce enumeration timing signals.
10. Add resend verification endpoint.
11. Add cooldown/hour/day rate limits.
12. Add tests proving verification logs exclude raw tokens, token hashes, secrets, and stack traces.
13. Add tests.

STOP checkpoint:

- Show register response.
- Show unverified user cannot login.
- Show verification succeeds.
- Show expired/invalid token behavior.
- Show resend rate limit behavior.
- Show Playwright evidence.
- Wait for user approval.

---

### Phase 10 — Password Reset

**Goal**: Implement safe forgot-password and reset-password flow.

Required MCP:

- Context7 MCP for secure token/password practices.
- Serena MCP for backend/frontend.
- Postgres MCP for token state.
- Playwright MCP for browser flow.

Tasks:

1. Add forgot-password endpoint.
2. Require captcha.
3. Return generic response.
4. Add simple uniform delay where appropriate for forgot-password and reset-token validation outcomes to reduce enumeration timing signals.
5. Create hashed 15-minute token for existing eligible user.
6. Send reset email.
7. Add validate token endpoint.
8. Add confirm reset endpoint.
9. Consume token.
10. Invalidate other active reset tokens for user.
11. Add safe application logging for password reset completion only.
12. Add tests proving reset logs exclude passwords, raw tokens, token hashes, password hashes, secrets, and stack traces.
13. Add frontend pages.
14. Add tests.

STOP checkpoint:

- Show generic unknown-email response.
- Show valid token flow.
- Show expired/invalid/consumed behavior.
- Show password changed and login works.
- Show Playwright evidence.
- Wait for user approval.

---

### Phase 11 — Persistent Remember-Me

**Goal**: Implement full Spring Security persistent remember-me.

Required MCP:

- Context7 MCP for Spring Security remember-me and persistent token repository.
- Serena MCP for security config.
- Postgres MCP for `persistent_logins`.
- Playwright MCP if practical.

Tasks:

1. Configure persistent remember-me.
2. Use email as remember-me username.
3. Store tokens in `persistent_logins`.
4. Ensure logout clears remember-me.
5. Ensure blocked/deleted/locked users cannot use remember-me.
6. Add tests.

STOP checkpoint:

- Show persistent token created.
- Show logout clears token/cookie.
- Show blocked/deleted user cannot use remember-me.
- Wait for user approval.

---

### Phase 12 — Google OAuth2

**Goal**: Add Google OAuth2 login through Spring Security.

Required MCP:

- Context7 MCP for Spring Security OAuth2 Login and OIDC user handling.
- Serena MCP for security config/service/DAO.
- Postgres MCP for `oauth_accounts`.
- Playwright MCP for browser start/error states.

Tasks:

1. Configure Google OAuth2 client for non-Boot app.
2. Add a safe backend/frontend config signal that tells frontend whether Google OAuth2 is configured.
3. Add frontend Continue with Google button only when Google OAuth2 is configured.
4. Hide the button when Google client ID/secret are absent; do not show dead button to users.
5. Start flow through `/oauth2/authorization/google`.
4. Handle OAuth success.
5. Require provider email verified.
6. Auto-link existing user by verified email.
7. Create new user for verified new email.
8. Create `oauth_accounts` record.
9. Reject unverified provider email with clear message.
10. Redirect by role.
11. Add tests with mocked OAuth principals.

STOP checkpoint:

- Show new-user OAuth behavior.
- Show existing-user auto-link.
- Show unverified provider email rejection.
- Show duplicate provider identity does not duplicate account.
- Show Playwright evidence for start/error state.
- Wait for user approval.

---

### Phase 13 — Frontend Auth UI and i18n Polish

**Goal**: Complete UI states and wording.

Required MCP:

- Context7 MCP for Vue/Vue Router/Vitest/vue-i18n.
- Serena MCP for frontend navigation/edits.
- Playwright MCP for UI evidence.

Tasks:

1. Add check-email page.
2. Add verified result page.
3. Add forgot-password page.
4. Add reset-password page.
5. Update auth page/forms.
6. Update auth service/composable.
7. Update router.
8. Add OAuth error handling.
9. Add captcha UI integration.
10. Add frontend behavior/test for hiding Google button when OAuth2 config is absent.
11. Update EN/RU i18n.
12. Proofread landing copy.
13. Add frontend tests.

STOP checkpoint:

- Show screenshots of all auth states.
- Show language switch evidence.
- Show no raw i18n keys.
- Show tests.
- Wait for user approval.

---

### Phase 14 — Admin Account Tab Email Verification Status

**Goal**: Show email verification status only in Admin User Details Account tab.

Required MCP:

- Context7 MCP for Vue/PrimeVue if UI component needed.
- Serena MCP for admin DTO/service/component.
- Playwright MCP for admin UI check.

Tasks:

1. Add backend DTO field for user details account verification status.
2. Do not expose sensitive auth fields.
3. Display status in Account tab.
4. Do not add Users table column.
5. Add tests.

STOP checkpoint:

- Show Account tab status.
- Confirm Users table unchanged.
- Confirm non-admin cannot access.
- Wait for user approval.

---

### Phase 15 — Production Hardening and Landing/Public Edge

**Goal**: Document and implement deploy-oriented guardrails.

Required MCP:

- Context7 MCP for relevant Spring/Security config.
- Serena MCP for config files.
- Playwright MCP for smoke checks.

Tasks:

1. Add production config validation.
2. Add cookie security configuration.
3. Document Nginx/proxy rate limiting for landing.
4. Add request/body limit guidance/config where appropriate.
5. Ensure no secrets enter frontend build.
6. Add tests or manual evidence.

STOP checkpoint:

- Show prod missing-secret behavior.
- Show dev fallback behavior.
- Show cookie config.
- Show landing protection notes/config.
- Wait for user approval.

---

### Phase 16 — Legacy Cleanup and Regression Sweep

**Goal**: Remove temporary legacy auth code and prove no major app regression.

Required MCP:

- Context7 MCP for final verification topics as needed.
- Serena MCP for reference checks before deletion.
- Postgres MCP for auth tables after flows.
- Playwright MCP for end-to-end smoke.

Tasks:

1. Find all references to deprecated legacy auth code.
2. Remove old `AuthInterceptor` if no longer needed.
3. Remove old `CsrfFilter` if no longer needed.
4. Remove old custom session source-of-truth logic.
5. Keep only DTO compatibility required by frontend.
6. Run backend tests.
7. Run frontend tests.
8. Run browser smoke:
   - register;
   - verify email;
   - login;
   - logout;
   - forgot/reset password;
   - admin protection;
   - Google OAuth start/error;
   - user home;
   - profile;
   - generate route access;
   - public landing.
9. Confirm untouched domains:
   - PDF;
   - AI;
   - generation;
   - finalization.
10. Perform D47 exposure audit across:
   - DTO fields;
   - SQL SELECT columns;
   - log statements;
   - error responses.
11. Confirm auth event logging is application logging only and no audit-log table/subsystem was added.

Final STOP checkpoint:

- Show changed files.
- Show removed legacy files/classes.
- Show full test output.
- Show Playwright evidence.
- Show DB evidence.
- Show D47 exposure audit results: DTOs + SQL SELECT columns + logs + error responses.
- Show auth logging evidence and proof no secrets/hashes/raw tokens are logged.
- Confirm no audit-log table/subsystem was added.
- Confirm no forbidden dependencies.
- Confirm no Spring Boot.
- Confirm no PDF/AI/generation changes.
- Ask user before final commit/PR.

---

## Testing Strategy

### Backend Tests

MockMvc/Spring Security caveat:

- Avoid relying on MockMvc standalone behavior that creates a fresh session for each `perform()`.
- Use WebApplicationContext-based Spring Security test setup where needed.
- Use Spring Security test helpers/annotations when appropriate.
- If a test must use session state, configure `MockHttpSession` explicitly.

Required areas:

- SecurityConfig bootstrap.
- Login success/failure.
- Email-only login.
- Unverified user rejection.
- Existing migrated test user login.
- Failed attempts.
- Captcha required after threshold.
- Account lock.
- Logout.
- Remember-me.
- CSRF.
- Admin authorization.
- Registration.
- Email verification token creation/verification.
- Resend verification limits.
- Password reset request/validate/confirm.
- OAuth new user.
- OAuth existing user link.
- OAuth unverified email rejection.
- Resend email service mock.
- Turnstile captcha service mock/dev/prod behavior.
- Auth DTO error codes.
- Auth event logging for failed login, successful login, email verification completion, password reset completion.
- Negative log-safety tests proving no passwords, raw tokens, token hashes, password hashes, API keys, secrets, or stack traces are logged.
- Public endpoint generic responses and simple uniform delay behavior where applicable.

### Frontend Tests

Required areas:

- Auth page login by email.
- Register pending check-email state.
- Email verification result states.
- Resend verification state.
- Forgot-password form.
- Reset-password valid/invalid token states.
- Captcha-required message.
- OAuth button URL.
- OAuth error state.
- CSRF header change.
- Admin Account tab email verification status.
- i18n keys for new surfaces.

### Playwright MCP Evidence

Required screenshots/evidence:

1. Auth page.
2. Register with captcha.
3. Check email page.
4. Verified success page.
5. Unverified login rejection.
6. Forgot password page.
7. Reset password page.
8. Login success.
9. Logout.
10. Admin access allowed for admin.
11. Admin access rejected for non-admin.
12. Account tab email verification status.
13. OAuth button/start.
14. Auth UI in EN and RU.

### Postgres MCP Evidence

Required verification:

1. users new columns exist.
2. existing users marked verified.
3. existing test password migration applied.
4. auth_tokens records created/consumed.
5. oauth_accounts records created.
6. persistent_logins record created/removed.
7. no user/resume/profile data hard-deleted.

---

## Risk Register

### RISK-001 — DeepSeek Adds Spring Boot

**Severity**: Critical  
**Mitigation**: Explicit forbidden dependency list, STOP after dependency phase, reject Boot artifacts.

### RISK-002 — Double Auth System

**Severity**: Critical  
**Mitigation**: Temporary deprecation only, final cleanup phase, reference scan with Serena.

### RISK-003 — CSRF Breaks Frontend

**Severity**: High  
**Mitigation**: Dedicated CSRF phase, update header to `X-XSRF-TOKEN`, Playwright evidence.

### RISK-004 — User Lockout

**Severity**: High  
**Mitigation**: Existing test user migration, role preservation, checkpoint login proof.

### RISK-005 — OAuth Creates Duplicate Accounts

**Severity**: High  
**Mitigation**: verified email auto-link, unique provider identity, email unique behavior.

### RISK-006 — Token Leakage

**Severity**: High  
**Mitigation**: hash-only tokens, no raw token logs except dev email link, DTO checks.

### RISK-007 — Email/Captcha Silent No-Op in Prod

**Severity**: High  
**Mitigation**: prod config validation.

### RISK-008 — Remember-Me Bypasses Status

**Severity**: High  
**Mitigation**: UserDetails account checks on remember-me auth.

### RISK-009 — Scope Explosion

**Severity**: High  
**Mitigation**: no account settings, no multi-provider OAuth, no forced password change, no admin table redesign.

### RISK-010 — Breaking Unrelated PDF/AI/Generation

**Severity**: High  
**Mitigation**: forbidden file list, final diff review, no edits outside auth/security/frontend auth/admin account status.

### RISK-011 — Auth Logs Leak Secrets

**Severity**: High  
**Mitigation**: safe application logging only, explicit negative tests, D47 exposure audit.

### RISK-012 — Google Button Shown When OAuth Is Not Configured

**Severity**: Medium  
**Mitigation**: config signal, frontend conditional rendering, frontend test.

### RISK-013 — Non-Boot Filter Registration Uses Boot API

**Severity**: Critical  
**Mitigation**: forbid `FilterRegistrationBean`, require non-Boot servlet registration evidence in Phase 1.

---

## Constitution Check

| Principle | Status | Notes |
|---|---|---|
| Code Quality & Maintainability | PASS | Spring Security migration is large but controlled by phases and cleanup. |
| Testing Excellence | PASS | TDD and mandatory evidence for security flows. |
| UX Consistency | PASS | SPA JSON auth preserved. |
| Performance & Reliability | PASS | Simple DB-backed tokens and rate limits. |
| Security by Design | PASS | Spring Security authoritative, hashed tokens, captcha, rate limits, OAuth email verification. |
| Scope Control | PASS WITH RISK | Full migration is high-risk; STOP gates are mandatory. |

---

## DeepSeek Safety Protocol

DeepSeek must follow these rules:

1. Never use Spring Boot examples directly.
2. Never add dependencies outside the approved list without STOP.
3. Never edit PDF/AI/generation/finalization code.
4. Never hard-delete user data.
5. Never store raw verification/reset tokens.
6. Never expose secrets or hashes in DTOs.
7. Never leave old auth and new auth both active as permanent systems.
8. Never bypass tests to make compilation pass.
9. Never remove failing tests without explanation and user approval.
10. Never use broad grep/blind edits where Serena can inspect symbols.
11. Never guess database schema; use Postgres MCP.
12. Never claim browser behavior is verified without Playwright MCP evidence.
13. Never proceed past STOP checkpoints without user approval.
14. Never invent extra flows not in spec.
15. If unsure, STOP and report options.

---

## Open Implementation Questions

No product-level open questions remain.

Implementation-level conflicts must be handled by STOP report with:

1. exact file/class/method;
2. exact conflicting requirement;
3. options;
4. recommended safest option;
5. no code change until approval.
