package id.ac.tazkia.minibank.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementation of Spring Data's AuditorAware interface to automatically
 * populate audit fields (createdBy, updatedBy) with the current user.
 *
 * This eliminates the need for manually setting audit fields in controllers
 * and services, providing a centralized and consistent approach.
 *
 * Note: This class is instantiated via @Bean in JpaAuditingConfig,
 * not via @Component annotation.
 */
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String SYSTEM_USER = "SYSTEM";
    private static final String ANONYMOUS_USER = "ANONYMOUS";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            // Return SYSTEM for batch jobs or system operations
            return Optional.of(SYSTEM_USER);
        }
        
        String username = authentication.getName();
        
        // Handle anonymous user
        if ("anonymousUser".equals(username)) {
            return Optional.of(ANONYMOUS_USER);
        }
        
        // Return the actual username
        return Optional.of(username);
    }
}