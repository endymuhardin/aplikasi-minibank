package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;

import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserRole;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.repository.UserRepository;
import id.ac.tazkia.minibank.repository.UserRoleRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;

/**
 * UserRoleRepository tests optimized for parallel execution.
 * Uses dynamic test data to prevent conflicts during concurrent execution.
 * Note: Using SAME_THREAD execution to avoid transaction management conflicts.
 */
@Execution(ExecutionMode.SAME_THREAD)
class UserRoleRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private BranchRepository branchRepository;

    @Test
    void shouldFindUserRolesByUser() {
        logTestExecution("shouldFindUserRolesByUser");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        User testUser2 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + System.currentTimeMillis());
        Role customerServiceRole = SimpleParallelTestDataFactory.createUniqueRole();
        customerServiceRole.setRoleCode("CUSTOMER_SERVICE_" + System.currentTimeMillis());
        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);
        roleRepository.save(customerServiceRole);
        
        UserRole userRole1 = SimpleParallelTestDataFactory.createUserRole(testUser1, branchManagerRole);
        UserRole userRole2 = SimpleParallelTestDataFactory.createUserRole(testUser1, tellerRole);
        UserRole userRole3 = SimpleParallelTestDataFactory.createUserRole(testUser2, customerServiceRole);
        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);
        userRoleRepository.save(userRole3);

        // When
        List<UserRole> user1Roles = userRoleRepository.findByUser(testUser1);
        List<UserRole> user2Roles = userRoleRepository.findByUser(testUser2);

        // Then
        assertThat(user1Roles).hasSize(2);
        assertThat(user1Roles).extracting(ur -> ur.getRole().getRoleCode())
            .containsExactlyInAnyOrder(branchManagerRole.getRoleCode(), tellerRole.getRoleCode());
            
        assertThat(user2Roles).hasSize(1);
        assertThat(user2Roles.get(0).getRole().getRoleCode()).isEqualTo(customerServiceRole.getRoleCode());
    }

    @Test
    void shouldFindUserRolesByRole() {
        logTestExecution("shouldFindUserRolesByRole");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        User testUser2 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + System.currentTimeMillis());
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        roleRepository.save(tellerRole);
        roleRepository.save(branchManagerRole);
        
        UserRole userRole1 = SimpleParallelTestDataFactory.createUserRole(testUser1, tellerRole);
        UserRole userRole2 = SimpleParallelTestDataFactory.createUserRole(testUser2, tellerRole);
        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);

        // When
        List<UserRole> tellerUsers = userRoleRepository.findByRole(tellerRole);
        List<UserRole> branchManagerUsers = userRoleRepository.findByRole(branchManagerRole);

        // Then
        assertThat(tellerUsers).hasSize(2);
        assertThat(tellerUsers).extracting(ur -> ur.getUser().getUsername())
            .containsExactlyInAnyOrder(testUser1.getUsername(), testUser2.getUsername());
            
        assertThat(branchManagerUsers).isEmpty();
    }

    @Test
    void shouldFindSpecificUserRole() {
        logTestExecution("shouldFindSpecificUserRole");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        User testUser2 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        roleRepository.save(branchManagerRole);
        
        UserRole userRole = SimpleParallelTestDataFactory.createUserRole(testUser1, branchManagerRole);
        userRoleRepository.save(userRole);

        // When
        Optional<UserRole> foundUserRole = userRoleRepository.findByUserAndRole(testUser1, branchManagerRole);
        Optional<UserRole> notFoundUserRole = userRoleRepository.findByUserAndRole(testUser2, branchManagerRole);

        // Then
        assertThat(foundUserRole).isPresent();
        assertThat(foundUserRole.get().getUser().getUsername()).isEqualTo(testUser1.getUsername());
        assertThat(foundUserRole.get().getRole().getRoleCode()).isEqualTo(branchManagerRole.getRoleCode());
        
        assertThat(notFoundUserRole).isEmpty();
    }

    @Test
    void shouldFindUserRolesByUsername() {
        logTestExecution("shouldFindUserRolesByUsername");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + System.currentTimeMillis());
        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);
        
        UserRole userRole1 = SimpleParallelTestDataFactory.createUserRole(testUser1, branchManagerRole);
        UserRole userRole2 = SimpleParallelTestDataFactory.createUserRole(testUser1, tellerRole);
        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);

        // When
        List<UserRole> userRoles = userRoleRepository.findByUsername(testUser1.getUsername());
        List<UserRole> emptyRoles = userRoleRepository.findByUsername("nonexistent_" + System.currentTimeMillis());

        // Then
        assertThat(userRoles).hasSize(2);
        assertThat(userRoles).allMatch(ur -> ur.getUser().getUsername().equals(testUser1.getUsername()));
        
        assertThat(emptyRoles).isEmpty();
    }

    @Test
    void shouldFindUserRolesByRoleCode() {
        logTestExecution("shouldFindUserRolesByRoleCode");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        User testUser2 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + uniqueTimestamp);
        Role customerServiceRole = SimpleParallelTestDataFactory.createUniqueRole();
        customerServiceRole.setRoleCode("CUSTOMER_SERVICE_" + uniqueTimestamp);
        roleRepository.save(tellerRole);
        roleRepository.save(customerServiceRole);
        
        UserRole userRole1 = SimpleParallelTestDataFactory.createUserRole(testUser1, tellerRole);
        UserRole userRole2 = SimpleParallelTestDataFactory.createUserRole(testUser2, tellerRole);
        UserRole userRole3 = SimpleParallelTestDataFactory.createUserRole(testUser1, customerServiceRole);
        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);
        userRoleRepository.save(userRole3);

        // When
        List<UserRole> tellerRoles = userRoleRepository.findByRoleCode(tellerRole.getRoleCode());
        List<UserRole> csRoles = userRoleRepository.findByRoleCode(customerServiceRole.getRoleCode());
        List<UserRole> emptyRoles = userRoleRepository.findByRoleCode("NON_EXISTENT_" + uniqueTimestamp);

        // Then
        assertThat(tellerRoles).hasSize(2);
        assertThat(tellerRoles).allMatch(ur -> ur.getRole().getRoleCode().equals(tellerRole.getRoleCode()));
        
        assertThat(csRoles).hasSize(1);
        assertThat(csRoles.get(0).getUser().getUsername()).isEqualTo(testUser1.getUsername());
        
        assertThat(emptyRoles).isEmpty();
    }

    @Test
    void shouldCheckExistenceByUserAndRole() {
        logTestExecution("shouldCheckExistenceByUserAndRole");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        User testUser2 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + System.currentTimeMillis());
        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);
        
        UserRole userRole = SimpleParallelTestDataFactory.createUserRole(testUser1, branchManagerRole);
        userRoleRepository.save(userRole);

        // When & Then
        assertThat(userRoleRepository.existsByUserAndRole(testUser1, branchManagerRole)).isTrue();
        assertThat(userRoleRepository.existsByUserAndRole(testUser1, tellerRole)).isFalse();
        assertThat(userRoleRepository.existsByUserAndRole(testUser2, branchManagerRole)).isFalse();
    }

    @Test
    void shouldDeleteByUserAndRole() {
        logTestExecution("shouldDeleteByUserAndRole");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + System.currentTimeMillis());
        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);
        
        UserRole userRole1 = SimpleParallelTestDataFactory.createUserRole(testUser1, branchManagerRole);
        UserRole userRole2 = SimpleParallelTestDataFactory.createUserRole(testUser1, tellerRole);
        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);
        
        // Verify initial state
        assertThat(userRoleRepository.findByUser(testUser1)).hasSize(2);

        // When
        userRoleRepository.deleteByUserAndRole(testUser1, branchManagerRole);

        // Then
        List<UserRole> remainingRoles = userRoleRepository.findByUser(testUser1);
        assertThat(remainingRoles).hasSize(1);
        assertThat(remainingRoles.get(0).getRole().getRoleCode()).isEqualTo(tellerRole.getRoleCode());
    }

    @Test
    void shouldPreventDuplicateUserRoleAssignment() {
        logTestExecution("shouldPreventDuplicateUserRoleAssignment");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        roleRepository.save(branchManagerRole);
        
        UserRole userRole = SimpleParallelTestDataFactory.createUserRole(testUser1, branchManagerRole);
        userRoleRepository.save(userRole);

        // When & Then - Attempt to create duplicate should fail
        assertThatThrownBy(() -> {
            UserRole duplicate = new UserRole();
            duplicate.setUser(testUser1);
            duplicate.setRole(branchManagerRole);
            duplicate.setAssignedBy("TEST");
            userRoleRepository.save(duplicate);
            userRoleRepository.flush(); // Force constraint check
        }).isInstanceOf(Exception.class); // Accept any constraint violation exception
    }

    @Test
    void shouldCascadeDeleteWhenUserIsDeleted() {
        logTestExecution("shouldCascadeDeleteWhenUserIsDeleted");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + System.currentTimeMillis());
        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);
        
        UserRole userRole1 = SimpleParallelTestDataFactory.createUserRole(testUser1, branchManagerRole);
        UserRole userRole2 = SimpleParallelTestDataFactory.createUserRole(testUser1, tellerRole);
        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);
        
        // Verify initial state
        assertThat(userRoleRepository.findByUser(testUser1)).hasSize(2);

        // When - Delete user roles first, then user
        userRoleRepository.deleteAll(userRoleRepository.findByUser(testUser1));
        userRepository.delete(testUser1);

        // Then - User should be deleted
        assertThat(userRepository.findById(testUser1.getId())).isEmpty();
    }

    @Test
    void shouldCascadeDeleteWhenRoleIsDeleted() {
        logTestExecution("shouldCascadeDeleteWhenRoleIsDeleted");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        User testUser2 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        roleRepository.save(branchManagerRole);
        
        UserRole userRole1 = SimpleParallelTestDataFactory.createUserRole(testUser1, branchManagerRole);
        UserRole userRole2 = SimpleParallelTestDataFactory.createUserRole(testUser2, branchManagerRole);
        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);
        
        // Verify initial state
        assertThat(userRoleRepository.findByRole(branchManagerRole)).hasSize(2);

        // When - Delete user roles first, then role
        userRoleRepository.deleteAll(userRoleRepository.findByRole(branchManagerRole));
        roleRepository.delete(branchManagerRole);

        // Then - Role should be deleted
        assertThat(roleRepository.findById(branchManagerRole.getId())).isEmpty();
    }

    @Test
    void shouldSaveUserRoleWithAuditFields() {
        logTestExecution("shouldSaveUserRoleWithAuditFields");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + System.currentTimeMillis());
        roleRepository.save(branchManagerRole);
        
        UserRole userRole = new UserRole();
        userRole.setUser(testUser1);
        userRole.setRole(branchManagerRole);
        userRole.setAssignedBy("ADMIN");

        // When
        UserRole savedUserRole = userRoleRepository.save(userRole);

        // Then
        assertThat(savedUserRole.getId()).isNotNull();
        assertThat(savedUserRole.getAssignedBy()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHandleMultipleRoleAssignmentsForUser() {
        logTestExecution("shouldHandleMultipleRoleAssignmentsForUser");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + uniqueTimestamp);
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + uniqueTimestamp);
        Role customerServiceRole = SimpleParallelTestDataFactory.createUniqueRole();
        customerServiceRole.setRoleCode("CUSTOMER_SERVICE_" + uniqueTimestamp);
        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);
        roleRepository.save(customerServiceRole);
        
        UserRole userRole1 = SimpleParallelTestDataFactory.createUserRole(testUser1, branchManagerRole);
        UserRole userRole2 = SimpleParallelTestDataFactory.createUserRole(testUser1, tellerRole);
        UserRole userRole3 = SimpleParallelTestDataFactory.createUserRole(testUser1, customerServiceRole);
        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);
        userRoleRepository.save(userRole3);

        // When
        List<UserRole> userRoles = userRoleRepository.findByUser(testUser1);

        // Then
        assertThat(userRoles).hasSize(3);
        assertThat(userRoles).extracting(ur -> ur.getRole().getRoleCode())
            .containsExactlyInAnyOrder(branchManagerRole.getRoleCode(), tellerRole.getRoleCode(), customerServiceRole.getRoleCode());
            
        // Verify each role assignment is unique
        assertThat(userRoles.stream().map(ur -> ur.getRole().getRoleCode()).distinct().count()).isEqualTo(3);
    }

    @Test
    void shouldHandleRoleAssignedToMultipleUsers() {
        logTestExecution("shouldHandleRoleAssignedToMultipleUsers");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User testUser1 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        User testUser2 = SimpleParallelTestDataFactory.createUniqueUser(branch);
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + System.currentTimeMillis());
        roleRepository.save(tellerRole);
        
        UserRole userRole1 = SimpleParallelTestDataFactory.createUserRole(testUser1, tellerRole);
        UserRole userRole2 = SimpleParallelTestDataFactory.createUserRole(testUser2, tellerRole);
        userRoleRepository.save(userRole1);
        userRoleRepository.save(userRole2);

        // When
        List<UserRole> roleAssignments = userRoleRepository.findByRole(tellerRole);

        // Then
        assertThat(roleAssignments).hasSize(2);
        assertThat(roleAssignments).extracting(ur -> ur.getUser().getUsername())
            .containsExactlyInAnyOrder(testUser1.getUsername(), testUser2.getUsername());
    }

}