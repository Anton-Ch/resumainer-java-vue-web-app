-- =============================================================================
-- ResumAIner — Add generation response bullet tables (Feature 008, PG1)
-- =============================================================================
-- Stores AI-generated and user-edited bullet points for work experience and
-- project entries as first-class ordered items per FR-008-001, FR-008-002.
--
-- IMPORTANT: experience_id / project_id are UUID because the parent tables
-- (generation_response_experience, generation_response_project) use UUID PKs
-- per V20 migration (B15 guard).
-- =============================================================================

-- =============================================================================
-- WORK EXPERIENCE BULLET POINTS
-- =============================================================================
CREATE TABLE generation_response_experience_bullet (
    id              BIGSERIAL       PRIMARY KEY,
    experience_id   UUID            NOT NULL REFERENCES generation_response_experience(id) ON DELETE CASCADE,
    bullet_order    INT             NOT NULL,
    bullet_text     VARCHAR(250)    NOT NULL CHECK (TRIM(bullet_text) <> ''),
    is_edited       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (experience_id, bullet_order)
);

COMMENT ON TABLE  generation_response_experience_bullet              IS 'Ordered bullet points for generated work experience entries';
COMMENT ON COLUMN generation_response_experience_bullet.experience_id IS 'FK to generation_response_experience (UUID — V20)';
COMMENT ON COLUMN generation_response_experience_bullet.bullet_order  IS 'Deterministic order from AI response, preserved through save';
COMMENT ON COLUMN generation_response_experience_bullet.bullet_text   IS 'Non-empty bullet text, max 250 chars per FR-008-007';
COMMENT ON COLUMN generation_response_experience_bullet.is_edited     IS 'TRUE if user modified the bullet in Review';

CREATE INDEX idx_exp_bullet_experience_id ON generation_response_experience_bullet (experience_id);

-- =============================================================================
-- PROJECT BULLET POINTS
-- =============================================================================
CREATE TABLE generation_response_project_bullet (
    id              BIGSERIAL       PRIMARY KEY,
    project_id      UUID            NOT NULL REFERENCES generation_response_project(id) ON DELETE CASCADE,
    bullet_order    INT             NOT NULL,
    bullet_text     VARCHAR(250)    NOT NULL CHECK (TRIM(bullet_text) <> ''),
    is_edited       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, bullet_order)
);

COMMENT ON TABLE  generation_response_project_bullet              IS 'Ordered bullet points for generated project entries';
COMMENT ON COLUMN generation_response_project_bullet.project_id    IS 'FK to generation_response_project (UUID — V20)';
COMMENT ON COLUMN generation_response_project_bullet.bullet_order  IS 'Deterministic order from AI response';
COMMENT ON COLUMN generation_response_project_bullet.bullet_text   IS 'Non-empty bullet text, max 250 chars';
COMMENT ON COLUMN generation_response_project_bullet.is_edited     IS 'TRUE if user modified the bullet in Review';

CREATE INDEX idx_proj_bullet_project_id ON generation_response_project_bullet (project_id);
