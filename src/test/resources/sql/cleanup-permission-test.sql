-- Cleanup test data for permission management Selenium tests
-- PRESERVE migration permissions (V004__insert_roles_permissions_data.sql)

-- Clean up only Selenium test-created permissions
DELETE FROM role_permissions WHERE id_permissions IN (
    SELECT id FROM permissions WHERE created_by = 'SELENIUM_TEST' OR permission_code LIKE 'SELENIUM_%'
);

DELETE FROM permissions WHERE created_by = 'SELENIUM_TEST' OR permission_code LIKE 'SELENIUM_%';

-- Clean up test permissions with various naming patterns
DELETE FROM permissions WHERE permission_code LIKE 'TEST_%' 
   OR permission_code LIKE '%_TEST_%';

-- NOTE: Migration permissions are preserved:
-- - CUSTOMER_*, ACCOUNT_*, TRANSACTION_*, PRODUCT_*, USER_*, REPORT_*, AUDIT_*, BALANCE_*
-- - These remain available for subsequent Selenium tests