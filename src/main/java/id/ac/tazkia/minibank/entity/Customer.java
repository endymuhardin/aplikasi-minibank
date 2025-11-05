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
@Table(name = "customers")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "customer_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
public abstract class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Customer number is required")
    @Size(max = 50, message = "Customer number must not exceed 50 characters")
    @Column(name = "customer_number", unique = true, nullable = false, length = 50)
    private String customerNumber;
    
    // Common fields
    @Size(max = 100, message = "Alias name must not exceed 100 characters")
    @Column(name = "alias_name", length = 100)
    private String aliasName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 13, message = "Phone number must be between 10 and 13 digits")
    @Pattern(regexp = "^08\\d{8,11}$", message = "Phone number must start with 08 and contain 10-13 digits")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 250, message = "Address must be between 10 and 250 characters")
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(name = "city", length = 100)
    private String city;

    @NotBlank(message = "Postal code is required")
    @Size(min = 5, max = 5, message = "Postal code must be exactly 5 digits")
    @Pattern(regexp = "^\\d{5}$", message = "Postal code must be 5 digits")
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    @Column(name = "country", length = 50)
    private String country = "Indonesia";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20, nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

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
    @JoinColumn(name = "id_branches", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Branch branch;
    
    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Account> accounts;
    
    // Abstract method to get customer type
    public abstract CustomerType getCustomerType();
    
    // Abstract method to get display name
    public abstract String getDisplayName();
    
    // Enums
    public enum CustomerType {
        PERSONAL, CORPORATE
    }
    
    public enum CustomerStatus {
        ACTIVE, INACTIVE, CLOSED, FROZEN
    }

    public enum ApprovalStatus {
        PENDING_APPROVAL, APPROVED, REJECTED
    }

    public enum IdentityType {
        KTP, PASSPORT, SIM
    }

    // Helper methods for approval status
    public boolean isPendingApproval() {
        return this.approvalStatus == ApprovalStatus.PENDING_APPROVAL;
    }

    public boolean isApproved() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.approvalStatus == ApprovalStatus.REJECTED;
    }
}