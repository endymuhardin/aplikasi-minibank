package id.ac.tazkia.minibank.functional.web.pageobject;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

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
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        PageFactory.initElements(driver, this);
    }
    
    public void navigateToLogin(String baseUrl) {
        driver.get(baseUrl + "/login");
        waitForPageLoad();
    }
    
    public void enterUsername(String username) {
        wait.until(ExpectedConditions.elementToBeClickable(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
    }
    
    public void enterPassword(String password) {
        wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        passwordField.clear();
        if (password != null && !password.isEmpty()) {
            passwordField.sendKeys(password);
        }
    }
    
    public void checkRememberMe() {
        if (!rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
        }
    }
    
    public void uncheckRememberMe() {
        if (rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
        }
    }
    
    public DashboardPage clickLogin() {
        log.info("About to click login button");
        
        // Check if there are any CSRF tokens or hidden fields
        try {
            var hiddenInputs = driver.findElements(By.xpath("//input[@type='hidden']"));
            log.info("Found {} hidden input fields in the form", hiddenInputs.size());
            for (var input : hiddenInputs) {
                String name = input.getAttribute("name");
                String value = input.getAttribute("value");
                log.info("Hidden field: {}={}", name, value);
            }
        } catch (Exception e) {
            log.warn("Could not check hidden fields: {}", e.getMessage());
        }
        
        loginButton.click();
        log.info("Login button clicked");
        
        // Wait a moment for the form submission and redirect
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/product/list"),
                ExpectedConditions.urlContains("/login?error"),
                ExpectedConditions.visibilityOf(errorMessage)
            ));
        } catch (Exception e) {
            log.error("Login wait condition failed: {}", e.getMessage());
            String currentUrl = driver.getCurrentUrl();
            log.error("Current URL after login attempt: {}", currentUrl);
            
            // Check if there's an error message
            if (isErrorMessageDisplayed()) {
                String error = getErrorMessage();
                log.error("Login error message displayed: {}", error);
            } else {
                log.info("No error message displayed");
            }
        }
        return new DashboardPage(driver);
    }
    
    public void clickLoginExpectingError() {
        loginButton.click();
        waitForErrorMessage();
    }
    
    public LoginPage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginExpectingError();
        return this;
    }
    
    public DashboardPage loginSuccessfully(String username, String password) {
        log.info("Attempting login with username: {}", username);
        enterUsername(username);
        enterPassword(password);
        
        String urlBeforeLogin = driver.getCurrentUrl();
        log.info("URL before login: {}", urlBeforeLogin);
        
        DashboardPage result = clickLogin();
        
        String urlAfterLogin = driver.getCurrentUrl();
        log.info("URL after login: {}", urlAfterLogin);
        
        return result;
    }
    
    public String getErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    public String getSuccessMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isSuccessMessageDisplayed() {
        try {
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isUsernameFieldEmpty() {
        return usernameField.getAttribute("value").isEmpty();
    }
    
    public boolean isPasswordFieldEmpty() {
        return passwordField.getAttribute("value").isEmpty();
    }
    
    public boolean isLoginButtonEnabled() {
        return loginButton.isEnabled();
    }
    
    public boolean isOnLoginPage() {
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.titleContains("Login")
            ));
            return driver.getCurrentUrl().contains("/login");
        } catch (Exception e) {
            return false;
        }
    }
    
    public void clickForgotPassword() {
        forgotPasswordLink.click();
    }
    
    public boolean isForgotPasswordLinkDisplayed() {
        try {
            return forgotPasswordLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    private void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
    }
    
    private void waitForErrorMessage() {
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOf(errorMessage),
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/login?error")
            ));
        } catch (Exception e) {
            // Timeout is acceptable here
        }
    }
}