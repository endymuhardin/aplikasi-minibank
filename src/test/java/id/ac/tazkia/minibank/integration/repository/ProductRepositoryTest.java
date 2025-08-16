package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.integration.BaseRepositoryTest;
import id.ac.tazkia.minibank.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/products.csv", numLinesToSkip = 1)
    void shouldSaveAndFindProductFromCsv(
            String productCode,
            String productName,
            String productType,
            String productCategory,
            String description,
            String isActiveStr,
            String isDefaultStr,
            String currency,
            String minimumOpeningBalanceStr,
            String minimumBalanceStr,
            String maximumBalanceStr,
            String dailyWithdrawalLimitStr,
            String monthlyTransactionLimitStr,
            String profitSharingRatioStr,
            String profitSharingType,
            String profitDistributionFrequency,
            String nisbahCustomerStr,
            String nisbahBankStr,
            String isShariahCompliantStr,
            String monthlyMaintenanceFeeStr,
            String atmWithdrawalFeeStr,
            String interBankTransferFeeStr,
            String belowMinimumBalanceFeeStr,
            String accountClosureFeeStr,
            String freeTransactionsPerMonthStr,
            String excessTransactionFeeStr,
            String allowOverdraftStr,
            String requireMaintainingBalanceStr,
            String minCustomerAgeStr,
            String maxCustomerAgeStr,
            String allowedCustomerTypes,
            String requiredDocuments) {

        // Given - Create product from CSV data
        Product product = new Product();
        product.setProductCode(productCode);
        product.setProductName(productName);
        product.setProductType(Product.ProductType.valueOf(productType));
        product.setProductCategory(productCategory);
        product.setDescription(description);
        product.setIsActive(Boolean.parseBoolean(isActiveStr));
        product.setIsDefault(Boolean.parseBoolean(isDefaultStr));
        product.setCurrency(currency);
        
        product.setMinimumOpeningBalance(new BigDecimal(minimumOpeningBalanceStr));
        product.setMinimumBalance(new BigDecimal(minimumBalanceStr));
        if (maximumBalanceStr != null && !maximumBalanceStr.isEmpty()) {
            product.setMaximumBalance(new BigDecimal(maximumBalanceStr));
        }
        if (dailyWithdrawalLimitStr != null && !dailyWithdrawalLimitStr.isEmpty()) {
            product.setDailyWithdrawalLimit(new BigDecimal(dailyWithdrawalLimitStr));
        }
        if (monthlyTransactionLimitStr != null && !monthlyTransactionLimitStr.isEmpty()) {
            product.setMonthlyTransactionLimit(Integer.parseInt(monthlyTransactionLimitStr));
        }
        
        product.setProfitSharingRatio(new BigDecimal(profitSharingRatioStr));
        product.setProfitSharingType(Product.ProfitSharingType.valueOf(profitSharingType));
        product.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.valueOf(profitDistributionFrequency));
        
        // Set nisbah values if provided
        if (nisbahCustomerStr != null && !nisbahCustomerStr.isEmpty()) {
            product.setNisbahCustomer(new BigDecimal(nisbahCustomerStr));
        }
        if (nisbahBankStr != null && !nisbahBankStr.isEmpty()) {
            product.setNisbahBank(new BigDecimal(nisbahBankStr));
        }
        
        // Set Shariah compliance
        if (isShariahCompliantStr != null && !isShariahCompliantStr.isEmpty()) {
            product.setIsShariahCompliant(Boolean.parseBoolean(isShariahCompliantStr));
        }
        
        product.setMonthlyMaintenanceFee(new BigDecimal(monthlyMaintenanceFeeStr));
        product.setAtmWithdrawalFee(new BigDecimal(atmWithdrawalFeeStr));
        product.setInterBankTransferFee(new BigDecimal(interBankTransferFeeStr));
        product.setBelowMinimumBalanceFee(new BigDecimal(belowMinimumBalanceFeeStr));
        product.setAccountClosureFee(new BigDecimal(accountClosureFeeStr));
        
        product.setFreeTransactionsPerMonth(Integer.parseInt(freeTransactionsPerMonthStr));
        product.setExcessTransactionFee(new BigDecimal(excessTransactionFeeStr));
        product.setAllowOverdraft(Boolean.parseBoolean(allowOverdraftStr));
        product.setRequireMaintainingBalance(Boolean.parseBoolean(requireMaintainingBalanceStr));
        
        if (minCustomerAgeStr != null && !minCustomerAgeStr.isEmpty()) {
            product.setMinCustomerAge(Integer.parseInt(minCustomerAgeStr));
        }
        if (maxCustomerAgeStr != null && !maxCustomerAgeStr.isEmpty()) {
            product.setMaxCustomerAge(Integer.parseInt(maxCustomerAgeStr));
        }
        
        product.setAllowedCustomerTypes(allowedCustomerTypes);
        product.setRequiredDocuments(requiredDocuments);
        product.setLaunchDate(LocalDate.now());
        product.setCreatedBy("TEST");

        // When - Save product
        Product savedProduct = productRepository.save(product);
        entityManager.flush();

        // Then - Verify product was saved correctly
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getProductCode()).isEqualTo(productCode);
        assertThat(savedProduct.getProductName()).isEqualTo(productName);
        assertThat(savedProduct.getProductType().name()).isEqualTo(productType);
        assertThat(savedProduct.getIsActive()).isEqualTo(Boolean.parseBoolean(isActiveStr));
        assertThat(savedProduct.getCreatedDate()).isNotNull();

        // Verify we can find by product code
        Optional<Product> foundProduct = productRepository.findByProductCode(productCode);
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getProductCode()).isEqualTo(productCode);
    }

    @Test
    void shouldFindProductsByType() {
        // Given
        saveTestProducts();

        // When
        List<Product> savingsProducts = productRepository.findByProductType(Product.ProductType.SAVINGS);
        List<Product> checkingProducts = productRepository.findByProductType(Product.ProductType.CHECKING);

        // Then
        assertThat(savingsProducts).hasSizeGreaterThan(0);
        assertThat(checkingProducts).hasSizeGreaterThan(0);
        
        savingsProducts.forEach(product -> 
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.SAVINGS));
        checkingProducts.forEach(product -> 
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.CHECKING));
    }

    @Test
    void shouldFindActiveProducts() {
        // Given
        saveTestProducts();

        // When
        List<Product> activeProducts = productRepository.findByIsActiveTrue();

        // Then
        assertThat(activeProducts).hasSizeGreaterThan(0);
        activeProducts.forEach(product -> assertThat(product.getIsActive()).isTrue());
    }

    @Test
    void shouldFindActiveProductsByType() {
        // Given
        saveTestProducts();

        // When
        List<Product> activeSavingsProducts = productRepository.findByIsActiveTrueAndProductType(Product.ProductType.SAVINGS);

        // Then
        assertThat(activeSavingsProducts).hasSizeGreaterThan(0);
        activeSavingsProducts.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.SAVINGS);
        });
    }

    @Test
    void shouldFindDefaultProduct() {
        // Given
        saveTestProducts();

        // When
        Optional<Product> defaultProduct = productRepository.findByIsActiveTrueAndIsDefaultTrue();

        // Then
        assertThat(defaultProduct).isPresent();
        assertThat(defaultProduct.get().getIsDefault()).isTrue();
        assertThat(defaultProduct.get().getIsActive()).isTrue();
    }

    @Test
    void shouldFindProductsByCategory() {
        // Given
        saveTestProducts();

        // When
        List<Product> regularSavings = productRepository.findByProductCategory("Regular Savings");

        // Then
        assertThat(regularSavings).hasSizeGreaterThan(0);
        regularSavings.forEach(product -> 
            assertThat(product.getProductCategory()).isEqualTo("Regular Savings"));
    }

    @Test
    void shouldCountActiveProductsByType() {
        // Given
        saveTestProducts();

        // When
        Long savingsCount = productRepository.countActiveByProductType(Product.ProductType.SAVINGS);
        Long checkingCount = productRepository.countActiveByProductType(Product.ProductType.CHECKING);

        // Then
        assertThat(savingsCount).isGreaterThan(0);
        assertThat(checkingCount).isGreaterThan(0);
    }

    @Test
    void shouldFindDistinctActiveCategories() {
        // Given
        saveTestProducts();

        // When
        List<String> categories = productRepository.findDistinctActiveCategories();

        // Then
        assertThat(categories).isNotEmpty();
        assertThat(categories).contains("Regular Savings", "Premium Savings", "Regular Checking");
    }

    @Test
    void shouldCheckProductCodeExistence() {
        // Given
        saveTestProducts();

        // When & Then
        assertThat(productRepository.existsByProductCode("SAV001")).isTrue();
        assertThat(productRepository.existsByProductCode("NONEXISTENT")).isFalse();
    }

    @Test
    void shouldFindCurrentlyAvailableProducts() {
        // Given
        saveTestProducts();

        // When
        List<Product> availableProducts = productRepository.findCurrentlyAvailableProducts();

        // Then
        assertThat(availableProducts).hasSizeGreaterThan(0);
        availableProducts.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getLaunchDate()).isBeforeOrEqualTo(LocalDate.now());
        });
    }

    @Test
    void shouldFindActiveProductsWithFilters() {
        // Given
        saveTestProducts();

        // When - Search by product type and search term
        List<Product> results = productRepository.findActiveProductsWithFilters(
            Product.ProductType.SAVINGS, null, "Basic");

        // Then
        assertThat(results).hasSizeGreaterThan(0);
        results.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.SAVINGS);
            assertThat(product.getProductName()).containsIgnoringCase("Basic");
        });
    }

    private void saveTestProducts() {
        // Save Basic Savings Account
        Product basicSavings = new Product();
        basicSavings.setProductCode("SAV001");
        basicSavings.setProductName("Basic Savings Account");
        basicSavings.setProductType(Product.ProductType.SAVINGS);
        basicSavings.setProductCategory("Regular Savings");
        basicSavings.setDescription("Basic savings account for individual customers");
        basicSavings.setIsActive(true);
        basicSavings.setIsDefault(true);
        basicSavings.setCurrency("IDR");
        basicSavings.setMinimumOpeningBalance(new BigDecimal("50000.00"));
        basicSavings.setMinimumBalance(new BigDecimal("10000.00"));
        basicSavings.setDailyWithdrawalLimit(new BigDecimal("5000000.00"));
        basicSavings.setMonthlyTransactionLimit(50);
        basicSavings.setProfitSharingRatio(new BigDecimal("0.0275"));
        basicSavings.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
        basicSavings.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        basicSavings.setNisbahCustomer(new BigDecimal("0.7000"));
        basicSavings.setNisbahBank(new BigDecimal("0.3000"));
        basicSavings.setMonthlyMaintenanceFee(new BigDecimal("2500.00"));
        basicSavings.setAtmWithdrawalFee(new BigDecimal("5000.00"));
        basicSavings.setInterBankTransferFee(new BigDecimal("7500.00"));
        basicSavings.setBelowMinimumBalanceFee(new BigDecimal("10000.00"));
        basicSavings.setAccountClosureFee(BigDecimal.ZERO);
        basicSavings.setFreeTransactionsPerMonth(10);
        basicSavings.setExcessTransactionFee(new BigDecimal("2500.00"));
        basicSavings.setAllowOverdraft(false);
        basicSavings.setRequireMaintainingBalance(true);
        basicSavings.setMinCustomerAge(17);
        basicSavings.setAllowedCustomerTypes("PERSONAL");
        basicSavings.setRequiredDocuments("KTP, NPWP (optional)");
        basicSavings.setLaunchDate(LocalDate.now());
        basicSavings.setCreatedBy("TEST");

        // Save Premium Savings Account
        Product premiumSavings = new Product();
        premiumSavings.setProductCode("SAV002");
        premiumSavings.setProductName("Premium Savings Account");
        premiumSavings.setProductType(Product.ProductType.SAVINGS);
        premiumSavings.setProductCategory("Premium Savings");
        premiumSavings.setDescription("Premium savings account with higher profit sharing");
        premiumSavings.setIsActive(true);
        premiumSavings.setIsDefault(false);
        premiumSavings.setCurrency("IDR");
        premiumSavings.setMinimumOpeningBalance(new BigDecimal("1000000.00"));
        premiumSavings.setMinimumBalance(new BigDecimal("500000.00"));
        premiumSavings.setDailyWithdrawalLimit(new BigDecimal("10000000.00"));
        premiumSavings.setMonthlyTransactionLimit(100);
        premiumSavings.setProfitSharingRatio(new BigDecimal("0.0350"));
        premiumSavings.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
        premiumSavings.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        premiumSavings.setNisbahCustomer(new BigDecimal("0.7000"));
        premiumSavings.setNisbahBank(new BigDecimal("0.3000"));
        premiumSavings.setMonthlyMaintenanceFee(new BigDecimal("0.00"));
        premiumSavings.setAtmWithdrawalFee(new BigDecimal("0.00"));
        premiumSavings.setInterBankTransferFee(new BigDecimal("5000.00"));
        premiumSavings.setBelowMinimumBalanceFee(new BigDecimal("25000.00"));
        premiumSavings.setAccountClosureFee(new BigDecimal("0.00"));
        premiumSavings.setFreeTransactionsPerMonth(25);
        premiumSavings.setExcessTransactionFee(new BigDecimal("2500.00"));
        premiumSavings.setAllowOverdraft(false);
        premiumSavings.setRequireMaintainingBalance(true);
        premiumSavings.setMinCustomerAge(21);
        premiumSavings.setAllowedCustomerTypes("PERSONAL");
        premiumSavings.setRequiredDocuments("KTP, NPWP, Slip Gaji");
        premiumSavings.setLaunchDate(LocalDate.now());
        premiumSavings.setCreatedBy("TEST");

        // Save Basic Checking Account
        Product basicChecking = new Product();
        basicChecking.setProductCode("CHK001");
        basicChecking.setProductName("Basic Checking Account");
        basicChecking.setProductType(Product.ProductType.CHECKING);
        basicChecking.setProductCategory("Regular Checking");
        basicChecking.setDescription("Basic checking account with overdraft facility");
        basicChecking.setIsActive(true);
        basicChecking.setIsDefault(false);
        basicChecking.setCurrency("IDR");
        basicChecking.setMinimumOpeningBalance(new BigDecimal("100000.00"));
        basicChecking.setMinimumBalance(new BigDecimal("50000.00"));
        basicChecking.setDailyWithdrawalLimit(new BigDecimal("20000000.00"));
        basicChecking.setMonthlyTransactionLimit(100);
        basicChecking.setProfitSharingRatio(new BigDecimal("0.0100"));
        basicChecking.setProfitSharingType(Product.ProfitSharingType.WADIAH);
        basicChecking.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        basicChecking.setMonthlyMaintenanceFee(new BigDecimal("5000.00"));
        basicChecking.setAtmWithdrawalFee(new BigDecimal("5000.00"));
        basicChecking.setInterBankTransferFee(new BigDecimal("7500.00"));
        basicChecking.setBelowMinimumBalanceFee(new BigDecimal("15000.00"));
        basicChecking.setAccountClosureFee(new BigDecimal("10000.00"));
        basicChecking.setFreeTransactionsPerMonth(20);
        basicChecking.setExcessTransactionFee(new BigDecimal("3000.00"));
        basicChecking.setAllowOverdraft(true);
        basicChecking.setRequireMaintainingBalance(true);
        basicChecking.setMinCustomerAge(18);
        basicChecking.setAllowedCustomerTypes("PERSONAL");
        basicChecking.setRequiredDocuments("KTP, NPWP, Slip Gaji");
        basicChecking.setLaunchDate(LocalDate.now());
        basicChecking.setCreatedBy("TEST");

        productRepository.save(basicSavings);
        productRepository.save(premiumSavings);
        productRepository.save(basicChecking);
        entityManager.flush();
    }
}