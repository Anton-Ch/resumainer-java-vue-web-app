-- V38: Create auth_tokens table for email verification and password reset
--
-- Stores hashed tokens only — raw tokens are never persisted.
-- Token type controlled by CHECK constraint for consistency.

CREATE TABLE auth_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id),
    token_type VARCHAR(40)  NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    consumed_at TIMESTAMP,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_auth_tokens_type CHECK (token_type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET'))
);

CREATE INDEX idx_auth_tokens_token_hash ON auth_tokens(token_hash);
CREATE INDEX idx_auth_tokens_user_type  ON auth_tokens(user_id, token_type);

COMMENT ON TABLE  auth_tokens                  IS 'Hashed email verification and password reset tokens';
COMMENT ON COLUMN auth_tokens.user_id          IS 'FK to users — the token owner';
COMMENT ON COLUMN auth_tokens.token_type       IS 'Token purpose: EMAIL_VERIFICATION or PASSWORD_RESET';
COMMENT ON COLUMN auth_tokens.token_hash       IS 'SHA-256 or similar hash of the raw token — raw token never stored';
COMMENT ON COLUMN auth_tokens.expires_at       IS 'Token expiry timestamp';
COMMENT ON COLUMN auth_tokens.consumed_at      IS 'When the token was consumed (one-time use); NULL if still valid';
