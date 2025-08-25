package id.ac.tazkia.minibank.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;

@Slf4j
public class AccountManagementPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Account List Page Elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "open-account-button")
    private WebElement openAccountButton;
    
    @FindBy(id = "accounts-table")
    private WebElement accountsTable;
    
    @FindBy(id = "search-accounts-btn")
    private WebElement searchButton;
    
    @FindBy(id = "no-accounts-message")
    private WebElement noAccountsMessage;
    
    // Account Closure Form Elements
    @FindBy(id = "reason")
    private WebElement reasonTextarea;
    
    @FindBy(id = "confirm")
    private WebElement confirmCheckbox;
    
    @FindBy(xpath = "//button[@type='submit']")
    private WebElement closeAccountButton;
    
    @FindBy(xpath = "//a[contains(@href, '/account/list')]")
    private WebElement cancelButton;
    
    // Success/Error Messages
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    @FindBy(xpath = "//div[contains(@class, 'bg-red-50')]")
    private WebElement balanceError;
    
    public AccountManagementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void navigateToAccountList(String baseUrl) {
        String accountListUrl = baseUrl + "/account/list";
        log.info("Navigating to account list: {}", accountListUrl);
        driver.get(accountListUrl);
        waitForPageLoad();
    }

    public void navigateToAccountClosure(String baseUrl, String accountId) {
        String closureUrl = baseUrl + "/account/" + accountId + "/close";
        log.info("Navigating to account closure: {}", closureUrl);
        driver.get(closureUrl);
        waitForPageLoad();
    }

    public boolean isAccountListPageLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pageTitle));
            return pageTitle.getText().contains("Account Management");
        } catch (Exception e) {
            log.warn("Account list page not loaded properly: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAccountClosurePageLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pageTitle));
            return pageTitle.getText().contains("Close Account");
        } catch (Exception e) {
            log.warn("Account closure page not loaded properly: {}", e.getMessage());
            return false;
        }
    }

    public boolean isOpenAccountButtonVisible() {
        try {
            return openAccountButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasAccountsInTable() {
        try {
            return accountsTable.isDisplayed() && 
                   !driver.findElements(By.xpath("//tbody//tr")).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickCloseAccountLink(String accountNumber) {
        log.info("Looking for close link for account: {}", accountNumber);
        try {
            WebElement closeLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("close-account-" + accountNumber)));
            closeLink.click();
            log.info("Clicked close account link for: {}", accountNumber);
        } catch (Exception e) {
            log.error("Failed to click close account link: {}", e.getMessage());
            throw new RuntimeException("Could not click close account link for " + accountNumber);
        }
    }

    public String getAccountBalance() {
        try {
            WebElement balanceSpan = driver.findElement(
                By.xpath("//span[contains(text(), 'IDR')]"));
            return balanceSpan.getText();
        } catch (Exception e) {
            log.warn("Could not get account balance: {}", e.getMessage());
            return "";
        }
    }

    public boolean isBalanceZero() {
        String balance = getAccountBalance();
        return balance.contains("IDR 0.00") || balance.contains("IDR 0,00");
    }

    public boolean isCloseAccountButtonEnabled() {
        try {
            return closeAccountButton.isEnabled() && closeAccountButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBalanceErrorDisplayed() {
        try {
            return balanceError.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void fillClosureReason(String reason) {
        log.info("Filling closure reason: {}", reason);
        reasonTextarea.clear();
        reasonTextarea.sendKeys(reason);
    }

    public void confirmAccountClosure() {
        log.info("Confirming account closure");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(confirmCheckbox));
            confirmCheckbox.click();
            log.info("Confirmation checkbox checked");
        } catch (Exception e) {
            log.error("Failed to confirm account closure: {}", e.getMessage());
            throw new RuntimeException("Could not confirm account closure");
        }
    }

    public void clickCloseAccount() {
        log.info("Clicking close account button");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(closeAccountButton));
            closeAccountButton.click();
            log.info("Close account button clicked");
        } catch (Exception e) {
            log.error("Failed to click close account button: {}", e.getMessage());
            throw new RuntimeException("Could not click close account button");
        }
    }

    public void clickCancel() {
        log.info("Clicking cancel button");
        cancelButton.click();
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
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
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getAccountNumber() {
        try {
            WebElement accountNumberSpan = driver.findElement(
                By.xpath("//p[contains(text(), 'Account Number:')]/span"));
            return accountNumberSpan.getText();
        } catch (Exception e) {
            log.warn("Could not get account number: {}", e.getMessage());
            return "";
        }
    }

    public String getCustomerName() {
        try {
            WebElement customerSpan = driver.findElement(
                By.xpath("//p[contains(text(), 'Customer:')]/span"));
            return customerSpan.getText();
        } catch (Exception e) {
            log.warn("Could not get customer name: {}", e.getMessage());
            return "";
        }
    }

    public List<WebElement> getAccountRows() {
        return driver.findElements(By.xpath("//tbody[@id='accounts-table-body']//tr"));
    }

    public boolean hasAccountWithNumber(String accountNumber) {
        try {
            WebElement accountElement = driver.findElement(
                By.id("account-number-" + accountNumber));
            return accountElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForPageLoad() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pageTitle));
        } catch (Exception e) {
            log.warn("Page title not loaded within timeout: {}", e.getMessage());
        }
    }
}