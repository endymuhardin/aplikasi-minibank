-- Clean up test data after account opening tests
-- PRESERVE migration data: customers (C1000001-C1000006), products (TAB001, TAB002, etc.), branches, sequences

-- Clean up only test-created accounts and transactions
DELETE FROM transactions WHERE id_accounts IN (
    SELECT id FROM accounts WHERE created_by = 'TEST' OR account_number LIKE 'TEST_%'
);
DELETE FROM accounts WHERE created_by = 'TEST' OR account_number LIKE 'TEST_%';

-- NOTE: Migration data (customers, products, branches, sequence_numbers) is preserved
-- This allows subsequent tests to reuse the same base data consistently