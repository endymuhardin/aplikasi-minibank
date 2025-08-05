package id.ac.tazkia.minibank.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    private Transaction transaction;
    private Account account;
    private Account destinationAccount;
    private PersonalCustomer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        // Setup test customer
        customer = new PersonalCustomer();
        customer.setCustomerNumber("C1000001");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customer.setIdentityNumber("1234567890123456");
        customer.setIdentityType(Customer.IdentityType.KTP);

        // Setup test product
        product = new Product();
        product.setProductCode("SAV001");
        product.setProductName("Basic Savings");
        product.setProductType(Product.ProductType.SAVINGS);
        product.setMinimumBalance(new BigDecimal("10000"));

        // Setup test account
        account = new Account();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setProduct(product);
        account.setAccountNumber("A2000001");
        account.setAccountName("John Doe - Savings");
        account.setBalance(new BigDecimal("100000"));
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setOpenedDate(LocalDate.now());
        account.setCreatedBy("TEST");

        // Setup destination account for transfer tests
        destinationAccount = new Account();
        destinationAccount.setId(UUID.randomUUID());
        destinationAccount.setCustomer(customer);
        destinationAccount.setProduct(product);
        destinationAccount.setAccountNumber("A2000002");
        destinationAccount.setAccountName("John Doe - Destination");
        destinationAccount.setBalance(new BigDecimal("50000"));
        destinationAccount.setStatus(Account.AccountStatus.ACTIVE);
        destinationAccount.setOpenedDate(LocalDate.now());
        destinationAccount.setCreatedBy("TEST");

        // Setup test transaction
        transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccount(account);
        transaction.setTransactionNumber("TXN20240101001");
        transaction.setAmount(new BigDecimal("25000"));
        transaction.setBalanceBefore(new BigDecimal("100000"));
        transaction.setBalanceAfter(new BigDecimal("125000"));
        transaction.setDescription("Test transaction");
        transaction.setReferenceNumber("REF12345");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setProcessedDate(LocalDateTime.now());
        transaction.setCreatedBy("TEST");
    }

    @Test
    void shouldCreateTransactionWithDefaultValues() {
        // Given
        Transaction newTransaction = new Transaction();

        // Then
        assertThat(newTransaction.getCurrency()).isEqualTo("IDR");
        assertThat(newTransaction.getChannel()).isEqualTo(Transaction.TransactionChannel.TELLER);
        assertThat(newTransaction.getTransactionDate()).isNotNull();
        assertThat(newTransaction.getProcessedDate()).isNotNull();
    }

    @Test
    void shouldReturnTrueForDebitTransactionTypes() {
        // Test WITHDRAWAL
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        assertThat(transaction.isDebitTransaction()).isTrue();
        assertThat(transaction.isCreditTransaction()).isFalse();

        // Test TRANSFER_OUT
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER_OUT);
        assertThat(transaction.isDebitTransaction()).isTrue();
        assertThat(transaction.isCreditTransaction()).isFalse();

        // Test FEE
        transaction.setTransactionType(Transaction.TransactionType.FEE);
        assertThat(transaction.isDebitTransaction()).isTrue();
        assertThat(transaction.isCreditTransaction()).isFalse();
    }

    @Test
    void shouldReturnTrueForCreditTransactionTypes() {
        // Test DEPOSIT
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        assertThat(transaction.isCreditTransaction()).isTrue();
        assertThat(transaction.isDebitTransaction()).isFalse();

        // Test TRANSFER_IN
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER_IN);
        assertThat(transaction.isCreditTransaction()).isTrue();
        assertThat(transaction.isDebitTransaction()).isFalse();

        // Test INTEREST
        transaction.setTransactionType(Transaction.TransactionType.INTEREST);
        assertThat(transaction.isCreditTransaction()).isTrue();
        assertThat(transaction.isDebitTransaction()).isFalse();
    }

    @Test
    void shouldReturnFalseForBothDebitAndCreditWhenTransactionTypeIsNull() {
        // Given
        transaction.setTransactionType(null);

        // When & Then
        assertThat(transaction.isDebitTransaction()).isFalse();
        assertThat(transaction.isCreditTransaction()).isFalse();
    }

    @Test
    void shouldCreateDepositTransaction() {
        // Given
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("50000"));
        transaction.setBalanceBefore(new BigDecimal("100000"));
        transaction.setBalanceAfter(new BigDecimal("150000"));
        transaction.setDescription("Cash deposit");
        transaction.setChannel(Transaction.TransactionChannel.TELLER);

        // Then
        assertThat(transaction.getTransactionType()).isEqualTo(Transaction.TransactionType.DEPOSIT);
        assertThat(transaction.getAmount()).isEqualByComparingTo("50000");
        assertThat(transaction.getBalanceBefore()).isEqualByComparingTo("100000");
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo("150000");
        assertThat(transaction.getDescription()).isEqualTo("Cash deposit");
        assertThat(transaction.getChannel()).isEqualTo(Transaction.TransactionChannel.TELLER);
        assertThat(transaction.isCreditTransaction()).isTrue();
        assertThat(transaction.isDebitTransaction()).isFalse();
    }

    @Test
    void shouldCreateWithdrawalTransaction() {
        // Given
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setAmount(new BigDecimal("30000"));
        transaction.setBalanceBefore(new BigDecimal("100000"));
        transaction.setBalanceAfter(new BigDecimal("70000"));
        transaction.setDescription("Cash withdrawal");
        transaction.setChannel(Transaction.TransactionChannel.ATM);

        // Then
        assertThat(transaction.getTransactionType()).isEqualTo(Transaction.TransactionType.WITHDRAWAL);
        assertThat(transaction.getAmount()).isEqualByComparingTo("30000");
        assertThat(transaction.getBalanceBefore()).isEqualByComparingTo("100000");
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo("70000");
        assertThat(transaction.getDescription()).isEqualTo("Cash withdrawal");
        assertThat(transaction.getChannel()).isEqualTo(Transaction.TransactionChannel.ATM);
        assertThat(transaction.isDebitTransaction()).isTrue();
        assertThat(transaction.isCreditTransaction()).isFalse();
    }

    @Test
    void shouldCreateTransferOutTransaction() {
        // Given
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER_OUT);
        transaction.setAmount(new BigDecimal("20000"));
        transaction.setBalanceBefore(new BigDecimal("100000"));
        transaction.setBalanceAfter(new BigDecimal("80000"));
        transaction.setDescription("Transfer to another account");
        transaction.setChannel(Transaction.TransactionChannel.ONLINE);
        transaction.setDestinationAccount(destinationAccount);

        // Then
        assertThat(transaction.getTransactionType()).isEqualTo(Transaction.TransactionType.TRANSFER_OUT);
        assertThat(transaction.getAmount()).isEqualByComparingTo("20000");
        assertThat(transaction.getDestinationAccount()).isEqualTo(destinationAccount);
        assertThat(transaction.getChannel()).isEqualTo(Transaction.TransactionChannel.ONLINE);
        assertThat(transaction.isDebitTransaction()).isTrue();
        assertThat(transaction.isCreditTransaction()).isFalse();
    }

    @Test
    void shouldCreateTransferInTransaction() {
        // Given
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER_IN);
        transaction.setAmount(new BigDecimal("15000"));
        transaction.setBalanceBefore(new BigDecimal("100000"));
        transaction.setBalanceAfter(new BigDecimal("115000"));
        transaction.setDescription("Transfer from another account");
        transaction.setChannel(Transaction.TransactionChannel.TRANSFER);

        // Then
        assertThat(transaction.getTransactionType()).isEqualTo(Transaction.TransactionType.TRANSFER_IN);
        assertThat(transaction.getAmount()).isEqualByComparingTo("15000");
        assertThat(transaction.getChannel()).isEqualTo(Transaction.TransactionChannel.TRANSFER);
        assertThat(transaction.isCreditTransaction()).isTrue();
        assertThat(transaction.isDebitTransaction()).isFalse();
    }

    @Test
    void shouldCreateInterestTransaction() {
        // Given
        transaction.setTransactionType(Transaction.TransactionType.INTEREST);
        transaction.setAmount(new BigDecimal("1250.50"));
        transaction.setBalanceBefore(new BigDecimal("100000"));
        transaction.setBalanceAfter(new BigDecimal("101250.50"));
        transaction.setDescription("Monthly interest payment");
        transaction.setChannel(Transaction.TransactionChannel.TELLER);

        // Then
        assertThat(transaction.getTransactionType()).isEqualTo(Transaction.TransactionType.INTEREST);
        assertThat(transaction.getAmount()).isEqualByComparingTo("1250.50");
        assertThat(transaction.getDescription()).isEqualTo("Monthly interest payment");
        assertThat(transaction.isCreditTransaction()).isTrue();
        assertThat(transaction.isDebitTransaction()).isFalse();
    }

    @Test
    void shouldCreateFeeTransaction() {
        // Given
        transaction.setTransactionType(Transaction.TransactionType.FEE);
        transaction.setAmount(new BigDecimal("5000"));
        transaction.setBalanceBefore(new BigDecimal("100000"));
        transaction.setBalanceAfter(new BigDecimal("95000"));
        transaction.setDescription("Monthly maintenance fee");
        transaction.setChannel(Transaction.TransactionChannel.TELLER);

        // Then
        assertThat(transaction.getTransactionType()).isEqualTo(Transaction.TransactionType.FEE);
        assertThat(transaction.getAmount()).isEqualByComparingTo("5000");
        assertThat(transaction.getDescription()).isEqualTo("Monthly maintenance fee");
        assertThat(transaction.isDebitTransaction()).isTrue();
        assertThat(transaction.isCreditTransaction()).isFalse();
    }

    @Test
    void shouldCreateMobileTransaction() {
        // Given
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setChannel(Transaction.TransactionChannel.MOBILE);
        transaction.setAmount(new BigDecimal("75000"));
        transaction.setDescription("Mobile deposit");

        // Then
        assertThat(transaction.getChannel()).isEqualTo(Transaction.TransactionChannel.MOBILE);
        assertThat(transaction.getDescription()).isEqualTo("Mobile deposit");
    }

    @Test
    void shouldHandleDecimalAmounts() {
        // Given
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("1250.75"));
        transaction.setBalanceBefore(new BigDecimal("999.25"));
        transaction.setBalanceAfter(new BigDecimal("2250.00"));

        // Then
        assertThat(transaction.getAmount()).isEqualByComparingTo("1250.75");
        assertThat(transaction.getBalanceBefore()).isEqualByComparingTo("999.25");
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo("2250.00");
    }

    @Test
    void shouldSetCustomCurrency() {
        // Given
        transaction.setCurrency("USD");

        // Then
        assertThat(transaction.getCurrency()).isEqualTo("USD");
    }

    @Test
    void shouldSetReferenceNumber() {
        // Given
        String referenceNumber = "REF-2024-001-12345";
        transaction.setReferenceNumber(referenceNumber);

        // Then
        assertThat(transaction.getReferenceNumber()).isEqualTo(referenceNumber);
    }

    @Test
    void shouldSetTransactionAndProcessedDates() {
        // Given
        LocalDateTime transactionDate = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        LocalDateTime processedDate = LocalDateTime.of(2024, 1, 15, 10, 35, 0);
        
        transaction.setTransactionDate(transactionDate);
        transaction.setProcessedDate(processedDate);

        // Then
        assertThat(transaction.getTransactionDate()).isEqualTo(transactionDate);
        assertThat(transaction.getProcessedDate()).isEqualTo(processedDate);
    }

    @Test
    void shouldTestTransactionTypeEnum() {
        // Test all enum values
        assertThat(Transaction.TransactionType.valueOf("DEPOSIT")).isEqualTo(Transaction.TransactionType.DEPOSIT);
        assertThat(Transaction.TransactionType.valueOf("WITHDRAWAL")).isEqualTo(Transaction.TransactionType.WITHDRAWAL);
        assertThat(Transaction.TransactionType.valueOf("TRANSFER_IN")).isEqualTo(Transaction.TransactionType.TRANSFER_IN);
        assertThat(Transaction.TransactionType.valueOf("TRANSFER_OUT")).isEqualTo(Transaction.TransactionType.TRANSFER_OUT);
        assertThat(Transaction.TransactionType.valueOf("INTEREST")).isEqualTo(Transaction.TransactionType.INTEREST);
        assertThat(Transaction.TransactionType.valueOf("FEE")).isEqualTo(Transaction.TransactionType.FEE);

        // Test enum values array
        Transaction.TransactionType[] types = Transaction.TransactionType.values();
        assertThat(types).hasSize(6);
        assertThat(types).containsExactly(
            Transaction.TransactionType.DEPOSIT,
            Transaction.TransactionType.WITHDRAWAL,
            Transaction.TransactionType.TRANSFER_IN,
            Transaction.TransactionType.TRANSFER_OUT,
            Transaction.TransactionType.INTEREST,
            Transaction.TransactionType.FEE
        );
    }

    @Test
    void shouldTestTransactionChannelEnum() {
        // Test all enum values
        assertThat(Transaction.TransactionChannel.valueOf("TELLER")).isEqualTo(Transaction.TransactionChannel.TELLER);
        assertThat(Transaction.TransactionChannel.valueOf("ATM")).isEqualTo(Transaction.TransactionChannel.ATM);
        assertThat(Transaction.TransactionChannel.valueOf("ONLINE")).isEqualTo(Transaction.TransactionChannel.ONLINE);
        assertThat(Transaction.TransactionChannel.valueOf("MOBILE")).isEqualTo(Transaction.TransactionChannel.MOBILE);
        assertThat(Transaction.TransactionChannel.valueOf("TRANSFER")).isEqualTo(Transaction.TransactionChannel.TRANSFER);

        // Test enum values array
        Transaction.TransactionChannel[] channels = Transaction.TransactionChannel.values();
        assertThat(channels).hasSize(5);
        assertThat(channels).containsExactly(
            Transaction.TransactionChannel.TELLER,
            Transaction.TransactionChannel.ATM,
            Transaction.TransactionChannel.ONLINE,
            Transaction.TransactionChannel.MOBILE,
            Transaction.TransactionChannel.TRANSFER
        );
    }

    @Test
    void shouldCreateCompleteTransactionWithAllFields() {
        // Given
        Transaction completeTransaction = new Transaction();
        LocalDateTime now = LocalDateTime.now();
        
        // When
        completeTransaction.setId(UUID.randomUUID());
        completeTransaction.setAccount(account);
        completeTransaction.setDestinationAccount(destinationAccount);
        completeTransaction.setTransactionNumber("TXN20240115001");
        completeTransaction.setTransactionType(Transaction.TransactionType.TRANSFER_OUT);
        completeTransaction.setAmount(new BigDecimal("50000"));
        completeTransaction.setCurrency("IDR");
        completeTransaction.setBalanceBefore(new BigDecimal("100000"));
        completeTransaction.setBalanceAfter(new BigDecimal("50000"));
        completeTransaction.setDescription("Transfer to savings account");
        completeTransaction.setReferenceNumber("REF-TRANSFER-001");
        completeTransaction.setChannel(Transaction.TransactionChannel.ONLINE);
        completeTransaction.setTransactionDate(now);
        completeTransaction.setProcessedDate(now);
        completeTransaction.setCreatedBy("SYSTEM");

        // Then
        assertThat(completeTransaction.getId()).isNotNull();
        assertThat(completeTransaction.getAccount()).isEqualTo(account);
        assertThat(completeTransaction.getDestinationAccount()).isEqualTo(destinationAccount);
        assertThat(completeTransaction.getTransactionNumber()).isEqualTo("TXN20240115001");
        assertThat(completeTransaction.getTransactionType()).isEqualTo(Transaction.TransactionType.TRANSFER_OUT);
        assertThat(completeTransaction.getAmount()).isEqualByComparingTo("50000");
        assertThat(completeTransaction.getCurrency()).isEqualTo("IDR");
        assertThat(completeTransaction.getBalanceBefore()).isEqualByComparingTo("100000");
        assertThat(completeTransaction.getBalanceAfter()).isEqualByComparingTo("50000");
        assertThat(completeTransaction.getDescription()).isEqualTo("Transfer to savings account");
        assertThat(completeTransaction.getReferenceNumber()).isEqualTo("REF-TRANSFER-001");
        assertThat(completeTransaction.getChannel()).isEqualTo(Transaction.TransactionChannel.ONLINE);
        assertThat(completeTransaction.getTransactionDate()).isEqualTo(now);
        assertThat(completeTransaction.getProcessedDate()).isEqualTo(now);
        assertThat(completeTransaction.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(completeTransaction.isDebitTransaction()).isTrue();
        assertThat(completeTransaction.isCreditTransaction()).isFalse();
    }
}