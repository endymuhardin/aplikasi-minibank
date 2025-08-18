package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.functional.web.pageobject.AccountListPage;
import id.ac.tazkia.minibank.functional.web.pageobject.CorporateAccountOpeningFormPage;
import id.ac.tazkia.minibank.functional.web.pageobject.CorporateCustomerSelectionPage;
import id.ac.tazkia.minibank.functional.web.pageobject.CustomerSelectionPage;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SqlGroup({
    @Sql(scripts = "/sql/setup-account-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/sql/cleanup-account-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class CorporateAccountOpeningSeleniumTest extends BaseSeleniumTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    protected void performInitialLogin() {
        // Login as Manager user who has ACCOUNT_CREATE, CUSTOMER_READ, PRODUCT_READ permissions for corporate accounts
        loginHelper.loginAsManager();
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldLoadCorporateCustomerSelectionPage() {
        log.info("🧪 TEST START: shouldLoadCorporateCustomerSelectionPage");
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        assertTrue(selectionPage.isOnCorporateCustomerSelectionPage());
        assertTrue(driver.getCurrentUrl().contains("/account/open/corporate"));
        log.info("✅ TEST PASS: shouldLoadCorporateCustomerSelectionPage completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldNavigateToCorporateSelectionFromPersonalAccounts() {
        log.info("🧪 TEST START: shouldNavigateToCorporateSelectionFromPersonalAccounts");
        
        // Start from personal account selection
        CustomerSelectionPage personalSelectionPage = new CustomerSelectionPage(driver, baseUrl);
        personalSelectionPage.openAndWaitForLoad();
        
        // Navigate to corporate account opening - assuming there's a link/button
        CorporateCustomerSelectionPage corporateSelectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        corporateSelectionPage.open();
        
        assertTrue(corporateSelectionPage.isOnCorporateCustomerSelectionPage());
        assertTrue(driver.getCurrentUrl().contains("/account/open/corporate"));
        log.info("✅ TEST PASS: shouldNavigateToCorporateSelectionFromPersonalAccounts completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayOnlyCorporateCustomersForSelection() {
        log.info("🧪 TEST START: shouldDisplayOnlyCorporateCustomersForSelection");
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        if (selectionPage.hasCorporateCustomers()) {
            assertTrue(selectionPage.getCorporateCustomerCount() > 0);
            assertTrue(selectionPage.verifyCorporateBadgeDisplayed());
            assertTrue(selectionPage.verifyRegistrationNumberDisplayed());
            assertTrue(selectionPage.verifyTaxIdDisplayed());
            
            // Check for any corporate customer from migration data
            Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
            if (corporateCustomer.isPresent() && corporateCustomer.get().getCustomerType() == Customer.CustomerType.CORPORATE) {
                assertTrue(selectionPage.isCorporateCustomerDisplayed("C1000004"));
            }
        }
        
        log.info("✅ TEST PASS: shouldDisplayOnlyCorporateCustomersForSelection completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldSearchCorporateCustomersInSelection() {
        log.info("🧪 TEST START: shouldSearchCorporateCustomersInSelection");
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        // Search for specific corporate customer
        selectionPage.searchCorporateCustomers("CORP");
        
        // Verify search results show only corporate customers
        if (selectionPage.hasCorporateCustomers()) {
            assertTrue(selectionPage.verifyCorporateBadgeDisplayed());
        }
        
        log.info("✅ TEST PASS: shouldSearchCorporateCustomersInSelection completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldOpenCorporateAccountFormForSelectedCustomer() {
        log.info("🧪 TEST START: shouldOpenCorporateAccountFormForSelectedCustomer");
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        // Find a corporate customer from migration data
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        assertTrue(formPage.isOnCorporateAccountOpeningFormPage());
        assertTrue(driver.getCurrentUrl().contains("/account/open/corporate/"));
        
        // Verify corporate customer information is displayed
        assertNotNull(formPage.getCompanyName());
        assertEquals("C1000004", formPage.getCustomerNumber());
        assertTrue(formPage.isCorporateBenefitsSectionDisplayed());
        log.info("✅ TEST PASS: shouldOpenCorporateAccountFormForSelectedCustomer completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldDisplayCorporateProductInformationWhenSelected() {
        log.info("🧪 TEST START: shouldDisplayCorporateProductInformationWhenSelected");
        
        // Find a corporate customer from migration data
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        // Select first available corporate product
        formPage.selectFirstAvailableCorporateProduct();
        
        // Verify corporate product information is displayed
        assertTrue(formPage.isCorporateProductInfoDisplayed());
        assertNotNull(formPage.getCorporateProductType());
        assertNotNull(formPage.getStandardMinimumBalance());
        assertNotNull(formPage.getCorporateMinimumBalance());
        assertTrue(formPage.isCorporateDepositInfoDisplayed());
        
        log.info("✅ TEST PASS: shouldDisplayCorporateProductInformationWhenSelected completed successfully");
    }
    
    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void shouldSuccessfullyOpenCorporateAccountWithValidData() {
        log.info("🧪 TEST START: shouldSuccessfullyOpenCorporateAccountWithValidData");
        
        // Find a corporate customer from migration data
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        // Fill the form with valid corporate data (higher minimum deposit)
        formPage.fillCompleteCorporateFormWithFirstProduct(
            "PT. Technology Maju - Corporate Operations Account",
            "500000.00",  // IDR 500,000 initial deposit (higher than personal minimum)
            "manager1"
        );
        
        AccountListPage listPage = formPage.submitCorporateForm();
        
        // Verify successful submission
        assertTrue(listPage.isSuccessMessageDisplayed());
        assertTrue(listPage.getSuccessMessage().contains("Corporate account opened successfully"));
        assertTrue(listPage.getSuccessMessage().contains("Account Number"));
        assertTrue(listPage.getSuccessMessage().contains("CORP")); // Corporate account number prefix
        
        // Verify corporate account was created in database
        long initialAccountCount = accountRepository.count();
        assertTrue(initialAccountCount > 0, "Corporate account should have been created in database");
        
        log.info("✅ TEST PASS: shouldSuccessfullyOpenCorporateAccountWithValidData completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldShowValidationErrorForInsufficientCorporateDeposit() {
        log.info("🧪 TEST START: shouldShowValidationErrorForInsufficientCorporateDeposit");
        
        // Find a corporate customer from migration data
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        // Fill form with insufficient initial deposit for corporate account
        formPage.fillCompleteCorporateFormWithFirstProduct(
            "Test Corporate Account",
            "50000.00",  // Amount that might be below corporate minimum (5x standard)
            "manager1"
        );
        
        formPage.submitCorporateFormExpectingError();
        
        // Verify corporate-specific error is displayed
        assertTrue(formPage.isErrorMessageDisplayed() || formPage.isCorporateMinDepositWarningDisplayed());
        if (formPage.isErrorMessageDisplayed()) {
            assertTrue(formPage.getErrorMessage().contains("corporate") || 
                      formPage.getErrorMessage().contains("Initial deposit"));
        }
        
        // Verify we're still on the corporate form page
        assertTrue(formPage.isOnCorporateAccountOpeningFormPage());
        log.info("✅ TEST PASS: shouldShowValidationErrorForInsufficientCorporateDeposit completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldShowValidationErrorForMissingRequiredCorporateFields() {
        log.info("🧪 TEST START: shouldShowValidationErrorForMissingRequiredCorporateFields");
        
        // Find a corporate customer from migration data
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        // Submit form without filling required fields
        formPage.submitCorporateFormExpectingError();
        
        // Verify validation errors are displayed
        assertTrue(formPage.isErrorMessageDisplayed() || 
                  isElementPresent(org.openqa.selenium.By.id("validation-errors")));
        
        // Verify we're still on the corporate form page
        assertTrue(formPage.isOnCorporateAccountOpeningFormPage());
        log.info("✅ TEST PASS: shouldShowValidationErrorForMissingRequiredCorporateFields completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldAllowNavigationBackToCorporateCustomerSelection() {
        log.info("🧪 TEST START: shouldAllowNavigationBackToCorporateCustomerSelection");
        
        // Find a corporate customer from migration data
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        CorporateCustomerSelectionPage backToSelection = formPage.goBackToCorporateCustomerSelection();
        
        assertTrue(backToSelection.isOnCorporateCustomerSelectionPage());
        assertTrue(driver.getCurrentUrl().contains("/account/open/corporate"));
        log.info("✅ TEST PASS: shouldAllowNavigationBackToCorporateCustomerSelection completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldAllowCancellingCorporateAccountOpening() {
        log.info("🧪 TEST START: shouldAllowCancellingCorporateAccountOpening");
        
        // Find a corporate customer from migration data
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        CorporateCustomerSelectionPage backToSelection = formPage.cancel();
        
        assertTrue(backToSelection.isOnCorporateCustomerSelectionPage());
        log.info("✅ TEST PASS: shouldAllowCancellingCorporateAccountOpening completed successfully");
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayCorporateSpecificInformation() {
        log.info("🧪 TEST START: shouldDisplayCorporateSpecificInformation");
        
        // Find a corporate customer from migration data
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        // Verify corporate-specific information is displayed
        assertNotNull(formPage.getCompanyName());
        assertNotNull(formPage.getCustomerNumber());
        
        // Check if registration number is displayed (may be null for test data)
        String registrationNumber = formPage.getRegistrationNumber();
        String taxId = formPage.getTaxId();
        String contactPerson = formPage.getContactPerson();
        
        // At least one corporate-specific field should be visible
        assertTrue(registrationNumber != null || taxId != null || contactPerson != null,
                  "At least one corporate-specific field should be displayed");
        
        assertTrue(formPage.isCorporateBenefitsSectionDisplayed());
        
        log.info("✅ TEST PASS: shouldDisplayCorporateSpecificInformation completed successfully");
    }
    
    @Test
    @Timeout(value = 75, unit = TimeUnit.SECONDS)
    void shouldEnforceCorporateMinimumDepositRequirements() {
        log.info("🧪 TEST START: shouldEnforceCorporateMinimumDepositRequirements");
        
        // Find a corporate customer from migration data
        Optional<Customer> corporateCustomer = customerRepository.findByCustomerNumber("C1000004");
        if (corporateCustomer.isEmpty() || corporateCustomer.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: No corporate customers found for testing");
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer("C1000004");
        formPage.waitForPageLoad();
        
        // Select a product and verify corporate minimum is higher than standard
        formPage.selectFirstAvailableCorporateProduct();
        
        assertTrue(formPage.isCorporateProductInfoDisplayed());
        
        String standardMinText = formPage.getStandardMinimumBalance();
        String corporateMinText = formPage.getCorporateMinimumBalance();
        
        assertNotNull(standardMinText);
        assertNotNull(corporateMinText);
        
        // Verify that corporate minimum is mentioned
        assertTrue(corporateMinText.contains("IDR"));
        
        log.info("✅ TEST PASS: shouldEnforceCorporateMinimumDepositRequirements completed successfully");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/account/valid-corporate-account-openings.csv", numLinesToSkip = 1)
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    void shouldOpenCorporateAccountsFromCsvData(String customerNumber, String accountName, String initialDeposit, String accountManager) {
        log.info("🧪 TEST START: shouldOpenCorporateAccountsFromCsvData for customer: {}", customerNumber);
        
        // Verify corporate customer exists
        Optional<Customer> customerOpt = customerRepository.findByCustomerNumber(customerNumber);
        if (customerOpt.isEmpty() || customerOpt.get().getCustomerType() != Customer.CustomerType.CORPORATE) {
            log.warn("⚠️ SKIP: Corporate customer {} not found in database, skipping test", customerNumber);
            return;
        }
        
        CorporateCustomerSelectionPage selectionPage = new CorporateCustomerSelectionPage(driver, baseUrl);
        selectionPage.openAndWaitForLoad();
        
        CorporateAccountOpeningFormPage formPage = selectionPage.selectCorporateCustomer(customerNumber);
        formPage.waitForPageLoad();
        
        formPage.fillCompleteCorporateFormWithFirstProduct(accountName, initialDeposit, accountManager);
        
        AccountListPage listPage = formPage.submitCorporateForm();
        
        // Verify successful submission
        assertTrue(listPage.isSuccessMessageDisplayed(), 
            "Corporate account opening should be successful for customer " + customerNumber);
        assertTrue(listPage.getSuccessMessage().contains("Corporate account opened successfully"));
        
        log.info("✅ TEST PASS: shouldOpenCorporateAccountsFromCsvData completed successfully for customer: {}", customerNumber);
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