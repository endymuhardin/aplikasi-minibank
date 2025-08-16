package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = {"userRoles", "password"})
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must not exceed 50 characters")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_locked")
    private Boolean isLocked = false;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Min(value = 0, message = "Failed login attempts cannot be negative")
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    // Branch relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_branches")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Branch branch;
    
    // Relationships
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private UserPassword password;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<UserRole> userRoles;
    
    // Business methods
    public boolean isAccountNonLocked() {
        return !isLocked || (lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil));
    }
    
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null) ? 1 : this.failedLoginAttempts + 1;
    }
    
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }
    
    public void lockAccount(int lockDurationMinutes) {
        this.isLocked = true;
        this.lockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }
}