package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class PersonalCustomerFormPage extends BasePage {
    
    private final WebDriverWait wait;
    
    // Personal customer form elements
    private static final By CUSTOMER_NUMBER_INPUT = By.id("customerNumber");
    private static final By BRANCH_SELECT = By.id("branch");
    private static final By FIRST_NAME_INPUT = By.id("firstName");
    private static final By LAST_NAME_INPUT = By.id("lastName");
    private static final By DATE_OF_BIRTH_INPUT = By.id("dateOfBirth");
    private static final By IDENTITY_TYPE_SELECT = By.id("identityType");
    private static final By ID_NUMBER_INPUT = By.id("identityNumber");
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
                        String dateOfBirth, String identityType, String idNumber,
                        String email, String phone, String address, String city) {
        
        // Wait for form to be fully loaded
        wait.until(ExpectedConditions.presenceOfElementLocated(CUSTOMER_NUMBER_INPUT));
        
        fillField(CUSTOMER_NUMBER_INPUT, customerNumber);
        
        // Select first available branch (required field)
        selectFirstAvailableBranch();
        
        fillField(FIRST_NAME_INPUT, firstName);
        fillField(LAST_NAME_INPUT, lastName);
        fillDateField(DATE_OF_BIRTH_INPUT, dateOfBirth);
        fillField(ID_NUMBER_INPUT, idNumber);
        fillField(EMAIL_INPUT, email);
        fillField(PHONE_INPUT, phone);
        fillField(ADDRESS_INPUT, address);
        fillField(CITY_INPUT, city);
        
        if (identityType != null && !identityType.isEmpty()) {
            try {
                WebElement identityTypeField = driver.findElement(IDENTITY_TYPE_SELECT);
                Select identityTypeSelect = new Select(identityTypeField);
                identityTypeSelect.selectByValue(identityType);
            } catch (Exception e) {
                // If no identity type selector, continue but log the issue
                System.err.println("Warning: Could not set identity type field: " + e.getMessage());
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
    
    private void selectFirstAvailableBranch() {
        try {
            WebElement branchField = wait.until(ExpectedConditions.presenceOfElementLocated(BRANCH_SELECT));
            Select branchSelect = new Select(branchField);
            // Select the first non-empty option (skip the "Select Branch" placeholder)
            if (branchSelect.getOptions().size() > 1) {
                branchSelect.selectByIndex(1); // First actual branch option
                log.info("Selected first available branch");
            } else {
                log.warn("No branches available in dropdown");
            }
        } catch (Exception e) {
            log.error("Could not select branch: " + e.getMessage());
        }
    }
    
    public CustomerListPage submitForm() {
        // Disable HTML5 validation to ensure form submission reaches the server
        disableHTML5Validation();
        
        String initialUrl = driver.getCurrentUrl();
        log.info("Submitting form from URL: {}", initialUrl);
        
        scrollToElementAndClick(SUBMIT_BUTTON);
        
        // Wait for form submission to complete (URL change or message display)
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/customer/list"),
            ExpectedConditions.presenceOfElementLocated(SUCCESS_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
            ExpectedConditions.presenceOfElementLocated(VALIDATION_ERRORS)
        ));
        
        String currentUrl = driver.getCurrentUrl();
        log.info("After form submission, current URL: {}", currentUrl);
        
        // The form should redirect to /customer/list - if it doesn't, something is wrong
        if (!currentUrl.contains("/customer/list")) {
            log.error("Form did not redirect to customer list. Current URL: {}", currentUrl);
            if (isErrorMessageDisplayed()) {
                // Get the actual error message text for debugging
                try {
                    WebElement errorElement = driver.findElement(ERROR_MESSAGE);
                    log.error("Error message displayed: {}", errorElement.getText());
                } catch (Exception e) {
                    // Try validation errors element
                    try {
                        WebElement validationElement = driver.findElement(VALIDATION_ERRORS);
                        log.error("Validation errors displayed: {}", validationElement.getText());
                    } catch (Exception ex) {
                        log.error("Error message element found but could not read text");
                    }
                }
            } else {
                log.error("No error message displayed, but redirect failed");
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
            System.out.println("HTML5 validation disabled successfully");
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
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("/customer/create/personal") || 
               currentUrl.contains("/customer/edit/") ||
               currentUrl.contains("/customer/update/personal/");
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