package id.ac.tazkia.minibank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PersonalCustomerCreateDto {
    
    // Personal customer specific fields
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Size(max = 100, message = "Place of birth must not exceed 100 characters")
    private String birthPlace;
    
    @Pattern(regexp = "^(MALE|FEMALE)?$", message = "Gender must be MALE or FEMALE")
    private String gender;
    
    @Size(max = 100, message = "Mother's name must not exceed 100 characters")
    private String motherName;
    
    @NotBlank(message = "Identity number is required")
    @Size(max = 50, message = "Identity number must not exceed 50 characters")
    private String identityNumber;
    
    @NotBlank(message = "Identity type is required")
    @Size(max = 20, message = "Identity type must not exceed 20 characters")
    private String identityType;
    
    // Common fields
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String postalCode;
    
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country = "Indonesia";
    
    // Note: customerNumber is NOT included as it will be auto-generated
    // Note: branch is set automatically based on logged-in user's branch
}