-- =============================================================================
-- ResumAIner — Create education table
-- =============================================================================
-- Stores formal education records: universities, colleges, degrees, programs.
-- Auto-sorted by start_date DESC, end_date DESC NULLS FIRST (DEC-012).
-- Supports soft-delete per SEC-003.
-- =============================================================================

CREATE TABLE education (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES users(id),

    -- Core fields
    institution_name VARCHAR(255)   NOT NULL,
    degree           VARCHAR(100)   NOT NULL,
    field_of_study   VARCHAR(255)   NOT NULL,
    description      TEXT,

    -- Date range
    start_date      DATE            NOT NULL,
    end_date        DATE,
    is_current      BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Optional fields
    location        VARCHAR(255),
    gpa_grade       VARCHAR(20),

    -- Audit timestamps
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,

    -- Soft-delete (SEC-003)
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

-- Index for fast user-scoped queries with soft-delete filtering (SEC-001, SEC-003)
CREATE INDEX idx_education_user_id ON education (user_id, is_deleted);

-- Index for default sort order
CREATE INDEX idx_education_dates ON education (user_id, start_date DESC, end_date DESC NULLS FIRST);

COMMENT ON TABLE  education                   IS 'Formal education records';
COMMENT ON COLUMN education.id                IS 'BIGSERIAL primary key';
COMMENT ON COLUMN education.user_id           IS 'FK to users table — record owner (SEC-001)';
COMMENT ON COLUMN education.institution_name  IS 'School, university, or institution name';
COMMENT ON COLUMN education.degree            IS 'Degree or qualification (Bachelor, Master, PhD)';
COMMENT ON COLUMN education.field_of_study    IS 'Major or specialization';
COMMENT ON COLUMN education.description       IS 'Additional education details (maps to Comment in spec)';
COMMENT ON COLUMN education.start_date        IS 'Study start date';
COMMENT ON COLUMN education.end_date          IS 'Graduation date; NULL = still studying';
COMMENT ON COLUMN education.is_current        IS 'Currently studying at this institution';
COMMENT ON COLUMN education.location          IS 'Institution location';
COMMENT ON COLUMN education.gpa_grade         IS 'GPA or grade (text for flexible format)';
COMMENT ON COLUMN education.created_at        IS 'Record creation timestamp';
COMMENT ON COLUMN education.updated_at        IS 'Last modification timestamp';
COMMENT ON COLUMN education.is_deleted        IS 'Soft-delete flag (SEC-003)';
COMMENT ON COLUMN education.deleted_at        IS 'Soft-delete timestamp (SEC-003)';
