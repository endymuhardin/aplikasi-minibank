package id.ac.tazkia.minibank.config;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Test-specific security configuration that provides simplified authentication
 * for Selenium tests that require security to be enabled.
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test-security")
public class TestSecurityConfig {

    @Value("classpath:/fixtures/selenium/login_test_data.csv")
    private Resource loginTestData;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() throws IOException, CsvException {
        List<UserDetails> users = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(loginTestData.getInputStream()))) {
            reader.readNext(); // skip header
            List<String[]> records = reader.readAll();
            for (String[] record : records) {
                if (Boolean.parseBoolean(record[4])) {
                    users.add(User.builder()
                            .username(record[0])
                            .password(passwordEncoder().encode(record[3]))
                            .authorities(getAuthorities(record[2]))
                            .build());
                }
            }
        }
        return new InMemoryUserDetailsManager(users);
    }

    private String[] getAuthorities(String roleCode) {
        if (StringUtils.equalsIgnoreCase(roleCode, "BRANCH_MANAGER")) {
            return new String[]{"ROLE_MANAGER", "USER_READ", "USER_CREATE", "USER_UPDATE", "PRODUCT_READ", "CUSTOMER_READ", "ACCOUNT_READ", "TRANSACTION_READ"};
        } else if (StringUtils.equalsIgnoreCase(roleCode, "CUSTOMER_SERVICE")) {
            return new String[]{"ROLE_CS", "CUSTOMER_READ", "CUSTOMER_CREATE", "CUSTOMER_UPDATE", "ACCOUNT_READ", "ACCOUNT_CREATE"};
        } else if (StringUtils.equalsIgnoreCase(roleCode, "TELLER")) {
            return new String[]{"ROLE_TELLER", "TRANSACTION_READ", "TRANSACTION_CREATE", "ACCOUNT_READ", "CUSTOMER_READ"};
        }
        return new String[]{"ROLE_USER"};
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