-- Cleanup script for account opening Selenium tests
-- PRESERVE all migration data: customers, products, branches, users, sequences

-- Clean up only Selenium test-created accounts and related transactions
DELETE FROM transactions WHERE id_accounts IN (
    SELECT id FROM accounts WHERE created_by = 'SELENIUM_TEST' OR account_number LIKE 'SELENIUM_%'
);

-- Delete only Selenium test-created accounts
DELETE FROM accounts WHERE created_by = 'SELENIUM_TEST' OR account_number LIKE 'SELENIUM_%';

-- NOTE: Migration data is preserved for reuse:
-- - CUSTOMERS: C1000001-C1000006 remain available
-- - PRODUCTS: TAB001, TAB002, DEP001, PEM001, PEM002 remain available
-- - BRANCHES: HO001, JKT01, BDG01, SBY01, YGY01 remain available  
-- - USERS: cs1, teller1, admin remain available
-- - SEQUENCES: Account number sequence remains intact

-- This allows subsequent Selenium tests to consistently reuse the same base data