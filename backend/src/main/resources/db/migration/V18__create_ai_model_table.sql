-- =============================================================================
-- ResumAIner — Create ai_model table
-- =============================================================================
-- Stores AI provider configuration: model code, endpoint URL, encrypted API key,
-- and visibility flags. UUID PK per D7 hybrid strategy (entity table).
-- API key is encrypted at rest, masked in UI, never logged (per DEC-008, NFR-001).
-- =============================================================================

CREATE TABLE ai_model (
    id                  UUID            DEFAULT gen_random_uuid() PRIMARY KEY,

    provider            VARCHAR(255)    NOT NULL,
    model_code          VARCHAR(255)    NOT NULL,
    display_name        VARCHAR(255)    NOT NULL,
    provider_api_url    VARCHAR(500)    NOT NULL,

    -- Encrypted, not plaintext. Masked in UI after saving.
    api_key_encrypted   VARCHAR(512)    NOT NULL,

    -- Visibility flags
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    is_paid             BOOLEAN         NOT NULL DEFAULT FALSE,
    is_hidden           BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Timestamps
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

COMMENT ON TABLE  ai_model                 IS 'AI provider model configuration with encrypted API key storage';
COMMENT ON COLUMN ai_model.provider        IS 'Provider name: OpenRouter, OpenAI, etc.';
COMMENT ON COLUMN ai_model.model_code      IS 'Model identifier used in API calls: deepseek/deepseek-v4-pro';
COMMENT ON COLUMN ai_model.display_name    IS 'User-facing model name: Deepseek v4 Pro';
COMMENT ON COLUMN ai_model.provider_api_url IS 'Base URL for API calls';
COMMENT ON COLUMN ai_model.api_key_encrypted IS 'Encrypted API key — never logged, masked in UI';
COMMENT ON COLUMN ai_model.is_active       IS 'If false, model is unavailable for new generations';
COMMENT ON COLUMN ai_model.is_paid         IS 'Marks paid models for admin awareness';
COMMENT ON COLUMN ai_model.is_hidden       IS 'If true, only privileged users can see this model';

CREATE UNIQUE INDEX uq_ai_model_code ON ai_model (provider, model_code);
