package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    Optional<Transaction> findByTransactionNumber(String transactionNumber);
    
    List<Transaction> findByAccount(Account account);
    
    List<Transaction> findByAccountId(UUID accountId);
    
    Page<Transaction> findByAccountIdOrderByTransactionDateDesc(UUID accountId, Pageable pageable);
    
    List<Transaction> findByAccountIdAndTransactionType(UUID accountId, Transaction.TransactionType transactionType);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByAccountIdAndDateRange(@Param("accountId") UUID accountId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:accountId IS NULL OR t.account.id = :accountId) AND " +
           "(:transactionType IS NULL OR t.transactionType = :transactionType) AND " +
           "(:channel IS NULL OR t.channel = :channel) AND " +
           "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.transactionDate <= :endDate) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(t.transactionNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.referenceNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY t.transactionDate DESC")
    Page<Transaction> findTransactionsWithFilters(@Param("accountId") UUID accountId,
                                                @Param("transactionType") Transaction.TransactionType transactionType,
                                                @Param("channel") Transaction.TransactionChannel channel,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                @Param("searchTerm") String searchTerm,
                                                Pageable pageable);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.transactionType = :transactionType " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByAccountAndTypeAndDateRange(@Param("accountId") UUID accountId,
                                                        @Param("transactionType") Transaction.TransactionType transactionType,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    Long countTransactionsByAccountAndDateRange(@Param("accountId") UUID accountId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.customer.id = :customerId " +
           "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByCustomerIdOrderByTransactionDateDesc(@Param("customerId") UUID customerId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.referenceNumber = :referenceNumber")
    List<Transaction> findByReferenceNumber(@Param("referenceNumber") String referenceNumber);
    
    boolean existsByTransactionNumber(String transactionNumber);
    
    @Query("SELECT t FROM Transaction t JOIN FETCH t.account JOIN FETCH t.account.customer " +
           "WHERE t.transactionNumber = :transactionNumber")
    Optional<Transaction> findByTransactionNumberWithDetails(@Param("transactionNumber") String transactionNumber);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId ORDER BY t.transactionDate DESC LIMIT 1")
    Optional<Transaction> findLastTransactionByAccountId(@Param("accountId") UUID accountId);
    
    // Search methods for transaction list
    Page<Transaction> findByTransactionNumberContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String transactionNumber, String description, Pageable pageable);
    
    Page<Transaction> findByTransactionType(Transaction.TransactionType transactionType, Pageable pageable);
    
    // Passbook printing methods
    Page<Transaction> findByAccount(Account account, Pageable pageable);
    
    List<Transaction> findByAccountOrderByTransactionDateAsc(Account account);
    
    Page<Transaction> findByAccountAndTransactionDateBetween(Account account, 
                                                           LocalDateTime startDate, 
                                                           LocalDateTime endDate, 
                                                           Pageable pageable);
    
    Page<Transaction> findByAccountAndTransactionDateGreaterThanEqual(Account account, 
                                                                    LocalDateTime startDate, 
                                                                    Pageable pageable);
    
    Page<Transaction> findByAccountAndTransactionDateLessThan(Account account, 
                                                            LocalDateTime endDate, 
                                                            Pageable pageable);
    
    // Methods for account statement generation
    List<Transaction> findByAccountIdAndTransactionDateBetween(UUID accountId, 
                                                             LocalDateTime startDate, 
                                                             LocalDateTime endDate, 
                                                             Sort sort);
    
    List<Transaction> findByAccountIdOrderByTransactionDateDesc(UUID accountId);
}