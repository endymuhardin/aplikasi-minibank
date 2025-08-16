package id.ac.tazkia.minibank.controller.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {
    
    private final PersonalCustomerRepository personalCustomerRepository;
    private final CorporateCustomerRepository corporateCustomerRepository;
    private final BranchRepository branchRepository;
    
    public CustomerRestController(PersonalCustomerRepository personalCustomerRepository,
                                CorporateCustomerRepository corporateCustomerRepository,
                                BranchRepository branchRepository) {
        this.personalCustomerRepository = personalCustomerRepository;
        this.corporateCustomerRepository = corporateCustomerRepository;
        this.branchRepository = branchRepository;
    }

    @PostMapping("/personal/register")
    public ResponseEntity<Object> registerPersonalCustomer(@Valid @RequestBody PersonalCustomerRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        // Handle branch assignment
        if (request.getBranch() != null && request.getBranch().getId() != null) {
            Branch branch = branchRepository.findById(request.getBranch().getId())
                .orElse(null);
            if (branch == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("branch", "Branch not found with id: " + request.getBranch().getId());
                return ResponseEntity.badRequest().body(errors);
            }
            
            PersonalCustomer customer = new PersonalCustomer();
            customer.setCustomerNumber(request.getCustomerNumber());
            customer.setFirstName(request.getFirstName());
            customer.setLastName(request.getLastName());
            customer.setDateOfBirth(request.getDateOfBirth());
            customer.setIdentityNumber(request.getIdentityNumber());
            customer.setIdentityType(Customer.IdentityType.valueOf(request.getIdentityType()));
            customer.setEmail(request.getEmail());
            customer.setPhoneNumber(request.getPhoneNumber());
            customer.setAddress(request.getAddress());
            customer.setCity(request.getCity());
            customer.setPostalCode(request.getPostalCode());
            customer.setCountry(request.getCountry());
            customer.setBranch(branch);
            
            PersonalCustomer savedCustomer = personalCustomerRepository.save(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("branch", "Branch assignment is required");
            return ResponseEntity.badRequest().body(errors);
        }
    }

    @PostMapping("/corporate/register")
    public ResponseEntity<Object> registerCorporateCustomer(@Valid @RequestBody CorporateCustomerRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }
        
        // Handle branch assignment
        if (request.getBranch() != null && request.getBranch().getId() != null) {
            Branch branch = branchRepository.findById(request.getBranch().getId())
                .orElse(null);
            if (branch == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("branch", "Branch not found with id: " + request.getBranch().getId());
                return ResponseEntity.badRequest().body(errors);
            }
            
            CorporateCustomer customer = new CorporateCustomer();
            customer.setCustomerNumber(request.getCustomerNumber());
            customer.setCompanyName(request.getCompanyName());
            customer.setCompanyRegistrationNumber(request.getCompanyRegistrationNumber());
            customer.setTaxIdentificationNumber(request.getTaxIdentificationNumber());
            customer.setEmail(request.getEmail());
            customer.setPhoneNumber(request.getPhoneNumber());
            customer.setAddress(request.getAddress());
            customer.setCity(request.getCity());
            customer.setPostalCode(request.getPostalCode());
            customer.setCountry(request.getCountry());
            customer.setBranch(branch);
            
            CorporateCustomer savedCustomer = corporateCustomerRepository.save(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("branch", "Branch assignment is required");
            return ResponseEntity.badRequest().body(errors);
        }
    }

    @GetMapping("/personal/{id}")
    public ResponseEntity<PersonalCustomer> getPersonalCustomer(@PathVariable UUID id) {
        return personalCustomerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/corporate/{id}")
    public ResponseEntity<CorporateCustomer> getCorporateCustomer(@PathVariable UUID id) {
        return corporateCustomerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/personal")
    public ResponseEntity<List<PersonalCustomer>> getAllPersonalCustomers(@RequestParam(required = false) String search) {
        List<PersonalCustomer> customers;
        if (search != null && !search.trim().isEmpty()) {
            customers = personalCustomerRepository.findPersonalCustomersWithSearchTerm(search.trim());
        } else {
            customers = personalCustomerRepository.findAll();
        }
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/corporate")
    public ResponseEntity<List<CorporateCustomer>> getAllCorporateCustomers(@RequestParam(required = false) String search) {
        List<CorporateCustomer> customers;
        if (search != null && !search.trim().isEmpty()) {
            customers = corporateCustomerRepository.findCorporateCustomersWithSearchTerm(search.trim());
        } else {
            customers = corporateCustomerRepository.findAll();
        }
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/number/{customerNumber}")
    public ResponseEntity<Customer> getCustomerByNumber(@PathVariable String customerNumber) {
        Optional<PersonalCustomer> personalCustomer = personalCustomerRepository.findByCustomerNumber(customerNumber);
        if (personalCustomer.isPresent()) {
            return ResponseEntity.ok(personalCustomer.get());
        }
        
        Optional<CorporateCustomer> corporateCustomer = corporateCustomerRepository.findByCustomerNumber(customerNumber);
        if (corporateCustomer.isPresent()) {
            return ResponseEntity.ok(corporateCustomer.get());
        }
        
        return ResponseEntity.notFound().build();
    }
    
    // DTOs
    public static class PersonalCustomerRequest {
        @jakarta.validation.constraints.NotBlank(message = "Customer number is required")
        private String customerNumber;
        
        @jakarta.validation.constraints.NotBlank(message = "First name is required")
        private String firstName;
        
        @jakarta.validation.constraints.NotBlank(message = "Last name is required")
        private String lastName;
        
        @jakarta.validation.constraints.NotNull(message = "Date of birth is required")
        private java.time.LocalDate dateOfBirth;
        
        @jakarta.validation.constraints.NotBlank(message = "Identity number is required")
        private String identityNumber;
        
        @jakarta.validation.constraints.NotBlank(message = "Identity type is required")
        private String identityType;
        
        private String email;
        private String phoneNumber;
        private String address;
        private String city;
        private String postalCode;
        private String country;
        private BranchRef branch;
        
        // Getters and setters
        public String getCustomerNumber() { return customerNumber; }
        public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(java.time.LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public String getIdentityNumber() { return identityNumber; }
        public void setIdentityNumber(String identityNumber) { this.identityNumber = identityNumber; }
        public String getIdentityType() { return identityType; }
        public void setIdentityType(String identityType) { this.identityType = identityType; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public BranchRef getBranch() { return branch; }
        public void setBranch(BranchRef branch) { this.branch = branch; }
    }
    
    public static class CorporateCustomerRequest {
        @jakarta.validation.constraints.NotBlank(message = "Customer number is required")
        private String customerNumber;
        
        @jakarta.validation.constraints.NotBlank(message = "Company name is required")
        private String companyName;
        
        @jakarta.validation.constraints.NotBlank(message = "Company registration number is required")
        private String companyRegistrationNumber;
        
        private String taxIdentificationNumber;
        private String email;
        private String phoneNumber;
        private String address;
        private String city;
        private String postalCode;
        private String country;
        private BranchRef branch;
        
        // Getters and setters
        public String getCustomerNumber() { return customerNumber; }
        public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getCompanyRegistrationNumber() { return companyRegistrationNumber; }
        public void setCompanyRegistrationNumber(String companyRegistrationNumber) { this.companyRegistrationNumber = companyRegistrationNumber; }
        public String getTaxIdentificationNumber() { return taxIdentificationNumber; }
        public void setTaxIdentificationNumber(String taxIdentificationNumber) { this.taxIdentificationNumber = taxIdentificationNumber; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public BranchRef getBranch() { return branch; }
        public void setBranch(BranchRef branch) { this.branch = branch; }
    }
    
    public static class BranchRef {
        private UUID id;
        
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
    }
}