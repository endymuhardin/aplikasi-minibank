package id.ac.tazkia.minibank.selenium.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Transaction list page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "cash-deposit-button")
    private WebElement cashDepositButton;
    
    @FindBy(id = "cash-withdrawal-button")
    private WebElement cashWithdrawalButton;
    
    @FindBy(id = "transfer-button")
    private WebElement transferButton;
    
    @FindBy(id = "search")
    private WebElement searchField;
    
    @FindBy(id = "transactionType")
    private WebElement transactionTypeSelect;
    
    @FindBy(id = "search-button")
    private WebElement searchButton;
    
    @FindBy(id = "reset-button")
    private WebElement resetButton;
    
    // Account selection page elements
    @FindBy(id = "accounts-list")
    private WebElement accountsList;
    
    @FindBy(id = "back-to-transaction-list")
    private WebElement backToTransactionListButton;
    
    // Transaction form elements
    @FindBy(id = "account-info-title")
    private WebElement accountInfoTitle;
    
    @FindBy(id = "account-number-display")
    private WebElement accountNumberDisplay;
    
    @FindBy(id = "account-name-display")
    private WebElement accountNameDisplay;
    
    @FindBy(id = "current-balance")
    private WebElement currentBalanceDisplay;
    
    @FindBy(id = "amount")
    private WebElement amountInput;
    
    @FindBy(id = "description")
    private WebElement descriptionInput;
    
    @FindBy(id = "referenceNumber")
    private WebElement referenceNumberInput;
    
    @FindBy(id = "createdBy")
    private WebElement createdByInput;
    
    @FindBy(id = "process-deposit-btn")
    private WebElement processDepositButton;
    
    @FindBy(id = "cancel-button")
    private WebElement cancelButton;
    
    @FindBy(id = "back-to-cash-deposit-selection")
    private WebElement backToSelectionButton;
    
    // Balance calculation elements
    @FindBy(id = "new-balance-display")
    private WebElement newBalanceDisplay;
    
    @FindBy(id = "new-balance-value")
    private WebElement newBalanceValue;
    
    // Flash messages
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    @FindBy(id = "validation-errors")
    private WebElement validationErrors;
    
    public TransactionPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to transaction list page
     */
    public TransactionPage navigateToTransactionList(String baseUrl) {
        driver.get(baseUrl + "/transaction/list");
        return waitForTransactionListPageLoad();
    }
    
    /**
     * Navigate to cash deposit account selection
     */
    public TransactionPage navigateToCashDeposit(String baseUrl) {
        driver.get(baseUrl + "/transaction/cash-deposit");
        return waitForAccountSelectionPageLoad();
    }
    
    /**
     * Navigate to cash withdrawal account selection
     */
    public TransactionPage navigateToCashWithdrawal(String baseUrl) {
        driver.get(baseUrl + "/transaction/cash-withdrawal");
        return waitForAccountSelectionPageLoad();
    }
    
    /**
     * Navigate to transfer account selection
     */
    public TransactionPage navigateToTransfer(String baseUrl) {
        driver.get(baseUrl + "/transaction/transfer");
        return waitForAccountSelectionPageLoad();
    }
    
    /**
     * Navigate directly to transaction form for specific account
     */
    public TransactionPage navigateToTransactionForm(String baseUrl, String transactionType, String accountId) {
        driver.get(baseUrl + "/transaction/" + transactionType + "/" + accountId);
        return waitForTransactionFormPageLoad();
    }
    
    /**
     * Wait for transaction list page to load
     */
    public TransactionPage waitForTransactionListPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.visibilityOf(cashDepositButton));
        log.debug("Transaction list page loaded successfully");
        return this;
    }
    
    /**
     * Wait for account selection page to load
     */
    public TransactionPage waitForAccountSelectionPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(accountsList),
            ExpectedConditions.textToBePresentInElement(pageTitle, "Pilih Rekening")
        ));
        log.debug("Account selection page loaded successfully");
        return this;
    }
    
    /**
     * Wait for transaction form page to load
     */
    public TransactionPage waitForTransactionFormPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(accountInfoTitle));
        wait.until(ExpectedConditions.visibilityOf(amountInput));
        log.debug("Transaction form page loaded successfully");
        return this;
    }
    
    /**
     * Check if transaction list page is loaded
     */
    public boolean isTransactionListPageLoaded() {
        try {
            waitForTransactionListPageLoad();
            return driver.getCurrentUrl().contains("/transaction/list") &&
                   pageTitle.getText().contains("Daftar Transaksi");
        } catch (Exception e) {
            log.debug("Transaction list page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if account selection page is loaded
     */
    public boolean isAccountSelectionPageLoaded() {
        try {
            waitForAccountSelectionPageLoad();
            return driver.getCurrentUrl().contains("/transaction/cash-") ||
                   driver.getCurrentUrl().contains("/transaction/transfer") &&
                   pageTitle.getText().contains("Pilih Rekening");
        } catch (Exception e) {
            log.debug("Account selection page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if transaction form page is loaded
     */
    public boolean isTransactionFormPageLoaded() {
        try {
            waitForTransactionFormPageLoad();
            return accountInfoTitle.isDisplayed() && amountInput.isDisplayed();
        } catch (Exception e) {
            log.debug("Transaction form page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if cash deposit button is visible
     */
    public boolean isCashDepositButtonVisible() {
        try {
            return cashDepositButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if cash withdrawal button is visible
     */
    public boolean isCashWithdrawalButtonVisible() {
        try {
            return cashWithdrawalButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if transfer button is visible
     */
    public boolean isTransferButtonVisible() {
        try {
            return transferButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click cash deposit button
     */
    public TransactionPage clickCashDeposit() {
        wait.until(ExpectedConditions.elementToBeClickable(cashDepositButton));
        cashDepositButton.click();
        log.debug("Clicked cash deposit button");
        return this;
    }
    
    /**
     * Click cash withdrawal button
     */
    public TransactionPage clickCashWithdrawal() {
        wait.until(ExpectedConditions.elementToBeClickable(cashWithdrawalButton));
        cashWithdrawalButton.click();
        log.debug("Clicked cash withdrawal button");
        return this;
    }
    
    /**
     * Click transfer button
     */
    public TransactionPage clickTransfer() {
        wait.until(ExpectedConditions.elementToBeClickable(transferButton));
        transferButton.click();
        log.debug("Clicked transfer button");
        return this;
    }
    
    /**
     * Search transactions
     */
    public TransactionPage searchTransactions(String searchTerm) {
        wait.until(ExpectedConditions.visibilityOf(searchField));
        searchField.clear();
        searchField.sendKeys(searchTerm);
        searchButton.click();
        log.debug("Searched transactions with term: {}", searchTerm);
        return this;
    }
    
    /**
     * Filter transactions by type
     */
    public TransactionPage filterByTransactionType(String transactionType) {
        try {
            wait.until(ExpectedConditions.visibilityOf(transactionTypeSelect));
            Select select = new Select(transactionTypeSelect);
            select.selectByVisibleText(transactionType);
            searchButton.click();
            log.debug("Filtered transactions by type: {}", transactionType);
        } catch (Exception e) {
            log.debug("Could not filter by transaction type '{}': {}", transactionType, e.getMessage());
        }
        return this;
    }
    
    /**
     * Check if accounts are displayed for selection
     */
    public boolean areAccountsDisplayed() {
        try {
            return accountsList.isDisplayed() && 
                   !driver.getPageSource().contains("Tidak ada rekening aktif ditemukan");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if a specific account is visible
     */
    public boolean isAccountVisible(String accountId) {
        try {
            WebElement accountCard = driver.findElement(
                org.openqa.selenium.By.id("account-card-" + accountId)
            );
            return accountCard.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Select account for transaction
     */
    public TransactionPage selectAccount(String accountId) {
        try {
            WebElement selectButton = driver.findElement(
                org.openqa.selenium.By.id("select-button-" + accountId)
            );
            wait.until(ExpectedConditions.elementToBeClickable(selectButton));
            selectButton.click();
            log.debug("Selected account: {}", accountId);
        } catch (Exception e) {
            log.error("Could not select account: {}", accountId, e);
        }
        return this;
    }
    
    /**
     * Get account number from form display
     */
    public String getAccountNumber() {
        try {
            wait.until(ExpectedConditions.visibilityOf(accountNumberDisplay));
            return accountNumberDisplay.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get account name from form display
     */
    public String getAccountName() {
        try {
            wait.until(ExpectedConditions.visibilityOf(accountNameDisplay));
            return accountNameDisplay.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get current balance from form display
     */
    public String getCurrentBalance() {
        try {
            wait.until(ExpectedConditions.visibilityOf(currentBalanceDisplay));
            return currentBalanceDisplay.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Fill transaction amount
     */
    public TransactionPage fillAmount(String amount) {
        wait.until(ExpectedConditions.visibilityOf(amountInput));
        amountInput.clear();
        amountInput.sendKeys(amount);
        log.debug("Filled amount: {}", amount);
        
        // Trigger balance calculation
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return this;
    }
    
    /**
     * Fill transaction description
     */
    public TransactionPage fillDescription(String description) {
        wait.until(ExpectedConditions.visibilityOf(descriptionInput));
        descriptionInput.clear();
        descriptionInput.sendKeys(description);
        log.debug("Filled description: {}", description);
        return this;
    }
    
    /**
     * Fill reference number
     */
    public TransactionPage fillReferenceNumber(String referenceNumber) {
        wait.until(ExpectedConditions.visibilityOf(referenceNumberInput));
        referenceNumberInput.clear();
        referenceNumberInput.sendKeys(referenceNumber);
        log.debug("Filled reference number: {}", referenceNumber);
        return this;
    }
    
    /**
     * Fill created by field
     */
    public TransactionPage fillCreatedBy(String createdBy) {
        wait.until(ExpectedConditions.visibilityOf(createdByInput));
        createdByInput.clear();
        createdByInput.sendKeys(createdBy);
        log.debug("Filled created by: {}", createdBy);
        return this;
    }
    
    /**
     * Submit transaction form
     */
    public TransactionPage submitTransaction() {
        wait.until(ExpectedConditions.elementToBeClickable(processDepositButton));
        processDepositButton.click();
        log.debug("Clicked submit transaction button");
        return this;
    }
    
    /**
     * Click cancel button
     */
    public TransactionPage clickCancel() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton));
        cancelButton.click();
        log.debug("Clicked cancel button");
        return this;
    }
    
    /**
     * Click back to selection button
     */
    public TransactionPage clickBackToSelection() {
        wait.until(ExpectedConditions.elementToBeClickable(backToSelectionButton));
        backToSelectionButton.click();
        log.debug("Clicked back to selection button");
        return this;
    }
    
    /**
     * Click back to transaction list button
     */
    public TransactionPage clickBackToTransactionList() {
        wait.until(ExpectedConditions.elementToBeClickable(backToTransactionListButton));
        backToTransactionListButton.click();
        log.debug("Clicked back to transaction list button");
        return this;
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
     * Check if validation errors are visible
     */
    public boolean hasValidationErrors() {
        try {
            return validationErrors.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if new balance display is visible
     */
    public boolean isNewBalanceDisplayVisible() {
        try {
            return newBalanceDisplay.isDisplayed() && 
                   !newBalanceDisplay.getAttribute("class").contains("hidden");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get new balance value
     */
    public String getNewBalanceValue() {
        try {
            if (isNewBalanceDisplayVisible()) {
                return newBalanceValue.getText();
            }
        } catch (Exception e) {
            log.debug("Could not get new balance value: {}", e.getMessage());
        }
        return "";
    }
    
    /**
     * Check if form is ready for submission
     */
    public boolean isFormReadyForSubmission() {
        try {
            return !amountInput.getAttribute("value").isEmpty() &&
                   !createdByInput.getAttribute("value").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if transactions are displayed in the list
     */
    public boolean areTransactionsDisplayed() {
        try {
            return driver.getPageSource().contains("transaction-row-") ||
                   !driver.getPageSource().contains("Tidak ada transaksi ditemukan");
        } catch (Exception e) {
            return false;
        }
    }
}