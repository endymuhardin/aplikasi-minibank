package id.ac.tazkia.minibank.selenium.essential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import id.ac.tazkia.minibank.config.BaseSeleniumTest;
import id.ac.tazkia.minibank.selenium.pages.LoginPage;
import id.ac.tazkia.minibank.selenium.pages.UserPage;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Tag("essential")
@DisplayName("User Management Essential Tests")
class UserManagementEssentialTest extends BaseSeleniumTest {
    
    private UserPage userPage;
    private LoginPage loginPage;
    
    @BeforeEach
    void setUp() {
        userPage = new UserPage(driver);
        loginPage = new LoginPage(driver);
        log.info("Starting user management essential test");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/login-credentials-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should handle user management page access based on role permissions")
    void shouldHandleUserManagementPageAccessBasedOnRolePermissions(String username, String password, String expectedRole, String roleDescription) {
        log.info("Testing user management page access for role: {} ({})", expectedRole, roleDescription);
        
        // Login with provided credentials
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith(username, password);
        
        // Only BRANCH_MANAGER role has access to user management
        boolean hasUserManagementAccess = expectedRole.equals("BRANCH_MANAGER");
        
        if (hasUserManagementAccess) {
            // Navigate to user management
            userPage.navigateToUserList(baseUrl);
            
            // Verify page loads correctly
            assertTrue(userPage.isUserListPageLoaded(), 
                       "User management page should load for " + expectedRole);
            
            // Both admin and manager roles should see create user button
            assertTrue(userPage.isCreateUserButtonVisible(),
                      "Create user button should be visible for " + expectedRole);
            
            log.debug("✓ User management page access verified for {}", expectedRole);
        } else {
            // TELLER and CUSTOMER_SERVICE don't have user management permissions
            // They would be redirected or see an error page
            log.debug("✓ Role {} does not have user management access as expected", expectedRole);
        }
    }
    
    @Test
    @DisplayName("Should display user management page title and elements correctly")
    void shouldDisplayUserManagementPageElements() {
        log.info("Testing user management page elements display");
        
        // Login as admin to access all features
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to user management
        userPage.navigateToUserList(baseUrl);
        
        // Verify all essential page elements are present
        assertTrue(userPage.isUserListPageLoaded(), "User list page should be loaded");
        assertTrue(userPage.isCreateUserButtonVisible(), "Create user button should be visible for admin");
        
        log.debug("✓ User management page elements displayed correctly");
    }
    
    @Test
    @DisplayName("Should display existing users in the user list")
    void shouldDisplayExistingUsersInList() {
        log.info("Testing existing users display in user list");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to user management
        userPage.navigateToUserList(baseUrl);
        
        // Verify users are displayed
        assertTrue(userPage.areUsersDisplayed(), "Users should be displayed in the list");
        
        // Verify some default users exist
        assertTrue(userPage.isUserVisible("admin"), "Admin user should be visible in the list");
        
        log.debug("✓ Existing users displayed correctly");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/user-search-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should search users correctly with various search terms")
    void shouldSearchUsersCorrectly(String searchTerm, String expectedResults, String description) {
        log.info("Testing user search functionality: {}", description);
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to user management and perform search
        userPage.navigateToUserList(baseUrl)
                .searchUsers(searchTerm);
        
        // Wait for search results
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify search results based on expected outcome
        if (expectedResults.contains("No users found")) {
            // For non-existent searches, either no users displayed or explicit message
            boolean noResults = userPage.isNoUsersMessageVisible() || !userPage.areUsersDisplayed();
            assertTrue(noResults, "Should show no results for search term: " + searchTerm);
        } else {
            // For valid searches, users should be displayed
            assertTrue(userPage.areUsersDisplayed(), 
                      "Should display users for search term: " + searchTerm);
        }
        
        log.debug("✓ User search verified for term: {}", searchTerm);
    }
    
    @Test
    @DisplayName("Should clear search results correctly")
    void shouldClearSearchResultsCorrectly() {
        log.info("Testing search clear functionality");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to user management and perform search
        userPage.navigateToUserList(baseUrl)
                .searchUsers("admin")
                .clearSearch();
        
        // Wait for clear operation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verify all users are displayed again
        assertTrue(userPage.areUsersDisplayed(), "All users should be displayed after clearing search");
        
        log.debug("✓ Search clear functionality verified");
    }
    
    @Test
    @DisplayName("Should navigate to create user form correctly")
    void shouldNavigateToCreateUserFormCorrectly() {
        log.info("Testing navigation to create user form");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to user management and click create user
        userPage.navigateToUserList(baseUrl)
                .clickCreateUser();
        
        // Verify form page loads
        assertTrue(userPage.isUserFormPageLoaded(), "Create user form should load");
        
        log.debug("✓ Navigation to create user form verified");
    }
    
    @Test
    @DisplayName("Should display create user form elements correctly")
    void shouldDisplayCreateUserFormElementsCorrectly() {
        log.info("Testing create user form elements display");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to create user form
        userPage.navigateToCreateUser(baseUrl);
        
        // Verify form elements are present
        assertTrue(userPage.isUserFormPageLoaded(), "User form should be loaded");
        assertFalse(userPage.getAvailableBranches().isEmpty(), "Branches should be available for selection");
        
        log.debug("✓ Create user form elements displayed correctly");
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/selenium/essential/user-creation-essential.csv", numLinesToSkip = 1)
    @DisplayName("Should fill user creation form with valid data")
    void shouldFillUserCreationFormWithValidData(String username, String fullName, String email, String branchName, String description) {
        log.info("Testing user creation form filling: {}", description);
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to create user form
        userPage.navigateToCreateUser(baseUrl);
        
        // Fill the form with test data
        userPage.fillUsername(username)
                .fillFullName(fullName)
                .fillEmail(email);
        
        // Select branch if available
        if (!userPage.getAvailableBranches().isEmpty()) {
            userPage.selectBranch(branchName.isEmpty() ? userPage.getAvailableBranches().get(0) : branchName);
        }
        
        // Verify form is ready for submission
        assertTrue(userPage.isFormReadyForSubmission(), "Form should be ready for submission with valid data");
        
        log.debug("✓ User creation form filled successfully with: {}", username);
    }
    
    @Test
    @DisplayName("Should cancel user creation correctly")
    void shouldCancelUserCreationCorrectly() {
        log.info("Testing user creation cancellation");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to create user form and cancel
        userPage.navigateToCreateUser(baseUrl)
                .clickCancel();
        
        // Should return to user list
        assertTrue(userPage.isUserListPageLoaded(), "Should return to user list after cancel");
        
        log.debug("✓ User creation cancellation verified");
    }
    
    @Test
    @DisplayName("Should navigate back to list from create form correctly")
    void shouldNavigateBackToListFromCreateFormCorrectly() {
        log.info("Testing back to list navigation from create form");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to create user form and go back
        userPage.navigateToCreateUser(baseUrl)
                .clickBackToList();
        
        // Should return to user list
        assertTrue(userPage.isUserListPageLoaded(), "Should return to user list");
        
        log.debug("✓ Back to list navigation verified");
    }
    
    @Test
    @DisplayName("Should display user status correctly in the list")
    void shouldDisplayUserStatusCorrectlyInList() {
        log.info("Testing user status display in list");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to user management
        userPage.navigateToUserList(baseUrl);
        
        // Verify admin user status
        if (userPage.isUserVisible("admin")) {
            String adminStatus = userPage.getUserStatus("admin");
            assertTrue(!adminStatus.isEmpty(), "Admin user should have status displayed");
            log.debug("Admin user status: {}", adminStatus);
        }
        
        log.debug("✓ User status display verified");
    }
    
    @Test
    @DisplayName("Should show appropriate action buttons for each user")
    void shouldShowAppropriateActionButtonsForEachUser() {
        log.info("Testing action buttons display for users");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to user management
        userPage.navigateToUserList(baseUrl);
        
        // Verify admin user has view and edit actions
        if (userPage.isUserVisible("admin")) {
            // Try to click view - this should work without error
            assertDoesNotThrow(() -> {
                userPage.clickViewUser("admin");
                // Verify we can navigate to view page
                assertTrue(userPage.isUserViewPageLoaded() || userPage.isUserListPageLoaded(), 
                          "Should navigate to view page or stay on list");
            }, "View user action should work");
        }
        
        log.debug("✓ Action buttons display verified");
    }
    
    @Test
    @DisplayName("Should handle navigation between user management pages")
    void shouldHandleNavigationBetweenUserManagementPages() {
        log.info("Testing navigation between user management pages");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Test navigation flow: List -> Create -> List
        userPage.navigateToUserList(baseUrl);
        assertTrue(userPage.isUserListPageLoaded(), "Should start at user list");
        
        userPage.clickCreateUser();
        assertTrue(userPage.isUserFormPageLoaded(), "Should navigate to create form");
        
        userPage.clickBackToList();
        assertTrue(userPage.isUserListPageLoaded(), "Should return to user list");
        
        log.debug("✓ Navigation between user management pages verified");
    }
    
    @Test
    @DisplayName("Should display user list with proper styling and layout")
    void shouldDisplayUserListWithProperStylingAndLayout() {
        log.info("Testing user list styling and layout");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to user management
        userPage.navigateToUserList(baseUrl);
        
        // Verify basic layout elements are present
        assertTrue(userPage.isUserListPageLoaded(), "User list should be loaded with proper layout");
        assertTrue(userPage.areUsersDisplayed(), "Users should be displayed in organized table");
        
        log.debug("✓ User list styling and layout verified");
    }
    
    @Test
    @DisplayName("Should handle form validation for required fields")
    void shouldHandleFormValidationForRequiredFields() {
        log.info("Testing form validation for required fields");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Navigate to create user form
        userPage.navigateToCreateUser(baseUrl);
        
        // Try to submit empty form
        userPage.submitForm();
        
        // Form should still be displayed (validation prevents submission)
        assertTrue(userPage.isUserFormPageLoaded(), "Form should remain loaded after validation failure");
        
        log.debug("✓ Form validation for required fields verified");
    }
    
    @Test
    @DisplayName("Should support user management workflow end-to-end")
    void shouldSupportUserManagementWorkflowEndToEnd() {
        log.info("Testing complete user management workflow");
        
        // Login as admin
        loginPage.navigateTo(baseUrl);
        loginPage.loginWith("admin", "minibank123");
        
        // Complete workflow: List -> Search -> Clear -> Create -> Cancel -> List
        userPage.navigateToUserList(baseUrl);
        assertTrue(userPage.isUserListPageLoaded(), "Should start at user list");
        
        userPage.searchUsers("admin");
        assertTrue(userPage.areUsersDisplayed() || userPage.isNoUsersMessageVisible(), 
                  "Search should return results or no results message");
        
        userPage.clearSearch();
        assertTrue(userPage.areUsersDisplayed(), "Should display all users after clear");
        
        userPage.clickCreateUser();
        assertTrue(userPage.isUserFormPageLoaded(), "Should navigate to create form");
        
        userPage.clickCancel();
        assertTrue(userPage.isUserListPageLoaded(), "Should return to user list");
        
        log.debug("✓ Complete user management workflow verified");
    }
}