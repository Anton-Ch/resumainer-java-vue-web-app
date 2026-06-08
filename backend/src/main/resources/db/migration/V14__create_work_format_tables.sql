-- =============================================================================
-- ResumAIner — Create work_format and user_work_format tables
-- =============================================================================
-- work_format: lookup table for preferred work format values (DEC-022).
-- user_work_format: M:N junction table between users and work formats.
-- Both tables are static lookups/data with no soft-delete needed.
-- =============================================================================

-- =============================================================================
-- work_format — lookup table
-- Seeds 8 values from BA data dictionary (V15__seed_work_format_data.sql)
-- =============================================================================
CREATE TABLE work_format (
    id          BIGSERIAL       PRIMARY KEY,
    code        VARCHAR(30)     NOT NULL UNIQUE,
    name        VARCHAR(50)     NOT NULL
);

COMMENT ON TABLE  work_format          IS 'Preferred work format lookup (DEC-022)';
COMMENT ON COLUMN work_format.id       IS 'BIGSERIAL primary key';
COMMENT ON COLUMN work_format.code     IS 'Format code: full-time, part-time, remote, etc.';
COMMENT ON COLUMN work_format.name     IS 'Display name: Full-time, Part-time, Remote';

-- =============================================================================
-- user_work_format — M:N junction table
-- =============================================================================
CREATE TABLE user_work_format (
    id              BIGSERIAL   PRIMARY KEY,
    user_id         UUID        NOT NULL REFERENCES users(id),
    work_format_id  BIGINT      NOT NULL REFERENCES work_format(id),

    -- Composite unique constraint prevents duplicate assignments
    CONSTRAINT uq_user_work_format UNIQUE (user_id, work_format_id)
);

-- Index for fast user-scoped queries
CREATE INDEX idx_user_work_format_user_id ON user_work_format (user_id);

COMMENT ON TABLE  user_work_format                 IS 'M:N junction between users and preferred work formats';
COMMENT ON COLUMN user_work_format.id              IS 'BIGSERIAL primary key';
COMMENT ON COLUMN user_work_format.user_id         IS 'FK to users table';
COMMENT ON COLUMN user_work_format.work_format_id  IS 'FK to work_format table';
