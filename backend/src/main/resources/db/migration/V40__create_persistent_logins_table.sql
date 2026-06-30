-- V40: Create persistent_logins table for Spring Security remember-me
--
-- Standard Spring Security persistent token remember-me table.
-- username stores email (the login identifier), sized to match users.email VARCHAR(255).
-- No FK constraint — Spring Security manages this table directly.

CREATE TABLE persistent_logins (
    username  VARCHAR(255) NOT NULL,
    series    VARCHAR(64)  PRIMARY KEY,
    token     VARCHAR(64)  NOT NULL,
    last_used TIMESTAMP    NOT NULL
);

COMMENT ON TABLE  persistent_logins              IS 'Spring Security persistent remember-me tokens';
COMMENT ON COLUMN persistent_logins.username     IS 'Login identifier (email) — matches users.email length';
COMMENT ON COLUMN persistent_logins.series       IS 'Series identifier — primary key per Spring Security spec';
COMMENT ON COLUMN persistent_logins.token        IS 'Authentication token per Spring Security spec';
COMMENT ON COLUMN persistent_logins.last_used    IS 'Timestamp of last use';
