package id.ac.tazkia.minibank.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.minibank.dto.AccountStatementRequest;
import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.entity.Transaction;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.PersonalCustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.repository.TransactionRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AccountStatementRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonalCustomerRepository personalCustomerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BranchRepository branchRepository;

    private PersonalCustomer testCustomer;
    private Product testProduct;
    private Branch testBranch;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Create test branch
        testBranch = new Branch();
        testBranch.setBranchCode("001");
        testBranch.setBranchName("Main Branch");
        testBranch.setAddress("Main Street 123");
        testBranch = branchRepository.save(testBranch);

        // Create test customer
        testCustomer = new PersonalCustomer();
        testCustomer.setCustomerNumber("CUST0000001");
        testCustomer.setEmail("test@example.com");
        testCustomer.setPhoneNumber("081234567890");
        testCustomer.setBranch(testBranch);
        testCustomer.setStatus(Customer.CustomerStatus.ACTIVE);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testCustomer.setIdentityNumber("1234567890123456");
        testCustomer.setIdentityType(Customer.IdentityType.KTP);
        testCustomer = personalCustomerRepository.save(testCustomer);

        // Create test product
        testProduct = new Product();
        testProduct.setProductCode("TABWADIAH001");
        testProduct.setProductName("Tabungan Wadiah");
        testProduct.setProductType(Product.ProductType.TABUNGAN_WADIAH);
        testProduct.setProductCategory("TABUNGAN");
        testProduct.setMinimumOpeningBalance(new BigDecimal("50000"));
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);

        // Create test account
        testAccount = new Account();
        testAccount.setCustomer(testCustomer);
        testAccount.setProduct(testProduct);
        testAccount.setBranch(testBranch);
        testAccount.setAccountNumber("ACC0000001");
        testAccount.setAccountName("John Doe Savings");
        testAccount.setBalance(new BigDecimal("100000"));
        testAccount.setStatus(Account.AccountStatus.ACTIVE);
        testAccount = accountRepository.save(testAccount);

        // Create some test transactions
        Transaction transaction1 = new Transaction();
        transaction1.setAccount(testAccount);
        transaction1.setTransactionNumber("TXN0000001");
        transaction1.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction1.setAmount(new BigDecimal("50000"));
        transaction1.setBalanceBefore(new BigDecimal("50000"));
        transaction1.setBalanceAfter(new BigDecimal("100000"));
        transaction1.setDescription("Initial deposit");
        transaction1.setTransactionDate(LocalDateTime.now().minusDays(1));
        transactionRepository.save(transaction1);

        Transaction transaction2 = new Transaction();
        transaction2.setAccount(testAccount);
        transaction2.setTransactionNumber("TXN0000002");
        transaction2.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction2.setAmount(new BigDecimal("10000"));
        transaction2.setBalanceBefore(new BigDecimal("100000"));
        transaction2.setBalanceAfter(new BigDecimal("90000"));
        transaction2.setDescription("ATM withdrawal");
        transaction2.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(transaction2);
    }

    @Test
    void shouldGenerateAccountStatementPdfWithAccountId() throws Exception {
        AccountStatementRequest request = new AccountStatementRequest();
        request.setAccountId(testAccount.getId());
        request.setStartDate(LocalDate.now().minusWeeks(1));
        request.setEndDate(LocalDate.now());

        mockMvc.perform(post("/api/accounts/statement/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void shouldGenerateAccountStatementPdfWithAccountNumber() throws Exception {
        AccountStatementRequest request = new AccountStatementRequest();
        request.setAccountNumber(testAccount.getAccountNumber());
        request.setStartDate(LocalDate.now().minusWeeks(1));
        request.setEndDate(LocalDate.now());

        mockMvc.perform(post("/api/accounts/statement/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void shouldGenerateAccountStatementPdfWithGetRequest() throws Exception {
        mockMvc.perform(get("/api/accounts/statement/pdf")
                .param("accountNumber", testAccount.getAccountNumber())
                .param("startDate", LocalDate.now().minusWeeks(1).toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void shouldReturnNotFoundForInvalidAccountId() throws Exception {
        AccountStatementRequest request = new AccountStatementRequest();
        request.setAccountId(java.util.UUID.randomUUID());
        request.setStartDate(LocalDate.now().minusWeeks(1));
        request.setEndDate(LocalDate.now());

        mockMvc.perform(post("/api/accounts/statement/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForMissingAccountIdentifier() throws Exception {
        AccountStatementRequest request = new AccountStatementRequest();
        request.setStartDate(LocalDate.now().minusWeeks(1));
        request.setEndDate(LocalDate.now());

        mockMvc.perform(post("/api/accounts/statement/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidDateRange() throws Exception {
        AccountStatementRequest request = new AccountStatementRequest();
        request.setAccountId(testAccount.getId());
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().minusWeeks(1)); // end before start

        mockMvc.perform(post("/api/accounts/statement/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}