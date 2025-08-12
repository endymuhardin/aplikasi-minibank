-- Cleanup test data for permission management tests
DELETE FROM role_permission WHERE permission_id IN (
    SELECT id FROM permission WHERE permission_code IN (
        'EDIT_TEST_PERM', 'VIEW_TEST_PERM', 'DUPLICATE_PERM', 'USER_CREATE', 'USER_READ'
    )
);

DELETE FROM permission WHERE permission_code IN (
    'EDIT_TEST_PERM', 'VIEW_TEST_PERM', 'DUPLICATE_PERM', 'USER_CREATE', 'USER_READ'
);

-- Cleanup any test permissions created during tests (with timestamp suffix)
DELETE FROM permission WHERE permission_code LIKE 'TEST_PERM_%' OR permission_code LIKE 'PERM_%';