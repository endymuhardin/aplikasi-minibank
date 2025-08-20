-- Setup test data for permission management Selenium tests
-- LEVERAGE migration permission system from V004__insert_roles_permissions_data.sql

-- Clean up only test-created permissions, preserve migration permissions
DELETE FROM role_permissions WHERE id_permissions IN (
    SELECT id FROM permissions WHERE permission_code LIKE '%TEST%'
);
DELETE FROM permissions WHERE permission_code LIKE '%TEST%';

-- NOTE: Migration provides comprehensive permission system:
--
-- PERMISSIONS (V004): Use existing permissions instead of creating test ones
-- - CUSTOMER_*: VIEW, CREATE, UPDATE (customer management)
-- - ACCOUNT_*: VIEW, CREATE, UPDATE, APPROVE (account management)  
-- - TRANSACTION_*: VIEW, DEPOSIT, WITHDRAWAL, TRANSFER, APPROVE (transactions)
-- - PRODUCT_VIEW: View banking products
-- - USER_*: VIEW, CREATE, UPDATE, DEACTIVATE (user management)
-- - REPORT_VIEW, AUDIT_VIEW, BALANCE_VIEW (reporting and monitoring)
--
-- Permission tests should primarily validate existing migration permissions
-- Create minimal test permissions only for unique test scenarios

-- Add test permissions only if needed for specific permission management UI tests
INSERT INTO permissions (id, permission_code, permission_name, permission_category, description, created_by, created_date)
VALUES
    ('11111111-1111-1111-1111-111111111001', 'SELENIUM_TEST_EDIT', 'Selenium Test Edit', 'SELENIUM_TEST', 'Permission for Selenium edit testing', 'SELENIUM_TEST', NOW()),
    ('11111111-1111-1111-1111-111111111002', 'SELENIUM_TEST_VIEW', 'Selenium Test View', 'SELENIUM_TEST', 'Permission for Selenium view testing', 'SELENIUM_TEST', NOW());