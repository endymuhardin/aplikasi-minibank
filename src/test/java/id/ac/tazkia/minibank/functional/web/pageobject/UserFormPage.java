package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class UserFormPage extends BasePage {
    
    @FindBy(id = "user-form")
    private WebElement userForm;
    
    @FindBy(id = "username")
    private WebElement usernameField;
    
    @FindBy(id = "fullName")
    private WebElement fullNameField;
    
    @FindBy(id = "email")
    private WebElement emailField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(id = "branch")
    private WebElement branchField;
    
    @FindBy(id = "save-user-btn")
    private WebElement saveButton;
    
    @FindBy(id = "back-to-list-btn")
    private WebElement backToListButton;
    
    @FindBy(tagName = "h1")
    private WebElement pageTitle;
    
    public UserFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public String getPageTitle() {
        return pageTitle.getText();
    }
    
    public boolean isFormDisplayed() {
        return isElementVisible(userForm);
    }
    
    public void fillUserForm(String username, String fullName, String email, String password) {
        fillUserForm(username, fullName, email, password, null);
    }
    
    public void fillUserForm(String username, String fullName, String email, String password, String branchId) {
        if (username != null) clearAndType(usernameField, username);
        if (fullName != null) clearAndType(fullNameField, fullName);
        if (email != null) clearAndType(emailField, email);
        if (password != null) clearAndType(passwordField, password);
        
        // Select branch if provided, otherwise select first available branch
        if (branchField != null) {
            if (branchId != null) {
                selectDropdownByValue(branchField, branchId);
            } else {
                // Select first available branch (not the empty option)
                selectFirstNonEmptyOption(branchField);
            }
        }
    }
    
    public UserListPage submitForm() {
        waitForElementToBeClickable(saveButton);
        String initialUrl = getCurrentUrl();
        saveButton.click();
        
        // Wait for either success (redirect to list) or remain on form with error
        try {
            // Wait up to 10 seconds for URL change or for form to be updated
            Thread.sleep(1000); // Brief wait for initial processing
            
            for (int i = 0; i < 10; i++) {
                String currentUrl = getCurrentUrl();
                
                // Success case: redirected to list page
                if (currentUrl.contains("/rbac/users/list")) {
                    return new UserListPage(driver, baseUrl);
                }
                
                // Error case: still on form page but URL hasn't changed for a while
                if (currentUrl.equals(initialUrl) && i > 3) {
                    // Check if we have any error messages or validation errors
                    String pageSource = driver.getPageSource();
                    boolean hasValidationErrors = pageSource.contains("text-red-600") || 
                                                 pageSource.contains("username-error") || 
                                                 pageSource.contains("fullname-error") || 
                                                 pageSource.contains("email-error");
                    
                    boolean hasGeneralError = pageSource.contains("Error creating user") ||
                                            pageSource.contains("error-message");
                    
                    if (hasValidationErrors || hasGeneralError) {
                        // This is expected - form validation errors, let the test handle it
                        throw new RuntimeException("Form has validation errors");
                    }
                }
                
                Thread.sleep(1000); // Wait 1 second before checking again
            }
            
            // If we get here, something unexpected happened
            String currentUrl = getCurrentUrl();
            throw new RuntimeException("Timeout waiting for form submission. Final URL: " + currentUrl);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Test interrupted", e);
        }
    }
    
    public UserFormPage submitFormExpectingError() {
        waitForElementToBeClickable(saveButton);
        saveButton.click();
        // Wait briefly for any validation to occur
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return this;
    }
    
    public UserListPage clickBackToList() {
        waitForElementToBeClickable(backToListButton);
        backToListButton.click();
        waitForUrlToContain("/rbac/users/list");
        return new UserListPage(driver, baseUrl);
    }
    
    public String getUsername() {
        return usernameField.getAttribute("value");
    }
    
    public String getFullName() {
        return fullNameField.getAttribute("value");
    }
    
    public String getEmail() {
        return emailField.getAttribute("value");
    }
}