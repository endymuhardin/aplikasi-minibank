package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

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
        try {
            return driver.findElement(By.id("customer-" + customerNumber)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isSuccessMessageDisplayed() {
        try {
            WebElement message = driver.findElement(SUCCESS_MESSAGE);
            return message.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            WebElement message = driver.findElement(ERROR_MESSAGE);
            return message.isDisplayed();
        } catch (Exception e) {
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
            // If filter not available, ignore
        }
    }
    
    public boolean hasSearchResults() {
        try {
            WebElement searchResults = driver.findElement(By.id("search-results"));
            return searchResults.findElements(By.tagName("tr")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean hasFilterResults() {
        return hasSearchResults();
    }
    
    public PersonalCustomerFormPage editPersonalCustomer(String customerNumber) {
        scrollToElementAndClick(By.id("edit-" + customerNumber));
        
        // Wait for personal customer edit form to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        return new PersonalCustomerFormPage(driver, baseUrl);
    }
    
    public CorporateCustomerFormPage editCorporateCustomer(String customerNumber) {
        scrollToElementAndClick(By.id("edit-" + customerNumber));
        
        // Wait for corporate customer edit form to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("companyName")));
        return new CorporateCustomerFormPage(driver, baseUrl);
    }
    
    public PersonalCustomerViewPage viewPersonalCustomer(String customerNumber) {
        scrollToElementAndClick(By.id("view-" + customerNumber));
        
        // Wait for personal customer view page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        return new PersonalCustomerViewPage(driver, baseUrl);
    }
    
    public CorporateCustomerViewPage viewCorporateCustomer(String customerNumber) {
        scrollToElementAndClick(By.id("view-" + customerNumber));
        
        // Wait for corporate customer view page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("companyName")));
        return new CorporateCustomerViewPage(driver, baseUrl);
    }
    
    public String getCustomerStatus(String customerNumber) {
        try {
            WebElement statusCell = driver.findElement(By.id("status-" + customerNumber));
            return statusCell.getText().trim();
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    public void activateCustomer(String customerNumber) {
        scrollToElementAndClick(By.id("activate-" + customerNumber));
        waitForPageLoad();
    }
    
    public void deactivateCustomer(String customerNumber) {
        scrollToElementAndClick(By.id("deactivate-" + customerNumber));
        waitForPageLoad();
    }
}