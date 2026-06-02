# Implementation Plan: Vue Auth Page

**Branch**: `feat/003-vue-auth-page` | **Date**: 2026-06-02 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/003-vue-auth-page/spec.md`

## Summary

Implement a complete authentication system: Registration and Login in Vue 3 SPA, with Spring MVC REST API backend, session-based auth via HandlerInterceptor, Flyway-managed PostgreSQL schema (UUID v4 via built-in `gen_random_uuid()` for entity tables, BIGSERIAL for lookup tables), and Docker Compose with PostgreSQL container. After successful auth, users land on placeholder User Home (regular) or Admin Home (admin) pages.

**Input design files used:**
- `spec_input_files/login-page-brief.md` — auth page UX requirements
- `spec_input_files/auth-home-pages-brief.md` — placeholder home pages spec
- `spec_input_files/auth-home-pages-decisions.md` — design decisions (D-A1 to D-A15)
- `spec_input_files/light_design_dna.md` — Light Enterprise SaaS visual style
- `spec_input_files/REDESIGN_NOTES.md` — redesign direction notes
- `spec_input_files/auth_page_reference.html/css/js` — reference CodePen for auth page layout
- `spec_input_files/draft_design/` — HTML mockups for auth, user-home, admin-home
- `spec_input_files/draft_design/vue_design_dna.md` — Vue-specific design decisions

**Key design decisions from input files:**
- D-A1: Single auth page with Login/Register toggle (link-switching), not separate pages
- D-A5: Add "Remember me" checkbox below password field
- D-A6: Password visibility toggle (eye icon) on password fields
- D-A12: Staggered slide animation for form switching (Login ↔ Register)
- D-A13: PrimeVue Form + Zod resolver for validation
- Light Enterprise SaaS visual palette (emerald `#0F9D7A`, blue `#2F6BFF`, canvas `#F6F7FB`)

## Technical Context

**Language/Version**: Java 21 LTS

**Primary Dependencies**:
- Backend: Spring MVC 6.2.x, Jakarta EE 10, Jakarta Bean Validation, SLF4J + Logback, Flyway 10.x, PostgreSQL JDBC driver
- Frontend: Vue 3 (Composition API), Vite, PrimeVue 4, Zod, @primevue/forms (zodResolver), Vue Router 4, i18next or vue-i18n
- Database: PostgreSQL 17

**Storage**: PostgreSQL 17, Flyway-versioned migrations. Entity PKs (`users`, `contact_detail`) use PostgreSQL built-in `gen_random_uuid()` (UUID v4). Lookup tables (`role`, `user_status`, `user_permission`, `language`) use `BIGSERIAL` — no custom UUID generators needed.

**Testing**:
- Backend: JUnit 5 + Mockito + JaCoCo. MockMvc for controller tests. Testcontainers or H2 for DAO integration tests.
- Frontend: Vitest + Vue Test Utils for component tests (optional for MVP).

**Target Platform**: Linux (Docker), Tomcat 10.1 (Jakarta EE 10), PostgreSQL 17 container.

**Project Type**: Web application (Java backend + Vue SPA frontend).

**Performance Goals**: Login/register API response < 500ms (BCrypt hashing excluded). Page load < 2s on broadband.

**Constraints**:
- No Spring Boot, no ORM/JPA/Hibernate — plain JDBC with custom thread-safe Connection Pool.
- Spring MVC HandlerInterceptor for auth, NOT Spring Security.
- Entity PKs use PostgreSQL built-in `gen_random_uuid()` (UUID v4). Lookup tables use `BIGSERIAL`. No custom UUID generators, no ORM.
- PrimeVue 4 components for all UI elements.

**Scale/Scope**: Single-user / small-team Capstone project. Max 100 users in MVP.

## Constitution Check

*GATE: Must pass before proceeding to task breakdown. Re-check if design changes.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | ✅ Pass | Layered architecture: AuthController → AuthService → AuthDao. No JPA/Hibernate/Spring Boot. No custom UUID generators — built-in PostgreSQL `gen_random_uuid()`. @Bean registration in WebConfig for all classes. |
| **II. Testing Excellence** | ✅ Pass | JUnit 5 + Mockito. TDD for auth service. 50%+ coverage target. MockMvc for controller tests. Mock AI provider not applicable. BCrypt tests for password hashing. |
| **III. User Experience Consistency** | ✅ Pass | i18n via messages_en/messages_ru for auth pages. PrimeVue + Zod dual validation. PRG pattern (button disabled + spinner). Error safety (generic auth errors). Light Enterprise SaaS styling. |
| **IV. Performance & Reliability** | ✅ Pass | PreparedStatement for all auth DAO queries. JDBC transaction (register: create user + profile in one tx). UTF-8 encoding. Session timeout (30 min inactivity). |
| **V. Security by Design** | ✅ Pass | BCrypt for passwords (never plaintext). No stack traces exposed. No secrets in logs/builds. Generic "Invalid email or password" (no email enumeration). HTTP-only cookies. Rate limiting (5 fails → 15 min lockout). Logged auth events. |

## Research

### UUID Strategy

- **Decision**: Use PostgreSQL built-in `gen_random_uuid()` (UUID v4) for entity table PKs (`users`, `contact_detail`). Use `BIGSERIAL` for lookup tables (`role`, `user_status`, `user_permission`, `language`). No custom UUID generators or extensions.
- **Rationale**: Built-in function, zero setup, no custom code to maintain. UUID v4 provides protection against ID enumeration for entity tables exposed via URLs. BIGSERIAL on lookup tables is more efficient (2-6 rows, never exposed externally).
- **Alternatives considered**: UUID v7 (requires custom Java generator or PL/pgSQL function — extra complexity, rejected per user decision), auto-increment everywhere (enumeration vulnerability on exposed entities).

### PrimeVue 4 Forms Decision

- **Decision**: Use `Form` component with `zodResolver` from `@primevue/forms/resolvers/zod`. Password with `toggleMask` for visibility toggle.
- **Rationale**: PrimeVue 4's recommended approach. Zod provides type-safe schema validation. Works with Composition API. Password component has built-in `toggleMask` (eye icon) — no custom implementation needed.
- **Best practice**: Use `FormField` for field-level validation state. Error messages via `Message` component with `severity="error" size="small" variant="simple"`.

### Spring MVC Auth Decision

- **Decision**: Session-based auth via `HandlerInterceptor` registered through `WebMvcConfigurer.addInterceptors()`. AuthController with `@PostMapping("/api/auth/login")`, `@PostMapping("/api/auth/register")`, `@PostMapping("/api/auth/logout")`, `@GetMapping("/api/auth/status")`.
- **Rationale**: Spring docs note interceptors are "not recommended as primary security layer", but for a Capstone project without sensitive financial data, session-based auth with server-side HttpSession is appropriate and aligns with Spring MVC patterns. Using `@SessionAttribute` for user context.
- **Registration transaction**: User creation + contact_detail creation in single JDBC transaction.

### Flyway Naming Convention

- **Decision**: Standard versioned migrations: `V1__create_role_table.sql`, `V2__create_user_status_table.sql`, `V3__create_user_permission_table.sql`, `V4__create_language_table.sql`, `V5__create_users_table.sql`, `V6__create_contact_detail_table.sql`, `V7__seed_lookup_data.sql`.
- **Rationale**: Flyway default naming convention. Sequential versions for readability. Seed data in a separate migration for clarity.

### Database Schema Decision

- **Decision**: Hybrid PK strategy based on table role:
  - Entity tables (`users`, `contact_detail`): UUID PK via `gen_random_uuid()` DEFAULT. FKs between entities also use UUID type.
  - Lookup tables (`role`, `user_status`, `user_permission`, `language`): `BIGSERIAL` PK. FKs from `users` to these tables use UUID (the FK column matches the parent PK type).
- **Note**: The existing data dictionary (from BA docs) defines Integer PKs. This plan supersedes that for entity tables. Future features should follow the same hybrid strategy.
- **Migration strategy**: Since this is the FIRST database migration (no prior tables exist), no data migration needed.

## Project Structure

### Documentation (this feature)

```text
specs/003-vue-auth-page/
├── plan.md                  # This file
├── research.md              # (to be generated in Phase 0)
├── data-model.md            # (to be generated in Phase 1)
├── quickstart.md            # (to be generated in Phase 1)
├── contracts/               # (to be generated in Phase 1)
│   └── api-contracts.md     # REST API contracts for auth endpoints
├── spec.md                  # Feature specification
├── spec_input_files/        # Design reference files
├── checklists/
│   └── requirements.md      # Quality checklist
└── memory-synthesis.md      # Project memory synthesis
```

### Source Code (repository root)

```text
backend/
├── pom.xml
├── mvnw / mvnw.cmd / .mvn/
├── src/main/java/com/resumainer/
│   ├── initializer/
│   │   └── AppInitializer.java
│   ├── config/
│   │   └── WebConfig.java                  # + @Bean for AuthController, AuthService, AuthDao, AuthInterceptor
│   ├── controller/
│   │   └── AuthController.java              # POST /api/auth/register, login, logout; GET /api/auth/status
│   ├── service/
│   │   ├── AuthService.java                 # register, authenticate, logout, checkAuth
│   │   └── PasswordService.java             # BCrypt hash + verify, password strength check
│   ├── dao/
│   │   ├── UserDao.java                    # CRUD for users table
│   │   ├── RoleDao.java                    # Read for role lookup
│   │   ├── UserStatusDao.java              # Read for user_status lookup
│   │   ├── UserPermissionDao.java          # Read for user_permission lookup
│   │   ├── LanguageDao.java                # Read for language lookup
│   │   └── ContactDetailDao.java           # Create for initial profile on registration
│   ├── model/
│   │   ├── User.java                        # UUID id, email, password_hash, role_id, etc.
│   │   ├── Role.java                        # UUID id, code, name
│   │   ├── UserStatus.java                  # UUID id, code, name
│   │   ├── UserPermission.java              # UUID id, code, name
│   │   ├── Language.java                    # UUID id, code, name
│   │   └── ContactDetail.java               # UUID id, user_id, full_name, etc.
│   ├── interceptor/
│   │   └── AuthInterceptor.java             # HandlerInterceptor — checks session, redirects if unauthenticated
│   ├── dto/
│   │   ├── RegisterRequest.java             # email, password, passwordConfirmation
│   │   ├── LoginRequest.java                # email, password, rememberMe
│   │   ├── AuthResponse.java                # success, role, message, redirectUrl
│   │   └── UserSession.java                 # userId, email, role
│   └── util/
│       └── UuidV7Generator.java             # UUID v7 generation utility
├── src/main/resources/
│   ├── messages_en.properties               # i18n auth messages (English)
│   ├── messages_ru.properties               # i18n auth messages (Russian)
│   ├── application.properties               # DB config, profile
│   └── db/migration/
│       ├── V1__create_role_table.sql
│       ├── V2__create_user_status_table.sql
│       ├── V3__create_user_permission_table.sql
│       ├── V4__create_language_table.sql
│       ├── V5__create_users_table.sql
│       ├── V6__create_contact_detail_table.sql
│       └── V7__seed_lookup_data.sql
└── src/test/java/com/resumainer/
    ├── controller/
    │   └── AuthControllerTest.java
    ├── service/
    │   ├── AuthServiceTest.java
    │   └── PasswordServiceTest.java
    └── dao/
        └── UserDaoTest.java

frontend/
├── package.json
├── vite.config.ts
├── src/
│   ├── App.vue
│   ├── main.ts
│   ├── router/
│   │   └── index.ts                          # Vue Router: /login, /register → AuthPage; / → UserHome; /admin → AdminHome
│   ├── views/
│   │   ├── AuthPage.vue                      # Login/Register toggle page (single component)
│   │   ├── UserHomePage.vue                  # Placeholder: title, stats, navigation buttons, empty table
│   │   └── AdminHomePage.vue                 # Placeholder: title, stats, navigation cards
│   ├── components/
│   │   ├── LoginForm.vue                     # PrimeVue Form + Zod: email, password, rememberMe, login button
│   │   ├── RegisterForm.vue                  # PrimeVue Form + Zod: email, password, confirmPassword, register button
│   │   ├── LanguageSwitcher.vue              # EN/RU toggle
│   │   └── AppHeader.vue                     # Logo, LanguageSwitcher, Logout button
│   ├── services/
│   │   └── authService.ts                    # API calls: register, login, logout, checkAuthStatus
│   ├── composables/
│   │   └── useAuth.ts                        # Auth state management (session check, login/logout actions)
│   ├── i18n/
│   │   ├── en.json                           # English auth strings
│   │   └── ru.json                           # Russian auth strings
│   └── assets/
│       └── styles/
│           └── auth.css                      # Light Enterprise SaaS auth styles
└── public/
    └── logo.svg

docker/
├── docker-compose.yml                        # PostgreSQL service + backend + frontend
├── Dockerfile                                # Backend multi-stage build (Maven → Tomcat)
└── scripts/
    └── wait-for-it.sh                        # Health check for PostgreSQL
```

## Implementation Phases

### Phase 0: Research & Setup (estimated: 0.5-1 day)

1. **Database research**: Confirm PostgreSQL UUID v7 approach (PL/pgSQL function vs Java-side generation vs extension)
2. **Docker setup**: Add PostgreSQL 17 service to docker-compose.yml. Add health check for db readiness.
3. **Maven dependencies**: Add Flyway, BCrypt, Jakarta Bean Validation API, Jackson, Servlet API, PostgreSQL JDBC driver to pom.xml
4. **Frontend scaffolding**: Create Vue 3 + Vite project in `frontend/`. Add PrimeVue 4, Zod, vue-router, i18n library.
5. **Create research.md**: Document all decisions with rationale.

**Output**: `research.md`, updated `docker-compose.yml`, `backend/pom.xml`, `frontend/package.json`

### Phase 1: Backend Core (estimated: 1.5-2 days)

**Step 1: Flyway Migrations (DB schema)**
- Create `V1`–`V7` migration scripts
- Entity PKs (`users`, `contact_detail`): `UUID DEFAULT gen_random_uuid()`
- Lookup PKs (`role`, `user_status`, `user_permission`, `language`): `BIGSERIAL`
- Tables: `role`, `user_status`, `user_permission`, `language`, `users`, `contact_detail`
- Seed data for lookup tables (ROLE: USER/ADMIN; STATUS: ACTIVE/BLOCKED; PERMISSION: ALLOWED/FORBIDDEN; LANGUAGE: EN/RU)

**Step 2: Model & DTO classes**
- Create model classes (`User.java`, `Role.java`, etc.) with UUID fields
- Create DTOs (`RegisterRequest`, `LoginRequest`, `AuthResponse`, `UserSession`)

**Step 3: DAO layer**
- `UserDao`: create (register), findByEmail, findById, updateLoginAttempts, resetLoginAttempts
- `RoleDao`: findByCode (USER/ADMIN)
- `UserStatusDao`, `UserPermissionDao`, `LanguageDao`: findById, findByCode
- `ContactDetailDao`: create (initial empty profile)

**Step 4: Service layer**
- `PasswordService`: BCrypt hash, verify, strength check
- `AuthService`: register (transactional: user + profile), authenticate, logout, checkAuthStatus, rate limiting logic

**Step 5: Controller layer**
- `AuthController`: POST /api/auth/register, POST /api/auth/login, POST /api/auth/logout, GET /api/auth/status
- Validation via Jakarta Validation (`@Valid` on request DTOs)

**Step 6: HandlerInterceptor**
- `AuthInterceptor`: preHandle — check HttpSession for user attribute; redirect to login if missing and path is protected

**Step 7: WebConfig updates**
- Register `@Bean` for `AuthController`, `AuthService`, `AuthDao` (and all DAOs), `PasswordService`, `AuthInterceptor`
- Register interceptor via `addInterceptors()` with path matchers

**Test coverage**: JUnit 5 + Mockito for service layer. MockMvc for controller. DAO tests with H2 or Testcontainers.

**Output**: All backend Java code, Flyway migrations, unit tests

### Phase 2: Frontend Auth Pages (estimated: 1.5-2 days)

**Step 1: Vue Router**
- Routes: `/` (Landing Page redirect), `/login` (AuthPage login mode), `/register` (AuthPage register mode, or toggle), `/home` (UserHomePage, protected), `/admin` (AdminHomePage, protected)
- Route guards: redirect to `/login` if not authenticated; redirect to `/home` if already authenticated and visiting `/login`

**Step 2: Auth Service (TypeScript)**
- `authService.ts`: fetch calls to `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`, `/api/auth/status`

**Step 3: useAuth composable**
- Reactive auth state: `isAuthenticated`, `user`, `role`
- `checkAuth()` on app mount, `login()`, `register()`, `logout()`
- Store session indicator (HTTP-only cookie, JS only checks status endpoint)

**Step 4: AuthPage component**
- Single page with Login/Register toggle
- Left panel: branded info sidebar (logo, product description, benefits)
- Right panel: form card with animated switch
- Staggered slide animation (D-A12) for form switching
- Language switcher in header

**Step 5: LoginForm component**
- PrimeVue Form with Zod resolver
- Fields: email (InputText), password (Password with toggleMask), rememberMe (Checkbox)
- Submit: disable button + spinner (PRG), POST to `/api/auth/login`
- Error display: inline validation + generic auth error banner
- Link to Register

**Step 6: RegisterForm component**
- PrimeVue Form with Zod resolver
- Fields: email (InputText), password (Password with toggleMask + strength meter), confirmPassword (Password)
- Submit: disable button + spinner, POST to `/api/auth/register`
- Error display: inline validation + duplicate email error
- Link to Login

**Step 7: Placeholder Pages**
- `UserHomePage.vue`: title "User Home", stats summary placeholders (0 values), "Edit my profile" button, "Generate new resume" button, empty resume table with guidance text, logout button
- `AdminHomePage.vue`: title "Admin Home", stats summary placeholders (0 values), navigation cards to Users/Resumes/AI Models, logout button

**Step 8: LanguageSwitcher + AppHeader**
- LanguageSwitcher: EN/RU toggle, persists preference (localStorage + cookie for backend)
- AppHeader: logo, language switcher, logout (visible only when authenticated)

**Step 9: Styling**
- Light Enterprise SaaS design system (from `light_design_dna.md`)
- PrimeVue theme customization (emerald primary, canvas background)
- Responsive: centered card on desktop, full-width on mobile

**Output**: All Vue components, router config, i18n files, styles

### Phase 3: Docker & Integration (estimated: 0.5-1 day)

**Step 1: Docker Compose**
- `docker-compose.yml` with:
  - `db`: PostgreSQL 17 container (port 5432, volume for data persistence, health check)
  - `backend`: Tomcat container built from Dockerfile, depends_on db, env vars for DB connection
  - `frontend`: Nginx container serving built Vue SPA, depends_on backend, proxy_pass for `/api/`

**Step 2: Backend Dockerfile**
- Multi-stage build: Maven build → Tomcat 10.1 deployment
- WAR file copied to webapps/ROOT.war

**Step 3: Frontend Dockerfile**
- Multi-stage: Node build → Nginx static serve
- Vite build output to Nginx html directory

**Step 4: Environment configuration**
- `application.properties` with dev profile (localhost DB, debug logging)
- `application-prod.properties` with prod profile (container DB, normal logging)
- `.env` file template for DB credentials (not committed)

**Step 5: Integration testing**
- `docker compose up` from clean checkout
- Verify: DB starts, migrations run, backend deploys, frontend serves
- Manual: open browser → landing page → register → login → home page

**Output**: Docker configuration files, verified end-to-end flow

## Complexity Tracking

> All Constitution checks pass — no complexity violations to justify.

## Tasks

Detailed task breakdown will be generated by `/speckit.tasks` command. See `tasks.md` after execution.

Summary of task groups:
1. **DB**: Flyway migrations (V1–V7) with hybrid PK strategy (UUID v4 + BIGSERIAL)
2. **Backend**: Model → DAO → Service → Controller → Interceptor → WebConfig
3. **Frontend**: Vue scaffold → Router → Auth Service → Forms → Placeholder pages → i18n → Styling
4. **Docker**: PostgreSQL service → Backend container → Frontend container → Integration
5. **Tests**: Service unit tests → Controller MockMvc → DAO integration → Frontend component tests
