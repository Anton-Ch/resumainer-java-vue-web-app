-- V4: Create language lookup table
-- BIGSERIAL PK — lookup tables use auto-increment, not UUID

CREATE TABLE language (
    id    BIGSERIAL    PRIMARY KEY,
    code  VARCHAR(10)  NOT NULL UNIQUE,
    name  VARCHAR(50)  NOT NULL
);

COMMENT ON TABLE  language      IS 'Language lookup (EN, RU)';
COMMENT ON COLUMN language.code IS 'Unique language code, e.g. EN, RU';
COMMENT ON COLUMN language.name IS 'Human-readable language name, e.g. English, Russian';
