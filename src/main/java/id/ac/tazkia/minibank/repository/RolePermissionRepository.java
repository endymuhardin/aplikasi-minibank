package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {
    
    List<RolePermission> findByRole(Role role);
    
    List<RolePermission> findByPermission(Permission permission);
    
    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.roleCode = :roleCode")
    List<RolePermission> findByRoleCode(@Param("roleCode") String roleCode);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.permissionCode = :permissionCode")
    List<RolePermission> findByPermissionCode(@Param("permissionCode") String permissionCode);
    
    @Query("SELECT rp.permission FROM RolePermission rp " +
           "JOIN rp.role r JOIN UserRole ur ON ur.role = r " +
           "WHERE ur.user.username = :username")
    List<Permission> findPermissionsByUsername(@Param("username") String username);
    
    boolean existsByRoleAndPermission(Role role, Permission permission);
    
    void deleteByRoleAndPermission(Role role, Permission permission);
}