-- =============================================================================
-- ResumAIner — Create work_experience table
-- =============================================================================
-- Stores the user's work history entries. Each record represents one job
-- position held by the user. Auto-sorted by start_date DESC, end_date DESC
-- NULLS FIRST (DEC-012). Supports soft-delete per SEC-003.
-- =============================================================================

CREATE TABLE work_experience (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES users(id),

    -- Core fields
    job_title       VARCHAR(255)    NOT NULL,
    company_name    VARCHAR(255)    NOT NULL,
    description     TEXT            NOT NULL,
    location        VARCHAR(255)    NOT NULL,

    -- Date range
    start_date      DATE            NOT NULL,
    end_date        DATE,
    is_current      BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Optional fields
    company_url     VARCHAR(500),

    -- Audit timestamps
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,

    -- Soft-delete (SEC-003)
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

-- Index for fast user-scoped queries with soft-delete filtering (SEC-001, SEC-003)
CREATE INDEX idx_work_experience_user_id ON work_experience (user_id, is_deleted);

-- Index for default sort order (start_date DESC, end_date DESC NULLS FIRST)
CREATE INDEX idx_work_experience_dates ON work_experience (user_id, start_date DESC, end_date DESC NULLS FIRST);

COMMENT ON TABLE  work_experience                    IS 'User work history records';
COMMENT ON COLUMN work_experience.id                 IS 'BIGSERIAL primary key';
COMMENT ON COLUMN work_experience.user_id            IS 'FK to users table — record owner (SEC-001)';
COMMENT ON COLUMN work_experience.job_title          IS 'Position/job title';
COMMENT ON COLUMN work_experience.company_name       IS 'Employer name';
COMMENT ON COLUMN work_experience.description        IS 'Role description, responsibilities, achievements';
COMMENT ON COLUMN work_experience.location           IS 'Work location (city, remote)';
COMMENT ON COLUMN work_experience.start_date         IS 'Employment start date';
COMMENT ON COLUMN work_experience.end_date           IS 'Employment end date; NULL = current job';
COMMENT ON COLUMN work_experience.is_current         IS 'Currently employed at this company';
COMMENT ON COLUMN work_experience.company_url        IS 'Company profile URL (MVP per user decision)';
COMMENT ON COLUMN work_experience.created_at         IS 'Record creation timestamp';
COMMENT ON COLUMN work_experience.updated_at         IS 'Last modification timestamp';
COMMENT ON COLUMN work_experience.is_deleted         IS 'Soft-delete flag (SEC-003)';
COMMENT ON COLUMN work_experience.deleted_at         IS 'Soft-delete timestamp (SEC-003)';
