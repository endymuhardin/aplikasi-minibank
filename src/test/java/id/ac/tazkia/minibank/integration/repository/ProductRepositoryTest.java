package id.ac.tazkia.minibank.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;

import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.integration.ParallelBaseRepositoryTest;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.util.SimpleParallelTestDataFactory;

/**
 * ProductRepository tests optimized for parallel execution.
 * Uses dynamic test data to prevent conflicts during concurrent execution.
 * Covers all test methods from the original ProductRepositoryTest without simplification.
 * Note: Using SAME_THREAD execution to avoid transaction management conflicts.
 */
@org.junit.jupiter.api.parallel.Execution(org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD)
class ProductRepositoryTest extends ParallelBaseRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProductByCode() {
        logTestExecution("shouldSaveAndFindProductByCode");
        
        // Given - Create unique test data
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        
        // When
        Product savedProduct = productRepository.save(product);
        
        // Then
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getProductCode()).isEqualTo(product.getProductCode());
        assertThat(savedProduct.getProductType()).isEqualTo(product.getProductType());
        
        // Verify we can find by product code
        Optional<Product> foundProduct = productRepository.findByProductCode(product.getProductCode());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getProductCode()).isEqualTo(product.getProductCode());
    }

    @Test
    void shouldFindProductsByType() {
        logTestExecution("shouldFindProductsByType");
        
        // Given - Create unique test data
        Product wadiahProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(wadiahProduct);
        
        Product mudharabahProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_MUDHARABAH);
        productRepository.save(mudharabahProduct);
        
        Product depositProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.DEPOSITO_MUDHARABAH);
        productRepository.save(depositProduct);
        
        // When
        List<Product> wadiahProducts = productRepository.findByProductType(Product.ProductType.TABUNGAN_WADIAH);
        List<Product> mudharabahProducts = productRepository.findByProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        List<Product> depositProducts = productRepository.findByProductType(Product.ProductType.DEPOSITO_MUDHARABAH);
        
        // Then
        assertThat(wadiahProducts).hasSizeGreaterThanOrEqualTo(1);
        assertThat(mudharabahProducts).hasSizeGreaterThanOrEqualTo(1);
        assertThat(depositProducts).hasSizeGreaterThanOrEqualTo(1);
        
        wadiahProducts.forEach(product -> 
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.TABUNGAN_WADIAH));
        mudharabahProducts.forEach(product -> 
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.TABUNGAN_MUDHARABAH));
        depositProducts.forEach(product -> 
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.DEPOSITO_MUDHARABAH));
            
        // Verify our specific products are included
        boolean hasOurWadiahProduct = wadiahProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(wadiahProduct.getProductCode()));
        boolean hasOurMudharabahProduct = mudharabahProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(mudharabahProduct.getProductCode()));
        boolean hasOurDepositProduct = depositProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(depositProduct.getProductCode()));
            
        assertThat(hasOurWadiahProduct).isTrue();
        assertThat(hasOurMudharabahProduct).isTrue();
        assertThat(hasOurDepositProduct).isTrue();
    }

    @Test
    void shouldFindActiveProducts() {
        logTestExecution("shouldFindActiveProducts");
        
        // Given - Create unique test data
        Product activeProduct1 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        activeProduct1.setIsActive(true);
        productRepository.save(activeProduct1);
        
        Product activeProduct2 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_MUDHARABAH);
        activeProduct2.setIsActive(true);
        productRepository.save(activeProduct2);
        
        Product inactiveProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.DEPOSITO_MUDHARABAH);
        inactiveProduct.setIsActive(false);
        productRepository.save(inactiveProduct);
        
        // When
        List<Product> activeProducts = productRepository.findByIsActiveTrue();
        
        // Then
        assertThat(activeProducts).hasSizeGreaterThanOrEqualTo(2);
        activeProducts.forEach(product -> assertThat(product.getIsActive()).isTrue());
        
        boolean hasActiveProduct1 = activeProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(activeProduct1.getProductCode()));
        boolean hasActiveProduct2 = activeProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(activeProduct2.getProductCode()));
        boolean hasInactiveProduct = activeProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(inactiveProduct.getProductCode()));
            
        assertThat(hasActiveProduct1).isTrue();
        assertThat(hasActiveProduct2).isTrue();
        assertThat(hasInactiveProduct).isFalse();
    }

    @Test
    void shouldFindActiveProductsByType() {
        logTestExecution("shouldFindActiveProductsByType");
        
        // Given - Create unique test data
        Product activeWadiahProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        activeWadiahProduct.setIsActive(true);
        productRepository.save(activeWadiahProduct);
        
        Product inactiveWadiahProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        inactiveWadiahProduct.setIsActive(false);
        productRepository.save(inactiveWadiahProduct);
        
        Product activeMudharabahProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_MUDHARABAH);
        activeMudharabahProduct.setIsActive(true);
        productRepository.save(activeMudharabahProduct);
        
        // When
        List<Product> activeWadiahProducts = productRepository.findByIsActiveTrueAndProductType(Product.ProductType.TABUNGAN_WADIAH);
        List<Product> activeMudharabahProducts = productRepository.findByIsActiveTrueAndProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        
        // Then
        assertThat(activeWadiahProducts).hasSizeGreaterThanOrEqualTo(1);
        assertThat(activeMudharabahProducts).hasSizeGreaterThanOrEqualTo(1);
        
        activeWadiahProducts.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.TABUNGAN_WADIAH);
        });
        activeMudharabahProducts.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.TABUNGAN_MUDHARABAH);
        });
        
        boolean hasActiveWadiahProduct = activeWadiahProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(activeWadiahProduct.getProductCode()));
        boolean hasActiveMudharabahProduct = activeMudharabahProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(activeMudharabahProduct.getProductCode()));
        boolean hasInactiveWadiahProduct = activeWadiahProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(inactiveWadiahProduct.getProductCode()));
            
        assertThat(hasActiveWadiahProduct).isTrue();
        assertThat(hasActiveMudharabahProduct).isTrue();
        assertThat(hasInactiveWadiahProduct).isFalse();
    }

    @Test
    void shouldFindDefaultProduct() {
        logTestExecution("shouldFindDefaultProduct");
        
        // Given - Create unique test data but check if any default exists first
        Optional<Product> existingDefault = productRepository.findByIsActiveTrueAndIsDefaultTrue();
        
        if (existingDefault.isEmpty()) {
            Product defaultProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
            defaultProduct.setIsActive(true);
            defaultProduct.setIsDefault(true);
            productRepository.save(defaultProduct);
        }
        
        // When
        Optional<Product> foundDefault = productRepository.findByIsActiveTrueAndIsDefaultTrue();
        
        // Then
        assertThat(foundDefault).isPresent();
        assertThat(foundDefault.get().getIsDefault()).isTrue();
        assertThat(foundDefault.get().getIsActive()).isTrue();
    }

    @Test
    @ResourceLock("product-category-test")
    void shouldFindProductsByCategory() {
        logTestExecution("shouldFindProductsByCategory");
        
        // Given - Create unique test data with same category
        String uniqueCategory = "Test Category " + System.currentTimeMillis() + "_" + Thread.currentThread().getName().hashCode();
        
        Product product1 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        product1.setProductCategory(uniqueCategory);
        productRepository.save(product1);
        
        Product product2 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_MUDHARABAH);
        product2.setProductCategory(uniqueCategory);
        productRepository.save(product2);
        
        Product product3 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.DEPOSITO_MUDHARABAH);
        product3.setProductCategory(uniqueCategory);
        productRepository.save(product3);
        
        // When
        List<Product> categoryProducts = productRepository.findByProductCategory(uniqueCategory);
        
        // Then
        assertThat(categoryProducts).hasSize(3);
        categoryProducts.forEach(product -> 
            assertThat(product.getProductCategory()).isEqualTo(uniqueCategory));
            
        boolean hasProduct1 = categoryProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(product1.getProductCode()));
        boolean hasProduct2 = categoryProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(product2.getProductCode()));
        boolean hasProduct3 = categoryProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(product3.getProductCode()));
            
        assertThat(hasProduct1).isTrue();
        assertThat(hasProduct2).isTrue();
        assertThat(hasProduct3).isTrue();
    }

    @Test
    void shouldCountActiveProductsByType() {
        logTestExecution("shouldCountActiveProductsByType");
        
        // Given - Create unique test data
        Long initialWadiahCount = productRepository.countActiveByProductType(Product.ProductType.TABUNGAN_WADIAH);
        Long initialMudharabahCount = productRepository.countActiveByProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        Long initialDepositCount = productRepository.countActiveByProductType(Product.ProductType.DEPOSITO_MUDHARABAH);
        
        Product wadiahProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        wadiahProduct.setIsActive(true);
        productRepository.save(wadiahProduct);
        
        Product mudharabahProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_MUDHARABAH);
        mudharabahProduct.setIsActive(true);
        productRepository.save(mudharabahProduct);
        
        Product depositProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.DEPOSITO_MUDHARABAH);
        depositProduct.setIsActive(true);
        productRepository.save(depositProduct);
        
        // Add some inactive products to ensure they're not counted
        Product inactiveWadiahProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        inactiveWadiahProduct.setIsActive(false);
        productRepository.save(inactiveWadiahProduct);
        
        // When
        Long finalWadiahCount = productRepository.countActiveByProductType(Product.ProductType.TABUNGAN_WADIAH);
        Long finalMudharabahCount = productRepository.countActiveByProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        Long finalDepositCount = productRepository.countActiveByProductType(Product.ProductType.DEPOSITO_MUDHARABAH);
        
        // Then
        assertThat(finalWadiahCount).isEqualTo(initialWadiahCount + 1); // Only active products counted
        assertThat(finalMudharabahCount).isEqualTo(initialMudharabahCount + 1);
        assertThat(finalDepositCount).isEqualTo(initialDepositCount + 1);
    }

    @Test
    void shouldFindDistinctActiveCategories() {
        logTestExecution("shouldFindDistinctActiveCategories");
        
        // Given - Create unique test data with unique categories
        String uniqueCategory1 = "Test Category A " + System.currentTimeMillis();
        String uniqueCategory2 = "Test Category B " + System.currentTimeMillis();
        String uniqueCategory3 = "Test Category C " + System.currentTimeMillis();
        
        Product product1 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        product1.setProductCategory(uniqueCategory1);
        product1.setIsActive(true);
        productRepository.save(product1);
        
        Product product2 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_MUDHARABAH);
        product2.setProductCategory(uniqueCategory2);
        product2.setIsActive(true);
        productRepository.save(product2);
        
        Product product3 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.DEPOSITO_MUDHARABAH);
        product3.setProductCategory(uniqueCategory3);
        product3.setIsActive(true);
        productRepository.save(product3);
        
        // Add duplicate category to test distinct behavior
        Product product4 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        product4.setProductCategory(uniqueCategory1); // Same as product1
        product4.setIsActive(true);
        productRepository.save(product4);
        
        // Add inactive product with same category - should not appear in results
        Product inactiveProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        inactiveProduct.setProductCategory(uniqueCategory1);
        inactiveProduct.setIsActive(false);
        productRepository.save(inactiveProduct);
        
        // When
        List<String> categories = productRepository.findDistinctActiveCategories();
        
        // Then
        assertThat(categories).isNotEmpty();
        assertThat(categories).contains(uniqueCategory1, uniqueCategory2, uniqueCategory3);
        
        // Verify no duplicates in the result
        long countCategory1 = categories.stream()
            .filter(cat -> cat.equals(uniqueCategory1))
            .count();
        assertThat(countCategory1).isEqualTo(1);
    }

    @Test
    void shouldCheckProductCodeExistence() {
        logTestExecution("shouldCheckProductCodeExistence");
        
        // Given - Create unique test data
        Product product = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        productRepository.save(product);
        
        // When & Then
        assertThat(productRepository.existsByProductCode(product.getProductCode())).isTrue();
        assertThat(productRepository.existsByProductCode("NONEXISTENT_" + product.getProductCode())).isFalse();
    }

    @Test
    void shouldFindCurrentlyAvailableProducts() {
        logTestExecution("shouldFindCurrentlyAvailableProducts");
        
        // Given - Create unique test data
        Product currentProduct1 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        currentProduct1.setIsActive(true);
        currentProduct1.setLaunchDate(LocalDate.now().minusDays(1)); // Launched yesterday
        productRepository.save(currentProduct1);
        
        Product currentProduct2 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_MUDHARABAH);
        currentProduct2.setIsActive(true);
        currentProduct2.setLaunchDate(LocalDate.now()); // Launch today
        productRepository.save(currentProduct2);
        
        Product futureProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.DEPOSITO_MUDHARABAH);
        futureProduct.setIsActive(true);
        futureProduct.setLaunchDate(LocalDate.now().plusDays(1)); // Launch tomorrow
        productRepository.save(futureProduct);
        
        Product inactiveProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        inactiveProduct.setIsActive(false);
        inactiveProduct.setLaunchDate(LocalDate.now().minusDays(1)); // Launched but inactive
        productRepository.save(inactiveProduct);
        
        // When
        List<Product> availableProducts = productRepository.findCurrentlyAvailableProducts();
        
        // Then
        assertThat(availableProducts).hasSizeGreaterThanOrEqualTo(2);
        availableProducts.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getLaunchDate()).isBeforeOrEqualTo(LocalDate.now());
        });
        
        boolean hasCurrentProduct1 = availableProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(currentProduct1.getProductCode()));
        boolean hasCurrentProduct2 = availableProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(currentProduct2.getProductCode()));
        boolean hasFutureProduct = availableProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(futureProduct.getProductCode()));
        boolean hasInactiveProduct = availableProducts.stream()
            .anyMatch(p -> p.getProductCode().equals(inactiveProduct.getProductCode()));
            
        assertThat(hasCurrentProduct1).isTrue();
        assertThat(hasCurrentProduct2).isTrue();
        assertThat(hasFutureProduct).isFalse();
        assertThat(hasInactiveProduct).isFalse();
    }

    @Test
    void shouldFindActiveProductsWithFilters() {
        logTestExecution("shouldFindActiveProductsWithFilters");
        
        // Given - Create unique test data with specific name pattern
        String searchTerm = "UniqueSearch" + System.currentTimeMillis();
        String uniqueCategory = "FilterCategory" + System.currentTimeMillis();
        
        Product matchingProduct1 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        matchingProduct1.setProductName("Test " + searchTerm + " Product 1");
        matchingProduct1.setProductCategory(uniqueCategory);
        matchingProduct1.setDescription("Description for " + searchTerm);
        matchingProduct1.setIsActive(true);
        productRepository.save(matchingProduct1);
        
        Product matchingProduct2 = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        matchingProduct2.setProductName("Another " + searchTerm + " Product 2");
        matchingProduct2.setProductCategory(uniqueCategory);
        matchingProduct2.setDescription("Another description for " + searchTerm);
        matchingProduct2.setIsActive(true);
        productRepository.save(matchingProduct2);
        
        Product nonMatchingProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        nonMatchingProduct.setProductName("Different Name");
        nonMatchingProduct.setProductCategory("Different Category");
        nonMatchingProduct.setDescription("Different Description");
        nonMatchingProduct.setIsActive(true);
        productRepository.save(nonMatchingProduct);
        
        Product inactiveMatchingProduct = SimpleParallelTestDataFactory.createUniqueProduct(Product.ProductType.TABUNGAN_WADIAH);
        inactiveMatchingProduct.setProductName("Inactive " + searchTerm + " Product");
        inactiveMatchingProduct.setDescription("Inactive Description");
        inactiveMatchingProduct.setIsActive(false);
        productRepository.save(inactiveMatchingProduct);
        
        // Test with empty strings for category and search term
        List<Product> typeFilterResults = productRepository.findActiveProductsWithFilters(
            Product.ProductType.TABUNGAN_WADIAH, "", "");
        
        // Test with specific category and empty search term
        List<Product> categoryFilterResults = productRepository.findActiveProductsWithFilters(
            null, uniqueCategory, "");
            
        // Test with specific search term and empty category
        List<Product> searchTermResults = productRepository.findActiveProductsWithFilters(
             null, "", searchTerm);
            
        // Test with combined filters (type and category)
        List<Product> combinedFilterResults = productRepository.findActiveProductsWithFilters(
            Product.ProductType.TABUNGAN_WADIAH, uniqueCategory, "");
        
        // Test with null category and null search term
        List<Product> nullCategoryNullSearchTermResults = productRepository.findActiveProductsWithFilters(
            Product.ProductType.TABUNGAN_WADIAH, null, null);

        // Test with null category and specific search term
        List<Product> nullCategorySearchTermResults = productRepository.findActiveProductsWithFilters(
            null, null, searchTerm);

        // Test with specific category and null search term
        List<Product> categoryNullSearchTermResults = productRepository.findActiveProductsWithFilters(
            null, uniqueCategory, null);

        // Then
        assertThat(typeFilterResults).hasSizeGreaterThanOrEqualTo(3); // Our products + possibly others
        typeFilterResults.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.TABUNGAN_WADIAH);
        });
        
        assertThat(categoryFilterResults).hasSizeGreaterThanOrEqualTo(2);
        categoryFilterResults.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductCategory()).isEqualTo(uniqueCategory);
        });
        
        assertThat(searchTermResults).hasSizeGreaterThanOrEqualTo(2);
        searchTermResults.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductName()).contains(searchTerm);
        });
        
        assertThat(combinedFilterResults).hasSizeGreaterThanOrEqualTo(2);
        combinedFilterResults.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.TABUNGAN_WADIAH);
            assertThat(product.getProductCategory()).isEqualTo(uniqueCategory);
        });

        // Assertions for null category and search term
        assertThat(nullCategoryNullSearchTermResults).hasSizeGreaterThanOrEqualTo(3); // All active TABUNGAN_WADIAH products
        nullCategoryNullSearchTermResults.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductType()).isEqualTo(Product.ProductType.TABUNGAN_WADIAH);
        });

        assertThat(nullCategorySearchTermResults).hasSizeGreaterThanOrEqualTo(2); // Products matching searchTerm
        nullCategorySearchTermResults.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductName()).contains(searchTerm);
        });

        assertThat(categoryNullSearchTermResults).hasSizeGreaterThanOrEqualTo(2); // Products matching category
        categoryNullSearchTermResults.forEach(product -> {
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getProductCategory()).isEqualTo(uniqueCategory);
        });
        
        // Verify our specific products are included where expected
        boolean hasMatchingProduct1 = combinedFilterResults.stream()
            .anyMatch(p -> p.getProductCode().equals(matchingProduct1.getProductCode()));
        boolean hasMatchingProduct2 = combinedFilterResults.stream()
            .anyMatch(p -> p.getProductCode().equals(matchingProduct2.getProductCode()));
        boolean hasNonMatchingProduct = combinedFilterResults.stream()
            .anyMatch(p -> p.getProductCode().equals(nonMatchingProduct.getProductCode()));
        boolean hasInactiveProduct = combinedFilterResults.stream()
            .anyMatch(p -> p.getProductCode().equals(inactiveMatchingProduct.getProductCode()));
            
        assertThat(hasMatchingProduct1).isTrue();
        assertThat(hasMatchingProduct2).isTrue();
        assertThat(hasNonMatchingProduct).isFalse();
        assertThat(hasInactiveProduct).isFalse();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/products.csv", numLinesToSkip = 1)
    @ResourceLock("parameterized-test-transaction")
    void shouldSaveAndFindProductFromCsv(
            String productCode, String productName, String productType, String productCategory,
            String description, String isActiveStr, String isDefaultStr, String currency,
            String minimumOpeningBalanceStr, String minimumBalanceStr, String maximumBalanceStr,
            String dailyWithdrawalLimitStr, String monthlyTransactionLimitStr,
            String profitSharingRatioStr, String profitSharingType, String profitDistributionFrequency,
            String nisbahCustomerStr, String nisbahBankStr, String isShariahCompliantStr,
            String monthlyMaintenanceFeeStr, String atmWithdrawalFeeStr, String interBankTransferFeeStr,
            String belowMinimumBalanceFeeStr, String accountClosureFeeStr, String freeTransactionsPerMonthStr,
            String excessTransactionFeeStr, String allowOverdraftStr, String requireMaintainingBalanceStr,
            String minCustomerAgeStr, String maxCustomerAgeStr, String allowedCustomerTypes, String requiredDocuments) {

        logTestExecution("shouldSaveAndFindProductFromCsv: " + productType);

        // Given - Create product from CSV data with unique product code to avoid conflicts
        // Keep product code under 20 chars limit by using shorter unique suffix
        String shortTimestamp = String.valueOf(System.currentTimeMillis() % 100000); // Last 5 digits
        String threadHash = String.valueOf(Math.abs(Thread.currentThread().getName().hashCode()) % 100); // 2 digits
        String uniqueProductCode = productCode + "_" + shortTimestamp + threadHash; // Should be around 6+1+5+2=14 chars
        
        Product product = new Product();
        product.setProductCode(uniqueProductCode);
        product.setProductName(productName);
        product.setProductType(Product.ProductType.valueOf(productType));
        product.setProductCategory(productCategory);
        product.setDescription(description);
        product.setIsActive(Boolean.parseBoolean(isActiveStr));
        product.setIsDefault(false); // Avoid conflicts with default products
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
        
        // Set nisbah values if provided and product type requires them
        if (nisbahCustomerStr != null && !nisbahCustomerStr.isEmpty() && 
            (productType.equals("TABUNGAN_MUDHARABAH") || productType.equals("DEPOSITO_MUDHARABAH") || productType.equals("PEMBIAYAAN_MUSHARAKAH"))) {
            product.setNisbahCustomer(new BigDecimal(nisbahCustomerStr));
            product.setNisbahBank(new BigDecimal(nisbahBankStr));
        }
        
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

        // Then - Verify product was saved correctly
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getProductCode()).isEqualTo(uniqueProductCode);
        assertThat(savedProduct.getProductName()).isEqualTo(productName);
        assertThat(savedProduct.getProductType().name()).isEqualTo(productType);
        assertThat(savedProduct.getIsActive()).isEqualTo(Boolean.parseBoolean(isActiveStr));

        // Verify we can find by product code
        Optional<Product> foundProduct = productRepository.findByProductCode(uniqueProductCode);
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getProductCode()).isEqualTo(uniqueProductCode);
    }
}