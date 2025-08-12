-- Cleanup test data for dashboard tests
DELETE FROM user_passwords WHERE id_users IN (SELECT id FROM users WHERE username IN ('dashuser', 'statsuser', 'manager', 'cs', 'teller'));
DELETE FROM user_roles WHERE id_users IN (SELECT id FROM users WHERE username IN ('dashuser', 'statsuser', 'manager', 'cs', 'teller'));
DELETE FROM users WHERE username IN ('dashuser', 'statsuser', 'manager', 'cs', 'teller');