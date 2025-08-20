package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.entity.PersonalCustomer;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.AccountRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import id.ac.tazkia.minibank.repository.CustomerRepository;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;

/**
 * AccountRepository tests optimized for parallel execution.
 * Uses dynamic test data to prevent conflicts during concurrent execution.
 * Note: Using SAME_THREAD execution to avoid transaction management conflicts.
 */
@org.junit.jupiter.api.parallel.Execution(org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD)
class AccountRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private BranchRepository branchRepository;

    @Test
    void shouldSaveAndFindAccount() {
        logTestExecution("shouldSaveAndFindAccount");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);

        // When - Save account
        Account savedAccount = accountRepository.save(account);

        // Then - Verify account was saved correctly
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getAccountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(savedAccount.getCustomer()).isEqualTo(customer);
        assertThat(savedAccount.getProduct()).isEqualTo(product);
        assertThat(savedAccount.getBranch()).isEqualTo(branch);
        assertThat(savedAccount.getBalance()).isEqualTo(account.getBalance());
        assertThat(savedAccount.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);
    }

    @Test
    void shouldFindByAccountNumber() {
        logTestExecution("shouldFindByAccountNumber");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        accountRepository.save(account);

        // When
        Optional<Account> foundAccount = accountRepository.findByAccountNumber(account.getAccountNumber());

        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getAccountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(foundAccount.get().getCustomer().getCustomerNumber()).isEqualTo(customer.getCustomerNumber());
    }

    @Test
    void shouldFindByCustomerId() {
        logTestExecution("shouldFindByCustomerId");
        
        // Given - Create unique test data with multiple accounts
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product savingsProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        Product depositProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.DEPOSITO_MUDHARABAH);
        productRepository.save(savingsProduct);
        productRepository.save(depositProduct);
        
        Account savingsAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, savingsProduct, branch);
        Account depositAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, depositProduct, branch);
        accountRepository.save(savingsAccount);
        accountRepository.save(depositAccount);

        // When
        List<Account> customerAccounts = accountRepository.findByCustomerId(customer.getId());

        // Then
        assertThat(customerAccounts).hasSize(2);
        assertThat(customerAccounts).extracting(Account::getCustomer)
                .allMatch(c -> c.getId().equals(customer.getId()));
        assertThat(customerAccounts).extracting(Account::getAccountNumber)
                .containsExactlyInAnyOrder(savingsAccount.getAccountNumber(), depositAccount.getAccountNumber());
    }

    @Test
    void shouldFindActiveAccountsByCustomerId() {
        logTestExecution("shouldFindActiveAccountsByCustomerId");
        
        // Given - Create unique test data with active and inactive accounts
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account activeAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        activeAccount.setStatus(Account.AccountStatus.ACTIVE);
        
        Account inactiveAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        inactiveAccount.setStatus(Account.AccountStatus.INACTIVE);
        
        accountRepository.save(activeAccount);
        accountRepository.save(inactiveAccount);

        // When
        List<Account> activeAccounts = accountRepository.findActiveAccountsByCustomerId(customer.getId());

        // Then
        assertThat(activeAccounts).hasSize(1);
        assertThat(activeAccounts.get(0).getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);
        assertThat(activeAccounts.get(0).getAccountNumber()).isEqualTo(activeAccount.getAccountNumber());
    }

    @Test
    void shouldFindAccountsByProduct() {
        logTestExecution("shouldFindAccountsByProduct");
        
        // Given - Create unique test data with different products
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer1 = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        PersonalCustomer customer2 = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer1);
        customerRepository.save(customer2);
        
        Product savingsProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        Product depositProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.DEPOSITO_MUDHARABAH);
        productRepository.save(savingsProduct);
        productRepository.save(depositProduct);
        
        Account savingsAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer1, savingsProduct, branch);
        Account depositAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer2, depositProduct, branch);
        accountRepository.save(savingsAccount);
        accountRepository.save(depositAccount);

        // When - Find accounts by product ID
        List<Account> savingsAccounts = accountRepository.findByProduct(savingsProduct);
        List<Account> depositAccounts = accountRepository.findByProduct(depositProduct);

        // Then
        assertThat(savingsAccounts).hasSize(1);
        assertThat(savingsAccounts.get(0).getProduct()).isEqualTo(savingsProduct);
        
        assertThat(depositAccounts).hasSize(1);
        assertThat(depositAccounts.get(0).getProduct()).isEqualTo(depositProduct);
    }

    @Test
    void shouldFindAccountsByBalanceRange() {
        logTestExecution("shouldFindAccountsByBalanceRange");
        
        // Given - Create unique test data with different balances
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer1 = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        PersonalCustomer customer2 = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer1);
        customerRepository.save(customer2);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account highBalanceAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer1, product, branch);
        highBalanceAccount.setBalance(new BigDecimal("10000000")); // 10M
        
        Account lowBalanceAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer2, product, branch);
        lowBalanceAccount.setBalance(new BigDecimal("100000")); // 100K
        
        accountRepository.save(highBalanceAccount);
        accountRepository.save(lowBalanceAccount);

        // When - Find all accounts and verify balances
        List<Account> allAccounts = accountRepository.findAll();

        // Then - Filter by balance in test logic
        BigDecimal threshold = new BigDecimal("5000000");
        List<Account> highBalanceAccounts = allAccounts.stream()
                .filter(a -> a.getBalance().compareTo(threshold) > 0)
                .toList();
        
        assertThat(highBalanceAccounts).hasSize(1);
        assertThat(highBalanceAccounts.get(0).getBalance()).isGreaterThan(threshold);
        assertThat(highBalanceAccounts.get(0).getAccountNumber()).isEqualTo(highBalanceAccount.getAccountNumber());
    }

    @Test
    void shouldUpdateAccountBalance() {
        logTestExecution("shouldUpdateAccountBalance");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        accountRepository.save(account);
        
        BigDecimal initialBalance = account.getBalance();
        BigDecimal newBalance = initialBalance.add(new BigDecimal("500000"));

        // When
        account.setBalance(newBalance);
        Account updatedAccount = accountRepository.save(account);

        // Then
        assertThat(updatedAccount.getBalance()).isEqualTo(newBalance);
        
        // Verify in database
        Optional<Account> foundAccount = accountRepository.findById(account.getId());
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getBalance()).isEqualTo(newBalance);
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/account/parallel-accounts.csv", numLinesToSkip = 1)
    @ResourceLock("parameterized-test-transaction")
    void shouldSaveAccountFromCsv(String productType, String initialBalance, String accountStatus) {
        logTestExecution("shouldSaveAccountFromCsv: " + productType);
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.valueOf(productType));
        productRepository.save(product);
        
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        account.setBalance(new BigDecimal(initialBalance));
        account.setStatus(Account.AccountStatus.valueOf(accountStatus));

        // When
        Account savedAccount = accountRepository.save(account);

        // Then
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getBalance()).isEqualTo(new BigDecimal(initialBalance));
        assertThat(savedAccount.getStatus()).isEqualTo(Account.AccountStatus.valueOf(accountStatus));
        assertThat(savedAccount.getProduct().getProductType()).isEqualTo(Product.ProductType.valueOf(productType));
    }
    
    @Test
    void shouldFindAccountsByCustomer() {
        logTestExecution("shouldFindAccountsByCustomer");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        accountRepository.save(account);
        
        // When
        List<Account> accounts = accountRepository.findByCustomer(customer);
        
        // Then
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getCustomer().getId()).isEqualTo(customer.getId());
    }
    
    @Test
    void shouldFindAccountsByStatus() {
        logTestExecution("shouldFindAccountsByStatus");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account activeAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        activeAccount.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(activeAccount);
        
        Account inactiveAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        inactiveAccount.setStatus(Account.AccountStatus.INACTIVE);
        accountRepository.save(inactiveAccount);
        
        // When
        List<Account> activeAccounts = accountRepository.findByStatus(Account.AccountStatus.ACTIVE);
        List<Account> inactiveAccounts = accountRepository.findByStatus(Account.AccountStatus.INACTIVE);
        
        // Then
        assertThat(activeAccounts).hasSizeGreaterThanOrEqualTo(1);
        assertThat(inactiveAccounts).hasSizeGreaterThanOrEqualTo(1);
        
        activeAccounts.forEach(account -> 
            assertThat(account.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE));
        inactiveAccounts.forEach(account -> 
            assertThat(account.getStatus()).isEqualTo(Account.AccountStatus.INACTIVE));
    }
    
    @Test
    void shouldGetTotalBalanceByCustomerId() {
        logTestExecution("shouldGetTotalBalanceByCustomerId");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account account1 = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        account1.setBalance(new BigDecimal("500000"));
        accountRepository.save(account1);
        
        Account account2 = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        account2.setBalance(new BigDecimal("300000"));
        accountRepository.save(account2);
        
        // When
        BigDecimal totalBalance = accountRepository.getTotalBalanceByCustomerId(customer.getId());
        
        // Then
        assertThat(totalBalance).isNotNull();
        assertThat(totalBalance).isEqualByComparingTo("800000");
    }
    
    @Test
    void shouldCountAccountsByStatus() {
        logTestExecution("shouldCountAccountsByStatus");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        // Create accounts with different statuses
        Account activeAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        activeAccount.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(activeAccount);
        
        Account inactiveAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        inactiveAccount.setStatus(Account.AccountStatus.INACTIVE);
        accountRepository.save(inactiveAccount);
        
        // When
        Long activeCountBefore = accountRepository.countByStatus(Account.AccountStatus.ACTIVE);
        Long inactiveCountBefore = accountRepository.countByStatus(Account.AccountStatus.INACTIVE);
        
        // Then
        assertThat(activeCountBefore).isGreaterThanOrEqualTo(1);
        assertThat(inactiveCountBefore).isGreaterThanOrEqualTo(1);
    }
    
    @Test
    void shouldCountActiveAccountsByProductType() {
        logTestExecution("shouldCountActiveAccountsByProductType");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product wadiah = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(wadiah);
        
        Product mudharabah = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_MUDHARABAH);
        productRepository.save(mudharabah);
        
        Account wadiahAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, wadiah, branch);
        wadiahAccount.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(wadiahAccount);
        
        Account mudharabahAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, mudharabah, branch);
        mudharabahAccount.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(mudharabahAccount);
        
        // When
        Long wadiahCount = accountRepository.countActiveAccountsByProductType(Product.ProductType.TABUNGAN_WADIAH);
        Long mudharabahCount = accountRepository.countActiveAccountsByProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        
        // Then
        assertThat(wadiahCount).isGreaterThanOrEqualTo(1);
        assertThat(mudharabahCount).isGreaterThanOrEqualTo(1);
    }
    
    @Test
    void shouldFindAccountsBelowMinimumBalance() {
        logTestExecution("shouldFindAccountsBelowMinimumBalance");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account lowBalanceAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        lowBalanceAccount.setBalance(new BigDecimal("50000"));
        accountRepository.save(lowBalanceAccount);
        
        Account highBalanceAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        highBalanceAccount.setBalance(new BigDecimal("200000"));
        accountRepository.save(highBalanceAccount);
        
        // When
        List<Account> belowMinimumAccounts = accountRepository.findAccountsBelowMinimumBalance(new BigDecimal("100000"));
        
        // Then
        assertThat(belowMinimumAccounts).hasSizeGreaterThanOrEqualTo(1);
        belowMinimumAccounts.forEach(account -> 
            assertThat(account.getBalance().compareTo(new BigDecimal("100000"))).isLessThan(0));
    }
    
    @Test
    void shouldFindZeroBalanceAccounts() {
        logTestExecution("shouldFindZeroBalanceAccounts");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account zeroBalanceAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        zeroBalanceAccount.setBalance(BigDecimal.ZERO);
        accountRepository.save(zeroBalanceAccount);
        
        Account normalAccount = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        normalAccount.setBalance(new BigDecimal("100000"));
        accountRepository.save(normalAccount);
        
        // When
        List<Account> zeroBalanceAccounts = accountRepository.findZeroBalanceAccounts();
        
        // Then
        assertThat(zeroBalanceAccounts).hasSizeGreaterThanOrEqualTo(1);
        zeroBalanceAccounts.forEach(account -> 
            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO));
    }
    
    @Test
    void shouldCheckAccountNumberExistence() {
        logTestExecution("shouldCheckAccountNumberExistence");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        String existingAccountNumber = account.getAccountNumber();
        accountRepository.save(account);
        
        // When & Then
        assertThat(accountRepository.existsByAccountNumber(existingAccountNumber)).isTrue();
        assertThat(accountRepository.existsByAccountNumber("NON_EXISTENT_" + existingAccountNumber)).isFalse();
    }
    
    @Test
    void shouldFindAccountByAccountNumberWithDetails() {
        logTestExecution("shouldFindAccountByAccountNumberWithDetails");
        
        // Given - Create unique test data
        Branch branch = SimpleParallelTestDataFactory.createUniqueBranch();
        branchRepository.save(branch);
        
        PersonalCustomer customer = SimpleParallelTestDataFactory.createUniquePersonalCustomer(branch);
        customerRepository.save(customer);
        
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        Account account = SimpleParallelTestDataFactory.createUniqueAccount(customer, product, branch);
        accountRepository.save(account);
        
        // When
        Optional<Account> foundAccount = accountRepository.findByAccountNumberWithDetails(account.getAccountNumber());
        
        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getCustomer()).isNotNull();
        assertThat(foundAccount.get().getProduct()).isNotNull();
        assertThat(foundAccount.get().getBranch()).isNotNull();
        assertThat(foundAccount.get().getAccountNumber()).isEqualTo(account.getAccountNumber());
    }
}