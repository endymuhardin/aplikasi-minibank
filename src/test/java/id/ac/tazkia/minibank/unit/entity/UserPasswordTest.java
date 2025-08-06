package id.ac.tazkia.minibank.unit.entity;

import id.ac.tazkia.minibank.entity.UserPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserPasswordTest {

    private UserPassword userPassword;

    @BeforeEach
    void setUp() {
        userPassword = new UserPassword();
        userPassword.setPasswordHash("$2a$10$hashedpassword");
        userPassword.setIsActive(true);
        userPassword.setCreatedBy("TEST");
    }

    @Test
    void shouldCreateUserPasswordWithDefaultValues() {
        // Given
        UserPassword newUserPassword = new UserPassword();

        // Then
        assertThat(newUserPassword.getIsActive()).isTrue();
        assertThat(newUserPassword.getPasswordExpiresAt()).isNull();
        assertThat(newUserPassword.getPasswordHash()).isNull();
    }

    @Test
    void shouldReturnFalseWhenPasswordIsNotExpired() {
        // Given - Password expires in the future
        userPassword.setPasswordExpiresAt(LocalDateTime.now().plusDays(30));

        // When & Then
        assertThat(userPassword.isExpired()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenPasswordHasNoExpirationDate() {
        // Given - Password never expires
        userPassword.setPasswordExpiresAt(null);

        // When & Then
        assertThat(userPassword.isExpired()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenPasswordIsExpired() {
        // Given - Password expired in the past
        userPassword.setPasswordExpiresAt(LocalDateTime.now().minusDays(1));

        // When & Then
        assertThat(userPassword.isExpired()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenPasswordExpiredJustNow() {
        // Given - Password expires exactly now (within nanoseconds)
        userPassword.setPasswordExpiresAt(LocalDateTime.now().minusNanos(1000));

        // When & Then
        assertThat(userPassword.isExpired()).isTrue();
    }

    @Test
    void shouldSetPasswordAsExpired() {
        // Given
        userPassword.setIsActive(true);

        // When
        userPassword.setExpired();

        // Then
        assertThat(userPassword.getIsActive()).isFalse();
    }

    @Test
    void shouldMaintainOriginalHashWhenSettingExpired() {
        // Given
        String originalHash = "$2a$10$originalhashedpassword";
        userPassword.setPasswordHash(originalHash);
        userPassword.setIsActive(true);

        // When
        userPassword.setExpired();

        // Then
        assertThat(userPassword.getPasswordHash()).isEqualTo(originalHash);
        assertThat(userPassword.getIsActive()).isFalse();
    }

    @Test
    void shouldHandlePasswordExpirationScenarios() {
        // Test Case 1: Password expires tomorrow
        userPassword.setPasswordExpiresAt(LocalDateTime.now().plusDays(1));
        assertThat(userPassword.isExpired()).isFalse();

        // Test Case 2: Password expired yesterday
        userPassword.setPasswordExpiresAt(LocalDateTime.now().minusDays(1));
        assertThat(userPassword.isExpired()).isTrue();

        // Test Case 3: Password expires in 1 hour
        userPassword.setPasswordExpiresAt(LocalDateTime.now().plusHours(1));
        assertThat(userPassword.isExpired()).isFalse();

        // Test Case 4: Password expired 1 hour ago
        userPassword.setPasswordExpiresAt(LocalDateTime.now().minusHours(1));
        assertThat(userPassword.isExpired()).isTrue();
    }

    @Test
    void shouldHandlePasswordRotationScenario() {
        // Given - Old active password
        userPassword.setIsActive(true);
        userPassword.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));

        // When - Password rotation occurs (old password is expired)
        userPassword.setExpired();

        // Then - Old password should be inactive
        assertThat(userPassword.getIsActive()).isFalse();
        // Expiration date remains unchanged
        assertThat(userPassword.getPasswordExpiresAt()).isAfter(LocalDateTime.now().plusDays(89));
    }

    @Test
    void shouldAllowMultipleSetExpiredCalls() {
        // Given
        userPassword.setIsActive(true);

        // When - Multiple calls to setExpired
        userPassword.setExpired();
        userPassword.setExpired();
        userPassword.setExpired();

        // Then - Should remain inactive
        assertThat(userPassword.getIsActive()).isFalse();
    }

    @Test
    void shouldHandleActivePasswordWithoutExpiration() {
        // Given - Active password that never expires
        userPassword.setIsActive(true);
        userPassword.setPasswordExpiresAt(null);

        // When & Then
        assertThat(userPassword.isExpired()).isFalse();
        assertThat(userPassword.getIsActive()).isTrue();
    }

    @Test
    void shouldHandleInactiveExpiredPassword() {
        // Given - Password that is both inactive and expired
        userPassword.setIsActive(false);
        userPassword.setPasswordExpiresAt(LocalDateTime.now().minusDays(1));

        // When & Then
        assertThat(userPassword.isExpired()).isTrue();
        assertThat(userPassword.getIsActive()).isFalse();
    }

    @Test
    void shouldHandleEdgeCaseWithVeryShortExpiration() {
        // Given - Password expires in 1 second
        userPassword.setPasswordExpiresAt(LocalDateTime.now().plusSeconds(1));

        // When & Then - Should not be expired yet
        assertThat(userPassword.isExpired()).isFalse();

        // Simulate time passing (in real scenario, this would be after 1 second)
        userPassword.setPasswordExpiresAt(LocalDateTime.now().minusNanos(1));
        assertThat(userPassword.isExpired()).isTrue();
    }

    @Test
    void shouldPreserveExpirationDateWhenSettingExpired() {
        // Given
        LocalDateTime originalExpirationDate = LocalDateTime.now().plusDays(30);
        userPassword.setPasswordExpiresAt(originalExpirationDate);
        userPassword.setIsActive(true);

        // When
        userPassword.setExpired();

        // Then - Expiration date should remain unchanged
        assertThat(userPassword.getPasswordExpiresAt()).isEqualTo(originalExpirationDate);
        assertThat(userPassword.getIsActive()).isFalse();
    }

    @Test
    void shouldHandlePasswordLifecycle() {
        // Step 1: New active password
        userPassword.setIsActive(true);
        userPassword.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
        
        assertThat(userPassword.getIsActive()).isTrue();
        assertThat(userPassword.isExpired()).isFalse();

        // Step 2: Password approaches expiration but is still valid
        userPassword.setPasswordExpiresAt(LocalDateTime.now().plusHours(1));
        
        assertThat(userPassword.getIsActive()).isTrue();
        assertThat(userPassword.isExpired()).isFalse();

        // Step 3: Password expires naturally
        userPassword.setPasswordExpiresAt(LocalDateTime.now().minusMinutes(1));
        
        assertThat(userPassword.getIsActive()).isTrue(); // Still active until explicitly set as expired
        assertThat(userPassword.isExpired()).isTrue();

        // Step 4: Password is manually expired/deactivated
        userPassword.setExpired();
        
        assertThat(userPassword.getIsActive()).isFalse();
        assertThat(userPassword.isExpired()).isTrue();
    }

    @Test
    void shouldHandleNullExpirationDateConsistently() {
        // Given - Password with null expiration date
        userPassword.setPasswordExpiresAt(null);

        // When & Then - Multiple checks should be consistent
        assertThat(userPassword.isExpired()).isFalse();
        assertThat(userPassword.isExpired()).isFalse(); // Second call
        assertThat(userPassword.isExpired()).isFalse(); // Third call
    }
}