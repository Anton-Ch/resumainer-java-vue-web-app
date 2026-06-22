-- =============================================================================
-- V36: Restore V12.1 spike-proven PDF budget parity
-- =============================================================================
-- Why:
--   The standalone pdf-spike-openhtmltopdf-v12-final V12.1 proved EC-016 with:
--     - 8 total work experience records rendered
--     - Page 1: 3 work records
--     - Page 2: 5 additional work records
--     - Projects: 3
--
--   Production drifted to Page 2 = 7 additional work records and projects = 4,
--   allowing 10 work + 4 projects into the PDF pipeline. That exceeds the proven
--   V12.1 capacity and causes runtime PAGE2:MISSING_TEXTS during finalization.
--
-- Scope:
--   This migration does not change source profile data. It only changes generation
--   and rendering budgets. The AI may still see all source records in Dynamic payload,
--   but must return no more than the resolved budget.
-- =============================================================================

-- Global Page 2 additional Work Experience cap: restore V12.1 EC-016 cap.
UPDATE resume_template_selection_rules
SET int_value = 5,
    description = 'Max additional Work Experience entries on Page 2 — restored to V12.1 spike-proven cap'
WHERE config_id = 1
  AND rule_key = 'page2_max_additional_jobs';

-- EC-016 remains a catch-all for dense profiles with 7+ source jobs, but it may render
-- only 3 + 5 = 8 work records. Do NOT set max_total_jobs to 8: that would make dense
-- profiles with 9+ source jobs fail rule matching. The resolver caps selected output.
UPDATE resume_work_experience_distribution_rules
SET page2_jobs = 5,
    page2_max_additional_jobs = 5
WHERE config_id = 1
  AND case_key = 'EC-016';

-- Restore project cap to the V12.1 proven EC-016 value.
UPDATE resume_section_budget_rules
SET max_value = 3
WHERE config_id = 1
  AND section_key = 'projects'
  AND profile_key = 'light'
  AND metric_key = 'max_projects';

-- Bump visible budget version for request snapshots and diagnostics.
UPDATE resume_budget_configs
SET version_no = CASE WHEN version_no < 2 THEN 2 ELSE version_no END,
    description = 'Main resume budget configuration restored to V12.1 spike-proven PDF capacity: EC-016 3+5 work and max 3 projects'
WHERE id = 1;
