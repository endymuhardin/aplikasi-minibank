package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

@Slf4j
public class CorporateCustomerSelectionPage extends BasePage {
    
    // Page elements
    private static final By PAGE_TITLE = By.id("page-title");
    private static final By PERSONAL_ACCOUNTS_BUTTON = By.linkText("Personal Accounts");
    private static final By MANAGE_CUSTOMERS_BUTTON = By.linkText("Manage Customers");
    private static final By SEARCH_INPUT = By.id("search-input");
    private static final By SEARCH_BUTTON = By.id("search-button");
    private static final By CUSTOMER_CARDS = By.className("corporate-customer-card");
    private static final By NO_CUSTOMERS_MESSAGE = By.id("no-customers-message");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By ERROR_MESSAGE = By.id("error-message");
    
    // Corporate-specific elements
    private static final By CORPORATE_BADGE = By.id("corporate-badge");
    private static final By COMPANY_NAME = By.xpath("//h3[contains(@class, 'text-xl')]");
    private static final By REGISTRATION_NUMBER = By.xpath("//p[contains(text(), 'Registration Number:')]");
    private static final By TAX_ID = By.xpath("//p[contains(text(), 'Tax ID')]");
    private static final By CONTACT_PERSON = By.xpath("//p[contains(text(), 'Contact Person:')]");
    
    public CorporateCustomerSelectionPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void open() {
        driver.get(baseUrl + "/account/open/corporate");
        waitForPageToLoad();
    }
    
    public void openAndWaitForLoad() {
        open();
        // Wait for page title and either customer cards or no customers message
        wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_TITLE));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(CUSTOMER_CARDS),
            ExpectedConditions.presenceOfElementLocated(NO_CUSTOMERS_MESSAGE)
        ));
    }
    
    public void searchCorporateCustomers(String searchTerm) {
        log.info("Searching for corporate customers with term: {}", searchTerm);
        clearAndType(driver.findElement(SEARCH_INPUT), searchTerm);
        scrollToElementAndClick(SEARCH_BUTTON);
        waitForPageToLoad();
        
        // Wait for search results to load
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(CUSTOMER_CARDS),
            ExpectedConditions.presenceOfElementLocated(NO_CUSTOMERS_MESSAGE)
        ));
    }
    
    public CorporateAccountOpeningFormPage selectCorporateCustomer(String customerNumber) {
        log.info("Selecting corporate customer with number: {}", customerNumber);
        
        // Use ID-based selectors for more reliable element location
        By customerCard = By.id("corporate-customer-card-" + customerNumber);
        By openAccountButton = By.id("open-account-btn-" + customerNumber);
        
        // Wait for customer card to be present
        wait.until(ExpectedConditions.presenceOfElementLocated(customerCard));
        
        // Click the Open Corporate Account button
        scrollToElementAndClick(openAccountButton);
        
        return new CorporateAccountOpeningFormPage(driver, baseUrl);
    }
    
    public CustomerListPage clickManageCustomers() {
        log.info("Clicking Manage Customers button");
        scrollToElementAndClick(MANAGE_CUSTOMERS_BUTTON);
        return new CustomerListPage(driver, baseUrl);
    }
    
    public CustomerSelectionPage clickPersonalAccounts() {
        log.info("Clicking Personal Accounts button");
        scrollToElementAndClick(PERSONAL_ACCOUNTS_BUTTON);
        return new CustomerSelectionPage(driver, baseUrl);
    }
    
    public boolean hasCorporateCustomers() {
        return !isElementPresent(NO_CUSTOMERS_MESSAGE);
    }
    
    public int getCorporateCustomerCount() {
        if (!hasCorporateCustomers()) {
            return 0;
        }
        List<WebElement> cards = driver.findElements(CUSTOMER_CARDS);
        return cards.size();
    }
    
    public boolean isCorporateCustomerDisplayed(String customerNumber) {
        return isElementPresent(By.id("corporate-customer-card-" + customerNumber));
    }
    
    public boolean isCorporateCustomerDisplayedByCompanyName(String companyName) {
        return isElementPresent(By.xpath("//h3[contains(text(), '" + companyName + "')]"));
    }
    
    public boolean verifyCorporateBadgeDisplayed() {
        return isElementPresent(CORPORATE_BADGE);
    }
    
    public String getFirstCorporateCustomerCompanyName() {
        try {
            WebElement companyElement = driver.findElement(COMPANY_NAME);
            return companyElement.getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean verifyRegistrationNumberDisplayed() {
        return isElementPresent(REGISTRATION_NUMBER);
    }
    
    public boolean verifyTaxIdDisplayed() {
        return isElementPresent(TAX_ID);
    }
    
    public boolean verifyContactPersonDisplayed() {
        return isElementPresent(CONTACT_PERSON);
    }
    
    public boolean isOnCorporateCustomerSelectionPage() {
        return getCurrentUrl().contains("/account/open/corporate") && 
               isElementPresent(PAGE_TITLE);
    }
}