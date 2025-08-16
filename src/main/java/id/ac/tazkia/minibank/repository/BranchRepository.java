package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Branch;
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
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    
    Optional<Branch> findByBranchCode(String branchCode);
    
    List<Branch> findByStatusOrderByBranchCodeAsc(Branch.BranchStatus status);
    
    List<Branch> findAllByOrderByBranchCodeAsc();
    
    @Query("SELECT b FROM Branch b WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(b.branchCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.branchName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Branch> findBranchesWithSearchTerm(@Param("searchTerm") String searchTerm);
    
    // Pageable search methods for web interface
    Page<Branch> findByBranchCodeContainingIgnoreCaseOrBranchNameContainingIgnoreCaseOrCityContainingIgnoreCase(
        String branchCode, String branchName, String city, Pageable pageable);
    
    Page<Branch> findByStatus(Branch.BranchStatus status, Pageable pageable);
    
    Page<Branch> findByCityContainingIgnoreCase(String city, Pageable pageable);
    
    boolean existsByBranchCode(String branchCode);
    
    // Find main branch
    Optional<Branch> findByIsMainBranchTrue();
    
    // Count branches by status
    @Query("SELECT COUNT(b) FROM Branch b WHERE b.status = :status")
    Long countByStatus(@Param("status") Branch.BranchStatus status);
    
    @Query("SELECT COUNT(b) FROM Branch b")
    Long countAllBranches();
    
    // Find branches by city for regional operations
    List<Branch> findByCityIgnoreCaseOrderByBranchCodeAsc(String city);
    
    // Find active branches for dropdowns
    @Query("SELECT b FROM Branch b WHERE b.status = 'ACTIVE' ORDER BY b.branchCode ASC")
    List<Branch> findActiveBranches();
}