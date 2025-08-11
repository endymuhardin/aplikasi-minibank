-- Clean up any existing test products for search functionality
DELETE FROM products WHERE product_code LIKE 'SEARCH_%';

-- Insert test products for search functionality
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
(gen_random_uuid(), 'SEARCH_SAV001', 'Search Test Savings', 'SAVINGS', 'Savings', 'Savings product for search testing',
 true, false, 'IDR', 50000, 10000, 0.0250, 'MUDHARABAH', 'MONTHLY', 0.7000, 0.3000, true,
 2500, 5000, 7500, 10000, 0, 10, 2500, false, true, 'PERSONAL', 'KTP',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM'),
(gen_random_uuid(), 'SEARCH_CHK001', 'Search Test Checking', 'CHECKING', 'Checking', 'Checking product for search testing',
 true, false, 'IDR', 100000, 50000, 0.0150, 'WADIAH', 'MONTHLY', NULL, NULL, true,
 5000, 5000, 7500, 15000, 10000, 20, 3000, true, true, 'PERSONAL', 'KTP',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM');