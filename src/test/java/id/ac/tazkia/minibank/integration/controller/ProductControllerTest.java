package id.ac.tazkia.minibank.integration.controller;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.config.TestPasswordEncoderConfig;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import({PostgresTestContainersConfiguration.class, TestPasswordEncoderConfig.class})
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductController Integration Tests")
class ProductControllerTest {

    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private ProductService productService;
    
    private MockMvc mockMvc;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        
        // Create test product
        testProduct = new Product();
        testProduct.setProductCode("TEST001");
        testProduct.setProductName("Test Savings Product");
        testProduct.setProductType(Product.ProductType.SAVINGS);
        testProduct.setProductCategory("Personal");
        testProduct.setDescription("Test product description");
        testProduct.setMinimumBalance(new BigDecimal("100000"));
        testProduct.setMaximumBalance(new BigDecimal("10000000"));
        testProduct.setProfitSharingRatio(new BigDecimal("0.05"));
        testProduct.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
        testProduct.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        // Fix nisbah values to sum to 1.0 for Islamic banking constraint
        testProduct.setNisbahCustomer(new BigDecimal("0.60"));
        testProduct.setNisbahBank(new BigDecimal("0.40"));
        testProduct.setIsActive(true);
        testProduct = productService.save(testProduct);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display products list page")
    void shouldDisplayProductsListPage() throws Exception {
        mockMvc.perform(get("/product/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("totalItems"))
                .andExpect(model().attributeExists("pageSize"))
                .andExpect(model().attributeExists("sortBy"))
                .andExpect(model().attributeExists("sortDir"))
                .andExpect(model().attributeExists("productTypes"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display products list with pagination parameters")
    void shouldDisplayProductsListWithPagination() throws Exception {
        mockMvc.perform(get("/product/list")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "productCode")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 5))
                .andExpect(model().attribute("sortBy", "productCode"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display products list with filters")
    void shouldDisplayProductsListWithFilters() throws Exception {
        mockMvc.perform(get("/product/list")
                        .param("productType", "SAVINGS")
                        .param("category", "Personal")
                        .param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attribute("productType", Product.ProductType.SAVINGS))
                .andExpect(model().attribute("category", "Personal"))
                .andExpect(model().attribute("search", "Test"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display create product form")
    void shouldDisplayCreateProductForm() throws Exception {
        mockMvc.perform(get("/product/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/form"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("productTypes"))
                .andExpect(model().attributeExists("profitSharingTypes"))
                .andExpect(model().attributeExists("profitDistributionFrequencies"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() throws Exception {
        mockMvc.perform(post("/product/create")
                        .param("productCode", "NEW001")
                        .param("productName", "New Product")
                        .param("productType", "SAVINGS")
                        .param("productCategory", "Business")
                        .param("description", "New product description")
                        .param("minimumBalance", "50000")
                        .param("maximumBalance", "5000000")
                        .param("interestRate", "0.02")
                        .param("profitSharingRatio", "0.03")
                        .param("profitSharingType", "MUDHARABAH")
                        .param("profitDistributionFrequency", "QUARTERLY")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("successMessage", "Product created successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject create product with duplicate product code")
    void shouldRejectCreateProductWithDuplicateProductCode() throws Exception {
        mockMvc.perform(post("/product/create")
                        .param("productCode", testProduct.getProductCode())
                        .param("productName", "Different Product")
                        .param("productType", "CHECKING")
                        .param("productCategory", "Personal")
                        .param("description", "Different description")
                        .param("minimumBalance", "100000")
                        .param("maximumBalance", "1000000")
                        .param("interestRate", "0.01")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("productTypes"))
                .andExpect(model().attributeExists("profitSharingTypes"))
                .andExpect(model().attributeExists("profitDistributionFrequencies"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle validation errors on create")
    void shouldHandleValidationErrorsOnCreate() throws Exception {
        mockMvc.perform(post("/product/create")
                        .param("productCode", "")  // Invalid: empty code
                        .param("productName", "")   // Invalid: empty name
                        .param("productType", "SAVINGS")
                        .param("productCategory", "Personal")
                        .param("minimumBalance", "-100")) // Invalid: negative balance
                .andExpect(status().isOk())
                .andExpect(view().name("product/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("productTypes"))
                .andExpect(model().attributeExists("profitSharingTypes"))
                .andExpect(model().attributeExists("profitDistributionFrequencies"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display edit product form")
    void shouldDisplayEditProductForm() throws Exception {
        mockMvc.perform(get("/product/edit/" + testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product/form"))
                .andExpect(model().attribute("product", hasProperty("id", is(testProduct.getId()))))
                .andExpect(model().attributeExists("productTypes"))
                .andExpect(model().attributeExists("profitSharingTypes"))
                .andExpect(model().attributeExists("profitDistributionFrequencies"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should redirect when edit non-existing product")
    void shouldRedirectWhenEditNonExistingProduct() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/product/edit/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("errorMessage", "Product not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() throws Exception {
        mockMvc.perform(post("/product/edit/" + testProduct.getId())
                        .param("productCode", "UPDATED001")
                        .param("productName", "Updated Product")
                        .param("productType", "CHECKING")
                        .param("productCategory", "Business")
                        .param("description", "Updated description")
                        .param("minimumBalance", "200000")
                        .param("maximumBalance", "20000000")
                        .param("interestRate", "0.025")
                        .param("profitSharingRatio", "0.08")
                        .param("profitSharingType", "MUDHARABAH")
                        .param("profitDistributionFrequency", "ANNUALLY")
                        .param("isActive", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("successMessage", "Product updated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject update product with duplicate product code")
    void shouldRejectUpdateProductWithDuplicateProductCode() throws Exception {
        // Create another product
        Product anotherProduct = new Product();
        anotherProduct.setProductCode("ANOTHER001");
        anotherProduct.setProductName("Another Product");
        anotherProduct.setProductType(Product.ProductType.CHECKING);
        anotherProduct.setProductCategory("Business");
        anotherProduct.setMinimumBalance(new BigDecimal("50000"));
        // Fix nisbah values to sum to 1.0 for Islamic banking constraint
        anotherProduct.setNisbahCustomer(new BigDecimal("0.70"));
        anotherProduct.setNisbahBank(new BigDecimal("0.30"));
        anotherProduct.setIsActive(true);
        anotherProduct = productService.save(anotherProduct);

        // Try to update testProduct with anotherProduct's code
        mockMvc.perform(post("/product/edit/" + testProduct.getId())
                        .param("productCode", anotherProduct.getProductCode())
                        .param("productName", "Updated Product")
                        .param("productType", "SAVINGS")
                        .param("productCategory", "Personal")
                        .param("description", "Updated description")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("productTypes"))
                .andExpect(model().attributeExists("profitSharingTypes"))
                .andExpect(model().attributeExists("profitDistributionFrequencies"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should allow updating product with same product code")
    void shouldAllowUpdatingProductWithSameProductCode() throws Exception {
        mockMvc.perform(post("/product/edit/" + testProduct.getId())
                        .param("productCode", testProduct.getProductCode()) // Same code
                        .param("productName", "Updated Product Name")        // Different name
                        .param("productType", "SAVINGS")
                        .param("productCategory", "Personal")
                        .param("description", "Updated description")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("successMessage", "Product updated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should display product view")
    void shouldDisplayProductView() throws Exception {
        mockMvc.perform(get("/product/view/" + testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product/view"))
                .andExpect(model().attribute("product", hasProperty("id", is(testProduct.getId()))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should redirect when view non-existing product")
    void shouldRedirectWhenViewNonExistingProduct() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/product/view/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("errorMessage", "Product not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should soft delete product successfully")
    void shouldSoftDeleteProductSuccessfully() throws Exception {
        mockMvc.perform(post("/product/delete/" + testProduct.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("successMessage", "Product deactivated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should activate product successfully")
    void shouldActivateProductSuccessfully() throws Exception {
        // First deactivate the product
        testProduct.setIsActive(false);
        productService.update(testProduct);

        mockMvc.perform(post("/product/activate/" + testProduct.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("successMessage", "Product activated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle activate non-existing product")
    void shouldHandleActivateNonExistingProduct() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(post("/product/activate/" + nonExistingId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product/list"))
                .andExpect(flash().attribute("errorMessage", "Product not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle validation errors on update")
    void shouldHandleValidationErrorsOnUpdate() throws Exception {
        mockMvc.perform(post("/product/edit/" + testProduct.getId())
                        .param("productCode", "")  // Invalid: empty code
                        .param("productName", "")   // Invalid: empty name
                        .param("productType", "SAVINGS")
                        .param("productCategory", "Personal")
                        .param("minimumBalance", "-100")) // Invalid: negative balance
                .andExpect(status().isOk())
                .andExpect(view().name("product/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("productTypes"))
                .andExpect(model().attributeExists("profitSharingTypes"))
                .andExpect(model().attributeExists("profitDistributionFrequencies"));
    }
}