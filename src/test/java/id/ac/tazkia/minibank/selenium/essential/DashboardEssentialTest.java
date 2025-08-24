package id.ac.tazkia.minibank.selenium.essential;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
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
@DisplayName("Dashboard Essential Tests")
class DashboardEssentialTest extends BaseSeleniumTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/login-credentials-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should load dashboard successfully for all user roles")
    void shouldLoadDashboardForAllRoles(String username, String password, String expectedRole, String roleDescription) {
        log.info("Essential Test: Dashboard access for {}: {} with role {}", roleDescription, username, expectedRole);
        
        // Login first
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith(username, password);
        
        // Verify dashboard loads successfully
        assertTrue(dashboardPage.isDashboardLoaded(), 
                "Dashboard should load successfully for " + roleDescription);
        assertEquals("Dashboard", dashboardPage.getPageTitle(), 
                "Page title should be 'Dashboard' for " + roleDescription);
        
        // Verify statistics section is visible for all roles
        assertTrue(dashboardPage.isStatisticsSectionVisible(), 
                "Statistics section should be visible for all roles");
        
        log.info("✅ Dashboard loaded successfully for {}", roleDescription);
    }

    @Test
    @DisplayName("Should display role-specific navigation elements correctly")
    void shouldDisplayRoleSpecificNavigationElements() {
        log.info("Essential Test: Role-specific navigation elements");
        
        // Test Admin role - should have access to user management
        testRoleSpecificNavigation("admin", "minibank123", "ADMIN", "System Administrator",
                () -> {
                    LoginPage loginPage = new LoginPage(driver);
                    loginPage.navigateTo(baseUrl);
                    DashboardPage dashboard = loginPage.loginWith("admin", "minibank123");
                    
                    assertTrue(dashboard.isUserManagementVisible(), 
                            "Admin should see user management link");
                    assertTrue(dashboard.isProductManagementVisible(), 
                            "Admin should see product management link");
                    log.info("✅ Admin navigation elements verified");
                    return true;
                });
        
        // Test Manager role - should have product management but may not have all admin features  
        testRoleSpecificNavigation("manager1", "minibank123", "MANAGER", "Branch Manager",
                () -> {
                    LoginPage loginPage = new LoginPage(driver);
                    loginPage.navigateTo(baseUrl);
                    DashboardPage dashboard = loginPage.loginWith("manager1", "minibank123");
                    
                    assertTrue(dashboard.isProductManagementVisible(), 
                            "Manager should see product management link");
                    log.info("✅ Manager navigation elements verified");
                    return true;
                });
        
        // Test Teller role - should have transaction processing
        testRoleSpecificNavigation("teller1", "minibank123", "TELLER", "Bank Teller",
                () -> {
                    LoginPage loginPage = new LoginPage(driver);
                    loginPage.navigateTo(baseUrl);
                    DashboardPage dashboard = loginPage.loginWith("teller1", "minibank123");
                    
                    assertTrue(dashboard.isProcessTransactionButtonVisible(), 
                            "Teller should see process transaction button");
                    log.info("✅ Teller navigation elements verified");
                    return true;
                });
        
        // Test Customer Service role - should see products but not transactions
        testRoleSpecificNavigation("cs1", "minibank123", "CUSTOMER_SERVICE", "Customer Service",
                () -> {
                    LoginPage loginPage = new LoginPage(driver);
                    loginPage.navigateTo(baseUrl);
                    DashboardPage dashboard = loginPage.loginWith("cs1", "minibank123");
                    
                    assertTrue(dashboard.verifyCustomerServiceRoleElements(), 
                            "Customer Service should have appropriate access");
                    log.info("✅ Customer Service navigation elements verified");
                    return true;
                });
    }

    @Test
    @DisplayName("Should navigate from dashboard to main sections successfully")
    void shouldNavigateFromDashboardSuccessfully() {
        log.info("Essential Test: Dashboard navigation functionality");
        
        // Login as admin (has access to most features)
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded first");
        
        // Test navigation to product management
        if (dashboardPage.isProductManagementVisible()) {
            dashboardPage.clickProductManagement();
            
            // Verify navigation occurred (URL should change)
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("/product"), 
                    "Should navigate to product management section");
            
            // Navigate back to dashboard for next test
            driver.get(baseUrl + "/dashboard");
            dashboardPage.waitForPageLoad();
            log.info("✅ Product management navigation working");
        }
        
        // Test navigation to user management
        if (dashboardPage.isUserManagementVisible()) {
            dashboardPage.clickUserManagement();
            
            // Verify navigation occurred
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("/user") || currentUrl.contains("/rbac"), 
                    "Should navigate to user management section");
            
            log.info("✅ User management navigation working");
        }
        
        log.info("✅ Dashboard navigation functionality verified");
    }

    @Test
    @DisplayName("Should display essential dashboard components")
    void shouldDisplayEssentialDashboardComponents() {
        log.info("Essential Test: Dashboard components display");
        
        // Login as admin
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        // Verify essential dashboard components are present
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded");
        assertTrue(dashboardPage.isStatisticsSectionVisible(), "Statistics section should be visible");
        
        // Verify page title and basic structure
        assertEquals("Dashboard", dashboardPage.getPageTitle(), "Page title should be correct");
        
        log.info("✅ Essential dashboard components verified");
    }

    @Test
    @DisplayName("Should handle dashboard refresh correctly")
    void shouldHandleDashboardRefreshCorrectly() {
        log.info("Essential Test: Dashboard refresh handling");
        
        // Login first
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(baseUrl);
        DashboardPage dashboardPage = loginPage.loginWith("admin", "minibank123");
        
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded initially");
        
        // Refresh the page
        driver.navigate().refresh();
        
        // Verify dashboard still loads correctly after refresh
        dashboardPage.waitForPageLoad();
        assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should load correctly after refresh");
        assertEquals("Dashboard", dashboardPage.getPageTitle(), "Page title should remain correct after refresh");
        
        log.info("✅ Dashboard refresh handling verified");
    }

    /**
     * Helper method to test role-specific navigation
     */
    private void testRoleSpecificNavigation(String username, String password, String role, 
                                          String roleDescription, NavigationTest test) {
        try {
            log.info("Testing navigation for {}: {}", roleDescription, role);
            boolean result = test.execute();
            assertTrue(result, "Navigation test should pass for " + roleDescription);
        } catch (Exception e) {
            log.error("Navigation test failed for {}: {}", roleDescription, e.getMessage());
            fail("Navigation test failed for " + roleDescription + ": " + e.getMessage());
        }
    }
    
    /**
     * Functional interface for navigation tests
     */
    @FunctionalInterface
    private interface NavigationTest {
        boolean execute() throws Exception;
    }
}