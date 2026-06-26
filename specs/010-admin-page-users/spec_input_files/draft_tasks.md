---
description: "Task breakdown for feat/010-admin-page-users"
---

# Tasks: Admin Console Users and Resumes

**Feature Branch**: `feat/010-admin-page-users`  
**Input**: Design documents from `specs/010-admin-page-users/`  
**Primary Documents**: `spec.md` and `plan.md` for Admin Console Users and Resumes.

> **Instruction for DeepSeek / OpenCode**: This task file is intentionally strict. Follow task order, stop at every `[STOP]`, use MCP tools as required, and do not expand scope. If current code conflicts with this task list, STOP and report the conflict before editing. Do not silently reinterpret requirements.

---

## Global Execution Rules

### Required MCP Usage

1. **Context7 MCP is mandatory before each logical task group**
   - Before starting a backend group, refresh relevant docs through Context7 MCP: Java 21, Spring MVC, JUnit 5, Mockito, JDBC, transactions, validation, or whichever APIs are involved.
   - Before starting a frontend group, refresh relevant docs through Context7 MCP: Vue 3 Composition API, Vue Router, PrimeVue DataTable/Dialog/Dropdown/DatePicker/Toast/Tooltip, Vitest, Vue Test Utils, vue-i18n.
   - Before starting testing/evidence groups, refresh relevant docs through Context7 MCP: Vitest coverage, JUnit/Mockito, Playwright MCP usage patterns.
   - Report briefly which docs were checked and which decisions they affected.

2. **Serena MCP is mandatory for code navigation and edits**
   - Use Serena MCP tools actively instead of manual `grep`/blind file scanning.
   - Required Serena workflow for each code group: inspect symbols, find references, understand call graph, then edit.
   - Manual grep/search is allowed only when Serena cannot answer the query; if used, report why Serena was insufficient.
   - Do not do blind broad edits. Use symbol-level reasoning.

3. **Playwright MCP only for browser/e2e checks**
   - Do not install Playwright.
   - Use the already available Playwright MCP for navigation, screenshots, locators, traces, downloads/popups if needed, and browser evidence.

4. **Postgres MCP for database inspection or verification**
   - If schema, lookup codes, seed data, row counts, or mutation verification are needed, use the available Postgres MCP.
   - Do not guess database schema.
   - Do not create migrations unless baseline inspection proves they are required and the user explicitly approves.

5. **spec-kit-memory MCP for lessons learned**
   - If a valuable lesson is discovered for local memory or cross-project memory, propose it to the user first.
   - Wait for explicit user confirmation before writing to memory.
   - Never write memory silently.

6. **Git commits are not automatic**
   - After each logically completed task group, propose whether to call subagent `@git_commit_pr_assistant`.
   - Do not commit without user permission.
   - If user approves use of `@git_commit_pr_assistant`, pass this instruction inside the subagent prompt: **"Using Serena MCP tools is mandatory; inspect changed symbols and references before preparing commit/PR summary."**

### Agent Switching Guidance

Before each logical task group, recommend the best agent context:

- For backend Java/Spring/JDBC/JUnit work: recommend switching to a Java/backend-focused agent.
- For frontend Vue/PrimeVue/Vitest work: recommend switching to a Vue/frontend-focused agent.
- For e2e/manual evidence: recommend Playwright/testing-focused agent.
- Do not switch agents automatically unless the user confirms or current workflow supports it explicitly.

### Engineering Principles

- **KISS is mandatory**: Keep it simple, stupid. Code must be simple, understandable, maintainable, and safe.
- Avoid overengineering: no new frameworks, no unnecessary abstractions, no speculative architecture.
- Professional does not mean complicated. Prefer clear DTOs, clear services, clear DAO SQL, clear tests.
- Use existing project patterns wherever practical.
- New/changed code should be understandable to a reviewer in a capstone defense.

### TDD and Coverage Rules

- Apply **TDD** for all meaningful backend and frontend behavior:
  1. write or update failing test first;
  2. implement minimal code to pass;
  3. refactor only if it makes code simpler.
- Target **80%+ coverage for new/changed feature code**.
- Do not lower existing coverage.
- If global repository coverage is below 80% because of legacy code, report baseline and prove that new/changed classes/components are covered at 80%+ where measurable.
- Tests must cover positive, negative, boundary, and security/self-protection cases.

### Anti-Patterns Strictly Forbidden

- Do not modify PDF renderer, PDF fitting engine, PDF validation, PDF templates, AI generation, prompt builder, parser, OpenRouter client, finalization pipeline, or budget config.
- Do not implement AI Models CRUD.
- Do not implement audit log.
- Do not add real token aggregation; token stats are WIP hardcoded `0` with WIP badge.
- Do not create `/app/admin/resumes` as a full page. Admin Resumes table lives on `/app/admin`.
- Do not add action/open-details column to Admin Users table.
- Do not expose `password_hash`, API keys, encrypted API keys, raw filesystem paths, storage paths, or internal exception details.
- Do not use raw entity objects as API DTOs.
- Do not hardcode role/status/permission database IDs. Use lookup codes such as `ADMIN`, `USER`, `ACTIVE`, `BLOCKED`, `ALLOWED`, `FORBIDDEN`.
- Do not inject raw sort query parameters into SQL. Use whitelist mapping only.
- Do not create migrations unless user explicitly approves after evidence.
- Do not add dependencies unless user explicitly approves.
- Do not hard-delete users, resumes, or profile data.
- Do not swallow exceptions with empty catch blocks.
- Do not make huge all-in-one Vue components if clear smaller components already fit existing project patterns.
- Do not copy props into local `ref` once and create stale modal state bugs. Use proper Vue controlled-state patterns.
- Do not duplicate unrelated user-home code blindly; extract/reuse only when it stays simple.
- Do not proceed past `[STOP]` checkpoints without user confirmation.

---

## Execution Markers

| Marker | Meaning |
|---|---|
| `[CTX7]` | Must use Context7 MCP before/while executing this task group |
| `[SERENA]` | Must use Serena MCP for symbol/reference navigation and edits |
| `[PG-MCP]` | Must use Postgres MCP for schema/data verification |
| `[PW-MCP]` | Must use Playwright MCP, not local Playwright install |
| `[MEMORY]` | Consider spec-kit-memory MCP, but ask user before writing memory |
| `[AGENT]` | Recommend best agent switch before starting the task group |
| `[TDD]` | Write/update failing tests first |
| `[SEC]` | Security-sensitive task |
| `[KISS]` | Keep implementation simple and maintainable |
| `[STOP]` | Stop and wait for user confirmation before continuing |
| `[EVIDENCE]` | Provide concrete evidence: files, test output, API samples, screenshots/traces |
| `[NO-PDF-AI]` | Must not touch PDF/AI/generation internals |

---

## Phase 0 — Context Loading and Baseline Inspection

**Goal**: Understand current code before editing. Confirm existing patterns and avoid breaking stable flows.

**Before starting**:
- [ ] Recommend switching to a general codebase-analysis agent.
- [ ] [CTX7] Refresh docs for Vue 3 Composition API, Vue Router, PrimeVue DataTable/Dialog/DatePicker/Dropdown/Tooltip/Toast, Spring MVC, JUnit 5, Mockito, JDBC transactions.
- [ ] [SERENA] Use Serena MCP to inspect project structure, symbols, and references. Do not rely on manual grep unless Serena is insufficient.
- [ ] [PG-MCP] Use Postgres MCP if schema/lookup codes need verification.
- [ ] [KISS] Keep baseline report short and factual.

### Tasks

- [ ] T001 [SERENA] Inspect frontend routing: current `/admin` route, `requiresAdmin` guard, and missing admin child routes.
- [ ] T002 [SERENA] Inspect `AdminHomePage.vue` and document current hardcoded stats, untranslated keys, and empty navigation behavior.
- [ ] T003 [SERENA] Inspect existing User Home saved resumes flow: `UserHomePage.vue`, `SavedResumesTable.vue`, `ResumeDetailsDialog.vue`, related composables/services.
- [ ] T004 [SERENA] Inspect existing filter/search/date/sort/reset behavior on User Home saved resumes table.
- [ ] T005 [SERENA] Inspect existing row click + row hover/highlight behavior on User Home saved resumes table.
- [ ] T006 [SERENA] Inspect existing profile shell/tabs/sidebar/unsaved changes pattern: `ProfilePage.vue`, `ProfileShell.vue`, `ProfileSidebar.vue`, `ProfileMobileTabs.vue`, `UnsavedChangesDialog.vue`.
- [ ] T007 [SERENA] Inspect backend auth/session/admin role model: `AuthInterceptor`, `UserSession`, `User`, `RoleDao`, `UserDao`, related tests.
- [ ] T008 [SERENA] Inspect existing resume listing/delete/export/public URL DTO behavior from feat/009 to avoid breaking modal contracts.
- [ ] T009 [SERENA] Inspect DAO models/tables used for users, contact details, additional info, resumes, work experience, education, projects, courses.
- [ ] T010 [PG-MCP] Verify lookup codes in database: roles `USER/ADMIN`, statuses `ACTIVE/BLOCKED`, permissions `ALLOWED/FORBIDDEN` if database is available.
- [ ] T011 [PG-MCP] Verify whether repeatable profile tables already have `is_deleted`/`deleted_at`; do not propose migrations unless evidence proves necessary.
- [ ] T012 [SERENA] Inspect existing backend tests style for controller/service/DAO layers.
- [ ] T013 [SERENA] Inspect frontend Vitest test style and coverage setup.
- [ ] T014 [NO-PDF-AI] Confirm no changes are needed in PDF/AI/generation internals.
- [ ] T015 [MEMORY] If baseline reveals reusable lessons from previous feat/009 mistakes, propose memory update to user and wait for approval before writing.

### Checkpoint

- [ ] T016 [STOP] [EVIDENCE] Report baseline findings:
  - current admin route/page status;
  - current User Home table patterns to reuse;
  - current Profile/UnsavedChanges patterns to reuse;
  - backend admin authorization gap, if present;
  - exact lookup codes found;
  - exact soft-delete fields found;
  - whether any migration appears necessary;
  - tests/coverage setup;
  - confirmation that PDF/AI internals are untouched.

After report, propose whether to call `@git_commit_pr_assistant` only if any baseline-only documentation changes were made. Do not commit without user permission.

---

## Phase 1 — Backend Admin Foundation and Authorization

**Goal**: Create safe backend foundation for `/api/admin/**` without exposing sensitive data.

**Before starting**:
- [ ] Recommend switching to Java/backend agent.
- [ ] [CTX7] Refresh docs for Spring MVC controllers, session handling, JUnit 5, Mockito, validation, and JDBC basics.
- [ ] [SERENA] Inspect existing controller/service/DAO patterns before creating new classes.
- [ ] [TDD] Write tests first.
- [ ] [KISS] Keep admin authorization simple and explicit.

### Tasks

- [ ] T017 [TDD] [SEC] Add backend tests proving `/api/admin/**` rejects unauthenticated users and non-admin authenticated users.
- [ ] T018 [TDD] [SEC] Add tests proving admin users can access admin endpoints.
- [ ] T019 [SERENA] Decide simplest authorization location based on existing code: controller helper, service helper, or interceptor. Do not overengineer.
- [ ] T020 [SEC] Implement backend ADMIN authorization for every `/api/admin/**` endpoint.
- [ ] T021 [SEC] Ensure frontend route guard is not treated as backend security.
- [ ] T022 [KISS] Create `AdminController`, `AdminService`, and `AdminDao` only if they fit current code style; otherwise use the closest existing pattern, but keep admin code clearly separated.
- [ ] T023 [TDD] Add tests for safe generic error responses for unauthorized admin calls.
- [ ] T024 [SEC] Ensure admin DTOs never include `password_hash`, API keys, encrypted API keys, local paths, or internal exception details.

### Checkpoint

- [ ] T025 [STOP] [EVIDENCE] Report:
  - files created/modified;
  - exact admin authorization mechanism;
  - test names and command output;
  - example rejected non-admin API response;
  - example accepted admin API response;
  - confirmation that no sensitive fields are exposed.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 2 — Backend Dashboard and Admin Resumes Read API

**Goal**: Implement read-only dashboard stats and all-resumes listing for Admin Home.

**Before starting**:
- [ ] Recommend Java/backend agent.
- [ ] [CTX7] Refresh docs for JDBC pagination, safe query building, JUnit/Mockito.
- [ ] [SERENA] Inspect existing `PagedResponse`, `HomeSavedResumeDto`, resume mappers, and User Home DAO queries.
- [ ] [PG-MCP] Verify schema and sample rows if needed.
- [ ] [TDD] Write tests first.
- [ ] [KISS] Reuse existing mapping concepts; do not add generic query framework.

### Tasks

- [ ] T026 [TDD] Add tests for `GET /api/admin/dashboard` returning real total users and total resumes, plus WIP token stats as `0`.
- [ ] T027 Implement `AdminDashboardDto`.
- [ ] T028 Implement dashboard DAO/service methods:
  - total non-deleted users;
  - total non-deleted saved resumes;
  - tokens sent/generated hardcoded `0` with WIP flags.
- [ ] T029 [TDD] Add tests for `GET /api/admin/resumes` default pagination excluding soft-deleted resumes.
- [ ] T030 [TDD] Add tests for admin resumes search across resume title, vacancy title, company name, owner username, owner email, owner full name.
- [ ] T031 [TDD] Add tests for language, adaptation level, created date range filters.
- [ ] T032 [TDD] Add tests for inclusive date range semantics: `createdFrom >= start of day`, `createdTo < next day start`.
- [ ] T033 [TDD] Add tests for invalid date range handling.
- [ ] T034 [TDD] [SEC] Add tests proving sort uses whitelist and rejects/normalizes unsupported sort fields.
- [ ] T035 Implement `AdminSavedResumeDto` with owner fields and canonical safe resume action URLs.
- [ ] T036 [SEC] Ensure `AdminSavedResumeDto` does not expose raw `pdf_file_path`, `html_file_path`, storage directories, or internal local paths.
- [ ] T037 Implement admin resumes DAO query with pagination, search, filters, date range, and whitelist sorting.
- [ ] T038 [KISS] Avoid N+1 queries. Use joins/aggregates where simple and readable.
- [ ] T039 [TDD] Add/adjust controller tests for `/api/admin/dashboard` and `/api/admin/resumes`.

### Checkpoint

- [ ] T040 [STOP] [EVIDENCE] Report:
  - dashboard API sample;
  - admin resumes API sample;
  - proof token stats are intentionally WIP hardcoded `0`;
  - proof deleted resumes excluded;
  - proof owner fields exist;
  - proof no raw paths/sensitive fields;
  - test command output.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 3 — Backend Admin Resume Delete

**Goal**: Allow admin to soft-delete any resume safely.

**Before starting**:
- [ ] Recommend Java/backend agent.
- [ ] [CTX7] Refresh docs for Spring MVC DELETE endpoints, JDBC transactions, JUnit/Mockito.
- [ ] [SERENA] Inspect existing user-owned resume delete behavior from feat/009 and reuse safe logic where appropriate.
- [ ] [PG-MCP] Verify saved resume soft-delete fields if needed.
- [ ] [TDD] Write tests first.
- [ ] [KISS] Do not create a second incompatible delete behavior.

### Tasks

- [ ] T041 [TDD] Add tests for `DELETE /api/admin/resumes/{resumeId}` success.
- [ ] T042 [TDD] Add tests that admin resume delete sets both `is_deleted = true` and `deleted_at`.
- [ ] T043 [TDD] Add tests that deleted resume disappears from admin resumes list and selected user resumes list.
- [ ] T044 [TDD] [SEC] Add tests for non-existent/already-deleted resume returning generic safe error/response.
- [ ] T045 [TDD] [SEC] Add tests that regular users cannot call admin resume delete.
- [ ] T046 Implement admin resume delete in service/DAO, reusing existing soft-delete semantics.
- [ ] T047 [SEC] Ensure delete does not leak ownership/path/internal details in responses.
- [ ] T048 [NO-PDF-AI] Confirm no changes to public route/PDF generation are made unless existing delete contract requires none.

### Checkpoint

- [ ] T049 [STOP] [EVIDENCE] Report:
  - delete endpoint behavior;
  - soft-delete SQL behavior;
  - tests showing deleted resume disappears;
  - generic error examples;
  - confirmation PDF/AI/public route internals untouched.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 4 — Backend Admin Users List API

**Goal**: Implement `/api/admin/users` with search, filters, date range, pagination, and sorting.

**Before starting**:
- [ ] Recommend Java/backend agent.
- [ ] [CTX7] Refresh docs for JDBC pagination, validation, JUnit/Mockito.
- [ ] [SERENA] Inspect existing user/profile DAOs and `PagedResponse`.
- [ ] [PG-MCP] Verify users/contact/role/status/permission schema if needed.
- [ ] [TDD] Write tests first.
- [ ] [KISS] Use straightforward SQL with safe parameters and whitelist sorting.

### Tasks

- [ ] T050 [TDD] Add tests for default `/api/admin/users` listing excluding soft-deleted users.
- [ ] T051 [TDD] Add tests for shared text search across username, email, full name.
- [ ] T052 [TDD] Add tests for role filter: `ALL / USER / ADMIN`.
- [ ] T053 [TDD] Add tests for status filter: `ALL / ACTIVE / BLOCKED`.
- [ ] T054 [TDD] Add tests for generation permission filter: `ALL / ALLOWED / FORBIDDEN`.
- [ ] T055 [TDD] Add tests for rights filter: `ALL / PRIVILEGED / NON_PRIVILEGED` mapping to `is_privileged`.
- [ ] T056 [TDD] Add tests for account creation date range with inclusive semantics.
- [ ] T057 [TDD] Add tests for invalid date range.
- [ ] T058 [TDD] [SEC] Add tests for sort whitelist and unsupported sort fields.
- [ ] T059 Implement `AdminUserListItemDto`.
- [ ] T060 Implement DAO/service/controller for `/api/admin/users`.
- [ ] T061 [SEC] Ensure no deleted users, password hashes, or sensitive fields are returned.
- [ ] T062 [KISS] Use lookup codes, not hardcoded lookup IDs.
- [ ] T063 [KISS] Keep resumes count aggregate simple and avoid N+1.

### Checkpoint

- [ ] T064 [STOP] [EVIDENCE] Report:
  - users API sample;
  - filters tested;
  - date range behavior;
  - sort whitelist proof;
  - no action/open-details column implication is frontend-only but API supports row data;
  - test command output.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 5 — Backend Admin User Details Read API

**Goal**: Implement `/api/admin/users/{userId}` read model for Contacts, Account, Additional.

**Before starting**:
- [ ] Recommend Java/backend agent.
- [ ] [CTX7] Refresh docs for Spring MVC path variables, JSON DTOs, JUnit/Mockito.
- [ ] [SERENA] Inspect profile DAOs and field names.
- [ ] [PG-MCP] Verify contact/additional/language data structure if needed.
- [ ] [TDD] Write tests first.
- [ ] [KISS] Read-only details must be simple DTO projection, not full profile editing subsystem.

### Tasks

- [ ] T065 [TDD] Add tests for `GET /api/admin/users/{userId}` returning Contacts, Account, Additional sections.
- [ ] T066 [TDD] Add tests for missing optional fields returning null/empty safely, not crashing.
- [ ] T067 [TDD] Add tests for soft-deleted/non-existent user returning safe not-found/unavailable response.
- [ ] T068 [TDD] Add tests for `isCurrentAdmin` true/false.
- [ ] T069 Implement DTOs:
  - `AdminUserDetailsDto`;
  - `AdminUserContactsDto`;
  - `AdminUserAccountDto`;
  - `AdminUserAdditionalDto`.
- [ ] T070 Implement DAO/service projection for Contacts fields:
  - full name;
  - professional title;
  - email;
  - phone;
  - location;
  - LinkedIn;
  - portfolio/personal website;
  - Telegram;
  - WhatsApp.
- [ ] T071 Implement DAO/service projection for Account fields:
  - username;
  - role;
  - status;
  - generation permission;
  - isPrivileged;
  - createdAt;
  - isCurrentAdmin.
- [ ] T072 Implement DAO/service projection for Additional fields:
  - username;
  - professional information;
  - languages;
  - professional aspirations;
  - achievements;
  - additional context for AI;
  - date of birth;
  - citizenship.
- [ ] T073 [SEC] Ensure no sensitive fields are returned.

### Checkpoint

- [ ] T074 [STOP] [EVIDENCE] Report:
  - user details API sample;
  - own-admin sample showing `isCurrentAdmin`;
  - missing fields handling;
  - not-found/deleted behavior;
  - test output.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 6 — Backend Access Update and User Soft-Delete

**Goal**: Implement safe account access updates and user soft-delete with self-protection.

**Before starting**:
- [ ] Recommend Java/backend agent.
- [ ] [CTX7] Refresh docs for PATCH/DELETE endpoints, validation, JDBC transactions, JUnit/Mockito.
- [ ] [SERENA] Inspect existing transaction patterns such as finalize/delete if available.
- [ ] [PG-MCP] Verify which related tables support soft-delete.
- [ ] [TDD] Write tests first.
- [ ] [KISS] Implement a simple transaction. Do not build audit framework.

### Access Update Tasks

- [ ] T075 [TDD] Add tests for successful `PATCH /api/admin/users/{userId}/access` on another user.
- [ ] T076 [TDD] Add tests for invalid role/status/permission code rejection.
- [ ] T077 [TDD] [SEC] Add tests rejecting self-demotion from `ADMIN` to `USER`.
- [ ] T078 [TDD] [SEC] Add tests rejecting self-block from `ACTIVE` to `BLOCKED`.
- [ ] T079 [TDD] Add tests allowing own generation permission change.
- [ ] T080 [TDD] Add tests allowing own `isPrivileged` change.
- [ ] T081 Implement `AdminAccessUpdateRequestDto`.
- [ ] T082 Implement access update service/DAO using lookup codes, not IDs.
- [ ] T083 [SEC] Ensure non-admin cannot update access.
- [ ] T084 [KISS] Use clear validation errors internally, generic localized frontend message externally.

### User Soft-Delete Tasks

- [ ] T085 [TDD] Add tests for successful `DELETE /api/admin/users/{userId}` on another user.
- [ ] T086 [TDD] Add tests rejecting self-delete.
- [ ] T087 [TDD] Add tests that user delete sets `users.is_deleted = true` and `users.deleted_at`.
- [ ] T088 [TDD] Add tests that user status becomes `BLOCKED` after delete.
- [ ] T089 [TDD] Add tests that user's saved resumes are soft-deleted.
- [ ] T090 [TDD] Add tests for repeatable profile record soft-delete only where schema supports it.
- [ ] T091 [TDD] Add tests that contact/additional profile records are not migrated/forced into new soft-delete behavior.
- [ ] T092 [TDD] [SEC] Add tests for non-existent/already-deleted user returning safe generic response.
- [ ] T093 Implement user soft-delete transaction:
  - mark user deleted;
  - set deleted_at;
  - set status to BLOCKED;
  - soft-delete saved resumes;
  - soft-delete repeatable profile records with existing soft-delete fields.
- [ ] T094 [SEC] Do not hard-delete any rows.
- [ ] T095 [SEC] Do not create audit log.
- [ ] T096 [SEC] Do not add migrations unless user explicitly approved earlier.

### Checkpoint

- [ ] T097 [STOP] [EVIDENCE] Report:
  - access update tests;
  - self-protection tests;
  - user delete transaction behavior;
  - exact related tables soft-deleted;
  - exact related tables intentionally not soft-deleted because schema lacks fields;
  - API samples;
  - test command output.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 7 — Frontend Routing, Types, Services, and i18n Foundation

**Goal**: Prepare frontend foundations without building full pages yet.

**Before starting**:
- [ ] Recommend Vue/frontend agent.
- [ ] [CTX7] Refresh docs for Vue Router, Vue 3 Composition API, TypeScript, vue-i18n, PrimeVue Toast/Tooltip.
- [ ] [SERENA] Inspect existing router, auth guard, service/httpClient patterns, and types.
- [ ] [TDD] Add tests where current frontend test setup supports it.
- [ ] [KISS] Keep services thin; do not add global state library.

### Tasks

- [ ] T098 [TDD] Add/update router tests if project has router tests; otherwise document why not and cover through component/e2e tests later.
- [ ] T099 Add routes:
  - `/admin` existing Admin Home;
  - `/admin/users`;
  - `/admin/users/:userId`;
  - `/admin/ai-models` WIP.
- [ ] T100 Ensure routes require admin via existing frontend route guard.
- [ ] T101 Create frontend admin types for dashboard, saved resume, user list item, user details, access update request.
- [ ] T102 Create `adminService.ts` methods:
  - getDashboard;
  - listAdminResumes;
  - deleteAdminResume;
  - listAdminUsers;
  - getAdminUserDetails;
  - updateAdminUserAccess;
  - deleteAdminUser;
  - listAdminUserResumes.
- [ ] T103 Add i18n keys in `en.json` and `ru.json` for all new admin pages, filters, labels, statuses, permissions, rights, WIP badges, toasts, confirmations, tooltips, empty states.
- [ ] T104 [SEC] Ensure no frontend type expects sensitive fields.
- [ ] T105 [KISS] Keep composables page-specific unless reuse is clearly simple.

### Checkpoint

- [ ] T106 [STOP] [EVIDENCE] Report:
  - routes added;
  - service methods;
  - i18n keys added;
  - no raw i18n keys visible in simple render test if available;
  - frontend test command output if tests were added.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 8 — Frontend Admin Home Dashboard and Resumes Table

**Goal**: Build `/app/admin` with dashboard cards, WIP badges, quick actions, and all-resumes table.

**Before starting**:
- [ ] Recommend Vue/frontend agent.
- [ ] [CTX7] Refresh docs for Vue 3, PrimeVue DataTable, DatePicker, Dropdown/Select, Tooltip, Toast, Dialog.
- [ ] [SERENA] Inspect existing `UserHomePage`, `SummaryCards`, `SavedResumesTable`, `ResumeDetailsDialog` before editing.
- [ ] [TDD] Write Vitest tests first for components/composables where practical.
- [ ] [KISS] Reuse patterns; do not create a new design system.

### Tasks

- [ ] T107 [TDD] Add tests for Admin Home dashboard rendering real stats and WIP badges.
- [ ] T108 [TDD] Add tests for quick actions:
  - Users navigates to `/admin/users`;
  - Resumes scrolls/anchors to Admin Resumes section;
  - AI Models navigates to `/admin/ai-models`.
- [ ] T109 Build/update `AdminHomePage.vue` dashboard area.
- [ ] T110 Build `AdminStatsCards.vue` if it keeps page simpler.
- [ ] T111 Build `AdminQuickActions.vue` if it keeps page simpler.
- [ ] T112 [TDD] Add tests for Admin Resumes table filters/search/date range/pagination reset.
- [ ] T113 [TDD] Add tests blocking `createdTo` earlier than `createdFrom`.
- [ ] T114 [TDD] Add tests for reset filters behavior.
- [ ] T115 [TDD] Add tests for row click opening modal and hover/clickable classes.
- [ ] T116 Build `AdminResumesTable.vue` using User Home table style and behavior.
- [ ] T117 Wire Admin Resumes table into `/app/admin` directly.
- [ ] T118 Ensure Admin Resumes table does not live on separate `/app/admin/resumes` page.
- [ ] T119 Wire row click to existing/resuable `ResumeDetailsDialog`.
- [ ] T120 Add admin delete resume flow in modal context:
  - confirmation;
  - loading/disabled button;
  - success reload;
  - failure keeps modal open.
- [ ] T121 Ensure filters/sort/pagination state is local to Admin Home table.
- [ ] T122 [SEC] Ensure modal does not render raw paths or sensitive fields.

### Checkpoint

- [ ] T123 [STOP] [EVIDENCE] Report:
  - screenshot of `/app/admin`;
  - dashboard/WIP badge proof;
  - all-resumes table proof;
  - row click modal proof;
  - delete resume proof;
  - test output;
  - confirmation no `/app/admin/resumes` full page was created.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 9 — Frontend Admin Users Table

**Goal**: Build `/app/admin/users` with consistent table UX.

**Before starting**:
- [ ] Recommend Vue/frontend agent.
- [ ] [CTX7] Refresh docs for PrimeVue DataTable, DatePicker, Select/Dropdown, Tooltip, pagination, Vue Router navigation.
- [ ] [SERENA] Inspect `SavedResumesTable.vue` for sort icons/tooltips/filters/reset style.
- [ ] [TDD] Write tests first.
- [ ] [KISS] Do not add action column. Use row click.

### Tasks

- [ ] T124 [TDD] Add tests for Admin Users page rendering table columns.
- [ ] T125 [TDD] Add tests for one shared search field over username/email/fullName behavior at service query level.
- [ ] T126 [TDD] Add tests for account creation date filter label and date range behavior.
- [ ] T127 [TDD] Add tests preventing `createdTo` earlier than `createdFrom`.
- [ ] T128 [TDD] Add tests for role/status/permission/rights filters.
- [ ] T129 [TDD] Add tests for page reset on filter/search changes.
- [ ] T130 [TDD] Add tests for reset filters button.
- [ ] T131 [TDD] Add tests for sort interaction and whitelist query values sent to service.
- [ ] T132 [TDD] Add tests for row click navigation to `/admin/users/:userId`.
- [ ] T133 Build `AdminUsersPage.vue`.
- [ ] T134 Build `AdminUsersTable.vue` if it keeps page simpler.
- [ ] T135 Implement columns exactly:
  - full name;
  - username;
  - email;
  - role;
  - status;
  - generation permission;
  - rights;
  - resumes count;
  - created date.
- [ ] T136 Ensure no action/open-details column exists.
- [ ] T137 Match User Home row hover/highlight and pointer behavior.
- [ ] T138 Ensure controls/pagination clicks do not trigger row navigation.
- [ ] T139 Add localized empty state.

### Checkpoint

- [ ] T140 [STOP] [EVIDENCE] Report:
  - screenshot of `/app/admin/users`;
  - search/filter/date/sort/reset behavior;
  - no action column;
  - row click navigation;
  - test output.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 10 — Frontend Admin User Details Tabs

**Goal**: Build `/app/admin/users/:userId` with Contacts, Account, Additional, Resumes tabs.

**Before starting**:
- [ ] Recommend Vue/frontend agent.
- [ ] [CTX7] Refresh docs for Vue Router path params, PrimeVue tabs/menu/confirm dialog/toast/tooltip, Vue Test Utils.
- [ ] [SERENA] Inspect Profile shell and UnsavedChangesDialog patterns.
- [ ] [TDD] Write tests first.
- [ ] [KISS] Make an admin details shell similar to Profile without over-generalizing.

### Shell and Read-Only Tabs

- [ ] T141 [TDD] Add tests for Admin User Details loading and tab order: Contacts, Account, Additional, Resumes.
- [ ] T142 [TDD] Add tests for not-found/deleted user state.
- [ ] T143 Build `AdminUserDetailsPage.vue`.
- [ ] T144 Build `AdminUserDetailsShell.vue` or reuse existing profile shell style if simpler.
- [ ] T145 [TDD] Add tests for Contacts tab fields and read-only behavior.
- [ ] T146 Build `AdminUserContactsTab.vue`.
- [ ] T147 [TDD] Add tests for Additional tab fields and read-only behavior.
- [ ] T148 Build `AdminUserAdditionalTab.vue`.
- [ ] T149 Ensure missing fields show localized empty state, not null/undefined.
- [ ] T150 Ensure long text wraps safely.

### Account Tab

- [ ] T151 [TDD] Add tests for Account tab controls:
  - role;
  - status;
  - generation permission;
  - rights/isPrivileged.
- [ ] T152 [TDD] Add tests Save disabled until changes exist.
- [ ] T153 [TDD] Add tests Save confirmation/cancel/success/failure flows.
- [ ] T154 [TDD] Add tests unsaved changes warning on tab switch/navigation/close/back.
- [ ] T155 [TDD] Add tests self-delete disabled tooltip.
- [ ] T156 [TDD] Add tests frontend prevents/warns on self-demotion and self-block where practical.
- [ ] T157 [TDD] Add tests own generation permission and own privileged flag remain editable.
- [ ] T158 Build `AdminUserAccountTab.vue`.
- [ ] T159 Wire update access service call with loading state and toasts.
- [ ] T160 Wire delete user confirmation/loading/toast/redirect.
- [ ] T161 Ensure delete account button is danger secondary.
- [ ] T162 Ensure own delete button is disabled with tooltip.
- [ ] T163 Ensure backend remains source of truth for self-protection.

### Resumes Tab

- [ ] T164 [TDD] Add tests for selected user resumes table showing only selected user's resumes.
- [ ] T165 [TDD] Add tests no Create Resume button exists.
- [ ] T166 [TDD] Add tests filters/sort/pagination state is independent from Admin Home table.
- [ ] T167 [TDD] Add tests row click opens resume modal.
- [ ] T168 [TDD] Add tests admin delete resume reloads only selected user's resumes table unless broader reload is active.
- [ ] T169 Build `AdminUserResumesTab.vue` using same table behavior as admin/home resumes table where simple.
- [ ] T170 Ensure Resumes tab has search/filter/sort/pagination and date range validation.
- [ ] T171 Ensure no Create Resume button.
- [ ] T172 Wire modal and admin resume delete flow.

### Checkpoint

- [ ] T173 [STOP] [EVIDENCE] Report:
  - screenshots of all four tabs;
  - account save confirmation;
  - unsaved changes warning;
  - self-delete disabled tooltip;
  - selected user resumes table;
  - delete user flow evidence;
  - delete resume from tab evidence;
  - test output.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 11 — Frontend AI Models WIP Page

**Goal**: Add clear WIP page and avoid AI model CRUD scope creep.

**Before starting**:
- [ ] Recommend Vue/frontend agent.
- [ ] [CTX7] Refresh docs for Vue Router and simple component testing.
- [ ] [SERENA] Inspect existing placeholder page components.
- [ ] [TDD] Write tests first if simple.
- [ ] [KISS] Use existing placeholder style.

### Tasks

- [ ] T174 [TDD] Add test for `/admin/ai-models` rendering localized WIP page.
- [ ] T175 Build `AdminAiModelsWipPage.vue` or reuse existing placeholder component.
- [ ] T176 Ensure no AI model CRUD controls exist.
- [ ] T177 Ensure no API key/provider configuration details are displayed.
- [ ] T178 Ensure route requires admin.

### Checkpoint

- [ ] T179 [STOP] [EVIDENCE] Report:
  - screenshot of WIP page;
  - test output;
  - confirmation no AI Models CRUD was implemented.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 12 — Integration Testing, Playwright MCP Evidence, and Database Verification

**Goal**: Verify end-to-end flows using available MCPs without installing extra tools.

**Before starting**:
- [ ] Recommend Playwright/testing-focused agent.
- [ ] [CTX7] Refresh docs for Playwright MCP patterns, Vitest coverage, JUnit/Mockito if needed.
- [ ] [PW-MCP] Use Playwright MCP only. Do not install Playwright.
- [ ] [PG-MCP] Use Postgres MCP for data verification if needed.
- [ ] [SERENA] Use Serena to inspect failures and changed code.
- [ ] [KISS] Evidence should be clear and minimal.

### Backend Verification

- [ ] T180 Run backend targeted tests for admin controller/service/DAO.
- [ ] T181 Run broader backend test suite if time permits.
- [ ] T182 Confirm new/changed backend code coverage target 80%+ or report measured scope coverage and legacy baseline.

### Frontend Verification

- [ ] T183 Run frontend targeted Vitest tests.
- [ ] T184 Run frontend coverage if available.
- [ ] T185 Confirm new/changed frontend code coverage target 80%+ or report measured scope coverage and legacy baseline.

### Playwright MCP Flows

- [ ] T186 [PW-MCP] Login as admin.
- [ ] T187 [PW-MCP] Open `/app/admin` and capture screenshot.
- [ ] T188 [PW-MCP] Verify dashboard stats and WIP token badges.
- [ ] T189 [PW-MCP] Verify Admin Resumes table search/filter/date/sort/reset/pagination.
- [ ] T190 [PW-MCP] Verify Admin Resumes row click opens modal.
- [ ] T191 [PW-MCP] Verify admin resume delete confirmation/cancel path.
- [ ] T192 [PW-MCP] Verify admin resume delete success path on disposable test data if available.
- [ ] T193 [PW-MCP] Open `/app/admin/users` and capture screenshot.
- [ ] T194 [PW-MCP] Verify users search/filter/date/sort/reset/pagination.
- [ ] T195 [PW-MCP] Verify user row click opens details page.
- [ ] T196 [PW-MCP] Capture Contacts tab screenshot.
- [ ] T197 [PW-MCP] Capture Account tab screenshot.
- [ ] T198 [PW-MCP] Verify unsaved changes warning on Account tab.
- [ ] T199 [PW-MCP] Verify self-delete disabled tooltip for current admin account.
- [ ] T200 [PW-MCP] Verify access update confirmation/cancel path.
- [ ] T201 [PW-MCP] Verify access update success path on disposable non-admin user if available.
- [ ] T202 [PW-MCP] Verify delete user confirmation/cancel path.
- [ ] T203 [PW-MCP] Verify delete user success path on disposable test user if available.
- [ ] T204 [PW-MCP] Capture Additional tab screenshot.
- [ ] T205 [PW-MCP] Capture Resumes tab screenshot.
- [ ] T206 [PW-MCP] Verify Resumes tab row click modal and delete resume path on disposable data if available.
- [ ] T207 [PW-MCP] Open `/app/admin/ai-models` and capture WIP screenshot.

### Postgres MCP Verification

- [ ] T208 [PG-MCP] If delete tests used real/disposable data, verify soft-delete flags in DB.
- [ ] T209 [PG-MCP] Verify self-delete/self-demotion/self-block are not persisted after rejected attempts if applicable.
- [ ] T210 [PG-MCP] Verify no hard deletes occurred in tested flows.

### Checkpoint

- [ ] T211 [STOP] [EVIDENCE] Report:
  - backend test output;
  - frontend test output;
  - coverage output or scoped coverage explanation;
  - Playwright MCP screenshots/traces list;
  - Postgres MCP verification summary;
  - unresolved issues.

After checkpoint, propose calling `@git_commit_pr_assistant` with mandatory Serena MCP usage. Do not commit without user permission.

---

## Phase 13 — Final Hardening and Scope Audit

**Goal**: Prevent accidental scope creep, security leaks, and maintainability regressions.

**Before starting**:
- [ ] Recommend senior-review/code-quality agent.
- [ ] [CTX7] Refresh docs only for any APIs touched during final fixes.
- [ ] [SERENA] Use Serena MCP to inspect changed symbols and references.
- [ ] [MEMORY] Consider lessons learned; ask before writing memory.
- [ ] [KISS] Remove complexity, not add it.

### Tasks

- [ ] T212 [SERENA] Inspect all changed files and references for unused code, dead branches, accidental duplication, and stale imports.
- [ ] T213 [SEC] Audit admin DTOs for sensitive fields.
- [ ] T214 [SEC] Audit SQL sorting for whitelist only.
- [ ] T215 [SEC] Audit self-protection rules in backend tests and implementation.
- [ ] T216 [SEC] Audit delete flows: soft-delete only, no hard delete.
- [ ] T217 [NO-PDF-AI] Confirm no PDF/AI/generation pipeline files were modified.
- [ ] T218 Confirm no new dependencies were added unless previously approved.
- [ ] T219 Confirm no migrations were added unless previously approved.
- [ ] T220 Confirm no `/app/admin/resumes` full page exists.
- [ ] T221 Confirm no AI Models CRUD exists.
- [ ] T222 Confirm all new visible strings are in EN/RU i18n files.
- [ ] T223 Confirm no raw i18n keys appear in browser screenshots.
- [ ] T224 Confirm code follows KISS and removes overengineered abstractions introduced during implementation.
- [ ] T225 [MEMORY] If valuable lessons emerged, propose memory update text and wait for user confirmation.

### Final Checkpoint

- [ ] T226 [STOP] [EVIDENCE] Final implementation report:
  - completed scope;
  - changed files grouped by backend/frontend/tests/docs;
  - test commands and outputs;
  - coverage evidence;
  - screenshots/evidence locations;
  - security guardrails verified;
  - explicit list of untouched forbidden areas;
  - known limitations/WIP items.

After final checkpoint, ask the user whether to call `@git_commit_pr_assistant`. If approved, instruct the subagent that Serena MCP usage is mandatory and that it must prepare a clean commit/PR summary without making a commit unless the user explicitly confirms.

---

## Suggested Implementation Order Summary

1. Phase 0 — Baseline inspection.
2. Phase 1 — Backend admin authorization foundation.
3. Phase 2 — Backend dashboard + admin resumes read API.
4. Phase 3 — Backend admin resume delete.
5. Phase 4 — Backend admin users list API.
6. Phase 5 — Backend user details read API.
7. Phase 6 — Backend access update + user soft-delete.
8. Phase 7 — Frontend routes/services/i18n.
9. Phase 8 — Frontend Admin Home + all-resumes table.
10. Phase 9 — Frontend Admin Users table.
11. Phase 10 — Frontend User Details tabs.
12. Phase 11 — AI Models WIP page.
13. Phase 12 — Tests + Playwright MCP + Postgres MCP evidence.
14. Phase 13 — Final hardening and scope audit.

---

## Minimum Definition of Done

- `/app/admin` works for admin and shows dashboard + all-resumes table.
- `/app/admin/users` works for admin and supports required filters/search/sort/pagination.
- `/app/admin/users/:userId` works with Contacts, Account, Additional, Resumes tabs.
- Admin can soft-delete resumes.
- Admin can soft-delete another user.
- Admin cannot self-delete, self-demote, or self-block.
- Admin may change own generation permission and own privileged flag.
- `/app/admin/ai-models` shows WIP.
- Backend admin authorization protects all `/api/admin/**` endpoints.
- New/changed feature code has 80%+ targeted coverage where measurable.
- Playwright MCP evidence exists for core flows.
- Postgres MCP verification exists for mutation flows where real/disposable data was used.
- No forbidden areas touched: PDF/AI/generation/finalization/budget/model CRUD/audit log.
- No sensitive data leaks in admin DTOs.
- No hard delete.
- No unapproved migrations or dependencies.
