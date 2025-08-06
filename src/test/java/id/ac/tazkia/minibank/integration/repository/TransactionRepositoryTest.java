package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TransactionRepositoryTest {

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

    private Map<String, Account> accountMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        setupTestData();
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

        // Given - Create transaction from CSV data
        Account account = accountMap.get(accountNumber);
        assertThat(account).isNotNull();

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
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
        List<Transaction> transactions = transactionRepository.findByAccount(account);

        // Then
        assertThat(transactions).hasSizeGreaterThan(0);
        transactions.forEach(transaction -> 
            assertThat(transaction.getAccount().getAccountNumber()).isEqualTo("A2000001"));
    }

    @Test
    void shouldFindTransactionsByAccountId() {
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
        List<Transaction> transactions = transactionRepository.findByAccountId(account.getId());

        // Then
        assertThat(transactions).hasSizeGreaterThan(0);
        transactions.forEach(transaction -> 
            assertThat(transaction.getAccount().getId()).isEqualTo(account.getId()));
    }

    @Test
    void shouldFindTransactionsByAccountIdWithPagination() {
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
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
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
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
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
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
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
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
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
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
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        Long transactionCount = transactionRepository.countTransactionsByAccountAndDateRange(
            account.getId(), startDate, endDate);

        // Then
        assertThat(transactionCount).isGreaterThan(0);
    }

    @Test
    void shouldFindTransactionsByCustomerId() {
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
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
        // Given
        saveTestTransactions();

        // When
        List<Transaction> transactions = transactionRepository.findByReferenceNumber("REF001");

        // Then
        assertThat(transactions).hasSizeGreaterThan(0);
        transactions.forEach(transaction -> 
            assertThat(transaction.getReferenceNumber()).isEqualTo("REF001"));
    }

    @Test
    void shouldCheckTransactionNumberExistence() {
        // Given
        saveTestTransactions();

        // When & Then
        assertThat(transactionRepository.existsByTransactionNumber("T3000001")).isTrue();
        assertThat(transactionRepository.existsByTransactionNumber("T9999999")).isFalse();
    }

    @Test
    void shouldFindTransactionByTransactionNumberWithDetails() {
        // Given
        saveTestTransactions();

        // When
        Optional<Transaction> transaction = transactionRepository.findByTransactionNumberWithDetails("T3000001");

        // Then
        assertThat(transaction).isPresent();
        assertThat(transaction.get().getAccount()).isNotNull();
        assertThat(transaction.get().getAccount().getCustomer()).isNotNull();
        assertThat(transaction.get().getTransactionNumber()).isEqualTo("T3000001");
    }

    @Test
    void shouldFindLastTransactionByAccountId() {
        // Given
        saveTestTransactions();

        // When
        Account account = accountMap.get("A2000001");
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

    private void setupTestData() {
        // Create test customers to match CSV transaction data
        PersonalCustomer customer1 = new PersonalCustomer();
        customer1.setCustomerNumber("C1000001");
        customer1.setFirstName("Ahmad");
        customer1.setLastName("Suharto");
        customer1.setDateOfBirth(LocalDate.of(1985, 3, 15));
        customer1.setIdentityNumber("3271081503850001");
        customer1.setIdentityType(Customer.IdentityType.KTP);
        customer1.setEmail("ahmad.suharto@email.com");
        customer1.setPhoneNumber("081234567890");
        customer1.setAddress("Jl. Sudirman No. 123");
        customer1.setCity("Jakarta");
        customer1.setPostalCode("10220");
        customer1.setCountry("Indonesia");
        customer1.setCreatedBy("TEST");
        customerRepository.save(customer1);

        PersonalCustomer customer2 = new PersonalCustomer();
        customer2.setCustomerNumber("C1000002");
        customer2.setFirstName("Siti");
        customer2.setLastName("Nurhaliza");
        customer2.setDateOfBirth(LocalDate.of(1990, 7, 22));
        customer2.setIdentityNumber("3271082207900002");
        customer2.setIdentityType(Customer.IdentityType.KTP);
        customer2.setEmail("siti.nurhaliza@email.com");
        customer2.setPhoneNumber("081234567891");
        customer2.setAddress("Jl. Thamrin No. 456");
        customer2.setCity("Jakarta");
        customer2.setPostalCode("10230");
        customer2.setCountry("Indonesia");
        customer2.setCreatedBy("TEST");
        customerRepository.save(customer2);

        PersonalCustomer customer3 = new PersonalCustomer();
        customer3.setCustomerNumber("C1000003");
        customer3.setFirstName("Budi");
        customer3.setLastName("Santoso");
        customer3.setDateOfBirth(LocalDate.of(1988, 12, 10));
        customer3.setIdentityNumber("3271081012880003");
        customer3.setIdentityType(Customer.IdentityType.KTP);
        customer3.setEmail("budi.santoso@email.com");
        customer3.setPhoneNumber("081234567892");
        customer3.setAddress("Jl. Gatot Subroto No. 789");
        customer3.setCity("Jakarta");
        customer3.setPostalCode("12950");
        customer3.setCountry("Indonesia");
        customer3.setCreatedBy("TEST");
        customerRepository.save(customer3);

        // Create test products
        Product savingsProduct = new Product();
        savingsProduct.setProductCode("SAV001");
        savingsProduct.setProductName("Basic Savings Account");
        savingsProduct.setProductType(Product.ProductType.SAVINGS);
        savingsProduct.setProductCategory("Regular Savings");
        savingsProduct.setDescription("Basic savings account");
        savingsProduct.setIsActive(true);
        savingsProduct.setIsDefault(true);
        savingsProduct.setCurrency("IDR");
        savingsProduct.setMinimumOpeningBalance(new BigDecimal("50000"));
        savingsProduct.setMinimumBalance(new BigDecimal("10000"));
        savingsProduct.setInterestRate(new BigDecimal("0.0275"));
        savingsProduct.setInterestCalculationType(Product.InterestCalculationType.DAILY);
        savingsProduct.setInterestPaymentFrequency(Product.InterestPaymentFrequency.MONTHLY);
        savingsProduct.setCreatedBy("TEST");
        productRepository.save(savingsProduct);

        Product checkingProduct = new Product();
        checkingProduct.setProductCode("CHK001");
        checkingProduct.setProductName("Basic Checking Account");
        checkingProduct.setProductType(Product.ProductType.CHECKING);
        checkingProduct.setProductCategory("Regular Checking");
        checkingProduct.setDescription("Basic checking account");
        checkingProduct.setIsActive(true);
        checkingProduct.setIsDefault(false);
        checkingProduct.setCurrency("IDR");
        checkingProduct.setMinimumOpeningBalance(new BigDecimal("100000"));
        checkingProduct.setMinimumBalance(new BigDecimal("50000"));
        checkingProduct.setInterestRate(new BigDecimal("0.0100"));
        checkingProduct.setInterestCalculationType(Product.InterestCalculationType.DAILY);
        checkingProduct.setInterestPaymentFrequency(Product.InterestPaymentFrequency.MONTHLY);
        checkingProduct.setAllowOverdraft(true);
        checkingProduct.setCreatedBy("TEST");
        productRepository.save(checkingProduct);

        // Create test accounts to match CSV transaction data
        Account account1 = new Account();
        account1.setCustomer(customer1);
        account1.setProduct(savingsProduct);
        account1.setAccountNumber("A2000001");
        account1.setAccountName("Ahmad Suharto - Savings");
        account1.setBalance(new BigDecimal("500000"));
        account1.setStatus(Account.AccountStatus.ACTIVE);
        account1.setOpenedDate(LocalDate.of(2024, 1, 15));
        account1.setCreatedBy("TEST");
        accountRepository.save(account1);

        Account account2 = new Account();
        account2.setCustomer(customer2);
        account2.setProduct(savingsProduct);
        account2.setAccountNumber("A2000002");
        account2.setAccountName("Siti Nurhaliza - Savings");
        account2.setBalance(new BigDecimal("750000"));
        account2.setStatus(Account.AccountStatus.ACTIVE);
        account2.setOpenedDate(LocalDate.of(2024, 1, 20));
        account2.setCreatedBy("TEST");
        accountRepository.save(account2);

        Account account3 = new Account();
        account3.setCustomer(customer3);
        account3.setProduct(checkingProduct);
        account3.setAccountNumber("A2000003");
        account3.setAccountName("Budi Santoso - Checking");
        account3.setBalance(new BigDecimal("1200000"));
        account3.setStatus(Account.AccountStatus.ACTIVE);
        account3.setOpenedDate(LocalDate.of(2024, 2, 1));
        account3.setCreatedBy("TEST");
        accountRepository.save(account3);

        Account account4 = new Account();
        account4.setCustomer(customer1);
        account4.setProduct(checkingProduct);
        account4.setAccountNumber("A2000004");
        account4.setAccountName("Ahmad Suharto - Checking");
        account4.setBalance(new BigDecimal("300000"));
        account4.setStatus(Account.AccountStatus.ACTIVE);
        account4.setOpenedDate(LocalDate.of(2024, 2, 10));
        account4.setCreatedBy("TEST");
        accountRepository.save(account4);

        accountMap.put("A2000001", account1);
        accountMap.put("A2000002", account2);
        accountMap.put("A2000003", account3);
        accountMap.put("A2000004", account4);
        
        entityManager.flush();
    }

    private void saveTestTransactions() {
        Account account = accountMap.get("A2000001");

        // Initial deposit
        Transaction deposit1 = new Transaction();
        deposit1.setAccount(account);
        deposit1.setTransactionNumber("T3000001");
        deposit1.setTransactionType(Transaction.TransactionType.DEPOSIT);
        deposit1.setAmount(new BigDecimal("500000"));
        deposit1.setCurrency("IDR");
        deposit1.setBalanceBefore(BigDecimal.ZERO);
        deposit1.setBalanceAfter(new BigDecimal("500000"));
        deposit1.setDescription("Initial deposit");
        deposit1.setReferenceNumber("REF001");
        deposit1.setChannel(Transaction.TransactionChannel.TELLER);
        deposit1.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        deposit1.setProcessedDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        deposit1.setCreatedBy("TEST");

        // Withdrawal
        Transaction withdrawal1 = new Transaction();
        withdrawal1.setAccount(account);
        withdrawal1.setTransactionNumber("T3000002");
        withdrawal1.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        withdrawal1.setAmount(new BigDecimal("50000"));
        withdrawal1.setCurrency("IDR");
        withdrawal1.setBalanceBefore(new BigDecimal("500000"));
        withdrawal1.setBalanceAfter(new BigDecimal("450000"));
        withdrawal1.setDescription("ATM withdrawal");
        withdrawal1.setReferenceNumber("REF002");
        withdrawal1.setChannel(Transaction.TransactionChannel.ATM);
        withdrawal1.setTransactionDate(LocalDateTime.of(2024, 1, 25, 16, 45));
        withdrawal1.setProcessedDate(LocalDateTime.of(2024, 1, 25, 16, 45));
        withdrawal1.setCreatedBy("TEST");

        // Another deposit
        Transaction deposit2 = new Transaction();
        deposit2.setAccount(account);
        deposit2.setTransactionNumber("T3000003");
        deposit2.setTransactionType(Transaction.TransactionType.DEPOSIT);
        deposit2.setAmount(new BigDecimal("100000"));
        deposit2.setCurrency("IDR");
        deposit2.setBalanceBefore(new BigDecimal("450000"));
        deposit2.setBalanceAfter(new BigDecimal("550000"));
        deposit2.setDescription("Salary deposit");
        deposit2.setReferenceNumber("REF003");
        deposit2.setChannel(Transaction.TransactionChannel.ONLINE);
        deposit2.setTransactionDate(LocalDateTime.of(2024, 2, 5, 8, 30));
        deposit2.setProcessedDate(LocalDateTime.of(2024, 2, 5, 8, 30));
        deposit2.setCreatedBy("TEST");

        transactionRepository.save(deposit1);
        transactionRepository.save(withdrawal1);
        transactionRepository.save(deposit2);
        entityManager.flush();
    }
}