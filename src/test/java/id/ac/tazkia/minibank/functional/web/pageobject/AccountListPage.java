package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

@Slf4j
public class AccountListPage extends BasePage {
    
    // Page elements - using ID-based locators for reliability
    private static final By OPEN_ACCOUNT_BUTTON = By.id("open-account-button");
    private static final By ACCOUNT_TABLE = By.id("accounts-table");
    private static final By SEARCH_INPUT = By.name("search");
    private static final By SEARCH_BUTTON = By.id("search-accounts-btn");
    private static final By STATUS_FILTER = By.name("status");
    private static final By CLEAR_BUTTON = By.id("clear-button");
    private static final By ACCOUNT_ROWS_CONTAINER = By.id("accounts-table-body");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By ERROR_MESSAGE = By.id("error-message");
    private static final By NO_ACCOUNTS_MESSAGE = By.id("no-accounts-message");
    
    public AccountListPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void open() {
        driver.get(baseUrl + "/account/list");
        waitForPageToLoad();
    }
    
    public void openAndWaitForLoad() {
        open();
        // Wait for table or no accounts message to be present
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(ACCOUNT_TABLE),
            ExpectedConditions.presenceOfElementLocated(NO_ACCOUNTS_MESSAGE)
        ));
    }
    
    public CustomerSelectionPage clickOpenNewAccount() {
        log.info("Clicking Open New Account button");
        scrollToElementAndClick(OPEN_ACCOUNT_BUTTON);
        return new CustomerSelectionPage(driver, baseUrl);
    }
    
    public void searchAccounts(String searchTerm) {
        log.info("Searching for accounts with term: {}", searchTerm);
        clearAndType(driver.findElement(SEARCH_INPUT), searchTerm);
        scrollToElementAndClick(SEARCH_BUTTON);
        waitForPageToLoad();
    }
    
    public void filterByStatus(String status) {
        log.info("Filtering accounts by status: {}", status);
        selectDropdownByValue(driver.findElement(STATUS_FILTER), status);
        scrollToElementAndClick(SEARCH_BUTTON);
        waitForPageToLoad();
    }
    
    public void clearFilters() {
        log.info("Clearing all filters");
        scrollToElementAndClick(CLEAR_BUTTON);
        waitForPageToLoad();
    }
    
    public boolean hasAccounts() {
        return !isElementPresent(NO_ACCOUNTS_MESSAGE);
    }
    
    public int getAccountCount() {
        if (!hasAccounts()) {
            return 0;
        }
        // Count rows in the accounts table body
        WebElement countElement = driver.findElement(By.id("account-count"));
        String countAttr = countElement.getAttribute("data-count");
        return countAttr != null ? Integer.parseInt(countAttr) : 0;
    }
    
    public boolean isAccountDisplayed(String accountNumber) {
        return isElementPresent(By.id("account-number-" + accountNumber));
    }
    
    public void viewAccount(String accountNumber) {
        log.info("Clicking view link for account: {}", accountNumber);
        By viewLink = By.id("view-account-" + accountNumber);
        scrollToElementAndClick(viewLink);
    }
    
    public void viewTransactions(String accountNumber) {
        log.info("Clicking transactions link for account: {}", accountNumber);
        By transactionsLink = By.id("transactions-account-" + accountNumber);
        scrollToElementAndClick(transactionsLink);
    }
    
    public boolean isOnAccountListPage() {
        return getCurrentUrl().contains("/account/list") && 
               (isElementPresent(ACCOUNT_TABLE) || isElementPresent(NO_ACCOUNTS_MESSAGE));
    }
}