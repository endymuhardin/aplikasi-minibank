package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

@Slf4j
public class RoleFormPage extends BasePage {
    
    @FindBy(id = "role-form")
    private WebElement roleForm;
    
    @FindBy(id = "roleCode")
    private WebElement roleCodeField;
    
    @FindBy(id = "roleName")
    private WebElement roleNameField;
    
    @FindBy(id = "description")
    private WebElement descriptionField;
    
    @FindBy(id = "save-role-btn")
    private WebElement saveButton;
    
    @FindBy(id = "back-to-list-btn")
    private WebElement backToListButton;
    
    @FindBy(tagName = "h1")
    private WebElement pageTitle;
    
    public RoleFormPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    @Override
    public String getPageTitle() {
        return pageTitle.getText();
    }
    
    public boolean isFormDisplayed() {
        return isElementVisible(roleForm);
    }
    
    public void fillRoleForm(String roleCode, String roleName, String description) {
        if (roleCode != null) clearAndType(roleCodeField, roleCode);
        if (roleName != null) clearAndType(roleNameField, roleName);
        if (description != null) clearAndType(descriptionField, description);
    }
    
    public RoleListPage submitForm() {
        waitForElementToBeClickable(saveButton);
        saveButton.click();
        
        // Wait for either success (redirect to list) or validation error (stay on form)
        try {
            waitForUrlToContain("/rbac/roles/list");
            return new RoleListPage(driver, baseUrl);
        } catch (Exception e) {
            // If we don't redirect, check if there are validation errors
            if (getCurrentUrl().contains("/rbac/roles/create") || getCurrentUrl().contains("/rbac/roles/edit")) {
                // Stay on form page - validation errors occurred
                throw new RuntimeException("Form validation failed or form submission error occurred");
            }
            throw e;
        }
    }
    
    public RoleFormPage submitFormExpectingError() {
        waitForElementToBeClickable(saveButton);
        saveButton.click();
        // Wait briefly for any validation to occur
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(500));
        try {
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
        } catch (Exception e) {
            log.debug("Validation wait timed out, continuing...", e);
        }
        return this;
    }
    
    public RoleListPage clickBackToList() {
        waitForElementToBeClickable(backToListButton);
        backToListButton.click();
        waitForUrlToContain("/rbac/roles/list");
        return new RoleListPage(driver, baseUrl);
    }
    
    public String getRoleCode() {
        return roleCodeField.getAttribute("value");
    }
    
    public String getRoleName() {
        return roleNameField.getAttribute("value");
    }
    
    public String getDescription() {
        return descriptionField.getAttribute("value");
    }
}