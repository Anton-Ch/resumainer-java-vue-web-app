-- =============================================================================
-- ResumAIner — Create project table
-- =============================================================================
-- Stores project and volunteering records. Captures personal, academic,
-- professional, and volunteer experience. Auto-sorted by start_date DESC,
-- end_date DESC NULLS FIRST (DEC-012). Supports soft-delete per SEC-003.
-- =============================================================================

CREATE TABLE project (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES users(id),

    -- Core fields
    project_name    VARCHAR(255)    NOT NULL,
    role            VARCHAR(255),
    description     TEXT            NOT NULL,
    location        VARCHAR(255)    NOT NULL,

    -- Date range
    start_date      DATE            NOT NULL,
    end_date        DATE,
    is_ongoing      BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Optional fields
    project_url     VARCHAR(500),

    -- Audit timestamps
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,

    -- Soft-delete (SEC-003)
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

-- Index for fast user-scoped queries with soft-delete filtering (SEC-001, SEC-003)
CREATE INDEX idx_project_user_id ON project (user_id, is_deleted);

-- Index for default sort order
CREATE INDEX idx_project_dates ON project (user_id, start_date DESC, end_date DESC NULLS FIRST);

COMMENT ON TABLE  project                  IS 'Project and volunteering records';
COMMENT ON COLUMN project.id               IS 'BIGSERIAL primary key';
COMMENT ON COLUMN project.user_id          IS 'FK to users table — record owner (SEC-001)';
COMMENT ON COLUMN project.project_name     IS 'Project or activity name';
COMMENT ON COLUMN project.role             IS 'User role in project (defaults to Participant)';
COMMENT ON COLUMN project.description      IS 'Project description and contributions';
COMMENT ON COLUMN project.location         IS 'Project location';
COMMENT ON COLUMN project.start_date       IS 'Project start date';
COMMENT ON COLUMN project.end_date         IS 'End date; NULL = ongoing';
COMMENT ON COLUMN project.is_ongoing       IS 'Ongoing project flag';
COMMENT ON COLUMN project.project_url      IS 'Project URL or repository link';
COMMENT ON COLUMN project.created_at       IS 'Record creation timestamp';
COMMENT ON COLUMN project.updated_at       IS 'Last modification timestamp';
COMMENT ON COLUMN project.is_deleted       IS 'Soft-delete flag (SEC-003)';
COMMENT ON COLUMN project.deleted_at       IS 'Soft-delete timestamp (SEC-003)';
