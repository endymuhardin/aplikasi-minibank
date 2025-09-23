# Test Scenarios: RBAC Data Management

## Overview
Dokumen ini berisi skenario test untuk Role-Based Access Control (RBAC) data management dalam aplikasi minibank Islam. RBAC management mencakup user management, role assignment, permission configuration, dan security validation.

## Preconditions
- Database PostgreSQL berjalan
- Aplikasi Spring Boot aktif
- User sudah login sebagai Branch Manager atau System Administrator
- RBAC seed data tersedia dari migration files
- Functional test infrastructure configured

## Existing Seed Data Reference

### Users dari Migration V004
```sql
-- Sample users dari V004__insert_roles_permissions_data.sql:
admin (BRANCH_MANAGER) - System Administrator
manager1, manager2 (BRANCH_MANAGER) - Branch Managers
teller1, teller2, teller3 (TELLER) - Teller staff
cs1, cs2, cs3 (CUSTOMER_SERVICE) - Customer Service staff
-- Password untuk semua users: minibank123 (BCrypt hash)
```

### Roles dari Seed Data
```sql
BRANCH_MANAGER - Full system access with all permissions
TELLER - Transaction processing and customer account operations
CUSTOMER_SERVICE - Customer registration and account opening
ADMIN - Full system administration (from test fixtures)
AUDITOR - Read-only access for compliance (from test fixtures)
```

### Permissions dari CSV Fixtures
```csv
# src/test/resources/fixtures/user/permissions.csv
CUSTOMER_VIEW,CUSTOMER_CREATE,CUSTOMER_UPDATE,CUSTOMER_DELETE
ACCOUNT_VIEW,ACCOUNT_CREATE,ACCOUNT_UPDATE,ACCOUNT_CLOSE
TRANSACTION_VIEW,TRANSACTION_DEPOSIT,TRANSACTION_WITHDRAWAL,TRANSACTION_TRANSFER
PRODUCT_VIEW,PRODUCT_CREATE,PRODUCT_UPDATE
USER_VIEW,USER_CREATE,USER_UPDATE,USER_DEACTIVATE
ROLE_VIEW,ROLE_ASSIGN
REPORT_VIEW,REPORT_EXPORT,AUDIT_VIEW
CONFIG_VIEW,CONFIG_UPDATE
```

## Test Cases

### TC-RBAC-001: View User List - Happy Path
**Deskripsi**: Menampilkan daftar semua system users dengan role information

**Test Data** (sesuai RbacManagementFunctionalTest):
- User: Branch Manager (has USER_VIEW permission)
- Expected Users: 9 users dari seed data (admin, manager1-2, teller1-3, cs1-3)

**Steps**:
1. Login sebagai Branch Manager (loginHelper.loginAsManager())
2. Navigate to User List page (/users/list)
3. Verify user list loaded dengan proper formatting
4. Check table headers: Username, Full Name, Email, Roles, Status, Actions
5. Verify user data displayed correctly

**Expected Result**:
- Page title: "User Management"
- User table contains 9+ rows dari seed data
- Users sorted by username
- Role badges displayed (BRANCH_MANAGER, TELLER, CUSTOMER_SERVICE)
- Status indicators: Active/Inactive/Locked
- Last login timestamps
- Action buttons: View, Edit, Reset Password, Lock/Unlock

### TC-RBAC-002: Create New User - Happy Path
**Deskripsi**: Membuat user baru dengan role assignment

**Test Data**:
- Username: testuser001 (unique, max 50 chars)
- Email: testuser001@yopmail.com (unique, max 100 chars)
- Full Name: Test User One (max 100 chars)
- Initial Password: Temp123!@# (will require change on first login)
- Role: TELLER
- Is Active: true

**Steps** (sesuai UserFormPage pattern):
1. Login sebagai Branch Manager
2. Navigate to User List page
3. Click "Add New User" button
4. Fill user creation form:
   - Username: testuser001
   - Email: testuser001@yopmail.com
   - Full Name: Test User One
   - Assign Role: TELLER
   - Set Initial Password: Temp123!@#
5. Click "Create User" button

**Expected Result**:
- User created successfully in users table
- Password stored as BCrypt hash in user_passwords table
- User-role relationship created in user_roles table
- Default values: is_active=true, is_locked=false, failed_login_attempts=0
- Success notification displayed
- Redirect to user list with new user visible
- Created_by field = current user
- User can login dengan temporary password

### TC-RBAC-003: Create User with Multiple Roles
**Deskripsi**: Assign multiple roles ke user baru

**Test Data**:
- Username: supervisor001
- Full Name: Branch Supervisor
- Email: supervisor001@yopmail.com
- Roles: TELLER + CUSTOMER_SERVICE (multiple roles)

**Steps**:
1. Login sebagai Branch Manager
2. Create new user: supervisor001
3. In role assignment section:
   - Check TELLER role
   - Check CUSTOMER_SERVICE role
   - Verify combined permissions preview
4. Save user

**Expected Result**:
- User created dengan multiple role assignments
- Two records in user_roles table untuk same user
- User inherits ALL permissions dari both roles
- Permission union logic working correctly
- Role combination validated for conflicts
- User can access features dari both roles

### TC-RBAC-004: Validation - Duplicate Username
**Deskripsi**: Validasi username yang sudah ada

**Test Data**:
- Username: admin (already exists in seed data)

**Steps**:
1. Login sebagai Branch Manager
2. Try to create user dengan username: admin
3. Fill other required fields
4. Submit form

**Expected Result**:
- Form validation error
- Error message: "Username already exists"
- Database unique constraint prevents duplicate
- Field highlighted dengan error styling
- Form tidak ter-submit
- User remains on form with error message

### TC-RBAC-005: Validation - Invalid Email Format
**Deskripsi**: Validasi format email address

**Test Data**:
- Email: invalid-email-format (no @ symbol)
- Email: user@domain (no TLD)
- Email: @domain.com (no local part)

**Steps**:
1. Login sebagai Branch Manager
2. Create new user dengan invalid email
3. Submit form

**Expected Result**:
- Client-side validation error
- Error message: "Please enter a valid email address"
- Bean validation @Email annotation enforced
- Server-side validation backup
- Form highlights email field
- User can correct email format

### TC-RBAC-006: Update User Information
**Deskripsi**: Update existing user details dan role assignments

**Test Data**:
- User: teller1 (existing from seed data)
- New Full Name: Senior Teller One
- New Email: senior.teller1@yopmail.com
- Additional Role: CUSTOMER_SERVICE (promotion)

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to User List page
3. Click "Edit" button untuk teller1
4. Update full name: Senior Teller One
5. Update email: senior.teller1@yopmail.com
6. Add CUSTOMER_SERVICE role (keep TELLER)
7. Click "Update User"

**Expected Result**:
- User information updated successfully
- Full name and email changed in users table
- Additional role assignment created
- User now has permissions dari both roles
- Updated_by field = current user
- Updated_date = current timestamp
- User session remains active (if logged in)
- Audit trail records all changes

### TC-RBAC-007: Deactivate User Account
**Deskripsi**: Deactivate user tanpa menghapus dari database

**Test Data**:
- User: cs3 (existing active user)
- New Status: is_active = false

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to User List page
3. Find cs3 user row
4. Click "Deactivate" button
5. Confirm deactivation in modal dialog

**Expected Result**:
- User status changed to inactive (is_active = false)
- User cannot login anymore
- Existing sessions terminated
- User still visible in list dengan "Inactive" status
- User dapat di-reactivate later
- Deactivation logged in audit trail
- User's role assignments preserved

### TC-RBAC-008: Lock User Account After Failed Logins
**Deskripsi**: Test automatic account locking mechanism

**Test Data**:
- User: teller2 (existing active user)
- Password attempts: wrong password 5 times
- Lock threshold: 5 failed attempts (configured)

**Steps**:
1. Attempt login as teller2 dengan wrong password
2. Repeat 5 times consecutively
3. Verify account locked automatically
4. Attempt login dengan correct password
5. Login sebagai Branch Manager
6. Check user status dan unlock account

**Expected Result**:
- Failed_login_attempts incremented with each failure
- Account locked after 5 failures (is_locked = true)
- Locked_until timestamp set (e.g., 30 minutes)
- Login prevented even dengan correct password
- Branch Manager can unlock account manually
- Lock counter reset after successful unlock
- Security audit log records lock/unlock events

### TC-RBAC-009: Password Reset for User
**Deskripsi**: Reset password untuk existing user

**Test Data**:
- User: cs1 (existing user)
- New Temporary Password: TempPass2024!
- Require Password Change: true

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to User List page
3. Click "Reset Password" untuk cs1
4. Enter new temporary password: TempPass2024!
5. Check "Require password change on next login"
6. Confirm password reset

**Expected Result**:
- New password hash stored in user_passwords table
- Old password record marked inactive
- User forced to change password on next login
- Password reset logged dengan timestamp
- Email notification sent to user (if configured)
- Temporary password expires after first use
- Password complexity rules enforced

### TC-RBAC-010: Role Management - View All Roles
**Deskripsi**: Display comprehensive role list dengan permissions

**Test Data**:
- Expected Roles: BRANCH_MANAGER, TELLER, CUSTOMER_SERVICE, ADMIN, AUDITOR

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Role Management page (/roles/list)
3. View role list dengan permission details
4. Expand role details untuk each role

**Expected Result**:
- All roles displayed dalam table format
- Role information: role_code, role_name, description, status
- Permission count per role
- Role hierarchy visualization (if applicable)
- User count per role
- Action buttons: View Permissions, Edit, Assign Users
- Sort/filter functionality untuk roles

### TC-RBAC-011: Create New Role dengan Custom Permissions
**Deskripsi**: Create custom role dengan specific permission set

**Test Data**:
- Role Code: READONLY_ANALYST (unique, max 50 chars)
- Role Name: Read Only Analyst (max 100 chars)
- Description: Limited access for data analysis and reporting
- Permissions: REPORT_VIEW, AUDIT_VIEW, CUSTOMER_VIEW, ACCOUNT_VIEW
- Is Active: true

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Role Management page
3. Click "Create New Role" button
4. Fill role details:
   - Role Code: READONLY_ANALYST
   - Role Name: Read Only Analyst
   - Description: Limited access for data analysis
5. Select permissions:
   - Check REPORT_VIEW
   - Check AUDIT_VIEW
   - Check CUSTOMER_VIEW
   - Check ACCOUNT_VIEW
6. Save role

**Expected Result**:
- Role created in roles table
- Permission assignments created in role_permissions table
- Role available untuk user assignment
- Permission validation ensures valid combinations
- Role appears in role list
- Created_by audit field populated

### TC-RBAC-012: Permission Management - View Permission Matrix
**Deskripsi**: Display permission matrix showing role-permission mappings

**Test Data**:
- All existing roles and permissions
- Matrix format untuk easy visualization

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to Permission Management page
3. View permission matrix grid
4. Filter by permission category (CUSTOMER, ACCOUNT, TRANSACTION, etc.)

**Expected Result**:
- Matrix shows roles vs permissions
- Checkmarks indicate granted permissions
- Color coding untuk permission categories
- Interactive matrix untuk role updates
- Permission categories grouped logically
- Search/filter functionality
- Export matrix untuk documentation

### TC-RBAC-013: Bulk Role Assignment
**Deskripsi**: Assign roles ke multiple users simultaneously

**Test Data**:
- Users: teller1, teller2, teller3
- Additional Role: CUSTOMER_SERVICE (bulk assignment)

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to User Management page
3. Select multiple users (teller1, teller2, teller3)
4. Click "Bulk Actions" → "Assign Role"
5. Select role: CUSTOMER_SERVICE
6. Confirm bulk assignment

**Expected Result**:
- Role assigned to all selected users
- Multiple records created in user_roles table
- Success notification dengan count
- Users immediately gain new permissions
- Bulk operation logged in audit trail
- Failed assignments reported separately
- Rollback available if partial failure

### TC-RBAC-014: Security Test - Unauthorized Access Prevention
**Deskripsi**: Validate access control untuk different user roles

**Test Data**:
- User 1: Teller (should NOT have USER_CREATE permission)
- User 2: Customer Service (should NOT have USER_UPDATE permission)
- User 3: Regular Customer (should have NO admin permissions)

**Steps**:
1. Login sebagai Teller
2. Try to access User Management page (should fail)
3. Try direct URL access to user creation (should fail)
4. Logout and login sebagai Customer Service
5. Try to edit existing user (should fail)
6. Logout and login sebagai Customer
7. Try any admin functionality

**Expected Result**:
- Teller: Cannot access user management features
- Customer Service: Cannot modify users or roles
- Customer: Cannot access any admin features
- Proper 403 Forbidden responses
- Navigation menus hidden based on permissions
- @PreAuthorize annotations enforced
- Security audit logs unauthorized attempts

### TC-RBAC-015: Session Management dan Concurrent Access
**Deskripsi**: Test user session handling dan concurrent login

**Test Data**:
- User: admin
- Multiple browser sessions
- Role changes during active session

**Steps**:
1. Login sebagai admin di Browser 1
2. Login sebagai same admin di Browser 2
3. Modify admin's roles di Browser 1
4. Verify session impact di Browser 2
5. Test concurrent role modifications

**Expected Result**:
- Multiple sessions allowed atau restricted (per configuration)
- Role changes reflected in active sessions
- Session invalidation when appropriate
- Concurrent modification detection
- User informed of session changes
- No data corruption dari concurrent access

### TC-RBAC-016: User Search and Filtering
**Deskripsi**: Comprehensive search functionality untuk user management

**Test Data**:
- Search by username: "teller"
- Filter by role: TELLER
- Filter by status: Active
- Sort by: Last Login Date

**Steps**:
1. Login sebagai Branch Manager
2. Navigate to User List page
3. Test search functionality:
   - Search username containing "teller"
   - Filter by TELLER role
   - Filter active users only
   - Sort by last login descending
4. Combine multiple filters

**Expected Result**:
- Search returns matching usernames
- Role filter shows only users dengan selected role
- Status filter works correctly
- Sorting functions on all columns
- Multiple filters dapat dikombinasikan
- Result count displayed
- Clear filters option available
- Pagination works dengan filters

### TC-RBAC-017: Audit Trail untuk RBAC Changes
**Deskripsi**: Comprehensive audit logging untuk all RBAC modifications

**Test Data**:
- Various RBAC operations: user creation, role assignment, permission changes

**Steps**:
1. Login sebagai Branch Manager
2. Perform various RBAC operations:
   - Create new user
   - Assign role to user
   - Modify user information
   - Reset user password
   - Create new role
   - Grant permissions to role
3. Check audit logs untuk each operation

**Expected Result**:
- All RBAC changes logged dengan details:
  - Timestamp of change
  - User who made change
  - Type of operation
  - Before/after values
  - Target user/role affected
- Audit log searchable by date/user/operation
- Immutable audit records
- Audit log export capability
- Compliance reporting features

### TC-RBAC-018: Role Hierarchy and Inheritance
**Deskripsi**: Test role hierarchy jika implemented

**Test Data**:
- Parent Role: BRANCH_MANAGER
- Child Role: TELLER
- Permission inheritance rules

**Steps**:
1. Login sebagai Branch Manager
2. View role hierarchy
3. Modify parent role permissions
4. Verify impact on child roles
5. Test user dengan hierarchical roles

**Expected Result**:
- Role hierarchy clearly displayed
- Child roles inherit parent permissions
- Permission additions propagate down
- Permission removals handled correctly
- Hierarchy conflicts resolved
- User effective permissions calculated correctly

## Performance Test Cases

### TC-RBAC-P001: User List Performance dengan Large Dataset
**Deskripsi**: Test performance dengan many users dan roles

**Test Scenario**:
- 10,000+ users in database
- 50+ roles dengan complex permissions
- Various filtering and sorting operations

**Expected Result**:
- Page load time < 3 seconds
- Search results < 1 second
- Pagination responsive
- Memory usage reasonable
- Database query optimization effective

### TC-RBAC-P002: Permission Check Performance
**Deskripsi**: Test performance untuk real-time permission checking

**Test Scenario**:
- 1000 concurrent users
- Complex role hierarchies
- Frequent permission checks

**Expected Result**:
- Permission check < 100ms
- Cache effectiveness high
- No performance degradation
- Session management efficient

## Database Validation

### RBAC Data Integrity Checks
```sql
-- Check user-role consistency
SELECT u.username, COUNT(ur.id_roles) as role_count
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.id_users
GROUP BY u.id, u.username
HAVING COUNT(ur.id_roles) = 0;

-- Verify role-permission mappings
SELECT r.role_code, COUNT(rp.id_permissions) as permission_count
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.id_roles
GROUP BY r.id, r.role_code
ORDER BY permission_count DESC;

-- Check password data integrity
SELECT u.username, COUNT(up.id) as password_count,
       MAX(up.created_date) as latest_password
FROM users u
LEFT JOIN user_passwords up ON u.id = up.id_users AND up.is_active = true
GROUP BY u.id, u.username
HAVING COUNT(up.id) != 1;

-- Validate failed login tracking
SELECT username, failed_login_attempts, is_locked, locked_until
FROM users 
WHERE failed_login_attempts > 0 OR is_locked = true;

-- Check orphaned role assignments
SELECT ur.id, ur.id_users, ur.id_roles
FROM user_roles ur
LEFT JOIN users u ON ur.id_users = u.id
LEFT JOIN roles r ON ur.id_roles = r.id
WHERE u.id IS NULL OR r.id IS NULL;
```

## API Test Examples

### REST API Calls
```bash
# Get all users dengan pagination
curl -X GET "http://localhost:8080/api/users?page=0&size=10&sort=username" \
  -H "Authorization: Bearer <token>"

# Get user by ID dengan roles
curl -X GET "http://localhost:8080/api/users/{userId}?include=roles" \
  -H "Authorization: Bearer <token>"

# Create new user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "username": "testuser001",
    "email": "testuser001@yopmail.com",
    "fullName": "Test User One",
    "isActive": true,
    "roles": ["TELLER"]
  }'

# Update user roles
curl -X PUT http://localhost:8080/api/users/{userId}/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "roles": ["TELLER", "CUSTOMER_SERVICE"]
  }'

# Reset user password
curl -X POST http://localhost:8080/api/users/{userId}/reset-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "temporaryPassword": "TempPass2024!",
    "requirePasswordChange": true
  }'

# Lock/unlock user account
curl -X PATCH http://localhost:8080/api/users/{userId}/lock \
  -H "Authorization: Bearer <token>"

curl -X PATCH http://localhost:8080/api/users/{userId}/unlock \
  -H "Authorization: Bearer <token>"

# Get all roles dengan permissions
curl -X GET "http://localhost:8080/api/roles?include=permissions" \
  -H "Authorization: Bearer <token>"

# Create new role
curl -X POST http://localhost:8080/api/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "roleCode": "READONLY_ANALYST",
    "roleName": "Read Only Analyst",
    "description": "Limited access for data analysis",
    "permissions": ["REPORT_VIEW", "AUDIT_VIEW", "CUSTOMER_VIEW"]
  }'

# Get permission matrix
curl -X GET "http://localhost:8080/api/permissions/matrix" \
  -H "Authorization: Bearer <token>"

# Check user permissions
curl -X GET "http://localhost:8080/api/users/{userId}/permissions" \
  -H "Authorization: Bearer <token>"

# Audit trail query
curl -X GET "http://localhost:8080/api/audit/rbac?from=2024-01-01&to=2024-01-31" \
  -H "Authorization: Bearer <token>"
```

## Functional Test Integration

### Page Object Model (sesuai RbacManagementFunctionalTest)
```java
// UserListPage elements
@FindBy(id = "user-table")
WebElement userTable;

@FindBy(className = "add-user-btn")
WebElement addUserButton;

@FindBy(id = "user-search")
WebElement searchInput;

@FindBy(id = "role-filter")
WebElement roleFilter;

// UserFormPage elements
@FindBy(id = "username")
WebElement usernameInput;

@FindBy(id = "email")
WebElement emailInput;

@FindBy(id = "full-name")
WebElement fullNameInput;

@FindBy(id = "role-assignments")
WebElement roleCheckboxes;

// RoleListPage elements
@FindBy(id = "role-table")
WebElement roleTable;

@FindBy(className = "add-role-btn")
WebElement addRoleButton;

// PermissionMatrixPage elements
@FindBy(id = "permission-matrix")
WebElement permissionMatrix;

@FindBy(className = "permission-checkbox")
List<WebElement> permissionCheckboxes;
```

### Test Data Integration dengan CSV Fixtures
```java
@ParameterizedTest
@CsvFileSource(resources = "/fixtures/user/user_creation_normal.csv", numLinesToSkip = 1)
void shouldCreateUserSuccessfully(String username, String email, String fullName, String roles) {
    // Test implementation menggunakan CSV data
}

@ParameterizedTest
@CsvFileSource(resources = "/fixtures/user/user_validation_errors.csv", numLinesToSkip = 1)
void shouldValidateUserInputErrors(String username, String email, String expectedError) {
    // Test validation errors menggunakan CSV data
}

@ParameterizedTest
@CsvFileSource(resources = "/fixtures/auth/role_permission_test_data.csv", numLinesToSkip = 1)
void shouldVerifyRolePermissions(String roleCode, String permissionCode, String hasPermission) {
    // Test role-permission mappings menggunakan CSV data
}
```

### Login Helper Integration (sesuai existing pattern)
```java
// RbacManagementFunctionalTest login pattern
@Override
protected void performInitialLogin() {
    // Login sebagai Manager yang has full USER dan ROLE permissions
    loginHelper.loginAsManager();
}

// Alternative login methods untuk different test scenarios
loginHelper.loginAsTeller();           // Limited permissions
loginHelper.loginAsCustomerService();  // Customer-focused permissions
loginHelper.loginAsAdmin();            // Full system access
```

## Cleanup Scripts

### Test Data Cleanup
```sql
-- Cleanup test users dan associated data (dalam correct order untuk foreign keys)
DELETE FROM user_roles WHERE id_users IN (
  SELECT id FROM users WHERE username LIKE 'TEST%'
);

DELETE FROM user_passwords WHERE id_users IN (
  SELECT id FROM users WHERE username LIKE 'TEST%'
);

DELETE FROM users WHERE username LIKE 'TEST%';

-- Cleanup test roles dan permissions
DELETE FROM role_permissions WHERE id_roles IN (
  SELECT id FROM roles WHERE role_code LIKE 'TEST%'
);

DELETE FROM roles WHERE role_code LIKE 'TEST%';

DELETE FROM permissions WHERE permission_code LIKE 'TEST%';

-- Reset BCrypt password hash untuk seed users (if needed untuk testing)
UPDATE user_passwords 
SET password_hash = '$2a$10$6tjICoD1DhK3r82bD4NiSuJ8A4xvf5osh96V7Q4BXFvIXZB3/s7da'
WHERE id_users IN (
  SELECT id FROM users WHERE username IN ('admin', 'manager1', 'teller1', 'cs1')
);

-- Reset failed login attempts
UPDATE users 
SET failed_login_attempts = 0, is_locked = false, locked_until = NULL
WHERE failed_login_attempts > 0 OR is_locked = true;
```

### Data Integrity Verification
```sql
-- Verify all users have at least one role
SELECT u.username 
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.id_users
WHERE ur.id_users IS NULL;

-- Verify all roles have at least one permission
SELECT r.role_code
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.id_roles
WHERE rp.id_roles IS NULL;

-- Check for duplicate role assignments
SELECT id_users, id_roles, COUNT(*)
FROM user_roles
GROUP BY id_users, id_roles
HAVING COUNT(*) > 1;
```

## Integration dengan Existing Functional Tests

### SQL Test Data Setup (sesuai @Sql annotations)
```java
@SqlGroup({
    @Sql(scripts = "/sql/setup-rbac-users-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-rbac-users-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
```

### Dependency pada Existing Test Infrastructure
- **BasePlaywrightTest**: Extends untuk common setup
- **LoginHelper**: Uses existing login automation
- **CSV Fixtures**: Leverages existing test data files
- **Page Object Model**: Follows established patterns
- **Database Setup**: Uses existing SQL scripts

### Cross-Module Integration Testing
- **User permissions affect account opening**: CS can create accounts
- **Role changes impact transaction processing**: Teller permissions for deposits
- **Manager approvals**: High-value transaction approvals
- **Audit requirements**: All administrative actions logged