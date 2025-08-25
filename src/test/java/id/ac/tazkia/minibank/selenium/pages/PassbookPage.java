package id.ac.tazkia.minibank.selenium.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PassbookPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Select account page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "search")
    private WebElement searchInput;
    
    @FindBy(id = "search-accounts-btn")
    private WebElement searchAccountsButton;
    
    @FindBy(id = "clear-search-button")
    private WebElement clearSearchButton;
    
    @FindBy(id = "accounts-table-body")
    private WebElement accountsTableBody;
    
    @FindBy(id = "no-accounts-message")
    private WebElement noAccountsMessage;
    
    @FindBy(id = "back-to-accounts-button")
    private WebElement backToAccountsButton;
    
    // Preview page elements
    @FindBy(id = "back-to-selection-button")
    private WebElement backToSelectionButton;
    
    @FindBy(id = "preview-print-button")
    private WebElement previewPrintButton;
    
    @FindBy(id = "fromDate")
    private WebElement fromDateInput;
    
    @FindBy(id = "toDate")
    private WebElement toDateInput;
    
    @FindBy(id = "filter-button")
    private WebElement filterButton;
    
    @FindBy(id = "transactions-table")
    private WebElement transactionsTable;
    
    @FindBy(id = "no-transactions-message")
    private WebElement noTransactionsMessage;
    
    // Print page elements
    @FindBy(id = "passbook-header")
    private WebElement passbookHeader;
    
    @FindBy(id = "account-number-print")
    private WebElement accountNumberPrint;
    
    @FindBy(id = "account-name-print")
    private WebElement accountNamePrint;
    
    @FindBy(id = "customer-name-print")
    private WebElement customerNamePrint;
    
    @FindBy(id = "print-passbook-btn")
    private WebElement printPassbookButton;
    
    @FindBy(id = "back-to-preview-button")
    private WebElement backToPreviewButton;
    
    // Flash messages
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    public PassbookPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to passbook account selection page
     */
    public PassbookPage navigateToSelectAccount(String baseUrl) {
        driver.get(baseUrl + "/passbook/select-account");
        return waitForSelectAccountPageLoad();
    }
    
    /**
     * Navigate to passbook preview page for specific account
     */
    public PassbookPage navigateToPreview(String baseUrl, String accountId) {
        driver.get(baseUrl + "/passbook/preview/" + accountId);
        return waitForPreviewPageLoad();
    }
    
    /**
     * Navigate to passbook print page for specific account
     */
    public PassbookPage navigateToPrint(String baseUrl, String accountId) {
        driver.get(baseUrl + "/passbook/print/" + accountId);
        return waitForPrintPageLoad();
    }
    
    /**
     * Wait for select account page to load
     */
    public PassbookPage waitForSelectAccountPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "Passbook Printing"));
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        log.debug("Passbook select account page loaded successfully");
        return this;
    }
    
    /**
     * Wait for preview page to load
     */
    public PassbookPage waitForPreviewPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "Passbook Preview"));
        log.debug("Passbook preview page loaded successfully");
        return this;
    }
    
    /**
     * Wait for print page to load
     */
    public PassbookPage waitForPrintPageLoad() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(passbookHeader),
            ExpectedConditions.visibilityOf(printPassbookButton)
        ));
        log.debug("Passbook print page loaded successfully");
        return this;
    }
    
    /**
     * Check if select account page is loaded
     */
    public boolean isSelectAccountPageLoaded() {
        try {
            waitForSelectAccountPageLoad();
            return driver.getCurrentUrl().contains("/passbook/select-account") &&
                   pageTitle.getText().contains("Passbook Printing");
        } catch (Exception e) {
            log.debug("Select account page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if preview page is loaded
     */
    public boolean isPreviewPageLoaded() {
        try {
            waitForPreviewPageLoad();
            return driver.getCurrentUrl().contains("/passbook/preview") &&
                   pageTitle.getText().contains("Passbook Preview");
        } catch (Exception e) {
            log.debug("Preview page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if print page is loaded
     */
    public boolean isPrintPageLoaded() {
        try {
            waitForPrintPageLoad();
            return driver.getCurrentUrl().contains("/passbook/print");
        } catch (Exception e) {
            log.debug("Print page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Search for accounts
     */
    public PassbookPage searchAccounts(String searchTerm) {
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.clear();
        searchInput.sendKeys(searchTerm);
        searchAccountsButton.click();
        log.debug("Searched accounts with term: {}", searchTerm);
        return this;
    }
    
    /**
     * Clear search
     */
    public PassbookPage clearSearch() {
        wait.until(ExpectedConditions.elementToBeClickable(clearSearchButton));
        clearSearchButton.click();
        log.debug("Cleared account search");
        return this;
    }
    
    /**
     * Check if accounts are displayed
     */
    public boolean areAccountsDisplayed() {
        try {
            return accountsTableBody.isDisplayed() && 
                   !isNoAccountsMessageVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if no accounts message is visible
     */
    public boolean isNoAccountsMessageVisible() {
        try {
            return noAccountsMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if a specific account is visible
     */
    public boolean isAccountVisible(String accountNumber) {
        try {
            WebElement accountRow = driver.findElement(
                org.openqa.selenium.By.id("account-row-" + accountNumber)
            );
            return accountRow.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click preview button for specific account
     */
    public PassbookPage clickPreviewPassbook(String accountNumber) {
        try {
            WebElement previewButton = driver.findElement(
                org.openqa.selenium.By.id("preview-passbook-" + accountNumber)
            );
            wait.until(ExpectedConditions.elementToBeClickable(previewButton));
            previewButton.click();
            log.debug("Clicked preview button for account: {}", accountNumber);
        } catch (Exception e) {
            log.error("Could not click preview button for account: {}", accountNumber, e);
        }
        return this;
    }
    
    /**
     * Click print button for specific account
     */
    public PassbookPage clickPrintPassbook(String accountNumber) {
        try {
            WebElement printButton = driver.findElement(
                org.openqa.selenium.By.id("print-passbook-" + accountNumber)
            );
            wait.until(ExpectedConditions.elementToBeClickable(printButton));
            printButton.click();
            log.debug("Clicked print button for account: {}", accountNumber);
        } catch (Exception e) {
            log.error("Could not click print button for account: {}", accountNumber, e);
        }
        return this;
    }
    
    /**
     * Get account customer name
     */
    public String getAccountCustomer(String accountNumber) {
        try {
            WebElement customerElement = driver.findElement(
                org.openqa.selenium.By.id("account-customer-" + accountNumber)
            );
            return customerElement.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get account product type
     */
    public String getAccountProduct(String accountNumber) {
        try {
            WebElement productElement = driver.findElement(
                org.openqa.selenium.By.id("account-product-" + accountNumber)
            );
            return productElement.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get account balance
     */
    public String getAccountBalance(String accountNumber) {
        try {
            WebElement balanceElement = driver.findElement(
                org.openqa.selenium.By.id("account-balance-" + accountNumber)
            );
            return balanceElement.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Click back to accounts button
     */
    public PassbookPage clickBackToAccounts() {
        wait.until(ExpectedConditions.elementToBeClickable(backToAccountsButton));
        backToAccountsButton.click();
        log.debug("Clicked back to accounts button");
        return this;
    }
    
    /**
     * Click back to selection button
     */
    public PassbookPage clickBackToSelection() {
        wait.until(ExpectedConditions.elementToBeClickable(backToSelectionButton));
        backToSelectionButton.click();
        log.debug("Clicked back to selection button");
        return this;
    }
    
    /**
     * Click preview print button (on preview page)
     */
    public PassbookPage clickPreviewPrintButton() {
        wait.until(ExpectedConditions.elementToBeClickable(previewPrintButton));
        previewPrintButton.click();
        log.debug("Clicked preview print button");
        return this;
    }
    
    /**
     * Set from date filter
     */
    public PassbookPage setFromDate(String date) {
        wait.until(ExpectedConditions.visibilityOf(fromDateInput));
        fromDateInput.clear();
        fromDateInput.sendKeys(date);
        log.debug("Set from date: {}", date);
        return this;
    }
    
    /**
     * Set to date filter
     */
    public PassbookPage setToDate(String date) {
        wait.until(ExpectedConditions.visibilityOf(toDateInput));
        toDateInput.clear();
        toDateInput.sendKeys(date);
        log.debug("Set to date: {}", date);
        return this;
    }
    
    /**
     * Click filter button
     */
    public PassbookPage clickFilterButton() {
        wait.until(ExpectedConditions.elementToBeClickable(filterButton));
        filterButton.click();
        log.debug("Clicked filter button");
        return this;
    }
    
    /**
     * Check if transactions are displayed
     */
    public boolean areTransactionsDisplayed() {
        try {
            return transactionsTable.isDisplayed() && 
                   !isNoTransactionsMessageVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if no transactions message is visible
     */
    public boolean isNoTransactionsMessageVisible() {
        try {
            return noTransactionsMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if passbook header is visible (on print page)
     */
    public boolean isPassbookHeaderVisible() {
        try {
            return passbookHeader.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get printed account number
     */
    public String getPrintedAccountNumber() {
        try {
            return accountNumberPrint.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get printed account name
     */
    public String getPrintedAccountName() {
        try {
            return accountNamePrint.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get printed customer name
     */
    public String getPrintedCustomerName() {
        try {
            return customerNamePrint.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Click print passbook button (on print page)
     */
    public PassbookPage clickPrintPassbookButton() {
        wait.until(ExpectedConditions.elementToBeClickable(printPassbookButton));
        printPassbookButton.click();
        log.debug("Clicked print passbook button");
        return this;
    }
    
    /**
     * Click back to preview button
     */
    public PassbookPage clickBackToPreview() {
        wait.until(ExpectedConditions.elementToBeClickable(backToPreviewButton));
        backToPreviewButton.click();
        log.debug("Clicked back to preview button");
        return this;
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
}