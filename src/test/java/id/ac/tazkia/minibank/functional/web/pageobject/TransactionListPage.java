package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionListPage extends BasePage {
    
    // Page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "cash-deposit-button")
    private WebElement cashDepositButton;
    
    @FindBy(id = "transfer-button")
    private WebElement transferButton;
    
    @FindBy(id = "cash-withdrawal-button")
    private WebElement cashWithdrawalButton;
    
    @FindBy(id = "search")
    private WebElement searchInput;
    
    @FindBy(id = "transactionType")
    private WebElement transactionTypeDropdown;
    
    @FindBy(id = "search-button")
    private WebElement searchButton;
    
    @FindBy(id = "reset-button")
    private WebElement resetButton;
    
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message") 
    private WebElement errorMessage;
    
    @FindBy(id = "transaction-row-0")
    private WebElement firstTransactionRow;
    
    // Constructor
    public TransactionListPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    // Navigation methods
    public void navigateToTransactionList() {
        driver.get(baseUrl + "/transaction/list");
        waitForPageLoad();
    }
    
    public AccountSelectionPage clickCashDepositButton() {
        scrollToElementAndClick(cashDepositButton);
        waitForUrlToContain("/transaction/cash-deposit");
        return new AccountSelectionPage(driver, baseUrl);
    }
    
    public AccountSelectionPage clickCashWithdrawalButton() {
        scrollToElementAndClick(cashWithdrawalButton);
        waitForUrlToContain("/transaction/cash-withdrawal");
        return new AccountSelectionPage(driver, baseUrl);
    }
    
    public AccountSelectionPage clickTransferButton() {
        scrollToElementAndClick(transferButton);
        waitForUrlToContain("/transaction/transfer");
        return new AccountSelectionPage(driver, baseUrl);
    }
    
    // Page verification methods
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("page-title")));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "Daftar Transaksi"));
        waitForPageToLoad();
    }
    
    public boolean isOnTransactionListPage() {
        try {
            waitForPageLoad();
            return pageTitle.getText().contains("Daftar Transaksi");
        } catch (Exception e) {
            return false;
        }
    }
    
    // Search functionality
    public void searchTransactions(String searchTerm) {
        clearAndType(searchInput, searchTerm);
        scrollToElementAndClick(searchButton);
        waitForPageToLoad();
    }
    
    public void filterByTransactionType(String transactionType) {
        selectDropdownByText(transactionTypeDropdown, transactionType);
        scrollToElementAndClick(searchButton);
        waitForPageToLoad();
    }
    
    public void resetFilters() {
        scrollToElementAndClick(resetButton);
        waitForPageToLoad();
    }
    
    // Transaction verification methods
    public boolean hasTransactions() {
        try {
            // Check for first transaction row using safer element detection
            return isElementPresentSafely(By.id("transaction-row-0"));
        } catch (Exception e) {
            log.debug("No transactions found or error checking transaction list: {}", e.getMessage());
            return false;
        }
    }
    
    public String getFirstTransactionNumber() {
        if (hasTransactions()) {
            WebElement transactionNumberElement = driver.findElement(By.id("transaction-number-0"));
            return transactionNumberElement.getText();
        }
        return "";
    }
    
    public String getFirstTransactionType() {
        if (hasTransactions()) {
            WebElement transactionTypeElement = driver.findElement(By.id("transaction-type-0"));
            return transactionTypeElement.getText();
        }
        return "";
    }
    
    public String getFirstTransactionAmount() {
        if (hasTransactions()) {
            WebElement transactionAmountElement = driver.findElement(By.id("transaction-amount-0"));
            return transactionAmountElement.getText();
        }
        return "";
    }
    
    public TransactionViewPage clickViewDetailForFirstTransaction() {
        if (hasTransactions()) {
            WebElement viewLink = driver.findElement(By.id("view-detail-0"));
            scrollToElementAndClick(viewLink);
            waitForUrlToContain("/transaction/view/");
            return new TransactionViewPage(driver, baseUrl);
        }
        throw new RuntimeException("No transactions available to view");
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
}