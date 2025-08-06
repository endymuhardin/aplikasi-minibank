package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByRoleCode(String roleCode);
    
    @Query("SELECT r FROM Role r WHERE r.isActive = true")
    List<Role> findActiveRoles();
    
    boolean existsByRoleCode(String roleCode);
}