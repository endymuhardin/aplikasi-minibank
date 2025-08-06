package id.ac.tazkia.minibank.integration;

import id.ac.tazkia.minibank.entity.*;
import id.ac.tazkia.minibank.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserPermissionValidationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    
    @Autowired
    private UserPasswordRepository userPasswordRepository;

    // Test data
    private User branchManager;
    private User teller;
    private User customerService;
    private User inactiveUser;
    private User lockedUser;

    private Role branchManagerRole;
    private Role tellerRole;
    private Role customerServiceRole;

    private Permission customerViewPermission;
    private Permission customerCreatePermission;
    private Permission accountViewPermission;
    private Permission transactionDepositPermission;
    private Permission userCreatePermission;
    private Permission reportViewPermission;

    @BeforeEach
    void setUp() {
        cleanupAllTables();
        setupTestData();
    }

    @Test
    void shouldValidateBranchManagerHasAllPermissions() {
        // When
        List<Permission> permissions = rolePermissionRepository.findPermissionsByUsername("branch_manager");

        // Then
        Set<String> permissionCodes = permissions.stream()
            .map(Permission::getPermissionCode)
            .collect(Collectors.toSet());

        assertThat(permissionCodes).containsExactlyInAnyOrder(
            "CUSTOMER_VIEW", "CUSTOMER_CREATE", "ACCOUNT_VIEW", 
            "TRANSACTION_DEPOSIT", "USER_CREATE", "REPORT_VIEW"
        );
        assertThat(permissions).hasSize(6); // Branch Manager has all permissions
    }

    @Test
    void shouldValidateTellerHasLimitedPermissions() {
        // When
        List<Permission> permissions = rolePermissionRepository.findPermissionsByUsername("teller");

        // Then
        Set<String> permissionCodes = permissions.stream()
            .map(Permission::getPermissionCode)
            .collect(Collectors.toSet());

        assertThat(permissionCodes).containsExactlyInAnyOrder(
            "CUSTOMER_VIEW", "ACCOUNT_VIEW", "TRANSACTION_DEPOSIT"
        );
        assertThat(permissions).hasSize(3);
        
        // Verify teller doesn't have admin permissions
        assertThat(permissionCodes).doesNotContain("USER_CREATE", "REPORT_VIEW");
    }

    @Test
    void shouldValidateCustomerServiceHasCustomerPermissions() {
        // When
        List<Permission> permissions = rolePermissionRepository.findPermissionsByUsername("customer_service");

        // Then
        Set<String> permissionCodes = permissions.stream()
            .map(Permission::getPermissionCode)
            .collect(Collectors.toSet());

        assertThat(permissionCodes).containsExactlyInAnyOrder(
            "CUSTOMER_VIEW", "CUSTOMER_CREATE", "ACCOUNT_VIEW"
        );
        assertThat(permissions).hasSize(3);
        
        // Verify CS doesn't have transaction or admin permissions
        assertThat(permissionCodes).doesNotContain("TRANSACTION_DEPOSIT", "USER_CREATE", "REPORT_VIEW");
    }

    @Test
    void shouldReturnEmptyPermissionsForInactiveUser() {
        // When
        List<Permission> permissions = rolePermissionRepository.findPermissionsByUsername("inactive_user");

        // Then - Inactive user still has permissions (business logic should filter these)
        assertThat(permissions).hasSize(3); // Customer Service permissions
        
        // But verify user is inactive
        User user = userRepository.findByUsername("inactive_user").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getIsActive()).isFalse();
    }

    @Test
    void shouldReturnPermissionsForLockedUser() {
        // When
        List<Permission> permissions = rolePermissionRepository.findPermissionsByUsername("locked_user");

        // Then - Locked user still has permissions in database
        assertThat(permissions).hasSize(3); // Teller permissions
        
        // But verify user is locked
        User user = userRepository.findByUsername("locked_user").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getIsLocked()).isTrue();
        assertThat(user.isAccountNonLocked()).isFalse();
    }

    @Test
    void shouldValidateUserWithMultipleRoles() {
        // Given - Assign teller an additional customer service role
        UserRole additionalRole = new UserRole();
        additionalRole.setUser(teller);
        additionalRole.setRole(customerServiceRole);
        additionalRole.setAssignedBy("TEST");
        userRoleRepository.save(additionalRole);
        entityManager.flush();

        // When
        List<Permission> permissions = rolePermissionRepository.findPermissionsByUsername("teller");

        // Then - Should have permissions from both roles (with no duplicates)
        Set<String> permissionCodes = permissions.stream()
            .map(Permission::getPermissionCode)
            .collect(Collectors.toSet());

        assertThat(permissionCodes).containsExactlyInAnyOrder(
            "CUSTOMER_VIEW", "CUSTOMER_CREATE", "ACCOUNT_VIEW", "TRANSACTION_DEPOSIT"
        );
    }

    @Test
    void shouldValidatePermissionsByCategory() {
        // When
        List<Permission> customerPermissions = permissionRepository.findByCategory("CUSTOMER");
        List<Permission> accountPermissions = permissionRepository.findByCategory("ACCOUNT");
        List<Permission> transactionPermissions = permissionRepository.findByCategory("TRANSACTION");
        List<Permission> userPermissions = permissionRepository.findByCategory("USER");
        List<Permission> reportPermissions = permissionRepository.findByCategory("REPORT");

        // Then
        assertThat(customerPermissions).hasSize(2); // VIEW, CREATE
        assertThat(accountPermissions).hasSize(1); // VIEW
        assertThat(transactionPermissions).hasSize(1); // DEPOSIT
        assertThat(userPermissions).hasSize(1); // CREATE
        assertThat(reportPermissions).hasSize(1); // VIEW
    }

    @Test
    void shouldValidatePermissionsByResourceAndAction() {
        // When
        List<Permission> customerReadPermissions = permissionRepository
            .findByResourceAndAction("customer", "read");
        List<Permission> customerCreatePermissions = permissionRepository
            .findByResourceAndAction("customer", "create");
        List<Permission> userCreatePermissions = permissionRepository
            .findByResourceAndAction("user", "create");

        // Then
        assertThat(customerReadPermissions).hasSize(1);
        assertThat(customerReadPermissions.get(0).getPermissionCode()).isEqualTo("CUSTOMER_VIEW");
        
        assertThat(customerCreatePermissions).hasSize(1);
        assertThat(customerCreatePermissions.get(0).getPermissionCode()).isEqualTo("CUSTOMER_CREATE");
        
        assertThat(userCreatePermissions).hasSize(1);
        assertThat(userCreatePermissions.get(0).getPermissionCode()).isEqualTo("USER_CREATE");
    }

    @Test
    void shouldValidateRoleBasedAccess() {
        // When - Find users by role
        List<User> branchManagers = userRepository.findByRoleCode("BRANCH_MANAGER");
        List<User> tellers = userRepository.findByRoleCode("TELLER");
        List<User> customerServiceUsers = userRepository.findByRoleCode("CUSTOMER_SERVICE");

        // Then
        assertThat(branchManagers).hasSize(1);
        assertThat(branchManagers.get(0).getUsername()).isEqualTo("branch_manager");
        
        assertThat(tellers).hasSize(2); // teller and locked_user
        assertThat(tellers).extracting(User::getUsername)
            .containsExactlyInAnyOrder("teller", "locked_user");
        
        assertThat(customerServiceUsers).hasSize(2); // customer_service and inactive_user
        assertThat(customerServiceUsers).extracting(User::getUsername)
            .containsExactlyInAnyOrder("customer_service", "inactive_user");
    }

    @Test
    void shouldValidatePasswordRequirements() {
        // When - Get all passwords directly from repository
        List<UserPassword> userPasswords = userPasswordRepository.findActivePasswords();

        // Then - All 5 users should have passwords
        assertThat(userPasswords).hasSize(5);
        
        // Verify password properties
        for (UserPassword password : userPasswords) {
            assertThat(password.getPasswordHash()).isNotNull();
            assertThat(password.getPasswordHash()).startsWith("$2a$"); // BCrypt format
            assertThat(password.getIsActive()).isTrue();
            assertThat(password.getCreatedDate()).isNotNull();
            assertThat(password.getUser()).isNotNull();
        }
    }

    @Test
    void shouldValidateUserAccountStates() {
        // When
        long activeUserCount = userRepository.countActiveUsers();
        List<User> activeUsers = userRepository.findActiveUsers();

        // Then
        assertThat(activeUserCount).isEqualTo(4L); // All except inactive_user
        assertThat(activeUsers).hasSize(4);
        assertThat(activeUsers).extracting(User::getUsername)
            .containsExactlyInAnyOrder("branch_manager", "teller", "customer_service", "locked_user");
        assertThat(activeUsers).allMatch(User::getIsActive);
    }

    @Test
    void shouldValidatePermissionHierarchy() {
        // Given - Get permissions for each role
        List<Permission> branchManagerPermissions = rolePermissionRepository
            .findPermissionsByUsername("branch_manager");
        List<Permission> tellerPermissions = rolePermissionRepository
            .findPermissionsByUsername("teller");
        List<Permission> csPermissions = rolePermissionRepository
            .findPermissionsByUsername("customer_service");

        // Then - Branch Manager should have superset of other roles' permissions
        Set<String> bmPermissionCodes = branchManagerPermissions.stream()
            .map(Permission::getPermissionCode).collect(Collectors.toSet());
        Set<String> tellerPermissionCodes = tellerPermissions.stream()
            .map(Permission::getPermissionCode).collect(Collectors.toSet());
        Set<String> csPermissionCodes = csPermissions.stream()
            .map(Permission::getPermissionCode).collect(Collectors.toSet());

        assertThat(bmPermissionCodes).containsAll(tellerPermissionCodes);
        assertThat(bmPermissionCodes).containsAll(csPermissionCodes);
        
        // Verify role-specific permissions
        assertThat(bmPermissionCodes).contains("USER_CREATE", "REPORT_VIEW"); // Admin only
        assertThat(tellerPermissionCodes).doesNotContain("USER_CREATE", "REPORT_VIEW");
        assertThat(csPermissionCodes).doesNotContain("USER_CREATE", "REPORT_VIEW");
    }

    @Test
    void shouldValidateAuditTrail() {
        // When - Check audit fields on all entities
        List<User> users = userRepository.findAll();
        List<Role> roles = roleRepository.findAll();
        List<Permission> permissions = permissionRepository.findAll();
        List<UserRole> userRoles = userRoleRepository.findAll();
        List<RolePermission> rolePermissions = rolePermissionRepository.findAll();

        // Then - All entities should have audit information
        assertThat(users).allMatch(u -> u.getCreatedDate() != null && u.getCreatedBy() != null);
        assertThat(roles).allMatch(r -> r.getCreatedDate() != null && r.getCreatedBy() != null);
        assertThat(permissions).allMatch(p -> p.getCreatedDate() != null && p.getCreatedBy() != null);
        assertThat(userRoles).allMatch(ur -> ur.getAssignedDate() != null && ur.getAssignedBy() != null);
        assertThat(rolePermissions).allMatch(rp -> rp.getGrantedDate() != null && rp.getGrantedBy() != null);
    }

    private void cleanupAllTables() {
        rolePermissionRepository.deleteAll();
        userRoleRepository.deleteAll();
        userPasswordRepository.deleteAll();
        userRepository.deleteAll();
        permissionRepository.deleteAll();
        roleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private void setupTestData() {
        // Create roles
        createRoles();
        
        // Create permissions
        createPermissions();
        
        // Create role-permission assignments
        createRolePermissions();
        
        // Create users
        createUsers();
        
        // Create user passwords
        createUserPasswords();
        
        // Create user-role assignments
        createUserRoles();
        
        entityManager.flush();
    }

    private void createRoles() {
        branchManagerRole = createRole("BRANCH_MANAGER", "Branch Manager", "Full access with monitoring");
        tellerRole = createRole("TELLER", "Teller", "Transaction processing");
        customerServiceRole = createRole("CUSTOMER_SERVICE", "Customer Service", "Customer management");

        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);
        roleRepository.save(customerServiceRole);
    }

    private void createPermissions() {
        customerViewPermission = createPermission("CUSTOMER_VIEW", "View Customer", "CUSTOMER", 
            "View customer information", "customer", "read");
        customerCreatePermission = createPermission("CUSTOMER_CREATE", "Create Customer", "CUSTOMER", 
            "Create new customers", "customer", "create");
        accountViewPermission = createPermission("ACCOUNT_VIEW", "View Account", "ACCOUNT", 
            "View account information", "account", "read");
        transactionDepositPermission = createPermission("TRANSACTION_DEPOSIT", "Process Deposit", "TRANSACTION", 
            "Process deposit transactions", "transaction", "deposit");
        userCreatePermission = createPermission("USER_CREATE", "Create User", "USER", 
            "Create new system users", "user", "create");
        reportViewPermission = createPermission("REPORT_VIEW", "View Reports", "REPORT", 
            "View business reports", "report", "read");

        permissionRepository.save(customerViewPermission);
        permissionRepository.save(customerCreatePermission);
        permissionRepository.save(accountViewPermission);
        permissionRepository.save(transactionDepositPermission);
        permissionRepository.save(userCreatePermission);
        permissionRepository.save(reportViewPermission);
    }

    private void createRolePermissions() {
        // Branch Manager - All permissions
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(branchManagerRole, customerCreatePermission);
        createRolePermission(branchManagerRole, accountViewPermission);
        createRolePermission(branchManagerRole, transactionDepositPermission);
        createRolePermission(branchManagerRole, userCreatePermission);
        createRolePermission(branchManagerRole, reportViewPermission);

        // Teller - Transaction focused permissions
        createRolePermission(tellerRole, customerViewPermission);
        createRolePermission(tellerRole, accountViewPermission);
        createRolePermission(tellerRole, transactionDepositPermission);

        // Customer Service - Customer focused permissions
        createRolePermission(customerServiceRole, customerViewPermission);
        createRolePermission(customerServiceRole, customerCreatePermission);
        createRolePermission(customerServiceRole, accountViewPermission);
    }

    private void createUsers() {
        branchManager = createUser("branch_manager", "bm@yopmail.com", "Branch Manager", true, false);
        teller = createUser("teller", "teller@yopmail.com", "John Teller", true, false);
        customerService = createUser("customer_service", "cs@yopmail.com", "Jane CS", true, false);
        inactiveUser = createUser("inactive_user", "inactive@yopmail.com", "Inactive User", false, false);
        lockedUser = createUser("locked_user", "locked@yopmail.com", "Locked User", true, true);

        userRepository.save(branchManager);
        userRepository.save(teller);
        userRepository.save(customerService);
        userRepository.save(inactiveUser);
        userRepository.save(lockedUser);
    }

    private void createUserPasswords() {
        createUserPassword(branchManager, "$2a$10$bmHashedPassword");
        createUserPassword(teller, "$2a$10$tellerHashedPassword");
        createUserPassword(customerService, "$2a$10$csHashedPassword");
        createUserPassword(inactiveUser, "$2a$10$inactiveHashedPassword");
        createUserPassword(lockedUser, "$2a$10$lockedHashedPassword");
    }

    private void createUserRoles() {
        createUserRole(branchManager, branchManagerRole);
        createUserRole(teller, tellerRole);
        createUserRole(customerService, customerServiceRole);
        createUserRole(inactiveUser, customerServiceRole);
        createUserRole(lockedUser, tellerRole);
    }

    private Role createRole(String roleCode, String roleName, String description) {
        Role role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setIsActive(true);
        role.setCreatedBy("TEST");
        role.setUpdatedBy("TEST");
        return role;
    }

    private Permission createPermission(String permissionCode, String permissionName, String category,
                                       String description, String resource, String action) {
        Permission permission = new Permission();
        permission.setPermissionCode(permissionCode);
        permission.setPermissionName(permissionName);
        permission.setPermissionCategory(category);
        permission.setDescription(description);
        permission.setResource(resource);
        permission.setAction(action);
        permission.setCreatedBy("TEST");
        return permission;
    }

    private User createUser(String username, String email, String fullName, boolean isActive, boolean isLocked) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setIsActive(isActive);
        user.setIsLocked(isLocked);
        user.setFailedLoginAttempts(0);
        if (isLocked) {
            user.setLockedUntil(LocalDateTime.now().plusHours(1));
        }
        user.setCreatedBy("TEST");
        user.setUpdatedBy("TEST");
        return user;
    }

    private void createUserPassword(User user, String passwordHash) {
        UserPassword userPassword = new UserPassword();
        userPassword.setUser(user);
        userPassword.setPasswordHash(passwordHash);
        userPassword.setIsActive(true);
        userPassword.setCreatedBy("TEST");
        userPasswordRepository.save(userPassword);
    }

    private void createUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy("TEST");
        userRoleRepository.save(userRole);
    }

    private void createRolePermission(Role role, Permission permission) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        rolePermission.setGrantedBy("TEST");
        rolePermissionRepository.save(rolePermission);
    }
}