# Implementation Plan: User Profile Page

**Branch**: `feat/006-profile-page` | **Date**: 2026-06-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-user-profile/spec.md`

## Summary

Implement the User Profile / My Profile feature with 6 sections: Contact Details, Work Experience, Projects & Volunteering, Education, Courses & Certificates, Additional Info. Full-stack implementation: new Flyway migrations for profile entity tables, backend DAO/Service/Controller layers, and Vue 3 + PrimeVue frontend with sidebar navigation, inline record forms, and Courses DataTable with server-side pagination.

## Technical Context

**Language/Version**: Java 21 (backend), TypeScript/Vue 3 (frontend)

**Primary Dependencies**:
- Backend: Spring MVC 6.2, JDBC (plain), Flyway, SLF4J + Logback, JUnit 5 + Mockito
- Frontend: Vue 3, Vite, PrimeVue 4, PrimeIcons, vue-router, vue-i18n, zod, @primevue/forms

**Storage**: PostgreSQL 17 — new tables needed: `work_experience`, `education`, `project`, `course_certificate`, `additional_profile_info` (already defined in BA data dictionary, Flyway migrations V9+)

**Testing**: JUnit 5 + Mockito for backend DAO/Service/Controller tests. Manual integration testing per D14.

**Target Platform**: Docker Compose (backend + frontend + PostgreSQL)

**Project Type**: Web application (Java backend + Vue SPA frontend)

**Performance Goals**: Courses DataTable with server-side pagination (10/20/50 per page) for up to 300 records. Section navigation switches content within 2 seconds.

**Constraints**: No Spring Boot, no JPA/Hibernate, no ORM. PreparedStatement for all SQL. JDBC transaction management for atomic saves. No autosave — all saves explicit.

**Scale/Scope**: Single user editing own profile (no concurrent edit conflicts expected). Up to 300 course records per user, 20 work experience records, 20-30 each for other sections.

## Security Requirements (from Security Review 2026-06-07)

### SEC-001: Owner-Scoped Access Control
All profile DAO queries MUST include `WHERE user_id = ?` as a PreparedStatement parameter to ensure each user can only access their own records. The authenticated user ID is extracted from the HttpSession and passed through ProfileController → ProfileService → DAO. Any query that omits the owner filter MUST be rejected in code review.

### SEC-002: PII Logging Protection
Profile data field values (fullName, email, phone, DOB, citizenship, location) MUST NOT appear in server log statements. Log only: operation type, user ID, affected entity type, and success/failure status. The global exception handler (NFR-003) strips PII from error responses.

### SEC-003: Soft-Delete for Profile Records
All profile entity tables (work_experience, education, project, course_certificate) MUST include `is_deleted BOOLEAN NOT NULL DEFAULT FALSE` and `deleted_at TIMESTAMP` columns, consistent with the existing `users.is_deleted` and `saved_resumes.is_deleted` pattern. `additional_profile_info` (1:1 with users) does not need soft-delete — it is updated in-place or created on first save. DAO SELECT queries MUST filter `WHERE is_deleted = FALSE` by default. DELETE endpoints perform soft-delete (UPDATE is_deleted = TRUE) rather than hard delete.

### SEC-004: Database-Level Uniqueness (Confirmed)
Username uniqueness is enforced by `users.username` UNIQUE constraint. DAO layer catches constraint violations and translates to user-friendly "username taken" error (not 500).

### SEC-005: Cache-Control Headers
All profile API endpoints MUST return `Cache-Control: no-store, private` headers to prevent caching of personal data in shared browsers or proxies.

## Constitution Check

*GATE: Must pass before proceeding to task breakdown. Re-check if design changes.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | ✅ Pass | Standard layered architecture (controller/service/dao/model). Profile sections share common patterns (card list + inline form). No JPA/Hibernate/Spring Boot. Maven CLI build. |
| **II. Testing Excellence** | ✅ Pass | JUnit 5 + Mockito for all new DAOs, Services, Controllers. TDD for business logic (validation rules, date range checks). Mock AI provider not needed (no AI calls in Profile). |
| **III. User Experience Consistency** | ✅ Pass | i18n via vue-i18n for ALL Profile UI strings (EN + RU). Dual validation (Zod frontend + backend). Subtle status styling (not badges). Short save button labels. Toasts without periods. |
| **IV. Performance & Reliability** | ✅ Pass | PreparedStatement for all queries. JDBC transactions for atomic multi-table saves. SQL-level pagination for Courses. UTF-8 encoding. |
| **V. Security by Design** | ✅ Pass | Backend validation is authoritative. Username validation (English/digits/underscores/hyphens only, no Cyrillic). No stack traces exposed. Global exception handler (NFR-003). |

## Project Structure

### Documentation (this feature)

```
specs/006-user-profile/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── memory-synthesis.md  # Context synthesis (completed)
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output — API contracts
├── checklists/          # Spec quality checklist
├── spec.md              # Feature specification
└── spec_input_files/    # Prototype reference files
```

### Source Code

```
backend/
├── src/main/java/com/resumainer/
│   ├── config/
│   │   └── WebConfig.java                          # Register new profile DAOs/Services/Controllers
│   ├── controller/
│   │   └── ProfileController.java                  # REST endpoints for all 6 profile sections
│   ├── service/
│   │   └── ProfileService.java                     # Business logic for profile CRUD operations
│   ├── dao/
│   │   ├── WorkExperienceDao.java                  # work_experience CRUD
│   │   ├── EducationDao.java                       # education CRUD
│   │   ├── ProjectDao.java                         # project CRUD
│   │   ├── CourseCertificateDao.java               # course_certificate CRUD with pagination
│   │   ├── AdditionalProfileInfoDao.java           # additional_profile_info CRUD (1:1)
│   │   └── WorkFormatDao.java                      # work_format lookup + user_work_format junction
│   ├── model/
│   │   ├── WorkExperience.java
│   │   ├── Education.java
│   │   ├── Project.java
│   │   ├── CourseCertificate.java
│   │   ├── AdditionalProfileInfo.java
│   │   └── WorkFormat.java
│   └── dto/
│       ├── ProfileSectionStatus.java               # Section completion status DTO
│       ├── ProfileData.java                        # Aggregated profile response DTO
│       └── CoursePage.java                         # Paginated course response DTO
├── src/main/resources/db/migration/
│   ├── V9__create_work_experience_table.sql
│   ├── V10__create_education_table.sql
│   ├── V11__create_project_table.sql
│   ├── V12__create_course_certificate_table.sql
│   ├── V13__create_additional_profile_info_table.sql
│   ├── V14__create_work_format_tables.sql
│   └── V15__seed_work_format_data.sql
└── src/test/java/com/resumainer/
    ├── dao/
    │   ├── WorkExperienceDaoTest.java
    │   ├── EducationDaoTest.java
    │   ├── ProjectDaoTest.java
    │   ├── CourseCertificateDaoTest.java
    │   ├── AdditionalProfileInfoDaoTest.java
    │   └── WorkFormatDaoTest.java
    ├── service/
    │   └── ProfileServiceTest.java
    └── controller/
        └── ProfileControllerTest.java

frontend/
├── src/
│   ├── views/
│   │   └── ProfilePage.vue                         # Route-level Profile view (from prototype)
│   ├── components/profile/
│   │   ├── ProfileShell.vue                        # Layout: sidebar + content area
│   │   ├── ProfileSidebar.vue                      # Desktop left sidebar
│   │   ├── ProfileMobileTabs.vue                   # Mobile 2x3 tab grid
│   │   ├── ProfileSectionHeader.vue                # Section title + purpose
│   │   ├── RecordCard.vue                          # Compact saved-record card
│   │   ├── EmptyRecordsState.vue                   # "No records yet" pattern
│   │   ├── UnsavedChangesDialog.vue                # Leave-without-saving modal
│   │   └── InlineRecordForm.vue                    # Shared Add/Edit form
│   ├── components/profile/sections/
│   │   ├── ContactDetailsSection.vue               # Contact form
│   │   ├── WorkExperienceSection.vue               # Cards + inline form
│   │   ├── ProjectsSection.vue                     # Cards + inline form
│   │   ├── EducationSection.vue                    # Cards + inline form
│   │   ├── CoursesSection.vue                      # DataTable + dialog
│   │   └── AdditionalInfoSection.vue               # 4-block form
│   ├── components/profile/courses/
│   │   ├── CoursesTable.vue                        # PrimeVue DataTable lazy mode
│   │   └── CourseDialog.vue                        # Add/View/Edit dialog
│   ├── services/
│   │   └── profileService.ts                       # REST API calls for all profile sections
│   └── types/
│       └── profile.ts                              # TypeScript interfaces
├── src/i18n/
│   ├── en.json                                     # Add profile namespace
│   └── ru.json                                     # Add profile namespace
└── src/router/
    └── index.ts                                    # Add /profile/* routes
```

## Complexity Tracking

No constitution violations — all principles satisfied without complexity trade-offs.

## Implementation Phases

### Phase 0: Research & Design
- Verify existing BA data dictionary for all profile entity fields
- Document API contract (REST endpoints for each section)
- Define pagination strategy for Courses (lazy DataTable + server-side LIMIT/OFFSET)

### Phase 1: Backend — Database & DAO Layer
- Create Flyway migrations V9-V15 for all profile tables (include `is_deleted` + `deleted_at` on all entity tables per SEC-003)
- Implement model classes: WorkExperience, Education, Project, CourseCertificate, AdditionalProfileInfo, WorkFormat
- Implement DAO classes with PreparedStatement + connection-accepting overloads (D10 pattern)
- ALL DAO SELECT/UPDATE/DELETE queries MUST include `WHERE user_id = ?` (SEC-001, PreparedStatement parameter)
- DAO SELECT queries MUST filter `WHERE is_deleted = FALSE` by default (SEC-003)
- DELETE operations perform `UPDATE is_deleted = TRUE` (soft-delete, SEC-003)
- Implement DAO tests for all new DAOs (JUnit 5 + Mockito)

### Phase 2: Backend — Service & Controller Layer
- Implement ProfileService with transaction management for atomic operations
- ProfileService extracts authenticated user ID from session and passes to DAOs (SEC-001)
- Implement ProfileController with REST endpoints for each section
- All ProfileController responses include `Cache-Control: no-store, private` header (SEC-005)
- ProfileService methods MUST NOT log profile field values — log only operation type, user ID, and status (SEC-002)
- Add profile-related routes to WebConfig (@Bean registration per B1/B5)
- Implement Controller and Service tests (standalone MockMvc per D16)
- Register all new beans via @Repository/@Service/@Controller + @ComponentScan (D15)

### Phase 3: Frontend — Core Infrastructure
- Create TypeScript types (profile.ts) matching backend DTOs
- Create profileService.ts with REST API methods
- Add profile namespace to en.json and ru.json (merge from prototype)
- Update router/index.ts with /profile/* routes
- Register PrimeVue plugins if needed (ToastService D20, Tooltip D21)

### Phase 4: Frontend — Profile Layout & Navigation
- Implement ProfilePage.vue (route-level view)
- Implement ProfileShell.vue, ProfileSidebar.vue, ProfileMobileTabs.vue
- Implement ProfileSectionHeader.vue
- Implement section status logic (completed/incomplete, record counts)
- Implement UnsavedChangesDialog.vue

### Phase 5: Frontend — Section Components (Part 1)
- Implement ContactDetailsSection.vue (form + validation + save)
- Implement WorkExperienceSection.vue (cards + inline form)
- Implement ProjectsSection.vue (cards + inline form)

### Phase 6: Frontend — Section Components (Part 2)
- Implement EducationSection.vue (cards + inline form)
- Implement CoursesSection.vue + CoursesTable.vue + CourseDialog.vue (DataTable lazy mode, search, filter, pagination)
- Implement AdditionalInfoSection.vue (4-block form)

### Phase 7: Integration & Polish
- Connect frontend to backend API (profileService.ts → ProfileController)
- End-to-end test all 6 sections
- i18n audit — verify no hardcoded strings
- Build verification (mvn clean package + npm run build)
- Docker Compose integration test

## API Contracts

### Profile Section Status
```
GET /api/profile/status → { contact: "completed"|"incomplete", experience: { count: 3, label: "3 records" }, ... }
```

### Contact Details
```
GET  /api/profile/contact       → ContactDetails
PUT  /api/profile/contact       ← ContactDetails → ContactDetails
```

### Work Experience
```
GET    /api/profile/experience             → WorkExperience[]
POST   /api/profile/experience             ← WorkExperience → WorkExperience
PUT    /api/profile/experience/{id}        ← WorkExperience → WorkExperience
DELETE /api/profile/experience/{id}        → 204 No Content
```

### Projects
```
GET    /api/profile/projects               → Project[]
POST   /api/profile/projects               ← Project → Project
PUT    /api/profile/projects/{id}          ← Project → Project
DELETE /api/profile/projects/{id}          → 204 No Content
```

### Education
```
GET    /api/profile/education              → Education[]
POST   /api/profile/education              ← Education → Education
PUT    /api/profile/education/{id}         ← Education → Education
DELETE /api/profile/education/{id}         → 204 No Content
```

### Courses & Certificates
```
GET    /api/profile/courses?page=0&size=10&sort=startDate,desc&search=&dateFrom=&dateTo= → CoursePage (paginated)
POST   /api/profile/courses                ← Course → Course
PUT    /api/profile/courses/{id}           ← Course → Course
DELETE /api/profile/courses/{id}           → 204 No Content
```

### Additional Info
```
GET  /api/profile/additional   → AdditionalInfo
PUT  /api/profile/additional   ← AdditionalInfo → AdditionalInfo
```
