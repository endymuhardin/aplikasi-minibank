-- Cleanup script for account statement Selenium tests
-- Remove only test-created data, preserve migration data

-- Clean up test transactions first (due to foreign key constraints)
DELETE FROM transactions WHERE created_by = 'SELENIUM_STATEMENT_TEST';

-- Clean up test accounts
DELETE FROM accounts WHERE created_by = 'SELENIUM_STATEMENT_TEST' OR account_number LIKE 'STMT_%';

-- Reset sequence numbers to avoid conflicts with future tests
-- Note: Only reset if no other test data exists
UPDATE sequence_numbers 
SET last_number = GREATEST(last_number - 20, 
    (SELECT COALESCE(MAX(CAST(SUBSTRING(transaction_number FROM '[0-9]+') AS INTEGER)), 0) 
     FROM transactions 
     WHERE transaction_number ~ '^[A-Z]+[0-9]+$'))
WHERE sequence_name = 'TRANSACTION_NUMBER';

-- Verify cleanup
-- SELECT 'Cleanup completed' as status, 
--        (SELECT COUNT(*) FROM accounts WHERE created_by = 'SELENIUM_STATEMENT_TEST') as remaining_accounts,
--        (SELECT COUNT(*) FROM transactions WHERE created_by = 'SELENIUM_STATEMENT_TEST') as remaining_transactions;