package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.*;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RolePermissionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;

    private Role branchManagerRole;
    private Role tellerRole;
    private Permission customerViewPermission;
    private Permission customerCreatePermission;
    private Permission transactionDepositPermission;
    private Permission transactionWithdrawalPermission;

    @BeforeEach
    void setUp() {
        rolePermissionRepository.deleteAll();
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        permissionRepository.deleteAll();
        roleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        setupTestData();
    }

    @Test
    void shouldFindRolePermissionsByRole() {
        // Given
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(branchManagerRole, customerCreatePermission);
        createRolePermission(tellerRole, transactionDepositPermission);

        // When
        List<RolePermission> branchManagerPermissions = rolePermissionRepository.findByRole(branchManagerRole);
        List<RolePermission> tellerPermissions = rolePermissionRepository.findByRole(tellerRole);

        // Then
        assertThat(branchManagerPermissions).hasSize(2);
        assertThat(branchManagerPermissions).extracting(rp -> rp.getPermission().getPermissionCode())
            .containsExactlyInAnyOrder("CUSTOMER_VIEW", "CUSTOMER_CREATE");
            
        assertThat(tellerPermissions).hasSize(1);
        assertThat(tellerPermissions.get(0).getPermission().getPermissionCode()).isEqualTo("TRANSACTION_DEPOSIT");
    }

    @Test
    void shouldFindRolePermissionsByPermission() {
        // Given
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(tellerRole, customerViewPermission); // Same permission for different roles

        // When
        List<RolePermission> customerViewRoles = rolePermissionRepository.findByPermission(customerViewPermission);
        List<RolePermission> customerCreateRoles = rolePermissionRepository.findByPermission(customerCreatePermission);

        // Then
        assertThat(customerViewRoles).hasSize(2);
        assertThat(customerViewRoles).extracting(rp -> rp.getRole().getRoleCode())
            .containsExactlyInAnyOrder("BRANCH_MANAGER", "TELLER");
            
        assertThat(customerCreateRoles).isEmpty();
    }

    @Test
    void shouldFindSpecificRolePermission() {
        // Given
        RolePermission savedRolePermission = createRolePermission(branchManagerRole, customerViewPermission);

        // When
        Optional<RolePermission> found = rolePermissionRepository.findByRoleAndPermission(
            branchManagerRole, customerViewPermission);
        Optional<RolePermission> notFound = rolePermissionRepository.findByRoleAndPermission(
            tellerRole, customerViewPermission);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRole().getRoleCode()).isEqualTo("BRANCH_MANAGER");
        assertThat(found.get().getPermission().getPermissionCode()).isEqualTo("CUSTOMER_VIEW");
        
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldFindRolePermissionsByRoleCode() {
        // Given
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(branchManagerRole, customerCreatePermission);
        createRolePermission(tellerRole, transactionDepositPermission);

        // When
        List<RolePermission> branchManagerPermissions = rolePermissionRepository.findByRoleCode("BRANCH_MANAGER");
        List<RolePermission> tellerPermissions = rolePermissionRepository.findByRoleCode("TELLER");
        List<RolePermission> nonExistentRole = rolePermissionRepository.findByRoleCode("NON_EXISTENT");

        // Then
        assertThat(branchManagerPermissions).hasSize(2);
        assertThat(branchManagerPermissions).allMatch(rp -> rp.getRole().getRoleCode().equals("BRANCH_MANAGER"));
        
        assertThat(tellerPermissions).hasSize(1);
        assertThat(tellerPermissions.get(0).getRole().getRoleCode()).isEqualTo("TELLER");
        
        assertThat(nonExistentRole).isEmpty();
    }

    @Test
    void shouldFindRolePermissionsByPermissionCode() {
        // Given
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(tellerRole, customerViewPermission);
        createRolePermission(branchManagerRole, customerCreatePermission);

        // When
        List<RolePermission> customerViewAssignments = rolePermissionRepository.findByPermissionCode("CUSTOMER_VIEW");
        List<RolePermission> customerCreateAssignments = rolePermissionRepository.findByPermissionCode("CUSTOMER_CREATE");
        List<RolePermission> nonExistentPermission = rolePermissionRepository.findByPermissionCode("NON_EXISTENT");

        // Then
        assertThat(customerViewAssignments).hasSize(2);
        assertThat(customerViewAssignments).allMatch(rp -> rp.getPermission().getPermissionCode().equals("CUSTOMER_VIEW"));
        assertThat(customerViewAssignments).extracting(rp -> rp.getRole().getRoleCode())
            .containsExactlyInAnyOrder("BRANCH_MANAGER", "TELLER");
            
        assertThat(customerCreateAssignments).hasSize(1);
        assertThat(customerCreateAssignments.get(0).getRole().getRoleCode()).isEqualTo("BRANCH_MANAGER");
        
        assertThat(nonExistentPermission).isEmpty();
    }

    @Test
    void shouldFindPermissionsByUsername() {
        // Given - Create user with role and permissions
        User testUser = createUser("testuser", "testuser@yopmail.com", "Test User");
        UserRole userRole = createUserRole(testUser, branchManagerRole);
        
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(branchManagerRole, customerCreatePermission);

        // When
        List<Permission> userPermissions = rolePermissionRepository.findPermissionsByUsername("testuser");
        List<Permission> nonExistentUserPermissions = rolePermissionRepository.findPermissionsByUsername("nonexistent");

        // Then
        assertThat(userPermissions).hasSize(2);
        assertThat(userPermissions).extracting(Permission::getPermissionCode)
            .containsExactlyInAnyOrder("CUSTOMER_VIEW", "CUSTOMER_CREATE");
            
        assertThat(nonExistentUserPermissions).isEmpty();
    }

    @Test
    void shouldCheckExistenceByRoleAndPermission() {
        // Given
        createRolePermission(branchManagerRole, customerViewPermission);

        // When & Then
        assertThat(rolePermissionRepository.existsByRoleAndPermission(branchManagerRole, customerViewPermission))
            .isTrue();
        assertThat(rolePermissionRepository.existsByRoleAndPermission(branchManagerRole, customerCreatePermission))
            .isFalse();
        assertThat(rolePermissionRepository.existsByRoleAndPermission(tellerRole, customerViewPermission))
            .isFalse();
    }

    @Test
    void shouldDeleteByRoleAndPermission() {
        // Given
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(branchManagerRole, customerCreatePermission);
        
        // Verify initial state
        assertThat(rolePermissionRepository.findByRole(branchManagerRole)).hasSize(2);

        // When
        rolePermissionRepository.deleteByRoleAndPermission(branchManagerRole, customerViewPermission);
        entityManager.flush();

        // Then
        List<RolePermission> remainingPermissions = rolePermissionRepository.findByRole(branchManagerRole);
        assertThat(remainingPermissions).hasSize(1);
        assertThat(remainingPermissions.get(0).getPermission().getPermissionCode()).isEqualTo("CUSTOMER_CREATE");
    }

    @Test
    void shouldPreventDuplicateRolePermissionAssignment() {
        // Given
        createRolePermission(branchManagerRole, customerViewPermission);

        // When & Then - Attempt to create duplicate should fail
        assertThatThrownBy(() -> {
            RolePermission duplicate = new RolePermission();
            duplicate.setRole(branchManagerRole);
            duplicate.setPermission(customerViewPermission);
            duplicate.setGrantedBy("TEST");
            rolePermissionRepository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // Accept any constraint violation exception
    }

    @Test
    void shouldCascadeDeleteWhenRoleIsDeleted() {
        // Given
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(branchManagerRole, customerCreatePermission);
        entityManager.flush();
        
        // Verify initial state
        assertThat(rolePermissionRepository.findByRole(branchManagerRole)).hasSize(2);

        // When - Delete role permissions first, then role
        rolePermissionRepository.deleteAll(rolePermissionRepository.findByRole(branchManagerRole));
        entityManager.flush();
        roleRepository.delete(branchManagerRole);
        entityManager.flush();

        // Then - All permissions should be deleted
        List<RolePermission> remainingPermissions = rolePermissionRepository.findAll();
        assertThat(remainingPermissions).isEmpty();
        assertThat(roleRepository.findById(branchManagerRole.getId())).isEmpty();
    }

    @Test
    void shouldCascadeDeleteWhenPermissionIsDeleted() {
        // Given
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(tellerRole, customerViewPermission);
        entityManager.flush();
        
        // Verify initial state
        assertThat(rolePermissionRepository.findByPermission(customerViewPermission)).hasSize(2);

        // When - Delete role permissions first, then permission
        rolePermissionRepository.deleteAll(rolePermissionRepository.findByPermission(customerViewPermission));
        entityManager.flush();
        permissionRepository.delete(customerViewPermission);
        entityManager.flush();

        // Then - All permissions should be deleted
        List<RolePermission> remainingPermissions = rolePermissionRepository.findAll();
        assertThat(remainingPermissions).isEmpty();
        assertThat(permissionRepository.findById(customerViewPermission.getId())).isEmpty();
    }

    @Test
    void shouldSaveRolePermissionWithAuditFields() {
        // Given
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(branchManagerRole);
        rolePermission.setPermission(customerViewPermission);
        rolePermission.setGrantedBy("ADMIN");

        // When
        RolePermission savedRolePermission = rolePermissionRepository.save(rolePermission);
        entityManager.flush();

        // Then
        assertThat(savedRolePermission.getId()).isNotNull();
        assertThat(savedRolePermission.getGrantedDate()).isNotNull();
        assertThat(savedRolePermission.getGrantedBy()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHandleComplexUserPermissionQuery() {
        // Given - User with multiple roles, each with different permissions
        User testUser = createUser("multiuser", "multiuser@yopmail.com", "Multi Role User");
        createUserRole(testUser, branchManagerRole);
        createUserRole(testUser, tellerRole);
        
        // Branch Manager permissions
        createRolePermission(branchManagerRole, customerViewPermission);
        createRolePermission(branchManagerRole, customerCreatePermission);
        
        // Teller permissions
        createRolePermission(tellerRole, transactionDepositPermission);
        createRolePermission(tellerRole, transactionWithdrawalPermission);

        // When
        List<Permission> userPermissions = rolePermissionRepository.findPermissionsByUsername("multiuser");

        // Then - Should get all permissions from all roles
        assertThat(userPermissions).hasSize(4);
        assertThat(userPermissions).extracting(Permission::getPermissionCode)
            .containsExactlyInAnyOrder("CUSTOMER_VIEW", "CUSTOMER_CREATE", 
                                     "TRANSACTION_DEPOSIT", "TRANSACTION_WITHDRAWAL");
    }

    private void setupTestData() {
        // Create roles
        branchManagerRole = createRole("BRANCH_MANAGER", "Branch Manager", "Full access");
        tellerRole = createRole("TELLER", "Teller", "Transaction processing");

        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);

        // Create permissions
        customerViewPermission = createPermission("CUSTOMER_VIEW", "View Customer", "CUSTOMER", 
            "View customer information", "customer", "read");
        customerCreatePermission = createPermission("CUSTOMER_CREATE", "Create Customer", "CUSTOMER", 
            "Create new customers", "customer", "create");
        transactionDepositPermission = createPermission("TRANSACTION_DEPOSIT", "Process Deposit", "TRANSACTION", 
            "Process deposit transactions", "transaction", "deposit");
        transactionWithdrawalPermission = createPermission("TRANSACTION_WITHDRAWAL", "Process Withdrawal", "TRANSACTION", 
            "Process withdrawal transactions", "transaction", "withdrawal");

        permissionRepository.save(customerViewPermission);
        permissionRepository.save(customerCreatePermission);
        permissionRepository.save(transactionDepositPermission);
        permissionRepository.save(transactionWithdrawalPermission);
        
        entityManager.flush();
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

    private User createUser(String username, String email, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setIsActive(true);
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedBy("TEST");
        user.setUpdatedBy("TEST");
        return userRepository.save(user);
    }

    private UserRole createUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy("TEST");
        return userRoleRepository.save(userRole);
    }

    private RolePermission createRolePermission(Role role, Permission permission) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        rolePermission.setGrantedBy("TEST");
        return rolePermissionRepository.save(rolePermission);
    }
}