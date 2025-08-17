package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
        log.info("🔍 WAIT: Starting to wait for account opening form page to load");
        log.info("📄 CURRENT URL: {}", driver.getCurrentUrl());
        log.info("📝 PAGE TITLE: {}", driver.getTitle());
        
        try {
            // Wait for page title and form elements to be present
            log.info("⏳ WAITING: For page title element with ID: page-title");
            wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_TITLE));
            log.info("✅ FOUND: Page title element");
            
            log.info("⏳ WAITING: For product dropdown with ID: productId");
            wait.until(ExpectedConditions.presenceOfElementLocated(PRODUCT_DROPDOWN));
            log.info("✅ FOUND: Product dropdown element");
            
            log.info("⏳ WAITING: For account name input with ID: accountName");
            wait.until(ExpectedConditions.presenceOfElementLocated(ACCOUNT_NAME_INPUT));
            log.info("✅ FOUND: Account name input element");
            
            log.info("⏳ WAITING: For initial deposit input with ID: initialDeposit");
            wait.until(ExpectedConditions.presenceOfElementLocated(INITIAL_DEPOSIT_INPUT));
            log.info("✅ FOUND: Initial deposit input element");
            
            waitForPageToLoad();
            log.info("✅ SUCCESS: Account opening form page loaded successfully");
            
        } catch (Exception e) {
            log.error("❌ ERROR: Failed to load account opening form page");
            log.error("📄 CURRENT URL: {}", driver.getCurrentUrl());
            log.error("📝 PAGE TITLE: {}", driver.getTitle());
            log.error("🔍 PAGE SOURCE PREVIEW: {}", driver.getPageSource().substring(0, Math.min(500, driver.getPageSource().length())));
            throw e;
        }
    }
    
    public void selectProduct(String productName) {
        log.info("Selecting product: {}", productName);
        WebElement productDropdown = driver.findElement(PRODUCT_DROPDOWN);
        selectDropdownByText(productDropdown, productName);
        
        // Optional: Try to trigger the onChange event, but don't wait for JavaScript
        try {
            ((JavascriptExecutor) driver).executeScript(
                "var event = new Event('change', { bubbles: true }); " +
                "arguments[0].dispatchEvent(event);", productDropdown);
            log.debug("Triggered onChange event for product selection");
        } catch (Exception e) {
            log.debug("Could not trigger onChange event, but product is still selected: {}", e.getMessage());
        }
        
        log.info("✅ Product '{}' selected successfully", productName);
    }
    
    public void selectProductByValue(String productId) {
        log.info("Selecting product by ID: {}", productId);
        WebElement productDropdown = driver.findElement(PRODUCT_DROPDOWN);
        selectDropdownByValue(productDropdown, productId);
        
        // Trigger the onChange event and wait for product info to update
        try {
            ((JavascriptExecutor) driver).executeScript(
                "var event = new Event('change', { bubbles: true }); " +
                "arguments[0].dispatchEvent(event);", productDropdown);
            log.debug("Triggered onChange event for product selection");
            
            // Wait for product info to become visible (if it should)
            try {
                wait.until(org.openqa.selenium.support.ui.ExpectedConditions.or(
                    org.openqa.selenium.support.ui.ExpectedConditions.attributeContains(PRODUCT_INFO_SECTION, "class", ""),
                    org.openqa.selenium.support.ui.ExpectedConditions.not(
                        org.openqa.selenium.support.ui.ExpectedConditions.attributeContains(PRODUCT_INFO_SECTION, "class", "hidden")
                    )
                ));
                log.debug("Product information section updated after selection");
            } catch (Exception waitException) {
                log.debug("Product info section may not have updated or doesn't exist: {}", waitException.getMessage());
            }
        } catch (Exception e) {
            log.debug("Could not trigger onChange event, but product is still selected: {}", e.getMessage());
        }
        
        log.info("✅ Product with ID '{}' selected successfully", productId);
    }
    
    public void selectFirstAvailableProduct() {
        log.info("Selecting first available product");
        WebElement productDropdown = driver.findElement(PRODUCT_DROPDOWN);
        selectFirstNonEmptyOption(productDropdown);
        
        // Optional: Try to trigger the onChange event, but don't wait for JavaScript
        try {
            ((JavascriptExecutor) driver).executeScript(
                "var event = new Event('change', { bubbles: true }); " +
                "arguments[0].dispatchEvent(event);", productDropdown);
            log.debug("Triggered onChange event for product selection");
        } catch (Exception e) {
            log.debug("Could not trigger onChange event, but product is still selected: {}", e.getMessage());
        }
        
        log.info("✅ Product selected successfully");
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
        // Note: min-deposit-warning is present but hidden, so we check visibility
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.visibilityOfElementLocated(VALIDATION_ERRORS),
            ExpectedConditions.visibilityOfElementLocated(MIN_DEPOSIT_WARNING),
            // Fallback: check if form submission failed (stayed on same page)
            ExpectedConditions.presenceOfElementLocated(SUBMIT_BUTTON)
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
        try {
            // First check if element exists
            WebElement productInfo = driver.findElement(PRODUCT_INFO_SECTION);
            
            // Check if the element does not have the 'hidden' class
            String classAttribute = productInfo.getAttribute("class");
            boolean isVisible = classAttribute == null || !classAttribute.contains("hidden");
            
            log.debug("Product info section classes: {}, isVisible: {}", classAttribute, isVisible);
            return isVisible;
        } catch (Exception e) {
            log.debug("Product information section not found or not accessible: {}", e.getMessage());
            return false;
        }
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
        String currentUrl = getCurrentUrl();
        // Accept both /account/open (error page) and /account/open/{customerId} (form page)
        boolean correctUrl = currentUrl.contains("/account/open");
        return correctUrl && 
               isElementPresent(PAGE_TITLE) &&
               isElementPresent(PRODUCT_DROPDOWN);
    }
    
    public boolean isErrorMessageDisplayed() {
        return isElementPresent(ERROR_MESSAGE) || isElementPresent(VALIDATION_ERRORS);
    }
    
    public String getErrorMessage() {
        try {
            if (isElementPresent(ERROR_MESSAGE)) {
                return driver.findElement(ERROR_MESSAGE).getText();
            } else if (isElementPresent(VALIDATION_ERRORS)) {
                return driver.findElement(VALIDATION_ERRORS).getText();
            }
            return "";
        } catch (Exception e) {
            log.debug("Could not get error message: {}", e.getMessage());
            return "";
        }
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