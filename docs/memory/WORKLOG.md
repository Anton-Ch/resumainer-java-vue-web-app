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

---

---

---

---

---

---

---

### 2026-06-08 - Feature 006 bug fixes — CSRF, connection pool, favicon

**Status**
Active

**Milestone**
Three bugs found during manual integration testing of Feature 006 (User Profile Page) were fixed:

Bug 1 (CRITICAL) - CSRF token missing in profileService.ts:
- Created shared httpClient.ts with getCsrfToken() and apiRequest()
- Refactored profileService.ts to use the shared client for all 20 endpoints
- All POST/PUT/DELETE operations now send X-CSRF-Token header
- Verified: PUT /api/profile/contact returns 200 with X-CSRF-Token header matching cookie

Bug 2 (LOW) - Connection pool rollback warning:
- PooledConnectionProxy.handleClose() was calling rollback() even when autoCommit=true
- PostgreSQL throws: Cannot rollback when autoCommit is enabled
- Fixed: check physicalConnection.getAutoCommit() before rollback
- Verified: no rollback warnings in Docker logs after fix

Bug 3 (LOW) - favicon.svg 404:
- Copied favicon from backend static assets to frontend/public/
- Vite serves it at /favicon.svg via nginx
- Verified: no more 404 in browser console

Evidence:
- mvn test: 224/224, BUILD SUCCESS
- npm run build: 0 errors
- PUT /api/profile/contact: 200 with X-CSRF-Token header
- Docker logs: no rollback warnings
- Console errors: 0 (including favicon)

### 2026-06-08 - Feature 006 Profile Page DAO and Service/Controller Layers Completed

**Status**
Active

**Milestone**
Phases 3-4 of Feature 006 (User Profile Page) completed with TDD.

Phase 3 (DAO layer):
- 6 new DAOs: WorkExperienceDao, EducationDao, ProjectDao, CourseCertificateDao (pagination), AdditionalProfileInfoDao (upsert), WorkFormatDao
- All with connection-accepting overloads (D10), owner-scoped WHERE user_id=? (SEC-001), soft-delete filter is_deleted=FALSE (SEC-003)
- 52 new DAO tests (Mockito)
- Extended ContactDetailDao with UPDATE + new BA fields
- Added V16 migration for missing contact_detail columns

Phase 4 (Service + Controller):
- ProfileService: business logic for all 6 sections, manual JDBC transaction for AdditionalInfo save (username + additional_info + work_formats atomic)
- ProfileController: 15 REST endpoints under /api/profile/*, Cache-Control: no-store private (SEC-005)
- 39 new tests (21 service + 18 controller), standalone MockMvc
- Total: 224/224 tests passing

**Evidence**
mvn test: 224/224, BUILD SUCCESS. All endpoints tested via standalone MockMvc.

### 2026-06-06 — Feature 005 Implementation — Backend and Frontend Core Completed

**Status**: Active

**Why this is durable**: Feature 005 (User Home Page &amp; Resume Workspace) implementation reached functional completeness across all layers:

**Backend** (Phases 1-3):
- Flyway migration V8 (saved_resumes table with soft-delete support)
- ResumeDao with PreparedStatement, sort whitelist, owner filter, pagination
- ResumeService with validation (page/size/sort params)
- UserHomeService composing profile readiness + resume summary
- UserHomeController (GET /api/user/home) + ResumeController (GET/DELETE /api/resumes)
- 130+ backend tests passing (JUnit 5 + Mockito + standalone MockMvc)

**Frontend** (Phases 4-9):
- SPA routing restructured from / to /app/... with role-based guards
- AppHeader with navigation (Home, Profile, Generate, Admin role-based)
- Full i18n EN/RU matrix (home, resumeDetails, deleteResume, generate, placeholders)
- UserHomePage with guided block, summary cards, lazy DataTable
- PrimeVue DataTable in lazy mode with server-side search/filter/sort/pagination
- ResumeDetailsDialog with View/Download/Copy Link/Delete actions
- Placeholder pages for 6 profile + 4 generate routes

### 2026-06-06 — Feature 005 Planning and All Artifacts Completed

**Status**: Active

**Why this is durable**: Feature 005 (User Home Page &amp; Resume Workspace) completed full planning cycle: spec → clarification → brainstorming → plan → research → data model → contracts → security review → component diagram → tasks. All 46 FRs, 11 SCs, 41 tasks, and 5 security findings documented. Tasks include [TDD], [SUBAGENT], and [REVIEW] execution markers. Routing restructure under /app/... was the key architectural decision.

### 2026-06-04 - Feature 004 Custom JDBC Connection Pool Implementation Completed

**Milestone**: Feature 004 (Custom JDBC Connection Pool) phases 1 and 2 completed.

**What was achieved:**
- 5 pool classes implemented with TDD: ConnectionPoolConfig, ConnectionFactory, ConnectionPoolException, PooledConnectionProxy (Java dynamic proxy), SimpleConnectionPool (ArrayBlockingQueue + AtomicInteger + AtomicBoolean)
- DataSourceConfig — Spring @Bean with init/destroy lifecycle
- Replaced DriverManagerDataSource with custom SimpleConnectionPool
- Switched to @ComponentScan("com.resumainer") — eliminated 15+ explicit @Bean methods
- 97/97 tests passing (23 new pool tests)
- All pool classes implement DataSource interface for future HikariCP replacement
- B5 (stereotype annotations) superseded by @ComponentScan resolution

**Key decisions:**
- @ComponentScan("com.resumainer") for bean discovery instead of explicit @Bean methods
- @Repository/@Service annotations added to all DAOs and services
- Controller constructor injection for @Value properties to enable standalone MockMvc tests
- JDBC URL configured via System.getenv() (B2 guard), not ${...} placeholders

### 2026-06-03 - Feature 003 Bug-Fix and Integration Testing Completed

**Milestone**: Feature 003 (Vue Auth Page) — all bugs found during manual integration testing fixed and verified.

**Bugs found and fixed**:
- B11: Flyway @Bean(initMethod="migrate") required in pure Spring MVC — migrations never ran, tables missing
- B12: DataSource URL with ${...} placeholders not resolved — used System.getenv() instead
- B13: PrimeVue 4 Zod resolver messages not reactive to locale changes — added watch(locale) + ref(resolver)
- D13: Hardcoded English strings in AuthPage.vue info panel and subtitle — moved to i18n $t()
- D13: Hardcoded Zod validation messages in LoginForm/RegisterForm — moved to i18n $t()
- Duplicate toggle text ("Don't have an account? Register now! Register") — fixed i18n key design
- Wrong brand logo (generic SVG instead of ResumAIner assets) — replaced with proper SVG from spec_input_files

**Key lessons captured**:
- B11, B12, B13: three new bug patterns for pure Spring MVC + PrimeVue 4
- D13: all user-facing strings must use i18n $t() — no exceptions
- D14: mandatory integration testing phase after all implementation — Docker rebuild, browser test, i18n check, edge cases
- Playwright MCP is effective for automated frontend testing and i18n verification

**Verification**
- Playwright: Registration flow tested end-to-end (Vue → API → PostgreSQL) — 200 OK, redirect to /home
- Playwright: Login flow tested — 200 OK, redirect to /home
- Playwright: i18n verified in EN and RU — all labels, validation messages, info text translated correctly
- Docker Compose: all 3 containers healthy (db + app + frontend)
- Backend tests: 74/74 pass

### 2026-06-03 - Feature 003 Phase 8 Bilingual Auth Forms with PrimeVue + Zod Completed

**Milestone**: Phase 8 (User Story 5 — Bilingual Auth Forms with PrimeVue + Zod) of Feature 003 (Vue Auth Page) completed.

**What was achieved**:
- LoginForm.vue: PrimeVue 4 Form with Zod resolver, email (InputText + @Email), password (Password + toggleMask), rememberMe (Checkbox), inline Message errors, @success emit
- RegisterForm.vue: PrimeVue 4 Form with Zod resolver, email (InputText), password (Password + feedback/strength meter), confirmPassword (Password + .refine() match), inline Message errors
- AuthPage.vue refactored: inline forms replaced with LoginForm + RegisterForm components, ~310 lines removed, form-slide transition preserved
- Zod schemas with bilingual error messages match backend validation rules

**Key lessons captured**:
- D12: PrimeVue 4 Form with Zod resolver standard validation pattern
- PrimeVue 4 components imported individually (tree-shaking)
- Zod .refine() for cross-field validation (password match)

**Evidence**
Commit 61713fa. All frontend builds pass (vue-tsc + vite, 2.60s).

**Next phase**: Phase 9 — Docker &amp; Integration (Dockerfiles, docker-compose, integration tests).

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

### 2026-06-12 - Feature 007 Specification, Planning, and Implementation Complete

**Status**: Active

**Milestone**: Feature 007 (Resume Generation) reached Tasks + Implementation phase complete.

**What was delivered**:
- Full spec with 45 FRs, 11 SCs, 7 user stories
- 15 implementation phases (0-14), 150+ tasks
- 8 Flyway migrations (V17-V24) + education migration (V25)
- Backend: 40+ new Java files (models, DTOs, DAOs, services, AI client, parser, persistence, renderer, finalize)
- Frontend: 14 new Vue components (4 wizard pages, 10 shared components) + service/composable layer
- 3 diagram files (component, system design, software architecture)
- Security review with 5 findings (overall LOW risk)

**Technical decisions**:
- All DAOs use connection-accepting overloads per D10
- AI client uses Factory Method pattern; prompt uses Builder pattern
- PDF conversion deferred to feat/008-pdf-conversion
- HTML is canonical generated artifact in feat/007

---

## Counter-Example (do not write entries like this)

> ### 2026-03-15 - Updated pagination
>
> - Changed pagination to use cursors
> - Deployed to staging

This is a changelog entry, not a durable lesson. It records what happened, not what was learned.
