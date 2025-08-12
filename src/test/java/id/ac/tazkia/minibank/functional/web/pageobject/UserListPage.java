package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class UserListPage extends BasePage {
    
    @FindBy(id = "create-user-btn")
    private WebElement createUserButton;
    
    @FindBy(id = "users-table")
    private WebElement usersTable;
    
    @FindBy(id = "search")
    private WebElement searchField;
    
    @FindBy(id = "search-btn")
    private WebElement searchButton;
    
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    public UserListPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void open() {
        driver.get(baseUrl + "/rbac/users/list");
        waitForPageToLoad();
        // Wait for the table structure to be present, even if empty
        waitForElementToBePresent(By.id("users-table"));
    }
    
    public String getPageTitle() {
        return pageTitle.getText();
    }
    
    public boolean isCreateButtonDisplayed() {
        return isElementVisible(createUserButton);
    }
    
    public UserFormPage clickCreateUser() {
        waitForElementToBeClickable(createUserButton);
        createUserButton.click();
        return new UserFormPage(driver, baseUrl);
    }
    
    public boolean isUserDisplayed(String username) {
        try {
            WebElement userRow = driver.findElement(By.id("user-name-" + username));
            return userRow.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public void searchUsers(String searchTerm) {
        clearAndType(searchField, searchTerm);
        searchButton.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("users-table")));
    }
    
    public int getUserCount() {
        List<WebElement> userRows = driver.findElements(By.id("user-rows"));
        return userRows.size();
    }
    
    public UserFormPage editUser(String username) {
        WebElement editButton = driver.findElement(By.id("edit-user-" + username));
        waitForElementToBeClickable(editButton);
        editButton.click();
        return new UserFormPage(driver, baseUrl);
    }
    
    public void activateUser(String username) {
        WebElement activateButton = driver.findElement(By.id("activate-user-" + username));
        waitForElementToBeClickable(activateButton);
        activateButton.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("success-message")));
    }
    
    public void deactivateUser(String username) {
        WebElement deactivateButton = driver.findElement(By.id("deactivate-user-" + username));
        waitForElementToBeClickable(deactivateButton);
        deactivateButton.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("success-message")));
    }
    
    public String getUserStatus(String username) {
        try {
            WebElement statusElement = driver.findElement(By.id("status-user-" + username));
            return statusElement.getText();
        } catch (Exception e) {
            throw new RuntimeException("Could not find status element for user: " + username, e);
        }
    }
}