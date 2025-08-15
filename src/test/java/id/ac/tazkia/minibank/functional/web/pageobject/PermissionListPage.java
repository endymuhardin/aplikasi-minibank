package id.ac.tazkia.minibank.functional.web.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Slf4j
public class PermissionListPage extends BasePage {
    
    @FindBy(id = "create-permission-btn")
    private WebElement createPermissionButton;
    
    @FindBy(id = "permissions-table")
    private WebElement permissionsTable;
    
    @FindBy(id = "category")
    private WebElement categoryFilter;
    
    @FindBy(id = "filter-btn")
    private WebElement filterButton;
    
    @FindBy(id = "page-title")
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
        // Wait for the page to be fully loaded by checking for actual elements that exist
        wait.until(webDriver -> 
            isElementPresent(By.id("permissions-table")) &&
            (isElementPresent(By.id("success-message")) ||
             isElementPresent(By.id("error-message")) ||
             isElementPresent(By.className("permission-row")) ||
             isElementPresent(By.id("no-permissions-message")))
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
        return findPermissionAcrossPages(permissionCode) != null;
    }
    
    /**
     * Searches for a permission across all pages using pagination
     */
    private WebElement findPermissionAcrossPages(String permissionCode) {
        log.info("Searching for permission {} across all pages", permissionCode);
        
        // First try current page
        WebElement permission = findPermissionOnCurrentPage(permissionCode);
        if (permission != null) {
            log.info("Found permission {} on current page", permissionCode);
            return permission;
        }
        
        // Search across all pages by starting with larger page size
        try {
            String baseUrl = driver.getCurrentUrl().split("\\?")[0];
            driver.get(baseUrl + "?page=0&size=100");
            waitForPageToLoad();
            waitForElementToBePresent(By.id("permissions-table"));
            
            permission = findPermissionOnCurrentPage(permissionCode);
            if (permission != null) {
                log.info("Found permission {} with larger page size", permissionCode);
                return permission;
            }
            
            // If still not found, check if pagination exists and navigate through pages
            int currentPage = 0;
            int maxPages = getMaxPages();
            
            while (currentPage < maxPages && currentPage < 10) { // Limit to 10 pages max
                driver.get(baseUrl + "?page=" + currentPage + "&size=50");
                waitForPageToLoad();
                waitForElementToBePresent(By.id("permissions-table"));
                
                permission = findPermissionOnCurrentPage(permissionCode);
                if (permission != null) {
                    log.info("Found permission {} on page {}", permissionCode, currentPage);
                    return permission;
                }
                currentPage++;
            }
            
        } catch (Exception e) {
            log.error("Error searching for permission across pages", e);
        }
        
        log.warn("Permission {} not found on any page", permissionCode);
        return null;
    }
    
    private WebElement findPermissionOnCurrentPage(String permissionCode) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement element = shortWait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("permission-code-" + permissionCode)
            ));
            log.debug("Found permission with code: {}", permissionCode);
            return element;
        } catch (TimeoutException e) {
            log.debug("Permission not found with code: {}", permissionCode);
            return null;
        }
    }
    
    private int getMaxPages() {
        try {
            // Look for pagination info to determine max pages
            WebElement paginationInfo = driver.findElement(By.className("pagination-info"));
            String text = paginationInfo.getText();
            // Extract total pages from text like "Page 1 of 5"
            if (text.contains(" of ")) {
                String[] parts = text.split(" of ");
                return Integer.parseInt(parts[1].trim());
            }
        } catch (Exception e) {
            log.debug("Could not determine max pages, defaulting to 5");
        }
        return 5; // Default reasonable limit
    }
    
    
    public void viewPermission(String permissionCode) {
        findPermissionAcrossPages(permissionCode);
        try {
            WebElement viewButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("view-permission-" + permissionCode)));
            scrollToElementAndClick(viewButton);
        } catch (TimeoutException e) {
            throw new RuntimeException("View button for permission " + permissionCode + " not found or not clickable", e);
        }
    }
    
    public void filterByCategory(String category) {
        selectDropdownByValue(categoryFilter, category);
        filterButton.click();
        waitForElementToBeVisible(permissionsTable);
    }
    
    public PermissionFormPage editPermission(String permissionCode) {
        findPermissionAcrossPages(permissionCode);
        try {
            WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("edit-permission-" + permissionCode)));
            scrollToElementAndClick(editButton);
            return new PermissionFormPage(driver, baseUrl);
        } catch (TimeoutException e) {
            throw new RuntimeException("Edit button for permission " + permissionCode + " not found or not clickable", e);
        }
    }
    
    public void deletePermission(String permissionCode) {
        WebElement deleteButton = findDeleteButton(permissionCode);
        if (deleteButton != null) {
            waitForElementToBeClickable(deleteButton);
            deleteButton.click();
            // Handle confirmation dialog
            driver.switchTo().alert().accept();
        } else {
            throw new RuntimeException("Delete button not found for permission: " + permissionCode);
        }
    }
    
    private WebElement findDeleteButton(String permissionCode) {
        try {
            WebElement button = driver.findElement(By.id("delete-permission-" + permissionCode));
            if (button.isDisplayed() && button.isEnabled()) {
                log.info("Found delete button for permission: {}", permissionCode);
                return button;
            }
        } catch (Exception e) {
            log.debug("Delete button not found for permission: {}", permissionCode);
        }
        
        log.error("Could not find delete button for permission: {}", permissionCode);
        return null;
    }
    
    /**
     * Ensures a permission is visible on the current page - fail fast if not found
     */
    public boolean ensurePermissionVisible(String permissionCode) {
        // Use larger page size from the start to maximize visibility
        String currentUrl = driver.getCurrentUrl();
        String newUrl;
        if (currentUrl.contains("?")) {
            newUrl = currentUrl.split("\\?")[0] + "?page=0&size=50";
        } else {
            newUrl = currentUrl + "?page=0&size=50";
        }
        
        driver.get(newUrl);
        waitForPageToLoad();
        waitForElementToBePresent(By.id("permissions-table"));
        
        // Check if permission is visible - fail fast if not
        return isPermissionDisplayed(permissionCode);
    }
    
    /**
     * Opens the page and waits for a specific permission to be visible
     */
    public void openAndWaitForPermission(String permissionCode) {
        openAndWaitForLoad();
        
        // Wait up to 15 seconds for the specific permission to appear
        WebDriverWait permissionWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            permissionWait.until(webDriver -> isPermissionDisplayed(permissionCode));
            log.info("Permission {} is now visible on the page", permissionCode);
        } catch (TimeoutException e) {
            log.warn("Permission {} did not become visible within timeout", permissionCode);
        }
    }
}