-- Cleanup test data for withdrawal functionality tests
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM personal_customers;
DELETE FROM corporate_customers;
DELETE FROM customers;
DELETE FROM products;
DELETE FROM sequence_numbers;