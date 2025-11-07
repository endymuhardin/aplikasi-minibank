-- Initialize branches data
INSERT INTO branches (
    id, branch_code, branch_name, address, city, postal_code, 
    phone_number, email, manager_name, is_main_branch, created_by
) VALUES 
('01234567-8901-2345-6789-012345678901', 'HO001', 'Kantor Pusat Jakarta', 'Jl. Sudirman Kav. 10-11', 'Jakarta Pusat', '10220', '021-29345678', 'kantor.pusat@bankbsi.co.id', 'H. Ahmad Surya', true, 'SYSTEM'),
('01234567-8901-2345-6789-012345678902', 'JKT01', 'Cabang Jakarta Timur', 'Jl. Ahmad Yani No. 45', 'Jakarta Timur', '13230', '021-85761234', 'jakarta.timur@bankbsi.co.id', 'Drs. Budi Pratama', false, 'SYSTEM'),
('01234567-8901-2345-6789-012345678903', 'BDG01', 'Cabang Bandung', 'Jl. Asia Afrika No. 88', 'Bandung', '40111', '022-42056789', 'bandung@bankbsi.co.id', 'H. Siti Nurhalimah', false, 'SYSTEM'),
('01234567-8901-2345-6789-012345678904', 'SBY01', 'Cabang Surabaya', 'Jl. Pemuda No. 123', 'Surabaya', '60271', '031-53419876', 'surabaya@bankbsi.co.id', 'Ir. Wahyu Setiawan', false, 'SYSTEM'),
('01234567-8901-2345-6789-012345678905', 'YGY01', 'Cabang Yogyakarta', 'Jl. Malioboro No. 56', 'Yogyakarta', '55213', '0274-562789', 'yogyakarta@bankbsi.co.id', 'Dr. Retno Wulandari', false, 'SYSTEM');

-- Initialize sequence numbers
-- Note: Prefix in seed data takes precedence over prefix passed in code
-- Ensure sequence names match exactly what's used in application code
INSERT INTO sequence_numbers (sequence_name, last_number, prefix) VALUES
    ('CUSTOMER', 1000010, 'C'),                      -- Used by CustomerController (next: C1000011)
    ('ACCOUNT_NUMBER', 2000006, 'A'),                 -- Used by AccountService for personal accounts (next: A2000007)
    ('CORPORATE_ACCOUNT_NUMBER', 0, 'CORP'),          -- Used by AccountService for corporate accounts (next: CORP0000001)
    ('TRANSACTION_NUMBER', 3000000, 'T');

-- Initialize Islamic banking products
INSERT INTO products (
    product_code, product_name, product_type, product_category, description,
    is_active, is_default, currency,
    minimum_opening_balance, minimum_balance, maximum_balance,
    daily_withdrawal_limit, monthly_transaction_limit,
    profit_sharing_ratio, profit_sharing_type, profit_distribution_frequency,
    nisbah_customer, nisbah_bank, is_shariah_compliant,
    monthly_maintenance_fee, atm_withdrawal_fee, inter_bank_transfer_fee,
    below_minimum_balance_fee, account_closure_fee,
    free_transactions_per_month, excess_transaction_fee,
    allow_overdraft, require_maintaining_balance,
    min_customer_age, allowed_customer_types,
    required_documents, launch_date, created_by
) VALUES 
-- Tabungan Wadiah Basic
('TAB001', 'Tabungan Wadiah Basic', 'TABUNGAN_WADIAH', 'Tabungan Syariah', 
 'Tabungan dengan akad wadiah untuk nasabah perorangan',
 true, true, 'IDR',
 50000, 10000, NULL,
 5000000, 50,
 0.0275, 'WADIAH', 'MONTHLY',
 NULL, NULL, true,
 2500, 5000, 7500,
 10000, 0,
 10, 2500,
 false, true,
 17, 'PERSONAL',
 'KTP, NPWP (optional)', CURRENT_DATE, 'SYSTEM'),

-- Tabungan Mudharabah Premium  
('TAB002', 'Tabungan Mudharabah Premium', 'TABUNGAN_MUDHARABAH', 'Tabungan Syariah',
 'Tabungan mudharabah dengan nisbah bagi hasil yang menarik',
 true, false, 'IDR',
 1000000, 500000, NULL,
 10000000, 100,
 0.0350, 'MUDHARABAH', 'MONTHLY',
 0.7000, 0.3000, true,
 0, 0, 5000,
 25000, 0,
 25, 2500,
 false, true,
 21, 'PERSONAL',
 'KTP, NPWP, Slip Gaji', CURRENT_DATE, 'SYSTEM'),

-- Deposito Mudharabah
('DEP001', 'Deposito Mudharabah', 'DEPOSITO_MUDHARABAH', 'Deposito Syariah',
 'Deposito berjangka dengan akad mudharabah',
 true, false, 'IDR',
 100000, 50000, NULL,
 20000000, 100,
 0.0100, 'MUDHARABAH', 'ON_MATURITY',
 0.7000, 0.3000, true,
 5000, 5000, 7500,
 15000, 10000,
 20, 3000,
 true, true,
 18, 'PERSONAL',
 'KTP, NPWP, Slip Gaji', CURRENT_DATE, 'SYSTEM'),

-- Pembiayaan Murabahah
('PEM001', 'Pembiayaan Murabahah', 'PEMBIAYAAN_MURABAHAH', 'Pembiayaan Syariah',
 'Pembiayaan dengan akad murabahah untuk kebutuhan konsumtif',
 true, false, 'IDR',
 5000000, 1000000, NULL,
 50000000, 200,
 0.0300, 'MURABAHAH', 'MONTHLY',
 NULL, NULL, true,
 15000, 5000, 5000,
 50000, 25000,
 50, 5000,
 false, true,
 NULL, 'CORPORATE',
 'Akta Pendirian, SIUP, TDP, NPWP', CURRENT_DATE, 'SYSTEM'),

-- Pembiayaan Musharakah
('PEM002', 'Pembiayaan Musharakah', 'PEMBIAYAAN_MUSHARAKAH', 'Pembiayaan Syariah',
 'Pembiayaan dengan akad musharakah untuk modal usaha',
 true, false, 'IDR',
 2000000, 1000000, NULL,
 50000000, 200,
 0.0150, 'MUSHARAKAH', 'QUARTERLY',
 0.6000, 0.4000, true,
 0, 0, 5000,
 25000, 15000,
 50, 3000,
 true, true,
 25, 'PERSONAL',
 'KTP, NPWP, Slip Gaji, Rekening Koran', CURRENT_DATE, 'SYSTEM'),

-- Giro Wadiah Corporate
('GIR001', 'Giro Wadiah Corporate', 'TABUNGAN_WADIAH', 'Giro Syariah',
 'Rekening giro dengan akad wadiah untuk kebutuhan operasional perusahaan',
 true, false, 'IDR',
 1000000, 500000, NULL,
 50000000, 500,
 0.0250, 'WADIAH', 'MONTHLY',
 NULL, NULL, true,
 15000, 2500, 5000,
 25000, 0,
 50, 5000,
 false, true,
 NULL, 'CORPORATE',
 'Akta Pendirian, SIUP, TDP, NPWP, Surat Kuasa', CURRENT_DATE, 'SYSTEM');

-- Sample personal customers data - first insert into base customers table
INSERT INTO customers (
    id, customer_type, customer_number, id_branches, email, phone_number, 
    address, city, postal_code, status, created_by
) VALUES 
(gen_random_uuid(), 'PERSONAL', 'C1000001', '01234567-8901-2345-6789-012345678901', 'ahmad.suharto@email.com', '081234567890', 
 'Jl. Sudirman No. 123', 'Jakarta', '10220', 'ACTIVE', 'SYSTEM'),

(gen_random_uuid(), 'PERSONAL', 'C1000002', '01234567-8901-2345-6789-012345678902', 'siti.nurhaliza@email.com', '081234567891', 
 'Jl. Thamrin No. 456', 'Jakarta', '10230', 'ACTIVE', 'SYSTEM'),

(gen_random_uuid(), 'PERSONAL', 'C1000004', '01234567-8901-2345-6789-012345678903', 'budi.santoso@email.com', '081234567892', 
 'Jl. Gatot Subroto No. 321', 'Jakarta', '12930', 'ACTIVE', 'SYSTEM'),

(gen_random_uuid(), 'PERSONAL', 'C1000006', '01234567-8901-2345-6789-012345678904', 'dewi.lestari@email.com', '081234567893', 
 'Jl. MH Thamrin No. 654', 'Jakarta', '10350', 'ACTIVE', 'SYSTEM');

-- Insert personal customer specific data
INSERT INTO personal_customers (
    id, first_name, last_name, date_of_birth, identity_number, identity_type
) VALUES 
((SELECT id FROM customers WHERE customer_number = 'C1000001'), 'Ahmad', 'Suharto', '1985-03-15', '3271081503850001', 'KTP'),
((SELECT id FROM customers WHERE customer_number = 'C1000002'), 'Siti', 'Nurhaliza', '1990-07-22', '3271082207900002', 'KTP'),
((SELECT id FROM customers WHERE customer_number = 'C1000004'), 'Budi', 'Santoso', '1988-11-10', '3271081011880003', 'KTP'),
((SELECT id FROM customers WHERE customer_number = 'C1000006'), 'Dewi', 'Lestari', '1992-05-18', '3271081805920004', 'KTP');

-- Sample corporate customers data - first insert into base customers table  
INSERT INTO customers (
    id, customer_type, customer_number, id_branches, email, phone_number, 
    address, city, postal_code, status, created_by
) VALUES 
(gen_random_uuid(), 'CORPORATE', 'C1000003', '01234567-8901-2345-6789-012345678901', 'info@teknologimaju.com', '02123456789', 
 'Jl. HR Rasuna Said No. 789', 'Jakarta', '12950', 'ACTIVE', 'SYSTEM');

-- Insert corporate customer specific data
INSERT INTO corporate_customers (
    id, company_name, company_registration_number, tax_identification_number
) VALUES 
((SELECT id FROM customers WHERE customer_number = 'C1000003'), 'PT. Teknologi Maju', '1234567890123456', '01.234.567.8-901.000');

-- Sample accounts for testing
INSERT INTO accounts (
    id, account_number, account_name, id_customers, id_products, id_branches, 
    balance, status, opened_date, created_by
) VALUES 
-- Personal customers accounts
(gen_random_uuid(), 'A2000001', 'Ahmad Suharto - Tabungan Wadiah', 
 (SELECT id FROM customers WHERE customer_number = 'C1000001'), 
 (SELECT id FROM products WHERE product_code = 'TAB001'),
 '01234567-8901-2345-6789-012345678901',
 1000000.00, 'ACTIVE', CURRENT_DATE, 'SYSTEM'),

(gen_random_uuid(), 'A2000002', 'Siti Nurhaliza - Tabungan Mudharabah', 
 (SELECT id FROM customers WHERE customer_number = 'C1000002'), 
 (SELECT id FROM products WHERE product_code = 'TAB002'),
 '01234567-8901-2345-6789-012345678902',
 2500000.00, 'ACTIVE', CURRENT_DATE, 'SYSTEM'),

(gen_random_uuid(), 'A2000003', 'Budi Santoso - Tabungan Wadiah', 
 (SELECT id FROM customers WHERE customer_number = 'C1000004'), 
 (SELECT id FROM products WHERE product_code = 'TAB001'),
 '01234567-8901-2345-6789-012345678903',
 750000.00, 'ACTIVE', CURRENT_DATE, 'SYSTEM'),

(gen_random_uuid(), 'A2000004', 'Dewi Lestari - Tabungan Mudharabah', 
 (SELECT id FROM customers WHERE customer_number = 'C1000006'), 
 (SELECT id FROM products WHERE product_code = 'TAB002'),
 '01234567-8901-2345-6789-012345678904',
 3200000.00, 'ACTIVE', CURRENT_DATE, 'SYSTEM'),

-- Corporate customer accounts
(gen_random_uuid(), 'A2000005', 'PT. Teknologi Maju - Giro Wadiah Corporate', 
 (SELECT id FROM customers WHERE customer_number = 'C1000003'), 
 (SELECT id FROM products WHERE product_code = 'GIR001'),
 '01234567-8901-2345-6789-012345678901',
 5000000.00, 'ACTIVE', CURRENT_DATE, 'SYSTEM'),
 
(gen_random_uuid(), 'A2000006', 'PT. Teknologi Maju - Pembiayaan Murabahah', 
 (SELECT id FROM customers WHERE customer_number = 'C1000003'), 
 (SELECT id FROM products WHERE product_code = 'PEM001'),
 '01234567-8901-2345-6789-012345678901',
 10000000.00, 'ACTIVE', CURRENT_DATE, 'SYSTEM');