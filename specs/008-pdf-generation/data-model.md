# Data Model — Feature 008

## New Entities

### GenerationResponseExperienceBullet

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | BIGSERIAL | PK | D7: BIGSERIAL for lookup tables |
| experience_id | BIGINT | FK → generation_response_experience(id), ON DELETE CASCADE | B15: verify FK type matches PK |
| bullet_order | INT | NOT NULL | Deterministic order from AI response |
| bullet_text | VARCHAR(250) | NOT NULL, CHECK(TRIM(bullet_text) <> '') | FR-008-004, FR-008-007 |
| is_edited | BOOLEAN | NOT NULL, DEFAULT FALSE | Tracks user edits |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

UNIQUE (experience_id, bullet_order)

### GenerationResponseProjectBullet

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| project_id | BIGINT | FK → generation_response_project(id), ON DELETE CASCADE | |
| bullet_order | INT | NOT NULL | |
| bullet_text | VARCHAR(250) | NOT NULL, CHECK(TRIM(bullet_text) <> '') | |
| is_edited | BOOLEAN | NOT NULL, DEFAULT FALSE | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

UNIQUE (project_id, bullet_order)

### ResumePdfFitLimits

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| config_key | VARCHAR(100) | NOT NULL, UNIQUE | Named config for lookup |
| active | BOOLEAN | NOT NULL, DEFAULT FALSE | Only one active at a time |
| body_font_min_px | NUMERIC(5,2) | NOT NULL | e.g., 6.00 |
| body_font_max_px | NUMERIC(5,2) | NOT NULL | e.g., 9.00 |
| line_height_min | NUMERIC(5,2) | NOT NULL | |
| line_height_max | NUMERIC(5,2) | NOT NULL | |
| section_gap_min_px | NUMERIC(5,2) | NOT NULL | |
| section_gap_max_px | NUMERIC(5,2) | NOT NULL | |
| item_gap_min_px | NUMERIC(5,2) | NOT NULL | |
| item_gap_max_px | NUMERIC(5,2) | NOT NULL | |
| paragraph_gap_min_px | NUMERIC(5,2) | NOT NULL | |
| paragraph_gap_max_px | NUMERIC(5,2) | NOT NULL | |
| bullet_gap_min_px | NUMERIC(5,2) | NOT NULL | |
| bullet_gap_max_px | NUMERIC(5,2) | NOT NULL | |
| max_attempts | INT | NOT NULL | Bounded fitting loop |
| page2_delta_limit_percent | NUMERIC(5,2) | NOT NULL | Page2 relative to page1 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | |

### ResumePdfFillTargets

| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | BIGSERIAL | PK | |
| fit_limits_id | BIGINT | NOT NULL, FK → resume_pdf_fit_limits(id) | |
| target_page_count | INT | NOT NULL | 1 or 2 |
| page_number | INT | NOT NULL | 1 or 2 |
| language_code | VARCHAR(10) | NULLABLE | NULL = applies to all |
| project_count_min | INT | NULLABLE | NULL = no lower bound |
| project_count_max | INT | NULLABLE | NULL = no upper bound |
| min_fill | NUMERIC(5,4) | NOT NULL | e.g., 0.3000 |
| max_fill | NUMERIC(5,4) | NOT NULL | e.g., 0.9500 |
| priority | INT | NOT NULL, DEFAULT 100 | Lower = higher priority |

UNIQUE (fit_limits_id, target_page_count, page_number, language_code, project_count_min, project_count_max, priority)

## Updated Entities

### SavedResume (new columns)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| pdf_status | VARCHAR(50) | NULLABLE | PENDING, GENERATING, READY, FAILED |
| pdf_file_path | VARCHAR(500) | NULLABLE | Relative path, never exposed |
| pdf_generated_at | TIMESTAMP | NULLABLE | B9: use Long (boxed) in model |
| pdf_generation_error_code | VARCHAR(100) | NULLABLE | Machine-readable |
| pdf_generation_error_message | VARCHAR(500) | NULLABLE | User-readable, not raw |
| pdf_render_profile | VARCHAR(100) | NULLABLE | config_key reference |
| pdf_page_count | INTEGER | NULLABLE | B9: use Integer (boxed) |

### GenerationResponseExperience (updated)

Add transient or persistent list of `GenerationResponseExperienceBullet`.

### GenerationResponseProject (updated)

Add transient or persistent list of `GenerationResponseProjectBullet`.

## Seed Data

- Default PDF fit limits (from spike V12.1): body font 6.0–9.0px, line-height 1.0–1.3, gaps 0–16px, max_attempts 30, page2_delta 50%.
- Fill targets: page1 min 0.60, page1 max 0.95; page2 min 0.30 (0 projects) to 0.50, page2 max 0.95.
