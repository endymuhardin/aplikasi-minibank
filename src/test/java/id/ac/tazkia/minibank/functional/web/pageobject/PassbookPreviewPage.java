package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class PassbookPreviewPage extends BasePage {

    @FindBy(id = "page-title")
    private WebElement pageTitle;

    @FindBy(id = "account-number")
    private WebElement accountNumber;

    @FindBy(id = "account-name")
    private WebElement accountName;

    @FindBy(id = "customer-name")
    private WebElement customerName;

    @FindBy(id = "current-balance")
    private WebElement currentBalance;

    @FindBy(id = "preview-print-button")
    private WebElement printButton;

    @FindBy(id = "back-to-selection-button")
    private WebElement backToSelectionButton;

    @FindBy(css = ".transaction-row")
    private List<WebElement> transactionRows;

    @FindBy(id = "bank-logo")
    private WebElement bankLogo;

    @FindBy(id = "bank-name")
    private WebElement bankName;

    @FindBy(id = "bank-address")
    private WebElement bankAddress;

    public PassbookPreviewPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("page-title")));
        waitForPageToLoad();
    }

    public String getAccountNumber() {
        return accountNumber.getText();
    }

    public String getAccountName() {
        return accountName.getText();
    }

    public String getCustomerName() {
        return customerName.getText();
    }

    public String getCurrentBalance() {
        return currentBalance.getText();
    }

    public List<WebElement> getTransactionRows() {
        return transactionRows;
    }

    public int getTransactionCount() {
        return transactionRows.size();
    }

    public void clickPrintButton() {
        scrollToElementAndClick(printButton);
    }

    public void clickBackToSelection() {
        scrollToElementAndClick(backToSelectionButton);
    }

    public boolean isBankLogoDisplayed() {
        return isElementVisible(bankLogo);
    }

    public String getBankName() {
        return bankName.getText();
    }

    public String getBankAddress() {
        return bankAddress.getText();
    }

    public boolean isPreviewMode() {
        // Check if this is preview mode by looking for preview-specific elements
        return isElementPresent(By.id("preview-print-button"));
    }

    public String getTransactionData(int rowIndex, String columnClass) {
        if (rowIndex >= transactionRows.size()) {
            throw new IndexOutOfBoundsException("Row index " + rowIndex + " is out of bounds");
        }
        
        WebElement row = transactionRows.get(rowIndex);
        WebElement cell = row.findElement(By.className(columnClass));
        return cell.getText();
    }

    public boolean areTransactionsDisplayed() {
        return !transactionRows.isEmpty();
    }

    public void waitForTransactionsToLoad() {
        // Wait for at least one transaction row or a no-transactions message
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".transaction-row")),
            ExpectedConditions.presenceOfElementLocated(By.id("no-transactions-message"))
        ));
    }

    @Override
    public String getCurrentUrl() {
        return super.getCurrentUrl();
    }
}