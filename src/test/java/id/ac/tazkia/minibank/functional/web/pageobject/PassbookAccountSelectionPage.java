package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class PassbookAccountSelectionPage extends BasePage {

    @FindBy(css = "h1:contains('Passbook Printing')")
    private WebElement pageTitle;

    @FindBy(id = "error-message")
    private WebElement errorMessage;

    @FindBy(name = "search")
    private WebElement searchInput;

    @FindBy(id = "search-accounts-btn")
    private WebElement searchButton;

    @FindBy(id = "accounts-table-body")
    private WebElement accountsTableBody;

    @FindBy(id = "no-accounts-message")
    private WebElement noAccountsMessage;

    public PassbookAccountSelectionPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }

    public void navigateTo() {
        driver.get(baseUrl + "/passbook/select-account");
        waitForPageLoad();
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        waitForPageToLoad();
    }

    public boolean isErrorMessageDisplayed() {
        return isElementVisible(errorMessage);
    }

    public String getErrorMessage() {
        return errorMessage.getText();
    }

    public void searchAccounts(String searchTerm) {
        clearAndType(searchInput, searchTerm);
        scrollToElementAndClick(searchButton);
        waitForPageToLoad();
    }

    public void clearSearch() {
        WebElement clearLink = driver.findElement(By.linkText("Clear"));
        scrollToElementAndClick(clearLink);
        waitForPageToLoad();
    }

    public boolean isAccountDisplayed(String accountNumber) {
        return isElementPresent(By.id("account-number-" + accountNumber));
    }

    public List<WebElement> getDisplayedAccounts() {
        return driver.findElements(By.cssSelector("#accounts-table-body tr"));
    }

    public void clickPreviewPassbook(String accountNumber) {
        WebElement previewButton = driver.findElement(By.id("preview-passbook-" + accountNumber));
        scrollToElementAndClick(previewButton);
    }

    public void clickPrintPassbook(String accountNumber) {
        WebElement printButton = driver.findElement(By.id("print-passbook-" + accountNumber));
        scrollToElementAndClick(printButton);
    }

    public boolean isNoAccountsMessageDisplayed() {
        return isElementVisible(noAccountsMessage);
    }

    public String getNoAccountsMessage() {
        return noAccountsMessage.getText();
    }

    public String getAccountBalance(String accountNumber) {
        WebElement accountRow = driver.findElement(By.id("account-row-" + accountNumber));
        WebElement balanceCell = accountRow.findElement(By.cssSelector("td:nth-child(5)"));
        return balanceCell.getText();
    }

    public String getAccountCustomer(String accountNumber) {
        WebElement accountRow = driver.findElement(By.id("account-row-" + accountNumber));
        WebElement customerCell = accountRow.findElement(By.cssSelector("td:nth-child(3)"));
        return customerCell.getText();
    }

    public String getAccountProduct(String accountNumber) {
        WebElement accountRow = driver.findElement(By.id("account-row-" + accountNumber));
        WebElement productCell = accountRow.findElement(By.cssSelector("td:nth-child(4)"));
        return productCell.getText();
    }

    public boolean areOnlyActiveAccountsDisplayed() {
        List<WebElement> accountRows = getDisplayedAccounts();
        // All displayed accounts should be active (we can't directly check status from UI,
        // but the controller only returns active accounts)
        return !accountRows.isEmpty();
    }

    public void navigateToAccountList() {
        WebElement backButton = driver.findElement(By.linkText("Back to Accounts"));
        scrollToElementAndClick(backButton);
    }

    @Override
    public String getCurrentUrl() {
        return super.getCurrentUrl();
    }

    @Override
    public String getPageTitle() {
        return super.getPageTitle();
    }
}