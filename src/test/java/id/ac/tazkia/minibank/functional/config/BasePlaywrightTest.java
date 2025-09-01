package id.ac.tazkia.minibank.functional.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.playwright.*;
import id.ac.tazkia.minibank.config.BaseIntegrationTest;

import lombok.extern.slf4j.Slf4j;

/**
 * Base class for Playwright functional tests.
 * Extends BaseIntegrationTest to inherit database setup and Spring context.
 * Manages Playwright browser lifecycle for each test.
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BasePlaywrightTest extends BaseIntegrationTest {

    @LocalServerPort
    protected int serverPort;

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext browserContext;
    protected Page page;
    protected String baseUrl;

    @BeforeEach
    void setUpPlaywright() {
        // Ensure test data is available
        ensureTestDataExists();
        
        // Initialize Playwright
        playwright = Playwright.create();
        
        // Configure browser based on system properties
        boolean headless = Boolean.parseBoolean(System.getProperty("playwright.headless", "true"));
        String browserName = System.getProperty("playwright.browser", "chromium");
        double slowMo = Double.parseDouble(System.getProperty("playwright.slowmo", "0"));
        
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
            .setHeadless(headless)
            .setSlowMo(slowMo); // Configurable slow motion delay in milliseconds
            
        // Launch browser based on configuration
        switch (browserName.toLowerCase()) {
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "webkit":
                browser = playwright.webkit().launch(launchOptions);
                break;
            default:
                browser = playwright.chromium().launch(launchOptions);
                break;
        }
        
        // Configure recording if enabled
        boolean recordVideo = Boolean.parseBoolean(System.getProperty("playwright.record", "false"));
        String recordMode = System.getProperty("playwright.record.mode", "retain-on-failure");
        
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
            .setViewportSize(1920, 1080)
            .setIgnoreHTTPSErrors(true);
            
        // Add video recording if enabled
        if (recordVideo) {
            String recordDir = System.getProperty("playwright.record.dir", "target/playwright-recordings");
            java.nio.file.Path recordPath = java.nio.file.Paths.get(recordDir);
            
            // Ensure recording directory exists
            try {
                java.nio.file.Files.createDirectories(recordPath);
                log.info("📁 Recording directory created/verified: {}", recordPath.toAbsolutePath());
            } catch (Exception e) {
                log.warn("Failed to create recording directory: {}", e.getMessage());
            }
            
            contextOptions.setRecordVideoDir(recordPath);
            
            // Configure recording size (optional)
            contextOptions.setRecordVideoSize(1920, 1080);
            log.info("🎥 Video recording enabled - Directory: {} | Mode: {}", recordPath.toAbsolutePath(), recordMode);
        }
        
        // Create browser context with recording configuration
        browserContext = browser.newContext(contextOptions);
        
        // Create page
        page = browserContext.newPage();
        
        // Set up base URL
        baseUrl = "http://localhost:" + serverPort;
        
        log.debug("🔧 Playwright Setup Complete - URL: {} | Browser: {} | Headless: {} | Recording: {} | SlowMo: {}ms", 
                baseUrl, browserName, headless, recordVideo, slowMo);
    }

    @AfterEach
    void tearDownPlaywright() {
        boolean recordVideo = Boolean.parseBoolean(System.getProperty("playwright.record", "false"));
        
        if (page != null) {
            try {
                page.close();
                log.debug("Page closed successfully");
            } catch (Exception e) {
                log.warn("Error closing page: {}", e.getMessage());
            }
        }
        
        if (browserContext != null) {
            try {
                // Save video before closing context
                if (recordVideo && page != null) {
                    try {
                        String recordDir = System.getProperty("playwright.record.dir", "target/playwright-recordings");
                        java.nio.file.Path videoPath = page.video().path();
                        log.info("🎥 Video saved to: {}", videoPath.toAbsolutePath());
                        
                        // Ensure the video file exists after context close
                        browserContext.close();
                        
                        // Verify video file was created
                        if (java.nio.file.Files.exists(videoPath)) {
                            log.info("✅ Video recording confirmed: {} (size: {} bytes)", 
                                videoPath.toAbsolutePath(), 
                                java.nio.file.Files.size(videoPath));
                        } else {
                            log.warn("❌ Video file not found after recording: {}", videoPath.toAbsolutePath());
                        }
                    } catch (Exception e) {
                        log.warn("Error handling video recording: {}", e.getMessage());
                        browserContext.close(); // Ensure context still closes
                    }
                } else {
                    browserContext.close();
                }
                
                log.debug("Browser context closed successfully");
            } catch (Exception e) {
                log.warn("Error closing browser context: {}", e.getMessage());
            }
        }
        
        if (browser != null) {
            try {
                browser.close();
                log.debug("Browser closed successfully");
            } catch (Exception e) {
                log.warn("Error closing browser: {}", e.getMessage());
            }
        }
        
        if (playwright != null) {
            try {
                playwright.close();
                log.debug("Playwright closed successfully");
            } catch (Exception e) {
                log.warn("Error closing Playwright: {}", e.getMessage());
            }
        }
        
        log.debug("Playwright cleanup completed");
    }
    
    /**
     * Ensures that the required test data exists in the database.
     * This includes customers from the seed data that tests depend on.
     * If no customers exist, creates test customers with TEST_ prefix.
     * Also resets sequences to avoid account number collisions.
     */
    protected void ensureTestDataExists() {
        try {
            // Reset sequences to avoid collisions from previous test runs
            resetSequencesForTest();
            
            // Check if customers exist in database
            Integer customerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers", Integer.class);
                
            if (customerCount == null || customerCount == 0) {
                log.warn("No customers found in database. Creating test customers...");
                createTestCustomers();
            } else {
                log.debug("Found {} customers in database for testing", customerCount);
                
                // Log available customers for debugging
                jdbcTemplate.query(
                    "SELECT customer_number, customer_type FROM customers ORDER BY customer_number",
                    (rs) -> {
                        log.debug("Available customer: {} ({})", 
                            rs.getString("customer_number"), 
                            rs.getString("customer_type"));
                    }
                );
                
                // Also log personal customer names for debugging
                jdbcTemplate.query(
                    """
                    SELECT c.customer_number, p.first_name, p.last_name 
                    FROM customers c 
                    JOIN personal_customers p ON c.id = p.id 
                    ORDER BY c.customer_number
                    """,
                    (rs) -> {
                        log.debug("Personal customer: {} - {} {}", 
                            rs.getString("customer_number"),
                            rs.getString("first_name"), 
                            rs.getString("last_name"));
                    }
                );
            }
        } catch (Exception e) {
            log.error("Error checking test data availability: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Creates test customers if none exist in the database.
     * Uses TEST_ prefix for easy identification and cleanup.
     */
    private void createTestCustomers() {
        try {
            // Get a random branch ID
            String branchId = jdbcTemplate.queryForObject(
                "SELECT id FROM branches LIMIT 1", String.class);
            
            if (branchId == null) {
                log.error("No branches found - cannot create test customers");
                return;
            }
            
            // Create test customers with random suffix to avoid ID conflicts
            long timestamp = System.currentTimeMillis();
            String suffix = String.valueOf(timestamp % 100000); // Last 5 digits
            
            // Test customer 1: Ahmad Suharto
            String customerId1 = java.util.UUID.randomUUID().toString();
            String customerNumber1 = "TEST_C" + suffix + "01";
            
            jdbcTemplate.update("""
                INSERT INTO customers (id, customer_type, customer_number, id_branches, 
                    email, phone_number, address, city, postal_code, status, created_by)
                VALUES (?, 'PERSONAL', ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', 'TEST_SYSTEM')
                """, customerId1, customerNumber1, branchId,
                "test.ahmad." + suffix + "@test.com", "0812345" + suffix,
                "Test Address Ahmad " + suffix, "Jakarta", "10220");
                
            jdbcTemplate.update("""
                INSERT INTO personal_customers (id, first_name, last_name, date_of_birth, 
                    identity_number, identity_type)
                VALUES (?, 'Ahmad', 'Suharto', '1985-03-15', ?, 'KTP')
                """, customerId1, "327108150385" + suffix);
            
            // Test customer 2: Siti Nurhaliza  
            String customerId2 = java.util.UUID.randomUUID().toString();
            String customerNumber2 = "TEST_C" + suffix + "02";
            
            jdbcTemplate.update("""
                INSERT INTO customers (id, customer_type, customer_number, id_branches,
                    email, phone_number, address, city, postal_code, status, created_by)
                VALUES (?, 'PERSONAL', ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', 'TEST_SYSTEM')
                """, customerId2, customerNumber2, branchId,
                "test.siti." + suffix + "@test.com", "0812346" + suffix,
                "Test Address Siti " + suffix, "Jakarta", "10230");
                
            jdbcTemplate.update("""
                INSERT INTO personal_customers (id, first_name, last_name, date_of_birth,
                    identity_number, identity_type)
                VALUES (?, 'Siti', 'Nurhaliza', '1990-07-22', ?, 'KTP')
                """, customerId2, "327108220790" + suffix);
            
            // Test corporate customer: PT. Teknologi Maju
            String customerId3 = java.util.UUID.randomUUID().toString();
            String customerNumber3 = "TEST_C" + suffix + "03";
            
            jdbcTemplate.update("""
                INSERT INTO customers (id, customer_type, customer_number, id_branches,
                    email, phone_number, address, city, postal_code, status, created_by)
                VALUES (?, 'CORPORATE', ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', 'TEST_SYSTEM')
                """, customerId3, customerNumber3, branchId,
                "test.teknologi." + suffix + "@test.com", "02123456" + suffix,
                "Test Corp Address " + suffix, "Jakarta", "12950");
                
            jdbcTemplate.update("""
                INSERT INTO corporate_customers (id, company_name, company_registration_number,
                    tax_identification_number)
                VALUES (?, 'PT. Teknologi Maju', ?, ?)
                """, customerId3, "123456789012" + suffix, "01.234.567." + suffix + ".000");
            
            log.info("Created test customers with suffix: {}", suffix);
            log.info("Test customers: {} (Ahmad Suharto), {} (Siti Nurhaliza), {} (PT. Teknologi Maju)",
                customerNumber1, customerNumber2, customerNumber3);
            
        } catch (Exception e) {
            log.error("Error creating test customers: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Resets sequence numbers to avoid collisions from previous test runs.
     * Uses a high starting number with timestamp suffix to ensure uniqueness.
     */
    private void resetSequencesForTest() {
        try {
            // Use current time millis to create unique sequence starting points
            long timestamp = System.currentTimeMillis();
            long accountStartNumber = 9000000 + (timestamp % 100000); // Start from 9M+ range
            long transactionStartNumber = 8000000 + (timestamp % 100000); // Start from 8M+ range
            
            // Update sequences to start from high numbers to avoid collision with seed data
            jdbcTemplate.update(
                "UPDATE sequence_numbers SET last_number = ? WHERE sequence_name = ?",
                accountStartNumber, "ACCOUNT_NUMBER");
                
            jdbcTemplate.update(
                "UPDATE sequence_numbers SET last_number = ? WHERE sequence_name = ?",
                transactionStartNumber, "TRANSACTION_NUMBER");
                
            // Also handle corporate account sequence
            jdbcTemplate.update(
                "INSERT INTO sequence_numbers (sequence_name, last_number, prefix) VALUES (?, ?, ?) " +
                "ON CONFLICT (sequence_name) DO UPDATE SET last_number = ?",
                "CORPORATE_ACCOUNT_NUMBER", accountStartNumber + 100000, "CORP", accountStartNumber + 100000);
                
            log.debug("Reset sequences - ACCOUNT_NUMBER: {}, TRANSACTION_NUMBER: {}", 
                accountStartNumber, transactionStartNumber);
                
        } catch (Exception e) {
            log.warn("Could not reset sequences (may not exist yet): {}", e.getMessage());
        }
    }
}