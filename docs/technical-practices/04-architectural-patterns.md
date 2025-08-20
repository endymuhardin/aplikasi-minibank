# Architectural Patterns

### 1. Layered Architecture
- **Presentation Layer**: Controllers (REST and Web)
- **Business Layer**: Services and rich domain entities
- **Data Access Layer**: Spring Data JPA repositories
- **Database Layer**: PostgreSQL with Flyway migrations

### 2. Repository Pattern
```java
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomer(Customer customer);
    List<Account> findByCustomerId(UUID customerId);
    
    @Query("SELECT a FROM Account a WHERE a.status = :status")
    List<Account> findByStatus(@Param("status") Account.AccountStatus status);
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.customer.id = :customerId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByCustomerId(@Param("customerId") UUID customerId);
}
```

### 3. Domain-Driven Design Elements
- Rich domain entities with business methods
- Value objects for complex types (enums, money)
- Repository pattern for data access abstraction
- Domain services for complex business logic

### 4. Security Architecture
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .build();
    }
}
```
