package id.ac.tazkia.minibank.unit.entity;

import id.ac.tazkia.minibank.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@yopmail.com");
        user.setFullName("Test User");
        user.setIsActive(true);
        user.setIsLocked(false);
        user.setFailedLoginAttempts(0);
    }

    @Test
    void shouldCreateUserWithDefaultValues() {
        // Given
        User newUser = new User();

        // Then
        assertThat(newUser.getIsActive()).isTrue();
        assertThat(newUser.getIsLocked()).isFalse();
        assertThat(newUser.getFailedLoginAttempts()).isZero();
        assertThat(newUser.getLastLogin()).isNull();
        assertThat(newUser.getLockedUntil()).isNull();
    }

    @Test
    void shouldReturnTrueWhenAccountIsNotLocked() {
        // Given - User is not locked
        user.setIsLocked(false);
        user.setLockedUntil(null);

        // When & Then
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenAccountIsLockedButExpired() {
        // Given - User was locked but lock has expired
        user.setIsLocked(true);
        user.setLockedUntil(LocalDateTime.now().minusHours(1)); // Lock expired 1 hour ago

        // When & Then
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAccountIsCurrentlyLocked() {
        // Given - User is locked and lock hasn't expired
        user.setIsLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusHours(1)); // Lock expires in 1 hour

        // When & Then
        assertThat(user.isAccountNonLocked()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAccountIsLockedWithoutExpiration() {
        // Given - User is locked indefinitely
        user.setIsLocked(true);
        user.setLockedUntil(null);

        // When & Then
        assertThat(user.isAccountNonLocked()).isFalse();
    }

    @Test
    void shouldIncrementFailedLoginAttemptsFromZero() {
        // Given
        user.setFailedLoginAttempts(0);

        // When
        user.incrementFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void shouldIncrementFailedLoginAttemptsFromExistingValue() {
        // Given
        user.setFailedLoginAttempts(3);

        // When
        user.incrementFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isEqualTo(4);
    }

    @Test
    void shouldIncrementFailedLoginAttemptsFromNull() {
        // Given
        user.setFailedLoginAttempts(null);

        // When
        user.incrementFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void shouldResetFailedLoginAttempts() {
        // Given
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(LocalDateTime.now().plusHours(1));

        // When
        user.resetFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void shouldLockAccountWithSpecifiedDuration() {
        // Given
        int lockDurationMinutes = 30;
        LocalDateTime beforeLock = LocalDateTime.now();

        // When
        user.lockAccount(lockDurationMinutes);

        // Then
        assertThat(user.getIsLocked()).isTrue();
        assertThat(user.getLockedUntil()).isAfter(beforeLock);
        assertThat(user.getLockedUntil()).isBefore(beforeLock.plusMinutes(lockDurationMinutes).plusSeconds(1));
    }

    @Test
    void shouldLockAccountForZeroMinutes() {
        // Given
        int lockDurationMinutes = 0;
        LocalDateTime beforeLock = LocalDateTime.now();

        // When
        user.lockAccount(lockDurationMinutes);

        // Then
        assertThat(user.getIsLocked()).isTrue();
        assertThat(user.getLockedUntil()).isAfter(beforeLock.minusSeconds(1));
        assertThat(user.getLockedUntil()).isBefore(beforeLock.plusSeconds(1));
    }

    @Test
    void shouldLockAccountForLongDuration() {
        // Given
        int lockDurationMinutes = 1440; // 24 hours
        LocalDateTime beforeLock = LocalDateTime.now();

        // When
        user.lockAccount(lockDurationMinutes);

        // Then
        assertThat(user.getIsLocked()).isTrue();
        assertThat(user.getLockedUntil()).isAfter(beforeLock.plusHours(23));
        assertThat(user.getLockedUntil()).isBefore(beforeLock.plusHours(25));
    }

    @Test
    void shouldHandleMultipleFailedLoginAttempts() {
        // Given
        user.setFailedLoginAttempts(0);

        // When - Simulate multiple failed attempts
        user.incrementFailedLoginAttempts(); // 1
        user.incrementFailedLoginAttempts(); // 2
        user.incrementFailedLoginAttempts(); // 3

        // Then
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
    }

    @Test
    void shouldResetAttemptsAfterLocking() {
        // Given
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(LocalDateTime.now().plusHours(1));

        // When - Reset after locking
        user.resetFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void shouldHandleAccountLockingScenario() {
        // Given - Simulate failed login scenario
        user.setFailedLoginAttempts(2);

        // When - Third failed attempt triggers lock
        user.incrementFailedLoginAttempts(); // 3 attempts
        user.lockAccount(15); // Lock for 15 minutes

        // Then
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(user.getIsLocked()).isTrue();
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now().plusMinutes(14));
        assertThat(user.isAccountNonLocked()).isFalse();
    }

    @Test
    void shouldHandleSuccessfulLoginAfterFailures() {
        // Given - User had previous failed attempts
        user.setFailedLoginAttempts(2);

        // When - Successful login resets attempts
        user.resetFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void shouldHandleLockExpirationCheck() {
        // Given - Lock that expires in the future
        user.setIsLocked(true);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        
        // When & Then - Account should be locked
        assertThat(user.isAccountNonLocked()).isFalse();

        // Given - Lock that has expired
        user.setLockedUntil(LocalDateTime.now().minusMinutes(5));
        
        // When & Then - Account should not be locked
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldHandleEdgeCaseWithExactLockExpiration() {
        // Given - Lock expires exactly now (within 1 second)
        user.setIsLocked(true);
        user.setLockedUntil(LocalDateTime.now().minusNanos(1000)); // Just expired

        // When & Then
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldMaintainUserStateConsistency() {
        // Given - Initial state
        assertThat(user.getIsActive()).isTrue();
        assertThat(user.getIsLocked()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isZero();

        // When - Lock account
        user.lockAccount(30);

        // Then - Verify state consistency
        assertThat(user.getIsActive()).isTrue(); // User can still be active but locked
        assertThat(user.getIsLocked()).isTrue();
        assertThat(user.isAccountNonLocked()).isFalse();

        // When - Reset after successful login
        user.resetFailedLoginAttempts();

        // Then - Verify unlock state
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        // Note: isLocked flag would need to be manually set to false by the service
    }

    @Test
    void shouldHandleNullFailedLoginAttempts() {
        // Given
        user.setFailedLoginAttempts(null);

        // When
        user.incrementFailedLoginAttempts();

        // Then
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void shouldPreserveLockStateWhenResettingAttempts() {
        // Given
        user.setIsLocked(true);
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(LocalDateTime.now().plusHours(1));

        // When
        user.resetFailedLoginAttempts();

        // Then - Attempts and lockedUntil are reset, but isLocked remains
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getIsLocked()).isTrue(); // This would need manual reset by service
    }
}