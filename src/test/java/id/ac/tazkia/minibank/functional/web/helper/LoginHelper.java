package id.ac.tazkia.minibank.functional.web.helper;

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
        return loginWithCredentials("cs1", "minibank123");
    }
    
    /**
     * Login as a user with TRANSACTION_READ, CUSTOMER_READ, ACCOUNT_READ permissions
     * (suitable for transaction tests)
     */
    public DashboardPage loginAsTeller() {
        return loginWithCredentials("teller1", "minibank123");
    }
    
    /**
     * Login as a user with all permissions (manager role)
     * (suitable for admin/management tests)
     */
    public DashboardPage loginAsManager() {
        return loginWithCredentials("admin", "minibank123");
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