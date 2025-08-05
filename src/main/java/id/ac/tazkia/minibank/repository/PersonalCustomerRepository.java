package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.PersonalCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalCustomerRepository extends JpaRepository<PersonalCustomer, UUID> {
    
    Optional<PersonalCustomer> findByCustomerNumber(String customerNumber);
    
    Optional<PersonalCustomer> findByIdentityNumber(String identityNumber);
    
    Optional<PersonalCustomer> findByEmail(String email);
    
    List<PersonalCustomer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    @Query("SELECT pc FROM PersonalCustomer pc WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(pc.customerNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pc.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pc.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pc.identityNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pc.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<PersonalCustomer> findPersonalCustomersWithSearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT pc FROM PersonalCustomer pc WHERE pc.dateOfBirth BETWEEN :startDate AND :endDate")
    List<PersonalCustomer> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pc FROM PersonalCustomer pc WHERE " +
           "YEAR(CURRENT_DATE) - YEAR(pc.dateOfBirth) BETWEEN :minAge AND :maxAge")
    List<PersonalCustomer> findByAgeBetween(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
    
    boolean existsByCustomerNumber(String customerNumber);
    
    boolean existsByIdentityNumber(String identityNumber);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(pc) FROM PersonalCustomer pc")
    Long countPersonalCustomers();
}