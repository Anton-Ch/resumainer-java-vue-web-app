-- V39: Create oauth_accounts table for Google OAuth2 login
--
-- Links external OAuth provider identities to internal app accounts.
-- Only GOOGLE is supported in this feature.
-- Provider access/refresh tokens are NOT stored in this feature.

CREATE TABLE oauth_accounts (
    id                     BIGSERIAL    PRIMARY KEY,
    user_id                UUID         NOT NULL REFERENCES users(id),
    provider               VARCHAR(40)  NOT NULL,
    provider_subject       VARCHAR(255) NOT NULL,
    provider_email         VARCHAR(255) NOT NULL,
    provider_email_verified BOOLEAN     NOT NULL,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP,

    CONSTRAINT ck_oauth_accounts_provider CHECK (provider IN ('GOOGLE')),
    CONSTRAINT uq_oauth_accounts_provider_subject UNIQUE (provider, provider_subject)
);

CREATE INDEX idx_oauth_accounts_user_id         ON oauth_accounts(user_id);
CREATE INDEX idx_oauth_accounts_provider_email  ON oauth_accounts(provider_email);

COMMENT ON TABLE  oauth_accounts                       IS 'External OAuth provider identities linked to app accounts';
COMMENT ON COLUMN oauth_accounts.user_id                IS 'FK to users — the linked app account';
COMMENT ON COLUMN oauth_accounts.provider               IS 'OAuth provider name (GOOGLE)';
COMMENT ON COLUMN oauth_accounts.provider_subject       IS 'Unique subject identifier from the provider';
COMMENT ON COLUMN oauth_accounts.provider_email         IS 'Email from the provider';
COMMENT ON COLUMN oauth_accounts.provider_email_verified IS 'Whether the provider verified the email';
COMMENT ON COLUMN oauth_accounts.updated_at             IS 'Last modification timestamp';
