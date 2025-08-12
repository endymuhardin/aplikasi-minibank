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
    private static final By SUCCESS_MESSAGE = By.cssSelector(".alert-success, .text-green-600");
    private static final By ERROR_MESSAGE = By.cssSelector(".alert-danger, .text-red-600");
    
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
            // Fallback: look for any create button
            try {
                return driver.findElement(By.xpath("//a[contains(@href, 'create') or contains(text(), 'Create') or contains(text(), 'Add')]")).isDisplayed();
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
    
    public CustomerFormPage clickCreateCustomer() {
        try {
            driver.findElement(CREATE_BUTTON).click();
        } catch (Exception e) {
            // Fallback: click any create link
            driver.findElement(By.xpath("//a[contains(@href, 'create') or contains(text(), 'Create') or contains(text(), 'Add')]")).click();
        }
        return new CustomerFormPage(driver, baseUrl);
    }
    
    public boolean isCustomerDisplayed(String customerNumber) {
        try {
            return driver.findElement(By.xpath("//td[contains(text(), '" + customerNumber + "')]")).isDisplayed();
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
            // Fallback: find any search input
            WebElement searchInput = driver.findElement(By.xpath("//input[@type='text' or @type='search']"));
            searchInput.clear();
            searchInput.sendKeys(searchTerm);
            searchInput.submit();
            waitForPageLoad();
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
            return driver.findElements(By.xpath("//table//tbody//tr")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean hasFilterResults() {
        return hasSearchResults();
    }
    
    public CustomerFormPage editCustomer(String customerNumber) {
        WebElement editButton = driver.findElement(By.xpath("//tr[td[contains(text(), '" + customerNumber + "')]]//a[contains(@href, 'edit') or contains(text(), 'Edit')]"));
        editButton.click();
        return new CustomerFormPage(driver, baseUrl);
    }
    
    public void viewCustomer(String customerNumber) {
        WebElement viewButton = driver.findElement(By.xpath("//tr[td[contains(text(), '" + customerNumber + "')]]//a[contains(@href, 'view') or contains(text(), 'View')]"));
        viewButton.click();
    }
    
    public String getCustomerStatus(String customerNumber) {
        try {
            WebElement statusCell = driver.findElement(By.xpath("//tr[td[contains(text(), '" + customerNumber + "')]]//td[contains(@class, 'status') or position()=last()]"));
            return statusCell.getText().trim();
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    public void activateCustomer(String customerNumber) {
        try {
            WebElement activateButton = driver.findElement(By.xpath("//tr[td[contains(text(), '" + customerNumber + "')]]//button[contains(text(), 'Activate') or contains(@title, 'Activate')]"));
            activateButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            // If no activate button, ignore
        }
    }
    
    public void deactivateCustomer(String customerNumber) {
        try {
            WebElement deactivateButton = driver.findElement(By.xpath("//tr[td[contains(text(), '" + customerNumber + "')]]//button[contains(text(), 'Deactivate') or contains(@title, 'Deactivate')]"));
            deactivateButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            // If no deactivate button, ignore
        }
    }
}