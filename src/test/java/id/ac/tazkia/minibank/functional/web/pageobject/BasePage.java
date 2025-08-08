package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
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
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Reduced from 30 to 10 seconds
        PageFactory.initElements(driver, this);
    }
    
    protected void waitForElementToBeClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }
    
    protected void waitForElementToBeVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }
    
    protected void waitForTextToBePresent(WebElement element, String text) {
        wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }
    
    protected void waitForPageToLoad() {
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete';"));
    }
    
    protected void waitForUrlToContain(String urlFragment) {
        wait.until(ExpectedConditions.urlContains(urlFragment));
    }
    
    protected void selectDropdownByText(WebElement dropdown, String text) {
        Select select = new Select(dropdown);
        select.selectByVisibleText(text);
    }
    
    protected void selectDropdownByValue(WebElement dropdown, String value) {
        Select select = new Select(dropdown);
        select.selectByValue(value);
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
        element.clear();
        element.sendKeys(text);
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