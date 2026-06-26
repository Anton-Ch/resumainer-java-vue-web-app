# Feature Specification: Admin Console Users and Resumes

**Feature Branch**: `feat/010-admin-page-users`

**Created**: 2026-06-26

**Status**: Draft

**Input**: Admin console: Admin Home dashboard with all-resumes moderation table, Admin Users list, Admin User Details with tabs, account access management, admin soft-delete for resumes and users, and WIP placeholder for AI Models.

> **Important**: This spec is the source of truth for `feat/010-admin-page-users`. If any requirement conflicts with the current codebase, STOP and ask for clarification before planning or coding. Do not silently reinterpret requirements. Do not expand scope into AI model CRUD, PDF generation internals, prompt generation, finalization, or unrelated profile editing.

---

## Brainstorm Log

### Session 2026-06-26

Spec refinement via `/speckit.superpowers.brainstorm`:

1. **Q: Total users scope** → **A**: `Total users` = non-deleted only (consistent with FR-007 and Admin Users table). FR-006 updated for clarity.
2. **Q: Save action on already-deleted user** → **A**: `PATCH /api/admin/users/{userId}/access` on soft-deleted or non-existent user returns `404 Not Found`. FR-092a added.
3. **Q: `ownerEmail` source in AdminSavedResume** → **A**: Maps to `users.email` (account email), not `contact_detail.resume_email`. Entity definition annotated.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — View Admin Home Dashboard (Priority: P1)

As an admin, I want to see core system overview cards on Admin Home so that I can quickly understand the current state of users and resumes.

**Why this priority**: Admin Home is the entry point for admin users. It must not show untranslated keys or fake full dashboard behavior.

**Independent Test**: Log in as an admin, open `/app/admin`, and verify dashboard cards show correct real/WIP values and localized labels.

**Acceptance Scenarios**:

1. **Given** an authenticated admin opens `/app/admin`, **Then** the page displays the Admin Home dashboard.
2. **Given** the dashboard loads, **Then** `Total users` shows a real backend value.
3. **Given** the dashboard loads, **Then** `Total resumes` shows a real backend value.
4. **Given** the dashboard loads, **Then** `Total tokens sent` shows `0` with a visible WIP badge.
5. **Given** the dashboard loads, **Then** `Total tokens generated` shows `0` with a visible WIP badge.
6. **Given** the dashboard loads, **Then** no untranslated i18n keys such as `home.totalUsers` are visible.
7. **Given** a non-admin user attempts to access `/app/admin`, **Then** frontend route guard prevents access and backend admin APIs reject access.
8. **Given** an unauthenticated user attempts to access `/app/admin`, **Then** the user is redirected to login according to existing auth behavior.

---

### User Story 2 — Navigate Admin Quick Actions (Priority: P2)

As an admin, I want Admin Home action cards to navigate to the correct admin areas so that unfinished areas are clear and finished areas are usable.

**Why this priority**: Navigation must be predictable. Broken/no-op cards reduce confidence and slow manual testing.

**Independent Test**: Click each Admin Home quick action card and verify destination behavior.

**Acceptance Scenarios**:

1. **Given** the admin clicks the Users card, **Then** the app navigates to `/app/admin/users`.
2. **Given** the admin clicks the Resumes card, **Then** the page scrolls or anchors to the Admin Resumes section on `/app/admin`.
3. **Given** the admin clicks the AI Models card, **Then** the app navigates to `/app/admin/ai-models`.
4. **Given** `/app/admin/ai-models` opens, **Then** a localized Work in Progress placeholder is displayed.
5. **Given** `/app/admin/ai-models` opens, **Then** no AI model create/edit/delete functionality is available in this feature.

---

### User Story 3 — View and Filter All Resumes on Admin Home (Priority: P1)

As an admin, I want to view all saved resumes from all users on Admin Home so that I can inspect and moderate generated content.

**Why this priority**: Resume moderation is one of the main admin capabilities for this feature, and the user explicitly decided that the Admin Resumes full table belongs on `/app/admin`.

**Independent Test**: Open `/app/admin`, use the all-resumes table search/filter/sort/pagination, and verify rows represent resumes across users.

**Acceptance Scenarios**:

1. **Given** admin opens `/app/admin`, **Then** an Admin Resumes table is visible directly on the page.
2. **Given** the table loads, **Then** it displays saved resumes across all non-deleted users and excludes soft-deleted resumes.
3. **Given** the table loads, **Then** each row includes owner information at minimum: owner username and enough owner identity to distinguish users.
4. **Given** the admin searches, **Then** search applies to resume title, vacancy title, company name, owner username, owner email, and owner full name where available.
5. **Given** the admin filters by language, adaptation level, and created date range, **Then** the backend returns only matching resumes.
6. **Given** the admin selects `createdFrom`, **Then** the `createdTo` picker must not allow a date earlier than `createdFrom`.
7. **Given** the backend receives `createdTo` earlier than `createdFrom`, **Then** it returns a safe validation error or ignores the invalid range consistently; it must not crash.
8. **Given** `createdFrom` and `createdTo` are selected, **Then** `createdFrom` is inclusive from start of day and `createdTo` is inclusive through the selected day.
9. **Given** the admin sorts a supported column, **Then** sort applies correctly and only through backend whitelisted sort fields.
10. **Given** the admin changes search/filter values, **Then** pagination resets to the first page.
11. **Given** active filters exist, **Then** a reset filters button appears using the same UX logic as the User Home saved resumes table.
12. **Given** the admin resets filters, **Then** search, dropdowns, dates, sort state if applicable, and page state return to default behavior.
13. **Given** there are no matching resumes, **Then** a localized empty state is shown.

---

### User Story 4 — Open Resume Details from Admin Resumes Table (Priority: P1)

As an admin, I want to click a resume row and open the resume details modal so that I can review generated artifacts without navigating away.

**Why this priority**: The existing user workflow uses row click and a details modal. Admin moderation should reuse the same interaction style.

**Independent Test**: Click a row in Admin Home Admin Resumes table and verify the modal opens for the selected resume.

**Acceptance Scenarios**:

1. **Given** the Admin Resumes table has rows, **When** the admin hovers over a row, **Then** the row shows pointer cursor and hover highlight consistent with User Home saved resumes table.
2. **Given** the admin clicks a resume row, **Then** the resume details modal opens for that exact selected resume.
3. **Given** the modal opens, **Then** it displays owner-safe resume metadata and action URLs without exposing raw filesystem paths.
4. **Given** PDF is available, **When** the admin clicks Open PDF, **Then** the PDF opens in a new tab using the existing authenticated PDF endpoint.
5. **Given** PDF is available, **When** the admin clicks Download PDF, **Then** PDF download starts using the existing authenticated PDF endpoint.
6. **Given** HTML download is available, **When** the admin clicks Download HTML, **Then** HTML download starts using the existing authenticated HTML endpoint.
7. **Given** public URL is available, **When** the admin clicks Copy public link, **Then** the full public URL is copied.
8. **Given** cover letter exists, **Then** cover letter is displayed using the same preview/full-toggle behavior as the user modal.
9. **Given** the modal is closed, **Then** parent state is synchronized and the table remains usable.
10. **Given** the admin opens another row after closing or without stale state, **Then** the modal displays the newly selected resume.

---

### User Story 5 — Admin Soft-Deletes a Resume (Priority: P1)

As an admin, I want to soft-delete an inappropriate or problematic resume so that it is no longer visible or publicly accessible.

**Why this priority**: Admin must be able to moderate generated content quickly.

**Independent Test**: Open a resume from Admin Home, delete it, verify it disappears from admin/user tables and the old public link no longer serves active content.

**Acceptance Scenarios**:

1. **Given** the admin opens a resume details modal, **Then** a Delete resume action is available.
2. **Given** the admin clicks Delete resume, **Then** a confirmation dialog appears before any backend mutation.
3. **Given** the admin cancels deletion, **Then** the resume remains unchanged and modal remains usable.
4. **Given** the admin confirms deletion, **Then** the confirm button is disabled and shows loading state until the API responds.
5. **Given** deletion succeeds, **Then** the resume is soft-deleted with `is_deleted = true` and `deleted_at` set.
6. **Given** deletion succeeds, **Then** the modal closes and the relevant resume table reloads.
7. **Given** deletion succeeds from Admin Home, **Then** Admin Home resume table and dashboard `Total resumes` reload.
8. **Given** deletion succeeds from Admin User Details -> Resumes tab, **Then** that tab's resume table reloads.
9. **Given** deletion fails, **Then** the modal remains open, data remains unchanged, the button re-enables, and a generic localized error toast is shown.
10. **Given** a soft-deleted resume had a public URL, **When** the public URL is opened, **Then** existing public route deletion behavior from previous feature must remain correct.
11. **Given** deletion is requested for a non-existent or already-deleted resume, **Then** backend returns a safe generic response without leaking ownership or path details.

---

### User Story 6 — View Admin Users Table (Priority: P1)

As an admin, I want to search, filter, sort, and paginate users so that I can find accounts quickly.

**Why this priority**: User management is the core of this feature.

**Independent Test**: Open `/app/admin/users`, apply each filter/search/sort, and verify results and page behavior.

**Acceptance Scenarios**:

1. **Given** admin opens `/app/admin/users`, **Then** a users table is displayed.
2. **Given** the table loads, **Then** it excludes soft-deleted users by default.
3. **Given** the table loads, **Then** columns include full name, username, email, role, status, generation permission, rights, resumes count, and created date.
4. **Given** the table loads, **Then** there is no action/open-details column.
5. **Given** the admin types in search, **Then** backend searches across username, email, and full name.
6. **Given** the admin selects role filter, **Then** values are `ALL`, `USER`, `ADMIN`.
7. **Given** the admin selects status filter, **Then** values are `ALL`, `ACTIVE`, `BLOCKED`.
8. **Given** the admin selects generation permission filter, **Then** values are `ALL`, `ALLOWED`, `FORBIDDEN`.
9. **Given** the admin selects rights filter, **Then** values are `ALL`, `PRIVILEGED`, `NON_PRIVILEGED`.
10. **Given** the admin selects account creation date range, **Then** it filters by `users.created_at`.
11. **Given** the admin selects `createdFrom`, **Then** `createdTo` picker must block selecting a date earlier than `createdFrom`.
12. **Given** the backend receives an invalid date range, **Then** it returns a safe validation error or normalizes consistently; it must not crash.
13. **Given** any filter/search changes, **Then** page resets to first page.
14. **Given** active filters exist, **Then** reset filters button appears and works consistently with User Home table behavior.
15. **Given** the admin sorts a column, **Then** sort icons/tooltips follow the same style as User Home saved resumes table.
16. **Given** sorting is requested, **Then** backend uses only whitelisted sort fields and never directly injects query params into SQL.
17. **Given** no users match filters, **Then** a localized empty state is shown.

---

### User Story 7 — Open User Details by Row Click (Priority: P1)

As an admin, I want to open a user details page by clicking a user row so that table behavior stays consistent with the resume table.

**Why this priority**: User details is the main management screen and should be accessible without adding a separate action column.

**Independent Test**: Click a user row in `/app/admin/users` and verify navigation to `/app/admin/users/:userId`.

**Acceptance Scenarios**:

1. **Given** the users table has rows, **When** the admin hovers over a row, **Then** the row shows pointer cursor and hover highlight.
2. **Given** the admin clicks a user row, **Then** the app navigates to `/app/admin/users/:userId`.
3. **Given** the admin interacts with filters, dropdowns, date pickers, reset button, pagination, or sort controls, **Then** those interactions must not accidentally open user details.
4. **Given** the selected user does not exist or was soft-deleted, **Then** a localized not-found or unavailable state is shown.
5. **Given** a non-admin user tries to open `/app/admin/users/:userId`, **Then** frontend and backend access checks prevent access.

---

### User Story 8 — View User Details Tabs (Priority: P1)

As an admin, I want user details split into tabs/sections so that account, contact, additional, and resume data are understandable.

**Why this priority**: User details contains multiple data groups. A single long page would be harder to inspect and easier to break.

**Independent Test**: Open a user details page and switch between Contacts, Account, Additional, and Resumes.

**Acceptance Scenarios**:

1. **Given** admin opens `/app/admin/users/:userId`, **Then** the page displays a details shell with tab/sidebar navigation similar to the Profile page style.
2. **Given** details page loads, **Then** tab order is exactly: Contacts, Account, Additional, Resumes.
3. **Given** Contacts tab is opened, **Then** contact summary fields are read-only.
4. **Given** Account tab is opened, **Then** role, status, generation permission, and rights are editable controls.
5. **Given** Additional tab is opened, **Then** additional profile information is read-only.
6. **Given** Resumes tab is opened, **Then** a resume table for the selected user is displayed.
7. **Given** admin switches between read-only tabs with no unsaved account changes, **Then** no unsaved changes warning appears.
8. **Given** admin has unsaved Account tab changes and tries to switch tab, navigate away, close/back, or leave the page, **Then** a warning dialog appears using the existing profile unsaved-changes UX pattern.

---

### User Story 9 — View Contacts Tab (Priority: P2)

As an admin, I want to view a user's contact and professional identity information so that I can understand who the account belongs to.

**Why this priority**: Contacts information is useful for support and admin review but is read-only in this feature.

**Independent Test**: Open Contacts tab for a user with filled and missing contact fields.

**Acceptance Scenarios**:

1. **Given** Contacts tab loads, **Then** it displays Full name.
2. **Given** Contacts tab loads, **Then** it displays Professional title or a localized empty value if missing.
3. **Given** Contacts tab loads, **Then** it displays Email.
4. **Given** Contacts tab loads, **Then** it displays Phone or localized empty value.
5. **Given** Contacts tab loads, **Then** it displays Location or localized empty value.
6. **Given** Contacts tab loads, **Then** it displays LinkedIn URL if present.
7. **Given** Contacts tab loads, **Then** it displays Portfolio / personal website URL if present.
8. **Given** Contacts tab loads, **Then** it displays Telegram if present.
9. **Given** Contacts tab loads, **Then** it displays WhatsApp if present.
10. **Given** any contact field contains long text, **Then** it wraps or truncates safely without breaking layout.
11. **Given** Contacts tab is read-only, **Then** no save controls for contact fields are available.

---

### User Story 10 — Edit Account Access Controls (Priority: P1)

As an admin, I want to update a user's role, status, generation permission, and privileged flag so that I can manage account access.

**Why this priority**: Access control changes are the primary editable admin action.

**Independent Test**: Change access fields for another user, save with confirmation, and verify persisted values.

**Acceptance Scenarios**:

1. **Given** Account tab loads, **Then** it displays role dropdown with full localized labels for `USER` and `ADMIN`.
2. **Given** Account tab loads, **Then** it displays status dropdown with full localized labels for `ACTIVE` and `BLOCKED`.
3. **Given** Account tab loads, **Then** it displays generation permission dropdown with full localized labels for `ALLOWED` and `FORBIDDEN`.
4. **Given** Account tab loads, **Then** it displays rights/privileged control with localized labels for privileged and non-privileged state.
5. **Given** no values changed, **Then** Save button is disabled or clearly inactive.
6. **Given** any account access value changes, **Then** Save button becomes available.
7. **Given** admin clicks Save, **Then** confirmation dialog appears before backend update.
8. **Given** admin cancels confirmation, **Then** no backend update occurs and form remains in changed state.
9. **Given** admin confirms save, **Then** save button enters loading/disabled state until API responds.
10. **Given** save succeeds, **Then** success toast appears and details reload or local baseline state updates.
11. **Given** save fails, **Then** generic localized error toast appears and unsaved changes remain available for retry.
12. **Given** backend receives invalid role/status/permission code, **Then** it returns a safe validation error.
13. **Given** backend receives a request from non-admin, **Then** it rejects the request.

---

### User Story 11 — Protect Admin from Self-Demotion and Self-Block (Priority: P1)

As an admin, I want the system to prevent destructive changes to my own admin access so that I do not accidentally lock myself out.

**Why this priority**: A single admin account in a capstone/demo environment must not be easy to disable by mistake.

**Independent Test**: Open own admin account in Admin User Details and try disallowed self-changes.

**Acceptance Scenarios**:

1. **Given** admin opens own account details, **Then** the page identifies this as current admin account.
2. **Given** admin attempts to change own role from `ADMIN` to `USER`, **Then** frontend prevents or warns, and backend rejects if submitted.
3. **Given** admin attempts to change own status from `ACTIVE` to `BLOCKED`, **Then** frontend prevents or warns, and backend rejects if submitted.
4. **Given** backend rejects self-demotion or self-block, **Then** it returns a safe error response without exposing internals.
5. **Given** admin changes own generation permission, **Then** this is allowed.
6. **Given** admin changes own privileged flag, **Then** this is allowed.
7. **Given** admin changes another user's role/status/permission/rights, **Then** standard validation applies.

---

### User Story 12 — Soft-Delete User Account (Priority: P1)

As an admin, I want to soft-delete another user's account so that inappropriate or test accounts can be removed from active admin and user workflows without destructive hard deletion.

**Why this priority**: User moderation is a required admin operation and must be safe.

**Independent Test**: Delete another user account and verify user, resumes, and list visibility behavior.

**Acceptance Scenarios**:

1. **Given** admin opens Account tab for another user, **Then** Delete account button is visible as a danger secondary action.
2. **Given** admin opens own Account tab, **Then** Delete account button is disabled and tooltip explains that an admin cannot delete own account.
3. **Given** admin clicks Delete account for another user, **Then** confirmation dialog appears before any backend mutation.
4. **Given** admin cancels confirmation, **Then** no backend mutation occurs.
5. **Given** admin confirms deletion, **Then** delete button enters loading/disabled state until API responds.
6. **Given** deletion succeeds, **Then** `users.is_deleted = true` and `users.deleted_at` is set.
7. **Given** deletion succeeds, **Then** the user's status becomes `BLOCKED`.
8. **Given** deletion succeeds, **Then** the user's saved resumes are soft-deleted.
9. **Given** deletion succeeds, **Then** related repeatable profile records are soft-deleted only for tables that already support soft-delete.
10. **Given** the soft-deleted user had saved resumes with previously valid public URLs, **Then** those public URLs MUST follow existing soft-deleted public resume behavior (`410 Gone`) and MUST NOT continue serving active PDFs.
11. **Given** contact/additional profile tables do not support soft-delete, **Then** no migration is created just for this feature; those records become unreachable through the soft-deleted user.
12. **Given** deletion succeeds, **Then** admin is redirected back to `/app/admin/users` or shown a clear deleted state.
13. **Given** deletion succeeds, **Then** deleted user no longer appears in default Admin Users table.
14. **Given** deletion fails, **Then** a generic localized error toast appears and the account remains unchanged.
15. **Given** admin attempts self-delete through API, **Then** backend rejects the request even if frontend disabled button was bypassed.
16. **Given** admin attempts to delete an already-deleted or non-existent user, **Then** backend returns a safe generic response.

---

### User Story 13 — View Additional Tab (Priority: P2)

As an admin, I want to review a user's additional profile information so that I can understand AI context and professional profile completeness.

**Why this priority**: Additional context can explain generated outputs but should not be admin-editable in this feature.

**Independent Test**: Open Additional tab and verify all required read-only fields render safely.

**Acceptance Scenarios**:

1. **Given** Additional tab opens, **Then** username is displayed.
2. **Given** Additional tab opens, **Then** professional information is displayed or localized empty value is shown.
3. **Given** Additional tab opens, **Then** languages are displayed or localized empty value is shown.
4. **Given** Additional tab opens, **Then** professional aspirations are displayed or localized empty value is shown.
5. **Given** Additional tab opens, **Then** achievements are displayed or localized empty value is shown.
6. **Given** Additional tab opens, **Then** additional context for AI is displayed or localized empty value is shown.
7. **Given** Additional tab opens, **Then** date of birth is displayed or localized empty value is shown.
8. **Given** Additional tab opens, **Then** citizenship is displayed or localized empty value is shown.
9. **Given** any long text appears, **Then** it wraps safely and does not break the layout.
10. **Given** Additional tab is read-only, **Then** no save controls for additional fields are available.

---

### User Story 14 — View Selected User Resumes Tab (Priority: P1)

As an admin, I want to see and manage resumes for the selected user from User Details so that I can moderate a specific account in context.

**Why this priority**: This tab connects user management with resume moderation.

**Independent Test**: Open User Details -> Resumes tab, filter/sort/paginate the user's resumes, open modal, and delete a resume.

**Acceptance Scenarios**:

1. **Given** admin opens Resumes tab, **Then** a table displays only resumes belonging to the selected user.
2. **Given** Resumes tab loads, **Then** there is no Create resume button.
3. **Given** admin searches/filters/sorts/paginates in Resumes tab, **Then** table behavior remains independent from Admin Home all-resumes table state.
4. **Given** admin changes filters in Resumes tab, **Then** page resets to first page.
5. **Given** created date range is used, **Then** `createdTo` cannot be earlier than `createdFrom`.
6. **Given** admin clicks a resume row, **Then** the resume details modal opens.
7. **Given** admin deletes a resume from the modal, **Then** only the selected user's Resumes tab table reloads unless broader dashboard reload is already active.
8. **Given** there are no resumes for the user, **Then** a localized empty state is shown.

---

### User Story 15 — AI Models WIP Page (Priority: P3)

As an admin, I want unfinished AI Models navigation to show a clear WIP page so that I know the area is intentionally not implemented yet.

**Why this priority**: Avoids broken navigation and prevents scope creep into model management.

**Independent Test**: Open `/app/admin/ai-models`.

**Acceptance Scenarios**:

1. **Given** admin opens `/app/admin/ai-models`, **Then** a localized Work in Progress page is shown.
2. **Given** the page renders, **Then** it uses the application layout and header.
3. **Given** the page renders, **Then** no AI provider, model, API key, create, edit, delete, or token usage logic is implemented.
4. **Given** a non-admin user attempts to open `/app/admin/ai-models`, **Then** access is prevented.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Admin Authorization

- **FR-001**: The system MUST protect every `/api/admin/**` endpoint with backend ADMIN authorization.
- **FR-002**: Frontend route guards MUST NOT be treated as sufficient security.
- **FR-003**: Non-admin authenticated users MUST be rejected from admin APIs.
- **FR-004**: Unauthenticated users MUST be rejected according to existing API authentication behavior.
- **FR-005**: Admin API responses MUST NOT expose `password_hash`, raw filesystem paths, storage paths, API keys, encrypted API keys, or internal exception details.

#### Admin Home Dashboard

- **FR-006**: Admin Home MUST display real total non-deleted users count (consistent with Admin Users table which excludes soft-deleted users).
- **FR-007**: Admin Home MUST display real total active non-deleted saved resumes count.
- **FR-008**: Admin Home MUST display `Total tokens sent = 0` with WIP badge.
- **FR-009**: Admin Home MUST display `Total tokens generated = 0` with WIP badge.
- **FR-010**: Admin Home MUST include Users, Resumes, and AI Models quick action cards.
- **FR-011**: Users card MUST navigate to `/app/admin/users`.
- **FR-012**: Resumes card MUST navigate/scroll to Admin Resumes section on `/app/admin`.
- **FR-013**: AI Models card MUST navigate to `/app/admin/ai-models`.

#### Admin Resumes on Admin Home

- **FR-014**: Admin Resumes table MUST live on `/app/admin`.
- **FR-015**: The system MUST NOT create a separate `/app/admin/resumes` full page for this feature.
- **FR-016**: Admin Resumes table MUST list saved resumes across users.
- **FR-017**: Admin Resumes table MUST exclude soft-deleted resumes by default.
- **FR-018**: Admin Resumes table MUST support pagination.
- **FR-019**: Admin Resumes table MUST support search.
- **FR-020**: Admin Resumes search MUST cover resume title, vacancy title, company name, owner username, owner email, and owner full name where available.
- **FR-021**: Admin Resumes table MUST support language filter.
- **FR-022**: Admin Resumes table MUST support adaptation level filter.
- **FR-023**: Admin Resumes table MUST support created date range filter.
- **FR-024**: Admin Resumes `createdFrom` MUST be inclusive from start of day.
- **FR-025**: Admin Resumes `createdTo` MUST be inclusive by using before next day start semantics.
- **FR-026**: Frontend MUST prevent selecting `createdTo` earlier than `createdFrom`.
- **FR-027**: Backend MUST handle invalid date ranges safely.
- **FR-028**: Admin Resumes sorting MUST use whitelisted sort fields only.
- **FR-029**: Admin Resumes rows MUST open details modal by row click.
- **FR-030**: Admin Resumes row hover/highlight MUST match User Home saved resumes table behavior.
- **FR-031**: Admin Resumes table MUST include reset filters behavior consistent with User Home saved resumes table.
- **FR-032**: Admin Resumes filter/search changes MUST reset pagination to first page.

#### Resume Details Modal for Admin

- **FR-033**: Admin resume modal MUST reuse existing user-facing resume details behavior where practical.
- **FR-034**: Admin resume modal MUST show owner-safe resume metadata.
- **FR-035**: Admin resume modal MUST support Open PDF, Download PDF, Copy public link, Download HTML when URLs are available.
- **FR-036**: Admin resume modal MUST NOT expose raw local paths or storage directories.
- **FR-037**: Admin resume modal MUST show cover letter with existing preview/full toggle behavior where applicable.
- **FR-038**: Admin resume modal MUST support admin soft-delete resume.
- **FR-039**: Resume delete MUST require confirmation.
- **FR-040**: Resume delete confirm button MUST be disabled/loading during request.
- **FR-041**: Resume delete success MUST close modal and reload relevant table.
- **FR-042**: Resume delete failure MUST keep modal open and show generic localized error.
- **FR-043**: Resume delete MUST be soft-delete only.
- **FR-044**: Resume delete MUST set both `is_deleted = true` and `deleted_at`.

#### Admin Users Table

- **FR-045**: Admin Users page MUST be available at `/app/admin/users`.
- **FR-046**: Admin Users table MUST exclude soft-deleted users by default.
- **FR-047**: Admin Users table MUST support pagination.
- **FR-048**: Admin Users table MUST support one shared text search over username, email, and full name.
- **FR-049**: Admin Users table MUST support account creation date range filter.
- **FR-050**: Account creation date filter label MUST be localized as `Account creation date` / `Дата создания аккаунта`.
- **FR-051**: Admin Users `createdFrom` MUST be inclusive from start of day.
- **FR-052**: Admin Users `createdTo` MUST be inclusive by using before next day start semantics.
- **FR-053**: Frontend MUST prevent selecting account `createdTo` earlier than `createdFrom`.
- **FR-054**: Backend MUST handle invalid account creation date ranges safely.
- **FR-055**: Admin Users table MUST support role filter values `ALL / USER / ADMIN`.
- **FR-056**: Admin Users table MUST support status filter values `ALL / ACTIVE / BLOCKED`.
- **FR-057**: Admin Users table MUST support generation permission filter values `ALL / ALLOWED / FORBIDDEN`.
- **FR-058**: Admin Users table MUST support rights filter values `ALL / PRIVILEGED / NON_PRIVILEGED`.
- **FR-059**: Admin Users table MUST show columns: full name, username, email, role, status, generation permission, rights, resumes count, created date.
- **FR-060**: Admin Users table MUST NOT include action/open-details column.
- **FR-061**: Admin Users rows MUST open details page by row click.
- **FR-062**: Admin Users row hover/highlight MUST match User Home saved resumes table behavior.
- **FR-063**: Admin Users sorting MUST use whitelisted fields only.
- **FR-064**: Admin Users filter/search changes MUST reset pagination to first page.
- **FR-065**: Admin Users reset filters behavior MUST be consistent with User Home saved resumes table.

#### Admin User Details

- **FR-066**: Admin User Details page MUST be available at `/app/admin/users/:userId`.
- **FR-067**: Admin User Details MUST use tabs/sidebar sections similar to Profile page style.
- **FR-068**: Tab order MUST be Contacts, Account, Additional, Resumes.
- **FR-069**: Contacts tab MUST be read-only.
- **FR-070**: Account tab MUST be editable.
- **FR-071**: Additional tab MUST be read-only.
- **FR-072**: Resumes tab MUST show only resumes for selected user.
- **FR-073**: User Details MUST show localized not-found/unavailable state if user does not exist or is soft-deleted.
- **FR-074**: Unsaved changes warning MUST appear only when Account tab has unsaved changes and admin attempts to switch tabs, navigate away, close/back, or leave page.
- **FR-075**: Contacts tab MUST show full name, professional title, phone, location, LinkedIn, portfolio/personal website, Telegram, WhatsApp.
- **FR-075a**: Contacts tab MUST display resume email from `contact_detail.resume_email`. UI label MUST be `Resume email` / `Email for resume` to distinguish it from account email.
- **FR-076**: Additional tab MUST show username, professional information, languages, professional aspirations, achievements, additional context for AI, date of birth, citizenship.
- **FR-077**: Missing read-only values MUST show localized empty state instead of raw null/undefined.

#### Account Access Management

- **FR-078**: Account tab MUST allow editing role, status, generation permission, and privileged flag.
- **FR-078a**: Account tab MUST display account email from `users.email` as a read-only field.
- **FR-079**: Role values MUST be `USER / ADMIN`.
- **FR-080**: Status values MUST be `ACTIVE / BLOCKED`.
- **FR-081**: Generation permission values MUST be `ALLOWED / FORBIDDEN`.
- **FR-082**: Rights/privileged control MUST map to `isPrivileged`.
- **FR-083**: Save MUST require confirmation.
- **FR-084**: Save button MUST be inactive until values change.
- **FR-085**: Save request MUST use loading/disabled state until response.
- **FR-086**: Save success MUST show localized success feedback.
- **FR-087**: Save failure MUST show localized generic error feedback.
- **FR-088**: Backend MUST validate role/status/permission codes against allowed lookup values.
- **FR-089**: Backend MUST reject self-demotion from `ADMIN` to `USER`.
- **FR-090**: Backend MUST reject self-block from `ACTIVE` to `BLOCKED`.
- **FR-091**: Backend MUST allow admin to change own generation permission.
- **FR-092**: Backend MUST allow admin to change own privileged flag.
- **FR-092a**: If a PATCH access request targets a soft-deleted or non-existent user, backend MUST return `404 Not Found`.

#### User Soft-Delete

- **FR-093**: Account tab MUST include Delete account button as danger secondary action.
- **FR-094**: Delete account button MUST be disabled for the current admin's own account.
- **FR-095**: Disabled own-account Delete button MUST provide tooltip explaining that admin cannot delete own account.
- **FR-096**: Backend MUST reject self-delete even if frontend is bypassed.
- **FR-097**: Delete account MUST require confirmation.
- **FR-098**: Delete account request MUST use loading/disabled state until response.
- **FR-099**: Delete account MUST be soft-delete only.
- **FR-100**: Delete account MUST set `users.is_deleted = true`.
- **FR-101**: Delete account MUST set `users.deleted_at`.
- **FR-102**: Delete account MUST set user status to `BLOCKED`.
- **FR-103**: Delete account MUST soft-delete the user's saved resumes.
- **FR-104**: Delete account MUST soft-delete related repeatable profile records only where tables already support soft-delete.
- **FR-105**: Delete account MUST NOT add migrations only to soft-delete contact/additional records in this feature.
- **FR-105a**: When user soft-delete cascade soft-deletes the user's saved resumes, previously valid public URLs for those resumes MUST follow existing soft-deleted public resume behavior (`410 Gone`) and MUST NOT continue serving active PDFs.
- **FR-106**: Delete account operation SHOULD run in one JDBC transaction.
- **FR-107**: Delete account success MUST remove the user from default Admin Users list.
- **FR-108**: Delete account failure MUST show generic localized error.

#### Selected User Resumes Tab

- **FR-109**: Resumes tab MUST show only selected user's resumes.
- **FR-110**: Resumes tab MUST NOT show Create resume button.
- **FR-111**: Resumes tab MUST preserve its own filters/sort/pagination independently from Admin Home Admin Resumes table.
- **FR-112**: Resumes tab MUST support search/filter/sort/pagination consistent with saved resumes table behavior.
- **FR-113**: Resumes tab row click MUST open resume details modal.
- **FR-114**: Resumes tab MUST allow admin soft-delete resume.
- **FR-115**: Resumes tab MUST reload after resume delete.

#### AI Models WIP

- **FR-116**: `/app/admin/ai-models` MUST exist for admin users.
- **FR-117**: `/app/admin/ai-models` MUST display localized Work in Progress placeholder.
- **FR-118**: This feature MUST NOT implement AI model CRUD.
- **FR-119**: This feature MUST NOT display API keys, encrypted API keys, or AI provider configuration details.

#### Localization and UX Consistency

- **FR-120**: All new visible frontend strings MUST be added to `en.json` and `ru.json`.
- **FR-121**: UI labels MUST not display raw i18n keys.
- **FR-122**: Admin table toolbar styling SHOULD reuse or closely match User Home saved resumes table toolbar.
- **FR-123**: Admin table sorting icons/tooltips SHOULD reuse or closely match User Home saved resumes table behavior.
- **FR-124**: Admin date pickers/dropdowns/reset button SHOULD reuse or closely match User Home saved resumes table behavior.
- **FR-125**: Long text values MUST not break layout; wrap or safe truncate with tooltip where appropriate.
- **FR-126**: Empty states MUST be localized.
- **FR-127**: Error/success toasts MUST be localized.

#### Explicit Non-Goals / Scope Boundaries

- **FR-128**: Do not modify PDF renderer, PDF fitting engine, PDF validation, PDF templates, or finalization pipeline.
- **FR-129**: Do not modify AI generation, prompt builder, parser, response validator, budget config, or OpenRouter client behavior.
- **FR-130**: Do not implement AI Models CRUD.
- **FR-131**: Do not implement audit log in this feature.
- **FR-132**: Do not add database migrations unless baseline inspection proves a required existing field is missing and user explicitly approves.
- **FR-133**: Do not add a new `/app/admin/resumes` full page for this feature.
- **FR-134**: Do not hard-delete users, resumes, or profile records.
- **FR-135**: Do not add a new modal-details API endpoint. Resume details modal MUST use already-loaded table DTO data unless current architecture absolutely requires otherwise and the user explicitly approves.

### Key Entities

#### AdminDashboard

Represents Admin Home summary values.

Fields:
- `totalUsers` — real count
- `totalResumes` — real count
- `totalTokensSent` — hardcoded `0` (WIP)
- `totalTokensSentWip` — boolean flag `true`
- `totalTokensGenerated` — hardcoded `0` (WIP)
- `totalTokensGeneratedWip` — boolean flag `true`

#### AdminSavedResume

Represents one saved resume row visible to admin.

Fields:
- `id`, `ownerUserId`, `ownerUsername`, `ownerEmail` (maps to `users.email` — account email), `ownerFullName`
- `resumeTitle`, `vacancyTitle`, `companyName`
- `languageCode`, `languageName`, `adaptationLevel`, `createdAt`
- `publicUrlLink`, `pdfOpenUrl`, `pdfDownloadUrl`, `htmlDownloadUrl`
- `pdfAvailable`, `pdfStatus`, `pdfMessage`, `coverLetter`

Security notes:
- Must not include raw `pdf_file_path`, `html_file_path`, storage directory, local path, or internal path.
- Must not include private user credentials.

#### AdminUserListItem

Represents one Admin Users table row.

Fields:
- `id`, `fullName`, `username`, `email` (`users.email` — account email)
- `roleCode`, `roleName`, `statusCode`, `statusName`
- `permissionCode`, `permissionName`, `isPrivileged`
- `resumesCount`, `createdAt`

#### AdminUserDetails

Represents selected user details with three sections:
- `contacts` (AdminUserContacts) — read-only
- `account` (AdminUserAccount) — editable
- `additional` (AdminUserAdditional) — read-only

#### AdminUserContacts

Read-only. Fields: `fullName`, `professionalTitle`, `resumeEmail`, `phone`, `location`, `linkedinUrl`, `portfolioUrl`, `telegram`, `whatsapp`.

> `resumeEmail` maps to `contact_detail.resume_email`, not `users.email`. UI label SHOULD be `Resume email` / `Email for resume`.

#### AdminUserAccount

Editable except account email which is read-only. Fields: `username`, `accountEmail`, `roleCode`, `roleName`, `statusCode`, `statusName`, `permissionCode`, `permissionName`, `isPrivileged`, `createdAt`, `isCurrentAdmin`.

> `accountEmail` maps to `users.email` and is read-only display-only.

#### AdminUserAdditional

Read-only. Fields: `username`, `professionalInfo`, `languages`, `professionalAspirations`, `achievements`, `aiAdditionalContext`, `dateOfBirth`, `citizenship`.

#### AdminAccessUpdateRequest

Fields: `roleCode`, `statusCode`, `permissionCode`, `isPrivileged`.

Validation:
- role MUST be `USER` or `ADMIN`
- status MUST be `ACTIVE` or `BLOCKED`
- permission MUST be `ALLOWED` or `FORBIDDEN`
- self-demotion forbidden
- self-block forbidden
- self generation permission change allowed
- self privileged flag change allowed

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Admin can open `/app/admin` and see localized dashboard cards without raw i18n keys.
- **SC-002**: Admin Home displays real total users and total resumes.
- **SC-003**: Token stats intentionally show `0` with WIP badge.
- **SC-004**: Admin Home displays all-resumes table directly on `/app/admin`.
- **SC-005**: Admin can search/filter/sort/paginate all resumes.
- **SC-006**: Admin can open resume modal by row click.
- **SC-007**: Admin can soft-delete resume from modal.
- **SC-008**: Admin can open `/app/admin/users`.
- **SC-009**: Admin can search/filter/sort/paginate users.
- **SC-010**: Admin can open user details by row click.
- **SC-011**: Admin User Details uses tab order: Contacts, Account, Additional, Resumes.
- **SC-012**: Account tab allows role/status/permission/rights update with confirmation.
- **SC-013**: Self-delete, self-demotion, and self-block are prevented by backend.
- **SC-014**: Admin can soft-delete another user.
- **SC-014a**: After admin soft-deletes a user, that user's previously valid public resume URLs return `410 Gone` and do not serve active PDFs.
- **SC-015**: Selected user's Resumes tab works independently from Admin Home resumes table.
- **SC-016**: `/app/admin/ai-models` displays WIP page only.
- **SC-017**: All new user-facing text is localized in EN/RU.
- **SC-018**: No admin DTO exposes passwords, API keys, encrypted API keys, or raw file paths.
- **SC-019**: PDF/AI generation pipeline is untouched.

---

## Constitution Alignment

This feature MUST comply with the ResumAIner Constitution principles:

| Principle | Impact on this feature |
|---|---|
| **I. Code Quality & Maintainability** | All Java code follows layered architecture (controller/service/dao/model/config/util). No Spring Boot, JPA, or Hibernate. Maven CLI build must succeed. |
| **II. Testing Excellence** | JUnit 5 + Mockito tests required. TDD for business logic. JaCoCo coverage tracked. Admin authorization and self-protection logic must be tested. |
| **III. User Experience Consistency** | i18n via `en.json`/`ru.json`. Dual validation (frontend + backend). Row click/tab/hover UX must match existing User Home patterns. No stack traces exposed. |
| **IV. Performance & Reliability** | PreparedStatement for all SQL queries. JDBC transaction management (commit/rollback). SQL-level pagination. UTF-8 encoding throughout. |
| **V. Security by Design** | Backend ADMIN authorization is authoritative. Self-protection enforced on backend. No secrets in DTOs. Sort fields whitelisted. |

**Technology Constraint Check** (per Constitution Technology Stack):
- [x] Java 21, Spring MVC (no Spring Boot), Plain JDBC (no ORM) — existing confirmed
- [x] PostgreSQL with Flyway migrations — no new migrations planned unless baseline proves otherwise
- [x] Docker Compose for deployment
- [x] Dev + Prod Spring profiles

---

## Edge Cases

- Admin opens `/app/admin` with no resumes in system.
- Admin opens `/app/admin/users` with no non-deleted users except self.
- Admin searches users and gets no results.
- Admin searches resumes and gets no results.
- Admin selects `createdFrom` then attempts to choose `createdTo` earlier than `createdFrom`.
- Backend receives invalid date ranges despite frontend prevention.
- Admin clicks row while table data is reloading.
- Admin changes filters while on page > 1.
- Admin opens user details for a user deleted in another session.
- Admin saves account access changes for a user who was deleted in another session — backend returns `404 Not Found`.
- Admin opens resume details for a resume deleted in another session.
- Admin attempts to delete own user account by bypassing frontend.
- Admin attempts self-demotion by bypassing frontend.
- Admin attempts self-block by bypassing frontend.
- Admin deletes a user with no resumes.
- Admin deletes a user with many resumes.
- Admin deletes a resume that was already deleted.
- Admin API called by regular USER role.
- Admin API called without authenticated session.
- Long full names, emails, resume titles, vacancy titles, company names, and cover letters.
- Missing contact/additional fields.
- AI Models WIP page opened by admin.
- AI Models WIP page opened by non-admin.

---

## Assumptions

- Existing `AuthInterceptor` only checks authentication, not ADMIN role. An admin authorization check layer (interceptor or controller-level check) will be added for `/api/admin/**` paths.
- Existing `PagedResponse<T>` with `items` field will be reused for all admin list endpoints (field name `items`, not `content`).
- Existing `UserSession` with `role` string field is the authority for admin checks.
- Backend service methods and DAO queries for admin-specific operations (dashboard stats, cross-user resume listing, user details with contact/additional data, access update, user soft-delete with cascades) will be created in a new admin service/DAO layer or added to existing services.
- No new frontend dependencies are introduced. PrimeVue table, dropdown, datepicker, dialog, button, and toast components are reused.
- Row click navigation on Admin Users table navigates via Vue Router to `/app/admin/users/:userId`.
- Admin Resumes on `/app/admin` is rendered as a table section on the same page, below dashboard cards — not as a separate route.
- The application already has role, user_status, and user_permission lookup tables with codes (e.g., `ADMIN`, `USER`, `ACTIVE`, `BLOCKED`, `ALLOWED`, `FORBIDDEN`).
- Soft-delete for users follows the existing pattern: `is_deleted` flag + `deleted_at` timestamp.

---

## API Contract Draft

> Endpoint behavior and response safety are requirements. Exact class names may be refined in `plan.md`.

### Dashboard

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

### All Admin Resumes

```
GET /api/admin/resumes?page=0&size=10&search=&language=&adaptationLevel=&createdFrom=&createdTo=&sort=createdAt,desc
```

Response: `PagedResponse<AdminSavedResume>` (uses `items` field per existing pattern)

### Delete Resume as Admin

```
DELETE /api/admin/resumes/{resumeId}
```

Success: `{ "message": "Resume deleted successfully." }`
Error: generic, localized on frontend.

### Admin Users

```
GET /api/admin/users?page=0&size=10&search=&role=&status=&permission=&rights=&createdFrom=&createdTo=&sort=createdAt,desc
```

Response: `PagedResponse<AdminUserListItem>`

### Admin User Details

```
GET /api/admin/users/{userId}
```

Response: `AdminUserDetails` (contacts + account + additional + id)

### Update User Access

```
PATCH /api/admin/users/{userId}/access
```

Request: `{ "roleCode": "ADMIN", "statusCode": "ACTIVE", "permissionCode": "ALLOWED", "isPrivileged": true }`

Success: `{ "message": "User access updated successfully." }`
Self-protection error: `{ "message": "This action is not allowed for your own admin account." }`

### Delete User as Admin

```
DELETE /api/admin/users/{userId}
```

Success: `{ "message": "User account deleted successfully." }`
Error: generic, localized on frontend.

### Selected User Resumes

```
GET /api/admin/users/{userId}/resumes?page=0&size=10&search=&language=&adaptationLevel=&createdFrom=&createdTo=&sort=createdAt,desc
```

Response: `PagedResponse<AdminSavedResume>`, scoped to the selected user.
