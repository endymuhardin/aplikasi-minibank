package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PermissionFormPage extends BasePage {
    
    @FindBy(id = "permission-form")
    private WebElement permissionForm;
    
    @FindBy(id = "permissionCode")
    private WebElement permissionCodeField;
    
    @FindBy(id = "permissionName")
    private WebElement permissionNameField;
    
    @FindBy(id = "permissionCategory")
    private WebElement permissionCategoryField;
    
    
    @FindBy(id = "description")
    private WebElement descriptionField;
    
    @FindBy(id = "save-permission-btn")
    private WebElement saveButton;
    
    @FindBy(id = "back-to-list-btn")
    private WebElement backToListButton;
    
    @FindBy(tagName = "h1")
    private WebElement pageTitle;
    
    public PermissionFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public String getPageTitle() {
        return pageTitle.getText();
    }
    
    public boolean isFormDisplayed() {
        return isElementVisible(permissionForm);
    }
    
    public void fillPermissionForm(String permissionCode, String permissionName, String category, String description) {
        if (permissionCode != null) clearAndType(permissionCodeField, permissionCode);
        if (permissionName != null) clearAndType(permissionNameField, permissionName);
        if (category != null) clearAndType(permissionCategoryField, category);
        if (description != null) clearAndType(descriptionField, description);
    }
    
    public String getPermissionCode() {
        return permissionCodeField.getAttribute("value");
    }
    
    public boolean hasValidationError(String fieldName) {
        try {
            return driver.findElement(org.openqa.selenium.By.id(fieldName + "-error")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    
    public PermissionListPage submitForm() {
        waitForElementToBeClickable(saveButton);
        saveButton.click();
        // Wait for redirect to complete
        waitForUrlToContain("/rbac/permissions/list");
        // Wait for page to be fully loaded after redirect
        waitForPageToLoad();
        // Wait for either success or error message to appear
        wait.until(webDriver -> 
            isElementPresent(org.openqa.selenium.By.id("success-message")) ||
            isElementPresent(org.openqa.selenium.By.id("error-message")) ||
            isElementPresent(org.openqa.selenium.By.id("permissions-table"))
        );
        return new PermissionListPage(driver, baseUrl);
    }
    
    public PermissionFormPage submitFormExpectingError() {
        waitForElementToBeClickable(saveButton);
        saveButton.click();
        
        // Wait for page to reload with validation errors
        waitForPageToLoad();
        
        // Wait for validation errors to appear or confirm we stayed on form page
        wait.until(webDriver -> 
            hasValidationError("permissionCode") || 
            hasValidationError("permissionName") || 
            hasValidationError("permissionCategory") || 
            isErrorMessageDisplayed() ||
            // Ensure we stayed on form page (validation failed)
            driver.getCurrentUrl().contains("/rbac/permissions/create") ||
            driver.getCurrentUrl().contains("/rbac/permissions/edit") ||
            // Check for presence of the form itself
            isElementPresent(org.openqa.selenium.By.id("permission-form"))
        );
        
        return this;
    }
    
    public PermissionListPage clickBackToList() {
        waitForElementToBeClickable(backToListButton);
        backToListButton.click();
        waitForUrlToContain("/rbac/permissions/list");
        return new PermissionListPage(driver, baseUrl);
    }
}