-- =============================================================================
-- ResumAIner — Create AI prompt configuration tables
-- =============================================================================
-- DB-backed modular prompt configuration (DEC-064 through DEC-069, ERD v4.0).
-- Backend assembles final prompt from fragments: system + language + adaptation
-- + cover_letter. Only one active config at a time.
-- =============================================================================

-- =============================================================================
-- PROMPT CONFIG — versioned bundle metadata
-- =============================================================================
CREATE TABLE ai_prompt_config (
    id              UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    is_active       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

COMMENT ON TABLE  ai_prompt_config          IS 'Versioned prompt bundle — only one active config at a time';
COMMENT ON COLUMN ai_prompt_config.is_active IS 'Only one config should be active; enforce via partial unique index';

-- Enforce only one active config
CREATE UNIQUE INDEX uq_active_prompt_config ON ai_prompt_config (is_active) WHERE is_active = TRUE;

-- =============================================================================
-- SYSTEM PROMPT — stable global rules
-- =============================================================================
CREATE TABLE ai_system_prompt (
    id              UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    prompt_config_id UUID           NOT NULL REFERENCES ai_prompt_config(id),
    prompt          TEXT            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

COMMENT ON TABLE ai_system_prompt IS 'Stable system prompt — role, JSON-only, safety rules. One per config.';

-- =============================================================================
-- LANGUAGE PROMPT — language-mode-specific instructions
-- =============================================================================
CREATE TABLE ai_request_prompt_language (
    id              UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    prompt_config_id UUID           NOT NULL REFERENCES ai_prompt_config(id),
    language_mode   VARCHAR(20)     NOT NULL CHECK (language_mode IN ('ENGLISH_ONLY', 'RUSSIAN_ONLY', 'BILINGUAL')),
    prompt          TEXT            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

COMMENT ON TABLE ai_request_prompt_language IS 'Language-mode-specific prompt fragment';

CREATE UNIQUE INDEX uq_prompt_lang_config_mode ON ai_request_prompt_language (prompt_config_id, language_mode);

-- =============================================================================
-- ADAPTATION PROMPT — adaptation-specific instructions
-- =============================================================================
CREATE TABLE ai_request_prompt_adaptation (
    id                    UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    prompt_config_id      UUID            NOT NULL REFERENCES ai_prompt_config(id),
    adaptation_selection  VARCHAR(20)     NOT NULL CHECK (adaptation_selection IN ('MINIMAL', 'BALANCED', 'MAXIMUM', 'ALL')),
    prompt                TEXT            NOT NULL,
    created_at            TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP
);

COMMENT ON TABLE ai_request_prompt_adaptation IS 'Adaptation-level-specific prompt fragment';

CREATE UNIQUE INDEX uq_prompt_adapt_config_adapt ON ai_request_prompt_adaptation (prompt_config_id, adaptation_selection);

-- =============================================================================
-- COVER LETTER PROMPT — cover-letter-specific rules
-- =============================================================================
CREATE TABLE ai_request_prompt_cover_letter (
    id                    UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    prompt_config_id      UUID            NOT NULL REFERENCES ai_prompt_config(id),
    include_cover_letter  BOOLEAN         NOT NULL,
    prompt                TEXT            NOT NULL,
    created_at            TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP
);

COMMENT ON TABLE ai_request_prompt_cover_letter IS 'Cover-letter-specific prompt fragment — prevents unwanted generation';

CREATE UNIQUE INDEX uq_prompt_cl_config_incl ON ai_request_prompt_cover_letter (prompt_config_id, include_cover_letter);

-- =============================================================================
-- PROMPT RENDER LOG — debugging and reproducibility
-- =============================================================================
CREATE TABLE ai_prompt_render_log (
    id                      UUID            DEFAULT gen_random_uuid() PRIMARY KEY,
    generation_request_id   UUID            NOT NULL REFERENCES resume_generation_request(id),
    prompt_config_id        UUID            NOT NULL REFERENCES ai_prompt_config(id),

    system_prompt_rendered  TEXT            NOT NULL,
    request_prompt_rendered TEXT            NOT NULL,
    profile_payload_json    TEXT,
    prompt_hash             VARCHAR(128),

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE ai_prompt_render_log IS 'Rendered prompt log for debugging — contains PII, admin access only in MVP';

CREATE INDEX idx_render_log_request_id ON ai_prompt_render_log (generation_request_id);
