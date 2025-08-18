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
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
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
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customer-table")));
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
                return driver.findElement(By.id("customer-table")).isDisplayed();
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
    
    @Override
    public boolean isSuccessMessageDisplayed() {
        try {
            // Log current URL and page source for debugging
            String currentUrl = driver.getCurrentUrl();
            log.info("Checking for success message on URL: {}", currentUrl);
            
            // Wait for page to stabilize first
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customer-table")));
            
            // Check if we're on the right page (customer list)
            if (!currentUrl.contains("/customer/list")) {
                log.warn("Not on customer list page, current URL: {}", currentUrl);
                // Try to navigate to customer list page
                driver.get(baseUrl + "/customer/list");
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customer-table")));
            }
            
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement message = longWait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
            log.info("Success message element found: {}", message.getText());
            return message.isDisplayed();
        } catch (TimeoutException e) {
            log.warn("Success message not visible within 20 seconds. Current URL: {}", driver.getCurrentUrl());
            // Log page source for debugging (first 500 chars)
            String pageSource = driver.getPageSource();
            if (pageSource.length() > 500) {
                pageSource = pageSource.substring(0, 500) + "...";
            }
            log.warn("Page source sample: {}", pageSource);
            
            // Check if success message exists but is not visible
            try {
                WebElement successElement = driver.findElement(SUCCESS_MESSAGE);
                log.warn("Success message element exists but not visible. Text: {}", successElement.getText());
                return false;
            } catch (Exception ex) {
                log.warn("Success message element not found at all");
                return false;
            }
        } catch (Exception e) {
            log.error("An error occurred while checking for the success message.", e);
            return false;
        }
    }
    
    @Override
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
            WebElement countElement = driver.findElement(By.id("customer-count"));
            String countAttr = countElement.getAttribute("data-count");
            return countAttr != null && Integer.parseInt(countAttr) > 0;
        } catch (Exception e) {
            log.error("Error checking element display status", e);
            return false;
        }
    }
    
    public boolean hasFilterResults() {
        return hasSearchResults();
    }
    
    public PersonalCustomerFormPage editPersonalCustomer(String customerNumber) {
        WebElement customerElement = findCustomerAcrossPages(customerNumber);
        if (customerElement == null) {
            throw new RuntimeException("Customer " + customerNumber + " not found on any page");
        }

        try {
            WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("edit-" + customerNumber)));
            scrollToElementAndClick(editButton);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
            return new PersonalCustomerFormPage(driver, baseUrl);
        } catch (TimeoutException e) {
            throw new RuntimeException("Edit button for customer " + customerNumber + " not found or not clickable", e);
        }
    }

    public CorporateCustomerFormPage editCorporateCustomer(String customerNumber) {
        WebElement customerElement = findCustomerAcrossPages(customerNumber);
        if (customerElement == null) {
            throw new RuntimeException("Customer " + customerNumber + " not found on any page");
        }

        try {
            WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("edit-" + customerNumber)));
            scrollToElementAndClick(editButton);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("companyName")));
            return new CorporateCustomerFormPage(driver, baseUrl);
        } catch (TimeoutException e) {
            throw new RuntimeException("Edit button for customer " + customerNumber + " not found or not clickable", e);
        }
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
        WebElement customerElement = findCustomerAcrossPages(customerNumber);
        if (customerElement == null) {
            throw new RuntimeException("Customer " + customerNumber + " not found on any page");
        }
        try {
            WebElement viewButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("view-" + customerNumber)));
            scrollToElementAndClick(viewButton);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
            return new PersonalCustomerViewPage(driver, baseUrl);
        } catch (TimeoutException e) {
            throw new RuntimeException("View button for customer " + customerNumber + " not found or not clickable", e);
        }
    }

    public CorporateCustomerViewPage viewCorporateCustomer(String customerNumber) {
        WebElement customerElement = findCustomerAcrossPages(customerNumber);
        if (customerElement == null) {
            throw new RuntimeException("Customer " + customerNumber + " not found on any page");
        }
        try {
            WebElement viewButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("view-" + customerNumber)));
            scrollToElementAndClick(viewButton);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("companyName")));
            return new CorporateCustomerViewPage(driver, baseUrl);
        } catch (TimeoutException e) {
            throw new RuntimeException("View button for customer " + customerNumber + " not found or not clickable", e);
        }
    }
    
    public String getCustomerStatus(String customerNumber) {
        WebElement customerElement = findCustomerAcrossPages(customerNumber);
        if (customerElement == null) {
            log.error("Customer {} not found for status check", customerNumber);
            return "Not Found";
        }

        try {
            WebElement statusCell = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status-" + customerNumber)));
            String statusText = statusCell.getText();
            if (statusText != null && !statusText.trim().isEmpty()) {
                return statusText.trim();
            }
            
            // Fallback to using JavaScript to get text content
            Object jsResult = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return arguments[0].textContent;", statusCell);
            if (jsResult != null) {
                return jsResult.toString().trim();
            }
        } catch (Exception e) {
            log.warn("Could not find status for customer {} using primary method", customerNumber, e);
        }
        
        return ""; // Return empty if not found after all attempts
    }
    
    public void activateCustomer(String customerNumber) {
        findCustomerAcrossPages(customerNumber);
        try {
            WebElement activateButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("activate-" + customerNumber)));
            scrollToElementAndClick(activateButton);
            waitForPageLoad();
        } catch (TimeoutException e) {
            throw new RuntimeException("Activate button for customer " + customerNumber + " not found or not clickable", e);
        }
    }

    public void deactivateCustomer(String customerNumber) {
        findCustomerAcrossPages(customerNumber);
        try {
            WebElement deactivateButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("deactivate-" + customerNumber)));
            scrollToElementAndClick(deactivateButton);
            wait.until(ExpectedConditions.alertIsPresent()).accept();
            waitForPageLoad();
        } catch (TimeoutException e) {
            throw new RuntimeException("Deactivate button for customer " + customerNumber + " not found or not clickable", e);
        }
    }
}