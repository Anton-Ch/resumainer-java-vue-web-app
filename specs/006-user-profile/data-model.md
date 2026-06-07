# Data Model: User Profile Page

## Entity-Relationship Summary

All profile entities belong to a single user (FK to `users.id`). Work format is M:N via junction table `user_work_format`. All other profile tables are 1:N (Work Experience, Education, Project, Course Certificate) or 1:1 (Contact Details, Additional Profile Info) with `users`.

```
users (existing)
├── contact_detail (1:1, existing V6)
├── work_experience (1:N)
├── education (1:N)
├── project (1:N, includes volunteering)
├── course_certificate (1:N)
├── additional_profile_info (1:1)
└── user_work_format (M:N) ── work_format (lookup)
```

## Entity: work_experience

| Attribute | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `user_id` | Integer | Yes | FK → users.id, NOT NULL | User reference |
| `job_title` | Varchar(255) | Yes | NOT NULL | Position/job title |
| `company_name` | Varchar(255) | Yes | NOT NULL | Employer name |
| `description` | Text | Yes | NOT NULL | Role description, responsibilities, achievements |
| `location` | Varchar(255) | Yes | NOT NULL | Work location |
| `start_date` | Date | Yes | NOT NULL | Employment start date |
| `end_date` | Date | No | NULL when current | Employment end date |
| `is_current` | Boolean | Yes | NOT NULL, DEFAULT false | Currently employed flag |
| `company_url` | Varchar(500) | No | — | Company profile URL (included in MVP per user decision) |
| `created_at` | Timestamp | Yes | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | Timestamp | No | — | Last update timestamp |
| `is_deleted` | Boolean | Yes | NOT NULL, DEFAULT FALSE | Soft-delete flag (SEC-003) |
| `deleted_at` | Timestamp | No | — | Soft-delete timestamp |

**Business rules:**
- Auto-sorted by start_date DESC, end_date DESC NULLS FIRST (DEC-012)
- `is_current = true` when `end_date` is NULL
- Frontend hides end_date when is_current is checked
- All SELECT queries filter `WHERE is_deleted = FALSE` by default (SEC-003)

## Entity: education

| Attribute | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `user_id` | Integer | Yes | FK → users.id, NOT NULL | User reference |
| `institution_name` | Varchar(255) | Yes | NOT NULL | School, university, or institution |
| `degree` | Varchar(100) | Yes | NOT NULL | Degree or qualification |
| `field_of_study` | Varchar(255) | Yes | NOT NULL | Major or specialization |
| `description` | Text | No | — | Additional education details (maps to "Comment" in spec) |
| `start_date` | Date | Yes | NOT NULL | Study start date |
| `end_date` | Date | No | NULL when studying | Graduation date |
| `is_current` | Boolean | Yes | NOT NULL, DEFAULT false | Currently studying flag |
| `location` | Varchar(255) | No | — | Institution location |
| `gpa_grade` | Varchar(20) | No | — | GPA or grade (text for flexible format) |
| `created_at` | Timestamp | Yes | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | Timestamp | No | — | Last update timestamp |

| `is_deleted` | Boolean | Yes | NOT NULL, DEFAULT FALSE | Soft-delete flag (SEC-003) |
| `deleted_at` | Timestamp | No | — | Soft-delete timestamp |

**Business rules:**
- Auto-sorted by start_date DESC, end_date DESC NULLS FIRST (DEC-012)
- `is_current = true` when `end_date` is NULL
- All SELECT queries filter `WHERE is_deleted = FALSE` by default (SEC-003)

## Entity: project

| Attribute | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `user_id` | Integer | Yes | FK → users.id, NOT NULL | User reference |
| `project_name` | Varchar(255) | Yes | NOT NULL | Project or activity name |
| `role` | Varchar(255) | No | — | User's role; code default = "Participant" (DEC-031) |
| `description` | Text | Yes | NOT NULL | Project description and contributions |
| `location` | Varchar(255) | Yes | NOT NULL | Project location |
| `start_date` | Date | Yes | NOT NULL | Project start date |
| `end_date` | Date | No | NULL when ongoing | End date |
| `is_ongoing` | Boolean | Yes | NOT NULL, DEFAULT false | Ongoing project flag |
| `project_url` | Varchar(500) | No | — | Project URL or repository link |
| `created_at` | Timestamp | Yes | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | Timestamp | No | — | Last update timestamp |
| `is_deleted` | Boolean | Yes | NOT NULL, DEFAULT FALSE | Soft-delete flag (SEC-003) |
| `deleted_at` | Timestamp | No | — | Soft-delete timestamp |

**Business rules:**
- Volunteering handled together with projects under the same entity
- If role is NULL, code defaults to "Participant" (DEC-031)
- Auto-sorted by start_date DESC, end_date DESC NULLS FIRST (DEC-012)
- All SELECT queries filter `WHERE is_deleted = FALSE` by default (SEC-003)

## Entity: course_certificate

| Attribute | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `user_id` | Integer | Yes | FK → users.id, NOT NULL | User reference |
| `name` | Varchar(255) | Yes | NOT NULL | Course or certificate name |
| `provider` | Varchar(255) | Yes | NOT NULL | Provider or issuer |
| `description` | Text | No | — | Course description and details |
| `course_focus` | Varchar(255) | No | — | Key skills/topics covered (maps to "skills" in prototype) |
| `start_date` | Date | Yes | NOT NULL | Course start date |
| `end_date` | Date | No | NULL when in progress | Completion date |
| `credential_url` | Varchar(500) | No | — | Link to credential or certificate |
| `created_at` | Timestamp | Yes | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | Timestamp | No | — | Last update timestamp |
| `is_deleted` | Boolean | Yes | NOT NULL, DEFAULT FALSE | Soft-delete flag (SEC-003) |
| `deleted_at` | Timestamp | No | — | Soft-delete timestamp |

**Business rules:**
- Mandatory section per DEC-018
- Server-side pagination with LIMIT/OFFSET for up to 300 records
- Search by name, provider, course_focus (3+ characters minimum)
- Date range filter by start_date (From/To)
- All SELECT queries filter `WHERE is_deleted = FALSE` by default (SEC-003)

## Entity: additional_profile_info

| Attribute | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `user_id` | Integer | Yes | FK → users.id, UNIQUE, NOT NULL | User reference (1:1) |
| `skills` | Text | No | — | Free-text skills list |
| `languages` | Text | No | — | Free-text languages with proficiency |
| `professional_aspirations` | Text | No | — | Target career direction |
| `achievements` | Text | No | — | Key achievements |
| `general_information` | Text | No | — | AI context for resume generation ("Additional context for AI" in spec) |
| `default_resume_language_id` | Integer | No | FK → language.id | Default resume language |
| `additional_resume_language_id` | Integer | No | FK → language.id | Additional resume language |
| `ready_for_relocation` | Varchar(20) | No | — | Yes / No / Negotiable |
| `ready_for_business_trips` | Varchar(20) | No | — | Yes / No / Negotiable |
| `date_of_birth` | Date | Yes | NOT NULL | Full exact date of birth |
| `citizenship` | Varchar(150) | Yes | NOT NULL | User's citizenship |
| `photo_file_path` | Varchar(500) | No | — | Profile photo (Post-MVP per DEC-050) |
| `created_at` | Timestamp | Yes | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | Timestamp | No | — | Last update timestamp |

**Business rules:**
- One-to-one with users (created when user first saves profile data)
- Resume languages are mutually exclusive (dropdown IDs from language table)
- Username is stored in `users.username` (existing field), not in this table

## Entity: work_format (lookup)

| Attribute | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `code` | Varchar(30) | Yes | UNIQUE, NOT NULL | Format code |
| `name` | Varchar(50) | Yes | NOT NULL | Display name |

**Seed data:**
| code | name (EN) | name (RU) |
|---|---|---|
| `full-time` | Full-time | Полный день |
| `part-time` | Part-time | Частичная занятость |
| `rotational_schedule` | Rotational schedule | Вахтовый метод |
| `internship` | Internship | Стажировка |
| `offline` | Offline | Офис |
| `remote` | Remote | Удалённо |
| `hybrid` | Hybrid | Гибрид |
| `on_project_site` | On-site project based | Работа на объекте |

## Entity: user_work_format (junction)

| Attribute | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `user_id` | Integer | Yes | FK → users.id, NOT NULL | User reference |
| `work_format_id` | Integer | Yes | FK → work_format.id, NOT NULL | Work format reference |

**Business rules:**
- Composite unique constraint on (`user_id`, `work_format_id`)
- One user can have multiple work formats (checkbox group on frontend)

## Frontend TypeScript Types

```typescript
export interface ContactDetails {
  fullName: string
  professionalTitle: string
  email: string
  phone: string
  location: string
  linkedinUrl: string
  portfolioUrl: string
  telegram: string
  whatsapp: string
}

export interface WorkExperience {
  id: string
  jobTitle: string
  companyName: string
  location: string
  startDate: string    // ISO date string
  endDate: string | null
  currentlyWorkHere: boolean
  description: string
  companyUrl: string
}

export interface Education {
  id: string
  institutionName: string
  degree: string
  fieldOfStudy: string
  startDate: string
  endDate: string | null
  currentlyStudying: boolean
  location: string
  description: string   // BA field name (was "comment" in spec)
  gpaGrade: string      // BA field name (was "gpa" in spec)
}

export interface Project {
  id: string
  projectName: string
  role: string
  startDate: string
  endDate: string | null
  isOngoing: boolean
  description: string
  location: string
  projectUrl: string
}

export interface Course {
  id: string
  courseName: string
  provider: string
  startDate: string
  endDate: string | null
  credentialUrl: string
  courseFocus: string    // BA field name (was "skills" in prototype)
  description: string
}

export interface AdditionalInfo {
  username: string
  defaultResumeLanguage: string
  additionalResumeLanguage: string
  acceptableWorkFormats: string[]
  willingnessToRelocate: string
  willingnessForBusinessTravel: string
  skills: string
  spokenLanguages: string
  professionalAspirations: string
  achievements: string
  additionalContextForAI: string
  dateOfBirth: string
  citizenship: string
}

export interface ProfileSectionStatus {
  contact: 'completed' | 'incomplete'
  experience: { count: number; label: string }
  education: { count: number; label: string }
  projects: { count: number; label: string }
  courses: { count: number; label: string }
  additional: 'completed' | 'incomplete'
}

export interface CoursePage {
  content: Course[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
```
