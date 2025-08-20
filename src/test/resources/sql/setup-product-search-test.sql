-- Setup for product search functionality tests
-- LEVERAGE migration products: TAB001, TAB002, DEP001, PEM001, PEM002 from V002__insert_initial_data.sql

-- Clean up only test-specific products, preserve migration products  
DELETE FROM products WHERE product_code LIKE 'SEARCH_%' OR product_code LIKE 'TEST_%';

-- NOTE: Tests can use existing migration products:
-- TAB001: Tabungan Wadiah Basic (TABUNGAN_WADIAH)
-- TAB002: Tabungan Mudharabah Premium (TABUNGAN_MUDHARABAH) 
-- DEP001: Deposito Mudharabah (DEPOSITO_MUDHARABAH)
-- PEM001: Pembiayaan Murabahah (PEMBIAYAAN_MURABAHAH)
-- PEM002: Pembiayaan Musharakah (PEMBIAYAAN_MUSHARAKAH)

-- Only add minimal additional test products if specific search scenarios require them
INSERT INTO products (
    id, product_code, product_name, product_type, product_category, description,
    is_active, is_default, currency,
    minimum_opening_balance, minimum_balance, maximum_balance,
    daily_withdrawal_limit, monthly_transaction_limit,
    profit_sharing_ratio, profit_sharing_type, profit_distribution_frequency,
    nisbah_customer, nisbah_bank, is_shariah_compliant,
    monthly_maintenance_fee, atm_withdrawal_fee, inter_bank_transfer_fee,
    below_minimum_balance_fee, account_closure_fee,
    free_transactions_per_month, excess_transaction_fee,
    allow_overdraft, require_maintaining_balance,
    min_customer_age, allowed_customer_types, required_documents,
    launch_date, created_by
) VALUES
-- Only add if search needs inactive product
(gen_random_uuid(), 'SEARCH_INACTIVE', 'Inactive Test Product', 'TABUNGAN_WADIAH', 'Tabungan Syariah',
 'Inactive product for search filter testing', false, false, 'IDR',
 50000, 10000, NULL, 5000000, 50,
 0.0200, 'WADIAH', 'MONTHLY', NULL, NULL, true,
 2500, 5000, 7500, 10000, 0, 10, 2500, false, true, 17, 'PERSONAL', 'KTP',
 CURRENT_DATE, 'SYSTEM');