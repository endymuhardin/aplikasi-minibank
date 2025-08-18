package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

@Slf4j
public class CustomerSelectionPage extends BasePage {
    
    // Page elements
    private static final By PAGE_TITLE = By.id("page-title");
    private static final By MANAGE_CUSTOMERS_BUTTON = By.id("manage-customers-button");
    private static final By SEARCH_INPUT = By.id("search-input");
    private static final By SEARCH_BUTTON = By.id("search-button");
    private static final By CUSTOMER_CARDS = By.id("customer-cards-container");
    private static final By NO_CUSTOMERS_MESSAGE = By.id("no-customers-message");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By ERROR_MESSAGE = By.id("error-message");
    
    public CustomerSelectionPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void open() {
        driver.get(baseUrl + "/account/open");
        waitForPageToLoad();
    }
    
    public void openAndWaitForLoad() {
        open();
        log.info("🔍 CUSTOMER SELECTION: Opened customer selection page");
        log.info("📄 CURRENT URL: {}", driver.getCurrentUrl());
        
        // Wait for page title and either customer cards or no customers message
        wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_TITLE));
        log.info("✅ FOUND: Page title element");
        
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(CUSTOMER_CARDS),
            ExpectedConditions.presenceOfElementLocated(NO_CUSTOMERS_MESSAGE)
        ));
        
        // Debug what we actually found
        boolean hasCustomers = isElementPresent(CUSTOMER_CARDS);
        boolean hasNoCustomersMessage = isElementPresent(NO_CUSTOMERS_MESSAGE);
        
        log.info("🔍 CUSTOMER PRESENCE: hasCustomers={}, hasNoCustomersMessage={}", hasCustomers, hasNoCustomersMessage);
        
        if (hasNoCustomersMessage) {
            log.warn("⚠️ NO CUSTOMERS MESSAGE: The page shows 'No customers found'");
            log.warn("🔍 PAGE SOURCE PREVIEW: {}", driver.getPageSource().substring(0, Math.min(1000, driver.getPageSource().length())));
        } else if (hasCustomers) {
            int customerCount = driver.findElements(CUSTOMER_CARDS).size();
            log.info("✅ CUSTOMERS FOUND: {} customer cards present", customerCount);
        }
    }
    
    public void searchCustomers(String searchTerm) {
        log.info("Searching for customers with term: {}", searchTerm);
        clearAndType(driver.findElement(SEARCH_INPUT), searchTerm);
        scrollToElementAndClick(SEARCH_BUTTON);
        waitForPageToLoad();
        
        // Wait for search results to load
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(CUSTOMER_CARDS),
            ExpectedConditions.presenceOfElementLocated(NO_CUSTOMERS_MESSAGE)
        ));
    }
    
    public AccountOpeningFormPage selectCustomer(String customerNumber) {
        log.info("🔍 CUSTOMER SELECTION: Looking for customer number: {}", customerNumber);
        log.info("📄 CURRENT URL: {}", driver.getCurrentUrl());
        log.info("📝 PAGE TITLE: {}", driver.getTitle());
        
        // Use ID-based locators for better reliability (following technical practices)
        By customerCard = By.id("customer-card-" + customerNumber);
        By openAccountButton = By.id("open-account-btn-" + customerNumber);
        
        try {
            // Wait for customer card to be present
            log.info("⏳ WAITING: For customer card with ID: customer-card-{}", customerNumber);
            wait.until(ExpectedConditions.presenceOfElementLocated(customerCard));
            log.info("✅ FOUND: Customer card for: {}", customerNumber);
            
            // Wait for Open Account button to be clickable
            log.info("⏳ WAITING: For Open Account button to be clickable with ID: open-account-btn-{}", customerNumber);
            wait.until(ExpectedConditions.elementToBeClickable(openAccountButton));
            log.info("✅ READY: Open Account button is clickable");
            
            // Click the Open Account button
            log.info("🖱️ CLICKING: Open Account button with ID: open-account-btn-{}", customerNumber);
            scrollToElementAndClick(openAccountButton);
            log.info("✅ CLICKED: Open Account button successfully");
            
            // Wait for navigation to complete
            log.info("⏳ WAITING: For navigation to account opening form");
            wait.until(ExpectedConditions.urlContains("/account/open/"));
            log.info("✅ NAVIGATION: Successfully navigated to account opening form");
            log.info("📄 NEW URL: {}", driver.getCurrentUrl());
            
        } catch (Exception e) {
            log.error("❌ ERROR: Failed to select customer {}", customerNumber);
            log.error("📄 CURRENT URL: {}", driver.getCurrentUrl());
            log.error("📝 PAGE TITLE: {}", driver.getTitle());
            log.error("🔍 PAGE SOURCE PREVIEW: {}", driver.getPageSource().substring(0, Math.min(500, driver.getPageSource().length())));
            throw e;
        }
        
        return new AccountOpeningFormPage(driver, baseUrl);
    }
    
    public CustomerListPage clickManageCustomers() {
        log.info("Clicking Manage Customers button");
        scrollToElementAndClick(MANAGE_CUSTOMERS_BUTTON);
        return new CustomerListPage(driver, baseUrl);
    }
    
    public boolean hasCustomers() {
        return !isElementPresent(NO_CUSTOMERS_MESSAGE);
    }
    
    public int getCustomerCount() {
        if (!hasCustomers()) {
            return 0;
        }
        List<WebElement> cards = driver.findElements(CUSTOMER_CARDS);
        return cards.size();
    }
    
    public boolean isCustomerDisplayed(String customerNumber) {
        return isElementPresent(By.id("customer-card-" + customerNumber));
    }
    
    public boolean isOnCustomerSelectionPage() {
        return getCurrentUrl().contains("/account/open") && 
               isElementPresent(PAGE_TITLE);
    }
}