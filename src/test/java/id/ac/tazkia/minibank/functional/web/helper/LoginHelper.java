package id.ac.tazkia.minibank.functional.web.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openqa.selenium.WebDriver;
import java.util.HashMap;
import java.util.Map;

import id.ac.tazkia.minibank.functional.web.pageobject.DashboardPage;
import id.ac.tazkia.minibank.functional.web.pageobject.LoginPage;
import lombok.extern.slf4j.Slf4j;

/**
 * Login helper that implements session-aware authentication caching
 * to significantly improve test execution speed by avoiding redundant logins.
 */
@Slf4j
public class LoginHelper {
    
    private final WebDriver driver;
    private final String baseUrl;
    
    // Session state management
    private static String currentLoggedInUser = null;
    private static String currentUserRole = null;
    private static boolean sessionValid = false;
    private static long lastLoginTime = 0;
    private static final long SESSION_TIMEOUT_MS = 300000; // 5 minutes
    
    // Role-based user mappings for faster lookup
    private static final Map<String, UserCredentials> ROLE_USERS = new HashMap<>();
    static {
        ROLE_USERS.put("CUSTOMER_SERVICE", new UserCredentials("cs1", "minibank123", "Customer Service"));
        ROLE_USERS.put("TELLER", new UserCredentials("teller1", "minibank123", "Teller"));
        ROLE_USERS.put("MANAGER", new UserCredentials("admin", "minibank123", "Branch Manager"));
        ROLE_USERS.put("USER_MANAGER", new UserCredentials("admin", "minibank123", "Branch Manager")); // Manager has all permissions
    }
    
    public LoginHelper(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
    }
    
    /**
     * Login as Customer Service user with session caching
     */
    public DashboardPage loginAsCustomerServiceUser() {
        return loginWithRole("CUSTOMER_SERVICE");
    }
    
    /**
     * Login as Teller with session caching
     */
    public DashboardPage loginAsTeller() {
        return loginWithRole("TELLER");
    }
    
    /**
     * Login as Manager with session caching
     */
    public DashboardPage loginAsManager() {
        return loginWithRole("MANAGER");
    }
    
    /**
     * Login as User Manager with session caching
     */
    public DashboardPage loginAsUserManager() {
        return loginWithRole("USER_MANAGER");
    }
    
    /**
     * Optimized role-based login with session awareness
     */
    public DashboardPage loginWithRole(String role) {
        UserCredentials credentials = ROLE_USERS.get(role);
        if (credentials == null) {
            throw new IllegalArgumentException("Unknown role: " + role);
        }
        
        // Check if we're already logged in with the right user and role
        if (isValidSession(credentials.username, credentials.role)) {
            log.info("🔄 LOGIN OPTIMIZATION: Reusing existing session for {} ({})", credentials.username, role);
            return new DashboardPage(driver, baseUrl);
        }
        
        // Perform fresh login
        log.info("🔑 LOGIN OPTIMIZATION: Performing fresh login for {} ({})", credentials.username, role);
        return performOptimizedLogin(credentials);
    }
    
    /**
     * Check if current session is valid for the requested user and role
     */
    private boolean isValidSession(String requiredUsername, String requiredRole) {
        // Check session timeout
        if (System.currentTimeMillis() - lastLoginTime > SESSION_TIMEOUT_MS) {
            log.debug("Session expired due to timeout");
            invalidateSession();
            return false;
        }
        
        // Check if session state matches requirements
        if (!sessionValid || 
            !requiredUsername.equals(currentLoggedInUser) || 
            !requiredRole.equals(currentUserRole)) {
            return false;
        }
        
        // Verify we're actually on a valid page (not logged out)
        try {
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("/login") || currentUrl.contains("/error")) {
                log.debug("Session invalid - redirected to login/error page");
                invalidateSession();
                return false;
            }
            
            // Quick validation - check if we can access a protected page
            if (currentUrl.contains("/dashboard") || currentUrl.contains("/product") || 
                currentUrl.contains("/customer") || currentUrl.contains("/account")) {
                return true;
            }
            
            // Try to verify session by checking dashboard accessibility
            return verifyDashboardAccess();
            
        } catch (Exception e) {
            log.debug("Session validation failed: {}", e.getMessage());
            invalidateSession();
            return false;
        }
    }
    
    /**
     * Verify dashboard access without full navigation
     */
    private boolean verifyDashboardAccess() {
        try {
            // Try to access dashboard quickly
            String originalUrl = driver.getCurrentUrl();
            driver.get(baseUrl + "/dashboard");
            
            // Check if we got redirected to login
            if (driver.getCurrentUrl().contains("/login")) {
                log.debug("Dashboard access failed - redirected to login");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.debug("Dashboard verification failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Perform optimized login with minimal overhead
     */
    private DashboardPage performOptimizedLogin(UserCredentials credentials) {
        try {
            // Navigate to login page only if not already there
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/login")) {
                driver.get(baseUrl + "/login");
            }
            
            LoginPage loginPage = new LoginPage(driver);
            DashboardPage dashboardPage = loginPage.loginSuccessfully(credentials.username, credentials.password);
            
            // Verify login success efficiently
            if (!dashboardPage.isOnDashboardPage()) {
                throw new RuntimeException("Login failed - not redirected to dashboard");
            }
            
            // Update session state
            updateSessionState(credentials.username, credentials.role);
            
            log.info("✅ LOGIN OPTIMIZATION: Successfully logged in as {} ({})", credentials.username, credentials.role);
            return dashboardPage;
            
        } catch (Exception e) {
            log.error("❌ LOGIN OPTIMIZATION: Login failed for {} ({}): {}", 
                     credentials.username, credentials.role, e.getMessage());
            invalidateSession();
            throw new RuntimeException("Optimized login failed", e);
        }
    }
    
    /**
     * Update session state after successful login
     */
    private void updateSessionState(String username, String role) {
        currentLoggedInUser = username;
        currentUserRole = role;
        sessionValid = true;
        lastLoginTime = System.currentTimeMillis();
    }
    
    /**
     * Invalidate current session state
     */
    private void invalidateSession() {
        currentLoggedInUser = null;
        currentUserRole = null;
        sessionValid = false;
        lastLoginTime = 0;
    }
    
    /**
     * Force logout and session invalidation
     */
    public void logout() {
        try {
            // Try to logout if possible
            if (!driver.getCurrentUrl().contains("/login")) {
                DashboardPage dashboardPage = new DashboardPage(driver, baseUrl);
                if (dashboardPage.isOnDashboardPage()) {
                    dashboardPage.logout();
                }
            }
        } catch (Exception e) {
            log.debug("Logout attempt failed, but continuing: {}", e.getMessage());
        } finally {
            invalidateSession();
            log.info("🔓 LOGIN OPTIMIZATION: Session invalidated and user logged out");
        }
    }
    
    /**
     * Get current session information
     */
    public String getCurrentSessionInfo() {
        if (sessionValid) {
            return String.format("User: %s, Role: %s, Age: %dms", 
                               currentLoggedInUser, 
                               currentUserRole, 
                               System.currentTimeMillis() - lastLoginTime);
        }
        return "No active session";
    }
    
    /**
     * Check if session is currently valid
     */
    public boolean hasValidSession() {
        return isValidSession(currentLoggedInUser, currentUserRole);
    }
    
    /**
     * Legacy compatibility methods - delegate to optimized versions
     */
    public DashboardPage loginWithCredentials(String username, String password) {
        // Find matching role for legacy calls
        for (Map.Entry<String, UserCredentials> entry : ROLE_USERS.entrySet()) {
            UserCredentials creds = entry.getValue();
            if (creds.username.equals(username)) {
                return loginWithRole(entry.getKey());
            }
        }
        
        // Fallback to direct login for unknown users
        invalidateSession();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        return loginPage.loginSuccessfully(username, password);
    }
    
    public DashboardPage loginWithCredentialsAndAssertSuccess(String username, String password) {
        DashboardPage dashboardPage = loginWithCredentials(username, password);
        
        assertTrue(dashboardPage.isOnDashboardPage(), 
            "Authentication failed - user was not redirected to dashboard after login with username: " + username);
        
        String actualUsername = dashboardPage.getCurrentUsername();
        assertEquals(username, actualUsername, 
            "Authentication succeeded but wrong user logged in. Expected: " + username + ", Actual: " + actualUsername);
        
        return dashboardPage;
    }
    
    public DashboardPage loginWithCredentialsAndAssertSuccessWithRole(String username, String password, String expectedRole) {
        DashboardPage dashboardPage = loginWithCredentialsAndAssertSuccess(username, password);
        
        String actualRole = dashboardPage.getCurrentUserRole();
        assertEquals(expectedRole, actualRole, 
            "Authentication succeeded but wrong role assigned. Expected: " + expectedRole + ", Actual: " + actualRole + " for user: " + username);
        
        return dashboardPage;
    }
    
    public void loginWithCredentialsAndAssertFailure(String username, String password) {
        invalidateSession(); // Clear any existing session
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLogin(baseUrl);
        
        loginPage.login(username, password);
        
        assertTrue(loginPage.isOnLoginPage() || loginPage.isErrorMessageDisplayed(), 
            "Authentication should have failed but user appears to have been logged in with username: " + username);
        
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
    
    /**
     * Internal class to hold user credentials
     */
    private static class UserCredentials {
        final String username;
        final String password;
        final String role;
        
        UserCredentials(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }
    }
}