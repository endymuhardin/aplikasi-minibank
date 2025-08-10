-- Clean up any existing test users/roles for RBAC tests
DELETE FROM user_roles WHERE id_users IN (
    SELECT id FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%'
);
DELETE FROM user_passwords WHERE id_users IN (
    SELECT id FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%'
);
DELETE FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%';
DELETE FROM roles WHERE role_code LIKE 'SELENIUM_%';

-- No need to insert data here - each test will create its own unique data
-- This script just ensures clean state before tests run