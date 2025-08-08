package id.ac.tazkia.minibank.unit.controller.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import id.ac.tazkia.minibank.controller.web.ProductController;
import id.ac.tazkia.minibank.service.ProductService;

@WebMvcTest(ProductController.class)
public class ProductControllerValidationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private ProductService productService;
    
    @Test
    @WithMockUser
    public void testValidationWithEmptyFields() throws Exception {
        mockMvc.perform(post("/product/create")
                .with(csrf())  // Add CSRF token
                .param("productCode", "")  // Empty product code
                .param("productName", "")  // Empty product name
                .param("productCategory", "")  // Empty category
                .param("isActive", "true"))
                .andExpect(status().isOk())  // Should return to form with validation errors
                .andExpect(view().name("product/form"))  // Should stay on form page
                .andExpect(model().hasErrors());  // Should have validation errors
    }
}