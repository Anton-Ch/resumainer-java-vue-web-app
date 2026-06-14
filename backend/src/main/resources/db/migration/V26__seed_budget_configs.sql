-- =============================================================================
-- ResumAIner — Seed resume budget configuration (MVP Default)
-- =============================================================================
-- Source: ba-docs/docs/05_ui-ux/resume_template_details_and_logic.md §11.6
-- This seeds the first active budget config with all associated rules.
-- =============================================================================

-- =============================================================================
-- 1. Budget config
-- =============================================================================
INSERT INTO resume_budget_configs (id, name, version_no, is_active, description)
VALUES (1, 'Default MVP Resume Budget', 1, TRUE,
        'Main budget configuration for one-page and two-page resume templates');

-- =============================================================================
-- 2. Template selection rules
-- =============================================================================
INSERT INTO resume_template_selection_rules (config_id, rule_key, value_type, int_value, boolean_value, description)
VALUES
    (1, 'standard_one_page_max_jobs', 'int', 3, NULL,
     'Normal one-page candidate if no projects'),
    (1, 'allow_course_free_page1_expansion', 'boolean', NULL, TRUE,
     'Allow Page 1 expansion when no courses and no projects'),
    (1, 'course_free_page1_max_jobs', 'int', 5, NULL,
     'Max jobs on Page 1 when no courses and no projects'),
    (1, 'default_two_page_min_jobs', 'int', 4, NULL,
     'Default threshold for two-page template'),
    (1, 'page2_max_additional_jobs', 'int', 7, NULL,
     'Max additional Work Experience entries on Page 2');

-- =============================================================================
-- 3. Work experience distribution rules (edge cases)
-- =============================================================================
INSERT INTO resume_work_experience_distribution_rules
    (config_id, case_key, min_total_jobs, max_total_jobs,
     min_projects, max_projects, require_no_courses,
     template_mode, page1_jobs, page2_jobs, page2_max_additional_jobs, priority)
VALUES
    (1, 'EC-001',  1,  1, 0, 0,    FALSE, 'one_page', 1, 0, NULL, 10),
    (1, 'EC-003',  3,  3, 0, 0,    FALSE, 'one_page', 3, 0, NULL, 10),
    (1, 'EC-010',  4,  4, 0, 0,    FALSE, 'two_page', 2, 2, 2,    20),
    (1, 'EC-012',  5,  5, 0, 0,    FALSE, 'two_page', 3, 2, 2,    20),
    (1, 'EC-016',  7, 99, 0, NULL, FALSE, 'two_page', 3, 7, 7,    30),
    (1, 'EC-017',  4,  5, 0, 0,    TRUE,  'one_page', 5, 0, NULL, 5);

-- =============================================================================
-- 4. Section budget rules
-- =============================================================================
INSERT INTO resume_section_budget_rules
    (config_id, section_key, profile_key, metric_key, min_value, max_value)
VALUES
    -- Professional Summary
    (1, 'professional_summary',   'one_page_light', 'sentences',             5, 5),
    (1, 'professional_summary',   'one_page_dense',  'sentences',             2, 3),

    -- Work Experience Page 1
    (1, 'work_experience_page1',  'one_job',         'description_sentences',  5, 5),
    (1, 'work_experience_page1',  'one_job',         'bullet_points',          3, 9),
    (1, 'work_experience_page1',  'three_jobs',      'description_sentences',  3, 3),
    (1, 'work_experience_page1',  'three_jobs',      'bullet_points',          2, 5),

    -- Work Experience Page 2
    (1, 'work_experience_page2',  'default',         'summary_sentences_per_job', 1, 1),
    (1, 'work_experience_page2',  'default',         'bullet_points_per_job',     0, 0),

    -- Skills — MVP default: 4-5 groups, 5-7 skills per group, 1-3 words per skill
    (1, 'skills',                 'light',           'groups',                   4, 5),
    (1, 'skills',                 'light',           'skills_per_group',         5, 7),
    (1, 'skills',                 'light',           'words_per_skill',          1, 3),

    -- Courses
    (1, 'courses',                'medium',          'max_courses',              0, 7),
    (1, 'courses',                'medium',          'focus_words_per_course',   1, 3),

    -- Projects
    (1, 'projects',               'light',           'max_projects',             0, 4),
    (1, 'projects',               'light',           'sentences_per_project',    2, 3),
    (1, 'projects',               'light',           'bullet_points_per_project',2, 4),

    -- Aspirations
    (1, 'aspirations',            'light_page2',     'sentences',                5, 9);
