package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Customer;
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
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    
    Optional<Customer> findByCustomerNumber(String customerNumber);
    
    Optional<Customer> findByEmail(String email);
    
    // Simple search by customer number or email (works for all customer types)
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Customer> findCustomersWithSearchTerm(@Param("searchTerm") String searchTerm);
    
    // Enhanced search for web interface - includes customer number, email, and personal/corporate customer names
    @Query("SELECT DISTINCT c FROM Customer c " +
           "LEFT JOIN PersonalCustomer pc ON c.id = pc.id " +
           "LEFT JOIN CorporateCustomer cc ON c.id = cc.id " +
           "WHERE LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(pc.firstName, ' ', pc.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pc.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pc.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cc.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    Page<Customer> findByCustomerNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String customerNumber, String email, Pageable pageable);
    
    Page<Customer> findByCustomerType(Customer.CustomerType customerType, Pageable pageable);
    
    List<Customer> findByStatus(Customer.CustomerStatus status);
    
    List<Customer> findByCustomerNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String customerNumber, String email);
    
    boolean existsByCustomerNumber(String customerNumber);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(c) FROM Customer c")
    Long countAllCustomers();
}