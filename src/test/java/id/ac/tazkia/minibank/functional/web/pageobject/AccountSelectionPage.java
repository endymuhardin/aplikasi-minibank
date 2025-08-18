package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class AccountSelectionPage extends BasePage {
    
    // Page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "search")
    private WebElement searchInput;
    
    @FindBy(id = "search-button")
    private WebElement searchButton;
    
    @FindBy(id = "reset-button")
    private WebElement resetButton;
    
    @FindBy(id = "account-cards")
    private List<WebElement> accountCards;
    
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    @FindBy(id = "back-to-transaction-list")
    private WebElement backToTransactionListLink;
    
    // Constructor
    public AccountSelectionPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    // Navigation methods
    public void navigateToAccountSelection() {
        driver.get(baseUrl + "/transaction/cash-deposit");
        waitForPageLoad();
    }
    
    public TransactionListPage clickBackToTransactionList() {
        scrollToElementAndClick(backToTransactionListLink);
        waitForUrlToContain("/transaction/list");
        return new TransactionListPage(driver, baseUrl);
    }
    
    // Page verification methods
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("page-title")));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.textToBePresentInElement(pageTitle, "Setoran Tunai - Pilih Rekening"),
            ExpectedConditions.textToBePresentInElement(pageTitle, "Penarikan Tunai - Pilih Rekening")
        ));
        waitForPageToLoad();
    }
    
    public boolean isOnAccountSelectionPage() {
        try {
            waitForPageLoad();
            String titleText = pageTitle.getText();
            return titleText.contains("Setoran Tunai - Pilih Rekening") || 
                   titleText.contains("Penarikan Tunai - Pilih Rekening");
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isWithdrawalType() {
        try {
            return pageTitle.getText().contains("Penarikan Tunai");
        } catch (Exception e) {
            return false;
        }
    }
    
    // Search functionality
    public void searchAccounts(String searchTerm) {
        clearAndType(searchInput, searchTerm);
        scrollToElementAndClick(searchButton);
        waitForPageToLoad();
    }
    
    public void resetSearch() {
        scrollToElementAndClick(resetButton);
        waitForPageToLoad();
    }
    
    // Account selection methods
    public boolean hasAccounts() {
        try {
            // Wait for accounts list to load and check for first test account
            waitForElementToBePresent(By.id("accounts-list"));
            // Check if the first test account from fixtures is present (using safe check)
            return isElementPresentSafely(By.id("account-card-11111111-1111-1111-aaaa-111111111111"));
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check if accounts are available. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public int getAccountCount() {
        try {
            // Count account cards by checking for known test account IDs
            int count = 0;
            String[] testAccountIds = {
                "11111111-1111-1111-aaaa-111111111111",
                "22222222-2222-2222-bbbb-222222222222", 
                "33333333-3333-3333-cccc-333333333333",
                "44444444-4444-4444-dddd-444444444444",
                "55555555-5555-5555-eeee-555555555555"
            };
            
            for (String accountId : testAccountIds) {
                if (isElementPresentSafely(By.id("account-card-" + accountId))) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot count accounts. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            throw new AssertionError(errorDetails, e);
        }
    }
    
    // Parameterized methods that accept account ID
    public String getAccountNumber(String accountId) {
        WebElement accountNumberElement = driver.findElement(By.id("account-number-" + accountId));
        return accountNumberElement.getText();
    }

    public String getAccountName(String accountId) {
        WebElement accountNameElement = driver.findElement(By.id("account-name-" + accountId));
        return accountNameElement.getText();
    }

    public CashDepositFormPage selectAccount(String accountId) {
        // Wait for the specific account card to be present
        waitForElementToBePresent(By.id("account-card-" + accountId));
        
        // Find and click the account card
        WebElement accountCard = driver.findElement(By.id("account-card-" + accountId));
        scrollToElementAndClick(accountCard);
        
        // Wait for navigation to cash deposit form URL (expects /transaction/cash-deposit/{accountId})
        waitForUrlToContain("/transaction/cash-deposit/" + accountId);
        waitForPageToLoad();
        
        return new CashDepositFormPage(driver, baseUrl);
    }
    
    public CashWithdrawalFormPage selectAccountForWithdrawal(String accountId) {
        // Wait for the specific account card to be present
        waitForElementToBePresent(By.id("account-card-" + accountId));
        
        // Find and click the account card
        WebElement accountCard = driver.findElement(By.id("account-card-" + accountId));
        scrollToElementAndClick(accountCard);
        
        // Wait for navigation to cash withdrawal form URL (expects /transaction/cash-withdrawal/{accountId})
        waitForUrlToContain("/transaction/cash-withdrawal/" + accountId);
        waitForPageToLoad();
        
        return new CashWithdrawalFormPage(driver, baseUrl);
    }
    
    // Convenience methods for the first account using known test data ID
    public String getFirstAccountNumber() {
        if (hasAccounts()) {
            return getAccountNumber("11111111-1111-1111-aaaa-111111111111");
        }
        return "";
    }
    
    public String getFirstAccountName() {
        if (hasAccounts()) {
            return getAccountName("11111111-1111-1111-aaaa-111111111111");
        }
        return "";
    }
    
    public CashDepositFormPage selectFirstAccount() {
        if (hasAccounts()) {
            return selectAccount("11111111-1111-1111-aaaa-111111111111");
        }
        throw new RuntimeException("No accounts available to select");
    }
    
    public CashWithdrawalFormPage selectFirstAccountForWithdrawal() {
        if (hasAccounts()) {
            return selectAccountForWithdrawal("11111111-1111-1111-aaaa-111111111111");
        }
        throw new RuntimeException("No accounts available to select");
    }
    
    public CashDepositFormPage selectAccountByNumber(String accountNumber) {
        for (WebElement accountCard : accountCards) {
            // Get account ID from card and use it to find the account number element by ID
            String cardId = accountCard.getAttribute("id");
            String accountId = cardId.replace("account-card-", "");
            WebElement accountNumberElement = driver.findElement(By.id("account-number-" + accountId));
            if (accountNumberElement.getText().equals(accountNumber)) {
                // Click the entire account card (which has onclick="selectAccount(accountId)")
                scrollToElementAndClick(accountCard);
                // Wait for navigation - the form page will detect if we're on the right page
                waitForPageToLoad();
                return new CashDepositFormPage(driver, baseUrl);
            }
        }
        throw new RuntimeException("Account with number " + accountNumber + " not found");
    }
    
    public CashDepositFormPage clickAccountCard(String accountNumber) {
        for (WebElement accountCard : accountCards) {
            // Get account ID from card and use it to find the account number element by ID
            String cardId = accountCard.getAttribute("id");
            String accountId = cardId.replace("account-card-", "");
            WebElement accountNumberElement = driver.findElement(By.id("account-number-" + accountId));
            if (accountNumberElement.getText().equals(accountNumber)) {
                scrollToElementAndClick(accountCard);
                waitForPageToLoad();
                return new CashDepositFormPage(driver, baseUrl);
            }
        }
        throw new RuntimeException("Account with number " + accountNumber + " not found");
    }
    
    // Account information methods
    public String getAccountBalance(String accountNumber) {
        for (WebElement accountCard : accountCards) {
            WebElement accountNumberElement = accountCard.findElement(By.id("account-number"));
            if (accountNumberElement.getText().equals(accountNumber)) {
                WebElement balanceElement = accountCard.findElement(By.className("font-semibold"));
                return balanceElement.getText();
            }
        }
        return "";
    }
    
    public String getAccountStatus(String accountNumber) {
        for (WebElement accountCard : accountCards) {
            WebElement accountNumberElement = accountCard.findElement(By.id("account-number"));
            if (accountNumberElement.getText().equals(accountNumber)) {
                WebElement statusElement = accountCard.findElement(By.className("rounded-full"));
                return statusElement.getText();
            }
        }
        return "";
    }
    
    public boolean isAccountActive(String accountNumber) {
        return "ACTIVE".equals(getAccountStatus(accountNumber));
    }
    
    // Validation methods
    public boolean isNoAccountsMessageDisplayed() {
        try {
            WebElement noAccountsMessage = driver.findElement(By.xpath("//p[contains(text(), 'Tidak ada rekening aktif ditemukan')]"));
            return isElementVisible(noAccountsMessage);
        } catch (Exception e) {
            return false;
        }
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
    
    // Note: Using className locators for balance and status since account IDs come from fixtures
}