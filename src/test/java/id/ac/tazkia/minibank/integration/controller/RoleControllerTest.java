package id.ac.tazkia.minibank.integration.controller;

import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.RolePermission;
import id.ac.tazkia.minibank.integration.BaseIntegrationTest;
import id.ac.tazkia.minibank.repository.PermissionRepository;
import id.ac.tazkia.minibank.repository.RolePermissionRepository;
import id.ac.tazkia.minibank.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("RoleController Integration Tests")
class RoleControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    
    private MockMvc mockMvc;
    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        
        // Create test role
        testRole = new Role();
        testRole.setRoleCode("TEST_ROLE");
        testRole.setRoleName("Test Role");
        testRole.setDescription("Test role description");
        testRole.setIsActive(true);
        testRole.setCreatedBy("system");
        testRole = roleRepository.save(testRole);
        
        // Create test permission
        testPermission = new Permission();
        testPermission.setPermissionCode("TEST_PERMISSION");
        testPermission.setPermissionName("Test Permission");
        testPermission.setPermissionCategory("TEST_CATEGORY");
        testPermission.setDescription("Test permission description");
        testPermission.setCreatedBy("system");
        testPermission = permissionRepository.save(testPermission);
    }

    @Test
    @DisplayName("Should display roles list page")
    void shouldDisplayRolesListPage() throws Exception {
        mockMvc.perform(get("/rbac/roles/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/roles/list"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("totalItems"))
                .andExpect(model().attributeExists("pageSize"))
                .andExpect(model().attributeExists("sortBy"))
                .andExpect(model().attributeExists("sortDir"));
    }

    @Test
    @DisplayName("Should display roles list with pagination parameters")
    void shouldDisplayRolesListWithPagination() throws Exception {
        mockMvc.perform(get("/rbac/roles/list")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "roleCode")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/roles/list"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 5))
                .andExpect(model().attribute("sortBy", "roleCode"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("Should display create role form")
    void shouldDisplayCreateRoleForm() throws Exception {
        mockMvc.perform(get("/rbac/roles/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/roles/form"))
                .andExpect(model().attributeExists("role"));
    }

    @Test
    @DisplayName("Should create role successfully")
    void shouldCreateRoleSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/roles/create")
                        .param("roleCode", "NEW_ROLE")
                        .param("roleName", "New Role")
                        .param("description", "New role description")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("successMessage", "Role created successfully"));
    }

    @Test
    @DisplayName("Should reject create role with duplicate role code")
    void shouldRejectCreateRoleWithDuplicateRoleCode() throws Exception {
        mockMvc.perform(post("/rbac/roles/create")
                        .param("roleCode", testRole.getRoleCode())
                        .param("roleName", "Different Role")
                        .param("description", "Different description")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/roles/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("Should display edit role form")
    void shouldDisplayEditRoleForm() throws Exception {
        mockMvc.perform(get("/rbac/roles/edit/" + testRole.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/roles/form"))
                .andExpect(model().attribute("role", hasProperty("id", is(testRole.getId()))));
    }

    @Test
    @DisplayName("Should redirect when edit non-existing role")
    void shouldRedirectWhenEditNonExistingRole() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/rbac/roles/edit/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("errorMessage", "Role not found"));
    }

    @Test
    @DisplayName("Should update role successfully")
    void shouldUpdateRoleSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/roles/edit/" + testRole.getId())
                        .param("roleCode", "UPDATED_ROLE")
                        .param("roleName", "Updated Role")
                        .param("description", "Updated description")
                        .param("isActive", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("successMessage", "Role updated successfully"));
    }

    @Test
    @DisplayName("Should reject update role with duplicate role code")
    void shouldRejectUpdateRoleWithDuplicateRoleCode() throws Exception {
        // Create another role
        Role anotherRole = new Role();
        anotherRole.setRoleCode("ANOTHER_ROLE");
        anotherRole.setRoleName("Another Role");
        anotherRole.setIsActive(true);
        anotherRole.setCreatedBy("system");
        anotherRole = roleRepository.save(anotherRole);

        // Try to update testRole with anotherRole's code
        mockMvc.perform(post("/rbac/roles/edit/" + testRole.getId())
                        .param("roleCode", anotherRole.getRoleCode())
                        .param("roleName", "Updated Role")
                        .param("description", "Updated description")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/roles/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("Should display role view")
    void shouldDisplayRoleView() throws Exception {
        mockMvc.perform(get("/rbac/roles/view/" + testRole.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/roles/view"))
                .andExpect(model().attribute("role", hasProperty("id", is(testRole.getId()))))
                .andExpect(model().attributeExists("rolePermissions"));
    }

    @Test
    @DisplayName("Should redirect when view non-existing role")
    void shouldRedirectWhenViewNonExistingRole() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/rbac/roles/view/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("errorMessage", "Role not found"));
    }

    @Test
    @DisplayName("Should display manage role permissions page")
    void shouldDisplayManageRolePermissionsPage() throws Exception {
        mockMvc.perform(get("/rbac/roles/" + testRole.getId() + "/permissions"))
                .andExpect(status().isOk())
                .andExpect(view().name("rbac/roles/permissions"))
                .andExpect(model().attribute("role", hasProperty("id", is(testRole.getId()))))
                .andExpect(model().attributeExists("rolePermissions"))
                .andExpect(model().attributeExists("allPermissions"));
    }

    @Test
    @DisplayName("Should redirect when manage permissions for non-existing role")
    void shouldRedirectWhenManagePermissionsForNonExistingRole() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/rbac/roles/" + nonExistingId + "/permissions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("errorMessage", "Role not found"));
    }

    @Test
    @DisplayName("Should assign permission to role successfully")
    void shouldAssignPermissionToRoleSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/roles/" + testRole.getId() + "/permissions/assign")
                        .param("permissionId", testPermission.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/" + testRole.getId() + "/permissions"))
                .andExpect(flash().attribute("successMessage", "Permission assigned successfully"));
    }

    @Test
    @DisplayName("Should reject assigning duplicate permission")
    void shouldRejectAssigningDuplicatePermission() throws Exception {
        // First assign the permission
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(testRole);
        rolePermission.setPermission(testPermission);
        rolePermission.setGrantedBy("system");
        rolePermissionRepository.save(rolePermission);

        // Try to assign the same permission again
        mockMvc.perform(post("/rbac/roles/" + testRole.getId() + "/permissions/assign")
                        .param("permissionId", testPermission.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/" + testRole.getId() + "/permissions"))
                .andExpect(flash().attribute("errorMessage", "Role already has this permission"));
    }

    @Test
    @DisplayName("Should handle assign permission with invalid role")
    void shouldHandleAssignPermissionWithInvalidRole() throws Exception {
        UUID invalidRoleId = UUID.randomUUID();
        mockMvc.perform(post("/rbac/roles/" + invalidRoleId + "/permissions/assign")
                        .param("permissionId", testPermission.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/" + invalidRoleId + "/permissions"))
                .andExpect(flash().attribute("errorMessage", "Role or permission not found"));
    }

    @Test
    @DisplayName("Should handle assign permission with invalid permission")
    void shouldHandleAssignPermissionWithInvalidPermission() throws Exception {
        UUID invalidPermissionId = UUID.randomUUID();
        mockMvc.perform(post("/rbac/roles/" + testRole.getId() + "/permissions/assign")
                        .param("permissionId", invalidPermissionId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/" + testRole.getId() + "/permissions"))
                .andExpect(flash().attribute("errorMessage", "Role or permission not found"));
    }

    @Test
    @DisplayName("Should remove permission from role successfully")
    void shouldRemovePermissionFromRoleSuccessfully() throws Exception {
        // First assign a permission
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(testRole);
        rolePermission.setPermission(testPermission);
        rolePermission.setGrantedBy("system");
        rolePermission = rolePermissionRepository.save(rolePermission);

        mockMvc.perform(post("/rbac/roles/" + testRole.getId() + "/permissions/remove")
                        .param("rolePermissionId", rolePermission.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/" + testRole.getId() + "/permissions"))
                .andExpect(flash().attribute("successMessage", "Permission removed successfully"));
    }

    @Test
    @DisplayName("Should activate role successfully")
    void shouldActivateRoleSuccessfully() throws Exception {
        // First deactivate the role
        testRole.setIsActive(false);
        roleRepository.save(testRole);

        mockMvc.perform(post("/rbac/roles/" + testRole.getId() + "/activate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("successMessage", "Role activated successfully"));
    }

    @Test
    @DisplayName("Should deactivate role successfully")
    void shouldDeactivateRoleSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/roles/" + testRole.getId() + "/deactivate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("successMessage", "Role deactivated successfully"));
    }

    @Test
    @DisplayName("Should delete role successfully")
    void shouldDeleteRoleSuccessfully() throws Exception {
        mockMvc.perform(post("/rbac/roles/delete/" + testRole.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("successMessage", "Role deleted successfully"));
    }

    @Test
    @DisplayName("Should handle activate non-existing role")
    void shouldHandleActivateNonExistingRole() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(post("/rbac/roles/" + nonExistingId + "/activate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("errorMessage", "Role not found"));
    }

    @Test
    @DisplayName("Should handle deactivate non-existing role")
    void shouldHandleDeactivateNonExistingRole() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(post("/rbac/roles/" + nonExistingId + "/deactivate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("errorMessage", "Role not found"));
    }

    @Test
    @DisplayName("Should handle delete non-existing role")
    void shouldHandleDeleteNonExistingRole() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(post("/rbac/roles/delete/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("errorMessage", "Role not found"));
    }

    @Test
    @DisplayName("Should allow updating role with same role code")
    void shouldAllowUpdatingRoleWithSameRoleCode() throws Exception {
        mockMvc.perform(post("/rbac/roles/edit/" + testRole.getId())
                        .param("roleCode", testRole.getRoleCode())  // Same code
                        .param("roleName", "Updated Role Name")      // Different name
                        .param("description", "Updated description")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rbac/roles/list"))
                .andExpect(flash().attribute("successMessage", "Role updated successfully"));
    }
}