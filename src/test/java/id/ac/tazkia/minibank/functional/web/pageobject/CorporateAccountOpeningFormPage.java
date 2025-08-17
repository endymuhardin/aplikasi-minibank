package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Slf4j
public class CorporateAccountOpeningFormPage extends BasePage {
    
    // Page elements
    private static final By PAGE_TITLE = By.xpath("//h1[contains(text(), 'Open Corporate Account')]");
    private static final By BACK_TO_SELECTION_LINK = By.linkText("← Back to Corporate Customer Selection");
    private static final By PRODUCT_DROPDOWN = By.id("productId");
    private static final By ACCOUNT_NAME_INPUT = By.id("accountName");
    private static final By INITIAL_DEPOSIT_INPUT = By.id("initialDeposit");
    private static final By CREATED_BY_INPUT = By.id("createdBy");
    private static final By SUBMIT_BUTTON = By.xpath("//button[contains(text(), 'Open Corporate Account')]");
    private static final By CANCEL_LINK = By.linkText("Cancel");
    
    // Corporate product information section
    private static final By CORPORATE_PRODUCT_INFO_SECTION = By.id("corporate-product-info");
    private static final By CORPORATE_PRODUCT_TYPE_DISPLAY = By.id("corporate-product-type");
    private static final By CORPORATE_PRODUCT_CATEGORY_DISPLAY = By.id("corporate-product-category");
    private static final By STANDARD_MIN_BALANCE_DISPLAY = By.id("standard-min-balance");
    private static final By CORPORATE_MIN_BALANCE_DISPLAY = By.id("corporate-min-balance");
    private static final By CORPORATE_PRODUCT_DESCRIPTION_DISPLAY = By.id("corporate-product-description");
    
    // Corporate-specific validation and messages
    private static final By CORPORATE_MIN_DEPOSIT_WARNING = By.id("corporate-min-deposit-warning");
    private static final By CORPORATE_DEPOSIT_INFO = By.id("corporate-deposit-info");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By ERROR_MESSAGE = By.id("error-message");
    private static final By VALIDATION_ERRORS = By.id("validation-errors");
    
    // Corporate customer information section (read-only display)
    private static final By CORPORATE_CUSTOMER_SECTION = By.xpath("//div[contains(@class, 'from-purple-50')]");
    private static final By COMPANY_NAME_DISPLAY = By.xpath("//h3[contains(@class, 'text-xl')]");
    private static final By CUSTOMER_NUMBER_DISPLAY = By.xpath("//p[contains(text(), 'Customer Number:')]//span");
    private static final By REGISTRATION_NUMBER_DISPLAY = By.xpath("//p[contains(text(), 'Registration Number:')]//span");
    private static final By TAX_ID_DISPLAY = By.xpath("//p[contains(text(), 'Tax ID')]//span");
    private static final By CONTACT_PERSON_DISPLAY = By.xpath("//p[contains(text(), 'Contact Person:')]//span");
    
    // Corporate banking benefits section
    private static final By CORPORATE_BENEFITS_SECTION = By.xpath("//h4[contains(text(), 'Corporate Banking Benefits')]");
    
    public CorporateAccountOpeningFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void waitForPageLoad() {
        // Wait for page title and form elements to be present
        wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_TITLE));
        wait.until(ExpectedConditions.presenceOfElementLocated(PRODUCT_DROPDOWN));
        wait.until(ExpectedConditions.presenceOfElementLocated(ACCOUNT_NAME_INPUT));
        wait.until(ExpectedConditions.presenceOfElementLocated(INITIAL_DEPOSIT_INPUT));
        wait.until(ExpectedConditions.presenceOfElementLocated(CORPORATE_CUSTOMER_SECTION));
        waitForPageToLoad();
    }
    
    public void selectCorporateProduct(String productName) {
        log.info("Selecting corporate product: {}", productName);
        WebElement productDropdown = driver.findElement(PRODUCT_DROPDOWN);
        selectDropdownByText(productDropdown, productName);
        
        // Wait for corporate product information to be displayed
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(CORPORATE_PRODUCT_INFO_SECTION),
            ExpectedConditions.invisibilityOfElementLocated(CORPORATE_PRODUCT_INFO_SECTION)
        ));
    }
    
    public void selectCorporateProductByValue(String productId) {
        log.info("Selecting corporate product by ID: {}", productId);
        WebElement productDropdown = driver.findElement(PRODUCT_DROPDOWN);
        selectDropdownByValue(productDropdown, productId);
        
        // Wait for corporate product information to be displayed
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(CORPORATE_PRODUCT_INFO_SECTION),
            ExpectedConditions.invisibilityOfElementLocated(CORPORATE_PRODUCT_INFO_SECTION)
        ));
    }
    
    public void selectFirstAvailableCorporateProduct() {
        log.info("Selecting first available corporate product");
        WebElement productDropdown = driver.findElement(PRODUCT_DROPDOWN);
        selectFirstNonEmptyOption(productDropdown);
        
        // Wait for corporate product information to be displayed
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(CORPORATE_PRODUCT_INFO_SECTION),
            ExpectedConditions.invisibilityOfElementLocated(CORPORATE_PRODUCT_INFO_SECTION)
        ));
    }
    
    public void fillCorporateAccountName(String accountName) {
        log.info("Filling corporate account name: {}", accountName);
        clearAndType(driver.findElement(ACCOUNT_NAME_INPUT), accountName);
    }
    
    public void fillInitialDeposit(String amount) {
        log.info("Filling corporate initial deposit: {}", amount);
        clearAndType(driver.findElement(INITIAL_DEPOSIT_INPUT), amount);
    }
    
    public void fillAccountManager(String manager) {
        log.info("Filling account manager: {}", manager);
        clearAndType(driver.findElement(CREATED_BY_INPUT), manager);
    }
    
    public AccountListPage submitCorporateForm() {
        log.info("Submitting corporate account opening form");
        scrollToElementAndClick(SUBMIT_BUTTON);
        
        // Wait for either success (redirect to list) or error (stay on form)
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/account/list"),
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERRORS)
        ));
        
        return new AccountListPage(driver, baseUrl);
    }
    
    public void submitCorporateFormExpectingError() {
        log.info("Submitting corporate account opening form expecting validation error");
        scrollToElementAndClick(SUBMIT_BUTTON);
        
        // Wait for error message or validation errors to appear
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERRORS),
            ExpectedConditions.presenceOfElementLocated(CORPORATE_MIN_DEPOSIT_WARNING)
        ));
    }
    
    public CorporateCustomerSelectionPage goBackToCorporateCustomerSelection() {
        log.info("Going back to corporate customer selection");
        scrollToElementAndClick(BACK_TO_SELECTION_LINK);
        return new CorporateCustomerSelectionPage(driver, baseUrl);
    }
    
    public CorporateCustomerSelectionPage cancel() {
        log.info("Cancelling corporate account opening");
        scrollToElementAndClick(CANCEL_LINK);
        return new CorporateCustomerSelectionPage(driver, baseUrl);
    }
    
    // Corporate-specific validation and information methods
    public boolean isCorporateProductInfoDisplayed() {
        return isElementVisible(driver.findElement(CORPORATE_PRODUCT_INFO_SECTION));
    }
    
    public String getCorporateProductType() {
        return driver.findElement(CORPORATE_PRODUCT_TYPE_DISPLAY).getText();
    }
    
    public String getCorporateProductCategory() {
        return driver.findElement(CORPORATE_PRODUCT_CATEGORY_DISPLAY).getText();
    }
    
    public String getStandardMinimumBalance() {
        return driver.findElement(STANDARD_MIN_BALANCE_DISPLAY).getText();
    }
    
    public String getCorporateMinimumBalance() {
        return driver.findElement(CORPORATE_MIN_BALANCE_DISPLAY).getText();
    }
    
    public String getCorporateProductDescription() {
        return driver.findElement(CORPORATE_PRODUCT_DESCRIPTION_DISPLAY).getText();
    }
    
    public boolean isCorporateMinDepositWarningDisplayed() {
        return isElementVisible(driver.findElement(CORPORATE_MIN_DEPOSIT_WARNING));
    }
    
    public String getCorporateMinDepositWarningText() {
        return driver.findElement(CORPORATE_MIN_DEPOSIT_WARNING).getText();
    }
    
    public boolean isCorporateDepositInfoDisplayed() {
        return isElementVisible(driver.findElement(CORPORATE_DEPOSIT_INFO));
    }
    
    // Corporate customer information methods
    public String getCompanyName() {
        return driver.findElement(COMPANY_NAME_DISPLAY).getText();
    }
    
    public String getCustomerNumber() {
        return driver.findElement(CUSTOMER_NUMBER_DISPLAY).getText();
    }
    
    public String getRegistrationNumber() {
        try {
            return driver.findElement(REGISTRATION_NUMBER_DISPLAY).getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getTaxId() {
        try {
            return driver.findElement(TAX_ID_DISPLAY).getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getContactPerson() {
        try {
            return driver.findElement(CONTACT_PERSON_DISPLAY).getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isCorporateBenefitsSectionDisplayed() {
        return isElementPresent(CORPORATE_BENEFITS_SECTION);
    }
    
    public boolean isOnCorporateAccountOpeningFormPage() {
        return getCurrentUrl().contains("/account/open/corporate/") && 
               isElementPresent(PAGE_TITLE) &&
               isElementPresent(PRODUCT_DROPDOWN) &&
               isElementPresent(CORPORATE_CUSTOMER_SECTION);
    }
    
    // Complete form filling method for convenience
    public void fillCompleteCorporateForm(String productName, String accountName, String initialDeposit, String accountManager) {
        log.info("Filling complete corporate account opening form");
        selectCorporateProduct(productName);
        fillCorporateAccountName(accountName);
        fillInitialDeposit(initialDeposit);
        if (accountManager != null && !accountManager.isEmpty()) {
            fillAccountManager(accountManager);
        }
    }
    
    public void fillCompleteCorporateFormWithFirstProduct(String accountName, String initialDeposit, String accountManager) {
        log.info("Filling complete corporate account opening form with first available product");
        selectFirstAvailableCorporateProduct();
        fillCorporateAccountName(accountName);
        fillInitialDeposit(initialDeposit);
        if (accountManager != null && !accountManager.isEmpty()) {
            fillAccountManager(accountManager);
        }
    }
}