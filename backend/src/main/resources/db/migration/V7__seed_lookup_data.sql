-- V7: Seed lookup tables with initial values
-- These are referenced by users table via FKs

-- Roles
INSERT INTO role (code, name) VALUES
    ('USER',  'Regular User'),
    ('ADMIN', 'Administrator');

-- User statuses
INSERT INTO user_status (code, name) VALUES
    ('ACTIVE',  'Active'),
    ('BLOCKED', 'Blocked');

-- Generation permissions
INSERT INTO user_permission (code, name) VALUES
    ('ALLOWED',   'Allowed'),
    ('FORBIDDEN', 'Forbidden');

-- Languages
INSERT INTO language (code, name) VALUES
    ('EN', 'English'),
    ('RU', 'Russian');
