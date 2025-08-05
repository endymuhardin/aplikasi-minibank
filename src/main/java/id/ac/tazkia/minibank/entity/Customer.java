package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType;
    
    @Column(name = "customer_number", unique = true, nullable = false, length = 50)
    private String customerNumber;
    
    // Personal customer fields
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", length = 100)
    private String lastName;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "identity_number", length = 50)
    private String identityNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", length = 20)
    private IdentityType identityType;
    
    // Corporate customer fields
    @Column(name = "company_name", length = 200)
    private String companyName;
    
    @Column(name = "company_registration_number", length = 100)
    private String companyRegistrationNumber;
    
    @Column(name = "tax_identification_number", length = 50)
    private String taxIdentificationNumber;
    
    // Common fields
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "postal_code", length = 10)
    private String postalCode;
    
    @Column(name = "country", length = 50)
    private String country = "Indonesia";
    
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
    private List<Account> accounts;
    
    // Enums
    public enum CustomerType {
        PERSONAL, CORPORATE
    }
    
    public enum IdentityType {
        KTP, PASSPORT, SIM
    }
}