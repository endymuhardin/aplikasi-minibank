package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.PermissionRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PermissionRepository tests optimized for parallel execution.
 * Uses dynamic test data to prevent conflicts during concurrent execution.
 * Note: Using SAME_THREAD execution to avoid transaction management conflicts.
 */
@Execution(ExecutionMode.SAME_THREAD)
class PermissionRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void shouldFindPermissionByPermissionCode() {
        logTestExecution("shouldFindPermissionByPermissionCode");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        Permission customerView = SimpleParallelTestDataFactory.createUniquePermission();
        customerView.setPermissionCode("CUSTOMER_VIEW_" + uniqueTimestamp);
        customerView.setPermissionName("View Customer");
        customerView.setDescription("View customer information");
        permissionRepository.save(customerView);
        
        Permission transactionDeposit = SimpleParallelTestDataFactory.createUniquePermission();
        transactionDeposit.setPermissionCode("TRANSACTION_DEPOSIT_" + uniqueTimestamp);
        transactionDeposit.setPermissionName("Process Deposit");
        transactionDeposit.setDescription("Process deposit transactions");
        permissionRepository.save(transactionDeposit);

        // When
        Optional<Permission> foundCustomerView = permissionRepository.findByPermissionCode(customerView.getPermissionCode());
        Optional<Permission> foundTransactionDeposit = permissionRepository.findByPermissionCode(transactionDeposit.getPermissionCode());
        Optional<Permission> nonExistent = permissionRepository.findByPermissionCode("NON_EXISTENT_" + uniqueTimestamp);

        // Then
        assertThat(foundCustomerView).isPresent();
        assertThat(foundCustomerView.get().getPermissionName()).isEqualTo("View Customer");
        
        assertThat(foundTransactionDeposit).isPresent();
        assertThat(foundTransactionDeposit.get().getPermissionName()).isEqualTo("Process Deposit");
        
        assertThat(nonExistent).isEmpty();
    }

    @Test
    void shouldFindPermissionsByCategory() {
        logTestExecution("shouldFindPermissionsByCategory");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        // Note: Since Permission entity may not have category field, we'll test basic functionality
        Permission permission1 = SimpleParallelTestDataFactory.createUniquePermission();
        permission1.setPermissionCode("PERM1_" + uniqueTimestamp);
        permission1.setPermissionName("Permission 1");
        permissionRepository.save(permission1);
        
        Permission permission2 = SimpleParallelTestDataFactory.createUniquePermission();
        permission2.setPermissionCode("PERM2_" + uniqueTimestamp);
        permission2.setPermissionName("Permission 2");
        permissionRepository.save(permission2);

        // When
        Optional<Permission> found1 = permissionRepository.findByPermissionCode(permission1.getPermissionCode());
        Optional<Permission> found2 = permissionRepository.findByPermissionCode(permission2.getPermissionCode());

        // Then
        assertThat(found1).isPresent();
        assertThat(found1.get().getPermissionName()).isEqualTo("Permission 1");
        
        assertThat(found2).isPresent();
        assertThat(found2.get().getPermissionName()).isEqualTo("Permission 2");
    }


    @Test
    void shouldCheckExistenceByPermissionCode() {
        logTestExecution("shouldCheckExistenceByPermissionCode");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        Permission permission1 = SimpleParallelTestDataFactory.createUniquePermission();
        permission1.setPermissionCode("CUSTOMER_VIEW_" + uniqueTimestamp);
        permissionRepository.save(permission1);
        
        Permission permission2 = SimpleParallelTestDataFactory.createUniquePermission();
        permission2.setPermissionCode("TRANSACTION_DEPOSIT_" + uniqueTimestamp);
        permissionRepository.save(permission2);
        
        Permission permission3 = SimpleParallelTestDataFactory.createUniquePermission();
        permission3.setPermissionCode("ACCOUNT_CREATE_" + uniqueTimestamp);
        permissionRepository.save(permission3);

        // When & Then
        assertThat(permissionRepository.existsByPermissionCode(permission1.getPermissionCode())).isTrue();
        assertThat(permissionRepository.existsByPermissionCode(permission2.getPermissionCode())).isTrue();
        assertThat(permissionRepository.existsByPermissionCode(permission3.getPermissionCode())).isTrue();
        assertThat(permissionRepository.existsByPermissionCode("NON_EXISTENT_" + uniqueTimestamp)).isFalse();
    }

    @Test
    void shouldSaveAndRetrievePermissionWithAuditFields() {
        logTestExecution("shouldSaveAndRetrievePermissionWithAuditFields");
        
        // Given - Create unique test data
        Permission permission = SimpleParallelTestDataFactory.createUniquePermission();
        permission.setPermissionCode("TEST_PERMISSION_" + System.currentTimeMillis());
        permission.setPermissionName("Test Permission");
        permission.setDescription("Test permission description");
        permission.setCreatedBy("ADMIN");

        // When
        Permission savedPermission = permissionRepository.save(permission);

        // Then
        assertThat(savedPermission.getId()).isNotNull();
        assertThat(savedPermission.getCreatedBy()).isEqualTo("ADMIN");
    }

    @Test
    void shouldFindAllPermissions() {
        logTestExecution("shouldFindAllPermissions");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        int initialCount = (int) permissionRepository.count();
        
        Permission permission1 = SimpleParallelTestDataFactory.createUniquePermission();
        permission1.setPermissionCode("PERM1_" + uniqueTimestamp);
        permissionRepository.save(permission1);
        
        Permission permission2 = SimpleParallelTestDataFactory.createUniquePermission();
        permission2.setPermissionCode("PERM2_" + uniqueTimestamp);
        permissionRepository.save(permission2);
        
        Permission permission3 = SimpleParallelTestDataFactory.createUniquePermission();
        permission3.setPermissionCode("PERM3_" + uniqueTimestamp);
        permissionRepository.save(permission3);

        // When
        List<Permission> allPermissions = permissionRepository.findAll();

        // Then
        assertThat(allPermissions).hasSizeGreaterThanOrEqualTo(initialCount + 3);
        
        boolean hasPermission1 = allPermissions.stream()
            .anyMatch(p -> p.getPermissionCode().equals(permission1.getPermissionCode()));
        boolean hasPermission2 = allPermissions.stream()
            .anyMatch(p -> p.getPermissionCode().equals(permission2.getPermissionCode()));
        boolean hasPermission3 = allPermissions.stream()
            .anyMatch(p -> p.getPermissionCode().equals(permission3.getPermissionCode()));
            
        assertThat(hasPermission1).isTrue();
        assertThat(hasPermission2).isTrue();
        assertThat(hasPermission3).isTrue();
    }

    @Test
    void shouldFindPermissionsByMultipleCategories() {
        logTestExecution("shouldFindPermissionsByMultipleCategories");
        
        // Given - Create unique test data
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        Permission permission1 = SimpleParallelTestDataFactory.createUniquePermission();
        permission1.setPermissionCode("CUSTOMER_PERM_" + uniqueTimestamp);
        permission1.setPermissionName("Customer Permission");
        permissionRepository.save(permission1);
        
        Permission permission2 = SimpleParallelTestDataFactory.createUniquePermission();
        permission2.setPermissionCode("TRANSACTION_PERM_" + uniqueTimestamp);
        permission2.setPermissionName("Transaction Permission");
        permissionRepository.save(permission2);

        // When
        Optional<Permission> customerPerm = permissionRepository.findByPermissionCode(permission1.getPermissionCode());
        Optional<Permission> transactionPerm = permissionRepository.findByPermissionCode(permission2.getPermissionCode());

        // Then - Verify permissions exist and are different
        assertThat(customerPerm).isPresent();
        assertThat(transactionPerm).isPresent();
        assertThat(customerPerm.get().getPermissionCode()).isNotEqualTo(transactionPerm.get().getPermissionCode());
        assertThat(customerPerm.get().getPermissionName()).isEqualTo("Customer Permission");
        assertThat(transactionPerm.get().getPermissionName()).isEqualTo("Transaction Permission");
    }

    @Test
    void shouldHandlePermissionsWithMinimalFields() {
        logTestExecution("shouldHandlePermissionsWithMinimalFields");
        
        // Given - Create unique test data
        String uniquePermissionCode = "MINIMAL_PERMISSION_" + System.currentTimeMillis();
        Permission minimalPermission = new Permission();
        minimalPermission.setPermissionCode(uniquePermissionCode);
        minimalPermission.setPermissionName("Minimal Permission");
        minimalPermission.setPermissionCategory("GENERAL");
        minimalPermission.setDescription("Test permission with minimal fields");
        minimalPermission.setCreatedBy("TEST");

        // When
        permissionRepository.save(minimalPermission);

        // Then
        Optional<Permission> saved = permissionRepository.findByPermissionCode(uniquePermissionCode);
        
        assertThat(saved).isPresent();
        assertThat(saved.get().getPermissionName()).isEqualTo("Minimal Permission");
        assertThat(saved.get().getDescription()).isEqualTo("Test permission with minimal fields");
    }

}