package id.ac.tazkia.minibank.functional.pages;

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
    private final Locator userMenuButton;
    private final Locator userMenu;
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
        this.navigationMenu = page.locator("#sidebar-nav");
        this.logoutButton = page.locator("#logout-button");
        this.userMenuButton = page.locator("#userMenuButton");
        this.userMenu = page.locator("#userMenu");
        this.dashboardContent = page.locator("#dashboard-content");
        
        // Navigation links - using actual IDs from main.html
        this.productManagementLink = page.locator("#product-link");
        this.customerManagementLink = page.locator("#customer-link");
        this.accountManagementLink = page.locator("#account-link");
        this.transactionLink = page.locator("#transaction-link");
        this.userManagementLink = page.locator("#users-link");
        this.branchManagementLink = page.locator("#roles-link"); // No specific branch link, using roles
        this.rbacLink = page.locator("#permissions-link");
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
        try {
            // Open user menu if it's not already open
            if (!logoutButton.isVisible()) {
                userMenuButton.click();
                // Wait for logout button to appear in the menu
                logoutButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(1000));
            }
            
            // Click logout button if visible
            if (logoutButton.isVisible()) {
                logoutButton.click();
                log.debug("Clicked logout button");
                
                // Wait for navigation to login page with logout parameter
                page.waitForURL("**/login?logout=true", new Page.WaitForURLOptions().setTimeout(10000));
                log.debug("Successfully navigated to login page after logout");
            } else {
                log.error("Logout button not visible even after opening menu");
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
        }
        return new LoginPage(page);
    }
    
    /**
     * Check if logout button is visible (opens user menu if needed)
     */
    public boolean isLogoutButtonVisible() {
        try {
            // First check if user menu button is visible (user is logged in)
            if (!userMenuButton.isVisible()) {
                return false;
            }
            
            // If logout button is already visible (menu is open), return true
            if (logoutButton.isVisible()) {
                return true;
            }
            
            // Open the user menu to check if logout button becomes visible
            userMenuButton.click();
            
            // Wait for menu to open and check if logout button is visible
            try {
                logoutButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(500));
            } catch (Exception e) {
                // Menu might not have opened
            }
            boolean isVisible = logoutButton.isVisible();
            
            // Close the menu by clicking outside or pressing escape
            if (isVisible) {
                page.keyboard().press("Escape");
            }
            
            return isVisible;
        } catch (Exception e) {
            log.debug("Could not check logout button visibility: {}", e.getMessage());
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