# Quickstart: User Profile Page

## Implementation Order

### Phase A: Database Migrations
Create Flyway migrations V9-V15 in order:
1. `V9__create_work_experience_table.sql` — work_experience table
2. `V10__create_education_table.sql` — education table
3. `V11__create_project_table.sql` — project table
4. `V12__create_course_certificate_table.sql` — course_certificate table
5. `V13__create_additional_profile_info_table.sql` — additional_profile_info table
6. `V14__create_work_format_tables.sql` — work_format + user_work_format tables
7. `V15__seed_work_format_data.sql` — seed 8 work format values

### Phase B: Backend Models + DAOs
1. Create model classes: WorkExperience, Education, Project, CourseCertificate, AdditionalProfileInfo, WorkFormat
2. Create DAO classes with PreparedStatement + connection-accepting overloads (D10)
3. Write DAO tests (JUnit 5 + Mockito)
4. Register all DAOs in WebConfig via @Repository + @ComponentScan

### Phase C: Backend Service + Controller
1. Create ProfileService with transaction management
2. Create ProfileController with all REST endpoints
3. Write Service and Controller tests (standalone MockMvc)
4. Register in WebConfig via @Service + @Controller + @ComponentScan

### Phase D: Frontend Infrastructure
1. Add TypeScript types (profile.ts)
2. Create profileService.ts with REST API calls
3. Merge profile namespace into en.json and ru.json (from prototype)
4. Add /profile/* routes to router/index.ts

### Phase E: Frontend Layout + Navigation
1. ProfilePage.vue (route-level view)
2. ProfileShell.vue + ProfileSidebar.vue + ProfileMobileTabs.vue
3. ProfileSectionHeader.vue
4. UnsavedChangesDialog.vue

### Phase F: Frontend Section Components
1. ContactDetailsSection.vue
2. WorkExperienceSection.vue + RecordCard.vue + EmptyRecordsState.vue
3. ProjectsSection.vue
4. EducationSection.vue
5. CoursesSection.vue + CoursesTable.vue + CourseDialog.vue
6. AdditionalInfoSection.vue

### Phase G: Integration + Testing
1. Connect frontend → backend API
2. Manual integration test all 6 sections
3. i18n audit (no hardcoded strings)
4. Build verification: `mvn clean package` + `npm run build`

## Key Files

| Purpose | Path |
|---|---|
| Spec | `specs/006-user-profile/spec.md` |
| Plan | `specs/006-user-profile/plan.md` |
| Data Model | `specs/006-user-profile/data-model.md` |
| API Contracts | `specs/006-user-profile/contracts/api.md` |
| Memory Synthesis | `specs/006-user-profile/memory-synthesis.md` |
| Prototype Reference | `specs/006-user-profile/spec_input_files/prototype/` |

## Key Decisions

- BA Data Dictionary is authoritative for field names and constraints
- All NOT NULL fields in data dictionary are required on frontend
- Education uses BA fields: `description` (not "comment"), `gpa_grade` (not "gpa")
- Course uses BA field: `course_focus` (not "skills")
- Work format values: 8 codes from BA data dictionary
- Willingness dropdown: Yes / No / Negotiable (user decision)
- Company URL included in MVP (user decision)
- Courses use server-side pagination (lazy DataTable) — D17
- All repeatable sections auto-sorted by start_date DESC (DEC-012)
- No autosave — explicit save with short button labels
