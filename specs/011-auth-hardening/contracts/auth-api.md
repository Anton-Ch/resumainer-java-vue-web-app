# Auth API Contract

**Date**: 2026-06-30  
**Source**: `spec.md` API Contract Draft  
**Status**: Frozen (do not change without STOP approval)

## General Rules

1. All auth endpoints are SPA-friendly JSON (unless marked redirect-based).
2. Frontend auth state relies on `GET /api/auth/status`.
3. Spring Security `Authentication` is the source of truth — NOT legacy session attributes.
4. CSRF cookie: `XSRF-TOKEN`. CSRF header: `X-XSRF-TOKEN`.
5. Auth responses never expose: password hashes, token hashes, provider secrets, stack traces, SQL errors, internal class names.
6. Tokens in email links are raw. Database stores only hashed tokens.

---

## GET /api/auth/status

Authenticated:
```json
{
  "authenticated": true,
  "email": "user@example.com",
  "role": "USER"
}
```

Unauthenticated:
```json
{
  "authenticated": false,
  "email": "",
  "role": ""
}
```

---

## POST /api/auth/register

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

Success (201):
```json
{
  "success": true,
  "code": "REGISTRATION_PENDING_EMAIL_VERIFICATION",
  "message": "Please check your email to verify your account.",
  "redirectUrl": "/auth/check-email"
}
```

Rules:
- Captcha required.
- Creates unverified user.
- Does NOT auto-login.
- Creates 24h email verification token (hash stored).
- Sends verification email via Resend or dev logging.

---

## GET /api/auth/verify-email?token=...

Redirect-based (302):

| Condition | Location |
|-----------|----------|
| Success | `/app/auth/verified?status=success` |
| Token expired | `/app/auth/verified?status=expired` |
| Invalid/consumed | `/app/auth/verified?status=invalid` |

Rules:
- One-time use. Token consumed on success.
- Sets `email_verified = true`.
- Does NOT create authenticated session.

---

## POST /api/auth/email-verification/resend

Request:
```json
{
  "email": "user@example.com",
  "captchaToken": "turnstile-token-or-dev-captcha-pass"
}
```

Safe response:
```json
{
  "success": true,
  "message": "If this account needs verification, a new verification email has been sent."
}
```

Rules:
- Captcha required.
- 60-second cooldown.
- Max 5/hour, 20/day per email/IP.
- Safe response for unknown/already-verified accounts.
- New token should supersede older active verification tokens.

---

## POST /api/auth/login

Request:
```json
{
  "email": "user@example.com",
  "password": "StrongPass1",
  "rememberMe": true,
  "captchaToken": "optional-until-required-after-failed-attempts"
}
```

Success (USER):
```json
{
  "success": true,
  "role": "USER",
  "message": "Login successful.",
  "redirectUrl": "/home"
}
```

Success (ADMIN):
```json
{
  "success": true,
  "role": "ADMIN",
  "message": "Login successful.",
  "redirectUrl": "/admin"
}
```

Failure examples:
```json
{ "success": false, "code": "INVALID_CREDENTIALS", "message": "Invalid email or password." }
{ "success": false, "code": "EMAIL_NOT_VERIFIED", "message": "Please verify your email before signing in." }
{ "success": false, "code": "CAPTCHA_REQUIRED", "message": "Please complete the security check." }
{ "success": false, "code": "ACCOUNT_LOCKED", "message": "Too many failed attempts. Please try again later." }
```

Rules:
- Email-only login. Username NOT accepted.
- Reject unverified, blocked, deleted, locked accounts.
- Count failed attempts. Captcha after 3. Lock after 5 (15 min).
- Remember-me uses Spring Security persistent remember-me.

---

## POST /api/auth/logout

Success:
```json
{
  "success": true,
  "message": "Logged out successfully."
}
```

Rules:
- Invalidates session.
- Clears Spring Security context.
- Clears remember-me cookie/token.

---

## POST /api/auth/password-reset/request

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
- Captcha required.
- Generic response for known/unknown emails.
- 15min token for existing eligible users. Hash stored.
- Per-IP and per-email rate limits.

---

## GET /api/auth/password-reset/validate?token=...

Valid:
```json
{ "valid": true }
```

Expired:
```json
{ "valid": false, "code": "TOKEN_EXPIRED", "message": "This reset link has expired." }
```

Invalid/consumed:
```json
{ "valid": false, "code": "TOKEN_INVALID", "message": "This reset link is invalid or has already been used." }
```

---

## POST /api/auth/password-reset/confirm

Request:
```json
{
  "token": "raw-token-from-link",
  "newPassword": "StrongPass1",
  "passwordConfirmation": "StrongPass1"
}
```

Success:
```json
{
  "success": true,
  "message": "Password updated successfully.",
  "redirectUrl": "/auth"
}
```

Rules:
- Validate token.
- Enforce existing password strength rules.
- Consume token after success.
- Invalidate other active reset tokens for same user.
- Reset failed login counters.

---

## GET /oauth2/authorization/google

Backend-managed OAuth2 flow start. Frontend button links here.

---

## Google OAuth2 Callback

Managed by Spring Security OAuth2 Login configuration.

Success redirects:
- USER: `302 /app/home`
- ADMIN: `302 /app/admin`

Failure redirects:
- `302 /app/auth?oauthError=OAUTH_EMAIL_NOT_VERIFIED`
- `302 /app/auth?oauthError=OAUTH_LOGIN_FAILED`

Rules:
- Verified Google email matching existing user → link.
- Verified new Google email → create user.
- Unverified provider email → reject.
- OAuth-only users: random password hash, password login disabled.
- No Google access/refresh tokens stored.
- No contact_detail populated from Google name.

---

## Auth Error Codes

| Code | Description |
|------|-------------|
| `INVALID_CREDENTIALS` | Wrong email or password |
| `EMAIL_NOT_VERIFIED` | Account exists but email not confirmed |
| `ACCOUNT_LOCKED` | Account temporarily locked due to failed attempts |
| `CAPTCHA_REQUIRED` | Captcha needed (e.g., after failed attempts) |
| `CAPTCHA_INVALID` | Captcha token rejected by Turnstile |
| `TOKEN_INVALID` | Verification/reset token is invalid or consumed |
| `TOKEN_EXPIRED` | Verification/reset token has expired |
| `OAUTH_EMAIL_NOT_VERIFIED` | Google returned unverified email |
| `OAUTH_LOGIN_FAILED` | Google OAuth2 flow failed |
| `RATE_LIMITED` | Too many requests |
| `REGISTRATION_PENDING_EMAIL_VERIFICATION` | Registration success — verify email |

Codes are stable. Use for frontend behavior selection and test assertions.
