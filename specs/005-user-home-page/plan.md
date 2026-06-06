# Implementation Plan: User Home Page & Resume Workspace

**Branch**: `feat/005-user-home-page` | **Date**: 2026-06-06 | **Status**: Approved | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/005-user-home-page/spec.md`

## Summary

Create a production-ready User Home (Resume Workspace) that:
1. Restructures SPA routing: root `/` → landing page (Thymeleaf), all SPA routes under `/app/...`
2. Provides a shared AppHeader with Home, My Profile, Generate Resume, Admin (role-based) navigation
3. Implements User Home page with guided next-step block (profile readiness-based), three summary cards, and a paginated Saved Resumes DataTable with search/filter/sort
4. Implements Resume Details modal with View PDF, Download PDF, Copy Link, Delete actions
5. Covers all states: loading, empty (with and without ready profile), error (inline, partial failure), data

Implementation splits into:
- **Backend**: `GET /api/user/home` summary endpoint, `GET /api/resumes` paginated listing, soft-delete endpoint, auth/role-checking interceptor updates, routing configuration for `/app/*` SPA proxy
- **Frontend**: `UserHomePage.vue` complete rewrite, `AppHeader.vue` creation, `GuidedNextStep.vue`, `SummaryCards.vue`, `SavedResumesTable.vue`, `ResumeDetailsDialog.vue`, `PlaceholderPage.vue`, router restructure, i18n full matrix, composables for data fetching

## Technical Context

**Language/Version**: Java 21, TypeScript (Vue 3 with Vite)

**Primary Dependencies**:
- Backend: Spring MVC 6, Plain JDBC, Flyway, PostgreSQL, SLF4J + Logback, Jackson, Lombok
- Frontend: Vue 3, vue-router 4, vue-i18n, PrimeVue 4, PrimeIcons

**Storage**: PostgreSQL (existing schema — may add new tables if saved_resumes not yet fully migrated)

**Testing**: JUnit 5 + Mockito + JaCoCo (backend), standalone MockMvc for controller tests, manual integration testing with Docker (per D14)

**Target Platform**: Linux server (Docker Compose: Tomcat 10 + Nginx + PostgreSQL)

**Project Type**: Web application (Java backend + Vue SPA frontend)

**Performance Goals**: 
- User Home page render < 3s (SC-003) from navigation to interactive
- Saved Resumes API pagination: SQL-level LIMIT/OFFSET with proper indexes
- Debounced search (300ms) with min 3 characters
- DataTable uses PrimeVue **lazy mode** — each page/sort/filter change triggers a backend API call

**Constraints**: 
- No Spring Boot, no JPA/Hibernate/ORM (per Constitution)
- All SQL via PreparedStatement
- i18n via en.json/ru.json for all visible strings (no hardcoded text)
- Admin route guard on BOTH frontend (vue-router) and backend
- Public resume links are backend-handled (no frontend exposure of PDF generation)
- Sort parameter MUST use whitelist validation + direction enum — never raw SQL interpolation (SEC-001)
- All resume queries MUST filter by authenticated user ID (SEC-002)
- Resume list queries MUST exclude soft-deleted records via `WHERE deleted_at IS NULL` (SEC-003)
- Nginx `/app/` location MUST use `autoindex off` and `try_files $uri /app/index.html` (SEC-004)
- No resume content (title, cover letter, company) MUST be logged at INFO level or above (SEC-005)

**Scale/Scope**: 
- Feature focus: User Home page, routing restructure, AppHeader, DataTable, modal, delete
- Placeholder pages for My Profile sections, Generate Resume steps, Admin page
- Desktop-first with responsive tablet/mobile adaptation

## Constitution Check

*GATE: Must pass before proceeding to task breakdown. Re-check if design changes.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | ✅ Pass | Backend: controller/service/dao/model/config/util layers. Frontend: components split by domain (home/, common/, layout/). No Spring Boot, JPA, Hibernate. Minimal new dependencies (PrimeVue already included). |
| **II. Testing Excellence** | ✅ Pass | JUnit 5 + Mockito for backend. Standalone MockMvc for UserHomeController (no DB dependency via mock services). JaCoCo coverage tracked. TDD for business logic (profile readiness calculation). Frontend: manual Docker integration testing (per D14). |
| **III. User Experience Consistency** | ✅ Pass | Full i18n via en.json/ru.json for all visible strings (FR-041). Empty states for all data views (FR-029). Skeleton loading states (FR-028). Error states with inline retry (FR-045, FR-046). 1280px max-width. Responsive mobile layout. Friendly `ты` tone for Russian. |
| **IV. Performance & Reliability** | ✅ Pass | PreparedStatement for all SQL queries. SQL-level pagination (LIMIT/OFFSET) for saved resumes (FR-027). Independent block loading (FR-046). UTF-8 encoding throughout. |
| **V. Security by Design** | ✅ Pass | Admin nav visible only to ADMIN role (FR-008). Backend enforces admin-only /app/admin (FR-009). No stack traces exposed. No token/technical metadata in User Home (FR-036). Logout clears session and redirects to /app/auth. |

## Project Structure

### Documentation (this feature)

```text
specs/005-user-home-page/
├── plan.md                    # This file
├── research.md                # Phase 0 — technology research
├── data-model.md              # Phase 1 — API contracts and entities
├── quickstart.md              # Phase 1 — developer quickstart
├── memory-synthesis.md        # Memory context for planning
├── contracts/                 # Phase 1 — API contracts
│   └── api-contracts.md
├── spec.md                    # Feature specification
├── checklists/
│   └── requirements.md        # Quality checklist
└── spec_input_files/          # Design prototype and briefs
```

### Source Code

```text
backend/
├── src/main/java/com/resumainer/
│   ├── config/
│   │   ├── WebConfig.java               # Add /app/* route handling + CORS
│   │   └── SecurityConfig.java          # Update auth filter for /app/* paths
│   ├── controller/
│   │   ├── UserHomeController.java      # NEW: GET /api/user/home
│   │   └── ResumeController.java        # NEW: GET /api/resumes, DELETE /api/resumes/{id}
│   ├── service/
│   │   ├── UserHomeService.java         # NEW: profile readiness + summary
│   │   └── ResumeService.java           # NEW: saved resume listing + delete
│   ├── dao/
│   │   └── ResumeDao.java               # NEW: resume CRUD with pagination
│   ├── model/
│   │   ├── UserHomeSummary.java         # NEW: response DTO
│   │   ├── SavedResume.java             # NEW: response DTO
│   │   └── PagedResponse.java           # NEW: generic paginated wrapper
│   └── util/
│       └── ProfileReadinessCalculator.java  # NEW: readiness logic
├── src/main/resources/db/migration/
│   ├── V005__create_saved_resumes.sql   # NEW: if table doesn't exist
│   └── V006__seed_resume_data.sql       # OPTIONAL: test data
└── src/test/java/com/resumainer/
    ├── controller/
    │   ├── UserHomeControllerTest.java  # NEW: standalone MockMvc
    │   └── ResumeControllerTest.java     # NEW: standalone MockMvc
    └── service/
        ├── UserHomeServiceTest.java     # NEW: TDD for readiness calc
        └── ResumeServiceTest.java       # NEW: TDD for pagination

frontend/
├── src/
│   ├── components/
│   │   ├── AppHeader.vue                # NEW: shared navigation header
│   │   ├── home/
│   │   │   ├── GuidedNextStep.vue       # NEW: profile readiness guidance block
│   │   │   ├── ProfileChecklist.vue     # NEW: checklist component
│   │   │   ├── SummaryCards.vue         # NEW: three summary cards
│   │   │   ├── SavedResumesTable.vue    # NEW: DataTable with search/filter/sort/pagination
│   │   │   └── ResumeDetailsDialog.vue  # NEW: modal with actions + cover letter
│   │   └── common/
│   │       └── PlaceholderPage.vue      # NEW: reusable placeholder
│   ├── views/
│   │   ├── UserHomePage.vue             # REWRITE: full workspace
│   │   ├── AuthPage.vue                 # KEEP: existing
│   │   └── AdminHomePage.vue            # KEEP: existing (will be moved to /app/admin)
│   ├── composables/
│   │   └── useUserHome.ts               # NEW: data fetching, state management
│   ├── services/
│   │   ├── userHomeService.ts           # NEW: /api/user/home API client
│   │   └── resumeService.ts             # NEW: /api/resumes API client
│   ├── i18n/
│   │   ├── en.json                      # UPDATE: add full home namespace
│   │   └── ru.json                      # UPDATE: add full home namespace
│   ├── router/
│   │   └── index.ts                     # REWRITE: /app/... prefix, new guards
│   └── App.vue                          # UPDATE: Toast + ConfirmDialog global
└── package.json                         # NO CHANGE (PrimeVue already included)
```

**Structure Decision**: Option 2 — Web application (backend + frontend). Frontend uses domain-based subdirectories under `components/`: `home/` for User Home components, `common/` for shared components.

## Complexity Tracking

No violations. All changes within standard patterns.

> **Note**: This feature is primarily frontend-heavy. The backend scope is limited to 2-3 new endpoints and a routing configuration change for `/app/*`.
