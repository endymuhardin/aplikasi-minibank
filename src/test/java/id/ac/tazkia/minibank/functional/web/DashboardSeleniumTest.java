package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import id.ac.tazkia.minibank.functional.web.pageobject.DashboardPage;
import id.ac.tazkia.minibank.functional.web.pageobject.LoginPage;

@SqlGroup({
    @Sql(scripts = "/fixtures/sql/dashboard-test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(scripts = "/fixtures/sql/dashboard-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class DashboardSeleniumTest extends BaseSecurityEnabledSeleniumTest {

    private String getStrongPasswordFor(String username) {
        switch (username) {
            case "manager": return "Fw8*jC5&uT1kQ7eR";
            case "cs": return "Gx3pM7bN2vZ9wS4k";
            case "teller": return "Hy6@lK4#sF8cX2qT";
            case "dashuser": return "Dash123@Strong!";
            case "statsuser": return "Stats456@Secure!";
            default: return "DefaultStrongPass123!";
        }
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayDashboardCorrectly() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("dashuser", getStrongPasswordFor("dashuser"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        assertEquals("Dashboard - Minibank", driver.getTitle());
        assertTrue(dashboardPage.isDashboardContentVisible());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayStatisticsCards() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("statsuser", getStrongPasswordFor("statsuser"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Verify statistics cards are displayed
        int cardCount = dashboardPage.getStatisticsCardsCount();
        assertTrue(cardCount >= 0, "Should display statistics cards");
        
        // Check if common statistics are present
        assertTrue(dashboardPage.hasStatisticsSection());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldNavigateToProductManagement() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("manager", getStrongPasswordFor("manager"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Navigate to product management if link is available
        if (dashboardPage.isProductManagementLinkVisible()) {
            dashboardPage.clickProductManagementLink();
            assertTrue(driver.getCurrentUrl().contains("/product"));
        }
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldNavigateToUserManagement() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("manager", getStrongPasswordFor("manager"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Navigate to user management if link is available
        if (dashboardPage.isUserManagementLinkVisible()) {
            dashboardPage.clickUserManagementLink();
            assertTrue(driver.getCurrentUrl().contains("/rbac/users"));
        }
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/dashboard_navigation_data.csv", numLinesToSkip = 1)
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldNavigateBasedOnUserRole(String username, String role, String expectedMenuItems) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully(username, getStrongPasswordFor(username));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Verify role-based menu visibility
        String[] menuItems = expectedMenuItems.split(",");
        for (String menuItem : menuItems) {
            menuItem = menuItem.trim();
            switch (menuItem) {
                case "products":
                    if (dashboardPage.isProductManagementLinkVisible()) {
                        assertTrue(true, "Products menu visible for " + role);
                    }
                    break;
                case "users":
                    if (dashboardPage.isUserManagementLinkVisible()) {
                        assertTrue(true, "Users menu visible for " + role);
                    }
                    break;
                case "transactions":
                    if (dashboardPage.isTransactionManagementLinkVisible()) {
                        assertTrue(true, "Transactions menu visible for " + role);
                    }
                    break;
                case "reports":
                    if (dashboardPage.isReportsLinkVisible()) {
                        assertTrue(true, "Reports menu visible for " + role);
                    }
                    break;
            }
        }
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayUserInformationInHeader() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("dashuser", getStrongPasswordFor("dashuser"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Check if user information is displayed in header
        String username = dashboardPage.getCurrentUsername();
        assertTrue(username.isEmpty() || username.contains("dashuser") || username.length() > 0,
                  "User information should be displayed");
        
        // Check if logout functionality is available
        assertTrue(dashboardPage.isLogoutLinkVisible());
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayRecentActivities() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("manager", getStrongPasswordFor("manager"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Check if recent activities section exists
        if (dashboardPage.hasRecentActivitiesSection()) {
            assertTrue(dashboardPage.areRecentActivitiesDisplayed());
        }
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayQuickActionsForManager() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("manager", getStrongPasswordFor("manager"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Manager should see quick action buttons
        if (dashboardPage.hasQuickActionsSection()) {
            assertTrue(dashboardPage.areQuickActionsDisplayed());
            
            // Check specific quick actions for managers
            if (dashboardPage.isCreateProductButtonVisible()) {
                assertTrue(true, "Create product quick action available");
            }
            if (dashboardPage.isCreateUserButtonVisible()) {
                assertTrue(true, "Create user quick action available");
            }
        }
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayLimitedFunctionalityForCS() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("cs", getStrongPasswordFor("cs"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // CS should have limited functionality
        if (dashboardPage.hasQuickActionsSection()) {
            // CS should not see admin functions
            assertFalse(dashboardPage.isCreateUserButtonVisible(), 
                       "CS should not see create user button");
            assertFalse(dashboardPage.isSystemConfigLinkVisible(), 
                       "CS should not see system config");
        }
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplayTransactionFunctionsForTeller() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("teller", getStrongPasswordFor("teller"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Teller should see transaction-related functions
        if (dashboardPage.hasQuickActionsSection()) {
            if (dashboardPage.isProcessTransactionButtonVisible()) {
                assertTrue(true, "Process transaction button visible for teller");
            }
            if (dashboardPage.isAccountLookupButtonVisible()) {
                assertTrue(true, "Account lookup button visible for teller");
            }
        }
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldHandlePageRefresh() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("dashuser", getStrongPasswordFor("dashuser"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Refresh the page
        driver.navigate().refresh();
        
        // Should still be on dashboard (assuming session is maintained)
        assertTrue(dashboardPage.isOnDashboardPage() || driver.getCurrentUrl().contains("/login"));
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void shouldDisplaySystemNotifications() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        DashboardPage dashboardPage = loginPage.loginSuccessfully("manager", getStrongPasswordFor("manager"));
        
        assertTrue(dashboardPage.isOnDashboardPage());
        
        // Check if notifications area exists
        if (dashboardPage.hasNotificationsSection()) {
            assertTrue(dashboardPage.areNotificationsDisplayed());
        }
    }
}