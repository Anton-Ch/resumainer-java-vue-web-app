# Feature Specification: User Profile Page

**Feature Branch**: `feat/006-profile-page`

**Created**: 2026-06-07

**Status**: Approved

**Input**: User description from `tempfiles/prototype/profile_brief_opencode.md`

## Clarifications

### Session 2026-06-07

- **Q1: Data volume per profile section** → A: Option B — Work Experience up to 20, Courses 50-300 (high-volume), other record-based sections up to 20-30. Courses require backend pagination with LIMIT/OFFSET. Confirmed from BA artifact: `docs/04_domain-and-data-model/data_dictionary.md` tables exist for all sections.
- **Q2: Work format values** → A: Option A — Use BA data dictionary values (`work_format.code`): `full-time`, `part-time`, `rotational_schedule`, `internship`, `offline`, `remote`, `hybrid`, `on_project_site`. Prototype will be extended during implementation to match BA authoritative set.
- **Q3: Willingness dropdown values** → A: Option A — Use `Yes / No / Negotiable` (match brief/prototype). DB column `ready_for_relocation` and `ready_for_business_trips` will accept these values.
- **Q4: Date of Birth and Citizenship required vs optional** → A: Option C — Both required (NOT NULL). Matches BA data dictionary. Date of birth must be a valid full date; citizenship is a text field.
- **Background confirmed from BA artifacts**: Error handling is already standardized per NFR-003/004/005 (global exception handler, user-friendly error responses, no stack traces). Username uniqueness enforced at DB level via UNIQUE constraint on `users.username`. Automatic sorting (end_date DESC) per DEC-012.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View and Edit Contact Details (Priority: P1)

As a logged-in user, I want to view and edit my contact details (full name, professional title, email, phone, location, social links) so that my professional profile is accurate and up to date.

**Why this priority**: Contact details are the foundation of every resume. Without accurate personal information, no generated resume is usable. This is the primary profile section.

**Independent Test**: Can be fully tested by navigating to `/profile/contact`, filling required fields, saving, and confirming the data persists after page refresh.

**Acceptance Scenarios**:

1. **Given** I am a logged-in user, **When** I navigate to `/profile/contact`, **Then** I see editable fields for full name, professional title, email, phone, location, LinkedIn URL, portfolio URL, Telegram, and WhatsApp.
2. **Given** I have filled all required fields (full name, professional title, email, phone, location), **When** I click "Save", **Then** the data is saved and the section status shows "Completed".
3. **Given** I enter an invalid email format, **When** I try to save, **Then** the form shows a validation error and does not save.
4. **Given** I enter a URL without protocol (e.g., `linkedin.com/in/example`), **When** I save, **Then** the URL is accepted as valid.
5. **Given** I have unsaved changes, **When** I try to navigate away, **Then** I see a confirmation dialog warning about unsaved changes.

---

### User Story 2 - Manage Work Experience Records (Priority: P1)

As a logged-in user, I want to add, edit, and delete work experience entries so that my resume reflects my career history.

**Why this priority**: Work experience is the most critical section for resume generation alongside contact details.

**Independent Test**: Can be fully tested by adding a new work entry with required fields, editing it, deleting it, and verifying the record count updates correctly.

**Acceptance Scenarios**:

1. **Given** I have no work experience entries, **When** I navigate to `/profile/experience`, **Then** I see an empty state and an option to add a new record.
2. **Given** I click "Add", **When** I fill required fields (job title, company name, start date, description) and save, **Then** a new record card appears in the list.
3. **Given** I check "I currently work here", **When** the form updates, **Then** the End Date field is hidden and the card displays "Present".
4. **Given** I try to save an empty form, **When** I click "Save", **Then** validation errors appear and no record is created.
5. **Given** I edit an existing record, **When** I change values and save, **Then** the card updates with new information.
6. **Given** I have unsaved changes in the inline form, **When** I try to navigate away, **Then** I see a warning dialog.

---

### User Story 3 - Manage Projects and Volunteering (Priority: P2)

As a logged-in user, I want to add, edit, and delete project/volunteering entries to showcase my practical experience.

**Why this priority**: Projects complement work experience and are important for resume quality, but secondary to core work history.

**Independent Test**: Can be tested by adding a project entry, toggling "ongoing", verifying card displays correctly, editing, and deleting.

**Acceptance Scenarios**:

1. **Given** I navigate to `/profile/projects`, **When** I click "Add", **Then** I see an inline form with fields for project name, role, dates, description, and project URL.
2. **Given** I check "This project is ongoing", **When** the form updates, **Then** the End Date field is hidden.
3. **Given** I try to save without a project name or description, **When** I click "Save", **Then** validation errors are shown.
4. **Given** I delete a project, **When** I confirm deletion, **Then** the record is removed and the count updates.

---

### User Story 4 - Manage Education Records (Priority: P2)

As a logged-in user, I want to add, edit, and delete education entries to document my academic background.

**Why this priority**: Education is a standard resume section but typically secondary to work experience for professional roles.

**Independent Test**: Can be tested by adding education with institution, degree, dates, toggling "currently studying", and verifying card display.

**Acceptance Scenarios**:

1. **Given** I navigate to `/profile/education`, **When** I see the empty state, **Then** I see a helpful message: "No education added yet. Add your education information to help improve generated resumes."
2. **Given** I check "I am currently studying here", **When** the form updates, **Then** the End Date field is hidden.
3. **Given** I save an education record, **When** I view the card, **Then** it shows institution name, date range, location, degree, and field of study in the specified order.
4. **Given** I try to save without institution name, degree, or start date, **When** I click "Save", **Then** validation errors appear.

---

### User Story 5 - Manage Courses and Certificates (Priority: P2)

As a logged-in user, I want to add, view, edit, and delete course/certificate records in a table layout, with search and filter capabilities for high-volume data.

**Why this priority**: The courses section uses a different UX pattern (DataTable with search/filter) than other sections, making it a distinct implementation concern.

**Independent Test**: Can be tested by adding multiple courses, using search, applying date filters, sorting columns, opening row details, editing, and deleting.

**Acceptance Scenarios**:

1. **Given** I have 15 courses, **When** I navigate to `/profile/courses`, **Then** I see a DataTable with pagination (10 per page).
2. **Given** I type "Java" in search, **When** search has 3+ characters, **Then** the table filters to matching records.
3. **Given** I type "Ja" in search, **When** search has only 2 characters, **Then** filtering is not applied yet.
4. **Given** I click a row, **When** the dialog opens, **Then** I see full course details in view mode.
5. **Given** I click Add/Edit, **When** I fill required fields and save, **Then** the table updates with the new/edited record.
6. **Given** I delete a course, **When** I confirm via the confirmation dialog, **Then** the record is removed.
7. **Given** I apply filters or sorting, **When** I click "Reset", **Then** all filters, search, and sorting return to defaults.

---

### User Story 6 - Manage Additional Profile Info (Priority: P2)

As a logged-in user, I want to configure my username, resume language preferences, work preferences, professional info, and personal info in one section.

**Why this priority**: This section consolidates settings that affect resume generation and public profile behavior.

**Independent Test**: Can be tested by setting username, toggling default/additional languages (mutual exclusivity), setting work preferences, and saving.

**Acceptance Scenarios**:

1. **Given** I navigate to `/profile/additional`, **When** I see the form, **Then** it is organized into four visual blocks: Resume & Public Profile Preferences, Work Preferences, Professional Info, Personal Info.
2. **Given** I enter a username with Cyrillic characters, **When** I try to save, **Then** validation rejects it (English letters, digits, underscores, and hyphens only).
3. **Given** I change Default language to Russian, **When** the form updates, **Then** Additional language automatically changes to English (they are mutually exclusive).
4. **Given** I set a date of birth, **When** I save, **Then** the full exact date is stored (not year-only).
5. **Given** I fill required fields (username, default language, additional language, date of birth, citizenship) and save, **Then** the section status shows "Completed".
6. **Given** I leave date of birth or citizenship empty, **When** I try to save, **Then** validation errors appear and the section status remains "Incomplete".

---

### Edge Cases

- What happens when all records in a section are deleted? → Empty state with guidance text is shown.
- What happens when user navigates between sections with unsaved changes? → Unsaved Changes dialog appears.
- What happens when user refreshes the browser with unsaved changes? → Browser beforeunload warning fires.
- What happens when the username is already taken by another user? → Inline validation error appears: "This username is already taken. Please try a different one." / "Это имя пользователя уже занято. Попробуйте другой вариант." User must choose a different username before saving.
- What happens when date of birth or citizenship are empty? → Validation error prevents save, section status stays "Incomplete".
- What happens when backend API returns an error during save? → User sees a descriptive error toast and data is not lost.
- What happens when the mobile/tablet viewport is very narrow? → Sidebar becomes 2-row × 3-column grid tabs.
- What happens when courses search returns no results? → "No records found" empty table state is displayed.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow authenticated users to access a Profile area with six distinct sections: Contact Details, Work Experience, Projects & Volunteering, Education, Courses & Certificates, Additional Info.
- **FR-002**: System MUST provide a navigation sidebar (desktop) or tab grid (mobile/tablet) for switching between profile sections.
- **FR-003**: System MUST display completion status for each section in the sidebar/tabs (Completed/Incomplete for simple sections, record counts for list-based sections).
- **FR-004**: System MUST validate Contact Details: email must be valid, URL fields must accept values with or without protocol prefix.
- **FR-005**: System MUST require full name, professional title, email, phone, and location for Contact Details to be marked "Completed".
- **FR-006**: System MUST allow adding, editing, and deleting Work Experience records with inline form above the record list.
- **FR-007**: System MUST require job title, company name, start date, and description for Work Experience records.
- **FR-008**: System MUST hide End Date field when "I currently work here" is checked for Work Experience.
- **FR-009**: System MUST display "Present" on Work Experience cards when currently employed.
- **FR-010**: System MUST allow adding, editing, and deleting Project records with inline form.
- **FR-011**: System MUST require project name and description for Project records.
- **FR-012**: System MUST hide End Date when "ongoing" checkbox is checked for Projects.
- **FR-013**: System MUST display "Present" on Project cards when ongoing.
- **FR-014**: System MUST allow adding, editing, and deleting Education records with inline form.
- **FR-015**: System MUST require institution name, degree/qualification, and start date for Education records.
- **FR-016**: System MUST hide End Date when "currently studying" checkbox is checked for Education.
- **FR-017**: System MUST show an Education empty state with guidance text.
- **FR-018**: System MUST display Courses & Certificates in a DataTable with server-side pagination (10/20/50 per page, default 10), as users may have up to 300 course records.
- **FR-019**: System MUST support search across course name, provider, and skills fields with minimum 3 characters before filtering.
- **FR-020**: System MUST support date range filtering (From/To) for course start dates with To date not earlier than From date.
- **FR-021**: System MUST provide a Reset button that clears all filters, search, sorting, and resets pagination.
- **FR-022**: System MUST allow adding, viewing, editing, and deleting course records via dialog.
- **FR-023**: System MUST require course name, provider, and start date for Course records.
- **FR-024**: System MUST validate course date range (End Date cannot be before Start Date).
- **FR-025**: System MUST display Additional Info in four visual blocks: Resume & Public Profile Preferences, Work Preferences, Professional Info, Personal Info.
- **FR-026**: System MUST require username (English letters/digits/underscores/hyphens only, no Cyrillic, no spaces), default resume language, additional resume language, date of birth (full exact date), and citizenship for Additional Info completion.
- **FR-027**: System MUST enforce mutual exclusivity of default and additional resume languages: if one changes, the other switches automatically.
- **FR-028**: System MUST validate username uniqueness: if the requested username is already taken, an inline error is shown ("This username is already taken. Please try a different one.") and the user must choose a different username before saving.
- **FR-029**: System MUST provide a username helper text explaining it will be part of the public resume link.
- **FR-030**: System MUST provide acceptable work formats as a checkbox group with values from the BA data dictionary: `full-time`, `part-time`, `rotational_schedule`, `internship`, `offline`, `remote`, `hybrid`, `on_project_site`.
- **FR-031**: System MUST support three willingness-to-relocate/travel dropdown values: Yes, No, Negotiable.
- **FR-032**: System MUST use full exact date pickers for all date fields (not month/year-only).
- **FR-033**: System MUST validate that End Date is not earlier than Start Date where both exist.
- **FR-034**: System MUST implement unsaved-changes warning for all forms when navigating away or refreshing the browser.
- **FR-035**: System MUST reset dirty state after successful save, after cancel, or after confirmed leave.
- **FR-036**: System MUST persist profile data via backend API calls to a PostgreSQL database, so data is durable across sessions and devices.
- **FR-037**: System MUST use short save button labels: "Save" (EN) / "Сохранить" (RU).
- **FR-038**: System MUST display "Fields marked with * are required" note near the save button, not at the top of the form.
- **FR-039**: System MUST show toast notifications without a period at the end.
- **FR-040**: System MUST validate all forms on blur and on submit, not aggressively on every keystroke.
- **FR-041**: System MUST NOT save forms with invalid or missing required fields.

### Key Entities *(include if feature involves data)*

- **ContactDetails**: Personal identification and contact information — full name, professional title, email, phone, location, social/portfolio URLs.
- **WorkExperience**: Individual employment history entry — job title, company, dates, location, description.
- **Project**: Project or volunteering entry — project name, role, dates, description, URL.
- **Education**: Academic qualification entry — institution, degree, field of study, dates, GPA, location.
- **Course**: Course or certificate entry — course name, provider, dates, credential URL, skills, description.
- **AdditionalInfo**: Extended profile settings — username, language preferences, work format preferences, relocation willingness, skills, languages, aspirations, personal info.
- **ProfileSectionStatus**: Metadata about each section's completion status (completed/incomplete or record count) shown in the sidebar navigation.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can navigate between all six profile sections and see the corresponding form within 2 seconds of clicking a sidebar/tab item.
- **SC-002**: Users can add a new Work Experience record (fill 4+ fields, save) in under 30 seconds.
- **SC-003**: Users can locate a specific course in a list of 50+ entries using search in under 10 seconds.
- **SC-004**: All form validation errors are displayed within the form (inline messages) — no console errors or page crashes.
- **SC-005**: Unsaved changes warning appears within 1 second of attempting navigation with dirty state on all applicable sections.
- **SC-006**: After saving data and refreshing the page, all previously saved data is restored (data persistence verified).
- **SC-007**: Section status indicators in the sidebar/tabs update immediately after save operations (within 1 second).
- **SC-008**: The mobile layout (viewport < 768px) shows tab navigation instead of sidebar, with all six sections accessible.
- **SC-009**: All user-facing Profile UI strings are displayed in the correct language (EN or RU) when switching languages, with no hardcoded English text remaining.
- **SC-010**: The project build completes without errors after all new files are added and modified.

## Constitution Alignment

This feature MUST comply with the ResumAIner Constitution principles:

| Principle | Impact on this feature |
|---|---|
| **I. Code Quality & Maintainability** | All new frontend and backend code follows existing project architecture: Vue 3 + TypeScript + PrimeVue on frontend, Spring MVC + plain JDBC + DAO on backend. Each profile section (contact, experience, education, projects, courses, additional info) has corresponding backend REST endpoints and DAO methods. |
| **II. Testing Excellence** | Manual testing as defined in regression checklist. Build must pass with zero TypeScript errors. |
| **III. User Experience Consistency** | i18n via vue-i18n for all UI strings — bilingual EN/RU. PrimeVue consistent with existing design DNA. Short save button labels. Toast messages without periods. Subtle status styling (not large badges). |
| **IV. Performance & Reliability** | Courses DataTable uses pagination for performance with large datasets. Search applies only after 3 characters to avoid unnecessary filtering. |
| **V. Security by Design** | Username validation restricts characters (no Cyrillic, no spaces, no special chars). All validation occurs on blur and submit. No profile picture upload (explicitly excluded). |

**Technology Constraint Check** (per Constitution Technology Stack):
- [ ] Java 21, Spring MVC (no Spring Boot), Plain JDBC (no ORM)
- [ ] PostgreSQL with Flyway migrations
- [ ] Docker Compose for deployment
- [ ] Dev + Prod Spring profiles

## Assumptions

- Backend profile REST APIs will be implemented alongside the frontend — each profile section has its own backend controller, service, and DAO layer with PostgreSQL persistence.
- All users accessing Profile features are authenticated (routes require `requiresAuth` guard, already implemented).
- The six profile sections cover all profile needs for the MVP — no additional sections will be added.
- Profile picture upload is explicitly excluded from this feature scope.
- Mobile layout uses 2-row × 3-column grid tabs as sidebar replacement — no hamburger menu or dropdown.
- All date pickers use full date selection (day/month/year).
- The existing PrimeVue Aura theme + existing design tokens are sufficient — no new design system needed.
- Russian language tone is informal "ты" (not formal "Вы").
- No right-side helper column is needed in the profile layout.
- The frontend already has Vue 3, Vite, TypeScript, PrimeVue, PrimeIcons, vue-router, vue-i18n, zod, and related dependencies.
