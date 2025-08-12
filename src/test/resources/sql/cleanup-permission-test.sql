-- Cleanup test data for permission management tests
DELETE FROM role_permissions WHERE id_permissions IN (
    SELECT id FROM permissions WHERE permission_code IN (
        'EDIT_TEST_PERM', 'VIEW_TEST_PERM', 'DUPLICATE_PERM', 'USER_CREATE', 'USER_READ'
    )
);

DELETE FROM permissions WHERE permission_code IN (
    'EDIT_TEST_PERM', 'VIEW_TEST_PERM', 'DUPLICATE_PERM', 'USER_CREATE', 'USER_READ'
);

-- Cleanup any test permissions created during tests (with timestamp suffix)
DELETE FROM permissions WHERE permission_code LIKE 'TEST_PERM_%' 
   OR permission_code LIKE 'PERM_USER_CREATE%'
   OR permission_code LIKE 'PERM_USER_READ%'
   OR permission_code LIKE 'PERM_USER_UPDATE%'
   OR permission_code LIKE 'PERM_USER_DELETE%'
   OR permission_code LIKE 'PERM_PROD_CREATE%'
   OR permission_code LIKE 'PERM_PROD_READ%'
   OR permission_code LIKE 'PERM_CUST_CREATE%'
   OR permission_code LIKE 'PERM_CUST_READ%'
   OR permission_code LIKE 'PERM_TXN_CREATE%'
   OR permission_code LIKE 'PERM_TXN_READ%';