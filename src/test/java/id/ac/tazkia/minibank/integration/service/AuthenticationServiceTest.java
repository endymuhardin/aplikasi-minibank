package id.ac.tazkia.minibank.integration.service;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.TestPasswordEncoderConfig;
import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserPassword;
import id.ac.tazkia.minibank.repository.UserRepository;
import id.ac.tazkia.minibank.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({PostgresTestContainersConfiguration.class, TestPasswordEncoderConfig.class})
@Transactional
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/auth/successful_login_test_data.csv", numLinesToSkip = 1)
    void testRecordSuccessfulLogin(String username, String fullName, boolean isActive, int initialFailedAttempts) {
        // Given
        User user = createTestUser(username, fullName, isActive, initialFailedAttempts);
        userRepository.save(user);
        
        // When
        authenticationService.recordSuccessfulLogin(username);
        
        // Then
        Optional<User> updatedUser = userRepository.findByUsername(username);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getLastLogin()).isNotNull();
        assertThat(updatedUser.get().getFailedLoginAttempts()).isEqualTo(0);
        assertThat(updatedUser.get().getLockedUntil()).isNull();
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/auth/failed_login_test_data.csv", numLinesToSkip = 1)
    void testRecordFailedLogin(String username, String fullName, int initialFailedAttempts, int expectedFailedAttempts, boolean shouldBeLocked) {
        // Given
        User user = createTestUser(username, fullName, true, initialFailedAttempts);
        userRepository.save(user);
        
        // When
        authenticationService.recordFailedLogin(username);
        
        // Then
        Optional<User> updatedUser = userRepository.findByUsername(username);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getFailedLoginAttempts()).isEqualTo(expectedFailedAttempts);
        
        if (shouldBeLocked) {
            assertThat(updatedUser.get().getIsLocked()).isTrue();
            assertThat(updatedUser.get().getLockedUntil()).isNotNull();
        } else {
            assertThat(updatedUser.get().getIsLocked()).isFalse();
        }
    }
    
    @Test
    void testGetCurrentUsername_WhenAuthenticated() {
        // Given
        String username = "testuser";
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(username, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // When
        String currentUsername = authenticationService.getCurrentUsername();
        
        // Then
        assertThat(currentUsername).isEqualTo(username);
    }
    
    @Test
    void testGetCurrentUsername_WhenNotAuthenticated() {
        // When
        String currentUsername = authenticationService.getCurrentUsername();
        
        // Then
        assertThat(currentUsername).isEqualTo("anonymous");
    }
    
    @Test
    void testGetCurrentUser_WhenUserExists() {
        // Given
        String username = "existinguser";
        User user = createTestUser(username, "Existing User", true, 0);
        userRepository.save(user);
        
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(username, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // When
        Optional<User> currentUser = authenticationService.getCurrentUser();
        
        // Then
        assertThat(currentUser).isPresent();
        assertThat(currentUser.get().getUsername()).isEqualTo(username);
    }
    
    @Test
    void testGetCurrentUser_WhenUserNotExists() {
        // Given
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken("nonexistent", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // When
        Optional<User> currentUser = authenticationService.getCurrentUser();
        
        // Then
        assertThat(currentUser).isEmpty();
    }
    
    private User createTestUser(String username, String fullName, boolean isActive, int failedLoginAttempts) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setFullName(fullName);
        user.setIsActive(isActive);
        user.setFailedLoginAttempts(failedLoginAttempts);
        user.setCreatedDate(LocalDateTime.now());
        user.setCreatedBy("test");
        
        UserPassword password = new UserPassword();
        password.setUser(user);
        password.setPasswordHash(passwordEncoder.encode("password123"));
        password.setIsActive(true);
        password.setCreatedDate(LocalDateTime.now());
        password.setCreatedBy("test");
        
        user.setPassword(password);
        return user;
    }
}