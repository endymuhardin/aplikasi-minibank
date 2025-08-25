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
public class RBACPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Common elements
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "success-message")
    private WebElement successMessage;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;
    
    // Role management elements
    @FindBy(id = "create-role-btn")
    private WebElement createRoleButton;
    
    @FindBy(id = "roles-table")
    private WebElement rolesTable;
    
    @FindBy(id = "no-roles-message")
    private WebElement noRolesMessage;
    
    // Permission management elements
    @FindBy(id = "create-permission-btn")
    private WebElement createPermissionButton;
    
    @FindBy(id = "category")
    private WebElement categorySelect;
    
    @FindBy(id = "filter-btn")
    private WebElement filterButton;
    
    @FindBy(id = "permissions-table")
    private WebElement permissionsTable;
    
    @FindBy(id = "no-permissions-message")
    private WebElement noPermissionsMessage;
    
    // Form elements (for both roles and permissions)
    @FindBy(id = "roleCode")
    private WebElement roleCodeInput;
    
    @FindBy(id = "roleName")
    private WebElement roleNameInput;
    
    @FindBy(id = "description")
    private WebElement descriptionInput;
    
    @FindBy(id = "permissionCode")
    private WebElement permissionCodeInput;
    
    @FindBy(id = "permissionName")
    private WebElement permissionNameInput;
    
    @FindBy(id = "permissionCategory")
    private WebElement permissionCategorySelect;
    
    @FindBy(id = "save-role-btn")
    private WebElement roleSubmitButton;
    
    @FindBy(id = "save-permission-btn")
    private WebElement permissionSubmitButton;
    
    // User role assignment elements
    @FindBy(id = "assign-roles-btn")
    private WebElement assignRolesButton;
    
    public RBACPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to roles list page
     */
    public RBACPage navigateToRolesList(String baseUrl) {
        driver.get(baseUrl + "/rbac/roles/list");
        return waitForRolesListPageLoad();
    }
    
    /**
     * Navigate to permissions list page
     */
    public RBACPage navigateToPermissionsList(String baseUrl) {
        driver.get(baseUrl + "/rbac/permissions/list");
        return waitForPermissionsListPageLoad();
    }
    
    /**
     * Navigate to create role form
     */
    public RBACPage navigateToCreateRole(String baseUrl) {
        driver.get(baseUrl + "/rbac/roles/create");
        return waitForRoleFormPageLoad();
    }
    
    /**
     * Navigate to create permission form
     */
    public RBACPage navigateToCreatePermission(String baseUrl) {
        driver.get(baseUrl + "/rbac/permissions/create");
        return waitForPermissionFormPageLoad();
    }
    
    /**
     * Navigate to role permissions management
     */
    public RBACPage navigateToRolePermissions(String baseUrl, String roleId) {
        driver.get(baseUrl + "/rbac/roles/" + roleId + "/permissions");
        return waitForRolePermissionsPageLoad();
    }
    
    /**
     * Wait for roles list page to load
     */
    public RBACPage waitForRolesListPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "Role Management"));
        log.debug("Roles list page loaded successfully");
        return this;
    }
    
    /**
     * Wait for permissions list page to load
     */
    public RBACPage waitForPermissionsListPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "Permission Management"));
        log.debug("Permissions list page loaded successfully");
        return this;
    }
    
    /**
     * Wait for role form page to load
     */
    public RBACPage waitForRoleFormPageLoad() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.textToBePresentInElement(pageTitle, "Create Role"),
            ExpectedConditions.textToBePresentInElement(pageTitle, "Edit Role")
        ));
        wait.until(ExpectedConditions.visibilityOf(roleCodeInput));
        log.debug("Role form page loaded successfully");
        return this;
    }
    
    /**
     * Wait for permission form page to load
     */
    public RBACPage waitForPermissionFormPageLoad() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.textToBePresentInElement(pageTitle, "Create Permission"),
            ExpectedConditions.textToBePresentInElement(pageTitle, "Edit Permission")
        ));
        wait.until(ExpectedConditions.visibilityOf(permissionCodeInput));
        log.debug("Permission form page loaded successfully");
        return this;
    }
    
    /**
     * Wait for role permissions management page to load
     */
    public RBACPage waitForRolePermissionsPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        wait.until(ExpectedConditions.textToBePresentInElement(pageTitle, "Manage Role Permissions"));
        log.debug("Role permissions page loaded successfully");
        return this;
    }
    
    /**
     * Check if roles list page is loaded
     */
    public boolean isRolesListPageLoaded() {
        try {
            waitForRolesListPageLoad();
            return driver.getCurrentUrl().contains("/rbac/roles/list") &&
                   pageTitle.getText().contains("Role Management");
        } catch (Exception e) {
            log.debug("Roles list page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if permissions list page is loaded
     */
    public boolean isPermissionsListPageLoaded() {
        try {
            waitForPermissionsListPageLoad();
            return driver.getCurrentUrl().contains("/rbac/permissions/list") &&
                   pageTitle.getText().contains("Permission Management");
        } catch (Exception e) {
            log.debug("Permissions list page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if create role button is visible
     */
    public boolean isCreateRoleButtonVisible() {
        try {
            return createRoleButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if create permission button is visible
     */
    public boolean isCreatePermissionButtonVisible() {
        try {
            return createPermissionButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if roles are displayed
     */
    public boolean areRolesDisplayed() {
        try {
            return rolesTable.isDisplayed() && 
                   !isNoRolesMessageVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if no roles message is visible
     */
    public boolean isNoRolesMessageVisible() {
        try {
            return noRolesMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if permissions are displayed
     */
    public boolean arePermissionsDisplayed() {
        try {
            return permissionsTable.isDisplayed() && 
                   !isNoPermissionsMessageVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if no permissions message is visible
     */
    public boolean isNoPermissionsMessageVisible() {
        try {
            return noPermissionsMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if a specific role is visible
     */
    public boolean isRoleVisible(String roleCode) {
        try {
            WebElement roleElement = driver.findElement(
                org.openqa.selenium.By.id("role-code-" + roleCode)
            );
            return roleElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get role status
     */
    public String getRoleStatus(String roleCode) {
        try {
            WebElement statusElement = driver.findElement(
                org.openqa.selenium.By.id("status-role-" + roleCode)
            );
            return statusElement.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Click view role button
     */
    public RBACPage clickViewRole(String roleCode) {
        try {
            WebElement viewButton = driver.findElement(
                org.openqa.selenium.By.id("view-role-" + roleCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(viewButton));
            viewButton.click();
            log.debug("Clicked view button for role: {}", roleCode);
        } catch (Exception e) {
            log.error("Could not click view button for role: {}", roleCode, e);
        }
        return this;
    }
    
    /**
     * Click edit role button
     */
    public RBACPage clickEditRole(String roleCode) {
        try {
            WebElement editButton = driver.findElement(
                org.openqa.selenium.By.id("edit-role-" + roleCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(editButton));
            editButton.click();
            log.debug("Clicked edit button for role: {}", roleCode);
        } catch (Exception e) {
            log.error("Could not click edit button for role: {}", roleCode, e);
        }
        return this;
    }
    
    /**
     * Click permissions button for role
     */
    public RBACPage clickRolePermissions(String roleCode) {
        try {
            WebElement permissionsButton = driver.findElement(
                org.openqa.selenium.By.id("permissions-role-" + roleCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(permissionsButton));
            permissionsButton.click();
            log.debug("Clicked permissions button for role: {}", roleCode);
        } catch (Exception e) {
            log.error("Could not click permissions button for role: {}", roleCode, e);
        }
        return this;
    }
    
    /**
     * Click activate role button
     */
    public RBACPage clickActivateRole(String roleCode) {
        try {
            WebElement activateButton = driver.findElement(
                org.openqa.selenium.By.id("activate-role-" + roleCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(activateButton));
            activateButton.click();
            log.debug("Clicked activate button for role: {}", roleCode);
        } catch (Exception e) {
            log.error("Could not click activate button for role: {}", roleCode, e);
        }
        return this;
    }
    
    /**
     * Click deactivate role button
     */
    public RBACPage clickDeactivateRole(String roleCode) {
        try {
            WebElement deactivateButton = driver.findElement(
                org.openqa.selenium.By.id("deactivate-role-" + roleCode)
            );
            wait.until(ExpectedConditions.elementToBeClickable(deactivateButton));
            deactivateButton.click();
            log.debug("Clicked deactivate button for role: {}", roleCode);
        } catch (Exception e) {
            log.error("Could not click deactivate button for role: {}", roleCode, e);
        }
        return this;
    }
    
    /**
     * Filter permissions by category
     */
    public RBACPage filterPermissionsByCategory(String category) {
        wait.until(ExpectedConditions.visibilityOf(categorySelect));
        Select categoryDropdown = new Select(categorySelect);
        categoryDropdown.selectByVisibleText(category);
        filterButton.click();
        log.debug("Filtered permissions by category: {}", category);
        return this;
    }
    
    /**
     * Fill role form
     */
    public RBACPage fillRoleForm(String roleCode, String roleName, String description) {
        wait.until(ExpectedConditions.visibilityOf(roleCodeInput));
        
        roleCodeInput.clear();
        if (roleCode != null && !roleCode.isEmpty()) {
            roleCodeInput.sendKeys(roleCode);
        }
        
        roleNameInput.clear();
        if (roleName != null && !roleName.isEmpty()) {
            roleNameInput.sendKeys(roleName);
        }
        
        if (description != null && !description.isEmpty()) {
            descriptionInput.clear();
            descriptionInput.sendKeys(description);
        }
        
        log.debug("Filled role form for: {}", roleCode);
        return this;
    }
    
    /**
     * Fill permission form
     */
    public RBACPage fillPermissionForm(String permissionCode, String permissionName, 
                                      String category, String description) {
        wait.until(ExpectedConditions.visibilityOf(permissionCodeInput));
        
        permissionCodeInput.clear();
        permissionCodeInput.sendKeys(permissionCode);
        
        permissionNameInput.clear();
        permissionNameInput.sendKeys(permissionName);
        
        if (category != null && !category.isEmpty()) {
            Select categoryDropdown = new Select(permissionCategorySelect);
            categoryDropdown.selectByVisibleText(category);
        }
        
        if (description != null && !description.isEmpty()) {
            descriptionInput.clear();
            descriptionInput.sendKeys(description);
        }
        
        log.debug("Filled permission form for: {}", permissionCode);
        return this;
    }
    
    /**
     * Submit form
     */
    public RBACPage submitForm() {
        // Try role submit button first, then permission submit button
        try {
            wait.until(ExpectedConditions.elementToBeClickable(roleSubmitButton));
            roleSubmitButton.click();
            log.debug("Submitted role form");
        } catch (Exception e) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(permissionSubmitButton));
                permissionSubmitButton.click();
                log.debug("Submitted permission form");
            } catch (Exception e2) {
                log.error("Could not find submit button for form");
                throw new RuntimeException("No submit button found", e2);
            }
        }
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
     * Get number of roles displayed
     */
    public int getRoleCount() {
        try {
            if (!areRolesDisplayed()) {
                return 0;
            }
            // Use page source analysis to count roles since we can't use xpath or className
            String pageSource = driver.getPageSource();
            int count = 0;
            // Count occurrences of role-row- IDs which indicate actual role entries
            String searchPattern = "role-row-";
            int index = 0;
            while ((index = pageSource.indexOf(searchPattern, index)) != -1) {
                count++;
                index += searchPattern.length();
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Get number of permissions displayed
     */
    public int getPermissionCount() {
        try {
            if (!arePermissionsDisplayed()) {
                return 0;
            }
            // Use page source analysis to count permissions since we can't use xpath or className
            String pageSource = driver.getPageSource();
            int count = 0;
            // Count occurrences of permission-row- IDs which indicate actual permission entries
            String searchPattern = "permission-row-";
            int index = 0;
            while ((index = pageSource.indexOf(searchPattern, index)) != -1) {
                count++;
                index += searchPattern.length();
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Check if user has access to RBAC functionality
     */
    public boolean hasRBACAccess() {
        try {
            return isRolesListPageLoaded() || isPermissionsListPageLoaded();
        } catch (Exception e) {
            return false;
        }
    }
}