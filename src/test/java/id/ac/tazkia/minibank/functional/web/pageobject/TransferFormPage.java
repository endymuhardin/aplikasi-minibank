package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Slf4j
public class TransferFormPage extends BasePage {
    
    // Page elements
    private static final By PAGE_TITLE = By.id("page-title");
    private static final By ACCOUNT_NUMBER_DISPLAY = By.id("account-number-display");
    private static final By ACCOUNT_NAME_DISPLAY = By.id("account-name-display");
    private static final By TO_ACCOUNT_NUMBER_INPUT = By.id("toAccountNumber");
    private static final By AMOUNT_INPUT = By.id("amount");
    private static final By DESCRIPTION_INPUT = By.id("description");
    private static final By REFERENCE_NUMBER_INPUT = By.id("referenceNumber");
    private static final By CREATED_BY_INPUT = By.id("createdBy");
    private static final By VALIDATE_TRANSFER_BUTTON = By.id("validate-transfer-submit-btn");
    private static final By CANCEL_BUTTON = By.id("cancel-button");
    private static final By BACK_TO_TRANSFER_SELECTION = By.id("back-to-transfer-selection");
    private static final By ERROR_MESSAGE = By.id("error-message");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By INFO_MESSAGE = By.id("info-message");
    private static final By INSUFFICIENT_BALANCE_WARNING = By.id("insufficient-balance-warning");
    
    public TransferFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_TITLE));
        log.info("Transfer form page loaded successfully");
    }
    
    public boolean isOnTransferFormPage() {
        return getCurrentUrl().contains("/transfer/") && isElementPresent(PAGE_TITLE);
    }
    
    public String getSourceAccountNumber() {
        WebElement element = driver.findElement(ACCOUNT_NUMBER_DISPLAY);
        return element.getText().trim();
    }
    
    public String getSourceAccountName() {
        WebElement element = driver.findElement(ACCOUNT_NAME_DISPLAY);
        return element.getText().trim();
    }
    
    public void fillDestinationAccountNumber(String accountNumber) {
        log.info("Filling destination account number: {}", accountNumber);
        clearAndType(driver.findElement(TO_ACCOUNT_NUMBER_INPUT), accountNumber);
    }
    
    public void fillTransferAmount(String amount) {
        log.info("Filling transfer amount: {}", amount);
        clearAndType(driver.findElement(AMOUNT_INPUT), amount);
    }
    
    public void fillDescription(String description) {
        log.info("Filling description: {}", description);
        clearAndType(driver.findElement(DESCRIPTION_INPUT), description);
    }
    
    public void fillReferenceNumber(String referenceNumber) {
        log.info("Filling reference number: {}", referenceNumber);
        clearAndType(driver.findElement(REFERENCE_NUMBER_INPUT), referenceNumber);
    }
    
    public void fillCreatedBy(String createdBy) {
        log.info("Filling created by: {}", createdBy);
        clearAndType(driver.findElement(CREATED_BY_INPUT), createdBy);
    }
    
    public TransferConfirmPage validateTransfer() {
        log.info("Clicking validate transfer button");
        scrollToElementAndClick(VALIDATE_TRANSFER_BUTTON);
        return new TransferConfirmPage(driver, baseUrl);
    }
    
    public void validateTransferExpectingError() {
        log.info("Clicking validate transfer button expecting error");
        scrollToElementAndClick(VALIDATE_TRANSFER_BUTTON);
        // Stay on same page to check errors
    }
    
    public void cancel() {
        log.info("Clicking cancel button");
        scrollToElementAndClick(CANCEL_BUTTON);
    }
    
    public void backToTransferSelection() {
        log.info("Clicking back to transfer selection");
        scrollToElementAndClick(BACK_TO_TRANSFER_SELECTION);
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
    
    public boolean isSuccessMessageDisplayed() {
        return isElementPresent(SUCCESS_MESSAGE);
    }
    
    public String getSuccessMessage() {
        if (isSuccessMessageDisplayed()) {
            WebElement successElement = driver.findElement(SUCCESS_MESSAGE);
            return successElement.getText().trim();
        }
        return "";
    }
    
    public boolean isInfoMessageDisplayed() {
        return isElementPresent(INFO_MESSAGE);
    }
    
    public String getInfoMessage() {
        if (isInfoMessageDisplayed()) {
            WebElement infoElement = driver.findElement(INFO_MESSAGE);
            return infoElement.getText().trim();
        }
        return "";
    }
    
    public boolean isInsufficientBalanceWarningDisplayed() {
        return isElementPresent(INSUFFICIENT_BALANCE_WARNING) && 
               !driver.findElement(INSUFFICIENT_BALANCE_WARNING).getAttribute("class").contains("hidden");
    }
    
    public TransferFormPage fillCompleteTransferForm(String destinationAccount, String amount, 
                                                    String description, String createdBy) {
        fillDestinationAccountNumber(destinationAccount);
        fillTransferAmount(amount);
        fillDescription(description);
        fillCreatedBy(createdBy);
        return this;
    }
    
    public TransferFormPage fillCompleteTransferFormWithReference(String destinationAccount, String amount, 
                                                                 String description, String referenceNumber, String createdBy) {
        fillDestinationAccountNumber(destinationAccount);
        fillTransferAmount(amount);
        fillDescription(description);
        fillReferenceNumber(referenceNumber);
        fillCreatedBy(createdBy);
        return this;
    }
}