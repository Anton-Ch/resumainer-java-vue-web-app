-- =============================================================================
-- V34: Patch PDF render fit limits to V12.1 spike parity
-- =============================================================================
-- Adds missing default-value columns and step_percent to resume_pdf_fit_limits.
-- Updates existing seed values to match spike V12.1 behavior.
-- page2_delta_limit_percent uses FRACTION semantics (0.65 = 65 percent).
-- =============================================================================

-- Add missing default columns (all nullable, set via UPDATE below)
ALTER TABLE resume_pdf_fit_limits
    ADD COLUMN IF NOT EXISTS step_percent            NUMERIC(5,4),
    ADD COLUMN IF NOT EXISTS body_font_default_px    NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS line_height_default     NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS section_gap_default_px  NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS item_gap_default_px     NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS paragraph_gap_default_px NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS bullet_gap_default_px   NUMERIC(5,2);

COMMENT ON COLUMN resume_pdf_fit_limits.step_percent IS 'Step change per fitting iteration as fraction (0.10 = 10 percent)';
COMMENT ON COLUMN resume_pdf_fit_limits.body_font_default_px IS 'Default body font size (spike V12.1: 12.5)';
COMMENT ON COLUMN resume_pdf_fit_limits.line_height_default IS 'Default line height (spike V12.1: 1.35)';
COMMENT ON COLUMN resume_pdf_fit_limits.section_gap_default_px IS 'Default section gap (spike V12.1: 15.0)';
COMMENT ON COLUMN resume_pdf_fit_limits.item_gap_default_px IS 'Default item gap (spike V12.1: 9.0)';
COMMENT ON COLUMN resume_pdf_fit_limits.paragraph_gap_default_px IS 'Default paragraph gap (spike V12.1: 5.0)';
COMMENT ON COLUMN resume_pdf_fit_limits.bullet_gap_default_px IS 'Default bullet gap (spike V12.1: 3.0)';

-- Patch existing active config to V12.1 spike values
UPDATE resume_pdf_fit_limits SET
    step_percent = 0.10,
    body_font_default_px = 12.5,
    line_height_default = 1.35,
    section_gap_default_px = 15.0,
    item_gap_default_px = 9.0,
    paragraph_gap_default_px = 5.0,
    bullet_gap_default_px = 3.0,
    page2_delta_limit_percent = 0.65,
    max_attempts = 20
WHERE active = TRUE;
