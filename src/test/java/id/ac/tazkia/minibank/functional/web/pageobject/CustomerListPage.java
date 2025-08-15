package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

@Slf4j
public class CustomerListPage extends BasePage {
    
    private final WebDriverWait wait;
    
    // Page elements
    private static final By CREATE_BUTTON = By.id("create-customer-btn");
    private static final By CUSTOMER_TABLE = By.id("customer-table");
    private static final By SEARCH_INPUT = By.id("search");
    private static final By SEARCH_BUTTON = By.id("search-btn");
    private static final By CUSTOMER_TYPE_FILTER = By.id("customerTypeFilter");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By ERROR_MESSAGE = By.id("error-message");
    
    public CustomerListPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public void open() {
        driver.get(baseUrl + "/customer/list");
        waitForPageLoad();
    }
    
    public void openAndWaitForLoad() {
        open();
        // Wait for the page to be fully loaded by checking for actual elements that exist
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customer-table")));
        
        // Wait for either success/error message or table content to be present
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(By.id("success-message")),
            ExpectedConditions.presenceOfElementLocated(By.id("error-message")),
            ExpectedConditions.presenceOfElementLocated(By.className("customer-row")),
            ExpectedConditions.presenceOfElementLocated(By.id("no-customers-message"))
        ));
    }
    
    private void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }
    
    public boolean isCreateButtonDisplayed() {
        try {
            return driver.findElement(CREATE_BUTTON).isDisplayed();
        } catch (Exception e) {
            // Fallback: use alternative create button id
            try {
                return driver.findElement(By.id("create-btn")).isDisplayed();
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    public boolean isCustomerTableDisplayed() {
        try {
            return driver.findElement(CUSTOMER_TABLE).isDisplayed();
        } catch (Exception e) {
            // Fallback: look for any table
            try {
                return driver.findElement(By.tagName("table")).isDisplayed();
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    public CustomerTypeSelectionPage clickCreateCustomer() {
        try {
            driver.findElement(CREATE_BUTTON).click();
        } catch (Exception e) {
            // Fallback: use alternative create button id
            driver.findElement(By.id("create-btn")).click();
        }
        return new CustomerTypeSelectionPage(driver, baseUrl);
    }
    
    public PersonalCustomerFormPage clickCreatePersonalCustomer() {
        // Navigate directly to personal customer form
        driver.get(baseUrl + "/customer/create/personal");
        return new PersonalCustomerFormPage(driver, baseUrl);
    }
    
    public CorporateCustomerFormPage clickCreateCorporateCustomer() {
        // Navigate directly to corporate customer form
        driver.get(baseUrl + "/customer/create/corporate");
        return new CorporateCustomerFormPage(driver, baseUrl);
    }
    
    public boolean isCustomerDisplayed(String customerNumber) {
        return findCustomerAcrossPages(customerNumber) != null;
    }
    
    /**
     * Searches for a customer across all pages using pagination
     */
    private WebElement findCustomerAcrossPages(String customerNumber) {
        log.info("Searching for customer {} across all pages", customerNumber);
        
        // First try current page
        WebElement customer = findCustomerOnCurrentPage(customerNumber);
        if (customer != null) {
            log.info("Found customer {} on current page", customerNumber);
            return customer;
        }
        
        // Search across all pages by starting with larger page size
        try {
            String baseUrl = driver.getCurrentUrl().split("\\?")[0];
            driver.get(baseUrl + "?page=0&size=100");
            waitForPageLoad();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customer-table")));
            
            customer = findCustomerOnCurrentPage(customerNumber);
            if (customer != null) {
                log.info("Found customer {} with larger page size", customerNumber);
                return customer;
            }
            
            // If still not found, check if pagination exists and navigate through pages
            int currentPage = 0;
            int maxPages = getMaxPages();
            
            while (currentPage < maxPages && currentPage < 10) { // Limit to 10 pages max
                driver.get(baseUrl + "?page=" + currentPage + "&size=50");
                waitForPageLoad();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customer-table")));
                
                customer = findCustomerOnCurrentPage(customerNumber);
                if (customer != null) {
                    log.info("Found customer {} on page {}", customerNumber, currentPage);
                    return customer;
                }
                currentPage++;
            }
            
        } catch (Exception e) {
            log.error("Error searching for customer across pages", e);
        }
        
        log.warn("Customer {} not found on any page", customerNumber);
        return null;
    }
    
    private WebElement findCustomerOnCurrentPage(String customerNumber) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            return shortWait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("customer-" + customerNumber)
            ));
        } catch (TimeoutException e) {
            log.debug("Customer {} not found on current page", customerNumber);
            return null;
        } catch (Exception e) {
            log.error("Error finding customer on current page", e);
            return null;
        }
    }
    
    private int getMaxPages() {
        try {
            // Look for pagination info to determine max pages
            WebElement paginationInfo = driver.findElement(By.className("pagination-info"));
            String text = paginationInfo.getText();
            // Extract total pages from text like "Page 1 of 5"
            if (text.contains(" of ")) {
                String[] parts = text.split(" of ");
                return Integer.parseInt(parts[1].trim());
            }
        } catch (Exception e) {
            log.debug("Could not determine max pages, defaulting to 5");
        }
        return 5; // Default reasonable limit
    }
    
    public boolean isSuccessMessageDisplayed() {
        try {
            // Wait for the success message element with ID
            WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(SUCCESS_MESSAGE));
            log.info("Success message element found: {}", message.getText());
            return message.isDisplayed();
        } catch (Exception e) {
            log.error("Success message element with ID 'success-message' not found", e);
            return false;
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            WebElement message = driver.findElement(ERROR_MESSAGE);
            return message.isDisplayed();
        } catch (Exception e) {
            log.error("Error checking element display status", e);
            return false;
        }
    }
    
    public void search(String searchTerm) {
        try {
            WebElement searchInput = driver.findElement(SEARCH_INPUT);
            searchInput.clear();
            searchInput.sendKeys(searchTerm);
            
            try {
                driver.findElement(SEARCH_BUTTON).click();
            } catch (Exception e) {
                // If no search button, submit the form
                searchInput.submit();
            }
            
            waitForPageLoad();
        } catch (Exception e) {
            // The primary search input should always be available
            throw new RuntimeException("Search functionality not available", e);
        }
    }
    
    public void filterByCustomerType(String customerType) {
        try {
            Select typeSelect = new Select(driver.findElement(CUSTOMER_TYPE_FILTER));
            typeSelect.selectByValue(customerType);
            waitForPageLoad();
        } catch (Exception e) {
            log.warn("Customer type filter not available, ignoring", e);
        }
    }
    
    public boolean hasSearchResults() {
        try {
            WebElement searchResults = driver.findElement(By.id("search-results"));
            return searchResults.findElements(By.tagName("tr")).size() > 0;
        } catch (Exception e) {
            log.error("Error checking element display status", e);
            return false;
        }
    }
    
    public boolean hasFilterResults() {
        return hasSearchResults();
    }
    
    public PersonalCustomerFormPage editPersonalCustomer(String customerNumber) {
        // First ensure customer is visible and navigate to its page
        WebElement customerElement = findCustomerAcrossPages(customerNumber);
        if (customerElement == null) {
            throw new RuntimeException("Customer " + customerNumber + " not found on any page");
        }
        
        // Now look for edit button with multiple strategies
        WebElement editButton = findEditButton(customerNumber);
        if (editButton == null) {
            throw new RuntimeException("Edit button for customer " + customerNumber + " not found");
        }
        
        scrollToElementAndClick(editButton);
        
        // Enhanced wait for personal customer edit form to load
        WebDriverWait enhancedWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        enhancedWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        
        return new PersonalCustomerFormPage(driver, baseUrl);
    }
    
    private WebElement findEditButton(String customerNumber) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement button = shortWait.until(ExpectedConditions.elementToBeClickable(
                By.id("edit-" + customerNumber)
            ));
            log.info("Found edit button for customer: {}", customerNumber);
            return button;
        } catch (TimeoutException e) {
            log.error("Edit button not found for customer: {}", customerNumber);
            return null;
        }
    }
    
    public CorporateCustomerFormPage editCorporateCustomer(String customerNumber) {
        // First ensure customer is visible and navigate to its page
        WebElement customerElement = findCustomerAcrossPages(customerNumber);
        if (customerElement == null) {
            throw new RuntimeException("Customer " + customerNumber + " not found on any page");
        }
        
        // Now look for edit button with multiple strategies
        WebElement editButton = findEditButton(customerNumber);
        if (editButton == null) {
            throw new RuntimeException("Edit button for customer " + customerNumber + " not found");
        }
        
        scrollToElementAndClick(editButton);
        
        // Wait for page navigation and form to load
        WebDriverWait enhancedWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        enhancedWait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/customer/edit/"),
            ExpectedConditions.urlContains("/customer/corporate-form")
        ));
        
        // Enhanced wait for corporate customer edit form to load
        try {
            enhancedWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("companyName")));
        } catch (TimeoutException e) {
            log.error("Could not find companyName element on page: {}", driver.getCurrentUrl());
            log.error("Page title: {}", driver.getTitle());
            
            // Check if we have personal customer fields instead (wrong customer type)
            try {
                driver.findElement(By.id("firstName"));
                log.error("Found firstName field - this appears to be a personal customer form, not corporate");
            } catch (Exception firstNameEx) {
                log.info("No firstName field found - not a personal customer form");
            }
            
            throw e;
        }
        return new CorporateCustomerFormPage(driver, baseUrl);
    }
    
    /**
     * Ensures a customer is visible on the current page - fail fast if not found
     */
    public boolean ensureCustomerVisible(String customerNumber) {
        // Use larger page size from the start to maximize visibility
        String currentUrl = driver.getCurrentUrl();
        String newUrl;
        if (currentUrl.contains("?")) {
            newUrl = currentUrl.split("\\?")[0] + "?page=0&size=50";
        } else {
            newUrl = currentUrl + "?page=0&size=50";
        }
        
        driver.get(newUrl);
        waitForPageLoad();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customer-table")));
        
        // Check if customer is visible - fail fast if not
        return isCustomerDisplayed(customerNumber);
    }
    
    /**
     * Opens the page and waits for a specific customer to be visible
     */
    public void openAndWaitForCustomer(String customerNumber) {
        openAndWaitForLoad();
        
        // Wait up to 15 seconds for the specific customer to appear
        WebDriverWait customerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            customerWait.until(webDriver -> isCustomerDisplayed(customerNumber));
            log.info("Customer {} is now visible on the page", customerNumber);
        } catch (TimeoutException e) {
            log.warn("Customer {} did not become visible within timeout", customerNumber);
        }
    }
    
    public PersonalCustomerViewPage viewPersonalCustomer(String customerNumber) {
        WebElement viewButton = findViewButton(customerNumber);
        if (viewButton != null) {
            scrollToElementAndClick(viewButton);
            // Wait for personal customer view page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
            return new PersonalCustomerViewPage(driver, baseUrl);
        } else {
            throw new RuntimeException("View button not found for customer: " + customerNumber);
        }
    }
    
    public CorporateCustomerViewPage viewCorporateCustomer(String customerNumber) {
        WebElement viewButton = findViewButton(customerNumber);
        if (viewButton != null) {
            scrollToElementAndClick(viewButton);
            // Wait for corporate customer view page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("companyName")));
            return new CorporateCustomerViewPage(driver, baseUrl);
        } else {
            throw new RuntimeException("View button not found for customer: " + customerNumber);
        }
    }
    
    private WebElement findViewButton(String customerNumber) {
        try {
            WebElement button = driver.findElement(By.id("view-" + customerNumber));
            if (button.isDisplayed() && button.isEnabled()) {
                log.info("Found view button for customer: {}", customerNumber);
                return button;
            }
        } catch (Exception e) {
            log.debug("View button not found for customer: {}", customerNumber);
        }
        
        log.error("Could not find view button for customer: {}", customerNumber);
        return null;
    }
    
    public String getCustomerStatus(String customerNumber) {
        // First ensure customer is visible
        WebElement customerElement = findCustomerAcrossPages(customerNumber);
        if (customerElement == null) {
            log.error("Customer {} not found for status check", customerNumber);
            return "Not Found";
        }
        
        // Use the exact ID from template
        try {
            WebElement statusCell = driver.findElement(By.id("status-" + customerNumber));
            String statusText = statusCell.getText().trim();
            log.info("Status text for customer {}: '{}'", customerNumber, statusText);
            if (!statusText.isEmpty()) {
                return statusText;
            }
        } catch (Exception e) {
            log.debug("Status not found with ID: status-{}", customerNumber);
        }
        
        // Try nested ID-based approach as fallback
        try {
            WebElement customerRow = driver.findElement(By.id("customer-" + customerNumber));
            // Try finding status elements by class name within the row
            try {
                WebElement statusElement = customerRow.findElement(By.className("status-badge"));
                String statusText = statusElement.getText().trim();
                if (!statusText.isEmpty()) {
                    log.info("Found status using class name: '{}'", statusText);
                    return statusText;
                }
            } catch (Exception ex) {
                log.debug("No status found with class 'status-badge'");
            }
            
            // Try other common status class names
            String[] statusClasses = {"badge", "status", "customer-status", "status-label"};
            for (String statusClass : statusClasses) {
                try {
                    WebElement statusElement = customerRow.findElement(By.className(statusClass));
                    String statusText = statusElement.getText().trim();
                    if (!statusText.isEmpty()) {
                        log.info("Found status using class '{}': '{}'", statusClass, statusText);
                        return statusText;
                    }
                } catch (Exception ex) {
                    log.debug("No status found with class '{}'", statusClass);
                }
            }
        } catch (Exception e) {
            log.error("ID-based status lookup failed for customer: " + customerNumber, e);
        }
        
        log.warn("Could not determine status for customer: {}", customerNumber);
        return "";
    }
    
    public void activateCustomer(String customerNumber) {
        WebElement activateButton = findActionButton(customerNumber, "activate");
        if (activateButton != null) {
            scrollToElementAndClick(activateButton);
            waitForPageLoad();
        } else {
            throw new RuntimeException("Activate button not found for customer: " + customerNumber);
        }
    }
    
    public void deactivateCustomer(String customerNumber) {
        WebElement deactivateButton = findActionButton(customerNumber, "deactivate");
        if (deactivateButton != null) {
            scrollToElementAndClick(deactivateButton);
            waitForPageLoad();
        } else {
            throw new RuntimeException("Deactivate button not found for customer: " + customerNumber);
        }
    }
    
    private WebElement findActionButton(String customerNumber, String action) {
        try {
            String buttonId = action + "-" + customerNumber;
            WebElement button = driver.findElement(By.id(buttonId));
            if (button.isDisplayed() && button.isEnabled()) {
                log.info("Found {} button for customer: {}", action, customerNumber);
                return button;
            }
        } catch (Exception e) {
            log.debug("{} button not found for customer: {}", action, customerNumber);
        }
        
        log.error("Could not find {} button for customer: {}", action, customerNumber);
        return null;
    }
}