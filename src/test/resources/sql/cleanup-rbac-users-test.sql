-- Cleanup script for Selenium RBAC tests  
-- PRESERVE migration RBAC system (roles, permissions, users from V004)

-- Clean up only Selenium test-created RBAC data
DELETE FROM user_roles WHERE id_users IN (
    SELECT id FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%'
);
DELETE FROM user_passwords WHERE id_users IN (
    SELECT id FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%'
);
DELETE FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%';
DELETE FROM roles WHERE role_code LIKE 'SELENIUM_%';

-- NOTE: Migration RBAC data is preserved for reuse:
-- - ROLES: CUSTOMER_SERVICE, TELLER, BRANCH_MANAGER remain available
-- - USERS: admin, manager1-2, teller1-3, cs1-3 remain available  
-- - PERMISSIONS: Complete permission system remains intact
-- - USER_ROLES: Migration user role assignments remain intact

-- This ensures consistent RBAC state for subsequent Selenium tests