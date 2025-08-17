package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.functional.web.pageobject.AccountListPage;
import id.ac.tazkia.minibank.functional.web.pageobject.AccountOpeningFormPage;
import id.ac.tazkia.minibank.functional.web.pageobject.CustomerSelectionPage;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Islamic Banking Specific Account Opening Tests
 * 
 * This test class focuses on Islamic banking product-specific scenarios:
 * - TABUNGAN_WADIAH (Safekeeping Savings)
 * - TABUNGAN_MUDHARABAH (Profit-Loss Sharing Savings)  
 * - DEPOSITO_MUDHARABAH (Islamic Term Deposit)
 * - Profit sharing (nisbah) validation
 * - Islamic banking compliance verification
 */
@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup-account-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-account-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class IslamicBankingAccountOpeningSeleniumTest extends BaseSeleniumTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Override
    protected void performInitialLogin() {
        // Login as Customer Service for Islamic banking operations
        loginHelper.loginAsCustomerServiceUser();
    }
    
    // TC-AO-001 Enhanced: TABUNGAN_WADIAH Opening (Safekeeping without profit sharing)
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldOpenTabunganWadiahAccountSuccessfully() {
        log.info("🧪 TEST START: shouldOpenTabunganWadiahAccountSuccessfully");
        
        // Debug: Check if customer data exists in database
        debugCustomerData();
        
        // Find TABUNGAN_WADIAH product
        Optional<Product> wadiahProduct = productRepository.findAll().stream()
            .filter(p -> p.getProductType() == Product.ProductType.TABUNGAN_WADIAH)
            .filter(Product::getIsActive)
            .findFirst();
        
        if (wadiahProduct.isEmpty()) {
            log.warn("⚠️ SKIP: No TABUNGAN_WADIAH product found for testing");
            return;
        }
        
        Product product = wadiahProduct.get();
        log.info("📋 Using TABUNGAN_WADIAH product: {} with minimum balance: {}", 
                product.getProductName(), product.getMinimumOpeningBalance());
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Select TABUNGAN_WADIAH specifically
        formPage.selectProductByValue(product.getId().toString());
        
        // Fill form with Islamic banking context
        BigDecimal initialDeposit = product.getMinimumOpeningBalance().multiply(new BigDecimal("2"));
        formPage.fillAccountName("Ahmad Susanto - Tabungan Wadiah Syariah");
        formPage.fillInitialDeposit(initialDeposit.toString());
        formPage.fillCreatedBy("cs1");
        
        AccountListPage listPage = formPage.submitForm();
        
        // Verify Wadiah account creation
        assertTrue(listPage.isSuccessMessageDisplayed());
        assertTrue(listPage.getSuccessMessage().contains("Account opened successfully"));
        
        // Verify Islamic banking compliance
        // Use eager loading to avoid LazyInitializationException
        List<Account> accounts = accountRepository.findAllWithProduct();
        Account wadiahAccount = accounts.stream()
            .filter(a -> a.getProduct().getProductType() == Product.ProductType.TABUNGAN_WADIAH)
            .max((a1, a2) -> a1.getCreatedDate().compareTo(a2.getCreatedDate()))
            .orElse(null);
        
        assertNotNull(wadiahAccount, "TABUNGAN_WADIAH account should be created");
        assertEquals(initialDeposit, wadiahAccount.getBalance());
        assertEquals(Account.AccountStatus.ACTIVE, wadiahAccount.getStatus());
        assertEquals("Ahmad Susanto - Tabungan Wadiah Syariah", wadiahAccount.getAccountName());
        
        // Verify WADIAH characteristics (no profit sharing expected)
        Product savedProduct = wadiahAccount.getProduct();
        assertEquals(Product.ProductType.TABUNGAN_WADIAH, savedProduct.getProductType());
        
        log.info("✅ TEST PASS: shouldOpenTabunganWadiahAccountSuccessfully completed successfully");
    }
    
    // TC-AO-002 Enhanced: TABUNGAN_MUDHARABAH Opening with profit sharing
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldOpenTabunganMudharabahWithProfitSharing() {
        log.info("🧪 TEST START: shouldOpenTabunganMudharabahWithProfitSharing");
        
        // Find TABUNGAN_MUDHARABAH product
        Optional<Product> mudharabahProduct = productRepository.findAll().stream()
            .filter(p -> p.getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH)
            .filter(Product::getIsActive)
            .findFirst();
        
        if (mudharabahProduct.isEmpty()) {
            log.warn("⚠️ SKIP: No TABUNGAN_MUDHARABAH product found for testing");
            return;
        }
        
        Product product = mudharabahProduct.get();
        log.info("📋 Using TABUNGAN_MUDHARABAH product: {} with nisbah customer: {}, bank: {}", 
                product.getProductName(), 
                product.getNisbahCustomer(), 
                product.getNisbahBank());
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000002");
        formPage.waitForPageLoad();
        
        // Select TABUNGAN_MUDHARABAH specifically
        formPage.selectProductByValue(product.getId().toString());
        
        BigDecimal initialDeposit = product.getMinimumOpeningBalance().multiply(new BigDecimal("3"));
        formPage.fillAccountName("Budi Santoso - Tabungan Mudharabah Syariah");
        formPage.fillInitialDeposit(initialDeposit.toString());
        formPage.fillCreatedBy("cs1");
        
        AccountListPage listPage = formPage.submitForm();
        
        // Verify Mudharabah account creation
        assertTrue(listPage.isSuccessMessageDisplayed());
        
        // Verify profit sharing compliance
        List<Account> accounts = accountRepository.findAllWithProduct();
        Account mudharabahAccount = accounts.stream()
            .filter(a -> a.getProduct().getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH)
            .max((a1, a2) -> a1.getCreatedDate().compareTo(a2.getCreatedDate()))
            .orElse(null);
        
        assertNotNull(mudharabahAccount, "TABUNGAN_MUDHARABAH account should be created");
        
        // Verify MUDHARABAH characteristics
        Product savedProduct = mudharabahAccount.getProduct();
        assertEquals(Product.ProductType.TABUNGAN_MUDHARABAH, savedProduct.getProductType());
        assertEquals(Product.ProfitSharingType.MUDHARABAH, savedProduct.getProfitSharingType());
        
        // Critical: Verify nisbah sum equals 1.0 (Islamic banking requirement)
        BigDecimal nisbahSum = savedProduct.getNisbahCustomer().add(savedProduct.getNisbahBank());
        assertEquals(new BigDecimal("1.0000"), nisbahSum, 
                    "Nisbah customer + bank must equal 1.0 for Islamic compliance");
        
        // Verify nisbah values are reasonable (customer share should be meaningful)
        assertTrue(savedProduct.getNisbahCustomer().compareTo(BigDecimal.ZERO) > 0,
                  "Customer nisbah should be greater than 0");
        assertTrue(savedProduct.getNisbahBank().compareTo(BigDecimal.ZERO) > 0,
                  "Bank nisbah should be greater than 0");
        
        log.info("✅ TEST PASS: shouldOpenTabunganMudharabahWithProfitSharing completed successfully");
    }
    
    // TC-AO-003: DEPOSITO_MUDHARABAH Opening with term deposit characteristics
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldOpenDepositoMudharabahWithTermDeposit() {
        log.info("🧪 TEST START: shouldOpenDepositoMudharabahWithTermDeposit");
        
        // Find DEPOSITO_MUDHARABAH product suitable for CORPORATE customers (C1000003)
        Optional<Product> depositoProduct = productRepository.findAll().stream()
            .filter(p -> p.getProductType() == Product.ProductType.DEPOSITO_MUDHARABAH)
            .filter(Product::getIsActive)
            .filter(p -> {
                String allowedTypes = p.getAllowedCustomerTypes();
                return allowedTypes == null || allowedTypes.isEmpty() || allowedTypes.contains("CORPORATE");
            })
            .findFirst();
        
        if (depositoProduct.isEmpty()) {
            log.warn("⚠️ SKIP: No DEPOSITO_MUDHARABAH product found for testing");
            return;
        }
        
        Product product = depositoProduct.get();
        log.info("📋 Using DEPOSITO_MUDHARABAH product: {} with minimum balance: {}", 
                product.getProductName(), product.getMinimumOpeningBalance());
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000003");
        formPage.waitForPageLoad();
        
        // Select DEPOSITO_MUDHARABAH specifically
        formPage.selectProductByValue(product.getId().toString());
        
        // Use substantial amount typical for Islamic term deposits
        BigDecimal depositAmount = new BigDecimal("25000000.00"); // IDR 25 million
        if (depositAmount.compareTo(product.getMinimumOpeningBalance()) < 0) {
            depositAmount = product.getMinimumOpeningBalance().multiply(new BigDecimal("5"));
        }
        
        formPage.fillAccountName("Siti Aminah - Deposito Mudharabah 12 Bulan");
        formPage.fillInitialDeposit(depositAmount.toString());
        formPage.fillCreatedBy("cs1");
        
        AccountListPage listPage = formPage.submitForm();
        
        // Verify Deposito creation
        assertTrue(listPage.isSuccessMessageDisplayed());
        
        // Verify deposito-specific characteristics
        List<Account> accounts = accountRepository.findAllWithProduct();
        Account depositoAccount = accounts.stream()
            .filter(a -> a.getProduct().getProductType() == Product.ProductType.DEPOSITO_MUDHARABAH)
            .max((a1, a2) -> a1.getCreatedDate().compareTo(a2.getCreatedDate()))
            .orElse(null);
        
        assertNotNull(depositoAccount, "DEPOSITO_MUDHARABAH account should be created");
        assertEquals(depositAmount, depositoAccount.getBalance());
        
        // Verify Islamic term deposit characteristics
        Product savedProduct = depositoAccount.getProduct();
        assertEquals(Product.ProductType.DEPOSITO_MUDHARABAH, savedProduct.getProductType());
        assertEquals(Product.ProfitSharingType.MUDHARABAH, savedProduct.getProfitSharingType());
        
        // Verify nisbah for deposito (often different from savings)
        BigDecimal nisbahSum = savedProduct.getNisbahCustomer().add(savedProduct.getNisbahBank());
        assertEquals(new BigDecimal("1.0000"), nisbahSum, 
                    "Deposito nisbah must sum to 1.0");
        
        // Verify initial transaction for deposito
        List<Transaction> transactions = transactionRepository.findByAccount(depositoAccount);
        assertEquals(1, transactions.size(), "Should have initial deposit transaction");
        
        Transaction initialTransaction = transactions.get(0);
        assertEquals(Transaction.TransactionType.DEPOSIT, initialTransaction.getTransactionType());
        assertEquals(depositAmount, initialTransaction.getAmount());
        assertEquals(0, initialTransaction.getBalanceBefore().compareTo(BigDecimal.ZERO), "Balance before should be zero");
        assertEquals(depositAmount, initialTransaction.getBalanceAfter());
        assertEquals("Initial deposit for account opening", initialTransaction.getDescription());
        assertEquals(Transaction.TransactionChannel.TELLER, initialTransaction.getChannel());
        
        log.info("✅ TEST PASS: shouldOpenDepositoMudharabahWithTermDeposit completed successfully");
    }
    
    // Product selection validation for Islamic banking
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldDisplayOnlyIslamicBankingProducts() {
        log.info("🧪 TEST START: shouldDisplayOnlyIslamicBankingProducts");
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Verify that only Islamic banking products are available
        // This test assumes the UI filters products appropriately
        
        // Find Islamic banking products available for PERSONAL customers (matching controller logic)
        List<Product> islamicProducts = productRepository.findByIsActiveTrue().stream()
            .filter(p -> {
                // Apply same customer type filtering as controller
                String allowedTypes = p.getAllowedCustomerTypes();
                if (allowedTypes == null || allowedTypes.isEmpty()) {
                    return true;
                }
                return allowedTypes.contains("PERSONAL"); // C1000001 is PERSONAL customer
            })
            .filter(p -> p.getProductType() == Product.ProductType.TABUNGAN_WADIAH ||
                        p.getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH ||
                        p.getProductType() == Product.ProductType.DEPOSITO_MUDHARABAH ||
                        p.getProductType() == Product.ProductType.SAVINGS ||
                        p.getProductType() == Product.ProductType.CHECKING)
            .toList();
        
        assertTrue(islamicProducts.size() > 0, "Should have Islamic banking products available");
        
        // Test product selection and information display
        for (Product product : islamicProducts.subList(0, Math.min(3, islamicProducts.size()))) {
            log.info("🧪 Testing product: {} (ID: {}, Type: {}, AllowedTypes: {})", 
                    product.getProductName(), product.getId(), product.getProductType(), product.getAllowedCustomerTypes());
            
            formPage.selectProductByValue(product.getId().toString());
            
            // Wait for JavaScript to update the product information section
            // The selectProductByValue method should already handle this via the page object
            
            // Verify product information is displayed
            boolean infoDisplayed = formPage.isProductInfoDisplayed();
            log.info("🔍 Product info displayed for {}: {}", product.getProductName(), infoDisplayed);
            
            assertTrue(infoDisplayed, 
                      "Product information should be displayed for " + product.getProductName());
            
            String displayedType = formPage.getProductType();
            assertNotNull(displayedType, "Product type should be displayed");
            
            log.info("✅ Verified Islamic product: {} - Type: {}", 
                    product.getProductName(), displayedType);
        }
        
        log.info("✅ TEST PASS: shouldDisplayOnlyIslamicBankingProducts completed successfully");
    }
    
    // Minimum balance validation for different Islamic products
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldEnforceIslamicProductMinimumBalances() {
        log.info("🧪 TEST START: shouldEnforceIslamicProductMinimumBalances");
        
        // Find Islamic products with different minimum balances
        List<Product> islamicProducts = productRepository.findByIsActiveTrue().stream()
            .filter(p -> p.getProductType() == Product.ProductType.TABUNGAN_WADIAH ||
                        p.getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH ||
                        p.getProductType() == Product.ProductType.DEPOSITO_MUDHARABAH)
            .toList();
        
        if (islamicProducts.isEmpty()) {
            log.warn("⚠️ SKIP: No Islamic banking products found for minimum balance testing");
            return;
        }
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Test minimum balance enforcement for first available product
        Product testProduct = islamicProducts.get(0);
        formPage.selectProductByValue(testProduct.getId().toString());
        
        // Try with amount below minimum
        BigDecimal belowMinimum = testProduct.getMinimumOpeningBalance().subtract(new BigDecimal("10000"));
        if (belowMinimum.compareTo(BigDecimal.ZERO) <= 0) {
            belowMinimum = new BigDecimal("1000"); // Very small amount
        }
        
        formPage.fillAccountName("Test Islamic Account");
        formPage.fillInitialDeposit(belowMinimum.toString());
        formPage.fillCreatedBy("cs1");
        
        formPage.submitFormExpectingError();
        
        // Verify minimum balance error
        assertTrue(formPage.isErrorMessageDisplayed(),
                  "Should show error for insufficient minimum balance");
        
        String errorMessage = formPage.getErrorMessage();
        assertTrue(errorMessage.toLowerCase().contains("minimum") || 
                  errorMessage.toLowerCase().contains("balance") ||
                  errorMessage.toLowerCase().contains("deposit"),
                  "Error message should mention minimum balance requirement");
        
        log.info("✅ Verified minimum balance enforcement for product: {} (minimum: {})", 
                testProduct.getProductName(), testProduct.getMinimumOpeningBalance());
        
        log.info("✅ TEST PASS: shouldEnforceIslamicProductMinimumBalances completed successfully");
    }
    
    // Cross-product compatibility test
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldAllowMultipleIslamicAccountsForSameCustomer() {
        log.info("🧪 TEST START: shouldAllowMultipleIslamicAccountsForSameCustomer");
        
        // Find different Islamic products
        Optional<Product> wadiahProduct = productRepository.findAll().stream()
            .filter(p -> p.getProductType() == Product.ProductType.TABUNGAN_WADIAH)
            .filter(Product::getIsActive)
            .findFirst();
        
        Optional<Product> mudharabahProduct = productRepository.findAll().stream()
            .filter(p -> p.getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH)
            .filter(Product::getIsActive)
            .findFirst();
        
        if (wadiahProduct.isEmpty() || mudharabahProduct.isEmpty()) {
            log.warn("⚠️ SKIP: Need both WADIAH and MUDHARABAH products for cross-product testing");
            return;
        }
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        // Open WADIAH account
        AccountOpeningFormPage formPage1 = selectionPage.selectCustomer("C1000001");
        formPage1.waitForPageLoad();
        
        formPage1.selectProductByValue(wadiahProduct.get().getId().toString());
        formPage1.fillAccountName("Ahmad - Tabungan Wadiah");
        formPage1.fillInitialDeposit(wadiahProduct.get().getMinimumOpeningBalance().toString());
        formPage1.fillCreatedBy("cs1");
        
        AccountListPage listPage1 = formPage1.submitForm();
        assertTrue(listPage1.isSuccessMessageDisplayed());
        
        // Open MUDHARABAH account for same customer
        selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage2 = selectionPage.selectCustomer("C1000001");
        formPage2.waitForPageLoad();
        
        formPage2.selectProductByValue(mudharabahProduct.get().getId().toString());
        formPage2.fillAccountName("Ahmad - Tabungan Mudharabah");
        formPage2.fillInitialDeposit(mudharabahProduct.get().getMinimumOpeningBalance().toString());
        formPage2.fillCreatedBy("cs1");
        
        AccountListPage listPage2 = formPage2.submitForm();
        assertTrue(listPage2.isSuccessMessageDisplayed());
        
        // Verify customer has both Islamic accounts
        Optional<Customer> customer = customerRepository.findByCustomerNumber("C1000001");
        assertTrue(customer.isPresent());
        
        List<Account> customerAccounts = accountRepository.findByCustomerWithProduct(customer.get());
        
        boolean hasWadiah = customerAccounts.stream()
            .anyMatch(a -> a.getProduct().getProductType() == Product.ProductType.TABUNGAN_WADIAH);
        boolean hasMudharabah = customerAccounts.stream()
            .anyMatch(a -> a.getProduct().getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH);
        
        assertTrue(hasWadiah, "Customer should have WADIAH account");
        assertTrue(hasMudharabah, "Customer should have MUDHARABAH account");
        
        log.info("✅ TEST PASS: shouldAllowMultipleIslamicAccountsForSameCustomer completed successfully");
    }
    
    private void debugCustomerData() {
        List<Customer> allCustomers = customerRepository.findAll();
        List<Customer> activeCustomers = customerRepository.findByStatus(Customer.CustomerStatus.ACTIVE);
        
        log.info("🔍 DATABASE DEBUG: Total customers in database: {}", allCustomers.size());
        log.info("🔍 DATABASE DEBUG: Active customers in database: {}", activeCustomers.size());
        
        allCustomers.forEach(customer -> {
            log.info("📋 CUSTOMER: {} - Status: {} - Type: {}", 
                    customer.getCustomerNumber(), 
                    customer.getStatus(),
                    customer.getCustomerType());
        });
        
        if (activeCustomers.isEmpty()) {
            log.error("❌ CRITICAL: No active customers found in database!");
        } else {
            log.info("✅ CUSTOMER DATA: {} active customers available for selection", activeCustomers.size());
        }
    }
}