package id.ac.tazkia.minibank.unit.service;

import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setProductCode("SAV001");
        product.setProductName("Basic Savings");
        product.setProductType(Product.ProductType.SAVINGS);
        product.setProductCategory("Personal");
        product.setIsActive(true);
        product.setProfitSharingRatio(new BigDecimal("0.05"));
    }

    @Test
    @DisplayName("Should return paginated products when findAll with pageable")
    void shouldReturnPaginatedProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> expectedPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(expectedPage);

        // When
        Page<Product> result = productService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(product);
        verify(productRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should return all products when findAll without pageable")
    void shouldReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should return active products only")
    void shouldReturnActiveProducts() {
        // Given
        List<Product> activeProducts = Arrays.asList(product);
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);

        // When
        List<Product> result = productService.findActiveProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(productRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("Should return product when found by id")
    void shouldReturnProductWhenFoundById() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        Optional<Product> result = productService.findById(productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(product);
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should return empty when product not found by id")
    void shouldReturnEmptyWhenProductNotFoundById() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.findById(productId);

        // Then
        assertThat(result).isEmpty();
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should return product when found by product code")
    void shouldReturnProductWhenFoundByProductCode() {
        // Given
        when(productRepository.findByProductCode("SAV001")).thenReturn(Optional.of(product));

        // When
        Optional<Product> result = productService.findByProductCode("SAV001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(product);
        verify(productRepository).findByProductCode("SAV001");
    }

    @Test
    @DisplayName("Should return products by product type")
    void shouldReturnProductsByProductType() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByProductType(Product.ProductType.SAVINGS)).thenReturn(products);

        // When
        List<Product> result = productService.findByProductType(Product.ProductType.SAVINGS);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductType()).isEqualTo(Product.ProductType.SAVINGS);
        verify(productRepository).findByProductType(Product.ProductType.SAVINGS);
    }

    @Test
    @DisplayName("Should return filtered products as list")
    void shouldReturnFilteredProductsList() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findActiveProductsWithFilters(
                Product.ProductType.SAVINGS, "Personal", "Basic")).thenReturn(products);

        // When
        List<Product> result = productService.findWithFilters(
                Product.ProductType.SAVINGS, "Personal", "Basic");

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findActiveProductsWithFilters(
                Product.ProductType.SAVINGS, "Personal", "Basic");
    }

    @Test
    @DisplayName("Should return filtered products as page")
    void shouldReturnFilteredProductsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> expectedPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findActiveProductsWithFilters(
                Product.ProductType.SAVINGS, "Personal", "Basic", pageable)).thenReturn(expectedPage);

        // When
        Page<Product> result = productService.findWithFilters(
                Product.ProductType.SAVINGS, "Personal", "Basic", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findActiveProductsWithFilters(
                Product.ProductType.SAVINGS, "Personal", "Basic", pageable);
    }

    @Test
    @DisplayName("Should return distinct categories")
    void shouldReturnDistinctCategories() {
        // Given
        List<String> categories = Arrays.asList("Personal", "Business");
        when(productRepository.findDistinctActiveCategories()).thenReturn(categories);

        // When
        List<String> result = productService.findDistinctCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("Personal", "Business");
        verify(productRepository).findDistinctActiveCategories();
    }

    @Test
    @DisplayName("Should save valid product")
    void shouldSaveValidProduct() {
        // Given
        when(productRepository.save(product)).thenReturn(product);

        // When
        Product result = productService.save(product);

        // Then
        assertThat(result).isEqualTo(product);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should update valid product")
    void shouldUpdateValidProduct() {
        // Given
        when(productRepository.save(product)).thenReturn(product);

        // When
        Product result = productService.update(product);

        // Then
        assertThat(result).isEqualTo(product);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should delete product by id")
    void shouldDeleteProductById() {
        // When
        productService.deleteById(productId);

        // Then
        verify(productRepository).deleteById(productId);
    }

    @Test
    @DisplayName("Should soft delete existing product")
    void shouldSoftDeleteExistingProduct() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.softDelete(productId);

        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).save(argThat(p -> !p.getIsActive()));
    }

    @Test
    @DisplayName("Should not fail when soft deleting non-existing product")
    void shouldNotFailWhenSoftDeletingNonExistingProduct() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        productService.softDelete(productId);

        // Then
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return true when product exists by code")
    void shouldReturnTrueWhenProductExistsByCode() {
        // Given
        when(productRepository.existsByProductCode("SAV001")).thenReturn(true);

        // When
        boolean result = productService.existsByProductCode("SAV001");

        // Then
        assertThat(result).isTrue();
        verify(productRepository).existsByProductCode("SAV001");
    }

    @Test
    @DisplayName("Should return false when product does not exist by code")
    void shouldReturnFalseWhenProductDoesNotExistByCode() {
        // Given
        when(productRepository.existsByProductCode("SAV001")).thenReturn(false);

        // When
        boolean result = productService.existsByProductCode("SAV001");

        // Then
        assertThat(result).isFalse();
        verify(productRepository).existsByProductCode("SAV001");
    }

    @Test
    @DisplayName("Should return true when product exists by code and different id")
    void shouldReturnTrueWhenProductExistsByCodeAndDifferentId() {
        // Given
        UUID differentId = UUID.randomUUID();
        Product existingProduct = new Product();
        existingProduct.setId(differentId);
        existingProduct.setProductCode("SAV001");
        when(productRepository.findByProductCode("SAV001")).thenReturn(Optional.of(existingProduct));

        // When
        boolean result = productService.existsByProductCodeAndNotId("SAV001", productId);

        // Then
        assertThat(result).isTrue();
        verify(productRepository).findByProductCode("SAV001");
    }

    @Test
    @DisplayName("Should return false when product exists by code but same id")
    void shouldReturnFalseWhenProductExistsByCodeButSameId() {
        // Given
        when(productRepository.findByProductCode("SAV001")).thenReturn(Optional.of(product));

        // When
        boolean result = productService.existsByProductCodeAndNotId("SAV001", productId);

        // Then
        assertThat(result).isFalse();
        verify(productRepository).findByProductCode("SAV001");
    }

    @Test
    @DisplayName("Should return false when product does not exist by code")
    void shouldReturnFalseWhenProductDoesNotExistByCodeForIdCheck() {
        // Given
        when(productRepository.findByProductCode("SAV001")).thenReturn(Optional.empty());

        // When
        boolean result = productService.existsByProductCodeAndNotId("SAV001", productId);

        // Then
        assertThat(result).isFalse();
        verify(productRepository).findByProductCode("SAV001");
    }

    @Test
    @DisplayName("Should throw exception when product code is null")
    void shouldThrowExceptionWhenProductCodeIsNull() {
        // Given
        product.setProductCode(null);

        // When & Then
        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product code is required");
        
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when product code is empty")
    void shouldThrowExceptionWhenProductCodeIsEmpty() {
        // Given
        product.setProductCode("  ");

        // When & Then
        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product code is required");
        
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when product name is null")
    void shouldThrowExceptionWhenProductNameIsNull() {
        // Given
        product.setProductName(null);

        // When & Then
        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name is required");
        
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when product name is empty")
    void shouldThrowExceptionWhenProductNameIsEmpty() {
        // Given
        product.setProductName("  ");

        // When & Then
        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name is required");
        
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when product type is null")
    void shouldThrowExceptionWhenProductTypeIsNull() {
        // Given
        product.setProductType(null);

        // When & Then
        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product type is required");
        
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when profit sharing ratio is negative")
    void shouldThrowExceptionWhenProfitSharingRatioIsNegative() {
        // Given
        product.setProfitSharingRatio(new BigDecimal("-0.1"));

        // When & Then
        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profit sharing ratio must be between 0 and 1");
        
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when profit sharing ratio is greater than 1")
    void shouldThrowExceptionWhenProfitSharingRatioIsGreaterThanOne() {
        // Given
        product.setProfitSharingRatio(new BigDecimal("1.1"));

        // When & Then
        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profit sharing ratio must be between 0 and 1");
        
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save product with valid profit sharing ratio of 0")
    void shouldSaveProductWithValidProfitSharingRatioOfZero() {
        // Given
        product.setProfitSharingRatio(BigDecimal.ZERO);
        when(productRepository.save(product)).thenReturn(product);

        // When
        Product result = productService.save(product);

        // Then
        assertThat(result).isEqualTo(product);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should save product with valid profit sharing ratio of 1")
    void shouldSaveProductWithValidProfitSharingRatioOfOne() {
        // Given
        product.setProfitSharingRatio(BigDecimal.ONE);
        when(productRepository.save(product)).thenReturn(product);

        // When
        Product result = productService.save(product);

        // Then
        assertThat(result).isEqualTo(product);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should save product with null profit sharing ratio")
    void shouldSaveProductWithNullProfitSharingRatio() {
        // Given
        product.setProfitSharingRatio(null);
        when(productRepository.save(product)).thenReturn(product);

        // When
        Product result = productService.save(product);

        // Then
        assertThat(result).isEqualTo(product);
        verify(productRepository).save(product);
    }
}