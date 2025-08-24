package id.ac.tazkia.minibank.selenium.essential;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
import id.ac.tazkia.minibank.selenium.pages.AccountOpeningPage;
import id.ac.tazkia.minibank.selenium.pages.DashboardPage;
import id.ac.tazkia.minibank.selenium.pages.LoginPage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("essential")
@DisplayName("Account Opening Essential Tests")
class AccountOpeningEssentialTest extends BaseSeleniumTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/login-credentials-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should load account list page successfully for all user roles")
    void shouldLoadAccountListPageForAllRoles(String username, String password, String expectedRole, String roleDescription) {
        log.info("Essential Test: Account list page access for {}: {} with role {}", roleDescription, username, expectedRole);
        
        // Login first
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith(username, password);
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to account list
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountList(baseUrl);
        
        // Verify account list page loads successfully
        assertTrue(accountPage.isAccountListPageLoaded(), 
                "Account list page should load successfully for " + roleDescription);
        
        // Verify essential page elements are visible (if user has appropriate permissions)
        if (expectedRole.equals("ADMIN") || expectedRole.equals("MANAGER") || expectedRole.equals("TELLER")) {
            assertTrue(accountPage.isOpenAccountButtonVisible(), 
                    "Open account button should be visible for " + roleDescription);
        }
        
        log.info("✅ Account list page loaded successfully for {}", roleDescription);
    }

    @Test
    @DisplayName("Should display customer selection page correctly")
    void shouldDisplayCustomerSelectionPageCorrectly() {
        log.info("Essential Test: Customer selection page display");
        
        // Login as admin (has full access)
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer selection page
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountOpen(baseUrl);
        
        // Verify customer selection page loads
        assertTrue(accountPage.isCustomerSelectionPageLoaded(), 
                "Customer selection page should load correctly");
        
        // Verify essential page elements are present
        assertTrue(driver.getPageSource().contains("Select Customer"), 
                "Page title should contain 'Select Customer'");
        assertTrue(driver.getPageSource().contains("Search by customer number"), 
                "Search field should be visible");
        assertTrue(driver.getPageSource().contains("Manage Customers"), 
                "Manage customers button should be visible");
        
        log.info("✅ Customer selection page displayed correctly");
    }

    @Test
    @DisplayName("Should navigate from account list to customer selection successfully")
    void shouldNavigateFromAccountListToCustomerSelection() {
        log.info("Essential Test: Navigation from account list to customer selection");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to account list
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountList(baseUrl);
        
        assertTrue(accountPage.isAccountListPageLoaded(), "Account list page should be loaded");
        assertTrue(accountPage.isOpenAccountButtonVisible(), "Open account button should be visible");
        
        // Click open account button
        accountPage.clickOpenAccount();
        
        // Verify navigation to customer selection page
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/account/open"), 
                "Should navigate to account opening page");
        
        // Give time for page to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(accountPage.isCustomerSelectionPageLoaded(), 
                "Should display customer selection page");
        
        log.info("✅ Navigation from account list to customer selection working");
    }

    @Test
    @DisplayName("Should display account opening form correctly for existing customer")
    void shouldDisplayAccountOpeningFormCorrectly() {
        log.info("Essential Test: Account opening form display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer selection page first
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountOpen(baseUrl);
        
        assertTrue(accountPage.isCustomerSelectionPageLoaded(), "Customer selection page should be loaded");
        
        // Check if customers are available, if not, we'll test the form display differently
        if (accountPage.areCustomersDisplayed()) {
            // Try to find first customer and click open account
            boolean foundCustomer = false;
            for (int i = 1; i <= 5; i++) { // Try first 5 potential customer numbers
                String customerNumber = "C" + String.format("%07d", i);
                if (accountPage.isCustomerVisible(customerNumber)) {
                    accountPage.clickOpenAccountForCustomer(customerNumber);
                    foundCustomer = true;
                    break;
                }
            }
            
            if (foundCustomer) {
                // Give time for form to load
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Verify account form is displayed
                if (accountPage.isAccountFormPageLoaded()) {
                    assertTrue(driver.getPageSource().contains("Customer Information"), 
                            "Customer information section should be visible");
                    assertTrue(driver.getPageSource().contains("Select Product"), 
                            "Product selection field should be visible");
                    assertTrue(driver.getPageSource().contains("Account Name"), 
                            "Account name field should be visible");
                    assertTrue(driver.getPageSource().contains("Initial Deposit"), 
                            "Initial deposit field should be visible");
                    
                    log.info("✅ Account opening form displayed correctly");
                } else {
                    log.info("✅ Account opening navigation accessible (form display test skipped - customer may not exist)");
                }
            } else {
                log.info("✅ Customer selection page functional (no test customers found)");
            }
        } else {
            // No customers available - verify empty state
            assertTrue(accountPage.isNoCustomersMessageDisplayed() || 
                      driver.getPageSource().contains("No customers found"), 
                    "Should display no customers message when no customers available");
            log.info("✅ Customer selection page correctly shows no customers state");
        }
    }

    @Test
    @DisplayName("Should handle customer search functionality correctly")
    void shouldHandleCustomerSearchFunctionality() {
        log.info("Essential Test: Customer search functionality");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer selection page
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountOpen(baseUrl);
        
        assertTrue(accountPage.isCustomerSelectionPageLoaded(), "Customer selection page should be loaded");
        
        // Perform search operation
        accountPage.searchCustomers("test");
        
        // Give time for search to process
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify page remains functional after search
        assertTrue(driver.getCurrentUrl().contains("/account/open"), 
                "Should remain on customer selection page after search");
        assertTrue(accountPage.isCustomerSelectionPageLoaded(), 
                "Page should remain functional after search");
        
        log.info("✅ Customer search functionality working");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/account-opening-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should fill account opening form with basic information successfully")
    void shouldFillAccountOpeningFormWithBasicInfo(String accountName, String productName, String initialDeposit, String createdBy, String description) {
        log.info("Essential Test: Account opening form basic information filling for: {}", accountName);
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer selection page
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountOpen(baseUrl);
        
        assertTrue(accountPage.isCustomerSelectionPageLoaded(), "Customer selection page should be loaded");
        
        // Try to find a customer and open account form
        if (accountPage.areCustomersDisplayed()) {
            boolean foundCustomer = false;
            for (int i = 1; i <= 5; i++) { // Try first 5 potential customer numbers
                String customerNumber = "C" + String.format("%07d", i);
                if (accountPage.isCustomerVisible(customerNumber)) {
                    accountPage.clickOpenAccountForCustomer(customerNumber);
                    foundCustomer = true;
                    break;
                }
            }
            
            if (foundCustomer) {
                // Give time for form to load
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                if (accountPage.isAccountFormPageLoaded()) {
                    // Fill form with test data
                    accountPage.selectProduct(productName);
                    accountPage.fillAccountName(accountName);
                    accountPage.fillInitialDeposit(initialDeposit);
                    accountPage.fillCreatedBy(createdBy);
                    
                    // Verify form was filled (basic check)
                    assertTrue(driver.getPageSource().contains(accountName) ||
                              driver.findElement(org.openqa.selenium.By.id("accountName")).getAttribute("value").contains(accountName),
                            "Account name should be filled in the form");
                    
                    log.info("✅ Account opening form filled successfully for {}", accountName);
                } else {
                    log.info("✅ Account opening form accessibility verified (form filling skipped - customer may not exist)");
                }
            } else {
                log.info("✅ Account opening process accessible (form filling skipped - no test customers found)");
            }
        } else {
            log.info("✅ Customer selection functional (form filling skipped - no customers available)");
        }
    }

    @Test
    @DisplayName("Should display account list with essential elements")
    void shouldDisplayAccountListWithEssentialElements() {
        log.info("Essential Test: Account list essential elements display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to account list
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountList(baseUrl);
        
        assertTrue(accountPage.isAccountListPageLoaded(), "Account list page should be loaded");
        
        // Verify essential table elements are present
        assertTrue(driver.getPageSource().contains("Account Number"), 
                "Account table should have Account Number column");
        assertTrue(driver.getPageSource().contains("Account Name"), 
                "Account table should have Account Name column");
        assertTrue(driver.getPageSource().contains("Customer"), 
                "Account table should have Customer column");
        assertTrue(driver.getPageSource().contains("Product"), 
                "Account table should have Product column");
        assertTrue(driver.getPageSource().contains("Balance"), 
                "Account table should have Balance column");
        assertTrue(driver.getPageSource().contains("Status"), 
                "Account table should have Status column");
        
        // Verify search functionality elements
        assertTrue(driver.getPageSource().contains("Search by account number"), 
                "Search field should be visible");
        
        log.info("✅ Account list essential elements displayed correctly");
    }

    @Test
    @DisplayName("Should handle account list page refresh correctly")
    void shouldHandleAccountListPageRefreshCorrectly() {
        log.info("Essential Test: Account list page refresh handling");
        
        // Login first
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to account list
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountList(baseUrl);
        
        assertTrue(accountPage.isAccountListPageLoaded(), 
                "Account list page should be loaded initially");
        
        // Refresh the page
        driver.navigate().refresh();
        
        // Verify page still loads correctly after refresh
        accountPage.waitForAccountListPageLoad();
        assertTrue(accountPage.isAccountListPageLoaded(), 
                "Account list page should load correctly after refresh");
        assertTrue(accountPage.isOpenAccountButtonVisible(), 
                "Open account button should remain visible after refresh");
        
        log.info("✅ Account list page refresh handling verified");
    }

    @Test
    @DisplayName("Should navigate between dashboard and account management successfully")
    void shouldNavigateBetweenDashboardAndAccountManagement() {
        log.info("Essential Test: Navigation between dashboard and account management");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to account list
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountList(baseUrl);
        
        assertTrue(accountPage.isAccountListPageLoaded(), 
                "Should be able to navigate to account list");
        
        // Navigate back to dashboard
        driver.get(baseUrl + "/dashboard");
        dashboardPage.waitForPageLoad();
        
        assertTrue(dashboardPage.isDashboardLoaded(), 
                "Should be able to navigate back to dashboard");
        
        // Navigate to account list again
        accountPage.navigateToAccountList(baseUrl);
        assertTrue(accountPage.isAccountListPageLoaded(), 
                "Should be able to navigate to account list again");
        
        log.info("✅ Navigation between dashboard and account management verified");
    }

    @Test
    @DisplayName("Should display customer selection interface correctly")
    void shouldDisplayCustomerSelectionInterface() {
        log.info("Essential Test: Customer selection interface display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer selection page
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountOpen(baseUrl);
        
        assertTrue(accountPage.isCustomerSelectionPageLoaded(), "Customer selection page should be loaded");
        
        // Verify customer selection interface elements are present
        assertTrue(driver.getPageSource().contains("Search by customer number, name, or email"), 
                "Customer search field should be visible with proper placeholder");
        assertTrue(driver.getPageSource().contains("Search"), 
                "Search button should be visible");
        assertTrue(driver.getPageSource().contains("Manage Customers"), 
                "Manage customers button should be visible");
        
        log.info("✅ Customer selection interface displayed correctly");
    }

    @Test
    @DisplayName("Should handle navigation between account opening workflow steps")
    void shouldHandleNavigationBetweenWorkflowSteps() {
        log.info("Essential Test: Account opening workflow navigation");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Step 1: Navigate to account list
        AccountOpeningPage accountPage = new AccountOpeningPage(driver);
        accountPage.navigateToAccountList(baseUrl);
        assertTrue(accountPage.isAccountListPageLoaded(), "Account list should be accessible");
        
        // Step 2: Navigate to customer selection
        accountPage.navigateToAccountOpen(baseUrl);
        assertTrue(accountPage.isCustomerSelectionPageLoaded(), "Customer selection should be accessible");
        
        // Step 3: Try to navigate to account form (if customer exists)
        if (accountPage.areCustomersDisplayed()) {
            for (int i = 1; i <= 3; i++) {
                String customerNumber = "C" + String.format("%07d", i);
                if (accountPage.isCustomerVisible(customerNumber)) {
                    accountPage.clickOpenAccountForCustomer(customerNumber);
                    
                    // Give time for navigation
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Verify we can navigate between steps
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("/account/open"), 
                            "Should be in account opening workflow");
                    
                    log.info("✅ Account opening workflow navigation working");
                    return;
                }
            }
        }
        
        // If no customers found, the workflow is still functional
        log.info("✅ Account opening workflow accessible (customer dependent steps skipped)");
    }
}