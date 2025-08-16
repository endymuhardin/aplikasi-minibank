package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;

public abstract class BasePage {
    
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final String baseUrl;
    
    public BasePage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Increased timeout for slow application startup
        PageFactory.initElements(driver, this);
    }
    
    protected void waitForElementToBeClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }
    
    protected void waitForElementToBeVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }
    
    protected void waitForElementToBePresent(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
    
    protected void waitForTextToBePresent(WebElement element, String text) {
        wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }
    
    protected void waitForPageToLoad() {
        // Wait for document ready state
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));
        // Use WebDriver wait instead of Thread.sleep for better reliability
        wait.until(webDriver -> {
            try {
                return ((JavascriptExecutor) webDriver)
                    .executeScript("return jQuery.active == 0") instanceof Boolean;
            } catch (Exception e) {
                // If jQuery is not available, just return true
                return true;
            }
        });
    }
    
    protected void waitForUrlToContain(String urlFragment) {
        wait.until(ExpectedConditions.urlContains(urlFragment));
    }
    
    protected void selectDropdownByText(WebElement dropdown, String text) {
        // Scroll dropdown into view first
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", dropdown
        );
        
        // Wait for dropdown to be interactable
        wait.until(ExpectedConditions.elementToBeClickable(dropdown));
        
        Select select = new Select(dropdown);
        select.selectByVisibleText(text);
    }
    
    protected void selectDropdownByValue(WebElement dropdown, String value) {
        // Scroll dropdown into view first
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", dropdown
        );
        
        // Wait for dropdown to be interactable
        wait.until(ExpectedConditions.elementToBeClickable(dropdown));
        
        Select select = new Select(dropdown);
        select.selectByValue(value);
    }
    
    protected void selectFirstNonEmptyOption(WebElement dropdown) {
        // Scroll dropdown into view first
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", dropdown
        );
        
        // Wait for dropdown to be interactable
        wait.until(ExpectedConditions.elementToBeClickable(dropdown));
        
        Select select = new Select(dropdown);
        // Get all options and select the first one that has a non-empty value
        select.getOptions().stream()
            .filter(option -> !option.getAttribute("value").isEmpty())
            .findFirst()
            .ifPresent(option -> select.selectByValue(option.getAttribute("value")));
    }
    
    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    protected boolean isElementVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    protected String getPageTitle() {
        return driver.getTitle();
    }
    
    protected void clearAndType(WebElement element, String text) {
        // Scroll element into view first
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element
        );
        
        // Wait for element to be interactable
        wait.until(ExpectedConditions.elementToBeClickable(element));
        
        element.clear();
        element.sendKeys(text);
    }
    
    protected void scrollToElementAndClick(WebElement element) {
        // Scroll element into view first
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element
        );
        
        // Wait for element to be both visible and clickable after scrolling
        wait.until(ExpectedConditions.and(
            ExpectedConditions.visibilityOf(element),
            ExpectedConditions.elementToBeClickable(element)
        ));
        
        // Click the element - fail fast if it doesn't work
        element.click();
    }
    
    protected void scrollToElementAndClick(By locator) {
        WebElement element = driver.findElement(locator);
        scrollToElementAndClick(element);
    }
    
    public boolean isSuccessMessageDisplayed() {
        try {
            // Wait up to 5 seconds for success message to appear
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("success-message")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        return isElementPresent(By.id("error-message"));
    }
    
    public String getSuccessMessage() {
        WebElement element = driver.findElement(By.id("success-message"));
        return element.getText();
    }
    
    public String getErrorMessage() {
        WebElement element = driver.findElement(By.id("error-message"));
        return element.getText();
    }
}