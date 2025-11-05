package id.ac.tazkia.minibank.controller.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.tazkia.minibank.dto.AccountOpeningRequest;
import id.ac.tazkia.minibank.dto.AccountOpeningResponse;
import id.ac.tazkia.minibank.dto.AccountStatementRequest;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CorporateCustomerRepository;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.service.AccountStatementService;
import id.ac.tazkia.minibank.service.AccountStatementPdfService;
import id.ac.tazkia.minibank.service.SequenceNumberService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
public class AccountRestController {
    
    private static final String PRODUCT_ID_FIELD = "productId";
    
    private final PersonalCustomerRepository personalCustomerRepository;
    private final CorporateCustomerRepository corporateCustomerRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final SequenceNumberService sequenceNumberService;
    private final AccountStatementService accountStatementService;
    private final AccountStatementPdfService accountStatementPdfService;
    
    public AccountRestController(PersonalCustomerRepository personalCustomerRepository,
                               CorporateCustomerRepository corporateCustomerRepository,
                               AccountRepository accountRepository,
                               ProductRepository productRepository,
                               BranchRepository branchRepository,
                               SequenceNumberService sequenceNumberService,
                               AccountStatementService accountStatementService,
                               AccountStatementPdfService accountStatementPdfService) {
        this.personalCustomerRepository = personalCustomerRepository;
        this.corporateCustomerRepository = corporateCustomerRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
        this.sequenceNumberService = sequenceNumberService;
        this.accountStatementService = accountStatementService;
        this.accountStatementPdfService = accountStatementPdfService;
    }

    @PostMapping("/open")
    public ResponseEntity<Object> openAccount(@Valid @RequestBody AccountOpeningRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            // Validate customer exists
            Customer customer = null;
            Optional<PersonalCustomer> personalCustomer = personalCustomerRepository.findById(request.getCustomerId());
            if (personalCustomer.isPresent()) {
                customer = personalCustomer.get();
            } else {
                Optional<CorporateCustomer> corporateCustomer = corporateCustomerRepository.findById(request.getCustomerId());
                if (corporateCustomer.isPresent()) {
                    customer = corporateCustomer.get();
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("customerId", "Customer not found");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // Validate product exists and is active
            Optional<Product> productOpt = productRepository.findById(request.getProductId());
            if (productOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put(PRODUCT_ID_FIELD, "Product not found");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            
            Product product = productOpt.get();
            if (!product.getIsActive()) {
                Map<String, String> error = new HashMap<>();
                error.put(PRODUCT_ID_FIELD, "Product is not active");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            // Validate customer type eligibility
            String allowedTypes = product.getAllowedCustomerTypes();
            if (allowedTypes != null && !allowedTypes.isEmpty()) {
                String customerType = customer.getCustomerType().name();
                if (!allowedTypes.contains(customerType)) {
                    Map<String, String> error = new HashMap<>();
                    error.put(PRODUCT_ID_FIELD, "Product not available for " + customerType + " customers");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // Validate minimum opening balance
            if (request.getInitialDeposit().compareTo(product.getMinimumOpeningBalance()) < 0) {
                Map<String, String> error = new HashMap<>();
                error.put("initialDeposit", "Initial deposit must be at least " + product.getMinimumOpeningBalance());
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            // Generate account number
            String accountNumber = generateAccountNumber();

            // Create and save account
            Account account = new Account();
            account.setCustomer(customer);
            account.setProduct(product);
            account.setBranch(customer.getBranch()); // Use customer's branch
            account.setAccountNumber(accountNumber);
            account.setAccountName(request.getAccountName());
            account.setBalance(request.getInitialDeposit());
            account.setStatus(Account.AccountStatus.ACTIVE);
            // createdBy will be set automatically by JPA auditing

            Account savedAccount = accountRepository.save(account);

            // Build response
            AccountOpeningResponse response = new AccountOpeningResponse();
            response.setAccountId(savedAccount.getId());
            response.setAccountNumber(savedAccount.getAccountNumber());
            response.setAccountName(savedAccount.getAccountName());
            response.setBalance(savedAccount.getBalance());
            response.setStatus(savedAccount.getStatus());
            response.setOpenedDate(savedAccount.getOpenedDate());
            response.setCreatedDate(savedAccount.getCreatedDate());

            // Customer info
            AccountOpeningResponse.CustomerInfo customerInfo = new AccountOpeningResponse.CustomerInfo();
            customerInfo.setId(customer.getId());
            customerInfo.setCustomerNumber(customer.getCustomerNumber());
            customerInfo.setDisplayName(customer.getDisplayName());
            customerInfo.setCustomerType(customer.getCustomerType().name());
            response.setCustomer(customerInfo);

            // Product info
            AccountOpeningResponse.ProductInfo productInfo = new AccountOpeningResponse.ProductInfo();
            productInfo.setId(product.getId());
            productInfo.setProductCode(product.getProductCode());
            productInfo.setProductName(product.getProductName());
            productInfo.setProductType(product.getProductType().name());
            productInfo.setMinimumOpeningBalance(product.getMinimumOpeningBalance());
            productInfo.setProfitSharingRatio(product.getProfitSharingRatio());
            response.setProduct(productInfo);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Account opening failed", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Account opening failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private String generateAccountNumber() {
        return sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "ACC");
    }

    @PostMapping("/statement/pdf")
    public ResponseEntity<byte[]> generateAccountStatementPdf(@Valid @RequestBody AccountStatementRequest request, 
                                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().build();
        }

        try {
            // Validate that either accountId or accountNumber is provided
            if (request.getAccountId() == null && (request.getAccountNumber() == null || request.getAccountNumber().trim().isEmpty())) {
                return ResponseEntity.badRequest().build();
            }

            // Find account
            Optional<Account> accountOpt;
            if (request.getAccountId() != null) {
                accountOpt = accountStatementService.findAccountById(request.getAccountId());
            } else {
                accountOpt = accountStatementService.findAccountByAccountNumber(request.getAccountNumber().trim());
            }

            if (accountOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Account account = accountOpt.get();

            // Validate date range
            if (request.getStartDate().isAfter(request.getEndDate())) {
                return ResponseEntity.badRequest().build();
            }

            // Get transactions for the period
            List<Transaction> transactions = accountStatementService.getTransactionsByAccountAndDateRange(
                account.getId(), request.getStartDate(), request.getEndDate()
            );

            // Generate PDF
            byte[] pdfBytes = accountStatementPdfService.generateAccountStatementPdf(
                account, transactions, request.getStartDate(), request.getEndDate()
            );

            // Build filename
            String filename = String.format("statement_%s_%s_to_%s.pdf", 
                account.getAccountNumber(),
                request.getStartDate().toString(),
                request.getEndDate().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            log.error("Invalid request for account statement", e);
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Failed to generate account statement PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statement/pdf")
    public ResponseEntity<byte[]> generateAccountStatementPdfByParams(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String accountNumber,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        try {
            AccountStatementRequest request = new AccountStatementRequest();
            
            if (accountId != null && !accountId.trim().isEmpty()) {
                request.setAccountId(java.util.UUID.fromString(accountId));
            }
            if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                request.setAccountNumber(accountNumber.trim());
            }
            
            request.setStartDate(java.time.LocalDate.parse(startDate));
            request.setEndDate(java.time.LocalDate.parse(endDate));

            return generateAccountStatementPdf(request, new org.springframework.validation.BeanPropertyBindingResult(request, "accountStatementRequest"));
            
        } catch (Exception e) {
            log.error("Failed to parse account statement parameters", e);
            return ResponseEntity.badRequest().build();
        }
    }
}