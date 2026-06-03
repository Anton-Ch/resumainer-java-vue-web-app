-- V3: Create user_permission lookup table
-- BIGSERIAL PK — lookup tables use auto-increment, not UUID

CREATE TABLE user_permission (
    id    BIGSERIAL    PRIMARY KEY,
    code  VARCHAR(20)  NOT NULL UNIQUE,
    name  VARCHAR(50)  NOT NULL
);

COMMENT ON TABLE  user_permission      IS 'Generation permission lookup (ALLOWED, FORBIDDEN)';
COMMENT ON COLUMN user_permission.code IS 'Unique permission identifier, e.g. ALLOWED, FORBIDDEN';
COMMENT ON COLUMN user_permission.name IS 'Human-readable permission name';
