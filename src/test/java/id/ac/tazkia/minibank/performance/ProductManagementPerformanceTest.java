package id.ac.tazkia.minibank.performance;

import id.ac.tazkia.minibank.config.BaseIntegrationTest;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Product Management Performance Tests")
class ProductManagementPerformanceTest extends BaseIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private static final int LARGE_DATASET_SIZE = 1000;
    private static final int CONCURRENT_THREADS = 10;
    private static final long ACCEPTABLE_RESPONSE_TIME_MS = 3000; // 3 seconds

    @BeforeEach
    @Transactional
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should handle large dataset efficiently - 1000+ products")
    void shouldHandleLargeDatasetEfficiently() throws Exception {
        log.info("Performance Test: Large dataset handling with {} products", LARGE_DATASET_SIZE);
        
        // Given - Create large dataset
        long startTime = System.currentTimeMillis();
        
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= LARGE_DATASET_SIZE; i++) {
            Product product = createTestProduct(
                String.format("PERF%04d", i),
                String.format("Performance Test Product %04d", i),
                i % 2 == 0 ? Product.ProductType.TABUNGAN_WADIAH : Product.ProductType.TABUNGAN_MUDHARABAH
            );
            products.add(product);
            
            // Batch save every 100 products to avoid memory issues
            if (i % 100 == 0) {
                productRepository.saveAll(products);
                products.clear();
                log.debug("Saved batch up to product {}", i);
            }
        }
        
        // Save remaining products
        if (!products.isEmpty()) {
            productRepository.saveAll(products);
        }
        
        long dataCreationTime = System.currentTimeMillis() - startTime;
        log.info("Dataset creation time: {}ms", dataCreationTime);
        
        // When - Test various operations on large dataset
        
        // Test 1: Find all with pagination
        startTime = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(0, 50);
        Page<Product> firstPage = productService.findAll(pageable);
        long findAllTime = System.currentTimeMillis() - startTime;
        
        assertEquals(50, firstPage.getContent().size());
        assertEquals(LARGE_DATASET_SIZE, firstPage.getTotalElements());
        assertTrue(findAllTime < ACCEPTABLE_RESPONSE_TIME_MS, 
                  "Find all with pagination took too long: " + findAllTime + "ms");
        
        // Test 2: Search with filters
        startTime = System.currentTimeMillis();
        Page<Product> filteredResults = productService.findWithFilters(
            Product.ProductType.TABUNGAN_WADIAH, null, null, pageable);
        long filterTime = System.currentTimeMillis() - startTime;
        
        assertEquals(LARGE_DATASET_SIZE / 2, filteredResults.getTotalElements());
        assertTrue(filterTime < ACCEPTABLE_RESPONSE_TIME_MS,
                  "Filter operation took too long: " + filterTime + "ms");
        
        // Test 3: Text search
        startTime = System.currentTimeMillis();
        Page<Product> searchResults = productService.findWithFilters(
            null, null, "Performance Test Product", pageable);
        long searchTime = System.currentTimeMillis() - startTime;
        
        assertTrue(searchResults.getTotalElements() > 0);
        assertTrue(searchTime < ACCEPTABLE_RESPONSE_TIME_MS,
                  "Text search took too long: " + searchTime + "ms");
        
        // Test 4: Find by product type
        startTime = System.currentTimeMillis();
        List<Product> typeResults = productService.findByProductType(Product.ProductType.TABUNGAN_MUDHARABAH);
        long typeSearchTime = System.currentTimeMillis() - startTime;
        
        assertEquals(LARGE_DATASET_SIZE / 2, typeResults.size());
        assertTrue(typeSearchTime < ACCEPTABLE_RESPONSE_TIME_MS,
                  "Type search took too long: " + typeSearchTime + "ms");
        
        log.info("✅ Large dataset performance test completed successfully");
        log.info("Performance metrics - FindAll: {}ms, Filter: {}ms, Search: {}ms, TypeSearch: {}ms",
                findAllTime, filterTime, searchTime, typeSearchTime);
    }

    @Test
    @DisplayName("Should handle concurrent read operations safely")
    void shouldHandleConcurrentReadOperationsSafely() throws Exception {
        log.info("Performance Test: Concurrent read operations with {} threads", CONCURRENT_THREADS);
        
        // Given - Create test data
        List<Product> testProducts = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            testProducts.add(createTestProduct(
                String.format("CONC%03d", i),
                String.format("Concurrent Test Product %03d", i),
                Product.ProductType.TABUNGAN_WADIAH
            ));
        }
        productRepository.saveAll(testProducts);
        
        // When - Execute concurrent reads
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CompletionService<Long> completionService = new ExecutorCompletionService<>(executor);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        // Submit concurrent read tasks
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            completionService.submit(() -> {
                try {
                    long threadStartTime = System.currentTimeMillis();
                    
                    // Perform various read operations
                    Pageable pageable = PageRequest.of(0, 20);
                    
                    // Operation 1: Find all
                    Page<Product> page1 = productService.findAll(pageable);
                    assertNotNull(page1);
                    
                    // Operation 2: Find with filters
                    Page<Product> page2 = productService.findWithFilters(
                        Product.ProductType.TABUNGAN_WADIAH, null, null, pageable);
                    assertNotNull(page2);
                    
                    // Operation 3: Find by product code
                    String productCode = String.format("CONC%03d", (threadId % 100) + 1);
                    var productOpt = productService.findByProductCode(productCode);
                    assertNotNull(productOpt);
                    
                    // Operation 4: Check if product exists
                    boolean exists = productService.existsByProductCode(productCode);
                    assertTrue(exists);
                    
                    successCount.incrementAndGet();
                    return System.currentTimeMillis() - threadStartTime;
                    
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("Thread {} failed: {}", threadId, e.getMessage());
                    throw new RuntimeException(e);
                }
            });
        }
        
        // Collect results
        List<Long> executionTimes = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            try {
                Future<Long> future = completionService.take();
                executionTimes.add(future.get(10, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                log.error("Thread timed out");
                errorCount.incrementAndGet();
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        executor.shutdown();
        
        // Then - Verify results
        assertEquals(0, errorCount.get(), "No errors should occur during concurrent reads");
        assertEquals(CONCURRENT_THREADS, successCount.get(), "All threads should complete successfully");
        
        double avgExecutionTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxExecutionTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        
        assertTrue(totalTime < ACCEPTABLE_RESPONSE_TIME_MS * 2, 
                  "Total concurrent execution time too long: " + totalTime + "ms");
        assertTrue(avgExecutionTime < ACCEPTABLE_RESPONSE_TIME_MS,
                  "Average execution time too long: " + avgExecutionTime + "ms");
        
        log.info("✅ Concurrent read operations completed successfully");
        log.info("Concurrent metrics - Total: {}ms, Avg: {}ms, Max: {}ms, Success: {}, Errors: {}",
                totalTime, Math.round(avgExecutionTime), maxExecutionTime, successCount.get(), errorCount.get());
    }

    @Test
    @DisplayName("Should handle concurrent write operations with proper isolation")
    void shouldHandleConcurrentWriteOperationsWithProperIsolation() throws Exception {
        log.info("Performance Test: Concurrent write operations with {} threads", CONCURRENT_THREADS);
        
        // Given - Prepare for concurrent writes
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<String> createdProductCodes = new CopyOnWriteArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // When - Execute concurrent write operations
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            completionService.submit(() -> {
                try {
                    String productCode = String.format("WRITE%03d_%d", threadId, System.currentTimeMillis() % 1000);
                    
                    Product product = createTestProduct(
                        productCode,
                        String.format("Concurrent Write Test %03d", threadId),
                        Product.ProductType.TABUNGAN_WADIAH
                    );
                    
                    // Save product
                    Product savedProduct = productService.save(product);
                    assertNotNull(savedProduct.getId());
                    
                    // Verify it was saved correctly
                    var retrievedOpt = productService.findByProductCode(productCode);
                    assertTrue(retrievedOpt.isPresent());
                    
                    createdProductCodes.add(productCode);
                    successCount.incrementAndGet();
                    
                    return productCode;
                    
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("Write thread {} failed: {}", threadId, e.getMessage());
                    return "ERROR_" + threadId;
                }
            });
        }
        
        // Collect results
        List<String> results = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            try {
                Future<String> future = completionService.take();
                results.add(future.get(10, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                log.error("Write thread timed out");
                errorCount.incrementAndGet();
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        executor.shutdown();
        
        // Then - Verify results
        assertTrue(errorCount.get() < CONCURRENT_THREADS / 2, 
                  "Too many write errors: " + errorCount.get());
        assertTrue(successCount.get() > CONCURRENT_THREADS / 2,
                  "Not enough successful writes: " + successCount.get());
        
        // Verify no duplicate product codes were created
        assertEquals(createdProductCodes.size(), createdProductCodes.stream().distinct().count(),
                    "No duplicate product codes should be created");
        
        // Verify all successful writes are actually in database
        for (String code : createdProductCodes) {
            assertTrue(productService.existsByProductCode(code),
                      "Product code " + code + " should exist in database");
        }
        
        log.info("✅ Concurrent write operations completed");
        log.info("Write metrics - Total: {}ms, Success: {}, Errors: {}, Unique products: {}",
                totalTime, successCount.get(), errorCount.get(), createdProductCodes.size());
    }

    @Test
    @DisplayName("Should handle mixed concurrent read/write operations efficiently")
    void shouldHandleMixedConcurrentOperationsEfficiently() throws Exception {
        log.info("Performance Test: Mixed concurrent read/write operations");
        
        // Given - Create initial test data
        for (int i = 1; i <= 50; i++) {
            Product product = createTestProduct(
                String.format("MIX%03d", i),
                String.format("Mixed Operations Product %03d", i),
                i % 2 == 0 ? Product.ProductType.TABUNGAN_WADIAH : Product.ProductType.TABUNGAN_MUDHARABAH
            );
            productService.save(product);
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
        
        AtomicInteger readCount = new AtomicInteger(0);
        AtomicInteger writeCount = new AtomicInteger(0);
        AtomicInteger updateCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        // When - Execute mixed operations
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            final String operation = (i % 3 == 0) ? "READ" : (i % 3 == 1) ? "WRITE" : "UPDATE";
            
            completionService.submit(() -> {
                try {
                    switch (operation) {
                        case "READ" -> {
                            // Perform various read operations
                            Pageable pageable = PageRequest.of(0, 10);
                            productService.findAll(pageable);
                            productService.findActiveProducts();
                            productService.findDistinctCategories();
                            readCount.incrementAndGet();
                            return "READ_" + threadId;
                        }
                        case "WRITE" -> {
                            // Create new products
                            String code = String.format("NEW%03d_%d", threadId, System.currentTimeMillis() % 1000);
                            Product product = createTestProduct(code, "New Product " + threadId, Product.ProductType.TABUNGAN_WADIAH);
                            productService.save(product);
                            writeCount.incrementAndGet();
                            return "WRITE_" + threadId;
                        }
                        case "UPDATE" -> {
                            // Update existing products
                            String existingCode = String.format("MIX%03d", (threadId % 50) + 1);
                            var existingOpt = productService.findByProductCode(existingCode);
                            if (existingOpt.isPresent()) {
                                Product existing = existingOpt.get();
                                existing.setDescription("Updated by thread " + threadId);
                                productService.update(existing);
                                updateCount.incrementAndGet();
                            }
                            return "UPDATE_" + threadId;
                        }
                        default -> {
                            return "UNKNOWN_" + threadId;
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.error("Mixed operation {} failed in thread {}: {}", operation, threadId, e.getMessage());
                    return "ERROR_" + threadId;
                }
            });
        }
        
        // Collect results
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            try {
                Future<String> future = completionService.take();
                future.get(10, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.error("Mixed operation thread timed out");
                errorCount.incrementAndGet();
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        executor.shutdown();
        
        // Then - Verify results
        int totalOperations = readCount.get() + writeCount.get() + updateCount.get();
        assertTrue(totalOperations > CONCURRENT_THREADS / 2, 
                  "Should complete majority of operations successfully");
        assertTrue(errorCount.get() < CONCURRENT_THREADS / 3,
                  "Should have minimal errors during mixed operations");
        
        // Verify data integrity
        List<Product> finalProducts = productService.findAll();
        assertTrue(finalProducts.size() >= 50, "Should maintain data integrity during concurrent operations");
        
        log.info("✅ Mixed concurrent operations completed");
        log.info("Mixed metrics - Total: {}ms, Reads: {}, Writes: {}, Updates: {}, Errors: {}",
                totalTime, readCount.get(), writeCount.get(), updateCount.get(), errorCount.get());
    }

    @Test
    @DisplayName("Should perform pagination efficiently on large datasets")
    void shouldPerformPaginationEfficientlyOnLargeDatasets() {
        log.info("Performance Test: Pagination efficiency on large dataset");
        
        // Given - Create large dataset
        final int DATASET_SIZE = 500;
        List<Product> products = new ArrayList<>();
        
        for (int i = 1; i <= DATASET_SIZE; i++) {
            products.add(createTestProduct(
                String.format("PAGE%04d", i),
                String.format("Pagination Test Product %04d", i),
                Product.ProductType.TABUNGAN_WADIAH
            ));
        }
        productRepository.saveAll(products);
        
        // When - Test pagination performance at different positions
        int pageSize = 20;
        long[] pageTimes = new long[5]; // Test first, quarter, middle, three-quarter, last pages
        
        int[] pageNumbers = {0, DATASET_SIZE / 4 / pageSize, DATASET_SIZE / 2 / pageSize, 
                            3 * DATASET_SIZE / 4 / pageSize, DATASET_SIZE / pageSize - 1};
        
        for (int i = 0; i < pageNumbers.length; i++) {
            long startTime = System.currentTimeMillis();
            
            Pageable pageable = PageRequest.of(pageNumbers[i], pageSize);
            Page<Product> page = productService.findAll(pageable);
            
            pageTimes[i] = System.currentTimeMillis() - startTime;
            
            // Verify pagination correctness
            assertEquals(DATASET_SIZE, page.getTotalElements());
            assertTrue(page.getContent().size() <= pageSize);
            
            log.debug("Page {} (position {}) loaded in {}ms", 
                     pageNumbers[i], i, pageTimes[i]);
        }
        
        // Then - Verify performance consistency
        long maxTime = java.util.Arrays.stream(pageTimes).max().orElse(0L);
        long minTime = java.util.Arrays.stream(pageTimes).min().orElse(0L);
        double avgTime = java.util.Arrays.stream(pageTimes).average().orElse(0.0);
        
        // Performance requirements
        assertTrue(maxTime < ACCEPTABLE_RESPONSE_TIME_MS,
                  "Maximum pagination time too long: " + maxTime + "ms");
        assertTrue(avgTime < ACCEPTABLE_RESPONSE_TIME_MS / 2,
                  "Average pagination time too long: " + avgTime + "ms");
        
        // Consistency check - last page shouldn't be more than 3x slower than first page
        assertTrue(pageTimes[4] <= pageTimes[0] * 3,
                  "Last page significantly slower than first page");
        
        log.info("✅ Pagination performance test completed");
        log.info("Pagination metrics - Min: {}ms, Max: {}ms, Avg: {}ms", 
                minTime, maxTime, Math.round(avgTime));
    }

    @Test
    @DisplayName("Should handle memory efficiently with large result sets")
    void shouldHandleMemoryEfficientlyWithLargeResultSets() {
        log.info("Performance Test: Memory efficiency with large result sets");
        
        // Given - Create moderate dataset
        final int DATASET_SIZE = 200;
        for (int i = 1; i <= DATASET_SIZE; i++) {
            Product product = createTestProduct(
                String.format("MEM%04d", i),
                String.format("Memory Test Product %04d with longer description to increase memory usage per object", i),
                Product.ProductType.TABUNGAN_MUDHARABAH
            );
            productService.save(product);
        }
        
        // When - Test memory usage during operations
        Runtime runtime = Runtime.getRuntime();
        
        // Force garbage collection to get baseline
        System.gc();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Perform memory-intensive operations
        List<Product> allProducts = productService.findAll();
        assertEquals(DATASET_SIZE, allProducts.size());
        
        // Test pagination memory usage (should be more efficient)
        Pageable pageable = PageRequest.of(0, 50);
        Page<Product> page = productService.findAll(pageable);
        assertEquals(50, page.getContent().size());
        
        // Measure memory after operations
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterMemory - beforeMemory;
        
        // Force garbage collection and measure again
        System.gc();
        Thread.yield(); // Give GC a chance to run
        long afterGcMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Then - Verify reasonable memory usage
        log.info("Memory usage - Before: {}MB, After: {}MB, Used: {}MB, After GC: {}MB",
                beforeMemory / 1024 / 1024, afterMemory / 1024 / 1024, 
                memoryUsed / 1024 / 1024, afterGcMemory / 1024 / 1024);
        
        // Memory should not grow excessively (allow up to 50MB for this test)
        assertTrue(memoryUsed < 50 * 1024 * 1024, 
                  "Memory usage too high: " + (memoryUsed / 1024 / 1024) + "MB");
        
        log.info("✅ Memory efficiency test completed successfully");
    }

    // Helper method to create test products
    private Product createTestProduct(String code, String name, Product.ProductType type) {
        Product product = new Product();
        product.setProductCode(code);
        product.setProductName(name);
        product.setProductType(type);
        product.setProductCategory("Performance Test");
        product.setDescription("Product created for performance testing");
        product.setIsActive(true);
        product.setIsShariahCompliant(true);
        product.setCurrency("IDR");
        product.setMinimumOpeningBalance(new BigDecimal("50000"));
        product.setMinimumBalance(new BigDecimal("25000"));
        product.setShariahBoardApprovalNumber("DSN-PERF-001/2024");
        product.setShariahBoardApprovalDate(LocalDate.now());
        
        // Set profit sharing details for Mudharabah products
        if (type == Product.ProductType.TABUNGAN_MUDHARABAH) {
            product.setProfitSharingType(Product.ProfitSharingType.MUDHARABAH);
            product.setNisbahCustomer(new BigDecimal("0.7000"));
            product.setNisbahBank(new BigDecimal("0.3000"));
            product.setProfitDistributionFrequency(Product.ProfitDistributionFrequency.MONTHLY);
        } else {
            product.setProfitSharingType(Product.ProfitSharingType.WADIAH);
        }
        
        return product;
    }
}