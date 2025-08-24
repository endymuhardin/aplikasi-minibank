package id.ac.tazkia.minibank.selenium.essential;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
import id.ac.tazkia.minibank.selenium.pages.CustomerManagementPage;
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
@DisplayName("Customer Management Essential Tests")
class CustomerManagementEssentialTest extends BaseSeleniumTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/login-credentials-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should load customer management page successfully for all user roles")
    void shouldLoadCustomerManagementPageForAllRoles(String username, String password, String expectedRole, String roleDescription) {
        log.info("Essential Test: Customer management page access for {}: {} with role {}", roleDescription, username, expectedRole);
        
        // Login first
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith(username, password);
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer management
        CustomerManagementPage customerPage = new CustomerManagementPage(driver);
        customerPage.navigateTo(baseUrl);
        
        // Verify customer management page loads successfully
        assertTrue(customerPage.isCustomerManagementPageLoaded(), 
                "Customer management page should load successfully for " + roleDescription);
        
        // Verify essential page elements are visible
        assertTrue(customerPage.isCreateCustomerButtonVisible(), 
                "Create customer button should be visible for " + roleDescription);
        
        log.info("✅ Customer management page loaded successfully for {}", roleDescription);
    }

    @Test
    @DisplayName("Should display customer type selection page correctly")
    void shouldDisplayCustomerTypeSelectionPage() {
        log.info("Essential Test: Customer type selection page");
        
        // Login as admin (has full access)
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer type selection page
        CustomerManagementPage customerPage = new CustomerManagementPage(driver);
        customerPage.navigateToCreateCustomer(baseUrl);
        
        // Verify customer type selection page loads
        assertTrue(customerPage.isCustomerTypeSelectionPageLoaded(), 
                "Customer type selection page should load correctly");
        
        // Verify both customer type options are available
        assertTrue(driver.getPageSource().contains("Personal Customer"), 
                "Personal customer option should be available");
        assertTrue(driver.getPageSource().contains("Corporate Customer"), 
                "Corporate customer option should be available");
        
        log.info("✅ Customer type selection page displayed correctly");
    }

    @Test
    @DisplayName("Should navigate to personal customer creation successfully")
    void shouldNavigateToPersonalCustomerCreation() {
        log.info("Essential Test: Personal customer creation navigation");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer type selection and click personal customer
        CustomerManagementPage customerPage = new CustomerManagementPage(driver);
        customerPage.navigateToCreateCustomer(baseUrl);
        
        assertTrue(customerPage.isCustomerTypeSelectionPageLoaded(), 
                "Customer type selection page should be loaded");
        
        // Click create personal customer
        customerPage.clickCreatePersonalCustomer();
        
        // Verify navigation to personal customer form
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/customer/create/personal"), 
                "Should navigate to personal customer creation form");
        
        log.info("✅ Personal customer creation navigation working");
    }

    @Test
    @DisplayName("Should navigate to corporate customer creation successfully")
    void shouldNavigateToCorporateCustomerCreation() {
        log.info("Essential Test: Corporate customer creation navigation");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer type selection and click corporate customer
        CustomerManagementPage customerPage = new CustomerManagementPage(driver);
        customerPage.navigateToCreateCustomer(baseUrl);
        
        assertTrue(customerPage.isCustomerTypeSelectionPageLoaded(), 
                "Customer type selection page should be loaded");
        
        // Click create corporate customer
        customerPage.clickCreateCorporateCustomer();
        
        // Verify navigation to corporate customer form
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/customer/create/corporate"), 
                "Should navigate to corporate customer creation form");
        
        log.info("✅ Corporate customer creation navigation working");
    }

    @Test
    @DisplayName("Should display search and filter elements correctly")
    void shouldDisplaySearchAndFilterElements() {
        log.info("Essential Test: Customer search and filter elements display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer management
        CustomerManagementPage customerPage = new CustomerManagementPage(driver);
        customerPage.navigateTo(baseUrl);
        
        assertTrue(customerPage.isCustomerManagementPageLoaded(), "Customer management page should be loaded");
        
        // Verify search and filter elements are visible
        assertTrue(driver.getPageSource().contains("Search"), "Search field should be visible");
        assertTrue(driver.getPageSource().contains("All Types"), "Customer type filter should be visible");
        assertTrue(driver.getPageSource().contains("Search by customer number or email"), 
                "Search placeholder text should be visible");
        
        log.info("✅ Customer search and filter elements displayed correctly");
    }

    @Test
    @DisplayName("Should display customer list with essential elements")
    void shouldDisplayCustomerListWithEssentialElements() {
        log.info("Essential Test: Customer list essential elements display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer management
        CustomerManagementPage customerPage = new CustomerManagementPage(driver);
        customerPage.navigateTo(baseUrl);
        
        assertTrue(customerPage.isCustomerManagementPageLoaded(), "Customer management page should be loaded");
        
        // Verify essential table elements are present
        assertTrue(driver.getPageSource().contains("Customer Number"), 
                "Customer table should have Customer Number column");
        assertTrue(driver.getPageSource().contains("Name"), 
                "Customer table should have Name column");
        assertTrue(driver.getPageSource().contains("Email"), 
                "Customer table should have Email column");
        assertTrue(driver.getPageSource().contains("Type"), 
                "Customer table should have Type column");
        assertTrue(driver.getPageSource().contains("Status"), 
                "Customer table should have Status column");
        assertTrue(driver.getPageSource().contains("Actions"), 
                "Customer table should have Actions column");
        
        log.info("✅ Customer list essential elements displayed correctly");
    }

    @Test
    @DisplayName("Should handle customer management page refresh correctly")
    void shouldHandleCustomerManagementPageRefreshCorrectly() {
        log.info("Essential Test: Customer management page refresh handling");
        
        // Login first
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer management
        CustomerManagementPage customerPage = new CustomerManagementPage(driver);
        customerPage.navigateTo(baseUrl);
        
        assertTrue(customerPage.isCustomerManagementPageLoaded(), 
                "Customer management page should be loaded initially");
        
        // Refresh the page
        driver.navigate().refresh();
        
        // Verify page still loads correctly after refresh
        customerPage.waitForPageLoad();
        assertTrue(customerPage.isCustomerManagementPageLoaded(), 
                "Customer management page should load correctly after refresh");
        assertTrue(customerPage.isCreateCustomerButtonVisible(), 
                "Create customer button should remain visible after refresh");
        
        log.info("✅ Customer management page refresh handling verified");
    }

    @Test
    @DisplayName("Should navigate back to customer list from dashboard successfully")
    void shouldNavigateBackToCustomerListFromDashboard() {
        log.info("Essential Test: Navigation from dashboard to customer management");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Navigate to customer management from dashboard (if link exists)
        // First check if there's a direct link from dashboard
        boolean hasDirectCustomerLink = driver.getPageSource().contains("customer");
        
        // Navigate to customer management directly
        CustomerManagementPage customerPage = new CustomerManagementPage(driver);
        customerPage.navigateTo(baseUrl);
        
        assertTrue(customerPage.isCustomerManagementPageLoaded(), 
                "Should be able to navigate to customer management");
        
        // Navigate back to dashboard
        driver.get(baseUrl + "/dashboard");
        dashboardPage.waitForPageLoad();
        
        assertTrue(dashboardPage.isDashboardLoaded(), 
                "Should be able to navigate back to dashboard");
        
        // Navigate to customer management again
        customerPage.navigateTo(baseUrl);
        assertTrue(customerPage.isCustomerManagementPageLoaded(), 
                "Should be able to navigate to customer management again");
        
        log.info("✅ Navigation between dashboard and customer management verified");
    }
}