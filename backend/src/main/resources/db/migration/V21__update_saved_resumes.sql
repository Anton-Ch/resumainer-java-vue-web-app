-- =============================================================================
-- ResumAIner — Update saved_resumes table for generation feature
-- =============================================================================
-- Adds columns required by the generation pipeline (DEC-073, ERD v4.0).
-- HTML is saved before PDF (DEC-073). PDF path is nullable until feat/008.
-- New columns coexist with existing V8 columns.
-- =============================================================================

ALTER TABLE saved_resumes
    ADD COLUMN IF NOT EXISTS html_file_path        VARCHAR(500),
    ADD COLUMN IF NOT EXISTS pdf_file_path         VARCHAR(500),
    ADD COLUMN IF NOT EXISTS public_code           VARCHAR(10),
    ADD COLUMN IF NOT EXISTS public_url_link       VARCHAR(200),
    ADD COLUMN IF NOT EXISTS generation_request_id UUID REFERENCES resume_generation_request(id),
    ADD COLUMN IF NOT EXISTS response_id           UUID REFERENCES resume_generation_response(id),
    ADD COLUMN IF NOT EXISTS adaptation_level_id   BIGINT REFERENCES adaptation_level(id),
    ADD COLUMN IF NOT EXISTS language_id           BIGINT REFERENCES language(id),
    ADD COLUMN IF NOT EXISTS template_id           BIGINT,
    ADD COLUMN IF NOT EXISTS is_deleted            BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN saved_resumes.html_file_path        IS 'Path to saved filled HTML file — canonical artifact in feat/007';
COMMENT ON COLUMN saved_resumes.pdf_file_path         IS 'Path to PDF file — nullable until PDF conversion in feat/008';
COMMENT ON COLUMN saved_resumes.public_code           IS 'Unique public code for recruiter link';
COMMENT ON COLUMN saved_resumes.public_url_link       IS 'Full public resume URL';
COMMENT ON COLUMN saved_resumes.generation_request_id IS 'FK to generation request that produced this resume';
COMMENT ON COLUMN saved_resumes.response_id           IS 'FK to generation response used for this resume';
COMMENT ON COLUMN saved_resumes.adaptation_level_id   IS 'FK to adaptation level — MINIMAL, BALANCED, or MAXIMUM';
COMMENT ON COLUMN saved_resumes.language_id           IS 'FK to language — EN or RU';
COMMENT ON COLUMN saved_resumes.template_id           IS 'Post-MVP: FK to resume_template for template selection';

CREATE INDEX IF NOT EXISTS idx_saved_resumes_public_code ON saved_resumes (public_code);
CREATE UNIQUE INDEX IF NOT EXISTS uq_saved_resumes_user_public_code ON saved_resumes (user_id, public_code);
