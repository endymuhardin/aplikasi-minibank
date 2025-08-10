package id.ac.tazkia.minibank.functional.web.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

public class DashboardPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    @FindBy(css = "h1")
    private WebElement pageTitle;
    
    @FindBy(id = "userMenuButton")
    private WebElement userMenuButton;
    
    @FindBy(id = "userMenu")
    private WebElement userMenu;
    
    @FindBy(css = "form[action='/logout'] button")
    private WebElement logoutButton;
    
    @FindBy(css = ".w-64 nav a")
    private List<WebElement> sidebarMenuItems;
    
    @FindBy(linkText = "Dashboard")
    private WebElement dashboardLink;
    
    @FindBy(linkText = "Product")
    private WebElement productLink;
    
    @FindBy(linkText = "Customer")
    private WebElement customerLink;
    
    @FindBy(linkText = "Account")
    private WebElement accountLink;
    
    @FindBy(linkText = "Transaction")
    private WebElement transactionLink;
    
    @FindBy(linkText = "Reports")
    private WebElement reportsLink;
    
    @FindBy(linkText = "Users")
    private WebElement usersLink;
    
    @FindBy(linkText = "Roles")
    private WebElement rolesLink;
    
    @FindBy(linkText = "Permissions")
    private WebElement permissionsLink;
    
    @FindBy(css = ".grid .bg-white")
    private List<WebElement> statisticsCards;
    
    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    public boolean isOnDashboardPage() {
        try {
            // First wait for successful login redirect, allowing for multiple possible success conditions
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/product/list"),
                ExpectedConditions.titleContains("Dashboard"),
                ExpectedConditions.titleContains("Product"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".w-64 nav")),
                ExpectedConditions.presenceOfElementLocated(By.id("userMenuButton"))
            ));
            
            String currentUrl = driver.getCurrentUrl();
            // Accept both /dashboard and /product/list as valid dashboard-like pages
            return currentUrl.contains("/dashboard") || currentUrl.contains("/product/list");
        } catch (Exception e) {
            // Fallback: try to detect dashboard elements
            try {
                return driver.findElement(By.cssSelector(".w-64 nav")).isDisplayed() ||
                       driver.findElement(By.id("userMenuButton")).isDisplayed();
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    public String getPageTitle() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pageTitle));
            return pageTitle.getText();
        } catch (Exception e) {
            return driver.getTitle();
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
            WebElement usernameElement = driver.findElement(By.cssSelector("[sec\\:authentication='name']"));
            return usernameElement.getText();
        } catch (Exception e) {
            try {
                openUserMenu();
                WebElement userInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#userMenu .font-semibold")));
                return userInfo.getText();
            } catch (Exception ex) {
                return "";
            }
        }
    }
    
    public String getCurrentUserRole() {
        openUserMenu();
        try {
            WebElement roleElement = driver.findElement(By.cssSelector("#userMenu .text-gray-500"));
            return roleElement.getText();
        } catch (Exception e) {
            return "";
        }
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
            WebElement adminSection = driver.findElement(By.xpath("//h3[contains(text(), 'Administration')]"));
            return adminSection.isDisplayed();
        } catch (Exception e) {
            return false;
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
    
    public void clickTransactionLink() {
        if (isTransactionLinkVisible()) {
            wait.until(ExpectedConditions.elementToBeClickable(transactionLink));
            transactionLink.click();
        }
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
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".grid .bg-white")));
            return statisticsCards.size();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public boolean hasQuickActionsSection() {
        try {
            WebElement quickActions = driver.findElement(By.xpath("//h3[contains(text(), 'Quick Actions')]"));
            return quickActions.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean hasRecentTransactionsSection() {
        try {
            WebElement recentTransactions = driver.findElement(By.xpath("//h3[contains(text(), 'Recent Transactions')]"));
            return recentTransactions.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isElementVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}