package id.ac.tazkia.minibank.integration.controller;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserRole;
import id.ac.tazkia.minibank.integration.BaseIntegrationTest;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.repository.UserRepository;
import id.ac.tazkia.minibank.repository.UserRoleRepository;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("UserController Integration Tests")
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    private MockMvc mockMvc;
    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setIsActive(true);
        testUser.setCreatedBy("system");
        testUser = userRepository.save(testUser);
        
        // Create test role
        testRole = new Role();
        testRole.setRoleCode("TEST_ROLE");
        testRole.setRoleName("Test Role");
        testRole.setIsActive(true);
        testRole.setCreatedBy("system");
        testRole = roleRepository.save(testRole);
    }

    @Test
    @DisplayName("Should display users list page")
    void shouldDisplayUsersListPage() throws Exception {
        mockMvc.perform(get("/rbac/users/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/list"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("totalItems"))
                .andExpect(model().attributeExists("pageSize"))
                .andExpect(model().attributeExists("sortBy"))
                .andExpect(model().attributeExists("sortDir"));
    }

    @Test
    @DisplayName("Should display users list with pagination parameters")
    void shouldDisplayUsersListWithPagination() throws Exception {
        mockMvc.perform(get("/rbac/users/list")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "username")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/list"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 5))
                .andExpect(model().attribute("sortBy", "username"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("Should display users list with search")
    void shouldDisplayUsersListWithSearch() throws Exception {
        mockMvc.perform(get("/rbac/users/list")
                        .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/list"))
                .andExpect(model().attribute("search", "test"));
    }

    @Test
    @DisplayName("Should display create user form")
    void shouldDisplayCreateUserForm() throws Exception {
        mockMvc.perform(get("/rbac/users/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/form"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/users/create")
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("fullName", "New User")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/list"))
                .andExpect(flash().attribute("successMessage", 
                    "User created successfully. You can set a password from the user details page."));
    }

    @Test
    @DisplayName("Should reject create user with duplicate username")
    void shouldRejectCreateUserWithDuplicateUsername() throws Exception {
        mockMvc.perform(post("/rbac/users/create")
                        .param("username", testUser.getUsername())
                        .param("email", "different@example.com")
                        .param("fullName", "Different User")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("Should reject create user with duplicate email")
    void shouldRejectCreateUserWithDuplicateEmail() throws Exception {
        mockMvc.perform(post("/rbac/users/create")
                        .param("username", "differentuser")
                        .param("email", testUser.getEmail())
                        .param("fullName", "Different User")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("Should display edit user form")
    void shouldDisplayEditUserForm() throws Exception {
        mockMvc.perform(get("/rbac/users/edit/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/form"))
                .andExpect(model().attribute("user", hasProperty("id", is(testUser.getId()))));
    }

    @Test
    @DisplayName("Should redirect when edit non-existing user")
    void shouldRedirectWhenEditNonExistingUser() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/rbac/users/edit/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/list"))
                .andExpect(flash().attribute("errorMessage", "User not found"));
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/users/edit/" + testUser.getId())
                        .param("username", "updateduser")
                        .param("email", "updated@example.com")
                        .param("fullName", "Updated User")
                        .param("isActive", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/list"))
                .andExpect(flash().attribute("successMessage", "User updated successfully"));
    }

    @Test
    @DisplayName("Should display user view")
    void shouldDisplayUserView() throws Exception {
        mockMvc.perform(get("/rbac/users/view/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/view"))
                .andExpect(model().attribute("user", hasProperty("id", is(testUser.getId()))))
                .andExpect(model().attributeExists("userRoles"));
    }

    @Test
    @DisplayName("Should display manage user roles page")
    void shouldDisplayManageUserRolesPage() throws Exception {
        mockMvc.perform(get("/rbac/users/" + testUser.getId() + "/roles"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/roles"))
                .andExpect(model().attribute("user", hasProperty("id", is(testUser.getId()))))
                .andExpect(model().attributeExists("userRoles"))
                .andExpect(model().attributeExists("allRoles"));
    }

    @Test
    @DisplayName("Should assign role to user successfully")
    void shouldAssignRoleToUserSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/users/" + testUser.getId() + "/roles/assign")
                        .param("roleId", testRole.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/" + testUser.getId() + "/roles"))
                .andExpect(flash().attribute("successMessage", "Role assigned successfully"));
    }

    @Test
    @DisplayName("Should reject assigning duplicate role")
    void shouldRejectAssigningDuplicateRole() throws Exception {
        // First assign the role
        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setRole(testRole);
        userRole.setAssignedBy("system");
        userRoleRepository.save(userRole);

        // Try to assign the same role again
        mockMvc.perform(post("/rbac/users/" + testUser.getId() + "/roles/assign")
                        .param("roleId", testRole.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/" + testUser.getId() + "/roles"))
                .andExpect(flash().attribute("errorMessage", "User already has this role"));
    }

    @Test
    @DisplayName("Should remove role from user successfully")
    void shouldRemoveRoleFromUserSuccessfully() throws Exception {
        // First assign a role
        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setRole(testRole);
        userRole.setAssignedBy("system");
        userRole = userRoleRepository.save(userRole);

        mockMvc.perform(post("/rbac/users/" + testUser.getId() + "/roles/remove")
                        .param("userRoleId", userRole.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/" + testUser.getId() + "/roles"))
                .andExpect(flash().attribute("successMessage", "Role removed successfully"));
    }

    @Test
    @DisplayName("Should activate user successfully")
    void shouldActivateUserSuccessfully() throws Exception {
        // First deactivate the user
        testUser.setIsActive(false);
        userRepository.save(testUser);

        mockMvc.perform(post("/rbac/users/" + testUser.getId() + "/activate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/list"))
                .andExpect(flash().attribute("successMessage", "User activated successfully"));
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUserSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/users/" + testUser.getId() + "/deactivate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/list"))
                .andExpect(flash().attribute("successMessage", "User deactivated successfully"));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/users/delete/" + testUser.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/list"))
                .andExpect(flash().attribute("successMessage", "User deleted successfully"));
    }

    @Test
    @DisplayName("Should display password form")
    void shouldDisplayPasswordForm() throws Exception {
        mockMvc.perform(get("/rbac/users/" + testUser.getId() + "/password"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/users/password"))
                .andExpect(model().attribute("user", hasProperty("id", is(testUser.getId()))));
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/users/" + testUser.getId() + "/password")
                        .param("password", "newpassword123")
                        .param("confirmPassword", "newpassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/view/" + testUser.getId()))
                .andExpect(flash().attribute("successMessage", "Password updated successfully"));
    }

    @Test
    @DisplayName("Should reject password change when passwords don't match")
    void shouldRejectPasswordChangeWhenPasswordsDontMatch() throws Exception {
        mockMvc.perform(post("/rbac/users/" + testUser.getId() + "/password")
                        .param("password", "password123")
                        .param("confirmPassword", "differentpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/" + testUser.getId() + "/password"))
                .andExpect(flash().attribute("errorMessage", "Passwords do not match"));
    }

    @Test
    @DisplayName("Should reject password change when password is too short")
    void shouldRejectPasswordChangeWhenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/rbac/users/" + testUser.getId() + "/password")
                        .param("password", "123")
                        .param("confirmPassword", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/" + testUser.getId() + "/password"))
                .andExpect(flash().attribute("errorMessage", "Password must be at least 6 characters long"));
    }

    @Test
    @DisplayName("Should reject password change when password is empty")
    void shouldRejectPasswordChangeWhenPasswordIsEmpty() throws Exception {
        mockMvc.perform(post("/rbac/users/" + testUser.getId() + "/password")
                        .param("password", "")
                        .param("confirmPassword", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/" + testUser.getId() + "/password"))
                .andExpect(flash().attribute("errorMessage", "Password cannot be empty"));
    }

    @Test
    @DisplayName("Should handle non-existing user in operations")
    void shouldHandleNonExistingUserInOperations() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        
        // Test view
        mockMvc.perform(get("/rbac/users/view/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/list"))
                .andExpect(flash().attribute("errorMessage", "User not found"));
        
        // Test activate
        mockMvc.perform(post("/rbac/users/" + nonExistingId + "/activate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/list"))
                .andExpect(flash().attribute("errorMessage", "User not found"));
        
        // Test delete
        mockMvc.perform(post("/rbac/users/delete/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/users/list"))
                .andExpect(flash().attribute("errorMessage", "User not found"));
    }
}