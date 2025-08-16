package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "branches")
@Data
@NoArgsConstructor
public class Branch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Branch code is required")
    @Size(max = 20, message = "Branch code must not exceed 20 characters")
    @Column(name = "branch_code", unique = true, nullable = false, length = 20)
    private String branchCode;
    
    @NotBlank(message = "Branch name is required")
    @Size(max = 100, message = "Branch name must not exceed 100 characters")
    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "city", length = 100)
    private String city;
    
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    @Column(name = "postal_code", length = 10)
    private String postalCode;
    
    @Size(max = 50, message = "Country must not exceed 50 characters")
    @Column(name = "country", length = 50)
    private String country = "Indonesia";
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;
    
    @Size(max = 100, message = "Manager name must not exceed 100 characters")
    @Column(name = "manager_name", length = 100)
    private String managerName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private BranchStatus status = BranchStatus.ACTIVE;
    
    @Column(name = "is_main_branch", nullable = false)
    private Boolean isMainBranch = false;
    
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
    
    // Relationships
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Customer> customers;
    
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Account> accounts;
    
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<User> users;
    
    // Enums
    public enum BranchStatus {
        ACTIVE, INACTIVE, CLOSED
    }
    
    // Business methods
    public String getDisplayName() {
        return branchName + " (" + branchCode + ")";
    }
    
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (this.address != null) {
            address.append(this.address);
        }
        if (this.city != null) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(this.city);
        }
        if (this.postalCode != null) {
            if (address.length() > 0) {
                address.append(" ");
            }
            address.append(this.postalCode);
        }
        if (this.country != null && !"Indonesia".equals(this.country)) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(this.country);
        }
        return address.toString();
    }
}