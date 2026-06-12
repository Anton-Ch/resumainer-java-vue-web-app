PRAGMA foreign_keys = ON;

INSERT INTO role (id, code, name) VALUES
  (1, 'USER', 'User'),
  (2, 'ADMIN', 'Admin');

INSERT INTO user_status (id, code, name) VALUES
  (1, 'ACTIVE', 'Active'),
  (2, 'INACTIVE', 'Inactive');

INSERT INTO user_generation_permission (id, code, name) VALUES
  (1, 'ALLOWED', 'Allowed'),
  (2, 'FORBIDDEN', 'Forbidden');

INSERT INTO language (id, code, name) VALUES
  (1, 'EN', 'English'),
  (2, 'RU', 'Russian');

INSERT INTO adaptation_level (id, code, name) VALUES
  (1, 'MINIMAL', 'Minimal'),
  (2, 'BALANCED', 'Balanced'),
  (3, 'MAXIMUM', 'Maximum');

-- DEC-022: 3NF work format reference data.
-- name_en/name_ru are kept in the prototype so prompt payload can pass localized display names without AI guessing.
INSERT INTO work_format (id, code, name_en, name_ru) VALUES
  (1, 'FULL_TIME', 'Full-time', 'Полная занятость'),
  (2, 'PART_TIME', 'Part-time', 'Частичная занятость'),
  (3, 'ROTATIONAL_SCHEDULE', 'Rotational schedule', 'Вахтовый график'),
  (4, 'INTERNSHIP', 'Internship', 'Стажировка'),
  (5, 'OFFLINE', 'Office / on-site', 'Офис / на месте'),
  (6, 'REMOTE', 'Remote', 'Удалённо'),
  (7, 'HYBRID', 'Hybrid', 'Гибрид'),
  (8, 'PROJECT_SITE', 'Project site', 'На проектной площадке');

-- Prototype stores OpenRouter API key and model in SQLite so they can be edited directly.
-- Java/PostgreSQL implementation must encrypt API keys and never log them.
INSERT INTO ai_model
(provider, model_code, display_name, provider_api_url, api_key_encrypted, is_active, is_paid, is_hidden)
VALUES
('OpenRouter', 'deepseek/deepseek-v4-flash', 'DeepSeek V4 Flash via OpenRouter',
 'https://openrouter.ai/api/v1/chat/completions', 'REPLACE_ME_OPENROUTER_API_KEY', 1, 0, 0);

INSERT INTO ai_prompt_config (id, name, description, is_active)
VALUES (1, 'Default Resume Generation Prompt Config v3', 'Modular prompt bundle for backend prototype v3.', 1);

INSERT INTO ai_system_prompt (prompt_config_id, prompt) VALUES (1, 'You are ResumAIner resume generation engine. Return valid JSON only. Do not include markdown fences. Do not hallucinate facts. Preserve sourceId for repeatable sections. Use only source profile and vacancy data. Keep bilingual versions semantically consistent but natural in each language. Return personalInfo for each language/adaptation variant. Education is profile-owned and must not be invented. Work formats are profile-owned normalized data: use only profile.workFormats values and do not invent additional work formats.');

INSERT INTO ai_request_prompt_language (prompt_config_id, language_mode, prompt) VALUES
(1, 'ENGLISH_ONLY', 'Language mode: ENGLISH_ONLY. Return only the "en" root object. All generated text must be in natural English.'),
(1, 'RUSSIAN_ONLY', 'Language mode: RUSSIAN_ONLY. Return only the "ru" root object. All generated text must be in natural Russian.'),
(1, 'BILINGUAL', 'Language mode: BILINGUAL. Return both "en" and "ru" root objects in a single JSON response. The two versions must describe the same real professional facts and situations. Use natural localization, not literal word-for-word translation. Preserve sourceId parity across languages. For personalInfo.workFormats, use profile.workFormats.english for English output and profile.workFormats.russian for Russian output.');

INSERT INTO ai_request_prompt_adaptation (prompt_config_id, adaptation_selection, prompt) VALUES
(1, 'MINIMAL', 'Adaptation selection: MINIMAL. Return only the "minimal" variant for each requested language. Make light cleanup and concise tailoring without changing the core meaning.'),
(1, 'BALANCED', 'Adaptation selection: BALANCED. Return only the "balanced" variant for each requested language. Make practical tailoring to the vacancy without overfitting.'),
(1, 'MAXIMUM', 'Adaptation selection: MAXIMUM. Return only the "maximum" variant for each requested language. Strongly tailor positioning and emphasis to the vacancy while preserving facts.'),
(1, 'ALL', 'Adaptation selection: ALL. Return all three variants for each requested language: "minimal", "balanced", and "maximum". The final saved resume will later use only one selected adaptation level.');

INSERT INTO ai_request_prompt_cover_letter (prompt_config_id, include_cover_letter, prompt) VALUES
(1, 1, 'Cover letter: enabled. Return coverLetter text for every generated language/adaptation variant.'),
(1, 0, 'Cover letter: disabled. Return coverLetter as null for every generated language/adaptation variant. Do not generate a cover letter.');
