-- Clean up existing test data
DELETE FROM products WHERE product_code LIKE 'TEST%' OR product_code LIKE 'STATUS%';

-- Insert small sample dataset (5 products total - less than default page size of 10)
-- This ensures all products fit on page 1, eliminating pagination issues

INSERT INTO products (
    id, product_code, product_name, product_type, product_category, description,
    is_active, is_default, currency,
    minimum_opening_balance, minimum_balance,
    profit_sharing_ratio, profit_sharing_type, profit_distribution_frequency,
    nisbah_customer, nisbah_bank, is_shariah_compliant,
    monthly_maintenance_fee, atm_withdrawal_fee, inter_bank_transfer_fee,
    below_minimum_balance_fee, account_closure_fee,
    free_transactions_per_month, excess_transaction_fee,
    allow_overdraft, require_maintaining_balance,
    allowed_customer_types, required_documents,
    created_date, updated_date, created_by
) VALUES
(gen_random_uuid(), 'TEST001', 'Test Savings Account', 'SAVINGS', 'Personal Banking', 'Basic savings account for testing',
 true, false, 'IDR', 50000, 10000, 0.0200, 'WADIAH', 'MONTHLY', NULL, NULL, true,
 2500, 5000, 7500, 10000, 0, 10, 2500, false, true, 'PERSONAL', 'KTP',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'TEST002', 'Test Checking Account', 'CHECKING', 'Personal Banking', 'Basic checking account for testing',
 true, false, 'IDR', 100000, 50000, 0.0150, 'WADIAH', 'MONTHLY', NULL, NULL, true,
 5000, 5000, 7500, 15000, 10000, 20, 3000, true, true, 'PERSONAL', 'KTP',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'TEST003', 'Test Loan Product', 'SAVINGS', 'Lending', 'Basic loan product for testing',
 false, false, 'IDR', 1000000, 500000, 0.0300, 'MUDHARABAH', 'MONTHLY', 0.7000, 0.3000, true,
 15000, 5000, 5000, 25000, 25000, 50, 5000, false, true, 'PERSONAL', 'KTP, NPWP',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'TEST004', 'Test CD Product', 'SAVINGS', 'Investment', 'CD product for testing',
 true, false, 'IDR', 500000, 100000, 0.0350, 'MUDHARABAH', 'QUARTERLY', 0.7000, 0.3000, true,
 0, 0, 5000, 25000, 0, 25, 2500, false, true, 'PERSONAL', 'KTP, NPWP',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'TEST005', 'Test Credit Card', 'SAVINGS', 'Credit Products', 'Credit card product for testing',
 false, false, 'IDR', 200000, 50000, 0.0100, 'WADIAH', 'MONTHLY', NULL, NULL, true,
 10000, 7500, 7500, 20000, 15000, 15, 5000, true, false, 'PERSONAL', 'KTP',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM');