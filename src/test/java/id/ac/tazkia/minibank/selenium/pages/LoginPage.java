package id.ac.tazkia.minibank.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class LoginPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    @FindBy(id = "username")
    private WebElement usernameField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(id = "login-button")
    private WebElement loginButton;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "remember-me")
    private WebElement rememberMeCheckbox;
    
    @FindBy(id = "forgot-password-link")
    private WebElement forgotPasswordLink;
    
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to login page
     */
    public LoginPage navigateTo(String baseUrl) {
        String loginUrl = baseUrl + "/login";
        log.info("🌐 NAVIGATE → Login Page: {}", loginUrl);
        driver.get(loginUrl);
        waitForPageLoad();
        log.info("✅ LOGIN PAGE LOADED - Current URL: {} | Title: {}", 
                driver.getCurrentUrl(), driver.getTitle());
        return this;
    }
    
    /**
     * Wait for login page to load completely
     */
    public LoginPage waitForPageLoad() {
        log.debug("⏳ WAITING → Login page elements to load...");
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        wait.until(ExpectedConditions.visibilityOf(passwordField));
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        log.debug("✅ LOGIN ELEMENTS LOADED - Username field, password field, and login button ready");
        return this;
    }
    
    /**
     * Enter username
     */
    public LoginPage enterUsername(String username) {
        log.debug("📝 ACTION → Entering username: {}", username);
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
        log.debug("✅ USERNAME ENTERED");
        return this;
    }
    
    /**
     * Enter password
     */
    public LoginPage enterPassword(String password) {
        log.debug("🔒 ACTION → Entering password");
        wait.until(ExpectedConditions.visibilityOf(passwordField));
        passwordField.clear();
        passwordField.sendKeys(password);
        log.debug("✅ PASSWORD ENTERED");
        return this;
    }
    
    /**
     * Click login button
     */
    public LoginPage clickLogin() {
        log.info("🖱️ ACTION → Clicking login button");
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();
        log.info("✅ LOGIN BUTTON CLICKED - Waiting for authentication...");
        return this;
    }
    
    /**
     * Complete login process with username and password
     */
    public DashboardPage loginWith(String username, String password) {
        log.info("🔐 LOGIN FLOW START → User: {} | Current URL: {}", username, driver.getCurrentUrl());
        
        enterUsername(username);
        enterPassword(password);
        clickLogin();
        
        log.info("⏳ AUTHENTICATION → Waiting for login response...");
        
        // Wait for either dashboard or error message
        try {
            wait.until(driver -> 
                driver.getCurrentUrl().contains("/dashboard") || 
                isErrorMessageVisible()
            );
            
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("/dashboard")) {
                log.info("🎉 LOGIN SUCCESS → Redirected to dashboard: {} | Title: {}", currentUrl, driver.getTitle());
                return new DashboardPage(driver);
            } else {
                String errorMsg = getErrorMessage();
                log.warn("❌ LOGIN FAILED → Error: {} | Current URL: {}", errorMsg, currentUrl);
                throw new RuntimeException("Login failed: " + errorMsg);
            }
        } catch (Exception e) {
            log.error("💥 LOGIN ERROR → Exception during authentication: {} | Current URL: {}", 
                    e.getMessage(), driver.getCurrentUrl());
            throw new RuntimeException("Login process failed", e);
        }
    }
    
    /**
     * Check if error message is visible
     */
    public boolean isErrorMessageVisible() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get error message text
     */
    public String getErrorMessage() {
        if (isErrorMessageVisible()) {
            return errorMessage.getText();
        }
        return "";
    }
    
    /**
     * Check if success message is visible
     */
    public boolean isSuccessMessageVisible() {
        try {
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get success message text
     */
    public String getSuccessMessage() {
        if (isSuccessMessageVisible()) {
            return successMessage.getText();
        }
        return "";
    }
    
    /**
     * Check remember me checkbox
     */
    public LoginPage checkRememberMe() {
        if (!rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
            log.debug("Checked remember me checkbox");
        }
        return this;
    }
    
    /**
     * Click forgot password link
     */
    public LoginPage clickForgotPassword() {
        forgotPasswordLink.click();
        log.debug("Clicked forgot password link");
        return this;
    }
    
    /**
     * Check if login form is displayed
     */
    public boolean isLoginFormVisible() {
        try {
            return usernameField.isDisplayed() && 
                   passwordField.isDisplayed() && 
                   loginButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}