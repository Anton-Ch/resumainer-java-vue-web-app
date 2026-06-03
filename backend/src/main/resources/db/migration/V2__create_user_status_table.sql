-- V2: Create user_status lookup table
-- BIGSERIAL PK — lookup tables use auto-increment, not UUID

CREATE TABLE user_status (
    id    BIGSERIAL    PRIMARY KEY,
    code  VARCHAR(20)  NOT NULL UNIQUE,
    name  VARCHAR(50)  NOT NULL
);

COMMENT ON TABLE  user_status      IS 'Account status lookup (ACTIVE, BLOCKED)';
COMMENT ON COLUMN user_status.code IS 'Unique status identifier, e.g. ACTIVE, BLOCKED';
COMMENT ON COLUMN user_status.name IS 'Human-readable status name';
