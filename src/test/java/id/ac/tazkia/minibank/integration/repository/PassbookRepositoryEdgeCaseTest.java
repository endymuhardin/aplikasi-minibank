package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PassbookRepositoryEdgeCaseTest extends BaseRepositoryTest {

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

    private Map<String, Account> accountMap = new HashMap<>();
    private Branch testBranch;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Clean up all data
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
        branchRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        setupBaseTestData();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/passbook/passbook-transactions.csv", numLinesToSkip = 1)
    void shouldLoadPassbookTransactionsFromCsvData(
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

        // Given
        Account account = getOrCreateAccount(accountNumber);

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

        // When
        Transaction savedTransaction = transactionRepository.save(transaction);
        entityManager.flush();

        // Then
        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getTransactionNumber()).isEqualTo(transactionNumber);

        // Verify passbook methods work with this data
        Page<Transaction> transactions = transactionRepository.findByAccount(account, PageRequest.of(0, 50));
        assertThat(transactions.getContent()).contains(savedTransaction);
    }

    @Test
    void shouldHandleEmptyAccountForPassbookPrinting() {
        // Given
        Account emptyAccount = createTestAccount("A9999001", "Empty Account");
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<Transaction> transactions = transactionRepository.findByAccount(emptyAccount, pageable);
        List<Transaction> orderedTransactions = transactionRepository.findByAccountOrderByTransactionDateAsc(emptyAccount);

        // Then
        assertThat(transactions).isNotNull();
        assertThat(transactions.getContent()).isEmpty();
        assertThat(transactions.getTotalElements()).isEqualTo(0);
        assertThat(orderedTransactions).isEmpty();
    }

    @Test
    void shouldHandleSingleTransactionAccount() {
        // Given
        Account singleTransactionAccount = createTestAccount("A9999002", "Single Transaction Account");
        Transaction singleTransaction = createTransaction(
            singleTransactionAccount, "T5000001", Transaction.TransactionType.DEPOSIT,
            new BigDecimal("1000000"), BigDecimal.ZERO,
            "Single deposit", "SINGLE001", Transaction.TransactionChannel.TELLER,
            LocalDateTime.of(2024, 6, 15, 10, 0)
        );
        singleTransaction.setBalanceAfter(new BigDecimal("1000000"));
        transactionRepository.save(singleTransaction);
        entityManager.flush();

        // When
        Page<Transaction> transactions = transactionRepository.findByAccount(singleTransactionAccount, PageRequest.of(0, 20));
        List<Transaction> orderedTransactions = transactionRepository.findByAccountOrderByTransactionDateAsc(singleTransactionAccount);

        // Then
        assertThat(transactions.getContent()).hasSize(1);
        assertThat(transactions.getTotalElements()).isEqualTo(1);
        assertThat(orderedTransactions).hasSize(1);
        assertThat(orderedTransactions.get(0).getTransactionNumber()).isEqualTo("T5000001");
    }

    @Test
    void shouldHandleLargeTransactionVolumeEfficiently() {
        // Given
        Account highVolumeAccount = createTestAccount("A9999003", "High Volume Account");
        List<Transaction> largeTransactionSet = createLargeTransactionSet(highVolumeAccount, 500);
        transactionRepository.saveAll(largeTransactionSet);
        entityManager.flush();

        // When - Performance test for large dataset
        long startTime = System.currentTimeMillis();
        
        Page<Transaction> firstPage = transactionRepository.findByAccount(highVolumeAccount, PageRequest.of(0, 50));
        Page<Transaction> lastPage = transactionRepository.findByAccount(highVolumeAccount, PageRequest.of(9, 50)); // Page 10
        List<Transaction> allOrderedTransactions = transactionRepository.findByAccountOrderByTransactionDateAsc(highVolumeAccount);
        
        long endTime = System.currentTimeMillis();

        // Then
        assertThat(firstPage.getContent()).hasSize(50);
        assertThat(firstPage.getTotalElements()).isEqualTo(500);
        assertThat(lastPage.getContent()).hasSize(50);
        assertThat(allOrderedTransactions).hasSize(500);
        
        // Performance assertion - should complete within reasonable time
        assertThat(endTime - startTime).isLessThan(10000); // 10 seconds max
        
        // Verify chronological ordering in large dataset
        for (int i = 0; i < allOrderedTransactions.size() - 1; i++) {
            assertThat(allOrderedTransactions.get(i).getTransactionDate())
                .isBeforeOrEqualTo(allOrderedTransactions.get(i + 1).getTransactionDate());
        }
    }

    @Test
    void shouldHandleDateRangeAtYearBoundary() {
        // Given
        Account boundaryAccount = createTestAccount("A9999004", "Year Boundary Account");
        
        // Create transactions around year boundary
        Transaction lastYearTransaction = createTransaction(
            boundaryAccount, "T5000101", Transaction.TransactionType.DEPOSIT,
            new BigDecimal("500000"), BigDecimal.ZERO,
            "Last transaction of 2023", "YEAR001", Transaction.TransactionChannel.TELLER,
            LocalDateTime.of(2023, 12, 31, 23, 59, 59)
        );
        lastYearTransaction.setBalanceAfter(new BigDecimal("500000"));

        Transaction firstYearTransaction = createTransaction(
            boundaryAccount, "T5000102", Transaction.TransactionType.DEPOSIT,
            new BigDecimal("300000"), new BigDecimal("500000"),
            "First transaction of 2024", "YEAR002", Transaction.TransactionChannel.ONLINE,
            LocalDateTime.of(2024, 1, 1, 0, 0, 1)
        );
        firstYearTransaction.setBalanceAfter(new BigDecimal("800000"));

        transactionRepository.save(lastYearTransaction);
        transactionRepository.save(firstYearTransaction);
        entityManager.flush();

        // When - Test various date range scenarios
        LocalDateTime startOf2024 = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endOf2023 = LocalDateTime.of(2023, 12, 31, 23, 59);

        Page<Transaction> onlyFirstYear = transactionRepository.findByAccountAndTransactionDateGreaterThanEqual(
            boundaryAccount, startOf2024, PageRequest.of(0, 10));
        Page<Transaction> onlyLastYear = transactionRepository.findByAccountAndTransactionDateLessThan(
            boundaryAccount, startOf2024, PageRequest.of(0, 10));
        Page<Transaction> bothYears = transactionRepository.findByAccountAndTransactionDateBetween(
            boundaryAccount, endOf2023, startOf2024.plusDays(1), PageRequest.of(0, 10));

        // Then
        assertThat(onlyFirstYear.getContent()).hasSize(1);
        assertThat(onlyFirstYear.getContent().get(0).getTransactionNumber()).isEqualTo("T5000102");
        
        assertThat(onlyLastYear.getContent()).hasSize(1);
        assertThat(onlyLastYear.getContent().get(0).getTransactionNumber()).isEqualTo("T5000101");
        
        assertThat(bothYears.getContent()).hasSize(2);
    }

    @Test
    void shouldHandleVeryOldTransactions() {
        // Given
        Account oldAccount = createTestAccount("A9999005", "Old Transaction Account");
        
        // Create very old transactions (5 years ago)
        LocalDateTime fiveYearsAgo = LocalDateTime.now().minusYears(5);
        Transaction oldTransaction = createTransaction(
            oldAccount, "T5000201", Transaction.TransactionType.DEPOSIT,
            new BigDecimal("100000"), BigDecimal.ZERO,
            "Very old transaction", "OLD001", Transaction.TransactionChannel.TELLER,
            fiveYearsAgo
        );
        oldTransaction.setBalanceAfter(new BigDecimal("100000"));
        transactionRepository.save(oldTransaction);
        entityManager.flush();

        // When
        Page<Transaction> allTransactions = transactionRepository.findByAccount(oldAccount, PageRequest.of(0, 10));
        Page<Transaction> recentTransactions = transactionRepository.findByAccountAndTransactionDateGreaterThanEqual(
            oldAccount, LocalDateTime.now().minusDays(30), PageRequest.of(0, 10));

        // Then
        assertThat(allTransactions.getContent()).hasSize(1);
        assertThat(recentTransactions.getContent()).isEmpty(); // No recent transactions
    }

    @Test
    void shouldHandleMultipleConcurrentDateRangeQueries() {
        // Given
        Account concurrencyAccount = createTestAccount("A9999006", "Concurrency Test Account");
        List<Transaction> monthlyTransactions = createMonthlyTransactions(concurrencyAccount);
        transactionRepository.saveAll(monthlyTransactions);
        entityManager.flush();

        // When - Simulate multiple concurrent passbook printing requests
        List<Page<Transaction>> results = new ArrayList<>();
        
        // Multiple date range queries that might happen concurrently
        for (int month = 1; month <= 6; month++) {
            LocalDateTime startOfMonth = LocalDateTime.of(2024, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
            
            Page<Transaction> monthlyResult = transactionRepository.findByAccountAndTransactionDateBetween(
                concurrencyAccount, startOfMonth, endOfMonth, PageRequest.of(0, 20));
            results.add(monthlyResult);
        }

        // Then
        assertThat(results).hasSize(6);
        results.forEach(result -> {
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
        });
        
        // Verify total transactions across all months
        int totalTransactions = results.stream()
            .mapToInt(result -> result.getContent().size())
            .sum();
        assertThat(totalTransactions).isEqualTo(monthlyTransactions.size());
    }

    @Test
    void shouldHandleExtremePageSizes() {
        // Given
        Account extremePageAccount = createTestAccount("A9999007", "Extreme Page Test Account");
        List<Transaction> transactions = createLargeTransactionSet(extremePageAccount, 100);
        transactionRepository.saveAll(transactions);
        entityManager.flush();

        // When - Test extreme page sizes
        Page<Transaction> verySmallPage = transactionRepository.findByAccount(extremePageAccount, PageRequest.of(0, 1));
        Page<Transaction> veryLargePage = transactionRepository.findByAccount(extremePageAccount, PageRequest.of(0, 1000));

        // Then
        assertThat(verySmallPage.getContent()).hasSize(1);
        assertThat(verySmallPage.getTotalElements()).isEqualTo(100);
        
        assertThat(veryLargePage.getContent()).hasSize(100); // All transactions fit in one page
        assertThat(veryLargePage.getTotalElements()).isEqualTo(100);
    }

    @Test
    void shouldMaintainDataIntegrityWithBalanceProgression() {
        // Given
        Account integrityAccount = createTestAccount("A9999008", "Data Integrity Account");
        
        // Create transactions with proper balance progression
        BigDecimal runningBalance = BigDecimal.ZERO;
        List<Transaction> balanceProgressionTransactions = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            BigDecimal amount = new BigDecimal(i * 10000); // Increasing amounts
            Transaction.TransactionType type = (i % 2 == 0) ? Transaction.TransactionType.WITHDRAWAL : Transaction.TransactionType.DEPOSIT;
            
            Transaction transaction = createTransaction(
                integrityAccount, "T5000" + String.format("%03d", i), type,
                amount, runningBalance,
                "Transaction " + i, "REF" + String.format("%03d", i), Transaction.TransactionChannel.TELLER,
                LocalDateTime.of(2024, 1, i, 10, 0)
            );
            
            if (type == Transaction.TransactionType.DEPOSIT) {
                runningBalance = runningBalance.add(amount);
            } else if (runningBalance.compareTo(amount) >= 0) {
                runningBalance = runningBalance.subtract(amount);
            } else {
                // Skip withdrawal if insufficient balance
                continue;
            }
            
            transaction.setBalanceAfter(runningBalance);
            balanceProgressionTransactions.add(transaction);
        }
        
        transactionRepository.saveAll(balanceProgressionTransactions);
        entityManager.flush();

        // When
        List<Transaction> orderedTransactions = transactionRepository.findByAccountOrderByTransactionDateAsc(integrityAccount);

        // Then - Verify balance progression integrity
        BigDecimal expectedBalance = BigDecimal.ZERO;
        for (Transaction transaction : orderedTransactions) {
            assertThat(transaction.getBalanceBefore()).isEqualByComparingTo(expectedBalance);
            
            if (transaction.isCreditTransaction()) {
                expectedBalance = expectedBalance.add(transaction.getAmount());
            } else {
                expectedBalance = expectedBalance.subtract(transaction.getAmount());
            }
            
            assertThat(transaction.getBalanceAfter()).isEqualByComparingTo(expectedBalance);
        }
    }

    private void setupBaseTestData() {
        // Create test branch
        testBranch = new Branch();
        testBranch.setBranchCode("TEST");
        testBranch.setBranchName("Test Branch");
        testBranch.setAddress("Test Address");
        testBranch.setCity("Test City");
        testBranch.setCountry("Indonesia");
        testBranch.setStatus(Branch.BranchStatus.ACTIVE);
        testBranch.setCreatedBy("TEST");
        testBranch = branchRepository.save(testBranch);

        // Create test product
        testProduct = new Product();
        testProduct.setProductCode("TST001");
        testProduct.setProductName("Test Savings Account");
        testProduct.setProductType(Product.ProductType.TABUNGAN_WADIAH);
        testProduct.setProductCategory("Test Category");
        testProduct.setDescription("Test product for passbook testing");
        testProduct.setIsActive(true);
        testProduct.setIsDefault(true);
        testProduct.setCurrency("IDR");
        testProduct.setMinimumOpeningBalance(new BigDecimal("50000"));
        testProduct.setMinimumBalance(new BigDecimal("10000"));
        testProduct.setProfitSharingRatio(new BigDecimal("0.0250"));
        testProduct.setProfitSharingType(Product.ProfitSharingType.WADIAH);
        testProduct.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        testProduct.setCreatedBy("TEST");
        testProduct = productRepository.save(testProduct);

        entityManager.flush();
    }

    private Account getOrCreateAccount(String accountNumber) {
        if (accountMap.containsKey(accountNumber)) {
            return accountMap.get(accountNumber);
        }
        
        Account account = createTestAccount(accountNumber, "Test Account for " + accountNumber);
        accountMap.put(accountNumber, account);
        return account;
    }

    private Account createTestAccount(String accountNumber, String accountName) {
        // Create test customer for this account
        PersonalCustomer customer = new PersonalCustomer();
        customer.setCustomerNumber("C" + accountNumber.substring(1)); // Convert A2000001 to C2000001
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customer.setIdentityNumber("1234567890123456");
        customer.setIdentityType(Customer.IdentityType.KTP);
        customer.setEmail("test." + accountNumber + "@email.com");
        customer.setPhoneNumber("081234567890");
        customer.setAddress("Test Address");
        customer.setCity("Test City");
        customer.setPostalCode("12345");
        customer.setCountry("Indonesia");
        customer.setCreatedBy("TEST");
        customer.setBranch(testBranch);
        customer = customerRepository.save(customer);

        Account account = new Account();
        account.setCustomer(customer);
        account.setProduct(testProduct);
        account.setBranch(testBranch);
        account.setAccountNumber(accountNumber);
        account.setAccountName(accountName);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setOpenedDate(LocalDate.now());
        account.setCreatedBy("TEST");
        return accountRepository.save(account);
    }

    private List<Transaction> createLargeTransactionSet(Account account, int transactionCount) {
        List<Transaction> transactions = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;
        LocalDateTime baseDate = LocalDateTime.of(2024, 1, 1, 9, 0);

        for (int i = 1; i <= transactionCount; i++) {
            Transaction.TransactionType type = (i % 3 == 0) ? Transaction.TransactionType.WITHDRAWAL : Transaction.TransactionType.DEPOSIT;
            BigDecimal amount = new BigDecimal(50000 + (i * 1000)); // Varying amounts
            
            Transaction transaction = createTransaction(
                account, "T6000" + String.format("%03d", i), type,
                amount, runningBalance,
                "Large set transaction " + i, "LARGE" + String.format("%03d", i),
                Transaction.TransactionChannel.TELLER,
                baseDate.plusDays(i % 365).plusHours(i % 24) // Spread across year
            );
            
            if (type == Transaction.TransactionType.DEPOSIT) {
                runningBalance = runningBalance.add(amount);
            } else if (runningBalance.compareTo(amount) >= 0) {
                runningBalance = runningBalance.subtract(amount);
            } else {
                // Convert to deposit if insufficient balance
                transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
                runningBalance = runningBalance.add(amount);
            }
            
            transaction.setBalanceAfter(runningBalance);
            transactions.add(transaction);
        }
        
        return transactions;
    }

    private List<Transaction> createMonthlyTransactions(Account account) {
        List<Transaction> transactions = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;

        for (int month = 1; month <= 6; month++) {
            for (int day = 1; day <= 5; day++) { // 5 transactions per month
                Transaction.TransactionType type = (day % 2 == 0) ? Transaction.TransactionType.WITHDRAWAL : Transaction.TransactionType.DEPOSIT;
                BigDecimal amount = new BigDecimal(100000 + (month * 10000));
                
                Transaction transaction = createTransaction(
                    account, "T7000" + month + String.format("%02d", day), type,
                    amount, runningBalance,
                    "Monthly transaction " + month + "-" + day, "MONTH" + month + day,
                    Transaction.TransactionChannel.TELLER,
                    LocalDateTime.of(2024, month, day * 5, 10, 0) // Spread within month
                );
                
                if (type == Transaction.TransactionType.DEPOSIT) {
                    runningBalance = runningBalance.add(amount);
                } else if (runningBalance.compareTo(amount) >= 0) {
                    runningBalance = runningBalance.subtract(amount);
                } else {
                    // Convert to deposit if insufficient balance
                    transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
                    runningBalance = runningBalance.add(amount);
                }
                
                transaction.setBalanceAfter(runningBalance);
                transactions.add(transaction);
            }
        }
        
        return transactions;
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
}