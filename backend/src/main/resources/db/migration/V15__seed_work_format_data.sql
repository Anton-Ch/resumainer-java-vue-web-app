-- =============================================================================
-- ResumAIner — Seed work_format lookup data
-- =============================================================================
-- Seeds 8 work format values from BA data dictionary.
-- Corresponds to DEC-022: preferred work format as M:N relationship.
-- =============================================================================

INSERT INTO work_format (code, name) VALUES
    ('full-time',           'Full-time'),
    ('part-time',           'Part-time'),
    ('rotational_schedule', 'Rotational schedule'),
    ('internship',          'Internship'),
    ('offline',             'Offline'),
    ('remote',              'Remote'),
    ('hybrid',              'Hybrid'),
    ('on_project_site',     'On-site project based');
