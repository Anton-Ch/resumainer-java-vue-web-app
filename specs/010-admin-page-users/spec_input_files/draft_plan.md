# Implementation Plan: Admin Console Users and Resumes

**Branch**: `feat/010-admin-page-users` | **Date**: 2026-06-26 | **Spec**: `specs/010-admin-page-users/spec.md`

**Input**: Feature specification for `feat/010-admin-page-users`: Admin Home dashboard with all-resumes moderation table, Admin Users list, Admin User Details with tabs, account access management, admin soft-delete for resumes and users, and AI Models WIP placeholder.

> **Instruction for DeepSeek / OpenCode**: This plan is a controlled implementation guide. If any item conflicts with the actual current codebase, STOP and ask before changing architecture, adding migrations, or inventing workarounds. Do not expand scope beyond this plan.

---

## Summary

Implement the MVP Admin Console for ResumAIner:

- `/app/admin` becomes a real Admin Home dashboard.
- Admin Home shows:
  - real total users count;
  - real total resumes count;
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
- Admin can soft-delete resumes.
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
- Existing tables expected to be sufficient:
  - `users`
  - `role`
  - `user_status`
  - `user_permission`
  - `contact_detail`
  - `additional_profile_info`
  - `saved_resumes`
  - repeatable profile tables such as work experience, education, projects, courses where soft-delete already exists

**Testing**:

- Backend: JUnit 5 + Mockito, controller/service/DAO tests where practical.
- Frontend: Vitest + Vue Test Utils for component/composable behavior.
- Manual/E2E evidence: screenshots for `/app/admin`, `/app/admin/users`, `/app/admin/users/:userId`, `/app/admin/ai-models`.

---

## Scope Decisions

### In Scope

1. Admin Home dashboard at `/app/admin`.
2. Admin Resumes table directly on `/app/admin`.
3. Admin Users page at `/app/admin/users`.
4. Admin User Details page at `/app/admin/users/:userId`.
5. User Details tabs:
   - Contacts read-only;
   - Account editable;
   - Additional read-only;
   - Resumes table for selected user.
6. Admin resume details modal behavior based on existing `ResumeDetailsDialog`.
7. Admin resume soft-delete.
8. Admin user soft-delete.
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

---

## Key Product Decisions

### Admin Home Resumes Location

Admin Resumes full table MUST live on `/app/admin`, not on a separate `/app/admin/resumes` page.

The Resumes quick card on Admin Home should scroll or anchor to the resumes section on the same page, for example `/app/admin#resumes`.

### Token Stats

Token stats are intentionally WIP:

- `totalTokensSent = 0`
- `totalTokensGenerated = 0`
- both cards show WIP badge

Do not query `ai_usage_log` for real token stats in this feature.

### Admin Users Search

Admin Users uses one shared text search field over:

- `username`
- `email`
- `fullName`

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

Contacts and Additional tabs are read-only.

### Self-Protection

Admin cannot:

- delete own account;
- change own role from `ADMIN` to `USER`;
- change own status from `ACTIVE` to `BLOCKED`.

Admin can:

- change own generation permission;
- change own privileged flag.

Frontend should prevent disallowed self-actions for UX, but backend validation is mandatory.

---

## Security Review Findings to Implement

### SEC-001 — Backend Admin Authorization

Every `/api/admin/**` endpoint MUST enforce ADMIN role on the backend.

Frontend route guards are not enough.

Expected behavior:

- unauthenticated request: rejected according to existing authentication behavior;
- authenticated non-admin request: rejected with safe forbidden response;
- authenticated admin request: allowed.

### SEC-002 — Sensitive Data Exclusion

Admin DTOs MUST NOT expose:

- `password_hash`
- raw filesystem paths
- storage directories
- local PDF/HTML file paths
- API keys
- encrypted API keys
- internal exception details

### SEC-003 — SQL Sort Whitelist

All sortable fields MUST use whitelist mapping.

Never inject raw `sort` query params into SQL `ORDER BY`.

### SEC-004 — Generic Delete Errors

Delete failures must not leak whether a target ID was non-existent, non-owned, already deleted, or failed for an internal reason.

Backend may log details server-side. Frontend must show generic localized error.

### SEC-005 — Soft Delete Only

Delete resume and delete user must be soft-delete only.

No hard delete in this feature.

### SEC-006 — User Soft-Delete Transaction

User soft-delete should run in a single JDBC transaction:

1. mark user `is_deleted = true`;
2. set `users.deleted_at`;
3. set status to `BLOCKED`;
4. soft-delete user's saved resumes;
5. soft-delete related repeatable profile records only where tables already support soft-delete.

Do not add migrations only to soft-delete contact/additional records.

---

## Constitution Check

*GATE: Must pass before task breakdown. Re-check if design changes.*

| Principle | Status | Notes |
|---|---|---|
| **I. Code Quality & Maintainability** | PASS | Keep layered controller/service/DAO architecture. Add dedicated admin DTOs. Do not return raw entities. Do not modify unrelated PDF/AI/generation code. |
| **II. Testing Excellence** | PASS | Add targeted backend tests for admin authorization, dashboard, users/resumes queries, access update, self-protection, and soft-delete. Add frontend tests for filters, row click, tabs, unsaved warning, and WIP page. |
| **III. User Experience Consistency** | PASS | Reuse User Home saved resumes table UX patterns: search, date range, dropdown filters, reset filters, sort icons/tooltips, row hover/highlight, pagination. Reuse Profile unsaved-changes UX pattern for Account tab. |
| **IV. Performance & Reliability** | PASS | Use paginated backend queries. No modal-details API unless approved. Use JDBC transaction for user soft-delete. Use safe validation for invalid date ranges. |
| **V. Security by Design** | PASS | Backend ADMIN check on all admin APIs. No sensitive fields. Sort whitelist. Soft-delete only. Self-delete/self-demotion/self-block forbidden. |

**Gate result**: PASS — no known constitution violations.

---

## API Design

### 1. Dashboard

```text
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

```text
GET /api/admin/resumes?page=0&size=10&search=&language=&adaptationLevel=&createdFrom=&createdTo=&sort=createdAt,desc
```

Search covers:

- resume title;
- vacancy title;
- company name;
- owner username;
- owner email;
- owner full name where available.

Response: `PagedResponse<AdminSavedResumeDto>`.

```json
{
  "content": [
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

```text
DELETE /api/admin/resumes/{resumeId}
```

Success:

```json
{
  "message": "Resume deleted successfully."
}
```

---

### 3. Admin Users

```text
GET /api/admin/users?page=0&size=10&search=&role=&status=&permission=&rights=&createdFrom=&createdTo=&sort=createdAt,desc
```

Search covers:

- username;
- email;
- full name.

Filters:

- role: `ALL / USER / ADMIN`
- status: `ALL / ACTIVE / BLOCKED`
- permission: `ALL / ALLOWED / FORBIDDEN`
- rights: `ALL / PRIVILEGED / NON_PRIVILEGED`
- account creation date range: `createdFrom`, `createdTo`

Response: `PagedResponse<AdminUserListItemDto>`.

```json
{
  "content": [
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

```text
GET /api/admin/users/{userId}
```

Response: `AdminUserDetailsDto`.

```json
{
  "id": "uuid",
  "contacts": {
    "fullName": "John Doe",
    "professionalTitle": "Backend Developer",
    "email": "john@example.com",
    "phone": "+7...",
    "location": "Almaty",
    "linkedinUrl": "https://linkedin.com/in/johndoe",
    "portfolioUrl": "https://johndoe.dev",
    "telegram": "@johndoe",
    "whatsapp": "+7..."
  },
  "account": {
    "username": "johndoe",
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

```text
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

Success:

```json
{
  "message": "User access updated successfully."
}
```

Self-protection error:

```json
{
  "message": "This action is not allowed for your own admin account."
}
```

Delete user:

```text
DELETE /api/admin/users/{userId}
```

Success:

```json
{
  "message": "User account deleted successfully."
}
```

---

### 5. Selected User Resumes

```text
GET /api/admin/users/{userId}/resumes?page=0&size=10&search=&language=&adaptationLevel=&createdFrom=&createdTo=&sort=createdAt,desc
```

Response: `PagedResponse<AdminSavedResumeDto>`, scoped to the selected user.

---

## Backend Architecture

### Expected New Files

```text
backend/src/main/java/com/resumainer/controller/AdminController.java
backend/src/main/java/com/resumainer/service/AdminService.java
backend/src/main/java/com/resumainer/dao/AdminDao.java
backend/src/main/java/com/resumainer/dto/admin/AdminDashboardDto.java
backend/src/main/java/com/resumainer/dto/admin/AdminSavedResumeDto.java
backend/src/main/java/com/resumainer/dto/admin/AdminUserListItemDto.java
backend/src/main/java/com/resumainer/dto/admin/AdminUserDetailsDto.java
backend/src/main/java/com/resumainer/dto/admin/AdminUserContactsDto.java
backend/src/main/java/com/resumainer/dto/admin/AdminUserAccountDto.java
backend/src/main/java/com/resumainer/dto/admin/AdminUserAdditionalDto.java
backend/src/main/java/com/resumainer/dto/admin/AdminAccessUpdateRequestDto.java
```

### Possible Updates to Existing Files

```text
backend/src/main/java/com/resumainer/interceptor/AuthInterceptor.java
backend/src/main/java/com/resumainer/config/WebConfig.java
backend/src/main/java/com/resumainer/dao/ResumeDao.java
backend/src/main/java/com/resumainer/dao/UserDao.java
backend/src/main/resources/messages.properties
backend/src/main/resources/messages_ru.properties
```

Exact changes depend on current code inspection.

### Backend ADMIN Authorization Options

Preferred implementation after inspection:

1. Add backend ADMIN check in `AuthInterceptor` for `/api/admin/**`, if interceptor already has access to `UserSession` and role.
2. Or add explicit `requireAdmin()` in `AdminController`/`AdminService` for every admin endpoint.

Either way, every `/api/admin/**` endpoint must reject non-admin users.

Do not rely only on frontend router metadata.

### AdminDao Responsibilities

`AdminDao` should provide:

- dashboard counts;
- paginated all-resumes query;
- all-resumes total count;
- admin resume soft-delete;
- paginated users query;
- users total count;
- user details query;
- user access update;
- user soft-delete transaction helpers;
- selected user's resumes query;
- selected user's resumes total count.

### Sort Whitelist Examples

Admin Users allowed sort fields:

```text
fullName
username
email
role
status
permission
rights
resumesCount
createdAt
```

Admin Resumes allowed sort fields:

```text
resumeTitle
vacancyTitle
companyName
language
adaptationLevel
createdAt
ownerUsername
ownerEmail
ownerFullName
```

Implementation must map each public sort key to a fixed SQL expression.

### Date Range Handling

Use safe backend parsing:

- accepted date format should match existing project conventions;
- invalid dates should return a safe validation error;
- `createdFrom` maps to start of day;
- `createdTo` maps to next day start exclusive;
- invalid range must not crash.

### User Soft-Delete Transaction

Pseudo-sequence:

```text
BEGIN TRANSACTION
  verify current user is ADMIN
  verify target user exists and is not deleted
  verify target user is not current admin
  update users set is_deleted=true, deleted_at=CURRENT_TIMESTAMP, status_id=(BLOCKED id)
  update saved_resumes set is_deleted=true, deleted_at=CURRENT_TIMESTAMP where user_id=? and is_deleted=false
  soft-delete repeatable profile records where tables support is_deleted/deleted_at
COMMIT
```

Rollback on any failure.

Do not add schema changes for contact/additional profile soft-delete.

---

## Frontend Architecture

### Expected New Files

```text
frontend/src/services/adminService.ts
frontend/src/composables/useAdminDashboard.ts
frontend/src/composables/useAdminUsers.ts
frontend/src/composables/useAdminUserDetails.ts
frontend/src/views/AdminUsersPage.vue
frontend/src/views/AdminUserDetailsPage.vue
frontend/src/views/AdminAiModelsWipPage.vue
frontend/src/components/admin/AdminStatsCards.vue
frontend/src/components/admin/AdminQuickActions.vue
frontend/src/components/admin/AdminResumesTable.vue
frontend/src/components/admin/AdminUsersTable.vue
frontend/src/components/admin/AdminUserDetailsShell.vue
frontend/src/components/admin/AdminUserContactsTab.vue
frontend/src/components/admin/AdminUserAccountTab.vue
frontend/src/components/admin/AdminUserAdditionalTab.vue
frontend/src/components/admin/AdminUserResumesTab.vue
```

### Expected Existing Files to Update

```text
frontend/src/views/AdminHomePage.vue
frontend/src/router/index.ts
frontend/src/i18n/en.json
frontend/src/i18n/ru.json
```

Possibly update/reuse:

```text
frontend/src/components/home/ResumeDetailsDialog.vue
frontend/src/components/home/SavedResumesTable.vue
frontend/src/composables/useUserHome.ts
frontend/src/services/userHomeService.ts
```

Do not break existing User Home behavior.

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
- unsaved changes dialog pattern.

### Route Design

Add/verify frontend routes:

```text
/app/admin                  -> AdminHomePage
/app/admin/users            -> AdminUsersPage
/app/admin/users/:userId    -> AdminUserDetailsPage
/app/admin/ai-models        -> AdminAiModelsWipPage
```

All admin routes must have `requiresAdmin` metadata or equivalent existing guard.

No `/app/admin/resumes` full page in this feature.

### Admin Home Behavior

`AdminHomePage.vue` should:

- load dashboard stats;
- load all-resumes table state;
- show quick cards;
- keep Admin Resumes section on the same page;
- open resume modal on row click;
- support admin resume delete and reload relevant data.

### Admin Users Behavior

`AdminUsersPage.vue` should:

- load users table;
- support filters;
- support account creation date range;
- reset page to first page on filter/search change;
- open details by row click;
- avoid action column.

### Admin User Details Behavior

`AdminUserDetailsPage.vue` should:

- load details by route `userId`;
- show tabs in fixed order;
- show Contacts read-only;
- show Account editable;
- show Additional read-only;
- show Resumes tab with independent filters/sort/pagination;
- warn on unsaved Account changes when switching tabs/leaving;
- support save access with confirmation;
- support delete user with confirmation;
- disable delete user button for current admin account with tooltip;
- reject/disable self-demotion and self-block in UI where practical.

---

## Data Model / DTO Mapping

### AdminSavedResumeDto

Source data likely joins:

- `saved_resumes`
- `users`
- `contact_detail`
- `language`

Required fields:

```text
id
ownerUserId
ownerUsername
ownerEmail
ownerFullName
resumeTitle
vacancyTitle
companyName
languageCode
languageName
adaptationLevel
createdAt
publicUrlLink
pdfOpenUrl
pdfDownloadUrl
htmlDownloadUrl
pdfAvailable
pdfStatus
pdfMessage
coverLetter
```

URL rules:

- PDF open: existing authenticated endpoint with inline disposition.
- PDF download: existing authenticated PDF endpoint.
- HTML download: existing authenticated HTML endpoint.
- Public URL: reuse existing public URL generation behavior if available.

No raw local paths.

### AdminUserListItemDto

Source data likely joins:

- `users`
- `role`
- `user_status`
- `user_permission`
- `contact_detail`
- `saved_resumes` aggregation

Required fields:

```text
id
fullName
username
email
roleCode
roleName
statusCode
statusName
permissionCode
permissionName
isPrivileged
resumesCount
createdAt
```

### AdminUserDetailsDto

Composed DTO:

```text
id
contacts
account
additional
```

Contacts fields:

```text
fullName
professionalTitle
email
phone
location
linkedinUrl
portfolioUrl
telegram
whatsapp
```

Account fields:

```text
username
roleCode
roleName
statusCode
statusName
permissionCode
permissionName
isPrivileged
createdAt
isCurrentAdmin
```

Additional fields:

```text
username
professionalInfo
languages
professionalAspirations
achievements
aiAdditionalContext
dateOfBirth
citizenship
```

If a field does not exist exactly under this name in the current schema/model, map the closest existing field only after inspection. If ambiguous, STOP and ask.

---

## Testing Strategy

### Backend Tests

Add tests for:

1. Admin authorization:
   - unauthenticated rejected;
   - non-admin rejected;
   - admin allowed.
2. Dashboard:
   - real user count;
   - real resume count;
   - token stats hardcoded WIP.
3. Admin resumes:
   - paginated list excludes deleted resumes;
   - search applies to resume and owner fields;
   - date range inclusive behavior;
   - invalid date range handled safely;
   - sort whitelist rejects/ignores unsupported fields;
   - response has no raw paths.
4. Admin resume delete:
   - sets `is_deleted=true` and `deleted_at`;
   - generic failure for missing/already-deleted cases;
   - non-admin rejected.
5. Admin users:
   - paginated list excludes deleted users;
   - search over username/email/fullName;
   - filters role/status/permission/rights;
   - account creation date range;
   - sort whitelist;
   - no sensitive fields.
6. User details:
   - details load for valid user;
   - deleted/missing user safe response;
   - current admin flag is correct.
7. Access update:
   - update another user succeeds;
   - invalid code rejected;
   - self-demotion rejected;
   - self-block rejected;
   - own generation permission change allowed;
   - own privileged flag change allowed.
8. User soft-delete:
   - self-delete rejected;
   - other user soft-delete succeeds;
   - status becomes BLOCKED;
   - saved resumes soft-deleted;
   - transaction rollback on failure where testable.
9. Selected user resumes:
   - returns only selected user's resumes;
   - filters/sort/pagination work;
   - excludes deleted resumes.

### Frontend Tests

Add tests for:

1. Admin Home:
   - stats render;
   - WIP badges render;
   - no raw i18n keys;
   - Users card navigation;
   - Resumes card anchors/scrolls;
   - AI Models card navigation.
2. Admin Resumes table:
   - filters render;
   - date `createdTo` min bound updates after `createdFrom`;
   - row click opens modal;
   - controls do not trigger row click;
   - reset filters works;
   - delete resume flow reloads table.
3. Admin Users table:
   - search/filter/date range controls render;
   - action column absent;
   - row click navigates to details;
   - filter changes reset page;
   - createdTo earlier than createdFrom blocked.
4. Admin User Details:
   - tab order correct;
   - Contacts read-only;
   - Account editable;
   - Additional read-only;
   - Resumes tab independent state;
   - unsaved changes warning appears only for Account changes;
   - self delete button disabled with tooltip;
   - self-demotion/self-block prevented in UI where practical.
5. AI Models WIP:
   - WIP page renders localized text;
   - no CRUD controls.

### Manual Evidence

Collect screenshots/traces for:

```text
/app/admin
/app/admin#resumes
/app/admin/users
/app/admin/users/:userId Contacts tab
/app/admin/users/:userId Account tab
/app/admin/users/:userId Additional tab
/app/admin/users/:userId Resumes tab
/app/admin/ai-models
```

Also capture:

- resume delete confirmation;
- user delete button disabled for current admin;
- unsaved changes warning on Account tab.

---

## Implementation Phases

### Phase 0 — Baseline Inspection

Goal: confirm actual current files, routes, DTOs, tables, and reusable components before coding.

Inspect:

- current `AdminHomePage.vue`;
- current router admin routes and guards;
- current User Home saved resumes table and modal flow;
- current Profile shell and unsaved changes dialog flow;
- current backend auth/session/role model;
- current DAO methods for users, roles, statuses, permissions, resumes;
- current soft-delete fields in repeatable profile tables;
- current canonical PDF/HTML endpoints;
- current public URL builder if any;
- current i18n keys.

STOP after baseline and report findings.

### Phase 1 — Backend Admin Foundation

Goal: create safe admin backend contract and authorization.

Implement:

- admin DTOs;
- `AdminController`;
- `AdminService`;
- `AdminDao`;
- backend ADMIN guard for `/api/admin/**`;
- dashboard endpoint;
- admin users list endpoint;
- admin resumes list endpoint;
- selected user details endpoint;
- selected user resumes endpoint.

STOP after backend read-only APIs pass tests and sample responses show no sensitive fields.

### Phase 2 — Backend Mutations

Goal: implement admin mutations safely.

Implement:

- `PATCH /api/admin/users/{userId}/access`;
- `DELETE /api/admin/resumes/{resumeId}`;
- `DELETE /api/admin/users/{userId}`;
- self-protection rules;
- transaction for user soft-delete;
- generic error responses.

STOP after mutation tests pass.

### Phase 3 — Frontend Admin Home

Goal: make `/app/admin` useful and consistent.

Implement:

- dashboard stats cards;
- WIP token badges;
- quick action cards;
- Admin Resumes section/table on the same page;
- filters/sort/pagination;
- row click modal;
- admin resume delete from modal;
- i18n.

STOP after screenshots and frontend tests for Admin Home.

### Phase 4 — Frontend Admin Users

Goal: implement users table.

Implement:

- `/app/admin/users` route/page;
- shared search;
- account creation date range;
- role/status/permission/rights filters;
- reset filters;
- sorting/tooltips;
- pagination;
- row click navigation;
- i18n.

STOP after screenshots and frontend tests for Admin Users.

### Phase 5 — Frontend Admin User Details

Goal: implement details tabs and account management.

Implement:

- `/app/admin/users/:userId` route/page;
- details shell/sidebar/tabs;
- Contacts tab;
- Account tab;
- Additional tab;
- Resumes tab;
- Account save confirmation;
- unsaved changes warning;
- delete user confirmation;
- self-delete disabled tooltip;
- self-demotion/self-block frontend guard where practical;
- i18n.

STOP after screenshots and frontend tests for User Details.

### Phase 6 — AI Models WIP + Final Verification

Goal: close navigation and verify scope boundaries.

Implement:

- `/app/admin/ai-models` WIP page;
- route guard;
- i18n.

Final verification:

- backend tests;
- frontend tests;
- manual screenshots;
- grep for forbidden sensitive fields in DTOs;
- confirm no PDF/AI/generation pipeline files were modified.

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
- sample `/api/admin/users/{id}` response;
- evidence admin auth rejects non-admin;
- evidence no sensitive fields/raw paths.

### STOP 3 — Backend Mutation Evidence

Report:

- access update tests;
- self-demotion/self-block tests;
- resume soft-delete tests;
- user soft-delete tests;
- self-delete rejection test;
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

- screenshot `/app/admin/users`;
- screenshot filters/date range;
- screenshot user details Contacts tab;
- screenshot Account tab;
- screenshot Additional tab;
- screenshot Resumes tab;
- evidence unsaved changes warning;
- evidence self-delete disabled tooltip;
- frontend test output.

### STOP 6 — Final Evidence

Report:

- full backend test command output;
- full frontend test command output;
- final screenshots;
- list of modified files;
- confirmation no PDF/AI/generation pipeline files were modified;
- unresolved issues if any.

---

## File Structure

### Documentation

```text
specs/010-admin-page-users/
├── spec.md
├── plan.md
├── tasks.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── api-contracts.md
└── checklists/
    └── requirements.md
```

### Backend Expected Structure

```text
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

### Frontend Expected Structure

```text
frontend/src/
├── services/
│   └── adminService.ts
├── composables/
│   ├── useAdminDashboard.ts
│   ├── useAdminUsers.ts
│   └── useAdminUserDetails.ts
├── views/
│   ├── AdminHomePage.vue
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
|---|---:|---|
| DeepSeek expands into AI Models CRUD | High | Explicit WIP only; tasks must mark AI CRUD out of scope. |
| Admin authorization implemented only on frontend | High | Backend ADMIN guard required and tested. |
| Raw paths leak through admin resume DTO | High | Dedicated DTO; tests/grep for path fields. |
| SQL injection through dynamic sort | High | Sort whitelist mapping only. |
| User soft-delete requires migrations | Medium | Do not migrate unless user approves; skip contact/additional soft-delete if schema lacks fields. |
| Existing User Home components become broken during reuse | High | Prefer admin-specific wrappers; run existing User Home tests. |
| Unsaved changes warning becomes global/noisy | Medium | Apply only for Account tab dirty state. |
| Self-lockout by admin | High | Backend rejects self-delete, self-demotion, self-block. |
| Date range off-by-one bugs | Medium | Use start-of-day and next-day-exclusive semantics. |
| Admin Resumes accidentally built as separate page | Medium | Explicitly keep full table on `/app/admin`. |

---

## Final Acceptance Gate

Implementation is accepted only if:

1. Admin Home shows dashboard and all-resumes table directly on `/app/admin`.
2. Users card opens `/app/admin/users`.
3. Resumes card anchors/scrolls to Admin Resumes section on `/app/admin`.
4. AI Models card opens WIP page.
5. Admin Users table supports required search/filter/date/sort/pagination behavior.
6. User Details opens by row click and uses correct tab order.
7. Account tab can update access controls with confirmation.
8. Self-delete, self-demotion, and self-block are impossible through backend.
9. Admin can soft-delete another user.
10. Admin can soft-delete resumes from Admin Home and User Details Resumes tab.
11. All new strings are localized EN/RU.
12. Backend tests pass.
13. Frontend tests pass.
14. No sensitive fields or raw paths are exposed.
15. No PDF/AI/generation pipeline changes are made.
