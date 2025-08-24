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
        log.info("Navigating to login page: {}", loginUrl);
        driver.get(loginUrl);
        waitForPageLoad();
        return this;
    }
    
    /**
     * Wait for login page to load completely
     */
    public LoginPage waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        wait.until(ExpectedConditions.visibilityOf(passwordField));
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        log.debug("Login page loaded successfully");
        return this;
    }
    
    /**
     * Enter username
     */
    public LoginPage enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
        log.debug("Entered username: {}", username);
        return this;
    }
    
    /**
     * Enter password
     */
    public LoginPage enterPassword(String password) {
        wait.until(ExpectedConditions.visibilityOf(passwordField));
        passwordField.clear();
        passwordField.sendKeys(password);
        log.debug("Entered password");
        return this;
    }
    
    /**
     * Click login button
     */
    public LoginPage clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();
        log.debug("Clicked login button");
        return this;
    }
    
    /**
     * Complete login process with username and password
     */
    public DashboardPage loginWith(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
        
        log.info("Attempting login with username: {}", username);
        
        // Wait for either dashboard or error message
        try {
            wait.until(driver -> 
                driver.getCurrentUrl().contains("/dashboard") || 
                isErrorMessageVisible()
            );
            
            if (driver.getCurrentUrl().contains("/dashboard")) {
                log.info("Login successful, redirected to dashboard");
                return new DashboardPage(driver);
            } else {
                log.warn("Login failed, error message displayed");
                throw new RuntimeException("Login failed: " + getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Login process failed: {}", e.getMessage());
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