# Worklog

Use concise high-value entries only.
This is not a changelog. Do not record routine releases, version bumps, or implementation summaries.

## Template

---

---

---

---

---

---

---

### 2026-06-03 - Feature 003 Phase 5 Interceptor, CSRF Filter, and Configuration Completed

**Milestone**: Phase 5 (Cross-cutting Backend — Interceptor &amp; Configuration) of Feature 003 (Vue Auth Page) completed.

**What was achieved**:
- AuthInterceptor: HandlerInterceptor that checks HttpSession for "user" attribute on /api/** paths, returns 401 JSON for unauthenticated requests, excludes /api/auth/*
- CsrfFilter: OncePerRequestFilter implementing OWASP cookie-to-header pattern with SecureRandom 32-byte tokens, Base64 URL-safe encoding, non-HTTP-only XSRF-TOKEN cookie, skips validation for /api/auth/* and /api/public/**
- AppInitializer: CsrfFilter registered via getServletFilters() (per B6), session timeout set to 30 min
- WebConfig: 12 new @Bean methods (DataSource, 6 DAOs, PasswordService, AuthService, AuthInterceptor, AuthController) + AuthInterceptor registered with path patterns
- AuthExceptionHandler: @ControllerAdvice mapping ServiceException error codes to HTTP status (409/400/401/423/403/500)

**Key lessons captured**:
- B10: MockMvc standalone creates fresh session per perform() — use MockHttpSession for filter tests
- CsrfFilter in pure Spring MVC uses getServletFilters() (B6), not FilterRegistrationBean
- All @Beans must be explicit in WebConfig per B1/B5

**Evidence**
All 62 tests pass (BUILD SUCCESS). 7 new tests: AuthInterceptor (2), CsrfFilter (5).

**Next phase**: Phase 6 — Vue Router guards, authService.ts, useAuth composable (T039-T041).

### 2026-06-03 - Feature 003 Phase 4 Login with Rate Limiting Completed

**Milestone**: Phase 4 (User Story 2 — Login) of Feature 003 (Vue Auth Page) completed with TDD.

**What was achieved**:
- AuthService.authenticate(): email/password verification with BCrypt, account status check (ACTIVE/BLOCKED), lockout check, and rate limiting (5 failed attempts → 15 min lockout)
- AuthController.login(): POST /api/auth/login with session regeneration (SEC-002 — invalidate old, create new), role-based redirect (USER→/home, ADMIN→/admin), rememberMe support (7 day TTL)
- AuthController.logout(): POST /api/auth/logout with session invalidation
- AuthController.status(): GET /api/auth/status — returns authenticated flag, email, role
- No email enumeration: all auth failures return generic "Invalid email or password"
- Proper HTTP status codes: 200 OK, 401 Unauthorized, 423 Locked, 403 Forbidden

**Key lessons captured**:
- B9: Long auto-unboxing NPE when comparing null Long with primitive literal
- Session management in Spring MVC requires HttpServletRequest (not HttpSession) to call getSession(boolean)

**Evidence**
All 55 tests pass (BUILD SUCCESS). AuthService: 10 tests, AuthController: 7 tests.

**Next phase**: Phase 5 — AuthInterceptor, CsrfFilter, WebConfig bean registration, AppInitializer filter registration, exception handler.

### 2026-06-03 - Feature 003 Phase 3 Registration Service and Controller Completed

**Milestone**: Phase 3 (User Story 1 — Registration) of Feature 003 (Vue Auth Page) completed with TDD.

**What was achieved**:
- PasswordService: BCrypt hashing (cost=12) + password strength validation (9 TDD tests)
- AuthService: registration with email uniqueness check, JDBC transaction (User + ContactDetail atomic), ServiceException with error codes (4 TDD tests)
- AuthController: POST /api/auth/register with @Valid @RequestBody, session auto-login, proper HTTP status codes (409 for duplicate, 400 for invalid) (2 MockMvc tests)
- DAO connection-accepting overloads for transaction support (D10)
- i18n messages: 11 auth keys in EN + RU
- jayway-jsonpath dependency added for MockMvc jsonPath assertions (B8)
- ServiceException: custom exception with errorCode for i18n integration

**Key lessons captured**:
- B8: MockMvc jsonPath requires explicit jayway-jsonpath dependency
- D10: DAO connection-accepting overloads for JDBC transaction management
- Mockito mocks return default values for unstubbed methods (boolean -&gt; false) — always stub ALL method calls

**Evidence**
Commit pending. All 44 tests pass (BUILD SUCCESS). `mvn test` green.

**Next phase**: Phase 4 — Login with rate limiting (5 fails -&gt; 15 min lockout), session regeneration, rememberMe support, AuthController.login/logout/status.

### 2026-06-03 - Feature 003 Phase 2 Foundational Database and DAO Layer Completed

**Milestone**: Phase 2 (Foundational — Database &amp; Backend Infrastructure) of Feature 003 (Vue Auth Page) completed.

**What was achieved**:
- 7 Flyway migrations (V1–V7) with hybrid UUID/BIGSERIAL PK strategy
- 6 Model classes: User, Role, UserStatus, UserPermission, Language, ContactDetail
- 4 DTO classes: RegisterRequest (@NotBlank/@Email/@Size), LoginRequest (+rememberMe), AuthResponse, UserSession
- 6 DAO classes with PreparedStatement-only SQL: UserDao, RoleDao, UserStatusDao, UserPermissionDao, LanguageDao, ContactDetailDao
- 6 TDD test classes — 26 tests, all passing
- Educational documentation: Model/DTO/DAO architecture in dev-docs/learnings.md
- JDK 21 installed and configured (resolved Mockito + JDK version mismatch)

**Key lessons captured**:
- D9: JDK version must match project target (Java 21) — avoid Mockito agent failures
- TDD for DAO layer: mock DataSource/Connection/PreparedStatement/ResultSet pattern proven effective for all 6 DAOs
- Hybrid PK strategy (D7) implemented in Flyway migrations: UUID for entities, BIGSERIAL for lookups

**Evidence**
Commit d7b70c7 — 33 files, 2484 insertions. All 26 DAO tests pass. `mvn clean compile` succeeds.

**Next phase**: Phase 3 — PasswordService, AuthService (registration with transaction), AuthController, i18n messages.

### 2026-06-02 - Feature 003 Planning and Security Review Completed

**Milestone**: Feature 003 (Vue Auth Page) reaches Spec + Plan + Tasks + Security Review complete.

**What was achieved**:
- Specification: 6 user stories, 28 FR, 10 SC, 3 clarifications rounds
- Plan: 4 phases (Backend, Frontend, Docker, Integration) with hybrid UUID/BIGSERIAL strategy
- Tasks: 63 tasks with [TDD], [P], [SUBAGENT], [REVIEW] execution markers
- Diagrams: component diagram, system design, software architecture
- Security review: 11 findings (1 High, 5 Medium, 3 Low, 2 Info)
- Applied fixes: session regeneration on login (SEC-002), CSRF cookie-to-header filter (SEC-003)
- Phase 1 (Setup) implemented: pom.xml dependencies, Vue 3 + Vite scaffold, Docker Compose PostgreSQL 17, application.properties

**Next phase**: Phase 2 — Flyway migrations, Model/DTO classes, DAO layer.

### 2026-05-31 - Second Feature MVP Achieved: Thymeleaf Landing Page

**Status**
Active

**Milestone**
Feature 002-thymeleaf-landing-page reaches MVP.

**What was achieved**
Full Landing Page with 8 sections (Header, Hero, Problem, How It Works, Features, Trust & Control, FAQ, Final CTA), bilingual EN/RU i18n with browser auto-detection and ?lang= parameter switching, responsive layout, self-hosted fonts (Inter + Manrope, 7 TTF files), SVG brand assets (4 logos), custom bilingual error pages (404/500) with full branding, and MockMvc controller tests (3 tests, all pass). Migrated from JSP to Thymeleaf. All 28 tasks complete. BUILD SUCCESS.

**Why this is durable**
Establishes the Thymeleaf + i18n + responsive design pattern for all future Thymeleaf views. Documents the migration from JSP to Thymeleaf and the self-hosted font approach (SEC-002).

**Evidence**
Commit `f8ec657` — 22 files, 1288 insertions. Branch `feat/002-thymeleaf-landing-page`. Build: 3 tests pass, WAR created.

**Where to look next**
backend/src/main/resources/templates/landing.html, backend/src/main/webapp/static/css/landing.css, backend/src/main/resources/messages.properties

### 2026-05-30 - First Feature MVP Achieved: Hello World Tomcat

**Milestone**: Feature `001-hello-world-tomcat` reaches MVP.

**What was achieved**: Full end-to-end validation: `git clone → mvnw clean package → docker compose up → browser shows Hello World page with server time`. Spring MVC 6.2.11 + Jakarta EE 10 on Tomcat 10.1, deployed in Docker via multi-stage build (Maven → Tomcat, non-root user). Unit test (MockMvc, standalone setup) passing.

**Key lessons captured**:
- D1: Servlet initialization via AbstractAnnotationConfigDispatcherServletInitializer (no web.xml)
- D2: Maven Wrapper at same level as pom.xml
- D3: Docker Tomcat health check uses bash /dev/tcp (not nc)
- B1: @Controller must be registered as explicit @Bean or via @ComponentScan
- JaCoCo 0.8.13+ required for Java 21 class file support (0.8.12 fails)

**Evidence**
docker compose up → http://localhost:8080 → 200 OK with ResumAIner Hello World page.
mvnw clean package → BUILD SUCCESS with 1 passing test.

### YYYY-MM-DD - Summary

- why this is durable
- what future mistake it prevents
- evidence
- where future contributors should look

## Example

### 2026-03-15 - Pagination cursor must be opaque to clients

- **Why durable**: three features so far have tried to expose raw database offsets as pagination cursors, each time creating breaking changes when the underlying query changes
- **Future mistake prevented**: next time a feature adds pagination, the implementer will know to use opaque cursors from the start
- **Evidence**: specs 018, 024, and 031 all required pagination rework; see DECISIONS.md entry on API pagination
- **Where to look**: `src/api/pagination.ts`, `docs/memory/DECISIONS.md`

## Counter-Example (do not write entries like this)

> ### 2026-03-15 - Updated pagination
>
> - Changed pagination to use cursors
> - Deployed to staging

This is a changelog entry, not a durable lesson. It records what happened, not what was learned.
