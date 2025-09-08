package id.ac.tazkia.minibank.unit.service;

import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should find all products with pagination")
    void shouldFindAllProductsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(
            createMockProduct("TWB001", "Tabungan Wadiah Basic"),
            createMockProduct("TMD001", "Tabungan Mudharabah Premium")
        );
        Page<Product> productPage = new PageImpl<>(products, pageable, 2);
        
        when(productRepository.findAll(pageable)).thenReturn(productPage);
        
        // When
        Page<Product> result = productService.findAll(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(productRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should find active products only")
    void shouldFindActiveProductsOnly() {
        // Given
        List<Product> activeProducts = Arrays.asList(
            createMockProduct("TWB001", "Active Product 1"),
            createMockProduct("TMD001", "Active Product 2")
        );
        
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);
        
        // When
        List<Product> result = productService.findActiveProducts();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should find product by ID")
    void shouldFindProductById() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = createMockProduct("TWB001", "Test Product");
        product.setId(productId);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // When
        Optional<Product> result = productService.findById(productId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("TWB001", result.get().getProductCode());
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should find product by product code")
    void shouldFindProductByProductCode() {
        // Given
        Product product = createMockProduct("TWB001", "Test Product");
        
        when(productRepository.findByProductCode("TWB001")).thenReturn(Optional.of(product));
        
        // When
        Optional<Product> result = productService.findByProductCode("TWB001");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getProductName());
        verify(productRepository).findByProductCode("TWB001");
    }

    @Test
    @DisplayName("Should find products by product type")
    void shouldFindProductsByProductType() {
        // Given
        List<Product> wadiahProducts = Arrays.asList(
            createMockProduct("TWB001", "Wadiah Basic"),
            createMockProduct("TWB002", "Wadiah Premium")
        );
        
        when(productRepository.findByProductType(Product.ProductType.TABUNGAN_WADIAH))
            .thenReturn(wadiahProducts);
        
        // When
        List<Product> result = productService.findByProductType(Product.ProductType.TABUNGAN_WADIAH);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findByProductType(Product.ProductType.TABUNGAN_WADIAH);
    }

    @Test
    @DisplayName("Should find products with filters")
    void shouldFindProductsWithFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> filteredProducts = Arrays.asList(createMockProduct("TMD001", "Mudharabah Product"));
        Page<Product> productPage = new PageImpl<>(filteredProducts, pageable, 1);
        
        when(productRepository.findActiveProductsWithFilters(
            eq(Product.ProductType.TABUNGAN_MUDHARABAH), 
            eq("Tabungan Syariah"), 
            eq("Premium"), 
            eq(pageable)))
            .thenReturn(productPage);
        
        // When
        Page<Product> result = productService.findWithFilters(
            Product.ProductType.TABUNGAN_MUDHARABAH, 
            "Tabungan Syariah", 
            "Premium", 
            pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository).findActiveProductsWithFilters(
            Product.ProductType.TABUNGAN_MUDHARABAH, "Tabungan Syariah", "Premium", pageable);
    }

    @Test
    @DisplayName("Should find distinct categories")
    void shouldFindDistinctCategories() {
        // Given
        List<String> categories = Arrays.asList("Tabungan Syariah", "Deposito Syariah", "Pembiayaan Syariah");
        
        when(productRepository.findDistinctActiveCategories()).thenReturn(categories);
        
        // When
        List<String> result = productService.findDistinctCategories();
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Tabungan Syariah"));
        verify(productRepository).findDistinctActiveCategories();
    }

    @Test
    @DisplayName("Should save valid product")
    void shouldSaveValidProduct() {
        // Given
        Product product = createValidProduct("TWB001", "Valid Product");
        Product savedProduct = createValidProduct("TWB001", "Valid Product");
        savedProduct.setId(UUID.randomUUID());
        
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        
        // When
        Product result = productService.save(product);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("TWB001", result.getProductCode());
        verify(productRepository).save(product);
    }

    @ParameterizedTest
    @CsvSource({
        "'', 'Valid Name', 'Product code is required'",
        "'   ', 'Valid Name', 'Product code is required'",
        "'VALID001', '', 'Product name is required'",
        "'VALID001', '   ', 'Product name is required'"
    })
    @DisplayName("Should validate required fields when saving")
    void shouldValidateRequiredFieldsWhenSaving(String productCode, String productName, String expectedMessage) {
        // Given
        Product product = createValidProduct(productCode, productName);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.save(product);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should validate product type is required")
    void shouldValidateProductTypeIsRequired() {
        // Given
        Product product = createValidProduct("TWB001", "Valid Product");
        product.setProductType(null);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.save(product);
        });
        
        assertEquals("Product type is required", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.1", "1.1", "-1.0", "2.0"})
    @DisplayName("Should validate profit sharing ratio range")
    void shouldValidateProfitSharingRatioRange(String ratioValue) {
        // Given
        Product product = createValidProduct("TMD001", "Mudharabah Product");
        product.setProfitSharingRatio(new BigDecimal(ratioValue));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.save(product);
        });
        
        assertEquals("Profit sharing ratio must be between 0 and 1", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should accept valid profit sharing ratios")
    void shouldAcceptValidProfitSharingRatios() {
        // Given
        Product product = createValidProduct("TMD001", "Mudharabah Product");
        product.setProfitSharingRatio(new BigDecimal("0.7500"));
        Product savedProduct = createValidProduct("TMD001", "Mudharabah Product");
        savedProduct.setId(UUID.randomUUID());
        
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        
        // When
        Product result = productService.save(product);
        
        // Then
        assertNotNull(result);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should update existing product")
    void shouldUpdateExistingProduct() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = createValidProduct("TWB001", "Updated Product");
        product.setId(productId);
        Product updatedProduct = createValidProduct("TWB001", "Updated Product");
        updatedProduct.setId(productId);
        
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        
        // When
        Product result = productService.update(product);
        
        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        verify(productRepository).existsById(productId);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should throw exception when updating product without ID")
    void shouldThrowExceptionWhenUpdatingProductWithoutId() {
        // Given
        Product product = createValidProduct("TWB001", "Product Without ID");
        product.setId(null);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.update(product);
        });
        
        assertEquals("Product ID is required for update operation", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing product")
    void shouldThrowExceptionWhenUpdatingNonExistingProduct() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = createValidProduct("TWB001", "Non-existing Product");
        product.setId(productId);
        
        when(productRepository.existsById(productId)).thenReturn(false);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.update(product);
        });
        
        assertEquals("Product with ID " + productId + " not found for update", exception.getMessage());
        verify(productRepository).existsById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete product by setting active to false")
    void shouldSoftDeleteProduct() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = createValidProduct("TWB001", "Product to Delete");
        product.setId(productId);
        product.setIsActive(true);
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // When
        productService.softDelete(productId);
        
        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).save(argThat(p -> !p.getIsActive()));
    }

    @Test
    @DisplayName("Should handle soft delete of non-existing product gracefully")
    void shouldHandleSoftDeleteOfNonExistingProduct() {
        // Given
        UUID productId = UUID.randomUUID();
        
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        
        // When
        assertDoesNotThrow(() -> productService.softDelete(productId));
        
        // Then
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should check if product code exists")
    void shouldCheckIfProductCodeExists() {
        // Given
        String productCode = "EXISTING001";
        
        when(productRepository.existsByProductCode(productCode)).thenReturn(true);
        
        // When
        boolean result = productService.existsByProductCode(productCode);
        
        // Then
        assertTrue(result);
        verify(productRepository).existsByProductCode(productCode);
    }

    @Test
    @DisplayName("Should check if product code exists excluding specific ID")
    void shouldCheckIfProductCodeExistsExcludingSpecificId() {
        // Given
        String productCode = "TWB001";
        UUID excludeId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        
        Product existingProduct = createValidProduct(productCode, "Existing Product");
        existingProduct.setId(existingId);
        
        when(productRepository.findByProductCode(productCode)).thenReturn(Optional.of(existingProduct));
        
        // When
        boolean result = productService.existsByProductCodeAndNotId(productCode, excludeId);
        
        // Then
        assertTrue(result); // Should return true because existing product has different ID
        verify(productRepository).findByProductCode(productCode);
    }

    @Test
    @DisplayName("Should return false when checking code exists excluding same ID")
    void shouldReturnFalseWhenCheckingCodeExistsExcludingSameId() {
        // Given
        String productCode = "TWB001";
        UUID productId = UUID.randomUUID();
        
        Product existingProduct = createValidProduct(productCode, "Existing Product");
        existingProduct.setId(productId);
        
        when(productRepository.findByProductCode(productCode)).thenReturn(Optional.of(existingProduct));
        
        // When
        boolean result = productService.existsByProductCodeAndNotId(productCode, productId);
        
        // Then
        assertFalse(result); // Should return false because it's the same product
        verify(productRepository).findByProductCode(productCode);
    }

    @Test
    @DisplayName("Should return false when product code does not exist")
    void shouldReturnFalseWhenProductCodeDoesNotExist() {
        // Given
        String productCode = "NON_EXISTING";
        UUID excludeId = UUID.randomUUID();
        
        when(productRepository.findByProductCode(productCode)).thenReturn(Optional.empty());
        
        // When
        boolean result = productService.existsByProductCodeAndNotId(productCode, excludeId);
        
        // Then
        assertFalse(result);
        verify(productRepository).findByProductCode(productCode);
    }

    @Test
    @DisplayName("Should hard delete product by ID")
    void shouldHardDeleteProductById() {
        // Given
        UUID productId = UUID.randomUUID();
        
        // When
        productService.deleteById(productId);
        
        // Then
        verify(productRepository).deleteById(productId);
    }

    // Helper methods
    private Product createMockProduct(String code, String name) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setProductCode(code);
        product.setProductName(name);
        product.setProductType(Product.ProductType.TABUNGAN_WADIAH);
        product.setProductCategory("Tabungan Syariah");
        product.setIsActive(true);
        return product;
    }

    private Product createValidProduct(String code, String name) {
        Product product = new Product();
        product.setProductCode(code);
        product.setProductName(name);
        product.setProductType(Product.ProductType.TABUNGAN_WADIAH);
        product.setProductCategory("Tabungan Syariah");
        product.setDescription("Test product description");
        product.setIsActive(true);
        product.setIsDefault(false);
        product.setCurrency("IDR");
        product.setMinimumOpeningBalance(new BigDecimal("50000"));
        product.setMinimumBalance(new BigDecimal("25000"));
        product.setProfitSharingType(Product.ProfitSharingType.WADIAH);
        product.setIsShariahCompliant(true);
        product.setShariahBoardApprovalNumber("DSN-MUI-001/2024");
        product.setShariahBoardApprovalDate(LocalDate.of(2024, 1, 1));
        product.setMonthlyMaintenanceFee(new BigDecimal("2500"));
        product.setAtmWithdrawalFee(new BigDecimal("2500"));
        return product;
    }
}