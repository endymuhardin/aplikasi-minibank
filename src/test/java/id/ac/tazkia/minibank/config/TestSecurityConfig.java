package id.ac.tazkia.minibank.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-specific security configuration that provides simplified authentication
 * for Selenium tests that require security to be enabled.
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test-security")
public class TestSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Create test users with different roles for testing
        UserDetails manager = User.builder()
            .username("manager")
            .password(passwordEncoder().encode("password123"))
            .authorities("ROLE_MANAGER", "USER_READ", "USER_CREATE", "USER_UPDATE", "PRODUCT_READ", "CUSTOMER_READ", "ACCOUNT_READ", "TRANSACTION_READ")
            .build();

        UserDetails cs = User.builder()
            .username("cs")
            .password(passwordEncoder().encode("password123"))
            .authorities("ROLE_CS", "CUSTOMER_READ", "CUSTOMER_CREATE", "CUSTOMER_UPDATE", "ACCOUNT_READ", "ACCOUNT_CREATE")
            .build();

        UserDetails teller = User.builder()
            .username("teller")
            .password(passwordEncoder().encode("password123"))
            .authorities("ROLE_TELLER", "TRANSACTION_READ", "TRANSACTION_CREATE", "ACCOUNT_READ", "CUSTOMER_READ")
            .build();

        // Additional test users
        UserDetails loginuser = User.builder()
            .username("loginuser")
            .password(passwordEncoder().encode("password123"))
            .authorities("ROLE_USER", "CUSTOMER_READ")
            .build();

        UserDetails logoutuser = User.builder()
            .username("logoutuser")
            .password(passwordEncoder().encode("password123"))
            .authorities("ROLE_USER", "CUSTOMER_READ")
            .build();

        UserDetails userinfo = User.builder()
            .username("userinfo")
            .password(passwordEncoder().encode("password123"))
            .authorities("ROLE_USER", "CUSTOMER_READ")
            .build();

        UserDetails statsuser = User.builder()
            .username("statsuser")
            .password(passwordEncoder().encode("password123"))
            .authorities("ROLE_USER", "CUSTOMER_READ")
            .build();

        // Users from CSV test data
        UserDetails validuser1 = User.builder()
            .username("validuser1")
            .password(passwordEncoder().encode("testpass123"))
            .authorities("ROLE_CS", "CUSTOMER_READ", "CUSTOMER_CREATE", "CUSTOMER_UPDATE", "ACCOUNT_READ", "ACCOUNT_CREATE")
            .build();

        UserDetails validuser2 = User.builder()
            .username("validuser2")
            .password(passwordEncoder().encode("testpass123"))
            .authorities("ROLE_TELLER", "TRANSACTION_READ", "TRANSACTION_CREATE", "ACCOUNT_READ", "CUSTOMER_READ")
            .build();

        UserDetails validuser3 = User.builder()
            .username("validuser3")
            .password(passwordEncoder().encode("testpass123"))
            .authorities("ROLE_MANAGER", "USER_READ", "USER_CREATE", "USER_UPDATE", "PRODUCT_READ", "CUSTOMER_READ", "ACCOUNT_READ", "TRANSACTION_READ")
            .build();

        return new InMemoryUserDetailsManager(manager, cs, teller, loginuser, logoutuser, userinfo, statsuser, validuser1, validuser2, validuser3);
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/assets/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/**").hasAnyAuthority("TRANSACTION_READ", "CUSTOMER_READ", "ACCOUNT_READ", "USER_READ")
                .requestMatchers("/rbac/**").hasAnyAuthority("USER_READ", "USER_CREATE", "USER_UPDATE")
                .requestMatchers("/product/**").hasAnyAuthority("PRODUCT_READ", "CUSTOMER_READ", "ACCOUNT_READ")
                .requestMatchers("/dashboard").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}