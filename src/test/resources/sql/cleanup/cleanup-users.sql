-- Clean up test data before running user management tests
-- Only delete test users, not system users (admin, manager1-2, teller1-3, cs1-3)
DELETE FROM user_roles WHERE id_users IN (
    SELECT id FROM users WHERE 
    (username LIKE '%test%' OR email LIKE '%test%@yopmail.com%') 
    AND username NOT IN ('admin', 'manager1', 'manager2', 'teller1', 'teller2', 'teller3', 'cs1', 'cs2', 'cs3')
);
DELETE FROM user_passwords WHERE id_users IN (
    SELECT id FROM users WHERE 
    (username LIKE '%test%' OR email LIKE '%test%@yopmail.com%') 
    AND username NOT IN ('admin', 'manager1', 'manager2', 'teller1', 'teller2', 'teller3', 'cs1', 'cs2', 'cs3')
);
DELETE FROM users WHERE 
    (username LIKE '%test%' OR email LIKE '%test%@yopmail.com%') 
    AND username NOT IN ('admin', 'manager1', 'manager2', 'teller1', 'teller2', 'teller3', 'cs1', 'cs2', 'cs3');