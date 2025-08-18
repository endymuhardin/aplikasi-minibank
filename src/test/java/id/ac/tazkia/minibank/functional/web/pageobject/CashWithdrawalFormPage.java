package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CashWithdrawalFormPage extends BasePage {
    
    // Page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "account-number-display")
    private WebElement accountNumberDisplay;
    
    @FindBy(id = "account-name-display")
    private WebElement accountNameDisplay;
    
    @FindBy(id = "current-balance")
    private WebElement currentBalanceDisplay;
    
    @FindBy(id = "amount")
    private WebElement amountInput;
    
    @FindBy(id = "description")
    private WebElement descriptionTextarea;
    
    @FindBy(id = "referenceNumber")
    private WebElement referenceNumberInput;
    
    @FindBy(id = "createdBy")
    private WebElement createdByInput;
    
    @FindBy(id = "process-withdrawal-btn")
    private WebElement processWithdrawalButton;
    
    @FindBy(id = "cancel-button")
    private WebElement cancelButton;
    
    @FindBy(id = "new-balance-display")
    private WebElement newBalanceDisplay;
    
    @FindBy(id = "new-balance-value")
    private WebElement newBalanceValue;
    
    @FindBy(id = "balance-warning")
    private WebElement balanceWarning;
    
    @FindBy(id = "balance-warning-text")
    private WebElement balanceWarningText;
    
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    @FindBy(id = "validation-errors")
    private WebElement validationErrors;
    
    @FindBy(xpath = "//a[contains(@href, '/transaction/cash-withdrawal')]")
    private WebElement backToAccountSelectionLink;
    
    // Constructor
    public CashWithdrawalFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    // Page verification methods
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("page-title")));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "Formulir Penarikan Tunai"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("account-number-display")));
        waitForPageToLoad();
    }
    
    public boolean isOnCashWithdrawalFormPage() {
        try {
            waitForPageLoad();
            return pageTitle.getText().contains("Formulir Penarikan Tunai");
        } catch (Exception e) {
            return false;
        }
    }
    
    // Form filling methods
    public void fillAmount(String amount) {
        clearAndType(amountInput, amount);
        // Trigger JavaScript validation
        amountInput.sendKeys(org.openqa.selenium.Keys.TAB);
        waitForJavaScriptProcessing(500); // Allow JS calculation to complete
    }
    
    public void fillDescription(String description) {
        clearAndType(descriptionTextarea, description);
    }
    
    public void fillReferenceNumber(String referenceNumber) {
        clearAndType(referenceNumberInput, referenceNumber);
    }
    
    public void fillCreatedBy(String createdBy) {
        clearAndType(createdByInput, createdBy);
    }
    
    public void fillCompleteForm(String amount, String description, String referenceNumber, String createdBy) {
        fillAmount(amount);
        if (description != null && !description.isEmpty()) {
            fillDescription(description);
        }
        if (referenceNumber != null && !referenceNumber.isEmpty()) {
            fillReferenceNumber(referenceNumber);
        }
        fillCreatedBy(createdBy);
    }
    
    // Form submission methods
    public TransactionListPage submitForm() {
        scrollToElementAndClick(processWithdrawalButton);
        
        // Wait for either success redirect or error message
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/transaction/list"),
            ExpectedConditions.presenceOfElementLocated(By.id("error-message")),
            ExpectedConditions.presenceOfElementLocated(By.id("validation-errors")),
            ExpectedConditions.presenceOfElementLocated(By.id("balance-warning"))
        ));
        
        if (getCurrentUrl().contains("/transaction/list")) {
            return new TransactionListPage(driver, baseUrl);
        } else {
            // Form submission failed, stay on the same page
            return null;
        }
    }
    
    public void submitFormExpectingError() {
        scrollToElementAndClick(processWithdrawalButton);
        
        // Wait for error message, validation errors, or balance warning to appear
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(By.id("error-message")),
            ExpectedConditions.presenceOfElementLocated(By.id("validation-errors")),
            ExpectedConditions.presenceOfElementLocated(By.id("balance-warning"))
        ));
    }
    
    public AccountSelectionPage clickCancel() {
        scrollToElementAndClick(cancelButton);
        waitForUrlToContain("/transaction/cash-withdrawal");
        return new AccountSelectionPage(driver, baseUrl);
    }
    
    public AccountSelectionPage clickBackToAccountSelection() {
        scrollToElementAndClick(backToAccountSelectionLink);
        waitForUrlToContain("/transaction/cash-withdrawal");
        return new AccountSelectionPage(driver, baseUrl);
    }
    
    // Information display methods
    public String getAccountNumber() {
        return accountNumberDisplay.getText();
    }
    
    public String getAccountName() {
        return accountNameDisplay.getText();
    }
    
    public String getCurrentBalance() {
        return currentBalanceDisplay.getText();
    }
    
    public String getNewBalanceValue() {
        if (isElementVisible(newBalanceDisplay)) {
            return newBalanceValue.getText();
        }
        return "";
    }
    
    public boolean isNewBalanceDisplayed() {
        return isElementVisible(newBalanceDisplay);
    }
    
    public boolean hasBalanceWarning() {
        return isElementVisible(balanceWarning);
    }
    
    public String getBalanceWarningText() {
        if (hasBalanceWarning()) {
            return balanceWarningText.getText();
        }
        return "";
    }
    
    public boolean isSubmitButtonDisabled() {
        return !processWithdrawalButton.isEnabled() || 
               processWithdrawalButton.getAttribute("class").contains("opacity-50");
    }
    
    // Form validation methods
    public String getAmountValue() {
        return amountInput.getAttribute("value");
    }
    
    public String getDescriptionValue() {
        return descriptionTextarea.getAttribute("value");
    }
    
    public String getReferenceNumberValue() {
        return referenceNumberInput.getAttribute("value");
    }
    
    public String getCreatedByValue() {
        return createdByInput.getAttribute("value");
    }
    
    public String getMinimumAmount() {
        return amountInput.getAttribute("min");
    }
    
    // Validation and error methods
    public boolean hasValidationErrors() {
        return isElementPresent(By.id("validation-errors"));
    }
    
    public String getValidationErrorsText() {
        if (hasValidationErrors()) {
            return validationErrors.getText();
        }
        return "";
    }
    
    public boolean hasFieldError(String fieldName) {
        return isElementPresent(By.xpath("//input[@id='" + fieldName + "' and contains(@class, 'border-red')]")) ||
               isElementPresent(By.xpath("//textarea[@id='" + fieldName + "' and contains(@class, 'border-red')]"));
    }
    
    // Success/Error message methods
    public boolean hasSuccessMessage() {
        return isSuccessMessageDisplayed();
    }
    
    public String getSuccessMessageText() {
        if (hasSuccessMessage()) {
            return getSuccessMessage();
        }
        return "";
    }
    
    public boolean hasErrorMessage() {
        return isErrorMessageDisplayed();
    }
    
    public String getErrorMessageText() {
        if (hasErrorMessage()) {
            return getErrorMessage();
        }
        return "";
    }
    
    // Convenience methods for testing
    public boolean isAmountFieldRequired() {
        return amountInput.getAttribute("required") != null;
    }
    
    public boolean isCreatedByFieldRequired() {
        return createdByInput.getAttribute("required") != null;
    }
    
    public String getAmountPlaceholder() {
        return amountInput.getAttribute("placeholder");
    }
    
    public String getDescriptionPlaceholder() {
        return descriptionTextarea.getAttribute("placeholder");
    }
    
    // Withdrawal-specific validation methods
    public boolean isInsufficientBalanceWarningDisplayed() {
        return hasBalanceWarning() && getBalanceWarningText().contains("melebihi saldo");
    }
    
    public boolean isNegativeBalanceWarningDisplayed() {
        return hasBalanceWarning() && getBalanceWarningText().contains("tidak boleh negatif");
    }
    
    public double getCurrentBalanceAsNumber() {
        try {
            String balanceText = getCurrentBalance();
            // Extract number from "IDR 1,000,000.00" format
            String numberPart = balanceText.replaceAll("[^0-9.,]", "")
                                          .replace(",", "");
            return Double.parseDouble(numberPart);
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    public double getAmountAsNumber() {
        try {
            String amount = getAmountValue();
            return amount.isEmpty() ? 0.0 : Double.parseDouble(amount);
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    public boolean isWithdrawalAmountValid() {
        double currentBalance = getCurrentBalanceAsNumber();
        double withdrawalAmount = getAmountAsNumber();
        return withdrawalAmount > 0 && withdrawalAmount <= currentBalance;
    }
}