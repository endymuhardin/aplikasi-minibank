package id.ac.tazkia.minibank.functional.web.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openqa.selenium.WebDriver;

import id.ac.tazkia.minibank.functional.web.pageobject.DashboardPage;
import id.ac.tazkia.minibank.functional.web.pageobject.LoginPage;

/**
 * Helper class for handling login operations in Selenium tests.
 * Provides standard credentials and login methods for different user roles.
 */
public class LoginHelper {
    
    private final WebDriver driver;
    private final String baseUrl;
    
    public LoginHelper(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
    }
    
    /**
     * Login as a user with CUSTOMER_READ, ACCOUNT_READ, PRODUCT_READ permissions
     * (suitable for product management tests)
     */
    public DashboardPage loginAsCustomerServiceUser() {
        return loginWithCredentialsAndAssertSuccessWithRole("cs1", "minibank123", "Customer Service");
    }
    
    /**
     * Login as a user with TRANSACTION_READ, CUSTOMER_READ, ACCOUNT_READ permissions
     * (suitable for transaction tests)
     */
    public DashboardPage loginAsTeller() {
        return loginWithCredentialsAndAssertSuccessWithRole("teller1", "minibank123", "Teller");
    }
    
    /**
     * Login as a user with all permissions (manager role)
     * (suitable for admin/management tests)
     */
    public DashboardPage loginAsManager() {
        return loginWithCredentialsAndAssertSuccessWithRole("admin", "minibank123", "Branch Manager");
    }
    
    /**
     * Login as a user with USER_READ, USER_CREATE, USER_UPDATE permissions
     * (suitable for RBAC tests)
     */
    public DashboardPage loginAsUserManager() {
        return loginAsManager(); // Manager has all permissions including user management
    }
    
    /**
     * Login with specific credentials
     */
    public DashboardPage loginWithCredentials(String username, String password) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        return loginPage.loginSuccessfully(username, password);
    }
    
    /**
     * Login with specific credentials and assert authentication success
     */
    public DashboardPage loginWithCredentialsAndAssertSuccess(String username, String password) {
        DashboardPage dashboardPage = loginWithCredentials(username, password);
        
        // Assert that authentication succeeded by verifying we're on the dashboard
        assertTrue(dashboardPage.isOnDashboardPage(), 
            "Authentication failed - user was not redirected to dashboard after login with username: " + username);
        
        // Verify the correct user is logged in
        String actualUsername = dashboardPage.getCurrentUsername();
        assertEquals(username, actualUsername, 
            "Authentication succeeded but wrong user logged in. Expected: " + username + ", Actual: " + actualUsername);
        
        return dashboardPage;
    }
    
    /**
     * Login with specific credentials, assert authentication success, and verify expected role
     */
    public DashboardPage loginWithCredentialsAndAssertSuccessWithRole(String username, String password, String expectedRole) {
        DashboardPage dashboardPage = loginWithCredentialsAndAssertSuccess(username, password);
        
        // Verify the correct role is assigned
        String actualRole = dashboardPage.getCurrentUserRole();
        assertEquals(expectedRole, actualRole, 
            "Authentication succeeded but wrong role assigned. Expected: " + expectedRole + ", Actual: " + actualRole + " for user: " + username);
        
        return dashboardPage;
    }
    
    /**
     * Attempt login with specific credentials and assert authentication failure
     */
    public void loginWithCredentialsAndAssertFailure(String username, String password) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        // Use the expectingError method to stay on login page
        loginPage.login(username, password);
        
        // Assert that authentication failed by checking we're still on login page or error is displayed
        assertTrue(loginPage.isOnLoginPage() || loginPage.isErrorMessageDisplayed(), 
            "Authentication should have failed but user appears to have been logged in with username: " + username);
        
        // Additional assertion to ensure no successful dashboard redirect occurred
        assertFalse(driver.getCurrentUrl().contains("/dashboard"), 
            "Authentication failed assertion: user was unexpectedly redirected to dashboard with username: " + username);
    }
    
    /**
     * Get password for a given username (matches migration data setup)
     */
    public String getPasswordFor(String username) {
        // All migration users have the same password: minibank123
        if (username.matches("^(admin|manager[12]|teller[123]|cs[123])$")) {
            return "minibank123";
        }
        return "minibank123"; // Default for any migration user
    }
}