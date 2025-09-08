package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.config.BaseIntegrationTest;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DisplayName("ProductRepository Integration Tests")
class ProductRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;
    
    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save and retrieve basic Islamic banking product")
    void shouldSaveAndRetrieveBasicProduct() {
        // Given
        Product product = createTabunganWadiahProduct("TWB001", "Tabungan Wadiah Basic");
        
        // When
        Product savedProduct = productRepository.save(product);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        assertNotNull(savedProduct.getId());
        Optional<Product> retrieved = productRepository.findById(savedProduct.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("TWB001", retrieved.get().getProductCode());
        assertEquals("Tabungan Wadiah Basic", retrieved.get().getProductName());
        assertEquals(Product.ProductType.TABUNGAN_WADIAH, retrieved.get().getProductType());
        assertTrue(retrieved.get().getIsShariahCompliant());
    }

    @Test
    @DisplayName("Should enforce unique product code constraint")
    void shouldEnforceUniqueProductCodeConstraint() {
        // Given
        Product product1 = createTabunganWadiahProduct("DUPLICATE", "First Product");
        Product product2 = createTabunganWadiahProduct("DUPLICATE", "Second Product");
        
        // When & Then
        productRepository.save(product1);
        entityManager.flush();
        
        assertThrows(Exception.class, () -> {
            productRepository.save(product2);
            entityManager.flush();
        }, "Should throw constraint violation exception for duplicate product code");
    }

    @Test
    @DisplayName("Should enforce nisbah sum constraint for Mudharabah products")
    void shouldEnforceNisbahSumConstraint() {
        // Given - Invalid nisbah sum (0.8 + 0.3 = 1.1, should be 1.0)
        Product product = createTabunganMudharabahProduct("TMD001", "Invalid Nisbah Product");
        product.setNisbahCustomer(new BigDecimal("0.8000"));
        product.setNisbahBank(new BigDecimal("0.3000"));
        
        // When & Then
        assertThrows(Exception.class, () -> {
            productRepository.save(product);
            entityManager.flush();
        }, "Should throw constraint violation exception for invalid nisbah sum");
    }

    @Test
    @DisplayName("Should save valid Mudharabah product with correct nisbah ratios")
    void shouldSaveValidMudharabahProduct() {
        // Given - Valid nisbah sum (0.7 + 0.3 = 1.0)
        Product product = createTabunganMudharabahProduct("TMD002", "Valid Mudharabah Product");
        product.setNisbahCustomer(new BigDecimal("0.7000"));
        product.setNisbahBank(new BigDecimal("0.3000"));
        
        // When
        Product savedProduct = productRepository.save(product);
        entityManager.flush();
        
        // Then
        assertNotNull(savedProduct.getId());
        assertEquals(Product.ProfitSharingType.MUDHARABAH, savedProduct.getProfitSharingType());
        assertEquals(0, savedProduct.getNisbahCustomer().compareTo(new BigDecimal("0.7000")));
        assertEquals(0, savedProduct.getNisbahBank().compareTo(new BigDecimal("0.3000")));
    }

    @Test
    @DisplayName("Should find products by product type")
    void shouldFindProductsByProductType() {
        // Given
        Product wadiah1 = createTabunganWadiahProduct("TWB001", "Wadiah Basic");
        Product wadiah2 = createTabunganWadiahProduct("TWB002", "Wadiah Premium");
        Product mudharabah = createTabunganMudharabahProduct("TMD001", "Mudharabah Basic");
        
        productRepository.save(wadiah1);
        productRepository.save(wadiah2);
        productRepository.save(mudharabah);
        entityManager.flush();
        
        // When
        List<Product> wadiahProducts = productRepository.findByProductType(Product.ProductType.TABUNGAN_WADIAH);
        List<Product> mudharabahProducts = productRepository.findByProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        
        // Then
        assertEquals(2, wadiahProducts.size());
        assertEquals(1, mudharabahProducts.size());
        assertTrue(wadiahProducts.stream().allMatch(p -> p.getProductType() == Product.ProductType.TABUNGAN_WADIAH));
        assertTrue(mudharabahProducts.stream().allMatch(p -> p.getProductType() == Product.ProductType.TABUNGAN_MUDHARABAH));
    }

    @Test
    @DisplayName("Should find active products only")
    void shouldFindActiveProductsOnly() {
        // Given
        Product activeProduct = createTabunganWadiahProduct("ACTIVE001", "Active Product");
        Product inactiveProduct = createTabunganWadiahProduct("INACTIVE001", "Inactive Product");
        inactiveProduct.setIsActive(false);
        
        productRepository.save(activeProduct);
        productRepository.save(inactiveProduct);
        entityManager.flush();
        
        // When
        List<Product> activeProducts = productRepository.findByIsActiveTrue();
        
        // Then
        assertEquals(1, activeProducts.size());
        assertEquals("ACTIVE001", activeProducts.get(0).getProductCode());
        assertTrue(activeProducts.get(0).getIsActive());
    }

    @Test
    @DisplayName("Should find products with filters using custom query")
    void shouldFindProductsWithFilters() {
        // Given
        Product wadiah = createTabunganWadiahProduct("TWB001", "Tabungan Wadiah Basic");
        wadiah.setProductCategory("Tabungan Syariah");
        
        Product mudharabah = createTabunganMudharabahProduct("TMD001", "Tabungan Mudharabah Premium");
        mudharabah.setProductCategory("Tabungan Syariah");
        
        Product deposito = createDepositoMudharabahProduct("DMD001", "Deposito Mudharabah 6M");
        deposito.setProductCategory("Deposito Syariah");
        
        productRepository.save(wadiah);
        productRepository.save(mudharabah);
        productRepository.save(deposito);
        entityManager.flush();
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When - Filter by product type
        Page<Product> wadiahResults = productRepository.findActiveProductsWithFilters(
            Product.ProductType.TABUNGAN_WADIAH, null, null, pageable);
        
        // When - Filter by category
        Page<Product> tabunganResults = productRepository.findActiveProductsWithFilters(
            null, "Tabungan Syariah", null, pageable);
        
        // When - Filter by search term
        Page<Product> searchResults = productRepository.findActiveProductsWithFilters(
            null, null, "Premium", pageable);
        
        // Then
        assertEquals(1, wadiahResults.getTotalElements());
        assertEquals("TWB001", wadiahResults.getContent().get(0).getProductCode());
        
        assertEquals(2, tabunganResults.getTotalElements());
        assertTrue(tabunganResults.getContent().stream()
            .allMatch(p -> p.getProductCategory().equals("Tabungan Syariah")));
        
        assertEquals(1, searchResults.getTotalElements());
        assertEquals("TMD001", searchResults.getContent().get(0).getProductCode());
    }

    @Test
    @DisplayName("Should find distinct active categories")
    void shouldFindDistinctActiveCategories() {
        // Given
        Product product1 = createTabunganWadiahProduct("TWB001", "Product 1");
        product1.setProductCategory("Tabungan Syariah");
        
        Product product2 = createTabunganMudharabahProduct("TMD001", "Product 2");
        product2.setProductCategory("Tabungan Syariah");
        
        Product product3 = createDepositoMudharabahProduct("DMD001", "Product 3");
        product3.setProductCategory("Deposito Syariah");
        
        Product inactiveProduct = createTabunganWadiahProduct("INACTIVE", "Inactive Product");
        inactiveProduct.setProductCategory("Inactive Category");
        inactiveProduct.setIsActive(false);
        
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        productRepository.save(inactiveProduct);
        entityManager.flush();
        
        // When
        List<String> categories = productRepository.findDistinctActiveCategories();
        
        // Then
        assertEquals(2, categories.size());
        assertTrue(categories.contains("Tabungan Syariah"));
        assertTrue(categories.contains("Deposito Syariah"));
        assertFalse(categories.contains("Inactive Category"));
    }

    @Test
    @DisplayName("Should validate negative fee constraints")
    void shouldValidateNegativeFeeConstraints() {
        // Given
        Product product = createTabunganWadiahProduct("TWB001", "Invalid Fee Product");
        product.setMonthlyMaintenanceFee(new BigDecimal("-100")); // Negative fee - should fail
        
        // When & Then
        assertThrows(Exception.class, () -> {
            productRepository.save(product);
            entityManager.flush();
        }, "Should throw constraint violation for negative fees");
    }

    @Test
    @DisplayName("Should handle decimal precision for financial amounts")
    void shouldHandleDecimalPrecisionForFinancialAmounts() {
        // Given
        Product product = createTabunganWadiahProduct("TWB001", "Precision Test Product");
        product.setMinimumOpeningBalance(new BigDecimal("123456789012345678.99"));
        product.setMonthlyMaintenanceFee(new BigDecimal("2500.50"));
        product.setAtmWithdrawalFee(new BigDecimal("3000.25"));
        
        // When
        Product savedProduct = productRepository.save(product);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        Optional<Product> retrieved = productRepository.findById(savedProduct.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(0, retrieved.get().getMinimumOpeningBalance()
            .compareTo(new BigDecimal("123456789012345678.99")));
        assertEquals(0, retrieved.get().getMonthlyMaintenanceFee()
            .compareTo(new BigDecimal("2500.50")));
        assertEquals(0, retrieved.get().getAtmWithdrawalFee()
            .compareTo(new BigDecimal("3000.25")));
    }

    @Test
    @DisplayName("Should handle special Islamic banking product types")
    void shouldHandleIslamicBankingProductTypes() {
        // Given
        Product murabahah = createPembiayaanMurabahaProduct("PMR001", "Pembiayaan Murabahah KPR");
        Product ijarah = createPembiayaanIjarahProduct("PIJ001", "Pembiayaan Ijarah Kendaraan");
        Product salam = createPembiayaanSalamProduct("PSL001", "Pembiayaan Salam Pertanian");
        Product istisna = createPembiayaanIstisnaProduct("PIS001", "Pembiayaan Istisna Konstruksi");
        
        // When
        productRepository.save(murabahah);
        productRepository.save(ijarah);
        productRepository.save(salam);
        productRepository.save(istisna);
        entityManager.flush();
        
        // Then
        List<Product> pembiayaanProducts = productRepository.findAll().stream()
            .filter(p -> p.getProductType().name().startsWith("PEMBIAYAAN_"))
            .toList();
        
        assertEquals(4, pembiayaanProducts.size());
        assertTrue(pembiayaanProducts.stream().allMatch(p -> p.getIsShariahCompliant()));
        assertTrue(pembiayaanProducts.stream().allMatch(p -> p.getShariahBoardApprovalNumber() != null));
    }

    @Test
    @DisplayName("Should check product code existence")
    void shouldCheckProductCodeExistence() {
        // Given
        Product product = createTabunganWadiahProduct("EXISTING", "Existing Product");
        productRepository.save(product);
        entityManager.flush();
        
        // When & Then
        assertTrue(productRepository.existsByProductCode("EXISTING"));
        assertFalse(productRepository.existsByProductCode("NON_EXISTING"));
    }

    @Test
    @DisplayName("Should find product by product code")
    void shouldFindProductByProductCode() {
        // Given
        Product product = createTabunganWadiahProduct("FINDME", "Find Me Product");
        productRepository.save(product);
        entityManager.flush();
        
        // When
        Optional<Product> found = productRepository.findByProductCode("FINDME");
        Optional<Product> notFound = productRepository.findByProductCode("NOTFOUND");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("Find Me Product", found.get().getProductName());
        assertFalse(notFound.isPresent());
    }

    // Helper methods to create test products
    private Product createTabunganWadiahProduct(String code, String name) {
        Product product = new Product();
        product.setProductCode(code);
        product.setProductName(name);
        product.setProductType(Product.ProductType.TABUNGAN_WADIAH);
        product.setProductCategory("Tabungan Syariah");
        product.setDescription("Basic Wadiah savings account");
        product.setIsActive(true);
        product.setIsDefault(false);
        product.setCurrency("IDR");
        product.setMinimumOpeningBalance(new BigDecimal("50000"));
        product.setMinimumBalance(new BigDecimal("25000"));
        product.setMaximumBalance(new BigDecimal("1000000000"));
        product.setProfitSharingType(Product.ProfitSharingType.WADIAH);
        product.setIsShariahCompliant(true);
        product.setShariahBoardApprovalNumber("DSN-MUI-001/2024");
        product.setShariahBoardApprovalDate(LocalDate.of(2024, 1, 1));
        product.setMonthlyMaintenanceFee(new BigDecimal("2500"));
        product.setAtmWithdrawalFee(new BigDecimal("2500"));
        return product;
    }

    private Product createTabunganMudharabahProduct(String code, String name) {
        Product product = createTabunganWadiahProduct(code, name);
        product.setProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        product.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
        product.setNisbahCustomer(new BigDecimal("0.7000"));
        product.setNisbahBank(new BigDecimal("0.3000"));
        product.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        return product;
    }

    private Product createDepositoMudharabahProduct(String code, String name) {
        Product product = createTabunganMudharabahProduct(code, name);
        product.setProductType(Product.ProductType.DEPOSITO_MUDHARABAH);
        product.setProductCategory("Deposito Syariah");
        product.setMinimumOpeningBalance(new BigDecimal("10000000"));
        product.setMinimumBalance(new BigDecimal("10000000"));
        product.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.ON_MATURITY);
        return product;
    }

    private Product createPembiayaanMurabahaProduct(String code, String name) {
        Product product = createTabunganWadiahProduct(code, name);
        product.setProductType(Product.ProductType.PEMBIAYAAN_MURABAHAH);
        product.setProductCategory("Pembiayaan Syariah");
        product.setProfitSharingType(Product.ProfitSharingType.MURABAHAH);
        product.setMinimumOpeningBalance(new BigDecimal("50000000"));
        product.setAllowedCustomerTypes("PERSONAL,CORPORATE");
        product.setNisbahCustomer(null); // Murabahah doesn't use nisbah
        product.setNisbahBank(null);
        return product;
    }

    private Product createPembiayaanIjarahProduct(String code, String name) {
        Product product = createPembiayaanMurabahaProduct(code, name);
        product.setProductType(Product.ProductType.PEMBIAYAAN_IJARAH);
        product.setProfitSharingType(Product.ProfitSharingType.IJARAH);
        return product;
    }

    private Product createPembiayaanSalamProduct(String code, String name) {
        Product product = createPembiayaanMurabahaProduct(code, name);
        product.setProductType(Product.ProductType.PEMBIAYAAN_SALAM);
        product.setProfitSharingType(Product.ProfitSharingType.SALAM);
        return product;
    }

    private Product createPembiayaanIstisnaProduct(String code, String name) {
        Product product = createPembiayaanMurabahaProduct(code, name);
        product.setProductType(Product.ProductType.PEMBIAYAAN_ISTISNA);
        product.setProfitSharingType(Product.ProfitSharingType.ISTISNA);
        return product;
    }
}