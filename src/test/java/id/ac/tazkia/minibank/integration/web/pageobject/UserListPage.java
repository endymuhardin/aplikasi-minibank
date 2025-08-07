package id.ac.tazkia.minibank.integration.web.pageobject;

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
    
    @FindBy(tagName = "h1")
    private WebElement pageTitle;
    
    public UserListPage(WebDriver driver, String baseUrl) {
        super(driver, baseUrl);
    }
    
    public void open() {
        driver.get(baseUrl + "/rbac/users/list");
        waitForElementToBeVisible(usersTable);
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
            WebElement userRow = driver.findElement(By.xpath("//tr[contains(@class, 'user-row')]//div[text()='" + username + "']"));
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
        List<WebElement> userRows = driver.findElements(By.cssSelector("tr.user-row"));
        return userRows.size();
    }
    
    public UserFormPage editUser(String username) {
        WebElement editButton = driver.findElement(
            By.xpath("//tr[contains(@class, 'user-row')]//div[text()='" + username + "']/ancestor::tr//a[contains(@class, 'edit-user-btn')]")
        );
        editButton.click();
        return new UserFormPage(driver, baseUrl);
    }
    
    public void activateUser(String username) {
        WebElement activateButton = driver.findElement(
            By.xpath("//tr[contains(@class, 'user-row')]//div[text()='" + username + "']/ancestor::tr//button[contains(@class, 'activate-user-btn')]")
        );
        activateButton.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("success-message")));
    }
    
    public void deactivateUser(String username) {
        WebElement deactivateButton = driver.findElement(
            By.xpath("//tr[contains(@class, 'user-row')]//div[text()='" + username + "']/ancestor::tr//button[contains(@class, 'deactivate-user-btn')]")
        );
        deactivateButton.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("success-message")));
    }
    
    public String getUserStatus(String username) {
        WebElement statusElement = driver.findElement(
            By.xpath("//tr[contains(@class, 'user-row')]//div[text()='" + username + "']/ancestor::tr//td[4]")
        );
        return statusElement.getText();
    }
}