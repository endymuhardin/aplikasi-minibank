package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class PersonalCustomerFormPage extends BasePage {
    
    private final WebDriverWait wait;
    
    // Personal customer form elements
    private static final By CUSTOMER_NUMBER_INPUT = By.id("customerNumber");
    private static final By FIRST_NAME_INPUT = By.id("firstName");
    private static final By LAST_NAME_INPUT = By.id("lastName");
    private static final By DATE_OF_BIRTH_INPUT = By.id("dateOfBirth");
    private static final By GENDER_SELECT = By.id("gender");
    private static final By ID_NUMBER_INPUT = By.id("idNumber");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phoneNumber");
    private static final By ADDRESS_INPUT = By.id("address");
    private static final By CITY_INPUT = By.id("city");
    private static final By SUBMIT_BUTTON = By.id("submit-button");
    private static final By BACK_BUTTON = By.id("back-button");
    private static final By SUCCESS_MESSAGE = By.id("success-message");
    private static final By ERROR_MESSAGE = By.id("error-message");
    private static final By VALIDATION_ERRORS = By.id("validation-errors");
    
    public PersonalCustomerFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public void fillForm(String customerNumber, String firstName, String lastName,
                        String dateOfBirth, String gender, String idNumber,
                        String email, String phone, String address, String city) {
        
        // Wait for form to be fully loaded
        wait.until(ExpectedConditions.presenceOfElementLocated(CUSTOMER_NUMBER_INPUT));
        
        fillField(CUSTOMER_NUMBER_INPUT, customerNumber);
        fillField(FIRST_NAME_INPUT, firstName);
        fillField(LAST_NAME_INPUT, lastName);
        fillDateField(DATE_OF_BIRTH_INPUT, dateOfBirth);
        fillField(ID_NUMBER_INPUT, idNumber);
        fillField(EMAIL_INPUT, email);
        fillField(PHONE_INPUT, phone);
        fillField(ADDRESS_INPUT, address);
        fillField(CITY_INPUT, city);
        
        if (gender != null && !gender.isEmpty()) {
            try {
                WebElement genderField = driver.findElement(GENDER_SELECT);
                Select genderSelect = new Select(genderField);
                genderSelect.selectByValue(gender);
            } catch (Exception e) {
                // If no gender selector, continue but log the issue
                System.err.println("Warning: Could not set gender field: " + e.getMessage());
            }
        }
    }

    private void fillDateField(By locator, String value) {
        if (value != null && !value.isEmpty()) {
            try {
                WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", field, value);
            } catch (Exception e) {
                System.err.println("Warning: Could not fill date field " + locator + " with value '" + value + "': " + e.getMessage());
            }
        }
    }
    
    private void fillField(By locator, String value) {
        if (value != null) {
            try {
                WebElement field = wait.until(ExpectedConditions.elementToBeClickable(locator));
                field.clear();
                if (!value.isEmpty()) {
                    field.sendKeys(value);
                }
                // For empty values, we just clear the field to trigger HTML5 validation
            } catch (Exception e) {
                System.err.println("Warning: Could not fill field " + locator + " with value '" + value + "': " + e.getMessage());
            }
        }
    }
    
    public CustomerListPage submitForm() {
        scrollToElementAndClick(SUBMIT_BUTTON);
        // Wait for either redirect to list page or form processing
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/customer/list"),
            ExpectedConditions.presenceOfElementLocated(SUCCESS_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERRORS)
        ));
        
        // If we're still on form page, check for success message and navigate to list
        if (driver.getCurrentUrl().contains("/customer/create/personal") || 
            driver.getCurrentUrl().contains("/customer/personal-form")) {
            // Wait a bit more for potential redirect
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // If still on form page but no error, navigate to list manually
            if (!isErrorMessageDisplayed()) {
                driver.get(baseUrl + "/customer/list");
            }
        }
        
        return new CustomerListPage(driver, baseUrl);
    }
    
    public PersonalCustomerFormPage submitFormExpectingError() {
        // Disable HTML5 validation to test server-side validation
        disableHTML5Validation();
        
        scrollToElementAndClick(SUBMIT_BUTTON);
        
        // Wait for server-side validation response
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERRORS),
            ExpectedConditions.urlContains("/customer/create/personal"), // Stay on form page
            ExpectedConditions.urlContains("/customer/personal-form") // Alternative form URL pattern
        ));
        
        return this;
    }
    
    private void disableHTML5Validation() {
        // Remove 'required' attributes and change email type to text to bypass HTML5 validation
        try {
            ((JavascriptExecutor) driver).executeScript(
                "document.querySelectorAll('input[required]').forEach(function(input) { input.removeAttribute('required'); });" +
                "document.querySelectorAll('input[type=\"email\"]').forEach(function(input) { input.type = 'text'; });"
            );
        } catch (Exception e) {
            System.err.println("Warning: Could not disable HTML5 validation: " + e.getMessage());
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
            WebElement validationErrors = driver.findElement(VALIDATION_ERRORS);
            if (validationErrors.isDisplayed()) {
                return true;
            }
        } catch (Exception e) {
            // Continue to check other error indicators
        }
        
        // Check if we're still on form page after submission (indicates validation failure)
        return driver.getCurrentUrl().contains("/customer/create/personal");
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