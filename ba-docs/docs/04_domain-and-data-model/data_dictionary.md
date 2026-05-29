# Data Dictionary

**Project ID:** `resumainer`
**Product Name:** ResumAIner
**Date Created:** 2026-05-17
**Last Updated:** 2026-05-23
**Author:** Anton
**Version:** 2.0
**Status:** Approved
**Source:** dbml_erd.md
**Total Entities:** 30

---

## Entity: role

**Description:** User role lookup. Defines system access level for each user account.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `code` | Varchar(20) | Yes | UNIQUE, NOT NULL | Role code: USER, ADMIN |
| `name` | Varchar(50) | Yes | NOT NULL | Display name: User, Admin |

**Business rules:**
- Seeded at deployment, not user-managed
- USER and ADMIN are the only valid values


## Entity: user_status

**Description:** Account status lookup. Controls whether a user can authenticate and use the system.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `code` | Varchar(20) | Yes | UNIQUE, NOT NULL | Status code: ACTIVE, BLOCKED |
| `name` | Varchar(50) | Yes | NOT NULL | Display name: Active, Blocked |

**Business rules:**
- BLOCKED status prevents login and generation
- User deletion uses soft-delete (is_deleted flag), not status change


## Entity: user_permission

**Description:** Generation permission lookup. Controls whether a user is allowed to generate resumes via AI.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `code` | Varchar(20) | Yes | UNIQUE, NOT NULL | Permission code: ALLOWED, FORBIDDEN |
| `name` | Varchar(50) | Yes | NOT NULL | Display name: Allowed, Forbidden |

**Business rules:**
- Admin sets permission per user in User Details page
- FORBIDDEN users can still log in and manage profile but cannot generate


## Entity: response_status

**Description:** Generation response status lookup. Tracks the lifecycle of an AI generation response from initial output to user approval.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `code` | Varchar(20) | Yes | UNIQUE, NOT NULL | Status code: DRAFT, FINALIZED |
| `name` | Varchar(50) | Yes | NOT NULL | Display name: Draft, Finalized |

**Business rules:**
- DRAFT = AI generated, user has not completed review
- FINALIZED = user reviewed and approved; triggers PDF generation
- Only FINALIZED responses can create a `saved_resume` record


## Entity: language

**Description:** Supported interface and resume languages lookup.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `code` | Varchar(10) | Yes | UNIQUE, NOT NULL | Language code: EN, RU |
| `name` | Varchar(50) | Yes | NOT NULL | Display name: English, Russian |

**Business rules:**
- Used for both interface language (DEC-023) and resume language (DEC-029)
- Dropdown selection ensures valid language identifiers for AI model


## Entity: adaptation_level

**Description:** Resume adaptation intensity lookup. Controls how aggressively the AI adapts the resume content to the target vacancy.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `code` | Varchar(20) | Yes | UNIQUE, NOT NULL | Adaptation code: MINIMAL, BALANCED, MAXIMUM |
| `name` | Varchar(50) | Yes | NOT NULL | Display name: Minimal, Balanced, Maximum |
| `description` | Text | No |  | Explanation of adaptation behavior at this level |

**Business rules:**
- Used in both generation requests and saved resumes for traceability
- Adaptation level affects AI prompt instructions


## Entity: work_format

**Description:** Preferred work format lookup. Normalized values for multi-select work format preference (DEC-022).

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `code` | Varchar(30) | Yes | UNIQUE, NOT NULL | Format code: full-time, part-time, rotational_schedule, internship, offline, remote, hybrid, on_project_site |
| `name` | Varchar(50) | Yes | NOT NULL | Display name: Full-time, Part-time, Remote, etc. |

**Business rules:**
- Values are predefined and seeded
- Used through user_work_format junction table (M:N)
- Frontend renders as checkbox group


## Entity: resume_template

**Description:** Resume HTML template catalog (Post-MVP ready). Each record represents a template layout for resume PDF generation.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `name` | Varchar(100) | Yes | NOT NULL | Template display name: Default Two-Page Template |
| `html_file_path` | Varchar(500) | Yes | NOT NULL | File system path to template HTML file |
| `description` | Text | No |  | Template description and use cases |
| `is_active` | Boolean | Yes | NOT NULL, DEFAULT true | Whether template is available for use |
| `created_at` | Timestamp | Yes | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | Timestamp | No |  | Last update timestamp |

**Business rules:**
- Post-MVP feature: users choose template (ATS-friendly, Human-friendly)
- MVP: single default template
- `saved_resume.template_id FK` is nullable for MVP backward compatibility


## Entity: users

**Description:** Registered user accounts. Stores authentication data and access control references. Profile data is stored in separate profile tables (DEC-021).

| Attribute               | Data type    | Required | Constraints                       | Description                                             |
| ----------------------- | ------------ | -------- | --------------------------------- | ------------------------------------------------------- |
| `id`                    | Integer      | Yes      | PK, AUTO_INCREMENT                | Unique identifier                                       |
| `username`              | Varchar(100) | Yes      | UNIQUE, NOT NULL                  | URL-friendly username for public resume links           |
| `email`                 | Varchar(255) | Yes      | UNIQUE, NOT NULL                  | Registration email (not exposed on resumes)             |
| `password_hash`         | Varchar(255) | Yes      | NOT NULL                          | BCrypt password hash                                    |
| `role_id`               | Integer      | Yes      | FK → role.id, NOT NULL            | User role reference                                     |
| `status_id`             | Integer      | Yes      | FK → user_status.id, NOT NULL     | Account status reference                                |
| `permission_id`         | Integer      | Yes      | FK → user_permission.id, NOT NULL | Generation permission reference                         |
| `default_language_id`   | Integer      | No       | FK → language.id                  | Default interface language                              |
| `secondary_language_id` | Integer      | No       | FK → language.id                  | Secondary interface language                            |
| `is_privileged`         | Boolean      | Yes      | NOT NULL, DEFAULT false           | Privileged user — can access hidden AI models (DEC-024) |
| `created_at`            | Timestamp    | Yes      | NOT NULL, DEFAULT now()           | Account creation timestamp                              |
| `updated_at`            | Timestamp    | No       |                                   | Last profile update timestamp                           |
| `deleted_at`            | Timestamp    | No       |                                   | Soft-delete timestamp                                   |
| `is_deleted`            | Boolean      | Yes      | NOT NULL, DEFAULT false           | Soft-delete flag                                        |

**Business rules:**
- Table name is plural (users) to avoid PostgreSQL reserved word conflict
- Email is used for authentication; `resume_email` in `contact_detail` is for resume display
- Username is part of public resume URL: `/{username}/{public_code}`
- Deactivation uses `status_id` = BLOCKED, not record deletion
- `is_privileged` grants access to hidden/paid AI models (DEC-024)


## Entity: contact_detail

**Description:** User profile contact information. One-to-one with users. Contains all resume-relevant contact data (DEC-021).

| Attribute            | Data type    | Required | Constraints                     | Description                                                    |
| -------------------- | ------------ | -------- | ------------------------------- | -------------------------------------------------------------- |
| `id`                 | Integer      | Yes      | PK, AUTO_INCREMENT              | Unique identifier                                              |
| `user_id`            | Integer      | Yes      | FK → users.id, UNIQUE, NOT NULL | User reference (1:1)                                           |
| `full_name`          | Varchar(255) | Yes      | NOT NULL                        | User's full name for resume display                            |
| `phone`              | Varchar(50)  | Yes      | NOT NULL                        | Contact phone number                                           |
| `resume_email`       | Varchar(255) | Yes      | NOT NULL                        | Email shown on resumes (may differ from account email)         |
| `location`           | Varchar(255) | Yes      | NOT NULL                        | City and country for resume                                    |
| `professional_title` | Varchar(255) | No       |                                 | Professional headline: Business Analyst, Junior Java Developer |
| `linkedin_url`       | Varchar(150) | No       |                                 | LinkedIn profile URL (DEC-026: max 150 chars for vanity URL)   |
| `portfolio_url`      | Varchar(500) | No       |                                 | Portfolio or personal website URL                              |
| `telegram`           | Varchar(100) | No       |                                 | Telegram username                                              |
| `whatsapp`           | Varchar(50)  | No       |                                 | WhatsApp phone number                                          |
| `created_at`         | Timestamp    | Yes      | NOT NULL, DEFAULT now()         | Record creation timestamp                                      |
| `updated_at`         | Timestamp    | No       |                                 | Last update timestamp                                          |

**Business rules:**
- One user has exactly one `contact_detail` record (created on registration)
- `full_name` is required for resume generation
- linkedin_url max 150 chars per LinkedIn vanity URL limit (DEC-026)


## Entity: work_experience

**Description:** User work history records. Each entry represents one job position held by the user.

| Attribute      | Data type    | Required | Constraints             | Description                                      |
| -------------- | ------------ | -------- | ----------------------- | ------------------------------------------------ |
| `id`           | Integer      | Yes      | PK, AUTO_INCREMENT      | Unique identifier                                |
| `user_id`      | Integer      | Yes      | FK → users.id, NOT NULL | User reference                                   |
| `job_title`    | Varchar(255) | Yes      | NOT NULL                | Position/job title                               |
| `company_name` | Varchar(255) | Yes      | NOT NULL                | Employer name                                    |
| `description`  | Text         | Yes      | NOT NULL                | Role description: responsibilities, achievements |
| `location`     | Varchar(255) | Yes      | NOT NULL                | Work location: city, remote                      |
| `start_date`   | Date         | Yes      | NOT NULL                | Employment start date                            |
| `end_date`     | Date         | No       |                         | Employment end date; NULL = current job          |
| `is_current`   | Boolean      | Yes      | NOT NULL, DEFAULT false | Flag: currently employed here                    |
| `company_url`  | Varchar(500) | No       |                         | Post-MVP: company profile URL (DEC-027)          |
| `created_at`   | Timestamp    | Yes      | NOT NULL, DEFAULT now() | Record creation timestamp                        |
| `updated_at`   | Timestamp    | No       |                         | Last update timestamp                            |

**Business rules:**
- Auto-sorted by start_date DESC, end_date DESC NULLS FIRST (DEC-012)
- description is confirmed required for useful resume generation
- `is_current` = true if `end_date` is NULL
- `company_url` is Post-MVP: not used in MVP generation flow


## Entity: education

**Description:** Formal education records: universities, colleges, degrees, programs.

| Attribute          | Data type    | Required | Constraints             | Description                                                 |
| ------------------ | ------------ | -------- | ----------------------- | ----------------------------------------------------------- |
| `id`               | Integer      | Yes      | PK, AUTO_INCREMENT      | Unique identifier                                           |
| `user_id`          | Integer      | Yes      | FK → users.id, NOT NULL | User reference                                              |
| `institution_name` | Varchar(255) | Yes      | NOT NULL                | School, university, or institution name                     |
| `degree`           | Varchar(100) | Yes      | NOT NULL                | Degree or qualification: Bachelor, Master, PhD, etc.        |
| `field_of_study`   | Varchar(255) | Yes      | NOT NULL                | Major or specialization: Information Systems                |
| `education_type`   | Varchar(150) | No       |                         | Optional: University, College, etc.                         |
| `description`      | Text         | No       |                         | Additional education details                                |
| `start_date`       | Date         | Yes      | NOT NULL                | Study start date                                            |
| `end_date`         | Date         | No       |                         | Graduation date; NULL = still studying; allows future dates |
| `location`         | Varchar(255) | No       |                         | Institution location                                        |
| `gpa_grade`        | Varchar(20)  | No       |                         | GPA or grade (text for flexible format)                     |
| `created_at`       | Timestamp    | Yes      | NOT NULL, DEFAULT now() | Record creation timestamp                                   |
| `updated_at`       | Timestamp    | No       |                         | Last update timestamp                                       |

**Business rules:**
- `start_date` (year) is required per confirmed elicitation decision
- `end_date` can be NULL (still studying) or in the future (planned graduation)
- degree field is free text to allow 'Other' values


## Entity: project

**Description:** Projects and volunteering records. Captures personal, academic, professional, and volunteer experience.

| Attribute      | Data type    | Required | Constraints             | Description                                                  |
| -------------- | ------------ | -------- | ----------------------- | ------------------------------------------------------------ |
| `id`           | Integer      | Yes      | PK, AUTO_INCREMENT      | Unique identifier                                            |
| `user_id`      | Integer      | Yes      | FK → users.id, NOT NULL | User reference                                               |
| `project_name` | Varchar(255) | Yes      | NOT NULL                | Project or activity name                                     |
| `role`         | Varchar(255) | No       |                         | User's role in project; code default = Participant (DEC-031) |
| `description`  | Text         | Yes      | NOT NULL                | Project description and contributions                        |
| `location`     | Varchar(255) | No       |                         | Project location                                             |
| `start_date`   | Date         | Yes      | NOT NULL                | Project start date                                           |
| `end_date`     | Date         | No       |                         | End date; NULL = ongoing; allows future dates                |
| `project_url`  | Varchar(500) | No       |                         | Project URL or repository link                               |
| `created_at`   | Timestamp    | Yes      | NOT NULL, DEFAULT now() | Record creation timestamp                                    |
| `updated_at`   | Timestamp    | No       |                         | Last update timestamp                                        |

**Business rules:**
- Volunteering handled together with projects under the same entity for MVP simplicity
- If role is NULL, code defaults to Participant / Participant (DEC-031)
- end_date allows future dates for planned project completions


## Entity: course_certificate

**Description:** Courses, certificates, and professional training records. Mandatory section per DEC-018.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `user_id` | Integer | Yes | FK → users.id, NOT NULL | User reference |
| `name` | Varchar(255) | Yes | NOT NULL | Course or certificate name |
| `provider` | Varchar(255) | Yes | NOT NULL | Provider or issuer: Coursera, Udemy |
| `description` | Text | No |  | Course description and details |
| `course_focus` | Varchar(255) | No |  | Optional: key skills/topics covered (user input) |
| `start_date` | Date | Yes | NOT NULL | Course start date |
| `end_date` | Date | No |  | Completion date; NULL = in progress |
| `credential_url` | Varchar(500) | No |  | Link to credential or certificate verification |
| `created_at` | Timestamp | Yes | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | Timestamp | No |  | Last update timestamp |

**Business rules:**
- Mandatory section per DEC-018 — target users are professionals with completed courses
- Two-page template limits: Page 1 max 7 most relevant courses; Page 2 adjusts based on work experience count (DEC-019)
- `course_focus` is user-provided, not AI-generated


## Entity: additional_profile_info

**Description:** Simplified additional profile data (DEC-013). Single table replaces 7 tables for MVP. Stores free-text fields, resume language preferences, and personal information.

| Attribute                       | Data type    | Required | Constraints                     | Description                                                  |
| ------------------------------- | ------------ | -------- | ------------------------------- | ------------------------------------------------------------ |
| `id`                            | Integer      | Yes      | PK, AUTO_INCREMENT              | Unique identifier                                            |
| `user_id`                       | Integer      | Yes      | FK → users.id, UNIQUE, NOT NULL | User reference (1:1)                                         |
| `skills`                        | Text         | No       |                                 | Free-text: skills list (comma-separated or free form)        |
| `languages`                     | Text         | No       |                                 | Free-text: languages with proficiency levels                 |
| `professional_aspirations`      | Text         | No       |                                 | Target career direction and goals                            |
| `achievements`                  | Text         | No       |                                 | Key professional and personal achievements                   |
| `general_information`           | Text         | No       |                                 | AI context: any job-related info to help resume generation   |
| `default_resume_language_id`    | Integer      | No       | FK → language.id                | Default resume generation language (DEC-029)                 |
| `additional_resume_language_id` | Integer      | No       | FK → language.id                | Additional resume generation language (DEC-029)              |
| `ready_for_relocation`          | Varchar(20)  | No       |                                 | Relocation readiness: Yes, No, Not specified                 |
| `ready_for_business_trips`      | Varchar(20)  | No       |                                 | Business trip readiness: Yes, No, Not specified              |
| `date_of_birth`                 | Date         | YES      | NOT NULL                        | Date of birth                                                |
| `citizenship`                   | Varchar(150) | YES      | NOT NULL                        | Optional: user's citizenship                                 |
| `photo_file_path`               | Varchar(500) | No       |                                 | Profile photo file path (optional per confirmed requirement) |
| `created_at`                    | Timestamp    | Yes      | NOT NULL, DEFAULT now()         | Record creation timestamp                                    |
| `updated_at`                    | Timestamp    | No       |                                 | Last update timestamp                                        |

**Business rules:**
- Simplified MVP design (DEC-013): skills, languages, aspirations, achievements are text fields, not separate tables because used as feed for AI model only
- Resume language IDs are FKs to language table (DEC-029) — dropdown selection on frontend
- Relocation/travel readiness uses controlled dropdown values
- Photo is optional despite wireframe asterisk (confirmed decision)
- `preferred_work_format` moved to separate junction table `user_work_format` (DEC-022)


## Entity: user_work_format

**Description:** Junction table for many-to-many relationship between users and preferred work formats (DEC-022).

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `user_id` | Integer | Yes | FK → users.id, NOT NULL | User reference |
| `work_format_id` | Integer | Yes | FK → work_format.id, NOT NULL | Work format reference |

**Business rules:**
- Composite unique constraint on (`user_id`, `work_format_id`)
- One user can have multiple work formats (checkbox group on frontend)
- Follows 3NF — enables querying by format


## Entity: ai_model

**Description:** AI provider model configurations. Stores provider connection details, API keys, and visibility settings.

| Attribute           | Data type    | Required | Constraints             | Description                                   |
| ------------------- | ------------ | -------- | ----------------------- | --------------------------------------------- |
| `id`                | Integer      | Yes      | PK, AUTO_INCREMENT      | Unique identifier                             |
| `provider`          | Varchar(255) | Yes      | NOT NULL                | AI provider name: OpenRouter                  |
| `model_code`        | Varchar(255) | Yes      | NOT NULL                | Provider model code: deepseek/deepseek-v4-pro |
| `display_name`      | Varchar(255) | Yes      | NOT NULL                | Human-readable model display name             |
| `provider_api_url`  | Varchar(500) | Yes      | NOT NULL                | API base URL for provider                     |
| `api_key_encrypted` | Varchar(512) | Yes      | NOT NULL                | Encrypted API key (NFR-001)                   |
| `is_active`         | Boolean      | Yes      | NOT NULL, DEFAULT true  | Whether model is available for generation     |
| `is_paid`           | Boolean      | Yes      | NOT NULL, DEFAULT false | Flag: model requires payment                  |
| `is_hidden`         | Boolean      | Yes      | NOT NULL, DEFAULT false | Hidden from non-privileged users (DEC-024)    |
| `created_at`        | Timestamp    | Yes      | NOT NULL, DEFAULT now() | Record creation timestamp                     |
| `updated_at`        | Timestamp    | No       |                         | Last update timestamp                         |

**Business rules:**
- API key is masked after saving, never logged (NFR-001)
- `is_paid` marks paid models for admin awareness
- `is_hidden` controls visibility to non-privileged users (DEC-024)
- Admin manages Visibility dropdown (Visible/Hidden) in AI Model Details page


## Entity: resume_generation_request

**Description:** User's request to generate an adapted resume. Captures all input parameters for traceability and re-generation.

| Attribute              | Data type   | Required | Constraints                        | Description                                               |
| ---------------------- | ----------- | -------- | ---------------------------------- | --------------------------------------------------------- |
| `id`                   | Integer     | Yes      | PK, AUTO_INCREMENT                 | Unique identifier                                         |
| `user_id`              | Integer     | Yes      | FK → users.id, NOT NULL            | User who submitted the request                            |
| `ai_model_id`          | Integer     | Yes      | FK → ai_model.id, NOT NULL         | AI model used for generation                              |
| `vacancy_description`  | Text        | Yes      | NOT NULL                           | Vacancy description pasted by user                        |
| `company_description`  | Text        | No       |                                    | Optional company context                                  |
| `additional_comments`  | Text        | No       |                                    | Additional instructions for AI                            |
| `include_cover_letter` | Boolean     | Yes      | NOT NULL, DEFAULT false            | Whether to generate cover letter                          |
| `language_id`          | Integer     | Yes      | FK → language.id, NOT NULL         | Target resume language                                    |
| `adaptation_level_id`  | Integer     | Yes      | FK → adaptation_level.id, NOT NULL | Adaptation intensity                                      |
| `language_mode`        | Varchar(20) | Yes      | NOT NULL, DEFAULT 'default'        | Language mode: 'default', 'additional', 'both'            |
| `budget_config_id`        | BigInt | No       | FK → resume_budget_configs.id                  | Budget config used for this generation (DB-backed) |
| `budget_config_version_used` | Int    | No       |                                              | Version of config at generation time |
| `status`               | Varchar(30) | Yes      | NOT NULL, DEFAULT 'pending'        | Processing status: pending, processing, completed, failed |
| `error_message`        | Text        | No       |                                    | Error details if generation failed                        |
| `created_at`           | Timestamp   | Yes      | NOT NULL, DEFAULT now()            | Request creation timestamp                                |
| `completed_at`         | Timestamp   | No       |                                    | Generation completion timestamp                           |

**Business rules:**
- Each request produces exactly one response (1:1)
- Status transitions: pending → processing → completed | failed
- Vacancy description is required — it's the core input for AI adaptation
- include_cover_letter flag triggers additional AI output (DEC-016)


## Entity: resume_generation_response

**Description:** AI generation output and user-reviewed edits. Status tracks lifecycle: DRAFT (AI output) → FINALIZED (user approved).

| Attribute                  | Data type    | Required | Constraints                                         | Description                                         |
| -------------------------- | ------------ | -------- | --------------------------------------------------- | --------------------------------------------------- |
| `id`                       | Integer      | Yes      | PK, AUTO_INCREMENT                                  | Unique identifier                                   |
| `generation_request_id`    | Integer      | Yes      | FK → resume_generation_request.id, UNIQUE, NOT NULL | Source request (1:1)                                |
| `status_id`                | Integer      | Yes      | FK → response_status.id, NOT NULL                   | Response status: DRAFT or FINALIZED                 |
| `professional_title`       | Varchar(250) | Yes      | NOT NULL                                            | AI-generated and user-reviewed professional title   |
| `professional_summary`     | Text         | Yes      | NOT NULL                                            | AI-generated and user-reviewed professional summary |
| `professional_aspirations` | Text         | Yes      | NOT NULL                                            | AI-generated and user-reviewed career aspirations   |
| `cover_letter`             | Text         | No       |                                                     | Generated and user-edited cover letter (DEC-016)    |
| `created_at`               | Timestamp    | Yes      | NOT NULL, DEFAULT now()                             | Response creation timestamp                         |
| `updated_at`               | Timestamp    | No       |                                                     | Last update timestamp                               |

**Business rules:**
- Only FINALIZED responses can create a saved_resume record
- cover_letter stored here (not in saved_resume) because it's AI-generated output (DEC-028)
- Multi-value sections (experience, education, etc.) stored in generation_response_* tables


## Entity: generation_response_experience

**Description:** Reviewed and edited work experience items from a generation response. Each row represents one work entry in the final resume.

| Attribute         | Data type    | Required | Constraints                                  | Description                                                 |
| ----------------- | ------------ | -------- | -------------------------------------------- | ----------------------------------------------------------- |
| `id`              | Integer      | Yes      | PK, AUTO_INCREMENT                           | Unique identifier                                           |
| `response_id`     | Integer      | Yes      | FK → resume_generation_response.id, NOT NULL | Parent generation response                                  |
| `job_title`       | Varchar(255) | Yes      | NOT NULL                                     | Job title in generated resume                               |
| `company_name`    | Varchar(255) | Yes      | NOT NULL                                     | Company name in generated resume                            |
| `description`     | Text         | Yes      | NOT NULL                                     | AI-generated and user-reviewed description                  |
| `location`        | Varchar(255) | Yes      | NOT NULL                                     | Work location in generated resume                           |
| `is_first_page`   | Boolean      | Yes      | NOT NULL, DEFAULT true                       | Page 1 (primary) or Page 2 (additional) placement (DEC-030) |
| `start_date`      | Date         | Yes      | NOT NULL                                     | Start date in generated resume                              |
| `end_date`        | Date         | No       |                                              | End date; NULL = current                                    |
| `order_in_resume` | Integer      | Yes      | NOT NULL, DEFAULT 0                          | Fixed display order (DEC-030)                               |
| `created_at`      | Timestamp    | Yes      | NOT NULL, DEFAULT now()                      | Record creation timestamp                                   |
| `updated_at`      | Timestamp    | No       |                                              | Last update timestamp                                       |

**Business rules:**
- start_date is required — work experience without start date has no resume value
- is_first_page=true → primary experience on page 1; false → Additional work experience on page 2
- order_in_resume is fixed (not user-reorderable) as per DEC-030


## Entity: generation_response_education

**Description:** Reviewed education items from a generation response. Compact format for resume template.

| Attribute          | Data type    | Required | Constraints                                  | Description                          |
| ------------------ | ------------ | -------- | -------------------------------------------- | ------------------------------------ |
| `id`               | Integer      | Yes      | PK, AUTO_INCREMENT                           | Unique identifier                    |
| `response_id`      | Integer      | Yes      | FK → resume_generation_response.id, NOT NULL | Parent generation response           |
| `institution_name` | Varchar(255) | Yes      | NOT NULL                                     | Institution name in generated resume |
| `degree`           | Varchar(100) | Yes      | NOT NULL                                     | Degree in generated resume           |
| `field_of_study`   | Varchar(255) | Yes      | NOT NULL                                     | Field of study in generated resume   |
| `start_date`       | Date         | Yes      | NOT NULL                                     | Start date in generated resume       |
| `end_date`         | Date         | No       |                                              | End date in generated resume         |
| `location`         | Varchar(255) | No       |                                              | Institution location                 |
| `gpa_grade`        | Varchar(20)  | No       |                                              | GPA or grade                         |
| `order_in_resume`  | Integer      | Yes      | NOT NULL, DEFAULT 0                          | Fixed display order (DEC-030)        |
| `created_at`       | Timestamp    | Yes      | NOT NULL, DEFAULT now()                      | Record creation timestamp            |
| `updated_at`       | Timestamp    | No       |                                              | Last update timestamp                |

**Business rules:**
- No description field — education uses compact format in resume template (DEC-030)
- `order_in_resume` is fixed (not user-reorderable) as per DEC-030


## Entity: generation_response_course

**Description:** Reviewed course/certificate items from a generation response. Compact format for resume template.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `response_id` | Integer | Yes | FK → resume_generation_response.id, NOT NULL | Parent generation response |
| `name` | Varchar(255) | Yes | NOT NULL | Course name in generated resume |
| `provider` | Varchar(255) | Yes | NOT NULL | Provider name in generated resume |
| `is_first_page` | Boolean | Yes | NOT NULL, DEFAULT true | Page 1 (primary) or Page 2 (additional) placement (DEC-030) |
| `course_focus` | Varchar(255) | No |  | Skills/topics covered |
| `order_in_resume` | Integer | Yes | NOT NULL, DEFAULT 0 | Fixed display order (DEC-030) |
| `created_at` | Timestamp | Yes | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | Timestamp | No |  | Last update timestamp |

**Business rules:**
- No description field — courses use compact format in resume template (DEC-030)
- is_first_page=true → primary courses on page 1; false → additional courses on page 2
- order_in_resume is fixed (not user-reorderable) as per DEC-030


## Entity: generation_response_project

**Description:** Reviewed project/volunteering items from a generation response.

| Attribute         | Data type    | Required | Constraints                                  | Description                             |
| ----------------- | ------------ | -------- | -------------------------------------------- | --------------------------------------- |
| `id`              | Integer      | Yes      | PK, AUTO_INCREMENT                           | Unique identifier                       |
| `response_id`     | Integer      | Yes      | FK → resume_generation_response.id, NOT NULL | Parent generation response              |
| `project_name`    | Varchar(255) | Yes      | NOT NULL                                     | Project name in generated resume        |
| `role`            | Varchar(255) | No       |                                              | User's role in project                  |
| `description`     | Text         | Yes      | NOT NULL                                     | Project description in generated resume |
| `location`        | Varchar(255) | No       |                                              | Project location                        |
| `start_date`      | Date         | Yes      | NOT NULL                                     | Project start date                      |
| `end_date`        | Date         | No       |                                              | Project end date                        |
| `order_in_resume` | Integer      | Yes      | NOT NULL, DEFAULT 0                          | Fixed display order (DEC-030)           |
| `created_at`      | Timestamp    | Yes      | NOT NULL, DEFAULT now()                      | Record creation timestamp               |
| `updated_at`      | Timestamp    | No       |                                              | Last update timestamp                   |

**Business rules:**
- start_date is required — project without start date has no resume value
- order_in_resume is fixed (not user-reorderable) as per DEC-030


## Entity: generation_response_skill

**Description:** Reviewed skill groups from a generation response. Skills are organized into groups with individual skill names.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | Integer | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `response_id` | Integer | Yes | FK → resume_generation_response.id, NOT NULL | Parent generation response |
| `skill_group` | Varchar(255) | Yes | NOT NULL | Skill group name: Leadership, Reporting |
| `skill_name` | Varchar(255) | Yes | NOT NULL | Individual skill: Team Leadership, Process Improvement |
| `order_in_resume` | Integer | Yes | NOT NULL, DEFAULT 0 | Fixed display order (DEC-030) |
| `created_at` | Timestamp | Yes | NOT NULL, DEFAULT now() | Record creation timestamp |
| `updated_at` | Timestamp | No |  | Last update timestamp |

**Business rules:**
- Example structure: group 'Leadership' contains skills 'Team Leadership', 'Process Improvement'
- Skills are grouped for organized display in resume template
- order_in_resume is fixed (not user-reorderable) as per DEC-030


## Entity: saved_resume

**Description:** Finalized saved resume record. Created after user approves (FINALIZEs) the generation response. Stores metadata and PDF file path.

| Attribute               | Data type    | Required | Constraints                                  | Description                          |
| ----------------------- | ------------ | -------- | -------------------------------------------- | ------------------------------------ |
| `id`                    | Integer      | Yes      | PK, AUTO_INCREMENT                           | Unique identifier                    |
| `user_id`               | Integer      | Yes      | FK → users.id, NOT NULL                      | Resume owner                         |
| `generation_request_id` | Integer      | Yes      | FK → resume_generation_request.id, NOT NULL  | Source generation request            |
| `response_id`           | Integer      | Yes      | FK → resume_generation_response.id, NOT NULL | Finalized response                   |
| `template_id`           | Integer      | No       | FK → resume_template.id                      | Post-MVP: template used for PDF      |
| `adaptation_level_id`   | Integer      | Yes      | FK → adaptation_level.id, NOT NULL           | Adaptation level used                |
| `language_id`           | Integer      | Yes      | FK → language.id, NOT NULL                   | Resume language                      |
| `title`                 | Varchar(255) | Yes      | NOT NULL                                     | Resume title for user identification |
| `public_code`           | Varchar(4)   | Yes      | NOT NULL                                     | 4-char public code for sharing URL   |
| `public_url_link`       | Varchar(200) | Yes      | NOT NULL                                     | Full ready-made public resume URL    |
| `pdf_file_path`         | Varchar(500) | Yes      | NOT NULL                                     | Server path to generated PDF file    |
| `is_deleted`            | Boolean      | Yes      | NOT NULL, DEFAULT false                      | Soft-delete flag                     |
| `deleted_at`            | Timestamp    | No       |                                              | Soft-delete timestamp                |
| `created_at`            | Timestamp    | Yes      | NOT NULL, DEFAULT now()                      | Save timestamp                       |
| `updated_at`            | Timestamp    | No       |                                              | Last update timestamp                |

**Business rules:**
- Generated only from FINALIZED responses
- public_code: 4 chars from QWRYUPASEDFGHJKZXCVBNM, no repeated letters, unique per user (DEC-019)
- Public URL pattern: /{username}/{public_code}
- Composite unique index on (user_id, public_code)
- All resumes are public by design — is_public flag not needed (DEC-028)
- pdf_file_path replaces separate pdf_file table (DEC-017)
- Top-level text fields (professional_summary, professional_aspirations, cover_letter) stored in resume_generation_response


## Entity: ai_usage_log

**Description:** AI token usage log. One row per API call. Powers statistics on User Home and Admin Home dashboards.

| Attribute                | Data type | Required | Constraints                                  | Description                 |
| ------------------------ | --------- | -------- | -------------------------------------------- | --------------------------- |
| `id`                     | Integer   | Yes      | PK, AUTO_INCREMENT                           | Unique identifier           |
| `user_id`                | Integer   | Yes      | FK → users.id, NOT NULL                      | User who made the request   |
| `ai_model_id`            | Integer   | Yes      | FK → ai_model.id, NOT NULL                   | AI model used               |
| `generation_request_id`  | Integer   | Yes      | FK → resume_generation_request.id, NOT NULL  | Related generation request  |
| `generation_response_id` | Integer   | No       | FK → resume_generation_response.id, NOT NULL | Related generation response |
| `tokens_sent`            | Integer   | Yes      | NOT NULL, DEFAULT 0                          | Prompt/input tokens         |
| `tokens_generated`       | Integer   | Yes      | NOT NULL, DEFAULT 0                          | Completion/output tokens    |
| `cost`                   | Decimal   | No       |                                              | Post-MVP: request cost      |
| `created_at`             | Timestamp | Yes      | NOT NULL, DEFAULT now()                      | Log entry timestamp         |

**Business rules:**
- One row per API call for granular tracking
- token counters power dashboard stats: Total tokens sent/generated
- cost field reserved for Post-MVP billing/analytics

---

## Entity: resume_budget_configs

**Description:** DB-backed budget configuration for resume template budgets. Stores config identity and version metadata. Replaces YAML-based external configuration.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | BigInt | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `name` | Varchar(100) | Yes | NOT NULL | Config display name |
| `version_no` | Int | Yes | NOT NULL, DEFAULT 1 | Incremented on settings change |
| `is_active` | Boolean | Yes | NOT NULL, DEFAULT false | Active flag; only one active config allowed |
| `description` | Text | No |  | Config description |
| `created_at` | Timestamp | Yes | NOT NULL | Record creation timestamp |
| `updated_at` | Timestamp | Yes | NOT NULL | Last update timestamp |

**Business rules:**
- Only one active config should exist — PostgreSQL partial unique index enforces this
- `version_no` is incremented when config settings are changed
- If multiple configs have `is_active = true`, backend selects newest by `updated_at DESC, id DESC`
- If no active config exists, backend falls back to newest config by `updated_at DESC, id DESC`
- If no config exists at all, backend must throw a clear configuration error


## Entity: resume_template_selection_rules

**Description:** General scalar configuration values for resume budget (key-value pattern). Uses typed columns (`int_value`, `boolean_value`, `text_value`) controlled by `value_type`.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | BigInt | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `config_id` | BigInt | Yes | FK → resume_budget_configs.id, NOT NULL | Parent config |
| `rule_key` | Varchar(100) | Yes | NOT NULL | Rule identifier |
| `value_type` | Varchar(20) | Yes | NOT NULL | Value type: int, boolean, text |
| `int_value` | Int | No |  | Integer value (used when value_type = 'int') |
| `boolean_value` | Boolean | No |  | Boolean value (used when value_type = 'boolean') |
| `text_value` | Varchar(255) | No |  | Text value (used when value_type = 'text') |
| `description` | Text | No |  | Rule description |
| `created_at` | Timestamp | Yes | NOT NULL | Record creation timestamp |
| `updated_at` | Timestamp | Yes | NOT NULL | Last update timestamp |

**Business rules:**
- Unique index on (`config_id`, `rule_key`)
- Only one value column should be non-null per row, based on `value_type`
- PostgreSQL check constraint recommended to enforce value type consistency


## Entity: resume_work_experience_distribution_rules

**Description:** Work experience distribution rules per edge case. Each rule maps a job/project/course profile to a template mode and page distribution.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | BigInt | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `config_id` | BigInt | Yes | FK → resume_budget_configs.id, NOT NULL | Parent config |
| `case_key` | Varchar(20) | Yes | NOT NULL | Edge case key: EC-001, EC-003, EC-010, etc. |
| `min_total_jobs` | Int | Yes | NOT NULL | Minimum total jobs for this rule |
| `max_total_jobs` | Int | Yes | NOT NULL | Maximum total jobs for this rule |
| `min_projects` | Int | Yes | NOT NULL, DEFAULT 0 | Minimum projects for this rule |
| `max_projects` | Int | No |  | Maximum projects; NULL = unlimited |
| `require_no_courses` | Boolean | Yes | NOT NULL, DEFAULT false | Whether no-courses condition is required |
| `template_mode` | Varchar(20) | Yes | NOT NULL | one_page or two_page |
| `page1_jobs` | Int | Yes | NOT NULL | Number of jobs on Page 1 |
| `page2_jobs` | Int | Yes | NOT NULL, DEFAULT 0 | Number of jobs on Page 2 |
| `page2_max_additional_jobs` | Int | No |  | Max additional jobs on Page 2 |
| `priority` | Int | Yes | NOT NULL, DEFAULT 100 | Lower value = higher priority |
| `notes` | Text | No |  | Rule notes |
| `created_at` | Timestamp | Yes | NOT NULL | Record creation timestamp |
| `updated_at` | Timestamp | Yes | NOT NULL | Last update timestamp |

**Business rules:**
- Unique index on (`config_id`, `case_key`)
- Lower `priority` value means higher priority
- Special cases (e.g., EC-017) should be evaluated before generic rules


## Entity: resume_section_budget_rules

**Description:** Section-level budget rules defining min/max values for content metrics. Each row defines a budget for one section/profile/metric combination.

| Attribute | Data type | Required | Constraints | Description |
|---------|-----------|--------------|-------------|----------|
| `id` | BigInt | Yes | PK, AUTO_INCREMENT | Unique identifier |
| `config_id` | BigInt | Yes | FK → resume_budget_configs.id, NOT NULL | Parent config |
| `section_key` | Varchar(100) | Yes | NOT NULL | Section: professional_summary, skills, courses, projects |
| `profile_key` | Varchar(100) | Yes | NOT NULL | Profile: light, medium, dense, one_page_light, etc. |
| `metric_key` | Varchar(100) | Yes | NOT NULL | Metric: sentences, bullet_points, max_courses, words_per_skill |
| `min_value` | Int | No |  | Minimum value for this metric |
| `max_value` | Int | No |  | Maximum value for this metric |
| `notes` | Text | No |  | Rule notes |
| `created_at` | Timestamp | Yes | NOT NULL | Record creation timestamp |
| `updated_at` | Timestamp | Yes | NOT NULL | Last update timestamp |

**Business rules:**
- Unique index on (`config_id`, `section_key`, `profile_key`, `metric_key`)
- Controls section-specific content budgets for each density profile


## Traceability

| Connection | Artifact |
|-------|----------|
| Source (4.3) | governance_plans/reports/docs/04_domain-and-data-model/dbml_erd.md (v2.0) |