package id.ac.tazkia.minibank.selenium.pages;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DashboardPage {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    @FindBy(id = "page-title")
    private WebElement pageTitle;
    
    @FindBy(id = "dashboard-content")
    private WebElement dashboardContent;
    
    @FindBy(id = "statistics-section")
    private WebElement statisticsSection;
    
    @FindBy(id = "statistics-cards")
    private WebElement statisticsCards;
    
    @FindBy(id = "product-management-link")
    private WebElement productManagementLink;
    
    @FindBy(id = "user-management-link")
    private WebElement userManagementLink;
    
    @FindBy(id = "transaction-management-link")
    private WebElement transactionManagementLink;
    
    @FindBy(id = "recent-transactions-title")
    private WebElement recentTransactionsTitle;
    
    @FindBy(id = "quick-actions-title")
    private WebElement quickActionsTitle;
    
    @FindBy(id = "quick-actions")
    private WebElement quickActionsSection;
    
    // Role-specific elements
    @FindBy(id = "create-product-button")
    private WebElement createProductButton;
    
    @FindBy(id = "create-user-button")
    private WebElement createUserButton;
    
    @FindBy(id = "process-transaction-button")
    private WebElement processTransactionButton;
    
    @FindBy(id = "account-lookup-button")
    private WebElement accountLookupButton;
    
    @FindBy(id = "system-config-link")
    private WebElement systemConfigLink;
    
    @FindBy(id = "recent-activities")
    private WebElement recentActivitiesSection;
    
    @FindBy(id = "notifications")
    private WebElement notificationsSection;
    
    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Wait for dashboard page to load completely
     */
    public DashboardPage waitForPageLoad() {
        log.debug("⏳ WAITING → Dashboard page elements to load...");
        wait.until(ExpectedConditions.visibilityOf(dashboardContent));
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        log.info("✅ DASHBOARD LOADED - Title: {} | URL: {}", 
                pageTitle.getText(), driver.getCurrentUrl());
        return this;
    }
    
    /**
     * Check if dashboard page is loaded
     */
    public boolean isDashboardLoaded() {
        try {
            waitForPageLoad();
            String title = pageTitle.getText();
            String url = driver.getCurrentUrl();
            boolean loaded = title.equals("Dashboard") && url.contains("/dashboard");
            
            if (loaded) {
                log.info("✅ DASHBOARD VERIFICATION SUCCESS - Title: '{}' | URL contains /dashboard", title);
            } else {
                log.warn("❌ DASHBOARD VERIFICATION FAILED - Title: '{}' | URL: {}", title, url);
            }
            
            return loaded;
        } catch (Exception e) {
            log.warn("💥 DASHBOARD CHECK ERROR - {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get page title text
     */
    public String getPageTitle() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return pageTitle.getText();
    }
    
    /**
     * Check if statistics section is visible
     */
    public boolean isStatisticsSectionVisible() {
        try {
            return statisticsSection.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if product management link is visible (Admin/Manager role)
     */
    public boolean isProductManagementVisible() {
        try {
            return productManagementLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if user management link is visible (Admin/Manager role)
     */
    public boolean isUserManagementVisible() {
        try {
            return userManagementLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if transaction management link is visible (Admin/Manager/Teller role)
     */
    public boolean isTransactionManagementVisible() {
        try {
            return transactionManagementLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if create product button is visible (Admin/Manager role)
     */
    public boolean isCreateProductButtonVisible() {
        try {
            return createProductButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if create user button is visible (Admin role)
     */
    public boolean isCreateUserButtonVisible() {
        try {
            return createUserButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if process transaction button is visible (Teller/CS role)
     */
    public boolean isProcessTransactionButtonVisible() {
        try {
            return processTransactionButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if account lookup button is visible (Teller role)
     */
    public boolean isAccountLookupButtonVisible() {
        try {
            return accountLookupButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if system config link is visible (Admin role)
     */
    public boolean isSystemConfigLinkVisible() {
        try {
            return systemConfigLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verify role-specific elements for Admin user
     */
    public boolean verifyAdminRoleElements() {
        waitForPageLoad();
        log.info("🔍 ROLE CHECK → Verifying ADMIN role permissions");
        
        boolean hasProductManagement = isProductManagementVisible();
        boolean hasUserManagement = isUserManagementVisible();
        boolean hasCreateUser = isCreateUserButtonVisible();
        boolean hasSystemConfig = isSystemConfigLinkVisible();
        
        log.info("📋 ADMIN PERMISSIONS - Product: {} | User: {} | CreateUser: {} | SystemConfig: {}", 
                hasProductManagement ? "✅" : "❌", 
                hasUserManagement ? "✅" : "❌",
                hasCreateUser ? "✅" : "❌", 
                hasSystemConfig ? "✅" : "❌");
        
        boolean result = hasProductManagement && hasUserManagement && hasCreateUser && hasSystemConfig;
        log.info("🎯 ADMIN VERIFICATION {} - All required elements: {}", 
                result ? "PASSED" : "FAILED", result ? "✅" : "❌");
        
        return result;
    }
    
    /**
     * Verify role-specific elements for Manager user
     */
    public boolean verifyManagerRoleElements() {
        waitForPageLoad();
        log.info("🔍 ROLE CHECK → Verifying MANAGER role permissions");
        
        boolean hasProductManagement = isProductManagementVisible();
        boolean hasUserManagement = isUserManagementVisible();
        boolean hasCreateProduct = isCreateProductButtonVisible();
        
        log.info("📋 MANAGER PERMISSIONS - Product: {} | User: {} | CreateProduct: {}", 
                hasProductManagement ? "✅" : "❌", 
                hasUserManagement ? "✅" : "❌",
                hasCreateProduct ? "✅" : "❌");
        
        boolean result = hasProductManagement && hasUserManagement && hasCreateProduct;
        log.info("🎯 MANAGER VERIFICATION {} - All required elements: {}", 
                result ? "PASSED" : "FAILED", result ? "✅" : "❌");
        
        return result;
    }
    
    /**
     * Verify role-specific elements for Customer Service user
     */
    public boolean verifyCustomerServiceRoleElements() {
        waitForPageLoad();
        log.info("🔍 ROLE CHECK → Verifying CUSTOMER SERVICE role permissions");
        
        // Customer Service should see product and account management capabilities
        // but NOT transaction processing (that's for Tellers)
        boolean hasProductView = isProductManagementVisible();
        boolean cannotProcessTransaction = !isProcessTransactionButtonVisible();
        
        log.info("📋 CS PERMISSIONS - ProductView: {} | NoTransactionButton: {} (correct)", 
                hasProductView ? "✅" : "❌",
                cannotProcessTransaction ? "✅" : "❌");
        
        boolean result = hasProductView && cannotProcessTransaction;
        log.info("🎯 CS VERIFICATION {} - Can view products but cannot process transactions: {}", 
                result ? "PASSED" : "FAILED", result ? "✅" : "❌");
        
        return result;
    }
    
    /**
     * Verify role-specific elements for Teller user
     */
    public boolean verifyTellerRoleElements() {
        waitForPageLoad();
        log.info("🔍 ROLE CHECK → Verifying TELLER role permissions");
        
        boolean hasProcessTransaction = isProcessTransactionButtonVisible();
        boolean hasAccountLookup = isAccountLookupButtonVisible();
        
        log.info("📋 TELLER PERMISSIONS - ProcessTransaction: {} | AccountLookup: {}", 
                hasProcessTransaction ? "✅" : "❌",
                hasAccountLookup ? "✅" : "❌");
        
        boolean result = hasProcessTransaction && hasAccountLookup;
        log.info("🎯 TELLER VERIFICATION {} - Transaction processing capabilities: {}", 
                result ? "PASSED" : "FAILED", result ? "✅" : "❌");
        
        return result;
    }
    
    /**
     * Click product management link
     */
    public DashboardPage clickProductManagement() {
        log.info("🖱️ ACTION → Clicking Product Management link");
        wait.until(ExpectedConditions.elementToBeClickable(productManagementLink));
        productManagementLink.click();
        log.info("✅ NAVIGATION → Product Management link clicked | New URL: {}", driver.getCurrentUrl());
        return this;
    }
    
    /**
     * Click user management link
     */
    public DashboardPage clickUserManagement() {
        log.info("🖱️ ACTION → Clicking User Management link");
        wait.until(ExpectedConditions.elementToBeClickable(userManagementLink));
        userManagementLink.click();
        log.info("✅ NAVIGATION → User Management link clicked | New URL: {}", driver.getCurrentUrl());
        return this;
    }
}