-- =============================================================================
-- ResumAIner — Update saved_resumes for PDF generation metadata (Feature 008)
-- =============================================================================
-- Adds PDF status, file path, page count, timestamps, and error fields.
-- pdf_file_path and public_code already exist from V21 migration.
-- All new fields are nullable — populated during/after PDF finalization.
-- =============================================================================

ALTER TABLE saved_resumes
    ADD COLUMN IF NOT EXISTS pdf_status                 VARCHAR(50),
    ADD COLUMN IF NOT EXISTS pdf_generated_at           TIMESTAMP,
    ADD COLUMN IF NOT EXISTS pdf_generation_error_code  VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pdf_generation_error_message VARCHAR(500),
    ADD COLUMN IF NOT EXISTS pdf_render_profile         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pdf_page_count             INT;

COMMENT ON COLUMN saved_resumes.pdf_status                  IS 'PDF generation status: PENDING, GENERATING, READY, FAILED';
COMMENT ON COLUMN saved_resumes.pdf_generated_at            IS 'Timestamp when PDF was successfully generated';
COMMENT ON COLUMN saved_resumes.pdf_generation_error_code   IS 'Error code when PDF generation fails';
COMMENT ON COLUMN saved_resumes.pdf_generation_error_message IS 'User-readable error message on PDF generation failure';
COMMENT ON COLUMN saved_resumes.pdf_render_profile          IS 'Config key of the render profile used for this PDF';
COMMENT ON COLUMN saved_resumes.pdf_page_count              IS 'Final page count of the generated PDF (1 or 2)';
