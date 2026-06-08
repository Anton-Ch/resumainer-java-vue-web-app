-- =============================================================================
-- ResumAIner — Create course_certificate table
-- =============================================================================
-- Stores courses, certificates, and professional training records.
-- Mandatory section per DEC-018. Supports server-side pagination with
-- LIMIT/OFFSET for up to 300 records. Soft-delete per SEC-003.
-- =============================================================================

CREATE TABLE course_certificate (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES users(id),

    -- Core fields
    name            VARCHAR(255)    NOT NULL,
    provider        VARCHAR(255)    NOT NULL,
    description     TEXT,
    course_focus    VARCHAR(255),

    -- Date range
    start_date      DATE            NOT NULL,
    end_date        DATE,

    -- Optional fields
    credential_url  VARCHAR(500),

    -- Audit timestamps
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,

    -- Soft-delete (SEC-003)
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

-- Index for fast user-scoped queries with soft-delete filtering (SEC-001, SEC-003)
CREATE INDEX idx_course_certificate_user_id ON course_certificate (user_id, is_deleted);

-- Index for default sort order (start_date DESC)
CREATE INDEX idx_course_certificate_dates ON course_certificate (user_id, start_date DESC);

-- Index for search across name, provider, and course_focus
CREATE INDEX idx_course_certificate_search ON course_certificate (user_id, name, provider, course_focus);

COMMENT ON TABLE  course_certificate                IS 'Courses, certificates, and professional training records';
COMMENT ON COLUMN course_certificate.id             IS 'BIGSERIAL primary key';
COMMENT ON COLUMN course_certificate.user_id        IS 'FK to users table — record owner (SEC-001)';
COMMENT ON COLUMN course_certificate.name           IS 'Course or certificate name';
COMMENT ON COLUMN course_certificate.provider       IS 'Provider or issuer (Coursera, Udemy)';
COMMENT ON COLUMN course_certificate.description    IS 'Course description and details';
COMMENT ON COLUMN course_certificate.course_focus   IS 'Key skills/topics covered (maps to skills in prototype)';
COMMENT ON COLUMN course_certificate.start_date     IS 'Course start date';
COMMENT ON COLUMN course_certificate.end_date       IS 'Completion date; NULL = in progress';
COMMENT ON COLUMN course_certificate.credential_url IS 'Link to credential or certificate verification';
COMMENT ON COLUMN course_certificate.created_at     IS 'Record creation timestamp';
COMMENT ON COLUMN course_certificate.updated_at     IS 'Last modification timestamp';
COMMENT ON COLUMN course_certificate.is_deleted     IS 'Soft-delete flag (SEC-003)';
COMMENT ON COLUMN course_certificate.deleted_at     IS 'Soft-delete timestamp (SEC-003)';
