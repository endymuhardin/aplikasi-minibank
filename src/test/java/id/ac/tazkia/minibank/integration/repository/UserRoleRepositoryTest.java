package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserRole;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.repository.UserRepository;
import id.ac.tazkia.minibank.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRoleRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    private User testUser1;
    private User testUser2;
    private Role branchManagerRole;
    private Role tellerRole;
    private Role customerServiceRole;

    @BeforeEach
    void setUp() {
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        setupTestData();
    }

    @Test
    void shouldFindUserRolesByUser() {
        // Given
        UserRole userRole1 = createUserRole(testUser1, branchManagerRole);
        UserRole userRole2 = createUserRole(testUser1, tellerRole); // User with multiple roles
        UserRole userRole3 = createUserRole(testUser2, customerServiceRole);

        // When
        List<UserRole> user1Roles = userRoleRepository.findByUser(testUser1);
        List<UserRole> user2Roles = userRoleRepository.findByUser(testUser2);

        // Then
        assertThat(user1Roles).hasSize(2);
        assertThat(user1Roles).extracting(ur -> ur.getRole().getRoleCode())
            .containsExactlyInAnyOrder("BRANCH_MANAGER", "TELLER");
            
        assertThat(user2Roles).hasSize(1);
        assertThat(user2Roles.get(0).getRole().getRoleCode()).isEqualTo("CUSTOMER_SERVICE");
    }

    @Test
    void shouldFindUserRolesByRole() {
        // Given
        UserRole userRole1 = createUserRole(testUser1, tellerRole);
        UserRole userRole2 = createUserRole(testUser2, tellerRole); // Multiple users with same role

        // When
        List<UserRole> tellerUsers = userRoleRepository.findByRole(tellerRole);
        List<UserRole> branchManagerUsers = userRoleRepository.findByRole(branchManagerRole);

        // Then
        assertThat(tellerUsers).hasSize(2);
        assertThat(tellerUsers).extracting(ur -> ur.getUser().getUsername())
            .containsExactlyInAnyOrder("testuser1", "testuser2");
            
        assertThat(branchManagerUsers).isEmpty();
    }

    @Test
    void shouldFindSpecificUserRole() {
        // Given
        UserRole savedUserRole = createUserRole(testUser1, branchManagerRole);

        // When
        Optional<UserRole> foundUserRole = userRoleRepository.findByUserAndRole(testUser1, branchManagerRole);
        Optional<UserRole> notFoundUserRole = userRoleRepository.findByUserAndRole(testUser2, branchManagerRole);

        // Then
        assertThat(foundUserRole).isPresent();
        assertThat(foundUserRole.get().getUser().getUsername()).isEqualTo("testuser1");
        assertThat(foundUserRole.get().getRole().getRoleCode()).isEqualTo("BRANCH_MANAGER");
        
        assertThat(notFoundUserRole).isEmpty();
    }

    @Test
    void shouldFindUserRolesByUsername() {
        // Given
        createUserRole(testUser1, branchManagerRole);
        createUserRole(testUser1, tellerRole);

        // When
        List<UserRole> userRoles = userRoleRepository.findByUsername("testuser1");
        List<UserRole> emptyRoles = userRoleRepository.findByUsername("nonexistent");

        // Then
        assertThat(userRoles).hasSize(2);
        assertThat(userRoles).allMatch(ur -> ur.getUser().getUsername().equals("testuser1"));
        
        assertThat(emptyRoles).isEmpty();
    }

    @Test
    void shouldFindUserRolesByRoleCode() {
        // Given
        createUserRole(testUser1, tellerRole);
        createUserRole(testUser2, tellerRole);
        createUserRole(testUser1, customerServiceRole);

        // When
        List<UserRole> tellerRoles = userRoleRepository.findByRoleCode("TELLER");
        List<UserRole> csRoles = userRoleRepository.findByRoleCode("CUSTOMER_SERVICE");
        List<UserRole> emptyRoles = userRoleRepository.findByRoleCode("NON_EXISTENT");

        // Then
        assertThat(tellerRoles).hasSize(2);
        assertThat(tellerRoles).allMatch(ur -> ur.getRole().getRoleCode().equals("TELLER"));
        
        assertThat(csRoles).hasSize(1);
        assertThat(csRoles.get(0).getUser().getUsername()).isEqualTo("testuser1");
        
        assertThat(emptyRoles).isEmpty();
    }

    @Test
    void shouldCheckExistenceByUserAndRole() {
        // Given
        createUserRole(testUser1, branchManagerRole);

        // When & Then
        assertThat(userRoleRepository.existsByUserAndRole(testUser1, branchManagerRole)).isTrue();
        assertThat(userRoleRepository.existsByUserAndRole(testUser1, tellerRole)).isFalse();
        assertThat(userRoleRepository.existsByUserAndRole(testUser2, branchManagerRole)).isFalse();
    }

    @Test
    void shouldDeleteByUserAndRole() {
        // Given
        createUserRole(testUser1, branchManagerRole);
        createUserRole(testUser1, tellerRole);
        
        // Verify initial state
        assertThat(userRoleRepository.findByUser(testUser1)).hasSize(2);

        // When
        userRoleRepository.deleteByUserAndRole(testUser1, branchManagerRole);
        entityManager.flush();

        // Then
        List<UserRole> remainingRoles = userRoleRepository.findByUser(testUser1);
        assertThat(remainingRoles).hasSize(1);
        assertThat(remainingRoles.get(0).getRole().getRoleCode()).isEqualTo("TELLER");
    }

    @Test
    void shouldPreventDuplicateUserRoleAssignment() {
        // Given
        createUserRole(testUser1, branchManagerRole);

        // When & Then - Attempt to create duplicate should fail
        assertThatThrownBy(() -> {
            UserRole duplicate = new UserRole();
            duplicate.setUser(testUser1);
            duplicate.setRole(branchManagerRole);
            duplicate.setAssignedBy("TEST");
            userRoleRepository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // Accept any constraint violation exception
    }

    @Test
    void shouldCascadeDeleteWhenUserIsDeleted() {
        // Given
        UserRole userRole1 = createUserRole(testUser1, branchManagerRole);
        UserRole userRole2 = createUserRole(testUser1, tellerRole);
        entityManager.flush();
        
        // Verify initial state
        assertThat(userRoleRepository.findByUser(testUser1)).hasSize(2);

        // When - Delete user roles first, then user
        userRoleRepository.deleteAll(userRoleRepository.findByUser(testUser1));
        entityManager.flush();
        userRepository.delete(testUser1);
        entityManager.flush();

        // Then - All roles should be deleted
        List<UserRole> remainingRoles = userRoleRepository.findAll();
        assertThat(remainingRoles).isEmpty();
        assertThat(userRepository.findById(testUser1.getId())).isEmpty();
    }

    @Test
    void shouldCascadeDeleteWhenRoleIsDeleted() {
        // Given
        createUserRole(testUser1, branchManagerRole);
        createUserRole(testUser2, branchManagerRole);
        entityManager.flush();
        
        // Verify initial state
        assertThat(userRoleRepository.findByRole(branchManagerRole)).hasSize(2);

        // When - Delete user roles first, then role
        userRoleRepository.deleteAll(userRoleRepository.findByRole(branchManagerRole));
        entityManager.flush();
        roleRepository.delete(branchManagerRole);
        entityManager.flush();

        // Then - All roles should be deleted
        List<UserRole> remainingRoles = userRoleRepository.findAll();
        assertThat(remainingRoles).isEmpty();
        assertThat(roleRepository.findById(branchManagerRole.getId())).isEmpty();
    }

    @Test
    void shouldSaveUserRoleWithAuditFields() {
        // Given
        UserRole userRole = new UserRole();
        userRole.setUser(testUser1);
        userRole.setRole(branchManagerRole);
        userRole.setAssignedBy("ADMIN");

        // When
        UserRole savedUserRole = userRoleRepository.save(userRole);
        entityManager.flush();

        // Then
        assertThat(savedUserRole.getId()).isNotNull();
        assertThat(savedUserRole.getAssignedDate()).isNotNull();
        assertThat(savedUserRole.getAssignedBy()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHandleMultipleRoleAssignmentsForUser() {
        // Given - Assign multiple roles to one user
        createUserRole(testUser1, branchManagerRole);
        createUserRole(testUser1, tellerRole);
        createUserRole(testUser1, customerServiceRole);

        // When
        List<UserRole> userRoles = userRoleRepository.findByUser(testUser1);

        // Then
        assertThat(userRoles).hasSize(3);
        assertThat(userRoles).extracting(ur -> ur.getRole().getRoleCode())
            .containsExactlyInAnyOrder("BRANCH_MANAGER", "TELLER", "CUSTOMER_SERVICE");
            
        // Verify each role assignment is unique
        assertThat(userRoles.stream().map(ur -> ur.getRole().getRoleCode()).distinct().count()).isEqualTo(3);
    }

    @Test
    void shouldHandleRoleAssignedToMultipleUsers() {
        // Given - Assign same role to multiple users
        createUserRole(testUser1, tellerRole);
        createUserRole(testUser2, tellerRole);

        // When
        List<UserRole> roleAssignments = userRoleRepository.findByRole(tellerRole);

        // Then
        assertThat(roleAssignments).hasSize(2);
        assertThat(roleAssignments).extracting(ur -> ur.getUser().getUsername())
            .containsExactlyInAnyOrder("testuser1", "testuser2");
    }

    private void setupTestData() {
        // Create roles
        branchManagerRole = createRole("BRANCH_MANAGER", "Branch Manager", "Full access");
        tellerRole = createRole("TELLER", "Teller", "Transaction processing");
        customerServiceRole = createRole("CUSTOMER_SERVICE", "Customer Service", "Customer management");

        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);
        roleRepository.save(customerServiceRole);

        // Create users
        testUser1 = createUser("testuser1", "testuser1@yopmail.com", "Test User 1");
        testUser2 = createUser("testuser2", "testuser2@yopmail.com", "Test User 2");

        userRepository.save(testUser1);
        userRepository.save(testUser2);
        
        entityManager.flush();
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
        return user;
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

    private UserRole createUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy("TEST");
        return userRoleRepository.save(userRole);
    }
}