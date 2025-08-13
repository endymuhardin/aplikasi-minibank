package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.RolePermission;
import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserPassword;
import id.ac.tazkia.minibank.entity.UserRole;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.PermissionRepository;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.repository.UserRepository;

class UserAuthenticationRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @BeforeEach
    void setUp() {
        // Create test roles and permissions
        createTestRolesAndPermissions();
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/auth/user_role_test_data.csv", numLinesToSkip = 1)
    void testFindByRoleCode(String username, String fullName, String roleCode, String roleName, boolean shouldFind) {
        // Given
        User user = createTestUser(username, fullName);
        Role role = findOrCreateRole(roleCode, roleName);
        
        if (shouldFind) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRole.setAssignedDate(LocalDateTime.now());
            userRole.setAssignedBy("test");
            entityManager.persistAndFlush(userRole);
        }
        
        entityManager.flush();
        entityManager.clear();
        
        // When
        List<User> usersWithRole = userRepository.findByRoleCode(roleCode);
        
        // Then
        if (shouldFind) {
            assertThat(usersWithRole).hasSizeGreaterThanOrEqualTo(1);
            // Check if any user matches the expected pattern - must be the user we created
            assertThat(usersWithRole.stream().anyMatch(u -> u.getUsername().startsWith(username))).isTrue();
        } else if ("INVALID_ROLE".equals(roleCode)) {
            // Invalid role should return empty result
            assertThat(usersWithRole).isEmpty();
        }
        // For "noroleuser1" case, we don't assign role, so it should not be found
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/auth/user_search_test_data.csv", numLinesToSkip = 1)
    void testFindUsersWithSearchTerm(String username, String fullName, String email, String searchTerm, boolean shouldFind) {
        // Given
        User user = createTestUser(username, fullName);
        // For email search tests, use the search term as the email if it contains @
        if (searchTerm.contains("@")) {
            user.setEmail(searchTerm);
        } else {
            // For non-email searches, keep the default email pattern
            String uniqueEmail = user.getUsername() + "@test.com";
            user.setEmail(uniqueEmail);
        }
        entityManager.persistAndFlush(user);
        entityManager.flush();
        entityManager.clear();
        
        // When - search using the search term, not the username
        List<User> foundUsers = userRepository.findUsersWithSearchTerm(searchTerm);
        
        // Then
        if (shouldFind) {
            assertThat(foundUsers).hasSizeGreaterThanOrEqualTo(1);
            // Check if any user matches the expected pattern based on the search term
            boolean foundMatch = foundUsers.stream().anyMatch(u -> {
                // For searches that should find our test user, check if it matches our created user
                // Check if the username starts with our original username (before timestamp)
                if (u.getUsername().startsWith(username)) {
                    // Additional checks based on search term
                    if (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchTerm.toLowerCase())) return true;
                    if (u.getUsername().toLowerCase().contains(searchTerm.toLowerCase())) return true;
                    if (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchTerm.toLowerCase())) return true;
                }
                return false;
            });
            assertThat(foundMatch).isTrue();
        } else {
            // For "xyz" search term that should not be found, ensure our test user is not in the results
            boolean foundMatch = foundUsers.stream().anyMatch(u -> u.getUsername().startsWith(username));
            assertThat(foundMatch).isFalse();
        }
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/auth/user_account_status_test_data.csv", numLinesToSkip = 1)
    void testUserAccountStatus(String username, String fullName, boolean isActive, boolean isLocked, 
                              String lockedUntilOffset, boolean expectedNonLocked) {
        // Given
        User user = createTestUser(username, fullName);
        String uniqueUsername = user.getUsername(); // Store the unique username
        user.setIsActive(isActive);
        user.setIsLocked(isLocked);
        
        if (lockedUntilOffset != null && !lockedUntilOffset.trim().isEmpty()) {
            int offsetMinutes = Integer.parseInt(lockedUntilOffset);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(offsetMinutes));
        }
        
        entityManager.persistAndFlush(user);
        entityManager.flush();
        entityManager.clear();
        
        // When - use the unique username for lookup
        Optional<User> foundUser = userRepository.findByUsername(uniqueUsername);
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().isAccountNonLocked()).isEqualTo(expectedNonLocked);
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/auth/role_permission_test_data.csv", numLinesToSkip = 1)
    void testRolePermissionAssignment(String roleCode, String roleName, String permissionCode, 
                                    String resource, String action, boolean hasPermission) {
        // Given
        Role role = findOrCreateRole(roleCode, roleName);
        Permission permission = findOrCreatePermission(permissionCode, resource, action);
        
        if (hasPermission) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermission.setGrantedDate(LocalDateTime.now());
            rolePermission.setGrantedBy("test");
            entityManager.persistAndFlush(rolePermission);
        }
        
        entityManager.flush();
        entityManager.clear();
        
        // When
        Optional<Role> foundRole = roleRepository.findByRoleCode(roleCode);
        
        // Then
        assertThat(foundRole).isPresent();
        if (hasPermission) {
            // Check that the role has the specific permission we just added
            boolean hasSpecificPermission = foundRole.get().getRolePermissions().stream()
                .anyMatch(rp -> rp.getPermission().getPermissionCode().equals(permissionCode) 
                           && "test".equals(rp.getGrantedBy()));
            assertThat(hasSpecificPermission).isTrue();
        } else {
            // Check that the role does not have the specific permission we didn't add
            boolean hasSpecificPermission = foundRole.get().getRolePermissions().stream()
                .anyMatch(rp -> rp.getPermission().getPermissionCode().equals(permissionCode)
                           && "test".equals(rp.getGrantedBy()));
            assertThat(hasSpecificPermission).isFalse();
        }
    }
    
    private User createTestUser(String username, String fullName) {
        // Make username unique by adding timestamp
        String uniqueUsername = username + "_" + System.nanoTime();
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail(uniqueUsername + "@test.com");
        user.setFullName(fullName);
        user.setIsActive(true);
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedDate(LocalDateTime.now());
        user.setCreatedBy("test");
        
        UserPassword password = new UserPassword();
        password.setUser(user);
        password.setPasswordHash(passwordEncoder.encode("password123"));
        password.setIsActive(true);
        password.setCreatedDate(LocalDateTime.now());
        password.setCreatedBy("test");
        
        user.setPassword(password);
        return entityManager.persistAndFlush(user);
    }
    
    private Role findOrCreateRole(String roleCode, String roleName) {
        Optional<Role> existingRole = roleRepository.findByRoleCode(roleCode);
        if (existingRole.isPresent()) {
            return existingRole.get();
        }
        
        Role role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setIsActive(true);
        role.setCreatedDate(LocalDateTime.now());
        role.setCreatedBy("test");
        return entityManager.persistAndFlush(role);
    }
    
    private Permission findOrCreatePermission(String permissionCode, String resource, String action) {
        Optional<Permission> existingPermission = permissionRepository.findByPermissionCode(permissionCode);
        if (existingPermission.isPresent()) {
            return existingPermission.get();
        }
        
        Permission permission = new Permission();
        permission.setPermissionCode(permissionCode);
        permission.setPermissionName(permissionCode.replace("_", " "));
        permission.setPermissionCategory("TEST");
        permission.setResource(resource);
        permission.setAction(action);
        permission.setCreatedDate(LocalDateTime.now());
        permission.setCreatedBy("test");
        return entityManager.persistAndFlush(permission);
    }
    
    private void createTestRolesAndPermissions() {
        // Create standard roles if they don't exist
        findOrCreateRole("BRANCH_MANAGER", "Branch Manager");
        findOrCreateRole("CS", "Customer Service");
        findOrCreateRole("TELLER", "Teller");
        
        // Create standard permissions if they don't exist
        findOrCreatePermission("USER_READ", "USER", "READ");
        findOrCreatePermission("USER_WRITE", "USER", "WRITE");
        findOrCreatePermission("PRODUCT_READ", "PRODUCT", "READ");
        findOrCreatePermission("PRODUCT_WRITE", "PRODUCT", "WRITE");
        findOrCreatePermission("TRANSACTION_READ", "TRANSACTION", "READ");
        findOrCreatePermission("TRANSACTION_WRITE", "TRANSACTION", "WRITE");
    }
}