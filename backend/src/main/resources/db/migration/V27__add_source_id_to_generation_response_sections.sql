-- =============================================================================
-- ResumAIner — Add source_id to generated repeatable response sections
-- =============================================================================
-- Purpose:
--   Preserve traceability between AI-generated repeatable sections and source
--   profile records used in the Dynamic payload.
--
-- Source examples:
--   workExperience[].id -> generation_response_experience.source_id
--   courses[].id        -> generation_response_course.source_id
--   projects[].id       -> generation_response_project.source_id
--
-- Notes:
--   source_id is VARCHAR because source identifiers in AI JSON are handled as
--   strings and may come from different source tables.
-- =============================================================================

ALTER TABLE generation_response_experience
    ADD COLUMN IF NOT EXISTS source_id VARCHAR(64);

ALTER TABLE generation_response_course
    ADD COLUMN IF NOT EXISTS source_id VARCHAR(64);

ALTER TABLE generation_response_project
    ADD COLUMN IF NOT EXISTS source_id VARCHAR(64);

COMMENT ON COLUMN generation_response_experience.source_id
    IS 'Original source workExperience.id from Dynamic payload, returned by AI as sourceId';

COMMENT ON COLUMN generation_response_course.source_id
    IS 'Original source courses.id from Dynamic payload, returned by AI as sourceId';

COMMENT ON COLUMN generation_response_project.source_id
    IS 'Original source projects.id from Dynamic payload, returned by AI as sourceId';

CREATE INDEX IF NOT EXISTS idx_resp_exp_response_source_id
    ON generation_response_experience (response_id, source_id);

CREATE INDEX IF NOT EXISTS idx_resp_course_response_source_id
    ON generation_response_course (response_id, source_id);

CREATE INDEX IF NOT EXISTS idx_resp_proj_response_source_id
    ON generation_response_project (response_id, source_id);
