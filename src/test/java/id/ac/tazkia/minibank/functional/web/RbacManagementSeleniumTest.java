package id.ac.tazkia.minibank.functional.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.functional.web.pageobject.PermissionListPage;
import id.ac.tazkia.minibank.functional.web.pageobject.RoleFormPage;
import id.ac.tazkia.minibank.functional.web.pageobject.RoleListPage;
import id.ac.tazkia.minibank.functional.web.pageobject.UserFormPage;
import id.ac.tazkia.minibank.functional.web.pageobject.UserListPage;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.repository.UserRepository;

public class RbacManagementSeleniumTest extends BaseSeleniumTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void shouldLoadUserManagementPage() {
        UserListPage listPage = new UserListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(driver.getCurrentUrl().contains("/rbac/users/list"));
        assertEquals("User Management", listPage.getPageTitle());
        assertTrue(listPage.isCreateButtonDisplayed());
    }

    @Test
    public void shouldLoadRoleManagementPage() {
        RoleListPage listPage = new RoleListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(driver.getCurrentUrl().contains("/rbac/roles/list"));
        assertEquals("Role Management", listPage.getPageTitle());
        assertTrue(listPage.isCreateButtonDisplayed());
    }

    @Test
    public void shouldLoadPermissionManagementPage() {
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(driver.getCurrentUrl().contains("/rbac/permissions/list"));
        assertEquals("Permission Management", listPage.getPageTitle());
        assertTrue(listPage.isCreateButtonDisplayed());
    }

    @Test
    public void shouldCreateNewUser() {
        long timestamp = System.currentTimeMillis();
        String uniqueUsername = "sel_" + timestamp;
        String fullName = "Selenium Test User";
        String email = "sel.test" + timestamp + "@example.com"; // Make email unique too
        
        UserListPage listPage = new UserListPage(driver, baseUrl);
        listPage.open();
        
        UserFormPage formPage = listPage.clickCreateUser();
        
        // No password field since we separated password functionality
        formPage.fillUserForm(uniqueUsername, fullName, email, null);
        
        UserListPage resultPage = formPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed());
        assertTrue(resultPage.isUserDisplayed(uniqueUsername));
        
        // Verify in database
        User savedUser = userRepository.findByUsername(uniqueUsername).orElse(null);
        assertNotNull(savedUser);
        assertEquals(uniqueUsername, savedUser.getUsername());
        assertEquals(fullName, savedUser.getFullName());
        assertEquals(email, savedUser.getEmail());
    }

    @Test
    public void shouldCreateNewRole() {
        String uniqueRoleCode = "SELENIUM_ROLE_" + System.currentTimeMillis();
        String roleName = "Selenium Test Role";
        String description = "A test role created by Selenium";
        
        RoleListPage listPage = new RoleListPage(driver, baseUrl);
        listPage.open();
        
        RoleFormPage formPage = listPage.clickCreateRole();
        
        formPage.fillRoleForm(uniqueRoleCode, roleName, description);
        
        RoleListPage resultPage = formPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed());
        assertTrue(resultPage.isRoleDisplayed(uniqueRoleCode));
        
        // Verify in database
        Role savedRole = roleRepository.findByRoleCode(uniqueRoleCode).orElse(null);
        assertNotNull(savedRole);
        assertEquals(uniqueRoleCode, savedRole.getRoleCode());
        assertEquals(roleName, savedRole.getRoleName());
        assertEquals(description, savedRole.getDescription());
    }

    @Test
    public void shouldValidateUserFormRequiredFields() {
        UserListPage listPage = new UserListPage(driver, baseUrl);
        listPage.open();
        
        UserFormPage formPage = listPage.clickCreateUser();
        
        // Try to submit empty form
        formPage.submitFormExpectingError();
        
        // Should remain on form page
        assertTrue(driver.getCurrentUrl().contains("/rbac/users/create"));
        
        // Verify form still exists
        assertTrue(formPage.isFormDisplayed());
    }

    @Test
    public void shouldValidateRoleFormRequiredFields() {
        RoleListPage listPage = new RoleListPage(driver, baseUrl);
        listPage.open();
        
        RoleFormPage formPage = listPage.clickCreateRole();
        
        // Try to submit empty form
        formPage.submitFormExpectingError();
        
        // Should remain on form page
        assertTrue(driver.getCurrentUrl().contains("/rbac/roles/create"));
        
        // Verify form still exists
        assertTrue(formPage.isFormDisplayed());
    }

    @Test
    public void shouldNavigateToUserFormAndBack() {
        UserListPage listPage = new UserListPage(driver, baseUrl);
        listPage.open();
        
        UserFormPage formPage = listPage.clickCreateUser();
        assertTrue(driver.getCurrentUrl().contains("/rbac/users/create"));
        
        UserListPage backToList = formPage.clickBackToList();
        assertTrue(driver.getCurrentUrl().contains("/rbac/users/list"));
        assertEquals("User Management", backToList.getPageTitle());
    }

    @Test
    public void shouldSearchUsers() {
        // Create a test user with shorter username to avoid 50-character limit
        long timestamp = System.currentTimeMillis();
        String searchUsername = "search_" + timestamp;
        String email = "search" + timestamp + "@example.com";
        
        UserListPage listPage = new UserListPage(driver, baseUrl);
        listPage.open();
        
        UserFormPage formPage = listPage.clickCreateUser();
        formPage.fillUserForm(searchUsername, "Searchable User", email, null);
        UserListPage resultPage = formPage.submitForm();
        
        // Now search for the user
        resultPage.searchUsers(searchUsername);
        
        assertTrue(resultPage.isUserDisplayed(searchUsername));
    }

    @Test
    public void shouldDisplayPermissionsList() {
        PermissionListPage listPage = new PermissionListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(listPage.isPermissionsTableDisplayed());
        assertTrue(listPage.isCategoryFilterDisplayed());
    }
}
