package id.ac.tazkia.minibank.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.integration.web.pageobject.ProductFormPage;
import id.ac.tazkia.minibank.integration.web.pageobject.ProductListPage;
import id.ac.tazkia.minibank.repository.ProductRepository;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProductWebIntegrationTest extends BaseSeleniumTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void testProductListPageLoads() {
        ProductListPage listPage = new ProductListPage(driver, baseUrl);
        listPage.open();
        
        assertTrue(driver.getCurrentUrl().contains("/product/list"));
        assertEquals("Product Management - Minibank", driver.getTitle());
    }
    
    @Test
    void testCreateProductBasicFlow() {
        ProductListPage listPage = new ProductListPage(driver, baseUrl);
        listPage.open();
        
        ProductFormPage formPage = listPage.clickCreateProduct();
        assertTrue(driver.getCurrentUrl().contains("/product/create"));
        
        formPage.fillCompleteProduct(
            "TEST001", 
            "Test Savings Product", 
            "SAVINGS", 
            "Test Category", 
            "Test description for savings product", 
            "0.015"
        );
        
        ProductListPage resultPage = formPage.submitForm();
        assertTrue(resultPage.isSuccessMessageDisplayed());
        assertTrue(resultPage.isProductDisplayed("TEST001"));
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/product/product-creation-data.csv", numLinesToSkip = 1)
    void testCreateProductWithVariousData(String productCode, String productName, String productType,
                                         String productCategory, String description, String interestRate,
                                         boolean isActive, boolean isDefault, String minimumOpeningBalance,
                                         String minimumBalance) {
        
        ProductListPage listPage = new ProductListPage(driver, baseUrl);
        listPage.open();
        
        ProductFormPage formPage = listPage.clickCreateProduct();
        
        formPage.fillBasicInformation(productCode, productName, productType, productCategory, "IDR");
        formPage.fillDescription(description);
        formPage.setSettings(isActive, isDefault, false, true);
        formPage.fillFinancialConfiguration(minimumOpeningBalance, minimumBalance, null,
                                           interestRate, "DAILY", "MONTHLY");
        
        ProductListPage resultPage = formPage.submitForm();
        
        assertTrue(resultPage.isSuccessMessageDisplayed(), 
                  "Product creation should succeed for: " + productCode);
        assertTrue(resultPage.isProductDisplayed(productCode), 
                  "Product should be displayed in list: " + productCode);
        
        // Verify in database
        Product savedProduct = productRepository.findByProductCode(productCode).orElse(null);
        assertNotNull(savedProduct, "Product should be saved in database");
        assertEquals(productName, savedProduct.getProductName());
        assertEquals(productType, savedProduct.getProductType().toString());
    }
    
    @ParameterizedTest @Transactional
    @CsvFileSource(resources = "/fixtures/product/product-validation-data.csv", numLinesToSkip = 1)  
    void testProductValidationErrors(String testCase, String productCode, String productName, 
                                   String productType, String expectedError) {
        
        ProductListPage listPage = new ProductListPage(driver, baseUrl);
        listPage.open();
        
        ProductFormPage formPage = listPage.clickCreateProduct();
        
        // Fill form with potentially invalid data
        // Always fill all required fields with valid defaults, then override the field being tested
        String defaultProductCode = "TEST" + System.currentTimeMillis();
        String defaultProductName = "Test Product";
        String defaultProductType = "SAVINGS";
        String defaultCategory = "Standard";
        
        // Override with test data (including empty values)
        String testProductCode = (productCode != null) ? productCode : defaultProductCode;
        String testProductName = (productName != null) ? productName : defaultProductName;
        String testProductType = (productType != null && !productType.isEmpty()) ? productType : defaultProductType;
        
        // For empty string tests, we need to explicitly pass empty string
        if (testCase.contains("Empty Product Code")) testProductCode = "";
        if (testCase.contains("Empty Product Name")) testProductName = "";
        if (testCase.contains("Empty Product Type")) testProductType = "";
        
        formPage.fillBasicInformation(testProductCode, testProductName, testProductType, defaultCategory, "IDR");
        
        // For invalid interest rate test
        if (testCase.contains("Interest Rate")) {
            formPage.fillFinancialConfiguration(null, null, null, "2.0", null, null); // Invalid rate > 1
        }
        
        // Create a duplicate product first for duplicate test
        if (testCase.contains("Duplicate")) {
            Product existing = new Product();
            existing.setProductCode("SAV001");
            existing.setProductName("Existing Savings");
            existing.setProductType(Product.ProductType.SAVINGS);
            existing.setIsActive(true);
            productRepository.save(existing);
        }
        
        ProductFormPage resultPage = formPage.submitFormExpectingError();
        
        // Should stay on form page with error
        assertTrue(driver.getCurrentUrl().contains("/product/create") || 
                  driver.getCurrentUrl().contains("/product/edit"),
                  "Should remain on form page after validation error: " + testCase);
        
        // Check for validation error indicators
        assertTrue(resultPage.isErrorMessageDisplayed() || 
                  hasValidationErrorOnPage(),
                  "Should display validation error for: " + testCase);
    }
    
    private boolean hasValidationErrorOnPage() {
        // Check for various validation error indicators
        return driver.getPageSource().contains("border-red-300") ||
               driver.getPageSource().contains("text-red-600") ||
               driver.getPageSource().contains("error");
    }
}