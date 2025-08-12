package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class CustomerFormPage extends BasePage {
    
    private final WebDriverWait wait;
    
    // Form elements
    private static final By CUSTOMER_NUMBER_INPUT = By.id("customerNumber");
    private static final By CUSTOMER_TYPE_SELECT = By.id("customerType");
    private static final By FIRST_NAME_INPUT = By.id("firstName");
    private static final By LAST_NAME_INPUT = By.id("lastName");
    private static final By COMPANY_NAME_INPUT = By.id("companyName");
    private static final By CONTACT_PERSON_INPUT = By.id("contactPerson");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By ADDRESS_INPUT = By.id("address");
    private static final By CITY_INPUT = By.id("city");
    private static final By ID_NUMBER_INPUT = By.id("idNumber");
    private static final By TAX_ID_INPUT = By.id("taxId");
    private static final By BIRTH_DATE_INPUT = By.id("birthDate");
    private static final By GENDER_SELECT = By.id("gender");
    private static final By COMPANY_TYPE_INPUT = By.id("companyType");
    private static final By INDUSTRY_INPUT = By.id("industry");
    private static final By SUBMIT_BUTTON = By.xpath("//button[@type='submit' or contains(text(), 'Save') or contains(text(), 'Submit')]");
    private static final By BACK_BUTTON = By.xpath("//a[contains(@href, 'list') or contains(text(), 'Back')]");
    private static final By SUCCESS_MESSAGE = By.cssSelector(".alert-success, .text-green-600");
    private static final By ERROR_MESSAGE = By.cssSelector(".alert-danger, .text-red-600");
    
    public CustomerFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public void fillPersonalCustomer(String customerNumber, String firstName, String lastName,
                                   String email, String phone, String address, String city,
                                   String idNumber, String birthDate, String gender) {
        
        // Set customer type to PERSONAL
        try {
            Select typeSelect = new Select(driver.findElement(CUSTOMER_TYPE_SELECT));
            typeSelect.selectByValue("PERSONAL");
        } catch (Exception e) {
            // If no customer type selector, continue
        }
        
        // Fill common fields
        fillField(CUSTOMER_NUMBER_INPUT, customerNumber);
        fillField(FIRST_NAME_INPUT, firstName);
        fillField(LAST_NAME_INPUT, lastName);
        fillField(EMAIL_INPUT, email);
        fillField(PHONE_INPUT, phone);
        fillField(ADDRESS_INPUT, address);
        fillField(CITY_INPUT, city);
        
        // Fill personal-specific fields
        fillField(ID_NUMBER_INPUT, idNumber);
        fillField(BIRTH_DATE_INPUT, birthDate);
        
        try {
            Select genderSelect = new Select(driver.findElement(GENDER_SELECT));
            genderSelect.selectByValue(gender);
        } catch (Exception e) {
            // If no gender selector, continue
        }
    }
    
    public void fillCorporateCustomer(String customerNumber, String companyName, String contactPerson,
                                    String email, String phone, String address, String city,
                                    String taxId, String companyType, String industry) {
        
        // Set customer type to CORPORATE
        try {
            Select typeSelect = new Select(driver.findElement(CUSTOMER_TYPE_SELECT));
            typeSelect.selectByValue("CORPORATE");
        } catch (Exception e) {
            // If no customer type selector, continue
        }
        
        // Fill common fields
        fillField(CUSTOMER_NUMBER_INPUT, customerNumber);
        fillField(COMPANY_NAME_INPUT, companyName);
        fillField(CONTACT_PERSON_INPUT, contactPerson);
        fillField(EMAIL_INPUT, email);
        fillField(PHONE_INPUT, phone);
        fillField(ADDRESS_INPUT, address);
        fillField(CITY_INPUT, city);
        
        // Fill corporate-specific fields
        fillField(TAX_ID_INPUT, taxId);
        fillField(COMPANY_TYPE_INPUT, companyType);
        fillField(INDUSTRY_INPUT, industry);
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
    
    public CustomerListPage submitForm() {
        driver.findElement(SUBMIT_BUTTON).click();
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/customer/list"),
            ExpectedConditions.presenceOfElementLocated(SUCCESS_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE)
        ));
        return new CustomerListPage(driver, baseUrl);
    }
    
    public CustomerFormPage submitFormExpectingError() {
        driver.findElement(SUBMIT_BUTTON).click();
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
}