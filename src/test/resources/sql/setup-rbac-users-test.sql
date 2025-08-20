-- Setup script for Selenium RBAC tests
-- LEVERAGE migration RBAC system from V004__insert_roles_permissions_data.sql

-- Clean up only Selenium test-created RBAC data, preserve migration RBAC
DELETE FROM user_roles WHERE id_users IN (
    SELECT id FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%'
);
DELETE FROM user_passwords WHERE id_users IN (
    SELECT id FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%'  
);
DELETE FROM users WHERE username LIKE 'sel_%' OR username LIKE 'search_%';
DELETE FROM roles WHERE role_code LIKE 'SELENIUM_%';

-- NOTE: Migration provides complete RBAC system for Selenium tests:
--
-- ROLES (V004): Use existing roles instead of creating custom ones
-- - CUSTOMER_SERVICE: Customer registration, account management permissions
-- - TELLER: Transaction processing, balance inquiry permissions  
-- - BRANCH_MANAGER: All permissions (reports, approvals, user management)
--
-- USERS (V004): Use existing users with known credentials
-- - admin: Branch Manager (password: minibank123)
-- - manager1-2: Branch Managers (password: minibank123)
-- - teller1-3: Tellers (password: minibank123) 
-- - cs1-3: Customer Service staff (password: minibank123)
--
-- PERMISSIONS (V004): Comprehensive permission system already configured
-- - CUSTOMER_*, ACCOUNT_*, TRANSACTION_*, PRODUCT_*, USER_*, REPORT_*, AUDIT_*
--
-- Selenium tests should primarily use these migration entities
-- LoginHelper already leverages this system effectively