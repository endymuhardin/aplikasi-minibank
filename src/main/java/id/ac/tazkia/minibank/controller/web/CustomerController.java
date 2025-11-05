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

import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;
import id.ac.tazkia.minibank.service.SequenceNumberService;
import id.ac.tazkia.minibank.service.ApprovalService;
import id.ac.tazkia.minibank.dto.PersonalCustomerCreateDto;
import id.ac.tazkia.minibank.dto.CorporateCustomerCreateDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/customer")
public class CustomerController {

    // Template view constants
    private static final String PERSONAL_FORM_VIEW = "customer/personal-form";
    private static final String CORPORATE_FORM_VIEW = "customer/corporate-form";
    private static final String CUSTOMER_LIST_REDIRECT = "redirect:/customer/list";
    
    // Model attribute constants
    private static final String CUSTOMER_ATTR = "customer";
    private static final String VALIDATION_ERRORS_ATTR = "validationErrors";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String CREATED_DATE_ATTR = "createdDate";
    
    // Error message constants
    private static final String CUSTOMER_NOT_FOUND_MSG = "Customer not found";

    private final CustomerRepository customerRepository;
    private final PersonalCustomerRepository personalCustomerRepository;
    private final CorporateCustomerRepository corporateCustomerRepository;
    private final BranchRepository branchRepository;
    private final SequenceNumberService sequenceNumberService;
    private final ApprovalService approvalService;

    public CustomerController(CustomerRepository customerRepository,
                             PersonalCustomerRepository personalCustomerRepository,
                             CorporateCustomerRepository corporateCustomerRepository,
                             BranchRepository branchRepository,
                             SequenceNumberService sequenceNumberService,
                             ApprovalService approvalService) {
        this.customerRepository = customerRepository;
        this.personalCustomerRepository = personalCustomerRepository;
        this.corporateCustomerRepository = corporateCustomerRepository;
        this.branchRepository = branchRepository;
        this.sequenceNumberService = sequenceNumberService;
        this.approvalService = approvalService;
    }

    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String customerType,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(CREATED_DATE_ATTR).descending());
        Page<Customer> customers;

        if (search != null && !search.trim().isEmpty()) {
            // For now, use simple search by customer number or email
            // This works for all customer types without complex JPQL
            customers = customerRepository.findBySearchTerm(search.trim(), pageable);
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
        model.addAttribute(CUSTOMER_ATTR, new PersonalCustomerCreateDto());
        return PERSONAL_FORM_VIEW;
    }
    
    @GetMapping("/create/corporate")
    public String createCorporateForm(Model model) {
        model.addAttribute(CUSTOMER_ATTR, new CorporateCustomerCreateDto());
        return CORPORATE_FORM_VIEW;
    }

    @PostMapping("/create/personal")
    public String createPersonal(@Valid @ModelAttribute("customer") PersonalCustomerCreateDto personalCustomerDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes, 
                                Model model) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            List<String> validationErrors = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                validationErrors.add(error.getDefaultMessage());
            }
            model.addAttribute(VALIDATION_ERRORS_ATTR, validationErrors);
            model.addAttribute(CUSTOMER_ATTR, personalCustomerDto);
            return PERSONAL_FORM_VIEW;
        }

        try {
            // Convert DTO to entity
            PersonalCustomer personalCustomer = convertToPersonalCustomer(personalCustomerDto);

            // Auto-generate customer number
            String customerNumber = sequenceNumberService.generateNextSequence("CUSTOMER", "C");
            personalCustomer.setCustomerNumber(customerNumber);

            // Set approval status to PENDING_APPROVAL
            personalCustomer.setApprovalStatus(Customer.ApprovalStatus.PENDING_APPROVAL);
            personalCustomer.setStatus(Customer.CustomerStatus.INACTIVE);

            // Save customer
            Customer savedCustomer = customerRepository.save(personalCustomer);

            // Create approval request
            // TODO: Get actual username from security context when authentication is implemented
            String requestedBy = "customer-service";
            approvalService.createCustomerApprovalRequest(savedCustomer, requestedBy,
                "New personal customer registration");

            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR,
                "Personal customer created and submitted for approval");
            return CUSTOMER_LIST_REDIRECT;
        } catch (Exception e) {
            log.error("Failed to create personal customer: " + e.getMessage(), e);
            e.printStackTrace(); // Temporary debug output
            model.addAttribute(ERROR_MESSAGE_ATTR, "Failed to create customer: " + e.getMessage());
            model.addAttribute(CUSTOMER_ATTR, personalCustomerDto);
            return PERSONAL_FORM_VIEW;
        }
    }
    
    @PostMapping("/create/corporate")
    public String createCorporate(@Valid @ModelAttribute("customer") CorporateCustomerCreateDto corporateCustomerDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, 
                                 Model model) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            List<String> validationErrors = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                validationErrors.add(error.getDefaultMessage());
            }
            model.addAttribute(VALIDATION_ERRORS_ATTR, validationErrors);
            model.addAttribute(CUSTOMER_ATTR, corporateCustomerDto);
            return CORPORATE_FORM_VIEW;
        }

        try {
            // Convert DTO to entity
            CorporateCustomer corporateCustomer = convertToCorporateCustomer(corporateCustomerDto);

            // Auto-generate customer number
            String customerNumber = sequenceNumberService.generateNextSequence("CUSTOMER", "C");
            corporateCustomer.setCustomerNumber(customerNumber);

            // Set approval status to PENDING_APPROVAL
            corporateCustomer.setApprovalStatus(Customer.ApprovalStatus.PENDING_APPROVAL);
            corporateCustomer.setStatus(Customer.CustomerStatus.INACTIVE);

            // Save customer
            Customer savedCustomer = customerRepository.save(corporateCustomer);

            // Create approval request
            // TODO: Get actual username from security context when authentication is implemented
            String requestedBy = "customer-service";
            approvalService.createCustomerApprovalRequest(savedCustomer, requestedBy,
                "New corporate customer registration");

            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR,
                "Corporate customer created and submitted for approval");
            return CUSTOMER_LIST_REDIRECT;
        } catch (Exception e) {
            log.error("Failed to create corporate customer", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Failed to create customer: " + e.getMessage());
            model.addAttribute(CUSTOMER_ATTR, corporateCustomerDto);
            return CORPORATE_FORM_VIEW;
        }
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            model.addAttribute(CUSTOMER_ATTR, customer);
            
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
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
            return CUSTOMER_LIST_REDIRECT;
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer c = customer.get();
            model.addAttribute(CUSTOMER_ATTR, c);
            model.addAttribute("customerTypes", Customer.CustomerType.values());
            
            // Route to appropriate edit form based on customer type
            if (c.getCustomerType() == Customer.CustomerType.PERSONAL) {
                // Ensure we pass the PersonalCustomer object for proper field access
                PersonalCustomer personalCustomer = (PersonalCustomer) c;
                model.addAttribute(CUSTOMER_ATTR, personalCustomer);
                    return PERSONAL_FORM_VIEW;
            } else if (c.getCustomerType() == Customer.CustomerType.CORPORATE) {
                // Ensure we pass the CorporateCustomer object for proper field access
                CorporateCustomer corporateCustomer = (CorporateCustomer) c;
                model.addAttribute(CUSTOMER_ATTR, corporateCustomer);
                    return CORPORATE_FORM_VIEW;
            } else {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Unknown customer type");
                return CUSTOMER_LIST_REDIRECT;
            }
        } else {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
            return CUSTOMER_LIST_REDIRECT;
        }
    }

    @PostMapping("/update/personal/{id}")
    public String updatePersonal(@PathVariable UUID id,
                                @Valid @ModelAttribute PersonalCustomer updatedCustomer,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
        if (existingCustomerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
            return CUSTOMER_LIST_REDIRECT;
        }

        Customer existingCustomer = existingCustomerOpt.get();
        if (!(existingCustomer instanceof PersonalCustomer)) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Customer is not a personal customer");
            return CUSTOMER_LIST_REDIRECT;
        }

        PersonalCustomer personalCustomer = (PersonalCustomer) existingCustomer;

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            List<String> validationErrors = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                validationErrors.add(error.getDefaultMessage());
            }
            model.addAttribute(VALIDATION_ERRORS_ATTR, validationErrors);
            model.addAttribute(CUSTOMER_ATTR, updatedCustomer);
            return PERSONAL_FORM_VIEW;
        }

        try {
            // Copy properties from form data to existing entity, preserving ID and audit fields
            BeanUtils.copyProperties(updatedCustomer, personalCustomer, "id", CREATED_DATE_ATTR, "createdBy", "accounts");
            
            customerRepository.save(personalCustomer);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Personal customer updated successfully");
            return CUSTOMER_LIST_REDIRECT;

        } catch (Exception e) {
            log.error("Failed to update personal customer", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Failed to update customer: " + e.getMessage());
            model.addAttribute(CUSTOMER_ATTR, personalCustomer);
            return PERSONAL_FORM_VIEW;
        }
    }

    @PostMapping("/update/corporate/{id}")
    public String updateCorporate(@PathVariable UUID id,
                                 @Valid @ModelAttribute CorporateCustomer updatedCustomer,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
        if (existingCustomerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
            return CUSTOMER_LIST_REDIRECT;
        }

        Customer existingCustomer = existingCustomerOpt.get();
        if (!(existingCustomer instanceof CorporateCustomer)) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Customer is not a corporate customer");
            return CUSTOMER_LIST_REDIRECT;
        }

        CorporateCustomer corporateCustomer = (CorporateCustomer) existingCustomer;

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            List<String> validationErrors = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                validationErrors.add(error.getDefaultMessage());
            }
            model.addAttribute(VALIDATION_ERRORS_ATTR, validationErrors);
            model.addAttribute(CUSTOMER_ATTR, updatedCustomer);
            return CORPORATE_FORM_VIEW;
        }

        try {
            // Copy properties from form data to existing entity, preserving ID and audit fields
            BeanUtils.copyProperties(updatedCustomer, corporateCustomer, "id", CREATED_DATE_ATTR, "createdBy", "accounts");
            
            customerRepository.save(corporateCustomer);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Corporate customer updated successfully");
            return CUSTOMER_LIST_REDIRECT;

        } catch (Exception e) {
            log.error("Failed to update corporate customer", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Failed to update customer: " + e.getMessage());
            model.addAttribute(CUSTOMER_ATTR, corporateCustomer);
            return CORPORATE_FORM_VIEW;
        }
    }

    @PostMapping("/activate/{id}")
    public String activate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer c = customer.get();
            c.setStatus(Customer.CustomerStatus.ACTIVE);
            customerRepository.save(c);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Customer activated successfully");
        } else {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
        }
        return CUSTOMER_LIST_REDIRECT;
    }

    @PostMapping("/deactivate/{id}")
    public String deactivate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer c = customer.get();
            c.setStatus(Customer.CustomerStatus.INACTIVE);
            customerRepository.save(c);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Customer deactivated successfully");
        } else {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
        }
        return CUSTOMER_LIST_REDIRECT;
    }
    
    // DTO to Entity conversion methods
    private PersonalCustomer convertToPersonalCustomer(PersonalCustomerCreateDto dto) {
        PersonalCustomer customer = new PersonalCustomer();

        // Basic Personal Information
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setBirthPlace(dto.getBirthPlace());
        if (dto.getGender() != null && !dto.getGender().trim().isEmpty()) {
            customer.setGender(PersonalCustomer.Gender.valueOf(dto.getGender()));
        }
        customer.setMotherName(dto.getMotherName());

        // Personal Data - New fields
        if (dto.getEducation() != null && !dto.getEducation().trim().isEmpty()) {
            customer.setEducation(PersonalCustomer.Education.valueOf(dto.getEducation()));
        }
        if (dto.getReligion() != null && !dto.getReligion().trim().isEmpty()) {
            customer.setReligion(PersonalCustomer.Religion.valueOf(dto.getReligion()));
        }
        if (dto.getMaritalStatus() != null && !dto.getMaritalStatus().trim().isEmpty()) {
            customer.setMaritalStatus(PersonalCustomer.MaritalStatus.valueOf(dto.getMaritalStatus()));
        }
        customer.setDependents(dto.getDependents());

        // Identity Information
        customer.setIdentityNumber(dto.getIdentityNumber());
        customer.setIdentityType(Customer.IdentityType.valueOf(dto.getIdentityType()));
        if (dto.getCitizenship() != null && !dto.getCitizenship().trim().isEmpty()) {
            customer.setCitizenship(PersonalCustomer.Citizenship.valueOf(dto.getCitizenship()));
        }
        customer.setResidencyStatus(dto.getResidencyStatus());
        customer.setIdentityExpiryDate(dto.getIdentityExpiryDate());
        customer.setProvince(dto.getProvince());

        // Employment Data - New fields
        customer.setOccupation(dto.getOccupation());
        customer.setCompanyName(dto.getCompanyName());
        customer.setCompanyAddress(dto.getCompanyAddress());
        customer.setBusinessField(dto.getBusinessField());
        customer.setMonthlyIncome(dto.getMonthlyIncome());
        customer.setSourceOfFunds(dto.getSourceOfFunds());
        customer.setAccountPurpose(dto.getAccountPurpose());
        customer.setEstimatedMonthlyTransactions(dto.getEstimatedMonthlyTransactions());
        customer.setEstimatedTransactionAmount(dto.getEstimatedTransactionAmount());

        // Common fields
        customer.setAliasName(dto.getAliasName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setPostalCode(dto.getPostalCode());
        customer.setCountry(dto.getCountry());

        // Set branch - TODO: Get from logged-in user's branch when authentication is implemented
        // For now, use the first active branch as default
        List<Branch> activeBranches = branchRepository.findActiveBranches();
        if (activeBranches.isEmpty()) {
            throw new IllegalStateException("No active branches found in the system");
        }
        Branch branch = activeBranches.get(0);
        customer.setBranch(branch);

        return customer;
    }
    
    private CorporateCustomer convertToCorporateCustomer(CorporateCustomerCreateDto dto) {
        CorporateCustomer customer = new CorporateCustomer();
        
        // Corporate customer specific fields
        customer.setCompanyName(dto.getCompanyName());
        customer.setCompanyRegistrationNumber(dto.getCompanyRegistrationNumber());
        customer.setTaxIdentificationNumber(dto.getTaxIdentificationNumber());
        customer.setContactPersonName(dto.getContactPersonName());
        customer.setContactPersonTitle(dto.getContactPersonTitle());
        
        // Common fields
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setPostalCode(dto.getPostalCode());
        customer.setCountry(dto.getCountry());
        
        // Set branch - TODO: Get from logged-in user's branch when authentication is implemented
        // For now, use the first active branch as default
        List<Branch> activeBranches = branchRepository.findActiveBranches();
        if (activeBranches.isEmpty()) {
            throw new IllegalStateException("No active branches found in the system");
        }
        Branch branch = activeBranches.get(0);
        customer.setBranch(branch);
        
        return customer;
    }
}