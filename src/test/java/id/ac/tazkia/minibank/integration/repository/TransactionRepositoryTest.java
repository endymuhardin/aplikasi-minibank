package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;

/**
 * TransactionRepository tests optimized for parallel execution.
 * Uses dynamic test data to prevent conflicts during concurrent execution.
 * Note: Using SAME_THREAD execution to avoid transaction management conflicts.
 */
@org.junit.jupiter.api.parallel.Execution(org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD)
class TransactionRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private BranchRepository branchRepository;

    // Helper method to create a single test account for individual tests
    private Account createTestAccount() {
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        return accountRepository.save(account);
    }
    
    // Helper method to create test transactions for an account
    private List<Transaction> createTestTransactions(Account account) {
        String prefix = getTestPrefix();
        List<Transaction> transactions = new ArrayList<>();

        // Initial deposit
        Transaction deposit1 = new Transaction();
        deposit1.setAccount(account);
        deposit1.setTransactionNumber("T" + prefix + "01");
        deposit1.setTransactionType(Transaction.TransactionType.DEPOSIT);
        deposit1.setAmount(new BigDecimal("500000"));
        deposit1.setCurrency("IDR");
        deposit1.setBalanceBefore(BigDecimal.ZERO);
        deposit1.setBalanceAfter(new BigDecimal("500000"));
        deposit1.setDescription("Initial deposit");
        deposit1.setReferenceNumber("REF" + prefix + "01");
        deposit1.setChannel(Transaction.TransactionChannel.TELLER);
        deposit1.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        deposit1.setProcessedDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        deposit1.setCreatedBy("TEST");
        transactions.add(transactionRepository.save(deposit1));

        // Withdrawal
        Transaction withdrawal1 = new Transaction();
        withdrawal1.setAccount(account);
        withdrawal1.setTransactionNumber("T" + prefix + "02");
        withdrawal1.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        withdrawal1.setAmount(new BigDecimal("50000"));
        withdrawal1.setCurrency("IDR");
        withdrawal1.setBalanceBefore(new BigDecimal("500000"));
        withdrawal1.setBalanceAfter(new BigDecimal("450000"));
        withdrawal1.setDescription("ATM withdrawal");
        withdrawal1.setReferenceNumber("REF" + prefix + "02");
        withdrawal1.setChannel(Transaction.TransactionChannel.ATM);
        withdrawal1.setTransactionDate(LocalDateTime.of(2024, 1, 25, 16, 45));
        withdrawal1.setProcessedDate(LocalDateTime.of(2024, 1, 25, 16, 45));
        withdrawal1.setCreatedBy("TEST");
        transactions.add(transactionRepository.save(withdrawal1));

        // Another deposit
        Transaction deposit2 = new Transaction();
        deposit2.setAccount(account);
        deposit2.setTransactionNumber("T" + prefix + "03");
        deposit2.setTransactionType(Transaction.TransactionType.DEPOSIT);
        deposit2.setAmount(new BigDecimal("100000"));
        deposit2.setCurrency("IDR");
        deposit2.setBalanceBefore(new BigDecimal("450000"));
        deposit2.setBalanceAfter(new BigDecimal("550000"));
        deposit2.setDescription("Salary deposit");
        deposit2.setReferenceNumber("REF" + prefix + "03");
        deposit2.setChannel(Transaction.TransactionChannel.ONLINE);
        deposit2.setTransactionDate(LocalDateTime.of(2024, 2, 5, 8, 30));
        deposit2.setProcessedDate(LocalDateTime.of(2024, 2, 5, 8, 30));
        deposit2.setCreatedBy("TEST");
        transactions.add(transactionRepository.save(deposit2));

        return transactions;
    }
    
    // Helper method to create extended transaction history for passbook tests
    private void createExtendedTransactionHistory(Account account) {
        BigDecimal runningBalance = BigDecimal.ZERO;
        String prefix = getTestPrefix();
        
        // Create a realistic transaction history over several months
        LocalDateTime baseDate = LocalDateTime.of(2024, 1, 1, 9, 0);
        
        for (int month = 1; month <= 6; month++) {
            for (int day = 1; day <= 28; day += 7) { // Weekly transactions
                
                // Salary deposit (monthly on 1st)
                if (day == 1) {
                    Transaction salaryDeposit = createTransaction(
                        account,
                        "T" + prefix + String.format("%04d", (month * 100) + day),
                        Transaction.TransactionType.DEPOSIT,
                        new BigDecimal("5000000"), // 5 million IDR salary
                        runningBalance,
                        "Salary deposit - Month " + month,
                        "SAL" + prefix + month + String.format("%02d", day),
                        Transaction.TransactionChannel.ONLINE,
                        baseDate.plusMonths(month - 1).plusDays(day - 1).plusHours(8)
                    );
                    runningBalance = runningBalance.add(salaryDeposit.getAmount());
                    salaryDeposit.setBalanceAfter(runningBalance);
                    transactionRepository.save(salaryDeposit);
                }
                
                // Weekly ATM withdrawal
                if (day > 1 && runningBalance.compareTo(new BigDecimal("500000")) > 0) {
                    Transaction atmWithdrawal = createTransaction(
                        account,
                        "T" + prefix + String.format("%04d", (month * 100) + day + 50),
                        Transaction.TransactionType.WITHDRAWAL,
                        new BigDecimal("500000"), // 500k IDR withdrawal
                        runningBalance,
                        "ATM withdrawal - Week " + (day / 7 + 1),
                        "ATM" + prefix + month + String.format("%02d", day),
                        Transaction.TransactionChannel.ATM,
                        baseDate.plusMonths(month - 1).plusDays(day - 1).plusHours(18)
                    );
                    runningBalance = runningBalance.subtract(atmWithdrawal.getAmount());
                    atmWithdrawal.setBalanceAfter(runningBalance);
                    transactionRepository.save(atmWithdrawal);
                }
                
                // Bi-weekly bill payment
                if (day % 14 == 0 && runningBalance.compareTo(new BigDecimal("200000")) > 0) {
                    Transaction billPayment = createTransaction(
                        account,
                        "T" + prefix + String.format("%04d", (month * 100) + day + 75),
                        Transaction.TransactionType.WITHDRAWAL,
                        new BigDecimal("150000"), // 150k IDR bill
                        runningBalance,
                        "Utility bill payment",
                        "BILL" + prefix + month + String.format("%02d", day),
                        Transaction.TransactionChannel.ONLINE,
                        baseDate.plusMonths(month - 1).plusDays(day - 1).plusHours(14)
                    );
                    runningBalance = runningBalance.subtract(billPayment.getAmount());
                    billPayment.setBalanceAfter(runningBalance);
                    transactionRepository.save(billPayment);
                }
            }
        }
    }

    private Transaction createTransaction(Account account, String transactionNumber, 
                                       Transaction.TransactionType type, BigDecimal amount,
                                       BigDecimal balanceBefore, String description,
                                       String referenceNumber, Transaction.TransactionChannel channel,
                                       LocalDateTime transactionDate) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionNumber(transactionNumber);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setCurrency("IDR");
        transaction.setBalanceBefore(balanceBefore);
        transaction.setDescription(description);
        transaction.setReferenceNumber(referenceNumber);
        transaction.setChannel(channel);
        transaction.setTransactionDate(transactionDate);
        transaction.setProcessedDate(transactionDate);
        transaction.setCreatedBy("TEST");
        return transaction;
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/transaction/transactions.csv", numLinesToSkip = 1)
    void shouldSaveAndFindTransactionFromCsv(
            String transactionNumber,
            String accountNumber,
            String transactionType,
            String amountStr,
            String currency,
            String balanceBeforeStr,
            String balanceAfterStr,
            String description,
            String referenceNumber,
            String channel,
            String transactionDateStr) {
            
        logTestExecution("shouldSaveAndFindTransactionFromCsv");

        // Given - Create unique test data for this specific test
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        account = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionNumber(transactionNumber);
        transaction.setTransactionType(Transaction.TransactionType.valueOf(transactionType));
        transaction.setAmount(new BigDecimal(amountStr));
        transaction.setCurrency(currency);
        transaction.setBalanceBefore(new BigDecimal(balanceBeforeStr));
        transaction.setBalanceAfter(new BigDecimal(balanceAfterStr));
        transaction.setDescription(description);
        transaction.setReferenceNumber(referenceNumber);
        transaction.setChannel(Transaction.TransactionChannel.valueOf(channel));
        transaction.setTransactionDate(LocalDateTime.parse(transactionDateStr));
        transaction.setProcessedDate(LocalDateTime.parse(transactionDateStr));
        transaction.setCreatedBy("TEST");

        // When - Save transaction
        Transaction savedTransaction = transactionRepository.save(transaction);
        entityManager.flush();

        // Then - Verify transaction was saved correctly
        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getTransactionNumber()).isEqualTo(transactionNumber);
        assertThat(savedTransaction.getTransactionType().name()).isEqualTo(transactionType);
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo(amountStr);
        assertThat(savedTransaction.getBalanceBefore()).isEqualByComparingTo(balanceBeforeStr);
        assertThat(savedTransaction.getBalanceAfter()).isEqualByComparingTo(balanceAfterStr);

        // Verify we can find by transaction number
        Optional<Transaction> foundTransaction = transactionRepository.findByTransactionNumber(transactionNumber);
        assertThat(foundTransaction).isPresent();
        assertThat(foundTransaction.get().getTransactionNumber()).isEqualTo(transactionNumber);
    }

    @Test
    void shouldFindTransactionsByAccount() {
        logTestExecution("shouldFindTransactionsByAccount");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        List<Transaction> savedTransactions = createTestTransactions(account);

        // When
        List<Transaction> transactions = transactionRepository.findByAccount(account);

        // Then
        assertThat(transactions).hasSizeGreaterThan(0);
        assertThat(transactions).hasSize(savedTransactions.size());
        transactions.forEach(transaction -> 
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId()));
    }

    @Test
    void shouldFindTransactionsByAccountId() {
        logTestExecution("shouldFindTransactionsByAccountId");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        List<Transaction> savedTransactions = createTestTransactions(account);

        // When
        List<Transaction> transactions = transactionRepository.findByAccountId(account.getId());

        // Then
        assertThat(transactions).hasSizeGreaterThan(0);
        assertThat(transactions).hasSize(savedTransactions.size());
        transactions.forEach(transaction -> 
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId()));
    }

    @Test
    void shouldFindTransactionsByAccountIdWithPagination() {
        logTestExecution("shouldFindTransactionsByAccountIdWithPagination");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);
        
        // When
        Pageable pageable = PageRequest.of(0, 5);
        Page<Transaction> transactionPage = transactionRepository.findByAccountIdOrderByTransactionDateDesc(
            account.getId(), pageable);

        // Then
        assertThat(transactionPage.getContent()).hasSizeGreaterThan(0);
        assertThat(transactionPage.getContent()).hasSizeLessThanOrEqualTo(5);
        
        // Verify ordering by transaction date descending
        List<Transaction> transactions = transactionPage.getContent();
        for (int i = 0; i < transactions.size() - 1; i++) {
            assertThat(transactions.get(i).getTransactionDate())
                .isAfterOrEqualTo(transactions.get(i + 1).getTransactionDate());
        }
    }

    @Test
    void shouldFindTransactionsByAccountIdAndTransactionType() {
        logTestExecution("shouldFindTransactionsByAccountIdAndTransactionType");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);

        // When
        List<Transaction> deposits = transactionRepository.findByAccountIdAndTransactionType(
            account.getId(), Transaction.TransactionType.DEPOSIT);

        // Then
        assertThat(deposits).hasSizeGreaterThan(0);
        deposits.forEach(transaction -> {
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId());
            assertThat(transaction.getTransactionType()).isEqualTo(Transaction.TransactionType.DEPOSIT);
        });
    }

    @Test
    void shouldFindTransactionsByDateRange() {
        logTestExecution("shouldFindTransactionsByDateRange");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);

        // When
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(
            account.getId(), startDate, endDate);

        // Then
        assertThat(transactions).hasSizeGreaterThan(0);
        transactions.forEach(transaction -> {
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId());
            assertThat(transaction.getTransactionDate()).isBetween(startDate, endDate);
        });
    }

    @Test
    void shouldFindTransactionsByDateRangeWithPagination() {
        logTestExecution("shouldFindTransactionsByDateRangeWithPagination");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);

        // When
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 3);
        
        Page<Transaction> transactionPage = transactionRepository.findByAccountIdAndDateRange(
            account.getId(), startDate, endDate, pageable);

        // Then
        assertThat(transactionPage.getContent()).hasSizeGreaterThan(0);
        assertThat(transactionPage.getContent()).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void shouldGetTotalAmountByAccountAndTypeAndDateRange() {
        logTestExecution("shouldGetTotalAmountByAccountAndTypeAndDateRange");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);

        // When
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        BigDecimal totalDeposits = transactionRepository.getTotalAmountByAccountAndTypeAndDateRange(
            account.getId(), Transaction.TransactionType.DEPOSIT, startDate, endDate);

        // Then
        assertThat(totalDeposits).isNotNull();
        assertThat(totalDeposits.compareTo(BigDecimal.ZERO)).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldCountTransactionsByAccountAndDateRange() {
        logTestExecution("shouldCountTransactionsByAccountAndDateRange");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);

        // When
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        Long transactionCount = transactionRepository.countTransactionsByAccountAndDateRange(
            account.getId(), startDate, endDate);

        // Then
        assertThat(transactionCount).isGreaterThan(0);
    }

    @Test
    void shouldFindTransactionsByCustomerId() {
        logTestExecution("shouldFindTransactionsByCustomerId");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = transactionRepository.findByCustomerIdOrderByTransactionDateDesc(
            account.getCustomer().getId(), pageable);

        // Then
        assertThat(transactionPage.getContent()).hasSizeGreaterThan(0);
        transactionPage.getContent().forEach(transaction ->
            assertThat(transaction.getAccount().getCustomer().getId())
                .isEqualTo(account.getCustomer().getId()));
    }

    @Test
    void shouldFindTransactionsByReferenceNumber() {
        logTestExecution("shouldFindTransactionsByReferenceNumber");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        List<Transaction> savedTransactions = createTestTransactions(account);
        String testReferenceNumber = savedTransactions.get(0).getReferenceNumber();

        // When
        List<Transaction> transactions = transactionRepository.findByReferenceNumber(testReferenceNumber);

        // Then
        assertThat(transactions).hasSizeGreaterThan(0);
        transactions.forEach(transaction -> 
            assertThat(transaction.getReferenceNumber()).isEqualTo(testReferenceNumber));
    }

    @Test
    void shouldCheckTransactionNumberExistence() {
        logTestExecution("shouldCheckTransactionNumberExistence");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        List<Transaction> savedTransactions = createTestTransactions(account);
        String existingTransactionNumber = savedTransactions.get(0).getTransactionNumber();

        // When & Then
        assertThat(transactionRepository.existsByTransactionNumber(existingTransactionNumber)).isTrue();
        assertThat(transactionRepository.existsByTransactionNumber("T9999999")).isFalse();
    }

    @Test
    void shouldFindTransactionByTransactionNumberWithDetails() {
        logTestExecution("shouldFindTransactionByTransactionNumberWithDetails");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        List<Transaction> savedTransactions = createTestTransactions(account);
        String testTransactionNumber = savedTransactions.get(0).getTransactionNumber();

        // When
        Optional<Transaction> transaction = transactionRepository.findByTransactionNumberWithDetails(testTransactionNumber);

        // Then
        assertThat(transaction).isPresent();
        assertThat(transaction.get().getAccount()).isNotNull();
        assertThat(transaction.get().getAccount().getCustomer()).isNotNull();
        assertThat(transaction.get().getTransactionNumber()).isEqualTo(testTransactionNumber);
    }

    @Test
    void shouldFindLastTransactionByAccountId() {
        logTestExecution("shouldFindLastTransactionByAccountId");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);

        // When
        Optional<Transaction> lastTransaction = transactionRepository.findLastTransactionByAccountId(account.getId());

        // Then
        assertThat(lastTransaction).isPresent();
        assertThat(lastTransaction.get().getAccount().getId()).isEqualTo(account.getId());
    }


    @Test
    void shouldTestTransactionBusinessMethods() {
        // Given
        Transaction depositTransaction = new Transaction();
        depositTransaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        
        Transaction withdrawalTransaction = new Transaction();
        withdrawalTransaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);

        // When & Then
        assertThat(depositTransaction.isCreditTransaction()).isTrue();
        assertThat(depositTransaction.isDebitTransaction()).isFalse();
        
        assertThat(withdrawalTransaction.isDebitTransaction()).isTrue();
        assertThat(withdrawalTransaction.isCreditTransaction()).isFalse();
    }

    // ========== Passbook Printing Repository Methods Tests ==========

    @Test
    void shouldFindTransactionsByAccountWithPagination() {
        logTestExecution("shouldFindTransactionsByAccountWithPagination");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Transaction> transactionPage = transactionRepository.findByAccount(account, pageable);

        // Then
        assertThat(transactionPage).isNotNull();
        assertThat(transactionPage.getContent()).hasSizeLessThanOrEqualTo(2);
        assertThat(transactionPage.getTotalElements()).isGreaterThan(0);
        
        // Verify all transactions belong to the account
        transactionPage.getContent().forEach(transaction -> 
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId()));
    }

    @Test
    void shouldFindTransactionsByAccountOrderedByDateAscending() {
        logTestExecution("shouldFindTransactionsByAccountOrderedByDateAscending");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);

        // When
        List<Transaction> transactions = transactionRepository.findByAccountOrderByTransactionDateAsc(account);

        // Then
        assertThat(transactions).isNotEmpty();
        
        // Verify ordering by transaction date ascending
        for (int i = 0; i < transactions.size() - 1; i++) {
            assertThat(transactions.get(i).getTransactionDate())
                .isBeforeOrEqualTo(transactions.get(i + 1).getTransactionDate());
        }
        
        // Verify all transactions belong to the account
        transactions.forEach(transaction -> 
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId()));
    }

    @Test
    void shouldFindTransactionsByAccountAndDateRangeWithPagination() {
        logTestExecution("shouldFindTransactionsByAccountAndDateRangeWithPagination");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 2, 28, 23, 59);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Transaction> transactionPage = transactionRepository.findByAccountAndTransactionDateBetween(
            account, startDate, endDate, pageable);

        // Then
        assertThat(transactionPage).isNotNull();
        assertThat(transactionPage.getContent()).isNotEmpty();
        
        // Verify date range filtering
        transactionPage.getContent().forEach(transaction -> {
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId());
            assertThat(transaction.getTransactionDate()).isBetween(startDate, endDate);
        });
    }

    @Test
    void shouldFindTransactionsByAccountAndStartDateWithPagination() {
        logTestExecution("shouldFindTransactionsByAccountAndStartDateWithPagination");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);
        LocalDateTime startDate = LocalDateTime.of(2024, 2, 1, 0, 0);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Transaction> transactionPage = transactionRepository.findByAccountAndTransactionDateGreaterThanEqual(
            account, startDate, pageable);

        // Then
        assertThat(transactionPage).isNotNull();
        
        // Verify start date filtering
        transactionPage.getContent().forEach(transaction -> {
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId());
            assertThat(transaction.getTransactionDate()).isAfterOrEqualTo(startDate);
        });
    }

    @Test
    void shouldFindTransactionsByAccountAndEndDateWithPagination() {
        logTestExecution("shouldFindTransactionsByAccountAndEndDateWithPagination");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);
        LocalDateTime endDate = LocalDateTime.of(2024, 2, 1, 0, 0);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Transaction> transactionPage = transactionRepository.findByAccountAndTransactionDateLessThan(
            account, endDate, pageable);

        // Then
        assertThat(transactionPage).isNotNull();
        
        // Verify end date filtering
        transactionPage.getContent().forEach(transaction -> {
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId());
            assertThat(transaction.getTransactionDate()).isBefore(endDate);
        });
    }

    @Test
    void shouldReturnEmptyResultForAccountWithNoTransactions() {
        logTestExecution("shouldReturnEmptyResultForAccountWithNoTransactions");
        
        // Given - Create test account without transactions
        Account accountWithoutTransactions = createTestAccount(); // This account has no transactions
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Transaction> transactionPage = transactionRepository.findByAccount(accountWithoutTransactions, pageable);

        // Then
        assertThat(transactionPage).isNotNull();
        assertThat(transactionPage.getContent()).isEmpty();
        assertThat(transactionPage.getTotalElements()).isEqualTo(0);
    }

    @Test
    void shouldHandleDateRangeWithNoMatches() {
        logTestExecution("shouldHandleDateRangeWithNoMatches");
        
        // Given - Create test account and transactions
        Account account = createTestAccount();
        createTestTransactions(account);
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0); // Future date
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Transaction> transactionPage = transactionRepository.findByAccountAndTransactionDateBetween(
            account, startDate, endDate, pageable);

        // Then
        assertThat(transactionPage).isNotNull();
        assertThat(transactionPage.getContent()).isEmpty();
        assertThat(transactionPage.getTotalElements()).isEqualTo(0);
    }

    @Test
    void shouldFindTransactionsForPassbookPrintingScenario() {
        logTestExecution("shouldFindTransactionsForPassbookPrintingScenario");
        
        // Given - Create a realistic passbook printing scenario
        Account account = createTestAccount();
        createExtendedTransactionHistory(account);
        
        // Recent transactions (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Pageable recentTransactions = PageRequest.of(0, 20);

        // When
        Page<Transaction> recentPage = transactionRepository.findByAccountAndTransactionDateGreaterThanEqual(
            account, thirtyDaysAgo, recentTransactions);
        
        List<Transaction> allTransactions = transactionRepository.findByAccountOrderByTransactionDateAsc(account);

        // Then
        assertThat(recentPage).isNotNull();
        assertThat(allTransactions).isNotEmpty();
        
        // Verify chronological ordering for passbook display
        for (int i = 0; i < allTransactions.size() - 1; i++) {
            assertThat(allTransactions.get(i).getTransactionDate())
                .isBeforeOrEqualTo(allTransactions.get(i + 1).getTransactionDate());
        }
        
        // Verify balance progression (each transaction should have correct running balance)
        BigDecimal runningBalance = BigDecimal.ZERO;
        for (Transaction transaction : allTransactions) {
            assertThat(transaction.getBalanceBefore()).isEqualByComparingTo(runningBalance);
            
            if (transaction.isCreditTransaction()) {
                runningBalance = runningBalance.add(transaction.getAmount());
            } else {
                runningBalance = runningBalance.subtract(transaction.getAmount());
            }
            
            assertThat(transaction.getBalanceAfter()).isEqualByComparingTo(runningBalance);
        }
    }

    @Test
    void shouldHandleLargePageSizeForPassbookPrinting() {
        logTestExecution("shouldHandleLargePageSizeForPassbookPrinting");
        
        // Given - Create test account with extended transaction history
        Account account = createTestAccount();
        createExtendedTransactionHistory(account);
        Pageable largePage = PageRequest.of(0, 1000); // Large page size for printing

        // When
        Page<Transaction> transactionPage = transactionRepository.findByAccount(account, largePage);

        // Then
        assertThat(transactionPage).isNotNull();
        assertThat(transactionPage.getContent()).isNotEmpty();
        assertThat(transactionPage.getContent().size()).isLessThanOrEqualTo(1000);
        
        // Performance check - should complete in reasonable time
        long startTime = System.currentTimeMillis();
        transactionRepository.findByAccount(account, largePage);
        long endTime = System.currentTimeMillis();
        assertThat(endTime - startTime).isLessThan(5000); // Should complete within 5 seconds
    }
}