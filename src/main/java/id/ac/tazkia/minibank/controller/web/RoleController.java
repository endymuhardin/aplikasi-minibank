package id.ac.tazkia.minibank.controller.web;

import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.entity.RolePermission;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.repository.PermissionRepository;
import id.ac.tazkia.minibank.repository.RolePermissionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/rbac/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    
    @GetMapping("/list")
    public String roleList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Role> roles = roleRepository.findAll(pageable);
        
        model.addAttribute("roles", roles);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roles.getTotalPages());
        model.addAttribute("totalItems", roles.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "rbac/roles/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("role", new Role());
        return "rbac/roles/form";
    }
    
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Role role, 
                        BindingResult result, 
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        if (roleRepository.existsByRoleCode(role.getRoleCode())) {
            result.rejectValue("roleCode", "error.role", "Role code already exists");
        }
        
        if (result.hasErrors()) {
            return "rbac/roles/form";
        }
        
        try {
            role.setCreatedBy("system");
            role.setUpdatedBy("system");
            roleRepository.save(role);
            redirectAttributes.addFlashAttribute("successMessage", "Role created successfully");
            return "redirect:/rbac/roles/list";
        } catch (Exception e) {
            log.error("Error creating role", e);
            model.addAttribute("errorMessage", "Error creating role: " + e.getMessage());
            return "rbac/roles/form";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Role> role = roleRepository.findById(id);
        if (role.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Role not found");
            return "redirect:/rbac/roles/list";
        }
        
        model.addAttribute("role", role.get());
        return "rbac/roles/form";
    }
    
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable UUID id,
                      @Valid @ModelAttribute Role role, 
                      BindingResult result, 
                      Model model,
                      RedirectAttributes redirectAttributes) {
        
        Optional<Role> existingRole = roleRepository.findById(id);
        if (existingRole.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Role not found");
            return "redirect:/rbac/roles/list";
        }
        
        Role existing = existingRole.get();
        
        if (!existing.getRoleCode().equals(role.getRoleCode()) && 
            roleRepository.existsByRoleCode(role.getRoleCode())) {
            result.rejectValue("roleCode", "error.role", "Role code already exists");
        }
        
        if (result.hasErrors()) {
            return "rbac/roles/form";
        }
        
        try {
            existing.setRoleCode(role.getRoleCode());
            existing.setRoleName(role.getRoleName());
            existing.setDescription(role.getDescription());
            existing.setIsActive(role.getIsActive());
            existing.setUpdatedBy("system");
            
            roleRepository.save(existing);
            redirectAttributes.addFlashAttribute("successMessage", "Role updated successfully");
            return "redirect:/rbac/roles/list";
        } catch (Exception e) {
            log.error("Error updating role", e);
            model.addAttribute("errorMessage", "Error updating role: " + e.getMessage());
            return "rbac/roles/form";
        }
    }
    
    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Role> role = roleRepository.findById(id);
        if (role.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Role not found");
            return "redirect:/rbac/roles/list";
        }
        
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(role.get());
        
        model.addAttribute("role", role.get());
        model.addAttribute("rolePermissions", rolePermissions);
        return "rbac/roles/view";
    }
    
    @GetMapping("/{id}/permissions")
    public String manageRolePermissions(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Role> role = roleRepository.findById(id);
        if (role.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Role not found");
            return "redirect:/rbac/roles/list";
        }
        
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(role.get());
        List<Permission> allPermissions = permissionRepository.findAll();
        
        model.addAttribute("role", role.get());
        model.addAttribute("rolePermissions", rolePermissions);
        model.addAttribute("allPermissions", allPermissions);
        return "rbac/roles/permissions";
    }
    
    @PostMapping("/{id}/permissions/assign")
    public String assignPermission(@PathVariable UUID id,
                           @RequestParam UUID permissionId,
                           RedirectAttributes redirectAttributes) {
        try {
            Role role = roleRepository.findById(id).orElse(null);
            Permission permission = permissionRepository.findById(permissionId).orElse(null);
            
            if (role == null || permission == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Role or permission not found");
                return "redirect:/rbac/roles/" + id + "/permissions";
            }
            
            if (rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Role already has this permission");
                return "redirect:/rbac/roles/" + id + "/permissions";
            }
            
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermission.setGrantedBy("system");
            rolePermissionRepository.save(rolePermission);
            
            redirectAttributes.addFlashAttribute("successMessage", "Permission assigned successfully");
        } catch (Exception e) {
            log.error("Error assigning permission to role", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error assigning permission: " + e.getMessage());
        }
        
        return "redirect:/rbac/roles/" + id + "/permissions";
    }
    
    @PostMapping("/{id}/permissions/remove")
    public String removePermission(@PathVariable UUID id,
                           @RequestParam UUID rolePermissionId,
                           RedirectAttributes redirectAttributes) {
        try {
            rolePermissionRepository.deleteById(rolePermissionId);
            redirectAttributes.addFlashAttribute("successMessage", "Permission removed successfully");
        } catch (Exception e) {
            log.error("Error removing permission from role", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error removing permission: " + e.getMessage());
        }
        
        return "redirect:/rbac/roles/" + id + "/permissions";
    }
    
    @PostMapping("/{id}/activate")
    public String activate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Role> roleOpt = roleRepository.findById(id);
            if (roleOpt.isPresent()) {
                Role role = roleOpt.get();
                role.setIsActive(true);
                role.setUpdatedBy("system");
                roleRepository.save(role);
                redirectAttributes.addFlashAttribute("successMessage", "Role activated successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Role not found");
            }
        } catch (Exception e) {
            log.error("Error activating role", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error activating role: " + e.getMessage());
        }
        return "redirect:/rbac/roles/list";
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Role> roleOpt = roleRepository.findById(id);
            if (roleOpt.isPresent()) {
                Role role = roleOpt.get();
                role.setIsActive(false);
                role.setUpdatedBy("system");
                roleRepository.save(role);
                redirectAttributes.addFlashAttribute("successMessage", "Role deactivated successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Role not found");
            }
        } catch (Exception e) {
            log.error("Error deactivating role", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating role: " + e.getMessage());
        }
        return "redirect:/rbac/roles/list";
    }
    
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Role> roleOpt = roleRepository.findById(id);
            if (roleOpt.isPresent()) {
                roleRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Role deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Role not found");
            }
        } catch (Exception e) {
            log.error("Error deleting role", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting role: " + e.getMessage());
        }
        return "redirect:/rbac/roles/list";
    }
}