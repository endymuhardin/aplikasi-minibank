-- Clean up test data before running user management tests
DELETE FROM user_roles WHERE id_users IN (SELECT id FROM users WHERE username LIKE '%test%' OR email LIKE '%yopmail.com%');
DELETE FROM user_passwords WHERE id_users IN (SELECT id FROM users WHERE username LIKE '%test%' OR email LIKE '%yopmail.com%');
DELETE FROM users WHERE username LIKE '%test%' OR email LIKE '%yopmail.com%';