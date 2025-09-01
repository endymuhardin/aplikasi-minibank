package id.ac.tazkia.minibank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CorporateCustomerCreateDto {
    
    // Corporate customer specific fields
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;
    
    @NotBlank(message = "Company registration number is required")
    @Size(max = 100, message = "Company registration number must not exceed 100 characters")
    private String companyRegistrationNumber;
    
    @Size(max = 50, message = "Tax identification number must not exceed 50 characters")
    private String taxIdentificationNumber;
    
    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    private String contactPersonName;
    
    @Size(max = 100, message = "Contact person title must not exceed 100 characters")
    private String contactPersonTitle;
    
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