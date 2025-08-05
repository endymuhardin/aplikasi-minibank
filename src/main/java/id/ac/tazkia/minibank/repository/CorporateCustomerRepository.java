package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.CorporateCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CorporateCustomerRepository extends JpaRepository<CorporateCustomer, UUID> {
    
    Optional<CorporateCustomer> findByCustomerNumber(String customerNumber);
    
    Optional<CorporateCustomer> findByCompanyRegistrationNumber(String companyRegistrationNumber);
    
    Optional<CorporateCustomer> findByTaxIdentificationNumber(String taxIdentificationNumber);
    
    Optional<CorporateCustomer> findByEmail(String email);
    
    List<CorporateCustomer> findByCompanyNameContainingIgnoreCase(String companyName);
    
    @Query("SELECT cc FROM CorporateCustomer cc WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(cc.customerNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cc.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cc.companyRegistrationNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cc.taxIdentificationNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cc.contactPersonName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cc.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<CorporateCustomer> findCorporateCustomersWithSearchTerm(@Param("searchTerm") String searchTerm);
    
    boolean existsByCustomerNumber(String customerNumber);
    
    boolean existsByCompanyRegistrationNumber(String companyRegistrationNumber);
    
    boolean existsByTaxIdentificationNumber(String taxIdentificationNumber);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(cc) FROM CorporateCustomer cc")
    Long countCorporateCustomers();
}