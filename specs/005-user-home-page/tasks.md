---
description: "Task breakdown for User Home Page & Resume Workspace feature"
---

# Tasks: User Home Page & Resume Workspace

**Input**: Design documents from specs/005-user-home-page/

**Prerequisites**: plan.md ?, spec.md ?, research.md ?, data-model.md ?, contracts/api-contracts.md ?

**Constitution Compliance**: Every task phase MUST reference the ResumAIner Constitution principles:
- **I** — Code Quality & Maintainability (layered architecture, SOLID, no Spring Boot/JPA)
- **II** — Testing Excellence (JUnit 5, Mockito, TDD for business logic, JaCoCo 50%+)
- **III** — User Experience (i18n, dual validation, PRG, error safety)
- **IV** — Performance & Reliability (PreparedStatement, JDBC transactions, pagination, UTF-8)
- **V** — Security by Design (backend auth, API key masking, XSS sanitization, no secrets in logs)

**Organization**: Tasks are grouped by foundational phase then user stories, ordered by dependency.

## Format: [ID] [P?] [TDD?] [SUBAGENT?] [Story] Description

- **[P]**: Parallel — can run concurrently with other [P] tasks
- **[TDD]**: Test-Driven — write failing test first, implement, verify pass
- **[SUBAGENT]**: Can be dispatched to a subagent
- **[REVIEW]**: Code review gate required before proceeding
- **[Story]**: Which user story this task belongs to (US1-US6)

---

## Phase 1: Setup (Project Infrastructure)

**Purpose**: Configure routing, security, and Nginx for /app/* SPA — blocks ALL user stories

**CRITICAL**: No user story work can begin until this phase is complete

- [x] T001 [SUBAGENT] Update WebConfig.java — add CORS headers for new /api/resumes and /api/user/home endpoints. (Constitution IV, V)
- [x] T002 [P] [REVIEW] Update AuthInterceptor — verify session validation covers /api/user/home and /api/resumes. (Constitution V)
- [x] T003 [P] [TDD] Create Flyway migration V005__create_saved_resumes.sql — define saved_resumes table. (Constitution I, IV)
- [x] T004 [P] [SUBAGENT] Update Nginx config — add /app/ location with autoindex off, try_files  /app/index.html (SEC-004). (Constitution IV, V)
- [x] T005 [P] [TDD] Create PagedResponse.java — generic paginated response DTO. (Constitution I)

**Checkpoint**: Backend foundation ready — new endpoints can be implemented

---

## Phase 2: Backend — ResumeService + ResumeDao (US1, US4, US5)

**Purpose**: Core resume data access and business logic that ALL user stories depend on

- [x] T006 [TDD] Create SavedResume.java — response DTO matching API contract. (Constitution I)
- [x] T007 [TDD] [SUBAGENT] Create ResumeDao.java — paginated SELECT with PreparedStatement, soft-delete UPDATE, owner filter (SEC-002), soft-delete exclusion (SEC-003). Tests on all methods. (Constitution I, II, IV, V)
- [x] T008 [TDD] Create ResumeService.java — listResumes with sort whitelist (SEC-001), deleteResume with owner check (SEC-002). Tests: invalid sort, other user delete, valid params. (Constitution I, II, V)
- [x] T009 [TDD] Create UserHomeSummary.java and ProfileReadinessCalculator.java — readiness formula: contactComplete && hasWorkExperience && hasEducation. (Constitution I, II)
- [x] T010 [TDD] Create UserHomeService.java — getHomeSummary queries UserDao + ResumeDao, composes UserHomeSummary. (Constitution I, II)

**Checkpoint**: Backend services and DAOs ready — controllers can be built

---

## Phase 3: Backend — Controllers (US1, US4, US5, US6)

**Purpose**: REST endpoints that the frontend consumes

- [x] T011 [TDD] Create UserHomeController.java — GET /api/user/home. Tests: 200 valid session, 401 no session. Standalone MockMvc. (Constitution II, V)
- [x] T012 [TDD] [SUBAGENT] Create ResumeController.java — GET /api/resumes, DELETE /api/resumes/{id}. Tests: valid list 200, other user delete 403, invalid size 400. (Constitution II, V)
- [x] T013 [REVIEW] Log safety check — no resume content logged at INFO+. Only operation, userId, HTTP status. (SEC-005, Constitution V)

**Checkpoint**: All 3 API endpoints functional — backend ready

---

## Phase 4: Frontend — Routing Restructure (US6 — Priority: P1)

**Goal**: Move all SPA routes under /app/... Landing at root / stays.

- [x] T014 Rewrite router/index.ts — routes under /app/...: /app/auth, /app/home, /app/admin, /app/profile/*, /app/generate/*. Guards: requiresAuth, requiresAdmin, requiresGuest. Remove old routes. (FR-001--FR-005, FR-009)
- [x] T015 Verify Thymeleaf landing at / still works. Run Docker, visit /. (FR-001)

**Checkpoint**: SPA routes under /app/... — login flow redirects to /app/home

---

## Phase 5: Frontend — i18n + AppHeader (US6 — Priority: P1)

**Goal**: Shared header with role-based nav. All text externalized.

- [x] T016 Enrich en.json and ru.json — add all namespaces from Implementation Brief i18n matrix. Russian uses friendly ty tone. (FR-041, Constitution III)
- [x] T017 Create AppHeader.vue — logo, nav (Home, My Profile, Generate Resume, Admin role-based FR-008), language switcher, logout with tooltip/aria-label. (Constitution III)
- [x] T018 Update App.vue — add Toast and ConfirmDialog globally. (FR-042)

**Checkpoint**: Navigation works — header renders on all SPA pages

---

## Phase 6: User Home Page — Core (US1 — Priority: P1)

**Goal**: Ready user with saved resumes sees guided block, 3 summary cards, functional DataTable.

- [x] T019 Create userHomeService.ts — fetchSummary(). (Constitution I)
- [x] T020 Create resumeService.ts — fetchResumes(params), deleteResume(id). (Constitution I)
- [x] T021 [SUBAGENT] Create useUserHome.ts composable — state: summary, resumes (current page), totalRecords, loading per block, error per block (FR-046), query params (page, size, sortField, sortOrder, search, language, adaptationLevel, createdDate). Methods: fetchAll (independent calls FR-046), fetchResumes (sends query params to resumeService, updates resumes+totalRecords), refresh, deleteResume (cascade refresh both blocks). Header immediate, content skeleton (FR-028). (FR-045, FR-046)
- [x] T022 Create ProfileChecklist.vue — 3 checklist items with Done/Missing. (FR-014)
- [x] T023 Create GuidedNextStep.vue — incomplete: guidance+checklist. Ready: Generate+Update cards. (FR-014, FR-015)
- [x] T024 Create SummaryCards.vue — saved resumes count, profile status, last resume. (FR-016, FR-017, FR-010)
- [x] T025 [SUBAGENT] Create SavedResumesTable.vue — PrimeVue DataTable in **lazy mode**. Props: `:lazy="true"`, `:totalRecords="totalRecords"`, `:loading="loading"`, `:first="first"`. Callbacks: `@page`, `@sort`, `@filter` emit params to parent for API call. 6 sortable columns with `removableSort`, default Created desc. Live search via InputText + 300ms debounce, min 3 chars — triggers backend search param. Filters: Language MultiSelect, Adaptation MultiSelect, DatePicker (exact date) — each change triggers backend filter. Paginator 10/20/50, currentPageReportTemplate. Skeleton for initial load, loading overlay for page/sort/filter changes. Context-aware empty states. `@row-click` emits `openResume`. CSS truncation with max-width %. Generate button in header if profileReady. (FR-018--FR-031)
- [x] T026 Rewrite UserHomePage.vue — orchestrate all components via useUserHome. Wire states.

**Checkpoint**: US1 functional — ready user sees full workspace

---

## Phase 7: Resume Details Modal (US4 — Priority: P2)

**Goal**: User can click a resume row or last resume card to see details with actions.

- [x] T027 Create ResumeDetailsDialog.vue — PrimeVue Dialog with details grid, public link+copy, cover letter accordion, action buttons (View, Download PDF, Copy Link, Delete). No technical metadata (FR-036). (FR-034--FR-038)
- [x] T028 Wire modal in UserHomePage.vue — row click + last resume card open modal. Delete via ConfirmDialog. Copy via Clipboard API + toast. (FR-034, FR-037, FR-039, FR-040)

**Checkpoint**: US4 functional — modal with all actions working

---

## Phase 8: Incomplete Profile — Guided States (US2 — Priority: P2)

**Goal**: Incomplete profile shows guidance. Generate navbar click shows blocking state.

- [x] T029 Implement incomplete state in GuidedNextStep.vue + UserHomePage.vue — no Generate CTA, no table header button (FR-031). (FR-014)
- [x] T030 Implement Generate Resume blocking state at /app/generate/vacancy when profile incomplete — checklist + Complete Profile CTA. (FR-033)
- [x] T031 [REVIEW] Verify FR-032 — Generate navbar always visible even when incomplete. (FR-032)

**Checkpoint**: US2 functional — incomplete profile guidance works

---

## Phase 9: Empty States + Ready-No-Resumes (US3 — Priority: P3)

**Goal**: Ready+no-resumes empty state. Delete flow works end-to-end.

- [x] T032 Implement empty state ready+no-resumes — "No resumes yet" with Generate CTA. (FR-029)
- [x] T033 Implement no-search-results empty state — "No resumes found" with filter suggestion. (FR-029)
- [x] T034 Wire delete flow: ConfirmDialog accept -> composable.deleteResume -> close modal + refresh table + refresh summary + toast. (FR-039, FR-040, US5)

**Checkpoint**: US3 + US5 functional — all states covered

---

## Phase 10: Placeholders + Polish

**Purpose**: Remaining pages, responsive polish, security verification, build validation.

- [x] T035 Create PlaceholderPage.vue — reusable placeholder with i18n. (FR-044)
- [x] T036 Create all profile and generate placeholder routes — stepper for generate, subnav for profile. (FR-043, FR-044)
- [x] T037 [P] AdminHomePage.vue at /app/admin — admin-only access. (FR-009, FR-044)
- [x] T038 Verify responsive: 1280px, cards wrap on tablet, stack on mobile, DataTable scroll. (Constitution III)
- [x] T039 [REVIEW] Security gate — sort whitelist, owner check, soft-delete exclusion, Nginx hardening, log safety.
- [x] T040 Run mvn clean package + npm run build — builds succeed.
- [x] T041 Manual Docker integration testing — test all states per D14.

**Checkpoint**: Feature complete — all user stories functional

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup (Phase 1): No dependencies — starts immediately
- Foundation (Phase 2-3): Depends on Setup — blocks ALL frontend stories
- Frontend Infrastructure (Phase 4-5): Depends on Setup
- User Stories (Phase 6-9): Depend on Phases 1-5
- Polish (Phase 10): Depends on all user stories

### Parallel Opportunities

- T002, T003, T004, T005 in Phase 1 can run in parallel
- T019, T020 (services) in Phase 6 can run together
- T025 (DataTable) and T021 (composable) — parallelize
- Phase 10: T035, T036, T037, T038 can run in parallel

### Within Each Phase

- TDD tasks: write failing test first, implement, verify pass
- Models before services, services before controllers, controllers before frontend
- Backend complete before frontend integration
- Phase complete before moving to next priority
