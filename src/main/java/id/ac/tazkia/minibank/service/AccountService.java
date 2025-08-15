package id.ac.tazkia.minibank.service;

import id.ac.tazkia.minibank.dto.PersonalAccountOpeningRequest;
import id.ac.tazkia.minibank.entity.*;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AccountService {

    private final PersonalCustomerRepository personalCustomerRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final SequenceNumberService sequenceNumberService;

    public AccountService(PersonalCustomerRepository personalCustomerRepository,
                          AccountRepository accountRepository,
                          ProductRepository productRepository,
                          TransactionRepository transactionRepository,
                          SequenceNumberService sequenceNumberService) {
        this.personalCustomerRepository = personalCustomerRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
        this.sequenceNumberService = sequenceNumberService;
    }

    public Account openPersonalAccount(PersonalAccountOpeningRequest request) {
        // 1. Create and save customer
        PersonalCustomer customer = new PersonalCustomer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setIdentityNumber(request.getIdentityNumber());
        customer.setIdentityType(request.getIdentityType());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setPostalCode(request.getPostalCode());
        customer.setCountry(request.getCountry());
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customer.setCreatedBy(request.getCreatedBy());
        customer.setCustomerNumber(sequenceNumberService.generateNextSequence("CUSTOMER_NUMBER", "C"));
        personalCustomerRepository.save(customer);

        // 2. Validate product
        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }
        Product product = productOpt.get();
        if (!product.getIsActive()) {
            throw new IllegalArgumentException("Product is not active");
        }

        // 3. Validate minimum opening balance
        if (request.getInitialDeposit().compareTo(product.getMinimumOpeningBalance()) < 0) {
            throw new IllegalArgumentException("Initial deposit must be at least " + product.getMinimumOpeningBalance());
        }

        // 4. Create and save account
        Account account = new Account();
        account.setCustomer(customer);
        account.setProduct(product);
        account.setAccountNumber(sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "A"));
        account.setAccountName(customer.getDisplayName());
        account.setBalance(request.getInitialDeposit());
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setCreatedBy(request.getCreatedBy());
        accountRepository.save(account);

        // 5. Create initial deposit transaction
        if (request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            Transaction initialDeposit = new Transaction();
            initialDeposit.setAccount(account);
            initialDeposit.setTransactionDate(LocalDateTime.now());
            initialDeposit.setTransactionType(Transaction.TransactionType.DEPOSIT);
            initialDeposit.setAmount(request.getInitialDeposit());
            initialDeposit.setDescription("Initial Deposit");
            initialDeposit.setBalanceBefore(BigDecimal.ZERO);
            initialDeposit.setBalanceAfter(request.getInitialDeposit());
            initialDeposit.setCreatedBy(request.getCreatedBy());
            transactionRepository.save(initialDeposit);
        }

        return account;
    }
}
