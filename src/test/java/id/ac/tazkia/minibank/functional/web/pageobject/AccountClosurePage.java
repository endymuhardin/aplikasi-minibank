package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Slf4j
public class AccountClosurePage extends BasePage {
    
    // Page elements
    private static final By PAGE_TITLE = By.xpath("//h1[contains(text(), 'Close Account')]");
    private static final By ACCOUNT_NUMBER_DISPLAY = By.xpath("//span[contains(text(), 'Account Number:')]/following-sibling::span");
    private static final By ACCOUNT_NAME_DISPLAY = By.xpath("//span[contains(text(), 'Account Name:')]/following-sibling::span");
    private static final By CURRENT_BALANCE_DISPLAY = By.xpath("//span[contains(text(), 'Current Balance:')]/following-sibling::*");
    private static final By REASON_TEXTAREA = By.id("reason");
    private static final By CONFIRM_CHECKBOX = By.id("confirm");
    private static final By CLOSE_ACCOUNT_BUTTON = By.xpath("//button[contains(text(), 'Close Account')]");
    private static final By CANCEL_BUTTON = By.xpath("//a[contains(text(), 'Cancel')]");
    private static final By BACK_TO_ACCOUNT_LIST = By.xpath("//a[contains(text(), '← Back to Account List')]");
    private static final By ERROR_MESSAGE = By.xpath("//div[contains(@class, 'bg-red-100')]");
    private static final By WARNING_MESSAGE = By.xpath("//div[contains(@class, 'bg-yellow-50')]");
    private static final By BALANCE_ERROR = By.xpath("//div[contains(@class, 'bg-red-50')]");
    
    public AccountClosurePage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_TITLE));
        log.info("Account closure page loaded successfully");
    }
    
    public boolean isOnAccountClosurePage() {
        return getCurrentUrl().contains("/close") && isElementPresent(PAGE_TITLE);
    }
    
    public String getAccountNumber() {
        WebElement element = driver.findElement(ACCOUNT_NUMBER_DISPLAY);
        return element.getText().trim();
    }
    
    public String getAccountName() {
        WebElement element = driver.findElement(ACCOUNT_NAME_DISPLAY);
        return element.getText().trim();
    }
    
    public String getCurrentBalance() {
        WebElement element = driver.findElement(CURRENT_BALANCE_DISPLAY);
        return element.getText().trim();
    }
    
    public boolean hasNonZeroBalance() {
        return isElementPresent(BALANCE_ERROR);
    }
    
    public boolean isCloseButtonDisabled() {
        WebElement closeButton = driver.findElement(CLOSE_ACCOUNT_BUTTON);
        return !closeButton.isEnabled() || closeButton.getAttribute("disabled") != null;
    }
    
    public void fillClosureReason(String reason) {
        log.info("Filling closure reason: {}", reason);
        clearAndType(driver.findElement(REASON_TEXTAREA), reason);
    }
    
    public void confirmClosure() {
        log.info("Checking confirmation checkbox");
        WebElement confirmCheckbox = driver.findElement(CONFIRM_CHECKBOX);
        if (!confirmCheckbox.isSelected()) {
            scrollToElementAndClick(confirmCheckbox);
        }
    }
    
    public AccountListPage closeAccount() {
        log.info("Clicking close account button");
        scrollToElementAndClick(CLOSE_ACCOUNT_BUTTON);
        return new AccountListPage(driver, baseUrl);
    }
    
    public void closeAccountExpectingError() {
        log.info("Clicking close account button expecting error");
        scrollToElementAndClick(CLOSE_ACCOUNT_BUTTON);
        // Don't navigate away, stay on same page to check errors
    }
    
    public AccountListPage cancel() {
        log.info("Clicking cancel button");
        scrollToElementAndClick(CANCEL_BUTTON);
        return new AccountListPage(driver, baseUrl);
    }
    
    public AccountListPage backToAccountList() {
        log.info("Clicking back to account list");
        scrollToElementAndClick(BACK_TO_ACCOUNT_LIST);
        return new AccountListPage(driver, baseUrl);
    }
    
    public boolean isErrorMessageDisplayed() {
        return isElementPresent(ERROR_MESSAGE);
    }
    
    public String getErrorMessage() {
        if (isErrorMessageDisplayed()) {
            WebElement errorElement = driver.findElement(ERROR_MESSAGE);
            return errorElement.getText().trim();
        }
        return "";
    }
    
    public boolean isWarningMessageDisplayed() {
        return isElementPresent(WARNING_MESSAGE);
    }
    
    public String getWarningMessage() {
        if (isWarningMessageDisplayed()) {
            WebElement warningElement = driver.findElement(WARNING_MESSAGE);
            return warningElement.getText().trim();
        }
        return "";
    }
    
    public boolean isConfirmationCheckboxVisible() {
        return isElementPresent(CONFIRM_CHECKBOX);
    }
    
    public AccountClosurePage fillCompleteForm(String reason) {
        fillClosureReason(reason);
        if (isConfirmationCheckboxVisible()) {
            confirmClosure();
        }
        return this;
    }
}