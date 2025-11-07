-- =========================================================================
-- Reset Customer Data for Tutorial Practice
-- =========================================================================
-- Purpose: Clean all customer-related data while keeping users, products,
--          and branches intact. This allows users to practice the complete
--          customer onboarding workflow from the user manual.
--
-- What this script does:
-- ✅ Deletes all transactions, accounts, customers (personal/corporate)
-- ✅ Deletes all approval requests
-- ✅ Resets sequence numbers for customer, account, and transaction numbers
-- ✅ Keeps users, roles, permissions (authentication system)
-- ✅ Keeps products (banking products configuration)
-- ✅ Keeps branches (branch information)
--
-- Usage:
--   # Via Docker (recommended)
--   docker exec -i aplikasi-minibank-postgres-1 psql -U minibank -d pgminibank < scripts/reset-customer-data.sql
--
--   # Direct psql connection
--   psql -U minibank -d pgminibank -h localhost -p 2345 -f scripts/reset-customer-data.sql
-- =========================================================================

BEGIN;

-- Show what we're about to delete
DO $$
DECLARE
    transaction_count INTEGER;
    approval_count INTEGER;
    account_count INTEGER;
    personal_count INTEGER;
    corporate_count INTEGER;
    customer_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO transaction_count FROM transactions;
    SELECT COUNT(*) INTO approval_count FROM approval_requests;
    SELECT COUNT(*) INTO account_count FROM accounts;
    SELECT COUNT(*) INTO personal_count FROM personal_customers;
    SELECT COUNT(*) INTO corporate_count FROM corporate_customers;
    SELECT COUNT(*) INTO customer_count FROM customers;

    RAISE NOTICE '';
    RAISE NOTICE '========================================================================';
    RAISE NOTICE 'RESET CUSTOMER DATA - SUMMARY';
    RAISE NOTICE '========================================================================';
    RAISE NOTICE 'Records to be deleted:';
    RAISE NOTICE '  - Transactions:        % records', transaction_count;
    RAISE NOTICE '  - Approval Requests:   % records', approval_count;
    RAISE NOTICE '  - Accounts:            % records', account_count;
    RAISE NOTICE '  - Personal Customers:  % records', personal_count;
    RAISE NOTICE '  - Corporate Customers: % records', corporate_count;
    RAISE NOTICE '  - Total Customers:     % records', customer_count;
    RAISE NOTICE '';
    RAISE NOTICE 'Preserved data:';
    RAISE NOTICE '  ✅ Users & Authentication (users, roles, permissions)';
    RAISE NOTICE '  ✅ Banking Products (products table)';
    RAISE NOTICE '  ✅ Branches (branches table)';
    RAISE NOTICE '========================================================================';
    RAISE NOTICE '';
END $$;

-- Step 1: Delete all transactions (depends on accounts)
DELETE FROM transactions;
RAISE NOTICE '✓ Deleted all transactions';

-- Step 2: Delete all approval requests (depends on customers/accounts)
DELETE FROM approval_requests;
RAISE NOTICE '✓ Deleted all approval requests';

-- Step 3: Delete all accounts (depends on customers)
DELETE FROM accounts;
RAISE NOTICE '✓ Deleted all accounts';

-- Step 4: Delete personal customers (joined table inheritance)
DELETE FROM personal_customers;
RAISE NOTICE '✓ Deleted all personal customers';

-- Step 5: Delete corporate customers (joined table inheritance)
DELETE FROM corporate_customers;
RAISE NOTICE '✓ Deleted all corporate customers';

-- Step 6: Delete base customers table
DELETE FROM customers;
RAISE NOTICE '✓ Deleted all base customers';

-- Step 7: Reset sequence numbers for fresh start
UPDATE sequence_numbers
SET last_number = 1000010
WHERE sequence_name = 'CUSTOMER';
RAISE NOTICE '✓ Reset CUSTOMER sequence to 1000010 (next: C1000011)';

UPDATE sequence_numbers
SET last_number = 2000006
WHERE sequence_name = 'ACCOUNT_NUMBER';
RAISE NOTICE '✓ Reset ACCOUNT_NUMBER sequence to 2000006 (next: A2000007)';

UPDATE sequence_numbers
SET last_number = 0
WHERE sequence_name = 'CORPORATE_ACCOUNT_NUMBER';
RAISE NOTICE '✓ Reset CORPORATE_ACCOUNT_NUMBER sequence to 0 (next: CORP0000001)';

UPDATE sequence_numbers
SET last_number = 3000000
WHERE sequence_name = 'TRANSACTION_NUMBER';
RAISE NOTICE '✓ Reset TRANSACTION_NUMBER sequence to 3000000 (next: T3000001)';

-- Verify final state
DO $$
DECLARE
    user_count INTEGER;
    product_count INTEGER;
    branch_count INTEGER;
    customer_seq_next VARCHAR(20);
    account_seq_next VARCHAR(20);
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO product_count FROM products;
    SELECT COUNT(*) INTO branch_count FROM branches;

    -- Get next sequence values
    SELECT prefix || LPAD((last_number + 1)::TEXT, 7, '0') INTO customer_seq_next
    FROM sequence_numbers WHERE sequence_name = 'CUSTOMER';

    SELECT prefix || LPAD((last_number + 1)::TEXT, 7, '0') INTO account_seq_next
    FROM sequence_numbers WHERE sequence_name = 'ACCOUNT_NUMBER';

    RAISE NOTICE '';
    RAISE NOTICE '========================================================================';
    RAISE NOTICE 'RESET COMPLETE - VERIFICATION';
    RAISE NOTICE '========================================================================';
    RAISE NOTICE 'Customer data: ALL DELETED ✓';
    RAISE NOTICE '';
    RAISE NOTICE 'Preserved system data:';
    RAISE NOTICE '  - Users:    % active', user_count;
    RAISE NOTICE '  - Products: % available', product_count;
    RAISE NOTICE '  - Branches: % configured', branch_count;
    RAISE NOTICE '';
    RAISE NOTICE 'Next generated numbers:';
    RAISE NOTICE '  - Customer Number: %', customer_seq_next;
    RAISE NOTICE '  - Account Number:  %', account_seq_next;
    RAISE NOTICE '';
    RAISE NOTICE 'System is ready for tutorial practice! ✅';
    RAISE NOTICE '========================================================================';
    RAISE NOTICE '';
END $$;

COMMIT;
