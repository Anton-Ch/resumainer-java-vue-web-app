# Resume Template Details and Logic v3

**Project ID:** `resumainer`  
**Product Name:** ResumAIner  
**Date Created:** 2026-05-19  
**Last Updated:** 2026-05-23  
**Author:** Anton  
**Version:** 7.0  
**Status:** Approved  
**Related BABOK Area:** 7.1 Specify Requirements / 7.5 Design Options  

---

## 1. Description

This document defines the business rules, content budget logic, AI response contract, and backend rendering assumptions for ResumAIner resume templates.

The goal is to generate selectable-text, A4-friendly PDF resumes using one-page and two-page HTML templates while keeping the layout predictable and configurable.

This document adds:
- Professional Summary budget rules;
- explicit HTML-in-JSON formatting rules;
- more precise edge-case handling for one-page and two-page templates;
- configurable content budget parameters stored in PostgreSQL (DB-backed configuration);
- clearer limits for skills, courses, projects, work experience, and aspirations;
- backend-owned HTML/PDF rendering assumptions.

## 2. Implementation Assumptions

### 2.1 Suggested Backend Services

The final design may use the following Java/Spring service responsibilities.

| Service                      | Responsibility                                                                                   |
| ---------------------------- | ------------------------------------------------------------------------------------------------ |
| `ResumePageProfileService`   | Calculates raw counts, page scores, density labels, and template mode.                           |
| `ResumeContentBudgetService` | Converts page profile into AI content limits and section budgets.                                |
| `ResumePromptBuilder`        | Builds AI prompt using profile data, vacancy data, template mode, and content budget. Implements the **Builder** pattern (DEC-056): constructs a complex prompt object step by step (vacancy context, profile data, content budget, language, adaptation level) and returns the complete prompt string. |
| `ResumeTemplateRenderer`     | Renders final HTML from structured generation response and selected template.                    |
| `PdfGenerationService`       | Converts HTML to PDF, validates page count, and stores PDF metadata.                             |
| `ResumeBudgetConfigService`  | Reads DB-backed budget configuration from PostgreSQL and provides current budget values to backend services. |

## 2.2 Generation Pipeline

~~~text
1. Validate minimum profile data.
2. Read user profile, vacancy, company, and generation settings.
3. Calculate raw counts: jobs, education, courses, projects, optional fields.
4. Calculate Page 1 Score, Page 2 Score, and density labels.
5. Select template mode: one-page or two-page.
6. Build content budget rules from DB-backed configuration.
7. Build AI prompt with exact section limits.
8. AI returns structured JSON with limited HTML inside text fields.
9. Backend validates JSON structure.
10. Store AI response in generation response entities/tables.
11. Show structured editable review form in Vue.
12. User edits and saves final resume form.
13. Backend fills final HTML from selected template.
14. Backend generates/converts PDF from HTML.
15. Backend validates page count.
16. Backend saves PDF metadata and exposes public PDF link.
~~~

## 2.3 Storage Assumption

AI response is stored as structured data across generation response tables before final rendering. Generated content must be structured and editable before final PDF creation.

### 2.3.1 Pipeline Overview

~~~text
AI returns structured JSON
    ↓
Backend validates JSON structure and sanitizes HTML (see 9.3)
    ↓
Backend maps JSON fields to database tables:
  ├── resume_generation_response   — single-value fields, status, FKs
  ├── generation_response_experience  — work experience items
  ├── generation_response_education   — education items
  ├── generation_response_course      — course/certificate items
  ├── generation_response_project     — project/volunteering items
  └── generation_response_skill      — skill groups and names
    ↓
Vue reads from tables → shows editable review form
    ↓
User edits → submits final version
    ↓
Backend saves → creates saved_resume record + PDF
~~~

### 2.3.2 Entity Mapping (AI JSON → Tables)

**`resume_generation_response`** — single-value fields. One row per generation.

| JSON Field | Table Column | Type | Required | Notes |
|---|---|---|---|---|
| `professionalTitle` | `professional_title` | Varchar(250) | Yes | AI-generated, vacancy-specific |
| `professionalSummary.html` | `professional_summary` | Text | Yes | May contain limited HTML |
| `professionalAspirations.html` | `professional_aspirations` | Text | Yes | Always generated even if user input empty |
| `coverLetter` (via FR-011) | `cover_letter` | Text | No | Optional, generated only if requested |

**`generation_response_experience`** — one row per work experience entry on the resume.

| JSON Field (per item) | Table Column | Type | Required | Notes |
|---|---|---|---|---|
| `sourceId` | — | — | — | Maps to `work_experience.id` for traceability |
| `jobTitle` | `job_title` | Varchar(255) | Yes | |
| `companyName` | `company_name` | Varchar(255) | Yes | May be shortened by AI (see 6. edge cases) |
| `location` | `location` | Varchar(255) | Yes | |
| `dateRange` | — | — | — | Parsed into `start_date`, `end_date` |
| `descriptionHtml` | `description` | Text | Yes | May contain limited HTML; stored as HTML |
| `bulletsHtml` | — | — | — | Concatenated into `description` or stored separately |
| `is_first_page` | Boolean | Yes | Set by backend: true for page 1 jobs, false for page 2 |
| `order_in_resume` | Integer | Yes | Set by backend based on AI output order |

**`generation_response_education`** — one row per education entry on the resume.

| JSON Field | Table Column | Type | Required | Notes |
|---|---|---|---|---|
| `sourceId` | — | — | — | Maps to `education.id` for traceability |
| `institutionName` | `institution_name` | Varchar(255) | Yes | |
| `degree` | `degree` | Varchar(100) | Yes | |
| `fieldOfStudy` | `field_of_study` | Varchar(255) | Yes | |
| `location` | `location` | Varchar(255) | No | |
| `dateRange` | — | — | — | Parsed into `start_date`, `end_date` |
| `descriptionHtml` | — | — | — | Not used in compact template format (DEC-030) |
| `order_in_resume` | Integer | Yes | Set by backend |

**`generation_response_course`** — one row per course/certificate entry on the resume.

| JSON Field | Table Column | Type | Required | Notes |
|---|---|---|---|---|
| `sourceId` | — | — | — | Maps to `course_certificate.id` for traceability |
| `title` | `name` | Varchar(255) | Yes | |
| `provider` | `provider` | Varchar(255) | Yes | |
| `focus` | `course_focus` | Varchar(255) | No | Focus words/keywords per budget rules |
| `dateRange` | — | — | — | Not displayed on resume, used for internal ordering |
| `is_first_page` | Boolean | Yes | Default: true for page 1 courses |
| `order_in_resume` | Integer | Yes | Set by backend |

**`generation_response_project`** — one row per project/volunteering entry.

| JSON Field | Table Column | Type | Required | Notes |
|---|---|---|---|---|
| `sourceId` | — | — | — | Maps to `project.id` for traceability |
| `projectName` | `project_name` | Varchar(255) | Yes | |
| `role` | `role` | Varchar(255) | No | |
| `location` | `location` | Varchar(255) | No | |
| `dateRange` | — | — | — | Parsed into `start_date`, `end_date` |
| `descriptionHtml` | `description` | Text | Yes | May contain limited HTML |
| `bulletsHtml` | — | — | — | Concatenated into `description` or stored separately |
| `order_in_resume` | Integer | Yes | Set by backend |

**`generation_response_skill`** — one row per individual skill within a group.

| JSON Field | Table Column | Type | Required | Notes |
|---|---|---|---|---|
| `groupName` | `skill_group` | Varchar(255) | Yes | Thematic group (e.g. "Core Competencies") |
| `items[]` | `skill_name` | Varchar(255) | Yes | One row per skill; repeated `groupName` for each item in the group |
| `order_in_resume` | Integer | Yes | Set by backend; skills within a group have sequential order |

Example of skill storage: AI returns `{"groupName": "Leadership", "items": ["Team Leadership", "Process Improvement"]}` → two rows in table with same `skill_group` = "Leadership" and different `skill_name`.

### 2.3.3 Backend-Generated Fields

The following fields are set by the backend, not by AI:

| Table | Field | Source |
|---|---|---|
| `resume_generation_response` | `generation_request_id` | FK to `resume_generation_request.id` |
| `resume_generation_response` | `status_id` | Set to DRAFT initially; changed to FINALIZED after user approval |
| All `generation_response_*` tables | `response_id` | FK to `resume_generation_response.id` |
| All `generation_response_*` tables | `is_first_page` | Based on Page Profile distribution rules (see 4.2) |
| All `generation_response_*` tables | `order_in_resume` | Sequential order determined by backend |
| All `generation_response_*` tables | `created_at`, `updated_at` | Timestamps |
| `saved_resume` | All fields | Created after user finalizes; not populated by AI |

### 2.3.4 Profile Data (Not AI-Generated)

The following resume sections are populated from user profile tables directly, not from AI output:

| Resume Section | Source Table | Notes |
|---|---|---|
| Candidate name | `contact_detail.full_name` | Direct copy |
| Contact details | `contact_detail` | Phone, email, links; code-determined display |
| Value line / keywords | AI-generated | Generated by AI; stored in `resume_generation_response` via `professional_summary` context |
| Personal Information | `additional_profile_info` | Location, languages, etc.; code-determined, not AI |

## 3. Minimum Profile Requirements

Resume generation is blocked unless the user profile contains minimum required data.

| Requirement                                | Rule                                                                   | Reason                                                                                                |
| ------------------------------------------ | ---------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| Work Experience                            | At least 1 work experience record is required                          | Resume generation needs professional experience context.                                              |
| Education                                  | At least 1 education record is required                                | Education is a standard resume section and part of template structure.                                |
| Contact Details                            | Full name, email, phone, and location are required                     | Resume must contain candidate identity and contact data.                                              |
| Skills                                     | Optional. Skills text can ge generated by AI based on provided context | Skills are critical for role matching and resume positioning provided by AI model.                    |
| Projects                                   | Optional                                                               | If no projects exist, Projects section is omitted.                                                    |
| Courses                                    | Optional                                                               | If no courses exist, Courses section is omitted and Page 1 may receive more work experience capacity. |
| Aspirations                                | Optional as user input, required as AI output                          | AI generates this section if missing from profile input.                                              |
| LinkedIn / Portfolio / Telegram / WhatsApp | Optional                                                               | Missing optional contact fields are omitted from rendered template.                                   |
| Date of Birth                              | Optional input, included if provided                                   | If user provides it, renderer includes it in Personal Information.                                    |
| Citizenship                                | Optional input, included if provided                                   | If user provides it, renderer includes it in Personal Information.                                    |

## 4. Page Profile Classification

Page Profile is calculated programmatically before the AI generation call.

The purpose is to decide:
- template mode 1 or 2 pages;
- work experience distribution;
- Professional Summary length;
- content verbosity;
- section budget in sentences and bullet points;
- AI prompt limits.

## 4.1 Raw Counts

Backend calculates these values before generation.

| Raw Count | Description |
|---|---|
| `total_jobs` | Number of active work experience records. |
| `total_education` | Number of active education records. |
| `total_courses` | Number of active courses/certificates records. |
| `total_projects` | Number of active projects/volunteering records. |
| `has_linkedin` | Whether LinkedIn URL is provided. |
| `has_portfolio` | Whether portfolio/website URL is provided. |
| `has_telegram` | Whether Telegram is provided. |
| `has_whatsapp` | Whether WhatsApp is provided. |
| `has_date_of_birth` | Whether Date of Birth is provided. |
| `has_citizenship` | Whether Citizenship is provided. |
| `has_courses` | `total_courses > 0`. |
| `has_projects` | `total_projects > 0`. |

## 4.2 Work Experience Distribution Count

The system determines how many jobs are assigned to Page 1 and Page 2 before building the AI prompt.

| Total Jobs | Page 1 Jobs |                      Page 2 Jobs | Notes                                              |
| ---------- | ----------: | -------------------------------: | -------------------------------------------------- |
| 1          |           1 |                                0 | Single-page candidate if no projects.              |
| 2          |           2 |                                0 | Single-page candidate if no projects.              |
| 3          |           3 |                                0 | Single-page candidate if no projects.              |
| 4          |           2 |                                2 | Default two-page mode. Confirmed edge case EC-010. |
| 5          |           3 |                                2 | Default two-page mode. Confirmed edge case EC-012. |
| 6          |           3 |                                3 | Two-page mode.                                     |
| 7+         |           3 | Up to 7, capped by budget limits | Oldest or least relevant jobs may be trimmed.      |

Course-free expansion rule:

| Condition                                                          | Rule                                                                                                        |
| ------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------- |
| `total_courses = 0` AND `total_projects = 0` AND `total_jobs <= 3` | Backend may allow up to 3 jobs on Page 1 if enabled by DB-backed budget configuration and PDF validation passes.        |
| `total_jobs > 5`                                                   | Two-page mode is required. Page 1 normally shows max 3 jobs; Page 2 shows max 7 additional jobs.            |
| `total_projects > 0`                                               | Projects create Page 2 driver. Course-free Page 1 expansion does not override project-driven two-page mode. |

This rule is intentionally configurable through DB-backed budget settings because it directly affects page density and PDF overflow risk.

## 4.3 Page 1 Score

| Element                     |                       Weight | Condition                                                            |
| --------------------------- | ---------------------------: | -------------------------------------------------------------------- |
| Page 1 work experience jobs | +1 per job, configurable max | Based on selected Page 1 jobs.                                       |
| Education records           |                 +0 / +1 / +2 | 1 education = +0; 2 education = +1; 3+ education = +2.               |
| Course records              |                 +0 / +1 / +2 | 0 courses = +0; 1-5 courses = +0; 6-7 courses = +1; 8+ courses = +2. |
| Professional Summary length |                Informational | Used as buffer; not scored directly.                                 |

## 4.4 Page 2 Score

| Element | Weight | Condition |
|---|---:|---|
| Additional work experience jobs | +0 / +1 / +2 / +3 | 0 jobs = +0; 1-2 = +1; 3-4 = +2; 5+ = +3. |
| Projects | +0 / +1 / +2 | 0 projects = +0; 1 project = +1; 2+ projects = +2. |

## 4.5 Density Labels

| Score | Label | Meaning |
|---:|---|---|
| 0 | Empty | No content for the page/section. |
| 1-3 | Light | Low density; content can be more detailed. |
| 4-5 | Medium | Moderate density; content should be balanced. |
| 6+ | Dense | High density; content should be compact. |

For Page 2, score `0` means there is no need for Page 2 unless other business rules force two-page mode.

## 5. Template Mode Decision Rules

## 5.1 Primary Rule

| Condition                                                                                                     | Template Mode                |
| ------------------------------------------------------------------------------------------------------------- | ---------------------------- |
| `total_projects = 0` AND `total_jobs <= 3`                                                                    | One-page candidate           |
| `total_projects = 0` AND `total_courses = 0` AND `total_jobs <= 3` AND `allowCourseFreePage1Expansion = true` | One-page expansion candidate |
| `total_projects > 0`                                                                                          | Two-page mode                |
| `total_jobs >= 4` AND course-free expansion is disabled or fails validation                                   | Two-page mode                |
| Page 2 Score > 0                                                                                              | Two-page mode                |

Template selection should be treated as a budget decision, not as a visual preference selected by the user.

## 5.2 Edge Case Matrix

| Case   | Jobs | Projects |    Additional Jobs | Page 2 Score Driver               | Template Mode                | Notes                                                                                                     |
| ------ | ---: | -------: | -----------------: | --------------------------------- | ---------------------------- | --------------------------------------------------------------------------------------------------------- |
| EC-001 |    1 |        0 |                  0 | None                              | One-page                     | Expanded summary/work experience may fill page.                                                           |
| EC-002 |    2 |        0 |                  0 | None                              | One-page                     | Both jobs on Page 1.                                                                                      |
| EC-003 |    3 |        0 |                  0 | None                              | One-page                     | All jobs on Page 1; compact if courses/education dense.                                                   |
| EC-004 |    1 |        1 |                  0 | Projects                          | Two-page                     | Page 2 contains Projects, Aspirations, Personal Info.                                                     |
| EC-005 |    2 |        1 |                  0 | Projects                          | Two-page                     | Page 1 has jobs; Page 2 has project section.                                                              |
| EC-006 |    3 |        1 |                  0 | Projects                          | Two-page                     | Page 1 can be dense; Page 2 still required for project.                                                   |
| EC-007 |    1 |       2+ |                  0 | Projects                          | Two-page                     | Projects may be detailed because Page 2 is light.                                                         |
| EC-008 |    2 |       2+ |                  0 | Projects                          | Two-page                     | Projects detail depends on Page 2 density.                                                                |
| EC-009 |    3 |       2+ |                  0 | Projects                          | Two-page                     | Projects may be medium/brief if Page 1 is dense.                                                          |
| EC-010 |    4 |        0 |                  2 | Additional WE                     | Two-page                     | Page 1 shows 2 jobs; Page 2 shows 2 jobs.                                                                 |
| EC-011 |    4 |       1+ |                  2 | Additional WE + Projects          | Two-page                     | Page 2 content budget becomes tighter.                                                                    |
| EC-012 |    5 |        0 |                  2 | Additional WE                     | Two-page                     | Page 1 shows 3 jobs; Page 2 shows 2 jobs.                                                                 |
| EC-013 |    5 |       1+ |                  2 | Additional WE + Projects          | Two-page                     | Projects may be limited to most relevant.                                                                 |
| EC-014 |    6 |        0 |                  3 | Additional WE                     | Two-page                     | Page 2 uses compact additional work format.                                                               |
| EC-015 |    6 |       1+ |                  3 | Additional WE + Projects          | Two-page                     | Dense Page 2; projects and additional WE become brief.                                                    |
| EC-016 |   7+ |      Any | 4+ (up to 7 total) | Additional WE + optional Projects | Two-page                     | Least relevant/oldest entries may be trimmed.                                                             |
| EC-017 |  4-5 |        0 |                  0 | None                              | One-page expansion candidate | Allowed only when `total_courses = 0`, `allowCourseFreePage1Expansion = true`, and PDF validation passes. |

## 5.3 End-to-End Budget Examples

The following examples trace a user profile through the entire Page Profile pipeline: raw counts → scores → density → template mode → content budgets.

Each example shows what the AI prompt receives.

---

**Example A: Junior profile (single-page)**

| Parameter | Value | Calculation |
|---|---|---|
| Jobs | 1 | Page 1: 1 job; Page 2: none |
| Education | 1 | Score +0 |
| Courses | 2 | Score +0 |
| Projects | 0 | Score +0 |
| Page 1 Score | 1 | Light |
| Page 2 Score | 0 | Light |
| Template | Single-page | Page 2 Score = 0, Jobs ≤ 3, Projects = 0 |

**Resulting AI budget:**
- Professional Summary: 5 sentences
- Work Experience (1 job): 5 description sentences + 7 bullet points
- Skills: 4 groups, 5-7 skills each, 1-3 words per skill
- Courses: up to 10, 3-5 focus words per course
- Aspirations: 5 sentences
- Personal Information: compact block at bottom

---

**Example B: Mid-level profile (two-page, balanced)**

| Parameter | Value | Calculation |
|---|---|---|
| Jobs | 4 | Page 1: 2 most relevant; Page 2: 2 oldest |
| Education | 2 | Score +1 |
| Courses | 6 | Score +1 |
| Projects | 1 | Score +1 (Page 2) |
| Page 1 Score | 4 | Medium |
| Page 2 Score | 2 | Medium |
| Template | Two-page | Page 2 Score > 0 |

**Resulting AI budget:**
- Professional Summary: 3-4 sentences
- Page 1 WE (2 jobs): 2-4 description sentences + 3-5 bullet points each
- Page 2 WE (2 jobs): 1-2 description sentences + 1-2 bullet points each
- Skills: 3-4 groups, 4-6 skills each
- Courses: up to 7, 1-3 focus words per course
- Projects (1): 2-3 sentences + 2-3 bullet points
- Aspirations: 3-5 sentences

---

**Example C: Senior profile (two-page, dense)**

| Parameter | Value | Calculation |
|---|---|---|
| Jobs | 6 | Page 1: 3 most relevant; Page 2: 3 oldest |
| Education | 3 | Score +2 |
| Courses | 10 | Score +2 |
| Projects | 2 | Score +2 (Page 2) |
| Page 1 Score | 7 | Dense |
| Page 2 Score | 4 | Dense |
| Template | Two-page | Page 2 Score > 0 |

**Resulting AI budget:**
- Professional Summary: 2-3 sentences
- Page 1 WE (3 jobs): 1-2 description sentences + 2-3 bullet points each
- Page 2 WE (3 jobs): 1 description sentence + 1 bullet point each
- Skills: 2-3 groups, 3-5 skills each
- Courses: top 5 most relevant, 2 focus words per course
- Projects (2 most relevant): 2 short sentences + 1-2 bullet points each
- Aspirations: 1-3 sentences

---

These values are budget targets for the AI prompt, not final visual guarantees. Final PDF validation still checks actual page count after rendering. | Skills | Courses | Projects | Aspirations | Additional WE | WE Total | Primary WE | Additional WE | Projects Total | Courses Total | Template Pages |
|---|---|---|---|---|---|---|---|---:|---:|---:|---:|---:|---:|
| EC-001 | 5 sentences | 5 sentences + 3 bullets | 5 groups, 5-7 skills/group, 1-3 words/skill | 0-5 focus words/course | 0 | 5 sentences | 0 | 1 | 1 | 0 | 0 | 0 | 1 |
| EC-002 | 5 sentences | 5 sentences + 3 bullets | 5 groups, 5-7 skills/group, 1-3 words/skill | 0-5 focus words/course | 0 | 5 sentences | 0 | 2 | 2 | 0 | 0 | 0 | 1 |
| EC-003 | 5 sentences | 3 sentences + 2 bullets | 3 groups, 5-7 skills/group, 1-3 words/skill | 0-5 focus words/course | 0 | 5 sentences | 0 | 3 | 3 | 0 | 0 | 0 | 1 |

These values are budget targets for the AI prompt, not final visual guarantees. Final PDF validation still checks actual page count after rendering.

## 5.4 Final Page Count Validation

Template selection is done before AI generation, but final page count is validated after PDF generation.

| Template | Required PDF Page Count | If Validation Fails |
|---|---:|---|
| One-page | 1 | Retry with compact budget or switch to two-page. |
| Two-page | 2 | Retry with compact budget and trim least relevant content. |

The prompt should try to fit content by budget rules first. Technical validation confirms the result after rendering.

## 6. Section Order

## 6.1 One-Page Template Order

1. Header and contact details
2. Value line / keyword positioning
3. Professional Summary
4. Work Experience
5. Skills
6. Education
7. Courses and Certifications
8. Professional Aspirations
9. Personal Information (inline not a separate line items)

## 6.2 Two-Page Template Order

Page 1:
1. Header and contact details
2. Value line / keyword positioning
3. Professional Summary
4. Primary Work Experience
5. Skills
6. Education
7. Courses and Certifications
8. “See the next page” note

Page 2:
1. “See the previous page” note
2. Candidate name / title continued header
3. Projects and Volunteering
4. Professional Aspirations
5. Additional Work Experience
6. Personal Information

## 7. Content Distribution Rules

## 7.1 Professional Summary

Professional Summary is a flexible buffer section. It can be expanded when the resume has little work/project/course content and compressed when the page is dense (but not less than 2 sentences).

| Page Profile | Target Length | Rule |
|---|---|---|
| One-page, 1 job, no projects | 5 sentences | Use as value-positioning buffer. |
| One-page, 2 jobs, no projects | 5 sentences | Keep strong but concise. |
| One-page, 3 jobs, no projects | 5 sentences | Use if page allows; compact Work Experience first if needed. |
| One-page dense profile | 2-3 sentences | Compress to protect Work Experience and Skills. |
| Two-page, light Page 1 | 4-5 sentences | Can explain positioning and transition. |
| Two-page, medium Page 1 | 3-4 sentences | Balanced summary. |
| Two-page, dense Page 1 | 2-3 sentences | Brief positioning only. |

Professional Summary may contain limited HTML tags such as `<p>`, `<strong>`, `<em>`, and `<br>`.

## 7.2 Work Experience Selection

| Total Jobs | Default Page 1 | Default Page 2 |
|---|---|---|
| 1-3 | All jobs on Page 1 | None |
| 4 | 2 most relevant jobs | 2 remaining jobs |
| 5 | 3 most relevant jobs | 2 remaining jobs |
| 6 | 3 most relevant jobs | 3 remaining jobs |
| 7+ | 3 most relevant jobs | Up to 7 additional jobs, capped by budget |

Selection priority for Page 1:

1. current job if relevant;
2. most relevant to vacancy;
3. second-most relevant;
4. newest/most recent if relevance tie exists.

Page 2 additional work experience should normally be shown in reverse chronological order among remaining jobs.

A job can appear on only one page.

## 7.3 Work Experience Verbosity

Page 1:

| Density / Case | Description | Bullet Points |
|---|---|---|
| Light, 1 job | 5 sentences | 3-9 bullets depending on available space |
| Light, 2 jobs | 5 sentences | 3-7 bullets |
| Light, 3 jobs | 3 sentences | 2-5 bullets |
| Medium | 2-4 sentences | 3-5 bullets |
| Dense | 1-2 sentences | 2-3 bullets |

Page 2 Additional Work Experience:

| Rule                          | Value                                                                          |
| ----------------------------- | ------------------------------------------------------------------------------ |
| Summary length                | 1 short and strong sentence per job (if 2nd page high density and 7 jobs)      |
| Bullet points                 | 0 (only for 7 and high density, for smaller density 1-3 points)                |
| Max additional jobs on Page 2 | 7                                                                              |
| Ordering                      | Reverse chronological among remaining jobs unless relevance requires otherwise |
| Overflow handling             | Trim oldest or least vacancy-relevant jobs first                               |

## 7.4 Skills

Skills are grouped thematically. Skill items should be concise because they are scanned quickly by recruiters and may affect ATS parsing.

| Page 1 Density | Skill Groups | Skills per Group | Skill Length |
|---|---:|---:|---|
| Light | 4-5 | 5-7 | 1-3 words per skill |
| Medium | 3-4 | 4-6 | 1-3 words per skill |
| Dense | 2-3 | 3-5 | 1-3 words per skill |
| Overflow | 2 | 3-4 | 1-2 words per skill |

Examples:

~~~text
Business Analysis: Requirements Elicitation, Stakeholder Interviews, BPMN, User Stories, Acceptance Criteria
~~~

**Note:** Education records affect Page 1 Score (see 4.3), which determines the Density label used in this table. Skills budgets are driven by the resulting Density, not by Education count directly.

## 7.5 Courses and Certifications

Course section is optional. If the user has no courses, the Courses section is omitted and Page 1 may receive more work experience capacity.

| Page 1 Density |      Max Courses | Focus Length               | Format                         |
| -------------- | ---------------: | -------------------------- | ------------------------------ |
| Light          |               10 | 3-5 focus words per course | Name — Provider \| short focus |
| Medium         |                7 | 1-3 focus words per course | Name — Provider \| keywords    |
| Dense          |                5 | 2 words                    | Name — Provider                |
| Overflow       | 3-5 relevant max | 1 words                    | Name — Provider only           |

Course selection is AI-assisted based on relevance to vacancy, but the maximum count and focus length come from backend budget rules.

## 7.6 Projects and Volunteering

Projects are rendered only if the user has project records.

| Page 2 Density | Projects Shown | Description                  | Bullet Points |
| -------------- | -------------: | ---------------------------- | ------------- |
| Light          |        Up to 2 | 4-5 sentences per project    | 2-4 bullets   |
| Medium         |        Up to 2 | 2-3 sentences per project    | 2-3 bullets   |
| Dense          |        Up to 2 | 2 short sentences per project | 1-2 bullets   |
| Overflow       |        Up to 2 | Title + 1 compact sentence   | 1 bullet      |

If there are no projects, the section is omitted.

If Page 2 contains both Additional Work Experience and Projects, Additional Work Experience has priority over detailed project text. Projects should be shortened first.

## 7.7 Professional Aspirations

AI output must always include Professional Aspirations.

| Page 2 Density / Template | Aspirations Volume |
|---|---|
| One-page | 5 sentences unless page is dense |
| One-page dense | 1-3 short sentences |
| Light Page 2 | 5-9 sentences as space filler |
| Medium Page 2 | 3-5 sentences |
| Dense Page 2 | 1-3 sentences |

AI may expand aspirations based on user profile data, vacancy context, and target role. The user reviews and edits the text before final save.

## 7.8 Personal Information

Personal Information is rendered by backend rules, not AI.

| Field | Rule |
|---|---|
| Location | Include if provided. |
| Languages | Include if provided. |
| Readiness for Relocation | Include if not “Not specified”. |
| Ready for Business Trips | Include if not “Not specified”. |
| Preferred Work Format | Include if at least one value is selected. |
| Citizenship | Include if provided. |
| Date of Birth | Include if provided. |

Missing optional fields are omitted without leaving empty labels or visual gaps.

## 8. Empty-Section Handling

| Section / Field                 | If Empty                                                                       |
| ------------------------------- | ------------------------------------------------------------------------------ |
| Work Experience                 | Block generation. At least 1 job is required.                                  |
| Education                       | Block generation. At least 1 education record is required.                     |
| Contact Details required fields | Block generation until fixed.                                                  |
| Skills                          | Warn user; generation may be not efficient if skills are skipped.              |
| Courses                         | Omit Courses section. Allow Page 1 capacity expansion according to DB-backed budget rules. |
| Projects                        | Omit Projects section.                                                         |
| Aspirations input               | AI still generates Professional Aspirations.                                   |
| LinkedIn                        | Omit from contact line.                                                        |
| Portfolio / Website             | Omit from contact line.                                                        |
| Telegram                        | Omit from contact line.                                                        |
| WhatsApp                        | Omit from contact line.                                                        |
| Date of Birth                   | Omit from Personal Information.                                                |
| Citizenship                     | Omit from Personal Information.                                                |

Renderer must not output empty section headers, empty labels, or placeholder text in final PDF.

## 9. Template Data Contract

## 9.1 AI Response Contract

AI must return structured JSON with fixed top-level sections.

Text fields may contain limited sanitized HTML.

Allowed tags:

| Tag | Purpose |
|---|---|
| `<p>` | Paragraphs |
| `<strong>` | Strong emphasis |
| `<em>` | Light emphasis |
| `<ul>` | Bullet list |
| `<li>` | Bullet item |
| `<br>` | Line break |

Disallowed:

- `<script>`;
- inline event handlers;
- external styles;
- external links inside generated text unless explicitly allowed;
- arbitrary HTML layouts.

~~~json
{
  "header": {
    "candidateName": "string",
    "professionalTitle": "string",
    "valueLine": "string"
  },
  "professionalSummary": {
    "html": "<p>string</p>"
  },
  "workExperience": {
    "primary": [
      {
        "sourceId": "string",
        "jobTitle": "string",
        "companyName": "string",
        "location": "string",
        "dateRange": "string",
        "descriptionHtml": "<p>string</p>",
        "bulletsHtml": "<ul><li>string</li></ul>"
      }
    ],
    "additional": [
      {
        "sourceId": "string",
        "jobTitle": "string",
        "companyName": "string",
        "location": "string",
        "dateRange": "string",
        "summaryHtml": "<p>one short strong sentence</p>"
      }
    ]
  },
  "skills": [
    {
      "groupName": "string",
      "items": ["string"]
    }
  ],
  "education": [
    {
      "sourceId": "string",
      "institutionName": "string",
      "degree": "string",
      "fieldOfStudy": "string",
      "location": "string",
      "dateRange": "string",
      "descriptionHtml": "<p>string</p>"
    }
  ],
  "coursesAndCertificates": [
    {
      "sourceId": "string",
      "title": "string",
      "provider": "string",
      "focus": "string",
      "dateRange": "string"
    }
  ],
  "projectsAndVolunteering": [
    {
      "sourceId": "string",
      "projectName": "string",
      "role": "string",
      "dateRange": "string",
      "descriptionHtml": "<p>string</p>",
      "bulletsHtml": "<ul><li>string</li></ul>"
    }
  ],
  "professionalAspirations": {
    "html": "<p>string</p>"
  },
  "renderingHints": {
    "templateMode": "one-page | two-page",
    "pageProfile": "string",
    "densityLevel": "Light | Medium | Dense"
  }
}
~~~

## 9.2 Backend-Generated Template Data

The backend adds non-AI fields before rendering. Considerations:

| Field | Source |
|---|---|
| Contact line | Contact Details profile data |
| Email / phone / links | Contact Details profile data |
| Personal Information | Additional Info profile data |
| Public resume code | Saved resume/public link service |
| PDF filename | Backend file naming rule |
| Template mode | ResumePageProfileService |
| Page count validation result | PdfGenerationService |

## 9.3 Contract Rules

- AI must not change source IDs.
- AI must not create work experience, education, courses, or projects that do not exist in source profile data.
- AI may rephrase, prioritize, compact, and expand text within the allowed content budget and based on adaptation level.
- AI may generate Professional Aspirations even if the user did not provide aspirations input.
- Backend validates JSON structure before showing review form.
- Backend sanitizes allowed HTML before storing/rendering.
- Vue displays formatted structured fields and allows user editing with formatting html tags.
- User edits structured sections before final save.

## 10. AI Prompt Instructions

## 10.1 Input to AI

Backend passes the following values to AI.

~~~text
Template Mode: one-page | two-page
Page Profile: Light / Medium / Dense combination
Page 1 Work Experience IDs: [IDs]
Page 2 Work Experience IDs: [IDs]
Education count: N
Courses count: N
Projects count: N
Professional Summary budget: sentence count
Skill budget: groups + skills per group + words per skill
Course budget: max count + focus words per course
Project budget: max count + sentences + bullet points
Aspirations budget: target sentence count
Vacancy description
Company information
Additional comments for AI
~~~

## 10.2 Content Budget Rules

PromptBuilder must include explicit limits. These limits are controlled through DB-backed budget configuration and can be changed without restarting the Java application.

~~~text
Professional Summary:
- Use {N} sentences.
- Use as a buffer: expand if profile is light, compact if page is dense.

Work Experience:
- Use only the provided source IDs.
- Page 1 jobs: {N}; description length: {X}; bullets: {Y}.
- Page 2 jobs: {N}; one short strong sentence per job; no bullet points.

Skills:
- Group skills thematically.
- Use {N} groups, {X-Y} skills per group, {Z} words per skill.

Courses:
- Select up to {N} most vacancy-relevant courses.
- Use {X} focus words per course.
- Use the required compact format.

Projects:
- Select up to {N} projects based on relevance.
- Use {X-Y} sentences and {Z} bullet points per project.

Professional Aspirations:
- Always return this section.
- Use {X-Y} sentences.
- May expand aspirations as space filler using profile and vacancy context.

Overflow:
- If content may exceed the budget, cut least vacancy-relevant items.
- Do not reduce font size or ask renderer to change CSS.
~~~

## 11. DB-Backed Resume Budget Configuration

## 11.1 Purpose

All key content budget parameters are stored in PostgreSQL and read before every resume generation.

Purpose:
- adjust sentence counts, bullet limits, skill limits, and density thresholds quickly;
- avoid hardcoding budget parameters in Java code;
- allow tuning after HTML/PDF testing;
- allow runtime configuration changes without Java code changes and without application restart.

Budget settings are stored in PostgreSQL and read by the Java backend before each resume generation.

## 11.2 Configuration Scope

The DB-backed configuration controls:

- one-page/two-page threshold;
- course-free Page 1 expansion;
- max Page 1 jobs;
- max Page 2 additional jobs;
- Professional Summary sentence counts;
- work experience description/bullet limits;
- skills groups and skill length;
- course count and focus length;
- project count, sentences, and bullets;
- aspirations length.

## 11.3 Active Config Selection

Backend uses the following logic before each resume generation:

1. Query `resume_budget_configs`.
2. Prefer configs where `is_active = true`.
3. If one active config exists, use it.
4. If multiple active configs exist, use the newest one by `updated_at DESC, id DESC`.
5. If no active config exists, use the newest config by `updated_at DESC, id DESC` as fallback.
6. If no config exists at all, stop generation and return a clear internal configuration error.

Recommended SQL query:

~~~sql
SELECT *
FROM resume_budget_configs
ORDER BY
    is_active DESC,
    updated_at DESC,
    id DESC
LIMIT 1;
~~~

This query is intentionally simple and MVP-friendly.

## 11.4 Versioning Rule

MVP versioning rules:

- There is one general active configuration for MVP.
- `version_no` is incremented when config settings are changed.
- The generation request stores `budget_config_id` and `budget_config_version_used`.
- No full config history/version tables are required for MVP.
- It is acceptable to update the same active config row and increment `version_no`.

## 11.5 Data Model

### resume_budget_configs

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | bigint | PK, AUTO_INCREMENT | Unique identifier |
| `name` | varchar(100) | NOT NULL | Config display name |
| `version_no` | int | NOT NULL, DEFAULT 1 | Incremented on settings change |
| `is_active` | boolean | NOT NULL, DEFAULT false | Active flag; only one active config allowed |
| `description` | text | | Config description |
| `created_at` | timestamp | NOT NULL | Record creation timestamp |
| `updated_at` | timestamp | NOT NULL | Last update timestamp |

PostgreSQL partial unique index to enforce one active config:

~~~sql
CREATE UNIQUE INDEX uq_one_active_resume_budget_config
ON resume_budget_configs (is_active)
WHERE is_active = true;
~~~

### resume_template_selection_rules

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | bigint | PK, AUTO_INCREMENT | Unique identifier |
| `config_id` | bigint | FK → resume_budget_configs.id, NOT NULL | Parent config |
| `rule_key` | varchar(100) | NOT NULL | Rule identifier |
| `value_type` | varchar(20) | NOT NULL | Value type: int, boolean, text |
| `int_value` | int | | Integer value |
| `boolean_value` | boolean | | Boolean value |
| `text_value` | varchar(255) | | Text value |
| `description` | text | | Rule description |
| `created_at` | timestamp | NOT NULL | Record creation timestamp |
| `updated_at` | timestamp | NOT NULL | Last update timestamp |

Indexes: unique on (`config_id`, `rule_key`).

Check constraint:

~~~sql
ALTER TABLE resume_template_selection_rules
ADD CONSTRAINT chk_template_rule_value_type
CHECK (
    (value_type = 'int' AND int_value IS NOT NULL AND boolean_value IS NULL AND text_value IS NULL)
 OR (value_type = 'boolean' AND boolean_value IS NOT NULL AND int_value IS NULL AND text_value IS NULL)
 OR (value_type = 'text' AND text_value IS NOT NULL AND int_value IS NULL AND boolean_value IS NULL)
);
~~~

### resume_work_experience_distribution_rules

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | bigint | PK, AUTO_INCREMENT | Unique identifier |
| `config_id` | bigint | FK → resume_budget_configs.id, NOT NULL | Parent config |
| `case_key` | varchar(20) | NOT NULL | Edge case key: EC-001, EC-003, EC-010, etc. |
| `min_total_jobs` | int | NOT NULL | Minimum total jobs for this rule |
| `max_total_jobs` | int | NOT NULL | Maximum total jobs for this rule |
| `min_projects` | int | NOT NULL, DEFAULT 0 | Minimum projects for this rule |
| `max_projects` | int | | Maximum projects; NULL = unlimited |
| `require_no_courses` | boolean | NOT NULL, DEFAULT false | Whether no-courses condition is required |
| `template_mode` | varchar(20) | NOT NULL | one_page or two_page |
| `page1_jobs` | int | NOT NULL | Number of jobs on Page 1 |
| `page2_jobs` | int | NOT NULL, DEFAULT 0 | Number of jobs on Page 2 |
| `page2_max_additional_jobs` | int | | Max additional jobs on Page 2 |
| `priority` | int | NOT NULL, DEFAULT 100 | Lower value = higher priority |
| `notes` | text | | Rule notes |
| `created_at` | timestamp | NOT NULL | Record creation timestamp |
| `updated_at` | timestamp | NOT NULL | Last update timestamp |

Indexes: unique on (`config_id`, `case_key`).

### resume_section_budget_rules

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | bigint | PK, AUTO_INCREMENT | Unique identifier |
| `config_id` | bigint | FK → resume_budget_configs.id, NOT NULL | Parent config |
| `section_key` | varchar(100) | NOT NULL | Section: professional_summary, skills, courses, projects, etc. |
| `profile_key` | varchar(100) | NOT NULL | Profile: light, medium, dense, one_page_light, etc. |
| `metric_key` | varchar(100) | NOT NULL | Metric: sentences, bullet_points, max_courses, words_per_skill, etc. |
| `min_value` | int | | Minimum value for this metric |
| `max_value` | int | | Maximum value for this metric |
| `notes` | text | | Rule notes |
| `created_at` | timestamp | NOT NULL | Record creation timestamp |
| `updated_at` | timestamp | NOT NULL | Last update timestamp |

Indexes: unique on (`config_id`, `section_key`, `profile_key`, `metric_key`).

### Updated resume_generation_requests

Add these columns to `resume_generation_request`:

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `budget_config_id` | bigint | FK → resume_budget_configs.id | Budget config used for generation |
| `budget_config_version_used` | int | | Version of config at generation time |

## 11.6 Example Records

### resume_budget_configs

| id | name | version_no | is_active | description |
|---:|---|---:|---|---|
| 1 | Default MVP Resume Budget | 1 | true | Main budget configuration for one-page and two-page resume templates |

### resume_template_selection_rules

| config_id | rule_key | value_type | int_value | boolean_value | text_value | description |
|---:|---|---|---:|---|---|---|
| 1 | standard_one_page_max_jobs | int | 3 | null | null | Normal one-page candidate if no projects |
| 1 | allow_course_free_page1_expansion | boolean | null | true | null | Allow Page 1 expansion when no courses and no projects |
| 1 | course_free_page1_max_jobs | int | 5 | null | null | Max jobs on Page 1 when no courses and no projects |
| 1 | default_two_page_min_jobs | int | 4 | null | null | Default threshold for two-page template |
| 1 | page2_max_additional_jobs | int | 7 | null | null | Max additional Work Experience entries on Page 2 |

### resume_work_experience_distribution_rules

| case_key | min_total_jobs | max_total_jobs | min_projects | max_projects | require_no_courses | template_mode | page1_jobs | page2_jobs | page2_max_additional_jobs | priority |
|---|---:|---:|---:|---:|---|---|---:|---:|---:|---:|
| EC-001 | 1 | 1 | 0 | 0 | false | one_page | 1 | 0 | 0 | 10 |
| EC-003 | 3 | 3 | 0 | 0 | false | one_page | 3 | 0 | 0 | 10 |
| EC-010 | 4 | 4 | 0 | 0 | false | two_page | 2 | 2 | 2 | 20 |
| EC-012 | 5 | 5 | 0 | 0 | false | two_page | 3 | 2 | 2 | 20 |
| EC-016 | 7 | 99 | 0 | null | false | two_page | 3 | 7 | 7 | 30 |
| EC-017 | 4 | 5 | 0 | 0 | true | one_page | 5 | 0 | 0 | 5 |

Lower `priority` value means higher priority. Special cases such as `EC-017` should be evaluated before generic rules.

### resume_section_budget_rules

| section_key | profile_key | metric_key | min_value | max_value |
|---|---|---|---|---:|---:|
| professional_summary | one_page_light | sentences | 5 | 5 |
| professional_summary | one_page_dense | sentences | 2 | 3 |
| work_experience_page1 | one_job | description_sentences | 5 | 5 |
| work_experience_page1 | one_job | bullet_points | 3 | 9 |
| work_experience_page1 | three_jobs | description_sentences | 3 | 3 |
| work_experience_page1 | three_jobs | bullet_points | 2 | 5 |
| work_experience_page2 | default | summary_sentences_per_job | 1 | 1 |
| work_experience_page2 | default | bullet_points_per_job | 0 | 0 |
| skills | light | groups | 4 | 5 |
| skills | light | skills_per_group | 5 | 7 |
| skills | light | words_per_skill | 1 | 3 |
| courses | medium | max_courses | 0 | 7 |
| courses | medium | focus_words_per_course | 1 | 3 |
| projects | light | max_projects | 0 | 4 |
| projects | light | sentences_per_project | 2 | 3 |
| projects | light | bullet_points_per_project | 2 | 4 |
| aspirations | light_page2 | sentences | 5 | 9 |

## 11.7 Fixed Section Order

Do not store resume section order in DB for MVP.

Reason:

- Section order is fixed.
- There is no requirement to configure section order through admin panel.
- Storing section order in DB adds unnecessary complexity.
- Section order belongs to template rendering logic, not runtime budget configuration.

Fixed resume section order is implemented in backend rendering code and is not configurable through DB in MVP.

## 11.8 Runtime Flow

Before each resume generation:

1. Backend reads active/newest resume budget config from DB.
2. Backend reads template selection rules.
3. Backend reads work experience distribution rules.
4. Backend reads section budget rules.
5. Backend calculates raw profile counts.
6. Backend selects template mode and distribution rule.
7. Backend builds prompt budget.
8. AI receives exact section limits.
9. AI returns structured JSON.
10. Backend stores config ID and version used with generation request.

No cache is used in MVP.

This means:

- changing DB values affects future generations immediately;
- already generated PDFs remain unchanged;
- new resume generation requests use the latest selected config.

## 12. PDF Rendering and Validation

## 12.1 Rendering Ownership

Final PDF rendering is backend responsibility.

Vue responsibilities:

- show generation settings form;
- show structured Resume Review form;
- display formatted editable fields;
- allow editing generated fields with formatting;
- submit final version for save/finalize;
- show link to final PDF after generation.

Backend responsibilities:

- render final HTML from template;
- sanitize allowed HTML;
- generate PDF;
- validate page count;
- save PDF file metadata;
- expose public PDF link.

## 12.2 Validation Rules

| Validation | Rule |
|---|---|
| One-page PDF | Must be exactly 1 page. |
| Two-page PDF | Must be exactly 2 pages. |
| Text selectability | PDF text must be selectable. |
| Empty sections | Must not appear in final PDF. |
| HTML safety | AI-provided HTML must be sanitized by allowlist. |
| Overflow | Must trigger compact retry or trimming. |
| Public link | Must open final PDF directly. |

## 12.3 Fallback Rules

If rendering fails:

1. Retry with compact content budget.
2. Trim least vacancy-relevant optional items.
3. If one-page still overflows, switch to two-page template.
4. If two-page still overflows, return a clear generation/rendering error for manual adjustment.

## 13. PDF File Naming

Format:
~~~text
{FirstName}_{LastName}_{JobTitle}_Resume.pdf
~~~

Example:
~~~text
John_Doe_Project_Manager_Resume.pdf
~~~

Filename should be sanitized for invalid filesystem characters.

## 14. Acceptance Criteria

## AC-001 Template Mode Selection

Given a user has valid minimum profile data, when a resume generation request is created, then the backend calculates raw counts, page scores, density labels, selected template mode, and content budgets before AI generation.

## AC-002 Structured AI Output

Given AI generation is completed, when the backend receives the response, then the response must match the structured JSON contract before it is shown in Resume Review.

## AC-003 Limited HTML Formatting

Given AI returns formatted text fields, when the backend validates the response, then only approved HTML tags are accepted and unsafe HTML is removed or rejected.

## AC-004 Editable Review

Given a valid structured generation response, when the user opens Resume Review, then the user can edit generated sections with formatting before final save.

## AC-005 Backend PDF Rendering

Given the user saves the final resume, when the backend renders the final version, then it uses the selected HTML template and generates a selectable-text PDF.

## AC-006 Page Count Validation

Given PDF generation is completed, when the backend validates the PDF, then one-page template output must contain exactly 1 page and two-page template output must contain exactly 2 pages.

## AC-007 Empty Section Handling

Given optional fields or sections are missing, when the final PDF is rendered, then missing optional data is omitted without empty labels, empty headers, or placeholder text.

## AC-008 Configurable Budget Rules

Given the DB-backed budget configuration is updated in PostgreSQL, when a new resume generation request is processed, then the updated content budget values are used without requiring Java code changes or application restart.

## 15. Related Artifacts

| Artifact | Location |
|---|---|
| DBML ERD | `docs/04_domain-and-data-model/dbml_erd.md` |
| Data Dictionary | `docs/04_domain-and-data-model/data_dictionary.md` |
| User Workflows | `docs/03_processes-and-workflows/user_workflows.md` |
| Wireframe Descriptions | `docs/05_ui-ux/wireframes_detailed_description.md` |
| HTML Template: One-page | `docs/05_ui-ux/one_pager_template.html` |
| HTML Template: Two-page | `docs/05_ui-ux/two_pager_template.html` |
| Resume Generation Requirements | `docs/02_requirements/requirements_log.md` |
| Decision Log | `docs/09_decisions/decision_log.md` |
| Risk Register | `docs/07_project-management/risk_register.md` |

## 16. Template content adaptation in HTML rules for 2 pager

### Dynamic Resume Layout: Full Technical Description

The solution is designed for a strict two-page A4 resume layout. Its goal is to make the resume visually adapt to the amount of content while keeping typography controlled, professional, and predictable.

The layout follows two core principles:

1. **Font size is adapted first.**
2. **Section spacing is adjusted only after the font size has been calculated.**

This avoids the common problem where empty space is hidden by large gaps before the text size has been properly optimized.

#### 1. Page Structure

Each resume page is represented by a `.resume-page` element:

```html
<main class="resume-page page-1">
  <div class="page-content">
    ...
  </div>
</main>

<main class="resume-page page-2">
  <div class="page-content">
    ...
  </div>
</main>
```

The pages are fixed to A4 dimensions:

```css
.resume-page {
  width: 210mm;
  height: 297mm;
  min-height: 297mm;
  padding: 11mm;
  box-sizing: border-box;
}
```

This means every page has a hard physical size equal to one A4 sheet.

The template also forces a page break after every resume page:

```css
.resume-page {
  page-break-after: always;
  break-after: page;
}
```

The final page disables the extra break:

```css
.resume-page:last-child {
  page-break-after: auto;
  break-after: auto;
}
```

#### 2. CSS Variables Used for Adaptation

The adaptive behavior is controlled by CSS custom properties:

```css
.resume-page {
  --font-scale: 1;
  --section-gap: 0px;
  --page-two-min-fill-ratio: 0.5;
}
```

##### `--font-scale`

This controls all proportional font scaling.

At the minimum value:

```css
--font-scale: 1;
```

all fonts use their original base sizes.

For example:

```css
font-size: calc(10.5px * var(--font-scale));
```

When `--font-scale` is `1`, the text is `10.5px`.

When `--font-scale` is `1.5238`, the text becomes approximately `16px`.

##### `--section-gap`

This controls vertical space between major sections inside `.page-content`.

```css
.page-content {
  row-gap: var(--section-gap);
}
```

Initially, this is `0px`.

It is calculated dynamically only after the shared font size has been finalized.

##### `--page-two-min-fill-ratio`

This represents the minimum visual fill target for page 2.

```css
--page-two-min-fill-ratio: 0.5;
```

In the JavaScript logic, the same rule is represented as:

```js
const PAGE_2_MIN_FILL_RATIO = 0.5;
```

This means page 2 content should occupy at least **50% of the full A4 page height**.

#### 3. Dynamic Font Scaling

The current font sizes are treated as the **absolute minimum**.

The main inherited body text starts from:

```css
font-size: calc(10.5px * var(--font-scale));
```

The maximum allowed main text size is equivalent to `16px`, similar to a large Microsoft Word body text size.

The maximum scale is calculated as:

```js
const MAX_SCALE = 16 / 10.5;
```

That gives approximately:

```js
MAX_SCALE = 1.5238
```

So the allowed scale range is:

```js
MIN_SCALE = 1
MAX_SCALE = 1.5238
```

The font can grow, but it can never shrink below the original template size.

#### 4. Proportional Font Scaling

All important font sizes are multiplied by the same `--font-scale`.

For example:

```css
.candidate-name {
  font-size: calc(25px * var(--font-scale));
}

.candidate-title {
  font-size: calc(14px * var(--font-scale));
}

.section-title {
  font-size: calc(12px * var(--font-scale));
}

.item-heading {
  font-size: calc(10.4px * var(--font-scale));
}

.summary-text,
.keyword-line,
.skill-group,
.education-line,
.course-line,
.info-line {
  font-size: calc(10px * var(--font-scale));
}
```

This keeps the visual hierarchy consistent.

If the body text grows by 20%, all headings and section labels also grow by 20%.

That means the design does not become visually distorted.

#### 5. Shared Font Scale for the Whole Resume

The template does **not** calculate separate font sizes for page 1 and page 2.

Instead, it calculates the maximum possible scale for each page individually, then chooses the smallest value.

Conceptually:

```js
page1MaxScale = maximum scale where page 1 still fits
page2MaxScale = maximum scale where page 2 still fits

sharedScale = Math.min(page1MaxScale, page2MaxScale)
```

Then the same `sharedScale` is applied to both pages.

This ensures both pages look like one consistent document.

Example:

```text
Page 1 can fit scale 1.12
Page 2 can fit scale 1.45

Final shared scale = 1.12
```

Even though page 2 could support larger text, it still uses `1.12` so that the whole resume remains visually consistent.

#### 6. How the Maximum Scale Is Found

The JavaScript uses a binary search algorithm.

The function is:

```js
function findMaxScaleForPage(page) {
  let low = MIN_SCALE;
  let high = MAX_SCALE;

  setSectionGap(page, 0);
  setScale(page, MIN_SCALE);

  if (!pageFits(page)) {
    page.dataset.overflowAtMinFont = "true";
    return MIN_SCALE;
  }

  delete page.dataset.overflowAtMinFont;

  for (let i = 0; i < 22; i++) {
    const mid = (low + high) / 2;
    setScale(page, mid);

    if (pageFits(page)) {
      low = mid;
    } else {
      high = mid;
    }
  }

  return low;
}
```

The algorithm works like this:

1. Start with the minimum font scale.
2. Check whether the page fits.
3. If the page does not fit even at minimum size, mark it as overflowing.
4. If it fits, try a larger scale.
5. If the larger scale still fits, keep increasing.
6. If it overflows, reduce the scale.
7. Repeat this process 22 times for precision.
8. Return the largest scale that still fits.

The page fit check is:

```js
function pageFits(page) {
  return page.scrollHeight <= page.clientHeight + FIT_TOLERANCE_PX;
}
```

`scrollHeight` is the actual height required by the content.

`clientHeight` is the available height of the fixed A4 page.

A small tolerance is allowed:

```js
const FIT_TOLERANCE_PX = 2;
```

This prevents tiny rendering differences from causing false overflow.

#### 7. Why Spacing Is Reset Before Font Fitting

Before calculating font size, the template resets section gaps:

```js
function resetSpacing() {
  pages.forEach(function (page) {
    setSectionGap(page, 0);
  });
}
```

This is important.

If section gaps were already large, the algorithm might think the page cannot support a larger font, even though the problem is actually the gaps.

So the order is always:

```text
1. Remove dynamic gaps.
2. Find maximum possible font scale.
3. Apply shared font scale.
4. Add dynamic gaps only after font size is finalized.
```

This makes the adaptation predictable.

#### 8. Replacing `space-between`

The original template used:

```css
justify-content: space-between;
```

That caused the browser to spread sections vertically before the font size was calculated.

In v3, this was changed to:

```css
.page-content {
  justify-content: flex-start;
  row-gap: var(--section-gap);
}
```

This gives JavaScript full control over vertical spacing.

Instead of letting the browser automatically distribute all remaining space, the script calculates exactly how much spacing is needed.

This makes the layout more controlled and easier to debug.

#### 9. Dynamic Section Gap Calculation

The function responsible for spacing is:

```js
function distributeSectionGapsToTarget(page, targetHeightPx) {
  const content = page.querySelector(".page-content");
  if (!content) return;

  setSectionGap(page, 0);

  const sections = getVisibleSections(content);
  const gapSlots = Math.max(0, sections.length - 1);

  if (gapSlots === 0) return;

  const naturalContentHeight = getNaturalContentHeight(content);
  const maxUsableHeight = content.clientHeight;
  const safeTargetHeight = Math.min(targetHeightPx, maxUsableHeight - GAP_SAFETY_PX);
  const extraSpaceNeeded = safeTargetHeight - naturalContentHeight;

  if (extraSpaceNeeded <= 0) return;

  const gap = extraSpaceNeeded / gapSlots;
  setSectionGap(page, gap);
}
```

This function works as follows:

1. Find the `.page-content` element.
2. Reset the current section gap to `0px`.
3. Get all visible direct child sections.
4. Count the number of gaps between sections.
5. Measure the natural content height.
6. Compare it with the desired target height.
7. If the content is shorter than the target, calculate the missing space.
8. Divide the missing space evenly between section gaps.
9. Apply the calculated gap via `--section-gap`.

Example:

```text
Target height: 500px
Natural content height: 380px
Extra space needed: 120px
Number of section gaps: 4

Gap per section = 120 / 4 = 30px
```

Then the page receives:

```css
--section-gap: 30px;
```

#### 10. Measuring Natural Content Height

The natural content height is calculated with:

```js
function getNaturalContentHeight(content) {
  const sections = getVisibleSections(content);

  if (!sections.length) {
    return 0;
  }

  const first = sections[0];
  const last = sections[sections.length - 1];

  return (last.offsetTop + last.offsetHeight) - first.offsetTop;
}
```

This does not simply use `content.scrollHeight`.

Instead, it measures the distance from the top of the first visible section to the bottom of the last visible section.

This gives a better estimate of the actual visual content block.

#### 11. Page 1 Spacing Behavior

Page 1 is allowed to use its available content area more fully.

After the shared font scale is applied, this function runs:

```js
function distributePageOneRemainingSpace() {
  const page = document.querySelector(".page-1");
  if (!page) return;

  const content = page.querySelector(".page-content");
  if (!content) return;

  distributeSectionGapsToTarget(page, content.clientHeight);
}
```

This means page 1 tries to spread sections across the available `.page-content` height.

The target is:

```js
content.clientHeight
```

So page 1 can use most of the usable page area after padding.

#### 12. Page 2 Minimum Fill Behavior

Page 2 has a different rule.

It should not leave all content compressed at the top if there is little content.

The function is:

```js
function distributePageTwoMinimumFill() {
  const page = document.querySelector(".page-2");
  if (!page) return;

  const content = page.querySelector(".page-content");
  if (!content) return;

  const a4PageHeightPx = page.clientHeight;
  const minimumContentBlockHeight = a4PageHeightPx * PAGE_2_MIN_FILL_RATIO;

  distributeSectionGapsToTarget(page, minimumContentBlockHeight);
}
```

The target height is calculated from the full A4 page height:

```js
minimumContentBlockHeight = page.clientHeight * 0.5
```

So if the A4 page is approximately `1122px` high in the browser, the minimum content block height is approximately:

```text
1122px * 0.5 = 561px
```

If page 2 content naturally occupies less than that, section gaps are increased until the content visually occupies 50% of the page.

If page 2 content already occupies more than 50%, no extra gap is added.

#### 13. Safety Limits for Gaps

The gap calculation includes a safety margin:

```js
const GAP_SAFETY_PX = 1;
```

The target height is capped:

```js
const safeTargetHeight = Math.min(targetHeightPx, maxUsableHeight - GAP_SAFETY_PX);
```

This prevents the script from adding gaps that push content beyond the usable page height.

So even if the target is large, the algorithm will not intentionally create overflow.

#### 14. Full Adaptation Order

The full adaptation process is handled by:

```js
function fitResumeWithSharedScale() {
  if (!pages.length) return;

  resetSpacing();

  pages.forEach(function (page) {
    setScale(page, MIN_SCALE);
  });

  const maxScalesPerPage = pages.map(findMaxScaleForPage);
  const sharedScale = Math.min.apply(null, maxScalesPerPage);

  pages.forEach(function (page) {
    setScale(page, sharedScale);
    page.dataset.fontScale = sharedScale.toFixed(4);
  });

  distributePageOneRemainingSpace();
  distributePageTwoMinimumFill();
}
```

The order is:

```text
1. Reset all dynamic section gaps to 0.
2. Set all pages to the minimum font scale.
3. Calculate the maximum scale each page can support.
4. Choose the smallest page scale as the shared resume scale.
5. Apply that shared scale to all pages.
6. Add dynamic spacing to page 1.
7. Add minimum-fill spacing to page 2.
```

This is the most important rule in the whole solution.

#### 15. When the Adaptation Runs

The script runs in three situations.

##### On page load

```js
window.addEventListener("load", function () {
  requestAnimationFrame(fitResumeWithSharedScale);
});
```

This ensures the layout is calculated after the page is loaded.

##### On browser resize

```js
window.addEventListener("resize", function () {
  clearTimeout(resizeTimer);
  resizeTimer = setTimeout(function () {
    requestAnimationFrame(fitResumeWithSharedScale);
  }, 100);
});
```

This recalculates the layout if the viewport or rendering dimensions change.

The `100ms` delay prevents excessive recalculations while resizing.

##### Before printing

```js
window.addEventListener("beforeprint", function () {
  fitResumeWithSharedScale();
});
```

This is important for PDF generation.

Before the browser opens the print/PDF process, the layout is recalculated to make sure the printed output uses the final adapted scale and spacing.

#### 16. Overflow Handling

If a page does not fit even at the minimum scale, the script does not shrink the font further.

Instead, it marks the page:

```js
page.dataset.overflowAtMinFont = "true";
```

This means the content is too large for the page under the current rules.

The minimum font size is protected.

The template intentionally does **not** solve this by making the font smaller than the original design.

In that case, the content itself needs to be reduced, moved, split, or restructured.

#### 17. Summary of Rules

The adaptive rules are:

```text
1. A4 page size is fixed: 210mm × 297mm.
2. Current font sizes are the minimum allowed values.
3. Main inherited text starts at 10.5px.
4. Main inherited text can grow up to 16px.
5. Maximum font scale is 16 / 10.5.
6. All other text sizes scale proportionally.
7. Each page is tested individually to find its maximum fitting scale.
8. The final resume scale is the smallest maximum scale among all pages.
9. The same font scale is applied to all pages.
10. Section gaps are reset before measuring fonts.
11. Section gaps are added only after font scaling is complete.
12. Page 1 may distribute sections across its usable content height.
13. Page 2 must occupy at least 50% of the full A4 page height.
14. If page 2 content is shorter than 50%, section gaps are increased.
15. If page 2 content is already taller than 50%, no artificial extra spacing is added.
16. The font never shrinks below the original template size.
17. If content still overflows at minimum size, the page is marked as overflowing.
```

#### 18. General Principle for Another AI

The general idea is:

> Treat the original template typography as the minimum design. Measure each fixed-size page with zero dynamic spacing. Use binary search to find the largest font scale each page can support without overflow. Apply the smallest of those scales globally to preserve visual consistency. Then, and only then, distribute remaining vertical space through controlled section gaps according to page-specific rules.

For this template:

```text
Page 1 rule:
Use available content height more fully.

Page 2 rule:
If content is short, expand section gaps until the visible content block reaches at least 50% of A4 height.
```

This produces a resume that adapts to content density while avoiding uncontrolled browser spacing and preserving a consistent typographic hierarchy across both pages.


## 17. Template content adaptation in HTML rules for 1 pager

### Dynamic Resume Layout: Full Technical Description

This solution is designed for a strict single-page A4 resume template. Its goal is to make a one-page resume adapt visually to different amounts of content while keeping typography consistent, controlled, and professional.

The adaptation follows two strict stages:

1. **Font size adaptation happens first.**
2. **Section spacing adaptation happens only after the final font scale is selected.**

This order is important because the template should first use typography to improve readability. Only after the font size has reached the best possible value should the layout use vertical gaps to fill the page.

The original template font sizes are treated as the absolute minimum. The template may enlarge text, but it must never reduce text below the original design values.

#### 1. Page Structure

The resume uses a single `.resume-page` element:

```html
<main class="resume-page">
  <div class="page-content">
    ...
  </div>
</main>
```

The page is fixed to A4 dimensions:

```css
.resume-page {
  width: 210mm;
  min-height: 297mm;
  height: 297mm;
  padding: 11mm;
  box-sizing: border-box;
}
```

This means the page has a hard physical size equal to one A4 sheet.

The page does not grow beyond A4 height. All content must either fit inside this page or be reduced manually by changing the actual resume content.

#### 2. Main Layout Container

The internal content wrapper is `.page-content`.

In the previous version, the template used:

```css
justify-content: space-between;
```

That made the browser automatically spread sections vertically before font size was calculated.

In the updated version, this was replaced with controlled spacing:

```css
.page-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  row-gap: var(--section-gap);
  min-height: 265mm;
}
```

This means sections are placed from top to bottom normally.

The browser no longer decides the gaps automatically.

Instead, JavaScript calculates the correct section gap after font scaling is complete.

#### 3. CSS Variables Used for Adaptation

The adaptive behavior is controlled through CSS custom properties:

```css
.resume-page {
  --font-scale: 1;
  --section-gap: 0px;
  --single-page-min-fill-ratio: 0.8;
}
```

These variables control the entire layout.

##### `--font-scale`

This variable controls proportional font scaling.

At the minimum value:

```css
--font-scale: 1;
```

all fonts use the original template sizes.

For example:

```css
font-size: calc(10.5px * var(--font-scale));
```

When `--font-scale` is `1`, the inherited body text is `10.5px`.

When `--font-scale` is approximately `1.5238`, the inherited body text becomes `16px`.

##### `--section-gap`

This controls the vertical distance between major resume sections:

```css
row-gap: var(--section-gap);
```

Initially, the gap is:

```css
--section-gap: 0px;
```

The value is calculated dynamically only after font size adaptation has completed.

##### `--single-page-min-fill-ratio`

This defines the minimum visual fill target for the one-page resume:

```css
--single-page-min-fill-ratio: 0.8;
```

The JavaScript uses the same rule:

```js
const SINGLE_PAGE_MIN_FILL_RATIO = 0.8;
```

This means the visible content block should occupy at least **80% of the full A4 page height**.

If the natural content is shorter than 80% of the A4 page height, the template increases the gaps between sections.

#### 4. Dynamic Font Scaling

The original font sizes are the hard minimum.

The inherited main text starts from:

```css
font-size: calc(10.5px * var(--font-scale));
```

The maximum allowed main text size is `16px`.

The maximum scale is calculated as:

```js
const MAX_SCALE = 16 / 10.5;
```

This gives approximately:

```js
MAX_SCALE = 1.5238;
```

The allowed scale range is:

```js
const MIN_SCALE = 1;
const MAX_SCALE = 16 / 10.5;
```

The font can grow from the original design size up to the maximum size.

It can never shrink below the original size.

#### 5. Proportional Font Scaling

All important font sizes scale proportionally through the same `--font-scale`.

Examples:

```css
.candidate-name {
  font-size: calc(25px * var(--font-scale));
}

.candidate-title {
  font-size: calc(14px * var(--font-scale));
}

.contact-line {
  font-size: calc(9.7px * var(--font-scale));
}

.value-line {
  font-size: calc(9.8px * var(--font-scale));
}

.section-title {
  font-size: calc(12px * var(--font-scale));
}

.summary-text,
.keyword-line,
.skill-group,
.education-line,
.course-line,
.info-line {
  font-size: calc(10px * var(--font-scale));
}

.item-heading {
  font-size: calc(10.4px * var(--font-scale));
}

.compact-info {
  font-size: calc(9.5px * var(--font-scale));
}
```

This keeps the visual hierarchy stable.

If the main text grows by 20%, headings, labels, section titles, contact lines, and compact info also grow proportionally.

The document does not become visually distorted.

#### 6. Shared Scale Rule for the One-Page Template

Even though this is a one-page template, it still uses the same conceptual rule as the two-page version:

```js
const maxScalesPerPage = pages.map(findMaxScaleForPage);
const sharedScale = Math.min.apply(null, maxScalesPerPage);
```

For a single-page resume, there is only one page, so the shared scale equals the maximum scale that this one page can support.

This keeps the logic compatible with multi-page templates.

Conceptually:

```text
pageMaxScale = maximum scale where the single A4 page still fits

sharedScale = pageMaxScale
```

The selected scale is then applied to the page:

```js
setScale(page, sharedScale);
page.dataset.fontScale = sharedScale.toFixed(4);
```

#### 7. How the Maximum Font Scale Is Found

The JavaScript uses a binary search algorithm.

The function responsible for finding the maximum fitting scale is:

```js
function findMaxScaleForPage(page) {
  let low = MIN_SCALE;
  let high = MAX_SCALE;

  setSectionGap(page, 0);
  setScale(page, MIN_SCALE);

  if (!pageFits(page)) {
    page.dataset.overflowAtMinFont = "true";
    return MIN_SCALE;
  }

  delete page.dataset.overflowAtMinFont;

  for (let i = 0; i < 22; i++) {
    const mid = (low + high) / 2;
    setScale(page, mid);

    if (pageFits(page)) {
      low = mid;
    } else {
      high = mid;
    }
  }

  return low;
}
```

The algorithm works like this:

1. Start at the minimum scale.
2. Reset dynamic section gaps to zero.
3. Check whether the page fits.
4. If the page does not fit even at minimum scale, stop and mark the page as overflowing.
5. If it fits, test a larger scale.
6. If the larger scale fits, continue increasing.
7. If it overflows, reduce the scale.
8. Repeat this process 22 times.
9. Return the largest scale that still fits inside the fixed A4 page.

#### 8. Page Fit Check

The page fit check is:

```js
function pageFits(page) {
  return page.scrollHeight <= page.clientHeight + FIT_TOLERANCE_PX;
}
```

`scrollHeight` is the actual height required by the content.

`clientHeight` is the available height of the fixed A4 page.

A small tolerance is allowed:

```js
const FIT_TOLERANCE_PX = 2;
```

This avoids false overflow caused by tiny browser rendering differences.

#### 9. Why Section Gaps Are Reset Before Font Fitting

Before font size is calculated, all dynamic spacing is removed:

```js
function resetSpacing() {
  pages.forEach(function (page) {
    setSectionGap(page, 0);
  });
}
```

This is essential.

If the page already had large section gaps, the algorithm might incorrectly conclude that larger fonts do not fit.

The correct order is:

```text
1. Remove dynamic gaps.
2. Measure how large the font can become.
3. Apply the final font scale.
4. Add dynamic gaps only after font scaling is complete.
```

This keeps the layout predictable and avoids mixing two different adaptation mechanisms at the same time.

#### 10. Dynamic Section Gap Calculation

After the final font scale is applied, the template calculates vertical spacing between sections.

The main function is:

```js
function distributeSectionGapsToTarget(page, targetHeightPx) {
  const content = page.querySelector(".page-content");
  if (!content) return;

  setSectionGap(page, 0);

  const sections = getVisibleSections(content);
  const gapSlots = Math.max(0, sections.length - 1);

  if (gapSlots === 0) return;

  const naturalContentHeight = getNaturalContentHeight(content);
  const maxUsableHeight = content.clientHeight;
  const safeTargetHeight = Math.min(targetHeightPx, maxUsableHeight - GAP_SAFETY_PX);
  const extraSpaceNeeded = safeTargetHeight - naturalContentHeight;

  if (extraSpaceNeeded <= 0) return;

  const gap = extraSpaceNeeded / gapSlots;
  setSectionGap(page, gap);
}
```

The function works like this:

1. Find the `.page-content` element.
2. Reset section gap to `0px`.
3. Find all visible direct child sections.
4. Count how many spaces exist between those sections.
5. Measure the natural height of the content block.
6. Compare natural content height with the target height.
7. If the content is shorter than the target, calculate the missing vertical space.
8. Divide the missing space equally between section gaps.
9. Apply the result through `--section-gap`.

Example:

```text
Target height: 900px
Natural content height: 720px
Extra space needed: 180px
Visible sections: 7
Gap slots: 6

Section gap = 180 / 6 = 30px
```

The page receives:

```css
--section-gap: 30px;
```

#### 11. Measuring Natural Content Height

The natural height of the content block is calculated with:

```js
function getNaturalContentHeight(content) {
  const sections = getVisibleSections(content);

  if (!sections.length) {
    return 0;
  }

  const first = sections[0];
  const last = sections[sections.length - 1];

  return (last.offsetTop + last.offsetHeight) - first.offsetTop;
}
```

This measures the distance from the top of the first visible section to the bottom of the last visible section.

It does not simply use `content.scrollHeight`.

This gives a more accurate measurement of the visible content block itself.

#### 12. Minimum 80% A4 Fill Rule

The one-page template has a strict visual fill rule:

```js
const SINGLE_PAGE_MIN_FILL_RATIO = 0.8;
```

The minimum target height is calculated from the full A4 page height:

```js
const a4PageHeightPx = page.clientHeight;
const minimumContentBlockHeight = a4PageHeightPx * SINGLE_PAGE_MIN_FILL_RATIO;
```

This means:

```text
minimumContentBlockHeight = A4 page height × 0.8
```

If the A4 page is approximately `1122px` high in the browser, then:

```text
1122px × 0.8 = 897.6px
```

So the visible content block should occupy at least about `898px`.

If the content naturally occupies less than this, section gaps are increased.

If the content already occupies 80% or more, no artificial spacing is added.

#### 13. Safety Limit for Gaps

The gap calculation includes a small safety limit:

```js
const GAP_SAFETY_PX = 1;
```

The target height is capped:

```js
const safeTargetHeight = Math.min(targetHeightPx, maxUsableHeight - GAP_SAFETY_PX);
```

This prevents dynamic gaps from pushing the content beyond the usable area.

Even if the target height is large, the algorithm will not intentionally create overflow.

#### 14. Full Adaptation Order

The full adaptation process is handled by:

```js
function fitSinglePageResume() {
  if (!pages.length) return;

  resetSpacing();
  pages.forEach(function (page) {
    setScale(page, MIN_SCALE);
  });

  const maxScalesPerPage = pages.map(findMaxScaleForPage);
  const sharedScale = Math.min.apply(null, maxScalesPerPage);

  pages.forEach(function (page) {
    setScale(page, sharedScale);
    page.dataset.fontScale = sharedScale.toFixed(4);
  });

  distributeSinglePageMinimumFill();
}
```

The order is:

```text
1. Reset all dynamic section gaps to 0.
2. Set the page to the minimum font scale.
3. Find the maximum font scale that still fits inside A4.
4. Apply that final scale to the whole page.
5. Only after font scaling, calculate section gaps.
6. If the visible content block is shorter than 80% of A4 height, increase gaps between sections.
7. If the content already occupies at least 80%, leave gaps at zero.
```

This order is the main principle of the solution.

#### 15. When Adaptation Runs

The adaptation runs in three situations.

##### On Page Load

```js
window.addEventListener("load", function () {
  requestAnimationFrame(fitSinglePageResume);
});
```

This runs the layout calculation after the page has loaded.

##### On Browser Resize

```js
window.addEventListener("resize", function () {
  clearTimeout(resizeTimer);
  resizeTimer = setTimeout(function () {
    requestAnimationFrame(fitSinglePageResume);
  }, 100);
});
```

This recalculates the layout when browser rendering dimensions change.

The `100ms` delay prevents excessive recalculations during resizing.

##### Before Printing or PDF Export

```js
window.addEventListener("beforeprint", function () {
  fitSinglePageResume();
});
```

This ensures the final adapted layout is applied before the browser generates a PDF or sends the page to print.

#### 16. Overflow Handling

If content does not fit even at the minimum font scale, the script does not shrink text below the original design.

Instead, it marks the page:

```js
page.dataset.overflowAtMinFont = "true";
```

This means the content is too large for the one-page template under the current rules.

The algorithm will not break the minimum font-size constraint.

In that case, the resume content must be shortened, split, or moved to a multi-page template.

#### 17. Summary of Rules

The adaptive rules are:

```text
1. The resume is fixed to one A4 page: 210mm × 297mm.
2. Original template font sizes are the minimum allowed sizes.
3. Main inherited text starts at 10.5px.
4. Main inherited text may grow up to 16px.
5. Maximum font scale is calculated as 16 / 10.5.
6. All other font sizes scale proportionally from their original values.
7. Dynamic section gaps are removed before measuring font size.
8. The largest fitting font scale is found using binary search.
9. The final font scale is applied to the whole page.
10. Section spacing is calculated only after font scaling is complete.
11. The visible content block must occupy at least 80% of the full A4 page height.
12. If content is shorter than 80%, gaps between sections are increased.
13. If content is already 80% or taller, no extra spacing is added.
14. The algorithm never shrinks text below the original template sizes.
15. If content overflows at minimum size, the page is marked as overflowing.
```

#### 18. General Principle for Another AI

The general idea is:

> Treat the original one-page resume typography as the minimum design. First, measure the fixed A4 page with zero dynamic section spacing. Use binary search to find the largest font scale that fits without overflow, with the main text capped at 16px. Apply that final scale to the whole page. Then, and only then, distribute remaining vertical space through controlled section gaps so that the visible content block occupies at least 80% of the A4 page height.

For this one-page template:

```text
Font rule:
Use one shared font scale for the whole page.

Minimum font rule:
Never go below the original CSS font sizes.

Maximum font rule:
Main inherited text may not exceed 16px.

Spacing rule:
After font scaling, increase only the gaps between major sections.

Minimum fill rule:
The content block should occupy at least 80% of the A4 page height.
```

This produces a one-page resume that adapts to content density while keeping typography predictable and preventing the browser from creating uncontrolled spacing.