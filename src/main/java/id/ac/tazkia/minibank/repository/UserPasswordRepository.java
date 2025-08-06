package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPasswordRepository extends JpaRepository<UserPassword, UUID> {
    
    Optional<UserPassword> findByUser(User user);
    
    @Query("SELECT up FROM UserPassword up WHERE up.user.username = :username AND up.isActive = true")
    Optional<UserPassword> findActivePasswordByUsername(@Param("username") String username);
    
    @Query("SELECT up FROM UserPassword up WHERE up.isActive = true")
    List<UserPassword> findActivePasswords();
    
    @Query("SELECT up FROM UserPassword up WHERE up.passwordExpiresAt IS NOT NULL AND up.passwordExpiresAt < :currentTime")
    List<UserPassword> findExpiredPasswords(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT up FROM UserPassword up WHERE up.passwordExpiresAt IS NOT NULL AND up.passwordExpiresAt BETWEEN :startTime AND :endTime")
    List<UserPassword> findPasswordsExpiringBetween(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(up) FROM UserPassword up WHERE up.isActive = true")
    Long countActivePasswords();
}