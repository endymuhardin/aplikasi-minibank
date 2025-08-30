package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginPage {
    
    private final Page page;
    
    // Login form elements using IDs exclusively
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;
    private final Locator successMessage;
    private final Locator pageTitle;
    
    public LoginPage(Page page) {
        this.page = page;
        
        // Initialize locators using IDs exclusively (following Selenium pattern)
        this.usernameInput = page.locator("#username");
        this.passwordInput = page.locator("#password");
        this.loginButton = page.locator("#login-button");
        this.errorMessage = page.locator("#error-message");
        this.successMessage = page.locator("#success-message");
        this.pageTitle = page.locator("#page-title");
    }
    
    /**
     * Navigate to login page
     */
    public LoginPage navigateTo(String baseUrl) {
        page.navigate(baseUrl + "/login");
        waitForPageLoad();
        return this;
    }
    
    /**
     * Wait for login page to load completely
     */
    public LoginPage waitForPageLoad() {
        usernameInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        passwordInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        loginButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        log.debug("Login page loaded successfully");
        return this;
    }
    
    /**
     * Check if login page is loaded
     */
    public boolean isLoginPageLoaded() {
        try {
            return page.url().contains("/login") &&
                   usernameInput.isVisible() &&
                   passwordInput.isVisible() &&
                   loginButton.isVisible();
        } catch (Exception e) {
            log.debug("Login page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Perform login with username and password
     */
    public DashboardPage loginWith(String username, String password) {
        waitForPageLoad();
        
        // Clear and fill username
        usernameInput.clear();
        usernameInput.fill(username);
        
        // Clear and fill password
        passwordInput.clear();
        passwordInput.fill(password);
        
        // Click login button
        loginButton.click();
        
        log.debug("Logged in with username: {}", username);
        
        // Return dashboard page object
        return new DashboardPage(page);
    }
    
    /**
     * Check if error message is visible
     */
    public boolean isErrorMessageVisible() {
        try {
            return errorMessage.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get error message text
     */
    public String getErrorMessage() {
        if (isErrorMessageVisible()) {
            return errorMessage.textContent();
        }
        return "";
    }
    
    /**
     * Check if success message is visible
     */
    public boolean isSuccessMessageVisible() {
        try {
            return successMessage.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get success message text
     */
    public String getSuccessMessage() {
        if (isSuccessMessageVisible()) {
            return successMessage.textContent();
        }
        return "";
    }
    
    /**
     * Clear login form
     */
    public LoginPage clearForm() {
        usernameInput.clear();
        passwordInput.clear();
        log.debug("Login form cleared");
        return this;
    }
    
    /**
     * Fill username field
     */
    public LoginPage fillUsername(String username) {
        usernameInput.clear();
        usernameInput.fill(username);
        log.debug("Username field filled");
        return this;
    }
    
    /**
     * Fill password field
     */
    public LoginPage fillPassword(String password) {
        passwordInput.clear();
        passwordInput.fill(password);
        log.debug("Password field filled");
        return this;
    }
    
    /**
     * Click login button
     */
    public LoginPage clickLogin() {
        loginButton.click();
        log.debug("Login button clicked");
        return this;
    }
    
    /**
     * Check if login button is enabled
     */
    public boolean isLoginButtonEnabled() {
        try {
            return loginButton.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get page title text
     */
    public String getPageTitle() {
        try {
            if (pageTitle.isVisible()) {
                return pageTitle.textContent();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
}