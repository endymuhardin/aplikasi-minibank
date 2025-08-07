package id.ac.tazkia.minibank.integration.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RoleListPage extends BasePage {
    
    @FindBy(id = "create-role-btn")
    private WebElement createRoleButton;
    
    @FindBy(id = "roles-table")
    private WebElement rolesTable;
    
    @FindBy(tagName = "h1")
    private WebElement pageTitle;
    
    public RoleListPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void open() {
        driver.get(baseUrl + "/rbac/roles/list");
        waitForElementToBeVisible(rolesTable);
    }
    
    public String getPageTitle() {
        return pageTitle.getText();
    }
    
    public boolean isCreateButtonDisplayed() {
        return isElementVisible(createRoleButton);
    }
    
    public RoleFormPage clickCreateRole() {
        waitForElementToBeClickable(createRoleButton);
        createRoleButton.click();
        return new RoleFormPage(driver, baseUrl);
    }
    
    public boolean isRoleDisplayed(String roleCode) {
        try {
            WebElement roleRow = driver.findElement(By.xpath("//tr[contains(@class, 'role-row')]//div[text()='" + roleCode + "']"));
            return roleRow.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public RoleFormPage editRole(String roleCode) {
        WebElement editButton = driver.findElement(
            By.xpath("//tr[contains(@class, 'role-row')]//div[text()='" + roleCode + "']/ancestor::tr//a[contains(@class, 'edit-role-btn')]")
        );
        editButton.click();
        return new RoleFormPage(driver, baseUrl);
    }
}