package id.ac.tazkia.minibank.integration.web.pageobject;

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
        waitForElementToBeVisible(permissionsTable);
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
            WebElement permissionRow = driver.findElement(By.xpath("//tr[contains(@class, 'permission-row')]//div[text()='" + permissionCode + "']"));
            return permissionRow.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public void filterByCategory(String category) {
        selectDropdownByValue(categoryFilter, category);
        filterButton.click();
        waitForElementToBeVisible(permissionsTable);
    }
}