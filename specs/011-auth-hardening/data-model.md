# Data Model: Auth Hardening and Spring Security Migration

**Date**: 2026-06-30  
**Source**: `spec.md` Key Entities + `plan.md` Database Design

## Entity Relationship Summary

```
┌─────────────────┐       ┌──────────────────────┐
│      User        │ 1──N  │     AuthToken         │
│                  │       │                      │
│ (existing table) │       │ email_verification   │
│ + new columns    │       │ password_reset       │
└────────┬─────────┘       └──────────────────────┘
         │
         │ 1──N        ┌──────────────────────┐
         ├─────────────│    OAuthAccount       │
         │             │                      │
         │             │ provider: GOOGLE     │
         │             └──────────────────────┘
         │
         │ 1──N        ┌──────────────────────┐
         └─────────────│   PersistentLogin     │
                       │                      │
                       │ (Spring Security      │
                       │  standard table)      │
                       └──────────────────────┘
```

## Users (existing table — new columns)

New columns to add:

| Column | Type | Default | Description |
|--------|------|---------|-------------|
| `email_verified` | BOOLEAN | FALSE | Whether the user has confirmed their email |
| `email_verified_at` | TIMESTAMP | NULL | When the email was verified |
| `password_login_enabled` | BOOLEAN | TRUE | Whether password login is allowed (FALSE for OAuth-only users) |
| `updated_at` | TIMESTAMP | NULL | Last update timestamp |

Existing columns relevant to auth:

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Primary key |
| `email` | VARCHAR | Login identifier |
| `username` | VARCHAR | Display name (NOT a login identifier) |
| `password_hash` | VARCHAR | BCrypt hashed password |
| `role_id` | INTEGER | FK to role lookup |
| `status_id` | INTEGER | FK to status lookup |
| `permission_id` | INTEGER | FK to permission lookup |
| `is_privileged` | BOOLEAN | Privileged flag |
| `failed_login_attempts` | INTEGER | Counter for failed logins |
| `locked_until` | TIMESTAMP | Lock expiry (NULL = not locked) |
| `is_deleted` | BOOLEAN | Soft-delete flag |
| `deleted_at` | TIMESTAMP | Soft-delete timestamp |
| `created_at` | TIMESTAMP | Account creation timestamp |

**Migration Rules**:
- Existing test users: set `email_verified = TRUE`, `password_hash = encoded('Aa123456')`, `password_login_enabled = TRUE`
- Preserve: roles, status, permissions, privileged flags, deletion flags, ownership
- Warning: mass password reset is test-data-only, NOT safe for production

## AuthToken (new table)

| Column | Type | Default | Description |
|--------|------|---------|-------------|
| `id` | BIGSERIAL | auto | Primary key |
| `user_id` | UUID | — | FK → users(id) |
| `token_type` | VARCHAR(40) | — | `EMAIL_VERIFICATION` or `PASSWORD_RESET` |
| `token_hash` | VARCHAR(255) | — | Hashed token value |
| `expires_at` | TIMESTAMP | — | Token expiry (24h for verification, 15min for reset) |
| `consumed_at` | TIMESTAMP | NULL | When token was used |
| `created_at` | TIMESTAMP | NOW() | When token was created |

**Indexes**:
- `token_hash`
- `(user_id, token_type)`

**Rules**:
- Raw token never stored. Only hash in DB.
- One-time use. Set `consumed_at` on use.
- New verification token should invalidate older active ones for same user.
- New reset token should invalidate older active reset tokens for same user.

## OAuthAccount (new table)

| Column | Type | Default | Description |
|--------|------|---------|-------------|
| `id` | BIGSERIAL | auto | Primary key |
| `user_id` | UUID | — | FK → users(id) |
| `provider` | VARCHAR(40) | — | Provider name (GOOGLE) |
| `provider_subject` | VARCHAR(255) | — | Provider's unique user ID |
| `provider_email` | VARCHAR(255) | — | Email from provider |
| `provider_email_verified` | BOOLEAN | — | Whether provider verified the email |
| `created_at` | TIMESTAMP | NOW() | When record was created |
| `updated_at` | TIMESTAMP | NULL | Last update |

**Unique constraint**: `(provider, provider_subject)`

**Indexes**:
- `user_id`
- `provider_email`

**Rules**:
- Google is the only provider in this feature.
- Provider email must be verified (reject unverified).
- Existing app user linked by matching verified email.
- No provider access/refresh tokens stored.
- No Google client secret stored in database.

## PersistentLogin (new table)

Spring Security standard persistent remember-me table.

| Column | Type | Description |
|--------|------|-------------|
| `username` | VARCHAR(64) | Login identifier (email) |
| `series` | VARCHAR(64) | Series identifier (PRIMARY KEY) |
| `token` | VARCHAR(64) | Persistent token |
| `last_used` | TIMESTAMP | Last authentication time |

**Rules**:
- Managed by Spring Security `PersistentTokenRepository`.
- Must be invalidated on logout.
- Must not authenticate blocked/deleted/locked users.

## Entity Descriptions

### User
App account. Login by email only. Supports password and OAuth2 login. Tracked for failed attempts and lock state.

### AuthToken
One-time hashed token for email verification or password reset. Raw token appears only in email link. 24h TTL for verification, 15min for reset.

### OAuthAccount
External OAuth2 identity linked to a user. Google only in this feature. Provider email must be verified before linking/creation.

### PersistentLogin
Spring Security managed persistent remember-me table. Tracks series-based persistent authentication tokens.
