# Feature Specification: Auth Hardening and Spring Security Migration

**Feature Branch**: `feat/011-auth-hardening`

**Created**: 2026-06-30

**Status**: Draft v0.2 — review-corrected

**Input**: Full authentication hardening before production deploy: migrate existing custom session authentication to Spring Security, add strict email confirmation, password reset by email, Google OAuth2 login, persistent remember-me, Cloudflare Turnstile captcha for public auth forms, Resend email delivery, landing anti-abuse hardening, and auth/i18n wording polish.

> **Important**: This spec is the source of truth for `feat/011-auth-hardening`. If any requirement conflicts with the current codebase, STOP and ask for clarification before planning or coding. Do not silently reinterpret requirements. Do not expand scope into Spring Boot, JWT auth, JPA/Hibernate, unrelated profile editing, PDF generation, AI generation, prompt generation, resume finalization, admin table redesign, or unrelated UI redesign.

> **Review correction v0.2**: API Contract Draft and Auth Error Codes are included to prevent implementation drift during the Spring Security migration. i18n wording was corrected to distinguish frontend JSON i18n from backend/landing message resources.

---

## Brainstorm Log

### Session 2026-06-30

Spec refinement decisions:

1. **Q: Feature branch name** → **A**: Use `feat/011-auth-hardening`.
2. **Q: Migration scope** → **A**: Full migration from custom session auth to Spring Security in this feature.
3. **Q: Spring Boot allowed?** → **A**: No. This is a non-Boot Spring MVC project. Spring Boot starters and Spring Boot auto-configuration assumptions are out of scope.
4. **Q: Persistence style** → **A**: Keep plain JDBC, existing DAO patterns, Flyway migrations, and the current custom database infrastructure. Do not add JPA/Hibernate.
5. **Q: Frontend auth style** → **A**: Keep SPA-friendly JSON auth APIs. Do not switch to Spring Security default form-login pages or redirect-based HTML login.
6. **Q: Login identifier** → **A**: Email only. `username` remains a display/profile identity, not a login credential.
7. **Q: Existing users** → **A**: Treat all existing users as email-verified. Because current users are capstone/test data, migrate their password to encoded `Aa123456`. This is a test-data-only shortcut and must be documented as forbidden for real production data.
8. **Q: Registration behavior** → **A**: Strict email confirmation. New user is created as unverified and is not logged in until email is confirmed.
9. **Q: Email verification link** → **A**: Verification link points to backend endpoint. Backend validates token and redirects to frontend result page.
10. **Q: Email verification TTL** → **A**: 24 hours.
11. **Q: Password reset TTL** → **A**: 15 minutes.
12. **Q: Token storage** → **A**: Store only hashed verification/reset tokens. Never store raw tokens.
13. **Q: Auth token tables** → **A**: Use one simple `auth_tokens` table with explicit token types. Do not create an abstract token framework.
14. **Q: Google OAuth2 existing email behavior** → **A**: If Google returns a verified email that matches an existing user, automatically link the Google account to the existing app account.
15. **Q: Google OAuth2 unverified email behavior** → **A**: Reject login with a clear user-facing explanation. Do not link or create accounts using unverified provider emails.
16. **Q: Google OAuth2 new user behavior** → **A**: Allow any verified Google account. Create a new app user if email is new.
17. **Q: Contact profile from Google** → **A**: Do not create or populate `contact_detail` from Google profile name in this feature.
18. **Q: OAuth-only users** → **A**: Store a technical random password hash and disable password login unless the user later sets a real password through an explicit future flow.
19. **Q: OAuth identity model** → **A**: Create `oauth_accounts` table. Do not put provider fields directly into `users`.
20. **Q: Remember-me** → **A**: Implement proper Spring Security persistent remember-me using standard persistent token storage.
21. **Q: CSRF** → **A**: Migrate to Spring Security CSRF. Use cookie `XSRF-TOKEN` and frontend header `X-XSRF-TOKEN`.
22. **Q: Legacy CSRF filter** → **A**: Mark legacy `CsrfFilter` deprecated during migration, then remove it after Spring Security CSRF proof.
23. **Q: Legacy auth interceptor** → **A**: Mark legacy `AuthInterceptor` deprecated during migration, then remove it after Spring Security authorization proof.
24. **Q: `UserSession` DTO** → **A**: Keep it as frontend response contract for `/api/auth/status`, but Spring Security `Authentication` becomes the source of truth.
25. **Q: Captcha provider** → **A**: Cloudflare Turnstile.
26. **Q: Captcha dev mode** → **A**: Config/profile driven. Dev may accept `dev-captcha-pass`; prod must require real Turnstile keys.
27. **Q: Captcha placement** → **A**: Required for registration, forgot password, resend confirmation. Required for login only after failed/suspicious attempts.
28. **Q: Email provider** → **A**: Resend. Use free tier for portfolio deployment.
29. **Q: Email templates** → **A**: One bilingual template: English first, Russian duplicate below. Provide plain text and basic HTML.
30. **Q: Email provider fallback** → **A**: Dev may log email links if API key is absent. Prod must not silently no-op.
31. **Q: Landing DDoS wording** → **A**: Captcha does not protect GET landing from DDoS. Landing protection must be proxy/Nginx rate limiting, static caching, request/body limits, and safe public endpoint limits.
32. **Q: Admin UI change** → **A**: Show email verification status only in Admin User Details → Account tab. Do not add a column to Admin Users table.
33. **Q: Password rules** → **A**: Keep existing password strength rules.
34. **Q: Logout behavior** → **A**: Logout invalidates session, clears remember-me, clears security context, returns JSON success, and frontend redirects to auth page.
35. **Q: Admin role recovery** → **A**: Preserve existing roles during migration. The user will adjust test admin role manually through DBeaver if needed. Do not add dev bootstrap admin in this feature.

---

## Clarifications

### Session 2026-06-30

- **Q: What auth events should be logged?** → **A**: Log failed login attempts (email, IP, timestamp, reason), successful logins, email verification completions, password reset completions. Never log passwords, raw tokens, token hashes, password hashes, API keys, or secrets. Auth logging must not expose PII beyond email and IP for operational debugging.
- **Q: How should the frontend handle Google OAuth when not configured?** → **A**: Hide the "Continue with Google" button when Google client ID/secret are not present in configuration. Do not show an error or dead button.

---

## User Scenarios & Testing

### User Story 1 — Migrate Authentication to Spring Security Without Breaking Existing User UX (Priority: P1)

As a registered user, I want login, logout, auth status, and protected routes to keep working after the security migration so that the app remains usable while becoming more production-ready.

**Why this priority**: The feature is a full auth migration. If basic login/status/logout breaks, every protected feature becomes unstable.

**Independent Test**: Start the app, log in by email/password through the Vue auth page, verify `/api/auth/status`, access `/app/home`, log out, and verify protected pages reject access.

**Acceptance Scenarios**:

1. **Given** a verified user enters valid email and password, **When** the user submits login, **Then** authentication succeeds and the response remains SPA-friendly JSON.
2. **Given** a verified admin enters valid email and password, **When** login succeeds, **Then** role-based redirect points to the admin area.
3. **Given** a verified regular user enters valid email and password, **When** login succeeds, **Then** role-based redirect points to the user home area.
4. **Given** a user is authenticated, **When** frontend calls `/api/auth/status`, **Then** it receives an authenticated response compatible with the existing frontend auth contract.
5. **Given** a user is not authenticated, **When** frontend calls `/api/auth/status`, **Then** it receives a safe unauthenticated response, not a server error.
6. **Given** a user logs out, **Then** the server invalidates the session, clears remember-me state, clears security context, and returns JSON success.
7. **Given** a logged-out user tries to open protected app routes, **Then** the app redirects to auth according to existing frontend behavior.
8. **Given** a non-admin user attempts to call `/api/admin/**`, **Then** backend authorization rejects the request regardless of frontend route guard.
9. **Given** an unauthenticated user attempts to call protected APIs, **Then** backend rejects the request with safe authentication failure behavior.
10. **Given** Spring Security is enabled, **Then** legacy custom session storage is not the source of truth for authentication.

---

### User Story 2 — Preserve Existing Test Accounts During Migration (Priority: P1)

As the project owner, I want existing capstone/test accounts to remain usable after the migration so that I do not lose access during development and demo.

**Why this priority**: A full auth migration can lock out all users if data migration is mishandled.

**Independent Test**: Run migrations against existing dev/test data, then log in using an existing account with migrated credentials.

**Acceptance Scenarios**:

1. **Given** existing users are present before the migration, **When** migrations run, **Then** existing users are marked email-verified.
2. **Given** existing users are test data, **When** migrations run, **Then** their password hash is replaced with an encoded value for `Aa123456`.
3. **Given** migrated existing users, **When** login is attempted with `Aa123456`, **Then** login succeeds if the account is active and not locked.
4. **Given** migrated existing users, **Then** roles, status, generation permission, privileged flag, soft-delete flags, and ownership data remain unchanged.
5. **Given** this migration is documented, **Then** it clearly states that resetting all existing passwords is allowed only for capstone/test data and is forbidden for real production user data.
6. **Given** migrations run, **Then** no profile, resume, PDF, AI, or admin moderation data is deleted.

---

### User Story 3 — Register With Strict Email Confirmation (Priority: P1)

As a new user, I want to register and confirm my email before logging in so that account ownership is verified before the account becomes active.

**Why this priority**: Verified email ownership is required before production deployment and before password reset or OAuth linking can be trusted.

**Independent Test**: Register with a new email, verify that the user is not logged in, inspect or receive the verification link, open it, then log in successfully.

**Acceptance Scenarios**:

1. **Given** a visitor opens the registration form, **Then** the form requires email, username, password, password confirmation, and captcha.
2. **Given** the registration form is submitted without a valid captcha, **Then** registration is rejected with a clear captcha message.
3. **Given** the registration form is submitted with valid data and valid captcha, **Then** the app creates an unverified user account.
4. **Given** registration succeeds, **Then** the user is not automatically logged in.
5. **Given** registration succeeds, **Then** the user is taken to `/app/auth/check-email`.
6. **Given** registration succeeds, **Then** an email verification token is created with a 24-hour TTL.
7. **Given** registration succeeds, **Then** the raw token is sent only through email and is not stored in the database.
8. **Given** registration succeeds, **Then** the stored token value is hashed.
9. **Given** a duplicate email is submitted, **Then** registration fails safely and does not create another account.
10. **Given** weak or mismatched passwords are submitted, **Then** existing password validation behavior is preserved.
11. **Given** registration fails, **Then** no partially authenticated session is created.

---

### User Story 4 — Confirm Email Through Verification Link (Priority: P1)

As a newly registered user, I want to confirm my email by opening a link so that I can log in after proving email ownership.

**Why this priority**: Registration is strict; email confirmation is the required activation path.

**Independent Test**: Use a valid verification token, verify redirect status, and confirm login becomes available after verification.

**Acceptance Scenarios**:

1. **Given** the user opens a valid verification link, **Then** the backend verifies the token and marks the user's email as verified.
2. **Given** the user opens a valid verification link, **Then** the token is marked consumed and cannot be reused.
3. **Given** verification succeeds, **Then** the backend redirects to `/app/auth/verified?status=success`.
4. **Given** the user opens an expired token, **Then** verification fails safely and redirects to a clear expired-token result.
5. **Given** the user opens an invalid token, **Then** verification fails safely and redirects to a clear invalid-token result.
6. **Given** a consumed token is opened again, **Then** verification does not repeat and returns a safe invalid/used-token result.
7. **Given** verification succeeds, **Then** the user can log in with email and password.
8. **Given** verification fails, **Then** no user is logged in and no account data is leaked.

---

### User Story 5 — Resend Email Verification (Priority: P1)

As a newly registered but unverified user, I want to request another verification email so that I can complete registration if the first email was lost or expired.

**Why this priority**: Strict email confirmation needs a recovery path that is safe against abuse.

**Independent Test**: Request resend for an unverified email, verify rate limits, captcha requirement, token invalidation or replacement, and email delivery/log behavior.

**Acceptance Scenarios**:

1. **Given** an unverified user requests a new verification email, **Then** captcha is required.
2. **Given** captcha is invalid, **Then** resend is rejected with a clear captcha message.
3. **Given** captcha is valid and rate limits allow the request, **Then** a new verification email is sent or logged according to environment.
4. **Given** a new verification token is created, **Then** old active verification tokens for that user are invalidated or made unusable.
5. **Given** resend is requested too soon, **Then** it is rejected by cooldown.
6. **Given** resend exceeds hourly or daily limits, **Then** it is rejected by rate limiting.
7. **Given** resend is requested for an email that is already verified, missing, or unknown, **Then** response remains safe and does not leak account existence unnecessarily.
8. **Given** resend succeeds, **Then** response is user-friendly and does not expose the raw token.

---

### User Story 6 — Prevent Login Before Email Verification (Priority: P1)

As an unverified user, I want a clear message when I try to log in before confirming email so that I understand what action is required.

**Why this priority**: Strict verification changes the old registration flow; the user must not be confused by a generic wrong-password message.

**Independent Test**: Register a new user without verifying email, attempt login, and verify the specific unverified-email response and resend option.

**Acceptance Scenarios**:

1. **Given** an unverified user enters correct email and password, **When** login is submitted, **Then** login is rejected.
2. **Given** login is rejected because email is not verified, **Then** the response uses a specific auth error code.
3. **Given** login is rejected because email is not verified, **Then** the frontend shows a clear message and offers a resend-verification path.
4. **Given** an unverified user enters the wrong password, **Then** the app must not accidentally verify or log in the user.
5. **Given** an unverified user becomes verified, **Then** the same credentials can be used to log in.

---

### User Story 7 — Request Password Reset by Email (Priority: P1)

As a user who forgot my password, I want to request a reset link by email so that I can regain access without contacting an administrator.

**Why this priority**: Password reset is mandatory for production-ready authentication.

**Independent Test**: Request reset for an existing email and for an unknown email; both must return generic success messaging while only the real account receives a usable token.

**Acceptance Scenarios**:

1. **Given** a visitor opens forgot-password page, **Then** the page asks for email and captcha.
2. **Given** captcha is invalid, **Then** reset request is rejected with a clear captcha message.
3. **Given** reset is requested for an existing verified user, **Then** a password reset token is created.
4. **Given** reset token is created, **Then** it expires after 15 minutes.
5. **Given** reset token is created, **Then** raw token is not stored in the database.
6. **Given** reset token is created, **Then** stored token value is hashed.
7. **Given** reset is requested for an unknown email, **Then** response is generic and does not reveal that the account does not exist.
8. **Given** reset is requested too frequently, **Then** rate limiting applies per email and per IP.
9. **Given** reset request succeeds in production, **Then** Resend sends the reset email.
10. **Given** reset request succeeds in development without Resend key, **Then** reset link may be logged for developer testing.

---

### User Story 8 — Reset Password With One-Time Token (Priority: P1)

As a user with a valid reset link, I want to set a new password so that I can log in again securely.

**Why this priority**: The reset request is useless without a safe token validation and password update flow.

**Independent Test**: Open reset page with valid, invalid, expired, and consumed tokens; confirm only the valid token allows password change.

**Acceptance Scenarios**:

1. **Given** a user opens `/app/auth/reset-password?token=...` with a valid token, **Then** the app shows the new password form.
2. **Given** the token is invalid, expired, or consumed, **Then** the app shows a safe error state and does not show a usable reset form.
3. **Given** the user submits a valid new password with a valid token, **Then** the password is updated.
4. **Given** password update succeeds, **Then** the token is consumed and cannot be reused.
5. **Given** password update succeeds, **Then** other active password reset tokens for the same user are invalidated.
6. **Given** password update succeeds, **Then** failed login counters are reset if applicable.
7. **Given** the new password is weak or mismatched, **Then** existing password validation behavior is preserved.
8. **Given** the token is expired during submission, **Then** password is not changed.
9. **Given** reset succeeds, **Then** the user is guided back to login.

---

### User Story 9 — Sign In With Google OAuth2 (Priority: P1)

As a user, I want to sign in with my verified Google account so that I can access the app without managing a separate password.

**Why this priority**: Google sign-in is a key production-auth feature and a strong portfolio signal.

**Independent Test**: Start Google OAuth2 flow, complete provider login, and verify user creation or linking behavior.

**Acceptance Scenarios**:

1. **Given** the user clicks Continue with Google, **Then** the OAuth2 flow starts from the backend.
2. **Given** Google returns a verified email that does not exist in the app, **Then** a new app user is created.
3. **Given** a new app user is created through Google, **Then** the user is marked email-verified.
4. **Given** a new app user is created through Google, **Then** a technical random password hash is stored.
5. **Given** a new app user is created through Google, **Then** password login remains disabled unless explicitly enabled through a future password-setting flow.
6. **Given** Google returns a verified email that already exists in the app, **Then** the Google identity is linked to the existing account.
7. **Given** Google returns an unverified email, **Then** login is rejected with a clear explanation.
8. **Given** Google OAuth2 fails or is cancelled, **Then** the user returns to the auth page with a safe localized error message.
9. **Given** OAuth2 login succeeds for a regular user, **Then** the user is redirected to home.
10. **Given** OAuth2 login succeeds for an admin user, **Then** the user is redirected to admin area.
11. **Given** OAuth2 login succeeds, **Then** auth status endpoint reflects authenticated user state.
12. **Given** the same Google identity is used again, **Then** it logs in the same linked user and does not create duplicates.

---

### User Story 10 — Persist Login With Remember-Me (Priority: P2)

As a user, I want the Remember me option to keep me signed in across browser restarts so that I do not need to log in every session on trusted devices.

**Why this priority**: The existing UI already has remember-me behavior, and Spring Security migration should preserve it properly.

**Independent Test**: Log in with remember-me checked, restart the browser/session, and verify persistent login works until logout or token expiry.

**Acceptance Scenarios**:

1. **Given** a verified user logs in with Remember me enabled, **Then** persistent remember-me state is created.
2. **Given** the session expires but remember-me is valid, **Then** the user can be re-authenticated according to Spring Security remember-me behavior.
3. **Given** the user logs out, **Then** remember-me token/cookie is cleared.
4. **Given** remember-me token is invalid or expired, **Then** the user is treated as unauthenticated.
5. **Given** remember-me is disabled during login, **Then** no persistent remember-me token is created.
6. **Given** a user's account is blocked, deleted, or otherwise not allowed to log in, **Then** remember-me must not bypass account status checks.

---

### User Story 11 — Protect Public Auth Forms From Abuse (Priority: P1)

As the app owner, I want public auth forms protected against bot abuse so that registration and email endpoints are safer before deployment.

**Why this priority**: Registration, password reset, and resend endpoints are public and can be abused for spam, enumeration, or resource exhaustion.

**Independent Test**: Submit public forms with missing captcha, invalid captcha, valid captcha, repeated requests, and suspicious login attempts.

**Acceptance Scenarios**:

1. **Given** registration is submitted without captcha, **Then** it is rejected.
2. **Given** forgot password is submitted without captcha, **Then** it is rejected.
3. **Given** resend verification is submitted without captcha, **Then** it is rejected.
4. **Given** captcha token is invalid, **Then** the response tells the user to complete the security check.
5. **Given** captcha is valid, **Then** the request may continue to normal validation.
6. **Given** login fails repeatedly, **Then** captcha becomes required after the configured threshold.
7. **Given** captcha is required for login and missing, **Then** login is rejected with `CAPTCHA_REQUIRED`.
8. **Given** public auth endpoints are hit too frequently, **Then** rate limiting rejects excess requests safely.
9. **Given** dev mode is active, **Then** the dev captcha token can be used for local testing.
10. **Given** production mode is active, **Then** real Turnstile verification is required and dev bypass is not accepted.

---

### User Story 12 — Preserve Account Lock Protection (Priority: P1)

As the app owner, I want failed login protection to continue working after Spring Security migration so that brute-force attempts are slowed down.

**Why this priority**: The migration must not remove existing security protections.

**Independent Test**: Attempt wrong password multiple times and verify captcha requirement, lockout, and reset on successful login.

**Acceptance Scenarios**:

1. **Given** a user enters a wrong password, **Then** failed login counter is updated.
2. **Given** a user reaches 3 failed attempts, **Then** captcha becomes required for further login attempts.
3. **Given** a user reaches 5 failed attempts, **Then** the account is locked for 15 minutes.
4. **Given** an account is locked, **Then** login is rejected even if password is correct until lock expires.
5. **Given** login succeeds, **Then** failed login counter and lock state are reset.
6. **Given** account is soft-deleted or blocked, **Then** login is rejected.
7. **Given** failed login protection rejects a request, **Then** no sensitive internal details are exposed.

---

### User Story 13 — Admin Views Email Verification Status in Account Tab (Priority: P2)

As an admin, I want to see whether a user's account email is verified on the Account tab so that I can understand authentication state during support or moderation.

**Why this priority**: Email verification is now part of account state, but the Admin Users table should not be expanded in this feature.

**Independent Test**: Open Admin User Details → Account tab for verified and unverified users and verify status display.

**Acceptance Scenarios**:

1. **Given** admin opens a user's Account tab, **Then** account email verification status is visible.
2. **Given** the user is verified, **Then** the status is shown as verified.
3. **Given** the user is unverified, **Then** the status is shown as unverified.
4. **Given** admin opens the Admin Users table, **Then** no new email verification column is added in this feature.
5. **Given** non-admin user attempts to view account verification status through admin API, **Then** access is rejected by backend authorization.

---

### User Story 14 — Polish Auth and Landing i18n Copy (Priority: P2)

As a user, I want auth and landing text to sound natural in English and Russian so that the app feels production-ready and trustworthy.

**Why this priority**: Authentication flows involve trust. Awkward, inconsistent, or untranslated text reduces confidence.

**Independent Test**: Switch language, review auth pages, error states, email verification states, reset password states, OAuth states, captcha messages, and landing copy.

**Acceptance Scenarios**:

1. **Given** auth pages are displayed in English, **Then** all visible text is natural and free of untranslated keys.
2. **Given** auth pages are displayed in Russian, **Then** all visible text is natural and free of untranslated keys.
3. **Given** landing page is displayed, **Then** obvious grammar, typo, and tone issues are corrected.
4. **Given** new auth errors occur, **Then** localized user-facing messages are shown.
5. **Given** email templates are generated, **Then** they include English text followed by Russian text.
6. **Given** OAuth email is unverified, **Then** the user sees a clear production-grade explanation.
7. **Given** captcha is required, **Then** the message is understandable and not overly technical.
8. **Given** password reset succeeds or fails, **Then** user-facing copy clearly explains the next step.

---

### User Story 15 — Harden Production Configuration and Landing/Public Edge (Priority: P1)

As the app owner, I want production auth configuration and public entry points to fail safely so that deployment does not accidentally run with insecure defaults.

**Why this priority**: This feature prepares the project for production deployment. Silent insecure misconfiguration is dangerous.

**Independent Test**: Run the app in dev and prod-like configuration with missing/present secrets and verify behavior.

**Acceptance Scenarios**:

1. **Given** production profile is active and required auth secrets are missing, **Then** the app must fail startup or expose a clear configuration error.
2. **Given** development profile is active and Resend API key is missing, **Then** email links may be logged for local testing.
3. **Given** production profile is active and Resend API key is missing, **Then** email delivery must not silently no-op.
4. **Given** production profile is active and Turnstile keys are missing, **Then** captcha-protected flows must not silently accept all requests.
5. **Given** landing page receives normal traffic, **Then** it remains publicly accessible.
6. **Given** landing page or public endpoints receive abusive traffic, **Then** proxy/app rate limiting should protect resources within the documented MVP limits.
7. **Given** large or malformed public requests are submitted, **Then** request/body limits prevent avoidable resource exhaustion.
8. **Given** HTTPS deployment is used, **Then** cookies are configured for secure production behavior.
9. **Given** frontend is built for production, **Then** no backend secrets, Resend secrets, Google client secrets, or Turnstile secret keys are included in frontend assets.

---


## API Contract Draft

> This API contract is intentionally included in the specification because `feat/011-auth-hardening` is a high-risk security migration. DeepSeek/OpenCode must not invent alternative endpoint names, request shapes, redirect targets, or auth error codes without a STOP report and explicit user approval.

### Contract Rules

1. Endpoints remain SPA-friendly and JSON-based unless explicitly marked as redirect-based.
2. Frontend auth state continues to rely on `/api/auth/status`.
3. Backend Spring Security `Authentication` is the source of truth.
4. `UserSession`-like DTO may remain only as response contract compatibility.
5. Tokens sent by email are raw only in the link; database stores only hashed tokens.
6. Auth responses must not expose password hashes, token hashes, provider secrets, stack traces, SQL errors, or internal class names.
7. Frontend must use CSRF cookie `XSRF-TOKEN` and header `X-XSRF-TOKEN`.

---

### Auth Status

```http
GET /api/auth/status
```

Authenticated response:

```json
{
  "authenticated": true,
  "email": "user@example.com",
  "role": "USER"
}
```

Unauthenticated response:

```json
{
  "authenticated": false,
  "email": "",
  "role": ""
}
```

Rules:

- Must not throw server error for anonymous users.
- Must remain compatible with the current frontend `useAuth` flow.
- Must derive current state from Spring Security, not legacy session attributes.

---

### Register

```http
POST /api/auth/register
```

Request:

```json
{
  "email": "user@example.com",
  "username": "anton",
  "password": "StrongPass1",
  "passwordConfirmation": "StrongPass1",
  "captchaToken": "turnstile-token-or-dev-captcha-pass"
}
```

Success response:

```json
{
  "success": true,
  "code": "REGISTRATION_PENDING_EMAIL_VERIFICATION",
  "message": "Please check your email to verify your account.",
  "redirectUrl": "/auth/check-email"
}
```

Rules:

- Must require captcha.
- Must create unverified user.
- Must not auto-login.
- Must create a 24-hour email verification token.
- Must store only token hash.
- Must send verification email through Resend or dev email logging.

---

### Verify Email

```http
GET /api/auth/verify-email?token=...
```

Success behavior:

```http
302 Location: /app/auth/verified?status=success
```

Expired token behavior:

```http
302 Location: /app/auth/verified?status=expired
```

Invalid or consumed token behavior:

```http
302 Location: /app/auth/verified?status=invalid
```

Rules:

- Token is one-time use.
- Successful verification marks `email_verified = true`.
- Verification consumes the token.
- Verification must not create an authenticated session.

---

### Resend Email Verification

```http
POST /api/auth/email-verification/resend
```

Request:

```json
{
  "email": "user@example.com",
  "captchaToken": "turnstile-token-or-dev-captcha-pass"
}
```

Success/safe response:

```json
{
  "success": true,
  "message": "If this account needs verification, a new verification email has been sent."
}
```

Rules:

- Must require captcha.
- Must enforce 60-second cooldown.
- Must enforce max 5 emails/hour per email/IP.
- Must enforce max 20 emails/day per email/IP.
- Must not expose whether unknown or already verified email exists.
- New token should invalidate/supersede older active email verification tokens.

---

### Login

```http
POST /api/auth/login
```

Request:

```json
{
  "email": "user@example.com",
  "password": "StrongPass1",
  "rememberMe": true,
  "captchaToken": "optional-until-required-after-failed-attempts"
}
```

User success response:

```json
{
  "success": true,
  "role": "USER",
  "message": "Login successful.",
  "redirectUrl": "/home"
}
```

Admin success response:

```json
{
  "success": true,
  "role": "ADMIN",
  "message": "Login successful.",
  "redirectUrl": "/admin"
}
```

Failure response examples:

```json
{
  "success": false,
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid email or password."
}
```

```json
{
  "success": false,
  "code": "EMAIL_NOT_VERIFIED",
  "message": "Please verify your email before signing in."
}
```

```json
{
  "success": false,
  "code": "CAPTCHA_REQUIRED",
  "message": "Please complete the security check."
}
```

```json
{
  "success": false,
  "code": "ACCOUNT_LOCKED",
  "message": "Too many failed attempts. Please try again later."
}
```

Rules:

- Login identifier is email only.
- Username must not be accepted as login identifier.
- Unverified accounts must not log in.
- Failed attempts must be counted.
- Captcha must become required after 3 failed attempts.
- Account must lock for 15 minutes after 5 failed attempts.
- Remember-me must use Spring Security persistent remember-me.

---

### Logout

```http
POST /api/auth/logout
```

Success response:

```json
{
  "success": true,
  "message": "Logged out successfully."
}
```

Rules:

- Must invalidate session.
- Must clear Spring Security context.
- Must clear remember-me cookie/token.
- Must return JSON for SPA handling.

---

### Password Reset Request

```http
POST /api/auth/password-reset/request
```

Request:

```json
{
  "email": "user@example.com",
  "captchaToken": "turnstile-token-or-dev-captcha-pass"
}
```

Generic response:

```json
{
  "success": true,
  "message": "If this email exists, a password reset link has been sent."
}
```

Rules:

- Must require captcha.
- Must return generic response for known and unknown emails.
- Must create a 15-minute token only for existing eligible users.
- Must store only token hash.
- Must send reset email through Resend or dev email logging.
- Must enforce per-IP and per-email rate limits.

---

### Password Reset Token Validation

```http
GET /api/auth/password-reset/validate?token=...
```

Valid token response:

```json
{
  "valid": true
}
```

Expired token response:

```json
{
  "valid": false,
  "code": "TOKEN_EXPIRED",
  "message": "This reset link has expired."
}
```

Invalid or consumed token response:

```json
{
  "valid": false,
  "code": "TOKEN_INVALID",
  "message": "This reset link is invalid or has already been used."
}
```

---

### Password Reset Confirmation

```http
POST /api/auth/password-reset/confirm
```

Request:

```json
{
  "token": "raw-token-from-link",
  "newPassword": "StrongPass1",
  "passwordConfirmation": "StrongPass1"
}
```

Success response:

```json
{
  "success": true,
  "message": "Password updated successfully.",
  "redirectUrl": "/auth"
}
```

Rules:

- Must validate token.
- Must enforce existing password strength rules.
- Must consume token after success.
- Must invalidate other active reset tokens for the same user.
- Must reset failed login counters if applicable.

---

### Google OAuth2 Start

```http
GET /oauth2/authorization/google
```

Rules:

- Frontend "Continue with Google" button links to this backend endpoint.
- Frontend must not build the Google OAuth URL manually.
- Spring Security manages the OAuth2 authorization flow.

---

### Google OAuth2 Callback

Handled by Spring Security OAuth2 Login configuration.

Success redirect:

```http
302 Location: /app/home
```

or for admin:

```http
302 Location: /app/admin
```

Failure redirect examples:

```http
302 Location: /app/auth?oauthError=OAUTH_EMAIL_NOT_VERIFIED
```

```http
302 Location: /app/auth?oauthError=OAUTH_LOGIN_FAILED
```

Rules:

- Verified Google email matching existing app user must link to existing user.
- Verified Google email not found in app must create new user.
- Unverified Google email must be rejected.
- OAuth-only users get technical random password hash and `password_login_enabled = false`.
- Do not store Google access/refresh tokens in this feature.
- Do not populate `contact_detail` from Google profile name in this feature.

---

## Auth Error Codes

> These codes are part of the frontend/backend contract. Do not rename, duplicate, or invent alternatives without STOP approval.

### Stable Codes

- `INVALID_CREDENTIALS`
- `EMAIL_NOT_VERIFIED`
- `ACCOUNT_LOCKED`
- `CAPTCHA_REQUIRED`
- `CAPTCHA_INVALID`
- `TOKEN_INVALID`
- `TOKEN_EXPIRED`
- `OAUTH_EMAIL_NOT_VERIFIED`
- `OAUTH_LOGIN_FAILED`
- `RATE_LIMITED`
- `REGISTRATION_PENDING_EMAIL_VERIFICATION`

### Rules

1. Codes are for frontend behavior, tests, and localized message selection.
2. Messages must be localized in English and Russian.
3. Backend must not expose raw exception messages as user-facing auth messages.
4. Frontend must not branch on free-form text when a stable code is available.
5. If a new code seems necessary, STOP and ask before adding it.


## Edge Cases

1. User registers and immediately tries to log in before email verification.
2. User clicks verification link twice.
3. User clicks expired verification link.
4. User requests multiple verification emails.
5. User requests password reset for unknown email.
6. User opens password reset link after token expiry.
7. User submits reset form after token was already consumed.
8. User enters weak password during reset.
9. User has 3 failed logins and now captcha is required.
10. User has 5 failed logins and account is locked.
11. User with valid remember-me token becomes blocked.
12. User logs out while remember-me exists.
13. Google returns verified email for existing account.
14. Google returns verified email for new account.
15. Google returns unverified email.
16. Google OAuth2 callback fails or is cancelled.
17. Resend API key is missing in development.
18. Resend API key is missing in production.
19. Turnstile secret is missing in production.
20. Dev captcha bypass is attempted in production.
21. CSRF token is missing for unsafe authenticated request.
22. Legacy auth session attribute exists but Spring Security authentication is absent.
23. Admin endpoint is called by authenticated non-admin.
24. Existing test user migration runs more than once.
25. Existing test user with soft-delete flag remains deleted after migration.
26. Landing receives abusive GET traffic.
27. Public auth endpoint receives large malformed payload.
28. Frontend language is switched and auth errors must remain localized.

---

## Requirements

### Spring Security Migration

- **FR-001**: The system MUST migrate authentication and authorization source of truth from custom `HttpSession` user attributes to Spring Security.
- **FR-002**: The system MUST keep SPA-friendly JSON auth endpoints for login, logout, status, registration, password reset, and resend verification.
- **FR-003**: The system MUST NOT use Spring Boot starters or Spring Boot auto-configuration.
- **FR-004**: The system MUST NOT add Spring Data JPA, Hibernate, Lombok, JWT auth, Keycloak SDK, Auth0 SDK, or unrelated frameworks.
- **FR-005**: The system MUST keep plain JDBC and existing DAO-style persistence.
- **FR-006**: The system MUST keep the existing frontend auth contract as stable as practical.
- **FR-007**: The system MUST preserve role-based redirects for USER and ADMIN.
- **FR-008**: The system MUST protect `/api/admin/**` with backend ADMIN authorization.
- **FR-009**: The system MUST not rely on frontend route guards for security.
- **FR-010**: The system MUST reject unauthenticated access to protected APIs.
- **FR-011**: The system MUST keep `/api/auth/status` compatible with the existing frontend auth state flow.
- **FR-012**: `UserSession`-like response data MAY remain as a DTO, but it MUST NOT be treated as authentication source of truth.
- **FR-013**: Legacy custom auth classes that remain during migration MUST be marked as temporary migration compatibility.
- **FR-014**: Legacy auth compatibility code MUST be removed after Spring Security behavior is verified.
- **FR-015**: The final implementation MUST NOT leave two independent permanent authentication systems active.

### Dependencies and Framework Boundaries

- **FR-016**: The implementation MAY add only the minimal Spring Security dependencies required for web security, config, core, crypto, OAuth2 client, and OAuth2/OIDC support.
- **FR-017**: Any additional dependency outside the approved Spring Security set MUST trigger STOP and require explicit user approval.
- **FR-018**: The implementation MUST register Spring Security correctly for the existing non-Boot servlet application.
- **FR-019**: The implementation MUST NOT create Spring Boot application classes, Boot configuration files, or Boot-specific assumptions.
- **FR-020**: The implementation MUST remain compatible with the current Maven/Flyway build style.

### Existing Users Migration

- **FR-021**: Existing users MUST be marked as email-verified during migration.
- **FR-022**: Existing users' passwords MUST be migrated to an encoded value for `Aa123456`.
- **FR-023**: Existing user roles, statuses, permissions, privileged flags, soft-delete flags, created dates, and ownership relationships MUST be preserved.
- **FR-024**: The migration MUST clearly document that resetting all existing passwords is acceptable only for capstone/test data.
- **FR-025**: The migration MUST NOT delete existing users, resumes, profile data, generated files, or admin data.
- **FR-026**: The migration MUST NOT create a dev bootstrap admin.

### Login and Logout

- **FR-027**: Login MUST use email as the only login identifier.
- **FR-028**: Username MUST NOT be accepted as a login identifier in this feature.
- **FR-029**: Login MUST require password unless the flow is OAuth2.
- **FR-030**: Login MUST reject unverified email accounts with a specific user-facing error.
- **FR-031**: Login MUST reject blocked, deleted, or locked accounts.
- **FR-032**: Login MUST reset failed-attempt counters after successful password login.
- **FR-033**: Login MUST return safe JSON responses and no internal exception details.
- **FR-034**: Logout MUST invalidate the current session.
- **FR-035**: Logout MUST clear remember-me state.
- **FR-036**: Logout MUST clear the Spring Security context.
- **FR-037**: Logout MUST return JSON success for SPA handling.

### Failed Login and Account Lock

- **FR-038**: Failed password login attempts MUST be counted.
- **FR-039**: Captcha MUST become required after 3 failed login attempts.
- **FR-040**: Account MUST lock for 15 minutes after 5 failed login attempts.
- **FR-041**: Locked account login MUST be rejected even with correct credentials until lock expires.
- **FR-042**: Successful login MUST reset failed counters and lock state.
- **FR-043**: Failed login and lock responses MUST not expose sensitive internal details.

### Remember-Me

- **FR-044**: The system MUST implement persistent remember-me through Spring Security.
- **FR-045**: Remember-me MUST be optional and controlled by the login request.
- **FR-046**: Remember-me MUST use persistent server-side token storage.
- **FR-047**: Remember-me MUST NOT bypass account status, deletion, lock, or role checks.
- **FR-048**: Logout MUST invalidate current remember-me state.
- **FR-049**: Expired or invalid remember-me tokens MUST be treated as unauthenticated.

### CSRF

- **FR-050**: The system MUST migrate CSRF protection to Spring Security.
- **FR-051**: CSRF cookie name MUST be `XSRF-TOKEN`.
- **FR-052**: Frontend CSRF header MUST be `X-XSRF-TOKEN`.
- **FR-053**: The legacy custom CSRF filter MUST be marked deprecated during migration.
- **FR-054**: The legacy custom CSRF filter MUST be removed after Spring Security CSRF is verified.
- **FR-055**: Unsafe authenticated methods MUST require valid CSRF protection unless explicitly and safely excluded.
- **FR-056**: Public auth endpoints MUST still be protected by tokens, captcha, rate limits, or other flow-specific controls where CSRF is not the main control.

### Registration and Email Verification

- **FR-057**: Registration MUST require captcha.
- **FR-058**: Registration MUST create unverified users.
- **FR-059**: Registration MUST NOT automatically log in the new user.
- **FR-060**: Registration success MUST direct the user to `/app/auth/check-email`.
- **FR-061**: Registration MUST generate an email verification token.
- **FR-062**: Email verification token TTL MUST be 24 hours.
- **FR-063**: Email verification raw token MUST never be stored in the database.
- **FR-064**: Email verification stored token MUST be hashed.
- **FR-065**: Email verification token MUST be one-time use.
- **FR-066**: Successful email verification MUST mark account email as verified.
- **FR-067**: Successful email verification MUST consume the token.
- **FR-068**: Verification endpoint MUST redirect to frontend verification result page.
- **FR-069**: Invalid, expired, or consumed verification tokens MUST fail safely.
- **FR-070**: Duplicate registration MUST not create duplicate accounts.
- **FR-071**: Registration MUST preserve existing password strength validation.

### Resend Verification

- **FR-072**: Resend verification MUST require captcha.
- **FR-073**: Resend verification MUST enforce a 60-second cooldown.
- **FR-074**: Resend verification MUST enforce a maximum of 5 emails per hour per email/IP.
- **FR-075**: Resend verification MUST enforce a maximum of 20 emails per day per email/IP.
- **FR-076**: Resend verification MUST not expose raw tokens.
- **FR-077**: Resend verification MUST handle unknown, already verified, or deleted accounts safely.
- **FR-078**: Creating a new verification token SHOULD invalidate or make older active verification tokens unusable.

### Password Reset

- **FR-079**: Forgot-password request MUST require captcha.
- **FR-080**: Forgot-password request MUST return generic response regardless of account existence.
- **FR-081**: Password reset token TTL MUST be 15 minutes.
- **FR-082**: Password reset raw token MUST never be stored in the database.
- **FR-083**: Password reset stored token MUST be hashed.
- **FR-084**: Password reset token MUST be one-time use.
- **FR-085**: Reset token validation endpoint MUST return safe valid/invalid/expired state.
- **FR-086**: Password reset confirmation MUST reject invalid, expired, or consumed tokens.
- **FR-087**: Password reset confirmation MUST preserve existing password strength validation.
- **FR-088**: Successful password reset MUST consume the token.
- **FR-089**: Successful password reset MUST invalidate other active password reset tokens for the same user.
- **FR-090**: Successful password reset MUST allow login with the new password.
- **FR-091**: Forgot-password rate limit MUST enforce 5 requests per 15 minutes per IP.
- **FR-092**: Forgot-password rate limit MUST enforce 3 requests per 15 minutes per email.

### Google OAuth2

- **FR-093**: The app MUST provide a Continue with Google action.
- **FR-094**: Google OAuth2 authorization flow MUST start from backend endpoint `/oauth2/authorization/google`.
- **FR-095**: Only verified Google emails MUST be accepted.
- **FR-096**: Unverified Google emails MUST be rejected with a clear user-facing explanation.
- **FR-097**: A verified Google email matching an existing user MUST link to the existing user.
- **FR-098**: A verified Google email not matching an existing user MUST create a new user.
- **FR-099**: New OAuth2-created users MUST be marked email-verified.
- **FR-100**: New OAuth2-created users MUST receive a technical random password hash.
- **FR-101**: New OAuth2-created users MUST have password login disabled unless explicitly enabled by a future password-setting flow.
- **FR-102**: OAuth provider identity MUST be stored separately from the `users` table.
- **FR-103**: Duplicate provider identity MUST NOT create duplicate app accounts.
- **FR-104**: OAuth2 success MUST redirect users by role.
- **FR-105**: OAuth2 failure MUST return the user to a safe frontend error state.
- **FR-106**: OAuth2 MUST NOT populate contact/profile details from Google name in this feature.
- **FR-107**: The frontend MUST hide the "Continue with Google" button when Google OAuth2 client configuration (client ID/secret) is absent.

### Captcha and Abuse Protection

- **FR-108**: Cloudflare Turnstile MUST protect registration.
- **FR-109**: Cloudflare Turnstile MUST protect forgot-password request.
- **FR-110**: Cloudflare Turnstile MUST protect resend verification.
- **FR-111**: Captcha MUST be required for login after suspicious/failed attempts.
- **FR-112**: Captcha failure MUST show a clear user-facing message.
- **FR-113**: Dev mode MUST support controlled captcha testing without real Turnstile.
- **FR-114**: Production mode MUST NOT accept dev captcha bypass.
- **FR-115**: Production mode MUST fail safely or clearly report misconfiguration if captcha secrets are missing.
- **FR-116**: Public auth rate limits MUST be enforced server-side.

### Email Delivery

- **FR-117**: Email delivery MUST use Resend.
- **FR-118**: Production mode MUST require Resend API key.
- **FR-119**: Production mode MUST NOT silently no-op email delivery.
- **FR-120**: Development mode MAY log email links if Resend API key is absent.
- **FR-121**: Email templates MUST include plain text and basic HTML.
- **FR-122**: Email templates MUST be bilingual, with English first and Russian second.
- **FR-123**: Verification and reset links MUST use configured public base URLs.
- **FR-124**: Backend verification links MUST use backend public base URL.
- **FR-125**: Password reset links MUST use frontend public base URL.

### Admin Account Visibility

- **FR-126**: Admin User Details → Account tab MUST show email verification status.
- **FR-127**: Admin Users table MUST NOT add an email verification column in this feature.
- **FR-128**: Only admins MUST be able to view admin account verification status.
- **FR-129**: Admin DTOs MUST NOT expose password hashes, OAuth provider secrets, tokens, token hashes, API keys, or internal security details.

### i18n and Copy

- **FR-130**: All new frontend auth text MUST be localized in English and Russian.
- **FR-131**: All new auth error messages MUST be localized in English and Russian.
- **FR-132**: Auth copy MUST be natural, user-friendly, and free of raw i18n keys.
- **FR-133**: Landing page copy MUST be proofread for obvious grammar, typos, and unnatural wording.
- **FR-134**: OAuth unverified-email rejection MUST have a clear production-grade explanation.
- **FR-135**: Captcha-required messages MUST be understandable and not overly technical.

### Production and Public Surface Hardening

- **FR-136**: Captcha MUST NOT be documented as DDoS protection for GET landing.
- **FR-137**: Landing protection MUST be documented as proxy/Nginx rate limiting, static caching, and request/body limits.
- **FR-138**: Production cookies MUST be configured safely for HTTPS deployment.
- **FR-139**: Secrets MUST be provided through environment/configuration and MUST NOT be included in frontend assets.
- **FR-140**: Missing production secrets MUST fail safely or produce clear configuration errors.
- **FR-141**: Public endpoint abuse protection MUST be covered by tests or manual evidence.

### Auth Event Logging

- **FR-142**: The system MUST log failed login attempts (email, IP, timestamp, reason code).
- **FR-143**: The system MUST log successful logins (email, IP, timestamp).
- **FR-144**: The system MUST log email verification completions (email, timestamp).
- **FR-145**: The system MUST log password reset completions (email, timestamp).
- **FR-146**: Auth logging MUST never include passwords, raw tokens, token hashes, password hashes, API keys, secrets, or internal stack traces.
- **FR-147**: Auth logging MUST NOT expose PII beyond email and IP for operational debugging.

### Non-Regression

- **FR-148**: The feature MUST NOT modify PDF rendering, PDF fitting, PDF validation, PDF templates, AI generation, prompt building, parser, OpenRouter client, finalization pipeline, or budget config.
- **FR-149**: The feature MUST NOT hard-delete users, resumes, profile data, or generated files.
- **FR-150**: Existing User Home, Profile, Generate Resume, Public Resume, and Admin flows MUST continue to work after auth migration.
- **FR-151**: Existing tests for unrelated domains MUST not be weakened or removed to hide regressions.
- **FR-152**: New/changed security code MUST be covered by positive, negative, boundary, and abuse-case tests.

---

## Key Entities

### User

Represents an app account.

Relevant auth fields after this feature:

* `id`
* `email`
* `username`
* `password_hash`
* `password_login_enabled`
* `email_verified`
* `email_verified_at`
* `role_id`
* `status_id`
* `permission_id`
* `is_privileged`
* `failed_login_attempts`
* `locked_until`
* `is_deleted`
* `deleted_at`
* `created_at`
* `updated_at`

Notes:

* Existing users are migrated to `email_verified = true`.
* Login identifier is email only.
* `username` is not used for login in this feature.
* OAuth-only users store a technical random password hash and have password login disabled.

### AuthToken

Represents one-time email verification or password reset token.

Fields:

* `id`
* `user_id`
* `token_type`
* `token_hash`
* `expires_at`
* `consumed_at`
* `created_at`

Rules:

* Raw token is shown only in the email link.
* Only hashed token is stored.
* Tokens are one-time use.
* Token types are explicit: `EMAIL_VERIFICATION`, `PASSWORD_RESET`.

### OAuthAccount

Represents an external OAuth2 identity linked to an app user.

Fields:

* `id`
* `user_id`
* `provider`
* `provider_subject`
* `provider_email`
* `provider_email_verified`
* `created_at`
* `updated_at`

Rules:

* `provider + provider_subject` must be unique.
* Google is the only provider in this feature.
* Provider email must be verified.
* Existing app users are linked by verified email.

### PersistentLogin

Represents Spring Security persistent remember-me state.

Fields follow the standard persistent remember-me model:

* `username`
* `series`
* `token`
* `last_used`

Rules:

* Remember-me must be invalidated on logout.
* Remember-me must not bypass account status checks.

### CaptchaChallenge

Represents server-side verification of a Cloudflare Turnstile token.

Fields are not necessarily persisted.

Relevant inputs:

* captcha token
* remote IP or request context
* action/form context
* dev/prod configuration

Rules:

* Dev bypass must be controlled by configuration.
* Prod must require real verification.

### EmailMessage

Represents an outgoing verification or reset email.

Fields:

* recipient email
* subject
* plain text body
* basic HTML body
* template type
* action link

Rules:

* Template is bilingual: English first, Russian second.
* Production sends through Resend.
* Development may log links if configured.

---

## Out of Scope

1. Spring Boot migration.
2. JWT authentication.
3. Stateless token auth.
4. Spring Data JPA or Hibernate.
5. Lombok.
6. Keycloak, Auth0, Clerk, Firebase Auth, or external identity platform SDKs.
7. Multi-provider OAuth beyond Google.
8. GitHub, LinkedIn, Microsoft, Facebook, or Apple login.
9. Account settings page for linking/unlinking providers.
10. User-driven password setup for OAuth-only accounts.
11. Forced password change for migrated test users.
12. Dev bootstrap admin creation.
13. Admin Users table email verification column.
14. Audit log.
15. Email queue system.
16. Redis-based distributed rate limiting.
17. Full DDoS protection.
18. CAPTCHA on GET landing page.
19. Profile editing changes.
20. Resume generation changes.
21. PDF generation or fitting changes.
22. AI prompt/parser/OpenRouter/finalization changes.
23. Public resume behavior changes unrelated to auth.
24. Admin moderation redesign.
25. Any hard-delete of existing user/resume/profile data.

---

## Success Criteria

### Measurable Outcomes

1. Existing migrated test users can log in with email and `Aa123456`.
2. New password registration does not auto-login and requires email verification.
3. Verified email user can log in.
4. Unverified email user cannot log in and sees clear message.
5. Password reset works with 15-minute one-time token.
6. Expired/invalid/consumed reset tokens are rejected.
7. Google OAuth2 login creates a new user for verified new Google email.
8. Google OAuth2 login links to existing user when verified email matches.
9. Google OAuth2 login rejects unverified Google email.
10. Remember-me persists login and is cleared on logout.
11. `/api/admin/**` rejects non-admin users on backend.
12. CSRF uses Spring Security and frontend sends `X-XSRF-TOKEN`.
13. Login with wrong password 3 times requires captcha.
14. Login with wrong password 5 times locks account for 15 minutes.
15. Registration with duplicate email is rejected.
16. Resend verification cooldown and hourly/daily limits are enforced.
17. Registration requires valid captcha.
18. Forgot-password requires valid captcha.
19. Forgot-password returns generic response for unknown email.
20. Auth page works in English and Russian with natural copy.
21. Admin Account tab shows email verification status.
22. Missing production secrets cause safe failure, not silent no-op.
23. Landing page is publicly accessible.
24. No PDF, AI, generation, or finalization behavior changes.

---

## Constitution Alignment

This feature MUST comply with the ResumAIner Constitution principles:

| Principle | Impact on this feature |
|---|---|
| **I. Code Quality & Maintainability** | All Java code follows layered architecture (controller/service/dao/model/config/util). No Spring Boot, JPA, or Hibernate. Maven CLI build must succeed. |
| **II. Testing Excellence** | JUnit 5 + Mockito tests required. TDD for business logic. Mock AI provider used — no real API calls in tests. JaCoCo coverage tracked. |
| **III. User Experience Consistency** | Frontend i18n uses `src/i18n/en.json` and `src/i18n/ru.json`; backend/landing/email text uses the existing backend message/template mechanism. Dual validation (frontend + backend). SPA JSON auth preserved. No stack traces exposed. |
| **IV. Performance & Reliability** | PreparedStatement for all SQL queries. JDBC transaction management (commit/rollback). SQL-level pagination. UTF-8 encoding throughout. |
| **V. Security by Design** | Backend validation is authoritative. No secrets in logs or builds. Hashed tokens. Captcha. Rate limits. OAuth email verification required. |

**Technology Constraint Check** (per Constitution Technology Stack):
- [X] Java 21, Spring MVC (no Spring Boot), Plain JDBC (no ORM)
- [X] PostgreSQL with Flyway migrations
- [X] Docker Compose for deployment
- [X] Dev + Prod Spring profiles

---

## Assumptions

- The feature must coexist with legacy custom auth code temporarily during migration phases.
- Spring Security must be explicitly registered for the existing non-Boot servlet application — no auto-configuration.
- Existing dev/test database users are capstone/test data and may have passwords reset to `Aa123456`.
- The frontend remains Vue SPA and continues to consume JSON auth endpoints.
- Captcha protects form submissions only; GET landing protection is handled by proxy/Nginx.
- Google OAuth2 requires a Google Cloud Console project with a configured OAuth 2.0 Client ID.
- Resend requires an API key for production email delivery.
- Dev environment may operate without real API keys by logging email links and accepting dev captcha tokens.
- No audit log, no email queue, no distributed rate limiting, no multi-provider OAuth.
- Existing browser sessions will be invalidated by the migration.
