package id.ac.tazkia.minibank.integration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import id.ac.tazkia.minibank.controller.web.ProductController;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.service.ProductService;

@WebMvcTest(ProductController.class)
@WithMockUser
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private ProductService productService;

    @Test
    public void testProductList() throws Exception {
        // Mock the service to return an empty page
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
        when(productService.findAll(any(Pageable.class))).thenReturn(emptyPage);
        
        mockMvc.perform(get("/product/list"))
                .andExpect(status().isOk());
    }
}