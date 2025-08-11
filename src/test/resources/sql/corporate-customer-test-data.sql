-- Clear existing data
DELETE FROM corporate_customers;
DELETE FROM personal_customers;
DELETE FROM customers;

-- Insert test corporate customers
INSERT INTO customers (id, customer_type, customer_number, email, phone_number, address, city, postal_code, created_by) VALUES
(gen_random_uuid(), 'CORPORATE', 'C2000001', 'test.corp1@email.com', '0211112222', 'Jl. Test No. 1', 'Test City', '12345', 'TEST'),
(gen_random_uuid(), 'CORPORATE', 'C2000002', 'test.corp2@email.com', '0213334444', 'Jl. Test No. 2', 'Test City', '12346', 'TEST');

INSERT INTO corporate_customers (id, company_name, company_registration_number, tax_identification_number) VALUES
((SELECT id FROM customers WHERE customer_number = 'C2000001'), 'Test Corp 1', '9876543210987654', '09.876.543.2-109.876'),
((SELECT id FROM customers WHERE customer_number = 'C2000002'), 'Test Corp 2', '1234567890123456', '01.234.567.8-901.000');