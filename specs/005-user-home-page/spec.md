# Feature Specification: User Home Page & Resume Workspace

**Feature Branch**: `feat/005-user-home-page`

**Created**: 2026-06-06

**Status**: Clarified

**Input**: User description: "Let's create the User Home Page the resume workspace. We move the SPA to /app/, leaving the root directory for the landing page. We implement a welcome section with a guide on filling out the profile, summary cards, a table of saved resumes with search/filters/sorting/pagination, a resume details modal with PDF/link/delete options, and a general navigation bar."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Authenticated user with complete profile and saved resumes (Priority: P1)

The user has filled in their contact details, work experience, and education. They have several saved resumes. The User Home displays a guided "next best step" block with Generate Resume as the primary action, three summary cards, and a table of saved resumes with full search/filter/sort/paginate functionality.

**Why this priority**: This is the core happy path — the primary reason the User Home exists is to help prepared users manage resumes and navigate to generation.

**Independent Test**: Can be fully tested by opening User Home with a ready profile and 5+ saved resumes. User sees Generate CTA, three summary cards, and a functional DataTable.

**Acceptance Scenarios**:

1. **Given** the user has a complete profile and multiple saved resumes, **When** they open User Home, **Then** the page shows "Resume workspace" heading, guided block with Generate Resume as primary CTA, three summary cards, and a paginated table of saved resumes.
2. **Given** the user has a last resume, **When** they view summary cards, **Then** the Last Resume card shows the resume title and date, and clicking it opens the Resume Details modal.
3. **Given** the user is on the Saved Resumes table, **When** they use Search, language/adaptation level/date filters, or sort columns, **Then** the table updates results accordingly with working pagination.

---

### User Story 2 — Authenticated user with incomplete profile (Priority: P2)

The user has not yet completed their profile. The User Home shows only profile completion guidance, a checklist of missing items (contact details, work experience, education), and a Complete Profile CTA. No generate functionality is accessible until the profile is ready.

**Why this priority**: Second most common state during onboarding. Users need clear guidance on what to do next.

**Independent Test**: Can be fully tested by opening User Home with a partially filled profile. User sees only completion guidance, no Generate CTA, and an actionable checklist.

**Acceptance Scenarios**:

1. **Given** the user has an incomplete profile, **When** they open User Home, **Then** the guided block shows "Complete your profile first" with a checklist and Complete Profile CTA.
2. **Given** the profile is incomplete, **When** the user views the Saved Resumes section, **Then** no Generate Resume button is shown in the section header.
3. **Given** the profile is incomplete, **When** the user navigates to Generate Resume via navbar, **Then** they see a blocking state with the same checklist and Complete Profile CTA, not the generation form.

---

### User Story 3 — Authenticated user with complete profile and no saved resumes (Priority: P3)

The user has completed their profile but has not yet generated any resumes. The guided block shows Generate Resume as the primary action. The Saved Resumes section shows a meaningful empty state with a CTA to create the first resume.

**Why this priority**: Important onboarding state but less critical than the two main states above.

**Independent Test**: Can be tested by opening User Home with a ready profile and zero saved resumes. User sees Generate CTA and an empty state with a "Generate Resume" call to action.

**Acceptance Scenarios**:

1. **Given** the user has a ready profile but no saved resumes, **When** they view the table, **Then** they see empty state: "No resumes yet" with description "Create your first resume for a specific vacancy" and a Generate Resume CTA.
2. **Given** the user has no saved resumes, **When** they view summary cards, **Then** the Last Resume card shows "No resumes yet".

---

### User Story 4 — User opens Resume Details modal and interacts (Priority: P2)

The user clicks on a saved resume row in the table or the Last Resume card. A modal opens with full resume details: title, vacancy, company, language, adaptation level, creation date, public link, and cover letter. The user can View PDF, Download PDF, Copy public link, or Delete the resume.

**Why this priority**: Core interaction with individual resume data. Essential for the resume management workflow.

**Independent Test**: Can be tested by clicking any table row or Last Resume card. Modal opens with correct data and all four actions are available.

**Acceptance Scenarios**:

1. **Given** a saved resume exists, **When** the user clicks its table row or the Last Resume card, **Then** a modal opens showing resume title, vacancy, company, language, adaptation level, creation date, public link, and cover letter section.
2. **Given** the Resume Details modal is open, **When** the user clicks Copy Link, **Then** the public link is copied and a toast notification confirms.
3. **Given** the Resume Details modal is open, **When** the user clicks Delete, **Then** a confirmation dialog appears before deletion.

---

### User Story 5 — User deletes a saved resume (Priority: P3)

From the Resume Details modal, the user clicks Delete, confirms in the dialog, and the resume is removed. The modal closes, the table refreshes, summary cards update, and a success toast appears.

**Why this priority**: Important user control mechanism but depends on the modal functioning first.

**Independent Test**: Can be tested by deleting a resume from the modal. Confirmation appears, deletion succeeds, table and summary update.

**Acceptance Scenarios**:

1. **Given** the delete confirmation dialog is open, **When** the user clicks Cancel, **Then** the dialog closes and nothing changes.
2. **Given** the delete confirmation dialog is open, **When** the user clicks Delete, **Then** the resume is removed, modal closes, table refreshes, summary updates, and a success toast appears.

---

### User Story 6 — SPA routing and navigation (Priority: P1)

The landing page remains at root `/`. All SPA pages live under `/app/...`. After login/register, users are redirected to `/app/home`. The header shows consistent navigation with Home, My Profile, Generate Resume, and Admin (visible only to admin users). Logout redirects to `/app/auth`.

**Why this priority**: Foundation for all other stories. Routing and navigation must work before any page can be used.

**Independent Test**: Can be tested by navigating to `/` (landing page is served), `/app/home` (User Home opens), and verifying the header navigation is present.

**Acceptance Scenarios**:

1. **Given** the user is at site root, **When** they visit `/`, **Then** the existing landing page is served (not the Vue SPA).
2. **Given** the user is authenticated, **When** they visit `/app/home`, **Then** the User Home page opens with the header.
3. **Given** the user is not an admin, **When** they visit `/app/admin` directly, **Then** they are redirected away or shown a 403 error.
4. **Given** the user clicks Logout, **When** logout completes, **Then** they are redirected to `/app/auth`.

### Edge Cases

- What happens when the API call for User Home data fails? → User sees an inline error block inside the page container with an error icon, descriptive message, and a "Retry" button. Not a toast notification, not a full-screen overlay.
- What happens during initial page load? → Loading state with skeleton placeholders.
- What if one API endpoint succeeds and the other fails? → Each block (Guided+Summary and Saved Resumes Table) handles errors independently. A failed table does not block the guided block from rendering.
- What if the user has no work experience or education records? → Profile is considered incomplete, guided block shows checklist.
- What if search returns zero results? → Table shows "No resumes found" empty state with suggestion to try different search/filters.
- What if a resume has no cover letter? → Modal shows a message "No cover letter for this resume."
- What happens on mobile devices? → Summary cards stack vertically, DataTable has horizontal scroll, modal fits small screens.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST preserve the existing landing page at site root `/`.
- **FR-002**: All SPA routes MUST be served under `/app/...` prefix.
- **FR-003**: After successful login or registration, users MUST be redirected to `/app/home`.
- **FR-004**: After logout, users MUST be redirected to `/app/auth`.
- **FR-005**: Unauthenticated users visiting any `/app/*` route MUST be redirected to `/app/auth`.
- **FR-006**: The application MUST display a consistent header/navigation bar on all SPA pages.
- **FR-007**: The header MUST include: logo (clickable to home), Home link, My Profile link, Generate Resume link, language switcher, and logout button.
- **FR-008**: The Admin navigation link MUST only be visible to users with the admin role.
- **FR-009**: The `/app/admin` route MUST be inaccessible to non-admin users.
- **FR-010**: User Home MUST determine profile readiness using the rule: contact details complete AND has work experience AND has education.
- **FR-011**: Contact details are considered complete when the user has full name, email, phone, and location filled in.
- **FR-012**: Work experience is considered present when at least one complete non-deleted work record exists (with job title, company, start date, description, and end date or current role flag).
- **FR-013**: Education is considered present when at least one complete non-deleted education record exists (with institution name, degree, and required date fields).
- **FR-014**: If the profile is incomplete, User Home MUST show the profile completion guidance block with a checklist and Complete Profile CTA. No resume generation CTA should be shown in the guided block.
- **FR-015**: If the profile is ready, User Home MUST show "Your next best step" guidance with Generate Resume as the primary action and Update Profile as the secondary action.
- **FR-016**: User Home MUST display three summary cards: Saved resumes count, Profile status, and Last resume.
- **FR-017**: The Last Resume card MUST be clickable and open the Resume Details modal for the most recent resume.
- **FR-018**: The Saved Resumes section MUST use a table with columns: Resume title, Vacancy, Company, Language, Adaptation level, and Created date.
- **FR-019**: Vacancy and Company columns MUST truncate overflowing text using CSS (`max-width` percentage, `text-overflow: ellipsis`). The full value MUST be accessible via tooltip on hover or focus.
- **FR-020**: Created date MUST be displayed in YYYY-MM-DD format.
- **FR-021**: The table MUST support sorting on all six columns with removable sort (third click clears sorting).
- **FR-022**: Default sort MUST be by Created date descending (newest first).
- **FR-023**: The table MUST support live text search with 300ms debounce across resume title, vacancy, and company fields. Search MUST activate only when at least 3 characters are entered.
- **FR-024**: The table MUST support filtering by language (English/Russian) via multi-select.
- **FR-025**: The table MUST support filtering by adaptation level (Minimal/Balanced/Maximum) via multi-select.
- **FR-026**: The table MUST support filtering by exact created date via date picker.
- **FR-027**: The table MUST support pagination with options for 10, 20, and 50 rows per page, defaulting to 10.
- **FR-028**: The header/navbar MUST render immediately on page navigation. The Guided+Summary block and Saved Resumes table MUST show skeleton/loading states independently. Each block transitions to content when its respective API response arrives. (Cross-reference: FR-046 defines failure isolation for the same independent blocks.)
- **FR-029**: Empty states MUST be context-aware: different messages for incomplete profile with no resumes, ready profile with no resumes, and no search results.
- **FR-030**: If the profile is ready, a Generate Resume button MUST appear in the Saved Resumes section header.
- **FR-031**: If the profile is incomplete, no Generate Resume button MUST appear in the Saved Resumes section header.
- **FR-032**: The Generate Resume navbar item MUST remain visible even when profile is incomplete.
- **FR-033**: When profile is incomplete and user enters the Generate Resume flow, they MUST see a blocking state with profile checklist and Complete Profile CTA instead of the generation form.
- **FR-034**: Clicking a table row or Last Resume card MUST open a Resume Details modal.
- **FR-035**: The Resume Details modal MUST show: resume title, vacancy, company, language, adaptation level, creation date, public link, and cover letter section.
- **FR-036**: The modal MUST NOT display token usage, AI model name, PDF status, or other technical metadata.
- **FR-037**: The modal MUST provide actions: View (opens PDF in new tab), Download PDF (file download), Copy Link (copies public URL), and Delete (opens confirmation dialog).
- **FR-038**: The modal MUST include a collapsible cover letter section. If absent, show a message instead.
- **FR-039**: Delete action MUST require confirmation via a dialog before executing.
- **FR-040**: After successful deletion: modal closes, table data refreshes, summary data refreshes, and a success toast appears.
- **FR-041**: All user-visible text MUST be available in English and Russian, using a friendly `ты` tone for Russian.
- **FR-042**: Copy actions (link, cover letter) MUST show a confirmation toast.
- **FR-043**: The Generate Resume stepper (Vacancy → Settings → Review → Export) MUST preserve user-entered data when navigating between steps.
- **FR-044**: Placeholder pages MUST exist for all My Profile sections, Generate Resume steps, and Admin page, showing a clear placeholder indicator.
- **FR-045**: When an API call for User Home data fails, the system MUST display an inline error block within the page container, showing an error icon, a user-readable message, and a "Retry" button.
- **FR-046**: The Guided+Summary section and the Saved Resumes Table MUST load data independently. A failure in one MUST NOT block the other from rendering.

### Key Entities

- **User Home Summary**: Aggregate of profile readiness status, checklist state, saved resumes count, and last resume preview. Used to populate the guided block and summary cards.
- **Saved Resume**: A finalized, saved resume record containing metadata like title, associated vacancy, company, language, adaptation level, creation date, public URL, PDF URL, and cover letter content.
- **Profile Readiness State**: Computed product-level assessment of whether a user's profile is sufficiently complete to generate resumes. Based on contact completeness, work experience presence, and education presence.
- **Profile Checklist**: A breakdown of which sections of the user's profile are complete or missing (contact details, work experience, education).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Root URL `/` continues to serve the existing landing page — verified by visiting `/` and confirming it is not the Vue SPA.
- **SC-002**: All SPA pages are accessible under `/app/...` — verified by navigating to `/app/home`, `/app/profile/contact`, `/app/generate/vacancy`, and confirming they load within the SPA.
- **SC-003**: An authenticated user with a complete profile can view their resume workspace, see three summary cards, and interact with the saved resumes table within 3 seconds of page load.
- **SC-004**: An authenticated user with an incomplete profile sees clear guidance and a clickable checklist — verified by visiting User Home with a partially filled profile.
- **SC-005**: Users can search, filter, sort, and paginate through their saved resumes — all twelve approved column operations function correctly.
- **SC-006**: Users can open the Resume Details modal from any table row or the Last Resume card, view full details, and perform all four actions (View, Download, Copy Link, Delete).
- **SC-007**: Delete flow works end-to-end with confirmation, data refresh, and toast notification — verified by deleting a resume.
- **SC-008**: Admin navigation item is visible to admin users only — verified by comparing the header for admin vs. regular user roles.
- **SC-009**: All user-visible text in the feature is available in both English and Russian with no hardcoded strings in templates.
- **SC-010**: Responsive layout works on desktop (1280px content, 3 cards in a row), tablet (cards wrap), and mobile (cards stack, table scrolls horizontally).
- **SC-011**: The Generate Resume navbar item is always visible, but the generation form is blocked with guidance when profile is incomplete.

## Constitution Alignment

This feature MUST comply with the ResumAIner Constitution principles:

| Principle | Impact on this feature |
|---|---|
| **I. Code Quality & Maintainability** | All Java code follows layered architecture (controller/service/dao/model/config/util). No Spring Boot, JPA, or Hibernate. Maven CLI build must succeed. Components split into small focused files. |
| **II. Testing Excellence** | JUnit 5 + Mockito tests required. TDD for business logic. Standalone MockMvc for controller tests (avoid DB dependency). JaCoCo coverage tracked. Service layer tests for profile readiness calculation. |
| **III. User Experience Consistency** | i18n via en.json/ru.json for all visible strings. Dual validation (frontend + backend). PRG pattern for form submissions. Empty states for all data views. Card list pattern for repeatable sections. 1280px max-width layout. |
| **IV. Performance & Reliability** | PreparedStatement for SQL queries. SQL-level pagination (LIMIT/OFFSET) with indexes for resume list queries. Skeleton loading states for initial page load. UTF-8 encoding throughout. |
| **V. Security by Design** | Backend validation is authoritative. Admin nav item hidden in UI is NOT security — backend must enforce admin-only access. No secrets in logs. Public resume links expose only finalized PDF output. Error responses never expose stack traces. |

**Technology Constraint Check** (per Constitution Technology Stack):
- [ ] Java 21, Spring MVC (no Spring Boot), Plain JDBC (no ORM)
- [ ] PostgreSQL with Flyway migrations
- [ ] Docker Compose for deployment
- [ ] Dev + Prod Spring profiles

## Clarifications

### Session 2026-06-06

- Q: How should API failure errors be displayed on User Home? → A: Inline error block inside the page container with an icon, descriptive message, and a "Retry" button. Not a toast, not a full-screen overlay.
- Q: What truncation approach for Vacancy and Company columns in the DataTable? → A: CSS-based truncation with max-width percentage (responsive). No fixed character count.
- Q: How to handle partial API failure when one endpoint succeeds and the other fails? → A: Guided+Summary block and Saved Resumes table load independently. If one fails, only that block shows an inline error with retry. The other block remains functional.
- Q: What search trigger for the Saved Resumes table? → A: Live debounce search (300ms) with a minimum of 3 characters before triggering a search request. Hard rule: fewer than 3 characters does not trigger search.
- Q: What loading sequence on initial page load? → A: Header renders immediately (static). Guided+Summary block shows skeleton. Saved Resumes table shows skeleton as well. Each block transitions to content independently when its API response arrives.

## Assumptions

- Users must be authenticated to access any `/app/*` route — unauthenticated access redirects to auth page.
- Admin role is determined server-side and communicated to the frontend via the session/auth state.
- The Generate Resume flow (Vacancy → Settings → Review → Export) is a placeholder in this feature and will be fully implemented later.
- Profile sections (Contact, Experience, Education, Projects, Courses, Additional info) are placeholders in this feature — their full implementation belongs to future features.
- The Admin page is a placeholder in this feature — full admin functionality belongs to a future feature.
- PDF generation and public URL generation are handled by the backend and assumed to have working endpoints.
- Resume soft-delete is handled by the backend — this feature calls the delete endpoint and refreshes the UI.
- The existing language switcher pattern is reused and extended with the required i18n keys.
- Desktop is the primary target; tablet and mobile are secondary with responsive adaptations.
- The landing page at `/` is a Thymeleaf page served by the backend, not part of the Vue SPA.
