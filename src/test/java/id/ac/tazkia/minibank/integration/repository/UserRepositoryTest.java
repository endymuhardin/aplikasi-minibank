package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserRole;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.repository.UserRepository;
import id.ac.tazkia.minibank.repository.UserRoleRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;

/**
 * UserRepository tests optimized for parallel execution.
 * Uses dynamic test data to prevent conflicts during concurrent execution.
 * Covers all 10 test methods from the original UserRepositoryTest.
 * Note: Using SAME_THREAD execution to avoid transaction management conflicts.
 */
@org.junit.jupiter.api.parallel.Execution(org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD)
class UserRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private BranchRepository branchRepository;

    @Test
    void shouldFindUserByUsername() {
        logTestExecution("shouldFindUserByUsername");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User adminUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        adminUser.setUsername("testadmin_" + System.currentTimeMillis());
        adminUser.setFullName("System Administrator");
        adminUser.setEmail("admin@yopmail.com_" + System.currentTimeMillis());
        userRepository.save(adminUser);
        
        User tellerUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        tellerUser.setUsername("teller01_" + System.currentTimeMillis());
        tellerUser.setFullName("John Teller");
        tellerUser.setEmail("john.teller@yopmail.com_" + System.currentTimeMillis());
        userRepository.save(tellerUser);

        // When
        Optional<User> foundAdminUser = userRepository.findByUsername(adminUser.getUsername());
        Optional<User> foundTellerUser = userRepository.findByUsername(tellerUser.getUsername());
        Optional<User> nonExistentUser = userRepository.findByUsername("nonexistent_" + System.currentTimeMillis());

        // Then
        assertThat(foundAdminUser).isPresent();
        assertThat(foundAdminUser.get().getFullName()).isEqualTo("System Administrator");
        assertThat(foundAdminUser.get().getEmail()).isEqualTo(adminUser.getEmail());
        
        assertThat(foundTellerUser).isPresent();
        assertThat(foundTellerUser.get().getFullName()).isEqualTo("John Teller");
        
        assertThat(nonExistentUser).isEmpty();
    }

    @Test
    void shouldFindUserByEmail() {
        logTestExecution("shouldFindUserByEmail");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User adminUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        adminUser.setUsername("admin_" + System.currentTimeMillis());
        adminUser.setEmail("admin@yopmail.com_" + System.currentTimeMillis());
        userRepository.save(adminUser);
        
        User tellerUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        tellerUser.setUsername("teller01_" + System.currentTimeMillis());
        tellerUser.setEmail("john.teller@yopmail.com_" + System.currentTimeMillis());
        userRepository.save(tellerUser);

        // When
        Optional<User> foundAdminUser = userRepository.findByEmail(adminUser.getEmail());
        Optional<User> foundTellerUser = userRepository.findByEmail(tellerUser.getEmail());
        Optional<User> nonExistentUser = userRepository.findByEmail("nonexistent@yopmail.com_" + System.currentTimeMillis());

        // Then
        assertThat(foundAdminUser).isPresent();
        assertThat(foundAdminUser.get().getUsername()).isEqualTo(adminUser.getUsername());
        
        assertThat(foundTellerUser).isPresent();
        assertThat(foundTellerUser.get().getUsername()).isEqualTo(tellerUser.getUsername());
        
        assertThat(nonExistentUser).isEmpty();
    }

    @Test
    void shouldFindActiveUsers() {
        logTestExecution("shouldFindActiveUsers");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        User adminUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        adminUser.setUsername("admin_" + System.currentTimeMillis());
        adminUser.setIsActive(true);
        userRepository.save(adminUser);
        
        User tellerUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        tellerUser.setUsername("teller01_" + System.currentTimeMillis());
        tellerUser.setIsActive(true);
        userRepository.save(tellerUser);
        
        User csUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        csUser.setUsername("cs01_" + System.currentTimeMillis());
        csUser.setIsActive(true);
        userRepository.save(csUser);
        
        // Create an inactive user
        User inactiveUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        inactiveUser.setUsername("inactive01_" + System.currentTimeMillis());
        inactiveUser.setIsActive(false);
        userRepository.save(inactiveUser);

        // When
        List<User> activeUsers = userRepository.findActiveUsers();

        // Then
        assertThat(activeUsers).hasSizeGreaterThanOrEqualTo(3);
        activeUsers.forEach(user -> assertThat(user.getIsActive()).isTrue());
        
        boolean hasOurAdminUser = activeUsers.stream()
            .anyMatch(u -> u.getUsername().equals(adminUser.getUsername()));
        boolean hasOurTellerUser = activeUsers.stream()
            .anyMatch(u -> u.getUsername().equals(tellerUser.getUsername()));
        boolean hasOurCsUser = activeUsers.stream()
            .anyMatch(u -> u.getUsername().equals(csUser.getUsername()));
        boolean hasInactiveUser = activeUsers.stream()
            .anyMatch(u -> u.getUsername().equals(inactiveUser.getUsername()));
            
        assertThat(hasOurAdminUser).isTrue();
        assertThat(hasOurTellerUser).isTrue();
        assertThat(hasOurCsUser).isTrue();
        assertThat(hasInactiveUser).isFalse();
    }

    @Test
    void shouldFindUsersWithSearchTerm() {
        logTestExecution("shouldFindUsersWithSearchTerm");
        
        // Given - Create unique test data with unique search terms
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        User adminUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        adminUser.setUsername("admin_" + uniqueTimestamp);
        adminUser.setFullName("Admin User " + uniqueTimestamp);
        adminUser.setEmail("admin_" + uniqueTimestamp + "@yopmail.com");
        userRepository.save(adminUser);
        
        User johnUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        johnUser.setUsername("johnuser_" + uniqueTimestamp);
        johnUser.setFullName("John " + uniqueTimestamp + " User");
        johnUser.setEmail("john_" + uniqueTimestamp + "@yopmail.com");
        userRepository.save(johnUser);
        
        User tellerUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        tellerUser.setUsername("otheruser_" + uniqueTimestamp);
        tellerUser.setFullName("Other User " + uniqueTimestamp);
        tellerUser.setEmail("teller_" + uniqueTimestamp + "@yopmail.com");
        userRepository.save(tellerUser);

        // When - Search by different terms
        List<User> usernameResults = userRepository.findUsersWithSearchTerm("admin_" + uniqueTimestamp);
        List<User> fullNameResults = userRepository.findUsersWithSearchTerm("John " + uniqueTimestamp);
        List<User> emailResults = userRepository.findUsersWithSearchTerm("teller_" + uniqueTimestamp);
        List<User> emptyResults = userRepository.findUsersWithSearchTerm("nonexistent_" + uniqueTimestamp);

        // Then
        assertThat(usernameResults).hasSizeGreaterThanOrEqualTo(1);
        boolean hasAdminUser = usernameResults.stream()
            .anyMatch(u -> u.getUsername().equals(adminUser.getUsername()));
        assertThat(hasAdminUser).isTrue();
        
        assertThat(fullNameResults).hasSizeGreaterThanOrEqualTo(1);
        boolean hasJohnUser = fullNameResults.stream()
            .anyMatch(u -> u.getFullName().contains("John " + uniqueTimestamp));
        assertThat(hasJohnUser).isTrue();
        
        assertThat(emailResults).hasSizeGreaterThanOrEqualTo(1);
        boolean hasTellerUser = emailResults.stream()
            .anyMatch(u -> u.getEmail().contains("teller_" + uniqueTimestamp));
        assertThat(hasTellerUser).isTrue();
        
        // No user should match the nonexistent search term
        boolean hasNonExistentMatch = emptyResults.stream()
            .anyMatch(u -> u.getUsername().contains("nonexistent_" + uniqueTimestamp) ||
                          u.getFullName().contains("nonexistent_" + uniqueTimestamp) ||
                          u.getEmail().contains("nonexistent_" + uniqueTimestamp));
        assertThat(hasNonExistentMatch).isFalse();
    }

    @Test
    void shouldCheckExistenceByUniqueFields() {
        logTestExecution("shouldCheckExistenceByUniqueFields");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        User adminUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        adminUser.setUsername("admin_" + uniqueTimestamp);
        adminUser.setEmail("admin_" + uniqueTimestamp + "@yopmail.com");
        userRepository.save(adminUser);

        // When & Then
        assertThat(userRepository.existsByUsername("admin_" + uniqueTimestamp)).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent_" + uniqueTimestamp)).isFalse();
        
        assertThat(userRepository.existsByEmail("admin_" + uniqueTimestamp + "@yopmail.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent_" + uniqueTimestamp + "@yopmail.com")).isFalse();
    }

    @Test
    void shouldCountActiveUsers() {
        logTestExecution("shouldCountActiveUsers");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        Long initialActiveCount = userRepository.countActiveUsers();
        
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        User adminUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        adminUser.setUsername("admin_" + uniqueTimestamp);
        adminUser.setIsActive(true);
        userRepository.save(adminUser);
        
        User tellerUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        tellerUser.setUsername("teller01_" + uniqueTimestamp);
        tellerUser.setIsActive(true);
        userRepository.save(tellerUser);
        
        User csUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        csUser.setUsername("cs01_" + uniqueTimestamp);
        csUser.setIsActive(true);
        userRepository.save(csUser);
        
        // Add an inactive user
        User inactiveUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        inactiveUser.setUsername("inactive01_" + uniqueTimestamp);
        inactiveUser.setIsActive(false);
        userRepository.save(inactiveUser);

        // When
        Long activeCount = userRepository.countActiveUsers();

        // Then
        assertThat(activeCount).isEqualTo(initialActiveCount + 3); // admin, teller01, cs01 (inactive01 excluded)
    }

    @Test
    void shouldFindUsersByRoleCode() {
        logTestExecution("shouldFindUsersByRoleCode");
        
        // Given - Create unique test data with roles
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        
        // Create roles first
        Role branchManagerRole = SimpleParallelTestDataFactory.createUniqueRole();
        branchManagerRole.setRoleCode("BRANCH_MANAGER_" + uniqueTimestamp);
        branchManagerRole.setRoleName("Branch Manager");
        branchManagerRole.setDescription("Full access with monitoring");
        roleRepository.save(branchManagerRole);
        
        Role tellerRole = SimpleParallelTestDataFactory.createUniqueRole();
        tellerRole.setRoleCode("TELLER_" + uniqueTimestamp);
        tellerRole.setRoleName("Teller");
        tellerRole.setDescription("Transaction processing");
        roleRepository.save(tellerRole);
        
        Role csRole = SimpleParallelTestDataFactory.createUniqueRole();
        csRole.setRoleCode("CUSTOMER_SERVICE_" + uniqueTimestamp);
        csRole.setRoleName("Customer Service");
        csRole.setDescription("Customer management");
        roleRepository.save(csRole);
        
        // Create users
        User adminUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        adminUser.setUsername("admin_" + uniqueTimestamp);
        adminUser.setFullName("System Administrator");
        adminUser.setEmail("admin_" + uniqueTimestamp + "@yopmail.com");
        userRepository.save(adminUser);
        
        User tellerUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        tellerUser.setUsername("teller01_" + uniqueTimestamp);
        tellerUser.setFullName("John Teller");
        tellerUser.setEmail("john.teller_" + uniqueTimestamp + "@yopmail.com");
        userRepository.save(tellerUser);
        
        User customerServiceUser = SimpleParallelTestDataFactory.createUniqueUser(branch);
        customerServiceUser.setUsername("cs01_" + uniqueTimestamp);
        customerServiceUser.setFullName("Jane Customer Service");
        customerServiceUser.setEmail("jane.cs_" + uniqueTimestamp + "@yopmail.com");
        userRepository.save(customerServiceUser);
        
        // Assign roles
        UserRole adminUserRole = SimpleParallelTestDataFactory.createUserRole(adminUser, branchManagerRole);
        userRoleRepository.save(adminUserRole);
        
        UserRole tellerUserRole = SimpleParallelTestDataFactory.createUserRole(tellerUser, tellerRole);
        userRoleRepository.save(tellerUserRole);
        
        UserRole csUserRole = SimpleParallelTestDataFactory.createUserRole(customerServiceUser, csRole);
        userRoleRepository.save(csUserRole);

        // When
        List<User> branchManagers = userRepository.findByRoleCode("BRANCH_MANAGER_" + uniqueTimestamp);
        List<User> tellers = userRepository.findByRoleCode("TELLER_" + uniqueTimestamp);
        List<User> customerService = userRepository.findByRoleCode("CUSTOMER_SERVICE_" + uniqueTimestamp);
        List<User> nonExistentRole = userRepository.findByRoleCode("NON_EXISTENT_" + uniqueTimestamp);

        // Then
        assertThat(branchManagers).hasSize(1);
        assertThat(branchManagers.get(0).getUsername()).isEqualTo("admin_" + uniqueTimestamp);
        
        assertThat(tellers).hasSize(1);
        assertThat(tellers.get(0).getUsername()).isEqualTo("teller01_" + uniqueTimestamp);
        
        assertThat(customerService).hasSize(1);
        assertThat(customerService.get(0).getUsername()).isEqualTo("cs01_" + uniqueTimestamp);
        
        assertThat(nonExistentRole).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveUserWithAuditFields() {
        logTestExecution("shouldSaveAndRetrieveUserWithAuditFields");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        User user = SimpleParallelTestDataFactory.createUniqueUser(branch);
        user.setUsername("testuser_" + uniqueTimestamp);
        user.setEmail("testuser_" + uniqueTimestamp + "@yopmail.com");
        user.setFullName("Test User");
        user.setCreatedBy("ADMIN");
        user.setUpdatedBy("ADMIN");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedBy()).isEqualTo("ADMIN");
        assertThat(savedUser.getUpdatedBy()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHandleUserAccountLocking() {
        logTestExecution("shouldHandleUserAccountLocking");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        User user = SimpleParallelTestDataFactory.createUniqueUser(branch);
        user.setUsername("locktest_" + uniqueTimestamp);
        user.setEmail("locktest_" + uniqueTimestamp + "@yopmail.com");
        user.setFullName("Lock Test User");
        user.setFailedLoginAttempts(3);
        user.setIsLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // When
        Optional<User> retrievedUser = userRepository.findByUsername("locktest_" + uniqueTimestamp);

        // Then
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getFailedLoginAttempts()).isEqualTo(3);
        assertThat(retrievedUser.get().getIsLocked()).isTrue();
        assertThat(retrievedUser.get().getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void shouldHandleLastLoginTracking() {
        logTestExecution("shouldHandleLastLoginTracking");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        String uniqueTimestamp = String.valueOf(System.currentTimeMillis());
        User user = SimpleParallelTestDataFactory.createUniqueUser(branch);
        user.setUsername("logintest_" + uniqueTimestamp);
        user.setEmail("logintest_" + uniqueTimestamp + "@yopmail.com");
        user.setFullName("Login Test User");
        LocalDateTime loginTime = LocalDateTime.now().minusHours(1);
        user.setLastLogin(loginTime);
        userRepository.save(user);

        // When
        Optional<User> retrievedUser = userRepository.findByUsername("logintest_" + uniqueTimestamp);

        // Then
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getLastLogin()).isNotNull();
        assertThat(retrievedUser.get().getLastLogin()).isEqualTo(loginTime);
    }
}