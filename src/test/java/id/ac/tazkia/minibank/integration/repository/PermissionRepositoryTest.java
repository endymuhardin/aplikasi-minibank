package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        permissionRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindPermissionByPermissionCode() {
        // Given
        saveTestPermissions();

        // When
        Optional<Permission> customerView = permissionRepository.findByPermissionCode("CUSTOMER_VIEW");
        Optional<Permission> transactionDeposit = permissionRepository.findByPermissionCode("TRANSACTION_DEPOSIT");
        Optional<Permission> nonExistent = permissionRepository.findByPermissionCode("NON_EXISTENT");

        // Then
        assertThat(customerView).isPresent();
        assertThat(customerView.get().getPermissionName()).isEqualTo("View Customer");
        assertThat(customerView.get().getPermissionCategory()).isEqualTo("CUSTOMER");
        
        assertThat(transactionDeposit).isPresent();
        assertThat(transactionDeposit.get().getPermissionCategory()).isEqualTo("TRANSACTION");
        
        assertThat(nonExistent).isEmpty();
    }

    @Test
    void shouldFindPermissionsByCategory() {
        // Given
        saveTestPermissions();

        // When
        List<Permission> customerPermissions = permissionRepository.findByCategory("CUSTOMER");
        List<Permission> transactionPermissions = permissionRepository.findByCategory("TRANSACTION");
        List<Permission> accountPermissions = permissionRepository.findByCategory("ACCOUNT");
        List<Permission> nonExistentCategory = permissionRepository.findByCategory("NON_EXISTENT");

        // Then
        assertThat(customerPermissions).hasSize(3); // VIEW, CREATE, UPDATE
        assertThat(customerPermissions).allMatch(p -> p.getPermissionCategory().equals("CUSTOMER"));
        assertThat(customerPermissions).extracting(Permission::getPermissionCode)
            .containsExactlyInAnyOrder("CUSTOMER_VIEW", "CUSTOMER_CREATE", "CUSTOMER_UPDATE");
            
        assertThat(transactionPermissions).hasSize(4); // VIEW, DEPOSIT, WITHDRAWAL, TRANSFER
        assertThat(transactionPermissions).allMatch(p -> p.getPermissionCategory().equals("TRANSACTION"));
        
        assertThat(accountPermissions).hasSize(4); // VIEW, CREATE, UPDATE, BALANCE_VIEW
        assertThat(accountPermissions).allMatch(p -> p.getPermissionCategory().equals("ACCOUNT"));
        
        assertThat(nonExistentCategory).isEmpty();
    }


    @Test
    void shouldCheckExistenceByPermissionCode() {
        // Given
        saveTestPermissions();

        // When & Then
        assertThat(permissionRepository.existsByPermissionCode("CUSTOMER_VIEW")).isTrue();
        assertThat(permissionRepository.existsByPermissionCode("TRANSACTION_DEPOSIT")).isTrue();
        assertThat(permissionRepository.existsByPermissionCode("ACCOUNT_CREATE")).isTrue();
        assertThat(permissionRepository.existsByPermissionCode("NON_EXISTENT")).isFalse();
    }

    @Test
    void shouldSaveAndRetrievePermissionWithAuditFields() {
        // Given
        Permission permission = createPermission(
            "TEST_PERMISSION", "Test Permission", "TEST", 
            "Test permission description");
        permission.setCreatedBy("ADMIN");

        // When
        Permission savedPermission = permissionRepository.save(permission);
        entityManager.flush();

        // Then
        assertThat(savedPermission.getId()).isNotNull();
        assertThat(savedPermission.getCreatedDate()).isNotNull();
        assertThat(savedPermission.getCreatedBy()).isEqualTo("ADMIN");
    }

    @Test
    void shouldFindAllPermissions() {
        // Given
        saveTestPermissions();

        // When
        List<Permission> allPermissions = permissionRepository.findAll();

        // Then
        assertThat(allPermissions).hasSize(11); // Total permissions from saveTestPermissions()
    }

    @Test
    void shouldFindPermissionsByMultipleCategories() {
        // Given
        saveTestPermissions();

        // When
        List<Permission> customerPermissions = permissionRepository.findByCategory("CUSTOMER");
        List<Permission> transactionPermissions = permissionRepository.findByCategory("TRANSACTION");

        // Then - Verify no overlap between categories
        assertThat(customerPermissions).extracting(Permission::getPermissionCategory)
            .allMatch(category -> category.equals("CUSTOMER"));
        assertThat(transactionPermissions).extracting(Permission::getPermissionCategory)
            .allMatch(category -> category.equals("TRANSACTION"));
            
        // Verify different categories have different permissions
        assertThat(customerPermissions).extracting(Permission::getPermissionCode)
            .doesNotContainAnyElementsOf(
                transactionPermissions.stream()
                    .map(Permission::getPermissionCode)
                    .toList()
            );
    }

    @Test
    void shouldHandlePermissionsWithMinimalFields() {
        // Given
        Permission minimalPermission = new Permission();
        minimalPermission.setPermissionCode("MINIMAL_PERMISSION");
        minimalPermission.setPermissionName("Minimal Permission");
        minimalPermission.setPermissionCategory("GENERAL");
        minimalPermission.setDescription("Test permission with minimal fields");
        minimalPermission.setCreatedBy("TEST");

        // When
        permissionRepository.save(minimalPermission);
        entityManager.flush();

        // Then
        Optional<Permission> saved = permissionRepository.findByPermissionCode("MINIMAL_PERMISSION");
        
        assertThat(saved).isPresent();
        assertThat(saved.get().getPermissionName()).isEqualTo("Minimal Permission");
        assertThat(saved.get().getPermissionCategory()).isEqualTo("GENERAL");
        assertThat(saved.get().getDescription()).isEqualTo("Test permission with minimal fields");
    }

    private void saveTestPermissions() {
        // Customer permissions
        permissionRepository.save(createPermission("CUSTOMER_VIEW", "View Customer", "CUSTOMER", 
            "View customer information"));
        permissionRepository.save(createPermission("CUSTOMER_CREATE", "Create Customer", "CUSTOMER", 
            "Register new customers"));
        permissionRepository.save(createPermission("CUSTOMER_UPDATE", "Update Customer", "CUSTOMER", 
            "Update customer information"));

        // Account permissions
        permissionRepository.save(createPermission("ACCOUNT_VIEW", "View Account", "ACCOUNT", 
            "View account information"));
        permissionRepository.save(createPermission("ACCOUNT_CREATE", "Create Account", "ACCOUNT", 
            "Open new accounts for customers"));
        permissionRepository.save(createPermission("ACCOUNT_UPDATE", "Update Account", "ACCOUNT", 
            "Update account information"));
        permissionRepository.save(createPermission("BALANCE_VIEW", "View Balance", "ACCOUNT", 
            "View account balance"));

        // Transaction permissions
        permissionRepository.save(createPermission("TRANSACTION_VIEW", "View Transaction", "TRANSACTION", 
            "View transaction history"));
        permissionRepository.save(createPermission("TRANSACTION_DEPOSIT", "Process Deposit", "TRANSACTION", 
            "Process deposit transactions"));
        permissionRepository.save(createPermission("TRANSACTION_WITHDRAWAL", "Process Withdrawal", "TRANSACTION", 
            "Process withdrawal transactions"));
        permissionRepository.save(createPermission("TRANSACTION_TRANSFER", "Process Transfer", "TRANSACTION", 
            "Process transfer transactions"));

        entityManager.flush();
    }

    private Permission createPermission(String permissionCode, String permissionName, String category,
                                       String description) {
        Permission permission = new Permission();
        permission.setPermissionCode(permissionCode);
        permission.setPermissionName(permissionName);
        permission.setPermissionCategory(category);
        permission.setDescription(description);
        permission.setCreatedBy("TEST");
        return permission;
    }
}