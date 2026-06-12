// ResumAIner — Entity-Relationship Data Model
// Format: DBML for dbdiagram.io
// Project ID: resumainer
// File: dbml_erd.md (v4.0)
// Date: 2026-06-12
// Author: Anton
// Status: Approved — MVP Baseline

// ============================================================
// GROUP 1: REFERENCE DATA
// ============================================================
// Lookup tables per 3NF. Reference data is immutable once seeded.

// ----- Section: Auth -----

Table role {
  id integer [primary key]
  code varchar(20) [not null, unique]      // USER, ADMIN
  name varchar(50) [not null]              // User, Admin
}

Table user_status {
  id integer [primary key]
  code varchar(20) [not null, unique]      // ACTIVE, BLOCKED
  name varchar(50) [not null]              // Active, Blocked
}

Table user_permission {
  id integer [primary key]
  code varchar(20) [not null, unique]      // ALLOWED, FORBIDDEN
  name varchar(50) [not null]              // Allowed, Forbidden
}

// ----- Section: Generated AI response -----

Table response_status {
  id integer [primary key]
  code varchar(20) [not null, unique]      // DRAFT, FINALIZED
  name varchar(50) [not null]              // Draft, Finalized
}

// ----- Section: Content -----

Table language {
  id integer [primary key]
  code varchar(10) [not null, unique]      // EN, RU
  name varchar(50) [not null]              // English, Russian
}

// DEC-063: Fixed set of supported generation language modes.
// UI labels: "English only", "Russian only", "Bilingual".
Enum resume_language_mode {
  ENGLISH_ONLY
  RUSSIAN_ONLY
  BILINGUAL
}

Table adaptation_level {
  id integer [primary key]
  code varchar(20) [not null, unique]      // MINIMAL, BALANCED, MAXIMUM
  name varchar(50) [not null]              // Minimal, Balanced, Maximum
  description text
}

// Junction + lookup for multi-select work format preference.
// DEC-022: Normalized approach instead of comma-separated TEXT.
Table work_format {
  id integer [primary key]
  code varchar(30) [not null, unique]      // full-time, part-time, rotational_schedule, internship, offline, remote, hybrid, on_project_site
  name varchar(50) [not null]              // Full-time, Part-time, Remote, etc.
}

// ----- Section: Post-MVP (DDL-ready, single seed record for MVP) -----

// DEC-025: resume_template table for future template selection feature.
// Post-MVP: Users choose template (ATS-friendly, Human-friendly).
// MVP: Single default template seeded.
Table resume_template {
  id integer [primary key]
  name varchar(100) [not null]             // "Default Two-Page Template"
  html_file_path varchar(500) [not null]   // Path to template HTML file
  description text
  is_active boolean [not null, default: true]
  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// ============================================================
// GROUP 2: CORE — User Account & Profile
// ============================================================

// ----- Section: User Account -----

// TABLE NAME: `users` (plural) — PostgreSQL reserves `user` as keyword.
// Java entity can be `User` (different package from Spring Security User).
Table users {
  id integer [primary key]
  username varchar(100) [not null, unique]     // Public URL: /{username}/{public_code}
  email varchar(255) [not null, unique]        // Registration email (NOT exposed on resumes)

  password_hash varchar(255) [not null]        // BCrypt hash

  role_id integer [not null, ref: > role.id]
  status_id integer [not null, ref: > user_status.id]
  permission_id integer [not null, ref: > user_permission.id]

  default_language_id integer [ref: > language.id]
  secondary_language_id integer [ref: > language.id]

  // DEC-024: Privileged users can access hidden (paid/experimental) AI models.
  // Controlled via "Privileged" checkbox in Admin User Details page.
  is_privileged boolean [not null, default: false]

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
  deleted_at timestamp
  is_deleted boolean [not null, default: false]
}

// ----- Section: Profile -----

// DEC-021: full_name, phone, resume_email moved from `users` to `contact_detail`.
// `users` = authentication & account control; `contact_detail` = resume profile data.
// Admin User Details JOINs both tables for complete view.
Table contact_detail {
  id integer [primary key]
  user_id integer [not null, unique, ref: > users.id]  // 1:1 with users

  // Contact info used on resumes
  full_name varchar(255) [not null]
  phone varchar(50) [not null]
  resume_email varchar(255) [not null]         // Email shown on resumes (may differ from account email)

  // Profile contacts
  location varchar(255) [not null]             // "Kazakhstan, Astana"
  professional_title varchar(255)              // "Business Analyst, Junior Java Developer"
  linkedin_url varchar(150)                    // DEC-026: LinkedIn vanity URL max 100 chars
  portfolio_url varchar(500)
  telegram varchar(100)
  whatsapp varchar(50)

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-012: Auto-sort by start_date DESC, end_date DESC NULLS FIRST.
// Sort order managed at query level, not by sort_order column.
Table work_experience {
  id integer [primary key]
  user_id integer [not null, ref: > users.id]

  job_title varchar(255) [not null]
  company_name varchar(255) [not null]
  description text [not null]
  location varchar(255) [not null]

  start_date date [not null]
  end_date date                                 // NULL = current job
  is_current boolean [not null, default: false] // calcuated if no end_date

  // Post-MVP expansion (DEC-027): Company URL for linking in generated resumes.
  // Not used by MVP generation flow. DDL-ready for future phases.
  company_url varchar(500)                     // Post-MVP: company profile link

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-070: Education is profile-owned, not AI-generated, but final resumes can be EN/RU.
// Therefore education stores mandatory bilingual profile fields.
// Resume Review does not edit Education; template rendering selects *_en or *_ru by response language.
Table education {
  id integer [primary key]
  user_id integer [not null, ref: > users.id]

  institution_name_ru varchar(255) [not null]
  institution_name_en varchar(255) [not null]
  degree_ru varchar(100) [not null]              // Bachelor, Master, PhD, etc. as localized free text
  degree_en varchar(100) [not null]              // Bachelor, Master, PhD, etc. as localized free text
  field_of_study_ru varchar(255) [not null]
  field_of_study_en varchar(255) [not null]

  education_type varchar(150)                    // Optional: University, College, Bootcamp, etc.
  description text                               // Optional profile note, not rendered in compact MVP template

  start_date date [not null]                     // Confirmed: required
  end_date date                                  // NULL = still studying; allows future planned graduation

  location varchar(255)
  gpa_grade varchar(20)                          // Flexible text format: 3.75, A, 4.8/5, etc.

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

Table project {
  id integer [primary key]
  user_id integer [not null, ref: > users.id]

  project_name varchar(255) [not null]
  role varchar(255)                            // "Developer", "Lead"; defaults to "Participant" at code level if empty (DEC-031)
  description text [not null]
  location varchar(255)

  start_date date [not null]
  end_date date                                // NULL = still ongoing; allows future dates (planned end)
  project_url varchar(500)

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-018: Course & certificate section is mandatory for MVP.
// DEC-019: Two-page template limits courses per page.
//   Page 1: max 7 most relevant courses.
//   Page 2: if 5+ work_exp → max 5 courses; if <5 work_exp → max 8 courses.
// Application logic — no DB column needed for page placement.
Table course_certificate {
  id integer [primary key]
  user_id integer [not null, ref: > users.id]

  name varchar(255) [not null]
  provider varchar(255) [not null]             // "Coursera", "Udemy"
  description text
  course_focus varchar(255)                    // Optional: skills/topics covered (user input, not AI-generated)

  start_date date [not null]
  end_date date                                // NULL = in progress
  credential_url varchar(500)

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-013: Simplified Additional Info for MVP.
// Single table replaces 7 normalized tables (skill, value, achievement, hobby,
// positioning, professional_aspiration, user_language).
// Future Post-MVP refinement: can migrate to normalized per-type tables.
Table additional_profile_info {
  id integer [primary key]
  user_id integer [not null, unique, ref: > users.id]  // 1:1 with users

  // Free-text fields (DEC-013 simplification)
  skills text                                  // "Business Analysis, Java, Python, SQL"
  languages text                               // "English C1, Russian Native"
  professional_aspirations text                // Target career direction
  achievements text
  general_information text                     // AI context for generation

  // Resume language preferences — FK to language table (DEC-029)
  default_resume_language_id integer [ref: > language.id]
  additional_resume_language_id integer [ref: > language.id]

  // Relocation & travel
  ready_for_relocation varchar(20)             // "Yes", "No", "Not specified"
  ready_for_business_trips varchar(20)         // "Yes", "No", "Not specified"

  // Personal
  date_of_birth date  [not null]
  citizenship varchar(150) [not null]          // Optional: user's citizenship
  photo_file_path varchar(500)                 // Optional per confirmed requirement

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-022: Junction table for many-to-many: users ↔ work_format.
// Enables querying by format (e.g., "find remote users") and follows 3NF.
Table user_work_format {
  id integer [primary key]
  user_id integer [not null, ref: > users.id]
  work_format_id integer [not null, ref: > work_format.id]

  indexes {
    (user_id, work_format_id) [unique]
  }
}

// ============================================================
// GROUP 3: GENERATION — AI Models, Generation Pipeline, Saved Resumes
// ============================================================

// ----- Section: AI Models -----

// DEC-024: is_hidden + is_privileged control access to paid/experimental models.
// Admin manages through AI Model Details page: Visibility dropdown (Visible/Hidden).
Table ai_model {
  id integer [primary key]

  provider varchar(255) [not null]             // "OpenRouter"
  model_code varchar(255) [not null]           // "deepseek/deepseek-v4-pro"
  display_name varchar(255) [not null]         // "Deepseek v4 Pro"
  provider_api_url varchar(500) [not null]     // Base URL for API calls

  // NFR-001: API key encrypted, never logged, masked in UI after saving
  api_key_encrypted varchar(512) [not null]    // Encrypted, not plaintext

  is_active boolean [not null, default: true]
  is_paid boolean [not null, default: false]   // Marks paid models for admin awareness
  is_hidden boolean [not null, default: false] // Hidden from non-privileged users (DEC-024)

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// ----- Section: AI Prompt Configs -----

// DEC-064: AI prompt configuration is stored as a versioned prompt bundle.
// Backend assembles the final prompt from modular fragments:
// system prompt + language mode + adaptation selection + cover letter rule.
// Only one prompt config should be active at a time in production.
Table ai_prompt_config {
  id integer [primary key]

  name varchar(255) [not null]                  // Example: "Default Resume Generation Prompt Set v1"
  description text                              // Admin-facing explanation of this prompt config
  is_active boolean [not null, default: false]  // Only one active config at a time

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-065: System prompt stores stable global rules that do not depend on user settings.
// Examples: role of the model, strict JSON-only output, no hallucination,
// sourceId preservation, tone constraints, safety rules.
// There should be exactly one system prompt per ai_prompt_config.
Table ai_system_prompt {
  id integer [primary key]
  prompt_config_id integer [not null, ref: > ai_prompt_config.id]

  prompt text [not null]                        // Global system prompt text

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-066: Language prompt stores only language-mode-specific instructions.
// It prevents duplication of bilingual/single-language rules across many full prompts.
// Expected active rows per config: ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL.
Table ai_request_prompt_language {
  id integer [primary key]
  prompt_config_id integer [not null, ref: > ai_prompt_config.id]

  language_mode varchar(50) [not null]          // ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL
  prompt text [not null]                        // Language-specific output and JSON structure rules

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-067: Adaptation prompt stores only adaptation-selection-specific instructions.
// ALL is a request selection, not a final saved resume level.
// If adaptation_selection = ALL, AI must return Minimal, Balanced, and Maximum variants.
Table ai_request_prompt_adaptation {
  id integer [primary key]
  prompt_config_id integer [not null, ref: > ai_prompt_config.id]

  adaptation_selection varchar(50) [not null]   // MINIMAL, BALANCED, MAXIMUM, ALL
  prompt text [not null]                        // Adaptation-level instruction fragment

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-068: Cover letter prompt stores cover-letter-specific generation rules.
// Two rows per config are recommended:
// include_cover_letter = true  -> require cover letter text
// include_cover_letter = false -> explicitly forbid/return null for cover letter
// This prevents the model from generating a cover letter when the user disabled it.
Table ai_request_prompt_cover_letter {
  id integer [primary key]
  prompt_config_id integer [not null, ref: > ai_prompt_config.id]

  include_cover_letter boolean [not null]       // true = generate cover letter; false = do not generate
  prompt text [not null]                        // Cover-letter instruction fragment

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-069: Prompt render log stores the exact prompt assembled for a generation request.
// This is useful for debugging, reproducibility, QA, and later comparison of AI results.
// For production, access to this table must be restricted because rendered prompts may contain profile data.
Table ai_prompt_render_log {
  id integer [primary key]
  generation_request_id integer [not null, ref: > resume_generation_request.id]
  prompt_config_id integer [not null, ref: > ai_prompt_config.id]

  system_prompt_rendered text [not null]        // Final system prompt sent to AI
  request_prompt_rendered text [not null]       // Final user/request prompt sent to AI
  profile_payload_json text                     // Optional: profile/vacancy payload used for prompt rendering
  prompt_hash varchar(128)                      // Optional checksum for debugging and reproducibility

  created_at timestamp [not null, default: `now()`]
}

// ----- Section: Resume Budget Configuration -----

// DB-backed configuration for resume template budgets.
// Replaces YAML-based external configuration.
// Only one active config should exist — enforce with partial unique index in PostgreSQL migration.
Table resume_budget_configs {
  id bigint [pk, increment]
  name varchar(100) [not null]
  version_no int [not null, default: 1]
  is_active boolean [not null, default: false]
  description text
  created_at timestamp [not null]
  updated_at timestamp [not null]

  Note: 'Only one active config should exist. Enforce with partial unique index in PostgreSQL migration.'
}

// General scalar configuration values (key-value pattern with typed columns).
// value_type: int, boolean, text. Only one value column used per row based on value_type.
Table resume_template_selection_rules {
  id bigint [pk, increment]
  config_id bigint [not null, ref: > resume_budget_configs.id]

  rule_key varchar(100) [not null]
  value_type varchar(20) [not null] // int, boolean, text

  int_value int
  boolean_value boolean
  text_value varchar(255)

  description text
  created_at timestamp [not null]
  updated_at timestamp [not null]

  indexes {
    (config_id, rule_key) [unique]
  }
}

// Work experience distribution rules per edge case.
// Each rule maps a job/project/course profile to a template mode and page distribution.
Table resume_work_experience_distribution_rules {
  id bigint [pk, increment]
  config_id bigint [not null, ref: > resume_budget_configs.id]

  case_key varchar(20) [not null] // EC-001, EC-003, EC-010, etc.
  min_total_jobs int [not null]
  max_total_jobs int [not null]
  min_projects int [not null, default: 0]
  max_projects int // null means unlimited
  require_no_courses boolean [not null, default: false]

  template_mode varchar(20) [not null] // one_page, two_page
  page1_jobs int [not null]
  page2_jobs int [not null, default: 0]
  page2_max_additional_jobs int

  priority int [not null, default: 100]
  notes text
  created_at timestamp [not null]
  updated_at timestamp [not null]

  indexes {
    (config_id, case_key) [unique]
  }
}

// Section-level budget rules defining min/max values for content metrics.
// Each row defines a budget for one section/profile/metric combination.
Table resume_section_budget_rules {
  id bigint [pk, increment]
  config_id bigint [not null, ref: > resume_budget_configs.id]

  section_key varchar(100) [not null] // professional_summary, skills, courses, projects
  profile_key varchar(100) [not null] // light, medium, dense, one_page_light, etc.
  metric_key varchar(100) [not null] // sentences, bullet_points, max_courses, words_per_skill

  min_value int
  max_value int

  notes text
  created_at timestamp [not null]
  updated_at timestamp [not null]

  indexes {
    (config_id, section_key, profile_key, metric_key) [unique]
  }
}

// ----- Section: Generation Pipeline -----
// Flow: request → AI response (DRAFT) → user review/edit → FINALIZED → saved_resume PDF

// DEC-020: Persists all input parameters for traceability.
// Each user can have many generation requests (different vacancies, settings).
Table resume_generation_request {
  id integer [primary key]

  user_id integer [not null, ref: > users.id]
  ai_model_id integer [not null, ref: > ai_model.id]
  prompt_config_id integer [ref: > ai_prompt_config.id]  // save id of used prompt configs

  // Generation input
  vacancy_title text [not null]                 // Pasted vacancy title
  vacancy_description text [not null]           // Pasted vacancy text
  company_name text                             // Pasted company name
  company_description text                      // Pasted company text
  additional_comments text                      // "Focus on my courses"
  include_cover_letter boolean [not null, default: false]

  // Generation parameters
  language_mode resume_language_mode [not null]
  // Values: ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL
  // UI labels: "English only", "Russian only", "Bilingual"
  // DEC-063: language choice belongs to the request as a mode, not as a single language FK.
  // A single request can produce one or two language-specific responses.
  adaptation_level_id integer [not null, ref: > adaptation_level.id]

  // Budget config used for generation (DB-backed budget configuration)
  budget_config_id bigint [ref: > resume_budget_configs.id]
  budget_config_version_used int

  // Processing state
  status varchar(30) [not null, default: 'pending']
  // pending → processing → completed | failed
  error_message text

  created_at timestamp [not null, default: `now()`]
  completed_at timestamp
}

// DEC-028: Stores the AI generation output and user-reviewed edits.
// status_id = DRAFT (AI returned, not yet reviewed) or FINALIZED (user edited & approved).
// Single-value fields live here; multi-value sections go to response_* tables.
// cover_letter is stored in response (DEC-016) — AI-generated output, logically part of response, not saved_resume.
Table resume_generation_response {
  id integer [primary key]

  generation_request_id integer [not null, ref: > resume_generation_request.id]
  // DEC-063: many-to-one. One request creates one response for single-language mode
  // or two responses for BILINGUAL mode.

  language_id integer [not null, ref: > language.id]
  // Actual language of this response row: EN or RU.
  // Kept here to preserve 3NF: language is a property of the response, not of a bilingual request.

  status_id integer [not null, ref: > response_status.id]
  // DRAFT = AI output, waiting for user review
  // FINALIZED = user reviewed and approved; ready for PDF generation

  // Top-level single-value fields (reviewed/edited by user)
  professional_title varchar(250) [not null]
  value_line varchar(500)                      // DEC-063: AI-generated positioning / keyword line
  professional_summary text [not null]
  professional_aspirations text [not null]
  cover_letter text                            // DEC-016: Final edited cover letter

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp

  indexes {
    (generation_request_id, language_id) [unique]
  }  
}

// DEC-028: Reviewed/edited work experience items from the generation response.
// Each row = one work experience entry edited by user on Resume Review page.
// order_in_resume = fixed display sequence (DEC-030, not user-reorderable).
// is_first_page = true → primary experience on page 1; false → "Additional work experience" on page 2.
Table generation_response_experience {
  id integer [primary key]
  response_id integer [not null, ref: > resume_generation_response.id]  // many-to-one (N:1)

  job_title varchar(255) [not null]
  company_name varchar(255) [not null]
  description text [not null]
  location varchar(255) [not null]
  is_first_page boolean [not null, default: true]  // DEC-030: page 1 (primary) or page 2 (additional)
  start_date date [not null]                       // Experience without start date has no resume value
  end_date date                                    // NULL = current
  order_in_resume integer [not null, default: 0]   // Fixed display order (DEC-030)

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-028: Reviewed/edited education items.
// Compact format in resume template — description field not needed (DEC-030).
Table generation_response_education {
  id integer [primary key]
  response_id integer [not null, ref: > resume_generation_response.id]

  institution_name varchar(255) [not null]
  degree varchar(100) [not null]
  field_of_study varchar(255) [not null]
  start_date date [not null]
  end_date date
  location varchar(255)
  gpa_grade varchar(20)
  order_in_resume integer [not null, default: 0]   // Fixed display order (DEC-030)

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-028: Reviewed/edited course items.
// Compact format in resume template — description field not needed (DEC-030).
// is_first_page = true → courses on page 1; false → additional courses on page 2.
Table generation_response_course {
  id integer [primary key]
  response_id integer [not null, ref: > resume_generation_response.id]

  name varchar(255) [not null]
  provider varchar(255) [not null]
  is_first_page boolean [not null, default: true]  // DEC-030: page 1 (primary) or page 2 (additional)
  course_focus varchar(255)
  order_in_resume integer [not null, default: 0]   // Fixed display order (DEC-030)

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-028: Reviewed/edited project items.
// start_date is required — project without start date has no resume value.
Table generation_response_project {
  id integer [primary key]
  response_id integer [not null, ref: > resume_generation_response.id]

  project_name varchar(255) [not null]
  role varchar(255)
  description text [not null]
  location varchar(255)
  start_date date [not null]
  end_date date
  order_in_resume integer [not null, default: 0]   // Fixed display order (DEC-030)

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// Skill groups with individual skill names within each group.
// Example: {"Leadership": ["Team Leadership", "Process Improvement"]},
//          {"Reporting": ["Performance Reporting", "Agile Frameworks"]}
Table generation_response_skill {
  id integer [primary key]
  response_id integer [not null, ref: > resume_generation_response.id]

  skill_group varchar(255) [not null]          // "Leadership", "Reporting"
  skill_name varchar(255) [not null]           // "Team Leadership", "Performance Reporting"
  order_in_resume integer [not null, default: 0]   // Fixed display order (DEC-030)

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// DEC-071: Reviewed/edited Personal Information generated per response language/adaptation.
// Profile stores raw/default personal data once, but AI returns localized resume-ready values.
// This table gives users editable intermediate values before final HTML/PDF generation.
Table generation_response_personal {
  id integer [primary key]
  response_id integer [not null, unique, ref: > resume_generation_response.id]

  location varchar(255) [not null]
  spoken_languages varchar(100) [not null]
  willingness_to_relocate varchar(50) [not null]
  willingness_for_business_trips varchar(50) [not null]
  citizenship varchar(50) [not null]
  date_of_birth date [not null]
  work_formats varchar(255)
  gpa_grade varchar(20)
  order_in_resume integer [not null, default: 0]

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp
}

// ----- Section: Saved Resumes -----

// DEC-028: Finalized resume record. Stores metadata and path to generated PDF.
// pdf_file_path replaces the separate pdf_file table for MVP simplicity.
// All resumes are public by design — no is_public flag needed (DEC-028).
// Top-level text fields (professional_summary, professional_aspirations, cover_letter)
// live in resume_generation_response table.
// DEC-073: Filled HTML is persisted before PDF conversion.
// Java backend renders and saves HTML first, then calls HtmlToPdfConverter.
// Both HTML and PDF paths are stored for traceability and future download actions.
Table saved_resume {
  id integer [primary key]

  user_id integer [not null, ref: > users.id]
  generation_request_id integer [not null, ref: > resume_generation_request.id]
  response_id integer [not null, ref: > resume_generation_response.id]
  template_id integer [ref: > resume_template.id]  // Post-MVP: template selection

  adaptation_level_id integer [not null, ref: > adaptation_level.id]
  language_id integer [not null, ref: > language.id]

  title varchar(255) [not null]
  public_code varchar(4) [not null]            // 4-char: QWRYUPASEDFGHJKZXCVBNM
  public_url_link varchar(200) [not null]      // Full ready-made public resume URL

  // Path to generated PDF and HTML files on server
  html_file_path varchar(500) [not null]       // DEC-073: HTML shoud be stored and be downloadable
  pdf_file_path varchar(500) [not null]        // DEC-017: HTML-to-PDF on Java backend

  is_deleted boolean [not null, default: false]
  deleted_at timestamp

  created_at timestamp [not null, default: `now()`]
  updated_at timestamp

  indexes {
    (user_id, public_code) [unique]
    (user_id)
    (created_at)
  }
}

// ============================================================
// GROUP 4: MONITORING — Usage tracking
// ============================================================

// One row per API call. Powers "Total tokens sent/generated" on User Home
// and Admin Home dashboards, plus per-model stats in AI Models table.
Table ai_usage_log {
  id integer [primary key]

  user_id integer [not null, ref: > users.id]
  ai_model_id integer [not null, ref: > ai_model.id]
  generation_request_id integer [not null, ref: > resume_generation_request.id]

  tokens_sent integer [not null, default: 0]          // Prompt tokens
  tokens_generated integer [not null, default: 0]     // Completion tokens
  cost decimal [note: 'Post-MVP: cost per request']

  created_at timestamp [not null, default: `now()`]   // When API call completed
}

// DEC-063: One API call may create one or two response rows.
// Junction table avoids duplicating usage-log facts and keeps monitoring in 3NF.
Table ai_usage_log_response {
  id integer [primary key]

  ai_usage_log_id integer [not null, ref: > ai_usage_log.id]
  generation_response_id integer [not null, ref: > resume_generation_response.id]

  indexes {
    (ai_usage_log_id, generation_response_id) [unique]
  }
}

// ============================================================
// RELATIONSHIPS OVERVIEW
// ============================================================

// === REFERENCE DATA (no FK into them, used as lookup) ===
// role, user_status, user_permission, response_status, language,
// adaptation_level, work_format, resume_template

// === CORE — User ===
// users → role:                   many-to-one
// users → user_status:            many-to-one
// users → user_permission:        many-to-one
// users → language:               many-to-one (default), many-to-one (secondary)

// === CORE — Profile ===
// users → contact_detail:          one-to-one
// users → work_experience:         one-to-many
// users → education:               one-to-many
// users → project:                 one-to-many
// users → course_certificate:      one-to-many
// users → additional_profile_info: one-to-one
// users → user_work_format:        one-to-many
// work_format → user_work_format:  one-to-many

// === GENERATION ===
// users → resume_generation_request:       one-to-many
// ai_model → resume_generation_request:    one-to-many
// resume_generation_request → resume_generation_response: one-to-many (1 response for ENGLISH_ONLY/RUSSIAN_ONLY, 2 responses for BILINGUAL)
// response_status → resume_generation_response: many-to-one
// language → resume_generation_response: many-to-one
// resume_generation_response → generation_response_experience: one-to-many
// resume_generation_response → generation_response_education: one-to-many
// resume_generation_response → generation_response_course: one-to-many
// resume_generation_response → generation_response_project: one-to-many
// resume_generation_response → generation_response_skill: one-to-many

// === BUDGET CONFIG ===
// resume_budget_configs → resume_template_selection_rules: one-to-many
// resume_budget_configs → resume_work_experience_distribution_rules: one-to-many
// resume_budget_configs → resume_section_budget_rules: one-to-many

// === SAVED ===
// users → saved_resume:              one-to-many
// resume_generation_request → saved_resume: one-to-many
// resume_generation_response → saved_resume: one-to-one
// resume_template → saved_resume:    one-to-many (Post-MVP)

// === MONITORING ===
// users → ai_usage_log:              one-to-many
// ai_model → ai_usage_log:           one-to-many
// resume_generation_request → ai_usage_log: one-to-many
// ai_usage_log → ai_usage_log_response: one-to-many
// resume_generation_response → ai_usage_log_response: one-to-many

// ============================================================
// TOTAL TABLE COUNT: 31 tables
// ============================================================
// Reference Data (8):     role, user_status, user_permission, response_status,
//                          language, adaptation_level, work_format, resume_template
// Core — User (1):        users
// Core — Profile (7):     contact_detail, work_experience, education, project,
//                          course_certificate, additional_profile_info, user_work_format
// Generation — Models (1): ai_model
// Generation — Pipeline (7): resume_generation_request, resume_generation_response,
//                          generation_response_experience, generation_response_education,
//                          generation_response_course, generation_response_project,
//                          generation_response_skill
// Budget Config (4):     resume_budget_configs, resume_template_selection_rules,
//                        resume_work_experience_distribution_rules, resume_section_budget_rules
// Saved (1):              saved_resume
// Monitoring (2):         ai_usage_log, ai_usage_log_response

// ============================================================
// KEY CHANGES FROM v0.1 TO v0.2
// ============================================================
//
// 1. Structure: Flat groups → Group > Section hierarchy
// 2. adaptation_level moved to Reference Data / Content (not User & Auth)
// 3. contact_detail: added full_name, phone, resume_email (DEC-021)
// 4. users: removed full_name, phone, resume_email; added is_privileged (DEC-024)
// 5. linkedin_url: varchar(500) → varchar(150) (DEC-026)
// 6. work_experience.company_url: marked Post-MVP (DEC-027)
// 7. education: added education_type (optional), end_date allows future dates
// 8. project.end_date: allows future dates
// 9. course_certificate: added course_focus (optional, user input)
// 10. additional_profile_info: added citizenship; removed preferred_work_format
// 11. work_format + user_work_format: junction tables (DEC-022)
// 12. response_status: lookup table (DRAFT, FINALIZED)
// 13. resume_generation_response: new table with status_id
// 14. generation_response_*: 5 section-level tables (DEC-028)
// 15. saved_resume: removed final_content, added professional_summary,
//     professional_aspirations, cover_letter, pdf_file_path, response_id,
//     template_id (Post-MVP)
// 16. pdf_file table removed → pdf_file_path in saved_resume
// 17. ai_model: added is_paid, is_hidden (DEC-024)
// 18. resume_template: new Post-MVP ready table (DEC-025)

// ============================================================
// KEY CHANGES FROM v0.2 TO v1.0
// ============================================================
//
// 1. resume_template: added updated_at timestamp for consistency
// 2. project.role: default value "Participant" at code level if empty (DEC-031)
// 3. additional_profile_info: resume language fields changed from varchar → FK to language table (DEC-029)
// 4. generation_response_experience: start_date made NOT NULL; sort_order renamed to order_in_resume (DEC-030)
// 5. generation_response_education: removed description field (compact template); order_in_resume (DEC-030)
// 6. generation_response_course: removed description field (compact template); order_in_resume (DEC-030)
// 7. generation_response_experience + generation_response_course: added is_first_page for two-page template (DEC-030)
// 8. generation_response_project: added updated_at; start_date made NOT NULL
// 9. generation_response_skill: added updated_at for data consistency; order_in_resume (DEC-030)
// 10. generation_response_education: added updated_at for data consistency
// 11. saved_resume: removed is_public (all resumes public by design); removed professional_summary,
//     professional_aspirations (live in resume_generation_response instead)
// 12. cover_letter: moved from saved_resume to resume_generation_response (DEC-028)
// 13. ai_usage_log: added generation_response_id FK
// 14. ai_usage_log.generation_rsponse_id → generation_response_id (typo fix)
// 15. response_status section relabeled to "Generated AI response" for clarity

// ============================================================
// KEY CHANGES FROM v1.0 TO v1.1
// ============================================================
//
// 1. saved_resume: added public_url_link varchar(200) for storing ready-made public resume URL (DEC-032)

// ============================================================
// KEY CHANGES FROM v1.1 TO v2.0
// ============================================================
//
// 1. YAML-based budget configuration replaced with DB-backed budget configuration.
// 2. New table: resume_budget_configs — config identity and version metadata.
// 3. New table: resume_template_selection_rules — general scalar config values.
// 4. New table: resume_work_experience_distribution_rules — edge case distribution rules.
// 5. New table: resume_section_budget_rules — section-level min/max budget rules.
// 6. resume_generation_request: added budget_config_id, budget_config_version_used.
// 7. Total table count: 25 → 30.

// ============================================================
// KEY CHANGES FROM v2.0 TO v3.0
// ============================================================
//
// 1. Added enum resume_language_mode with values ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL.
//    UI labels: "English only", "Russian only", "Bilingual".
// 2. resume_generation_request.language_id removed.
//    Reason: a request can now represent one-language or bilingual generation.
// 3. resume_generation_request.language_mode changed from varchar/default/additional/both
//    to fixed enum resume_language_mode.
// 4. resume_generation_response.language_id added.
//    Reason: actual generated language is a property of each response row.
// 5. resume_generation_response.generation_request_id is no longer unique.
//    Instead, unique index is now (generation_request_id, language_id).
// 6. One resume_generation_request can now have one or two resume_generation_response rows.
//    ENGLISH_ONLY/RUSSIAN_ONLY create one response; BILINGUAL creates two responses.
// 7. resume_generation_response.value_line added for AI-generated resume positioning line.
// 8. ai_usage_log.generation_response_id removed.
//    Reason: one API call may produce two response rows in bilingual mode.
// 9. ai_usage_log_response junction table added to keep API usage tracking normalized and 3NF-compliant.
// 10. Relationships overview and total table count updated: 30 → 31.

// ============================================================
// KEY CHANGES FROM v3.0 TO v4.0
// ============================================================
//
// 1. Added DB-backed modular AI prompt configuration (DEC-064).
//    Instead of storing 16 duplicated full request prompts, backend now builds
//    the final prompt from reusable prompt fragments.
//
// 2. New table: ai_prompt_config.
//    Stores versioned prompt bundle metadata and controls which prompt set is active.
//
// 3. New table: ai_system_prompt.
//    Stores stable global system prompt rules that do not depend on generation settings.
//
// 4. New table: ai_request_prompt_language.
//    Stores language-mode-specific prompt fragments for ENGLISH_ONLY,
//    RUSSIAN_ONLY, and BILINGUAL generation modes.
//
// 5. New table: ai_request_prompt_adaptation.
//    Stores adaptation-specific prompt fragments for MINIMAL, BALANCED,
//    MAXIMUM, and ALL generation scenarios.
//
// 6. New table: ai_request_prompt_cover_letter.
//    Stores cover-letter-specific prompt fragments for include_cover_letter = true/false.
//    This prevents the AI model from generating a cover letter when the user disabled it.
//
// 7. New table: ai_prompt_render_log.
//    Stores final rendered system/request prompts for debugging, QA,
//    reproducibility, and future AI result comparison.
//
// 8. resume_generation_request: added prompt_config_id FK to ai_prompt_config.
//    Reason: each generation request must be traceable to the exact prompt bundle
//    used at generation time.
//
// 9. Education model updated to bilingual profile-owned fields (DEC-070).
//    institution_name, degree, and field_of_study were replaced by mandatory
//    RU/EN pairs: institution_name_ru/en, degree_ru/en, field_of_study_ru/en.
//    Reason: Education is not AI-generated/reviewed, but final resumes can be
//    rendered in Russian or English.
//
// 10. generation_response_personal table added (DEC-071).
//     Personal Information is now AI-generated/localized per response language
//     and available for user review/edit before final HTML/PDF generation.
//
// 11. Resume Review flow updated conceptually (DEC-072).
//     Personal Information is added as the last Review section after Skills.
//     Education remains outside Review because it is now bilingual profile-owned data.
//
// 12. saved_resume: added html_file_path (DEC-073).
//     Filled HTML is now persisted before PDF conversion.
//     Java backend will render and save HTML first, then call HtmlToPdfConverter.
//
// 13. Generated artifact storage convention introduced (DEC-073).
//     Final generated files should be stored under:
//     generated_results/{username}/{public_code}/
//     Each saved resume language version has its own public_code and folder.
//
// 14. Export flow expanded conceptually to support HTML download.
//     In addition to PDF download/open actions, frontend should expose
//     "Download HTML" for saved filled HTML files.
//
// 15. Relationships overview updated:
//     ai_prompt_config → ai_system_prompt: one-to-one
//     ai_prompt_config → ai_request_prompt_language: one-to-many
//     ai_prompt_config → ai_request_prompt_adaptation: one-to-many
//     ai_prompt_config → ai_request_prompt_cover_letter: one-to-many
//     ai_prompt_config → ai_prompt_render_log: one-to-many
//     resume_generation_request → ai_prompt_render_log: one-to-many
//     resume_generation_response → generation_response_personal: one-to-one
//
// 16. Total table count updated: 31 → 38.
//     Added 7 tables:
//     ai_prompt_config, ai_system_prompt, ai_request_prompt_language,
//     ai_request_prompt_adaptation, ai_request_prompt_cover_letter,
//     ai_prompt_render_log, generation_response_personal.