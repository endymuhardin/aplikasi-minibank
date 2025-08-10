-- Clean up any existing test products for search functionality
DELETE FROM products WHERE product_code LIKE 'SEARCH_%';

-- Insert test products for search functionality
INSERT INTO products (id, product_code, product_name, product_type, product_category, description, is_active, is_default, created_date, updated_date) VALUES
(gen_random_uuid(), 'SEARCH_SAV001', 'Search Test Savings', 'SAVINGS', 'Savings', 'Savings product for search testing', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'SEARCH_CHK001', 'Search Test Checking', 'CHECKING', 'Checking', 'Checking product for search testing', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);