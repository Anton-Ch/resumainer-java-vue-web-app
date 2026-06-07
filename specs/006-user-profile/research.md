# Research: User Profile Page

## Scope & Decisions

### Data Model Authority
**Decision**: BA Data Dictionary (`ba-docs/docs/04_domain-and-data-model/data_dictionary.md`) is the authoritative source for field names, types, and constraints.
**Rationale**: All profile entity tables (work_experience, education, project, course_certificate, additional_profile_info, work_format) are defined in the BA data dictionary with field-level specifications. Using BA fields ensures consistency with existing schema conventions (users, contact_detail).
**Alternatives considered**: Using prototype field names directly — rejected because BA is the approved business analysis artifact and database seed data is already mapped to BA names.

### Field Mappings (BA → Frontend)
- **Education**: BA `description` → frontend `description` (not `comment`). BA `gpa_grade` → frontend `gpaGrade`.
- **Course**: BA `course_focus` → frontend `courseFocus` (not `skills`). This field stores skills/topics covered.
- **Work Experience**: BA `company_url` included in MVP scope per user decision (DEC-027 deferred but included now).

### Field Required Status
All fields marked as NOT NULL in BA data dictionary are required for frontend validation and DB schema. Confirmed discrepancies resolved per BA authority:
- Work Experience: location = required
- Education: field_of_study = required
- Project: location = required, start_date = required

### Willingness Dropdown Values
BA stores `Yes / No / Not specified` but user confirmed `Yes / No / Negotiable` as the correct UX values. DB column stores the display value directly (Varchar(20)). Backend validation accepts all three values.

### Work Format Values
BA data dictionary `work_format.code` values to be used: `full-time`, `part-time`, `rotational_schedule`, `internship`, `offline`, `remote`, `hybrid`, `on_project_site`. These are seeded in the work_format lookup table.

### Sorting Rules (DEC-012)
All repeatable sections (Work Experience, Education, Projects, Courses) sorted by start_date DESC, end_date DESC NULLS FIRST. This is applied at DAO/query level.

### Date Handling
All date fields use full date pickers (day/month/year). Validation rules:
- End Date cannot be before Start Date (where both present)
- If "current/ongoing/studying" checkbox is checked, End Date is hidden and not sent to backend (NULL in DB)
- Date of Birth is a full exact date (NOT NULL per data dictionary, user confirmed required)

### Error Handling
Existing NFR-003/004/005 framework applies: global exception handler (`@ControllerAdvice`), user-friendly JSON error responses, no stack traces exposed. Profile-specific errors return appropriate HTTP status codes (400 validation, 404 not found, 409 conflict for username).

### Pagination (Courses)
Server-side pagination with LIMIT/OFFSET. PrimeVue DataTable lazy mode (D17). Parameters: page, size (10/20/50, default 10), sort field, sort direction, search query (applied after 3+ characters), date range filters (from/to).

## Unresolved Questions
None — all questions resolved via BA artifact analysis and user clarification.

## Dependencies
- Existing user authentication and session management (Feature 003)
- Existing global exception handler (NFR-003)
- Existing @ComponentScan pattern for bean registration (Feature 004)
- Existing PrimeVue + Zod + i18n setup (Feature 003/005)
- Existing Flyway migration pattern (Features 003/005)
- New Flyway migrations V9-V15 for profile entity tables
