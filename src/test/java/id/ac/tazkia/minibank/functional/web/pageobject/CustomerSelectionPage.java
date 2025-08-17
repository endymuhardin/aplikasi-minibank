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
    private static final By PAGE_TITLE = By.xpath("//h1[contains(text(), 'Select Customer for Account Opening')]");
    private static final By MANAGE_CUSTOMERS_BUTTON = By.linkText("Manage Customers");
    private static final By SEARCH_INPUT = By.name("search");
    private static final By SEARCH_BUTTON = By.xpath("//button[text()='Search']");
    private static final By CUSTOMER_CARDS = By.xpath("//div[contains(@class, 'border-gray-200')]");
    private static final By NO_CUSTOMERS_MESSAGE = By.xpath("//h3[contains(text(), 'No customers found')]");
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
        // Wait for page title and either customer cards or no customers message
        wait.until(ExpectedConditions.presenceOfElementLocated(PAGE_TITLE));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(CUSTOMER_CARDS),
            ExpectedConditions.presenceOfElementLocated(NO_CUSTOMERS_MESSAGE)
        ));
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
        
        // Use ID-based locators for better reliability (following technical practices)
        By customerCard = By.id("customer-card-" + customerNumber);
        By openAccountButton = By.id("open-account-btn-" + customerNumber);
        
        // Wait for customer card to be present
        log.info("⏳ WAITING: For customer card with ID: customer-card-{}", customerNumber);
        wait.until(ExpectedConditions.presenceOfElementLocated(customerCard));
        log.info("✅ FOUND: Customer card for: {}", customerNumber);
        
        // Click the Open Account button
        log.info("🖱️ CLICKING: Open Account button with ID: open-account-btn-{}", customerNumber);
        scrollToElementAndClick(openAccountButton);
        log.info("✅ CLICKED: Open Account button successfully");
        
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