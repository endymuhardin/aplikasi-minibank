-- Clean up test products created for search functionality
-- PRESERVE migration products (TAB001, TAB002, DEP001, PEM001, PEM002)
DELETE FROM products WHERE product_code LIKE 'SEARCH_%' OR product_code LIKE 'TEST_%';