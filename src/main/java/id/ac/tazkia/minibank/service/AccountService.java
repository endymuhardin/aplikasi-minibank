package id.ac.tazkia.minibank.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.minibank.dto.AccountOpeningRequest;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for account management operations including account opening and transaction processing.
 * Handles complex business logic involving multiple entities and ensures proper audit trail.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SequenceNumberService sequenceNumberService;
    private final ApprovalService approvalService;
    private final AuditorAware<String> auditorAware;
    
    /**
     * Opens a new account without initial deposit.
     * Initial deposit will be handled separately via teller menu.
     * This method handles the complete account opening process including:
     * - Account creation with zero balance
     * - Submission for approval
     *
     * @param accountRequest the account opening request
     * @return the created account with zero balance
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if account opening fails
     */
    public Account openAccount(AccountOpeningRequest accountRequest) {
        log.info("Opening account for customer ID: {}", accountRequest.getCustomerId());

        // Validate and get customer
        Customer customer = validateAndGetCustomer(accountRequest.getCustomerId());

        // Validate and get product
        Product product = validateAndGetProduct(accountRequest.getProductId());

        // Validate product eligibility for customer
        validateProductEligibility(customer, product);

        // Generate account number
        String accountNumber = generateAccountNumber(customer);

        // Create account with zero balance
        Account account = createAccount(customer, product, accountRequest, accountNumber);

        // Set approval status to PENDING_APPROVAL
        account.setApprovalStatus(Account.ApprovalStatus.PENDING_APPROVAL);
        account.setStatus(Account.AccountStatus.INACTIVE);

        // Save account
        account = accountRepository.save(account);
        log.info("Account created with number: {}", accountNumber);

        // Create approval request using current authenticated user
        String requestedBy = auditorAware.getCurrentAuditor().orElse("SYSTEM");
        approvalService.createAccountApprovalRequest(account, requestedBy,
            "New account opening for " + customer.getDisplayName());

        log.info("Account created and submitted for approval. Account Number: {}, Balance: {}",
                accountNumber, account.getBalance());

        return account;
    }
    
    /**
     * Opens a corporate account without initial deposit.
     * Initial deposit will be handled separately via teller menu.
     */
    public Account openCorporateAccount(AccountOpeningRequest accountRequest) {
        log.info("Opening corporate account for customer ID: {}", accountRequest.getCustomerId());

        // Validate and get customer
        Customer customer = validateAndGetCustomer(accountRequest.getCustomerId());

        // Ensure customer is corporate
        if (customer.getCustomerType() != Customer.CustomerType.CORPORATE) {
            throw new IllegalArgumentException("Selected customer is not a corporate customer");
        }

        // Validate and get product
        Product product = validateAndGetProduct(accountRequest.getProductId());

        // Validate product eligibility for customer
        validateProductEligibility(customer, product);

        // Generate corporate account number with different prefix
        String accountNumber = generateCorporateAccountNumber();

        // Create account with zero balance
        Account account = createAccount(customer, product, accountRequest, accountNumber);

        // Set approval status to PENDING_APPROVAL
        account.setApprovalStatus(Account.ApprovalStatus.PENDING_APPROVAL);
        account.setStatus(Account.AccountStatus.INACTIVE);

        // Save account
        account = accountRepository.save(account);
        log.info("Corporate account created with number: {}", accountNumber);

        // Create approval request using current authenticated user
        String requestedBy = auditorAware.getCurrentAuditor().orElse("SYSTEM");
        approvalService.createAccountApprovalRequest(account, requestedBy,
            "New corporate account opening for " + customer.getDisplayName());

        log.info("Corporate account created and submitted for approval. Account Number: {}, Balance: {}",
                accountNumber, account.getBalance());

        return account;
    }
    
    /**
     * Get available products for a customer based on their type.
     */
    public List<Product> getAvailableProductsForCustomer(Customer customer) {
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
                    product.getProductType() == Product.ProductType.DEPOSITO_MUDHARABAH ||
                    product.getProductType() == Product.ProductType.SAVINGS ||
                    product.getProductType() == Product.ProductType.CHECKING
                )
                .toList();
    }
    
    private Customer validateAndGetCustomer(UUID customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer not found");
        }
        
        Customer customer = customerOpt.get();
        if (customer.getStatus() != Customer.CustomerStatus.ACTIVE) {
            throw new IllegalArgumentException("Customer is not active");
        }
        
        return customer;
    }
    
    private Product validateAndGetProduct(UUID productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }
        
        Product product = productOpt.get();
        if (!product.getIsActive()) {
            throw new IllegalArgumentException("Selected product is not active");
        }
        
        return product;
    }
    
    private void validateProductEligibility(Customer customer, Product product) {
        String allowedTypes = product.getAllowedCustomerTypes();
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            String customerType = customer.getCustomerType().name();
            if (!allowedTypes.contains(customerType)) {
                throw new IllegalArgumentException("Product not available for " + customerType + " customers");
            }
        }
    }
    
    private Account createAccount(Customer customer, Product product,
                                AccountOpeningRequest accountRequest, String accountNumber) {
        Account account = new Account();
        account.setCustomer(customer);
        account.setProduct(product);
        account.setBranch(customer.getBranch());
        account.setAccountNumber(accountNumber);
        account.setAccountName(accountRequest.getAccountName());
        account.setBalance(BigDecimal.ZERO); // Start with zero balance
        account.setStatus(Account.AccountStatus.INACTIVE); // Will be activated after approval
        account.setApprovalStatus(Account.ApprovalStatus.PENDING_APPROVAL);
        // AuditorAware will automatically set createdBy

        return account;
    }
    
    private String generateAccountNumber(Customer customer) {
        return customer.getCustomerType() == Customer.CustomerType.CORPORATE 
            ? generateCorporateAccountNumber()
            : sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "ACC");
    }
    
    private String generateCorporateAccountNumber() {
        return sequenceNumberService.generateNextSequence("CORPORATE_ACCOUNT_NUMBER", "CORP");
    }
}