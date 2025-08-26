package id.ac.tazkia.minibank.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import id.ac.tazkia.minibank.config.BaseIntegrationTest;
import id.ac.tazkia.minibank.config.TestDataFactory;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA integration test demonstrating schema-per-thread isolation using Account entity
 * with foreign key relationships to Branch, Customer, and Product entities.
 * Tests use migration data for relationships and TestDataFactory for account data.
 */
@Slf4j
@Execution(ExecutionMode.CONCURRENT)
class SchemaPerThreadJpaTest extends BaseIntegrationTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // Test data for lifecycle methods
    private Account lifecycleAccount;
    private String lifecycleAccountNumber;
    private String threadMarker;
    
    // Migration data references (loaded once per test)
    private Branch migrationBranch;
    private Customer migrationCustomer;  
    private Product migrationProduct;
    
    @BeforeEach
    void setUpTestData() {
        // Create unique thread marker for this test execution
        threadMarker = TestDataFactory.generateThreadMarker();
        
        // Load migration data for foreign key relationships
        loadMigrationDataReferences();
        
        // Set up lifecycle account using migration data relationships
        lifecycleAccountNumber = TestDataFactory.generateAccountNumber();
        String lifecycleAccountName = TestDataFactory.generateIndonesianPersonName() + " - Account";
        
        lifecycleAccount = new Account();
        lifecycleAccount.setBranch(migrationBranch);
        lifecycleAccount.setCustomer(migrationCustomer);
        lifecycleAccount.setProduct(migrationProduct);
        lifecycleAccount.setAccountNumber(lifecycleAccountNumber);
        lifecycleAccount.setAccountName(lifecycleAccountName);
        lifecycleAccount.setBalance(TestDataFactory.generateAccountBalance());
        lifecycleAccount.setStatus(Account.AccountStatus.ACTIVE);
        lifecycleAccount.setOpenedDate(LocalDate.now());
        lifecycleAccount.setCreatedBy(threadMarker);
        
        // Save lifecycle account for update/delete tests
        lifecycleAccount = accountRepository.save(lifecycleAccount);
        
        log.info("@BeforeEach: Set up lifecycle account {} for schema: {} on thread: {} with marker: {}", 
                lifecycleAccount.getAccountNumber(), schemaName, Thread.currentThread().getName(), threadMarker);
    }
    
    /**
     * Loads migration data references once per test execution
     */
    private void loadMigrationDataReferences() {
        // Load first branch from migration data (HO001 - Kantor Pusat Jakarta)
        migrationBranch = branchRepository.findByBranchCode("HO001")
                .orElseThrow(() -> new IllegalStateException("Migration branch HO001 not found"));
        
        // Load first customer from migration data (C1000001 - Ahmad Suharto) 
        migrationCustomer = customerRepository.findByCustomerNumber("C1000001")
                .orElseThrow(() -> new IllegalStateException("Migration customer C1000001 not found"));
        
        // Load first product from migration data (TAB001 - Tabungan Wadiah Basic)
        migrationProduct = productRepository.findByProductCode("TAB001")
                .orElseThrow(() -> new IllegalStateException("Migration product TAB001 not found"));
        
        log.debug("Loaded migration data - Branch: {}, Customer: {}, Product: {}", 
                migrationBranch.getBranchCode(), migrationCustomer.getCustomerNumber(), migrationProduct.getProductCode());
    }
    
    @AfterEach
    void cleanUpTestData() {
        // Clean up test accounts for this specific thread (preserve migration data)
        try {
            List<Account> testAccounts = accountRepository.findAll()
                    .stream()
                    .filter(account -> threadMarker.equals(account.getCreatedBy()))
                    .toList();
            
            if (!testAccounts.isEmpty()) {
                accountRepository.deleteAll(testAccounts);
                log.info("@AfterEach: Cleaned up {} test accounts for thread marker {} from schema: {} (preserved migration data)", 
                        testAccounts.size(), threadMarker, schemaName);
            } else {
                log.info("@AfterEach: No test accounts cleanup needed for thread marker {} in schema: {}", 
                        threadMarker, schemaName);
            }
        } catch (Exception e) {
            log.warn("Failed to clean up test accounts for thread marker {} in schema {}: {}", 
                    threadMarker, schemaName, e.getMessage());
        }
    }
    
    @Test
    void shouldCreateAccountSuccessfully() {
        // Given
        String accountNumber = TestDataFactory.generateAccountNumber();
        String accountName = TestDataFactory.generateIndonesianPersonName() + " - Savings";
        BigDecimal initialBalance = TestDataFactory.generateDepositAmount();
        
        Account newAccount = new Account();
        newAccount.setBranch(migrationBranch);
        newAccount.setCustomer(migrationCustomer);
        newAccount.setProduct(migrationProduct);
        newAccount.setAccountNumber(accountNumber);
        newAccount.setAccountName(accountName);
        newAccount.setBalance(initialBalance);
        newAccount.setStatus(Account.AccountStatus.ACTIVE);
        newAccount.setOpenedDate(LocalDate.now());
        newAccount.setCreatedBy(threadMarker);
        
        // When - Persist via repository
        Account savedAccount = accountRepository.save(newAccount);
        
        // Then - Verify via JdbcTemplate
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, accountNumber);
        
        assertNotNull(result);
        assertEquals(accountNumber, result.get("account_number"));
        assertEquals(accountName, result.get("account_name"));
        assertTrue(initialBalance.compareTo((BigDecimal) result.get("balance")) == 0);
        assertEquals("ACTIVE", result.get("status"));
        // Note: created_by is auto-populated by JPA auditing, not set by test code
        
        // Verify foreign key relationships via JdbcTemplate
        assertEquals(migrationBranch.getId().toString(), result.get("id_branches").toString());
        assertEquals(migrationCustomer.getId().toString(), result.get("id_customers").toString());
        assertEquals(migrationProduct.getId().toString(), result.get("id_products").toString());
        
        // Verify audit fields via JdbcTemplate
        assertNotNull(result.get("created_date"));
        assertEquals(LocalDate.now(), ((java.sql.Date) result.get("opened_date")).toLocalDate());
        
        log.info("shouldCreateAccountSuccessfully: Created account {} with balance {} in schema {} - verified via JdbcTemplate", 
                accountNumber, initialBalance, schemaName);
    }
    
    @Test
    void shouldReadAccountByAccountNumber() {
        // Given - Migration account from lifecycle setup
        String accountNumber = lifecycleAccount.getAccountNumber();
        
        // When
        Optional<Account> foundAccount = accountRepository.findByAccountNumber(accountNumber);
        
        // Then
        assertTrue(foundAccount.isPresent());
        Account account = foundAccount.get();
        
        assertEquals(lifecycleAccount.getId(), account.getId());
        assertEquals(accountNumber, account.getAccountNumber());
        assertEquals(lifecycleAccount.getAccountName(), account.getAccountName());
        assertTrue(lifecycleAccount.getBalance().compareTo(account.getBalance()) == 0);
        assertEquals(Account.AccountStatus.ACTIVE, account.getStatus());
        // Note: createdBy is auto-populated by JPA auditing, not set by test code
        
        // Verify lazy loading of relationships
        assertNotNull(account.getBranch());
        assertNotNull(account.getCustomer());
        assertNotNull(account.getProduct());
        
        log.info("shouldReadAccountByAccountNumber: Successfully read account {} from schema {}", 
                accountNumber, schemaName);
    }
    
    @Test
    void shouldReadAccountsByCustomer() {
        // Given - Use migration customer from lifecycle setup
        Customer customer = migrationCustomer;
        
        // When - Find accounts by customer (should include lifecycle account)
        List<Account> customerAccounts = accountRepository.findByCustomer(customer);
        
        // Then
        assertNotNull(customerAccounts);
        assertFalse(customerAccounts.isEmpty());
        
        // Verify our test account is included
        boolean lifecycleAccountFound = customerAccounts.stream()
                .anyMatch(account -> lifecycleAccount.getId().equals(account.getId()));
        assertTrue(lifecycleAccountFound, "Lifecycle account should be found for customer");
        
        // Verify all accounts belong to the correct customer
        customerAccounts.forEach(account -> {
            assertNotNull(account.getCustomer());
            assertEquals(customer.getId(), account.getCustomer().getId());
        });
        
        log.info("shouldReadAccountsByCustomer: Found {} accounts for customer {} in schema {}", 
                customerAccounts.size(), customer.getCustomerNumber(), schemaName);
    }
    
    @Test
    void shouldUpdateAccountSuccessfully() {
        // Given - Load account via repository
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(lifecycleAccountNumber);
        assertTrue(accountOpt.isPresent());
        
        Account accountToUpdate = accountOpt.get();
        BigDecimal originalBalance = accountToUpdate.getBalance();
        
        // When - Modify and save via repository
        String newAccountName = TestDataFactory.generateIndonesianPersonName() + " - Updated Account";
        BigDecimal depositAmount = TestDataFactory.generateDepositAmount();
        
        accountToUpdate.setAccountName(newAccountName);
        accountToUpdate.deposit(depositAmount); // Use business method
        // Note: updatedBy is auto-populated by JPA auditing
        
        Account savedAccount = accountRepository.save(accountToUpdate);
        
        // Then - Verify via JdbcTemplate
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, lifecycleAccountNumber);
        
        assertNotNull(result);
        assertEquals(newAccountName, result.get("account_name"));
        assertTrue(originalBalance.add(depositAmount).compareTo((BigDecimal) result.get("balance")) == 0);
        // Note: updated_by is auto-populated by JPA auditing, not set by test code
        assertNotNull(result.get("updated_date"));
        
        // Verify relationships are preserved via JdbcTemplate
        assertEquals(migrationBranch.getId().toString(), result.get("id_branches").toString());
        assertEquals(migrationCustomer.getId().toString(), result.get("id_customers").toString());
        assertEquals(migrationProduct.getId().toString(), result.get("id_products").toString());
        
        log.info("shouldUpdateAccountSuccessfully: Successfully updated account {} in schema {} - verified via JdbcTemplate", 
                lifecycleAccountNumber, schemaName);
    }
    
    @Test
    void shouldDeleteAccountSuccessfully() {
        // Given - Load account via repository
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(lifecycleAccountNumber);
        assertTrue(accountOpt.isPresent());
        
        Account accountToDelete = accountOpt.get();
        
        // Verify account exists via JdbcTemplate before deletion
        String countSql = "SELECT COUNT(*) FROM accounts WHERE account_number = ?";
        Integer countBefore = jdbcTemplate.queryForObject(countSql, Integer.class, lifecycleAccountNumber);
        assertEquals(1, countBefore);
        
        // When - Delete via repository
        accountRepository.delete(accountToDelete);
        
        // Then - Verify deletion via JdbcTemplate
        Integer countAfter = jdbcTemplate.queryForObject(countSql, Integer.class, lifecycleAccountNumber);
        assertEquals(0, countAfter);
        
        // Also verify via repository
        Optional<Account> deletedAccount = accountRepository.findByAccountNumber(lifecycleAccountNumber);
        assertFalse(deletedAccount.isPresent());
        
        log.info("shouldDeleteAccountSuccessfully: Successfully deleted account {} from schema {} - verified via JdbcTemplate", 
                lifecycleAccountNumber, schemaName);
    }
    
    @Test
    void shouldFindAccountsFromMigrationData() {
        // Given - Verify migration data is accessible
        
        // When - Query for all accounts (should find at least lifecycle account)
        List<Account> allAccounts = accountRepository.findAll();
        
        // Then 
        assertNotNull(allAccounts);
        assertFalse(allAccounts.isEmpty(), "Should find at least the lifecycle account");
        
        // Verify our lifecycle account is present
        boolean lifecycleAccountFound = allAccounts.stream()
                .anyMatch(account -> lifecycleAccount.getId().equals(account.getId()));
        assertTrue(lifecycleAccountFound, "Lifecycle account should be found");
        
        log.info("shouldFindAccountsFromMigrationData: Found {} accounts including lifecycle account in schema {}", 
                allAccounts.size(), schemaName);
    }
    
    @Test
    void shouldVerifySchemaIsolation() {
        // This test verifies that each test is running in its own schema with JPA entities
        
        // Given
        String testAccountNumber = TestDataFactory.generateAccountNumber();
        
        // When - Create simple account
        Account isolationTestAccount = new Account();
        isolationTestAccount.setBranch(migrationBranch);
        isolationTestAccount.setCustomer(migrationCustomer);
        isolationTestAccount.setProduct(migrationProduct);
        isolationTestAccount.setAccountNumber(testAccountNumber);
        isolationTestAccount.setAccountName("Schema Isolation Test");
        isolationTestAccount.setBalance(TestDataFactory.generateAccountBalance());
        isolationTestAccount.setCreatedBy(threadMarker);
        
        Account savedAccount = accountRepository.save(isolationTestAccount);
        
        // Then - Verify account exists
        Optional<Account> foundAccount = accountRepository.findByAccountNumber(testAccountNumber);
        assertTrue(foundAccount.isPresent());
        assertEquals(testAccountNumber, foundAccount.get().getAccountNumber());
        
        // Log the schema name for verification during parallel execution
        log.info("Test running in schema: {} with thread: {} and account: {}", 
                schemaName, Thread.currentThread().getName(), testAccountNumber);
        
        assertNotNull(schemaName);
        assertTrue(schemaName.startsWith("test_"));
    }
}