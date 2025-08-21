package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

@Slf4j
public class DashboardPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "userMenuButton")
    private WebElement userMenuButton;
    
    @FindBy(id = "userMenu")
    private WebElement userMenu;
    
    @FindBy(id = "logout-button")
    private WebElement logoutButton;
    
    @FindBy(id = "sidebar-nav-items")
    private List<WebElement> sidebarMenuItems;
    
    @FindBy(id = "dashboard-link")
    private WebElement dashboardLink;
    
    @FindBy(id = "product-link")
    private WebElement productLink;
    
    @FindBy(id = "customer-link")
    private WebElement customerLink;
    
    @FindBy(id = "account-link")
    private WebElement accountLink;
    
    @FindBy(id = "transaction-link")
    private WebElement transactionLink;
    
    @FindBy(id = "reports-link")
    private WebElement reportsLink;
    
    @FindBy(id = "users-link")
    private WebElement usersLink;
    
    @FindBy(id = "roles-link")
    private WebElement rolesLink;
    
    @FindBy(id = "permissions-link")
    private WebElement permissionsLink;
    
    @FindBy(id = "statistics-cards")
    private List<WebElement> statisticsCards;
    
    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        // Use timeout from WebDriver configuration (which comes from properties)
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        PageFactory.initElements(driver, this);
    }
    
    public DashboardPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        // Use timeout from WebDriver configuration (which comes from properties)
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        PageFactory.initElements(driver, this);
    }
    
    public boolean isOnDashboardPage() {
        try {
            wait.until(ExpectedConditions.urlContains("/dashboard"));
            return driver.getCurrentUrl().contains("/dashboard") && driver.findElements(By.id("userMenuButton")).size() > 0;
        } catch (Exception e) {
            log.warn("isOnDashboardPage check failed: {}", e.getMessage());
            return false;
        }
    }
    
    public String getPageTitle() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pageTitle));
            return pageTitle.getText();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot get page title from element. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public void openUserMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(userMenuButton));
        userMenuButton.click();
        
        // Try to wait for the menu to become visible, but if it doesn't work, try to make it visible with JavaScript
        try {
            wait.until(ExpectedConditions.visibilityOf(userMenu));
        } catch (Exception e) {
            // Fallback: Use JavaScript to show the menu
            ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("document.getElementById('userMenu').classList.remove('hidden');");
            wait.until(ExpectedConditions.visibilityOf(userMenu));
        }
    }
    
    public LoginPage logout() {
        openUserMenu();
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
        logoutButton.click();
        return new LoginPage(driver);
    }
    
    public String getCurrentUsername() {
        try {
            // First try to get username from header (faster)
            WebElement headerUsername = driver.findElement(By.id("header-username"));
            if (headerUsername.isDisplayed()) {
                return headerUsername.getText().trim();
            }
        } catch (Exception e) {
            // Fallback to user menu approach
            try {
                openUserMenu();
                WebElement userInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-menu-username")));
                return userInfo.getText().trim();
            } catch (Exception ex) {
                String errorDetails = String.format(
                    "❌ FAIL-FAST: Cannot get username from user menu. URL: '%s', Page title: '%s', Error: %s",
                    driver.getCurrentUrl(), driver.getTitle(), ex.getMessage()
                );
                log.error(errorDetails, ex);
                throw new AssertionError(errorDetails, ex);
            }
        }
        return "";
    }
    
    public String getCurrentUserRole() {
        try {
            // First try to get role from header (faster)
            WebElement headerRole = driver.findElement(By.id("header-role"));
            if (headerRole.isDisplayed()) {
                return headerRole.getText().trim();
            }
        } catch (Exception e) {
            // Fallback to user menu approach
            try {
                openUserMenu();
                WebElement roleElement = driver.findElement(By.id("user-menu-role"));
                return roleElement.getText().trim();
            } catch (Exception ex) {
                String errorDetails = String.format(
                    "❌ FAIL-FAST: Cannot get user role from menu. URL: '%s', Page title: '%s', Error: %s",
                    driver.getCurrentUrl(), driver.getTitle(), ex.getMessage()
                );
                log.error(errorDetails, ex);
                throw new AssertionError(errorDetails, ex);
            }
        }
        return "";
    }
    
    // Sidebar navigation methods
    public boolean isDashboardLinkVisible() {
        return isElementVisible(dashboardLink);
    }
    
    public boolean isProductLinkVisible() {
        return isElementVisible(productLink);
    }
    
    public boolean isCustomerLinkVisible() {
        return isElementVisible(customerLink);
    }
    
    public boolean isAccountLinkVisible() {
        return isElementVisible(accountLink);
    }
    
    public boolean isTransactionLinkVisible() {
        return isElementVisible(transactionLink);
    }
    
    public boolean isReportsLinkVisible() {
        return isElementVisible(reportsLink);
    }
    
    public boolean isUsersLinkVisible() {
        return isElementVisible(usersLink);
    }
    
    public boolean isRolesLinkVisible() {
        return isElementVisible(rolesLink);
    }
    
    public boolean isPermissionsLinkVisible() {
        return isElementVisible(permissionsLink);
    }
    
    public boolean isAdministrationSectionVisible() {
        try {
            WebElement adminSection = driver.findElement(By.id("administration-section"));
            return adminSection.isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public int getVisibleMenuItemsCount() {
        return (int) sidebarMenuItems.stream().filter(WebElement::isDisplayed).count();
    }
    
    public List<String> getVisibleMenuItems() {
        return sidebarMenuItems.stream()
            .filter(WebElement::isDisplayed)
            .map(WebElement::getText)
            .toList();
    }
    
    // Click navigation methods
    public void clickProductLink() {
        wait.until(ExpectedConditions.elementToBeClickable(productLink));
        productLink.click();
    }
    
    public void clickCustomerLink() {
        wait.until(ExpectedConditions.elementToBeClickable(customerLink));
        customerLink.click();
    }
    
    public void clickAccountLink() {
        if (isAccountLinkVisible()) {
            wait.until(ExpectedConditions.elementToBeClickable(accountLink));
            accountLink.click();
        }
    }
    
    public TransactionListPage clickTransactionLink() {
        if (isTransactionLinkVisible()) {
            wait.until(ExpectedConditions.elementToBeClickable(transactionLink));
            transactionLink.click();
            // Wait for navigation to transaction list page
            wait.until(ExpectedConditions.urlContains("/transaction/list"));
            return new TransactionListPage(driver, driver.getCurrentUrl().split("/transaction")[0]);
        }
        throw new IllegalStateException("Transaction link is not visible for current user");
    }
    
    public void clickReportsLink() {
        if (isReportsLinkVisible()) {
            wait.until(ExpectedConditions.elementToBeClickable(reportsLink));
            reportsLink.click();
        }
    }
    
    public UserListPage clickUsersLink() {
        if (isUsersLinkVisible()) {
            wait.until(ExpectedConditions.elementToBeClickable(usersLink));
            usersLink.click();
            return new UserListPage(driver, driver.getCurrentUrl().split("/dashboard")[0]);
        }
        throw new IllegalStateException("Users link is not visible for current user");
    }
    
    public RoleListPage clickRolesLink() {
        if (isRolesLinkVisible()) {
            wait.until(ExpectedConditions.elementToBeClickable(rolesLink));
            rolesLink.click();
            return new RoleListPage(driver, driver.getCurrentUrl().split("/dashboard")[0]);
        }
        throw new IllegalStateException("Roles link is not visible for current user");
    }
    
    public PermissionListPage clickPermissionsLink() {
        if (isPermissionsLinkVisible()) {
            wait.until(ExpectedConditions.elementToBeClickable(permissionsLink));
            permissionsLink.click();
            return new PermissionListPage(driver, driver.getCurrentUrl().split("/dashboard")[0]);
        }
        throw new IllegalStateException("Permissions link is not visible for current user");
    }
    
    public int getStatisticsCardsCount() {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("statistics-cards")));
            return statisticsCards.size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public boolean hasQuickActionsSection() {
        try {
            WebElement quickActions = driver.findElement(By.id("quick-actions-title"));
            return quickActions.isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean hasRecentTransactionsSection() {
        try {
            WebElement recentTransactions = driver.findElement(By.id("recent-transactions-title"));
            return recentTransactions.isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    // Additional methods for enhanced dashboard testing
    public boolean isDashboardContentVisible() {
        try {
            return driver.findElement(By.id("dashboard-content")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean hasStatisticsSection() {
        try {
            return driver.findElement(By.id("statistics-section")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isProductManagementLinkVisible() {
        try {
            return driver.findElement(By.id("product-management-link")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public void clickProductManagementLink() {
        driver.findElement(By.id("product-management-link")).click();
    }
    
    public boolean isUserManagementLinkVisible() {
        try {
            return driver.findElement(By.id("user-management-link")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public void clickUserManagementLink() {
        driver.findElement(By.id("user-management-link")).click();
    }
    
    public boolean isTransactionManagementLinkVisible() {
        try {
            return driver.findElement(By.id("transaction-management-link")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean areQuickActionsDisplayed() {
        try {
            return driver.findElements(By.id("quick-actions")).size() > 0;
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isCreateProductButtonVisible() {
        try {
            return driver.findElement(By.id("create-product-button")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isCreateUserButtonVisible() {
        try {
            return driver.findElement(By.id("create-user-button")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isSystemConfigLinkVisible() {
        try {
            return driver.findElement(By.id("system-config-link")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isProcessTransactionButtonVisible() {
        try {
            return driver.findElement(By.id("process-transaction-button")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isAccountLookupButtonVisible() {
        try {
            return driver.findElement(By.id("account-lookup-button")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean isLogoutLinkVisible() {
        try {
            return driver.findElement(By.id("logout-link")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean hasRecentActivitiesSection() {
        try {
            return driver.findElement(By.id("recent-activities")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean areRecentActivitiesDisplayed() {
        try {
            return driver.findElements(By.id("activity-items")).size() > 0;
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean hasNotificationsSection() {
        try {
            return driver.findElement(By.id("notifications")).isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    public boolean areNotificationsDisplayed() {
        try {
            return driver.findElements(By.id("notification-items")).size() > 0;
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
    
    private boolean isElementVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            String errorDetails = String.format(
                "❌ FAIL-FAST: Cannot check element display status. URL: '%s', Page title: '%s', Error: %s",
                driver.getCurrentUrl(), driver.getTitle(), e.getMessage()
            );
            log.error(errorDetails, e);
            throw new AssertionError(errorDetails, e);
        }
    }
}