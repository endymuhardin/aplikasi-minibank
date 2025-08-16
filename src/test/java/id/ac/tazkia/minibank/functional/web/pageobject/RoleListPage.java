package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@Slf4j
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
        waitForPageToLoad();
        // Wait for the table structure to be present, even if empty
        waitForElementToBePresent(By.id("roles-table"));
    }
    
    @Override
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
            WebElement roleRow = driver.findElement(By.id("role-code-" + roleCode));
            return roleRow.isDisplayed();
        } catch (Exception e) {
            log.error("Error checking element display status", e);
            return false;
        }
    }
    
    public RoleFormPage editRole(String roleCode) {
        WebElement editButton = driver.findElement(By.id("edit-role-" + roleCode));
        waitForElementToBeClickable(editButton);
        editButton.click();
        return new RoleFormPage(driver, baseUrl);
    }
    
    public String getRoleStatus(String roleCode) {
        try {
            WebElement statusElement = driver.findElement(By.id("status-role-" + roleCode));
            return statusElement.getText();
        } catch (Exception e) {
            throw new RuntimeException("Could not find status element for role: " + roleCode, e);
        }
    }
    
    public void activateRole(String roleCode) {
        WebElement activateButton = driver.findElement(By.id("activate-role-" + roleCode));
        waitForElementToBeClickable(activateButton);
        activateButton.click();
    }
    
    public void deactivateRole(String roleCode) {
        WebElement deactivateButton = driver.findElement(By.id("deactivate-role-" + roleCode));
        waitForElementToBeClickable(deactivateButton);
        deactivateButton.click();
    }
}