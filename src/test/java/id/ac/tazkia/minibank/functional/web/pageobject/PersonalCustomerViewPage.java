package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

@Slf4j
public class PersonalCustomerViewPage extends BasePage {
    
    private final WebDriverWait wait;
    
    // Personal customer view elements
    private static final By CUSTOMER_NUMBER_DISPLAY = By.id("customerNumber");
    private static final By FIRST_NAME_DISPLAY = By.id("firstName");
    private static final By LAST_NAME_DISPLAY = By.id("lastName");
    private static final By DATE_OF_BIRTH_DISPLAY = By.id("dateOfBirth");
    private static final By GENDER_DISPLAY = By.id("gender");
    private static final By ID_NUMBER_DISPLAY = By.id("idNumber");
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
    
    public PersonalCustomerViewPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public boolean isViewPageDisplayed() {
        try {
            return driver.findElement(CUSTOMER_NUMBER_DISPLAY).isDisplayed();
        } catch (Exception e) {
            log.error("Error checking element display status", e);
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
    
    public String getFirstName() {
        try {
            return driver.findElement(FIRST_NAME_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getLastName() {
        try {
            return driver.findElement(LAST_NAME_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getFullName() {
        return getFirstName() + " " + getLastName();
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
    
    public String getDateOfBirth() {
        try {
            return driver.findElement(DATE_OF_BIRTH_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getGender() {
        try {
            return driver.findElement(GENDER_DISPLAY).getText();
        } catch (Exception e) {
            log.error("Error getting text from element", e);
            return "";
        }
    }
    
    public String getIdNumber() {
        try {
            return driver.findElement(ID_NUMBER_DISPLAY).getText();
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
    
    public PersonalCustomerFormPage clickEdit() {
        driver.findElement(EDIT_BUTTON).click();
        return new PersonalCustomerFormPage(driver, baseUrl);
    }
    
    public CustomerListPage clickBackToList() {
        driver.findElement(BACK_BUTTON).click();
        return new CustomerListPage(driver, baseUrl);
    }
    
    public PersonalCustomerViewPage clickActivate() {
        try {
            driver.findElement(ACTIVATE_BUTTON).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            return this;
        } catch (Exception e) {
            log.error("Error performing page action", e);
            return this;
        }
    }
    
    public PersonalCustomerViewPage clickDeactivate() {
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
            log.error("Error checking element display status", e);
            return false;
        }
    }
    
    public boolean isActivateButtonDisplayed() {
        try {
            return driver.findElement(ACTIVATE_BUTTON).isDisplayed();
        } catch (Exception e) {
            log.error("Error checking element display status", e);
            return false;
        }
    }
    
    public boolean isDeactivateButtonDisplayed() {
        try {
            return driver.findElement(DEACTIVATE_BUTTON).isDisplayed();
        } catch (Exception e) {
            log.error("Error checking element display status", e);
            return false;
        }
    }
}