-- V1: Create role lookup table
-- BIGSERIAL PK — lookup tables use auto-increment, not UUID

CREATE TABLE role (
    id    BIGSERIAL    PRIMARY KEY,
    code  VARCHAR(20)  NOT NULL UNIQUE,
    name  VARCHAR(50)  NOT NULL
);

COMMENT ON TABLE  role      IS 'User role lookup (USER, ADMIN)';
COMMENT ON COLUMN role.code IS 'Unique role identifier, e.g. USER, ADMIN';
COMMENT ON COLUMN role.name IS 'Human-readable role name';
