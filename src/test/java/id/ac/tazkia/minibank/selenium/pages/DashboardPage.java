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
        wait.until(ExpectedConditions.visibilityOf(dashboardContent));
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        log.debug("Dashboard page loaded successfully");
        return this;
    }
    
    /**
     * Check if dashboard page is loaded
     */
    public boolean isDashboardLoaded() {
        try {
            waitForPageLoad();
            return pageTitle.getText().equals("Dashboard") && 
                   driver.getCurrentUrl().contains("/dashboard");
        } catch (Exception e) {
            log.debug("Dashboard not loaded: {}", e.getMessage());
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
        log.info("Verifying admin role elements");
        
        boolean hasProductManagement = isProductManagementVisible();
        boolean hasUserManagement = isUserManagementVisible();
        boolean hasCreateUser = isCreateUserButtonVisible();
        boolean hasSystemConfig = isSystemConfigLinkVisible();
        
        log.debug("Admin elements - Product: {}, User: {}, CreateUser: {}, SystemConfig: {}", 
                hasProductManagement, hasUserManagement, hasCreateUser, hasSystemConfig);
        
        return hasProductManagement && hasUserManagement && hasCreateUser && hasSystemConfig;
    }
    
    /**
     * Verify role-specific elements for Manager user
     */
    public boolean verifyManagerRoleElements() {
        waitForPageLoad();
        log.info("Verifying manager role elements");
        
        boolean hasProductManagement = isProductManagementVisible();
        boolean hasUserManagement = isUserManagementVisible();
        boolean hasCreateProduct = isCreateProductButtonVisible();
        
        log.debug("Manager elements - Product: {}, User: {}, CreateProduct: {}", 
                hasProductManagement, hasUserManagement, hasCreateProduct);
        
        return hasProductManagement && hasUserManagement && hasCreateProduct;
    }
    
    /**
     * Verify role-specific elements for Customer Service user
     */
    public boolean verifyCustomerServiceRoleElements() {
        waitForPageLoad();
        log.info("Verifying customer service role elements");
        
        // Customer Service should see product and account management capabilities
        // but NOT transaction processing (that's for Tellers)
        boolean hasProductView = isProductManagementVisible();
        boolean cannotProcessTransaction = !isProcessTransactionButtonVisible();
        
        log.debug("CS elements - ProductView: {}, NoTransactionButton: {}", 
                hasProductView, cannotProcessTransaction);
        
        // CS should be able to view products but not process transactions
        return hasProductView && cannotProcessTransaction;
    }
    
    /**
     * Verify role-specific elements for Teller user
     */
    public boolean verifyTellerRoleElements() {
        waitForPageLoad();
        log.info("Verifying teller role elements");
        
        boolean hasProcessTransaction = isProcessTransactionButtonVisible();
        boolean hasAccountLookup = isAccountLookupButtonVisible();
        
        log.debug("Teller elements - ProcessTransaction: {}, AccountLookup: {}", 
                hasProcessTransaction, hasAccountLookup);
        
        return hasProcessTransaction && hasAccountLookup;
    }
    
    /**
     * Click product management link
     */
    public DashboardPage clickProductManagement() {
        wait.until(ExpectedConditions.elementToBeClickable(productManagementLink));
        productManagementLink.click();
        log.debug("Clicked product management link");
        return this;
    }
    
    /**
     * Click user management link
     */
    public DashboardPage clickUserManagement() {
        wait.until(ExpectedConditions.elementToBeClickable(userManagementLink));
        userManagementLink.click();
        log.debug("Clicked user management link");
        return this;
    }
}