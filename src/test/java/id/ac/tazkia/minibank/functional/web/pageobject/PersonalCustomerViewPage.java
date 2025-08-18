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
    
    private static final By EDIT_BUTTON = By.id("edit-button");
    private static final By BACK_BUTTON = By.id("back-button");
    private static final By ACTIVATE_BUTTON = By.id("activate-button");
    private static final By DEACTIVATE_BUTTON = By.id("deactivate-button");
    
    public PersonalCustomerViewPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5)); // Reduced timeout for faster feedback
    }
    
    public boolean isViewPageDisplayed() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(CUSTOMER_NUMBER_DISPLAY)).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot verify view page display. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getCustomerNumber() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(CUSTOMER_NUMBER_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get customer number. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getFirstName() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(FIRST_NAME_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get first name. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getLastName() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(LAST_NAME_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get last name. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }
    
    public String getEmail() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(EMAIL_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get email. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getPhoneNumber() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(PHONE_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get phone number. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getAddress() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(ADDRESS_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get address. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getCity() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(CITY_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get city. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getDateOfBirth() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(DATE_OF_BIRTH_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get date of birth. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getGender() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(GENDER_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get gender. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getIdNumber() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(ID_NUMBER_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get ID number. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getStatus() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(STATUS_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public String getCustomerType() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(CUSTOMER_TYPE_DISPLAY)).getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get customer type. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
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
            wait.until(ExpectedConditions.elementToBeClickable(ACTIVATE_BUTTON)).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("page-title")));
            return this;
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot click activate button. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public PersonalCustomerViewPage clickDeactivate() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(DEACTIVATE_BUTTON)).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("page-title")));
            return this;
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot click deactivate button. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isEditButtonDisplayed() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(EDIT_BUTTON)).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check edit button display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isActivateButtonDisplayed() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(ACTIVATE_BUTTON)).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check activate button display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isDeactivateButtonDisplayed() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(DEACTIVATE_BUTTON)).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check deactivate button display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
}