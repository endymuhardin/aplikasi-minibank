package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class CorporateCustomerFormPage extends BasePage {
    
    private final WebDriverWait wait;
    
    // Corporate customer form elements
    private static final By CUSTOMER_NUMBER_INPUT = By.id("customerNumber");
    private static final By COMPANY_NAME_INPUT = By.id("companyName");
    private static final By COMPANY_TYPE_INPUT = By.id("companyType");
    private static final By BUSINESS_TYPE_INPUT = By.id("businessType");
    private static final By TAX_ID_INPUT = By.id("taxId");
    private static final By CONTACT_PERSON_NAME_INPUT = By.id("contactPersonName");
    private static final By CONTACT_PERSON_TITLE_INPUT = By.id("contactPersonTitle");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phoneNumber");
    private static final By ADDRESS_INPUT = By.id("address");
    private static final By CITY_INPUT = By.id("city");
    private static final By SUBMIT_BUTTON = By.xpath("//button[@type='submit']");
    private static final By BACK_BUTTON = By.xpath("//a[contains(@href, 'list') or contains(text(), 'Back')]");
    private static final By SUCCESS_MESSAGE = By.cssSelector(".alert-success, .text-green-600");
    private static final By ERROR_MESSAGE = By.cssSelector(".alert-danger, .text-red-600");
    
    public CorporateCustomerFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public void fillForm(String customerNumber, String companyName, String companyType, 
                        String businessType, String taxId, String contactPersonName, 
                        String contactPersonTitle, String email, String phone, 
                        String address, String city) {
        
        fillField(CUSTOMER_NUMBER_INPUT, customerNumber);
        fillField(COMPANY_NAME_INPUT, companyName);
        fillField(COMPANY_TYPE_INPUT, companyType);
        fillField(BUSINESS_TYPE_INPUT, businessType);
        fillField(TAX_ID_INPUT, taxId);
        fillField(CONTACT_PERSON_NAME_INPUT, contactPersonName);
        fillField(CONTACT_PERSON_TITLE_INPUT, contactPersonTitle);
        fillField(EMAIL_INPUT, email);
        fillField(PHONE_INPUT, phone);
        fillField(ADDRESS_INPUT, address);
        fillField(CITY_INPUT, city);
    }
    
    private void fillField(By locator, String value) {
        if (value != null) {
            try {
                WebElement field = driver.findElement(locator);
                field.clear();
                field.sendKeys(value);
            } catch (Exception e) {
                // If field not found, continue
            }
        }
    }
    
    public CustomerListPage submitForm() {
        scrollToElementAndClick(SUBMIT_BUTTON);
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/customer/list"),
            ExpectedConditions.presenceOfElementLocated(SUCCESS_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE)
        ));
        return new CustomerListPage(driver, baseUrl);
    }
    
    public CorporateCustomerFormPage submitFormExpectingError() {
        scrollToElementAndClick(SUBMIT_BUTTON);
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".border-red-300, .text-red-600"))
        ));
        return this;
    }
    
    public CustomerListPage clickBackToList() {
        driver.findElement(BACK_BUTTON).click();
        return new CustomerListPage(driver, baseUrl);
    }
    
    public boolean isFormDisplayed() {
        try {
            return driver.findElement(SUBMIT_BUTTON).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean hasValidationError(String fieldName) {
        try {
            return driver.findElement(By.xpath("//input[@id='" + fieldName + "' or @name='" + fieldName + "']//following-sibling::*[contains(@class, 'error') or contains(@class, 'text-red')]")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            return driver.findElement(ERROR_MESSAGE).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getCustomerNumber() {
        try {
            return driver.findElement(CUSTOMER_NUMBER_INPUT).getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }
    
    public void updateEmail(String email) {
        fillField(EMAIL_INPUT, email);
    }
    
    public void updatePhone(String phone) {
        fillField(PHONE_INPUT, phone);
    }
    
    public void updateAddress(String address) {
        fillField(ADDRESS_INPUT, address);
    }
}