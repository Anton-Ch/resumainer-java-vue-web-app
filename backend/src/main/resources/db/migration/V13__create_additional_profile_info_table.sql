-- =============================================================================
-- ResumAIner — Create additional_profile_info table
-- =============================================================================
-- Stores extended profile settings in a 1:1 relationship with users.
-- Contains resume language preferences, work format preferences, skills,
-- languages, aspirations, and personal info (date of birth, citizenship).
-- Simplified MVP design (DEC-013): free-text fields for AI context.
-- No soft-delete needed — updated in-place or created on first save.
-- =============================================================================

CREATE TABLE additional_profile_info (
    id                            BIGSERIAL       PRIMARY KEY,
    user_id                       UUID            NOT NULL UNIQUE REFERENCES users(id),

    -- Free-text fields (feed for AI model)
    skills                        TEXT,
    languages                     TEXT,
    professional_aspirations      TEXT,
    achievements                  TEXT,
    general_information           TEXT,

    -- Resume language preferences (FK to language table)
    default_resume_language_id    BIGINT          REFERENCES language(id),
    additional_resume_language_id BIGINT          REFERENCES language(id),

    -- Work preferences
    ready_for_relocation          VARCHAR(20)     CHECK (ready_for_relocation IN ('Yes', 'No', 'Negotiable')),
    ready_for_business_trips      VARCHAR(20)     CHECK (ready_for_business_trips IN ('Yes', 'No', 'Negotiable')),

    -- Personal info
    date_of_birth                 DATE            NOT NULL,
    citizenship                   VARCHAR(150)    NOT NULL,

    -- Post-MVP (DEC-050)
    photo_file_path               VARCHAR(500),

    -- Audit timestamps
    created_at                    TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at                    TIMESTAMP
);

COMMENT ON TABLE  additional_profile_info                           IS 'Extended profile settings (1:1 with users)';
COMMENT ON COLUMN additional_profile_info.id                        IS 'BIGSERIAL primary key';
COMMENT ON COLUMN additional_profile_info.user_id                   IS 'FK to users table — one-to-one (SEC-001)';
COMMENT ON COLUMN additional_profile_info.skills                    IS 'Free-text skills list (comma-separated or free form)';
COMMENT ON COLUMN additional_profile_info.languages                 IS 'Free-text languages with proficiency levels';
COMMENT ON COLUMN additional_profile_info.professional_aspirations  IS 'Target career direction and goals';
COMMENT ON COLUMN additional_profile_info.achievements              IS 'Key professional and personal achievements';
COMMENT ON COLUMN additional_profile_info.general_information       IS 'AI context for resume generation';
COMMENT ON COLUMN additional_profile_info.default_resume_language_id    IS 'Default resume generation language (FK to language)';
COMMENT ON COLUMN additional_profile_info.additional_resume_language_id IS 'Additional resume generation language (FK to language)';
COMMENT ON COLUMN additional_profile_info.ready_for_relocation      IS 'Relocation readiness: Yes, No, Negotiable';
COMMENT ON COLUMN additional_profile_info.ready_for_business_trips  IS 'Business trip readiness: Yes, No, Negotiable';
COMMENT ON COLUMN additional_profile_info.date_of_birth             IS 'Full exact date of birth (required)';
COMMENT ON COLUMN additional_profile_info.citizenship               IS 'User citizenship (required)';
COMMENT ON COLUMN additional_profile_info.photo_file_path           IS 'Profile photo file path (Post-MVP, DEC-050)';
COMMENT ON COLUMN additional_profile_info.created_at                IS 'Record creation timestamp';
COMMENT ON COLUMN additional_profile_info.updated_at                IS 'Last modification timestamp';
