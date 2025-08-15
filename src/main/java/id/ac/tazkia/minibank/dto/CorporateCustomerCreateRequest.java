package id.ac.tazkia.minibank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CorporateCustomerCreateRequest {
    
    @NotBlank(message = "Customer number is required")
    @Size(max = 50, message = "Customer number must not exceed 50 characters")
    private String customerNumber;
    
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;
    
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
    
    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    private String contactPersonName;
    
    @Size(max = 100, message = "Contact person title must not exceed 100 characters")
    private String contactPersonTitle;
    
    @Size(max = 50, message = "Tax identification number must not exceed 50 characters")
    private String taxId;
    
}