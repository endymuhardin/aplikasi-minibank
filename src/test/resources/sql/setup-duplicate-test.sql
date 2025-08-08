-- Clean up any existing SAV001 product
DELETE FROM products WHERE product_code = 'SAV001';

-- Insert a test product with SAV001 for duplicate validation testing
INSERT INTO products (id, product_code, product_name, product_type, product_category, is_active, created_date, updated_date) 
VALUES (gen_random_uuid(), 'SAV001', 'Existing Savings Product', 'SAVINGS', 'Test Category', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);