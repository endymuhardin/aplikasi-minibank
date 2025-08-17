-- Setup script for account opening tests
-- This script creates test data needed for account opening functionality

-- Insert test branches (required for customers)
INSERT INTO branches (id, branch_code, branch_name, is_main_branch, status, address, city, country, postal_code, phone_number, email, manager_name, created_date, created_by) 
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'MAIN', 'Main Branch', true, 'ACTIVE', 'Jl. Sudirman No. 1', 'Jakarta', 'Indonesia', '10110', '021-1234567', 'main@minibank.com', 'Manager One', CURRENT_TIMESTAMP, 'SYSTEM'),
    ('22222222-2222-2222-2222-222222222222', 'BRANCH2', 'Secondary Branch', false, 'ACTIVE', 'Jl. Thamrin No. 2', 'Jakarta', 'Indonesia', '10230', '021-7654321', 'branch2@minibank.com', 'Manager Two', CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (branch_code) DO NOTHING;

-- Note: We'll use existing customers from migration data to avoid conflicts
-- The migration already creates customers C1000001-C1000005
-- C1000001: Ahmad Suharto (PERSONAL)
-- C1000002: Budi Santoso (PERSONAL) 
-- C1000003: PT Maju Mundur (CORPORATE)
-- C1000004: PT Teknologi Nusantara (CORPORATE)
-- C1000005: Siti Nurhaliza (PERSONAL)

-- Just ensure branches exist since tests might need them

-- Insert test products suitable for account opening (savings and checking accounts)
INSERT INTO products (id, product_code, product_name, product_type, product_category, description, is_active, is_default, currency, minimum_opening_balance, minimum_balance, allowed_customer_types, profit_sharing_type, profit_distribution_frequency, is_shariah_compliant, nisbah_customer, nisbah_bank, created_date, created_by)
VALUES 
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'SAV001', 'Tabungan Wadiah Personal', 'TABUNGAN_WADIAH', 'Personal Banking', 'Islamic savings account based on Wadiah (safekeeping) contract for personal customers', true, true, 'IDR', 50000.00, 10000.00, 'PERSONAL', 'WADIAH', 'MONTHLY', true, 1.0000, 0.0000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'SAV002', 'Tabungan Mudharabah Personal', 'TABUNGAN_MUDHARABAH', 'Personal Banking', 'Islamic savings account based on Mudharabah (profit sharing) contract for personal customers', true, false, 'IDR', 100000.00, 25000.00, 'PERSONAL', 'MUDHARABAH', 'MONTHLY', true, 0.7000, 0.3000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'CHK001', 'Checking Account Personal', 'CHECKING', 'Personal Banking', 'Personal checking account for daily transactions', true, false, 'IDR', 250000.00, 50000.00, 'PERSONAL', 'WADIAH', 'MONTHLY', true, 1.0000, 0.0000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('99999999-9999-9999-9999-999999999999', 'SAV003', 'Tabungan Corporate', 'TABUNGAN_WADIAH', 'Corporate Banking', 'Islamic savings account for corporate customers', true, false, 'IDR', 500000.00, 100000.00, 'CORPORATE', 'WADIAH', 'MONTHLY', true, 1.0000, 0.0000, CURRENT_TIMESTAMP, 'SYSTEM'),
    ('88888888-8888-8888-8888-888888888888', 'SAV004', 'Tabungan Universal', 'SAVINGS', 'Universal Banking', 'Universal savings account for all customer types', true, false, 'IDR', 75000.00, 15000.00, 'PERSONAL,CORPORATE', 'WADIAH', 'MONTHLY', true, 1.0000, 0.0000, CURRENT_TIMESTAMP, 'SYSTEM')
ON CONFLICT (product_code) DO NOTHING;

-- Ensure sequence numbers are set up for account number generation
INSERT INTO sequence_numbers (id, sequence_name, last_number, prefix, created_date, updated_date)
VALUES 
    ('77777777-7777-7777-7777-777777777777', 'ACCOUNT_NUMBER', 2000000, 'ACC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (sequence_name) DO UPDATE SET
    last_number = GREATEST(sequence_numbers.last_number, 2000000),
    updated_date = CURRENT_TIMESTAMP;