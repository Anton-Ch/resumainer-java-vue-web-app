-- V41: Migrate existing test users for auth hardening
--
-- CAPSTONE TEST DATA ONLY:
-- This resets existing test user passwords to Aa123456.
-- This is not safe for real production user data.
--
-- Rules:
--   - marks all existing users as email verified
--   - sets password hash to encoded Aa123456 (BCrypt cost=12)
--   - enables password login for all existing users
--   - preserves: roles, statuses, permissions, privileged flags,
--     deletion flags, and all ownership relationships
--   - does NOT delete any data

UPDATE users
SET
    email_verified         = TRUE,
    email_verified_at      = COALESCE(email_verified_at, created_at, NOW()),
    password_hash          = '$2a$12$b6Flut1MIqFT5gQNqWZwtOWAIxbDDZHNW.tDRA4ppSCcZGHIXJTyG',
    password_login_enabled = TRUE,
    updated_at             = NOW()
WHERE
    is_deleted = FALSE;
