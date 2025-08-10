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
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {
    
    private final PersonalCustomerRepository personalCustomerRepository;
    private final CorporateCustomerRepository corporateCustomerRepository;
    
    public CustomerRestController(PersonalCustomerRepository personalCustomerRepository,
                                CorporateCustomerRepository corporateCustomerRepository) {
        this.personalCustomerRepository = personalCustomerRepository;
        this.corporateCustomerRepository = corporateCustomerRepository;
    }

    @PostMapping("/personal/register")
    public ResponseEntity<Object> registerPersonalCustomer(@Valid @RequestBody PersonalCustomer customer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        PersonalCustomer savedCustomer = personalCustomerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);

    }

    @PostMapping("/corporate/register")
    public ResponseEntity<Object> registerCorporateCustomer(@Valid @RequestBody CorporateCustomer customer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }
        
        CorporateCustomer savedCustomer = corporateCustomerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    
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
}