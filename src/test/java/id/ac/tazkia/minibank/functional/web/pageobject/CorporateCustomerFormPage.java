package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

@Slf4j
public class CorporateCustomerFormPage extends BasePage {
    
    private final WebDriverWait wait;
    
    // Corporate customer form elements
    private static final By CUSTOMER_NUMBER_INPUT = By.id("customerNumber");
    private static final By COMPANY_NAME_INPUT = By.id("companyName");
    private static final By TAX_ID_INPUT = By.id("taxIdentificationNumber");
    private static final By CONTACT_PERSON_NAME_INPUT = By.id("contactPersonName");
    private static final By CONTACT_PERSON_TITLE_INPUT = By.id("contactPersonTitle");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phoneNumber");
    private static final By ADDRESS_INPUT = By.id("address");
    private static final By CITY_INPUT = By.id("city");
    private static final By SUBMIT_BUTTON = By.id("submit-button");
    private static final By BACK_BUTTON = By.id("back-button");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By ERROR_MESSAGE = By.id("error-message");
    private static final By VALIDATION_ERRORS = By.id("validation-errors");
    
    public CorporateCustomerFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public void fillForm(String customerNumber, String companyName, String taxIdentificationNumber, 
                        String contactPersonName, String contactPersonTitle, 
                        String email, String phone, String address, String city) {
        
        fillField(CUSTOMER_NUMBER_INPUT, customerNumber);
        fillField(COMPANY_NAME_INPUT, companyName);
        fillField(TAX_ID_INPUT, taxIdentificationNumber);
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
                if (!value.isEmpty()) {
                    field.sendKeys(value);
                }
                // For empty values, we just clear the field to trigger HTML5 validation
            } catch (Exception e) {
                log.warn("Field not found, continuing", e);
            }
        }
    }
    
    public CustomerListPage submitForm() {
        // Disable HTML5 validation to ensure form submission reaches the server
        disableHTML5Validation();
        
        scrollToElementAndClick(SUBMIT_BUTTON);
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/customer/list"),
            ExpectedConditions.presenceOfElementLocated(SUCCESS_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERRORS)
        ));
        return new CustomerListPage(driver, baseUrl);
    }
    
    public CorporateCustomerFormPage submitFormExpectingError() {
        // Disable HTML5 validation to test server-side validation
        disableHTML5Validation();
        
        scrollToElementAndClick(SUBMIT_BUTTON);
        
        // Wait for server-side validation response
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERRORS),
            ExpectedConditions.urlContains("/customer/create/corporate"), // Stay on form page
            ExpectedConditions.urlContains("/customer/list") // Or redirect (but this shouldn't happen for invalid data)
        ));
        
        return this;
    }
    
    private void disableHTML5Validation() {
        // Comprehensively disable HTML5 validation to test server-side validation
        try {
            ((JavascriptExecutor) driver).executeScript(
                // Remove required attributes from all form elements
                "document.querySelectorAll('input[required], select[required], textarea[required]').forEach(function(element) { " +
                "  element.removeAttribute('required'); " +
                "});" +
                // Change email inputs to text type
                "document.querySelectorAll('input[type=\"email\"]').forEach(function(input) { " +
                "  input.type = 'text'; " +
                "});" +
                // Change date inputs to text type to prevent date validation
                "document.querySelectorAll('input[type=\"date\"]').forEach(function(input) { " +
                "  input.type = 'text'; " +
                "});" +
                // Disable HTML5 validation on the form itself
                "document.querySelectorAll('form').forEach(function(form) { " +
                "  form.setAttribute('novalidate', 'novalidate'); " +
                "});" +
                // Remove pattern attributes if any
                "document.querySelectorAll('input[pattern]').forEach(function(input) { " +
                "  input.removeAttribute('pattern'); " +
                "});" +
                // Remove min/max attributes that might cause validation
                "document.querySelectorAll('input[min], input[max]').forEach(function(input) { " +
                "  input.removeAttribute('min'); " +
                "  input.removeAttribute('max'); " +
                "});"
            );
            log.info("HTML5 validation disabled successfully");
        } catch (Exception e) {
            log.warn("Could not disable HTML5 validation", e);
        }
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
            return driver.findElement(By.id(fieldName + "-error")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            // Check for explicit error message div
            WebElement errorMsg = driver.findElement(ERROR_MESSAGE);
            if (errorMsg.isDisplayed()) {
                return true;
            }
        } catch (Exception e) {
            // Continue to check other error indicators
        }
        
        try {
            // Check for validation errors div
            WebElement validationErrors = driver.findElement(By.id("validation-errors"));
            if (validationErrors.isDisplayed()) {
                return true;
            }
        } catch (Exception e) {
            // Continue to check other error indicators
        }
        
        // Check if we're still on form page after submission (indicates validation failure)
        return driver.getCurrentUrl().contains("/customer/create/corporate");
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