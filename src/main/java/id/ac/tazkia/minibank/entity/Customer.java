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
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;
    
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
    
    public enum IdentityType {
        KTP, PASSPORT, SIM
    }
}