-- =============================================================================
-- ResumAIner — Create resume_generation_request table
-- =============================================================================
-- Core entity for the generation pipeline. Stores user input (vacancy/company
-- details), generation settings (language mode, adaptation, AI model), and
-- processing state. UUID PK per D7 hybrid strategy.
-- =============================================================================

CREATE TABLE resume_generation_request (
    id                      UUID            DEFAULT gen_random_uuid() PRIMARY KEY,

    -- User and model references
    user_id                 UUID            NOT NULL REFERENCES users(id),
    ai_model_id             UUID            NOT NULL REFERENCES ai_model(id),

    -- Generation input
    vacancy_title           TEXT            NOT NULL,
    vacancy_description     TEXT            NOT NULL,
    company_name            TEXT,
    company_description     TEXT,
    additional_comments     TEXT,
    include_cover_letter    BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Generation parameters
    language_mode           VARCHAR(20)     NOT NULL CHECK (language_mode IN ('ENGLISH_ONLY', 'RUSSIAN_ONLY', 'BILINGUAL')),
    adaptation_selection    VARCHAR(20)     NOT NULL CHECK (adaptation_selection IN ('MINIMAL', 'BALANCED', 'MAXIMUM', 'ALL')),

    -- Reference data FKs
    prompt_config_id        UUID,

    -- Budget config (BIGINT matches resume_budget_configs PK)
    budget_config_id        BIGINT,
    budget_config_version_used INTEGER,

    -- Processing state
    status                  VARCHAR(30)     NOT NULL DEFAULT 'pending'
                                            CHECK (status IN ('pending', 'processing', 'completed', 'failed')),
    error_message           TEXT,

    -- Audit timestamps
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMP
);

COMMENT ON TABLE  resume_generation_request          IS 'Generation request: user input, settings, and processing state';
COMMENT ON COLUMN resume_generation_request.user_id  IS 'Owner of this generation request';
COMMENT ON COLUMN resume_generation_request.ai_model_id IS 'Selected AI model for this request (FK to ai_model)';
COMMENT ON COLUMN resume_generation_request.vacancy_title IS 'Target vacancy title';
COMMENT ON COLUMN resume_generation_request.vacancy_description IS 'Target vacancy description';
COMMENT ON COLUMN resume_generation_request.language_mode IS 'ENGLISH_ONLY, RUSSIAN_ONLY, or BILINGUAL';
COMMENT ON COLUMN resume_generation_request.adaptation_selection IS 'MINIMAL, BALANCED, MAXIMUM, or ALL (ALL is request-level only)';
COMMENT ON COLUMN resume_generation_request.status  IS 'pending → processing → completed | failed';
COMMENT ON COLUMN resume_generation_request.error_message IS 'User-readable error message if status = failed';
COMMENT ON COLUMN resume_generation_request.prompt_config_id IS 'FK to ai_prompt_config — saved for traceability';

-- Index for owner-scoped queries
CREATE INDEX idx_gen_request_user_id ON resume_generation_request (user_id);
CREATE INDEX idx_gen_request_status ON resume_generation_request (status);
