package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RoleRepository tests optimized for parallel execution.
 * Uses dynamic test data to prevent conflicts during concurrent execution.
 * Note: Using SAME_THREAD execution to avoid transaction management conflicts.
 */
@Execution(ExecutionMode.SAME_THREAD)
class RoleRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldFindRoleByRoleCode() {
        logTestExecution("shouldFindRoleByRoleCode");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + uniqueTimestamp);
        branchManagerRole.setRoleName("Branch Manager");
        branchManagerRole.setDescription("Full access with monitoring and approval capabilities");
        roleRepository.save(branchManagerRole);
        
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + uniqueTimestamp);
        tellerRole.setRoleName("Teller");
        tellerRole.setDescription("Handle financial transactions");
        roleRepository.save(tellerRole);

        // When
        Optional<Role> foundBranchManagerRole = roleRepository.findByRoleCode(branchManagerRole.getRoleCode());
        Optional<Role> foundTellerRole = roleRepository.findByRoleCode(tellerRole.getRoleCode());
        Optional<Role> nonExistentRole = roleRepository.findByRoleCode("NON_EXISTENT_" + uniqueTimestamp);

        // Then
        assertThat(foundBranchManagerRole).isPresent();
        assertThat(foundBranchManagerRole.get().getRoleName()).isEqualTo("Branch Manager");
        assertThat(foundBranchManagerRole.get().getDescription()).contains("monitoring and approval");
        
        assertThat(foundTellerRole).isPresent();
        assertThat(foundTellerRole.get().getRoleName()).isEqualTo("Teller");
        
        assertThat(nonExistentRole).isEmpty();
    }

    @Test
    void shouldFindActiveRoles() {
        logTestExecution("shouldFindActiveRoles");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        int initialActiveCount = roleRepository.findActiveRoles().size();
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + uniqueTimestamp);
        branchManagerRole.setIsActive(true);
        roleRepository.save(branchManagerRole);
        
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + uniqueTimestamp);
        tellerRole.setIsActive(true);
        roleRepository.save(tellerRole);
        
        Role customerServiceRole = SimpleParallelTestDataFactory.createUniqueRole();
        customerServiceRole.setRoleCode("CUSTOMER_SERVICE_" + uniqueTimestamp);
        customerServiceRole.setIsActive(true);
        roleRepository.save(customerServiceRole);
        
        // Create an inactive role
        Role inactiveRole = SimpleParallelTestDataFactory.createUniqueRole();
        inactiveRole.setRoleCode("INACTIVE_ROLE_" + uniqueTimestamp);
        inactiveRole.setRoleName("Inactive Role");
        inactiveRole.setDescription("Test inactive role");
        inactiveRole.setIsActive(false);
        roleRepository.save(inactiveRole);

        // When
        List<Role> activeRoles = roleRepository.findActiveRoles();

        // Then
        assertThat(activeRoles).hasSizeGreaterThanOrEqualTo(initialActiveCount + 3);
        assertThat(activeRoles).allMatch(Role::getIsActive);
        
        boolean hasBranchManager = activeRoles.stream()
            .anyMatch(r -> r.getRoleCode().equals(branchManagerRole.getRoleCode()));
        boolean hasTeller = activeRoles.stream()
            .anyMatch(r -> r.getRoleCode().equals(tellerRole.getRoleCode()));
        boolean hasCustomerService = activeRoles.stream()
            .anyMatch(r -> r.getRoleCode().equals(customerServiceRole.getRoleCode()));
        boolean hasInactive = activeRoles.stream()
            .anyMatch(r -> r.getRoleCode().equals(inactiveRole.getRoleCode()));
            
        assertThat(hasBranchManager).isTrue();
        assertThat(hasTeller).isTrue();
        assertThat(hasCustomerService).isTrue();
        assertThat(hasInactive).isFalse();
    }

    @Test
    void shouldCheckExistenceByRoleCode() {
        logTestExecution("shouldCheckExistenceByRoleCode");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + uniqueTimestamp);
        roleRepository.save(branchManagerRole);
        
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + uniqueTimestamp);
        roleRepository.save(tellerRole);
        
        Role customerServiceRole = SimpleParallelTestDataFactory.createUniqueRole();
        customerServiceRole.setRoleCode("CUSTOMER_SERVICE_" + uniqueTimestamp);
        roleRepository.save(customerServiceRole);

        // When & Then
        assertThat(roleRepository.existsByRoleCode(branchManagerRole.getRoleCode())).isTrue();
        assertThat(roleRepository.existsByRoleCode(tellerRole.getRoleCode())).isTrue();
        assertThat(roleRepository.existsByRoleCode(customerServiceRole.getRoleCode())).isTrue();
        assertThat(roleRepository.existsByRoleCode("NON_EXISTENT_" + uniqueTimestamp)).isFalse();
    }

    @Test
    void shouldSaveAndRetrieveRoleWithAuditFields() {
        logTestExecution("shouldSaveAndRetrieveRoleWithAuditFields");
        
        // Given - Create unique test data
        Role role = SimpleParallelTestDataFactory.createUniqueRole();
        role.setRoleCode("TEST_ROLE_" + System.currentTimeMillis());
        role.setRoleName("Test Role");
        role.setDescription("Test role description");
        role.setCreatedBy("ADMIN");
        role.setUpdatedBy("ADMIN");

        // When
        Role savedRole = roleRepository.save(role);

        // Then
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getCreatedBy()).isEqualTo("ADMIN");
        assertThat(savedRole.getUpdatedBy()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHandleRoleActivationDeactivation() {
        logTestExecution("shouldHandleRoleActivationDeactivation");
        
        // Given - Create unique test data
        String uniqueRoleCode = "TOGGLE_ROLE_" + System.currentTimeMillis();
        Role role = SimpleParallelTestDataFactory.createUniqueRole();
        role.setRoleCode(uniqueRoleCode);
        role.setRoleName("Toggle Role");
        role.setDescription("Role for activation test");
        role.setIsActive(true);
        roleRepository.save(role);

        // When - Deactivate role
        role.setIsActive(false);
        roleRepository.save(role);

        // Then
        List<Role> activeRoles = roleRepository.findActiveRoles();
        Optional<Role> toggledRole = roleRepository.findByRoleCode(uniqueRoleCode);
        
        assertThat(activeRoles).doesNotContain(role);
        assertThat(toggledRole).isPresent();
        assertThat(toggledRole.get().getIsActive()).isFalse();
    }

    @Test
    void shouldFindAllRoles() {
        logTestExecution("shouldFindAllRoles");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        int initialCount = (int) roleRepository.count();
        
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + uniqueTimestamp);
        roleRepository.save(branchManagerRole);
        
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + uniqueTimestamp);
        roleRepository.save(tellerRole);
        
        Role customerServiceRole = SimpleParallelTestDataFactory.createUniqueRole();
        customerServiceRole.setRoleCode("CUSTOMER_SERVICE_" + uniqueTimestamp);
        roleRepository.save(customerServiceRole);

        // When
        List<Role> allRoles = roleRepository.findAll();

        // Then
        assertThat(allRoles).hasSizeGreaterThanOrEqualTo(initialCount + 3);
        
        boolean hasBranchManager = allRoles.stream()
            .anyMatch(r -> r.getRoleCode().equals(branchManagerRole.getRoleCode()));
        boolean hasTeller = allRoles.stream()
            .anyMatch(r -> r.getRoleCode().equals(tellerRole.getRoleCode()));
        boolean hasCustomerService = allRoles.stream()
            .anyMatch(r -> r.getRoleCode().equals(customerServiceRole.getRoleCode()));
            
        assertThat(hasBranchManager).isTrue();
        assertThat(hasTeller).isTrue();
        assertThat(hasCustomerService).isTrue();
    }

    @Test
    void shouldUpdateRoleInformation() {
        logTestExecution("shouldUpdateRoleInformation");
        
        // Given - Create unique test data
        String uniqueRoleCode = "UPDATE_TEST_" + System.currentTimeMillis();
        Role role = SimpleParallelTestDataFactory.createUniqueRole();
        role.setRoleCode(uniqueRoleCode);
        role.setRoleName("Original Name");
        role.setDescription("Original description");
        Role savedRole = roleRepository.save(role);

        // When - Update role information
        savedRole.setRoleName("Updated Name");
        savedRole.setDescription("Updated description");
        savedRole.setUpdatedBy("UPDATER");
        roleRepository.save(savedRole);

        // Then
        Optional<Role> retrievedRole = roleRepository.findByRoleCode(uniqueRoleCode);
        assertThat(retrievedRole).isPresent();
        assertThat(retrievedRole.get().getRoleName()).isEqualTo("Updated Name");
        assertThat(retrievedRole.get().getDescription()).isEqualTo("Updated description");
        assertThat(retrievedRole.get().getUpdatedBy()).isEqualTo("UPDATER");
    }

}