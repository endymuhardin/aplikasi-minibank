package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PermissionListPage extends BasePage {
    
    @FindBy(id = "create-permission-btn")
    private WebElement createPermissionButton;
    
    @FindBy(id = "permissions-table")
    private WebElement permissionsTable;
    
    @FindBy(id = "category")
    private WebElement categoryFilter;
    
    @FindBy(id = "filter-btn")
    private WebElement filterButton;
    
    @FindBy(tagName = "h1")
    private WebElement pageTitle;
    
    public PermissionListPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void open() {
        driver.get(baseUrl + "/rbac/permissions/list");
        waitForPageToLoad();
        // Wait for the table structure to be present, even if empty
        waitForElementToBePresent(By.id("permissions-table"));
    }
    
    public void openAndWaitForLoad() {
        open();
        // Wait for flash messages or table content to be rendered
        wait.until(webDriver -> 
            isElementPresent(By.id("success-message")) ||
            isElementPresent(By.id("error-message")) ||
            isElementPresent(By.cssSelector("#permissions-table tbody tr")) ||
            isElementPresent(By.cssSelector(".no-permissions-message"))
        );
    }
    
    public String getPageTitle() {
        return pageTitle.getText();
    }
    
    public boolean isCreateButtonDisplayed() {
        return isElementVisible(createPermissionButton);
    }
    
    public boolean isPermissionsTableDisplayed() {
        return isElementVisible(permissionsTable);
    }
    
    public boolean isCategoryFilterDisplayed() {
        return isElementVisible(categoryFilter);
    }
    
    public PermissionFormPage clickCreatePermission() {
        waitForElementToBeClickable(createPermissionButton);
        createPermissionButton.click();
        return new PermissionFormPage(driver, baseUrl);
    }
    
    public boolean isPermissionDisplayed(String permissionCode) {
        try {
            WebElement permissionRow = driver.findElement(By.id("permission-code-" + permissionCode));
            return permissionRow.isDisplayed();
        } catch (Exception e) {
            // Fallback: search by text content
            try {
                return driver.findElement(By.xpath("//td[contains(text(), '" + permissionCode + "')]")).isDisplayed();
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    
    public void viewPermission(String permissionCode) {
        try {
            WebElement viewButton = driver.findElement(By.id("view-permission-" + permissionCode));
            waitForElementToBeClickable(viewButton);
            viewButton.click();
        } catch (Exception e) {
            // Fallback: find view button by table row
            WebElement viewButton = driver.findElement(By.xpath("//tr[td[contains(text(), '" + permissionCode + "')]]//a[contains(@href, 'view') or contains(text(), 'View')]"));
            viewButton.click();
        }
    }
    
    public void filterByCategory(String category) {
        selectDropdownByValue(categoryFilter, category);
        filterButton.click();
        waitForElementToBeVisible(permissionsTable);
    }
    
    public PermissionFormPage editPermission(String permissionCode) {
        try {
            WebElement editButton = driver.findElement(By.id("edit-permission-" + permissionCode));
            waitForElementToBeClickable(editButton);
            editButton.click();
        } catch (Exception e) {
            // Fallback: find edit button by table row
            WebElement editButton = driver.findElement(By.xpath("//tr[td[contains(text(), '" + permissionCode + "')]]//a[contains(@href, 'edit') or contains(text(), 'Edit')]"));
            editButton.click();
        }
        return new PermissionFormPage(driver, baseUrl);
    }
    
    public void deletePermission(String permissionCode) {
        WebElement deleteButton = driver.findElement(By.id("delete-permission-" + permissionCode));
        waitForElementToBeClickable(deleteButton);
        deleteButton.click();
        // Handle confirmation dialog
        driver.switchTo().alert().accept();
    }
}