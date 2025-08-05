package id.ac.tazkia.minibank.controller.rest;

import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private PersonalCustomerRepository personalCustomerRepository;
    
    @Autowired
    private CorporateCustomerRepository corporateCustomerRepository;

    @PostMapping("/personal/register")
    public ResponseEntity<?> registerPersonalCustomer(@Valid @RequestBody PersonalCustomer customer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        
        try {
            PersonalCustomer savedCustomer = personalCustomerRepository.save(customer);
            return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to register personal customer: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/corporate/register")
    public ResponseEntity<?> registerCorporateCustomer(@Valid @RequestBody CorporateCustomer customer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        
        try {
            CorporateCustomer savedCustomer = corporateCustomerRepository.save(customer);
            return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to register corporate customer: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/personal/{id}")
    public ResponseEntity<PersonalCustomer> getPersonalCustomer(@PathVariable UUID id) {
        return personalCustomerRepository.findById(id)
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/corporate/{id}")
    public ResponseEntity<CorporateCustomer> getCorporateCustomer(@PathVariable UUID id) {
        return corporateCustomerRepository.findById(id)
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/personal")
    public ResponseEntity<List<PersonalCustomer>> getAllPersonalCustomers(@RequestParam(required = false) String search) {
        List<PersonalCustomer> customers;
        if (search != null && !search.trim().isEmpty()) {
            customers = personalCustomerRepository.findPersonalCustomersWithSearchTerm(search.trim());
        } else {
            customers = personalCustomerRepository.findAll();
        }
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    @GetMapping("/corporate")
    public ResponseEntity<List<CorporateCustomer>> getAllCorporateCustomers(@RequestParam(required = false) String search) {
        List<CorporateCustomer> customers;
        if (search != null && !search.trim().isEmpty()) {
            customers = corporateCustomerRepository.findCorporateCustomersWithSearchTerm(search.trim());
        } else {
            customers = corporateCustomerRepository.findAll();
        }
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    @GetMapping("/number/{customerNumber}")
    public ResponseEntity<Customer> getCustomerByNumber(@PathVariable String customerNumber) {
        Optional<PersonalCustomer> personalCustomer = personalCustomerRepository.findByCustomerNumber(customerNumber);
        if (personalCustomer.isPresent()) {
            return new ResponseEntity<>(personalCustomer.get(), HttpStatus.OK);
        }
        
        Optional<CorporateCustomer> corporateCustomer = corporateCustomerRepository.findByCustomerNumber(customerNumber);
        if (corporateCustomer.isPresent()) {
            return new ResponseEntity<>(corporateCustomer.get(), HttpStatus.OK);
        }
        
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}