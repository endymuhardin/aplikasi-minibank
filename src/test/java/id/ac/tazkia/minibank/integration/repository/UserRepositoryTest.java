package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserRole;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.repository.UserRepository;
import id.ac.tazkia.minibank.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;

    @BeforeEach
    void setUp() {
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFindUserByUsername() {
        // Given
        saveTestUsers();

        // When
        Optional<User> adminUser = userRepository.findByUsername("admin");
        Optional<User> tellerUser = userRepository.findByUsername("teller01");
        Optional<User> nonExistentUser = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(adminUser).isPresent();
        assertThat(adminUser.get().getFullName()).isEqualTo("System Administrator");
        assertThat(adminUser.get().getEmail()).isEqualTo("admin@yopmail.com");
        
        assertThat(tellerUser).isPresent();
        assertThat(tellerUser.get().getFullName()).isEqualTo("John Teller");
        
        assertThat(nonExistentUser).isEmpty();
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        saveTestUsers();

        // When
        Optional<User> adminUser = userRepository.findByEmail("admin@yopmail.com");
        Optional<User> tellerUser = userRepository.findByEmail("john.teller@yopmail.com");
        Optional<User> nonExistentUser = userRepository.findByEmail("nonexistent@yopmail.com");

        // Then
        assertThat(adminUser).isPresent();
        assertThat(adminUser.get().getUsername()).isEqualTo("admin");
        
        assertThat(tellerUser).isPresent();
        assertThat(tellerUser.get().getUsername()).isEqualTo("teller01");
        
        assertThat(nonExistentUser).isEmpty();
    }

    @Test
    void shouldFindActiveUsers() {
        // Given
        saveTestUsers();
        
        // Create an inactive user
        User inactiveUser = createUser("inactive01", "inactive@yopmail.com", "Inactive User");
        inactiveUser.setIsActive(false);
        userRepository.save(inactiveUser);
        entityManager.flush();

        // When
        List<User> activeUsers = userRepository.findActiveUsers();

        // Then
        assertThat(activeUsers).hasSize(3); // admin, teller01, cs01 (inactive01 should be excluded)
        assertThat(activeUsers).allMatch(User::getIsActive);
        assertThat(activeUsers).extracting(User::getUsername)
            .containsExactlyInAnyOrder("admin", "teller01", "cs01");
    }

    @Test
    void shouldFindUsersWithSearchTerm() {
        // Given
        saveTestUsers();

        // When - Search by username
        List<User> usernameResults = userRepository.findUsersWithSearchTerm("admin");
        List<User> fullNameResults = userRepository.findUsersWithSearchTerm("John");
        List<User> emailResults = userRepository.findUsersWithSearchTerm("teller");
        List<User> emptyResults = userRepository.findUsersWithSearchTerm("nonexistent");

        // Then
        assertThat(usernameResults).hasSize(1);
        assertThat(usernameResults.get(0).getUsername()).isEqualTo("admin");
        
        assertThat(fullNameResults).hasSize(1);
        assertThat(fullNameResults.get(0).getFullName()).contains("John");
        
        assertThat(emailResults).hasSize(1);
        assertThat(emailResults.get(0).getEmail()).contains("teller");
        
        assertThat(emptyResults).isEmpty();
    }

    @Test
    void shouldCheckExistenceByUniqueFields() {
        // Given
        saveTestUsers();

        // When & Then
        assertThat(userRepository.existsByUsername("admin")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        
        assertThat(userRepository.existsByEmail("admin@yopmail.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@yopmail.com")).isFalse();
    }

    @Test
    void shouldCountActiveUsers() {
        // Given
        saveTestUsers();
        
        // Add an inactive user
        User inactiveUser = createUser("inactive01", "inactive@yopmail.com", "Inactive User");
        inactiveUser.setIsActive(false);
        userRepository.save(inactiveUser);
        entityManager.flush();

        // When
        Long activeCount = userRepository.countActiveUsers();

        // Then
        assertThat(activeCount).isEqualTo(3L); // admin, teller01, cs01
    }

    @Test
    void shouldFindUsersByRoleCode() {
        // Given
        saveTestUsersWithRoles();

        // When
        List<User> branchManagers = userRepository.findByRoleCode("BRANCH_MANAGER");
        List<User> tellers = userRepository.findByRoleCode("TELLER");
        List<User> customerService = userRepository.findByRoleCode("CUSTOMER_SERVICE");
        List<User> nonExistentRole = userRepository.findByRoleCode("NON_EXISTENT");

        // Then
        assertThat(branchManagers).hasSize(1);
        assertThat(branchManagers.get(0).getUsername()).isEqualTo("admin");
        
        assertThat(tellers).hasSize(1);
        assertThat(tellers.get(0).getUsername()).isEqualTo("teller01");
        
        assertThat(customerService).hasSize(1);
        assertThat(customerService.get(0).getUsername()).isEqualTo("cs01");
        
        assertThat(nonExistentRole).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveUserWithAuditFields() {
        // Given
        User user = createUser("testuser", "testuser@yopmail.com", "Test User");
        user.setCreatedBy("ADMIN");
        user.setUpdatedBy("ADMIN");

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedDate()).isNotNull();
        assertThat(savedUser.getUpdatedDate()).isNotNull();
        assertThat(savedUser.getCreatedBy()).isEqualTo("ADMIN");
        assertThat(savedUser.getUpdatedBy()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHandleUserAccountLocking() {
        // Given
        User user = createUser("locktest", "locktest@yopmail.com", "Lock Test User");
        user.setFailedLoginAttempts(3);
        user.setIsLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        entityManager.flush();

        // When
        Optional<User> retrievedUser = userRepository.findByUsername("locktest");

        // Then
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getFailedLoginAttempts()).isEqualTo(3);
        assertThat(retrievedUser.get().getIsLocked()).isTrue();
        assertThat(retrievedUser.get().getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void shouldHandleLastLoginTracking() {
        // Given
        User user = createUser("logintest", "logintest@yopmail.com", "Login Test User");
        LocalDateTime loginTime = LocalDateTime.now().minusHours(1);
        user.setLastLogin(loginTime);
        userRepository.save(user);
        entityManager.flush();

        // When
        Optional<User> retrievedUser = userRepository.findByUsername("logintest");

        // Then
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getLastLogin()).isNotNull();
        assertThat(retrievedUser.get().getLastLogin()).isEqualTo(loginTime);
    }

    private void saveTestUsers() {
        User admin = createUser("admin", "admin@yopmail.com", "System Administrator");
        User teller = createUser("teller01", "john.teller@yopmail.com", "John Teller");
        User customerService = createUser("cs01", "jane.cs@yopmail.com", "Jane Customer Service");

        userRepository.save(admin);
        userRepository.save(teller);
        userRepository.save(customerService);
        entityManager.flush();
    }

    private void saveTestUsersWithRoles() {
        // Create roles first
        Role branchManagerRole = createRole("BRANCH_MANAGER", "Branch Manager", "Full access with monitoring");
        Role tellerRole = createRole("TELLER", "Teller", "Transaction processing");
        Role csRole = createRole("CUSTOMER_SERVICE", "Customer Service", "Customer management");

        roleRepository.save(branchManagerRole);
        roleRepository.save(tellerRole);
        roleRepository.save(csRole);
        
        // Create users
        User admin = createUser("admin", "admin@yopmail.com", "System Administrator");
        User teller = createUser("teller01", "john.teller@yopmail.com", "John Teller");
        User customerService = createUser("cs01", "jane.cs@yopmail.com", "Jane Customer Service");

        userRepository.save(admin);
        userRepository.save(teller);
        userRepository.save(customerService);
        
        // Assign roles
        createUserRole(admin, branchManagerRole);
        createUserRole(teller, tellerRole);
        createUserRole(customerService, csRole);
        
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

    private void createUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy("TEST");
        userRoleRepository.save(userRole);
    }
}