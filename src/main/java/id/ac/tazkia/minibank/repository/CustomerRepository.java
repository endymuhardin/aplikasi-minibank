package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    
    Optional<Customer> findByCustomerNumber(String customerNumber);
    
    Optional<Customer> findByIdentityNumber(String identityNumber);
    
    Optional<Customer> findByCompanyRegistrationNumber(String companyRegistrationNumber);
    
    Optional<Customer> findByEmail(String email);
    
    List<Customer> findByCustomerType(Customer.CustomerType customerType);
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(:customerType IS NULL OR c.customerType = :customerType) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> findCustomersWithFilters(@Param("customerType") Customer.CustomerType customerType,
                                          @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.customerType = :customerType")
    Long countByCustomerType(@Param("customerType") Customer.CustomerType customerType);
    
    boolean existsByCustomerNumber(String customerNumber);
    
    boolean existsByIdentityNumber(String identityNumber);
    
    boolean existsByCompanyRegistrationNumber(String companyRegistrationNumber);
    
    boolean existsByEmail(String email);
}