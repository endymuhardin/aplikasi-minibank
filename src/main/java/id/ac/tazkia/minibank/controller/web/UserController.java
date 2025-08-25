package id.ac.tazkia.minibank.controller.web;

import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.Role;
import id.ac.tazkia.minibank.entity.UserRole;
import id.ac.tazkia.minibank.repository.UserRepository;
import id.ac.tazkia.minibank.repository.RoleRepository;
import id.ac.tazkia.minibank.repository.UserRoleRepository;
import id.ac.tazkia.minibank.repository.UserPasswordRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@RequestMapping("/rbac/users")
@RequiredArgsConstructor
public class UserController {
    
    private static final String REDIRECT_USERS_LIST = "redirect:/rbac/users/list";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String USER_NOT_FOUND_MSG = "User not found";
    private static final String ROLES_PATH = "/roles";
    private static final String REDIRECT_USERS_PREFIX = "redirect:/rbac/users/";
    private static final String PASSWORD_PATH = "/password";
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/list")
    public String userList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findUsersWithSearchTermPage(search.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        
        return "rbac/users/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("availableBranches", branchRepository.findActiveBranches());
        return "rbac/users/form";
    }
    
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute User user, 
                        BindingResult result, 
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("availableBranches", branchRepository.findActiveBranches());
            return "rbac/users/form";
        }
        
        if (userRepository.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("availableBranches", branchRepository.findActiveBranches());
            return "rbac/users/form";
        }
        
        try {
            // AuditorAware will automatically set createdBy and updatedBy
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "User created successfully. You can set a password from the user details page.");
            return REDIRECT_USERS_LIST;
        } catch (Exception e) {
            log.error("Error creating user", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Error creating user: " + e.getMessage());
            model.addAttribute("availableBranches", branchRepository.findActiveBranches());
            return "rbac/users/form";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, USER_NOT_FOUND_MSG);
            return REDIRECT_USERS_LIST;
        }
        
        model.addAttribute("user", user.get());
        model.addAttribute("availableBranches", branchRepository.findActiveBranches());
        return "rbac/users/form";
    }
    
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable UUID id,
                      @Valid @ModelAttribute User user, 
                      BindingResult result, 
                      Model model,
                      RedirectAttributes redirectAttributes) {
        
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, USER_NOT_FOUND_MSG);
            return REDIRECT_USERS_LIST;
        }
        
        User existing = existingUser.get();
        
        if (!existing.getUsername().equals(user.getUsername()) && 
            userRepository.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
        }
        
        if (!existing.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("availableBranches", branchRepository.findActiveBranches());
            return "rbac/users/form";
        }
        
        try {
            existing.setUsername(user.getUsername());
            existing.setEmail(user.getEmail());
            existing.setFullName(user.getFullName());
            existing.setIsActive(user.getIsActive());
            // AuditorAware will automatically set updatedBy
            
            userRepository.save(existing);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "User updated successfully");
            return REDIRECT_USERS_LIST;
        } catch (Exception e) {
            log.error("Error updating user", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Error updating user: " + e.getMessage());
            model.addAttribute("availableBranches", branchRepository.findActiveBranches());
            return "rbac/users/form";
        }
    }
    
    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, USER_NOT_FOUND_MSG);
            return REDIRECT_USERS_LIST;
        }
        
        List<UserRole> userRoles = userRoleRepository.findByUser(user.get());
        
        model.addAttribute("user", user.get());
        model.addAttribute("userRoles", userRoles);
        return "rbac/users/view";
    }
    
    @GetMapping("/{id}/roles")
    public String manageUserRoles(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, USER_NOT_FOUND_MSG);
            return REDIRECT_USERS_LIST;
        }
        
        List<UserRole> userRoles = userRoleRepository.findByUser(user.get());
        List<Role> allRoles = roleRepository.findActiveRoles();
        
        model.addAttribute("user", user.get());
        model.addAttribute("userRoles", userRoles);
        model.addAttribute("allRoles", allRoles);
        return "rbac/users/roles";
    }
    
    @PostMapping("/{id}/roles/assign")
    public String assignRole(@PathVariable UUID id,
                           @RequestParam UUID roleId,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            Role role = roleRepository.findById(roleId).orElse(null);
            
            if (user == null || role == null) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "User or role not found");
                return REDIRECT_USERS_PREFIX + id + ROLES_PATH;
            }
            
            if (userRoleRepository.existsByUserAndRole(user, role)) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "User already has this role");
                return REDIRECT_USERS_PREFIX + id + ROLES_PATH;
            }
            
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            // AuditorAware will automatically set assignedBy
            userRoleRepository.save(userRole);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Role assigned successfully");
        } catch (Exception e) {
            log.error("Error assigning role to user", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error assigning role: " + e.getMessage());
        }
        
        return REDIRECT_USERS_PREFIX + id + ROLES_PATH;
    }
    
    @PostMapping("/{id}/roles/remove")
    public String removeRole(@PathVariable UUID id,
                           @RequestParam UUID userRoleId,
                           RedirectAttributes redirectAttributes) {
        try {
            userRoleRepository.deleteById(userRoleId);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Role removed successfully");
        } catch (Exception e) {
            log.error("Error removing role from user", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error removing role: " + e.getMessage());
        }
        
        return REDIRECT_USERS_PREFIX + id + ROLES_PATH;
    }
    
    @PostMapping("/{id}/activate")
    public String activate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setIsActive(true);
                // AuditorAware will automatically set updatedBy
                userRepository.save(user);
                redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "User activated successfully");
            } else {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, USER_NOT_FOUND_MSG);
            }
        } catch (Exception e) {
            log.error("Error activating user", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error activating user: " + e.getMessage());
        }
        return REDIRECT_USERS_LIST;
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setIsActive(false);
                // AuditorAware will automatically set updatedBy
                userRepository.save(user);
                redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "User deactivated successfully");
            } else {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, USER_NOT_FOUND_MSG);
            }
        } catch (Exception e) {
            log.error("Error deactivating user", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error deactivating user: " + e.getMessage());
        }
        return REDIRECT_USERS_LIST;
    }
    
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                userRepository.deleteById(id);
                redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "User deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, USER_NOT_FOUND_MSG);
            }
        } catch (Exception e) {
            log.error("Error deleting user", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error deleting user: " + e.getMessage());
        }
        return REDIRECT_USERS_LIST;
    }
    
    @GetMapping("/{id}/password")
    public String passwordForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, USER_NOT_FOUND_MSG);
            return REDIRECT_USERS_LIST;
        }
        
        model.addAttribute("user", user.get());
        return "rbac/users/password";
    }
    
    @PostMapping("/{id}/password")
    public String changePassword(@PathVariable UUID id,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, USER_NOT_FOUND_MSG);
                return REDIRECT_USERS_LIST;
            }
            
            User user = userOpt.get();
            
            if (password == null || password.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Password cannot be empty");
                return REDIRECT_USERS_PREFIX + id + PASSWORD_PATH;
            }
            
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Passwords do not match");
                return REDIRECT_USERS_PREFIX + id + PASSWORD_PATH;
            }
            
            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Password must be at least 6 characters long");
                return REDIRECT_USERS_PREFIX + id + PASSWORD_PATH;
            }
            
            // Check if user already has a password
            Optional<id.ac.tazkia.minibank.entity.UserPassword> existingPassword = userPasswordRepository.findByUser(user);
            
            if (existingPassword.isPresent()) {
                // Update existing password
                id.ac.tazkia.minibank.entity.UserPassword userPassword = existingPassword.get();
                userPassword.setPasswordHash(passwordEncoder.encode(password));
                userPasswordRepository.save(userPassword);
            } else {
                // Create new password
                id.ac.tazkia.minibank.entity.UserPassword userPassword = new id.ac.tazkia.minibank.entity.UserPassword();
                userPassword.setUser(user);
                userPassword.setPasswordHash(passwordEncoder.encode(password));
                // AuditorAware will automatically set createdBy
                userPasswordRepository.save(userPassword);
            }
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Password updated successfully");
            return "redirect:/rbac/users/view/" + id;
        } catch (Exception e) {
            log.error("Error updating user password", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error updating password: " + e.getMessage());
            return REDIRECT_USERS_PREFIX + id + PASSWORD_PATH;
        }
    }
}