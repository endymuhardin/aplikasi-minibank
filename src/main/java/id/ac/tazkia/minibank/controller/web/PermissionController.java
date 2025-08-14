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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/rbac/permissions")
@RequiredArgsConstructor
public class PermissionController {
    
    private static final String CATEGORIES_ATTR = "categories";
    private static final String PERMISSION_ATTR = "permission";
    private static final String PERMISSIONS_FORM_VIEW = "rbac/permissions/form";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String REDIRECT_PERMISSIONS_LIST = "redirect:/rbac/permissions/list";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String PERMISSION_NOT_FOUND_MSG = "Permission not found";
    
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
        model.addAttribute(CATEGORIES_ATTR, categories);
        
        return "rbac/permissions/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute(PERMISSION_ATTR, new Permission());
        List<String> categories = permissionRepository.findDistinctCategories();
        model.addAttribute(CATEGORIES_ATTR, categories);
        return PERMISSIONS_FORM_VIEW;
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
            model.addAttribute(CATEGORIES_ATTR, categories);
            return PERMISSIONS_FORM_VIEW;
        }
        
        try {
            permission.setCreatedBy("system");
            permissionRepository.save(permission);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Permission created successfully");
            return REDIRECT_PERMISSIONS_LIST;
        } catch (Exception e) {
            log.error("Error creating permission", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Error creating permission: " + e.getMessage());
            List<String> categories = permissionRepository.findDistinctCategories();
            model.addAttribute(CATEGORIES_ATTR, categories);
            return PERMISSIONS_FORM_VIEW;
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Permission> permission = permissionRepository.findById(id);
        if (permission.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, PERMISSION_NOT_FOUND_MSG);
            return REDIRECT_PERMISSIONS_LIST;
        }
        
        List<String> categories = permissionRepository.findDistinctCategories();
        model.addAttribute(PERMISSION_ATTR, permission.get());
        model.addAttribute(CATEGORIES_ATTR, categories);
        return PERMISSIONS_FORM_VIEW;
    }
    
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable UUID id,
                      @Valid @ModelAttribute Permission permission, 
                      BindingResult result, 
                      Model model,
                      RedirectAttributes redirectAttributes) {
        
        Optional<Permission> existingPermission = permissionRepository.findById(id);
        if (existingPermission.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, PERMISSION_NOT_FOUND_MSG);
            return REDIRECT_PERMISSIONS_LIST;
        }
        
        Permission existing = existingPermission.get();
        
        if (!existing.getPermissionCode().equals(permission.getPermissionCode()) && 
            permissionRepository.existsByPermissionCode(permission.getPermissionCode())) {
            result.rejectValue("permissionCode", "error.permission", "Permission code already exists");
        }
        
        if (result.hasErrors()) {
            List<String> categories = permissionRepository.findDistinctCategories();
            model.addAttribute(CATEGORIES_ATTR, categories);
            return PERMISSIONS_FORM_VIEW;
        }
        
        try {
            existing.setPermissionCode(permission.getPermissionCode());
            existing.setPermissionName(permission.getPermissionName());
            existing.setPermissionCategory(permission.getPermissionCategory());
            existing.setDescription(permission.getDescription());
            
            permissionRepository.save(existing);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Permission updated successfully");
            return REDIRECT_PERMISSIONS_LIST;
        } catch (Exception e) {
            log.error("Error updating permission", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Error updating permission: " + e.getMessage());
            List<String> categories = permissionRepository.findDistinctCategories();
            model.addAttribute(CATEGORIES_ATTR, categories);
            return PERMISSIONS_FORM_VIEW;
        }
    }
    
    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Permission> permission = permissionRepository.findById(id);
        if (permission.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, PERMISSION_NOT_FOUND_MSG);
            return REDIRECT_PERMISSIONS_LIST;
        }
        
        List<RolePermission> rolePermissions = rolePermissionRepository.findByPermission(permission.get());
        
        model.addAttribute(PERMISSION_ATTR, permission.get());
        model.addAttribute("rolePermissions", rolePermissions);
        return "rbac/permissions/view";
    }
    
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Permission> permissionOpt = permissionRepository.findById(id);
            if (permissionOpt.isPresent()) {
                permissionRepository.deleteById(id);
                redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Permission deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, PERMISSION_NOT_FOUND_MSG);
            }
        } catch (Exception e) {
            log.error("Error deleting permission", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error deleting permission: " + e.getMessage());
        }
        return REDIRECT_PERMISSIONS_LIST;
    }
}