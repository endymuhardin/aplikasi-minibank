package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.CorporateCustomer;
import id.ac.tazkia.minibank.entity.Customer;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    private Map<String, Customer> customerMap = new HashMap<>();
    private Map<String, Product> productMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        // Setup test customers and products
        setupTestCustomersAndProducts();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/account/accounts.csv", numLinesToSkip = 1)
    void shouldSaveAndFindAccountFromCsv(
            String customerNumber,
            String productCode,
            String accountNumber,
            String accountName,
            String balanceStr,
            String status,
            String openedDateStr) {

        // Given - Create account from CSV data
        Customer customer = customerMap.get(customerNumber);
        Product product = productMap.get(productCode);
        
        assertThat(customer).isNotNull();
        assertThat(product).isNotNull();

        Account account = new Account();
        account.setCustomer(customer);
        account.setProduct(product);
        account.setAccountNumber(accountNumber);
        account.setAccountName(accountName);
        account.setBalance(new BigDecimal(balanceStr));
        account.setStatus(Account.AccountStatus.valueOf(status));
        account.setOpenedDate(LocalDate.parse(openedDateStr));
        account.setCreatedBy("TEST");

        // When - Save account
        Account savedAccount = accountRepository.save(account);
        entityManager.flush();

        // Then - Verify account was saved correctly
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(savedAccount.getAccountName()).isEqualTo(accountName);
        assertThat(savedAccount.getBalance()).isEqualByComparingTo(balanceStr);
        assertThat(savedAccount.getStatus().name()).isEqualTo(status);
        assertThat(savedAccount.getCreatedDate()).isNotNull();

        // Verify we can find by account number
        Optional<Account> foundAccount = accountRepository.findByAccountNumber(accountNumber);
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getAccountNumber()).isEqualTo(accountNumber);
    }

    @Test
    void shouldFindAccountsByCustomer() {
        // Given
        saveTestAccounts();

        // When
        Customer customer = customerMap.get("C1000001");
        List<Account> accounts = accountRepository.findByCustomer(customer);

        // Then
        assertThat(accounts).hasSizeGreaterThan(0);
        accounts.forEach(account -> 
            assertThat(account.getCustomer().getCustomerNumber()).isEqualTo("C1000001"));
    }

    @Test
    void shouldFindAccountsByCustomerId() {
        // Given
        saveTestAccounts();

        // When
        Customer customer = customerMap.get("C1000001");
        List<Account> accounts = accountRepository.findByCustomerId(customer.getId());

        // Then
        assertThat(accounts).hasSizeGreaterThan(0);
        accounts.forEach(account -> 
            assertThat(account.getCustomer().getId()).isEqualTo(customer.getId()));
    }

    @Test
    void shouldFindAccountsByProduct() {
        // Given
        saveTestAccounts();

        // When
        Product product = productMap.get("SAV001");
        List<Account> accounts = accountRepository.findByProduct(product);

        // Then
        assertThat(accounts).hasSizeGreaterThan(0);
        accounts.forEach(account -> 
            assertThat(account.getProduct().getProductCode()).isEqualTo("SAV001"));
    }

    @Test
    void shouldFindAccountsByStatus() {
        // Given
        saveTestAccounts();

        // When
        List<Account> activeAccounts = accountRepository.findByStatus(Account.AccountStatus.ACTIVE);
        List<Account> inactiveAccounts = accountRepository.findByStatus(Account.AccountStatus.INACTIVE);

        // Then
        assertThat(activeAccounts).hasSizeGreaterThan(0);
        assertThat(inactiveAccounts).hasSizeGreaterThanOrEqualTo(0);
        
        activeAccounts.forEach(account -> 
            assertThat(account.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE));
    }

    @Test
    void shouldFindActiveAccountsByCustomerId() {
        // Given
        saveTestAccounts();

        // When
        Customer customer = customerMap.get("C1000001");
        List<Account> activeAccounts = accountRepository.findActiveAccountsByCustomerId(customer.getId());

        // Then
        assertThat(activeAccounts).hasSizeGreaterThan(0);
        activeAccounts.forEach(account -> {
            assertThat(account.getCustomer().getId()).isEqualTo(customer.getId());
            assertThat(account.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);
        });
    }

    @Test
    void shouldGetTotalBalanceByCustomerId() {
        // Given
        saveTestAccounts();

        // When
        Customer customer = customerMap.get("C1000001");
        BigDecimal totalBalance = accountRepository.getTotalBalanceByCustomerId(customer.getId());

        // Then
        assertThat(totalBalance).isNotNull();
        assertThat(totalBalance.compareTo(BigDecimal.ZERO)).isGreaterThan(0);
    }

    @Test
    void shouldCountAccountsByStatus() {
        // Given
        saveTestAccounts();

        // When
        Long activeCount = accountRepository.countByStatus(Account.AccountStatus.ACTIVE);
        Long inactiveCount = accountRepository.countByStatus(Account.AccountStatus.INACTIVE);

        // Then
        assertThat(activeCount).isGreaterThan(0);
        assertThat(inactiveCount).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldCountActiveAccountsByProductType() {
        // Given
        saveTestAccounts();

        // When
        Long savingsCount = accountRepository.countActiveAccountsByProductType(Product.ProductType.SAVINGS);
        Long checkingCount = accountRepository.countActiveAccountsByProductType(Product.ProductType.CHECKING);

        // Then
        assertThat(savingsCount).isGreaterThan(0);
        assertThat(checkingCount).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldFindAccountsBelowMinimumBalance() {
        // Given
        saveTestAccounts();

        // When
        List<Account> belowMinimumAccounts = accountRepository.findAccountsBelowMinimumBalance(new BigDecimal("100000"));

        // Then
        assertThat(belowMinimumAccounts).hasSizeGreaterThanOrEqualTo(0);
        belowMinimumAccounts.forEach(account -> 
            assertThat(account.getBalance().compareTo(new BigDecimal("100000"))).isLessThan(0));
    }

    @Test
    void shouldFindZeroBalanceAccounts() {
        // Given
        saveTestAccounts();
        
        // Add a zero balance account
        Customer customer = customerMap.get("C1000001");
        Product product = productMap.get("SAV001");
        Account zeroBalanceAccount = new Account();
        zeroBalanceAccount.setCustomer(customer);
        zeroBalanceAccount.setProduct(product);
        zeroBalanceAccount.setAccountNumber("A9999999");
        zeroBalanceAccount.setAccountName("Zero Balance Test");
        zeroBalanceAccount.setBalance(BigDecimal.ZERO);
        zeroBalanceAccount.setStatus(Account.AccountStatus.ACTIVE);
        zeroBalanceAccount.setCreatedBy("TEST");
        accountRepository.save(zeroBalanceAccount);
        entityManager.flush();

        // When
        List<Account> zeroBalanceAccounts = accountRepository.findZeroBalanceAccounts();

        // Then
        assertThat(zeroBalanceAccounts).hasSizeGreaterThan(0);
        zeroBalanceAccounts.forEach(account -> 
            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO));
    }

    @Test
    void shouldCheckAccountNumberExistence() {
        // Given
        saveTestAccounts();

        // When & Then
        assertThat(accountRepository.existsByAccountNumber("A2000001")).isTrue();
        assertThat(accountRepository.existsByAccountNumber("A9999999")).isFalse();
    }

    @Test
    void shouldFindAccountByAccountNumberWithDetails() {
        // Given
        saveTestAccounts();

        // When
        Optional<Account> account = accountRepository.findByAccountNumberWithDetails("A2000001");

        // Then
        assertThat(account).isPresent();
        assertThat(account.get().getCustomer()).isNotNull();
        assertThat(account.get().getProduct()).isNotNull();
        assertThat(account.get().getAccountNumber()).isEqualTo("A2000001");
    }


    private void setupTestCustomersAndProducts() {
        // Create test customers to match CSV data
        PersonalCustomer personal1 = new PersonalCustomer();
        personal1.setCustomerNumber("C1000001");
        personal1.setFirstName("Ahmad");
        personal1.setLastName("Suharto");
        personal1.setDateOfBirth(LocalDate.of(1985, 3, 15));
        personal1.setIdentityNumber("3271081503850001");
        personal1.setIdentityType(Customer.IdentityType.KTP);
        personal1.setEmail("ahmad.suharto@email.com");
        personal1.setPhoneNumber("081234567890");
        personal1.setAddress("Jl. Sudirman No. 123");
        personal1.setCity("Jakarta");
        personal1.setPostalCode("10220");
        personal1.setCountry("Indonesia");
        personal1.setCreatedBy("TEST");

        PersonalCustomer personal2 = new PersonalCustomer();
        personal2.setCustomerNumber("C1000002");
        personal2.setFirstName("Siti");
        personal2.setLastName("Nurhaliza");
        personal2.setDateOfBirth(LocalDate.of(1990, 7, 22));
        personal2.setIdentityNumber("3271082207900002");
        personal2.setIdentityType(Customer.IdentityType.KTP);
        personal2.setEmail("siti.nurhaliza@email.com");
        personal2.setPhoneNumber("081234567891");
        personal2.setAddress("Jl. Thamrin No. 456");
        personal2.setCity("Jakarta");
        personal2.setPostalCode("10230");
        personal2.setCountry("Indonesia");
        personal2.setCreatedBy("TEST");

        PersonalCustomer personal3 = new PersonalCustomer();
        personal3.setCustomerNumber("C1000003");
        personal3.setFirstName("Budi");
        personal3.setLastName("Santoso");
        personal3.setDateOfBirth(LocalDate.of(1988, 12, 10));
        personal3.setIdentityNumber("3271081012880003");
        personal3.setIdentityType(Customer.IdentityType.KTP);
        personal3.setEmail("budi.santoso@email.com");
        personal3.setPhoneNumber("081234567892");
        personal3.setAddress("Jl. Gatot Subroto No. 789");
        personal3.setCity("Jakarta");
        personal3.setPostalCode("12950");
        personal3.setCountry("Indonesia");
        personal3.setCreatedBy("TEST");

        CorporateCustomer corporate1 = new CorporateCustomer();
        corporate1.setCustomerNumber("C1000004");
        corporate1.setCompanyName("PT. Teknologi Maju");
        corporate1.setCompanyRegistrationNumber("1234567890123456");
        corporate1.setTaxIdentificationNumber("01.234.567.8-901.000");
        corporate1.setEmail("info@teknologimaju.com");
        corporate1.setPhoneNumber("02123456789");
        corporate1.setAddress("Jl. HR Rasuna Said No. 789");
        corporate1.setCity("Jakarta");
        corporate1.setPostalCode("12950");
        corporate1.setCountry("Indonesia");
        corporate1.setCreatedBy("TEST");

        CorporateCustomer corporate2 = new CorporateCustomer();
        corporate2.setCustomerNumber("C1000005");
        corporate2.setCompanyName("CV. Berkah Jaya");
        corporate2.setCompanyRegistrationNumber("9876543210987654");
        corporate2.setTaxIdentificationNumber("98.765.432.1-098.000");
        corporate2.setEmail("admin@berkahjaya.com");
        corporate2.setPhoneNumber("02187654321");
        corporate2.setAddress("Jl. Kuningan Raya No. 456");
        corporate2.setCity("Jakarta");
        corporate2.setPostalCode("12940");
        corporate2.setCountry("Indonesia");
        corporate2.setCreatedBy("TEST");

        PersonalCustomer personal4 = new PersonalCustomer();
        personal4.setCustomerNumber("C1000006");
        personal4.setFirstName("Dewi");
        personal4.setLastName("Lestari");
        personal4.setDateOfBirth(LocalDate.of(1992, 5, 18));
        personal4.setIdentityNumber("3271051892920004");
        personal4.setIdentityType(Customer.IdentityType.PASSPORT);
        personal4.setEmail("dewi.lestari@email.com");
        personal4.setPhoneNumber("081234567893");
        personal4.setAddress("Jl. Senayan No. 321");
        personal4.setCity("Jakarta");
        personal4.setPostalCode("10270");
        personal4.setCountry("Indonesia");
        personal4.setCreatedBy("TEST");

        customerRepository.save(personal1);
        customerRepository.save(personal2);
        customerRepository.save(personal3);
        customerRepository.save(corporate1);
        customerRepository.save(corporate2);
        customerRepository.save(personal4);
        
        customerMap.put("C1000001", personal1);
        customerMap.put("C1000002", personal2);
        customerMap.put("C1000003", personal3);
        customerMap.put("C1000004", corporate1);
        customerMap.put("C1000005", corporate2);
        customerMap.put("C1000006", personal4);

        // Create test products to match CSV data
        Product savingsBasic = new Product();
        savingsBasic.setProductCode("SAV001");
        savingsBasic.setProductName("Basic Savings Account");
        savingsBasic.setProductType(Product.ProductType.SAVINGS);
        savingsBasic.setProductCategory("Regular Savings");
        savingsBasic.setDescription("Basic savings account for individual customers");
        savingsBasic.setIsActive(true);
        savingsBasic.setIsDefault(true);
        savingsBasic.setCurrency("IDR");
        savingsBasic.setMinimumOpeningBalance(new BigDecimal("50000.00"));
        savingsBasic.setMinimumBalance(new BigDecimal("10000.00"));
        savingsBasic.setDailyWithdrawalLimit(new BigDecimal("5000000.00"));
        savingsBasic.setMonthlyTransactionLimit(50);
        savingsBasic.setProfitSharingRatio(new BigDecimal("0.0275"));
        savingsBasic.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
        savingsBasic.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        savingsBasic.setNisbahCustomer(new BigDecimal("0.7000"));
        savingsBasic.setNisbahBank(new BigDecimal("0.3000"));
        savingsBasic.setMonthlyMaintenanceFee(new BigDecimal("2500.00"));
        savingsBasic.setAtmWithdrawalFee(new BigDecimal("5000.00"));
        savingsBasic.setInterBankTransferFee(new BigDecimal("7500.00"));
        savingsBasic.setBelowMinimumBalanceFee(new BigDecimal("10000.00"));
        savingsBasic.setAccountClosureFee(new BigDecimal("0.00"));
        savingsBasic.setFreeTransactionsPerMonth(10);
        savingsBasic.setExcessTransactionFee(new BigDecimal("2500.00"));
        savingsBasic.setAllowOverdraft(false);
        savingsBasic.setRequireMaintainingBalance(true);
        savingsBasic.setMinCustomerAge(17);
        savingsBasic.setAllowedCustomerTypes("PERSONAL");
        savingsBasic.setRequiredDocuments("KTP, NPWP (optional)");
        savingsBasic.setCreatedBy("TEST");

        Product savingsPremium = new Product();
        savingsPremium.setProductCode("SAV002");
        savingsPremium.setProductName("Premium Savings Account");
        savingsPremium.setProductType(Product.ProductType.SAVINGS);
        savingsPremium.setProductCategory("Premium Savings");
        savingsPremium.setDescription("Premium savings account with higher interest");
        savingsPremium.setIsActive(true);
        savingsPremium.setIsDefault(false);
        savingsPremium.setCurrency("IDR");
        savingsPremium.setMinimumOpeningBalance(new BigDecimal("1000000.00"));
        savingsPremium.setMinimumBalance(new BigDecimal("500000.00"));
        savingsPremium.setDailyWithdrawalLimit(new BigDecimal("10000000.00"));
        savingsPremium.setMonthlyTransactionLimit(100);
        savingsPremium.setProfitSharingRatio(new BigDecimal("0.0350"));
        savingsPremium.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
        savingsPremium.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        savingsPremium.setNisbahCustomer(new BigDecimal("0.7000"));
        savingsPremium.setNisbahBank(new BigDecimal("0.3000"));
        savingsPremium.setMonthlyMaintenanceFee(new BigDecimal("0.00"));
        savingsPremium.setAtmWithdrawalFee(new BigDecimal("0.00"));
        savingsPremium.setInterBankTransferFee(new BigDecimal("5000.00"));
        savingsPremium.setBelowMinimumBalanceFee(new BigDecimal("25000.00"));
        savingsPremium.setAccountClosureFee(new BigDecimal("0.00"));
        savingsPremium.setFreeTransactionsPerMonth(25);
        savingsPremium.setExcessTransactionFee(new BigDecimal("2500.00"));
        savingsPremium.setAllowOverdraft(false);
        savingsPremium.setRequireMaintainingBalance(true);
        savingsPremium.setMinCustomerAge(21);
        savingsPremium.setAllowedCustomerTypes("PERSONAL");
        savingsPremium.setRequiredDocuments("KTP, NPWP, Slip Gaji");
        savingsPremium.setCreatedBy("TEST");

        Product savingsCorporate = new Product();
        savingsCorporate.setProductCode("SAV003");
        savingsCorporate.setProductName("Corporate Savings Account");
        savingsCorporate.setProductType(Product.ProductType.SAVINGS);
        savingsCorporate.setProductCategory("Corporate");
        savingsCorporate.setDescription("Savings account for corporate customers");
        savingsCorporate.setIsActive(true);
        savingsCorporate.setIsDefault(false);
        savingsCorporate.setCurrency("IDR");
        savingsCorporate.setMinimumOpeningBalance(new BigDecimal("5000000.00"));
        savingsCorporate.setMinimumBalance(new BigDecimal("1000000.00"));
        savingsCorporate.setDailyWithdrawalLimit(new BigDecimal("50000000.00"));
        savingsCorporate.setMonthlyTransactionLimit(200);
        savingsCorporate.setProfitSharingRatio(new BigDecimal("0.0300"));
        savingsCorporate.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
        savingsCorporate.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        savingsCorporate.setNisbahCustomer(new BigDecimal("0.7000"));
        savingsCorporate.setNisbahBank(new BigDecimal("0.3000"));
        savingsCorporate.setMonthlyMaintenanceFee(new BigDecimal("15000.00"));
        savingsCorporate.setAtmWithdrawalFee(new BigDecimal("5000.00"));
        savingsCorporate.setInterBankTransferFee(new BigDecimal("5000.00"));
        savingsCorporate.setBelowMinimumBalanceFee(new BigDecimal("50000.00"));
        savingsCorporate.setAccountClosureFee(new BigDecimal("25000.00"));
        savingsCorporate.setFreeTransactionsPerMonth(50);
        savingsCorporate.setExcessTransactionFee(new BigDecimal("5000.00"));
        savingsCorporate.setAllowOverdraft(false);
        savingsCorporate.setRequireMaintainingBalance(true);
        savingsCorporate.setAllowedCustomerTypes("CORPORATE");
        savingsCorporate.setRequiredDocuments("Akta Pendirian, SIUP, TDP, NPWP");
        savingsCorporate.setCreatedBy("TEST");

        Product checkingBasic = new Product();
        checkingBasic.setProductCode("CHK001");
        checkingBasic.setProductName("Basic Checking Account");
        checkingBasic.setProductType(Product.ProductType.CHECKING);
        checkingBasic.setProductCategory("Regular Checking");
        checkingBasic.setDescription("Basic checking account with overdraft facility");
        checkingBasic.setIsActive(true);
        checkingBasic.setIsDefault(false);
        checkingBasic.setCurrency("IDR");
        checkingBasic.setMinimumOpeningBalance(new BigDecimal("100000.00"));
        checkingBasic.setMinimumBalance(new BigDecimal("50000.00"));
        checkingBasic.setDailyWithdrawalLimit(new BigDecimal("20000000.00"));
        checkingBasic.setMonthlyTransactionLimit(100);
        checkingBasic.setProfitSharingRatio(new BigDecimal("0.0100"));
        checkingBasic.setProfitSharingType(Product.ProfitSharingType.WADIAH);
        checkingBasic.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        checkingBasic.setMonthlyMaintenanceFee(new BigDecimal("5000.00"));
        checkingBasic.setAtmWithdrawalFee(new BigDecimal("5000.00"));
        checkingBasic.setInterBankTransferFee(new BigDecimal("7500.00"));
        checkingBasic.setBelowMinimumBalanceFee(new BigDecimal("15000.00"));
        checkingBasic.setAccountClosureFee(new BigDecimal("10000.00"));
        checkingBasic.setFreeTransactionsPerMonth(20);
        checkingBasic.setExcessTransactionFee(new BigDecimal("3000.00"));
        checkingBasic.setAllowOverdraft(true);
        checkingBasic.setRequireMaintainingBalance(true);
        checkingBasic.setMinCustomerAge(18);
        checkingBasic.setAllowedCustomerTypes("PERSONAL");
        checkingBasic.setRequiredDocuments("KTP, NPWP, Slip Gaji");
        checkingBasic.setCreatedBy("TEST");

        Product checkingPremium = new Product();
        checkingPremium.setProductCode("CHK002");
        checkingPremium.setProductName("Premium Checking Account");
        checkingPremium.setProductType(Product.ProductType.CHECKING);
        checkingPremium.setProductCategory("Premium Checking");
        checkingPremium.setDescription("Premium checking with higher overdraft limit");
        checkingPremium.setIsActive(true);
        checkingPremium.setIsDefault(false);
        checkingPremium.setCurrency("IDR");
        checkingPremium.setMinimumOpeningBalance(new BigDecimal("2000000.00"));
        checkingPremium.setMinimumBalance(new BigDecimal("1000000.00"));
        checkingPremium.setDailyWithdrawalLimit(new BigDecimal("50000000.00"));
        checkingPremium.setMonthlyTransactionLimit(200);
        checkingPremium.setProfitSharingRatio(new BigDecimal("0.0150"));
        checkingPremium.setProfitSharingType(Product.ProfitSharingType.WADIAH);
        checkingPremium.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        checkingPremium.setMonthlyMaintenanceFee(new BigDecimal("0.00"));
        checkingPremium.setAtmWithdrawalFee(new BigDecimal("0.00"));
        checkingPremium.setInterBankTransferFee(new BigDecimal("5000.00"));
        checkingPremium.setBelowMinimumBalanceFee(new BigDecimal("25000.00"));
        checkingPremium.setAccountClosureFee(new BigDecimal("15000.00"));
        checkingPremium.setFreeTransactionsPerMonth(50);
        checkingPremium.setExcessTransactionFee(new BigDecimal("3000.00"));
        checkingPremium.setAllowOverdraft(true);
        checkingPremium.setRequireMaintainingBalance(true);
        checkingPremium.setMinCustomerAge(25);
        checkingPremium.setAllowedCustomerTypes("PERSONAL");
        checkingPremium.setRequiredDocuments("KTP, NPWP, Slip Gaji, Rekening Koran");
        checkingPremium.setCreatedBy("TEST");

        productRepository.save(savingsBasic);
        productRepository.save(savingsPremium);
        productRepository.save(savingsCorporate);
        productRepository.save(checkingBasic);
        productRepository.save(checkingPremium);
        
        productMap.put("SAV001", savingsBasic);
        productMap.put("SAV002", savingsPremium);
        productMap.put("SAV003", savingsCorporate);
        productMap.put("CHK001", checkingBasic);
        productMap.put("CHK002", checkingPremium);
        
        entityManager.flush();
    }

    private void saveTestAccounts() {
        // Save test accounts
        Customer customer1 = customerMap.get("C1000001");
        Customer customer2 = customerMap.get("C1000002");
        Product savingsProduct = productMap.get("SAV001");
        Product checkingProduct = productMap.get("CHK001");

        Account account1 = new Account();
        account1.setCustomer(customer1);
        account1.setProduct(savingsProduct);
        account1.setAccountNumber("A2000001");
        account1.setAccountName("Ahmad Suharto - Savings");
        account1.setBalance(new BigDecimal("500000"));
        account1.setStatus(Account.AccountStatus.ACTIVE);
        account1.setOpenedDate(LocalDate.of(2024, 1, 15));
        account1.setCreatedBy("TEST");

        Account account2 = new Account();
        account2.setCustomer(customer2);
        account2.setProduct(savingsProduct);
        account2.setAccountNumber("A2000002");
        account2.setAccountName("Siti Nurhaliza - Savings");
        account2.setBalance(new BigDecimal("750000"));
        account2.setStatus(Account.AccountStatus.ACTIVE);
        account2.setOpenedDate(LocalDate.of(2024, 1, 20));
        account2.setCreatedBy("TEST");

        Account account3 = new Account();
        account3.setCustomer(customer1);
        account3.setProduct(checkingProduct);
        account3.setAccountNumber("A2000003");
        account3.setAccountName("Ahmad Suharto - Checking");
        account3.setBalance(new BigDecimal("300000"));
        account3.setStatus(Account.AccountStatus.ACTIVE);
        account3.setOpenedDate(LocalDate.of(2024, 2, 10));
        account3.setCreatedBy("TEST");

        accountRepository.save(account1);
        accountRepository.save(account2);
        accountRepository.save(account3);
        entityManager.flush();
    }
}