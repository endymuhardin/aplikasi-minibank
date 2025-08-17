package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
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
import id.ac.tazkia.minibank.functional.web.pageobject.AccountListPage;
import id.ac.tazkia.minibank.functional.web.pageobject.AccountOpeningFormPage;
import id.ac.tazkia.minibank.functional.web.pageobject.CustomerSelectionPage;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Personal Account Opening Selenium Tests
 * 
 * This test class focuses on personal customer account opening scenarios using Selenium WebDriver.
 * Tests cover the complete flow from customer selection to account creation for individual customers.
 * 
 * Key test areas:
 * - Personal customer selection and search
 * - Account opening form validation
 * - Product selection for personal accounts
 * - Successful account creation flow
 * - Field validation and error handling
 * - Navigation and cancellation scenarios
 * 
 * Complements CorporateAccountOpeningSeleniumTest for corporate customers.
 */
@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup-account-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-account-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class PersonalAccountOpeningSeleniumTest extends BaseSeleniumTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    protected void performInitialLogin() {
        // Login as Teller user who has ACCOUNT_CREATE, CUSTOMER_READ, PRODUCT_READ permissions
        loginHelper.loginAsTeller();
    }
    
    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void shouldLoadAccountListPage() {
        log.info("🧪 TEST START: shouldLoadAccountListPage (Personal Accounts)");
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        assertTrue(listPage.isOnAccountListPage());
        assertTrue(driver.getCurrentUrl().contains("/account/list"));
        log.info("✅ TEST PASS: shouldLoadAccountListPage (Personal Accounts) completed successfully");
    }
    
    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void shouldNavigateToCustomerSelectionFromAccountList() {
        log.info("🧪 TEST START: shouldNavigateToPersonalCustomerSelectionFromAccountList");
        AccountListPage listPage = new AccountListPage(driver, baseUrl);
        listPage.openAndWaitForLoad();
        
        CustomerSelectionPage selectionPage = listPage.clickOpenNewAccount();
        
        assertTrue(selectionPage.isOnCustomerSelectionPage());
        assertTrue(driver.getCurrentUrl().contains("/account/open"));
        log.info("✅ TEST PASS: shouldNavigateToPersonalCustomerSelectionFromAccountList completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayActiveCustomersForSelection() {
        log.info("🧪 TEST START: shouldDisplayActivePersonalCustomersForSelection");
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        assertTrue(selectionPage.hasCustomers());
        assertTrue(selectionPage.getCustomerCount() > 0);
        
        // Check if test personal customer is displayed (using existing migration data)
        assertTrue(selectionPage.isCustomerDisplayed("C1000001"));
        log.info("✅ TEST PASS: shouldDisplayActivePersonalCustomersForSelection completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldSearchCustomersInSelection() {
        log.info("🧪 TEST START: shouldSearchPersonalCustomersInSelection");
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        // Search for specific personal customer
        selectionPage.searchCustomers("C1000001");
        
        assertTrue(selectionPage.hasCustomers());
        assertTrue(selectionPage.isCustomerDisplayed("C1000001"));
        log.info("✅ TEST PASS: shouldSearchPersonalCustomersInSelection completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldOpenAccountOpeningFormForSelectedCustomer() {
        log.info("🧪 TEST START: shouldOpenPersonalAccountOpeningFormForSelectedCustomer");
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        assertTrue(formPage.isOnAccountOpeningFormPage());
        assertTrue(driver.getCurrentUrl().contains("/account/open/"));
        
        // Verify personal customer information is displayed
        assertNotNull(formPage.getCustomerName());
        assertEquals("C1000001", formPage.getCustomerNumber());
        log.info("✅ TEST PASS: shouldOpenPersonalAccountOpeningFormForSelectedCustomer completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldDisplayProductInformationWhenSelected() {
        log.info("🧪 TEST START: shouldDisplayProductInformationWhenSelected");
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Select first available product
        formPage.selectFirstAvailableProduct();
        
        // Verify product information is displayed
        assertTrue(formPage.isProductInfoDisplayed());
        assertNotNull(formPage.getProductType());
        assertNotNull(formPage.getMinimumBalance());
        log.info("✅ TEST PASS: shouldDisplayProductInformationWhenSelected completed successfully");
    }
    
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldSuccessfullyOpenAccountWithValidData() {
        log.info("🧪 TEST START: shouldSuccessfullyOpenPersonalAccountWithValidData");
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Fill the form with valid personal account data
        formPage.fillCompleteFormWithFirstProduct(
            "Test Personal Savings Account",
            "100000.00",  // IDR 100,000 initial deposit
            "teller1"
        );
        
        AccountListPage listPage = formPage.submitForm();
        
        // Verify successful personal account creation
        assertTrue(listPage.isSuccessMessageDisplayed());
        assertTrue(listPage.getSuccessMessage().contains("Account opened successfully"));
        assertTrue(listPage.getSuccessMessage().contains("Account Number"));
        
        // Verify personal account was created in database
        long accountCount = accountRepository.count();
        assertTrue(accountCount > 0, "Personal account should have been created in database");
        
        log.info("✅ TEST PASS: shouldSuccessfullyOpenPersonalAccountWithValidData completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldShowValidationErrorForInsufficientInitialDeposit() {
        log.info("🧪 TEST START: shouldShowValidationErrorForInsufficientInitialDeposit");
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Fill form with insufficient initial deposit
        formPage.fillCompleteFormWithFirstProduct(
            "Test Savings Account",
            "10.00",  // Very low amount that should be below minimum
            "teller1"
        );
        
        formPage.submitFormExpectingError();
        
        // Verify error is displayed
        assertTrue(formPage.isErrorMessageDisplayed());
        assertTrue(formPage.getErrorMessage().contains("Initial deposit must be at least"));
        
        // Verify we're still on the form page
        assertTrue(formPage.isOnAccountOpeningFormPage());
        log.info("✅ TEST PASS: shouldShowValidationErrorForInsufficientInitialDeposit completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldShowValidationErrorForMissingRequiredFields() {
        log.info("🧪 TEST START: shouldShowValidationErrorForMissingRequiredFields");
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        // Submit form without filling required fields
        formPage.submitFormExpectingError();
        
        // Verify validation errors are displayed
        assertTrue(formPage.isErrorMessageDisplayed() || 
                  isElementPresent(org.openqa.selenium.By.id("validation-errors")));
        
        // Verify we're still on the form page
        assertTrue(formPage.isOnAccountOpeningFormPage());
        log.info("✅ TEST PASS: shouldShowValidationErrorForMissingRequiredFields completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldAllowNavigationBackToCustomerSelection() {
        log.info("🧪 TEST START: shouldAllowNavigationBackToCustomerSelection");
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        CustomerSelectionPage backToSelection = formPage.goBackToCustomerSelection();
        
        assertTrue(backToSelection.isOnCustomerSelectionPage());
        assertTrue(driver.getCurrentUrl().contains("/account/open"));
        log.info("✅ TEST PASS: shouldAllowNavigationBackToCustomerSelection completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldAllowCancellingAccountOpening() {
        log.info("🧪 TEST START: shouldAllowCancellingAccountOpening");
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer("C1000001");
        formPage.waitForPageLoad();
        
        CustomerSelectionPage backToSelection = formPage.cancel();
        
        assertTrue(backToSelection.isOnCustomerSelectionPage());
        log.info("✅ TEST PASS: shouldAllowCancellingAccountOpening completed successfully");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/account/valid-account-openings.csv", numLinesToSkip = 1)
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    void shouldOpenAccountsFromCsvData(String customerNumber, String accountName, String initialDeposit, String createdBy) {
        log.info("🧪 TEST START: shouldOpenPersonalAccountsFromCsvData for customer: {}", customerNumber);
        
        // Verify personal customer exists
        Optional<Customer> customerOpt = customerRepository.findByCustomerNumber(customerNumber);
        if (customerOpt.isEmpty()) {
            log.warn("⚠️ SKIP: Personal customer {} not found in database, skipping test", customerNumber);
            return;
        }
        
        CustomerSelectionPage selectionPage = new CustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        AccountOpeningFormPage formPage = selectionPage.selectCustomer(customerNumber);
        formPage.waitForPageLoad();
        
        formPage.fillCompleteFormWithFirstProduct(accountName, initialDeposit, createdBy);
        
        AccountListPage listPage = formPage.submitForm();
        
        // Verify successful personal account creation
        assertTrue(listPage.isSuccessMessageDisplayed(), 
            "Personal account opening should be successful for customer " + customerNumber);
        
        log.info("✅ TEST PASS: shouldOpenPersonalAccountsFromCsvData completed successfully for customer: {}", customerNumber);
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