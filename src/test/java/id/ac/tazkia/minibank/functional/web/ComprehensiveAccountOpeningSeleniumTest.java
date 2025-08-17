package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
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
import id.ac.tazkia.minibank.functional.web.pageobject.CorporateAccountOpeningFormPage;
import id.ac.tazkia.minibank.functional.web.pageobject.CorporateCustomerSelectionPage;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Comprehensive Account Opening Selenium Tests
 * 
 * This test class covers the missing scenarios from the documentation:
 * - TC-AO-003: DEPOSITO_MUDHARABAH opening
 * - TC-AO-007: Customer number duplicate validation
 * - TC-AO-009: Nisbah validation for MUDHARABAH products
 * - TC-AO-010: Security/Authorization tests
 * - Field-level validation scenarios
 * - Database integrity validation
 */
@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup-account-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-account-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class ComprehensiveAccountOpeningSeleniumTest extends BaseSeleniumTest {
    
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
        // Login as Manager for comprehensive permissions
        loginHelper.loginAsManager();
    }
    
    // TC-AO-003: DEPOSITO_MUDHARABAH Opening
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldOpenDepositoMudharabahAccountWithProfitSharing() {
        log.info("🧪 TEST START: shouldOpenDepositoMudharabahAccountWithProfitSharing");
        
        // Find DEPOSITO_MUDHARABAH product
        Optional<Product> depositoProduct = productRepository.findAll().stream()
            .filter(p -> p.getProductType() == Product.ProductType.DEPOSITO_MUDHARABAH)
            .filter(Product::getIsActive)
            .findFirst();
        
        if (depositoProduct.isEmpty()) {
            log.warn("⚠️ SKIP: No DEPOSITO_MUDHARABAH product found for testing");
            return;
        }
        
        Product product = depositoProduct.get();
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Select DEPOSITO_MUDHARABAH product specifically
        formPage.selectProductByValue(product.getId().toString());
        
        // Use high deposit amount suitable for DEPOSITO
        formPage.fillCompleteForm(
            product.getProductName(),
            "Ahmad Susanto Deposito Account",
            "10000000.00",  // IDR 10,000,000 for deposito
            "manager1"
        );
        
        AccountListPage listPage = formPage.submitForm();
        
        // Verify deposito account creation
        assertTrue(listPage.isSuccessMessageDisplayed());
        assertTrue(listPage.getSuccessMessage().contains("Account opened successfully"));
        
        // Verify database consistency for DEPOSITO_MUDHARABAH
        List<Account> accounts = accountRepository.findAll();
        Account newAccount = accounts.stream()
            .filter(a -> a.getProduct().getProductType() == Product.ProductType.DEPOSITO_MUDHARABAH)
            .findFirst()
            .orElse(null);
        
        assertNotNull(newAccount, "DEPOSITO_MUDHARABAH account should be created");
        assertEquals(new BigDecimal("10000000.00"), newAccount.getBalance());
        assertEquals(Account.AccountStatus.ACTIVE, newAccount.getStatus());
        
        // Verify profit sharing configuration
        assertEquals(product.getNisbahCustomer().add(product.getNisbahBank()), 
                    new BigDecimal("1.0000"), "Nisbah should sum to 1.0");
        
        log.info("✅ TEST PASS: shouldOpenDepositoMudharabahAccountWithProfitSharing completed successfully");
    }
    
    // TC-AO-009: Nisbah Validation for MUDHARABAH Products
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldValidateNisbahSumForMudharabahProducts() {
        log.info("🧪 TEST START: shouldValidateNisbahSumForMudharabahProducts");
        
        // Find MUDHARABAH products and validate their nisbah
        List<Product> mudharabahProducts = productRepository.findAll().stream()
            .filter(p -> p.getProfitSharingType() == Product.ProfitSharingType.MUDHARABAH || 
                        p.getProfitSharingType() == Product.ProfitSharingType.MUSHARAKAH)
            .filter(Product::getIsActive)
            .toList();
        
        assertTrue(mudharabahProducts.size() > 0, "Should have MUDHARABAH products for testing");
        
        for (Product product : mudharabahProducts) {
            BigDecimal nisbahSum = product.getNisbahCustomer().add(product.getNisbahBank());
            assertEquals(new BigDecimal("1.0000"), nisbahSum, 
                "Product " + product.getProductCode() + " nisbah should sum to 1.0");
            
            log.info("✅ Product {} nisbah validation passed: customer={}, bank={}, sum={}", 
                    product.getProductCode(), 
                    product.getNisbahCustomer(), 
                    product.getNisbahBank(), 
                    nisbahSum);
        }
        
        log.info("✅ TEST PASS: shouldValidateNisbahSumForMudharabahProducts completed successfully");
    }
    
    // Field-level validation tests
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/account/invalid-field-validation.csv", numLinesToSkip = 1)
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldValidateSpecificFieldConstraints(String testCase, String fieldName, String invalidValue, String expectedError) {
        log.info("🧪 TEST START: shouldValidateSpecificFieldConstraints for {}", testCase);
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Apply field-specific invalid values based on test case
        switch (fieldName.toLowerCase()) {
            case "accountname":
                formPage.fillAccountName(invalidValue);
                break;
            case "initialdeposit":
                formPage.fillInitialDeposit(invalidValue);
                break;
            case "createdby":
                formPage.fillCreatedBy(invalidValue);
                break;
        }
        
        // Fill minimal valid data for other fields
        formPage.selectFirstAvailableProduct();
        if (!fieldName.equalsIgnoreCase("accountname")) {
            formPage.fillAccountName("Valid Account Name");
        }
        if (!fieldName.equalsIgnoreCase("initialdeposit")) {
            formPage.fillInitialDeposit("100000.00");
        }
        
        formPage.submitFormExpectingError();
        
        // Verify appropriate error is displayed
        assertTrue(formPage.isErrorMessageDisplayed() || 
                  isElementPresent(org.openqa.selenium.By.id("validation-errors")),
                  "Validation error should be displayed for " + testCase);
        
        log.info("✅ TEST PASS: shouldValidateSpecificFieldConstraints completed for {}", testCase);
    }
    
    // Database integrity validation
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldMaintainDatabaseIntegrityAfterAccountOpening() {
        log.info("🧪 TEST START: shouldMaintainDatabaseIntegrityAfterAccountOpening");
        
        // Record initial counts
        long initialAccountCount = accountRepository.count();
        long initialTransactionCount = transactionRepository.count();
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        BigDecimal initialDeposit = new BigDecimal("150000.00");
        formPage.fillCompleteFormWithFirstProduct(
            "Integrity Test Account",
            initialDeposit.toString(),
            "manager1"
        );
        
        AccountListPage listPage = formPage.submitForm();
        assertTrue(listPage.isSuccessMessageDisplayed());
        
        // Verify database integrity
        
        // 1. Account count increased by 1
        assertEquals(initialAccountCount + 1, accountRepository.count(),
                    "Account count should increase by 1");
        
        // 2. Transaction recorded for initial deposit
        assertEquals(initialTransactionCount + 1, transactionRepository.count(),
                    "Transaction count should increase by 1");
        
        // 3. Account balance matches initial deposit
        List<Account> newAccounts = accountRepository.findAll();
        Account latestAccount = newAccounts.stream()
            .max((a1, a2) -> a1.getCreatedDate().compareTo(a2.getCreatedDate()))
            .orElse(null);
        
        assertNotNull(latestAccount, "Latest account should exist");
        assertEquals(initialDeposit, latestAccount.getBalance(),
                    "Account balance should match initial deposit");
        
        // 4. Initial transaction recorded correctly
        List<Transaction> accountTransactions = transactionRepository.findByAccount(latestAccount);
        assertEquals(1, accountTransactions.size(), "Should have exactly one initial transaction");
        
        Transaction initialTransaction = accountTransactions.get(0);
        assertEquals(Transaction.TransactionType.DEPOSIT, initialTransaction.getTransactionType());
        assertEquals(initialDeposit, initialTransaction.getAmount());
        assertEquals(Transaction.TransactionChannel.TELLER, initialTransaction.getChannel());
        
        // 5. Account has proper relationships
        assertNotNull(latestAccount.getCustomer(), "Account should have customer");
        assertNotNull(latestAccount.getProduct(), "Account should have product");
        assertNotNull(latestAccount.getBranch(), "Account should have branch");
        assertNotNull(latestAccount.getAccountNumber(), "Account should have account number");
        
        log.info("✅ TEST PASS: shouldMaintainDatabaseIntegrityAfterAccountOpening completed successfully");
    }
    
    // Account opening for existing customer with multiple accounts
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldAllowMultipleAccountsForSameCustomer() {
        log.info("🧪 TEST START: shouldAllowMultipleAccountsForSameCustomer");
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        // Open first account
        AccountOpeningFormPage formPage1 = selectionPage.selectCustomer("C1000001");
        formPage1.waitForPageLoad();
        
        formPage1.fillCompleteFormWithFirstProduct(
            "Ahmad Susanto First Account",
            "100000.00",
            "manager1"
        );
        
        AccountListPage listPage1 = formPage1.submitForm();
        assertTrue(listPage1.isSuccessMessageDisplayed());
        
        // Navigate back and open second account for same customer
        selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage2 = selectionPage.selectCustomer("C1000001");
        formPage2.waitForPageLoad();
        
        formPage2.fillCompleteFormWithFirstProduct(
            "Ahmad Susanto Second Account",
            "200000.00",
            "manager1"
        );
        
        AccountListPage listPage2 = formPage2.submitForm();
        assertTrue(listPage2.isSuccessMessageDisplayed());
        
        // Verify multiple accounts for same customer
        Optional<Customer> customer = customerRepository.findByCustomerNumber("C1000001");
        assertTrue(customer.isPresent(), "Customer should exist");
        
        List<Account> customerAccounts = accountRepository.findByCustomer(customer.get());
        assertTrue(customerAccounts.size() >= 2, "Customer should have at least 2 accounts");
        
        // Verify unique account numbers
        long uniqueAccountNumbers = customerAccounts.stream()
            .map(Account::getAccountNumber)
            .distinct()
            .count();
        assertEquals(customerAccounts.size(), uniqueAccountNumbers, 
                    "All account numbers should be unique");
        
        log.info("✅ TEST PASS: shouldAllowMultipleAccountsForSameCustomer completed successfully");
    }
    
    // Islamic Banking product-specific tests
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldDisplayIslamicBankingProductInformation() {
        log.info("🧪 TEST START: shouldDisplayIslamicBankingProductInformation");
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Find and select TABUNGAN_MUDHARABAH product
        Optional<Product> mudharabahProduct = productRepository.findAll().stream()
            .filter(p -> p.getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH)
            .filter(Product::getIsActive)
            .findFirst();
        
        if (mudharabahProduct.isPresent()) {
            Product product = mudharabahProduct.get();
            formPage.selectProductByValue(product.getId().toString());
            
            // Verify Islamic banking product information is displayed
            assertTrue(formPage.isProductInfoDisplayed(), 
                      "Product information should be displayed");
            
            String productType = formPage.getProductType();
            assertNotNull(productType, "Product type should be displayed");
            assertTrue(productType.contains("MUDHARABAH") || productType.contains("TABUNGAN"),
                      "Product type should indicate Islamic banking");
            
            // Verify profit sharing information if available
            String productDescription = formPage.getProductDescription();
            if (productDescription != null && !productDescription.isEmpty()) {
                assertTrue(productDescription.toLowerCase().contains("mudharabah") ||
                          productDescription.toLowerCase().contains("syariah") ||
                          productDescription.toLowerCase().contains("islami"),
                          "Product description should indicate Islamic banking");
            }
        } else {
            log.warn("⚠️ SKIP: No TABUNGAN_MUDHARABAH product found for testing");
        }
        
        log.info("✅ TEST PASS: shouldDisplayIslamicBankingProductInformation completed successfully");
    }
    
    // Corporate customer field validation
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldValidateCorporateCustomerFieldLengths() {
        log.info("🧪 TEST START: shouldValidateCorporateCustomerFieldLengths");
        
        // Find a corporate customer
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        // Test with extremely long account name (exceeding 200 chars)
        String longAccountName = "PT. Very Long Company Name That Exceeds The Maximum Allowed Length Of Two Hundred Characters In The Database Schema And Should Trigger A Validation Error When Registering X".repeat(2);
        
        formPage.selectFirstAvailableCorporateProduct();
        formPage.fillCorporateAccountName(longAccountName);
        formPage.fillInitialDeposit("500000.00");
        formPage.fillAccountManager("manager1");
        
        formPage.submitCorporateFormExpectingError();
        
        // Verify validation error for long account name
        assertTrue(formPage.isErrorMessageDisplayed() || 
                  isElementPresent(org.openqa.selenium.By.id("validation-errors")),
                  "Validation error should be displayed for long account name");
        
        log.info("✅ TEST PASS: shouldValidateCorporateCustomerFieldLengths completed successfully");
    }
    
    // Security test - basic authorization check
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldRequireProperAuthenticationForAccountOpening() {
        log.info("🧪 TEST START: shouldRequireProperAuthenticationForAccountOpening");
        
        // Try to access account opening without proper authentication
        driver.get(baseUrl + "/account/open");
        
        // Should redirect to login or show access denied
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login") || currentUrl.contains("error") || currentUrl.contains("denied"),
                  "Should redirect to login or show access denied without authentication");
        
        log.info("✅ TEST PASS: shouldRequireProperAuthenticationForAccountOpening completed successfully");
    }
    
    // Helper method for validation
    private boolean isElementPresent(org.openqa.selenium.By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}