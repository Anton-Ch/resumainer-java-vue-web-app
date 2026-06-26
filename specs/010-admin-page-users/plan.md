# Implementation Plan: Admin Console Users and Resumes

**Branch**: `feat/010-admin-page-users` | **Date**: 2026-06-26 | **Spec**: `specs/010-admin-page-users/spec.md`

**Input**: Feature specification for `feat/010-admin-page-users`: Admin Home dashboard with all-resumes moderation table, Admin Users list, Admin User Details with tabs, account access management, admin soft-delete for resumes and users, and AI Models WIP placeholder.

> **Instruction for implementer**: This plan is a controlled implementation guide. If any item conflicts with the actual current codebase, STOP and ask before changing architecture, adding migrations, or inventing workarounds. Do not expand scope beyond this plan.

---

## Summary

Implement the MVP Admin Console for ResumAIner:

- `/app/admin` becomes a real Admin Home dashboard.
- Admin Home shows:
  - real total users count (non-deleted only);
  - real total resumes count (non-deleted only);
  - WIP token stats as hardcoded `0` with WIP badges;
  - quick cards for Users, Resumes, and AI Models;
  - all-resumes moderation table directly on Admin Home.
- `/app/admin/users` shows a searchable, filterable, sortable, paginated users table.
- `/app/admin/users/:userId` shows user details with tabs:
  1. Contacts;
  2. Account;
  3. Additional;
  4. Resumes.
- Admin can edit only Account tab access controls:
  - role;
  - status;
  - generation permission;
  - privileged flag.
- Admin can soft-delete resumes (from Admin Home table modal and User Details Resumes tab modal).
- Admin can soft-delete other users.
- Admin cannot self-delete, self-demote from ADMIN to USER, or self-block from ACTIVE to BLOCKED.
- `/app/admin/ai-models` shows a localized Work in Progress placeholder only.

This feature MUST NOT modify PDF generation, PDF fitting, AI generation, prompt building, parser, response validator, budget configs, finalization pipeline, or resume templates.

---

## Technical Context

**Language/Version**:

- Backend: Java 21
- Frontend: TypeScript 5.x, Vue 3 Composition API

**Primary Dependencies**:

- Backend: Spring MVC 6, Plain JDBC, custom connection pool, Flyway, SLF4J/Logback, JUnit 5, Mockito
- Frontend: Vue 3, Vite, PrimeVue, vue-i18n, Vitest, Vue Test Utils
- Infrastructure: Docker Compose deployment with backend, frontend, PostgreSQL

**Storage**:

- PostgreSQL
- Existing tables expected to be sufficient (no new migrations planned):
  - `users` (UUID PK, is_deleted, deleted_at, role_id, status_id, permission_id, is_privileged, created_at, email, username)
  - `role` (BIGSERIAL PK, code, name)
  - `user_status` (BIGSERIAL PK, code, name)
  - `user_permission` (BIGSERIAL PK, code, name)
  - `contact_detail` (UUID PK, resume_email, full_name, professional_title, phone, location, linkedin_url, portfolio_url, telegram, whatsapp)
  - `additional_profile_info` (UUID PK, professional_info, languages, professional_aspirations, achievements, ai_additional_context, date_of_birth, citizenship)
  - `saved_resumes` (BIGSERIAL PK, user_id UUID FK, is_deleted, deleted_at, cover_letter, etc.)
  - `language` (BIGSERIAL PK, code, name)
  - Repeatable profile tables: `work_experience`, `education`, `project`, `course_certificate` — check which already support soft-delete

**Testing**:

- Backend: JUnit 5 + Mockito, controller/service/DAO tests where practical
- Frontend: Vitest + Vue Test Utils for component/composable behavior
- Manual/E2E evidence: screenshots for each admin page/state

**Current codebase state (verified via Serena MCP)**:

- Router `frontend/src/router/index.ts` already has route for `/admin` (AdminHomePage) with `meta: { requiresAuth: true, requiresAdmin: true }` and a global `beforeEach` guard checking `role !== 'ADMIN'`
- No routes yet for `/admin/users`, `/admin/users/:userId`, or `/admin/ai-models`
- No backend admin controller, service, or DAO exists
- `AuthInterceptor` only checks authentication (session user exists) — does NOT check ADMIN role
- `PagedResponse<T>` uses field `items` (not `content`) in both backend and frontend
- `HomeSavedResumeDto` exists with similar field pattern to `AdminSavedResumeDto`
- `UserHomeService`/`SavedResumeDao`/`UserDao` exist for reference
- Soft-delete pattern: `is_deleted` boolean + `deleted_at` timestamp already used in `users` and `saved_resumes`

---

## Scope Decisions

### In Scope

1. Admin Home dashboard at `/app/admin`.
2. Admin Resumes table directly on `/app/admin` (no separate page).
3. Admin Users page at `/app/admin/users`.
4. Admin User Details page at `/app/admin/users/:userId`.
5. User Details tabs:
   - Contacts read-only (resumeEmail from `contact_detail.resume_email`);
   - Account editable (with read-only accountEmail from `users.email`);
   - Additional read-only;
   - Resumes table for selected user.
6. Admin resume details modal based on existing resume modal pattern (reuses table DTO data — no separate modal-details API, per FR-135).
7. Admin resume soft-delete.
8. Admin user soft-delete with cascade to saved_resumes.
9. Account access update with confirmation and self-protection.
10. AI Models WIP page at `/app/admin/ai-models`.
11. i18n EN/RU for all new visible strings.
12. Backend ADMIN authorization for every `/api/admin/**` endpoint.

### Out of Scope

1. AI Models CRUD.
2. API key management.
3. Audit log.
4. Real token usage aggregation.
5. Separate `/app/admin/resumes` full page.
6. Hard-delete of users/resumes/profile data.
7. Editing Contacts tab fields.
8. Editing Additional tab fields.
9. Creating resumes from admin pages.
10. New public resume route behavior.
11. New PDF generation behavior.
12. Database migrations unless baseline inspection proves a required existing field is missing and the user explicitly approves.
13. Dedicated modal-details API endpoint (FR-135).

---

## Key Product Decisions

### Admin Home Resumes Location

Admin Resumes full table MUST live on `/app/admin`, not on a separate `/app/admin/resumes` page.

The Resumes quick card on Admin Home should scroll or anchor to the resumes section on the same page.

### Token Stats

Token stats are intentionally WIP:

- `totalTokensSent = 0`
- `totalTokensGenerated = 0`
- both cards show WIP badge

Do not query `ai_usage_log` for real token stats in this feature.

### Admin Users Search

Admin Users uses one shared text search field over:

- `users.username`
- `users.email`
- `contact_detail.full_name` (LEFT JOIN)

Do not create separate search fields for these values.

### Date Range Semantics

For both Admin Users and Admin Resumes:

- `createdFrom` is inclusive from selected day start.
- `createdTo` is inclusive through selected day by filtering `< next day start`.
- Frontend datepicker must prevent selecting `createdTo` earlier than `createdFrom`.
- Backend must handle invalid date ranges safely and must not crash.

### User Status Naming

Use only:

- `ACTIVE`
- `BLOCKED`

Do not introduce `DISABLED` in this feature.

### Rights Naming

Rights maps to `users.is_privileged` / DTO `isPrivileged`.

Filter values:

- `ALL`
- `PRIVILEGED`
- `NON_PRIVILEGED`

### User Details Tab Order

Fixed tab order:

1. Contacts
2. Account
3. Additional
4. Resumes

### Editable User Details Fields

Only Account tab fields are editable:

- role
- status
- generation permission
- privileged flag

Account tab also MUST display read-only `accountEmail` from `users.email` (FR-078a).

Contacts and Additional tabs are read-only.

Contacts tab MUST display `resumeEmail` from `contact_detail.resume_email`, labeled as `Resume email` / `Email for resume` (FR-075a).

### Self-Protection

Admin cannot:

- delete own account (backend rejects even if frontend bypassed, FR-096);
- change own role from `ADMIN` to `USER` (FR-089);
- change own status from `ACTIVE` to `BLOCKED` (FR-090).

Admin can:

- change own generation permission (FR-091);
- change own privileged flag (FR-092).

Frontend should prevent disallowed self-actions for UX (disable buttons, show tooltips), but backend validation is mandatory and authoritative.

---

## Security Review Findings to Implement

### SEC-001 - Backend Admin Authorization

Every `/api/admin/**` endpoint MUST enforce ADMIN role on the backend.

The existing `AuthInterceptor` only checks authentication. Add an ADMIN role check for `/api/admin/**` paths — either in `AuthInterceptor` or via a separate interceptor/check.

Expected behavior:

- unauthenticated request: rejected (`401`) according to existing authentication behavior;
- authenticated non-admin request: rejected with safe forbidden response (`403`);
- authenticated admin request: allowed.

### SEC-002 - Sensitive Data Exclusion

Admin DTOs MUST NOT expose:

- `password_hash`
- raw filesystem paths
- storage directories
- local PDF/HTML file paths
- API keys
- encrypted API keys
- internal exception details

### SEC-003 - SQL Sort Whitelist

All sortable fields MUST use whitelist mapping. Never inject raw `sort` query params into SQL `ORDER BY`.

### SEC-004 - Generic Delete Errors

Delete failures must not leak whether a target ID was non-existent, non-owned, already deleted, or failed for an internal reason. Backend may log details server-side. Frontend must show generic localized error.

### SEC-005 - Soft Delete Only

Delete resume and delete user must be soft-delete only. No hard delete in this feature.

### SEC-006 - User Soft-Delete Transaction

User soft-delete should run in a single JDBC transaction:

1. mark user `is_deleted = true`;
2. set `users.deleted_at`;
3. set status to `BLOCKED`;
4. soft-delete user's saved resumes;
5. after user soft-delete cascade, previously valid public URLs for those resumes MUST follow existing soft-deleted public resume behavior (`410 Gone`) and MUST NOT continue serving active PDFs (FR-105a);
6. soft-delete related repeatable profile records only where tables already support soft-delete.

Do not add migrations only to soft-delete contact/additional records.

### SEC-007 - 404 for Deleted User Access Update

If `PATCH /api/admin/users/{userId}/access` targets a soft-deleted or non-existent user, backend MUST return `404 Not Found` (FR-092a).

---

## Constitution Check

*GATE: Must pass before task breakdown. Re-check if design changes.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | PASS | Keep layered controller/service/dao architecture. Add dedicated admin DTOs in `dto/admin/` package. Do not return raw entities. Do not modify unrelated PDF/AI/generation code. |
| **II. Testing Excellence** | PASS | Add targeted backend tests for admin authorization, dashboard, users/resumes queries, access update, self-protection, and soft-delete. Add frontend tests for filters, row click, tabs, unsaved warning, and WIP page. |
| **III. User Experience Consistency** | PASS | Reuse User Home saved resumes table UX patterns: search, date range, dropdown filters, reset filters, sort icons/tooltips, row hover/highlight, pagination. Reuse Profile unsaved-changes UX pattern for Account tab. All new strings externalized to en.json/ru.json. |
| **IV. Performance & Reliability** | PASS | Use paginated backend queries. No modal-details API (FR-135). Use JDBC transaction for user soft-delete. Use safe validation for invalid date ranges. PreparedStatement for all queries. |
| **V. Security by Design** | PASS | Backend ADMIN check on all admin APIs. No sensitive fields in DTOs. Sort whitelist. Soft-delete only. Self-delete/self-demotion/self-block forbidden on backend. 404 for deleted user PATCH. 410 for soft-deleted resume public URLs. |

**Gate result**: PASS — no known constitution violations.

---

## API Design

### Important note on PagedResponse field

The existing `PagedResponse<T>` model uses field name **`items`** (not `content`). All JSON response examples below use `items` to match the actual codebase. Frontend interface `PagedResponse` also uses `items`.

### 1. Dashboard

```
GET /api/admin/dashboard
```

Response:
```json
{
  "totalUsers": 12,
  "totalResumes": 48,
  "totalTokensSent": 0,
  "totalTokensSentWip": true,
  "totalTokensGenerated": 0,
  "totalTokensGeneratedWip": true
}
```

Notes:
- `totalUsers`: count non-deleted users.
- `totalResumes`: count non-deleted saved resumes.
- token stats are hardcoded WIP values.

---

### 2. Admin Resumes on Admin Home

```
GET /api/admin/resumes?page=0&size=10&search=&language=&adaptationLevel=&createdFrom=&createdTo=&sort=createdAt,desc
```

Search covers:
- resume title;
- vacancy title;
- company name;
- owner username;
- owner email (`users.email` account email);
- owner full name from `contact_detail.full_name` (LEFT JOIN, safe fallback if missing).

Response: `PagedResponse<AdminSavedResumeDto>` (field: `items`, not `content`).

Response shape:
```json
{
  "items": [
    {
      "id": 101,
      "ownerUserId": "uuid",
      "ownerUsername": "anton",
      "ownerEmail": "anton@example.com",
      "ownerFullName": "Anton Ch.",
      "resumeTitle": "Senior Java Developer",
      "vacancyTitle": "Senior Java Developer",
      "companyName": "ABC LTD.",
      "languageCode": "RU",
      "languageName": "Russian",
      "adaptationLevel": "BALANCED",
      "createdAt": "2026-06-25T10:20:00",
      "publicUrlLink": "https://example.com/anton/ABCDE",
      "pdfOpenUrl": "/api/generate/resumes/101/pdf?disposition=inline",
      "pdfDownloadUrl": "/api/generate/resumes/101/pdf",
      "htmlDownloadUrl": "/api/generate/resumes/101/html",
      "pdfAvailable": true,
      "pdfStatus": "READY",
      "pdfMessage": null,
      "coverLetter": "..."
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 48,
  "totalPages": 5
}
```

Delete:
```
DELETE /api/admin/resumes/{resumeId}
```
Success: `{ "message": "Resume deleted successfully." }`

---

### 3. Admin Users

```
GET /api/admin/users?page=0&size=10&search=&role=&status=&permission=&rights=&createdFrom=&createdTo=&sort=createdAt,desc
```

Search covers: username, email, full name.

Filters:
- role: `ALL / USER / ADMIN`
- status: `ALL / ACTIVE / BLOCKED`
- permission: `ALL / ALLOWED / FORBIDDEN`
- rights: `ALL / PRIVILEGED / NON_PRIVILEGED`
- account creation date range: `createdFrom`, `createdTo`

Response: `PagedResponse<AdminUserListItemDto>`.

Response shape:
```json
{
  "items": [
    {
      "id": "uuid",
      "fullName": "John Doe",
      "username": "johndoe",
      "email": "john@example.com",
      "roleCode": "USER",
      "roleName": "User",
      "statusCode": "ACTIVE",
      "statusName": "Active",
      "permissionCode": "ALLOWED",
      "permissionName": "Allowed",
      "isPrivileged": false,
      "resumesCount": 3,
      "createdAt": "2026-06-01T12:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 12,
  "totalPages": 2
}
```

---

### 4. Admin User Details

```
GET /api/admin/users/{userId}
```

Response: `AdminUserDetailsDto`.

**Important**: Contacts section uses `resumeEmail` from `contact_detail.resume_email`, not `users.email`. Account section includes read-only `accountEmail` from `users.email`.

Response shape:
```json
{
  "id": "uuid",
  "contacts": {
    "fullName": "John Doe",
    "professionalTitle": "Backend Developer",
    "resumeEmail": "resume@example.com",
    "phone": "+7...",
    "location": "Almaty",
    "linkedinUrl": "https://linkedin.com/in/johndoe",
    "portfolioUrl": "https://johndoe.dev",
    "telegram": "@johndoe",
    "whatsapp": "+7..."
  },
  "account": {
    "username": "johndoe",
    "accountEmail": "john@example.com",
    "roleCode": "USER",
    "roleName": "User",
    "statusCode": "ACTIVE",
    "statusName": "Active",
    "permissionCode": "ALLOWED",
    "permissionName": "Allowed",
    "isPrivileged": false,
    "createdAt": "2026-06-01T12:00:00",
    "isCurrentAdmin": false
  },
  "additional": {
    "username": "johndoe",
    "professionalInfo": "...",
    "languages": ["English", "Russian"],
    "professionalAspirations": "...",
    "achievements": "...",
    "aiAdditionalContext": "...",
    "dateOfBirth": "1993-01-01",
    "citizenship": "Kazakhstan"
  }
}
```

Update access:
```
PATCH /api/admin/users/{userId}/access
```

Request:
```json
{
  "roleCode": "ADMIN",
  "statusCode": "ACTIVE",
  "permissionCode": "ALLOWED",
  "isPrivileged": true
}
```

Success: `{ "message": "User access updated successfully." }`

Self-protection error: `{ "message": "This action is not allowed for your own admin account." }`

**Deleted user behavior**: If target user is soft-deleted or does not exist, backend returns `404 Not Found` (FR-092a).

Delete user:
```
DELETE /api/admin/users/{userId}
```
Success: `{ "message": "User account deleted successfully." }`

Self-delete: Backend rejects even if frontend bypassed.

---

### 5. Selected User Resumes

```
GET /api/admin/users/{userId}/resumes?page=0&size=10&search=&language=&adaptationLevel=&createdFrom=&createdTo=&sort=createdAt,desc
```

Response: `PagedResponse<AdminSavedResumeDto>`, scoped to the selected user.

---

## Backend Architecture

### New Files

```
backend/src/main/java/com/resumainer/
├── controller/
│   └── AdminController.java                    # All /api/admin/** endpoints
├── service/
│   └── AdminService.java                       # Business logic for admin operations
├── dao/
│   └── AdminDao.java                           # Admin-specific cross-user queries
└── dto/
    └── admin/
        ├── AdminDashboardDto.java              # Dashboard stats response
        ├── AdminSavedResumeDto.java            # Admin view of saved resume with owner info
        ├── AdminUserListItemDto.java           # Admin Users table row DTO
        ├── AdminUserDetailsDto.java            # Composed user details (contacts+account+additional)
        ├── AdminUserContactsDto.java           # Read-only contacts data
        ├── AdminUserAccountDto.java            # Editable account + read-only accountEmail
        ├── AdminUserAdditionalDto.java         # Read-only additional profile data
        └── AdminAccessUpdateRequestDto.java    # PATCH request for access update
```

### Updated Existing Files

```
backend/src/main/java/com/resumainer/
├── interceptor/AuthInterceptor.java  # Add ADMIN role check for /api/admin/** paths
├── config/WebConfig.java             # Register admin interceptor if separate
├── dao/UserDao.java                  # Add user details query, access update, soft-delete
├── dao/SavedResumeDao.java           # Add admin cross-user resume query (if not in AdminDao)
├── dao/RoleDao.java                  # Lookup helpers if needed
├── dao/UserStatusDao.java            # Lookup helpers if needed
├── dao/UserPermissionDao.java        # Lookup helpers if needed
└── dao/ContactDetailDao.java         # For user details contacts section
```

### Admin Authorization Strategy

Current state: `AuthInterceptor` checks `session.getAttribute("user") != null` (authentication only).

Option A (preferred): Add ADMIN role check directly in `AuthInterceptor` for paths starting with `/api/admin/`:
```java
// Use getServletPath() or strip getContextPath() to avoid context-path issues
String path = request.getServletPath();
if (path.startsWith("/api/admin/")) {
    UserSession user = (UserSession) session.getAttribute("user");
    if (!"ADMIN".equals(user.getRole())) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // return forbidden JSON
        return false;
    }
}
```
> **Safety note**: Do not rely on raw `request.getRequestURI().startsWith("/api/admin/")` if the app may run with a context path. Use `request.getServletPath()` or strip `request.getContextPath()` before checking `/api/admin/**`.

Option B: Controller-level check in `AdminController` or `AdminService`.

Option A is preferred because it keeps security centralized. Every `/api/admin/**` endpoint is automatically protected regardless of controller implementation.

### AdminDao Responsibilities

All queries needing fullName or ownerFullName must use `LEFT JOIN contact_detail cd ON cd.user_id = u.id`.

`AdminDao` should provide:

- dashboard counts (totalUsers non-deleted, totalResumes non-deleted);
- paginated all-resumes query with search/filter/sort/date range;
- all-resumes total count;
- admin resume soft-delete (reuses or wraps existing SavedResumeDao.softDelete);
- paginated users query with search/filter/sort/date range;
- users total count;
- user details query (joins users + contact_detail + additional_profile_info + lookup tables);
- user access update (role, status, permission, is_privileged);
- user soft-delete transaction (is_deleted, deleted_at, status BLOCKED, cascade to resumes);
- selected user's resumes query (scoped to userId);
- selected user's resumes total count.

### Sort Whitelist Examples

Admin Users allowed sort fields (all lowercase for whitelist comparison):

- `fullname`, `username`, `email`
- `role`, `status`, `permission`, `rights`
- `resumescount`, `createdat`

Admin Resumes allowed sort fields:

- `resumetitle`, `vacancytitle`, `companyname`
- `language`, `adaptationlevel`, `createdat`
- `ownerusername`, `owneremail`, `ownerfullname`

Implementation must map each public sort key to a fixed SQL expression using a whitelist map (following the pattern from `CourseCertificateDao`).

### Date Range Backend Handling

- Accept date format matching existing project conventions (ISO date or LocalDate).
- Invalid/unparseable dates return safe validation error.
- `createdFrom` maps to `start of day` (e.g., `>= '2026-06-01 00:00:00'`).
- `createdTo` maps to `before next day start` (e.g., `< '2026-06-26 00:00:00'`).
- If `createdTo` < `createdFrom`, return empty result set or safe validation error (must not crash).

### User Soft-Delete Transaction

Pseudo-sequence (single JDBC transaction):

```text
BEGIN TRANSACTION
  verify current user is ADMIN
  verify target user exists and is not deleted
  verify target user is not current admin
  update users set is_deleted=true, deleted_at=CURRENT_TIMESTAMP, status_id=(BLOCKED id)
  update saved_resumes set is_deleted=true, deleted_at=CURRENT_TIMESTAMP where user_id=? and is_deleted=false
  -- soft-delete repeatable profile records where tables support is_deleted/deleted_at
  -- (check which tables: work_experience? education? project? course_certificate?)
COMMIT
```

Rollback on any failure. Do not add schema changes for contact/additional profile soft-delete.

After cascade, previously valid public URLs for those resumes already follow `410 Gone` behavior from existing public route implementation (Feature 009). Verify this during testing.

### Resume Soft-Delete

Admin resume delete reuses existing `SavedResumeDao.softDelete(resumeId)` or wraps it with an admin authorization check. Must set both `is_deleted=true` and `deleted_at`.

Generic error for missing/already-deleted resume (SEC-004).

---

## Frontend Architecture

### Current Frontend State (verified)

- `frontend/src/router/index.ts`: route exists for `/admin` (AdminHomePage) with `meta: { requiresAuth: true, requiresAdmin: true }`. Global `beforeEach` guard checks `role !== 'ADMIN'`.
- `AdminHomePage.vue`: exists but likely minimal/placeholder.
- No routes for `/admin/users`, `/admin/users/:userId`, `/admin/ai-models`.
- User Home saved resumes table (`SavedResumesTable.vue`) and modal (`ResumeDetailsDialog.vue`) exist for reference/reuse.

### New Files

```
frontend/src/
├── services/
│   └── adminService.ts                      # API client for /api/admin/*
├── composables/
│   ├── useAdminDashboard.ts                 # Dashboard data + WIP state
│   ├── useAdminUsers.ts                     # Users table state (search/filter/sort/page)
│   └── useAdminUserDetails.ts               # User details tabs + account edit state
├── views/
│   ├── AdminHomePage.vue                    # Updated: dashboard + resumes section
│   ├── AdminUsersPage.vue                   # New: users table page
│   ├── AdminUserDetailsPage.vue             # New: user details tabs page
│   └── AdminAiModelsWipPage.vue             # New: WIP page
└── components/
    └── admin/
        ├── AdminStatsCards.vue              # Dashboard stats cards
        ├── AdminQuickActions.vue            # Quick action navigation cards
        ├── AdminResumesTable.vue            # All-resumes table section on Admin Home
        ├── AdminUsersTable.vue              # Users table
        ├── AdminUserDetailsShell.vue        # Details shell with sidebar/tabs
        ├── AdminUserContactsTab.vue         # Read-only contacts display
        ├── AdminUserAccountTab.vue          # Editable account form
        ├── AdminUserAdditionalTab.vue       # Read-only additional display
        └── AdminUserResumesTab.vue          # User-scoped resumes table
```

### Updated Existing Files

```
frontend/src/
├── router/index.ts         # Add routes for /admin/users, /admin/users/:userId, /admin/ai-models
├── i18n/en.json            # All new EN strings
├── i18n/ru.json            # All new RU strings
```

### Frontend UX Reuse Targets

Admin tables should follow existing User Home saved resumes table behavior for:

- search input style;
- date range style;
- dropdown filter style;
- reset filters logic;
- pagination style;
- sort icons/tooltips;
- row click behavior;
- row hover/highlight;
- modal open/close state management.

Admin User Details should follow Profile page behavior for:

- sidebar/tabs shell style;
- mobile tab behavior if applicable;
- unsaved changes dialog pattern (only for Account tab).

### Route Design

Add frontend routes:

```
/app/admin                  -> AdminHomePage (exists, updated)
/app/admin/users            -> AdminUsersPage (new)
/app/admin/users/:userId    -> AdminUserDetailsPage (new)
/app/admin/ai-models        -> AdminAiModelsWipPage (new)
```

All admin routes must have `meta: { requiresAuth: true, requiresAdmin: true }` matching the existing guard pattern.

No `/app/admin/resumes` full page in this feature.

### Admin Home Behavior

`AdminHomePage.vue` should:

- load dashboard stats via `useAdminDashboard` composable;
- show `AdminStatsCards` with real user/resume counts and WIP token badges;
- show `AdminQuickActions` for navigation;
- show `AdminResumesTable` section directly on the page (not a separate route);
- open resume modal on row click (reuse or adapt existing `ResumeDetailsDialog`);
- support admin resume delete with confirmation and reload.

### Admin Users Behavior

`AdminUsersPage.vue` should:

- load users table via `useAdminUsers` composable;
- support search (shared text field);
- support role/status/permission/rights dropdown filters;
- support account creation date range with `createdFrom`/`createdTo` pickers;
- prevent `createdTo` earlier than `createdFrom` on frontend;
- reset page to first page on any filter/search change;
- open details page by row click (no action column, FR-060);
- row hover/highlight matching User Home table behavior.

### Admin User Details Behavior

`AdminUserDetailsPage.vue` should:

- load details by route `userId` via `useAdminUserDetails` composable;
- detect `isCurrentAdmin` for self-protection UI;
- show tabs in fixed order: Contacts, Account, Additional, Resumes;
- Contacts tab: read-only fields including `resumeEmail` (FR-075a);
- Account tab: editable role/status/permission/rights + read-only `accountEmail` (FR-078a);
- Additional tab: read-only display;
- Resumes tab: independent filters/sort/pagination (no Create Resume button, FR-110);
- warn on unsaved Account changes when switching tabs or leaving page;
- save access update with confirmation dialog;
- delete user with confirmation dialog;
- disable delete user button for current admin with tooltip (FR-094, FR-095);
- prevent self-demotion and self-block in UI where practical.

### AdminAiModelsWipPage Behavior

- Simple localized WIP page with application layout.
- No CRUD controls, no AI provider details, no API keys (FR-116 to FR-119).

---

## Data Model / DTO Mapping

### AdminSavedResumeDto

Source data joins:
- `saved_resumes`
- `users` (for owner username, email)
- `contact_detail` (LEFT JOIN for ownerFullName via `contact_detail.full_name`)
- `language` (for language code, name)

Notes:
- `ownerEmail` comes from `users.email` (account email).
- `ownerFullName` comes from `contact_detail.full_name` (LEFT JOIN, safe fallback if missing).
- If contact detail is missing, the row must still be returned; frontend shows localized empty value.

Required fields (no raw paths):
- `id`, `ownerUserId`, `ownerUsername`, `ownerEmail` (from users.email), `ownerFullName`
- `resumeTitle`, `vacancyTitle`, `companyName`
- `languageCode`, `languageName`, `adaptationLevel`, `createdAt`
- `publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`
- `pdfAvailable`, `pdfStatus`, `pdfMessage`, `coverLetter`

URL rules: reuse existing canonical URL/endpoint patterns from `HomeSavedResumeDto`.

### AdminUserListItemDto

Source data joins:
- `users`
- `contact_detail` (LEFT JOIN for fullName via `contact_detail.full_name`)
- `role`
- `user_status`
- `user_permission`
- `saved_resumes` (COUNT aggregation for resumesCount)

Notes:
- `fullName` comes from `contact_detail.full_name` (LEFT JOIN, safe fallback if missing).
- `email` comes from `users.email` (account email).
- Search over full name must search `contact_detail.full_name`.

Required fields:
- `id`, `fullName`, `username`, `email`
- `roleCode`, `roleName`, `statusCode`, `statusName`
- `permissionCode`, `permissionName`, `isPrivileged`
- `resumesCount`, `createdAt`

### AdminUserDetailsDto

Composed DTO with three sections:

**Contacts** (read-only, from `contact_detail`):
- `fullName` (from `contact_detail.full_name`), `professionalTitle`, `resumeEmail` (from `contact_detail.resume_email`, UI label MUST be `Resume email` / `Email for resume`)
- `phone`, `location`, `linkedinUrl`, `portfolioUrl`, `telegram`, `whatsapp`

**Account** (editable, from `users` + lookup tables):
- `username`, `accountEmail` (from `users.email`, read-only display)
- `roleCode`, `roleName`, `statusCode`, `statusName`
- `permissionCode`, `permissionName`, `isPrivileged`
- `createdAt`, `isCurrentAdmin`

**Additional** (read-only, from `additional_profile_info`):
- `username`, `professionalInfo`, `languages`, `professionalAspirations`
- `achievements`, `aiAdditionalContext`, `dateOfBirth`, `citizenship`

### AdminAccessUpdateRequestDto

Request fields:
- `roleCode` (String, must be USER or ADMIN)
- `statusCode` (String, must be ACTIVE or BLOCKED)
- `permissionCode` (String, must be ALLOWED or FORBIDDEN)
- `isPrivileged` (boolean)

---

## Testing Strategy

### Backend Tests (JUnit 5 + Mockito)

Add tests for:

1. **Admin authorization**:
   - unauthenticated rejected (401) from existing interceptor;
   - non-admin authenticated rejected (403) for `/api/admin/**`;
   - admin allowed.
2. **Dashboard**:
   - totalUsers reflects non-deleted count;
   - totalResumes reflects non-deleted count;
   - token stats hardcoded WIP with WIP flags.
3. **Admin resumes**:
   - paginated list excludes deleted resumes;
   - search applies to resume and owner fields;
   - date range inclusive behavior (startOfDay, beforeNextDay);
   - invalid date range handled safely (no crash);
   - sort whitelist rejects unsupported fields;
   - response has no raw paths.
4. **Admin resume delete**:
   - sets `is_deleted=true` and `deleted_at`;
   - generic failure for missing/already-deleted cases;
   - non-admin rejected.
5. **Admin users**:
   - paginated list excludes deleted users;
   - search over username/email/fullName;
   - filters role/status/permission/rights;
   - account creation date range;
   - sort whitelist;
   - no sensitive fields.
6. **User details**:
   - details load for valid user;
   - deleted/missing user returns safe response;
   - `isCurrentAdmin` flag is correct;
   - contacts tab includes `resumeEmail` from `contact_detail.resume_email`;
   - account tab includes `accountEmail` from `users.email`.
7. **Access update**:
   - update another user succeeds;
   - invalid code rejected;
   - self-demotion rejected (backend);
   - self-block rejected (backend);
   - own generation permission change allowed;
   - own privileged flag change allowed;
   - `404 Not Found` for soft-deleted/non-existent user (FR-092a).
8. **User soft-delete**:
   - self-delete rejected (backend);
   - other user soft-delete succeeds;
   - status becomes BLOCKED;
   - saved resumes soft-deleted;
   - public URL 410 behavior verified after cascade;
   - transaction rollback on failure where testable.
9. **Selected user resumes**:
   - returns only selected user's resumes;
   - filters/sort/pagination work;
   - excludes deleted resumes.

### Frontend Tests (Vitest + Vue Test Utils)

Add tests for:

1. **Admin Home**:
   - stats render with correct labels;
   - WIP badges render;
   - no raw i18n keys;
   - Users card navigates;
   - Resumes card anchors/scrolls;
   - AI Models card navigates.
2. **Admin Resumes table**:
   - filters render;
   - date `createdTo` min bound updates after `createdFrom`;
   - row click opens modal;
   - controls do not trigger row click;
   - reset filters works;
   - delete resume flow reloads table.
3. **Admin Users table**:
   - search/filter/date range controls render;
   - action column absent;
   - row click navigates to details;
   - filter changes reset page;
   - `createdTo` earlier than `createdFrom` blocked.
4. **Admin User Details**:
   - tab order correct (Contacts, Account, Additional, Resumes);
   - Contacts read-only (includes resumeEmail label);
   - Account editable with read-only accountEmail;
   - Additional read-only;
   - Resumes tab independent state;
   - unsaved changes warning appears only for Account changes;
   - self delete button disabled with tooltip;
   - self-demotion/self-block prevented in UI where practical.
5. **AI Models WIP**:
   - WIP page renders localized text;
   - no CRUD controls.

### Manual Evidence

Collect screenshots/traces for:

- `/app/admin` showing dashboard cards + resumes table;
- `/app/admin#resumes` showing resumes section;
- `/app/admin/users` with filters active;
- `/app/admin/users/:userId` Contacts tab;
- `/app/admin/users/:userId` Account tab (editable controls + read-only accountEmail);
- `/app/admin/users/:userId` Additional tab;
- `/app/admin/users/:userId` Resumes tab;
- `/app/admin/ai-models` WIP page;
- resume delete confirmation;
- user delete button disabled for current admin (tooltip);
- unsaved changes warning on Account tab.

---

## Implementation Phases

### Phase 0 — Baseline Inspection

**Goal**: Confirm actual current files, routes, DTOs, tables, and reusable components before coding.

**Recommended agent**: `software_engineering_team_lead` (cross-layer inspection)

Inspect:
- current `AdminHomePage.vue` content and behavior;
- current router admin routes and existing guard;
- current User Home saved resumes table (`SavedResumesTable.vue`) and modal (`ResumeDetailsDialog.vue`);
- current Profile shell and unsaved changes dialog flow;
- current backend auth/session/role model (`AuthInterceptor`, `UserSession`);
- current DAO methods for users, roles, statuses, permissions, resumes;
- current soft-delete fields in repeatable profile tables (`work_experience`, `education`, `project`, `course_certificate`);
- current canonical PDF/HTML endpoints and public URL builder;
- current i18n keys.

> **Commit checkpoint**: Propose using `@git_commit_pr_assistant` (via Serena) to commit baseline inspection findings.

After baseline, report findings and confirm no migration needed before coding.

### Phase 1 — Backend Admin Foundation

**Goal**: Create safe admin backend contract and authorization.

**Recommended agent**: `java_agent`

Implement:
- admin DTOs (all 8 DTOs in `dto/admin/`);
- `AdminController` with read-only endpoints;
- `AdminService` with read-only business logic;
- `AdminDao` with read-only queries;
- backend ADMIN guard in `AuthInterceptor` for `/api/admin/**`;
- dashboard endpoint (`GET /api/admin/dashboard`);
- admin users list endpoint (`GET /api/admin/users`);
- admin resumes list endpoint (`GET /api/admin/resumes`);
- selected user details endpoint (`GET /api/admin/users/{userId}`);
- selected user resumes endpoint (`GET /api/admin/users/{userId}/resumes`).
- backend tests for authorization and read-only endpoints.

> **Commit checkpoint**: Propose using `@git_commit_pr_assistant` (via Serena) to commit backend read-only phase.

STOP after backend read-only APIs pass tests and sample responses show no sensitive fields.

### Phase 2 — Backend Mutations

**Goal**: Implement admin mutations safely.

**Recommended agent**: `java_agent`

Implement:
- `PATCH /api/admin/users/{userId}/access` with self-protection rules;
- `DELETE /api/admin/resumes/{resumeId}` (soft-delete);
- `DELETE /api/admin/users/{userId}` (soft-delete with transaction);
- backend tests for all mutation paths including self-protection rejection;
- generic error responses (SEC-004).

> **Commit checkpoint**: Propose using `@git_commit_pr_assistant` (via Serena) to commit backend mutations.

STOP after mutation tests pass.

### Phase 3 — Frontend Admin Home

**Goal**: Make `/app/admin` useful and consistent.

**Recommended agent**: `vue_agent`

Implement:
- update `AdminHomePage.vue` with dashboard stats cards;
- `AdminStatsCards.vue` with WIP token badges;
- `AdminQuickActions.vue` navigation cards;
- `AdminResumesTable.vue` section on Admin Home (filters/sort/pagination);
- row click opens resume modal (reuse existing modal or admin variant);
- admin resume delete from modal;
- `adminService.ts` API client;
- `useAdminDashboard.ts` composable;
- i18n strings in en.json and ru.json.
- frontend tests.

> **Commit checkpoint**: Propose using `@git_commit_pr_assistant` (via Serena) to commit Admin Home frontend.

STOP after screenshots and frontend tests for Admin Home.

### Phase 4 — Frontend Admin Users

**Goal**: Implement users table.

**Recommended agent**: `vue_agent`

Implement:
- `/app/admin/users` route and `AdminUsersPage.vue`;
- `AdminUsersTable.vue` with search, date range, dropdown filters, sort, pagination;
- `useAdminUsers.ts` composable;
- date picker restriction (createdTo < createdFrom blocked);
- reset filters;
- row click navigation to details;
- i18n strings.

> **Commit checkpoint**: Propose using `@git_commit_pr_assistant` (via Serena) to commit Admin Users frontend.

STOP after screenshots and frontend tests for Admin Users.

### Phase 5 — Frontend Admin User Details

**Goal**: Implement details tabs and account management.

**Recommended agent**: `vue_agent`

Implement:
- `/app/admin/users/:userId` route and `AdminUserDetailsPage.vue`;
- `AdminUserDetailsShell.vue` with sidebar/tabs;
- `AdminUserContactsTab.vue` (read-only, includes resumeEmail label);
- `AdminUserAccountTab.vue` (editable controls + read-only accountEmail);
- `AdminUserAdditionalTab.vue` (read-only);
- `AdminUserResumesTab.vue` (no Create Resume button, independent state);
- `useAdminUserDetails.ts` composable;
- Account save with confirmation;
- unsaved changes warning;
- delete user with confirmation;
- self-delete disabled with tooltip;
- self-demotion/self-block frontend guard where practical;
- i18n strings.

> **Commit checkpoint**: Propose using `@git_commit_pr_assistant` (via Serena) to commit User Details frontend.

STOP after screenshots and frontend tests for User Details.

### Phase 6 — AI Models WIP + Final Verification

**Goal**: Close navigation and verify scope boundaries.

**Recommended agent**: `vue_agent` (for WIP page) then `software_engineering_team_lead` (for final verification)

Implement:
- `/app/admin/ai-models` route and `AdminAiModelsWipPage.vue`;
- WIP page with localized text;
- route guard.

Final verification:
- backend tests: run `mvn test` and verify all pass;
- frontend tests: run `npm run test:unit` and verify all pass;
- manual screenshots for each page/state;
- grep for forbidden sensitive fields in admin DTOs;
- confirm no PDF/AI/generation pipeline files were modified;
- verify `PagedResponse.items` consistency (not `content`);
- verify `resumeEmail` field naming consistency;
- verify `accountEmail` field in Account tab.

> **Commit checkpoint**: Propose using `@git_commit_pr_assistant` (via Serena) for final commit/PR preparation.

---

## STOP Checkpoints

### STOP 1 — Baseline Inspection Evidence

Report:
- current admin route/page state;
- current User Home table/modal reusable behavior;
- current Profile shell/unsaved dialog reusable behavior;
- current backend role/session/admin guard capability;
- current soft-delete fields for relevant tables;
- whether any migration appears necessary;
- exact canonical PDF/HTML endpoints;
- risks/unknowns.

Do not code until user confirms.

### STOP 2 — Backend Read-Only API Evidence

Report:
- changed backend files;
- tests added/updated;
- test command output;
- sample `/api/admin/dashboard` response;
- sample `/api/admin/users` response;
- sample `/api/admin/resumes` response;
- sample `/api/admin/users/{id}` response (verify resumeEmail and accountEmail fields);
- evidence admin auth rejects non-admin;
- evidence no sensitive fields/raw paths.

### STOP 3 — Backend Mutation Evidence

Report:
- access update tests;
- self-demotion/self-block tests;
- resume soft-delete tests;
- user soft-delete tests;
- self-delete rejection test;
- `404 Not Found` for deleted user PATCH test (FR-092a);
- transaction behavior notes;
- generic error response samples.

### STOP 4 — Frontend Admin Home Evidence

Report:
- changed frontend files;
- screenshot `/app/admin`;
- screenshot Admin Resumes section;
- evidence WIP token badges;
- evidence row click modal;
- evidence resume delete flow;
- frontend test output.

### STOP 5 — Frontend Admin Users/User Details Evidence

Report:
- screenshot `/app/admin/users` with filters;
- screenshot user details Contacts tab (with resumeEmail);
- screenshot Account tab (with accountEmail + editable controls);
- screenshot Additional tab;
- screenshot Resumes tab;
- evidence unsaved changes warning;
- evidence self-delete disabled tooltip;
- frontend test output.

### STOP 6 — Final Evidence

Report:
- full backend test command output;
- full frontend test command output;
- final screenshots collection;
- list of modified files;
- confirmation no PDF/AI/generation pipeline files were modified;
- confirmation `PagedResponse.items` naming is consistent;
- unresolved issues if any.

---

## File Structure

### Documentation

```
specs/010-admin-page-users/
├── spec.md
├── plan.md                        # This file
├── tasks.md                       # Generated by /speckit.tasks
├── research.md                    # Phase 0 output
├── data-model.md                  # Phase 1 output
├── quickstart.md                  # Phase 1 output
├── memory-synthesis.md            # Memory context synthesis
├── contracts/
│   └── api-contracts.md           # Phase 1 output
├── checklists/
│   └── requirements.md
└── spec_input_files/
    ├── draft_plan.md
    ├── draft_spec.md
    └── draft_tasks.md
```

### Backend Structure

```
backend/src/main/java/com/resumainer/
├── controller/
│   └── AdminController.java
├── service/
│   └── AdminService.java
├── dao/
│   └── AdminDao.java
└── dto/
    └── admin/
        ├── AdminDashboardDto.java
        ├── AdminSavedResumeDto.java
        ├── AdminUserListItemDto.java
        ├── AdminUserDetailsDto.java
        ├── AdminUserContactsDto.java
        ├── AdminUserAccountDto.java
        ├── AdminUserAdditionalDto.java
        └── AdminAccessUpdateRequestDto.java
```

### Frontend Structure

```
frontend/src/
├── services/
│   └── adminService.ts
├── composables/
│   ├── useAdminDashboard.ts
│   ├── useAdminUsers.ts
│   └── useAdminUserDetails.ts
├── views/
│   ├── AdminHomePage.vue           # Updated
│   ├── AdminUsersPage.vue
│   ├── AdminUserDetailsPage.vue
│   └── AdminAiModelsWipPage.vue
└── components/
    └── admin/
        ├── AdminStatsCards.vue
        ├── AdminQuickActions.vue
        ├── AdminResumesTable.vue
        ├── AdminUsersTable.vue
        ├── AdminUserDetailsShell.vue
        ├── AdminUserContactsTab.vue
        ├── AdminUserAccountTab.vue
        ├── AdminUserAdditionalTab.vue
        └── AdminUserResumesTab.vue
```

---

## Risk Register

| Risk | Severity | Mitigation |
|---|---|---|
| DeepSeek expands into AI Models CRUD | High | Explicit WIP only; tasks must mark AI CRUD out of scope. |
| Admin authorization implemented only on frontend | High | Backend ADMIN guard required and tested (SEC-001). |
| Raw paths leak through admin resume DTO | High | Dedicated DTO; tests/grep for path fields (SEC-002). |
| SQL injection through dynamic sort | High | Sort whitelist mapping only (SEC-003). |
| User soft-delete requires migrations | Medium | Do not migrate unless user approves; skip contact/additional soft-delete if schema lacks fields. |
| Existing User Home components become broken during reuse | High | Prefer admin-specific wrappers; run existing User Home tests. |
| Unsaved changes warning becomes global/noisy | Medium | Apply only for Account tab dirty state. |
| Self-lockout by admin | High | Backend rejects self-delete, self-demotion, self-block (SEC-006). |
| Date range off-by-one bugs | Medium | Use start-of-day and next-day-exclusive semantics. |
| Admin Resumes accidentally built as separate page | Medium | Explicitly keep full table on `/app/admin`. |
| resumeEmail/accountEmail field confusion | Medium | Clear field naming: Contacts.resumeEmail (contact_detail), Account.accountEmail (users.email). Verify in API tests. |
| PagedResponse field name mismatch (content vs items) | Low | All admin endpoints must use `items` field to match existing `PagedResponse<T>`. |

---

## Assumptions

1. No new database migrations needed — all required tables and columns already exist.
2. The existing `PagedResponse<T>` model (field `items`) is reused for all admin list endpoints.
3. Soft-delete fields (`is_deleted`, `deleted_at`) already exist on `users`, `saved_resumes`, and some repeatable profile tables (check during baseline).
4. `role`, `user_status`, `user_permission` lookup tables exist with codes `ADMIN/USER`, `ACTIVE/BLOCKED`, `ALLOWED/FORBIDDEN`.
5. Existing public route (`/{username}/{publicCode}`) already returns `410 Gone` for soft-deleted resumes (Feature 009).
6. Existing PDF/HTML download endpoints and URL patterns are reused.
7. The existing AuthInterceptor handles authentication; ADMIN role check will be added.
8. The global navigation guard already handles frontend route protection for admin routes.
9. No modal-details API endpoint is needed — resume details modal uses already-loaded table DTO data.

---

## Final Acceptance Gate

Implementation is accepted only if:

1. Admin Home shows dashboard and all-resumes table directly on `/app/admin`.
2. Users card opens `/app/admin/users`.
3. Resumes card anchors/scrolls to Admin Resumes section on `/app/admin`.
4. AI Models card opens WIP page.
5. Admin Users table supports required search/filter/date/sort/pagination behavior.
6. User Details opens by row click and uses correct tab order.
7. Account tab can update access controls with confirmation (and shows read-only accountEmail).
8. Contacts tab displays resumeEmail from contact_detail with correct label.
9. Self-delete, self-demotion, and self-block are impossible through backend.
10. Admin can soft-delete another user.
11. Admin can soft-delete resumes from Admin Home and User Details Resumes tab.
12. `PATCH /api/admin/users/{userId}/access` on deleted user returns `404 Not Found`.
13. All new strings are localized EN/RU.
14. Backend tests pass.
15. Frontend tests pass.
16. No sensitive fields or raw paths are exposed.
17. No PDF/AI/generation pipeline changes are made.
