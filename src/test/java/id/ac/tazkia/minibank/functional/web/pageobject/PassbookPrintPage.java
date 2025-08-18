package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class PassbookPrintPage extends BasePage {

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

    @FindBy(id = "print-date")
    private WebElement printDate;

    @FindBy(name = "fromDate")
    private WebElement fromDateInput;

    @FindBy(name = "toDate")
    private WebElement toDateInput;

    @FindBy(id = "apply-date-filter-button")
    private WebElement applyDateFilterButton;

    @FindBy(id = "clear-date-filter-button")
    private WebElement clearDateFilterButton;

    @FindBy(css = ".transaction-row")
    private List<WebElement> transactionRows;

    @FindBy(id = "bank-logo")
    private WebElement bankLogo;

    @FindBy(id = "bank-name")
    private WebElement bankName;

    @FindBy(id = "bank-address")
    private WebElement bankAddress;

    @FindBy(id = "pagination-info")
    private WebElement paginationInfo;

    @FindBy(id = "previous-page-link")
    private WebElement previousPageLink;

    @FindBy(id = "next-page-link")
    private WebElement nextPageLink;

    @FindBy(id = "back-to-selection-button")
    private WebElement backToSelectionButton;

    @FindBy(id = "browser-print-button")
    private WebElement browserPrintButton;

    public PassbookPrintPage(WebDriver driver, String baseUrl) {
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

    public String getPrintDate() {
        return printDate.getText();
    }

    public void setDateRange(String fromDate, String toDate) {
        if (fromDate != null && !fromDate.isEmpty()) {
            clearAndType(fromDateInput, fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            clearAndType(toDateInput, toDate);
        }
        scrollToElementAndClick(applyDateFilterButton);
        waitForPageToLoad();
    }

    public void clearDateFilter() {
        scrollToElementAndClick(clearDateFilterButton);
        waitForPageToLoad();
    }

    public List<WebElement> getTransactionRows() {
        return transactionRows;
    }

    public int getTransactionCount() {
        return transactionRows.size();
    }

    public void clickBrowserPrint() {
        scrollToElementAndClick(browserPrintButton);
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

    public boolean isPrintMode() {
        // Check if this is print mode by looking for print-specific elements
        return isElementPresent(By.id("browser-print-button"));
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

    public boolean isPaginationDisplayed() {
        return isElementVisible(paginationInfo);
    }

    public String getPaginationInfo() {
        return paginationInfo.getText();
    }

    public void clickNextPage() {
        if (isElementVisible(nextPageLink)) {
            scrollToElementAndClick(nextPageLink);
            waitForPageToLoad();
        }
    }

    public void clickPreviousPage() {
        if (isElementVisible(previousPageLink)) {
            scrollToElementAndClick(previousPageLink);
            waitForPageToLoad();
        }
    }

    public boolean isNextPageAvailable() {
        return isElementVisible(nextPageLink) && nextPageLink.isEnabled();
    }

    public boolean isPreviousPageAvailable() {
        return isElementVisible(previousPageLink) && previousPageLink.isEnabled();
    }

    @Override
    public String getCurrentUrl() {
        return super.getCurrentUrl();
    }

    public boolean hasDateFilter() {
        return isElementPresent(By.name("fromDate")) && isElementPresent(By.name("toDate"));
    }

    public String getFromDate() {
        return fromDateInput.getAttribute("value");
    }

    public String getToDate() {
        return toDateInput.getAttribute("value");
    }
}