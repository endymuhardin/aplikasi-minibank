-- Initialize sequence numbers
INSERT INTO sequence_numbers (sequence_name, last_number, prefix) VALUES 
    ('CUSTOMER_NUMBER', 1000000, 'C'),
    ('ACCOUNT_NUMBER', 2000000, 'A'),
    ('TRANSACTION_NUMBER', 3000000, 'T');

-- Initialize banking products
INSERT INTO products (
    product_code, product_name, product_type, product_category, description,
    is_active, is_default, currency,
    minimum_opening_balance, minimum_balance, maximum_balance,
    daily_withdrawal_limit, monthly_transaction_limit,
    interest_rate, interest_calculation_type, interest_payment_frequency,
    monthly_maintenance_fee, atm_withdrawal_fee, inter_bank_transfer_fee,
    below_minimum_balance_fee, account_closure_fee,
    free_transactions_per_month, excess_transaction_fee,
    allow_overdraft, require_maintaining_balance,
    min_customer_age, allowed_customer_types,
    required_documents, launch_date, created_by
) VALUES 
-- Basic Savings Account
('SAV001', 'Basic Savings Account', 'SAVINGS', 'Regular Savings', 
 'Basic savings account for individual customers with competitive interest rates',
 true, true, 'IDR',
 50000, 10000, NULL,
 5000000, 50,
 0.0275, 'DAILY', 'MONTHLY',
 2500, 5000, 7500,
 10000, 0,
 10, 2500,
 false, true,
 17, 'PERSONAL',
 'KTP, NPWP (optional)', CURRENT_DATE, 'SYSTEM'),

-- Premium Savings Account  
('SAV002', 'Premium Savings Account', 'SAVINGS', 'Premium Savings',
 'Premium savings account with higher interest and more free transactions',
 true, false, 'IDR',
 1000000, 500000, NULL,
 10000000, 100,
 0.0350, 'DAILY', 'MONTHLY',
 0, 0, 5000,
 25000, 0,
 25, 2500,
 false, true,
 21, 'PERSONAL',
 'KTP, NPWP, Slip Gaji', CURRENT_DATE, 'SYSTEM'),

-- Corporate Savings Account
('SAV003', 'Corporate Savings Account', 'SAVINGS', 'Corporate',
 'Savings account designed for corporate customers with business features',
 true, false, 'IDR',
 5000000, 1000000, NULL,
 50000000, 200,
 0.0300, 'DAILY', 'MONTHLY',
 15000, 5000, 5000,
 50000, 25000,
 50, 5000,
 false, true,
 NULL, 'CORPORATE',
 'Akta Pendirian, SIUP, TDP, NPWP', CURRENT_DATE, 'SYSTEM'),

-- Basic Checking Account
('CHK001', 'Basic Checking Account', 'CHECKING', 'Regular Checking',
 'Basic checking account with overdraft facility for daily transactions',
 true, false, 'IDR',
 100000, 50000, NULL,
 20000000, 100,
 0.0100, 'DAILY', 'MONTHLY',
 5000, 5000, 7500,
 15000, 10000,
 20, 3000,
 true, true,
 18, 'PERSONAL',
 'KTP, NPWP, Slip Gaji', CURRENT_DATE, 'SYSTEM'),

-- Premium Checking Account
('CHK002', 'Premium Checking Account', 'CHECKING', 'Premium Checking',
 'Premium checking account with higher overdraft limit and more benefits',
 true, false, 'IDR',
 2000000, 1000000, NULL,
 50000000, 200,
 0.0150, 'DAILY', 'MONTHLY',
 0, 0, 5000,
 25000, 15000,
 50, 3000,
 true, true,
 25, 'PERSONAL',
 'KTP, NPWP, Slip Gaji, Rekening Koran', CURRENT_DATE, 'SYSTEM'),

-- Corporate Checking Account
('CHK003', 'Corporate Checking Account', 'CHECKING', 'Corporate',
 'Checking account for corporate customers with extensive transaction capabilities',
 true, false, 'IDR',
 10000000, 2000000, NULL,
 100000000, 500,
 0.0125, 'DAILY', 'MONTHLY',
 25000, 5000, 5000,
 75000, 50000,
 100, 5000,
 true, true,
 NULL, 'CORPORATE',
 'Akta Pendirian, SIUP, TDP, NPWP, SK Kemenkumham', CURRENT_DATE, 'SYSTEM');

-- Sample customers data
INSERT INTO customers (
    customer_type, customer_number, first_name, last_name, 
    date_of_birth, identity_number, identity_type,
    email, phone_number, address, city, postal_code,
    created_by
) VALUES 
('PERSONAL', 'C1000001', 'Ahmad', 'Suharto', 
 '1985-03-15', '3271081503850001', 'KTP',
 'ahmad.suharto@email.com', '081234567890', 
 'Jl. Sudirman No. 123', 'Jakarta', '10220', 'SYSTEM'),

('PERSONAL', 'C1000002', 'Siti', 'Nurhaliza', 
 '1990-07-22', '3271082207900002', 'KTP',
 'siti.nurhaliza@email.com', '081234567891', 
 'Jl. Thamrin No. 456', 'Jakarta', '10230', 'SYSTEM');

INSERT INTO customers (
    customer_type, customer_number, company_name, 
    company_registration_number, tax_identification_number,
    email, phone_number, address, city, postal_code,
    created_by
) VALUES 
('CORPORATE', 'C1000003', 'PT. Teknologi Maju', 
 '1234567890123456', '01.234.567.8-901.000',
 'info@teknologimaju.com', '02123456789', 
 'Jl. HR Rasuna Said No. 789', 'Jakarta', '12950', 'SYSTEM');