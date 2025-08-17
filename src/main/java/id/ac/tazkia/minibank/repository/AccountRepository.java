package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    List<Account> findByCustomer(Customer customer);
    
    List<Account> findByCustomerId(UUID customerId);
    
    List<Account> findByProduct(Product product);
    
    List<Account> findByProductId(UUID productId);
    
    List<Account> findByStatus(Account.AccountStatus status);
    
    List<Account> findByCustomerAndStatus(Customer customer, Account.AccountStatus status);
    
    List<Account> findByCustomerIdAndStatus(UUID customerId, Account.AccountStatus status);
    
    @Query("SELECT a FROM Account a WHERE a.customer.id = :customerId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT a FROM Account a WHERE " +
           "(:customerId IS NULL OR a.customer.id = :customerId) AND " +
           "(:productType IS NULL OR a.product.productType = :productType) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.accountName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Account> findAccountsWithFilters(@Param("customerId") UUID customerId,
                                        @Param("productType") Product.ProductType productType,
                                        @Param("status") Account.AccountStatus status,
                                        @Param("searchTerm") String searchTerm);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.customer.id = :customerId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = :status")
    Long countByStatus(@Param("status") Account.AccountStatus status);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.product.productType = :productType AND a.status = 'ACTIVE'")
    Long countActiveAccountsByProductType(@Param("productType") Product.ProductType productType);
    
    @Query("SELECT a FROM Account a WHERE a.balance < :minimumBalance AND a.status = 'ACTIVE'")
    List<Account> findAccountsBelowMinimumBalance(@Param("minimumBalance") BigDecimal minimumBalance);
    
    @Query("SELECT a FROM Account a WHERE a.balance = 0 AND a.status = 'ACTIVE'")
    List<Account> findZeroBalanceAccounts();
    
    boolean existsByAccountNumber(String accountNumber);
    
    @Query("SELECT a FROM Account a JOIN FETCH a.customer JOIN FETCH a.product WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithDetails(@Param("accountNumber") String accountNumber);
    
    @Query("SELECT a FROM Account a JOIN FETCH a.product")
    List<Account> findAllWithProduct();
    
    @Query("SELECT a FROM Account a JOIN FETCH a.product WHERE a.customer = :customer")
    List<Account> findByCustomerWithProduct(@Param("customer") Customer customer);
    
    Page<Account> findByAccountNumberContainingIgnoreCaseOrAccountNameContainingIgnoreCase(
        String accountNumber, String accountName, Pageable pageable);
    
    List<Account> findByAccountNumberContainingIgnoreCaseOrAccountNameContainingIgnoreCase(
        String accountNumber, String accountName);
    
    Page<Account> findByStatus(Account.AccountStatus status, Pageable pageable);
}