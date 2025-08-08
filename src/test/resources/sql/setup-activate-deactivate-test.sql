-- Clean up existing test data
DELETE FROM products WHERE product_code LIKE 'TEST%' OR product_code LIKE 'STATUS%';

-- Insert small sample dataset (5 products total - less than default page size of 10)
-- This ensures all products fit on page 1, eliminating pagination issues

INSERT INTO products (id, product_code, product_name, product_type, product_category, description, is_active, is_default, created_date, updated_date) VALUES
(gen_random_uuid(), 'TEST001', 'Test Savings Account', 'SAVINGS', 'Personal Banking', 'Basic savings account for testing', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'TEST002', 'Test Checking Account', 'CHECKING', 'Personal Banking', 'Basic checking account for testing', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'TEST003', 'Test Loan Product', 'LOAN', 'Lending', 'Basic loan product for testing', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'TEST004', 'Test CD Product', 'SAVINGS', 'Investment', 'CD product for testing', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'TEST005', 'Test Credit Card', 'SAVINGS', 'Credit Products', 'Credit card product for testing', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);