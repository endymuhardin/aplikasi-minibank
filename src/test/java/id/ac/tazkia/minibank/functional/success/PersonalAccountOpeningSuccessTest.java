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
@DisplayName("Personal Customer Account Opening Success Scenario Tests")
class PersonalAccountOpeningSuccessTest extends BasePlaywrightTest {
    
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
        ensurePersonalCustomersExist();
    }
    
    private void ensurePersonalCustomersExist() {
        try {
            // Navigate to customer list to check if customers exist
            page.navigate(baseUrl + "/customer/list");
            page.waitForLoadState();
            
            // Check if customers exist
            Locator customerTable = page.locator("#customer-table tbody tr");
            if (customerTable.count() == 0 || 
                (customerTable.count() == 1 && page.locator("td:has-text('No customers found')").isVisible())) {
                
                log.info("No personal customers found, creating test customers...");
                
                // Create personal test customers
                CustomerManagementPage customerPage = new CustomerManagementPage(page);
                
                // Create Ahmad Suharto
                createPersonalCustomer(customerPage, "Ahmad Suharto", "3271081503850001");
                
                // Create Siti Nurhaliza  
                createPersonalCustomer(customerPage, "Siti Nurhaliza", "3271082207900002");
                
                log.info("Personal test customers created successfully");
            } else {
                log.info("Personal customers already exist, proceeding with tests");
            }
        } catch (Exception e) {
            log.error("Failed to ensure personal test customers exist: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create personal customers needed for account opening tests", e);
        }
    }
    
    private void createPersonalCustomer(CustomerManagementPage customerPage, String name, String identityNumber) {
        try {
            // Navigate to create customer
            customerPage.navigateToAddCustomer(baseUrl);
            
            // Select personal customer type
            customerPage.selectCustomerType("PERSONAL");
            page.waitForLoadState();
            
            // Fill and save personal customer form
            customerPage.fillPersonalCustomerForm(
                name,                          // name
                identityNumber,                // identityNumber
                "KTP",                        // identityType
                "1990-01-01",                // birthDate
                null,                        // birthPlace (optional)
                null,                        // gender (optional)
                null,                        // motherName (optional)
                name.toLowerCase().replace(" ", ".") + "@email.com", // email
                "081234567890",              // phone
                "Test Address 123",          // address
                "Jakarta",                   // city
                "DKI Jakarta",               // province
                "12345"                      // postalCode
            );
            
            customerPage.clickSave();
            page.waitForLoadState();
            
            log.info("Personal customer '{}' created successfully", name);
        } catch (Exception e) {
            log.error("Failed to create personal customer '{}': {}", name, e.getMessage(), e);
            throw e;
        }
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/functional/personal-account-opening-success.csv", numLinesToSkip = 1)
    @DisplayName("[CS-S-002-01] Should successfully open various personal Islamic banking accounts")
    void shouldOpenPersonalAccountSuccessfully(
            String customerIdentifier, String productCode, String productName,
            String initialDeposit, String accountPurpose, boolean expectNisbah) {
        
        log.info("Personal Account Success Test: Opening {} account for customer: {}", productName, customerIdentifier);
        
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
            
            // Verify nisbah display for Mudharabah products
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
            
            log.info("✅ Personal account opened successfully: {} for {}", accountNumber, customerIdentifier);
        }
    }
    
    @Test
    @DisplayName("[CS-S-002-02] Should successfully open Tabungan Wadiah account with minimum deposit")
    void shouldOpenTabunganWadiahAccountSuccessfully() {
        log.info("Personal Success Test: Opening Tabungan Wadiah account");
        
        // Navigate to account opening
        accountPage.navigateToOpenAccount(baseUrl);
        page.waitForLoadState();
        
        // Open Tabungan Wadiah account with minimum deposit (50,000)
        accountPage.fillAccountOpeningForm("Ahmad Suharto", "TAB001", "50000", "Emergency Fund");
        
        // Save account
        accountPage.clickSave();
        page.waitForLoadState();
        
        // Verify success
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "Should successfully open Tabungan Wadiah account");
        
        if (accountPage.isOnViewPage()) {
            String productDetails = accountPage.getProductDetails();
            assertTrue(productDetails.contains("Wadiah") || productDetails.contains("TAB001"), 
                    "Product should be Tabungan Wadiah");
            
            // Wadiah products should NOT have nisbah
            assertFalse(accountPage.isNisbahDisplayed(),
                    "Wadiah products should not display nisbah ratio");
        }
        
        log.info("✅ Personal Tabungan Wadiah account opened successfully");
    }
    
    @Test
    @DisplayName("[CS-S-002-03] Should successfully open Tabungan Mudharabah account with profit sharing")
    void shouldOpenTabunganMudharabahAccountSuccessfully() {
        log.info("Personal Success Test: Opening Tabungan Mudharabah account");
        
        // Navigate to account opening
        accountPage.navigateToOpenAccount(baseUrl);
        page.waitForLoadState();
        
        // Open Tabungan Mudharabah account with minimum deposit (1,000,000)
        accountPage.fillAccountOpeningForm("Siti Nurhaliza", "TAB002", "1000000", "Investment Savings");
        
        // Save account
        accountPage.clickSave();
        page.waitForLoadState();
        
        // Verify success
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "Should successfully open Tabungan Mudharabah account");
        
        if (accountPage.isOnViewPage()) {
            String productDetails = accountPage.getProductDetails();
            assertTrue(productDetails.contains("Mudharabah") || productDetails.contains("TAB002"), 
                    "Product should be Tabungan Mudharabah");
            
            // Mudharabah products MUST have nisbah
            assertTrue(accountPage.isNisbahDisplayed(),
                    "Mudharabah products should display nisbah ratio");
            
            String nisbahRatio = accountPage.getNisbahRatio();
            assertTrue(nisbahRatio.contains("70") || nisbahRatio.contains("30"),
                    "Should show 70:30 profit sharing ratio");
        }
        
        log.info("✅ Personal Tabungan Mudharabah account opened successfully with nisbah");
    }
    
    @Test
    @DisplayName("[CS-S-002-06] Should successfully open Deposito Mudharabah account for high-value customers")
    void shouldOpenDepositoMudharabahAccountSuccessfully() {
        log.info("Personal Success Test: Opening Deposito Mudharabah account");
        
        // Navigate to account opening
        accountPage.navigateToOpenAccount(baseUrl);
        page.waitForLoadState();
        
        // Open Deposito Mudharabah account (high minimum deposit)
        accountPage.fillAccountOpeningForm("Siti Nurhaliza", "DEP001", "10000000", "Term Deposit Investment");
        
        // Save account
        accountPage.clickSave();
        page.waitForLoadState();
        
        // Verify success
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "Should successfully open Deposito Mudharabah account");
        
        if (accountPage.isOnViewPage()) {
            String productDetails = accountPage.getProductDetails();
            assertTrue(productDetails.contains("Deposito") || productDetails.contains("DEP001"), 
                    "Product should be Deposito Mudharabah");
            
            // Deposito products have nisbah and term
            assertTrue(accountPage.isNisbahDisplayed(),
                    "Deposito should display nisbah ratio");
            
            String nisbahRatio = accountPage.getNisbahRatio();
            assertTrue(nisbahRatio.contains("70") || nisbahRatio.contains("30"),
                    "Should show 70:30 profit sharing ratio for Deposito");
        }
        
        log.info("✅ Personal Deposito Mudharabah account opened successfully");
    }
    
    @Test
    @DisplayName("[CS-S-002-99] Should successfully validate Islamic banking compliance for personal accounts")
    void shouldValidatePersonalIslamicBankingCompliance() {
        log.info("Personal Success Test: Islamic banking compliance validation");
        
        // Open a Mudharabah account
        accountPage.navigateToOpenAccount(baseUrl);
        accountPage.fillAccountOpeningForm(
            "Siti Nurhaliza", 
            "TAB002", // Mudharabah product
            "5000000", 
            "Islamic Compliance Test"
        );
        accountPage.clickSave();
        page.waitForLoadState();
        
        assertTrue(accountPage.isSuccessMessageVisible() || accountPage.isOnViewPage(),
                "Personal Islamic banking account should be opened successfully");
        
        if (accountPage.isOnViewPage()) {
            String details = accountPage.getAccountDetailsText();
            
            // Verify Islamic banking terms are used
            assertFalse(details.toLowerCase().contains("interest"), 
                    "Should not contain 'interest' term in Islamic banking");
            
            // Verify profit sharing is mentioned for Mudharabah
            assertTrue(accountPage.isNisbahDisplayed() || 
                      details.contains("bagi hasil") || 
                      details.contains("profit sharing"),
                    "Should mention profit sharing for Mudharabah products");
        }
        
        log.info("✅ Personal Islamic banking compliance validated successfully");
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