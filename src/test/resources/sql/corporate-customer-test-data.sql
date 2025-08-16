-- Clear existing data
DELETE FROM corporate_customers;
DELETE FROM personal_customers;
DELETE FROM customers;

-- Ensure the main branch exists (from migration data) for test data
-- Insert if not exists to handle test isolation
INSERT INTO branches (id, branch_code, branch_name, address, city, postal_code, country, status, is_main_branch, created_by, created_date)
SELECT '01234567-8901-2345-6789-012345678901', 'HO001', 'Kantor Pusat Jakarta', 'Jl. Sudirman Kav. 10-11', 'Jakarta Pusat', '10220', 'Indonesia', 'ACTIVE', true, 'SYSTEM', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM branches WHERE branch_code = 'HO001');

-- Insert test corporate customers with fixed UUIDs
INSERT INTO customers (id, customer_type, customer_number, id_branches, email, phone_number, address, city, postal_code, created_by) VALUES
('33333333-3333-3333-3333-333333333001', 'CORPORATE', 'C2000001', '01234567-8901-2345-6789-012345678901', 'test.corp1@email.com', '0211112222', 'Jl. Test No. 1', 'Test City', '12345', 'TEST'),
('33333333-3333-3333-3333-333333333002', 'CORPORATE', 'C2000002', '01234567-8901-2345-6789-012345678901', 'test.corp2@email.com', '0213334444', 'Jl. Test No. 2', 'Test City', '12346', 'TEST');

INSERT INTO corporate_customers (id, company_name, company_registration_number, tax_identification_number) VALUES
('33333333-3333-3333-3333-333333333001', 'Test Corp 1', '9876543210987654', '09.876.543.2-109.876'),
('33333333-3333-3333-3333-333333333002', 'Test Corp 2', '1234567890123456', '01.234.567.8-901.000');