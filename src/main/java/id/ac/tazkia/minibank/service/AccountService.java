package id.ac.tazkia.minibank.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.minibank.dto.AccountOpeningRequest;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
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
    private final TransactionRepository transactionRepository;
    private final SequenceNumberService sequenceNumberService;
    
    /**
     * Opens a new account with initial deposit transaction.
     * This method handles the complete account opening process including:
     * - Account creation with zero balance
     * - Initial deposit transaction creation
     * - Balance update using entity business method
     * 
     * @param accountRequest the account opening request
     * @return the created account with initial balance
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if account opening fails
     */
    public Account openAccount(AccountOpeningRequest accountRequest) {
        log.info("Opening account for customer ID: {} with initial deposit: {}", 
                accountRequest.getCustomerId(), accountRequest.getInitialDeposit());
        
        // Validate and get customer
        Customer customer = validateAndGetCustomer(accountRequest.getCustomerId());
        
        // Validate and get product
        Product product = validateAndGetProduct(accountRequest.getProductId());
        
        // Validate product eligibility for customer
        validateProductEligibility(customer, product);
        
        // Validate minimum deposit requirement
        validateMinimumDeposit(accountRequest.getInitialDeposit(), product, customer);
        
        // Generate account number
        String accountNumber = generateAccountNumber(customer);
        
        // Create account with zero balance initially
        Account account = createAccount(customer, product, accountRequest, accountNumber);
        
        // Save account first to get ID for transaction
        account = accountRepository.save(account);
        log.info("Account created with number: {}", accountNumber);
        
        // Create initial deposit transaction
        createInitialDepositTransaction(account, accountRequest.getInitialDeposit(), 
                                      accountRequest.getCreatedBy());
        
        // Use entity business method to deposit and update balance
        account.deposit(accountRequest.getInitialDeposit());
        
        // Save updated account balance
        account = accountRepository.save(account);
        log.info("Account opened successfully. Account Number: {}, Balance: {}", 
                accountNumber, account.getBalance());
        
        return account;
    }
    
    /**
     * Opens a corporate account with enhanced validation and different account number prefix.
     */
    public Account openCorporateAccount(AccountOpeningRequest accountRequest) {
        log.info("Opening corporate account for customer ID: {} with initial deposit: {}", 
                accountRequest.getCustomerId(), accountRequest.getInitialDeposit());
        
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
        
        // Validate corporate minimum deposit requirement (higher than personal)
        BigDecimal corporateMinimum = getCorporateMinimumDeposit(product);
        if (accountRequest.getInitialDeposit().compareTo(corporateMinimum) < 0) {
            throw new IllegalArgumentException(
                "Initial deposit for corporate accounts must be at least " + corporateMinimum);
        }
        
        // Generate corporate account number with different prefix
        String accountNumber = generateCorporateAccountNumber();
        
        // Create account with zero balance initially
        Account account = createAccount(customer, product, accountRequest, accountNumber);
        
        // Save account first to get ID for transaction
        account = accountRepository.save(account);
        log.info("Corporate account created with number: {}", accountNumber);
        
        // Create initial deposit transaction
        createInitialDepositTransaction(account, accountRequest.getInitialDeposit(), 
                                      accountRequest.getCreatedBy());
        
        // Use entity business method to deposit and update balance
        account.deposit(accountRequest.getInitialDeposit());
        
        // Save updated account balance
        account = accountRepository.save(account);
        log.info("Corporate account opened successfully. Account Number: {}, Balance: {}", 
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
    
    private String generateAccountNumber(Customer customer) {
    if (customer.getCustomerType() == Customer.CustomerType.CORPORATE) {
        return generateCorporateAccountNumber();
    }
    // Personal: pastikan unik walau ada data seed
    String candidate;
    int attempts = 0;
    do {
        candidate = sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "A");
        attempts++;
        if (attempts > 20) {
            throw new IllegalStateException("Gagal membuat nomor rekening unik setelah banyak percobaan");
        }
    } while (accountRepository.existsByAccountNumber(candidate));
    return candidate;
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
    
    private void validateMinimumDeposit(BigDecimal initialDeposit, Product product, Customer customer) {
        BigDecimal minimumRequired = customer.getCustomerType() == Customer.CustomerType.CORPORATE 
            ? getCorporateMinimumDeposit(product)
            : product.getMinimumOpeningBalance();
            
        if (initialDeposit.compareTo(minimumRequired) < 0) {
            throw new IllegalArgumentException(
                "Initial deposit must be at least " + minimumRequired);
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
        account.setStatus(Account.AccountStatus.ACTIVE);
        // AuditorAware will automatically set createdBy
        
        return account;
    }
    
    private void createInitialDepositTransaction(Account account, BigDecimal amount, String createdBy) {
        // Generate transaction number
        String transactionNumber = sequenceNumberService.generateNextSequence("TRANSACTION_NUMBER", "TXN");
        
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionNumber(transactionNumber);
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setCurrency("IDR");
        transaction.setBalanceBefore(BigDecimal.ZERO); // Account starts with zero
        transaction.setBalanceAfter(amount); // Will equal the deposit amount
        transaction.setDescription("Initial deposit for account opening");
        transaction.setReferenceNumber("ACCOUNT-OPENING-" + account.getAccountNumber());
        transaction.setChannel(Transaction.TransactionChannel.TELLER);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setProcessedDate(LocalDateTime.now());
        // AuditorAware will automatically set createdBy
        
        transactionRepository.save(transaction);
        log.info("Initial deposit transaction created: {} for amount: {}", transactionNumber, amount);
    }
    
    private BigDecimal getCorporateMinimumDeposit(Product product) {
        // Corporate accounts typically require higher minimums
        BigDecimal baseMinimum = product.getMinimumOpeningBalance();
        BigDecimal corporateMultiplier = new BigDecimal("5.0"); // 5x minimum for corporate
        return baseMinimum.multiply(corporateMultiplier);
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