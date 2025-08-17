package id.ac.tazkia.minibank.controller.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.minibank.dto.AccountOpeningRequest;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.service.SequenceNumberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    
    private static final String ACCOUNT_FORM_VIEW = "account/form";
    private static final String ACCOUNT_LIST_REDIRECT = "redirect:/account/list";
    private static final String ACCOUNT_ATTR = "accountRequest";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String CUSTOMER_NOT_FOUND_MSG = "Customer not found";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found";
    
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final SequenceNumberService sequenceNumberService;
    
    @GetMapping("/list")
    public String accountList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Account> accounts;
        
        if (search != null && !search.trim().isEmpty()) {
            accounts = accountRepository.findByAccountNumberContainingIgnoreCaseOrAccountNameContainingIgnoreCase(
                search.trim(), search.trim(), pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            accounts = accountRepository.findByStatus(Account.AccountStatus.valueOf(status), pageable);
        } else {
            accounts = accountRepository.findAll(pageable);
        }
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("accountStatuses", Account.AccountStatus.values());
        
        return "account/list";
    }
    
    @GetMapping("/open")
    public String selectCustomer(@RequestParam(required = false) UUID customerId,
                                @RequestParam(required = false) String search,
                                Model model) {
        if (customerId != null) {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isPresent()) {
                return "redirect:/account/open/" + customerId;
            }
        }
        
        List<Customer> customers;
        if (search != null && !search.trim().isEmpty()) {
            customers = customerRepository.findByCustomerNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
                search.trim(), search.trim()).stream()
                .filter(customer -> customer.getStatus() == Customer.CustomerStatus.ACTIVE)
                .toList();
        } else {
            customers = customerRepository.findByStatus(Customer.CustomerStatus.ACTIVE);
        }
        
        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        return "account/select-customer";
    }
    
    @GetMapping("/open/{customerId}")
    public String openAccountForm(@PathVariable UUID customerId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
            return "redirect:/account/open";
        }
        
        Customer customer = customerOpt.get();
        
        AccountOpeningRequest accountRequest = new AccountOpeningRequest();
        accountRequest.setCustomerId(customerId);
        
        List<Product> availableProducts = getAvailableProductsForCustomer(customer);
        
        model.addAttribute(ACCOUNT_ATTR, accountRequest);
        model.addAttribute("customer", customer);
        model.addAttribute("availableProducts", availableProducts);
        
        return ACCOUNT_FORM_VIEW;
    }
    
    @PostMapping("/open")
    public String openAccount(@Valid @ModelAttribute AccountOpeningRequest accountRequest,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return prepareFormWithErrors(accountRequest, model, bindingResult);
        }
        
        try {
            Optional<Customer> customerOpt = customerRepository.findById(accountRequest.getCustomerId());
            if (customerOpt.isEmpty()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
                return prepareFormWithErrors(accountRequest, model, null);
            }
            
            Customer customer = customerOpt.get();
            
            Optional<Product> productOpt = productRepository.findById(accountRequest.getProductId());
            if (productOpt.isEmpty()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, PRODUCT_NOT_FOUND_MSG);
                return prepareFormWithErrors(accountRequest, model, null);
            }
            
            Product product = productOpt.get();
            
            if (!product.getIsActive()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, "Selected product is not active");
                return prepareFormWithErrors(accountRequest, model, null);
            }
            
            String allowedTypes = product.getAllowedCustomerTypes();
            if (allowedTypes != null && !allowedTypes.isEmpty()) {
                String customerType = customer.getCustomerType().name();
                if (!allowedTypes.contains(customerType)) {
                    model.addAttribute(ERROR_MESSAGE_ATTR, "Product not available for " + customerType + " customers");
                    return prepareFormWithErrors(accountRequest, model, null);
                }
            }
            
            if (accountRequest.getInitialDeposit().compareTo(product.getMinimumOpeningBalance()) < 0) {
                model.addAttribute(ERROR_MESSAGE_ATTR, 
                    "Initial deposit must be at least " + product.getMinimumOpeningBalance());
                return prepareFormWithErrors(accountRequest, model, null);
            }
            
            String accountNumber = generateAccountNumber();
            
            Account account = new Account();
            account.setCustomer(customer);
            account.setProduct(product);
            account.setBranch(customer.getBranch());
            account.setAccountNumber(accountNumber);
            account.setAccountName(accountRequest.getAccountName());
            account.setBalance(accountRequest.getInitialDeposit());
            account.setStatus(Account.AccountStatus.ACTIVE);
            account.setCreatedBy(accountRequest.getCreatedBy() != null ? accountRequest.getCreatedBy() : "SYSTEM");
            
            accountRepository.save(account);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, 
                "Account opened successfully. Account Number: " + accountNumber);
            return ACCOUNT_LIST_REDIRECT;
            
        } catch (Exception e) {
            log.error("Failed to open account", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Failed to open account: " + e.getMessage());
            return prepareFormWithErrors(accountRequest, model, null);
        }
    }
    
    private String prepareFormWithErrors(AccountOpeningRequest accountRequest, Model model, BindingResult bindingResult) {
        Optional<Customer> customerOpt = customerRepository.findById(accountRequest.getCustomerId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            List<Product> availableProducts = getAvailableProductsForCustomer(customer);
            
            model.addAttribute(ACCOUNT_ATTR, accountRequest);
            model.addAttribute("customer", customer);
            model.addAttribute("availableProducts", availableProducts);
        }
        
        return ACCOUNT_FORM_VIEW;
    }
    
    private List<Product> getAvailableProductsForCustomer(Customer customer) {
        List<Product> activeProducts = productRepository.findByIsActiveTrue();
        
        return activeProducts.stream()
                .filter(product -> {
                    String allowedTypes = product.getAllowedCustomerTypes();
                    if (allowedTypes == null || allowedTypes.isEmpty()) {
                        return true;
                    }
                    return allowedTypes.contains(customer.getCustomerType().name());
                })
                .filter(product -> 
                    product.getProductType() == Product.ProductType.TABUNGAN_WADIAH ||
                    product.getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH ||
                    product.getProductType() == Product.ProductType.SAVINGS ||
                    product.getProductType() == Product.ProductType.CHECKING
                )
                .toList();
    }
    
    // Corporate Account Opening Endpoints
    
    @GetMapping("/open/corporate")
    public String selectCorporateCustomer(@RequestParam(required = false) UUID customerId,
                                         @RequestParam(required = false) String search,
                                         Model model) {
        if (customerId != null) {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isPresent() && customerOpt.get().getCustomerType() == Customer.CustomerType.CORPORATE) {
                return "redirect:/account/open/corporate/" + customerId;
            }
        }
        
        List<Customer> corporateCustomers;
        if (search != null && !search.trim().isEmpty()) {
            corporateCustomers = customerRepository.findByCustomerNumberContainingIgnoreCaseOrEmailContainingIgnoreCase(
                search.trim(), search.trim()).stream()
                .filter(customer -> customer.getStatus() == Customer.CustomerStatus.ACTIVE)
                .filter(customer -> customer.getCustomerType() == Customer.CustomerType.CORPORATE)
                .toList();
        } else {
            corporateCustomers = customerRepository.findByStatus(Customer.CustomerStatus.ACTIVE).stream()
                .filter(customer -> customer.getCustomerType() == Customer.CustomerType.CORPORATE)
                .toList();
        }
        
        model.addAttribute("customers", corporateCustomers);
        model.addAttribute("search", search);
        model.addAttribute("customerType", "CORPORATE");
        return "account/select-corporate-customer";
    }
    
    @GetMapping("/open/corporate/{customerId}")
    public String openCorporateAccountForm(@PathVariable UUID customerId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
            return "redirect:/account/open/corporate";
        }
        
        Customer customer = customerOpt.get();
        if (customer.getCustomerType() != Customer.CustomerType.CORPORATE) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Selected customer is not a corporate customer");
            return "redirect:/account/open/corporate";
        }
        
        AccountOpeningRequest accountRequest = new AccountOpeningRequest();
        accountRequest.setCustomerId(customerId);
        
        List<Product> availableProducts = getAvailableProductsForCorporateCustomer(customer);
        
        model.addAttribute(ACCOUNT_ATTR, accountRequest);
        model.addAttribute("customer", customer);
        model.addAttribute("corporateCustomer", (CorporateCustomer) customer);
        model.addAttribute("availableProducts", availableProducts);
        model.addAttribute("customerType", "CORPORATE");
        
        return "account/corporate-form";
    }
    
    @PostMapping("/open/corporate")
    public String openCorporateAccount(@Valid @ModelAttribute AccountOpeningRequest accountRequest,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return prepareCorporateFormWithErrors(accountRequest, model, bindingResult);
        }
        
        try {
            Optional<Customer> customerOpt = customerRepository.findById(accountRequest.getCustomerId());
            if (customerOpt.isEmpty()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, CUSTOMER_NOT_FOUND_MSG);
                return prepareCorporateFormWithErrors(accountRequest, model, null);
            }
            
            Customer customer = customerOpt.get();
            if (customer.getCustomerType() != Customer.CustomerType.CORPORATE) {
                model.addAttribute(ERROR_MESSAGE_ATTR, "Selected customer is not a corporate customer");
                return prepareCorporateFormWithErrors(accountRequest, model, null);
            }
            
            Optional<Product> productOpt = productRepository.findById(accountRequest.getProductId());
            if (productOpt.isEmpty()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, PRODUCT_NOT_FOUND_MSG);
                return prepareCorporateFormWithErrors(accountRequest, model, null);
            }
            
            Product product = productOpt.get();
            
            if (!product.getIsActive()) {
                model.addAttribute(ERROR_MESSAGE_ATTR, "Selected product is not active");
                return prepareCorporateFormWithErrors(accountRequest, model, null);
            }
            
            // Validate customer type eligibility
            String allowedTypes = product.getAllowedCustomerTypes();
            if (allowedTypes != null && !allowedTypes.isEmpty()) {
                String customerType = customer.getCustomerType().name();
                if (!allowedTypes.contains(customerType)) {
                    model.addAttribute(ERROR_MESSAGE_ATTR, "Product not available for " + customerType + " customers");
                    return prepareCorporateFormWithErrors(accountRequest, model, null);
                }
            }
            
            // Corporate-specific validation: higher minimum amounts
            BigDecimal corporateMinimum = getCorporateMinimumDeposit(product);
            if (accountRequest.getInitialDeposit().compareTo(corporateMinimum) < 0) {
                model.addAttribute(ERROR_MESSAGE_ATTR, 
                    "Initial deposit for corporate accounts must be at least " + corporateMinimum);
                return prepareCorporateFormWithErrors(accountRequest, model, null);
            }
            
            // Generate corporate account number with different prefix
            String accountNumber = generateCorporateAccountNumber();
            
            Account account = new Account();
            account.setCustomer(customer);
            account.setProduct(product);
            account.setBranch(customer.getBranch());
            account.setAccountNumber(accountNumber);
            account.setAccountName(accountRequest.getAccountName());
            account.setBalance(accountRequest.getInitialDeposit());
            account.setStatus(Account.AccountStatus.ACTIVE);
            account.setCreatedBy(accountRequest.getCreatedBy() != null ? accountRequest.getCreatedBy() : "SYSTEM");
            
            accountRepository.save(account);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, 
                "Corporate account opened successfully. Account Number: " + accountNumber);
            return ACCOUNT_LIST_REDIRECT;
            
        } catch (Exception e) {
            log.error("Failed to open corporate account", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Failed to open corporate account: " + e.getMessage());
            return prepareCorporateFormWithErrors(accountRequest, model, null);
        }
    }
    
    private String prepareCorporateFormWithErrors(AccountOpeningRequest accountRequest, Model model, BindingResult bindingResult) {
        Optional<Customer> customerOpt = customerRepository.findById(accountRequest.getCustomerId());
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            List<Product> availableProducts = getAvailableProductsForCorporateCustomer(customer);
            
            model.addAttribute(ACCOUNT_ATTR, accountRequest);
            model.addAttribute("customer", customer);
            model.addAttribute("corporateCustomer", (CorporateCustomer) customer);
            model.addAttribute("availableProducts", availableProducts);
            model.addAttribute("customerType", "CORPORATE");
        }
        
        return "account/corporate-form";
    }
    
    private List<Product> getAvailableProductsForCorporateCustomer(Customer customer) {
        List<Product> activeProducts = productRepository.findByIsActiveTrue();
        
        return activeProducts.stream()
                .filter(product -> {
                    String allowedTypes = product.getAllowedCustomerTypes();
                    if (allowedTypes == null || allowedTypes.isEmpty()) {
                        return true;
                    }
                    return allowedTypes.contains("CORPORATE");
                })
                .filter(product -> 
                    product.getProductType() == Product.ProductType.TABUNGAN_WADIAH ||
                    product.getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH ||
                    product.getProductType() == Product.ProductType.SAVINGS ||
                    product.getProductType() == Product.ProductType.CHECKING
                )
                .toList();
    }
    
    private BigDecimal getCorporateMinimumDeposit(Product product) {
        // Corporate accounts typically require higher minimums
        BigDecimal baseMinimum = product.getMinimumOpeningBalance();
        BigDecimal corporateMultiplier = new BigDecimal("5.0"); // 5x minimum for corporate
        return baseMinimum.multiply(corporateMultiplier);
    }
    
    private String generateCorporateAccountNumber() {
        return sequenceNumberService.generateNextSequence("CORPORATE_ACCOUNT_NUMBER", "CORP");
    }
    
    private String generateAccountNumber() {
        return sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "ACC");
    }
}