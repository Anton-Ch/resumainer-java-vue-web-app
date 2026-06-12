PRAGMA foreign_keys = ON;

-- ============================================================
-- ResumAIner backend prototype v3 SQLite schema
-- Purpose: executable reference for future Java/PostgreSQL implementation.
-- ============================================================

DROP TABLE IF EXISTS saved_resume;
DROP TABLE IF EXISTS generation_response_skill;
DROP TABLE IF EXISTS generation_response_skill_group;
DROP TABLE IF EXISTS generation_response_personal;
DROP TABLE IF EXISTS generation_response_project_bullet;
DROP TABLE IF EXISTS generation_response_project;
DROP TABLE IF EXISTS generation_response_course;
DROP TABLE IF EXISTS generation_response_experience_bullet;
DROP TABLE IF EXISTS generation_response_experience;
DROP TABLE IF EXISTS resume_generation_response;
DROP TABLE IF EXISTS ai_prompt_render_log;
DROP TABLE IF EXISTS resume_generation_request;
DROP TABLE IF EXISTS ai_request_prompt_cover_letter;
DROP TABLE IF EXISTS ai_request_prompt_adaptation;
DROP TABLE IF EXISTS ai_request_prompt_language;
DROP TABLE IF EXISTS ai_system_prompt;
DROP TABLE IF EXISTS ai_prompt_config;
DROP TABLE IF EXISTS ai_model;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS course_certificate;
DROP TABLE IF EXISTS education;
DROP TABLE IF EXISTS work_experience;
DROP TABLE IF EXISTS additional_profile_info;
DROP TABLE IF EXISTS user_work_format;
DROP TABLE IF EXISTS contact_detail;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS work_format;
DROP TABLE IF EXISTS adaptation_level;
DROP TABLE IF EXISTS language;
DROP TABLE IF EXISTS user_generation_permission;
DROP TABLE IF EXISTS user_status;
DROP TABLE IF EXISTS role;

-- ----- Lookup tables -----
CREATE TABLE role (
  id INTEGER PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL
);

CREATE TABLE user_status (
  id INTEGER PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL
);

CREATE TABLE user_generation_permission (
  id INTEGER PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL
);

CREATE TABLE language (
  id INTEGER PRIMARY KEY,
  code TEXT NOT NULL UNIQUE, -- EN, RU
  name TEXT NOT NULL
);

CREATE TABLE adaptation_level (
  id INTEGER PRIMARY KEY,
  code TEXT NOT NULL UNIQUE, -- MINIMAL, BALANCED, MAXIMUM
  name TEXT NOT NULL
);

-- DEC-022: Normalized work format reference data.
-- User selections are stored through user_work_format junction table, not as comma-separated profile text.
CREATE TABLE work_format (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  code TEXT NOT NULL UNIQUE, -- FULL_TIME, HYBRID, REMOTE, etc.
  name_en TEXT NOT NULL,
  name_ru TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

-- ----- User profile tables -----
CREATE TABLE users (
  id TEXT PRIMARY KEY, -- UUID in Java/PostgreSQL
  username TEXT NOT NULL UNIQUE,
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role_id INTEGER NOT NULL REFERENCES role(id),
  status_id INTEGER NOT NULL REFERENCES user_status(id),
  permission_id INTEGER NOT NULL REFERENCES user_generation_permission(id),
  default_language_id INTEGER REFERENCES language(id),
  secondary_language_id INTEGER REFERENCES language(id),
  is_privileged BOOLEAN NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

CREATE TABLE contact_detail (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  full_name TEXT NOT NULL,
  phone TEXT NOT NULL,
  resume_email TEXT NOT NULL,
  location TEXT NOT NULL,
  professional_title TEXT,
  linkedin_url TEXT,
  portfolio_url TEXT,
  telegram TEXT,
  whatsapp TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

CREATE TABLE additional_profile_info (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  skills TEXT,
  languages TEXT,
  professional_aspirations TEXT,
  achievements TEXT,
  general_information TEXT,
  default_resume_language_id INTEGER REFERENCES language(id),
  additional_resume_language_id INTEGER REFERENCES language(id),
  ready_for_relocation BOOLEAN NOT NULL DEFAULT 0,
  ready_for_business_trips BOOLEAN NOT NULL DEFAULT 0,
  date_of_birth TEXT,
  citizenship TEXT,
  photo_file_path TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

-- DEC-022: M:N relationship between users and preferred/acceptable work formats.
-- Keeps profile data normalized and lets PromptBuilder pass localized display names to AI.
CREATE TABLE user_work_format (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  work_format_id INTEGER NOT NULL REFERENCES work_format(id),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, work_format_id)
);

CREATE TABLE work_experience (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  source_id TEXT NOT NULL,
  job_title TEXT NOT NULL,
  company_name TEXT NOT NULL,
  location TEXT,
  start_date TEXT NOT NULL,
  end_date TEXT,
  is_current BOOLEAN NOT NULL DEFAULT 0,
  description TEXT NOT NULL,
  company_url TEXT,
  display_order INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(user_id, source_id)
);

CREATE TABLE education (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  source_id TEXT NOT NULL,

  -- DEC-070: Bilingual education profile fields are required because Education is not AI-generated/reviewed.
  institution_name_ru TEXT NOT NULL,
  institution_name_en TEXT NOT NULL,
  degree_ru TEXT NOT NULL,
  degree_en TEXT NOT NULL,
  field_of_study_ru TEXT NOT NULL,
  field_of_study_en TEXT NOT NULL,

  education_type TEXT,
  description TEXT,
  start_date TEXT NOT NULL,
  end_date TEXT,
  location TEXT,
  gpa_grade TEXT,
  display_order INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(user_id, source_id)
);

CREATE TABLE course_certificate (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  source_id TEXT NOT NULL,
  name TEXT NOT NULL,
  provider TEXT NOT NULL,
  start_date TEXT,
  end_date TEXT,
  credential_url TEXT,
  skills_topics TEXT,
  description TEXT,
  display_order INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(user_id, source_id)
);

CREATE TABLE project (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  source_id TEXT NOT NULL,
  project_name TEXT NOT NULL,
  role TEXT,
  start_date TEXT,
  end_date TEXT,
  description TEXT NOT NULL,
  project_url TEXT,
  display_order INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(user_id, source_id)
);

-- ----- AI Models and configs -----
CREATE TABLE ai_model (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  provider TEXT NOT NULL,              -- OpenRouter
  model_code TEXT NOT NULL,            -- user can update directly in SQLite DB
  display_name TEXT NOT NULL,
  provider_api_url TEXT NOT NULL,
  api_key_encrypted TEXT NOT NULL,     -- prototype stores placeholder in DB; Java must encrypt
  is_active BOOLEAN NOT NULL DEFAULT 1,
  is_paid BOOLEAN NOT NULL DEFAULT 0,
  is_hidden BOOLEAN NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(provider, model_code)
);

-- ----- Section: AI Prompt Configs -----
-- DEC-064: AI prompt configuration is stored as a versioned prompt bundle,
-- not as 16 duplicated request prompts. Backend assembles final prompt from
-- modular fragments: system + language mode + adaptation selection + cover letter rule.
CREATE TABLE ai_prompt_config (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  description TEXT,
  is_active BOOLEAN NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

-- SQLite cannot express PostgreSQL partial unique index exactly in a portable way inside DBML.
-- Java/PostgreSQL Flyway should add:
-- CREATE UNIQUE INDEX uq_ai_prompt_config_only_one_active ON ai_prompt_config (is_active) WHERE is_active = true;
CREATE UNIQUE INDEX uq_ai_prompt_config_one_active_sqlite
ON ai_prompt_config(is_active)
WHERE is_active = 1;

-- DEC-065: stable global system prompt, one per prompt config.
CREATE TABLE ai_system_prompt (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  prompt_config_id INTEGER NOT NULL REFERENCES ai_prompt_config(id) ON DELETE CASCADE,
  prompt TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(prompt_config_id)
);

-- DEC-066: language-mode-specific prompt fragments.
CREATE TABLE ai_request_prompt_language (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  prompt_config_id INTEGER NOT NULL REFERENCES ai_prompt_config(id) ON DELETE CASCADE,
  language_mode TEXT NOT NULL, -- ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL
  prompt TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(prompt_config_id, language_mode)
);

-- DEC-067: adaptation-selection-specific prompt fragments.
-- ALL is request selection only. Response rows must use MINIMAL/BALANCED/MAXIMUM.
CREATE TABLE ai_request_prompt_adaptation (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  prompt_config_id INTEGER NOT NULL REFERENCES ai_prompt_config(id) ON DELETE CASCADE,
  adaptation_selection TEXT NOT NULL, -- MINIMAL, BALANCED, MAXIMUM, ALL
  prompt TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(prompt_config_id, adaptation_selection)
);

-- DEC-068: cover-letter true/false prompt fragments.
CREATE TABLE ai_request_prompt_cover_letter (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  prompt_config_id INTEGER NOT NULL REFERENCES ai_prompt_config(id) ON DELETE CASCADE,
  include_cover_letter BOOLEAN NOT NULL,
  prompt TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(prompt_config_id, include_cover_letter)
);

-- ----- Generation request / response -----
CREATE TABLE resume_generation_request (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  ai_model_id INTEGER NOT NULL REFERENCES ai_model(id),
  prompt_config_id INTEGER NOT NULL REFERENCES ai_prompt_config(id),
  language_mode TEXT NOT NULL,         -- ENGLISH_ONLY, RUSSIAN_ONLY, BILINGUAL
  adaptation_selection TEXT NOT NULL,  -- MINIMAL, BALANCED, MAXIMUM, ALL
  include_cover_letter BOOLEAN NOT NULL DEFAULT 0,
  vacancy_title TEXT NOT NULL,
  vacancy_description TEXT NOT NULL,
  company_name TEXT,
  company_description TEXT,
  additional_comments TEXT,
  status TEXT NOT NULL DEFAULT 'PENDING',
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

-- DEC-069: stores final rendered prompts for debugging/reproducibility.
CREATE TABLE ai_prompt_render_log (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  generation_request_id INTEGER NOT NULL REFERENCES resume_generation_request(id) ON DELETE CASCADE,
  prompt_config_id INTEGER NOT NULL REFERENCES ai_prompt_config(id),
  system_prompt_rendered TEXT NOT NULL,
  request_prompt_rendered TEXT NOT NULL,
  profile_payload_json TEXT,
  prompt_hash TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resume_generation_response (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  generation_request_id INTEGER NOT NULL REFERENCES resume_generation_request(id) ON DELETE CASCADE,
  language_id INTEGER NOT NULL REFERENCES language(id),
  adaptation_level_id INTEGER NOT NULL REFERENCES adaptation_level(id),
  professional_title TEXT,
  value_line TEXT,
  professional_summary TEXT,
  professional_aspirations TEXT,
  cover_letter TEXT,
  raw_variant_json TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'GENERATED',
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT,
  UNIQUE(generation_request_id, language_id, adaptation_level_id)
);

CREATE TABLE generation_response_experience (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  response_id INTEGER NOT NULL REFERENCES resume_generation_response(id) ON DELETE CASCADE,
  source_id TEXT NOT NULL,
  job_title TEXT NOT NULL,
  company_name TEXT NOT NULL,
  location TEXT,
  date_range TEXT,
  description TEXT,
  order_in_resume INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

CREATE TABLE generation_response_experience_bullet (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  experience_id INTEGER NOT NULL REFERENCES generation_response_experience(id) ON DELETE CASCADE,
  bullet_text TEXT NOT NULL,
  order_in_experience INTEGER NOT NULL DEFAULT 0
);

-- DEC-028/030: compact course format: name, provider, course_focus.
CREATE TABLE generation_response_course (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  response_id INTEGER NOT NULL REFERENCES resume_generation_response(id) ON DELETE CASCADE,
  source_id TEXT NOT NULL,
  name TEXT NOT NULL,
  provider TEXT NOT NULL,
  is_first_page BOOLEAN NOT NULL DEFAULT 1,
  course_focus TEXT,
  order_in_resume INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

CREATE TABLE generation_response_project (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  response_id INTEGER NOT NULL REFERENCES resume_generation_response(id) ON DELETE CASCADE,
  source_id TEXT NOT NULL,
  project_name TEXT NOT NULL,
  role TEXT,
  date_range TEXT,
  description TEXT,
  order_in_resume INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

CREATE TABLE generation_response_project_bullet (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  project_id INTEGER NOT NULL REFERENCES generation_response_project(id) ON DELETE CASCADE,
  bullet_text TEXT NOT NULL,
  order_in_project INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE generation_response_skill_group (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  response_id INTEGER NOT NULL REFERENCES resume_generation_response(id) ON DELETE CASCADE,
  group_name TEXT NOT NULL,
  order_in_resume INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

CREATE TABLE generation_response_skill (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  skill_group_id INTEGER NOT NULL REFERENCES generation_response_skill_group(id) ON DELETE CASCADE,
  skill_name TEXT NOT NULL,
  order_in_group INTEGER NOT NULL DEFAULT 0
);

-- DEC-071: AI-generated/reviewed Personal Information per response language/adaptation.
-- This keeps translated/localized personal resume lines editable on Review page before final HTML/PDF rendering.
CREATE TABLE generation_response_personal (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  response_id INTEGER NOT NULL UNIQUE REFERENCES resume_generation_response(id) ON DELETE CASCADE,
  location TEXT NOT NULL,
  spoken_languages TEXT NOT NULL,
  willingness_to_relocate TEXT NOT NULL,
  willingness_for_business_trips TEXT NOT NULL,
  citizenship TEXT NOT NULL,
  date_of_birth TEXT NOT NULL,
  work_formats TEXT,
  gpa_grade TEXT,
  order_in_resume INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);

-- Saved final resume rows. HTML is persisted on disk before PDF conversion.
-- PDF path is created as placeholder in prototype; Java must call real converter.
CREATE TABLE saved_resume (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  generation_request_id INTEGER NOT NULL REFERENCES resume_generation_request(id),
  response_id INTEGER NOT NULL REFERENCES resume_generation_response(id),
  language_id INTEGER NOT NULL REFERENCES language(id),
  adaptation_level_id INTEGER NOT NULL REFERENCES adaptation_level(id),
  title TEXT NOT NULL,
  public_code TEXT NOT NULL UNIQUE,
  public_url_link TEXT NOT NULL UNIQUE,
  html_file_path TEXT NOT NULL,
  pdf_file_path TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT
);
