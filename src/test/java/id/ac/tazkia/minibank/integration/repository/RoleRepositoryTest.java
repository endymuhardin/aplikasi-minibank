package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RoleRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindRoleByRoleCode() {
        // Given
        saveTestRoles();

        // When
        Optional<Role> branchManagerRole = roleRepository.findByRoleCode("BRANCH_MANAGER");
        Optional<Role> tellerRole = roleRepository.findByRoleCode("TELLER");
        Optional<Role> nonExistentRole = roleRepository.findByRoleCode("NON_EXISTENT");

        // Then
        assertThat(branchManagerRole).isPresent();
        assertThat(branchManagerRole.get().getRoleName()).isEqualTo("Branch Manager");
        assertThat(branchManagerRole.get().getDescription()).contains("monitoring and approval");
        
        assertThat(tellerRole).isPresent();
        assertThat(tellerRole.get().getRoleName()).isEqualTo("Teller");
        
        assertThat(nonExistentRole).isEmpty();
    }

    @Test
    void shouldFindActiveRoles() {
        // Given
        saveTestRoles();
        
        // Create an inactive role
        Role inactiveRole = createRole("INACTIVE_ROLE", "Inactive Role", "Test inactive role");
        inactiveRole.setIsActive(false);
        roleRepository.save(inactiveRole);
        entityManager.flush();

        // When
        List<Role> activeRoles = roleRepository.findActiveRoles();

        // Then
        assertThat(activeRoles).hasSize(3); // BRANCH_MANAGER, TELLER, CUSTOMER_SERVICE
        assertThat(activeRoles).allMatch(Role::getIsActive);
        assertThat(activeRoles).extracting(Role::getRoleCode)
            .containsExactlyInAnyOrder("BRANCH_MANAGER", "TELLER", "CUSTOMER_SERVICE");
    }

    @Test
    void shouldCheckExistenceByRoleCode() {
        // Given
        saveTestRoles();

        // When & Then
        assertThat(roleRepository.existsByRoleCode("BRANCH_MANAGER")).isTrue();
        assertThat(roleRepository.existsByRoleCode("TELLER")).isTrue();
        assertThat(roleRepository.existsByRoleCode("CUSTOMER_SERVICE")).isTrue();
        assertThat(roleRepository.existsByRoleCode("NON_EXISTENT")).isFalse();
    }

    @Test
    void shouldSaveAndRetrieveRoleWithAuditFields() {
        // Given
        Role role = createRole("TEST_ROLE", "Test Role", "Test role description");
        role.setCreatedBy("ADMIN");
        role.setUpdatedBy("ADMIN");

        // When
        Role savedRole = roleRepository.save(role);
        entityManager.flush();

        // Then
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getCreatedDate()).isNotNull();
        assertThat(savedRole.getUpdatedDate()).isNotNull();
        assertThat(savedRole.getCreatedBy()).isEqualTo("ADMIN");
        assertThat(savedRole.getUpdatedBy()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHandleRoleActivationDeactivation() {
        // Given
        Role role = createRole("TOGGLE_ROLE", "Toggle Role", "Role for activation test");
        role.setIsActive(true);
        roleRepository.save(role);
        entityManager.flush();

        // When - Deactivate role
        role.setIsActive(false);
        roleRepository.save(role);
        entityManager.flush();

        // Then
        List<Role> activeRoles = roleRepository.findActiveRoles();
        Optional<Role> toggledRole = roleRepository.findByRoleCode("TOGGLE_ROLE");
        
        assertThat(activeRoles).doesNotContain(role);
        assertThat(toggledRole).isPresent();
        assertThat(toggledRole.get().getIsActive()).isFalse();
    }

    @Test
    void shouldFindAllRoles() {
        // Given
        saveTestRoles();

        // When
        List<Role> allRoles = roleRepository.findAll();

        // Then
        assertThat(allRoles).hasSize(3);
        assertThat(allRoles).extracting(Role::getRoleCode)
            .containsExactlyInAnyOrder("BRANCH_MANAGER", "TELLER", "CUSTOMER_SERVICE");
    }

    @Test
    void shouldUpdateRoleInformation() {
        // Given
        Role role = createRole("UPDATE_TEST", "Original Name", "Original description");
        Role savedRole = roleRepository.save(role);
        entityManager.flush();

        // When - Update role information
        savedRole.setRoleName("Updated Name");
        savedRole.setDescription("Updated description");
        savedRole.setUpdatedBy("UPDATER");
        roleRepository.save(savedRole);
        entityManager.flush();

        // Then
        Optional<Role> retrievedRole = roleRepository.findByRoleCode("UPDATE_TEST");
        assertThat(retrievedRole).isPresent();
        assertThat(retrievedRole.get().getRoleName()).isEqualTo("Updated Name");
        assertThat(retrievedRole.get().getDescription()).isEqualTo("Updated description");
        assertThat(retrievedRole.get().getUpdatedBy()).isEqualTo("UPDATER");
        assertThat(retrievedRole.get().getUpdatedDate()).isAfter(retrievedRole.get().getCreatedDate());
    }

    private void saveTestRoles() {
        Role branchManager = createRole("BRANCH_MANAGER", "Branch Manager", 
            "Full access with monitoring and approval capabilities");
        Role teller = createRole("TELLER", "Teller", 
            "Handle financial transactions");
        Role customerService = createRole("CUSTOMER_SERVICE", "Customer Service", 
            "Handle customer registration and account opening");

        roleRepository.save(branchManager);
        roleRepository.save(teller);
        roleRepository.save(customerService);
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
}