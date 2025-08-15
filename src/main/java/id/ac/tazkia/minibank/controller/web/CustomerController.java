package id.ac.tazkia.minibank.controller.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.minibank.dto.CorporateCustomerCreateRequest;
import id.ac.tazkia.minibank.dto.PersonalCustomerCreateRequest;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String customerType,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Customer> customers;

        if (search != null && !search.trim().isEmpty()) {
            customers = customerRepository.findByCustomerNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
                search.trim(), search.trim(), pageable);
        } else if (customerType != null && !customerType.trim().isEmpty()) {
            customers = customerRepository.findByCustomerType(
                Customer.CustomerType.valueOf(customerType), pageable);
        } else {
            customers = customerRepository.findAll(pageable);
        }

        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        model.addAttribute("customerType", customerType);
        model.addAttribute("customerTypes", Customer.CustomerType.values());
        
        return "customer/list";
    }

    @GetMapping("/create")
    public String selectCustomerType() {
        return "customer/select-type";
    }
    
    @GetMapping("/create/personal")
    public String createPersonalForm(Model model) {
        model.addAttribute("formData", new PersonalCustomerCreateRequest());
        model.addAttribute("customer", new PersonalCustomer());
        return "customer/personal-form";
    }
    
    @GetMapping("/create/corporate")
    public String createCorporateForm(Model model) {
        model.addAttribute("formData", new CorporateCustomerCreateRequest());
        return "customer/corporate-form";
    }

    @PostMapping("/create/personal")
    public String createPersonal(@Valid @ModelAttribute PersonalCustomerCreateRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes, 
                                Model model) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            List<String> validationErrors = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                validationErrors.add(error.getDefaultMessage());
            }
            model.addAttribute("validationErrors", validationErrors);
            model.addAttribute("formData", request);
            model.addAttribute("customer", new PersonalCustomer());
            return "customer/personal-form";
        }

        // Check for duplicate customer number
        Optional<Customer> existing = customerRepository.findByCustomerNumber(request.getCustomerNumber());
        if (existing.isPresent()) {
            model.addAttribute("errorMessage", "Customer number already exists");
            model.addAttribute("formData", request);
            model.addAttribute("customer", new PersonalCustomer());
            return "customer/personal-form";
        }

        try {
            PersonalCustomer personalCustomer = new PersonalCustomer();
            
            // Copy common properties from DTO to entity
            BeanUtils.copyProperties(request, personalCustomer, "dateOfBirth", "idNumber");
            
            // Handle specific field mappings
            if (request.getDateOfBirth() != null && !request.getDateOfBirth().isEmpty()) {
                personalCustomer.setDateOfBirth(java.time.LocalDate.parse(request.getDateOfBirth()));
            }
            personalCustomer.setIdentityNumber(request.getIdNumber());
            personalCustomer.setIdentityType(Customer.IdentityType.KTP);

            customerRepository.save(personalCustomer);
            redirectAttributes.addFlashAttribute("successMessage", "Personal customer created successfully");
            return "redirect:/customer/list";
        } catch (Exception e) {
            log.error("Failed to create personal customer", e);
            model.addAttribute("errorMessage", "Failed to create customer: " + e.getMessage());
            model.addAttribute("formData", request);
            model.addAttribute("customer", new PersonalCustomer());
            return "customer/personal-form";
        }
    }
    
    @PostMapping("/create/corporate")
    public String createCorporate(@Valid @ModelAttribute CorporateCustomerCreateRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, 
                                 Model model) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            List<String> validationErrors = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                validationErrors.add(error.getDefaultMessage());
            }
            model.addAttribute("validationErrors", validationErrors);
            model.addAttribute("formData", request);
            return "customer/corporate-form";
        }

        // Check for duplicate customer number
        Optional<Customer> existing = customerRepository.findByCustomerNumber(request.getCustomerNumber());
        if (existing.isPresent()) {
            model.addAttribute("errorMessage", "Customer number already exists");
            model.addAttribute("formData", request);
            return "customer/corporate-form";
        }

        try {
            CorporateCustomer corporateCustomer = new CorporateCustomer();
            
            // Copy common properties from DTO to entity
            BeanUtils.copyProperties(request, corporateCustomer, "taxId");
            
            // Handle specific field mappings
            corporateCustomer.setTaxIdentificationNumber(request.getTaxId());
            corporateCustomer.setCompanyRegistrationNumber("REG-" + System.currentTimeMillis());

            customerRepository.save(corporateCustomer);
            redirectAttributes.addFlashAttribute("successMessage", "Corporate customer created successfully");
            return "redirect:/customer/list";
        } catch (Exception e) {
            log.error("Failed to create corporate customer", e);
            model.addAttribute("errorMessage", "Failed to create customer: " + e.getMessage());
            model.addAttribute("formData", request);
            return "customer/corporate-form";
        }
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            model.addAttribute("customer", customer);
            
            // Route to appropriate view template based on customer type
            if (customer.getCustomerType() == Customer.CustomerType.PERSONAL) {
                return "customer/personal-view";
            } else if (customer.getCustomerType() == Customer.CustomerType.CORPORATE) {
                return "customer/corporate-view";
            } else {
                // Fallback to unified view for unknown types
                return "customer/view";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
            return "redirect:/customer/list";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer c = customer.get();
            model.addAttribute("customer", c);
            model.addAttribute("customerTypes", Customer.CustomerType.values());
            
            // Route to appropriate edit form based on customer type
            if (c.getCustomerType() == Customer.CustomerType.PERSONAL) {
                // Ensure we pass the PersonalCustomer object for proper field access
                PersonalCustomer personalCustomer = (PersonalCustomer) c;
                model.addAttribute("customer", personalCustomer);
                model.addAttribute("formData", personalCustomer);
                return "customer/personal-form";
            } else if (c.getCustomerType() == Customer.CustomerType.CORPORATE) {
                // Ensure we pass the CorporateCustomer object for proper field access
                CorporateCustomer corporateCustomer = (CorporateCustomer) c;
                model.addAttribute("customer", corporateCustomer);
                model.addAttribute("formData", corporateCustomer);
                return "customer/corporate-form";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Unknown customer type");
                return "redirect:/customer/list";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
            return "redirect:/customer/list";
        }
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable UUID id, 
                        @RequestParam String customerNumber,
                        @RequestParam String email,
                        @RequestParam String phoneNumber,
                        @RequestParam(required = false) String address,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) String firstName,
                        @RequestParam(required = false) String lastName,
                        @RequestParam(required = false) String dateOfBirth,
                        @RequestParam(required = false) String idNumber,
                        @RequestParam(required = false) String companyName,
                        @RequestParam(required = false) String contactPersonName,
                        @RequestParam(required = false) String contactPersonTitle,
                        @RequestParam(required = false) String taxId,
                        RedirectAttributes redirectAttributes, 
                        Model model) {

        Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
        if (!existingCustomerOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
            return "redirect:/customer/list";
        }

        try {
            Customer existingCustomer = existingCustomerOpt.get();
            
            // Update common fields
            existingCustomer.setCustomerNumber(customerNumber);
            existingCustomer.setEmail(email);
            existingCustomer.setPhoneNumber(phoneNumber);
            existingCustomer.setAddress(address);
            existingCustomer.setCity(city);
            
            // Update type-specific fields
            if (existingCustomer instanceof PersonalCustomer) {
                PersonalCustomer personalCustomer = (PersonalCustomer) existingCustomer;
                personalCustomer.setFirstName(firstName);
                personalCustomer.setLastName(lastName);
                if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                    try {
                        personalCustomer.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                    } catch (java.time.format.DateTimeParseException e) {
                        model.addAttribute("errorMessage", "Invalid date format. Please use YYYY-MM-DD format");
                        model.addAttribute("customer", existingCustomer);
                        return "customer/personal-form";
                    }
                }
                personalCustomer.setIdentityNumber(idNumber);
            } else if (existingCustomer instanceof CorporateCustomer) {
                CorporateCustomer corporateCustomer = (CorporateCustomer) existingCustomer;
                corporateCustomer.setCompanyName(companyName);
                corporateCustomer.setContactPersonName(contactPersonName);
                corporateCustomer.setContactPersonTitle(contactPersonTitle);
                corporateCustomer.setTaxIdentificationNumber(taxId);
            }
            
            customerRepository.save(existingCustomer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully");
            return "redirect:/customer/list";
        } catch (Exception e) {
            log.error("Failed to update customer", e);
            model.addAttribute("errorMessage", "Failed to update customer: " + e.getMessage());
            
            // Return to appropriate form based on customer type
            Optional<Customer> customer = customerRepository.findById(id);
            if (customer.isPresent()) {
                model.addAttribute("customer", customer.get());
                if (customer.get().getCustomerType() == Customer.CustomerType.PERSONAL) {
                    return "customer/personal-form";
                } else {
                    return "customer/corporate-form";
                }
            }
            return "redirect:/customer/list";
        }
    }

    @PostMapping("/activate/{id}")
    public String activate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer c = customer.get();
            c.setStatus(Customer.CustomerStatus.ACTIVE);
            customerRepository.save(c);
            redirectAttributes.addFlashAttribute("successMessage", "Customer activated successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
        }
        return "redirect:/customer/list";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer c = customer.get();
            c.setStatus(Customer.CustomerStatus.INACTIVE);
            customerRepository.save(c);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deactivated successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
        }
        return "redirect:/customer/list";
    }
}