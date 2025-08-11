-- Clean up any existing SAV001 product
DELETE FROM products WHERE product_code = 'SAV001';

-- Insert a test product with SAV001 for duplicate validation testing
INSERT INTO products (
    id, product_code, product_name, product_type, product_category,
    is_active, currency,
    minimum_opening_balance, minimum_balance,
    profit_sharing_ratio, profit_sharing_type, profit_distribution_frequency,
    nisbah_customer, nisbah_bank, is_shariah_compliant,
    monthly_maintenance_fee, atm_withdrawal_fee, inter_bank_transfer_fee,
    below_minimum_balance_fee, account_closure_fee,
    free_transactions_per_month, excess_transaction_fee,
    allow_overdraft, require_maintaining_balance,
    allowed_customer_types, required_documents,
    created_date, updated_date, created_by
) VALUES (
    gen_random_uuid(), 'SAV001', 'Existing Savings Product', 'SAVINGS', 'Test Category',
    true, 'IDR', 50000, 10000, 0.0275, 'MUDHARABAH', 'MONTHLY', 0.7000, 0.3000, true,
    2500, 5000, 7500, 10000, 0, 10, 2500, false, true, 'PERSONAL', 'KTP',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM'
);