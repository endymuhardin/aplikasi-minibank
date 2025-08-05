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
    
    Optional<Customer> findByEmail(String email);
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> findCustomersWithSearchTerm(@Param("searchTerm") String searchTerm);
    
    boolean existsByCustomerNumber(String customerNumber);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(c) FROM Customer c")
    Long countAllCustomers();
}