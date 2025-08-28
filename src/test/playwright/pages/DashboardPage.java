package id.ac.tazkia.minibank.playwright.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DashboardPage {
    
    private final Page page;
    
    // Dashboard elements using IDs exclusively
    private final Locator pageTitle;
    private final Locator welcomeMessage;
    private final Locator userInfo;
    private final Locator navigationMenu;
    private final Locator logoutButton;
    private final Locator dashboardContent;
    
    // Navigation menu items
    private final Locator productManagementLink;
    private final Locator customerManagementLink;
    private final Locator accountManagementLink;
    private final Locator transactionLink;
    private final Locator userManagementLink;
    private final Locator branchManagementLink;
    private final Locator rbacLink;
    
    public DashboardPage(Page page) {
        this.page = page;
        
        // Initialize locators using IDs exclusively
        this.pageTitle = page.locator("#page-title");
        this.welcomeMessage = page.locator("#welcome-message");
        this.userInfo = page.locator("#user-info");
        this.navigationMenu = page.locator("#navigation-menu");
        this.logoutButton = page.locator("#logout-btn");
        this.dashboardContent = page.locator("#dashboard-content");
        
        // Navigation links
        this.productManagementLink = page.locator("#nav-product-management");
        this.customerManagementLink = page.locator("#nav-customer-management");
        this.accountManagementLink = page.locator("#nav-account-management");
        this.transactionLink = page.locator("#nav-transaction");
        this.userManagementLink = page.locator("#nav-user-management");
        this.branchManagementLink = page.locator("#nav-branch-management");
        this.rbacLink = page.locator("#nav-rbac");
    }
    
    /**
     * Wait for dashboard page to load completely
     */
    public DashboardPage waitForPageLoad() {
        pageTitle.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        dashboardContent.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        log.debug("Dashboard page loaded successfully");
        return this;
    }
    
    /**
     * Check if dashboard is loaded
     */
    public boolean isDashboardLoaded() {
        try {
            waitForPageLoad();
            return page.url().contains("/dashboard") &&
                   pageTitle.textContent().equals("Dashboard");
        } catch (Exception e) {
            log.debug("Dashboard not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Navigate to dashboard (if not already there)
     */
    public DashboardPage navigateTo(String baseUrl) {
        page.navigate(baseUrl + "/dashboard");
        waitForPageLoad();
        return this;
    }
    
    /**
     * Get welcome message text
     */
    public String getWelcomeMessage() {
        try {
            if (welcomeMessage.isVisible()) {
                return welcomeMessage.textContent();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Get user info text
     */
    public String getUserInfo() {
        try {
            if (userInfo.isVisible()) {
                return userInfo.textContent();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Check if navigation menu is visible
     */
    public boolean isNavigationMenuVisible() {
        try {
            return navigationMenu.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Navigate to Product Management
     */
    public ProductManagementPage navigateToProductManagement() {
        if (productManagementLink.isVisible()) {
            productManagementLink.click();
            log.debug("Navigated to Product Management");
        }
        return new ProductManagementPage(page);
    }
    
    /**
     * Navigate to Customer Management
     */
    public CustomerManagementPage navigateToCustomerManagement() {
        if (customerManagementLink.isVisible()) {
            customerManagementLink.click();
            log.debug("Navigated to Customer Management");
        }
        return new CustomerManagementPage(page);
    }
    
    /**
     * Navigate to Account Management
     */
    public AccountManagementPage navigateToAccountManagement() {
        if (accountManagementLink.isVisible()) {
            accountManagementLink.click();
            log.debug("Navigated to Account Management");
        }
        return new AccountManagementPage(page);
    }
    
    /**
     * Navigate to Transaction
     */
    public TransactionPage navigateToTransaction() {
        if (transactionLink.isVisible()) {
            transactionLink.click();
            log.debug("Navigated to Transaction");
        }
        return new TransactionPage(page);
    }
    
    /**
     * Navigate to User Management
     */
    public UserManagementPage navigateToUserManagement() {
        if (userManagementLink.isVisible()) {
            userManagementLink.click();
            log.debug("Navigated to User Management");
        }
        return new UserManagementPage(page);
    }
    
    /**
     * Navigate to Branch Management
     */
    public BranchManagementPage navigateToBranchManagement() {
        if (branchManagementLink.isVisible()) {
            branchManagementLink.click();
            log.debug("Navigated to Branch Management");
        }
        return new BranchManagementPage(page);
    }
    
    /**
     * Navigate to RBAC
     */
    public RBACPage navigateToRBAC() {
        if (rbacLink.isVisible()) {
            rbacLink.click();
            log.debug("Navigated to RBAC");
        }
        return new RBACPage(page);
    }
    
    /**
     * Perform logout
     */
    public LoginPage logout() {
        if (logoutButton.isVisible()) {
            logoutButton.click();
            log.debug("Logged out successfully");
        }
        return new LoginPage(page);
    }
    
    /**
     * Check if logout button is visible
     */
    public boolean isLogoutButtonVisible() {
        try {
            return logoutButton.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get page title
     */
    public String getPageTitle() {
        try {
            if (pageTitle.isVisible()) {
                return pageTitle.textContent();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Check if specific navigation link is visible
     */
    public boolean isNavigationLinkVisible(String linkType) {
        try {
            switch (linkType.toLowerCase()) {
                case "product":
                    return productManagementLink.isVisible();
                case "customer":
                    return customerManagementLink.isVisible();
                case "account":
                    return accountManagementLink.isVisible();
                case "transaction":
                    return transactionLink.isVisible();
                case "user":
                    return userManagementLink.isVisible();
                case "branch":
                    return branchManagementLink.isVisible();
                case "rbac":
                    return rbacLink.isVisible();
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}