-- V37: Add auth hardening columns to users table
--
-- Adds columns required for:
--   - email verification before first login
--   - password login disable for OAuth-only users

ALTER TABLE users
    ADD COLUMN email_verified         BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verified_at      TIMESTAMP,
    ADD COLUMN password_login_enabled BOOLEAN     NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN users.email_verified         IS 'Whether the user has verified their email address';
COMMENT ON COLUMN users.email_verified_at      IS 'Timestamp of email verification';
COMMENT ON COLUMN users.password_login_enabled IS 'Whether password login is allowed (FALSE for OAuth-only accounts)';
