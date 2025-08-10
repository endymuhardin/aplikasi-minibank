-- Clean up test users/roles created during RBAC tests
DELETE FROM user_roles WHERE id_users IN (
    SELECT id FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%'
);
DELETE FROM user_passwords WHERE id_users IN (
    SELECT id FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%'
);
DELETE FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%';
DELETE FROM roles WHERE role_code LIKE 'SELENIUM_%';