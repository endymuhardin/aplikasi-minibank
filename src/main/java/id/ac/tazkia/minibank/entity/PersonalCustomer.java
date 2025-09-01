package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "personal_customers")
@DiscriminatorValue("PERSONAL")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonalCustomer extends Customer {
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Identity number is required")
    @Size(max = 50, message = "Identity number must not exceed 50 characters")
    @Column(name = "identity_number", nullable = false, length = 50)
    private String identityNumber;
    
    @NotNull(message = "Identity type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", nullable = false, length = 20)
    private IdentityType identityType;
    
    @Size(max = 100, message = "Birth place must not exceed 100 characters")
    @Column(name = "birth_place", length = 100)
    private String birthPlace;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;
    
    @Size(max = 100, message = "Mother name must not exceed 100 characters")
    @Column(name = "mother_name", length = 100)
    private String motherName;
    
    @Size(max = 100, message = "Province must not exceed 100 characters")
    @Column(name = "province", length = 100)
    private String province;
    
    public enum Gender {
        MALE, FEMALE
    }
    
    @Override
    public CustomerType getCustomerType() {
        return CustomerType.PERSONAL;
    }
    
    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }
    
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
}