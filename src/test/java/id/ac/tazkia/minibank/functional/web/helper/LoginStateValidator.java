package id.ac.tazkia.minibank.functional.web.helper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates and recovers login state for optimized test execution.
 * Provides intelligent detection of authentication issues and automatic recovery.
 */
@Slf4j
public class LoginStateValidator {
    
    private final WebDriver driver;
    private final String baseUrl;
    private final WebDriverWait shortWait;
    
    public LoginStateValidator(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
    }
    
    /**
     * Comprehensive login state validation
     */
    public LoginStateResult validateLoginState(String expectedUsername, String expectedRole) {
        try {
            // Quick URL-based check
            String currentUrl = driver.getCurrentUrl();
            if (isOnLoginPage(currentUrl)) {
                return LoginStateResult.NOT_LOGGED_IN;
            }
            
            if (isOnErrorPage(currentUrl)) {
                return LoginStateResult.ERROR_STATE;
            }
            
            // Check for session expiration indicators
            if (hasSessionExpiredIndicators()) {
                return LoginStateResult.SESSION_EXPIRED;
            }
            
            // Try to access a protected resource to verify session
            if (!canAccessProtectedResource()) {
                return LoginStateResult.SESSION_INVALID;
            }
            
            // Verify user identity if on dashboard
            if (isOnDashboard(currentUrl)) {
                UserInfo userInfo = extractUserInfoFromDashboard();
                if (userInfo != null) {
                    if (!expectedUsername.equals(userInfo.username)) {
                        return LoginStateResult.WRONG_USER;
                    }
                    if (!expectedRole.equals(userInfo.role)) {
                        return LoginStateResult.WRONG_ROLE;
                    }
                    return LoginStateResult.VALID_SESSION;
                }
            }
            
            // If we can access protected resources but can't verify user info,
            // assume session is valid but navigate to dashboard for verification
            try {
                driver.get(baseUrl + "/dashboard");
                UserInfo userInfo = extractUserInfoFromDashboard();
                if (userInfo != null && 
                    expectedUsername.equals(userInfo.username) && 
                    expectedRole.equals(userInfo.role)) {
                    return LoginStateResult.VALID_SESSION;
                }
            } catch (Exception e) {
                log.debug("Dashboard verification failed: {}", e.getMessage());
            }
            
            return LoginStateResult.UNCERTAIN;
            
        } catch (Exception e) {
            log.debug("Login state validation failed: {}", e.getMessage());
            return LoginStateResult.VALIDATION_ERROR;
        }
    }
    
    /**
     * Attempt to recover from various login states
     */
    public boolean attemptRecovery(LoginStateResult state, String username, String password, String role) {
        log.info("🔧 LOGIN RECOVERY: Attempting recovery from state: {}", state);
        
        try {
            switch (state) {
                case NOT_LOGGED_IN:
                case SESSION_EXPIRED:
                case SESSION_INVALID:
                    return performFreshLogin(username, password, role);
                    
                case WRONG_USER:
                case WRONG_ROLE:
                    // Logout and login with correct credentials
                    attemptLogout();
                    return performFreshLogin(username, password, role);
                    
                case ERROR_STATE:
                    // Try to navigate away from error page
                    driver.get(baseUrl + "/login");
                    return performFreshLogin(username, password, role);
                    
                case VALIDATION_ERROR:
                    // Clear state and try fresh login
                    clearBrowserState();
                    return performFreshLogin(username, password, role);
                    
                case UNCERTAIN:
                    // Try to verify current state again
                    driver.get(baseUrl + "/dashboard");
                    LoginStateResult newState = validateLoginState(username, role);
                    if (newState == LoginStateResult.VALID_SESSION) {
                        return true;
                    }
                    return performFreshLogin(username, password, role);
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("❌ LOGIN RECOVERY: Recovery attempt failed: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean isOnLoginPage(String url) {
        return url.contains("/login");
    }
    
    private boolean isOnErrorPage(String url) {
        return url.contains("/error") || url.contains("/403") || url.contains("/404");
    }
    
    private boolean isOnDashboard(String url) {
        return url.contains("/dashboard");
    }
    
    private boolean hasSessionExpiredIndicators() {
        try {
            // Look for common session expiration messages
            WebElement sessionMessage = shortWait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[contains(text(), 'session') and contains(text(), 'expired')] | " +
                            "//div[contains(text(), 'Session') and contains(text(), 'expired')] | " +
                            "//div[contains(text(), 'logged out')] | " +
                            "//div[contains(text(), 'authentication required')]")
                )
            );
            return sessionMessage.isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
    
    private boolean canAccessProtectedResource() {
        try {
            String originalUrl = driver.getCurrentUrl();
            
            // Try to access a simple protected endpoint
            driver.get(baseUrl + "/api/customers"); // Should return 401/403 if not logged in
            
            // If we get redirected to login, session is invalid
            if (driver.getCurrentUrl().contains("/login")) {
                return false;
            }
            
            // Navigate back to original URL
            if (!originalUrl.equals(driver.getCurrentUrl())) {
                driver.get(originalUrl);
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private UserInfo extractUserInfoFromDashboard() {
        try {
            // Try to extract username from dashboard
            WebElement usernameElement = driver.findElement(By.id("current-username"));
            WebElement roleElement = driver.findElement(By.id("current-role"));
            
            if (usernameElement != null && roleElement != null) {
                return new UserInfo(usernameElement.getText().trim(), roleElement.getText().trim());
            }
        } catch (Exception e) {
            // Try alternative selectors
            try {
                WebElement userInfo = driver.findElement(By.cssSelector("[data-testid='user-info']"));
                String text = userInfo.getText();
                // Parse user info from text (format might be "Username (Role)")
                if (text.contains("(") && text.contains(")")) {
                    String username = text.substring(0, text.indexOf("(")).trim();
                    String role = text.substring(text.indexOf("(") + 1, text.indexOf(")")).trim();
                    return new UserInfo(username, role);
                }
            } catch (Exception e2) {
                log.debug("Could not extract user info from dashboard: {}", e2.getMessage());
            }
        }
        return null;
    }
    
    private boolean performFreshLogin(String username, String password, String role) {
        try {
            // Navigate to login page
            driver.get(baseUrl + "/login");
            
            // Fill login form
            WebElement usernameField = shortWait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));
            
            usernameField.clear();
            usernameField.sendKeys(username);
            passwordField.clear();
            passwordField.sendKeys(password);
            loginButton.click();
            
            // Wait for login to complete
            shortWait.until(ExpectedConditions.urlContains("/dashboard"));
            
            // Verify login success
            LoginStateResult result = validateLoginState(username, role);
            boolean success = result == LoginStateResult.VALID_SESSION;
            
            if (success) {
                log.info("✅ LOGIN RECOVERY: Fresh login successful for {} ({})", username, role);
            } else {
                log.error("❌ LOGIN RECOVERY: Fresh login failed for {} ({}), state: {}", username, role, result);
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("❌ LOGIN RECOVERY: Fresh login attempt failed: {}", e.getMessage());
            return false;
        }
    }
    
    private void attemptLogout() {
        try {
            // Try to find and click logout button
            WebElement logoutButton = driver.findElement(By.id("logout-button"));
            if (logoutButton.isDisplayed()) {
                logoutButton.click();
                shortWait.until(ExpectedConditions.urlContains("/login"));
            }
        } catch (Exception e) {
            // Logout failed, continue with other recovery methods
            log.debug("Logout attempt failed: {}", e.getMessage());
        }
    }
    
    private void clearBrowserState() {
        try {
            // Clear cookies and storage
            driver.manage().deleteAllCookies();
            driver.get("data:text/html,<html><body></body></html>");
        } catch (Exception e) {
            log.debug("Browser state clearing failed: {}", e.getMessage());
        }
    }
    
    /**
     * Result of login state validation
     */
    public enum LoginStateResult {
        VALID_SESSION,      // Session is valid and correct
        NOT_LOGGED_IN,      // Not logged in at all
        SESSION_EXPIRED,    // Session has expired
        SESSION_INVALID,    // Session exists but is invalid
        WRONG_USER,         // Logged in as wrong user
        WRONG_ROLE,         // Logged in with wrong role
        ERROR_STATE,        // On error page
        UNCERTAIN,          // Cannot determine state clearly
        VALIDATION_ERROR    // Error during validation
    }
    
    /**
     * User information extracted from dashboard
     */
    private static class UserInfo {
        final String username;
        final String role;
        
        UserInfo(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }
}