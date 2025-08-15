package id.ac.tazkia.minibank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PersonalCustomerCreateRequest {
    
    @NotBlank(message = "Customer number is required")
    @Size(max = 50, message = "Customer number must not exceed 50 characters")
    private String customerNumber;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date of birth must be in YYYY-MM-DD format")
    private String dateOfBirth;
    
    private String gender;
    
    @Size(max = 50, message = "Identity number must not exceed 50 characters")
    private String idNumber;
}