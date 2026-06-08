-- =============================================================================
-- ResumAIner — Add missing contact_detail columns from BA data dictionary
-- =============================================================================
-- Adds professional_title, linkedin_url, portfolio_url, telegram, and whatsapp
-- columns to the existing contact_detail table. These were defined in the BA
-- data dictionary but omitted from the original V6 migration.
-- =============================================================================

ALTER TABLE contact_detail
    ADD COLUMN IF NOT EXISTS professional_title VARCHAR(255),
    ADD COLUMN IF NOT EXISTS linkedin_url       VARCHAR(150),
    ADD COLUMN IF NOT EXISTS portfolio_url      VARCHAR(500),
    ADD COLUMN IF NOT EXISTS telegram           VARCHAR(100),
    ADD COLUMN IF NOT EXISTS whatsapp           VARCHAR(50);

COMMENT ON COLUMN contact_detail.professional_title IS 'Professional headline: Business Analyst, Junior Java Developer';
COMMENT ON COLUMN contact_detail.linkedin_url       IS 'LinkedIn profile URL (max 150 chars per DEC-026)';
COMMENT ON COLUMN contact_detail.portfolio_url      IS 'Portfolio or personal website URL';
COMMENT ON COLUMN contact_detail.telegram           IS 'Telegram username';
COMMENT ON COLUMN contact_detail.whatsapp           IS 'WhatsApp phone number';
