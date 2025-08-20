package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Product;
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
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    Optional<Product> findByProductCode(String productCode);
    
    List<Product> findByProductType(Product.ProductType productType);
    
    List<Product> findByProductCategory(String productCategory);
    
    List<Product> findByIsActiveTrue();
    
    List<Product> findByIsActiveTrueAndProductType(Product.ProductType productType);
    
    Optional<Product> findByIsActiveTrueAndIsDefaultTrue();
    
    Optional<Product> findByIsActiveTrueAndIsDefaultTrueAndProductType(Product.ProductType productType);
    
    @Query("SELECT p FROM Product p WHERE " +
           "p.isActive = true AND " +
           "(:productType IS NULL OR p.productType = :productType) AND " +
           "(:category IS NULL OR :category = '' OR p.productCategory = :category) AND " +
           "(:searchTerm IS NULL OR :searchTerm = '' OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> findActiveProductsWithFilters(@Param("productType") Product.ProductType productType,
                                              @Param("category") String category,
                                              @Param("searchTerm") String searchTerm);
    
    @Query("SELECT p FROM Product p WHERE " +
           "p.isActive = true AND " +
           "(:productType IS NULL OR p.productType = :productType) AND " +
           "(:category IS NULL OR :category = '' OR p.productCategory = :category) AND " +
           "(:searchTerm IS NULL OR :searchTerm = '' OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> findActiveProductsWithFilters(@Param("productType") Product.ProductType productType,
                                              @Param("category") String category,
                                              @Param("searchTerm") String searchTerm,
                                              Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.productType = :productType")
    Long countActiveByProductType(@Param("productType") Product.ProductType productType);
    
    @Query("SELECT DISTINCT p.productCategory FROM Product p WHERE p.isActive = true ORDER BY p.productCategory")
    List<String> findDistinctActiveCategories();
    
    boolean existsByProductCode(String productCode);
    
    @Query("SELECT p FROM Product p WHERE p.launchDate <= CURRENT_DATE AND " +
           "(p.retirementDate IS NULL OR p.retirementDate > CURRENT_DATE) AND " +
           "p.isActive = true")
    List<Product> findCurrentlyAvailableProducts();
}