package id.ac.tazkia.minibank.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import id.ac.tazkia.minibank.service.AuthenticationService;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private static final String LOGIN_PATH = "/login";

    private final DataSource dataSource;
    private final AuthenticationService authenticationService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager() {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        
        // Custom query to get user credentials
        manager.setUsersByUsernameQuery(
            "SELECT u.username, up.password_hash as password, " +
            "CASE WHEN u.is_active = true " +
            "AND (u.is_locked = false OR u.locked_until IS NULL OR u.locked_until < NOW()) " +
            "AND up.is_active = true " +
            "AND (up.password_expires_at IS NULL OR up.password_expires_at > NOW()) " +
            "THEN true ELSE false END as enabled " +
            "FROM users u INNER JOIN user_passwords up ON u.id = up.id_users " +
            "WHERE u.username = ?"
        );
        
        // Custom query to get user authorities (permissions)
        // Note: Spring Security only provides one parameter (username) to this query
        manager.setAuthoritiesByUsernameQuery(
            "SELECT u.username, p.permission_code as authority " +
            "FROM users u " +
            "JOIN user_roles ur ON u.id = ur.id_users " +
            "JOIN roles r ON ur.id_roles = r.id " +
            "JOIN role_permissions rp ON r.id = rp.id_roles " +
            "JOIN permissions p ON rp.id_permissions = p.id " +
            "WHERE u.username = ? AND u.is_active = true AND r.is_active = true " +
            "AND p.permission_code IS NOT NULL"
        );
        
        return manager;
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            authenticationService.recordSuccessfulLogin(authentication.getName());
            response.sendRedirect("/dashboard");
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            if (username != null) {
                authenticationService.recordFailedLogin(username);
            }
            response.sendRedirect("/login?error=true");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(LOGIN_PATH, "/assets/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/**").hasAnyAuthority("TRANSACTION_VIEW", "CUSTOMER_VIEW", "ACCOUNT_VIEW", "USER_VIEW")
                .requestMatchers("/rbac/**").hasAnyAuthority("USER_VIEW", "USER_CREATE", "USER_UPDATE")
                .requestMatchers("/product/**").hasAnyAuthority("PRODUCT_VIEW", "CUSTOMER_VIEW", "ACCOUNT_VIEW")
                .requestMatchers("/dashboard").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage(LOGIN_PATH)
                .loginProcessingUrl(LOGIN_PATH)
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            // Enable HTTP Basic authentication for API testing (Karate tests)
            .httpBasic(basic -> basic.realmName("Minibank API"))
            .userDetailsService(jdbcUserDetailsManager())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            );
        return http.build();
    }
}