package id.ac.tazkia.minibank.integration.controller;

import id.ac.tazkia.minibank.controller.rest.ProductRestController;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductRestController.class)
@DisplayName("ProductRestController Simple Tests")
class ProductRestControllerSimpleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(authorities = {"PRODUCT_VIEW"})
    @DisplayName("Should get all products successfully")
    void shouldGetAllProductsSuccessfully() throws Exception {
        // Given
        Product wadiah = createTabunganWadiahProduct("TWB001", "Tabungan Wadiah Basic");
        Product mudharabah = createTabunganMudharabahProduct("TMD001", "Tabungan Mudharabah Premium");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(wadiah, mudharabah));

        // When & Then
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productCode", is("TWB001")))
                .andExpect(jsonPath("$[0].productName", is("Tabungan Wadiah Basic")))
                .andExpect(jsonPath("$[0].productType", is("TABUNGAN_WADIAH")))
                .andExpect(jsonPath("$[0].isActive", is(true)))
                .andExpect(jsonPath("$[1].productCode", is("TMD001")))
                .andExpect(jsonPath("$[1].productName", is("Tabungan Mudharabah Premium")))
                .andExpect(jsonPath("$[1].productType", is("TABUNGAN_MUDHARABAH")));
        
        verify(productRepository).findAll();
    }

    @Test
    @WithMockUser(authorities = {"PRODUCT_VIEW"})
    @DisplayName("Should return empty array when no products exist")
    void shouldReturnEmptyArrayWhenNoProductsExist() throws Exception {
        // Given
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(productRepository).findAll();
    }

    @Test
    @WithMockUser(authorities = {"PRODUCT_VIEW"})
    @DisplayName("Should handle Islamic banking product types correctly")
    void shouldHandleIslamicBankingProductTypesCorrectly() throws Exception {
        // Given
        Product mudharabah = createTabunganMudharabahProduct("TMD001", "Mudharabah Product");
        when(productRepository.findAll()).thenReturn(Arrays.asList(mudharabah));

        // When & Then
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productType", is("TABUNGAN_MUDHARABAH")))
                .andExpect(jsonPath("$[0].profitSharingType", is("MUDHARABAH")))
                .andExpect(jsonPath("$[0].nisbahCustomer", is(0.7000)))
                .andExpect(jsonPath("$[0].nisbahBank", is(0.3000)))
                .andExpect(jsonPath("$[0].isShariahCompliant", is(true)));
        
        verify(productRepository).findAll();
    }

    @Test
    @WithMockUser(authorities = {"PRODUCT_VIEW"})
    @DisplayName("Should return products with correct decimal precision")
    void shouldReturnProductsWithCorrectDecimalPrecision() throws Exception {
        // Given
        Product product = createTabunganWadiahProduct("TWB001", "Precision Test");
        product.setMinimumOpeningBalance(new BigDecimal("123456.78"));
        product.setMonthlyMaintenanceFee(new BigDecimal("2500.50"));
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        // When & Then
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].minimumOpeningBalance", is(123456.78)))
                .andExpect(jsonPath("$[0].monthlyMaintenanceFee", is(2500.50)));
        
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should require authentication for API access")
    void shouldRequireAuthenticationForApiAccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        
        verifyNoInteractions(productRepository);
    }

    // Helper methods
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
        product.setLaunchDate(LocalDate.of(2024, 1, 1));
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
}