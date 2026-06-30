---
description: "DRAFT task breakdown for feat/011-auth-hardening"
---

# DRAFT Tasks: Auth Hardening and Spring Security Migration

**Feature Branch**: `feat/011-auth-hardening`  
**Input**: Design documents from `specs/011-auth-hardening/`  
**Primary Documents**: `spec.md` and `plan.md` for Auth Hardening and Spring Security Migration.  
**Status**: DRAFT v0.1

> **Instruction for implementer / DeepSeek / OpenCode**: This file is a DRAFT task document. If you have no questions, contradictions, or evidence-based concerns, copy this document verbatim into the official `tasks.md` for this feature. Do not creatively rewrite it. Do not simplify it. Do not add extra scope. Do not remove STOP checkpoints. Do not replace the phase order unless you have a concrete conflict from the current codebase and the user explicitly approves the change.

> **Hard rule**: If current code conflicts with this task list, STOP and report the conflict. Do not silently reinterpret requirements. Do not "fix forward" by inventing architecture.

---

## Source of Truth Order

When sources conflict, follow this order:

1. `specs/011-auth-hardening/spec.md`
2. `specs/011-auth-hardening/plan.md`
3. this `tasks.md`
4. project constitution / Spec Kit rules
5. explicitly approved user decisions in the current chat
6. current codebase evidence
7. external documentation
8. model assumptions

If two sources conflict, STOP and ask. Do not silently choose.

---

## Global Execution Rules

### Required MCP Usage

#### Context7 MCP is mandatory before every logical task group

Before each backend, frontend, database, security, or testing group, use Context7 MCP to refresh relevant official documentation.

Examples:

- Spring Security non-Boot servlet setup
- SecurityFilterChain
- Spring Security JSON login/custom handlers
- Spring Security CSRF
- Spring Security remember-me
- Spring Security OAuth2 Login / OIDC
- PasswordEncoder
- Spring MVC controllers
- JDBC and transactions
- JUnit 5 / Mockito
- Vue 3 Composition API
- Vue Router
- vue-i18n
- PrimeVue components
- Vitest / Vue Test Utils
- Playwright MCP usage patterns

Each checkpoint report MUST state:

- which Context7 docs were checked;
- what decisions they affected;
- whether any doc/code conflict was found.

#### Serena MCP is mandatory for code navigation and edits

Use Serena MCP instead of blind grep/manual broad scanning.

Required workflow before each code group:

1. inspect relevant symbols;
2. inspect references;
3. inspect call graph;
4. identify exact edit points;
5. edit targeted files only.

Manual grep/search is allowed only if Serena cannot answer the query. If used, report why Serena was insufficient.

#### Postgres MCP is mandatory for database inspection and verification

Use Postgres MCP for:

- current schema inspection;
- lookup codes;
- current users/password/auth fields;
- migration verification;
- auth token verification;
- OAuth account verification;
- persistent remember-me verification.

Do not guess schema. Do not create migrations from assumptions.

#### Playwright MCP is mandatory for browser/e2e evidence

Use Playwright MCP for:

- registration;
- check-email page;
- verification result;
- login/logout;
- CSRF behavior;
- forgot/reset password;
- remember-me where practical;
- Google OAuth start/error states;
- admin Account tab verification status;
- i18n evidence;
- smoke checks.

Do not install local Playwright. Use available Playwright MCP.

#### spec-kit-memory MCP

If you discover a reusable lesson, propose it to the user first. Do not write memory silently.

---

## DeepSeek Discipline Rules

These rules are based on recurring implementation anti-patterns previously caught in capstone work.

### No creativity moves

- Do not redesign the architecture.
- Do not "improve" the UI beyond the task.
- Do not create extra routes, pages, tables, fields, or flows.
- Do not rename existing concepts unless the task explicitly says so.
- Do not invent onboarding, account linking UI, multi-provider OAuth, JWT, or admin dashboards.
- Do not replace the existing project stack.

### No Spring Boot drift

Forbidden:

- Spring Boot starters
- `@SpringBootApplication`
- Boot auto-configuration assumptions
- Boot-specific OAuth property assumptions without explicit non-Boot wiring
- Spring Data JPA
- Hibernate
- Lombok
- JWT migration
- external auth platforms

This is a non-Boot Spring MVC project.

### No broad scaffolding

Past issue: generated broad placeholder scaffolding and extra routes created drift from the approved feature.

Rules:

- Add only approved routes.
- Add only approved DTOs/classes.
- Do not create placeholder features.
- Do not create huge "future-ready" structures.
- Keep implementation narrow and testable.

### No wrong HTTP client usage

Past issue: raw frontend `fetch()` bypassed the shared API client and missed CSRF headers.

Rules:

- Use the existing shared frontend API client pattern.
- Do not introduce raw `fetch()` for protected API calls unless explicitly approved.
- CSRF must use cookie `XSRF-TOKEN` and header `X-XSRF-TOKEN`.
- After changing CSRF, verify unsafe requests through tests/browser evidence.

### No port / entrypoint drift

Past issue: local port and entrypoint behavior broke, making `/app/auth` hit the backend instead of frontend.

Rules:

- Do not change Docker/Nginx/port mapping unless the task explicitly requires it.
- If deployment config must change, STOP and report.
- Verify frontend `/app/auth` and API proxy behavior after changes.

### No mock/spike leakage into production

Past issue: spike/mock ideas risked being copied into production.

Rules:

- Do not port mock-only tables, seed behavior, fake providers, or prototype shortcuts unless explicitly approved.
- Dev captcha token is allowed only because it is explicitly approved.
- Dev email logging is allowed only because it is explicitly approved.
- Production must not accept dev bypasses.

### No permanent double-security system

During migration, legacy code may temporarily exist.

Final state must not keep:

- custom auth as equal source of truth;
- custom CSRF filter alongside Spring Security CSRF;
- AuthInterceptor as second permanent backend authorization layer;
- stale `HttpSession` user attribute checks as authoritative.

Spring Security must become authoritative.

### No hidden weakening of tests

- Do not delete tests to make build pass.
- Do not weaken assertions.
- Do not mark tests ignored/skipped without explicit approval.
- New security behavior requires positive, negative, boundary, and abuse-case tests.

### STOP_FOR_CONFIRMATION rule

STOP immediately if:

- docs and code conflict;
- migration needs an unapproved dependency;
- Spring Security non-Boot setup differs from examples;
- current DB schema differs from assumed schema;
- tests require deleting/weakening old coverage;
- unrelated PDF/AI/generation files appear necessary;
- a phase cannot be completed without changing scope;
- you are unsure.

---

## Execution Markers

| Marker | Meaning |
|---|---|
| `[CTX7]` | Must use Context7 MCP before/while executing this task group |
| `[SERENA]` | Must use Serena MCP for symbol/reference navigation and edits |
| `[PG-MCP]` | Must use Postgres MCP for schema/data verification |
| `[PW-MCP]` | Must use Playwright MCP for browser/e2e evidence |
| `[MEMORY]` | Consider spec-kit-memory MCP, but ask user before writing memory |
| `[AGENT]` | Recommend best agent/context switch before starting |
| `[TDD]` | Write/update failing tests first |
| `[SEC]` | Security-sensitive task |
| `[KISS]` | Keep implementation simple and maintainable |
| `[STOP]` | Stop and wait for user confirmation |
| `[EVIDENCE]` | Provide concrete evidence |
| `[NO-PDF-AI]` | Must not touch PDF/AI/generation internals |
| `[NO-BOOT]` | Must not use Spring Boot |
| `[NO-CREATIVE-SCOPE]` | Do not invent scope beyond spec/plan/tasks |
| `[P]` | Can run in parallel with other `[P]` tasks only within the same approved phase |

---

## Phase 0 — Baseline Security Map

**Goal**: Understand current auth/security behavior before editing.

**Before starting**:

- [ ] [AGENT] Recommend switching to a backend/security analysis context.
- [ ] [CTX7] Refresh docs for Spring Security servlet architecture, SecurityFilterChain, CSRF, remember-me, OAuth2 Login, PasswordEncoder, and non-Boot setup.
- [ ] [SERENA] Inspect code symbols and references before any edits.
- [ ] [PG-MCP] Inspect current DB schema and lookup data.
- [ ] [NO-PDF-AI] Confirm PDF/AI/generation/finalization code is out of scope.

### Tasks

- [ ] T001 [SERENA] Inspect current backend auth flow: `AuthController`, `AuthService`, `UserDao`, `UserSession`, `PasswordService`, `PasswordStrengthValidator`.
- [ ] T002 [SERENA] Inspect current legacy security boundaries: `AuthInterceptor`, `CsrfFilter`, `WebConfig`, `AppInitializer`.
- [ ] T003 [SERENA] Inspect current admin authorization behavior and `/api/admin/**` protection.
- [ ] T004 [SERENA] Inspect current frontend auth flow: `AuthPage.vue`, `LoginForm.vue`, `RegisterForm.vue`, `authService.ts`, `useAuth.ts`, router guard.
- [ ] T005 [SERENA] Inspect current shared frontend API client and CSRF header behavior. Confirm no new raw `fetch()` should be introduced.
- [ ] T006 [PG-MCP] Verify current `users` table columns, password hash format, failed login fields, soft-delete fields, and timestamps.
- [ ] T007 [PG-MCP] Verify current lookup codes for roles, statuses, permissions.
- [ ] T008 [SERENA] Inspect current backend tests for auth, filters, interceptors, controllers, services, and DAOs.
- [ ] T009 [SERENA] Inspect current frontend tests for auth/router/services if present.
- [ ] T010 [NO-PDF-AI] Confirm no required change in PDF, AI, prompt, parser, OpenRouter, generation, finalization, budget config, or PDF templates.

### Checkpoint

- [ ] T011 [STOP] [EVIDENCE] Report:
  - current backend auth source of truth;
  - current frontend auth contract;
  - current CSRF behavior;
  - current admin authorization gaps;
  - current DB schema evidence;
  - exact migration files needed;
  - exact files likely affected;
  - tests baseline;
  - confirmation that forbidden domains are untouched.

Do not proceed without user approval.

---

## Phase 1 — Dependencies and Non-Boot Spring Security Bootstrap

**Goal**: Add Spring Security dependencies and explicit non-Boot integration.

**Before starting**:

- [ ] [CTX7] Refresh docs for non-Boot Spring Security servlet setup.
- [ ] [SERENA] Inspect `pom.xml`, initializer/config classes.
- [ ] [NO-BOOT] Confirm no Spring Boot artifacts will be added.
- [ ] [TDD] Add/adjust minimal test to prove filter chain/config loads if practical.

### Tasks

- [ ] T012 [NO-BOOT] Add only approved Spring Security dependencies:
  - `spring-security-web`
  - `spring-security-config`
  - `spring-security-core`
  - `spring-security-crypto`
  - `spring-security-oauth2-client`
  - `spring-security-oauth2-jose`
- [ ] T013 [NO-BOOT] Explicitly register Spring Security filter chain in current non-Boot servlet setup.
- [ ] T014 [SERENA] Add minimal `SecurityConfig` following non-Boot Spring MVC style.
- [ ] T015 [SEC] Keep behavior minimally permissive only during bootstrap if needed; do not treat this as final security.
- [ ] T016 [TDD] Add test proving Spring Security config/filter chain is active or app context loads with it.
- [ ] T017 Run backend tests affected by config.

### Checkpoint

- [ ] T018 [STOP] [EVIDENCE] Report:
  - dependency diff;
  - exact non-Boot registration mechanism;
  - proof no Spring Boot starter/classes were added;
  - test output;
  - any unresolved security bootstrap concerns.

Do not proceed without user approval.

---

## Phase 2 — Database Migrations

**Goal**: Add auth schema safely.

**Before starting**:

- [ ] [CTX7] Refresh docs for Flyway/JDBC if needed.
- [ ] [SERENA] Inspect migration naming/order.
- [ ] [PG-MCP] Confirm current schema before writing migrations.
- [ ] [TDD] Add/adjust DAO/migration tests where current project style supports it.

### Tasks

- [ ] T019 [PG-MCP] Re-confirm `users` schema immediately before migration.
- [ ] T020 Add migration for users auth columns:
  - `email_verified`
  - `email_verified_at`
  - `password_login_enabled`
  - `updated_at` if needed and not present.
- [ ] T021 Add migration for `auth_tokens`.
- [ ] T022 Add migration for `oauth_accounts`.
- [ ] T023 Add migration for standard `persistent_logins`.
- [ ] T024 Add capstone/test-data-only migration:
  - mark existing users email verified;
  - set existing users password hash to encoded `Aa123456`;
  - preserve roles/statuses/permissions/privileged/deleted flags.
- [ ] T025 Include SQL comment warning that mass password reset is test-data-only and not production-safe.
- [ ] T026 [PG-MCP] Apply/verify migrations in dev/test DB.
- [ ] T027 Add DAO model or row mapper tests only after schema is verified.

### Checkpoint

- [ ] T028 [STOP] [EVIDENCE] Report:
  - migration files;
  - Postgres MCP schema output;
  - sample migrated existing user evidence;
  - proof no data hard-delete;
  - proof roles/status/permissions preserved.

Do not proceed without user approval.

---

## Phase 3 — PasswordEncoder and UserDetails

**Goal**: Make Spring Security load users by email and verify passwords.

**Before starting**:

- [ ] [CTX7] Refresh docs for PasswordEncoder, UserDetailsService, UserDetails, account state checks.
- [ ] [SERENA] Inspect `PasswordService`, `UserDao`, `User`, role/status/permission models.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T029 [TDD] Add tests for loading user by email only.
- [ ] T030 [TDD] Add tests proving username is not a login identifier.
- [ ] T031 Implement Spring Security PasswordEncoder configuration.
- [ ] T032 Implement `CustomUserDetails`.
- [ ] T033 Implement `CustomUserDetailsService`.
- [ ] T034 Map roles to Spring Security authorities.
- [ ] T035 Enforce deleted/blocked/locked/unverified states consistently.
- [ ] T036 Ensure password hashes are never exposed through DTOs/logs.
- [ ] T037 Verify existing migrated user can be authenticated with `Aa123456` in tests.
- [ ] T038 Run backend tests.

### Checkpoint

- [ ] T039 [STOP] [EVIDENCE] Report:
  - exact classes created/modified;
  - email-only lookup proof;
  - role/authority mapping;
  - account state checks;
  - password encoder behavior;
  - tests.

Do not proceed without user approval.

---

## Phase 4 — JSON Login, Logout, and Status

**Goal**: Move password login/logout/status to Spring Security while preserving SPA JSON contract.

**Before starting**:

- [ ] [CTX7] Refresh docs for custom JSON login handlers, authentication filters, logout handlers.
- [ ] [SERENA] Inspect existing auth controller/service/frontend status contract.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T040 [TDD] Add tests for valid email/password login JSON success.
- [ ] T041 [TDD] Add tests for invalid login JSON error.
- [ ] T042 [TDD] Add tests for `/api/auth/status` authenticated and unauthenticated states.
- [ ] T043 [TDD] Add tests for JSON logout.
- [ ] T044 Configure JSON login endpoint.
- [ ] T045 Add JSON authentication success handler.
- [ ] T046 Add JSON authentication failure handler.
- [ ] T047 Add JSON logout success handler.
- [ ] T048 Keep `/api/auth/status` response compatible with frontend `useAuth`.
- [ ] T049 Ensure old session `UserSession` is response DTO only, not source of truth.
- [ ] T050 Update frontend auth service only if contract requires it.
- [ ] T051 [PW-MCP] Verify login/status/logout in browser.

### Checkpoint

- [ ] T052 [STOP] [EVIDENCE] Report:
  - request/response examples;
  - backend test output;
  - frontend/browser evidence;
  - proof Spring Security Authentication is source of truth;
  - legacy code still present/temporary list.

Do not proceed without user approval.

---

## Phase 5 — Failed Login, Captcha Trigger, and Account Lock

**Goal**: Preserve brute-force protections.

**Before starting**:

- [ ] [CTX7] Refresh docs for authentication failure handling.
- [ ] [SERENA] Inspect failed login fields and existing auth logic.
- [ ] [PG-MCP] Verify fields in DB.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T053 [TDD] Add tests for failed login counter increment.
- [ ] T054 [TDD] Add tests for captcha required after 3 failed attempts.
- [ ] T055 [TDD] Add tests for lock after 5 failed attempts.
- [ ] T056 [TDD] Add tests for successful login resetting counters.
- [ ] T057 Implement failed login counter integration with Spring Security failure handler/provider.
- [ ] T058 Implement captcha-required signal after threshold.
- [ ] T059 Implement 15-minute lock after 5 failed attempts.
- [ ] T060 Add auth error codes:
  - `INVALID_CREDENTIALS`
  - `CAPTCHA_REQUIRED`
  - `ACCOUNT_LOCKED`
- [ ] T061 Ensure blocked/deleted accounts cannot bypass through login.
- [ ] T062 Run tests.

### Checkpoint

- [ ] T063 [STOP] [EVIDENCE] Report:
  - failed counter behavior;
  - captcha-required behavior;
  - lock behavior;
  - DB evidence if useful;
  - tests.

Do not proceed without user approval.

---

## Phase 6 — Spring Security CSRF Migration

**Goal**: Replace custom CSRF with Spring Security CSRF.

**Before starting**:

- [ ] [CTX7] Refresh Spring Security CSRF docs, especially cookie CSRF repository.
- [ ] [SERENA] Inspect `CsrfFilter`, frontend API client, auth/public endpoint exclusions.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T064 [TDD] Add tests for unsafe request rejected without CSRF.
- [ ] T065 [TDD] Add tests for unsafe request accepted with valid CSRF.
- [ ] T066 Configure Spring Security CSRF cookie `XSRF-TOKEN`.
- [ ] T067 Update frontend shared API client to send `X-XSRF-TOKEN`.
- [ ] T068 Remove or temporarily deprecate legacy `CsrfFilter`.
- [ ] T069 Confirm no raw `fetch()` bypasses shared CSRF client for protected calls.
- [ ] T070 [PW-MCP] Verify browser unsafe request behavior.
- [ ] T071 Run tests.

### Checkpoint

- [ ] T072 [STOP] [EVIDENCE] Report:
  - backend CSRF config;
  - frontend header update;
  - test output;
  - browser evidence;
  - status of legacy `CsrfFilter`.

Do not proceed without user approval.

---

## Phase 7 — Authorization and AuthInterceptor Cleanup

**Goal**: Make Spring Security authoritative for protected/admin routes.

**Before starting**:

- [ ] [CTX7] Refresh docs for authorization matchers/authorities.
- [ ] [SERENA] Inspect route/API patterns and `AuthInterceptor`.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T073 [TDD] Add tests: unauthenticated rejected from protected APIs.
- [ ] T074 [TDD] Add tests: USER rejected from `/api/admin/**`.
- [ ] T075 [TDD] Add tests: ADMIN allowed to `/api/admin/**`.
- [ ] T076 Configure Spring Security authorization rules.
- [ ] T077 Ensure public endpoints are only the explicitly approved ones.
- [ ] T078 Deprecate/remove legacy `AuthInterceptor` after proof.
- [ ] T079 Confirm frontend route guard remains UX only.
- [ ] T080 [PW-MCP] Verify admin/non-admin behavior.
- [ ] T081 Run tests.

### Checkpoint

- [ ] T082 [STOP] [EVIDENCE] Report:
  - authorization rules;
  - admin API examples;
  - test output;
  - Playwright evidence;
  - status of legacy `AuthInterceptor`.

Do not proceed without user approval.

---

## Phase 8 — Captcha Service

**Goal**: Add Cloudflare Turnstile verification with controlled dev mode.

**Before starting**:

- [ ] [CTX7] Refresh docs for Java HTTP client/project HTTP style and testing external calls.
- [ ] [SERENA] Inspect config loading style.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T083 [TDD] Add tests for valid dev captcha token.
- [ ] T084 [TDD] Add tests that dev bypass is rejected in prod mode.
- [ ] T085 [TDD] Add tests for invalid captcha response.
- [ ] T086 Implement `CaptchaService` interface only if it stays simple.
- [ ] T087 Implement Turnstile verification.
- [ ] T088 Implement config-driven dev token `dev-captcha-pass`.
- [ ] T089 Add production missing-secret fail-safe.
- [ ] T090 Add auth error codes:
  - `CAPTCHA_INVALID`
  - `CAPTCHA_REQUIRED`
- [ ] T091 Run tests.

### Checkpoint

- [ ] T092 [STOP] [EVIDENCE] Report:
  - captcha config;
  - dev behavior;
  - prod behavior;
  - tests.

Do not proceed without user approval.

---

## Phase 9 — Email Service with Resend

**Goal**: Add Resend-backed bilingual email delivery.

**Before starting**:

- [ ] [CTX7] Refresh docs for Java HTTP client/project HTTP style.
- [ ] [SERENA] Inspect config and service patterns.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T093 [TDD] Add tests for email template generation.
- [ ] T094 [TDD] Add tests for dev logging fallback.
- [ ] T095 [TDD] Add tests for prod missing API key failure.
- [ ] T096 Implement bilingual plain-text template.
- [ ] T097 Implement bilingual basic HTML template.
- [ ] T098 Implement Resend email sending service.
- [ ] T099 Ensure no Resend API key is exposed to frontend/logs.
- [ ] T100 Add config for:
  - `APP_BACKEND_PUBLIC_BASE_URL`
  - `APP_FRONTEND_PUBLIC_BASE_URL`
  - Resend API key
  - sender email
- [ ] T101 Run tests.

### Checkpoint

- [ ] T102 [STOP] [EVIDENCE] Report:
  - sample verification email;
  - sample password reset email;
  - dev fallback proof;
  - prod missing-key proof;
  - tests.

Do not proceed without user approval.

---

## Phase 10 — Registration and Email Verification

**Goal**: Implement strict email confirmation.

**Before starting**:

- [ ] [CTX7] Refresh docs for registration patterns with Spring Security and token safety.
- [ ] [SERENA] Inspect existing register flow and frontend form.
- [ ] [PG-MCP] Verify DB schema ready.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T103 [TDD] Add tests: registration requires captcha.
- [ ] T104 [TDD] Add tests: registration creates unverified user.
- [ ] T105 [TDD] Add tests: registration does not auto-login.
- [ ] T106 [TDD] Add tests: raw token is not stored.
- [ ] T107 [TDD] Add tests: valid verification token verifies user.
- [ ] T108 [TDD] Add tests: expired/invalid/consumed token fails safely.
- [ ] T109 Update registration DTO to include captcha token.
- [ ] T110 Update registration service to create unverified account.
- [ ] T111 Generate hashed email verification token.
- [ ] T112 Send verification email through email service.
- [ ] T113 Add backend verify endpoint.
- [ ] T114 Redirect verification result to frontend status page.
- [ ] T115 Add frontend check-email page.
- [ ] T116 Add frontend verified-result page.
- [ ] T117 Ensure unverified user cannot log in and gets `EMAIL_NOT_VERIFIED`.
- [ ] T118 [PW-MCP] Verify registration/check-email/verify flow.

### Checkpoint

- [ ] T119 [STOP] [EVIDENCE] Report:
  - register response;
  - DB user verification state;
  - token hash/consume behavior;
  - unverified login rejection;
  - browser evidence;
  - tests.

Do not proceed without user approval.

---

## Phase 11 — Resend Verification

**Goal**: Add safe resend-verification flow.

**Before starting**:

- [ ] [CTX7] Refresh rate limiting and Spring MVC docs as needed.
- [ ] [SERENA] Inspect auth token/email service.
- [ ] [PG-MCP] Verify token state if needed.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T120 [TDD] Add tests: resend requires captcha.
- [ ] T121 [TDD] Add tests: resend cooldown 60 seconds.
- [ ] T122 [TDD] Add tests: max 5/hour and 20/day per email/IP.
- [ ] T123 [TDD] Add tests: unknown/already-verified email safe response.
- [ ] T124 Implement resend endpoint.
- [ ] T125 Invalidate or supersede old active verification tokens.
- [ ] T126 Add frontend resend action on check-email/unverified-login state.
- [ ] T127 Add localized messages.
- [ ] T128 [PW-MCP] Verify resend UI and rate limit state.

### Checkpoint

- [ ] T129 [STOP] [EVIDENCE] Report:
  - resend API behavior;
  - rate limit proof;
  - token replacement behavior;
  - browser evidence;
  - tests.

Do not proceed without user approval.

---

## Phase 12 — Password Reset

**Goal**: Implement forgot-password and reset-password.

**Before starting**:

- [ ] [CTX7] Refresh secure reset token practices and Spring MVC validation.
- [ ] [SERENA] Inspect frontend routing/auth service.
- [ ] [PG-MCP] Verify token table.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T130 [TDD] Add tests: forgot-password requires captcha.
- [ ] T131 [TDD] Add tests: unknown email returns generic response.
- [ ] T132 [TDD] Add tests: reset token TTL 15 minutes.
- [ ] T133 [TDD] Add tests: raw token is not stored.
- [ ] T134 [TDD] Add tests: validate token valid/expired/invalid.
- [ ] T135 [TDD] Add tests: confirm reset consumes token.
- [ ] T136 [TDD] Add tests: consumed token cannot be reused.
- [ ] T137 [TDD] Add tests: other active reset tokens invalidated.
- [ ] T138 Implement forgot-password endpoint.
- [ ] T139 Implement token validate endpoint.
- [ ] T140 Implement reset confirm endpoint.
- [ ] T141 Add forgot-password frontend page.
- [ ] T142 Add reset-password frontend page.
- [ ] T143 Add localized messages.
- [ ] T144 [PW-MCP] Verify full browser reset flow.

### Checkpoint

- [ ] T145 [STOP] [EVIDENCE] Report:
  - generic request response;
  - token TTL;
  - token consume behavior;
  - password changed and login works;
  - browser evidence;
  - tests.

Do not proceed without user approval.

---

## Phase 13 — Persistent Remember-Me

**Goal**: Implement Spring Security persistent remember-me.

**Before starting**:

- [ ] [CTX7] Refresh Spring Security persistent remember-me docs.
- [ ] [SERENA] Inspect security config and login request handling.
- [ ] [PG-MCP] Verify `persistent_logins`.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T146 [TDD] Add tests: remember-me token created when requested.
- [ ] T147 [TDD] Add tests: no token created when not requested.
- [ ] T148 [TDD] Add tests: logout clears remember-me.
- [ ] T149 [TDD] Add tests: blocked/deleted/locked users cannot authenticate via remember-me.
- [ ] T150 Configure persistent remember-me.
- [ ] T151 Use email as persistent remember-me username.
- [ ] T152 Ensure logout clears token/cookie.
- [ ] T153 [PG-MCP] Verify token insert/remove.
- [ ] T154 [PW-MCP] Verify practical browser behavior if possible.

### Checkpoint

- [ ] T155 [STOP] [EVIDENCE] Report:
  - remember-me config;
  - DB token evidence;
  - logout clearing behavior;
  - blocked/deleted/locked behavior;
  - tests.

Do not proceed without user approval.

---

## Phase 14 — Google OAuth2

**Goal**: Add Google OAuth2 Login through Spring Security.

**Before starting**:

- [ ] [CTX7] Refresh Spring Security OAuth2 Login/OIDC docs for servlet/non-Boot setup.
- [ ] [SERENA] Inspect security config, user DAO, frontend auth page.
- [ ] [PG-MCP] Verify `oauth_accounts`.
- [ ] [TDD] Tests first with mocked OAuth/OIDC principal. Do not require real Google in automated tests.

### Tasks

- [ ] T156 [TDD] Add tests: verified new Google email creates user.
- [ ] T157 [TDD] Add tests: verified existing email auto-links account.
- [ ] T158 [TDD] Add tests: unverified Google email rejected.
- [ ] T159 [TDD] Add tests: duplicate provider identity does not duplicate app account.
- [ ] T160 Configure Google OAuth2 client for non-Boot app.
- [ ] T161 Add frontend Continue with Google button linking to `/oauth2/authorization/google`.
- [ ] T162 Implement OAuth2 success handler.
- [ ] T163 Implement OAuth2 failure handler.
- [ ] T164 Implement OAuth account DAO/service.
- [ ] T165 Create new user for verified new Google email.
- [ ] T166 Auto-link existing user by verified email.
- [ ] T167 Store technical random password hash for OAuth-only users.
- [ ] T168 Set password login disabled for OAuth-only users.
- [ ] T169 Do not create/populate `contact_detail` from Google profile.
- [ ] T170 Redirect by role after success.
- [ ] T171 Redirect to safe localized error after failure.
- [ ] T172 [PW-MCP] Verify OAuth start and failure/error state.

### Checkpoint

- [ ] T173 [STOP] [EVIDENCE] Report:
  - OAuth config without Spring Boot;
  - new-user behavior;
  - existing-user link behavior;
  - unverified-provider-email rejection;
  - `oauth_accounts` DB evidence;
  - tests;
  - browser evidence.

Do not proceed without user approval.

---

## Phase 15 — Frontend Auth UI and i18n Polish

**Goal**: Finish frontend auth states and natural EN/RU copy.

**Before starting**:

- [ ] [CTX7] Refresh Vue 3, Vue Router, vue-i18n, Vitest, Vue Test Utils.
- [ ] [SERENA] Inspect frontend routes/components/services.
- [ ] [PW-MCP] Plan browser evidence.

### Tasks

- [ ] T174 [TDD] Add/update frontend tests for email-only login.
- [ ] T175 [TDD] Add/update frontend tests for check-email page.
- [ ] T176 [TDD] Add/update frontend tests for verified result page.
- [ ] T177 [TDD] Add/update frontend tests for forgot/reset password.
- [ ] T178 [TDD] Add/update frontend tests for OAuth button URL.
- [ ] T179 [TDD] Add/update frontend tests for captcha-required message.
- [ ] T180 Add/update routes:
  - `/app/auth/check-email`
  - `/app/auth/verified`
  - `/app/auth/forgot-password`
  - `/app/auth/reset-password`
- [ ] T181 Update auth service methods.
- [ ] T182 Update auth composable only as needed.
- [ ] T183 Update login/register forms.
- [ ] T184 Add forgot/reset/check/verified pages.
- [ ] T185 Add OAuth error handling.
- [ ] T186 Add captcha UI integration.
- [ ] T187 Update EN i18n.
- [ ] T188 Update RU i18n.
- [ ] T189 Proofread landing copy.
- [ ] T190 Confirm no raw i18n keys visible.
- [ ] T191 [PW-MCP] Capture screenshots/evidence in EN and RU.

### Checkpoint

- [ ] T192 [STOP] [EVIDENCE] Report:
  - frontend changed files;
  - auth routes;
  - i18n keys;
  - screenshots;
  - tests;
  - confirmation no raw fetch bypasses shared API client.

Do not proceed without user approval.

---

## Phase 16 — Admin Account Tab Email Verification Status

**Goal**: Show email verification status only in Admin User Details → Account tab.

**Before starting**:

- [ ] [CTX7] Refresh Vue/PrimeVue/Vitest if needed.
- [ ] [SERENA] Inspect Admin User Details DTO/service/component.
- [ ] [TDD] Tests first.

### Tasks

- [ ] T193 [TDD] Add backend test for admin user details includes email verification status.
- [ ] T194 [TDD] Add frontend/component test for Account tab status display.
- [ ] T195 Add backend DTO field for email verification status.
- [ ] T196 Do not expose password hashes, token hashes, OAuth identities, or secrets.
- [ ] T197 Display status in Account tab only.
- [ ] T198 Confirm Admin Users table is unchanged.
- [ ] T199 [PW-MCP] Verify admin Account tab.
- [ ] T200 Run tests.

### Checkpoint

- [ ] T201 [STOP] [EVIDENCE] Report:
  - DTO change;
  - Account tab screenshot;
  - Users table unchanged proof;
  - non-admin rejection proof;
  - tests.

Do not proceed without user approval.

---

## Phase 17 — Production Hardening and Landing/Public Edge

**Goal**: Add deploy-oriented guardrails.

**Before starting**:

- [ ] [CTX7] Refresh relevant Spring Security/config docs.
- [ ] [SERENA] Inspect config files, Nginx/Docker only if needed.
- [ ] [NO-CREATIVE-SCOPE] Do not change port/entrypoint unless explicitly required.

### Tasks

- [ ] T202 Add production config validation for Resend, Turnstile, Google OAuth, public URLs.
- [ ] T203 Add dev fallback config for email logging and captcha dev token.
- [ ] T204 Configure secure production cookie behavior:
  - HttpOnly session cookie;
  - Secure under HTTPS;
  - SameSite Lax where practical;
  - CSRF cookie readable by JS if required for SPA header.
- [ ] T205 Document landing protection correctly:
  - Nginx/proxy rate limiting;
  - static caching;
  - request/body limits;
  - no claim that captcha protects GET landing from DDoS.
- [ ] T206 Ensure no backend secrets are present in frontend assets.
- [ ] T207 Add tests or manual evidence for missing prod secrets.
- [ ] T208 [PW-MCP] Run smoke check for public landing and auth pages.

### Checkpoint

- [ ] T209 [STOP] [EVIDENCE] Report:
  - prod missing-secret behavior;
  - dev fallback behavior;
  - cookie config;
  - landing protection notes/config;
  - no port/entrypoint drift;
  - tests/evidence.

Do not proceed without user approval.

---

## Phase 18 — Legacy Cleanup and Regression Sweep

**Goal**: Remove temporary legacy security code and prove the app still works.

**Before starting**:

- [ ] [CTX7] Refresh final needed docs.
- [ ] [SERENA] Find references before removing legacy code.
- [ ] [PG-MCP] Verify auth tables after flows.
- [ ] [PW-MCP] Prepare smoke test checklist.

### Tasks

- [ ] T210 [SERENA] Find all references to legacy `AuthInterceptor`.
- [ ] T211 [SERENA] Find all references to legacy `CsrfFilter`.
- [ ] T212 [SERENA] Find all references to old session source-of-truth logic.
- [ ] T213 Remove legacy `AuthInterceptor` if Spring Security fully replaces it.
- [ ] T214 Remove legacy `CsrfFilter` if Spring Security CSRF fully replaces it.
- [ ] T215 Remove old custom auth source-of-truth logic.
- [ ] T216 Keep `UserSession`-style DTO only if needed for `/api/auth/status` response compatibility.
- [ ] T217 Run full backend test suite.
- [ ] T218 Run frontend tests.
- [ ] T219 [PG-MCP] Verify:
  - users auth columns;
  - auth_tokens consumed states;
  - oauth_accounts;
  - persistent_logins.
- [ ] T220 [PW-MCP] Smoke test:
  - landing;
  - register;
  - verify email;
  - login;
  - logout;
  - forgot/reset password;
  - admin auth;
  - non-admin rejection;
  - OAuth start/error;
  - user home;
  - profile route access;
  - generate route access.
- [ ] T221 [NO-PDF-AI] Confirm no PDF/AI/generation/finalization files changed.
- [ ] T222 [NO-BOOT] Confirm no Spring Boot dependency/class/config was added.
- [ ] T223 [SEC] Confirm no raw token/hash/secret exposure.
- [ ] T224 [MEMORY] Propose any reusable lesson to user before writing memory.

### Final Checkpoint

- [ ] T225 [STOP] [EVIDENCE] Final report:
  - all changed files;
  - removed legacy files/classes;
  - full backend test output;
  - frontend test output;
  - Playwright evidence;
  - Postgres evidence;
  - dependency list;
  - no Spring Boot proof;
  - no forbidden scope proof;
  - no PDF/AI/generation changes proof;
  - known limitations;
  - recommendation whether feature is safe to commit/merge.

Do not commit without user permission.

---

## Approved Backend Endpoint Targets

Do not invent additional auth endpoints without STOP approval.

- [ ] `POST /api/auth/register`
- [ ] `POST /api/auth/login`
- [ ] `POST /api/auth/logout`
- [ ] `GET /api/auth/status`
- [ ] `GET /api/auth/verify-email?token=...`
- [ ] `POST /api/auth/email-verification/resend`
- [ ] `POST /api/auth/password-reset/request`
- [ ] `GET /api/auth/password-reset/validate?token=...`
- [ ] `POST /api/auth/password-reset/confirm`
- [ ] `GET /oauth2/authorization/google`
- [ ] Spring Security OAuth2 callback endpoint as required by Spring Security

---

## Approved Frontend Routes

Do not invent additional auth routes without STOP approval.

- [ ] `/app/auth`
- [ ] `/app/auth/check-email`
- [ ] `/app/auth/verified`
- [ ] `/app/auth/forgot-password`
- [ ] `/app/auth/reset-password`

---

## Approved New Database Objects

Do not invent additional auth tables without STOP approval.

- [ ] users auth columns
- [ ] `auth_tokens`
- [ ] `oauth_accounts`
- [ ] `persistent_logins`

---

## Forbidden File Areas

Do not edit these unless user explicitly approves after STOP report:

- PDF rendering
- PDF fitting engine
- PDF validation
- PDF templates
- AI generation
- OpenRouter client
- prompt builder
- AI parser/validator
- resume finalization pipeline
- budget config
- public resume behavior unrelated to auth
- admin moderation redesign
- profile editing unrelated to auth

---

## Evidence Format for Every STOP Checkpoint

Every checkpoint report must include:

1. Context7 docs checked.
2. Serena symbols/references inspected.
3. Postgres MCP evidence if DB was involved.
4. Playwright MCP evidence if UI/browser was involved.
5. Files changed.
6. Tests added/updated.
7. Test command output.
8. Security notes.
9. Scope compliance notes.
10. Explicit question: "May I proceed to the next phase?"

---

## Final Note to Implementer

If there are no questions or contradictions after reading this draft, copy it into the official `tasks.md` for `feat/011-auth-hardening` and follow it strictly.

Do not rewrite this plan into a shorter version.

Do not merge phases.

Do not skip STOP checkpoints.

Do not make creative architecture decisions.

When in doubt, STOP.
