-- =============================================================================
-- ResumAIner — Create saved_resumes table
-- =============================================================================
-- Stores finalized saved resumes for the User Home / Resume Workspace feature.
-- Each resume belongs to a user and stores metadata needed for the DataTable
-- and Resume Details modal.
-- =============================================================================

CREATE TABLE saved_resumes (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    resume_title    VARCHAR(255)    NOT NULL,
    vacancy         VARCHAR(255)    NOT NULL,
    company         VARCHAR(255)    NOT NULL,
    language        VARCHAR(10)     NOT NULL CHECK (language IN ('EN', 'RU')),
    adaptation_level VARCHAR(20)    NOT NULL CHECK (adaptation_level IN ('MINIMAL', 'BALANCED', 'MAXIMUM')),
    created_at      DATE            NOT NULL DEFAULT CURRENT_DATE,
    public_url      VARCHAR(500),
    pdf_url         VARCHAR(500),
    cover_letter    TEXT,
    deleted_at      TIMESTAMP,
    created_by      BIGINT          NOT NULL REFERENCES users(id),
    created_date    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT          REFERENCES users(id),
    updated_date    TIMESTAMP
);

-- Index for fast user-specific queries with soft-delete filtering
CREATE INDEX idx_saved_resumes_user_id ON saved_resumes (user_id, deleted_at);

-- Index for default sort order (newest first)
CREATE INDEX idx_saved_resumes_created_at ON saved_resumes (created_at DESC);

-- Index for search across title, vacancy, company
CREATE INDEX idx_saved_resumes_search ON saved_resumes (user_id, deleted_at, resume_title, vacancy, company);
