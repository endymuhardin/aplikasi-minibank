package id.ac.tazkia.minibank.functional.documentation;

import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import id.ac.tazkia.minibank.functional.pages.CustomerManagementPage;
import id.ac.tazkia.minibank.functional.pages.LoginPage;
import id.ac.tazkia.minibank.functional.pages.DashboardPage;
import id.ac.tazkia.minibank.functional.pages.AccountManagementPage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Documentation-specific Playwright test that generates screenshots and videos
 * for creating an Indonesian user manual/tutorial on personal customer account opening
 * for the CS (Customer Service) role.
 * 
 * This test is designed to capture high-quality screenshots and videos with
 * human-readable filenames for use in documentation generation.
 * 
 * Run with:
 * mvn test -Dtest=PersonalCustomerAccountOpeningTutorialTest -Dplaywright.headless=false -Dplaywright.slowmo=2000 -Dplaywright.record=true
 */
@Slf4j
@Tag("documentation")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tutorial: Personal Customer Account Opening for CS Role")
class PersonalCustomerAccountOpeningTutorialTest extends BasePlaywrightTest {
    
    private CustomerManagementPage customerPage;
    private AccountManagementPage accountPage;
    private DashboardPage dashboardPage;
    
    @BeforeEach
    void setUp() {
        customerPage = new CustomerManagementPage(page);
        accountPage = new AccountManagementPage(page);
    }
    
    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("Tutorial Step 1: Login as Customer Service")
    void step01_LoginAsCustomerService() {
        log.info("📚 Tutorial Step 1: Login as Customer Service (CS)");
        
        // Navigate to login page and wait for it to load
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        loginPage.waitForPageLoad();
        takeScreenshot("01_login_page_loaded");
        
        // Fill username field and take screenshot
        loginPage.fillUsername("cs1");
        takeScreenshot("02_username_filled");
        
        // Fill password field and take screenshot
        loginPage.fillPassword("minibank123");
        takeScreenshot("03_password_filled");
        
        // Take screenshot before login
        takeScreenshot("04_ready_to_login");
        
        // Perform login
        dashboardPage = loginPage.loginWith("cs1", "minibank123");
        
        // Wait for dashboard to load and verify
        dashboardPage.waitForPageLoad();
        assertTrue(dashboardPage.isDashboardLoaded(), "Should be logged in successfully");
        takeScreenshot("05_dashboard_after_login");
        
        log.info("✅ Step 1 Complete: Successfully logged in as CS");
    }
    
    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("Tutorial Step 2: Navigate to Customer Management")
    void step02_NavigateToCustomerManagement() {
        log.info("📚 Tutorial Step 2: Navigate to Customer Management");
        
        // Login first and wait for dashboard
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        dashboardPage = loginPage.loginWith("cs1", "minibank123");
        dashboardPage.waitForPageLoad();
        
        // Take dashboard screenshot highlighting navigation
        takeScreenshot("06_dashboard_navigation_menu");
        
        // Navigate to customer list and wait for page load
        customerPage.navigateToList(baseUrl);
        customerPage.waitForListPageLoad();
        takeScreenshot("07_customer_list_page");
        
        // Verify customer list elements are visible
        assertTrue(customerPage.isOnListPage(), "Should be on customer list page");
        takeElementScreenshot("#customer-table", "08_customer_table_view");
        
        log.info("✅ Step 2 Complete: Navigated to customer management");
    }
    
    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("Tutorial Step 3: Start Creating New Personal Customer")
    void step03_StartCreatingNewPersonalCustomer() {
        log.info("📚 Tutorial Step 3: Start creating new personal customer");
        
        // Setup: Login and navigate to customer list
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        dashboardPage = loginPage.loginWith("cs1", "minibank123");
        dashboardPage.waitForPageLoad();
        
        customerPage.navigateToList(baseUrl);
        customerPage.waitForListPageLoad();
        
        // Click "Add Customer" button
        takeElementScreenshot("#create-customer-btn", "09_add_customer_button");
        customerPage.clickAddCustomer();
        page.waitForLoadState();
        
        // Take screenshot of customer type selection page
        takeScreenshot("10_customer_type_selection_page");
        
        // Highlight personal customer option
        takeElementScreenshot("#create-personal-customer-link", "11_personal_customer_option");
        
        // Select personal customer type
        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState();
        customerPage.waitForFormLoad();
        
        // Take screenshot of personal customer form
        takeScreenshot("12_personal_customer_form_loaded");
        
        log.info("✅ Step 3 Complete: Personal customer form loaded");
    }
    
    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("Tutorial Step 4: Fill Personal Information")
    void step04_FillPersonalInformation() {
        log.info("📚 Tutorial Step 4: Fill personal information");
        
        // Setup: Navigate to personal customer form
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        dashboardPage = loginPage.loginWith("cs1", "minibank123");
        dashboardPage.waitForPageLoad();
        
        customerPage.navigateToAddCustomer(baseUrl);
        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState();
        customerPage.waitForFormLoad();
        
        // Take screenshot of empty form
        takeScreenshot("13_empty_personal_form");
        
        // Fill personal information using the page object methods to avoid direct locators
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String uniqueEmail = "ahmad.tutorial" + timestamp + "@email.com";
        String uniquePhone = "08123" + timestamp;
        String uniqueIdNumber = "3271081234567" + timestamp.substring(0, 3);
        
        // Fill the form using page object method which handles waiting
        customerPage.fillPersonalCustomerForm(
            "Ahmad Tutorial", uniqueIdNumber, "KTP", "1990-01-15", "Jakarta",
            "MALE", "Siti Ahmad", uniqueEmail, uniquePhone,
            "Jl. Tutorial No. 123", "Jakarta", "DKI Jakarta", "12345"
        );
        
        // Take screenshots of filled sections
        takeElementScreenshot("#firstName", "14_first_name_filled");
        takeElementScreenshot("#identityNumber", "16_identity_info_filled");
        takeElementScreenshot("#dateOfBirth", "17_birth_info_filled");
        takeElementScreenshot("#motherName", "18_mother_name_filled");
        
        log.info("✅ Step 4 Complete: Personal information filled");
    }
    
    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("Tutorial Step 5: Fill Contact Information")
    void step05_FillContactInformation() {
        log.info("📚 Tutorial Step 5: Fill contact information");
        
        // Setup: Navigate to form
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        dashboardPage = loginPage.loginWith("cs1", "minibank123");
        dashboardPage.waitForPageLoad();
        
        customerPage.navigateToAddCustomer(baseUrl);
        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState();
        customerPage.waitForFormLoad();
        
        // Fill complete form with unique data
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String uniqueEmail = "ahmad.tutorial" + timestamp + "@email.com";
        String uniquePhone = "08123" + timestamp;
        String uniqueIdNumber = "3271081234567" + timestamp.substring(0, 3);
        
        customerPage.fillPersonalCustomerForm(
            "Ahmad Tutorial", uniqueIdNumber, "KTP", "1990-01-15", "Jakarta",
            "MALE", "Siti Ahmad", uniqueEmail, uniquePhone,
            "Jl. Tutorial No. 123", "Jakarta", "DKI Jakarta", "12345"
        );
        
        // Take screenshots of contact sections
        takeElementScreenshot("#email", "20_email_field_filled");
        takeElementScreenshot("#phoneNumber", "21_phone_field_filled");
        takeElementScreenshot("#address", "22_address_info_filled");
        takeScreenshot("23_complete_form_filled");
        
        log.info("✅ Step 5 Complete: Contact information filled");
    }
    
    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("Tutorial Step 6: Save Customer and Verify Success")
    void step06_SaveCustomerAndVerifySuccess() {
        log.info("📚 Tutorial Step 6: Save customer and verify success");
        
        // Setup: Fill complete form
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        dashboardPage = loginPage.loginWith("cs1", "minibank123");
        dashboardPage.waitForPageLoad();
        
        customerPage.navigateToAddCustomer(baseUrl);
        customerPage.selectCustomerType("PERSONAL");
        page.waitForLoadState();
        customerPage.waitForFormLoad();
        
        // Fill form with unique data
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String uniqueEmail = "ahmad.tutorial" + timestamp + "@email.com";
        String uniquePhone = "08123" + timestamp;
        String uniqueIdNumber = "3271081234567" + timestamp.substring(0, 3);
        
        customerPage.fillPersonalCustomerForm(
            "Ahmad Tutorial", uniqueIdNumber, "KTP", "1990-01-15", "Jakarta",
            "MALE", "Siti Ahmad", uniqueEmail, uniquePhone,
            "Jl. Tutorial No. 123", "Jakarta", "DKI Jakarta", "12345"
        );
        
        // Take screenshot before saving
        takeElementScreenshot("#submit-button", "24_save_button_ready");
        
        // Click save button and wait
        customerPage.clickSave();
        page.waitForLoadState();
        
        // Take screenshot after save
        takeScreenshot("25_after_save_customer");
        
        // Verify success using the page object method
        assertTrue(customerPage.isOperationSuccessful(),
                "Customer should be created successfully");
        
        // Take additional screenshot if on view page
        if (customerPage.isOnViewPage()) {
            takeScreenshot("26_customer_created_view_page");
        }
        
        log.info("✅ Step 6 Complete: Customer successfully created");
    }
    
    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("Tutorial Step 7: View Customer List and Summary")
    void step07_ViewCustomerListAndSummary() {
        log.info("📚 Tutorial Step 7: View customer list and tutorial summary");
        
        // Setup: Login 
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        dashboardPage = loginPage.loginWith("cs1", "minibank123");
        dashboardPage.waitForPageLoad();
        
        // Navigate back to customer list to see our created customer
        customerPage.navigateToList(baseUrl);
        customerPage.waitForListPageLoad();
        
        // Take screenshot of customer list showing the tutorial workflow complete
        takeScreenshot("27_customer_list_with_created_customer");
        
        // Take screenshot of the complete customer table
        takeElementScreenshot("#customer-table", "28_complete_customer_table");
        
        log.info("✅ Step 7 Complete: Customer creation tutorial completed successfully");
    }
    
    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("Tutorial Step 8: Tutorial Complete Summary")
    void step08_TutorialCompleteSummary() {
        log.info("📚 Tutorial Step 8: Tutorial completion summary");
        
        // Setup: Login and navigate to dashboard for final summary
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigateTo(baseUrl);
        dashboardPage = loginPage.loginWith("cs1", "minibank123");
        dashboardPage.waitForPageLoad();
        
        // Take final screenshot of dashboard showing successful completion
        takeScreenshot("29_tutorial_complete_dashboard");
        
        // Navigate to customer list one more time to show the complete workflow
        customerPage.navigateToList(baseUrl);
        customerPage.waitForListPageLoad();
        takeScreenshot("30_tutorial_complete_customer_list");
        
        log.info("✅ Step 8 Complete: Personal customer account opening tutorial complete!");
        log.info("🎉 Tutorial Summary: Successfully demonstrated complete workflow for CS staff");
    }
}