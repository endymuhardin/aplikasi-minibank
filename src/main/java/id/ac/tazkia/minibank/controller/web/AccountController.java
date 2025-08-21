package id.ac.tazkia.minibank.controller.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.service.AccountService;
import id.ac.tazkia.minibank.service.AccountStatementService;
import id.ac.tazkia.minibank.service.AccountStatementPdfService;
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
    private final AccountService accountService;
    private final AccountStatementService accountStatementService;
    private final AccountStatementPdfService accountStatementPdfService;
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
        
        List<Product> availableProducts = accountService.getAvailableProductsForCustomer(customer);
        
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
            // Use service layer for account opening business logic
            Account account = accountService.openAccount(accountRequest);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, 
                "Account opened successfully. Account Number: " + account.getAccountNumber());
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
            List<Product> availableProducts = accountService.getAvailableProductsForCustomer(customer);
            
            model.addAttribute(ACCOUNT_ATTR, accountRequest);
            model.addAttribute("customer", customer);
            model.addAttribute("availableProducts", availableProducts);
            
            // Add validation errors if present
            if (bindingResult != null && bindingResult.hasErrors()) {
                List<String> validationErrors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .toList();
                model.addAttribute("validationErrors", validationErrors);
            }
        }
        
        return ACCOUNT_FORM_VIEW;
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
        
        List<Product> availableProducts = accountService.getAvailableProductsForCustomer(customer);
        
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
            // Use service layer for corporate account opening business logic
            Account account = accountService.openCorporateAccount(accountRequest);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, 
                "Corporate account opened successfully. Account Number: " + account.getAccountNumber());
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
            List<Product> availableProducts = accountService.getAvailableProductsForCustomer(customer);
            
            model.addAttribute(ACCOUNT_ATTR, accountRequest);
            model.addAttribute("customer", customer);
            model.addAttribute("corporateCustomer", (CorporateCustomer) customer);
            model.addAttribute("availableProducts", availableProducts);
            model.addAttribute("customerType", "CORPORATE");
        }
        
        return "account/corporate-form";
    }
    
    @GetMapping("/{accountId}/statement")
    public String accountStatementForm(@PathVariable UUID accountId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Account not found");
            return ACCOUNT_LIST_REDIRECT;
        }
        
        Account account = accountOpt.get();
        model.addAttribute("account", account);
        
        // Set default date range (last 3 months)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "account/statement-form";
    }
    
    @PostMapping("/{accountId}/statement/pdf")
    public ResponseEntity<byte[]> generateAccountStatementPdf(@PathVariable UUID accountId,
                                                             @RequestParam String startDate,
                                                             @RequestParam String endDate,
                                                             RedirectAttributes redirectAttributes) {
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Account account = accountOpt.get();
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            // Validate date range
            if (start.isAfter(end)) {
                return ResponseEntity.badRequest().build();
            }
            
            // Get transactions for the period
            List<Transaction> transactions = accountStatementService.getTransactionsByAccountAndDateRange(
                account.getId(), start, end
            );
            
            // Generate PDF
            byte[] pdfBytes = accountStatementPdfService.generateAccountStatementPdf(
                account, transactions, start, end
            );
            
            // Build filename
            String filename = String.format("statement_%s_%s_to_%s.pdf", 
                account.getAccountNumber(), startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            log.error("Failed to generate account statement PDF for account: {}", accountId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{accountId}/close")
    public String closeAccountForm(@PathVariable UUID accountId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Account not found");
            return ACCOUNT_LIST_REDIRECT;
        }
        
        Account account = accountOpt.get();
        
        if (account.isClosed()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Account is already closed");
            return ACCOUNT_LIST_REDIRECT;
        }
        
        model.addAttribute("account", account);
        return "account/close-form";
    }
    
    @PostMapping("/{accountId}/close")
    public String closeAccount(@PathVariable UUID accountId, 
                              @RequestParam(required = false) String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            Optional<Account> accountOpt = accountRepository.findById(accountId);
            if (accountOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Account not found");
                return ACCOUNT_LIST_REDIRECT;
            }
            
            Account account = accountOpt.get();
            account.closeAccount();
            accountRepository.save(account);
            
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, 
                "Account " + account.getAccountNumber() + " has been closed successfully");
            return ACCOUNT_LIST_REDIRECT;
            
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, e.getMessage());
            return "redirect:/account/" + accountId + "/close";
        } catch (Exception e) {
            log.error("Failed to close account: {}", accountId, e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Failed to close account: " + e.getMessage());
            return "redirect:/account/" + accountId + "/close";
        }
    }
    
}