package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

@Slf4j
public class CustomerTypeSelectionPage extends BasePage {
    
    private final WebDriverWait wait;
    
    // Page elements
    private static final By PERSONAL_CUSTOMER_CARD = By.id("personal-customer-card");
    private static final By CORPORATE_CUSTOMER_CARD = By.id("corporate-customer-card");
    private static final By BACK_TO_LIST = By.id("back-to-list");
    
    public CustomerTypeSelectionPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public void open() {
        driver.get(baseUrl + "/customer/create");
        waitForPageLoad();
    }
    
    private void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("page-title")));
    }
    
    public PersonalCustomerFormPage clickPersonalCustomer() {
        driver.findElement(PERSONAL_CUSTOMER_CARD).click();
        return new PersonalCustomerFormPage(driver, baseUrl);
    }
    
    public CorporateCustomerFormPage clickCorporateCustomer() {
        driver.findElement(CORPORATE_CUSTOMER_CARD).click();
        return new CorporateCustomerFormPage(driver, baseUrl);
    }
    
    public CustomerListPage clickBackToList() {
        driver.findElement(BACK_TO_LIST).click();
        return new CustomerListPage(driver, baseUrl);
    }
    
    public boolean isPersonalCardDisplayed() {
        try {
            return driver.findElement(PERSONAL_CUSTOMER_CARD).isDisplayed();
        } catch (Exception e) {
            log.error("Error checking element display status", e);
            return false;
        }
    }
    
    public boolean isCorporateCardDisplayed() {
        try {
            return driver.findElement(CORPORATE_CUSTOMER_CARD).isDisplayed();
        } catch (Exception e) {
            log.error("Error checking element display status", e);
            return false;
        }
    }
}