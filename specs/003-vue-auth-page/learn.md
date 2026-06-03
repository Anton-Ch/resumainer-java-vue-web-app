# What I Learned: Vue Auth Page

**Feature**: Registration + Login + Session-based auth with Vue 3 frontend and Spring MVC backend
**Generated**: 2026-06-03
**Scope**: Full feature — all 10 phases
**Implementation status**: 63/63 tasks completed

---

## Key Decisions

### 1. Session-Based Auth Over JWT

**What we did**: Used server-side HttpSession with JSESSIONID cookie instead of JWT tokens.

**Why**: For a single-server Capstone project, server-side sessions are simpler: no token refresh logic, no client-side token storage, immediate invalidation on logout. The session is just a Java object in memory — no library needed beyond what Spring MVC already provides. JWT would add complexity (token generation, validation, refresh cycles, secure storage in browser) without benefit for a single-server app.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| JWT (stateless tokens) | Adds token refresh, secure storage, and blacklist complexity. No benefit for single-server. |
| Spring Security | Heavy framework for a Capstone. HandlerInterceptor + session attribute check is 30 lines. |

**When you'd choose differently**: For a multi-server deployment or if you need to share auth state across separate backend services (microservices), JWT becomes valuable because it avoids centralized session storage.

---

### 2. Plain JDBC Over ORM

**What we did**: Used raw JDBC with PreparedStatement and manual `mapRow()` instead of JPA/Hibernate.

**Why**: The project constitution explicitly forbids ORM. But beyond that, plain JDBC gives you complete control over SQL — no magic queries, no lazy loading surprises, no N+1 problems. For a schema with 6 tables and simple CRUD, the boilerplate is manageable (about 20 lines per DAO). You also learn exactly what SQL runs against your database.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| JPA / Hibernate | Forbidden by constitution. Would add complexity far exceeding the needs of 6 simple tables. |
| Spring Data JDBC | Lighter than JPA, but still adds abstraction layer. Not needed for this scope. |

**When you'd choose differently**: If you had 30+ entities with complex relationships, JPA's lazy loading and cascade operations would save significant boilerplate. For a prototype with simple queries, stay with plain JDBC.

---

### 3. JDBC Transaction via Connection Overloads Instead of @Transactional

**What we did**: Added `create(Entity, Connection)` overloads to DAOs so the Service can manage one connection across multiple DAO operations.

**Why**: In pure Spring MVC without Spring Boot, `@Transactional` requires AOP configuration that's complex to set up. The connection-overload pattern is explicit, testable (you can pass a mock connection), and doesn't hide what's happening. Registration creates User + ContactDetail atomically — if User creation succeeds but ContactDetail fails, the entire transaction rolls back.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| `@Transactional` annotation | Requires AOP configuration in pure Spring MVC. More magic, harder to debug. |
| Each DAO manages its own connection | Can't support multi-table transactions. |
| TransactionTemplate | Spring's programmatic template — adds API dependency. Connection overloads are simpler. |

**When you'd choose differently**: If the project moves to Spring Boot, `@Transactional` becomes trivial (one `@EnableTransactionManagement`). Use connection overloads only in pure Spring MVC.

---

### 4. BCrypt Over Other Password Hashing

**What we did**: Used BCrypt (via `at.favre.lib:bcrypt`) with cost factor 12 for all password hashing.

**Why**: BCrypt is the industry standard for password storage. It's deliberately slow (cost 12 takes ~250ms) — that's a feature, not a bug. It makes brute-force attacks impractical. The `at.favre.lib` fork is actively maintained (unlike the original `jbcrypt` which hasn't been updated since 2015).

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| SHA-256 (fast hash) | Fast hashes are vulnerable to GPU brute-force. BCrypt's slowness is the whole point. |
| Argon2 | Stronger than BCrypt, but Java library support is less mature. Overkill for this scope. |
| No hashing (plaintext) | Catastrophically insecure. Never store plaintext passwords. |

**When you'd choose differently**: For a high-security application, Argon2 would be the better choice (it's resistant to GPU/ASIC attacks). For a Capstone project, BCrypt at cost 12 is the right balance of security and simplicity.

---

### 5. PrimeVue 4 Form + Zod Over HTML Forms

**What we did**: Used PrimeVue 4's `<Form>` component with `zodResolver` for all form validation instead of plain HTML forms with manual validation.

**Why**: PrimeVue 4's Form component provides a declarative validation pipeline: define a Zod schema once, and the form automatically handles error display, field states, and submission gating. Zod schemas are type-safe (TypeScript infers the validated type), composable (reuse validation rules), and the code is self-documenting — you can read the schema and understand exactly what's required.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Plain HTML forms with v-model | Manual validation logic in every component. More code, more bugs. |
| VeeValidate | Popular but adds another dependency. PrimeVue Form + Zod is the PrimeVue-native approach. |

**When you'd choose differently**: If you needed complex cross-field validation (e.g., "if field A > X then field B becomes required"), VeeValidate's conditional validation rules are more expressive. For standard form validation, PrimeVue Form + Zod is cleaner.

---

### 6. OWASP Cookie-to-Header CSRF Instead of SameSite Only

**What we did**: Implemented a custom `CsrfFilter` that generates a SecureRandom token, stores it in session, sets it as a non-HTTP-only cookie, and validates it on every POST/PUT/DELETE.

**Why**: SameSite=Lax is not sufficient for SPAs — it doesn't protect state-changing GET requests, doesn't cover all browsers equally, and doesn't protect against subdomain attacks. The OWASP cookie-to-header pattern is the recommended approach for SPAs.

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| SameSite=Lax only | Insufficient for SPA security requirements per OWASP. |
| Spring Security CSRF | Adds entire Spring Security framework. Custom filter is ~80 lines. |

**When you'd choose differently**: If the project adds Spring Security later, replace the custom filter with Spring Security's built-in CSRF protection. The custom filter is a temporary solution for pure Spring MVC.

---

### 7. i18n-First Design (No Hardcoded Strings)

**What we did**: Every user-facing string in the Vue SPA uses `$t('key')` from vue-i18n. Zod validation messages are also i18n-reactive via `watch(locale)`.

**Why**: Hardcoded strings look correct in English (the developer's language) but break completely when the user switches to Russian. This was discovered during manual testing — 6 hardcoded strings were found in the auth page that nobody noticed during implementation because "it looked fine in English."

**Alternatives considered**:

| Approach | Why it wasn't chosen |
|----------|---------------------|
| Hardcode English, add i18n later | Creates invisible bugs. Always cheaper to i18n from day one. |
| Machine translation only | Russian text needs to sound natural, not translated. Manual translations in ru.json. |

**When you'd choose differently**: For a single-language app, skip i18n entirely. For any multi-language app, do i18n from the first commit — retrofitting is harder than building it in.

---

## Concepts to Know

### Standardized Layered Architecture (Controller → Service → DAO)

**What it is**: A three-layer pattern where each layer has a distinct responsibility: Controller handles HTTP and validation, Service handles business logic, DAO handles database access. Dependencies flow in one direction: Controller → Service → DAO.

**Where we used it**: `AuthController` → `AuthService` → `UserDao` / `ContactDetailDao`. The Controller never touches SQL. The Service never touches HTTP responses. Each layer is independently testable.

**Why it matters**: Without layers, business logic leaks into controllers (fat controllers) and SQL leaks into services. Changes to the database schema require changes across the entire codebase. Layers create firewalls — you can rewrite the database access layer without touching business logic, and vice versa.

---

### TDD (Test-Driven Development) with Mockito

**What it is**: Write the test before the implementation. Watch it fail (RED). Write minimal code to pass (GREEN). Refactor. Repeat. Mockito creates fake objects (mocks) so you can test one class in isolation without setting up the database.

**Where we used it**: All 6 DAOs, both Services, the Controller (MockMvc), and the Interceptor were developed test-first. 74 tests total.

**Why it matters**: Testing after implementation doesn't prove the test works — you never saw it fail. TDD forces you to think about what the code should do *before* writing it. The test is the specification. If you can't write a test, you don't understand the requirement.

---

### CSRF (Cross-Site Request Forgery) Protection

**What it is**: An attack where a malicious site tricks a user's browser into making a state-changing request (like transferring money) to a site where the user is authenticated. The attack works because browsers automatically send cookies with requests to the target domain.

**Where we used it**: `CsrfFilter.java` implements the OWASP cookie-to-header pattern: the server sets a random token as a non-HTTP-only cookie, the Vue app reads it via `document.cookie` and sends it as a custom header, and the server validates that the header matches the session-stored token.

**Why it matters**: Without CSRF protection, an attacker could trick an authenticated user into creating resumes or sharing private data simply by visiting a malicious website. The cookie-to-header pattern stops this because the attacker's site can't read the cookie value (different origin) and can't set custom headers.

---

### Flyway Migrations as Database Version Control

**What it is**: Flyway treats SQL scripts like Git commits — each script has a version number, checksum, and runs exactly once. The `flyway_schema_history` table tracks which scripts ran and their checksums.

**Where we used it**: 7 SQL migration files (V1–V7) in `backend/src/main/resources/db/migration/`. V1 creates `role`, V7 seeds lookup data. Flyway runs automatically on startup via `@Bean(initMethod = "migrate")`.

**Why it matters**: Without migrations, developers run SQL scripts manually. Different developer machines drift apart. Nobody knows which schema version is in production. Flyway makes the database schema a versioned artifact like the rest of your code.

---

### Session Fixation Prevention

**What it is**: An attack where an attacker forces a victim to use a known session ID. If the server doesn't regenerate the session after login, the attacker can hijack the victim's authenticated session.

**Where we used it**: `AuthController.login()` calls `oldSession.invalidate()` then `request.getSession(true)` — creating a completely new session with a new ID after successful authentication.

**Why it matters**: Without this, logging in doesn't protect you. The old session ID (which might have been set by the attacker) becomes authenticated. Session regeneration is a one-liner that prevents an entire class of attacks.

---

## Architecture Overview

The feature follows a strict layered architecture with 6 layers total:

```
┌─────────────────────────────────────────────────────┐
│  Vue SPA (PrimeVue + Zod + vue-i18n)               │
│  AuthPage ↔ LoginForm / RegisterForm               │
│  useAuth composable ↔ authService.ts                │
├─────────────────────────────────────────────────────┤
│  Nginx (SPA serving + /api/* proxy)                │
├─────────────────────────────────────────────────────┤
│  Spring MVC Backend                                 │
│  Controller → Service → DAO → PostgreSQL            │
│  Interceptor (auth) + Filter (CSRF)                │
├─────────────────────────────────────────────────────┤
│  PostgreSQL 17 (Flyway migrations V1-V7)            │
└─────────────────────────────────────────────────────┘
```

Key architectural decisions:
- **Frontend owns UI rendering and client-side validation** (Zod schemas matching backend rules)
- **Backend owns all business logic, database access, and security** — never trust the frontend
- **Session-based auth** — no JWT, no OAuth, just HttpSession with JSESSIONID cookie
- **CSRF protected** — OWASP cookie-to-header pattern with SecureRandom tokens
- **Docker Compose** — 3 containers (Nginx + Tomcat + PostgreSQL) connected via internal network

---

## Glossary

| Term | Meaning |
|------|---------|
| **BCrypt** | A password hashing algorithm that is deliberately slow (configurable cost factor) to make brute-force attacks impractical |
| **CSRF** | Cross-Site Request Forgery — an attack that tricks authenticated users into making unintended requests |
| **Flyway** | A database migration tool that runs SQL scripts in order and tracks which ones have been executed |
| **HandlerInterceptor** | Spring MVC's mechanism for intercepting HTTP requests before they reach controllers — used here for session checks |
| **JAWS** (JSESSIONID) | The HTTP-only cookie that identifies a user's session — automatically managed by Tomcat |
| **OncePerRequestFilter** | A Spring filter that guarantees its logic runs exactly once per request, even if there are forwards or error dispatches |
| **Session fixation** | An attack where an attacker forces a known session ID on a victim before authentication |
| **Zod** | A TypeScript-first schema validation library that infers types from validation rules |
