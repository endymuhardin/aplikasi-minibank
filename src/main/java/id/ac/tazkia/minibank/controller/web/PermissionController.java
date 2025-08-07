package id.ac.tazkia.minibank.controller.web;

import id.ac.tazkia.minibank.entity.Permission;
import id.ac.tazkia.minibank.entity.RolePermission;
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

@Controller
@RequestMapping("/rbac/permissions")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    
    @GetMapping("/list")
    public String permissionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String category,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Permission> permissions;
        if (category != null && !category.trim().isEmpty()) {
            permissions = permissionRepository.findByCategoryPage(category, pageable);
        } else {
            permissions = permissionRepository.findAll(pageable);
        }
        
        List<String> categories = permissionRepository.findDistinctCategories();
        
        model.addAttribute("permissions", permissions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", permissions.getTotalPages());
        model.addAttribute("totalItems", permissions.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("category", category);
        model.addAttribute("categories", categories);
        
        return "rbac/permissions/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("permission", new Permission());
        List<String> categories = permissionRepository.findDistinctCategories();
        model.addAttribute("categories", categories);
        return "rbac/permissions/form";
    }
    
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Permission permission, 
                        BindingResult result, 
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        if (permissionRepository.existsByPermissionCode(permission.getPermissionCode())) {
            result.rejectValue("permissionCode", "error.permission", "Permission code already exists");
        }
        
        if (result.hasErrors()) {
            List<String> categories = permissionRepository.findDistinctCategories();
            model.addAttribute("categories", categories);
            return "rbac/permissions/form";
        }
        
        try {
            permission.setCreatedBy("system");
            permissionRepository.save(permission);
            redirectAttributes.addFlashAttribute("successMessage", "Permission created successfully");
            return "redirect:/rbac/permissions/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating permission: " + e.getMessage());
            List<String> categories = permissionRepository.findDistinctCategories();
            model.addAttribute("categories", categories);
            return "rbac/permissions/form";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Permission> permission = permissionRepository.findById(id);
        if (permission.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Permission not found");
            return "redirect:/rbac/permissions/list";
        }
        
        List<String> categories = permissionRepository.findDistinctCategories();
        model.addAttribute("permission", permission.get());
        model.addAttribute("categories", categories);
        return "rbac/permissions/form";
    }
    
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable UUID id,
                      @Valid @ModelAttribute Permission permission, 
                      BindingResult result, 
                      Model model,
                      RedirectAttributes redirectAttributes) {
        
        Optional<Permission> existingPermission = permissionRepository.findById(id);
        if (existingPermission.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Permission not found");
            return "redirect:/rbac/permissions/list";
        }
        
        Permission existing = existingPermission.get();
        
        if (!existing.getPermissionCode().equals(permission.getPermissionCode()) && 
            permissionRepository.existsByPermissionCode(permission.getPermissionCode())) {
            result.rejectValue("permissionCode", "error.permission", "Permission code already exists");
        }
        
        if (result.hasErrors()) {
            List<String> categories = permissionRepository.findDistinctCategories();
            model.addAttribute("categories", categories);
            return "rbac/permissions/form";
        }
        
        try {
            existing.setPermissionCode(permission.getPermissionCode());
            existing.setPermissionName(permission.getPermissionName());
            existing.setPermissionCategory(permission.getPermissionCategory());
            existing.setDescription(permission.getDescription());
            existing.setResource(permission.getResource());
            existing.setAction(permission.getAction());
            
            permissionRepository.save(existing);
            redirectAttributes.addFlashAttribute("successMessage", "Permission updated successfully");
            return "redirect:/rbac/permissions/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating permission: " + e.getMessage());
            List<String> categories = permissionRepository.findDistinctCategories();
            model.addAttribute("categories", categories);
            return "rbac/permissions/form";
        }
    }
    
    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Permission> permission = permissionRepository.findById(id);
        if (permission.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Permission not found");
            return "redirect:/rbac/permissions/list";
        }
        
        List<RolePermission> rolePermissions = rolePermissionRepository.findByPermission(permission.get());
        
        model.addAttribute("permission", permission.get());
        model.addAttribute("rolePermissions", rolePermissions);
        return "rbac/permissions/view";
    }
    
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Permission> permissionOpt = permissionRepository.findById(id);
            if (permissionOpt.isPresent()) {
                permissionRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Permission deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Permission not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting permission: " + e.getMessage());
        }
        return "redirect:/rbac/permissions/list";
    }
}