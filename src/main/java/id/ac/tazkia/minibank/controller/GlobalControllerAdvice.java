package id.ac.tazkia.minibank.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Global controller advice to provide common model attributes to all views
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    
    private final UserRepository userRepository;
    
    /**
     * Add current user's role name to all views
     */
    @ModelAttribute
    public void addCurrentUserRole(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            
            String username = authentication.getName();
            userRepository.findByUsername(username)
                .ifPresent(user -> {
                    // Get the first active role for the user
                    user.getUserRoles().stream()
                        .filter(userRole -> userRole.getRole().getIsActive())
                        .findFirst()
                        .ifPresent(userRole -> {
                            model.addAttribute("currentUserRoleName", userRole.getRole().getRoleName());
                        });
                });
        }
    }
}