package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    
    List<UserRole> findByUser(User user);
    
    List<UserRole> findByRole(Role role);
    
    Optional<UserRole> findByUserAndRole(User user, Role role);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.username = :username")
    List<UserRole> findByUsername(@Param("username") String username);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.role.roleCode = :roleCode")
    List<UserRole> findByRoleCode(@Param("roleCode") String roleCode);
    
    boolean existsByUserAndRole(User user, Role role);
    
    void deleteByUserAndRole(User user, Role role);
}