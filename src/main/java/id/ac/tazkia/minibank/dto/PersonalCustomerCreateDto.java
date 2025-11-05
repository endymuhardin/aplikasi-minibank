package id.ac.tazkia.minibank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PersonalCustomerCreateDto {

    // Basic Personal Information
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'\\-]+$", message = "First name can only contain letters, spaces, apostrophes, dots, and hyphens")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'\\-]+$", message = "Last name can only contain letters, spaces, apostrophes, dots, and hyphens")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 100, message = "Place of birth must not exceed 100 characters")
    private String birthPlace;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "Gender must be MALE or FEMALE")
    private String gender;

    @Size(max = 100, message = "Mother's name must not exceed 100 characters")
    private String motherName;

    // Personal Data - New fields as per FR.002
    @Pattern(regexp = "^(SD|SMP|SMA|D3|S1|S2|S3)?$", message = "Education must be one of: SD, SMP, SMA, D3, S1, S2, S3")
    private String education;

    @Pattern(regexp = "^(ISLAM|KRISTEN_PROTESTAN|KATOLIK|HINDU|BUDDHA|KONGHUCU|LAINNYA)?$",
             message = "Religion must be one of: ISLAM, KRISTEN_PROTESTAN, KATOLIK, HINDU, BUDDHA, KONGHUCU, LAINNYA")
    private String religion;

    @Pattern(regexp = "^(BELUM_KAWIN|KAWIN|CERAI_HIDUP|CERAI_MATI)?$",
             message = "Marital status must be one of: BELUM_KAWIN, KAWIN, CERAI_HIDUP, CERAI_MATI")
    private String maritalStatus;

    @Min(value = 0, message = "Dependents cannot be negative")
    private Integer dependents;

    // Identity Information
    @NotBlank(message = "Identity number is required")
    @Size(min = 16, max = 16, message = "Identity number must be exactly 16 digits")
    @Pattern(regexp = "^\\d{16}$", message = "Identity number must be 16 digits")
    private String identityNumber;

    @NotBlank(message = "Identity type is required")
    @Pattern(regexp = "^(KTP|PASSPORT|SIM)$", message = "Identity type must be KTP, PASSPORT, or SIM")
    private String identityType;

    @Pattern(regexp = "^(WNI|WNA)?$", message = "Citizenship must be WNI or WNA")
    private String citizenship;

    @Size(max = 100, message = "Residency status must not exceed 100 characters")
    private String residencyStatus;

    @Future(message = "Identity expiry date must be in the future")
    private LocalDate identityExpiryDate;

    @Size(max = 100, message = "Province must not exceed 100 characters")
    private String province;

    // Employment Data - New fields as per FR.002
    @Size(max = 100, message = "Occupation must not exceed 100 characters")
    private String occupation;

    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    @Size(max = 500, message = "Company address must not exceed 500 characters")
    private String companyAddress;

    @Size(max = 100, message = "Business field must not exceed 100 characters")
    private String businessField;

    @DecimalMin(value = "0.0", message = "Monthly income cannot be negative")
    private BigDecimal monthlyIncome;

    @Size(max = 100, message = "Source of funds must not exceed 100 characters")
    private String sourceOfFunds;

    @Size(max = 200, message = "Account purpose must not exceed 200 characters")
    private String accountPurpose;

    @Min(value = 0, message = "Estimated monthly transactions cannot be negative")
    private Integer estimatedMonthlyTransactions;

    @DecimalMin(value = "0.0", message = "Estimated transaction amount cannot be negative")
    private BigDecimal estimatedTransactionAmount;

    // Common Address fields
    @Size(max = 100, message = "Alias name must not exceed 100 characters")
    private String aliasName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 13, message = "Phone number must be between 10 and 13 digits")
    @Pattern(regexp = "^08\\d{8,11}$", message = "Phone number must start with 08 and contain 10-13 digits")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 250, message = "Address must be between 10 and 250 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Postal code is required")
    @Size(min = 5, max = 5, message = "Postal code must be exactly 5 digits")
    @Pattern(regexp = "^\\d{5}$", message = "Postal code must be 5 digits")
    private String postalCode;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country = "Indonesia";

    // Note: customerNumber is NOT included as it will be auto-generated
    // Note: branch is set automatically based on logged-in user's branch
}