-- Clean up test products created for search functionality
DELETE FROM products WHERE product_code LIKE 'SEARCH_%';