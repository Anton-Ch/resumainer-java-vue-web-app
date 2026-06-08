---
description: "Task breakdown for User Profile Page feature"
---

# Tasks: User Profile Page

**Input**: Design documents from specs/006-user-profile/

**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/api.md ✅

**Constitution Compliance**: Every task phase MUST reference the ResumAIner Constitution principles:
- **I** — Code Quality & Maintainability (layered architecture, SOLID, no Spring Boot/JPA)
- **II** — Testing Excellence (JUnit 5, Mockito, TDD for business logic, JaCoCo 50%+)
- **III** — User Experience (i18n, dual validation, error safety)
- **IV** — Performance & Reliability (PreparedStatement, JDBC transactions, pagination, UTF-8)
- **V** — Security by Design (owner-scoped access, PII-safe logging, soft-delete, cache-control)

**Organization**: Tasks are grouped by foundational phase then user stories, ordered by dependency.

## Format: `[ID] [P?] [TDD?] [SUBAGENT?] [REVIEW?] [Story] Description`

- **[P]**: Parallel — can run concurrently with other [P] tasks
- **[TDD]**: Test-Driven — write failing test first, implement, verify pass
- **[SUBAGENT]**: Can be dispatched to a subagent
- **[REVIEW]**: Code review gate required before proceeding

---

## Phase 1: Setup — Database Migrations

**Purpose**: Create all new profile entity tables via Flyway migrations — BLOCKS all other phases

**⚠️ CRITICAL**: No backend or frontend work can begin until this phase is complete

- [x] T001 [TDD] [SUBAGENT] Create Flyway migration V9__create_work_experience_table.sql — work_experience table with is_deleted + deleted_at (SEC-003). (Constitution I, IV, V)
- [x] T002 [TDD] [P] Create Flyway migration V10__create_education_table.sql — education table with is_deleted + deleted_at. (Constitution I, IV, V)
- [x] T003 [TDD] [P] Create Flyway migration V11__create_project_table.sql — project table with is_deleted + deleted_at. (Constitution I, IV, V)
- [x] T004 [TDD] [P] Create Flyway migration V12__create_course_certificate_table.sql — course_certificate table with is_deleted + deleted_at. (Constitution I, IV, V)
- [x] T005 [TDD] [P] Create Flyway migration V13__create_additional_profile_info_table.sql — additional_profile_info table (1:1 with users). (Constitution I, IV)
- [x] T006 [TDD] [P] Create Flyway migration V14__create_work_format_tables.sql — work_format + user_work_format tables. (Constitution I, IV)
- [x] T007 [TDD] [P] Create Flyway migration V15__seed_work_format_data.sql — seed 8 work format values from BA data dictionary. (Constitution I)

**Checkpoint**: All profile tables created in PostgreSQL — migrations verified with `mvn flyway:migrate`

---

## Phase 2: Backend — Model Classes

**Purpose**: Create Java model classes for all new entities — needed by DAO layer

- [x] T008 [TDD] [P] Create WorkExperience.java model class in backend/src/main/java/com/resumainer/model/WorkExperience.java. (Constitution I)
- [x] T009 [TDD] [P] Create Education.java model class in backend/src/main/java/com/resumainer/model/Education.java. (Constitution I)
- [x] T010 [TDD] [P] Create Project.java model class in backend/src/main/java/com/resumainer/model/Project.java. (Constitution I)
- [x] T011 [TDD] [P] Create CourseCertificate.java model class in backend/src/main/java/com/resumainer/model/CourseCertificate.java. (Constitution I)
- [x] T012 [TDD] [P] Create AdditionalProfileInfo.java model class in backend/src/main/java/com/resumainer/model/AdditionalProfileInfo.java. (Constitution I)
- [x] T013 [TDD] [P] Create WorkFormat.java model class in backend/src/main/java/com/resumainer/model/WorkFormat.java. (Constitution I)

**Checkpoint**: All model classes compile with `mvn compile`

---

## Phase 3: Backend — DAO Layer

**Purpose**: Implement data access layer with PreparedStatement, owner-scoped queries (SEC-001), and soft-delete (SEC-003)

- [x] T014 [TDD] [SUBAGENT] Create WorkExperienceDao.java — CRUD with PreparedStatement, WHERE user_id = ? (SEC-001), is_deleted = FALSE filter (SEC-003), soft-delete UPDATE. Test: mock DataSource/Connection/PreparedStatement/ResultSet. (Constitution I, II, IV, V)
- [x] T015 [TDD] [P] [SUBAGENT] Create EducationDao.java — same pattern. Test: all CRUD methods. (Constitution I, II, IV, V)
- [x] T016 [TDD] [P] [SUBAGENT] Create ProjectDao.java — same pattern. Test: all CRUD methods. (Constitution I, II, IV, V)
- [x] T017 [TDD] [P] [SUBAGENT] Create CourseCertificateDao.java — same pattern + pagination with LIMIT/OFFSET + search/filter. Test: pagination params, search (3+ chars), date range filter. (Constitution I, II, IV, V)
- [x] T018 [TDD] [P] Create AdditionalProfileInfoDao.java — upsert pattern (INSERT ON CONFLICT UPDATE) for 1:1 relation. Test: first save creates, second save updates. (Constitution I, II, IV)
- [x] T019 [TDD] [P] Create WorkFormatDao.java — findAll lookup + findByUserId + saveUserFormats (delete + insert). Test: round-trip save/read. (Constitution I, II, IV)

**Checkpoint**: All DAO tests pass with `mvn test`

---

## Phase 4: Backend — DTOs + Service + Controller

**Purpose**: Business logic layer and REST API endpoints

- [x] T020 [TDD] Create ProfileSectionStatus.java and ProfileData.java DTOs in backend/src/main/java/com/resumainer/dto/. (Constitution I)
- [x] T021 [TDD] Create CoursePage.java — paginated response DTO with content, totalElements, totalPages, number, size. (Constitution I)
- [x] T022 [TDD] [SUBAGENT] Create ProfileService.java — business logic for all 6 sections, transaction management for atomic operations (e.g., AdditionalInfo + WorkFormat). Methods MUST NOT log PII (SEC-002). (Constitution I, II, V)
- [x] T023 [TDD] [SUBAGENT] Create ProfileController.java — REST endpoints for all sections under /api/profile/*. Extract userId from session, pass to service. Return Cache-Control: no-store, private (SEC-005). (Constitution I, II, III, V)
- [x] T024 [TDD] Update WebConfig.java — register ProfileService, ProfileController, and all new DAOs via @Bean (B1/B5 guard). (Constitution I)

**Checkpoint**: All backend tests pass with `mvn test`. Backend API testable via curl/Postman.

---

## Phase 5: Frontend — Core Infrastructure

**Purpose**: Frontend types, API service, i18n, and routing for profile feature

- [x] T025 [P] Create profile.ts TypeScript types in frontend/src/types/profile.ts — match backend DTOs (use BA field names: gpaGrade, courseFocus, description not comment). (Constitution I)
- [x] T026 [P] Create profileService.ts in frontend/src/services/profileService.ts — REST API methods for all 6 sections + status endpoint. Include error handling. (Constitution I, V)
- [x] T027 [P] Merge profile namespace into frontend/src/i18n/en.json — add all Profile UI strings from prototype. No hardcoded text (D13). Verify JSON validity. (Constitution III)
- [x] T028 [P] Merge profile namespace into frontend/src/i18n/ru.json — add all Profile RU translations from prototype. Preserve manually reviewed RU text. (Constitution III)
- [x] T029 Update frontend/src/router/index.ts — add /profile/* routes with requiresAuth meta guard. /profile redirects to /profile/contact. (Constitution III, V)

**Checkpoint**: Frontend compiles with `npm run build`

---

## Phase 6: Frontend — Profile Layout & Navigation (US1 Foundation)

**Purpose**: Profile shell, sidebar, mobile tabs, and navigation structure — shared by ALL sections

- [x] T030 Implement ProfilePage.vue in frontend/src/views/ProfilePage.vue — route-level view: loads ProfileShell, tracks dirty state across sections, handles UnsavedChangesDialog, browser beforeunload warning. (Constitution III)
- [x] T031 [P] Implement ProfileShell.vue in frontend/src/components/profile/ProfileShell.vue — layout wrapper with sidebar + main content area. (Constitution III)
- [x] T032 [P] Implement ProfileSidebar.vue in frontend/src/components/profile/ProfileSidebar.vue — desktop left sidebar with section list and status indicators (completed/incomplete, record counts). EN: "Completed ✓", "Incomplete !", "{count} records", "No records". RU from i18n. (Constitution III)
- [x] T033 [P] Implement ProfileMobileTabs.vue in frontend/src/components/profile/ProfileMobileTabs.vue — mobile 2-row × 3-column grid tabs at breakpoint < 768px. (Constitution III)
- [x] T034 [P] Implement ProfileSectionHeader.vue in frontend/src/components/profile/ProfileSectionHeader.vue — section title, purpose text, and required-fields note near save button. (Constitution III)
- [x] T035 [P] Implement UnsavedChangesDialog.vue in frontend/src/components/profile/UnsavedChangesDialog.vue — PrimeVue Dialog with "Leave without saving?" / "Stay on this page" buttons. EN and RU variants. (Constitution III)

**Checkpoint**: Profile layout renders with sidebar navigation and section switching

---

## Phase 7: Frontend — Contact Details (US1)

- [x] T036 [TDD] [P] Implement ContactDetailsSection.vue in frontend/src/components/profile/sections/ContactDetailsSection.vue — form fields matching data-model, Zod validation (email, URL with/without protocol), save with dirty-state tracking. (Constitution II, III)
- [x] T037 [P] Implement RecordCard.vue and EmptyRecordsState.vue in frontend/src/components/profile/ — shared UI patterns. (Constitution III)

**Checkpoint**: Contact Details section — save, validate, dirty state all work

---

## Phase 8: Frontend — Work Experience (US2) + Projects (US3)

- [x] T038 [TDD] [SUBAGENT] Implement WorkExperienceSection.vue in frontend/src/components/profile/sections/WorkExperienceSection.vue — card list + inline Add/Edit form. "I currently work here" hides End Date. Card shows "Present" chip. Smooth scroll to form. Real REST API. (Constitution II, III)
- [x] T039 [P] Implement InlineRecordForm.vue in frontend/src/components/profile/InlineRecordForm.vue — shared inline form component for add/edit. Emits save/cancel events with dirty state. (Constitution III)
- [x] T040 [TDD] [SUBAGENT] [P] Implement ProjectsSection.vue in frontend/src/components/profile/sections/ProjectsSection.vue — same card + inline form pattern. "Ongoing" checkbox hides End Date. Real REST API. (Constitution II, III)

**Checkpoint**: Work Experience and Projects sections — add/edit/delete/validate all work

---

## Phase 9: Frontend — Education (US4) + Courses (US5) + Additional Info (US6)

- [x] T041 [TDD] [SUBAGENT] Implement EducationSection.vue in frontend/src/components/profile/sections/EducationSection.vue — card list + inline form. "Currently studying" hides End Date. Empty state guidance text from spec. Card displays: institution, date range, location, degree, field. BA field names (description, gpaGrade). Real REST API. (Constitution II, III)
- [x] T042 [TDD] [SUBAGENT] Implement CoursesSection.vue + CoursesTable.vue + CourseDialog.vue in frontend/src/components/profile/courses/ — PrimeVue DataTable lazy mode (D17), server-side pagination 10/20/50, search (3+ chars), date filter range, 3-state column sorting, row-click details dialog, add/edit/delete. Real REST API with pagination params. (Constitution II, III, IV)
- [x] T043 [TDD] [SUBAGENT] Implement AdditionalInfoSection.vue in frontend/src/components/profile/sections/AdditionalInfoSection.vue — 4 visual blocks: Resume Preferences, Work Preferences, Professional Info, Personal Info. Username validation (English/digits/underscores/hyphens), language mutual exclusivity (number IDs), DOB/citizenship required. Real REST API. (Constitution II, III)

**Checkpoint**: All 6 profile sections implemented and independently testable

---

## Phase 10: Integration & Polish

**Purpose**: Connect frontend to backend, build verification, and final testing

- [ ] T044 Connect frontend profileService.ts to backend ProfileController — replace any localStorage mock with real REST API calls. (Constitution I)
- [ ] T045 Manual integration test — test all 6 sections end-to-end (Vue → API → PostgreSQL). Verify: save, edit, delete, validation errors, soft-delete, dirty state, unsaved-changes dialog. (Constitution II, D14)
- [ ] T046 i18n audit — verify no hardcoded Profile UI strings. Switch EN/RU and check all labels, toasts, validation messages, status text, empty states. (Constitution III)
- [ ] T047 Docker build verification — run `mvn clean package` and `npm run build`. Fix any compilation/type errors. (Constitution I)
- [ ] T048 [REVIEW] Security verification — confirm owner-scoped access (SEC-001), PII not in logs (SEC-002), soft-delete works (SEC-003), username uniqueness enforced (SEC-004), Cache-Control headers present (SEC-005). (Constitution V)

**Checkpoint**: Feature 006 complete — ready for merge

---

## Dependencies & Execution Order

### Phase Dependencies
- **Setup (Phase 1)**: No dependencies — can start immediately
- **Models (Phase 2)**: Depends on Phase 1 — tables must exist
- **DAOs (Phase 3)**: Depends on Phase 2 — model classes needed
- **Service + Controller (Phase 4)**: Depends on Phase 3 — DAOs needed
- **Frontend Infrastructure (Phase 5)**: Can start in parallel with Phase 2-4 (no backend dependency for types/i18n/routing)
- **Frontend Layout (Phase 6)**: Depends on Phase 5 — routing and i18n needed
- **Frontend Sections (Phase 7-9)**: Depends on Phase 6 — layout shell needed
- **Integration (Phase 10)**: Depends on all previous phases

### Parallel Opportunities
- All [P] tasks within a phase can run in parallel
- Phase 5 (Frontend Infrastructure) can start alongside Phase 2-4 (Backend)
- Migration tasks T001-T007 can run in parallel (different files)
- Model tasks T008-T013 can run in parallel
- DAO tasks T014-T019 can run in parallel (different entities)
- Section components T038-T043 can be worked on in parallel by different team members

### Execution Markers Summary
- **[TDD]**: 23 tasks require RED-GREEN-REFACTOR discipline
- **[P]**: 19 tasks can run in parallel
- **[SUBAGENT]**: 14 tasks can be dispatched to subagents
- **[REVIEW]**: 2 tasks require code review gate
