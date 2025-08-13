package id.ac.tazkia.minibank.integration.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
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

import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.RolePermission;
import id.ac.tazkia.minibank.integration.BaseIntegrationTest;
import id.ac.tazkia.minibank.repository.PermissionRepository;
import id.ac.tazkia.minibank.repository.RolePermissionRepository;
import id.ac.tazkia.minibank.repository.RoleRepository;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("PermissionController Integration Tests")
class PermissionControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    
    private MockMvc mockMvc;
    private Permission testPermission;
    private Role testRole;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        
        // Create test permission
        testPermission = new Permission();
        testPermission.setPermissionCode("TEST_PERMISSION");
        testPermission.setPermissionName("Test Permission");
        testPermission.setPermissionCategory("TEST_CATEGORY");
        testPermission.setDescription("Test permission description");
        testPermission.setResource("test_resource");
        testPermission.setAction("read");
        testPermission.setCreatedBy("system");
        testPermission = permissionRepository.save(testPermission);
        
        // Create test role
        testRole = new Role();
        testRole.setRoleCode("TEST_ROLE");
        testRole.setRoleName("Test Role");
        testRole.setIsActive(true);
        testRole.setCreatedBy("system");
        testRole = roleRepository.save(testRole);
    }

    @Test
    @DisplayName("Should display permissions list page")
    void shouldDisplayPermissionsListPage() throws Exception {
        mockMvc.perform(get("/rbac/permissions/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/list"))
                .andExpect(model().attributeExists("permissions"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("totalItems"))
                .andExpect(model().attributeExists("pageSize"))
                .andExpect(model().attributeExists("sortBy"))
                .andExpect(model().attributeExists("sortDir"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Should display permissions list with pagination parameters")
    void shouldDisplayPermissionsListWithPagination() throws Exception {
        mockMvc.perform(get("/rbac/permissions/list")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "permissionCode")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/list"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 5))
                .andExpect(model().attribute("sortBy", "permissionCode"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("Should display permissions list with category filter")
    void shouldDisplayPermissionsListWithCategoryFilter() throws Exception {
        mockMvc.perform(get("/rbac/permissions/list")
                        .param("category", "TEST_CATEGORY"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/list"))
                .andExpect(model().attribute("category", "TEST_CATEGORY"));
    }

    @Test
    @DisplayName("Should display create permission form")
    void shouldDisplayCreatePermissionForm() throws Exception {
        mockMvc.perform(get("/rbac/permissions/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/form"))
                .andExpect(model().attributeExists("permission"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Should create permission successfully")
    void shouldCreatePermissionSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/permissions/create")
                        .param("permissionCode", "NEW_PERMISSION")
                        .param("permissionName", "New Permission")
                        .param("permissionCategory", "NEW_CATEGORY")
                        .param("description", "New permission description")
                        .param("resource", "new_resource")
                        .param("action", "write"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/permissions/list"))
                .andExpect(flash().attribute("successMessage", "Permission created successfully"));
    }

    @Test
    @DisplayName("Should reject create permission with duplicate permission code")
    void shouldRejectCreatePermissionWithDuplicatePermissionCode() throws Exception {
        mockMvc.perform(post("/rbac/permissions/create")
                        .param("permissionCode", testPermission.getPermissionCode())
                        .param("permissionName", "Different Permission")
                        .param("permissionCategory", "DIFFERENT_CATEGORY")
                        .param("description", "Different description")
                        .param("resource", "different_resource")
                        .param("action", "write"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Should display edit permission form")
    void shouldDisplayEditPermissionForm() throws Exception {
        mockMvc.perform(get("/rbac/permissions/edit/" + testPermission.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/form"))
                .andExpect(model().attribute("permission", hasProperty("id", is(testPermission.getId()))))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Should redirect when edit non-existing permission")
    void shouldRedirectWhenEditNonExistingPermission() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/rbac/permissions/edit/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/permissions/list"))
                .andExpect(flash().attribute("errorMessage", "Permission not found"));
    }

    @Test
    @DisplayName("Should update permission successfully")
    void shouldUpdatePermissionSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/permissions/edit/" + testPermission.getId())
                        .param("permissionCode", "UPDATED_PERMISSION")
                        .param("permissionName", "Updated Permission")
                        .param("permissionCategory", "UPDATED_CATEGORY")
                        .param("description", "Updated description")
                        .param("resource", "updated_resource")
                        .param("action", "delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/permissions/list"))
                .andExpect(flash().attribute("successMessage", "Permission updated successfully"));
    }

    @Test
    @DisplayName("Should reject update permission with duplicate permission code")
    void shouldRejectUpdatePermissionWithDuplicatePermissionCode() throws Exception {
        // Create another permission
        Permission anotherPermission = new Permission();
        anotherPermission.setPermissionCode("ANOTHER_PERMISSION");
        anotherPermission.setPermissionName("Another Permission");
        anotherPermission.setPermissionCategory("ANOTHER_CATEGORY");
        anotherPermission.setCreatedBy("system");
        anotherPermission = permissionRepository.save(anotherPermission);

        // Try to update testPermission with anotherPermission's code
        mockMvc.perform(post("/rbac/permissions/edit/" + testPermission.getId())
                        .param("permissionCode", anotherPermission.getPermissionCode())
                        .param("permissionName", "Updated Permission")
                        .param("permissionCategory", "UPDATED_CATEGORY")
                        .param("description", "Updated description")
                        .param("resource", "updated_resource")
                        .param("action", "write"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Should allow updating permission with same permission code")
    void shouldAllowUpdatingPermissionWithSamePermissionCode() throws Exception {
        mockMvc.perform(post("/rbac/permissions/edit/" + testPermission.getId())
                        .param("permissionCode", testPermission.getPermissionCode()) // Same code
                        .param("permissionName", "Updated Permission Name")            // Different name
                        .param("permissionCategory", "UPDATED_CATEGORY")
                        .param("description", "Updated description")
                        .param("resource", "updated_resource")
                        .param("action", "write"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/permissions/list"))
                .andExpect(flash().attribute("successMessage", "Permission updated successfully"));
    }

    @Test
    @DisplayName("Should redirect when update non-existing permission")
    void shouldRedirectWhenUpdateNonExistingPermission() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(post("/rbac/permissions/edit/" + nonExistingId)
                        .param("permissionCode", "SOME_PERMISSION")
                        .param("permissionName", "Some Permission")
                        .param("permissionCategory", "SOME_CATEGORY")
                        .param("description", "Some description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/permissions/list"))
                .andExpect(flash().attribute("errorMessage", "Permission not found"));
    }

    @Test
    @DisplayName("Should display permission view")
    void shouldDisplayPermissionView() throws Exception {
        mockMvc.perform(get("/rbac/permissions/view/" + testPermission.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/view"))
                .andExpect(model().attribute("permission", hasProperty("id", is(testPermission.getId()))))
                .andExpect(model().attributeExists("rolePermissions"));
    }

    @Test
    @DisplayName("Should redirect when view non-existing permission")
    void shouldRedirectWhenViewNonExistingPermission() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/rbac/permissions/view/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/permissions/list"))
                .andExpect(flash().attribute("errorMessage", "Permission not found"));
    }

    @Test
    @DisplayName("Should display permission view with role permissions")
    void shouldDisplayPermissionViewWithRolePermissions() throws Exception {
        // Create a role permission association
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(testRole);
        rolePermission.setPermission(testPermission);
        rolePermission.setGrantedBy("system");
        rolePermissionRepository.save(rolePermission);

        mockMvc.perform(get("/rbac/permissions/view/" + testPermission.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/view"))
                .andExpect(model().attribute("permission", hasProperty("id", is(testPermission.getId()))))
                .andExpect(model().attribute("rolePermissions", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should delete permission successfully")
    void shouldDeletePermissionSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/permissions/delete/" + testPermission.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/permissions/list"))
                .andExpect(flash().attribute("successMessage", "Permission deleted successfully"));
    }

    @Test
    @DisplayName("Should handle delete non-existing permission")
    void shouldHandleDeleteNonExistingPermission() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(post("/rbac/permissions/delete/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/permissions/list"))
                .andExpect(flash().attribute("errorMessage", "Permission not found"));
    }

    @Test
    @DisplayName("Should handle validation errors on create")
    void shouldHandleValidationErrorsOnCreate() throws Exception {
        mockMvc.perform(post("/rbac/permissions/create")
                        .param("permissionCode", "")  // Invalid: empty code
                        .param("permissionName", "")   // Invalid: empty name
                        .param("permissionCategory", "TEST_CATEGORY")
                        .param("description", "Test description"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Should handle validation errors on update")
    void shouldHandleValidationErrorsOnUpdate() throws Exception {
        mockMvc.perform(post("/rbac/permissions/edit/" + testPermission.getId())
                        .param("permissionCode", "")  // Invalid: empty code
                        .param("permissionName", "")   // Invalid: empty name
                        .param("permissionCategory", "TEST_CATEGORY")
                        .param("description", "Test description"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Should show categories in create form even on error")
    void shouldShowCategoriesInCreateFormEvenOnError() throws Exception {
        mockMvc.perform(post("/rbac/permissions/create")
                        .param("permissionCode", testPermission.getPermissionCode()) // Duplicate code
                        .param("permissionName", "Some Name")
                        .param("permissionCategory", "SOME_CATEGORY"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Should show categories in update form even on error")
    void shouldShowCategoriesInUpdateFormEvenOnError() throws Exception {
        // Create another permission to cause duplicate error
        Permission anotherPermission = new Permission();
        anotherPermission.setPermissionCode("ANOTHER_PERMISSION");
        anotherPermission.setPermissionName("Another Permission");
        anotherPermission.setPermissionCategory("ANOTHER_CATEGORY");
        anotherPermission.setCreatedBy("system");
        permissionRepository.save(anotherPermission);

        mockMvc.perform(post("/rbac/permissions/edit/" + testPermission.getId())
                        .param("permissionCode", "ANOTHER_PERMISSION") // Duplicate code
                        .param("permissionName", "Some Name")
                        .param("permissionCategory", "SOME_CATEGORY"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/permissions/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"));
    }
}