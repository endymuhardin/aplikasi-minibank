package id.ac.tazkia.minibank.integration.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.integration.BaseIntegrationTest;
import id.ac.tazkia.minibank.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sql(scripts = "/sql/setup-passbook-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PassbookControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AccountRepository accountRepository;
    

    private MockMvc mockMvc;
    private Map<String, Account> accountMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        // Initialize accountMap with test accounts for convenience
        accountRepository.findAll().forEach(account -> 
            accountMap.put(account.getAccountNumber(), account)
        );
    }


    @Test
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldDisplayAccountSelectionPage() throws Exception {
        log.info("🧪 TEST START: shouldDisplayAccountSelectionPage");
        
        mockMvc.perform(get("/passbook/select-account"))
                .andExpect(status().isOk())
                .andExpect(view().name("passbook/select-account"))
                .andExpect(model().attributeExists("accounts"))
                .andExpect(model().attribute("accounts", hasSize(greaterThan(0))));
        
        log.info("✅ TEST PASS: shouldDisplayAccountSelectionPage completed successfully");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/passbook/search-scenarios.csv", numLinesToSkip = 1)
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldDisplayAccountSelectionPageWithSearch(String searchTerm, String expectedResults, String description) throws Exception {
        log.info("🧪 TEST START: shouldDisplayAccountSelectionPageWithSearch - {}", description);
        
        var request = get("/passbook/select-account");
        if (!"EMPTY".equals(searchTerm)) {
            request = request.param("search", searchTerm);
        }
        
        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("passbook/select-account"))
                .andExpect(model().attributeExists("accounts"));
        
        if (!"EMPTY".equals(searchTerm)) {
            response.andExpect(model().attribute("search", searchTerm));
        }
        
        log.info("✅ TEST PASS: shouldDisplayAccountSelectionPageWithSearch completed for: {}", description);
    }


    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/passbook/passbook-test-scenarios.csv", numLinesToSkip = 1)
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldDisplayPassbookPreview(String accountNumber, String expectedStatus, String description) throws Exception {
        log.info("🧪 TEST START: shouldDisplayPassbookPreview for account: {}, description: {}", accountNumber, description);
        
        // Given
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalStateException("Test account not found: " + accountNumber));
        
        // When & Then
        if ("OK".equals(expectedStatus)) {
            mockMvc.perform(get("/passbook/preview/{accountId}", account.getId()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("passbook/preview"))
                    .andExpect(model().attributeExists("account"))
                    .andExpect(model().attributeExists("transactions"))
                    .andExpect(model().attributeExists("bankLogoPath"))
                    .andExpect(model().attributeExists("bankName"))
                    .andExpect(model().attributeExists("bankAddress"))
                    .andExpect(model().attribute("isPreview", true))
                    .andExpect(model().attribute("account", hasProperty("accountNumber", is(accountNumber))));
        } else {
            mockMvc.perform(get("/passbook/preview/{accountId}", account.getId()))
                    .andExpect(status().is3xxRedirection());
        }
        
        log.info("✅ TEST PASS: shouldDisplayPassbookPreview completed for account: {}", accountNumber);
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldDisplayPassbookPreviewWithDateParameters() throws Exception {
        log.info("🧪 TEST START: shouldDisplayPassbookPreviewWithDateParameters");
        
        // Given
        Account account = accountRepository.findByAccountNumber("A2000001")
                .orElseThrow(() -> new IllegalStateException("Test account A2000001 not found"));

        // When & Then
        mockMvc.perform(get("/passbook/preview/{accountId}", account.getId())
                .param("fromDate", "2024-01-01")
                .param("toDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(view().name("passbook/preview"))
                .andExpect(model().attribute("fromDate", "2024-01-01"))
                .andExpect(model().attribute("toDate", "2024-12-31"));
        
        log.info("✅ TEST PASS: shouldDisplayPassbookPreviewWithDateParameters completed successfully");
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldRedirectToSelectionWhenAccountNotFoundForPreview() throws Exception {
        log.info("🧪 TEST START: shouldRedirectToSelectionWhenAccountNotFoundForPreview");
        
        // Given
        UUID nonExistentAccountId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/passbook/preview/{accountId}", nonExistentAccountId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/passbook/select-account"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Account not found"));
        
        log.info("✅ TEST PASS: shouldRedirectToSelectionWhenAccountNotFoundForPreview completed successfully");
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldDisplayPassbookPrintPage() throws Exception {
        log.info("🧪 TEST START: shouldDisplayPassbookPrintPage");
        
        // Given
        Account account = accountRepository.findByAccountNumber("A2000001")
                .orElseThrow(() -> new IllegalStateException("Test account A2000001 not found"));

        // When & Then
        mockMvc.perform(get("/passbook/print/{accountId}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("passbook/print"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("allTransactions"))
                .andExpect(model().attributeExists("printDate"))
                .andExpect(model().attributeExists("bankLogoPath"))
                .andExpect(model().attributeExists("bankName"))
                .andExpect(model().attributeExists("bankAddress"))
                .andExpect(model().attribute("account", hasProperty("accountNumber", is("A2000001"))));
        
        log.info("✅ TEST PASS: shouldDisplayPassbookPrintPage completed successfully");
    }




    @Test
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldRedirectWhenAccountIsInactive() throws Exception {
        log.info("🧪 TEST START: shouldRedirectWhenAccountIsInactive");
        
        // Given - Create an inactive account for this test
        Account account = accountRepository.findByAccountNumber("A2000001")
                .orElseThrow(() -> new IllegalStateException("Test account A2000001 not found"));
        account.setStatus(Account.AccountStatus.INACTIVE);
        accountRepository.save(account);

        // When & Then
        mockMvc.perform(get("/passbook/print/{accountId}", account.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/passbook/select-account"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Cannot print passbook for inactive account"));
        
        log.info("✅ TEST PASS: shouldRedirectWhenAccountIsInactive completed successfully");
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldHandleInvalidDateFormat() throws Exception {
        log.info("🧪 TEST START: shouldHandleInvalidDateFormat");
        
        // Given
        Account account = accountRepository.findByAccountNumber("A2000001")
                .orElseThrow(() -> new IllegalStateException("Test account A2000001 not found"));

        // When & Then
        mockMvc.perform(get("/passbook/print/{accountId}", account.getId())
                .param("fromDate", "invalid-date")
                .param("toDate", "2024-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/passbook/select-account"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", containsString("Invalid date format")));
        
        log.info("✅ TEST PASS: shouldHandleInvalidDateFormat completed successfully");
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldDisplayEmptyTransactionsForAccountWithNoHistory() throws Exception {
        // Given
        Account accountWithoutTransactions = accountMap.get("A2000002"); // No transactions added

        // When & Then
        mockMvc.perform(get("/passbook/print/{accountId}", accountWithoutTransactions.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("passbook/print"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attribute("transactions", hasProperty("content", hasSize(0))));
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_VIEW", "TRANSACTION_VIEW"})
    void shouldVerifyBankConfigurationInModel() throws Exception {
        log.info("🧪 TEST START: shouldVerifyBankConfigurationInModel");
        
        // Given
        Account account = accountRepository.findByAccountNumber("A2000001")
                .orElseThrow(() -> new IllegalStateException("Test account A2000001 not found"));

        // When & Then
        mockMvc.perform(get("/passbook/print/{accountId}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("bankLogoPath", "/images/bank-logo.svg"))
                .andExpect(model().attribute("bankName", "Minibank Islamic Banking"))
                .andExpect(model().attribute("bankAddress", "Jl. Raya Jakarta No. 123, Jakarta 12345, Indonesia"));
        
        log.info("✅ TEST PASS: shouldVerifyBankConfigurationInModel completed successfully");
    }

    @Test
    void shouldRequireAuthenticationForAccess() throws Exception {
        log.info("🧪 TEST START: shouldRequireAuthenticationForAccess");
        
        // When & Then - No authentication (requires authentication)
        mockMvc.perform(get("/passbook/select-account"))
                .andExpect(status().isUnauthorized());
        
        log.info("✅ TEST PASS: shouldRequireAuthenticationForAccess completed successfully");
    }

    @Test
    @WithMockUser(authorities = {"OTHER_PERMISSION"})
    void shouldAllowAccessWithAnyAuthenticatedUser() throws Exception {
        log.info("🧪 TEST START: shouldAllowAccessWithAnyAuthenticatedUser");
        
        // When & Then - Any authenticated user can access (no specific permission required)
        mockMvc.perform(get("/passbook/select-account"))
                .andExpect(status().isOk());
        
        log.info("✅ TEST PASS: shouldAllowAccessWithAnyAuthenticatedUser completed successfully");
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_VIEW"}) // Only one permission
    void shouldAllowAccessWithMinimumPermissions() throws Exception {
        log.info("🧪 TEST START: shouldAllowAccessWithMinimumPermissions");
        
        // When & Then
        mockMvc.perform(get("/passbook/select-account"))
                .andExpect(status().isOk());
        
        log.info("✅ TEST PASS: shouldAllowAccessWithMinimumPermissions completed successfully");
    }

    @Test
    @WithMockUser(authorities = {"TRANSACTION_VIEW"}) // Only one permission
    void shouldAllowAccessWithAlternativePermissions() throws Exception {
        log.info("🧪 TEST START: shouldAllowAccessWithAlternativePermissions");
        
        // When & Then
        mockMvc.perform(get("/passbook/select-account"))
                .andExpect(status().isOk());
        
        log.info("✅ TEST PASS: shouldAllowAccessWithAlternativePermissions completed successfully");
    }




}