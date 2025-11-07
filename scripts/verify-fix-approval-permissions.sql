-- Script to verify and fix approval permissions
-- This ensures ONLY Branch Manager has approval permissions

-- 1. Check current approval permission assignments
SELECT
    r.role_code,
    r.role_name,
    p.permission_code,
    p.permission_name
FROM roles r
JOIN role_permissions rp ON r.id = rp.id_roles
JOIN permissions p ON rp.id_permissions = p.id
WHERE p.permission_code IN ('APPROVAL_VIEW', 'CUSTOMER_APPROVE', 'ACCOUNT_APPROVE')
ORDER BY r.role_code, p.permission_code;

-- 2. Remove approval permissions from non-BRANCH_MANAGER roles
DELETE FROM role_permissions
WHERE id_permissions IN (
    SELECT id FROM permissions
    WHERE permission_code IN ('APPROVAL_VIEW', 'CUSTOMER_APPROVE', 'ACCOUNT_APPROVE')
)
AND id_roles IN (
    SELECT id FROM roles
    WHERE role_code != 'BRANCH_MANAGER'
);

-- 3. Ensure BRANCH_MANAGER has all approval permissions
INSERT INTO role_permissions (id_roles, id_permissions, granted_by)
SELECT r.id, p.id, 'SYSTEM'
FROM roles r, permissions p
WHERE r.role_code = 'BRANCH_MANAGER'
AND p.permission_code IN ('APPROVAL_VIEW', 'CUSTOMER_APPROVE', 'ACCOUNT_APPROVE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.id_roles = r.id AND rp.id_permissions = p.id
);

-- 4. Verify final state
SELECT
    r.role_code,
    r.role_name,
    p.permission_code,
    p.permission_name
FROM roles r
JOIN role_permissions rp ON r.id = rp.id_roles
JOIN permissions p ON rp.id_permissions = p.id
WHERE p.permission_code IN ('APPROVAL_VIEW', 'CUSTOMER_APPROVE', 'ACCOUNT_APPROVE')
ORDER BY r.role_code, p.permission_code;
