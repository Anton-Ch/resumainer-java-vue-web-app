PRAGMA foreign_keys = ON;

-- PRODUCTION-CANDIDATE: port concept to main app as PDF render fit configuration.
CREATE TABLE IF NOT EXISTS pdf_fit_limits (
  id INTEGER PRIMARY KEY CHECK (id = 1),
  max_attempts INTEGER NOT NULL,
  step_percent REAL NOT NULL,
  page2_delta_limit_percent REAL NOT NULL,
  body_font_min_px REAL NOT NULL,
  body_font_default_px REAL NOT NULL,
  body_font_max_px REAL NOT NULL,
  line_height_min REAL NOT NULL,
  line_height_default REAL NOT NULL,
  line_height_max REAL NOT NULL,
  section_gap_min_px REAL NOT NULL,
  section_gap_default_px REAL NOT NULL,
  section_gap_max_px REAL NOT NULL,
  item_gap_min_px REAL NOT NULL,
  item_gap_default_px REAL NOT NULL,
  item_gap_max_px REAL NOT NULL,
  paragraph_gap_min_px REAL NOT NULL,
  paragraph_gap_default_px REAL NOT NULL,
  paragraph_gap_max_px REAL NOT NULL,
  bullet_gap_min_px REAL NOT NULL,
  bullet_gap_default_px REAL NOT NULL,
  bullet_gap_max_px REAL NOT NULL
);

-- PRODUCTION-CANDIDATE: port concept to main app as PDF page fill targets.
CREATE TABLE IF NOT EXISTS pdf_fill_targets (
  page_count INTEGER NOT NULL,
  page_number INTEGER NOT NULL,
  min_fill_ratio REAL NOT NULL,
  max_fill_ratio REAL,
  required_non_empty INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (page_count, page_number)
);

-- SPIKE_ONLY_DO_NOT_PORT: edge-case matrix for standalone validation only.
-- In the capstone app, use production budget/config rules instead of this mock table.
CREATE TABLE IF NOT EXISTS edge_case_rule (
  ec_number INTEGER PRIMARY KEY,
  min_work INTEGER NOT NULL,
  max_work INTEGER NOT NULL,
  project_count INTEGER NOT NULL,
  course_count INTEGER NOT NULL,
  template_mode TEXT NOT NULL,
  page1_work_items INTEGER NOT NULL,
  page2_work_items INTEGER NOT NULL,
  max_total_work_items INTEGER NOT NULL,
  reason TEXT NOT NULL
);

-- SPIKE_ONLY_DO_NOT_PORT: synthetic candidate data for PDF spike only.
-- In the capstone app, build ResumeData from real profile/generated/saved-resume tables.
CREATE TABLE IF NOT EXISTS mock_candidate (
  ec_number INTEGER PRIMARY KEY REFERENCES edge_case_rule(ec_number),
  en_full_name TEXT NOT NULL,
  ru_full_name TEXT NOT NULL,
  en_title TEXT NOT NULL,
  ru_title TEXT NOT NULL,
  phone TEXT NOT NULL,
  email TEXT NOT NULL,
  en_location TEXT NOT NULL,
  ru_location TEXT NOT NULL,
  linkedin TEXT NOT NULL,
  portfolio TEXT NOT NULL,
  telegram TEXT NOT NULL,
  whatsapp TEXT NOT NULL,
  work_count INTEGER NOT NULL,
  project_count INTEGER NOT NULL,
  course_count INTEGER NOT NULL
);

-- SPIKE_ONLY_DO_NOT_PORT: synthetic EN/RU edge scenarios for batch testing only.
CREATE TABLE IF NOT EXISTS mock_scenario (
  scenario_key TEXT PRIMARY KEY,
  ec_number INTEGER NOT NULL REFERENCES edge_case_rule(ec_number),
  language TEXT NOT NULL,
  expected_pages INTEGER NOT NULL
);
