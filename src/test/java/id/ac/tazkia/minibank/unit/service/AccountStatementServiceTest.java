package id.ac.tazkia.minibank.unit.service;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import id.ac.tazkia.minibank.service.AccountStatementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountStatementServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountStatementService accountStatementService;

    private UUID testAccountId;
    private String testAccountNumber;
    private Account testAccount;
    private List<Transaction> testTransactions;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testAccountNumber = "ACC0000001";
        
        testAccount = new Account();
        testAccount.setId(testAccountId);
        testAccount.setAccountNumber(testAccountNumber);
        
        Transaction transaction1 = new Transaction();
        transaction1.setId(UUID.randomUUID());
        transaction1.setTransactionNumber("TXN0000001");
        
        Transaction transaction2 = new Transaction();
        transaction2.setId(UUID.randomUUID());
        transaction2.setTransactionNumber("TXN0000002");
        
        testTransactions = Arrays.asList(transaction1, transaction2);
    }

    @Test
    void shouldFindAccountById() {
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        
        Optional<Account> result = accountStatementService.findAccountById(testAccountId);
        
        assertTrue(result.isPresent());
        assertEquals(testAccountId, result.get().getId());
        assertEquals(testAccountNumber, result.get().getAccountNumber());
    }

    @Test
    void shouldFindAccountByAccountNumber() {
        when(accountRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testAccount));
        
        Optional<Account> result = accountStatementService.findAccountByAccountNumber(testAccountNumber);
        
        assertTrue(result.isPresent());
        assertEquals(testAccountId, result.get().getId());
        assertEquals(testAccountNumber, result.get().getAccountNumber());
    }

    @Test
    void shouldGetTransactionsByAccountAndDateRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        when(transactionRepository.findByAccountIdAndTransactionDateBetween(
            eq(testAccountId), eq(startDateTime), eq(endDateTime), any(Sort.class)))
            .thenReturn(testTransactions);
        
        List<Transaction> result = accountStatementService.getTransactionsByAccountAndDateRange(
            testAccountId, startDate, endDate);
        
        assertEquals(2, result.size());
        assertEquals("TXN0000001", result.get(0).getTransactionNumber());
        assertEquals("TXN0000002", result.get(1).getTransactionNumber());
    }

    @Test
    void shouldGetTransactionsByAccountNumber() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        when(accountRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountIdAndTransactionDateBetween(
            eq(testAccountId), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
            .thenReturn(testTransactions);
        
        List<Transaction> result = accountStatementService.getTransactionsByAccountNumber(
            testAccountNumber, startDate, endDate);
        
        assertEquals(2, result.size());
        assertEquals("TXN0000001", result.get(0).getTransactionNumber());
        assertEquals("TXN0000002", result.get(1).getTransactionNumber());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundForTransactionsByAccountNumber() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        String invalidAccountNumber = "INVALID";
        
        when(accountRepository.findByAccountNumber(invalidAccountNumber)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountStatementService.getTransactionsByAccountNumber(invalidAccountNumber, startDate, endDate);
        });
        
        assertEquals("Account not found: " + invalidAccountNumber, exception.getMessage());
    }

    @Test
    void shouldGetRecentTransactions() {
        int limit = 5;
        
        when(transactionRepository.findByAccountIdOrderByTransactionDateDesc(testAccountId))
            .thenReturn(testTransactions);
        
        List<Transaction> result = accountStatementService.getRecentTransactions(testAccountId, limit);
        
        assertEquals(2, result.size());
        assertEquals("TXN0000001", result.get(0).getTransactionNumber());
        assertEquals("TXN0000002", result.get(1).getTransactionNumber());
    }
}