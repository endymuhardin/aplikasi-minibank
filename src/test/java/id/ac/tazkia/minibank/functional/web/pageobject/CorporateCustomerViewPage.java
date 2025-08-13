package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

@Slf4j
public class CorporateCustomerViewPage extends BasePage {
    
    private final WebDriverWait wait;
    
    // Corporate customer view elements
    private static final By CUSTOMER_NUMBER_DISPLAY = By.id("customerNumber");
    private static final By COMPANY_NAME_DISPLAY = By.id("companyName");
    private static final By COMPANY_REGISTRATION_NUMBER_DISPLAY = By.id("companyRegistrationNumber");
    private static final By TAX_ID_DISPLAY = By.id("taxIdentificationNumber");
    private static final By CONTACT_PERSON_NAME_DISPLAY = By.id("contactPersonName");
    private static final By CONTACT_PERSON_TITLE_DISPLAY = By.id("contactPersonTitle");
    private static final By EMAIL_DISPLAY = By.id("email");
    private static final By PHONE_DISPLAY = By.id("phoneNumber");
    private static final By ADDRESS_DISPLAY = By.id("address");
    private static final By CITY_DISPLAY = By.id("city");
    private static final By STATUS_DISPLAY = By.id("status");
    private static final By CUSTOMER_TYPE_DISPLAY = By.id("customerType");
    private static final By CREATED_DATE_DISPLAY = By.id("createdDate");
    private static final By UPDATED_DATE_DISPLAY = By.id("updatedDate");
    
    private static final By EDIT_BUTTON = By.id("edit-button");
    private static final By BACK_BUTTON = By.id("back-button");
    private static final By ACTIVATE_BUTTON = By.id("activate-button");
    private static final By DEACTIVATE_BUTTON = By.id("deactivate-button");
    
    public CorporateCustomerViewPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public boolean isViewPageDisplayed() {
        try {
            return driver.findElement(CUSTOMER_NUMBER_DISPLAY).isDisplayed();
        } catch (Exception e) {
            log.error("Error checking if view page is displayed", e);
            return false;
        }
    }
    
    public String getCustomerNumber() {
        try {
            return driver.findElement(CUSTOMER_NUMBER_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getCompanyName() {
        try {
            return driver.findElement(COMPANY_NAME_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getCompanyRegistrationNumber() {
        try {
            return driver.findElement(COMPANY_REGISTRATION_NUMBER_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getTaxIdentificationNumber() {
        try {
            return driver.findElement(TAX_ID_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getContactPersonName() {
        try {
            return driver.findElement(CONTACT_PERSON_NAME_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getContactPersonTitle() {
        try {
            return driver.findElement(CONTACT_PERSON_TITLE_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getEmail() {
        try {
            return driver.findElement(EMAIL_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getPhoneNumber() {
        try {
            return driver.findElement(PHONE_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getAddress() {
        try {
            return driver.findElement(ADDRESS_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getCity() {
        try {
            return driver.findElement(CITY_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getStatus() {
        try {
            return driver.findElement(STATUS_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getCustomerType() {
        try {
            return driver.findElement(CUSTOMER_TYPE_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public CorporateCustomerFormPage clickEdit() {
        driver.findElement(EDIT_BUTTON).click();
        return new CorporateCustomerFormPage(driver, baseUrl);
    }
    
    public CustomerListPage clickBackToList() {
        driver.findElement(BACK_BUTTON).click();
        return new CustomerListPage(driver, baseUrl);
    }
    
    public CorporateCustomerViewPage clickActivate() {
        try {
            driver.findElement(ACTIVATE_BUTTON).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            return this;
        } catch (Exception e) {
            log.error("Error performing page action", e);
            return this;
        }
    }
    
    public CorporateCustomerViewPage clickDeactivate() {
        try {
            driver.findElement(DEACTIVATE_BUTTON).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            return this;
        } catch (Exception e) {
            log.error("Error performing page action", e);
            return this;
        }
    }
    
    public boolean isEditButtonDisplayed() {
        try {
            return driver.findElement(EDIT_BUTTON).isDisplayed();
        } catch (Exception e) {
            log.error("Error checking if edit button is displayed", e);
            return false;
        }
    }
    
    public boolean isActivateButtonDisplayed() {
        try {
            return driver.findElement(ACTIVATE_BUTTON).isDisplayed();
        } catch (Exception e) {
            log.error("Error checking if activate button is displayed", e);
            return false;
        }
    }
    
    public boolean isDeactivateButtonDisplayed() {
        try {
            return driver.findElement(DEACTIVATE_BUTTON).isDisplayed();
        } catch (Exception e) {
            log.error("Error checking if deactivate button is displayed", e);
            return false;
        }
    }
}