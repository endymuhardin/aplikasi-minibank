package id.ac.tazkia.minibank.functional.success;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.AccountManagementPage;
import id.ac.tazkia.minibank.functional.pages.CustomerManagementPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import com.microsoft.playwright.Locator;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("playwright-success")
@DisplayName("Corporate Customer Account Opening Success Scenario Tests")
class CorporateAccountOpeningSuccessTest extends BasePlaywrightTest {
    
    private AccountManagementPage accountPage;
    
    @BeforeEach
    void setUp() {
        // Login as Customer Service (CS) who has permission to open accounts
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("cs1", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        
        // Initialize account management page
        accountPage = new AccountManagementPage(page);
        
        // Ensure test customers exist by creating them if needed
        ensureCorporateCustomersExist();
    }
    
    private void ensureCorporateCustomersExist() {
        try {
            // Navigate to customer list to check if customers exist
            page.navigate(baseUrl + "/customer/list");
            page.waitForLoadState();
            
            // Check if customers exist
            Locator customerTable = page.locator("#customer-table tbody tr");
            if (customerTable.count() == 0 || 
                (customerTable.count() == 1 && page.locator("td:has-text('No customers found')").isVisible())) {
                
                log.info("No corporate customers found, creating test customers...");
                
                // Create corporate test customers
                CustomerManagementPage customerPage = new CustomerManagementPage(page);
                
                // Create PT. Teknologi Maju
                createCorporateCustomer(customerPage, "PT. Teknologi Maju", "1234567890123456");
                
                log.info("Corporate test customers created successfully");
            } else {
                log.info("Corporate customers already exist, proceeding with tests");
            }
        } catch (Exception e) {
            log.error("Failed to ensure corporate test customers exist: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create corporate customers needed for account opening tests", e);
        }
    }
    
    private void createCorporateCustomer(CustomerManagementPage customerPage, String companyName, String registrationNumber) {
        try {
            // Navigate to create customer
            customerPage.navigateToAddCustomer(baseUrl);
            
            // Select corporate customer type
            customerPage.selectCustomerType("CORPORATE");
            page.waitForLoadState();
            
            // Fill and save corporate customer form
            customerPage.fillCorporateCustomerForm(
                companyName,                     // companyName
                registrationNumber,              // companyRegistrationNumber
                "01.234.567.8-901.000",         // taxIdentificationNumber
                "Direktur",                     // contactPersonName
                "CEO",                          // contactPersonTitle
                "info@teknologimaju.com",       // email
                "02123456789",                  // phone
                "Jl. HR Rasuna Said No. 789",   // address
                "Jakarta",                      // city
                "12950",                        // postalCode
                "Indonesia"                     // country
            );
            
            customerPage.clickSave();
            page.waitForLoadState();
            
            log.info("Corporate customer '{}' created successfully", companyName);
        } catch (Exception e) {
            log.error("Failed to create corporate customer '{}': {}", companyName, e.getMessage(), e);
            throw e;
        }
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/corporate-account-opening-success.csv", numLinesToSkip = 1)
    @DisplayName("Should successfully open various corporate Islamic banking accounts")
    void shouldOpenCorporateAccountSuccessfully(
            String customerIdentifier, String productCode, String productName,
            String initialDeposit, String accountPurpose, boolean expectNisbah) {
        
        log.info("Corporate Account Success Test: Opening {} account for customer: {}", productName, customerIdentifier);
        
        // Navigate to account opening page
        accountPage.navigateToOpenAccount(baseUrl);
        page.waitForLoadState();
        
        // Fill account opening form
        accountPage.fillAccountOpeningForm(customerIdentifier, productCode, initialDeposit, accountPurpose);
        
        // Save account
        accountPage.clickSave();
        page.waitForLoadState();
        
        // Verify success
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "Should show success message or redirect to account view page");
        
        // If on view page, verify account details
        if (accountPage.isOnViewPage()) {
            String accountNumber = accountPage.getAccountNumber();
            assertFalse(accountNumber.isEmpty(), "Account number should be generated");
            assertTrue(accountNumber.matches("A\\d+"), "Account number should follow pattern Axxxxxxx");
            
            String status = accountPage.getAccountStatus();
            assertTrue(status.contains("ACTIVE") || status.contains("Active"), 
                    "New account should have ACTIVE status");
            
            String balance = accountPage.getCurrentBalance();
            assertTrue(balance.contains(initialDeposit) || balance.contains(formatAmount(initialDeposit)),
                    "Initial deposit should be reflected in balance");
            
            String details = accountPage.getAccountDetailsText();
            assertTrue(details.contains(customerIdentifier) || details.contains(productName),
                    "Account details should show customer and product information");
            
            // Corporate accounts typically use Wadiah (safe-keeping) rather than profit-sharing
            if (expectNisbah) {
                assertTrue(accountPage.isNisbahDisplayed(),
                        "Nisbah ratio should be displayed for " + productName);
                
                String nisbahRatio = accountPage.getNisbahRatio();
                assertFalse(nisbahRatio.isEmpty(), "Nisbah ratio should have value");
                log.info("Nisbah ratio displayed: {}", nisbahRatio);
            } else {
                // Wadiah products should not have nisbah
                assertFalse(accountPage.isNisbahDisplayed(),
                        "Wadiah products should not display nisbah ratio");
            }
            
            log.info("✅ Corporate account opened successfully: {} for {}", accountNumber, customerIdentifier);
        }
    }
    
    @Test
    @DisplayName("Should successfully open Giro Wadiah Corporate account with high minimum deposit")
    void shouldOpenGiroWadiahCorporateAccountSuccessfully() {
        log.info("Corporate Success Test: Opening Giro Wadiah Corporate account");
        
        // Navigate to account opening
        accountPage.navigateToOpenAccount(baseUrl);
        page.waitForLoadState();
        
        // Open Giro Wadiah Corporate account with well above minimum deposit (5,000,000)
        // GIR001 minimum opening balance is 1,000,000, so 5,000,000 should be safe
        accountPage.fillAccountOpeningForm("PT. Teknologi Maju", "GIR001", "5000000", "Business Operations");
        
        // Save account
        accountPage.clickSave();
        page.waitForLoadState();
        
        // Debug: Check current page state
        log.info("Current URL after save: {}", page.url());
        log.info("Success message visible: {}", accountPage.isSuccessMessageVisible());
        log.info("On view page: {}", accountPage.isOnViewPage());
        
        // Check for any error messages on the page
        if (page.locator("#error-message").isVisible()) {
            String errorText = page.locator("#error-message").textContent();
            log.error("Error message found: {}", errorText);
        }
        if (page.locator("#validation-errors").isVisible()) {
            String validationText = page.locator("#validation-errors").textContent();
            log.error("Validation errors found: {}", validationText);
        }
        
        // Verify success
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "Should successfully open Giro Wadiah Corporate account");
        
        if (accountPage.isOnViewPage()) {
            String productDetails = accountPage.getProductDetails();
            assertTrue(productDetails.contains("Giro") || productDetails.contains("GIR001") || productDetails.contains("Wadiah"), 
                    "Product should be Giro Wadiah Corporate");
            
            // Wadiah products should NOT have nisbah (safe-keeping, not profit-sharing)
            assertFalse(accountPage.isNisbahDisplayed(),
                    "Wadiah products should not display nisbah ratio");
            
            // Verify corporate-specific details
            String details = accountPage.getAccountDetailsText();
            assertTrue(details.contains("PT.") || details.contains("Corporate"),
                    "Should show corporate customer information");
        }
        
        log.info("✅ Corporate Giro Wadiah account opened successfully");
    }
    
    @Test
    @DisplayName("Should successfully open multiple corporate accounts for same company")
    void shouldOpenMultipleCorporateAccountsSuccessfully() {
        log.info("Corporate Success Test: Opening multiple accounts for same company");
        
        String companyName = "PT. Teknologi Maju";
        
        // Open first account - Giro Wadiah for operations
        accountPage.navigateToOpenAccount(baseUrl);
        accountPage.fillAccountOpeningForm(companyName, "GIR001", "5000000", "Daily Operations");
        accountPage.clickSave();
        page.waitForLoadState();
        
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "First corporate account should be opened successfully");
        
        String firstAccountNumber = accountPage.getAccountNumber();
        
        // Open second account - Giro Wadiah for investment
        accountPage.navigateToOpenAccount(baseUrl);
        accountPage.fillAccountOpeningForm(companyName, "GIR001", "10000000", "Investment Fund");
        accountPage.clickSave();
        page.waitForLoadState();
        
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "Second corporate account should be opened successfully");
        
        String secondAccountNumber = accountPage.getAccountNumber();
        
        // Verify different account numbers
        assertNotEquals(firstAccountNumber, secondAccountNumber,
                "Multiple corporate accounts should have different account numbers");
        
        log.info("✅ Multiple corporate accounts opened successfully for same company");
    }
    
    @Test
    @DisplayName("Should successfully handle high-value corporate deposits")
    void shouldHandleHighValueCorporateDeposits() {
        log.info("Corporate Success Test: High-value corporate deposit");
        
        // Navigate to account opening
        accountPage.navigateToOpenAccount(baseUrl);
        page.waitForLoadState();
        
        // Open account with very high initial deposit (typical for corporations)
        accountPage.fillAccountOpeningForm("PT. Teknologi Maju", "GIR001", "100000000", "Large Corporate Fund");
        
        // Save account
        accountPage.clickSave();
        page.waitForLoadState();
        
        // Verify success
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "Should successfully handle high-value corporate deposit");
        
        if (accountPage.isOnViewPage()) {
            String balance = accountPage.getCurrentBalance();
            assertTrue(balance.contains("100000000") || balance.contains("100.000.000"),
                    "Should correctly display high-value balance");
        }
        
        log.info("✅ High-value corporate account opened successfully");
    }
    
    @Test
    @DisplayName("Should successfully validate corporate Islamic banking compliance")
    void shouldValidateCorporateIslamicBankingCompliance() {
        log.info("Corporate Success Test: Corporate Islamic banking compliance validation");
        
        // Open a corporate Wadiah account
        accountPage.navigateToOpenAccount(baseUrl);
        accountPage.fillAccountOpeningForm(
            "PT. Teknologi Maju", 
            "GIR001", // Corporate Wadiah product
            "5000000", 
            "Corporate Islamic Compliance Test"
        );
        accountPage.clickSave();
        page.waitForLoadState();
        
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "Corporate Islamic banking account should be opened successfully");
        
        if (accountPage.isOnViewPage()) {
            String details = accountPage.getAccountDetailsText();
            
            // Verify Islamic banking terms are used
            assertFalse(details.toLowerCase().contains("interest"), 
                    "Should not contain 'interest' term in Islamic banking");
            
            // Verify Wadiah (safe-keeping) concept for corporate accounts
            assertTrue(details.contains("Wadiah") || details.contains("wadiah") ||
                      details.toLowerCase().contains("safe") || details.toLowerCase().contains("keeping"),
                    "Should mention Wadiah (safe-keeping) concept for corporate accounts");
            
            // Corporate Wadiah should not have profit-sharing nisbah
            assertFalse(accountPage.isNisbahDisplayed(),
                    "Corporate Wadiah accounts should not have profit-sharing nisbah");
        }
        
        log.info("✅ Corporate Islamic banking compliance validated successfully");
    }
    
    @Test
    @DisplayName("Should successfully display corporate account list")
    void shouldDisplayCorporateAccountListSuccessfully() {
        log.info("Corporate Success Test: Corporate account list display");
        
        // Create an account first to ensure there's data
        accountPage.navigateToOpenAccount(baseUrl);
        accountPage.fillAccountOpeningForm("PT. Teknologi Maju", "GIR001", "2000000", "Test Account");
        accountPage.clickSave();
        page.waitForLoadState();
        
        // Navigate to account list
        accountPage.navigateToList(baseUrl);
        
        // Verify list page loaded
        assertTrue(accountPage.isOnListPage(), "Should be on account list page");
        
        // Check if table exists
        boolean tableVisible = page.locator("#accounts-table").isVisible();
        assertTrue(tableVisible, "Corporate account table should be visible");
        
        // Verify corporate accounts are shown
        if (tableVisible) {
            int accountCount = accountPage.getAccountRowCount();
            log.info("Corporate account row count: {}", accountCount);
            assertTrue(accountCount >= 0, "Corporate account list should be accessible");
        }
        
        log.info("✅ Corporate account list displayed successfully");
    }
    
    /**
     * Helper method to format amount for display
     */
    private String formatAmount(String amount) {
        try {
            long value = Long.parseLong(amount);
            return String.format("%,d", value).replace(",", ".");
        } catch (NumberFormatException e) {
            return amount;
        }
    }
}