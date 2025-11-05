package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "personal_customers", indexes = {
    @Index(name = "idx_personal_customers_identity_number_unique", columnList = "identity_number", unique = true)
})
@DiscriminatorValue("PERSONAL")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonalCustomer extends Customer {

    // Basic Personal Information
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'\\-]+$", message = "First name can only contain letters, spaces, apostrophes, dots, and hyphens")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'\\-]+$", message = "Last name can only contain letters, spaces, apostrophes, dots, and hyphens")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Size(max = 100, message = "Birth place must not exceed 100 characters")
    @Column(name = "birth_place", length = 100)
    private String birthPlace;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Size(max = 100, message = "Mother name must not exceed 100 characters")
    @Column(name = "mother_name", length = 100)
    private String motherName;

    // Personal Data - New fields as per FR.002
    @Enumerated(EnumType.STRING)
    @Column(name = "education", length = 20)
    private Education education;

    @Enumerated(EnumType.STRING)
    @Column(name = "religion", length = 20)
    private Religion religion;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @Min(value = 0, message = "Dependents cannot be negative")
    @Column(name = "dependents")
    private Integer dependents = 0;

    // Identity Information
    @NotBlank(message = "Identity number is required")
    @Size(min = 16, max = 16, message = "Identity number must be exactly 16 digits")
    @Pattern(regexp = "^\\d{16}$", message = "Identity number must be 16 digits")
    @Column(name = "identity_number", nullable = false, length = 50, unique = true)
    private String identityNumber;

    @NotNull(message = "Identity type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", nullable = false, length = 20)
    private IdentityType identityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "citizenship", length = 20)
    private Citizenship citizenship = Citizenship.WNI;

    @Size(max = 100, message = "Residency status must not exceed 100 characters")
    @Column(name = "residency_status", length = 100)
    private String residencyStatus;

    @Future(message = "Identity expiry date must be in the future")
    @Column(name = "identity_expiry_date")
    private LocalDate identityExpiryDate;

    @Size(max = 100, message = "Province must not exceed 100 characters")
    @Column(name = "province", length = 100)
    private String province;

    // Employment Data - New fields as per FR.002
    @Size(max = 100, message = "Occupation must not exceed 100 characters")
    @Column(name = "occupation", length = 100)
    private String occupation;

    @Size(max = 200, message = "Company name must not exceed 200 characters")
    @Column(name = "company_name", length = 200)
    private String companyName;

    @Size(max = 500, message = "Company address must not exceed 500 characters")
    @Column(name = "company_address", columnDefinition = "TEXT")
    private String companyAddress;

    @Size(max = 100, message = "Business field must not exceed 100 characters")
    @Column(name = "business_field", length = 100)
    private String businessField;

    @DecimalMin(value = "0.0", message = "Monthly income cannot be negative")
    @Column(name = "monthly_income", precision = 20, scale = 2)
    private BigDecimal monthlyIncome;

    @Size(max = 100, message = "Source of funds must not exceed 100 characters")
    @Column(name = "source_of_funds", length = 100)
    private String sourceOfFunds;

    @Size(max = 200, message = "Account purpose must not exceed 200 characters")
    @Column(name = "account_purpose", length = 200)
    private String accountPurpose;

    @Min(value = 0, message = "Estimated monthly transactions cannot be negative")
    @Column(name = "estimated_monthly_transactions")
    private Integer estimatedMonthlyTransactions;

    @DecimalMin(value = "0.0", message = "Estimated transaction amount cannot be negative")
    @Column(name = "estimated_transaction_amount", precision = 20, scale = 2)
    private BigDecimal estimatedTransactionAmount;

    // Enums
    public enum Gender {
        MALE, FEMALE
    }

    public enum Education {
        SD, SMP, SMA, D3, S1, S2, S3
    }

    public enum Religion {
        ISLAM, KRISTEN_PROTESTAN, KATOLIK, HINDU, BUDDHA, KONGHUCU, LAINNYA
    }

    public enum MaritalStatus {
        BELUM_KAWIN, KAWIN, CERAI_HIDUP, CERAI_MATI
    }

    public enum Citizenship {
        WNI, WNA
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.PERSONAL;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    /**
     * Calculate age from date of birth
     * @return age in years, or null if date of birth is not set
     */
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Validate that customer is at least 17 years old (FR.002 requirement)
     * @return true if customer is at least 17 years old
     */
    @AssertTrue(message = "Customer must be at least 17 years old")
    public boolean isAgeValid() {
        if (dateOfBirth == null) {
            return true; // Let @NotNull handle this
        }
        Integer age = getAge();
        return age != null && age >= 17;
    }

    /**
     * Get full name (convenience method)
     * @return full name combining first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}