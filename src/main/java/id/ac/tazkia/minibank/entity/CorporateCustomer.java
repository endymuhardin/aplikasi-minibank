package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "corporate_customers")
@DiscriminatorValue("CORPORATE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CorporateCustomer extends Customer {
    
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;
    
    @NotBlank(message = "Company registration number is required")
    @Size(max = 100, message = "Company registration number must not exceed 100 characters")
    @Column(name = "company_registration_number", nullable = false, length = 100)
    private String companyRegistrationNumber;
    
    @Size(max = 50, message = "Tax identification number must not exceed 50 characters")
    @Column(name = "tax_identification_number", length = 50)
    private String taxIdentificationNumber;
    
    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    @Column(name = "contact_person_name", length = 100)
    private String contactPersonName;
    
    @Size(max = 100, message = "Contact person title must not exceed 100 characters")
    @Column(name = "contact_person_title", length = 100)
    private String contactPersonTitle;
    
    @Override
    public CustomerType getCustomerType() {
        return CustomerType.CORPORATE;
    }
    
    @Override
    public String getDisplayName() {
        return companyName;
    }
    
    public String getContactPersonFullName() {
        if (contactPersonName == null) {
            return null;
        }
        if (contactPersonTitle == null) {
            return contactPersonName;
        }
        return contactPersonName + " (" + contactPersonTitle + ")";
    }
}