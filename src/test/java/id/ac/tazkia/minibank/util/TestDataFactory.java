package id.ac.tazkia.minibank.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import id.ac.tazkia.minibank.entity.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe test data factory for creating test entities across parallel tests.
 * Provides consistent, isolated test data generation to prevent conflicts.
 */
@Slf4j
public class TestDataFactory {
    
    // Thread-safe counters for unique identifiers
    private static final AtomicLong customerCounter = new AtomicLong(1000);
    private static final AtomicLong accountCounter = new AtomicLong(2000);
    private static final AtomicLong transactionCounter = new AtomicLong(3000);
    private static final AtomicLong userCounter = new AtomicLong(4000);
    
    // Thread-local storage for test-specific data
    private static final ThreadLocal<String> currentTestContext = new ThreadLocal<>();
    
    /**
     * Set test context for thread-local data generation
     */
    public static void setTestContext(String testName) {
        currentTestContext.set(testName);
    }
    
    /**
     * Clear test context
     */
    public static void clearTestContext() {
        currentTestContext.remove();
    }
    
    /**
     * Get current test context
     */
    public static String getCurrentTestContext() {
        String context = currentTestContext.get();
        return context != null ? context : "default";
    }
    
    // Personal Customer Factory Methods
    
    /**
     * Create a valid personal customer with unique data
     */
    public static PersonalCustomer createPersonalCustomer() {
        long id = customerCounter.getAndIncrement();
        String context = getCurrentTestContext();
        
        PersonalCustomer customer = new PersonalCustomer();
        customer.setId(UUID.randomUUID());
        customer.setCustomerNumber(String.format("CUST%08d", id));
        customer.setFirstName(String.format("TestFirst%d", id));
        customer.setLastName(String.format("TestLast%d_%s", id, context));
        customer.setIdentityNumber(generateNationalId(id));
        customer.setIdentityType(Customer.IdentityType.KTP);
        customer.setPhoneNumber(generatePhoneNumber(id));
        customer.setEmail(String.format("customer%d.%s@test.minibank.com", id, sanitizeContext(context)));
        customer.setAddress(String.format("Test Address %d, Test City", id));
        customer.setDateOfBirth(LocalDate.now().minusYears(25 + (id % 40))); // Age 25-65
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customer.setCreatedBy("test-system");
        
        return customer;
    }
    
    /**
     * Create personal customer with specific attributes
     */
    public static PersonalCustomer createPersonalCustomer(String name, String nationalId) {
        PersonalCustomer customer = createPersonalCustomer();
        String[] nameParts = name.split(" ", 2);
        customer.setFirstName(nameParts[0]);
        customer.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        customer.setIdentityNumber(nationalId);
        return customer;
    }
    
    // Corporate Customer Factory Methods
    
    /**
     * Create a valid corporate customer with unique data
     */
    public static CorporateCustomer createCorporateCustomer() {
        long id = customerCounter.getAndIncrement();
        String context = getCurrentTestContext();
        
        CorporateCustomer customer = new CorporateCustomer();
        customer.setId(UUID.randomUUID());
        customer.setCustomerNumber(String.format("CORP%08d", id));
        customer.setTaxIdentificationNumber(generateTaxId(id));
        customer.setCompanyRegistrationNumber(String.format("REG%08d", id));
        customer.setPhoneNumber(generatePhoneNumber(id));
        customer.setEmail(String.format("corp%d.%s@test.minibank.com", id, sanitizeContext(context)));
        customer.setAddress(String.format("Test Corporate Address %d, Business District", id));
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customer.setCreatedBy("test-system");
        
        // Corporate-specific fields
        customer.setCompanyName(String.format("Test Corporation %d (%s)", id, context));
        customer.setContactPersonName(String.format("Contact Person %d", id));
        customer.setContactPersonTitle("Director");
        
        return customer;
    }
    
    // Product Factory Methods
    
    /**
     * Create Islamic banking product
     */
    public static Product createTabunganWadiahProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setProductCode("TWD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        product.setProductName("Test Tabungan Wadiah");
        product.setProductType(Product.ProductType.TABUNGAN_WADIAH);
        product.setProductCategory("Savings");
        product.setDescription("Test Islamic savings account with Wadiah principle");
        product.setMinimumBalance(BigDecimal.valueOf(100000)); // 100K IDR
        product.setMinimumOpeningBalance(BigDecimal.valueOf(50000)); // 50K IDR
        product.setMaximumBalance(BigDecimal.valueOf(100000000)); // 100M IDR
        product.setIsActive(true);
        product.setCreatedBy("test-system");
        
        return product;
    }
    
    /**
     * Create Mudharabah product with profit sharing
     */
    public static Product createTabunganMudharabahProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setProductCode("TMD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        product.setProductName("Test Tabungan Mudharabah");
        product.setProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        product.setProductCategory("Savings");
        product.setDescription("Test Islamic savings with profit sharing");
        product.setMinimumBalance(BigDecimal.valueOf(500000)); // 500K IDR
        product.setMinimumOpeningBalance(BigDecimal.valueOf(100000)); // 100K IDR
        product.setMaximumBalance(BigDecimal.valueOf(500000000)); // 500M IDR
        product.setNisbahCustomer(BigDecimal.valueOf(0.60)); // 60% customer
        product.setNisbahBank(BigDecimal.valueOf(0.40)); // 40% bank
        product.setIsActive(true);
        product.setCreatedBy("test-system");
        
        return product;
    }
    
    // Account Factory Methods
    
    /**
     * Create account for customer and product
     */
    public static Account createAccount(Customer customer, Product product) {
        long id = accountCounter.getAndIncrement();
        String context = getCurrentTestContext();
        
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setAccountNumber(String.format("ACC%08d%s", id, sanitizeContext(context).substring(0, Math.min(3, sanitizeContext(context).length())).toUpperCase()));
        account.setCustomer(customer);
        account.setProduct(product);
        account.setBalance(product.getMinimumBalance());
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setOpenedDate(LocalDate.now().minusDays(id % 30));
        account.setAccountName(customer.getDisplayName() + " - " + product.getProductName());
        account.setCreatedBy("test-system");
        
        return account;
    }
    
    // Transaction Factory Methods
    
    /**
     * Create deposit transaction
     */
    public static Transaction createDepositTransaction(Account account, BigDecimal amount) {
        long id = transactionCounter.getAndIncrement();
        String context = getCurrentTestContext();
        
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setTransactionNumber(String.format("TXN%010d", id));
        transaction.setAccount(account);
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(account.getBalance());
        transaction.setBalanceAfter(account.getBalance().add(amount));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(String.format("Test deposit %d (%s)", id, context));
        transaction.setChannel(Transaction.TransactionChannel.TELLER);
        transaction.setCreatedBy("test-system");
        
        return transaction;
    }
    
    /**
     * Create withdrawal transaction
     */
    public static Transaction createWithdrawalTransaction(Account account, BigDecimal amount) {
        long id = transactionCounter.getAndIncrement();
        String context = getCurrentTestContext();
        
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setTransactionNumber(String.format("TXN%010d", id));
        transaction.setAccount(account);
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(account.getBalance());
        transaction.setBalanceAfter(account.getBalance().subtract(amount));
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(String.format("Test withdrawal %d (%s)", id, context));
        transaction.setChannel(Transaction.TransactionChannel.TELLER);
        transaction.setCreatedBy("test-system");
        
        return transaction;
    }
    
    // User and Authentication Factory Methods
    
    /**
     * Create test user with specific role
     */
    public static User createUser(String role) {
        long id = userCounter.getAndIncrement();
        String context = getCurrentTestContext();
        
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(String.format("testuser%d_%s", id, sanitizeContext(context)));
        user.setEmail(String.format("testuser%d.%s@test.minibank.com", id, sanitizeContext(context)));
        user.setFullName(String.format("Test User %d (%s)", id, context));
        user.setIsActive(true);
        user.setIsLocked(false);
        user.setCreatedBy("test-system");
        
        return user;
    }
    
    // Utility Methods
    
    /**
     * Generate unique national ID
     */
    private static String generateNationalId(long id) {
        return String.format("32%013d", id); // Indonesian NIK format
    }
    
    /**
     * Generate unique tax ID for corporations
     */
    private static String generateTaxId(long id) {
        return String.format("01.%03d.%03d.%d-123.000", 
                           (id % 999) + 1, 
                           ((id / 1000) % 999) + 1, 
                           ((id / 1000000) % 9) + 1);
    }
    
    /**
     * Generate unique phone number
     */
    private static String generatePhoneNumber(long id) {
        return String.format("+62812%08d", id % 100000000);
    }
    
    /**
     * Sanitize test context for use in identifiers
     */
    private static String sanitizeContext(String context) {
        return context.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
    
    /**
     * Generate random amount within range
     */
    public static BigDecimal randomAmount(long min, long max) {
        long randomLong = ThreadLocalRandom.current().nextLong(min, max + 1);
        return BigDecimal.valueOf(randomLong);
    }
    
    /**
     * Generate test data summary for debugging
     */
    public static String getTestDataSummary() {
        return String.format("TestDataFactory: Customers=%d, Accounts=%d, Transactions=%d, Users=%d, Context=%s",
                           customerCounter.get(),
                           accountCounter.get(), 
                           transactionCounter.get(),
                           userCounter.get(),
                           getCurrentTestContext());
    }
    
    /**
     * Reset all counters (useful for test isolation)
     */
    public static void resetCounters() {
        customerCounter.set(1000);
        accountCounter.set(2000);
        transactionCounter.set(3000);
        userCounter.set(4000);
        clearTestContext();
        log.debug("TestDataFactory counters reset");
    }
}