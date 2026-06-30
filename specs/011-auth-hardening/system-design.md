# System Design: Auth Hardening and Spring Security Migration

**Feature**: Full authentication hardening before production deployment
**Generated**: 2026-06-30
**Scope**: New infrastructure integrations (OAuth2, email, captcha) + Spring Security for existing deployment

---

## Overview

The system is a browser-based Vue SPA communicating with a Java backend via JSON REST API. This feature adds three new external integrations (Google OAuth2, Cloudflare Turnstile, Resend) and replaces the internal custom session auth with Spring Security. The database adds three new tables for token and identity storage. The deployment remains Docker Compose with three services: Java backend, Vue frontend (served by Nginx), and PostgreSQL.

## System Design Diagram

```mermaid
flowchart LR
    subgraph Client ["Browser"]
        VueApp["Vue 3 SPA<br/>Port 5173 (dev) | Nginx :80 (prod)"]
        LocalStorage["XSRF-TOKEN cookie<br/>Session cookie<br/>Remember-me cookie"]
    end

    subgraph DockerCompose ["Docker Compose"]
        direction TB
        Nginx["Nginx<br/>Frontend container"]
        Backend["Java Backend<br/>Spring MVC 6<br/>Port 8080"]
        Postgres["PostgreSQL<br/>Port 5432"]
    end

    subgraph External ["External Services"]
        GoogleOAuth["Google OAuth2<br/>accounts.google.com"]
        Turnstile["Cloudflare Turnstile<br/>challenges.cloudflare.com"]
        ResendAPI["Resend API<br/>api.resend.com"]
    end

    %% Request flows
    Client -->|"/app/* Vue routes"| Nginx
    Client -->|"/api/*"| Backend
    
    Nginx -->|"/api/* proxy"| Backend
    
    Backend -->|"JDBC"| Postgres
    Backend -->|"REST"| Turnstile
    Backend -->|"HTTP"| ResendAPI
    Backend -->|"OAuth2/OIDC"| GoogleOAuth

    %% Database
    Postgres --> Users[(users<br/>+ auth columns)]
    Postgres --> AuthTokens[(auth_tokens)]
    Postgres --> OAuthAcc[(oauth_accounts)]
    Postgres --> PersLogins[(persistent_logins)]
```

## Infrastructure Decisions

### PostgreSQL for Auth Data

**What**: Existing PostgreSQL instance with 4 new/updated tables: `users` (new columns), `auth_tokens`, `oauth_accounts`, `persistent_logins`.

**Why**: The project already uses PostgreSQL for all application data. Adding auth tables to the same database maintains transaction consistency (e.g., creating a user + verification token in the same transaction) without introducing a second data store. Spring Security's persistent remember-me also expects a relational database.

**Alternatives considered**:
| Option | Why it wasn't chosen |
|--------|---------------------|
| Redis for token storage | Adds operational complexity. Auth tokens are write-once/read-once with TTL — PostgreSQL handles this fine. Redis would be justified at higher scale. |
| Separate auth database | Adds connection pool complexity. No isolation benefit for a single-app deployment. |

**When you'd choose differently**: If the project scaled to multiple backend instances, a distributed token cache (Redis) for session/remember-me would reduce DB load on each auth request.

---

### Cloudflare Turnstile for Captcha

**What**: Server-side POST verification to `https://challenges.cloudflare.com/turnstile/v0/siteverify` with the Turnstile secret key and client token.

**Why**: Turnstile provides invisible captcha (no visual challenge) on a generous free tier suitable for portfolio deployment. Server-side verification ensures the captcha cannot be bypassed by client-side manipulation.

**Alternatives considered**:
| Option | Why it wasn't chosen |
|--------|---------------------|
| Google reCAPTCHA | Privacy concerns, visual challenges degrade UX. Turnstile is simpler to integrate. |
| hCaptcha | Higher latency, lower free tier limits for a portfolio project. |

**When you'd choose differently**: If the project required enterprise compliance with specific captcha providers mandated by policy.

---

### Resend for Email Delivery

**What**: HTTP API to `https://api.resend.com/emails` with sender identity, recipient, subject, and HTML+plain text body.

**Why**: Resend has a free tier (100 emails/day) sufficient for dev/test and low-traffic portfolio deployment. The API is simple REST — no SDK required, no complex SMTP configuration.

**Alternatives considered**:
| Option | Why it wasn't chosen |
|--------|---------------------|
| SMTP (Gmail, SendGrid) | SMTP credentials management is more complex. SendGrid requires dedicated domain setup. Resend's API-only approach is simpler. |
| JavaMailSession | Requires SMTP server configuration. Overkill for a single email provider. |

**When you'd choose differently**: If the project required high-volume transactional email (thousands/day), SendGrid or AWS SES would be more cost-effective at scale.

---

### Google OAuth2 via Spring Security

**What**: Standard OIDC authorization code flow. Spring Security manages the redirect, token exchange, and user info extraction.

**Why**: Spring Security's built-in OAuth2 client handles the protocol complexity. No custom OAuth2 client code needed. The frontend only needs a link to `/oauth2/authorization/google`.

**Alternatives considered**:
| Option | Why it wasn't chosen |
|--------|---------------------|
| Google Sign-In SDK on frontend | Exposes Google client ID to frontend, complex token handling. Backend-managed OAuth2 is more secure. |
| Custom OAuth2 implementation | Spring Security already provides production-grade OAuth2 client. Custom implementation would duplicate tested protocol handling. |

---

## Data Flow

### Registration + Verification (happy path)

```mermaid
sequenceDiagram
    actor User
    participant Vue as Vue SPA
    participant Backend as Java Backend
    participant Turnstile as Turnstile
    participant Resend as Resend
    participant DB as PostgreSQL

    User->>Vue: Fill register form + captcha
    Vue->>Backend: POST /api/auth/register
    Backend->>Turnstile: POST /siteverify (token + secret)
    Turnstile-->>Backend: success
    Backend->>DB: INSERT users (unverified, hashed password)
    Backend->>DB: INSERT auth_tokens (hashed verification token, 24h TTL)
    Backend->>Resend: Send verification email
    Resend-->>Backend: 200 OK
    Backend-->>Vue: 201 REGISTRATION_PENDING_EMAIL_VERIFICATION
    Vue-->>User: Show /app/auth/check-email

    User->>User: Open email, click verification link
    User->>Backend: GET /api/auth/verify-email?token=...
    Backend->>DB: Lookup token hash
    Backend->>DB: UPDATE users SET email_verified=true
    Backend->>DB: UPDATE auth_tokens SET consumed_at=NOW()
    Backend-->>User: 302 /app/auth/verified?status=success
```

### Login with failed attempt lockout

```mermaid
sequenceDiagram
    actor User
    participant Vue as Vue SPA
    participant Backend as Java Backend
    participant DB as PostgreSQL

    User->>Vue: Enter wrong password x3
    Vue->>Backend: POST /api/auth/login
    Backend->>DB: Verify credentials -> fail
    Backend->>DB: UPDATE users SET failed_attempts+=1
    Backend-->>Vue: 401 INVALID_CREDENTIALS

    Note over User,Backend: After 3rd failure...
    Vue->>Backend: POST /api/auth/login (no captcha)
    Backend-->>Vue: 401 CAPTCHA_REQUIRED

    User->>Vue: Enter wrong password x2 more (with captcha)
    Note over User,Backend: After 5th total failure...
    Backend->>DB: UPDATE users SET locked_until=NOW()+15min
    Backend-->>Vue: 401 ACCOUNT_LOCKED

    Note over User,Backend: After lock period...
    User->>Vue: Enter correct email + password + captcha
    Vue->>Backend: POST /api/auth/login
    Backend->>DB: Verify credentials -> success
    Backend->>DB: UPDATE users SET failed_attempts=0, locked_until=NULL
    Backend-->>Vue: 200 Login successful
```

### Google OAuth2 new user

```mermaid
sequenceDiagram
    actor User
    participant Vue as Vue SPA
    participant Backend as Java Backend
    participant Google as Google OAuth2
    participant DB as PostgreSQL

    User->>Vue: Click Continue with Google
    Vue->>Backend: GET /oauth2/authorization/google
    Backend->>Google: Redirect to Google login
    Google-->>User: Google login page
    User->>Google: Sign in with Google
    Google-->>Backend: OAuth2 callback (code)
    Backend->>Google: Exchange code for tokens
    Backend->>Google: Fetch OIDC user info
    Note over Backend: Verify provider email verified=true

    alt New email
        Backend->>DB: INSERT users (verified=true, random password hash, password_login_enabled=false)
        Backend->>DB: INSERT oauth_accounts (provider=GOOGLE)
    else Existing email
        Backend->>DB: Find user by email
        Backend->>DB: INSERT oauth_accounts (link to existing user)
    end

    alt User role
        Backend-->>User: 302 /app/home
    else Admin role
        Backend-->>User: 302 /app/admin
    end
```

## Scaling & Reliability Notes

At the project's current scale (capstone/portfolio deployment), the architecture uses a single backend instance with one PostgreSQL database. The new auth tables add minimal load. Key reliability points:

- **Missing secrets**: Production startup fails safely if Turnstile, Resend, or Google OAuth2 secrets are missing (FR-139).
- **Email delivery failure**: Dev logs links. Production requires working API key (FR-118).
- **Database failure**: Auth becomes unavailable if DB is down — the design does not add a caching layer for a single-instance deployment. This is acceptable for the target scale.
- **Rate limiting**: Server-side rate limits protect public endpoints even without a distributed rate limiter (single instance → in-memory counts are sufficient for MVP).
