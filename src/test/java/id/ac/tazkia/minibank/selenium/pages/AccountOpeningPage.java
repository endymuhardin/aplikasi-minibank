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
public class AccountOpeningPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Account list page elements
    @FindBy(id = "open-account-button")
    private WebElement openAccountButton;
    
    @FindBy(id = "accounts-table")
    private WebElement accountsTable;
    
    @FindBy(id = "no-accounts-message")
    private WebElement noAccountsMessage;
    
    @FindBy(id = "search-accounts-btn")
    private WebElement searchAccountsButton;
    
    // Customer selection page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "search-input")
    private WebElement searchInput;
    
    @FindBy(id = "search-button")
    private WebElement searchButton;
    
    @FindBy(id = "customer-cards-container")
    private WebElement customerCardsContainer;
    
    @FindBy(id = "no-customers-message")
    private WebElement noCustomersMessage;
    
    @FindBy(id = "manage-customers-button")
    private WebElement manageCustomersButton;
    
    // Account opening form elements
    @FindBy(id = "customer-info-title")
    private WebElement customerInfoTitle;
    
    @FindBy(id = "customer-name-display")
    private WebElement customerNameDisplay;
    
    @FindBy(id = "customer-number-display")
    private WebElement customerNumberDisplay;
    
    @FindBy(id = "productId")
    private WebElement productSelect;
    
    @FindBy(id = "accountName")
    private WebElement accountNameInput;
    
    @FindBy(id = "initialDeposit")
    private WebElement initialDepositInput;
    
    @FindBy(id = "createdBy")
    private WebElement createdByInput;
    
    @FindBy(id = "open-account-submit-btn")
    private WebElement submitButton;
    
    @FindBy(id = "cancel-button")
    private WebElement cancelButton;
    
    @FindBy(id = "back-to-customer-selection")
    private WebElement backToCustomerSelectionButton;
    
    // Product information display elements
    @FindBy(id = "product-type")
    private WebElement productTypeDisplay;
    
    @FindBy(id = "product-category")
    private WebElement productCategoryDisplay;
    
    @FindBy(id = "min-balance")
    private WebElement minBalanceDisplay;
    
    @FindBy(id = "product-description")
    private WebElement productDescriptionDisplay;
    
    @FindBy(id = "min-deposit-warning")
    private WebElement minDepositWarning;
    
    // Flash messages
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    @FindBy(id = "validation-errors")
    private WebElement validationErrors;
    
    public AccountOpeningPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to account list page
     */
    public AccountOpeningPage navigateToAccountList(String baseUrl) {
        driver.get(baseUrl + "/account/list");
        return waitForAccountListPageLoad();
    }
    
    /**
     * Navigate to customer selection page (account opening entry point)
     */
    public AccountOpeningPage navigateToAccountOpen(String baseUrl) {
        driver.get(baseUrl + "/account/open");
        return waitForCustomerSelectionPageLoad();
    }
    
    /**
     * Navigate directly to account form for a specific customer
     */
    public AccountOpeningPage navigateToAccountForm(String baseUrl, String customerId) {
        driver.get(baseUrl + "/account/open/" + customerId);
        return waitForAccountFormPageLoad();
    }
    
    /**
     * Wait for account list page to load
     */
    public AccountOpeningPage waitForAccountListPageLoad() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(accountsTable),
            ExpectedConditions.visibilityOf(noAccountsMessage)
        ));
        wait.until(ExpectedConditions.visibilityOf(openAccountButton));
        log.debug("Account list page loaded successfully");
        return this;
    }
    
    /**
     * Wait for customer selection page to load
     */
    public AccountOpeningPage waitForCustomerSelectionPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(customerCardsContainer),
            ExpectedConditions.visibilityOf(noCustomersMessage)
        ));
        log.debug("Customer selection page loaded successfully");
        return this;
    }
    
    /**
     * Wait for account opening form page to load
     */
    public AccountOpeningPage waitForAccountFormPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(customerInfoTitle));
        wait.until(ExpectedConditions.visibilityOf(productSelect));
        wait.until(ExpectedConditions.visibilityOf(submitButton));
        log.debug("Account form page loaded successfully");
        return this;
    }
    
    /**
     * Check if account list page is loaded
     */
    public boolean isAccountListPageLoaded() {
        try {
            waitForAccountListPageLoad();
            return driver.getCurrentUrl().contains("/account/list");
        } catch (Exception e) {
            log.debug("Account list page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if customer selection page is loaded
     */
    public boolean isCustomerSelectionPageLoaded() {
        try {
            waitForCustomerSelectionPageLoad();
            return driver.getCurrentUrl().contains("/account/open") &&
                   !driver.getCurrentUrl().matches(".*/account/open/[0-9a-f-]+$") &&
                   pageTitle.getText().contains("Select Customer");
        } catch (Exception e) {
            log.debug("Customer selection page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if account opening form page is loaded
     */
    public boolean isAccountFormPageLoaded() {
        try {
            waitForAccountFormPageLoad();
            return driver.getCurrentUrl().matches(".*/account/open/[0-9a-f-]+$") ||
                   driver.getCurrentUrl().contains("/account/open") && customerInfoTitle.isDisplayed();
        } catch (Exception e) {
            log.debug("Account form page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if open account button is visible
     */
    public boolean isOpenAccountButtonVisible() {
        try {
            return openAccountButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click open account button from account list
     */
    public AccountOpeningPage clickOpenAccount() {
        wait.until(ExpectedConditions.elementToBeClickable(openAccountButton));
        openAccountButton.click();
        log.debug("Clicked open account button");
        return this;
    }
    
    /**
     * Search for customers in customer selection page
     */
    public AccountOpeningPage searchCustomers(String searchTerm) {
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.clear();
        searchInput.sendKeys(searchTerm);
        searchButton.click();
        log.debug("Searched for customers with term: {}", searchTerm);
        return this;
    }
    
    /**
     * Check if customers are displayed
     */
    public boolean areCustomersDisplayed() {
        try {
            return customerCardsContainer.isDisplayed() && 
                   !driver.getPageSource().contains("No customers found");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if no customers message is displayed
     */
    public boolean isNoCustomersMessageDisplayed() {
        try {
            return noCustomersMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click open account button for a specific customer
     */
    public AccountOpeningPage clickOpenAccountForCustomer(String customerNumber) {
        try {
            WebElement openAccountBtn = driver.findElement(
                org.openqa.selenium.By.id("open-account-btn-" + customerNumber)
            );
            wait.until(ExpectedConditions.elementToBeClickable(openAccountBtn));
            openAccountBtn.click();
            log.debug("Clicked open account button for customer: {}", customerNumber);
        } catch (Exception e) {
            log.error("Could not click open account button for customer: {}", customerNumber, e);
        }
        return this;
    }
    
    /**
     * Check if a specific customer is visible
     */
    public boolean isCustomerVisible(String customerNumber) {
        try {
            WebElement customerCard = driver.findElement(
                org.openqa.selenium.By.id("customer-card-" + customerNumber)
            );
            return customerCard.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get customer name from the form display
     */
    public String getCustomerName() {
        try {
            wait.until(ExpectedConditions.visibilityOf(customerNameDisplay));
            return customerNameDisplay.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get customer number from the form display
     */
    public String getCustomerNumber() {
        try {
            wait.until(ExpectedConditions.visibilityOf(customerNumberDisplay));
            return customerNumberDisplay.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Select product from dropdown
     */
    public AccountOpeningPage selectProduct(String productName) {
        try {
            wait.until(ExpectedConditions.visibilityOf(productSelect));
            Select select = new Select(productSelect);
            select.selectByVisibleText(productName);
            log.debug("Selected product: {}", productName);
            
            // Wait for product information to update
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            log.debug("Could not select product '{}': {}", productName, e.getMessage());
        }
        return this;
    }
    
    /**
     * Fill account name
     */
    public AccountOpeningPage fillAccountName(String accountName) {
        wait.until(ExpectedConditions.visibilityOf(accountNameInput));
        accountNameInput.clear();
        accountNameInput.sendKeys(accountName);
        log.debug("Filled account name: {}", accountName);
        return this;
    }
    
    /**
     * Fill initial deposit
     */
    public AccountOpeningPage fillInitialDeposit(String amount) {
        wait.until(ExpectedConditions.visibilityOf(initialDepositInput));
        initialDepositInput.clear();
        initialDepositInput.sendKeys(amount);
        log.debug("Filled initial deposit: {}", amount);
        return this;
    }
    
    /**
     * Fill created by field
     */
    public AccountOpeningPage fillCreatedBy(String createdBy) {
        wait.until(ExpectedConditions.visibilityOf(createdByInput));
        createdByInput.clear();
        createdByInput.sendKeys(createdBy);
        log.debug("Filled created by: {}", createdBy);
        return this;
    }
    
    /**
     * Submit account opening form
     */
    public AccountOpeningPage submitAccountForm() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        submitButton.click();
        log.debug("Clicked submit account form button");
        return this;
    }
    
    /**
     * Click cancel button
     */
    public AccountOpeningPage clickCancel() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton));
        cancelButton.click();
        log.debug("Clicked cancel button");
        return this;
    }
    
    /**
     * Click back to customer selection
     */
    public AccountOpeningPage clickBackToCustomerSelection() {
        wait.until(ExpectedConditions.elementToBeClickable(backToCustomerSelectionButton));
        backToCustomerSelectionButton.click();
        log.debug("Clicked back to customer selection");
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
     * Check if product information is displayed
     */
    public boolean isProductInfoDisplayed() {
        try {
            return !productTypeDisplay.getText().equals("-") &&
                   !productCategoryDisplay.getText().equals("-");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get product type from display
     */
    public String getProductType() {
        try {
            return productTypeDisplay.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Check if minimum deposit warning is visible
     */
    public boolean isMinDepositWarningVisible() {
        try {
            return minDepositWarning.isDisplayed() && 
                   !minDepositWarning.getAttribute("class").contains("hidden");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if form is ready for submission
     */
    public boolean isFormReadyForSubmission() {
        try {
            return !productSelect.getAttribute("value").isEmpty() &&
                   !accountNameInput.getAttribute("value").isEmpty() &&
                   !initialDepositInput.getAttribute("value").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}