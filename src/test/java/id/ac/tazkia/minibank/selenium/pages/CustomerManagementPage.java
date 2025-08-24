package id.ac.tazkia.minibank.selenium.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomerManagementPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Main page elements
    @FindBy(id = "create-customer-btn")
    private WebElement createCustomerButton;
    
    @FindBy(id = "customer-table")
    private WebElement customerTable;
    
    @FindBy(id = "search")
    private WebElement searchField;
    
    @FindBy(id = "search-btn")
    private WebElement searchButton;
    
    @FindBy(id = "customerTypeFilter")
    private WebElement customerTypeFilter;
    
    @FindBy(id = "customer-count")
    private WebElement customerCount;
    
    @FindBy(id = "search-results")
    private WebElement searchResults;
    
    // Flash messages
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    // Customer type selection elements
    @FindBy(xpath = "//a[@href='/customer/create/personal']")
    private WebElement createPersonalCustomerLink;
    
    @FindBy(xpath = "//a[@href='/customer/create/corporate']")
    private WebElement createCorporateCustomerLink;
    
    public CustomerManagementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to customer list page
     */
    public CustomerManagementPage navigateTo(String baseUrl) {
        driver.get(baseUrl + "/customer/list");
        return waitForPageLoad();
    }
    
    /**
     * Navigate to customer type selection page
     */
    public CustomerManagementPage navigateToCreateCustomer(String baseUrl) {
        driver.get(baseUrl + "/customer/create");
        return waitForCustomerTypeSelectionLoad();
    }
    
    /**
     * Wait for customer list page to load
     */
    public CustomerManagementPage waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(customerTable));
        log.debug("Customer management page loaded successfully");
        return this;
    }
    
    /**
     * Wait for customer type selection page to load
     */
    public CustomerManagementPage waitForCustomerTypeSelectionLoad() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOf(createPersonalCustomerLink),
            ExpectedConditions.visibilityOf(createCorporateCustomerLink)
        ));
        log.debug("Customer type selection page loaded successfully");
        return this;
    }
    
    /**
     * Check if customer management page is loaded
     */
    public boolean isCustomerManagementPageLoaded() {
        try {
            waitForPageLoad();
            return driver.getCurrentUrl().contains("/customer/list") &&
                   driver.getTitle().contains("Customer Management");
        } catch (Exception e) {
            log.debug("Customer management page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if customer type selection page is loaded
     */
    public boolean isCustomerTypeSelectionPageLoaded() {
        try {
            waitForCustomerTypeSelectionLoad();
            return driver.getCurrentUrl().contains("/customer/create") &&
                   createPersonalCustomerLink.isDisplayed() &&
                   createCorporateCustomerLink.isDisplayed();
        } catch (Exception e) {
            log.debug("Customer type selection page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if create customer button is visible
     */
    public boolean isCreateCustomerButtonVisible() {
        try {
            return createCustomerButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click create customer button
     */
    public CustomerManagementPage clickCreateCustomer() {
        wait.until(ExpectedConditions.elementToBeClickable(createCustomerButton));
        createCustomerButton.click();
        log.debug("Clicked create customer button");
        return this;
    }
    
    /**
     * Click create personal customer link
     */
    public CustomerManagementPage clickCreatePersonalCustomer() {
        wait.until(ExpectedConditions.elementToBeClickable(createPersonalCustomerLink));
        createPersonalCustomerLink.click();
        log.debug("Clicked create personal customer link");
        return this;
    }
    
    /**
     * Click create corporate customer link
     */
    public CustomerManagementPage clickCreateCorporateCustomer() {
        wait.until(ExpectedConditions.elementToBeClickable(createCorporateCustomerLink));
        createCorporateCustomerLink.click();
        log.debug("Clicked create corporate customer link");
        return this;
    }
    
    /**
     * Search for customers
     */
    public CustomerManagementPage searchCustomers(String searchTerm) {
        wait.until(ExpectedConditions.visibilityOf(searchField));
        searchField.clear();
        searchField.sendKeys(searchTerm);
        searchButton.click();
        log.debug("Searched for customers with term: {}", searchTerm);
        return this;
    }
    
    /**
     * Filter customers by type
     */
    public CustomerManagementPage filterByCustomerType(String customerType) {
        wait.until(ExpectedConditions.visibilityOf(customerTypeFilter));
        org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(customerTypeFilter);
        select.selectByValue(customerType);
        searchButton.click();
        log.debug("Filtered customers by type: {}", customerType);
        return this;
    }
    
    /**
     * Get customer count from the page
     */
    public int getCustomerCount() {
        try {
            wait.until(ExpectedConditions.visibilityOf(customerCount));
            String countStr = customerCount.getAttribute("data-count");
            return Integer.parseInt(countStr);
        } catch (Exception e) {
            log.debug("Could not get customer count: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Check if customers are displayed in the table
     */
    public boolean areCustomersDisplayed() {
        try {
            return getCustomerCount() > 0;
        } catch (Exception e) {
            return false;
        }
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
     * Check if a specific customer is visible in the table by customer number
     */
    public boolean isCustomerVisible(String customerNumber) {
        try {
            WebElement customerRow = driver.findElement(
                org.openqa.selenium.By.id("customer-" + customerNumber)
            );
            return customerRow.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click view button for a specific customer
     */
    public CustomerManagementPage clickViewCustomer(String customerNumber) {
        try {
            WebElement viewButton = driver.findElement(
                org.openqa.selenium.By.id("view-" + customerNumber)
            );
            wait.until(ExpectedConditions.elementToBeClickable(viewButton));
            viewButton.click();
            log.debug("Clicked view button for customer: {}", customerNumber);
        } catch (Exception e) {
            log.error("Could not click view button for customer: {}", customerNumber, e);
        }
        return this;
    }
    
    /**
     * Click edit button for a specific customer
     */
    public CustomerManagementPage clickEditCustomer(String customerNumber) {
        try {
            WebElement editButton = driver.findElement(
                org.openqa.selenium.By.id("edit-" + customerNumber)
            );
            wait.until(ExpectedConditions.elementToBeClickable(editButton));
            editButton.click();
            log.debug("Clicked edit button for customer: {}", customerNumber);
        } catch (Exception e) {
            log.error("Could not click edit button for customer: {}", customerNumber, e);
        }
        return this;
    }
    
    /**
     * Get customer status from the table
     */
    public String getCustomerStatus(String customerNumber) {
        try {
            WebElement statusElement = driver.findElement(
                org.openqa.selenium.By.id("status-" + customerNumber)
            );
            return statusElement.getText();
        } catch (Exception e) {
            log.debug("Could not get status for customer: {}", customerNumber);
            return "";
        }
    }
}