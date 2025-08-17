package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class TransactionListPage extends BasePage {
    
    // Page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(linkText = "+ Setoran Tunai")
    private WebElement cashDepositButton;
    
    @FindBy(id = "search")
    private WebElement searchInput;
    
    @FindBy(id = "transactionType")
    private WebElement transactionTypeDropdown;
    
    @FindBy(id = "search-button")
    private WebElement searchButton;
    
    @FindBy(xpath = "//a[text()='Reset']")
    private WebElement resetButton;
    
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message") 
    private WebElement errorMessage;
    
    @FindBy(css = "table tbody tr")
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
            return isElementVisible(firstTransactionRow);
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getFirstTransactionNumber() {
        if (hasTransactions()) {
            WebElement transactionNumberCell = firstTransactionRow.findElement(By.cssSelector("td:first-child div.text-sm"));
            return transactionNumberCell.getText();
        }
        return "";
    }
    
    public String getFirstTransactionType() {
        if (hasTransactions()) {
            WebElement transactionTypeCell = firstTransactionRow.findElement(By.cssSelector("td:nth-child(3) span"));
            return transactionTypeCell.getText();
        }
        return "";
    }
    
    public String getFirstTransactionAmount() {
        if (hasTransactions()) {
            WebElement amountCell = firstTransactionRow.findElement(By.cssSelector("td:nth-child(5) div.text-sm"));
            return amountCell.getText();
        }
        return "";
    }
    
    public TransactionViewPage clickViewDetailForFirstTransaction() {
        if (hasTransactions()) {
            WebElement viewLink = firstTransactionRow.findElement(By.linkText("Lihat Detail"));
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