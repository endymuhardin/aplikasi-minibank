package id.ac.tazkia.minibank.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import id.ac.tazkia.minibank.controller.web.ProductController;

@WebMvcTest(ProductController.class)
@WithMockUser
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testProductList() throws Exception {
        mockMvc.perform(get("/product/list"))
                .andExpect(status().isOk());
    }
}