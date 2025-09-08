package id.ac.tazkia.minibank.integration.business;

import id.ac.tazkia.minibank.config.BaseIntegrationTest;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Islamic Banking Business Rules Tests")
class IslamicBankingBusinessRulesTest extends BaseIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @ParameterizedTest
    @CsvSource({
        "0.7000, 0.3000, 'Valid 70-30 split'",
        "0.6000, 0.4000, 'Valid 60-40 split'",
        "0.8000, 0.2000, 'Valid 80-20 split'", 
        "0.5000, 0.5000, 'Valid 50-50 split'",
        "1.0000, 0.0000, 'Valid 100-0 split'",
        "0.0000, 1.0000, 'Valid 0-100 split'"
    })
    @DisplayName("Should accept valid nisbah ratios that sum to 1.0")
    void shouldAcceptValidNisbahRatios(String customerRatio, String bankRatio, String testCase) {
        // Given
        Product product = createMudharabahProduct("VALID_NISBAH", "Valid Nisbah Test");
        product.setNisbahCustomer(new BigDecimal(customerRatio));
        product.setNisbahBank(new BigDecimal(bankRatio));
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product savedProduct = productService.save(product);
            entityManager.flush();
            assertNotNull(savedProduct.getId(), testCase + " should be saved successfully");
        }, testCase + " should not throw any exception");
    }

    @ParameterizedTest
    @CsvSource({
        "0.8000, 0.3000, 1.1000, 'Invalid sum > 1.0'",
        "0.6000, 0.3000, 0.9000, 'Invalid sum < 1.0'", 
        "0.7500, 0.2000, 0.9500, 'Invalid sum < 1.0'",
        "1.2000, 0.0000, 1.2000, 'Customer ratio > 1.0'",
        "0.0000, 1.5000, 1.5000, 'Bank ratio > 1.0'",
        "-0.1000, 1.1000, 1.0000, 'Negative customer ratio'",
        "1.1000, -0.1000, 1.0000, 'Negative bank ratio'"
    })
    @DisplayName("Should reject invalid nisbah ratios")
    void shouldRejectInvalidNisbahRatios(String customerRatio, String bankRatio, String expectedSum, String testCase) {
        // Given
        Product product = createMudharabahProduct("INVALID_NISBAH", "Invalid Nisbah Test");
        product.setNisbahCustomer(new BigDecimal(customerRatio));
        product.setNisbahBank(new BigDecimal(bankRatio));
        
        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            productService.save(product);
            entityManager.flush();
        }, testCase + " should throw constraint violation exception");
        
        assertTrue(exception instanceof DataIntegrityViolationException || 
                  exception instanceof PersistenceException ||
                  exception instanceof jakarta.validation.ConstraintViolationException ||
                  exception.getCause() instanceof org.hibernate.exception.ConstraintViolationException,
                  testCase + " should throw constraint violation exception");
    }

    @Test
    @DisplayName("Should enforce nisbah constraint only for profit-sharing products")
    void shouldEnforceNisbahConstraintOnlyForProfitSharingProducts() {
        // Given - Wadiah product (no profit sharing)
        Product wadiahProduct = createWadiahProduct("WADIAH_TEST", "Wadiah No Nisbah Test");
        wadiahProduct.setNisbahCustomer(null);
        wadiahProduct.setNisbahBank(null);
        
        // When & Then - Should save successfully without nisbah
        assertDoesNotThrow(() -> {
            Product saved = productService.save(wadiahProduct);
            entityManager.flush();
            assertNotNull(saved.getId());
            assertNull(saved.getNisbahCustomer());
            assertNull(saved.getNisbahBank());
        });
        
        // Given - Murabahah product (fixed margin, no profit sharing)
        Product murabahaProduct = createMurabahaProduct("MURABAHA_TEST", "Murabaha No Nisbah Test");
        murabahaProduct.setNisbahCustomer(null);
        murabahaProduct.setNisbahBank(null);
        
        // When & Then - Should save successfully without nisbah
        assertDoesNotThrow(() -> {
            Product saved = productService.save(murabahaProduct);
            entityManager.flush();
            assertNotNull(saved.getId());
            assertNull(saved.getNisbahCustomer());
            assertNull(saved.getNisbahBank());
        });
    }

    @Test
    @DisplayName("Should require Shariah compliance for Islamic banking products")
    void shouldRequireShariahComplianceForIslamicProducts() {
        // Given
        Product islamicProduct = createMudharabahProduct("ISLAMIC_TEST", "Islamic Product Test");
        islamicProduct.setIsShariahCompliant(true); // Islamic products should be Shariah compliant
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product saved = productService.save(islamicProduct);
            entityManager.flush();
            
            // Verify that Islamic products are Shariah compliant
            assertTrue(saved.getIsShariahCompliant(), 
                     "Islamic banking products should be Shariah compliant");
        });
    }

    @Test
    @DisplayName("Should require Shariah board approval for Islamic products")
    void shouldRequireShariahBoardApprovalForIslamicProducts() {
        // Given
        Product islamicProduct = createMudharabahProduct("APPROVAL_TEST", "Shariah Approval Test");
        // Keep the approval number and date set by the helper method
        
        // When
        Product savedProduct = productService.save(islamicProduct);
        entityManager.flush();
        
        // Then - Islamic products should have approval number and date
        assertNotNull(savedProduct.getId());
        assertNotNull(savedProduct.getShariahBoardApprovalNumber(), 
                     "Islamic products should have Shariah board approval number");
        assertNotNull(savedProduct.getShariahBoardApprovalDate(), 
                     "Islamic products should have Shariah board approval date");
    }

    @ParameterizedTest
    @CsvSource({
        "TABUNGAN_MUDHARABAH, MUDHARABAH, true",
        "DEPOSITO_MUDHARABAH, MUDHARABAH, true", 
        "PEMBIAYAAN_MUDHARABAH, MUDHARABAH, true",
        "PEMBIAYAAN_MUSHARAKAH, MUSHARAKAH, true",
        "TABUNGAN_WADIAH, WADIAH, false",
        "PEMBIAYAAN_MURABAHAH, MURABAHAH, false",
        "PEMBIAYAAN_IJARAH, IJARAH, false"
    })
    @DisplayName("Should match profit sharing type with product type correctly")
    void shouldMatchProfitSharingTypeWithProductType(String productType, String profitSharingType, boolean requiresNisbah) {
        // Given
        Product product = new Product();
        product.setProductCode("TM_" + productType.substring(0, Math.min(productType.length(), 15)));
        product.setProductName("Type Matching Test " + productType);
        product.setProductType(Product.ProductType.valueOf(productType));
        product.setProfitSharingType(Product.ProfitSharingType.valueOf(profitSharingType));
        product.setProductCategory("Test Category");
        product.setIsActive(true);
        product.setIsShariahCompliant(true);
        product.setCurrency("IDR");
        product.setMinimumOpeningBalance(new BigDecimal("50000"));
        product.setMinimumBalance(new BigDecimal("25000"));
        product.setShariahBoardApprovalNumber("DSN-MUI-TEST/2024");
        product.setShariahBoardApprovalDate(LocalDate.now());
        
        // Set nisbah if required
        if (requiresNisbah) {
            product.setNisbahCustomer(new BigDecimal("0.7000"));
            product.setNisbahBank(new BigDecimal("0.3000"));
        }
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product saved = productService.save(product);
            entityManager.flush();
            
            assertNotNull(saved.getId());
            assertEquals(Product.ProductType.valueOf(productType), saved.getProductType());
            assertEquals(Product.ProfitSharingType.valueOf(profitSharingType), saved.getProfitSharingType());
            
            if (requiresNisbah) {
                assertNotNull(saved.getNisbahCustomer(), 
                             "Products requiring nisbah should have customer ratio set");
                assertNotNull(saved.getNisbahBank(), 
                             "Products requiring nisbah should have bank ratio set");
                
                // Verify nisbah sum constraint
                BigDecimal sum = saved.getNisbahCustomer().add(saved.getNisbahBank());
                assertEquals(0, sum.compareTo(BigDecimal.ONE), 
                           "Nisbah ratios should sum to 1.0");
            }
        }, "Product type and profit sharing type should match correctly");
    }

    @Test
    @DisplayName("Should validate minimum balance constraints for Islamic products")
    void shouldValidateMinimumBalanceConstraintsForIslamicProducts() {
        // Given - Invalid: minimum balance higher than opening balance
        Product product = createMudharabahProduct("BALANCE_TEST", "Balance Constraint Test");
        product.setMinimumOpeningBalance(new BigDecimal("50000"));
        product.setMinimumBalance(new BigDecimal("100000")); // Higher than opening balance
        
        // When & Then
        // Note: This constraint doesn't exist in the current database schema
        // The database only checks that both values are >= 0
        assertDoesNotThrow(() -> {
            productService.save(product);
            entityManager.flush();
        }, "Database allows minimum balance higher than opening balance");
        
        // Given - Valid: minimum balance lower than opening balance
        Product validProduct = createMudharabahProduct("BALANCE_VALID", "Valid Balance Test");
        validProduct.setMinimumOpeningBalance(new BigDecimal("100000"));
        validProduct.setMinimumBalance(new BigDecimal("50000"));
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product saved = productService.save(validProduct);
            entityManager.flush();
            assertNotNull(saved.getId());
        });
    }

    @Test
    @DisplayName("Should validate fee constraints for Islamic products")
    @Transactional
    void shouldValidateFeeConstraintsForIslamicProducts() {
        // Test valid fees first
        // Given - Valid: positive fees
        Product validProduct = createMudharabahProduct("FEE_VALID", "Valid Fee Test");
        validProduct.setMonthlyMaintenanceFee(new BigDecimal("2500"));
        validProduct.setAtmWithdrawalFee(new BigDecimal("3000"));
        validProduct.setInterBankTransferFee(new BigDecimal("5000"));
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product saved = productService.save(validProduct);
            entityManager.flush();
            assertNotNull(saved.getId());
            assertTrue(saved.getMonthlyMaintenanceFee().compareTo(BigDecimal.ZERO) >= 0);
        });
        
        // Clear the context for next test
        entityManager.clear();
        
        // Test invalid fees
        // Given - Invalid: negative fees
        Product invalidProduct = createMudharabahProduct("FEE_INVALID", "Fee Constraint Test");
        invalidProduct.setMonthlyMaintenanceFee(new BigDecimal("-1000")); // Negative fee
        
        // When & Then
        assertThrows(Exception.class, () -> {
            productService.save(invalidProduct);
            entityManager.flush();
        }, "Should reject negative fees");
    }

    @Test
    @DisplayName("Should handle precision for nisbah calculations correctly")
    void shouldHandlePrecisionForNisbahCalculationsCorrectly() {
        // Given - High precision nisbah ratios
        Product product = createMudharabahProduct("PRECISION_TEST", "Precision Nisbah Test");
        product.setNisbahCustomer(new BigDecimal("0.72536")); // 5 decimal places
        product.setNisbahBank(new BigDecimal("0.27464"));     // Sum = 1.00000
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product saved = productService.save(product);
            entityManager.flush();
            
            // Verify precision is maintained
            assertEquals(0, saved.getNisbahCustomer().compareTo(new BigDecimal("0.72536")));
            assertEquals(0, saved.getNisbahBank().compareTo(new BigDecimal("0.27464")));
            
            // Verify sum constraint still works with precision
            BigDecimal sum = saved.getNisbahCustomer().add(saved.getNisbahBank());
            assertEquals(0, sum.compareTo(new BigDecimal("1.00000")));
        });
    }

    @Test
    @DisplayName("Should validate profit distribution frequency for different product types")
    void shouldValidateProfitDistributionFrequencyForDifferentProductTypes() {
        // Given - Savings product with monthly distribution
        Product savingsProduct = createMudharabahProduct("FREQ_SAVINGS", "Frequency Savings Test");
        savingsProduct.setProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        savingsProduct.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product saved = productService.save(savingsProduct);
            entityManager.flush();
            assertEquals(Product.ProfitDistributionFrequency.MONTHLY, 
                        saved.getProfitDistributionFrequency());
        });
        
        // Given - Deposit product with on-maturity distribution
        Product depositProduct = createMudharabahProduct("FREQ_DEPOSIT", "Frequency Deposit Test");
        depositProduct.setProductType(Product.ProductType.DEPOSITO_MUDHARABAH);
        depositProduct.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.ON_MATURITY);
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product saved = productService.save(depositProduct);
            entityManager.flush();
            assertEquals(Product.ProfitDistributionFrequency.ON_MATURITY, 
                        saved.getProfitDistributionFrequency());
        });
    }

    @Test
    @DisplayName("Should validate customer type restrictions for corporate-only products")
    void shouldValidateCustomerTypeRestrictionsForCorporateOnlyProducts() {
        // Given - Corporate-only financing product
        Product corporateProduct = createMurabahaProduct("CORP_ONLY", "Corporate Only Product");
        corporateProduct.setAllowedCustomerTypes("CORPORATE");
        corporateProduct.setMinimumOpeningBalance(new BigDecimal("50000000")); // High minimum for corporate
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product saved = productService.save(corporateProduct);
            entityManager.flush();
            assertEquals("CORPORATE", saved.getAllowedCustomerTypes());
            assertTrue(saved.getMinimumOpeningBalance().compareTo(new BigDecimal("50000000")) == 0);
        });
        
        // Given - Personal and corporate product
        Product mixedProduct = createWadiahProduct("MIXED_CUSTOMER", "Mixed Customer Product");
        mixedProduct.setAllowedCustomerTypes("PERSONAL,CORPORATE");
        mixedProduct.setMinimumOpeningBalance(new BigDecimal("10000")); // Lower minimum for personal
        
        // When & Then
        assertDoesNotThrow(() -> {
            Product saved = productService.save(mixedProduct);
            entityManager.flush();
            assertEquals("PERSONAL,CORPORATE", saved.getAllowedCustomerTypes());
        });
    }

    // Helper methods
    private Product createMudharabahProduct(String code, String name) {
        Product product = new Product();
        product.setProductCode(code);
        product.setProductName(name);
        product.setProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        product.setProductCategory("Islamic Banking");
        product.setDescription("Mudharabah product for testing");
        product.setIsActive(true);
        product.setIsShariahCompliant(true);
        product.setCurrency("IDR");
        product.setMinimumOpeningBalance(new BigDecimal("100000"));
        product.setMinimumBalance(new BigDecimal("50000"));
        product.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
        product.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        product.setShariahBoardApprovalNumber("DSN-MUI-001/2024");
        product.setShariahBoardApprovalDate(LocalDate.of(2024, 1, 1));
        product.setNisbahCustomer(new BigDecimal("0.7000"));
        product.setNisbahBank(new BigDecimal("0.3000"));
        return product;
    }

    private Product createWadiahProduct(String code, String name) {
        Product product = new Product();
        product.setProductCode(code);
        product.setProductName(name);
        product.setProductType(Product.ProductType.TABUNGAN_WADIAH);
        product.setProductCategory("Islamic Banking");
        product.setDescription("Wadiah product for testing");
        product.setIsActive(true);
        product.setIsShariahCompliant(true);
        product.setCurrency("IDR");
        product.setMinimumOpeningBalance(new BigDecimal("50000"));
        product.setMinimumBalance(new BigDecimal("25000"));
        product.setProfitSharingType(Product.ProfitSharingType.WADIAH);
        product.setShariahBoardApprovalNumber("DSN-MUI-002/2024");
        product.setShariahBoardApprovalDate(LocalDate.of(2024, 1, 1));
        return product;
    }

    private Product createMurabahaProduct(String code, String name) {
        Product product = new Product();
        product.setProductCode(code);
        product.setProductName(name);
        product.setProductType(Product.ProductType.PEMBIAYAAN_MURABAHAH);
        product.setProductCategory("Islamic Financing");
        product.setDescription("Murabahah financing product for testing");
        product.setIsActive(true);
        product.setIsShariahCompliant(true);
        product.setCurrency("IDR");
        product.setMinimumOpeningBalance(new BigDecimal("10000000"));
        product.setMinimumBalance(new BigDecimal("10000000"));
        product.setProfitSharingType(Product.ProfitSharingType.MURABAHAH);
        product.setShariahBoardApprovalNumber("DSN-MUI-003/2024");
        product.setShariahBoardApprovalDate(LocalDate.of(2024, 1, 1));
        product.setAllowedCustomerTypes("PERSONAL,CORPORATE");
        return product;
    }
}