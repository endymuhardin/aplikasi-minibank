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
    
    @FindBy(id = "resource")
    private WebElement resourceField;
    
    @FindBy(id = "action")
    private WebElement actionField;
    
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
    
    public void fillPermissionForm(String permissionCode, String permissionName, String category, String resource, String action) {
        if (permissionCode != null) clearAndType(permissionCodeField, permissionCode);
        if (permissionName != null) clearAndType(permissionNameField, permissionName);
        if (category != null) selectDropdownByValue(permissionCategoryField, category);
        if (resource != null) clearAndType(resourceField, resource);
        if (action != null) clearAndType(actionField, action);
    }
    
    public PermissionListPage submitForm() {
        waitForElementToBeClickable(saveButton);
        saveButton.click();
        waitForUrlToContain("/rbac/permissions/list");
        return new PermissionListPage(driver, baseUrl);
    }
    
    public PermissionFormPage submitFormExpectingError() {
        waitForElementToBeClickable(saveButton);
        saveButton.click();
        // Wait briefly for any validation to occur
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return this;
    }
    
    public PermissionListPage clickBackToList() {
        waitForElementToBeClickable(backToListButton);
        backToListButton.click();
        waitForUrlToContain("/rbac/permissions/list");
        return new PermissionListPage(driver, baseUrl);
    }
}