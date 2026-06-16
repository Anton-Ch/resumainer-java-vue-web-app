-- =============================================================================
-- ResumAIner — Patch missing Work Experience edge-case distribution rules
-- =============================================================================
-- Business logic gap fix:
-- BA Edge Case Matrix defines EC-001..EC-017. V26 seeded only a subset:
-- EC-001, EC-003, EC-010, EC-012, EC-016, EC-017.
--
-- This migration adds the missing MVP-critical cases so that the
-- WorkExperienceBudgetResolver can be fully DB-backed and does not need
-- hardcoded fallbacks for 2 jobs, project-driven two-page cases, or 6 jobs.
-- =============================================================================

INSERT INTO resume_work_experience_distribution_rules
    (config_id, case_key, min_total_jobs, max_total_jobs,
     min_projects, max_projects, require_no_courses,
     template_mode, page1_jobs, page2_jobs, page2_max_additional_jobs, priority)
SELECT
    v.config_id,
    v.case_key,
    v.min_total_jobs,
    v.max_total_jobs,
    v.min_projects,
    v.max_projects,
    v.require_no_courses,
    v.template_mode,
    v.page1_jobs,
    v.page2_jobs,
    v.page2_max_additional_jobs,
    v.priority
FROM (
    VALUES
        -- No projects, normal one-page cases
        (1, 'EC-002', 2, 2, 0, 0,    FALSE, 'one_page', 2, 0, NULL, 10),

        -- Project-driven two-page cases with no additional Work Experience jobs
        (1, 'EC-004', 1, 1, 1, 1,    FALSE, 'two_page', 1, 0, 0,    15),
        (1, 'EC-005', 2, 2, 1, 1,    FALSE, 'two_page', 2, 0, 0,    15),
        (1, 'EC-006', 3, 3, 1, 1,    FALSE, 'two_page', 3, 0, 0,    15),
        (1, 'EC-007', 1, 1, 2, NULL, FALSE, 'two_page', 1, 0, 0,    15),
        (1, 'EC-008', 2, 2, 2, NULL, FALSE, 'two_page', 2, 0, 0,    15),
        (1, 'EC-009', 3, 3, 2, NULL, FALSE, 'two_page', 3, 0, 0,    15),

        -- Additional Work Experience + projects cases
        (1, 'EC-011', 4, 4, 1, NULL, FALSE, 'two_page', 2, 2, 2,    20),
        (1, 'EC-013', 5, 5, 1, NULL, FALSE, 'two_page', 3, 2, 2,    20),

        -- Exact 6-job cases
        (1, 'EC-014', 6, 6, 0, 0,    FALSE, 'two_page', 3, 3, 3,    25),
        (1, 'EC-015', 6, 6, 1, NULL, FALSE, 'two_page', 3, 3, 3,    25)
) AS v(config_id, case_key, min_total_jobs, max_total_jobs,
       min_projects, max_projects, require_no_courses,
       template_mode, page1_jobs, page2_jobs, page2_max_additional_jobs, priority)
WHERE NOT EXISTS (
    SELECT 1
    FROM resume_work_experience_distribution_rules existing
    WHERE existing.config_id = v.config_id
      AND existing.case_key = v.case_key
);
