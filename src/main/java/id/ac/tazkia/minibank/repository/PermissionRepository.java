package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    Optional<Permission> findByPermissionCode(String permissionCode);
    
    @Query("SELECT p FROM Permission p WHERE p.permissionCategory = :category")
    List<Permission> findByCategory(@Param("category") String category);
    
    
    @Query("SELECT p FROM Permission p WHERE p.permissionCategory = :category")
    Page<Permission> findByCategoryPage(@Param("category") String category, Pageable pageable);
    
    @Query("SELECT DISTINCT p.permissionCategory FROM Permission p ORDER BY p.permissionCategory")
    List<String> findDistinctCategories();
    
    boolean existsByPermissionCode(String permissionCode);
}