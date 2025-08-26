package id.ac.tazkia.minibank.selenium.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // User list page elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "create-user-btn")
    private WebElement createUserButton;
    
    @FindBy(id = "search")
    private WebElement searchInput;
    
    @FindBy(id = "search-btn")
    private WebElement searchButton;
    
    @FindBy(id = "clear-search-btn")
    private WebElement clearSearchButton;
    
    @FindBy(id = "users-table")
    private WebElement usersTable;
    
    @FindBy(id = "no-users-message")
    private WebElement noUsersMessage;
    
    // User form elements
    @FindBy(id = "user-form")
    private WebElement userForm;
    
    @FindBy(id = "username")
    private WebElement usernameInput;
    
    @FindBy(id = "fullName")
    private WebElement fullNameInput;
    
    @FindBy(id = "email")
    private WebElement emailInput;
    
    @FindBy(id = "branch")
    private WebElement branchSelect;
    
    @FindBy(id = "isActive")
    private WebElement isActiveCheckbox;
    
    @FindBy(id = "save-user-btn")
    private WebElement saveUserButton;
    
    @FindBy(id = "cancel-btn")
    private WebElement cancelButton;
    
    @FindBy(id = "back-to-list-btn")
    private WebElement backToListButton;
    
    // Flash messages
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    // Validation errors
    @FindBy(id = "username-error")
    private WebElement usernameError;
    
    @FindBy(id = "fullname-error")
    private WebElement fullNameError;
    
    @FindBy(id = "email-error")
    private WebElement emailError;
    
    @FindBy(id = "branch-error")
    private WebElement branchError;
    
    // Pagination elements
    @FindBy(id = "prev-page-btn")
    private WebElement prevPageButton;
    
    @FindBy(id = "next-page-btn")
    private WebElement nextPageButton;
    
    @FindBy(id = "current-page-indicator")
    private WebElement currentPageIndicator;
    
    public UserPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to user management list page
     */
    public UserPage navigateToUserList(String baseUrl) {
        driver.get(baseUrl + "/rbac/users/list");
        return waitForUserListPageLoad();
    }
    
    /**
     * Navigate to create user form
     */
    public UserPage navigateToCreateUser(String baseUrl) {
        driver.get(baseUrl + "/rbac/users/create");
        return waitForUserFormPageLoad();
    }
    
    /**
     * Navigate to edit user form
     */
    public UserPage navigateToEditUser(String baseUrl, String userId) {
        driver.get(baseUrl + "/rbac/users/edit/" + userId);
        return waitForUserFormPageLoad();
    }
    
    /**
     * Navigate to view user page
     */
    public UserPage navigateToViewUser(String baseUrl, String userId) {
        driver.get(baseUrl + "/rbac/users/view/" + userId);
        return waitForUserViewPageLoad();
    }
    
    /**
     * Wait for user list page to load
     */
    public UserPage waitForUserListPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "User Management"));
        // Don't wait for createUserButton as it's role-dependent
        // Instead, wait for the search field which should always be present
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        log.debug("User list page loaded successfully");
        return this;
    }
    
    /**
     * Wait for user form page to load
     */
    public UserPage waitForUserFormPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(userForm));
        wait.until(ExpectedConditions.visibilityOf(usernameInput));
        wait.until(ExpectedConditions.visibilityOf(saveUserButton));
        log.debug("User form page loaded successfully");
        return this;
    }
    
    /**
     * Wait for user view page to load
     */
    public UserPage waitForUserViewPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "View User"));
        log.debug("User view page loaded successfully");
        return this;
    }
    
    /**
     * Check if user list page is loaded
     */
    public boolean isUserListPageLoaded() {
        try {
            waitForUserListPageLoad();
            return driver.getCurrentUrl().contains("/rbac/users/list") &&
                   pageTitle.getText().contains("User Management");
        } catch (Exception e) {
            log.debug("User list page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user form page is loaded
     */
    public boolean isUserFormPageLoaded() {
        try {
            waitForUserFormPageLoad();
            return (driver.getCurrentUrl().contains("/rbac/users/create") ||
                    driver.getCurrentUrl().contains("/rbac/users/edit")) &&
                   userForm.isDisplayed();
        } catch (Exception e) {
            log.debug("User form page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user view page is loaded
     */
    public boolean isUserViewPageLoaded() {
        try {
            waitForUserViewPageLoad();
            return driver.getCurrentUrl().contains("/rbac/users/view") &&
                   pageTitle.getText().contains("View User");
        } catch (Exception e) {
            log.debug("User view page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if create user button is visible
     */
    public boolean isCreateUserButtonVisible() {
        try {
            return createUserButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click create user button
     */
    public UserPage clickCreateUser() {
        wait.until(ExpectedConditions.elementToBeClickable(createUserButton));
        createUserButton.click();
        log.debug("Clicked create user button");
        return this;
    }
    
    /**
     * Search for users
     */
    public UserPage searchUsers(String searchTerm) {
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.clear();
        searchInput.sendKeys(searchTerm);
        searchButton.click();
        log.debug("Searched users with term: {}", searchTerm);
        return this;
    }
    
    /**
     * Clear search
     */
    public UserPage clearSearch() {
        wait.until(ExpectedConditions.elementToBeClickable(clearSearchButton));
        clearSearchButton.click();
        log.debug("Cleared user search");
        return this;
    }
    
    /**
     * Check if users are displayed in the table
     */
    public boolean areUsersDisplayed() {
        try {
            return usersTable.isDisplayed() && 
                   !isNoUsersMessageVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if no users message is visible
     */
    public boolean isNoUsersMessageVisible() {
        try {
            return noUsersMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if a specific user is visible in the list
     */
    public boolean isUserVisible(String username) {
        try {
            WebElement userElement = driver.findElement(
                org.openqa.selenium.By.id("user-name-" + username)
            );
            return userElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get user status from the list
     */
    public String getUserStatus(String username) {
        try {
            // Wait for element to be present and have non-empty text
            WebElement statusElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                org.openqa.selenium.By.id("status-user-" + username)
            ));
            // Additional wait for text to appear
            wait.until(driver -> !statusElement.getText().trim().isEmpty());
            return statusElement.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Check if user is locked
     */
    public boolean isUserLocked(String username) {
        try {
            WebElement lockedElement = driver.findElement(
                org.openqa.selenium.By.id("locked-user-" + username)
            );
            return lockedElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if a specific action button exists for a user
     */
    public boolean hasActionButton(String username, String action) {
        try {
            WebElement actionButton = driver.findElement(
                org.openqa.selenium.By.id(action + "-user-" + username)
            );
            return actionButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click view user button for specific user
     */
    public UserPage clickViewUser(String username) {
        try {
            WebElement viewButton = driver.findElement(
                org.openqa.selenium.By.id("view-user-" + username)
            );
            wait.until(ExpectedConditions.elementToBeClickable(viewButton));
            viewButton.click();
            log.debug("Clicked view user button for: {}", username);
        } catch (Exception e) {
            log.error("Could not click view user button for: {}", username, e);
        }
        return this;
    }
    
    /**
     * Click edit user button for specific user
     */
    public UserPage clickEditUser(String username) {
        try {
            WebElement editButton = driver.findElement(
                org.openqa.selenium.By.id("edit-user-" + username)
            );
            wait.until(ExpectedConditions.elementToBeClickable(editButton));
            editButton.click();
            log.debug("Clicked edit user button for: {}", username);
        } catch (Exception e) {
            log.error("Could not click edit user button for: {}", username, e);
        }
        return this;
    }
    
    /**
     * Click delete user button for specific user
     */
    public UserPage clickDeleteUser(String username) {
        try {
            WebElement deleteButton = driver.findElement(
                org.openqa.selenium.By.id("delete-user-" + username)
            );
            wait.until(ExpectedConditions.elementToBeClickable(deleteButton));
            deleteButton.click();
            log.debug("Clicked delete user button for: {}", username);
        } catch (Exception e) {
            log.error("Could not click delete user button for: {}", username, e);
        }
        return this;
    }
    
    /**
     * Click activate user button for specific user
     */
    public UserPage clickActivateUser(String username) {
        try {
            WebElement activateButton = driver.findElement(
                org.openqa.selenium.By.id("activate-user-" + username)
            );
            wait.until(ExpectedConditions.elementToBeClickable(activateButton));
            activateButton.click();
            log.debug("Clicked activate user button for: {}", username);
        } catch (Exception e) {
            log.error("Could not click activate user button for: {}", username, e);
        }
        return this;
    }
    
    /**
     * Click deactivate user button for specific user
     */
    public UserPage clickDeactivateUser(String username) {
        try {
            WebElement deactivateButton = driver.findElement(
                org.openqa.selenium.By.id("deactivate-user-" + username)
            );
            wait.until(ExpectedConditions.elementToBeClickable(deactivateButton));
            deactivateButton.click();
            log.debug("Clicked deactivate user button for: {}", username);
        } catch (Exception e) {
            log.error("Could not click deactivate user button for: {}", username, e);
        }
        return this;
    }
    
    /**
     * Fill username field
     */
    public UserPage fillUsername(String username) {
        wait.until(ExpectedConditions.visibilityOf(usernameInput));
        usernameInput.clear();
        usernameInput.sendKeys(username);
        log.debug("Filled username: {}", username);
        return this;
    }
    
    /**
     * Fill full name field
     */
    public UserPage fillFullName(String fullName) {
        wait.until(ExpectedConditions.visibilityOf(fullNameInput));
        fullNameInput.clear();
        fullNameInput.sendKeys(fullName);
        log.debug("Filled full name: {}", fullName);
        return this;
    }
    
    /**
     * Fill email field
     */
    public UserPage fillEmail(String email) {
        wait.until(ExpectedConditions.visibilityOf(emailInput));
        emailInput.clear();
        emailInput.sendKeys(email);
        log.debug("Filled email: {}", email);
        return this;
    }
    
    /**
     * Select branch
     */
    public UserPage selectBranch(String branchName) {
        try {
            wait.until(ExpectedConditions.visibilityOf(branchSelect));
            Select select = new Select(branchSelect);
            select.selectByVisibleText(branchName);
            log.debug("Selected branch: {}", branchName);
        } catch (Exception e) {
            log.debug("Could not select branch '{}': {}", branchName, e.getMessage());
        }
        return this;
    }
    
    /**
     * Get available branches
     */
    public List<String> getAvailableBranches() {
        try {
            wait.until(ExpectedConditions.visibilityOf(branchSelect));
            Select select = new Select(branchSelect);
            return select.getOptions().stream()
                    .map(WebElement::getText)
                    .filter(text -> !text.equals("Select Branch"))
                    .toList();
        } catch (Exception e) {
            log.debug("Could not get available branches: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Set active status (checkbox)
     */
    public UserPage setActiveStatus(boolean active) {
        try {
            wait.until(ExpectedConditions.visibilityOf(isActiveCheckbox));
            if (isActiveCheckbox.isSelected() != active) {
                isActiveCheckbox.click();
            }
            log.debug("Set active status: {}", active);
        } catch (Exception e) {
            log.debug("Could not set active status: {}", e.getMessage());
        }
        return this;
    }
    
    /**
     * Submit user form
     */
    public UserPage submitForm() {
        wait.until(ExpectedConditions.elementToBeClickable(saveUserButton));
        saveUserButton.click();
        log.debug("Clicked save user button");
        return this;
    }
    
    /**
     * Click cancel button
     */
    public UserPage clickCancel() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton));
        cancelButton.click();
        log.debug("Clicked cancel button");
        return this;
    }
    
    /**
     * Click back to list button
     */
    public UserPage clickBackToList() {
        wait.until(ExpectedConditions.elementToBeClickable(backToListButton));
        backToListButton.click();
        log.debug("Clicked back to list button");
        return this;
    }
    
    /**
     * Check if success message is visible
     */
    public boolean isSuccessMessageVisible() {
        try {
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get success message text
     */
    public String getSuccessMessage() {
        if (isSuccessMessageVisible()) {
            return successMessage.getText();
        }
        return "";
    }
    
    /**
     * Check if error message is visible
     */
    public boolean isErrorMessageVisible() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get error message text
     */
    public String getErrorMessage() {
        if (isErrorMessageVisible()) {
            return errorMessage.getText();
        }
        return "";
    }
    
    /**
     * Check if validation errors are visible
     */
    public boolean hasValidationErrors() {
        return hasUsernameError() || hasFullNameError() || hasEmailError() || hasBranchError();
    }
    
    /**
     * Check for username validation error
     */
    public boolean hasUsernameError() {
        try {
            return usernameError.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check for full name validation error
     */
    public boolean hasFullNameError() {
        try {
            return fullNameError.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check for email validation error
     */
    public boolean hasEmailError() {
        try {
            return emailError.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check for branch validation error
     */
    public boolean hasBranchError() {
        try {
            return branchError.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if pagination is visible
     */
    public boolean isPaginationVisible() {
        try {
            return prevPageButton.isDisplayed() || nextPageButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get current page number
     */
    public String getCurrentPageNumber() {
        try {
            return currentPageIndicator.getText();
        } catch (Exception e) {
            return "1";
        }
    }
    
    /**
     * Click next page
     */
    public UserPage clickNextPage() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(nextPageButton));
            nextPageButton.click();
            log.debug("Clicked next page button");
        } catch (Exception e) {
            log.debug("Could not click next page: {}", e.getMessage());
        }
        return this;
    }
    
    /**
     * Click previous page
     */
    public UserPage clickPrevPage() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(prevPageButton));
            prevPageButton.click();
            log.debug("Clicked previous page button");
        } catch (Exception e) {
            log.debug("Could not click previous page: {}", e.getMessage());
        }
        return this;
    }
    
    /**
     * Check if form is ready for submission
     */
    public boolean isFormReadyForSubmission() {
        try {
            return !usernameInput.getAttribute("value").isEmpty() &&
                   !fullNameInput.getAttribute("value").isEmpty() &&
                   !emailInput.getAttribute("value").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}