package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Slf4j
public class AccountOpeningFormPage extends BasePage {
    
    // Page elements - using ID-based locators for reliability
    private static final By PAGE_TITLE = By.id("page-title");
    private static final By BACK_TO_SELECTION_LINK = By.linkText("← Back to Customer Selection");
    private static final By PRODUCT_DROPDOWN = By.id("productId");
    private static final By ACCOUNT_NAME_INPUT = By.id("accountName");
    private static final By INITIAL_DEPOSIT_INPUT = By.id("initialDeposit");
    private static final By CREATED_BY_INPUT = By.id("createdBy");
    private static final By SUBMIT_BUTTON = By.id("open-account-submit-btn");
    private static final By CANCEL_LINK = By.linkText("Cancel");
    
    // Product information section
    private static final By PRODUCT_INFO_SECTION = By.id("product-info");
    private static final By PRODUCT_TYPE_DISPLAY = By.id("product-type");
    private static final By PRODUCT_CATEGORY_DISPLAY = By.id("product-category");
    private static final By MIN_BALANCE_DISPLAY = By.id("min-balance");
    private static final By PRODUCT_DESCRIPTION_DISPLAY = By.id("product-description");
    
    // Validation and messages
    private static final By MIN_DEPOSIT_WARNING = By.id("min-deposit-warning");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By ERROR_MESSAGE = By.id("error-message");
    private static final By VALIDATION_ERRORS = By.id("validation-errors");
    
    // Customer information section - using ID-based locators
    private static final By CUSTOMER_NAME_DISPLAY = By.id("customer-name-display");
    private static final By CUSTOMER_NUMBER_DISPLAY = By.id("customer-number-display");
    
    public AccountOpeningFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void waitForPageLoad() {
        // Wait for page title and form elements to be present
        wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_TITLE));
        wait.until(ExpectedConditions.presenceOfElementLocated(PRODUCT_DROPDOWN));
        wait.until(ExpectedConditions.presenceOfElementLocated(ACCOUNT_NAME_INPUT));
        wait.until(ExpectedConditions.presenceOfElementLocated(INITIAL_DEPOSIT_INPUT));
        waitForPageToLoad();
    }
    
    public void selectProduct(String productName) {
        log.info("Selecting product: {}", productName);
        WebElement productDropdown = driver.findElement(PRODUCT_DROPDOWN);
        selectDropdownByText(productDropdown, productName);
        
        // Wait for product information to be displayed
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(PRODUCT_INFO_SECTION),
            ExpectedConditions.invisibilityOfElementLocated(PRODUCT_INFO_SECTION)
        ));
    }
    
    public void selectProductByValue(String productId) {
        log.info("Selecting product by ID: {}", productId);
        WebElement productDropdown = driver.findElement(PRODUCT_DROPDOWN);
        selectDropdownByValue(productDropdown, productId);
        
        // Wait for product information to be displayed
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(PRODUCT_INFO_SECTION),
            ExpectedConditions.invisibilityOfElementLocated(PRODUCT_INFO_SECTION)
        ));
    }
    
    public void selectFirstAvailableProduct() {
        log.info("Selecting first available product");
        WebElement productDropdown = driver.findElement(PRODUCT_DROPDOWN);
        selectFirstNonEmptyOption(productDropdown);
        
        // Wait for product information to be displayed
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(PRODUCT_INFO_SECTION),
            ExpectedConditions.invisibilityOfElementLocated(PRODUCT_INFO_SECTION)
        ));
    }
    
    public void fillAccountName(String accountName) {
        log.info("Filling account name: {}", accountName);
        clearAndType(driver.findElement(ACCOUNT_NAME_INPUT), accountName);
    }
    
    public void fillInitialDeposit(String amount) {
        log.info("Filling initial deposit: {}", amount);
        clearAndType(driver.findElement(INITIAL_DEPOSIT_INPUT), amount);
    }
    
    public void fillCreatedBy(String createdBy) {
        log.info("Filling created by: {}", createdBy);
        clearAndType(driver.findElement(CREATED_BY_INPUT), createdBy);
    }
    
    public AccountListPage submitForm() {
        log.info("Submitting account opening form");
        scrollToElementAndClick(SUBMIT_BUTTON);
        
        // Wait for either success (redirect to list) or error (stay on form)
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/account/list"),
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERRORS)
        ));
        
        return new AccountListPage(driver, baseUrl);
    }
    
    public void submitFormExpectingError() {
        log.info("Submitting account opening form expecting validation error");
        scrollToElementAndClick(SUBMIT_BUTTON);
        
        // Wait for error message or validation errors to appear
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERRORS),
            ExpectedConditions.presenceOfElementLocated(MIN_DEPOSIT_WARNING)
        ));
    }
    
    public CustomerSelectionPage goBackToCustomerSelection() {
        log.info("Going back to customer selection");
        scrollToElementAndClick(BACK_TO_SELECTION_LINK);
        return new CustomerSelectionPage(driver, baseUrl);
    }
    
    public CustomerSelectionPage cancel() {
        log.info("Cancelling account opening");
        scrollToElementAndClick(CANCEL_LINK);
        return new CustomerSelectionPage(driver, baseUrl);
    }
    
    // Validation and information methods
    public boolean isProductInfoDisplayed() {
        return isElementVisible(driver.findElement(PRODUCT_INFO_SECTION));
    }
    
    public String getProductType() {
        return driver.findElement(PRODUCT_TYPE_DISPLAY).getText();
    }
    
    public String getProductCategory() {
        return driver.findElement(PRODUCT_CATEGORY_DISPLAY).getText();
    }
    
    public String getMinimumBalance() {
        return driver.findElement(MIN_BALANCE_DISPLAY).getText();
    }
    
    public String getProductDescription() {
        return driver.findElement(PRODUCT_DESCRIPTION_DISPLAY).getText();
    }
    
    public boolean isMinDepositWarningDisplayed() {
        return isElementVisible(driver.findElement(MIN_DEPOSIT_WARNING));
    }
    
    public String getMinDepositWarningText() {
        return driver.findElement(MIN_DEPOSIT_WARNING).getText();
    }
    
    public String getCustomerName() {
        return driver.findElement(CUSTOMER_NAME_DISPLAY).getText();
    }
    
    public String getCustomerNumber() {
        return driver.findElement(CUSTOMER_NUMBER_DISPLAY).getText();
    }
    
    public boolean isOnAccountOpeningFormPage() {
        return getCurrentUrl().contains("/account/open/") && 
               isElementPresent(PAGE_TITLE) &&
               isElementPresent(PRODUCT_DROPDOWN);
    }
    
    // Complete form filling method for convenience
    public void fillCompleteForm(String productName, String accountName, String initialDeposit, String createdBy) {
        log.info("Filling complete account opening form");
        selectProduct(productName);
        fillAccountName(accountName);
        fillInitialDeposit(initialDeposit);
        if (createdBy != null && !createdBy.isEmpty()) {
            fillCreatedBy(createdBy);
        }
    }
    
    public void fillCompleteFormWithFirstProduct(String accountName, String initialDeposit, String createdBy) {
        log.info("Filling complete account opening form with first available product");
        selectFirstAvailableProduct();
        fillAccountName(accountName);
        fillInitialDeposit(initialDeposit);
        if (createdBy != null && !createdBy.isEmpty()) {
            fillCreatedBy(createdBy);
        }
    }
}