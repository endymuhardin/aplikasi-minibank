-- Transaction Test Data Cleanup
-- Removes test data created with TXN_ prefix for clean test isolation

-- Clean up test transactions (if any were created during tests)
DELETE FROM transactions WHERE created_by = 'TEST_SYSTEM' AND description LIKE '%TXN Test%';

-- Clean up test accounts with TXN_ prefix
DELETE FROM accounts WHERE account_number LIKE 'TXN_%' OR account_name LIKE '%TXN Test%';

-- Note: We don't delete customers since we're reusing existing seed data (C1000001, C1000002, etc.)
-- The seed customers and their original accounts (A2000001-A2000004) remain untouched