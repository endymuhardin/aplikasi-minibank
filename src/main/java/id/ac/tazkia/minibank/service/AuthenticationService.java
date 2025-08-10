package id.ac.tazkia.minibank.service;

import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public void recordSuccessfulLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            user.resetFailedLoginAttempts();
            userRepository.save(user);
            log.info("Recorded successful login for user: {}", username);
        }
    }
    
    @Transactional
    public void recordFailedLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.incrementFailedLoginAttempts();
            
            if (user.getFailedLoginAttempts() >= 5) {
                user.lockAccount(30);
                log.warn("User account locked due to too many failed attempts: {}", username);
            }
            
            userRepository.save(user);
            log.warn("Recorded failed login attempt for user: {} (attempt: {})", 
                    username, user.getFailedLoginAttempts());
        }
    }
    
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }
    
    public Optional<User> getCurrentUser() {
        String username = getCurrentUsername();
        if ("anonymous".equals(username) || "anonymousUser".equals(username)) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }
    
    public boolean hasRole(String roleCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleCode));
    }
    
    public boolean hasPermission(String resource, String action) {
        String permissionCode = (resource + "_" + action).toUpperCase();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(permissionCode));
    }
}